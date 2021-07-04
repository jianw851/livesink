package com.liveget.sink.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.DayOfWeek;

public class DateTimeUtils {
    private static DateTimeUtils INSTANCE = null;

    private static String timeZone = "America/New_York";
    private static Instant currentTime = null;

    private DateTimeUtils() {}

    public static DateTimeUtils getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new DateTimeUtils();
        }
        return INSTANCE;
    }

    public static String getTimeZone() {
        return timeZone;
    }

    public static void setTimeZone(String tz) {
        timeZone = tz;
    }

    public static void setCurrentTime(Instant now) {
        currentTime = now;
    }

    public static int getSecondsFromZonedStartOfDay() {
        Instant now = null;
        if (currentTime != null)
            now = currentTime;
        else
            now = Instant.now();
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(now, zone);
        return dateTime.getHour()*3600 + dateTime.getMinute()*60 + dateTime.getSecond();
    }

    public static DayOfWeek getDayOfWeek() {
        Instant now = null;
        if (currentTime != null)
            now = currentTime;
        else
            now = Instant.now();
        ZoneId zone = ZoneId.of(timeZone);
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(now, zone);
        return dateTime.getDayOfWeek();
    }

    public static long getCurrEpochSecond() {
        if(currentTime == null)
            return 0L;
        return currentTime.getEpochSecond();
    }

    public static String getCurrDateTimeString() {
        if(currentTime == null)
            return "";
        return currentTime.toString();
    }

    public static String getCurrDateString() {
        if(currentTime == null)
            return "";
        return currentTime.toString().substring(0,10);
    }

    /*
    input: 22:10:12
    return: seconds since 00:00:00 of the day
     */
    public static int parseTimeString(String time) throws Exception {
        String[] array = time.split(":");
        return Integer.valueOf(array[0])*3600 + Integer.valueOf(array[1])*60 + Integer.valueOf(array[2].substring(0,2));
    }

    public static DayOfWeek parseWeekString(String day) {
        if (day.equalsIgnoreCase("MON") || day.equalsIgnoreCase("MONDAY")) {
            return DayOfWeek.MONDAY;
        } else if (day.equalsIgnoreCase("TUE") || day.equalsIgnoreCase("TUESDAY")) {
            return DayOfWeek.TUESDAY;
        } else if (day.equalsIgnoreCase("WED") || day.equalsIgnoreCase("WEDNESDAY")) {
            return DayOfWeek.WEDNESDAY;
        } else if (day.equalsIgnoreCase("THU") || day.equalsIgnoreCase("THURSDAY")) {
            return DayOfWeek.THURSDAY;
        } else if (day.equalsIgnoreCase("FRI") || day.equalsIgnoreCase("FRIDAY")) {
            return DayOfWeek.FRIDAY;
        } else if (day.equalsIgnoreCase("SAT") || day.equalsIgnoreCase("SATURDAY")) {
            return DayOfWeek.SATURDAY;
        } else if (day.equalsIgnoreCase("SUN") || day.equalsIgnoreCase("SUNDAY")) {
            return DayOfWeek.SUNDAY;
        }
        return null;
    }
}
