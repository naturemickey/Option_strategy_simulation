package trade;

import t.OptionDate;
import t.TwoWayQuoteMonth;
import util.CalculateUtil;
import util.DateUtil;
import util.HistoryData;
import util.HistoryDataUtil;

import java.util.Date;
import java.util.Objects;

public record Contract(
        TwoWayQuoteMonth quote,
        double 行权价, double 权利金,
        boolean cp,// true 认购；false 认沽
        boolean 权利还是义务 // true 权力；false 义务
) {
    /**
     * @param today
     * @return 0为本月，1为下月
     */
    public int 是哪个月的仓(OptionDate today) {
        Date 当月行权日 = DateUtil.get当月行权日(today.getDate());
        if (quote.getExpirationDate().getDate().equals(当月行权日)) {
            return 0;
        }
        return 1;
    }

    private double 当前权利金(Date today) {
        if (this.cp) {
            return this.quote.getC(this.行权价, today);
        }
        return this.quote.getP(this.行权价, today);
    }

    private double 当前市场价(Date today) {
        HistoryData historyData = HistoryDataUtil.上证50历史数据(today);
        return (historyData.get最高价() + historyData.get最低价()) / 2;
    }

    public double 保证金(Date today) {
        if (this.权利还是义务) {
            return 0;
        } else {
            double 合约最新成交价 = this.当前权利金(today);
            double 合约标的最新价 = this.当前市场价(today);
            double 行权价 = this.行权价;
            if (this.cp) {
                return CalculateUtil.认购期权保证金(合约最新成交价, 合约标的最新价, 行权价, 1);
            } else {
                return CalculateUtil.认沽期权保证金(合约最新成交价, 合约标的最新价, 行权价, 1);
            }
        }
    }

    public double 权利金剩余价值(Date today) {
        double 当前权利金 = this.当前权利金(today);
        if (this.权利还是义务) {
            // 权力仓当前的权利金，即：如果当下平仓能拿回来的钱。
            return 当前权利金;
        } else {
            // 义务他当前的权利金，即：如果当下平仓要付出的钱。
            return -当前权利金;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return Double.compare(contract.行权价, 行权价) == 0 && Double.compare(contract.权利金, 权利金) == 0 && cp == contract.cp && 权利还是义务 == contract.权利还是义务 && Objects.equals(quote, contract.quote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quote, 行权价, 权利金, cp, 权利还是义务);
    }
}
