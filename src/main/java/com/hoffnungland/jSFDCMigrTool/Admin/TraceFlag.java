package com.hoffnungland.jSFDCMigrTool.Admin;

import java.time.OffsetDateTime;

enum LogType {
	CLASS_TRACING,
	DEVELOPER_LOG,
	PROFILING,
	USER_DEBUG
}



public class TraceFlag {
	
	private LogType LogType;
	private String TracedEntityId;
	private String DebugLevelId;
	private OffsetDateTime StartDate;
	private OffsetDateTime ExpirationDate; 
	private ApexCode ApexCode;
    private ApexProfiling ApexProfiling;
    private Callout Callout;
    private Database Database;
    private SystemLL System;
    private Validation Validation;
    private Visualforce Visualforce;
    private Workflow Workflow;
    
	public TraceFlag(LogType logType, String tracedEntityId, String debugLevelId, OffsetDateTime startDate, OffsetDateTime expirationDate,
			ApexCode apexCode, ApexProfiling apexProfiling, Callout callout, Database database, SystemLL system,
			Validation validation, Visualforce visualforce, Workflow workflow) {
		super();
		this.LogType = logType;
		this.TracedEntityId = tracedEntityId;
		this.DebugLevelId = debugLevelId;
		this.StartDate = startDate;
		this.ExpirationDate = expirationDate;
		this.ApexCode = apexCode;
		this.ApexProfiling = apexProfiling;
		this.Callout = callout;
		this.Database = database;
		this.System = system;
		this.Validation = validation;
		this.Visualforce = visualforce;
		this.Workflow = workflow;
	}
	
	public String getTracedEntityId() {
		return TracedEntityId;
	}
	public void setTracedEntityId(String tracedEntityId) {
		TracedEntityId = tracedEntityId;
	}
	public String getDebugLevelId() {
		return DebugLevelId;
	}
	public void setDebugLevelId(String debugLevelId) {
		DebugLevelId = debugLevelId;
	}
	public OffsetDateTime getStartDate() {
		return StartDate;
	}
	public void setStartDate(OffsetDateTime startDate) {
		StartDate = startDate;
	}
	public OffsetDateTime getExpirationDate() {
		return ExpirationDate;
	}
	public void setExpirationDate(OffsetDateTime expirationDate) {
		ExpirationDate = expirationDate;
	}
	public ApexCode getApexCode() {
		return ApexCode;
	}
	public void setApexCode(ApexCode apexCode) {
		this.ApexCode = apexCode;
	}
	public ApexProfiling getApexProfiling() {
		return ApexProfiling;
	}
	public void setApexProfiling(ApexProfiling apexProfiling) {
		this.ApexProfiling = apexProfiling;
	}
	public Callout getCallout() {
		return Callout;
	}
	public void setCallout(Callout callout) {
		this.Callout = callout;
	}
	public Database getDatabase() {
		return Database;
	}
	public void setDatabase(Database database) {
		this.Database = database;
	}
	public SystemLL getSystem() {
		return System;
	}
	public void setSystem(SystemLL system) {
		this.System = system;
	}
	public Validation getValidation() {
		return Validation;
	}
	public void setValidation(Validation validation) {
		this.Validation = validation;
	}
	public Visualforce getVisualforce() {
		return Visualforce;
	}
	public void setVisualforce(Visualforce visualforce) {
		this.Visualforce = visualforce;
	}
	public Workflow getWorkflow() {
		return Workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.Workflow = workflow;
	}
	
    
    
    
}
