package pt.tecnico.SDStore.handler;

import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.*;

public class ClientHandler implements SOAPHandler<SOAPMessageContext> {

    public static final String REQUEST_PROPERTY = "request.pt.ulisboa.tecnico.sdis.store.ws.SDStore";
    public static final String RESPONSE_PROPERTY = "response.pt.ulisboa.tecnico.sdis.store.ws.SDStore";

    public static final String REQUEST_HEADER = "clientRequest";
    public static final String REQUEST_NS = "tag";

    public static final String RESPONSE_HEADER = "storeResponse";
    public static final String RESPONSE_NS = "tag";

    public static final String CLASS_NAME = ClientHandler.class.getSimpleName();

    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {
            // outbound message

            // *** #2 ***
            // get token from request context
            String propertyValue = (String) smc.get(REQUEST_PROPERTY);

            // put token in request SOAP header
            try {
                // get SOAP envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                // add header
                SOAPHeader sh = se.getHeader();
                if (sh == null)
                    sh = se.addHeader();

                // add header element (name, namespace prefix, namespace)
                Name name = se.createName(REQUEST_HEADER, "e", REQUEST_NS);
                SOAPHeaderElement element = sh.addHeaderElement(name);

                // *** #3 ***
                // add header element value
                String[] args = null;
                String newValue;
                if(propertyValue!=null){
                    args = propertyValue.split(";");
                    newValue = "Seq Number:"+args[0]+";"+"client:"+args[1];
                    }
                else
                    newValue = "Seq Number:"+args+";"+"client:"+args;
                
                element.addTextNode(newValue);

            } catch (SOAPException e) {
                System.out.printf("Failed to add SOAP header because of %s%n", e);
            }

        } else {
            // inbound message

            // get token from response SOAP header
            try {
                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }

                // get first header element
                Name name = se.createName(RESPONSE_HEADER, "e", RESPONSE_NS);
                Iterator it = sh.getChildElements(name);
                // check header element
                if (!it.hasNext()) {
                    System.out.printf("Header element %s not found.%n", RESPONSE_HEADER);
                    return true;
                }
                SOAPElement element = (SOAPElement) it.next();

                // *** #10 ***
                // get header element value
                String headerValue = element.getValue();

                // *** #11 ***
                // put token in response context
                String newValue = headerValue + "";
                smc.put(RESPONSE_PROPERTY, newValue);
                // set property scope to application so that client class can access property
                smc.setScope(RESPONSE_PROPERTY, Scope.APPLICATION);
            } catch (SOAPException e) {
                System.out.printf("Failed to get SOAP header because of %s%n", e);
            }

        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        return true;
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext messageContext) {
    }

}
