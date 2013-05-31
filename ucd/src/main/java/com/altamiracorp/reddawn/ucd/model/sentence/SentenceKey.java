package com.altamiracorp.reddawn.ucd.model.sentence;

import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.base.BaseDTO;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.hadoop.thirdparty.guava.common.collect.ComparisonChain;
import org.apache.hadoop.thirdparty.guava.common.collect.Ordering;

public class SentenceKey implements BaseDTO<SentenceKey> {
  private ArtifactKey artifactKey;
  private long start;
  private long end;

  private SentenceKey(Builder builder) {
    this.artifactKey = builder.artifactKey;
    this.start = builder.start;
    this.end = builder.end;
  }

  public ArtifactKey getArtifactKey() {
    return artifactKey;
  }

  public long getStart() {
    return start;
  }

  public long getEnd() {
    return end;
  }

  @Override
  public int compareTo(SentenceKey o) {
    return o == null ? 1 : ComparisonChain
            .start()
            .compare(start, o.start)
            .compare(end, o.end)
            .compare(artifactKey, o.artifactKey,
                    Ordering.natural().nullsFirst()).result();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;
    return o instanceof SentenceKey ? compareTo((SentenceKey) o) == 0
            : false;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(artifactKey).append(start)
            .append(end).toHashCode();
  }

  @Override
  public String toString() {
    // The following was from another implementation, but probably wasn't correct

//    return new ToStringBuilder(this)
//            .append("Artifact Key", this.artifactKey)
//            .append("Start", this.start)
//            .append("End", this.end)
//            .toString();

    // The following was is probably not correct but was written based off of the UCD document

    return this.artifactKey + ":" + this.start + ":" + this.end;
  }

  public static class Builder {
    ArtifactKey artifactKey;
    long start;
    long end;

    Builder() {
    }

    public Builder start(long start) {
      this.start = start;
      return this;
    }

    public Builder end(long end) {
      this.end = end;
      return this;
    }

    public Builder artifactKey(ArtifactKey artifactKey) {
      this.artifactKey = artifactKey;
      return this;
    }

    public SentenceKey build() throws IllegalArgumentException {
      if (start > end) {
        throw new IllegalArgumentException(
                "End must be greater than start");
      }
      if (artifactKey == null)
        throw new IllegalArgumentException(
                "ArtifactKey must not be null");

      return new SentenceKey(this);
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder copy(Builder builder) {
      return new Builder().artifactKey(builder.artifactKey)
              .start(builder.start).end(builder.end);
    }

  }
}
