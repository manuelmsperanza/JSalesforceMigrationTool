package com.hoffnungland.jSFDCMigrTool;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.xml.sax.SAXException;

import com.hoffnungland.jAppKs.AppKeyStoreManager;
import com.hoffnungland.jAppKs.JksFilter;
import com.hoffnungland.jAppKs.PasswordPanel;
import com.salesforce.ant.DeployTask;
import com.salesforce.ant.RetrieveTask;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;

import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class App implements ActionListener {

	private static final Logger logger = LogManager.getLogger(App.class);
	private static String fileSeparator = System.getProperty("file.separator");

	private JFrame frame;
	private Properties jSalesforceMigrationToolProperties;
	private AppKeyStoreManager appKsManager;
	private JComboBox<String> sourceOrgComboBox;
	private JComboBox<String> targetOrgComboBox;
	private JComboBox<String> sourcePackagesComboBox;
	private JComboBox<String> targetPackagesComboBox;
	private JTextArea sourcePackageTextArea;
	private JTextArea targetPackageTextArea;
	private JLabel progressLabel;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger.traceEntry();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			logger.error(e);
		}



		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					Properties jSfMigrToolProperties = new Properties();
					File JSalesforceMigrationToolPropFile = new File("." + fileSeparator + "etc" + fileSeparator + "JSalesforceMigrationTool.properties");
					if(JSalesforceMigrationToolPropFile.exists()) {
						try (FileInputStream configFile = new FileInputStream(JSalesforceMigrationToolPropFile)) {
							jSfMigrToolProperties.load(configFile);
						}
					}

					String keyStorePath = null;
					if(jSfMigrToolProperties.containsKey("keyStorePath")) {
						keyStorePath = jSfMigrToolProperties.getProperty("keyStorePath");
					} else {

						JFileChooser fc = new JFileChooser();
						JksFilter fcJsonFiler = new JksFilter();
						fc.setMultiSelectionEnabled(false);
						fc.setFileFilter(fcJsonFiler);
						fc.addChoosableFileFilter(fcJsonFiler);
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

						int returnVal = fc.showOpenDialog(null);

						if(returnVal == JFileChooser.APPROVE_OPTION) {
							keyStorePath = fc.getSelectedFile().getPath();
							jSfMigrToolProperties.put("keyStorePath", keyStorePath);
							try (FileOutputStream configFile = new FileOutputStream(JSalesforceMigrationToolPropFile)) {
								jSfMigrToolProperties.store(configFile, null);
							}
						} else {
							logger.traceExit();
							return;
						}
					}

					String passwordKs = null;
					if(args.length > 0) {
						passwordKs = args[0];
					} else {
						PasswordPanel passwordPanel = new PasswordPanel();
						int option = JOptionPane.showOptionDialog(null, passwordPanel, "Vault password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

						if(option == JOptionPane.OK_OPTION) { // pressing OK button
							char[] passwd = passwordPanel.getPasswordField().getPassword();
							passwordKs = new String(passwd);
						} else {	
							logger.traceExit();
							return;
						}
					}

					App window = new App(keyStorePath, passwordKs);
					window.frame.setVisible(true);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
		logger.traceExit();
	}


	/**
	 * Create the application.
	 */
	public App(String keyStorePath, String passwordKs) {
		initialize(keyStorePath, passwordKs);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String keyStorePath, String passwordKs) {
		logger.traceEntry();


		this.appKsManager = new AppKeyStoreManager(keyStorePath, passwordKs);
		try {

			this.appKsManager.init();
			frame = new JFrame();
			frame.setResizable(false);
			frame.setBounds(new Rectangle(100, 100, 1010, 800));
			//frame.getContentPane().setMinimumSize(new Dimension(1000, 800));
			//frame.getContentPane().setMaximumSize(new Dimension(1000, 800));

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			SpringLayout springLayout = new SpringLayout();
			frame.getContentPane().setLayout(springLayout);

			JPanel commandPanelRetrieve = new JPanel();
			springLayout.putConstraint(SpringLayout.NORTH, commandPanelRetrieve, 0, SpringLayout.NORTH, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.SOUTH, commandPanelRetrieve, 550, SpringLayout.NORTH, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.EAST, commandPanelRetrieve, 500, SpringLayout.WEST, frame.getContentPane());
			commandPanelRetrieve.setMaximumSize(new Dimension(500, 200));
			springLayout.putConstraint(SpringLayout.WEST, commandPanelRetrieve, 0, SpringLayout.WEST, frame.getContentPane());
			frame.getContentPane().add(commandPanelRetrieve);
			SpringLayout sl_commandPanelRetrieve = new SpringLayout();
			commandPanelRetrieve.setLayout(sl_commandPanelRetrieve);

			JLabel lblSourceOrg = new JLabel("Source Org");
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, lblSourceOrg, 10, SpringLayout.NORTH, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, lblSourceOrg, 10, SpringLayout.WEST, commandPanelRetrieve);
			commandPanelRetrieve.add(lblSourceOrg);

			sourceOrgComboBox = new JComboBox<String>();
			sourceOrgComboBox.setActionCommand("sourceOrgComboBoxChanged");
			sourceOrgComboBox.addActionListener(this);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, sourceOrgComboBox, -4, SpringLayout.NORTH, lblSourceOrg);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, sourceOrgComboBox, 10, SpringLayout.EAST, lblSourceOrg);
			lblSourceOrg.setLabelFor(sourceOrgComboBox);
			commandPanelRetrieve.add(sourceOrgComboBox);

			JButton btnRetrieve = new JButton("Retrieve");
			btnRetrieve.addActionListener(this);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, btnRetrieve, -4, SpringLayout.NORTH, lblSourceOrg);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, btnRetrieve, -10, SpringLayout.EAST, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, sourceOrgComboBox, -10, SpringLayout.WEST, btnRetrieve);
			commandPanelRetrieve.add(btnRetrieve);

			sourcePackagesComboBox = new JComboBox<String>();
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, sourcePackagesComboBox, 10, SpringLayout.EAST, lblSourceOrg);
			sourcePackagesComboBox.setActionCommand("sourcePackageComboBoxChanged");
			sourcePackagesComboBox.addActionListener(this);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, sourcePackagesComboBox, 15, SpringLayout.SOUTH, lblSourceOrg);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, sourcePackagesComboBox, -10, SpringLayout.EAST, commandPanelRetrieve);
			commandPanelRetrieve.add(sourcePackagesComboBox);

			JScrollPane sourcePackageScrollPane = new JScrollPane();
			sl_commandPanelRetrieve.putConstraint(SpringLayout.SOUTH, sourcePackageScrollPane, 0, SpringLayout.SOUTH, commandPanelRetrieve);
			sourcePackageScrollPane.setAutoscrolls(true);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, sourcePackageScrollPane, 10, SpringLayout.WEST, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, sourcePackageScrollPane, -10, SpringLayout.EAST, commandPanelRetrieve);
			commandPanelRetrieve.add(sourcePackageScrollPane);

			sourcePackageTextArea = new JTextArea();
			sourcePackageTextArea.setEditable(false);
			sourcePackageScrollPane.setViewportView(sourcePackageTextArea);

			JPanel commandPanelDeploy = new JPanel();
			springLayout.putConstraint(SpringLayout.NORTH, commandPanelDeploy, 0, SpringLayout.NORTH, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.WEST, commandPanelDeploy, 0, SpringLayout.EAST, commandPanelRetrieve);
			springLayout.putConstraint(SpringLayout.SOUTH, commandPanelDeploy, 550, SpringLayout.NORTH, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.EAST, commandPanelDeploy, 500, SpringLayout.EAST, commandPanelRetrieve);
			frame.getContentPane().add(commandPanelDeploy);
			SpringLayout sl_commandPanelDeploy = new SpringLayout();
			commandPanelDeploy.setLayout(sl_commandPanelDeploy);

			JLabel lblTargetOrg = new JLabel("Target Org");
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, lblTargetOrg, 10, SpringLayout.NORTH, commandPanelDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, lblTargetOrg, 10, SpringLayout.WEST, commandPanelDeploy);
			commandPanelDeploy.add(lblTargetOrg);

			targetOrgComboBox = new JComboBox<String>();
			targetOrgComboBox.setActionCommand("targetOrgComboBoxChanged");
			targetOrgComboBox.addActionListener(this);
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, targetOrgComboBox, -4, SpringLayout.NORTH, lblTargetOrg);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, targetOrgComboBox, 10, SpringLayout.EAST, lblTargetOrg);
			commandPanelDeploy.add(targetOrgComboBox);
			lblTargetOrg.setLabelFor(targetOrgComboBox);

			JButton btnDeploy = new JButton("Deploy");
			btnDeploy.addActionListener(this);
			btnDeploy.setMinimumSize(new Dimension(73, 23));
			btnDeploy.setMaximumSize(new Dimension(73, 23));
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, btnDeploy, -4, SpringLayout.NORTH, lblTargetOrg);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, targetOrgComboBox, -10, SpringLayout.WEST, btnDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, btnDeploy, -10, SpringLayout.EAST, commandPanelDeploy);
			commandPanelDeploy.add(btnDeploy);

			targetPackagesComboBox = new JComboBox<String>();
			targetPackagesComboBox.setActionCommand("targetPackageComboBoxChanged");
			targetPackagesComboBox.addActionListener(this);
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, targetPackagesComboBox, 15, SpringLayout.SOUTH, lblTargetOrg);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, targetPackagesComboBox, -10, SpringLayout.EAST, commandPanelDeploy);
			commandPanelDeploy.add(targetPackagesComboBox);

			JScrollPane targetPackageScrollPane = new JScrollPane();
			sl_commandPanelDeploy.putConstraint(SpringLayout.SOUTH, targetPackageScrollPane, 0, SpringLayout.SOUTH, commandPanelDeploy);
			targetPackageScrollPane.setAutoscrolls(true);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, targetPackageScrollPane, 10, SpringLayout.WEST, commandPanelDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, targetPackageScrollPane, -10, SpringLayout.EAST, commandPanelDeploy);
			commandPanelDeploy.add(targetPackageScrollPane);

			targetPackageTextArea = new JTextArea();
			targetPackageTextArea.setEditable(false);
			targetPackageScrollPane.setViewportView(targetPackageTextArea);
			
			JLabel lblTargetPkg = new JLabel("Target Pkg");
			lblTargetPkg.setLabelFor(targetPackagesComboBox);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, targetPackagesComboBox, 10, SpringLayout.EAST, lblTargetPkg);
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, targetPackageScrollPane, 11, SpringLayout.SOUTH, lblTargetPkg);
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, lblTargetPkg, 4, SpringLayout.NORTH, targetPackagesComboBox);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, lblTargetPkg, 0, SpringLayout.WEST, lblTargetOrg);
			commandPanelDeploy.add(lblTargetPkg);

			progressLabel = new JLabel("New label");
			progressLabel.setVisible(false);
			springLayout.putConstraint(SpringLayout.WEST, progressLabel, 10, SpringLayout.WEST, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.SOUTH, progressLabel, -10, SpringLayout.SOUTH, frame.getContentPane());
			frame.getContentPane().add(progressLabel);
			
			JButton btnExcelMetadataButton = new JButton("Excel Org Info");
			springLayout.putConstraint(SpringLayout.NORTH, btnExcelMetadataButton, 10, SpringLayout.SOUTH, commandPanelRetrieve);
			springLayout.putConstraint(SpringLayout.WEST, btnExcelMetadataButton, 10, SpringLayout.WEST, commandPanelRetrieve);
			btnExcelMetadataButton.addActionListener(this);
			frame.getContentPane().add(btnExcelMetadataButton);
			
			JButton btnClearMetadata = new JButton("Clear Metadata");
			springLayout.putConstraint(SpringLayout.SOUTH, btnClearMetadata, 0, SpringLayout.SOUTH, btnExcelMetadataButton);
			springLayout.putConstraint(SpringLayout.WEST, btnClearMetadata, 10, SpringLayout.EAST, btnExcelMetadataButton);
			btnClearMetadata.addActionListener(this);
			frame.getContentPane().add(btnClearMetadata);

			JButton btnUtilityAppMaMeS = new JButton("Utility App MaMeS");
			springLayout.putConstraint(SpringLayout.NORTH, btnUtilityAppMaMeS, 10, SpringLayout.SOUTH, btnExcelMetadataButton);
			springLayout.putConstraint(SpringLayout.WEST, btnUtilityAppMaMeS, 0, SpringLayout.WEST, btnExcelMetadataButton);
			btnUtilityAppMaMeS.addActionListener(this);
			
			JLabel lblSourcePkg = new JLabel("Source Pkg");
			lblSourcePkg.setLabelFor(sourcePackagesComboBox);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, sourcePackageScrollPane, 11, SpringLayout.SOUTH, lblSourcePkg);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, lblSourcePkg, 4, SpringLayout.NORTH, sourcePackagesComboBox);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, lblSourcePkg, 0, SpringLayout.WEST, lblSourceOrg);
			commandPanelRetrieve.add(lblSourcePkg);
			frame.getContentPane().add(btnUtilityAppMaMeS);

			JButton btnProcessClickMaMeSButton = new JButton("Process Click MaMeS");
			springLayout.putConstraint(SpringLayout.SOUTH, btnProcessClickMaMeSButton, 0, SpringLayout.SOUTH, btnUtilityAppMaMeS);
			springLayout.putConstraint(SpringLayout.WEST, btnProcessClickMaMeSButton, 10, SpringLayout.EAST, btnUtilityAppMaMeS);
			btnProcessClickMaMeSButton.addActionListener(this);
			frame.getContentPane().add(btnProcessClickMaMeSButton);
			
			JButton btnBlockAutomation = new JButton("Block Automations");
			springLayout.putConstraint(SpringLayout.NORTH, btnBlockAutomation, 10, SpringLayout.SOUTH, btnUtilityAppMaMeS);
			springLayout.putConstraint(SpringLayout.WEST, btnBlockAutomation, 0, SpringLayout.WEST, progressLabel);
			btnBlockAutomation.addActionListener(this);
			frame.getContentPane().add(btnBlockAutomation);
			
			JButton btnUnblockAutomation = new JButton("Unblock Automations");
			springLayout.putConstraint(SpringLayout.WEST, btnUnblockAutomation, 10, SpringLayout.EAST, btnBlockAutomation);
			springLayout.putConstraint(SpringLayout.SOUTH, btnUnblockAutomation, 0, SpringLayout.SOUTH, btnBlockAutomation);
			btnUnblockAutomation.addActionListener(this);
			frame.getContentPane().add(btnUnblockAutomation);

			

			this.jSalesforceMigrationToolProperties = new Properties();
			File JSalesforceMigrationToolPropFile = new File("." + fileSeparator + "etc" + fileSeparator + "JSalesforceMigrationTool.properties");
			if(JSalesforceMigrationToolPropFile.exists()) {
				try (FileInputStream configFile = new FileInputStream(JSalesforceMigrationToolPropFile)) {
					jSalesforceMigrationToolProperties.load(configFile);
				}
			}

			this.loadProperties();

		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | InvalidKeySpecException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
		logger.traceExit();
	}

	private void loadProperties() throws FileNotFoundException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException, CertificateException {

		logger.traceEntry();
		String selectedSourceOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedSourceOrg");
		String selectedTargetOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedTargetOrg");

		File orgPropertiesDir = new File("." + fileSeparator + "etc" + fileSeparator + "orgs");
		for (File curPropFile : orgPropertiesDir.listFiles()) {
			if(curPropFile.isFile() && curPropFile.getName().endsWith(".properties")) {

				logger.debug("Loading " + curPropFile.getName());

				String orgName = curPropFile.getName().substring(0, curPropFile.getName().indexOf(".properties"));

				Properties orgProperties = new Properties();
				try (FileInputStream configFile = new FileInputStream(curPropFile)) {
					orgProperties.load(configFile);
				}

				String passwordType = orgProperties.getProperty("passwordType");

				if(StringUtils.isBlank(passwordType) || "encrypt".equals(passwordType)) {

					String password = this.appKsManager.writePasswordToKeyStore(orgName + ".password", orgProperties.getProperty("sf.password"));

					orgProperties.setProperty("passwordType", "encrypted");
					orgProperties.setProperty("sf.password", password);

					try(FileOutputStream out = new FileOutputStream(curPropFile)) {			
						orgProperties.store(out, null);
					}

				}

				this.sourceOrgComboBox.addItem(orgName);
				this.targetOrgComboBox.addItem(orgName);


			}
		}

		if(selectedSourceOrg != null) {
			this.sourceOrgComboBox.setSelectedItem(selectedSourceOrg);
		}

		if(selectedTargetOrg != null) {
			this.targetOrgComboBox.setSelectedItem(selectedTargetOrg);
		}


		String selectedSourcePackage = this.jSalesforceMigrationToolProperties.getProperty("selectedSourcePackage");
		String selectedTargetPackage = this.jSalesforceMigrationToolProperties.getProperty("selectedTargetPackage");
		File packagePropertiesDir = new File("." + fileSeparator + "etc" + fileSeparator + "packages");
		for (File curPackageFile : packagePropertiesDir.listFiles()) {
			if(curPackageFile.isFile() && curPackageFile.getName().endsWith(".xml")) {

				logger.debug("Loading " + curPackageFile.getName());

				this.sourcePackagesComboBox.addItem(curPackageFile.getName());
				this.targetPackagesComboBox.addItem(curPackageFile.getName());

			}
		}

		if(selectedSourcePackage != null) {
			this.sourcePackagesComboBox.setSelectedItem(selectedSourcePackage);
		}

		if(selectedTargetPackage != null) {
			this.targetPackagesComboBox.setSelectedItem(selectedTargetPackage);
		}

		logger.traceExit();

	}

	private void saveJSfMigrToolProperties() throws FileNotFoundException, IOException {
		logger.traceEntry();

		File JSalesforceMigrationToolPropFile = new File("." + fileSeparator + "etc" + fileSeparator + "JSalesforceMigrationTool.properties");
		try (FileOutputStream configFile = new FileOutputStream(JSalesforceMigrationToolPropFile)) {
			this.jSalesforceMigrationToolProperties.store(configFile, null);
		}
		logger.traceExit();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		logger.traceEntry();
		logger.debug(event.getActionCommand());
		try {
			switch (event.getActionCommand()) {
			case "sourceOrgComboBoxChanged":
				String selectedSourceOrg = (String) this.sourceOrgComboBox.getSelectedItem();
				this.jSalesforceMigrationToolProperties.put("selectedSourceOrg", selectedSourceOrg);
				this.saveJSfMigrToolProperties();
				break;
			case "sourcePackageComboBoxChanged":
				String selectedSourcePackage = (String) this.sourcePackagesComboBox.getSelectedItem();
				this.showPackage(selectedSourcePackage, this.sourcePackageTextArea);
				this.jSalesforceMigrationToolProperties.put("selectedSourcePackage", selectedSourcePackage);
				this.saveJSfMigrToolProperties();
				break;
			case "targetOrgComboBoxChanged":
				String selectedTargetOrg = (String) this.targetOrgComboBox.getSelectedItem();
				this.jSalesforceMigrationToolProperties.put("selectedTargetOrg", selectedTargetOrg);
				this.saveJSfMigrToolProperties();
				break;
			case "targetPackageComboBoxChanged":
				String selectedTargetPackage = (String) this.targetPackagesComboBox.getSelectedItem();
				this.showPackage(selectedTargetPackage, this.targetPackageTextArea);
				this.jSalesforceMigrationToolProperties.put("selectedTargetPackage", selectedTargetPackage);
				this.saveJSfMigrToolProperties();
				break;
			case "Retrieve":
				this.retrieveAction();
				break;
			case "Deploy":
				this.deployAction();
				break;
			case "Utility App MaMeS":
				new EnergyAppMaMeS().executeChanges();
				JOptionPane.showMessageDialog(this.frame, "Metadata update is completed", "Update completed", JOptionPane.INFORMATION_MESSAGE);
				break;
			case "Process Click MaMeS":
				new ProcessClickMaMeS().executeChanges();
				JOptionPane.showMessageDialog(this.frame, "Metadata update is completed", "Update completed", JOptionPane.INFORMATION_MESSAGE);
				break;
			case "Excel Org Info":
				String sourceOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedSourceOrg");
				new OrgMetadataToExcel().generateExcel(sourceOrg);
				this.postActionClearMetadataDir("Excel ready");
				break;
			case "Clear Metadata":
				this.postActionClearMetadataDir("Clear Metadata Directory");
				break;
			case "Block Automations":
				this.startAutomationManager(false);
				break;
			case "Unblock Automations":
				this.startAutomationManager(true);
				break;
			}

		} catch (IOException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidKeySpecException | IndexOutOfBoundsException | SaxonApiUncheckedException | SaxonApiException | SAXException | ParserConfigurationException | TransformerException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		} catch (BuildException e) {
			logger.fatal(e);
			this.showErrorList(e.getMessage());
		}finally {
			this.progressLabel.setVisible(false);
		}
		logger.traceExit();
	}

	private void startAutomationManager(boolean enable) throws FileNotFoundException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException, IndexOutOfBoundsException, SaxonApiUncheckedException, SaxonApiException, SAXException, ParserConfigurationException, TransformerException {
		
		logger.traceEntry();
		this.progressLabel.setVisible(true);
		this.progressLabel.setText("Retrieve metadata in progress");
		String selectedSourceOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedSourceOrg");
		Properties sourceOrgProperties = new Properties();
		File sourceOrgPropertiesFile = new File("." + fileSeparator + "etc" + fileSeparator + "orgs" + fileSeparator + selectedSourceOrg + ".properties");
		try (FileInputStream configFile = new FileInputStream(sourceOrgPropertiesFile)) {
			sourceOrgProperties.load(configFile);
		}

		String passwordType = sourceOrgProperties.getProperty("passwordType");
		String passwd = null;
		switch(passwordType) {
		case "encrypted":
			String encriptedPassword = sourceOrgProperties.getProperty("sf.password");
			passwd = this.appKsManager.readPasswordFromKeyStore(selectedSourceOrg + ".password", encriptedPassword);
			break;
		case "oneTimePassword":
			PasswordPanel passwordPanel = new PasswordPanel();
			int option = JOptionPane.showOptionDialog(this.frame, passwordPanel, selectedSourceOrg, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if(option == JOptionPane.OK_OPTION) { // pressing OK button
				char[] passwdChr = passwordPanel.getPasswordField().getPassword();
				passwd = new String(passwdChr);
			} else {
				logger.traceExit();
				return;
			}
			break;
		}

		
		
		String username = sourceOrgProperties.getProperty("sf.username");
		String sourcePackageName = this.jSalesforceMigrationToolProperties.getProperty("selectedSourcePackage");
		String targetPackageName = this.jSalesforceMigrationToolProperties.getProperty("selectedTargetPackage");
		
		AutomationManager automationManager = new AutomationManager(
				username,
				passwd,
				sourceOrgProperties.getProperty("sf.serverurl"),
				"." + fileSeparator,
				"." + fileSeparator + "etc" + fileSeparator + "packages"  + fileSeparator + sourcePackageName,
				"." + fileSeparator + "etc" + fileSeparator + "packages"  + fileSeparator + targetPackageName);
		
		if(automationManager.checkEnablement(enable)) {			
			automationManager.retrieveAutomationConfiguration();
			automationManager.changeAutomations(enable);
			automationManager.deployAutomationConfiguration();
		}
		
		JOptionPane.showMessageDialog(this.frame, "Automation " + (enable ? "unblock" : "block") + " is completed", "Automantion switch completed", JOptionPane.INFORMATION_MESSAGE);
		
	}


	private void showErrorList(String message) {
		JScrollPane scrollpane = new JScrollPane(); 

		JTextArea textArea = new JTextArea(message);
		textArea.setRows(20);
		textArea.setColumns(60);
		JPanel panel = new JPanel(); 
		panel.add(scrollpane);
		scrollpane.setViewportView(textArea);
		JOptionPane.showMessageDialog(this.frame, scrollpane, "Error List", JOptionPane.ERROR_MESSAGE);
	}

	private void showPackage(String packageName, JTextArea targetTextArea) throws IOException {

		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines( Paths.get("." + fileSeparator + "etc" + fileSeparator + "packages"  + fileSeparator + packageName), StandardCharsets.UTF_8)) 
		{
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		}

		targetTextArea.setText(contentBuilder.toString());		
	}

	private void retrieveAction() throws IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException {

		logger.traceEntry();
		this.progressLabel.setVisible(true);
		this.progressLabel.setText("Retrieve metadata in progress");
		String selectedSourceOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedSourceOrg");
		Properties sourceOrgProperties = new Properties();
		File sourceOrgPropertiesFile = new File("." + fileSeparator + "etc" + fileSeparator + "orgs" + fileSeparator + selectedSourceOrg + ".properties");
		try (FileInputStream configFile = new FileInputStream(sourceOrgPropertiesFile)) {
			sourceOrgProperties.load(configFile);
		}

		String passwordType = sourceOrgProperties.getProperty("passwordType");
		String passwd = null;
		switch(passwordType) {
		case "encrypted":
			String encriptedPassword = sourceOrgProperties.getProperty("sf.password");
			passwd = this.appKsManager.readPasswordFromKeyStore(selectedSourceOrg + ".password", encriptedPassword);
			break;
		case "oneTimePassword":
			PasswordPanel passwordPanel = new PasswordPanel();
			int option = JOptionPane.showOptionDialog(this.frame, passwordPanel, selectedSourceOrg, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if(option == JOptionPane.OK_OPTION) { // pressing OK button
				char[] passwdChr = passwordPanel.getPasswordField().getPassword();
				passwd = new String(passwdChr);
			} else {
				logger.traceExit();
				return;
			}
			break;
		}

		RetrieveTask retrieveTask = new RetrieveTask();
		Project retrieveTaskProject = new Project();
		retrieveTaskProject.setBasedir(".");
		retrieveTask.setProject(retrieveTaskProject);
		String username = sourceOrgProperties.getProperty("sf.username");
		retrieveTask.setUsername(username);
		retrieveTask.setPassword(passwd);
		if(sourceOrgProperties.containsKey("sf.serverurl")) {
			retrieveTask.setServerURL(sourceOrgProperties.getProperty("sf.serverurl"));
		}
		retrieveTask.setTaskName("retrieveUnpackaged");
		String targetDir = "retrieveUnpackaged";

		Path targetDirPath = Paths.get(targetDir);

		if(!Files.exists(targetDirPath)) {
			Files.createDirectory(targetDirPath).toFile();				
		}

		retrieveTask.setRetrieveTarget(targetDir);
		String sourcePackageName = this.jSalesforceMigrationToolProperties.getProperty("selectedSourcePackage");
		retrieveTask.setUnpackaged("." + fileSeparator + "etc" + fileSeparator + "packages"  + fileSeparator + sourcePackageName);
		retrieveTask.setTrace(logger.isTraceEnabled());
		logger.info("Run retrieve task");
		retrieveTask.execute();
		logger.info("Retrieve task done");

		JOptionPane.showMessageDialog(this.frame, "Metadata retrieve is completed", "Retrieve completed", JOptionPane.INFORMATION_MESSAGE);

	}

	private void deployAction() throws IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException {

		logger.traceEntry();
		this.progressLabel.setVisible(true);
		this.progressLabel.setText("Deploy metadata in progress");
		String selectedTargetOrg = this.jSalesforceMigrationToolProperties.getProperty("selectedTargetOrg");
		Properties targetOrgProperties = new Properties();
		File sourceOrgPropertiesFile = new File("." + fileSeparator + "etc" + fileSeparator + "orgs" + fileSeparator + selectedTargetOrg + ".properties");
		try (FileInputStream configFile = new FileInputStream(sourceOrgPropertiesFile)) {
			targetOrgProperties.load(configFile);
		}

		String passwordType = targetOrgProperties.getProperty("passwordType");
		String passwd = null;
		switch(passwordType) {
		case "encrypted":
			String encriptedPassword = targetOrgProperties.getProperty("sf.password");
			passwd = this.appKsManager.readPasswordFromKeyStore(selectedTargetOrg + ".password", encriptedPassword);
			break;
		case "oneTimePassword":
			PasswordPanel passwordPanel = new PasswordPanel();
			int option = JOptionPane.showOptionDialog(this.frame, passwordPanel, selectedTargetOrg, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			if(option == JOptionPane.OK_OPTION) { // pressing OK button
				char[] passwdChr = passwordPanel.getPasswordField().getPassword();
				passwd = new String(passwdChr);
			} else {
				logger.traceExit();
				return;
			}
			break;
		}

		DeployTask deployTask = new DeployTask();

		Project deployTaskProject = new Project();
		deployTaskProject.setBasedir(".");
		deployTask.setProject(deployTaskProject);
		String username = targetOrgProperties.getProperty("sf.username");
		deployTask.setUsername(username);
		deployTask.setPassword(passwd);
		if(targetOrgProperties.containsKey("sf.serverurl")) {
			deployTask.setServerURL(targetOrgProperties.getProperty("sf.serverurl"));
		}
		deployTask.setTaskName("deployUnpackaged");

		String targetDir = "retrieveUnpackaged";
		deployTask.setDeployRoot(targetDir);

		String targetPackageName = this.jSalesforceMigrationToolProperties.getProperty("selectedTargetPackage");

		Files.copy(Paths.get("." + fileSeparator + "etc" + fileSeparator + "packages"  + fileSeparator + targetPackageName), Paths.get("." + fileSeparator + "retrieveUnpackaged" + fileSeparator + "package.xml"), StandardCopyOption.REPLACE_EXISTING);

		deployTask.setTrace(logger.isTraceEnabled());
		logger.info("Run deploy task");
		deployTask.execute();		
		logger.info("Deploy task done");
		
		this.postActionClearMetadataDir("Deploy completed");
		logger.traceExit();
	}
		
	private void postActionClearMetadataDir(String title) throws IOException {
		logger.traceEntry();
		String targetDir = "retrieveUnpackaged";
		int option = JOptionPane.showConfirmDialog(this.frame, "Would you like to remove " + targetDir + " directory?", title, JOptionPane.YES_NO_OPTION);

		if(option == JOptionPane.YES_OPTION) {			
			Path targetDirPath = Paths.get(targetDir);
			File targetDirFile = targetDirPath.toFile();
			FileUtils.deleteDirectory(targetDirFile);
		}
		logger.traceExit();
	}
}
