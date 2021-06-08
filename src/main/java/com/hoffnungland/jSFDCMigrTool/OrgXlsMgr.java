package com.hoffnungland.jSFDCMigrTool;

import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.hoffnungland.poi.corner.orcxlsreport.ExcelManager;

public class OrgXlsMgr extends ExcelManager {
	
	private static final Logger logger = LogManager.getLogger(OrgXlsMgr.class);
	
	public OrgXlsMgr(String name) {
		super(name);
	}
	
	protected void createMetadataHeader(org.apache.poi.xssf.usermodel.XSSFSheet workSheet, String headerValue, int columnsCount, int inRowId, int inColId) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFRow headerRow = workSheet.createRow(inRowId);
		org.apache.poi.xssf.usermodel.XSSFCell columnNameCell = headerRow.createCell(inColId);
		columnNameCell.setCellValue(headerValue);
		columnNameCell.setCellStyle(this.metadataHeaderCellStyle);
		if(columnsCount > 1) {
			workSheet.addMergedRegion(new CellRangeAddress(inRowId, inRowId, inColId, inColId + columnsCount - 1));
		}
		logger.traceExit();
	}
	
	protected void createSheetHeader(org.apache.poi.xssf.usermodel.XSSFSheet workSheet, String[] columnsList, int inRowId, int inColId) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFRow headerRow = workSheet.createRow(inRowId);
		for(int headerIdx = 0; headerIdx < columnsList.length; headerIdx++){
			org.apache.poi.xssf.usermodel.XSSFCell columnNameCell = headerRow.createCell(headerIdx + inColId);
			String columnaName = columnsList[headerIdx];
			columnNameCell.setCellValue(columnaName);
			columnNameCell.setCellStyle(this.headerCellStyle);
			if(System.getProperty("os.name").startsWith("Windows")){
				workSheet.autoSizeColumn(headerIdx);
			}
		}
	}
	
	private void setDefaultStyleCell(XSSFRow bodyRow, int cellIdx, String cellValue) {
		org.apache.poi.xssf.usermodel.XSSFCell objectNameCell = bodyRow.getCell(cellIdx);
		
		if(objectNameCell == null) {
			objectNameCell = bodyRow.createCell(cellIdx);
		}
		
		objectNameCell.setCellStyle(this.defaultCellStyle);
		objectNameCell.setCellValue(cellValue);
	}
	
	private void setDefaultStyleCell(XSSFRow bodyRow, int cellIdx, int cellValue) {
		org.apache.poi.xssf.usermodel.XSSFCell objectNameCell = bodyRow.getCell(cellIdx);
		
		if(objectNameCell == null) {
			objectNameCell = bodyRow.createCell(cellIdx);
		}
		
		objectNameCell.setCellStyle(this.defaultCellStyle);
		objectNameCell.setCellValue(cellValue);
	}

	public void createSheetFields(List<FieldMetadata> listFieldMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Fields");
		
		String[] columnsList = {"object", "fullName", "label", "type", "externalId", "required", "unique", "deleteConstraint", "fieldManageability", "referenceTo", "relationshipLabel", "relationshipName", "relationshipOrder", "reparentableMasterDetail", "writeRequiresMasterRead", "valueSet.restricted", "valueSetDefinition.sorted", "value.position", "value.fullName", "value.default", "value.label"};
		
		this.createMetadataHeader(workSheet, "Metadata fields", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listFieldMetadata.sort(Comparator.naturalOrder());
		for(FieldMetadata entry : listFieldMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			this.setDefaultStyleCell(bodyRow, 0, entry.objectName);
			this.setDefaultStyleCell(bodyRow, 1, entry.fullName);
			this.setDefaultStyleCell(bodyRow, 2, entry.label);
			this.setDefaultStyleCell(bodyRow, 3, entry.type);
			this.setDefaultStyleCell(bodyRow, 4, entry.externalId);
			this.setDefaultStyleCell(bodyRow, 5, entry.required);
			this.setDefaultStyleCell(bodyRow, 6, entry.unique);
			this.setDefaultStyleCell(bodyRow, 7, entry.deleteConstraint);
			this.setDefaultStyleCell(bodyRow, 8, entry.fieldManageability);
			this.setDefaultStyleCell(bodyRow, 9, entry.referenceTo);
			this.setDefaultStyleCell(bodyRow, 10, entry.relationshipLabel);
			this.setDefaultStyleCell(bodyRow, 11, entry.relationshipName);
			this.setDefaultStyleCell(bodyRow, 12, entry.relationshipOrder);
			this.setDefaultStyleCell(bodyRow, 13, entry.reparentableMasterDetail);
			this.setDefaultStyleCell(bodyRow, 14, entry.writeRequiresMasterRead);
			this.setDefaultStyleCell(bodyRow, 15, entry.valueSetRestricted);
			this.setDefaultStyleCell(bodyRow, 16, entry.valueSetDefinitionSorted);
						
			if(entry.listValues.size() > 0) {
				{
					FieldValueMetadata fieldValueMetadata = entry.listValues.get(0);
					
					this.setDefaultStyleCell(bodyRow, 17, fieldValueMetadata.idx);
					this.setDefaultStyleCell(bodyRow, 18, fieldValueMetadata.fullName);
					this.setDefaultStyleCell(bodyRow, 19, fieldValueMetadata.defaultVs);
					this.setDefaultStyleCell(bodyRow, 20, fieldValueMetadata.label);
				}
				for(int idx = 1; idx < entry.listValues.size(); idx++) {
					rowId++;
					FieldValueMetadata fieldValueMetadata = entry.listValues.get(idx);
					
					org.apache.poi.xssf.usermodel.XSSFRow bodyVsRow = workSheet.getRow(rowId);
					if(bodyVsRow == null) {
						bodyVsRow = workSheet.createRow(rowId);
					}
					
					this.setDefaultStyleCell(bodyVsRow, 0, entry.objectName);
					this.setDefaultStyleCell(bodyVsRow, 1, entry.fullName);
					this.setDefaultStyleCell(bodyVsRow, 2, entry.label);
					this.setDefaultStyleCell(bodyVsRow, 3, entry.type);
					this.setDefaultStyleCell(bodyVsRow, 4, entry.externalId);
					this.setDefaultStyleCell(bodyVsRow, 5, entry.required);
					this.setDefaultStyleCell(bodyVsRow, 6, entry.unique);
					this.setDefaultStyleCell(bodyVsRow, 7, entry.deleteConstraint);
					this.setDefaultStyleCell(bodyVsRow, 8, entry.fieldManageability);
					this.setDefaultStyleCell(bodyVsRow, 9, entry.referenceTo);
					this.setDefaultStyleCell(bodyVsRow, 10, entry.relationshipLabel);
					this.setDefaultStyleCell(bodyVsRow, 11, entry.relationshipName);
					this.setDefaultStyleCell(bodyVsRow, 12, entry.relationshipOrder);
					this.setDefaultStyleCell(bodyVsRow, 13, entry.reparentableMasterDetail);
					this.setDefaultStyleCell(bodyVsRow, 14, entry.writeRequiresMasterRead);
					this.setDefaultStyleCell(bodyVsRow, 15, entry.valueSetRestricted);
					this.setDefaultStyleCell(bodyVsRow, 16, entry.valueSetDefinitionSorted);
					this.setDefaultStyleCell(bodyVsRow, 17, fieldValueMetadata.idx);
					this.setDefaultStyleCell(bodyVsRow, 18, fieldValueMetadata.fullName);
					this.setDefaultStyleCell(bodyVsRow, 19, fieldValueMetadata.defaultVs);
					this.setDefaultStyleCell(bodyVsRow, 20, fieldValueMetadata.label);
					
				}
			}
			
			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}
	
	public void createSheetLabels(List<LabelMetadata> listLabelMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Labels");
		
		String[] columnsList = {"object", "fullName", "categories", "language", "protected", "shortDescription", "value"};
		
		this.createMetadataHeader(workSheet, "Metadata labels", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listLabelMetadata.sort(Comparator.naturalOrder());
		for(LabelMetadata entry : listLabelMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell objectNameCell = bodyRow.getCell(0);

			if(objectNameCell == null) {
				objectNameCell = bodyRow.createCell(0);
			}

			objectNameCell.setCellStyle(this.defaultCellStyle);
			objectNameCell.setCellValue(entry.objectName);

			org.apache.poi.xssf.usermodel.XSSFCell fullNameCell = bodyRow.getCell(1);

			if(fullNameCell == null) {
				fullNameCell = bodyRow.createCell(1);
			}

			fullNameCell.setCellStyle(this.defaultCellStyle);
			fullNameCell.setCellValue(entry.fullName);

			org.apache.poi.xssf.usermodel.XSSFCell categoryCell = bodyRow.getCell(2);

			if(categoryCell == null) {
				categoryCell = bodyRow.createCell(2);
			}

			categoryCell.setCellStyle(this.defaultCellStyle);
			categoryCell.setCellValue(entry.categories);

			org.apache.poi.xssf.usermodel.XSSFCell languageCell = bodyRow.getCell(3);

			if(languageCell == null) {
				languageCell = bodyRow.createCell(3);
			}

			languageCell.setCellStyle(this.defaultCellStyle);
			languageCell.setCellValue(entry.language);

			org.apache.poi.xssf.usermodel.XSSFCell protectedCell = bodyRow.getCell(4);

			if(protectedCell == null) {
				protectedCell = bodyRow.createCell(4);
			}

			protectedCell.setCellStyle(this.defaultCellStyle);
			protectedCell.setCellValue(entry.protectedFlag);

			org.apache.poi.xssf.usermodel.XSSFCell shortDescriptionCell = bodyRow.getCell(5);

			if(shortDescriptionCell == null) {
				shortDescriptionCell = bodyRow.createCell(5);
			}

			shortDescriptionCell.setCellStyle(this.defaultCellStyle);
			shortDescriptionCell.setCellValue(entry.shortDescription);
			
			org.apache.poi.xssf.usermodel.XSSFCell valueCell = bodyRow.getCell(6);

			if(valueCell == null) {
				valueCell = bodyRow.createCell(6);
			}

			valueCell.setCellStyle(this.defaultCellStyle);
			valueCell.setCellValue(entry.value);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetBundle(String sheetName, String[] columnsList, List<BundleMetadata> listBundleMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet(sheetName);
		
		this.createMetadataHeader(workSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listBundleMetadata.sort(Comparator.naturalOrder());
		for(BundleMetadata entry : listBundleMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell bundleNameCell = bodyRow.getCell(0);

			if(bundleNameCell == null) {
				bundleNameCell = bodyRow.createCell(0);
			}

			bundleNameCell.setCellStyle(this.defaultCellStyle);
			bundleNameCell.setCellValue(entry.bundleName);

			org.apache.poi.xssf.usermodel.XSSFCell componentNameCell = bodyRow.getCell(1);

			if(componentNameCell == null) {
				componentNameCell = bodyRow.createCell(1);
			}

			componentNameCell.setCellStyle(this.defaultCellStyle);
			componentNameCell.setCellValue(entry.componentName);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetList(String sheetName, String columnName, List<String> listObjects) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet(sheetName);
		
		String[] columnsList = {columnName};
		this.createMetadataHeader(workSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listObjects.sort(Comparator.naturalOrder());
		for(String entry : listObjects) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell bundleNameCell = bodyRow.getCell(0);

			if(bundleNameCell == null) {
				bundleNameCell = bodyRow.createCell(0);
			}

			bundleNameCell.setCellStyle(this.defaultCellStyle);
			bundleNameCell.setCellValue(entry);
			
			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetProfilAction(List<ProfileActionOverride> listProfileActionMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Profile Actions");
		
		String[] columnsList = {"applicationName", "actionName", "content", "formFactor", "pageOrSobjectType", "recordType", "type", "profile"};
		
		this.createMetadataHeader(workSheet, "Metadata Profile Actions", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listProfileActionMetadata.sort(Comparator.naturalOrder());
		for(ProfileActionOverride entry : listProfileActionMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell applicationNameCell = bodyRow.getCell(0);

			if(applicationNameCell == null) {
				applicationNameCell = bodyRow.createCell(0);
			}

			applicationNameCell.setCellStyle(this.defaultCellStyle);
			applicationNameCell.setCellValue(entry.applicationName);

			org.apache.poi.xssf.usermodel.XSSFCell actionNameCell = bodyRow.getCell(1);

			if(actionNameCell == null) {
				actionNameCell = bodyRow.createCell(1);
			}

			actionNameCell.setCellStyle(this.defaultCellStyle);
			actionNameCell.setCellValue(entry.actionName);

			org.apache.poi.xssf.usermodel.XSSFCell contentCell = bodyRow.getCell(2);

			if(contentCell == null) {
				contentCell = bodyRow.createCell(2);
			}

			contentCell.setCellStyle(this.defaultCellStyle);
			contentCell.setCellValue(entry.content);

			org.apache.poi.xssf.usermodel.XSSFCell formFactorCell = bodyRow.getCell(3);

			if(formFactorCell == null) {
				formFactorCell = bodyRow.createCell(3);
			}

			formFactorCell.setCellStyle(this.defaultCellStyle);
			formFactorCell.setCellValue(entry.formFactor);

			org.apache.poi.xssf.usermodel.XSSFCell pageOrSobjectTypeCell = bodyRow.getCell(4);

			if(pageOrSobjectTypeCell == null) {
				pageOrSobjectTypeCell = bodyRow.createCell(4);
			}

			pageOrSobjectTypeCell.setCellStyle(this.defaultCellStyle);
			pageOrSobjectTypeCell.setCellValue(entry.pageOrSobjectType);

			org.apache.poi.xssf.usermodel.XSSFCell recordTypeCell = bodyRow.getCell(5);

			if(recordTypeCell == null) {
				recordTypeCell = bodyRow.createCell(5);
			}

			recordTypeCell.setCellStyle(this.defaultCellStyle);
			recordTypeCell.setCellValue(entry.recordType);
			
			org.apache.poi.xssf.usermodel.XSSFCell typeCell = bodyRow.getCell(6);

			if(typeCell == null) {
				typeCell = bodyRow.createCell(6);
			}

			typeCell.setCellStyle(this.defaultCellStyle);
			typeCell.setCellValue(entry.type);
			
			org.apache.poi.xssf.usermodel.XSSFCell profileCell = bodyRow.getCell(7);

			if(profileCell == null) {
				profileCell = bodyRow.createCell(7);
			}

			profileCell.setCellStyle(this.defaultCellStyle);
			profileCell.setCellValue(entry.profile);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetLayoutAssignment(List<LayoutAssignment> listLayoutAssignmentMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Layout Assignments");
		
		String[] columnsList = {"profileName", "layout", "recordType"};
		
		this.createMetadataHeader(workSheet, "Metadata Layout Assignments", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listLayoutAssignmentMetadata.sort(Comparator.naturalOrder());
		for(LayoutAssignment entry : listLayoutAssignmentMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell profileNameCell = bodyRow.getCell(0);

			if(profileNameCell == null) {
				profileNameCell = bodyRow.createCell(0);
			}

			profileNameCell.setCellStyle(this.defaultCellStyle);
			profileNameCell.setCellValue(entry.profileName);

			org.apache.poi.xssf.usermodel.XSSFCell layoutCell = bodyRow.getCell(1);

			if(layoutCell == null) {
				layoutCell = bodyRow.createCell(1);
			}

			layoutCell.setCellStyle(this.defaultCellStyle);
			layoutCell.setCellValue(entry.layout);

			org.apache.poi.xssf.usermodel.XSSFCell recordTypeCell = bodyRow.getCell(2);

			if(recordTypeCell == null) {
				recordTypeCell = bodyRow.createCell(2);
			}

			recordTypeCell.setCellStyle(this.defaultCellStyle);
			recordTypeCell.setCellValue(entry.recordType);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetApplicationVisibility(List<ApplicationVisibility> listApplicationVisibilityMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Application Visibilities");
		
		String[] columnsList = {"profileName", "application", "default", "visible"};
		
		this.createMetadataHeader(workSheet, "Metadata Application Visibilities", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listApplicationVisibilityMetadata.sort(Comparator.naturalOrder());
		for(ApplicationVisibility entry : listApplicationVisibilityMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell profileNameCell = bodyRow.getCell(0);

			if(profileNameCell == null) {
				profileNameCell = bodyRow.createCell(0);
			}

			profileNameCell.setCellStyle(this.defaultCellStyle);
			profileNameCell.setCellValue(entry.profileName);

			org.apache.poi.xssf.usermodel.XSSFCell applicationCell = bodyRow.getCell(1);

			if(applicationCell == null) {
				applicationCell = bodyRow.createCell(1);
			}

			applicationCell.setCellStyle(this.defaultCellStyle);
			applicationCell.setCellValue(entry.application);

			org.apache.poi.xssf.usermodel.XSSFCell defaultCell = bodyRow.getCell(2);

			if(defaultCell == null) {
				defaultCell = bodyRow.createCell(2);
			}

			defaultCell.setCellStyle(this.defaultCellStyle);
			defaultCell.setCellValue(entry.defaultFlag);
			
			org.apache.poi.xssf.usermodel.XSSFCell visibleCell = bodyRow.getCell(3);

			if(visibleCell == null) {
				visibleCell = bodyRow.createCell(3);
			}

			visibleCell.setCellStyle(this.defaultCellStyle);
			visibleCell.setCellValue(entry.visible);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

	public void createSheetRecordTypeVisibility(List<RecordTypeVisibility> listRecordTypeVisibilityMetadata) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet("Record Type Visibilities");
		
		String[] columnsList = {"profileName", "default", "personAccountDefault", "recordType"};
		
		this.createMetadataHeader(workSheet, "Metadata Record Type Visibilities", columnsList.length, 0, 0);
		this.createSheetHeader(workSheet, columnsList, 1, 0);
		int rowId = 2;
		listRecordTypeVisibilityMetadata.sort(Comparator.naturalOrder());
		for(RecordTypeVisibility entry : listRecordTypeVisibilityMetadata) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowId);
			}
			
			org.apache.poi.xssf.usermodel.XSSFCell profileNameCell = bodyRow.getCell(0);

			if(profileNameCell == null) {
				profileNameCell = bodyRow.createCell(0);
			}

			profileNameCell.setCellStyle(this.defaultCellStyle);
			profileNameCell.setCellValue(entry.profileName);

			org.apache.poi.xssf.usermodel.XSSFCell defaultCell = bodyRow.getCell(1);

			if(defaultCell == null) {
				defaultCell = bodyRow.createCell(1);
			}

			defaultCell.setCellStyle(this.defaultCellStyle);
			defaultCell.setCellValue(entry.defaultFlag);

			org.apache.poi.xssf.usermodel.XSSFCell personAccountDefaultCell = bodyRow.getCell(2);

			if(personAccountDefaultCell == null) {
				personAccountDefaultCell = bodyRow.createCell(2);
			}

			personAccountDefaultCell.setCellStyle(this.defaultCellStyle);
			personAccountDefaultCell.setCellValue(entry.personAccountDefault);
			
			org.apache.poi.xssf.usermodel.XSSFCell recordTypeCell = bodyRow.getCell(3);

			if(recordTypeCell == null) {
				recordTypeCell = bodyRow.createCell(3);
			}

			recordTypeCell.setCellStyle(this.defaultCellStyle);
			recordTypeCell.setCellValue(entry.recordType);

			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}

}
