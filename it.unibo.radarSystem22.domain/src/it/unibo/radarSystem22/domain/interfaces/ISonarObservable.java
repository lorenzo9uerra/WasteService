package it.unibo.radarSystem22.domain.interfaces;

public interface ISonarObservable {
    void subscribe(ISonarObserver observer);
    void unsubscribe(ISonarObserver observer);
}
