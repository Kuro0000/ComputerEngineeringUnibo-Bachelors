/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: Server Corretto
*/
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <sys/stat.h>

#define DIM_BUFF 256
#define LENGTH_CMD 256
#define RECV_STRING(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define max(a, b) ((a) > (b) ? (a) : (b))

typedef struct {
    char input;
    int occorrenze;
} Pack;

int main(int argc, char **argv) {
    int listenfd, connfd, udpfd, nready, maxfdp1, occorrenze, fd, count, occ;
    int len, cont, nwrite, i, bytes_to_read, flag;
    long file_size;
    int line_start_upper; // Flag per inizio riga maiuscola
    int is_first_char_of_line; // Flag per identificare inizio riga

    const int on = 1;
    char buff[DIM_BUFF], input, c, nome_dir[LENGTH_CMD], nome_file[LENGTH_CMD], prefisso[LENGTH_CMD]; // Prefisso aumentato a LENGTH_CMD
    fd_set rset;
    int nread, port, res;
    struct sockaddr_in cliaddr, servaddr;
    Pack pack;
    struct dirent *entry;
    DIR *dir;

    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    
    // Controllo porta
    nread = 0;
    while (argv[1][nread] != '\0') {
        if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
            printf("Argomento non intero\n");
            exit(2);
        }
        nread++;
    }
    port = atoi(argv[1]);
    if (port < 1024 || port > 65535) {
        printf("Porta scorretta...\n");
        exit(2);
    }

    // Evita processi zombie
    signal(SIGCHLD, SIG_IGN);

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);

    printf("Inizializzo le socket\n");
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) { perror("apertura socket TCP"); exit(EXIT_FAILURE); }
    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("TCP setsockopt"); exit(EXIT_FAILURE); }
    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind TCP"); exit(EXIT_FAILURE); }
    if (listen(listenfd, 5) < 0) { perror("listen"); exit(EXIT_FAILURE); }

    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) { perror("apertura socket UDP"); exit(EXIT_FAILURE); }
    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("UDP setsockopt"); exit(EXIT_FAILURE); }
    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind UDP"); exit(EXIT_FAILURE); }

    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;
    printf("Inizio del demone\n");

    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);
        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) continue;
            else { perror("select"); exit(EXIT_FAILURE); }
        }

        // --- TCP STREAM ---
        if (FD_ISSET(listenfd, &rset)) {
            len = sizeof(cliaddr);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) continue;
                else { perror("accept"); exit(9); }
            }
            printf("Accettata connessione stream\n");
            
            if (fork() == 0) { // FIGLIO
                close(listenfd);
                RECV_STRING(connfd, nome_dir);
                // IMPORTANTE: Leggiamo LENGTH_CMD byte per allinearci col client
                RECV_STRING(connfd, prefisso); 
                
                printf("Richiesto direttorio: %s, prefisso: %s\n", nome_dir, prefisso);
                
                dir = opendir(nome_dir);
                if (dir == NULL) {
                    file_size = -1;
                    SEND_LONG(connfd, file_size);
                    printf("Directory non trovata\n");
                } else {
                    while ((entry = readdir(dir)) != NULL) {
                        if (entry->d_type == DT_REG) { // Solo file regolari
                            // Controllo estensione .txt
                            if (strlen(entry->d_name) < 5 || strcmp(entry->d_name + strlen(entry->d_name) - 4, ".txt") != 0) {
                                continue;
                            }
                            // Controllo prefisso (strncmp è più sicuro di strstr per "inizia con")
                            if (strstr(entry->d_name, prefisso) != entry->d_name) {
                                continue; // Salta se il prefisso non è all'inizio
                            }

                            strcpy(nome_file, entry->d_name);
                            sprintf(buff, "%s/%s", nome_dir, nome_file);
                            
                            if ((fd = open(buff, O_RDONLY)) < 0) {
                                perror("open file");
                                continue;
                            }
                            
                            file_size = (long)lseek(fd, 0, SEEK_END);
                            lseek(fd, 0, SEEK_SET);
                            
                            SEND_LONG(connfd, file_size);
                            SEND_STRING(connfd, nome_file);
                            
                            printf("Invio file: %s (%d byte)\n", nome_file, file_size);
                            
                            cont = 0; flag = 0;
                            while (cont < file_size && flag == 0) {
                                bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                nread = read(fd, buff, bytes_to_read);
                                if (nread > 0) {
                                    nwrite = write(connfd, buff, nread);
                                    if (nwrite >= 0) cont += nread;
                                    else flag = 1;
                                } else flag = 1;
                            }
                            close(fd);
                        }
                    }
                    closedir(dir);
                    file_size = -1;
                    SEND_LONG(connfd, file_size); // Fine trasmissione
                }
                printf("Fine operazione stream figlio\n");
                close(connfd);
                exit(EXIT_SUCCESS);
            }
            close(connfd); // Padre chiude
        }

        // --- UDP DATAGRAM ---
        if (FD_ISSET(udpfd, &rset)) {
            printf("Ricevuta richiesta UDP\n");
            res = -1;
            len = sizeof(cliaddr);
            nread = recvfrom(udpfd, &pack, sizeof(Pack), 0, (struct sockaddr *)&cliaddr, &len);
            
            if (nread > 0) {
                input = pack.input;
                occorrenze = pack.occorrenze;
                printf("Input: %c, Occorrenze min: %d\n", input, occorrenze);

                dir = opendir("."); // Directory corrente remota
                if (!dir) {
                    res = -1;
                    sendto(udpfd, &res, sizeof(res), 0, (struct sockaddr *)&cliaddr, len);
                    perror("opendir udp");
                    continue; 
                }

                count = 0; // Totale righe trovate in tutti i file
                while ((entry = readdir(dir)) != NULL) {
                    // Salta . e .. e file non .txt
                    if (entry->d_name[0] == '.') continue;
                    if (strlen(entry->d_name) < 5 || strcmp(entry->d_name + strlen(entry->d_name) - 4, ".txt") != 0) continue;

                    if ((fd = open(entry->d_name, O_RDONLY)) < 0) continue;

                    // Logica conteggio righe
                    occ = 0;
                    line_start_upper = 0;
                    is_first_char_of_line = 1;
                    
                    while (read(fd, &c, 1) > 0) {
                        if (c == '\n') {
                            // Fine riga: controlliamo se soddisfa i requisiti
                            if (line_start_upper && occ >= occorrenze) {
                                count++;
                            }
                            // Reset per la prossima riga
                            occ = 0;
                            line_start_upper = 0;
                            is_first_char_of_line = 1;
                        } else {
                            // Carattere normale
                            if (is_first_char_of_line) {
                                if (c >= 'A' && c <= 'Z') {
                                    line_start_upper = 1;
                                } else {
                                    line_start_upper = 0;
                                }
                                is_first_char_of_line = 0;
                            }
                            
                            if (c == input) {
                                occ++;
                            }
                        }
                    }
                    // Controllo ultima riga se il file non finisce con \n
                    if (!is_first_char_of_line && line_start_upper && occ >= occorrenze) {
                        count++;
                    }

                    close(fd);
                }
                closedir(dir);
                res = count;
                printf("Invio risultato UDP: %d\n", res);

                if (sendto(udpfd, &res, sizeof(res), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                    perror("sendto");
                }
            }
        }
    }
}