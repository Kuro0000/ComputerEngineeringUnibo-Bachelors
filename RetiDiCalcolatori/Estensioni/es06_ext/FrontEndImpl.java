import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class FrontEndImpl extends UnicastRemoteObject implements FrontEndClientServer, FrontEndRegistry {
    // Variabili di istanza
    private static final int MAX_REGISTRY = 10;
    private char[] intervalloInizio;
    private char[] intervalloFine;
    private RegistryRemotoServer[] registries;
    private int count;

    public FrontEndImpl() throws RemoteException {
        super();
        // Inizializzazione variabili
        int i = 0;
        intervalloInizio = new char[MAX_REGISTRY];
        intervalloFine = new char[MAX_REGISTRY];
        registries = new RegistryRemotoServer[MAX_REGISTRY];
        count = 0;
        
        while (i < MAX_REGISTRY) {
            intervalloInizio[i] = 0;
            intervalloFine[i] = 0;
            registries[i] = null;
            i++;
        }
    }

    // Metodo chiamato dai RegistryRemoti per accreditarsi
    public synchronized boolean registraFE(char inizio, char fine, RegistryRemotoServer registry) throws RemoteException {
        // Variabili locali tutte all'inizio
        boolean inserito = false;
        boolean duplicato = false;
        int i = 0;

        if (count >= MAX_REGISTRY || registry == null) {
            return false;
        }

        // Controllo duplicati
        i = 0;
        while (i < count && !duplicato) {
            if (registries[i].equals(registry)) {
                duplicato = true;
            }
            i++;
        }

        // Se non è duplicato, inserisco
        if (!duplicato) {
            intervalloInizio[count] = Character.toUpperCase(inizio);
            intervalloFine[count] = Character.toUpperCase(fine);
            registries[count] = registry;
            count++;
            inserito = true;
            System.out.println("FrontEnd: Registrato nuovo Registry per intervallo " + inizio + "-" + fine);
        }

        return inserito;
    }

    // Metodo chiamato dai ServerCongresso per registrarsi
    public synchronized boolean registra(String nomeLogico, Remote riferimento) throws RemoteException {
        // Variabili locali
        boolean esito = false;
        int i = 0;
        char c = 0;
        boolean trovato = false;

        if (nomeLogico == null || riferimento == null || nomeLogico.length() == 0) {
            return false;
        }

        c = Character.toUpperCase(nomeLogico.charAt(0));
        
        // Cerco il registry competente
        i = 0;
        while (i < count && !trovato) {
            if (c >= intervalloInizio[i] && c <= intervalloFine[i]) {
                // Delego la registrazione al registry competente
                esito = registries[i].aggiungi(nomeLogico, riferimento);
                trovato = true;
            }
            i++;
        }

        if (!trovato) {
            System.out.println("FrontEnd: Nessun Registry competente trovato per " + nomeLogico);
        }

        return esito;
    }

    // Metodo per cercare un servizio (Client)
    public synchronized Remote cercaFE(String nomeLogico) throws RemoteException {
        // Variabili locali
        Remote risultato = null;
        int i = 0;
        char c = 0;
        boolean trovato = false;

        if (nomeLogico == null || nomeLogico.length() == 0) {
            return null;
        }

        c = Character.toUpperCase(nomeLogico.charAt(0));

        // Scansione array per competenza
        i = 0;
        while (i < count && !trovato) {
            if (c >= intervalloInizio[i] && c <= intervalloFine[i]) {
                risultato = registries[i].cerca(nomeLogico);
                trovato = true;
            }
            i++;
        }

        return risultato;
    }
    
    // Metodo per cercare tutti i servizi (Client) - Aggiunto come da specifica dell'estensione
    public synchronized Remote[] cercaTuttiFE(String nomeLogico) throws RemoteException {
        // Variabili locali
        Remote[] risultato = null;
        int i = 0;
        char c = 0;
        boolean trovato = false;
        
        if (nomeLogico == null || nomeLogico.length() == 0) {
            return new Remote[0];
        }

        c = Character.toUpperCase(nomeLogico.charAt(0));

        i = 0;
        while (i < count && !trovato) {
             if (c >= intervalloInizio[i] && c <= intervalloFine[i]) {
                risultato = registries[i].cercaTutti(nomeLogico);
                trovato = true;
            }
            i++;
        }
        
        if (risultato == null) {
            risultato = new Remote[0];
        }
        
        return risultato;
    }

    public static void main(String[] args) {
        // Variabili tutte all'inizio
        int registryPort = 1099;
        String registryHost = "localhost";
        String frontEndName = "FrontEnd";
        String completeName = null;
        FrontEndImpl serverRMI = null;

        // Controllo argomenti
        if (args.length != 0 && args.length != 1) {
            System.out.println("Sintassi: FrontEndImpl [registryPort]");
            System.exit(1);
        }
        if (args.length == 1) {
            try {
                registryPort = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.out.println("Sintassi: FrontEndImpl [registryPort] (intero)");
                System.exit(2);
            }
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        completeName = "//" + registryHost + ":" + registryPort + "/" + frontEndName;
        
        try {
            serverRMI = new FrontEndImpl();
            Naming.rebind(completeName, serverRMI);
            System.out.println("FrontEnd: Servizio \"" + frontEndName + "\" registrato e pronto.");
        } catch (Exception e) {
            System.err.println("FrontEnd Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}