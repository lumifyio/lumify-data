package com.altamiracorp.lumify.tools.asciitable;

/**
 * Represents ASCII table header.
 *
 * @author K Venkata Sudhakar (kvenkatasudhakar@gmail.com)
 * @version 1.0
 */
public class ASCIITableHeader {
    private String headerName;
    private int headerAlign = ASCIITable.DEFAULT_HEADER_ALIGN;
    private int dataAlign = ASCIITable.DEFAULT_DATA_ALIGN;

    public ASCIITableHeader(String headerName) {
        this.headerName = headerName;
    }

    public ASCIITableHeader(String headerName, int dataAlign) {
        this.headerName = headerName;
        this.dataAlign = dataAlign;
    }

    public ASCIITableHeader(String headerName, int dataAlign, int headerAlign) {
        this.headerName = headerName;
        this.dataAlign = dataAlign;
        this.headerAlign = headerAlign;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public int getHeaderAlign() {
        return headerAlign;
    }

    public void setHeaderAlign(int headerAlign) {
        this.headerAlign = headerAlign;
    }

    public int getDataAlign() {
        return dataAlign;
    }

    public void setDataAlign(int dataAlign) {
        this.dataAlign = dataAlign;
    }

}

