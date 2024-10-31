package com.hoffnungland.jSFDCMigrTool.Admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Scanner;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ddf.EscherColorRef.SysIndexProcedure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sforce.soap.partner.LoginResult;

public class LoggerTask {
	
	private static final Logger logger = LogManager.getLogger(LoggerTask.class);
	
	private String sessionId;
	private double apiVersion = 59.0D;
	private String serviceUrl;
	private TraceFlag traceFlag;
	
	public LoggerTask(String sessionId, String serviceUrl) {
		super();
		this.sessionId = sessionId;
		this.serviceUrl = serviceUrl;
	}


	public TraceFlag getTraceFlag() {
		return traceFlag;
	}


	public void setTraceFlag(TraceFlag traceFlag) {
		this.traceFlag = traceFlag;
	}


	public void enableLog(TraceFlag traceFlag) throws IOException {
		logger.traceEntry();
		
		this.traceFlag = traceFlag;
		
		try(CloseableHttpClient httpclient = HttpClients.createDefault()){
			String targetUrl = this.serviceUrl + "/services/data/v" + this.apiVersion + "/tooling/sobjects/TraceFlag";
			logger.trace(targetUrl);
			HttpPost postRequest = new HttpPost(targetUrl);
			postRequest.addHeader("Authorization", "Bearer " + this.sessionId);
			
			Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(OffsetDateTime.class,
                    (com.google.gson.JsonSerializer<OffsetDateTime>) (src, typeOfSrc, context) ->
            new com.google.gson.JsonPrimitive(src.toString())).create();
			String content = gson.toJson(traceFlag);
			logger.trace(content);
			StringEntity myEntity = new StringEntity(content,ContentType.create("application/json", "UTF-8"));
			postRequest.setEntity(myEntity);
			BasicHttpClientResponseHandler responseClientHandler = new BasicHttpClientResponseHandler();
			String response = httpclient.execute(postRequest, responseClientHandler);
			logger.trace(response);
		}
		
		logger.traceExit();
	}
	
	public void downloadLog() throws IOException {
		logger.traceEntry();
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String targetUrl = this.serviceUrl + "/services/data/v" + this.apiVersion + "/query/?q=SELECT+Id,+LogUserId,+LogUser.Username,+LogLength,+LastModifiedDate,+Request,+Operation,+Application,+Status,+DurationMilliseconds,+SystemModstamp,+StartTime,+Location,+RequestIdentifier+FROM+ApexLog+WHERE+LogUserId='" + this.traceFlag.getTracedEntityId() + "'+ORDER+BY+StartTime";
			logger.trace(targetUrl);
			HttpGet getRequest = new HttpGet(targetUrl);
			getRequest.addHeader("Authorization", "Bearer " + sessionId);

			String response = httpclient.execute(getRequest, new BasicHttpClientResponseHandler());
			logger.trace(response);
			
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			
			JsonArray recordsArray = jsonObject.getAsJsonArray("records");
			int totalLength = 0;
			for (JsonElement recordElement : recordsArray) {
	            JsonObject recordObject = recordElement.getAsJsonObject();
	            
	            totalLength += recordObject.get("LogLength").getAsInt();
	            String requestIdentifier = recordObject.get("RequestIdentifier").getAsString();
	            String request = recordObject.get("Request").getAsString();
	            String operation = recordObject.get("Operation").getAsString();
	            
	            String fileName = requestIdentifier + "_" + request + "_" + operation.replace("/", "_") + ".log";
	            logger.trace(fileName);
	             
	            String logUrl = this.serviceUrl + recordObject.get("attributes").getAsJsonObject().get("url").getAsString();
	            
	            logger.trace(logUrl);
	            
	            
	            HttpGet getLogRequest = new HttpGet(logUrl + "/Body");
				getLogRequest.addHeader("Authorization", "Bearer " + sessionId);

				String logBody = httpclient.execute(getLogRequest, new BasicHttpClientResponseHandler());
				
				File file = new File("./logs/" + fileName);
				
				try (OutputStream outputStream = new FileOutputStream(file)) {
                    outputStream.write(logBody.getBytes());
                    System.out.println("Response saved to file: " + file.getAbsolutePath());
                }
				
				HttpDelete deleteRequest = new HttpDelete(logUrl);
				deleteRequest.addHeader("Authorization", "Bearer " + sessionId);
				
				String deleteResponse = httpclient.execute(deleteRequest, new BasicHttpClientResponseHandler());
				logger.trace(deleteResponse);
				
			}
			
			logger.info("Log size " + (totalLength/1024/1024) + " MB");
			
		}
		logger.traceExit();
	}
	
	public void clearAllLogs() throws IOException {
		
		logger.traceEntry();
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String targetUrl = this.serviceUrl + "/services/data/v" + this.apiVersion + "/query/?q=SELECT+Id,+LogUserId,+LogUser.Username,+LogLength,+LastModifiedDate,+Request,+Operation,+Application,+Status,+DurationMilliseconds,+SystemModstamp,+StartTime,+Location,+RequestIdentifier+FROM+ApexLog";
			logger.trace(targetUrl);
			HttpGet getRequest = new HttpGet(targetUrl);
			getRequest.addHeader("Authorization", "Bearer " + sessionId);

			String response = httpclient.execute(getRequest, new BasicHttpClientResponseHandler());
			logger.trace(response);
			
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			
			JsonArray recordsArray = jsonObject.getAsJsonArray("records");
			int totalLength = 0;
			for (JsonElement recordElement : recordsArray) {
	            JsonObject recordObject = recordElement.getAsJsonObject();
	            
	            totalLength += recordObject.get("LogLength").getAsInt();
	            String logUrl = this.serviceUrl + recordObject.get("attributes").getAsJsonObject().get("url").getAsString();
	            
	            logger.trace(logUrl);
	            				
				HttpDelete deleteRequest = new HttpDelete(logUrl);
				deleteRequest.addHeader("Authorization", "Bearer " + sessionId);
				
				String deleteResponse = httpclient.execute(deleteRequest, new BasicHttpClientResponseHandler());
				logger.trace(deleteResponse);
			}
			
			logger.info("Log size " + (totalLength/1024/1024) + " MB");
			
		}
		logger.traceExit();
		
	}
	
	public static void main(String[] args) {
		String username = args[0];
		String password = args[1];
		LoginAntTask loginTask = new LoginAntTask(username, password);
		
		LoginResult logingResult = loginTask.doLogin();
		System.out.println(logingResult.getSessionId());
		System.out.println(logingResult.getMetadataServerUrl());
		System.out.println(logingResult.getServerUrl());
		String endpoint = logingResult.getServerUrl();
		int baseUrl = endpoint.indexOf("/services/Soap/u");
	    String serviceUrl = endpoint.substring(0, baseUrl);
	    System.out.println(serviceUrl);
	    
	    LoggerTask loggerTask = new LoggerTask(logingResult.getSessionId(), serviceUrl);
	    
	    OffsetDateTime StartDate = OffsetDateTime.now().plusSeconds(10);
	    OffsetDateTime ExpirationDate = OffsetDateTime.now().plusMinutes(5); 
	    String TracedEntityId = args[2];
	    //select id, DeveloperName FROM DebugLevel
	    String DebugLevelId = args[3];
	    
	    TraceFlag traceFlag = new TraceFlag(LogType.USER_DEBUG, TracedEntityId, DebugLevelId, StartDate, ExpirationDate, ApexCode.FINEST, ApexProfiling.NONE, Callout.FINEST, Database.FINEST, SystemLL.FINE, Validation.INFO, Visualforce.FINER, Workflow.FINER);
	    
	    try {
	    	
	    	Scanner scanner = new Scanner(System.in);
	        
	    	
	    	loggerTask.clearAllLogs();
	    	System.out.print("Please press Enter to enable log");
	        String userInput = scanner.nextLine();
	        
	       
	    	loggerTask.enableLog(traceFlag);
	    	
	    	System.out.print("Please press Enter to download log");
	        userInput = scanner.nextLine();
	    	loggerTask.setTraceFlag(traceFlag);
			loggerTask.downloadLog();
			scanner.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	}
	
}
