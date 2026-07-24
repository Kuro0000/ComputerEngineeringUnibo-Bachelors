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
    /*variabili metodo1*/
    matricola_t mat_da_inviare; 
    int *esito_eliminazione;

    /* Variabili per metodo2 */
    int soglia_input;
    OutputLista *lista_risultato;
    int k, flag;

    /* --- CONTROLLI INIZIALI --- */
    if (argc < 2) {
        printf("Uso: %s <host>\n", argv[0]);
        exit(1);
    }
    host = argv[1];
    //MODIFICA---------------
    cl = clnt_create(host, GESTIONE_STUDENTI_PROG, GESTIONE_STUDENTI_VERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }
        printf("\n--- GESTIONE ESAMI ---\n");
        printf("Elimina: Elimina prenotazione\n");
        printf("Visualizza: Visualizza studenti con voto > soglia\n");
        printf("Inserisci scelta: ");

    while (gets(input_buffer)) {

            
            if (strcmp(input_buffer, "Elimina") == 0) {
                printf("Inserisci matricola da eliminare: ");
                gets(input_buffer);
                strcpy(mat_da_inviare, input_buffer);
                esito_eliminazione = elimina_prenotazione_1(mat_da_inviare, cl);

                if (esito_eliminazione == NULL) {
                    clnt_perror(cl, host);
                } else {
                    if (*esito_eliminazione > 0) {
                        printf("Successo: Prenotazione eliminata.\n");
                    } else {
                        printf("Errore: Matricola non trovata.\n");
                    }
                }
            } 
            /* SCELTA V: VISUALIZZA */
            else if (strcmp("Visualizza", input_buffer) == 0) {
                printf("Inserisci voto soglia: ");
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
                    soglia_input = atoi(input_buffer);

                        lista_risultato = visualizza_voto_maggiore_soglia_1(&soglia_input, cl);

                        if (lista_risultato == NULL) {
                            clnt_perror(cl, host);
                        } else {
                            
                        }
                }else{
                    printf("inserire intero valido riprovare");
                }
            } 
        
    }

    clnt_destroy(cl);
    return 0;
}