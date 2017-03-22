package com.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	private IloNumVar eta;
	//常數
	//成本常數
	private int c[]={2,1,3,4,5}; // 第j品種種子成本
	private int E[]={2,2,2,2,2,2,2,2,2,2}; // 代表第k日雇用多少人
	private int CostHire=1000; // 代表每日單位成本
	private int costTransportation[]; //運輸成本 在y情境下
	private int CTFA[]={4,5,5,5,6};
	private int CostFix=1000; //固定成本
	//到貨日常數
	private int MM=10000000;//輔助常數
	//每人一天平均種植顆數
	private int B=1500;
	//種子成熟期間
	private static int d[]={1,2,1,1,1}; //第j品種所需收成天數
	private static double Y[]={1,1,1,1,1}; //j品種收成比率
	//土地常數
	private IloNumExpr L[]; //第k日土地種植量
	private int Lmax=800000;//最大限制
	//品質常數
	private static double YA[][]={{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,1,1,1},{1,1,1,1}}; //第j品種在a等級下之比率
	//索引參數
	private static int M=5; //市場：台北、台中、彰化、台南、高雄
	private static int K=10; //日期： 1~10日
	private static int J=5; //百合花品種：1~5種
	private static int A=4; //百合花等級： 1~4
	//成本函數
	private IloNumExpr costBulb;
	private IloNumExpr[] costTotal;
	
	public CropMasterProblem(ArrayList<int[][][][]> p,ArrayList<int[][][][]> s,ArrayList<int[][][][]> dens,ArrayList<boolean[][]> arrival){
		this.pricesSenario=p;//價格
		this.supplySenario=s; //供給
		this.DENS=dens; //每箱分裝量
		this.costTotal=new IloNumExpr[p.size()]; // 每個情境下總成本 初始化
		this.costTransportation=new int[p.size()]; //每個情境下總運輸成本 初始化
		this.arrival=arrival; //到達情境
		this.L=new IloNumExpr[K];
		
		createModel();
	}
	public void createModel(){
		try{
			cplex=new IloCplex();
			//初始化決策變數
			q=new IloNumVar[J][K];
			for(int j=0;j<J;j++){
				for(int k=0;k<K;k++){
					q[j][k]=cplex.intVar(0, Integer.MAX_VALUE);
				}
			}
			v=new IloNumVar[J];
			for(int j=0;j<J;j++){
				v[j]=cplex.intVar(0, Integer.MAX_VALUE);
			}
			h=new IloNumVar[J][K];
			for(int j=0;j<J;j++){
				for(int k=0;k<K;k++){
					h[j][k]=cplex.intVar(0, Integer.MAX_VALUE);
				}
			}
			eta=cplex.intVar(0, Integer.MAX_VALUE);
			
			obj=cplex.addMaximize(eta);
			
			

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
			//利潤限制式
			for(int i=0;i<this.pricesSenario.size();i++){
				int p[][][][]=this.pricesSenario.get(i);
				int s[][][][]=this.supplySenario.get(i);
				IloNumExpr tempProfit=cplex.numExpr();
				for(int m=0;m<M;m++){
					for(int j=0;j<J;j++){
						for(int k=0;k<K;k++){
							for(int a=0;a<A;a++){
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
			for(int j=0;j<J;j++){
				IloLinearNumExpr qexpr=cplex.linearNumExpr();
				for(int k=0;k<K;k++){
					qexpr.addTerm(1.0, q[j][k]);
				}
				cplex.addLe(qexpr, v[j]);
			}
			//到貨日限制
			for(int i=0;i<arrival.size();i++){
				boolean [][]tempArrival=this.arrival.get(i);
				for(int j=0;j<J;j++){
					for(int k=0;k<K;k++){
						if(tempArrival[j][k]){
							cplex.addLe(q[j][k],M);
						}else{
							cplex.addLe(q[j][k],0);
						}
					}
				}
			}
			//人力限制
			for(int j=0;j<J;j++){
				for(int k=0;k<K;k++){
					cplex.addLe(q[j][k], E[k]*B);
				}
			}
			//收割限制式
			for(int j=0;j<J;j++){
				for(int k=0;k<K;k++){
					IloLinearNumExpr hexpr=cplex.linearNumExpr();
					hexpr.addTerm(q[j][k], Y[j]);
					if((k+d[j])<9){
						cplex.addEq(h[j][k+d[j]], hexpr);
					}
					
				}
			}
			//土地限制式
			for(int k=0;k<K;k++){
				IloNumExpr havestExpr=cplex.numExpr();
				for(int j=0;j<J;j++){
					if(k==0){
						havestExpr=cplex.sum(havestExpr,cplex.diff(q[j][k], h[j][k]));
						L[k]=havestExpr;
					}else{
						L[k]=cplex.sum(havestExpr,L[k-1]);
					}
				}
				cplex.addLe(L[k], Lmax);
			}
			//供應限制式
			for(int i=0;i<this.supplySenario.size();i++){
				int[][][][] s=this.supplySenario.get(i);
				for(int j=0;j<J;j++){
					for(int k=0;k<K;k++){
						for(int a=0;a<A;a++){
							int sumSupply=0;
							for(int m=0;m<M;m++){
								sumSupply+=s[m][j][k][a];
							}
							
							cplex.addLe(sumSupply,cplex.prod(YA[j][a], cplex.prod(1/5, h[j][k])));
						}
					}
				}
			}
			//cplex.exportModel("a.pl");
			if(cplex.solve()){
				
				int ee=(int) cplex.getValue(eta);
				System.out.println("eta = " + ee);
				double[] vv=cplex.getValues(v);
				for(int j=0;j<J;j++){
					System.out.print(j+" = "+ vv[j]+ "\t");
				}
				System.out.print("\n");
				
				double[][] qq=new double[J][];
				for(int j=0;j<J;j++){
					qq[j]=cplex.getValues(q[j]);
				}
				for(int j=0;j<J;j++){
					for(int k=0;k<K;k++){
						System.out.print(j+" - " +k+" = "+ qq[j][k]+ "\t");
					}
					System.out.print("\n");
				}
			}
		}catch(IloException e){
			System.err.println("Concert exception caught: " + e);
		}
	}
	public static void main(String []args){
		int[][][][] p=new int[M][J][K][A];
		int[][][][] s=new int[M][J][K][A];
		int[][][][] dens=new int[M][J][K][A];
		Random ranP = new Random();
		Random ranS = new Random();
		Random ranD = new Random();
		for(int m=0;m<M;m++){
			for(int j=0;j<J;j++){
				for(int k=0;k<K;k++){
					for(int a=0;a<A;a++){
						p[m][j][k][a]=ranP.nextInt(400)+300;
						s[m][j][k][a]=ranS.nextInt(200)+100;
						dens[m][j][k][a]=1;
					}
				}
			}
		}
		boolean[][] aa=new boolean[J][K];
		for(int j=0;j<J;j++){
			for(int k=0;k<K;k++){
				aa[j][k]=true;
			}
		}
		ArrayList<int[][][][]> ps=new ArrayList<int[][][][]>(); // 價格情境 
		ArrayList<int[][][][]> ss=new ArrayList<int[][][][]>(); //供應量
		ArrayList<int[][][][]> DT=new ArrayList<int[][][][]>();
		ArrayList<boolean[][]> AT=new ArrayList<boolean[][]>(); //到貨日 0 or 1
		ps.add(p);
		ss.add(s);
		DT.add(dens);
		AT.add(aa);
		CropMasterProblem master=new CropMasterProblem(ps,ss,DT,AT);
		
	}
}