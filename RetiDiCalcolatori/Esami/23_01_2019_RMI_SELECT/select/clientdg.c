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
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

/* Struttura richiesta */
typedef struct {
    char targa[8];
    char nuova_patente[6];
} ReqUDP;

int main(int argc, char **argv) {
    /* --- DICHIARAZIONE VARIABILI --- */
    int sd, port, len, ris;
    struct hostent *host;
    struct sockaddr_in servaddr;
    ReqUDP req;
    char input_buffer[100]; // Buffer temporaneo per input

    /* --- CONTROLLO ARGOMENTI --- */
    if (argc != 3) {
        printf("Error: %s serverAddress serverPort\n", argv[0]);
        exit(1);
    }

    /* --- PREPARAZIONE INDIRIZZO --- */
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { printf("Error: host not found\n"); exit(1); }
    
    port = atoi(argv[2]);
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    /* --- CREAZIONE SOCKET --- */
    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) { perror("socket"); exit(1); }

    printf("Client Datagram Pronto.\n");
    printf("Inserisci Targa (o EOF per uscire): ");

    /* --- CICLO RICHIESTE --- */
    while (gets(req.targa) != NULL) {
        
        printf("Inserisci Nuova Patente: ");
        if (gets(req.nuova_patente) == NULL) {
            // Se finisce input qui, esco
            // flag = 0 per uscire loop (simulato)
        } else {
            len = sizeof(servaddr);

            /* Invio Richiesta */
            if (sendto(sd, &req, sizeof(ReqUDP), 0, (struct sockaddr *)&servaddr, len) < 0) {
                perror("sendto");
            }

            /* Ricezione Risposta */
            if (recvfrom(sd, &ris, sizeof(int), 0, (struct sockaddr *)&servaddr, &len) < 0) {
                perror("recvfrom");
            } else {
                if (ris == 0) {
                    printf("Successo: Patente aggiornata per %s.\n", req.targa);
                } else {
                    printf("Errore: Targa %s non trovata o errore server.\n", req.targa);
                }
            }
        }
        printf("Inserisci Targa (o EOF per uscire): ");
    }

    close(sd);
    return 0;
}