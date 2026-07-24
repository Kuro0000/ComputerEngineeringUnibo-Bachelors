#include <arpa/inet.h>
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
#include <dirent.h>

#define DIM_BUFF 256
#define LINE_LENGTH 256
//definisco le seguenti macro dato che le utilizzeremo spesso
#define SEND_STRING(sd, buf) write(sd, buf, LINE_LENGTH)
#define RECV_STRING(sd, buf) read(sd, buf, LINE_LENGTH)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define RECV_LONG(sd, val) read(sd, &val, sizeof(long))

void gestore(int signo) {
    int stato;
    wait(&stato);
}

int main(int argc, char **argv) {
    int listen_sd, conn_sd, nread, port, len, fd_file, cont, nwrite, i, bytes_to_read, flag;
    const int on = 1;
    struct sockaddr_in cliaddr, servaddr;
    struct hostent *host;
    char operazione[LINE_LENGTH], nome_dir[LINE_LENGTH], nome_file[LINE_LENGTH];
    char buff[DIM_BUFF];
    long file_size;
    DIR *dir;
    struct dirent *entry;

    if (argc != 2) {
        printf("Error: %s port\n", argv[0]);
        exit(EXIT_FAILURE);
    }else{
        i = 0;
        while (argv[1][i] != '\0') {
            if ((argv[1][i] < '0') || (argv[1][i] > '9')) {
                printf("Argomento non intero\n");
                exit(EXIT_FAILURE);
            }
            i++;
        }
        port = atoi(argv[1]);
        if (port < 1024 || port > 65535) {
            printf("Porta scorretta...\n");
            exit(EXIT_FAILURE);
        }
    }
    memset((char *)&servaddr, 0, sizeof(servaddr));

    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = INADDR_ANY;
    servaddr.sin_port = htons(port);

    listen_sd = socket(AF_INET, SOCK_STREAM, 0);
    if (listen_sd < 0) {
        perror("socket");
        exit(EXIT_FAILURE);
    }
printf("Server: creata la socket d'ascolto per le richieste di ordinamento, fd=%d\n",
           listen_sd);    
        if (setsockopt(listen_sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("setsockopt");
        exit(EXIT_FAILURE);
    }
        printf("Server: set opzioni socket d'ascolto ok\n");

    if (bind(listen_sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("bind");
        exit(EXIT_FAILURE);
    }
        printf("Server: bind socket d'ascolto ok\n");

    if (listen(listen_sd, 5) < 0) {
        perror("listen");
        exit(EXIT_FAILURE);
    }
        printf("Server: listen ok\n");

    signal(SIGCHLD, gestore);

    for (;;) {
        len = sizeof(cliaddr);
        conn_sd = accept(listen_sd, (struct sockaddr *)&cliaddr, &len);
        if (conn_sd < 0) {
            if (errno == EINTR) {
                perror("forzo la continuazione della accept");
                continue;}
            else exit(EXIT_FAILURE);
        }

        if (fork() == 0) {
            close(listen_sd);
            host = gethostbyaddr((char *)&cliaddr.sin_addr, sizeof(cliaddr.sin_addr), AF_INET);
            if (host == NULL) {
                printf("Client: %s\n", inet_ntoa(cliaddr.sin_addr));
            } else {
                printf("Client: %s\n", host->h_name);
            }

            while (RECV_STRING(conn_sd, operazione) > 0) {
                if (strcmp(operazione, "mput") == 0) {
                    // ora sequenza di file: per ognuno si riceve size, nome, dati; se size < 0, fine
                    RECV_LONG(conn_sd, file_size);
                    while (file_size >= 0) {
                        RECV_STRING(conn_sd, nome_file);
                        fd_file = open(nome_file, O_WRONLY | O_CREAT | O_TRUNC, 0644);
                        if (fd_file < 0) {
                            perror("open file server");
                        } else {
                            cont = 0;
                            flag = 0;
                            while (cont < file_size && flag == 0) {
                                bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                nread = read(conn_sd, buff, bytes_to_read);
                                if (nread > 0) {
                                    nwrite = write(fd_file, buff, nread);
                                    if (nwrite >= 0) cont += nread;
                                    else flag = 1;
                                } else flag = 1;
                            }
                            close(fd_file);
                        }
                        printf("File \"%s\" ricevuto dal client. Bytes scritti: %d\n", nome_file, cont);
                        RECV_LONG(conn_sd, file_size); // prossimo file size o stop
                    }
                } else if (strcmp(operazione, "mget") == 0) {
                    RECV_STRING(conn_sd, nome_dir);
                    printf("ricevuto la cartella %s", nome_dir);
                    dir = opendir(nome_dir);
                    if (dir == NULL) {
                        file_size = -1;
                        SEND_LONG(conn_sd, file_size); // Fine subito, directory non trovata
                        printf("mandato messaggio not found");
                    } else {
                        while ((entry = readdir(dir)) != NULL) {
                            if (entry->d_type == DT_REG) {
                                strcpy(nome_file, entry->d_name);
                                sprintf(buff, "%s/%s", nome_dir, nome_file);
                                printf("inviando il seguente path %s", buff);
                                if((fd_file = open(buff, O_RDONLY))<0) {
                                    perror("apertura del file");
                                    continue;
                                } else {
                                    file_size = (long)lseek(fd_file, 0, SEEK_END); // prendo la dimensione del file portando il puntatore a fine file
                                   lseek(fd_file, 0, SEEK_SET);
                                    SEND_LONG(conn_sd, file_size);
                                    SEND_STRING(conn_sd, nome_file);
                                    
                                        cont = 0;
                                        flag = 0;
                                        while (cont < file_size && flag == 0) {
                                            bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                            nread = read(fd_file, buff, bytes_to_read);
                                            if (nread > 0) {
                                                nwrite = write(conn_sd, buff, nread);
                                                if (nwrite >= 0) cont += nread;
                                                else flag = 1;
                                            } else flag = 1;
                                        }
                                        close(fd_file);
                                    
                                    printf("File \"%s\" inviato. Bytes: %d\n", nome_file, cont);
                                }
                            }
                        }
                        closedir(dir);
                        file_size = -1;
                        SEND_LONG(conn_sd, file_size); // Segnale di fine files
                    }
                }
                printf("Operazione completata dal server\n");
            }
            close(conn_sd);
            exit(EXIT_SUCCESS);
        }
        close(conn_sd);
    }
    return EXIT_SUCCESS;
}
