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
        String nomeFile = null;
        String direttorio = null;
        Result result;
        int esito;
        DataInputStream in = null;
        String[] files =null;
        int i = 0;
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
            System.out.print("inserire elimina per eliminare e lista per vedere i sottodirettori(EOF per terminare): ");

            while ((input = stdIn.readLine()) != null) {
               if(input.equals("elimina")){
                 System.out.println("inserire il nome file");
                nomeFile = stdIn.readLine();
                esito = serverRMI.elimina_occorrenze(nomeFile);
                if(esito == -1){
                    System.out.println("operazione fallita");
                }else{
                    System.out.println("operazione successo compeltata con " + esito);
                }

               }else if(input.equals("lista")){
                 System.out.println("inserire nome direttorio");
                direttorio = stdIn.readLine();
                result = serverRMI.lista_sottodirettori(direttorio);
                if(result.getErrorMessage()==null){
                    files = result.getFiles();
                    i = 0;
                    while(i<files.length && files[i]!=null){
                        System.out.println("nome file " + i + " " + files[i]+"\n");
                        i++;
                    }
                }else{
                    System.out.println(result.getErrorMessage() + "\n");
                }
               }else{
                System.out.println("operazione non supportata");
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