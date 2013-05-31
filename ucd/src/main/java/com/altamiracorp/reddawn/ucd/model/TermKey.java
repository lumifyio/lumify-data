package com.altamiracorp.reddawn.ucd.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.json.JSONException;
import org.json.JSONObject;

public class TermKey {
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
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return this.toString().equals(obj.toString());
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
