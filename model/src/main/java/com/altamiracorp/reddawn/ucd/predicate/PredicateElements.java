package com.altamiracorp.reddawn.ucd.predicate;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class PredicateElements extends ColumnFamily {
    public static final String NAME = "ELEMENTS";
    public static final String LABEL_UI = "atc:label_ui";

    public PredicateElements() {
        super(NAME);
    }

    public String getLabelUi() {
        return Value.toString(get(LABEL_UI));
    }

    public PredicateElements setLabelUi(String charset) {
        set(LABEL_UI, charset);
        return this;
    }
}
