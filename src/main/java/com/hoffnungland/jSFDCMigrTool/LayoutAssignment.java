package com.hoffnungland.jSFDCMigrTool;

public class LayoutAssignment implements Comparable<LayoutAssignment> {
	public String profileName;
	public String layout;
	public String recordType;
	
	@Override
	public int compareTo(LayoutAssignment o) {
		int result = this.profileName.compareTo(o.profileName);
		if(result == 0) {
			result = this.layout.compareTo(o.layout);
			if(result == 0) {
				result = this.recordType.compareTo(o.recordType);
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof LayoutAssignment) {
			LayoutAssignment o = (LayoutAssignment) obj;
			result = (this.profileName.equals(o.profileName));
			if(result) {
				result = (this.layout.equals(o.layout));
				if(result) {
					result = (this.recordType.equals(o.recordType));
				}
			}
		}
		return result;
	}
}
