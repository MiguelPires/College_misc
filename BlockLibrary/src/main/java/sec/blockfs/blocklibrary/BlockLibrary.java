package sec.blockfs.blocklibrary;

import java.rmi.Naming;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import sec.blockfs.blockserver.BlockServer;

public class BlockLibrary {
    private static BlockServer blockServer;

    public static void main(String[] args) {
        try {
            String servicePort = args[0];
            String serviceName = args[1];
            String serviceUrl = args[2];
            blockServer =
                (BlockServer) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
            System.out.println("Connected to block server");
            
            // generate public key            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "SunRsaSign");         
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(2048, random);

            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();
            
            String text = "Some random string"; 
            byte[] textBytes = text.getBytes();
            int byteLength = textBytes.length;

            // initialize signing algorithm            
            Signature rsa = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            rsa.initSign(priv);
            rsa.update(textBytes, 0, byteLength);
            
            // sign and send data
            byte[] signature = rsa.sign();  
            blockServer.put_k(text.getBytes(), signature, pub.getEncoded());
            System.in.read();
        } catch (Exception e) {
            System.err.println("Connection failed: ");
            e.printStackTrace();
        }
    }
}
