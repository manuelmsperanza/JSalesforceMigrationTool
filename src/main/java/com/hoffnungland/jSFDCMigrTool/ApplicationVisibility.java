package com.hoffnungland.jSFDCMigrTool;

public class ApplicationVisibility implements Comparable<ApplicationVisibility> {
	
	public String profileName;
	public String application;
	public String defaultFlag;
	public String visible;
	
	@Override
	public int compareTo(ApplicationVisibility o) {
		int result = this.profileName.compareTo(o.profileName);
		if(result == 0) {
			result = this.application.compareTo(o.application);
			if(result == 0) {
				result = this.defaultFlag.compareTo(o.defaultFlag);
				if(result == 0) {
					result = this.visible.compareTo(o.visible);
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof ApplicationVisibility) {
			ApplicationVisibility o = (ApplicationVisibility) obj;
			result = (this.profileName.equals(o.profileName));
			if(result) {
				result = (this.application.equals(o.application));
				if(result) {
					result = (this.defaultFlag.equals(o.defaultFlag));
					if(result) {
						result = (this.visible.equals(o.visible));
					}
				}
			}
		}
		return result;
	}
}
