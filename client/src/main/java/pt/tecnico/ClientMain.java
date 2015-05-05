package pt.tecnico;

import javax.xml.transform.TransformerFactoryConfigurationError;


public class ClientMain {
   
    public static void main(String[] args) throws TransformerFactoryConfigurationError, Exception {
        
        new Client(args[0], args[1]);
    }
}
