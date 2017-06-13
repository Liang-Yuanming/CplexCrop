package com.crop.v2;

import java.util.Random;

import com.model.Scenario;

import ilog.concert.IloException;
import ilog.concert.IloLinearIntExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Planting {
	public static void main(String args[]){
		int year=1;
		Scenario[] scenario;
		//Load data
		LoadData load=new LoadData();
		load.start();
		scenario=load.scenario;
		LoadPrice loadPrice=new LoadPrice(scenario);
		loadPrice.start();
		scenario=loadPrice.scenario;
		
		int vv[]=new int[Common.J];
		int vvcount[]=new int[Common.J];
		
		int v3[]=new int[Common.J];
		int v2[]=new int[Common.J];
		int priceAverage[]=new int[Common.J];
		int priceAverage2[]=new int[Common.J];
		int p_c[]=new int[Common.J];
		int p_c2[]=new int[Common.J];
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					for(int m=0;m<Common.M;m++){
						if(scenario[year].getSupply()[m][j][k][a]!=0){
							v2[j]+=(scenario[year].getSupply()[m][j][k][a]*5);
							vv[j]+=(scenario[year].getSupply()[m][j][k][a]*5);
							priceAverage[j]+=scenario[year].getPrice()[m][j][k][a];
							p_c[j]++;
						}
						if(scenario[year+1].getSupply()[m][j][k][a]!=0){
							v3[j]+=(scenario[year+1].getSupply()[m][j][k][a]*5);
							priceAverage2[j]+=scenario[year+1].getPrice()[m][j][k][a];
							p_c2[j]++;
						}
					}
				}
			}
		}
		for(int j=0;j<Common.J;j++){
			if(v2[j]>0){
				vvcount[j]++;
			}
			
		}
		for(int j=0;j<Common.J;j++){
			if(priceAverage[j]>0)
				priceAverage[j]=priceAverage[j]/p_c[j];
			
		}
		
		
		int sumj=0;
		for(int j=0;j<Common.J;j++){
			
			
			if(vv[j]>0){
				System.out.println("=========================");
				vv[j]=vv[j]/vvcount[j];
				sumj+=vv[j];
				System.out.println(Common.JSTR[j]+" = "+vv[j]);
				System.out.println(Common.JSTR[j]+" price = "+priceAverage[j]);
				System.out.println("===========end==============");
			}
			if(v3[j]>0){
				System.out.println("----------data 2015-------------------");
				priceAverage2[j]=priceAverage2[j]/p_c2[j];
				System.out.println(Common.JSTR[j]+" = "+v3[j]);
				System.out.println(Common.JSTR[j]+" price = "+priceAverage2[j]);
				System.out.println("---------- end data -------------------");
			}
		}
		
		
		System.out.println("sum = "+sumj);
		
		
		//種植統計
		int plantQ[][]=new int[Common.J][Common.K];
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				int sumJK=0;
				for(int m=0;m<Common.M;m++){
					for(int a=0;a<Common.A;a++){
						if(scenario[year].getSupply()[m][j][k][a]>0){
							sumJK+=scenario[year].getSupply()[m][j][k][a];
						}
					}
				}
				if((k-Common.d[j])>=0){
					plantQ[j][k-Common.d[j]]=sumJK;
				}
			}
		}
		
		//種植限制
		int sumVJ=0;
		int v[]=new int[Common.J];
		for(int j=0;j<Common.J;j++){
			if(priceAverage[j]<50 && Common.JSTR[j].equals("FH661")){
				v[j]=0;
			}else if(vv[j]>0 && v3[j]>0){
				v[j]=v3[j];
				sumVJ+=v[j];
			}else if(v3[j]>0){
				Random r=new Random();
				int plant=r.nextInt(500)+100;
				v[j]=plant;
				sumVJ+=v[j];
			}
		}
		System.out.println("count sum= "+sumVJ);
		int plantV[]=v;
		int plantingQ[]=new int[Common.K];
		for(int k=0;k<Common.K;k++){
			plantingQ[k]=5*Common.B;
		}
		int q[][]=new int[Common.J][Common.K];
		while(true){
			Random random=new Random();
			int planting=random.nextInt(Common.J-1)+1;
			
			for(int k=0;k<Common.K;k++){
				if(scenario[year].getArrival()[planting][k]){
					if(plantV[planting]>0 && plantingQ[k]>0){
						if((plantingQ[k]-plantV[planting])>=0){
							q[planting][k]=plantV[planting];
							plantingQ[k]=plantingQ[k]-plantV[planting];
							plantV[planting]=0;
							k+=2;
						}else{
							q[planting][k]=plantingQ[k];
							plantV[planting]=plantV[planting]-plantingQ[k];
							plantingQ[k]=0;
							k+=2;
						}
					}
				}
			}
			boolean check=true;
			for(int j=0;j<Common.J;j++){
				if(plantV[j]>0){
					check=false;
					System.out.println(Common.JSTR[j]+" == "+plantV[j]);
				}
			}
			if(check){
				break;
			}
		}
		int sum=0;
		for(int j=0;j<Common.J;j++){
//			System.out.println("-----"+Common.JSTR[j]+"----------");
			for(int k=0;k<Common.K;k++){
				if(q[j][k]>0){
//					System.out.print( "第 "+k+" 日 = "+q[j][k] +"   ");
					sum+=q[j][k];
				}
			}
//			System.out.println("");
		}
//		System.out.println(sum);
		//收成
		int h[][]=new int[Common.J][Common.K];
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				if((Common.d[j]+k)<365 ){
					if(q[j][k]>0){
						h[j][Common.d[j]+k]=(int)(q[j][k]*Common.Y[j]);
					}
				}
			}
		}
		//分送
		int s_grade[][][]=new int[Common.J][Common.K][Common.A];
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				if(h[j][k]>0){
					for(int a=0;a<Common.A;a++){
						if(scenario[year].getYA()[j][a]>0){
							if(scenario[year].getDens()[0][j][k][a]>0){
								s_grade[j][k][a]=(int)(scenario[year].getYA()[j][a]*0.2*h[j][k]/scenario[1].getDens()[0][j][k][a]);
							}else{
								s_grade[j][k][a]=(int)(scenario[year].getYA()[j][a]*0.2*h[j][k]/12);
							}
						}
						
					}
				}
			}
		}
		for(int j=0;j<Common.J;j++){
			System.out.println("-----"+Common.JSTR[j]+"----------");
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					if(s_grade[j][k][a]>0){
						System.out.print("第 " +k+" 日  "+Common.ASTR[a]+"=" +s_grade[j][k][a] +"   ");
					}
				}
			}
			System.out.println("");
		}
		
		//平均分送
		int s[][][][]=new int[Common.M][Common.J][Common.K][Common.A];
		for(int j=0;j<Common.J;j++){
			for(int k=0;k<Common.K;k++){
				for(int a=0;a<Common.A;a++){
					if(s_grade[j][k][a]>0){
						switch(s_grade[j][k][a]){
						case 1:
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=12;
							}
							break;
						case 2:
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[1][j][k][a]=scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=12;
								s[1][j][k][a]=12;
							}
							
							break;
						case 3:
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[1][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[2][j][k][a]=scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=12;
								s[1][j][k][a]=12;
								s[2][j][k][a]=12;
							}
							break;
						case 4:
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[1][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[2][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[4][j][k][a]=scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=12;
								s[1][j][k][a]=12;
								s[2][j][k][a]=12;
								s[4][j][k][a]=12;
							}
							break;
						case 5:
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[1][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[2][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[3][j][k][a]=scenario[year].getDens()[0][j][k][a];
								s[4][j][k][a]=scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=12;
								s[1][j][k][a]=12;
								s[2][j][k][a]=12;
								s[3][j][k][a]=12;
								s[4][j][k][a]=12;
							}
							break;
						default:
							int average=(int)Math.floor(s_grade[j][k][a]/5);
							int p_d=s_grade[j][k][a]-average*5;
							if(scenario[year].getDens()[0][j][k][a]>0){
								s[0][j][k][a]=(average+p_d)*scenario[year].getDens()[0][j][k][a];
								s[1][j][k][a]=average*scenario[year].getDens()[0][j][k][a];
								s[2][j][k][a]=average*scenario[year].getDens()[0][j][k][a];
								s[3][j][k][a]=average*scenario[year].getDens()[0][j][k][a];
								s[4][j][k][a]=average*scenario[year].getDens()[0][j][k][a];
							}else{
								s[0][j][k][a]=(average+p_d)*12;
								s[1][j][k][a]=average*12;
								s[2][j][k][a]=average*12;
								s[3][j][k][a]=average*12;
								s[4][j][k][a]=average*12;
							}
							break;
						}
						
					}
				}
			}
		}
		int profit=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(scenario[year].getPrice()[m][j][k][a]>0 && s[m][j][k][a] >0){
							profit+=s[m][j][k][a]*scenario[year].getPrice()[m][j][k][a];
						}else if(s[m][j][k][a]>0){
							profit+=s[m][j][k][a]*60;
						}
					}
				}
			}
		}
		int costBulb=0;
		for(int j=0;j<Common.J;j++){
			costBulb+=Common.c[j]*v[j];
		}
		int costLabor=0;
		for(int k=0;k<Common.K;k++){
			costLabor+=5*Common.CostHire;
		}
		int costTransportExpr=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(s[m][j][k][a]>0){
							if(scenario[year].getDens()[m][j][k][a]!=0){
								costTransportExpr+=Common.CTFA[m]*s[m][j][k][a]/scenario[year].getDens()[m][j][k][a];
							}else{
								costTransportExpr+=Common.CTFA[m]*s[m][j][k][a]/12;
							}
						}
						
					}
				}
			}
		}
		int costTotal=0;
		costTotal=costBulb+costLabor+Common.CostFix+Common.COSTF[year]+costTransportExpr;
		
		System.out.println("profit="+(profit-costTotal));
		System.out.println("R="+(profit));
		System.out.println("C="+(costTotal));
		
	}
}
