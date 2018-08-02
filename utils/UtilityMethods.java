package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import specification.Agent;
import specification.Agent2D;
import specification.GridCell2D;
import game.BDDWrapper;
import game.GameStructure;
import game.Variable;
import jdd.bdd.BDD;

public class UtilityMethods {

	private static final long MEGABYTE = 1024L * 1024L;
	private static final int durationThreshold = 500;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		String[] binaries  = enumerate(3);
//		for(String str : binaries){
//			System.out.println(str);
//		}
		
		BDD bdd = new BDD(10000,1000);
//		int a = bdd.createVar();
//		int b = bdd. createVar();
//		int c= bdd.createVar();
//		
//		int f=bdd.ref(bdd.and(a, b));
//		bdd.printSet(f);
//		
//		int g = bdd.ref(bdd.and(a, c));
//		bdd.printSet(g);
//		
////		int[] predicates = {f,g};
////		
////		int[] eqClasses = UtilityMethods.enumerate(bdd, predicates);
////		
////		for(int i=0; i<eqClasses.length;i++){
////			bdd.printSet(eqClasses[i]);
////		}
//		
//		int h = bdd.ref(bdd.or(f, c));
//		bdd.printSet(h);
		
		//testing random minterm
//		Variable a= Variable.createVariableAndPrimedVariable(bdd, "a");
//		Variable b = Variable.createVariableAndPrimedVariable(bdd, "b");
//		Variable c = Variable.createVariableAndPrimedVariable(bdd, "c");
//		Variable[] vars = new Variable[]{a,b,c};
//		
//		int h = BDDWrapper.and(bdd, a.getBDDVar(), b.getBDDVar());
//		h = BDDWrapper.orTo(bdd, h, c.getBDDVar());
//		UtilityMethods.debugBDDMethods(bdd, "h=", h);
//		
//		int minterm1 = BDDWrapper.oneRandomMinterm(bdd, h, vars);
//		UtilityMethods.debugBDDMethods(bdd, "random minterm 1", minterm1);
//		
//		int minterm2 = BDDWrapper.oneRandomMinterm(bdd, h, vars);
//		UtilityMethods.debugBDDMethods(bdd, "random minterm 2", minterm2);
//		
//		int minterm3 = BDDWrapper.oneRandomMinterm(bdd, h, vars);
//		UtilityMethods.debugBDDMethods(bdd, "random minterm 3", minterm3);
		
	}
	
	/**
	 * Given a set of predicates, generates all combinations of truth assignment to predicates 
	 * @param bdd
	 * @param arr
	 * @return
	 */
	public static int[] enumerate(BDD bdd, int[] predicates){
		int[] equivalenceClasses  = new int[(int) Math.pow(2, predicates.length)];
		boolean[] valuation=new boolean[predicates.length];
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, 0, predicates);
		return equivalenceClasses;
	}
	
	/**
	 * Given a set of predicates, generates all combinations of truth assignment to predicates 
	 * @param bdd
	 * @param arr
	 * @return
	 */
	public static int[] enumerate(BDD bdd, Variable[] vars){
		int[] predicates = Variable.getBDDVars(vars);
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
				int predicateValue = bdd.ref(valuation[i]?predicates[i]:bdd.not(predicates[i]));
				equivalenceClasses[eqClassIndex]=bdd.andTo(equivalenceClasses[eqClassIndex], predicateValue);
				bdd.deref(predicateValue);
			}
			return;
		}
		valuation[index]=false;
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, index+1, predicates);
		valuation[index]=true;
		equivalenceClassesFromPredicates(bdd, equivalenceClasses, valuation, index+1, predicates);
	}
	
	public static String booleanArrayToString(boolean[] arr ){
		String result="";
		for(int i=0;i<arr.length;i++){
			result+=arr[i]?"1":"0";
		}
		return result;
	}
	
	public static int booleanArrayToInteger(boolean[] arr){
		String binary = booleanArrayToString(arr);
		return Integer.parseInt(binary, 2);
	}
	
	public static int numOfBits(int number){
		String binary = Integer.toBinaryString(number);
		return binary.length();
	}
	
	public static int[] concatenateArrays(int[] arr1, int[] arr2){
		int length = arr1.length + arr2.length;
		int[] result=new int[length];
		int arr1length=arr1.length;
		for(int i=0;i<length;i++){
			if(i<arr1.length){
				result[i]=arr1[i];
			}else{
				result[i]=arr2[i-arr1length];
			}
		}
		return result;
	}
	
	public static String getUserInput(){
		Scanner reader = new Scanner(System.in);
		System.out.println("press any button to continue");
		String result= reader.nextLine();
		//reader.close();
		return result;
	}
	
	public static void debugBDDMethods(BDD bdd, String message, int set, Variable[] vars){
		if(set ==1){
			System.out.println("TRUE");
			return;
		}
		
		if(set == 0){
			System.out.println("FALSE");
			return;
		}
		
		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, set, vars);
		if(minterms == null){
			System.out.println("FALSE");
			return;
		}
		
		for(String minterm : minterms ){
//			System.out.println("mintrem is "+minterm);
			for(int i=0; i<vars.length; i++){
				System.out.print(vars[i].getName()+"="+minterm.charAt(i)+", ");
			}
			System.out.println();
		}
	}
	
	public static  void debugBDDMethods(BDD bdd, String message, int set){
		System.out.println();
		System.out.println(message);
		bdd.printSet(set);
	}
	
	public static void debugBDDMethodsAndWait(BDD bdd, String message, int set){
		debugBDDMethods(bdd, message, set);
		getUserInput();
	}
	
	public static void debugBDDMethods(BDD bdd, String message, int set, int numberOfLines){
		System.out.println();
		System.out.println(message);
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, set);
		int i=0; 
		while(i<minterms.size()){
			int min = Math.min(i+numberOfLines, minterms.size()-i);
			for(int j=i;j<i+min;j++){
				System.out.println(minterms.get(j));
			}
			i=i+min;
			String input = UtilityMethods.getUserInput();
			if(input.equalsIgnoreCase("C")){
				break;
			}
		}
	}
	
	public static int[] IntegerArrayListTointArray(ArrayList<Integer> arrList){
		int[] result= new int[arrList.size()];
		for(int i=0; i<arrList.size();i++){
			result[i]=arrList.get(i);
		}
		return result;
	}
	
	public static Variable[] variableArrayListToArray(ArrayList<Variable> input){
		Variable[] result = new Variable[input.size()];
		for(int i=0; i<input.size();i++){
			result[i]=input.get(i);
		}
		return result;
	}

	public static void prompt(String message){
		System.out.println(message);
		getUserInput();
		
		
	}
	
	/**
	 * given the number of bits, generates all possible combinations of 0 and 1 strings
	 * @param numOfBits
	 * @return
	 */
	public static String[] enumerate(int numOfBits){
		String[] result = new String[(int) Math.pow(2, numOfBits)];
		boolean[] valuation=new boolean[numOfBits];
		enumerateRec(result, valuation, 0, numOfBits);
		return result;
	}
	
	private static void enumerateRec(String[] result, boolean[] valuation, int index, int numOfBits){
		if(index==numOfBits){
			int resultIndex=booleanArrayToInteger(valuation);
			result[resultIndex] = booleanArrayToString(valuation);
			return;
		}
		valuation[index]=false;
		enumerateRec(result, valuation, index+1, numOfBits);
		valuation[index]=true;
		enumerateRec(result, valuation, index+1, numOfBits);
	}
	
	public static Variable[] concatenateArrays(Variable[] arr1, Variable[] arr2){
		int length = arr1.length + arr2.length;
		Variable[] result=new Variable[length];
		int arr1length=arr1.length;
		for(int i=0;i<length;i++){
			if(i<arr1.length){
				result[i]=arr1[i];
			}else{
				result[i]=arr2[i-arr1length];
			}
		}
		return result;
	}
	
	public static <E> ArrayList<E> copyArrayList(ArrayList<E> arr){
		ArrayList<E> result=new ArrayList<E>();
		for(int i=0;i<arr.size();i++){
			result.add(arr.get(i));
		}
		return result;
	}
	
	public static int[] intToBinary(int num){
		String binary=Integer.toBinaryString(num);
		int length=binary.length();
		int[] result=new int[length];
		for(int i=0;i<length;i++){
			//result[length-1-i]=(binary.charAt(i)=='0'? 0 : 1);
			result[i]=(binary.charAt(i)=='0'? 0 : 1);
		}
		return result;
	}
	
	public static long timeStamp(){
		return System.currentTimeMillis();
	}
	
	public static double duration(long t0, String message){
//		long t1=System.currentTimeMillis();
//		double duration =(double)(t1-t0)/60000;
//		System.out.println(message+ ": "+duration + " minutes, which is "+(duration*60) + " seconds");
//		return duration;
		return duration(t0, message, durationThreshold);
	}
	
	public static String duration_string(long t0){
		return duration_string(t0, durationThreshold);
	}
	
	public static double duration(long t0, String message, String timeUnit){
		long t1=System.currentTimeMillis();
		double duration;
		if(timeUnit.equals("s")){
			duration =(double)(t1-t0)/1000;
			System.out.println(message+ ": "+duration + " seconds");
		}else if(timeUnit.equals("m")){
			duration =(double)(t1-t0)/60000;
			System.out.println(message+ ": "+duration + " minutes");
		}else if(timeUnit.equals("h")){
			duration =(double)(t1-t0)/(60*60000);
			System.out.println(message+ ": "+duration + " hours");
		}else{//ms
			duration =(double)(t1-t0);
			System.out.println(message+ ": "+duration + " ms");
		}
		return duration;	
	}
	
	public static double duration(long t0, String message, int threshold){
		long t1=System.currentTimeMillis();
		double duration=t1-t0;
		String timeUnit="ms";
		if(duration>threshold){
			duration =(double)duration/1000;
			timeUnit="seconds";
			if(duration>threshold){
				duration =(double)duration/60;
				timeUnit="minutes";
				if(duration>threshold){
					duration =(double)duration/60;
					timeUnit="hours";
				}
			}	
		}
		System.out.println(message+ ": "+duration + " "+timeUnit);
		return duration;
	}
	
	public static String duration_string(long t0, int threshold){
		long t1=System.currentTimeMillis();
		double duration=t1-t0;
		String timeUnit="ms";
		if(duration>threshold){
			duration =(double)duration/1000;
			timeUnit="seconds";
			if(duration>threshold){
				duration =(double)duration/60;
				timeUnit="minutes";
				if(duration>threshold){
					duration =(double)duration/60;
					timeUnit="hours";
				}
			}	
		}
		return duration + " "+timeUnit;
	}
	
	/**
	 * runs an external process
	 * @param command
	 * @return the output as a string, null if the process did not exit successfully
	 */
	public static String runExternalProcess(String command){
		
		//System.out.println(command);
//		long t0=System.currentTimeMillis();
		String result="";
		boolean successful = true;
		try {
			//System.out.println(command);
	        Process proc = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", command});
	        BufferedReader read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	        
//	        String line2=null;
//	        InputStream stderr = proc.getErrorStream();
//          InputStreamReader isr = new InputStreamReader(stderr);
//          BufferedReader br = new BufferedReader(isr);
//          System.out.println("<ERROR>");
//          while ( (line2 = br.readLine()) != null)
//              System.out.println(line2);
//          System.out.println("</ERROR>");
	        
	        try {
	        	//this.wait(10000);
	            int exitVal=proc.waitFor();
	            if(exitVal!=0){
	            	throw new Exception("The external process didn't terminate successfully!");
	            }
	        } catch (Exception e) {
	            System.out.println(e.getMessage());
	            successful=false;
//	            Scanner reader = new Scanner(System.in);
//	    		System.out.println("press any button");
//	    		reader.nextLine();
	        }
	        
	        if(!successful){
	        	return null;
	        }
	        
	        while (read.ready()) {
	        	result += read.readLine()+"\n";
	        }
	    } catch (Exception e) {
	        System.out.println(e.getMessage());
	    }
//		long t1=System.currentTimeMillis();
//		System.out.println("the external process was executed in "+(t1-t0));
		return result;
	}
	
	public static  <E> void printArrayList(ArrayList<E> input){
		for(E e : input){
			System.out.println(e);
		}
	}
	
	
	public static <E> ArrayList<E> arrayToArrayList(E[] input){
		ArrayList<E> result=new ArrayList<E>();
		for(int i=0;i<input.length;i++){
			result.add(input[i]);
		}
		return result;
	}
	
	public static void memoryUsage(){
		// Get the Java runtime
	    Runtime runtime = Runtime.getRuntime();
	    // Run the garbage collector
	    runtime.gc();
	    // Calculate the used memory
	    long memory = runtime.totalMemory() - runtime.freeMemory();
	    System.out.println("Used memory is bytes: " + memory);
	    System.out.println("Used memory is megabytes: "
	        + bytesToMegabytes(memory));
	  
	}
	
	

	  public static long bytesToMegabytes(long bytes) {
	    return bytes / MEGABYTE;
	  }
	  
//	  public static ArrayList<String> createInterleavingVariableOrderWithTheirPrimedCopy(ArrayList<String> namePrefix, ArrayList<Integer> numOfVars){
//		  ArrayList<String> result = new ArrayList<String>();
//		  int[] counter = new int[namePrefix.size()];
//		  for(int i=0;i<namePrefix.size();i++){
//			  counter[i]=0;
//		  }
//		  
//		  for(int i=0;i<namePrefix.size();i++){
//			  if(counter[i]<numOfVars.get(i)){
//				  result.add(namePrefix.get(i)+i);
//				  counter[i]++;
//			  }
//		  }
//		  
//		  return result;
//	  }
	  
	  public static ArrayList<String> createInterleavingVariableOrder(ArrayList<String> namePrefix, int numOfVars){
		  ArrayList<String> result = new ArrayList<String>();
		  int counter=0;
		  while(counter<numOfVars){
			  for(int i=0;i<namePrefix.size();i++){
				  if(counter < numOfVars){
					  result.add(namePrefix.get(i)+counter); 
				  }
			  }
			  counter++;
		  }
		  
		  return result;
	  }
	  
	  public static ArrayList<String> createAgentsVariablesNamePrefixWithInterleavingOrder(int numOfAgents, String agentNamePrefix, String varsNamePrefix, int numOfBits){
		  ArrayList<String> agentsVarsPrefix = new ArrayList<String>();
		  for(int i= 0; i<numOfAgents;i++){
				String currentAgentName = agentNamePrefix+i+"_";
				agentsVarsPrefix.add(currentAgentName+varsNamePrefix);
		  }
		  ArrayList<String> agentsVars = createInterleavingVariableOrder(agentsVarsPrefix, numOfBits);
		  return agentsVars;
	  }
	  
	  public static Variable[] createVariablesForAgentsWithInterleavingOrder(BDD bdd, int numOfAgents, String agentNamePrefix, String varsNamePrefix, int numOfBits){
		  
		  ArrayList<String> agentsVars = createAgentsVariablesNamePrefixWithInterleavingOrder(numOfAgents, agentNamePrefix, varsNamePrefix, numOfBits);
		  Variable[] vars = Variable.createVariables(bdd, agentsVars);
		  return vars;
	  }
	  
	  public static Variable[] createVariablesAndTheirPrimedCopiesForAgentsWithInterleavingOrder(BDD bdd, int numOfAgents, String agentNamePrefix, String varsNamePrefix, int numOfBits){
		  
		  ArrayList<String> agentsVars = createAgentsVariablesNamePrefixWithInterleavingOrder(numOfAgents, agentNamePrefix, varsNamePrefix, numOfBits);
		  Variable[] vars = Variable.createVariablesAndPrimedCopies(bdd, agentsVars);
		  return vars;
	  }
	  
	  public static Variable[] createVariablesAndTheirPrimedCopyForAgentsWithInterleavingOrder(BDD bdd, 
			  int numOfAgents, String agentNamePrefix, String varsNamePrefix, int numOfBits){
		  
		  ArrayList<String> agentsVars = createAgentsVariablesNamePrefixWithInterleavingOrder(numOfAgents, agentNamePrefix, varsNamePrefix, numOfBits);
		  Variable[] vars = Variable.createVariablesAndPrimedCopies(bdd, agentsVars);
		  return vars;
	  }
	
	  
	//simulation methods  
	public static int randomIndex(int bound){
		return (int) Math.floor(bound*Math.random());
	}
	
	public static ArrayList<GridCell2D> setOfStatesToGridCells(BDD bdd, int states, Agent2D agent){
		return setOfStatesToGridCells(bdd, states, agent.getXVars(), agent.getYVars());
	}
		
	public static ArrayList<GridCell2D> setOfStatesToGridCells(BDD bdd, int states, Variable[] x, Variable[] y){
    	Variable[] relatedVariables = Variable.unionVariables(x, y);
    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, relatedVariables);
    	ArrayList<GridCell2D> gridCells = new ArrayList<GridCell2D>();
    	for(String m : minterms){
    		gridCells.add(getGridCellFromMinterm(m,x,y));
    	}
    	return gridCells;
    }
	
    public static GridCell2D getGridCellFromMinterm(String binary, Variable[] x , Variable[] y){
    	String xBinary = binary.substring(0, x.length);
    	String yBinary = binary.substring(x.length);
    	
    	int xValue = Integer.parseInt(xBinary,2);
    	int yValue = Integer.parseInt(yBinary,2);
    	
    	GridCell2D gc = new GridCell2D(xValue, yValue);
    	return gc;
    }
    
    public static GridCell2D getGridCellFromMinterm(String xBinary, String yBinary){
    	
    	int xValue = Integer.parseInt(xBinary,2);
    	int yValue = Integer.parseInt(yBinary,2);
    	
    	GridCell2D gc = new GridCell2D(xValue, yValue);
    	return gc;
    }
    
    public static GridCell2D[][] getGridCellsFromStates(BDD bdd, ArrayList<String> minterms, Variable[] vars, 
    		ArrayList<Agent2D> agents){
    	
//    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, gameStructure.variables);
//    	if(minterms == null ){
//    		System.err.println("the set of gridcells corresponding to the states is empty");
//    		UtilityMethods.getUserInput();
//    		return null;
//    	}
    	
    	GridCell2D[][] result = new GridCell2D[agents.size()][minterms.size()];
    	for(int i=0; i<agents.size();i++){
    		Agent2D agent = agents.get(i);
    		for(int j=0; j<minterms.size(); j++){
    			String minterm = minterms.get(j);
    			String xVal = BDDWrapper.keepRelevantVariables(minterm, vars, agent.getXVars());
    			String yVal = BDDWrapper.keepRelevantVariables(minterm, vars, agent.getYVars());
    			result[i][j] = UtilityMethods.getGridCellFromMinterm(xVal, yVal);
    		}
    	}
    	
    	return result;
    }
    
    public static ArrayList<GridCell2D> getGridCellsFromState(BDD bdd, String minterm, Variable[] vars, 
    		ArrayList<Agent2D> agents){
    	ArrayList<GridCell2D> result = new ArrayList<GridCell2D>();
    	for(int i=0; i<agents.size();i++){
    		Agent2D agent = agents.get(i);
    		String xVal = BDDWrapper.keepRelevantVariables(minterm, vars, agent.getXVars());
    		String yVal = BDDWrapper.keepRelevantVariables(minterm, vars, agent.getYVars());
    		result.add(UtilityMethods.getGridCellFromMinterm(xVal, yVal));
    	}
    	
    	return result;
    }
    
    public static Variable[] getAgentsVariables(ArrayList<Agent2D> agents){
    	if(agents == null || agents.size()==0){
    		return null;
    	}
    	Variable[] vars = agents.get(0).getVariables();
    	for(int i=1; i<agents.size(); i++){
    		vars = Variable.unionVariables(vars, agents.get(i).getVariables());
    	}
    	return vars;
    }
    
    public static Variable[] getAgentsActionVariables(ArrayList<Agent2D> agents){
    	if(agents == null || agents.size()==0){
    		return null;
    	}
    	Variable[] vars = agents.get(0).getActionVars();
    	for(int i=1; i<agents.size(); i++){
    		vars = Variable.unionVariables(vars, agents.get(i).getActionVars());
    	}
    	return vars;
    }
    
    
    public static int getAgentsInitialState(BDD bdd, ArrayList<Agent2D> agents){
    	int init = bdd.ref(bdd.getOne());
    	for(Agent2D agent : agents){
    		init = BDDWrapper.andTo(bdd, init, agent.getInit());
    	}
    	return init;
    }
    
    /**
     * Given a list of conjuncts, computes a single objective as conjunction of conjucts and return it
     * @param objectives
     * @return
     */
    public static int computeConjunctiveObjective(BDD bdd, ArrayList<Integer> objectives){
    	int objective = bdd.ref(bdd.getOne());
    	for(Integer obj : objectives){
    		objective = BDDWrapper.andTo(bdd, objective, obj);
    	}
    	return objective;
    }
    
    //Computing bounded reachability objective
	/**
	 * creates the memory variable for a bounded reachability objective which indicates whether the objective is 
	 * fulfilled or not
	 * @param namePrefix
	 * @return
	 */
	public static Variable createBoundedReachabilityFlag(BDD bdd, String namePrefix){
		Variable flag = Variable.createVariableAndPrimedVariable(bdd, namePrefix);
		return flag;
	}
	
	public static Variable[] createBoundedReachabilityFlags(BDD bdd, int numberOfFlags, String namePrefix){
		Variable[] flags = new Variable[numberOfFlags];
		for(int i=0; i<numberOfFlags; i++){
			flags[i]=Variable.createVariableAndPrimedVariable(bdd, namePrefix);
		}
		return flags;
	}
	
	/**
	 * creates counter variables for a bounded reachability objective 
	 * the first row corresponds to counter and the second row to their primed version
	 * @param bound
	 * @param namePrefix
	 * @return
	 */
	public static Variable[] createBoundedReachabilityCounter(BDD bdd, int bound, String namePrefix){
		int counterBits=UtilityMethods.numOfBits(bound);
		Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, counterBits, "i"+namePrefix);
		return counter;
	}
	
	public static ArrayList<Variable[]> createBoundedReachabilityCounters(BDD bdd, int numberOfCounterVariables, int bound, String namePrefix){
		//create names 
		ArrayList<String> names = new ArrayList<String>();
		for(int i=0; i<numberOfCounterVariables; i++){
			names.add(namePrefix+"_"+i+"_");
		}
		//create counters 
		ArrayList<Variable[]> counters = Variable.createVariablesWithInterleavingOrder(bdd, names, bound);
		return counters;
	}
	
	public static int boundedReachabilityInit(BDD bdd, int targetState, Variable flag, Variable[] counter){
		//init= counter=0 & targetState <-> f
		int initReachabilityFormula=bdd.ref(bdd.biimp(targetState, flag.getBDDVar()));
		int initCounter = BDDWrapper.assign(bdd, 0, counter);
		initReachabilityFormula = bdd.andTo(initReachabilityFormula, initCounter);
		return initReachabilityFormula;
	}
	
	public static int boundedReachabilityTransitions(BDD bdd, int bound, int targetState, int targetStatePrime, Variable[] counter, Variable flag){
		Variable[] counterPrime = Variable.getPrimedCopy(counter);
		Variable flagPrime = flag.getPrimedCopy();
		
		//boundedReachabilityFormula = (f or (i=0 & i'=1) or ... or (i=bound-1 & i'=bound)) & (s' -> f') & (f -> f') & (not f & not s' -> neg f')
		int boundedReachabilityFormula=bdd.ref(bdd.and(flag.getBDDVar(), BDDWrapper.same(bdd, counter,counterPrime)));
		for(int i=0; i<bound;i++ ){
			int currentCounter=BDDWrapper.assign(bdd, i, counter);
			int nextCounter = BDDWrapper.assign(bdd, i+1, counterPrime);
			int counterTransition = bdd.ref(bdd.and(currentCounter, nextCounter));
			boundedReachabilityFormula = bdd.orTo(boundedReachabilityFormula, counterTransition);
			bdd.deref(currentCounter);
			bdd.deref(nextCounter);
			bdd.deref(counterTransition);
		}
//		UtilityMethods.debugBDDMethods(bdd, "reachability and counter", boundedReachabilityFormula);
		int sImpF = bdd.ref(bdd.imp(targetState, flag.getBDDVar()));
		int sPImpFP = bdd.ref(bdd.imp(targetStatePrime, flagPrime.getBDDVar()));
		int fTrans=bdd.ref(bdd.imp(flag.getBDDVar(), flagPrime.getBDDVar()));
		int ftmp=bdd.ref(bdd.and(bdd.not(flag.getBDDVar()), bdd.not(targetStatePrime)));
		int ftrans2=bdd.ref(bdd.imp(ftmp, bdd.not(flagPrime.getBDDVar())));
		boundedReachabilityFormula=bdd.andTo(boundedReachabilityFormula, sImpF);
		boundedReachabilityFormula=bdd.andTo(boundedReachabilityFormula, sPImpFP);
		boundedReachabilityFormula=bdd.andTo(boundedReachabilityFormula, fTrans);
		boundedReachabilityFormula=bdd.andTo(boundedReachabilityFormula, ftrans2);
		bdd.deref(sImpF);
		bdd.deref(fTrans);
		bdd.deref(ftmp);
		bdd.deref(ftrans2);
		return boundedReachabilityFormula;
	}
	
	public static boolean[] allFalseArray(int size){
		boolean[] result = new boolean[size];
		for(int i=0; i<size; i++){
			result[i] = false;
		}
		return result;
	}
	
	public static boolean[] allTrueArray(int size){
		boolean[] result = new boolean[size];
		for(int i=0; i<size; i++){
			result[i] = true;
		}
		return result;
	}
	
	public static int allFalse(BDD bdd, Variable[] vars){
		boolean[] allFalseArray = allFalseArray(vars.length);
		return BDDWrapper.assign(bdd, allFalseArray, vars);
	}
	
	/**
	 * Generates a formula that is true if and only if var1 < vars2
	 * Assumes 
	 * @param bdd
	 * @param var1
	 * @param var2
	 * @return
	 */
	public static int lessThan(BDD bdd, Variable[] var1, Variable[] var2){
		int result = bdd.ref(bdd.getZero());
		if(var1.length != var2.length){
			return result;
		}
		
		
		for(int i=var1.length-1; i>=0; i--){
			int var1LeastSignificant = var1[i].getBDDVar();
			int var2LeastSignificant = var2[i].getBDDVar();
			int leastSignificantComparison = BDDWrapper.and(bdd, bdd.not(var1LeastSignificant), var2LeastSignificant);
			
			if(i!=var1.length-1){
				int eq = BDDWrapper.biimp(bdd, var1LeastSignificant, var2LeastSignificant);
				result = BDDWrapper.andTo(bdd, result, eq);
				BDDWrapper.free(bdd, eq); 
			}
			result = BDDWrapper.orTo(bdd, result, leastSignificantComparison);
			BDDWrapper.free(bdd, leastSignificantComparison);
		}
		
		return result;
	}
	
	public static int chooseStateRandomly(BDD bdd, int states, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, states, vars);
		int randomIndex = UtilityMethods.randomIndex(minterms.size());
		String chosenMinterm = minterms.get(randomIndex);
		int state = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		return state;
		
	}
	
	/**
	 * Given a binary string, returns the number of ones in it 
	 * @param minterm
	 * @return
	 */
	public static int numOfOnes(String minterm){
		int result = 0;
		for(int i=0; i<minterm.length(); i++){
			if(minterm.charAt(i)=='1') result++;
		}
		return result;
	}
}
