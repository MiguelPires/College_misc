package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import sec.blockfs.blockutility.BlockUtility;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
    private FileSystemImpl fileSystem;

    public ServerImpl() throws RemoteException, ServerErrorException {
        super();
        fileSystem = new FileSystemImpl();
    }

    public static void main(String[] args) {
        try {
            String servicePort = args[0];
            String serviceName = args[1];

            Registry registry = LocateRegistry.createRegistry(new Integer(servicePort));
            registry.rebind(serviceName, new ServerImpl());
            System.out.println("Server initiated");
            System.in.read();
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc) Interface methods
     */

    @Override
    public byte[] get(byte[] publicKeyBytes) throws WrongArgumentsException, ServerErrorException, DataIntegrityFailureException {
        byte[] publicKeyBlock;
        try {
            publicKeyBlock = readPublicKeyBlock(publicKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WrongArgumentsException("Wrong public key bytes");
        }
        String dataBlockName = BlockUtility.getKeyString(publicKeyBlock);
        byte[] dataBlock;

        try {
            dataBlock = fileSystem.read(dataBlockName);
        } catch (FileSystemException e) {
            throw new ServerErrorException(e.getMessage());
        } catch (DataIntegrityFailureException e) {
            throw e;
        }
        return dataBlock;
    }

    @Override
    public byte[] put_k(byte[] data, byte[] signature, byte[] publicKeyBytes)
            throws ServerErrorException, DataIntegrityFailureException {
        // verify data integrity
        if (!verifyDataIntegrity(data, signature, publicKeyBytes)) {
            throw new DataIntegrityFailureException("Data integrity check failed");
        }

        try {
            // write public key block
            byte[] dataDigest = BlockUtility.digest(data);
            byte[] keyDigest = writePublicKeyBlock(publicKeyBytes, dataDigest);

            // write data block
            fileSystem.write(data);

            return keyDigest;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public byte[] put_h(byte[] data) throws ServerErrorException {
        try {
            fileSystem.write(data);
            return BlockUtility.digest(data);
        } catch (Exception e) {
            throw new ServerErrorException(e.getMessage());
        }
    }

    /*
     * (non Java-doc)
     * 
     * Auxiliary methods
     */

    // TODO: refactor this to the library
    private byte[] writePublicKeyBlock(byte[] publicKey, byte[] dataDigest) throws NoSuchAlgorithmException, IOException {
        byte[] keyDigest = BlockUtility.digest(publicKey);
        String fileName = BlockUtility.getKeyString(keyDigest);
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;

        System.out.println("Writing public key block: " + filePath);
        FileOutputStream stream = new FileOutputStream(filePath);
        stream.write(dataDigest, 0, dataDigest.length);
        stream.close();

        return keyDigest;
    }

    // TODO: refactor to the library
    private byte[] readPublicKeyBlock(byte[] publicKey) throws NoSuchAlgorithmException, IOException {
        byte[] keyDigest = BlockUtility.digest(publicKey);
        String fileName = BlockUtility.getKeyString(keyDigest);
        String filePath = FileSystemImpl.BASE_PATH + File.separatorChar + fileName;

        System.out.println("Reading public key block: " + filePath);
        FileInputStream stream = new FileInputStream(filePath);
        Path path = Paths.get(filePath);
        byte[] dataBlock = Files.readAllBytes(path);
        stream.close();

        return dataBlock;
    }

    private boolean verifyDataIntegrity(byte[] data, byte[] signature, byte[] publicKeyBytes) {
        try {
            PublicKey publicKey = regeneratePublicKey(publicKeyBytes);
            // initialize signing algorithm
            Signature rsa = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            rsa.initVerify(publicKey);
            rsa.update(data, 0, data.length);

            // verify data integrity
            return rsa.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey regeneratePublicKey(byte[] publicKeyBytes)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
        return keyFactory.generatePublic(pubKeySpec);
    }
}
