# Analisi del problema

## Prima analisi

### Architettura V1

Le richieste arrivano dall'esterno; viene considerato nel contesto del sistema il camion come un "oggetto" attivo. 

Una prima architettura tratta i vari componenti come attori

Due versioni (si accoda richieste il Trolley, o no?)

StorageManager osservabile per GUI?

Schema architettura:

![schema dell'architettura](doc/img/architettura1.jpg)

Questa prima architettura evidenzia i vari componenti e i collegamenti tra loro, senza soffermarsi troppo sul comportamento interno (in particolare quello del Trolley) per ora.

#### Requisiti

*Fare lista dei requisiti e come sono stati implementati nell'architettura per ora*

#### Prototipo e modifiche

A realizzare un primo prototipo in Qak, sono emerse le prime problematiche: 

- Necessaria aggiunta di un dispatch pickedUp da RequestHandler a Camion per notificare il camion dell'avvenuta raccolta dei rifiuti (usecase: robot impegnato durante arrivo del camion) e permettergli di andare via
- Rinominazione di alcuni messaggi (storageAt (evento) -> storageUpdate, deposit (trolley->storage) -> storageDeposit) per pulizia dati i diversi dati contenuti rispetto ad altri con lo stesso nome
- stop/resume da Sonar diventano eventi per migliore espandibilità
