package com.crop.v2;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class RandomForest_V1 {
	private final int treeNumber=30;
	private RandomForest randomForest;
	public RandomForest_V1(){
		
	}
	public void build(){
		InstanceQuery query;
		try {
			query = new InstanceQuery();
			query.setUsername("mike");
			query.setPassword("mike");
			query.setQuery(makeSQL());
			
			Instances trainData = query.retrieveInstances();
			trainData.setClassIndex(trainData.numAttributes()-1);
			
			randomForest = new RandomForest();
			randomForest.setNumIterations(treeNumber);
			
			randomForest.buildClassifier(trainData);
//			randomForest.classifyInstance(arg0)
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@SuppressWarnings("unchecked")
	public double classfyInstance(String v,String g,int m,int numOfPie,double mat,double rain,int pdn,double sh,int fv,int ttv,int up) throws Exception{
		Instance test=new DenseInstance(11);
		FastVector marketNominalVal = new FastVector(4);
		marketNominalVal.addElement("高雄市場");
		marketNominalVal.addElement("台北市場");
		marketNominalVal.addElement("彰化市場");
		marketNominalVal.addElement("台南市場");
		Attribute market = new Attribute("market",marketNominalVal);
		FastVector varietyNominalVal = new FastVector(1);
		varietyNominalVal.addElement(v);
		Attribute variety = new Attribute("variety",varietyNominalVal);
		FastVector gradeNominalVal = new FastVector(1);
		gradeNominalVal.addElement(g);
		Attribute grade = new Attribute("grade",gradeNominalVal);
		Attribute number_of_pieces = new Attribute("number_of_pieces");
		Attribute monthly_average_temperature = new Attribute("monthly_average_temperature");
		Attribute rainfall = new Attribute("rainfall");
		Attribute precipitation_days = new Attribute("precipitation_days");
		Attribute sunshine_hours = new Attribute("sunshine_hours");
		Attribute festival_value = new Attribute("festival_value");
		Attribute total_trading_volume = new Attribute("total_trading_volume");
		Attribute unit_price = new Attribute("unit_price");
		test.setValue(market, m);
		test.setValue(variety, v);
		test.setValue(grade, g);
		test.setValue(number_of_pieces, numOfPie);
		test.setValue(monthly_average_temperature, mat);
		test.setValue(rainfall, rain);
		test.setValue(precipitation_days, pdn);
		test.setValue(sunshine_hours, sh);
		test.setValue(festival_value, fv);
		test.setValue(total_trading_volume, ttv);
		test.setValue(unit_price, up);
		double result=randomForest.classifyInstance(test);
		return result;
	}
	public String makeSQL(){
		String sql="select s.\"market\",s.\"variety\",s.\"grade\","
				+ "s.\"number_of_pieces\",w.\"monthly_average_temperature\",w.\"rainfall\","
				+ "w.\"precipitation_days\",w.\"sunshine_hours\","
				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\","
				+ "s.\"unit_price\" from \"sales\" s LEFT JOIN \"weather\" w "
				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" "
				+ "AND TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
				+ "where s.\"sales_date\">=TO_DATE('2008/02/01','YYYY/MM/DD') AND "
				+ "s.\"sales_date\"<=TO_DATE('2016/06/30','YYYY/MM/DD') ORDER BY s.\"sales_date\"";
		return sql;
	}
}	
