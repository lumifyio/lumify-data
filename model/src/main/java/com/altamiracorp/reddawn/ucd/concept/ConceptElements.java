package com.altamiracorp.reddawn.ucd.concept;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class ConceptElements extends ColumnFamily {
    public static final String NAME = "ELEMENTS";
    public static final String LABEL_UI = "label_ui";

    public ConceptElements() {
        super(NAME);
    }

    public String getLabelUi() {
        return Value.toString(get(LABEL_UI));
    }

    public ConceptElements setLabelUi(String charset) {
        set(LABEL_UI, charset);
        return this;
    }
}
