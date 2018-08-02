package symbolic;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import testCases.SimpleRobotMotionPlanningTestCaseGenerator;
import automaton.LabeledNode;
import automaton.Node;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

/**
 * TODO:
 * 1) test post images
 * 2) synthesizing strategies
 * 3) strategy and counter-strategies as transition systems with initial states
 * @author moarref
 *
 */

public class SymbolicGame {
	
	
	int[] inputVariables; //input variables of the transition system
	int[] outputVariables; //output variables of the transition system
	int[] inputPrimeVariables; //primed version of the input variables
	int[] outputPrimeVariables;
	int[] variables;
	int[] primedVariables;
	int init; //bdd representing the initial states of the transition system
	int T; //bdd representing the transition relation
	int T_env; //transition relation for environment
	int T_sys; //transition relation for the system player
	BDD bdd; //bdd object managing the binary decision diagrams
	Permutation vTovPrime; //permutation from variables to their prime version
	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
	
	int variablesCube;
	int primeVariablesCube;
	int inputVarsCube;
	int outputVarsCube;
	int primeInputVarsCube;
	int primeOutputVarsCube;
	
	public SymbolicGame(BDD argBdd, int[] argInputVariables, int[] argOutputVariables, int[] argInputPrimedVars, int[] argOutputPrimedVars, int initial, int transitionRelation){
		bdd=argBdd;
		inputVariables= argInputVariables;
		outputVariables= argOutputVariables;
		inputPrimeVariables = argInputPrimedVars;
		outputPrimeVariables = argOutputPrimedVars;
		init=initial;
		T=transitionRelation;
		
		int length = inputVariables.length + outputVariables.length;
		variables=new int[length];
		primedVariables= new int[length];
		int numOfInputVars=inputVariables.length;
		for(int i=0;i<length;i++){
			if(i<numOfInputVars){
				variables[i]=inputVariables[i];
				primedVariables[i] = inputPrimeVariables[i];
			}else{
				variables[i]=outputVariables[i-numOfInputVars];
				primedVariables[i] = outputPrimeVariables[i-numOfInputVars];
			}
		}
		
		vTovPrime=bdd.createPermutation(variables, primedVariables);
		vPrimeTov=bdd.createPermutation(primedVariables, variables);
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		inputVarsCube=-1;
		outputVarsCube=-1;
		primeInputVarsCube=-1;
		primeOutputVarsCube=-1;
	}
	
	public SymbolicGame(BDD argBdd, int[] argInputVariables, int[] argOutputVariables, int[] argInputPrimedVars, int[] argOutputPrimedVars, int initial, int envTrans, int sysTrans){
		bdd=argBdd;
		inputVariables= argInputVariables;
		outputVariables= argOutputVariables;
		inputPrimeVariables = argInputPrimedVars;
		outputPrimeVariables = argOutputPrimedVars;
		init=initial;
		T_env= envTrans;
		T_sys= sysTrans;
		T=bdd.ref(bdd.and(T_env, T_sys));
		
		int length = inputVariables.length + outputVariables.length;
		variables=new int[length];
		primedVariables= new int[length];
		int numOfInputVars=inputVariables.length;
		for(int i=0;i<length;i++){
			if(i<numOfInputVars){
				variables[i]=inputVariables[i];
				primedVariables[i] = inputPrimeVariables[i];
			}else{
				variables[i]=outputVariables[i-numOfInputVars];
				primedVariables[i] = outputPrimeVariables[i-numOfInputVars];
			}
		}
		
		vTovPrime=bdd.createPermutation(variables, primedVariables);
		vPrimeTov=bdd.createPermutation(primedVariables, variables);
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		inputVarsCube=-1;
		outputVarsCube=-1;
		primeInputVarsCube=-1;
		primeOutputVarsCube=-1;
	}
	
	public int[] getInputVariables(){
		return inputVariables;
	}
	
	public int[] getOutputVariables(){
		return outputVariables;
	}
	
	public int[] getVariables(){
		return variables;
	}
	
	public int[] getPrimeVariables(){
		return primedVariables;
	}
	
	public int[] getPrimeInputVariables(){
		return inputPrimeVariables;
	}
	
	public int[] getPrimeOutputVariables(){
		return outputPrimeVariables;
	}
	
	
	public static void main(String[] args) {
		SimpleRobotMotionPlanningTestCaseGenerator.testRMP(3);
	}
	
	/**
	 * Computes the set of states where any valuation of the variables in the cube will lead to a state in the set
	 * Assumes that the cube is a subset of variables
	 * @param set
	 * @param cube
	 * @return
	 */
	public int ApreImage(int set, int cube){
		int statesWithTransitionToSet = EpreImage(set, cube);
		
//		System.out.println("pre image computed");
//		bdd.printSet(statesWithTransitionToSet);
		
		int temp = bdd.ref(bdd.not(set));
		int statesWithTransitionsToComplementSet= EpreImage(temp, cube);
		
//		System.out.println("pre image of complement of the set computed");
//		bdd.printSet(statesWithTransitionsToComplementSet);
		
		bdd.deref(temp);
		int diff= BDDWrapper.diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		
//		System.out.println("the diff was computed");
//		bdd.printSet(diff);
		
		return diff;
	}
	
	public int synthesize(int winningRegion){
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, vTovPrime));
		int winningTrans=bdd.ref(bdd.and(winningRegion, winningRegionPrime));
		bdd.deref(winningRegionPrime);
		int strategy = bdd.ref(bdd.and(T, winningTrans));
		bdd.deref(winningTrans);
		return strategy;
	}
	
	public int removeUnreachableStates(int reachable){
		return synthesize(reachable);
	}
	
	public int reachable(int transitionRelation){
		int Q=0;
		int Qprime=init;
		int cube = getVariablesCube();
		while(Q != Qprime){
			bdd.deref(Q);
			Q=Qprime;
			int post = EpostImage(Qprime, cube, transitionRelation);
			Qprime = bdd.orTo(Qprime, post);
			bdd.deref(post);
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public int counterStrategy(int winningRegion, int objective){
		int complementWinningRegion=bdd.ref(bdd.not(winningRegion));
		int complementObjective=bdd.ref(bdd.not(objective));
		int cs = synthesize(complementWinningRegion);
		
		int complementWinningRegionPrime=bdd.ref(bdd.replace(complementWinningRegion, vTovPrime));
		int deadlocks=bdd.ref(bdd.and(complementObjective, complementWinningRegionPrime));
		bdd.deref(complementWinningRegionPrime);
		
//		int complementObjectivePrime=bdd.ref(bdd.replace(complementObjective, vTovPrime));
//		int deadlocks=bdd.ref(bdd.and(complementWinningRegion, complementObjectivePrime));
//		bdd.deref(complementObjectivePrime);
		
		bdd.deref(complementWinningRegion);
		bdd.deref(complementObjective);
		
		return BDDWrapper.diff(bdd, cs, deadlocks);
	}
	
	public int counterStrategyReachable(int winningRegion, int objective){
		int cs = counterStrategy(winningRegion, objective);
		int reachable = reachable(cs);
		int reachablePrime=bdd.ref(bdd.replace(reachable, vTovPrime));
		int reachableTrans=bdd.ref(bdd.and(reachable,reachablePrime));
		bdd.deref(reachablePrime);
		int counterStrategy = bdd.ref(bdd.and(cs, reachableTrans));
		bdd.deref(reachableTrans);
		bdd.deref(cs);
		return counterStrategy;
	}
	
//	public int synthesize(int objective){
//		//compute the winning region
//		int winningRegion=greatestFixPoint(objective);
//		//if the game is realizable
//		if(BDDWrapper.subset(bdd, init, winningRegion)){
//			return synthesize(winningRegion);
//		}
//		return -1;
//	}
	
	public boolean isRealizable(int complementErrorSet){
		int winningRegion=greatestFixPoint(complementErrorSet);
		return BDDWrapper.subset(bdd, init, winningRegion);
		
	}
	
	public int greatestFixPoint(int set){
		int Q=1;
		int Qprime=set;
		while(Q != Qprime){
			bdd.deref(Q);
			Q=Qprime;
//			System.out.println("the set for Qprime is ");
//			bdd.printSet(Qprime);
			int pre = controllablePredecessor(Qprime);
			Qprime = bdd.andTo(Qprime, pre);
			bdd.deref(pre);
			
			
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public int controllablePredecessor(int set){
		if(primeOutputVarsCube==-1){
			primeOutputVarsCube=andAll(outputPrimeVariables);
		}
		int cotrollableEpre=EpreImage(set, primeOutputVarsCube); 
		
		if(primeVariablesCube==-1){
			primeVariablesCube=andAll(primedVariables);
		}
//		if(primeInputVarsCube==-1){
//			primeInputVarsCube=andAll(inputPrimeVariables);
//		}
		int uncontrollableApre=ApreImageNoReplacement(cotrollableEpre, primeVariablesCube);
		bdd.deref(cotrollableEpre);
		return uncontrollableApre;
	}
	
	public int ApostImage(int set){
		return -1;
	}
	
	public int ApreImageNoReplacement(int set, int cube){
		int statesWithTransitionToSet = EpreImageNoReplacement(set, cube);
		
//		System.out.println("pre image computed");
//		bdd.printSet(statesWithTransitionToSet);
		
		int temp = bdd.ref(bdd.not(set));
		int statesWithTransitionsToComplementSet= EpreImageNoReplacement(temp, cube);
		
//		System.out.println("pre image of complement of the set computed");
//		bdd.printSet(statesWithTransitionsToComplementSet);
		
		bdd.deref(temp);
		int diff= BDDWrapper.diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		
//		System.out.println("the diff was computed");
//		bdd.printSet(diff);
		
		return diff;
	}
	
	public int EpreImageNoReplacement(int set, int cube){
		int temp = bdd.ref(bdd.and(set, T));
		int preImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
		
		return preImage;
	}
	
	public int EpreImage(int set, int cube){
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
		int temp = bdd.ref(bdd.and(targetSetPrime, T));
		int preImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		
		return preImage;
	}
	
	
	
	public int EpostImage(int set, int cube){
		int temp = bdd.ref(bdd.and(set, T));
		int tempImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
				
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeTov));
		bdd.deref(tempImage);
				
		return postImage;
	}
	
	public int EpostImage(int set, int cube, int trans){
		int temp = bdd.ref(bdd.and(set, trans));
		int tempImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
				
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeTov));
		bdd.deref(tempImage);
				
		return postImage;
	}
	
	private int andAll(int[] vars){
		int result=1;
		for(int i=0;i<vars.length;i++){
			result=bdd.andTo(result, vars[i]);
		}
		return result;
	}
	
	
	
	public void drawStrategy(int strategy, String file){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
		
		PrintStream old = System.out;
		System.setOut(ps);
		bdd.printSet(strategy);
		System.out.flush();
	    System.setOut(old);
	    String transitions=baos.toString();
	    
	    String[] trans=transitions.split("\n");
	    
	     
	    //TODO: transform to something from automaton package
	    ArrayList<Node> nodes = new ArrayList<Node>();
	    HashMap<Node, ArrayList<Node>> adjacencyList= new HashMap<Node, ArrayList<Node>>();
	    HashMap<Node, String> nodeToLabel=new HashMap<Node, String>();
	    HashMap<String, Node> labelToNode= new HashMap<String, Node>();
	    
	    int i=0;
	    int numOfVariables=variables.length;
	    for(String tr : trans){
	    	String state = tr.substring(0,numOfVariables);
	    	String nextState= tr.substring(numOfVariables, tr.length());
	    	
//	    	System.out.println("state: "+state+" & next state: "+nextState);
	    	Node n ;
	    	if(labelToNode.containsKey(state)){
	    		n = labelToNode.get(state);
	    	}else{
	    		n = new Node("q"+i);
	    		i++;
	    		nodes.add(n);
	    		nodeToLabel.put(n, state);
	    		labelToNode.put(state, n);
	    		adjacencyList.put(n, new ArrayList<Node>());
	    	}
	    	
	    	Node n2;
	    	if(labelToNode.containsKey(nextState)){
	    		n2 = labelToNode.get(nextState);
	    	}else{
	    		n2 = new Node("q"+i);
	    		i++;
	    		nodes.add(n2);
	    		nodeToLabel.put(n2, nextState);
	    		labelToNode.put(nextState, n2);
	    		adjacencyList.put(n2, new ArrayList<Node>());
	    	}
	    	
	    	ArrayList<Node> adjacent = adjacencyList.get(n);
	    	adjacent.add(n2);
	    }
	    
	    drawAutomaton(adjacencyList, nodeToLabel, file, 1);
	    
	}
	
	/**
	 * Generates a dot file for the given automaton
	 * @param aut: the input automaton
	 * @param file: the output file
	 * @param verbosity: the amount of information to be shown in output graph - 0: just the graph, 1: also show labels
	 */
	public static void drawAutomaton(HashMap<Node, ArrayList<Node>> adjacency, HashMap<Node, String> nodeToLabel, String file, int verbosity){
		String result="";
		String space="    ";
		result+="digraph G{\n";
		
		for(Node n : adjacency.keySet()){
			String currentNode=n.getName();
			if(verbosity>=1){
				result+=currentNode+" [label=\"" +currentNode+"\\n "+nodeToLabel.get(n)+ "\"];\n";
			}
			
			List<Node> edges = adjacency.get(n);
			if(edges!=null && !edges.isEmpty()){
				for(Node e : edges){
					result+=space+currentNode+" -> "+e.getName();
					if(verbosity>=2){
						result+=" [label=\""+nodeToLabel.get(e)+"\"]"+";\n";
					}
					result+=";\n";
				}
			}
		}
		
		result+="}\n";
		//write the result in a .dot file
		write(result,file);
		//run the dot program to produce the graph as a .ps file
		try{
			Runtime.getRuntime().exec("dot -Tps "+file+" -o "+file.substring(0, file.indexOf("."))+".ps");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void write(String str, String file){
		try{
			PrintWriter pw = new PrintWriter(file);
			pw.println(str);
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public int getCube(int[] vars){
		int cube = andAll(vars);
		return cube;
	}
	
	public int getInputVariablesCube(){
		if(inputVarsCube==-1){
			inputVarsCube=andAll(inputVariables);
		}
		return inputVarsCube;
	}
	
	public int getOutputVariablesCube(){
		if(outputVarsCube==-1){
			outputVarsCube=andAll(outputVariables);
		}
		return outputVarsCube;
	}
	
	public int getVariablesCube(){
		if(variablesCube==-1){
			variablesCube=andAll(variables);
		}
		return variablesCube;
	}
	
	public int getInit(){
		return init;
	}

}
