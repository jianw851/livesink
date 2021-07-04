package com.liveget.sink.parser;

public enum BrokerName {
    UNDEFINED(  -1),
    LIVEGET(0),
    OANDA(1),
    IB(2),
    ROBINHOOD(3),
    FORESIGNAL(4);
    BrokerName(int i) {
        this.id = i;
    }

    private int id;
    public int id() {
        return this.id;
    }
}
