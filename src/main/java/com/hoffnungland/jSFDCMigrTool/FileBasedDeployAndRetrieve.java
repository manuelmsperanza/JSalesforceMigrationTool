package com.hoffnungland.jSFDCMigrTool;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.rmi.RemoteException;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.sforce.soap.metadata.*;
import com.sforce.ws.ConnectionException;

/**
 * Sample that logs in and shows a menu of retrieve and deploy metadata options.
 */
public class FileBasedDeployAndRetrieve {
	private static final Logger logger = LogManager.getLogger(FileBasedDeployAndRetrieve.class);
	private MetadataConnection metadataConnection;

	private static final String ZIP_FILE = "components.zip";

	// manifest file that controls which components get retrieved
	private static final String MANIFEST_FILE = "package.xml";

	private static final double API_VERSION = 29.0;

	// one second in milliseconds
	private static final long ONE_SECOND = 1000;

	// maximum number of attempts to deploy the zip file
	private static final int MAX_NUM_POLL_REQUESTS = 50;

	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String[] args) throws Exception {
		FileBasedDeployAndRetrieve sample = new FileBasedDeployAndRetrieve();
		sample.run();
	}

	public FileBasedDeployAndRetrieve() {
	}

	private void run() throws Exception {
		this.metadataConnection = MetadataLoginUtil.login();

		// Show the options to retrieve or deploy until user exits
		String choice = getUsersChoice();
		while (choice != null && !choice.equals("99")) {
			if (choice.equals("1")) {
				retrieveZip();
			} else if (choice.equals("2")) {
				deployZip();
			} else {
				this.listMetadata(choice);
			}
			// show the options again
			choice = getUsersChoice();
		}
	}

	/*
	 * Utility method to present options to retrieve or deploy.
	 */
	private String getUsersChoice() throws IOException {
		System.out.println(" 1: Retrieve");
		System.out.println(" 2: Deploy");
		System.out.println("99: Exit");
		System.out.println();
		System.out.print("Enter 1 to retrieve, 2 to deploy, or 99 to exit: ");
		// wait for the user input.
		String choice = reader.readLine();
		return choice != null ? choice.trim() : "";
	}

	private void deployZip() throws Exception {
		byte zipBytes[] = readZipFile();
		DeployOptions deployOptions = new DeployOptions();
		deployOptions.setPerformRetrieve(false);
		deployOptions.setRollbackOnError(true);
		AsyncResult asyncResult = metadataConnection.deploy(zipBytes, deployOptions);
		DeployResult result = waitForDeployCompletion(asyncResult.getId());
		if (!result.isSuccess()) {
			printErrors(result, "Final list of failures:\n");
			throw new Exception("The files were not successfully deployed");
		}
		System.out.println("The file " + ZIP_FILE + " was successfully deployed\n");
	}

	/*
	 * Read the zip file contents into a byte array.
	 */
	private byte[] readZipFile() throws Exception {
		byte[] result = null;
		// We assume here that you have a deploy.zip file.
		// See the retrieve sample for how to retrieve a zip file.
		File zipFile = new File(ZIP_FILE);
		if (!zipFile.exists() || !zipFile.isFile()) {
			throw new Exception("Cannot find the zip file for deploy() on path:"
					+ zipFile.getAbsolutePath());
		}

		FileInputStream fileInputStream = new FileInputStream(zipFile);
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead = 0;
			while (-1 != (bytesRead = fileInputStream.read(buffer))) {
				bos.write(buffer, 0, bytesRead);
			}

			result = bos.toByteArray();
		} finally {
			fileInputStream.close();
		}
		return result;
	}

	/*
	 * Print out any errors, if any, related to the deploy.
	 * @param result - DeployResult
	 */
	private void printErrors(DeployResult result, String messageHeader) {
		DeployDetails details = result.getDetails();
		StringBuilder stringBuilder = new StringBuilder();
		if (details != null) {
			DeployMessage[] componentFailures = details.getComponentFailures();
			for (DeployMessage failure : componentFailures) {
				String loc = "(" + failure.getLineNumber() + ", " + failure.getColumnNumber();
				if (loc.length() == 0 && !failure.getFileName().equals(failure.getFullName()))
				{
					loc = "(" + failure.getFullName() + ")";
				}
				stringBuilder.append(failure.getFileName() + loc + ":" 
						+ failure.getProblem()).append('\n');
			}
			RunTestsResult rtr = details.getRunTestResult();
			if (rtr.getFailures() != null) {
				for (RunTestFailure failure : rtr.getFailures()) {
					String n = (failure.getNamespace() == null ? "" :
						(failure.getNamespace() + ".")) + failure.getName();
					stringBuilder.append("Test failure, method: " + n + "." +
							failure.getMethodName() + " -- " + failure.getMessage() + 
							" stack " + failure.getStackTrace() + "\n\n");
				}
			}
			if (rtr.getCodeCoverageWarnings() != null) {
				for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
					stringBuilder.append("Code coverage issue");
					if (ccw.getName() != null) {
						String n = (ccw.getNamespace() == null ? "" :
							(ccw.getNamespace() + ".")) + ccw.getName();
						stringBuilder.append(", class: " + n);
					}
					stringBuilder.append(" -- " + ccw.getMessage() + "\n");
				}
			}
		}
		if (stringBuilder.length() > 0) {
			stringBuilder.insert(0, messageHeader);
			System.out.println(stringBuilder.toString());
		}
	}


	private void retrieveZip() throws Exception {
		RetrieveRequest retrieveRequest = new RetrieveRequest();
		// The version in package.xml overrides the version in RetrieveRequest
		retrieveRequest.setApiVersion(API_VERSION);
		setUnpackaged(retrieveRequest);

		AsyncResult asyncResult = metadataConnection.retrieve(retrieveRequest);
		RetrieveResult result = waitForRetrieveCompletion(asyncResult);

		if (result.getStatus() == RetrieveStatus.Failed) {
			throw new Exception(result.getErrorStatusCode() + " msg: " +
					result.getErrorMessage());
		} else if (result.getStatus() == RetrieveStatus.Succeeded) {  
			// Print out any warning messages
			StringBuilder stringBuilder = new StringBuilder();
			if (result.getMessages() != null) {
				for (RetrieveMessage rm : result.getMessages()) {
					stringBuilder.append(rm.getFileName() + " - " + rm.getProblem() + "\n");
				}
			}
			if (stringBuilder.length() > 0) {
				System.out.println("Retrieve warnings:\n" + stringBuilder);
			}

			System.out.println("Writing results to zip file");
			File resultsFile = new File(ZIP_FILE);
			FileOutputStream os = new FileOutputStream(resultsFile);

			try {
				os.write(result.getZipFile());
			} finally {
				os.close();
			}
		}
	}

	private DeployResult waitForDeployCompletion(String asyncResultId) throws Exception {
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		DeployResult deployResult;
		boolean fetchDetails;
		do {
			Thread.sleep(waitTimeMilliSecs);
			// double the wait time for the next iteration

			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new Exception(
						"Request timed out. If this is a large set of metadata components, " +
						"ensure that MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			// Fetch in-progress details once for every 3 polls
			fetchDetails = (poll % 3 == 0);

			deployResult = metadataConnection.checkDeployStatus(asyncResultId, fetchDetails);
			System.out.println("Status is: " + deployResult.getStatus());
			if (!deployResult.isDone() && fetchDetails) {
				printErrors(deployResult, "Failures for deployment in progress:\n");
			}
		}
		while (!deployResult.isDone());

		if (!deployResult.isSuccess() && deployResult.getErrorStatusCode() != null) {
			throw new Exception(deployResult.getErrorStatusCode() + " msg: " +
					deployResult.getErrorMessage());
		}

		if (!fetchDetails) {
			// Get the final result with details if we didn't do it in the last attempt.
			deployResult = metadataConnection.checkDeployStatus(asyncResultId, true);
		}

		return deployResult;
	}

	private RetrieveResult waitForRetrieveCompletion(AsyncResult asyncResult) throws Exception {
		// Wait for the retrieve to complete
		int poll = 0;
		long waitTimeMilliSecs = ONE_SECOND;
		String asyncResultId = asyncResult.getId();
		RetrieveResult result = null;
		do {
			Thread.sleep(waitTimeMilliSecs);
			// Double the wait time for the next iteration
			waitTimeMilliSecs *= 2;
			if (poll++ > MAX_NUM_POLL_REQUESTS) {
				throw new Exception("Request timed out.  If this is a large set " +
						"of metadata components, check that the time allowed " +
						"by MAX_NUM_POLL_REQUESTS is sufficient.");
			}
			result = metadataConnection.checkRetrieveStatus(
					asyncResultId, true);
			System.out.println("Retrieve Status: " + result.getStatus());
		} while (!result.isDone());         

		return result;
	}

	private void setUnpackaged(RetrieveRequest request) throws Exception {
		// Edit the path, if necessary, if your package.xml file is located elsewhere
		File unpackedManifest = new File(MANIFEST_FILE);
		System.out.println("Manifest file: " + unpackedManifest.getAbsolutePath());

		if (!unpackedManifest.exists() || !unpackedManifest.isFile()) {
			throw new Exception("Should provide a valid retrieve manifest " +
					"for unpackaged content. Looking for " +
					unpackedManifest.getAbsolutePath());
		}

		// Note that we use the fully quualified class name because
		// of a collision with the java.lang.Package class
		com.sforce.soap.metadata.Package p = parsePackageManifest(unpackedManifest);
		request.setUnpackaged(p);
	}

	private com.sforce.soap.metadata.Package parsePackageManifest(File file)
			throws ParserConfigurationException, IOException, SAXException {
		com.sforce.soap.metadata.Package packageManifest = null;
		List<PackageTypeMembers> listPackageTypes = new ArrayList<PackageTypeMembers>();
		DocumentBuilder db =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
		InputStream inputStream = new FileInputStream(file);
		Element d = db.parse(inputStream).getDocumentElement();
		for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element) {
				Element ce = (Element) c;
				NodeList nodeList = ce.getElementsByTagName("name");
				if (nodeList.getLength() == 0) {
					continue;
				}
				String name = nodeList.item(0).getTextContent();
				NodeList m = ce.getElementsByTagName("members");
				List<String> members = new ArrayList<String>();
				for (int i = 0; i < m.getLength(); i++) {
					Node mm = m.item(i);
					members.add(mm.getTextContent());
				}
				PackageTypeMembers packageTypes = new PackageTypeMembers();
				packageTypes.setName(name);
				packageTypes.setMembers(members.toArray(new String[members.size()]));
				listPackageTypes.add(packageTypes);
			}
		}
		packageManifest = new com.sforce.soap.metadata.Package();
		PackageTypeMembers[] packageTypesArray =
				new PackageTypeMembers[listPackageTypes.size()];
		packageManifest.setTypes(listPackageTypes.toArray(packageTypesArray));
		packageManifest.setVersion(API_VERSION + "");
		return packageManifest;
	}
	
	public void listMetadata(String metadataType) {
		logger.traceEntry();
		try {
			ListMetadataQuery query = new ListMetadataQuery();
			query.setType(metadataType);
			//query.setFolder(null);
			double asOfVersion = 52.0;
			// Assuming that the SOAP binding has already been established.
			FileProperties[] lmr = metadataConnection.listMetadata(new ListMetadataQuery[] {query}, asOfVersion);
			int metadataFileListLength = 1; //(lmr.length > 10) ? 10 : lmr.length;
			String metadataFileList[] =  new String[metadataFileListLength];
			if (lmr != null) {
				//int fileidx = 0;
				for (FileProperties n : lmr) {
					System.out.println("Component fullName: " + n.getFullName());
					System.out.println("Component type: " + n.getType());
					metadataFileList[0] = n.getFullName();
					this.readMetadataSync(metadataType, metadataFileList);
					/*metadataFileList[fileidx%10] = n.getFullName();
					fileidx++;
					if(fileidx%10 == 0) {
						this.readMetadataSync(metadataType, metadataFileList);
						metadataFileListLength = (lmr.length - fileidx > 10) ? 10 : lmr.length - fileidx;
						metadataFileList =  new String[metadataFileListLength];
					}*/
				}
			}             
		} catch (ConnectionException ce) {
			ce.printStackTrace();
			logger.error(ce);
		}
		logger.traceExit();
	}
	
	public void readMetadataSync(String metadataType, String metadataFileList[]) {
		logger.traceEntry();
		try {

			ReadResult readResult = metadataConnection.readMetadata(metadataType, metadataFileList);
			Metadata[] mdInfo = readResult.getRecords();
			System.out.println("Number of component info returned: "
					+ mdInfo.length);
			for (Metadata md : mdInfo) {
				if (md != null) {
					System.out.println(md.getClass() + ": " + md.toString());
					
					
					
					/*CustomObject customObj = (CustomObject) md;
                    System.out.println("Custom object full name: "
                            + customObj.getFullName());
                    System.out.println("Label: " + customObj.getLabel());
                    System.out.println("Number of custom fields: "
                            + customObj.getFields().length);
                    System.out.println("Sharing model: "
                            + customObj.getSharingModel());*/
				} else {
					System.out.println("Empty metadata.");
				}
			}
		} catch (ConnectionException ce) {
			ce.printStackTrace();
			logger.error(ce);
		}
		logger.traceExit();
	}

}
