/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LENGTH_CMD 256
typedef struct {
    char car;
    int occorrenze;
} Pack;
int main(int argc, char **argv) {
    struct hostent *host;
    struct sockaddr_in clientaddr, servaddr;
    int sd, nread, port, ret, len, occorrenze;
    char input, trash, occoChar[LENGTH_CMD], flag;
    Pack pack;

    if (argc != 3) { 
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
    clientaddr.sin_family = AF_INET;
    clientaddr.sin_addr.s_addr = INADDR_ANY;
    clientaddr.sin_port = 0;

    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { 
        printf("%s not found\n", argv[1]); 
        exit(EXIT_FAILURE);
    }
    nread = 0;
    while (argv[2][nread] != '\0') {
        if ((argv[2][nread] < '0') || (argv[2][nread] > '9')) {
            printf("Argomento non intero\n");
            exit(EXIT_FAILURE);
        }
        nread++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) {
        printf("%s = porta scorretta...\n", argv[2]);
        exit(EXIT_FAILURE);
    }
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);


    sd = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd < 0) { 
        perror("socket"); 
        exit(EXIT_FAILURE);
    }
        printf("Client: creata la socket sd=%d\n", sd);

    if (bind(sd, (struct sockaddr_in *)&clientaddr, sizeof(clientaddr)) < 0) {
        perror("bind"); 
        exit(EXIT_FAILURE);
    }
        printf("Client: bind socket ok, alla porta %i\n", clientaddr.sin_port);

    len = sizeof(servaddr);
        printf("Comando da eseguire: ");
    // NOTA: getchar() lascia il \n nel buffer. Bisogna consumarlo.
    while ((input = getchar()) != EOF) {
        // Consuma il resto della riga (incluso \n) lasciato da getchar
        while ((trash = getchar()) != '\n' && trash != EOF);
        pack.car = input;
        printf("Numero minimo occorrenze: ");
        (gets(occoChar)); // Controllo EOF su gets

        // Controllo che occoChar sia un numero
        nread = 0; flag = 0;
        while (occoChar[nread] != '\0') {
            if (occoChar[nread] < '0' || occoChar[nread] > '9') {
                flag = 1;
            }
            nread++;
        }
        if (flag == 1 || strlen(occoChar) == 0) {
            printf("Numero non valido.\n");
            printf("Carattere maiuscolo da cercare: ");
            continue;
        }

        occorrenze = atoi(occoChar);
        pack.occorrenze = occorrenze;
        
        if(sendto(sd, &pack, sizeof(Pack), 0, (struct sockaddr *)&servaddr, len)<0){
            perror("sendto");
            continue;
        }
        // Attesa risposta
        if (recvfrom(sd, &ret, sizeof(ret), 0, (struct sockaddr *)&servaddr, &len) < 0) {
            perror("recvfrom");
            continue;
        }
        
        if (ret == -1) printf("Errore sul server (directory non trovata o altro)\n");
        else printf("Linee trovate: %d\n", ret);

        printf("Carattere maiuscolo da cercare (CTRL+D per uscire): ");
    }
    
    close(sd);
    return EXIT_SUCCESS;
}
