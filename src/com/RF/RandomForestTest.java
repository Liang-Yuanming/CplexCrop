package com.RF;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
public class RandomForestTest {
	public static void main(String []args)throws Exception{
		BufferedReader br=null;
		int numFolds=10;
		
		InstanceQuery query=new InstanceQuery();
		query.setUsername("mike");
		query.setPassword("mike");
		query.setQuery(makeSQL());
		
		Instances trainData = query.retrieveInstances();
		trainData.setClassIndex(trainData.numAttributes()-1);
		double minMean=999999.0;
		int tree=8;
		for(int i=8;i<2000;i++){
			RandomForest randomForest = new RandomForest();
	        randomForest.setNumIterations(i);

	        Evaluation evaluation=new Evaluation(trainData);
	        evaluation.crossValidateModel(randomForest, trainData, numFolds, new Random(1));
	        System.out.println("========== Tree " + i +"  =======");
	        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
	        if(minMean>evaluation.meanAbsoluteError()){
	        	minMean=evaluation.meanAbsoluteError();
	        	tree=i;
	        }
		}
		System.out.println("生成="+tree);
		System.out.println("Mean absolute error =   " +minMean);
		
//         System.out.println("Results For Class -1- ");
//         System.out.println("Precision=  " + evaluation.precision(0));
//         System.out.println("Recall=  " + evaluation.recall(0));
//         System.out.println("F-measure=  " + evaluation.fMeasure(0));
//         System.out.println("Results For Class -2- ");
//         System.out.println("Precision=  " + evaluation.precision(1));
//         System.out.println("Recall=  " + evaluation.recall(1));
//         System.out.println("F-measure=  " + evaluation.fMeasure(1));
	}
	public static String makeSQL(){
//		String sql="select TO_CHAR(\"sales_date\", 'YYYY/MM/DD') as \"market_date\","
//				+ "s.\"market\",s.\"variety\","
//				+ "s.\"grade\",w.\"monthly_average_temperature\",w.\"rainfall\",w.\"precipitation_days\",w.\"sunshine_hours\","
//				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\",s.\"unit_price\" "
//				+ "from \"sales\" s LEFT JOIN \"weather\" w "
//				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" AND "
//				+ "TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
//				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
//				+ "where  s.\"sales_date\">=TO_DATE('2017/01/01','YYYY/MM/DD')";
		String sql="select "
				+ "s.\"market\",s.\"variety\","
				+ "s.\"grade\",w.\"monthly_average_temperature\",w.\"rainfall\",w.\"precipitation_days\",w.\"sunshine_hours\","
				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\",s.\"unit_price\" "
				+ "from \"sales\" s LEFT JOIN \"weather\" w "
				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" AND "
				+ "TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
				+ "where  s.\"sales_date\">=TO_DATE('2016/10/01','YYYY/MM/DD')";
		return sql;
	}
}
