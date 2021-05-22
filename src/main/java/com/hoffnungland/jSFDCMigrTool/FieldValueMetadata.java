package com.hoffnungland.jSFDCMigrTool;

public class FieldValueMetadata implements Comparable<FieldValueMetadata> {
	
	public int idx;
	public String fullName;
	public String defaultVs;
	public String label;
	
	@Override
	public int compareTo(FieldValueMetadata o) {
		int result = this.idx - o.idx;
		if(result == 0) {
			result = this.fullName.compareTo(o.fullName);
			if(result == 0) {
				result = this.defaultVs.compareTo(o.defaultVs);
				if(result == 0) {
					result = this.label.compareTo(o.label);
				}
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof FieldValueMetadata) {
			FieldValueMetadata o = (FieldValueMetadata) obj;
			result = (this.idx == o.idx);
			if(result) {
				result = this.fullName.equals(o.fullName);
				if(result) {
					result = this.defaultVs.equals(o.defaultVs);
					if(result) {
						result = this.label.equals(o.label);
					}
				}
			}
		}
		return result;
	}
	
}
