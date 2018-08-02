package solver;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.print.attribute.SetOfIntegerSyntax;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

import game.Variable;

public class BDDWrapper {
	
	BDD bdd;
	
	public BDDWrapper(BDD argBDD) {
		bdd=argBDD;
	}

	public BDDWrapper(int numOfVars, int numOfNodes ){
		bdd=new BDD(numOfVars, numOfNodes);
	}
	
	
	public int and(int formula1, int formula2){
		int result = bdd.ref(bdd.and(formula1,formula2));
		return result;
	}
	
	public int andFree(int formula1, int formula2){
		int result = bdd.ref(bdd.and(formula1,formula2));
		bdd.deref(formula1);
		bdd.deref(formula2);
		return result;
	}
	
	public static int andFree(BDD bdd, int formula1, int formula2){
		int result = bdd.ref(bdd.and(formula1,formula2));
		bdd.deref(formula1);
		bdd.deref(formula2);
		return result;
	}
	
	public static int and(BDD bdd, int formula1, int formula2){
		int result = bdd.ref(bdd.and(formula1,formula2));
		return result;
	}
	
	public int andTo(int f1, int f2){
		int result = bdd.andTo(f1, f2);
		return result;
	}
	
	
	public static int andTo(BDD bdd, int f1, int f2){
		int result = bdd.andTo(f1, f2);
		return result;
	}
	
	public int or(int f1, int f2){
		return bdd.ref(bdd.or(f1, f2));
	}
	
	public static int or(BDD bdd, int f1, int f2){
		return bdd.ref(bdd.or(f1, f2));
	}
	
	public int orTo(int f1, int f2){
		return bdd.orTo(f1, f2);
	}
	
	public static int orTo(BDD bdd, int f1, int f2){
		return bdd.orTo(f1, f2);
	}
	
	public int not(int f){
		return bdd.ref(bdd.not(f));
	}
	
	public static int not(BDD bdd, int f){
		return bdd.ref(bdd.not(f));
	}
	
	/**
	 * computes set1\set2
	 * @param set1
	 * @param set2
	 * @return
	 */
	public int diff(int set1, int set2){
		int difference  = bdd.ref(bdd.not(set2));
		difference= bdd.andTo(difference, set1);
		return difference;
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
	
	public int implies(int set1, int set2){
		return bdd.ref(bdd.imp(set1, set2));
	}
	
	public static  int implies(BDD bdd, int set1, int set2){
		return bdd.ref(bdd.imp(set1, set2));
	}
	
	public static int biimp(BDD bdd, int set1 , int set2){
		return bdd.ref(bdd.biimp(set1, set2));
	}
	
	public int biimp(int set1 , int set2){
		return bdd.ref(bdd.biimp(set1, set2));
	}
	
	public int intersect(int set1, int set2){
		int intersection = bdd.ref(bdd.and(set1, set2));
		return intersection;
	}
	
	public static int intersect(BDD bdd, int set1, int set2){
		int intersection = bdd.ref(bdd.and(set1, set2));
		return intersection;
	}
	
	public int union(int set1, int set2){
		int union = bdd.ref(bdd.or(set1, set2));
		return union;
	}
	
	public static int union(BDD bdd, int set1, int set2){
		int union = bdd.ref(bdd.or(set1, set2));
		return union;
	}

	public boolean subset(int set1, int set2){
		int difference=diff(bdd, set1, set2);
		if(difference == 0){
			return true;
		}
		return false;
	}
	
	//checks if set \subseteq union of sets in states
	public static boolean subset(BDD bdd, int set, ArrayList<Integer> setsOfStates){
		int remaining = bdd.ref(set);
		for(int i=0; i<setsOfStates.size();i++){
			int currentSet = setsOfStates.get(i);
			remaining = bdd.andTo(remaining, bdd.not(currentSet));
			if(remaining == 0) return true;
		}
		return false;
	}
	
	
	/**
	 * returns true if set1 \subseteq set2
	 * @param bdd
	 * @param set1
	 * @param set2
	 * @return
	 */
	public static boolean subset(BDD bdd, int set1, int set2){
//		UtilityMethods.debugBDDMethods(bdd, "set1 ", set1);
//		UtilityMethods.debugBDDMethods(bdd, "set2 ", set2);
		
		int difference=diff(bdd, set1, set2);
		
//		UtilityMethods.debugBDDMethods(bdd, "diff is  ", difference);
//		UtilityMethods.getUserInput();
		
		if(difference == 0){
			bdd.deref(difference);
			return true;
		}
		bdd.deref(difference);
		return false;
	}
	
	public boolean equivalent(int set1, int set2){
		
		int eq = bdd.ref(bdd.biimp(set1, set2));
		
		if(eq == 1){
			return true;
		}
		
		return false;
		
//		if(subset(bdd, set1, set2) && subset(bdd, set2, set1)){
//			return true;
//		}
//		return false;
	}
	
	public static boolean equivalent(BDD bdd, int set1, int set2){
		if(subset(bdd, set1, set2) && subset(bdd, set2, set1)){
			return true;
		}
		return false;
	}
	
	public void free(int formula){
		if(formula != -1){
			bdd.deref(formula);
		}
	}
	
	public static void free(BDD bdd, int[] formulas){
		if(formulas == null) return;
		for(int i=0;i<formulas.length;i++){
			bdd.deref(formulas[i]);
		}
	}
	
	public static void free(BDD bdd, int formula){
		if(formula == -1) return;
		bdd.deref(formula);
	}
	
	public static void free(BDD bdd, ArrayList<Integer> formulas){
		if(formulas == null) return;
		for(Integer f : formulas){
			bdd.deref(f);
		}
	}
	
	public static int replace(BDD bdd, int formula, Permutation p){
		return bdd.ref(bdd.replace(formula, p));
	}
	
	public int replace(int formula, Permutation p){
		return bdd.ref(bdd.replace(formula, p));
	}
	
	public static int[] replace(BDD bdd, int[] formulas, Permutation p){
		int[] replacesFormulas=new int[formulas.length];
		for(int i=0;i<formulas.length; i++){
			replacesFormulas[i] = bdd.ref(bdd.replace(formulas[i], p));
		}
		return replacesFormulas;
	}
	
	public static int complement(BDD bdd, int formula){
		int comp=bdd.ref(bdd.not(formula));
		return comp;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BDD bdd = new BDD(10000,1000);
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		
		Variable[] variables = {a,b,c};
		
		int f = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		f=bdd.orTo(f, c.getBDDVar());
		System.out.println(BDDWrapper.BDDtoFormula(bdd, f, variables));
		bdd.printSet(f);
		System.out.println(bdd.nodeCount(f));
		
		System.out.println();
		int g = bdd.ref(bdd.and(b.getBDDVar(), c.getBDDVar()));
		g = bdd.orTo(g, a.getBDDVar());
		System.out.println(BDDWrapper.BDDtoFormula(bdd, g, variables));
		bdd.printSet(g);
		System.out.println(bdd.nodeCount(g));
		
	}
	
	
	
	public static int same(BDD bdd, int[] x1, int[] y1, int[] x2, int[] y2){
		int result = bdd.ref(bdd.getOne());
		int x1eqx2=same(bdd,x1,x2);
		int y1eqy2=same(bdd,y1,y2);
		result=bdd.andTo(result, x1eqx2);
		result=bdd.andTo(result, y1eqy2);
		bdd.deref(x1eqx2);
		bdd.deref(y1eqy2);
		return result;
	}
	
	public static int same(BDD bdd, int[] x, int[] y){
		int T=bdd.ref(bdd.getOne());
		for(int i=0; i<x.length;i++){
			int eq=bdd.ref(bdd.biimp(x[i], y[i]));
			T=bdd.andTo(T, eq);
			bdd.deref(eq);
		}
		return T;
	}
	
	public static int same(BDD bdd, Variable[] x1, Variable[] y1, Variable[] x2, Variable[] y2){
		return same(bdd, Variable.getBDDVars(x1), Variable.getBDDVars(y1), Variable.getBDDVars(x2), Variable.getBDDVars(y2));
	}
	
	public static int same(BDD bdd, Variable[] x, Variable[] y){
		return same(bdd, Variable.getBDDVars(x), Variable.getBDDVars(y));
	}
	
	/**
	 * returns a formula where the variables in vars stay unchanged during a transition
	 * vars <-> vars'
	 * @param bdd
	 * @param vars
	 * @return
	 */
	public static int same(BDD bdd, Variable[] vars){
		return same(bdd, vars, Variable.getPrimedCopy(vars));
	}
	
	/**
	 * Checks if the state represented by assigning the stateString to variables vars belongs to the set of states stateSet
	 * @param bdd
	 * @param stateString
	 * @param vars
	 * @param stateSet
	 * @return true if state belongs to the set, false otherwise
	 */
	public static boolean isMember(BDD bdd, String stateString, Variable[] vars, int stateSet){
		int state = assign(bdd, stateString, vars);
		int intersect = intersect(bdd, state, stateSet);
		bdd.deref(state);
		if(intersect==0){
			bdd.deref(intersect);
			return false;
		}
		bdd.deref(intersect);
		return true;
	}
	
	/**
	 * checks if the state belongs to the set of states stateSet
	 * @param bdd
	 * @param state
	 * @param stateSet
	 * @return
	 */
	public static boolean overlap(BDD bdd, int state, int stateSet){
		int intersect = intersect(bdd, state, stateSet);
		if(intersect==0){
			bdd.deref(intersect);
			return false;
		}
		bdd.deref(intersect);
		return true;
	}
	
	public static void cleanup(BDD bdd){
		bdd.cleanup();
	}
	
	public static ArrayList<String> mintermsWithDontCares(BDD bdd, int formula, Variable[] variables){
		String[] sat = parsePrintSetOutput(bdd, formula);
		if(sat!=null){
			if(sat[0].equals("TRUE")){
				//sat[0] = dontCares(variables.length);
				ArrayList<String> result = new ArrayList<String>();
				result.add("TRUE");
			}else{
				sat=keepRelevantVariables(sat, variables);
			}
			ArrayList<String> minterms=new ArrayList<String>();
			for(int i=0;i<sat.length; i++){
				minterms.add(sat[i]);
			}
			return minterms;
		}
		return null;
	}
	
	public static ArrayList<String> minterms(BDD bdd, int formula, Variable[] variables){
		String[] sat = parsePrintSetOutput(bdd, formula);
		if(sat!=null){
			if(sat[0].equals("TRUE")){
				sat[0] = dontCares(variables.length);
			}else{
				sat=keepRelevantVariables(sat, variables);
			}
			ArrayList<String> minterms=extractMinterms(sat);
			return minterms;
		}
		return null;
	}
	
	public static String dontCares(int length){
		String result="";
		for(int i=0;i<length;i++){
			result+="-";
		}
		return result;
	}
	
	/**
	 * Enumerates the set of states satisfying the given formula
	 * @param bdd
	 * @param formula
	 * @param vars
	 * @return
	 */
	public static int[] enumerate(BDD bdd, int formula, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula, vars);
		
		//the set of abstract states is empty
		if(minterms == null){
			return null;
		}
		
		int[] states=new int[minterms.size()];
		for(int i=0;i<minterms.size();i++){
			states[i]=BDDWrapper.assign(bdd, minterms.get(i), vars);
		}
		return states;
	}
	
	
//	public static String BDDtoFormula(BDD bdd, int formula){
//		String[] satisfyingAssignments = parsePrintSetOutput(bdd, formula);
//		if(satisfyingAssignments!=null){
//			if(satisfyingAssignments[0].equals("TRUE")){
//				return "TRUE";
//			}
//			
//		}
//		return "FALSE";
//	}
	
	/**
	 * Given a formula and the relevant variables, computes the minterms and then returns the formula as 
	 * a string in disjunctive form
	 * @param bdd
	 * @param formula
	 * @param variables
	 * @return
	 */
	public static String BDDtoFormula(BDD bdd, int formula, Variable[] variables){
		ArrayList<String> minterms = mintermsWithDontCares(bdd, formula, variables);
		if(minterms == null) return "FALSE";
		if(minterms.get(0).equals("TRUE")) return "TRUE";
		return mintermsToFormulas(bdd, minterms, variables);
	}
	
	//TODO: or operator
	public static String mintermsToFormulas(BDD bdd, ArrayList<String> minterms, Variable[] variables){
		String formula="";
		for(int i=0;i<minterms.size();i++){
			String minterm=minterms.get(i);
			formula+=mintermToFormula(bdd, minterm, variables);
			if(i<minterms.size()-1){
				formula+=" || ";
			}
		}
		return formula;
	}
	
	//TODO: complement operator
	public static String mintermToFormula(BDD bdd, String minterm, Variable[] variables){
		
		String formula="(";
		boolean before = false;
		for(int i=0;i<minterm.length();i++){
			if(minterm.charAt(i)=='0'){
				String part ="!"+variables[i].getName();
				if(before) formula+=" & ";
				else before = true;
				formula+=part;
			}else if(minterm.charAt(i)=='1'){
				String part =variables[i].getName();
				if(before) formula+=" & ";
				else before = true;
				formula+=part;
			}
			
			
		}
		formula+=")";
		return formula;
	}
	
	public static String[] keepRelevantVariables(String[] input, Variable[] variables){
		
//		System.out.println("keep relevant variables");
//		System.out.println("the set of variables");
//		for(Variable v : variables){
//			System.out.println(v.getName()+ " at position "+v.getBDDPosition());
//		}
		
		String result[]=new String[input.length];
		for(int j=0;j<input.length;j++){
			String str=input[j];
//			System.out.println("minterm is "+str);
			if(str.equals("FALSE")){
				result[j]="FALSE";
				continue;
			}
			result[j]=new String();
			for(int i=0;i<variables.length;i++){
				result[j]+=str.charAt(variables[i].getBDDPosition());
			}
		}
		return result;
	}
	
	public static String keepRelevantVariables(String input, Variable[] variables){

		if(input.equals("FALSE")){
			return "FALSE";
		}
		
		String result="";
		for(int i=0;i<variables.length;i++){
			result+=input.charAt(variables[i].getBDDPosition());
		}
		
		return result;
	}
	
	/**
	 * if the given minterm respects the order of variables in allVars, the method computes and returns 
	 * the value of the relatedVars
	 * @param minterm
	 * @param allVars
	 * @param relatedvars
	 * @return
	 */
	public static String keepRelevantVariables(String minterm, Variable[] allVars, Variable[] relatedVars){
		String result="";
		for(int i=0;i<relatedVars.length;i++){
			for(int j=0;j<minterm.length();j++){
				if(allVars[j].getBDDPosition() == relatedVars[i].getBDDPosition()){
					result+=minterm.charAt(j);
				}
			}
		}
		return result;
	}
	
	public static int mintermToBDDFormula(BDD bdd, String minterm, Variable[] vars){
		if(minterm.contains("-")){
			System.err.println("something is wrong in mintermToBDDFormula");
			return -1;
		}
		
		int result=bdd.ref(bdd.getOne());
		for(int i=0;i<minterm.length();i++){
			if(minterm.charAt(i)=='0'){
				result=bdd.andTo(result, bdd.not(vars[i].getBDDVar()));
			}else{
				result = bdd.andTo(result, vars[i].getBDDVar());
			}
		}
		return result;
	}
	
	public static ArrayList<String> minterms(BDD bdd, int formula, Variable[] variables, Variable[] primedVariables, Variable[] actionVars){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    PrintStream ps = new PrintStream(baos);
			PrintStream old = System.out;
			System.setOut(ps);
			bdd.printSet(formula);
			System.out.flush();
		    System.setOut(old);
		    String satisfying=baos.toString();
		    String[] satisfyingAssinments=satisfying.split("\n");
		    satisfyingAssinments=keepRelevantVariables(satisfyingAssinments, variables, primedVariables, actionVars);
		    ArrayList<String> minterms = extractMinterms(satisfyingAssinments);
		    baos.close();
		    ps.close();
		    return minterms;
		}catch(Exception e){
			System.err.println("Error parsing the transition relation");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * takes as input an array of strings, each representing a minterm for the current variables in the BDD
	 * and returns minterms as (variables,primedVariables,actionVars)
	 * @param input
	 * @param variables
	 * @param primedVariables
	 * @param actionVars
	 * @return
	 */
	private static String[] keepRelevantVariables(String[] input, Variable[] variables, Variable[] primedVariables, Variable[] actionVars){
		String result[]=new String[input.length];
		for(int j=0;j<input.length;j++){
			String str=input[j];
			if(str.equals("FALSE")){
				result[j]="FALSE";
				continue;
			}
			result[j]=new String();
			for(int i=0;i<variables.length;i++){
				result[j]+=str.charAt(variables[i].getBDDPosition());
			}
			for(int i=0; i<primedVariables.length;i++){
				result[j]+=str.charAt(primedVariables[i].getBDDPosition());
			}
			for(int i=0;i<actionVars.length;i++){
				result[j]+=str.charAt(actionVars[i].getBDDPosition());
			}
		}
		return result;
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
	
	
	public static boolean[] bddPrintSetMintermToBooleanArray(BDD bdd, int formula){
		String[] minterms = parsePrintSetOutput(bdd, formula);
		if(minterms.length>1){
			System.out.println("there are more than one satsifying minterm for the formula");
			bdd.printSet(formula);
		}
		String firstMintrem=minterms[0];
		return bddPrintSetMintermToBooleanArray(firstMintrem);
	}
	
	public static boolean[] bddPrintSetMintermToBooleanArray(String minterm){
		String mintermString=minterm.replaceAll("[-]", "");
		boolean[] result= new boolean[mintermString.length()];
		for(int i=0; i<mintermString.length();i++){
			if(mintermString.charAt(i)=='0'){
				result[i]=false;
			}else{
				result[i]=true;
			}
		}
		return result;
	}
	
	public static String[] parsePrintSetOutput(BDD bdd, int formula){
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
		    
		    if(satisfyingAssinmnets[0].equals("FALSE")){
		    	return null;
		    }
		    
		    baos.close();
		    ps.close();
		    return satisfyingAssinmnets;
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
	
	/**
	 * assigns the value of index to the variables vars and returns the corresponding bdd formula 
	 * @param index
	 * @param vars
	 * @return
	 */
	public static int assign(BDD bdd, int index, Variable[] variables){
		int abstractState=bdd.ref(bdd.getOne());
		String binary = Integer.toBinaryString(index);
		//append zeros in front of the binary if the length of binary and abstractVariables are not the same
		//TODO: check everywhere that when you append zeros to binary strings, you don't have binary.length() in the loop condition as it changes dynamically
		int binaryLength=binary.length();
		for(int i=0;i<(variables.length-binaryLength);i++){
			binary="0"+binary;
		}
		
//		String reverse = "";
//		for(int i=binary.length()-1; i>=0; i--){
//			reverse+=binary.charAt(i);
//		}
//		binary = reverse;
		
		for(int j=0;j<binary.length();j++){
			if(binary.charAt(j)=='0'){
				abstractState=bdd.andTo(abstractState, bdd.not(variables[j].getBDDVar()));
			}else{
				abstractState=bdd.andTo(abstractState, variables[j].getBDDVar());
			}
		}
		return abstractState;
	}
	
//	/**
//	 * Assigns the value to the variables and if the value if 
//	 * @param bdd
//	 * @param value
//	 * @param variables
//	 * @return
//	 */
//	public static int assignAndExpand(BDD bdd, int value, Variable[] variables){
//		
//	}
	
	public static int assign(BDD bdd, int[] values, Variable[] variables){
		int result=bdd.ref(bdd.getZero());
		for(int i=0;i<values.length;i++){
			int tmp = assign(bdd, values[i], variables);
			result=bdd.orTo(result, tmp);
			bdd.deref(tmp);
		}
		return result;
	}
	
	public static int assign(BDD bdd, String binaryString, Variable[] variables){
		int state=bdd.ref(bdd.getOne());
		//TODO: check that binaryString is indeed a binary String
		//copying the binaryString into a new String binary
		String binary = binaryString.substring(0);
		int binaryLength=binary.length();
		for(int i=0;i<(variables.length-binaryLength);i++){
			binary="0"+binary;
		}
		for(int j=0;j<binary.length();j++){
			if(binary.charAt(j)=='0'){
				state=bdd.andTo(state, bdd.not(variables[j].getBDDVar()));
			}else{
				state=bdd.andTo(state, variables[j].getBDDVar());
			}
		}
		return state;
	}
	
	public static int assign(BDD bdd, boolean[] value, Variable[] variables){
		if(value.length != variables.length){
			System.err.println("the length of vectors are not the same");
			return -1;
		}
		String binaryString = UtilityMethods.booleanArrayToString(value);
		return assign(bdd, binaryString,variables);
	}
	
	public static int assign(BDD bdd, char value, Variable var){
		if(value != '0' && value != '1'){
			System.err.println("The value assigned to a boolean variable must be either 0 or 1!");
			return -1;
		}
		int result=-1;
		if(value == '0'){
			result=bdd.ref(bdd.not(var.getBDDVar()));
		}else{
			result=bdd.ref(var.getBDDVar());
		}
		return result;
	}
	
	public int assign(char value, Variable var){
		return assign(bdd, value, var);
	}
	
	public static int[] getEquivalenceClassesFromPredicates(BDD bdd, int[] predicates){
		int[] equivalenceClasses  = new int[(int) Math.pow(2, predicates.length)];
		boolean[] valuation=new boolean[predicates.length];
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, 0, predicates);
		return equivalenceClasses;
	}
	
	private static void equivalenceClassesFromPredicates(BDD bdd, int[] equivalenceClasses, boolean[] valuation, int index,  int[] predicates){
		if(index==predicates.length){
			int eqClassIndex=UtilityMethods.booleanArrayToInteger(valuation);
			equivalenceClasses[eqClassIndex]=bdd.ref(bdd.getOne());
			for(int i=0; i<valuation.length;i++){
				int predicateValue = valuation[i]?predicates[i]:bdd.not(predicates[i]);
				equivalenceClasses[eqClassIndex]=bdd.andTo(equivalenceClasses[eqClassIndex], predicateValue);
			}
			return;
		}
		valuation[index]=false;
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, index+1, predicates);
		valuation[index]=true;
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, index+1, predicates);
	}
	
	public static void BDD_Usage(BDD bdd){
		
		System.out.println("BDD usage");
		
		System.out.println("Num of variables in BDD: "+bdd.numberOfVariables() );
		
		System.out.println("BDD memory usage: "+((double)bdd.getMemoryUsage()/1048576) + " MB");
	}
	
	public static int numOfVariablesInBDD(BDD bdd){
		return bdd.numberOfVariables();
	}
	
	public static String BDD_memory_usage(BDD bdd){
		return ((double)bdd.getMemoryUsage()/1048576) + " MB";
	}
	
	public void BDD_Usage(){
		
		System.out.println("BDD usage");
		
		System.out.println("Num of variables in BDD: "+bdd.numberOfVariables() );
		
		System.out.println("BDD memory usage: "+((double)bdd.getMemoryUsage()/1048576) + " MB");
	}
	
	/**
	 * Computes the existential preImage of the set of states according the transition relation trans
	 * @param set
	 * @param cube
	 * @param trans
	 * @param vTovPrime
	 * @return
	 */
	public int EpreImage(int set, int primeVarsCube, int trans, Permutation vTovPrime){
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
		int temp = bdd.ref(bdd.and(targetSetPrime, trans));
		int preImage=bdd.ref(bdd.exists(temp, primeVarsCube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		return preImage;
	}
	
	public static int EpreImage(BDD bdd, int set, int primeVarsCube, int trans, Permutation vTovPrime){
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
		int temp = bdd.ref(bdd.and(targetSetPrime, trans));
		int preImage=bdd.ref(bdd.exists(temp, primeVarsCube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		return preImage;
	}
	
	public static int EpostImage(BDD bdd, int set, int variablesCube, int trans, Permutation vPrimeToV){
		int temp = bdd.ref(bdd.and(set, trans));
		int tempImage=bdd.ref(bdd.exists(temp, variablesCube));
		bdd.deref(temp);
				
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeToV));
		bdd.deref(tempImage);
				
		return postImage;
	}
	
//	public static int ApostImage_EpostImageBased(BDD bdd, int preSet, int nextSet, int variablesCube, int trans, Permutation vPrimeToV){
//		
//	}
	
	/**
	 * Computes the universal preImage of the set of states according the transition relation trans
	 * @param set
	 * @param cube
	 * @param trans
	 * @param vTovPrime
	 * @return
	 */
	public int ApreImage(int set, int cube, int trans, Permutation vTovPrime){
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
		int temp = bdd.ref(bdd.imp(trans, targetSetPrime));
		int aPreImage = bdd.ref(bdd.forall(temp, cube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		return aPreImage;
	}
	
	public static int ApreImage(BDD bdd, int set, int cube, int trans, Permutation vTovPrime){
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
		int temp = bdd.ref(bdd.imp(trans, targetSetPrime));
		int aPreImage = bdd.ref(bdd.forall(temp, cube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		return aPreImage;
	}
	
	/**
	 * Computes ApreImage using Epre
	 * @param set
	 * @param cube
	 * @param trans
	 * @param vTovPrime
	 * @return
	 */
	public int ApreImage_EPreBased(int set, int cube, int trans, Permutation vTovPrime){
		int statesWithTransitionToSet = EpreImage(set, cube, trans, vTovPrime);		
		int temp = bdd.ref(bdd.not(set));
		int statesWithTransitionsToComplementSet= EpreImage(temp, cube, trans, vTovPrime);
		bdd.deref(temp);
		int diff= diff(statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		return diff;
	}
	
	public static int ApreImage_EPreBased(BDD bdd, int set, int cube, int trans, Permutation vTovPrime){
//		UtilityMethods.debugBDDMethods(bdd, "inside APre, current set is", set);
//		UtilityMethods.getUserInput();
		
		int statesWithTransitionToSet = EpreImage(bdd, set, cube, trans, vTovPrime);		
		
//		UtilityMethods.debugBDDMethods(bdd, "inside APre, fisrt epre is", statesWithTransitionToSet);
//		UtilityMethods.getUserInput();
		
		int temp = bdd.ref(bdd.not(set));
		
//		UtilityMethods.debugBDDMethods(bdd, "inside APre, complement set is", temp);
//		UtilityMethods.getUserInput();
		
		int statesWithTransitionsToComplementSet= EpreImage(bdd, temp, cube, trans, vTovPrime);
		
//		UtilityMethods.debugBDDMethods(bdd, "inside APre, second epre is", statesWithTransitionsToComplementSet);
//		UtilityMethods.getUserInput();
		
		bdd.deref(temp);
		int diff= diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		
//		UtilityMethods.debugBDDMethods(bdd, "inside APre, diff is", diff);
//		UtilityMethods.getUserInput();
		
		return diff;
	}
	
	/**
	 * Computes the existential post image of a set of states "set"
	 * @param set
	 * @param cube
	 * @param trans
	 * @param vPrimeTov
	 * @return
	 */
	public int EpostImage(int set, int cube, int trans, Permutation vPrimeTov){
		int temp = bdd.ref(bdd.and(set, trans));
		int tempImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);	
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeTov));
		bdd.deref(tempImage);
		return postImage;
	}
	
	
	public static int computeDisjunction(BDD bdd, ArrayList<Integer> transitionList){
		int T=bdd.ref(bdd.getZero());
		for(Integer trans : transitionList){
			T=bdd.orTo(T, trans);
		}
		return T;
	}

	public static ArrayList<Integer> copyWithReference(BDD bdd, ArrayList<Integer> formulas){
		if(formulas == null){
			return null;
		}
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(Integer f : formulas){
			result.add(bdd.ref(f));
		}
		return result;
	}
	
	public static int[] copyWithReference(BDD bdd, int[] formulas){
		if(formulas == null){
			return null;
		}
		int[] result = new int[formulas.length];
		for(int i=0;i<formulas.length;i++){
			result[i] = bdd.ref(formulas[i]);
		}
		return result;
	}
	
	public static int createCube(BDD bdd, Variable[] vars){
		int cube = bdd.ref(bdd.getOne());
		for(Variable var : vars){
			cube = bdd.andTo(cube, var.getBDDVar());
		}
		return cube;
	}
	
	public int createCube(Variable[] vars){
		int cube = bdd.ref(bdd.getOne());
		for(Variable var : vars){
			cube = bdd.andTo(cube, var.getBDDVar());
		}
		return cube;
	}
	
	public static Permutation createPermutations(BDD bdd, Variable[] vars, Variable[] primeVars){
		return bdd.createPermutation(Variable.getBDDVars(vars), Variable.getBDDVars(primeVars));
		
	}
	
	public static int oneSatisfyingAssignment(BDD bdd, int formula){
		return bdd.ref(bdd.oneSat(formula));
	}
	
	public int oneSatisfyingAssignment(int formula){
		return bdd.ref(bdd.oneSat(formula));
	}
	
	public static int oneSatisfyingAssignment(BDD bdd, int formula, Variable[] vars){
		int minterm = bdd.ref(bdd.oneSat(formula));
		ArrayList<String> minterms = minterms(bdd, minterm, vars);
		int result = mintermToBDDFormula(bdd, minterms.get(0), vars);
		bdd.deref(minterm);
		return result;
	}
	
	public static int exists(BDD bdd, int formula, int cube){
		return bdd.ref(bdd.exists(formula, cube));
	}
	
	public int exists(int formula, int cube){
		return bdd.ref(bdd.exists(formula, cube));
	}
	
	public static int restrict(BDD bdd, int formula, int varsValues, int cube){
		int restriction = BDDWrapper.and(bdd, formula, varsValues);
		int result = BDDWrapper.exists(bdd, restriction, cube);
		BDDWrapper.free(bdd, restriction);
		return result;
	}
	
	/**
	 * returns a minterm satisfying the formula specified as states chosen randomly among all satisfying minterms
	 * @param bdd
	 * @param states
	 * @param vars
	 * @return
	 */
	public static int oneRandomMinterm(BDD bdd, int states, Variable[] vars ){
		String randomMintermString = oneRandomMintermString(bdd, states, vars);
		if(randomMintermString == null){
			System.err.println("The formula has no satisfying assignment!");
			return -1;
		}
		int randomMinterm = BDDWrapper.mintermToBDDFormula(bdd, randomMintermString, vars);
		return randomMinterm;
	}
	
	public static String oneRandomMintermString(BDD bdd, int states, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, vars);
		if(minterms==null){
			System.err.println("The formula has no satisfying assignment!");
			return null;
		}
		int randomIndex = UtilityMethods.randomIndex(minterms.size());
		return minterms.get(randomIndex);
	}
	
	public int getTrue(){
		return bdd.ref(bdd.getOne());
	}
	
	public int getFalse(){
		return bdd.ref(bdd.getZero());
	}
	
	public BDD getBdd(){
		return bdd;
	}
}
