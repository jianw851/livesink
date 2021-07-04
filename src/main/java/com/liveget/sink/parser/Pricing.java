package com.liveget.sink.parser;

import com.liveget.sink.util.TradingUtils;

public abstract class Pricing {
    protected static Pricing INSTANCE = null;
    protected static String sendTime;
    protected static double bidPrice;
    protected static double askPrice;

    public String getSendTime() {
        return INSTANCE.sendTime;
    }

    public String getSendDateStr() {
        return INSTANCE.sendTime.substring(0, 10);
    }

    public double getAskPrice() {
        return INSTANCE.askPrice;
    }

    public double getBidPrice() {
        return INSTANCE.bidPrice;
    }

    public String getSqlInsertValues() {
        return String.format("('%s', %f, %f)", sendTime, bidPrice, askPrice);
    }

    public String getSQLInsertHead() { return "insert into %s (send_time, bid_price, ask_price) values"; }

    public String getCreateSinkTableStatement() { return "create table if not exists %s (send_time varchar(30), bid_price double, ask_price double);"; }


    public abstract void setRecord(String toString) throws Exception;

    public void reset() {
        INSTANCE.sendTime = null;
        INSTANCE.bidPrice = TradingUtils.DEFAULT_PRICE;
        INSTANCE.askPrice = TradingUtils.DEFAULT_PRICE;
    }

    public abstract boolean isPricingValid();
}
