package trade.s2;

import t.OptionDate;
import t.TwoWayQuoteMonth;
import trade.Account;
import trade.Contract;
import util.HistoryData;
import util.HistoryDataUtil;

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

    public 交易策略2(Account account) {
        this.account = account;
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
                this.平掉所有认购义务合约(today);
            }
            if (c2.quote().getExpirationDate().equals(today)) {
                this.平掉所有认沽义务合约(today);
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
        this.跨级调仓(today);
        this.收益调仓(today);
    }

    private void 跨级调仓(OptionDate today) {
        Contract c1 = this.account.getAny认购义务合约();
        Contract c2 = this.account.getAny认沽义务合约();

        int n = this.account.get认购义务合约s().size(); // 合约个数

        HistoryData data = HistoryDataUtil.上证50历史数据(today.getDate());
        double S = (data.get最高价() + data.get最低价()) / 2; // 当前市场价
        if (S > c1.行权价() + 0.05 - 0.01) {
            TwoWayQuoteMonth quote = TwoWayQuoteMonth.getQuote(today.nextExpirationDate(0));
            this.平掉所有认购义务合约(today);
            TwoWayQuoteMonth.R 义务购 = quote.getC(0, today.getDate());
            Contract 义务购合约 = new Contract(quote, 义务购.行权价(), 义务购.权力金(), true, false);
            
        }
        if (S < c2.行权价() - 0.05 + 0.01) {
            this.平掉所有认沽义务合约(today);
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
    }

    private void 加仓(OptionDate today) {
    }
}
