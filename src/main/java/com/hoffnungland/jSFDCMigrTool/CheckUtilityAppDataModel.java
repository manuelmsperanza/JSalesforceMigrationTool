package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
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

import com.hoffnungland.xpath.XmlExtractor;

import net.sf.saxon.s9api.SaxonApiException;

public class CheckUtilityAppDataModel {
	
	private static final Logger logger = LogManager.getLogger(CheckUtilityAppDataModel.class);
	
	private static final String sfMetadataNs = "xmlns=\"http://soap.sforce.com/2006/04/metadata\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"";
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		logger.traceEntry();
		
		List<String> listSources = new ArrayList<String>();
		listSources.add("EAP2");
		listSources.add("UAPP");
		listSources.add("TEA");
		
		//Read Excel
		String dmExcelPath = "C:\\Users\\msperanza\\OneDrive - Engineering Ingegneria Informatica S.p.A\\Documenti condivisi\\General\\Analisi\\UAPP Data Model v01.xlsx";
		String orgExcelPath = "wrtsuapp.xlsx";
		//String orgExcelPath = "eng.msperanza - teaspa.it.release1.xlsx";
		//String orgExcelPath = "eng.msperanza - teaspa.it.dev.xlsx";
		
		try {
			
			org.apache.poi.xssf.usermodel.XSSFWorkbook orgWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(orgExcelPath);
			
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
			int objTranslationSheetLastRow = objTranslationSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet fieldSheet = orgWb.getSheet("Fields");
			int fieldSheetLastRow = fieldSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet fieldTranslationSheet = orgWb.getSheet("Field translation");
			int fieldTranslationSheetLastRow = fieldTranslationSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet fieldValueSetsSheet = orgWb.getSheet("Fields valueSets");
			int fieldValueSetsSheetLastRow = fieldValueSetsSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet fieldValueSetTranslationSheet = orgWb.getSheet("Field valueSets translation");
			int fieldValueSetTranslationSheetLastRow = fieldValueSetTranslationSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet globalValueSetSheet = orgWb.getSheet("Global ValueSet");
			int globalValueSetSheetLastRow = globalValueSetSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet globalValueSetTranslation = orgWb.getSheet("Global ValueSet Translation");
			int globalValueSetTranslationLastRow = globalValueSetTranslation.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet labelSheet = orgWb.getSheet("Labels");
			int labelSheetLastRow = labelSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet labelSheetTranslation = orgWb.getSheet("Label translation");
			int labelSheetTranslationLastRow = labelSheetTranslation.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet recordTypeSheet = orgWb.getSheet("RecordTypes");
			int recordTypeSheetLastRow = recordTypeSheet.getLastRowNum();
			org.apache.poi.ss.usermodel.Sheet recordTypeTranslationSheet = orgWb.getSheet("RecordTypes translation");
			int recordTypeTranslationSheetLastRow = recordTypeTranslationSheet.getLastRowNum();
			
			org.apache.poi.ss.usermodel.Workbook dmWb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(dmExcelPath);
			Iterator<org.apache.poi.ss.usermodel.Sheet> wsIter = dmWb.sheetIterator();
			
			while(wsIter.hasNext()) {
				org.apache.poi.ss.usermodel.Sheet dmSheet = wsIter.next();
				
				switch(dmSheet.getSheetName().toLowerCase()) {
				case "cover":
					break;
				case "lov":
					break;
				case "recordtype":
					break;
				case "labels":
					break;
				default:
					Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
					org.apache.poi.ss.usermodel.Row dmHeadRow = null;
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
							
							org.apache.poi.ss.usermodel.Row dmColsRow = null;
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
									
									if(objTranslationSheet != null) {
										Iterator<org.apache.poi.ss.usermodel.Row> objTranslationRowIter = objTranslationSheet.rowIterator();
										
										boolean insertTranslation = true;
										while(objTranslationRowIter.hasNext()) {
											org.apache.poi.ss.usermodel.Row objTranslationRow = objTranslationRowIter.next();
											
											org.apache.poi.ss.usermodel.Cell fileNameCell = null;
											if((fileNameCell = objTranslationRow.getCell(0)) != null) {
												if((entityName + "-it").equals(fileNameCell.getStringCellValue())) {
													
													org.apache.poi.ss.usermodel.Cell valueCell = objTranslationRow.getCell(2);
													String orgObjectTranslation = valueCell == null ? null : valueCell.getStringCellValue();
													
													if(!StringUtils.isBlank(entityNameItalian)) {
														if(entityNameItalian.equals(orgObjectTranslation)) {
															insertTranslation = false;
															objTranslationRow.setRowStyle(existingCellStyle);
														} else {
															valueCell.setCellStyle(errorCellStyle);
															logger.error("Mismatch in object translation. Data Model: " + entityNameItalian + " Org: " + orgObjectTranslation);
														}
													} else {
														if(valueCell == null) {
															valueCell = objTranslationRow.createCell(2);
														}
														valueCell.setCellStyle(errorCellStyle);
														logger.error("Missing object translation in data model design for " + entityName + ": " + orgObjectTranslation);
														insertTranslation = false;
													}
													
												}
											}
											
											if(insertTranslation) {
												org.apache.poi.ss.usermodel.Row newObjTranslationRow = objTranslationSheet.createRow(++objTranslationSheetLastRow);
												newObjTranslationRow.createCell(0).setCellValue(entityName + "-it");
												newObjTranslationRow.createCell(2).setCellValue(entityNameItalian);
												newObjTranslationRow.setRowStyle(newCellStyle);
											}
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
												if(StringUtils.isBlank(fieldName)) {
													logger.error(entityName + ": name is empty for row " + dmRow.getRowNum());
													rowHasError = true;
												}
												String fieldLabel = getCellValue(dmRow, labelPos);
												if(StringUtils.isBlank(fieldLabel)) {
													logger.error(entityName + ": label is empty for row " + dmRow.getRowNum());
													rowHasError = true;
												}
												String fieldStatus = getCellValue(dmRow, statusPos);
												if(StringUtils.isBlank(fieldStatus)) {
													logger.error(entityName + ": status is empty for row " + dmRow.getRowNum());
													rowHasError = true;
												}
												
												if(rowHasError) {
													logger.error(entityName + ": missing useful value(s). Skip row #" + dmRow.getRowNum());
												} else {
												
													String fieldItalianTranslation = getCellValue(dmRow, italianPos);
													
													String fieldValueSetName = getCellValue(dmRow, valueSetNamePos);
													
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
																
																if(fieldLabel.equals(orgFieldLabel)) {
																	orgFieldLabelCell.setCellStyle(existingCellStyle);
																} else {
																	insertField = true;
																	logger.error(entityName + "." + fieldName + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgFieldLabel);
																	orgFieldLabelCell.setCellStyle(errorCellStyle);
																}
																
																org.apache.poi.ss.usermodel.Cell orgFieldValueSetsNameCell = null;
																String orgFieldValueSetsName = (orgFieldValueSetsNameCell = orgFieldRow.getCell(23)) == null ? null : orgFieldValueSetsNameCell.getStringCellValue();
																if(fieldValueSetName == null) {
																	if(orgFieldValueSetsName != null) {
																		if(orgFieldValueSetsNameCell == null) {
																			orgFieldValueSetsNameCell = orgFieldRow.createCell(23);
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
														
														org.apache.poi.ss.usermodel.Row newFieldRow = fieldSheet.createRow(++fieldSheetLastRow);
														newFieldRow.createCell(0).setCellValue(entityName);
														newFieldRow.createCell(1).setCellValue(fieldName);
														newFieldRow.createCell(2).setCellValue(fieldLabel);
														if(!StringUtils.isBlank(fieldValueSetName)) {
															newFieldRow.createCell(23).setCellValue(fieldValueSetName);
														}
														newFieldRow.setRowStyle(newCellStyle);
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
			
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
		
		org.apache.poi.ss.usermodel.Cell workingCell = null;
		String workingCellStringValue = (workingCell = inRow.getCell(position)) == null ? null : workingCell.getStringCellValue();
		
		return logger.traceExit(workingCellStringValue);
	}
	
}
