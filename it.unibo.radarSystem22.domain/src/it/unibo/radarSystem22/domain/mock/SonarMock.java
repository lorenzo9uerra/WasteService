package it.unibo.radarSystem22.domain.mock;

import it.unibo.radarSystem22.domain.Distance;
import it.unibo.radarSystem22.domain.models.SonarModel;
import it.unibo.radarSystem22.domain.utils.BasicUtils;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import it.unibo.radarSystem22.domain.utils.DomainSystemConfig;

public class SonarMock extends SonarModel {
    private int distUpdateDelta;

    @Override
    protected void sonarSetUp() {
        curVal = new Distance(DomainSystemConfig.sonarMockStartDist);
        distUpdateDelta = DomainSystemConfig.sonarMockDelta;
        ColorsOut.outappl("[Sonar mock] set up, delta = " + distUpdateDelta, ColorsOut.CYAN);
    }

    @Override
    protected void sonarProduce() {
        if (DomainSystemConfig.testing ) {
            ColorsOut.outappl("[Sonar mock] testing oneshot mode", ColorsOut.MAGENTA);
            updateDistance(DomainSystemConfig.sonarMockStartDist);
            deactivate();
        } else {
            int val = getDistance().getVal() + distUpdateDelta;
            updateDistance(val);
            boolean stop = val <= 0 || val >= DomainSystemConfig.sonarDistanceMax;
            if (stop) {
                deactivate();
                if (DomainSystemConfig.sonarVerbose)
                    ColorsOut.outappl("Stopped", ColorsOut.GREEN);
            }
            BasicUtils.delay(DomainSystemConfig.sonarDelay);
        }
    }
}
