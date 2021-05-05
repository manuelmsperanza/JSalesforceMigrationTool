package com.hoffnungland.jSFDCMigrTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class ExtractProfilesInformation {
	
	private static final Logger logger = LogManager.getLogger(ExtractProfilesInformation.class);
	
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
			
			XmlExtractor xmlExtractor = new XmlExtractor();
			
			try(BufferedWriter layoutAssignmentWriter = new BufferedWriter(new FileWriter("LayoutAssignments.txt", false));
					BufferedWriter appVisibilitiesWriter = new BufferedWriter(new FileWriter("ApplicationVisibilities.txt", false));){
				
				for(File curDir : targetDirFile.listFiles()) {
					if(curDir.isDirectory() && curDir.getName().equals("profiles")) {
						
						for(File curProfileFile : curDir.listFiles()) {
							if(curProfileFile.isFile() && curProfileFile.getName().endsWith(".profile")){
								
								String profileName = curProfileFile.getName().substring(0, curProfileFile.getName().length() - 8 );
								logger.debug(profileName);
															
								xmlExtractor.init(curProfileFile);
								XdmValue layoutNodes = xmlExtractor.extractNode("//layoutAssignments[contains(layout, 'EAP2_')]/concat('" + profileName + "', '\t', layout, '\t', recordType)", "xmlns=\"http://soap.sforce.com/2006/04/metadata\"");
								for (int nodeIdx = 0; nodeIdx < layoutNodes.size(); nodeIdx++){
									XdmItem nodeItem = layoutNodes.itemAt(nodeIdx);
									layoutAssignmentWriter.write(nodeItem.getStringValue());
									layoutAssignmentWriter.newLine();
								}
								XdmValue applicationNodes = xmlExtractor.extractNode("//applicationVisibilities[application='Force_com']/concat('" + profileName + "', '\t', application, '\t', default, '\t', visible)", "xmlns=\"http://soap.sforce.com/2006/04/metadata\"");
								for (int nodeIdx = 0; nodeIdx < applicationNodes.size(); nodeIdx++){
									XdmItem nodeItem = applicationNodes.itemAt(nodeIdx);
									appVisibilitiesWriter.write(nodeItem.getStringValue());
									appVisibilitiesWriter.newLine();
								}
							}
						}
					}
				}
			}
		} catch (IOException | IndexOutOfBoundsException | SaxonApiUncheckedException | SAXException | ParserConfigurationException | SaxonApiException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
			
		logger.traceExit();

	}

}
