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

#define LENGTH_CMD 256
#define RECV_STRING_STREAM(sd, buf) read(sd, buf, LENGTH_CMD)
#define max(a, b)  ((a) > (b) ? (a) : (b))
typedef struct {
    char comando[LENGTH_CMD];
    char argomenti[LENGTH_CMD];
} Pack;
/*Si vuole sviluppare un semplice shell remoto per l’esecuzioni di
comandi sul nodo server
In particolare, si vuole realizzare un’applicazione C/S che fornisca
due servizi,
il primo realizzato utilizzando socket senza connessione
(datagram), mentre
il secondo utilizzando socket con connessione (stream)
Si dovranno quindi realizzare due client, e un server unico
multiservizio (uso di select)
DATAGRAM:
    Il primo servizio può essere gestito da un
    processo figlio con attesa sincrona di terminazione
    Per ogni richiesta ricevuta, il server (padre) genera un processo
    figlio per l’esecuzione del comando richiesto (fork), recupera il
    process id (pid) del figlio generato e si mette in attesa del risultato
    di tale figlio effettuando una waitpid. Alla terminazione del
    figlio, il padre recupera il valore di ritorno del comando e lo
    spedisce al client; e poi si mette in attesa di una nuova richiesta da
    servire in ordine
STREAM:
    Il secondo servizio viene gestito da un processo figlio senza
    attesa di terminazione del padre (gestione multiprocesso):
    per ogni richiesta ricevuta il server genera un processo figlio.
    Il padre, dopo aver lanciato il figlio, si mette in attesa di nuove
    richieste
*/


int main(int argc, char **argv) {
    int listenfd, connfd, udpfd, nready, maxfdp1, status, pid;
    const int on = 1;
    char comando[LENGTH_CMD], argomenti[LENGTH_CMD];
    char terminator = '\0';
    fd_set rset;
    int len, nread, port, res;
    struct sockaddr_in cliaddr, servaddr;
    Pack pack;

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
                    printf("ricevuto comando %s\n", comando);
                    nread = RECV_STRING_STREAM(connfd, argomenti);
                    printf("ricevuto argomenti %s\n", argomenti);
                    if(nread<0){
                        perror("receivefrom");
                    }
                    pid = fork();
                    if (pid == 0) { // nipote: ridireziona stdout su socket e lancia comando
                        close(1);
                        dup(connfd);
                        close(connfd);
                        if (strlen(argomenti) == 0)
                            execlp(comando, comando, (char *)0);
                        else
                            execlp(comando, comando, argomenti, (char *)0);
                        perror("exec:");
                        exit(EXIT_FAILURE);
                    } else { // figlio: attende solo
                        waitpid(pid, &status, 0);
                        write(connfd, &terminator, 1);
                        printf("terminato l'esecuzione\n");
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
            if ((nread)>0) { // comando
                strcpy(comando, pack.comando);
                printf("ricevuto comando %s\n", comando);
                strcpy(argomenti, pack.argomenti);
                printf("ricevuto argomento %s \n", argomenti);
                pid = fork();
                if (pid == 0) { // figlio serve la richiesta
                    if (strlen(argomenti) == 0)
                        execlp(comando, comando, (char *)0);
                    else
                        execlp(comando, comando, argomenti, (char *)0);
                    perror("exec");
                    exit(EXIT_FAILURE);
                } else if(pid>0) { // padre in attesa del figlio per poi mandare l'esito se 0 successo se -1 fallito
                    waitpid(pid, &status, 0);
                    if (WIFEXITED(status)) {
                        printf("figlio terminato correttamente");
                        res = WEXITSTATUS(status);
                    }
                    else 
                        res = -1;
                    printf("invio il risultato dell'operazione %d\n", res);
                    if(sendto(udpfd, &res, sizeof(res), 0, (struct sockaddr *)&cliaddr, len)<0){
                        perror("sendto");

                    }
                }else{
                    perror("errore fork");
                }
            }else{
                perror("receivefrom");
            }
            printf("fine operazione udp\n");

        }

    }
}