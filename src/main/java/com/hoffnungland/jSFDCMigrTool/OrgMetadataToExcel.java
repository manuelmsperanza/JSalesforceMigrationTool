package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.hoffnungland.xpath.XmlExtractorBinding;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;

public class OrgMetadataToExcel {
	private static final Logger logger = LogManager.getLogger(OrgMetadataToExcel.class);

	public static void main(String[] args) {

		logger.traceEntry();
		
		String targetPath = "./";
		String inExcelName = "OrgMetadata_EA2";
		String targetDir = "ea2/retrieveUnpackaged";
		//String targetDir = "scratchOrg/retrieveUnpackaged";
		
		Path targetDirPath = Paths.get(targetDir);
		
		File targetDirFile = targetDirPath.toFile();
		OrgXlsMgr xlsMng = null;
		try {
			
			XmlExtractorBinding xmlExtractorBinding = new XmlExtractorBinding();
			
			List<BundleMetadata> listAuraMetadata = new ArrayList<BundleMetadata>();
			List<String> listClasses = new ArrayList<String>();
			List<LabelMetadata> listLabelMetadata = new ArrayList<LabelMetadata>();
			List<BundleMetadata> listLwcMetadata = new ArrayList<BundleMetadata>();
			List<FieldMetadata> listFieldMetadata = new ArrayList<FieldMetadata>();
			List<String> listTriggers = new ArrayList<String>();
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
				} else if(curDir.isDirectory() && curDir.getName().equals("classes")) {
					
					for(File curClassFile : curDir.listFiles()) {
						if(curClassFile.isFile() && curClassFile.getName().endsWith(".cls")){
							String className = curClassFile.getName().substring(0, curClassFile.getName().length()-4);
							logger.debug(className);
							listClasses.add(className);
						}
					}
				} else if(curDir.isDirectory() && curDir.getName().equals("labels")) {
					net.sf.saxon.s9api.QName[] qNameList = {new net.sf.saxon.s9api.QName("", "labelIdx")};
					XdmItem[] listBindingValuesFields = {new XdmAtomicValue(0)};
					
					for(File curLabelsFile : curDir.listFiles()) {
						if(curLabelsFile.isFile() && curLabelsFile.getName().endsWith(".labels")){
							String labelFileName = curLabelsFile.getName().substring(0, curLabelsFile.getName().length()-7);
							logger.debug(labelFileName);
							xmlExtractorBinding.init(curLabelsFile, "xmlns=\"http://soap.sforce.com/2006/04/metadata\"", qNameList);
							
							XdmValue labelssCount = extractNode("count(/CustomLabels/labels)", xmlExtractorBinding, qNameList, listBindingValuesFields);
							for (int nodeIdx = 0; nodeIdx < Integer.parseInt(labelssCount.toString()); nodeIdx++){
								
								LabelMetadata labelMetadata = new LabelMetadata();
								labelMetadata.objectName = labelFileName;
								listLabelMetadata.add(labelMetadata);
								
								listBindingValuesFields[0] = new XdmAtomicValue(nodeIdx+1);
				
								labelMetadata.fullName = extractNode("/CustomLabels/labels[$labelIdx]/fullName/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								labelMetadata.categories = extractNode("/CustomLabels/labels[$labelIdx]/categories/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								labelMetadata.language = extractNode("/CustomLabels/labels[$labelIdx]/language/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								labelMetadata.protectedFlag = extractNode("/CustomLabels/labels[$labelIdx]/protected/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								labelMetadata.shortDescription = extractNode("/CustomLabels/labels[$labelIdx]/shortDescription/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								labelMetadata.value = extractNode("/CustomLabels/labels[$labelIdx]/value/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								
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
					
				} else if(curDir.isDirectory() && curDir.getName().equals("objects")) {
					net.sf.saxon.s9api.QName[] qNameList = {new net.sf.saxon.s9api.QName("", "fieldsIdx"), new net.sf.saxon.s9api.QName("", "valueSetsIdx")};
					XdmItem[] listBindingValuesFields = {new XdmAtomicValue(0), new XdmAtomicValue(0)};
					
					for(File curObjectFile : curDir.listFiles()) {
						if(curObjectFile.isFile() && curObjectFile.getName().endsWith(".object")){
							
							String objectName = curObjectFile.getName().substring(0, curObjectFile.getName().length()-7);
							logger.debug(objectName);
							
							xmlExtractorBinding.init(curObjectFile, "xmlns=\"http://soap.sforce.com/2006/04/metadata\"", qNameList);
							
							XdmValue fieldsCount = extractNode("count(/CustomObject/fields)", xmlExtractorBinding, qNameList, listBindingValuesFields);
							for (int nodeIdx = 0; nodeIdx < Integer.parseInt(fieldsCount.toString()); nodeIdx++){
								
								FieldMetadata fieldMetadata = new FieldMetadata();
								fieldMetadata.objectName = objectName;
								listFieldMetadata.add(fieldMetadata);
								
								listBindingValuesFields[0] = new XdmAtomicValue(nodeIdx+1);
				
								fieldMetadata.fullName = extractNode("/CustomObject/fields[$fieldsIdx]/fullName/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.label = extractNode("/CustomObject/fields[$fieldsIdx]/label/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.type = extractNode("/CustomObject/fields[$fieldsIdx]/type/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.externalId = extractNode("/CustomObject/fields[$fieldsIdx]/externalId/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.required = extractNode("/CustomObject/fields[$fieldsIdx]/required/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.unique = extractNode("/CustomObject/fields[$fieldsIdx]/unique/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.deleteConstraint = extractNode("/CustomObject/fields[$fieldsIdx]/deleteConstraint/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.fieldManageability = extractNode("/CustomObject/fields[$fieldsIdx]/fieldManageability/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.referenceTo = extractNode("/CustomObject/fields[$fieldsIdx]/referenceTo/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.relationshipLabel = extractNode("/CustomObject/fields[$fieldsIdx]/relationshipLabel/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.relationshipName = extractNode("/CustomObject/fields[$fieldsIdx]/relationshipName/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.relationshipOrder = extractNode("/CustomObject/fields[$fieldsIdx]/relationshipOrder/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.reparentableMasterDetail = extractNode("/CustomObject/fields[$fieldsIdx]/reparentableMasterDetail/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.writeRequiresMasterRead = extractNode("/CustomObject/fields[$fieldsIdx]/writeRequiresMasterRead/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.valueSetRestricted = extractNode("/CustomObject/fields[$fieldsIdx]/valueSet/restricted/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								fieldMetadata.valueSetDefinitionSorted = extractNode("/CustomObject/fields[$fieldsIdx]/valueSet/valueSetDefinition/sorted/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								XdmValue valueSetCount= extractNode("count(/CustomObject/fields[$fieldsIdx]/valueSet/valueSetDefinition/value)", xmlExtractorBinding, qNameList, listBindingValuesFields);
								
								fieldMetadata.listValues = new ArrayList<FieldValueMetadata>();
								for (int valueSetsNodeIdx = 0; valueSetsNodeIdx < Integer.parseInt(valueSetCount.toString()); valueSetsNodeIdx++){
									listBindingValuesFields[1] = new XdmAtomicValue(valueSetsNodeIdx+1);
									FieldValueMetadata fieldValueMetadata = new FieldValueMetadata();
									fieldMetadata.listValues.add(fieldValueMetadata);
									fieldValueMetadata.idx = valueSetsNodeIdx;
									fieldValueMetadata.fullName = extractNode("/CustomObject/fields[$fieldsIdx]/valueSet/valueSetDefinition/value[$valueSetsIdx]/fullName/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
									fieldValueMetadata.defaultVs = extractNode("/CustomObject/fields[$fieldsIdx]/valueSet/valueSetDefinition/value[$valueSetsIdx]/default/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
									fieldValueMetadata.label = extractNode("/CustomObject/fields[$fieldsIdx]/valueSet/valueSetDefinition/value[$valueSetsIdx]/label/text()", xmlExtractorBinding, qNameList, listBindingValuesFields).toString();
								}
							}
						}
					}
				} else if(curDir.isDirectory() && curDir.getName().equals("triggers")) {
					
					for(File curTriggerFile : curDir.listFiles()) {
						if(curTriggerFile.isFile() && curTriggerFile.getName().endsWith(".trigger")){
							String triggerName = curTriggerFile.getName().substring(0, curTriggerFile.getName().length()-8);
							logger.debug(triggerName);
							listTriggers.add(triggerName);
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
			
			if(!listClasses.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "Classes";
				String columnName = "Class";
				xlsMng.createSheetList(sheetName, columnName, listClasses);
			}
			
			if(!listLabelMetadata.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				xlsMng.createSheetLabels(listLabelMetadata);
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
			
			if(!listFieldMetadata.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				xlsMng.createSheetFields(listFieldMetadata);
			}
			
			if(!listTriggers.isEmpty()) {
				if(xlsMng == null) {
					logger.info("Initialize the excel");
					xlsMng = new OrgXlsMgr(inExcelName);
				}
				String sheetName = "Triggers";
				String columnName = "Trigger";
				xlsMng.createSheetList(sheetName, columnName, listTriggers);
			}
			
		} catch(SaxonApiException | IndexOutOfBoundsException | SaxonApiUncheckedException e) {
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
