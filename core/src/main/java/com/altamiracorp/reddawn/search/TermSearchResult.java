package com.altamiracorp.reddawn.search;

public class TermSearchResult {
    private final String sign;
    private final String rowKey;

    public TermSearchResult (String rowKey, String sign){
        this.rowKey = rowKey;
        this.sign = sign;
    }

    public String getSign() {
        return sign;
    }

    public String getRowKey() {
        return rowKey;
    }

    @Override
    public String toString () {
        return "rowKey: " + getRowKey() + ", sign: " + getSign();
    }

}
