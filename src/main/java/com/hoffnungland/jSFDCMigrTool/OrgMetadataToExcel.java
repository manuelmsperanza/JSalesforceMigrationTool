package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		OrgXlsMgr xlsMng = null;
		
		
		File orgXlsDir = Paths.get("." + fileSeparator + "etc" + fileSeparator + "orgXls").toFile();
		
		try {
			
			XmlExtractorBinding xmlExtractorBinding = new XmlExtractorBinding();
			
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
			
			net.sf.saxon.s9api.QName[] qNameList = {new net.sf.saxon.s9api.QName("", "nodeIdx")};
			XdmItem[] listBindingValuesFields = {new XdmAtomicValue(0)};
			
			for(File curPropertyFile : orgXlsDir.listFiles()) {
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
							xlsMng = new OrgXlsMgr(inExcelName);
						}
						
						String sheetName = curPropertyFile.getName().substring(0, curPropertyFile.getName().length()-11);
						logger.debug(sheetName);
						String orgXlsFileExtension = orgXlsProperties.getProperty("fileExtension");
						
						List<String> columnsList = new ArrayList<String>();
						columnsList.add("filename");
						
						String[] orgXlsFields = null;
						if(orgXlsProperties.containsKey("fields")) {
							orgXlsFields = orgXlsProperties.getProperty("fields").split(",");
							for(String curFieldName : orgXlsFields) {										
								columnsList.add(curFieldName);
							}
						}
						
						List<String[]> data = new ArrayList<String[]>();
						for(File curOrgXmlFile : orgXlsFolder.listFiles()) {
							if(curOrgXmlFile.isFile() && curOrgXmlFile.getName().endsWith("." + orgXlsFileExtension)){
								String fileName = curOrgXmlFile.getName().substring(0, curOrgXmlFile.getName().length()-(orgXlsFileExtension.length()+1));
								logger.debug(fileName);
								String[] rowData = null;
								if(orgXlsProperties.containsKey("baseXpath")) {									
									String orgXlsBaseXpath = orgXlsProperties.getProperty("baseXpath");
									
									xmlExtractorBinding.init(curOrgXmlFile, "xmlns=\"http://soap.sforce.com/2006/04/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"", qNameList);
									XdmValue countItemsNode = extractNode("count(" + orgXlsBaseXpath +")", xmlExtractorBinding, qNameList, listBindingValuesFields);
									int countItems = Integer.parseInt(countItemsNode.toString());
									
									for (int nodeIdx = 0; nodeIdx < countItems; nodeIdx++){
										
										rowData = new String[columnsList.size()];			
										rowData[0] = fileName;
										listBindingValuesFields[0] = new XdmAtomicValue(nodeIdx+1);
										int columnIdx = 1;
										for(String curFieldName : orgXlsFields) {
											String fieldNameXPath = orgXlsProperties.getProperty(curFieldName+".xpath", curFieldName + "/text()");
											rowData[columnIdx] = extractNode("(" + orgXlsBaseXpath + ")[$nodeIdx]/" + fieldNameXPath, xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
											columnIdx++;
										}
										data.add(rowData);
									}
									
								} else {
									rowData = new String[1];
									rowData[0] = fileName;
									data.add(rowData);
								}
							}
						}
						if(data.size() > 0) {							
							xlsMng.createSheetList(sheetName, columnsList.toArray(new String[columnsList.size()]), data);
						}
					}					
				}	
			}
			
			if(!listAuraMetadata.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "Aura";
				String[] columnsList = {"Aura Bundle name", "Aura Component Name"};
				xlsMng.createSheetBundle(sheetName, columnsList, listAuraMetadata);
			}
			
			if(!listLwcMetadata.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "LWC";
				String[] columnsList = {"LWC Bundle name", "LWC Component Name"};
				xlsMng.createSheetBundle(sheetName, columnsList, listLwcMetadata);
			}
			
		} catch(SaxonApiException | IndexOutOfBoundsException | SaxonApiUncheckedException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if(xlsMng != null) {
				xlsMng.finalWrite(targetPath);
				logger.info(inExcelName + " is ready in " + targetPath);
				xlsMng = null;
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
