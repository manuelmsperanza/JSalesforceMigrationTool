package com.hoffnungland.jSFDCMigrTool;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hoffnungland.poi.corner.dbxlsreport.ExcelManager;

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

	public void createSheetList(String sheetName, String[] columnsList, List<String[]> data, Set<String> pivotColumnList, JsonArray jsonArray) {
		logger.traceEntry();
		org.apache.poi.xssf.usermodel.XSSFSheet workSheet = this.wb.createSheet(sheetName);
		
		int columnListSize = columnsList.length;
		String[] columnListJoin = null;
		if(pivotColumnList != null && pivotColumnList.size() > 0) {
			columnListSize += pivotColumnList.size();
			columnListJoin = new String[columnListSize];
			int columnIdx = 0;
			for(String curColumnValue : columnsList) {
				columnListJoin[columnIdx++] =  curColumnValue;
			}
			for(String curPivotColumnValue : pivotColumnList) {
				columnListJoin[columnIdx++] =  curPivotColumnValue;
			}
		} else {
			columnListJoin = columnsList;
		}
		
		this.createMetadataHeader(workSheet, "Metadata " + sheetName, columnListSize, 0, 0);
		this.createSheetHeader(workSheet, columnListJoin, 1, 0);
		int rowOffset = 2;
		int rowId = 0;
		for(String[] row : data) {
			org.apache.poi.xssf.usermodel.XSSFRow bodyRow = workSheet.getRow(rowOffset + rowId);
			if(bodyRow == null) {
				bodyRow = workSheet.createRow(rowOffset + rowId);
			}
			int cellIdx = 0;
			for(String cell : row) {
				
				org.apache.poi.xssf.usermodel.XSSFCell bundleNameCell = bodyRow.getCell(cellIdx);
				
				if(bundleNameCell == null) {
					bundleNameCell = bodyRow.createCell(cellIdx);
				}
				
				bundleNameCell.setCellStyle(this.defaultCellStyle);
				bundleNameCell.setCellValue(cell);
				cellIdx++;
			}
			
			if(pivotColumnList != null && pivotColumnList.size() > 0) {
				
				JsonObject pivotRow = (JsonObject) jsonArray.get(rowId);
				
				for(String curPivotColumnValue : pivotColumnList) {
					
					org.apache.poi.xssf.usermodel.XSSFCell bundleNameCell = bodyRow.getCell(cellIdx);
					
					if(bundleNameCell == null) {
						bundleNameCell = bodyRow.createCell(cellIdx);
					}
					
					bundleNameCell.setCellStyle(this.defaultCellStyle);
					JsonElement pivotRowElement = pivotRow.get(curPivotColumnValue);
					if(pivotRowElement != null) {						
						bundleNameCell.setCellValue(pivotRowElement.getAsString());
					}
					cellIdx++;
				}
			}
			
			rowId++;
		}
		
		workSheet.createFreezePane(0, 2);
		workSheet.setZoom(85);
		
		logger.traceExit();
		
	}
}
