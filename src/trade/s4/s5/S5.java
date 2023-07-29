package trade.s4.s5;

import t.OptionDate;
import trade.Account;
import trade.BaseStrategy;
import trade.Contract;

public class S5 extends BaseStrategy {

    private double money;

    protected S5(double money) {
        super(money / 2);
        this.money = money / 2;
    }

    @Override
    public double 总资产(OptionDate today) {
        return this.account.总资产(today) + this.money;
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
        } else if (delta > 0) {
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
        } else {
            // **这个分支是delta恰好为0，理论上存在，实际上不太可能达到**
            if (this.account.get认购义务合约s().size() > 0) {
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
            } else if (this.account.get认沽义务合约s().size() > 0) {
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
        }
        this.account.平仓(c, today);
    }

    @Override
    protected void 加仓(OptionDate today) {
        while (true) {
            double delta = this.account.delta(today.getDate());
            try {
                if (delta > 0) {
                    this.account.加仓_认购义务(today, 0, 0);
                } else {
                    this.account.加仓_认沽义务(today, 0, 0);
                }
            } catch (Account.RiskException e) {
                return;
            }
        }
    }

    @Override
    protected void 调仓(OptionDate today) {
        调现金保留仓位(today);

        加仓(today);
        调仓_internal(today);
    }

    private void 调现金保留仓位(OptionDate today) {
        // 50% 仓位，没用
//        double 总资产 = this.总资产(today);
//        double money2add = this.money - 总资产 / 2;
//        this.money -= money2add;
//        super.addMoney(today, money2add);

        // 10万固定仓位，没用
//        double 期权总资产 = this.account.总资产(today);
//        double money2add = 10 - 期权总资产;
//        this.money -= money2add;
//        super.addMoney(today, money2add);

        // 50% 上浮到个位， 没用
//        double 总资产 = this.总资产(today);
//        double 期权应该的仓位 = Math.ceil(总资产 * 0.5);
//        double money2add = this.money - (总资产 - 期权应该的仓位);
//        this.money -= money2add;
//        super.addMoney(today, money2add);

        // 行权日
        if (today.isExpirationDate()) {
            // 50% 仓位
//            double 总资产 = this.总资产(today);
//            double money2add = this.money - 总资产 / 2;
//            this.money -= money2add;
//            super.addMoney(today, money2add);

//             10万固定仓位，没用
//            double 期权总资产 = this.account.总资产(today);
//            double money2add = 10 - 期权总资产;
//            this.money -= money2add;
//            super.addMoney(today, money2add);

            // 50% 上浮到个位， 没用
        double 总资产 = this.总资产(today);
        double 期权应该的仓位 = Math.ceil(总资产 * 0.5);
        double money2add = this.money - (总资产 - 期权应该的仓位);
        this.money -= money2add;
        super.addMoney(today, money2add);

            // 50% 上浮到个位，记录最高，后续取最高
//            double 总资产 = this.总资产(today);
//            double 期权应该的仓位 = Math.ceil(总资产 * 0.5);
//            if (期权应该的仓位 > 期权仓位) {
//                期权仓位 = 期权应该的仓位;
//            }
//            double money2add = this.money - (总资产 - 期权仓位);
//            this.money -= money2add;
//            super.addMoney(today, money2add);
        }
    }

    private double 期权仓位 = -1;

    private void 调仓_internal(OptionDate today) {
        平仓一组期权(today);

        double delta = this.account.delta(today.getDate());

        if (delta < -1 || delta > 1) {
            调仓_internal(today);
        }
    }

    public static void main(String[] args) {
        var s4 = new S5(20);
        s4.runSimulate();
    }
}
