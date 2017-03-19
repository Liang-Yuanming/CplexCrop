package com.crop;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

public class CropMasterProblem {
	public IloCplex cplex;
	
	//目標函數
	private IloObjective obj;
	//情境變數
	private List<int[][][][]> pricesSenario; // 價格情境 
	private List<int[][][][]> supplySenario; //供應量
	private List<int[][][][]> DENS;
	//決策變數
	private IloNumVar q[][]; //種值j 品種  在第k日種植
	private IloNumVar v[];  // j 品種採購量
	private IloNumVar h[][]; // j品種 在第k日收成
	//常數
	//成本常數
	private int c[]; // 第j品種種子成本
	private int E[]; // 代表第k日雇用多少人
	private int CostHire; // 代表每日單位成本
	private int costTransportation[]; //運輸成本 在y情境下
	private int CTFA[]={40,50,50,50,60};
	private int CostFix=1000; //固定成本
	//索引參數
	private int M=5; //市場：台北、台中、彰化、台南、高雄
	private int K=10; //日期： 1~10日
	private int J=5; //百合花品種：1~5種
	private int A=4; //百合花等級： 1~4
	//成本函數
	private IloNumExpr costBulb;
	private IloNumExpr[] costTotal;
	
	public CropMasterProblem(ArrayList<int[][][][]> p,ArrayList<int[][][][]> s,ArrayList<int[][][][]> dens){
		this.pricesSenario=p;//價格
		this.supplySenario=s; //供給
		this.DENS=dens; //每箱分裝量
		this.costTotal=new IloNumExpr[p.size()]; // 每個情境下總成本 初始化
		this.costTransportation=new int[p.size()]; //每個情境下總運輸成本 初始化
		createModel();
	}
	public void createModel(){
		try{
			cplex=new IloCplex();
			obj=cplex.addMaximize();
			
			//成本限制式
			//種子成本
			costBulb=cplex.scalProd(v, c);
			//人力成本
			int costLabor=0;
			for (int i=0;i<E.length;i++){
				costLabor=costLabor+E[i]*CostHire;
			}
			//運輸成本
			for(int i=0;i<costTransportation.length;i++){
				int[][][][] s=supplySenario.get(i);
				int dens[][][][]=DENS.get(i);
				for(int m=0;m<M;m++){
					for(int j=0;j<J;j++){
						for(int k=0;k<K;k++){
							for(int a=0;a<A;a++){
								costTransportation[i]+=(s[m][j][k][a]/dens[m][j][k][a])*CTFA[m];
							}
						}
					}
				}
			}//END 運輸成本
			//總成本
			for(int i=0;i<costTotal.length;i++){
				costTotal[i]=cplex.sum(costBulb,(CostFix+costLabor+costTransportation[i]));
			}
			
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
}