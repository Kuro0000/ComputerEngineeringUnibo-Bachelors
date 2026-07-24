/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.net.*;
import java.io.*;

public class Client {



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
        int dirEsiste;
        String nomeFile = null;
        String direttorio = null;
        String parola = null;
        long soglia;
        int res;
        String relativePath = null;
        FileOutputStream outFile = null;
        int cont, bufferRead;
        File file = null;
        long dim;
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Client Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti operazione: ");

        try {
            while ((operazione = stdIn.readLine()) != null) {
                 outSock.writeUTF(operazione);

                //Iscrizione studente----------------------------------------
                if (operazione.equals("is")) {
                    System.out.println("Inserire il nome di un direttorio");
                     direttorio = stdIn.readLine();
                     if(direttorio.trim().isEmpty()){
                        System.out.println("inserire direttorio valido\n");
                        continue;
                     }
                    System.out.println("Inserire la soglia di un file");
                     try{
                    soglia = Long.parseLong(stdIn.readLine());
                     }catch(NumberFormatException nfe){
                        System.out.println("inserire soglia valida\n");
                        continue;
                     }
                    outSock.writeUTF(direttorio);
                    outSock.writeLong(soglia);
                    // attendo e controllo se esiste nel lato server
                    System.out.println("in attesa di procedere");

                    dirEsiste = inSock.readInt();
                    
                    if (dirEsiste == -1) {
                        System.out.println("direttorio non esistente nel lato server");
                        // Niente continue, uso else per saltare
                    } else {
                       System.out.println("direttorio esistente, inizio trasferimento");

                        
                        while ((dim = inSock.readLong()) != -1) {
                            System.out.println("Dimensione ricevuto: " + dim);
                            relativePath = inSock.readUTF();
                            
                            file = new File(relativePath);

                            try {
                                outFile = new FileOutputStream(file);
                                cont = 0;
                                // **RICEVI IL FILE**
                                while (cont < dim && (bufferRead = inSock.read()) >= 0) {
                                    outFile.write(bufferRead);
                                    cont++;
                                }
                                outFile.flush();
                                outFile.close();
                                System.out.println(" File ricevuto: " + file.getName() + " " + cont + " bytes");
                                
                            } catch (Exception e) {
                                System.out.println("Problemi nel salvataggio: ");
                                e.printStackTrace();
                            }
                            // outFile chiuso nel try
                        }
                        System.out.println("Trasferimento directory completato");
                    }
                    // --- FINE SNIPPET ---



                    System.out.println("operazione completato "  );

                    //registra voto -----------------------------------------------
                } else if (operazione.equals("el")) {
                    System.out.println("Inserire il nome del file");
                     nomeFile = stdIn.readLine();
                    
                    if(nomeFile.trim().isEmpty()){
                        System.out.println("inserire nome file valido\n");
                        continue;
                     }
                    System.out.println("Inserire la parola da eliminare del file");
                    parola = stdIn.readLine();
                    
                    if(parola.trim().isEmpty()){
                        System.out.println("inserire parola valido\n");
                        continue;
                     }
                    outSock.writeUTF(nomeFile);
                    outSock.writeUTF(parola);
                    System.out.println("in attesa di risposta\n");
                    res = inSock.readInt();
   

                    System.out.println("operazione completato " + res);
                }else{
                     System.out.println("operazione non supportata");

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


 

}