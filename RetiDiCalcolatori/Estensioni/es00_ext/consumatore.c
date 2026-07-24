#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#define MAX_STRING_LENGTH 256
/*specifiche:
    Il consumatore agisce in modo concorrente, lanciando diversi
    processi figli che lavorano in modo indipendente, ciascuno su
    uno dei file passati come argomenti in ordine
    Il processo padre controlla gli argomenti e genera i figli per il
    processing degli argomenti correttamente ricevuti (cioè file
    esistenti e presenti nel direttorio locale), quindi termina
    Ciascun processo figlio è un filtro che legge il file fino a EOF (end
    of file). Si noti che gli output generati non devono essere scritti su
    standard output, ma rispettivamente scritti sui singoli file di testo
    passati come argomenti all’invocazione (ovviamente cambiandone il
    contenuto)
    A tale scopo, si suggerisce che ciascun figlio crei un file di appoggio
    che sarà popolato col contenuto filtrato e poi copiato sul file di input,
    (sostituendolo al file esistente ed eliminandolo alla terminazione)

*/
int main(int argc, char *argv[]) {
    if (argc < 3) {//controllo argomenti, successivamente si controllera se sia un file testo
        printf("Errore: numeri di argomenti insufficienti per l'esecuzione di %s", argv[0]);
        exit(EXIT_FAILURE);
    }
    //creo variabili e array
    char prefix[MAX_STRING_LENGTH], filename[MAX_STRING_LENGTH], ch,  temp_name[MAX_STRING_LENGTH];
    int num_files, f, i, len, fd_in, fd_out, nread, found, status, pid_terminated, pid[argc-2], written;
    strcpy(prefix, argv[1]);
    num_files = argc - 2;

 
    for ( f = 0; f < num_files; f++) {
         strcpy(filename,argv[f+2]);
         len = strlen(filename);

        // Controllo che sia un file di testo, in caso di errore significa che gli argomenti erano sbagliati
        if (len<5 || strcmp(filename+len-4, ".txt") != 0) {
            printf("Errore: %s non è un file .txt valido.\n", filename);
            exit(EXIT_FAILURE);
        }

        // Controlla che esista, in caso di errore
        if (access(filename, F_OK) != 0) {
            perror("File non trovato");
            exit(EXIT_FAILURE);
        }
        //controllo dei permessi di lettura per copiarlo e scrittura dato che dobbiamo filtrarlo e modificarlo
        if (access(filename, R_OK) != 0 || access(filename,W_OK)!=0) {
            perror("File permesso negato");
            exit(EXIT_FAILURE);
        }

        pid[f]= fork();
        if (pid[f] < 0) {
            perror("Errore nella fork");
            exit(EXIT_FAILURE);
        } else if (pid[f] == 0) {//figlio
            fd_in = open(filename, O_RDONLY);
            if (fd_in < 0) {
                perror("Errore apertura file in lettura");
                exit(EXIT_FAILURE);
            }
            sprintf(temp_name,"%d",getpid());//nome univoco per evitare concorrenza sullo stesso file
            //apro il file, in caso esista gia lo svuoto e in caso non esista lo creo
            fd_out = open(temp_name, O_WRONLY | O_CREAT | O_TRUNC, 0644);
            if (fd_out < 0) {
                perror("Errore creazione file temporaneo");
                close(fd_in);
                exit(EXIT_FAILURE);
            }

            
            while ((nread = read(fd_in, &ch, sizeof(char)))) {
                if (nread < 0) {
                    perror("Errore durante la lettura");
                    close(fd_in);
                    close(fd_out);
                    unlink(temp_name);
                    exit(EXIT_FAILURE);
                }
                found = 0;
                i=0;
                while(prefix[i]!='\0' && found==0) {
                    if (ch == prefix[i]) {
                        found = 1;
                    }
                    i++;
                }
                if (!found) {
                    written = write(fd_out, &ch, sizeof(char));
                    if(written<0){
                        perror("Errore scrittura file temporaneo");
                        close(fd_in);
                        close(fd_out);
                        unlink(temp_name);
                        exit(EXIT_FAILURE);
                    }

                }
            }
            close(fd_in);
            close(fd_out);

            // Sovrascrive il file originale con il file filtrato
            if (rename(temp_name, filename) != 0) {
                perror("Errore nella sostituzione del file originale");
                unlink(temp_name);
                exit(EXIT_FAILURE);
            }

            printf("File %s filtrato con successo dal figlio PID %d.\n", filename, getpid());
            exit(EXIT_SUCCESS);
        }

       
    }
    
    for(f= 0;f<num_files;f++){
        if(pid[f]>0){ //padre
            pid_terminated=wait(&status);

            if(WIFEXITED(status)){
                if(WEXITSTATUS(status)== 0)
                    printf("\nPADRE: terminazione volontaria del figlio %d con stato %d\n",pid_terminated,WEXITSTATUS(status));
                else
                    printf("\nPADRE: terminazione involontaria del figlio %d con stato %d\n",pid_terminated,WEXITSTATUS(status));
            }
            else if(WIFSIGNALED(status))
                printf("\nPADRE: terminazione involontaria del figlio %d a causa del segnale %d\n",pid_terminated,WTERMSIG(status));
        }
    }
    return EXIT_SUCCESS;
}
