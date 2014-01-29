/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * The mapping for Date properties.  If no format is provided,
 * this mapping will attempt to parse the date as one of the
 * following formats, returning <code>null</code> if it cannot.
 * <ul>
 *   <li>MM/dd/yyyy HH:mm:ss</li>
 *   <li>MM/dd/yyyy</li>
 * </ul>
 */
@JsonTypeName("date")
public class DateCsvPropertyColumnMapping extends AbstractSimpleCsvPropertyColumnMapping<Long> {
    /**
     * The default date formats, in the order they will be attempted.
     * <ul>
     *   <li>MM/dd/yyyy HH:mm:ss</li>
     *   <li>MM/dd/yyyy</li>
     * </ul>
     */
    public static final List<String> DEFAULT_DATE_FORMATS = Collections.unmodifiableList(Arrays.asList(
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy"
    ));

    /**
     * The date format.
     */
    private final String format;

    /**
     * Create a new DateCsvPropertyColumnMapping.
     * @param index the column index
     * @param name the property name
     * @param required <code>true</code> if the property is required
     * @param fmt optionally, the date format for this column
     */
    @JsonCreator
    public DateCsvPropertyColumnMapping(@JsonProperty("column") final int index,
            @JsonProperty("name") final String name,
            @JsonProperty(value="required", required=false) final Boolean required,
            @JsonProperty(value="format", required=false) final String fmt) {
        super(index, name, required);
        this.format = isNullOrEmpty(fmt) ? null : fmt;
    }

    @JsonProperty("format")
    public String getFormat() {
        return format;
    }

    @Override
    protected Long fromString(final String fieldValue) {
        Date date = null;
        if (!isNullOrEmpty(fieldValue)) {
            if (format == null) {
                for (String fmt : DEFAULT_DATE_FORMATS) {
                    try {
                        date = new SimpleDateFormat(fmt).parse(fieldValue);
                        break;
                    } catch (ParseException pe) {
                        date = null;
                    }
                }
            } else {
                try {
                    date = new SimpleDateFormat(format).parse(fieldValue);
                } catch (ParseException pe) {
                    date = null;
                }
            }
        }
        return date != null ? date.getTime() : null;
    }
}
