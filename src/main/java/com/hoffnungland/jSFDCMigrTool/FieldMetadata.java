package com.hoffnungland.jSFDCMigrTool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FieldMetadata implements Comparable<FieldMetadata> {
	public String objectName;
	public String fullName;
	public String label;
	public String type;
	public String externalId;
	public String required;
	public String unique;
	public String deleteConstraint;
	public String fieldManageability;
	public String referenceTo;
	public String relationshipLabel;
	public String relationshipName;
	public String relationshipOrder;
	public String reparentableMasterDetail;
	public String writeRequiresMasterRead;
	public String valueSetRestricted;
	public String valueSetDefinitionSorted;
	public List<FieldValueMetadata> listValues;
	
	@Override
	public int compareTo(FieldMetadata o) {
		int result = this.objectName.compareTo(o.objectName);
		if(result == 0) {
			result = this.fullName.compareTo(o.fullName);
			if(result == 0) {
				result = this.label.compareTo(o.label);
				if(result == 0) {
					result = this.type.compareTo(o.type);
					if(result == 0) {
						result = this.valueSetRestricted.compareTo(o.valueSetRestricted);
						if(result == 0) {
							result = this.valueSetDefinitionSorted.compareTo(o.valueSetDefinitionSorted);
							if(result == 0) {
								result = this.listValues.size() - o.listValues.size();
								if(result == 0) {
									this.listValues.sort(Comparator.naturalOrder());
									o.listValues.sort(Comparator.naturalOrder());
									for(int listValuesIdx=0; listValuesIdx < listValues.size(); listValuesIdx++) {
										result = this.listValues.get(listValuesIdx).compareTo(o.listValues.get(listValuesIdx));
										if(result != 0) {
											return result;
										}
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
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof FieldMetadata) {
			FieldMetadata o = (FieldMetadata) obj;
			result = (this.objectName.equals(o.objectName));
			if(result) {
				result = this.fullName.equals(o.fullName);
				if(result) {
					result = this.label.equals(o.label);
					if(result) {
						result = this.type.equals(o.type);
						if(result) {
							result = this.valueSetRestricted.equals(o.valueSetRestricted);
							if(result) {
								result = this.valueSetDefinitionSorted.equals(o.valueSetDefinitionSorted);
								if(result) {
									List<FieldValueMetadata> cp = new ArrayList<FieldValueMetadata>(this.listValues );
								    for (FieldValueMetadata curFieldMetadata : o.listValues ) {
								        if (!cp.remove(curFieldMetadata)){
								            return false;
								        }
								    }
								    return cp.isEmpty();
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
