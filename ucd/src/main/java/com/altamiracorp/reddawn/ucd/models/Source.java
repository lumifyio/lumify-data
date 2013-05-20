package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Source {
  public static final String TABLE_NAME = "Source";

  @Expose
  private SourceMetadata metadata;

  @Expose
  private SourceKey key;

  @Expose
  private String uuid;

  private Source() {

  }

  public SourceKey getKey() {
    return this.key;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getUuid() {
    return uuid;
  }

  public static class Builder {
    private Source source = new Source();

    private Builder() {
    }

    public Builder sourceMetadata(SourceMetadata sourceMetadata) {
      this.source.metadata = sourceMetadata;
      return this;
    }

    public Builder uuid(String uuid) {
      source.uuid = uuid;
      return this;
    }

    public com.altamiracorp.reddawn.ucd.models.Source build() {
      generateKey(source);
      return source;
    }

    private void generateKey(Source source) {
      if (source.getUuid() == null) {
        throw new RuntimeException("UUID cannot be null");
      }

      if (source.getKey() == null) {
        source.key = SourceKey.newBuilder()
                .uuid(source.getUuid())
                .build();
      }
    }
  }
}
