package sec.blockfs.blocklibrary;

import java.rmi.Naming;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import sec.blockfs.blockserver.BlockServer;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;;

public class BlockLibrary {
    private static BlockServer blockServer;
    private PrivateKey privateKey;
    public PublicKey publicKey;
    private Signature signAlgorithm;

    public String FS_init(String serviceName, String servicePort, String serviceUrl) throws OperationFailedException {

        try {
            System.out.println("Connecting to server: " + serviceUrl + ":" + servicePort + "/" + serviceName);
            blockServer = (BlockServer) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
            System.out.println("Connected to block server");

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
            throw new OperationFailedException("Couldn't connect to server");
        }

    }

    public void FS_write(int position, int size, byte[] contents) throws OperationFailedException {
        try {
            if (position < 0 || size < 0 || contents == null)
                throw new OperationFailedException("Invalid arguments");
            // TODO: add padding

            int startBlock = position / BlockUtility.BLOCK_SIZE;
            int endBlock = (position + size) / BlockUtility.BLOCK_SIZE;

            byte[][] toWriteBlocks = new byte[endBlock - startBlock + 1][BlockUtility.BLOCK_SIZE];
            byte[][] toWriteHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int writtenBytes = 0, num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                int bytesToWrite = contents.length-writtenBytes > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : contents.length-writtenBytes;
                System.arraycopy(contents, writtenBytes, toWriteBlocks[num], 0, bytesToWrite);
                System.arraycopy(BlockUtility.digest(toWriteBlocks[num]), 0, toWriteHashes[num], 0, BlockUtility.DIGEST_SIZE);
                writtenBytes += bytesToWrite;
                ++num;
            }

            // rewrite public key block
            byte[] publicBlock = blockServer.get(BlockUtility.getKeyString(publicKey.getEncoded()));

            byte[] rewrittenBlock = null;

            if (publicBlock != null) {
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
                rewrittenBlock = new byte[toWriteHashes.length * BlockUtility.DIGEST_SIZE];

                // there is no written public key block
                for (int i = 0; i < toWriteHashes.length; ++i)
                    System.arraycopy(toWriteHashes[i], 0, rewrittenBlock, i * BlockUtility.DIGEST_SIZE, BlockUtility.DIGEST_SIZE);
            }

            // sign public key block
            signAlgorithm.initSign(privateKey);
            signAlgorithm.update(rewrittenBlock, 0, rewrittenBlock.length);
            byte[] keyBlockSignature = signAlgorithm.sign();

            blockServer.put_k(rewrittenBlock, keyBlockSignature, publicKey.getEncoded());

            for (int i = 0; i < toWriteBlocks.length; ++i) {
                // sign data block
                signAlgorithm.initSign(privateKey);
                signAlgorithm.update(rewrittenBlock);
                byte[] dataBlockSignature = signAlgorithm.sign();

                byte[] dataBlock = new byte[dataBlockSignature.length + toWriteBlocks[i].length];
                System.arraycopy(dataBlockSignature, 0, dataBlock, 0, dataBlockSignature.length);
                System.arraycopy(toWriteBlocks[i], 0, dataBlock, dataBlockSignature.length, toWriteBlocks[i].length);
                blockServer.put_h(dataBlock);
            }
        } catch (Exception e) {
            System.out.println("Library - Couldn't write to server: " + e.getMessage());
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    public int FS_read(byte[] publicKey, int position, int size, byte[] buffer) throws OperationFailedException {
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
            byte[] publicKeyData = new byte[publicKeyBlock.length - BlockUtility.SIGNATURE_SIZE];
            System.arraycopy(publicKeyBlock, BlockUtility.SIGNATURE_SIZE, publicKeyData, 0, publicKeyData.length);

            // verify public key block integrity
            if (!BlockUtility.verifyDataIntegrity(publicKeyData, publicKeySignature, publicKey))
                throw new DataIntegrityFailureException("Data integrity check failed");

            int startBlock = position / BlockUtility.BLOCK_SIZE;
            int endBlock = (position + size) / BlockUtility.BLOCK_SIZE;

            // TODO: add boundary checks and etc
            byte[][] blockHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                System.arraycopy(publicKeyData, i * BlockUtility.DIGEST_SIZE, blockHashes[num], 0, BlockUtility.DIGEST_SIZE);
                num++;
            }

            num = 0;
            int readLength = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                byte[] data = blockServer.get(BlockUtility.getKeyString(blockHashes[num]));

                if (data == null)
                    return -1;

                int dataLength = size - readLength > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE: size - readLength ;
                    
                /*System.out.println("DATA " + Arrays.toString(data));
                System.out.println("Block size: "+data.length+"; Read size: "+dataLength+"; Sig size: "+BlockUtility.SIGNATURE_SIZE);
                System.out.println("Buffer size: "+buffer.length+"; Read length: "+readLength);*/
               
                System.arraycopy(data, BlockUtility.SIGNATURE_SIZE, buffer, readLength, dataLength);
                readLength += dataLength;
                num++;
            }

            return buffer.length;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }
}
