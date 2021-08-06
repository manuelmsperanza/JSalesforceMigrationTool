package com.hoffnungland.jSFDCMigrTool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Login utility.
 */
public class MetadataLoginUtil {
	
	private static final Logger logger = LogManager.getLogger(MetadataLoginUtil.class);
	
    public static MetadataConnection login() throws ConnectionException {
    	//final String USERNAME = "manuel.speranza@eap2.it";
        final String USERNAME = "test-ctqadmynuucv@example.com";
        // This is only a sample. Hard coding passwords in source files is a bad practice.
    	//final String PASSWORD = "&2lcMmlwbslufEV3WaPxLkKPmQsE1poJp8gCbz";
        final String PASSWORD = "4XgweJnjx4m(JQCk1gEg0fMPoVkGlG6BWEoT9L";
        //final String URL = "https://login.salesforce.com/services/Soap/c/52.0";
        final String URL = "https://test.salesforce.com/services/Soap/c/52.0/";
        final LoginResult loginResult = loginToSalesforce(USERNAME, PASSWORD, URL);
        return createMetadataConnection(loginResult);
    }

    private static MetadataConnection createMetadataConnection(
            final LoginResult loginResult) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config.setSessionId(loginResult.getSessionId());
        return new MetadataConnection(config);
    }

    private static LoginResult loginToSalesforce(
            final String username,
            final String password,
            final String loginUrl) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(loginUrl);
        config.setServiceEndpoint(loginUrl);
        config.setManualLogin(true);
        return (new EnterpriseConnection(config)).login(username, password);
    }
}
