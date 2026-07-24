import java.io.Serializable;
/*Definisco una nuova classe in cui uso i dati che mi servono per i file anziché
 * utilizzare direttamente la classe File di java, poiché rendendola serializzabile
 * trasferirei un grafo più complesso, con questa classe minimizzo il grafo
 * da trasferire
*/
public class Dati implements Serializable {
    private String nomeFile;
    private long dimensione;
    public Dati(String nomeFile, long dimensione) {
        this.nomeFile = nomeFile;
        this.dimensione = dimensione;
    }
    // definisco metodi seguenti
    public long length() { 
        return dimensione; 
    }
    public String getName() { 
        return nomeFile; 
    }
}