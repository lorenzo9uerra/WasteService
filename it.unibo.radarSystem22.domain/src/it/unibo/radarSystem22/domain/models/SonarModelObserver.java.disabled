package it.unibo.radarSystem22.domain.models;

import it.unibo.radarSystem22.domain.Distance;
import it.unibo.radarSystem22.domain.SafeUpdateSet;
import it.unibo.radarSystem22.domain.concrete.SonarConcrete;
import it.unibo.radarSystem22.domain.interfaces.IDistance;
import it.unibo.radarSystem22.domain.interfaces.ISonar;
import it.unibo.radarSystem22.domain.interfaces.ISonarObservable;
import it.unibo.radarSystem22.domain.interfaces.ISonarObserver;
import it.unibo.radarSystem22.domain.mock.SonarMock;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class SonarModel implements ISonar, ISonarObservable {
    protected boolean stopped = true; //se true il sonar si ferma
    protected IDistance curVal = new Distance(90);
    protected SafeUpdateSet<ISonarObserver> observers;

    protected SonarModel() {
        observers = new SafeUpdateSet<>();

        sonarSetUp();
    }

    public static ISonar create() {
        if (DomainSystemConfig.simulation || DomainSystemConfig.simulateSonar) {
            return createSonarMock();
        } else {
            return createSonarConcrete();
        }
    }
    public static ISonarObservable createObservable() {
        return (ISonarObservable) create();
    }

    public static ISonar createSonarMock() {
        return new SonarMock();
    }
    public static ISonar createSonarConcrete() {
        return new SonarConcrete();
    }

    protected abstract void sonarSetUp();
    protected abstract void sonarProduce(); // invia echo del sonar, modifica curVal

    protected void updateDistance(IDistance dist) {
        curVal = dist;
        observers.safeForEach(obs -> obs.update(curVal));

        if (DomainSystemConfig.sonarVerbose)
            ColorsOut.out("\tCurrent distance: " + curVal.getVal());
    }
    protected void updateDistance(int val) { updateDistance(new Distance(val)); }

    @Override
    public void activate() {
        if (!stopped)
            throw new RuntimeException("Sonar già attivato!");
        stopped = false;
        observers.safeForEach(ISonarObserver::activated);
        new Thread(() -> {
            while (!stopped) {
                sonarProduce();
            }
            observers.safeForEach(ISonarObserver::deactivated);
        }).start();
    }

    @Override
    public void deactivate() {
        if (!stopped) {
            stopped = true;
            if (DomainSystemConfig.sonarVerbose)
                ColorsOut.out("\tDeactivated sonar", ColorsOut.YELLOW);
        }
    }

    @Override
    public IDistance getDistance() {
        return curVal;
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }

    @Override
    public void subscribe(ISonarObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(ISonarObserver observer) {
        observers.remove(observer);
    }
}
