package trade.s2;

import t.OptionDate;
import trade.Account;

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
        this.调仓(today);
        this.加仓(today);
    }

    private void 风险检查(OptionDate today) {
    }

    private void 平仓一组跨式期权(OptionDate today) {
    }

    private void 调仓(OptionDate today) {
    }

    private void 加仓(OptionDate today) {
    }
}
