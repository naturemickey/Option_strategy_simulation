package trade.s1;

import t.OptionCalendar;
import t.OptionDate;
import t.TwoWayQuoteMonth;
import trade.Account;
import trade.Contract;
import util.HistoryDataUtil;
import util.NumberUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 本策略是：
 * 1. 每个行权日平仓所有合约；
 * 2. 之后添加新的合约。
 * 3. 整体使用铁蝶策略。
 */
public class 交易策略1改 {

    private Account account;
    private int 义务仓虚几级, 权利仓虚几级;

    public 交易策略1改(double money, int 义务仓虚几级, int 权利仓虚几级) {
        this.account = new Account(money);
        this.义务仓虚几级 = 义务仓虚几级;
        this.权利仓虚几级 = 权利仓虚几级;
    }

    public void 交易(OptionDate today) {
        评估风险(today);
        if (today.isExpirationDate()) {
            行权日平仓(today);
        }
        if (this.account.get认购权力合约s().isEmpty()) { // 空仓的情况下加仓
            加仓(today);
        }
    }

    private void 行权日平仓(OptionDate today) {
        this.account.forEachContract(contract -> {
            this.account.平仓(contract, today);
        });
    }

    private void 清理一组合约(OptionDate today) {
        int idx = this.account.get认购权力合约s().size() - 1;
        final Contract c1 = this.account.get认购权力合约s().get(0);
        final Contract c2 = this.account.get认购义务合约s().get(0);
        final Contract c3 = this.account.get认沽权力合约s().get(0);
        final Contract c4 = this.account.get认沽义务合约s().get(0);

        this.account.平仓(c1, today);
        this.account.平仓(c2, today);
        this.account.平仓(c3, today);
        this.account.平仓(c4, today);
    }

    private void 加仓(OptionDate today) {
        TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0));

        TwoWayQuoteMonth.R 义务购 = quote.getC(this.义务仓虚几级, today.getDate());
        TwoWayQuoteMonth.R 权利购 = quote.getC(this.权利仓虚几级, today.getDate());
        TwoWayQuoteMonth.R 义务沽 = quote.getP(this.义务仓虚几级, today.getDate());
        TwoWayQuoteMonth.R 权利沽 = quote.getP(this.权利仓虚几级, today.getDate());

        Contract 义务购合约 = new Contract(quote, 义务购.行权价(), 义务购.权力金(), true, false);
        Contract 权利购合约 = new Contract(quote, 权利购.行权价(), 权利购.权力金(), true, true);
        Contract 义务沽合约 = new Contract(quote, 义务沽.行权价(), 义务沽.权力金(), false, false);
        Contract 权利沽合约 = new Contract(quote, 权利沽.行权价(), 权利沽.权力金(), false, true);

        // 以上4笔（1铁蝶个组合）交易的总金额
        double 一组交易的保证金 = 义务购合约.保证金(today.getDate()) + 义务沽合约.保证金(today.getDate());
        double 一组交易的权利金 = 义务购合约.权利金() + 义务沽合约.权利金() - 权利购合约.权利金() - 权利沽合约.权利金();

        // 组数是对x的求解：(一组交易的保证金 * x) / (money - 手续费 * 4 * x) < 0.9
        int 组数 = (int) (9 * this.account.getMoney() / (10 * 一组交易的保证金 + 9 * NumberUtil.手续费 * 4));
        for (int i = 0; i < 组数; i++) {
            try {
                this.account.加仓(权利购合约, today);
                this.account.加仓(义务购合约, today);
                this.account.加仓(权利沽合约, today);
                this.account.加仓(义务沽合约, today);
            } catch (Account.RiskException e) {
                // 理论上来说，上面已经计算了『组数』，那么应该是走不到这里的。
                throw new RuntimeException(e);
            }
        }
    }

    private void 评估风险(OptionDate today) {
        try {
            this.account.评估风险(today);
        } catch (Account.RiskException e) {
            this.清理一组合约(today);
            评估风险(today);
        }
    }

    public double 总资产(OptionDate today) {
        return this.account.总资产(today);
    }

    public static void main(String[] args) {
        交易策略1改 a = new 交易策略1改(10, 0, 1);

        double d = -1;
        for (OptionDate optionDate : OptionCalendar.getInstance().getDates()) {
            if (HistoryDataUtil.上证50历史数据(optionDate.getDate()) != null
                    && TwoWayQuoteMonth.getQuote(optionDate.nextExpirationDate(0)) != null) {
                a.交易(optionDate);

                if (optionDate.isExpirationDate()) {
                    double 总资产 = a.总资产(optionDate);

                    // 将 double 值格式化为百分比字符串
                    String percentage = NumberUtil.数字转百分比(d > 0 ? (总资产 - d) / d : 0);

                    System.out.println(optionDate + "\t" + a.总资产(optionDate) + "\t" + percentage);

                    d = 总资产;
                }
            }
        }
    }

}
