/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A mapping that uses multiple input columns to create and parse
 * a formatted date string.
 */
@JsonTypeName("multiDate")
public class DateMultiColumnCsvPropertyColumnMapping implements CsvPropertyColumnMapping<Long> {
    /**
     * The property name.
     */
    private final String name;

    /**
     * Is this property required?
     */
    private final boolean required;

    /**
     * The column indices.
     */
    private final List<Integer> columns;

    /**
     * The format string used to generate the date string from
     * the input column values.
     */
    private final String inputFormat;

    /**
     * The expected date format string to parse.
     */
    private final String dateFormat;

    /**
     * Create a new DateMultiColumnCsvPropertyColumnMapping.  The inputFormat
     * should be a String compatible with the String.format() method, expecting
     * the string values of the specified columns as the substitution values,
     * with any <code>null</code> values provided as empty strings.  Column values
     * will be provided in the order they are specified in the input mapping.
     * @param nm the name of the property
     * @param cols the indices of the columns used to construct this date
     * @param inputFmt the format string used to generate the date string
     * @param dateFmt the format string used to parse the date string
     * @param reqd is this property required?
     */
    public DateMultiColumnCsvPropertyColumnMapping(@JsonProperty("name") final String nm,
            @JsonProperty("columns") final List<Integer> cols,
            @JsonProperty("inputFormat") final String inputFmt,
            @JsonProperty("dateFormat") final String dateFmt,
            @JsonProperty(value="required", required=false) final Boolean reqd) {
        checkNotNull(nm, "Name must be provided");
        checkArgument(!nm.trim().isEmpty(), "Name must be provided");
        checkNotNull(cols, "Columns must be provided");
        checkArgument(!cols.isEmpty(), "At least one column must be provided");
        checkNotNull(inputFmt, "Input format must be provided");
        checkArgument(!inputFmt.trim().isEmpty(), "Input format must be provided");
        checkNotNull(dateFmt, "Date format must be provided");
        checkArgument(!dateFmt.trim().isEmpty(), "Date format must be provided");
        // verify date format is valid
        SimpleDateFormat check = new SimpleDateFormat(dateFmt.trim());

        this.name = nm;
        this.required = reqd != null ? reqd : CsvPropertyColumnMapping.DEFAULT_REQUIRED;
        this.columns = Collections.unmodifiableList(new ArrayList<Integer>(cols));
        this.inputFormat = inputFmt.trim();
        this.dateFormat = dateFmt.trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @JsonProperty("columns")
    public List<Integer> getColumns() {
        return columns;
    }

    @JsonProperty("inputFormat")
    public String getInputFormat() {
        return inputFormat;
    }

    @JsonProperty("dateFormat")
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public Long getPropertyValue(final List<String> fields) {
        List<String> fieldValues = new ArrayList<String>(columns.size());
        for (Integer col : columns) {
            fieldValues.add(getFieldValue(fields, col));
        }
        Long value = null;
        String dateStr = String.format(inputFormat, fieldValues.toArray());
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                value = new SimpleDateFormat(dateFormat).parse(dateStr).getTime();
            } catch (ParseException pe) {
                value = null;
            }
        }
        if (required && value == null) {
            throw new IllegalArgumentException(String.format("%s is a required property.", name));
        }
        return value;
    }

    private String getFieldValue(final List<String> fields, final Integer idx) {
        String value;
        try {
            value = fields.get(idx);
            return value != null ? value : "";
        } catch (IndexOutOfBoundsException iobe) {
            value = "";
        }
        return value;
    }
}
