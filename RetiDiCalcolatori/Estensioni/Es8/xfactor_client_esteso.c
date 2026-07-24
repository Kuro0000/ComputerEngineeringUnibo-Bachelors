#include "xfactor.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
    char *host;
    CLIENT *cl;
    int *ris;
    Output *classificaGiudici;
    Output *candidatiFiltrati;
    char ok[2], nl[2];
    Input input;
    CandidatoInfo nuovoCandidato;
    FiltroInput filtro;
    int i;

    if (argc != 2) exit(1);
    host = argv[1];
    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) exit(1);

    printf("Inserire:\n1 Classifica Giudici\n2 Esprimi voto\n3 Aggiungi candidato\n4 Rimuovi candidato\n5 Candidati filtrati\n^D per terminare: ");
    
    while(gets(ok)) {
        gets(nl);
        if (strcmp(ok,"1") == 0) {
            classificaGiudici = classifica_giudici_1(NULL,cl);
            printf("=== CLASSIFICA GIUDICI ===\n");
            for(i=0;i<NUM_GIUDICI;i++) {
                if(strcmp(classificaGiudici->classificaGiudici[i].nomeGiudice,"L")!=0 &&
                    classificaGiudici->classificaGiudici[i].punteggioTot>=0)
                    printf("%s con %d voti\n",
                        classificaGiudici->classificaGiudici[i].nomeGiudice,
                        classificaGiudici->classificaGiudici[i].punteggioTot);
            }
        } else if (strcmp(ok,"2")==0) {
            printf("Nome candidato: ");
            gets(input.nomeCandidato);
            printf("Operazione (A/S): ");
            gets(nl); input.tipoOp = nl[0];
            ris = esprimi_voto_1(&input,cl);
            if(ris == NULL) exit(1);
            if(*ris<0) printf("Problemi nel voto\n");
            else printf("Votazione OK\n");
        } else if (strcmp(ok,"3")==0) {
            printf("Nome candidato: ");
            gets(nuovoCandidato.candidato);
            printf("Giudice: ");
            gets(nuovoCandidato.giudice);
            printf("Categoria (U/D/O/B): ");
            gets(nl); nuovoCandidato.categoria[0] = nl[0]; nuovoCandidato.categoria[1] = '\0';
            printf("Nome file: ");
            gets(nuovoCandidato.nomeFile);
            printf("Fase (A/B/S): ");
            gets(nuovoCandidato.fase);
            printf("Voto iniziale: ");
            gets(nl); nuovoCandidato.voto = atoi(nl);
            
            ris = aggiungi_candidato_1(&nuovoCandidato,cl);
            if(ris == NULL) exit(1);
            if(*ris<0) printf("Problemi nell'aggiunta\n");
            else printf("Candidato aggiunto OK\n");
        } else if (strcmp(ok,"4")==0) {
            printf("Nome candidato da rimuovere: ");
            gets(input.nomeCandidato);
            ris = rimuovi_candidato_1(&input,cl);
            if(ris == NULL) exit(1);
            if(*ris<0) printf("Candidato non trovato\n");
            else printf("Candidato rimosso OK\n");
        } else if (strcmp(ok,"5")==0) {
            printf("Tipo filtro (C=categoria, P=punteggio, G=giudice): ");
            gets(nl); filtro.tipoFiltro = nl[0];
            
            if(filtro.tipoFiltro == 'C') {
                printf("Categoria (U/D/O/B): ");
                gets(filtro.valore);
            } else if(filtro.tipoFiltro == 'P') {
                printf("Punteggio minimo: ");
                gets(nl); filtro.punteggioMin = atoi(nl);
            } else if(filtro.tipoFiltro == 'G') {
                printf("Nome giudice: ");
                gets(filtro.valore);
            }
            
            candidatiFiltrati = candidati_filtrati_1(&filtro,cl);
            printf("=== CANDIDATI FILTRATI ===\n");
            for(i=0;i<NUM_GIUDICI;i++) {
                if(strcmp(candidatiFiltrati->classificaGiudici[i].nomeGiudice,"L")!=0 &&
                    candidatiFiltrati->classificaGiudici[i].punteggioTot>=0)
                    printf("%s - %d voti\n",
                        candidatiFiltrati->classificaGiudici[i].nomeGiudice,
                        candidatiFiltrati->classificaGiudici[i].punteggioTot);
            }
        }
        printf("\nInserire:\n1 Classifica Giudici\n2 Esprimi voto\n3 Aggiungi candidato\n4 Rimuovi candidato\n5 Candidati filtrati\n^D per terminare: ");
    }
    clnt_destroy(cl);
    exit(0);
}