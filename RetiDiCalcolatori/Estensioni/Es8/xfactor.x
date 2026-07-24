const NUM_GIUDICI = 12;
const MAX_NAME_SIZE = 128;

struct Giudice {
    char nomeGiudice[MAX_NAME_SIZE]; 
    int punteggioTot;
}; 

struct Output {
    Giudice classificaGiudici[NUM_GIUDICI]; 
}; 

struct Input {
    char nomeCandidato[MAX_NAME_SIZE];
    char tipoOp;
};

struct CandidatoInfo {
    char candidato[MAX_NAME_SIZE];
    char giudice[MAX_NAME_SIZE];
    char categoria[2];
    char nomeFile[FILENAME_MAX];
    char fase[10];
    int voto;
};

struct FiltroInput {
    char tipoFiltro;
    char valore[MAX_NAME_SIZE];
    int punteggioMin;
};

program OPERATION {
    version OPERATIONVERS {         
        Output CLASSIFICA_GIUDICI(void) = 1;        
        int ESPRIMI_VOTO(Input) = 2;
        int AGGIUNGI_CANDIDATO(CandidatoInfo) = 3;
        int RIMUOVI_CANDIDATO(Input) = 4;
        Output CANDIDATI_FILTRATI(FiltroInput) = 5;
    } = 1;
} = 0x20000013;