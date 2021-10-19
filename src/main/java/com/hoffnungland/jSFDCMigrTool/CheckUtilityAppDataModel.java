package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
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
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;


public class CheckUtilityAppDataModel {
	
	private static final Logger logger = LogManager.getLogger(CheckUtilityAppDataModel.class);
	
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
		
		FileInputStream fis = null;
		FileOutputStream fileOut = null;
		org.apache.poi.xssf.usermodel.XSSFWorkbook orgWb = null;
		try {
			File orgExcel = new File(orgExcelPath);
			fis = new FileInputStream(orgExcel);
			
			orgWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(fis);
			
			org.apache.poi.xssf.usermodel.XSSFCellStyle existingCellStyle = orgWb.createCellStyle();
			
			byte[] yellowRgb = new byte[3];
			yellowRgb[0] = (byte) 255; // red
			yellowRgb[1] = (byte) 255; // green
			yellowRgb[2] = (byte) 0; // blue
			XSSFColor yellowForeGroundcolor = new XSSFColor(yellowRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FFFF00
			existingCellStyle.setFillForegroundColor(yellowForeGroundcolor);
			existingCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
			existingCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			existingCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			existingCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			existingCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			
			org.apache.poi.xssf.usermodel.XSSFCellStyle newCellStyle = orgWb.createCellStyle();
			
			byte[] greenRgb = new byte[3];
			greenRgb[0] = (byte) 0; // red
			greenRgb[1] = (byte) 255; // green
			greenRgb[2] = (byte) 0; // blue
			XSSFColor greenForeGroundcolor = new XSSFColor(greenRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #00FF00
			newCellStyle.setFillForegroundColor(greenForeGroundcolor);
			newCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
			newCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			newCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			newCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			newCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			
			org.apache.poi.xssf.usermodel.XSSFCellStyle errorCellStyle = orgWb.createCellStyle();
			
			byte[] redRgb = new byte[3];
			redRgb[0] = (byte) 255; // red
			redRgb[1] = (byte) 0; // green
			redRgb[2] = (byte) 0; // blue
			XSSFColor redForeGroundcolor = new XSSFColor(redRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FF0000
			errorCellStyle.setFillForegroundColor(redForeGroundcolor);
			errorCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
			errorCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			errorCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			errorCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			errorCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
			XSSFFont boldFont= orgWb.createFont();
			boldFont.setBold(true);
			errorCellStyle.setFont(boldFont);
			
			org.apache.poi.ss.usermodel.Sheet objTranslationSheet = orgWb.getSheet("Object translation");
			int objTranslationSheetLastRow = (objTranslationSheet == null ? -1 : objTranslationSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet fieldSheet = orgWb.getSheet("Fields");
			int fieldSheetLastRow = (fieldSheet == null ? -1 : fieldSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet fieldTranslationSheet = orgWb.getSheet("Field translation");
			int fieldTranslationSheetLastRow = (fieldTranslationSheet == null ? -1 : fieldTranslationSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet fieldValueSetsSheet = orgWb.getSheet("Fields valueSets");
			int fieldValueSetsSheetLastRow = (fieldValueSetsSheet == null ? -1 : fieldValueSetsSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet fieldValueSetTranslationSheet = orgWb.getSheet("Field valueSets translation");
			int fieldValueSetTranslationSheetLastRow = (fieldValueSetTranslationSheet == null ? -1 : fieldValueSetTranslationSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet globalValueSetSheet = orgWb.getSheet("Global ValueSet");
			int globalValueSetSheetLastRow = (globalValueSetSheet == null ? -1 : globalValueSetSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet globalValueSetTranslationSheet = orgWb.getSheet("Global ValueSet Translation");
			int globalValueSetTranslationSheetLastRow = (globalValueSetTranslationSheet == null ? -1 : globalValueSetTranslationSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet labelSheet = orgWb.getSheet("Labels");
			int labelSheetLastRow = (labelSheet == null ? -1 : labelSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet labelSheetTranslation = orgWb.getSheet("Label translation");
			int labelSheetTranslationLastRow = (labelSheetTranslation == null ? -1 : labelSheetTranslation.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet recordTypeSheet = orgWb.getSheet("RecordTypes");
			int recordTypeSheetLastRow = (recordTypeSheet == null ? -1 : recordTypeSheet.getLastRowNum());
			org.apache.poi.ss.usermodel.Sheet recordTypeTranslationSheet = orgWb.getSheet("RecordTypes translation");
			int recordTypeTranslationSheetLastRow = (recordTypeTranslationSheet == null ? -1 : recordTypeTranslationSheet.getLastRowNum());
			
			org.apache.poi.ss.usermodel.Workbook dmWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(dmExcelPath);
			Iterator<org.apache.poi.ss.usermodel.Sheet> wsIter = dmWb.sheetIterator();
			
			while(wsIter.hasNext()) {
				org.apache.poi.ss.usermodel.Sheet dmSheet = wsIter.next();
				
				Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
				org.apache.poi.ss.usermodel.Row dmHeadRow = null;
				org.apache.poi.ss.usermodel.Row dmColsRow = null;
				
				switch(dmSheet.getSheetName().toLowerCase()) {
				case "cover":
					break;
				case "lov":
					
					if(dmRowIter.hasNext()) {
						dmColsRow = dmRowIter.next();	
					}
						
					if(dmColsRow == null) {
						logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
					} else {
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
						} else {
							while(dmRowIter.hasNext()) {
								org.apache.poi.ss.usermodel.Row dmRow = dmRowIter.next();
								org.apache.poi.ss.usermodel.Cell sourceCell = dmRow.getCell(sourcePos);
								String sourceValue = null;
								if(sourceCell == null || StringUtils.isBlank((sourceValue = sourceCell.getStringCellValue()))) {
									logger.error("LOV: Source is empty for row " + dmRow.getRowNum());
								} else {
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
												
												Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetsIter = globalValueSetSheet.rowIterator();
												while(orgGlobalValueSetsIter.hasNext()) {
													org.apache.poi.ss.usermodel.Row orgGlobalValueSetRow = orgGlobalValueSetsIter.next();
													String orgGlobalVsName = getCellValue(orgGlobalValueSetRow, 0);
													if(fieldName.equals(orgGlobalVsName)) {
														String orgGlobalVsValue = getCellValue(orgGlobalValueSetRow, 1);
														if(fieldValue.equals(orgGlobalVsValue)) {
															org.apache.poi.ss.usermodel.Cell orgGlobalVsLabelCell = null;
															String orgGlobalVsLabel = (orgGlobalVsLabelCell = orgGlobalValueSetRow.getCell(3)) == null ? null : orgGlobalVsLabelCell.getStringCellValue();
															
															if(fieldLabel.equals(orgGlobalVsLabel)) {
																orgGlobalVsLabelCell.setCellStyle(existingCellStyle);
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
													org.apache.poi.ss.usermodel.Row newGlobalValueSet = globalValueSetSheet.createRow(++globalValueSetSheetLastRow);
													newGlobalValueSet.createCell(0).setCellValue(fieldName);
													newGlobalValueSet.createCell(1).setCellValue(fieldValue);
													newGlobalValueSet.createCell(3).setCellValue(fieldLabel);
													newGlobalValueSet.createCell(4).setCellValue(fieldStatus);
													newGlobalValueSet.createCell(5).setCellValue(sourceValue);
													newGlobalValueSet.setRowStyle(newCellStyle);
												}
												
												boolean insertGlobalValueSetTranslation = !StringUtils.isBlank(fieldItalianTranslation);
												Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetTranslationIter = globalValueSetTranslationSheet.rowIterator();
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
																	orgGlobalVsLabelCell.setCellStyle(errorCellStyle);
																	logger.error("Missing Global Value Set italian Translation in data model design for " + fieldName + "." + fieldLabel + ": " + orgGlobalVsLabel);
																	
																}
															} else {
															
																if(fieldItalianTranslation.equals(orgGlobalVsLabel)) {
																	orgGlobalVsLabelCell.setCellStyle(existingCellStyle);
																	insertGlobalValueSetTranslation = false;
																	if(!"active".equalsIgnoreCase(fieldStatus)) {
																		logger.error("Global Value Set italian Translation " + fieldName + "." + fieldLabel + ": Status is not Active");
																	}
																} else {
																	insertGlobalValueSetTranslation = true;
																	orgGlobalVsLabelCell.setCellStyle(errorCellStyle);
																	logger.error("Global Value Set " + fieldName + "." + fieldLabel + ": italian Label Translation mismatch. Data Model: " + fieldItalianTranslation + " Org: " + orgGlobalVsLabel);
																}
															}
														}
													}
												}
												
												if(insertGlobalValueSetTranslation) {
													logger.error("Global Value Set " + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
													org.apache.poi.ss.usermodel.Row newGlobalValueSetTranslation = globalValueSetTranslationSheet.createRow(++globalValueSetTranslationSheetLastRow);
													newGlobalValueSetTranslation.createCell(0).setCellValue(fieldName + "-it");
													newGlobalValueSetTranslation.createCell(1).setCellValue(fieldLabel);
													newGlobalValueSetTranslation.createCell(2).setCellValue(fieldItalianTranslation);
													newGlobalValueSetTranslation.createCell(3).setCellValue(fieldStatus);
													newGlobalValueSetTranslation.createCell(4).setCellValue(sourceValue);
													newGlobalValueSetTranslation.setRowStyle(newCellStyle);
												}
												
											} else {
												boolean insertFieldValueSet = true;
												Iterator<org.apache.poi.ss.usermodel.Row> orgFieldValueSetsIter = fieldValueSetsSheet.rowIterator();
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
																	orgFieldLabelCell.setCellStyle(existingCellStyle);
																	insertFieldValueSet = false;
																	if(!"active".equalsIgnoreCase(fieldStatus)) {
																		logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": Status is not Active");
																	}
																} else {
																	orgFieldLabelCell.setCellStyle(errorCellStyle);
																	logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgFieldLabel);
																}
																
															}
														}
													}
												}
												
												if(insertFieldValueSet) {
													logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value missing. Status: " + fieldStatus + " Source: " + sourceValue);
													org.apache.poi.ss.usermodel.Row newFieldValueSet = fieldValueSetsSheet.createRow(++fieldValueSetsSheetLastRow);
													newFieldValueSet.createCell(0).setCellValue(entityName);
													newFieldValueSet.createCell(1).setCellValue(fieldName);
													newFieldValueSet.createCell(3).setCellValue(fieldValue);
													newFieldValueSet.createCell(5).setCellValue(fieldLabel);
													newFieldValueSet.createCell(6).setCellValue(fieldStatus);
													newFieldValueSet.createCell(7).setCellValue(sourceValue);
													newFieldValueSet.setRowStyle(newCellStyle);
												}
												
												boolean insertValueSetTranslation = !StringUtils.isBlank(fieldItalianTranslation);
												Iterator<org.apache.poi.ss.usermodel.Row> orgValueSetTranslationIter = fieldValueSetTranslationSheet.rowIterator();
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
																		orgValueTranslationCell.setCellStyle(errorCellStyle);
																		logger.error("Missing Value Set Translation in data model design for " + entityName + "." + fieldName + "." + fieldLabel + ": " + orgValueTranslation);
																		
																	}
																} else {
																	
																	if(fieldItalianTranslation.equals(orgValueTranslation)) {
																		orgValueTranslationCell.setCellStyle(existingCellStyle);
																		insertValueSetTranslation = false;
																		if(!"active".equalsIgnoreCase(fieldStatus)) {
																			logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Status is not Active");
																		}
																	} else {
																		insertValueSetTranslation = true;
																		orgValueTranslationCell.setCellStyle(errorCellStyle);
																		logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Label Translation mismatch. Data Model: " + fieldItalianTranslation + " Org: " + orgValueTranslation);
																	}
																}
															}
														}
													}
												}
												
												if(insertValueSetTranslation) {
													logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
													org.apache.poi.ss.usermodel.Row newFieldValueSetTranslation = fieldValueSetTranslationSheet.createRow(++fieldValueSetTranslationSheetLastRow);
													newFieldValueSetTranslation.createCell(0).setCellValue(entityName + "-it");
													newFieldValueSetTranslation.createCell(2).setCellValue(fieldName);
													newFieldValueSetTranslation.createCell(3).setCellValue(fieldLabel);
													newFieldValueSetTranslation.createCell(4).setCellValue(fieldItalianTranslation);
													newFieldValueSetTranslation.createCell(5).setCellValue(fieldStatus);
													newFieldValueSetTranslation.createCell(6).setCellValue(sourceValue);
													newFieldValueSetTranslation.setRowStyle(newCellStyle);
												}
											}
										}
									}
								}
							}
						}
						
					}
					
					break;
				case "recordtype":
					break;
				case "labels":
					break;
				default:
					
					
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
						} else {
							logger.debug("entityName: " + entityName);
							if(dmRowIter.hasNext()) {
								dmColsRow = dmRowIter.next();	
							}
								
							if(dmColsRow == null) {
								logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
							} else {
								
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
								} else {
									
									org.apache.poi.ss.usermodel.Cell entityNameItalianCell = dmHeadRow.getCell(italianPos);
									String entityNameItalian = entityNameCell == null ? null : entityNameItalianCell.getStringCellValue();
									logger.debug("entityNameItalian: " + entityNameItalian);
									
									if(objTranslationSheet != null) {
										Iterator<org.apache.poi.ss.usermodel.Row> objTranslationRowIter = objTranslationSheet.rowIterator();
										
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
															valueCell.setCellStyle(errorCellStyle);
														}
													} else {
														
														if(entityNameItalian.equals(orgObjectTranslation)) {
															insertTranslation = false;
															objTranslationRow.setRowStyle(existingCellStyle);
														} else {
															if(valueCell == null) {
																valueCell = objTranslationRow.createCell(2);
															}
															valueCell.setCellStyle(errorCellStyle);
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
											newObjTranslationRow.createCell(0).setCellValue(entityName + "-it");
											newObjTranslationRow.createCell(2).setCellValue(entityNameItalian);
											newObjTranslationRow.setRowStyle(newCellStyle);
										}
										
									}
									
									while(dmRowIter.hasNext()) {
										org.apache.poi.ss.usermodel.Row dmRow = dmRowIter.next();
										org.apache.poi.ss.usermodel.Cell sourceCell = dmRow.getCell(sourcePos);
										String sourceValue = null;
										if(sourceCell == null || StringUtils.isBlank((sourceValue = sourceCell.getStringCellValue()))) {
											logger.error(entityName + ": source is empty for row " + dmRow.getRowNum());
										} else {
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
												} else {
												
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
														newFieldRow.createCell(0).setCellValue(entityName);
														newFieldRow.createCell(1).setCellValue(fieldName);
														newFieldRow.createCell(2).setCellValue(fieldLabel);
														if(!StringUtils.isBlank(fieldValueSetName)) {
															newFieldRow.createCell(21).setCellValue(fieldValueSetName);
														}
														newFieldRow.createCell(23).setCellValue(fieldStatus);
														newFieldRow.createCell(24).setCellValue(sourceValue);
														newFieldRow.setRowStyle(newCellStyle);
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
									}
								}
							}
						}
					}
				}
				
				
			}
			
			logger.debug("Writing " + orgExcelPath);
			fileOut  = new FileOutputStream(orgExcel);
			orgWb.write(fileOut);
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			if(fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			if(orgWb != null) {
				try {
					orgWb.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			
		}
		
		//Read retrieved metadata
		
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
	
}
