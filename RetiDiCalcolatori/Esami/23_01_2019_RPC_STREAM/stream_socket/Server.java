
/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.*;
import java.net.*;
public class Server {
        public static final int N = 256;

    public static void main(String[] args) throws IOException {
        int port = -1;

        try {
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    System.out.println("Usage: java FileServer [serverPort>1024]");
                    System.exit(1);
                }
            } else {
                System.out.println("Usage: java FileServer port");
                System.exit(1);
            }
        } catch (Exception e) {
            System.out.println("Problemi, i seguenti: ");
            e.printStackTrace();
            System.out.println("Usage: java FileServer port");
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
         Veicolo[] registro = null;
        registro = new Veicolo[N];
        // ED000AA corrisponde circa al 2011. > ED significa dopo il 2011.
        registro[0] = new Veicolo("AA123BB", "11111", "auto", "AA123BB_img"); // Vecchia
        registro[1] = new Veicolo("FF000ZZ", "22222", "auto", "FF000ZZ_img"); // Nuova (> ED)
        registro[2] = new Veicolo("GG123HH", "33333", "camper", "GG123HH_img"); // Nuova (> ED)
        registro[3] = new Veicolo("CC000DD", "44444", "auto", "CC000DD_img"); // Vecchia
        registro[4] = new Veicolo("EZ999ZZ", "55555", "camper", "EZ999ZZ_img"); // Nuova (> ED)
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            System.out.println("FileServer: avviato ");
            System.out.println("Server: creata la server socket: " + serverSocket);
        } catch (Exception e) {
            System.err.println("Server: problemi nella creazione della server socket: " + e.getMessage());
            e.printStackTrace();
            if (serverSocket != null) {
                serverSocket.close();
            }
            System.exit(1);
        }
        try {
            while (true) {
                System.out.println("Server: in attesa di richieste...\n");

                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Server: connessione accettata: " + clientSocket);
                } catch (Exception e) {
                    System.err.println("Server: problemi nella accettazione della connessione: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }

                try {
                    new ServerThread(clientSocket, registro, registro.length).start();
                } catch (Exception e) {
                    System.err.println("Server: problemi nel server thread: " + e.getMessage());
                    e.printStackTrace();
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Server: termino...");
            System.exit(2);
        }
    }
}