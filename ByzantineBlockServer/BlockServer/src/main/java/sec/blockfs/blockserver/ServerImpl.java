package sec.blockfs.blockserver;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import sec.blockfs.blockutility.BlockLibrary;
import sec.blockfs.blockutility.BlockServer;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;
import sec.blockfs.blockutility.ServerErrorException;
import sec.blockfs.blockutility.WrongArgumentsException;

@SuppressWarnings("serial")
public class ServerImpl extends UnicastRemoteObject implements BlockServer {
    private FileSystemImpl fileSystem;
    private Long nonce = new Long(0);

    public ServerImpl(String serverId) throws RemoteException, ServerErrorException {
        super();
        fileSystem = new FileSystemImpl(serverId);
    }

    public ServerImpl() throws RemoteException, ServerErrorException {
        super();
        fileSystem = new FileSystemImpl("0");
    }

    public static void main(String[] args) {
        try {
            String servicePort = args[0];
            String serviceName = args[1];
            String serverId = args[2];

            Registry registry = LocateRegistry.createRegistry(new Integer(servicePort) + new Integer(serverId));
            registry.rebind(serviceName + serverId, new ServerImpl(serverId));
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
    public String put_k(byte[] data, byte[] signature, byte[] publicKeyBytes, String serviceUrl, String serviceName,
            int servicePort) throws ServerErrorException, DataIntegrityFailureException {
        if (data == null || signature == null || publicKeyBytes == null) {
            throw new ServerErrorException("Invalid (null) argument");
        }

        try {
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
}
