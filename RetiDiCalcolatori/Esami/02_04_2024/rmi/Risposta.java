/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.Serializable;

public class Risposta implements Serializable {
    public static final int MAX_FILES = 256;
    private String[] files;
    private String errorMessage;

    public Risposta() {
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
