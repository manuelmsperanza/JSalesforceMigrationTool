package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CheckUtilityAppDataModel {
	
	private static final Logger logger = LogManager.getLogger(CheckUtilityAppDataModel.class);
	
	private org.apache.poi.xssf.usermodel.XSSFWorkbook dmWb;
	private File orgExcel;
	private FileInputStream orgExcelFis = null;
	private FileOutputStream orgExcelFos = null;
	private org.apache.poi.xssf.usermodel.XSSFWorkbook orgWb;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle existingCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle newCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle errorCellStyle;
	
	private org.apache.poi.ss.usermodel.Sheet objTranslationSheet;
	private int objTranslationSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet fieldSheet;
	private int fieldSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet fieldTranslationSheet;
	private int fieldTranslationSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet fieldValueSetsSheet;
	private int fieldValueSetsSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet fieldValueSetTranslationSheet;
	private int fieldValueSetTranslationSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet globalValueSetSheet;
	private int globalValueSetSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet globalValueSetTranslationSheet;
	private int globalValueSetTranslationSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet standardValueSetSheet;
	private int standardValueSetSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet standardValueSetTranslationSheet;
	private int standardValueSetTranslationSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet labelSheet;
	private int labelSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet labelSheetTranslation;
	private int labelSheetTranslationLastRow;
	private org.apache.poi.ss.usermodel.Sheet recordTypeSheet;
	private int recordTypeSheetLastRow;
	private org.apache.poi.ss.usermodel.Sheet recordTypeTranslationSheet;
	private int recordTypeTranslationSheetLastRow;
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		
		List<String> listSources = new ArrayList<String>();
		listSources.add("EAP2");
		listSources.add("UAPP");
		//listSources.add("TEA");
		
		//Read Excel
		String dmExcelPath = "C:\\Users\\msperanza\\OneDrive - Engineering Ingegneria Informatica S.p.A\\Documenti condivisi\\General\\Analisi\\UAPP Data Model v01.xlsx";
		String orgExcelPath = "wrtsuapp.xlsx";
		//String orgExcelPath = "eng.msperanza - teaspa.it.release1.xlsx";
		//String orgExcelPath = "eng.msperanza - teaspa.it.dev.xlsx";
		
		CheckUtilityAppDataModel checkUAppDm = new CheckUtilityAppDataModel();
		try {
			checkUAppDm.init(orgExcelPath, dmExcelPath);
			checkUAppDm.scanDataModel(listSources);
			checkUAppDm.write();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			checkUAppDm.close();
		}
		
		//Read retrieved metadata
		
		logger.traceExit();
	}
	
	public void init(String orgExcelPath, String dmExcelPath) throws IOException {
		
		logger.traceEntry();
		
		this.orgExcel = new File(orgExcelPath);
		this.orgExcelFis = new FileInputStream(this.orgExcel);
		
		this.orgWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(this.orgExcelFis);
		
		this.existingCellStyle = this.orgWb.createCellStyle();	
		byte[] yellowRgb = new byte[3];
		yellowRgb[0] = (byte) 255; // red
		yellowRgb[1] = (byte) 255; // green
		yellowRgb[2] = (byte) 0; // blue
		org.apache.poi.xssf.usermodel.XSSFColor yellowForeGroundcolor = new org.apache.poi.xssf.usermodel.XSSFColor(yellowRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FFFF00
		this.existingCellStyle.setFillForegroundColor(yellowForeGroundcolor);
		this.existingCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		this.existingCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.existingCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.existingCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.existingCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		
		this.newCellStyle = this.orgWb.createCellStyle();
		
		byte[] greenRgb = new byte[3];
		greenRgb[0] = (byte) 0; // red
		greenRgb[1] = (byte) 255; // green
		greenRgb[2] = (byte) 0; // blue
		org.apache.poi.xssf.usermodel.XSSFColor greenForeGroundcolor = new org.apache.poi.xssf.usermodel.XSSFColor(greenRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #00FF00
		this.newCellStyle.setFillForegroundColor(greenForeGroundcolor);
		this.newCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		this.newCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.newCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.newCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.newCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		
		this.errorCellStyle = this.orgWb.createCellStyle();
		
		byte[] redRgb = new byte[3];
		redRgb[0] = (byte) 255; // red
		redRgb[1] = (byte) 0; // green
		redRgb[2] = (byte) 0; // blue
		org.apache.poi.xssf.usermodel.XSSFColor redForeGroundcolor = new org.apache.poi.xssf.usermodel.XSSFColor(redRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FF0000
		this.errorCellStyle.setFillForegroundColor(redForeGroundcolor);
		this.errorCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		this.errorCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		org.apache.poi.xssf.usermodel.XSSFFont boldFont= this.orgWb.createFont();
		boldFont.setBold(true);
		this.errorCellStyle.setFont(boldFont);
		
		this.objTranslationSheet = this.orgWb.getSheet("Object translation");
		this.objTranslationSheetLastRow = (objTranslationSheet == null ? -1 : objTranslationSheet.getLastRowNum());
		this.fieldSheet = this.orgWb.getSheet("Fields");
		this.fieldSheetLastRow = (this.fieldSheet == null ? -1 : this.fieldSheet.getLastRowNum());
		this.fieldTranslationSheet = this.orgWb.getSheet("Fields translation");
		this.fieldTranslationSheetLastRow = (this.fieldTranslationSheet == null ? -1 : this.fieldTranslationSheet.getLastRowNum());
		this.fieldValueSetsSheet = this.orgWb.getSheet("Fields valueSets");
		this.fieldValueSetsSheetLastRow = (this.fieldValueSetsSheet == null ? -1 : this.fieldValueSetsSheet.getLastRowNum());
		this.fieldValueSetTranslationSheet = this.orgWb.getSheet("Fields valueSets translations");
		this.fieldValueSetTranslationSheetLastRow = (this.fieldValueSetTranslationSheet == null ? -1 : this.fieldValueSetTranslationSheet.getLastRowNum());
		this.globalValueSetSheet = this.orgWb.getSheet("Global valueSets");
		this.globalValueSetSheetLastRow = (this.globalValueSetSheet == null ? -1 : this.globalValueSetSheet.getLastRowNum());
		this.globalValueSetTranslationSheet = this.orgWb.getSheet("Global valueSets translation");
		this.globalValueSetTranslationSheetLastRow = (this.globalValueSetTranslationSheet == null ? -1 : this.globalValueSetTranslationSheet.getLastRowNum());
		this.standardValueSetSheet = this.orgWb.getSheet("Standard valueSets");
		this.standardValueSetSheetLastRow = (this.standardValueSetSheet == null ? -1 : this.standardValueSetSheet.getLastRowNum());
		this.standardValueSetTranslationSheet = this.orgWb.getSheet("Standard valueSets translation");
		this.standardValueSetTranslationSheetLastRow = (this.standardValueSetTranslationSheet == null ? -1 : this.standardValueSetTranslationSheet.getLastRowNum());
		this.labelSheet = this.orgWb.getSheet("Labels");
		this.labelSheetLastRow = (this.labelSheet == null ? -1 : this.labelSheet.getLastRowNum());
		this.labelSheetTranslation = this.orgWb.getSheet("Label translation");
		this.labelSheetTranslationLastRow = (this.labelSheetTranslation == null ? -1 : this.labelSheetTranslation.getLastRowNum());
		this.recordTypeSheet = this.orgWb.getSheet("RecordTypes");
		this.recordTypeSheetLastRow = (this.recordTypeSheet == null ? -1 : this.recordTypeSheet.getLastRowNum());
		this.recordTypeTranslationSheet = this.orgWb.getSheet("RecordTypes translation");
		this.recordTypeTranslationSheetLastRow = (this.recordTypeTranslationSheet == null ? -1 : this.recordTypeTranslationSheet.getLastRowNum());
		
		this.dmWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(dmExcelPath);
		logger.traceExit();
	}
	
	public void write() throws IOException {
		logger.traceEntry();
		logger.debug("Writing " + this.orgExcel.getName());
		this.orgExcelFos  = new FileOutputStream(this.orgExcel);
		this.orgWb.write(this.orgExcelFos);
		logger.traceExit();
	}


	public void close() {
		logger.traceEntry();
		if(this.orgExcelFis != null) {
			try {
				this.orgExcelFis.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(this.orgExcelFos != null) {
			try {
				this.orgExcelFos.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		if(this.orgWb != null) {
			try {
				this.orgWb.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		logger.traceExit();
	}

	public static Map<String, File> getFileMap(String path) {
		logger.traceEntry();
		Map<String, File> fileMap = new HashMap<String, File>();
		
		File fileMapDir = new File(path);
		for(File curFile : fileMapDir.listFiles()) {
			if(curFile.isFile()) {
				fileMap.put(curFile.getName(), curFile);
			}
		}
		
		return logger.traceExit(fileMap);
	}
	
	public static String getCellValue(org.apache.poi.ss.usermodel.Row inRow, int position) {
		logger.traceEntry();
		org.apache.poi.ss.usermodel.Cell workingCell = inRow.getCell(position);
		
		String workingCellStringValue = null;
		if(workingCell != null) {
			if(workingCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
				workingCellStringValue = Double.toString(workingCell.getNumericCellValue());
			} else {
				workingCellStringValue = workingCell.getStringCellValue();
			}
			
		}
			
		return logger.traceExit(workingCellStringValue);
	}

	
	public void scanDataModel(List<String> listSources) {
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Sheet> wsIter = this.dmWb.sheetIterator();
		
		while(wsIter.hasNext()) {
			org.apache.poi.ss.usermodel.Sheet dmSheet = wsIter.next();
			
			switch(dmSheet.getSheetName().toLowerCase()) {
			case "cover":
				break;
			case "lov":
				this.checkLov(dmSheet, listSources);
				break;
			case "recordtype":
				break;
			case "labels":
				break;
			default:
				this.checkEntity(dmSheet, listSources);
			}
			
			
		}
		
		logger.traceExit();
		
	}

	private void checkLov(org.apache.poi.ss.usermodel.Sheet dmSheet, List<String> listSources) {
		
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
		org.apache.poi.ss.usermodel.Row dmColsRow = null;
		
		if(dmRowIter.hasNext()) {
			dmColsRow = dmRowIter.next();	
		}
			
		if(dmColsRow == null) {
			logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		} 
		int objectPos = -1;
		int fieldPos = -1;
		int valuePos = -1;
		int statusPos = -1;
		int sourcePos = -1;
		int labelPos = -1;
		int italianPos = -1;
		
		Iterator<org.apache.poi.ss.usermodel.Cell> dmColsCellIter = dmColsRow.cellIterator();
		while(dmColsCellIter.hasNext()) {
			org.apache.poi.ss.usermodel.Cell collCell = dmColsCellIter.next();
			if(collCell != null) {
				switch(collCell.getStringCellValue().toLowerCase()) {
				case "object":
					objectPos = collCell.getColumnIndex();
					break;
				case "name":
					fieldPos = collCell.getColumnIndex();
					break;
				case "value":
					valuePos = collCell.getColumnIndex();
					break;
				case "status":
					statusPos = collCell.getColumnIndex();
					break;
				case "source":
					sourcePos = collCell.getColumnIndex();
					break;
				case "english":
					labelPos = collCell.getColumnIndex();
					break;
				case "italian":
					italianPos = collCell.getColumnIndex();
					break;
				}
			}
		}
		boolean colsError = false;
		if(objectPos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Object column");
			colsError = true;
		}
		
		if(fieldPos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Name column");
			colsError = true;
		}
		
		if(valuePos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Value column");
			colsError = true;
		}
		
		if(statusPos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Status column");
			colsError = true;
		}
		
		if(sourcePos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Source column");
			colsError = true;
		}
		
		if(labelPos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing English column");
			colsError = true;
		}
		
		if(italianPos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing Italian column");
			colsError = true;
		}
		
		
		if(colsError) {
			logger.error("Missing useful colum(s). Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		}
		
		while(dmRowIter.hasNext()) {
			org.apache.poi.ss.usermodel.Row dmRow = dmRowIter.next();
			org.apache.poi.ss.usermodel.Cell sourceCell = dmRow.getCell(sourcePos);
			String sourceValue = (sourceCell == null) ? null : sourceCell.getStringCellValue();
			if(StringUtils.isBlank(sourceValue)) {
				logger.error("LOV: Source is empty for row #" + dmRow.getRowNum());
				continue;
			}
			if(listSources.contains(sourceValue)) {
				boolean rowHasError = false;
				
				String entityName = getCellValue(dmRow, objectPos);
				if(StringUtils.isBlank(entityName)) {
					logger.error("LOV: Object is empty for row " + dmRow.getRowNum());
					rowHasError = true;
				}
				
				String fieldName = getCellValue(dmRow, fieldPos);
				if(StringUtils.isBlank(fieldName)) {
					logger.error("LOV: Name is empty for row " + dmRow.getRowNum());
					rowHasError = true;
				}
				String fieldValue = getCellValue(dmRow, valuePos);
				if(StringUtils.isBlank(fieldValue)) {
					logger.error("LOV: Value is empty for row " + dmRow.getRowNum());
					rowHasError = true;
				}
				String fieldStatus = getCellValue(dmRow, statusPos);
				if(StringUtils.isBlank(fieldStatus)) {
					logger.error("LOV: Status is empty for row " + dmRow.getRowNum());
					rowHasError = true;
				}
				
				String fieldLabel = getCellValue(dmRow, labelPos);
				if(StringUtils.isBlank(fieldLabel)) {
					logger.error("LOV: English is empty for row " + dmRow.getRowNum());
					rowHasError = true;
				}
				
				if(rowHasError) {
					logger.error("LOV: missing useful value(s). Skip row #" + dmRow.getRowNum());
				} else {
					
					String fieldItalianTranslation = getCellValue(dmRow, italianPos);
					
					if("*".equals(entityName)) {
						boolean insertGlobalValueSet = true;
						
						Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetsIter = this.globalValueSetSheet.rowIterator();
						while(orgGlobalValueSetsIter.hasNext()) {
							org.apache.poi.ss.usermodel.Row orgGlobalValueSetRow = orgGlobalValueSetsIter.next();
							String orgGlobalVsName = getCellValue(orgGlobalValueSetRow, 0);
							if(fieldName.equals(orgGlobalVsName)) {
								String orgGlobalVsValue = getCellValue(orgGlobalValueSetRow, 1);
								if(fieldValue.equals(orgGlobalVsValue)) {
									org.apache.poi.ss.usermodel.Cell orgGlobalVsLabelCell = null;
									String orgGlobalVsLabel = (orgGlobalVsLabelCell = orgGlobalValueSetRow.getCell(3)) == null ? null : orgGlobalVsLabelCell.getStringCellValue();
									
									if(fieldLabel.equals(orgGlobalVsLabel)) {
										orgGlobalVsLabelCell.setCellStyle(this.existingCellStyle);
										insertGlobalValueSet = false;
										if(!"active".equalsIgnoreCase(fieldStatus)) {
											logger.error("Global Value Set " + fieldName + "." + fieldValue + ": Status is not Active");
										}
									} else {
										orgGlobalVsLabelCell.setCellStyle(errorCellStyle);
										logger.error("Global Value Set " + fieldName + "." + fieldValue + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgGlobalVsLabel);
									}
								}
							}
						}
						
						if(insertGlobalValueSet) {
							logger.error("Global Value Set " + fieldName + "." + fieldValue + ": value missing. Status: " + fieldStatus + " Source: " + sourceValue);
							org.apache.poi.ss.usermodel.Row newGlobalValueSet = this.globalValueSetSheet.createRow(++this.globalValueSetSheetLastRow);
							newGlobalValueSet.createCell(0).setCellValue(fieldName);
							newGlobalValueSet.createCell(1).setCellValue(fieldValue);
							newGlobalValueSet.createCell(3).setCellValue(fieldLabel);
							newGlobalValueSet.createCell(4).setCellValue(fieldStatus);
							newGlobalValueSet.createCell(5).setCellValue(sourceValue);
							newGlobalValueSet.setRowStyle(this.newCellStyle);
						}
						
						boolean insertGlobalValueSetTranslation = !StringUtils.isBlank(fieldItalianTranslation);
						Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetTranslationIter = this.globalValueSetTranslationSheet.rowIterator();
						while(orgGlobalValueSetTranslationIter.hasNext()) {
							org.apache.poi.ss.usermodel.Row orgGlobalValueSetTranslationRow = orgGlobalValueSetTranslationIter.next();
							String orgGlobalVsName = getCellValue(orgGlobalValueSetTranslationRow, 0);
							if((fieldName + "-it").equals(orgGlobalVsName)) {
								String orgGlobalVsValue = getCellValue(orgGlobalValueSetTranslationRow, 1);
								if(fieldLabel.equals(orgGlobalVsValue)) {
									org.apache.poi.ss.usermodel.Cell orgGlobalVsLabelCell = null;
									String orgGlobalVsLabel = (orgGlobalVsLabelCell = orgGlobalValueSetTranslationRow.getCell(2)) == null ? null : orgGlobalVsLabelCell.getStringCellValue();
									
									if(StringUtils.isBlank(fieldItalianTranslation)) {
										if(!StringUtils.isBlank(orgGlobalVsLabel)) {
											insertGlobalValueSetTranslation = true;
											orgGlobalVsLabelCell.setCellStyle(this.errorCellStyle);
											logger.error("Missing Global Value Set italian Translation in data model design for " + fieldName + "." + fieldLabel + ": " + orgGlobalVsLabel);
											
										}
									} else {
									
										if(fieldItalianTranslation.equals(orgGlobalVsLabel)) {
											orgGlobalVsLabelCell.setCellStyle(this.existingCellStyle);
											insertGlobalValueSetTranslation = false;
											if(!"active".equalsIgnoreCase(fieldStatus)) {
												logger.error("Global Value Set italian Translation " + fieldName + "." + fieldLabel + ": Status is not Active");
											}
										} else {
											insertGlobalValueSetTranslation = true;
											orgGlobalVsLabelCell.setCellStyle(this.errorCellStyle);
											logger.error("Global Value Set " + fieldName + "." + fieldLabel + ": italian Label Translation mismatch. Data Model: " + fieldItalianTranslation + " Org: " + orgGlobalVsLabel);
										}
									}
								}
							}
						}
						
						if(insertGlobalValueSetTranslation) {
							logger.error("Global Value Set " + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
							org.apache.poi.ss.usermodel.Row newGlobalValueSetTranslation = this.globalValueSetTranslationSheet.createRow(++this.globalValueSetTranslationSheetLastRow);
							newGlobalValueSetTranslation.createCell(0).setCellValue(fieldName + "-it");
							newGlobalValueSetTranslation.createCell(1).setCellValue(fieldLabel);
							newGlobalValueSetTranslation.createCell(2).setCellValue(fieldItalianTranslation);
							newGlobalValueSetTranslation.createCell(3).setCellValue(fieldStatus);
							newGlobalValueSetTranslation.createCell(4).setCellValue(sourceValue);
							newGlobalValueSetTranslation.setRowStyle(this.newCellStyle);
						}
						
					} else {
						boolean insertFieldValueSet = true;
						Iterator<org.apache.poi.ss.usermodel.Row> orgFieldValueSetsIter = this.fieldValueSetsSheet.rowIterator();
						while(orgFieldValueSetsIter.hasNext()) {
							
							org.apache.poi.ss.usermodel.Row orgFieldValueSetRow = orgFieldValueSetsIter.next();
							
							String orgFieldFilename = getCellValue(orgFieldValueSetRow, 0);
							
							if(entityName.equals(orgFieldFilename)) {
								String orgFieldName = getCellValue(orgFieldValueSetRow, 1);
								if(fieldName.equals(orgFieldName)) {
									String orgFieldValue = getCellValue(orgFieldValueSetRow, 3);
									if(fieldValue.equals(orgFieldValue)) {
										org.apache.poi.ss.usermodel.Cell orgFieldLabelCell = null;
										String orgFieldLabel = (orgFieldLabelCell = orgFieldValueSetRow.getCell(5)) == null ? null : orgFieldLabelCell.getStringCellValue();
										
										if(fieldLabel.equals(orgFieldLabel)) {
											orgFieldLabelCell.setCellStyle(this.existingCellStyle);
											insertFieldValueSet = false;
											if(!"active".equalsIgnoreCase(fieldStatus)) {
												logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": Status is not Active");
											}
										} else {
											orgFieldLabelCell.setCellStyle(this.errorCellStyle);
											logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgFieldLabel);
										}
										
									}
								}
							}
						}
						
						if(insertFieldValueSet) {
							logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value missing. Status: " + fieldStatus + " Source: " + sourceValue);
							org.apache.poi.ss.usermodel.Row newFieldValueSet = this.fieldValueSetsSheet.createRow(++this.fieldValueSetsSheetLastRow);
							newFieldValueSet.createCell(0).setCellValue(entityName);
							newFieldValueSet.createCell(1).setCellValue(fieldName);
							newFieldValueSet.createCell(3).setCellValue(fieldValue);
							newFieldValueSet.createCell(5).setCellValue(fieldLabel);
							newFieldValueSet.createCell(6).setCellValue(fieldStatus);
							newFieldValueSet.createCell(7).setCellValue(sourceValue);
							newFieldValueSet.setRowStyle(this.newCellStyle);
						}
						
						boolean insertValueSetTranslation = !StringUtils.isBlank(fieldItalianTranslation);
						Iterator<org.apache.poi.ss.usermodel.Row> orgValueSetTranslationIter = this.fieldValueSetTranslationSheet.rowIterator();
						while(orgValueSetTranslationIter.hasNext()) {
							org.apache.poi.ss.usermodel.Row orgValueSetTranslationRow = orgValueSetTranslationIter.next();
							String orgEntityName = getCellValue(orgValueSetTranslationRow, 0);
							if((entityName + "-it").equals(orgEntityName)) {
								String orgFieldName = getCellValue(orgValueSetTranslationRow, 2);
								if(fieldName.equals(orgFieldName)) {
									String orgFieldValue = getCellValue(orgValueSetTranslationRow, 3);
									if(fieldLabel.equals(orgFieldValue)) {
										org.apache.poi.ss.usermodel.Cell orgValueTranslationCell = null;
										String orgValueTranslation = (orgValueTranslationCell = orgValueSetTranslationRow.getCell(4)) == null ? null : orgValueTranslationCell.getStringCellValue();
										
										if(StringUtils.isBlank(fieldItalianTranslation)) {
											if(!StringUtils.isBlank(orgValueTranslation)) {
												insertValueSetTranslation = true;
												orgValueTranslationCell.setCellStyle(this.errorCellStyle);
												logger.error("Missing Value Set Translation in data model design for " + entityName + "." + fieldName + "." + fieldLabel + ": " + orgValueTranslation);
												
											}
										} else {
											
											if(fieldItalianTranslation.equals(orgValueTranslation)) {
												orgValueTranslationCell.setCellStyle(this.existingCellStyle);
												insertValueSetTranslation = false;
												if(!"active".equalsIgnoreCase(fieldStatus)) {
													logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Status is not Active");
												}
											} else {
												insertValueSetTranslation = true;
												orgValueTranslationCell.setCellStyle(this.errorCellStyle);
												logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Label Translation mismatch. Data Model: " + fieldItalianTranslation + " Org: " + orgValueTranslation);
											}
										}
									}
								}
							}
						}
						
						if(insertValueSetTranslation) {
							logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
							org.apache.poi.ss.usermodel.Row newFieldValueSetTranslation = this.fieldValueSetTranslationSheet.createRow(++this.fieldValueSetTranslationSheetLastRow);
							newFieldValueSetTranslation.createCell(0).setCellValue(entityName + "-it");
							newFieldValueSetTranslation.createCell(2).setCellValue(fieldName);
							newFieldValueSetTranslation.createCell(3).setCellValue(fieldLabel);
							newFieldValueSetTranslation.createCell(4).setCellValue(fieldItalianTranslation);
							newFieldValueSetTranslation.createCell(5).setCellValue(fieldStatus);
							newFieldValueSetTranslation.createCell(6).setCellValue(sourceValue);
							newFieldValueSetTranslation.setRowStyle(this.newCellStyle);
						}
					}
				}
			}
		}
		
		logger.traceExit();
	}
	
	private void checkEntity(org.apache.poi.ss.usermodel.Sheet dmSheet, List<String> listSources) {
		
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
		org.apache.poi.ss.usermodel.Row dmHeadRow = null;
		org.apache.poi.ss.usermodel.Row dmColsRow = null;
		
		if(dmRowIter.hasNext()) {
			dmHeadRow = dmRowIter.next();	
		}
		if(dmHeadRow == null) {
			logger.warn("Missing header. Skip " + dmSheet.getSheetName() + " sheet.");
		} else {
			org.apache.poi.ss.usermodel.Cell entityNameCell = dmHeadRow.getCell(0);
			String entityName = (entityNameCell == null ? null : entityNameCell.getStringCellValue());
			if(StringUtils.isBlank(entityName)) {
				logger.warn("Missing entity name. Skip " + dmSheet.getSheetName() + " sheet.");
				logger.traceExit();
				return;
			}
			logger.debug("entityName: " + entityName);
			if(dmRowIter.hasNext()) {
				dmColsRow = dmRowIter.next();	
			}
				
			if(dmColsRow == null) {
				logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
				logger.traceExit();
				return;
			} 
				
			int namePos = -1;
			int labelPos = -1;
			int statusPos = -1;
			int sourcePos = -1;
			int italianPos = -1;
			int valueSetNamePos = -1;
			
			Iterator<org.apache.poi.ss.usermodel.Cell> dmColsCellIter = dmColsRow.cellIterator();
			while(dmColsCellIter.hasNext()) {
				org.apache.poi.ss.usermodel.Cell collCell = dmColsCellIter.next();
				if(collCell != null) {
					switch(collCell.getStringCellValue().toLowerCase()) {
					case "name":
						namePos = collCell.getColumnIndex();
						break;
					case "label":
						labelPos = collCell.getColumnIndex();
						break;
					case "status":
						statusPos = collCell.getColumnIndex();
						break;
					case "source":
						sourcePos = collCell.getColumnIndex();
						break;
					case "italian":
						italianPos = collCell.getColumnIndex();
						break;
					case "valuesetname":
						valueSetNamePos = collCell.getColumnIndex();
						break;
					}
				}
			}
			boolean colsError = false;
			if(namePos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing name column");
				colsError = true;
			}
			
			if(labelPos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing label column");
				colsError = true;
			}
			
			if(statusPos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing Status column");
				colsError = true;
			}
			
			if(sourcePos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing Source column");
				colsError = true;
			}
			
			if(italianPos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing Italian column");
				colsError = true;
			}
			
			if(valueSetNamePos == -1) {
				logger.error(dmSheet.getSheetName() + ": missing valueSetName column");
				colsError = true;
			}
			
			if(colsError) {
				logger.error("Missing useful colum(s). Skip " + dmSheet.getSheetName() + " sheet.");
				logger.traceExit();
				return;
			}
			
			org.apache.poi.ss.usermodel.Cell entityNameItalianCell = dmHeadRow.getCell(italianPos);
			String entityNameItalian = entityNameCell == null ? null : entityNameItalianCell.getStringCellValue();
			logger.debug("entityNameItalian: " + entityNameItalian);
			
			if(this.objTranslationSheet != null) {
				Iterator<org.apache.poi.ss.usermodel.Row> objTranslationRowIter = this.objTranslationSheet.rowIterator();
				
				boolean insertTranslation = !StringUtils.isBlank(entityNameItalian);
				while(objTranslationRowIter.hasNext()) {
					org.apache.poi.ss.usermodel.Row objTranslationRow = objTranslationRowIter.next();
					
					org.apache.poi.ss.usermodel.Cell fileNameCell = null;
					if((fileNameCell = objTranslationRow.getCell(0)) != null) {
						if((entityName + "-it").equals(fileNameCell.getStringCellValue())) {
							
							org.apache.poi.ss.usermodel.Cell valueCell = objTranslationRow.getCell(2);
							String orgObjectTranslation = (valueCell == null ? null : valueCell.getStringCellValue());
							logger.debug("orgObjectTranslation: " + orgObjectTranslation);
							if(StringUtils.isBlank(entityNameItalian)) {
								if(!StringUtils.isBlank(orgObjectTranslation)) {
									insertTranslation = true;
									logger.error("Missing object translation in data model design for " + entityName + ": " + orgObjectTranslation);
									valueCell.setCellStyle(this.errorCellStyle);
								}
							} else {
								
								if(entityNameItalian.equals(orgObjectTranslation)) {
									insertTranslation = false;
									objTranslationRow.setRowStyle(this.existingCellStyle);
								} else {
									if(valueCell == null) {
										valueCell = objTranslationRow.createCell(2);
									}
									valueCell.setCellStyle(this.errorCellStyle);
									logger.error("Mismatch in object translation. Data Model: " + entityNameItalian + " Org: " + orgObjectTranslation);
									insertTranslation = true;
								}
								
							}
							
						}
					}
					
				}
				
				if(insertTranslation) {
					logger.error(entityName + ": italian translation missing.");
					org.apache.poi.ss.usermodel.Row newObjTranslationRow = objTranslationSheet.createRow(++objTranslationSheetLastRow);
					newObjTranslationRow.setRowStyle(this.newCellStyle);
					newObjTranslationRow.createCell(0).setCellValue(entityName + "-it");
					newObjTranslationRow.createCell(2).setCellValue(entityNameItalian);
				}
				
			}
			
			while(dmRowIter.hasNext()) {
				org.apache.poi.ss.usermodel.Row dmRow = dmRowIter.next();
				org.apache.poi.ss.usermodel.Cell sourceCell = dmRow.getCell(sourcePos);
				String sourceValue = null;
				if(sourceCell == null || StringUtils.isBlank((sourceValue = sourceCell.getStringCellValue()))) {
					logger.error(entityName + ": source is empty for row " + dmRow.getRowNum());
					continue;
				}
				if(listSources.contains(sourceValue)) {
					boolean rowHasError = false;
					String fieldName = getCellValue(dmRow, namePos);
					logger.debug("fieldName: " + fieldName);
					if(StringUtils.isBlank(fieldName)) {
						logger.error(entityName + ": name is empty for row " + dmRow.getRowNum());
						rowHasError = true;
					}
					String fieldLabel = getCellValue(dmRow, labelPos);
					logger.debug("fieldLabel: " + fieldLabel);
					if(StringUtils.isBlank(fieldLabel)) {
						logger.error(entityName + ": label is empty for row " + dmRow.getRowNum());
						rowHasError = true;
					}
					String fieldStatus = getCellValue(dmRow, statusPos);
					logger.debug("fieldStatus: " + fieldStatus);
					if(StringUtils.isBlank(fieldStatus)) {
						logger.error(entityName + ": status is empty for row " + dmRow.getRowNum());
						rowHasError = true;
					}
					
					if(rowHasError) {
						logger.error(entityName + ": missing useful value(s). Skip row #" + dmRow.getRowNum());
						continue;
					}
				
					String fieldItalianTranslation = getCellValue(dmRow, italianPos);
					logger.debug("fieldItalianTranslation: " + fieldItalianTranslation);
					String fieldValueSetName = getCellValue(dmRow, valueSetNamePos);
					logger.debug("fieldValueSetName: " + fieldValueSetName);
					
					boolean insertField = true;
					Iterator<org.apache.poi.ss.usermodel.Row> orgFieldsIter = fieldSheet.rowIterator();
					while(orgFieldsIter.hasNext()) {
						
						org.apache.poi.ss.usermodel.Row orgFieldRow = orgFieldsIter.next();
						
						String orgFieldFilename = getCellValue(orgFieldRow, 0);
						
						if(entityName.equals(orgFieldFilename)) {
						
							String orgFieldFullname = getCellValue(orgFieldRow, 1);
							if(fieldName.equals(orgFieldFullname)) {
								
								insertField = false;
								org.apache.poi.ss.usermodel.Cell orgFieldLabelCell = null;
								String orgFieldLabel = (orgFieldLabelCell = orgFieldRow.getCell(2)) == null ? null : orgFieldLabelCell.getStringCellValue();
								logger.debug("orgFieldLabel: " + orgFieldLabel);
								if(fieldLabel.equals(orgFieldLabel)) {
									orgFieldLabelCell.setCellStyle(existingCellStyle);
									if(!"active".equalsIgnoreCase(fieldStatus)) {
										logger.error(entityName + "." + fieldName + ": Status is not Active");
									}
								} else {
									insertField = true;
									logger.error(entityName + "." + fieldName + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgFieldLabel);
									orgFieldLabelCell.setCellStyle(errorCellStyle);
								}
								
								org.apache.poi.ss.usermodel.Cell orgFieldValueSetsNameCell = null;
								String orgFieldValueSetsName = (orgFieldValueSetsNameCell = orgFieldRow.getCell(21)) == null ? null : orgFieldValueSetsNameCell.getStringCellValue();
								logger.debug("orgFieldValueSetsName: " + orgFieldValueSetsName);
								if(StringUtils.isBlank(fieldValueSetName)) {
									if(!StringUtils.isBlank(orgFieldValueSetsName)) {
										if(orgFieldValueSetsNameCell == null) {
											orgFieldValueSetsNameCell = orgFieldRow.createCell(21);
										}
										orgFieldValueSetsNameCell.setCellStyle(errorCellStyle);
										logger.error("Missing valueSetName in data model design for " + entityName  + "." + fieldName + ": " + orgFieldValueSetsName);
									}
								} else {
									if(fieldValueSetName.equals(orgFieldValueSetsName)) {
										orgFieldValueSetsNameCell.setCellStyle(existingCellStyle);
									} else {
										insertField = true;
										logger.error(entityName + "." + fieldName + ": valueSetName mismatch. Data Model: " + fieldValueSetName + " Org: " + orgFieldValueSetsName);
										orgFieldValueSetsNameCell.setCellStyle(errorCellStyle);
									}
								}
								
							}
						}
						
					}
					
					if(insertField) {
						logger.error(entityName + "." + fieldName + ": field missing. Status: " + fieldStatus + " Source: " + sourceValue);
						org.apache.poi.ss.usermodel.Row newFieldRow = fieldSheet.createRow(++fieldSheetLastRow);
						newFieldRow.setRowStyle(newCellStyle);
						newFieldRow.createCell(0).setCellValue(entityName);
						newFieldRow.createCell(1).setCellValue(fieldName);
						newFieldRow.createCell(2).setCellValue(fieldLabel);
						if(!StringUtils.isBlank(fieldValueSetName)) {
							newFieldRow.createCell(21).setCellValue(fieldValueSetName);
						}
						newFieldRow.createCell(23).setCellValue(fieldStatus);
						newFieldRow.createCell(24).setCellValue(sourceValue);
						
					}
					
					boolean insertFieldTranslation = !StringUtils.isBlank(fieldItalianTranslation);
					Iterator<org.apache.poi.ss.usermodel.Row> orgFieldsTranslationIter = fieldTranslationSheet.rowIterator();
					while(orgFieldsTranslationIter.hasNext()) {
						org.apache.poi.ss.usermodel.Row orgFieldTranslationRow = orgFieldsTranslationIter.next();
						
						String orgEntityName = getCellValue(orgFieldTranslationRow, 0);
						if((entityName + "-it").equals(orgEntityName)) {
							
							String orgFieldFullname = getCellValue(orgFieldTranslationRow, 1);
							if(fieldName.equals(orgFieldFullname)) {
								insertFieldTranslation = false;
								org.apache.poi.ss.usermodel.Cell orgFieldLabelTranslationCell = null;
								String orgFieldLabelTranslation = (orgFieldLabelTranslationCell = orgFieldTranslationRow.getCell(2)) == null ? null : orgFieldLabelTranslationCell.getStringCellValue();
								logger.debug("orgFieldLabelTranslation: " + orgFieldLabelTranslation);
								if(StringUtils.isBlank(fieldItalianTranslation)) {
									if(!StringUtils.isBlank(orgFieldLabelTranslation)) {
										insertFieldTranslation = true;
										if(orgFieldLabelTranslationCell == null) {
											orgFieldLabelTranslationCell = orgFieldTranslationRow.createCell(2);
										}
										orgFieldLabelTranslationCell.setCellStyle(errorCellStyle);
										logger.error("Missing italian Label Translation in data model design for " + entityName  + "." + fieldName + ": " + orgFieldLabelTranslation);
									}
								} else {
									if(fieldItalianTranslation.equals(orgFieldLabelTranslation)) {
										orgFieldLabelTranslationCell.setCellStyle(existingCellStyle);
										
									} else {
										insertFieldTranslation = true;
										logger.error(entityName + "." + fieldName + ": Label italian Translation mismatch. Data Model: " + fieldItalianTranslation + " Org: " + orgFieldLabelTranslation);
										orgFieldLabelTranslationCell.setCellStyle(errorCellStyle);
									}
								}
							}
						}
						
					}
					
					if(insertFieldTranslation) {
						logger.error(entityName + "." + fieldName + ": italian field translation missing. Translation: " + fieldItalianTranslation + " Status: " + fieldStatus + " Source: " + sourceValue);
						org.apache.poi.ss.usermodel.Row newFieldTranslationRow = fieldTranslationSheet.createRow(++fieldTranslationSheetLastRow);
						newFieldTranslationRow.createCell(0).setCellValue(entityName + "-it");
						newFieldTranslationRow.createCell(1).setCellValue(fieldName);
						newFieldTranslationRow.createCell(2).setCellValue(fieldItalianTranslation);
						newFieldTranslationRow.createCell(8).setCellValue(fieldStatus);
						newFieldTranslationRow.createCell(9).setCellValue(sourceValue);
						newFieldTranslationRow.setRowStyle(newCellStyle);
					}
				}
			}
		}
		logger.traceExit();
	}
	
}
