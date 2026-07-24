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
        String targaInput = null;
        String tipoInput;

        int esito = 0;
        Risposta ris;
        int i = 0;
        Veicolo[] listaVeicoli;
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
System.out.print("Operazioni: E (Elimina per targa), L (Lista per tipo post-2011). \nInserire comando: ");

            while ((input = stdIn.readLine()) != null) {
                
                if (input.equals("E")) {
                    System.out.print("Inserire TARGA da eliminare: ");
                    targaInput = stdIn.readLine();
                    
                    esito = serverRMI.elimina_prenotazione(targaInput);
                    
                    if (esito == 1) {
                        System.out.println("Eliminazione avvenuta con successo.");
                    } else {
                        System.out.println("Errore: Targa non trovata o problema server.");
                    }

                } else if (input.equals("L")) {
                    System.out.print("Inserire TIPO (auto/camper): ");
                    tipoInput = stdIn.readLine();

                    ris = serverRMI.visualizza_prenotazioni(tipoInput);

                    if (ris.getErrorMessage() == null) {
                        listaVeicoli = ris.getFiles();
                        i = 0;
                        System.out.println("--- Veicoli trovati (Post-2011) ---");
                        // Scorriamo finché troviamo elementi non nulli
                        while (i < listaVeicoli.length && listaVeicoli[i] != null) {
                            System.out.println(listaVeicoli[i].toString());
                            i++;
                        }
                        if (i == 0) System.out.println("Nessun veicolo trovato.");
                    } else {
                        System.out.println("Errore dal server: " + ris.getErrorMessage());
                    }

                } else {
                    System.out.println("Comando non riconosciuto.");
                }
                
                System.out.print("\nInserire comando (E/L) o Ctrl+C per uscire: ");
            }
            
        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}