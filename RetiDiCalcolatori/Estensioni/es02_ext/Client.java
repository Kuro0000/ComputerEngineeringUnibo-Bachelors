import java.net.*;
import java.io.*;

public class Client {

    /*
    ESTENSIONE:Si estenda il programma sviluppato in modo che gestisca il
    trasferimento di più direttori dal server al client (multiple get di più
    direttori)
    Per ogni richiesta, il Client richiede all'utente il tipo della richiesta che
    viene inoltrata al server (mput o mget); poi, Client e Server si
    coordinano per portare a termine l'operazione richiesta. Al termine, il
    client si pone in attesa di una nuova richiesta dell'utente (mput e mget)
    fino alla terminazione dell'interazione.
    il Client dovrà creare in locale un direttorio con lo
    stesso nome di quello richiesto, dove verranno salvati i file inviati dal
    server, quindi dovrà salvare in tale direttorio i file in arrivo dal server, e
    mettersi in attesa di una nuova richiesta dell’utente
    -------------------------------------------------------
    OBBIETTIVO: 
    modifichiamo il comportamento in maniera tale da ricreare le directory originali al server
    e implementare mget, modificando il comportamento di attiva e salta, dato che le directory
    le dobbiamo ricreare tutta localmente
    */

    public static void main(String[] args) throws IOException {
        // Dichiarazione variabili PRIMA del ciclo
        InetAddress addr = null;
        int port = -1;
        Socket socket = null;
        DataInputStream inSock = null;
        DataOutputStream outSock = null;
        
        try {
            if (args.length == 2) {
                addr = InetAddress.getByName(args[0]);
                port = Integer.parseInt(args[1]);
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java Client serverAddr serverPort");
                    System.exit(1);
                }
            } else {
                System.out.println("Usage: java Client serverAddr serverPort");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java Client serverAddr serverPort");
            System.exit(2);
        }

        try {
            socket = new Socket(addr, port);
            socket.setSoTimeout(30000);
            System.out.println("Creata la socket: " + socket);
            inSock = new DataInputStream(socket.getInputStream());
            outSock = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Problemi nella creazione degli stream su socket: ");
            e.printStackTrace();
            System.exit(1);
        }
        BufferedReader stdIn = null;
        String operazione = null;
        String nomeDirettorio = null;
        FileOutputStream outFile = null;
        File dir = null;
        File[] files = null;
        int i = 0;
        int bufferRead = -1;

        long dim = 0;
        File file = null;
        int dirEsiste = -1;
        int cont = 0;
        String relativePath =null;
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        cont = 0;
        System.out.print("Client Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti operazione: ");

        try {
            while ((operazione = stdIn.readLine()) != null) {
                //MPUT----------------------------------------
                if (operazione.equals("mput")) {
                    System.out.print("Inserire il nome del direttorio: ");
                    nomeDirettorio = stdIn.readLine();
                    dir = new File(nomeDirettorio);

                    if (dir.exists() && dir.isDirectory()) {
                        outSock.writeUTF(operazione);//prima di inviare l'operazione mi accerto che esista nel client la directory
                        System.out.println("Inviata operazione: " + operazione);
                        outSock.writeUTF(nomeDirettorio);
                       
                      
                        files = dir.listFiles();
                        System.out.println("File totali nel direttorio: " + files.length);

                          sendAllFilesWithMarker( "", outSock, files);
                          
                        //fine invio dei file del direttorio
                        outSock.writeLong(-1);

                    }
                    //MGET -----------------------------------------------
                } else if (operazione.equals("mget")) {
                    outSock.writeUTF(operazione);
                    

                    System.out.print("Inserire il nome del direttorio: ");
                    nomeDirettorio = stdIn.readLine();
                    //richiedo il direttorio desiderato
                    outSock.writeUTF(nomeDirettorio);
                    System.out.println("Richiesto direttorio: " + nomeDirettorio);
                    //attendo e controllo se esiste nel lato server il direttorio, in caso non esista, salto passando alla
                    //prossima operazione
                    dirEsiste = inSock.readInt();

                    if(dirEsiste != 0){
                        System.out.println("Il direttorio non esiste sul server.");
                        continue;
                    } else {
                        dir = new File(nomeDirettorio);
                        
                        if (!dir.exists()) {
                            dir.mkdir(); // La creo solo se manca
                            System.out.println("Creata directory locale: " + nomeDirettorio);
                        } else {
                            System.out.println("Directory locale esistente: unisco i file.");
                        }
                    }
                    System.out.println("MGET - Direttorio: " + nomeDirettorio);
                    while((dim = inSock.readLong())!=-1) {
   
                    System.out.println("Dimensione ricevuto: " + dim);
                    relativePath = inSock.readUTF();
                     file = new File(dir, relativePath);
                    file.getParentFile().mkdirs();

                        try {
                            outFile = new FileOutputStream(file);
                            cont = 0;
                            // **RICEVI IL FILE**
                            while (cont < dim && (bufferRead=inSock.read()) >= 0) {
                                outFile.write(bufferRead);
                                cont++;
                            }
                            outFile.flush();
                            outFile.close();
                            System.out.println(" File ricevuto: " +file.getName() +" "+ + cont + " bytes");
                            
                        } catch (Exception e) {
                            System.out.println("Problemi nel salvataggio: ");
                            e.printStackTrace();
                            
                        }
                        outFile.close(); 
                        
                    }
                    System.out.println("Trasferimento directory completato");
                }
                System.out.print("\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti operazione: ");
            }
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            try {
                socket.close();
                System.out.println("Client: termino...");
            } catch (IOException ef) {
                System.out.println("Problemi nella chiusura della socket: ");
                ef.printStackTrace();
            }
        }
    }


    private static void sendAllFilesWithMarker( String relativePath, DataOutputStream outSock, File[] files) throws IOException {
        //dato che dobbiamo ricostruire tutta la cartella in questa estensione
        //dobbiamo fare una ricorsione
    if (files == null) {
        System.out.println("directory vuota");
        return;
    }
    //dichiaro le variabili all'interno del metodo poiché sarei costretto in caso
    //a dover dichiarare le variabili statiche che potrebbe causare problemi di risorse
    //condivise tra le varie istanze di Client
    FileInputStream inFile = null; 
    long dim = -1;
    int cont = -1;
    int bufferWrite = -1;
    String fileRelativePath =null;
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