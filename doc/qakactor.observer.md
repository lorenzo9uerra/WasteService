# Qakactor Observer

La utility Qakactor Observer è composta da una classe Kotlin `CoapObserverActor`, e una utility per utilizzarla da Qak `coapObserverUtil`. Permette di rendere degli attori Qak Observer di una risorsa Coap, sia generica che nello specifico altri attori Qak che sono risorse osservabili.

Il progetto è disponibile [su Github](https://github.com/lnwor/WasteService/tree/main/qakactor.observer).

Sempre su Github si può scaricare la [libreria in formato jar](https://github.com/lnwor/WasteService/blob/main/unibolibs/qakactor.observer-1.0.jar?raw=true)

## Esempio

Un esempio di uso è il seguente:

```
// Inizio file
Dispatch coapUpdate : coapUpdate(RESOURCE, VALUE)

// [...] interno di attore
State s0 initial {
   qrun coapObserverUtil.startObserving(myself, "observable")
   /* qrun coapObserverUtil.startObserving(myself, "ctx_observertest", "observable") */
   /* qrun coapObserverUtil.startObservingHost(myself, "localhost:9000", "ctx_observertest/observable") */
}
Goto listen

State listen {
}
Transition t0 whenMsg coapUpdate -> handleUpdate

State handleUpdate {
    onMsg(coapUpdate : coapUpdate(RESOURCE, VALUE)) {
        // [...]
    }
}
Goto listen
```

Un esempio completo è [qui disponibile](https://github.com/lnwor/WasteService/blob/main/qakactor.observer/test/observerdemo.qak)

## Funzionamento e scelte di sviluppo

### Handler Coap

La libreria fornisce una classe CoapHandler, `CoapObserverActor`, che riceve in costruzione un attore "proprietario". Quando arrivano degli update Coap, li invia al proprietario incapsulati in un **dispatch** di tipo coapUpdate, che va quindi definito all'interno del file qak.

```
Dispatch coapUpdate : coapUpdate(RESOURCE, VALUE)
```

Il proprietario può quindi gestire aggiornamenti Coap come un qualunque altro dispatch, distinguendo la risorsa che li invia tramite il primo parametro RESOURCE. Viene anche impostata come mittente del messaggio, ma è stato ritenuto più intuitivo per l'uso dentro Qak renderla disponibile anche all'interno di payloadArg(0).

È stato scelto di implementare l'osservazione come dispatch e non evento, per rappresentare la natura specifica dell'osservazione: non viene inviato il dato a tutti, ma solo a chi lo osserva.

### Classe di utility

La classe fornita `coapObserverUtil` offre metodi statici per iniziare e finire di osservare una risorsa, sia per host e uri assoluti, sia in termini relativi a Qak fornendo nome dell'attore e opzionalmente del contesto (usando il contesto dell'attore osservante di default).

Possono essere semplicemente lanciati tramite `qrun` usando la keyword `myself` come primo argomento.
