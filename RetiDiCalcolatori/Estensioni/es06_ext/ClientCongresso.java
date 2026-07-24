import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;

class ClientCongresso {

    public static void main(String[] args) {
        // Variabili tutte all'inizio
        int registryPort = 1099;
        String registryHost = null;
        String frontEndName = "FrontEnd";
        String serviceName = "ServerCongresso";
        String completeName = null;
        BufferedReader stdIn = null;
        String service = null;
        FrontEndClientServer frontEnd = null;
        ServerCongresso serverCongresso = null;
        int g = 0;
        String sess = null;
        String speak = null;
        boolean ok = false;

        // Controllo argomenti
        if (args.length != 1 && args.length != 2) {
            System.out.println("Sintassi: ClientCongresso FrontEndHost [FrontEndPort]");
            System.exit(1);
        }
        
        registryHost = args[0];
        if (args.length == 2) {
            try {
                registryPort = Integer.parseInt(args[1]);
            } catch (Exception e) {
                System.out.println("Porta non valida");
                System.exit(1);
            }
        }

        stdIn = new BufferedReader(new InputStreamReader(System.in));

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        try {
            completeName = "//" + registryHost + ":" + registryPort + "/" + frontEndName;
            
            // Ottengo riferimento al FrontEnd
            frontEnd = (FrontEndClientServer) Naming.lookup(completeName);
            
            // Chiedo al FrontEnd di trovarmi il ServerCongresso
            // Il FrontEnd cercherà nel registry competente per la lettera 'S'
            serverCongresso = (ServerCongresso) frontEnd.cercaFE(serviceName);

            if (serverCongresso == null) {
                System.out.println("Servizio " + serviceName + " non trovato.");
                System.exit(1);
            }
            
            System.out.println("Client: Servizio \"" + serviceName + "\" connesso");
            System.out.print("Servizio (R=Registrazione, P=Programma, EOF=Fine): ");

            while ((service = stdIn.readLine()) != null) {

                if ("R".equals(service)) {
                    ok = false;
                    System.out.print("Giornata (1-3)? ");
                    while (!ok) {
                        try {
                            g = Integer.parseInt(stdIn.readLine());
                            if (g >= 1 && g <= 3) ok = true;
                            else System.out.print("Riprova (1-3): ");
                        } catch (Exception e) {
                            System.out.print("Riprova (numero): ");
                        }
                    }

                    System.out.print("Sessione (S1..S12)? ");
                    sess = stdIn.readLine(); // Semplifico loop per brevità, l'importante è la logica RMI
                    
                    System.out.print("Speaker? ");
                    speak = stdIn.readLine();

                    if (serverCongresso.registrazione(g, sess, speak) == 0)
                        System.out.println("Registrazione OK");
                    else
                        System.out.println("Registrazione fallita (piena o errore)");
                } 
                else if ("P".equals(service)) {
                    ok = false;
                    System.out.print("Giornata (1-3)? ");
                    while (!ok) {
                         try {
                            g = Integer.parseInt(stdIn.readLine());
                            if (g >= 1 && g <= 3) ok = true;
                        } catch (Exception e) {}
                    }
                    serverCongresso.programma(g).stampa();
                }

                System.out.print("\nServizio (R=Registrazione, P=Programma): ");
            }

        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}