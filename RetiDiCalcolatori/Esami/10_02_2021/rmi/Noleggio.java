import java.io.Serializable;

public class Noleggio implements Serializable {
    
    /* DICHIARAZIONE VARIABILI (Tutte private) */
    private String id;
    private int giorno;
    private int mese;
    private int anno;
    private int durata;    // Giorni di noleggio
    private String modello;
    private int costo;     // Uso int per semplicità col -1, se serve double usa -1.0
    private String nomeFile;

    /* COSTRUTTORE 1: DEFAULT / VUOTO 
       Crea lo sci "invisibile" o "libero" con i valori sentinella richiesti:
       L -1/-1/-1 -1 -1 -1 L
    */
    public Noleggio() {
        this.id = "L";
        this.giorno = -1;
        this.mese = -1;
        this.anno = -1;
        this.durata = -1;
        this.modello = "L";
        this.costo = -1;
        this.nomeFile = "L";
    }

    /* COSTRUTTORE 2: INIZIALIZZAZIONE REALE
       Usalo per i 5 sci che inserisci manualmente nel codice.
       Nota: inizializza la data a -1 perché all'inizio non sono noleggiati.
    */
    public Noleggio(String id, String modello, int costo, String nomeFile) {
        this.id = id;
        this.modello = modello;
        this.costo = costo;
        this.nomeFile = nomeFile;
        
        // Di default non è noleggiato
        this.giorno = -1;
        this.mese = -1;
        this.anno = -1;
        this.durata = -1;
    }
    
    /* COSTRUTTORE 3: COMPLETO (Se serve caricare uno sci già noleggiato) */
    public Noleggio(String id, int g, int m, int a, int d, String mod, int c, String f) {
        this.id = id;
        this.giorno = g;
        this.mese = m;
        this.anno = a;
        this.durata = d;
        this.modello = mod;
        this.costo = c;
        this.nomeFile = f;
    }

    /* GETTERS E SETTERS */
    // Servono per leggere e modificare i dati dal Server
    
    public String getId() { return id; }
    public String getModello() { return modello; }
    public String getNomeFile() { return nomeFile; }
    public int getDurata() { return durata; }
    
    // Metodo per impostare il noleggio
    public void setNoleggio(int g, int m, int a, int d) {
        this.giorno = g;
        this.mese = m;
        this.anno = a;
        this.durata = d;
    }
    
    
    // Controlla se è "vuoto" nel senso di slot array non usato
    public boolean isLibero() {
        return durata==-1;
    }
}