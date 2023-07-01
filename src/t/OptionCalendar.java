package t;

import util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OptionCalendar {
    private List<OptionDate> dates;

    private OptionCalendar() {
        List<OptionDate> dates = new ArrayList<>();
        for (Date from = java.sql.Date.valueOf("2017-01-01"), to = java.sql.Date.valueOf("2023-05-30");
             from.before(to);
             from = DateUtil.addDay(from, 1)) {
            dates.add(new OptionDate(from));
        }
        this.dates = Collections.unmodifiableList(dates);
    }

    private static OptionCalendar instance = new OptionCalendar();

    public static OptionCalendar getInstance() {
        return instance;
    }

    public List<OptionDate> getDates() {
        return dates;
    }
}
