package trade.s3;

import t.OptionDate;
import t.TwoWayQuoteMonth;
import trade.Account;
import trade.BaseStrategy;
import trade.Contract;

import java.util.ArrayList;

/**
 * 策略集成自S3
 * 实1改虚1
 */
public class S3_1 extends BaseStrategy {

    private int level;

    public S3_1(double money, int level) {
        super(money);
        this.level = level;
    }


    protected void 行权日处理(OptionDate today) {
       // super.行权日处理(today);
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


    public void 平仓一组期权(OptionDate today) {
        平仓一组跨式期权(today);
    }

    private void 平仓一组跨式期权(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        this.account.平仓(c1, today);
        this.account.平仓(c2, today);
    }

    protected void 调仓(OptionDate today) {
        if (this.account.getAny认购义务合约() != null) {
            this.跨级调仓(today);
        }
    }

    private void 跨级调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (!c1.虚实(today)) {
            重构所有认购义务合约(today);
        }
        if (!c2.虚实(today)) {
            重构所有认沽义务合约(today);
        }
    }

    private void 重构所有认沽义务合约(OptionDate today) {
        int n = this.account.get认沽义务合约s().size();

        this.平掉所有认沽义务合约(today);

        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
        TwoWayQuoteMonth.R 义务沽1 = quote1.getP(this.level, today.getDate());

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

    private void 重构所有认购义务合约(OptionDate today) {
        int n = this.account.get认购义务合约s().size();
        this.平掉所有认购义务合约(today);

        TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
        TwoWayQuoteMonth.R 义务购1 = quote1.getC(this.level, today.getDate());

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

    protected void 加仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        if (c1 == null) {
            if (c2 != null) {
                throw new RuntimeException(); // 此时c2应该也为空
            }
            TwoWayQuoteMonth quote1 = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(1)); // 下月
            TwoWayQuoteMonth.R 义务购1 = quote1.getC(this.level, today.getDate());
            TwoWayQuoteMonth.R 义务沽1 = quote1.getP(this.level, today.getDate());

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
        S3_1 a = new S3_1(5, 1);
        a.runSimulate();
    }
}
