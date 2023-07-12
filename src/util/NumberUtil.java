package util;

import java.text.NumberFormat;

public class NumberUtil {
    // 获取一个百分比格式化实例
    private static NumberFormat percentFormat = NumberFormat.getPercentInstance();

    static {
        // 设置百分比显示的小数位数（可根据需要修改）
        percentFormat.setMinimumFractionDigits(2);
    }

    public static String 数字转百分比(double d) {
        return percentFormat.format(d);
    }

    public static final double 手续费 = 5D / 10000D; // 正常是0.5元/笔交易
    public static final double 风险率阈值 = 0.9D;
}
