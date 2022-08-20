package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.IDistance;
import it.unibo.radarSystem22.domain.interfaces.ISonar;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

import static org.junit.Assert.*;

public class SonarTestConsumer extends Thread {
    private ISonar sonar;
    private int maxDelta;
    private boolean success;
    private AssertionError assertErr;

    public SonarTestConsumer(ISonar sonar, int maxDelta) {
        this.sonar = sonar;
        this.maxDelta = maxDelta;
        this.success = true;
        this.assertErr = null;
    }

    @Override
    public void run() {
        if (DomainSystemConfig.sonarVerbose)
            System.out.println("[Sonar test consumer] start test...");
        int prevValue = sonar.getDistance().getVal();
        while (sonar.isActive()) {
            BasicUtils.delay(DomainSystemConfig.sonarDelay / 2);
            IDistance dist = sonar.getDistance();
            int curVal = dist.getVal();
            int minExpected = prevValue - maxDelta;
            int maxExpected = prevValue + maxDelta;
            try {
                assertTrue(curVal >= minExpected && curVal <= maxExpected);
            // assertTrue in sub-thread lancia eccezione senza far fallire test, passa a thread principale
            } catch (AssertionError e) {
                System.out.println("[Sonar test consumer] excessive change " + (curVal - prevValue) + ", failing");
                success = false;
                assertErr = e;
                return; //simula fallimento, chiuderebbe thread
            }
            prevValue = curVal;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public AssertionError getAssertErr() {
        return assertErr;
    }
}
