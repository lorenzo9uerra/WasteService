package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.concrete.RadarDisplay;
import it.unibo.radarSystem22.domain.interfaces.ILed;
import it.unibo.radarSystem22.domain.interfaces.IRadarDisplay;
import it.unibo.radarSystem22.domain.interfaces.ISonar;
import it.unibo.radarSystem22.domain.interfaces.ISonarObservable;
import it.unibo.radarSystem22.domain.models.LedModel;
import it.unibo.radarSystem22.domain.models.SonarModel;

public class DeviceFactory {
    public static ILed createLed() {
        return LedModel.create();
    }

    public static ISonar createSonar() {
        return SonarModel.create();
    }
    public static ISonarObservable createSonarObservable() {
        throw new NotImplementedException("Observable domain non implementato");
    }

    public static IRadarDisplay createRadarDisplay() {
        return RadarDisplay.getRadarDisplay();
    }
    public static IRadarDisplay createRadarGui() {
        return createRadarDisplay();
    }
}
