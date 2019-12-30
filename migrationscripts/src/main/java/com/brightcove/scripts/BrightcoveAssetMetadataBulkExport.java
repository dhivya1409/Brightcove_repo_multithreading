package com.brightcove.scripts;

import com.brightcove.config.ScriptConfigurationProperties;
import com.brightcove.constants.*;
import com.brightcove.utils.TokenGenerator;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.brightcove.scripts.Metadata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class BrightcoveAssetMetadataBulkExport extends Thread{

	static final Logger logger = Logger.getLogger(BrightcoveAssetMetadataBulkExport.class);

	private static int offset=0;
	static int limit = 100;
	private static int count=0;
	private static int count1=0;
	static JSONObject tokenInfo = new JSONObject();
	
	static boolean exit = false;
	String CD = ",";
	String NLS = "\n";
	String header = "Video_Id,Reference_Id";
	
	static String name = null;
	private static int totalCount;
	
	List<JSONArray> videosArray = new ArrayList<JSONArray>();
	

	public static void main(String[] args) throws Exception {
		totalCount=count();
		logger.debug("total:" +totalCount);
		
		Thread[] t = new Thread [10];//amount of threads
		for(int i =0; i < t.length; i++){
		t[i] = new Thread(new BrightcoveAssetMetadataBulkExport());
			t[i].start();
		}
	}
	
	public void run() {	
		try {			
			tokenInfo = TokenGenerator.getTokenWithExpiry();
				while (offset <= totalCount) {
					offset=offset();
					Properties props = new ScriptConfigurationProperties().getPropValues();
					
					String token = tokenInfo.getString(Constants.TOKEN);
					Long tokenExpiry = tokenInfo.getLong(Constants.TOKEN_EXPIRY);
				
					String threadName = Thread.currentThread().getName();
					logger.info("&&&&&&&&&&&&&&& " + threadName);
					
					if (Instant.now().toEpochMilli() < tokenExpiry) {
						HttpResponse<String> videosListResponse;
						try {
							videosListResponse = Unirest
									.get("https://cms.api.brightcove.com/v1/accounts/"
											+ props.getProperty(Constants.ACCOUNTID) + "/videos?limit=" + limit + "&"
											+ "offset=" + offset)
									.header(Constants.AUTHORIZATION, Constants.BEARER + token).asString();
							logger.debug("response: " + new JSONArray(videosListResponse.getBody()));

							JSONArray videos = new JSONArray(videosListResponse.getBody());
							if (videos.length() <= 0) {
								logger.info("No. of videos returned: " + videos.length());
							}
							logger.info("offset value now: " + offset);
//							writeCSV(videos);
							videosArray.add(videos);
						} catch (UnirestException e) {
							e.printStackTrace();
						}}
				else {
					tokenInfo = TokenGenerator.getTokenWithExpiry();
					}
					}
				
				}catch (Exception e) {
			e.printStackTrace();
		}finally {
			writeCSV(videosArray);
			System.out.println("Completed");
		}
	}
	
	public int offset(){
		
		if(count==0) {
			offset=0;
			count++;
			logger.debug("count" +count);
		}else if(count>0){
		offset=offset+100;
		count++;
		}
		return offset;
	}
	public static int count() throws Exception {
		try {
			Properties props = new ScriptConfigurationProperties().getPropValues();

			tokenInfo = TokenGenerator.getTokenWithExpiry();

			String token = tokenInfo.getString(Constants.TOKEN);

			HttpResponse<String> totalCountResponse = Unirest.get("https://cms.api.brightcove.com/v1/accounts/"
					+ props.getProperty(Constants.ACCOUNTID) + "/counts/videos")
					.header(Constants.AUTHORIZATION, Constants.BEARER + token).asString();
			JSONObject totalCountResponseJson = new JSONObject(totalCountResponse.getBody());
			Integer count = totalCountResponseJson.getInt(Constants.COUNT);
			
			totalCount=count;

			logger.info("Total no: of videos in account: " + count);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return totalCount;
	}
	public void writeCSV(List<JSONArray> videosArray) {
		List<Metadata> meta = new ArrayList<Metadata>();
		for(JSONArray videos : videosArray) {
		for (int i = 0; i < videos.length(); i++) {

			String refId = videos.getJSONObject(i).get(Constants.REFERENCE_ID).toString() != "null"
					? videos.getJSONObject(i).get(Constants.REFERENCE_ID).toString()
					: "";

			String videoId = videos.getJSONObject(i).get(Constants.ID).toString();
			meta.add(new Metadata(refId , videoId));
		}
		}
		FileWriter fw=null;
		BufferedWriter bw = null;
		try {
			Properties props = new ScriptConfigurationProperties().getPropValues();
			fw = new FileWriter(props.getProperty(Constants.META_OUTPUT_LOG),true);
			bw = new BufferedWriter(fw);
			if(count1==0) {
				fw.write(header);
				count1++;
			}
			try {
				for(Metadata m: meta) {
					bw.write(NLS);
					bw.write(m.getRefId());
					bw.write(CD);
					bw.write(m.getVideoId());
				}
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally {
			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}
		}
		
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
}