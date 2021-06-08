package com.hoffnungland.jSFDCMigrTool;

public class ProfileActionOverride implements Comparable<ProfileActionOverride> {
	
	public String applicationName;
	public String actionName;
	public String content;
	public String formFactor;
	public String pageOrSobjectType;
	public String recordType;
	public String type;
	public String profile;
	
	@Override
	public int compareTo(ProfileActionOverride o) {
		int result = this.applicationName.compareTo(o.applicationName);
		if(result == 0) {
			result = this.actionName.compareTo(o.actionName);
			if(result == 0) {
				result = this.content.compareTo(o.content);
				if(result == 0) {
					result = this.formFactor.compareTo(o.formFactor);
					if(result == 0) {
						result = this.pageOrSobjectType.compareTo(o.pageOrSobjectType);
						if(result == 0) {
							result = this.recordType.compareTo(o.recordType);
							if(result == 0) {
								result = this.type.compareTo(o.type);
								if(result == 0) {
									result = this.profile.compareTo(o.profile);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof ProfileActionOverride) {
			ProfileActionOverride o = (ProfileActionOverride) obj;
			result = (this.applicationName.equals(o.applicationName));
			if(result) {
				result = (this.actionName.equals(o.actionName));
				if(result) {
					result = (this.content.equals(o.content));
					if(result) {
						result = (this.formFactor.equals(o.formFactor));
						if(result) {
							result = (this.pageOrSobjectType.equals(o.pageOrSobjectType));
							if(result) {
								result = (this.recordType.equals(o.recordType));
								if(result) {
									result = (this.type.equals(o.type));
									if(result) {
										result = (this.profile.equals(o.profile));
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
}
