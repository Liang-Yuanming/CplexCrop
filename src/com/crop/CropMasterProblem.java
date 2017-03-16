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
	//�����ܼ�
	private List<Integer[][][][]> pricesSenario; // ���污�� 
	private List<Integer[][][][]> supplySenario; //�����q
	//�M���ܼ�
	private IloNumVar q[][]; //�ح�j �~��  �b��k��ش�
	private IloNumVar v[];  // j �~�ر��ʶq
	private IloNumVar h[][]; // j�~�� �b��k�馬��
	//�Ѽ�
	//�����Ѽ�
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
			
			//�������
			
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
}
