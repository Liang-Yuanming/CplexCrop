package com.crop_v4;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PredictData {
	
	private String market[]; //市場
	private String v; //品種
	private String g; //等級
	private int numOfPie[]; //廂數
	private int salesWeek;  //週數
	private double mat[]; //月平均溫度
	private double rain[]; //降雨量
	private int pdn[]; // 降水日數
	private double sh[]; //日照時數
	private int fv; // 節慶
	private int ttv[]; //總交易量
	private double up[]; //單價
	private int dens; //幾把裝成一箱
	private int scenario; //情境
	private String dateStr;//日期轉換
	public PredictData(String market[],String v,String g,int numOfPie[],int salesWeek,int ttv[],double up[],int dens,int scenario){
		this.market=market;
		this.v=v;
		this.g=g;
		this.numOfPie=numOfPie;
		this.salesWeek=salesWeek;
		this.ttv=ttv;
		this.up=up;
		this.dens=dens;
		this.scenario=scenario;
		this.dateStr=getDate();
		
		this.mat=new double[market.length];
		this.rain=new double[market.length];
		this.pdn=new int[market.length];
		this.sh=new double[market.length];
		build();
		
	}
	//建置天氣資料與節慶
	public void build(){
		Connection conn = ODB.createConn();
		PreparedStatement ps = null;
		ResultSet rs = null;
		for(int i=0;i<market.length;i++){
			
			
			try {
				if (conn.isClosed()){
					conn=ODB.createConn();
				}
				ps = ODB.prepare(conn, makeSqlWeather());
				ps.setString(1, market[i]);
				ps.setString(2, dateStr);
				ps.setString(3, dateStr);
				rs=ps.executeQuery();
				if(rs.next()){
					mat[i]=rs.getDouble("MONTH_AVG_TEMP");
					rain[i]=rs.getDouble("RAINFALL");
					pdn[i]=rs.getInt("PRE_DAY");
					sh[i]=rs.getDouble("SUN_HOURS");
//					System.out.println(market[i]+" = " + mat[i]+","+rain[i]+","+pdn[i]+"," + sh[i]);
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			ODB.close(ps,rs);
		}
		
		try {
			if (conn.isClosed()){
				conn=ODB.createConn();
			}
			ps = ODB.prepare(conn, makeSqlFestival());
			ps.setString(1, dateStr);
			ps.setInt(2, salesWeek);
			rs=ps.executeQuery();
			if(rs.next()){
				fv=1;
			}else{
				fv=0;
			}
//			System.out.println("節慶 = "+fv);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		ODB.close(ps,rs,conn);
		
	}
	//預測
	public double predict(){
		RandomForest_V1 rf=new RandomForest_V1();
		try {
			//
			return rf.predit(market, v, g, numOfPie, salesWeek, mat, rain, pdn, sh, fv, ttv, up,dens);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		return 0;
	}
	public String getDate(){
		SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
		Date beginDate,d;
		String dateStr="";
		try {
			beginDate = df.parse(Common.BEGINDATE[scenario]);
			Calendar c = Calendar.getInstance(); 
			c.setTime(beginDate); 
			c.add(Calendar.DATE, salesWeek*7);
			d=c.getTime();
			dateStr=df.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateStr;
	}
	//取得天氣資料
	public String makeSqlWeather(){
		String sql="SELECT YEAR,MONTH,MONTH_AVG_TEMP,RAINFALL,PRE_DAY,SUN_HOURS,CITY FROM WEATHER "
				+ "WHERE CITY=? AND YEAR=TO_CHAR(ADD_MONTHS(TO_DATE(?,'YYYY/MM/DD'),-1),'YYYY') "
				+ "AND MONTH=TO_CHAR(ADD_MONTHS(TO_DATE(?,'YYYY/MM/DD'),-1),'MM')";
		
		return sql;
	}
	//節慶
	public String makeSqlFestival(){
		String sql="SELECT FESTIVAL_VALUE FROM FESTIVAL "
				+ "WHERE TO_CHAR(TO_DATE(?,'YYYY/MM/DD'),'YYYY')=TO_CHAR(FESTIVAL_DATE,'YYYY') "
				+ "AND FESTIVAL_WEEK=? AND ROWNUM=1";
		return sql;
	
	}
	public static void main(String [] args){
		System.out.println("test");
		String market[]={"台北市場","台中市場","彰化市場","台南市場","高雄市場"};
		String v="FS631";
		String g="A9";
		int numOfPie[]={8,1,4,2,3};
		int salesWeek=39;
		double mat[]={18.2,20.2,20.2,21.5,23.5};
		double rain[]={182.7,38,38,12.8,3.5};
		int pdn[]={18,9,9,3,3};
		double sh[]={60.8,146.8,146.8,194.7,200.5};
		int fv=0;
		int ttv[]={158,20,80,40,60};
		double up[]={95,71,73,81,75};
		int dens=20;
		PredictData predict=new PredictData(market,v,g,numOfPie,salesWeek,ttv,up,dens,0);
		predict.predict();
		
	}
}