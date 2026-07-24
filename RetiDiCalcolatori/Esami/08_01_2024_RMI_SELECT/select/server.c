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
#define SEND_STRING(sd, buf) write(sd, buf, DIM_BUFF)

#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define max(a, b)  ((a) > (b) ? (a) : (b))
typedef struct {
    char car;
    int occorrenze;
} Pack;
void gestore(int signo) {
    int state;
    wait(&state);
}

int main(int argc, char **argv) {
    int listenfd, connfd, udpfd, nready, maxfdp1, status, pid;
    const int on = 1;
    char buff[DIM_BUFF], nome_dir[LENGTH_CMD], nome_file[LENGTH_CMD], c, car;
    fd_set rset;
    int len, nread, port, res, fd_file, cont, flag, bytes_to_read, nwrite, occorrenze;
    long file_size;
    struct sockaddr_in cliaddr, servaddr;
    Pack pack;
    DIR *dir;
    struct dirent *entry;

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
    signal(SIGCHLD, gestore);
    printf("inizializzato su porta %d, inizializzo le socket\n", port);
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
            if (fork() == 0) {
                close(listenfd);
                while ((nread = RECV_STRING_STREAM(connfd, nome_dir)) > 0) {


                    printf("ricevuto la cartella %s", nome_dir);
                    dir = opendir(nome_dir);
                    if (dir == NULL) {
                        file_size = -1;
                        SEND_LONG(connfd, file_size); // Fine subito, directory non trovata
                        printf("mandato messaggio not found");
                    } else {
                        while ((entry = readdir(dir)) != NULL) {
                            if (entry->d_type == DT_REG && (strlen(entry->d_name) <5 || strcmp(entry->d_name + strlen(entry->d_name) - 4, ".txt") != 0)) {
                                strcpy(nome_file, entry->d_name);
                                sprintf(buff, "%s/%s", nome_dir, nome_file);
                                printf("inviando il seguente path %s", buff);
                                if((fd_file = open(buff, O_RDONLY))<0) {
                                    perror("apertura del file");
                                    continue;
                                } else {
                                    file_size = (long)lseek(fd_file, 0, SEEK_END); // prendo la dimensione del file portando il puntatore a fine file
                                   lseek(fd_file, 0, SEEK_SET);
                                    SEND_LONG(connfd, file_size);
                                    SEND_STRING(connfd, nome_file);
                                    
                                        cont = 0;
                                        flag = 0;
                                        while (cont < file_size && flag == 0) {
                                            bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                            nread = read(fd_file, buff, bytes_to_read);
                                            if (nread > 0) {
                                                nwrite = write(connfd, buff, nread);
                                                if (nwrite >= 0) cont += nread;
                                                else flag = 1;
                                            } else flag = 1;
                                        }
                                        close(fd_file);
                                    
                                    printf("File \"%s\" inviato. Bytes: %d\n", nome_file, cont);
                                }
                            }
                        }
                        closedir(dir);
                        file_size = -1;
                        SEND_LONG(connfd, file_size); // Segnale di fine files
                    }

                }
                if(nread<0){
                    perror("receivefrom");
                }
                printf("fine operazione stream\n");
                close(connfd);
                exit(EXIT_SUCCESS);
            }
            close(connfd);
        }
        //-----------------------------------------------------------------------------------------
        // DATAGRAM: esecuzione comando e invio solo valore di ritorno
        if (FD_ISSET(udpfd, &rset)) {
            printf("accettato la connessione udp, inizio con le operazioni\n");

            len = sizeof(cliaddr);
            nread = recvfrom(udpfd, &pack, sizeof(Pack), 0, (struct sockaddr *)&cliaddr, &len);

           printf("DEBUG: recvfrom completata, nread = %d\n", nread);  // ← AGGIUNGI QUESTA

            if ((nread)>0) { // comando
                     res = -1;
                car = pack.car;
                occorrenze = pack.occorrenze;
                dir = opendir("."); 
                printf("ricevuto %c e %i\n", car, occorrenze);
                if (dir == NULL) {
                    /* Uso file_size come flag errore */
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
                            printf("preso in considerazione %s\n", nome_file);
                            cont = 0;
                            /* Apro il file */
                            if((fd_file = open(nome_file, O_RDONLY)) >= 0){
                                while(read(fd_file, &c, 1) > 0){
                                    if(c == '\n'){
                                        /* Fine riga: controllo lunghezza */
                                        if(cont >= occorrenze){
                                            res++;
                                        }
                                        cont = 0; /* Resetto contatore lunghezza riga */
                                    } else if(car == c){
                                        cont++;
                                    }
                                }
                                close(fd_file);
                            }
                            
                        }
                    }
                    closedir(dir);
                    printf("invio il risultato dell'operazione %d\n", res);

                }
                
                if(sendto(udpfd, &res, sizeof(res), 0, (struct sockaddr *)&cliaddr, len)<0){
                    perror("sendto");

                }
            }else{
                perror("receivefrom");
            }

            printf("fine operazione udp\n");

        

        }
        
    }

}





