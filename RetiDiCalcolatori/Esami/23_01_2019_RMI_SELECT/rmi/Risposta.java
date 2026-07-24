/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
import java.io.Serializable;

public class Risposta implements Serializable {
    public static final int MAX_FILES = 256;
    private Veicolo[] veicoli;
    private String errorMessage;

    public Risposta() {
        veicoli = new Veicolo[MAX_FILES]; // array statico di File!
        errorMessage = null;
    }

    public  Veicolo[] getFiles() { 
        return veicoli; 
    }
    public String getErrorMessage() { 
        return errorMessage; 
    }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
    }
}
