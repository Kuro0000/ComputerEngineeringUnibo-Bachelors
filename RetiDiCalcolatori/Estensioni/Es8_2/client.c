#include "xfactor.h"
#include <rpc/rpc.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char *argv[]) {
    // VARIABILI TUTTE ALL'INIZIO
    char *host;
    CLIENT *cl;
    char ok[10], buffer[256];
    int *risInt;
    ClassificaGiudici *classifica;
    ListaCandidati *listaCand;
    InputVoto inVoto;
    Candidato inCand;
    InputSoglia inSoglia;
    InputCategoria inCat;
    InputFase inFase;
    int i, nread, flag;
    char *nomeTemp, input, trash;

    if (argc != 2) {
        printf("usage: %s server_host\n", argv[0]);
        exit(1);
    }
    host = argv[1];

    cl = clnt_create(host, OPERATION, OPERATIONVERS, "udp");
    if (cl == NULL) {
        clnt_pcreateerror(host);
        exit(1);
    }

    printf("OPERAZIONI:\n");
    printf("1. Classifica Giudici\n");
    printf("2. Esprimi Voto\n");
    printf("3. Aggiungi Candidato\n");
    printf("4. Rimuovi Candidato\n");
    printf("5. Lista Candidati sopra soglia\n");
    printf("6. Lista Candidati per Categoria\n");
    printf("7. Cambia Fase \n");
    printf("^D per terminare.\n");
    printf("Scelta: ");

    while (gets(ok)) {
        
        // --- 1. CLASSIFICA GIUDICI ---
        if (strcmp(ok, "1") == 0) {
            classifica = classifica_giudici_1(NULL, cl);
            if (classifica == NULL) {
                clnt_perror(cl, host);
            } else {
                printf("--- Classifica Giudici ---\n");
                i = 0;
                while (i < classifica->ClassificaGiudici_len) {
                    printf("%d) %s - Punti: %d\n", i + 1,
                           classifica->ClassificaGiudici_val[i].nomeGiudice,
                           classifica->ClassificaGiudici_val[i].punteggioTot);
                    i++;
                }
                xdr_free((xdrproc_t)xdr_ClassificaGiudici, (char *)classifica);
            }
        }
        // --- 2. ESPRIMI VOTO ---
        else if (strcmp(ok, "2") == 0) {
            inVoto.nomeCandidato = (char *)malloc(MAX_NAME_SIZE);
            
            printf("Nome Candidato: ");
            gets(inVoto.nomeCandidato);
            
            flag = 0;
            while(flag == 0){
                printf("Operazione (A=Add, S=Sub): ");
                input = getchar();
                
                // Consumo il resto della riga (incluso \n) SOLO se ho letto qualcosa diverso da \n
                if (input != '\n') {
                    while ((trash = getchar()) != '\n' && trash != EOF);
                }

                if(input == 'A' || input == 'S'){
                    inVoto.tipoOp = input;
                    flag = 1;
                } else {
                    printf("Errore: inserire solo 'A' o 'S'. Riprova.\n");
                }
            }
            risInt = esprimi_voto_1(&inVoto, cl);
            if (risInt == NULL) {
                clnt_perror(cl, host);
            } else if (*risInt < 0) {
                printf("Errore: Candidato non trovato o voto gia' a 0.\n");
            } else {
                printf("Voto aggiornato. Nuovo totale: %d\n", *risInt);
            }
            free(inVoto.nomeCandidato);
        }
        // --- 3. AGGIUNGI CANDIDATO ---
        else if (strcmp(ok, "3") == 0) {
            // Pulizia struttura
            memset(&inCand, 0, sizeof(Candidato));
            
            inCand.nome = (char *)malloc(MAX_NAME_SIZE);
            inCand.giudice = (char *)malloc(MAX_NAME_SIZE);
            inCand.nomeFile = (char *)malloc(MAX_FILE_SIZE);

            printf("Nome: ");
            gets(inCand.nome);
            printf("Giudice: ");
            gets(inCand.giudice);
            
            flag = 0;
            while(flag == 0){
                printf("Categoria (U/D/O/B): ");
                input = getchar();
                if (input != '\n') {
                    while ((trash = getchar()) != '\n' && trash != EOF);
                }

                if(input == 'U' || input == 'D' || input == 'O' || input == 'B'){
                    inCand.categoria = input;
                    flag = 1;
                } else {
                    printf("Errore: categoria non valida (U, D, O, B).\n");
                }
            }
            
            printf("Nome File: ");
            gets(inCand.nomeFile);
            
            flag = 0;
            while(flag == 0){
                printf("Fase (A/B/S): ");
                input = getchar();
                if (input != '\n') {
                    while ((trash = getchar()) != '\n' && trash != EOF);
                }

                if(input == 'A' || input == 'B' || input == 'S'){
                    inCand.fase = input;
                    flag = 1;
                } else {
                    printf("Errore: fase non valida (A, B, S).\n");
                }
            }
            inCand.voto = 0; 

            risInt = aggiungi_candidato_1(&inCand, cl);
            if (risInt == NULL) {
                clnt_perror(cl, host);
            } else if (*risInt > 0) {
                printf("Candidato aggiunto con successo.\n");
            } else {
                printf("Errore: Candidato gia' esistente.\n");
            }
            free(inCand.nome); free(inCand.giudice); 
            free(inCand.nomeFile);
        }
        // --- 4. RIMUOVI CANDIDATO ---
        else if (strcmp(ok, "4") == 0) {
            nomeTemp = (char *)malloc(MAX_NAME_SIZE);
            printf("Nome Candidato da rimuovere: ");
            gets(nomeTemp);
            
            risInt = rimuovi_candidato_1(&nomeTemp, cl);
            if (risInt == NULL) {
                clnt_perror(cl, host);
            } else if (*risInt > 0) {
                printf("Candidato rimosso.\n");
            } else {
                printf("Errore: Candidato non trovato.\n");
            }
            free(nomeTemp);
        }
        // --- 5. LISTA SOPRA SOGLIA ---
        else if (strcmp(ok, "5") == 0) {
            flag = 0; 
            while(flag == 0){
                printf("Inserisci soglia minima voti: ");
                gets(buffer);
                nread = 0;
                flag = 1; // Assumo valido
                if(buffer[0] == '\0') flag = 0;
                
                while(buffer[nread]!='\0' && flag==1){
                    if(!(buffer[nread]>='0' && buffer[nread]<='9')){
                        flag = 0;
                    }
                    nread++;
                }
                if(flag == 0){
                    printf("Errore: inserire un numero intero positivo.\n");
                }
            }
            
            inSoglia.soglia = atoi(buffer);

            listaCand = lista_sopra_soglia_1(&inSoglia, cl);
            if (listaCand == NULL) {
                clnt_perror(cl, host);
            } else {
                printf("--- Candidati con > %d voti ---\n", inSoglia.soglia);
                i = 0;
                while (i < listaCand->ListaCandidati_len) {
                    printf("%s (Voti: %d)\n", 
                        listaCand->ListaCandidati_val[i].nome,
                        listaCand->ListaCandidati_val[i].voto);
                    i++;
                }
                xdr_free((xdrproc_t)xdr_ListaCandidati, (char *)listaCand);
            }
        }
        // --- 6. LISTA PER CATEGORIA ---
        else if (strcmp(ok, "6") == 0) {
            flag = 0;
            while(flag == 0){
                printf("Inserisci categoria (U/D/O/B): ");
                input = getchar();
                if (input != '\n') {
                    while ((trash = getchar()) != '\n' && trash != EOF);
                }

                if(input == 'U' || input == 'D' || input == 'O' || input == 'B'){
                    inCat.categoria = input;
                    flag = 1;
                } else {
                    printf("Errore: categoria non valida.\n");
                }
            }

            listaCand = lista_per_categoria_1(&inCat, cl);
            if (listaCand == NULL) {
                clnt_perror(cl, host);
            } else {
                printf("--- Candidati Categoria %c ---\n", inCat.categoria);
                i = 0;
                while (i < listaCand->ListaCandidati_len) {
                    printf("%s (Giudice: %s)\n", 
                           listaCand->ListaCandidati_val[i].nome,
                           listaCand->ListaCandidati_val[i].giudice);
                    i++;
                }
                xdr_free((xdrproc_t)xdr_ListaCandidati, (char *)listaCand);
            }
        }
        // --- 7. CAMBIA FASE ---
        else if (strcmp(ok, "7") == 0) {
            inFase.nomeCandidato = (char *)malloc(MAX_NAME_SIZE);
            
            printf("Nome Candidato: ");
            gets(inFase.nomeCandidato);
            
            flag = 0;
            while(flag == 0){
                printf("Nuova Fase (A/B/S): ");
                input = getchar();
                if (input != '\n') {
                    while ((trash = getchar()) != '\n' && trash != EOF);
                }

                if(input == 'A' || input == 'B' || input == 'S'){
                    // assegnazione diretta
                    inFase.nuovaFase = input;
                    flag = 1;
                } else {
                    printf("Errore: fase non valida.\n");
                }
            }
            risInt = cambia_fase_1(&inFase, cl);
            if (risInt == NULL) {
                clnt_perror(cl, host);
            } else if (*risInt > 0) {
                printf("Fase aggiornata correttamente.\n");
            } else {
                printf("Errore: Candidato non trovato.\n");
            }
            free(inFase.nomeCandidato);
        }
        else {
            printf("Comando non riconosciuto.\n");
        }

        printf("\nScelta: ");
    }
    
    clnt_destroy(cl);
    exit(0);
}