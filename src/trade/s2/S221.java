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
 * 继承自『交易策略2改2』
 */
public class S221 {

    private Account account;
    private double 平值的scope;

    public S221(double money, double 平值的scope) {
        this.account = new Account(money);
        this.平值的scope = 平值的scope;
    }

    public void 交易(OptionDate today) {
        if (today.toString().equals("2022-07-08")) {
            Object o = null;
        }
        this.风险检查(today);
        this.行权日处理(today);
        this.调仓(today);
        this.加仓(today);
    }

    private void 风险检查(OptionDate today) {
        try {
            this.account.评估风险(today);
        } catch (Account.RiskException e) {
            this.平仓一组跨式期权(today);
            this.风险检查(today);
        }
    }

    private void 行权日处理(OptionDate today) {
        if (today.isExpirationDate()) {
            Contract c1 = this.account.getAny认购义务合约();
            Contract c2 = this.account.getAny认沽义务合约();

            if (c1.quote().getExpirationDate().equals(today)) {
                重构所有认购义务合约(today);
            }
            if (c2.quote().getExpirationDate().equals(today)) {
                重构所有认沽义务合约(today);
            }
        }
    }

    private void 平仓一组跨式期权(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        this.account.平仓(c1, today);
        this.account.平仓(c2, today);
    }

    private void 调仓(OptionDate today) {
        if (this.account.getAny认购义务合约() != null) {
            this.跨级调仓(today);
        }
    }

    private void 跨级调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        HistoryData data = HistoryDataUtil.上证50历史数据(today.getDate());
        double S = (data.get最高价() + data.get最低价()) / 2; // 当前市场价
        // TODO 下面这两个if有点儿门道（如果操作反了，就亏钱）
        if (S > c1.行权价() + 0.05 - 平值的scope) { // 认购合约虚出1级
            重构所有认购义务合约(today);

            if (S > c2.行权价() + 0.05*x - 平值的scope) { // 同时认沽合约实出2级
                认沽合约重构为实1级合约(today);
            }
        } else if (S < c2.行权价() - 0.05 + 平值的scope) { // 认沽合约虚出1级
            重构所有认沽义务合约(today);

            if (S < c1.行权价() - 0.05*x + 平值的scope) { // 同时认购合约实出2级
                认购合约重构为实1级合约(today);
            }
        }
    }

    private int x = 5;

    private void 认沽合约重构为实1级合约(OptionDate today) {
        重构所有认沽义务合约(today, 1 - x);
    }

    private void 认购合约重构为实1级合约(OptionDate today) {
        重构所有认购义务合约(today, 1 - x);
    }

    private void 重构所有认沽义务合约(OptionDate today) {
        重构所有认沽义务合约(today, 0);
    }

    private void 重构所有认购义务合约(OptionDate today) {
        重构所有认购义务合约(today, 0);
    }

    private void 重构所有认沽义务合约(OptionDate today, int level) {
        int n = this.account.get认沽义务合约s().size();

        this.平掉所有认沽义务合约(today);

        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(level, today.getDate());

        Contract contract = new Contract(quote1, 义务沽1.行权价(), 义务沽1.权力金(), false, false);

        try {
            for (int i = 0; i < n; i++) {
                this.account.加仓(contract, today);
            }
        } catch (Account.RiskException riskException) {
            for (int i = 0, len = n - this.account.get认沽义务合约s().size(); i < len; i++) {
                Contract c = this.account.getAny认购义务合约();
                this.account.平仓(c, today);
            }
        }
    }

    private void 重构所有认购义务合约(OptionDate today, int level) {
        int n = this.account.get认购义务合约s().size();
        this.平掉所有认购义务合约(today);

        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
        TwoWayQuoteMonth.R 义务购1 = quote1.getC(level, today.getDate());

        Contract contract = new Contract(quote1, 义务购1.行权价(), 义务购1.权力金(), true, false);

        try {
            for (int i = 0; i < n; i++) {
                this.account.加仓(contract, today);
            }
        } catch (Account.RiskException riskException) {
            for (int i = 0, len = n - this.account.get认购义务合约s().size(); i < len; i++) {
                Contract c = this.account.getAny认沽义务合约();
                this.account.平仓(c, today);
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

    private void 加仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (c1 == null) {
            if (c2 != null) {
                throw new RuntimeException(); // 此时c2应该也为空
            }
            TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
            TwoWayQuoteMonth.R 义务购1 = quote1.getC(0, today.getDate());
            TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());

            c1 = new Contract(quote1, 义务购1.行权价(), 义务购1.权力金(), true, false);
            c2 = new Contract(quote1, 义务沽1.行权价(), 义务沽1.权力金(), false, false);
        }

        while (this.account.评估风险if加仓(today, c1, c2)) {
            try {
                this.account.加仓(c1, today);
                this.account.加仓(c2, today);
            } catch (Account.RiskException riskException) {
                // 理论上不可能到这里
                throw new RuntimeException(riskException);
            }
        }
    }

    public static void main(String[] args) {
        S221 a = new S221(5, 0.015);

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
