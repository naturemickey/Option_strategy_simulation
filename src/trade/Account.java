package trade;

import t.OptionDate;
import util.NumberUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

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

    public double 总浮盈(Date today) {
        return this.认购权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认购义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum();
    }

    public void 平仓(Contract c, OptionDate today) {
        if (c.cp()) {
            if (c.权利还是义务()) {
                认购权力合约s.remove(c);
            } else {
                认购义务合约s.remove(c);
            }
        } else {
            if (c.权利还是义务()) {
                认沽权力合约s.remove(c);
            } else {
                认沽义务合约s.remove(c);
            }
        }
        this.money += c.权利金剩余价值(today.getDate());

        if (c.权利还是义务()) { // 行权日的权力仓，当剩余价值大于手续费才有平仓的必要，否则等待即可。
            if (today.isExpirationDate()) {
                if (c.权利金剩余价值(today.getDate()) > NumberUtil.手续费) {
                    this.money -= NumberUtil.手续费;
                }
            }
        } else { // 行权日的义务仓，等待即可。
            if (!today.isExpirationDate()) {
                this.money -= NumberUtil.手续费;
            }
        }
    }

    public void 评估风险(OptionDate today) throws RiskException {
        double 总保证金 = this.总保证金(today.getDate());
        double money = this.money + this.总浮盈(today.getDate());
        if (总保证金 / money > 0.9D) { //
            throw new RiskException();
        }
    }

    public boolean 评估风险if加仓(OptionDate today, Contract... contracts) {
        double 总保证金 = this.总保证金(today.getDate());
        double money = this.money + this.总浮盈(today.getDate());

        for (Contract contract : contracts) {
            总保证金 += contract.保证金(today.getDate());
            money += contract.权利金剩余价值(today.getDate());
        }

        if (总保证金 / money > 0.9D) {
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
        return this.money + this.总浮盈(today.getDate());
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

    public static class RiskException extends Exception {
    }
}
