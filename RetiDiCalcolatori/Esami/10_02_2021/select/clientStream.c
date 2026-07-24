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
#include <fcntl.h>
#define DIM_BUFF 256
#define LENGTH_CMD 256
#define DIM_STR 50

#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_STRING(sd, buf) write(sd, buf, LENGTH_CMD)
#define RECV_STRING(sd, buf) read(sd, buf, LENGTH_CMD)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define RECV_LONG(sd, val) read(sd, &val, sizeof(long))



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

int main(int argc, char *argv[]) {
    int sd, nread, port, fd_file,cont, flag,nwrite, bytes_to_read;
    long file_size;
    char input[LENGTH_CMD], nome_file[LENGTH_CMD], buff[LENGTH_CMD];
    struct hostent *host;
    struct sockaddr_in servaddr;
    Noleggio temp;
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
    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { 
        perror("socket"); 
        exit(EXIT_FAILURE);
    }
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("connect"); 
        exit(EXIT_FAILURE);
    }
    printf("input da eseguire: ");

    while (gets(input)) {
            SEND_STRING(sd, input);

            RECV_LONG(sd, file_size);
            if(file_size<0){
                printf("errore continuo con le richieste");
          
            }else{
                while(file_size >= 0) {
                    read(sd, &temp, sizeof(Noleggio));
                    printf("ricevuto %s libera ", temp.id);
                    strcpy(nome_file,temp.nome_file);
                    printf("Ricevo: %s (%ld bytes)\n", nome_file, file_size);

                    
                    if ((fd_file = open(nome_file, O_WRONLY | O_CREAT | O_TRUNC, 0644)) < 0) {
                        perror("open local file");
                    } else {
                        cont = 0; flag = 0;
                        while (cont < file_size && flag == 0) {
                            bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                            nread = read(sd, buff, bytes_to_read);
                            if (nread > 0) {
                                nwrite = write(fd_file, buff, nread);
                                if (nwrite >= 0) cont += nread;
                                else flag = 1;
                            } else flag=1;
                        }
                        close(fd_file);
                        printf("Ricevuto: %s (%d bytes)\n", nome_file, cont);
                    }
                    RECV_LONG(sd, file_size); // prossimo file o fine (-1)
                }
            }

    }
    close(sd);
    return EXIT_SUCCESS;
}
