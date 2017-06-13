package com.crop.v2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.model.Scenario;

import ilog.concert.IloLinearNumExpr;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class Statistics {
	public static String BEGINDATE[]={"2016/07/01","2015/07/01","2014/07/01","2013/07/01","2012/07/01","2011/07/01",
			"2010/07/01","2009/07/01","2008/07/01","2007/07/01"};
	public void run(int[] v,int[][] h,int s[][][][],Scenario[] scenario,int i) {
		//統計種子成本
		int costBulb=0;
		for(int j=0;j<Common.J;j++){
			costBulb+=Common.c[j]*v[j];
		}
		//統計人力成本
		int costLabor=0;
		for(int k=0;k<Common.K;k++){
			costLabor+=5*Common.CostHire;
		}
		
		int costTranspor=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(s[m][j][k][a]!=0 && scenario[i].getDens()[m][j][k][a]!=0){
							costTranspor+=Math.floor(s[m][j][k][a]/scenario[i].getDens()[m][j][k][a])*Common.CTFA[m];
						}
					}
				}
			}
		}
		int costTotal=0;
		costTotal=costBulb+costLabor+Common.CostFix+costTranspor;
		int profit=0;
		int p[][][][]=scenario[i].getPrice();
		int tempProfit=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(p[m][j][k][a]!=0)
							tempProfit+=p[m][j][k][a]*s[m][j][k][a];
						else if(s[m][j][k][a]!=0)
							tempProfit+=58*s[m][j][k][a];
					}
				}
			}
		}
		
		System.out.println(i+" Profit="+(tempProfit-costTotal));
		
		
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					String variet=Common.JSTR[j];
					SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
					Date beginDate,d;
					String dateStr="";
					try {
						beginDate = df.parse(BEGINDATE[i]);
						Calendar c = Calendar.getInstance(); 
						c.setTime(beginDate); 
						c.add(Calendar.DATE, k);
						d=c.getTime();
						dateStr=df.format(d);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(dateStr);
					String grade=Common.ASTR[a];
					for(int m=0;m<Common.M;m++){
						
					}
				}
			}
		}
		
		
		
		
//		System.out.println(i+" income="+(tempProfit));
//		System.out.println(i+" cost="+(costTotal));
		
	}
	public static void main(String args[]){
		RandomForest_V1 randomforest=new RandomForest_V1();
		randomforest.build();
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					String variet=Common.JSTR[j];
					SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
					Date beginDate,d;
					String dateStr="";
					try {
						beginDate = df.parse(BEGINDATE[0]);
						Calendar c = Calendar.getInstance(); 
						c.setTime(beginDate); 
						c.add(Calendar.DATE, k);
						d=c.getTime();
						dateStr=df.format(d);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println(dateStr);
					String grade=Common.ASTR[a];
					connect(dateStr);
					
//					for(int m=0;m<Common.M;m++){
//						randomforest.classfyInstance(variet, grade, Common.MARKET[m], 12, , rain, pdn, sh, fv, 30, 120)
//					}
				}
			}
		}
	}
	public static String makeSQL(String date){
		String sql="SELECT f.\"festival_value\" FROM \"festival\" f "
				+ "WHERE TO_DATE('"+date+"','YYYY/MM/DD')=f.\"festival_date\"";
		return sql;
	}
	public static void connect(String date){
		 Connection conDB = null;
		 Statement stmt = null;
        try {
        	String DbSID     = "orcl";
    	    String HostName  = "127.0.0.1";
    	    String username  = "mike";
    	    String userpwd   = "mike";
    	    String url = "jdbc:oracle:thin:@"+ HostName + ":1521:" + DbSID; 
    	   
    	    Properties prop = new Properties();
    	    prop.put("user",         username);
            prop.put("password",     userpwd);
            prop.put("CHARSET",     "UTF-8");
			Class.forName("oracle.jdbc.driver.OracleDriver");
			//取得連線
            conDB = DriverManager.getConnection(url, username, userpwd);     
            stmt = conDB.createStatement();
            ResultSet rs = stmt.executeQuery(makeSQL(date));
            int count=0;
            while (rs.next()) {
            	count+=1;
            }
            
            //System.out.println(count);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			 if (stmt != null) { try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
            if(conDB == null){
                try{
                    conDB.close(); //關閉資料庫連結
                }
                catch(SQLException sqle){ }
            }
        }    
	}
}
