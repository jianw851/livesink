package com.liveget.sink.parser;

public class EventTopic {
    private static final String DELIMITER = "-";
    public static String parseEventTopic(String ds) throws Exception {
        String[] array = ds.split(DELIMITER);
        if(array.length < 4) {
            // to do send to kafka
            throw new Exception();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(parseBrokerID(array[0])));
        sb.append(DELIMITER);
        sb.append(String.valueOf(parseInstrumentTypeID(array[1])));
        sb.append(DELIMITER);
        sb.append(String.valueOf(parseEventTypeID(array[2])));
        sb.append(DELIMITER);
        sb.append(array[3]);
        return sb.toString();
    }

    public static EventType parseEventTypeFromTopic(String topic) throws Exception {
        String[] array = topic.split(DELIMITER);
        return EventType.values()[Integer.valueOf(array[2])];
    }

    private static int parseBrokerID(String b) throws Exception {
        switch (b) {
            case "OANDA":
                return BrokerName.OANDA.id();
            case "IB":
                return BrokerName.IB.id();
            case "ROBINHOOD":
                return BrokerName.ROBINHOOD.id();
            case "FORESIGNAL":
                return BrokerName.FORESIGNAL.id();
            default: {
                throw new Exception("parseBrokerID -> no such BrokerID: " + b);
            }
        }
    }

    private static int parseInstrumentTypeID(String i) throws Exception {
        switch (i) {
            case "CURRENCY":
                return InstrumentType.CURRENCY.id();
            case "STOCK":
                return InstrumentType.STOCK.id();
            default: {
                throw new Exception("parseInstrumentTypeID -> no such InstrumentType: " + i);
            }
        }
    }

    private static int parseEventTypeID(String st) throws Exception {
        switch (st) {
            case "PRICING":
                return EventType.PRICING.id();
            case "SIGNAL":
                return EventType.SIGNAL.id();
            default: {
                throw new Exception("parseEventTypeID -> no such EventType: " + st);
            }
        }
    }

    private static EventType parseEventType(String st) throws Exception {
        switch (st) {
            case "PRICING":
                return EventType.PRICING;
            case "SIGNAL":
                return EventType.SIGNAL;
            default: {
                throw new Exception("parseEventType -> no such EventType: " + st);
            }
        }
    }
}
