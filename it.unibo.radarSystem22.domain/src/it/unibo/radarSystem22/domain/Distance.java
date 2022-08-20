package it.unibo.radarSystem22.domain;

import it.unibo.radarSystem22.domain.interfaces.IDistance;

public class Distance implements IDistance {
    int val = 0;

    public Distance(int val) {
        this.val = val;
    }

    @Override
    public int getVal() {
        return val;
    }

    @Override
    public String toString() {
        return Integer.toString(val);
    }
}
