import java.io.*;
import java.net.*;

public class SwapServer {

    public static void main(String[] args) {
        //controllo argomenti
        if (args.length != 4) {
            System.out.println("Uso: SwapServer IPDiscoveryServer portaDiscoveryServer portaRS nomeFile");
            System.exit(1);
        }
        DatagramSocket socket = null;
        InetAddress addressDS = null;
        int portaDS = -1;
        int portaRS = -1;
        File nomeFile = null;
        String localIP = null;

        try {
            addressDS = InetAddress.getByName(args[0]);
            portaDS = Integer.parseInt(args[1]);
            portaRS = Integer.parseInt(args[2]);
            nomeFile = new File(args[3]);
        } catch (Exception e) {
            System.err.println("Errore negli argomenti: " + e.getMessage());
            System.exit(1);
        }

        // controlli del file
        if (!nomeFile.exists() || !nomeFile.isFile()) {
            System.out.println("Errore: file non trovato: " + nomeFile.getName());
            System.exit(1);
        }
        if (!nomeFile.canRead() || !nomeFile.canWrite()) {
            System.out.println("Errore: permessi insufficienti sul file: " + nomeFile.getName());
            System.exit(1);
        }
        if (!nomeFile.getName().endsWith(".txt")) {
            System.out.println("Errore: il file deve essere di testo (.txt)");
            System.exit(1);
        }

        String request = null;
        ByteArrayOutputStream boStreamReg = null;
        DataOutputStream doStreamReg = null;
        byte[] dataReg = null;
        DatagramPacket regPacket = null;
        byte[] buf = new byte[256];
        DatagramPacket packet = null;
        DataInputStream diStream = null;
        int response = -1;

        try {
            try {
                socket = new DatagramSocket(portaRS);
                socket.setSoTimeout(1000);
                packet = new DatagramPacket(buf, buf.length);
            } catch (SocketException e) {
                System.out.println("Problemi nella creazione della socket: ");
                e.printStackTrace();
                System.exit(1);
            }

            try {
                localIP = InetAddress.getLocalHost().getHostAddress();
                request = "registra:" + nomeFile.getName() + ":" + localIP + ":" + portaRS;
            } catch (UnknownHostException e) {
                System.out.println("Problemi nell-indirizzo ip: ");
                e.printStackTrace();
                System.exit(1);
            }

            

            try {
                boStreamReg = new ByteArrayOutputStream();
                doStreamReg = new DataOutputStream(boStreamReg);
                doStreamReg.writeUTF(request);
                dataReg = boStreamReg.toByteArray();
                regPacket = new DatagramPacket(dataReg, dataReg.length, addressDS, portaDS);
                socket.send(regPacket);
            } catch (IOException e) {
                System.err.println("Errore durante l'invio del pacchetto: " + e.getMessage());
            }
            System.out.println("Registrazione inviata al DiscoveryServer...");

            // Attesa conferma
            try {
                packet.setData(buf);
                socket.receive(packet);
                diStream = new DataInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                response = diStream.readInt();
                if (response==0) {
                    System.out.println("Registrazione rifiutata dal DiscoveryServer.");
                    socket.close();
                    System.exit(1);
                }
                System.out.println("Registrazione completata con successo.");
            } catch (IOException e) {
                System.err.println("Errore durante la lettura del pacchetto: " + e.getMessage());
            }

            SwapServerThread serverThread = new SwapServerThread(socket, portaRS, nomeFile);
            serverThread.start();

            System.out.println("Server attivo. Digita 'esci' per terminare.");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input = null;
            String deregistra = null;
            ByteArrayOutputStream boStreamDereg = null;
            DataOutputStream doStreamDereg = null;
            byte[] dataDereg = null;
            DatagramPacket deregPacket = null;
            //dichiaro le variabili prima del ciclo
            while ((input = reader.readLine()) != null) {
                if (input.equals("esci")) {
                    System.out.println("Comando di terminazione ricevuto...");
                    try {
                        localIP = InetAddress.getLocalHost().getHostAddress();
                        deregistra = "deregistra:" + nomeFile.getName() + ":" + localIP + ":" + portaRS;
                        
                        boStreamDereg = new ByteArrayOutputStream();
                        doStreamDereg = new DataOutputStream(boStreamDereg);
                        doStreamDereg.writeUTF(deregistra);
                        dataDereg = boStreamDereg.toByteArray();

                        deregPacket = new DatagramPacket(dataDereg, dataDereg.length, addressDS, portaDS);
                        socket.send(deregPacket);
                        System.out.println("Richiesta di deregistrazione inviata...");
                    } catch (Exception e) {
                        System.err.println("Errore durante deregistrazione: " + e.getMessage());
                    }
                    socket.close();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } 
    }
}