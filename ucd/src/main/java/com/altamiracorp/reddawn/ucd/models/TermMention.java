package com.altamiracorp.reddawn.ucd.models;

public class TermMention {
  private final int start;
  private final int end;

  public TermMention(int start, int end) {
    this.start = start;
    this.end = end;
  }

  public TermMention(String str) {
    throw new RuntimeException();
  }

  public String toString() {
    return String.format("{\"start\":%d,\"end\":%d}", getStart(), getEnd());
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
