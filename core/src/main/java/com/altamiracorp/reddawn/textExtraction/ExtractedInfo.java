package com.altamiracorp.reddawn.textExtraction;

import java.util.Date;

public class ExtractedInfo {
	private String mediaType = "";
	private String subject = "";
	private String text = "";
	private Date date;

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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
}
