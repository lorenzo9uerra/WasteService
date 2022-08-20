package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.ISonar;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestSonarMock {

    @Before
    public void up() {
        DomainSystemConfig.setTheConfiguration();
        DomainSystemConfig.sonarDelay = 10;
    }

    @Test
    public void testSonar() {
        int maxDelta = 2;

        DomainSystemConfig.simulateSonar = true;
        DomainSystemConfig.sonarMockDelta = -1;
        DomainSystemConfig.sonarMockStartDist = 30;

        ColorsOut.outappl("Running SIMULATION sonar test mode", ColorsOut.ANSI_PURPLE);

        ISonar sonar = DeviceFactory.createSonar();
        SonarTestConsumer testConsumer = new SonarTestConsumer(sonar, maxDelta);
        testConsumer.start();
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(100);
        }

        if (!testConsumer.isSuccess()) {
            throw testConsumer.getAssertErr();
        }
    }

    @Test
    public void testSonarFail() {
        int maxDelta = 1;

        DomainSystemConfig.simulateSonar = true;
        DomainSystemConfig.sonarMockDelta = -5;
        DomainSystemConfig.sonarMockStartDist = 10;

        ColorsOut.outappl("Running SIMULATION sonar test mode (to intentionally fail)", ColorsOut.ANSI_PURPLE);

        ISonar sonar = DeviceFactory.createSonar();
        SonarTestConsumer testConsumer = new SonarTestConsumer(sonar, maxDelta);
        testConsumer.start();
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(100);
        }

        assertFalse(testConsumer.isSuccess());
    }
}
