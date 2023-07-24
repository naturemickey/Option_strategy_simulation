package trade.s4;

import t.OptionDate;
import trade.BaseStrategy;
import trade.Contract;

public class S4 extends BaseStrategy {
    protected S4(double money) {
        super(money);
    }

    @Override
    protected void 平仓一组期权(OptionDate today) {
        double delta = this.account.delta(today.getDate());
        Contract c = null;
        double minm = Double.MAX_VALUE;
        if (delta < 0) {
            // 平认购
            for (Contract 认购义务合约 : this.account.get认购义务合约s()) {
                if (c == null) {
                    c = 认购义务合约;
                    continue;
                }
                double m = Math.abs(认购义务合约.delta(today.getDate()) - delta);
                if (minm > m) {
                    minm = m;
                    c = 认购义务合约;
                }
            }
        } else {
            // 平认沽
            for (Contract 认沽义务合约 : this.account.get认沽义务合约s()) {
                if (c == null) {
                    c = 认沽义务合约;
                    continue;
                }
                double m = Math.abs(认沽义务合约.delta(today.getDate()) - delta);
                if (minm > m) {
                    minm = m;
                    c = 认沽义务合约;
                }
            }
        }
        this.account.平仓(c, today);
    }

    @Override
    protected void 加仓(OptionDate today) {

    }

    @Override
    protected void 调仓(OptionDate today) {
        // todo
    }

    public static void main(String[] args) {
        var s4 = new S4(10);
        s4.runSimulate();
    }
}
