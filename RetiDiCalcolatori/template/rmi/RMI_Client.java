/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class RMI_Client {
    public static final int MAX_FILES = 256;

    public static void main(String[] args) {
        // VARIABILI IN TESTA
        int registryPort = 1099;
        String registryHost = null;
        String serviceName = "Server";


        if (args.length != 1 && args.length != 2) 
            return;
            registryHost = args[0];
            try{
                if (args.length == 2) 
                    registryPort = Integer.parseInt(args[1]);
            }catch(NumberFormatException e){
                System.out.println("formato sbagliato");
                System.exit(1);
            }
            //  if (System.getSecurityManager() == null){
            //      System.setSecurityManager(new RMISecurityManager()); 
            //  }
        try {
            String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
            RemOp serverRMI = (RemOp) Naming.lookup(completeName);
            System.out.println("Client RMI: Servizio \"" + serviceName + "\" connesso");
            System.out.print("Nome directory da trasferire (EOF per terminare): ");



            //dichiaro le variabili fuori dal ciclo e li inizializzo con null
            BufferedReader stdIn = null;
            String input = null;
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            String nomeFile = null;
            int cont = 0;
            int bufferRead = 0;
            DataInputStream in = null;
            String[] files =null;
            int i = 0;
            while ((input = stdIn.readLine()) != null) {
               //modifice qua
                if(input.equals("op1")){





                }else if(input.equals("op2")){






                }else{
                    System.out.println("operazione non consentita");
                }

                System.out.print("\nInserire input(EOF per terminare): ");
            }
            System.out.println("fine operazione");
        } catch (Exception e) {
            System.err.println("ClientRMI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}