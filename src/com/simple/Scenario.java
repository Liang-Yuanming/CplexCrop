package com.simple;



public class Scenario {
	private int supply[][][]; //供應
	private int price[][][]; //價格
	private int dens[][][]; //
	private boolean arrival[][];
	public Scenario(){
		price=new int[Common.M][Common.J][Common.K];
		dens=new int[Common.M][Common.J][Common.K];
		supply=new int[Common.M][Common.J][Common.K];
		arrival=new boolean[Common.J][Common.K];
	}
	public int[][][] getSupply() {
		return supply;
	}
	public void setSupply(int m,int j,int k,int value){
		this.supply[m][j][k]=value;
	}
	public void setSupply(int[][][] supply) {
		this.supply = supply;
	}
	public int[][][] getPrice() {
		return price;
	}
	public void setPrice(int m,int j,int k,int value){
		this.price[m][j][k]=value;
	}
	public void setPrice(int[][][] price) {
		this.price = price;
	}
	
	public int[][][] getDens() {
		return dens;
	}
	public void setDens(int m,int j,int k,int value){
		this.dens[m][j][k]=value;
	}
	public void setDens(int[][][] dens) {
		this.dens = dens;
	}
	public boolean[][] getArrival() {
		return arrival;
	}
	public void setArrival(int j,int k,boolean value) {
		this.arrival[j][k] = value;
	}
	public void setArrival(boolean[][] arrival) {
		this.arrival = arrival;
	}
}
