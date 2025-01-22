package com.hoffnungland.jSFDCMigrTool.Admin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.BuildException;

import com.sforce.soap.partner.CallOptions_element;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.IGetUserInfoResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.fault.ApiFault;
import com.sforce.soap.partner.fault.LoginFault;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sforce.ws.SoapFaultException;

/**
 * 
 */

public class LoginAntTask {
	
	private static final Logger logger = LogManager.getLogger(LoginAntTask.class);

	private String username;

	private String password;

	private String sessionId;

	private double apiVersion = 59.0D;

	private String server = "https://login.salesforce.com";

	public LoginAntTask(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getErrorMessage(Throwable t) {
		String errorMsg = "";
		if (t instanceof ApiFault) {
			ApiFault fault = (ApiFault) t;
			errorMsg = fault.getExceptionCode() + " - " + fault.getExceptionMessage();
		} else if (t instanceof SoapFaultException) {
			SoapFaultException fault = (SoapFaultException) t;
			errorMsg = fault.getFaultCode() + " - " + fault.getMessage();
		} else if (t.getMessage() != null) {
			errorMsg = t.getMessage();
		}
		return errorMsg;
	}

	LoginResult doLogin() {
		logger.traceEntry();
		ConnectorConfig config = new ConnectorConfig();

		String serverEndpoint = this.server + "/services/Soap/u/" + this.apiVersion;

		config.setAuthEndpoint(serverEndpoint);
		config.setServiceEndpoint(serverEndpoint);
		if (this.sessionId != null && !this.sessionId.isEmpty()) {
			config.setSessionId(this.sessionId);
		} else {
			config.setManualLogin(true);
		}

		try {
			
			PartnerConnection partnerConnection = Connector.newConnection(config);
		    CallOptions_element callOptions = new CallOptions_element();
		    callOptions.setClient("AntMigrationTool/59.0");
		    partnerConnection.__setCallOptions(callOptions);
			
			if (this.sessionId != null && !this.sessionId.isEmpty()) {
				LoginResult lResult = new LoginResult();
				lResult.setSessionId(this.sessionId);
				lResult.setServerUrl(serverEndpoint);
				lResult.setUserInfo((IGetUserInfoResult) partnerConnection.getUserInfo());
				return lResult;
			}
			return logger.traceExit(partnerConnection.login(this.username, this.password));
		} catch (LoginFault lf) {
			throw new BuildException(lf.getExceptionMessage(), lf);
		} catch (ConnectionException ce) {
			throw new BuildException("Failed to login: " + getErrorMessage(ce), ce);
		}
	}
}
