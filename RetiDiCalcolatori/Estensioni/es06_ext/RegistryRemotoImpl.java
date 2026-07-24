import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RegistryRemotoImpl extends UnicastRemoteObject implements RegistryRemotoServer {

    // Variabili globali classe
    final int tableSize = 100;
    Object[][] table = new Object[tableSize][2];

    public RegistryRemotoImpl() throws RemoteException {
        super();
        int i = 0;
        while (i < tableSize) {
            table[i][0] = null;
            table[i][1] = null;
            i++;
        }
    }

    // --- Metodi esistenti (adattati allo stile while/no break) ---

    public synchronized boolean aggiungi(String nomeLogico, Remote riferimento) throws RemoteException {
        boolean risultato = false;
        int i = 0;
        
        if ((nomeLogico == null) || (riferimento == null))
            return false;
            
        while (i < tableSize && !risultato) {
            if (table[i][0] == null) {
                table[i][0] = nomeLogico;
                table[i][1] = riferimento;
                risultato = true;
            }
            i++;
        }
        return risultato;
    }

    public synchronized Remote cerca(String nomeLogico) throws RemoteException {
        Remote risultato = null;
        int i = 0;
        boolean trovato = false;
        
        if (nomeLogico == null) return null;
        
        while (i < tableSize && !trovato) {
            if (table[i][0] != null && nomeLogico.equals((String) table[i][0])) {
                risultato = (Remote) table[i][1];
                trovato = true;
            }
            i++;
        }
        return risultato;
    }

    public synchronized Remote[] cercaTutti(String nomeLogico) throws RemoteException {
        int cont = 0;
        int i = 0;
        Remote[] risultato = null;
        
        if (nomeLogico == null) return new Remote[0];
        
        // Conta occorrenze
        while (i < tableSize) {
            if (table[i][0] != null && nomeLogico.equals((String) table[i][0])) {
                cont++;
            }
            i++;
        }
        
        risultato = new Remote[cont];
        cont = 0;
        i = 0;
        
        // Riempimento
        while (i < tableSize) {
            if (table[i][0] != null && nomeLogico.equals((String) table[i][0])) {
                risultato[cont] = (Remote) table[i][1];
                cont++;
            }
            i++;
        }
        return risultato;
    }

    public synchronized Object[][] restituisciTutti() throws RemoteException {
        int cont = 0;
        int i = 0;
        Object[][] risultato = null;
        
        while (i < tableSize) {
            if (table[i][0] != null) cont++;
            i++;
        }
        
        risultato = new Object[cont][2];
        cont = 0;
        i = 0;
        while (i < tableSize) {
            if (table[i][0] != null) {
                risultato[cont][0] = table[i][0];
                risultato[cont][1] = table[i][1];
                cont++;
            }
            i++;
        }
        return risultato;
    }

    public synchronized boolean eliminaPrimo(String nomeLogico) throws RemoteException {
        boolean risultato = false;
        int i = 0;
        
        if (nomeLogico == null) 
            return false;
        
        while (i < tableSize && !risultato) {
            if (nomeLogico.equals((String) table[i][0])) {
                table[i][0] = null;
                table[i][1] = null;
                risultato = true;
            }
            i++;
        }
        return risultato;
    }

    public synchronized boolean eliminaTutti(String nomeLogico) throws RemoteException {
        boolean risultato = false;
        int i = 0;
        
        if (nomeLogico == null) return false;
        
        while (i < tableSize) {
            if (nomeLogico.equals((String) table[i][0])) {
                table[i][0] = null;
                table[i][1] = null;
                risultato = true;
            }
            i++;
        }
        return risultato;
    }

    // --- MAIN MODIFICATO PER REGISTRARSI AL FRONTEND ---
    public static void main(String[] args) {
        // Variabili iniziali
        int registryPort = 1099; // Porta del registry locale (dove mi bindo io)
        String registryHost = "localhost";
        String serviceName = "RegistryRemoto"; // Nome con cui mi registro sul mio RMI registry locale
        
        // Variabili per FrontEnd
        String frontEndHost = "localhost";
        int frontEndPort = 1099;
        String frontEndServiceName = "FrontEnd";
        char charStart = 'A';
        char charEnd = 'Z';
        String completeName = null;
        String completeFrontEndName = null;
        RegistryRemotoImpl serverRMI = null;
        FrontEndRegistry frontEnd = null;

        // Argomenti: [registryPort] [FrontEndHost] [FrontEndPort] [CharStart] [CharEnd]
        // Esempio: 1099 localhost 1099 A M
        if (args.length < 4) {
             System.out.println("Sintassi: RegistryRemotoImpl <registryPort> <FrontEndHost> <FrontEndPort> <CharStart> <CharEnd>");
             System.exit(1);
        }

        try {
            registryPort = Integer.parseInt(args[0]);
            frontEndHost = args[1];
            frontEndPort = Integer.parseInt(args[2]);
            charStart = args[3].charAt(0);
            charEnd = args[4].charAt(0);
        } catch (Exception e) {
            System.out.println("Errore parsing argomenti");
            System.exit(1);
        }

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new RMISecurityManager());

        try {
            // 1. Creo e registro me stesso sul mio registry locale
            completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
            serverRMI = new RegistryRemotoImpl();
            Naming.rebind(completeName, serverRMI);
            System.out.println("Registry Remoto locale avviato su " + completeName);

            // 2. Cerco il FrontEnd
            completeFrontEndName = "//" + frontEndHost + ":" + frontEndPort + "/" + frontEndServiceName;
            frontEnd = (FrontEndRegistry) Naming.lookup(completeFrontEndName);

            // 3. Mi registro al FrontEnd
            if (frontEnd.registraFE(charStart, charEnd, serverRMI)) {
                 System.out.println("Registrazione presso FrontEnd avvenuta con successo per range " + charStart + "-" + charEnd);
            } else {
                 System.out.println("Errore registrazione presso FrontEnd (forse range duplicato o pieno)");
                 System.exit(1);
            }

        } catch (Exception e) {
            System.err.println("Errore RegistryRemoto: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}