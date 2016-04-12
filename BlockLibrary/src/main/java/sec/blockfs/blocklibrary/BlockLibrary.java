package sec.blockfs.blocklibrary;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

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
    public boolean ENABLE_CACHE = true;

    private Signature signAlgorithm;
    private int writeTimestamp = 0;
    private List<BlockServer> blockServers = new ArrayList<BlockServer>();
    private byte[] publicBlockCache = null;

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

    public void FS_write(int position, int size, byte[] contents)
            throws OperationFailedException, WrongArgumentsException, DataIntegrityFailureException {
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

            byte[] rewrittenBlock = null;
            try {

                final String publicKeyString = BlockUtility.getKeyString(BlockUtility.digest(publicKey.getEncoded()));
                byte[] blockHashes = readPublicKeyBlockHashes(publicKeyString);

                // rewrite
                int publicBlockSize = blockHashes.length / BlockUtility.DIGEST_SIZE;
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
                        System.arraycopy(blockHashes, 0, rewrittenBlock, 1 + i * BlockUtility.DIGEST_SIZE,
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

            writePublicKeyBlock(rewrittenBlock);
            writeContentBlocks(toWriteBlocks);

        } catch (WrongArgumentsException e) {
            throw e;
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Library - Couldn't write to server: " + e.getMessage());
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    public int FS_read(final byte[] publicKey, int position, int size, byte[] buffer)
            throws OperationFailedException, DataIntegrityFailureException {
        if (position < 0 || size < 0 || buffer == null)
            throw new OperationFailedException("Invalid arguments");

        try {
            final String publicKeyHash = BlockUtility.getKeyString(BlockUtility.digest(publicKey));
            byte[] dataHashes = readPublicKeyBlockHashes(publicKeyHash);

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

            final byte[][] blockHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int blockCount = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                System.arraycopy(dataHashes, i * BlockUtility.DIGEST_SIZE, blockHashes[blockCount], 0, BlockUtility.DIGEST_SIZE);
                blockCount++;
            }

            buffer = readContentBlocks(startBlock, endBlock, size, blockHashes, buffer);
            return buffer.length;
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    private byte[] readPublicKeyBlockHashes(final String keyBlockName)
            throws FileNotFoundException, InterruptedException, DataIntegrityFailureException {
        final Semaphore readSemaphore = new Semaphore(
                -((int) Math.ceil((BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0) - 1));
        final AtomicInteger faultyServers = new AtomicInteger(0);
        final ConcurrentHashMap<Integer, byte[]> readBlocks = new ConcurrentHashMap<>();

        for (final BlockServer replica : blockServers) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        byte[] publicBlock = replica.get(keyBlockName);
                        // obtain signature
                        byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
                        System.arraycopy(publicBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);

                        // obtain data
                        int dataLength = publicBlock.length - BlockUtility.SIGNATURE_SIZE;
                        byte[] data = new byte[dataLength];
                        System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, data, 0, dataLength);

                        // verify public key block integrity
                        if (!BlockUtility.verifyDataIntegrity(data, storedSignature, publicKey)) {
                            faultyServers.incrementAndGet();
                            return;
                        }

                        int timestamp = (byte) data[0];
                        byte[] hashes = new byte[data.length - 1];
                        System.arraycopy(data, 1, hashes, 0, hashes.length);
                        readBlocks.put(timestamp, hashes);
                    } catch (FileNotFoundException e) {
                        ;
                    } catch (RemoteException | WrongArgumentsException | ServerErrorException e) {
                        faultyServers.incrementAndGet();
                    } finally {
                        readSemaphore.release();
                    }
                }
            }).start();
        }

        // wait for the (N+f)/2 fastest responses
        readSemaphore.acquire();

        if (faultyServers.get() == 1)
            System.out.println("Invalid public key block from one server");
        else if (faultyServers.get() > 1) {
            System.out.println("Invalid public key block from " + faultyServers.get() + " servers");
        }

        if (faultyServers.get() > BlockUtility.NUM_FAULTS)
            throw new DataIntegrityFailureException("Couldn't obtain a valid quorum.");

        byte[] chosenBlockHashes = null;
        Integer chosenTimestamp = 0;
        for (Integer readTimestamp : readBlocks.keySet()) {
            if (readTimestamp > chosenTimestamp) {
                chosenTimestamp = readTimestamp;
                chosenBlockHashes = readBlocks.get(readTimestamp);
            }
        }

        // there is no public block or the ones returned aren't enough to ensure byzantine fault tolerance
        if (chosenBlockHashes == null)
            throw new FileNotFoundException();
        else
            return chosenBlockHashes;
    }

    private byte[] readContentBlocks(int firstBlockIndex, int lastBlockIndex, int size, final byte[][] blockHashes, byte[] buffer)
            throws InterruptedException, DataIntegrityFailureException {
        final AtomicInteger faultyServers = new AtomicInteger(0);
        final AtomicInteger num = new AtomicInteger(0);
        int readLength = 0;

        for (int i = firstBlockIndex; i <= lastBlockIndex; ++i) {
            final String dataBlockName = BlockUtility.getKeyString(blockHashes[num.get()]);
            final Semaphore dataBlockSemaphore = new Semaphore(-((int) Math.ceil((BlockUtility.NUM_REPLICAS) / 2.0) - 1));
            final ConcurrentLinkedQueue<byte[]> dataBlock = new ConcurrentLinkedQueue<>();

            for (final BlockServer replica : blockServers) {
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            byte[] data = replica.get(dataBlockName);

                            if (!Arrays.equals(blockHashes[num.get()], BlockUtility.digest(data))) {
                                faultyServers.incrementAndGet();
                                return;
                            } else {
                                dataBlock.add(data);
                            }
                        } catch (Exception e) {
                            faultyServers.incrementAndGet();
                        } finally {
                            dataBlockSemaphore.release();
                        }
                    }
                }).start();
            }

            dataBlockSemaphore.acquire();

            if (faultyServers.get() == 1)
                System.out.println("Invalid data block from one server");
            else if (faultyServers.get() > 1)
                System.out.println("Invalid data block from " + faultyServers.get() + " servers");

            // the number of correct processes must be a majority
            if (BlockUtility.NUM_REPLICAS - faultyServers.get() <= BlockUtility.NUM_REPLICAS / 2.0)
                throw new DataIntegrityFailureException("Couldn't obtain a valid quorum.");

            int dataLength = size - readLength > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - readLength;
            System.arraycopy(dataBlock.remove(), 0, buffer, readLength, dataLength);
            readLength += dataLength;
            num.getAndIncrement();
        }
        return buffer;
    }

    private void writePublicKeyBlock(byte[] toWriteBlock) throws SignatureException, InvalidKeyException, InterruptedException {
        // sign public key block
        signAlgorithm.initSign(privateKey);
        signAlgorithm.update(toWriteBlock, 0, toWriteBlock.length);
        final byte[] keyBlockSignature = signAlgorithm.sign();

        final Semaphore putkSemaphore = new Semaphore(
                -((int) Math.ceil((BlockUtility.NUM_REPLICAS + BlockUtility.NUM_FAULTS) / 2.0) - 1));
        final byte[] rewrittenBlockCopy = toWriteBlock;

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
    }

    private void writeContentBlocks(final byte[][] toWriteBlocks) throws InterruptedException {
        // since the blocks are immutable and self-verifying, we only need to ensure a simple quorum
        final Semaphore puthSemaphore = new Semaphore(-((int) Math.ceil((BlockUtility.NUM_REPLICAS) / 2.0) - 1));

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
    }
}
