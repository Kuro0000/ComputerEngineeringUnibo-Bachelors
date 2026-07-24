const MAX_NAME_SIZE = 64;
const MAX_FILE_SIZE = 128;

struct Candidato {
    string nome<MAX_NAME_SIZE>;
    string giudice<MAX_NAME_SIZE>;
    char categoria; /* U, D, O, B */
    string nomeFile<MAX_FILE_SIZE>;
    char fase; /* A, B, S */
    int voto;
};

/* Definisco liste dinamiche per i risultati */
typedef Candidato ListaCandidati<>;

struct Giudice {
    string nomeGiudice<MAX_NAME_SIZE>;
    int punteggioTot;
};

typedef Giudice ClassificaGiudici<>;

struct InputVoto {
    string nomeCandidato<MAX_NAME_SIZE>;
    char tipoOp; /* A o S */
};

struct InputSoglia {
    int soglia;
};

struct InputCategoria {
    char categoria;
};

struct InputFase {
    string nomeCandidato<MAX_NAME_SIZE>;
    char nuovaFase;
};

program OPERATION {
    version OPERATIONVERS {
        /* 1. Classifica Giudici */
        ClassificaGiudici CLASSIFICA_GIUDICI(void) = 1;
        
        /* 2. Esprimi Voto (ritorna nuovo voto o -1 se errore) */
        int ESPRIMI_VOTO(InputVoto) = 2;

        /* 3. Aggiungi Candidato (ritorna 1 se ok, -1 se esiste già) */
        int AGGIUNGI_CANDIDATO(Candidato) = 3;

        /* 4. Rimuovi Candidato (ritorna 1 se ok, -1 se non trovato) */
        int RIMUOVI_CANDIDATO(string) = 4;

        /* 5. Lista Candidati con punteggio > N */
        ListaCandidati LISTA_SOPRA_SOGLIA(InputSoglia) = 5;

        /* 6. Lista Candidati per Categoria */
        ListaCandidati LISTA_PER_CATEGORIA(InputCategoria) = 6;

        /* 7. Cambia Fase (promuove candidato) */
        int CAMBIA_FASE(InputFase) = 7;

    } = 1;
} = 0x20000013;