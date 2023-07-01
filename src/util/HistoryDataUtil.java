package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HistoryDataUtil {
    private static final Map<Date, Double> _10年期国债收益率 = Collections.unmodifiableMap(read10年期国债收益率FromFile());
    private static final Map<Date, HistoryData> 上证50历史数据 = Collections.unmodifiableMap(read上证50历史数据FromFile());
    private static final Map<Date, HistoryData> 沪深300历史数据 = Collections.unmodifiableMap(read沪深300历史数据FromFile());

    public static Double _10年期国债收益率(Date date) {
        return _10年期国债收益率.get(date) / 100;
    }

    public static HistoryData 上证50历史数据(Date date) {
        return 上证50历史数据.get(date);
    }

    public static HistoryData 沪深300历史数据(Date date) {
        return 沪深300历史数据.get(date);
    }

    private static Map<Date, Double> read10年期国债收益率FromFile() {
        try {
            Map<Date, Double> _10年期国债收益率 = new HashMap<>();
            Files.lines(Paths.get("10年期国债收益率.txt")).forEach(line -> {
                // 日期	10年期国债收益率
                String[] split = line.split("\t");
                if (split[0].equals("日期")) {
                    return; // 标题行
                }
                String date = split[0];
                Double rate = Double.valueOf(split[1]);

                _10年期国债收益率.put(java.sql.Date.valueOf(date), rate);
            });
            return _10年期国债收益率;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Date, HistoryData> read上证50历史数据FromFile() {
        return readDataFromFile("上证50历史数据.txt");
    }

    private static Map<Date, HistoryData> read沪深300历史数据FromFile() {
        return readDataFromFile("沪深300历史数据.txt");
    }

    private static Map<Date, HistoryData> readDataFromFile(String path) {
        try {
            Map<Date, HistoryData> 历史数据 = new HashMap<>();
            Files.lines(Paths.get(path)).forEach(line -> {
                // 时间	收盘价	开盘价	最高价	最低价	涨跌幅	涨跌额	成交量	成交额	振幅	换手率
                String[] split = line.split("\t");
                if ("时间".equals(split[0])) {
                    return; // 标题行
                }
                String date = split[0];
                Double 收盘价 = Double.valueOf(split[1]);
                Double 开盘价 = Double.valueOf(split[2]);
                Double 最高价 = Double.valueOf(split[3]);
                Double 最低价 = Double.valueOf(split[4]);
                Double 涨跌幅 = Double.valueOf(split[5]);
                Double 涨跌额 = Double.valueOf(split[6]);
                Double 成交量 = Double.valueOf(split[7]);
                Double 成交额 = Double.valueOf(split[8]);
                Double 振幅 = Double.valueOf(split[9]);
                Double 换手率 = Double.valueOf(split[10]);
                HistoryData data = new HistoryData(收盘价, 开盘价, 最高价, 最低价, 涨跌幅, 涨跌额, 成交量, 成交额, 振幅, 换手率);

                历史数据.put(java.sql.Date.valueOf(date), data);
            });

            return 历史数据;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
