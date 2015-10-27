package com.topanimestream.models;

public class WatchType {
    private int WatchTypeId;
    private String Name;

    public WatchType(){}

    public int getWatchTypeId() {
        return WatchTypeId;
    }

    public void setWatchTypeId(int watchTypeId) {
        WatchTypeId = watchTypeId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
