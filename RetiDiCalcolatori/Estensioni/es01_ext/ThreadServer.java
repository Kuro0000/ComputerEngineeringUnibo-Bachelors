import java.io.*;
import java.net.*;

public class ThreadServer extends Thread {
    private int porta = -1;
    private Registro registro;

    public ThreadServer(int port, Registro registro) {
        if (port <= 1024 || port > 65535) {
            System.out.println("Porta registrazione RS non valida: " + port);
            System.exit(2);
        }
        this.porta = port;
        this.registro = registro;
    }

    @Override
    public void run() {
        byte[] buf = new byte[256];
        byte[] responseData = new byte[256];
        ByteArrayInputStream biStream = null;
        DataInputStream diStream = null;
        String richiesta =null;
        String[] parti =null;
        String operazione = null;
         int risposta = -1;
           String fileName = null;
            String ip = null;
            int port = -1;
            boolean success;
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        ByteArrayOutputStream boStream = null;
        DataOutputStream doStream = null;
    
        try{
            socket = new DatagramSocket(porta);
            packet = new DatagramPacket(buf, buf.length);
        }catch(SocketException e){
	        System.out.println("Problemi nella creazione della socket: ");
			e.printStackTrace();
			System.exit(1);
        }
        try {
            System.out.println("ThreadServer registrazione RS in ascolto sulla porta: " + porta);

            while (true) {
      			try {
					packet.setData(buf);
					socket.receive(packet);

				} catch (IOException e) {
					System.err.println("Problemi nella ricezione del datagramma: "+ e.getMessage());
                    
					e.printStackTrace();
					continue;
					// il server continua a fornire il servizio ricominciando dall'inizio
					// del ciclo
				}
                try {
                    biStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    diStream = new DataInputStream(biStream);

                    richiesta = diStream.readUTF();
                    System.out.println("Richiesta registrazione da " + packet.getAddress() + ": " + richiesta);

                    parti = richiesta.split(":");
                    operazione = parti[0];
                   

                    if ("registra".equals(operazione) && parti.length == 4) {
                        fileName = parti[1];
                        ip = parti[2];
                        port = Integer.parseInt(parti[3]);

                        success = registro.setServer(new File(fileName), port, InetAddress.getByName(ip));
                        risposta = (success) ? 1 : 0;
                            try {
                                boStream = new ByteArrayOutputStream();
                                doStream = new DataOutputStream(boStream);
                                doStream.writeInt(risposta);
                                responseData = boStream.toByteArray();
					packet.setData(responseData, 0, responseData.length);

                                
                                socket.send(packet);
                                
                                System.out.println("Inviato al SwapServer: " + 0);
                                
                            } catch (IOException ex) {
                                System.err.println("Errore nell'invio risposta: " + ex.getMessage());
                            }

                        System.out.println("RS registrato: " + fileName + " su " + ip + ":" + port);
                        
                    } else if ("deregistra".equals(operazione) && parti.length == 4) {
                        fileName = parti[1];
                        ip = parti[2];
                        port = Integer.parseInt(parti[3]);

                        success = registro.removeServer(fileName);
                        risposta = (success) ? 1 : 0;
                        System.out.println("Deregistrazione RS: " + fileName + " da " + ip + ":" + port);
                        
                    } else {
                     
                            try {
                                boStream = new ByteArrayOutputStream();
                                doStream = new DataOutputStream(boStream);
                                doStream.writeInt(0);
                                responseData = boStream.toByteArray();
					packet.setData(responseData, 0, responseData.length);
                                
                                socket.send(packet);
                                
                                System.out.println("Inviato al SwapServer: " + 0);
                                
                            } catch (IOException ex) {
                                System.err.println("Errore nell'invio risposta: " + ex.getMessage());
                            }

                        System.err.println("Richiesta malformata: " + richiesta);
                    }



                }  catch (Exception e) {
                    System.err.println("Errore nella gestione della richiesta: " + e.getMessage());



                            try {
                                boStream = new ByteArrayOutputStream();
                                doStream = new DataOutputStream(boStream);
                                doStream.writeInt(0);
                                responseData = boStream.toByteArray();
					packet.setData(responseData, 0, responseData.length);

                                
                                socket.send(packet);
                                
                                System.out.println("Inviato al SwapServer: " + 0);
                                
                            } catch (IOException ex) {
                                System.err.println("Errore nell'invio risposta: " + ex.getMessage());
                            }





                }
            }
        } catch (Exception e) {
            System.err.println("Problemi nella creazione della socket: " + e.getMessage());
        }
                if (socket != null && !socket.isClosed()) 
            socket.close();

    }


}