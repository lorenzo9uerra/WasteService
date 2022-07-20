## Progettazione

### Gestione delle richieste

La parte di WasteService che da analisi interagisce con WasteTruck viene implementata come server web: la pagina servita fa le veci dell'attore nel modello di analisi WasteTruck.

Questa pagina contiene script che comunicano con il server di WasteService tramite WebSocket, permettendo sia di inviare richieste e ricevere risposte, sia di ascoltare sulla connessione per la notifica di carico raccolto. WasteTruck diventa quindi, da attore, una pagina web, che viene aperta dal pilota del Waste Truck. 

Il server web di WasteService si occupa solo della parte di *core business* correlata alla gestione delle richieste dei Waste Truck.

TODO: come interagisce con il WasteService attore/la parte che gestisce il trolley?