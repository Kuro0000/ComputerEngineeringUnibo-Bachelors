
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
    private Prenotazione[][] registro;
    public ServerThread(Socket clientSocket, Prenotazione[][] rg) {
        this.clientSocket = clientSocket;
        this.registro = rg;
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
        String matricola = null;
        boolean esito = false;
        int resRow, resCol;
        char car;
        int occ;
        int check;
        boolean flag;
        int i,j, cont, bufferWrite;
        String fileRelativePath = null;
        long dim;
        File dir = null;;
        File[] files = null;
        FileInputStream inFile = null;
        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //iscrizione studente
                if (operazione.equals("op1")) {
                    
                    matricola = inSock.readUTF();
                    System.out.println("Cerco vicino per matricola: " + matricola);
                    
                    resRow = -1;
                    resCol = -1;
                    flag = false;
                    
                    // Scansione Matrice
                    i = 0;
                    while (i < registro.length && !flag) {
                        j = 0;
                        while (j < registro[i].length && !flag) {
                            
                            // Se trovo la matricola dell'utente esistente
                            if (registro[i][j].getMatricola().equals(matricola)) {
                                
                                // Controllo se esiste la colonna a destra
                                if (j + 1 < registro[i].length) {
                                    // Controllo se è libero
                                    if (registro[i][j+1].getId().equals("L")) {
                                        // Prenoto
                                        registro[i][j+1].prenota("OSPITE", "GUEST");
                                        resRow = i;
                                        resCol = j + 1;
                                        flag = true;
                                    }
                                }
                            }
                            j++;
                        }
                        i++;
                    }
                    // Invio coordinate (-1, -1 se fallito)
                    outSock.writeInt(resRow);
                    outSock.writeInt(resCol);
                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("op2")) {
                    // DEFINIRE
                    System.out.println("ricevuto dal client operazione " + operazione);
                   
                  
                    car = inSock.readChar();
                     System.out.println("ricevuto dal client " + car);

                     occ = inSock.readInt();
                    //DA DEFINIRE--------------


                        dir = new File("<matricola>_img");

                        if (dir.exists() && dir.isDirectory()) {
                            outSock.writeInt(0);
                            System.out.println("Inviato conferma direttorio esistente: <matricola>_img");

                            files = dir.listFiles();
                            // Controllo files != null
                            if (files != null) {
                                System.out.println("File totali nel direttorio: " + files.length);

                                i = 0;
                                flag = false;
                                while(i < files.length) {
                                    if (files[i].isDirectory()) {
                                        // continue non si può usare, non faccio nulla
                                    } else {
                                        fileRelativePath = files[i].getName();
                                        check = 0;
                                        if(!fileRelativePath.endsWith(".txt")){
                                                
                                            for(int k = 0;k<fileRelativePath.length();k++){
                                                if(fileRelativePath.charAt(k)==car){
                                                    check++;
                                                }
                                            }
                                            if(check>occ){

                                                dim = files[i].length();
                                                outSock.writeLong(dim);
                                                System.out.println("Inviata dimensione: " + dim);
                                                
                                                
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