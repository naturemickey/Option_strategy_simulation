package t;

import util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class OptionDate {
    private Date date;
    private boolean isExpirationDate = false;

    public OptionDate(Date date) {
        this.date = date;
        this.isExpirationDate = DateUtil.isExpirationDate(date);
    }

    public Date getDate() {
        return date;
    }

    public boolean isExpirationDate() {
        return isExpirationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionDate that = (OptionDate) o;
        return isExpirationDate == that.isExpirationDate && Objects.equals(date, that.date);
    }

    public Date nextExpirationDate(int next) {
        return DateUtil.getNext行权日(this.date, next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, isExpirationDate);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(this.date);
    }
}
