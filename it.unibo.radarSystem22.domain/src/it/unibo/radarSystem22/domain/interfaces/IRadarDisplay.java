package it.unibo.radarSystem22.domain.interfaces;

public interface IRadarDisplay {
    void update(String d, String a);
    IDistance getCurDistance();
}
