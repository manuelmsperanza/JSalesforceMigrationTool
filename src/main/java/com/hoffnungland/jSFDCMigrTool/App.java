package com.hoffnungland.jSFDCMigrTool;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hoffnungland.jAppKs.AppKeyStoreManager;
import com.hoffnungland.jAppKs.PasswordPanel;

public class App implements ActionListener {

	private static final Logger logger = LogManager.getLogger(App.class);
	private static String fileSeparator = System.getProperty("file.separator");
	
	private JFrame frame;
	
	private AppKeyStoreManager appKsManager;
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

					App window = new App(passwordKs);
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
	public App(String passwordKs) {
		initialize(passwordKs);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String passwordKs) {
		logger.traceEntry();

		String keyStorePath = System.getProperty("user.home") + fileSeparator + "OneDrive"+ fileSeparator + "JSalesforceMigrationTool.jks";
		this.appKsManager = new AppKeyStoreManager(keyStorePath, passwordKs);
		try {
			
			this.appKsManager.init();
			frame = new JFrame();
			frame.setBounds(100, 100, 450, 300);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error(e);
			JOptionPane.showMessageDialog(this.frame, e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
		}
		logger.traceExit();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		logger.traceEntry();
		
		logger.traceExit();
	}

}
