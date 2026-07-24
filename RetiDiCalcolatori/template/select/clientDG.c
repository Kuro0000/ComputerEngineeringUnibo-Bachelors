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
    char comando[LENGTH_CMD];
    char argomenti[LENGTH_CMD];
} Pack;
int main(int argc, char **argv) {
    struct hostent *host;
    struct sockaddr_in clientaddr, servaddr;
    int sd, nread, port, ret, len;
    char input[LENGTH_CMD], argomenti[LENGTH_CMD];
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
        if ((argv[2][nread] < '0') || (argv[1][nread] > '9')) {
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
    while ( gets(input)) {
        strcpy(pack.comando,input);
        printf("Argomenti: ");
        gets(argomenti);
        strcpy(pack.argomenti, argomenti);
        if(sendto(sd, &pack, sizeof(Pack), 0, (struct sockaddr *)&servaddr, len)<0){
            perror("sendto");
            continue;
        }
        if(recvfrom(sd, &ret, sizeof(ret), 0, (struct sockaddr *)&servaddr, &len)<0){
            perror("receiveto");
            continue;
        }
        printf("Valore di ritorno: %d\n", ret);
        printf("Comando da eseguire o terminare: ");

    }
    close(sd);
    return EXIT_SUCCESS;
}
