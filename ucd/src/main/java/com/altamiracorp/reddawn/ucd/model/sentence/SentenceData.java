package com.altamiracorp.reddawn.ucd.model.sentence;

import com.altamiracorp.reddawn.ucd.model.base.BaseDTO;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

public class SentenceData implements BaseDTO<SentenceData> {
  private String artifactId;
  private Long start;
  private Long end;
  private String text;

  SentenceData(Builder builder) {
    artifactId = Strings.nullToEmpty(builder.artifactId);
    start = builder.start;
    end = builder.end;
    text = Strings.nullToEmpty(builder.text);
  }

  public String getArtifactId() {
    return artifactId;
  }

  public Long getStart() {
    return start;
  }

  public Long getEnd() {
    return end;
  }

  public String getText() {
    return text;
  }

  @Override
  public int compareTo(SentenceData o) {
    return o == null ? 1 : ComparisonChain
            .start()
            .compare(start, o.start)
            .compare(end, o.end)
            .compare(artifactId, o.artifactId,
                    Ordering.natural().nullsFirst()).compare(text, o.text)
            .result();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;

    return o instanceof SentenceData ? false
            : compareTo((SentenceData) o) == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(artifactId).append(start)
            .append(end).append(text).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("artifactId", artifactId)
            .append("start", start).append("end", end).append("text", text)
            .toString();
  }

  public static class Builder {
    private String artifactId;
    private Long start;
    private Long end;
    private String text;

    Builder() {

    }

    public Builder artifactId(String artifactId) {
      this.artifactId = artifactId;
      return this;
    }

    public Builder start(Long start) {
      this.start = start;
      return this;
    }

    public Builder end(Long end) {
      this.end = end;
      return this;
    }

    public Builder text(String text) {
      this.text = text;
      return this;
    }

    public SentenceData build() {
      return new SentenceData(this);
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder copy(Builder builder) {
      return new Builder().artifactId(builder.artifactId)
              .start(builder.start).end(builder.end).text(builder.text);
    }
  }
}
