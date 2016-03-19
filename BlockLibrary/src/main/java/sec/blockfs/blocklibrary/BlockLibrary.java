package sec.blockfs.blocklibrary;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import pteidlib.pteid;
import sec.blockfs.blockserver.BlockServer;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockserver.WrongArgumentsException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;;

@SuppressWarnings("restriction")
public class BlockLibrary {
    private static BlockServer blockServer;
    // the visibility of these attributes is package/public
    // because of the tests/clients
    public PublicKey publicKey;
    long privateKey;
    PKCS11 pkcs11; // used to access the PKCS11 API
    CK_MECHANISM mechanism; // access mechanism
    protected long sessionToken;

    public BlockLibrary(String serviceName, String servicePort, String serviceUrl) throws InitializationFailureException {
        try {
            System.out.println("Connecting to server: " + serviceUrl + ":" + servicePort + "/" + serviceName);
            blockServer = (BlockServer) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
            System.out.println("Connected to block server");
        } catch (NotBoundException | RemoteException | MalformedURLException e) {
            throw new InitializationFailureException("Couldn't connect to server");

        }
    }

    public void FS_init() throws InitializationFailureException {

        try {
            System.loadLibrary("pteidlibj");
            pteid.Init(""); // Initializes the eID Lib
            pteid.SetSODChecking(false);

            String osName = System.getProperty("os.name");
            String javaVersion = System.getProperty("java.version");
            String libName = "libpteidpkcs11.so";

            if (-1 != osName.indexOf("Windows"))
                libName = "pteidpkcs11.dll";
            else if (-1 != osName.indexOf("Mac"))
                libName = "pteidpkcs11.dylib";

            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
            if (javaVersion.startsWith("1.5.")) {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                        new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, null, false });
            } else {
                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                        new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
                pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
            }

            // Open the PKCS11 session
            sessionToken = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

            // Token login
            pkcs11.C_Login(sessionToken, 1, null);
            // CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(sessionToken);

            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
            attributes[0] = new CK_ATTRIBUTE();
            attributes[0].type = PKCS11Constants.CKA_CLASS;
            attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

            pkcs11.C_FindObjectsInit(sessionToken, attributes);
            long[] keyHandles = pkcs11.C_FindObjects(sessionToken, 5);
            privateKey = keyHandles[0]; 
            
            pkcs11.C_FindObjectsFinal(sessionToken);

            // initialize the signature method
            mechanism = new CK_MECHANISM();
            mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
            mechanism.pParameter = null;

            // store the public key in the server
            byte[] authCertBytes = BlockUtility.getCertificateInBytes(0);
            X509Certificate authCert = BlockUtility.getCertFromByteArray(authCertBytes);
            publicKey = authCert.getPublicKey();

            ArrayList<X509Certificate> rootCertificates = new ArrayList<X509Certificate>();
            
            rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(3)));
            rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(4)));
            rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(7)));
            blockServer.storePubKey(authCert, rootCertificates);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InitializationFailureException("Couldn't connect to server");
        }
    }

    public void FS_write(int position, int size, byte[] contents) throws OperationFailedException, WrongArgumentsException {
        try {
            if (position < 0 || size < 0 || contents == null || size > contents.length)
                throw new WrongArgumentsException("Invalid arguments");

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

            byte[][] toWriteBlocks = new byte[endBlock - startBlock + 1][BlockUtility.BLOCK_SIZE];
            byte[][] toWriteHashes = new byte[endBlock - startBlock + 1][BlockUtility.DIGEST_SIZE];

            int writtenBytes = 0, num = 0;
            for (int i = startBlock; i <= endBlock; ++i) {
                int bytesToWrite = size - writtenBytes > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - writtenBytes;
                System.arraycopy(contents, writtenBytes, toWriteBlocks[num], 0, bytesToWrite);
                System.arraycopy(BlockUtility.digest(toWriteBlocks[num]), 0, toWriteHashes[num], 0, BlockUtility.DIGEST_SIZE);
                writtenBytes += bytesToWrite;
                ++num;
            }

            byte[] publicKeyHash = BlockUtility.digest(publicKey.getEncoded());
            byte[] rewrittenBlock = null;

            try {
                byte[] publicBlock = blockServer.get(BlockUtility.getKeyString(publicKeyHash));

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
            } catch (FileNotFoundException e) {
                // write new public key block
                rewrittenBlock = new byte[toWriteHashes.length * BlockUtility.DIGEST_SIZE];
                for (int i = 0; i < toWriteHashes.length; ++i)
                    System.arraycopy(toWriteHashes[i], 0, rewrittenBlock, i * BlockUtility.DIGEST_SIZE, BlockUtility.DIGEST_SIZE);
            }

            pkcs11.C_SignInit(sessionToken, mechanism, privateKey);
            byte[] keyBlockSignature = pkcs11.C_Sign(sessionToken, rewrittenBlock);

            // write public key block
            blockServer.put_k(rewrittenBlock, keyBlockSignature, publicKey.getEncoded());

            // write data blocks
            for (int i = 0; i < toWriteBlocks.length; ++i) {
                blockServer.put_h(toWriteBlocks[i]);
            }
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
            String blockName = BlockUtility.getKeyString(publicKeyHash);

            byte[] publicKeyBlock;

            try {
                publicKeyBlock = blockServer.get(blockName);
            } catch (FileNotFoundException e) {
                throw new OperationFailedException("Data block not found: " + blockName);
            }

            // extract signature
            byte[] publicKeySignature = new byte[BlockUtility.SIGNATURE_SIZE];
            System.arraycopy(publicKeyBlock, 0, publicKeySignature, 0, BlockUtility.SIGNATURE_SIZE);

            // extract data
            byte[] dataHashes = new byte[publicKeyBlock.length - BlockUtility.SIGNATURE_SIZE];
            System.arraycopy(publicKeyBlock, BlockUtility.SIGNATURE_SIZE, dataHashes, 0, dataHashes.length);

            // verify public key block integrity
            if (!BlockUtility.verifyDataIntegrity(dataHashes, publicKeySignature, publicKey))
                throw new DataIntegrityFailureException("Data integrity check failed on public key block");

            int startBlock = position / (BlockUtility.BLOCK_SIZE + 1);
            int endBlock = (position + size) / (BlockUtility.BLOCK_SIZE + 1);

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

                byte[] data;
                try {
                    data = blockServer.get(dataBlockName);
                } catch (FileNotFoundException e) {
                    throw new OperationFailedException("Data block not found: " + dataBlockName);
                }

                if (!Arrays.equals(blockHashes[num], BlockUtility.digest(data))) {
                    throw new DataIntegrityFailureException("Data integrity check failed on data block");
                }

                int dataLength = size - readLength > BlockUtility.BLOCK_SIZE ? BlockUtility.BLOCK_SIZE : size - readLength;

                System.arraycopy(data, 0, buffer, readLength, dataLength);
                readLength += dataLength;
                num++;
            }

            return buffer.length;
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (OperationFailedException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

}
