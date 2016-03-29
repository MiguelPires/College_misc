package sec.blockfs.blockserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.OperationFailedException;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
    private FileSystemImpl fileSystem;
    private List<CertPath> certificates;
    private long previousNonce = 0;

    public ServerImpl() throws RemoteException, ServerErrorException {
        super();
        fileSystem = new FileSystemImpl();
        certificates = new ArrayList<CertPath>();
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
            System.out.println("Server exception:");
            e.printStackTrace();
        }
    }

    @Override
    public byte[] get(String id) throws WrongArgumentsException, ServerErrorException, FileNotFoundException {
        if (id == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }
        try {
            return fileSystem.read(id);
        } catch (FileSystemException e) {
            // e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public String put_k(byte[] data, byte[] signature, byte[] publicKeyBytes, long nonce)
            throws ServerErrorException, DataIntegrityFailureException {
        if (data == null || signature == null || publicKeyBytes == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }

        try {
            // verify data integrity
            if (!BlockUtility.verifyDataIntegrity(data, signature, publicKeyBytes)) {
                throw new DataIntegrityFailureException("Integrity failure - data and signature don't match");
            } else if (nonce != previousNonce + 1) {
                throw new DataIntegrityFailureException("Integrity failure - wrong nonce");
            }
            // update nonce
            ++previousNonce;

            // write public key block
            return fileSystem.writePublicKey(data, signature, publicKeyBytes);
        } catch (DataIntegrityFailureException e) {
            throw e;
        } catch (Exception e) {
            // e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public String put_h(byte[] data) throws ServerErrorException {
        if (data == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }

        try {
            return fileSystem.writeData(data);
        } catch (Exception e) {
            // e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public void storePubKey(CertPath certPath) throws DataIntegrityFailureException, ServerErrorException {
        if (certPath == null || certPath.getCertificates().isEmpty()) {
            throw new ServerErrorException("Invalid certificates");
        }

        try {
            BlockUtility.validateCertPath(certPath);
            if (!certificates.contains(certPath)) {
                certificates.add(certPath);
            }

        } catch (Exception e) {
            // e.printStackTrace();
            throw new DataIntegrityFailureException("The certificate isn't valid - " + e.getMessage());
        }

        /*
         * try {
         * 
         * // obtain root cert X509Certificate lastCert = certificates.get(certificates.size() - 1); X500Principal rootEntity =
         * lastCert.getIssuerX500Principal(); X509Certificate previousCert = findRootCertificate(rootEntity);
         * 
         * if (previousCert == null) throw new DataIntegrityFailureException(
         * "The can't validate certificate. Root CA's certificate unknown");
         * 
         * // check intermediate certs previousCert.checkValidity(); for (int i = certificates.size() - 1; i >= 0; --i) {
         * X509Certificate cert = certificates.get(i); cert.checkValidity(); cert.verify(previousCert.getPublicKey()); previousCert
         * = cert; }
         * 
         * if (!certificates.contains(userCertificate)) { CertificateFactory fact = CertificateFactory.getInstance("X.509");
         * intermediateCertificates.add(0, element); CertPath path = fact.generateCertPath(intermediateCertificates);
         * certificates.add(userCertificate); } } catch (Exception e) { // e.printStackTrace(); throw new
         * DataIntegrityFailureException("The certificate isn't valid - " + e.getMessage()); }
         */
    }

    @Override
    public List<CertPath> readPubkeys() {
        return certificates;
    }

}
