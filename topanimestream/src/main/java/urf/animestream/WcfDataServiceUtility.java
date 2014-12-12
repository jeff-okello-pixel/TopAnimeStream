package urf.animestream;

import android.text.TextUtils;


import java.util.ArrayList;

public class WcfDataServiceUtility {
    String dataServiceUrl;
    ArrayList<String> queries;
    public WcfDataServiceUtility(String url) {
        if(url.lastIndexOf("/") != url.length())
            url += "/";

        this.dataServiceUrl = url;
        queries = new ArrayList<String>();
    }

    public WcfDataServiceUtility getEntity(String tableName)  {
        dataServiceUrl = this.dataServiceUrl + tableName;
        return this;
    }

    public WcfDataServiceUtility getEntitySpecificRow(String tableName, int id, boolean isLong)  {
        dataServiceUrl = this.dataServiceUrl + tableName + "(" + id + (isLong ? "L" : "") + ")";
        return this;
    }

    public WcfDataServiceUtility formatJson(){
        this.format("json");
        return this;
    }

    public WcfDataServiceUtility format(String format){
        queries.add("$format=" + format);
        return this;
    }

    public WcfDataServiceUtility expand(String tables){
        queries.add("$expand=" + tables);
        return this;
    }

    public WcfDataServiceUtility filter(String filter){
        queries.add("$filter=" + filter);
        return this;
    }

    public WcfDataServiceUtility addParameter(String name, String value)
    {
        queries.add(name + "=" + value);
        return this;
    }

    public WcfDataServiceUtility top(int top){
        queries.add("$top=" + String.valueOf(top));
        return this;
    }

    public WcfDataServiceUtility select(String select){
        queries.add("$select=" + select);
        return this;
    }

    public WcfDataServiceUtility skip(int skip){
        queries.add("$skip=" + String.valueOf(skip));
        return this;
    }

    public WcfDataServiceUtility orderby(String orderBy, int orderType){
        switch (orderType)
        {
            case 0:
                queries.add("$orderby=" + orderBy);
                break;
            case 1:
                queries.add("$orderby=" + orderBy + " desc");
                break;
            default:
                queries.add("$orderby=" + orderBy);
                break;
        }

        return this;
    }

    public WcfDataServiceUtility orderby(String orderby){
        queries.add("$orderby=" + orderby);
        return this;
    }

    public String build()
    {
        String fullQuery;
        fullQuery = this.dataServiceUrl + "?" + TextUtils.join("&", this.queries);
        return fullQuery;

    }

}
