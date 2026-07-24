/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class RMI_Server extends UnicastRemoteObject implements RemOp {
    public static final int MAX_FILES = 256;
    public RMI_Server() throws RemoteException { 
        super(); 
    }
    public static void main(String[] args) {
        int registryPort = 1099;
        String registryHost = "localhost";
        String serviceName = "Server";
        if (args.length == 1) {
            try { 
                registryPort = Integer.parseInt(args[0]); 
            } catch (Exception e) { 
                System.exit(2); 
            }
        }
            // if (System.getSecurityManager() == null){
            //     System.setSecurityManager(new RMISecurityManager()); 
            // }
        String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
        System.out.println(completeName);
        try {
            RMI_Server serverRMI = new RMI_Server();
            Naming.rebind(completeName, serverRMI);
            System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
        } catch (Exception e) {
            System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    @Override
    public int metodo1(String directoryName) throws RemoteException {
        Result result = null; //dichiaro le variabili nel metodo per non avere sovrapposizione
      
        return result;
    }
    @Override
    public Result metodo2(String directoryName) throws RemoteException {
        Result result = null;

       
        return result;
    }
}
