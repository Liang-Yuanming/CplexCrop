package com.crop;

import ilog.concert.IloException;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

public class CropMasterProblem {
	public IloCplex cplex;
	private IloObjective profit;
	private int scenario;
	public CropMasterProblem(int y){
		this.scenario=y;
		createModel();
	}
	public void createModel(){
		try{
			cplex=new IloCplex();
			profit=cplex.addMaximize();
			
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
}
