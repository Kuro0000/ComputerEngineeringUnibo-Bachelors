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
    /* matricola_t è definito in .h come char[10], lo ridichiaro qui per chiarezza o uso il tipo */
    matricola_t mat_da_inviare; 
    int *esito_eliminazione;

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
        printf("\n--- GESTIONE ESAMI ---\n");
        printf("Elimina: Elimina prenotazione\n");
        printf("Visualizza: Visualizza studenti con voto > soglia\n");
        printf("Inserisci scelta: ");

    while (gets(input_buffer)) {

            
            if (strcmp(input_buffer, "Elimina") == 0) {
                printf("Inserisci matricola da eliminare: ");
                gets(input_buffer);
                
                /* Copio l'input nel buffer fisso del tipo matricola_t */
                /* Assicuro terminazione stringa copiando max 9 char */
                strcpy(mat_da_inviare, input_buffer);

                /* Passo l'indirizzo dell'array (che rpcgen si aspetta come puntatore al tipo) */
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
                    if(soglia_input>=1 && soglia_input<=33){

                        lista_risultato = visualizza_voto_maggiore_soglia_1(&soglia_input, cl);

                        if (lista_risultato == NULL) {
                            clnt_perror(cl, host);
                        } else {
                            printf("\nRisultati trovati: %d\n", lista_risultato->numero_studenti_trovati);
                            
                            k = 0;
                            while (k < lista_risultato->numero_studenti_trovati) {
                                /* Accesso diretto ai campi array */
                                printf("- %s %s (Matr: %s) : Voto %d\n",
                                    lista_risultato->studenti[k].nome,
                                    lista_risultato->studenti[k].cognome,
                                    lista_risultato->studenti[k].matricola,
                                    lista_risultato->studenti[k].voto);
                                k++;
                            }
                        }
                    }else{
                        printf("inserire una soglia valida");
                    }
                }else{
                    printf("inserire intero valido riprovare");
                }
            } 
        printf("\n--- GESTIONE ESAMI ---\n");
        printf("Elimina: Elimina prenotazione\n");
        printf("Visualizza: Visualizza studenti con voto > soglia\n");
        printf("Inserisci scelta: ");
        
    }

    clnt_destroy(cl);
    return 0;
}