package com.altamiracorp.reddawn.textExtraction;

import java.util.Date;

public class ExtractedInfo {
	private String mediaType = "";
	private String subject = "";
	private String text = "";
	private Date date;
    private String url = "";
    private String type = "";
    private String extUrl = "";
    private String srcType = "";
    private Long retrievalTime = 0l;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtUrl() {
        return extUrl;
    }

    public void setExtUrl(String extUrl) {
        this.extUrl = extUrl;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }

    public Long getRetrievalTime() {
        return retrievalTime;
    }

    public void setRetrievalTime(Long retrievalTime) {
        this.retrievalTime = retrievalTime;
    }
}
