import java.io.*;
import java.net.*;
import java.util.Arrays; // Permesso solo per stampe debug o toString, non per liste dinamiche

public class UtilityExam {

    /* DEFINIZIONE CLASSE DATI (Equivalente struct C) */
    /* Deve essere statica per essere usata nel main senza istanza della classe esterna */
    static class Elemento {
        String nome;
        int valore;
        
        /* Costruttore per comodità nell'esempio, all'esame puoi inizializzare i campi a mano */
        public Elemento(String n, int v) {
            this.nome = n;
            this.valore = v;
        }
    }

    public static void main(String[] args) {
        /* ------------------------------------------------------------
           AREA DICHIARAZIONE VARIABILI (TUTTE QUI OBBLIGATORIAMENTE)
           ------------------------------------------------------------ */
        
        /* Costanti */
        final int MAX_ELEM = 100;
        final int R = 3;
        final int C = 3;

        /* Strutture Dati */
        Elemento[] lista;
        Elemento tempElem;
        char[][] matrice;
        String[] partiComando; // Per lo split della stringa

        /* Variabili primitive e contatori */
        int i, j, k;
        int numElementiEffettivi;
        int indexToRemove, trovatoIdx;
        int left, right, mid;
        int argValore;
        boolean ordinato, flag, vittoria;
        
        /* Stringhe di appoggio */
        String inputString;
        String keyString;
        char giocatoreCorrente;

        /* ------------------------------------------------------------
           INIZIALIZZAZIONE (Obbligatoria prima dell'uso)
           ------------------------------------------------------------ */
        
        lista = new Elemento[MAX_ELEM]; // Crea array di null
        matrice = new char[R][C];
        numElementiEffettivi = 0;
        
        /* Riempimento fittizio (simulate input) */
        lista[0] = new Elemento("z_file.txt", 100);
        lista[1] = new Elemento("a_file.txt", 50);
        lista[2] = new Elemento("m_file.txt", 200);
        lista[3] = new Elemento("c_file.txt", 10);
        lista[4] = new Elemento("b_file.txt", 150);
        
        /* Importante: il resto dell'array è NULL */
        for(i = 5; i < MAX_ELEM; i++) {
            lista[i] = null;
        }
        numElementiEffettivi = 5;


        /* ============================================================
           1. BUBBLE SORT (Ordinamento Array di Oggetti)
           Note: 
           - Uso compareTo per le stringhe
           - Gestisco il ciclo con flag 'ordinato' per evitare break
           ============================================================ */
        System.out.println("\n--- 1. BUBBLE SORT (per nome) ---");
        
        i = 0;
        ordinato = false; 

        while (i < numElementiEffettivi - 1 && !ordinato) {
            ordinato = true; // Assumo sia ordinato
            j = 0;
            while (j < numElementiEffettivi - 1 - i) {
                /* Confronto: > 0 significa che lista[j] è "maggiore" (viene dopo) */
                if (lista[j].nome.compareTo(lista[j+1].nome) > 0) {
                    /* Swap */
                    tempElem = lista[j];
                    lista[j] = lista[j+1];
                    lista[j+1] = tempElem;
                    ordinato = false; // Trovato disordine, continuo
                }
                j++;
            }
            i++;
        }

        /* Stampa verifica */
        i = 0;
        while(i < numElementiEffettivi) {
            System.out.println(lista[i].nome + " - " + lista[i].valore);
            i++;
        }


        /* ============================================================
           2. BINARY SEARCH (Ricerca Binaria)
           Pre-requisito: Array ordinato (fatto sopra)
           ============================================================ */
        System.out.println("\n--- 2. BINARY SEARCH (Cerco 'm_file.txt') ---");

        keyString = "m_file.txt";
        left = 0;
        right = numElementiEffettivi - 1;
        trovatoIdx = -1;

        while (left <= right && trovatoIdx == -1) {
            mid = left + (right - left) / 2;
            
            /* compareTo restituisce 0 se uguali */
            if (lista[mid].nome.compareTo(keyString) == 0) {
                trovatoIdx = mid;
            } else {
                /* Se keyString è "minore" (viene prima), cerco a sinistra */
                if (keyString.compareTo(lista[mid].nome) < 0) {
                    right = mid - 1;
                } else {
                    left = mid + 1;
                }
            }
        }

        if (trovatoIdx != -1) System.out.println("Trovato all'indice: " + trovatoIdx);
        else System.out.println("Non trovato.");


        /* ============================================================
           3. RIMOZIONE CON SHIFT (Compattamento Array)
           Evita i 'null' in mezzo all'array. 
           Esempio: Rimuovo 'c_file.txt'
           ============================================================ */
        System.out.println("\n--- 3. ELIMINAZIONE 'c_file.txt' CON SHIFT ---");

        keyString = "c_file.txt";
        indexToRemove = -1;
        
        /* A. Ricerca Lineare */
        i = 0;
        while (i < numElementiEffettivi && indexToRemove == -1) {
            if (lista[i].nome.equals(keyString)) {
                indexToRemove = i;
            }
            i++;
        }

        /* B. Shift e Pulizia */
        if (indexToRemove != -1) {
            j = indexToRemove;
            /* Sposto elementi da destra a sinistra */
            while (j < numElementiEffettivi - 1) {
                lista[j] = lista[j+1];
                j++;
            }
            /* Fondamentale in Java: annullo l'ultimo elemento duplicato */
            lista[numElementiEffettivi - 1] = null;
            numElementiEffettivi--;
            
            System.out.println("Eliminato. Nuova dimensione: " + numElementiEffettivi);
        } else {
            System.out.println("Elemento non trovato.");
        }


        /* ============================================================
           4. PARSING COMANDI (String Split & ParseInt)
           Utile per protocolli testuali
           ============================================================ */
        System.out.println("\n--- 4. PARSING COMANDO ---");

        inputString = "PUT foto.jpg 1024";
        
        /* Split in base agli spazi bianchi (regex \\s+) */
        partiComando = inputString.split("\\s+");

        /* Controllo lunghezza e contenuto */
        if (partiComando.length == 3 && partiComando[0].equals("PUT")) {
            try {
                /* Parsing intero sicuro */
                argValore = Integer.parseInt(partiComando[2]);
                System.out.println("Cmd: " + partiComando[0]);
                System.out.println("File: " + partiComando[1]);
                System.out.println("Dim: " + argValore);
            } catch (NumberFormatException e) {
                System.out.println("Errore: il terzo argomento non è un numero.");
            }
        } else {
            System.out.println("Formato comando errato.");
        }


        /* ============================================================
           5. MATRICI E VITTORIA (Tris / Check Griglia)
           ============================================================ */
        System.out.println("\n--- 5. CHECK MATRICE ---");

        /* Init matrice vuota */
        i = 0;
        while(i < R) {
            j = 0;
            while(j < C) {
                matrice[i][j] = ' ';
                j++;
            }
            i++;
        }
        /* Setup vincente diagonale */
        matrice[0][0] = 'X'; matrice[1][1] = 'X'; matrice[2][2] = 'X';

        giocatoreCorrente = 'X';
        vittoria = false;

        /* Controllo RIGHE */
        i = 0;
        while (i < R && !vittoria) {
            if (matrice[i][0] == giocatoreCorrente && 
                matrice[i][1] == giocatoreCorrente && 
                matrice[i][2] == giocatoreCorrente) {
                vittoria = true;
            }
            i++;
        }

        /* Controllo COLONNE (solo se non ho già vinto) */
        j = 0;
        while (j < C && !vittoria) {
            if (matrice[0][j] == giocatoreCorrente && 
                matrice[1][j] == giocatoreCorrente && 
                matrice[2][j] == giocatoreCorrente) {
                vittoria = true;
            }
            j++;
        }

        /* Controllo DIAGONALI */
        if (!vittoria) {
            if (matrice[0][0] == giocatoreCorrente && 
                matrice[1][1] == giocatoreCorrente && 
                matrice[2][2] == giocatoreCorrente) {
                vittoria = true;
            }
        }
        if (!vittoria) {
            if (matrice[0][2] == giocatoreCorrente && 
                matrice[1][1] == giocatoreCorrente && 
                matrice[2][0] == giocatoreCorrente) {
                vittoria = true;
            }
        }

        if (vittoria) System.out.println("Vittoria per: " + giocatoreCorrente);
        else System.out.println("Nessuna vittoria.");


        /* ============================================================
           6. AGGIUNTA IN PRIMO SLOT LIBERO (Gestione array 'sporchi')
           Utile se non usi lo shift ma lasci i buchi (null)
           ============================================================ */
        System.out.println("\n--- 6. RICERCA SLOT NULL ---");
        
        /* Creo un buco artificiale */
        lista[1] = null; 

        trovatoIdx = -1;
        i = 0;
        while (i < MAX_ELEM && trovatoIdx == -1) {
            if (lista[i] == null) {
                trovatoIdx = i;
            }
            i++;
        }

        if (trovatoIdx != -1) {
            System.out.println("Primo slot libero a indice: " + trovatoIdx);
            /* Qui faresti: lista[trovatoIdx] = new Elemento(...); */
        } else {
            System.out.println("Array pieno.");
        }
    }
}