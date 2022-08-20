package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.ISonarObservable;
import it.unibo.radarSystem22.domain.interfaces.ISonarObserver;
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
        DomainSystemConfig.setTheConfiguration();
        DomainSystemConfig.sonarDelay = 3;
    }

    @Test
    public void testSonar() {
        int maxDelta = 1;
        didTerm = false;

        DomainSystemConfig.simulateSonar = true;
        DomainSystemConfig.sonarMockDelta = -1;
        DomainSystemConfig.sonarMockStartDist = 30;

        ColorsOut.outappl("Running SIMULATION sonar observer test mode", ColorsOut.ANSI_PURPLE);

        ISonarObservable sonar = DeviceFactory.createSonarObservable();
        SonarTestObserver testObserver = new SonarTestObserver(sonar, maxDelta);
        testObserver.setTermAction(() -> {
            setDidTerm(true); // gira intorno al limite delle variabili readonly in lambda
            System.out.println("[testSonar] finished observing");
        });
        sonar.subscribe(testObserver);
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

        DomainSystemConfig.simulateSonar = true;
        DomainSystemConfig.sonarMockDelta = -5;
        DomainSystemConfig.sonarMockStartDist = 10;

        ColorsOut.outappl("Running SIMULATION sonar observer test mode (to intentionally fail)", ColorsOut.ANSI_PURPLE);

        ISonarObservable sonar = DeviceFactory.createSonarObservable();
        SonarTestObserver testObserver = new SonarTestObserver(sonar, maxDelta);
        sonar.subscribe(testObserver);
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(100);
        }

        assertFalse(testObserver.isSuccess());
    }

    private static final int LAMBDA_STATUS_START = -100;
    private static final int LAMBDA_STATUS_FIRST_UPDATE = -1;
    private static final int LAMBDA_STATUS_SUCCESS = -200;
    private static final int LAMBDA_STATUS_FAIL = -600;

    @Test
    public void testSonarObserverLambda() {
        final int maxDelta = 1;

        lambdaStatus = LAMBDA_STATUS_START;

        DomainSystemConfig.simulateSonar = true;
        DomainSystemConfig.sonarMockDelta = -1;
        DomainSystemConfig.sonarMockStartDist = 30;

        ColorsOut.outappl("Running SIMULATION sonar observer lambda", ColorsOut.ANSI_PURPLE);

        ISonarObservable sonar = DeviceFactory.createSonarObservable();
        ISonarObserver observer = SonarObserverLambda.make()
                .setActivated(() -> {
                    setLambdaStatus(LAMBDA_STATUS_FIRST_UPDATE);
                })
                .setUpdate(d -> {
                    int status = getLambdaStatus();
                    if (status == LAMBDA_STATUS_START) {
                        setLambdaStatus(LAMBDA_STATUS_FAIL);
                        return;
                    }
                    else if (status == LAMBDA_STATUS_FAIL) {
                        return;
                    }
                    int curVal = d.getVal();
                    if (status != LAMBDA_STATUS_FIRST_UPDATE) {
                        int expectedMin = status - maxDelta;
                        int expectedMax = status + maxDelta;
                        if (curVal < expectedMin || curVal > expectedMax) {
                            System.out.println(String.format("Value out of expected range: [%d, %d], %d",
                                    expectedMin, expectedMax, curVal));
                            status = LAMBDA_STATUS_FAIL;
                        } else {
                            status = curVal;
                        }
                    }
                    setLambdaStatus(curVal);
                })
                .setDeactivated(() -> {
                    setDidTerm(true);
                    if (getLambdaStatus() >= 0) {
                        setLambdaStatus(LAMBDA_STATUS_SUCCESS);
                    }
                });
        sonar.subscribe(observer);
        sonar.activate();
        BasicUtils.delay(100);
        while (sonar.isActive()) {
            BasicUtils.delay(200);
        }

        assertTrue(didTerm);
        assertTrue(lambdaStatus == LAMBDA_STATUS_SUCCESS);
    }

    private void setDidTerm(boolean didTerm) { this.didTerm = didTerm; }

    public int getLambdaStatus() {
        return lambdaStatus;
    }

    public void setLambdaStatus(int lambdaStatus) {
        this.lambdaStatus = lambdaStatus;
    }
}
