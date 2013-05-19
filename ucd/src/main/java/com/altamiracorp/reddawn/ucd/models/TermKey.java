package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;

public class TermKey {
  @Expose
  private String sign;

  @Expose
  private String concept;

  @Expose
  private String model;

  public TermKey(String keyString) {
    String[] parts = KeyHelpers.splitOnSeperator(keyString);
    this.sign = parts[0];
    this.model = parts[1];
    this.concept = parts[2];
  }

  public TermKey() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getSign() {
    return sign;
  }

  public String getConcept() {
    return concept;
  }

  public String getModel() {
    return model;
  }

  public String toString() {
    return KeyHelpers.createCompositeKey(getSign(), getModel(), getConcept());
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return this.toString().equals(obj.toString());
  }

  public static class Builder {
    private final TermKey termKey;

    private Builder() {
      this.termKey = new TermKey();
    }

    public Builder sign(String sign) {
      this.termKey.sign = sign;
      return this;
    }

    public Builder concept(String concept) {
      this.termKey.concept = concept;
      return this;
    }

    public Builder model(String model) {
      this.termKey.model = model;
      return this;
    }

    public TermKey build() {
      return this.termKey;
    }
  }
}
