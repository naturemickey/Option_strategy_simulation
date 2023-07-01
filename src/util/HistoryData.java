package util;

public class HistoryData {
    private double 收盘价;
    private double 开盘价;
    private double 最高价;
    private double 最低价;
    private double 涨跌幅;
    private double 涨跌额;
    private double 成交量;
    private double 成交额;
    private double 振幅;
    private double 换手率;

    public HistoryData(double 收盘价, double 开盘价, double 最高价, double 最低价, double 涨跌幅, double 涨跌额, double 成交量, double 成交额, double 振幅, double 换手率) {
        final int D = 1000;
        this.收盘价 = 收盘价 / D;
        this.开盘价 = 开盘价 / D;
        this.最高价 = 最高价 / D;
        this.最低价 = 最低价 / D;
        this.涨跌幅 = 涨跌幅;
        this.涨跌额 = 涨跌额;
        this.成交量 = 成交量;
        this.成交额 = 成交额;
        this.振幅 = 振幅 / 100;
        this.换手率 = 换手率;
    }

    public double get收盘价() {
        return 收盘价;
    }

    public double get开盘价() {
        return 开盘价;
    }

    public double get最高价() {
        return 最高价;
    }

    public double get最低价() {
        return 最低价;
    }

    public double get涨跌幅() {
        return 涨跌幅;
    }

    public double get涨跌额() {
        return 涨跌额;
    }

    public double get成交量() {
        return 成交量;
    }

    public double get成交额() {
        return 成交额;
    }

    public double get振幅() {
        return 振幅;
    }

    public double get换手率() {
        return 换手率;
    }
}
