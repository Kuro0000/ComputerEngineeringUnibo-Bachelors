import java.io.Serializable;

public class Result implements Serializable {
    public static final int MAX_FILES = 256;
    private int serverPort;
    private Dati[] files;
    /*Definisco una nuova classe in cui uso i dati che mi servono per i file anziché
    * utilizzare direttamente la classe File di java, poiché rendendola serializzabile
    * trasferirei un grafo più complesso, con questa classe minimizzo il grafo
    * da trasferire
    */
    private String errorMessage;

    public Result() {
        serverPort = -1;
        files = new Dati[MAX_FILES]; // array statico di File!
        errorMessage = null;
    }
  
    public  int getServerPort() { 
        return serverPort; 
    }
    public  Dati[] getFiles() { 
        return files; 
    }
    public String getErrorMessage() { 
        return errorMessage; 
    }

    public  void setServerPort(int serverPort) { 
        this.serverPort = serverPort; 
    }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
    }
}
