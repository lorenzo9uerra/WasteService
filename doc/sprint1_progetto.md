## Progettazione

La progettazione e lo sviluppo delle componenti software stabilite in fase di analisi è stata divisa in questo modo:

- Trolley, e interazione con BasicRobot: L. Guerra
- Wasteservice (parte relativa a interazione e controllo Trolley): L. Guerra
- Wasteservice (parte relativa a gestione richieste Wastetruck): F. Lenzi
- StorageManager: F. Lenzi
- WasteTruck (ricezione pickUp e GUI): F. Lenzi

È stato possibile dividere facilmente lo sviluppo di WasteService per la struttura che è stata adottata, dettagliata [in seguito](#wasteservice).

### In generale
- Per quasi ogni attore classe support con possibile estensione nei casi reale/virtuale
- Ogni attore è risorsa osservabile con dati in formato Prolog
### Gestione delle richieste

La parte di WasteService che da analisi interagisce con WasteTruck viene implementata come server web: la pagina servita fa le veci dell'attore nel modello di analisi WasteTruck.

Questa pagina contiene script che comunicano con il server di WasteService tramite WebSocket, permettendo sia di inviare richieste e ricevere risposte, sia di ascoltare sulla connessione per la notifica di carico raccolto. WasteTruck diventa quindi, da attore, una pagina web, che viene aperta dal pilota del Waste Truck. 

Il server web di WasteService si occupa solo della parte di *core business* correlata alla gestione delle richieste dei Waste Truck.

Interazione WasteService / attore
- Richiesta da server a attore, risposta quando trolley raccoglie carico per mandare pickedUp a truck

### StorageManager

- Creazione interfaccia IStorageManagerSupport, e classi annesse

- Modifica della resource da content(type, amount) a content(type, amount, max)

### Navigazione

Per lo spostamento del trolley come da analisi si è pensato di riutilizzare un software già
sviluppato, chiamato _basicrobot_. Questo componente ci permette di controllare
il robot DDR attraverso messaggi, come se questo fosse un semplice attuatore. In
questo modo si può tenere traccia della posizione del trolley così da potergli
indicare successivamente la strada migliore da percorrere durante ogni suo
stato.
Inoltre _basicrobot_ ci permette di far eseguire comandi al robot in modo
indipendente dalla tecnologia, il che lo rende un componente utile in fase di
testing. Infatti, prima di utilizzare un robot reale, sarà possibile osservare i
movimenti del trolley grazie a _WEnv_, un software che simula un robot virtuale
che ci è fornito ed è quindi riutilizzabile, permettendoci di abbattere i costi
della fase di testing.

Per interagire con _basicrobot_ viene utilizzato _aril_, ossia _abstract robot
interaction language_, che espone un interfaccia con cui è possibile comunicare
in modo indipendente dalla tecnologia.

- Creazione interfaccia ITrolleySupport, e classi annesse
- Generazione percorso da inviare a pathexec
- Tracciamento posizione e direzione
- Risorsa coap 
- Conversione da trolleyDone a trolleyDone/trolleyFail per
essere più adatto a qak
- Aggiunto trolleyRotate per pulizia così guarda in basso
dopo ritorno a home
- Errore: blocca tutto
