package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
  private FileSystemImpl fileSystem;
  private ArrayList<String> clients;
  private MessageDigest digestAlgorithm;

  public ServerImpl() throws RemoteException, ServerErrorException {
    super();

    fileSystem = new FileSystemImpl();
    clients = new ArrayList<String>();
    try {
      digestAlgorithm = MessageDigest.getInstance("SHA-512");
    } catch (NoSuchAlgorithmException e) {
      throw new ServerErrorException(e.getMessage());
    }

    fileSystem.FS_init();
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

  public byte[] get(int id) {
    // TODO Auto-generated method stub
    return null;
  }

  public byte[] put_k(byte[] data, byte[] signature, byte[] publicKeyBytes)
      throws ServerErrorException, DataIntegrityFailureException {
    // verify data integrity
    if (!verifyDataIntegrity(data, signature, publicKeyBytes)) {
      throw new DataIntegrityFailureException("Data integrity check failed");
    }

    try {

      // write public key block
      byte[] dataDigest = clearAndCompute(data);
      byte[] keyDigest = writePublicKeyBlock(publicKeyBytes, dataDigest);

      // write data block
      fileSystem.FS_write(0, data.length, data);

      return keyDigest;
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServerErrorException();
    }
  }

  public byte[] put_h(byte[] data) throws ServerErrorException {
    try {
      fileSystem.FS_write(0, data.length, data);
      return clearAndCompute(data);
    } catch (Exception e) {
      throw new ServerErrorException(e.getMessage());
    }
  }

  /*
   * (non Java-doc)
   * 
   * Auxiliary methods
   */

  private byte[] clearAndCompute(byte[] data) {
    digestAlgorithm.reset();
    digestAlgorithm.update(data);
    return digestAlgorithm.digest();
  }

  private byte[] writePublicKeyBlock(byte[] publicKey, byte[] dataDigest) throws NoSuchAlgorithmException, IOException {
    byte[] keyDigest = clearAndCompute(publicKey);
    String fileName = Base64.getEncoder().encode(keyDigest).toString();

    System.out.println("Writing public key block: " + FileSystemImpl.BASE_PATH + File.separatorChar + fileName);
    FileOutputStream stream = new FileOutputStream(FileSystemImpl.BASE_PATH + File.separatorChar + fileName);
    stream.write(dataDigest, 0, dataDigest.length);
    stream.close();

    return keyDigest;
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
