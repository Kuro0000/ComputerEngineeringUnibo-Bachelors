                                                                                                                                                                                                                                                                                                                                                                                                                             import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Produttore {
    /*specifiche:
    Il produttore non chiede all’utente quante righe scrivere, ma è un
    programma sequenziale che legge fino a quando l’utente immette
    EOF (end of file) per terminare l’inserimento (è un filtro)
    Ciascuna riga letta comincia con un prefisso composto da un intero
    seguito dal separatore ‘:’; tale intero indica all’interno di quale file
    andrà scritto (in append) il contenuto della riga letta ad esempio:
    >1:Questa riga andra’ in fileName1.txt
    // in fileName1.txt, che assumiamo essere il primo argomento
    >3:Questa riga andra’ in fileName3.txt
    // in fileName3.txt, che assumiamo essere il terzo argomento
    >1:Questa riga andra’ in fileName1.txt
    …
    Una volta terminato il ciclo di letture il produttore chiude tutti i file
    aperti e libera tutte le risorse occupate
     */
  
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Errore: numero di argomenti insufficienti");
            System.exit(1);
        }
        int numFiles, i;
        numFiles = args.length;
        String filename = null;
        PrintWriter[] writers = new PrintWriter[numFiles];
        //dichiaro le variabili prima di entrare dentro il ciclo for
        
        // Apri tutti i file in modalità append
        try {
            for (i = 0; i < numFiles; i++) {
                filename = args[i];
                
                // Controllo se il file è un .txt
                if (!filename.endsWith(".txt")) {
                    System.err.println("Errore: " + filename + " non è un file .txt valido.");
                    System.exit(1);
                }
                // Apri il file in modalità append
                writers[i] = new PrintWriter(new FileWriter(filename, true));
            }

            BufferedReader stdIn = null;
            int indice, sepIndex;
            String testo, indiceStr, riga;
            indice =-1;
            sepIndex = -1;
            testo = null;
            indiceStr = null;
            riga = null;
            //dichiaro le variabili prima di entrare nel ciclo while

            System.out.println("Scrivere le righe da inserire (CTRL+D o CTRL+Z per terminare):");
		    stdIn = new BufferedReader(new InputStreamReader(System.in));
     
            while ((riga = stdIn.readLine())!=null) {
                
                // Trova il separatore ':'
                sepIndex = riga.indexOf(':');
                if (sepIndex == -1) {
                    System.err.println("Formato errato: manca il separatore ':' in: " + riga);
                    for (i = 0;i<writers.length;i++) {
                        if (writers[i] != null) {
                            writers[i].close();
                        }
                    }
                    System.exit(1);
                }
                
                // Estrai indice numerico
                indiceStr = riga.substring(0, sepIndex);
                
                // Verifica che sia numerico
                if (!indiceStr.matches("\\d+")) {
                    System.err.println("Formato errato: prima del separatore deve esserci un indice numerico in: " + riga);
                    for (i = 0;i<writers.length;i++) {
                        if (writers[i] != null) {
                            writers[i].close();
                        }
                    }
                    System.exit(1);
                }
                try{
                indice = Integer.parseInt(indiceStr);
                }catch(NumberFormatException e){
                    System.err.println("Formato errato: prima del separatore deve esserci un indice numerico in: " + riga);
                    for (i = 0;i<writers.length;i++) {
                        if (writers[i] != null) {
                            writers[i].close();
                        }
                    }
                    System.exit(1);
                }
                testo = riga.substring(sepIndex + 1);
                
                if (indice < 1 || indice > numFiles) {
                    System.err.println("Indice file non valido: " + indice);
                    for (i = 0;i<writers.length;i++) {
                        if (writers[i] != null) {
                            writers[i].close();
                        }
                    }
                    System.exit(1);
                }
                
                // Scrivi nel file appropriato
                writers[indice-1].append(testo+System.lineSeparator());
                writers[indice-1].flush();
            }
                        
        } catch (IOException e) {
            System.err.println("Errore I/O: " + e.getMessage());
            for (i = 0;i<writers.length;i++) {
                if (writers[i] != null) {
                    writers[i].close();
                }
            }
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Errore formato numero: " + e.getMessage());
            for (i = 0;i<writers.length;i++) {
                if (writers[i] != null) {
                    writers[i].close();
                }
            }
            System.exit(1);
        }
        
        // Chiudi tutti i file
        for (i = 0;i<writers.length;i++) {
            if (writers[i] != null) {
                writers[i].close();
            }
        }
        System.out.println("Tutti i file chiusi correttamente.");
    }
    

    
}
