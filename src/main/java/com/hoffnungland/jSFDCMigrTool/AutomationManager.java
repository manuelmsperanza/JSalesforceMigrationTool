package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.hoffnungland.jSFDCMigrTool.entity.Automations;
import com.hoffnungland.jSFDCMigrTool.entity.ValidationRules;
import com.hoffnungland.jSFDCMigrTool.entity.Workflows;
import com.hoffnungland.xpath.XmlExtractor;
import com.salesforce.ant.DeployTask;
import com.salesforce.ant.RetrieveTask;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;

public class AutomationManager {
	
	private static final Logger logger = LogManager.getLogger(AutomationManager.class);
	private static String fileSeparator = System.getProperty("file.separator");
	
	private String passwordType;
	private String username;
	private String passwd;
	private String sessionId;
	private String serverUrl;
	private String baseDir = ".";
	private String automationPackagePathRetrieve;
	private String automationPackagePathDeploy;
	
	public AutomationManager(String passwordType, String username, String passwd, String sessionId, String serverUrl, String baseDir,
			String automationPackagePathRetrieve, String automationPackagePathDeploy) {
		super();
		this.passwordType = passwordType;
		this.username = username;
		this.passwd = passwd;
		this.sessionId = sessionId;
		this.serverUrl = serverUrl;
		this.baseDir = baseDir;
		this.automationPackagePathRetrieve = automationPackagePathRetrieve;
		this.automationPackagePathDeploy = automationPackagePathDeploy;
	}
	
	public void retrieveAutomationConfiguration() throws IOException {
		logger.traceEntry();
		RetrieveTask retrieveTask = new RetrieveTask();
		Project retrieveTaskProject = new Project();
		retrieveTaskProject.setBasedir(this.baseDir);
		retrieveTask.setProject(retrieveTaskProject);
		
		
		retrieveTask.setUsername(this.username);
		if("sessionId".equals(this.passwordType)) {
			retrieveTask.setSessionId(this.sessionId);
		} else {
			retrieveTask.setPassword(this.passwd);
		}
		if(this.serverUrl != null && this.serverUrl.length() > 0) {
			retrieveTask.setServerURL(this.serverUrl);
		}
		retrieveTask.setTaskName("retrieveUnpackaged");
		String targetDir = "retrieveUnpackaged";

		Path targetDirPath = Paths.get(this.baseDir + targetDir);

		if(!Files.exists(targetDirPath)) {
			Files.createDirectory(targetDirPath).toFile();				
		}

		retrieveTask.setRetrieveTarget(targetDir);
		retrieveTask.setUnpackaged(this.automationPackagePathRetrieve);
		retrieveTask.setTrace(logger.isTraceEnabled());
		logger.info("Run retrieve task");
		retrieveTask.execute();
		logger.info("Retrieve task done");
		logger.traceExit();
	}
	
	public void deployAutomationConfiguration() throws IOException {
		logger.traceEntry();
		
		DeployTask deployTask = new DeployTask();

		Project deployTaskProject = new Project();
		deployTaskProject.setBasedir(this.baseDir);
		deployTask.setProject(deployTaskProject);
		deployTask.setUsername(this.username);
		
		if("sessionId".equals(this.passwordType)) {
			deployTask.setSessionId(this.sessionId);
		} else {
			deployTask.setPassword(this.passwd);
		}
		
		if(this.serverUrl != null && this.serverUrl.length() > 0) {
			deployTask.setServerURL(this.serverUrl);
		}
		deployTask.setTaskName("deployUnpackaged");

		String targetDir = "retrieveUnpackaged";
		deployTask.setDeployRoot(targetDir);

		Files.copy(Paths.get(this.automationPackagePathDeploy), Paths.get(this.baseDir + fileSeparator + "retrieveUnpackaged" + fileSeparator + "package.xml"), StandardCopyOption.REPLACE_EXISTING);

		deployTask.setTrace(logger.isTraceEnabled());
		logger.info("Run deploy task");
		deployTask.execute();		
		logger.info("Deploy task done");
		logger.traceExit();
	}
	
	private void changeFileStatus(DocumentBuilder builder, Transformer transformer, File metadataFile, String statusElementName, String newStatus) throws SAXException, IOException, TransformerException {
		logger.traceEntry();
		Document doc = builder.parse(metadataFile);
		Element root = doc.getDocumentElement();
		NodeList metadataStatusNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), statusElementName);
		if(metadataStatusNodes.getLength() > 0) {
			Node metadataStatus = metadataStatusNodes.item(0);			
			metadataStatus.setTextContent(newStatus);
		} else {
			Element metadataStatusEl = doc.createElementNS(root.getNamespaceURI(), statusElementName);
			metadataStatusEl.setTextContent(newStatus);
			root.appendChild(metadataStatusEl);
		}
		
		DOMSource source = new DOMSource(doc);
		
		try(FileWriter writer = new FileWriter(metadataFile)){
			
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
		}
		logger.traceExit();
	}
	
	private void manageAutomationChange(XmlExtractor xmlExtractor, DocumentBuilder builder, Transformer transformer, File metadataDir, String metadataPrefix, String statusXPath, String statusElementName, String inactiveStatus, Map<String, String> automationMap, boolean enable) throws SaxonApiException, SAXException, IOException, TransformerException {
		logger.traceEntry();
		for(File curMetadataFile : metadataDir.listFiles()) {
			if(curMetadataFile.isFile() && curMetadataFile.getName().endsWith(metadataPrefix)){
				
				String objectName = curMetadataFile.getName().substring(0, curMetadataFile.getName().length() - metadataPrefix.length());
				logger.debug("objectName: {}", objectName);
				String newStatus = enable ? automationMap.get(objectName) : inactiveStatus;
				logger.debug("newStatus: {}", newStatus);
				xmlExtractor.init(curMetadataFile);
				String currentStatus = xmlExtractor.extractString(statusXPath, "xmlns=\"http://soap.sforce.com/2006/04/metadata\"");
				logger.debug("currentStatus: {}", currentStatus);
				if(!enable) {
					automationMap.put(objectName, currentStatus);
				}
				if((newStatus == null && currentStatus != null) || !newStatus.equals(currentStatus)) {
					this.changeFileStatus(builder, transformer, curMetadataFile, statusElementName, newStatus);
				}
			}
		}
		logger.traceExit();
	}
	
	private void manageWorkflowChange(XmlExtractor xmlExtractor, DocumentBuilder builder, Transformer transformer, File metadataDir, String metadataPrefix, Automations automationMap, boolean enable) throws SaxonApiException, SAXException, IOException, TransformerException {
		
		logger.traceEntry();
		for(File curWorkflowFile : metadataDir.listFiles()) {
			if(curWorkflowFile.isFile() && curWorkflowFile.getName().endsWith(metadataPrefix)){
				
				String objectName = curWorkflowFile.getName().substring(0, curWorkflowFile.getName().length() - metadataPrefix.length());
				logger.debug("objectName: {}", objectName);
				Workflows wfMap = automationMap.workflows.get(objectName);
				if(wfMap == null) {
					wfMap = new Workflows();
					automationMap.workflows.put(objectName, wfMap);
				}
				Document doc = builder.parse(curWorkflowFile);
				Element root = doc.getDocumentElement();
				NodeList workflowRulesNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "rules");
				for(int curWfRulesNodeIdx = 0; curWfRulesNodeIdx < workflowRulesNodes.getLength(); curWfRulesNodeIdx++) {
					Node wfRulesNode = workflowRulesNodes.item(curWfRulesNodeIdx);
					String ruleName = null;
					Node statusNode = null;
					String currentStatus = null;
					NodeList wfRulesChildren = wfRulesNode.getChildNodes();
					for(int wfRuleChildIdx = 0; wfRuleChildIdx < wfRulesChildren.getLength(); wfRuleChildIdx++) {
						Node wfRuleChildNode = wfRulesChildren.item(wfRuleChildIdx);
						switch(wfRuleChildNode.getNodeName()) {
							case "fullName":
								ruleName = wfRuleChildNode.getTextContent();
								break;
							case "active":
								statusNode = wfRuleChildNode;
								currentStatus = statusNode.getTextContent();
								logger.debug("currentStatus: {}", currentStatus);
								break;
						}
					}
					
					String newStatus = enable ? wfMap.rules.get(ruleName) : "false";
					logger.debug("newStatus: {}", newStatus);
					if(!enable) {
						wfMap.rules.put(ruleName, currentStatus);
					}
					if(!newStatus.equals(currentStatus)) {
						statusNode.setTextContent(newStatus);
					}
				}
				DOMSource source = new DOMSource(doc);
				try(FileWriter writer = new FileWriter(curWorkflowFile)){
					StreamResult result = new StreamResult(writer);
					transformer.transform(source, result);
				}
			}
		}
		logger.traceExit();
	}
	
	private void manageValidationRulesChange(XmlExtractor xmlExtractor, DocumentBuilder builder, Transformer transformer, File metadataDir, String metadataPrefix, Automations automationMap, boolean enable) throws SaxonApiException, SAXException, IOException, TransformerException {
		
		logger.traceEntry();
		for(File curObjectile : metadataDir.listFiles()) {
			if(curObjectile.isFile() && curObjectile.getName().endsWith(metadataPrefix)){
				
				String objectName = curObjectile.getName().substring(0, curObjectile.getName().length() - metadataPrefix.length());
				logger.debug("objectName: {}", objectName);
				ValidationRules vrMap = automationMap.validationRules.get(objectName);
				if(vrMap == null) {
					vrMap = new ValidationRules();
					automationMap.validationRules.put(objectName, vrMap);
				}
				Document doc = builder.parse(curObjectile);
				Element root = doc.getDocumentElement();
				NodeList validationRulesNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "validationRules");
				if(validationRulesNodes.getLength() == 0) {
					logger.debug("object without validationRules {}", objectName);
					FileUtils.delete(curObjectile);
					automationMap.validationRules.remove(objectName);
				} else {
					for(int curValidationRulesNodeIdx = 0; curValidationRulesNodeIdx < validationRulesNodes.getLength(); curValidationRulesNodeIdx++) {
						Node validationRulesNode = validationRulesNodes.item(curValidationRulesNodeIdx);
						String ruleName = null;
						Node statusNode = null;
						String currentStatus = null;
						NodeList wfRulesChildren = validationRulesNode.getChildNodes();
						for(int wfRuleChildIdx = 0; wfRuleChildIdx < wfRulesChildren.getLength(); wfRuleChildIdx++) {
							Node wfRuleChildNode = wfRulesChildren.item(wfRuleChildIdx);
							switch(wfRuleChildNode.getNodeName()) {
								case "fullName":
									ruleName = wfRuleChildNode.getTextContent();
									break;
								case "active":
									statusNode = wfRuleChildNode;
									currentStatus = statusNode.getTextContent();
									logger.debug("currentStatus: {}", currentStatus);
									break;
							}
						}
						
						String newStatus = enable ? vrMap.rules.get(ruleName) : "false";
						logger.debug("newStatus: {}", newStatus);
						if(!enable) {
							vrMap.rules.put(ruleName, currentStatus);
						}
						if(!newStatus.equals(currentStatus)) {
							statusNode.setTextContent(newStatus);
						}
					}
					DOMSource source = new DOMSource(doc);
					try(FileWriter writer = new FileWriter(curObjectile)){
						StreamResult result = new StreamResult(writer);
						transformer.transform(source, result);
					}
				}
			}
		}
		logger.traceExit();
	}
	
	public boolean checkEnablement(boolean enable) throws FileNotFoundException, IOException {
		logger.traceEntry();
		
		Gson jsonb = new GsonBuilder().create();
		Automations automations = null;
		
		String outFile = "automations.json";
		String outZipFilePath = this.baseDir + "automations.zip";
		File outZipFile = new File(outZipFilePath);
		if(outZipFile.exists()) {
			try(ZipInputStream inZip = new ZipInputStream(new FileInputStream(outZipFile));){				
				ZipEntry inZipEntry = inZip.getNextEntry();
				if(outFile.equals(inZipEntry.getName())) {
					automations = jsonb.fromJson(new InputStreamReader(inZip, "UTF-8"), Automations.class);
				}
			}
		}
		if(automations != null && !automations.enabled && !enable) {
			logger.warn("Automations aleady disabled");
			return logger.traceExit(false);
		}
		
		return logger.traceExit(true);
	}
	
	public void changeAutomations(boolean enable) throws SaxonApiException, IndexOutOfBoundsException, SaxonApiUncheckedException, SAXException, IOException, ParserConfigurationException, TransformerException {
		
		logger.traceEntry();
		
		Gson jsonb = new GsonBuilder().create();
		Automations automations = null;
		
		String outFile = "automations.json";
		String outZipFilePath = this.baseDir + "automations.zip";
		File outZipFile = new File(outZipFilePath);
		if(outZipFile.exists()) {
			try(ZipInputStream inZip = new ZipInputStream(new FileInputStream(outZipFile));){				
				ZipEntry inZipEntry = inZip.getNextEntry();
				if(outFile.equals(inZipEntry.getName())) {
					automations = jsonb.fromJson(new InputStreamReader(inZip, "UTF-8"), Automations.class);
				}
			}
		}
		if(automations != null && !automations.enabled && !enable) {
			logger.warn("Autmations aleady disabled");
			logger.traceExit();
			return;
		}
		if(!enable){
			automations = new Automations();
		}
		automations.enabled = enable;
		
		XmlExtractor xmlExtractor = new XmlExtractor();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		String targetDir = "retrieveUnpackaged";
		Path targetDirPath = Paths.get(this.baseDir + targetDir);
		File targetDirFile = targetDirPath.toFile();
		for(File curDir : targetDirFile.listFiles()) {
			
			if(curDir.isDirectory()) {
				switch(curDir.getName()) {
					case "flowDefinitions":
						this.manageAutomationChange(xmlExtractor, builder, transformer, curDir, ".flowDefinition", "/FlowDefinition/activeVersionNumber", "activeVersionNumber", "0", automations.flowDefinitions, enable);
						break;
					case "flows":
						this.manageAutomationChange(xmlExtractor, builder, transformer, curDir, ".flow", "/Flow/status", "status", "Obsolete", automations.flows, enable);
						break;
					case "triggers":
						this.manageAutomationChange(xmlExtractor, builder, transformer, curDir, ".trigger-meta.xml", "/ApexTrigger/status", "status", "Inactive", automations.triggers, enable);
						break;
					case "workflows":
						this.manageWorkflowChange(xmlExtractor, builder, transformer, curDir, ".workflow", automations, enable);
						break;
					case "objects":
						this.manageValidationRulesChange(xmlExtractor, builder, transformer, curDir, ".object", automations, enable);
						break;
				}	
			}				
		}
		
		try(ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(outZipFilePath))){
			outZip.putNextEntry(new ZipEntry(outFile));
			JsonWriter writerAutomationsMap = new JsonWriter(new OutputStreamWriter(outZip, "UTF-8"));
			writerAutomationsMap.jsonValue(jsonb.toJson(automations));
			writerAutomationsMap.close();
		}
		
		logger.traceExit();
	}
}
