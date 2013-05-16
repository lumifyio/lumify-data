package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.*;

public class Term {
  public static final String TABLE_NAME = "Term";
  private TermKey key;
  private HashMap<String, TermMetadata> metadata = new HashMap<String, TermMetadata>();

  public static Builder newBuilder() {
    return new Builder();
  }

  public TermKey getKey() {
    return key;
  }

  public Collection<TermMetadata> getMetadata() {
    return metadata.values();
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getKey().toString());
    for (TermMetadata metadata : getMetadata()) {
      metadata.addMutations(mutation);
    }
    return mutation;
  }

  public static class Builder {
    private final Term term;

    private Builder() {
      this.term = new Term();
    }

    public Builder key(TermKey termKey) {
      this.term.key = termKey;
      return this;
    }

    public Builder metadata(TermMetadata termMetadata) {
      this.term.metadata.put(termMetadata.getColumnFamilyName(), termMetadata);
      return this;
    }

    public Term build() {
      return this.term;
    }

    public List<Term> buildFromScanner(Scanner scanner) {
      List<Term> results = new ArrayList<Term>();
      RowIterator rowIterator = new RowIterator(scanner);
      while (rowIterator.hasNext()) {
        Iterator<Map.Entry<Key, Value>> columns = rowIterator.next();
        results.add(buildFromRow(columns));
      }
      return results;
    }

    public Term buildFromRow(Iterator<Map.Entry<Key, Value>> columns) {
      Term result = new Term();
      while (columns.hasNext()) {
        Map.Entry<Key, Value> column = columns.next();
        if (result.key == null) {
          result.key = new TermKey(column.getKey().getRow().toString());
        }
        populateFromColumn(result, column);
      }
      return result;
    }

    private void populateFromColumn(Term term, Map.Entry<Key, Value> column) {
      String columnFamily = column.getKey().getColumnFamily().toString();
      TermMetadata termMetadata = term.metadata.get(columnFamily);
      if (termMetadata == null) {
        termMetadata = TermMetadata.newBuilder().build();
        term.metadata.put(columnFamily, termMetadata);
      }
      termMetadata.populateFromColumn(column);
    }

  }
}
