package com.hoffnungland.jSFDCMigrTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

import com.hoffnungland.xpath.XmlExtractor;
import com.salesforce.ant.DeployTask;
import com.salesforce.ant.RetrieveTask;
import com.sun.tools.sjavac.Log;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class EnergyAppMaMeS {
	
	private static final Logger logger = LogManager.getLogger(EnergyAppMaMeS.class);
	
	public void executeChanges() throws SaxonApiException, IndexOutOfBoundsException, SaxonApiUncheckedException, SAXException, IOException, ParserConfigurationException, TransformerException {
		
		logger.traceEntry();
		
		String targetDir = "retrieveUnpackaged";
		
		Path targetDirPath = Paths.get(targetDir);
		
		File targetDirFile = targetDirPath.toFile();
		
		List<String> listRecordTypes = new ArrayList<String>();
		
		for(File curDir : targetDirFile.listFiles()) {
			
			if(curDir.isDirectory() && curDir.getName().equals("objects")) {
				
				for(File curObjectFile : curDir.listFiles()) {
					if(curObjectFile.isFile() && curObjectFile.getName().endsWith(".object")){
						
						String objectName = curObjectFile.getName().substring(0, curObjectFile.getName().length()-7);
						logger.debug(objectName);
						listRecordTypes.add(objectName);
						XmlExtractor xmlExtractor = new XmlExtractor();
						
						xmlExtractor.init(curObjectFile);
						XdmValue recordTypesNodes = xmlExtractor.extractNode("//recordTypes[active='true']/concat('" + objectName + ".', fullName)", "xmlns=\"http://soap.sforce.com/2006/04/metadata\"");
						for (int nodeIdx = 0; nodeIdx < recordTypesNodes.size(); nodeIdx++){
							XdmItem nodeItem = recordTypesNodes.itemAt(nodeIdx);
							listRecordTypes.add(nodeItem.getStringValue());
						}
					}
					
				}
				
			}				
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		for(File curDir : targetDirFile.listFiles()) {
			
			if(curDir.isDirectory() && curDir.getName().equals("applications")) {
				
				logger.info("Loading ProfileActionOverrides.txt");
				
				Map<String, List<String>> mapProfileActionOverride = new HashMap<String, List<String>>();
				try(BufferedReader reader = new BufferedReader(new FileReader("ProfileActionOverrides.txt"));) {
					while(reader.ready()) {
						String profileActionOverrideRow = reader.readLine();
						String[] profileActionOverrideMetadata = profileActionOverrideRow.split("\t");
						String applicationName = profileActionOverrideMetadata[0];
						
						String recordType = profileActionOverrideMetadata[5];
						boolean addRecordType = true;
						if(recordType != null && !"".equals(recordType)) {
							addRecordType = listRecordTypes.contains(recordType);
						}
						
						if(addRecordType) {
							List<String> listOverrides = null;
							if(mapProfileActionOverride.containsKey(applicationName)) {
								listOverrides = mapProfileActionOverride.get(applicationName);
							} else {
								listOverrides = new ArrayList<String>();
								mapProfileActionOverride.put(applicationName, listOverrides);
							}
							listOverrides.add(profileActionOverrideRow);
						}
					}
				}
				
				for(File curApplicationFile : curDir.listFiles()) {
					if(curApplicationFile.isFile() && curApplicationFile.getName().endsWith(".app")){
						
						String appName = curApplicationFile.getName().substring(0, curApplicationFile.getName().length() - 4 );
						logger.trace(appName);
						if(mapProfileActionOverride.containsKey(appName)) {
							
							Document doc = builder.parse(curApplicationFile);
							Element root = doc.getDocumentElement();
							
							logger.info("Manage profileActionOverrides for " + appName);
							for(String curPofileActionOverrideRow : mapProfileActionOverride.get(appName)) {
								
								String[] profileActionOverrideMetadata = curPofileActionOverrideRow.split("\t");
								String actionName = profileActionOverrideMetadata[1];
								String content = profileActionOverrideMetadata[2];
								String formFactor = profileActionOverrideMetadata[3];
								String pageOrSobjectType = profileActionOverrideMetadata[4];
								String recordType = profileActionOverrideMetadata[5];
								if(recordType == null) {
									recordType = "";
								}
								String type = profileActionOverrideMetadata[6];
								String profile = profileActionOverrideMetadata[7];
								
								NodeList profileActionOverridesNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "profileActionOverrides");
								Node firstProfileActionOverride = null;
								if(profileActionOverridesNodes.getLength() > 0) {
									firstProfileActionOverride = profileActionOverridesNodes.item(0);
								}
								
								boolean doInsert = true;
								for(int profileActionOverridesNodeIdx = 0; profileActionOverridesNodeIdx < profileActionOverridesNodes.getLength(); profileActionOverridesNodeIdx++) {
									Node profileActionOverride = profileActionOverridesNodes.item(profileActionOverridesNodeIdx);
									
									Node pageOrSobjectTypeNode = null;
									Node recordTypeNode = null;
									String recordTypeNodeValue = "";
									Node profileNode = null;
									Node contentNode = null;
									NodeList profileActionOverrideChildren = profileActionOverride.getChildNodes();
									
									for(int profileActionOverrideChildrenIdx = 0; profileActionOverrideChildrenIdx < profileActionOverrideChildren.getLength(); profileActionOverrideChildrenIdx++ ) {
										Node profileActionOverrideChild = profileActionOverrideChildren.item(profileActionOverrideChildrenIdx);
										if(profileActionOverrideChild.getNodeName().equals("pageOrSobjectType")) {
											pageOrSobjectTypeNode = profileActionOverrideChild;
										} else if(profileActionOverrideChild.getNodeName().equals("recordType")) {
											recordTypeNode = profileActionOverrideChild;
											recordTypeNodeValue = recordTypeNode.getTextContent();
										} else if(profileActionOverrideChild.getNodeName().equals("profile")) {
											profileNode = profileActionOverrideChild;
										} else if(profileActionOverrideChild.getNodeName().equals("content")) {
											contentNode = profileActionOverrideChild;
										}
									}
									
									if(pageOrSobjectType.equals(pageOrSobjectTypeNode.getTextContent()) 
											&& profile.equals(profileNode.getTextContent())
											&& recordType.equals(recordTypeNodeValue)) {
										logger.debug("update " + pageOrSobjectType + "-" + recordType + "-" + profile);
										contentNode.setTextContent(content);
										doInsert = false;
									}
									
								}
								if(doInsert) {
									logger.debug("Insert applicationVisibilities for " + appName);
									Element profileActionOverride = doc.createElementNS(root.getNamespaceURI(), "profileActionOverrides");
									
									Element actionNameNode = doc.createElementNS(root.getNamespaceURI(), "actionName");
									actionNameNode.setTextContent(actionName);
									profileActionOverride.appendChild(actionNameNode);
									
									Element contentNode = doc.createElementNS(root.getNamespaceURI(), "content");
									contentNode.setTextContent(content);
									profileActionOverride.appendChild(contentNode);
									
									Element formFactorNode = doc.createElementNS(root.getNamespaceURI(), "formFactor");
									formFactorNode.setTextContent(formFactor);
									profileActionOverride.appendChild(formFactorNode);
									
									Element pageOrSobjectTypeNode = doc.createElementNS(root.getNamespaceURI(), "pageOrSobjectType");
									pageOrSobjectTypeNode.setTextContent(pageOrSobjectType);
									profileActionOverride.appendChild(pageOrSobjectTypeNode);
									
									if(!"".equals(recordType)) {
										Element recordTypeNode = doc.createElementNS(root.getNamespaceURI(), "recordType");
										recordTypeNode.setTextContent(recordType);
										profileActionOverride.appendChild(recordTypeNode);
									}
									
									Element typeNode = doc.createElementNS(root.getNamespaceURI(), "type");
									typeNode.setTextContent(type);
									profileActionOverride.appendChild(typeNode);
									
									Element profileNode = doc.createElementNS(root.getNamespaceURI(), "profile");
									profileNode.setTextContent(profile);
									profileActionOverride.appendChild(profileNode);
									
									if(firstProfileActionOverride == null) {
										root.appendChild(profileActionOverride);
									} else {
										root.insertBefore(profileActionOverride, firstProfileActionOverride);
									}
								}
							}
							
							DOMSource source = new DOMSource(doc);
							
							try(FileWriter writer = new FileWriter(curApplicationFile)){
								
								StreamResult result = new StreamResult(writer);
								transformer.transform(source, result);
							}
							
						}
					}
				}
			} else if(curDir.isDirectory() && curDir.getName().equals("profiles")) {
				
				logger.info("Loading ApplicationVisibilities.txt");
				
				Map<String, String> mapAppVisibilities = new HashMap<String, String>();
				try(BufferedReader reader = new BufferedReader(new FileReader("ApplicationVisibilities.txt"));) {
					while(reader.ready()) {
						String appVisibilityRow = reader.readLine();
						String[] appVisibilityMetadata = appVisibilityRow.split("\t");
						String profileName = appVisibilityMetadata[0];
						
						mapAppVisibilities.put(profileName, appVisibilityRow);
					}
				}
				
				logger.info("Loading LayoutAssignments.txt");
				
				Map<String, Map<String, String>> mapProfiles = new HashMap<String, Map<String, String>>();
				try(BufferedReader reader = new BufferedReader(new FileReader("LayoutAssignments.txt"));) {
					while(reader.ready()) {
						String layoutAssignmentRow = reader.readLine();
						String[] layoutAssignmentMetadata = layoutAssignmentRow.split("\t");
						String profileName = layoutAssignmentMetadata[0];
						String layoutName = layoutAssignmentMetadata[1];
						String recordType = null;
						
						boolean addRecordType = true;
						if(layoutAssignmentMetadata.length > 2) {
							recordType = layoutAssignmentMetadata[2];
							addRecordType = listRecordTypes.contains(recordType);
						} else {
							recordType = layoutName.split("-")[0];
							logger.trace(recordType);
						}
						
						Map<String, String> mapLayouts = null;
						
						if(mapProfiles.containsKey(profileName)) {
							mapLayouts = mapProfiles.get(profileName);
						} else {
							mapLayouts = new HashMap<String, String>();
							mapProfiles.put(profileName, mapLayouts);
						}
						
						if(addRecordType) {
							mapLayouts.put(recordType, layoutName);
						} else {
							logger.warn("Record type " + recordType + " does not exits");
						}
						
					}
				}
				
				for(File curProfileFile : curDir.listFiles()) {
					if(curProfileFile.isFile() && curProfileFile.getName().endsWith(".profile")){
						
						String profileName = curProfileFile.getName().substring(0, curProfileFile.getName().length() - 8 );
						logger.trace(profileName);
						
						boolean mapProfilesContainsProfileName = mapProfiles.containsKey(profileName);
						boolean mapAppVisibilitiesContainsProfileName = mapAppVisibilities.containsKey(profileName);
						
						if(mapProfilesContainsProfileName || mapAppVisibilitiesContainsProfileName) {
							
							Document doc = builder.parse(curProfileFile);
							Element root = doc.getDocumentElement();
							
							if(mapAppVisibilitiesContainsProfileName) {
								
								logger.info("Manage applicationVisibilities for " + profileName);
								
								String appVisibilityRow = mapAppVisibilities.get(profileName);
								String[] appVisibilityMetadata = appVisibilityRow.split("\t");
								String appName = appVisibilityMetadata[1];
								String defaultFlag = appVisibilityMetadata[2];
								String visibleFlag = appVisibilityMetadata[3];
								
								NodeList appVisibilitiesNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "applicationVisibilities");
								Node firstAppVisibility = null;
								if(appVisibilitiesNodes.getLength() > 0) {
									firstAppVisibility = appVisibilitiesNodes.item(0);
								}
								
								boolean doInsert = true;
								for(int appVisibilitiesNodeIdx = 0; appVisibilitiesNodeIdx < appVisibilitiesNodes.getLength(); appVisibilitiesNodeIdx++) {
									Node appVisibility = appVisibilitiesNodes.item(appVisibilitiesNodeIdx);
									
									Node appNode = null;
									Node defaultNode = null;
									Node visibleNode = null;
									NodeList appVisibilityChildren = appVisibility.getChildNodes();
									
									for(int appVisibilityChildrenIdx = 0; appVisibilityChildrenIdx < appVisibilityChildren.getLength(); appVisibilityChildrenIdx++ ) {
										Node appVisibilityChild = appVisibilityChildren.item(appVisibilityChildrenIdx);
										if(appVisibilityChild.getNodeName().equals("application")) {
											appNode = appVisibilityChild;
										} else if(appVisibilityChild.getNodeName().equals("default")) {
											defaultNode = appVisibilityChild;
										} else if(appVisibilityChild.getNodeName().equals("visible")) {
											visibleNode = appVisibilityChild;
										}
									}
									
									if(appName.equals(appNode.getTextContent())) {
										logger.debug("update " + appNode.getTextContent());
										defaultNode.setTextContent(defaultFlag);
										visibleNode.setTextContent(visibleFlag);
										doInsert = false;
									} else if("true".equals(defaultFlag) && "true".equals(defaultNode.getTextContent())) {
										logger.debug("Remove default flag from " + appNode.getTextContent());
										defaultNode.setTextContent("false");
									}
									
								}
								if(doInsert) {
									logger.debug("Insert applicationVisibilities for " + appName);
									Element appVisibility = doc.createElementNS(root.getNamespaceURI(), "applicationVisibilities");
									
									Element appNode = doc.createElementNS(root.getNamespaceURI(), "application");
									appNode.setTextContent(appName);
									appVisibility.appendChild(appNode);
									
									Element defaultNode = doc.createElementNS(root.getNamespaceURI(), "default");
									defaultNode.setTextContent(defaultFlag);
									appVisibility.appendChild(defaultNode);
									
									Element visibleNode = doc.createElementNS(root.getNamespaceURI(), "visible");
									visibleNode.setTextContent(visibleFlag);
									appVisibility.appendChild(visibleNode);
									
									if(firstAppVisibility == null) {
										root.appendChild(appVisibility);
									} else {
										root.insertBefore(appVisibility, firstAppVisibility);
									}
								}
								
							}
							
							if(mapProfilesContainsProfileName) {
								
								logger.info("Manage layoutAssignments for " + profileName);
								
								Map<String, String> mapLayouts = mapProfiles.get(profileName);
								
								NodeList layoutAssignmentsNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "layoutAssignments");
								Node firstLayoutAssignment = null;
								if(layoutAssignmentsNodes.getLength() > 0) {
									firstLayoutAssignment = layoutAssignmentsNodes.item(0);
								}
								
								for(int layoutAssignmentsNodeIdx = 0; layoutAssignmentsNodeIdx < layoutAssignmentsNodes.getLength(); layoutAssignmentsNodeIdx++) {
									Node layoutAssignment = layoutAssignmentsNodes.item(layoutAssignmentsNodeIdx);
									
									Node layoutNode = null;
									Node recordTypeNode = null;
									NodeList layoutAssignmentChildren = layoutAssignment.getChildNodes();
									
									for(int layoutAssignmentChildrenIdx = 0; layoutAssignmentChildrenIdx < layoutAssignmentChildren.getLength(); layoutAssignmentChildrenIdx++ ) {
										Node layoutAssignmentChild = layoutAssignmentChildren.item(layoutAssignmentChildrenIdx);
										if(layoutAssignmentChild.getNodeName().equals("layout")) {
											layoutNode = layoutAssignmentChild;
										} else if(layoutAssignmentChild.getNodeName().equals("recordType")) {
											recordTypeNode = layoutAssignmentChild;
										}
									}
									
									String recordType = null;
									if(recordTypeNode == null){
										recordType = layoutNode.getTextContent().split("-")[0];
										logger.trace(recordType);
									} else {
										recordType = recordTypeNode.getTextContent();
									}
									if(mapLayouts.containsKey(recordType)) {
										String layoutName = mapLayouts.get(recordType);
										if(!layoutName.equals(layoutNode.getTextContent())) {
											logger.debug("Change layoutAssignment for " + recordType);
											layoutNode.setTextContent(layoutName);
										}
										
										mapLayouts.remove(recordType);
									}
								}
								
								for(Entry<String, String> curEntry : mapLayouts.entrySet()) {
									
									logger.debug("Insert layoutAssignments for " + curEntry.getKey());
									
									Element layoutAssignment = doc.createElementNS(root.getNamespaceURI(), "layoutAssignments");
									
									Element layout = doc.createElementNS(root.getNamespaceURI(), "layout");
									layout.setTextContent(curEntry.getValue());
									layoutAssignment.appendChild(layout);
									
									if(curEntry.getKey().contains(".")) {
										Element recordType = doc.createElementNS(root.getNamespaceURI(), "recordType");
										recordType.setTextContent(curEntry.getKey());
										layoutAssignment.appendChild(recordType);										
									}
									if(firstLayoutAssignment == null) {
										root.appendChild(layoutAssignment);
									} else {
										root.insertBefore(layoutAssignment, firstLayoutAssignment);
									}
								}
								
							}
							
							DOMSource source = new DOMSource(doc);
							
							try(FileWriter writer = new FileWriter(curProfileFile)){
								
								StreamResult result = new StreamResult(writer);
								transformer.transform(source, result);
							}
						}
					}
				}
			} else if(curDir.isDirectory() && curDir.getName().equals("settings")) {
				for(File curSettingFile : curDir.listFiles()) {
					if(curSettingFile.isFile() && curSettingFile.getName().equals("Case.settings")){
						
						String settingName = curSettingFile.getName().substring(0, curSettingFile.getName().length() - 9 );
						logger.trace(settingName);
							
						Document doc = builder.parse(curSettingFile);
						Element root = doc.getDocumentElement();
						
						NodeList closeCaseThroughStatusChangeNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "closeCaseThroughStatusChange");
						
						if(closeCaseThroughStatusChangeNodes.getLength() > 0) {
							Node firstCloseCaseThroughStatusChangeAssignment = closeCaseThroughStatusChangeNodes.item(0);
							firstCloseCaseThroughStatusChangeAssignment.setTextContent("true");
						} else {
							Element closeCaseThroughStatusChangeEl = doc.createElementNS(root.getNamespaceURI(), "closeCaseThroughStatusChange");
							closeCaseThroughStatusChangeEl.setTextContent("true");
							root.appendChild(closeCaseThroughStatusChangeEl);
						}
						
						DOMSource source = new DOMSource(doc);
						
						try(FileWriter writer = new FileWriter(curSettingFile)){
							
							StreamResult result = new StreamResult(writer);
							transformer.transform(source, result);
						}
						
					}
				}
			}
		}
		
		for(File curDir : targetDirFile.listFiles()) {
			if(curDir.isDirectory()) {
				switch(curDir.getName()) {
				case"applications" :
				case"profiles" :
				case"settings" :
					break;
				default:
					FileUtils.deleteDirectory(curDir);
				}
			}
		}
			

			
		logger.traceExit();
		
	}

}
