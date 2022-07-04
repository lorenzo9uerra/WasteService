# WasteService - Analisi dei Requisiti

## Requisiti

- **request**: il sistema accetta richieste di deposito da *camion* che arrivano nella zona specificata come INDOOR, che specificicano il tipo di materiale da depositare
    1. `Domanda: le richieste possono essere gestite anche mentre il robot è in attività?`
    Sì, potrebbe arrivare altro camion che chiede.
    2. `Domanda: solo un camion alla volta in INDOOR?`
    Sì.

- **storage-check**: il sistema può controllare se c'è spazio per un certo materiale, e rifiuta le richieste di deposito in caso contrario

- **navigation**: il *trolley* deve essere in grado di navigare tra varie aree preimpostate per adempiere al proprio lavoro

- **deposit**: il *trolley*, quando viene attivato, raccoglie i materiali a INDOOR, e li deposita, in base al tipo, in GLASS BOX o PLASTIC BOX
    - A lavoro finito, il *trolley* torna a HOME solo se non ci sono altre richieste da gestire, sennò gestisce subito la richiesta successiva

- **led**: nel sistema è presente un led che:
    - è *acceso* se il *trolley* è a HOME
    - *lampeggia* se il *trolley* è in attività
    - è *spento* se il trolley è in stato di *stop*

- **sonar-stop**: è presente un *sonar* che, se misura una distanza sotto DLIMIT (valore prefissato), mette il *trolley* in stato di *stop* fino a che la distanza non torna a DLIMIT, nel qual caso il *trolley* riparte
    1. `Domanda: cosa vuol dire precisamente *stop*? Torna a HOME o rimane lì?`
    Sì ferma e basta.

- **gui**: è presente una gui (*WasteServiceStatusGUI*) che mostra i seguenti dati:
    - Stato del *trolley* e sua posizione
    - Carico depositato attuale (in kg)
    - Stato del Led (acceso/spento/lampeggiante)

## Glossario

- Area di servizio: **INDOOR**, **PlasticBox**, **GlassBox**, aree definite nella creazione del sistema e punti di riferimento per il *trolley*. In particolare:
    - INDOOR: area dove i *camion* si fermano a lasciare il proprio carico, facendo una richiesta di deposito. Essa può contenere un camion
    - PlasticBox: area dove depositare la plastica
    - GlassBox: area dove depositare il vetro

- *trolley*: robot DDR (differential drive robot), di dimensione approssimabile a quadrato di lato RD, usato per trasportare i rifiuti nel sistema

- *Service-manager*: umano che supervisiona il sistema tramite la *WasteServiceStatusGUI*

- *Sonar*: sensore che misura distanza
