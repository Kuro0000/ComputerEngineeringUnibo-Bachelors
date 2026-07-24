/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include "xfactor.h"
#include <stdio.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>


int * conta_occorrenze_linea_1_svc(Inputfile *input, struct svc_req *rqstp) {
    static int result;
    char nomeFile[20];
    char linea[257];
    strcpy(nomeFile, input->nome);
    strcpy(linea, input->linea);
    int fd, nread, flag, tempCount;
    char c;
    if((fd=open(nomeFile, O_RDONLY))<0){
        result = -1;
    }else{
        result = 0;
        tempCount = 0;
        while((nread = read(fd, &c, 1))>0){
            if(linea[tempCount]==c){
                tempCount++;
                if(tempCount==strlen(linea)){
                    result++;
                }
            }else{
                tempCount=0;
            }
        }
    }
    close(fd);
    
    return &result;
}


OutputLista * lista_file_prefisso_1_svc(Inputdir *input, struct svc_req *rqstp) {
    static OutputLista result;
    memset(&result, 0, sizeof(OutputLista));
    char prefisso[10];
    char direttorio[20];
    int k;
    DIR *dir;
    struct dirent *entry;
    strcpy(direttorio, input->nomeDir);
    strcpy(prefisso, input->prefisso);


    dir = opendir(direttorio);
    if (dir == NULL) {
        /* Directory non trovata: invio -1  per terminare subito */
        printf("TCP: Cartella %s non trovata.\n", direttorio);
        result.numero_file_trovati=-1;
    } else {
        k = 0;
        /* Scorro i file nella directory */
        while ((entry = readdir(dir)) != NULL && k<6) {
            /* Salto . e .. */
            if (strcmp(entry->d_name, ".") != 0 && strcmp(entry->d_name, "..") != 0) {
                if (strstr(entry->d_name, prefisso) != entry->d_name || strlen(entry->d_name) < 5 || strcmp(entry->d_name + strlen(entry->d_name) - 4, ".txt") != 0) {
                     // Salta se il prefisso non è all'inizio o non è un file di testo
                }else{
                    strcpy(result.nomeFile[k], entry->d_name);
                    k++;
                }
            }
        }
        result.numero_file_trovati = k;
        closedir(dir);
        
    }

    return &result;
}