package util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static final int DAY_SECOND = 24 * 3600 * 1000;

    public static Date addDay(Date d, int x) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        c.add(Calendar.DATE, x);
        return c.getTime();
    }

    /**
     * @param d
     * @return d是否就是当月的行权日
     */
    public static boolean isExpirationDate(Date d) {
        Date d1 = get当月行权日(d);
        return d1.equals(d);
    }

    public static Date get当月行权日(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);

        c.set(Calendar.DAY_OF_MONTH, 1); // 先设置成当月第1天
        int dow = c.get(Calendar.DAY_OF_WEEK); // 再获取到星期几
        if (dow <= 4) { // 如果是小于星期三
            c.set(Calendar.WEEK_OF_MONTH, 4);
        } else {
            c.set(Calendar.WEEK_OF_MONTH, 5);
        }

        c.set(Calendar.DAY_OF_WEEK, 4); // 从周日开始，第4天是周三

        HistoryData historyData = null;
        Date res = null;
        int n = 0;
        while (historyData == null) { // 恰好碰到本来是行权日的时候过年，就往后推几天
            res = c.getTime();
            historyData = HistoryDataUtil.上证50历史数据(res);
            if (historyData == null) {
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
            if (n++ > 20) { // 推了好几天都没有数据，说明数据已经没了
                return null;
            }
        }
        return res;
    }

    /**
     * @param d
     * @param next 0为最近一个行权日，1为再下一个，依次类推……
     * @return 下一个最近的行权日
     */
    public static Date getNext行权日(Date d, int next) {
        Date d1 = get当月行权日(d);
        if (d1.after(d)) {
            next -= 1;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(d1);
        c.add(Calendar.MONTH, next + 1);

        return get当月行权日(c.getTime());
    }

    public static long distanceToNext行权日(Date d, int next) {
        Date d1 = getNext行权日(d, next);
        return (d1.getTime() - d.getTime()) / 1000 / 3600 / 24;
    }

    public static long distance(Date from, Date to) {
        return (to.getTime() - from.getTime()) / 1000 / 3600 / 24;
    }

    public static void main(String[] args) {
        Date d = java.sql.Date.valueOf("2022-05-22");
//        Date d = new Date();
        System.out.println(get当月行权日(d));
        System.out.println(getNext行权日(d, 0));
//        System.out.println(getNext行权日(d, 1));
//        System.out.println(distanceToNext行权日(d, 0));
//        System.out.println(distanceToNext行权日(d, 1));

//        System.out.println(addDay(d, -1));
    }
}
