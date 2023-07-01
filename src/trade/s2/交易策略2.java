package trade.s2;

import t.OptionDate;
import trade.Account;

/**
 * 1. 以买出跨式期权为主开仓
 * 2. 每天检查期权标的现价是否接近或者跨越一级（0.05），如果有，则调整虚值期权到平值
 * 3. 『接近』先定义成0.01范围，后面再调参。
 * 4. 在调整合约到平值的时候下月同一行权价的合约的权利金是否达到当月合约一倍，如果有，则卖下个月的，否则卖本月的。
 */
public class 交易策略2 {

    private Account account;

    public 交易策略2(Account account) {
        this.account = account;
    }

    public void 交易(OptionDate today) {
        
    }
}
