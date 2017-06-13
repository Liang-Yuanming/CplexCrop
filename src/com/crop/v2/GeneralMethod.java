package com.crop.v2;

import java.util.Random;

import com.crop.v2.Common;
import com.crop.v2.LoadData;
import com.crop.v2.LoadPrice;
import com.model.Scenario;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class GeneralMethod {

		
	public  static void main(String args[]){
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
		
		int priceCount100[]=new int[Common.J];
		for(int i=2;i<4;i++){
			int v2[]=new int[Common.J];
			int priceAverage[]=new int[Common.J];
			int p_c[]=new int[Common.J];
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						for(int m=0;m<Common.M;m++){
							if(scenario[i].getSupply()[m][j][k][a]!=0){
								v2[j]+=(scenario[i].getSupply()[m][j][k][a]*5);
								vv[j]+=(scenario[i].getSupply()[m][j][k][a]*5);
								priceAverage[j]+=scenario[i].getPrice()[m][j][k][a];
								p_c[j]++;
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
				if(priceAverage[j]>100){
					priceCount100[j]++;
				}
			}
		}
		
		int sumj=0;
		for(int j=0;j<Common.J;j++){
			
			
			if(vv[j]>0){
				vv[j]=vv[j]/vvcount[j];
				sumj+=vv[j];
				System.out.println(Common.JSTR[j]+" = "+vv[j]);
				System.out.println(Common.JSTR[j]+" price count 100= "+priceCount100[j]);
			}
		}
		
		System.out.println("sum = "+sumj);
		
		
		try {
			IloCplex cplex=new IloCplex();
			IloNumVar q[][]; //種值j 品種  在第k日種植
			IloNumVar v[];  // j 品種採購量
			IloNumVar h[][]; // j品種 在第k日收成
			IloNumVar s[][][][];
			IloNumExpr L[]=new IloNumExpr[Common.K];//第k日土地種植量
			IloNumExpr costBulb;
			int Lmax=800000;
			q=new IloNumVar[Common.J][Common.K];
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					q[j][k]=cplex.intVar(0, Integer.MAX_VALUE);
				}
			}
			v=new IloNumVar[Common.J];
			for(int j=0;j<Common.J;j++){
				v[j]=cplex.intVar(0, Integer.MAX_VALUE);
			}
			h=new IloNumVar[Common.J][Common.K];
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					h[j][k]=cplex.intVar(0, Integer.MAX_VALUE);
				}
			}
			
			s=new IloNumVar[Common.M][Common.J][Common.K][Common.A];
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						for(int a=0;a<Common.A;a++){
							s[m][j][k][a]=cplex.intVar(0, Integer.MAX_VALUE);
						}
					}
				}
			}
			//成本限制式
			//種子成本
			costBulb=cplex.scalProd(v, Common.c);
			//人力成本
			int costLabor=0;
			for (int i=0;i<Common.K;i++){
				costLabor=costLabor+5*Common.CostHire;
			}
			IloLinearNumExpr costTransportExpr=cplex.linearNumExpr();
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						for(int a=0;a<Common.A;a++){
							if(scenario[0].getDens()[m][j][k][a]!=0){
								costTransportExpr.addTerm(1.0/scenario[0].getDens()[m][j][k][a]*Common.CTFA[m], s[m][j][k][a]);
							}
						}
					}
				}
			}
			IloNumExpr costTotal=cplex.numExpr();
			costTotal=cplex.sum(cplex.sum(cplex.sum(costBulb,costLabor),Common.CostFix), costTransportExpr);
			
			int[][][][] temp=scenario[0].getPrice();
			IloLinearNumExpr temProfit=cplex.linearNumExpr();
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						for(int a=0;a<Common.A;a++){
							if(temp[m][j][k][a]!=0){
								temProfit.addTerm(temp[m][j][k][a], s[m][j][k][a]);
							}else {
								temProfit.addTerm(20, s[m][j][k][a]);
							}
							
						}
					}
				}
			}
			
			//種子限制式
			for(int j=0;j<Common.J;j++){
				IloLinearNumExpr qexpr=cplex.linearNumExpr();
				for(int k=0;k<Common.K;k++){
					qexpr.addTerm(1.0, q[j][k]);
				}
				cplex.addLe(qexpr, v[j]);
			}
			for(int j=0;j<Common.J;j++){
				if(priceCount100[j]>2){
					cplex.addEq(vv[j], v[j]);
				}else{				
					cplex.addLe(vv[j], v[j]);
				}
			}
			
			//到貨日限制
			boolean [][]tempArrival=scenario[0].getArrival();
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					if(tempArrival[j][k]){
						cplex.addLe(q[j][k],200000);
						
					}else{
						cplex.addLe(q[j][k],0);
					}
				}
			}
			boolean plantB[]=new boolean[Common.J];
			
//			for(int k=0;k<Common.K;k++){
//				Random random=new Random();
//				int planting=random.nextInt(Common.J-1)+1;
//				if(tempArrival[planting][k] && !plantB[planting]){
//					
//					cplex.addLe(q[j][k],200000);
//				}
//			}
			//人力限制
			for(int k=0;k<Common.K;k++){
				IloLinearNumExpr labor=cplex.linearNumExpr();
				for(int j=0;j<Common.J;j++){
					labor.addTerm(1, q[j][k]);
				}
				cplex.addLe(labor,5*Common.B);
			}
			//收割限制式
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					IloLinearNumExpr hexpr=cplex.linearNumExpr();
					hexpr.addTerm(Common.Y[j],q[j][k]);
					if((Common.d[j]+k)<365  )
						cplex.addEq( h[j][k+Common.d[j]],hexpr,"Havest_"+Common.JSTR[j]+"_"+k);
					
				}
			}
			//土地限制式
			for(int k=0;k<Common.K;k++){
				IloLinearNumExpr havestExpr=cplex.linearNumExpr();
				for(int j=0;j<Common.J;j++){
					if(k==0){
						havestExpr.addTerm(1.0, q[j][k]);
						havestExpr.addTerm(-1.0, h[j][k]);
						L[k]=havestExpr;
					}else{
						havestExpr.addTerm(1.0, q[j][k]);
						havestExpr.addTerm(-1.0, h[j][k]);
						L[k]=cplex.sum(havestExpr,L[k-1]);
					}
				}
				cplex.addLe(L[k], Lmax);
			}
			//供應限制式
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						IloLinearNumExpr expr=cplex.linearNumExpr();
						for(int m=0;m<Common.M;m++){
							expr.addTerm(1.0, s[m][j][k][a]);
						}
						IloLinearNumExpr expr2=cplex.linearNumExpr();
						expr2.addTerm(0.3*scenario[1].getYA()[j][a], h[j][k]);
						cplex.addLe(expr,expr2);
					}
				}
			}
			
			IloNumExpr profit=cplex.numExpr();
			profit=cplex.diff(temProfit, costTotal);
			cplex.addMaximize(temProfit);
			
			
			if(cplex.solve()){
				//cplex.exportModel("a.lp"); 
				System.out.println("profit="+cplex.getObjValue());
				
				double[][] hh=new double[Common.J][];
				for(int j=0;j<Common.J;j++){
					hh[j]=cplex.getValues(h[j]);
				}
				double[] varx=cplex.getValues(v);

				int[] tempV=new int[Common.J];
				for(int j=0;j<Common.J;j++){
					tempV[j]=(int)varx[j];
				}
				int[][] tmepHavest=new int[Common.J][Common.K];
				for(int j=0;j<Common.J;j++){
					System.out.println("-----"+Common.JSTR[j]+"----------");
					for(int k=0;k<Common.K;k++){
						tmepHavest[j][k]=(int)hh[j][k];
						if(tmepHavest[j][k]!=0)
							System.out.print( "第 "+k+" 日 = "+tmepHavest[j][k] +"   ");
					}
					System.out.println("");
				}
				for(int j=0;j<Common.J;j++){
					System.out.println(Common.JSTR[j]+" = "+tempV[j]);
				}
				
			}
			cplex.end();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
