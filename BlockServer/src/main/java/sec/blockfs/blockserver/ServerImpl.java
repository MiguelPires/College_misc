package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
    private FileSystemImpl fileSystem;
    private List<X509Certificate> certificates;

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

    @Override
    public byte[] get(String id) throws WrongArgumentsException, ServerErrorException, FileNotFoundException {
        try {
            return fileSystem.read(id);
        } catch (FileSystemException e) {
            e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public String put_k(byte[] data, byte[] signature, byte[] publicKeyBytes)
            throws ServerErrorException, DataIntegrityFailureException {
        if (data == null || signature == null || publicKeyBytes == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }

        try {
            // verify data integrity
            if (!BlockUtility.verifyDataIntegrity(data, signature, publicKeyBytes)) {
                throw new DataIntegrityFailureException("Data integrity check failed");
            }

            // write public key block
            return fileSystem.writePublicKey(data, signature, publicKeyBytes);
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public String put_h(byte[] data) throws ServerErrorException {
        try {
            return fileSystem.writeData(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public void storePubKey(X509Certificate certificate, ArrayList<X509Certificate> intermediateCertificates)
            throws DataIntegrityFailureException {
        try {
            X509Certificate lastCert = intermediateCertificates.get(intermediateCertificates.size() - 1);
            X500Principal rootEntity = lastCert.getIssuerX500Principal();
            X509Certificate previousCert = findRootCertificate(rootEntity);
            previousCert.checkValidity();

            for (int i = intermediateCertificates.size() - 1; i >= 0; --i) {
                X509Certificate cert = intermediateCertificates.get(i);
                cert.checkValidity();
                cert.verify(previousCert.getPublicKey());
                previousCert = cert;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataIntegrityFailureException("The certificate isn't valid - " + e.getMessage());
        }
    }

    @Override
    public List<X509Certificate> readPubkeys() {
        return certificates;
    }

    private X509Certificate findRootCertificate(X500Principal root) throws OperationFailedException {
        try {
            String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            FileInputStream is = new FileInputStream(filename);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            String password = "changeit";
            keystore.load(is, password.toCharArray());

            // retrieve the most-trusted CAs from the keystore
            PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            for (TrustAnchor ta : params.getTrustAnchors()) {
                X509Certificate storedCert = ta.getTrustedCert();
                if (storedCert.getIssuerX500Principal().equals(root)) {
                    return ta.getTrustedCert();
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }
    }
}
