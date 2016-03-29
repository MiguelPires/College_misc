package sec.blockfs.blockclient;

import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockutility.BlockUtility;
import sec.blockfs.blockutility.DataIntegrityFailureException;

public class ClientReplayAttack {
    public static void main(String[] args) throws Exception {
        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        BlockLibrary library = null;
        try {
            library = new BlockLibrary(serviceName, servicePort, serviceUrl);
            library.FS_init();
        } catch (InitializationFailureException e) {
            System.out.println("Error - " + e.getMessage());
            return;
        }

        String firstText = BlockUtility.generateString(BlockUtility.BLOCK_SIZE - 1);
        byte[] firstTextBytes = firstText.getBytes();

        // the first (legitimate) write
        library.FS_write(0, firstTextBytes.length, firstTextBytes);

        // the second (legitimate) write
        String secondText = BlockUtility.generateString(BlockUtility.BLOCK_SIZE - 1);
        byte[] secondTextBytes = secondText.getBytes();
        library.FS_write(0, secondTextBytes.length, secondTextBytes);

        // simulate an attacker copying the first request
        byte[] hashBlock = new byte[BlockUtility.SIGNATURE_SIZE];
        System.arraycopy(BlockUtility.digest(firstTextBytes), 0, hashBlock, 0, BlockUtility.SIGNATURE_SIZE);

        library.pkcs11.C_SignInit(library.sessionToken, library.mechanism, library.privateKey);
        byte[] keyBlockSignature = library.pkcs11.C_Sign(library.sessionToken, hashBlock);

        try {
            // maliciously rewrite the public key block to the first one
            library.blockServer.put_k(hashBlock, keyBlockSignature, library.publicKey.getEncoded(), 1);
        } catch (DataIntegrityFailureException e) {
            System.out.println("Couldn't read data. " + e.getMessage());

        }

    }
}
