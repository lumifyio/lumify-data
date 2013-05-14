package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.*;

public class Term {
  public static final String TABLE_NAME = "Term";
  public static final char ROW_ID_SEPERATOR = (char) 0x1f;
  private String rowId;
  private String sign;
  private String modelKey;
  private String conceptLabel;
  private List<TermMention> termMentions = new ArrayList<TermMention>();

  public Term() {

  }

  public Term(String rowId) throws Exception {
    String[] parts = rowId.split(String.valueOf(ROW_ID_SEPERATOR));
    if (parts.length != 3) {
      throw new Exception("Invalid Term row id. Expected 3 parts, found " + parts.length + " parts."); // TODO: change to custom exception
    }
    this.sign = parts[0];
    this.modelKey = parts[1];
    this.conceptLabel = parts[2];
    this.rowId = rowId;
  }

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

  public Iterable<TermMention> getTermMentions() {
    return this.termMentions;
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getRowId());
    for (TermMention termMention : this.termMentions) {
      termMention.addMutations(mutation);
    }
    return mutation;
  }

  public static List<Term> createFromScanner(Scanner scanner) throws Exception {
    List<Term> results = new ArrayList<Term>();
    RowIterator rowIterator = new RowIterator(scanner);
    while (rowIterator.hasNext()) {
      Iterator<Map.Entry<Key, Value>> termRow = rowIterator.next();
      results.add(createFromRow(termRow));
    }
    return results;
  }

  private static Term createFromRow(Iterator<Map.Entry<Key, Value>> termRow) throws Exception {
    boolean first = true;
    String termRowId = null;
    HashMap<String, ArrayList<Map.Entry<Key, Value>>> termMentionMap = new HashMap<String, ArrayList<Map.Entry<Key, Value>>>();
    while (termRow.hasNext()) {
      Map.Entry<Key, Value> column = termRow.next();
      if (first) {
        termRowId = column.getKey().getRow().toString();
        first = false;
      }
      String columnFamily = column.getKey().getColumnFamily().toString();
      ArrayList<Map.Entry<Key, Value>> columnList = termMentionMap.get(columnFamily);
      if (columnList == null) {
        columnList = new ArrayList<Map.Entry<Key, Value>>();
        termMentionMap.put(columnFamily, columnList);
      }
      columnList.add(column);
    }
    if (termRowId == null) {
      return null;
    }

    Term result = new Term(termRowId);
    for (String termMentionRowId : termMentionMap.keySet()) {
      TermMention termMention = TermMention.createFromColumns(termMentionRowId, termMentionMap.get(termMentionRowId));
      result.termMentions.add(termMention);
    }
    return result;
  }
}
