package trade.s2;

import t.OptionCalendar;
import t.OptionDate;
import t.TwoWayQuoteMonth;
import trade.Account;
import trade.Contract;
import trade.s1.交易策略1改;
import util.HistoryData;
import util.HistoryDataUtil;
import util.NumberUtil;

import java.util.ArrayList;

/**
 * 1. 以买出跨式期权为主开仓
 * 2. 每天检查期权标的现价是否接近或者跨越一级（0.05），如果有，则调整虚值期权到平值
 * 3. 『接近』先定义成0.01范围，后面再调参。
 * 4. 在调整合约到平值的时候下月同一行权价的合约的权利金是否达到当月合约一倍，如果有，则卖下个月的，否则卖本月的。
 * 5. 每天调现有仓位之后，进行一次加仓检查（只要加仓之后风险不大于90%就进行加仓）
 */
public class 交易策略2 {

    private Account account;

    public 交易策略2(double money) {
        this.account = new Account(money);
    }

    public void 交易(OptionDate today) {

        if (today.toString().equals("2017-05-25")) {
            System.out.println();
        }

        checkForDebug(today);

        this.风险检查(today);

        checkForDebug(today);

        this.行权日处理(today);

        checkForDebug(today);

        this.调仓(today);

        checkForDebug(today);

        this.加仓(today);

        checkForDebug(today);

    }

    private void checkForDebug(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (c1 == null || c2 == null) {
            return;
        }

        if (today.toString().equals("2017-06-27")) {
//            System.out.println();
        }

        if (c1.quote().getExpirationDate().getDate().before(today.getDate())) {
            System.out.println("c1: " + c1.quote() + "\ttoday: " + today);
        }
        if (c2.quote().getExpirationDate().getDate().before(today.getDate())) {
            System.out.println("c2: " + c1.quote() + "\ttoday: " + today);
        }

        OptionDate optionDate = null;
        for (Contract c : this.account.get认购义务合约s()) {
            if (optionDate == null) {
                optionDate = c.quote().getExpirationDate();
            } else if (!optionDate.equals(c.quote().getExpirationDate())) {
                System.out.println("c: " + c);
            }
        }
        optionDate = null;
        for (Contract c : this.account.get认沽义务合约s()) {
            if (optionDate == null) {
                optionDate = c.quote().getExpirationDate();
            } else if (!optionDate.equals(c.quote().getExpirationDate())) {
                System.out.println("c: " + c);
            }
        }
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
            this.收益调仓(today);
        }
    }

    private void 跨级调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        HistoryData data = HistoryDataUtil.上证50历史数据(today.getDate());
        double S = (data.get最高价() + data.get最低价()) / 2; // 当前市场价
        if (S > c1.行权价() + 0.05 - 0.01) {
            重构所有认购义务合约(today);
        }
        if (S < c2.行权价() - 0.05 + 0.01) {
            重构所有认沽义务合约(today);
        }
    }

    private void 重构所有认沽义务合约(OptionDate today) {
        int n = this.account.get认沽义务合约s().size();

        this.平掉所有认沽义务合约(today);

        TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0)); // 当月
        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月

        TwoWayQuoteMonth.R 义务沽 = quote.getP(0, today.getDate());
        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());

        Contract contract;
        if (义务沽1.权力金() >= 义务沽.权力金() * 2) { // 下个月的合约更划算的情况下
            contract = new Contract(quote1, 义务沽1.行权价(), 义务沽1.权力金(), false, false);
        } else {
            contract = new Contract(quote, 义务沽.行权价(), 义务沽.权力金(), false, false);
        }
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

    private void 重构所有认购义务合约(OptionDate today) {
        int n = this.account.get认购义务合约s().size();
        this.平掉所有认购义务合约(today);

        TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0)); // 当月
        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月

        TwoWayQuoteMonth.R 义务购 = quote.getC(0, today.getDate());
        TwoWayQuoteMonth.R 义务购1 = quote1.getC(0, today.getDate());

        Contract contract;
        if (义务购1.权力金() >= 义务购.权力金() * 2) { // 下个月的合约更划算的情况下
            contract = new Contract(quote1, 义务购1.行权价(), 义务购1.权力金(), true, false);
        } else {
            contract = new Contract(quote, 义务购.行权价(), 义务购.权力金(), true, false);
        }
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

    private void 收益调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (c1 == null) {
            return;
        }

        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月

        TwoWayQuoteMonth.R 义务购1 = quote1.getC(0, today.getDate());
        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());

        if (义务购1.权力金() >= c1.权利金() * 2) {
            this.重构所有认购义务合约(today);
        }

        if (义务沽1.权力金() >= c2.权利金() * 2) {
            this.重构所有认沽义务合约(today);
        }
    }

    private void 加仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (c1 == null) {
            if (c2 != null) {
                throw new RuntimeException(); // 此时c2应该也为空
            }
            TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0)); // 当月
            TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月

            TwoWayQuoteMonth.R 义务购 = quote.getC(0, today.getDate());
            TwoWayQuoteMonth.R 义务购1 = quote1.getC(0, today.getDate());
            TwoWayQuoteMonth.R 义务沽 = quote.getP(0, today.getDate());
            TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());

            if (义务沽1.权力金() >= 义务沽.权力金() * 2 || 义务购1.权力金() >= 义务购.权力金() * 2) {
                c1 = new Contract(quote, 义务购1.行权价(), 义务购1.权力金(), true, false);
                c2 = new Contract(quote, 义务沽1.行权价(), 义务沽1.权力金(), false, false);
            } else {
                c1 = new Contract(quote, 义务购.行权价(), 义务购.权力金(), true, false);
                c2 = new Contract(quote, 义务沽.行权价(), 义务沽.权力金(), false, false);
            }
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
        交易策略2 a = new 交易策略2(10);

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
