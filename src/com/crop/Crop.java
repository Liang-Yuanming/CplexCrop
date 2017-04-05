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
		
		ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
		ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
		ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
		ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
		ps.add(scenario[0].getPrice());
		ss.add(scenario[0].getSupply());
		DT.add(scenario[0].getDens());
		AT.add(scenario[0].getArrival());
		CropMasterProblem mp=new CropMasterProblem(ps,ss,DT,AT,scenario[0].getYA());
		if(mp.isSolve()){
			UB=mp.getObjectValue();
			System.out.println(" Master Objective value= "+mp.getObjectValue());
			int sb_value[]=new int[scenario.length];
			for(int i=0;i<scenario.length;i++){
				SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,i);
				sb_value[i]=sp.getObjectValue();
			}
			int sub_min=sb_value[0];
			int index_sub=0;
			for(int i=1;i<sb_value.length;i++){
				if(sub_min>sb_value[i]){
					sub_min=sb_value[i];
					index_sub=i;
				}
			}
			SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
			ps.add(scenario[index_sub].getPrice());
			ss.add(sp.getSupply());
			DT.add(scenario[index_sub].getDens());
			AT.add(scenario[index_sub].getArrival());
			LB=sub_min;
			while(true){
				y++;
				mp=new CropMasterProblem(ps,ss,DT,AT,scenario[index_sub].getYA());
				UB=mp.getObjectValue();
				if(mp.isSolve()){
					if(Math.abs(UB-LB)<GAP){
						System.out.println("收斂～");
						break;
					}else{
						for(int i=0;i<scenario.length;i++){
							SubProblem sp_1=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,i);
							sb_value[i]=sp_1.getObjectValue();
						}
						sub_min=sb_value[0];
						index_sub=0;
						for(int i=1;i<sb_value.length;i++){
							if(sub_min>sb_value[i]){
								sub_min=sb_value[i];
								index_sub=i;
							}
						}
						if(LB>sub_min){
							LB=sub_min;
							sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
							ps.add(scenario[index_sub].getPrice());
							ss.add(sp.getSupply());
							DT.add(scenario[index_sub].getDens());
							AT.add(scenario[index_sub].getArrival());
						}
					}
					
				}else{
					System.out.println("迭代第" +y +"次");
					System.out.println("UB="+UB);
					System.out.println("LB="+LB);
					
					System.out.println("Master Problem infeasible!!");
					break;
				}
			}
		}else{
			System.out.println("Master Problem infeasible!!");
		}
		
		//
		
		
	
			
	}
	
}