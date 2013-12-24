package com.altamiracorp.lumify.tools.util;

public class TimePeriod {
    private long timePeriod;

    public TimePeriod(long timePeriod) {
        this.timePeriod = timePeriod;
    }

    @Override
    public String toString() {
        if (timePeriod < 1000) {
            return String.format("%dms", timePeriod);
        }
        if (timePeriod < 1000 * 60) {
            return String.format("%.2fs", (double) timePeriod / 1000.0);
        }
        if (timePeriod < 1000 * 60 * 60) {
            return String.format("%dm %.2fs", timePeriod / (60 * 1000), (double) (timePeriod % (60 * 1000)) / 1000.0);
        }
        if (timePeriod < 1000 * 60 * 60 * 24) {
            return String.format("%dh %dm", timePeriod / (60 * 60 * 1000), (timePeriod / (60 * 1000)) % (60));
        }
        return String.format("%dd %dh", timePeriod / (24 * 60 * 60 * 1000), (timePeriod / (60 * 60 * 1000)) % (24));
    }
}
