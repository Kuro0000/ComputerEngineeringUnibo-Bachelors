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
    public static final int MAX_FILES = 256;
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

    public Result lista_file(String directoryName) throws RemoteException {
        Result result = null; //dichiaro le variabili nel metodo per non avere sovrapposizione
      File rootDir = null;
        File[] stackDir = null;     // Simulazione stack per evitare ricorsione
        File currentDir = null;
        File[] filesInDir = null;
        File f = null;
        String name = null;
        int stackTop = 0;           // Puntatore dello stack
        int resIndex = 0;           // Indice risultati
        int i = 0;
        int j = 0;
        int consCount = 0;
        int len = 0;
        char c = ' ';
        boolean validDir = false;

        // Inizializzazione
        rootDir = new File(directoryName);
        stackDir = new File[100];   // Max 100 sottocartelle in profondità/coda
        stackTop = 0;
        resIndex = 0;
        validDir = false;
        result = new Result();
        // Verifica esistenza direttorio iniziale
        if (rootDir.exists()) {
            if (rootDir.isDirectory()) {
                validDir = true;
            }
        }
        //Applicazione dell'algoritmo DFS, in maniera tale da attuare una ricorsione
        //ma in maniera iterativa, cercando in un contesto distribuito di alleviare
        //uno stack overflow come visto a lezione
        if (validDir) {
            // Push della root nello stack
            stackDir[stackTop] = rootDir;
            stackTop = stackTop + 1;

            // Loop principale (simula la ricorsione)
            while (stackTop > 0) {
                // Pop dallo stack
                stackTop = stackTop - 1;
                currentDir = stackDir[stackTop];

                filesInDir = currentDir.listFiles();

                if (filesInDir != null) {
                    i = 0;
                    while (i < filesInDir.length) {
                        f = filesInDir[i];
                        
                        // Se è direttorio, push nello stack
                        if (f.isDirectory()) {
                            if (stackTop < 100) {
                                stackDir[stackTop] = f;
                                stackTop = stackTop + 1;
                            }
                        } else {
                            // Se è file, controllo consonanti
                            name = f.getName();
                            len = name.length();
                            consCount = 0;
                            j = 0;
                            
                            while (j < len) {
                                c = name.charAt(j);
                                // Controllo se è lettera e non è vocale
                                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                                    if (c != 'a' && c != 'e' && c != 'i' && c != 'o' && c != 'u' &&
                                        c != 'A' && c != 'E' && c != 'I' && c != 'O' && c != 'U') {
                                        consCount = consCount + 1;
                                    }
                                }
                                j = j + 1;
                            }

                            // Se ha 3 o più consonanti, aggiungo a risultato
                            if (consCount >= 3) {
                                if (resIndex < 100) {
                                    result.getFiles()[resIndex] = name;
                                    resIndex = resIndex + 1;
                                }
                            }
                        }
                        i = i + 1;
                    }
                }
            }
        } else {
            // Segnalazione errore nel primo slot
            result.setErrorMessage("Errore: Direttorio inesistente") ;
        }
        return result;
    }


    public int numerazione_righe(String nomeFile) throws RemoteException {
        // Dichiarazione variabili all'inizio
        File fileTarget = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        String[] fileContent = null; // Buffer in memoria (no liste dinamiche)
        String line = null;
        int lineCount = 0;
        int modifiedCount = 0;
        int currentLineNum = 1; // Contatore logico righe (1, 2, 3...)
        int i = 0;
        boolean success = false;
        
        // Inizializzazione
        fileTarget = new File(nomeFile);
        fileContent = new String[200]; // Max 200 righe supportate
        lineCount = 0;
        modifiedCount = 0;
        success = true;

        if (!fileTarget.exists()) {
            return -1;
        }

        // Lettura del file in memoria
        try {
            br = new BufferedReader(new FileReader(fileTarget));
            line = br.readLine();
            while (line != null && lineCount < 200) {
                fileContent[lineCount] = line;
                lineCount = lineCount + 1;
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            success = false;
        }

        // Elaborazione in memoria
        if (success) {
            i = 0;
            currentLineNum = 1;
            while (i < lineCount) {
                // Righe dispari: 1, 3, 5... (indici array 0, 2, 4...)
                // Controllo resto della divisione del numero riga
                if ((currentLineNum % 2) != 0) {
                    fileContent[i] = currentLineNum + " " + fileContent[i];
                    modifiedCount = modifiedCount + 1;
                }
                currentLineNum = currentLineNum + 1;
                i = i + 1;
            }

            // Riscrittura del file
            try {
                bw = new BufferedWriter(new FileWriter(fileTarget));
                i = 0;
                while (i < lineCount) {
                    bw.write(fileContent[i]);
                    bw.newLine();
                    i = i + 1;
                }
                bw.close();
            } catch (IOException e) {
                success = false;
            }
        }

        if (!success) {
            return -1;
        }

        return modifiedCount;
    }
}
