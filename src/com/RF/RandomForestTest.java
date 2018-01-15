package com.RF;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Random;

import org.apache.poi.poifs.nio.DataSource;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.experiment.InstanceQuery;
public class RandomForestTest {
	public static void main(String []args)throws Exception{
		int numFolds=10;
		
		InstanceQuery query=new InstanceQuery();
		query.setUsername("mike");
		query.setPassword("mike");
		query.setQuery(makeSQL());
		
		Instances trainData = query.retrieveInstances();
		trainData.setClassIndex(trainData.numAttributes()-1);
//		 BufferedReader reader = new BufferedReader(
//                 new FileReader("C:\\Users\\mike\\Documents\\11222.arff"));
//		Instances test = new Instances(reader);
//		reader.close();
		query.close();
		InstanceQuery testquery=new InstanceQuery();
		testquery.setUsername("mike");
		testquery.setPassword("mike");
		testquery.setQuery(makePredictSQL());
		Instances test = testquery.retrieveInstances();
		test.setClassIndex(test.numAttributes()-1);
		testquery.close();
		double minMean=999999.0;
		int tree=8;
		PrintWriter writerTree=new PrintWriter("unit_price_6_TREE2.csv","UTF-8");
		writerTree.println("#Tree ,MAE,RMSE,Correlation coefficient,timeTook");
		for(int i=8;i<100;i++){
			 long startTime = Calendar.getInstance().getTimeInMillis();
			System.out.println("========== Tree " + i +"  =======");
			RandomForest randomForest = new RandomForest();
	        randomForest.setNumIterations(i);
	        
	        Evaluation evaluation=new Evaluation(test);
	        StringBuffer predsBuffer = new StringBuffer();
            PlainText plainText = new PlainText();
            plainText.setHeader(test);
            plainText.setBuffer(predsBuffer);
            randomForest.buildClassifier(trainData);
            evaluation.evaluateModel(randomForest, test,plainText);
	        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
	        System.out.println(predsBuffer.toString());
	        long endTime = Calendar.getInstance().getTimeInMillis();
	        long timeTook = endTime - startTime;
	        System.out.println("time took in milliseconds="+timeTook);
	        System.out.println("time took in seconds="+timeTook/1000);
	        if(minMean>evaluation.meanAbsoluteError()){
	        	minMean=evaluation.meanAbsoluteError();
	        	tree=i;
	        }
	        
	        //寫入
	        writerTree.println(i+","+evaluation.meanAbsoluteError()+","+evaluation.rootMeanSquaredError()+","+evaluation.correlationCoefficient()+","+timeTook);
		}
		writerTree.close();
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
//		String sql="select "
//				+ "s.\"market\",s.\"variety\","
//				+ "s.\"grade\",w.\"monthly_average_temperature\",w.\"rainfall\",w.\"precipitation_days\",w.\"sunshine_hours\","
//				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\",s.\"unit_price\" "
//				+ "from \"sales\" s LEFT JOIN \"weather\" w "
//				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" AND "
//				+ "TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
//				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
//				+ "where  s.\"sales_date\">=TO_DATE('2016/10/01','YYYY/MM/DD')";
//		String sql="select s.\"market\",s.\"variety\",s.\"grade\","
//				+ "s.\"number_of_pieces\",w.\"monthly_average_temperature\",w.\"rainfall\","
//				+ "w.\"precipitation_days\",w.\"sunshine_hours\","
//				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\","
//				+ "s.\"unit_price\" from \"sales\" s LEFT JOIN \"weather\" w "
//				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" "
//				+ "AND TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
//				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
//				+ "where s.\"sales_date\">=TO_DATE('2010/07/01','YYYY/MM/DD') AND "
//				+ "s.\"sales_date\"<=TO_DATE('2016/06/30','YYYY/MM/DD') ORDER BY s.\"sales_date\"";
		String sql="select s.\"variety\",s.\"grade\","
				+ "s.\"number_of_pieces\",w.\"monthly_average_temperature\",w.\"rainfall\","
				+ "w.\"precipitation_days\",w.\"sunshine_hours\","
				+ "NVL(f.\"festival_value\",0) AS \"festival_value\",s.\"total_trading_volume\","
				+ "s.\"unit_price\" from \"sales\" s LEFT JOIN \"weather\" w "
				+ "ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" "
				+ "AND TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
				+ "where s.\"sales_date\">=TO_DATE('2010/07/01','YYYY/MM/DD') AND "
				+ "s.\"sales_date\"<=TO_DATE('2016/06/30','YYYY/MM/DD') AND s.\"market\"='台北市場' ORDER BY s.\"sales_date\"";
		return sql;
	}
	public static String makePredictSQL(){
		String sql="select s.\"variety\",s.\"grade\",s.\"number_of_pieces\","
				+ "w.\"monthly_average_temperature\",w.\"rainfall\",w.\"precipitation_days\","
				+ "w.\"sunshine_hours\",NVL(f.\"festival_value\",0) AS \"festival_value\","
				+ "s.\"total_trading_volume\",s.\"unit_price\" from \"sales\" s "
				+ "LEFT JOIN \"weather\" w ON TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'MM')=w.\"moth\" "
				+ "AND TO_CHAR(ADD_MONTHS(\"sales_date\",-1), 'yyyy')=w.\"year\" AND w.\"city\"='臺中' "
				+ "LEFT JOIN \"festival\" f ON \"sales_date\"=f.\"festival_date\" "
				+ "where  s.\"sales_date\">=TO_DATE('2016/11/28','YYYY/MM/DD') AND "
				+ "s.\"sales_date\"<=TO_DATE('2016/11/28','YYYY/MM/DD') AND s.\"market\"='台北市場' ORDER BY s.\"sales_date\"";
		return sql;
	}
}
