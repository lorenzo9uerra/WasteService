# WasteService - Analisi dei Requisiti

## Requisiti

- **request**: il *WasteService* accetta richieste di deposito da *Waste truck* che arrivano nella zona specificata come INDOOR, che specificicano il tipo di materiale da depositare
    - il sistema può controllare se c'è spazio per un certo materiale: accetta (risposta *loadaccept*), e rifiuta (risposta *loadrejecetd*) le richieste di deposito in caso contrario
    1. > Domanda: le richieste possono essere gestite anche mentre il robot è in attività?

        Sì, potrebbe arrivare altro camion che chiede.

    2. > Domanda: solo un camion alla volta in INDOOR?

        Sì.

- **deposit**: il *trolley*, quando viene attivato, raccoglie i materiali a INDOOR, e li deposita, in base al tipo, in GLASS BOX o PLASTIC BOX; questa è una *deposit action*:
    1. Raccolta di rifiuti da *Waste truck* in INDOOR
    2. Andare da INDOOR a contenitore rifiuti (* BOX)
    3. Depositare rifiuti nel contenitore
    - A lavoro finito, il *trolley* torna a HOME solo se non ci sono altre richieste da gestire, sennò gestisce subito la richiesta successiva andando a INDOOR

- **led**: nel sistema è presente un led che:
    - è *acceso* se il *trolley* è a HOME
    - *lampeggia* se il *trolley* è in attività
    - è *spento* se il trolley è in stato di *stop*

- **sonar-stop**: è presente un *sonar* che, se misura una distanza sotto DLIMIT (valore prefissato), mette il *trolley* in stato di *stop* fino a che la distanza non torna a DLIMIT, nel qual caso il *trolley* riparte
    1. > Domanda: cosa vuol dire precisamente *stop*? Torna a HOME o rimane lì?

        Sì ferma e basta.

- **gui**: è presente una gui (*WasteServiceStatusGUI*) che mostra i seguenti dati:
    - Stato del *trolley* e sua posizione
    1. > Domanda: Posizione del trolley: deve essere precisa o informazione più generale (INDOOR, in mezzo, HOME, ecc)?
    
        Basta una posizione più generale.
    - Carico depositato attuale (in kg)
    - Stato del Led (acceso/spento/lampeggiante)

Da questa ananlisi, emerge che il *core business* del sistema è costituito dai requisiti di **request** e **deposit**. Un primo sprint SCRUM sarà quindi lo sviluppo a partire da questi requisiti centrali, mentre i successivi sprint implementeranno le funzionalità aggiuntive di **led**, **sonar-stop**, e **gui**.

## Glossario

- *WasteService*: il servizio centrale che risponde alle richieste dei *waste truck*

- *Waste truck*: i camion che arrivano dall'esterno a depositare rifiuti

- Area di servizio: **INDOOR**, **PlasticBox**, **GlassBox**, aree definite nella creazione del sistema e punti di riferimento per il *trolley*. In particolare:
    - INDOOR: area dove i *waste truck* si fermano a lasciare il proprio carico, facendo una richiesta di deposito. Essa può contenere un *waste truck* alla volta
    - PlasticBox: area dove depositare la plastica
    - GlassBox: area dove depositare il vetro

- *transport trolley* (o *trolley*): robot DDR (differential drive robot), di dimensione approssimabile a quadrato di lato RD, usato per trasportare i rifiuti nel sistema

- *Service-manager*: umano che supervisiona il sistema tramite la *WasteServiceStatusGUI*

- *Sonar*: sensore che misura distanza

- *Led*: spia luminosa

## Materiale fornito dal committente

<chiedere a committente>
