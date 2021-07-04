package com.liveget.sink.parser;

import com.liveget.sink.util.TradingUtils;

public class SignalLiveget extends Signal {

    public SignalLiveget() {}

    public static SignalLiveget getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new SignalLiveget();
        }
        return (SignalLiveget) INSTANCE;
    }

    public void setRecord(String record) throws Exception {
        getInstance();
        // liveget adds stoploss price to the signal
        // CHF_JPY|2020-09-09 12:02:01|2020-09-08 19:52:29|116.342|117.01535|116.302|69.66|14400|14400
        String[] fields = record.split("\\|");
        INSTANCE.Instrument = fields[0];
        INSTANCE.identifiedTime = fields[1];
        INSTANCE.feedSendTime = fields[2];
        INSTANCE.breakoutPrice = Double.valueOf(fields[3]);
        INSTANCE.forecastPrice = Double.valueOf(fields[4]);
        if(fields[5].length() > 0) {
            INSTANCE.stoplossPrice = Double.valueOf(fields[5]);
        }
        if(INSTANCE.forecastPrice >= INSTANCE.breakoutPrice) {
            INSTANCE.Direction = 'L';
        } else {
            INSTANCE.Direction = 'S';
        }
        INSTANCE.probability = Double.valueOf(fields[6]);
        INSTANCE.minPredictedTimeInSec = Integer.valueOf(fields[7]);
        INSTANCE.maxPredictedTimeInSec = Integer.valueOf(fields[8]);
        if(!isSignalValid()) {
            throw new Exception("Invalid Signal received! Kafka Record:" + record);
        }
    }

    @Override
    public boolean isSignalValid() {
        return  INSTANCE.identifiedTime != null &&
                INSTANCE.feedSendTime != null &&
                INSTANCE.Direction != '\0' &&
                INSTANCE.breakoutPrice != TradingUtils.DEFAULT_PRICE &&
                INSTANCE.forecastPrice != TradingUtils.DEFAULT_PRICE &&
                INSTANCE.stoplossPrice != TradingUtils.DEFAULT_PRICE &&
                INSTANCE.probability != TradingUtils.DEFAULT_PRICE &&
                INSTANCE.minPredictedTimeInSec != TradingUtils.DEFAULT_MIN_TIME_IN_SEC &&
                INSTANCE.maxPredictedTimeInSec != TradingUtils.DEFAULT_MAX_TIME_IN_SEC;
    }


    @Override
    public String toString() {
        if(INSTANCE == null) {
            INSTANCE = new SignalLiveget();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("Instrument: ");
        sb.append(INSTANCE.Instrument);
        sb.append(", ");
        sb.append("identifiedTime: ");
        sb.append(INSTANCE.identifiedTime);
        sb.append(", ");
        sb.append("feedSendTime: ");
        sb.append(INSTANCE.feedSendTime);
        sb.append(", ");
        sb.append("Direction: ");
        sb.append(INSTANCE.Direction);
        sb.append(", ");
        sb.append("breakoutPrice: ");
        sb.append(INSTANCE.breakoutPrice);
        sb.append(", ");
        sb.append("forecastPrice: ");
        sb.append(INSTANCE.forecastPrice);
        sb.append(", ");
        sb.append("stoplossPrice: ");
        sb.append(INSTANCE.stoplossPrice);
        sb.append(", ");
        sb.append("probability: ");
        sb.append(INSTANCE.probability);
        sb.append(", ");
        sb.append("minPredictedTimeInSec: ");
        sb.append(INSTANCE.minPredictedTimeInSec);
        sb.append(", ");
        sb.append("maxPredictedTimeInSec: ");
        sb.append(INSTANCE.maxPredictedTimeInSec);
        sb.append("}");
        return sb.toString();
    }
}