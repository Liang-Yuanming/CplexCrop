package com.crop;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//常數宣告
public class Common {
	//索引
	public final static int M=5; // 市場編號
	public final static int J=175; //品種編號
	public final static int K=366; //日期
	public final static int A=24; //等級
	//市場對應編號
	public final static String MARKET[]={"台北市場","台中市場","彰化市場","台南市場","高雄市場"};
	//品種對應編號
	public final static String JSTR[]={"FH293","FH298","FH364","FH630","FH631","FH633","FH634","FH636","FH661","FK410",
			"FK411","FK412","FK413","FK419","FK420","FK422","FK423","FK429","FK433","FK442","FK443","FK449",
			"FS000","FS002","FS003","FS009","FS010","FS012","FS013","FS019","FS030","FS031","FS032","FS033",
			"FS039","FS040","FS041","FS042","FS043","FS049","FS060","FS130","FS132","FS133","FS139","FS230",
			"FS231","FS232","FS233","FS239","FS350","FS351","FS352","FS353","FS359","FS360","FS361","FS362",
			"FS363","FS369","FS410","FS411","FS412","FS413","FS419","FS430","FS431","FS432","FS433","FS439",
			"FS440","FS441","FS442","FS443","FS449","FS450","FS451","FS452","FS453","FS459","FS460","FS461",
			"FS462","FS463","FS469","FS539","FS560","FS562","FS563","FS569","FS570","FS571","FS572","FS573",
			"FS579","FS590","FS591","FS592","FS593","FS599","FS600","FS601","FS602","FS603","FS609","FS620",
			"FS622","FS623","FS629","FS630","FS631","FS632","FS633","FS639","FS660","FS661","FS662","FS663",
			"FS669","FS670","FS671","FS672","FS673","FS679","FS680","FS681","FS682","FS683","FS689","FS690",
			"FS691","FS692","FS693","FS700","FS702","FS703","FS710","FS712","FS713","FS719","FS720","FS722",
			"FS723","FS729","FS730","FS732","FS733","FS739","FS750","FS752","FS753","FS759","FS770","FS771",
			"FS772","FS773","FS779","FS830","FS831","FS832","FS833","FS839","FS880","FS881","FS882","FS883",
			"FS890","FS892","FS893","FS899","FT660","FT661","FT662","FT663","FT669"}; 
	//等級對應名稱
	public static String ASTR[]={"A","A+","A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","B","B0","B1",
			"B2","B3","B4","B5","B6","B7","B8","B9","格外"};
	//成本常數
	public static int c[]={10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,
			10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10}; // 第j品種種子成本
	public static int E[]={2,2,2,2,2,2,2,2,2,2}; // 代表第k日雇用多少人
	public static int CostHire=1200; //每天每人平均成本為1200元
	public static int CTFA[]={85,85,85,85,85}; //運輸成本
	public static int CostFix=400000; //固定成本
	//到貨日常數
	public static int MM=10000000;//輔助常數
	//每人一天平均種植顆數
	public final static int B=2000;
	
	//熟成期間
	public static int d[]={85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,85,
			85,85,85,85,85,85,85,85,85,85,85}; //第j品種所需收成天數
	public static double Y[]={0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,
			0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8,0.8}; //j品種收成比率

	public static void main(String[] args){
//		String date1="2008/07/01";
//		String date2="2009/06/30";
//		SimpleDateFormat df=new SimpleDateFormat("yyyy/MM/dd");
//		try {
//			Date beginDate=df.parse(date1);
//			Date endDate=df.parse(date2);
//			long betweenDate=((endDate.getTime()-beginDate.getTime())/(1000*60*60*24));
//			int k=(int)betweenDate;
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(beginDate);
//			int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
//			
//			 System.out.println("第"+k+"天");   
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.println(Common.JSTR.length);
		System.out.println(Common.d.length);
		System.out.println(Common.Y.length);   
		System.out.println(Common.searchJ("FS779"));
	}
	public static int searchJ(String str){
		for(int i=0;i<JSTR.length;i++){
			if(JSTR[i].equals(str)){
				return i;
			}
		}
		return -1;
	}
	public static int searchM(String marketStr){
		for(int i=0;i<MARKET.length;i++){
			if(MARKET[i].equals(marketStr)){
				return i;
			}
		}
		return -1;
	}
	public static int searchA(String aStr){
		for(int i=0;i<ASTR.length;i++){
			if(ASTR[i].equals(aStr)){
				return i;
			}
		}
		return -1;
	}
}
