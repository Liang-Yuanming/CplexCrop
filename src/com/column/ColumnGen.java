package com.column;

import ilog.concert.IloException;
import ilog.concert.IloObjective;
import ilog.cplex.IloCplex;

public class ColumnGen {
	//¥D°ÝÃD
	public class MasterProblem{
		public IloCplex cplex;
		private IloObjective total_cost;
		public MasterProblem(){
			createModel();
		}
		
		public void createModel(){
			try{
				cplex=new IloCplex();
				total_cost=cplex.addMinimize();
				
			}catch(IloException e){
				e.printStackTrace();
			}
		}
	}
}
