package csguided;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import specification.Agent;
import utils.UtilityMethods;
import automaton.GameGraph;
import automaton.GameNode;
import automaton.Label;
import automaton.LabeledEdge;
import automaton.StringLabel;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

/**
 * TODO:
 * 1) test post images
 * 2) synthesizing strategies
 * 3) strategy and counter-strategies as transition systems with initial states
 * 4) implement uncontrollable predecessor
 * @author moarref
 *
 */

//TODO: access to variables are direct now, and the get methods return bdd variables, revise later
public class Game {
	
	String id="";
	
	Variable[] inputVariables; //input variables of the transition system
	Variable[] outputVariables; //output variables of the transition system
	Variable[] inputPrimeVariables; //primed version of the input variables
	Variable[] outputPrimeVariables;
	
	public Variable[] variables;
	public Variable[] primedVariables;
	
	int init; //bdd representing the initial states of the transition system
//	int T; //bdd representing the transition relation
	int T_env=-1; //transition relation for environment
	int T_sys=-1; //transition relation for the system player
	
	BDD bdd; //bdd object managing the binary decision diagrams
	Permutation vTovPrime; //permutation from variables to their prime version
	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
	
	//cubes
	
	int inputVarsCube=-1;
	int outputVarsCube=-1;
	int primeInputVarsCube=-1;
	int primeOutputVarsCube=-1;
	
	int variablesCube=-1;
	int primeVariablesCube=-1;
	int variablesAndPrimedVariablesCube=-1;
	int variablesAndActionsCube=-1;
	int primedVariablesAndActionsCube=-1;
	
	//actions 
	
	public Variable[] actionVars;//TODO: this is actually action bits, rename it later
	public int[] sysAvlActions;
	public int[] envAvlActions;
	int actionsCube=-1;
	
	
	
	//for more natural visual representation of states and actions in the game graph
	protected HashMap<String, String> actionMap;
	protected HashMap<String, String> stateMap;
	
	//new code
	
	//We are going to represent the transition relation as disjunction of formulas
	ArrayList<Integer> T_envList;
	ArrayList<Integer> T_sysList;
	
	int[] actions;
	
	//Separating environment and system actions
	public Variable[] envActionVars;
	public Variable[] sysActionVars;
	public int[] envActions;
	public int[] sysActions;
	
	int envActionsCube=-1;
	int sysActionsCube=-1;
	
	int variablesAndEnvActionsCube=-1;
	int variablesAndSysActionsCube=-1;
	int primedVariablesAndEnvActionsCube=-1;
	int primedVariablesAndSysActionsCube=-1;
	
	//parsing from text of the program
	private int envTransIndex=0;
	private int sysTransIndex=0;
	
	public boolean envReset = true;
	public boolean sysReset = true;
	
	public Game(BDD argBdd){
		bdd=argBdd;
	}
	
	
	public void setActionMap(HashMap<String, String> argMap){
		actionMap = argMap;
	}
	
	public void setStateMap(HashMap<String, String> argMap){
		stateMap = argMap;
	}
	
	public String getActionName(String actionVarsValue){
		if(actionMap == null){
			return null;
		}
		
		return actionMap.get(actionVarsValue);
	}
	
	public HashMap<String , String> getActionMap(){
		return actionMap;
	}
	
	public HashMap<String , String> getStateMap(){
		return stateMap;
	}
	
	public Permutation getVtoVprime(){
		return vTovPrime;
	}
	
	public Permutation getVprimetoV(){
		return vPrimeTov;
	}
	
	public String getID(){
		return id;
	}
	
	public void setID(String argId){
		id = argId;
	}
	
	
	
	/**
	 * A constructor for the cases where the input and output variables are not important, 
	 * and only the set of variables (i.e., atomic propositions) matters. 
	 * Also the set of actions should be extracted automatically from the transition relation
	 * @param argBdd
	 * @param argVariables
	 * @param argPrimedVars
	 * @param initial
	 * @param envTrans
	 * @param sysTrans
	 * @param argActions
	 * @param argAvl
	 */
	public Game(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argActions){
		bdd=argBdd;
		init=bdd.ref(initial);
		T_env= bdd.ref(envTrans);
		T_sys= bdd.ref(sysTrans);
//		T=bdd.ref(bdd.and(T_env, T_sys));
		variables = argVariables;
		primedVariables = argPrimedVars;
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		actionVars=argActions;
		envActionVars=argActions;
		sysActionVars=argActions;
		actionsCube=-1;
		
		actions = enumerateActions();
		T_envList=new ArrayList<Integer>();
		T_envList.add(bdd.ref(T_env));
		T_sysList=new ArrayList<Integer>();
		T_sysList.add(bdd.ref(T_sys));
		
		actions = enumerateActions();
		envActions = actions;
		sysActions = actions;
		actionAvailabilitySets();	
	}
	
	public Game(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argEnvActions, Variable[] argSysActions){
		bdd=argBdd;
		init=bdd.ref(initial);
		T_env= bdd.ref(envTrans);
		T_sys= bdd.ref(sysTrans);
//		T=bdd.ref(bdd.and(T_env, T_sys));
		variables = argVariables;
		primedVariables = argPrimedVars;
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
	
		envActionVars = argEnvActions;
		sysActionVars = argSysActions;
		actionVars=Variable.unionVariables(envActionVars, sysActionVars);
		actionsCube=-1;
		
		envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
		sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
		actions = enumerateActions();
		
		T_envList=new ArrayList<Integer>();
		T_envList.add(bdd.ref(T_env));
		T_sysList=new ArrayList<Integer>();
		T_sysList.add(bdd.ref(T_sys));
		
		actionAvailabilitySets();	
	}
	
	public Game(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, ArrayList<Integer> argT_envList, ArrayList<Integer> argT_sysList, Variable[] argActions){
		bdd=argBdd;
//		T=bdd.ref(bdd.and(T_env, T_sys));
		variables = argVariables;
		primedVariables = argPrimedVars;
		actionVars=argActions;
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables),Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
		actionsCube=-1;
		
		init=bdd.ref(initial);
		
		//TODO: check the code for consistency
		//We set the T_env and T_sys to -1 to indicate that they are not computed yet and are stored as partitioned transition relation
		T_env= -1;
		T_sys= -1;
		
		T_envList = BDDWrapper.copyWithReference(bdd, argT_envList);
		T_sysList = BDDWrapper.copyWithReference(bdd, argT_sysList);
		
		actions = enumerateActions();
		envActions = actions;
		sysActions = actions;
		actionAvailabilitySets();
	}
	
	public Game(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, ArrayList<Integer> argT_envList, ArrayList<Integer> argT_sysList, Variable[] argEnvActions, Variable[] argSysActions){
		bdd=argBdd;
//		T=bdd.ref(bdd.and(T_env, T_sys));
		variables = argVariables;
		primedVariables = argPrimedVars;
		
		envActionVars = argEnvActions;
		sysActionVars = argSysActions;
		actionVars=Variable.unionVariables(envActionVars, sysActionVars);
		actionsCube=-1;
		
		envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
		sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
		actions = enumerateActions();
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables),Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
		
		init=bdd.ref(initial);
		
		//TODO: check the code for consistency
		//We set the T_env and T_sys to -1 to indicate that they are not computed yet and are stored as partitioned transition relation
		T_env= -1;
		T_sys= -1;
		
		T_envList = BDDWrapper.copyWithReference(bdd, argT_envList);
		T_sysList = BDDWrapper.copyWithReference(bdd, argT_sysList);

		actionAvailabilitySets();
	}
	
	public void setPermutations(Variable[] vars, Variable[] primeVars){
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(vars), Variable.getBDDVars(primeVars));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primeVars), Variable.getBDDVars(vars));
	}
	
	public int[] getActionVars(){
		return Variable.getBDDVars(actionVars);
	}
	
	public int[] getEnvActionVars(){
		return Variable.getBDDVars(envActionVars);
	}
	
	public int[] getSysActionVars(){
		return Variable.getBDDVars(sysActionVars);
	}
	
	public int[] getActions(){
		return actions;
	}
	
	public int[] getEnvActions(){
		return envActions;
	}
	
	public int[] getSysActions(){
		return sysActions;
	}
	
	public int[] getSystemActionAvailabilityList(){
		return sysAvlActions;
	}
	
	public int[] getEnvironmentActionAvailabilityList(){
		return envAvlActions;
	}
	
	public int[] getInputVariables(){
		return Variable.getBDDVars(inputVariables);
	}
	
	public int[] getOutputVariables(){
		return Variable.getBDDVars(outputVariables);
	}
	
	//TODO: returns int[], maybe it should return Variable[]
	public int[] getVariables(){
		return Variable.getBDDVars(variables);
	}
	
	public int[] getPrimeVariables(){
		return Variable.getBDDVars(primedVariables);
	}
	
	public int[] getPrimeInputVariables(){
		return Variable.getBDDVars(inputPrimeVariables);
	}
	
	public int[] getPrimeOutputVariables(){
		return Variable.getBDDVars(outputPrimeVariables);
	}
	
	
	public ArrayList<Integer> getT_envList(){
		return T_envList;
	}
	
	public ArrayList<Integer> getT_sysList(){
		return T_sysList;
	}
	
	public void setT_envList(ArrayList<Integer> argList){
		T_envList = argList;
	}
	
	public void setT_sysList(ArrayList<Integer> argList){
		T_sysList = argList;
	}
	
	public void setVariables(Variable[] vars){
		variables = vars;
		primedVariables = Variable.getPrimedCopy(variables);
		setPermutations(variables, primedVariables);
		resetCubes();
	}
	
	
	public void freeCubes(){
		if(inputVarsCube != -1) BDDWrapper.free(bdd, inputVarsCube);
		if(outputVarsCube !=-1) BDDWrapper.free(bdd, outputVarsCube);
		if(primeInputVarsCube != -1) BDDWrapper.free(bdd, primeInputVarsCube);
		if(primeOutputVarsCube !=-1) BDDWrapper.free(bdd, primeOutputVarsCube);
		if(variablesCube != -1) BDDWrapper.free(bdd, variablesCube);
		if(primeVariablesCube != -1) BDDWrapper.free(bdd,primeVariablesCube);
		if(variablesAndPrimedVariablesCube != -1) BDDWrapper.free(bdd, variablesAndPrimedVariablesCube);
		if(variablesAndActionsCube != -1) BDDWrapper.free(bdd, variablesAndActionsCube);
		if(primedVariablesAndActionsCube != -1) BDDWrapper.free(bdd, primedVariablesAndActionsCube);
		if(variablesAndEnvActionsCube != -1) BDDWrapper.free(bdd, variablesAndEnvActionsCube);
		if(variablesAndSysActionsCube != -1) BDDWrapper.free(bdd, variablesAndSysActionsCube);
		if(primedVariablesAndEnvActionsCube != -1) BDDWrapper.free(bdd, primedVariablesAndEnvActionsCube);
		if(primedVariablesAndSysActionsCube != -1) BDDWrapper.free(bdd, primedVariablesAndSysActionsCube);
		if(actionsCube != -1) BDDWrapper.free(bdd,actionsCube);
		if(envActionsCube != -1) BDDWrapper.free(bdd, envActionsCube);
		if(sysActionsCube != -1) BDDWrapper.free(bdd, sysActionsCube);
	}
	
	public void resetCubes(){
		inputVarsCube = -1;
		outputVarsCube = -1;
		primeInputVarsCube = -1;
		primeOutputVarsCube = -1;
		variablesCube=-1;
		primeVariablesCube=-1;
		variablesAndPrimedVariablesCube=-1;
		variablesAndActionsCube=-1;
		primedVariablesAndActionsCube=-1;
		variablesAndEnvActionsCube = -1;
		variablesAndSysActionsCube = -1;
		primedVariablesAndEnvActionsCube = -1;
		primedVariablesAndSysActionsCube = -1;
		actionsCube =-1;
		envActionsCube = -1;
		sysActionsCube = -1;
	}
	
	public void setInit(int newInit){
		bdd.deref(getInit());
		init=bdd.ref(newInit);
	}
	
	public static void main(String[] args) {
		//SimpleRobotMotionPlanningTestCaseGenerator.testRMP(3);
		
//		BDD bdd = new BDD(10000,1000);
//		Game game = TestCaseGenerator.simpleNondeterministicGame(bdd);
//		game.toGameGraph().draw("ndgame.dot", 1);
		
//		Tester.simpleNDGameSolverTest();
	}
	
	
	public Game removeUnreachableStates(){
		
		int reachable = reachable();
		int newT_env = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), reachable));
		
		int cube = getVariablesAndActionsCube();
		int reachableSystemStates = EpostImage(reachable, cube , getEnvironmentTransitionRelation());

		int newT_sys =  bdd.ref(bdd.and(getSystemTransitionRelation(), reachableSystemStates));
		Game newGame=new Game(bdd, variables, primedVariables, init, newT_env, newT_sys, envActionVars, sysActionVars);
		newGame.setActionMap(actionMap);
		return newGame;
	}
	

	
	public int reachable(){
		int Q=bdd.ref(bdd.getZero());
//		int Qprime=bdd.ref(bdd.and(init, bdd.getOne()));
		int Qprime = getInit();
		while(Q != Qprime){
			if(Q != getInit()) bdd.deref(Q);
//			Q=bdd.ref(bdd.and(Qprime, bdd.getOne()));
			Q = Qprime;
			int post = symbolicGameOneStepExecution(Qprime);
			Qprime = bdd.ref(bdd.or(Qprime, post));
			bdd.deref(post);
		}
		if(Qprime != getInit()) bdd.deref(Qprime);
		return Q;
	}

	
	public int controllablePredecessor(int set){
//		int cube = bdd.ref(bdd.and(getActionsCube(), getPrimeOutputVariablesCube()));
//		int controllabelEpre = EpreImage(set, cube, T_sys);
////		System.out.println("system can push to ");
////		bdd.printSet(controllabelEpre);
//		bdd.deref(cube);
//		cube=bdd.ref(bdd.and(getActionsCube(), getPrimeInputVarsCube()));
//		int controllablePredecessor=ApreImageNoReplacement(controllabelEpre, cube, T_env);
//		bdd.deref(cube);
//		bdd.deref(controllabelEpre);
//		return controllablePredecessor;
		
//		int cube = bdd.ref(bdd.and(getActionsCube(), getPrimeVariablesCube()));
		int cube = getPrimedVariablesAndActionsCube();
		
		int controllableEpre = bdd.ref(bdd.getZero());
		//int[] actions = enumerateActions();
		
		for(int act : sysActions){
			int controllableAct = ApreImage(set, act, cube, getSystemTransitionRelation());
			controllableEpre = bdd.orTo(controllableEpre, controllableAct);
			bdd.deref(controllableAct);
		}
		
//		UtilityMethods.debugBDDMethods(bdd, "controllableEpre is ", controllableEpre);

//		int controllabelEpre = EpreImage(set, cube, T_sys);
//		System.out.println("system can push to ");
//		bdd.printSet(controllabelEpre);
		
		int controllablePredecessor=ApreImage(controllableEpre, cube, getEnvironmentTransitionRelation());
//		bdd.deref(cube);
		bdd.deref(controllableEpre);
//		System.out.println("environment cannot avoid from ");
//		bdd.printSet(controllablePredecessor);
		return controllablePredecessor;
	}
	
	public int controllablePredecessor_system(int set){
		int cube = bdd.ref(bdd.and(getActionsCube(), getPrimeVariablesCube()));
		int controllableEpre = bdd.ref(bdd.getZero());
		//int[] actions = enumerateActions();
		
		for(int act : sysActions){
			int controllableAct = ApreImage(set, act, cube, getSystemTransitionRelation());
			controllableEpre = bdd.orTo(controllableEpre, controllableAct);
			bdd.deref(controllableAct);
		}
		
		bdd.deref(cube);
		return controllableEpre;
	}
	
	public int controllablePredecessor_env(int controllableEpre){
		int cube = bdd.ref(bdd.and(getActionsCube(), getPrimeVariablesCube()));
		int controllablePredecessor=ApreImage(controllableEpre, cube, getEnvironmentTransitionRelation());
		bdd.deref(cube);
		return controllablePredecessor;
	}
	
	public int ApreImage(int set , int act, int cube, int trans){
		int restrictedTrans = bdd.ref(bdd.and(act, trans));
		int result = ApreImage(set, cube, restrictedTrans);
		bdd.deref(restrictedTrans);
		return result;
	}
	
	public int ApreImage(int set, int act, int cube, ArrayList<Integer> trans){
		int result = bdd.ref(bdd.getZero());
		for(Integer t : trans){
			int restrictedT = ApreImage(set, act, cube, t);
			result=bdd.orTo(result, restrictedT);
			bdd.deref(restrictedT);
		}
		return result;
	}
	
	public int EpreImage(int set, int act, int cube, int trans){
		int restrictedTrans = bdd.ref(bdd.and(act, trans));
		int result = EpreImage(set, cube, restrictedTrans);
		bdd.deref(restrictedTrans);
		return result;
	}
	
//	public int ApostImage(int set){
//		return -1;
//	}
	

	
	public int ApreImage(int set, int cube, int trans){
		int statesWithTransitionToSet = EpreImage(set, cube, trans);
		
//		System.out.println("pre image computed");
//		bdd.printSet(statesWithTransitionToSet);
		
		int temp = bdd.ref(bdd.not(set));
		int statesWithTransitionsToComplementSet= EpreImage(temp, cube, trans);
		
//		System.out.println("pre image of complement of the set computed");
//		bdd.printSet(statesWithTransitionsToComplementSet);
		
		bdd.deref(temp);
		int diff= BDDWrapper.diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		
//		System.out.println("the diff was computed");
//		bdd.printSet(diff);
		
		return diff;
	}
	
	//TODO: ensure it is correct, assumes that APre is computed based on Epre
	public int ApreImage(int set, int cube, ArrayList<Integer> trans){
		int result=bdd.ref(bdd.getZero());
		for(Integer t : trans){
			int restrictedT = ApreImage(set, cube, t);
			result=bdd.orTo(result, restrictedT);
			bdd.deref(restrictedT);
		}
		return result;
	}
	

	
	public int EpreImage(int set, int cube, int trans){		
		int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
//		bdd.printSet(targetSetPrime);
		int temp = bdd.ref(bdd.and(targetSetPrime, trans));
//		System.out.println("Trans is ");
//		bdd.printSet(trans);
//		bdd.printSet(temp);
		int preImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		
		return preImage;
	}
	
	
	public int EpreImage(int set, int cube, ArrayList<Integer> transList){
		int preImage=bdd.ref(bdd.getZero());
		for(int trans : transList){		
			int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
//			bdd.printSet(targetSetPrime);
			int temp = bdd.ref(bdd.and(targetSetPrime, trans));
//			System.out.println("Trans is ");
//			bdd.printSet(trans);
//			bdd.printSet(temp);
			int pre=bdd.ref(bdd.exists(temp, cube));
			preImage=bdd.orTo(preImage, pre);
			bdd.deref(pre);
			bdd.deref(temp);
			bdd.deref(targetSetPrime);
		}
		return preImage;
	}
	
	public int EpreImage(int set, int cube, Player p){
		int preImage=bdd.ref(bdd.getZero());
		if(p == Player.ENVIRONMENT){
			while(hasNextTransition(p)){
				int trans = getNextEnvTransition();				
				int pre = EpreImage(set,cube,trans);
				preImage=bdd.orTo(preImage, pre);
				bdd.deref(pre);
			}
		}else{
			while(hasNextTransition(p)){
				int trans = getNextSysTransition();
				int pre = EpreImage(set,cube,trans);
				preImage=bdd.orTo(preImage, pre);
				bdd.deref(pre);
			}
		}
		return preImage;
	}
	
	

	
	public int EpostImage(int set, int cube, int trans){
		
//		if(trans == getEnvironmentTransitionRelation()){
//			System.out.println("postImage, envTrans");
//			UtilityMethods.debugBDDMethods(bdd, "env trans", getEnvironmentTransitionRelation());
//		}
		int temp = bdd.ref(bdd.and(set, trans));
		
//		UtilityMethods.debugBDDMethods(bdd, "EpostImage, cube is", cube);
//		UtilityMethods.debugBDDMethods(bdd, "EpostImage, Epost, trans is", trans);
//		UtilityMethods.debugBDDMethods(bdd, "EpostImage, Epost, set is", set);
//		UtilityMethods.debugBDDMethods(bdd, "EpostImage, temp is", temp);
		
		int tempImage=bdd.ref(bdd.exists(temp, cube));
		bdd.deref(temp);
				
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeTov));
		bdd.deref(tempImage);
				
		return postImage;
	}
	
	public int EpostImage(int set, int cube, ArrayList<Integer> transList){
		int postImage=bdd.ref(bdd.getZero());
		
		for(int trans : transList){
			int temp = bdd.ref(bdd.and(set, trans));
			int tempImage=bdd.ref(bdd.exists(temp, cube));
			bdd.deref(temp);
					
			int post=bdd.ref(bdd.replace(tempImage, vPrimeTov));
			postImage = bdd.orTo(postImage, post);
			bdd.deref(post);
			bdd.deref(tempImage);
		}
				
		return postImage;
	}
	
	public int symbolicGameOneStepExecution(int set){
		int cube=getVariablesAndActionsCube();
//		int envMoves = EpostImage(set, cube, getEnvironmentTransitionRelation());
//		int sysMoves = EpostImage(envMoves, cube, getSystemTransitionRelation());
		
		int envMoves = EpostImage(set, cube, getT_envList());
		int sysMoves = EpostImage(envMoves, cube, getT_sysList());
		
		bdd.deref(envMoves);
		return sysMoves;
	}
	
	private int andAll(int[] vars){
		int result=bdd.ref(bdd.getOne());
		for(int i=0;i<vars.length;i++){
			result=bdd.andTo(result, vars[i]);
		}
		return result;
	}
	
	
	public int getCube(int[] vars){
		int cube = andAll(vars);
		return cube;
	}
	
	public int getInputVariablesCube(){
		if(inputVarsCube==-1){
			inputVarsCube=andAll(getInputVariables());
		}
		return inputVarsCube;
	}
	
	public int getPrimeInputVarsCube(){
		if(primeInputVarsCube == -1){
			primeInputVarsCube = andAll(getPrimeInputVariables());
		}
		return primeInputVarsCube;
	}
	
	public int getOutputVariablesCube(){
		if(outputVarsCube==-1){
			outputVarsCube=andAll(getOutputVariables());
		}
		return outputVarsCube;
	}
	
	public int getPrimeOutputVariablesCube(){
		if(primeOutputVarsCube==-1){
			primeOutputVarsCube=andAll(getPrimeOutputVariables());
		}
		return primeOutputVarsCube;
	}
	
	public int getVariablesCube(){
		if(variablesCube==-1){
			variablesCube=andAll(getVariables());
		}
		return variablesCube;
	}
	
	public int getPrimeVariablesCube(){
		if(primeVariablesCube==-1){
			primeVariablesCube=andAll(getPrimeVariables());
		}
		return primeVariablesCube;
	}
	
	public int getVariablesAndPrimedVariablesCube(){
		if(variablesAndPrimedVariablesCube==-1){
			variablesAndPrimedVariablesCube=bdd.ref(bdd.and(getVariablesCube(), getPrimeVariablesCube()));
		}
		return variablesAndPrimedVariablesCube;
	}
	
	public int getVariablesAndActionsCube(){
		if(variablesAndActionsCube==-1){
			variablesAndActionsCube=bdd.ref(bdd.and(getVariablesCube(), getActionsCube()));
		}
		return variablesAndActionsCube;
	}
	
	public int getPrimedVariablesAndActionsCube(){
		if(primedVariablesAndActionsCube==-1){
			primedVariablesAndActionsCube=bdd.ref(bdd.and(getPrimeVariablesCube(), getActionsCube()));
		}
		return primedVariablesAndActionsCube;
	}
	
	public int getActionsCube(){
		if(actionsCube==-1){
			actionsCube=andAll(getActionVars());
		}
		return actionsCube;
	}
	
	
	public int getEnvActionsCube(){
		if(envActionsCube==-1){
			envActionsCube=andAll(getEnvActions());
		}
		return envActionsCube;
	}
	
	public int getSysActionsCube(){
		if(sysActionsCube==-1){
			sysActionsCube=andAll(getSysActions());
		}
		return sysActionsCube;
	}
	
	public int getVariablesAndEnvActionsCube(){
		if(variablesAndEnvActionsCube==-1){
			variablesAndEnvActionsCube=bdd.ref(bdd.and(getVariablesCube(), getEnvActionsCube()));
		}
		return variablesAndEnvActionsCube;
	}
	
	public int getVariablesAndSysActionsCube(){
		if(variablesAndSysActionsCube==-1){
			variablesAndSysActionsCube=bdd.ref(bdd.and(getVariablesCube(), getSysActionsCube()));
		}
		return variablesAndSysActionsCube;
	}
	
	public int getPrimedVariablesAndEnvActionsCube(){
		if(primedVariablesAndEnvActionsCube==-1){
			primedVariablesAndEnvActionsCube=bdd.ref(bdd.and(getPrimeVariablesCube(), getEnvActionsCube()));
		}
		return primedVariablesAndEnvActionsCube;
	}
	
	public int getPrimeVariablesAndSysActionsCube(){
		if(primedVariablesAndSysActionsCube==-1){
			primedVariablesAndSysActionsCube=bdd.ref(bdd.and(getPrimeVariablesCube(), getSysActionsCube()));
		}
		return primedVariablesAndSysActionsCube;
	}
	
	public int getInit(){
		return init;
	}
	
	public int getEnvironmentTransitionRelation(){
		//if T_env is not computed 
		if(T_env==-1){
			if(T_envList!=null){
				T_env = BDDWrapper.computeDisjunction(bdd, T_envList);
			}else{
				return -1;
			}
		}
		return T_env;
	}
	
	public int getSystemTransitionRelation(){
		//if T_env is not computed 
		if(T_sys==-1){
			if(T_sysList!=null){
				T_sys = BDDWrapper.computeDisjunction(bdd, T_sysList);
			}else{
				return -1;
			}
		}
		return T_sys;
	}
	
	public GameGraph toGameGraph(){
		return toGameGraph("");
	}
	
	//TODO: update for the current state of the program
	public GameGraph toGameGraph(String id){
		GameGraph gg=new GameGraph(id);
		ArrayList<String>  envTrans = parseTransitions(getEnvironmentTransitionRelation());
		ArrayList<String>  sysTrans = parseTransitions(getSystemTransitionRelation());
		
		HashMap<String, GameNode> labelToSystemNode= new HashMap<String, GameNode>();
	    HashMap<String, GameNode> labelToEnvironmentNode= new HashMap<String, GameNode>();
		
		 //environment transitions
	    int i=0;
	    int numOfVariables=variables.length;
	    for(String tr : envTrans){
	    	if(tr.equals("FALSE")){
	    		continue;
	    	}
	    	
	    	String state = tr.substring(0,numOfVariables);
	    	String nextState= tr.substring(numOfVariables, 2*numOfVariables);
	    	String action = tr.substring(2*numOfVariables, tr.length());
	    	
	    	//parse the labels
	    	if(stateMap!=null){
	    		state=stateMap.get(state);
	    		nextState=stateMap.get(nextState);
	    	}
//	    	else{
//	    		state=parseLabel(state, variables);
//		    	nextState=parseLabel(nextState, variables);
//	    	}
	    	
	    	if(actionMap != null){
	    		action=getActionName(action);
	    	}else{
	    		action = parseLabel(action, actionVars);
	    	}
	    	
	    	
//	    	System.out.println("state: "+state+" & next state: "+nextState+" & action is "+action);
	    	GameNode gn ;
	    	if(labelToEnvironmentNode.containsKey(state)){
	    		gn = labelToEnvironmentNode.get(state);
	    	}else{
	    		gn = new GameNode("q"+i, 'e', new StringLabel(state));
	    		
	    		//check if the new node is initial
	    		if(BDDWrapper.isMember(bdd, state, variables, init)){
	    			gn.setInitial(true);
	    		}
	    		
	    		i++;
	    		gg.addNode(gn);
	    		labelToEnvironmentNode.put(state, gn);
	    	}
	    	
	    	GameNode gn2;
	    	if(labelToSystemNode.containsKey(nextState)){
	    		gn2 = labelToSystemNode.get(nextState);
	    	}else{
	    		gn2 = new GameNode("q"+i, 's', new StringLabel(nextState));	    		
	    		i++;
	    		gg.addNode(gn2);
	    		labelToSystemNode.put(nextState, gn2);
	    	}
	    	
	    	
	    	LabeledEdge<GameNode, Label> e = new LabeledEdge<GameNode, Label>(gn, gn2, new StringLabel(action));
	    	gg.addEdge(e);
	    	
	    }
	    
	    
	    //system transitions
	    for(String tr : sysTrans){
	    	if(tr.equals("FALSE")){
	    		continue;
	    	}
	    	String state = tr.substring(0,numOfVariables);
	    	String nextState= tr.substring(numOfVariables, 2*numOfVariables);
	    	String action = tr.substring(2*numOfVariables, tr.length());
	    	
	    	//parse the labels
	    	if(stateMap!=null){
	    		state=stateMap.get(state);
	    		nextState=stateMap.get(nextState);
	    	}
//	    	else{
//	    		state=parseLabel(state, variables);
//		    	nextState=parseLabel(nextState, variables);
//	    	}
	    	
	    	if(actionMap != null){
	    		action=getActionName(action);
	    	}else{
	    		action = parseLabel(action, actionVars);
	    	}
	    	
//	    	System.out.println("state: "+state+" & next state: "+nextState);
	    	GameNode gn ;
	    	if(labelToSystemNode.containsKey(state)){
	    		gn = labelToSystemNode.get(state);
	    	}else{
	    		gn = new GameNode("q"+i, 's', new StringLabel(state)); 		
	    		i++;
	    		gg.addNode(gn);
	    		labelToSystemNode.put(state, gn);
	    	}
	    	
	    	GameNode gn2;
	    	if(labelToEnvironmentNode.containsKey(nextState)){
	    		gn2 = labelToEnvironmentNode.get(nextState);
	    	}else{
	    		gn2 = new GameNode("q"+i, 'e', new StringLabel(nextState));
	    		
	    		//check if the new node is initial
	    		if(BDDWrapper.isMember(bdd, nextState, variables, init)){
	    			gn2.setInitial(true);
	    		}
	    		
	    		i++;
	    		gg.addNode(gn2);
	    		labelToEnvironmentNode.put(nextState, gn2);
	    	}
	    	
	    	
	    	LabeledEdge<GameNode, Label> e = new LabeledEdge<GameNode, Label>(gn, gn2, new StringLabel(action));
	    	gg.addEdge(e);
	    }
		return gg;
	}
	
	/**
	 * given the boolean values for the variables, replace it with variables names
	 * @param label
	 * @return
	 */
	protected String parseLabel(String label, Variable[] vars){
		String result="";
//		System.out.println("label is "+label);
		if(label.length()!=vars.length){
			System.err.println("the label and the variables do not have the same size! NULL is returned");
			return null;
		}
		boolean before=false;
		for(int i=0;i<label.length();i++){
			if(before) result+=",";
			if(label.charAt(i)=='0'){
				result+="!"+vars[i].getName();
			}else if(label.charAt(i)=='1'){
				result+=vars[i].getName();
			}
			if(!before) before=true;
		}
//		System.out.println("result is "+result);
		return result;
	}
	
	protected ArrayList<String> parseTransitions(int transitionRelation){
		return BDDWrapper.minterms(bdd, transitionRelation, variables, primedVariables, actionVars);
	}


	
	public int getAvailableActionsAtState(int state, Player player){
		int availableActions=bdd.ref(bdd.getZero());
		if(player == Player.ENVIRONMENT){
			for(int i=0;i<envAvlActions.length;i++){
				int intersection = BDDWrapper.intersect(bdd, state, envAvlActions[i]);
				if(intersection != 0){//action available
					int act = BDDWrapper.assign(bdd, i, envActionVars);
					availableActions=bdd.orTo(availableActions, act);
					bdd.deref(act);
				}
				bdd.deref(intersection);
			}
		}else{
			for(int i=0;i<sysAvlActions.length;i++){
				int intersection = BDDWrapper.intersect(bdd, state, sysAvlActions[i]);
				if(intersection != 0){//action available
					int act = BDDWrapper.assign(bdd, i, sysActionVars);
					availableActions=bdd.orTo(availableActions, act);
					bdd.deref(act);
				}
				bdd.deref(intersection);	
			}
		}
		return availableActions;
	}
	

	

	

	
	
	
	/**
	 * returns all possible actions as bdd formulas, each index of the returned array is a cube representing 
	 * one possible action, it is very close to the code for getting the equivalence classes
	 * TODO: refactor and have a code for enumerating atomic propositions
	 * @return
	 */
	public int[] enumerateActions(){
		return UtilityMethods.enumerate(bdd,Variable.getBDDVars(actionVars));
	}
	
	public int[] enumerateEnvActions(){
		return UtilityMethods.enumerate(bdd,Variable.getBDDVars(envActionVars));
	}
	
	public int[] enumerateSysActions(){
		return UtilityMethods.enumerate(bdd,Variable.getBDDVars(sysActionVars));
	}
	

	
	public void printGame(){
		System.out.println("printing the game information");
		System.out.println("variables");
		Variable.printVariables(variables);
		System.out.println("actions");
		System.out.println("env action vars");
		Variable.printVariables(envActionVars);
		System.out.println("sys action vars");
		Variable.printVariables(sysActionVars);
		UtilityMethods.debugBDDMethods(bdd, "init", init);
		UtilityMethods.debugBDDMethods(bdd, "T_env", getEnvironmentTransitionRelation());
//		UtilityMethods.getUserInput();
		UtilityMethods.debugBDDMethods(bdd, "T_sys", getSystemTransitionRelation());
//		UtilityMethods.getUserInput();
		
//		System.out.println("\nin formula form");
//		Variable[] vars = Variable.unionVariables(this.actionVars, this.variables);
//		vars=Variable.unionVariables(vars, primedVariables);
//		
//		System.out.println("init");
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, getInit(), vars));
//		System.out.println("T_env");
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, getEnvironmentTransitionRelation(), vars));
//		System.out.println("T_sys");
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, getSystemTransitionRelation(), vars));
	}
	
	public void printGameInFormulaForm(){
		System.out.println("printing the game information");
		System.out.println("variables");
		Variable.printVariables(variables);
		System.out.println("actions");
		Variable.printVariables(actionVars);
		UtilityMethods.debugBDDMethods(bdd, "init", init);
		UtilityMethods.debugBDDMethods(bdd, "T_env", getEnvironmentTransitionRelation());
		UtilityMethods.debugBDDMethods(bdd, "T_sys", getSystemTransitionRelation());
		
		System.out.println("\nin formula form");
		Variable[] vars = Variable.unionVariables(this.actionVars, this.variables);
		vars=Variable.unionVariables(vars, primedVariables);
		
		System.out.println("init");
		System.out.println(BDDWrapper.BDDtoFormula(bdd, getInit(), vars));
		System.out.println("T_env");
		System.out.println(BDDWrapper.BDDtoFormula(bdd, getEnvironmentTransitionRelation(), vars));
		System.out.println("T_sys");
		System.out.println(BDDWrapper.BDDtoFormula(bdd, getSystemTransitionRelation(), vars));
	}
	
	public void printGameVars(){
		System.out.println("printing the game information");
		System.out.println("variables");
		Variable.printVariables(variables);
		System.out.println("actions");
		System.out.println("env action vars");
		Variable.printVariables(envActionVars);
		System.out.println("sys action vars");
		Variable.printVariables(sysActionVars);
	}
	
	/**
	 * given the set of equivalence classes, return the abstraction of the game
	 * @param eqClasses
	 * @return
	 */
	public AbstractGame computeAbstraction(int[] eqClasses){
		return new AbstractGame(bdd, eqClasses, this);
	}
	
	
	public AbstractGame computeAbstractionFromAbstractPredicates(int[] abstractPredicates, boolean approximate){
		return new AbstractGame(bdd, abstractPredicates, this, approximate);
	}
	
	/**
	 * compose the input game with the current game and returns the composed game
	 * @param game
	 * @return
	 */
	public Game compose(Game game){
		Variable[] gameVariables = Variable.unionVariables(variables, game.variables);
		Variable[] gamePrimeVariables=Variable.unionVariables(primedVariables, game.primedVariables);
		
//		for(Variable v : gameVariables){
//			System.out.println(v.toString());
//		}
//		UtilityMethods.getUserInput();
//		

		
		int gameInit= bdd.ref(bdd.and(getInit(), game.getInit()));
		
		//just in case if the BDD package does not check whether the two formulas are actually the same
		int T_env1 = getEnvironmentTransitionRelation();
		int T_env2 = game.getEnvironmentTransitionRelation();
		int gameEnvTrans;
		if(T_env1 == T_env2){
			gameEnvTrans = bdd.ref(T_env1);
		}else{
			gameEnvTrans = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), game.getEnvironmentTransitionRelation()));
		}
		
		int T_sys1 = getSystemTransitionRelation();
		int T_sys2 = game.getSystemTransitionRelation();
		int gameSysTrans;
		if(T_sys1 == T_sys2){
			gameSysTrans = bdd.ref(T_sys1);
		}else{
			gameSysTrans = bdd.ref(bdd.and(getSystemTransitionRelation(), game.getSystemTransitionRelation()));
		}
		
//		int gameEnvTrans = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), game.getEnvironmentTransitionRelation()));
//		int gameSysTrans = bdd.ref(bdd.and(getSystemTransitionRelation(), game.getSystemTransitionRelation()));
		
//		Variable[] gameActionVars = Variable.unionVariables(actionVars, game.actionVars);
		Variable[] gameEnvActionVars= Variable.unionVariables(envActionVars, game.envActionVars);
		Variable[] gameSysActionVars= Variable.unionVariables(sysActionVars, game.sysActionVars);
		
		
		Game composedGame= new Game(bdd, gameVariables, gamePrimeVariables, gameInit, gameEnvTrans, gameSysTrans, gameEnvActionVars, gameSysActionVars);
		composedGame.setActionMap(composeActionMaps(getActionMap(), game.getActionMap()));
		return composedGame;
	}
	
	public static HashMap<String , String> composeActionMaps(HashMap<String, String> actMap1, HashMap<String, String> actMap2){
		if(actMap1 == null || actMap2 == null) return null;
		HashMap<String, String> composedActMap=new HashMap<String, String>();
		for(String act1 : actMap1.keySet()){
			for(String act2 : actMap2.keySet()){
				String act=act1+act2;
				String label = actMap1.get(act1)+","+actMap2.get(act2);
				composedActMap.put(act, label);
			}
		}
		return composedActMap;
	}
	
	//altering games to encode bounded reachability objectives
	public Game gameWithBoundedReachabilityObjective(int bound, String namePrefix, int targetStates, int targetStatesPrime ){
		
		//define variables of the reachability game
		Variable[] flag=createBoundedReachabilityFlag(namePrefix);
		Variable[] flagVar={flag[0]};
		Variable[] flagPrimeVar={flag[1]};
		
		Variable[][] counter=createBoundedReachabilityCounter(bound, namePrefix);
		
		//define variables of the new game
		Variable[] vars=UtilityMethods.concatenateArrays(this.variables, flagVar);
		vars=UtilityMethods.concatenateArrays(vars, counter[0]);
		
		Variable[] primedVars=UtilityMethods.concatenateArrays(this.primedVariables, flagPrimeVar);
		primedVars=UtilityMethods.concatenateArrays(primedVars,counter[1]);
		
		//define initial values for reachability game
		int reachInit=boundedReachabilityInit(targetStates, flag[0], counter[0]);
		int boundedReachabilityGameInit= bdd.ref(bdd.and(this.getInit(), reachInit));
		
		//define transition relation for bounded reachability game 
		int reachTrans=boundedReachabilityTransitions(bound, targetStates, targetStatesPrime, counter[0], counter[1], flag[0], flag[1]); 
		int sameCounter=BDDWrapper.same(bdd, counter[0], counter[1]);
		int sameFlag=bdd.ref(bdd.biimp(flag[0].getBDDVar(), flag[1].getBDDVar()));
		int sameReachTrans=bdd.ref(bdd.and(sameCounter, sameFlag));
		
		int boundedReachabilityGameT_env= bdd.ref(bdd.and(getEnvironmentTransitionRelation(), sameReachTrans));
		int boundedReachabilityGameT_sys=  bdd.ref(bdd.and(getSystemTransitionRelation(), reachTrans));
		
//		UtilityMethods.debugBDDMethods(bdd, "reachTransitions", reachTrans1);
		
		//create game based on reachability obj
		Game boundedReachabilityGame=new Game(bdd, vars, primedVars, boundedReachabilityGameInit, boundedReachabilityGameT_env, boundedReachabilityGameT_sys, this.envActionVars, this.sysActionVars);
		return boundedReachabilityGame;
	}
	
	/**
	 * creates counter variables for a bounded reachability objective 
	 * the first row corresponds to counter and the second row to their primed version
	 * @param bound
	 * @param namePrefix
	 * @return
	 */
	public Variable[][] createBoundedReachabilityCounter(int bound, String namePrefix){
		int counterBits=UtilityMethods.numOfBits(bound);
		Variable[] counter = Variable.createVariables(bdd, counterBits, "i"+namePrefix);
		Variable[] counterPrime = Variable.createPrimeVariables(bdd, counter);
		Variable[][] vars={counter,counterPrime};
		return vars;
	}
	
	/**
	 * creates the memory variable for a bounded reachability objective which indicates whether the objective is 
	 * fulfilled or not
	 * @param namePrefix
	 * @return
	 */
	public Variable[] createBoundedReachabilityFlag(String namePrefix){
		Variable flag = new Variable(bdd,namePrefix+"f");
//		Variable flagPrime = new Variable(bdd,namePrefix+"fPrime");
		Variable flagPrime = Variable.createPrimeVariable(bdd, flag);
		Variable[] flagVars = {flag, flagPrime};
		return flagVars;
	}
	
	public int boundedReachabilityInit(int targetState, Variable flag, Variable[] counter){
		//init= counter=0 & targetState <-> f
		int initReachabilityFormula=bdd.ref(bdd.biimp(targetState, flag.getBDDVar()));
		int initCounter = BDDWrapper.assign(bdd, 0, counter);
		initReachabilityFormula = bdd.andTo(initReachabilityFormula, initCounter);
		return initReachabilityFormula;
	}
	
	public int boundedReachabilityTransitions(int bound, int targetState, int targetStatePrime, Variable[] counter, Variable[] counterPrime, Variable flag, Variable flagPrime){
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
	
	public int getDeadLockStates(int transitionRelation){
		int cube = getPrimedVariablesAndActionsCube();
		int statesWithOutgoingTransitions=bdd.ref(bdd.exists(transitionRelation, cube));
		int deadlockStates=bdd.ref(bdd.not(statesWithOutgoingTransitions));
		bdd.deref(statesWithOutgoingTransitions);
		return deadlockStates;
	}
	
	/**
	 * Transform the game into a FairGame with empty set of fairness
	 * @return
	 */
	public FairGame toFairGame(){
		return new FairGame(bdd, variables, primedVariables, init, T_env, T_sys, actionVars);
	}
	
	
	/**
	 * Parsing a game from the program text without storing the whole game
	 */
	
	public void resetTransitionsIndex(){
		envTransIndex = 0;
		sysTransIndex = 0;
	}
	
	public boolean hasNextTransition(Player p){
		if(p == Player.ENVIRONMENT){
			if(envTransIndex < T_envList.size()){
				return true;
			}
			resetEnvTransVars();
			return false;
		}else{
			if(sysTransIndex < T_sysList.size()){
				return true;
			}
			resetSysTransVars();
			return false;
		}
	}
	
	public boolean hasNextTransitionWithoutReset(Player p){
		if(p == Player.ENVIRONMENT){
			if(envTransIndex < T_envList.size()){
				return true;
			}
			return false;
		}else{
			if(sysTransIndex < T_sysList.size()){
				return true;
			}
			return false;
		}
	}
	
	public int getNextEnvTransition(){
		if(!hasNextTransition(Player.ENVIRONMENT)){
			envTransIndex = 0;
			return -1;
		}
		
		envReset =false;
		
		int nextEnvTrans = T_envList.get(envTransIndex);
		envTransIndex++;
		return nextEnvTrans;
	
	}
	
	public int getNextSysTransition(){
		if(!hasNextTransition(Player.SYSTEM)){
			sysTransIndex=0;
			return -1;
		}
		
		sysReset = false;
		
		int nextSysTrans = T_sysList.get(sysTransIndex);
		sysTransIndex++;
		return nextSysTrans;
	}
	

	
	public int getNextTransition(Player p){
		if(p == Player.ENVIRONMENT){
			return getNextEnvTransition();
		}else{
			return getNextSysTransition();
		}
	}
	
	public void resetEnvTransVars(){
		envTransIndex = 0;
		envReset = true;
	}
	
	public void resetSysTransVars(){
		sysTransIndex = 0;
		sysReset = true;
	}
	
	public void actionAvailabilitySets(){
//		System.out.println("computing actions availability sets");
//		long t_avl = UtilityMethods.timeStamp(); 
		sysAvlActions = new int[sysActions.length];
		envAvlActions = new int[envActions.length];
		
		for(int i=0; i<envAvlActions.length;i++){
			envAvlActions[i]= bdd.ref(bdd.getZero());
		}
		
		for(int i=0;i<sysAvlActions.length;i++){
			sysAvlActions[i]= bdd.ref(bdd.getZero());
		}
		
//		int availabilitySet = bdd.ref(bdd.getZero());
		int cube = getPrimedVariablesAndActionsCube();

		while(hasNextTransition(Player.ENVIRONMENT)){
			int transitionRelation = getNextEnvTransition();
			for(int i=0;i<envActions.length;i++){
				int availableTransitions = bdd.ref(bdd.and(envActions[i], transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				envAvlActions[i]=bdd.orTo(envAvlActions[i], states);
				bdd.deref(states);
				
			}
		}
		resetEnvTransVars();
		
		while(hasNextTransition(Player.SYSTEM)){
			int transitionRelation = getNextSysTransition();
			for(int i=0;i<sysActions.length;i++){
				int availableTransitions = bdd.ref(bdd.and(sysActions[i], transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				sysAvlActions[i]=bdd.orTo(sysAvlActions[i], states);
				bdd.deref(states);
				
			}
		}
		resetSysTransVars();
		
//		UtilityMethods.duration(t_avl, "availability sets computed in ", 500);
//		BDDWrapper.BDD_Usage(bdd);
	}
	
	/**
	 *TODO: clean actions, etc
	 * frees the memory associated with bdd formulas of the game
	 */
	public void cleanUp(){
		//free init
		BDDWrapper.free(bdd, init);
		//free transition relations 
		freeTransitionRelations();
		//free cubes
		freeCubes();
		resetCubes();
		//free action availability sets
		freeActionAvailabilitySets();
	}
	
	public void freeActionAvailabilitySets(){
		BDDWrapper.free(bdd,envAvlActions);
		BDDWrapper.free(bdd, sysAvlActions);
//		BDDWrapper.free(bdd, actions);
//		BDDWrapper.free(bdd, envActions);
//		BDDWrapper.free(bdd, sysActions);
	}
	
	public void freeTransitionRelations(){
//		BDDWrapper.free(bdd, getInit());
		if(T_env != -1) BDDWrapper.free(bdd, T_env);
		if(T_envList != null){
			for(Integer f : T_envList){
				BDDWrapper.free(bdd, f);
			}
		}
		if(T_sys != -1) BDDWrapper.free(bdd, T_sys);
		if(T_sysList != null){
			for(Integer f : T_sysList){
				BDDWrapper.free(bdd, f);
			}
		}
	}
	
	public void printActionsAvlSets(){
		System.out.println("env actions");
		for(int i=0;i<envActions.length;i++){
			UtilityMethods.debugBDDMethods(bdd, "action", envActions[i]);
			UtilityMethods.debugBDDMethods(bdd, "avl at ", envAvlActions[i]);
		}
		
		System.out.println("sys actions");
		for(int i=0;i<sysActions.length;i++){
			UtilityMethods.debugBDDMethods(bdd, "action", sysActions[i]);
			UtilityMethods.debugBDDMethods(bdd, "avl at ", sysAvlActions[i]);
		}
	}
	
	
	//Remove from this part on
//	/**
//	 * Computes the set of states where the action is available for the transition relation given in 
//	 * disjunctive form.
//	 * @param act
//	 * @param transitionRelation
//	 * @return
//	 */
//	public int actionAvailabilitySet(int act, ArrayList<Integer> disjunctiveTransitionRelation){
//		int availabilitySet = bdd.ref(bdd.getZero());
//		int cube = getPrimedVariablesAndActionsCube();
//		for(Integer transitionRelation : disjunctiveTransitionRelation){
//			int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
//			int states=bdd.ref(bdd.exists(availableTransitions, cube));
//			bdd.deref(availableTransitions);
//			availabilitySet=bdd.orTo(availabilitySet, states);
//			bdd.deref(states);
//		}
//		return availabilitySet;
//	}
	
//	public void systemActionAvailabilitySet(){
//	sysAvlActions = new int[sysActions.length];
//	if(T_sysList != null){
//		for(int i=0;i<sysActions.length;i++){
//			sysAvlActions[i]=actionAvailabilitySet(sysActions[i], T_sysList);
//		}
//	}else{
//		for(int i=0;i<actions.length;i++){
//			sysAvlActions[i]=actionAvailabilitySet(sysActions[i], getSystemTransitionRelation());
//		}
//	}
//}
//
//public void environmentActionAvailabilitySet(){
//	envAvlActions = new int[envActions.length];
//	if(T_envList != null){
//		for(int i=0;i<envActions.length;i++){
//			envAvlActions[i]=actionAvailabilitySet(envActions[i], T_envList);
//		}
//	}else{
//		for(int i=0;i<envActions.length;i++){
//			envAvlActions[i]=actionAvailabilitySet(envActions[i], T_env);
//		}
//	}
//}
	
//	public int reachable(int transitionRelation){
//	int Q=bdd.ref(bdd.getZero());
//	int Qprime=bdd.ref(bdd.and(init,bdd.getOne()));
//	int cube = getVariablesCube();
//	while(Q != Qprime){
//		bdd.deref(Q);
//		Q=bdd.ref(bdd.and(Qprime, bdd.getOne()));
//		int post = EpostImage(Qprime, cube, transitionRelation);
//		Qprime = bdd.orTo(Qprime, post);
//		bdd.deref(post);
//	}
//	bdd.deref(Qprime);
//	return Q;
//}
	
	
//	public int counterStrategy(int winningRegion, int objective){
//		int complementWinningRegion=bdd.ref(bdd.not(winningRegion));
//		int complementObjective=bdd.ref(bdd.not(objective));
//		int cs = synthesize(complementWinningRegion);
//		
//		int complementWinningRegionPrime=bdd.ref(bdd.replace(complementWinningRegion, vTovPrime));
//		int deadlocks=bdd.ref(bdd.and(complementObjective, complementWinningRegionPrime));
//		bdd.deref(complementWinningRegionPrime);
//		
////		int complementObjectivePrime=bdd.ref(bdd.replace(complementObjective, vTovPrime));
////		int deadlocks=bdd.ref(bdd.and(complementWinningRegion, complementObjectivePrime));
////		bdd.deref(complementObjectivePrime);
//		
//		bdd.deref(complementWinningRegion);
//		bdd.deref(complementObjective);
//		
//		return BDDWrapper.diff(bdd, cs, deadlocks);
//	}
	
//	public int getDeadEndStates(){
//		
//	}
	
//	public int counterStrategyReachable(int winningRegion, int objective){
//		int cs = counterStrategy(winningRegion, objective);
//		int reachable = reachable(cs);
//		int reachablePrime=bdd.ref(bdd.replace(reachable, vTovPrime));
//		int reachableTrans=bdd.ref(bdd.and(reachable,reachablePrime));
//		bdd.deref(reachablePrime);
//		int counterStrategy = bdd.ref(bdd.and(cs, reachableTrans));
//		bdd.deref(reachableTrans);
//		bdd.deref(cs);
//		return counterStrategy;
//	}
	
//	public int synthesize(int objective){
//		//compute the winning region
//		int winningRegion=greatestFixPoint(objective);
//		//if the game is realizable
//		if(BDDWrapper.subset(bdd, init, winningRegion)){
//			return synthesize(winningRegion);
//		}
//		return -1;
//	}

	
//	public int controllablePredecessor(int set){
//		if(primeOutputVarsCube==-1){
//			primeOutputVarsCube=andAll(outputPrimeVariables);
//		}
//		int cotrollableEpre=EpreImage(set, primeOutputVarsCube); 
//		
//		if(primeVariablesCube==-1){
//			primeVariablesCube=andAll(primedVariables);
//		}
////		if(primeInputVarsCube==-1){
////			primeInputVarsCube=andAll(inputPrimeVariables);
////		}
//		int uncontrollableApre=ApreImageNoReplacement(cotrollableEpre, primeVariablesCube);
//		bdd.deref(cotrollableEpre);
//		return uncontrollableApre;
//	}
	
	//TODO: remove this and CSguided class
	/**
	 * Computes the set of states where the action is available using the transition relation
	 * @param act
	 * @param transitionRelation
	 * @return
	 */
	public int actionAvailabilitySet(int act, int transitionRelation){
		int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
		int cube = bdd.ref(bdd.and(getActionsCube(), getPrimeVariablesCube()));
		int states=bdd.ref(bdd.exists(availableTransitions, cube));
		bdd.deref(availableTransitions);
		bdd.deref(cube);
		return states;
	}
	
	//TODO: remove this
	/**
	 * Computes the set of available actions for the given state based on the given transition relation
	 * @param state
	 * @return the set of available actions
	 */
	public int getAvailableActions(int state, int transitionRelation){
		int availableTransitions=bdd.ref(bdd.and(state, transitionRelation));
		int cube=getVariablesAndPrimedVariablesCube();
		int availableActions=bdd.ref(bdd.exists(availableTransitions, cube));
		bdd.deref(availableTransitions);
		return availableActions;
	}
	
	public int numOfVariables(){
		return variables.length+actionVars.length+primedVariables.length;
	}
	
	public static Game createGameForAgents(BDD bdd, Agent environmentAgent, Agent systemAgent){
		Variable[] gameVars = Variable.unionVariables(systemAgent.getVariables(), environmentAgent.getVariables());
		Variable[] gamePrimedVars = Variable.getPrimedCopy(gameVars);
		int init = BDDWrapper.and(bdd, systemAgent.getInit(), environmentAgent.getInit());
		int T_env = BDDWrapper.and(bdd, environmentAgent.getTransitionRelation(), systemAgent.getSame());
		int T_sys = BDDWrapper.and(bdd, systemAgent.getTransitionRelation(), environmentAgent.getSame());
		Game g = new Game(bdd, gameVars , gamePrimedVars, init, T_env, T_sys, environmentAgent.getActionVars(), systemAgent.getActionVars());
		return g;
	}
	
//	/**
//	 * Computes the set of states where the action is available for the transition relation given in 
//	 * disjunctive form.
//	 * @param act
//	 * @param transitionRelation
//	 * @return
//	 */
//	public int actionAvailabilitySet(int act, Player p){
//		int availabilitySet = bdd.ref(bdd.getZero());
//		int cube = getPrimedVariablesAndActionsCube();
//		if(p== Player.ENVIRONMENT){
//			while(hasNextTransition(p)){
//				int transitionRelation = getNextEnvTransition();
//				int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
//				int states=bdd.ref(bdd.exists(availableTransitions, cube));
//				bdd.deref(availableTransitions);
//				availabilitySet=bdd.orTo(availabilitySet, states);
//				bdd.deref(states);
//			}
//			resetEnvTransVars();
//		}else{
//			while(hasNextTransition(p)){
//				int transitionRelation = getNextSysTransition();
//				int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
//				int states=bdd.ref(bdd.exists(availableTransitions, cube));
//				bdd.deref(availableTransitions);
//				availabilitySet=bdd.orTo(availabilitySet, states);
//				bdd.deref(states);
//			}
//			resetSysTransVars();
//		}
//		return availabilitySet;
//	}
	
//	//TODO: remove this
//		public Game(BDD argBdd, Variable[] argInputVariables, Variable[] argOutputVariables, Variable[] argInputPrimedVars, Variable[] argOutputPrimedVars, int initial, int envTrans, int sysTrans, Variable[] argActions, int[] argSysAvl, int[] argEnvAvl){
//			bdd=argBdd;
//			inputVariables= argInputVariables;
//			outputVariables= argOutputVariables;
//			inputPrimeVariables = argInputPrimedVars;
//			outputPrimeVariables = argOutputPrimedVars;
//			init=initial;
//			T_env= envTrans;
//			T_sys= sysTrans;
////			T=bdd.ref(bdd.and(T_env, T_sys));
//			
//			int length = inputVariables.length + outputVariables.length;
//			variables=new Variable[length];
//			primedVariables= new Variable[length];
//			int numOfInputVars=inputVariables.length;
//			for(int i=0;i<length;i++){
//				if(i<numOfInputVars){
//					variables[i]=inputVariables[i];
//					primedVariables[i] = inputPrimeVariables[i];
//				}else{
//					variables[i]=outputVariables[i-numOfInputVars];
//					primedVariables[i] = outputPrimeVariables[i-numOfInputVars];
//				}
//			}
//			
//			vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
//			vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
//			
//			//cube for the variables, initially -1, meaning that they are not currently initialized
//			variablesCube=-1;
//			primeVariablesCube=-1;
//			inputVarsCube=-1;
//			outputVarsCube=-1;
//			primeInputVarsCube=-1;
//			primeOutputVarsCube=-1;
//			
//			actionVars=argActions;
//			sysAvlActions=argSysAvl;
//			envAvlActions=argEnvAvl;
//			actionsCube=-1;
//			
//			actions = enumerateActions();
//		}
//		
//		/**
//		 * A constructor for the cases where the input and output variables are not important, 
//		 * and only the set of variables (i.e., atomic propositions) matters.
//		 * @param argBdd
//		 * @param argVariables
//		 * @param argPrimedVars
//		 * @param initial
//		 * @param envTrans
//		 * @param sysTrans
//		 * @param argActions
//		 * @param argAvl
//		 */
//		public Game(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argActions, int[] argSysAvl, int[] argEnvAvl){
//			bdd=argBdd;
//			init=initial;
//			T_env= envTrans;
//			T_sys= sysTrans;
////			T=bdd.ref(bdd.and(T_env, T_sys));
//			variables = argVariables;
//			primedVariables = argPrimedVars;
//			vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
//			vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
//			
//			//cube for the variables, initially -1, meaning that they are not currently initialized
//			variablesCube=-1;
//			primeVariablesCube=-1;
//			actionVars=argActions;
//			sysAvlActions=argSysAvl;
//			envAvlActions=argEnvAvl;
//			actionsCube=-1;
//			
//			actions = enumerateActions();
//		}
	
}
