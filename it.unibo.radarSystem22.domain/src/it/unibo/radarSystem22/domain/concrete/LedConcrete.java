package it.unibo.radarSystem22.domain.concrete;

import it.unibo.radarSystem22.domain.DeviceFactory;
import it.unibo.radarSystem22.domain.models.LedModel;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

import java.io.IOException;

public class LedConcrete extends LedModel {
    private static final String ON_SCRIPT_PATH  = "bash/led25GpioTurnOn.sh";
    private static final String OFF_SCRIPT_PATH = "bash/led25GpioTurnOff.sh";

    @Override
    protected void ledActivate(boolean val) {
        try {
            if (val)
                BasicUtils.runExecutable(DomainSystemConfig.deviceScriptFolder + ON_SCRIPT_PATH, DomainSystemConfig.sudoRequired);
            else
                BasicUtils.runExecutable(DomainSystemConfig.deviceScriptFolder + OFF_SCRIPT_PATH, DomainSystemConfig.sudoRequired);
        } catch (IOException e) {
            e.printStackTrace();
            ColorsOut.outerr("Could not set led state!");
        }
    }
}
