package t;

import util.CalculateUtil;
import util.DateUtil;
import util.HistoryData;
import util.HistoryDataUtil;

import java.util.*;

import static util.CalculateUtil.calculateσ;

public class TwoWayQuoteMonth {

    private OptionDate expirationDate;
    private static Map<Date, TwoWayQuoteMonth> quotes = new HashMap<>();

    static {
        for (OptionDate optionDate : OptionCalendar.getInstance().getDates()) {
            if (optionDate.isExpirationDate()) {
                quotes.put(optionDate.getDate(), new TwoWayQuoteMonth(optionDate));
            }
        }
    }

    public static TwoWayQuoteMonth getQuote(Date d) {
        return quotes.get(d);
    }

    public static TwoWayQuoteMonth getQuote(OptionDate optionDate) {
        return quotes.get(optionDate.getDate());
    }

    public OptionDate getExpirationDate() {
        return expirationDate;
    }

    private TwoWayQuoteMonth(OptionDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public double getC(double 行权价, Date today) {
        Result result = getResult(行权价, today);

        return CalculateUtil.calculateC(result.X(), result.S(), result.t(), result.r(), result.σ());
    }

    public double getP(double 行权价, Date today) {
        Result result = getResult(行权价, today);

        return CalculateUtil.calculateP(result.X(), result.S(), result.t(), result.r(), result.σ());
    }

    /**
     * @param level 1为虚1级，-1为实1级
     * @param today
     * @return 权力金
     */
    public R getC(int level, Date today) {
        return get(level, today, true);
    }

    public R getP(int level, Date today) {
        return get(level, today, false);
    }

    private R get(int level, Date today, boolean cp) {
        HistoryData historyData = HistoryDataUtil.上证50历史数据(today);
        if (historyData == null) {
            throw new RuntimeException();
        }
        // S 为市场现价
        double S = (historyData.get最高价() + historyData.get最低价()) / 2; // 假设在中间出手

        int g = ((int) (S * 100)) % 10; // 小数点后第2位。

        S = (((int) (S * 10)) * 10) / 100D; // 去掉S的小数点后第2位及以后的所有位。

        double X; // 行权价
        if (g <= 2) {
            X = S;
        } else if (g <= 7) {
            X = S + 0.05D;
        } else {
            X = S + 0.1D;
        }

        if (cp) {
            X += level * 0.05D;
            return new R(X, getC(X, today));
        } else {
            X -= level * 0.05D;
            return new R(X, getP(X, today));
        }
    }

    private Result getResult(double 行权价, Date today) {
        if (today.after(this.expirationDate.getDate())) {
            throw new RuntimeException();
        }

//        if ((行权价 - ((int) (行权价 * 100)) / 100D) > 0.000001D) { // 小数点第2位之后不能有数字
//            throw new RuntimeException();
//        }

//        int mod = ((int) (行权价 * 100)) % 10; // TODO
        int mod = ((int) Math.round(行权价 * 100)) % 10;
        if (mod != 0 && mod != 5) { // 小数点后二位只能是0或5（50ETF期权的行权价通常是0.5间隔的）
            throw new RuntimeException();
        }

        HistoryData historyData = HistoryDataUtil.上证50历史数据(today);
        double S = (historyData.get最高价() + historyData.get最低价()) / 2; // 假设在中间出手
        double X = 行权价;
        long t = DateUtil.distance(today, expirationDate.getDate());
        double r = HistoryDataUtil._10年期国债收益率(today);
        double σ = calculateσ(historyData.get振幅(), S, t);

        return new Result(S, X, t, r, σ);
    }

    @Override
    public String toString() {
        return "TwoWayQuoteMonth{" +
                "expirationDate=" + expirationDate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TwoWayQuoteMonth that = (TwoWayQuoteMonth) o;
        return Objects.equals(expirationDate, that.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expirationDate);
    }

    private record Result(double S, double X, long t, double r, double σ) {
    }

    public static record R(double 行权价, double 权力金) {
    }
}
