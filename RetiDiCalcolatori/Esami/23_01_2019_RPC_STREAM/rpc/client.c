/* RPC_Client.c */
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
    char scelta_char;
    int condition_loop;
    
    /* Variabili per Eliminazione */
    Input input; 
    int *esito;

    /* Variabili per Visualizzazione */
    int soglia_input;
    OutputLista *lista_risultato;
    int k, flag;

    /* --- CONTROLLI INIZIALI --- */
    if (argc < 2) {
        printf("Uso: %s <host>\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, GESTIONE_STUDENTI_PROG, GESTIONE_STUDENTI_VERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }
        printf("\n--- GESTIONE PRENOTAZIONI ---\n");
        printf("Aggiorna: Aggiorna prenotazione\n");
        printf("Visualizza: Visualizza studenti con voto > soglia\n");
        printf("Inserisci scelta: ");

    while (gets(input_buffer)) {

            
            if (strcmp(input_buffer, "Aggiorna") == 0) {
                printf("Inserisci Targa da aggiornare: ");
                gets(input_buffer);
                
                strcpy(input.targa, input_buffer);
                    printf("Inserisci patente da aggiornare: ");

             gets(input_buffer);
                
                strcpy(input.patente, input_buffer);

                /* Passo l'indirizzo dell'array (che rpcgen si aspetta come puntatore al tipo) */
                esito = aggiorna_licenza_1(&input, cl);

                if (esito == NULL) {
                    clnt_perror(cl, host);
                } else {
                    if (*esito > 0) {
                        printf("Successo: Prenotazione eliminata.\n");
                    } else {
                        printf("Errore: Prenotazione non trovata.\n");
                    }
                }
            } 
            /* SCELTA V: VISUALIZZA */
            else if (strcmp("Visualizza", input_buffer) == 0) {
                printf("Inserisci targa soglia: ");
                gets(input_buffer);

                        lista_risultato = visualizza_prenotazione_1(&input_buffer, cl);

                        if (lista_risultato == NULL) {
                            clnt_perror(cl, host);
                        } else {
                            printf("\nRisultati trovati: %d\n", lista_risultato->numero_prenotazioni_trovati);
                           if(lista_risultato->numero_prenotazioni_trovati!= 0){ 
                                k = 0;
                                while (k < lista_risultato->numero_prenotazioni_trovati) {
                                    /* Accesso diretto ai campi array */
                                    printf("- %s %s  %s  %s\n",
                                        lista_risultato->prenotazioni[k].targa,
                                        lista_risultato->prenotazioni[k].patente,
                                        lista_risultato->prenotazioni[k].tipo,
                                        lista_risultato->prenotazioni[k].img);
                                    k++;
                                }
                            }
                        }
            } 
        printf("\n--- GESTIONE Prenotazioni ---\n");
        printf("Aggiorna: aggiorna prenotazione\n");
        printf("Visualizza: Visualizza studenti con voto > soglia\n");
        printf("Inserisci scelta: ");
        
    }

    clnt_destroy(cl);
    return 0;
}