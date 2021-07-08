package com.hoffnungland.jSFDCMigrTool;


public class BundleMetadata implements Comparable<BundleMetadata> {
	public String bundleName;
	public String componentName;
	
	@Override
	public int compareTo(BundleMetadata o) {
		int result = this.bundleName.compareTo(o.bundleName);
		if(result == 0) {
			result = this.componentName.compareTo(o.componentName);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof BundleMetadata) {
			BundleMetadata o = (BundleMetadata) obj;
			result = (this.compareTo(o) == 0);
		}
		return result;
	}
}
