
import util.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Main {

//    public static void main(String[] args) throws Exception {
//        List<Date> calendar = getCalendar();
//        Contracts contracts = new Contracts(1); // 手上持有有所有合约
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        for (Date date : calendar) {
//            contracts.交易(date);
//
//            System.out.println(sdf.format(date) + "\t" + contracts.总资产(date));
//        }
//    }
//
//    private static List<Date> getCalendar() throws Exception {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        Date from = sdf.parse("2015-01-01");
////        Date to = sdf.parse("2023-05-23");
//        Date to = sdf.parse("2019-01-01");
//        List<Date> calendar = new ArrayList<>(); // 用来模拟的整个日历
//        for (; from.getTime() <= to.getTime(); from.setTime(from.getTime() + 24 * 3600 * 1000)) {
//            HistoryData historyData = HistoryDataUtil.上证50历史数据(from);
//            if (historyData != null) {
//                calendar.add(Date.class.cast(from.clone()));
//            }
//        }
//        return calendar;
//    }

}