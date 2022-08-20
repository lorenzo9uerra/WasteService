package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.ISonar;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;
import org.junit.Before;
import org.junit.Test;

public class TestSonarReal {

    @Before
    public void up() {
        DomainSystemConfig.setTheConfiguration();
    }

    @Test
    public void testSonar() {
        int maxDelta = 5; // tolleranza per rumore

        if (!DomainSystemConfig.sonarAvailable) {
            ColorsOut.outappl("Sonar device not available, won't do real sonar test", ColorsOut.ANSI_YELLOW);
            return;
        }

        DomainSystemConfig.simulateSonar = false;

        ColorsOut.outappl("Running REAL sonar test", ColorsOut.ANSI_PURPLE);
        ColorsOut.outappl("Assicurarsi che il sonar sia fissato e " +
                "posizionato davanti ad un oggetto fermo", ColorsOut.ANSI_YELLOW);

        ISonar sonar = DeviceFactory.createSonar();
        SonarTestConsumer testConsumer = new SonarTestConsumer(sonar, maxDelta);
        sonar.activate();
        System.out.println("Attesa attivazione sonar...");
        while (!sonar.isActive()) { BasicUtils.delay(50); };
        System.out.println("Sonar attivo, inizio test");
        testConsumer.start();
        // Se il risultato è coerente per 3 secondi successo, altrimenti termina
        // con errore dentro a SonarTestConsumer
        BasicUtils.delay(2000);
        int lastVal = sonar.getDistance().getVal();
        sonar.deactivate();

        if (!testConsumer.isSuccess()) {
            throw testConsumer.getAssertErr();
        } else {
            ColorsOut.outappl("Sonar ha fornito lo stesso output per 2 secondi con successo", ColorsOut.GREEN);
            System.out.println("Valore finale: " + lastVal);
        }
    }
}
