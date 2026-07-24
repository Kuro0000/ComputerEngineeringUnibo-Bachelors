/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <errno.h>
#include <fcntl.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <sys/select.h>
#include <sys/stat.h>

#define DIM_BUFF 256
#define N 10 // Numero massimo prenotazioni

/* Strutture Dati */
typedef struct {
    char targa[8];
    char patente[6]; // 5 interi + terminatore
    char tipo[10];   // camper o auto
} Prenotazione;

typedef struct {
    char targa[8];
    char nuova_patente[6];
} ReqUDP;

int main(int argc, char **argv) {
    /* --- DICHIARAZIONE VARIABILI --- */
    int listenfd, connfd, udpfd, nready, maxfdp1, port;
    int i, k, ris, len, fd_file, nread, cont, found;
    const int on = 1;
    long file_size;
    struct sockaddr_in cliaddr, servaddr;
    fd_set rset;
    char buff[DIM_BUFF];
    char nome_file[DIM_BUFF];
    char dir_path[DIM_BUFF];
    char targa_richiesta[8];
    pid_t pid;
    
    /* Variabili per la gestione directory */
    DIR *dir;
    struct dirent *entry;

    /* Variabili per UDP */
    ReqUDP req_udp;
    
    /* Struttura dati (Database) */
    Prenotazione tabella[N];

    /* --- INIZIALIZZAZIONE STRUTTURA DATI (Hardcoded per test) --- */
    /* Pulisco tutto a 'L' (libero) logicamente */
    i = 0;
    while(i < N){
        strcpy(tabella[i].targa, "L");
        strcpy(tabella[i].patente, "");
        strcpy(tabella[i].tipo, "");
        i++;
    }
    
    /* Inserisco dati di prova */
    strcpy(tabella[0].targa, "AA123BB");
    strcpy(tabella[0].patente, "11111");
    strcpy(tabella[0].tipo, "auto");

    strcpy(tabella[1].targa, "ED999XX");
    strcpy(tabella[1].patente, "22222");
    strcpy(tabella[1].tipo, "camper");

    /* --- CONTROLLO ARGOMENTI --- */
    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(1);
    }
    port = atoi(argv[1]);

    /* --- CREAZIONE SOCKET TCP --- */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) { perror("socket tcp"); exit(1); }
    if (setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("setsockopt tcp"); exit(1); }
    
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);

    if (bind(listenfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind tcp"); exit(1); }
    if (listen(listenfd, 5) < 0) { perror("listen"); exit(1); }

    /* --- CREAZIONE SOCKET UDP --- */
    udpfd = socket(AF_INET, SOCK_DGRAM, 0);
    if (udpfd < 0) { perror("socket udp"); exit(1); }
    if (setsockopt(udpfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) { perror("setsockopt udp"); exit(1); }
    if (bind(udpfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { perror("bind udp"); exit(1); }

    /* Gestione Zombie */
    signal(SIGCHLD, SIG_IGN);

    /* Pulizia Set */
    FD_ZERO(&rset);
    maxfdp1 = (listenfd > udpfd ? listenfd : udpfd) + 1;

    printf("Server avviato sulla porta %d. Attesa richieste...\n", port);

    /* --- CICLO PRINCIPALE --- */
    while(1) {
        FD_SET(listenfd, &rset);
        FD_SET(udpfd, &rset);

        if ((nready = select(maxfdp1, &rset, NULL, NULL, NULL)) < 0) {
            if (errno == EINTR) continue;
            else { perror("select"); exit(1); }
        }

        /* ----------------------------------------------------------- */
        /* GESTIONE UDP: Aggiornamento Patente (Seriale)               */
        /* ----------------------------------------------------------- */
        if (FD_ISSET(udpfd, &rset)) {
            len = sizeof(cliaddr);
            if (recvfrom(udpfd, &req_udp, sizeof(ReqUDP), 0, (struct sockaddr *)&cliaddr, &len) > 0) {
                printf("UDP: Richiesta cambio patente per targa %s\n", req_udp.targa);
                
                ris = -1;
                found = 0;
                i = 0;
                /* Ricerca nella tabella */
                while(i < N && found == 0) {
                    if (strcmp(tabella[i].targa, req_udp.targa) == 0) {
                        /* Trovato: Aggiorno */
                        strcpy(tabella[i].patente, req_udp.nuova_patente);
                        ris = 0;
                        found = 1;
                        printf("UDP: Patente aggiornata a %s per targa %s\n", req_udp.nuova_patente, req_udp.targa);
                    }
                    i++;
                }

                if (sendto(udpfd, &ris, sizeof(int), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                    perror("sendto udp");
                }
            }
        }

        /* ----------------------------------------------------------- */
        /* GESTIONE TCP: Download Foto (Parallelo)                     */
        /* ----------------------------------------------------------- */
        if (FD_ISSET(listenfd, &rset)) {
            len = sizeof(cliaddr);
            if ((connfd = accept(listenfd, (struct sockaddr *)&cliaddr, &len)) < 0) {
                if (errno == EINTR) continue;
                else { perror("accept"); exit(1); }
            }

            if ((pid = fork()) == 0) {
                /* FIGLIO */
                close(listenfd);
                
                printf("TCP (Figlio): Attesa targa...\n");

                /* Ciclo richieste sulla connessione */
                while (read(connfd, targa_richiesta, sizeof(targa_richiesta)) > 0) {
                    printf("TCP: Richiesta download per targa %s\n", targa_richiesta);

                    /* Costruisco il nome directory: <targa>_img */
                    sprintf(dir_path, "%s_img", targa_richiesta);

                    dir = opendir(dir_path);
                    if (dir == NULL) {
                        /* Directory non trovata: invio -1 come size per terminare subito */
                        printf("TCP: Cartella %s non trovata.\n", dir_path);
                        file_size = -1;
                        write(connfd, &file_size, sizeof(long));
                    } else {
                        /* Scorro i file nella directory */
                        while ((entry = readdir(dir)) != NULL) {
                            /* Salto . e .. */
                            if (strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0) {
                                
                                sprintf(nome_file, "%s/%s", dir_path, entry->d_name);
                                
                                /* Apro il file immagine */
                                fd_file = open(nome_file, O_RDONLY);
                                if (fd_file >= 0) {
                                    /* Calcolo dimensione */
                                    file_size = lseek(fd_file, 0, SEEK_END);
                                    lseek(fd_file, 0, SEEK_SET);

                                    /* PROTOCOLLO: 
                                       1. Invio dimensione (long)
                                       2. Invio nome file (char array)
                                       3. Invio contenuto
                                    */
                                    write(connfd, &file_size, sizeof(long));
                                    write(connfd, entry->d_name, sizeof(entry->d_name)); // Invio solo nome, non path

                                    printf("TCP: Invio file %s (%ld byte)\n", entry->d_name, file_size);

                                    /* Invio contenuto a blocchi */
                                    while ((nread = read(fd_file, buff, sizeof(buff))) > 0) {
                                        write(connfd, buff, nread);
                                    }
                                    close(fd_file);
                                }
                            }
                        }
                        closedir(dir);
                        
                        /* Terminatore sessione di invio file per questa targa */
                        file_size = -1;
                        write(connfd, &file_size, sizeof(long));
                        printf("TCP: Fine invio file per %s\n", targa_richiesta);
                    }
                }

                printf("TCP (Figlio): Chiusura connessione.\n");
                close(connfd);
                exit(0);
            }
            /* PADRE */
            close(connfd);
        }
    }
}




