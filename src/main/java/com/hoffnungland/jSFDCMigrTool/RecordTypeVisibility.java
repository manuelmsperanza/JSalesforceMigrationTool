package com.hoffnungland.jSFDCMigrTool;

public class RecordTypeVisibility implements Comparable<RecordTypeVisibility> {
	
	public String profileName;
	public String defaultFlag;
	public String personAccountDefault;
	public String recordType;
	
	@Override
	public int compareTo(RecordTypeVisibility o) {
		int result = this.profileName.compareTo(o.profileName);
		if(result == 0) {
			result = this.defaultFlag.compareTo(o.defaultFlag);
			if(result == 0) {
				result = this.personAccountDefault.compareTo(o.personAccountDefault);
				if(result == 0) {
					result = this.recordType.compareTo(o.recordType);
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof RecordTypeVisibility) {
			RecordTypeVisibility o = (RecordTypeVisibility) obj;
			result = (this.profileName.equals(o.profileName));
			if(result) {
				result = (this.defaultFlag.equals(o.defaultFlag));
				if(result) {
					result = (this.personAccountDefault.equals(o.personAccountDefault));
					if(result) {
						result = (this.recordType.equals(o.recordType));
					}
				}
			}
		}
		return result;
	}
}
