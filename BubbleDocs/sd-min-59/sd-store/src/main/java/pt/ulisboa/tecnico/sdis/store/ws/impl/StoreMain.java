package pt.ulisboa.tecnico.sdis.store.ws.impl;

import javax.xml.ws.Endpoint;

import uddi.UDDINaming;


public class StoreMain {

    public static final String SERVER_KEY = "CYd/FbnCGtfTyr8uzJKeAw==";
    public static final String STORE_NAME = "SD-STORE";
    public static final boolean HANDLER_PRINT = false;
    private static final int REPLICAS_NUMBER = 3;
    public static boolean REPL_DEMO = true;

    public static void main(String[] args) {
        if (args.length == 0 || args.length == 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + StoreMain.class.getName() + " wsURL OR uddiURL wsName wsURL");
            return;
        }
        String uddiURL = null;
        String wsURL = null;

        String iWsURL = null;

        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 3) {
            uddiURL = args[0];
            wsURL = args[2];
        }

        Endpoint[] endpoint = new Endpoint[REPLICAS_NUMBER];

        for (int i = 0; i < REPLICAS_NUMBER; i++) {
            endpoint[i] = null;
        }

        UDDINaming uddiNaming = null;

        try {

            for (int i = 0; i < REPLICAS_NUMBER; i++) {
                String serverName = STORE_NAME + "-" + i;
                String[] split = wsURL.split("localhost:");
                String[] oldPort = split[1].split("/");

                Integer port = Integer.parseInt(oldPort[0]) + 1;
                String newPort = port.toString();
                iWsURL = split[0] + "localhost:" + newPort + "/" + oldPort[1] + "/" + oldPort[2] + "-" + i;

                StoreImpl impl = new StoreImpl(SERVER_KEY, serverName);

                endpoint[i] = Endpoint.create(impl);

                // publish endpoint
                System.out.printf("Starting %s%n", iWsURL);
                endpoint[i].publish(iWsURL);

                // publish to UDDI
                if (uddiURL != null) {
                    System.out.printf("Publishing '%s' to UDDI at %s%n", serverName, uddiURL);
                    uddiNaming = new UDDINaming(uddiURL);
                    uddiNaming.rebind(serverName, iWsURL);
                }
            }

            // wait
            System.out.println("Awaiting connections");
            System.out.println("\n ******* SD-STORE *******");
            System.in.read();

        } catch (Exception e) {
            System.out.printf("Caught exception: %s%n", e);
            e.printStackTrace();

        } finally {
            try {
                for (int i = 0; i < REPLICAS_NUMBER; i++) {
                    if (endpoint[i] != null) {
                        // stop endpoint
                        endpoint[i].stop();
                        System.out.printf("Stopped %s%n", iWsURL);
                    }
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
            try {
                for (int i = 0; i < REPLICAS_NUMBER; i++) {
                    if (uddiNaming != null) {
                        uddiNaming.unbind(iWsURL);
                        System.out.printf("Deleted '%s' from UDDI%n", iWsURL);
                    }
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when deleting: %s%n", e);
            }
        }

    }

}
