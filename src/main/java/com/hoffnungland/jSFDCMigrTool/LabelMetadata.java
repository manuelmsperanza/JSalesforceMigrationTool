package com.hoffnungland.jSFDCMigrTool;


public class LabelMetadata implements Comparable<LabelMetadata> {
	public String objectName;
	public String fullName;
	public String categories;
	public String language;
	public String protectedFlag;
	public String shortDescription;
	public String value;
	
	@Override
	public int compareTo(LabelMetadata o) {
		int result = this.objectName.compareTo(o.objectName);
		if(result == 0) {
			result = this.fullName.compareTo(o.fullName);
			if(result == 0) {
				result = this.categories.compareTo(o.categories);
				if(result == 0) {
					result = this.language.compareTo(o.language);
					if(result == 0) {
						result = this.protectedFlag.compareTo(o.protectedFlag);
						if(result == 0) {
							result = this.value.compareTo(o.value);
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
		if(obj instanceof LabelMetadata) {
			LabelMetadata o = (LabelMetadata) obj;
			result = (this.objectName.equals(o.objectName));
			if(result) {
				result = this.fullName.equals(o.fullName);
				if(result) {
					result = this.categories.equals(o.categories);
					if(result) {
						result = this.language.equals(o.language);
						if(result) {
							result = this.protectedFlag.equals(o.protectedFlag);
							if(result) {
								result = this.value.equals(o.value);
							}
						}
					}
				}
			}
		}
		return result;
	}
}
