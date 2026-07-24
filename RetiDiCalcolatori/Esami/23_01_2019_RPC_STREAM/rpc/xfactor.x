/* RPC_xFile.x */

/* Definisco tipi a dimensione fissa per evitare 'string' dinamiche */
typedef char targa_t[8];
typedef char patente_t[6];
typedef char tipo_t[10];
typedef char img_t[13];

 struct Prenotazione{
    targa_t targa;
    patente_t patente; 
    tipo_t tipo;   
    img_t img;
} ;

 struct Input{
    targa_t targa;
    patente_t patente;
} ;

struct OutputLista {
    struct Prenotazione prenotazioni[6];
    int numero_prenotazioni_trovati;
};

program GESTIONE_STUDENTI_PROG {
    version GESTIONE_STUDENTI_VERS {
        /* Procedura 1: Elimina. Input: matricola (array fisso), Output: int */
        int AGGIORNA_LICENZA(Input) = 1;

        /* Procedura 2: Visualizza. Input: int, Output: Lista fissa */
        OutputLista VISUALIZZA_PRENOTAZIONE(targa_t) = 2;
    } = 1;
} = 0x20000015;