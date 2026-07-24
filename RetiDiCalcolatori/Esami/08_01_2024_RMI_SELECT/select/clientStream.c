/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>

#define DIM_BUFF 256
#define LENGTH_CMD 256
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define RECV_STRING(sd, buf) read(sd, buf, LENGTH_CMD)
#define RECV_LONG(sd, val) read(sd, &val, sizeof(long))

int main(int argc, char *argv[]) {
    // VARIABILI TUTTE ALL'INIZIO
    int sd, nread, port, fd_file, cont;
    int bytes_to_read;
    int flag_read; // Per sostituire break
    long file_size;
    char input[LENGTH_CMD], nome_file[LENGTH_CMD], buff[DIM_BUFF];
    struct hostent *host;
    struct sockaddr_in servaddr;

    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { 
        printf("not found\n"); 
        exit(EXIT_FAILURE);
    }

    port = atoi(argv[2]);
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { perror("socket"); exit(EXIT_FAILURE); }
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("connect"); exit(EXIT_FAILURE);
    }

    printf("Inserisci direttorio per download: ");

    // Uso gets come richiesto
    while (gets(input)) {
        SEND_STRING(sd, input);

        // Prima lettura: dimensione file o -1 (fine) o errore
        RECV_LONG(sd, file_size);
        
        if (file_size < 0 && file_size != -1) {
             // Caso di errore lato server (opzionale se gestito)
             printf("Errore o direttorio vuoto.\n");
        } 
        else {
            // Ciclo finché ci sono file (file_size >= 0)
            while (file_size >= 0) {
                RECV_STRING(sd, nome_file);
                printf("Scaricando: %s (%ld bytes)\n", nome_file, file_size);

                fd_file = open(nome_file, O_WRONLY | O_CREAT | O_TRUNC, 0644);
                if (fd_file < 0) {
                    perror("open file locale");
                    // Bisogna comunque consumare i byte dal socket per non disallineare
                    // Ma per semplicità qui stampiamo errore
                } 
                
                cont = 0;
                flag_read = 1;
                while (cont < file_size && flag_read == 1) {
                    // Calcolo quanto leggere: minimo tra buffer e rimanente
                    bytes_to_read = DIM_BUFF;
                    if ((file_size - cont) < DIM_BUFF) {
                        bytes_to_read = (int)(file_size - cont);
                    }

                    nread = read(sd, buff, bytes_to_read);
                    if (nread > 0) {
                        if (fd_file >= 0) write(fd_file, buff, nread);
                        cont += nread;
                    } else {
                        flag_read = 0; // Errore o fine inattesa
                    }
                }
                
                if (fd_file >= 0) {
                    close(fd_file);
                    printf("Completato: %s\n", nome_file);
                }

                // Leggo la dimensione del PROSSIMO file
                RECV_LONG(sd, file_size);
            }
        }
        printf("Finito trasferimento per %s. Inserisci altro direttorio: ", input);
    }
    
    close(sd);
    return EXIT_SUCCESS;
}