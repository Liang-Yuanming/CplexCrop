package com.crop.v2;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.model.Scenario;

public class Crop {
	public static Scenario[] scenario;
	public static int GAP=100000;
	
	
	public static void main(String []args){
		System.out.println("============start working==============");
		int mp_id=0;
		int sp_id=0;
		int supply[][][][]=null;
		//設定上限與下限
		int UB=Integer.MAX_VALUE;   //上限
		int LB=Integer.MIN_VALUE;  // 下限 
		//初始化情境
		int y=1; 
		//Load data
		LoadData load=new LoadData();
		load.start();
		scenario=load.scenario;
		LoadPrice loadPrice=new LoadPrice(scenario);
		loadPrice.start();
		scenario=loadPrice.scenario;
		
		ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
		ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
		ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
		ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
		ps.add(scenario[0].getPrice());
		ss.add(scenario[0].getSupply());
		DT.add(scenario[0].getDens());
		AT.add(scenario[0].getArrival());
		CropMasterProblem mp=new CropMasterProblem(ps,ss,DT,AT,scenario[0].getYA2());
		if(mp.isSolve()){
			UB=mp.getObjectValue();
			System.out.println(" Master Objective value= "+mp.getObjectValue());
			int sb_value[]=new int[scenario.length];
			for(int i=0;i<scenario.length;i++){
				SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,i);
				sb_value[i]=sp.getObjectValue();
				System.out.println("Scenario "+ i + " - Sub Problem Objective value= "+sp.getObjectValue());
			}
			int sub_min=sb_value[0];
			int index_sub=0;
			for(int i=1;i<sb_value.length;i++){
				if(sub_min>sb_value[i]){
					sub_min=sb_value[i];
					index_sub=i;
				}
			}
			mp_id=index_sub;
			SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
			sp_id=index_sub;
			System.out.println("Update LB  Sub Problem Objective value= "+sp.getObjectValue());
			ps.add(scenario[index_sub].getPrice());
			ss.add(sp.getSupply());
			DT.add(scenario[index_sub].getDens());
			AT.add(scenario[index_sub].getArrival());
			supply=sp.getSupply();
			PrintWriter writerS;
			try {
				writerS = new PrintWriter("s1.txt","UTF-8");
				for(int m=0;m<Common.M;m++){
					for(int j=0;j<Common.J;j++){
						for(int k=0;k<Common.K;k++){
							for(int a=0;a<Common.A;a++){
								if(sp.getSupply()[m][j][k][a]!=0){
									writerS.println(Common.MARKET[m]+","+Common.JSTR[j]+","+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
								}
							}
						}
					}
				}
				writerS.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LB=sub_min;
			System.out.println("迭代第" +y +"次");
			System.out.println("UB="+UB);
			System.out.println("LB="+LB);
			while(true){
				y++;
				System.out.println("迭代第" +y +"次");
				System.out.println("ps size = " +ps.size());
				System.out.println(" ===" +index_sub +"===");
				mp=new CropMasterProblem(ps,ss,DT,AT,scenario[index_sub].getYA2());
				if(mp.isSolve()){
					System.out.println(" Master Objective value= "+mp.getObjectValue());
					UB=mp.getObjectValue();
					if(Math.abs(UB-LB)<GAP){
						System.out.println("收斂～");
						System.out.println("UB="+UB);
						System.out.println("LB="+LB);
						PrintWriter writer,writerQ,writerH;
						try {
							writer = new PrintWriter("v.txt","UTF-8");
							int i=0;
							for(int j=0;j<Common.J;j++){
								if(mp.getV()[j]!=0){
									i++;
									writer.print(Common.JSTR[j]+"="+mp.getV()[j]+"   ");
								}
								if(i==10){
									writer.println();
									i=0;
								}
							}
							writer.close();
							writerQ= new PrintWriter("q.txt","UTF-8");
							for(int j=0;j<Common.J;j++){
								writerQ.println("-------"+Common.JSTR[j]+"--------------");;
								for(int k=0;k<Common.K;k++){
									if(mp.getQ()[j][k]!=0){
										writerQ.println(k+" = "+mp.getQ()[j][k]);
									}
								}
								writerQ.println("-------   end   --------------");;
							}
							writerQ.close();
							writerH=new PrintWriter("h.txt","UTF-8");
							for(int j=0;j<Common.J;j++){
								writerH.println("-------"+Common.JSTR[j]+"--------------");;
								for(int k=0;k<Common.K;k++){
									if(mp.getH()[j][k]!=0){
										writerH.println(k+" = "+mp.getH()[j][k]);
									}
								}
								writerH.println("-------   end   --------------");;
							}
							writerH.close();
							
							
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("mp_id="+mp_id);
						System.out.println("sp_id="+sp_id);
						sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
						supply=sp.getSupply();
						try {
							writerS = new PrintWriter("s2.txt","UTF-8");
							for(int m=0;m<Common.M;m++){
								for(int j=0;j<Common.J;j++){
									for(int k=0;k<Common.K;k++){
										for(int a=0;a<Common.A;a++){
											if(sp.getSupply()[m][j][k][a]!=0){
												writerS.println(Common.MARKET[m]+","+Common.JSTR[j]+"-"+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
											}
										}
									}
								}
							}
							writerS.close();
						} catch (FileNotFoundException | UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("sub profit="+sp.getObjectValue());
						Statistics sta=new Statistics();
						for(int i=0;i<10;i++){
							sta.run(mp.getV(), mp.getH(), sp.getSupply(), scenario, i);
						}
						
						
						
						
						break;
					}else{
						for(int i=0;i<scenario.length;i++){
							SubProblem sp_1=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,i);
							System.out.println("Scenario "+ i + " -  Sub Problem Objective value= "+sp.getObjectValue());
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
							sp_id=index_sub;
							sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
							System.out.println("Update LB Sub Problem Objective value- "+index_sub+"="+sp.getObjectValue());
							ps.add(scenario[index_sub].getPrice());
							ss.add(sp.getSupply());
							DT.add(scenario[index_sub].getDens());
							AT.add(scenario[index_sub].getArrival());
							supply=sp.getSupply();
							try {
								writerS = new PrintWriter("s.txt","UTF-8");
								for(int m=0;m<Common.M;m++){
									for(int j=0;j<Common.J;j++){
										for(int k=0;k<Common.K;k++){
											for(int a=0;a<Common.A;a++){
												if(sp.getSupply()[m][j][k][a]!=0){
													writerS.println(Common.MARKET[m]+","+Common.JSTR[j]+"-"+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
												}
											}
										}
									}
								}
								writerS.close();
							} catch (FileNotFoundException | UnsupportedEncodingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
					System.out.println("UB="+UB);
					System.out.println("LB="+LB);
				}else{
					//System.out.println("迭代第" +y +"次");
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