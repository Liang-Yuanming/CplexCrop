package com.crop.v2;

import com.model.Scenario;

import ilog.concert.IloLinearNumExpr;

public class Statistics {
	public void run(int[] v,int[][] h,int s[][][][],Scenario[] scenario,int i){
		//統計種子成本
		int costBulb=0;
		for(int j=0;j<Common.J;j++){
			costBulb+=Common.c[j]*v[j];
		}
		//統計人力成本
		int costLabor=0;
		for(int k=0;k<Common.K;k++){
			costLabor+=5*Common.CostHire;
		}
		
		int costTranspor=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(s[m][j][k][a]!=0 && scenario[i].getDens()[m][j][k][a]!=0){
							costTranspor+=Math.floor(s[m][j][k][a]/scenario[i].getDens()[m][j][k][a])*Common.CTFA[m];
						}
					}
				}
			}
		}
		int costTotal=0;
		costTotal=costBulb+costLabor+Common.CostFix+costTranspor;
		int profit=0;
		int p[][][][]=scenario[i].getPrice();
		int tempProfit=0;
		for(int m=0;m<Common.M;m++){
			for(int j=0;j<Common.J;j++){
				for(int k=0;k<Common.K;k++){
					for(int a=0;a<Common.A;a++){
						if(p[m][j][k][a]!=0)
							tempProfit+=p[m][j][k][a]*s[m][j][k][a];
						else if(s[m][j][k][a]!=0)
							tempProfit+=58*s[m][j][k][a];
					}
				}
			}
		}
		
		System.out.println(i+" Profit="+(tempProfit-costTotal));
//		System.out.println(i+" income="+(tempProfit));
//		System.out.println(i+" cost="+(costTotal));
		
	}
}
