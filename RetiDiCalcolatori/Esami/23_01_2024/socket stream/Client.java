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
        String matricola = null;
        char car;
        int resRow, resCol;
        int occorrenze;
        int dirEsiste;
        File file = null;
        String relativePath = null;
        FileOutputStream outFile = null;;
        long dim;
        int cont, bufferRead;
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(
            "Client Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti operazione op1 per oppure op2: "
        );

        try {
            while ((operazione = stdIn.readLine()) != null) {
                 outSock.writeUTF(operazione);

                //Iscrizione studente----------------------------------------
                if (operazione.equals("op1")) {
                    System.out.println("Inserire matricola");
                    matricola = stdIn.readLine();
                    if(matricola.trim().isEmpty()){
                        System.out.println("inserire matricola valida");
                        continue;
                    }
                     outSock.writeUTF(matricola);
                     resRow = inSock.readInt();
                     resCol = inSock.readInt();
                    System.out.println("operazione completato " + resRow + " e " + resCol);

                    //registra voto -----------------------------------------------
                } else if (operazione.equals("op2")) {
                    car =  (char)stdIn.read();
                     stdIn.readLine();
                  
                    try{
                        occorrenze = Integer.parseInt(stdIn.readLine());
                    }catch(NumberFormatException nfe){
                        continue;
                    }
                     //DEFINIRE ALTRI INVII
                    outSock.writeChar(car);
                    outSock.writeInt(occorrenze);

   
                    //INIZIO-------

                    // attendo e controllo se esiste nel lato server
                    dirEsiste = inSock.readInt();
                    
                    if (dirEsiste != 0) {
                        System.out.println("direttorio non esistente nel lato server " + dirEsiste);
                        // Niente continue, uso else per saltare
                    } else {
                        while ((dim = inSock.readLong()) != -1) {
                            System.out.println("Dimensione ricevuto: " + dim);
                            relativePath = inSock.readUTF();
                            
                            file = new File( relativePath);

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






                    //FINE-------






                    System.out.println("operazione completato " );
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