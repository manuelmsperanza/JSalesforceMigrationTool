package com.hoffnungland.jSFDCMigrTool;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.Project;

import com.hoffnungland.jAppKs.AppKeyStoreManager;
import com.hoffnungland.jAppKs.JksFilter;
import com.hoffnungland.jAppKs.PasswordPanel;
import com.salesforce.ant.DeployTask;
import com.salesforce.ant.RetrieveTask;

import javax.swing.JLabel;
import javax.swing.SpringLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JTree;
import java.awt.Rectangle;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
	private JTextField textField;
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
			springLayout.putConstraint(SpringLayout.SOUTH, commandPanelRetrieve, 0, SpringLayout.SOUTH, frame.getContentPane());
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
			sourcePackagesComboBox.setActionCommand("sourcePackageComboBoxChanged");
			sourcePackagesComboBox.addActionListener(this);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, sourcePackagesComboBox, 15, SpringLayout.SOUTH, lblSourceOrg);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, sourcePackagesComboBox, 10, SpringLayout.WEST, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, sourcePackagesComboBox, -10, SpringLayout.EAST, commandPanelRetrieve);
			commandPanelRetrieve.add(sourcePackagesComboBox);
			
			JScrollPane sourcePackageScrollPane = new JScrollPane();
			sourcePackageScrollPane.setAutoscrolls(true);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.NORTH, sourcePackageScrollPane, 15, SpringLayout.SOUTH, sourcePackagesComboBox);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.WEST, sourcePackageScrollPane, 10, SpringLayout.WEST, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.SOUTH, sourcePackageScrollPane, 550, SpringLayout.NORTH, commandPanelRetrieve);
			sl_commandPanelRetrieve.putConstraint(SpringLayout.EAST, sourcePackageScrollPane, -10, SpringLayout.EAST, commandPanelRetrieve);
			commandPanelRetrieve.add(sourcePackageScrollPane);
			
			sourcePackageTextArea = new JTextArea();
			sourcePackageTextArea.setEditable(false);
			sourcePackageScrollPane.setViewportView(sourcePackageTextArea);
			
			JPanel commandPanelDeploy = new JPanel();
			springLayout.putConstraint(SpringLayout.NORTH, commandPanelDeploy, 0, SpringLayout.NORTH, frame.getContentPane());
			springLayout.putConstraint(SpringLayout.WEST, commandPanelDeploy, 0, SpringLayout.EAST, commandPanelRetrieve);
			springLayout.putConstraint(SpringLayout.SOUTH, commandPanelDeploy, 0, SpringLayout.SOUTH, frame.getContentPane());
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
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, targetPackagesComboBox, 10, SpringLayout.WEST, commandPanelDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, targetPackagesComboBox, -10, SpringLayout.EAST, commandPanelDeploy);
			commandPanelDeploy.add(targetPackagesComboBox);
			
			JScrollPane targetPackageScrollPane = new JScrollPane();
			targetPackageScrollPane.setAutoscrolls(true);
			sl_commandPanelDeploy.putConstraint(SpringLayout.NORTH, targetPackageScrollPane, 15, SpringLayout.SOUTH, targetPackagesComboBox);
			sl_commandPanelDeploy.putConstraint(SpringLayout.WEST, targetPackageScrollPane, 10, SpringLayout.WEST, commandPanelDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.SOUTH, targetPackageScrollPane, 550, SpringLayout.NORTH, commandPanelDeploy);
			sl_commandPanelDeploy.putConstraint(SpringLayout.EAST, targetPackageScrollPane, -10, SpringLayout.EAST, commandPanelDeploy);
			commandPanelDeploy.add(targetPackageScrollPane);
			
			targetPackageTextArea = new JTextArea();
			targetPackageTextArea.setEditable(false);
			targetPackageScrollPane.setViewportView(targetPackageTextArea);
			
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

				if("encrypt".equals(passwordType)) {

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
			}

		} catch (IOException | NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidKeySpecException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
		logger.traceExit();
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
		
		logger.info("Run retrieve task");
		retrieveTask.execute();
		logger.info("Retrieve task done");
		
	}
	
	private void deployAction() throws IOException, NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, InvalidKeySpecException {
		
		logger.traceEntry();
		
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
				
		deployTask.setDeployRoot("retrieveUnpackaged");
		
		logger.info("Run deploy task");
		deployTask.execute();
		logger.info("Deploy task done");
		
	}
	
}
