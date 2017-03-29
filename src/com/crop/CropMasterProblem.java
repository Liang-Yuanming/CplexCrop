package com.crop;

import java.util.ArrayList;
import java.util.List;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
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
	private List<boolean[][]> arrival; //到貨日 0 or 1
	//決策變數
	private IloNumVar q[][]; //種值j 品種  在第k日種植
	private IloNumVar v[];  // j 品種採購量
	private IloNumVar h[][]; // j品種 在第k日收成
	//private IloNumExpr h[][];
	private IloNumVar eta;
	//常數
	//成本常數
	//private int E[]={2,2,2,2,2,2,2,2,2,2}; // 代表第k日雇用多少人
	
	private int costTransportation[]; //運輸成本 在y情境下


	//土地常數
	private IloNumExpr L[]; //第k日土地種植量
	private int Lmax=800000;//最大限制
	
	private double YA[][];
	//成本函數
	private IloNumExpr costBulb;
	private IloNumExpr[] costTotal;
	
	public CropMasterProblem(ArrayList<int[][][][]> p,ArrayList<int[][][][]> s,ArrayList<int[][][][]> dens,ArrayList<boolean[][]> arrival,double[][] YA){
		this.pricesSenario=p;//價格
		this.supplySenario=s; //供給
		this.DENS=dens; //每箱分裝量
		this.costTotal=new IloNumExpr[p.size()]; // 每個情境下總成本 初始化
		this.costTransportation=new int[p.size()]; //每個情境下總運輸成本 初始化
		this.arrival=arrival; //到達情境
		this.L=new IloNumExpr[Common.K];
		//this.h=new IloNumExpr[Common.J][Common.K];
		this.YA=YA;
		createModel();
	}
	public void createModel(){
		try{
			cplex=new IloCplex();
			//初始化決策變數
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
			eta=cplex.intVar(0, Integer.MAX_VALUE);
			
			obj=cplex.addMaximize(eta);
			
			

			//成本限制式
			//種子成本
			costBulb=cplex.scalProd(v, Common.c);
			//人力成本
			int costLabor=0;
			for (int i=0;i<Common.K;i++){
				costLabor=costLabor+3*Common.CostHire;
			}
			//運輸成本
			for(int i=0;i<costTransportation.length;i++){
				int[][][][] s=supplySenario.get(i);
				int dens[][][][]=DENS.get(i);
				for(int m=0;m<Common.M;m++){
					for(int j=0;j<Common.J;j++){
						for(int k=0;k<Common.K;k++){
							for(int a=0;a<Common.A;a++){
								if(s[m][j][k][a]!=0){
									costTransportation[i]+=(s[m][j][k][a]/dens[m][j][k][a])*Common.CTFA[m];
								}
							}
						}
					}
				}
			}//END 運輸成本
			//總成本
			for(int i=0;i<costTotal.length;i++){
				costTotal[i]=cplex.sum(costBulb,(Common.CostFix+costLabor+costTransportation[i]));
			}
			//利潤限制式
			for(int i=0;i<this.pricesSenario.size();i++){
				int p[][][][]=this.pricesSenario.get(i);
				int s[][][][]=this.supplySenario.get(i);
				IloNumExpr tempProfit=cplex.numExpr();
				for(int m=0;m<Common.M;m++){
					for(int j=0;j<Common.J;j++){
						for(int k=0;k<Common.K;k++){
							for(int a=0;a<Common.A;a++){
								tempProfit=cplex.sum(tempProfit,(p[m][j][k][a]*s[m][j][k][a]));
							}
						}
					}
				}
				IloNumExpr profit=cplex.numExpr();
				profit=cplex.diff(tempProfit,costTotal[i]);
				cplex.addLe(eta,profit);
			}
			//種子限制式
			for(int j=0;j<Common.J;j++){
				IloLinearNumExpr qexpr=cplex.linearNumExpr();
				for(int k=0;k<Common.K;k++){
					qexpr.addTerm(1.0, q[j][k]);
				}
				cplex.addLe(qexpr, v[j]);
			}
			//到貨日限制
			for(int i=0;i<arrival.size();i++){
				boolean [][]tempArrival=this.arrival.get(i);
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						if(tempArrival[j][k]){
							cplex.addLe(q[j][k],Common.MM);
						}else{
							cplex.addLe(q[j][k],0);
						}
					}
				}
			}
			//人力限制
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					cplex.addLe(q[j][k],20*Common.B);
				}
			}
			//收割限制式
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					IloLinearNumExpr hexpr=cplex.linearNumExpr();
					hexpr.addTerm(q[j][k], Common.Y[j]);
					if((Common.d[j]+k)<365)
						cplex.addEq(h[j][k],hexpr);
					
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
			for(int i=0;i<this.supplySenario.size();i++){
				int[][][][] s=this.supplySenario.get(i);
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						for(int a=0;a<Common.A;a++){
							int sumSupply=0;
							for(int m=0;m<Common.M;m++){
								sumSupply+=s[m][j][k][a];
							}
							IloNumExpr expr=cplex.numExpr();
							expr=cplex.prod(YA[j][a],h[j][k]);
							cplex.addLe(sumSupply,expr);
						}
					}
				}
			}
			//cplex.exportModel("a.pl");
			if(cplex.solve()){
				//cplex.exportModel("ap.lp"); 
				int ee=(int) cplex.getValue(eta);
				System.out.println("eta = " + ee);
				double[] vv=cplex.getValues(v);
				for(int j=0;j<Common.J;j++){
					if(vv[j]!=0){
						System.out.println(Common.JSTR[j]+" = "+ vv[j]);
					}
					
				}
				
				double[][] qq=new double[Common.J][];
				for(int j=0;j<Common.J;j++){
					qq[j]=cplex.getValues(q[j]);
				}
				for(int j=0;j<Common.J;j++){
					for(int k=0;k<Common.K;k++){
						if(qq[j][k]!=0)
							System.out.print(Common.JSTR[j]+" - " +k+" = "+ qq[j][k]+ "\t");
					}
					System.out.print("\n");
				}
			}
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
//	public static void main(String []args){
//		int[][][][] p=new int[Common.M][Common.J][Common.K][Common.A];
//		int[][][][] s=new int[Common.M][Common.J][Common.K][Common.A];
//		int[][][][] dens=new int[Common.M][Common.J][Common.K][Common.A];
//		Random ranP = new Random();
//		Random ranS = new Random();
//		Random ranD = new Random();
//		for(int m=0;m<Common.M;m++){
//			for(int j=0;j<Common.J;j++){
//				for(int k=0;k<Common.K;k++){
//					for(int a=0;a<Common.A;a++){
//						p[m][j][k][a]=ranP.nextInt(100)+200;
//						s[m][j][k][a]=ranS.nextInt(20)+5;
//						dens[m][j][k][a]=1;
//					}
//				}
//			}
//		}
//		boolean[][] aa=new boolean[Common.J][Common.K];
//		for(int j=0;j<Common.J;j++){
//			for(int k=150;k<Common.K;k++){
//				aa[j][k]=true;
//			}
//		}
//		ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
//		ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
//		ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
//		ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
//		ps.add(p);
//		ss.add(s);
//		DT.add(dens);
//		AT.add(aa);
//		CropMasterProblem master=new CropMasterProblem(ps,ss,DT,AT);
//		
//	}
}