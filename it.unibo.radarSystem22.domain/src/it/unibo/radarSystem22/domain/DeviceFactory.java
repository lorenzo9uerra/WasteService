package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.concrete.RadarDisplay;
import it.unibo.radarSystem22.domain.interfaces.*;
import it.unibo.radarSystem22.domain.models.LedModel;
import it.unibo.radarSystem22.domain.models.SonarModel;
import it.unibo.radarSystem22.domain.observable.SonarObservableHandlerSimple;

public class DeviceFactory {
    public static ILed createLed() {
        return LedModel.create();
    }

    public static IRadarDisplay createRadarDisplay() {
        return RadarDisplay.getRadarDisplay();
    }
    public static IRadarDisplay createRadarGui() {
        return createRadarDisplay();
    }

    public static ISonar createSonar() {
        return SonarModel.create();
    }

    public static ISonarObservable makeSonarObservable(ISonar sonar) {
        if (sonar instanceof SonarModel) {
            SonarModel sm = (SonarModel) sonar;
            if (sm.getSonarEventHandler() == null) {
                sm.addSonarEventHandler(new SonarObservableHandlerSimple());
            } else if (!(sm.getSonarEventHandler() instanceof ISonarObservable)) {
                throw new IllegalArgumentException("Sonar (model) has an event handler that is not observable!");
            }
            return (ISonarObservable) sm.getSonarEventHandler();
        }
        else if (sonar instanceof ISonarObservable) {
            return (ISonarObservable) sonar;
        }
        else {
            throw new NotImplementedException("Observable sonar not available for type " + sonar.getClass().getTypeName());
        }
    }

    public static IDistanceObservable makeDistanceObservable(ISonar sonar) {
        if (sonar instanceof SonarModel) {
            SonarModel sm = (SonarModel) sonar;
            if (!(sm.getMutableDistance() instanceof IDistanceObservable)) {
                throw new IllegalArgumentException("Sonar (model) has a distance type that is not observable!");
            }
            return (IDistanceObservable) sm.getMutableDistance();
        }
        // Shot in the dark, to add another avenue of extensibility
        else if (sonar instanceof IDistanceObservable) {
            return (IDistanceObservable) sonar;
        }
        else {
            throw new NotImplementedException("Observable sonar not available for type " + sonar.getClass().getTypeName());
        }
    }
}
