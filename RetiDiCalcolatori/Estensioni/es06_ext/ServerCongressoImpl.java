import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerCongressoImpl extends UnicastRemoteObject implements ServerCongresso {
    
    // Variabili statiche
    static Programma prog[];

    public ServerCongressoImpl() throws RemoteException {
        super();
    }

    public int registrazione(int giorno, String sessione, String speaker) throws RemoteException {
        // Logica invariata, aggiungo solo controlli stile richiesto
        int numSess = -1;
        System.out.println("Server RMI: richiesta registrazione " + speaker);
        
        if (sessione != null && sessione.startsWith("S")) {
            try {
                numSess = Integer.parseInt(sessione.substring(1)) - 1;
            } catch (NumberFormatException e) {}
        }

        if (numSess == -1 || giorno < 1 || giorno > 3)
            throw new RemoteException("Dati errati");

        return prog[giorno - 1].registra(numSess, speaker);
    }

    public Programma programma(int giorno) throws RemoteException {
        System.out.println("Server RMI: richiesto programma giorno " + giorno);
        return prog[giorno - 1];
    }

    public static void main(String[] args) {
        // Variabili tutte all'inizio
        int i = 0;
        int registryPort = 1099;
        String registryHost = "localhost"; // Host del FrontEnd
        String frontEndName = "FrontEnd";
        String serviceName = "ServerCongresso"; // Nome logico del servizio
        String completeName = null;
        ServerCongressoImpl serverRMI = null;
        FrontEndClientServer frontEnd = null;

        // Inizializzazione Array
        prog = new Programma[3];
        while (i < 3) {
            prog[i] = new Programma();
            i++;
        }

        // Args: [FrontEndHost] [FrontEndPort]
        if (args.length > 0) registryHost = args[0];
        if (args.length > 1) {
            try {
                registryPort = Integer.parseInt(args[1]);
            } catch (Exception e) {}
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        completeName = "//" + registryHost + ":" + registryPort + "/" + frontEndName;

        try {
            // Cerco il FrontEnd
            frontEnd = (FrontEndClientServer) Naming.lookup(completeName);
            
            // Creo il mio oggetto remoto
            serverRMI = new ServerCongressoImpl();
            
            // Mi registro tramite il FrontEnd
            // Il FrontEnd calcolerà l'iniziale di "ServerCongresso" ('S') e lo metterà nel registry giusto
            if (frontEnd.registra(serviceName, serverRMI)) {
                System.out.println("ServerCongresso registrato con successo tramite FrontEnd");
            } else {
                System.out.println("Errore: impossibile registrare il servizio (FrontEnd non ha trovato registry competente o errore)");
            }
            
        } catch (Exception e) {
            System.err.println("Server Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}