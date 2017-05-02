package com.crop;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.Random;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.model.Scenario;

public class LoadData {
	//檔案路徑與名稱
	public static String FILENAMES[]={"10507-10606.xls","10407-10506.xls","10307-10406.xls","10207-10306.xls","10107-10206.xls",
			"10007-10106.xls","09907-10006.xls","09807-09906.xls","09707-09806.xls","09607-09706.xls"};
	public static String FILEPATH="/home/w87754/Desktop/Agri-data/";
	public static String BEGINDATE[]={"2016/07/01","2015/07/01","2014/07/01","2013/07/01","2012/07/01","2011/07/01",
			"2010/07/01","2009/07/01","2008/07/01","2007/07/01"};
	public static String SHEETNAME="Sheet1";
	//public static String fileToBeRead="/Users/mike/Dropbox/成大資料/研究/Agri-data/10407-10506.xls";
	
	public Scenario[] scenario;
	
	public LoadData(){
		scenario=new Scenario[FILENAMES.length];		
	}
	public void start(){

		System.out.println("---------Loading data ..... ---------");
		try {
			
			for(int i=0;i<FILENAMES.length;i++){
				
				scenario[i]=new Scenario();
				String fileName=FILEPATH+FILENAMES[i];
				System.out.println("Load file name : " + FILENAMES[i] );
				HSSFWorkbook workbook=new HSSFWorkbook(new FileInputStream(fileName));
				HSSFSheet sheet=workbook.getSheet(SHEETNAME);
				SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
				Date beginDate=df.parse(BEGINDATE[i]);
				double YA[][]=new double[Common.J][Common.A];
				double YAPe[][]=new double[Common.J][Common.A];
				for(int x=1;x<sheet.getPhysicalNumberOfRows()-1;x++){
					
					HSSFRow row = sheet.getRow(x);
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
					//品質
					HSSFCell aCell = row.getCell((short)6);
					int a=Common.searchA(aCell.getStringCellValue());
					//價格
					HSSFCell pCell = row.getCell((short)9);
					int pr=(int)pCell.getNumericCellValue();
					
					if(scenario[i].getPrice()[m][j][k][a]==0){
						scenario[i].setPrice(m,j,k,a,pr);
					}else{
						int prr=(pr+scenario[i].getPrice()[m][j][k][a])/2;
						scenario[i].setPrice(m, j, k, a, prr);
					}
					HSSFCell supplyCell = row.getCell((short)8);
					HSSFCell boxCell = row.getCell((short)7);
					int supply=(int)supplyCell.getNumericCellValue();
					int boxNumber=(int)boxCell.getNumericCellValue();
					int avageDens=supply/boxNumber;
					if(scenario[i].getDens()[m][j][k][a]==0 ){
						scenario[i].setDens(m, j, k,a, avageDens);
					}else{
						scenario[i].setDens(m, j, k,a, (scenario[i].getDens()[m][j][k][a]+avageDens)/2);
					}
					
					scenario[i].setSupply(m, j, k, a, (scenario[i].getSupply()[m][j][k][a]+supply));
					
					YA[j][a]+=supply;
//					for(int kk=0;kk<Common.K;kk++){
//						scenario[i].setArrival(j, kk, true);
//					}
				}
				//品質比率
//				for(int j=0;j<Common.J;j++){
//					double totalJ=0;
//					for(int a=0;a<Common.A;a++){
//						totalJ+=YA[j][a];
//					}
//					for(int a=0;a<Common.A;a++){
//						//YAPe[j][a]=YA[j][a]/totalJ;
//						if(YA[j][a]==0){
//							YAPe[j][a]=0;
//						}else{
//							YAPe[j][a]=0.8;
//						}
//					}
//					for(int a=0;a<Common.A;a++){
//						switch(a){
//						case 2:
//							YAPe[j][a]=0.8;
//							break;
//						case 7:
//							YAPe[j][a]=1;
//							break;
//						case 8:
//							YAPe[j][a]=1;
//							break;
//						case 9:
//							YAPe[j][a]=1;
//							break;
//						case 10:
//							YAPe[j][a]=1;
//							break;
//						case 11:
//							YAPe[j][a]=1;
//							break;
//						default:
//							YAPe[j][a]=0.5;
//						}	
//					}
//					for(int a=0;a<Common.A;a++){
//						//YAPe[j][a]=YA[j][a]/totalJ;
//						if(YA[j][a]!=0){
//							YAPe[j][a]=0.7;
//						}
//					}
//				}
//				scenario[i].setYA(YAPe);
				//到達日設定
				for(int j=0;j<Common.J;j++){
					for(int k=30;k<Common.K;k++){
						scenario[i].setArrival(j, k, true);
					}
				}
//				Random r=new Random();
				//箱數
//				for(int m=0;m<Common.M;m++){
//					for(int j=0;j<Common.J;j++){
//						for(int k=0;k<Common.K;k++){
//							for(int a=0;a<Common.A;a++){
//								if(scenario[i].getDens()[m][j][k][a]==0){
//									scenario[i].setDens(m,j,k,a,10);
//								}
////								if(scenario[i].getPrice()[m][j][k][a]==0){
////									scenario[i].setPrice(m, j, k, a, r.nextInt(130)+50);
////								}
//							}
//						}
//					}
//				}
			}

			System.out.println("---------Finish load data---------");
//			ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
//			ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
//			ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
//			ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
//			ps.add(price);
//			ss.add(s);
//			DT.add(dens);
//			AT.add(arrival);
//			CropMasterProblem master=new CropMasterProblem(ps,ss,DT,AT,YAPe);
//
//			
//			SubProblem sp=new SubProblem(master.getV(),master.getH(),master.getQ(),YAPe,ps,dens);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
}
