
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
    private Veicolo[] registro = null;
    private int dimensioneLogica = -1;
    public ServerThread(Socket clientSocket, Veicolo[] registro, int dimensioneLogica) {
        this.clientSocket = clientSocket;
        this.registro = registro;
        this.dimensioneLogica = dimensioneLogica;
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
        String targa = null;
        String fileRelativePath = null;
        String targaDaEliminare = null;
        int i, j, cont, result, bufferWrite;
        boolean trovato;
        FileInputStream inFile= null;
        File[] files = null;
        long dim;
        File dir = null;
        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //iscrizione studente
                if (operazione.equals("is")) {
                    
                        targa = inSock.readUTF();
                        System.out.println("Richiesto foto: " + targa);

                        dir = new File(".");

                        if (dir!=null) {
                            outSock.writeInt(0);
                            files = dir.listFiles();
                            // Controllo files != null
                            if (files != null) {
                                System.out.println("File totali nel direttorio: " + files.length);

                                i = 0;
                                while(i < files.length) {
                                    if (files[i].isDirectory() || !files[i].getName().startsWith(targa)) {
                                        // continue non si può usare, non faccio nulla
                                    } else {
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

                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("el")) {
                    // DEFINIRE
                    System.out.println("ricevuto dal client operazione " + operazione);
                   
                  
                    targaDaEliminare = inSock.readUTF();
                    i = 0;
                    result = -1;
                    trovato = false;
                    // ALGORITMO DI RICERCA
                    synchronized(registro){
                    while (i < dimensioneLogica && !trovato) {
                        if (registro[i] != null && registro[i].getTarga().equals(targaDaEliminare)) {
                            trovato = true;
                            // Non incrementiamo i, ci serve l'indice per lo shift
                        } else {
                            i++;
                        }
                    }

                    // ALGORITMO DI RIMOZIONE (SHIFT)
                    if (trovato) {
                        j = i;
                        while (j < dimensioneLogica - 1) {
                            registro[j] = registro[j + 1];
                            j++;
                        }
                        // Pulizia ultimo elemento e decremento dimensione
                        registro[dimensioneLogica - 1] = null;
                        dimensioneLogica--;
                      result = 1; // Successo

                    }
                        outSock.writeInt(result);
                        System.out.println("Server: Prenotazione con targa " + targaDaEliminare + " rimosso.");
                    }
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