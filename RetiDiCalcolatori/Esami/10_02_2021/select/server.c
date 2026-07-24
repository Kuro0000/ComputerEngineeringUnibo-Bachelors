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
#define DIM_STR 50
#define N 20
#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define max(a, b)  ((a) > (b) ? (a) : (b))

/* DEFINIZIONE STRUTTURA */
typedef struct {
    char id[DIM_STR];
    int giorno;
    int mese;
    int anno;
    int durata;          /* -1 se libero */
    char modello[DIM_STR];
    int costo;
    char nome_file[DIM_STR];
} Noleggio;



int main(int argc, char **argv) {
    int listenfd, connfd, udpfd, nready, maxfdp1, status, pid;
    const int on = 1;
    char buff[DIM_BUFF], comando[LENGTH_CMD], argomenti[LENGTH_CMD];
    fd_set rset;
    int len, nread, port, res;
    struct sockaddr_in cliaddr, servaddr;

    /* --- DICHIARAZIONE VARIABILI (Tutte all'inizio) --- */
    Noleggio registro[N];
    Noleggio result[N];
    int i;
    int dimensioneLogica;
    int trovato_udp;
    /* Variabili temporanee per input/output (se servissero dopo) */
    char input_buffer[DIM_STR];
    long file_size;
    int fd_file,n_read, flag, cont;
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






    /* --- INIZIALIZZAZIONE DATI --- */

    /* Elemento 0: X12AB */
    strcpy(registro[0].id, "X12AB");
    registro[0].giorno = 12;
    registro[0].mese = 12;
    registro[0].anno = 2012;
    registro[0].durata = 14;
    strcpy(registro[0].modello, "VolkiShark2");
    registro[0].costo = 7;
    strcpy(registro[0].nome_file, "VolkiShark1.jpg");

    /* Elemento 1: Y23CC */
    strcpy(registro[1].id, "Y23CC");
    registro[1].giorno = 23;
    registro[1].mese = 12;
    registro[1].anno = 2024;
    registro[1].durata = 7;
    strcpy(registro[1].modello, "Volki Shark");
    registro[1].costo = 14;
    strcpy(registro[1].nome_file, "VolkiShark2.jpg");

    /* Elemento 2: Y255C (Presente ma NON noleggiato: date a -1) */
    strcpy(registro[2].id, "Y255C");
    registro[2].giorno = -1;
    registro[2].mese = -1;
    registro[2].anno = -1;
    registro[2].durata = -1; /* Indica che è libero */
    strcpy(registro[2].modello, "Volki Shark2");
    registro[2].costo = 14;
    strcpy(registro[2].nome_file, "VolkiShark3.jpg");

    /* Elemento 3: 777CC */
    strcpy(registro[3].id, "777CC");
    registro[3].giorno = 23;
    registro[3].mese = 12;
    registro[3].anno = 2023;
    registro[3].durata = 7;
    strcpy(registro[3].modello, "Volki Shark");
    registro[3].costo = 14;
    strcpy(registro[3].nome_file, "VolkiShark4.jpg");

    /* Elemento 4: 999CC (Libero/Non noleggiato) */
    strcpy(registro[4].id, "999CC");
    registro[4].giorno = -1; // O 0, ma -1 è coerente con "vuoto"
    registro[4].mese = -1;
    registro[4].anno = -1;
    registro[4].durata = -1;
    strcpy(registro[4].modello, "Volki Shark");
    registro[4].costo = 14;
    strcpy(registro[4].nome_file, "VolkiShark5.jpg");

    dimensioneLogica = 5;

    /* RIEMPIMENTO RESTANTE DELL'ARRAY (Default: L / -1) */
    i = 5;
    while (i < N) {
        strcpy(registro[i].id, "L");
        registro[i].giorno = -1;
        registro[i].mese = -1;
        registro[i].anno = -1;
        registro[i].durata = -1;
        strcpy(registro[i].modello, "L");
        registro[i].costo = -1;
        strcpy(registro[i].nome_file, "L");
        
        i++;
    }



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
                while ((nread = RECV_STRING_STREAM(connfd, comando)) > 0) {
                    printf("richiesta arrivata %s\n", comando);
                    i = 0;
                    cont = 0;
                    while(i < dimensioneLogica) {
                        /* Se modello corrisponde E sci disponibile (durata == -1) */
                        if(strcmp(registro[i].modello, comando) == 0 && registro[i].durata == -1) {
                            printf("entrato qua con file %s\n", registro[i].nome_file);
                            /* Apertura file con OPEN (no fopen) */
                            fd_file = open(registro[i].nome_file, O_RDONLY);
                            if(fd_file < 0) {
                                printf("entrato qua con fd negativo\n");
                                perror("errore file");
                            } else {
                                  printf("entrato qua dentro il file\n");
                                /* Calcolo size con lseek (no fseek/ftell) */
                                file_size = lseek(fd_file, 0, SEEK_END);
                                lseek(fd_file, 0, SEEK_SET);
                              
                                write(connfd, &file_size, sizeof(long));
                                write(connfd, &registro[i], sizeof(Noleggio));
                                flag = 0;
                                cont = 0;
                                while (cont < file_size && flag == 0) {
                                    n_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                    nread = read(fd_file, buff, n_read);
                                    if (nread > 0) {
                                        write(connfd, buff, nread);
                                        cont += nread;
                                    } else flag = 1;
                                }
                                close(fd_file);
                            }
                        }
                        i++;
                    }
                    /* Fine lista */
                    file_size = -1;
                    write(connfd, &file_size, sizeof(long));
                    //FINE
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
            nread = recvfrom(udpfd, &buff, sizeof(buff), 0, (struct sockaddr *)&cliaddr, &len);
            if ((nread)>0) { // comando
                printf("ricevuto argomento %s \n", buff);
                res = -1; /* Valore di default (errore/non trovato) */
                trovato_udp = 0;
                i = 0;
                
                /* Ricerca lineare */
                while (i < dimensioneLogica && !trovato_udp) {
                    if (strcmp(registro[i].id, buff) == 0) {
                        trovato_udp = 1;
                        /* Calcolo costo solo se noleggiato (durata > 0) */
                        if (registro[i].durata > 0) {
                            res = registro[i].durata * registro[i].costo;
                        }
                        /* Se durata è -1 (libero), ris rimane -1 (errore logico per costo) */
                    } else {
                        i++;
                    }
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