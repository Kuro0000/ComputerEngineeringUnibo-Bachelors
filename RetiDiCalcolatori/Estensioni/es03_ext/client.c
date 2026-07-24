#include <fcntl.h>
#include <netdb.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>
#include <dirent.h>

#define DIM_BUFF 256
#define LINE_LENGTH 256
//definisco le seguenti macro dato che le utilizzeremo spesso
#define SEND_STRING(sd, buf) write(sd, buf, LINE_LENGTH)
#define RECV_STRING(sd, buf) read(sd, buf, LINE_LENGTH)
#define SEND_LONG(sd, val) write(sd, &val, sizeof(long))
#define RECV_LONG(sd, val) read(sd, &val, sizeof(long))

int main(int argc, char *argv[]) {
    int sd, fd_file, nread, port, cont, nwrite, i, bytes_to_read, flag;
    char buff[DIM_BUFF], operazione[LINE_LENGTH], nome_dir[LINE_LENGTH], nome_file[LINE_LENGTH], file_path[DIM_BUFF];
    struct hostent *host;
    struct sockaddr_in servaddr;
    struct dirent *entry;
    DIR *dir;
    long file_size;

    if (argc != 3) { 
        printf("%s serverAddress serverPort\n", argv[0]); 
        exit(EXIT_FAILURE); 
    }
     memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));

    servaddr.sin_family = AF_INET;
    host = gethostbyname(argv[1]);
    if (host == NULL) { 
        printf("%s not found in /etc/hosts\n", argv[1]); 
        exit(EXIT_FAILURE); 
    }
    i = 0;
    while (argv[2][i] != '\0') {
        if ((argv[2][i] < '0') || (argv[2][i] > '9')) {
            printf("Secondo argomento non intero\n");
            exit(EXIT_FAILURE);
        }
        i++;
    }
    port = atoi(argv[2]);
    if (port < 1024 || port > 65535) { 
        printf("Porta scorretta!\n"); 
        exit(EXIT_FAILURE); 
    }
    servaddr.sin_addr.s_addr = ((struct in_addr *)(host->h_addr))->s_addr;
    servaddr.sin_port = htons(port);

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) { 
        perror("apertura socket"); 
        exit(EXIT_FAILURE); 
    }
    if (connect(sd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) { 
        perror("connect"); 
        exit(EXIT_FAILURE); 
    }
    printf("Client connesso\n");
      printf("Operazione (mget/mput, EOF per uscire): ");
    while(gets(operazione)) {
        if (strcmp(operazione, "mput") == 0) {
            SEND_STRING(sd, operazione);

            printf("Nome directory locale da inviare: ");
            gets(nome_dir);

            dir = opendir(nome_dir);
            if (dir == NULL) {
                perror("opendir");
                file_size = -1;
                SEND_LONG(sd, file_size); // Fine lista file
            }else{
            entry = readdir(dir);
            while (entry != NULL) {
                if (entry->d_type == DT_REG) {
                    strcpy(nome_file, entry->d_name);
                    sprintf(file_path, "%s/%s", nome_dir, nome_file);
                    if ((fd_file = open(file_path, O_RDONLY)) < 0) {
                        perror("apertura file");
                    } else {
                        file_size = lseek(fd_file, 0, SEEK_END);//uso lseek per il vedere la dimensione
                        lseek(fd_file, 0, SEEK_SET);//riporto il puntatore all'inizio
                        SEND_LONG(sd, file_size);
                        SEND_STRING(sd, nome_file);
                            cont = 0; flag = 0;
                            while (cont < file_size && flag == 0) {
                                bytes_to_read = (file_size - cont < DIM_BUFF) ? (file_size - cont) : DIM_BUFF;
                                nread = read(fd_file, buff, bytes_to_read);
                                if (nread > 0) {
                                    nwrite = write(sd, buff, nread);
                                    if (nwrite >= 0) cont += nread;
                                    else flag = 1;
                                } else 
                                    flag=1;
                            }
                            close(fd_file);
                            printf("Inviato: %s (%ld bytes)\n", nome_file, file_size);
                        
                    }
                }
                entry = readdir(dir);
            }
        
            closedir(dir);
            file_size = -1;
            SEND_LONG(sd, file_size); // Segnale di fine file
        }
        }
        else if (strcmp(operazione, "mget") == 0) {
            SEND_STRING(sd, operazione);

            printf("Nome directory da ricevere dal server: ");
            gets(nome_dir);
            SEND_STRING(sd, nome_dir);

            RECV_LONG(sd, file_size);
            if(file_size<0){
                printf("errore con il direttorio, continuo con le richieste");
          
            }else{
                while(file_size >= 0) {
                    RECV_STRING(sd, nome_file);
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
        else {
            printf("Operazione non supportata: %s\n", operazione);
        }
        printf("Operazione (mget/mput, EOF per uscire): ");
    }
    close(sd);
    printf("Disconnesso.\n");
    return EXIT_SUCCESS;
}
