package it.unibo.radarSystem22.domain.models;

import it.unibo.radarSystem22.domain.Distance;
import it.unibo.radarSystem22.domain.observable.DistanceObservableSimple;
import it.unibo.radarSystem22.domain.concrete.SonarConcrete;
import it.unibo.radarSystem22.domain.interfaces.*;
import it.unibo.radarSystem22.domain.mock.SonarMock;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

public abstract class SonarModel implements ISonar {
    protected boolean stopped = true; //se true il sonar si ferma
    protected final IDistanceMutable distance;
    protected ISonarEventHandler eventHandler;

    protected SonarModel(IDistanceMutable distance, ISonarEventHandler eventHandler) {
        this.distance = distance;
        this.eventHandler = eventHandler;
        sonarSetUp();
    }

    protected SonarModel(IDistanceMutable distance) { this(distance, null); }

    protected SonarModel() { this(new DistanceObservableSimple(new Distance(90)), null); }

    public static ISonar create() {
        if (DomainSystemConfig.simulation || DomainSystemConfig.simulateSonar) {
            return createSonarMock();
        } else {
            return createSonarConcrete();
        }
    }

    public static ISonar createSonarMock() {
        return new SonarMock();
    }
    
    public static ISonar createSonarConcrete() {
        return new SonarConcrete();
    }

    protected abstract void sonarSetUp();
    protected abstract void sonarProduce(); // invia echo del sonar, modifica distance

    protected void updateDistance(IDistance dist) {
        distance.set(dist);

        if (DomainSystemConfig.sonarVerbose)
            ColorsOut.out("\tCurrent distance: " + distance.get());
    }
    protected void updateDistance(int val) { updateDistance(new Distance(val)); }

    public void addSonarEventHandler(ISonarEventHandler eventHandler) {
        if (this.eventHandler == null) {
            this.eventHandler = eventHandler;
        } else {
            throw new IllegalStateException("Cannot add sonar observable component, already set");
        }
    }

    public ISonarEventHandler getSonarEventHandler() {
        return eventHandler;
    }

    public IDistanceMutable getMutableDistance() {
        return distance;
    }

    @Override
    public void activate() {
        if (!stopped)
            throw new RuntimeException("Sonar già attivato!");
        stopped = false;
        new Thread(() -> {
            while (!stopped) {
                sonarProduce();
            }
        }).start();
        if (eventHandler != null)
            eventHandler.onActivate();
    }

    @Override
    public void deactivate() {
        if (!stopped) {
            stopped = true;
            if (eventHandler != null)
                eventHandler.onDeactivate();
            if (DomainSystemConfig.sonarVerbose)
                ColorsOut.out("\tDeactivated sonar", ColorsOut.YELLOW);
        }
    }

    @Override
    public IDistance getDistance() {
        return distance.get();
    }

    @Override
    public boolean isActive() {
        return !stopped;
    }
}
