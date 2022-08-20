package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.IDistance;
import it.unibo.radarSystem22.domain.interfaces.ISonarObservable;
import it.unibo.radarSystem22.domain.interfaces.ISonarObserver;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class SonarTestObserver implements ISonarObserver {
    private ISonarObservable sonar;
    private int maxDelta;
    private Optional<Integer> prevValueOpt;
    private boolean success;
    private AssertionError assertErr;
    private Runnable termAction;

    public SonarTestObserver(ISonarObservable sonar, int maxDelta) {
        this.sonar = sonar;
        this.maxDelta = maxDelta;
        this.success = false; // Impostato a true dentro activate, per assicurarsi che ci passi
        this.assertErr = null;
        this.prevValueOpt = Optional.empty();
        this.termAction = null;
    }

    @Override
    public synchronized void activated() {
        this.success = true;
        System.out.println("[Sonar test observer] started");
    }

    @Override
    public synchronized void update(IDistance distance) {
        if (DomainSystemConfig.sonarVerbose)
            System.out.println("[Sonar test observer] update at distance " + distance);

        // Per la prima iterazione, prendi la distanza iniziale
        // come valore precedente (quindi non fare niente)
        int prevValue = prevValueOpt.orElse(distance.getVal());
        int curVal = distance.getVal();
        int minExpected = prevValue - maxDelta;
        int maxExpected = prevValue + maxDelta;
        try {
            assertTrue(curVal >= minExpected && curVal <= maxExpected);
        // assertTrue in sub-thread lancia eccezione senza far fallire test, passa a thread principale
        } catch (AssertionError e) {
            System.out.println("[Sonar test consumer] excessive change " + (curVal - prevValue) + ", failing");
            success = false;
            assertErr = e;
            sonar.unsubscribe(this);
        }
        prevValueOpt = Optional.of(curVal);
    }

    @Override
    public synchronized void deactivated() {
        if (termAction != null)
            termAction.run();
        System.out.println("[Sonar test observer] ended");
    }

    public void setTermAction(Runnable termAction) { this.termAction = termAction; }

    public boolean isSuccess() {
        return success;
    }

    public AssertionError getAssertErr() {
        return assertErr;
    }
}
