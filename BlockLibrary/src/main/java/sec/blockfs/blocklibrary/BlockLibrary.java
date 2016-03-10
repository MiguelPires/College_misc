package sec.blockfs.blocklibrary;

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
import java.util.Arrays;

import sec.blockfs.blockserver.BlockServer;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;;

public class BlockLibrary {
    private static BlockServer blockServer;
    // these attributes are only public because of the tests
    public PrivateKey privateKey;
    public PublicKey publicKey;
    private Signature signAlgorithm;

    public BlockLibrary(String serviceName, String servicePort, String serviceUrl) throws InitializationFailureException {
        try {
            System.out.println("Connecting to server: " + serviceUrl + ":" + servicePort + "/" + serviceName);
            blockServer = (BlockServer) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
            System.out.println("Connected to block server");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new InitializationFailureException("Couldn't connect to server");

        }
    }

    public String FS_init() throws InitializationFailureException {

        try {
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

    public void FS_write(int position, int size, byte[] contents) throws OperationFailedException {
        try {
            if (position < 0 || size < 0 || contents == null)
                throw new OperationFailedException("Invalid arguments");

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

            byte[][] toWriteBlocks = new byte[endBlock - startBlock + 1][BlockUtility.BLOCK_SIZE];
            byte[][] toWriteHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int writtenBytes = 0, num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                int bytesToWrite = contents.length - writtenBytes > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE
                        : contents.length - writtenBytes;
                System.arraycopy(contents, writtenBytes, toWriteBlocks[num], 0, bytesToWrite);
                System.arraycopy(BlockUtility.digest(toWriteBlocks[num]), 0, toWriteHashes[num], 0, BlockUtility.DIGEST_SIZE);
                writtenBytes += bytesToWrite;
                ++num;
            }

            byte[] publicKeyHash = BlockUtility.digest(publicKey.getEncoded());
            byte[] publicBlock = blockServer.get(BlockUtility.getKeyString(publicKeyHash));
            byte[] rewrittenBlock = null;

            if (publicBlock != null) {

                byte[] storedSignature = new byte[BlockUtility.SIGNATURE_SIZE];
                System.arraycopy(publicBlock, 0, storedSignature, 0, BlockUtility.SIGNATURE_SIZE);

                int hashesLength = publicBlock.length - BlockUtility.SIGNATURE_SIZE;
                byte[] dataHashes = new byte[hashesLength];
                System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, dataHashes, 0, hashesLength);

                // verify public key block integrity
                if (!BlockUtility.verifyDataIntegrity(dataHashes, storedSignature, publicKey))
                    throw new DataIntegrityFailureException("Data integrity check failed on public key block");

                // rewrite
                int dataSize = publicBlock.length - BlockUtility.SIGNATURE_SIZE;
                byte[] dataPublicBlock = new byte[dataSize];
                System.arraycopy(publicBlock, BlockUtility.SIGNATURE_SIZE, dataPublicBlock, 0, dataSize);
                int publicBlockSize = dataPublicBlock.length / BlockUtility.DIGEST_SIZE;
                int newPublicBlockSize = publicBlockSize > endBlock + 1 ? publicBlockSize : endBlock + 1;

                rewrittenBlock = new byte[newPublicBlockSize * BlockUtility.DIGEST_SIZE];

                num = 0;
                for (int i = 0; i < newPublicBlockSize; ++i) {
                    if (i >= startBlock && i >= endBlock)
                        System.arraycopy(toWriteHashes[num], 0, rewrittenBlock, i * BlockUtility.DIGEST_SIZE,
                                BlockUtility.DIGEST_SIZE);
                    else
                        System.arraycopy(dataPublicBlock, 0, rewrittenBlock, i * BlockUtility.DIGEST_SIZE,
                                BlockUtility.DIGEST_SIZE);
                }
            } else {
                // write new public key block
                rewrittenBlock = new byte[toWriteHashes.length * BlockUtility.DIGEST_SIZE];
                for (int i = 0; i < toWriteHashes.length; ++i)
                    System.arraycopy(toWriteHashes[i], 0, rewrittenBlock, i * BlockUtility.DIGEST_SIZE, BlockUtility.DIGEST_SIZE);
            }

            // sign public key block
            signAlgorithm.initSign(privateKey);
            signAlgorithm.update(rewrittenBlock, 0, rewrittenBlock.length);
            byte[] keyBlockSignature = signAlgorithm.sign();

            // write public key block
            blockServer.put_k(rewrittenBlock, keyBlockSignature, publicKey.getEncoded());

            // write data blocks
            for (int i = 0; i < toWriteBlocks.length; ++i) {
                blockServer.put_h(toWriteBlocks[i]);
            }
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

        // TODO: use position and size to determine which block should be read
        try {
            byte[] publicKeyHash = BlockUtility.digest(publicKey);
            byte[] publicKeyBlock = blockServer.get(BlockUtility.getKeyString(publicKeyHash));

            // extract signature
            byte[] publicKeySignature = new byte[BlockUtility.SIGNATURE_SIZE];
            System.arraycopy(publicKeyBlock, 0, publicKeySignature, 0, BlockUtility.SIGNATURE_SIZE);

            // extract data
            byte[] dataHashes = new byte[publicKeyBlock.length - BlockUtility.SIGNATURE_SIZE];
            System.arraycopy(publicKeyBlock, BlockUtility.SIGNATURE_SIZE, dataHashes, 0, dataHashes.length);

            // verify public key block integrity
            if (!BlockUtility.verifyDataIntegrity(dataHashes, publicKeySignature, publicKey))
                throw new DataIntegrityFailureException("Data integrity check failed on public key block");

            int startBlock = position / (BlockUtility.BLOCK_SIZE+1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE+1);

            // TODO: add boundary checks and etc
            byte[][] blockHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                System.arraycopy(dataHashes, i * BlockUtility.DIGEST_SIZE, blockHashes[num], 0, BlockUtility.DIGEST_SIZE);
                num++;
            }

            num = 0;
            int readLength = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                String dataBlockName = BlockUtility.getKeyString(blockHashes[num]);
                byte[] data = blockServer.get(dataBlockName);

                if (data == null)
                    throw new OperationFailedException("Data block not found: "+dataBlockName);

                if (!Arrays.equals(blockHashes[num], BlockUtility.digest(data))) {
                    throw new DataIntegrityFailureException("Data integrity check failed on public key block");
                }

                int dataLength = size - readLength > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - readLength;

                /*
                 * System.out.println("DATA " + Arrays.toString(data)); System.out.println("Block size: "+data.length+
                 * "; Read size: "+dataLength+"; Sig size: "+BlockUtility.SIGNATURE_SIZE); System.out.println("Buffer size: "
                 * +buffer.length+"; Read length: "+readLength);
                 */

                System.arraycopy(data, 0, buffer, readLength, dataLength);
                readLength += dataLength;
                num++;
            }

            return buffer.length;
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }
}
