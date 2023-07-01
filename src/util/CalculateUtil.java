package util;

public class CalculateUtil {
    /**
     * @param X 执行价
     * @param S 标的现价
     * @param t 期权剩余天数(天)
     * @param r 无风险利率(10年国债利率)
     * @param σ 波动率
     * @return 权力金
     */
    public static double calculateC(double X, double S, long t, double r, double σ) {
        double T = t / 365D;
        // d1=[ln(S/X)+(r+σ^2/2)T]/(σ√T)
        double d1 = (Math.log(S / X) + (r + (σ * σ) / 2) * T) / (σ * Math.pow(T, 0.5));
        // d2=d1-σ√T
        double d2 = d1 - σ * Math.pow(T, 0.5);
        // C=S*N(d1)-X*exp(-r*T)*N(d2)
        return S * NormSDist(d1) - X * Math.exp(-r * T) * NormSDist(d2);
    }

    public static double calculateP(double X, double S, long t, double r, double σ) {
        double T = t / 365D;
        double C = calculateC(X, S, t, r, σ);
        // P=C+X*exp(-r*T)-S
        return C + X * Math.exp(-r * T) - S;
    }

    private static double NormSDist(double z) {
        // this guards against overflow
        if (z > 6)
            return 1;
        if (z < -6)
            return 0;

        double gamma = 0.231641900,
                a1 = 0.319381530,
                a2 = -0.356563782,
                a3 = 1.781477973,
                a4 = -1.821255978,
                a5 = 1.330274429;

        double x = Math.abs(z);
        double t = 1 / (1 + gamma * x);


        double n = 1
                - (1 / (Math.sqrt(2 * Math.PI)) * Math.exp(-z * z / 2))
                * (a1 * t + a2 * Math.pow(t, 2) + a3 * Math.pow(t, 3) + a4
                * Math.pow(t, 4) + a5 * Math.pow(t, 5));
        if (z < 0)
            return 1.0 - n;

        return n;
    }

    public static double calculateσ(double EM, double S, double t) {
        // 没搞懂这个公式，先写死一个相对比较常规的值
//        return EM / S / Math.pow(t / 365D, 0.5);
        return 0.16D;
    }

    public static double 认购期权保证金(double 合约最新成交价, double 合约标的最新价, double 行权价, int 合约单位) {
        return (合约最新成交价 + Math.max(0.12 * 合约标的最新价 - Math.max(行权价 - 合约标的最新价, 0), 0.07 * 合约标的最新价)) * 合约单位;
    }

    public static double 认沽期权保证金(double 合约最新成交价, double 合约标的最新价, double 行权价, int 合约单位) {
        return Math.min(合约最新成交价 + Math.max(0.12 * 合约标的最新价 - Math.max(合约标的最新价 - 行权价, 0), 0.07 * 行权价), 行权价) * 合约单位;
    }

    public static void main(String[] args) {
        double S = 2.5;

        System.out.println(认购期权保证金(0.051525094954115946D, S, 2.55, 1));
        System.out.println(认购期权保证金(515.25094954115946D, S, 25500, 1));
//        System.out.println(认沽期权保证金(0.05943, 2.522, 2.55, 1));
//        System.out.println(认购期权保证金(0.0300, 2.522, 2.55, 1));
//        System.out.println(认沽期权保证金(0.0300, 2.522, 2.55, 1));

        System.out.println(calculateC(2.55, S, 60, 0.027142, 0.1697));
        System.out.println(calculateC(25500, 25000, 60, 0.027142, 0.1697));
//        System.out.println(calculateC(2.60, 2.525, 60, 0.027142, 0.1697));
//        System.out.println(calculateC(2.65, 2.525, 60, 0.027142, 0.1697));
//
//        System.out.println(calculateC(2.55, S, 30, 0.027142, 0.1697));
//        System.out.println(calculateC(2.60, 2.525, 30, 0.027142, 0.1697));
//        System.out.println(calculateC(2.65, S, 30, 0.027142, 0.1697));

        // σ = (1/N)*sqrt(Σ[(ri - r_bar)^2])
        // 2535.14	2546.17	2553.95
//        double avg = 2535.14 + 2546.17 + 2553.95;
//        System.out.println(0.5 * Math.sqrt(Math.pow(2535.14 - avg, 2) + Math.pow(2546.17 - avg, 2) + Math.pow(2553.95 - avg, 2)));
    }
}
