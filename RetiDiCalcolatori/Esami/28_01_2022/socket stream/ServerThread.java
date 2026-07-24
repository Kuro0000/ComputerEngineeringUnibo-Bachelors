
/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.*;
import java.net.*;


class ServerThread extends Thread {

    private Socket clientSocket = null; 
    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Attivazione figlio: " + Thread.currentThread().getName());

        DataInputStream inSock = null;
        DataOutputStream outSock = null;


        try {
            inSock = new DataInputStream(clientSocket.getInputStream());
            outSock = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException ioe) {
            System.out.println("Problemi nella creazione degli stream di input/output su socket: ");
            ioe.printStackTrace();
            return;
        }
        //dichiarazioni delle variabili prima del ciclo
        String operazione = null;
        String parola = null;
        String fileRelativePath = null;
        String nomeFile = null;
        String direttorio = null;
        int i, j, cont, result, bufferWrite;
        boolean trovato;
        FileInputStream inFile= null;
        File[] filesInDir = null;
        File[] stackDir = null;     // Simulazione stack per evitare ricorsione
        int stackTop = 0;           // Puntatore dello stack
        int resIndex = 0;           // Indice risultati
        long dim, soglia;

        File dir = null;
        File file = null;
        File outFile = null;
        FileWriter fw = null;
        BufferedReader br      = null;   
        String line = null;
        int pos = -1;
        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //iscrizione studente
                if (operazione.equals("is")) {
                        direttorio = inSock.readUTF();
                        soglia = inSock.readLong();
                        System.out.println("Richiesto con soglia: " + soglia);

                        dir = new File(direttorio);
                    if (dir.exists() && dir.isDirectory()) {
                        outSock.writeInt(0);
                        stackDir = new File[100];   // Max 100 sottocartelle in profondità/coda

                        //INIZIO
                        // Push della root nello stack
                        stackDir[stackTop] = dir;
                        stackTop = stackTop + 1;

                        //Applicazione dell'algoritmo DFS, in maniera tale da attuare una ricorsione
                        //ma in maniera iterativa, cercando in un contesto distribuito di alleviare
                        //uno stack overflow, creatosi da una esecuzione ricorsiva, come visto a lezione
                        while (stackTop > 0) {
                            // Pop dallo stack
                            stackTop = stackTop - 1;
                            dir = stackDir[stackTop];

                            filesInDir = dir.listFiles();

                            if (filesInDir != null) {
                                i = 0;
                                while (i < filesInDir.length) {
                                    file = filesInDir[i];
                                    
                                    // Se è direttorio, push nello stack
                                    if (file.isDirectory()) {
                                        if (stackTop < 100) {
                                            stackDir[stackTop] = file;
                                            stackTop = stackTop + 1;
                                        }
                                    } else if(( dim = filesInDir[i].length())>=soglia && filesInDir[i].getName().endsWith(".txt")){
                                       
                                        outSock.writeLong(dim);
                                        System.out.println("Inviata dimensione: " + dim);
                                        
                                        fileRelativePath = filesInDir[i].getName();
                                        outSock.writeUTF(fileRelativePath);
                                        System.out.println("Inviato nome file: " + filesInDir[i].getName() + " (" + dim + " bytes)");

                                        cont = 0;
                                        bufferWrite = -1;
                                        try {
                                            inFile = new FileInputStream(filesInDir[i]);
                                            // byte per byte
                                            while (cont < dim && (bufferWrite = inFile.read()) >= 0) {
                                                outSock.write(bufferWrite);
                                                cont++;
                                            }
                                            outSock.flush();
                                            System.out.println("File trasferito: " + cont + " bytes");
                                            inFile.close();
                                        } catch (Exception e) {
                                            System.out.println("Problemi nel trasferimento: ");
                                            e.printStackTrace();
                                        }
                                    }else{
                                        System.out.println("soglia non valida oppure non è un file di testo");
                                    }
                                        
                                        
                        
                                      i = i + 1;
                                }
                                    
              
                            }
                        }
                    
                        outSock.writeLong(-1);
                        System.out.println("inviato il fine dei file della directory");

                    }else{
                                                    
                        outSock.writeLong(-1);
                        System.out.println("nessuna directory trovata");
                        
                                
                    }


                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("el")) {
                    // DEFINIRE
                    System.out.println("ricevuto dal client operazione " + operazione);
                   
                  
                    nomeFile = inSock.readUTF();
                    System.out.println("ricevuto dal client nomefile " + nomeFile);

                    parola = inSock.readUTF();
                      System.out.println("ricevuto dal client parola " + parola);

                    pos = -1;
                    cont = 0;
                    try{
                            file = new File(nomeFile);
                            outFile = new File("temp");
                            System.out.println("creato file temporaneo");
                            if(!file.exists() || !file.canWrite()){
                                result = -1;
                            }else{
                                System.out.println("file esiste e inizio l'operazione");
                            fw = new FileWriter(outFile);
                            br = new BufferedReader(new FileReader(file));
                            while((line = br.readLine())!=null){
                                        // Continua finché indexOf non restituisce -1
                                while ((pos = line.indexOf(parola)) != -1) {
                                    cont++;
                                    // Rimuovi la parola trovata
                                    line = line.substring(0, pos) + line.substring(pos + parola.length());
                    
                                }
                                fw.append(line+'\n');

                            }
                            System.out.println("fine operazione");
                            result = cont;
                            br.close();
                            fw.close();
                            // Sostituisce il file originale
                            if (file.delete()) {
                                if (!outFile.renameTo(file)) {
                                    outFile.delete();// errore, eliminiamo il file temporaneo creato
                                    System.err.println("Errore nella sostituzione del file originale: " + file.getName());
                                } 
                            } else {
                                outFile.delete();// errore, eliminiamo il file temporaneo creato
                                System.err.println("Errore eliminando il file originale " + file.getName());
                            }
                            }
                        
                        
                    }catch(Exception e){
                        System.out.println("eccezione lato server " + e.getMessage());
                            result = -1;
                    }
                        outSock.writeInt(result);
                        System.out.println("Server: Parola " + parola + " rimosso.");
                    
                    System.out.println("caricato lato server");
                } else {
                    System.out.println("Operazione non supportata: " + operazione);
                }

                System.out.println("Operazione completata");
            }

        } catch(EOFException eof){
            try{
                    outSock.flush();
                    clientSocket.close();
                System.out.println("ServerThread: termino...");
            } catch(IOException ef){
                ef.printStackTrace();
            }
            
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            try{
                clientSocket.close();
                System.out.println("ServerThread: termino...");
            } catch(IOException ef){
                ef.printStackTrace();
            }
        }
    }


}