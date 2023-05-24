package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hoffnungland.xpath.XmlExtractorBinding;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class OrgMetadataToExcel {
	private static final Logger logger = LogManager.getLogger(OrgMetadataToExcel.class);
	private static String fileSeparator = System.getProperty("file.separator");

	public void generateExcel(String inExcelName) {

		logger.traceEntry();
		
		String targetPath = "." + fileSeparator;
		
		String targetDir = "retrieveUnpackaged";
		
		Path targetDirPath = Paths.get(targetDir);
		
		File targetDirFile = targetDirPath.toFile();
		OrgXlsMgr componentXlsMng = null;
		
		File orgXlsDir = Paths.get("." + fileSeparator + "etc" + fileSeparator + "orgXls").toFile();
		
		try {
			
			List<BundleMetadata> listAuraMetadata = new ArrayList<BundleMetadata>();
			List<BundleMetadata> listLwcMetadata = new ArrayList<BundleMetadata>();
			
			for(File curDir : targetDirFile.listFiles()) {
				
				if(curDir.isDirectory() && curDir.getName().equals("aura")) {
					
					for(File curAuraBundleDir : curDir.listFiles()) {
						if(curAuraBundleDir.isDirectory()) {
							for(File curAuraComponent : curAuraBundleDir.listFiles()) {
								BundleMetadata bundleMetadata = new BundleMetadata();
								bundleMetadata.bundleName = curAuraBundleDir.getName();
								bundleMetadata.componentName = curAuraComponent.getName();
								listAuraMetadata.add(bundleMetadata);
							}
						}
					}
				
				} else if(curDir.isDirectory() && curDir.getName().equals("lwc")) {
					
					for(File curAuraBundleDir : curDir.listFiles()) {
						if(curAuraBundleDir.isDirectory()) {
							for(File curAuraComponent : curAuraBundleDir.listFiles()) {
								BundleMetadata bundleMetadata = new BundleMetadata();
								bundleMetadata.bundleName = curAuraBundleDir.getName();
								bundleMetadata.componentName = curAuraComponent.getName();
								listLwcMetadata.add(bundleMetadata);
							}
						}
					}
				} 
			}
			
			if(!listAuraMetadata.isEmpty()) {
				if(componentXlsMng == null) {
					logger.info("Initialize the excel");
					componentXlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "Aura";
				String[] columnsList = {"Aura Bundle name", "Aura Component Name"};
				componentXlsMng.createSheetBundle(sheetName, columnsList, listAuraMetadata);
			}
			
			if(!listLwcMetadata.isEmpty()) {
				if(componentXlsMng == null) {
					logger.info("Initialize the excel");
					componentXlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "LWC";
				String[] columnsList = {"LWC Bundle name", "LWC Component Name"};
				componentXlsMng.createSheetBundle(sheetName, columnsList, listLwcMetadata);
			}
			
		} catch(IndexOutOfBoundsException | SaxonApiUncheckedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(componentXlsMng != null) {
				componentXlsMng.finalWrite(targetPath);
				logger.info(inExcelName + " is ready in " + targetPath);
				componentXlsMng = null;
			}
		}
		
		
			XmlExtractorBinding xmlExtractorBinding = new XmlExtractorBinding();
			
			net.sf.saxon.s9api.QName[] qNameList = {new net.sf.saxon.s9api.QName("", "nodeIdx")};
			XdmItem[] listBindingValuesFields = {new XdmAtomicValue(0)};
			
			for(File curTemplateDir : orgXlsDir.listFiles()) {
				
				if(curTemplateDir.isDirectory()) {
					OrgXlsMgr xlsMng = null;
					String excelName = inExcelName + " - " + curTemplateDir.getName();
					
					try {
						for(File curPropertyFile : curTemplateDir.listFiles()) {
							if(curPropertyFile.isFile() && curPropertyFile.getName().endsWith(".properties")){
								
								Properties orgXlsProperties = new Properties();
								
								try (FileInputStream configFile = new FileInputStream(curPropertyFile)) {
									orgXlsProperties.load(configFile);
								}
								
								String orgXlsFolderName = orgXlsProperties.getProperty("folder");
								
								File orgXlsFolder = new File(targetDirFile.getPath() + fileSeparator + orgXlsFolderName);
								if(orgXlsFolder.exists() && orgXlsFolder.isDirectory()) {
									
									if(xlsMng == null) {
										logger.info("Initialize the excel");
										xlsMng = new OrgXlsMgr(excelName);
									}
									
									String sheetName = curPropertyFile.getName().substring(0, curPropertyFile.getName().length()-11);
									logger.debug(sheetName);
									String orgXlsFileExtension = orgXlsProperties.getProperty("fileExtension");
									
									List<String> columnsList = new ArrayList<String>();
									columnsList.add("filename");
									
									SortedSet<String> pivotColumnList = new TreeSet<String>();
									
									String[] orgXlsFields = null;
									if(orgXlsProperties.containsKey("fields")) {
										orgXlsFields = orgXlsProperties.getProperty("fields").split(",");
										for(String curFieldName : orgXlsFields) {										
											columnsList.add(curFieldName);
										}
									}
									
									String pivotField = orgXlsProperties.getProperty("pivotField");
									String pivotValue = orgXlsProperties.getProperty("pivotValue");
									
									JsonArray pivotArray = new JsonArray();
									
									
									List<String[]> data = new ArrayList<String[]>();
									for(File curOrgXmlFile : orgXlsFolder.listFiles()) {
										if(curOrgXmlFile.isFile() && curOrgXmlFile.getName().endsWith("." + orgXlsFileExtension)){
											String fileName = curOrgXmlFile.getName().substring(0, curOrgXmlFile.getName().length()-(orgXlsFileExtension.length()+1));
											logger.debug(fileName);
											String[] rowData = null;
											if(orgXlsProperties.containsKey("baseXpath")) {									
												String orgXlsBaseXpath = orgXlsProperties.getProperty("baseXpath");
												JsonObject pivotRowValue = new JsonObject();
												
												xmlExtractorBinding.init(curOrgXmlFile, "xmlns=\"http://soap.sforce.com/2006/04/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", qNameList);
												XdmValue countItemsNode = extractNode("count(" + orgXlsBaseXpath +")", xmlExtractorBinding, qNameList, listBindingValuesFields);
												int countItems = Integer.parseInt(countItemsNode.toString());
												
												for (int nodeIdx = 0; nodeIdx < countItems; nodeIdx++){
													
													listBindingValuesFields[0] = new XdmAtomicValue(nodeIdx+1);
													if(orgXlsFields != null) {
														rowData = new String[columnsList.size()];			
														rowData[0] = fileName;
														int columnIdx = 1;
														for(String curFieldName : orgXlsFields) {
															String fieldNameXPath = orgXlsProperties.getProperty(curFieldName+".xpath", curFieldName + "/text()");
															rowData[columnIdx] = extractNode("(" + orgXlsBaseXpath + ")[$nodeIdx]/" + fieldNameXPath, xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
															columnIdx++;
														}
														data.add(rowData);
													}
													
													if(pivotField != null && pivotValue != null) {
														
														String fieldName = extractNode("(" + orgXlsBaseXpath + ")[$nodeIdx]/" + pivotField + "/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
														pivotColumnList.add(fieldName);
														String fieldValue = extractNode("(" + orgXlsBaseXpath + ")[$nodeIdx]/" + pivotValue, xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
														pivotRowValue.addProperty(fieldName, fieldValue);
														
													}
												}
												
												if(pivotField != null && pivotValue != null) {
													rowData = new String[1];
													rowData[0] = fileName;
													data.add(rowData);
													pivotArray.add(pivotRowValue);
												}
												
											} else {
												rowData = new String[1];
												rowData[0] = fileName;
												data.add(rowData);
											}
										}
									}
									if(data.size() > 0) {
										xlsMng.createSheetList(sheetName, columnsList.toArray(new String[columnsList.size()]), data, pivotColumnList, pivotArray);
									}
								}					
							}
						}
					} catch(SaxonApiException | IndexOutOfBoundsException | SaxonApiUncheckedException | IOException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} finally {
						if(xlsMng != null) {
							xlsMng.finalWrite(targetPath);
							logger.info(excelName + " is ready in " + targetPath);
							xlsMng = null;
						}
					}
				}
			}
		
		logger.traceExit();

	}
	
	private static XdmValue extractNode(String xPath, XmlExtractorBinding xmlExtractorBinding, QName[] qNameList, XdmItem[] listBindingValuesFields) throws SaxonApiException {
		logger.traceEntry();
		XPathSelector xPathSelector =  xmlExtractorBinding.defineXPath(xPath);
		return logger.traceExit(xmlExtractorBinding.evaluateBinding(xPathSelector, qNameList, listBindingValuesFields));
	}
	
	
}
