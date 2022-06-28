# Analisi del problema

## Prototipo Iniziale

Per poter avere una migliore visione d'insieme del problema, è stato realizzato un prototipo usando una architettura logica di prova (non definitiva, mancando ancora una vera fase di analisi):

![schema dell'architettura](doc/img/architettura1.jpg)

Il [prototipo è questo](prototipo/src/prototipo.qak) (per semplicità, non è inclusa la distinzione tra ritorno a HOME e ritorno a INDOOR, che rimane un requisito del sistema vero e proprio).

## Interazione

Vengono definite diverse tipologie di messaggio:

### Requisito **request**:
Si tratta di una domanda con risposta, quindi l'implementazione immediata è request-reply:
```
Request deposit : deposit(MAT, QNT)
Reply allowDeposit : allowDeposit(ALLOW)
```
È emersa a realizzare il primo prototipo la necessità di comunicare al camion l'avvenuta raccolta dei rifiuti nel caso il deposito venga accettato:
```
Dispatch pickedUp : pickedUp(MAT, QNT)
```

### Requisito **storage-check**:
Come sopra, request-reply:
```
Request storageAsk : storageAsk(_)
Reply storageAt : storageAt(QNT)
```

- Potrebbe essere anche sfruttato l'evento del [requisito GUI](#requisito-led-e-requisito-gui), con alcune conseguenze (vedi sotto).

### Requisito **deposit**:
Viene comunicato il deposito di un materiale:
```
Dispatch storageDeposit : storageDeposit(QNT)
```
Il trolley, per permettere la gestione di casi di coda, deve anche notificare al sistema quando inizia e quando finisce l'operazione di trasporto; questo potrebbe essere implementato come risposta a una richiesta deposit per iniziare il lavoro:
```
Request deposit : deposit(MAT, QNT)
Reply startedDeposit : startedDeposit(MAT, QNT)
Dispatch doneDeposit : doneDeposit(MAT, QNT)
```
Oppure, in alternativa, come dispatch successivo e scollegato:
```
Dispatch trolleyDeposit : deposit(MAT, QNT)
Dispatch startedDeposit : startedDeposit(MAT, QNT)
Dispatch doneDeposit : doneDeposit(MAT, QNT)
```
Nel primo caso, sarebbe più chiaro a livello concettuale, ma si avrebbe una risposta in un momento molto distante dalla richiesta; nel secondo, la separazione rappresenterebbe meglio questa distanza nel tempo, evitando il mantenimento di una connessione a seconda dell'implementazione.

Il messaggio di _startedDeposit_ è necessario perchè quando il trolley parte da HOME ha bisogno di tempo per dirigersi ad INDOOR; inoltre, in un caso reale, impiega del tempo per scaricare i rifiuti dal camion e potrebbe anche rompersi prima di completare lo scarico. Il camion quindi ha bisogno di sapere quando lo scarico è avvenuto per poter partire.

- Nella prototipazione, abbiamo realizzato che è necessaria una comunicazione successiva al trolley nel caso in cui arrivino altre richieste mentre è al lavoro, per permettergli di decidere in base a questo se tornare a HOME o INDOOR a lavoro finito:

```
Dispatch moreRequests : moreRequests(MAT)
```

### Requisito **led** e requisito **gui**:
Led e GUI devono ricevere aggiornamenti sullo stato del trollley. Un modo di farlo potrebbe essere tramite eventi:
```
Event tStatus : tStatus(STATE, POSDATA)
```
Questo avrebbe il lato positivo di poter inviare in una sola volta a tutti i componenti che lo richiedono i dati necessari, ma inviando anche dati non necessari dovendo mandare lo stesso messaggio a ogni destinatario (per esempio, POSDATA al Led).

Un altro modo è inviare dispatch ad ogni componente: più complicato e meno espandibile (nel caso vengano introdotti altri componenti di osservazione), ma evita la trasmissione di dati inutili:
```
Dispatch tStatusLed : tStatusLed(STATE)
Dispatch tStatusGUI : tStatusGUI(STATE, POSDATA)
```

GUI inoltre deve ricevere lo stato del Led; potrebbe ricavarlo dallo stato del trolley ricevuto (visto che lo stato del Led dipende direttamente dallo stato del trolley), ma questo avrebbe la conseguenza di non poter rilevare eventuali errori o guasti del Led; in alternativa, il Led può comunicare alla Gui il proprio stato, anche qua potendolo realizzare sia come dispatch che come evento:
```
Dispatch ledStatus : ledStatus(STATUS)
//---
Event ledStatus : ledStatus(STATUS)
```
Qua non sono presenti dati inutili, ma data la specificità del dato usare eventi potrebbe avere più overhead sul sistema inutilmente a seconda dell'implementazione.

Infine, la GUI deve ricevere informazioni sullo storage; nello stesso modo e con le stesse conseguenze, può essere sia dispatch che evento:
```
Dispatch storageUpdate : storageUpdate(MAT, QNT)
//---
Event storageUpdate : storageUpdate(MAT, QNT)
```
Data la maggiore probabilità che questi dati siano utili a più componenti, l'evento in questo caso potrebbe essere l'opzione migliore.

- Inoltre, se realizzato come evento, potrebbe essere sfruttato per il [requisito storage-check](#requisito-storage-check) (vedi sopra), aumentando il traffico di dati ma evitando la necessità di una richiesta esplicita dello storage attuale. L'efficacia di questo approccio dipende da quanto sia frequente la modifica dello storage rispetto alla frequenza di nuove richieste da parte dei camion.

### Requisito **sonar-stop**:
Il sonar deve comunicare al trolley di fermarsi o riprendere; anche qua, sia dispatch che eventi sono approcci possibili:

```
Event trolleyStop : trolleyStop(_)
Event trolleyResume : trolleyResume(_)
//---
Dispatch trolleyStop : trolleyStop(_)
Dispatch trolleyResume : trolleyResume(_)
```

L'uso di un evento permetterebbe una maggiore espandibilità nel caso vengano introdotti più trolley in una versione futura del sistema; chiaramente quale versione convenga dipende dal comportamento che sarebbe desiderato in quel caso, fermare tutti i trolley oppure uno solo. Al di fuori del future-proofing, la scelta influisce solo sull'overhead del sistema in quantità minore.

## Architettura

Data la numerosa quantità di componenti che comunicano tra loro, implementare la logica del sistema come un insieme di attori è abbastanza naturale. 

Gli attori ricavati dai componenti fisici sono:

- **Trolley**: si occupa di controllare il trolley.
- **LedController**: si occupa di controllare il Led, che è un oggetto "alieno" al sistema di attori; due versioni possibili:
    - *Gestione led e controller separati*: un attore, LedActor, gestisce 1:1 l'oggetto Led "agnostico" (messaggi *setState(on/off)*, *getState*, ecc.), con il controller che in base allo stato del trolley invia i messaggi di controllo a LedActor, e aggiornamenti alla Gui; più concettualmente "pulito" ma molto più traffico, specie per il lampeggiamento richiesto in certi casi
    - *Gestione led e controller uniti*: LedController riceve i dati del sistema, decide lo stato che dovrebbe avere il Led in base ad essi, e comunica direttamente con l'oggetto esterno; evitando overhead nel caso di lampeggiamento.
- **SonarActor**: si occupa di ricevere dati dal sonar e comunicarli al sistema, anche qua due versioni possibili analoghe a quelle del Led
    - *Sonar e controller separati*: analogamente, attore che invia eventi su ogni aggiornamento della distanza del sonar, e attore "controller" che osserva il sonar e in base a esso invia i segnali di STOP e RESUME
    - *Sonar e controller uniti*: come per il Led, con le stesse conseguenze: pulizia logica contro minore overhead e complessità.

```
Context ctxwasteservice ip [host="localhost" port=8050]

QActor trolley context ctxwasteservice {...}
QActor led_actor context ctxwasteservice {...}
QActor sonar context ctxwasteservice {...}
```

Inoltre, per poter interagire con i cassonetti, sono introdotti degli attori di Storage per comunicare modifiche allo storage e inviare aggiornamenti a Gui, ecc.

```
QActor storage_glass context ctxwasteservice {...}
QActor storage_paper context ctxwasteservice {...}
```

Anche qua due opzioni possibili:
- *Storage manager centrale*: un solo attore che gestisce tutti i cassonetti, tenendo traccia dei contenuti attuali e facendo da unico fronte di comunicazione nel sistema per questi dati. Questo semplificherebbe la comunicazione non richiedendo di distinguere il destinatario in base al materiale, ma richiedendo forse più complessità nel caso, per esempio, in cui i dati di storage siano ricavati da sensori su ogni cassonetto, che quindi dovrebbero aggiornare separatamente lo stesso controllore contenente lo StorageManager.
- *Storage manager separati*: un attore per cassonetto (rappresentato nell'esempio sopra), che andrà quindi separatamente interpellato in base al tipo di materiale. La soluzione sarebbe la più intuitiva ma con i problemi sopraelencati.

Per scopo di prototipo e simulazione, i camion vengono trattati come attori, ma nel caso reale sarebbero "alieni" al sistema, inviando dati dall'esterno.

```
Context ctxcamion ip [host="localhost" port=8060]

QActor camion context ctxcamion {...}
```

### Requisito **request**: architettura

Le richieste ricevute dai camion possono venire gestite direttamente dal Trolley, che però aggiungerebbe overhead al componente che già deve comunicare con buona parte del sistema, oppure da un attore dedicato: viene introdotto **RequestHandler**:

- Riceve richieste dai camion
- In base allo stato di storage (vedi [req. storage-check](#requisito-storage-check)) conferma o rifiuta
- Se conferma, o invia il trolley a depositare, oppure se esso è al lavoro gli comunica la presenza di ulteriori richieste

```
QActor requesthandler context ctxwasteservice {...}
```

Questo approccio eviterebbe overhead ulteriore sul trolley, permetterebbe una maggiore espandibilità nel caso di aggiunta di trolley o altro al sistema, e ridurrebbe il response time per i camion.

### Requisito **GUI**: architettura

Viene introdotto un GuiActor per interagire con il contesto di attori del sistema e aggiornare la GUI (oggetto "alieno") con i dati ricevuti:

```
QActor gui context ctxwasteservice {...}
```

## Navigation: movimento del trolley

Da requisiti, si suppone che le posizioni e grandezza delle aree di HOME, INDOOR, e cassonetti vari, siano decise a priori e a priori comunicate al trolley prima dell'inizio del movimento.

Data una posizione di partenza e di arrivo verso la quale il trolley deve navigare, questo può calcolare due tipi di percorso:
- Dividendo la stanza in una griglia quadrata di lato RD, il trolley può semplicemente navigare lungo le direzioni cartesiane, prima ad una coordinata della destinazione e poi all'altra.

![](./doc/img/navigazione_cart.jpg)

- Il trolley compie un percorso diretto a destinazione ignorando la griglia, opzione più veloce ma con maggiori difficoltà implementative.

![](./doc/img/navigazione_diretta.jpg)

## Da inserire poi

~~- Necessaria aggiunta di una comunicazione da RequestHandler a Camion per notificare il camion dell'avvenuta raccolta dei rifiuti (usecase: robot impegnato durante arrivo del camion) e permettergli di andare via~~
~~- Necessaria aggiunta di un qualche messaggio tra Trolley e RequestHandler dopo aver depositato il carico ma prima del ritorno per sapere se tornare a HOME oppure a INDOOR~~

~~- Le richieste arrivano dall'esterno; viene considerato nel contesto del sistema il camion come un "oggetto" attivo. ~~

~~- Una prima architettura tratta i vari componenti come attori~~

~~- Due versioni (si accoda richieste il Trolley, o no?)~~

~~- StorageManager osservabile per GUI?~~

~~- Questa prima architettura evidenzia i vari componenti e i collegamenti tra loro, senza soffermarsi troppo sul comportamento interno (in particolare quello del Trolley) per ora.~~

- Ottimizzazione facendo fare a deposit (trolley) le veci di moreRequests~~


# TestPlan

### Test Sonar

Fornito un sonar mock fatto per il test, verificare che il segnale stop/resume venga correttamente inviato quando la distanza rilevata è minore o maggiore di un valore DLIMIT.

### Test Led

Fornito un led mock e inviando segnali di stato del trolley simulati, questo si accenda nel caso in cui lo stato indichi che il trolley è fermo, si spenga nel caso in cui il trolley sia alla home e lampeggi nel caso in cui il trolley si muova (controllando che si accenda e si spenga con una determinata frequenza). Dopo ogni cambiamento di stato invii un segnale alla gui mock comunicandoglielo.

### Test RequestHandler

Sono necessari mock per la gestione dello storage, a scopo verifica, per il trolley, che deve aspettare un tempo casuale per poi inviare segnali ed un oggetto virtuale che simuli l'arrivo di camion.

- **Test Deny**: il camion invia una richiesta di deposito al RequestHandler e il gestore dello storage risponde con un segnale che indica che lo spazio non è sufficiente; il RequestHandler risponde così con un rifiuto al camion e non invia nulla al trolley.

- **Test Accept Idle**: il camion invia una richiesta di deposito al RequestHandler, il gestore dello storage risponde che c'è sufficiente spazio disponibile, così il RequestHandler comunica l'accettazione del carico al camion e invia un messaggio di inizio deposito al trolley, poi, ricevuto il messaggio di inizio trasporto dal trolley, invia un segnale di avvenuto scarico al camion. Finito il deposito riceve un messaggio che indica la terminazione del trasferimento.

- **Test Accept Move**: durante l'esecuzione del test precedente, il camion invia un'ulteriore richiesta e il gestore dello storage comunica che c'è sufficiente spazio disponibile, così il RequestHandler comunica l'accettazione del carico al camion e al trolley che è presente una nuova richiesta in attesa. Solo dopo il ritorno del trolley che invia un segnale di completamento del deposito precedente, il RequestHandler invia il segnale di deposito al trolley e, ricevuto il messaggio di inizio trasporto da parte del trolley, invia un segnale di avvenuto scarico al camion.

### Test Gestione Storage

A prescindere che venga implementato come una o più classi (vedi [analisi](#architettura)), il gestore dello storage inizializza lo spazio disponibile a 0, riceve richieste di storageAsk e risponde con lo stato corrente dello storage. Le richieste di deposito modificano correttamente lo stato. Inoltre deve inviare degli eventi dopo ogni modifica dello stato, contenenti lo stato aggiornato.

### Test GUI

Verificare che l'interfaccia si aggiorni correttamente dopo aver ricevuto eventi di stato da parte del led, del trolley e del gestore dello storage.

### Test Trolley

Per verificare il passaggio nelle varie posizioni target bisogna verificare che passi su certe coordinate. Un modo rapido di testarlo è di dividere lo spazio in una griglia e, per verificare che abbia raggiunto la destinazione, controllare che sia passato sopra uno dei quadretti corrispondenti ad essa.

- **Test Messaggi**: Ignorando lo spostamento e la posizione del trolley, verificare che, ricevuto il segnale di inizio deposito, invii un segnale di inizio trasporto al RequestHandler e che invii almeno un aggiornamento di stato e posizione, dopodiché invii un segnale di deposito al gestore dello storage. Infine deve inviare almeno un altro aggiornamento di stato e posizione e inviare _doneDeposit_ al RequestHandler.

- **Test Movimento Base**: partendo da HOME, ricevuto il messaggio di inizio deposito, si sposti a INDOOR, poi al cassonetto corrispondente e nuovamente ad HOME ed invia _doneDeposit_.

- **Test Movimento Senza Ritorno**: partendo da HOME, ricevuto il messaggio di inizio deposito, si sposti ad INDOOR, poi, prima di arrivare al cassonetto corrispondente riceva il messaggio di moreRequests, così arrivato al cassonetto torni ad INDOOR senza passare da HOME ed invii _doneDeposit_.

- **Test Movimento Cambio Direzione**: partendo da HOME, ricevuto il messaggio di inizio deposito, si sposti ad INDOOR e poi al cassonetto corrispondente, ma durante il ritorno ad HOME riceva il messaggio di moreRequests. In questo caso deve cambiare direzione e tornare ad INDOOR senza passare per HOME, anche se vi era diretto inizialmente, per poi inviare _doneDeposit_.
