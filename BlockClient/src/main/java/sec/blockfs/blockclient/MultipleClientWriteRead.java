/*package sec.blockfs.blockclient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import pteidlib.PteidException;
import pteidlib.pteid;
import sec.blockfs.blocklibrary.BlockLibrary;
import sec.blockfs.blocklibrary.InitializationFailureException;
import sec.blockfs.blockserver.DataIntegrityFailureException;
import sec.blockfs.blockserver.ServerErrorException;
import sec.blockfs.blockutility.BlockUtility;
import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class MultipleClientWriteRead {
    private static PKCS11 pkcs11;
    private static BlockLibrary writeClientLibrary = null;

    @SuppressWarnings({ "restriction", "unchecked" })
    public static void main(String[] args) {

        String servicePort = args[0];
        String serviceName = args[1];
        String serviceUrl = args[2];

        initWriteClient(serviceName, servicePort, serviceUrl);
        String text = BlockUtility.generateString(BlockUtility.BLOCK_SIZE);
        clientWriteBlock(text.getBytes());
        
        library.blockServer.put_k(data, signature, pub);
        List<X509Certificate> readCertificates = library.FS_list();

        assertTrue("There are no stored certificates", readCertificates != null);
        assertTrue("Expected one certificate. " + readCertificates.size() + " certs read instead", readCertificates.size() == 1);

        byte[] byteCert = pteid.GetCertificates()[0].certif;
        X509Certificate clientCert = BlockUtility.getCertFromByteArray(byteCert);
        X509Certificate storedCert = readCertificates.get(0);
    }

    @SuppressWarnings("restriction")
    private static void clientWriteBlock(byte[] data) throws PKCS11Exception, CertificateException, PteidException, RemoteException, DataIntegrityFailureException, ServerErrorException {
        // Open the PKCS11 session
        long sessionToken = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);

        // Token login
        pkcs11.C_Login(sessionToken, 1, null);

        CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
        attributes[0] = new CK_ATTRIBUTE();
        attributes[0].type = PKCS11Constants.CKA_CLASS;
        attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);

        pkcs11.C_FindObjectsInit(sessionToken, attributes);
        long[] keyHandles = pkcs11.C_FindObjects(sessionToken, 5);
        long writeClientPrivateKey = keyHandles[0];

        pkcs11.C_FindObjectsFinal(sessionToken);

        // initialize the signature method
        CK_MECHANISM mechanism = new CK_MECHANISM();
        mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
        mechanism.pParameter = null;

        // obtain the client certificate
        byte[] authCertBytes = BlockUtility.getCertificateInBytes(1);
        X509Certificate authCert = BlockUtility.getCertFromByteArray(authCertBytes);
        PublicKey writeClientPubKey = authCert.getPublicKey();

        // obtain the intermediate certificates
        ArrayList<X509Certificate> rootCertificates = new ArrayList<X509Certificate>();
        rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(3)));
        rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(4)));
        rootCertificates.add(BlockUtility.getCertFromByteArray(BlockUtility.getCertificateInBytes(7)));

        writeClientLibrary.blockServer.storePubKey(authCert, rootCertificates);

        writeClientLibrary.pkcs11.C_SignInit(sessionToken, mechanism, writeClientPrivateKey);
        byte[] keyBlockSignature = pkcs11.C_Sign(sessionToken, data);
    }

    private static void initWriteClient(String serviceName, String servicePort, String serviceUrl) throws PteidException,
            ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        try {
            writeClientLibrary = new BlockLibrary(serviceName, servicePort, serviceUrl);
        } catch (InitializationFailureException e) {
            System.out.println("Error - " + e.getMessage());
            return;
        }

        System.loadLibrary("pteidlibj");
        pteid.Init(""); // Initializes the eID library
        pteid.SetSODChecking(false);

        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        String libName = "libpteidpkcs11.so";

        if (-1 != osName.indexOf("Windows"))
            libName = "pteidpkcs11.dll";
        else if (-1 != osName.indexOf("Mac"))
            libName = "pteidpkcs11.dylib";

        Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
        if (javaVersion.startsWith("1.5.")) {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                    new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, null, false });
        } else {
            Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance",
                    new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
            pkcs11 = (PKCS11) getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
        }

    }
}
*/