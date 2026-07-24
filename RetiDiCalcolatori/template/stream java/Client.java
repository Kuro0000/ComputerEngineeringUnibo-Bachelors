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
        //dichiarazione delle variabili prima del ciclo
        BufferedReader stdIn = null;
        String operazione = null;
        String input = null;
        String res = null;
        stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(
            "Client Started.\n\n^D(Unix)/^Z(Win)+invio per uscire, oppure immetti operazione op1 per oppure op2: "
        );

        try {
            while ((operazione = stdIn.readLine()) != null) {
                 outSock.writeUTF(operazione);

                if (operazione.equals("op1")) {
                    System.out.println("Inserire input");
                     input = stdIn.readLine();
                    //DEFINIRE ALTRI INVII



                    System.out.println("operazione completato " + res);

                } else if (operazione.equals("op2")) {
                    input =  stdIn.readLine();
                    outSock.writeUTF(input);
                     //DEFINIRE ALTRI INVII




                    res = inSock.readUTF();
   

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