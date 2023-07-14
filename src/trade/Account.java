package trade;

import t.OptionDate;
import util.NumberUtil;

import java.util.*;
import java.util.function.Consumer;

import static util.NumberUtil.风险率阈值;

public class Account {
    private double money; // 资金(万)
    private final List<Contract> 认购权力合约s = new ArrayList<>();
    private final List<Contract> 认购义务合约s = new ArrayList<>();
    private final List<Contract> 认沽权力合约s = new ArrayList<>();
    private final List<Contract> 认沽义务合约s = new ArrayList<>();

    public Account(double money) {
        this.money = money;
    }

    public void 加仓(Contract c, OptionDate today) throws RiskException {
        if (c.cp()) {
            if (c.权利还是义务()) {
                认购权力合约s.add(c);
                this.money -= c.权利金();
            } else {
                认购义务合约s.add(c);
                this.money += c.权利金();
            }
        } else {
            if (c.权利还是义务()) {
                认沽权力合约s.add(c);
                this.money -= c.权利金();
            } else {
                认沽义务合约s.add(c);
                this.money += c.权利金();
            }
        }

        this.money -= NumberUtil.手续费;

        评估风险(today);
    }

    public double 总保证金(Date today) {
        double s = 0;
        for (Contract 认购义务合约 : 认购义务合约s) {
            s += 认购义务合约.保证金(today);
        }
        for (Contract 认沽义务合约 : 认沽义务合约s) {
            s += 认沽义务合约.保证金(today);
        }
        return s;
    }

    public double 权利金剩余总价值(Date today) {
        return this.认购权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认购义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum();
    }

    public void 平仓(Contract c, OptionDate today) {
        boolean success;
        if (c.cp()) {
            if (c.权利还是义务()) {
                success = 认购权力合约s.remove(c);
            } else {
                success = 认购义务合约s.remove(c);
            }
        } else {
            if (c.权利还是义务()) {
                success = 认沽权力合约s.remove(c);
            } else {
                success = 认沽义务合约s.remove(c);
            }
        }
        if (!success) {
            throw new RuntimeException(); // 走到这里就是BUG。
        }

        this.money += c.权利金剩余价值(today.getDate());

        if (c.权利还是义务()) { // 行权日的权力仓，当剩余价值大于手续费才有平仓的必要，否则等待即可。
            if (today.isExpirationDate()) {
                if (c.权利金剩余价值(today.getDate()) > NumberUtil.手续费) {
                    this.money -= NumberUtil.手续费;
                }
            }
        } else { // 行权日的义务仓，等待到期就不用手续费。
            if (!today.isExpirationDate() && !c.quote().getExpirationDate().equals(today)) {
                this.money -= NumberUtil.手续费;
            }
        }
    }

    public void 评估风险(OptionDate today) throws RiskException {
        double 总保证金 = this.总保证金(today.getDate());
//        double 总资产 = this.money + this.总浮盈(today.getDate());
        double 总资产 = this.总资产(today);
        if (总保证金 / 总资产 > 风险率阈值) { //
            throw new RiskException();
        }
    }

    public boolean 评估风险if加仓(OptionDate today, Contract... contracts) {
        double 总保证金 = this.总保证金(today.getDate());
//        double money = this.money + this.总浮盈(today.getDate());
        double 总资产 = this.总资产(today);

        for (Contract contract : contracts) {
            总保证金 += contract.保证金(today.getDate());
            总资产 += contract.权利金剩余价值(today.getDate());
        }

        if (总保证金 / 总资产 > 风险率阈值) {
            return false; // 有风险
        }
        return true; // 无风险
    }

    public void forEachContract(Consumer<Contract> contractConsumer) {
        new ArrayList<>(this.认购权力合约s).forEach(contractConsumer);
        new ArrayList<>(this.认购义务合约s).forEach(contractConsumer);
        new ArrayList<>(this.认沽权力合约s).forEach(contractConsumer);
        new ArrayList<>(this.认沽义务合约s).forEach(contractConsumer);
    }

    public double 总资产(OptionDate today) {
        return this.money + this.权利金剩余总价值(today.getDate());
    }

    /**
     * @return 期末余额
     */
    public double getMoney() {
        return money;
    }

    public List<Contract> get认购权力合约s() {
        return Collections.unmodifiableList(认购权力合约s);
    }

    public List<Contract> get认购义务合约s() {
        return Collections.unmodifiableList(认购义务合约s);
    }

    public List<Contract> get认沽权力合约s() {
        return Collections.unmodifiableList(认沽权力合约s);
    }

    public List<Contract> get认沽义务合约s() {
        return Collections.unmodifiableList(认沽义务合约s);
    }

    public Contract getAny认购权力合约() {
        if (this.认购权力合约s.isEmpty())
            return null;
        return this.认购权力合约s.get(this.认购权力合约s.size() - 1);
//        return this.认购权力合约s.get(0);
    }

    public Contract getAny认购义务合约() {
        if (this.认购义务合约s.isEmpty())
            return null;
        return this.认购义务合约s.get(this.认购义务合约s.size() - 1);
//        return this.认购义务合约s.get(0);
    }

    public Contract getAny认沽权力合约() {
        if (this.认沽权力合约s.isEmpty())
            return null;
        return this.认沽权力合约s.get(this.认沽权力合约s.size() - 1);
//        return this.认沽权力合约s.get(0);
    }

    public Contract getAny认沽义务合约() {
        if (this.认沽义务合约s.isEmpty())
            return null;
        return this.认沽义务合约s.get(this.认沽义务合约s.size() - 1);
//        return this.认沽义务合约s.get(0);
    }

    public void 行权日处理(OptionDate today) {
        if (today.isExpirationDate()) {
            this.forEachContract(contract -> {
                if (contract.quote().getExpirationDate().equals(today)) {
                    this.平仓(contract, today);
                }
            });
        }
    }

    public static class RiskException extends Exception {
    }
}
