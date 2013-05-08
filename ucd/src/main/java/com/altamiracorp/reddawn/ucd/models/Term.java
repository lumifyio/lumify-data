package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.ArrayList;
import java.util.List;

public class Term {
  public static final char ROW_ID_SEPERATOR = (char) 0x1f;
  private String rowId;
  private String sign;
  private String modelKey;
  private String conceptLabel;
  private List<TermMention> termMentions = new ArrayList<TermMention>();
  private Mutation mutation;

  public void setSign(String sign) {
    this.rowId = null;
    this.sign = sign;
  }

  public String getSign() {
    return sign;
  }

  public void setModelKey(String modelKey) {
    this.rowId = null;
    this.modelKey = modelKey;
  }

  public String getModelKey() {
    return modelKey;
  }

  public void setConceptLabel(String conceptLabel) {
    this.rowId = null;
    this.conceptLabel = conceptLabel;
  }

  public String getConceptLabel() {
    return conceptLabel;
  }

  public String getRowId() {
    if (this.rowId == null) {
      this.rowId = getSign() + ROW_ID_SEPERATOR + getModelKey() + ROW_ID_SEPERATOR + getConceptLabel();
    }
    return this.rowId;
  }

  public void addTermMention(TermMention termMention) {
    this.termMentions.add(termMention);
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getRowId());
    for (TermMention termMention : this.termMentions) {
      termMention.addMutations(mutation);
    }
    return mutation;
  }
}
