package com.topanimestream.enums;

public enum Languages
{
    English(1), French(2), Japanese(3), Spanish(4);

    private final int languageId;

    private Languages(int languageId)
    {
        this.languageId = languageId;
    }

    public int getLanguageId()
    {
        return languageId;
    }

}