/*
cognome: Yang
Nome: Andrea
Matricola: 0001077398
Compito: 
*/
#include "xfactor.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <rpc/rpc.h>

int main(int argc, char *argv[]) {
    /* --- DICHIARAZIONE VARIABILI (TUTTE IN TESTA) --- */
    CLIENT *cl;
    char *host;
    
    /* Buffer input utente */
    char input_buffer[256];//riutilizzo
    
    /* Variabili per Eliminazione */
    /* matricola_t è definito in .h come char[10], lo ridichiaro qui per chiarezza o uso il tipo */
    Inputfile inputfile;
    Inputdir inputdir; 
    int *esito, k;

    /* Variabili per Visualizzazione */
    OutputLista *lista_risultato;

    /* --- CONTROLLI INIZIALI --- */
    if (argc < 2) {
        printf("Uso: %s <host>\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, GESTIONE_FILE_PROG, GESTIONE_FILE_VERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }
        printf("\n--- GESTIONE FILE ---\n");
        printf("Conta: Conta occorrenze\n");
        printf("Visualizza: Visualizza file con prefisso\n");
        printf("Inserisci scelta: ");

    while (gets(input_buffer)) {
            if (strcmp(input_buffer, "Conta") == 0) {
                printf("Inserisci file da contare: ");
                gets(input_buffer);
                if(strlen(input_buffer) < 5 || strcmp(input_buffer + strlen(input_buffer) - 4, ".txt") != 0){
                    printf("Inserisci file valido");
                    continue;
                }
                strcpy(inputfile.nome, input_buffer);
                printf("Inserisci linea da contare: ");
                gets(input_buffer);
                if(strlen(input_buffer)==0 || strlen(input_buffer)>256){
                    printf("Inserisci linea valida");
                    continue;
                }
                strcpy(inputfile.linea, input_buffer);

                esito = conta_occorrenze_linea_1(&inputfile, cl);

                if (esito == NULL) {
                    clnt_perror(cl, host);
                } else {
                    
                    printf("linee contate %d.\n", esito);
                }
            } 
            /* SCELTA V: VISUALIZZA */
            else if (strcmp("Visualizza", input_buffer) == 0) {
                printf("Inserisci direttorio desiderato: ");
                gets(input_buffer);
                if(strlen(input_buffer)==0 ){
                    printf("Inserisci direttorio valido");
                    continue;
                }
                strcpy(inputdir.nomeDir, input_buffer);
                printf("Inserisci prefisso desiderato: ");
                gets(input_buffer);
                if(strlen(input_buffer)==0 || strlen(input_buffer)>10){
                    printf("Inserisci direttorio valido");
                    continue;
                }
                strcpy(inputdir.prefisso, input_buffer);
        

                lista_risultato = lista_file_prefisso_1(&inputdir, cl);

                if (lista_risultato == NULL) {
                    clnt_perror(cl, host);
                } else if(lista_risultato->numero_file_trovati!=-1){
                    k=0;
                    while(k<lista_risultato->numero_file_trovati){
                        printf("file %d: %s\n", k, lista_risultato->nomeFile[k]);
                        k++;
                    }
                }else{
                    printf("nessun file trovato");
                }
              
            } else{
                 printf("inserire operazione valida");
            }
        printf("\n--- GESTIONE FILE ---\n");
        printf("Conta: Conta occorrenze\n");
        printf("Visualizza: Visualizza file con prefisso\n");
        printf("Inserisci scelta: ");
        
    }

    clnt_destroy(cl);
    return 0;
}