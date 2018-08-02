package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.rmi.CORBA.Util;

import specification.Agent2D;
import specification.GridCell2D;
import testsAndCaseStudies.SampleGameStructureGenerator;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import jdd.sat.Var;

public class TurnBasedPartiallyObservableGameStructure {
	
	String id="";
	protected BDD bdd; //bdd object managing the binary decision diagrams
	GameStructure perfectInformationGameStructure;
	GameStructure knowledgeGameStructure = null;
	
//	public Variable[] variables;
//	public Variable[] primedVariables;
//	
//	int T_env = -1;
//	int T_sys = -1;
//	
//	
//	Permutation vTovPrime; //permutation from variables to their prime version
//	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
//	
//	//actions 
//	public Variable[] actionVars;//TODO: this is actually action bits, rename it later
//	public int[] sysAvlActions;
//	public int[] envAvlActions;
//	int actionsCube=-1;
//	
//	public Variable[] envActionVars;
//	public Variable[] sysActionVars;
//	public int[] envActions;
//	public int[] sysActions;
//	
//	//cube 
//	int variablesCube=-1;
//	int primeVariablesCube=-1;
//	int variablesAndPrimedVariablesCube=-1;
//	int variablesAndActionsCube=-1;
//	int primedVariablesAndActionsCube=-1;
//	
//	int envActionsCube=-1;
//	int sysActionsCube=-1;
	
	
	//observable variables
	public Variable[] observableVars;
//	public Variable[] primedObservableVars;
//	
//	Permutation observableVtoVprime;
//	Permutation observableVprimeToV;
	
	public int observableVarsCube=-1;
	
	//observation function that maps observables to sets of states
	int observationMap = -1;
	
	
	//maps between states of the knowledge game and states of the partially observable game structure
//	HashMap<Integer, Integer> knowledgeGameEnvironmentStatesToPartiallyObservableGameStructureStates;
//	HashMap<Integer, Integer> knowledgeGameSystemStatesToPartiallyObservableGameStructureStates;
//	
//	HashMap<Integer, Integer> partiallyObservableGameStructureEnvironmentStatesToknowledgeGameStates;
//	HashMap<Integer, Integer> partiallyObservableGameStructureSystemStatesToknowledgeGameStates;
	
	HashMap<Integer, Integer> knowledgeGameStatesToPartiallyObservableGameStructureStates;
	HashMap<Integer, Integer> partiallyObservableGameStructureStatesToknowledgeGameStates;
	
	//keeps track of sets of states that are expanded during the search. 0->environment set, 1-> system set
	HashMap<Integer, Boolean[]> observedSetAlreadyExpanded;
	
	public String getID(){
		return id;
	}
	
	public GameStructure getUnderlyingGameStructure(){
		return perfectInformationGameStructure;
	}
	
	public GameStructure getKnowledgeGameStructure(){
		return knowledgeGameStructure;
	}
	
	public HashMap<Integer, Integer> getKnowledgeGameStatesToPartiallyObservableGameStructureStates(){
		return knowledgeGameStatesToPartiallyObservableGameStructureStates;
	}
	
	public HashMap<Integer, Integer> getPartiallyObservableGameStructureStatesToKnowledgeGameStates(){
		return partiallyObservableGameStructureStatesToknowledgeGameStates;
	}
	
	public void setID(String argId){
		id = argId;
	}
	
	public int getObservableVarsCube(){
		if(observableVarsCube == -1){
			observableVarsCube = BDDWrapper.createCube(bdd, observableVars);
		}
		return observableVarsCube;
	}
	
	//TODO: there was an odd bug, so if part was removed, investigate why
//	public int getVariablesAndActionsCube(){
////		if(variablesAndActionsCube==-1){
////			Variable[] varsAndActs = Variable.unionVariables(variables, actionVars);
////			variablesAndActionsCube = BDDWrapper.createCube(bdd, varsAndActs);
//			variablesAndActionsCube=bdd.ref(bdd.and(getVariablesCube(), getActionsCube()));
////		}
//		return variablesAndActionsCube;
//	}
//	
//	public int getVariablesCube(){
//		if(variablesCube==-1){
//			variablesCube=BDDWrapper.createCube(bdd, variables);
//		}
//		return variablesCube;
//	}
//	
//	public int getActionsCube(){
//		if(actionsCube==-1){
//			actionsCube=BDDWrapper.createCube(bdd, actionVars);
//		}
//		return actionsCube;
//	}
//	
//	public int[] getVariables(){
//		return Variable.getBDDVars(variables);
//	}
//	
//	public int[] getActionVars(){
//		return Variable.getBDDVars(actionVars);
//	}
	
	public TurnBasedPartiallyObservableGameStructure(BDD argBdd, GameStructure perfectInformationGame, Variable[] argObservableVars, 
			int argObservationMap ){
		bdd=argBdd;
		perfectInformationGameStructure = perfectInformationGame;
		
		observableVars = argObservableVars;
		observationMap = bdd.ref(argObservationMap);
		
	}
	
//	public TurnBasedPartiallyObservableGameStructure(BDD argBdd, 
//			Variable[] argVariables, int envTrans, int sysTrans, 
//			Variable[] argEnvActionVars, Variable[] argSysActionVars, 
//			Variable[] argObservableVars, 
//			int argObservationMap ){
//		
//		bdd=argBdd;
//		T_env= bdd.ref(envTrans);
//		T_sys= bdd.ref(sysTrans);
//		variables = argVariables;
//		primedVariables = Variable.getPrimedCopy(variables);
//		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
//		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
//		
//		//cube for the variables, initially -1, meaning that they are not currently initialized
//		variablesCube=-1;
//		primeVariablesCube=-1;
//	
//		envActionVars = argEnvActionVars;
//		sysActionVars = argSysActionVars;
//		envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
//		sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
//		
//		actionVars=Variable.unionVariables(envActionVars, sysActionVars);
//		actionsCube=-1;
//		
//		observableVars = argObservableVars;
////		primedObservableVars = Variable.getPrimedCopy(observableVars);
////		
////		observableVtoVprime = bdd.createPermutation(Variable.getBDDVars(observableVars), Variable.getBDDVars(primedObservableVars));
////		observableVprimeToV = bdd.createPermutation(Variable.getBDDVars(primedObservableVars), Variable.getBDDVars(observableVars));
//		
//		observationMap = bdd.ref(argObservationMap);
//	}
//	
	
	public void restrictKnowledgeGame(int strategy){
		if(knowledgeGameStructure == null){
			return;
		}
		knowledgeGameStructure = knowledgeGameStructure.composeWithController(strategy);
	}
	
	/**
	 * Creates the knowledge game structure (perfect information game strcuture) 
	 * starting from the given initial state (assumed to be environment state)
	 * corresponding to this partially observable game through a symbolic subset construction
	 * 
	 * Assumes that the game has a unique initial state
	 * Assumes that all the actions are available in all states
	 * TODO: implement the one-to-one map between knowledge game structure and partially observable game structure states 
	 * using bi-maps
	 * TODO: optimize and check for ref/derefs
	 * TODO: optimize the number of variables based on knowledgeGameVarCounter
	 * @return
	 */
	public GameStructure createKnowledgeGame(int initialState){
		Variable[] variables = perfectInformationGameStructure.variables;
		int n = variables.length;
		
		//compute number of variables
		//upper bound
//		int numOfKnowledgeVars = (int) Math.pow(2, variables.length);
		
		//better estimate
//		int n_pow_2 = (int) Math.pow(2, n/2);
//		System.out.println("n_pow_2 "+n_pow_2);
//		int n_pow_2_pow_2 = (int) Math.pow(2, n_pow_2-4);
//		System.out.println("n_pow_2_pow_2: "+n_pow_2_pow_2);
//		int est = 9*n_pow_2+n_pow_2*n_pow_2_pow_2;
//		System.out.println("est is: "+est );
//		int numOfKnowledgeVars = UtilityMethods.numOfBits(est);
		
		//even better estimate
		int dim = (int) Math.pow(2, n/4);
		int numOfKnowledgeVars = 2*UtilityMethods.numOfBits(dim)+dim^2;
		
		System.out.println("num of knowledge vars "+numOfKnowledgeVars);
		
		Variable[] kVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfKnowledgeVars, "kVars");
		Variable[] primedKVars = Variable.getPrimedCopy(kVars);
		Permutation kVarsToPrimedKVars = BDDWrapper.createPermutations(bdd, kVars, primedKVars);
		
		System.out.println("num of vars  is "+variables.length);
		System.out.println("kVars num is "+kVars.length);
//		Variable.printVariables(kVars);
		UtilityMethods.debugBDDMethods(bdd, "initial state", initialState);
		UtilityMethods.getUserInput();
		
		
		
		//the counter that is used to assign a new value to a variable in partially observable game structure
		int knowledgeGameVarCounter=0;
		
//		knowledgeGameEnvironmentStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		knowledgeGameSystemStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureEnvironmentStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureSystemStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
		
		knowledgeGameStatesToPartiallyObservableGameStructureStates= new HashMap<Integer, Integer>();
		partiallyObservableGameStructureStatesToknowledgeGameStates= new HashMap<Integer, Integer>();
		
		observedSetAlreadyExpanded = new HashMap<Integer, Boolean[]>();
		
		int kT_env = bdd.ref(bdd.getZero());
		ArrayList<Integer> environmentWaitingStates = new ArrayList<Integer>();
		environmentWaitingStates.add(bdd.ref(initialState));
		
		observedSetAlreadyExpanded.put(bdd.ref(initialState), new Boolean[]{false, false});
		
		
		int kT_sys = bdd.ref(bdd.getZero());
		ArrayList<Integer> systemWaitingStates = new ArrayList<Integer>();
		
		//initialize maps
		int initialKnowledgeGameState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
		knowledgeGameVarCounter++;
		knowledgeGameStatesToPartiallyObservableGameStructureStates.put(initialKnowledgeGameState, bdd.ref(initialState));
		partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(initialState), initialKnowledgeGameState);
		
		
		
		while(!environmentWaitingStates.isEmpty() || !systemWaitingStates.isEmpty()){
			
			//define kT_env, the environment transition relation for the knowledge game 
			if(!environmentWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = environmentWaitingStates.remove(0);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.ENVIRONMENT);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				//compute the post image for each environment action
//				for(int i=0; i<envActions.length; i++){
//					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, envActions[i]);
//					
//					UtilityMethods.debugBDDMethods(bdd, "current state and act is ", currentStatesAndAct);
					
//					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, getVariablesAndActionsCube(), T_env, vPrimeTov);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to exapnd", statesToExpand);
//				UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
//				UtilityMethods.debugBDDMethods(bdd, "variables cube is ", getVariablesCube());
//				UtilityMethods.debugBDDMethods(bdd, "actions cube ",getActionsCube());
				int postImage = BDDWrapper.EpostImage(bdd, statesToExpand, perfectInformationGameStructure.getVariablesAndActionsCube(), 
						perfectInformationGameStructure.getEnvironmentTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
				
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					
//					BDDWrapper.free(bdd, currentStatesAndAct);
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					
//					System.out.println("before enumerate");
//					Variable.printVariables(observableVars);
//					UtilityMethods.debugBDDMethods(bdd, "relevant observation", relevantObservations);
					
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
//						UtilityMethods.debugBDDMethods(bdd, "observed partition is ", observedPartitionOfPostImage);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[1]==false){
							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state source is ", knowledgeStateSource);
//						UtilityMethods.debugBDDMethods(bdd, "current action is ", envActions[i]);
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state destination is", knowledgeState);
						
//						int trans = BDDWrapper.and(bdd, knowledgeStateSource, envActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, knowledgeStateDestination);
//						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_env = BDDWrapper.orTo(bdd, kT_env, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
//					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
					
					
				}
//			}
			
			int[] sysActions = perfectInformationGameStructure.getSysActions();
			
			//define kT_sys, the system transition relation for the knowledge game 
			if(!systemWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = systemWaitingStates.remove(0);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.SYSTEM);
				
				//compute the post image for each environment action
				for(int i=0; i<sysActions.length; i++){
					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, sysActions[i]);
//					UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, perfectInformationGameStructure.getVariablesAndActionsCube(), 
							perfectInformationGameStructure.getSystemTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
					BDDWrapper.free(bdd, currentStatesAndAct);
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					//TODO: check for correctness of following step
					if(postImage==0) continue;
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[0]==false){
							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, sysActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_sys = BDDWrapper.orTo(bdd, kT_sys, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
				}
			}
			
			
		}
		
		//adjust the size of the kVars and its prime copies based on the number of the variables used 
		int numOfBitsUsed = UtilityMethods.numOfBits(knowledgeGameVarCounter-1);
		Variable[] knowledgeGameVars = new Variable[numOfBitsUsed];
		Variable[] extraVars = new Variable[kVars.length-numOfBitsUsed];
		int numOfExtraVariables = kVars.length - numOfBitsUsed;
		for(int i=0; i<kVars.length;i++){
			if(i<numOfExtraVariables){
				extraVars[i] = kVars[i];
			}else{
				knowledgeGameVars[i-numOfExtraVariables] = kVars[i];
			}
		}
		
		Variable[] knowledgeGameVarsPrime = Variable.getPrimedCopy(knowledgeGameVars);
		Variable[] extraVarsPrime = Variable.getPrimedCopy(extraVars);
		Variable[] extraVarsUnion = Variable.unionVariables(extraVars, extraVarsPrime);
		int extraVarsCube = BDDWrapper.createCube(bdd, extraVarsUnion);
		
//		System.out.println("reducing the number of variables");
//		System.out.println("knowledge game vars");
//		Variable.printVariables(knowledgeGameVars);
//		System.out.println("extra vars");
//		Variable.printVariables(extraVars);
		
//		System.out.println();
		//update the transition relation and init
		int knowledgeGameInitialState = BDDWrapper.exists(bdd, initialKnowledgeGameState, extraVarsCube);
//		UtilityMethods.debugBDDMethods(bdd, "knowledge game init", knowledgeGameInitialState);
		int knowledgeGameT_env = BDDWrapper.exists(bdd, kT_env, extraVarsCube);
		int knowledgeGameT_sys = BDDWrapper.exists(bdd, kT_sys, extraVarsCube);
		
		//update the maps
		for(Integer setOfStates : partiallyObservableGameStructureStatesToknowledgeGameStates.keySet()){
			Integer kGameState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(setOfStates);
			int compactKGameState = BDDWrapper.exists(bdd, kGameState, extraVarsCube);
			
			knowledgeGameStatesToPartiallyObservableGameStructureStates.remove(kGameState);
			knowledgeGameStatesToPartiallyObservableGameStructureStates.put(compactKGameState, setOfStates);

			BDDWrapper.free(bdd, kGameState);
		}
		
		for(Integer k : knowledgeGameStatesToPartiallyObservableGameStructureStates.keySet()){
			Integer setOfStates = knowledgeGameStatesToPartiallyObservableGameStructureStates.get(k);
			partiallyObservableGameStructureStatesToknowledgeGameStates.remove(setOfStates);
			partiallyObservableGameStructureStatesToknowledgeGameStates.put(setOfStates, k);
		}
		
		knowledgeGameStructure = new GameStructure(bdd, knowledgeGameVars, knowledgeGameVarsPrime, knowledgeGameInitialState, knowledgeGameT_env, knowledgeGameT_sys, 
				perfectInformationGameStructure.envActionVars, perfectInformationGameStructure.sysActionVars);
		
		//TODO: initialState must be replaced by initialKnowledge game state
//		GameStructure g = new GameStructure(bdd, kVars, primedKVars, initialState, kT_env, kT_sys, envActionVars, sysActionVars);
		return knowledgeGameStructure;
	}
	
	//TODO: unify the three createKnowledgeGame methods
	public GameStructure createKnowledgeGame(int initialState, int numOfKnowledgeVars){
		Variable[] variables = perfectInformationGameStructure.variables;
		
		System.out.println("estimated num of knowledge vars "+numOfKnowledgeVars);
		
		Variable[] kVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfKnowledgeVars, "kVars");
		Variable[] primedKVars = Variable.getPrimedCopy(kVars);
		Permutation kVarsToPrimedKVars = BDDWrapper.createPermutations(bdd, kVars, primedKVars);
		
//		System.out.println("num of vars  is "+variables.length);
//		System.out.println("kVars num is "+kVars.length);
////		Variable.printVariables(kVars);
//		UtilityMethods.debugBDDMethods(bdd, "initial state", initialState);
//		UtilityMethods.getUserInput();
		
		
		
		//the counter that is used to assign a new value to a variable in partially observable game structure
		int knowledgeGameVarCounter=0;
		
//		knowledgeGameEnvironmentStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		knowledgeGameSystemStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureEnvironmentStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureSystemStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
		
		knowledgeGameStatesToPartiallyObservableGameStructureStates= new HashMap<Integer, Integer>();
		partiallyObservableGameStructureStatesToknowledgeGameStates= new HashMap<Integer, Integer>();
		
		observedSetAlreadyExpanded = new HashMap<Integer, Boolean[]>();
		
		int kT_env = bdd.ref(bdd.getZero());
		ArrayList<Integer> environmentWaitingStates = new ArrayList<Integer>();
		environmentWaitingStates.add(bdd.ref(initialState));
		
		observedSetAlreadyExpanded.put(bdd.ref(initialState), new Boolean[]{false, false});
		
		
		int kT_sys = bdd.ref(bdd.getZero());
		ArrayList<Integer> systemWaitingStates = new ArrayList<Integer>();
		
		//initialize maps
		int initialKnowledgeGameState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
		knowledgeGameVarCounter++;
		knowledgeGameStatesToPartiallyObservableGameStructureStates.put(initialKnowledgeGameState, bdd.ref(initialState));
		partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(initialState), initialKnowledgeGameState);
		
		
		
		while(!environmentWaitingStates.isEmpty() || !systemWaitingStates.isEmpty()){
			
			//define kT_env, the environment transition relation for the knowledge game 
			if(!environmentWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = environmentWaitingStates.remove(0);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.ENVIRONMENT);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				//compute the post image for each environment action
//				for(int i=0; i<envActions.length; i++){
//					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, envActions[i]);
//					
//					UtilityMethods.debugBDDMethods(bdd, "current state and act is ", currentStatesAndAct);
					
//					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, getVariablesAndActionsCube(), T_env, vPrimeTov);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to exapnd", statesToExpand);
//				UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
//				UtilityMethods.debugBDDMethods(bdd, "variables cube is ", getVariablesCube());
//				UtilityMethods.debugBDDMethods(bdd, "actions cube ",getActionsCube());
				int postImage = BDDWrapper.EpostImage(bdd, statesToExpand, perfectInformationGameStructure.getVariablesAndActionsCube(), 
						perfectInformationGameStructure.getEnvironmentTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
				
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					
//					BDDWrapper.free(bdd, currentStatesAndAct);
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					
//					System.out.println("before enumerate");
//					Variable.printVariables(observableVars);
//					UtilityMethods.debugBDDMethods(bdd, "relevant observation", relevantObservations);
					
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
//						UtilityMethods.debugBDDMethods(bdd, "observed partition is ", observedPartitionOfPostImage);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[1]==false){
							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state source is ", knowledgeStateSource);
//						UtilityMethods.debugBDDMethods(bdd, "current action is ", envActions[i]);
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state destination is", knowledgeState);
						
//						int trans = BDDWrapper.and(bdd, knowledgeStateSource, envActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, knowledgeStateDestination);
//						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_env = BDDWrapper.orTo(bdd, kT_env, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
//					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
					
					
				}
//			}
			
			int[] sysActions = perfectInformationGameStructure.getSysActions();
			
			//define kT_sys, the system transition relation for the knowledge game 
			if(!systemWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = systemWaitingStates.remove(0);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.SYSTEM);
				
				//compute the post image for each environment action
				for(int i=0; i<sysActions.length; i++){
					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, sysActions[i]);
//					UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, perfectInformationGameStructure.getVariablesAndActionsCube(), 
							perfectInformationGameStructure.getSystemTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
					BDDWrapper.free(bdd, currentStatesAndAct);
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					//TODO: check for correctness of following step
					if(postImage==0) continue;
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[0]==false){
							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, sysActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_sys = BDDWrapper.orTo(bdd, kT_sys, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
				}
			}
			
			
		}
		
		//adjust the size of the kVars and its prime copies based on the number of the variables used 
		int numOfBitsUsed = UtilityMethods.numOfBits(knowledgeGameVarCounter-1);
		Variable[] knowledgeGameVars = new Variable[numOfBitsUsed];
		Variable[] extraVars = new Variable[kVars.length-numOfBitsUsed];
		int numOfExtraVariables = kVars.length - numOfBitsUsed;
		for(int i=0; i<kVars.length;i++){
			if(i<numOfExtraVariables){
				extraVars[i] = kVars[i];
			}else{
				knowledgeGameVars[i-numOfExtraVariables] = kVars[i];
			}
		}
		
		Variable[] knowledgeGameVarsPrime = Variable.getPrimedCopy(knowledgeGameVars);
		Variable[] extraVarsPrime = Variable.getPrimedCopy(extraVars);
		Variable[] extraVarsUnion = Variable.unionVariables(extraVars, extraVarsPrime);
		int extraVarsCube = BDDWrapper.createCube(bdd, extraVarsUnion);
		
//		System.out.println("reducing the number of variables");
//		System.out.println("knowledge game vars");
//		Variable.printVariables(knowledgeGameVars);
//		System.out.println("extra vars");
//		Variable.printVariables(extraVars);
		
//		System.out.println();
		//update the transition relation and init
		int knowledgeGameInitialState = BDDWrapper.exists(bdd, initialKnowledgeGameState, extraVarsCube);
//		UtilityMethods.debugBDDMethods(bdd, "knowledge game init", knowledgeGameInitialState);
		int knowledgeGameT_env = BDDWrapper.exists(bdd, kT_env, extraVarsCube);
		int knowledgeGameT_sys = BDDWrapper.exists(bdd, kT_sys, extraVarsCube);
		
		//update the maps
		for(Integer setOfStates : partiallyObservableGameStructureStatesToknowledgeGameStates.keySet()){
			Integer kGameState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(setOfStates);
			int compactKGameState = BDDWrapper.exists(bdd, kGameState, extraVarsCube);
			
			knowledgeGameStatesToPartiallyObservableGameStructureStates.remove(kGameState);
			knowledgeGameStatesToPartiallyObservableGameStructureStates.put(compactKGameState, setOfStates);

			BDDWrapper.free(bdd, kGameState);
		}
		
		for(Integer k : knowledgeGameStatesToPartiallyObservableGameStructureStates.keySet()){
			Integer setOfStates = knowledgeGameStatesToPartiallyObservableGameStructureStates.get(k);
			partiallyObservableGameStructureStatesToknowledgeGameStates.remove(setOfStates);
			partiallyObservableGameStructureStatesToknowledgeGameStates.put(setOfStates, k);
		}
		
		knowledgeGameStructure = new GameStructure(bdd, knowledgeGameVars, knowledgeGameVarsPrime, knowledgeGameInitialState, knowledgeGameT_env, knowledgeGameT_sys, 
				perfectInformationGameStructure.envActionVars, perfectInformationGameStructure.sysActionVars);
		
		System.out.println("\n Exact number of knowledge game vars is: "+knowledgeGameVars.length);
		System.out.println();
		
		//TODO: initialState must be replaced by initialKnowledge game state
//		GameStructure g = new GameStructure(bdd, kVars, primedKVars, initialState, kT_env, kT_sys, envActionVars, sysActionVars);
		return knowledgeGameStructure;
	}
	
	public GameStructure createKnowledgeGame_OLD(int initialState){
		Variable[] variables = perfectInformationGameStructure.variables;
		int numOfKnowledgeVars = (int) Math.pow(2, variables.length);
		
		
		
		Variable[] kVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfKnowledgeVars, "kVars");
		Variable[] primedKVars = Variable.getPrimedCopy(kVars);
		
		System.out.println("num of vars  is "+variables.length);
		System.out.println("kVars num is "+kVars.length);
//		Variable.printVariables(kVars);
		
		UtilityMethods.debugBDDMethods(bdd, "init is", perfectInformationGameStructure.getInit());
		
		
		
		Permutation kVarsToPrimedKVars = BDDWrapper.createPermutations(bdd, kVars, primedKVars);
		
		//the counter that is used to assign a new value to a variable in partially observable game structure
		int knowledgeGameVarCounter=0;
		
//		knowledgeGameEnvironmentStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		knowledgeGameSystemStatesToPartiallyObservableGameStructureStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureEnvironmentStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
//		partiallyObservableGameStructureSystemStatesToknowledgeGameStates = new HashMap<Integer, Integer>();
		
		knowledgeGameStatesToPartiallyObservableGameStructureStates= new HashMap<Integer, Integer>();
		partiallyObservableGameStructureStatesToknowledgeGameStates= new HashMap<Integer, Integer>();
		
		observedSetAlreadyExpanded = new HashMap<Integer, Boolean[]>();
		
		int kT_env = bdd.ref(bdd.getZero());
		ArrayList<Integer> environmentWaitingStates = new ArrayList<Integer>();
		environmentWaitingStates.add(bdd.ref(initialState));
		
		observedSetAlreadyExpanded.put(bdd.ref(initialState), new Boolean[]{false, false});
		
		
		int kT_sys = bdd.ref(bdd.getZero());
		ArrayList<Integer> systemWaitingStates = new ArrayList<Integer>();
		
		//initialize maps
		int initialKnowledgeGameState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
		knowledgeGameVarCounter++;
		knowledgeGameStatesToPartiallyObservableGameStructureStates.put(initialKnowledgeGameState, bdd.ref(initialState));
		partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(initialState), initialKnowledgeGameState);
		
		
		
		while(!environmentWaitingStates.isEmpty() || !systemWaitingStates.isEmpty()){
			
			//define kT_env, the environment transition relation for the knowledge game 
			if(!environmentWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = environmentWaitingStates.remove(0);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.ENVIRONMENT);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to expand is ", statesToExpand);
				
				//compute the post image for each environment action
//				for(int i=0; i<envActions.length; i++){
//					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, envActions[i]);
//					
//					UtilityMethods.debugBDDMethods(bdd, "current state and act is ", currentStatesAndAct);
					
//					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, getVariablesAndActionsCube(), T_env, vPrimeTov);
				
//				UtilityMethods.debugBDDMethods(bdd, "states to exapnd", statesToExpand);
//				UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
//				UtilityMethods.debugBDDMethods(bdd, "variables cube is ", getVariablesCube());
//				UtilityMethods.debugBDDMethods(bdd, "actions cube ",getActionsCube());
				int postImage = BDDWrapper.EpostImage(bdd, statesToExpand, perfectInformationGameStructure.getVariablesAndActionsCube(), 
						perfectInformationGameStructure.getEnvironmentTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
				
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					
//					BDDWrapper.free(bdd, currentStatesAndAct);
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					
//					System.out.println("before enumerate");
//					Variable.printVariables(observableVars);
//					UtilityMethods.debugBDDMethods(bdd, "relevant observation", relevantObservations);
					
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
//						UtilityMethods.debugBDDMethods(bdd, "observed partition is ", observedPartitionOfPostImage);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[1]==false){
							systemWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state source is ", knowledgeStateSource);
//						UtilityMethods.debugBDDMethods(bdd, "current action is ", envActions[i]);
//						UtilityMethods.debugBDDMethods(bdd, "knowledge state destination is", knowledgeState);
						
//						int trans = BDDWrapper.and(bdd, knowledgeStateSource, envActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, knowledgeStateDestination);
//						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_env = BDDWrapper.orTo(bdd, kT_env, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
//					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
					
					
				}
//			}
			
			int[] sysActions = perfectInformationGameStructure.getSysActions();
			
			//define kT_sys, the system transition relation for the knowledge game 
			if(!systemWaitingStates.isEmpty()){
				//pick a set of states to expand
				int statesToExpand = systemWaitingStates.remove(0);
				
				updateObservedStateAlreadyExpanded(statesToExpand, Player.SYSTEM);
				
				//compute the post image for each environment action
				for(int i=0; i<sysActions.length; i++){
					int currentStatesAndAct = BDDWrapper.and(bdd, statesToExpand, sysActions[i]);
//					UtilityMethods.debugBDDMethods(bdd, "variables and actions cube", getVariablesAndActionsCube());
					int postImage = BDDWrapper.EpostImage(bdd, currentStatesAndAct, perfectInformationGameStructure.getVariablesAndActionsCube(), 
							perfectInformationGameStructure.getSystemTransitionRelation(), perfectInformationGameStructure.getVprimetoV());
					BDDWrapper.free(bdd, currentStatesAndAct);
					
//					UtilityMethods.debugBDDMethods(bdd, "post image is ", postImage);
					//TODO: check for correctness of following step
					if(postImage==0) continue;
					
					//partition the post image based on the observations
					//First intersect the post image with observation map to compute the set of relevant observations
					int observationsAndStates = BDDWrapper.and(bdd, observationMap, postImage);
					int relevantObservations = BDDWrapper.exists(bdd, observationsAndStates, perfectInformationGameStructure.getVariablesCube());
					int[] observations = BDDWrapper.enumerate(bdd, relevantObservations, observableVars);
					BDDWrapper.free(bdd, relevantObservations);
					
					//partition the post image based on the observations
					for(int observation : observations){
						int observedPartitionOfPostImageTmp = BDDWrapper.and(bdd, observation, observationsAndStates);
						int observedPartitionOfPostImage = BDDWrapper.exists(bdd, observedPartitionOfPostImageTmp, getObservableVarsCube());
						BDDWrapper.free(bdd, observedPartitionOfPostImageTmp);
						BDDWrapper.free(bdd, observation);
						
						//update the environment transition relation in knowledge game
						//add a transition between current set of states with current action and observed partition of 
						// the post image
						
						//check if the observed partition of the post image is already explored and exists in the map
						Integer knowledgeState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(observedPartitionOfPostImage);
						if(knowledgeState == null){//it is a new system state in the knowledge game 
							//create a new state and update the mappings
							knowledgeState = BDDWrapper.assign(bdd, knowledgeGameVarCounter, kVars);
							knowledgeGameVarCounter++;
							knowledgeGameStatesToPartiallyObservableGameStructureStates.put(knowledgeState, bdd.ref(observedPartitionOfPostImage));
							partiallyObservableGameStructureStatesToknowledgeGameStates.put(bdd.ref(observedPartitionOfPostImage), knowledgeState);
//							//add the state to system waiting states queue to be expanded later
//							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						//add the state to system waiting states queue to be expanded later
						Boolean[] isExpanded = observedSetAlreadyExpanded.get(observedPartitionOfPostImage);
						if(isExpanded == null || isExpanded[0]==false){
							environmentWaitingStates.add(bdd.ref(observedPartitionOfPostImage));
						}
						
						// add a transition to the knowledge game structure
						int knowledgeStateSource = partiallyObservableGameStructureStatesToknowledgeGameStates.get(statesToExpand);
						int trans = BDDWrapper.and(bdd, knowledgeStateSource, sysActions[i]);
						int knowledgeStateDestination = BDDWrapper.replace(bdd, knowledgeState, kVarsToPrimedKVars );
						trans = BDDWrapper.andTo(bdd, trans, knowledgeStateDestination);
						kT_sys = BDDWrapper.orTo(bdd, kT_sys, trans);
						BDDWrapper.free(bdd,trans);
						BDDWrapper.free(bdd, observedPartitionOfPostImage);
						BDDWrapper.free(bdd, knowledgeStateDestination);
					}
					
					BDDWrapper.free(bdd,observationsAndStates);
					BDDWrapper.free(bdd, currentStatesAndAct);
					BDDWrapper.free(bdd, postImage);
				}
			}
			
			
		}
		
		//adjust the size of the kVars and its prime copies based on the number of the variables used 
		int numOfBitsUsed = UtilityMethods.numOfBits(knowledgeGameVarCounter-1);
		Variable[] knowledgeGameVars = new Variable[numOfBitsUsed];
		Variable[] extraVars = new Variable[kVars.length-numOfBitsUsed];
		int numOfExtraVariables = kVars.length - numOfBitsUsed;
		for(int i=0; i<kVars.length;i++){
			if(i<numOfExtraVariables){
				extraVars[i] = kVars[i];
			}else{
				knowledgeGameVars[i-numOfExtraVariables] = kVars[i];
			}
		}
		
		Variable[] knowledgeGameVarsPrime = Variable.getPrimedCopy(knowledgeGameVars);
		Variable[] extraVarsPrime = Variable.getPrimedCopy(extraVars);
		Variable[] extraVarsUnion = Variable.unionVariables(extraVars, extraVarsPrime);
		int extraVarsCube = BDDWrapper.createCube(bdd, extraVarsUnion);
		
//		System.out.println("reducing the number of variables");
//		System.out.println("knowledge game vars");
//		Variable.printVariables(knowledgeGameVars);
//		System.out.println("extra vars");
//		Variable.printVariables(extraVars);
		
//		System.out.println();
		//update the transition relation and init
		int knowledgeGameInitialState = BDDWrapper.exists(bdd, initialKnowledgeGameState, extraVarsCube);
//		UtilityMethods.debugBDDMethods(bdd, "knowledge game init", knowledgeGameInitialState);
		int knowledgeGameT_env = BDDWrapper.exists(bdd, kT_env, extraVarsCube);
		int knowledgeGameT_sys = BDDWrapper.exists(bdd, kT_sys, extraVarsCube);
		
		//update the maps
		for(Integer setOfStates : partiallyObservableGameStructureStatesToknowledgeGameStates.keySet()){
			Integer kGameState = partiallyObservableGameStructureStatesToknowledgeGameStates.get(setOfStates);
			int compactKGameState = BDDWrapper.exists(bdd, kGameState, extraVarsCube);
			
			knowledgeGameStatesToPartiallyObservableGameStructureStates.remove(kGameState);
			knowledgeGameStatesToPartiallyObservableGameStructureStates.put(compactKGameState, setOfStates);

			BDDWrapper.free(bdd, kGameState);
		}
		
		for(Integer k : knowledgeGameStatesToPartiallyObservableGameStructureStates.keySet()){
			Integer setOfStates = knowledgeGameStatesToPartiallyObservableGameStructureStates.get(k);
			partiallyObservableGameStructureStatesToknowledgeGameStates.remove(setOfStates);
			partiallyObservableGameStructureStatesToknowledgeGameStates.put(setOfStates, k);
		}
		
		knowledgeGameStructure = new GameStructure(bdd, knowledgeGameVars, knowledgeGameVarsPrime, knowledgeGameInitialState, knowledgeGameT_env, knowledgeGameT_sys, 
				perfectInformationGameStructure.envActionVars, perfectInformationGameStructure.sysActionVars);
		
		//TODO: initialState must be replaced by initialKnowledge game state
//		GameStructure g = new GameStructure(bdd, kVars, primedKVars, initialState, kT_env, kT_sys, envActionVars, sysActionVars);
		return knowledgeGameStructure;
	}
	
	private void updateObservedStateAlreadyExpanded(int setToExpand, Player p){
		Boolean[] isExpanded = observedSetAlreadyExpanded.get(setToExpand); 
		if(isExpanded == null){
			isExpanded = new Boolean[]{false, false};
			observedSetAlreadyExpanded.put(bdd.ref(setToExpand), isExpanded);
		}
		if(p == Player.ENVIRONMENT){
//			observedSetAlreadyExpanded.replace((Integer) setToExpand, new Boolean[]{true, isExpanded[1]});
			observedSetAlreadyExpanded.remove(setToExpand);
			observedSetAlreadyExpanded.put(setToExpand, new Boolean[]{true, isExpanded[1]});
		}else{
//			observedSetAlreadyExpanded.replace(setToExpand, new Boolean[]{isExpanded[0], true});
			observedSetAlreadyExpanded.remove(setToExpand);
			observedSetAlreadyExpanded.put(setToExpand, new Boolean[]{isExpanded[0], true});
		}
	}
	
	int counter=0;
	public void printKnowledgeGameToPartiallyObservableGameStatesMap(){
		System.out.println("\nprinting the map");
		for(int k  : knowledgeGameStatesToPartiallyObservableGameStructureStates.keySet()){
			UtilityMethods.debugBDDMethods(bdd, "knowledge state", k);
			UtilityMethods.debugBDDMethods(bdd, "corrseponds to", knowledgeGameStatesToPartiallyObservableGameStructureStates.get(k));
			counter++;
			if(counter%5==0){
				UtilityMethods.getUserInput();
			}
		}
		
	}
	
	public void printPartiallyObservableGameToKnowledgeGameStatesMap(){
		System.out.println("\nprinting the map");
		for(int s  : partiallyObservableGameStructureStatesToknowledgeGameStates.keySet()){
			UtilityMethods.debugBDDMethods(bdd, "sets of states", s);
			UtilityMethods.debugBDDMethods(bdd, "corrseponds to", partiallyObservableGameStructureStatesToknowledgeGameStates.get(s));
			counter++;
			if(counter%5==0){
				UtilityMethods.getUserInput();
			}
		}
		
	}
	
	//TODO: cleans up the memory for the partially observable game structure
	public void cleanUp(){
		
	}
	
	public void printObservationMap(){
		System.out.println("printing the observation map for the partially observable game structure");
		for(Entry<Integer, Integer> e :knowledgeGameStatesToPartiallyObservableGameStructureStates.entrySet()){
			System.out.println("knowledge game state ");
			bdd.printSet(e.getKey());
			System.out.println("corresponds to");
			bdd.printSet(e.getValue());
		}
		System.out.println();
	}

	public static void main(String[] args) {

		BDD bdd = new BDD(10000,10000);
		
		//test BDDWrapper.enumerate
//		Variable a = Variable.createVariableAndPrimedVariable(bdd, "a");
//		Variable b = Variable.createVariableAndPrimedVariable(bdd, "b");
//		
//		int av = a.getBDDVar();
//		int bv = b.getBDDVar();
//		
//		int f = bdd.ref(bdd.xor(av, bv));
//		bdd.printSet(f);
//		
//		Variable[] vars = {a,b};
//		
//		int[] minterms = BDDWrapper.enumerate(bdd, f, vars);
//		for(int m : minterms){
//			bdd.printSet(m);
//		}
		
		//test creating knowledge game structure
//		GameStructure gs = SampleGameStructureGenerator.simpleGameStructure(bdd, 1);
//		gs.printGame();
//		gs.toGameGraph("simpleGame").draw("simpleGame.dot",1,0);
		
		
		
		int numOfCells = 4;
		
		System.out.println("Constrcuting the game for "+numOfCells+" * "+numOfCells+" grid world");
		long t0 = UtilityMethods.timeStamp();
		
		GameStructure gs = SampleGameStructureGenerator.simpleGameStructureForTestingPartiallyObservableGamesWithStayPut(bdd, numOfCells);
		gs.printGame();
//		gs.toGameGraph("simpleGame").draw("simpleGame.dot",1,0);
		
//		int postImage = BDDWrapper.EpostImage(bdd, gs.getInit(), gs.getVariablesAndActionsCube(), gs.T_env, gs.getVprimetoV());
//		UtilityMethods.debugBDDMethods(bdd, "post Image is", postImage);
//		UtilityMethods.getUserInput();
		
		

		//1) define a partially observable game structure
		Variable[] vars = gs.variables;
		
		Variable[] inputVars = Variable.getVariablesStartingWith(vars, "X");
		Variable[] outputVars = Variable.getVariablesStartingWith(vars, "Y");
		
//		System.out.println("input vars");
//		Variable.printVariables(inputVars);
//		System.out.println("output vars");
//		Variable.printVariables(outputVars);
//		UtilityMethods.getUserInput();
		
		Variable[] observables =  Variable.createVariables(bdd, vars.length, "obsVars");
		
		//special case where observations and vars are the same
//		int observationMap = BDDWrapper.same(bdd, vars, observables);
		
		int observationMap = createSimpleLocalObservationMapOneDimensional(bdd, numOfCells, inputVars, 
				outputVars, observables);
		
//		UtilityMethods.debugBDDMethods(bdd, "observation map is", observationMap);
//		UtilityMethods.getUserInput();
		
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
//				gs.variables, gs.getEnvironmentTransitionRelation(), gs.getSystemTransitionRelation(), 
//				gs.actionVars, gs.actionVars, observables, observationMap);
		
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
				gs, observables, observationMap);
		
		UtilityMethods.duration(t0, "partially observable game was constructed in ");
		
		//2) call the subset construction procedure
		System.out.println("constructing the knowledge game");
		t0 = UtilityMethods.timeStamp();
		GameStructure kGS = pogs.createKnowledgeGame(gs.getInit());
		UtilityMethods.duration(t0, "subset construction done in ");
		
//		kGS.printGame();
//		kGS.toGameGraph().draw("knowledgeGame.dot", 1, 0);
//		pogs.printObservationMap();
		
		//define objective 
		System.out.println("defining the objective");
		t0 = UtilityMethods.timeStamp();
		int notPhi = BDDWrapper.same(bdd, inputVars, outputVars);
		int phi = BDDWrapper.not(bdd, notPhi);
		BDDWrapper.free(bdd, notPhi);
		
		
		//translate to objective for knowledge game
		int objective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(phi);
		UtilityMethods.duration(t0, "objective defined ");
		
//		UtilityMethods.debugBDDMethods(bdd, "phi is", phi);
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
		
		System.out.println("Solving the game");
		t0 = UtilityMethods.timeStamp();
		GameSolution sol = GameSolver.solve(bdd, kGS, objective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in");
		
//		sol.drawWinnerStrategy("partialGameStrat.dot", 1, 0);
	}
	
	//TODO
	/**
	 * Creates a simple local observation function that assumes system can observe its own states perfectly, and 
	 * maps the environment states to their perfect valuation if they are close enough and to an special value if they 
	 * are far
	 * @return
	 */
	public static int createSimpleLocalObservationMap2DExplicit(BDD bdd, int numOfCells, Agent2D environment, 
			Agent2D system, Variable[] observables){
		
		//how far the system agent can see in each direction
		int viewBound = 1;
		int observationMap = bdd.ref(bdd.getZero());
		
		int observationCounter=0;
		
		for(int i= 0; i<numOfCells;i++){
			for(int j=0; j<numOfCells; j++){
				int currentSystemState = UtilityFormulas.assignCell(bdd, system, new GridCell2D(i, j));
				
				int notObservable = bdd.ref(bdd.getZero());
				for(int k=0; k<numOfCells; k++){
					for(int l=0; l<numOfCells; l++){
						int environmentState = UtilityFormulas.assignCell(bdd, environment, new GridCell2D(k, l));
						if(Math.abs(i-k)<=viewBound && Math.abs(j-l)<=viewBound){//observable
							int observation = BDDWrapper.assign(bdd, observationCounter, observables);
							observationCounter++;
							observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
							observation = BDDWrapper.andTo(bdd, observation, environmentState);
							observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
							BDDWrapper.free(bdd, observation);
						}else{//not observable
							notObservable = BDDWrapper.orTo(bdd, notObservable, environmentState);
						}
						BDDWrapper.free(bdd, environmentState);
					}
				}
				
				if(notObservable != 0){
					int observation = BDDWrapper.assign(bdd, observationCounter, observables);
					observationCounter++;
					observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
					observation = BDDWrapper.andTo(bdd, observation, notObservable);
					BDDWrapper.free(bdd, notObservable);
					observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
					BDDWrapper.free(bdd, observation);
				}
				
				BDDWrapper.free(bdd, currentSystemState);
			}
		}
		
		return observationMap;
	}
	
	/**
	 * Creates a simple local observation function that assumes system can observe its own states perfectly, and 
	 * maps the environment states to their perfect valuation if they are close enough and to an special value if they 
	 * are far
	 * @return
	 */
	public static int createSimpleLocalObservationMap2DExplicitWithExtraVars(BDD bdd, int numOfCells, Agent2D environment, 
			Agent2D system, Variable[] observables){
		
		//how far the system agent can see in each direction
		int viewBound = 1;
		int observationMap = bdd.ref(bdd.getZero());
		
		int observationCounter=0;
		
		Variable[] extraVars = system.getNonCoordinationVars();
		int[] extraVarsValues = UtilityMethods.enumerate(bdd, extraVars);
		
		for(int extraVarsVal : extraVarsValues){
			for(int i= 0; i<numOfCells;i++){
				for(int j=0; j<numOfCells; j++){
					int currentSystemState = UtilityFormulas.assignCell(bdd, system, new GridCell2D(i, j));
					currentSystemState = BDDWrapper.andTo(bdd, currentSystemState, extraVarsVal);
					int notObservable = bdd.ref(bdd.getZero());
					for(int k=0; k<numOfCells; k++){
						for(int l=0; l<numOfCells; l++){
							int environmentState = UtilityFormulas.assignCell(bdd, environment, new GridCell2D(k, l));
							if(Math.abs(i-k)<=viewBound && Math.abs(j-l)<=viewBound){//observable
								int observation = BDDWrapper.assign(bdd, observationCounter, observables);
								observationCounter++;
								observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
								observation = BDDWrapper.andTo(bdd, observation, environmentState);
								observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
								BDDWrapper.free(bdd, observation);
							}else{//not observable
								notObservable = BDDWrapper.orTo(bdd, notObservable, environmentState);
							}
							BDDWrapper.free(bdd, environmentState);
						}
					}
					
					if(notObservable != 0){
						int observation = BDDWrapper.assign(bdd, observationCounter, observables);
						observationCounter++;
						observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
						observation = BDDWrapper.andTo(bdd, observation, notObservable);
						BDDWrapper.free(bdd, notObservable);
						observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
						BDDWrapper.free(bdd, observation);
					}
					
					BDDWrapper.free(bdd, currentSystemState);
				}
			}
		}
		
		BDDWrapper.free(bdd, extraVarsValues);
		
		return observationMap;
	}
	
	public static int createSimpleLocalObservationMap2DSymbolic(){
		return -1;
	}
	
	public static int createSimpleLocalObservationMapOneDimensional(BDD bdd, int numOfCells, Variable[] inputVars, 
			Variable[] outputVars, Variable[] observables){
		
		//how far behind can you observe perfectly
		int behindViewBound = 1;
		//how far ahead can you observe perfectly
		int frontViewBound = 1;
		
		int observationMap = bdd.ref(bdd.getZero());
		
		int observationCounter=0;
		for(int i=0; i<numOfCells; i++){
			int currentSystemState = BDDWrapper.assign(bdd, i, outputVars);
			
			//if the uncontrolled robot is behind the controlled robot, we cannot see it
			int behind = bdd.ref(bdd.getZero());
			for(int j=i-1-behindViewBound; j>=0; j--){
				int behind_j = BDDWrapper.assign(bdd, j, inputVars);
				behind = BDDWrapper.orTo(bdd, behind, behind_j);
				BDDWrapper.free(bdd, behind_j);
			}
//			if(behind != 0){
//				int observation = BDDWrapper.assign(bdd, observationCounter, observables);
//				observationCounter++;
//				observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
//				observation = BDDWrapper.andTo(bdd, observation, behind);
//				BDDWrapper.free(bdd, behind);
//				observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
//				BDDWrapper.free(bdd, observation);
//			}
			
			//if the uncontrolled robot is close enough, we can see it
			for(int j=i - behindViewBound>=0?i-behindViewBound:0; j<i+1+frontViewBound && j<numOfCells; j++){
				int closeEnough = BDDWrapper.assign(bdd, j, inputVars);
				int observation = BDDWrapper.assign(bdd, observationCounter, observables);
				observationCounter++;
				observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
				observation = BDDWrapper.andTo(bdd, observation, closeEnough);
				BDDWrapper.free(bdd, closeEnough);
				observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
				BDDWrapper.free(bdd, observation);
			}
			
			//if the uncontrolled robot is too far ahead, we cannot see it
			int front = bdd.ref(bdd.getZero());
			for(int j=i+1+frontViewBound; j<numOfCells; j++){
				int front_j = BDDWrapper.assign(bdd, j, inputVars);
				front = BDDWrapper.orTo(bdd, front, front_j);
				BDDWrapper.free(bdd, front_j);
			}
//			if(front != 0){
//				int observation = BDDWrapper.assign(bdd, observationCounter, observables);
//				observationCounter++;
//				observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
//				observation = BDDWrapper.andTo(bdd, observation, front);
//				BDDWrapper.free(bdd, front);
//				observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
//				BDDWrapper.free(bdd, observation);
//			}
			
			int notObservavle = BDDWrapper.or(bdd, behind, front);
			BDDWrapper.free(bdd, behind);
			BDDWrapper.free(bdd, front);
			if(notObservavle != 0){
				int observation = BDDWrapper.assign(bdd, observationCounter, observables);
				observationCounter++;
				observation = BDDWrapper.andTo(bdd, observation, currentSystemState);
				observation = BDDWrapper.andTo(bdd, observation, notObservavle);
				BDDWrapper.free(bdd, notObservavle);
				observationMap=BDDWrapper.orTo(bdd, observationMap, observation);
				BDDWrapper.free(bdd, observation);
			}
			
			BDDWrapper.free(bdd, currentSystemState);
		}
		return observationMap;
	}
	
	/**
	 * Given an objective phi over the variables, translates it to an objective for the knowledge game 
	 * @param phi
	 * @return
	 */
	public int translateConcreteObjectiveToKnowledgeGameObjective(int phi){
		int objective = bdd.ref(bdd.getZero());
		for(Entry<Integer, Integer> e : knowledgeGameStatesToPartiallyObservableGameStructureStates.entrySet()){
			Integer statesSet = e.getValue();
			if(BDDWrapper.subset(bdd, statesSet, phi)){
				objective = BDDWrapper.orTo(bdd, objective, e.getKey());
			}
		}
		return objective;
	}
	
	public static int translateConcreteObjectiveToKnowledgeGameObjective(BDD bdd, int concreteObjective, 
			HashMap<Integer, Integer> knowledgeGameStateToPerfectInformationGameStructureStates){
		int objective = bdd.ref(bdd.getZero());
		for(Entry<Integer, Integer> e : knowledgeGameStateToPerfectInformationGameStructureStates.entrySet()){
			Integer statesSet = e.getValue();
			if(BDDWrapper.subset(bdd, statesSet, concreteObjective)){
				objective = BDDWrapper.orTo(bdd, objective, e.getKey());
			}
		}
		return objective;
	}
	
	/**
	 * Given two maps that map the knowledge game structure states to their corresponding set of perfect information states, 
	 * compute a composed map
	 * @param bdd
	 * @param map1
	 * @param map2
	 * @return
	 */
	public static HashMap<Integer, Integer> composeKnowledgeToPerfectInformationStatesMap(BDD bdd, 
			HashMap<Integer, Integer> map1, HashMap<Integer, Integer> map2){
		HashMap<Integer, Integer> composedMap = new HashMap<Integer, Integer>();
		
		for(Entry<Integer, Integer> e1 : map1.entrySet()){
			int knowledgeGameState1 = e1.getKey();
			int setOfStates1 = e1.getValue();
			for(Entry<Integer, Integer> e2 : map2.entrySet()){
				int knowledgeGameState2 = e2.getKey();
				int setOfStates2 = e2.getValue();
				int key = BDDWrapper.and(bdd, knowledgeGameState1, knowledgeGameState2);
				int value = BDDWrapper.and(bdd, setOfStates1, setOfStates2);
				if(value != 0){
					composedMap.put(key, value);
				}
				//TODO:if value=0, free the memory for key
			}
		}
		
		
		return composedMap;
		
	}
	
	/**
	 * Returns the state in the knowledge game structure corresponding to the set of states
	 * @param setOfStates
	 * @return
	 */
	public int getCorrespondingKnowledgeGameState(int setOfStates){
		return partiallyObservableGameStructureStatesToknowledgeGameStates.get(setOfStates);
	}
	
	public int getCorrespondingGameState(int knowledgeGameState){
		return knowledgeGameStatesToPartiallyObservableGameStructureStates.get(knowledgeGameState);
	}
	
	
	

}
