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
        String dir = null;
        char c;
        int occorrenze;
        Result result= null;
        String[] files =null;
        int esito;
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
            System.out.print("Inserire operazione(elimina/lista) (EOF per terminare): ");

            while ((input = stdIn.readLine()) != null) {
               
               if(input.equals("elimina")){
                System.out.println("inserire nome del file");
                nomeFile = stdIn.readLine();
                if(!nomeFile.endsWith(".txt")){
                    System.out.println("deve essere un file txt");
                  
                }else{
                    esito = serverRMI.elimina_occorrenze(nomeFile);
                    System.out.println("l'esito dell'operazione è " + esito);
                }
               }else if(input.equals("lista")){
                System.out.println("inserire carattere da contare");
                    c = (char)stdIn.read();
                    stdIn.readLine(); //eliminare uno scarto

                System.out.println("dopo "+c +" inserire nome del direttorio");
                    dir = stdIn.readLine();

                    try{
                        System.out.println("inserire soglia di occorrenza");
                        occorrenze = Integer.parseInt(stdIn.readLine());
                    }catch(NumberFormatException e){
                        System.out.println("inserire un numero di occorrenza valida");
                        continue;
                    }
                    if(dir== null || dir.trim().isEmpty() || c == ' '){
                        System.out.println("input invalido");
                        System.out.println("riprovare \n");
                       
                    }else{
                        result = serverRMI.lista_file_carattere(dir, c, occorrenze);
                        if(result.getErrorMessage()==null){
                            files = result.getFiles();

                            i = 0;
                            while(i<files.length && files[i]!=null){
                                System.out.println("il file trovato " + i + ": " + files[i]);
                                i++;
                            }
                        }else{
                            System.out.println(result.getErrorMessage());
                        }
                    }

                }else{
                    System.out.println("Errore, operazione non esistente");
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