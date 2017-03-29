package com.crop;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class LoadData {
	public static String fileToBeRead="/Users/mike/Dropbox/成大資料/研究/Agri-data/10307-10406.xls";
	public static void main(String[] args){
		try {
			HSSFWorkbook workbook=new HSSFWorkbook(new FileInputStream(fileToBeRead));
			HSSFSheet sheet=workbook.getSheet("Sheet1");
			
			int price[][][][]=new int[Common.M][Common.J][Common.K][Common.A];
			int dens[][][][]=new int[Common.M][Common.J][Common.K][Common.A];
			int s[][][][]=new int[Common.M][Common.J][Common.K][Common.A];
			boolean arrival[][]=new boolean[Common.J][Common.K];
			
			double YA[][]=new double[Common.J][Common.A];
			double YAPe[][]=new double[Common.J][Common.A];
			String date1="2014/07/01";
			SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
			Date beginDate=df.parse(date1);
			
			
			for(int i=1;i<sheet.getPhysicalNumberOfRows()-1;i++){
				HSSFRow row = sheet.getRow(i);
				//日期
				HSSFCell dateCell = row.getCell((short)12);
				Date endDate=df.parse(dateCell.getStringCellValue());
				long betweenDate=((endDate.getTime()-beginDate.getTime())/(1000*60*60*24));
				int k=(int)betweenDate;
				//品種
				HSSFCell jCell = row.getCell((short)3);
				int j=Common.searchJ(jCell.getStringCellValue());
				//市場
				HSSFCell mCell = row.getCell((short)1);
				int m=Common.searchM(mCell.getStringCellValue());
				
				HSSFCell aCell = row.getCell((short)6);
				int a=Common.searchA(aCell.getStringCellValue());
				
				HSSFCell pCell = row.getCell((short)9);
				int pr=(int)pCell.getNumericCellValue();
				if(price[m][j][k][a]==0){
					price[m][j][k][a]=pr;
				}else{
					price[m][j][k][a]=(pr+price[m][j][k][a])/2;
				}
				
				HSSFCell supplyCell = row.getCell((short)8);
				HSSFCell boxCell = row.getCell((short)7);
				int supply=(int)supplyCell.getNumericCellValue();
				int boxNumber=(int)boxCell.getNumericCellValue();
				int avageDens=supply/boxNumber;
				if(dens[m][j][k][a]==0 ){
					dens[m][j][k][a]=avageDens;
				}else{
					dens[m][j][k][a]=(avageDens+dens[m][j][k][a])/2;
				}
				s[m][j][k][a]+=supply;
				
				for(int y=(k-Common.d[j]);y<Common.K;y++){
					arrival[j][y]=true;
				}
				
				YA[j][a]+=supply;
				//System.out.println("日期： " + cell.getStringCellValue());
				//System.out.println("差： " +((endDate.getTime()-beginDate.getTime())/(1000*60*60*24)));
			}
			for(int j=0;j<Common.J;j++){
				double totalJ=0;
				for(int a=0;a<Common.A;a++){
					totalJ+=YA[j][a];
				}
				for(int a=0;a<Common.A;a++){
					YAPe[j][a]=YA[j][a]/totalJ;
				}
			}
			
//			ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
//			ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
//			ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
//			ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
//			ps.add(price);
//			ss.add(s);
//			DT.add(dens);
//			AT.add(arrival);
//			CropMasterProblem master=new CropMasterProblem(ps,ss,DT,AT,YAPe);
			//System.out.println("左上端單元是： " + cell.getStringCellValue());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
