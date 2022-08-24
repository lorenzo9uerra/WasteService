## Analisi del problema

### Componenti

#### Requisito **sonar-stop** - componenti

Emergono due opzioni principali su come gestire il Led:

- Usare un solo attore Sonar che interagisce direttamente con il dispositivo sonar tramite la libreria e il software fornito e comunica al trolley i messaggi di stop e resume.

- Dividere gli incarichi tra due componenti:
    - SonarShim, che interagirebbe con il sonar tramite la libreria o il
    software fornito, così da rilevare quando la distanza misurata dal sonar è
    minore di DLIMIT e invii degli eventi
    - SonarInterrupter, che rileverebbe gli eventi emessi da SonarShim e li
    tradurrebbe in interrupt, ossia dispatch, da inviare direttamente al trolley

**Conclusione.** Si ritiene migliore la seconda opzione, vale a dire **dividere gli incarichi**, visto che rispetta il principio di singola responsabilità. Inoltre, questo permetterebbe il riutilizzo dell'attore SonarShim in altri contesti, essendo agnostico al dominio.

### Modifiche

In questo caso è necessario modificare *pathexec* per permettere a basicrobot di
accettare gli interrupt langiati da SonarInterrupter.

### Interazione

Per SonarShim è possibile utilizzare un pattern ad eventi o observer, ma sarà
preferibile utilizzare eventi per trasmettere le informazioni, in quanto i
componenti sono attori in un sistema distribuito, il che permette anche di
migliorare l'estendibilità.

```
Event sonarStop : sonatStop(_)
Event sonarResume : sonatResume(_)
```

L'attore SonarInterrupter invece dovrà usare dei dispatch affinchè siano ricevuti
come interrupt da Trolley, sfruttando il linguaggio Qak.

### Architettura Logica

Ecco quindi l'architettura logica finale del sistema in generale per questo SPRINT:

![modello architettura logica](img/sprint3_pro_arch.jpg)

[**Modello eseguibile generale / prototipo.**](../wasteservice.prototype/src/prototype_sprint3.qak) Si noti come rispetto al modello eseguibile dello SPRINT 1, non sia stato necessario modificare niente del codice preesistente ma sia bastato aggiungere gli attori-osservatori.

### Test Plan

#### TestPlan: sonar-stop

Test plan in Kotlin: [TestSonarStop.kt](../wasteservice.prototype/test/it/unibo/TestSonarStop.kt)

- **Test sonar-stop**: invio di *trolleyMove* e durante il percorso inviare *sonarStop* e *sonarResume*, controllando che il trolley si fermi e riprenda correttamente.
