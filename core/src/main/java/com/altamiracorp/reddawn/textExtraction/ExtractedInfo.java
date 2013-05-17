package com.altamiracorp.reddawn.textExtraction;

public class ExtractedInfo {
  private String subject = "";
  private String text = "";

  public void setText(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
