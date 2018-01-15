package com.simple;

public class test {
	public static void main(String arg[]){
		System.out.println("123");
		String st="12.0a";
		try{
			int a=Integer.parseInt(st);
			System.out.println(a);
		}catch(NumberFormatException e){
			try{
				double a=Double.parseDouble(st);
				System.out.println(a);
			}catch(NumberFormatException ee){
				
				System.out.println(st);
			}
			
		}
		
	}
}
