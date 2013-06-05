package com.altamiracorp.reddawn.ucd.model.terms;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.altamiracorp.reddawn.ucd.model.KeyHelpers;
import com.altamiracorp.reddawn.ucd.model.base.BaseDTO;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public class TermKey implements BaseDTO<TermKey> {
  @Expose
  private String sign;

  @Expose
  private String concept;

  @Expose
  private String model;

  public TermKey(String keyString) {
    String[] parts = KeyHelpers.splitOnSeperator(keyString);
    this.sign = parts[0].toLowerCase();
    this.model = parts[1];
    this.concept = parts[2];
  }

  public TermKey() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getSign() {
    return sign.toLowerCase();
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
  public TermKey clone() {
    TermKey other = new TermKey();
    other.sign = this.sign;
    other.model = this.model;
    other.concept = this.concept;

    return other;
  }

  @Override
  public int compareTo(TermKey rhs) {
    return ComparisonChain.start()
            .compare(sign, rhs.sign, Ordering.natural().nullsFirst())
            .compare(concept, rhs.concept, Ordering.natural().nullsFirst())
            .compare(model, rhs.model, Ordering.natural().nullsFirst())
            .result();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.concept).append(this.model)
            .append(this.sign).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TermKey)) {
      return false;
    }

    TermKey other = (TermKey) obj;

    return new EqualsBuilder().append(this.concept, other.concept)
            .append(this.sign, other.sign).append(this.model, other.model)
            .isEquals();
  }

  public String toJson() throws JSONException {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return new JSONObject(gson.toJson(this)).toString();
  }

  public static class Builder {
    private final TermKey termKey;

    private Builder() {
      this.termKey = new TermKey();
    }

    public Builder sign(String sign) {
      this.termKey.sign = sign.toLowerCase();
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
