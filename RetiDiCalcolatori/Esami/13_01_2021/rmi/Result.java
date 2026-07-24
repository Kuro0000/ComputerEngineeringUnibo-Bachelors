/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.Serializable;

public class Result implements Serializable {
    public static final int MAX_FILES = 6;
    private String[] files;
    /*Definisco una nuova classe in cui uso i dati che mi servono per i file anziché
    * utilizzare direttamente la classe File di java, poiché rendendola serializzabile
    * trasferirei un grafo più complesso, con questa classe minimizzo il grafo
    * da trasferire
    */
    private String errorMessage;

    public Result() {
        files = new String[MAX_FILES]; // array statico di File!
        errorMessage = null;
    }
  
    public  String[] getFiles() { 
        return files; 
    }
    public String getErrorMessage() { 
        return errorMessage; 
    }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
    }
}
