package it.unibo.radarSystem22.domain.observable;

import it.unibo.radarSystem22.domain.interfaces.ISonarEventHandler;
import it.unibo.radarSystem22.domain.interfaces.ISonarObservable;
import it.unibo.radarSystem22.domain.interfaces.ISonarObserver;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles calling observers; as it is handled in the same thread,
 * it's not meant to handle a high amount of observers, use more
 * appropriate implementations for that.
 */
public class SonarObservableHandlerSimple implements ISonarEventHandler, ISonarObservable {
    private final Set<ISonarObserver> observers = new HashSet<>();

    @Override
    public void onActivate() {
        observers.forEach(ISonarObserver::activated);
    }

    @Override
    public void onDeactivate() {
        observers.forEach(ISonarObserver::deactivated);
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
