#include "xfactor.h"
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>  
#include <arpa/inet.h>
#include <fcntl.h>
#include <netdb.h> 
#define DIM_BUFF 512

int main(int argc, char *argv[]) {
    // VARIABILI TUTTE ALL'INIZIO
    CLIENT *cl;
    Risposta *out;
    PuntoClient cli;
    char host[64], dir[256], buf[DIM_BUFF], scelta[10], myip[64];
    char my_hostname[256];
    int sfd, i, port, fd, n, cont, lfd, cfd, flag;
    int on = 1;
    long file_size_rpc, bytes_to_read;
    struct sockaddr_in serv, cliente;
    socklen_t clilen, slen;
    nome_t dir_rpc; 
    struct hostent *he;       // Per recuperare info host
    struct in_addr **addr_list; // Per la lista indirizzi IP
    clilen = sizeof(cliente);
    slen = sizeof(serv);

    if (argc != 2) {
        printf("Uso: %s <host_server>\n", argv[0]);
        exit(1);
    }

    cl = clnt_create(argv[1], OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(argv[1]);
        exit(1);
    }

    printf("Inserire operazione (client/server): ");
    while (gets(scelta)) {
        
        if (strcmp(scelta, "client") != 0 && strcmp(scelta, "server") != 0) {
            printf("Scelta non valida.\n");
            printf("Inserire operazione (client/server): ");
            continue;
        }

        printf("Inserire direttorio remoto: ");
        gets(dir); //controllo di dir è presente nel metodo remoto
        strcpy(dir_rpc, dir);

        // --- CLIENT ATTIVO ---
        if (strcmp(scelta, "client") == 0) {
            out = getlista_1(dir_rpc, cl);

            if (out == NULL) {
                clnt_perror(cl, argv[1]);
                exit(1);
            }

            if (strlen(out->errmsg) > 0) {
                printf("Errore dal server: %s\n", out->errmsg);
            } else {
                port = out->port;
                sfd = socket(AF_INET, SOCK_STREAM, 0);
                
                memset(&serv, 0, sizeof(serv));
                serv.sin_family = AF_INET;
                serv.sin_port = htons(port);
                inet_pton(AF_INET, argv[1], &serv.sin_addr);

                if (connect(sfd, (struct sockaddr*)&serv, sizeof(serv)) < 0) {
                    printf("Errore connessione socket\n");
                } else {
                    //chiusura soft
                    // Chiudo scrittura (protocollo unidirezionale)
                    shutdown(sfd, 1); 

                    // PROTOCOLLO BASATO SU DATI RPC
                    i = 0;
                    while (i < out->num_files) {
                        file_size_rpc = out->files[i].size;
                        
                     
                        if (file_size_rpc >= 0) {
                            printf("Ricezione %s (%ld bytes)...\n", out->files[i].nomefile, file_size_rpc);
                            
                            fd = open(out->files[i].nomefile, O_WRONLY | O_CREAT | O_TRUNC, 0666);
                            if (fd < 0) 
                                perror("Errore creazione file locale");
                            else{
                            cont = 0;
                            flag = 0; 
                            // Leggo esattamente i byte previsti dalla RPC
                            while (cont < file_size_rpc && flag == 0) {
                                bytes_to_read = (file_size_rpc - cont < DIM_BUFF) ? (file_size_rpc - cont) : DIM_BUFF;
                                n = read(sfd, buf, bytes_to_read);
                                
                                if (n > 0) {
                                    if (fd >= 0) 
                                        write(fd, buf, n);
                                    cont += n;
                                } else {
                                    // Connessione chiusa prematuramente o errore
                                    printf("Errore: connessione interrotta durante il download di %s\n", out->files[i].nomefile);
                                    flag=1;
                                }
                            }
                            close(fd);
                        }
                        }
                        i++;
                    }
                    shutdown(sfd, 0);
                    close(sfd);
                    printf("Trasferimento completato.\n");
                }
            }
        } 
        // --- SERVER ATTIVO ---
        else if (strcmp(scelta, "server") == 0) {
            lfd = socket(AF_INET, SOCK_STREAM, 0);
            setsockopt(lfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));
            
            memset(&serv, 0, sizeof(serv));
            serv.sin_family = AF_INET;
            serv.sin_addr.s_addr = INADDR_ANY;
            serv.sin_port = 0;

            if (bind(lfd, (struct sockaddr*)&serv, sizeof(serv)) < 0) {
                printf("Errore bind\n");
                close(lfd);
            } else {
                listen(lfd, 1);
                slen = sizeof(serv);
                getsockname(lfd, (struct sockaddr*)&serv, &slen);
                port = ntohs(serv.sin_port);

                // prendo il mio indirizzo ip
                if (gethostname(my_hostname, sizeof(my_hostname)) == 0) {
                    he = gethostbyname(my_hostname);
                    if (he != NULL && he->h_addr_list[0] != NULL) {
                        strcpy(myip, inet_ntoa(*(struct in_addr*)he->h_addr_list[0]));
                    } else {
                        strcpy(myip, "127.0.0.1");
                    }
                } else {
                    strcpy(myip, "127.0.0.1");
                }
                printf("Sto ascoltando sulla porta %d. Il mio IP rilevato è: %s\n", port, myip);

                cli.port = port;
                strcpy(cli.host, myip);
                strcpy(cli.dirname, dir);

                out = getlista_sa_1(&cli, cl);

                if (out == NULL) {
                    clnt_perror(cl, argv[1]);
                    close(lfd);
                    exit(1);
                }

                if (strlen(out->errmsg) > 0) {
                    printf("Errore dal server: %s\n", out->errmsg);
                } else {
                    cfd = accept(lfd, (struct sockaddr*)&cliente, &clilen);
                    close(lfd); 
                    
                    if (cfd < 0) {
                        printf("Errore accept\n");
                    } else {
                        shutdown(cfd, 1); // Chiudo scrittura

                        // PROTOCOLLO BASATO SU DATI RPC
                        i = 0;
                        while (i < out->num_files) {
                            file_size_rpc = out->files[i].size;

                            if (file_size_rpc >= 0) {
                                printf("Ricezione %s (%ld bytes)...\n", out->files[i].nomefile, file_size_rpc);

                                fd = open(out->files[i].nomefile, O_WRONLY | O_CREAT | O_TRUNC, 0666);
                                if (fd < 0) 
                                    perror("Errore creazione file locale");
                                else{
                                cont = 0;
                                flag = 0;
                                while (cont < file_size_rpc && flag == 0) {
                                    bytes_to_read = (file_size_rpc - cont < DIM_BUFF) ? (file_size_rpc - cont) : DIM_BUFF;
                                    n = read(cfd, buf, bytes_to_read);
                                    
                                    if (n > 0) {
                                        if (fd >= 0) 
                                            write(fd, buf, n);
                                        cont += n;
                                    } else {
                                        flag = 1; 
                                    }
                                }
                                 close(fd);
                                }
                            }
                            i++;
                        }
                        shutdown(cfd, 0);
                        close(cfd);
                        printf("Trasferimento completato.\n");
                    }
                }
            }
        }
        printf("\nInserire operazione (client/server) o ^D per uscire: ");
    }
    clnt_destroy(cl);
    return 0;
}