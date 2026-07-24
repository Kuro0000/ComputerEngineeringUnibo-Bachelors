/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.*;
import java.net.*;
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
            System.out.print("lista o numera (EOF per terminare): ");
            String dir = null;
            Result result = null;
            String[] files = null;
            int esito, i;
            while ((input = stdIn.readLine()) != null) {
                if(input.equals("lista")){
                    System.out.println("inserire il direttorio remoto");
                    dir = stdIn.readLine();
                    result = serverRMI.lista_file(dir);
                    if(result.getErrorMessage()==null){
                        i = 0;
                        files = result.getFiles();
                        while(i < files.length && files[i]!=null){
                            System.out.println(files[i] + "\n");
                            i++;
                        }
                    }else{
                        System.out.println(result.getErrorMessage());
                    }
                }else if(input.equals("numera")){
                    System.out.println("inserire il nome del file");
                    dir = stdIn.readLine();
                    result = serverRMI.lista_file(dir);
                    esito = serverRMI.numerazione_righe(dir);
                    System.out.println("esito " + esito);
                }else{
                    System.out.println("operazione non concessa");
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