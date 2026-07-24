/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements RemOp1 {
    public static final int N = 256;
    private Veicolo[] registro = null;
    private int dimensioneLogica;
    public ServerImpl() throws RemoteException { 
        super(); 
        registro = new Veicolo[N];
        // ED000AA corrisponde circa al 2011. > ED significa dopo il 2011.
        registro[0] = new Veicolo("AA123BB", "11111", "auto", "AA123BB_img"); // Vecchia
        registro[1] = new Veicolo("FF000ZZ", "22222", "auto", "FF000ZZ_img"); // Nuova (> ED)
        registro[2] = new Veicolo("GG123HH", "33333", "camper", "GG123HH_img"); // Nuova (> ED)
        registro[3] = new Veicolo("CC000DD", "44444", "auto", "CC000DD_img"); // Vecchia
        registro[4] = new Veicolo("EZ999ZZ", "55555", "camper", "EZ999ZZ_img"); // Nuova (> ED)
         dimensioneLogica = 5;
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
            ServerImpl serverRMI = new ServerImpl();
            Naming.rebind(completeName, serverRMI);
            System.out.println("Server RMI: Servizio \"" + serviceName + "\" registrato");
        } catch (Exception e) {
            System.err.println("Server RMI \"" + serviceName + "\": " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Specifica: Lista prenotazioni di un 'tipo', immatricolati dopo 2011 (> 'ED')
    public Risposta visualizza_prenotazioni(String tipo) throws RemoteException {
        // VARIABILI IN TESTA
        Risposta result = new Risposta();
        int i = 0;
        int count = 0;
        String targaCorr = null;
        boolean isRecent = false;

        // ALGORITMO DI RICERCA E FILTRO
        while (i < dimensioneLogica) {
            if (registro[i] != null) {
                targaCorr = registro[i].getTarga();
                
                // Controllo targa > "ED" (prime due lettere)
                isRecent = false;
                    if (targaCorr.substring(0, 2).compareTo("ED") > 0) {
                        isRecent = true;
                    }
                

                // Se il tipo corrisponde E la targa è recente
                if (tipo.equals(registro[i].getTipo()) && isRecent) {
                    result.getFiles()[count] = registro[i];
                    count++;
                }
            }
            i++; // IMPORTANTE: incrementare sempre fuori dagli if
        }
        
        if (count == 0) {
            result.setErrorMessage("Nessun veicolo trovato per i criteri specificati.");
        }
        
        return result;
    }

    public synchronized int elimina_prenotazione(String targaDaEliminare) throws RemoteException {
        // VARIABILI IN TESTA
        int result = -1; // -1 errore/non trovato, 0 o 1 successo
        int i = 0;
        int j = 0;
        boolean trovato = false;

        if (dimensioneLogica == 0 || targaDaEliminare == null) {
            return -1;
        }

        // ALGORITMO DI RICERCA
        while (i < dimensioneLogica && !trovato) {
            if (registro[i] != null && registro[i].getTarga().equals(targaDaEliminare)) {
                trovato = true;
                // Non incrementiamo i, ci serve l'indice per lo shift
            } else {
                i++;
            }
        }

        // ALGORITMO DI RIMOZIONE (SHIFT)
        if (trovato) {
            j = i;
            while (j < dimensioneLogica - 1) {
                registro[j] = registro[j + 1];
                j++;
            }
            // Pulizia ultimo elemento e decremento dimensione
            registro[dimensioneLogica - 1] = null;
            dimensioneLogica--;
            
            result = 1; // Successo
            System.out.println("Server: Veicolo con targa " + targaDaEliminare + " rimosso.");
        } else {
            System.out.println("Server: Veicolo con targa " + targaDaEliminare + " non trovato.");
        }

        return result;
    }
}
