#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#define LENGTH_CMD 256

int main(int argc, char *argv[]) {
    int sd, nread, port;
    char comando[LENGTH_CMD], argomenti[LENGTH_CMD], c;
    struct hostent *host;
    struct sockaddr_in servaddr;
    if (argc != 3) {
        printf("Error:%s serverAddress serverPort\n", argv[0]);
        exit(EXIT_FAILURE);
    }
    memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { 
        printf("not found\n"); 
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
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { 
        perror("socket"); 
        exit(EXIT_FAILURE);
    }
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("connect"); 
        exit(EXIT_FAILURE);
    }
    printf("Comando da eseguire: ");

    while (gets(comando)) {
        printf("Argomenti: ");
        gets(argomenti);
        if(write(sd, comando, LENGTH_CMD)<0){
            printf("errore, riprova a scrivere il comando:");
            continue;
        }
        if(write(sd, argomenti, LENGTH_CMD)<0){
            printf("errore, riprova a scrivere il comando:");
            continue;
        }
        while ((nread = read(sd, &c, 1)) > 0 && c != '\0') 
            write(1, &c, 1);
        printf("Comando da eseguire oppure uscire: ");

    }
    close(sd);
    return EXIT_SUCCESS;
}
