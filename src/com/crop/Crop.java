package com.crop;

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
			SubProblem sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
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
									writerS.println(Common.Market[m]+","+Common.JSTR[j]+","+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
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
						
						sp=new SubProblem(mp.getV(),mp.getH(),mp.getQ(),scenario,index_sub);
						supply=sp.getSupply();
						try {
							writerS = new PrintWriter("s2.txt","UTF-8");
							for(int m=0;m<Common.M;m++){
								for(int j=0;j<Common.J;j++){
									for(int k=0;k<Common.K;k++){
										for(int a=0;a<Common.A;a++){
											if(sp.getSupply()[m][j][k][a]!=0){
												writerS.println(Common.Market[m]+","+Common.JSTR[j]+"-"+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
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
						Sta sta=new Sta();
						sta.sta(mp.getV(), sp.getSupply(), scenario);
					
//						for(int i=0;i<scenario.length;i++){
//							int sum=0;
//							
//							for(int m=0;m<Common.M;m++){
//								for(int j=0;j<Common.J;j++){
//									for(int k=0;k<Common.K;k++){
//										for(int a=0;a<Common.A;a++){
//											if(supply[m][j][k][a]!=0){
//												if(scenario[i].getPrice()[m][j][k][a]==0){
//													
//													boolean isPrice=false;
//													for(int kk=0;kk<Common.K;kk++){
//														if(scenario[i].getPrice()[m][j][kk][a]!=0){
//															sum+=supply[m][j][k][a]*scenario[i].getPrice()[m][j][kk][a];
////															System.out.println("true");
//															isPrice=true;
//															break;
//														}
//													}
//													if(!isPrice){
//														System.out.println(i+","+Common.Market[m]+","+Common.JSTR[j]+"-"+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
//														if(Common.JSTR[j].equals("FS683")){
//															if(Common.ASTR[a].equals("A1")){
//																sum+=supply[m][j][k][a]*90;
//															}else if(Common.ASTR[a].equals("A8")){
//																sum+=supply[m][j][k][a]*131;
//															}else if(Common.ASTR[a].equals("A9")){
//																sum+=supply[m][j][k][a]*159;
//															}
//														}else if(Common.JSTR[j].equals("FS689")){
//															if(Common.ASTR[a].equals("A9")){
//																sum+=supply[m][j][k][a]*130;
//															}else if(Common.ASTR[a].equals("A8")){
//																sum+=supply[m][j][k][a]*130;
//															}else if(Common.ASTR[a].equals("A0")){
//																sum+=supply[m][j][k][a]*294;
//															}else if(Common.ASTR[a].equals("A1")){
//																sum+=supply[m][j][k][a]*128;
//															}
//														}else if(Common.JSTR[j].equals("FS682")){
//															if(Common.ASTR[a].equals("A7")){
//																sum+=supply[m][j][k][a]*123;
//															}else if(Common.ASTR[a].equals("A8")){
//																sum+=supply[m][j][k][a]*116;
//															}else if(Common.ASTR[a].equals("A9")){
//																sum+=supply[m][j][k][a]*108;
//															}else if(Common.ASTR[a].equals("A6")){
//																sum+=supply[m][j][k][a]*128;
//															}else if(Common.ASTR[a].equals("A")){
//																sum+=supply[m][j][k][a]*128;
//															}else if(Common.ASTR[a].equals("B6")){
//																sum+=supply[m][j][k][a]*60;
//															}else if(Common.ASTR[a].equals("B7")){
//																sum+=supply[m][j][k][a]*70;
//															}
//															
//														}else if(Common.JSTR[j].equals("FS680")){
//															if(Common.ASTR[a].equals("A0")){
//																sum+=supply[m][j][k][a]*125;
//															}else if(Common.ASTR[a].equals("A9")){
//																sum+=supply[m][j][k][a]*127;
//															}else if(Common.ASTR[a].equals("A8")){
//																sum+=supply[m][j][k][a]*78;
//															}else if(Common.ASTR[a].equals("A7")){
//																sum+=supply[m][j][k][a]*91;
//															}else if(Common.ASTR[a].equals("B7")){
//																sum+=supply[m][j][k][a]*38;
//															}else if(Common.ASTR[a].equals("B8")){
//																sum+=supply[m][j][k][a]*60;
//															}else if(Common.ASTR[a].equals("B9")){
//																sum+=supply[m][j][k][a]*70;
//															}else if(Common.ASTR[a].equals("A6")){
//																sum+=supply[m][j][k][a]*128;
//															}else if(Common.ASTR[a].equals("A1")){
//																sum+=supply[m][j][k][a]*128;
//															}
//															
//														}else{
//															sum+=supply[m][j][k][a]*250;
//														}
//													}
//												}else{
//													sum+=supply[m][j][k][a]*scenario[i].getPrice()[m][j][k][a];
//												}
//												
//											}
//										}
//									}
//								}
//							}
//							int costBulb=0;
//							for(int j=0;j<Common.J;j++){
//								if(mp.getV()[j]!=0){
//									costBulb+=mp.getV()[j]*Common.c[j];
//								}
//							}
//							int costLabor=0;
//							costLabor=365*1*1200;
//							int costTransport=0;
//							for(int m=0;m<Common.M;m++){
//								for(int j=0;j<Common.J;j++){
//									for(int k=0;k<Common.K;k++){
//										for(int a=0;a<Common.A;a++){
//											if(scenario[i].getDens()[m][j][k][a]!=0 && supply[m][j][k][a]!=0){
//												costTransport+=(Math.floor(supply[m][j][k][a]/scenario[i].getDens()[m][j][k][a])*Common.CTFA[m]);
//											}
//										}
//									}
//								}
//							}
//							int costTotal=costBulb+costLabor+Common.CostFix+costTransport;
//							int profit=sum-costTotal;
//							System.out.println(i+" = "+sum);
//							System.out.println(i+" = "+profit);
//						}
						
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
													writerS.println(Common.Market[m]+","+Common.JSTR[j]+"-"+k+","+Common.ASTR[a]+","+sp.getSupply()[m][j][k][a]);
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