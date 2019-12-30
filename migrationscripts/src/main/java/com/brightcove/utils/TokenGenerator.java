package com.brightcove.utils;

import java.time.Instant;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.brightcove.constants.Constants;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public final class TokenGenerator {
	
	static int expiry = 0;
	static long tokenExpiry = 0;
	static JSONObject tokenInfo = new JSONObject();
	
	static final Logger logger = Logger.getLogger(TokenGenerator.class);
	
	public TokenGenerator() {
		
	}
	
	public static JSONObject getTokenWithExpiry() {
		HttpResponse<String> tokenresponse;
		String token = "";
		try {
			tokenresponse = Unirest.post("https://oauth.brightcove.com/v4/access_token?grant_type=client_credentials")
					.header(Constants.CONTENT_TYPE, "application/x-www-form-urlencoded")
					.header(Constants.AUTHORIZATION,
							"Basic YTcwNmI0OWMtYmUzZC00MmE2LTk4NmEtYjIwYTdmOWU1MTUzOkVIMUNBd04tMjJhRFVka3JQcWJCUV9fSzdkbEpTdko4UTNUNjZCMFV2TnVJUF9EX2U2S0RmOXRoTTdlOWJfbWxreUx3a2ZHaGxla015czFEYkZybGlB")
					.header(Constants.CACHE_CONTROL, Constants.NO_CACHE).asString();

			JSONObject jsonObject = new JSONObject(tokenresponse.getBody());
			token = jsonObject.getString(Constants.ACCESS_TOKEN);

			expiry = jsonObject.getInt(Constants.EXPIRES_IN);
			tokenExpiry = Instant.now().toEpochMilli() + (expiry-60) * 1000;

			tokenInfo.put("token", token);
			tokenInfo.put("tokenExpiry", tokenExpiry);
			
		} catch (UnirestException e) {
			logger.error("Caught UnirestException while trying to genrate auth token: " + e.getMessage()); 
		}
		return tokenInfo;
	}
	
}
