import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;

public class Client {

    public static void main(String[] args) {
        // VARIABILI IN TESTA
        int registryPort = 1099;
        String registryHost = null;
        String serviceName = "Server";
        BufferedReader stdIn = null;
        String directoryName = null;
        String mode = null;
        Result dirInfo = null;
        long dim = 0;
        String nomeFile = null;
        FileOutputStream outFile = null;
        int cont = 0;
        int bufferRead = 0;
        ServerSocket serverSocket = null;
        int clientPort = 0;
        InetAddress clientHost = null;
        Socket socket = null;
        DataInputStream in = null;
        Dati[] files =null;
        int i = 0;
        stdIn = new BufferedReader(new InputStreamReader(System.in));

        if (args.length != 1 && args.length != 2) 
            return;
            registryHost = args[0];
            try{
                if (args.length == 2) 
                    registryPort = Integer.parseInt(args[1]);
            }catch(NumberFormatException e){
                System.out.println("formato sbagliato");
                System.exit(1);
            }
              if (System.getSecurityManager() == null){
                  System.setSecurityManager(new RMISecurityManager()); 
              }
        try {
            String completeName = "//" + registryHost + ":" + registryPort + "/" + serviceName;
            RemOp serverRMI = (RemOp) Naming.lookup(completeName);
            System.out.println("Client RMI: Servizio \"" + serviceName + "\" connesso");
            System.out.print("Nome directory da trasferire (EOF per terminare): ");

            while ((directoryName = stdIn.readLine()) != null) {
                System.out.println("Modalità (client=ClientAttivo, server=ServerAttivo): ");
                mode = stdIn.readLine();

                if ("client".equals(mode)) {
                    System.out.println("Iniziato modalità Cliente attivo:");
                    dirInfo = serverRMI.getDirectoryInfoClientActive(directoryName);

                    if (dirInfo.getErrorMessage()==null) {

                            try{
                            socket = new Socket(registryHost, dirInfo.getServerPort());
                            socket.shutdownOutput();
                            }catch(IOException ioe){
                                System.out.println("riprovare l'input");
                                ioe.printStackTrace();
                                continue;
                            }
                        try {
                            in = new DataInputStream(socket.getInputStream());
                            files = dirInfo.getFiles();
                            i = 0;
                            while(files[i] != null && i < files.length){
                                dim = files[i].length();
                                nomeFile = files[i].getName();
                                cont = 0;
                                outFile = null;
                                try {
                                    outFile = new FileOutputStream(new File(nomeFile));
                                    while (cont < dim && (bufferRead = in.read()) != -1) {
                                        outFile.write(bufferRead);
                                        cont++;
                                    }
                                    outFile.close();

                                    System.out.println("File ricevuto: " + nomeFile + " " + cont + " bytes");

                                } catch (Exception e) {
                                    System.out.println("Problemi nel salvataggio:");
                                    e.printStackTrace();
                                }
                                i++;

                            }
                            socket.shutdownInput();
                            in.close();
                            socket.close();
                            System.out.println("Trasferimento directory completato");
                        } catch (IOException e) {
                            System.out.println("Errore durante il trasferimento");
                            e.printStackTrace();
                            if(socket!=null){
                                socket.shutdownInput();
                                socket.close();
                            }
                            if(in !=null)
                                in.close();
                            if(outFile!=null) {
                                try {
                                    outFile.close();
                                } catch (IOException ex) {
                                    System.out.println("file non chiuso");
                                }
                            }
                            
                        }
                    } else {
                        System.out.println("Errore: " + dirInfo.getErrorMessage());
                    }
                } else if ("server".equals(mode)) {
                    System.out.println("Iniziato modalità Server attivo:");
                    try {
                         try { 
                        serverSocket = new ServerSocket(0);
                        clientPort = serverSocket.getLocalPort();
                        clientHost = InetAddress.getLocalHost(); 
                        } catch (IOException ioe) {
                                System.out.println("riprovare l'input");
                                ioe.printStackTrace();
                                continue;
                        }
                            

                        System.out.println("In ascolto su " + clientHost + ":" + clientPort);
                        dirInfo = serverRMI.getDirectoryInfoServerActive(directoryName, clientHost, clientPort);

                        if (dirInfo.getErrorMessage()==null) {
                            try {
                                socket = serverSocket.accept();
                                socket.shutdownOutput();
                                in = new DataInputStream(socket.getInputStream());
                                files = dirInfo.getFiles();
                                i = 0;
                                while(files[i] != null && i < files.length){
                                    dim = files[i].length();
                                    nomeFile = files[i].getName();
                                    cont = 0;
                                    outFile = null;
                                    try {
                                        outFile = new FileOutputStream(new File(nomeFile));
                                    while (cont < dim && (bufferRead = in.read()) != -1) {
                                        
                                        outFile.write(bufferRead);
                                        cont++;
                                    }
                                        outFile.close();
                                        System.out.println("File ricevuto: " + nomeFile + " " + cont + " bytes");
                                    } catch (Exception e) {
                                        System.out.println("Problemi nel salvataggio:");
                                        e.printStackTrace();
                                    }
                                    i++;
                                } 
                                socket.shutdownInput(); 
                                in.close();
                                socket.close();
                                serverSocket.close();
                                System.out.println("Trasferimento directory completato");
                            } catch (IOException e) {
                                System.out.println("Errore durante il trasferimento: " + (e.getMessage() != null ? e.getMessage() : "<nessun messaggio>"));
                                e.printStackTrace();
                                if (serverSocket != null)
                                    serverSocket.close(); 
                                     if(socket!=null){
                                            socket.shutdownInput();
                                            socket.close();
                                        }
                                        if(in !=null)
                                            in.close();
                                        if(outFile!=null) {
                                        outFile.close();
                                        }
                            }
                        } else {
                            System.out.println("Errore: " + dirInfo.getErrorMessage());
                            serverSocket.close();
                        }
                    } catch (IOException ioe) {
                        System.out.println("Errore creazione ServerSocket:");
                        ioe.printStackTrace();
                        if (serverSocket != null)
                            serverSocket.close(); 
                        if(socket!=null){
                            socket.shutdownInput();
                            socket.close();
                        }
                        if(in !=null)
                            in.close();
                        if(outFile!=null) {
                            outFile.close();
                        }
                    }
                } else {
                    System.out.println("Modalità non valida");
                }
                System.out.print("\nNome directory da trasferire (EOF per terminare): ");
            }
            System.out.println("fine operazione");
        } catch (Exception e) {
            System.err.println("ClientRMI: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}