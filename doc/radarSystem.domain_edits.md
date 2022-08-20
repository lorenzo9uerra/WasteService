## Modifiche a radarSystem22.domain

### Documentazione

La libreria radarSystem22.domain, realizzata per progetti precedenti e riutilizzata in WasteService per gestire Led e Sonar, è stata modificata per permetterne l'osservabilità.

Sono state introdotte le interfaccie ISonarObservable e IDistanceObservable, così riassumibili*:

```Java
public interface ISonarObservable {
    void subscribe(ISonarObserver observer);
    void unsubscribe(ISonarObserver observer);
}
public interface IDistanceObservable {
    void subscribe(IDistanceObserver observer);
    void unsubscribe(IDistanceObserver observer);

    void set(IDistance distance);
    IDistance get();
}
```

mentre i corrispettivi Observer sono:

```Java
public interface ISonarObserver {
    void activated();
    void deactivated();
}
public interface IDistanceObserver {
    void update(IDistance dist);
}
```

*\* Nota: le interfaccie su file sono in realtà diverse, estendendo diverse interfacce per permettere una maggiore indipendenza ereditaria tra i componenti software.*

Queste sono utilizzabili per osservare aggiornamenti del sonar (attivazione e disattivazione) e della distanza.

Viene fornita un'implementazione degli observer utilizzabile in locale *per quantità ridotte di observer*, che altrimenti porterebbe a un rallentamento nelle operazioni primitive del sonar con grandi quantità di utenti (utile però per singoli observer o poco più, come nel caso del sistema), vale a dire `DistanceObservableSimple` e `SonarObservableHandlerSimple`.

Per ottenere un riferimento a `ISonarObservable` e `IDistanceObservable`, è possibile usare i nuovi metodi `DeviceFactory.makeSonarObservable(ISonar sonar)` e `DeviceFactory.makeDistanceObservable(ISonar sonar)`. Di default, passando sonar "standard" della libreria, viene usata la suddetta implementazione degli observer.

Può essere poi creato un observer ad-hoc per il caso d'uso per il quale si vuole osservare la risorsa in questione.

TODO: Coap

### Analisi e progetto

È stato scelto di realizzare le nuove funzionalità seguendo questi principi:

* Ridurre quanto possibile le modifiche alle classi pre-esistenti, prediligendo composizione e estensione
* Rendere flessibile il sistema, non legando le classi di basso livello a specifiche implementazioni o "opinioni"

Quindi, sono state realizzate diverse interfaccie, le principali riassunte di seguito:

* *IDistanceMutable*: racchiude il concetto di "distanza modificabile", con set e get, SonarModel "vede" solo questa interfaccia non necessitando quindi di conoscere il concetto di osservabilità.
* *IDistanceObservable*: estende IDistanceMutable, permettendo alle implementazioni di includere aggiornamenti agli observer internamente al set senza che le classi che usano IDistanceMutable lo debbano conoscere, grazie al polimorfismo. Interfaccia come sopra.
* *ISonarEventHandler*: handler di callback di attivazione e disattivazione chiamato dal SonarModel, vedi sotto.
* *ISonarObservable*: interfaccia come sopra.

Le modifiche a SonarModel sono state contenute al minimo, come detto a inizio sezione. È stata sostituita l'istanza di IDistance con IDistanceMutable, che viene chiamata per modificare il valore. Questa viene di default istanziata con un DistanceObservableSimple, ma può appunto essere una qualunque altra implementazione anche non observable, slegando le due classi.

Inoltre, SonarModel opzionalmente può disporre di un *ISonarEventHandler* che viene in tal caso chiamato all'attivazione e disattivazione del Sonar. Viene fornita l'implementazione `SonarObservableHandlerSimple`, che all'attivazione dei callback chiama le funzioni corrispondenti degli Observer registrati internamente all'handler.