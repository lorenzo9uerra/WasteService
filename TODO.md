- Eliminare moreRequests ed inviare doneDeposit immediatamente dopo lo scarico ai cassonetti

- Aggiungere test per trolley con stop

DOPO RICEVIMENTO 30/06
- Usare nomi giusti, loadaccept/reject, WasteService, WasteTruck, ecc

- Modello eseguibile di requisiti (uno per uno)

- Da solo request, deve essere WasteService a inviare la risposta da requisiti

- storage-check fa parte di request

- testPlan: uno per caso, esempio storage-check (circa, va tolto inr ealtà il requisito, quindi in request)
    - Uno dove parte vuoto, chiede richiesta che ci sta, verifica che ci sia accept
    - Uno dove parte non vuoto, chiede richiesta che non ci sta, verifica che ci sia reject


- togliere "è emersa a realizzare ~~il primo prototipo~~" e dire il perchè

- Invece di pickedUp, potrebbe essere risposta in ritardo a richiesta di deposito
    - Dire ragioni di due versioni (risposta delay non dice niente finoa  risposta, quindi meno user-friendly, per esempio)
    - Non è detto che camion possa ricevere messaggi, request è iniziativa sua
    - "Si può fare anche così, MA [...]"

- Gerarchia dei requisiti, individuare il "core business" del sistema
    - Serve per poter avere *goal* di ogni sprint SCRUM

- Sprint 1 può essere la parte centrale (quindi no led, sonar, gui), si analizza, testplanna, progetta, e implementa quello, poi gli sprint dopo si aggiungono gli extra

- Si può dire già in fase di analisi che la GUI la si fa come web application

- Avere prototipi man mano, ma facilmente eseguibili e modificabili

- Test plan: già in codice, semplicemente non eseguibile mancando le classi per avere un test funzionante ed eseguibile; quindi si devono usare sì delle tecnologie precise per esprimere il concetto in qualche modo
    - Non deve essere necessariamente QUELLO preciso come tecnologia, ma deve solo cambiare il meno possibile: indi per cui è comodo avere già delle astrazioni, così da ridurre al minimo ciò che andrà cambiato una volta implementato il progetto e realizzato il test vero
    - Vincolarsi non alla tecnologia, ma ai concetti dell'analisi del problema

- Anche navigation non è un *vero* requisito, ma un problema che emerge in fase di analisi del problema
    - "Posizione del trolley: deve essere precisa o informazione più generale (INDOOR, in mezzo, HOME, ecc)?
    - Non necessariamente è il trolley a navigare

- Nei requisiti vanno SOLO requisiti che sono DIRETTAMENTE ottenibili dal testo scritto dal cliente: cose come "storage-check" e "navigation" sono problematiche lato analista che emergono a parte e quindi da mettere in analisi (if any, tipo storage-check direttamente sta dentro al requisito request)

- Per ogni termine, bisogna chiedere al committente cosa intende e se ha già del software/materiale/altro

- DOMANDA: ma quindi la posizione serve precisa o no?
    - RISPOSTA: basta indicativa (è a HOME, INDOOR, etc)

- Ma i test *plan* di navigazione (vedere se arriva alla posizione giusta) quindi servono? Non essendo un requisito esplicito
    - RISPOSTA: NO, basta vedere se rispetta requisiti stretti (quindi dopo richiesta, se a un certo punto arrivano nel cassonetto i materiali)
    - Il test si può poi fare dopo lo sviluppo se lo si ritiene necessario, ma non serve nel test plan in analisi

- Trolley deve essere puro attuatore, no business logic
    - DOMANDA: è considerabile attuatore qualcosa che riceve coordinata e ci va? Che quindi include calcolo del percorso, ma non conosce cose specifiche del dominio (tipo che esiste destinazione INDOOR, ecc.)
    - RISPOSTA: attuatore è attuatore, non è la gamba a pensare cosa vuol dire "correre" o "camminare"; il discorso dell'estendibilità si può tradurre con "più menti" (processi, ecc.) che gestiscono più trolley
    - BasicRobot ha pure path executor, quindi creazione path dall'alto e mandare a robot sarebbe fattibile più facilmente
    - Non è obbligatorio BasicRobot comunque, l'opzione diagonale rimane un'opzione