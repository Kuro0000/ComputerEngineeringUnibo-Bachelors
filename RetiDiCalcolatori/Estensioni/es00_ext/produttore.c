#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#define MAX_STRING_LENGTH 256
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
int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Errore: numeri di argomenti insufficienti per l'esecuzione di %s", argv[0]);
        exit(EXIT_FAILURE);
    }
    int num_files, fd[argc-1], i, j, indice, len, written;
    char riga[MAX_STRING_LENGTH], *sep, *testo, filename[MAX_STRING_LENGTH];

    num_files= argc-1;

    // Apri tutti i file in modalità append
    for (i=0; i<num_files; i++) {
        strcpy(filename,argv[i+1]);
        len = strlen(filename);
        //controllo del file se sia un .txt
        if (len<5 || strcmp(filename+len-4, ".txt") != 0) {
            printf("Errore: %s non è un file .txt valido.\n", filename);
            exit(EXIT_FAILURE);
        }
        //controllo permessi di scrittura
        if (access(filename,W_OK)!=0) {
            perror("File permesso negato");
            exit(EXIT_FAILURE);
        }
        fd[i]= open(filename, O_WRONLY | O_CREAT | O_APPEND, 0640);
        if (fd[i]<0) {
            perror("Errore apertura file");
            // Chiudi eventuali file già aperti
            for (j=0;j<i;j++) close(fd[j]);
            exit(EXIT_FAILURE);
        }
    }
    printf("scrivere le righe da inserire\n");
    while (gets(riga)) {
        // Rimuovi eventuale newline finale
        riga[strlen(riga)+1] = '\0';
        riga[strlen(riga)] = '\n';

        // Trova il separatore ':'
        sep = strchr(riga, ':');
        if (!sep) {
            printf("Formato errato: manca il separatore ':'  %s\n", riga);
            exit(EXIT_FAILURE);
        }

        // Estrai indice numerico
        *sep = '\0'; // separa la stringa in due parti
        for(j=0;riga[j]!='\0';j++){
            if(riga[j] < '0' || riga[j] > '9')
                {
                    printf("Formato errato: prima del separatore deve esserci un indice numerico %s\n", riga);
                    exit(EXIT_FAILURE);
                }
        }
        indice = atoi(riga); // parte prima dei due punti
        testo = sep + 1; // assegno al puntatore il puntatore dopo i due punti

        if (indice<1 || indice>num_files) {
            printf("Indice file non valido: %d\n", indice);
            exit(EXIT_FAILURE);

        }

            // Aggiungi newline se non presente
            len = strlen(testo);
            if (len == 0) {
                testo[len] = '\n';
                testo[len + 1] = '\0';
            }

        written = write(fd[indice-1], testo, sizeof(char)*(len));
        if (written < 0) {
            perror("Errore scrittura file");
            exit(EXIT_FAILURE);
        }
    }

    // Chiudi tutti i file
    for (i=0; i<num_files; i++) {
        close(fd[i]);
    }

    printf("Tutti i file chiusi correttamente.\n");
    return EXIT_SUCCESS;
}
