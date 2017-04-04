package com.crop;

import java.util.ArrayList;

import com.model.Scenario;

public class Crop {
	public static Scenario[] scenario;
	public static int GAP=100000;
	public static void main(String []args){
		System.out.println("============start working==============");
		//設定上限與下限
		int UB=Integer.MAX_VALUE;   //上限
		int LB=Integer.MIN_VALUE;  // 下限 
		//初始化情境
		int y=1; 
		//Load data
		LoadData load=new LoadData();
		load.start();
		scenario=load.scenario;
		//
		ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
		ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
		ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
		ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
		ps.add(scenario[0].getPrice());
		ss.add(scenario[0].getPrice());
		DT.add(scenario[0].getDens());
		AT.add(scenario[0].getArrival());
		CropMasterProblem mp=new CropMasterProblem(ps,ss,DT,AT,scenario[0].getYA());
		UB=mp.getObjectValue();
		if(Math.abs(UB-LB)<GAP){
			System.out.println("-------end-----");
		}else{
			SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario);
		}
//		do{
//			//SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),);
//		}while(UB-LB<GAP);
//		
			
	}
	
}