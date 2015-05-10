package pt.ulisboa.tecnico.sdis.store.ws.impl;

import javax.xml.ws.Endpoint;

import uddi.UDDINaming;


public class StoreMain {

    public static void main(String[] args) {
        // Check arguments
        if (args.length == 0 || args.length == 2) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + StoreMain.class.getName()
                    + " wsURL OR uddiURL wsName wsURL");
            return;
        }
        String uddiURL = null;
        String wsName = null;
        String wsURL = null;
        String key = null;
        if (args.length == 1) {
            wsURL = args[0];
        } else if (args.length >= 3) {
            uddiURL = args[0];
            wsName = args[1];
            wsURL = args[2];
            key = args[3];
        }
        String wsTestUrl = wsURL + "/test";

        Endpoint endpoint = null;
        Endpoint testEndpoint = null;

        UDDINaming uddiNaming = null;

        try {
            StoreImpl impl = new StoreImpl(key, wsName);

            if (System.getProperty("store-ws.test") != null) {
                System.out.println("Populating test data...");
            }
            endpoint = Endpoint.create(impl);

            // publish endpoint
            System.out.printf("Starting %s%n", wsURL);
            endpoint.publish(wsURL);

            // publish to UDDI
            if (uddiURL != null) {
                System.out.printf("Publishing '%s' to UDDI at %s%n", wsName,
                        uddiURL);
                uddiNaming = new UDDINaming(uddiURL);
                uddiNaming.rebind(wsName, wsURL);
            }

            if ("true".equalsIgnoreCase(System.getProperty("ws.test"))) {
                StoreImpl.reset();
                
                System.out.printf("Starting %s%n", wsTestUrl);
                testEndpoint = Endpoint.create(new TestControl());
                testEndpoint.publish(wsTestUrl);
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
                if (endpoint != null) {
                    // stop endpoint
                    endpoint.stop();
                    System.out.printf("Stopped %s%n", wsURL);
                }
                if (testEndpoint != null) {
                    // stop test endpoint
                    testEndpoint.stop();
                    System.out.printf("Stopped %s%n", wsTestUrl);
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
            try {
                if (uddiNaming != null) {
                    // delete from UDDI
                    uddiNaming.unbind(wsName);
                    System.out.printf("Deleted '%s' from UDDI%n", wsName);
                }
            } catch (Exception e) {
                System.out.printf("Caught exception when deleting: %s%n", e);
            }
        }

    }

}
