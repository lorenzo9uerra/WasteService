# Analisi del problema

In questa fase di analisi verrà utilizzato il linguaggio ad attori Qak per la modellazione; i messaggi useranno termini specifici del linguaggio per rappresentare le varie modalità di comunicazione, ma non necessariamente corrisponderanno alla tecnologia specifica utilizzata in implementazione.

## Prototipo Iniziale

Per poter avere una migliore visione d'insieme del problema, è stato realizzato un prototipo usando una architettura logica di prova (non definitiva, mancando ancora una vera fase di analisi):

![schema dell'architettura](doc/img/architettura1.jpg)

Il [prototipo è questo](prototipo/src/prototipo.qak) (per semplicità, non è inclusa la distinzione tra ritorno a HOME e ritorno a INDOOR, che rimane un requisito del sistema vero e proprio).

## Interazione

Vengono definite diverse tipologie di messaggio:

### Requisito **request**

Si tratta di una domanda con risposta, quindi l'implementazione immediata è request-reply:
```
Request deposit : deposit(MAT, QNT)
Reply loadaccept : loadaccept()
Reply loadrejected : loadrejected()
```

È necessario che (in caso di loadaccept) il camion sappia quando lo scarico dei rifiuti da parte del trolley è stato completato per poter ripartire. Ci sono diverse opzioni: 

1. La risposta (loadaccept) potrebbe essere semplicemente inviata solo a scarico completato, a differenza di loadrejected che verrebbe inviata appena possibile. Una volta arrivata la risposta, il camion potrebbe partire. La conseguenza di questo approccio sarebbe l'impossibilità di rilevare errori da parte del Waste truck: "vedrebbe" nella UI un'attesa senza sapere se è per via dello scarico rifiuti in corso oppure per un errore.

2. La risposta (loadaccept) arriva subito come per loadrejected, per informare il Waste truck il prima possibile, e viene inviato un successivo messaggio pickedUp per notificare l'avvenuto scarico e la possibilità di partire. Questo richiede che il Waste truck sia anche in grado di ricevere passivamente messaggi, e non solo inviare richieste e ricevere risposte come da requisiti; è possibile, ma richiede accorgimenti più specifici nello sviluppo.

```
Dispatch pickedUp : pickedUp()
```

### Requisito **deposit**

Il Wasteservice invia un messaggio al trolley per richiedere un'azione di deposito. Il trolley, per permettere la gestione di casi di coda, deve anche notificare al WasteService quando finisce lo scarico dei rifiuti dal Waste truck e quando finisce l'operazione di deposito al BOX.

1. Questo potrebbe essere implementato come risposta a una richiesta deposit per iniziare il lavoro:
```
Request deposit : deposit(MAT, QNT)
Reply collectWaste : collectWaste(MAT, QNT)
Dispatch doneDeposit : doneDeposit(MAT, QNT)
```
2. Oppure, in alternativa, come dispatch successivo e scollegato:
```
Dispatch trolleyDeposit : deposit(MAT, QNT)
Dispatch collectWaste : collectWaste(MAT, QNT)
Dispatch doneDeposit : doneDeposit(MAT, QNT)
```
Nel primo caso, sarebbe più chiaro a livello concettuale, ma si avrebbe una risposta in un momento molto distante dalla richiesta; nel secondo, la separazione rappresenterebbe meglio questa distanza nel tempo, evitando il mantenimento di una connessione a seconda dell'implementazione.

Il messaggio di _collectWaste_ è necessario perchè quando il trolley parte da HOME ha bisogno di tempo per dirigersi ad INDOOR; inoltre, in un caso reale, impiega del tempo per scaricare i rifiuti dal camion e potrebbe anche rompersi prima di completare lo scarico. Il camion quindi ha bisogno di sapere quando lo scarico è avvenuto per poter partire, e il WasteService deve a sua volta saperlo dal trolley.

Inoltre, per iniziare lo scarico nel cassonetto viene comunicato il deposito di un materiale da parte del trolley:
```
Dispatch storageDeposit : storageDeposit(MAT, QNT)
```

- *doneDeposit* viene inviato al termine dell'operazione di deposito: quindi, come da requisiti, quando i rifiuti sono stati scaricati dentro al cassonetto con successo e prima di dirigersi verso HOME o INDOOR. Questo serve per notificare il WasteService che il trolley è libero per altre azioni di deposito.
- Il WasteService può inviare ulteriori messaggi di *deposit* prima che il trolley torni alla posizione iniziale, così che il trolley possa sapere se tornare a HOME (nel caso in cui non ci siano altre azioni di deposito da compiere) o a INDOOR (nel caso ce ne siano), a seconda di quale componente contenga la logica di movimento del trolley (vedi sotto: [Deposit: movimento del trolley](#deposit-movimento-del-trolley)). In questo caso, ci sono due opzioni:
    1. Il WasteService invia *deposit* al trolley appena arriva la richiesta dal Waste truck, e il trolley ne tiene traccia e comincia ad eseguirla appena finita la precedente.
    2. Il WasteService aspetta di ricevere *doneDeposit* per poi inviare un ulteriore richiesta di *deposit* al trolley.


## Architettura

Data la numerosa quantità di componenti che comunicano tra loro, implementare la logica del sistema come un insieme di attori è abbastanza naturale. 

Per scopo di prototipo e simulazione, i Waste truck vengono trattati come attori, ma nel caso reale sarebbero "alieni" al sistema, inviando dati dall'esterno, probabilmente tramite una GUI (web o analoga) usabile dal camionista.

```
Context ctxwastetruck ip [host="localhost" port=8060]

QActor wastetruck context ctxwastetruck {...}
```

Gli attori ricavati dai componenti requisiti e fisici sono:

### Requisito **request** - architettura

Il WasteService è rappresentato da un attore:

- Riceve richieste dai camion
- In base allo stato di storage (vedi [req. storage-check](#requisito-storage-check)) conferma o rifiuta
- Se conferma, o invia il trolley a depositare, oppure se esso è al lavoro gli comunica la presenza di ulteriori richieste

```
Context ctxwasteservice ip [host="localhost" port=8050]

QActor wasteservice context ctxwasteservice {...}
```

Inoltre, per poter interagire con i cassonetti, sono introdotti degli attori di Storage per comunicare modifiche allo storage e inviare aggiornamenti a componenti di controllo per futuri Sprint.

Anche qua due opzioni possibili:
- *Storage manager centrale*: un solo attore che gestisce tutti i cassonetti, tenendo traccia dei contenuti attuali e facendo da unico fronte di comunicazione nel sistema per questi dati. Questo semplificherebbe la comunicazione non richiedendo di distinguere il destinatario in base al materiale, ma richiedendo forse più complessità nel caso, per esempio, in cui i dati di storage siano ricavati da sensori su ogni cassonetto, che quindi dovrebbero aggiornare separatamente lo stesso controllore contenente lo StorageManager.

```
QActor storage context ctxwasteservice {...}
```

- *Storage manager separati*: un attore per cassonetto (rappresentato nell'esempio sopra), che andrà quindi separatamente interpellato in base al tipo di materiale. La soluzione sarebbe la più intuitiva ma con i problemi sopraelencati.

```
QActor storage_glass context ctxwasteservice {...}
QActor storage_paper context ctxwasteservice {...}
```


### Requisito **deposit** - architettura

- **Trolley**: si occupa di controllare il trolley.

```
QActor trolley context ctxwasteservice {...}
```

La logica di movimento è analizzata in seguito: [Deposit: movimento del trolley](#deposit-movimento-del-trolley).

## Deposit: movimento del trolley

Da requisiti, si suppone che le posizioni e grandezza delle aree di HOME, INDOOR, e cassonetti vari, siano decise a priori e a priori comunicate al trolley prima dell'inizio del movimento.

Data una posizione di partenza e di arrivo verso la quale il trolley deve navigare, questo può calcolare due tipi di percorso:
1. Dividendo la stanza in una griglia quadrata di lato RD, il trolley può semplicemente navigare lungo le direzioni cartesiane, prima ad una coordinata della destinazione e poi all'altra.

    ![](./doc/img/navigazione_cart.jpg)

    **PRO**: si dispone già di componenti in grado di generare e seguire percorsi su griglia in direzioni cartesiane, l'implementazione sarebbe quindi semplice
    
    **CONTRO**: più lento dell'alternativa.

2. Il trolley compie un percorso diretto a destinazione ignorando la griglia.

    ![](./doc/img/navigazione_diretta.jpg)

    **PRO**: il percorso sarebbe diretto e più veloce.
    
    **CONTRO**: non disponendo di componenti già implementate per questo scopo, andrebbe programmata la logica di pathfinding e navigazione per questa casistica.

Inoltre, esistono anche più opzioni per quanto riguarda quale componente calcoli il percorso:

1. Il trolley potrebbe calcolare internamente il percorso, ricevendo dal Wasteservice solo istruzioni riguardo a quali destinazioni raggiungere. Questo sposterebbe parte della logica dentro al trolley, rendendo necessarie componenti computazionali più elaborate dell'alternativa.

2. Il trolley potrebbe essere un puro attuatore, ricevendo il percorso già calcolato dal Wasteservice e limitandosi a seguirlo. Questo aumenta il carico sul WasteService (salvo il dedicare processi appositi a questo scopo) e aumenta i dati trasmessi; inoltre richiede per evitare errori che il WasteService sia aggiornato sulla posizione del trolley.

Si è notato inoltre che, data la staticità dell'ambiente, i percorsi in caso di funzionamento regolare hanno un numero ridotto, essendo sempre tra le stesse (e poche) posizioni. Quindi, se necessario, potrebbe essere possibile precalcolare i percorsi, e riutilizzare sempre gli stessi senza richiedere la generazione ogni volta; nel caso per qualche motivo il trolley si ritrovi in una posizione fuori dalle aspettative, il percorso andrebbe comunque calcolato ad hoc.

# TestPlan

### TestPlan: request

Sono necessari mock per la gestione dello storage, a scopo verifica, per il trolley, che deve aspettare un tempo casuale per poi inviare segnali ed un oggetto virtuale che simuli l'arrivo di camion.

- **Test Deny**: il camion invia una richiesta di deposito al RequestHandler e il gestore dello storage risponde con un segnale che indica che lo spazio non è sufficiente; il RequestHandler risponde così con un rifiuto al camion e non invia nulla al trolley.

- **Test Accept Idle**: il camion invia una richiesta di deposito al RequestHandler, il gestore dello storage risponde che c'è sufficiente spazio disponibile, così il RequestHandler comunica l'accettazione del carico al camion e invia un messaggio di inizio deposito al trolley, poi, ricevuto il messaggio di inizio trasporto dal trolley, invia un segnale di avvenuto scarico al camion. Finito il deposito riceve un messaggio che indica la terminazione del trasferimento.

- **Test Accept Move**: durante l'esecuzione del test precedente, il camion invia un'ulteriore richiesta e il gestore dello storage comunica che c'è sufficiente spazio disponibile, così il RequestHandler comunica l'accettazione del carico al camion e al trolley che è presente una nuova richiesta in attesa. Solo dopo il ritorno del trolley che invia un segnale di completamento del deposito precedente, il RequestHandler invia il segnale di deposito al trolley e, ricevuto il messaggio di inizio trasporto da parte del trolley, invia un segnale di avvenuto scarico al camion.
