package trade;

import t.OptionCalendar;
import t.OptionDate;
import t.TwoWayQuoteMonth;
import util.HistoryDataUtil;
import util.NumberUtil;

public abstract class BaseStrategy {
    protected Account account;

    protected BaseStrategy(double money) {
        this.account = new Account(money);
    }

    public void 交易(OptionDate today) {
        if (today.toString().equals("2017-01-09")) {
            System.out.print("");
        }
        this.风险检查(today);
        this.行权日处理(today);
        this.调仓(today);
        this.加仓(today);
    }

    public void 风险检查(OptionDate today) {
        try {
            this.account.评估风险(today);
        } catch (Account.RiskException e) {
            this.平仓一组期权(today);
            this.风险检查(today);
        }
    }

    protected abstract void 平仓一组期权(OptionDate today);

    protected abstract void 加仓(OptionDate today);

    protected void 行权日处理(OptionDate today) {
        this.account.行权日处理(today);
    }

    protected abstract void 调仓(OptionDate today);

    public void runSimulate() {
        double d = -1;
        for (OptionDate optionDate : OptionCalendar.getInstance().getDates()) {
            if (HistoryDataUtil.上证50历史数据(optionDate.getDate()) != null
                    && TwoWayQuoteMonth.getQuote(optionDate.nextExpirationDate(0)) != null
                    && TwoWayQuoteMonth.getQuote(optionDate.nextExpirationDate(1)) != null) {
                this.交易(optionDate);

                if (optionDate.isExpirationDate()) {
                    double 总资产 = this.account.总资产(optionDate);

                    // 将 double 值格式化为百分比字符串
                    String percentage = NumberUtil.数字转百分比(d > 0 ? (总资产 - d) / d : 0);

                    System.out.println(optionDate + "\t" + this.account.总资产(optionDate) + "\t" + percentage);

                    d = 总资产;
                }
            }
        }
    }
}
