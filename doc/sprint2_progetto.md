## Progettazione

La progettazione e lo sviluppo delle componenti software stabilite in fase di analisi è stata divisa in questo modo:

- Led, e classi annesse: F. Lenzi
- WasteserviceStatusGui, e classi annesse: L. Guerra

Punti vari
- Aggiunto updateState a Trolley per fare stopped in caso di errore
- testFixtures in shared per condividere utils di test
- Trovato modo di eseguire test con qak con attori finti, e tutti insieme dalla classe invece che uno alla volta
- Correzione a websockethandler, usa @Component per evitare di inizializzare handler 2 volte con spring