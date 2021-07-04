package com.liveget.sink.parser;

public enum EventType {
    UNDEFINED(0),
    SIGNAL(1),
    PRICING(2);

    EventType(int i) {
        this.id = i;
    }

    private int id;

    public int id() {
        return this.id;
    }
}

