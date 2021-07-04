package com.liveget.sink.parser;

import com.liveget.sink.util.TradingUtils;

public class PricingOanda extends Pricing {


    public PricingOanda() {}

    public static PricingOanda getInstance() {
           if (INSTANCE == null) {
               INSTANCE = new PricingOanda();
           }
           return (PricingOanda)INSTANCE;
    }


    public void setRecord(String record) throws Exception {
        getInstance();
        String[] fields = record.split("\\|");
        INSTANCE.sendTime = fields[0];
        INSTANCE.bidPrice = Double.valueOf(fields[1]);
        INSTANCE.askPrice = Double.valueOf(fields[2]);
    }

    @Override
    public boolean isPricingValid() {
        return INSTANCE.sendTime != null &&
                INSTANCE.bidPrice != TradingUtils.DEFAULT_PRICE &&
                INSTANCE.askPrice != TradingUtils.DEFAULT_PRICE;
    }

    @Override
    public String toString() {
        getInstance();
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        sb.append("sendTime: ");
        sb.append(INSTANCE.sendTime);
        sb.append(", ");
        sb.append("bidPrice: ");
        sb.append(INSTANCE.bidPrice);
        sb.append(", ");
        sb.append("askPrice: ");
        sb.append(INSTANCE.askPrice);
        sb.append("}");
        return sb.toString();
    }
}
