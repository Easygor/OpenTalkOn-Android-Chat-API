package com.thinkspace.opentalkon.data;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;

public abstract class TAMultiData {
	public abstract Map<String,Object> getListOfItem();
	public String boundaryValue;
	
	public String generateBoundaryValue(){
		Random rand = new Random();
		rand.setSeed(System.currentTimeMillis());
		BigInteger val = new BigInteger(String.valueOf(rand.nextLong()));
		BigInteger val2 = new BigInteger(String.valueOf(rand.nextLong()));
		
		return boundaryValue = val.multiply(val2).toString();
	}

	public String getBoundaryValue() {
		return boundaryValue;
	}
}
