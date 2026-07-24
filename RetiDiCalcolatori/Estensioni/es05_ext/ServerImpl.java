import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.net.*;

public class ServerImpl extends UnicastRemoteObject implements RemOp {
    public ServerImpl() throws RemoteException { 
        super(); 
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
             if (System.getSecurityManager() == null){
                 System.setSecurityManager(new RMISecurityManager()); 
             }
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

    public Result getDirectoryInfoClientActive(String directoryName) throws RemoteException {
        Result result = null; //dichiaro le variabili nel metodo per non avere sovrapposizione
        File dir = null;        //con altri clienti
        File[] files = null;
        result = new Result();
        int count = 0;
        int    i = 0;
         ServerThread t = null;
        System.out.println("ricevuto directory per il servizio Client attivo: " + directoryName );
        if(directoryName.trim().isEmpty() ){
            result.setErrorMessage("Errore: uno degli input sono errati");
            return result;
        }
        dir = new File(directoryName);
        // Controlli
        if (!dir.exists()) {
            result.setErrorMessage("Errore: directory non esistente");
            return result;
        }
        if (!dir.isDirectory()) {
            result.setErrorMessage("Errore: il percorso specificato non è una directory");
            return result;
        }
        files = dir.listFiles();
        while (files != null && i < files.length) {
            if (files[i].isFile()) {
                System.out.println("salvando: " + files[i].getName());
                result.getFiles()[count] = new Dati(files[i].getName(), files[i].length());
                count++;
            }
            i++;
        }
    // Creazione Thread: passo 'result' per permettere al figlio di settare la porta
        // Passo null come serverSocket perché lo deve creare il figlio
        t = new ServerThread(true, dir.getAbsolutePath(), null, 0, result);
        t.start();

        // Sincronizzazione: Il padre deve aspettare che il figlio crei la socket e scriva la porta
        //questo perché come richiesto da specifica dell'estensione il figlio deve gestire la creazione della socket
        //alternativamente si poteva creare la porta il padre ma non avrebbe rispettato i requisiti di progetto
        //oppure si poteva utilizzare una porta statica ma il server sarebbe stato sequenziale dato che
        //per due richieste ci sarebbe lato server un errore della condivisione della porta
        synchronized (result) {
            // Aspetto finché la porta è -1 e non ci sono errori
            while (result.getServerPort() == -1 && result.getErrorMessage() == null) {
                try {
                    result.wait();
                } catch (InterruptedException e) {
                    // Gestione interruzione
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public Result getDirectoryInfoServerActive(String directoryName, InetAddress clientHost, int clientPort) throws RemoteException {
        Result result = null;

        File dir = null;
        File[] files = null;
        result = new Result();
        int count = 0;
        int    i = 0;
        ServerThread t = null;
        System.out.println("ricevuto directory per il servizio Server Attivo: " + directoryName);
        if(directoryName.trim().isEmpty() || clientHost == null || clientPort < 1024 || clientPort > 65535){
            result.setErrorMessage("Errore: uno degli input sono errati " + directoryName + " " + clientPort);
            return result;
        }

         dir = new File(directoryName);
      // Controlli
        if (!dir.exists()) {
            result.setErrorMessage("Errore: directory non esistente");
            return result;
        }
        if (!dir.isDirectory()) {
            result.setErrorMessage("Errore: il percorso specificato non è una directory");
            return result;
        }

        files = dir.listFiles();
        if (files == null) {
            result.setErrorMessage("Errore: impossibile leggere i file della directory");
            return result;
        }
        while (files != null && i < files.length) {
            if (files[i].isFile()) {
                System.out.println("salvando: " + files[i].getName() );

                result.getFiles()[count] =  new Dati(files[i].getName(), files[i].length());;
                count++;
            }
            i++;
        }
            System.out.println("inizio thread" + System.lineSeparator());

         t = new ServerThread(false,dir.getAbsolutePath(), clientHost, clientPort, result);
        t.start();
        return result;
    }
}
