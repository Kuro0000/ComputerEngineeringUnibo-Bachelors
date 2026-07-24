import java.io.File;
import java.net.InetAddress;

public class Registro {
    /*classe creata per gestire il registro dei server, poiché potrebbero accadere problemi di 
     * interferenza dei dati, sincronizziamo la scrittura
     */
    private static final int MAX_DIM = 10;
    private File[] files;
    private int[] ports;
    private InetAddress[] ips;
    private int count, i;

    public Registro() {
        files = new File[MAX_DIM];
        ports = new int[MAX_DIM];
        ips = new InetAddress[MAX_DIM];
        count = 0;
    }



    public synchronized boolean setServer(File file, int port, InetAddress ip) {
        if (count >= MAX_DIM) {
            System.out.println("Tabella piena: impossibile registrare altri RS");
            return false;
        }

        // Controlla duplicati
        for (i = 0; i < count; i++) {
            if (files[i] != null && 
                (file.getName().equals(files[i].getName()) || 
                (ip.equals(ips[i]) && port == ports[i]))) {
                    System.out.println("Errore: file già registrato");
                    return false;
                }
            
        }

        files[count] = file;
        ips[count] = ip;
        ports[count] = port;
        count++;

        System.out.println("Registrato RS: " + file.getName() + " -> " + ip.getHostAddress() + ":" + port);
        return true;
    }
    public synchronized boolean removeServer(String nomeFile) {
        for (i = 0; i < count; i++) {
            if (files[i] != null && nomeFile.equals(files[i].getName())) {
                // Sposta l'ultimo elemento nella posizione i
                files[i] = files[count - 1];
                ips[i] = ips[count - 1];
                ports[i] = ports[count - 1];
                files[count - 1] = null;
                count--;
                System.out.println("RS rimosso: " + nomeFile);
                return true;
            }
        }
        return false;
    }

        public String elencoServer() {
            if (count == 0) return null;//nessun file disponibile
            
            StringBuilder sb = new StringBuilder();
            for (i = 0; i < count; i++) {
                if (i > 0) sb.append(";");
                sb.append(files[i].getName())
                .append(":")
                .append(ips[i].getHostAddress())
                .append(":")
                .append(ports[i]);
            }
            return sb.toString();
        }
}
