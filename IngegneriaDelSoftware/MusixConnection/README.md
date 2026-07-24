# MusixConnection — Prototipo

Prototipo della piattaforma **MusixConnection** (TecWeb, UniBO): web-app Java
(Servlet + JSP) su **Tomcat 9**, con persistenza **a "forza bruta"** (JDBC diretto,
senza DAO) su **MySQL**.

## Funzionalità del prototipo

1. **Registrazione e autenticazione** di un utente (username univoco).
2. **Profilo**: aggiunta di strumenti e stili praticati.
3. **Creazione di un'associazione** (con validazione locale provvisoria del CF);
   il creatore ne diventa **Presidente**.
4. **Iscrizione** a un'associazione, **conferma presenza** agli eventi futuri.
5. **Nomina degli insegnanti** (e nomina di un nuovo presidente) da parte del Presidente.
6. **Scambio di messaggi** nella chat dell'associazione, **in memoria** (senza persistenza, senza notifiche push).

## Prerequisiti

- JDK 19 (o compatibile)
- Apache Maven
- MySQL in esecuzione (server separato)
- Apache Tomcat 9 installato in locale

## Configurazione

1. **Crea il database** su MySQL:

   ```sql
   CREATE DATABASE musixconnection CHARACTER SET utf8mb4;
   ```

   Le tabelle vengono create automaticamente all'avvio dell'applicazione
   (listener `InizializzazioneDB`, `CREATE TABLE IF NOT EXISTS`), che inserisce
   anche alcuni dati di esempio. Lo schema è documentato in
   `src/main/webapp/sql/schema_mysql.sql`.

2. **Imposta le credenziali del DB** come variabili d'ambiente (convenzione del corso):

   ```bash
   export usernameDB="root"
   export passwordDB="la_tua_password"
   ```

   (Su Windows: `set usernameDB=...` / variabili di sistema.)

3. **Aggiorna il percorso di Tomcat** nel `pom.xml`, proprietà `tomcat.server`,
   con il path della tua installazione di Tomcat 9.

## Avvio

```bash
mvn clean package          # compila e produce target/MusixConnection.war
mvn cargo:run              # avvia Tomcat 9 (modalità "existing") con la web-app
```

Poi apri: `http://localhost:8080/MusixConnection/`

In alternativa, copia `target/MusixConnection.war` nella cartella `webapps/` di Tomcat.

> Importante: avvia Tomcat **nello stesso ambiente** in cui hai esportato
> `usernameDB`/`passwordDB`, altrimenti il `DataSource` non riesce a connettersi.

### Credenziali demo

| Username | Password | Ruolo                                |
|----------|----------|--------------------------------------|
| `pippo`  | `1234`   | Presidente di "Blue Note Club"       |
| `pluto`  | `1234`   | Iscritto a "Blue Note Club"          |

## Architettura

```
it.unibo.tw.web
├── model         POJO di dominio + pattern
│   ├── Associazione / AssociazioneConcreta / DecoratoreAssociazione
│   │   AssociazioneStrumento / AssociazioneDanza   → pattern Decorator
│   ├── Iscrizione / IscrizioneConcreta / DecoratoreIscrizione
│   │   Presidente / Insegnante                     → pattern Decorator
│   ├── Associazioni                                → Singleton (registro)
│   ├── SistemaMessaggistica                        → Singleton (chat in memoria)
│   ├── Utente, Evento, Messaggio, Costanti
│
├── db            persistenza "forza bruta" (un repository per entità)
│   ├── DataSource (connessione JDBC, credenziali da env), PersistenceException
│   └── *Repository  (costanti tabella/colonne, SQL statico, CRUD con
│                     PreparedStatement e chiusura in finally)
│
└── controller    servlet, tutti estendono ControllerPersistenza
    ├── ControllerPersistenza  (DataSource condiviso + scrittura log)
    ├── RegistrazioneServlet, LoginServlet, LogoutServlet, ProfiloServlet
    ├── AssociazioneServlet  (create / iscriviti / confirmEvent / nominate / nominatePresident)
    ├── MessaggioServlet     (chat in memoria)
    └── InizializzazioneDB   (ServletContextListener: crea tabelle + seed)
```

Le viste sono JSP in `src/main/webapp/pages/`, con tema grafico in
`src/main/webapp/styles/musix.css` (derivato dai mockup arancioni).

## Note e semplificazioni del prototipo

Coerentemente con la sezione "Prototipo" del documento dei requisiti, sono
**escluse** o semplificate alcune parti:

- **Chat senza persistenza**: i messaggi vivono solo in memoria
  (`SistemaMessaggistica`) e si perdono al riavvio del server; nessuna notifica push.
- **Validazione CF locale e provvisoria**: niente verifica tramite sistema esterno;
  si accettano 11 cifre numeriche oppure 16 caratteri alfanumerici.
- **Iscrizione con approvazione** (R15F): l'utente presenta una *richiesta* di
  iscrizione (eventuale breve descrizione); il Presidente la accetta o la rifiuta
  dalla home o dalla pagina dell'associazione. L'utente diventa iscritto solo dopo
  l'accettazione. Non si può richiedere l'iscrizione ad associazioni di cui si è già
  Presidente (R5F).
- **Nomina Presidente esclusa dal prototipo**: pur essendo un requisito (R14F), non
  rientra nei punti del prototipo; è quindi stata rimossa (la nomina Insegnante resta).
- **Log di sicurezza / GestoreSicurezza** non implementati: `scriviLog()` è un
  segnaposto che scrive su standard output nel formato "Timestamp Operazione".
- Niente blocco dei tentativi di login, niente verifica T&C/GDPR.

## Vincoli rispettati (riferimento ai requisiti)

- Username univoco (R02NF), CF associazione univoco (R09NF).
- Il creatore diventa Presidente (R6F), un solo Presidente alla volta (R01D).
- Conferma presenza solo da iscritti attivi (R22F), solo eventi futuri (R35F), una
  sola volta per evento e, per le **Lezioni**, solo se si pratica lo strumento/stile
  della lezione (R34F).
- Nomina insegnante solo dal Presidente, solo per strumenti/stili praticati
  dall'iscritto e coerenti con la tipologia dell'associazione (R30F), senza duplicati
  sulla stessa materia (R37F).
- Richiesta di iscrizione valutata dal Presidente: accettazione/rifiuto (R15F); messaggi
  solo da iscritti attivi (R23F).
