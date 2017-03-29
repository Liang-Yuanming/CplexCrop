package com.crop;

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
	private IloNumVar s[][][][];
	//參數
	private int DENS[][][][];
	
	private IloCplex cplex;
	public SubProblem(){
		
	}
	public void createModel(){
		try {
			cplex=new IloCplex();
			
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
			
			//供應限制式
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						IloLinearNumExpr expr=cplex.linearNumExpr();
						for(int m=0;m<Common.M;m++){
							expr.addTerm(1.0, s[m][j][k][a]);
						}
						
						cplex.addLe(expr,Math.floor(h[j][k]/5)*Common.YA[j][a]);
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
			IloNumExpr costTransportExpr=cplex.numExpr();
			for(int m=0;m<Common.M;m++){
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						for(int a=0;a<Common.A;a++){
							costTransportExpr=cplex.sum(costTransportExpr,cplex.prod(1.0/DENS[m][j][k][a]*Common.CTFA[m],s[m][j][k][a]));
						}
					}
				}
			}
			IloNumExpr costTotal=cplex.numExpr();
			costTotal=cplex.sum(costBulb+costLabor, costTransportExpr);
			
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
