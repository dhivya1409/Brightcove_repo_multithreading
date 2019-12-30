package com.brightcove.utils;

import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public final class ExcelHelper {

	private ExcelHelper() {
		/*
		 * Private constructor
		 * */
	}
	
	static int rownum = 0;
	
	public static void writeToExcelSheet(XSSFSheet sheet, Map<String, String[]> data) {
		// Iterate over data and write to sheet
				Set<String> keyset = data.keySet();

				for (String key : keyset) {
					Row row = sheet.createRow(rownum++);
					Object[] objArr = data.get(key);
					int cellnum = 0;
					for (Object obj : objArr) {
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);
					}
				}
	}
}
