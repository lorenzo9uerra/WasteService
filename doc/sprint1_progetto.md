## Progettazione

La progettazione e lo sviluppo delle componenti software stabilite in fase di analisi è stata divisa in questo modo:

- Trolley, e interazione con BasicRobot: L. Guerra
- Wasteservice (parte relativa a interazione e controllo Trolley): L. Guerra
- Wasteservice (parte relativa a gestione richieste Wastetruck): F. Lenzi
- StorageManager: F. Lenzi
- WasteTruck (ricezione pickUp e GUI): F. Lenzi

È stato possibile dividere facilmente lo sviluppo di WasteService per la struttura che è stata adottata, dettagliata [in seguito](#wasteservice).

### In generale

In fase di progettazione e sviluppo, i vari attori facenti parti del sistema sono stati strutturati come risorsa osservabile COaP, con dati in formato Prolog, sfruttando la libreria QakActor, sia per facilitare l'espansione futura in successivi SPRINT, sia per rendere più facile il testing. 

Per esempio Trolley, quando osservato, fornisce questi dati in formato Prolog:

```
state(work)
pos(X,Y)
{content(MAT,QNT)}
```

con `content` opzionale se non trasporta carichi.

Inoltre, ad ogni attore Qak è stat associata una classe Kotlin di support, per ridurre il codice nel file Qak e permettere comportamenti di classe più complessi. Ogni classe implementa un'interfaccia che contiene le proprietà e metodi accessibili dall'attore.

### Struttura del software

Il sistema è stato realizzato in linguaggio Kotlin, e parti minori in Java, ed è stato diviso in più progetti:

- *wasteservice.shared*: contiene la classe di configurazione `SystemConfig`, enumerativi, e utilità comuni alle varie parti del sistema.
- *wasteservice.core*: contiene il core business dell'applicazione, vale a dire WasteService, Trolley, e StorageManager, che è stato implementato in questo SPRINT.

Il software è contenuto nel package *it.unibo.lenziguerra.wasteservice*, eccetto le classi Kotlin generate da Qak che sono nel percorso attribuito da quel sistema. In particolare i due progetti contengono questi package:

- **wasteservice.shared**
    - *it.unibo.lenziguerra.wasteservice* (config e enumerativi)
    - *it.unibo.lenziguerra.wasteservice.utils*
- **wasteservice.core**
    - *it.unibo.lenziguerra.wasteservice.wasteservice*
    - *it.unibo.lenziguerra.wasteservice.trolley*
    - *it.unibo.lenziguerra.wasteservice.storage*

Per scopo di documentazione, il codice allo stato della fine dello SPRINT 1 è stato copiato in una cartella *src-sprint1*.

### Test

I test plan stabiliti in analisi sono stati adattati al sistema finale dello SPRINT 1, sono eseguibili in questi file:

* [TestRequest.java](../wasteservice.core/test/it/unibo/lenziguerra/wasteservice/wasteservice/TestRequest.java)
* [TestDeposit.java](../wasteservice.core/test/it/unibo/lenziguerra/wasteservice/TestDeposit.java)
* [TestMoreRequests.java](../wasteservice.core/test/it/unibo/lenziguerra/wasteservice/TestMoreRequests.java)

Note sull'esecuzione: 

* È necessario avviare BasicRobot22 prima di eseguire i test, è incluso un [file docker](../wasteservice.core/basicrobot22.yaml) per farlo facilmente, con le impostazioni della mappa che seguono il dominio del problema.
* I test vanno avviati un metodo alla volta, ed è necessario aggiornare la pagina di VirtualEnv di BasicRobot tra le esecuzioni, a causa di requisiti di VirtualEnv e Qak.


### WasteService

#### Divisione

È stata presa la decisione di implementare WasteService come due entità software distinte ma interagenti nello stesso nodo:

- *WasteServiceServer*, un applicazione web realizzata con il framework SpringBoot che espone un interfaccia per i WasteTruck, e gestisce le loro richieste e le relative autorizzazioni.
- *WasteService* propter, un attore Qak con annessa classe di supporto, che si occupa di gestire l'operazione di deposito coordinando il Trolley che agisce da attuatore come da analisi.

Questo permette di realizzare facilmente l'interazione e l'interfaccia per il WasteTruck, oltre ad avere il vantaggio di disaccoppiare l'operazione di deposito dalla gestione delle richieste: il modello degli stati sviluppato in analisi portava a rispondere a una richiesta arrivata da un WasteTruck solo alla fine di una operazione di deposito. In questo modo, invece, il server è sempre in ascolto di richieste, e in grado di gestirle.

Questo ha inoltre, come effetto secondario, permesso agli sviluppatori di lavorare separatamente alle due parti del progetto (gestione richieste e operazione di deposito)

#### Richieste

La parte di WasteService che da analisi interagisce con WasteTruck viene implementata come server web: la pagina servita fa le veci dell'attore nel modello di analisi WasteTruck.

Questa pagina contiene script che comunicano con il server di WasteService tramite WebSocket, permettendo sia di inviare richieste e ricevere risposte, sia di ascoltare sulla connessione per la notifica di carico raccolto. WasteTruck diventa quindi, da attore, una pagina web, che viene aperta dal pilota del Waste Truck. 

Tramite WebSocket, la pagina del WasteTruck è in grado di rimanere in ascolto per aggiornamenti sulla raccolta del carico, come da analisi (dispatch `pickedUp`).

Il server, per gestire le richieste, deve chiedere come da analisi informazioni a StorageManager: per farlo agisce da "alieno" al contesto Qak, stabilendo una connessione TCP con il contesto di StorageManager (in questa prima versione, ospitato per demo nello stesso nodo). Chiede al Trolley tramite COaP dati sul suo contenuto attuale, per lo stesso motivo.

L'inizio dell'operazione di deposito viene fatto inviando da "alieno" una richiesta tramite TCP al contesto di WasteService attore ospitato in locale: viene introdotta una nuova richiesta per coordinare le due "metà" di WasteService:

```
Request triggerDeposit : triggerDeposit(MAT,QNT)
Reply trolleyPickedUp : trolleyPickedUp(_)
```

WasteServiceServer invia `triggerDeposit` all'attore per avviare l'operazione di deposito, e riceve `trolleyPickedUp` per sapere quando inviare `pickedUp` al WasteTruck come da analisi.

#### Deposit

WasteService attore è simile al modello dell'analisi. È in grado di stabilire la coordinata più vicina al trolley tra quelle incluse nel rettangolo della area di destinazione, e coordina il Trolley attraverso le varie fasi della *deposit action*.

Il requisito **indoor-more-requests** è adempiuto in modo semplice: WasteService attore, per comportamento default di Qak, mette in coda eventuali richieste di `triggerDeposit` arrivate durante una *deposit action*, e decide se mandare a HOME o a INDOOR il Trolley a operazione finita in base alla presenza o meno di ulteriori richieste in coda.

L'attore, come detto, è osservabile tramite COaP: la risorsa espone questi dati:

```
tpos(LOC) // Posizione del trolley in termini di nome ("home", "indoor", ecc.)
{error(ERR)} // Errore (opz.), nel caso il sistema abbia errori
```

#### Gestione errori

In caso di errori, cioè, allo stato virtuale del sistema,collisione del Trolley, il sistema si blocca e necessita di una risoluzione manuale del problema seguita da un riavvio. Questo comportamento è stato deciso data l'impossibilità di risolvere dal lato del sistema problemi di questa natura, che in un caso reale sarebbero dati da incidenti fisici nell'area risolvibili solo da un operatore.

<immagine architettura>

<immagine interazioni>

### Trolley

Per lo spostamento del Trolley, come da analisi, si è pensato di utilizzare il componente software *BasicRobot*. In particolare, il Trolley interagisce con l'attore *pathexec*, che permette di svolgere percorsi costituiti da una serie di comandi (w: forward, r: gira a destra, l: gira a sinistra) con una singola interazione request-reply.

Il Trolley agisce, come da analisi, come semplice attuatore. Espone come "interfaccia" la possibilità di inviare richieste `trolleyDeposit`, `trolleyCollect`, e `trolleyMove`. Le risposte inviate dal robot sono state espanse da una singola risposta `trolleyDone(OK)` da analisi, a due risposte `trolleyDone` e `trolleyFail(REASON)`, per rendere più semplice la distinzione dei casi in Qak.

Le prime due richieste sono interamente gestite dalla classe di supporto legata al trolley, *ITrolleySupport*; al momento è implementata per il caso virtuale, ma è facilmente espandibile per un eventuale caso reale. 

L'operazione di movimento è come sopra gestita da *pathexec*: la classe di supporto genera il percorso a partire dalla posizione di partenza e dalle coordinate target, poi Trolley lo invia a *pathexec* e in caso di successo aggiorna la posizione memorizzata. Essa, infatti, tiene traccia di posizione e direzione attuali del robot.

È stata inoltre aggiunta un'operazione `trolleyRotate`, per permettere la rotazione senza movimento, usata per tornare rivolti nella posizione iniziale una volta tornati a HOME.

_Basicrobot_ permette di implementare il movimento del robot in modo indipendente dalla tecnologia, il che lo rende un componente utile in fase di testing. Infatti, prima di utilizzare un robot reale, sarà possibile osservare i movimenti del trolley grazie a _WEnv_.

Come per *WasteService*, un errore blocca il sistema fino alla risoluzione manuale, per le ragioni già indicate.

Come detto sopra, inoltre, il Trolley è una risorsa COaP osservabile, esponendo questi dati:

```
state(work) // In questo SPRINT costante, espandibile per includere altri stati (stop, ecc.)
pos(X, Y) // Posizione numerica
{content(MAT, QNT)} // Opzionale, contenuto trasportato
```

<immagine architettura>

### StorageManager

StorageManager, in modo analogo a Trolley, è rappresentato come classe Qak associata a classe Kotlin di support. Come Trolley rimane aperta all'espansione per un ipotetico caso reale.

La risorsa COaP esposta mostra questi dati:

```
// Per ogni materiale contenuto:
content(MAT, QNT, MAX)
```

### Struttura del sistema

La struttura finale del sistema nello SPRINT 1 è riassunta in questo grafico: 

<immagine architettura>

### Immagine Docker

Viene fornito [wasteservice.yaml](../wasteservice.core/wasteservice.yaml) per eseguire il sistema con Docker. Ci si può connettere alla porta 8080 per aprire l'interfaccia per i WasteTruck usata per inviare richieste, e alla porta 8090 per visualizzare l'ambiente virtuale del robot.

