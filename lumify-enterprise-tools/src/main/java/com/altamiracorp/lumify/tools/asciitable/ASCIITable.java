package com.altamiracorp.lumify.tools.asciitable;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation builds the header and data  rows
 * by considering each column one by one in the format of 'FFFF...' (border format).
 * While rendering the last column, it closes the table.
 * It takes <max_length_string_in_each_column>+3 characters to render each column.
 * These three characters include two white spaces at left and right side of
 * the max length string in the column and one char(|) for table formatting.
 *
 * @author K Venkata Sudhakar (kvenkatasudhakar@gmail.com)
 * @version 1.0
 */
public class ASCIITable {
    public static final int ALIGN_LEFT = -1;
    public static final int ALIGN_CENTER = 0;
    public static final int ALIGN_RIGHT = 1;

    public static final int DEFAULT_HEADER_ALIGN = ALIGN_CENTER;
    public static final int DEFAULT_DATA_ALIGN = ALIGN_RIGHT;

    public static void printTable(ASCIITableHeader[] headerObjs, List<String[]> data) {
        System.out.println(getTable(headerObjs, data));
    }

    public static String getTable(ASCIITableHeader[] headerObjs, List<String[]> data) {

        if (data == null || data.size() == 0) {
            throw new IllegalArgumentException("Please provide valid data : " + data);
        }

        /**
         * Table String buffer
         */
        StringBuilder tableBuf = new StringBuilder();

        /**
         * Get maximum number of columns across all rows
         */
        String[] header = getHeaders(headerObjs);
        int colCount = getMaxColumns(header, data);

        /**
         * Get max length of data in each column
         */
        List<Integer> colMaxLenList = getMaxColLengths(colCount, header, data);

        /**
         * Check for the existence of header
         */
        if (header != null && header.length > 0) {
            /**
             * 1. Row line
             */
            tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));

            /**
             * 2. Header line
             */
            tableBuf.append(getRowDataBuf(colCount, colMaxLenList, header, headerObjs, true));
        }

        /**
         * 3. Data Row lines
         */
        tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
        String[] rowData = null;

        //Build row data buffer by iterating through all rows
        for (int i = 0; i < data.size(); i++) {

            //Build cell data in each row
            rowData = new String[colCount];
            for (int j = 0; j < colCount; j++) {

                if (j < data.get(i).length) {
                    rowData[j] = data.get(i)[j];
                } else {
                    rowData[j] = "";
                }
            }

            tableBuf.append(getRowDataBuf(colCount, colMaxLenList, rowData, headerObjs, false));
        }

        /**
         * 4. Row line
         */
        tableBuf.append(getRowLineBuf(colCount, colMaxLenList, data));
        return tableBuf.toString();
    }

    private static String getRowDataBuf(int colCount, List<Integer> colMaxLenList,
                                        String[] row, ASCIITableHeader[] headerObjs, boolean isHeader) {

        StringBuilder rowBuilder = new StringBuilder();
        String formattedData = null;
        int align;

        for (int i = 0; i < colCount; i++) {

            align = isHeader ? DEFAULT_HEADER_ALIGN : DEFAULT_DATA_ALIGN;

            if (headerObjs != null && i < headerObjs.length) {
                if (isHeader) {
                    align = headerObjs[i].getHeaderAlign();
                } else {
                    align = headerObjs[i].getDataAlign();
                }
            }

            formattedData = i < row.length ? row[i] : "";

            //format = "| %" + colFormat.get(i) + "s ";
            formattedData = "| " +
                    getFormattedData(colMaxLenList.get(i), formattedData, align) + " ";

            if (i + 1 == colCount) {
                formattedData += "|";
            }

            rowBuilder.append(formattedData);
        }

        return rowBuilder.append("\n").toString();
    }

    private static String getFormattedData(int maxLength, String data, int align) {

        if (data.length() > maxLength) {
            return data;
        }

        boolean toggle = true;

        while (data.length() < maxLength) {

            if (align == ALIGN_LEFT) {
                data = data + " ";
            } else if (align == ALIGN_RIGHT) {
                data = " " + data;
            } else if (align == ALIGN_CENTER) {
                if (toggle) {
                    data = " " + data;
                    toggle = false;
                } else {
                    data = data + " ";
                    toggle = true;
                }
            }
        }

        return data;
    }

    /**
     * Each string item rendering requires the border and a space on both sides.
     * <p/>
     * 12   3   12      3  12    34
     * +-----   +--------  +------+
     * abc      venkat     last
     *
     * @param colCount
     * @param colMaxLenList
     * @param data
     * @return
     */
    private static String getRowLineBuf(int colCount, List<Integer> colMaxLenList, List<String[]> data) {

        StringBuilder rowBuilder = new StringBuilder();
        int colWidth = 0;

        for (int i = 0; i < colCount; i++) {

            colWidth = colMaxLenList.get(i) + 3;

            for (int j = 0; j < colWidth; j++) {
                if (j == 0) {
                    rowBuilder.append("+");
                } else if ((i + 1 == colCount && j + 1 == colWidth)) {//for last column close the border
                    rowBuilder.append("-+");
                } else {
                    rowBuilder.append("-");
                }
            }
        }

        return rowBuilder.append("\n").toString();
    }

    private static int getMaxItemLength(List<String> colData) {
        int maxLength = 0;
        for (int i = 0; i < colData.size(); i++) {
            maxLength = Math.max(colData.get(i).length(), maxLength);
        }
        return maxLength;
    }

    private static int getMaxColumns(String[] header, List<String[]> data) {
        int maxColumns = 0;
        for (int i = 0; i < data.size(); i++) {
            maxColumns = Math.max(data.get(i).length, maxColumns);
        }
        maxColumns = Math.max(header.length, maxColumns);
        return maxColumns;
    }

    private static List<Integer> getMaxColLengths(int colCount, String[] header, List<String[]> data) {

        List<Integer> colMaxLenList = new ArrayList<Integer>(colCount);
        List<String> colData = null;
        int maxLength;

        for (int i = 0; i < colCount; i++) {
            colData = new ArrayList<String>();

            if (header != null && i < header.length) {
                colData.add(header[i]);
            }

            for (int j = 0; j < data.size(); j++) {
                if (i < data.get(j).length) {
                    colData.add(data.get(j)[i]);
                } else {
                    colData.add("");
                }
            }

            maxLength = getMaxItemLength(colData);
            colMaxLenList.add(maxLength);
        }

        return colMaxLenList;
    }

    private static String[] getHeaders(ASCIITableHeader[] headerObjs) {
        String[] header = new String[0];
        if (headerObjs != null && headerObjs.length > 0) {
            header = new String[headerObjs.length];
            for (int i = 0; i < headerObjs.length; i++) {
                header[i] = headerObjs[i].getHeaderName();
            }
        }

        return header;
    }

}
