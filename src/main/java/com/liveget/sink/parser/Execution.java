package com.liveget.sink.parser;


import java.util.ArrayList;
import java.util.List;

public class Execution {
    private static Execution INSTANCE = null;
    private static Pricing pricing = null;
    private static Signal signal = null;
    private static EventType eventType = null;

    private Execution() {}


    public static Execution getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Execution();
        }
        return INSTANCE;
    }

    public static void setEventType(EventType e) {
        getInstance().eventType = e;
    }

    public static void setPricing(Pricing p) {
        getInstance().pricing = p;
    }

    public static void setSignal(Signal s) {
        getInstance().signal = s;
    }

    public static Signal getSignal() {
        return getInstance().signal;
    }

    public static Pricing getPricing() {
        return getInstance().pricing;
    }

    public static String getSqlInsertValues() throws Exception {
        if (INSTANCE == null) {
            return getInstance().getSqlInsertValues();
        }
        String ret = null;
        switch(eventType) {
            case PRICING:
                ret = getPricing().getSqlInsertValues();
                break;
            case SIGNAL:
                ret = getSignal().getSqlInsertValues();
                break;
            default:
                throw new Exception("EventListener::setRecord -> no such event type defined" + eventType.name());
        }
        return ret;
    }

    public static String getCreateSinkTableStatement() throws Exception {
        if (INSTANCE == null) {
            return getInstance().getCreateSinkTableStatement();
        }
        String ret = null;
        switch(eventType) {
            case PRICING:
                ret = getPricing().getCreateSinkTableStatement();
                break;
            case SIGNAL:
                ret = getSignal().getCreateSinkTableStatement();
                break;
            default:
                throw new Exception("EventListener::setRecord -> no such event type defined" + eventType.name());
        }
        return ret;
    }


    public static String getShortJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExecutionLog: {");
        sb.append("pricing: ");
        sb.append(getInstance().pricing);
        sb.append(", ");
        sb.append("signal: ");
        sb.append(getInstance().signal);
        sb.append("}");
        return sb.toString();
    }

}
