package com.crop.v2;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class RandomForestCrop {
	private int numFolds=10;
	private String Account="mike";
	private String Password="mike";
	private InstanceQuery query;
	private Instances trainData;
	public RandomForestCrop(String market,int classIndex){
		try {
			query=new InstanceQuery();
			query.setUsername(Account);
			query.setPassword(Password);
			query.setQuery(makeSQL(market));
			trainData = query.retrieveInstances();
			trainData.setClassIndex(trainData.numAttributes()-classIndex);
			query.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void predict(){
		Instance inst = new DenseInstance(3); 
	}
	public static String makeSQL(String market){
		String sql="select s.\"variety\",s.\"grade\","
				+ "s.\"number_of_pieces\",w.\"monthly_average_temperature\",w.\"rainfall\","
				+ "w.\"precipitation_days\",w.\"sunshine_hours\","
				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\","
				+ "s.\"unit_price\" from \"sales\" s LEFT JOIN \"weather\" w "
				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" "
				+ "AND TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
				+ "where s.\"sales_date\">=TO_DATE('2010/07/01','YYYY/MM/DD') AND "
				+ "s.\"sales_date\"<=TO_DATE('2016/06/30','YYYY/MM/DD') AND s.\"market\"='"+market+"' ORDER BY s.\"sales_date\"";
		return sql;
	}
}
