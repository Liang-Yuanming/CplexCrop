package com.crop.day;

import java.util.ArrayList;
import java.util.List;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class SubProblem {
	//由Master problem 給予 
	private int[] v; //採購j品種
	private int[][] q; //第j品種 第k日種下
	private int[][] h; //第j品種 第k日收成
	//決策變數
	private IloNumVar s[][][];

	//參數
	
	private IloCplex cplex;

	//object
	private int obj;
	private Scenario[] scenario;
	private int supply[][][];
	public SubProblem(int[] v,int[][] h,int[][] q,Scenario[] scenario,int i){
		this.v=v;
		this.h=h;
		this.q=q;
		this.scenario=scenario;
		obj=0;
		supply=new int[Common.M][Common.J][Common.K];
		createModel(i);
		
	}
	public void createModel(int sc_y){
		try {
			cplex=new IloCplex();
			
			s=new IloNumVar[Common.M][Common.J][Common.K];
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						s[m][j][k]=cplex.intVar(0, Integer.MAX_VALUE);
					}
				}
			}
			
			//供應限制式
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						IloLinearNumExpr expr=cplex.linearNumExpr();
						for(int m=0;m<Common.M;m++){
							expr.addTerm(1.0, s[m][j][k]);
						}
						cplex.addLe(expr,Math.floor(h[j][k]*0.2));
					}
				}
			}
			//成本函數
			int costBulb=0;
			for(int j=0;j<Common.J;j++){
				costBulb+=Common.c[j]*v[j];
			}
			int costLabor=0;
			for(int k=0;k<Common.K;k++){
				costLabor+=3*Common.CostHire;
			}
			IloLinearNumExpr costTransportExpr=cplex.linearNumExpr();
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						if(scenario[sc_y].getDens()[m][j][k]==0){
							costTransportExpr.addTerm(1.0/12*Common.CTFA[m], s[m][j][k]);
						}else{
							costTransportExpr.addTerm(1.0/scenario[sc_y].getDens()[m][j][k]*Common.CTFA[m], s[m][j][k]);
						}
						
					}
				}
			}
			IloNumExpr costTotal=cplex.numExpr();
			costTotal=cplex.sum(costBulb+costLabor+Common.CostFix, costTransportExpr);
			
			int[][][] temp=scenario[sc_y].getPrice();
			IloLinearNumExpr temProfit=cplex.linearNumExpr();
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						temProfit.addTerm(temp[m][j][k], s[m][j][k]);
					}
				}
			}
			
			IloNumExpr profit=cplex.numExpr();
			profit=cplex.diff(temProfit, costTotal);
			cplex.addMaximize(profit);
			if(cplex.solve()){
				obj=(int)cplex.getObjValue();
				
				double[][][] ss=new double[Common.M][Common.J][];
				for(int m=0;m<Common.M;m++){
					for(int j=0;j<Common.J;j++){
						ss[m][j]=cplex.getValues(s[m][j]);
					}
				}
				for(int m=0;m<Common.M;m++){
					for(int j=0;j<Common.J;j++){
						for(int k=0;k<Common.K;k++){
							supply[m][j][k]=(int)ss[m][j][k];
						}
					}
				}
				
			}
			System.out.println("profit="+cplex.getValue(temProfit));
			System.out.println("cost="+cplex.getValue(costTotal));
			cplex.end();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public int getObjectValue(){
		return obj;
	}
	public int[][][] getSupply(){
		return supply;
	}
}
