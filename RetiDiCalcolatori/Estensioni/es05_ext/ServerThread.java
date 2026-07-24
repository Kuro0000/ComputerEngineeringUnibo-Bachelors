
import java.io.*;
import java.net.*;
class ServerThread extends Thread {
    private boolean acceptMode;
    private InetAddress clientHost;
    private String path;
    private int clientPort;
    private Result result;

public ServerThread(boolean acceptMode, String path, InetAddress clientHost, int clientPort, Result result) {
        if(path==null || result ==null){ // anche se i controlli li faccio già nei metodi controllo anche negli argomenti
                                        //del thread in caso di riutilizzo di nuovi metodi in cui non controllano
            throw new IllegalArgumentException("Errore: path o result nulli");
        }
        this.acceptMode = acceptMode;
        this.path = path;
        this.clientHost = clientHost;
        this.clientPort = clientPort;
        this.result = result;
    }

    public void run() {
        Socket client = null;
        DataOutputStream out = null;
        FileInputStream inFile = null;
        int j = 0, bufferWrite = -1;
        long dim = 0, cont = 0;
        Dati[] files = result.getFiles();
        ServerSocket serverSocket = null;
        try{

            if (acceptMode) {
                try{
                    serverSocket = new ServerSocket(0);
                    // Comunico la porta al padre
                    // Sincronizzazione: Il padre deve aspettare che il figlio crei la socket e scriva la porta
                    //questo perché come richiesto da specifica dell'estensione il figlio deve gestire la creazione della socket
                    //alternativamente si poteva creare la porta il padre ma non avrebbe rispettato i requisiti di progetto
                    //oppure si poteva utilizzare una porta statica ma il server sarebbe stato sequenziale dato che
                    //per due richieste ci sarebbe lato server un errore della condivisione della porta
                    synchronized (result) {
                        result.setServerPort(serverSocket.getLocalPort());
                        result.notify(); // Sveglio il padre in attesa nel getDirectoryInfoClientActive
                    }
                    // Mi metto in attesa del client
                    client = serverSocket.accept();
                    serverSocket.close(); // Chiudo la serverSocket dopo la accept (per singolo client)
                }catch(IOException e){
                    synchronized (result) {
                        result.setErrorMessage("Errore creazione socket figlio: " + e.getMessage());
                        result.notify();
                    }
                    return; // Termino
                }
            } else {
                client = new Socket(clientHost, clientPort);
            }
            client.shutdownInput();
        }catch(IOException ioe){
            System.out.println("errore di socket");
            return;
        }
        try {
            
            out = new DataOutputStream(client.getOutputStream());
            while ( j < files.length && files[j] != null) {
                dim = files[j].length();
                cont = 0;
                inFile = new FileInputStream(new File(path, files[j].getName()));
                while (cont < dim && (bufferWrite = inFile.read()) != -1) {
                    out.write(bufferWrite);
                    cont++;
                }
                inFile.close();
                j++;
            }
            out.flush();
            client.shutdownOutput();
            out.close();
            client.close();
            System.out.println("servizio terminato, in attesa di altri client...");
        } catch (Exception ex) {
            try {  
                if(inFile!=null)
                    inFile.close(); 
                if(out!=null)
                    out.close(); 
                if(out!=null)
                    client.close();
            } catch (IOException e) {}
        }
    }
}
