const MAX_FILES = 20;      
const MAX_FILENAME = 20;    
const MAX_NAMELEN = 20;
const MAX_ERRMSG = 64;
/* Definisco tipi a dimensione fissa per evitare char* dinamici */
typedef char nome_t[MAX_FILENAME];
typedef char host_t[MAX_NAMELEN];
typedef char err_t[MAX_ERRMSG];

struct Info {
    nome_t nomefile; /* Diventa char nomefile[256] */
    long size;
};

struct Risposta {
    Info files[MAX_FILES];
    int num_files;
    int port;
    err_t errmsg;    /* Diventa char errmsg[128] */
};

struct PuntoClient {
    host_t host;
    int port;
    nome_t dirname;
};

program OPERATION {
    version OPERATIONVERS {
        Risposta GETLISTA(nome_t) = 1;        
        Risposta GETLISTA_SA(PuntoClient) = 2;  
    } = 1;
} = 0x20000013;