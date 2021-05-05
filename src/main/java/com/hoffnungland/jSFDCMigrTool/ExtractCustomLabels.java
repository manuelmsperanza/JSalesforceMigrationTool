package com.hoffnungland.jSFDCMigrTool;

import java.io.IOException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import com.hoffnungland.xpath.XmlExtractor;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;

public class ExtractCustomLabels {
	
	private static final Logger logger = LogManager.getLogger(ExtractCustomLabels.class);
	
	public static void main(String[] args) {
		logger.traceEntry();
		XmlExtractor xmlExtractor = new XmlExtractor();
		String customrProfileFilePath = "C:\\Users\\msperanza\\git\\gaxa-gas\\force-app\\main\\default\\labels\\CustomLabels.labels-meta.xml";
		try {
			xmlExtractor.init(Paths.get(customrProfileFilePath).toFile());
			xmlExtractor.extractNode("//labels/concat(fullName, '\t', categories, '\t', language, '\t', protected, '\t', shortDescription, '\t', value)", "xmlns=\"http://soap.sforce.com/2006/04/metadata\"");
		} catch (SaxonApiException | IndexOutOfBoundsException | SaxonApiUncheckedException | SAXException | IOException | ParserConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
			
		logger.traceExit();

	}

}
