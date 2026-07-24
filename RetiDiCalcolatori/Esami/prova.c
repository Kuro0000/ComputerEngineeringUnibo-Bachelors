/**
 * server_select.c
 *  Il server discrimina due servizi con la select:
 *    + elimina le occorrenze di una parola in un file (UDP)
 *    + restituisce i nomi dei file in un direttorio di secondo livello
 **/

#include <arpa/inet.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <regex.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#define LINE_LENGTH 256
#define WORD_LENGTH 64
#define max(a, b)   ((a) > (b) ? (a) : (b))

/********************************************************/
void gestore(int signo) {
    int stato;
    printf("esecuzione gestore di SIGCHLD\n");
    wait(&stato);
}
/********************************************************/
typedef struct {
    char carattere;
    int occorrenze;
} udp_req_t;
/********************************************************/

int main(int argc, char **argv) {
    struct sockaddr_in cliaddr, servaddr;
    struct hostent    *hostTcp, *hostUdp;
    int                port, listen_sd, conn_sd, udp_sd, nread, maxfdp1, len;
    const int          on = 1;
    fd_set             rset;
    udp_req_t          req;
    int                fd_sorg_udp, fd_temp_udp;
    char               read_char, err[128];
    char           charBuff[2], newDir[LINE_LENGTH], fileNameTemp[LINE_LENGTH], dir[LINE_LENGTH];
    DIR           *dir1, *dir2, *dir3;
    struct dirent *dd1, *dd2;

    /* CONTROLLO ARGOMENTI ---------------------------------- */
    if (argc != 2) {
        printf("Error: %s port \n", argv[0]);
        exit(1);
    } else {
        nread = 0;
        while (argv[1][nread] != '\0') {
            if ((argv[1][nread] < '0') || (argv[1][nread] > '9')) {
                printf("Secondo argomento non intero\n");
                exit(2);
            }
            nread++;
        }
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535) {
            printf("Porta scorretta...");
            exit(2);
        }
    }

    /* INIZIALIZZAZIONE INDIRIZZO SERVER ----------------------------------------- */
    memset((char *)&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family      = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port        = htons(port);

    /* CREAZIONE E SETTAGGI SOCKET TCP --------------------------------------- */
    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("creazione socket ");
        exit(3);
    }
    printf("Server: creata la socket d'ascolto per le richieste di ordinamento, fd=%d\n",
           listen_sd);

    if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket d'ascolto");
        exit(3);
    }
    printf("Server: set opzioni socket d'ascolto ok\n");

    if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket d'ascolto");
        exit(3);
    }
    printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) {
        perror("listen");
        exit(3);
    }
    printf("Server: listen ok\n");

    /* CREAZIONE E SETTAGGI SOCKET UDP --------------------------------------- */
    udp_sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udp_sd < 0) {
        perror("apertura socket UDP");
        exit(4);
    }
    printf("Creata la socket UDP, fd=%d\n", udp_sd);

    if (setsockopt(udp_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("set opzioni socket UDP");
        exit(4);
    }
    printf("Set opzioni socket UDP ok\n");

    if (bind(udp_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind socket UDP");
        exit(4);
    }
    printf("Bind socket UDP ok\n");

    signal(SIGCHLD, gestore);

    /* PULIZIA E SETTAGGIO MASCHERA DEI FILE DESCRIPTOR ------------------------- */
    FD_ZERO(&rset);
    maxfdp1 = max(listen_sd, udp_sd) + 1;

    /* CICLO DI RICEZIONE RICHIESTE --------------------------------------------- */
    for (;;) {
        FD_SET(listen_sd, &rset);
        FD_SET(udp_sd, &rset);

        if ((nread = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) {
                continue;
            } else {
                perror("select");
                exit(5);
            }
        }
        /* GESTIONE RICHIESTE UDP  ----------------------------- */
        if (FD_ISSET(udp_sd, &rset)) {
            printf("Ricevuta richiesta di UDP: numero di righe che iniziano con un carattere maiuscolo \n");
            len = sizeof(struct sockaddr_in);

            if (recvfrom(udp_sd, &req, sizeof(req), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
                perror("recvfrom ");
                continue;
            }

            hostUdp = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (hostUdp == NULL) {
                printf("Operazione richiesta da: %s\n", inet_ntoa(cliaddr.sin_addr));
            } else {
                printf("Operazione richiesta da: %s %i\n", hostUdp->h_name,
                       (unsigned)ntohs(cliaddr.sin_port));
            }



            int ris = 0;
            
            if ((dir1 = opendir(".")) != NULL) { // direttorio presente
                
                printf("Invio risposta affermativa al client ciao\n");
                
                
                while ((dd1 = readdir(dir1)) != NULL) {
                    char last_read_char='\n';
                    strcpy(newDir,"");
                    if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0) {
                        // build new path
                        strcat(newDir, dd1->d_name);
                        printf("%s\n",newDir);
                        int fileris=0;
                        // se non è un direttorio, è un file da considerare
                        if ((dir3 = opendir(newDir)) == NULL)
                        { 
                            
                            if ((fd_sorg_udp = open(newDir, O_RDONLY)) < 0) {
                                
                                perror("Errore apertura file sorgente");
                                
                            }else{
                             printf("letuta %s\n",newDir);
                                while ((nread = read(fd_sorg_udp, &read_char, sizeof(char))) != 0) {
                                    printf("letutass\n");
                                    if (nread < 0) {
                                        sprintf(err, "(PID %d) impossibile leggere dal file", getpid());
                                        perror(err);
                                        
                                        break;
                                    } else {

                                        if (last_read_char == '\n') {
                                            if (read_char>='A' && read_char<='Z') { 
                                                ris++;
                                            }
                                            last_read_char=read_char;
                                        }
                                    }
                                }
                                /*
                                }*/
                                close(fd_sorg_udp);
                                printf("Fine lettutra file %s\n", newDir);
                                
                            }

                            
                        } 
                        
                    }  
                }  
            }else
            { // err apertura dir
                printf("Invio risposta negativa al client per dir %s \n", dir);
                write(conn_sd, &ris, sizeof(char));
            }

                           // if fork
            close(fd_sorg_udp);
            close(fd_temp_udp); // padre

            if (sendto(udp_sd, &ris, sizeof(ris), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                perror("sendto ");
                continue;
            }
        }
        /* GESTIONE RICHIESTE TCP  ----------------------------- */
        if (FD_ISSET(listen_sd, &rset)) {
            printf("Ricevuta richiesta TCP: file del direttorio secondo livello\n");
            len = sizeof(cliaddr);
            if ((conn_sd = accept(listen_sd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) {
                    perror("Forzo la continuazione della accept");
                    continue;
                } else {
                    perror("Error in accept: ");
                    exit(6);
                }
            }
            if (fork() == 0) {
                close(listen_sd);

                // Stampo a video le informazioni sul client
                hostTcp =
                    gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
                if (hostTcp == NULL) {
                    printf("Server (figlio): host client e' %s\n", inet_ntoa(cliaddr.sin_addr));
                } else {
                    printf("Server (figlio): host client e' %s \n", hostTcp->h_name);
                }

                // Leggo la richiesta del client
                while ((nread = read(conn_sd, dir, sizeof(dir))) > 0) {
                    printf("Server (figlio): direttorio richiesto: %s\n", dir);

                    char risp;
                    if ((dir1 = opendir(dir)) != NULL) { // direttorio presente
                        risp = 'S';
                        printf("Invio risposta affermativa al client\n");
                        write(conn_sd, &risp, sizeof(char));
                        while ((dd1 = readdir(dir1)) != NULL) {
                            if (strcmp(dd1->d_name, ".") != 0 && strcmp(dd1->d_name, "..") != 0) {
                                // build new path
                                sprintf(newDir, "%s/%s", dir, dd1->d_name);
                                if ((dir2 = opendir(newDir)) != NULL) { // dir sec livello
                                    while ((dd2 = readdir(dir2)) != NULL) {
                                        if (strcmp(dd2->d_name, ".") != 0 &&
                                            strcmp(dd2->d_name, "..") != 0)
                                        {
                                            // build new path
                                            strcat(newDir, "/");
                                            strcat(newDir, dd2->d_name);
                                            // se non è un direttorio, è un file da considerare
                                            if ((dir3 = opendir(newDir)) == NULL)
                                            { // file of sec livellov
                                                // Il nome del file viene inviato subito, appena
                                                // scoperto. Sarebbe un errore creare una grande
                                                // stringa con tutti i nomi e inviarla solo alla
                                                // fine!
                                                printf("Invio nome: %s\n", dd2->d_name);
                                                if (write(conn_sd, dd2->d_name,
                                                          (strlen(dd2->d_name) + 1)) < 0)
                                                {
                                                    perror("Errore nell'invio del nome file\n");
                                                    continue;
                                                }
                                            } // if file 2 livello
                                        }     // if not . and .. 2 livello
                                    }         // while in 2* livello
                                    printf("Fine invio\n");
                                } // if dir 2 livello
                            }     // if opendir 2 livello
                        }         // while frst livello
                        risp = '\1';
                        write(conn_sd, &risp, sizeof(char));
                    } // if open dir 1 livello
                    else
                    { // err apertura dir
                        risp = 'N';
                        printf("Invio risposta negativa al client per dir %s \n", dir);
                        write(conn_sd, &risp, sizeof(char));
                    }
                } // while read req

                // Libero risorse
                printf("Figlio TCP terminato, libero risorse e chiudo. \n");
                close(conn_sd);
                exit(0);
            }               // if fork
            close(conn_sd); // padre
        }                   // if TCP
    }                       // for
} // main