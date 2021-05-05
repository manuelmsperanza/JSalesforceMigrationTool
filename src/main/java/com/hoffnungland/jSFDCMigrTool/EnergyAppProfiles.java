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
import java.util.HashMap;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hoffnungland.xpath.XmlExtractor;
import com.salesforce.ant.RetrieveTask;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmValue;

public class EnergyAppProfiles {
	
	private static final Logger logger = LogManager.getLogger(EnergyAppProfiles.class);
	
	public static void main(String[] args) {
		
		logger.traceEntry();
		
		RetrieveTask retrieveTask = new RetrieveTask();
		Project retrieveTaskProject = new Project();
		retrieveTaskProject.setBasedir(".");
		retrieveTask.setProject(retrieveTaskProject);
		retrieveTask.setUsername(args[0]);
		retrieveTask.setPassword(args[1]);
		if(args.length > 2) {
			retrieveTask.setServerURL(args[2]);
		}
		retrieveTask.setTaskName("retrieveUnpackaged");
		String targetDir = "retrieveUnpackaged";
		try {
			
			Path targetDirPath = Paths.get(targetDir);
			
			File targetDirFile = null;
			if(Files.exists(targetDirPath)) {
				targetDirFile = targetDirPath.toFile();
			} else {
				targetDirFile = Files.createDirectory(targetDirPath).toFile();				
			}
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("package.xml"))){
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.write("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");
				writer.write("<types><members>*</members>");
				writer.write("<members>Account</members>");
				writer.write("<members>AccountContactRelation</members>");
				writer.write("<members>Contact</members>");
				writer.write("<members>Individual</members>");
				writer.write("<members>Opportunity</members>");
				writer.write("<members>PersonAccount</members>");
				writer.write("<members>PriceBook2</members>");
				writer.write("<members>PriceBookEntry</members>");
				writer.write("<members>Product2</members>");
				writer.write("<name>CustomObject</name></types>");
				writer.write("<types><members>*</members><name>CustomApplication</name></types>");
				writer.write("<types><members>*</members><name>Layout</name></types>");
				writer.write("<types><members>*</members><name>Profile</name></types>");
				writer.write("<version>51.0</version>");
				writer.write("</Package>");
			}
			
			retrieveTask.setRetrieveTarget(targetDir);
			
			retrieveTask.setUnpackaged("package.xml");
			
			logger.info("Run retrieve task");
			retrieveTask.execute();
			logger.info("Retrieve task done");
			Files.delete(Paths.get("package.xml"));
				
			for(File curDir : targetDirFile.listFiles()) {
				if(curDir.isDirectory() && curDir.getName().equals("profiles")) {
					
					logger.info("Loading LayoutAssignments.txt");
					
					Map<String, Map<String, String>> mapProfiles = new HashMap<String, Map<String, String>>();
					try(BufferedReader reader = new BufferedReader(new FileReader("LayoutAssignments.txt"));) {
						while(reader.ready()) {
							String layoutAssignmentRow = reader.readLine();
							String[] layoutAssignmentMetadata = layoutAssignmentRow.split("\t");
							String profileName = layoutAssignmentMetadata[0];
							String layoutName = layoutAssignmentMetadata[1];
							String recordType = null;
							if(layoutAssignmentMetadata.length > 2) {
								recordType = layoutAssignmentMetadata[2];
							} else {
								recordType = layoutName.split("-")[0];
								logger.debug(recordType);
							}
							
							Map<String, String> mapLayouts = null;
							
							if(mapProfiles.containsKey(profileName)) {
								mapLayouts = mapProfiles.get(profileName);
							} else {
								mapLayouts = new HashMap<String, String>();
								mapProfiles.put(profileName, mapLayouts);
							}
							
							mapLayouts.put(recordType, layoutName);
							
						}
					}
					
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					DocumentBuilder builder = factory.newDocumentBuilder();
					
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					
					for(File curProfileFile : curDir.listFiles()) {
						if(curProfileFile.isFile() && curProfileFile.getName().endsWith(".profile")){
							
							String profileName = curProfileFile.getName().substring(0, curProfileFile.getName().length() - 8 );
							logger.debug(profileName);
														
							if(mapProfiles.containsKey(profileName)) {
								
								Map<String, String> mapLayouts = mapProfiles.get(profileName);
								
								Document doc = builder.parse(curProfileFile);
								Element root = doc.getDocumentElement();

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
										logger.debug(recordType);
									} else {
										recordType = recordTypeNode.getTextContent();
									}
									if(mapLayouts.containsKey(recordType)) {
										String layoutName = mapLayouts.get(recordType);
										if(!layoutName.equals(layoutNode.getTextContent())) {
											layoutNode.setTextContent(layoutName);
										}
										
										mapLayouts.remove(recordType);
									}
								}
								logger.info("Add missing layoutAssignments");
								for(Entry<String, String> curEntry : mapLayouts.entrySet()) {
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
								
								DOMSource source = new DOMSource(doc);
								
								try(FileWriter writer = new FileWriter(curProfileFile)){
									
									StreamResult result = new StreamResult(writer);
									transformer.transform(source, result);
								}
							}
						}
					}
				}
			}
			
		} catch (IOException | IndexOutOfBoundsException | SaxonApiUncheckedException | SAXException | ParserConfigurationException | TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
			
		logger.traceExit();
		
	}

}
