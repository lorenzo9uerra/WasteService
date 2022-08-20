package it.unibo.radarSystem22.domain.models;

import it.unibo.radarSystem22.domain.NotImplementedException;
import it.unibo.radarSystem22.domain.interfaces.ILed;
import it.unibo.radarSystem22.domain.mock.LedMock;
import it.unibo.radarSystem22.domain.concrete.LedConcrete;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

public abstract class LedModel implements ILed {
    private boolean state = false;

    public static ILed create() {
        ILed led;
        if (DomainSystemConfig.simulation || DomainSystemConfig.simulateLed) {
            led = createLedMock();
        } else {
            led = createLedConcrete();
        }
        return led;
    }
    public static ILed createLedMock() {
        return new LedMock();
    }
    public static ILed createLedConcrete() {
        return new LedConcrete();
    }

    protected abstract void ledActivate( boolean val );

    protected void setState(boolean val) {
        state = val;
        ledActivate(state);
    }
    @Override
    public void turnOn(){ setState(true); }
    @Override
    public void turnOff(){ setState(false); }
    @Override
    public boolean getState(){ return state; }
}
