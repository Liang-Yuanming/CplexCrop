package com.crop;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

public class CropMasterProblem {
	public IloCplex cplex;
	private IloObjective profit;
	//情境變數
	private List<Integer[][][][]> pricesSenario; // 價格情境 
	private List<Integer[][][][]> supplySenario; //供應量
	//決策變數
	private IloNumVar q[][]; //種值j 品種  在第k日種植
	private IloNumVar v[];  // j 品種採購量
	private IloNumVar h[][]; // j品種 在第k日收成
	//參數
	//成本參數
	private IloNumExpr costTotal,costFix,costBulb,costLabor,costTransportation;
	public CropMasterProblem(ArrayList<Integer[][][][]> p,ArrayList<Integer[][][][]> s){
		this.pricesSenario=p;
		this.supplySenario=s;
		createModel();
	}
	public void createModel(){
		try{
			cplex=new IloCplex();
			profit=cplex.addMaximize();
			
			//成本限制式
			
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
}
