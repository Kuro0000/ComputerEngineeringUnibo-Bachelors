/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class Client {
    public static final int MAX_FILES = 256;

    public static void main(String[] args) {
        // VARIABILI IN TESTA
        int registryPort = 1099;
        String registryHost = null;
        String serviceName = "Server";
        BufferedReader stdIn = null;
        String input = null;
        stdIn = new BufferedReader(new InputStreamReader(System.in));

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
            String id = null;
            int giorno, mese, anno, durata;
            while ((input = stdIn.readLine()) != null) {
               if(input.equals("noleggia")){
                id = stdIn.readLine();
                try{
                    giorno = Integer.parseInt(stdIn.readLine());
                    mese =Integer.parseInt(stdIn.readLine());
                    anno =Integer.parseInt(stdIn.readLine());
                    durata =Integer.parseInt(stdIn.readLine());
                }catch(NumberFormatException nfe){
                    continue;
                }
                serverRMI.noleggia_sci(id, giorno, mese, anno, durata);

               }else if(input.equals("elimina")){
                id = stdIn.readLine();
               System.out.println("Esito dell'operazione " + serverRMI.elimina_sci(id));
               }else{
                System.out.println("operazione non ammessa");
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