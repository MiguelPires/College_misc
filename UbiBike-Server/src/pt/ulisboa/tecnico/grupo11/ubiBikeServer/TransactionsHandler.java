package pt.ulisboa.tecnico.grupo11.ubiBikeServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class TransactionsHandler implements HttpHandler {

    private List<String> transactionsLog = new ArrayList<String>();
    Signature rsaSignature;

    public TransactionsHandler() throws NoSuchAlgorithmException {
        rsaSignature = Signature.getInstance("SHA512withRSA");
    }

    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().toString();
        System.out.println("URI: " + path);
        System.out.println("Method: " + exchange.getRequestMethod());

        switch (exchange.getRequestMethod()) {
        case "GET":
            parseGetRequest(exchange, path);
            break;
        case "PUT":
            try {
                parsePutRequest(exchange, path);
            } catch (InvalidKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SignatureException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            break;
        }

    }

    private void parseGetRequest(HttpExchange exchange, String path) throws IOException {

    }

    private void parsePutRequest(HttpExchange exchange, String path)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        if (path.contains("/transactions")) {
            // System.out.println("Entered transactions");
            InputStream inputStream = exchange.getRequestBody();
            byte[] buffer = new byte[Server.MAX_PUT_SIZE];
            int offset = 0;
            while (offset < Server.MAX_PUT_SIZE) {
                int bytesRead = inputStream.read(buffer, offset, Server.MAX_PUT_SIZE - offset);
                if (bytesRead == -1)
                    break;
                offset += bytesRead;
            }
            byte[] data = new byte[offset];
            System.arraycopy(buffer, 0, data, 0, offset);

            String transactionsString = new String(data, "UTF-8");
            exchange.sendResponseHeaders(200, 0);
            if (!transactionsString.equals("")) {
                System.out.println("Signed transaction: " + transactionsString);
                String[] splitToVerify = transactionsString.split(";;;");

                String[] getreceiver = splitToVerify[splitToVerify.length - 2].split("#");

                // get receiver's public key
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(UsersHandler.users.get(getreceiver[3]).getKey());
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

                rsaSignature.initVerify(publicKey);
                rsaSignature.update((transactionsString.replace(splitToVerify[splitToVerify.length - 1], "")).getBytes());

                if (!rsaSignature.verify(Base64.decode(splitToVerify[splitToVerify.length - 1]))) {
                    System.out.println("Failed to validate message!");
                    return;
                }
                String[] splittedTransactions = (transactionsString.replace(splitToVerify[splitToVerify.length - 1], ""))
                        .split(";;;");
                for (String transaction : splittedTransactions) {
                    System.out.println("Transaction: " + transaction);
                    String[] splittedTransaction = transaction.split("#");

                    if (!transactionsLog.contains(new String(splittedTransaction[6]))) {

                        transactionsLog.add(splittedTransaction[6]);
                        System.out.println("Log does not contain this hash: " + splittedTransaction[6]);

                        String sender = splittedTransaction[2];
                        String receiver = splittedTransaction[3];

                        User receiverUser = UsersHandler.users.get(receiver);
                        int receiverPoints = receiverUser.getPoints();

                        if (splittedTransaction[2].equals("_")) {
                            System.out.println(splittedTransaction[4] + " points won by biking.");
                            receiverUser.setPoints(receiverPoints + Integer.valueOf(splittedTransaction[4]));
                        } else {
                            User senderUser = UsersHandler.users.get(sender);
                            int senderPoints = senderUser.getPoints();

                            if ((senderPoints - Integer.valueOf(splittedTransaction[4])) >= 0) {

                                // update sender
                                System.out.println(sender + " (sender) points: " + senderPoints);
                                senderUser.setPoints(senderPoints - Integer.valueOf(splittedTransaction[4]));
                                System.out.println(sender + " (sender) points final: " + senderUser.getPoints());

                                // update receiver

                                System.out.println(receiver + " (receiver) points: " + receiverPoints);
                                receiverUser.setPoints(receiverPoints + Integer.valueOf(splittedTransaction[4]));
                                System.out.println(receiver + " (receiver) points final: " + receiverUser.getPoints());
                            }
                        }
                    } else {
                        System.out.println("Already contains hash");
                    }
                }
            }
        }
    }
}
