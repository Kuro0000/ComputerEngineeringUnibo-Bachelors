
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
    private Registro registro = null;

    public ServerThread(Socket clientSocket, Registro registro) {
        this.clientSocket = clientSocket;
        this.registro = registro;
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
        String coppia = null;
        int voto = -1;
        String nome = null;
        String[] token;
        String cognome = null;
        boolean esito;
        try {
            while ((operazione = inSock.readUTF()) != null) {
                System.out.println("Operazione ricevuta: " + operazione);
                //iscrizione studente
                if (operazione.equals("is")) {
                    
                    matricola = inSock.readUTF();
                    System.out.println("ricevuto dal client " + matricola);

                    nome = inSock.readUTF();
                     System.out.println("ricevuto dal client " + nome);

                    cognome = inSock.readUTF();
                    System.out.println("ricevuto dal client " + cognome);

                    esito = registro.iscriviStudente(nome, cognome, matricola);
                    if(esito == false){
                        outSock.writeUTF("fallita iscrizione");
                    }else{
                        outSock.writeUTF("iscrizione avvenuta con successo");
                    }

                    System.out.println("fine ricezione, in attesa di altre operazioni");
                } else if (operazione.equals("rv")) {
                    // DEFINIRE
                    System.out.println("ricevuto dal client operazione " + operazione);
                   
                  
                    coppia = inSock.readUTF();
                     System.out.println("ricevuto dal client " + matricola);
                     token = coppia.split(",");
                    if(token.length!=2){
                        outSock.writeUTF("formato sbagliato, deve essere coppia<matricola, voto>");
                        continue;
                    }
                    matricola = token[0].trim();
                    try{
                        voto = Integer.parseInt(token[1]);
                    }catch(NumberFormatException nfe){
                        System.out.println("Errore parsing del voto");
                        outSock.writeUTF("Errore parsing del voto");
                        continue;
                    }
                    
 
                    esito = registro.caricaVoto(matricola, voto);
                    System.out.println("caricato lato server");
                    if(esito == false){
                        outSock.writeUTF("fallita caricamento voto");
                    }else{
                        outSock.writeUTF("caricamento avvenuto con successo ");
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