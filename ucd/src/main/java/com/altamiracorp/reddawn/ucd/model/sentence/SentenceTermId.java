package com.altamiracorp.reddawn.ucd.model.sentence;

import com.altamiracorp.reddawn.ucd.model.base.BaseDTO;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.hadoop.thirdparty.guava.common.collect.ComparisonChain;
import org.apache.hadoop.thirdparty.guava.common.collect.Ordering;

public class SentenceTermId implements BaseDTO<SentenceTermId> {
  private String termColumnFamilyHash;
  private TermKey termId;

  SentenceTermId(Builder builder) {
    termColumnFamilyHash = builder.termColumnFamilyHash;
    termId = builder.termId;
  }

  public String getTermColumnFamilyHash() {
    return termColumnFamilyHash;
  }

  public TermKey getTermId() {
    return termId;
  }

  @Override
  public int compareTo(SentenceTermId o) {
    return o == null ? 1 : ComparisonChain.start()
            .compare(termId, o.termId, Ordering.natural().nullsFirst())
            .compare(termColumnFamilyHash, o.termColumnFamilyHash).result();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null)
      return false;

    return o instanceof SentenceTermId ? false
            : compareTo((SentenceTermId) o) == 0;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(termColumnFamilyHash)
            .append(termId).toHashCode();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("termColumnFamilyHash", termColumnFamilyHash)
            .append("termId", termId).toString();
  }

  public static class Builder {
    private String termColumnFamilyHash;
    private TermKey termId;

    Builder() {
    }

    public Builder termColumnFamilyHash(String termColumnFamilyHash) {
      this.termColumnFamilyHash = termColumnFamilyHash;
      return this;
    }

    public Builder termId(TermKey termId) {
      this.termId = termId;
      return this;
    }

    public SentenceTermId build() {
      return new SentenceTermId(this);
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public static Builder copy(Builder builder) {
      return new Builder().termColumnFamilyHash(
              builder.termColumnFamilyHash).termId(builder.termId);
    }
  }
}
