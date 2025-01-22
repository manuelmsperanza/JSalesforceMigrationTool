# JSalesforceMigrationTool

# Org Properties


* **sf.serverurl**=<Use 'https://login.salesforce.com' for production or developer edition (the default if not specified), use 'https://test.salesforce.com for sandbox.>
* **sf.username**=<target user>
* **passwordType**=[encrypt: encrypt the value and add the new entry to keyStore |encrypted: keystore entry password |oneTimePassword: password is not stored, you must enter it every time you try to connect] 
* **sf.password**=<target password when passwordType is encrypt or keystore entry password when passwordType is encrypted>

Please, refer to official documentation in case of org connection errors. [Entering Salesforce Connection Information | Ant Migration Tool Guide | Salesforce Developers](https://developer.salesforce.com/docs/atlas.en-us.daas.meta/daas/forcemigrationtool_connect.htm)

## Get Org info

	sfdx force:org:display

Please, refer to official documentation for more information. [org Commands | Salesforce CLI Command Reference | Salesforce Developers](https://developer.salesforce.com/docs/atlas.en-us.sfdx_cli_reference.meta/sfdx_cli_reference/cli_reference_force_org.htm#cli_reference_force_org_display)

## Get Scratch Org Password

	sfdx force:user:password:generate

Please, refer to official documentation for more information. [Salesforce CLI Command Reference | Salesforce CLI Command Reference | Salesforce Developers](https://developer.salesforce.com/docs/atlas.en-us.sfdx_cli_reference.meta/sfdx_cli_reference/cli_reference_force_user.htm#cli_reference_force_user_password_generate)

You can see the password again by running
	
	sfdx force:user:display

## SOAP API & Metadata API

[SOAP API Developer Guide | Introducing SOAP API (salesforce.com)](https://developer.salesforce.com/docs/atlas.en-us.api.meta/api/sforce_api_quickstart_intro.htm)

[Metadata API Developer Guide | Understanding Metadata API (salesforce.com)](https://developer.salesforce.com/docs/atlas.en-us.api_meta.meta/api_meta/meta_intro.htm)

### Get Salesforce WSDL for SOAP Api

[SOAP API Developer Guide | Step 2: Generate or Obtain the Web Service WSDL (salesforce.com)](https://developer.salesforce.com/docs/atlas.en-us.api.meta/api/sforce_api_quickstart_steps_generate_wsdl.htm)

### Force.com Web Service Connector

clone the https://github.com/forcedotcom/wsc repository

Create the executable jar

	mvn clean package "-Dgpg.skip" "-Dmaven.test.skip"

Generate stub from WSDL

	java -jar target/force-wsc-52.2.0-uber.jar <inputwsdlfile> <outputjarfile>

# KeyStore
For more information, please refer to official documentation [keytool - Oracle Help Center](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html)

## Create KeyStore and Private Key (with user prompt)
keytool -genkeypair -keysize 2048 -keyalg RSA -alias <name.surname> -keystore ~/OneDrive/JSalesforceMigrationTool.jks

	Enter keystore password: <your keystore password>
	Re-enter new password: <your keystore password previously set>
	What is your first and last name?
		[Unknown]:  <name.surname>
	What is the name of your organizational unit?
		[Unknown]:  NA
	What is the name of your organization?
		[Unknown]:  NA
	What is the name of your City or Locality?
		[Unknown]:  <your city or locality>
	What is the name of your State or Province?
		[Unknown]:  <state or province of your city>
	What is the two-letter country code for this unit?
		[Unknown]:  <[ISO 3166-1 alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)>
	Is CN=<name.surname>, OU=NA, O=NA, L=<city or locality>, ST=<state or province>, C=<country code> correct?
		[no]:  yes

	Enter key password for <name.surname>
	        (RETURN if same as keystore password):
	Re-enter new password:

## Create KeyStore and Private Key (Silent mode)
_Different store and key passwords not supported for PKCS12 KeyStores_

	keytool -genkeypair -keysize 2048 -keyalg RSA -alias <name.surname> -keystore ~/OneDrive/JSalesforceMigrationTool.jks -dname "CN=<name.surname>, OU=NA, O=NA, L=<city or locality>, ST=<state or province>, C=<country code>" -validity 365 -storepass <your keystore password>

## Create a new project
	mvn archetype:generate -Dfilter="org.apache.maven.archetypes:maven-archetype-quickstart" -DgroupId="com.hoffnungland" -DartifactId=JSalesforceMigrationTool -Dpackage="com.hoffnungland.jSFDCMigrTool" -Dversion="0.0.1-SNAPSHOT"

## Build settings
### Add prerequisites
	<prerequisites>
		<maven>3.1.0</maven>
	</prerequisites>

Update to java 1.8<br>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<java.source.version>1.8</java.source.version>
		<java.target.version>1.8</java.target.version>
	</properties>



# Run with Maven
	
	start mvn exec:java -Dexec.mainClass="com.hoffnungland.jSFDCMigrTool.App" -Dlog4j.configurationFile=src/main/resources/log4j2.xml

# Create Jar with dependencies

## Configure the pom.xml

	<plugin>
		<artifactId>maven-assembly-plugin</artifactId>
		<configuration>
			<descriptorRefs>
				<descriptorRef>jar-with-dependencies</descriptorRef>
			</descriptorRefs>
			<appendAssemblyId>false</appendAssemblyId>
			<finalName>${project.name}</finalName>
			<archive>
				<manifest>
					<mainClass>com.hoffnungland.jSFDCMigrTool.App</mainClass>
				</manifest>
			</archive>
		</configuration>
	</plugin>

## Execute the maven assembly single

	mvn install assembly:single
	
# add .gitignore to mandatory empty directory

	# Ignore everything in this directory
	*
	# Except this file
	!.gitignore

# Configure the Package Clean UP Automation with GitHub Action

The Action run during the release phase of package (or you can run it manually).
Leave only the latest package version into the repository.
Create the .github/workflows/cleanupPackages.yml file.


# Support

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/K3K441XSO)

[![Support the development of these features](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/donate/?business=VU48PTCSF93A2&no_recurring=0&item_name=Support+the+development+of+these+features.&currency_code=USD)