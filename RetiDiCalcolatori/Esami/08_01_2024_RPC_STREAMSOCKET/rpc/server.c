/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include "xfactor.h"
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <dirent.h>

#define DIM_BUFF 256
/* IMPLEMENTAZIONE PROCEDURA 1: Elimina */
int * elimina_occorrenze_1_svc(nomeFile_t nomeFile, struct svc_req *rqstp) {
    static int result;
    int fd, fd_out, nread, flag, cont;
    char c;
    //INIZIO

    fd = open(nomeFile, O_RDONLY);
    if (fd >= 0) {
        fd_out = open("temp", O_WRONLY | O_CREAT | O_TRUNC, 0666);
        if (fd_out >= 0) {
            result = -1;
            cont = 0;
            while((nread = read(fd, &c, 1))>0){
                if(c>='a' && c<='z' || c>'A' &&c<='Z'){
                    cont++;
                }else{
                    write(fd_out, &c, 1);
                }
            }
            close(fd_out);
            close(fd);
            
            if (rename("temp", nomeFile) == 0 && !(nread<0)) {
                result = cont; 
            } else {
                unlink("temp");
            }
        } else {
            close(fd);
        }
    }

    //FINE
    return &result;
}

/* IMPLEMENTAZIONE PROCEDURA 2: Visualizza */
OutputLista * lista_file_carattere_1_svc(Inputdir *input, struct svc_req *rqstp) {
    static OutputLista result;
    char dirname[DIM_BUFF], carattere, nome_file[DIM_BUFF];
    int occorrenze;
    occorrenze = input->occorrenze;
    strcpy(dirname, input->nomeDir);
    carattere = input->carattere;

    DIR *dir;
    struct dirent *entry;
    int cont, index, occ;
    //INIZIO-------------

    dir = opendir(dirname); 

    if (dir == NULL) {
        result.numero_file_trovati = -1;
    } else {
        index = 0;
        result.numero_file_trovati = 0;
        while ((entry = readdir(dir)) != NULL && index<7) {
            if (entry->d_type == DT_REG) {
                
                /* Controllo estensione .txt */
                cont = strlen(entry->d_name);
                if (cont < 4 || strcmp(entry->d_name + cont - 4, ".txt") != 0) {
                     /* Salto se non è .txt */
                }else{

                    strcpy(nome_file, entry->d_name);
                    
                    cont = 0;
                    occ = 0;
                    while(nome_file[cont] != '\0'){
                        if(nome_file[cont] == carattere){
                            occ++;
                        }
                        cont++;
                    }
                    if(occ >= occorrenze){
                        strcpy(result.nomeFile[index++],nome_file);
                        
                    }
           
                }
            }
        }
        result.numero_file_trovati = index;
        closedir(dir);
    }
    //FINE----------

    
    return &result;
}