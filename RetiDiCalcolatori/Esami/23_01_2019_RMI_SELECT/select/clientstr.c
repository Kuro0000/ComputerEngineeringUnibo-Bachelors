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
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>

#define DIM_BUFF 256

int main(int argc, char **argv) {
    /* --- DICHIARAZIONE VARIABILI --- */
    int sd, port, fd_file, nread;
    long file_size, nleft, n_byte_letti;
    struct hostent *host;
    struct sockaddr_in servaddr;
    char targa[8];
    char nome_file[256]; // Dimensione sufficiente per d_name
    char buff[DIM_BUFF];

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

    /* --- CREAZIONE SOCKET E CONNESSIONE --- */
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { perror("socket"); exit(1); }
    
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("connect"); exit(1);
    }

    printf("Client Stream Connesso.\n");
    printf("Inserisci targa per scaricare le foto (o EOF per uscire): ");

    /* --- CICLO RICHIESTE --- */
    while (gets(targa) != NULL) {
        
        /* Invio Targa */
        if (write(sd, targa, sizeof(targa)) < 0) {
            perror("write targa");
            exit(1);
        }

        printf("Richiesta inviata per targa: %s. In attesa file...\n", targa);

        /* --- CICLO RICEZIONE FILE (Multiple-get) --- */
        /* Leggo dimensione file. Se -1, ho finito. */
        while (read(sd, &file_size, sizeof(long)) > 0) {
            
            if (file_size == -1) {
                printf("Fine ricezione file per questa targa.\n");
                /* Esco dal ciclo interno con un flag o forzando la condizione del while esterno se usassi un flag */
                /* Qui uso un artificio per uscire dal while interno ma restare in quello gets */
     
            } else {
                
                /* Leggo nome file */
                read(sd, nome_file, sizeof(nome_file));
                
                printf("Ricezione: %s (%ld byte)...\n", nome_file, file_size);

                /* Apro file locale (creo o sovrascrivo) */
                fd_file = open(nome_file, O_WRONLY | O_CREAT | O_TRUNC, 0644);
                if (fd_file < 0) { perror("open file locale"); }

                nleft = file_size;
                
                /* Leggo il contenuto del file */
                while (nleft > 0) {
                    /* Se nleft < DIM_BUFF leggo nleft, altrimenti DIM_BUFF */
                    n_byte_letti = read(sd, buff, (nleft < DIM_BUFF ? nleft : DIM_BUFF));
                    if (n_byte_letti > 0) {
                        write(fd_file, buff, n_byte_letti);
                        nleft -= n_byte_letti;
                    } else {
                        printf("Errore o fine imprevista lettura socket\n");
                        nleft = 0; // Esco
                    }
                }
                close(fd_file);
                printf("File %s salvato.\n", nome_file);
            }
            
        }

        printf("Inserisci targa per scaricare le foto (o EOF per uscire): ");
    }

    close(sd);
    return 0;
}