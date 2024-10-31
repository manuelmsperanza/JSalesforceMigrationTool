package com.hoffnungland.jSFDCMigrTool.Admin;

enum ApexCode {
	NONE,
	ERROR,
	WARN,
	INFO,
	DEBUG,
	FINE,
	FINER,
	FINEST
}

enum ApexProfiling {
	NONE,
	INFO,
	FINE,
	FINEST
}

enum Callout {
	NONE,
	ERROR,
	INFO,
	FINER,
	FINEST
}

enum Database {
	NONE,
	WARN,
	INFO,
	FINE,
	FINEST
}

enum SystemLL {
	NONE,
	INFO,
	DEBUG,
	FINE
}

enum Validation {
	NONE,
	INFO
}

enum Visualforce {
	NONE,
	INFO,
	DEBUG,
	FINE,
	FINER
}

enum Workflow {
	NONE,
	ERROR,
	WARN,
	INFO,
	FINE,
	FINER
}

public class DebugLevel {
	
	private String DeveloperName;
	private String MasterLabel;
	private ApexCode apexCode;
    private ApexProfiling apexProfiling;
    private Callout callout;
    private Database database;
    private SystemLL system;
    private Validation validation;
    private Visualforce visualforce;
    private Workflow workflow;
    
	public DebugLevel(String developerName, String masterLabel, ApexCode apexCode, ApexProfiling apexProfiling,
			Callout callout, Database database, SystemLL system, Validation validation, Visualforce visualforce,
			Workflow workflow) {
		super();
		DeveloperName = developerName;
		MasterLabel = masterLabel;
		this.apexCode = apexCode;
		this.apexProfiling = apexProfiling;
		this.callout = callout;
		this.database = database;
		this.system = system;
		this.validation = validation;
		this.visualforce = visualforce;
		this.workflow = workflow;
	}

	public String getDeveloperName() {
		return DeveloperName;
	}

	public void setDeveloperName(String developerName) {
		DeveloperName = developerName;
	}

	public String getMasterLabel() {
		return MasterLabel;
	}

	public void setMasterLabel(String masterLabel) {
		MasterLabel = masterLabel;
	}

	public ApexCode getApexCode() {
		return apexCode;
	}

	public void setApexCode(ApexCode apexCode) {
		this.apexCode = apexCode;
	}

	public ApexProfiling getApexProfiling() {
		return apexProfiling;
	}

	public void setApexProfiling(ApexProfiling apexProfiling) {
		this.apexProfiling = apexProfiling;
	}

	public Callout getCallout() {
		return callout;
	}

	public void setCallout(Callout callout) {
		this.callout = callout;
	}

	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	public SystemLL getSystem() {
		return system;
	}

	public void setSystem(SystemLL system) {
		this.system = system;
	}

	public Validation getValidation() {
		return validation;
	}

	public void setValidation(Validation validation) {
		this.validation = validation;
	}

	public Visualforce getVisualforce() {
		return visualforce;
	}

	public void setVisualforce(Visualforce visualforce) {
		this.visualforce = visualforce;
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
    
	
    
}
