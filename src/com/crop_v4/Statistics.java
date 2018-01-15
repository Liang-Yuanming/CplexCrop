package com.crop_v4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;


import ilog.concert.IloLinearNumExpr;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class Statistics {
//	public static String BEGINDATE[]={"2015/07/01"};
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
			costLabor+=3*Common.CostHire;
		}
		
		int costTranspor=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(s[m][j][k][a]!=0 ){
							if(scenario[i].getDens()[m][j][k][a]==0){
								costTranspor+=Math.floor(s[m][j][k][a]/12)*Common.CTFA[m];
							}else{
								costTranspor+=Math.floor(s[m][j][k][a]/scenario[i].getDens()[m][j][k][a])*Common.CTFA[m];
							}
							
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
						if(p[m][j][k][a]==0 && s[m][j][k][a]!=0){
//							System.out.println(Common.MARKET[m]+" = " +s[m][j][k][a]);
							tempProfit+=120*s[m][j][k][a];
						}else{
							tempProfit+=p[m][j][k][a]*s[m][j][k][a];
						}
						
					}
					
				}
			}
		}
		
		System.out.println(BEGINDATE[i]+" Profit="+(tempProfit-costTotal));
		//RF 利潤
		double profitNew=0;
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					
					String variet=Common.JSTR[j];
					//System.out.println(dateStr);
					String grade=Common.ASTR[a];
					ArrayList<String> marketArray=new ArrayList<String>();
					ArrayList<Integer> numOfPieArray=new ArrayList<Integer>();
					ArrayList<Integer> ttvArray=new ArrayList<Integer>();
					ArrayList<Double> upArray=new ArrayList<Double>();
					int dens=12;
					for(int m=0;m<Common.M;m++){
						if(s[m][j][k][a]!=0){
							
							marketArray.add(Common.MARKET[m]);
							if(scenario[i].getDens()[m][j][k][a]!=0){
								numOfPieArray.add((int)Math.ceil(s[m][j][k][a]/scenario[i].getDens()[m][j][k][a]));
								dens=scenario[i].getDens()[m][j][k][a];
							}else{
								numOfPieArray.add((int)Math.ceil(s[m][j][k][a]/dens));
							}
							ttvArray.add(s[m][j][k][a]);
							upArray.add((double)p[m][j][k][a]);
						}
					}
					String market[]= new String[marketArray.size()];
					int numOfPie[]=new int[marketArray.size()];
					int salesWeek=k;
					int ttv[]=new int[marketArray.size()];
					double up[]=new double[marketArray.size()];
					
					for(int x=0;x<market.length;x++){
						market[x]=marketArray.get(x);
						numOfPie[x]=numOfPieArray.get(x);
						ttv[x]=ttvArray.get(x);
						if(upArray.get(x)==0){
							up[x]=120;
						}else{
							up[x]=upArray.get(x);
						}
						
					}
					if(marketArray.size()>0 && market.length>0 &&  numOfPie.length>0 && ttv.length >0 && up.length >0){
						double pp=0;
						try{
							PredictData predict=new PredictData(market,variet,grade,numOfPie,salesWeek,ttv,up,dens,i);
							pp=predict.predict();
						}catch (NullPointerException e){
							
						}
						if(pp==0){
							
							for(int x=0;x<market.length;x++){
//								System.out.println(market[x]+" = " +up[x] + " , " + ttv[x]);
								pp+=ttv[x]*up[x];
								profitNew+=pp;
							}
						}else{
							profitNew+=pp;
						}
						
					}
					
				}
			}
		}
		System.out.println("RF Profit = "+(profitNew-costTotal));
//		
		//平均法則：
		int ruleProfit=0;
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					int dens=12;
					
					int box=0;
					int totalVolumn=0;
					ArrayList<String> marketList=new ArrayList<String>();
					for(int m=0;m<Common.M;m++){
						totalVolumn+=s[m][j][k][a];
						if(s[m][j][k][a]!=0){
							marketList.add(Common.MARKET[m]);
						}
						if(scenario[i].getDens()[m][j][k][a]!=0){
							dens=scenario[i].getDens()[m][j][k][a];
						}
					}
					//平均分送
					box=(int)Math.floor(totalVolumn/dens);
					if(marketList.size()>0){
						int avgBox=(int)Math.floor(box/marketList.size());
						int otherBox=(box-(avgBox*marketList.size()));
						for(int m=0;m<Common.M;m++){
							if(s[m][j][k][a]!=0){
								if(p[m][j][k][a]==0){
									ruleProfit+=120*((avgBox+otherBox)*dens);
									otherBox=0;
								}else{
									ruleProfit+=p[m][j][k][a]*((avgBox+otherBox)*dens);
									otherBox=0;
								}
							}
						}
					}
					
					
				}
			}
		}
		
		System.out.println("rule Profit = "+(ruleProfit-costTotal));
//		System.out.println(i+" income="+(tempProfit));
//		System.out.println(i+" cost="+(costTotal));
		
	}
	public static void main(String args[]){
		RandomForest_V1 randomforest=new RandomForest_V1();
		randomforest.build(-1,11);
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
