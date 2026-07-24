/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
    public static final int N = 20;
    private Noleggio[] registro = null;
    private int dimensioneLogica;
    public ServerImpl() throws RemoteException { 
        super(); 
        registro = new Noleggio[N];
        registro[0] = new Noleggio("X12AB ", 12,12,2012, 14, "Volki Shark", 7, "VolkiShark1.jpg");
        registro[1] = new Noleggio("Y23CC ", 23,12,2024, 7, "Volki Shark", 14,"VolkiShark2.jpg");
        registro[2] = new Noleggio("Y255C ", -1,-1,-1, -1, "Volki Shark", 14,"VolkiShark3.jpg");
        registro[3] = new Noleggio("777CC ", 23,12,2023, 7, "Volki Shark", 14,"VolkiShark4.jpg");
        registro[4] = new Noleggio("999CC ", 0,0,0, -1, "Volki Shark", 14,"VolkiShark5.jpg");

        for(int i = 5;i<N;i++){
            registro[i] = new Noleggio();
        }
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

    public  int elimina_sci(String id) throws RemoteException {
        int result = -1; //dichiaro le variabili nel metodo per non avere sovrapposizione
        int i = 0;
        int j = 0;
        boolean trovato = false;
        File file = null;
        if (dimensioneLogica == 0 || id == null) {
            return -1;
        }

        // ALGORITMO DI RICERCA
        while (i < dimensioneLogica && !trovato) {
            if (registro[i] != null && registro[i].getId().equals(id)) {
                trovato = true;
                // Non incrementiamo i, ci serve l'indice per lo shift
            } else {
                i++;
            }
        }
         

        // ALGORITMO DI RIMOZIONE (SHIFT)
        if (trovato) {
            j = i;
            synchronized(registro){
                file =new File( registro[i].getNomeFile());
                if(file.exists()){
                    file.delete();
                }
                while (j < dimensioneLogica - 1) {
                    registro[j] = registro[j + 1];
                    j++;
                }
                // Pulizia ultimo elemento e decremento dimensione
                registro[dimensioneLogica - 1] =  new Noleggio();
                dimensioneLogica--;

                result = 1; // Successo
                System.out.println("Server: id " + id + " rimosso.");
            }
        } else {
            System.out.println("Server: id " + id + " non trovato.");
        }
            


        return result;
    }

    public int noleggia_sci(String id, int giorno, int mese, int anno, int durata )  throws RemoteException {
        int result = -1;
        int i = 0;
        boolean trovato = false;
        synchronized(registro){
            while(i<dimensioneLogica && !trovato){
                if(registro[i].getId().equals(id)){
                    trovato = true;
                    if(registro[i].isLibero()){
                            registro[i].setNoleggio(giorno, mese, anno, durata);
                            result = 1;
                    }
                    
                }else
                    i++;
            }
        }

       
        return result;
    }
}
