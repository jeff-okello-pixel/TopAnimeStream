package com.topanimestream.models;

public class Source{
    private int SourceId;
    private int LinkId;
    private String Url;
    private String Quality;
    private Link Link;

    public Source(){}

    public Link getLink() {
        return Link;
    }

    public void setLink(Link link) {
        Link = link;
    }

    public int getSourceId() {
        return SourceId;
    }

    public void setSourceId(int sourceId) {
        SourceId = sourceId;
    }

    public int getLinkId() {
        return LinkId;
    }

    public void setLinkId(int linkId) {
        LinkId = linkId;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getQuality() {
        return Quality + "p";
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    @Override
    public String toString() {
        return getQuality();
    }
}
