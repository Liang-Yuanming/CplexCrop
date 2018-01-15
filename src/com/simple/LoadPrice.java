package com.simple;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class LoadPrice {
	// 檔案路徑與名稱
	public static String FILENAMES[] = { "10507-10606.xls", "10407-10506.xls", "10307-10406.xls", "10207-10306.xls",
			"10107-10206.xls", "10007-10106.xls", "09907-10006.xls", "09807-09906.xls", "09707-09806.xls",
			"09607-09706.xls"};
	public final static String FILENAMES_X[] = { "10507-10606.xlsx", "10407-10506.xlsx", "10307-10406.xlsx",
			"10207-10306.xlsx", "10107-10206.xlsx", "10007-10106.xlsx", "09907-10006.xlsx", "09807-09906.xlsx",
			"09707-09806.xlsx", "09607-09706.xlsx"  };
	public static String FILEPATH = "C:\\Users\\mike\\Documents\\Agri-data\\price\\";
	public static String FILEPATHOTHER = "C:\\Users\\mike\\Documents\\Agri-data\\priceflower\\";
	public static String BEGINDATE[] = { "2016/07/01", "2015/07/01", "2014/07/01", "2013/07/01", "2012/07/01",
			"2011/07/01", "2010/07/01", "2009/07/01", "2008/07/01", "2007/07/01" };
	public static String SHEETNAME = "Sheet1";
	// public static String
	// fileToBeRead="/Users/mike/Dropbox/成大資料/研究/Agri-data/10407-10506.xls";

	public Scenario[] scenario;

	public LoadPrice(Scenario[] s) {
		scenario = s;
	}

	public void start() {
		System.out.println("---------Loading data ..... ---------");
		try {

			for (int i = 0; i < FILENAMES.length; i++) {

				String fileName = FILEPATH + FILENAMES[i];
				System.out.println("Load file name : " + FILENAMES[i]);
				HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(fileName));
				HSSFSheet sheet = workbook.getSheet(SHEETNAME);
				SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
				Date beginDate = df.parse(BEGINDATE[i]);
				for (int x = 1; x < sheet.getPhysicalNumberOfRows() - 1; x++) {

					HSSFRow row = sheet.getRow(x);
					// 日期
					HSSFCell dateCell = row.getCell((short) 12);

					Date endDate = df.parse(dateCell.getStringCellValue());
					long betweenDate = ((endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24));
					int k = (int) betweenDate/7;
					// 品種
					HSSFCell jCell = row.getCell((short) 3);
					int j = Common.searchJ(jCell.getStringCellValue());
					if (j == -1)
						continue;
					// 市場
					HSSFCell mCell = row.getCell((short) 1);
					int m = Common.searchM(mCell.getStringCellValue());
					// 品質
					HSSFCell aCell = row.getCell((short) 6);
					int a = Common.searchA(aCell.getStringCellValue());
					if (a == -1)
						continue;
					// 價格
					HSSFCell pCell = row.getCell((short) 9);
					int pr = (int) pCell.getNumericCellValue();

					if (scenario[i].getPrice()[m][j][k] == 0) {
						scenario[i].setPrice(m, j, k, pr);
					} else if (scenario[i].getPrice()[m][j][k] != 0 && scenario[i].getSupply()[m][j][k] == 0) {
						int prr = (pr + scenario[i].getPrice()[m][j][k]) / 2;
						scenario[i].setPrice(m, j, k, prr);
					}

					HSSFCell supplyCell = row.getCell((short) 8);
					HSSFCell boxCell = row.getCell((short) 7);
					int supply = (int) supplyCell.getNumericCellValue();
					int boxNumber = (int) boxCell.getNumericCellValue();
					int avageDens = supply / boxNumber;
					if (scenario[i].getDens()[m][j][k] == 0) {
						scenario[i].setDens(m, j, k, avageDens);
					} else {
						scenario[i].setDens(m, j, k, (scenario[i].getDens()[m][j][k] + avageDens) / 2);
					}

				}
				
				
			}

			System.out.println("---------Finish load data---------");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("---------Loading data 2 Other price ..... ---------");
		try {

			for (int i = 0; i < FILENAMES_X.length; i++) {

				String fileName = FILEPATHOTHER + FILENAMES_X[i];
				System.out.println("Load file name : " + FILENAMES_X[i]);
				Workbook workbook = new XSSFWorkbook(new FileInputStream(new File(fileName)));
				Sheet sheet = workbook.getSheetAt(0);
				SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
				Date beginDate = df.parse(BEGINDATE[i]);
				Iterator<Row> rowIterator = sheet.iterator();
				int x = 0;
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Iterator<Cell> cellIterator = row.cellIterator();

					Date endDate;
					long betweenDate;
					int k = 0, j = 0, m = 0, pr = 0;
					if (x != 0) {
						int y = 0;
						while (cellIterator.hasNext()) {
							Cell cell = cellIterator.next();
							// System.out.println(y+"="+cell.getStringCellValue());
							switch (y) {
							case 14:
								endDate = df.parse(cell.getStringCellValue());
								betweenDate = ((endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24));
								k = (int) betweenDate/7;
								break;
							case 3:
								j = Common.searchJ(cell.getStringCellValue());
								break;
							case 2:
								m = Common.searchM(cell.getStringCellValue());
								break;
							case 9:
								pr = (int) cell.getNumericCellValue();
								break;
							}
							y++;
						}
						if (m != -1 && j != -1) {
							if (scenario[i].getPrice()[m][j][k] == 0) {
								scenario[i].setPrice(m, j, k, pr);
							}
						}
					}
					x++;
				}
//				for (int m = 0; m < Common.M; m++) {
//					for (int j = 0; j < Common.J; j++) {
//						for (int k = 0; k < Common.K; k++) {
//							if (scenario[i].getDens()[m][j][k] == 0) {
//								scenario[i].setDens(m, j, k, 10);
//							}
//						}
//					}
//				}
			}

			System.out.println("---------Finish load data---------");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
