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
        String parola;
        String dir;

        int esito = 0;
        Risposta ris;
        int i = 0;
        String[] files;
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
            RemOp1 serverRMI = (RemOp1) Naming.lookup(completeName);
            System.out.println("Client RMI: Servizio \"" + serviceName + "\" connesso");
            System.out.print("inserire operazione richiesta E(Elimina linee)/L(Lista di una directory): ");

            while ((input = stdIn.readLine()) != null) {
                if(input.equals("E")){
                    System.out.println("inserire nome del file:");
                    nomeFile = stdIn.readLine();
                    System.out.println("inserire parola da eliminare:");
                    parola = stdIn.readLine();
                   esito =  serverRMI.elimina_linee_contenenti_parola(nomeFile, parola);
                   System.out.println("esito " + esito);
                }else if(input.equals("L")){
                    System.out.println("inserire il direttorio da visualizzare:");
                    dir = stdIn.readLine();
                    
                    ris = serverRMI.lista_filetesto(dir);
                    if(ris.getErrorMessage()==null){
                        files = ris.getFiles();
                        i = 0;
                        while(i<files.length && files[i]!=null){
                            System.out.println(files[i]);
                            i++;
                        }
                    }else{
                        System.out.println(ris.getErrorMessage());
                    }
                }else{
                System.out.print("operazione non esistente");
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