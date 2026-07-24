import java.io.*;

public class Consumatore {
    /*  
    specifiche:
    Il consumatore agisce in modo concorrente, lanciando diversi
    processi figli che lavorano in modo indipendente, ciascuno su
    uno dei file passati come argomenti in ordine
    Il processo padre controlla gli argomenti e genera i figli per il
    processing degli argomenti correttamente ricevuti (cioè file
    esistenti e presenti nel direttorio locale), quindi termina
    Ciascun processo figlio è un filtro che legge il file fino a EOF (end
    of file). Si noti che gli output generati non devono essere scritti su
    standard output, ma rispettivamente scritti sui singoli file di testo
    passati come argomenti all'invocazione (ovviamente cambiandone il
    contenuto)
    A tale scopo, si suggerisce che ciascun figlio crei un file di appoggio
    che sarà popolato col contenuto filtrato e poi copiato sul file di input,
    (sostituendolo al file esistente ed eliminandolo alla terminazione) */

    public static void main(String[] args) {
        String prefisso =null;
        int numFiles = -1;
        FiltroThread[] threads = null;
        if (args.length < 2) {
            System.out.println("Errore: numero di argomenti insufficiente. Uso: java Consumatore <prefisso> <file1> <file2> ...");
            System.exit(1);
        }
        prefisso = args[0];
        numFiles = args.length - 1;
        threads = new FiltroThread[numFiles];

        String filename =null;
        File file = null;
        int i =-1; // riutilizzo i in due cicli for
        // Controllo e creazione thread
        for (i = 0; i < numFiles; i++) {
            filename = args[i+1];
            file = new File(filename);

            // Controllo estensione
            if (!filename.endsWith(".txt")) {
                System.out.println("Errore: " + filename + " non è un file .txt valido.");
                System.exit(1);
            }

            // Controllo esistenza e permessi
            if (!file.exists()) {
                System.out.println("Errore: file non trovato -> " + filename);
                System.exit(1);
            }
            if (!file.canRead() || !file.canWrite()) {
                System.out.println("Errore: permessi insufficienti su " + filename);
                System.exit(1);
            }

            // Crea e avvia il thread
            threads[i] = new FiltroThread(prefisso, file);
            threads[i].start();
        }

        // Attesa dei thread
        try {
            for (i = 0; i < numFiles; i++) {
                    threads[i].join();
            }
        } catch (InterruptedException e) {
            System.err.println("Attesa interrotta per il thread " + i);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("\nTutti i thread hanno terminato correttamente.");
    }
}

