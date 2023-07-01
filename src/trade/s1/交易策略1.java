package trade.s1;

import t.OptionCalendar;
import t.OptionDate;
import t.TwoWayQuoteMonth;
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
public class 交易策略1 {
    private double money; // 资金(万)
    private int 义务仓虚几级, 权利仓虚几级;
    private List<Contract> 认购权力合约s = new ArrayList<>();
    private List<Contract> 认购义务合约s = new ArrayList<>();
    private List<Contract> 认沽权力合约s = new ArrayList<>();
    private List<Contract> 认沽义务合约s = new ArrayList<>();

    public 交易策略1(double money, int 义务仓虚几级, int 权利仓虚几级) {
        this.money = money;
        this.义务仓虚几级 = 义务仓虚几级;
        this.权利仓虚几级 = 权利仓虚几级;
    }

    public void 交易(OptionDate today) {
        评估风险(today);
        if (today.isExpirationDate()) {
            行权日平仓(today);
        }
        if (认购权力合约s.isEmpty()) { // 空仓的情况下加仓
            加仓(today);
        }
    }

    private void 行权日平仓(OptionDate today) {
        for (Contract contract : this.认购权力合约s) {
            if (contract.权利金剩余价值(today.getDate()) > NumberUtil.手续费) { // 还有剩余价值才值得平仓
                this.money += contract.权利金剩余价值(today.getDate()) - NumberUtil.手续费;
            }
        }
        for (Contract contract : this.认购义务合约s) {
            // 义务仓在行权日不用平仓，直接等待当时结束即可。
            this.money += contract.权利金剩余价值(today.getDate());
        }
        for (Contract contract : this.认沽权力合约s) {
            if (contract.权利金剩余价值(today.getDate()) > NumberUtil.手续费) { // 还有剩余价值才值得平仓
                this.money += contract.权利金剩余价值(today.getDate()) - NumberUtil.手续费;
            }
        }
        for (Contract contract : this.认沽义务合约s) {
            this.money += contract.权利金剩余价值(today.getDate());
        }
        this.认购权力合约s.clear();
        this.认购义务合约s.clear();
        this.认沽权力合约s.clear();
        this.认沽义务合约s.clear();
    }

    private void 清理一组合约(Date today) {
        int idx = this.认购权力合约s.size() - 1;
        final Contract c1 = this.认购权力合约s.remove(idx);
        final Contract c2 = this.认购义务合约s.remove(idx);
        final Contract c3 = this.认沽权力合约s.remove(idx);
        final Contract c4 = this.认沽义务合约s.remove(idx);
        this.money += c1.权利金剩余价值(today) + c2.权利金剩余价值(today) + c3.权利金剩余价值(today) + c4.权利金剩余价值(today)
                - NumberUtil.手续费 * 4;
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

        // 组数是对x的求解：(一组交易的保证金 * x) / (money + 一组交易的权利金 * x) < 0.9
        int 组数 = (int) (9 * money / (10 * 一组交易的保证金 + 9 * NumberUtil.手续费 * 4));
        for (int i = 0; i < 组数; i++) {
            this.认购权力合约s.add(权利购合约);
            this.认购义务合约s.add(义务购合约);
            this.认沽权力合约s.add(权利沽合约);
            this.认沽义务合约s.add(义务沽合约);

            this.money += 一组交易的权利金 - NumberUtil.手续费 * 4;
        }
    }

    private void 评估风险(OptionDate today) {
        double 总保证金 = this.总保证金(today.getDate());
        double money = this.money + this.总浮盈(today.getDate());
        if (总保证金 / money > 0.9D) { //
            this.清理一组合约(today.getDate());
            评估风险(today);
        }
    }

    private double 总浮盈(Date today) {
        return this.认购权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认购义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽权力合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum()
                + this.认沽义务合约s.stream().mapToDouble(contract -> contract.权利金剩余价值(today)).sum();
    }

    public double 总资产(OptionDate today) {
        return this.money + this.总浮盈(today.getDate());
    }

    private double 总保证金(Date today) {
        double s = 0;
        for (Contract 认购义务合约 : 认购义务合约s) {
            s += 认购义务合约.保证金(today);
        }
        for (Contract 认沽义务合约 : 认沽义务合约s) {
            s += 认沽义务合约.保证金(today);
        }
        return s;
    }

    public static void main(String[] args) {
        交易策略1 a = new 交易策略1(10, 0, 1);

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
