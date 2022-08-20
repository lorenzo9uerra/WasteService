package it.unibo.radarSystem22.domain.interfaces;

public interface IObservable<OBSERVER> {
    void subscribe(OBSERVER observer);
    void unsubscribe(OBSERVER observer);
}
