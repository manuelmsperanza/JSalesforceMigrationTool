package com.hoffnungland.jSFDCMigrTool.entity;

import java.util.HashMap;
import java.util.Map;

public class Automations {
	public Map<String, String> flows = new HashMap<String, String>();
	public Map<String, String> flowDefinitions = new HashMap<String, String>();
	public Map<String, String> triggers = new HashMap<String, String>();
	public Map<String, Workflows> workflows = new HashMap<String, Workflows>();
	public Map<String, ValidationRules> validationRules = new HashMap<String, ValidationRules>();
}
