package com.semantix.util;

public class CommonMethods {
	public static boolean isNullOrEmpty(String s){
		if(s == null || s.equalsIgnoreCase(""))
			return true;
		else return false;
	}
	
}
