/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
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

#define DIM_BUFF 256
#define LENGTH_PATH 256
#define MAX_STACK 100
#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_PATH)
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_PATH)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))

#define max(a, b)  ((a) > (b) ? (a) : (b))

int main(int argc, char **argv) {
    // --- 1. DICHIARAZIONE DI TUTTE LE VARIABILI ALL'INIZIO ---
    int listenfd, connfd, udpfd, nready, maxfdp1;
    const int on = 1;
    char buff[DIM_BUFF];
    char rootDir[LENGTH_PATH];
    char stackDir[MAX_STACK][LENGTH_PATH]; 
    char currentDir[LENGTH_PATH];
    char fullPath[LENGTH_PATH];
    char entryName[LENGTH_PATH];
    char nome_file_udp[LENGTH_PATH];
    
    // Puntatori per directory
    DIR *d;
    struct dirent *dir;
    
    // Variabili contatori, flag e file descriptors
    int stackTop;
    int i, len;
    int nread, port, res;
    int hasVowel, hasConsonant;
    int finishedDir; 
    int fd, fd_out;
    long file_size;
    int bytes_to_read;
    int cont;
    int flag_rw; // Flag per sostituire break
    char c;

    struct sockaddr_in cliaddr, servaddr;
    socklen_t len_sock;
    fd_set rset;

    // --- 2. CONTROLLO ARGOMENTI ---
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    
    nread = 0;
    flag_rw = 1; // 1 = valido
    while (argv[1][nread] != '\0' && flag_rw == 1) {
        if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
            flag_rw = 0;
        }
        nread++;
    }
    if (flag_rw == 0) {
        printf("Argomento non intero\n");
        exit(2);
    }
    port = atoi(argv[1]);

    // --- 3. INIZIALIZZAZIONE SOCKET ---
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) { perror("socket TCP"); exit(EXIT_FAILURE); }
    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("setsockopt"); exit(EXIT_FAILURE); }
    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind TCP"); exit(EXIT_FAILURE); }
    if (listen(listenfd, 5) < 0) { perror("listen"); exit(EXIT_FAILURE); }

    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) { perror("socket UDP"); exit(EXIT_FAILURE); }
    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("setsockopt"); exit(EXIT_FAILURE); }
    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind UDP"); exit(EXIT_FAILURE); }

    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;
    printf("Server avviato.\n");

    // --- 4. CICLO PRINCIPALE ---
    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);

        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) continue;
            else { perror("select"); exit(EXIT_FAILURE); }
        }

        // ---------------- TCP (TRASFERIMENTO FILE) ----------------
        if (FD_ISSET(listenfd, &rset)) {
            len_sock = sizeof(cliaddr);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len_sock)) < 0) {
                if (errno == EINTR) continue;
                else { perror("accept"); exit(9); }
            }

            if (fork() == 0) { // Figlio
                close(listenfd);
                
                while ((nread = RECV_STRING_STREAM(connfd, rootDir)) > 0) {
                    printf("Richiesta TCP per direttorio: %s\n", rootDir);

                    // Reset Stack
                    stackTop = 0;
                    strcpy(stackDir[stackTop], rootDir);
                    stackTop = stackTop + 1;

                    //Applicazione dell'algoritmo DFS, in maniera tale da attuare una ricorsione
                    //ma in maniera iterativa, cercando in un contesto distribuito di alleviare
                    //uno stack overflow, creatosi da una esecuzione ricorsiva, come visto a lezione
                    while (stackTop > 0) {
                        stackTop = stackTop - 1;
                        strcpy(currentDir, stackDir[stackTop]);

                        d = opendir(currentDir);

                        if (d != NULL) {
                            finishedDir = 0;
                            while (finishedDir == 0) {
                                dir = readdir(d);
                                if (dir == NULL) {
                                    finishedDir = 1;
                                } else {
                                    strcpy(entryName, dir->d_name);
                                    
                                    // Salta . e ..
                                    if (strcmp(entryName, ".") != 0 && strcmp(entryName, "..") != 0) {
                                        
                                        // Costruisco percorso completo
                                        strcpy(fullPath, currentDir);
                                        strcat(fullPath, "/");
                                        strcat(fullPath, entryName);

                                        
                                        // 1. Se è una DIRECTORY -> Push nello stack
                                        if (dir->d_type == DT_DIR) {
                                            if (stackTop < MAX_STACK) {
                                                strcpy(stackDir[stackTop], fullPath);
                                                stackTop = stackTop + 1;
                                            }
                                        } 
                                        // 2. Se è un FILE REGOLARE -> Controllo nome
                                        else if (dir->d_type == DT_REG) {
                                            len = strlen(entryName);
                                            i = 0;
                                            hasVowel = 0;
                                            hasConsonant = 0;

                                            while (i < len) {
                                                c = entryName[i];
                                                if (c=='a'||c=='e'||c=='i'||c=='o'||c=='u'||
                                                    c=='A'||c=='E'||c=='I'||c=='O'||c=='U') hasVowel = 1;
                                                else if ((c>='a' && c<='z') || (c>='A' && c<='Z')) hasConsonant = 1;
                                                i++;
                                            }

                                            if (hasVowel == 1 && hasConsonant == 1) {
                                                printf("Invio file: %s\n", entryName);
                                                
                                                fd = open(fullPath, O_RDONLY);
                                                if (fd >= 0) {
                                                    // Uso lseek per la dimensione (evito stat)
                                                    file_size = (long)lseek(fd, 0, SEEK_END);
                                                    lseek(fd, 0, SEEK_SET); // Torno all'inizio

                                                    SEND_LONG(connfd, file_size);
                                                    SEND_STRING(connfd, entryName); // Mando solo il nome base

                                                    // Invio contenuto
                                                    flag_rw = 1;
                                                    while(flag_rw == 1){
                                                        nread = read(fd, buff, DIM_BUFF);
                                                        if(nread > 0) write(connfd, buff, nread);
                                                        else flag_rw = 0;
                                                    }
                                                    close(fd);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            closedir(d);
                        }
                    } // Fine Stack Loop

                    // Segnale fine files (-1)
                    file_size = -1;
                    SEND_LONG(connfd, file_size);
                }
                
                close(connfd);
                exit(0);
            }
            close(connfd);
        }

        // ---------------- UDP (RIMOZIONE VOCALI) ----------------
        if (FD_ISSET(udpfd, &rset)) {
            len_sock = sizeof(cliaddr);
            nread = recvfrom(udpfd, nome_file_udp, LENGTH_PATH, 0, (struct sockaddr *)&cliaddr, &len_sock);
            
            if (nread > 0) {
                // Assicuro terminatore stringa
                if(nread < LENGTH_PATH) nome_file_udp[nread] = '\0';
                
                printf("Richiesta UDP su file: %s\n", nome_file_udp);
                res = -1; // Default errore
                cont = 0;

                fd = open(nome_file_udp, O_RDONLY);
                if (fd >= 0) {
                    fd_out = open("temp", O_WRONLY | O_CREAT | O_TRUNC, 0666);
                    if (fd_out >= 0) {
                        flag_rw = 1;
                        while(flag_rw == 1){
                            nread = read(fd, &c, 1);
                            if (nread > 0) {
                                // Controllo vocale
                                if(c=='a'||c=='A'||c=='e'||c=='E'||c=='i'||c=='I'||
                                   c=='o'||c=='O'||c=='u'||c=='U') {
                                    cont++;
                                } else {
                                    write(fd_out, &c, 1);
                                }
                            } else {
                                flag_rw = 0;
                            }
                        }
                        close(fd_out);
                        close(fd);
                        
                        if (rename("temp", nome_file_udp) == 0) {
                            res = cont; 
                        } else {
                            unlink("temp");
                        }
                    } else {
                        close(fd);
                    }
                }
                sendto(udpfd, &res, sizeof(res), 0, (struct sockaddr *)&cliaddr, len_sock);
            }
        }
    }
}