package com.altamiracorp.reddawn.ucd.models;

public class TermKey {
  private String sign;
  private String concept;
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
