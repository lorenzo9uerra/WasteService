package it.unibo.radarSystem22.domain.mock;

import it.unibo.radarSystem22.domain.models.LedModel;
import it.unibo.radarSystem22.domain.utils.ColorsOut;

public class LedMock extends LedModel {
    @Override
    protected void ledActivate(boolean val) {
        String color = ColorsOut.CYAN;
        if (val)
            color = ColorsOut.RED;
        ColorsOut.outappl("[LedMock] state = " + color + val, ColorsOut.MAGENTA);
    }
}