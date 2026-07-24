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
    
    /* Variabili per Eliminazione */
    /* matricola_t è definito in .h come char[10], lo ridichiaro qui per chiarezza o uso il tipo */
    matricola_t mat_da_inviare; 
    int *esito_eliminazione;

    OutputLista *lista_risultato;
    int k, flag;

    /* --- CONTROLLI INIZIALI --- */
    if (argc < 2) {
        printf("Uso: %s <host>\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, GESTIONE_PRENOTAZIONI_PROG, GESTIONE_PRENOTAZIONI_VERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }
        printf("\n--- GESTIONE ESAMI ---\n");
        printf("Elimina: Elimina prenotazione\n");
        printf("Visualizza: Visualizza prenotazioni\n");
        printf("Inserisci scelta: ");

    while (gets(input_buffer)) {

            
            if (strcmp(input_buffer, "Elimina") == 0) {
                printf("Inserisci matricola da eliminare: \n");
                gets(input_buffer);
                //CONTROLLI INPUT
                if(strlen(input_buffer)!=5){
                    printf("dimensioni della matricola sbagliata, riprovare\n");
                    
                    continue;
                }
                k = 0;
                flag = 0;
                while(k<5){
                    if(!(input_buffer[k]>='0' && input_buffer[k]<='9')){
                        flag = 1;
                    }
                    k++;
                }
                if(flag == 1){
                    printf("la matricola devono essere solo numero riprovare\n");
                    
                    continue;
                }
                strcpy(mat_da_inviare, input_buffer);
                esito_eliminazione = elimina_prenotazione_1(mat_da_inviare, cl);

                if (esito_eliminazione == NULL) {
                    clnt_perror(cl, host);
                } else {
                    printf("esito %i", *esito_eliminazione);
                }
            } 
            /* SCELTA V: VISUALIZZA */
            else if (strcmp("Visualizza", input_buffer) == 0) {
      

                lista_risultato = visualizza_multiple_1( (void*)0, cl);

                if (lista_risultato == NULL) {
                    clnt_perror(cl, host);
                } else {
                    k = 0;
                    while(k<lista_risultato->numero_prenotazioni_trovati){
                        printf("prenotazione %s con matricola %s\n", lista_risultato->prenotazione[k].id, lista_risultato->prenotazione[k].matricola);
                        k++;
                    }
                }
              
            } 
        
    }

    clnt_destroy(cl);
    return 0;
}