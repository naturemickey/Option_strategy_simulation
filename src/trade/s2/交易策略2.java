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
 * 1. 以买出跨式期权为主开仓
 * 2. 每天检查期权标的现价是否接近或者跨越一级（0.05），如果有，则调整虚值期权到平值
 * 3. 『接近』先定义成0.01范围，后面再调参。
 * 4. 在调整合约到平值的时候下月同一行权价的合约的权利金是否达到当月合约一倍，如果有，则卖下个月的，否则卖本月的。（『一倍』是通数）
 * 5. 每天调现有仓位之后，进行一次加仓检查（只要加仓之后风险不大于90%就进行加仓）
 * <p>
 * 注：
 * 1. 第3条中的调参，后面发现0.25更合适。
 * 2. 第4条中的参数为1.25
 * 3. 第2条测出来是实值期权调到平值（而不是调虚值）
 */
public class 交易策略2 {

    private Account account;
    private double 平值的scope;
    private double 下月是本月的多少倍; //

    public 交易策略2(double money, double 平值的scope, double 下月是本月的多少倍) {
        this.account = new Account(money);
        this.平值的scope = 平值的scope;
        this.下月是本月的多少倍 = 下月是本月的多少倍;
    }

    public void 交易(OptionDate today) {
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
            // 收益调仓不做更划算
//            this.收益调仓(today);
        }
    }

    private void 跨级调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        HistoryData data = HistoryDataUtil.上证50历史数据(today.getDate());
        double S = (data.get最高价() + data.get最低价()) / 2; // 当前市场价
        // TODO 下面这两个if有点儿门道
        if (S > c1.行权价() + 0.05 - 平值的scope) {
            重构所有认购义务合约(today);
        }
        if (S < c2.行权价() - 0.05 + 平值的scope) {
            重构所有认沽义务合约(today);
        }
//        if (S > c1.行权价() + 0.05 - 平值的scope || S < c2.行权价() - 0.05 + 平值的scope) {
//            重构所有认购义务合约(today);
//            重构所有认沽义务合约(today);
//        }
    }

    private void 重构所有认沽义务合约(OptionDate today) {
        int n = this.account.get认沽义务合约s().size();

        this.平掉所有认沽义务合约(today);

        TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0)); // 当月
        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月

        TwoWayQuoteMonth.R 义务沽 = quote.getP(0, today.getDate());
        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());

        Contract contract;
        if (是否搞下月的更好(义务沽1.权力金(), 义务沽.权力金())) { // 下个月的合约更划算的情况下
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
        if (是否搞下月的更好(义务购1.权力金(), 义务购.权力金())) { // 下个月的合约更划算的情况下
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

//    private void 收益调仓(OptionDate today) {
//        Contract c1 = this.account.getAny认购义务合约();
//        Contract c2 = this.account.getAny认沽义务合约();
//
//        if (c1 == null) {
//            return;
//        }
//
//        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
//
//        TwoWayQuoteMonth.R 义务购1 = quote1.getC(0, today.getDate());
//        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(0, today.getDate());
//
//        if (是否搞下月的更好(义务购1.权力金(), c1.权利金())) {
//            this.重构所有认购义务合约(today);
//        }
//
//        if (是否搞下月的更好(义务沽1.权力金(), c2.权利金())) {
//            this.重构所有认沽义务合约(today);
//        }
//    }

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

            if (是否搞下月的更好(义务沽1.权力金(), 义务沽.权力金()) || 是否搞下月的更好(义务购1.权力金(), 义务购.权力金())) {
                c1 = new Contract(quote1, 义务购1.行权价(), 义务购1.权力金(), true, false);
                c2 = new Contract(quote1, 义务沽1.行权价(), 义务沽1.权力金(), false, false);
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

    private boolean 是否搞下月的更好(double 下月的权利金, double 本月的权利金) {
        return 下月的权利金 >= 本月的权利金 * 下月是本月的多少倍;
    }

    public static void main(String[] args) {
        交易策略2 a = new 交易策略2(5, 0.015, 1.25);

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
