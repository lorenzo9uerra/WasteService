[Ritorna all'inizio](../README.md)

## Analisi dei Requisiti

Per il requisito *sonar-stop* è stato incluso un modello eseguibile in
[Qak](#scelta-del-linguaggio-di-modellazione).

### Requisiti e chiarimenti

- **request**: il *WasteService* accetta richieste di deposito da *Waste truck* che arrivano nella zona specificata come INDOOR, che specificicano il tipo di materiale da depositare
    - il sistema può controllare se c'è spazio per un certo materiale: accetta (risposta *loadaccept*), e rifiuta (risposta *loadrejected*) le richieste di deposito in caso contrario
    1. > Domanda: le richieste possono essere gestite anche mentre il robot è in attività?

        Sì, potrebbe arrivare altro camion che chiede.

    2. > Domanda: solo un camion alla volta in INDOOR?

        Sì.

- **deposit**: il *trolley*, quando viene attivato, raccoglie i materiali a INDOOR, e li deposita, in base al tipo, in GLASS BOX o PLASTIC BOX; questa è una *deposit action*:
    1. Raccolta di rifiuti da *Waste truck* in INDOOR
    2. Andare da INDOOR a contenitore rifiuti (\* BOX)
    3. Depositare rifiuti nel contenitore

- **indoor-more-requests**: il *trolley*, terminata una *deposit action*, torna a HOME solo se non ci sono altre richieste da gestire, sennò gestisce subito la richiesta successiva andando a INDOOR

- **led**: nel sistema è presente un led che:
    - è *acceso* se il *trolley* è a HOME
    - *lampeggia* se il *trolley* è in attività
    - è *spento* se il trolley è in stato di *stop*

- **sonar-stop**: è presente un *sonar* che, se misura una distanza sotto DLIMIT (valore prefissato), mette il *trolley* in stato di *stop* fino a che la distanza non torna a DLIMIT, nel qual caso il *trolley* riparte
    1. > Domanda: cosa vuol dire precisamente *stop*? Torna a HOME o rimane lì?

        Si ferma e basta.

- **gui**: è presente una gui (*WasteServiceStatusGUI*) che mostra i seguenti dati:
    - Stato del *trolley* e sua posizione
    1. > Domanda: Posizione del trolley: deve essere precisa o informazione più generale (INDOOR, in mezzo, HOME, ecc)?
    
        Basta una posizione più generale.
    - Carico depositato attuale (in kg)
    - Stato del Led (acceso/spento/lampeggiante)

In questo terzo SPRINT verrà analizzato il requisito rimanente di
**sonar-stop**.

### Glossario

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

### Analisi dei requisiti

#### Analisi **sonar-stop**

La lettura dei valori del sonar verranno fatti tramite software fornito dal
committente, *SonarAlone.c*.

Dai requisiti sono stati individuati due principali messaggi, *trolleyStop* e
*trolleyResume*, che fermeranno il trolley o lo faranno uscire dallo stato di
stop.

Il componente avrà bisogno di comunicare con il resto del sistema, quindi dovrà
essere un attore.

Il tipo di comunicazione con il resto del sistema deve essere definito, incluso il tipo di messaggio costituito da *trolleyStop* e *trolleyResume*.

[Modello eseguibile Sonar](../model.requisiti/src/sonar-stop.qak)

[Test plan sonar-stop](../model.requisiti/test-disabled/TestSonarStop.kt). Viene incluso un test plan in Kotlin con JUnit, attualmente non eseguibile mancando un sonar pilotabile.

### Materiale fornito dal committente

- Robot DDR: viene fornita una componente software, *BasicRobot22*, che implementa comandi primitivi *MOVE = w | s | l | r | h*, e permette di fare *step* in avanti per un certo tempo.
- Sonar: viene fornito un programma in C, *SonarAlone.c*, che stampa su standard output la distanza attualmente rilevata dal sonar, configurando le porte GPIO in questo modo:
    - Porta VCC : pin fisico 4 (+5v)
    - Porta GND : pin fisico 6 (GND)
    - Porta TRIG: pin fisico 11 (WPI 0, BCM 17)
    - Porta ECHO: pin fisico 13 (WPI 2, BCM 27)

- Led: vengono forniti gli script bash e *led25GpioTurnOn.sh* e *led25GpioTurnOff.sh* per accendere e spegnere un Led connesso alla porta GPIO 25 di un Raspberry Pi.
