package com.brightcove.scripts;
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
import java.util.Set;
import java.util.TreeMap;

public class BrightCoveCreateAsset {
    final static Logger logger = Logger.getLogger(BrightCoveCreateAsset.class);
    public static void main(String args[]){
        Map<Post_update_accountid,CreateRemoteVideo> updateMap = ReadInput1.getPostData();
        Set<Post_update_accountid> keySet = updateMap.keySet();
        XSSFWorkbook workbook = new XSSFWorkbook();
        int i=1;
        XSSFSheet sheet = workbook.createSheet(Constants.VIDEO_CLOUD_ACCOUNTS);
        Map<String, Object[]> data = new TreeMap<String, Object[]>();

        data.put(String.valueOf(i), new Object[] {Constants.ACCOUNTID, Constants.REFERENCE_ID, Constants.POST_REQUEST,Constants.RESPONSE,Constants.HLS_REQUEST,Constants.HLS_RESPONSE,Constants.FOLDER_ID,Constants.FOLDER_RESPONSE,Constants.MP4_REQUEST,Constants.MP4_RESPONSE});
        
        long parallelstart, parallelend;
        parallelstart = System.currentTimeMillis();
        keySet.parallelStream().forEach(post_update_accountid -> {
            final String post_request = updateMap.get(post_update_accountid).getPost_request();
            final String hls_request = updateMap.get(post_update_accountid).getHls_request();
            final String add_mp4_request = updateMap.get(post_update_accountid).getAdd_mp4_request();
            final String folder_id = updateMap.get(post_update_accountid).getFolder_id();
            final String reference_id = updateMap.get(post_update_accountid).getReference_id();
            logger.debug("post_request :: "+ post_request
                    +" and hls_request ::"+ hls_request +" and add_mp4_request ::"+" and folder_id ::"+folder_id);
            logger.debug("Performing patch update for accountid :: "+ post_update_accountid.getAccount_id()
                    +" and video_id ::"+ reference_id);
            JSONObject tokenInfo = TokenGenerator.getTokenWithExpiry();
            

			String token = tokenInfo.getString(Constants.TOKEN);
            HttpResponse<String> tokenresponse = null;
            
            
            logger.debug("access_token "+token);
            logger.debug("Access_token "+token);
            HttpResponse<String> response = null;
            HttpResponse<String> hlsResponse = null;
            HttpResponse<String> mp4response = null;
            HttpResponse<String> folderResponse = null;
            try {
                logger.debug("Post Request "+post_request);
                response = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos")
                		.header(Constants.AUTHORIZATION, Constants.BEARER + token)
                        .body(post_request)
                        .asString();
                logger.debug("Post response ::"+ response.getBody());

                hlsResponse = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos/"+reference_id+"/assets/hls_manifest")
                		.header(Constants.AUTHORIZATION, Constants.BEARER + token)
                        .body(hls_request)
                        .asString();

                mp4response = Unirest.post("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/videos/"+reference_id+"/assets/renditions")
                		.header(Constants.AUTHORIZATION, Constants.BEARER + token)
                        .body(add_mp4_request)
                        .asString();

                folderResponse = Unirest.put("https://cms.api.brightcove.com/v1/accounts/"+post_update_accountid.getAccount_id()+"/folders/"+folder_id+"/videos/"+reference_id)
                		.header(Constants.AUTHORIZATION, Constants.BEARER + token)
                        .asString();

                data.put(reference_id, new Object[]{post_update_accountid.getAccount_id(), reference_id, post_request, (response !=null ? response.getBody():""), hls_request, (hlsResponse !=null ? hlsResponse.getBody():""), folder_id, (folderResponse !=null ? folderResponse.getBody():""), add_mp4_request,(mp4response !=null ? mp4response.getBody():"") });

                


            } catch (UnirestException e) {
                logger.debug("Failed Performing patch update for accountid :: "+ post_update_accountid.getAccount_id()
                        +" and video_id ::"+ reference_id);
                data.put(reference_id, new Object[]{post_update_accountid.getAccount_id(), reference_id, post_request, (response !=null ? response.getBody():""), hls_request, (hlsResponse !=null ? hlsResponse.getBody():""), folder_id, (folderResponse !=null ? folderResponse.getBody():""), add_mp4_request,(mp4response !=null ? mp4response.getBody():"") });
                writeLogs(sheet, data);
                e.printStackTrace();

            }
        
    });
        writeLogs(sheet, data);
       
        
        parallelend = System.currentTimeMillis();
        logger.debug("Time taken :"+(parallelend-parallelstart));

        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(new File("/home/ntedev/Downloads/migrationscripts/outputlog_create1.xlsx"));
            workbook.write(out);
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
            try {
            FileOutputStream out = new FileOutputStream(new File("/home/ntedev/Downloads/migrationscripts/outputlog_create1.xlsx"));

                workbook.write(out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void writeLogs(XSSFSheet sheet, Map<String, Object[]> data) {
        //Iterate over data and write to sheet
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
