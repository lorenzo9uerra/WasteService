package it.unibo.radarSystem22.domain.observable;

import it.unibo.radarSystem22.domain.interfaces.IDistance;
import it.unibo.radarSystem22.domain.interfaces.IDistanceObservable;
import it.unibo.radarSystem22.domain.interfaces.IDistanceObserver;

import java.util.HashSet;
import java.util.Set;

public class DistanceObservableSimple implements IDistanceObservable {
    private IDistance value;
    private final Set<IDistanceObserver> observers;

    public DistanceObservableSimple(IDistance value) {
        this.value = value;
        this.observers = new HashSet<>();
    }

    /**
     * Sets distance and calls all observers. Not meant for use with high
     * amounts of observers, as that will slow down .set; for that, use a
     * more appropriate implementation of the observer pattern.
     * @param distance
     */
    @Override
    public void set(IDistance distance) {
        boolean callObservers = value.getVal() != distance.getVal();
        value = distance;

        if (callObservers) {
            observers.forEach(obs -> obs.update(value));
        }
    }

    @Override
    public IDistance get() {
        return value;
    }

    @Override
    public void subscribe(IDistanceObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribe(IDistanceObserver observer) {
        observers.remove(observer);
    }
}
