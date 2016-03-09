package sec.blockfs.blockserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

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
    public byte[] get(String id) throws WrongArgumentsException, ServerErrorException {
        // TODO: add checks
        try {
            return fileSystem.read(id);
         } catch (Exception e) {
             e.printStackTrace();
            throw new ServerErrorException(e.getMessage());
        }
    }

    @Override
    public String put_k(byte[] data, byte[] signature, byte[] publicKeyBytes)
            throws ServerErrorException, DataIntegrityFailureException {
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
            throw new ServerErrorException(e.getMessage());
        }
    }
}
