/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: Server TCP/UDP
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
#define LENGTH_CMD 256
#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))

#define max(a, b)  ((a) > (b) ? (a) : (b))

int main(int argc, char **argv) {
    /* DICHIARAZIONE VARIABILI IN TESTA */
    int listenfd, connfd, udpfd, nready, maxfdp1, pid;
    const int on = 1;
    char buff[DIM_BUFF], direttorio[LENGTH_CMD], nome_file[LENGTH_CMD];
    char subdir_path[LENGTH_CMD]; 
    char c;
    fd_set rset;
    int len, nread, port, res, parziale_res, fd, cont, flag, bytes_to_read, foundAlfa, foundNum, count_nums;
    long soglia, file_size;
    struct sockaddr_in cliaddr, servaddr;
    
    /* Puntatori per directory principale */
    DIR *dir;
    struct dirent *entry;
    
    /* Puntatori AGGIUNTIVI per sottodirectory */
    DIR *subDir;
    struct dirent *subEntry;

    /* CONTROLLO ARGOMENTI */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    
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

    /* INIZIALIZZAZIONE STRUTTURE */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* SOCKET TCP */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) { perror("apertura socket TCP"); exit(EXIT_FAILURE); }
    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("TCP setsockopt"); exit(EXIT_FAILURE); }
    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind TCP"); exit(EXIT_FAILURE); }
    if (listen(listenfd, 5) < 0) { perror("listen"); exit(EXIT_FAILURE); }

    /* SOCKET UDP */
    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) { perror("apertura socket UDP"); exit(EXIT_FAILURE); }
    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("UDP setsockopt"); exit(EXIT_FAILURE); }
    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind UDP"); exit(EXIT_FAILURE); }

    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;
    printf("Server avviato sulla porta %d\n", port);

    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);
        
        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) continue;
            else { perror("select"); exit(EXIT_FAILURE); }
        }

        /* ---------------------------------------------------------------- */
        /* GESTIONE TCP (STREAM) - Directory e Sottodirectory */
        /* ---------------------------------------------------------------- */
        if (FD_ISSET(listenfd, &rset)) {
            len = sizeof(cliaddr);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) continue;
                else { perror("accept"); exit(9); }
            }
            
            if (fork() == 0) { /* Figlio */
                close(listenfd);
                
                while((nread = RECV_STRING_STREAM(connfd, direttorio)) > 0) {
                    printf("Richiesto direttorio: %s\n", direttorio);
                    
                    dir = opendir(direttorio);
                    if (dir == NULL) {
                        file_size = -1;
                        SEND_LONG(connfd, file_size);
                        printf("Direttorio non trovato\n");
                    } else {
                        /* CICLO PRINCIPALE */
                        while ((entry = readdir(dir)) != NULL) {
                            
                            /* CASO FILE (Livello 1) */
                            if (entry->d_type == DT_REG) {
                                strcpy(nome_file, entry->d_name);
                                sprintf(buff, "%s/%s", direttorio, nome_file);
                                
                                if((fd = open(buff, O_RDONLY)) >= 0) {
                                    file_size = (long)lseek(fd, 0, SEEK_END);
                                    lseek(fd, 0, SEEK_SET);
                                    
                                    SEND_LONG(connfd, file_size);
                                    SEND_STRING(connfd, nome_file);
                                    
                                    cont = 0; flag = 0;
                                    while (cont < file_size && flag == 0) {
                                        bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                        nread = read(fd, buff, bytes_to_read);
                                        if (nread > 0) {
                                            write(connfd, buff, nread);
                                            cont += nread;
                                        } else flag = 1;
                                    }
                                    close(fd);
                                }
                            }
                            /* CASO DIRECTORY (Livello 1 -> Entro nel Livello 2) */
                            else if(entry->d_type == DT_DIR) {
                                /* Importante: saltare . e .. */
                                if(strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0){
                                    
                                    sprintf(subdir_path, "%s/%s", direttorio, entry->d_name);
                                    subDir = opendir(subdir_path);
                                    
                                    if (subDir != NULL) {
                                        /* Uso subEntry per non sovrascrivere entry del ciclo esterno */
                                        while ((subEntry = readdir(subDir)) != NULL) {
                                            if (subEntry->d_type == DT_REG) {
                                                strcpy(nome_file, subEntry->d_name);
                                                sprintf(buff, "%s/%s", subdir_path, nome_file);
                                                
                                                if((fd = open(buff, O_RDONLY)) >= 0) {
                                                    file_size = (long)lseek(fd, 0, SEEK_END);
                                                    lseek(fd, 0, SEEK_SET);
                                                    
                                                    SEND_LONG(connfd, file_size);
                                                    SEND_STRING(connfd, nome_file);
                                                    
                                                    cont = 0; flag = 0;
                                                    while (cont < file_size && flag == 0) {
                                                        bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                                        nread = read(fd, buff, bytes_to_read);
                                                        if (nread > 0) {
                                                            write(connfd, buff, nread);
                                                            cont += nread;
                                                        } else flag = 1;
                                                    }
                                                    close(fd);
                                                }
                                            }
                                        }
                                        closedir(subDir); /* Corretto: closedir, non close */
                                    }
                                }
                            }
                        }
                        closedir(dir);
                        
                        /* Terminazione invio */
                        file_size = -1;
                        SEND_LONG(connfd, file_size);
                    }
                }
                close(connfd);
                exit(0);
            }
            close(connfd);
        }

        /* ---------------------------------------------------------------- */
        /* GESTIONE UDP (DATAGRAM) - Conta righe con validazione */
        /* ---------------------------------------------------------------- */
        
        if (FD_ISSET(udpfd, &rset)) {
            len = sizeof(cliaddr);
            nread = recvfrom(udpfd, &soglia, sizeof(long), 0, (struct sockaddr *)&cliaddr, &len);
            
            if (nread > 0) {
                res = 0; /* Totale righe valide trovate in tutti i file */
                dir = opendir("."); 

                if (dir == NULL) {
                    file_size = -1L; /* Uso file_size come flag errore */
                    sendto(udpfd, &file_size, sizeof(long), 0, (struct sockaddr *)&cliaddr, len);
                } else {
                    while ((entry = readdir(dir)) != NULL) {
                        if (entry->d_type == DT_REG) {
                            
                            /* Controllo estensione .txt */
                            cont = strlen(entry->d_name);
                            if (cont < 4 || strcmp(entry->d_name + cont - 4, ".txt") != 0) {
                                continue; /* Salto se non è .txt */
                            }

                            strcpy(nome_file, entry->d_name);
                            
                            /* Controllo almeno 2 cifre nel nome file */
                            cont = 0;
                            count_nums = 0;
                            while(nome_file[cont] != '\0'){
                                if(nome_file[cont] >= '0' && nome_file[cont] <= '9'){
                                    count_nums++;
                                }
                                cont++;
                            }
                            
                            if(count_nums >= 2){
                                /* Apro il file */
                                if((fd = open(nome_file, O_RDONLY)) >= 0){
                                    
                                    /* Variabili per il file corrente */
                                    parziale_res = 0; 
                                    cont = 0;       /* Lunghezza riga corrente */
                                    foundAlfa = 0;  /* Flag Globale per il file */
                                    foundNum = 0;   /* Flag Globale per il file */
                                    
                                    while(read(fd, &c, 1) > 0){
                                        if(c == '\n'){
                                            /* Fine riga: controllo lunghezza */
                                            if(cont > soglia){
                                                parziale_res++;
                                            }
                                            cont = 0; /* Resetto contatore lunghezza riga */
                                        } else {
                                            cont++;
                                            /* Aggiorno flag validità file (non resettare mai) */
                                            if((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')){
                                                foundAlfa = 1;
                                            }
                                            if(c >= '0' && c <= '9'){
                                                foundNum = 1;
                                            }
                                        }
                                    }
                                    /* Controllo eventuale ultima riga senza \n */
                                    if(cont > soglia) {
                                        parziale_res++;
                                    }
                                    
                                    close(fd);

                                    /* SOLO ORA decido se il file era valido */
                                    if(foundAlfa == 1 && foundNum == 1){
                                        res += parziale_res;
                                    }
                                }
                            }
                        }
                    }
                    closedir(dir);
                    
                    /* Invio risultato intero */
                    sendto(udpfd, &res, sizeof(int), 0, (struct sockaddr *)&cliaddr, len);
                }
            }
        }
    }
}