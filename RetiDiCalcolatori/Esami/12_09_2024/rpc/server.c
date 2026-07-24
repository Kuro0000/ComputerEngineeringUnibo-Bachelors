/* RPC_Server.c */
#include "xfactor.h"
#include <stdio.h>
#include <string.h>


static struct Studente tabella[6];
static int DIM_TABELLA = 6;
static int index = 0;
int inizializza(){
    if (index == 1)
        return 0;

    strcpy(tabella[0].matricola, "123456");
    strcpy(tabella[0].nome, "Mario");
    strcpy(tabella[0].cognome, "Rossi");
    tabella[0].voto = 28;

    strcpy(tabella[1].matricola, "654321");
    strcpy(tabella[1].nome, "Luigi");
    strcpy(tabella[1].cognome, "Bianchi");
    tabella[1].voto = 22;

    /* resto vuoto */
    for (index = 2; index < DIM_TABELLA; index++) {
        strcpy(tabella[index].matricola, "");
        strcpy(tabella[index].nome, "");
        strcpy(tabella[index].cognome, "");
        tabella[index].voto = 0;
    }

    index = 1;
    return 1;
}



/* IMPLEMENTAZIONE PROCEDURA 1: Elimina prenotazione */
/* L'argomento ora è un puntatore al typedef matricola_t (array di char) */
int * elimina_prenotazione_1_svc(char *matricola_input, struct svc_req *rqstp) {
    static int result;
    int i;
    inizializza();
    result = -1;
    i = 0;

    /* Nota: *matricola_input è l'array di char stesso */
    printf("Richiesta eliminazione per matricola: %s\n", matricola_input);

    while (i < DIM_TABELLA && result == -1) {
        /* Confronto stringhe */
        if (strcmp(tabella[i].matricola, matricola_input) == 0) {
            /* Cancellazione logica: copio stringa vuota negli array fissi */
            strcpy(tabella[i].matricola, "");
            strcpy(tabella[i].nome, "");
            strcpy(tabella[i].cognome, "");
            tabella[i].voto = 0;
            
            result = 1; /* Successo */
            printf("Eliminato studente indice %d\n", i);
        } else {
            i++;
        }
    }

    return &result;
}

/* IMPLEMENTAZIONE PROCEDURA 2: Visualizza voti sopra soglia */
OutputLista * visualizza_voto_maggiore_soglia_1_svc(int *soglia, struct svc_req *rqstp) {
    static OutputLista result;
    int i;
    int count;
    inizializza();

    /* Pulizia memoria statica: setto tutto a 0 */
    memset(&result, 0, sizeof(OutputLista));
    result.numero_studenti_trovati = 0;
    
    i = 0;
    count = 0;

    printf("Richiesta ricerca voti > %d\n", *soglia);

    while (i < DIM_TABELLA && count < 5) {
        /* Se matricola non è vuota (studente esiste) */
        if (strcmp(tabella[i].matricola, "") != 0) {
             if (tabella[i].voto > *soglia) {
                
                /* COPIA DEI DATI: Ora sono array, DEVO usare strcpy */
                /* Non posso fare result...nome = tabella...nome */
                strcpy(result.studenti[count].nome, tabella[i].nome);
                strcpy(result.studenti[count].cognome, tabella[i].cognome);
                strcpy(result.studenti[count].matricola, tabella[i].matricola);
                result.studenti[count].voto = tabella[i].voto;
                
                count++;
             }
        }
        i++;
    }

    result.numero_studenti_trovati = count;
    return &result;
}