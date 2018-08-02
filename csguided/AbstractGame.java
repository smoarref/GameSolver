package csguided;



import java.util.ArrayList;

import utils.UtilityMethods;
import jdd.bdd.BDD;

//TODO: probably we need to have separate sets for system and environment equivalence classes
//TODO: for both games and abstract games, maybe the set of actions and available actions can be 
//directly extracted from the transition relation, thus there is no need to keep them separately
public class AbstractGame extends Game{
	
	Game concreteGame;
	int[] equivalenceClasses;
	
	int[] abstractPredicates;


	int[] equivalenceClassesPrime;
	
//	int[] abstractStates;
	

	
	public AbstractGame(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argEnvActions, Variable[] argSysActions, Game argConcrete, int[] argEqClasses){
		
		super(argBdd, argVariables, argPrimedVars, initial, envTrans, sysTrans, argEnvActions, argSysActions);
		concreteGame = argConcrete;
		equivalenceClasses = BDDWrapper.copyWithReference(bdd, argEqClasses);
		
		equivalenceClassesPrime = BDDWrapper.replace(bdd, equivalenceClasses, concreteGame.vTovPrime);
		
//		abstractStates = UtilityMethods.enumerate(bdd, Variable.getBDDVars(variables));
	}
	
	
	
	public AbstractGame(BDD argBdd, int[] argAbstractPredicates, Game argConcreteGame, boolean approximate){
		super(argBdd);
		if(approximate){
			computeApproximateAbstraction(argAbstractPredicates, argConcreteGame);
		}else{
			int[] eqClasses=UtilityMethods.enumerate(bdd, argAbstractPredicates);
			computeAbstraction(eqClasses, argConcreteGame);
		}
	}
	
	public Game getConcreteGame(){
		return concreteGame;
	}
	
	public int[] computeEquivalenceClassesFromAbstractPredicates(){
		ArrayList<Integer> eqClasses=new ArrayList<Integer>();
		computeEqClassesFromAbsPreds(bdd.getOne(), eqClasses, 0);
		return UtilityMethods.IntegerArrayListTointArray(eqClasses);
	}
	
	
	public void computeEqClassesFromAbsPreds(int formula, ArrayList<Integer> eqClasses, int index){
		if(index==abstractPredicates.length){
			if(formula != 0) eqClasses.add(bdd.ref(formula));
			return;
		}
		
		int f0 = bdd.ref(bdd.and(formula, abstractPredicates[index]));
//		int notAbsPre = bdd.ref(bdd.not(abstractPredicates[index]));
		int f1 = bdd.ref(bdd.and(formula, bdd.not(abstractPredicates[index])));
		
		if(f0 != 0){
			computeEqClassesFromAbsPreds(f0, eqClasses, index+1);
		}
		
		if(f1 !=0){
			computeEqClassesFromAbsPreds(f1, eqClasses, index+1);
		}
		
//		if(index < abstractPredicates.length-1){
//			bdd.deref(f0);
//			bdd.deref(f1);
//		}
		
		bdd.deref(f0);
		bdd.deref(f1);
		
//		bdd.deref(notAbsPre);
	}
	
	public void computeApproximateAbstraction(int[] argAbstractPredicates, Game argConcreteGame){
		
		
		
		
		
		abstractPredicates  = BDDWrapper.copyWithReference(bdd, argAbstractPredicates);
		concreteGame = argConcreteGame;
		
		
		
//		concreteGame.printGame();
//		UtilityMethods.getUserInput();
		
//		equivalenceClasses = UtilityMethods.enumerate(bdd, abstractPredicates);
		equivalenceClasses = computeEquivalenceClassesFromAbstractPredicates();
		
		equivalenceClassesPrime = BDDWrapper.replace(bdd, equivalenceClasses, concreteGame.vTovPrime);
		
		System.out.println("eq classes was computed from abstract predicates");
		
//		printEquivalenceClasses();
//		UtilityMethods.getUserInput();
		
//		this.variables=createAbstractVariables(equivalenceClasses, "absVars");
//		
//		this.primedVariables=Variable.createPrimeVariables(bdd, this.variables);
		
		createAbstractVariables();
		
//		abstractStates = UtilityMethods.enumerate(bdd, Variable.getBDDVars(variables));
		
//		System.out.println("vars and prime vars created");
//		Variable.printVariables(variables);
//		Variable.printVariables(primedVariables);
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		actionVars=concreteGame.actionVars;
		actionsCube=-1;
		
		//hatI
		//create the initial relation \hat{I} of the abstract game
		this.init = computeApproximateInit();
		
		System.out.println("abstract init was computed");
		
		//hatT_env
		//create the transition relation \hat{T}_env of the abstract game
//		this.T_env=computeApproximateT_env();
		this.T_env = computeT_env();
		T_envList = new ArrayList<Integer>();
		T_envList.add(bdd.ref(T_env));
		
		System.out.println("abstract env transition relation was computed");
				
		//hatT_sys
//		this.T_sys=computeApproximateT_sys();
		this.T_sys=computeT_sys();
		T_sysList = new ArrayList<Integer>();
		T_sysList.add(T_sys);
		
		System.out.println("abstract sys transition relation was computed");
		
		System.out.println("transition relations were created");
		
		setActionMap(concreteGame.getActionMap());
		
		this.actions = concreteGame.actions;
		
		this.envActionVars = concreteGame.envActionVars;
		this.sysActionVars = concreteGame.sysActionVars;
		this.envActions = concreteGame.envActions;
		this.sysActions = concreteGame.sysActions;
		
		actionAvailabilitySets();
		
		System.out.println("actions avl sets were computed");
		
		BDDWrapper.free(bdd,equivalenceClassesPrime);
	}
	
	public int computeApproximateInit(){
		return approximateAbstractionFunction(concreteGame.getInit());
	}
	

	
	public int computeT_env(){
		long t_env = UtilityMethods.timeStamp();
		
		int abstractT_env=bdd.ref(bdd.getZero());
		
		while(concreteGame.hasNextTransition(Player.ENVIRONMENT)){
			int trans = concreteGame.getNextEnvTransition();
			
//			UtilityMethods.debugBDDMethods(bdd, "computing T_env, current trans is ", trans);
//			UtilityMethods.getUserInput();
//			System.out.println("env trans fetched");
			
			for(int k=0; k<concreteGame.envActions.length;k++){
				int transWithAction = BDDWrapper.intersect(bdd, trans, concreteGame.envActions[k]);
//				System.out.println("trans with action fetched");
				if(transWithAction != 0){
					for(int i=0; i<equivalenceClasses.length; i++){
						int transWithEqClassSource = BDDWrapper.intersect(bdd, transWithAction, equivalenceClasses[i]);
//						System.out.println("trans with action and source  fetched");
						if(transWithEqClassSource !=0){
							for(int j=0; j<equivalenceClassesPrime.length;j++){
								int abstractTransExists = BDDWrapper.intersect(bdd, transWithEqClassSource, equivalenceClassesPrime[j]);
//								System.out.println("abs trans exists computed");
								if(abstractTransExists != 0){
									int currentAbsState = equivalenceClassToAbstractStateMap(i);
									int nextAbsState = equivalenceClassToAbstractStateMap(j);
									

									
//									UtilityMethods.debugBDDMethods(bdd, "currentAbsState",currentAbsState);
//									UtilityMethods.debugBDDMethods(bdd, "nextAbsState",nextAbsState);
//									UtilityMethods.debugBDDMethods(bdd, "current abs state test",currentAbsStateTest);
//									UtilityMethods.debugBDDMethods(bdd, "next abs state test",nextAbsStateTest);
//									UtilityMethods.getUserInput();
									
									
//									System.out.println("computing abstract states");
									int nextAbsStatePrime = BDDWrapper.replace(bdd, nextAbsState, vTovPrime);
//									System.out.println("replacing");
									BDDWrapper.free(bdd, nextAbsState);
									int abstractTrans = bdd.ref(bdd.and(currentAbsState, concreteGame.envActions[k]));
									abstractTrans=bdd.andTo(abstractTrans, nextAbsStatePrime);
									BDDWrapper.free(bdd, currentAbsState);
									BDDWrapper.free(bdd, nextAbsStatePrime);
									
//									UtilityMethods.debugBDDMethods(bdd, "abstract trans is", abstractTrans);
									
									
			//						bdd.deref(primeAbstractStates);
									
									abstractT_env = bdd.orTo(abstractT_env, abstractTrans);
									
//									UtilityMethods.debugBDDMethods(bdd, "abstract T_env so far", abstractT_env);
									
									bdd.deref(abstractTrans);
								}
								BDDWrapper.free(bdd, abstractTransExists);
							}
						}
						BDDWrapper.free(bdd, transWithEqClassSource);
					}
					
				}
				bdd.deref(transWithAction);
			}
			
		}
		
		UtilityMethods.duration(t_env, "T_env was computed in ");
//		BDDWrapper.BDD_Usage(bdd);
		return abstractT_env;
		
	}
	
	public int computeApproximateT_env(){
		long t_env = UtilityMethods.timeStamp();
		
		int abstractT_env=bdd.ref(bdd.getZero());
		int concreteVariablesCube = concreteGame.getVariablesCube();
		int concreteActionsCube = concreteGame.getActionsCube();
		int concretePrimeVariablesCube = concreteGame.getPrimeVariablesCube();
		int concreteCube = concreteGame.getVariablesAndPrimedVariablesCube();
		
//		System.out.println("computing abstract T_env");
		
//		for(Integer trans : concreteGame.T_envList){
		while(concreteGame.hasNextTransition(Player.ENVIRONMENT)){
			int trans = concreteGame.getNextEnvTransition();
//			UtilityMethods.debugBDDMethods(bdd, "concrete transition", trans);
			int stateTransition = bdd.ref(bdd.exists(trans, concreteActionsCube));
			int states = bdd.ref(bdd.exists(stateTransition, concretePrimeVariablesCube));
			int primeStates = bdd.ref(bdd.exists(stateTransition, concreteVariablesCube));
			int actions = bdd.ref(bdd.exists(trans,concreteCube));
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete states", states);
//			UtilityMethods.debugBDDMethods(bdd, "concrete primed states", primeStates);
//			UtilityMethods.debugBDDMethods(bdd, "concrete actions", actions);
//			
//			UtilityMethods.getUserInput();
			
			bdd.deref(stateTransition);
			
			int abstractStates = approximateAbstractionFunction(states);
			bdd.deref(states);
			
//			UtilityMethods.debugBDDMethods(bdd, "abstract state is", abstractStates);
			
			int nextStates=bdd.ref(bdd.replace(primeStates, concreteGame.vPrimeTov));
			bdd.deref(primeStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "next states are", nextStates);
			
			int nextAbstractStates=approximateAbstractionFunction(nextStates);
			bdd.deref(nextStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "next abstract states are", nextAbstractStates);
			
//			int primeAbstractStates=bdd.ref(bdd.replace(nextAbstractStates, vTovPrime));
//			bdd.deref(nextAbstractStates);
//			
//			UtilityMethods.debugBDDMethods(bdd, "next primed abstract states are",primeAbstractStates);
			
			
			int[] absStates = enumerateAbstractStates(abstractStates);
			int[] nextAbsStates = enumerateAbstractStates(nextAbstractStates);
			
			int[] nextAbsStatesPrime = BDDWrapper.replace(bdd, nextAbsStates,vTovPrime);
			int[] concreteStatesForAbsStates = concretize(absStates);
			int[] nextConcreteStatesForNextAbsStates = concretize(nextAbsStates);
			int[] nextConcreteStatesForNextAbsStatesPrime = BDDWrapper.replace(bdd, nextConcreteStatesForNextAbsStates, concreteGame.vTovPrime);
			
			for(int i=0; i<absStates.length;i++){
				int currentAbsState = absStates[i];
				int currentConcreteStates = concreteStatesForAbsStates[i];
				
//				UtilityMethods.debugBDDMethods(bdd, "currentAbsstate",currentAbsState);
//				UtilityMethods.debugBDDMethods(bdd, "currentconcstate",currentConcreteStates);
				
				for(int j=0;j<nextAbsStates.length;j++){
//					int nextAbsState = nextAbsStates[j];
//					UtilityMethods.debugBDDMethods(bdd, "nextABsstate",nextAbsState);
					int nextAbsStatePrime = nextAbsStatesPrime[j];
//					UtilityMethods.debugBDDMethods(bdd, "nextABsstatePrime",nextAbsStatePrime);
					
//					int nextConcreteStates = nextConcreteStatesForNextAbsStates[j];
					int nextConcreteStatesPrime = nextConcreteStatesForNextAbsStatesPrime[j];
//					UtilityMethods.debugBDDMethods(bdd, "nextConcretestatePrime",nextConcreteStatesPrime);
					
					for(int k=0; k<concreteGame.envActions.length;k++){
						int imp = bdd.ref(bdd.imp(concreteGame.envActions[k], actions));
						if(imp == 1){//if this action is enabled
					
							int concreteTrans = bdd.ref(bdd.and(currentConcreteStates, nextConcreteStatesPrime));
							concreteTrans=bdd.andTo(concreteTrans, concreteGame.envActions[k]);
							
//							UtilityMethods.debugBDDMethods(bdd, "actions are ", actions);
//							UtilityMethods.debugBDDMethods(bdd, "concrete Trans is ", concreteTrans);
							
							int absTransExists = BDDWrapper.intersect(bdd, concreteTrans, trans);
							
							if(absTransExists != 0){
								int abstractTrans = bdd.ref(bdd.and(currentAbsState, concreteGame.envActions[k]));
								abstractTrans=bdd.andTo(abstractTrans, nextAbsStatePrime);
								
//								UtilityMethods.debugBDDMethods(bdd, "abstract trans is", abstractTrans);
								
								
		//						bdd.deref(primeAbstractStates);
								
								abstractT_env = bdd.orTo(abstractT_env, abstractTrans);
								
//								UtilityMethods.debugBDDMethods(bdd, "abstract T_env so far", abstractT_env);
								
								bdd.deref(abstractTrans);
								
//								UtilityMethods.prompt("press enter to continue");
							}
		//					
							bdd.deref(concreteTrans);
							bdd.deref(absTransExists);
						}
						bdd.deref(imp);
					}
				}
				
			}
			
//			for(int i=0;i<absStates.length;i++){
//				bdd.deref(absStates[i]);
//			}
//			
//			for(int i=0;i<nextAbsStates.length;i++){
//				bdd.deref(nextAbsStates[i]);
//			}
			
			BDDWrapper.free(bdd, absStates);
			BDDWrapper.free(bdd, nextAbsStates);
			BDDWrapper.free(bdd, nextAbsStatesPrime);
			BDDWrapper.free(bdd, concreteStatesForAbsStates);
			BDDWrapper.free(bdd, nextConcreteStatesForNextAbsStates);
			BDDWrapper.free(bdd, nextConcreteStatesForNextAbsStatesPrime);
			
			
			bdd.deref(actions);
			bdd.deref(abstractStates);
			bdd.deref(nextAbstractStates);
			
		}
		
		UtilityMethods.duration(t_env, "T_env computed in ", 500);
		BDDWrapper.BDD_Usage(bdd);
		
		return abstractT_env;
	}
	
	public int[] concretize(int[] abstractStates){
		int[] concreteStates = new int[abstractStates.length];
		for(int i=0;i<concreteStates.length;i++){
			concreteStates[i]=concretize(abstractStates[i]);
		}
		return concreteStates;
	}
	
	public int computeT_sys(){
		long t_sys = UtilityMethods.timeStamp();
		
		int abstractT_sys=bdd.ref(bdd.getZero());
		
		while(concreteGame.hasNextTransition(Player.SYSTEM)){
			int trans = concreteGame.getNextSysTransition();
			
//			UtilityMethods.debugBDDMethods(bdd, "computing T_sys, current trans is ", trans);
//			UtilityMethods.getUserInput();
			
			for(int k=0; k<concreteGame.sysActions.length;k++){
				
				int transWithAction = BDDWrapper.intersect(bdd, trans, concreteGame.sysActions[k]);
				if(transWithAction != 0){
					for(int i=0; i<equivalenceClasses.length; i++){
						int avl_act = BDDWrapper.diff(bdd, equivalenceClasses[i], concreteGame.sysAvlActions[k]);
						if(avl_act == 0){ //action is available in all concrete states
							int transWithEqClassSource = BDDWrapper.intersect(bdd, transWithAction, equivalenceClasses[i]);
							if(transWithEqClassSource !=0){
								for(int j=0; j<equivalenceClassesPrime.length;j++){
									int abstractTransExists = BDDWrapper.intersect(bdd, transWithEqClassSource, equivalenceClassesPrime[j]);
									if(abstractTransExists != 0){
										int currentAbsState = equivalenceClassToAbstractStateMap(i);
										int nextAbsState = equivalenceClassToAbstractStateMap(j);
										int nextAbsStatePrime = BDDWrapper.replace(bdd, nextAbsState, vTovPrime);
										BDDWrapper.free(bdd, nextAbsState);
										int abstractTrans = bdd.ref(bdd.and(currentAbsState, concreteGame.sysActions[k]));
										abstractTrans=bdd.andTo(abstractTrans, nextAbsStatePrime);
										BDDWrapper.free(bdd, currentAbsState);
										BDDWrapper.free(bdd, nextAbsStatePrime);
	//									UtilityMethods.debugBDDMethods(bdd, "abstract trans is", abstractTrans);
										
										
				//						bdd.deref(primeAbstractStates);
										
										abstractT_sys = bdd.orTo(abstractT_sys, abstractTrans);
										
	//									UtilityMethods.debugBDDMethods(bdd, "abstract T_env so far", abstractT_env);
										
										bdd.deref(abstractTrans);
									}
									BDDWrapper.free(bdd, abstractTransExists);
								}
							}
							BDDWrapper.free(bdd, transWithEqClassSource);
						}
						BDDWrapper.free(bdd, avl_act);
					}
					
				}
				bdd.deref(transWithAction);
			}
			
		}
		
		UtilityMethods.duration(t_sys, "T_sys was computed in ");
//		BDDWrapper.BDD_Usage(bdd);
		return abstractT_sys;
	}
	
	public int computeApproximateT_sys(){
		long t_sys = UtilityMethods.timeStamp();
		int abstractT_sys=bdd.ref(bdd.getZero());
		int concreteVariablesCube = concreteGame.getVariablesCube();
		int concreteActionsCube = concreteGame.getActionsCube();
		int concretePrimeVariablesCube = concreteGame.getPrimeVariablesCube();
		int concreteCube = concreteGame.getVariablesAndPrimedVariablesCube();
		
//		System.out.println("Computing T_sys");

		
//		for(Integer trans : concreteGame.T_sysList){
		while(concreteGame.hasNextTransition(Player.SYSTEM)){
			int trans = concreteGame.getNextSysTransition();
			if(trans == 0) UtilityMethods.debugBDDMethods(bdd, "concrete transition", trans);
//			UtilityMethods.getUserInput();
			
			int stateTransition = bdd.ref(bdd.exists(trans, concreteActionsCube));
			int states = bdd.ref(bdd.exists(stateTransition, concretePrimeVariablesCube));
			int primeStates = bdd.ref(bdd.exists(stateTransition, concreteVariablesCube));
			int actions = bdd.ref(bdd.exists(trans,concreteCube));
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete states", states);
//			UtilityMethods.debugBDDMethods(bdd, "concrete primed states", primeStates);
//			UtilityMethods.debugBDDMethods(bdd, "concrete actions", actions);
//			UtilityMethods.getUserInput();
			
//			int[] possibleActions = UtilityMethods.enumerate(bdd, actions, concreteGame.actionVars);
			
			bdd.deref(stateTransition);
			
			int abstractStates = approximateAbstractionFunction(states);
			bdd.deref(states);
			
//			UtilityMethods.debugBDDMethods(bdd, "abstract state is", abstractStates);
			
			int nextStates=bdd.ref(bdd.replace(primeStates, concreteGame.vPrimeTov));
			bdd.deref(primeStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "next states are", nextStates);
			
			int nextAbstractStates=approximateAbstractionFunction(nextStates);
			bdd.deref(nextStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "next abstract states are", nextAbstractStates);
			
//			int primeAbstractStates=bdd.ref(bdd.replace(nextAbstractStates, vTovPrime));
//			bdd.deref(nextAbstractStates);
			
			int[] absStates = enumerateAbstractStates(abstractStates);
			int[] nextAbsStates = enumerateAbstractStates(nextAbstractStates);
			
			if(nextAbsStates == null){
				System.err.println("next abs states are null");
			}
			
			int[] nextAbsStatesPrime = BDDWrapper.replace(bdd, nextAbsStates,vTovPrime);
			int[] concreteStatesForAbsStates = concretize(absStates);
			int[] nextConcreteStatesForNextAbsStates = concretize(nextAbsStates);
			int[] nextConcreteStatesForNextAbsStatesPrime = BDDWrapper.replace(bdd, nextConcreteStatesForNextAbsStates, concreteGame.vTovPrime);
			
			for(int i=0; i<concreteGame.sysActions.length;i++){
				int imp = bdd.ref(bdd.imp(concreteGame.sysActions[i], actions));
				if(imp == 1){//if this action is enabled
//					UtilityMethods.debugBDDMethods(bdd, "asbtractstates", abstractStates);
					
					for(int j=0; j<absStates.length;j++){
						int currentAbsState = absStates[j];
						int currentConcreteStates = concreteStatesForAbsStates[j];
						if(currentConcreteStates==0){
							continue; //skip the abstract state with empty concrete states
						}
						int avl_act = BDDWrapper.diff(bdd, currentConcreteStates, concreteGame.sysAvlActions[i]);
						
//						UtilityMethods.debugBDDMethods(bdd, "current concrete states", currentConcreteStates);
//						UtilityMethods.debugBDDMethods(bdd, "considered action is", concreteGame.actions[i]);
//						UtilityMethods.debugBDDMethods(bdd,"available at states", concreteGame.sysAvlActions[i]);
//						UtilityMethods.debugBDDMethods(bdd, "diff is", avl_act);
//						UtilityMethods.getUserInput();
						
						if(avl_act == 0){ //action is available in all concrete states
							
//							UtilityMethods.debugBDDMethods(bdd, "action is available",concreteGame.sysAvlActions[i]);
//							UtilityMethods.getUserInput();
							
							for(int k=0;k<nextAbsStates.length;k++){
								int nextAbsStatePrime = nextAbsStatesPrime[k];
								
								
//								int nextConcreteStates = concretize(nextAbsStates[k]);
								int nextConcreteStatesPrime = nextConcreteStatesForNextAbsStatesPrime[k];
								
								int concreteTrans = bdd.ref(bdd.and(currentConcreteStates, nextConcreteStatesPrime));
								concreteTrans=bdd.andTo(concreteTrans, concreteGame.sysActions[i]);
								
								int absTransExists = BDDWrapper.intersect(bdd, concreteTrans, trans);
								
								if(absTransExists != 0){
									int abstractTrans = bdd.ref(bdd.and(currentAbsState, concreteGame.sysActions[i]));
									abstractTrans=bdd.andTo(abstractTrans, nextAbsStatePrime);
									
//									UtilityMethods.debugBDDMethods(bdd, "abstract trans is", abstractTrans);
									
									abstractT_sys = bdd.orTo(abstractT_sys, abstractTrans);
									bdd.deref(abstractTrans);
									
//									UtilityMethods.debugBDDMethods(bdd, "abstract T_sys so far", abstractT_sys);
									
//									UtilityMethods.prompt("press enter to continue");
								}
								bdd.deref(concreteTrans);
								bdd.deref(absTransExists);
								
							}
							
							
//							bdd.deref(currentAbsState);
							
							
							
						}
						bdd.deref(avl_act);
					}
				}
				bdd.deref(imp);
			}
			
//			for(int i=0;i<absStates.length;i++){
//				bdd.deref(absStates[i]);
//			}
//			
//			for(int i=0;i<nextAbsStates.length;i++){
//				bdd.deref(nextAbsStates[i]);
//			}
			
			BDDWrapper.free(bdd, absStates);
			BDDWrapper.free(bdd, nextAbsStates);
			BDDWrapper.free(bdd, nextAbsStatesPrime);
			BDDWrapper.free(bdd, concreteStatesForAbsStates);
			BDDWrapper.free(bdd, nextConcreteStatesForNextAbsStates);
			BDDWrapper.free(bdd, nextConcreteStatesForNextAbsStatesPrime);
			
			bdd.deref(actions);
			bdd.deref(abstractStates);
			bdd.deref(nextAbstractStates);
//			bdd.deref(primeAbstractStates);
		}
		
		UtilityMethods.duration(t_sys, "T_sys computed in ", 500);
		BDDWrapper.BDD_Usage(bdd);
		
		return abstractT_sys;
	}
	
	//TODO: I changed to abstractFunction due to incompatibility between abstract predicates and eq classes
	//Find another solution
	public int approximateAbstractionFunction(int predicate){
////		System.out.println("in approximate abstract function, predicate is ");
////		bdd.printSet(predicate);
//		if(this.variables.length != abstractPredicates.length){
//			System.out.println("the length of variables is not the same as the length of abstract predicates!!");
//			return -1;
//		}
//		
//		if(predicate == 0){//if predicate is false, return false
//			return bdd.getZero();
//		}
//		
//		int abstractState = bdd.ref(bdd.getOne());
//		for(int i=0;i<abstractPredicates.length;i++){
//			int imp = bdd.ref(bdd.imp(predicate, abstractPredicates[i]));
//			if(imp==1){
////				System.out.println("predicate implies abstract predicate "+i);
//				abstractState = bdd.andTo(abstractState, variables[i].getBDDVar());
//			}else{
//				int notPred = bdd.ref(bdd.not(abstractPredicates[i]));
//				int notImp = bdd.ref(bdd.imp(predicate, notPred));
//				if(notImp == 1){
////					System.out.println("predicate implies complement abstract predicate "+i);
//					abstractState = bdd.andTo(abstractState, bdd.not(variables[i].getBDDVar()));
//				}
//				bdd.deref(notPred);
//				bdd.deref(notImp);	
//			}
//			bdd.deref(imp);
//			
//			
//		}
//		return abstractState;
		
		return abstractionFunction(predicate);
	}
	
	public AbstractGame(BDD argBdd, int[] eqClasses, Game argConcreteGame){
		super(argBdd);
		equivalenceClasses = BDDWrapper.copyWithReference(bdd, eqClasses);
		computeAbstraction(equivalenceClasses, argConcreteGame);
	}
	
	//
	private void computeAbstraction(int[] eqClasses, Game argConcreteGame){
		
		System.out.println("computing an abstraction, number of eq classes are "+eqClasses.length);
		
		equivalenceClasses=eqClasses;
		concreteGame=argConcreteGame;
		
//		printEquivalenceClasses();
//		UtilityMethods.getUserInput();
		
		equivalenceClassesPrime = BDDWrapper.replace(bdd, equivalenceClasses, concreteGame.vTovPrime);
		
//		this.variables=createAbstractVariables(eqClasses, "absVars");
//		
//		this.primedVariables=Variable.createPrimeVariables(bdd, this.variables);
		
		createAbstractVariables();
		
//		abstractStates = UtilityMethods.enumerate(bdd, Variable.getBDDVars(variables));
		
//		System.out.println("vars and prime vars created");
//		Variable.printVariables(variables);
//		Variable.printVariables(primedVariables);
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		actionVars=concreteGame.actionVars;
		actionsCube=-1;
		
		//hatI
		//create the initial relation \hat{I} of the abstract game
		this.init = computeApproximateInit();
		
		//hatT_env
		//create the transition relation \hat{T}_env of the abstract game
//		this.T_env=computeApproximateT_env();
		this.T_env = computeT_env();
		T_envList = new ArrayList<Integer>();
		T_envList.add(bdd.ref(T_env));
		
		//hatT_sys
//		this.T_sys=computeApproximateT_sys();
		this.T_sys=computeT_sys();
		T_sysList = new ArrayList<Integer>();
		T_sysList.add(bdd.ref(T_sys));
		
		//System.out.println("transition relations were created");
		
		setActionMap(concreteGame.getActionMap());
		
		//Assumes that we will never free the concrete game
		this.actions = concreteGame.actions;
		
		this.envActionVars = concreteGame.envActionVars;
		this.sysActionVars = concreteGame.sysActionVars;
		this.envActions = concreteGame.envActions;
		this.sysActions = concreteGame.sysActions;
		
		actionAvailabilitySets();
		
		BDDWrapper.free(bdd,equivalenceClassesPrime);
	}
	
//	
	
	public Variable[] createAbstractVariables(int[] equivalenceClasses, String namePrefix){
		//create the abstract variables based on equivalence classes
		int numOfBits=UtilityMethods.numOfBits(equivalenceClasses.length-1);
		Variable[] abstractVariables=new Variable[numOfBits];
		for(int i=0; i<abstractVariables.length;i++){
			abstractVariables[i]=new Variable(bdd, namePrefix+i);
		}
		return abstractVariables;
	}
	
	public static Variable[] createAbstractVariables(int[] equivalenceClasses, Variable[] concreteVars){
		int numOfBits=UtilityMethods.numOfBits(equivalenceClasses.length-1);
		Variable[] abstractVars = new Variable[numOfBits];
		for(int i=0; i<numOfBits; i++){
//			abstractVars[i]=concreteVars[i];
			if(i==0){
				abstractVars[i]=getVariableWithLowestPosition(concreteVars, 0);
			}else{
				abstractVars[i]=getVariableWithLowestPosition(concreteVars, abstractVars[i-1].getBDDPosition()+1);
			}
		}
		return  abstractVars;
	}
	
	public void createAbstractVariables(){
		int numOfBits=UtilityMethods.numOfBits(equivalenceClasses.length-1);
		variables = new Variable[numOfBits];
		for(int i=0; i<numOfBits; i++){
//			variables[i]=concreteGame.variables[i];
			if(i==0){
				variables[i]=getVariableWithLowestPosition(concreteGame.variables, 0);
			}else{
//				variables[i-1].print();
//				System.out.println("next position is "+(variables[i-1].getBDDPosition()+1));
				variables[i]=getVariableWithLowestPosition(concreteGame.variables, variables[i-1].getBDDPosition()+1);
//				variables[i].print();
			}
		}
		
		primedVariables = Variable.getPrimedCopy(variables);
		
//		Variable.printVariables(variables);
//		Variable.printVariables(concreteGame.variables);
	}
	
	private static Variable getVariableWithLowestPosition(Variable[] vars, int position){
		Variable chosen = vars[0];
		for(int i=1;i<vars.length;i++){
//			System.out.println("vars position is "+vars[i].getBDDPosition());
//			System.out.println("current lowest position is "+position);
			if(vars[i].getBDDPosition() >= position){
				if(chosen.getBDDPosition()> vars[i].getBDDPosition() || chosen.getBDDPosition()< position){
					chosen = vars[i];
				}
			}
		}
		return chosen;
	}
	

	
	/**
	 * given the BDD representation of the abstract state(s), the union of the concrete states included in 
	 * them is computed
	 * @param abstractStates
	 * @return
	 */
	public int concretize(int abstractStates){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, abstractStates, variables);
		if(minterms == null){
			System.out.println("in abstractGame, concretize, the set of abstract states is empty, returning false");
			return bdd.ref(bdd.getZero());
		}
		int[] eqClassIndices=eqClassIndices(minterms);
		int concreteStates=bdd.ref(bdd.getZero());
		for(int i=0;i<eqClassIndices.length;i++){
			concreteStates=bdd.orTo(concreteStates, equivalenceClasses[eqClassIndices[i]]);
		}
		return concreteStates;
	}
	
	/**
	 * returns the set of individual abstract states included in the given formula abstract state
	 * @param abstractStates
	 * @return
	 */
	public int[] enumerateAbstractStates(int abstractStates){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, abstractStates, variables);
		
		//the set of abstract states is empty
		if(minterms == null){
			return null;
		}
		
		int[] abstractStateList=new int[minterms.size()];
		for(int i=0;i<minterms.size();i++){
			abstractStateList[i]=BDDWrapper.assign(bdd, minterms.get(i), variables);
		}
		return abstractStateList;
	}
	

	
	public int[] eqClassIndices(int abstractStates){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, abstractStates, variables);
		int[] eqClassIndices=eqClassIndices(minterms);
		return eqClassIndices;
	}
	
	public int[] eqClassIndices(ArrayList<String> minterms){
		int[] eqClassIndices=new int[minterms.size()];
		for(int i=0;i<eqClassIndices.length;i++){
			eqClassIndices[i]=Integer.parseInt(minterms.get(i), 2);
		}
		return eqClassIndices;
	}
	
	/**
	 * returns the abstract state which includes the concrete states, -1 if no such abstract state exists
	 * @param concreteState
	 * @return
	 */
	public int abstractState(int concreteStates){
		for(int i=0; i<equivalenceClasses.length;i++){
			int diff = BDDWrapper.diff(bdd, concreteStates, equivalenceClasses[i]);
			if(diff == 0){
				bdd.deref(diff);
				return equivalenceClassToAbstractStateMap(i);
			}
			bdd.deref(diff);
		}
		return -1;
	}
	
	public int[] getEquivalenceClasses(){
		return equivalenceClasses;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public int equivalenceClassToAbstractStateMap(int index){
		return BDDWrapper.assign(bdd, index , variables);
	}
	
	public void printEquivalenceClasses(){
		System.out.println("\n\neq classes are");
		for(int i=0; i<equivalenceClasses.length;i++){
			if(equivalenceClasses[i]==0) continue;
			System.out.println("eq class for valuation "+Integer.toBinaryString(i));
			bdd.printSet(equivalenceClasses[i]);
			
//			UtilityMethods.getUserInput();
			
//			Variable[] vars = Variable.unionVariables(concreteGame.actionVars, concreteGame.variables);
//			vars=Variable.unionVariables(vars, concreteGame.primedVariables);
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, equivalenceClasses[i], vars));
		}
	}
	
	public void printGame(){
		super.printGame();
		//System.out.println("Equivalence classes are");
//		UtilityMethods.getUserInput();
		printEquivalenceClasses();
		
	}
	
	public static AbstractGame compose(ArrayList<AbstractGame> abstractGames){
		System.out.println("composing the abstract games");
		if(abstractGames == null || abstractGames.size() ==0){
			return null;
		}
		AbstractGame composedAbsGames = abstractGames.get(0);
		for(int i=1;i<abstractGames.size();i++){
			composedAbsGames.compose(abstractGames.get(i));
		}
		return composedAbsGames;
	}
	
	public AbstractGame compose(AbstractGame absGame){
//		System.out.println("composing the abstract games not static");
		int[] eqClasses1 = getEquivalenceClasses();
		int[] eqClasses2 = absGame.getEquivalenceClasses();
		ArrayList<Integer> eqClassesArrayList= new ArrayList<Integer>();
		ArrayList<Integer> eqClasses1Indices=new ArrayList<Integer>();
		ArrayList<Integer> eqClasses2Indices=new ArrayList<Integer>();
		
		//create the abstract states based on equivalence classes which are not empty
		for(int i=0;i<eqClasses1.length;i++){
			for(int j=0;j<eqClasses2.length;j++){
				int newEqClass=bdd.ref(bdd.and(eqClasses1[i], eqClasses2[j]));
				if(newEqClass!=0){
					eqClassesArrayList.add(newEqClass);
					eqClasses1Indices.add(i);
					eqClasses2Indices.add(j);
				}
			}
		}
		
		int[] equivalenceClasses = UtilityMethods.IntegerArrayListTointArray(eqClassesArrayList);
		
//		Variable[] abstractVars=createAbstractVariables(equivalenceClasses, "absVars");
//		Variable[] abstractVarsPrime=Variable.createPrimeVariables(bdd, abstractVars);
		
		Variable[] abstractVars=AbstractGame.createAbstractVariables(equivalenceClasses, concreteGame.variables);
		Variable[] abstractVarsPrime=Variable.getPrimedCopy(abstractVars);
		
		
		
//		Variable[] absGameActionVars = Variable.unionVariables(actionVars, absGame.actionVars);
		Variable[] absGameEnvActionVars = Variable.unionVariables(envActionVars, absGame.envActionVars);
		Variable[] absGameSysActionVars = Variable.unionVariables(sysActionVars, absGame.sysActionVars);
		
		//hatI
		int hatI=bdd.ref(bdd.getZero());
		int hatI_1=getInit();
		int hatI_2=absGame.getInit();
		for(int i=0;i<eqClasses1Indices.size();i++){
			int abstractState1Index = eqClasses1Indices.get(i);
			int abstractState1=equivalenceClassToAbstractStateMap(abstractState1Index);
			int isInit = BDDWrapper.intersect(bdd, hatI_1, abstractState1);
			if(isInit!=0){
				int abstractState2Index = eqClasses2Indices.get(i);
				int abstractState2=absGame.equivalenceClassToAbstractStateMap(abstractState2Index);
				int isInit2 = BDDWrapper.intersect(bdd, hatI_2, abstractState2);
				if(isInit2 != 0 ){
					//the new equivalence class corresponding to eqclasses i and j is an init state for the new
					//abstract game
					int initAbstractState= BDDWrapper.assign(bdd, i, abstractVars);
					hatI=bdd.orTo(hatI, initAbstractState);
					bdd.deref(initAbstractState);
				}
			}
			
		}
		
		//hat_T_env & 	//hat_T_sys
		int hatT_env=bdd.ref(bdd.getZero());
		int hatT_sys=bdd.ref(bdd.getZero());
		int hatT_env1=getEnvironmentTransitionRelation();
		int hatT_env2=absGame.getEnvironmentTransitionRelation();
		int hatT_sys1=getSystemTransitionRelation();
		int hatT_sys2=absGame.getSystemTransitionRelation();
		
		int cube1 = bdd.ref(bdd.and(getVariablesCube(), getPrimeVariablesCube()));
		int cube2 = bdd.ref(bdd.and(absGame.getVariablesCube(), absGame.getPrimeVariablesCube()));
		
		for(int i=0;i<equivalenceClasses.length;i++){
			int abstractState_i=BDDWrapper.assign(bdd, i, abstractVars);
			int abstractState1Index_i = eqClasses1Indices.get(i);
			int abstractState1_i=equivalenceClassToAbstractStateMap(abstractState1Index_i);
			int abstractState2Index_i = eqClasses2Indices.get(i);
			int abstractState2_i=absGame.equivalenceClassToAbstractStateMap(abstractState2Index_i);
			for(int j=0;j<equivalenceClasses.length;j++){
				int abstractState_j_prime=BDDWrapper.assign(bdd, j, abstractVarsPrime);
				
				int abstractState1Index_j = eqClasses1Indices.get(j);
				int abstractState1_j=equivalenceClassToAbstractStateMap(abstractState1Index_j);
				int abstractState1_j_prime = bdd.ref(bdd.replace(abstractState1_j, this.vTovPrime));
				bdd.deref(abstractState1_j);
				
				int abstractState2Index_j = eqClasses2Indices.get(j);
				int abstractState2_j=absGame.equivalenceClassToAbstractStateMap(abstractState2Index_j);
				int abstractState2_j_prime = bdd.ref(bdd.replace(abstractState2_j, absGame.vTovPrime));
				bdd.deref(abstractState2_j);
				
				//if there is a transition between abstractState1_i to abstractState1_j and the same for 
				//the second game, add a transition between abstractState_i and abstractState_j
				int trans1=bdd.ref(bdd.and(abstractState1_i, abstractState1_j_prime));
				bdd.deref(abstractState1_j_prime);
				int envTrans1=bdd.ref(bdd.and(trans1, hatT_env1));
				int sysTrans1=bdd.ref(bdd.and(trans1, hatT_sys1));
				bdd.deref(trans1);
				int envActions1=bdd.ref(bdd.exists(envTrans1, cube1));
				int sysActions1=bdd.ref(bdd.exists(sysTrans1, cube1));
				bdd.deref(envTrans1);
				bdd.deref(sysTrans1);
				
				int trans2=bdd.ref(bdd.and(abstractState2_i, abstractState2_j_prime));
				bdd.deref(abstractState2_j_prime);
				int envTrans2=bdd.ref(bdd.and(trans2, hatT_env2));
				int sysTrans2=bdd.ref(bdd.and(trans2, hatT_sys2));
				bdd.deref(trans2);
				int envActions2=bdd.ref(bdd.exists(envTrans2, cube2));
				int sysActions2=bdd.ref(bdd.exists(sysTrans2, cube2));
				bdd.deref(envTrans2);
				bdd.deref(sysTrans2);
				
				int envActions = bdd.ref(bdd.and(envActions1, envActions2));
				bdd.deref(envActions1);
				bdd.deref(envActions2);
				if(envActions!=0){
					int transition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
					transition=bdd.andTo(transition, envActions);
					hatT_env=bdd.orTo(hatT_env, transition);
					bdd.deref(transition);
				}
				
				int sysActions = bdd.ref(bdd.and(sysActions1, sysActions2));
				bdd.deref(sysActions1);
				bdd.deref(sysActions2);
				if(sysActions!=0){
					int transition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
					transition=bdd.andTo(transition, sysActions);
					hatT_sys=bdd.orTo(hatT_sys, transition);
					bdd.deref(transition);
				}
				bdd.deref(abstractState_j_prime);
				
			}
			bdd.deref(abstractState_i);
			bdd.deref(abstractState1_i);
			bdd.deref(abstractState2_i);
		}
		
	
		
		
		
		//concrete game
		ArrayList<Game> concreteGames=new ArrayList<Game>();
		concreteGames.add(concreteGame);
		concreteGames.add(absGame.concreteGame);
		Game compositeConcreteGame = new CompositeGame(bdd, concreteGames);
		
		AbstractGame abstraction=new AbstractGame(bdd, abstractVars, abstractVarsPrime, hatI, hatT_env, hatT_sys, absGameEnvActionVars, absGameSysActionVars, compositeConcreteGame, equivalenceClasses);
		return abstraction;
	}
	
	/**
	 * Transforms the objective for the concrete game to an abstract objective for the abstract game
	 * @param concreteObjective
	 * @param abstractGame
	 * @return
	 */
	public int computeAbstractObjective(int concreteObjective){
		
		int abstractObjective = bdd.ref(bdd.getZero());
		int[] eqClasses=getEquivalenceClasses();
		for(int i=0;i<eqClasses.length;i++){
			int diff = BDDWrapper.diff(bdd, eqClasses[i], concreteObjective);
			//if the equivalence class is a subset of states represented by the concrete objective
			if(diff == 0){
				//add the abstract state corresponding to this equivalence class to the abstract objective
				int abstractState = equivalenceClassToAbstractStateMap(i);
				abstractObjective=bdd.orTo(abstractObjective, abstractState);
				bdd.deref(abstractState);
			}
			bdd.deref(diff);
		}
		return abstractObjective;
	}
	

	public AbstractGame restrict(int restrictedT_env, int restrictedT_sys){
		AbstractGame restrictedAbsGame=new AbstractGame(bdd, this.variables, this.primedVariables, this.init, restrictedT_env, restrictedT_sys, this.envActionVars, this.sysActionVars,  this.concreteGame, this.equivalenceClasses);
		return restrictedAbsGame;
	}
	
	/**
	 * given the set of concrete states, computes the "minimal" set of abstract states which include them. 
	 * It is more complex than computing an approximate abstract function 
	 * @param concreteStates
	 * @return
	 */
	public int abstractionFunction(int concreteStates){
		int abstractStates = bdd.ref(bdd.getZero());
		int[] eqClasses=getEquivalenceClasses();
		for(int i=0;i<eqClasses.length;i++){
			int intersection = BDDWrapper.intersect(bdd, concreteStates, eqClasses[i]);
			//if the equivalence class is a subset of states represented by the concrete objective
			if(intersection != 0){
				//add the abstract state corresponding to this equivalence class to the abstract objective
				int abstractState = equivalenceClassToAbstractStateMap(i);
				abstractStates=bdd.orTo(abstractStates, abstractState);
				bdd.deref(abstractState);
			}
			bdd.deref(intersection);
		}
		return abstractStates;
	}
	
	//	public int mintermToBDDFormula(String minterm, Variable[] argVariables){
	////System.out.println("minterm to bdd formula");
	////System.out.println("min term is "+minterm);
	////Variable.printVariables(argVariables);
	//int state=bdd.ref(bdd.getOne());
	//for(int i=0;i<minterm.length();i++){
	//	if(minterm.charAt(i)=='0'){
	//		state=bdd.andTo(state, bdd.not(argVariables[i].getBDDVar()));
	//	}else{
	//		state=bdd.andTo(state, argVariables[i].getBDDVar());
	//	}
	//}
	//return state;
	//}
	
	/**
	 * Frees the memory used for the abstract game
	 */
	public void cleanUp(){
		super.cleanUp();
		
		//free equivalence classes
		BDDWrapper.free(bdd, equivalenceClasses);
		
		//free abstract predicates
		BDDWrapper.free(bdd, abstractPredicates);
		

		
//		BDDWrapper.free(bdd, init);
//		BDDWrapper.free(bdd, getEnvironmentTransitionRelation());
//		BDDWrapper.free(bdd, getSystemTransitionRelation());
//		BDDWrapper.free(bdd, equivalenceClasses);
		
		//TODO: each game must have its avl actions list ready! check the code
//		if(envAvlActions != null) BDDWrapper.free(bdd, envAvlActions);
//		if(sysAvlActions != null) BDDWrapper.free(bdd, sysAvlActions);
//		
//		if(actions != null) BDDWrapper.free(bdd, actions);
//		if(envActions != null) BDDWrapper.free(bdd, envActions);
//		if(sysActions != null) BDDWrapper.free(bdd, sysActions);
		
//		
	}
	
	public int concretizeTransitionRelation(int abstractTransitionRelation){
		int T = bdd.ref(bdd.getZero());
		
		int numOfEqClasses = equivalenceClasses.length;
		for(int i=0;i<numOfEqClasses; i++){
			for(int j=0; j< numOfEqClasses; j++){
				int abstractState = BDDWrapper.assign(bdd, i, variables);
				int nextAbsState = BDDWrapper.assign(bdd, j, primedVariables);
				int absTrans = BDDWrapper.and(bdd, abstractState, nextAbsState);
				BDDWrapper.free(bdd, abstractState);
				BDDWrapper.free(bdd, nextAbsState);
				
				int trans = BDDWrapper.and(bdd, absTrans, abstractTransitionRelation);
//				UtilityMethods.debugBDDMethods(bdd, "trans is", trans);
				bdd.deref(absTrans);
				int actions = bdd.ref(bdd.exists(trans, getVariablesAndPrimedVariablesCube()));
//				UtilityMethods.debugBDDMethods(bdd, "actions are", actions);
				BDDWrapper.free(bdd, trans);
				
				if(actions != 0){
					int eqClass_j_prime = bdd.ref(bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime));
					
					int concreteTransition = bdd.ref(bdd.and(equivalenceClasses[i],eqClass_j_prime));
					bdd.deref(eqClass_j_prime);
					concreteTransition = bdd.andTo(concreteTransition, actions);
					T = BDDWrapper.orTo(bdd, T, concreteTransition);
					
//					UtilityMethods.debugBDDMethods(bdd, "concrete trans is", concreteTransition);
//					UtilityMethods.debugBDDMethods(bdd, "T so far", T);
//					UtilityMethods.getUserInput();
					
					bdd.deref(concreteTransition);
					
				}
				
			}
		}
		
		return T;
		
	}
	
	public int restrictSysActions(){
		int actionsAvailableAtConcreteStates=bdd.ref(bdd.getZero());
		for(int i=0;i<sysAvlActions.length;i++){
			int act = sysActions[i];
//			UtilityMethods.debugBDDMethods(bdd, "restricting actions in abs game, act is", act);
//			UtilityMethods.debugBDDMethods(bdd, "restricting actions in abs game, sysAvlAct is", sysAvlActions[i]);
			int concreteStates=concretize(sysAvlActions[i]);
			int actAvlAtConcStates = bdd.ref(bdd.and(concreteStates, act));
			actionsAvailableAtConcreteStates=bdd.orTo(actionsAvailableAtConcreteStates, actAvlAtConcStates);
			bdd.deref(actAvlAtConcStates);
		}
		int restrictedConcreteT_sys = bdd.ref(bdd.and(concreteGame.getSystemTransitionRelation(), actionsAvailableAtConcreteStates));
		bdd.deref(actionsAvailableAtConcreteStates);
		return restrictedConcreteT_sys;
		
	}
	
	//TODO: clean up
	public Game concretizeAbstractStrategy(Game abstractStrategy){ 
//		int concreteT_env = concretizeTransitionRelation(abstractStrategy.getEnvironmentTransitionRelation());
		int concreteT_env = concreteGame.getEnvironmentTransitionRelation();
//		int concreteT_sys = concretizeTransitionRelation(abstractStrategy.getSystemTransitionRelation());
		int concreteT_sys = restrictSysActions();
		Game concreteStrategy = new Game(bdd, concreteGame.variables, concreteGame.primedVariables, concreteGame.getInit(), concreteT_env, concreteT_sys, abstractStrategy.envActionVars, abstractStrategy.sysActionVars);
		return concreteStrategy;
	}
	
	public Game concretizeAbstractStrategy(){ 
		System.out.println("concretizing the strategy");
//		int concreteT_env = concretizeTransitionRelation(abstractStrategy.getEnvironmentTransitionRelation());
		int concreteT_env = concreteGame.getEnvironmentTransitionRelation();
		
		
//		UtilityMethods.debugBDDMethods(bdd, "concrete T_env of the strategy", concreteT_env);
//		UtilityMethods.debugBDDMethods(bdd, "concrete T_sys of the strategy", concreteGame.getSystemTransitionRelation());
		
//		int concreteT_sys = concretizeTransitionRelation(abstractStrategy.getSystemTransitionRelation());
		int concreteT_sys = restrictSysActions();
		Game concreteStrategy = new Game(bdd, concreteGame.variables, concreteGame.primedVariables, concreteGame.getInit(), concreteT_env, concreteT_sys, envActionVars, sysActionVars);
		return concreteStrategy;
	}

	public int numOfVariables(){
		return variables.length + actionVars.length+ primedVariables.length;
	}
	
	//TODO: remove this method
	/**
	 * returns the set of states represented by the abstractState as a BDD
	 * valuation is the truth assignment to the abstract state variable
	 * @param valuation
	 * @return the BDD represeting the set of concrete states included in the abstract state, -1 if such equivalence class does not exist
	 */
	public int concretize(boolean[] valuation){
		int eqClassIndex = UtilityMethods.booleanArrayToInteger(valuation);
		if(eqClassIndex<equivalenceClasses.length){
			return equivalenceClasses[eqClassIndex];
		}
		System.err.println("something is wrong in concretization");
		return -1;
	}
	
//	private int computeInit(){
//		int hatI=bdd.ref(bdd.getZero());
//		for(int i=0;i<equivalenceClasses.length;i++){
//			int initIntersection=bdd.ref(bdd.and(concreteGame.getInit(), equivalenceClasses[i]));
//			//if intersection is not empty
//			if(initIntersection != 0){
//				int abstractState=equivalenceClassToAbstractStateMap(i);
//				hatI=bdd.orTo(hatI, abstractState);
//				bdd.deref(abstractState);
//			}
//			bdd.deref(initIntersection);
//		}
//		return hatI;
//	}
//	
//	private int computeT_env(){
//		//create the transition relation \hat{T}_env of the abstract game
//		int hatT_env=bdd.ref(bdd.getZero());
//		for(int i=0;i<equivalenceClasses.length;i++){
//			for(int j=0; j<equivalenceClasses.length;j++){
//				//if \exists s1 \in eqClass[i] and s2 \in eqClass[j] and l \in actions s.t. T_env(s,s',l), 
//				//then add eqClass[i],eqClass[j], l to hatT_env
//				int variablesCube=concreteGame.getVariablesCube();
//				int variablesPrimeCube=concreteGame.getPrimeVariablesCube();
////				UtilityMethods.debugBDDMethods(bdd, "eqClassj", equivalenceClasses[j]);
//				int eqClass_j_prime=bdd.ref(bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime));
////				UtilityMethods.debugBDDMethods(bdd, "eqClassjprime", eqClass_j_prime);
//				int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//				transition=bdd.andTo(transition, concreteGame.getEnvironmentTransitionRelation());
//								
////				UtilityMethods.debugBDDMethods(bdd, "computing possible trans between eq classes "+i+" & "+j, transition);
//								
//				int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
//				int actions = bdd.ref(bdd.exists(transition, cube));
//								
////				UtilityMethods.debugBDDMethods(bdd, "possible actions are", actions);
//								
//				bdd.deref(eqClass_j_prime);
//				bdd.deref(transition);
//								
//				//if the set of actions is not empty, add a transition between two abstract states representing
//				//the equivalence classes i and j
//				if(actions != 0){
//					int abstractState_i=equivalenceClassToAbstractStateMap(i);					
//					int abstractState_j_prime=bdd.ref(bdd.replace(equivalenceClassToAbstractStateMap(j),vTovPrime));
//					int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//					abstractTransition=bdd.andTo(abstractTransition, actions);
//					hatT_env=bdd.orTo(hatT_env, abstractTransition);
//					bdd.deref(abstractState_i);
//					bdd.deref(abstractState_j_prime);
//					bdd.deref(abstractTransition);
//				}
//				bdd.deref(actions);
//			}
//		}
//						
////			System.out.println("the abstract environment transition relation is ");
////			bdd.printSet(hatT_env);
//		return hatT_env;
//	}
//	
//	private int computeT_sys(){
//		//create the transition relation \hat{T}_sys of the abstract game
//		int hatT_sys=bdd.ref(bdd.getZero());
//		int[] actionsList = concreteGame.enumerateActions();
//
////		System.out.println("the set of actions");
////		for(int acccc: actionsList){
////			bdd.printSet(acccc);
////		}
//						
//		for(int i=0;i<equivalenceClasses.length;i++){
//			//first compute the set of available actions in the abstract state Avl^\alpha(v^\alpha)
//			//Avl^\alpha(v^\alpha) = \cap_(v \in v^\alpha) Avl(v)
//							
//			//for any action l, if eqClass[i] \subseteq Avl(l), then add l to Avl^\alpha
//			int abstractAvailabilityList_i=bdd.ref(bdd.getZero());
//			for(int act : actionsList){
//								
//	//			System.out.println("current action is");
//	//			bdd.printSet(act);
//									
//				//TODO: this repetitive, move it to outside of loop
//				int avl_act = concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());
//									
//	//			System.out.println("the set of availale states are ");
//	//			bdd.printSet(avl_act);
//									
//				int subset = BDDWrapper.diff(bdd, equivalenceClasses[i], avl_act);
//									
//	//			System.out.println("current eq class is ");
//	//			bdd.printSet(equivalenceClasses[i]);
//	//			System.out.println("current action is"+(subset!=0?" NOT ":" ")+ "available at all states");
//	//			bdd.printSet(subset);
//									
//				//if eqClass[i] is a subset of Avl(act)
//				if(subset == 0){
//					//add the action to the availability list of class i
//					abstractAvailabilityList_i=bdd.orTo(abstractAvailabilityList_i, act);
//				}
//									
//				bdd.deref(subset);
//				bdd.deref(avl_act);
//			}
//							
//	//		System.out.println("the availability list is ");
//	//		bdd.printSet(abstractAvailabilityList_i);
//								
//			if(abstractAvailabilityList_i!=0){
//				for(int j=0; j<equivalenceClasses.length;j++){
//					//if there exists action l and states v and v' such that avl^\alpha(l) & v \in eqClass[i]
//					// & v' \in eqClass[j] & T_sys(v,v',l), then add a transition between two abstract states representing
//					//the equivalence classes i and j
//										
//					int variablesCube=concreteGame.getVariablesCube();
//					int variablesPrimeCube=concreteGame.getPrimeVariablesCube();
//					int eqClass_j_prime=bdd.ref(bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime));
//					int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//					transition=bdd.andTo(transition, concreteGame.getSystemTransitionRelation());
//					int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
//					int existActions = bdd.ref(bdd.exists(transition, cube));
//					bdd.deref(eqClass_j_prime);
//					bdd.deref(transition);
//					//the set of available actions
//					int possibleActs = bdd.ref(bdd.and(existActions, abstractAvailabilityList_i));
//					bdd.deref(existActions);
//					if(possibleActs != 0){
//						int abstractState_i=equivalenceClassToAbstractStateMap(i);
//						int abstractState_j_prime=bdd.ref(bdd.replace(equivalenceClassToAbstractStateMap(j),vTovPrime));
//						int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//						abstractTransition=bdd.andTo(abstractTransition, possibleActs);
//						hatT_sys=bdd.orTo(hatT_sys, abstractTransition);
//						bdd.deref(abstractState_i);
//						bdd.deref(abstractState_j_prime);
//						bdd.deref(abstractTransition);
//					}
//					bdd.deref(possibleActs);
//				}
//			}
//		}
//						
//		for(int act : actionsList){
//			bdd.deref(act);
//		}
//		return hatT_sys;
//	}
	
//	public AbstractGame(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argActions, int[] argSysAvl, int[] argEnvAvl, Game argConcrete, int[] argEqClasses){
//	super(argBdd, argVariables, argPrimedVars, initial, envTrans, sysTrans, argActions, argSysAvl, argEnvAvl);
//	concreteGame = argConcrete;
//	equivalenceClasses = argEqClasses;
//	
//}
}
