package it.unibo.radarSystem22.domain.interfaces;

public interface IDistanceObservable {
    void set(IDistance distance);
    void get(IDistance distance);

    void subscribe(IDistanceObserver observer);
    void unsubscribe(IDistanceObserver observer);
}
