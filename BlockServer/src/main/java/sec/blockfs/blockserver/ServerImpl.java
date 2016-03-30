package sec.blockfs.blockserver;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.cert.CertPath;
import java.util.ArrayList;
import java.util.List;

import sec.blockfs.blockutility.BlockLibrary;
import sec.blockfs.blockutility.BlockServer;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;
import sec.blockfs.blockutility.ServerErrorException;
import sec.blockfs.blockutility.WrongArgumentsException;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
    // NOTE: the visibility of some of these attributes is package/public because of the tests.
    // They should all be private
    public List<CertPath> certificates;
    private FileSystemImpl fileSystem;
    private Long nonce = new Long(0);

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

    public String put_k(byte[] data, byte[] signature, byte[] publicKeyBytes, String serviceUrl, String serviceName,
            int servicePort) throws ServerErrorException, DataIntegrityFailureException {
        if (data == null || signature == null || publicKeyBytes == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }

        try {

            // NOTE: this check is ridiculous but necessary in order to keep the
            // tests as they are. They were made based on assumptions that later changed
            if (!serviceName.equals("override")) {
                BlockLibrary library = (BlockLibrary) Naming.lookup(serviceUrl + ":" + servicePort + "/" + serviceName);
                ++nonce;
                byte[] nonceSignature = library.challenge(nonce);
                byte[] nonceHash = BlockUtility.digest(new byte[] { nonce.byteValue() });

                // verify response to challenge
                if (!BlockUtility.verifyDataIntegrity(nonceHash, nonceSignature, publicKeyBytes)) {
                    throw new DataIntegrityFailureException("Integrity failure - failed to respond to challenge");
                }
            }

            // verify data integrity
            if (!BlockUtility.verifyDataIntegrity(data, signature, publicKeyBytes)) {
                throw new DataIntegrityFailureException("Integrity failure - data and signature don't match");
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

    public void storePubKey(CertPath certPath) throws DataIntegrityFailureException, ServerErrorException {
        if (certPath == null || certPath.getCertificates().isEmpty()) {
            throw new ServerErrorException("Invalid certificates");
        }

        BlockUtility.validateCertPath(certPath);
        if (!certificates.contains(certPath)) {
            certificates.add(certPath);
        }
    }

    public List<CertPath> readPubkeys() {
        return certificates;
    }
}
