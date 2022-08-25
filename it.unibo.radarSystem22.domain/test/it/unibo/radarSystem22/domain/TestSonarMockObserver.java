package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.*;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestSonarMockObserver {
    // Entrambi usati per girare intorno
    // a richiesta variabili effectively final
    // dentro le lambda
    private boolean didTerm = false;
    private int lambdaStatus = 0;

    @Before
    public void up() {
        DomainSystemConfig.sonarDelay = 3;
        DomainSystemConfig.sonarVerbose = true;
        DomainSystemConfig.simulateSonar = true;
   }

    @Test
    public void testSonar() {
        int maxDelta = 1;
        didTerm = false;

        DomainSystemConfig.sonarMockDelta = -1;
        DomainSystemConfig.sonarMockStartDist = 30;

        ColorsOut.outappl("Running SIMULATION sonar observer test mode", ColorsOut.ANSI_PURPLE);

        ISonar sonar = DeviceFactory.createSonar();
        ISonarObservable sonarObservable = DeviceFactory.makeSonarObservable(sonar);
        IDistanceObservable distanceObservable = DeviceFactory.makeDistanceObservable(sonar);
        SonarTestObserver testObserver = new SonarTestObserver(sonarObservable, distanceObservable, maxDelta);
        testObserver.setTermAction(() -> {
            setDidTerm(true); // gira intorno al limite delle variabili readonly in lambda
            System.out.println("[testSonar] finished observing");
        });
        sonarObservable.subscribe(testObserver);
        distanceObservable.subscribe(testObserver);
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(100);
        }
        BasicUtils.delay(300);

        assertTrue(didTerm);

        if (!testObserver.isSuccess()) {
            throw testObserver.getAssertErr();
        }
    }

    @Test
    public void testSonarFail() {
        int maxDelta = 1;

        DomainSystemConfig.sonarMockDelta = -5;
        DomainSystemConfig.sonarMockStartDist = 10;

        ColorsOut.outappl("Running SIMULATION sonar observer test mode (to intentionally fail)", ColorsOut.ANSI_PURPLE);

        ISonar sonar = DeviceFactory.createSonar();
        ISonarObservable sonarObservable = DeviceFactory.makeSonarObservable(sonar);
        IDistanceObservable distanceObservable = DeviceFactory.makeDistanceObservable(sonar);
        SonarTestObserver testObserver = new SonarTestObserver(sonarObservable, distanceObservable, maxDelta);
        sonarObservable.subscribe(testObserver);
        distanceObservable.subscribe(testObserver);
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(100);
        }

        assertFalse(testObserver.isSuccess());
    }

    private void setDidTerm(boolean didTerm) { this.didTerm = didTerm; }
}
