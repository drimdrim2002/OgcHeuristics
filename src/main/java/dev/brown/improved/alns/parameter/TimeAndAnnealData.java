package dev.brown.improved.alns.parameter;

public class TimeAndAnnealData {

    public final double[][] timeList;
    public final double annealerModifyRatio;
    public final double spareTime;

    public TimeAndAnnealData(double[][] timeList, double annealerModifyRatio, double spareTime) {
        this.timeList = timeList;
        this.annealerModifyRatio = annealerModifyRatio;
        this.spareTime = spareTime;
    }

    public double getAnnealerModifyRatio() {
        return annealerModifyRatio;
    }

    public double getSpareTime() {
        return spareTime;
    }

    public double[][] getTimeList() {
        return timeList;
    }
}