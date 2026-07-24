import java.io.*;
import java.net.*;

public class ThreadClient extends Thread {
    private int porta;
    private Registro registro;

    public ThreadClient(int port, Registro registro) {
        if (port <= 1024 || port > 65535) {
            System.out.println("Porta registrazione RS non valida: " + port);
            System.exit(2);
        }
        this.porta = port;
        this.registro = registro;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
         DatagramSocket socket = null;
     DatagramPacket packet = null;
     DataOutputStream doStream = null;
     ByteArrayOutputStream boStream = null;
     byte[] data = new byte[256]; 

        try {
            socket = new DatagramSocket(porta);
            packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("ThreadClient in ascolto sulla porta: " + porta);
        } catch (SocketException e) {
            System.err.println("Errore nella creazione della socket: " + e.getMessage());
            return;
        }

        InetAddress clientAddress = null;
        int clientPort = -1;
        int richiesta = -1;
        String  risposta = null;
        ByteArrayInputStream biStream = null;
        DataInputStream diStream = null;

        try {
            while (true) {
                try {
                    // Ricezione richiesta
                    packet.setData(buffer);
                    socket.receive(packet);
                    
                    clientAddress = packet.getAddress();
                    clientPort = packet.getPort();

                    // Estrazione richiesta
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);
                    richiesta = diStream.readInt();

                    System.out.println("Richiesta client da " + clientAddress + ":" + clientPort + " - " + richiesta);

                    // Gestione comandi
                    if (richiesta == 1) {
                        // Richiesta lista completa file disponibili
                        risposta = registro.elencoServer();
                        if(risposta == null)
                            risposta = "";
                    try {
                        boStream = new ByteArrayOutputStream();
                        doStream = new DataOutputStream(boStream);
                        doStream.writeUTF(risposta);
                        data = boStream.toByteArray();
                        packet.setData(data, 0, data.length);
                        socket.send(packet);
                    } catch (IOException ioe) {
                        System.err.println("Errore nell'invio risposta: " + ioe.getMessage());
                    }   
                    } 

                } catch (IOException e) {
                    System.err.println("Errore nella gestione richiesta: " + e.getMessage());
                    try {
                        boStream = new ByteArrayOutputStream();
                        doStream = new DataOutputStream(boStream);
                        doStream.writeUTF("");
                        data = boStream.toByteArray();
                        packet.setData(data, 0, data.length);
                        socket.send(packet);
                    } catch (IOException ioe) {
                        System.err.println("Errore nell'invio risposta: " + ioe.getMessage());
                    }                
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } 
        if (socket != null && !socket.isClosed()) 
            socket.close();        
    }

}