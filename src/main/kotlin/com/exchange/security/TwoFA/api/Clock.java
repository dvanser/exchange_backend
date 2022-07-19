package com.exchange.security.TwoFA.api;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;


/**
 * Class sets and returns interval to be used in two factor code
 */

public class Clock {

    private final int interval;
    private Calendar calendar;

    public Clock() {
        interval = 30;
    }

    public Clock(int interval) {
        this.interval = interval;
    }

    public long getCurrentInterval() {
        calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTimeSeconds = calendar.getTimeInMillis() / 1000;
        return currentTimeSeconds / interval;
    }
}
