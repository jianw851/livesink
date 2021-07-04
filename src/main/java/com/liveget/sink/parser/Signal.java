package com.liveget.sink.parser;

import com.liveget.sink.util.TradingUtils;

public abstract class Signal {

    protected static Signal INSTANCE = null;
    protected static String Instrument = null;
    protected static String identifiedTime = null;
    protected static String feedSendTime = null;
    protected static char Direction = '\0';
    protected static double breakoutPrice = TradingUtils.DEFAULT_PRICE;
    protected static double forecastPrice = TradingUtils.DEFAULT_PRICE;
    protected static double stoplossPrice = TradingUtils.DEFAULT_PRICE;
    protected static double probability = TradingUtils.DEFAULT_PRICE;
    protected static int minPredictedTimeInSec = TradingUtils.DEFAULT_MIN_TIME_IN_SEC;
    protected static int maxPredictedTimeInSec = TradingUtils.DEFAULT_MAX_TIME_IN_SEC;

    public abstract void setRecord(String record) throws Exception;

    public String getInstrument() {
        return INSTANCE.Instrument;
    }

    public String getIdentifiedTime() {
        return INSTANCE.identifiedTime;
    }

    public String getIdentifiedDateStr() {
        return INSTANCE.identifiedTime.substring(0, 10);
    }

    public String getFeedSendTime() {
        return INSTANCE.feedSendTime;
    }

    public char getDirection() {
        return INSTANCE.Direction;
    }

    public double getBreakoutPrice() {
        return INSTANCE.breakoutPrice;
    }

    public double getForecastPrice() {
        return INSTANCE.forecastPrice;
    }

    public double getStoplossPrice() {
        return INSTANCE.stoplossPrice;
    }

    public void setBreakoutPrice(double adj) {
        INSTANCE.breakoutPrice = adj;
    }

    public void setForecastPrice(double adj) {
        INSTANCE.forecastPrice = adj;
    }

    public void setStoplossPrice(double adj) {
        INSTANCE.stoplossPrice = adj;
    }

    public double getProbability() {
        return INSTANCE.probability;
    }

    public int getMinPredictedTimeInSec() {
        return INSTANCE.minPredictedTimeInSec;
    }

    public int getMaxPredictedTimeInSec() {
        return INSTANCE.maxPredictedTimeInSec;
    }


    public String getSQLInsertHead() {
        return "insert into %s (instrument, identified_time, feed_send_time, direction, breakout_price, forecast_price, stoploss_price, probability, min_predicted_timeinsec, max_predicted_timeinsec) values";
    }

    public String getCreateSinkTableStatement() {
        return "create table if not exists %s (" +
                " instrument varchar(50)," +
                " identified_time varchar(35)," +
                " feed_send_time varchar(35)," +
                " direction varchar(5)," +
                "  breakout_price double," +
                "  forecast_price double," +
                "  stoploss_price double," +
                "  probability int," +
                "  min_predicted_timeinsec int," +
                "  max_predicted_timeinsec int" +
                " );";
    }


    public String getSqlInsertValues() {
        return String.format("('%s', '%s', '%s', '%s', %f, %f, %f, %f, %d, %d)",
                Instrument,
                identifiedTime,
                feedSendTime,
                Direction,
                breakoutPrice,
                forecastPrice,
                stoplossPrice,
                probability,
                minPredictedTimeInSec,
                maxPredictedTimeInSec
                );
    }

    public void reset() {
        INSTANCE.identifiedTime = null;
        INSTANCE.feedSendTime = null;
        INSTANCE.Direction = '\0';
        INSTANCE.breakoutPrice = TradingUtils.DEFAULT_PRICE;
        INSTANCE.forecastPrice = TradingUtils.DEFAULT_PRICE;
        INSTANCE.stoplossPrice = TradingUtils.DEFAULT_PRICE;
        INSTANCE.probability = TradingUtils.DEFAULT_PRICE;
        INSTANCE.minPredictedTimeInSec = TradingUtils.DEFAULT_MIN_TIME_IN_SEC;
        INSTANCE.maxPredictedTimeInSec = TradingUtils.DEFAULT_MAX_TIME_IN_SEC;
    }

    public abstract boolean isSignalValid();
}