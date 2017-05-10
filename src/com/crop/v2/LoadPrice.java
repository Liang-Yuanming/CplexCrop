package com.crop.v2;

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

import com.model.Scenario;

public class LoadPrice {
	// 檔案路徑與名稱
	public static String FILENAMES[] = { "10507-10606.xls", "10407-10506.xls", "10307-10406.xls", "10207-10306.xls",
			"10107-10206.xls", "10007-10106.xls", "09907-10006.xls", "09807-09906.xls", "09707-09806.xls",
			"09607-09706.xls" };
	public final static String FILENAMES_X[] = { "10507-10606.xlsx", "10407-10506.xlsx", "10307-10406.xlsx",
			"10207-10306.xlsx", "10107-10206.xlsx", "10007-10106.xlsx", "09907-10006.xlsx", "09807-09906.xlsx",
			"09707-09806.xlsx", "09607-09706.xlsx" };
	public static String FILEPATH = "/home/w87754/Desktop/price/";
	public static String FILEPATHOTHER = "/home/w87754/Desktop/priceflower/";
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
				double YA[][] = new double[Common.J][Common.A];
				double YAPe[][] = new double[Common.J][Common.A];
				double YAPe2[][] = new double[Common.J][Common.A];
				for (int x = 1; x < sheet.getPhysicalNumberOfRows() - 1; x++) {

					HSSFRow row = sheet.getRow(x);
					// 日期
					HSSFCell dateCell = row.getCell((short) 12);

					Date endDate = df.parse(dateCell.getStringCellValue());
					long betweenDate = ((endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60 * 24));
					int k = (int) betweenDate;
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

					if (scenario[i].getPrice()[m][j][k][a] == 0) {
						scenario[i].setPrice(m, j, k, a, pr);
					} else if (scenario[i].getPrice()[m][j][k][a] != 0 && scenario[i].getSupply()[m][j][k][a] == 0) {
						int prr = (pr + scenario[i].getPrice()[m][j][k][a]) / 2;
						scenario[i].setPrice(m, j, k, a, prr);
					}

					HSSFCell supplyCell = row.getCell((short) 8);
					HSSFCell boxCell = row.getCell((short) 7);
					int supply = (int) supplyCell.getNumericCellValue();
					int boxNumber = (int) boxCell.getNumericCellValue();
					int avageDens = supply / boxNumber;
					if (scenario[i].getDens()[m][j][k][a] == 0) {
						scenario[i].setDens(m, j, k, a, avageDens);
					} else {
						scenario[i].setDens(m, j, k, a, (scenario[i].getDens()[m][j][k][a] + avageDens) / 2);
					}

					YA[j][a] += supply;

				}
				for (int j = 0; j < Common.J; j++) {
					double totalJ = 0;
					// for(int a=0;a<Common.A;a++){
					// totalJ+=YA[j][a];
					// }

					 for(int a=0;a<Common.A;a++){
						 if(YA[j][a]==0){
//							 YAPe[j][a]=0.2;
							 YAPe2[j][a]=0.5;
						 }else{
//							 YAPe[j][a]=(YA[j][a]/totalJ)+0.5;
							 YAPe2[j][a]=0.8;
						 }
					 }
					if (totalJ == 0) {
						// for(int a=0;a<Common.A;a++){
						// YAPe[j][a]=0.8;
						// }
						// System.out.println(Common.JSTR[j]);
						switch (Common.JSTR[j]) {
						case "FH293":
							YAPe[j][11] = 1;
							break;
						case "FH298":
							YAPe[j][3] = 0.6;
							YAPe[j][2] = 0.3;
							YAPe[j][0] = 0.1;
							break;
						case "FH364":
							YAPe[j][18] = 1;
							break;
						case "FH630":
							YAPe[j][6] = 1;
							break;
						case "FH631":
							YAPe[j][9] = 0.3;
							YAPe[j][6] = 0.3;
							YAPe[j][5] = 0.4;
							break;
						case "FH633":
							YAPe[j][7] = 0.6;
							YAPe[j][6] = 0.4;
							break;
						case "FH634":
							YAPe[j][8] = 0.3;
							YAPe[j][7] = 0.5;
							YAPe[j][6] = 0.2;
							break;
						case "FH636":
							YAPe[j][7] = 1;
							break;
						case "FH661":
//							YAPe[j][0] = 0.003;
							YAPe[j][2] = 0.623;
//							YAPe[j][3] = 0.001;
							YAPe[j][11] = 0.202;
							YAPe[j][10] = 0.113;
//							YAPe[j][9] = 0.050;
							YAPe[j][9] = 0.050;
//							YAPe[j][8] = 0.008;
							break;
						case "FK410":
							YAPe[j][0] = 0.2;
							YAPe[j][8] = 0.4;
							YAPe[j][7] = 0.4;
							break;
						case "FK411":
							YAPe[j][11] = 0.01;
							YAPe[j][9] = 0.26;
							YAPe[j][8] = 0.56;
							YAPe[j][7] = 0.17;
							break;
						case "FK412":
							YAPe[j][10] = 0.13;
							YAPe[j][9] = 0.37;
							YAPe[j][8] = 0.37;
							YAPe[j][7] = 0.15;
							break;
						case "FK413":
							YAPe[j][11] = 0.1;
							YAPe[j][10] = 0.21;
							YAPe[j][9] = 0.53;
							YAPe[j][8] = 0.16;
							break;
						case "FK419":
							YAPe[j][2] = 0.7;
							YAPe[j][11] = 0.28;
							YAPe[j][10] = 0.02;
							break;
						case "FK420":
							YAPe[j][0] = 0.075;
							YAPe[j][11] = 0.125;
							YAPe[j][10] = 0.125;
							YAPe[j][9] = 0.2;
							YAPe[j][8] = 0.3;
							YAPe[j][7] = 0.175;
							break;
						case "FK422":
							YAPe[j][0] = 0.066;
							YAPe[j][9] = 0.295;
							YAPe[j][8] = 0.508;
							YAPe[j][7] = 0.131;
							break;
						case "FK423":
							YAPe[j][0] = 0.022;
							YAPe[j][2] = 0.111;
							YAPe[j][11] = 0.222;
							YAPe[j][10] = 0.222;
							YAPe[j][9] = 0.256;
							YAPe[j][8] = 0.144;
							YAPe[j][7] = 0.011;
							YAPe[j][22] = 0.011;
							break;
						case "FK429":
							YAPe[j][2] = 0.5;
							YAPe[j][3] = 0.220;
							YAPe[j][11] = 0.119;
							YAPe[j][10] = 0.073;
							YAPe[j][9] = 0.081;
							YAPe[j][8] = 0.008;
							break;
						case "FK433":
							YAPe[j][2] = 1;
							break;
						case "FK442":
							YAPe[j][11] = 0.16;
							YAPe[j][10] = 0.75;
							YAPe[j][9] = 0.07;
							YAPe[j][8] = 0.02;
							break;
						case "FK443":
							YAPe[j][2] = 0.96;
							YAPe[j][11] = 0.02;
							YAPe[j][10] = 0.02;
							break;
						case "FK449":
							YAPe[j][2] = 0.93;
							YAPe[j][22] = 0.07;
							break;
						case "FS000":
							YAPe[j][10] = 1;
							break;
						case "FS002":
							YAPe[j][10] = 1;
							break;
						case "FS003":
							YAPe[j][2] = 0.5;
							YAPe[j][11] = 0.5;
							break;
						case "FS009":
							YAPe[j][2] = 0.16;
							YAPe[j][11] = 0.46;
							YAPe[j][10] = 0.35;
							YAPe[j][9] = 0.03;
							break;
						case "FS010":
							YAPe[j][2] = 0.083;
							YAPe[j][11] = 0.167;
							YAPe[j][10] = 0.5;
							YAPe[j][9] = 0.167;
							YAPe[j][19] = 0.083;
							break;
						case "FS012":
							YAPe[j][2] = 0.36;
							YAPe[j][11] = 0.44;
							YAPe[j][10] = 0.16;
							YAPe[j][9] = 0.04;
							break;
						case "FS013":
							YAPe[j][2] = 0.386;
							YAPe[j][11] = 0.432;
							YAPe[j][10] = 0.182;
							break;
						case "FS019":
							YAPe[j][2] = 0.748;
							YAPe[j][11] = 0.215;
							YAPe[j][10] = 0.037;
							break;
						case "FS030":
							YAPe[j][11] = 0.5;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.2;
							YAPe[j][8] = 0.1;
							break;
						case "FS031":
							YAPe[j][0] = 0.05;
							YAPe[j][11] = 0.7;
							YAPe[j][10] = 0.05;
							YAPe[j][9] = 0.2;
							break;
						case "FS032":
							YAPe[j][0] = 0.01;
							YAPe[j][2] = 0.56;
							YAPe[j][11] = 0.24;
							YAPe[j][10] = 0.13;
							YAPe[j][9] = 0.07;
							break;
						case "FS033":
							YAPe[j][2] = 0.7;
							YAPe[j][11] = 0.1;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.02;
							break;
						case "FS039":
							YAPe[j][2] = 0.8;
							YAPe[j][10] = 0.2;
							break;
						case "FS040":
							YAPe[j][11] = 0.038;
							YAPe[j][10] = 0.269;
							YAPe[j][9] = 0.346;
							YAPe[j][8] = 0.155;
							YAPe[j][7] = 0.192;
							break;
						case "FS041":
							YAPe[j][0] = 0.06;
							YAPe[j][11] = 0.12;
							YAPe[j][10] = 0.58;
							YAPe[j][9] = 0.24;
							break;
						case "FS042":
							YAPe[j][0] = 0.012;
							YAPe[j][11] = 0.293;
							YAPe[j][10] = 0.311;
							YAPe[j][9] = 0.168;
							YAPe[j][8] = 0.144;
							YAPe[j][7] = 0.054;
							YAPe[j][19] = 0.018;
							break;
						case "FS043":
							YAPe[j][2] = 0.091;
							YAPe[j][11] = 0.406;
							YAPe[j][10] = 0.294;
							YAPe[j][9] = 0.137;
							YAPe[j][8] = 0.056;
							YAPe[j][20] = 0.010;
							YAPe[j][19] = 0.005;
							break;
						case "FS049":
							YAPe[j][0] = 0.012;
							YAPe[j][2] = 0.318;
							YAPe[j][11] = 0.376;
							YAPe[j][10] = 0.129;
							YAPe[j][9] = 0.129;
							YAPe[j][8] = 0.035;
							break;
						case "FS060":
							YAPe[j][8] = 1;
							break;
						case "FS130":
							YAPe[j][0] = 0.2;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.4;
							YAPe[j][8] = 0.2;
							break;
						case "FS132":
							YAPe[j][10] = 0.6;
							YAPe[j][9] = 0.3;
							YAPe[j][8] = 0.3;
							break;
						case "FS133":
							YAPe[j][11] = 0.25;
							YAPe[j][10] = 0.54;
							YAPe[j][9] = 0.21;
							break;
						case "FS139":
							YAPe[j][11] = 0.65;
							YAPe[j][10] = 0.30;
							YAPe[j][9] = 0.05;
							break;
						case "FS230":
							YAPe[j][11] = 0.25;
							YAPe[j][10] = 0.25;
							YAPe[j][9] = 0.5;
							break;
						case "FS231":
							YAPe[j][10] = 0.5;
							YAPe[j][9] = 0.5;
							break;
						case "FS232":
							YAPe[j][2] = 0.12;
							YAPe[j][11] = 0.63;
							YAPe[j][10] = 0.25;
							break;
						case "FS233":
							YAPe[j][2] = 0.45;
							YAPe[j][11] = 0.45;
							YAPe[j][10] = 0.1;
							break;
						case "FS239":
							YAPe[j][2] = 0.84;
							YAPe[j][11] = 0.12;
							YAPe[j][3] = 0.04;
							break;
						case "FS350":
							YAPe[j][0] = 0.048;
							YAPe[j][11] = 0.048;
							YAPe[j][10] = 0.262;
							YAPe[j][9] = 0.475;
							YAPe[j][8] = 0.095;
							YAPe[j][7] = 0.024;
							YAPe[j][21] = 0.024;
							YAPe[j][20] = 0.024;
							break;
						case "FS351":
							YAPe[j][8] = 1;
							break;
						case "FS352":
							YAPe[j][9] = 0.125;
							YAPe[j][8] = 0.125;
							YAPe[j][7] = 0.75;
							break;
						case "FS353":
							YAPe[j][8] = 0.75;
							YAPe[j][7] = 0.25;
							break;
						case "FS359":
							YAPe[j][0] = 0.006;
							YAPe[j][2] = 0.446;
							YAPe[j][11] = 0.322;
							YAPe[j][10] = 0.191;
							YAPe[j][9] = 0.032;
							YAPe[j][8] = 0.003;
							break;
						case "FS360":
							YAPe[j][0] = 0.1;
							YAPe[j][2] = 0.3;
							YAPe[j][11] = 0.2;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.1;
							YAPe[j][8] = 0.1;
							break;
						case "FS361":
							YAPe[j][10] = 1;
							break;
						case "FS362":
							YAPe[j][0] = 0.03;
							YAPe[j][2] = 0.03;
							YAPe[j][11] = 0.25;
							YAPe[j][9] = 0.29;
							YAPe[j][8] = 0.32;
							YAPe[j][7] = 0.08;
							break;
						case "FS363":
							YAPe[j][2] = 0.66;
							YAPe[j][11] = 0.03;
							YAPe[j][10] = 0.08;
							YAPe[j][9] = 0.2;
							YAPe[j][8] = 0.03;
							break;
						case "FS369":
							YAPe[j][2] = 0.7;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.1;
							break;
						case "FS410":
							YAPe[j][11] = 0.43;
							YAPe[j][10] = 0.39;
							YAPe[j][9] = 0.35;
							YAPe[j][9] = 0.13;
							break;
						case "FS411":
							YAPe[j][9] = 1;
							break;
						case "FS412":
							YAPe[j][0] = 0.01;
							YAPe[j][11] = 0.03;
							YAPe[j][10] = 0.59;
							YAPe[j][9] = 0.14;
							YAPe[j][8] = 0.15;
							YAPe[j][7] = 0.08;
							break;
						case "FS413":
							YAPe[j][0] = 0.02;
							YAPe[j][11] = 0.36;
							YAPe[j][10] = 0.52;
							YAPe[j][9] = 0.06;
							YAPe[j][8] = 0.04;
							break;
						case "FS419":
							YAPe[j][0] = 0.02;
							YAPe[j][11] = 0.56;
							YAPe[j][10] = 0.40;
							YAPe[j][9] = 0.02;
							break;
						case "FS430":
							YAPe[j][11] = 0.16;
							YAPe[j][10] = 0.16;
							YAPe[j][9] = 0.52;
							YAPe[j][8] = 0.16;
							break;
						case "FS431":
							YAPe[j][8] = 1;
							break;
						case "FS432":
							YAPe[j][10] = 0.67;
							YAPe[j][9] = 0.25;
							YAPe[j][8] = 0.08;
							break;
						case "FS433":
							YAPe[j][11] = 0.28;
							YAPe[j][10] = 0.59;
							YAPe[j][9] = 0.13;
							break;
						case "FS439":
							YAPe[j][11] = 0.5;
							YAPe[j][10] = 0.36;
							YAPe[j][9] = 0.14;
							break;
						case "FS440":
							YAPe[j][0] = 0.058;
							YAPe[j][2] = 0.010;
							YAPe[j][11] = 0.058;
							YAPe[j][10] = 0.168;
							YAPe[j][9] = 0.254;
							YAPe[j][8] = 0.261;
							YAPe[j][7] = 0.165;
							YAPe[j][6] = 0.017;
							YAPe[j][18] = 0.003;
							YAPe[j][20] = 0.003;
							break;
						case "FS441":
//							YAPe[j][0] = 0.041;
//							YAPe[j][10] = 0.123;
//							YAPe[j][9] = 0.301;
							YAPe[j][9] = 0.3;
//							YAPe[j][8] = 0.274;
							YAPe[j][8] = 0.3;
							YAPe[j][7] = 0.4;
//							YAPe[j][7] = 0.3;
//							YAPe[j][6] = 0.041;
							break;
						case "FS442":
							YAPe[j][0] = 0.018;
							YAPe[j][11] = 0.105;
							YAPe[j][10] = 0.227;
							YAPe[j][9] = 0.252;
							YAPe[j][8] = 0.249;
							YAPe[j][7] = 0.128;
							YAPe[j][6] = 0.021;
							YAPe[j][17] = 0.001;
							break;
						case "FS443":
							YAPe[j][0] = 0.013;
							YAPe[j][2] = 0.084;
							YAPe[j][11] = 0.316;
							YAPe[j][10] = 0.229;
							YAPe[j][9] = 0.222;
							YAPe[j][8] = 0.113;
							YAPe[j][7] = 0.020;
							YAPe[j][6] = 0.001;
							YAPe[j][20] = 0.001;
							YAPe[j][19] = 0.001;
							break;
						case "FS449":
							YAPe[j][10] = 0.6;
							YAPe[j][9] = 0.4;
							YAPe[j][8] = 0.02;
							break;
						case "FS450":
							YAPe[j][0] = 0.04;
							YAPe[j][11] = 0.16;
							YAPe[j][10] = 0.28;
							YAPe[j][9] = 0.26;
							YAPe[j][8] = 0.24;
							YAPe[j][7] = 0.02;
							break;
						case "FS451":
							YAPe[j][9] = 0.4;
							YAPe[j][8] = 0.6;
							break;
						case "FS452":
							YAPe[j][0] = 0.03;
							YAPe[j][11] = 0.03;
							YAPe[j][10] = 0.3;
							YAPe[j][9] = 0.33;
							YAPe[j][8] = 0.22;
							YAPe[j][7] = 0.08;
							YAPe[j][6] = 0.01;
							break;
						case "FS453":
							YAPe[j][0] = 0.02;
							YAPe[j][11] = 0.39;
							YAPe[j][10] = 0.3;
							YAPe[j][9] = 0.23;
							YAPe[j][8] = 0.05;
							YAPe[j][19] = 0.01;
							break;
						case "FS459":
							YAPe[j][0] = 0.01;
							YAPe[j][2] = 0.34;
							YAPe[j][11] = 0.32;
							YAPe[j][10] = 0.20;
							YAPe[j][9] = 0.1;
							YAPe[j][8] = 0.02;
							YAPe[j][19] = 0.01;
							break;
						case "FS460":
							YAPe[j][2] = 0.154;
							YAPe[j][11] = 0.077;
							YAPe[j][9] = 0.615;
							YAPe[j][8] = 0.077;
							YAPe[j][20] = 0.077;
							break;
							
						case "FS463":
							YAPe[j][11] = 0.286;
							YAPe[j][10] = 0.571;
							YAPe[j][8] = 0.143;
							break;
						case "FS469":
							YAPe[j][2] = 1;
							break;
						case "FS539":
							YAPe[j][9] = 1;
							break;
						case "FS560":
							YAPe[j][11] = 0.24;
							YAPe[j][10] = 0.15;
							YAPe[j][9] = 0.46;
							YAPe[j][8] = 0.15;
							break;
						case "FS562":
							YAPe[j][10] = 0.6;
							YAPe[j][9] = 0.33;
							YAPe[j][8] = 0.07;
							break;
						case "FS563":
							YAPe[j][11] = 0.5;
							YAPe[j][10] = 0.4;
							YAPe[j][9] = 0.1;
							break;
						case "FS569":
							YAPe[j][0] = 0.02;
							YAPe[j][11] = 0.69;
							YAPe[j][10] = 0.29;
							break;
						case "FS570":
							YAPe[j][11] = 0.5;
							YAPe[j][10] = 0.167;
							YAPe[j][9] = 0.333;
							break;
						case "FS571":
							YAPe[j][9] = 1;
							break;
						case "FS572":
							YAPe[j][0] = 0.024;
							YAPe[j][11] = 0.060;
							YAPe[j][10] = 0.202;
							YAPe[j][9] = 0.107;
							YAPe[j][8] = 0.393;
							YAPe[j][7] = 0.214;
							break;
						case "FS573":
							YAPe[j][2] = 0.052;
							YAPe[j][11] = 0.148;
							YAPe[j][10] = 0.087;
							YAPe[j][9] = 0.174;
							YAPe[j][8] = 0.443;
							YAPe[j][7] = 0.096;
							break;
						case "FS579":
							YAPe[j][2] = 0.242;
							YAPe[j][3] = 0.011;
							YAPe[j][11] = 0.495;
							YAPe[j][10] = 0.099;
							YAPe[j][9] = 0.115;
							YAPe[j][8] = 0.038;
							break;
						case "FS590":
							YAPe[j][2] = 0.090;
							YAPe[j][11] = 0.227;
							YAPe[j][10] = 0.182;
							YAPe[j][9] = 0.409;
							YAPe[j][8] = 0.090;
							break;
						case "FS591":
							YAPe[j][10] = 0.875;
							YAPe[j][9] = 0.125;
							break;
						case "FS592":
							YAPe[j][0] = 0.026;
							YAPe[j][11] = 0.302;
							YAPe[j][10] = 0.466;
							YAPe[j][9] = 0.168;
							YAPe[j][8] = 0.039;
							break;
						case "FS593":
							YAPe[j][2] = 0.33;
							YAPe[j][11] = 0.57;
							YAPe[j][10] = 0.05;
							YAPe[j][12] = 0.04;
							break;
						case "FS599":
							YAPe[j][2] = 0.984;
							YAPe[j][11] = 0.02;
							break;
						case "FS600":
							YAPe[j][0] = 0.036;
							YAPe[j][2] = 0.043;
							YAPe[j][11] = 0.152;
							YAPe[j][10] = 0.203;
							YAPe[j][9] = 0.174;
							YAPe[j][8] = 0.181;
							YAPe[j][7] = 0.087;
							YAPe[j][13] = 0.007;
							YAPe[j][22] = 0.014;
							YAPe[j][21] = 0.014;
							YAPe[j][20] = 0.058;
							YAPe[j][19] = 0.029;
							break;
						case "FS601":
							YAPe[j][0] = 0.008;
							YAPe[j][11] = 0.005;
							YAPe[j][10] = 0.151;
							YAPe[j][9] = 0.496;
							YAPe[j][8] = 0.301;
							YAPe[j][7] = 0.036;
							YAPe[j][20] = 0.003;
							break;
						case "FS602":
							YAPe[j][11] = 0.2;
							YAPe[j][10] = 0.6;
							YAPe[j][9] = 0.4;
							YAPe[j][8] = 0.2;
							YAPe[j][20] = 0.02;
							YAPe[j][19] = 0.01;
							break;
						case "FS603":
							YAPe[j][0] = 0.03;
//							YAPe[j][2] = 0.193;
							YAPe[j][2] = 0.2;
//							YAPe[j][3] = 0.036;
//							YAPe[j][11] = 0.351;
							YAPe[j][11] = 0.4;
//							YAPe[j][10] = 0.272;
							YAPe[j][10] = 0.3;
							YAPe[j][9] = 0.2;
//							YAPe[j][8] = 0.016;
							YAPe[j][8] = 0.02;
//							YAPe[j][22] = 0.009;
							YAPe[j][22] = 0.01;
//							YAPe[j][21] = 0.003;
							YAPe[j][21] = 0.02;
//							YAPe[j][20] = 0.003;
							YAPe[j][20] = 0.02;
							break;
						case "FS609":
//							YAPe[j][0] = 0.006;
//							YAPe[j][2] = 0.393;
							YAPe[j][2] = 0.4;
//							YAPe[j][3] = 0.386;
							YAPe[j][3] = 0.4;
//							YAPe[j][11] = 0.165;
							YAPe[j][11] = 0.2;
//							YAPe[j][10] = 0.044;
							YAPe[j][10] = 0.05;
//							YAPe[j][9] = 0.003;
							YAPe[j][9] = 0.02;
//							YAPe[j][13] = 0.002;
							break;
						case "FS620":
							YAPe[j][0] = 0.021;
							YAPe[j][2] = 0.146;
							YAPe[j][3] = 0.021;
							YAPe[j][11] = 0.188;
							YAPe[j][10] = 0.292;
							YAPe[j][9] = 0.229;
							YAPe[j][8] = 0.083;
							YAPe[j][21] = 0.021;
							break;
						case "FS622":
							YAPe[j][0] = 0.009;
							YAPe[j][11] = 0.107;
							YAPe[j][10] = 0.369;
							YAPe[j][9] = 0.369;
							YAPe[j][8] = 0.129;
							YAPe[j][7] = 0.017;
							break;
						case "FS623":
							YAPe[j][0] = 0.021;
							YAPe[j][2] = 0.213;
							YAPe[j][3] = 0.038;
							YAPe[j][11] = 0.339;
							YAPe[j][10] = 0.289;
							YAPe[j][9] = 0.100;
							break;
						case "FS629":
							YAPe[j][0] = 0.001;
							YAPe[j][2] = 0.610;
							YAPe[j][3] = 0.181;
							YAPe[j][11] = 0.165;
							YAPe[j][10] = 0.042;
							YAPe[j][13] = 0.001;
							break;
						case "FS630":
							YAPe[j][2] = 0.148;
							YAPe[j][3] = 0.074;
							YAPe[j][11] = 0.148;
							YAPe[j][10] = 0.074;
							YAPe[j][9] = 0.148;
							YAPe[j][8] = 0.037;
							YAPe[j][7] = 0.037;
							YAPe[j][22] = 0.148;
							YAPe[j][21] = 0.148;
							YAPe[j][20] = 0.037;
							break;
						case "FS631":
							YAPe[j][11] = 0.495;
							YAPe[j][10] = 0.8;
//							YAPe[j][10] = 0.140;
							YAPe[j][9] = 0.242;
//							YAPe[j][9] = 0.242;
							YAPe[j][8] = 0.075;
							YAPe[j][21] = 0.047;
							break;
						case "FS632":
//							YAPe[j][0] = 0.005;
//							YAPe[j][2] = 0.271;
							YAPe[j][2] = 0.5;
//							YAPe[j][3] = 0.002;
//							YAPe[j][11] = 0.313;
							YAPe[j][11] = 0.4;
//							YAPe[j][10] = 0.15;
							YAPe[j][10] = 0.2;
//							YAPe[j][9] = 0.098;
							YAPe[j][9] = 0.1;
//							YAPe[j][8] = 0.042;
							YAPe[j][8] = 0.05;
//							YAPe[j][7] = 0.013;
//							YAPe[j][12] = 0.001;
//							YAPe[j][18] = 0.001;
//							YAPe[j][22] = 0.008;
							YAPe[j][22] = 0.05;
//							YAPe[j][21] = 0.05;
							YAPe[j][21] = 0.05;
//							YAPe[j][20] = 0.023;
//							YAPe[j][19] = 0.023;
							break;
						case "FS633":
							YAPe[j][0] = 0.005;
							YAPe[j][2] = 0.408;
							YAPe[j][3] = 0.309;
							YAPe[j][11] = 0.104;
							YAPe[j][10] = 0.043;
							YAPe[j][9] = 0.021;
							YAPe[j][8] = 0.003;
							YAPe[j][12] = 0.002;
							YAPe[j][13] = 0.016;
							YAPe[j][22] = 0.059;
							YAPe[j][21] = 0.022;
							YAPe[j][20] = 0.008;
							break;
						case "FS639":
							YAPe[j][0] = 0.003;
							YAPe[j][2] = 0.118;
							YAPe[j][3] = 0.781;
							YAPe[j][11] = 0.056;
							YAPe[j][10] = 0.038;
							YAPe[j][9] = 0.003;
							break;
						case "FS660":
							YAPe[j][0] = 0.021;
							YAPe[j][2] = 0.083;
							YAPe[j][3] = 0.021;
							YAPe[j][11] = 0.208;
							YAPe[j][10] = 0.104;
							YAPe[j][9] = 0.167;
							YAPe[j][8] = 0.188;
							YAPe[j][7] = 0.021;
							YAPe[j][20] = 0.104;
							YAPe[j][19] = 0.083;
							break;
						case "FS661":
							YAPe[j][0] = 0.01;
							YAPe[j][11] = 0.74;
							YAPe[j][10] = 0.18;
							YAPe[j][9] = 0.05;
							YAPe[j][21] = 0.02;
							break;
						case "FS662":
							YAPe[j][0] = 0.002;
							YAPe[j][2] = 0.120;
							YAPe[j][11] = 0.288;
							YAPe[j][10] = 0.310;
							YAPe[j][9] = 0.198;
							YAPe[j][8] = 0.063;
							YAPe[j][7] = 0.011;
							YAPe[j][3] = 0.003;
							YAPe[j][20] = 0.005;
							break;
						case "FS663":
							YAPe[j][0] = 0.004;
							YAPe[j][2] = 0.283;
							YAPe[j][11] = 0.297;
							YAPe[j][10] = 0.145;
							YAPe[j][9] = 0.052;
							YAPe[j][3] = 0.208;
							YAPe[j][20] = 0.011;
							break;
						case "FS669":
							YAPe[j][11] = 0.33;
							YAPe[j][10] = 0.057;
							YAPe[j][9] = 0.012;
							YAPe[j][3] = 0.598;
							YAPe[j][14] = 0.003;
							break;
						case "FS670":
							YAPe[j][11] = 0.08;
							YAPe[j][10] = 0.08;
							YAPe[j][9] = 0.16;
							YAPe[j][8] = 0.5;
							YAPe[j][20] = 0.08;
							YAPe[j][19] = 0.08;
							YAPe[j][18] = 0.08;
							break;
						case "FS671":
							YAPe[j][2] = 0.098;
//							YAPe[j][2] = 0.8;
//							YAPe[j][11] = 0.8;
							YAPe[j][11] = 0.314;
//							YAPe[j][10] = 0.8;
							YAPe[j][10] = 0.373;
							YAPe[j][9] = 0.186;
//							YAPe[j][9] = 0.8;
							YAPe[j][8] = 0.3;
							break;
						case "FS672":
							YAPe[j][2] = 0.3;
							YAPe[j][11] = 0.4;
							YAPe[j][10] = 0.3;
							YAPe[j][9] = 0.15;
							YAPe[j][8] = 0.040;
							YAPe[j][20] = 0.02;
//							YAPe[j][19] = 0.01;
							YAPe[j][19] = 0.08;
							break;
						case "FS673":
							YAPe[j][0] = 0.016;
							YAPe[j][2] = 0.301;
							YAPe[j][3] = 0.497;
							YAPe[j][11] = 0.130;
							YAPe[j][10] = 0.041;
//							YAPe[j][9] = 0.014;
							YAPe[j][9] = 0.03;
//							YAPe[j][22] = 0.002;
							YAPe[j][22] = 0.03;
							break;
						case "FS679":
							YAPe[j][0] = 0.003;
							YAPe[j][2] = 0.065;
							YAPe[j][3] = 0.922;
							YAPe[j][11] = 0.011;
							break;
						case "FS680":
							YAPe[j][0] = 0.015;
							YAPe[j][2] = 0.241;
							YAPe[j][3] = 0.060;
							YAPe[j][11] = 0.173;
							YAPe[j][10] = 0.165;
							YAPe[j][9] = 0.120;
							YAPe[j][8] = 0.053;
							YAPe[j][12] = 0.006;
							YAPe[j][13] = 0.006;
							YAPe[j][22] = 0.030;
							YAPe[j][21] = 0.045;
							YAPe[j][20] = 0.075;
							YAPe[j][19] = 0.006;
							break;
						case "FS681":
							YAPe[j][10] = 0.05;
							YAPe[j][9] = 0.71;
							YAPe[j][8] = 0.20;
							YAPe[j][7] = 0.03;
							break;
						case "FS682":
//							YAPe[j][0] = 0.01;
							YAPe[j][0] = 0.08;
							YAPe[j][11] = 0.22;
							YAPe[j][10] = 0.26;
							YAPe[j][9] = 0.30;
							YAPe[j][8] = 0.14;
							YAPe[j][20] = 0.03;
							YAPe[j][19] = 0.02;
							break;
						case "FS683":
//							YAPe[j][0] = 0.01;
							YAPe[j][0] = 0.04;
							YAPe[j][2] = 0.42;
							YAPe[j][11] = 0.3;
							YAPe[j][10] = 0.18;
							YAPe[j][9] = 0.05;
//							YAPe[j][20] = 0.01;
							YAPe[j][20] = 0.04;
							break;
						case "FS689":
							YAPe[j][2] = 0.32;
							YAPe[j][3] = 0.59;
//							YAPe[j][11] = 0.07;
//							YAPe[j][10] = 0.03;
//							YAPe[j][11] = 0.07;
							YAPe[j][11] = 0.1;
							YAPe[j][10] = 0.05;
							break;
						case "FS690":
							YAPe[j][10] = 0.385;
							YAPe[j][9] = 0.538;
							YAPe[j][8] = 0.077;
							break;
						case "FS691":
							YAPe[j][9] = 0.67;
							YAPe[j][8] = 0.33;
							break;
						case "FS692":
							YAPe[j][10] = 0.646;
							YAPe[j][9] = 0.246;
							YAPe[j][8] = 0.108;
							break;
						case "FS693":
							YAPe[j][0] = 0.056;
							YAPe[j][11] = 0.666;
							YAPe[j][10] = 0.222;
							YAPe[j][9] = 0.056;
							break;
						case "FS700":
							YAPe[j][9] = 1;
							break;
						case "FS702":
							YAPe[j][0] = 0.05;
							YAPe[j][10] = 0.2;
							YAPe[j][9] = 0.75;
							break;
						case "FS703":
							YAPe[j][10] = 1;
							break;
						case "FS710":
							YAPe[j][10] = 0.1;
							YAPe[j][9] = 0.3;
							YAPe[j][8] = 0.5;
							YAPe[j][21] = 0.1;
							break;
						case "FS712":
							YAPe[j][11] = 0.030;
							YAPe[j][10] = 0.515;
							YAPe[j][9] = 0.212;
							YAPe[j][8] = 0.242;
							break;
						case "FS713":
							YAPe[j][0] = 0.011;
							YAPe[j][11] = 0.276;
							YAPe[j][10] = 0.253;
							YAPe[j][9] = 0.368;
							YAPe[j][8] = 0.092;
							break;
						case "FS719":
							YAPe[j][0] = 0.15;
							YAPe[j][11] = 0.328;
							YAPe[j][10] = 0.436;
							YAPe[j][9] = 0.221;
							break;
						case "FS720":
							YAPe[j][2] = 0.109;
							YAPe[j][11] = 0.261;
							YAPe[j][10] = 0.174;
							YAPe[j][9] = 0.217;
							YAPe[j][8] = 0.022;
							YAPe[j][13] = 0.022;
							YAPe[j][21] = 0.065;
							YAPe[j][20] = 0.130;
							break;
						case "FS722":
							YAPe[j][0] = 0.014;
							YAPe[j][2] = 0.201;
							YAPe[j][3] = 0.005;
							YAPe[j][11] = 0.322;
							YAPe[j][10] = 0.182;
							YAPe[j][9] = 0.192;
							YAPe[j][8] = 0.075;
							YAPe[j][20] = 0.005;
							YAPe[j][19] = 0.005;
							break;
						case "FS723":
							YAPe[j][0] = 0.002;
							YAPe[j][2] = 0.771;
							YAPe[j][11] = 0.109;
							YAPe[j][10] = 0.072;
							YAPe[j][9] = 0.020;
							YAPe[j][23] = 0.007;
							YAPe[j][13] = 0.005;
							YAPe[j][21] = 0.01;
							YAPe[j][20] = 0.002;
							break;
						case "FS729":
							YAPe[j][2] = 0.583;
							YAPe[j][3] = 0.197;
							YAPe[j][11] = 0.157;
							YAPe[j][10] = 0.037;
							YAPe[j][22] = 0.026;
							break;
						case "FS730":
							YAPe[j][0] = 0.067;
							YAPe[j][11] = 0.2;
							YAPe[j][10] = 0.333;
							YAPe[j][9] = 0.2;
							YAPe[j][8] = 0.133;
							YAPe[j][7] = 0.067;
							break;
						case "FS732":
							YAPe[j][11] = 0.206;
							YAPe[j][10] = 0.176;
							YAPe[j][9] = 0.206;
							YAPe[j][8] = 0.268;
							YAPe[j][7] = 0.147;
							break;
						case "FS733":
							YAPe[j][11] = 0.798;
							YAPe[j][10] = 0.107;
							YAPe[j][9] = 0.083;
							YAPe[j][8] = 0.012;
							break;
						case "FS739":
							YAPe[j][11] = 0.220;
							YAPe[j][10] = 0.049;
							YAPe[j][2] = 0.731;
							break;
						case "FS750":
							YAPe[j][2] = 0.1;
							YAPe[j][11] = 0.2;
							YAPe[j][9] = 0.1;
							YAPe[j][8] = 0.1;
							YAPe[j][7] = 0.4;
							YAPe[j][20] = 0.1;
							break;
						case "FS752":
							YAPe[j][10] = 0.074;
							YAPe[j][9] = 0.037;
							YAPe[j][8] = 0.444;
							YAPe[j][7] = 0.426;
							YAPe[j][6] = 0.019;
							break;
						case "FS753":
							YAPe[j][2] = 0.354;
							YAPe[j][11] = 0.146;
							YAPe[j][10] = 0.042;
							YAPe[j][9] = 0.104;
							YAPe[j][8] = 0.271;
							YAPe[j][7] = 0.063;
							YAPe[j][20] = 0.021;
							break;
						case "FS759":
							YAPe[j][2] = 0.379;
							YAPe[j][3] = 0.474;
							YAPe[j][11] = 0.086;
							YAPe[j][10] = 0.034;
							YAPe[j][9] = 0.026;
							break;
						case "FS770":
							YAPe[j][11] = 0.192;
							YAPe[j][10] = 0.192;
							YAPe[j][9] = 0.365;
							YAPe[j][8] = 0.096;
							YAPe[j][22] = 0.039;
							YAPe[j][21] = 0.039;
							YAPe[j][20] = 0.019;
							YAPe[j][19] = 0.058;
							break;
						case "FS771":
							YAPe[j][10] = 0.076;
							YAPe[j][9] = 0.578;
							YAPe[j][8] = 0.346;
							break;
						case "FS772":
							YAPe[j][11] = 0.101;
							YAPe[j][10] = 0.348;
							YAPe[j][9] = 0.5;
							YAPe[j][8] = 0.179;
//							YAPe[j][7] = 0.014;
							YAPe[j][7] = 0.1;
							YAPe[j][20] = 0.1;
							break;
						case "FS773":
							YAPe[j][11] = 0.5;
							YAPe[j][10] = 0.4;
							YAPe[j][9] = 0.2;
							YAPe[j][8] = 0.1;
							YAPe[j][22] = 0.1;
							YAPe[j][21] = 0.1;
							break;
						case "FS779":
							YAPe[j][0] = 0.1;
							YAPe[j][2] = 0.2;
							YAPe[j][11] = 0.6;
							YAPe[j][10] = 0.4;
							YAPe[j][9] = 0.3;
							break;
						case "FS830":
							YAPe[j][11] = 0.182;
							YAPe[j][10] = 0.273;
							YAPe[j][9] = 0.045;
							YAPe[j][8] = 0.091;
							YAPe[j][7] = 0.319;
							YAPe[j][22] = 0.045;
							YAPe[j][21] = 0.045;
							break;
						case "FS831":
							YAPe[j][8] = 0.83;
							YAPe[j][7] = 0.17;
							break;
						case "FS832":
							YAPe[j][10] = 0.188;
							YAPe[j][9] = 0.413;
							YAPe[j][8] = 0.15;
							YAPe[j][7] = 0.225;
							YAPe[j][6] = 0.2;
							break;
						case "FS833":
							YAPe[j][0] = 0.2;
							YAPe[j][11] = 0.2;
							YAPe[j][10] = 0.512;
							YAPe[j][9] = 0.317;
							YAPe[j][8] = 0.2;
							YAPe[j][7] = 0.2;
							break;
						case "FS839":
							YAPe[j][11] = 0.246;
							YAPe[j][10] = 0.508;
							YAPe[j][9] = 0.246;
							break;
						case "FS880":
							YAPe[j][9] = 0.75;
							YAPe[j][8] = 0.25;
							break;
						case "FS881":
							YAPe[j][9] = 0.8;
							YAPe[j][8] = 0.2;
							break;
						case "FS882":
							YAPe[j][2] = 0.235;
							YAPe[j][11] = 0.765;
							break;
						case "FS883":
							YAPe[j][2] = 1;
							break;
						case "FS890":
							YAPe[j][11] = 0.5;
							YAPe[j][2] = 0.5;
							break;
						case "FS892":
							YAPe[j][2] = 1;
							break;
						case "FS893":
							YAPe[j][3] = 1;
							break;
						case "FS899":
							YAPe[j][2] = 0.07;
							YAPe[j][3] = 0.93;
							break;
						case "FT660":
							YAPe[j][2] = 0.25;
							YAPe[j][9] = 0.75;
							break;
						case "FT661":
							YAPe[j][10] = 0.893;
							YAPe[j][9] = 0.107;
							break;
						case "FT662":
							YAPe[j][2] = 0.043;
							YAPe[j][11] = 0.87;
							YAPe[j][9] = 0.087;
							break;
						case "FT663":
							YAPe[j][2] = 0.88;
							YAPe[j][11] = 0.04;
							YAPe[j][10] = 0.08;
							break;
						case "FT669":
							YAPe[j][2] = 1;
							break;
						default:
							// YAPe[j][11]=1;
							break;
						}
					}
				}
				scenario[i].setYA2(YAPe2);
				scenario[i].setYA(YAPe);
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
								k = (int) betweenDate;
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
							for (int a = 0; a < Common.A; a++) {
								if (scenario[i].getPrice()[m][j][k][a] == 0) {
									scenario[i].setPrice(m, j, k, a, pr);
								}
							}
						}
					}
					x++;
				}
				for (int m = 0; m < Common.M; m++) {
					for (int j = 0; j < Common.J; j++) {
						for (int k = 0; k < Common.K; k++) {
							for (int a = 0; a < Common.A; a++) {
								if (scenario[i].getDens()[m][j][k][a] == 0) {
									scenario[i].setDens(m, j, k, a, 10);
								}

							}
						}
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
	}
}
