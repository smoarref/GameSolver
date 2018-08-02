package symbolic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import jdd.bdd.BDD;

public class BDDWrapper {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Computes set1\set2, i.e., the difference between two sets 
	 * @param bdd: the bdd object
	 * @param set1: the first set
	 * @param set2: the second set
	 * @return a bdd representing the difference 
	 */
	public static int diff(BDD bdd , int set1, int set2){
		int difference  = bdd.ref(bdd.not(set2));
		difference= bdd.andTo(difference, set1);
		return difference;
	}
	
	public static int intersect(BDD bdd, int set1, int set2){
		int intersection = bdd.ref(bdd.and(set1, set2));
		return intersection;
	}
	
	/**
	 * returns true if set1 \subseteq set2
	 * @param bdd
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static boolean subset(BDD bdd, int set1, int set2){
		int difference=diff(bdd, set1, set2);
		if(difference == 0){
			return true;
		}
		return false;
	}
	
	public static boolean equivalent(BDD bdd, int set1, int set2){
		if(subset(bdd, set1, set2) && subset(bdd, set2, set1)){
			return true;
		}
		return false;
	}
	
	
	
	public static void cleanup(BDD bdd){
		bdd.cleanup();
	}
	
	
	public static ArrayList<String> minterms(BDD bdd, int formula){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    PrintStream ps = new PrintStream(baos);
			PrintStream old = System.out;
			System.setOut(ps);
			bdd.printSet(formula);
			System.out.flush();
		    System.setOut(old);
		    String satisfying=baos.toString();
		    String[] satisfyingAssinmnets=satisfying.split("\n");
		    ArrayList<String> minterms = extractMinterms(satisfyingAssinmnets);
		    baos.close();
		    ps.close();
		    return minterms;
		}catch(Exception e){
			System.err.println("Error parsing the transition relation");
			e.printStackTrace();
		}
		return null;
	}
	
	public static ArrayList<String> extractMinterms(String[] input){
		ArrayList<String> result=new ArrayList<String>();
		for(String s : input){
			result.addAll(extractMinterms(s));
		}
		return result;
	}
	
	//Given a string as a combination of 1,0, and - characters, returns all possible string where - characters 
	//are replaced with 0 or 1
	private static ArrayList<String> extractMinterms(String input){
		//System.out.println("extracting minterms of "+input);
		ArrayList<String> result=new ArrayList<String>();
		return extractMintermsRec(input, result);
	}
	
	private static ArrayList<String> extractMintermsRec(String input, ArrayList<String> result){
		int index=input.indexOf('-');
		if(index==-1){
			//System.out.println("there is no -, input is "+input);
			result.add(input);
			return result;
		}
		String min1=input.substring(0, index)+"0"+input.substring(index+1, input.length());
		//System.out.println("min1 is "+min1);
		extractMintermsRec(min1, result);
		String min2=input.substring(0, index)+"1"+input.substring(index+1, input.length());
		extractMintermsRec(min2, result);
		//System.out.println("min2 is "+min2);
		return result;
	}

}
