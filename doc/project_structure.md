# Struttura del Progetto

```
├── doc
├── it.unibo.radarSystem22.domain
├── model.problema
├── model.requisiti
├── qakactor.observer
├── unibolibs
├── versioni_vecchie
├── wasteservice.core
├── wasteservice.led
├── wasteservice.prototype
├── wasteservice.shared
├── wasteservice.sonar
└── wasteservice.statusgui
```

Il progetto è interamente contenuto nella cartella WasteService che verrà
definita come root, e in `/doc` è possibile trovare documentazione di vario
tipo, tra cui i documenti dei vari sprint in formato _markdown_ visualizzabili
direttamente da [github](https://github.com/lnwor/WasteService/tree/main/) e dei
file in formato _html_ che contengono l'analisi dei requisiti, del problema, il
documento di progetto e un breve riassunto. È presente anche la documentazione
che spiega le modifiche al progetto radarSystem.domain fornito dal committente e
la spiegazione del funzionamento di una utility per trasformare degli attori in
*Qak Observer* di una risorsa Coap.

La cartella `/qakactor.observer` contiene il progetto che mira ad estendere qak,
successivamente incluso nelle librerie del linguaggio, che viene però utilizzato
da `wasteservice` al posto delle suddette librerie in quanto contiene qualche
funzionalità aggiuntiva.

In `/it.unibo.radarSystem22.domain` si trova il progetto modificato del software
fornito dal committente.
In `/model.requisiti` si trovano i modelli eseguibili utilizzati per
formalizzare l'analisi dei requisiti, mentre in `/model.problema` quelli
relativi all'analisi del problema.

È possibile trovare dei prototipi funzionanti dell'intero sistema collegati ai
vari sprint nella cartella `/wasteservice.prototype`.

La cartella `/versioni_vecchie` contiene versioni precedenti dell'analisi,
successivamente corrette.

Nelle altre cartelle `wasteservice.*` si trovano i progetti gradle che
compongono il progetto finale di WasteService. In `wasteservice.core` si trovano
i componenti principali della core logic, in `wasteservice.shared` si trovano
utility e classi in comune con gli altri progetti, e
`wasteservice.led`,`wasteservice.statusgui` e `wasteservice.sonar` sono i
progetti dei relativi componenti. In `/unibolibs` invece si trovano le librerie
in comune tra i progetti.
