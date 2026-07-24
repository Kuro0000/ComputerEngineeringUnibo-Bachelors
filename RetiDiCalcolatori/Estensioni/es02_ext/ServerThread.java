import java.io.*;
import java.net.*;


class ServerThread extends Thread {
    /*ESTENSIONE:Si estenda il programma sviluppato in modo che gestisca il
    trasferimento di più direttori dal server al client (multiple get di più
    direttori)
    Si estenda ulteriormente il programma sviluppato in modo da abilitare il
    funzionamento del cliente servitore in modalità sia get (dal server al
    client) che put (dal client al server). Si studi in particolare come
    determinare i direttori di partenza e arrivo
    Per ogni richiesta di direttorio, il Server deve inviare tutti i file del
    direttorio, e notificare la fine della trasmissione del direttorio stesso
    Si gestisca inoltre la terminazione dell'interazione fra client e server: il
    client deve poter indicare al server la propria intenzione di chiusura
    dell'interazione
    Una volta terminata la sessione client e server (processo figlio del server
    principale) terminano la propria esecuzione
    -------------------------------------------------------
    OBBIETTIVO:implementare mget e partendo dal codice dell'esercitazione originaria
    modificare il comportamento di mput in maniera tale da ricreare i direttori e sottodirettori
    del file presente nel client 
*/

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
        String nomeDirettorio = null;
        FileOutputStream outFile = null;
        String relativePath = null;
        File dir = null;
        File[] files = null;
        File file = null;
        int cont = 0;
        long dim = -1;
        int bufferRead = -1;

        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //MPUT
                if (operazione.equals("mput")) {
                    nomeDirettorio = inSock.readUTF();
                    dir = new File(nomeDirettorio);
                        
                    if (!dir.exists()) {
                        dir.mkdir(); // La creo solo se manca
                        System.out.println("Creata directory locale: " + nomeDirettorio);
                    } else {
                        System.out.println("Directory locale esistente: unisco i file.");
                    }
                    System.out.println("MPUT - Direttorio: " + nomeDirettorio);
 
                    while((dim = inSock.readLong())!=-1){
                        System.out.println("Dimensione ricevuto: " + dim);           
                    relativePath = inSock.readUTF();
                    file = new File(dir, relativePath);
                    file.getParentFile().mkdirs();
                        
                        try {
                            outFile = new FileOutputStream(file);
                            cont = 0;
                            while (cont < dim && (bufferRead=inSock.read()) >= 0) {
                                outFile.write(bufferRead);
                                cont++;
                            }
                            outFile.flush();
                            outFile.close();
                            System.out.println(" File ricevuto: " + file.getName() + " "+ cont + " bytes");
                            
                        } catch (Exception e) {
                            System.out.println("Problemi nel salvataggio: ");
                            e.printStackTrace();
                            if (outFile != null) {
                                try { outFile.close(); } catch (IOException e1) {}
                            }
                        }
                    }
                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("mget")) {
                    // MGET 
                    nomeDirettorio = inSock.readUTF();
                    System.out.println("Richiesto direttorio: " + nomeDirettorio);

                    dir = new File(nomeDirettorio);

                    if (dir.exists() && dir.isDirectory()) {
                        outSock.writeInt(0);
                        System.out.println("Inviato conferma direttorio esistente: " + nomeDirettorio);

                        files = dir.listFiles();
                        
                        System.out.println("File totali nel direttorio: " + files.length);
                        

                            sendAllFilesWithMarker("", outSock, files);
                            outSock.writeLong(-1);

                            System.out.println("inviato il fine dei file della directory");
                  
                    }else{
                        outSock.writeInt(-1);
                        System.out.println("non esiste la directory");
                    }

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


    private  void sendAllFilesWithMarker(String relativePath, DataOutputStream outSock, File[] files) throws IOException {
        //dato che dobbiamo ricostruire tutta la cartella in questa estensione
        //dobbiamo fare una ricorsione
    if (files == null) {
        System.out.println("directory vuota");
        return;
    }
    //dichiaro le seguenti variabili all'interno del metodo dato che potrebbero esserci
    //dei problemi di risorse condivise durante la ricorsione mentre clientSocket lo lascio
    //private dato che è sempre la stessa istanza
    String fileRelativePath =null;
    FileInputStream inFile = null;
    int bufferWrite = -1;
    long dim = -1;
    int cont = -1;
    for (int i = 0;i<files.length;i++) {
        if (files[i].isDirectory()) {
            // Nuovo percorso relativo per la ricorsione
           fileRelativePath = relativePath.isEmpty() ? files[i].getName() : relativePath + "/" + files[i].getName();
            sendAllFilesWithMarker( fileRelativePath, outSock, files[i].listFiles());
        } else {
                fileRelativePath = relativePath.isEmpty() ? files[i].getName() : relativePath + "/" + files[i].getName();
                dim = files[i].length();
                outSock.writeLong(dim);
                System.out.println("Inviata dimensione: " + dim);
                outSock.writeUTF(fileRelativePath);
                System.out.println("Inviato nome file: " + files[i].getName() + " (" + dim + " bytes)");

                cont = 0;
                bufferWrite = -1;
                try {
                    inFile = new FileInputStream(files[i]);
                    //byte per byte, alternativamente si poteva utilizzare un buffer, ma facendo un cast
                    // per la dimensione del file per via della firma di read
                    while (cont < dim && (bufferWrite = inFile.read()) >= 0) {
                        outSock.write(bufferWrite);
                        cont++;
                    }
                    inFile.close();
                    outSock.flush();
                    System.out.println("File trasferito: " + cont + " bytes");
                } catch (Exception e) {
                    System.out.println("Problemi nel trasferimento: ");
                    e.printStackTrace();
                }
            
        }
    }
}
}