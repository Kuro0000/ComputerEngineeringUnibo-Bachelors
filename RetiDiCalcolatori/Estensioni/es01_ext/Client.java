import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        InetAddress indirizzoServer = null;
        int portaServer = -1;
        String fileName = null;
        
        // controllo argomenti
        try {
            if (args.length == 2) {
                indirizzoServer = InetAddress.getByName(args[0]);
                portaServer = Integer.parseInt(args[1]);
                System.out.println("Discovery server con indirizzo: " + args[0] + "\nporta: " + portaServer);
            } else {
                System.out.println("Uso: Client IPDiscoveryServer portaDiscoveryServer");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Problemi nei parametri: ");
            e.printStackTrace();
            System.exit(1);
        }

        // Creazione socket
        DatagramSocket datagramSocket = null;
        DatagramPacket datagramPacket = null;
        byte[] buf = new byte[256]; 
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setSoTimeout(30000);
            datagramPacket = new DatagramPacket(buf, buf.length, indirizzoServer, portaServer);
            System.out.println("Client avviato");
        } catch (SocketException e) {
            System.out.println("Problemi nella creazione della socket: ");
            e.printStackTrace();
            System.exit(1);
        }

        ByteArrayOutputStream boStream = null;
        DataOutputStream doStream = null;
        String risposta = null;
        ByteArrayInputStream biStream = null;
        DataInputStream diStream = null;
        byte[] data = null;
        String[] files = null;

        try {
            boStream = new ByteArrayOutputStream();
            doStream = new DataOutputStream(boStream);
            doStream.writeInt(1); // Comando per richiedere lista
            data = boStream.toByteArray();
            datagramPacket.setData(data);
            datagramSocket.send(datagramPacket);
            System.out.println("Richiesta lista file inviata");
        } catch (IOException e) {
            System.out.println("Problemi nell'invio della richiesta lista: ");
            e.printStackTrace();
            System.exit(1);
        }

        // Ricezione lista file
        try {
            datagramPacket.setData(buf);
            datagramSocket.receive(datagramPacket);
        } catch (IOException e) {
            System.out.println("Problemi nella ricezione della lista file: ");
            e.printStackTrace();
            System.exit(1);
        }

        // Estrazione lista file
        try {
            biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
            diStream = new DataInputStream(biStream);
            risposta = diStream.readUTF();
            System.out.println("Lista file ricevuta");
        } catch (IOException e) {
            System.out.println("Problemi nella lettura della lista file: ");
            e.printStackTrace();
            System.exit(1);
        }

        
        if (risposta == null || risposta.isEmpty()) {
            System.out.println("Nessun file disponibile sul DiscoveryServer");
            datagramSocket.close();
            System.exit(1);
        }

        // Mostra lista file all'utente
        System.out.println("\nfile disponibili:");
        files = risposta.split(";");
        String[] info= null;
        for (int i = 0; i < files.length; i++) {
            info = files[i].split(":");
            if (info.length >= 3) {
                System.out.println((i + 1) + ". " + info[0] + " [Server: " + info[1] + ":" + info[2] + "]");
            }
        }

        // Selezione file
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        int scelta = -1;
        String input = null;
            System.out.print("\nSeleziona il numero del file (1-" + files.length + "): ");
            try {
                input = stdIn.readLine();
                if (input == null) {
                    System.out.println("Input terminato");
                    datagramSocket.close();
                    System.exit(0);
                }
                scelta = Integer.parseInt(input);
                if (!(scelta >= 1 && scelta <= files.length)) {
                    System.out.println("Selezione non valida. Inserisci un numero tra 1 e " + files.length);
                }
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero valido");
                datagramSocket.close();
 
                System.exit(1);
            } catch (IOException e) {
                System.out.println("Errore lettura input: " + e.getMessage());
                datagramSocket.close();

                System.exit(1);
            }
                
        String[] token = null;
        String serverIP = null;
        int portaSwapRow = -1;
        InetAddress ipSwapRow = null;
         String linea1 = null, linea2 = null, numRighe = null;
        int esito = -2;

        // Estrai informazioni del file selezionato
    
        token = files[scelta - 1].split(":");
        fileName = token[0];
        serverIP = token[1];

        try {
            ipSwapRow = InetAddress.getByName(serverIP);
            portaSwapRow =Integer.parseInt(token[2]);
        } catch (UnknownHostException e) {
            System.out.println("Errore indirizzo server swap: " + serverIP);
            e.printStackTrace();
            datagramSocket.close();

            System.exit(1);
        }catch(NumberFormatException e){
               System.out.println("Errore porta server swap: " + serverIP);
            e.printStackTrace();
            datagramSocket.close();
  
            System.exit(1);
        }

        System.out.println("Selezionato: " + fileName + " su " + serverIP + ":" + portaSwapRow);

        //  Comunicazione con SwapServer
        datagramPacket = new DatagramPacket(buf, buf.length, ipSwapRow, portaSwapRow);
        
     

        
        System.out.println("\nInserisci i numeri delle righe da scambiare (Ctrl+D per uscire)");
        System.out.println("Formato: prima riga <invio> seconda riga <invio>");

        try {
			System.out.print("Prima riga: ");

            while ((linea1 = stdIn.readLine())!=null) {

                System.out.print("Seconda riga: ");
                linea2 = stdIn.readLine();

                numRighe = linea1 + "-" + linea2;
                //controllo se linea1 e linea2 sono validi
                //controlliamo qua così evitiamo che li controlli il server
                try{
                    Integer.parseInt(linea1);
                    Integer.parseInt(linea2);
                }catch(NumberFormatException e){
                        System.out.println("Errore, inserire linee da scambiare valide");
                        continue;
                }
                try {
                    boStream = new ByteArrayOutputStream();
                    doStream = new DataOutputStream(boStream);
                    doStream.writeUTF(numRighe);
                    data = boStream.toByteArray();
                    datagramPacket.setData(data);
                    datagramSocket.send(datagramPacket);
                    System.out.println("Richiesta swap inviata");
                } catch (IOException e) {
                    System.out.println("Problemi nell'invio della richiesta: ");
                    e.printStackTrace();
                    continue;
                }

                try {
                    datagramPacket.setData(buf);
                    datagramSocket.receive(datagramPacket);
                } catch (IOException e) {
                    System.out.println("Problemi nella ricezione del datagramma: ");
                    e.printStackTrace();
                    continue;
                }

                try {
                    biStream = new ByteArrayInputStream(datagramPacket.getData(), 0, datagramPacket.getLength());
                    diStream = new DataInputStream(biStream);
                    esito = diStream.readInt();
                    System.out.println("Esisto dello scambio: " + esito);
                    
                } catch (IOException e) {
                    System.out.println("Problemi nella lettura della risposta: ");
                    e.printStackTrace();
                    continue;
                }
                System.out.println("Inserisci prima riga da scambiare contenuta nel file " + fileName + " oppure Ctrl+D(Unix)/Ctrl+Z(Win)+invio per uscire");

            }
        } catch (IOException e) {
            System.out.println("Errore lettura input: " + e.getMessage());
        } 
            System.out.println("Client terminato");
            datagramSocket.close();

        
    }
}