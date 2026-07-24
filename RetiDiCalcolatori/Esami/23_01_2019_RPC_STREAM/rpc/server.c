/* RPC_Server.c */
#include "xfactor.h"
#include <stdio.h>
#include <string.h>

/* STRUTTURA DATI GLOBALE (Database statico) */
/* Inizializzo la tabella. Essendo array char fissi, l'inizializzazione statica funziona così */
static struct Prenotazione tabella[6];
static int inizializzato = 0;

static int DIM_TABELLA = 6;


int inizializza(){
    if (inizializzato == 1)
        return 0;

    strcpy(tabella[0].targa, "AA123BB");
    strcpy(tabella[0].patente, "11111");
    strcpy(tabella[0].tipo, "auto");
    strcpy(tabella[0].img, "AA123BB_img");



    strcpy(tabella[1].targa, "ED999XX");
    strcpy(tabella[1].patente, "22222");
    strcpy(tabella[1].tipo, "auto");
    strcpy(tabella[1].img, "ED999XX_img");

    
    strcpy(tabella[1].targa, "ED888XX");
    strcpy(tabella[1].patente, "33333");
    strcpy(tabella[1].tipo, "camper");
    strcpy(tabella[1].img, "ED888XX_img");

    strcpy(tabella[1].targa, "AA223CB");
    strcpy(tabella[1].patente, "44444");
    strcpy(tabella[1].tipo, "auto");
    strcpy(tabella[1].img, "AA223CB_img");

    strcpy(tabella[1].targa, "ED555CB");
    strcpy(tabella[1].patente, "55555");
    strcpy(tabella[1].tipo, "camper");
    strcpy(tabella[1].img, "ED555CB_img");

    strcpy(tabella[1].targa, "EB578CB");
    strcpy(tabella[1].patente, "66666");
    strcpy(tabella[1].tipo, "auto");
    strcpy(tabella[1].img, "EB578CB_img");
    inizializzato = 1;
    return 1;
}

/* IMPLEMENTAZIONE PROCEDURA 1: Elimina prenotazione */
/* L'argomento ora è un puntatore al typedef matricola_t (array di char) */
int * aggiorna_licenza_1_svc(Input *input, struct svc_req *rqstp) {
    static int result;
    int i;
    char targa[8];
    char patente[6];
    strcpy(targa, &input->targa);
    strcpy(patente, &input->patente);
    inizializza();
    result = -1;
    i = 0;

    /* Nota: *matricola_input è l'array di char stesso */
    printf("Richiesta aggiornamento per patente: %s\n", &input->targa);

    while (i < DIM_TABELLA && result == -1) {
        /* Confronto stringhe */
        if (strcmp(tabella[i].targa, targa) == 0) {
            /* Cancellazione logica: copio stringa vuota negli array fissi */
            strcpy(tabella[i].patente, patente);
            
            result = 1; /* Successo */
            printf("Aggiornato patente indice %d\n", i);
        } else {
            i++;
        }
    }

    return &result;
}

/* IMPLEMENTAZIONE PROCEDURA 2: Visualizza voti sopra soglia */
OutputLista * visualizza_prenotazione_1_svc(char *targa, struct svc_req *rqstp) {
    static OutputLista result;
    int i;
    int count;
    inizializza();
    /* Pulizia memoria statica: setto tutto a 0 */
    memset(&result, 0, sizeof(OutputLista));
    result.numero_prenotazioni_trovati = 0;
    
    i = 0;
    count = 0;

    printf("Richiesta ricerca tipo > %s\n", targa);

    while (i < DIM_TABELLA && count < 6) {
        if (strcmp(tabella[i].tipo, targa) == 0) {
             if (tabella[i].targa[0] >='E' && tabella[i].targa[1] >= 'D') {
                
                /* COPIA DEI DATI: Ora sono array, DEVO usare strcpy */
                /* Non posso fare result...nome = tabella...nome */
                strcpy(result.prenotazioni[count].targa, tabella[i].targa);
                strcpy(result.prenotazioni[count].patente, tabella[i].patente);
                strcpy(result.prenotazioni[count].tipo, tabella[i].tipo);
                strcpy(result.prenotazioni[count].img, tabella[i].img);
                
                count++;
             }
        }
        i++;
    }

    result.numero_prenotazioni_trovati = count;
    return &result;
}