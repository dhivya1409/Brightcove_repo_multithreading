package com.brightcove.scripts;
import com.brightcove.config.ScriptConfigurationProperties;
import com.brightcove.constants.Constants;
import com.brightcove.utils.TokenGenerator;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

public class BrightCoveMetadataPatch {
    final static Logger logger = Logger.getLogger(BrightCoveMetadataPatch.class);
    static JSONObject tokenInfo = new JSONObject();
    public static void main(String args[]){
    	
        HttpResponse<String> tokenresponse = null;
        Map<Patch_update_accountid,Patch_update> updateMap = ReadInput1.getData();
        Set<Patch_update_accountid> keySet = updateMap.keySet();
        //Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook();
        int i=1;
        long startTime, endTime;
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(Constants.VIDEO_CLOUD_ACCOUNTS);
        Map<String, Object[]> data = new TreeMap<String, Object[]>();
        try
        {
        Properties props = new ScriptConfigurationProperties().getPropValues();
        data.put(String.valueOf(i), new Object[] {Constants.ACCOUNTID, Constants.VIDEOID, Constants.PATCH_REQUEST, Constants.RESPONSE});
        startTime = System.currentTimeMillis();
        
        keySet.parallelStream().forEach(patch_update_acc_id ->{
                   
            final String video_id = updateMap.get(patch_update_acc_id).getVideo_id();
            final String patch_request = updateMap.get(patch_update_acc_id).getPatch_request();
            logger.debug("Performing patch update for accountid :: "+ patch_update_acc_id.getAccount_id()
                    +" and video_id ::"+ video_id);

            tokenInfo = TokenGenerator.getTokenWithExpiry();

			String token = tokenInfo.getString(Constants.TOKEN);
            logger.debug("access_token "+token);
            HttpResponse<String> response = null;
            try {
                logger.debug("Metadata Request "+patch_request);
                response = Unirest.patch("https://cms.api.brightcove.com/v1/accounts/" +props.getProperty(Constants.ACCOUNTID) + "/videos/"+video_id)
    					.header(Constants.AUTHORIZATION, Constants.BEARER + token).body(patch_request).asString();
                data.put(video_id , new Object[] {patch_update_acc_id.getAccount_id(), video_id, patch_request,(response !=null ? response.getBody():"")});
            } catch (UnirestException e) {
                data.put(video_id, new Object[] {patch_update_acc_id.getAccount_id(), video_id, patch_request,(response !=null ? response.getBody():"")});
                writeLogs(sheet, data);
                e.printStackTrace();
                logger.debug("Failed Performing patch update for accountid :: "+ patch_update_acc_id.getAccount_id()
                        +" and video_id ::"+ video_id);
            }

        });
        endTime = System.currentTimeMillis();
        System.out.println("Total time taken: "+(endTime - startTime));
        
        writeLogs(sheet, data);

        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File(props.getProperty(Constants.OUTPUT_LOG1)));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        }
        catch (IOException e1) {
			logger.error("Could not load properties file" + e1.getMessage());
		} 
        
    }

    private static void writeLogs(XSSFSheet sheet, Map<String, Object[]> data) {
        Set<String> keyset = data.keySet();
        int rownum = 0;
        for (String key : keyset)
        {
            Row row = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            for (Object obj : objArr)
            {
                Cell cell = row.createCell(cellnum++);
                if(obj instanceof String)
                    cell.setCellValue((String)obj);
                else if(obj instanceof Integer)
                    cell.setCellValue((Integer)obj);
            }
        }
    }
}
