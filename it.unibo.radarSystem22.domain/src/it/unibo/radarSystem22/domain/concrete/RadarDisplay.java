package it.unibo.radarSystem22.domain.concrete;

import it.unibo.radarSystem22.domain.Distance;
import it.unibo.radarSystem22.domain.interfaces.IDistance;
import it.unibo.radarSystem22.domain.interfaces.IRadarDisplay;
import it.unibo.radarSystem22.domain.utils.ColorsOut;
import radarPojo.radarSupport;

public class RadarDisplay implements IRadarDisplay {
    private String curDistance = "0";
    // Singleton
    private static RadarDisplay display = null;

    public static RadarDisplay getRadarDisplay(){
        if( display == null ) {
            display = new RadarDisplay();
        }
        return display;
    }

    protected RadarDisplay() {
        radarSupport.setUpRadarGui();
    }

    @Override
    public void update(String distance, String angle) {
        //Colors.out("RadarDisplay | update distance="+distance);
        curDistance =  distance;
        radarSupport.update(distance,angle);
    }

    @Override
    public IDistance getCurDistance() {
        ColorsOut.out("RadarDisplay | getCurDistance="+curDistance);
        return new Distance(Integer.parseInt(curDistance));
    }
}
