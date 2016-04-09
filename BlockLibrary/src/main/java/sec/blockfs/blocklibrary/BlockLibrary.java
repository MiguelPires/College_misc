package sec.blockfs.blocklibrary;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import sec.blockfs.blockserver.BlockServer;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockserver.ServerErrorException;
import sec.blockfs.blockserver.WrongArgumentsException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;;

public class BlockLibrary {
    // these attributes are only public because of the tests
    public PrivateKey privateKey;
    public PublicKey publicKey;
    private Signature signAlgorithm;
    private int writeTimestamp = 0;
    private List<BlockServer> blockServers = new ArrayList<BlockServer>();

    @SuppressWarnings("unused")
    public BlockLibrary(String serviceName, String servicePort, String serviceUrl) throws InitializationFailureException {
        assert (BlockUtility.NUM_REPLICAS > 3
                * BlockUtility.NUM_FAULTS) : "Error -  the number of replicas must be larger than 3*f (number of faults)";

        String serverName = "none";
        String serverPort = "none";
        try {
            for (int i = 0; i < BlockUtility.NUM_REPLICAS; ++i) {
                serverName = serviceName + i;
                Integer port = new Integer(servicePort) + new Integer(i);
                serverPort = port.toString();
                blockServers.add((BlockServer) Naming.lookup(serviceUrl + ":" + serverPort + "/" + serverName));
                System.out.println("Connected to server: " + serviceUrl + ":" + serverPort + "/" + serverName);
            }
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new InitializationFailureException(
                    "Couldn't connect to server " + serviceUrl + ":" + serverPort + "/" + serverName);

        }
    }

    public String FS_init() throws InitializationFailureException {
        try {
            writeTimestamp = new Integer(0);
            // instantiate key generator
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(BlockUtility.KEY_SIZE, random);

            // generate keys
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();

            // initialize signing algorithm
            signAlgorithm = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            byte[] keyDigest = BlockUtility.digest(publicKey.getEncoded());
            return BlockUtility.getKeyString(keyDigest);
        } catch (Exception e) {
            throw new InitializationFailureException("Couldn't connect to server");
        }
    }

    public void FS_write(int position, int size, byte[] contents) throws OperationFailedException, WrongArgumentsException {
        try {
            if (position < 0 || size < 0 || contents == null || size > contents.length)
                throw new WrongArgumentsException("Invalid arguments");

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

            final byte[][] toWriteBlocks = new byte[endBlock - startBlock + 1][BlockUtility.BLOCK_SIZE];
            byte[][] toWriteHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int writtenBytes = 0, num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                int bytesToWrite = size - writtenBytes > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - writtenBytes;
                System.arraycopy(contents, writtenBytes, toWriteBlocks[num], 0, bytesToWrite);
                System.arraycopy(BlockUtility.digest(toWriteBlocks[num]), 0, toWriteHashes[num], 0, BlockUtility.DIGEST_SIZE);
                writtenBytes += bytesToWrite;
                ++num;
            }

            final byte[] publicKeyHash = BlockUtility.digest(publicKey.getEncoded());
            byte[] rewrittenBlock = null;
            final Semaphore readSemaphore = new Semaphore(
                    -((int) Math.ceil((BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0) - 1));

            try {
                final ConcurrentHashMap<Integer, byte[]> readBlocks = new ConcurrentHashMap<>();

                for (final BlockServer replica : blockServers) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                byte[] publicBlock = replica.get(BlockUtility.getKeyString(publicKeyHash));
                                // obtain signature
                                byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
                                System.arraycopy(publicBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);

                                // obtain data
                                int dataLength = publicBlock.length - BlockUtility.SIGNATURE_SIZE;
                                byte[] data = new byte[dataLength];
                                System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, data, 0, dataLength);

                                // verify public key block integrity
                                if (!BlockUtility.verifyDataIntegrity(data, storedSignature, publicKey))
                                    readSemaphore.release();

                                int timestamp = (byte) data[0];
                                readBlocks.put(timestamp, publicBlock);
                            } catch (RemoteException | FileNotFoundException | WrongArgumentsException | ServerErrorException e) {
                                ;
                            } finally {
                                readSemaphore.release();
                            }
                        }
                    }).start();
                }

                // wait for the (N+f)/2 fastest responses
                readSemaphore.acquire();

                byte[] chosenPublicBlock = null;
                Integer chosenTimestamp = 0;
                for (Integer readTimestamp : readBlocks.keySet()) {
                    if (readTimestamp > chosenTimestamp) {
                        chosenTimestamp = readTimestamp;
                        chosenPublicBlock = readBlocks.get(readTimestamp);
                    }
                }

                // there is no public block or the ones returned aren't enough to ensure byzantine fault tolerance
                if (chosenPublicBlock == null)
                    throw new FileNotFoundException();

                // rewrite
                int dataSize = chosenPublicBlock.length - BlockUtility.SIGNATURE_SIZE - 1;
                byte[] dataPublicBlock = new byte[dataSize];
                System.arraycopy(chosenPublicBlock, BlockUtility.SIGNATURE_SIZE + 1, dataPublicBlock, 0, dataSize);
                int publicBlockSize = dataPublicBlock.length / BlockUtility.DIGEST_SIZE;
                int newPublicBlockSize = publicBlockSize > endBlock + 1 ? publicBlockSize : endBlock + 1;

                rewrittenBlock = new byte[1 + newPublicBlockSize * BlockUtility.DIGEST_SIZE];
                ++writeTimestamp;
                rewrittenBlock[0] = (byte) writeTimestamp;

                num = 0;
                for (int i = 0; i < newPublicBlockSize; ++i) {
                    if (i >= startBlock && i >= endBlock)
                        System.arraycopy(toWriteHashes[num], 0, rewrittenBlock, 1 + i * BlockUtility.DIGEST_SIZE,
                                BlockUtility.DIGEST_SIZE);
                    else
                        System.arraycopy(dataPublicBlock, 0, rewrittenBlock, 1 + i * BlockUtility.DIGEST_SIZE,
                                BlockUtility.DIGEST_SIZE);
                }
            } catch (FileNotFoundException e) {
                // write new public key block
                rewrittenBlock = new byte[1 + toWriteHashes.length * BlockUtility.DIGEST_SIZE];
                ++writeTimestamp;
                rewrittenBlock[0] = (byte) writeTimestamp;

                for (int i = 0; i < toWriteHashes.length; ++i)
                    System.arraycopy(toWriteHashes[i], 0, rewrittenBlock, 1 + i * BlockUtility.DIGEST_SIZE,
                            BlockUtility.DIGEST_SIZE);
            }

            // sign public key block
            signAlgorithm.initSign(privateKey);
            signAlgorithm.update(rewrittenBlock, 0, rewrittenBlock.length);
            final byte[] keyBlockSignature = signAlgorithm.sign();

            final Semaphore putkSemaphore = new Semaphore(
                    -((int) Math.ceil((BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0) - 1));
            final byte[] rewrittenBlockCopy = rewrittenBlock;

            for (final BlockServer replica : blockServers) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            replica.put_k(rewrittenBlockCopy, keyBlockSignature, publicKey.getEncoded());
                        } catch (Exception e) {
                            ;
                        } finally {
                            putkSemaphore.release();
                        }
                    }
                }).start();
            }
            putkSemaphore.acquire();

            // since the blocks are immutable and self-verifying, we only need to ensure a simple quorum
            final Semaphore puthSemaphore = new Semaphore(
                    -((int) Math.ceil((BlockUtility.NUM_REPLICAS) / 2.0) - 1));

            for (final BlockServer replica : blockServers) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            // write data blocks
                            for (int i = 0; i < toWriteBlocks.length; ++i) {
                                replica.put_h(toWriteBlocks[i]);
                            }
                        } catch (Exception e) {
                            ;
                        } finally {
                            puthSemaphore.release();
                        }
                    }
                }).start();
            }
            puthSemaphore.acquire();

        } catch (WrongArgumentsException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Library - Couldn't write to server: " + e.getMessage());
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    public int FS_read(byte[] publicKey, int position, int size, byte[] buffer)
            throws OperationFailedException, DataIntegrityFailureException {
        if (position < 0 || size < 0 || buffer == null)
            throw new OperationFailedException("Invalid arguments");

        try {
            byte[] publicKeyHash = BlockUtility.digest(publicKey);

            int numReadBlocks = 0;
            byte[] readDataHashes = null;
            Integer chosenTimestamp = 0;

            for (BlockServer replica : blockServers) {
                // we already achieved a quorum
                if (numReadBlocks > (BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0) {
                    break;
                }

                byte[] publicBlock;
                try {
                    ++numReadBlocks;
                    publicBlock = replica.get(BlockUtility.getKeyString(publicKeyHash));
                } catch (Exception e) {
                    continue;
                }

                // obtain signature
                byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
                System.arraycopy(publicBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);

                // obtain data
                int dataLength = publicBlock.length - BlockUtility.SIGNATURE_SIZE;
                byte[] data = new byte[dataLength];
                System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, data, 0, dataLength);

                // verify public key block integrity
                if (!BlockUtility.verifyDataIntegrity(data, storedSignature, publicKey))
                    continue;

                int timestamp = data[0];

                byte[] dataHashes = new byte[data.length - 1];
                System.arraycopy(data, 1, dataHashes, 0, data.length - 1);

                if (timestamp > chosenTimestamp) {
                    readDataHashes = dataHashes;
                    chosenTimestamp = timestamp;
                }
            }

            // there is no public block or the ones returned aren't enough to ensure byzantine fault tolerance
            if (readDataHashes == null)
                throw new DataIntegrityFailureException("Public key block invalid or non-existent");

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

            byte[][] blockHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                System.arraycopy(readDataHashes, i * BlockUtility.DIGEST_SIZE, blockHashes[num], 0, BlockUtility.DIGEST_SIZE);
                num++;
            }

            num = 0;
            int readLength = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                String dataBlockName = BlockUtility.getKeyString(blockHashes[num]);

                int acks = 0;
                byte[] readData = null;
                for (int id = 0; id < BlockUtility.NUM_REPLICAS; ++id) {
                    // since the blocks are immutable and self-verifying, we only need to ensure a simple quorum
                    if (acks > (BlockUtility.NUM_REPLICAS) / 2.0) {
                        break;
                    }

                    ++acks;
                    byte[] data;

                    try {
                        data = blockServers.get(id).get(dataBlockName);
                    } catch (Exception e) {
                        continue;
                    }

                    if (!Arrays.equals(blockHashes[num], BlockUtility.digest(data))) {
                        continue;
                    } else {
                        readData = data;
                    }
                }

                if (readData == null) {
                    throw new DataIntegrityFailureException("Data integrity check failed on data block");
                }

                int dataLength = size - readLength > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - readLength;

                System.arraycopy(readData, 0, buffer, readLength, dataLength);
                readLength += dataLength;
                num++;
            }

            return buffer.length;
        } catch (DataIntegrityFailureException e) {
            throw e;
        } /*
           * catch (OperationFailedException e) { throw e; }
           */catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    private List<BlockServer> obtainQuorum() {
        List<BlockServer> replicas = new ArrayList<BlockServer>();
        final int quorumSize = (int) Math.ceil((BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0);
        int quorumReplicas = 0;

        Random random = new Random();

        while (quorumReplicas <= quorumSize) {
            int randomId = random.nextInt(BlockUtility.NUM_REPLICAS);
            BlockServer replica = blockServers.get(randomId);
            if (!replicas.contains(replica)) {
                replicas.add(replica);
                ++quorumReplicas;
            }
        }
        return replicas;
    }
}
