## Analisi del problema

### Componenti

#### Requisito **led** - componenti

Emergono due opzioni principali su come gestire il Led:

- Usare un solo attore LedActor, che gestisce sia i dati di dominio, e interagisce direttamente con il dispositivo. Quindi, questo singolo componente riceverebbe i dati dello stato del Trolley, e si occuperebbe di accensione e spegnimento del Led.

- Dividere gli incarichi tra due componenti: LedActor, che interagirebbe con il dispositivo tramite la libreria in base a istruzioni ricevute dall'esterno, e LedController, che riceverebbe i dati dello stato del trolley e interagirebbe con LedActor.

**Conclusione.** Si ritiene migliore la seconda opzione, vale a dire **dividere gli incarichi**, visto che rispetta il principio di singola responsabilità. Inoltre, permetterebbe il riutilizzo dell'attore LedActor in altri contesti, essendo agnostico al dominio.

### Interazione

Il problema della comunicazione per **led** e **gui** è analogo, quindi verranno analizzati insieme.

Escludendo di usare comunicazione punto-punto come dispatch e richieste, data la scarsa espandibilità, e la complicazione nella necessità di modificare ogni attore da analizzare, le opzioni attuali sono due:

- Usare eventi: si potrebbero aggiungere emissioni di eventi contenenti i dati di stato attuale dei vari attori di interesse per Led e WasteServiceStatusGui, incluso il Led i cui dati sono visualizzati dalla Gui.

    ```
    Event trolleyStatus : trolleyStatus(STATUS,POS)
    Event storageStatus : storageStatus(CONTENTS)
    Event ledStatus : ledStatus(STATUS) //on|blink|off
    ```

- Osservabilità: come detto nel progetto dello SPRINT 1, i vari attori sono osservabili tramite COAP. Quindi un'opzione sarebbe rendere Led e WasteServiceStatusGui osservatori degli attori rilevanti (e rendere osservabile il Led), e aggiungere alle informazioni osservabili degli attori di interesse eventuali dati mancanti.

**Conclusione.** Viene ritenuta come opzione migliore la seconda, l'**osservabilità**, visto che gli attori creati nello SPRINT 1 sono già risorse osservabili con le informazioni necessarie; quindi, non sarebbe necessaria alcuna modifica al software già sviluppato per adempiere a questo requisito, il che sarebbe un grande vantaggio. Inoltre, il fatto che COAP sia un protocollo già definito renderebbe ancora più facile l'estendibilità.


### Architettura Logica

Ecco quindi l'architettura logica finale del sistema in generale per questo SPRINT:

![modello architettura logica]()
<inserire immagine>

[**Prototipo eseguibile**]()
<inserire prototipo>

### Test Plan

#### TestPlan: led

Test plan in Kotlin: []()

- **Test Led**: creazione di server COAP "fasullo" allo stesso indirizzo del contesto del sistema principale osservato. Invio di dati che portano ai vari stati del Led, poi verifica che questi stati siano stati raggiunti.


#### TestPlan: gui

Test plan in Kotlin: []()

- **Test Gui**: creazione di server COAP "fasullo" allo stesso indirizzo del contesto del sistema principale osservato. Invio di dati che portano a vari stati della Gui, e verifica che la pagina risultante venga modificata correttamente.
