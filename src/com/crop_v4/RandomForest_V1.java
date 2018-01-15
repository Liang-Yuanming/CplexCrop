package com.crop_v4;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class RandomForest_V1 {
	private final int treeNumber=100;
	private final int MONTH=-1;
	private RandomForest randomForest;
	private final int priceClassIndex=11;
	private final int demandClassIndex=3;
	public RandomForest_V1(){
		
	}
	public void build(int month,int classIndex){
		InstanceQuery query;
		try {
			query = new InstanceQuery();
			query.setUsername("mike");
			query.setPassword("mike");
			query.setQuery(makeSQL(month));
			if(!query.isConnected()){
				query.connectToDatabase();
			}
			Instances trainData = query.retrieveInstances();
			
			trainData.setClassIndex(classIndex);
			query.close();
			query.disconnectFromDatabase();
			randomForest = new RandomForest();
			randomForest.setNumIterations(treeNumber);
			
			randomForest.buildClassifier(trainData);
//			for(int i=0;i<trainData.numAttributes();i++){
//				System.out.println(trainData.attribute(i));
//			}
			
			//衡量工具
//			Evaluation eTest = new Evaluation(trainData);
//			eTest.evaluateModel(randomForest, trainData);
//			// Print the result à la Weka explorer:
//			String strSummary = eTest.toSummaryString();
//			System.out.println(strSummary);
//			randomForest.classifyInstance(arg0)
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("unchecked")
	public double[][] classfyInstance(String m,String v,String g,int numOfPie,int salesWeek,double mat,double rain,int pdn,
			double sh,int fv,int ttv,double up,int classIndex) throws Exception{
		
//		
		FastVector marketNominalVal = new FastVector(Common.M);
		for(int i=0;i<Common.M;i++){
			marketNominalVal.addElement(Common.MARKET[i]);
		}
		Attribute market = new Attribute("MARKET",marketNominalVal);

		FastVector varietyNominalVal = new FastVector(Common.J);
		for(int i=0;i<Common.J;i++){
			varietyNominalVal.addElement(Common.JSTR[i]);
		}
		Attribute variety = new Attribute("VARIETY",varietyNominalVal);
		
		
		FastVector gradeNominalVal = new FastVector(21);
		for(int i=0;i<Common.A;i++){
			gradeNominalVal.addElement(Common.ASTR[i]);
		}
		Attribute grade = new Attribute("GRADE",gradeNominalVal);
		
		
		Attribute number_of_pieces = new Attribute("NUMBER_OF_PIECES");
		Attribute sales_week = new Attribute("SALES_WEEK");
		
		Attribute monthly_average_temperature = new Attribute("MONTH_AVG_TEMP");
		Attribute rainfall = new Attribute("RAINFALL");
		Attribute precipitation_days = new Attribute("PRE_DAY");
		Attribute sunshine_hours = new Attribute("SUN_HOURS");
		Attribute festival_value = new Attribute("FESTIVAL_VALUE");
		Attribute total_trading_volume = new Attribute("TOTAL_TRADING_VOLUME");
		Attribute unit_price = new Attribute("UNIT_PRICE");
		
		FastVector allWekaAttributes=new FastVector(12);
		allWekaAttributes.addElement(market);
		allWekaAttributes.addElement(variety);
		allWekaAttributes.addElement(grade);
		allWekaAttributes.addElement(number_of_pieces);
		allWekaAttributes.addElement(sales_week);
		allWekaAttributes.addElement(monthly_average_temperature);
		allWekaAttributes.addElement(rainfall);
		allWekaAttributes.addElement(precipitation_days);
		allWekaAttributes.addElement(sunshine_hours);
		allWekaAttributes.addElement(festival_value);
		allWekaAttributes.addElement(total_trading_volume);
		allWekaAttributes.addElement(unit_price);
		Instances isTrainingSet = new Instances("Rel", allWekaAttributes, 10);
		isTrainingSet.setClassIndex(classIndex);
		
		Instance test=new DenseInstance(12);
		test.setValue(market, m);
		test.setValue(variety, v);
		test.setValue(grade, g);
		test.setValue(number_of_pieces, numOfPie);
		test.setValue(sales_week, salesWeek);
		test.setValue(monthly_average_temperature, mat);
		test.setValue(rainfall, rain);
		test.setValue(precipitation_days, pdn);
		test.setValue(sunshine_hours,sh);
		test.setValue(festival_value, fv);
		test.setValue(total_trading_volume, ttv);
		test.setValue(unit_price, up);
		isTrainingSet.add(test);

		
		double result[][]=randomForest.distributionsForInstances(isTrainingSet);

		return result;
	}
	public String makeSQL(int month){
		String sql1="SELECT s.MARKET,s.VARIETY,s.GRADE,s.NUMBER_OF_PIECES,s.SALES_WEEK ,"
				+ "w.MONTH_AVG_TEMP,w.RAINFALL,w.PRE_DAY,w.SUN_HOURS,NVL(f.FESTIVAL_VALUE,0) AS FESTIVAL_VALUE,"
				+ "s.TOTAL_TRADING_VOLUME,s.UNIT_PRICE FROM ("
				+ "SELECT TO_DATE(TO_CHAR(SALES_DATE,'yyyymm'),'yyyymm') as YEAR,MARKET,VARIETY,GRADE,"
				+ "SUM(NUMBER_OF_PIECES) as NUMBER_OF_PIECES,SUM(TOTAL_TRADING_VOLUME) as TOTAL_TRADING_VOLUME,"
				+ "Round(AVG(UNIT_PRICE),0) as UNIT_PRICE, "
				+ "SUM(TOTAL_PRICE) as TOTAL_PRICE,SUM(AMOUNT_OF_GOODS) as AMOUNT_OF_GOODS,SALES_WEEK FROM SALES "
				+ "GROUP BY MARKET,VARIETY,GRADE,SALES_WEEK,TO_CHAR(SALES_DATE,'yyyymm') ) s "
				+ "LEFT JOIN WEATHER w ON TO_CHAR(ADD_MONTHS(s.YEAR,%s),'MM')=w.MONTH "
				+ "AND TO_CHAR(ADD_MONTHS(s.YEAR,%s),'yyyy')=w.YEAR AND w.CITY='台中市場' "
				+ "LEFT JOIN (select TO_DATE(TO_CHAR(FESTIVAL_DATE,'yyyymm'),'yyyymm') as year,FESTIVAL_WEEK,max(FESTIVAL_VALUE) as FESTIVAL_VALUE from FESTIVAL "
				+ "GROUP BY TO_CHAR(FESTIVAL_DATE,'yyyymm'),FESTIVAL_WEEK) f ON s.SALES_WEEK=f.FESTIVAL_WEEK and  s.year=f.year "
				+ "WHERE s.year>=TO_DATE('2009/07/01','YYYY/MM/DD')  "
				+ "ORDER BY s.year asc";
		sql1=String.format(sql1, month,month);
		return sql1;
	}
	public double predit(String market[],String v,String g,int numOfPie[],int salesWeek,double mat[],double rain[],int pdn[],
			double sh[],int fv,int ttv[],double up[],int dens) throws Exception{
		//預測價格
		build(MONTH,priceClassIndex);
		
		double[] price=new double[market.length];
		double[] d=new double[market.length];
		for(int i=0;i<market.length;i++){
			double result[][]=classfyInstance(market[i], v, g, numOfPie[i],salesWeek , mat[i], rain[i], pdn[i], sh[i], fv, ttv[i],up[i],priceClassIndex);
			price[i]=result[0][0];
//			System.out.println(market[i]+"old vs new price = "+up[i]+","+price[i]);
		}
		double totalOldD=0;
		double totalD=0;
		double totalVolum=0;
		build(MONTH,demandClassIndex);
		for(int i=0;i<market.length;i++){
			double result[][]=classfyInstance(market[i], v, g, numOfPie[i],salesWeek , mat[i], rain[i], pdn[i], sh[i], fv, ttv[i],price[i],demandClassIndex);
			d[i]=Math.round(result[0][0]);
			totalOldD+=numOfPie[i];
			totalVolum+=ttv[i];
			totalD+=d[i];
//			System.out.println(market[i]+"old vs new D = "+numOfPie[i]+","+d[i]);
		}
//		System.out.println("total old vs new d= "+totalOldD+" , " +totalD);
		
		double oldProfit=0;
		double profit=0;
		int indexP=0;
		double maxPrice=price[0];
		int index[]=getSortList(price);
		double box=Math.ceil(totalVolum/dens);
//		System.out.println("box= "+box);
		for(int i=0;i<market.length;i++){
//			if(up[i]==0){
//				up[i]=50;
//			}
			oldProfit+=up[i]*ttv[i];
			if(box>=d[index[i]] && box>0){
				profit+=d[index[i]]*dens*up[index[i]];
				box=box-d[index[i]];
				d[index[i]]=0;
			}else if(box <d[index[i]] && box>0 && d[index[i]]>0){
				profit+=box*dens*up[index[i]];
				d[index[i]]=d[index[i]]-box;
				box=0;
			}else if(box==0){
				break;
			}
		}
		if(box!=0){
			profit+=box*dens*up[0];
//			System.out.println("box=" + box );
		}
			
//		System.out.println("Old Profit vs New Profit = "+oldProfit +" , " +profit);

		return profit;
	}
	private int[] getSortList(double price[]){
		int indexPrice[]=new int[price.length];
		for(int i=0;i<indexPrice.length;i++){
			indexPrice[i]=i;
			
		}
		for(int i=0;i<indexPrice.length-1;i++){


			for(int j=i+1;j<indexPrice.length;j++){
				if(price[i]<price[j]){
					int tempIndex=indexPrice[i];
					indexPrice[i]=indexPrice[j];
					indexPrice[j]=tempIndex;
					double tempPrice=price[i];
					price[i]=price[j];
					price[j]=tempPrice;
				}
			}

		}
		return indexPrice;
	}
	public static void main(String [] args){
		RandomForest_V1 rf=new RandomForest_V1();
		String market[]={"台北市場","台中市場","彰化市場","台南市場","高雄市場"};
		String v="FS631";
		String g="A9";
		int numOfPie[]={8,1,4,2,3};
		int salesWeek=39;
		double mat[]={18.2,20.2,20.2,21.5,23.5};
		double rain[]={182.7,38,38,12.8,3.5};
		int pdn[]={18,9,9,3,3};
		double sh[]={60.8,146.8,146.8,194.7,200.5};
//		double mat[]={20.2,20.2,20.2,20.2,20.2};
//		double rain[]={38,38,38,38,38};
//		int pdn[]={9,9,9,9,9};
//		double sh[]={146.8,146.8,146.8,146.8,146.8};
		int fv=0;
		int ttv[]={158,20,80,40,60};
		double up[]={95,71,73,81,75};
		int dnen=20;
		try {
			//
			rf.predit(market, v, g, numOfPie, salesWeek, mat, rain, pdn, sh, fv, ttv, up,dnen);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		double p[]={3.0,1.0,2.0,0.0,6.0};
//		rf.getSortList(p);
	}
}	
