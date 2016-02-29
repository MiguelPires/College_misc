package sec.blockfs.blocklibrary;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import sec.blockfs.blockserver.BlockServer;
import sec.blockfs.blockutility.BlockUtility;;

public class BlockLibrary {
    private static BlockServer blockServer;
    private PrivateKey privateKey;
    PublicKey publicKey;
    private Signature signAlgorithm;

    public BlockLibrary(String serviceName, String servicePort, String serviceUrl)
            throws MalformedURLException,
            RemoteException,
            NotBoundException,
            NoSuchAlgorithmException,
            NoSuchProviderException {

        System.out.println(
                "Connecting to server: " + serviceUrl + ":" + servicePort + "/" + serviceName);
        blockServer =
            (BlockServer) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
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
    }

    public void write(byte[] contents) throws OperationFailedException {

        try {
            // initialize signing algorithm            
            signAlgorithm.initSign(privateKey);
            signAlgorithm.update(contents, 0, contents.length);

            // sign and send data
            byte[] signature = signAlgorithm.sign();
            blockServer.put_k(contents, signature, publicKey.getEncoded());

        } catch (Exception e) {
            System.out.println("Library - Couldn't write to server: " + e.getMessage());
            //e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }

    public void writeInBlocks(byte[] contents) {
        throw new UnsupportedOperationException();
    }

    public byte[] read() {
        throw new UnsupportedOperationException();
    }

}
