package trade.s2;

import t.OptionCalendar;
import t.OptionDate;
import t.TwoWayQuoteMonth;
import trade.Account;
import trade.Contract;
import util.HistoryData;
import util.HistoryDataUtil;
import util.NumberUtil;

import java.util.ArrayList;

/**
 * 此策略从『交易策略2』继承过来。
 * 修改的部分是：
 * 1. 到了行权日再买平所有期权，并卖出当月期权
 */
public class 交易策略2改 {

    private Account account;
    private int next; // 0为本月，1为下月

    public 交易策略2改(double money, int next) {
        this.account = new Account(money);
        this.next = next;
    }

    public void 交易(OptionDate today) {
        this.风险检查(today);
        this.行权日处理(today);
    }

    private void 风险检查(OptionDate today) {
        try {
            this.account.评估风险(today);
        } catch (Account.RiskException e) {
            this.平仓一组跨式期权(today);
            this.风险检查(today);
        }
    }

    private void 平仓一组跨式期权(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        this.account.平仓(c1, today);
        this.account.平仓(c2, today);
    }

    private void 行权日处理(OptionDate today) {
        if (today.isExpirationDate() || this.account.getAny认购义务合约() == null) {
            this.平掉所有认沽义务合约(today);
            this.平掉所有认购义务合约(today);


            TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(this.next));
            TwoWayQuoteMonth.R 义务沽 = quote.getP(0, today.getDate());
            TwoWayQuoteMonth.R 义务购 = quote.getC(0, today.getDate());
            Contract contract义务沽 = new Contract(quote, 义务沽.行权价(), 义务沽.权力金(), false, false);
            Contract contract义务购 = new Contract(quote, 义务购.行权价(), 义务购.权力金(), true, false);


            while (this.account.评估风险if加仓(today, contract义务沽, contract义务购)) {
                try {
                    this.account.加仓(contract义务沽, today);
                    this.account.加仓(contract义务购, today);
                } catch (Account.RiskException riskException) {
                    // 理论上不可能到这里
                    throw new RuntimeException(riskException);
                }
            }
        }
    }

    private void 平掉所有认购义务合约(OptionDate today) {
        new ArrayList<>(this.account.get认购义务合约s()).forEach(contract -> {
            this.account.平仓(contract, today);
        });
    }

    private void 平掉所有认沽义务合约(OptionDate today) {
        new ArrayList<>(this.account.get认沽义务合约s()).forEach(contract -> {
            this.account.平仓(contract, today);
        });
    }

    public static void main(String[] args) {
        交易策略2改 a = new 交易策略2改(10, 1);

        double d = -1;
        for (OptionDate optionDate : OptionCalendar.getInstance().getDates()) {
            if (HistoryDataUtil.上证50历史数据(optionDate.getDate()) != null
                    && TwoWayQuoteMonth.getQuote(optionDate.nextExpirationDate(0)) != null
                    && TwoWayQuoteMonth.getQuote(optionDate.nextExpirationDate(1)) != null) {
                a.交易(optionDate);

                if (optionDate.isExpirationDate()) {
                    double 总资产 = a.account.总资产(optionDate);

                    // 将 double 值格式化为百分比字符串
                    String percentage = NumberUtil.数字转百分比(d > 0 ? (总资产 - d) / d : 0);

                    System.out.println(optionDate + "\t" + a.account.总资产(optionDate) + "\t" + percentage);

                    d = 总资产;
                }
            }
        }
    }
}
