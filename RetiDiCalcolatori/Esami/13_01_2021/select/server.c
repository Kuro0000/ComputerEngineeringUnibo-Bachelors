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
#define LENGTH_CMD 256
#define MAX_STACK 100
#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define TO_LOWER(c) ( ((c) >= 'A' && (c) <= 'Z') ? ((c) + 32) : (c) )
#define max(a, b)  ((a) > (b) ? (a) : (b))
typedef struct {
    char car;
    int occorrenze;
} Pack;


int main(int argc, char **argv) {
    int listenfd, connfd, udpfd, nready, maxfdp1, status, pid;
    const int on = 1;
    //variabili trasferimento
    char buff[DIM_BUFF];
    char rootDir[DIM_BUFF];
    char stackDir[MAX_STACK][DIM_BUFF]; 
    char currentDir[DIM_BUFF];
    char fullPath[DIM_BUFF];
    char entryName[DIM_BUFF];
    int stackTop;
    int finishedDir; 
    int fd;
    long file_size;
    int bytes_to_read;
    int cont;
    int flag_rw; // Flag per sostituire break
    char c;
    DIR *dir;// Puntatori per directory
    struct dirent *entry;
    //fine variabili trasferimento
    char nome_file[DIM_BUFF];
    char car;
    int occorrenze;
    fd_set rset;
    int len, nread, port, res;
    struct sockaddr_in cliaddr, servaddr;
    Pack pack;

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

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    printf("inizializzo le socket\n");
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) { 
        perror("apertura socket TCP "); 
        exit(EXIT_FAILURE); 
    }
    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { 
        perror("TCP setsockopt"); 
        exit(EXIT_FAILURE); 
    }
    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { 
        perror("bind TCP"); 
        exit(EXIT_FAILURE); 
    }
    if (listen(listenfd, 5) < 0) { 
        perror("listen"); 
        exit(EXIT_FAILURE); 
    }

    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) { 
        perror("apertura socket UDP"); 
        exit(EXIT_FAILURE); 
    }
    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { 
        perror("UDP setsockopt"); 
        exit(EXIT_FAILURE); 
    }
    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { 
        perror("bind UDP"); 
        exit(EXIT_FAILURE); 
    }


    FD_ZERO(&rset);
    maxfdp1 = max(listenfd, udpfd) + 1;
    printf("inizio del demone\n");
    for (;;) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);
        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) continue;
            else { 
                perror("select"); 
                exit(EXIT_FAILURE); 
            }
        }
        //-----------------------------------------------------------------------------------------

        // STREAM: esecuzione remota comando (output inviato al client)
        if (FD_ISSET(listenfd, &rset)) {

            len = sizeof(cliaddr);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) continue;
                else { perror("accept"); exit(9); }
            }
            printf("accettato la connessione stream, inizio con le operazioni\n");
              if (fork() == 0) { // Figlio
                close(listenfd);
                
                while ((nread = RECV_STRING_STREAM(connfd, rootDir)) > 0) {
                    printf("Richiesta TCP per direttorio: %s\n", rootDir);

                    // Reset Stack
                    stackTop = 0;
                    strcpy(stackDir[stackTop], rootDir);
                    stackTop = stackTop + 1;

                    // Loop Stack (Simulazione Ricorsione)
                    while (stackTop > 0) {
                        stackTop = stackTop - 1;
                        strcpy(currentDir, stackDir[stackTop]);

                        dir = opendir(currentDir);

                        if (dir != NULL) {
                            finishedDir = 0;
                            while (finishedDir == 0) {
                                entry = readdir(dir);
                                if (entry == NULL) {
                                    finishedDir = 1;
                                } else {
                                    strcpy(entryName, entry->d_name);
                                    
                                    // Salta . e ..
                                    if (strcmp(entryName, ".") != 0 && strcmp(entryName, "..") != 0) {
                                        
                                        // Costruisco percorso completo
                                        strcpy(fullPath, currentDir);
                                        strcat(fullPath, "/");
                                        strcat(fullPath, entryName);

                                        
                                        // 1. Se è una DIRECTORY -> Push nello stack
                                        if (entry->d_type == DT_DIR) {
                                            if (stackTop < MAX_STACK) {
                                                strcpy(stackDir[stackTop], fullPath);
                                                stackTop = stackTop + 1;
                                            }
                                        } 
                                        // 2. Se è un FILE REGOLARE -> Controllo nome
                                        else if (entry->d_type == DT_REG) {
                                            len = strlen(entryName);
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
                            closedir(dir);
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
        //-----------------------------------------------------------------------------------------
        // DATAGRAM: esecuzione comando e invio solo valore di ritorno
        if (FD_ISSET(udpfd, &rset)) {
            printf("accettato la connessione udp, inizio con le operazioni\n");

            len = sizeof(cliaddr);
            nread = recvfrom(udpfd, &pack, sizeof(Pack), 0, (struct sockaddr *)&cliaddr, &len);
            if ((nread)>0) { // comando
                car = pack.car;
                printf("ricevuto comando %c\n", car);
                occorrenze = pack.occorrenze;
                printf("ricevuto argomento %i \n", occorrenze);
            //iNIZIO




                dir = opendir("."); 

                if (dir == NULL) {
                    res = -1;
                } else {
                    res = 0;   
                    while ((entry = readdir(dir)) != NULL) {
                        if (entry->d_type == DT_REG) {
                            
                            /* Controllo estensione .txt */
                            cont = strlen(entry->d_name);
                            if (cont < 4 || strcmp(entry->d_name + cont - 4, ".txt") != 0) {
                                continue; /* Salto se non è .txt */
                            }

                            strcpy(nome_file, entry->d_name);
                            
                 
                                /* Apro il file */
                                if((fd = open(nome_file, O_RDONLY)) >= 0){
                                 
                                    cont = 0;
                                
                                    if(read(fd, &c, 1)<0){

                                    }else{
                                        if(c>='A' && c<='Z'){
                                           
                                            if(TO_LOWER(c) == TO_LOWER(car)){
                                                cont++;
                                            }
                                            while(read(fd, &c, 1) > 0){
                                                if(c == '\n'){
                                                        if(cont > occorrenze) {
                                                            res++;
                                                        }
                                                    cont = 0;
                                                }else if(TO_LOWER(c) == TO_LOWER(car)){
                                                    cont++;
                                                }

                                            }

                                        }
                                    }
                                    close(fd);

                                }
                            
                        }
                    }
                    closedir(dir);

                }
                    
                    /* Invio risultato intero */
                    sendto(udpfd, &res, sizeof(int), 0, (struct sockaddr *)&cliaddr, len);



            //FINE

          
            }else{
                perror("receivefrom");
            }
            printf("fine operazione udp\n");

        }

    }
}