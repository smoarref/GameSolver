package game;


import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import parametric.ParametricGameStructure;
import specification.Agent;
import specification.Agent2D;
import specification.AgentType;
import utils.UtilityMethods;
import automaton.GameGraph;
import automaton.GameNode;
import automaton.Label;
import automaton.LabeledEdge;
import automaton.StringLabel;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

//TODO: change its name to something else, GameStructure class must be more general
public class GameStructure {




	/**
	 * TODO:
	 * 1) test post images
	 * 2) synthesizing strategies
	 * 3) strategy and counter-strategies as transition systems with initial states
	 * 4) implement uncontrollable predecessor
	 * 5) special cases where there is no system player or there is no environment player
	 * @author moarref
	 *
	 */

	//TODO: access to variables are direct now, and the get methods return bdd variables, revise later
		
		String id="";
		
		public Variable[] inputVariables; //input variables of the transition system
		public Variable[] outputVariables; //output variables of the transition system
		public Variable[] inputPrimeVariables; //primed version of the input variables
		public  Variable[] outputPrimeVariables;
		
		public Variable[] variables;
		public Variable[] primedVariables;
		
		protected int init; //bdd representing the initial states of the transition system
//		int T; //bdd representing the transition relation
		protected int T_env=-1; //transition relation for environment
		protected int T_sys=-1; //transition relation for the system player
		
		protected BDD bdd; //bdd object managing the binary decision diagrams
		protected Permutation vTovPrime; //permutation from variables to their prime version
		protected Permutation vPrimeTov; //permutation from primed version of the variables to their original form
		
		//cubes
		
		protected int inputVarsCube=-1;
		protected int outputVarsCube=-1;
		protected int primeInputVarsCube=-1;
		protected int primeOutputVarsCube=-1;
		
		protected int variablesCube=-1;
		protected int primeVariablesCube=-1;
		protected int variablesAndPrimedVariablesCube=-1;
		protected int variablesAndActionsCube=-1;
		protected int primedVariablesAndActionsCube=-1;
		
		//actions 
		
		public Variable[] actionVars;//TODO: this is actually action bits, rename it later
		public int[] sysAvlActions;
		public int[] envAvlActions;
		protected int actionsCube=-1;
		
		
		
		//for more natural visual representation of states and actions in the game graph
		protected HashMap<String, String> actionMap;
		protected HashMap<String, String> stateMap;
		
		protected HashMap<String, String> envActionMap;
		protected HashMap<String, String> sysActionMap;
		
		protected HashMap<String, String> envStateMap;
		protected HashMap<String, String> sysStateMap;
		
		//new code
		
		//We are going to represent the transition relation as disjunction of formulas
		protected ArrayList<Integer> T_envList;
		protected ArrayList<Integer> T_sysList;
		
		int[] actions;
		
		//Separating environment and system actions
		public Variable[] envActionVars;
		public Variable[] sysActionVars;
		public int[] envActions = null;
		public int[] sysActions = null;
		
		int envActionsCube=-1;
		int sysActionsCube=-1;
		
		int variablesAndEnvActionsCube=-1;
		int variablesAndSysActionsCube=-1;
		int primedVariablesAndEnvActionsCube=-1;
		int primedVariablesAndSysActionsCube=-1;
		

		
		public GameStructure(BDD argBdd){
			bdd=argBdd;
		}
		
		
		public void setActionMap(HashMap<String, String> argMap){
			actionMap = argMap;
		}
		
		public void setStateMap(HashMap<String, String> argMap){
			stateMap = argMap;
		}
		
		public void setActionMaps(HashMap<String, String> argEnvMap, HashMap<String, String> argSysMap){
			envActionMap = argEnvMap;
			sysActionMap = argSysMap;
		}
		
		public void setStateMaps(HashMap<String, String> argEnvMap, HashMap<String, String> argSysMap){
			envStateMap = argEnvMap;
			sysStateMap = argSysMap;
		}
		
		public HashMap<String, String> getEnvActionMap(){
			return envActionMap;
		}
		
		public HashMap<String, String> getSysActionMap(){
			return sysActionMap;
		}
		
		public HashMap<String, String> getEnvStateMap(){
			return envStateMap;
		}
		
		public HashMap<String, String> getSysStateMap(){
			return sysStateMap;
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
		
		public GameStructure(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argEnvActions, Variable[] argSysActions){
			bdd=argBdd;
			init=bdd.ref(initial);
			T_env= bdd.ref(envTrans);
			T_sys= bdd.ref(sysTrans);
//			T=bdd.ref(bdd.and(T_env, T_sys));
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
			
			if(envActionVars != null && envActionVars.length!=0){
				envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
			}
//			else{
//				System.out.println("environment player has no defined actions in this game structure!");
//			}
			if(sysActionVars != null && sysActionVars.length != 0){
				sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
			}
			if(actionVars!=null){
				actions = enumerateActions();
			}
			
			T_envList=new ArrayList<Integer>();
			T_envList.add(bdd.ref(T_env));
			T_sysList=new ArrayList<Integer>();
			T_sysList.add(bdd.ref(T_sys));
			
			actionAvailabilitySets();	
		}
		
		public GameStructure(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, ArrayList<Integer> argT_envList, ArrayList<Integer> argT_sysList, Variable[] argEnvActions, Variable[] argSysActions){
			bdd=argBdd;
//			T=bdd.ref(bdd.and(T_env, T_sys));
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
			if(init != -1) bdd.deref(getInit());
			init=bdd.ref(newInit);
		}
		
		public static void main(String[] args) {
			//SimpleRobotMotionPlanningTestCaseGenerator.testRMP(3);
			
//			BDD bdd = new BDD(10000,1000);
//			Game game = TestCaseGenerator.simpleNondeterministicGame(bdd);
//			game.toGameGraph().draw("ndgame.dot", 1);
			
//			Tester.simpleNDGameSolverTest();
		}
		
		
		public GameStructure removeUnreachableStates(){
			
			int reachable = reachable();
			int newT_env = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), reachable));
			
			int cube = getVariablesAndActionsCube();
			int reachableSystemStates = EpostImage(reachable, cube , getEnvironmentTransitionRelation());

			int newT_sys =  bdd.ref(bdd.and(getSystemTransitionRelation(), reachableSystemStates));
			GameStructure newGame=new GameStructure(bdd, variables, primedVariables, init, newT_env, newT_sys, envActionVars, sysActionVars);
			newGame.setActionMap(actionMap);
			return newGame;
		}
		

		
		public int reachable(){
			int Q=bdd.ref(bdd.getZero());
			int Qprime = bdd.ref(getInit());
			while(Q != Qprime){
				bdd.deref(Q);
				Q = bdd.ref(Qprime);
				int post = symbolicGameOneStepExecution(Qprime);
				Qprime = bdd.orTo(Qprime, post);
				bdd.deref(post);
			}
			bdd.deref(Qprime);
			return Q;
		}
		
		public int reachable(int init){
			int Q=bdd.ref(bdd.getZero());
			int Qprime = bdd.ref(init);
			while(Q != Qprime){
				bdd.deref(Q);
				Q = bdd.ref(Qprime);
				int post = symbolicGameOneStepExecution(Qprime);
				Qprime = bdd.orTo(Qprime, post);
				bdd.deref(post);
			}
			bdd.deref(Qprime);
			return Q;
		}

		
		public int controllablePredecessor(int set){
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "computing the cpre of ", set);
			
			int cube = getPrimedVariablesAndActionsCube();
			
			int controllableEpre = bdd.ref(bdd.getZero());
			
			for(int act : sysActions){
				int controllableAct = ApreImage(set, act, cube, getSystemTransitionRelation());
				controllableEpre = bdd.orTo(controllableEpre, controllableAct);
				bdd.deref(controllableAct);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "controllableEpre is ", controllableEpre);

//			int controllabelEpre = EpreImage(set, cube, T_sys);
//			System.out.println("system can push to ");
//			bdd.printSet(controllabelEpre);
			
			int controllablePredecessor=ApreImage(controllableEpre, cube, getEnvironmentTransitionRelation());
//			bdd.deref(cube);
			bdd.deref(controllableEpre);
//			System.out.println("environment cannot avoid from ");
//			bdd.printSet(controllablePredecessor);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "cpre computed ", controllablePredecessor);
			
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
		
//		public int ApostImage(int set){
//			return -1;
//		}
		

		
		public int ApreImage(int set, int cube, int trans){
			int statesWithTransitionToSet = EpreImage(set, cube, trans);
			
//			System.out.println("pre image computed");
//			bdd.printSet(statesWithTransitionToSet);
			
			int temp = bdd.ref(bdd.not(set));
			int statesWithTransitionsToComplementSet= EpreImage(temp, cube, trans);
			
//			System.out.println("pre image of complement of the set computed");
//			bdd.printSet(statesWithTransitionsToComplementSet);
			
			bdd.deref(temp);
			int diff= BDDWrapper.diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
			
//			System.out.println("the diff was computed");
//			bdd.printSet(diff);
			
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
//			bdd.printSet(targetSetPrime);
			int temp = bdd.ref(bdd.and(targetSetPrime, trans));
//			System.out.println("Trans is ");
//			bdd.printSet(trans);
//			bdd.printSet(temp);
			int preImage=bdd.ref(bdd.exists(temp, cube));
			bdd.deref(temp);
			bdd.deref(targetSetPrime);
			
			return preImage;
		}
		
		
		public int EpreImage(int set, int cube, ArrayList<Integer> transList){
			int preImage=bdd.ref(bdd.getZero());
			for(int trans : transList){		
				int targetSetPrime=bdd.ref(bdd.replace(set, vTovPrime));
//				bdd.printSet(targetSetPrime);
				int temp = bdd.ref(bdd.and(targetSetPrime, trans));
//				System.out.println("Trans is ");
//				bdd.printSet(trans);
//				bdd.printSet(temp);
				int pre=bdd.ref(bdd.exists(temp, cube));
				preImage=bdd.orTo(preImage, pre);
				bdd.deref(pre);
				bdd.deref(temp);
				bdd.deref(targetSetPrime);
			}
			return preImage;
		}
		
//		public int EpreImage(int set, int cube, Player p){
//			int preImage=bdd.ref(bdd.getZero());
//			if(p == Player.ENVIRONMENT){
//				while(hasNextTransition(p)){
//					int trans = getNextEnvTransition();				
//					int pre = EpreImage(set,cube,trans);
//					preImage=bdd.orTo(preImage, pre);
//					bdd.deref(pre);
//				}
//			}else{
//				while(hasNextTransition(p)){
//					int trans = getNextSysTransition();
//					int pre = EpreImage(set,cube,trans);
//					preImage=bdd.orTo(preImage, pre);
//					bdd.deref(pre);
//				}
//			}
//			return preImage;
//		}
		
		

		
		public int EpostImage(int set, int cube, int trans){
			
//			if(trans == getEnvironmentTransitionRelation()){
//				System.out.println("postImage, envTrans");
//				UtilityMethods.debugBDDMethods(bdd, "env trans", getEnvironmentTransitionRelation());
//			}
			int temp = bdd.ref(bdd.and(set, trans));
			
//			UtilityMethods.debugBDDMethods(bdd, "EpostImage, cube is", cube);
//			UtilityMethods.debugBDDMethods(bdd, "EpostImage, Epost, trans is", trans);
//			UtilityMethods.debugBDDMethods(bdd, "EpostImage, Epost, set is", set);
//			UtilityMethods.debugBDDMethods(bdd, "EpostImage, temp is", temp);
			
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
		
		/**
		 * Executes a one-step symbolic step backward from the given set on the game structure
		 * @param set
		 * @return
		 */
		public int symbolicOneStepBackwardExecution(int set){
			int cube=getPrimedVariablesAndActionsCube();

			int sysMoves = EpreImage(set, cube, getSystemTransitionRelation());
			int envMoves = EpreImage(sysMoves, cube, getEnvironmentTransitionRelation());
			
			bdd.deref(sysMoves);
			return envMoves;
		}
		
		public int symbolicGameOneStepExecution(int set){
			int cube=getVariablesAndActionsCube();
//			int envMoves = EpostImage(set, cube, getEnvironmentTransitionRelation());
//			int sysMoves = EpostImage(envMoves, cube, getSystemTransitionRelation());
			
			int envMoves = EpostImage(set, cube, getT_envList());
			int sysMoves = EpostImage(envMoves, cube, getT_sysList());
			
			bdd.deref(envMoves);
			return sysMoves;
		}
		
		private int andAll(int[] vars){
			int result=bdd.ref(bdd.getOne());
			if(vars!=null){
				for(int i=0;i<vars.length;i++){
					result=bdd.andTo(result, vars[i]);
				}
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
		
		public void setSystemTransitionRelation(int newT_sys){
			if(T_sys == -1){
				BDDWrapper.free(bdd, T_sys);
			}
			T_sys = bdd.ref(newT_sys);
		}
		
		public void setEnvironmentTransitionRelation(int newT_env){
			if(T_env == -1){
				BDDWrapper.free(bdd, T_env);
			}
			T_env = bdd.ref(newT_env);
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
//		    	else{
//		    		state=parseLabel(state, variables);
//			    	nextState=parseLabel(nextState, variables);
//		    	}
		    	
		    	if(actionMap != null){
		    		action=getActionName(action);
		    	}else{
		    		action = parseLabel(action, actionVars);
		    	}
		    	
		    	
//		    	System.out.println("state: "+state+" & next state: "+nextState+" & action is "+action);
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
//		    	else{
//		    		state=parseLabel(state, variables);
//			    	nextState=parseLabel(nextState, variables);
//		    	}
		    	
		    	if(actionMap != null){
		    		action=getActionName(action);
		    	}else{
		    		action = parseLabel(action, actionVars);
		    	}
		    	
//		    	System.out.println("state: "+state+" & next state: "+nextState);
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
//			System.out.println("label is "+label);
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
//			System.out.println("result is "+result);
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
			if(init != -1) UtilityMethods.debugBDDMethods(bdd, "init", init);
			if(T_env != -1) UtilityMethods.debugBDDMethods(bdd, "T_env", getEnvironmentTransitionRelation());
//			UtilityMethods.getUserInput();
			if(T_sys != -1) UtilityMethods.debugBDDMethods(bdd, "T_sys", getSystemTransitionRelation());
//			UtilityMethods.getUserInput();
			
//			System.out.println("\nin formula form");
//			Variable[] vars = Variable.unionVariables(this.actionVars, this.variables);
//			vars=Variable.unionVariables(vars, primedVariables);
//			
//			System.out.println("init");
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, getInit(), vars));
//			System.out.println("T_env");
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, getEnvironmentTransitionRelation(), vars));
//			System.out.println("T_sys");
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, getSystemTransitionRelation(), vars));
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
		
//		/**
//		 * given the set of equivalence classes, return the abstraction of the game
//		 * @param eqClasses
//		 * @return
//		 */
//		public AbstractGame computeAbstraction(int[] eqClasses){
//			return new AbstractGame(bdd, eqClasses, this);
//		}
//		
//		
//		public AbstractGame computeAbstractionFromAbstractPredicates(int[] abstractPredicates, boolean approximate){
//			return new AbstractGame(bdd, abstractPredicates, this, approximate);
//		}
		
		/**
		 * compose the input game with the current game and returns the composed game
		 * @param game
		 * @return
		 */
		public GameStructure compose(GameStructure game){
			Variable[] gameVariables = Variable.unionVariables(variables, game.variables);
			Variable[] gamePrimeVariables=Variable.unionVariables(primedVariables, game.primedVariables);
			
//			for(Variable v : gameVariables){
//				System.out.println(v.toString());
//			}
//			UtilityMethods.getUserInput();
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
			
//			int gameEnvTrans = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), game.getEnvironmentTransitionRelation()));
//			int gameSysTrans = bdd.ref(bdd.and(getSystemTransitionRelation(), game.getSystemTransitionRelation()));
			
//			Variable[] gameActionVars = Variable.unionVariables(actionVars, game.actionVars);
			Variable[] gameEnvActionVars= Variable.unionVariables(envActionVars, game.envActionVars);
			Variable[] gameSysActionVars= Variable.unionVariables(sysActionVars, game.sysActionVars);
			
			
			GameStructure composedGame= new GameStructure(bdd, gameVariables, gamePrimeVariables, gameInit, gameEnvTrans, gameSysTrans, gameEnvActionVars, gameSysActionVars);
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
		
		public GameStructure gameWithBoundedReachabilityObjective(int bound, String namePrefix, int targetStates){
			int targetStatesPrime = BDDWrapper.replace(bdd, targetStates, getVtoVprime());
			return gameWithBoundedReachabilityObjective(bound, namePrefix, targetStates, targetStatesPrime);
		}
		
		//altering games to encode bounded reachability objectives
		public GameStructure gameWithBoundedReachabilityObjective(int bound, String namePrefix, int targetStates, int targetStatesPrime ){
			
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
			
//			UtilityMethods.debugBDDMethods(bdd, "reachTransitions", reachTrans1);
			
			//create game based on reachability obj
			GameStructure boundedReachabilityGame=new GameStructure(bdd, vars, primedVars, boundedReachabilityGameInit, boundedReachabilityGameT_env, boundedReachabilityGameT_sys, this.envActionVars, this.sysActionVars);
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
//			Variable flagPrime = new Variable(bdd,namePrefix+"fPrime");
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
//			UtilityMethods.debugBDDMethods(bdd, "reachability and counter", boundedReachabilityFormula);
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
		
		
		public void actionAvailabilitySets(){
//			System.out.println("computing actions availability sets");
//			long t_avl = UtilityMethods.timeStamp(); 
			
			
//			int availabilitySet = bdd.ref(bdd.getZero());
			int cube = getPrimedVariablesAndActionsCube();
			
			if(envActions != null){
				envAvlActions = new int[envActions.length];
				for(int i=0; i<envAvlActions.length;i++){
					envAvlActions[i]= bdd.ref(bdd.getZero());
				}
				int transitionRelation = getEnvironmentTransitionRelation();
				for(int i=0;i<envActions.length;i++){
					int availableTransitions = bdd.ref(bdd.and(envActions[i], transitionRelation));
					int states=bdd.ref(bdd.exists(availableTransitions, cube));
					bdd.deref(availableTransitions);
					envAvlActions[i]=bdd.orTo(envAvlActions[i], states);
					bdd.deref(states);
						
				}
			}
			
			if(sysActions != null){
				sysAvlActions = new int[sysActions.length];
				for(int i=0;i<sysAvlActions.length;i++){
					sysAvlActions[i]= bdd.ref(bdd.getZero());
				}
				int transitionRelation = getSystemTransitionRelation();
				for(int i=0;i<sysActions.length;i++){
					int availableTransitions = bdd.ref(bdd.and(sysActions[i], transitionRelation));
					int states=bdd.ref(bdd.exists(availableTransitions, cube));
					bdd.deref(availableTransitions);
					sysAvlActions[i]=bdd.orTo(sysAvlActions[i], states);
					bdd.deref(states);
						
				}
			}
			
			
//			UtilityMethods.duration(t_avl, "availability sets computed in ", 500);
//			BDDWrapper.BDD_Usage(bdd);
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
//			BDDWrapper.free(bdd, actions);
//			BDDWrapper.free(bdd, envActions);
//			BDDWrapper.free(bdd, sysActions);
		}
		
		public void freeTransitionRelations(){
//			BDDWrapper.free(bdd, getInit());
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
		
		//Forming a game structure from give set of Agents. Assumes that uncontrollable agents play first, 
		//then controllable agents play
		public static GameStructure createGameForAgents(BDD bdd, Agent environmentAgent, Agent systemAgent){
			Variable[] gameVars = Variable.unionVariables(environmentAgent.getVariables(), systemAgent.getVariables());
			Variable[] gamePrimedVars = Variable.getPrimedCopy(gameVars);
			int init = BDDWrapper.and(bdd, systemAgent.getInit(), environmentAgent.getInit());
			int T_env = BDDWrapper.and(bdd, environmentAgent.getTransitionRelation(), systemAgent.getSame());
			int T_sys = BDDWrapper.and(bdd, systemAgent.getTransitionRelation(), environmentAgent.getSame());
			GameStructure g = new GameStructure(bdd, gameVars , gamePrimedVars, init, T_env, T_sys, environmentAgent.getActionVars(), systemAgent.getActionVars());
			return g;
		}
		
		public static GameStructure createGameForAgents(BDD bdd, ArrayList<Agent2D> agents){
			Variable[] gameVars = agents.get(0).getVariables();
			for(int i=1; i<agents.size();i++){
				gameVars = Variable.unionVariables(gameVars, agents.get(i).getVariables());
			}
			Variable[] primeGameVars = Variable.getPrimedCopy(gameVars);
			int init = bdd.ref(bdd.getOne());
			int T_env = bdd.ref(bdd.getOne());
			int T_sys = bdd.ref(bdd.getOne());
			Variable[] envActionVars=null;
			Variable[] sysActionVars=null;
			for(Agent agent : agents){
				init = BDDWrapper.andTo(bdd, init, agent.getInit());
				if(agent.getType() == AgentType.Uncontrollable){
					T_env = BDDWrapper.andTo(bdd, T_env, agent.getTransitionRelation());
					T_sys = BDDWrapper.andTo(bdd, T_sys, agent.getSame());
					if(envActionVars == null){
						envActionVars = agent.getActionVars();
					}else{
						envActionVars = Variable.unionVariables(envActionVars, agent.getActionVars());
					}
				}else{
					T_env = BDDWrapper.andTo(bdd, T_env, agent.getSame());
					T_sys = BDDWrapper.andTo(bdd, T_sys, agent.getTransitionRelation());
					if(sysActionVars == null){
						sysActionVars = agent.getActionVars();
					}else{
						sysActionVars = Variable.unionVariables(sysActionVars, agent.getActionVars());
					}
				}
			}
			
			GameStructure g = new GameStructure(bdd, gameVars, primeGameVars, init, T_env, T_sys, envActionVars, sysActionVars);
			return g;
		}
		
		
		//Checking that all traces of the game structure satisfies a safety formula
		public boolean safetyModelCheck(int safetyFormula){
//			System.out.println("model checking");
			
			int Q = bdd.ref(getInit());
			int QPrime = bdd.ref(bdd.getZero());
			do{
				
//				UtilityMethods.debugBDDMethods(bdd, "Q is", Q);
//				UtilityMethods.debugBDDMethods(bdd, "Qprime is", QPrime);
//				UtilityMethods.getUserInput();
		
				if(!BDDWrapper.subset(bdd, Q, safetyFormula)){
					return false;
				}
				bdd.deref(QPrime);
				QPrime = Q;
				int tmp = symbolicGameOneStepExecution(Q);
				Q = BDDWrapper.or(bdd, Q, tmp);
				BDDWrapper.free(bdd, tmp);
			}while(Q != QPrime);
			bdd.deref(QPrime);
			bdd.deref(Q);
			return true;
		}
		
		public boolean checkSafety(int safetyFormula){
			System.out.println("*************************");
			System.out.println("\nchecking the solution");
//			bdd.printSet(badStates);
			int t = getSystemTransitionRelation();
			int objPrime = bdd.ref(bdd.replace(safetyFormula, vTovPrime));
			int badStates = bdd.ref(bdd.and(t, bdd.not(objPrime)));
			if(badStates != 0 ){
				System.out.println("something is wrong in the synthesized strategy!!:(");
				return false;
			}else{
				System.out.println("evrything seems fine!:)");
				return true;
			}
		}
		
		//Composes the controller with the game structure
		//assumes that the controller and the game are defined over the same set of variables and actions
		//TODO: generalize
		public GameStructure composeWithController(int controller){
			int composedT_sys = BDDWrapper.and(bdd, getSystemTransitionRelation(), controller);
			GameStructure result = new GameStructure(bdd, variables, primedVariables, getInit(), getEnvironmentTransitionRelation(), composedT_sys, envActionVars, sysActionVars);
			return result;
		}
		
		public GameStructure composeWithController(Controller controller){
			Variable[] gameVars = Variable.unionVariables(variables, controller.getVariables());
			Variable[] primedGameVars = Variable.getPrimedCopy(gameVars);
			//in case the controller refers to actions that are not already in this game structure
			Variable[] gameSysActionVars = Variable.unionVariables(sysActionVars, controller.getActionVars());
			
			//if the controller has additional variables which are not in the game structure, then they
			//must remain unchanged during environment transitions
			Variable[] controllerSpecificVariables = Variable.difference(controller.getVariables(), variables);
			int same = BDDWrapper.same(bdd, controllerSpecificVariables);
			int composedT_env = BDDWrapper.and(bdd, getEnvironmentTransitionRelation(), same);
			BDDWrapper.free(bdd, same);
			
			int composedT_sys = BDDWrapper.and(bdd, getSystemTransitionRelation(), controller.getTransitionRelation());
			GameStructure result = new GameStructure(bdd, gameVars, primedGameVars, getInit(), composedT_env, composedT_sys, envActionVars, gameSysActionVars);
			return result;
		}
		
		/**
		 * Given a game structure and a set of parameters, returns a ParametricGameStructure
		 * @param parameters
		 * @return
		 */
		public ParametricGameStructure generateParametricGameStructure(Variable[] parameters){
			int paramTrans = BDDWrapper.same(bdd, parameters, Variable.getPrimedCopy(parameters));
			Variable[] parametricGameVariables = Variable.unionVariables(variables, parameters);
			int parametricT_env = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), paramTrans));
			int parametricT_sys = bdd.ref(bdd.and(getSystemTransitionRelation(), paramTrans));
			BDDWrapper.free(bdd, paramTrans);
			return new ParametricGameStructure(bdd, parametricGameVariables, 
					bdd.ref(init), parametricT_env, parametricT_sys, envActionVars, 
					sysActionVars, parameters, this);
		}
		
		public boolean includePrimeVariable(int formula){
			int existQuantifyPrimeVars = BDDWrapper.exists(bdd, formula, getPrimeVariablesCube());
			if(formula == existQuantifyPrimeVars){
				return false;
			}
			return true;
		}
		
		public int transitionsEndingInGoalStates(int goalStates){
			int goalStatesPrime = BDDWrapper.replace(bdd, goalStates, getVtoVprime());
			int result = BDDWrapper.and(bdd, getSystemTransitionRelation(), goalStatesPrime);
			BDDWrapper.free(bdd, goalStatesPrime);
			return result;
		}
		
		public int turnIntoTransitionFormula(int formula){
			System.out.println("this method is not defined for generic game structure!");
			return formula;
		}
		
//		//Forming a game structure from give set of Agents. Assumes that uncontrollable agents play first, 
//		//then controllable agents play
//		public static GameStructure createGameStructureFromAgents(BDD bdd, Agent uncontrollable, Agent controllable){
//			Variable[] vars = Variable.unionVariables(uncontrollable.getVariables(), controllable.getVariables());
//			Variable[] primedVars = Variable.getPrimedCopy(vars);
//			int init = BDDWrapper.and(bdd, uncontrollable.getInit(), controllable.getInit());
//			int envTrans = BDDWrapper.and(bdd, uncontrollable.getTransitionRelation(), controllable.getSame());
//			int sysTrans = BDDWrapper.and(bdd, uncontrollable.getSame(), controllable.getTransitionRelation());
//			GameStructure result = new GameStructure(bdd, vars, primedVars, init, envTrans, sysTrans, uncontrollable.getActionVars(), controllable.getActionVars());
//			return result;
//		}
	}

