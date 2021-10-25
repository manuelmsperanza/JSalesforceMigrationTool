package com.hoffnungland.jSFDCMigrTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;

public class CheckUtilityAppDataModel {
	
	private static final Logger logger = LogManager.getLogger(CheckUtilityAppDataModel.class);
	
	private File orgExcel;
	private FileInputStream orgExcelFis = null;
	private FileOutputStream orgExcelFos = null;
	private org.apache.poi.xssf.usermodel.XSSFWorkbook dmWb;
	private org.apache.poi.xssf.usermodel.XSSFWorkbook orgWb;
	
	private org.apache.poi.xssf.usermodel.XSSFCellStyle metadataHeaderCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle headerCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle existingCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle newCellStyle;
	private org.apache.poi.xssf.usermodel.XSSFCellStyle errorCellStyle;
	
	private org.apache.poi.xssf.usermodel.XSSFSheet objTranslationSheet;
	private int objTranslationSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet nameFieldSheet;
	private int nameFieldSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet fieldSheet;
	private int fieldSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet fieldTranslationSheet;
	private int fieldTranslationSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet fieldValueSetsSheet;
	private int fieldValueSetsSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet fieldValueSetTranslationSheet;
	private int fieldValueSetTranslationSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet globalValueSetSheet;
	private int globalValueSetSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet globalValueSetTranslationSheet;
	private int globalValueSetTranslationSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet standardValueSetSheet;
	private int standardValueSetSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet standardValueSetTranslationSheet;
	private int standardValueSetTranslationSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet labelSheet;
	private int labelSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet labelSheetTranslation;
	private int labelSheetTranslationLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet recordTypeSheet;
	private int recordTypeSheetLastRow;
	private org.apache.poi.xssf.usermodel.XSSFSheet recordTypeTranslationSheet;
	private int recordTypeTranslationSheetLastRow;

	private int orgObjTranslationRow;
	private int dmObjTranslationRow;
	
	private Properties fieldStandardValueSet;

	private Set<String> setStandardValueSet;
	
	private class MatchEntry {
		
		String matchValue;
		int matchPosition;
		
		public MatchEntry(String matchValue, int matchPosition) {
			this.matchValue = matchValue;
			this.matchPosition  = matchPosition;
		}
	}
	
	private class CheckEntry {
		
		String sourceCheckValue;
		int checkPosition;
		String objectDescription;
		
		public CheckEntry(String sourceCheckValue, int checkPosition, String objectDescription) {
			super();
			this.sourceCheckValue = sourceCheckValue;
			this.checkPosition = checkPosition;
			this.objectDescription = objectDescription;
		}
		
	}
	
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
		//String orgExcelPath = "manuel.speranza - eap2.xlsx";
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
		
		this.headerCellStyle = this.orgWb.createCellStyle();
		//XSSFColor foreGroundcolor = new XSSFColor(new java.awt.Color(255,204,153));
		byte[] rgb = new byte[3];
		rgb[0] = (byte) 255; // red
		rgb[1] = (byte) 204; // green
		rgb[2] = (byte) 153; // blue
		org.apache.poi.xssf.usermodel.XSSFColor foreGroundcolor = new org.apache.poi.xssf.usermodel.XSSFColor(rgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #f2dcdb
		this.headerCellStyle.setFillForegroundColor(foreGroundcolor );
		this.headerCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		this.headerCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.headerCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.headerCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.headerCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		
		this.metadataHeaderCellStyle = this.orgWb.createCellStyle();
		this.metadataHeaderCellStyle.setFillForegroundColor(foreGroundcolor);
		this.metadataHeaderCellStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
		this.metadataHeaderCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.metadataHeaderCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.metadataHeaderCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.metadataHeaderCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.metadataHeaderCellStyle.setAlignment(HorizontalAlignment.CENTER);
		org.apache.poi.xssf.usermodel.XSSFFont defaultFont= this.orgWb.createFont();
		defaultFont.setBold(true);
		this.metadataHeaderCellStyle.setFont(defaultFont);
		
		this.existingCellStyle = this.orgWb.createCellStyle();	
		byte[] yellowRgb = new byte[3];
		yellowRgb[0] = (byte) 255; // red
		yellowRgb[1] = (byte) 255; // green
		yellowRgb[2] = (byte) 0; // blue
		org.apache.poi.xssf.usermodel.XSSFColor yellowColor = new org.apache.poi.xssf.usermodel.XSSFColor(yellowRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FFFF00
		this.existingCellStyle.setFillForegroundColor(yellowColor);
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
		org.apache.poi.xssf.usermodel.XSSFColor greenColor = new org.apache.poi.xssf.usermodel.XSSFColor(greenRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #00FF00
		this.newCellStyle.setFillForegroundColor(greenColor);
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
		org.apache.poi.xssf.usermodel.XSSFColor redColor = new org.apache.poi.xssf.usermodel.XSSFColor(redRgb, new org.apache.poi.xssf.usermodel.DefaultIndexedColorMap()); // #FF0000
		this.errorCellStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		this.errorCellStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
		org.apache.poi.xssf.usermodel.XSSFFont boldFont= this.orgWb.createFont();
		boldFont.setBold(true);
		boldFont.setColor(redColor);
		this.errorCellStyle.setFont(boldFont);
		
		this.objTranslationSheet = this.orgWb.getSheet("Object translation");
		this.objTranslationSheetLastRow = (this.objTranslationSheet == null ? -1 : this.objTranslationSheet.getLastRowNum());
		
		this.nameFieldSheet = this.orgWb.getSheet("Name Fields");
		this.nameFieldSheetLastRow = (this.nameFieldSheet == null ? -1 : this.nameFieldSheet.getLastRowNum());
		
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
	
	private String getCellValue(org.apache.poi.xssf.usermodel.XSSFRow inRow, int position) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFCell workingCell = inRow.getCell(position);
		
		String workingCellStringValue = null;
		if(workingCell != null) {
			if(workingCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
				workingCellStringValue = StringUtils.removeEnd(Double.toString(workingCell.getNumericCellValue()), ".0");
			} else {
				workingCellStringValue = workingCell.getStringCellValue();
			}	
		}	
		return logger.traceExit(workingCellStringValue);
	}
	
	private void createStringCell(org.apache.poi.xssf.usermodel.XSSFRow row, String cellValues[], org.apache.poi.xssf.usermodel.XSSFCellStyle cellStyle) {
		logger.traceEntry();
		
		for(int columnIndex = 0; columnIndex < cellValues.length; columnIndex++) {
			org.apache.poi.xssf.usermodel.XSSFCell workingCell = row.createCell(columnIndex);
			String fieldValue = cellValues[columnIndex];
			if(fieldValue != null) {
				workingCell.setCellValue(fieldValue);
			}
			workingCell.setCellStyle(cellStyle);
		}
		
		logger.traceExit();
	}
	
	public void scanDataModel(List<String> listSources) throws FileNotFoundException, IOException {
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Sheet> wsIter = this.dmWb.sheetIterator();
		
		while(wsIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFSheet dmSheet = (org.apache.poi.xssf.usermodel.XSSFSheet) wsIter.next();
			
			switch(dmSheet.getSheetName().toLowerCase()) {
			case "cover":
				break;
			case "lov":
				this.checkLov(dmSheet, listSources);
				break;
			case "recordtype":
				break;
			case "labels":
				this.checkLabels(dmSheet, listSources);
				break;
			default:
				this.checkEntity(dmSheet, listSources);
			}
			
		}
		
		logger.traceExit();
		
	}
	
	private void checkLov(org.apache.poi.xssf.usermodel.XSSFSheet dmSheet, List<String> listSources) throws FileNotFoundException, IOException {
		
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
		org.apache.poi.xssf.usermodel.XSSFRow dmColsRow = null;
		
		if(dmRowIter.hasNext()) {
			dmColsRow = (org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next();	
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
			org.apache.poi.xssf.usermodel.XSSFCell collCell = (org.apache.poi.xssf.usermodel.XSSFCell) dmColsCellIter.next();
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
		
		this.fieldStandardValueSet = new Properties();
		File fieldStandardValueSetFile = new File("./etc/fieldStandardValueSet.properties");
		if(fieldStandardValueSetFile.exists()) {
			try (FileInputStream configFile = new FileInputStream(fieldStandardValueSetFile)) {
				this.fieldStandardValueSet.load(configFile);
			}
		}
		this.setStandardValueSet = new HashSet<String>();
		for(Object curValue : this.fieldStandardValueSet.values()) {
			this.setStandardValueSet.add((String) curValue);
		}
		
		while(dmRowIter.hasNext()) {
			
			this.checkLovRow((org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next(), listSources, objectPos, fieldPos, valuePos, labelPos, italianPos, statusPos, sourcePos);
		}
		
		logger.traceExit();
	}
	
	private void checkLovRow(org.apache.poi.xssf.usermodel.XSSFRow dmRow, List<String> listSources, int objectPos, int fieldPos, int valuePos, int labelPos, int italianPos, int statusPos, int sourcePos) {
		logger.traceEntry();
		
		org.apache.poi.xssf.usermodel.XSSFCell sourceCell = dmRow.getCell(sourcePos);
		String sourceValue = (sourceCell == null) ? null : sourceCell.getStringCellValue();
		if(StringUtils.isBlank(sourceValue)) {
			logger.error("LOV: Source is empty for row #" + dmRow.getRowNum());
			logger.traceExit();
			return;
		}
		
		boolean rowHasError = false;
			
		String entityName = this.getCellValue(dmRow, objectPos);
		if(StringUtils.isBlank(entityName)) {
			logger.error("LOV: Object is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
			
		String fieldName = this.getCellValue(dmRow, fieldPos);
		if(StringUtils.isBlank(fieldName)) {
			logger.error("LOV: Name is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		String fieldValue = this.getCellValue(dmRow, valuePos);
		if(StringUtils.isBlank(fieldValue)) {
			logger.error("LOV: Value is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		String fieldStatus = this.getCellValue(dmRow, statusPos);
		if(StringUtils.isBlank(fieldStatus)) {
			logger.error("LOV: Status is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		String fieldLabel = this.getCellValue(dmRow, labelPos);
		if(StringUtils.isBlank(fieldLabel)) {
			logger.error("LOV: English is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		if(rowHasError) {
			logger.error("LOV: missing useful value(s). Skip row #" + dmRow.getRowNum());
			logger.traceExit();
			return;
		}
				
		String italianFieldTranslation = this.getCellValue(dmRow, italianPos);
		
		if("*".equals(entityName)) {
			
			if(this.setStandardValueSet.contains(fieldName)) {
				
				CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVs[] = {
						new CheckUtilityAppDataModel.MatchEntry(fieldName, 0),
						new CheckUtilityAppDataModel.MatchEntry(fieldValue, 1)
				};
				CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVs[] = {
						new CheckUtilityAppDataModel.CheckEntry(fieldLabel, 3, "standard valueset entry"),
				};
				
				boolean insertStandardValueSet = this.checkMultipleEntry(this.standardValueSetSheet, listMatchEntryStandardVs, listCheckEntryStandardVs, fieldStatus);
				
				if(insertStandardValueSet) {
					this.insertStandardValueSet(fieldName, fieldValue, fieldLabel, fieldStatus, sourceValue);
				}
				
				CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVsTranslation[] = {
						new CheckUtilityAppDataModel.MatchEntry(fieldName+"-it", 0),
						new CheckUtilityAppDataModel.MatchEntry(fieldLabel, 1)
				};
				CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVsTranslation[] = {
						new CheckUtilityAppDataModel.CheckEntry(italianFieldTranslation, 2, "italian standard valueset translation"),
				};
				
				boolean insertStandardValueSetTranslation = this.checkMultipleEntry(this.standardValueSetTranslationSheet, listMatchEntryStandardVsTranslation, listCheckEntryStandardVsTranslation, fieldStatus);
				
				if(insertStandardValueSetTranslation) {
					this.insertStandardValueSetTranslation(fieldName + "-it", fieldLabel, italianFieldTranslation, fieldStatus, sourceValue);
				}
				
			} else {
				
				if(!listSources.contains(sourceValue)) {
					logger.traceExit();
					return;
				}
				
				/*boolean insertGlobalValueSet = true;
				
				Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetsIter = this.globalValueSetSheet.rowIterator();
				while(orgGlobalValueSetsIter.hasNext()) {
					org.apache.poi.xssf.usermodel.XSSFRow orgGlobalValueSetRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgGlobalValueSetsIter.next();
					String orgGlobalVsName = this.getCellValue(orgGlobalValueSetRow, 0);
					if(fieldName.equals(orgGlobalVsName)) {
						String orgGlobalVsValue = this.getCellValue(orgGlobalValueSetRow, 1);
						if(fieldValue.equals(orgGlobalVsValue)) {
							org.apache.poi.xssf.usermodel.XSSFCell orgGlobalVsLabelCell = null;
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
				}*/
				CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVs[] = {
						new CheckUtilityAppDataModel.MatchEntry(fieldName, 0),
						new CheckUtilityAppDataModel.MatchEntry(fieldValue, 1)
				};
				CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVs[] = {
						new CheckUtilityAppDataModel.CheckEntry(fieldLabel, 3, "global valueset entry"),
				};
				
				boolean insertGlobalValueSet = this.checkMultipleEntry(this.globalValueSetSheet, listMatchEntryStandardVs, listCheckEntryStandardVs, fieldStatus);
				
				if(insertGlobalValueSet) {
					this.insertGlobalValueSet(fieldName, fieldValue, fieldLabel, fieldStatus, sourceValue);
					/*logger.error("Global Value Set " + fieldName + "." + fieldValue + ": value missing. Status: " + fieldStatus + " Source: " + sourceValue);
					org.apache.poi.xssf.usermodel.XSSFRow newGlobalValueSet = this.globalValueSetSheet.createRow(++this.globalValueSetSheetLastRow);
					String cellValues[] = {fieldName, fieldValue, null, fieldLabel, fieldStatus, sourceValue};
					this.createStringCell(newGlobalValueSet, cellValues, this.newCellStyle);*/
				}
				
				/*boolean insertGlobalValueSetTranslation = !StringUtils.isBlank(italianFieldTranslation);
				Iterator<org.apache.poi.ss.usermodel.Row> orgGlobalValueSetTranslationIter = this.globalValueSetTranslationSheet.rowIterator();
				while(orgGlobalValueSetTranslationIter.hasNext()) {
					org.apache.poi.xssf.usermodel.XSSFRow orgGlobalValueSetTranslationRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgGlobalValueSetTranslationIter.next();
					String orgGlobalVsName = this.getCellValue(orgGlobalValueSetTranslationRow, 0);
					if((fieldName + "-it").equals(orgGlobalVsName)) {
						String orgGlobalVsValue = this.getCellValue(orgGlobalValueSetTranslationRow, 1);
						if(fieldLabel.equals(orgGlobalVsValue)) {
							org.apache.poi.xssf.usermodel.XSSFCell orgGlobalVsLabelCell = null;
							String orgGlobalVsLabel = (orgGlobalVsLabelCell = orgGlobalValueSetTranslationRow.getCell(2)) == null ? null : orgGlobalVsLabelCell.getStringCellValue();
							
							if(StringUtils.isBlank(italianFieldTranslation)) {
								if(!StringUtils.isBlank(orgGlobalVsLabel)) {
									insertGlobalValueSetTranslation = true;
									orgGlobalVsLabelCell.setCellStyle(this.errorCellStyle);
									logger.error("Missing Global Value Set italian Translation in data model design for " + fieldName + "." + fieldLabel + ": " + orgGlobalVsLabel);
									
								}
							} else {
							
								if(italianFieldTranslation.equals(orgGlobalVsLabel)) {
									orgGlobalVsLabelCell.setCellStyle(this.existingCellStyle);
									insertGlobalValueSetTranslation = false;
									if(!"active".equalsIgnoreCase(fieldStatus)) {
										logger.error("Global Value Set italian Translation " + fieldName + "." + fieldLabel + ": Status is not Active");
									}
								} else {
									insertGlobalValueSetTranslation = true;
									orgGlobalVsLabelCell.setCellStyle(this.errorCellStyle);
									logger.error("Global Value Set " + fieldName + "." + fieldLabel + ": italian Label Translation mismatch. Data Model: " + italianFieldTranslation + " Org: " + orgGlobalVsLabel);
								}
							}
						}
					}
				}*/
				
				CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVsTranslation[] = {
						new CheckUtilityAppDataModel.MatchEntry(fieldName+"-it", 0),
						new CheckUtilityAppDataModel.MatchEntry(fieldLabel, 1)
				};
				CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVsTranslation[] = {
						new CheckUtilityAppDataModel.CheckEntry(italianFieldTranslation, 2, "italian global valueset translation"),
				};
				
				boolean insertGlobalValueSetTranslation = this.checkMultipleEntry(this.globalValueSetTranslationSheet, listMatchEntryStandardVsTranslation, listCheckEntryStandardVsTranslation, fieldStatus);
				
				if(insertGlobalValueSetTranslation) {
					this.insertGlobalValueSetTranslation(fieldName + "-it", fieldLabel, italianFieldTranslation, fieldStatus, sourceValue);
					/*logger.error("Global Value Set " + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
					org.apache.poi.xssf.usermodel.XSSFRow newGlobalValueSetTranslation = this.globalValueSetTranslationSheet.createRow(++this.globalValueSetTranslationSheetLastRow);							
					String cellValues[] = {fieldName + "-it", fieldLabel, italianFieldTranslation, fieldStatus, sourceValue};
					this.createStringCell(newGlobalValueSetTranslation, cellValues, this.newCellStyle);*/
				}
				
			}
			
			
			
		} else {
			
			if(!listSources.contains(sourceValue)) {
				logger.traceExit();
				return;
			}
			
			/*boolean insertFieldValueSet = true;
			Iterator<org.apache.poi.ss.usermodel.Row> orgFieldValueSetsIter = this.fieldValueSetsSheet.rowIterator();
			while(orgFieldValueSetsIter.hasNext()) {
				
				org.apache.poi.xssf.usermodel.XSSFRow orgFieldValueSetRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgFieldValueSetsIter.next();
				
				String orgFieldFilename = this.getCellValue(orgFieldValueSetRow, 0);
				
				if(entityName.equals(orgFieldFilename)) {
					String orgFieldName = this.getCellValue(orgFieldValueSetRow, 1);
					if(fieldName.equals(orgFieldName)) {
						String orgFieldValue = this.getCellValue(orgFieldValueSetRow, 3);
						if(fieldValue.equals(orgFieldValue)) {
							org.apache.poi.xssf.usermodel.XSSFCell orgFieldLabelCell = null;
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
			}*/
			
			CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVs[] = {
					new CheckUtilityAppDataModel.MatchEntry(entityName, 0),
					new CheckUtilityAppDataModel.MatchEntry(fieldName, 1),
					new CheckUtilityAppDataModel.MatchEntry(fieldValue, 3)
			};
			CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVs[] = {
					new CheckUtilityAppDataModel.CheckEntry(fieldLabel, 5, "field valueset entry"),
			};
			
			boolean insertFieldValueSet = this.checkMultipleEntry(this.fieldValueSetsSheet, listMatchEntryStandardVs, listCheckEntryStandardVs, fieldStatus);
			
			if(insertFieldValueSet) {
				this.insertFieldValueSet(entityName, fieldName, fieldValue, fieldLabel, fieldStatus, sourceValue);
				/*logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value missing. Status: " + fieldStatus + " Source: " + sourceValue);
				org.apache.poi.xssf.usermodel.XSSFRow newFieldValueSet = this.fieldValueSetsSheet.createRow(++this.fieldValueSetsSheetLastRow);
				
				String cellValues[] = {entityName, fieldName, null, fieldValue, null, fieldLabel, fieldStatus, sourceValue};
				this.createStringCell(newFieldValueSet, cellValues, this.newCellStyle);*/
			}
			
			/*boolean insertFieldValueSetTranslation = !StringUtils.isBlank(italianFieldTranslation);
			Iterator<org.apache.poi.ss.usermodel.Row> orgValueSetTranslationIter = this.fieldValueSetTranslationSheet.rowIterator();
			while(orgValueSetTranslationIter.hasNext()) {
				org.apache.poi.xssf.usermodel.XSSFRow orgValueSetTranslationRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgValueSetTranslationIter.next();
				String orgEntityName = this.getCellValue(orgValueSetTranslationRow, 0);
				if((entityName + "-it").equals(orgEntityName)) {
					String orgFieldName = this.getCellValue(orgValueSetTranslationRow, 2);
					if(fieldName.equals(orgFieldName)) {
						String orgFieldValue = this.getCellValue(orgValueSetTranslationRow, 3);
						if(fieldLabel.equals(orgFieldValue)) {
							org.apache.poi.xssf.usermodel.XSSFCell orgValueTranslationCell = null;
							String orgValueTranslation = (orgValueTranslationCell = orgValueSetTranslationRow.getCell(4)) == null ? null : orgValueTranslationCell.getStringCellValue();
							
							if(StringUtils.isBlank(italianFieldTranslation)) {
								if(!StringUtils.isBlank(orgValueTranslation)) {
									insertFieldValueSetTranslation = true;
									orgValueTranslationCell.setCellStyle(this.errorCellStyle);
									logger.error("Missing Value Set Translation in data model design for " + entityName + "." + fieldName + "." + fieldLabel + ": " + orgValueTranslation);
								}
							} else {
								
								if(italianFieldTranslation.equals(orgValueTranslation)) {
									orgValueTranslationCell.setCellStyle(this.existingCellStyle);
									insertFieldValueSetTranslation = false;
									if(!"active".equalsIgnoreCase(fieldStatus)) {
										logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Status is not Active");
									}
								} else {
									insertFieldValueSetTranslation = true;
									orgValueTranslationCell.setCellStyle(this.errorCellStyle);
									logger.error("Value Set " + entityName + "." + fieldName + "." + fieldLabel + ": Label Translation mismatch. Data Model: " + italianFieldTranslation + " Org: " + orgValueTranslation);
								}
							}
						}
					}
				}
			}*/
			
			CheckUtilityAppDataModel.MatchEntry listMatchEntryStandardVsTranslation[] = {
					new CheckUtilityAppDataModel.MatchEntry(entityName+"-it", 0),
					new CheckUtilityAppDataModel.MatchEntry(fieldName, 2),
					new CheckUtilityAppDataModel.MatchEntry(fieldLabel, 3)
			};
			CheckUtilityAppDataModel.CheckEntry listCheckEntryStandardVsTranslation[] = {
					new CheckUtilityAppDataModel.CheckEntry(italianFieldTranslation, 4, "italian field valueset translation"),
			};
			
			boolean insertFieldValueSetTranslation = this.checkMultipleEntry(this.fieldValueSetTranslationSheet, listMatchEntryStandardVsTranslation, listCheckEntryStandardVsTranslation, fieldStatus);
			
			if(insertFieldValueSetTranslation) {
				this.insertFieldValueSetTranslation(entityName + "-it", fieldName, fieldLabel, italianFieldTranslation, fieldStatus, sourceValue);
				/*logger.error("Value Set " + entityName + "." + fieldName + "." + fieldValue + ": value italian translation missing. Status: " + fieldStatus + " Source: " + sourceValue);
				org.apache.poi.xssf.usermodel.XSSFRow newFieldValueSetTranslation = this.fieldValueSetTranslationSheet.createRow(++this.fieldValueSetTranslationSheetLastRow);
				
				String cellValues[] = {entityName + "-it", null, fieldName, fieldLabel, italianFieldTranslation, fieldStatus, sourceValue};
				this.createStringCell(newFieldValueSetTranslation, cellValues, this.newCellStyle);*/
				
			}
		}
		
		logger.traceExit();
		
	}
	
	private void checkLabels(org.apache.poi.xssf.usermodel.XSSFSheet dmSheet, List<String> listSources) {
		
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
		org.apache.poi.xssf.usermodel.XSSFRow dmColsRow = null;
		
		if(dmRowIter.hasNext()) {
			dmColsRow = (org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next();	
		}
			
		if(dmColsRow == null) {
			logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		} 
		int fullNamePos = -1;
		int valuePos = -1;
		int statusPos = -1;
		int sourcePos = -1;
		int italianPos = -1;
		
		Iterator<org.apache.poi.ss.usermodel.Cell> dmColsCellIter = dmColsRow.cellIterator();
		while(dmColsCellIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFCell collCell = (org.apache.poi.xssf.usermodel.XSSFCell) dmColsCellIter.next();
			if(collCell != null) {
				switch(collCell.getStringCellValue().toLowerCase()) {
				case "fullname":
					fullNamePos = collCell.getColumnIndex();
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
				case "italian":
					italianPos = collCell.getColumnIndex();
					break;
				}
			}
		}
		boolean colsError = false;
		if(fullNamePos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing fullName column");
			colsError = true;
		}
				
		if(valuePos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing value column");
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
		
		if(colsError) {
			logger.error("Missing useful colum(s). Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		}
				
		while(dmRowIter.hasNext()) {
			
			this.checkLabelRow((org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next(), listSources, fullNamePos, valuePos, italianPos, statusPos, sourcePos);
		}
		
		logger.traceExit();
	}
	
	private void checkLabelRow(org.apache.poi.xssf.usermodel.XSSFRow dmRow, List<String> listSources, int fullNamePos, int valuePos, int italianPos, int statusPos, int sourcePos) {
		logger.traceEntry();
		
		org.apache.poi.xssf.usermodel.XSSFCell sourceCell = dmRow.getCell(sourcePos);
		String sourceValue = (sourceCell == null) ? null : sourceCell.getStringCellValue();
		if(StringUtils.isBlank(sourceValue)) {
			logger.error("Labels: Source is empty for row #" + dmRow.getRowNum());
			logger.traceExit();
			return;
		}
		
		if(!listSources.contains(sourceValue)) {
			logger.traceExit();
			return;
		}
		
		boolean rowHasError = false;
			
		String fullName = this.getCellValue(dmRow, fullNamePos);
		if(StringUtils.isBlank(fullName)) {
			logger.error("Labels: fullName is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
			
		String value = this.getCellValue(dmRow, valuePos);
		if(StringUtils.isBlank(value)) {
			logger.error("Labels: value is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		String fieldStatus = this.getCellValue(dmRow, statusPos);
		if(StringUtils.isBlank(fieldStatus)) {
			logger.error("Labels: Status is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		if(rowHasError) {
			logger.error("Labels: missing useful value(s). Skip row #" + dmRow.getRowNum());
			logger.traceExit();
			return;
		}
		
		boolean insertLabels = this.checkSingleEntry(this.labelSheet, fullName, 1, value, 5, "custom label", fieldStatus);
		if(insertLabels) {
			this.insertLabels(fullName, value, fieldStatus, sourceValue);
		}
		
		String italianFieldTranslation = this.getCellValue(dmRow, italianPos);
		
		boolean insertLabelTranslation = this.checkSingleEntry(this.labelSheet, fullName, 1, italianFieldTranslation, 2, "italian custom label translation", fieldStatus);
		if(insertLabelTranslation) {
			this.insertLabelTranslation(fullName, italianFieldTranslation, fieldStatus, sourceValue);
		}
		
		logger.traceExit();
		
	}
	
	private void checkEntity(org.apache.poi.xssf.usermodel.XSSFSheet dmSheet, List<String> listSources) {
		
		logger.traceEntry();
		
		Iterator<org.apache.poi.ss.usermodel.Row> dmRowIter = dmSheet.rowIterator();
		org.apache.poi.xssf.usermodel.XSSFRow dmHeadRow = null;
		
		
		if(dmRowIter.hasNext()) {
			dmHeadRow = (org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next();	
		}
		if(dmHeadRow == null) {
			logger.warn("Missing header. Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		}
		
		org.apache.poi.xssf.usermodel.XSSFCell entityNameCell = dmHeadRow.getCell(0);
		String entityName = (entityNameCell == null ? null : entityNameCell.getStringCellValue());
		if(StringUtils.isBlank(entityName)) {
			logger.warn("Missing entity name. Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		}
		logger.debug("entityName: " + entityName);
		
		org.apache.poi.xssf.usermodel.XSSFRow dmColsRow = null;
		if(dmRowIter.hasNext()) {
			dmColsRow = (org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next();	
		}
			
		if(dmColsRow == null) {
			logger.warn("Missing colums. Skip " + dmSheet.getSheetName() + " sheet.");
			logger.traceExit();
			return;
		} 
			
		int namePos = -1;
		int labelPos = -1;
		int typePos = -1;
		int statusPos = -1;
		int sourcePos = -1;
		int italianPos = -1;
		int valueSetNamePos = -1;
		
		Iterator<org.apache.poi.ss.usermodel.Cell> dmColsCellIter = dmColsRow.cellIterator();
		while(dmColsCellIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFCell collCell = (org.apache.poi.xssf.usermodel.XSSFCell) dmColsCellIter.next();
			if(collCell != null) {
				switch(collCell.getStringCellValue().toLowerCase()) {
				case "name":
					namePos = collCell.getColumnIndex();
					break;
				case "label":
					labelPos = collCell.getColumnIndex();
					break;
				case "type":
					typePos = collCell.getColumnIndex();
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
		
		if(typePos == -1) {
			logger.error(dmSheet.getSheetName() + ": missing type column");
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
		
		org.apache.poi.xssf.usermodel.XSSFCell italianEntityNameCell = dmHeadRow.getCell(italianPos);
		String italianEntityName = (entityNameCell == null ? null : italianEntityNameCell.getStringCellValue());
		logger.debug("italianEntityName: " + italianEntityName);
		
		boolean insertTranslation = this.checkObjectTranslation(entityName, italianEntityName);
		if(insertTranslation) {
			this.insertObjectTranslation(entityName, italianEntityName, null);
		}
		
		while(dmRowIter.hasNext()) {
			this.checkEntityRow((org.apache.poi.xssf.usermodel.XSSFRow) dmRowIter.next(), entityName, italianEntityName, listSources, namePos, labelPos, typePos, italianPos, valueSetNamePos, statusPos, sourcePos);
		}
		logger.traceExit();
	}

	private void checkEntityRow(org.apache.poi.xssf.usermodel.XSSFRow dmRow, String entityName, String italianEntityName, List<String> listSources, int namePos, int labelPos, int typePos, int italianPos, int valueSetNamePos, int statusPos, int sourcePos) {
		logger.traceEntry();
		
		org.apache.poi.xssf.usermodel.XSSFCell sourceCell = dmRow.getCell(sourcePos);
		String sourceValue = null;
		if(sourceCell == null || StringUtils.isBlank((sourceValue = sourceCell.getStringCellValue()))) {
			logger.error(entityName + ": source is empty for row " + dmRow.getRowNum());
			return;
		}
		
		boolean rowHasError = false;
		String fieldName = this.getCellValue(dmRow, namePos);
		logger.debug("fieldName: " + fieldName);
		if(StringUtils.isBlank(fieldName)) {
			logger.error(entityName + ": name is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		String fieldLabel = this.getCellValue(dmRow, labelPos);
		logger.debug("fieldLabel: " + fieldLabel);
		if(StringUtils.isBlank(fieldLabel)) {
			logger.error(entityName + ": label is empty for row " + dmRow.getRowNum());
			rowHasError = true;
		}
		
		String italianFieldTranslation = this.getCellValue(dmRow, italianPos);
		logger.debug("italianFieldTranslation: " + italianFieldTranslation);
		
		if("name".equalsIgnoreCase(fieldName)) {
			
			if(rowHasError) {
				logger.error(entityName + ": missing useful value(s). Skip row #" + dmRow.getRowNum());
				return;
			}
			
			boolean insertNameField = this.checkSingleEntry(this.nameFieldSheet, entityName, 0, fieldLabel, 2, "object name label", "active");
			if(insertNameField) {
				this.insertNameField(entityName, fieldLabel);
			}
			
			boolean insertNameTranslation = this.checkNameTranslation(entityName, italianFieldTranslation);
			if(insertNameTranslation) {
				this.insertObjectTranslation(entityName, italianEntityName, italianFieldTranslation);
			}
			
		} else {
			
			if(!listSources.contains(sourceValue)) {
				return;
			}
			
			String fieldType = this.getCellValue(dmRow, typePos);
			logger.debug("fieldType: " + fieldType);
			if(StringUtils.isBlank(fieldType)) {
				logger.error(entityName + ": type is empty for row " + dmRow.getRowNum());
				rowHasError = true;
			}
			
			String fieldStatus = this.getCellValue(dmRow, statusPos);
			logger.debug("fieldStatus: " + fieldStatus);
			if(StringUtils.isBlank(fieldStatus)) {
				logger.error(entityName + ": status is empty for row " + dmRow.getRowNum());
				rowHasError = true;
			}
			
			if(rowHasError) {
				logger.error(entityName + ": missing useful value(s). Skip row #" + dmRow.getRowNum());
				return;
			}
			
			String fieldValueSetName = this.getCellValue(dmRow, valueSetNamePos);
			logger.debug("fieldValueSetName: " + fieldValueSetName);
			
			//boolean insertField = this.checkField(entityName, fieldName, fieldLabel, fieldType, fieldValueSetName, fieldStatus);
			
			CheckUtilityAppDataModel.MatchEntry listMatchEntryField[] = {
					new CheckUtilityAppDataModel.MatchEntry(entityName, 0),
					new CheckUtilityAppDataModel.MatchEntry(fieldName, 1)
			};
			CheckUtilityAppDataModel.CheckEntry listCheckEntryField[] = {
					new CheckUtilityAppDataModel.CheckEntry(fieldLabel, 2, "label"),
					new CheckUtilityAppDataModel.CheckEntry(fieldType, 3, " data type"),
					new CheckUtilityAppDataModel.CheckEntry(fieldValueSetName, 21, "valueSetName"),
			};
			
			boolean insertField = this.checkMultipleEntry(this.fieldSheet, listMatchEntryField, listCheckEntryField, fieldStatus);
			
			if(insertField) {
				this.insertField(entityName, fieldName, fieldLabel, fieldType,fieldValueSetName, fieldStatus, sourceValue);
			}
			
			//boolean insertFieldTranslation = this.checkFieldTranslation(entityName, fieldName, italianFieldTranslation);
			
			CheckUtilityAppDataModel.MatchEntry listMatchEntryFieldTranslation[] = {
					new CheckUtilityAppDataModel.MatchEntry(entityName + "-it", 0),
					new CheckUtilityAppDataModel.MatchEntry(fieldName, 1)
			};
			CheckUtilityAppDataModel.CheckEntry listCheckEntryFieldTranslation[] = {
					new CheckUtilityAppDataModel.CheckEntry(italianFieldTranslation, 2, "italian field translation"),
			};
			
			boolean insertFieldTranslation = this.checkMultipleEntry(this.fieldTranslationSheet, listMatchEntryFieldTranslation, listCheckEntryFieldTranslation, fieldStatus);
			
			if(insertFieldTranslation) {
				this.insertFieldTranslation(entityName, fieldName, italianFieldTranslation, fieldStatus, sourceValue);
			}
		}
		
		logger.traceExit();
		
	}

	private boolean checkSingleEntry(org.apache.poi.xssf.usermodel.XSSFSheet checkSheet, String matchValue, int matchPosition, String sourceCheckValue, int checkPosition, String objectDescription, String fieldStatus) {
		logger.traceEntry();
		
		boolean insertNewRecord = !StringUtils.isBlank(sourceCheckValue);
				
		if(checkSheet == null) {
			return logger.traceExit(insertNewRecord);
		}
		
		Iterator<org.apache.poi.ss.usermodel.Row> rowIter = checkSheet.rowIterator();
		
		while(rowIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFRow entryRow = (org.apache.poi.xssf.usermodel.XSSFRow) rowIter.next();
			
			org.apache.poi.xssf.usermodel.XSSFCell matchCell = null;
			if((matchCell = entryRow.getCell(matchPosition)) != null) {
				if((matchValue).equals(matchCell.getStringCellValue())) {
					
					org.apache.poi.xssf.usermodel.XSSFCell checkValueCell = entryRow.getCell(checkPosition);
					String targetCheckString = (checkValueCell == null ? null : checkValueCell.getStringCellValue());
					logger.debug("checkString: " + targetCheckString);
					if(StringUtils.isBlank(sourceCheckValue)) {
						if(!StringUtils.isBlank(targetCheckString)) {
							logger.error("Missing " + objectDescription + " in data model design for " + matchValue + ": " + targetCheckString);
							checkValueCell.setCellStyle(this.errorCellStyle);
						}
					} else {
						
						if(sourceCheckValue.equals(targetCheckString)) {
							insertNewRecord = false;
							checkValueCell.setCellStyle(this.existingCellStyle);
							if(!"active".equalsIgnoreCase(fieldStatus)) {
								logger.error(matchValue + ": Status is not Active");
							}
						} else {
							if(checkValueCell == null) {
								checkValueCell = entryRow.createCell(checkPosition);
							}
							checkValueCell.setCellStyle(this.errorCellStyle);
							logger.error("Mismatch in " + objectDescription + "(" + matchValue + "). Data Model: " + sourceCheckValue + " Org: " + targetCheckString);
						}
					}
					return logger.traceExit(insertNewRecord);
				}
			}
				
		}
		
		return logger.traceExit(insertNewRecord);
	}
	
	private boolean needsInsert(CheckUtilityAppDataModel.CheckEntry checkEntries[]) {
		logger.traceEntry();
		
		for(CheckUtilityAppDataModel.CheckEntry curCheckEntry : checkEntries) {
			if(!StringUtils.isBlank(curCheckEntry.sourceCheckValue)) {
				return logger.traceExit(true);
			}
		}
		
		return logger.traceExit(false);
	}
	
	private boolean isRecordMatching(org.apache.poi.xssf.usermodel.XSSFRow entryRow, CheckUtilityAppDataModel.MatchEntry matchEntries[]) {
		logger.traceEntry();
				
		for(CheckUtilityAppDataModel.MatchEntry curMatchEntry : matchEntries) {
			String targetMatchValue = this.getCellValue(entryRow, curMatchEntry.matchPosition);
			if(!curMatchEntry.matchValue.equals(targetMatchValue)) {
				return logger.traceExit(false);
			}
		}
		
		return logger.traceExit(true);
	}
	
	private boolean checkCellEntry(org.apache.poi.xssf.usermodel.XSSFRow entryRow, CheckUtilityAppDataModel.CheckEntry checkEntry, String matchEntryName, boolean insertNewRecord, String fieldStatus) {
		logger.traceEntry();
		
		org.apache.poi.xssf.usermodel.XSSFCell checkValueCell = entryRow.getCell(checkEntry.checkPosition);
		String targetCheckString = (checkValueCell == null ? null : checkValueCell.getStringCellValue());
		logger.debug("checkString: " + targetCheckString);
		if(StringUtils.isBlank(checkEntry.sourceCheckValue)) {
			if(!StringUtils.isBlank(targetCheckString)) {
				logger.error("Missing " + checkEntry.objectDescription + " in data model design for " + matchEntryName + ": " + targetCheckString);
				checkValueCell.setCellStyle(this.errorCellStyle);
			}
		} else {
			
			if(checkEntry.sourceCheckValue.equals(targetCheckString)) {
				checkValueCell.setCellStyle(this.existingCellStyle);
				if(!"active".equalsIgnoreCase(fieldStatus)) {
					logger.error(matchEntryName + ": Status is not Active");
				}
			} else {
				insertNewRecord = true;
				if(checkValueCell == null) {
					checkValueCell = entryRow.createCell(checkEntry.checkPosition);
				}
				checkValueCell.setCellStyle(this.errorCellStyle);
				logger.error("Mismatch in " + checkEntry.objectDescription + " (" + matchEntryName + "). Data Model: " + checkEntry.sourceCheckValue + " Org: " + targetCheckString);
			}
		}
		return logger.traceExit(insertNewRecord);
	}
	
	private boolean checkMultipleEntry(org.apache.poi.xssf.usermodel.XSSFSheet checkSheet, CheckUtilityAppDataModel.MatchEntry matchEntries[], CheckUtilityAppDataModel.CheckEntry checkEntries[], String fieldStatus) {
		logger.traceEntry();
		
		boolean insertNewRecord = this.needsInsert(checkEntries);
				
		if(checkSheet == null) {
			return logger.traceExit(insertNewRecord);
		}
		
		Iterator<org.apache.poi.ss.usermodel.Row> rowIter = checkSheet.rowIterator();
		String matchEntryName = new String();
		for(CheckUtilityAppDataModel.MatchEntry curMatchEntry : matchEntries) {
			matchEntryName += curMatchEntry.matchValue + ".";
		}
		
		matchEntryName = StringUtils.removeEnd(matchEntryName, ".");
		
		while(rowIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFRow entryRow = (org.apache.poi.xssf.usermodel.XSSFRow) rowIter.next();
			
			if(this.isRecordMatching(entryRow, matchEntries)) {
				insertNewRecord = false;
				for(CheckUtilityAppDataModel.CheckEntry curCheckEntry : checkEntries) {
					insertNewRecord = this.checkCellEntry(entryRow, curCheckEntry, matchEntryName, insertNewRecord, fieldStatus);
				}
			
				return logger.traceExit(insertNewRecord);
			}
		}
		
		return logger.traceExit(insertNewRecord);
	}
	
	private boolean checkObjectTranslation(String entityName, String italianEntityName) {
		logger.traceEntry();
		
		boolean insertTranslation = !StringUtils.isBlank(italianEntityName);
		
		this.orgObjTranslationRow = -1;
		this.dmObjTranslationRow = -1;
		
		if(this.objTranslationSheet == null) {
			return logger.traceExit(insertTranslation);
		}
		
		Iterator<org.apache.poi.ss.usermodel.Row> objTranslationRowIter = this.objTranslationSheet.rowIterator();
		
		while(objTranslationRowIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFRow objTranslationRow = (org.apache.poi.xssf.usermodel.XSSFRow) objTranslationRowIter.next();
			
			org.apache.poi.xssf.usermodel.XSSFCell fileNameCell = null;
			if((fileNameCell = objTranslationRow.getCell(0)) != null) {
				if((entityName + "-it").equals(fileNameCell.getStringCellValue())) {
					this.orgObjTranslationRow = objTranslationRow.getRowNum();
					org.apache.poi.xssf.usermodel.XSSFCell valueCell = objTranslationRow.getCell(2);
					String orgObjectTranslation = (valueCell == null ? null : valueCell.getStringCellValue());
					logger.debug("orgObjectTranslation: " + orgObjectTranslation);
					if(StringUtils.isBlank(italianEntityName)) {
						if(!StringUtils.isBlank(orgObjectTranslation)) {
							logger.error("Missing object translation in data model design for " + entityName + ": " + orgObjectTranslation);
							valueCell.setCellStyle(this.errorCellStyle);
						}
					} else {
						
						if(italianEntityName.equals(orgObjectTranslation)) {
							insertTranslation = false;
							valueCell.setCellStyle(this.existingCellStyle);
						} else {
							if(valueCell == null) {
								valueCell = objTranslationRow.createCell(2);
							}
							valueCell.setCellStyle(this.errorCellStyle);
							logger.error("Mismatch in object translation. Data Model: " + italianEntityName + " Org: " + orgObjectTranslation);
						}
					}
					return logger.traceExit(insertTranslation);
				}
			}
				
		}
		
		return logger.traceExit(insertTranslation);
	}
	
	private void insertObjectTranslation(String entityName, String italianEntityName, String nameFieldLabel) {
		logger.traceEntry();
		if(this.objTranslationSheet == null) {
			String sheetName = "Object translation";
			String[] columnsList = {"filename", "plural", "value", "gender", "startsWith", "nameFieldLabel"};
			this.objTranslationSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.objTranslationSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.objTranslationSheet, columnsList, 1, 0);
			this.objTranslationSheetLastRow = 1;
		}
		
		logger.error(entityName + ": italian object translation missing.");
		org.apache.poi.xssf.usermodel.XSSFRow newObjTranslationRow = this.objTranslationSheet.createRow(++this.objTranslationSheetLastRow);
		this.dmObjTranslationRow = this.objTranslationSheetLastRow;
		
		String cellValues[] = {entityName + "-it", null, italianEntityName, null, null, nameFieldLabel};
		this.createStringCell(newObjTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();
		
	}
	
	private void insertNameField(String entityName, String fieldLabel) {
		
		if(this.nameFieldSheet == null) {
			String sheetName = "Name Fields";
			String[] columnsList = {"filename", "displayFormat", "label", "type"};
			this.nameFieldSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.nameFieldSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.nameFieldSheet, columnsList, 1, 0);
			this.nameFieldSheetLastRow = 1;
		}
		
		logger.error(entityName + ": italian object translation missing.");
		org.apache.poi.xssf.usermodel.XSSFRow newNameFieldRow = this.nameFieldSheet.createRow(++this.nameFieldSheetLastRow);
		
		String cellValues[] = {entityName, null, fieldLabel, null};
		this.createStringCell(newNameFieldRow, cellValues, this.newCellStyle);
		
		logger.traceExit();
		
	}
	
	private boolean checkNameTranslation(String entityName, String italianFieldTranslation) {
		logger.traceEntry();
		
		boolean insertNameTranslation = !StringUtils.isBlank(italianFieldTranslation);
		if(this.dmObjTranslationRow != -1) {
			insertNameTranslation = false;
			org.apache.poi.xssf.usermodel.XSSFRow dmObjTranslationRow = this.objTranslationSheet.getRow(this.dmObjTranslationRow);
			org.apache.poi.xssf.usermodel.XSSFCell dmitalianFieldTranslation = dmObjTranslationRow.getCell(5);
			if(dmitalianFieldTranslation == null) {
				dmitalianFieldTranslation = dmObjTranslationRow.createCell(5);
				dmitalianFieldTranslation.setCellStyle(this.newCellStyle);
			}
			dmitalianFieldTranslation.setCellValue(italianFieldTranslation);
		}
		
		if(this.objTranslationSheet == null || this.orgObjTranslationRow == -1) {
			return logger.traceExit(insertNameTranslation);
		}
		
		org.apache.poi.xssf.usermodel.XSSFRow orgObjTranslationRow = this.objTranslationSheet.getRow(this.orgObjTranslationRow);
		org.apache.poi.xssf.usermodel.XSSFCell orgItalianFieldTranslationCell = orgObjTranslationRow.getCell(5);
		String orgNameTranslation = (orgItalianFieldTranslationCell == null ? null : orgItalianFieldTranslationCell.getStringCellValue());
		
		if(StringUtils.isBlank(italianFieldTranslation)) {
			if(!StringUtils.isBlank(orgNameTranslation)) {
				logger.error("Missing object name translation in data model design for " + entityName + ": " + orgNameTranslation);
				orgItalianFieldTranslationCell.setCellStyle(this.errorCellStyle);
			}
		} else {
			
			if(italianFieldTranslation.equals(orgNameTranslation)) {
				insertNameTranslation = false;
				orgItalianFieldTranslationCell.setCellStyle(this.existingCellStyle);
			} else {
				if(orgItalianFieldTranslationCell == null) {
					orgItalianFieldTranslationCell = orgObjTranslationRow.createCell(5);
				}
				orgItalianFieldTranslationCell.setCellStyle(this.errorCellStyle);
				logger.error("Mismatch in object name translation. Data Model: " + italianFieldTranslation + " Org: " + orgNameTranslation);
			}
		}
		
		
		return logger.traceExit(insertNameTranslation);
	}
	

	
	private boolean checkField(String entityName, String fieldName, String fieldLabel, String fieldType, String fieldValueSetName, String fieldStatus) {
		
		logger.traceEntry();
		
		boolean insertField = true;
		if(this.fieldSheet == null) {
			return logger.traceExit(insertField); 
		}
		
		Iterator<org.apache.poi.ss.usermodel.Row> orgFieldsIter = this.fieldSheet.rowIterator();
		while(orgFieldsIter.hasNext()) {
			
			org.apache.poi.xssf.usermodel.XSSFRow orgFieldRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgFieldsIter.next();
			
			String orgFieldFilename = this.getCellValue(orgFieldRow, 0);
			
			if(entityName.equals(orgFieldFilename)) {
			
				String orgFieldFullname = this.getCellValue(orgFieldRow, 1);
				if(fieldName.equals(orgFieldFullname)) {
					
					insertField = false;
					org.apache.poi.xssf.usermodel.XSSFCell orgFieldLabelCell = orgFieldRow.getCell(2);
					String orgFieldLabel = (orgFieldLabelCell == null ? null : orgFieldLabelCell.getStringCellValue());
					logger.debug("orgFieldLabel: " + orgFieldLabel);
					if(fieldLabel.equals(orgFieldLabel)) {
						orgFieldLabelCell.setCellStyle(this.existingCellStyle);
						if(!"active".equalsIgnoreCase(fieldStatus)) {
							logger.error(entityName + "." + fieldName + ": Status is not Active");
						}
					} else {
						insertField = true;
						logger.error(entityName + "." + fieldName + ": Label mismatch. Data Model: " + fieldLabel + " Org: " + orgFieldLabel);
						orgFieldLabelCell.setCellStyle(this.errorCellStyle);
					}
					
					org.apache.poi.xssf.usermodel.XSSFCell orgFieldTypeCell = orgFieldRow.getCell(3);
					String orgFieldType = (orgFieldTypeCell == null ? null : orgFieldTypeCell.getStringCellValue());
					logger.debug("orgFieldType: " + orgFieldType);
					if(fieldType.equals(orgFieldType)) {
						orgFieldTypeCell.setCellStyle(this.existingCellStyle);
						if(!"active".equalsIgnoreCase(fieldStatus)) {
							logger.error(entityName + "." + fieldName + ": Status is not Active");
						}
					} else {
						insertField = true;
						logger.error(entityName + "." + fieldName + ": Type mismatch. Data Model: " + fieldType + " Org: " + orgFieldType);
						orgFieldTypeCell.setCellStyle(this.errorCellStyle);
					}
					
					org.apache.poi.xssf.usermodel.XSSFCell orgFieldValueSetsNameCell = orgFieldRow.getCell(21);
					String orgFieldValueSetsName = (orgFieldValueSetsNameCell == null ? null : orgFieldValueSetsNameCell.getStringCellValue());
					logger.debug("orgFieldValueSetsName: " + orgFieldValueSetsName);
					if(StringUtils.isBlank(fieldValueSetName)) {
						if(!StringUtils.isBlank(orgFieldValueSetsName)) {
							orgFieldValueSetsNameCell.setCellStyle(this.errorCellStyle);
							logger.error("Missing valueSetName in data model design for " + entityName  + "." + fieldName + ": " + orgFieldValueSetsName);
						}
					} else {
						if(fieldValueSetName.equals(orgFieldValueSetsName)) {
							orgFieldValueSetsNameCell.setCellStyle(this.existingCellStyle);
						} else {
							insertField = true;
							logger.error(entityName + "." + fieldName + ": valueSetName mismatch. Data Model: " + fieldValueSetName + " Org: " + orgFieldValueSetsName);
							orgFieldValueSetsNameCell.setCellStyle(this.errorCellStyle);
						}
					}
					return logger.traceExit(insertField);
					
				}
			}
			
		}
		
		return logger.traceExit(insertField);
	}
	
	private void insertField(String entityName, String fieldName, String fieldLabel, String fieldType, String fieldValueSetName, String fieldStatus, String sourceValue) {
		logger.traceEntry();
		
		if(this.fieldSheet == null) {
			String sheetName = "Fields";
			String[] columnsList = {"filename", "fullName", "label", "type", "length", "defaultValue", "scale", "precision", "externalId", 
					"required", "unique", "calculated", "deleteConstraint", "fieldManageability", "referenceTo", "relationshipLabel", 
					"relationshipName", "relationshipOrder", "reparentableMasterDetail", "writeRequiresMasterRead", "valueSet.restricted",
					"valueSetName", "valueSetDefinition.sorted"};
			

			this.fieldSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.fieldSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.fieldSheet, columnsList, 1, 0);
			this.fieldSheetLastRow = 1;
		}
		
		logger.error(entityName + "." + fieldName + ": field missing. Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newFieldRow = this.fieldSheet.createRow(++this.fieldSheetLastRow);
		
		String cellValues[] = {entityName, fieldName, fieldLabel, fieldType, null,
				null, null, null, null, null,
				null, null, null, null, null,
				null, null, null, null, null,
				null, fieldValueSetName, null, fieldStatus, sourceValue};
		this.createStringCell(newFieldRow, cellValues, this.newCellStyle);
		
		logger.traceExit();
		
	}
	
	private boolean checkFieldTranslation(String entityName, String fieldName, String italianFieldTranslation) {
		logger.traceEntry();
		
		boolean insertFieldTranslation = !StringUtils.isBlank(italianFieldTranslation);
		
		if(this.fieldTranslationSheet == null) {
			return logger.traceExit(insertFieldTranslation);
		}
		
		Iterator<org.apache.poi.ss.usermodel.Row> orgFieldsTranslationIter = this.fieldTranslationSheet.rowIterator();
		while(orgFieldsTranslationIter.hasNext()) {
			org.apache.poi.xssf.usermodel.XSSFRow orgFieldTranslationRow = (org.apache.poi.xssf.usermodel.XSSFRow) orgFieldsTranslationIter.next();
			
			String orgEntityName = this.getCellValue(orgFieldTranslationRow, 0);
			if((entityName + "-it").equals(orgEntityName)) {
				
				String orgFieldFullname = this.getCellValue(orgFieldTranslationRow, 1);
				if(fieldName.equals(orgFieldFullname)) {
					insertFieldTranslation = false;
					org.apache.poi.xssf.usermodel.XSSFCell orgFieldLabelTranslationCell = null;
					String orgFieldLabelTranslation = (orgFieldLabelTranslationCell = orgFieldTranslationRow.getCell(2)) == null ? null : orgFieldLabelTranslationCell.getStringCellValue();
					logger.debug("orgFieldLabelTranslation: " + orgFieldLabelTranslation);
					if(StringUtils.isBlank(italianFieldTranslation)) {
						if(!StringUtils.isBlank(orgFieldLabelTranslation)) {
							insertFieldTranslation = true;
							if(orgFieldLabelTranslationCell == null) {
								orgFieldLabelTranslationCell = orgFieldTranslationRow.createCell(2);
							}
							orgFieldLabelTranslationCell.setCellStyle(errorCellStyle);
							logger.error("Missing italian Label Translation in data model design for " + entityName  + "." + fieldName + ": " + orgFieldLabelTranslation);
						}
					} else {
						if(italianFieldTranslation.equals(orgFieldLabelTranslation)) {
							orgFieldLabelTranslationCell.setCellStyle(existingCellStyle);
							
						} else {
							insertFieldTranslation = true;
							logger.error(entityName + "." + fieldName + ": Label italian Translation mismatch. Data Model: " + italianFieldTranslation + " Org: " + orgFieldLabelTranslation);
							orgFieldLabelTranslationCell.setCellStyle(errorCellStyle);
						}
					}
					return logger.traceExit(insertFieldTranslation);
				}
			}
			
		}
		
		return logger.traceExit(insertFieldTranslation);
	}
	
	private void insertFieldTranslation(String entityName, String fieldName, String italianFieldTranslation, String fieldStatus, String sourceValue) {
		logger.traceEntry();
		
		if(this.fieldTranslationSheet == null) {
			String sheetName = "Fields translation";
			String[] columnsList = {"filename", "name", "label", "caseValues.plural", "caseValues.value", "relationshipLabel", "gender", "startsWith"};
		
			this.fieldTranslationSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.fieldTranslationSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.fieldTranslationSheet, columnsList, 1, 0);
			this.fieldTranslationSheetLastRow = 1;
		}
		
		logger.error(entityName + "." + fieldName + ": italian field translation missing. Translation: " + italianFieldTranslation + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newFieldTranslationRow = this.fieldTranslationSheet.createRow(++this.fieldTranslationSheetLastRow);
		
		String cellValues[] = {entityName + "-it", fieldName, italianFieldTranslation, null, null, null, null, null, fieldStatus, sourceValue};
		this.createStringCell(newFieldTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertStandardValueSet(String fieldName, String fieldValue, String fieldLabel, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.standardValueSetSheet == null) {
			String sheetName = "Standard valueSets";
			String[] columnsList = {"filename", "fullName", "default", "label", "groupingStringEnum", "groupingString", "cssExposed", "closed",
					"converted", "forecastCategory", "probability", "won", "reviewed", "highPriority"};

			this.standardValueSetSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.standardValueSetSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.standardValueSetSheet, columnsList, 1, 0);
			this.standardValueSetSheetLastRow = 1;
		}
		
		logger.error(fieldName + "." + fieldValue + ": standard valueset missing. Label: " + fieldLabel + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newStandardValueSetRow = this.standardValueSetSheet.createRow(++this.standardValueSetSheetLastRow);
		
		String cellValues[] = {fieldName, fieldValue, null, fieldLabel, null, null, null, null,
				null, null, null, null, null, null, fieldStatus, sourceValue};
		this.createStringCell(newStandardValueSetRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertStandardValueSetTranslation(String fieldName, String fieldLabel, String italianFieldTranslation, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.standardValueSetTranslationSheet == null) {
			String sheetName = "Standard valueSets translation";
			String[] columnsList = {"filename", "masterLabel", "translation"};

			this.standardValueSetTranslationSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.standardValueSetTranslationSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.standardValueSetTranslationSheet, columnsList, 1, 0);
			this.standardValueSetTranslationSheetLastRow = 1;
		}
		
		logger.error(fieldName + "." + fieldLabel + ": italian standard valueset missing. Value: " + italianFieldTranslation + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newStandardValueSetTranslationRow = this.standardValueSetTranslationSheet.createRow(++this.standardValueSetTranslationSheetLastRow);
		
		String cellValues[] = {fieldName, fieldLabel, italianFieldTranslation, fieldStatus, sourceValue};
		this.createStringCell(newStandardValueSetTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertGlobalValueSet(String fieldName, String fieldValue, String fieldLabel, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.globalValueSetSheet == null) {
			String sheetName = "Global valueSets";
			String[] columnsList = {"filename", "fullName", "default", "label"};

			this.globalValueSetSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.globalValueSetSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.globalValueSetSheet, columnsList, 1, 0);
			this.globalValueSetSheetLastRow = 1;
		}
		
		logger.error(fieldName + "." + fieldValue + ": global valueset missing. Label: " + fieldLabel + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newGlobalValueSetRow = this.globalValueSetSheet.createRow(++this.globalValueSetSheetLastRow);
		
		String cellValues[] = {fieldName, fieldValue, null, fieldLabel, fieldStatus, sourceValue};
		this.createStringCell(newGlobalValueSetRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertGlobalValueSetTranslation(String fieldName, String fieldLabel, String italianFieldTranslation, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.globalValueSetTranslationSheet == null) {
			String sheetName = "Global valueSets translation";
			String[] columnsList = {"filename", "masterLabel", "translation"};

			this.globalValueSetTranslationSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.globalValueSetTranslationSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.globalValueSetTranslationSheet, columnsList, 1, 0);
			this.globalValueSetTranslationSheetLastRow = 1;
		}
		
		logger.error(fieldName + "." + fieldLabel + ": italian standard valueset missing. Value: " + italianFieldTranslation + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newGlobalValueSetTranslationRow = this.globalValueSetTranslationSheet.createRow(++this.globalValueSetTranslationSheetLastRow);
		
		String cellValues[] = {fieldName, fieldLabel, italianFieldTranslation, fieldStatus, sourceValue};
		this.createStringCell(newGlobalValueSetTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertFieldValueSet(String entityName, String fieldName, String fieldValue, String fieldLabel, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.fieldValueSetsSheet == null) {
			String sheetName = "Fields valueSets";
			String[] columnsList = {"filename", "fullName", "value.position", "value.fullName", "value.default", "value.label"};

			this.fieldValueSetsSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.fieldValueSetsSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.fieldValueSetsSheet, columnsList, 1, 0);
			this.fieldValueSetsSheetLastRow = 1;
		}
		
		logger.error(entityName + "." + fieldName + "." + fieldValue + ": valueset missing. Label: " + fieldLabel + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newFieldValueSetRow = this.fieldValueSetsSheet.createRow(++this.fieldValueSetsSheetLastRow);
		
		String cellValues[] = {entityName, fieldName, null, fieldValue, null, fieldLabel, fieldStatus, sourceValue};
		this.createStringCell(newFieldValueSetRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertFieldValueSetTranslation(String entityName, String fieldName, String fieldLabel, String italianFieldTranslation, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.fieldValueSetTranslationSheet == null) {
			String sheetName = "Fields valueSets translations";
			String[] columnsList = {"filename", "label", "name", "masterLabel", "translation"};

			this.fieldValueSetTranslationSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.fieldValueSetTranslationSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.fieldValueSetTranslationSheet, columnsList, 1, 0);
			this.fieldValueSetTranslationSheetLastRow = 1;
		}
		
		logger.error(fieldName + "." + fieldLabel + ": italian standard valueset missing. Value: " + italianFieldTranslation + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newFieldValueSetTranslationRow = this.fieldValueSetTranslationSheet.createRow(++this.fieldValueSetTranslationSheetLastRow);
		
		String cellValues[] = {entityName, null, fieldName, fieldLabel, italianFieldTranslation, fieldStatus, sourceValue};
		this.createStringCell(newFieldValueSetTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	

	private void insertLabels(String fieldLabel, String fieldValue, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.labelSheet == null) {
			String sheetName = "Labels";
			String[] columnsList = {"filename", "fullName", "categories", "language", "protected", "shortDescription", "value"};

			this.labelSheet = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.labelSheet, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.labelSheet, columnsList, 1, 0);
			this.labelSheetLastRow = 1;
		}
		
		logger.error(fieldLabel + ": label missing. value: " + fieldValue + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newLabelRow = this.labelSheet.createRow(++this.labelSheetLastRow);
		
		String cellValues[] = {fieldLabel, null, null, null, null, null, fieldValue, fieldStatus, sourceValue};
		this.createStringCell(newLabelRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
	private void insertLabelTranslation(String fieldLabel, String fieldValue, String fieldStatus, String sourceValue) {
		
		logger.traceEntry();
		
		if(this.labelSheetTranslation == null) {
			String sheetName = "Label translation";
			String[] columnsList = {"filename", "name", "label"};

			this.labelSheetTranslation = this.orgWb.createSheet(sheetName);
			this.createMetadataHeader(this.labelSheetTranslation, "Metadata " + sheetName, columnsList.length, 0, 0);
			this.createSheetHeader(this.labelSheetTranslation, columnsList, 1, 0);
			this.labelSheetTranslationLastRow = 1;
		}
		
		logger.error(fieldLabel + ": label missing. value: " + fieldValue + " Status: " + fieldStatus + " Source: " + sourceValue);
		org.apache.poi.xssf.usermodel.XSSFRow newLabelTranslationRow = this.labelSheetTranslation.createRow(++this.labelSheetTranslationLastRow);
		
		String cellValues[] = {"it", fieldLabel, fieldValue, fieldValue, fieldStatus, sourceValue};
		this.createStringCell(newLabelTranslationRow, cellValues, this.newCellStyle);
		
		logger.traceExit();	
	}
	
}
