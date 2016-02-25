package sec.blockfs.blockserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
  private FileSystem fileSystem;
  private ArrayList<String> clients;

  public ServerImpl() throws RemoteException {
    super();

    fileSystem = new FileSystemImpl();
    clients = new ArrayList<String>();
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

  public byte[] get(int id) {
    // TODO Auto-generated method stub
    return null;
  }

  public byte[] put_k(byte[] data, byte[] signature, byte[] publicKeyBytes) throws ServerErrorException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA");

      // testar se o digest da chave existe e senao, chamar o FS_INIT
      digest.update(publicKeyBytes);
      byte[] keyDigest = digest.digest();

      String base64Key = new String(Base64.getEncoder().encode(keyDigest));
      if (!clients.contains(base64Key)) {
        fileSystem.FS_init(base64Key);
      }

      // regenerate public key
      X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKeyBytes);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
      PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

      // initialize signing algorithm
      Signature rsa = Signature.getInstance("SHA512withRSA", "SunRsaSign");
      rsa.initVerify(publicKey);
      rsa.update(data, 0, data.length);
      
      // verify data integrity
      if (rsa.verify(signature)) {
        fileSystem.FS_write(base64Key, 0, data.length, data);
        System.out.println("Data integrity check - Success");
      } else
      {
        System.out.println("Data integrity check - Failure");
        throw new DataIntegrityFailureException();
      }

      return keyDigest;

    } catch (Exception e) {
      e.printStackTrace();
      throw new ServerErrorException();
    }

  }

  public int put_h(byte[] data) {
    // TODO Auto-generated method stub
    return 0;
  }

}
