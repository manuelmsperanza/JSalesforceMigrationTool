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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class ProcessClickMaMeS {
	private static final Logger logger = LogManager.getLogger(ProcessClickMaMeS.class);
	
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
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("package.xml", false))){
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.write("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");
				writer.write("<types><members>EAP2_Account_Page</members><name>FlexiPage</name></types>");
				writer.write("<types><members>wrts_prcgvr__ServiceLink__c</members><name>CustomObject</name></types>");
				writer.write("<version>51.0</version>");
				writer.write("</Package>");
			}
			
			retrieveTask.setRetrieveTarget(targetDir);
			
			retrieveTask.setUnpackaged("package.xml");
			
			logger.info("Run retrieve task");
			retrieveTask.execute();
			logger.info("Retrieve task done");
			Files.delete(Paths.get("package.xml"));
			
			
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			for(File curDir : targetDirFile.listFiles()) {
				if(curDir.isDirectory() && curDir.getName().equals("flexipages")) {
					for(File curFlexiPageFile : curDir.listFiles()) {
						if(curFlexiPageFile.isFile() && curFlexiPageFile.getName().equals("EAP2_Account_Page.flexipage")){
							Document doc = builder.parse(curFlexiPageFile);
							Element root = doc.getDocumentElement();
							NodeList flexiPageRegionsNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "flexiPageRegions");
							for(int flexiPageRegionsNodeIdx = 0; flexiPageRegionsNodeIdx < flexiPageRegionsNodes.getLength(); flexiPageRegionsNodeIdx++) {
								Node flexiPageRegionNode = flexiPageRegionsNodes.item(flexiPageRegionsNodeIdx);
								
								Node firstItemInstancesNode = null;
								Node nameNode = null;
								Node typeNode = null;
								NodeList flexiPageRegionChildren = flexiPageRegionNode.getChildNodes();
								
								for(int flexiPageRegionChildrenIdx = 0; flexiPageRegionChildrenIdx < flexiPageRegionChildren.getLength(); flexiPageRegionChildrenIdx++ ) {
									Node flexiPageRegionChild = flexiPageRegionChildren.item(flexiPageRegionChildrenIdx);
									if(flexiPageRegionChild.getNodeName().equals("itemInstances")) {
										if(firstItemInstancesNode == null) {
											firstItemInstancesNode = flexiPageRegionChild;
										}
									} else if(flexiPageRegionChild.getNodeName().equals("name")) {
										nameNode = flexiPageRegionChild;
									} else if(flexiPageRegionChild.getNodeName().equals("type")) {
										typeNode = flexiPageRegionChild;
									}
								}
								if("sidebar".equals(nameNode.getTextContent()) && "Region".equals(typeNode.getTextContent())) {
									boolean doInsert = true;
									if(firstItemInstancesNode != null) {
										Node componentInstanceNode = firstItemInstancesNode.getFirstChild();
										Node componentNameNode = componentInstanceNode.getLastChild();
										if("wrts_prcgvr:ServiceCatalogLtgCmp_1_1".equals(componentNameNode.getTextContent())) {
											doInsert = false;
											logger.warn("wrts_prcgvr:ServiceCatalogLtgCmp_1_1 already set");
										}
									}
									if(doInsert) {
										Element itemInstancesEl = doc.createElementNS(root.getNamespaceURI(), "itemInstances");
										Element componentInstanceEl = doc.createElementNS(root.getNamespaceURI(), "componentInstance");
										itemInstancesEl.appendChild(componentInstanceEl);
										
										Element componentInstancePropertiesEl = doc.createElementNS(root.getNamespaceURI(), "componentInstanceProperties");
										componentInstanceEl.appendChild(componentInstancePropertiesEl);
										
										Element componentInstancePropertiesNameEl = doc.createElementNS(root.getNamespaceURI(), "name");
										componentInstancePropertiesNameEl.setTextContent("hideStatusTab");
										componentInstancePropertiesEl.appendChild(componentInstancePropertiesNameEl);
										Element componentInstancePropertiesValueEl = doc.createElementNS(root.getNamespaceURI(), "value");
										componentInstancePropertiesValueEl.setTextContent("false");
										componentInstancePropertiesEl.appendChild(componentInstancePropertiesValueEl);
										
										Element componentNameEl = doc.createElementNS(root.getNamespaceURI(), "componentName");
										componentNameEl.setTextContent("wrts_prcgvr:ServiceCatalogLtgCmp_1_1");
										componentInstanceEl.appendChild(componentNameEl);
										
										if(firstItemInstancesNode == null) {										
											flexiPageRegionNode.appendChild(itemInstancesEl);
										} else {
											flexiPageRegionNode.insertBefore(itemInstancesEl, firstItemInstancesNode);
										}
									}
								}
								
							}
							
							DOMSource source = new DOMSource(doc);
							
							try(FileWriter writer = new FileWriter(curFlexiPageFile)){
								
								StreamResult result = new StreamResult(writer);
								transformer.transform(source, result);
							}
						}
					}
				} else if(curDir.isDirectory() && curDir.getName().equals("objects")) {
					
					for(File curObjectFile : curDir.listFiles()) {
						if(curObjectFile.isFile() && curObjectFile.getName().equals("wrts_prcgvr__ServiceLink__c.object")){
							
							Document doc = builder.parse(curObjectFile);
							Element root = doc.getDocumentElement();
							String[] categoryLov = {"Acquisition", "Disconnection", "Technical", "Commercial"};
							int categoryLovIdx = 0;
							NodeList fieldsNodes = root.getElementsByTagNameNS(root.getNamespaceURI(), "fields");
							for(int fieldsNodeIdx = 0; fieldsNodeIdx < fieldsNodes.getLength(); fieldsNodeIdx++) {
								Node fieldNode = fieldsNodes.item(fieldsNodeIdx);
								
								Node fullNameNode = null;
								Node valueSetNode = null;
								String recordTypeNodeValue = "";
								Node profileNode = null;
								Node contentNode = null;
								NodeList fieldChildren = fieldNode.getChildNodes();
								
								for(int fieldChildrenIdx = 0; fieldChildrenIdx < fieldChildren.getLength(); fieldChildrenIdx++ ) {
									Node fieldChild = fieldChildren.item(fieldChildrenIdx);
									if(fieldChild.getNodeName().equals("fullName")) {
										fullNameNode = fieldChild;
									} else if(fieldChild.getNodeName().equals("valueSet")) {
										valueSetNode = fieldChild;
									}
								}
								
								if("wrts_prcgvr__Category__c".equals(fullNameNode.getTextContent())) {
									if(valueSetNode == null) {
										logger.warn("valueSet is null");
									} else {
										logger.debug("update " + fullNameNode);
										NodeList valueSetChildren = valueSetNode.getChildNodes();
										
										for(int valueSetChildrenIdx = 0; valueSetChildrenIdx < valueSetChildren.getLength(); valueSetChildrenIdx++ ) {
											Node valueSetChild = valueSetChildren.item(valueSetChildrenIdx);
											if(valueSetChild.getNodeName().equals("valueSetDefinition")) {
												
												NodeList valueSetDefinitionChildren = valueSetChild.getChildNodes();
												
												for(int valueSetDefinitionChildrenIdx = 0; valueSetDefinitionChildrenIdx < valueSetDefinitionChildren.getLength(); valueSetDefinitionChildrenIdx++ ) {
													Node valueSetDefinitionChild = valueSetDefinitionChildren.item(valueSetDefinitionChildrenIdx);
													if(valueSetDefinitionChild.getNodeName().equals("value")) {
														if(categoryLovIdx < categoryLov.length) {
															
															String valueFullName = categoryLov[categoryLovIdx++];
															
															NodeList valueChildren = valueSetDefinitionChild.getChildNodes();
															for(int valueChildrenIdx = 0; valueChildrenIdx < valueChildren.getLength(); valueChildrenIdx++) {
																Node valueChild = valueChildren.item(valueChildrenIdx);
																if(valueChild.getNodeName().equals("fullName") || valueChild.getNodeName().equals("label")) {
																	valueChild.setTextContent(valueFullName);
																} else if(valueChild.getNodeName().equals("default")) {
																	valueChild.setTextContent("false");
																}
															}
															
														} else {
															valueSetChild.removeChild(valueSetDefinitionChild);
														}
													}
												}
												
												for(;categoryLovIdx < categoryLov.length; categoryLovIdx++) {
													String valueFullName = categoryLov[categoryLovIdx];
													
													Element valueEl = doc.createElementNS(root.getNamespaceURI(), "value");
													
													Element fullNameEl = doc.createElementNS(root.getNamespaceURI(), "fullName");
													fullNameEl.setTextContent(valueFullName);
													valueEl.appendChild(fullNameEl);
													
													Element defaultEl = doc.createElementNS(root.getNamespaceURI(), "default");
													defaultEl.setTextContent("false");
													valueEl.appendChild(defaultEl);
													
													Element labelEl = doc.createElementNS(root.getNamespaceURI(), "label");
													labelEl.setTextContent(valueFullName);
													valueEl.appendChild(labelEl);
													
													valueSetChild.appendChild(valueEl);
												}
											}
										}
									}
								}
								
							}
							
							DOMSource source = new DOMSource(doc);
							
							try(FileWriter writer = new FileWriter(curObjectFile)){
								
								StreamResult result = new StreamResult(writer);
								transformer.transform(source, result);
							}
						}
						
						
					}
				}
			}
			
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("retrieveUnpackaged\\package.xml", false))){
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.write("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">");
				writer.write("<types><members>EAP2_Account_Page</members><name>FlexiPage</name></types>");
				writer.write("<types><members>wrts_prcgvr__ServiceLink__c</members><name>CustomObject</name></types>");
				writer.write("<version>51.0</version>");
				writer.write("</Package>");
			}
			
			DeployTask deployTask = new DeployTask();
					
			Project deployTaskProject = new Project();
			deployTaskProject.setBasedir(".");
			deployTask.setProject(deployTaskProject);
			deployTask.setUsername(args[0]);
			deployTask.setPassword(args[1]);
			if(args.length > 2) {
				deployTask.setServerURL(args[2]);
			}
			deployTask.setTaskName("deployUnpackaged");
			deployTask.setDeployRoot(targetDir);
			
			logger.info("Run deploy task");
			deployTask.execute();
			logger.info("Deploy task done");
			
			FileUtils.deleteDirectory(targetDirFile);
			
		} catch (IOException | IndexOutOfBoundsException | SaxonApiUncheckedException | ParserConfigurationException | TransformerException | SAXException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
			
		logger.traceExit();
		
	}

	
}
