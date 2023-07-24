package trade.s4;

import t.OptionDate;
import trade.Account;
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
        while (true) {
            double delta = this.account.delta(today.getDate());
            try {
                if (delta < 0) {
                    this.account.加仓_认购义务(today, 1, 0);
                } else {
                    this.account.加仓_认沽义务(today, 1, 0);
                }
            } catch (Account.RiskException e) {
                return;
            }
        }
    }

    @Override
    protected void 调仓(OptionDate today) {
        double delta = this.account.delta(today.getDate());
        double 总资产 = this.account.总资产(today);

        if (Math.abs(delta / 总资产) > 0.01) {
            加仓(today);
            调仓_internal(today, delta < 0);
        }
    }

    private void 调仓_internal(OptionDate today, boolean gl) {
        平仓一组期权(today);

        double delta = this.account.delta(today.getDate());
        if ((delta < 0) == gl) {
            调仓_internal(today, gl);
        }
    }

    public static void main(String[] args) {
        var s4 = new S4(10);
        s4.runSimulate();
    }
}
