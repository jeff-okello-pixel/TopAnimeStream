package com.topanimestream.models;

import com.google.gson.annotations.SerializedName;

public class OdataRequestInfo{
    @SerializedName("@odata.context")
    private String Context;

    @SerializedName("@odata.count")
    private int Count;

    public OdataRequestInfo() {
    }

    public String getContext() {
        return Context;
    }

    public int getCount() {
        return Count;
    }
}
