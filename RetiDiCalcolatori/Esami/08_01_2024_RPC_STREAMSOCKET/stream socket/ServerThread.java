
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
        String input = null;
        int occorrenza;
        char carattere;
        boolean esito = false;
        File dir = null;
        File[] files = null;
        int occ;
        int carNum;
        char car;
        int result, i;
        long dim;
        String fileRelativePath = null;
        int cont, bufferWrite;
        BufferedReader br = null;
        FileInputStream inFile = null;
        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //iscrizione studente
                if (operazione.equals("op1")) {
                    
                    carattere =inSock.readChar();
                    System.out.println("ricevuto dal client " + carattere);

                    occorrenza = inSock.readInt();
                    System.out.println("ricevuto dal client " + occorrenza);
                    dir = new File(".");
                    files = dir.listFiles();
                    result = -1;
                    if(files != null){
                    System.out.println("aperto la directory corrente");
                        result = 0;
                        for(i = 0;i<files.length;i++){
                            if(files[i].getName().endsWith(".txt")){
                                System.out.println("elaborando il file " + files[i].getName());
                                occ = 0;
                                br = new BufferedReader(new FileReader(files[i]));
                                while((carNum = br.read())!=-1){
                                    car = (char)carNum;
                                    if(car == '\n'){
                                    if(occ>=occorrenza){
                                            result++;
                                            }
                                        occ = 0;
                                    }else if(car==carattere){
                                        occ++;
                                    }
                                }
                            }

                        }
                    
                    }
                    System.out.println("finita operazione directory");
                    outSock.writeInt(result);

                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("op2")) {
                    // DEFINIRE
                    System.out.println("ricevuto dal client operazione " + operazione);
                   
                  
                    input = inSock.readUTF();
                     System.out.println("ricevuto dal client " + input);
                    //DA DEFINIRE---------




                     dir = new File(input);

                        if (dir.exists() && dir.isDirectory()) {
                            outSock.writeInt(0);
                            System.out.println("Inviato conferma direttorio esistente: " + input);

                            files = dir.listFiles();
                            // Controllo files != null
                            if (files != null) {
                                System.out.println("File totali nel direttorio: " + files.length);

                                i = 0;
                                while(i < files.length) {
                                    if (files[i].isDirectory()) {
                                        // continue non si può usare, non faccio nulla
                                    } else if(!files[i].getName().endsWith(".txt")){
                                        dim = files[i].length();
                                        outSock.writeLong(dim);
                                        System.out.println("Inviata dimensione: " + dim);
                                        
                                        fileRelativePath = files[i].getName();
                                        outSock.writeUTF(fileRelativePath);
                                        System.out.println("Inviato nome file: " + files[i].getName() + " (" + dim + " bytes)");

                                        cont = 0;
                                        bufferWrite = -1;
                                        try {
                                            inFile = new FileInputStream(files[i]);
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
                                    }
                                    i++;
                                }
                            }
                            
                            outSock.writeLong(-1);
                            System.out.println("inviato il fine dei file della directory");
                            
                        } else {
                            outSock.writeInt(-1);
                            System.out.println("non esiste la directory");
                        }



                        //FINE------------
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