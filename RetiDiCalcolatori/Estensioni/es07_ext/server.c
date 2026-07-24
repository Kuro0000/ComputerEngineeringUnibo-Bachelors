#include "xfactor.h"
#include <stdio.h>
#include <string.h>
#include <dirent.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <signal.h>

#define DIM_BUFF 512

void gestore(int signo) {
    int stato;
    wait(&stato);
}

// CLIENT ATTIVO
Risposta *getlista_1_svc(char *dirname, struct svc_req *r) {
    static Risposta out;
    struct dirent *entry;
    DIR *dir;
    int i, fd, lfd, pid, n, cont, k, cfd;
    struct sockaddr_in server, client;
    socklen_t slen, clen;
    char path[1024], buf[DIM_BUFF];
    int channel[2];
    int porta_ricevuta;
    int n_read_pipe;
    long file_size;
    int flag;
    int on = 1;

    memset(&out, 0, sizeof(Risposta));
    signal(SIGCHLD, gestore);
    
    dir = opendir(dirname);
    if (!dir) {
        strcpy(out.errmsg, "Directory inesistente");
        return &out;
    }

    // Popolamento struttura
    i = 0;
    while ((entry = readdir(dir)) != NULL && i < MAX_FILES) {
        if (entry->d_name[0] != '.') {
            strcpy(out.files[i].nomefile, entry->d_name);
            
            // Calcolo dimensione
            sprintf(path, "%s/%s", dirname, entry->d_name);
            fd = open(path, O_RDONLY);
            if (fd >= 0) {
                //seppur c non è un linguaggio tipizzato il cast del long è inserito per leggibilità
                //e evidenziare che la lunghezza del file è un long
                out.files[i].size = (long)lseek(fd, 0, SEEK_END);
                close(fd);
            } else {
                out.files[i].size = -1; // Segnalo errore
            }
            i++;
        }
    }
    out.num_files = i;
    closedir(dir);

    // Preparazione Socket
    if(pipe(channel) < 0){
        strcpy(out.errmsg, "Errore creazione pipe");
        return &out;
    }
     /*uso una pipe per comunicare la porta della socket al padre
    dato che da specifiche del progetto il server deve essere concorrente,
    quindi la porta non può essere statica, e che la socket deve essere creata
    e gestita dal figlio*/
    pid = fork();
    if (pid == 0) {
        // --- FIGLIO ---
        close(channel[0]);
        lfd = socket(AF_INET, SOCK_STREAM, 0);
        if (lfd < 0) {
            porta_ricevuta = -1;
            write(channel[1], &porta_ricevuta, sizeof(int));
            exit(1);
        }
        setsockopt(lfd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on));
        memset(&server, 0, sizeof(server));
        server.sin_family = AF_INET;
        server.sin_addr.s_addr = INADDR_ANY;
        server.sin_port = 0;
        
        if (bind(lfd, (struct sockaddr*)&server, sizeof(server)) < 0) {
            porta_ricevuta = -1;
            write(channel[1], &porta_ricevuta, sizeof(int));
            exit(1);
        }   
        listen(lfd, 1);
        
        slen = sizeof(server);
        getsockname(lfd, (struct sockaddr*)&server, &slen);
        porta_ricevuta = ntohs(server.sin_port);

        write(channel[1], &porta_ricevuta, sizeof(int));
        close(channel[1]); 

        clen = sizeof(client);
        cfd = accept(lfd, (struct sockaddr*)&client, &clen);
        close(lfd); // Non serve più ascoltare
        
        if(cfd >= 0){
            shutdown(cfd,0);
            //  Trasferimento 
            k = 0;
            while (k < out.num_files) {
                // Se la dimensione è valida, invio il file
                if (out.files[k].size >= 0) {
                    sprintf(path, "%s/%s", dirname, out.files[k].nomefile);
                    
                    if ((fd = open(path, O_RDONLY)) >= 0) {
                        cont = 0;
                        flag= 0;
                        file_size = out.files[k].size;
                        
                        while (cont < file_size && flag == 0) {
                            n = read(fd, buf, sizeof(buf));
                            if (n > 0) {
                                write(cfd, buf, n);
                                cont += n;
                            } else {
                                flag = 1;
                            } // EOF o errore
                        }
                        close(fd);
                    }
                }
                k++;
            }
            shutdown(cfd, 1);
            close(cfd);
        }
        exit(0);
    }
    
    // --- PADRE ---
    close(channel[1]); 
    n_read_pipe = read(channel[0], &porta_ricevuta, sizeof(int));
    close(channel[0]);
    if (n_read_pipe > 0 && porta_ricevuta > 0) {
        out.port = porta_ricevuta;
    } else {
        strcpy(out.errmsg, "Errore creazione socket nel figlio");
        out.port = -1;
    }
    return &out;
}

// SERVER ATTIVO
Risposta *getlista_sa_1_svc(PuntoClient *cli, struct svc_req *r) {
    static Risposta out;
    struct dirent *entry;
    DIR *dir;
    int i, fd, pid, n, cont, k, sfd, flag;
    struct sockaddr_in serv;
    char path[1024], buf[DIM_BUFF];
    long file_size;

    memset(&out, 0, sizeof(Risposta));
    signal(SIGCHLD, gestore);

    dir = opendir(cli->dirname);
    if (!dir) {
        strcpy(out.errmsg, "Directory inesistente");
        return &out;
    }

    i = 0;
    while ((entry = readdir(dir)) != NULL && i < MAX_FILES) {
        if (entry->d_name[0] != '.') {
            strcpy(out.files[i].nomefile, entry->d_name);
            
            sprintf(path, "%s/%s", cli->dirname, entry->d_name);
            fd = open(path, O_RDONLY);
            if (fd >= 0) {
                //seppur c non è un linguaggio tipizzato il cast del long è inserito per leggibilità
                //e evidenziare che la lunghezza del file è un long 
                out.files[i].size = (long)lseek(fd, 0, SEEK_END);
                close(fd);
            } else {
                out.files[i].size = -1;
            }
            i++;
        }
    }
    out.num_files = i;
    closedir(dir);

    pid = fork();
    if (pid == 0) {
        // --- FIGLIO ---
        sfd = socket(AF_INET, SOCK_STREAM, 0);
        memset(&serv, 0, sizeof(serv));
        serv.sin_family = AF_INET;
        serv.sin_port = htons(cli->port);
        inet_pton(AF_INET, cli->host, &serv.sin_addr);

        if (connect(sfd, (struct sockaddr*)&serv, sizeof(serv)) >= 0) {
            shutdown(sfd,0);
            k = 0;
            while (k < out.num_files) {
                if (out.files[k].size >= 0) {
                    sprintf(path, "%s/%s", cli->dirname, out.files[k].nomefile);
                    
                    if ((fd = open(path, O_RDONLY)) >= 0) {
                        cont = 0;
                        flag = 0;
                        file_size = out.files[k].size;
                        while (cont < file_size && flag ==0) {
                            n = read(fd, buf, sizeof(buf));
                            if (n > 0) {
                                write(sfd, buf, n);
                                cont += n;
                            } else {
                                flag = 1;
                            };
                        }
                        close(fd);
                    }
                }
                k++;
            }
            shutdown(sfd, 1);
        }
        close(sfd);
        exit(0);
    }

    return &out;
}