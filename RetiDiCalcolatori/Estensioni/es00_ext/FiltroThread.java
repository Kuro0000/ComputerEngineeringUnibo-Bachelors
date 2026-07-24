import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
class FiltroThread extends Thread {
    private String prefix;
    private File file;

    public FiltroThread(String prefix, File file) {
        this.prefix = prefix;
        this.file = file;
    }

    @Override
    public void run() {
        String tempName =this.getName();
        File tempFile = new File(tempName);
        try (
            BufferedReader reader = new BufferedReader(new FileReader(file));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            int carattere = -1;
            char c;
            while ((carattere = reader.read()) != -1) {//filtro a carattere
                c = (char) carattere;

                // Controllo se il carattere è nel prefisso
                if (prefix.indexOf(c) == -1) {
                    writer.write(c);
                }
            }

            writer.flush();

            // Sostituisce il file originale
            if (file.delete()) {
                if (!tempFile.renameTo(file)) {
                    tempFile.delete();// errore, eliminiamo il file temporaneo creato
                    System.err.println("Errore nella sostituzione del file originale: " + file.getName());
                } else {
                    System.out.println("File " + file.getName() + " filtrato con successo dal thread " + this.getName());
                }
            } else {
                tempFile.delete();// errore, eliminiamo il file temporaneo creato
                System.err.println("Errore eliminando il file originale " + file.getName());
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'elaborazione di " + file.getName() + ": " + e.getMessage());
            tempFile.delete();
        }
    }
}