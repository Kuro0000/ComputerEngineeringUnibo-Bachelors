
#include "xfactor.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// MACRO UTILITY PER STRINGHE
#define STR(s) \
    ((s) == NULL ? NULL : ({ \
        char *src = (char *)(s); \
        size_t len = strlen(src) + 1; \
        char *dst = (char *)malloc(len); \
        if (dst != NULL) strcpy(dst, src); \
        dst; \
    }))

// MACRO PER CERCARE CANDIDATO
#define CERCA_CANDIDATO(nome_da_cercare) \
    ({ \
        int index_result = -1; \
        int i_counter = 0; \
        int trovato_flag = 0; \
        while (i_counter < db.size && trovato_flag == 0) { \
            if (strcmp(db.array[i_counter].nome, nome_da_cercare) == 0) { \
                index_result = i_counter; \
                trovato_flag = 1; \
            } \
            i_counter++; \
        } \
        index_result; \
    })

// Struttura dati interna
typedef struct {
    Candidato *array;    // Puntatore all'array dinamico
    int size;            // Numero elementi attuali
    int capacity;        // Capacità allocata
} Database;
//definisco un database comune in cui ogni volta che faccio un azione di scrittura
// di aggiunta se arrivo al limite aumento la dimensione, un idea che poteva essere alternativa
//poteva essere di utilizzare una lista e sfruttare i nodi visti a fondamenti di informatica 1
//oppure di utilizzare ogni volta che si aggiungeva un realloc
static Database db = { NULL, 0, 0 };

// 1. CLASSIFICA GIUDICI
ClassificaGiudici *classifica_giudici_1_svc(void *argp, struct svc_req *rqstp) {
    // VARIABILI TUTTE ALL'INIZIO
    static ClassificaGiudici result;
    int i, j, found, numGiudici, scambiato;
    char **nomiGiudici;
    int *votiGiudici;
    Giudice temp;
    
    // INIZIALIZZAZIONE DATI 
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
        
        db.array[db.size].nome = STR("Brasco");
        db.array[db.size].giudice = STR("Bowie");
        db.array[db.size].categoria = 'U';
        db.array[db.size].nomeFile = STR("brasco.txt");
        db.array[db.size].fase = 'A';
        db.array[db.size].voto = 100;
        db.size++;
        
        // Controllo resize (anche se capacity 10 basta)
        if (db.size >= db.capacity) {
            db.capacity *= 2;
            Candidato *temp_realloc = (Candidato *)realloc(db.array, db.capacity * sizeof(Candidato));
            if (temp_realloc != NULL) db.array = temp_realloc;
        }
        
        db.array[db.size].nome = STR("Viga");
        db.array[db.size].giudice = STR("Winehouse");
        db.array[db.size].categoria = 'D';
        db.array[db.size].nomeFile = STR("viga.txt");
        db.array[db.size].fase = 'S';
        db.array[db.size].voto = 50;
        db.size++;
        
        printf("Database inizializzato con %d candidati\n", db.size);
    }
    
    // Libera memoria risultato chiamata precedente 
    xdr_free((xdrproc_t)xdr_ClassificaGiudici, (char *)&result);
    
    // Se non ci sono candidati
    if (db.size == 0) {
        result.ClassificaGiudici_len = 0;
        result.ClassificaGiudici_val = NULL;
        return &result;
    }
    
    // Array temporanei
    nomiGiudici = (char **)malloc(db.size * sizeof(char *));
    votiGiudici = (int *)malloc(db.size * sizeof(int));
    numGiudici = 0;
    
    // Calcolo voti per giudice
    i = 0;
    while (i < db.size) {
        found = 0;
        j = 0;
        
        // Cerca se giudice già presente
        while (j < numGiudici && found == 0) {
            if (strcmp(nomiGiudici[j], db.array[i].giudice) == 0) {
                votiGiudici[j] += db.array[i].voto;
                found = 1;
            }
            j++;
        }
        
        // Se non trovato, aggiungi
        if (found == 0) {
            nomiGiudici[numGiudici] = STR(db.array[i].giudice);
            votiGiudici[numGiudici] = db.array[i].voto;
            numGiudici++;
        }
        i++;
    }
    
    // Alloca risultato RPC
    result.ClassificaGiudici_len = numGiudici;
    result.ClassificaGiudici_val = (Giudice *)malloc(numGiudici * sizeof(Giudice));
    
    // Copia dati
    i = 0;
    while (i < numGiudici) {
        result.ClassificaGiudici_val[i].nomeGiudice = nomiGiudici[i]; 
        result.ClassificaGiudici_val[i].punteggioTot = votiGiudici[i];
        i++;
    }
    
    // Bubble Sort 
    scambiato = 1;
    while (scambiato == 1) {
        scambiato = 0;
        i = 0;
        while (i < numGiudici - 1) {
            if (result.ClassificaGiudici_val[i].punteggioTot < 
                result.ClassificaGiudici_val[i+1].punteggioTot) {
                // Scambia
                temp = result.ClassificaGiudici_val[i];
                result.ClassificaGiudici_val[i] = result.ClassificaGiudici_val[i+1];
                result.ClassificaGiudici_val[i+1] = temp;
                scambiato = 1;
            }
            i++;
        }
    }
    
    // Libera array temporanei di puntatori (le stringhe sono passate a result)
    free(nomiGiudici);
    free(votiGiudici);
    
    return &result;
}

// 2. ESPRIMI VOTO
int *esprimi_voto_1_svc(InputVoto *argp, struct svc_req *rqstp) {
    static int result;
    int idx;
    
    // Inizializzazione 
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
    }
    
  
    idx = CERCA_CANDIDATO(argp->nomeCandidato);
    
    if (idx != -1) {
        if (argp->tipoOp == 'A') {
            db.array[idx].voto++;
            result = db.array[idx].voto;
        } else if (argp->tipoOp == 'S') {
            if (db.array[idx].voto > 0) {
                db.array[idx].voto--;
                result = db.array[idx].voto;
            } else {
                result = 0;
            }
        } else {
            result = -1;
        }
    } else {
        result = -1; // Candidato non trovato
    }
    
    return &result;
}

// 3. AGGIUNGI CANDIDATO
int *aggiungi_candidato_1_svc(Candidato *argp, struct svc_req *rqstp) {
    static int result;
    int idx;
    Candidato *temp;
    
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
    }
    
    
    idx = CERCA_CANDIDATO(argp->nome);
    
    if (idx != -1) {
        result = -1; // Esiste già
    } else {
        // Espansione se neccessario
        if (db.size >= db.capacity) {
            db.capacity *= 2;
            temp = (Candidato *)realloc(db.array, db.capacity * sizeof(Candidato));
            if (temp != NULL) {
                db.array = temp;
            }
        }
        
       
        db.array[db.size].nome = STR(argp->nome);
        db.array[db.size].giudice = STR(argp->giudice);
        db.array[db.size].categoria = argp->categoria;
        db.array[db.size].nomeFile = STR(argp->nomeFile);
        db.array[db.size].fase = argp->fase;
        db.array[db.size].voto = argp->voto;
        db.size++;
        
        result = 1;
        printf("Aggiunto candidato: %s\n", argp->nome);
    }
    
    return &result;
}

// 4. RIMUOVI CANDIDATO
int *rimuovi_candidato_1_svc(char **argp, struct svc_req *rqstp) {
    static int result;
    int idx, i;
    
    if (db.array == NULL) {
        result = -1;
        return &result;
    }
    
    idx = CERCA_CANDIDATO(argp[0]);
    
    if (idx != -1) {
        // Libera memoria delle stringhe interne
        free(db.array[idx].nome);
        free(db.array[idx].giudice);
        free(db.array[idx].nomeFile);
        
        // Sposta elementi (shift a sinistra)
        i = idx;
        while (i < db.size - 1) {
            db.array[i] = db.array[i + 1];
            i++;
        }
        
        db.size--;
        result = 1;
        printf("Rimosso candidato: %s\n", argp[0]);
    } else {
        result = -1;
    }
    
    return &result;
}

// 5. LISTA SOPRA SOGLIA
ListaCandidati *lista_sopra_soglia_1_svc(InputSoglia *argp, struct svc_req *rqstp) {
    static ListaCandidati result;
    int i, count, j;
    
    // Inizializzazione INLINE 
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
    }
    
    // Libera memoria precedente
    xdr_free((xdrproc_t)xdr_ListaCandidati, (char *)&result);
    
    // Conta
    count = 0;
    i = 0;
    while (i < db.size) {
        if (db.array[i].voto >= argp->soglia) {
            count++;
        }
        i++;
    }
    
    // Alloca
    result.ListaCandidati_len = count;
    result.ListaCandidati_val = (Candidato *)malloc(count * sizeof(Candidato));
    
    // Copia
    j = 0;
    i = 0;
    while (i < db.size) {
        if (db.array[i].voto >= argp->soglia) {
            result.ListaCandidati_val[j].nome = STR(db.array[i].nome);
            result.ListaCandidati_val[j].giudice = STR(db.array[i].giudice);
            result.ListaCandidati_val[j].categoria = db.array[i].categoria;
            result.ListaCandidati_val[j].nomeFile = STR(db.array[i].nomeFile);
            result.ListaCandidati_val[j].fase = db.array[i].fase;
            result.ListaCandidati_val[j].voto = db.array[i].voto;
            j++;
        }
        i++;
    }
    
    return &result;
}

// 6. LISTA PER CATEGORIA
ListaCandidati *lista_per_categoria_1_svc(InputCategoria *argp, struct svc_req *rqstp) {
    static ListaCandidati result;
    int i, count, j;
    
    // Inizializzazione INLINE 
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
    }
    
    xdr_free((xdrproc_t)xdr_ListaCandidati, (char *)&result);
    
    // Conta
    count = 0;
    i = 0;
    while (i < db.size) {
        if (db.array[i].categoria == argp->categoria) {
            count++;
        }
        i++;
    }
    
    result.ListaCandidati_len = count;
    result.ListaCandidati_val = (Candidato *)malloc(count * sizeof(Candidato));
    
    // Copia
    j = 0;
    i = 0;
    while (i < db.size) {
        if (db.array[i].categoria == argp->categoria) {
            result.ListaCandidati_val[j].nome = STR(db.array[i].nome);
            result.ListaCandidati_val[j].giudice = STR(db.array[i].giudice);
            result.ListaCandidati_val[j].categoria = db.array[i].categoria;
            result.ListaCandidati_val[j].nomeFile = STR(db.array[i].nomeFile);
            result.ListaCandidati_val[j].fase = db.array[i].fase;
            result.ListaCandidati_val[j].voto = db.array[i].voto;
            j++;
        }
        i++;
    }
    
    return &result;
}

// 7. CAMBIA FASE
int *cambia_fase_1_svc(InputFase *argp, struct svc_req *rqstp) {
    static int result;
    int idx;
    
    // Inizializzazione 
    if (db.array == NULL) {
        db.capacity = 10;
        db.array = (Candidato *)malloc(db.capacity * sizeof(Candidato));
        db.size = 0;
    }
    
    idx = CERCA_CANDIDATO(argp->nomeCandidato);
    
    if (idx != -1) {
        db.array[idx].fase = argp->nuovaFase;
        result = 1;
        printf("Fase cambiata per %s a %c\n", argp->nomeCandidato, argp->nuovaFase);
    } else {
        result = -1;
    }
    
    return &result;
}