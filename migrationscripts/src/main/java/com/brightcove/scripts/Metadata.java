package com.brightcove.scripts;

class Metadata{
	private String videoId;
	private String refId;
	public String getVideoId() {
		return videoId;
	}
	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}
	public String getRefId() {
		return refId;
	}
	public void setRefId(String refId) {
		this.refId = refId;
	}
	public Metadata(String videoId, String refId) {
		super();
		this.videoId = videoId;
		this.refId = refId;
	}
	public Metadata() {
		super();
	}
	
	
}