/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: Client Stream Corretto
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

#define LENGTH_CMD 256
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define RECV_STRING(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define RECV_LONG(sd, val) read(sd, &val, sizeof(long))

int main(int argc, char *argv[]) {
    char dir[LENGTH_CMD], prefisso[LENGTH_CMD];
    char buff[LENGTH_CMD], nome_file[LENGTH_CMD];
    int sd, fd_file, nread, port, cont, nwrite, bytes_to_read, flag;
    long file_size;
    struct hostent *host;
    struct sockaddr_in servaddr;

    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(EXIT_FAILURE);
    }

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { printf("not found\n"); exit(EXIT_FAILURE); }

    // Controllo porta
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Argomento non intero\n");
            exit(EXIT_FAILURE);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("Porta scorretta...\n");
        exit(EXIT_FAILURE);
    }

    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { perror("socket"); exit(EXIT_FAILURE); }
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("connect"); exit(EXIT_FAILURE);
    }

    printf("Nome del direttorio remoto: ");
    while (gets(dir)) {
        printf("Prefisso file (max 4 char): ");
        gets(prefisso);
        
        if (strlen(prefisso) > 4) {
            printf("Prefisso troppo lungo, riprova.\n");
            printf("Nome del direttorio remoto: ");
            continue;
        }

        // Invio richieste
        if (write(sd, dir, LENGTH_CMD) < 0) { perror("write dir"); break; }
        if (write(sd, prefisso, LENGTH_CMD) < 0) { perror("write pref"); break; }

        RECV_LONG(sd, file_size);
        
        if (file_size < 0) {
            printf("Errore: directory inesistente o vuota lato server.\n");
        } else {
            // Ciclo ricezione file
            while (file_size >= 0) {
                RECV_STRING(sd, nome_file);
                printf("Ricezione file: %s (%ld bytes)\n", nome_file, file_size);

                // Apro file locale
                if ((fd_file = open(nome_file, O_WRONLY | O_CREAT | O_TRUNC, 0644)) < 0) {
                    perror("open local file");
                    // Bisogna comunque consumare i byte dalla socket
                    cont = 0;
                    while(cont < file_size) {
                        bytes_to_read = (file_size - cont < LENGTH_CMD) ? (file_size - cont) : LENGTH_CMD;
                        read(sd, buff, bytes_to_read);
                        cont += bytes_to_read;
                    }
                } else {
                    cont = 0; flag = 0;
                    while (cont < file_size && flag == 0) {
                        bytes_to_read = (file_size - cont < LENGTH_CMD) ? (file_size - cont) : LENGTH_CMD;
                        nread = read(sd, buff, bytes_to_read);
                        if (nread > 0) {
                            nwrite = write(fd_file, buff, nread);
                            if (nwrite >= 0) cont += nread;
                            else flag = 1;
                        } else flag = 1;
                    }
                    close(fd_file);
                }
                // Attendo dimensione prossimo file o -1
                RECV_LONG(sd, file_size);
            }
            printf("Trasferimento completato.\n");
        }
        printf("Nome del direttorio remoto (o CTRL+D per uscire): ");
    }
    close(sd);
    return EXIT_SUCCESS;
}