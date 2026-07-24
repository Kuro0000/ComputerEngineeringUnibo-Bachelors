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
    char input_buffer[100];
    nomeFile_t nomeFile;
    /* Variabili per Eliminazione */
    /* matricola_t è definito in .h come char[10], lo ridichiaro qui per chiarezza o uso il tipo */
    Inputdir input; 
    int *esito_eliminazione, occorrenza;
    char carattere, trash[100], direttorio[256];
    /* Variabili per Visualizzazione */
    OutputLista *lista_risultato;
    int k, flag;

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
        printf("Elimina: Elimina caratteri di un file\n");
        printf("Visualizza: nomi file con occorrenza di un carattere\n");
        printf("Inserisci scelta: \n");

    while (gets(input_buffer)) {

            
            if (strcmp(input_buffer, "Elimina") == 0) {
                printf("Inserisci nome del file da eliminare le occorrenze: ");
                gets(input_buffer);
                if (strlen(input_buffer) < 5 || strcmp(input_buffer+ strlen(input_buffer) - 4, ".txt") != 0) {
                            //salto se non è un file txt
                }else{
                    strcpy(nomeFile, input_buffer);
                    esito_eliminazione = elimina_occorrenze_1(nomeFile, cl);

                    if (esito_eliminazione == NULL) {
                        clnt_perror(cl, host);
                    } else {                    
                        printf("esito %d", *esito_eliminazione);

                    }
                }
            } 
            /* SCELTA V: VISUALIZZA */
            else if (strcmp("Visualizza", input_buffer) == 0) {
                 printf("Inserisci nome del direttorio: ");
                 gets(input_buffer);
                 if(strlen(input_buffer)==0){
                    printf("inserire un direttorio valido\n");
                    continue;
                 }
                 strcpy(direttorio, input_buffer);
                printf("Inserisci occorrenze soglia: ");
                gets(input_buffer);
                 k = 0;
                 flag = 0;
                while(input_buffer[k]!='\0'){
                    if(!(input_buffer[k]>='0' && input_buffer[k]<='9')){
                        flag = 1;
                    }
                    k++;
                }
                if(flag == 0 ){
                    occorrenza = atoi(input_buffer);
                    printf("inserire carattere\n");

                    carattere = getchar();
                    gets(trash);
                    input.carattere = carattere;
                    input.occorrenze = occorrenza;
                    strcpy(input.nomeDir, direttorio);
                    lista_risultato = lista_file_carattere_1(&input, cl);

                    if (lista_risultato == NULL) {
                        clnt_perror(cl, host);
                    } else {
                        k = 0;
                        while(k<lista_risultato->numero_file_trovati){
                            printf("nome file %d: %s\n", k, lista_risultato->nomeFile[k]);
                            k++;
                        }
                        
                    }
                }else{
                    printf("inserire intero valido riprovare");
                }
            } 
        printf("\n--- GESTIONE FILE ---\n");
        printf("Elimina: Elimina caratteri di un file\n");
        printf("Visualizza: nomi file con occorrenza di un carattere\n");
        printf("Inserisci scelta: \n");

        
    }

    clnt_destroy(cl);
    return 0;
}