package game;



import game.Objective.ObjectiveType;

import java.util.ArrayList;

import parametric.CoSafeControllerInterface;
import parametric.ParametricCoSafeController;
import parametric.ParametricGameStructure;
import utils.UtilityMethods;
import automaton.GameGraph;
import jdd.bdd.BDD;


//TODO: counter-strategy forward computation
//TODO: the algorithm for computing counter-strategy is very inefficient, revise 
//TODO: removing the unreachable parts of strategy and counter-strategy
//TODO: check the whole package for the correct use of "ref" and "deref"
//TODO: the game solver class needs a deep cleaning and renaming the methods. We only need the static methods
//There is no need to keeping the game structures and objectives

//TODO: We do not restrcit T_env to winning region in computed strategy, as a result it can have 
//unreachable losing states, can it cause any harm?
public class GameSolver {
	GameStructure game;
	int objective;
	BDD bdd;
//	Game strategyOfTheWinner;
	
	boolean solved;
	char winner;
	int winningRegion;
	
	
	public GameSolver(GameStructure g, int obj, BDD argbdd){
		game=g;
		objective=obj;
		bdd=argbdd;
		
		solved=false;
		winner='u'; //unknown
		winningRegion=-1;
	}
	
	
	
	public static void main(String[] args){

		
	}
	
	
	
	/**
	 * solves the given game with respect to the given objective
	 * returns a GameSolution object which encapsulates the information about the winner and its strategy
	 * @return
	 */
	public GameSolution solve(){
		GameStructure strategyOfTheWinner;
		Player winner;
		int controller = -1;
		if(isRealizable()){
//			UtilityMethods.prompt("inside the game solver, the winner is the system");
			winner = Player.SYSTEM;
//			strategyOfTheWinner = mostGeneralStrategyUnreachablePartRemoved();
			strategyOfTheWinner = mostGeneralStrategy();
			controller = synthesizeController();
		}else{
//			UtilityMethods.prompt("inside the game solver, the winner is the environment");
			winner = Player.ENVIRONMENT;
//			UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//			UtilityMethods.getUserInput();
//			strategyOfTheWinner = counterStrategyUnreachablePartRemoved();
			strategyOfTheWinner = counterStrategy();
		}
		GameSolution solution = new GameSolution(game, winner, strategyOfTheWinner, winningRegion, controller);
		return solution;
	}
	
	public static GameSolution solve(BDD bdd, GameStructure gs, int objective){
		int winningRegion=greatestFixedPoint(bdd, gs, objective);
		boolean isRealizable = BDDWrapper.subset(bdd, gs.getInit(), winningRegion);
		
		GameStructure strategyOfTheWinner;
		Player winner;
		int controller = -1;
		
		if(isRealizable){
//			UtilityMethods.prompt("inside the game solver, the winner is the system");
			winner = Player.SYSTEM;
//			strategyOfTheWinner = mostGeneralStrategyUnreachablePartRemoved();
			strategyOfTheWinner = mostGeneralStrategy(bdd, gs, winningRegion);
			controller = synthesizeController(bdd, gs, winningRegion);
		}else{
//			UtilityMethods.prompt("inside the game solver, the winner is the environment");
			winner = Player.ENVIRONMENT;
//			UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//			UtilityMethods.getUserInput();
//			strategyOfTheWinner = counterStrategyUnreachablePartRemoved();
			strategyOfTheWinner = counterStrategy(bdd, gs, objective);
		}
		GameSolution solution = new GameSolution(gs, winner, strategyOfTheWinner, winningRegion, controller);
		return solution;
	}
	
	public GameStructure mostGeneralStrategy(){
		return synthesize(winningRegion);
	}
	
	public static GameStructure mostGeneralStrategy(BDD bdd, GameStructure gameStructure, int winningRegion){
		return synthesize(bdd, gameStructure, winningRegion);
	}
	
	public boolean isRealizable(){
		winningRegion=greatestFixPoint(objective);
//		UtilityMethods.debugBDDMethods(bdd, "is realizable? wr is", winningRegion );
//		UtilityMethods.debugBDDMethods(bdd, "is realizable? objective is", objective );
//		UtilityMethods.debugBDDMethods(bdd, "is realizable? init is", game.init );
		solved =true;
		return BDDWrapper.subset(bdd, game.getInit(), winningRegion);
	}
	
	//TODO: check for correctness of ref and deref
	public int greatestFixPoint(int set){
		int Q=bdd.ref(bdd.getOne());
		//TODO: it was QPrime = set, but not sure deref ing QPrime effects set too or not
//		int Qprime=bdd.ref(bdd.and(set, bdd.getOne()));
		int Qprime = bdd.ref(set);
		do{
//		while(Q != Qprime){
//			if(Q != set) bdd.deref(Q);
			bdd.deref(Q);
			//TODO: it was Q=QPrime
//			Q=bdd.ref(bdd.and(Qprime, bdd.getOne()));
			Q = bdd.ref(Qprime);
			
//			UtilityMethods.debugBDDMethods(bdd, "gfp, Q is ", Q);
			
			
			
			int pre = game.controllablePredecessor(Qprime);
			
//			UtilityMethods.debugBDDMethods(bdd, "gfp, pre is ", pre);
			
			Qprime = bdd.ref(bdd.and(Qprime, pre));
			
//			UtilityMethods.debugBDDMethods(bdd,"in gfp, Qprime is", Qprime);
			
			bdd.deref(pre);	
		}while(Q != Qprime);
//		if(Qprime != set) bdd.deref(Qprime);
		bdd.deref(Qprime);
		return Q;
	}
	
	public static int greatestFixedPoint(BDD bdd, GameStructure game, int set){
		int Q=bdd.ref(bdd.getOne());
		int Qprime = bdd.ref(set);
		do{
			bdd.deref(Q);
			Q = bdd.ref(Qprime);
			int pre = game.controllablePredecessor(Qprime);
			Qprime = bdd.ref(bdd.and(Qprime, pre));
			bdd.deref(pre);	
		}while(Q != Qprime);
		bdd.deref(Qprime);
		return Q;
	}
	
	public ArrayList<Integer> leastFixedPoint(int set){
//		int Q = bdd.ref(bdd.getZero());
//		int QPrime = bdd.ref(set);
//		
//		do{
//			bdd.deref(Q);
//			Q = bdd.ref(QPrime);
//			
//			int pre = game.controllablePredecessor(QPrime);
//			
//			QPrime = bdd.orTo(QPrime, pre);
//			bdd.deref(pre);
//			
//		}while(Q != QPrime);
//		
//		bdd.deref(QPrime);
//		return Q;
		
		return leastFixedPoint(bdd, game, set);
	}
	
	public static ArrayList<Integer> leastFixedPoint(BDD bdd, GameStructure game, int set){
//		int Q = bdd.ref(bdd.getZero());
//		int QPrime = bdd.ref(set);
//		
//		do{
//			bdd.deref(Q);
//			Q = bdd.ref(QPrime);
//			
//			int pre = game.controllablePredecessor(QPrime);
//			
//			QPrime = bdd.orTo(QPrime, pre);
//			bdd.deref(pre);
//			
//		}while(Q != QPrime);
//		
//		bdd.deref(QPrime);
//		return Q;
		
		return leastFixedPointWithSafety(bdd, game, set, bdd.getOne());
	}
	
	/**
	 * the set of states that can reach (reachabilityObjective & safetyObjective) while 
	 * satisfying safetyObjective at all steps
	 * @param reachablityObjective
	 * @param safetyObjective
	 * @return
	 */
	public ArrayList<Integer> leastFixedPointWithSafety(int reachablityObjective, int safetyObjective){
//		int Q = bdd.ref(bdd.getZero());
//		int QPrime = bdd.ref(bdd.and(reachablityObjective, safetyObjective));
//		
//		do{
//			bdd.deref(Q);
//			Q = bdd.ref(QPrime);
//			
//			int pre = game.controllablePredecessor(QPrime);
//			pre = bdd.andTo(pre, safetyObjective);
//			
//			QPrime = bdd.orTo(QPrime, pre);
//			bdd.deref(pre);
//			
//		}while(Q != QPrime);
//		
//		bdd.deref(QPrime);
//		return Q;
		
		return leastFixedPointWithSafety(bdd, game, reachablityObjective, safetyObjective);
	}
	
	public static ArrayList<Integer> leastFixedPointWithSafety(BDD bdd, GameStructure game, 
			int reachabilityObjective, int safetyObjective){
		ArrayList<Integer> result = new ArrayList<Integer>();
		int Q = bdd.ref(bdd.getZero());
		int QPrime = bdd.ref(bdd.and(reachabilityObjective, safetyObjective));
		result.add(bdd.ref(QPrime));
		
		do{
			bdd.deref(Q);
			Q = bdd.ref(QPrime);
			
			int pre = game.controllablePredecessor(QPrime);
			pre = bdd.andTo(pre, safetyObjective);
			
//			int newStates = BDDWrapper.diff(bdd, pre, QPrime);
//			if(newStates != 0){
//				result.add(newStates);
//			}
			
			
			
			QPrime = bdd.orTo(QPrime, pre);
			bdd.deref(pre);
			
			if(Q != QPrime) result.add(bdd.ref(QPrime));
			
		}while(Q != QPrime);
		
		bdd.deref(QPrime);
		return result;
	}
	
	public static GameSolution solveForReachabilyObjectives(BDD bdd, GameStructure game, 
			int reachabilityObjective, int safetyObjective){
		
		GameSolution result;
		
		//compute the winning region
		ArrayList<Integer> reachabilityLayers = leastFixedPointWithSafety(bdd, game, reachabilityObjective, safetyObjective);
		
//		for(int i=0; i<reachabilityLayers.size(); i++){
//			UtilityMethods.debugBDDMethods(bdd, "reachability layer "+i, reachabilityLayers.get(i));
//		}
		
		
		//check if initial states of the game is a subset of the winning region
		if(!BDDWrapper.subset(bdd, game.getInit(), reachabilityLayers)){
			//unrealizable
			//TODO: compute a counter strategy
			result= new GameSolution(game, Player.ENVIRONMENT, null, -1, -1);
			return result;
		}
		
		//synthesize the strategy
		
		int controller  = synthesizeControllerForReachability(bdd, game, reachabilityLayers);
		result = new GameSolution(game, Player.SYSTEM, game.composeWithController(controller),reachabilityLayers.get(reachabilityLayers.size()-1), controller);
		BDDWrapper.free(bdd, reachabilityLayers);
		return result;
	}
	
	public static int synthesizeControllerForReachabilityAndSafetyObjective(BDD bdd, GameStructure game, 
			int reachabilityObjective, int safetyObjective){
		//compute the winning region
		ArrayList<Integer> reachabilityLayers = leastFixedPointWithSafety(bdd, game, reachabilityObjective, safetyObjective);
				
		//check if initial states of the game is a subset of the winning region
		if(!BDDWrapper.subset(bdd, game.getInit(), reachabilityLayers)){
			//unrealizable
			//TODO: compute a counter strategy
			return -1;
		}
				
		//synthesize the strategy
				
		int controller  = synthesizeControllerForReachability(bdd, game, reachabilityLayers);
		
		BDDWrapper.free(bdd, reachabilityLayers);
		return controller;
	}
	
	/**
	 * Given a sequence of winning states, where sets of states at i can reach the set of states at i-1 in one step, 
	 * synthesizes a controller that pushes the game from higher layers to lower one 
	 * @param bdd
	 * @param game
	 * @param reachabilitySets
	 * @return
	 */
	public static int synthesizeControllerForReachability(BDD bdd, GameStructure game, ArrayList<Integer> reachabilitySets){
		int controller = bdd.ref(bdd.getZero());
		for(int i=1; i<reachabilitySets.size(); i++){
			//from fromStates, push to the toStates in one step
			int fromStates = reachabilitySets.get(i);
			int toStates = reachabilitySets.get(i-1);
			int nonOverlappingStates = BDDWrapper.diff(bdd, fromStates, toStates);
			
			int systemStates = game.EpostImage(nonOverlappingStates, game.getVariablesAndActionsCube(), game.getEnvironmentTransitionRelation());
			
			
//			UtilityMethods.debugBDDMethods(bdd, "nonOverlapping states are",nonOverlappingStates);
//			UtilityMethods.debugBDDMethods(bdd, "reachable system states are", systemStates);
//			UtilityMethods.debugBDDMethods(bdd, "toStates are", toStates);
			
			int nonOverlappingStatesTransitions = BDDWrapper.and(bdd, game.getSystemTransitionRelation(), systemStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "new states trans", nonOverlappingStatesTransitions);
			
			int toStatesPrime = BDDWrapper.replace(bdd, toStates, game.getVtoVprime());
			int winningSysTransitions = bdd.ref(bdd.and(nonOverlappingStatesTransitions, toStatesPrime));
			int winningActAtStates = bdd.ref(bdd.exists(winningSysTransitions, game.getPrimeVariablesCube()));
			BDDWrapper.free(bdd, winningSysTransitions);
			int losingSysTransitions = bdd.ref(bdd.and(nonOverlappingStatesTransitions, bdd.not(toStatesPrime)));
			int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
			BDDWrapper.free(bdd, losingSysTransitions);
			BDDWrapper.free(bdd, nonOverlappingStatesTransitions);
			BDDWrapper.free(bdd, nonOverlappingStates);
			BDDWrapper.free(bdd, systemStates);
			int controller_i = BDDWrapper.diff(bdd, winningActAtStates, losingActsAtStates);
			BDDWrapper.free(bdd, winningActAtStates);
			BDDWrapper.free(bdd, losingActsAtStates);
			controller = BDDWrapper.orTo(bdd, controller, controller_i);
			BDDWrapper.free(bdd, controller_i);
		}
		return controller;
	}
	
	public static  GameStructure synthesize(BDD bdd, GameStructure game, int winningRegion){
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.getVtoVprime()));
//		int winningTrans=bdd.ref(bdd.and(winningRegion, winningRegionPrime));
//		bdd.deref(winningRegionPrime);
		
		int losingSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		

		int T_sys = bdd.ref(bdd.and(game.getSystemTransitionRelation(), winningRegionPrime));
		
		//remove nondeterministic acts that can lead to a losing state
		T_sys = BDDWrapper.diff(bdd, T_sys, losingActsAtStates);
		bdd.deref(losingActsAtStates);
		bdd.deref(winningRegionPrime);
		
		
		
		
		
		//T_env for the env states of the strategy
//		int T_env = bdd.ref(bdd.and(game.T_env, winningRegion));
		int T_env = game.getEnvironmentTransitionRelation();
		
		//TODO: correct actions and available actions
		//Game strategy = new Game(bdd, game.inputVariables,game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, game.getInit(), T_env, T_sys, game.actionVars, game.sysAvlActions, game.envAvlActions);
		GameStructure strategy = new GameStructure(bdd, game.variables, game.primedVariables, game.getInit(), T_env, T_sys, game.envActionVars, game.sysActionVars);
		strategy.setActionMap(game.getActionMap());
		strategy.setStateMap(game.getStateMap());
		return strategy;
	}
	
	//TODO: call the static method
	public GameStructure synthesize(int winningRegion){
		
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.getVtoVprime()));
//		int winningTrans=bdd.ref(bdd.and(winningRegion, winningRegionPrime));
//		bdd.deref(winningRegionPrime);
		
		int losingSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		

		int T_sys = bdd.ref(bdd.and(game.getSystemTransitionRelation(), winningRegionPrime));
		
		//remove nondeterministic acts that can lead to a losing state
		T_sys = BDDWrapper.diff(bdd, T_sys, losingActsAtStates);
		bdd.deref(losingActsAtStates);
		bdd.deref(winningRegionPrime);
		
		
		
		
		
		//T_env for the env states of the strategy
//		int T_env = bdd.ref(bdd.and(game.T_env, winningRegion));
		int T_env = game.getEnvironmentTransitionRelation();
		
		//TODO: correct actions and available actions
		//Game strategy = new Game(bdd, game.inputVariables,game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, game.getInit(), T_env, T_sys, game.actionVars, game.sysAvlActions, game.envAvlActions);
		GameStructure strategy = new GameStructure(bdd, game.variables, game.primedVariables, game.getInit(), T_env, T_sys, game.envActionVars, game.sysActionVars);
		strategy.setActionMap(game.getActionMap());
		strategy.setStateMap(game.getStateMap());
		return strategy;
	}
	
	public static int synthesizeController(BDD bdd, GameStructure game, int winningRegion){
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.getVtoVprime()));
		//Second version of computing a controller, potentially simpler, we existentially quantify
		//primed variables to have controllers of the form "state & actions", that is, f: V_2 -> 2^actions
		//from here
		int winningSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), winningRegionPrime));
		int winningActAtStates = bdd.ref(bdd.exists(winningSysTransitions, game.getPrimeVariablesCube()));
		BDDWrapper.free(bdd, winningSysTransitions);
		int losingSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		BDDWrapper.free(bdd, losingSysTransitions);
		int controller = BDDWrapper.diff(bdd, winningActAtStates, losingActsAtStates);
		BDDWrapper.free(bdd, winningActAtStates);
		BDDWrapper.free(bdd, losingActsAtStates);
				
//				for(int act : game.getSysActions()){
//					//winning states where act have an existing transition to a winning state
//					int actWinningRegion = bdd.ref(bdd.and(act, winningSysTransitions));
//					//winning states where act has an existing transition to a losing state 
//					int actLosingRegion = bdd.ref(bdd.and(act, losingSysTransitions));
//					
//				}
		//to here
		return controller;
	}
	
	public int synthesizeController(){
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.getVtoVprime()));
		//Second version of computing a controller, potentially simpler, we existentially quantify
		//primed variables to have controllers of the form "state & actions", that is, f: V_2 -> 2^actions
		//from here
		int winningSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), winningRegionPrime));
		int winningActAtStates = bdd.ref(bdd.exists(winningSysTransitions, game.getPrimeVariablesCube()));
		BDDWrapper.free(bdd, winningSysTransitions);
		int losingSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		BDDWrapper.free(bdd, losingSysTransitions);
		int controller = BDDWrapper.diff(bdd, winningActAtStates, losingActsAtStates);
		BDDWrapper.free(bdd, winningActAtStates);
		BDDWrapper.free(bdd, losingActsAtStates);
				
//				for(int act : game.getSysActions()){
//					//winning states where act have an existing transition to a winning state
//					int actWinningRegion = bdd.ref(bdd.and(act, winningSysTransitions));
//					//winning states where act has an existing transition to a losing state 
//					int actLosingRegion = bdd.ref(bdd.and(act, losingSysTransitions));
//					
//				}
		//to here
		return controller;
	}
	
	public static GameStructure counterStrategy(BDD bdd, GameStructure game, int objective){
		
		
		
//		game.printGame();
//		
//		UtilityMethods.getUserInput();
//		
//		int complementObjectiveStates=bdd.ref(bdd.not(objective));
//		
//		System.out.println("computing cs");
//		System.out.println("\ncomplement objective is ");
//		bdd.printSet(complementObjectiveStates);
		
//		int Q=complementObjectiveStates;
		
		int Q=bdd.ref(bdd.not(objective));
		
		
		//int losingEnvStates = bdd.ref(bdd.not(objective)); //losingEnvStates = Q in the beginnig
//		int csInit=bdd.ref(bdd.and(game.init, Q));
		int csInit= bdd.ref(game.getInit());
		
		int csT_sys=bdd.ref(bdd.getZero());
		int csT_env=bdd.ref(bdd.getZero());
		
		int cube=game.getPrimedVariablesAndActionsCube();
		
		int losingSystemStates=getDeadLockStates(bdd, game, game.getSystemTransitionRelation());
		
//		System.out.println("in cs, starting, initial values");
//		
//		UtilityMethods.debugBDDMethods(bdd, "deadlock states are",  losingSystemStates);
//		
//		UtilityMethods.debugBDDMethods(bdd, "cs_init is ",  csInit);
//		UtilityMethods.debugBDDMethods(bdd, "Q is ", Q);
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//		UtilityMethods.getUserInput();
		
//		int[] actions = game.actions;
//		
//		int[] envActions= game.getEnvActions();
		
		int[] sysActions= game.getSysActions();
		
		//while init is not part of the discovered losing states
		//while(csInit == 0){
		int Q_old=bdd.ref(Q);
		do{
			
//			UtilityMethods.debugBDDMethods(bdd, "cs computation, Q is ", Q);
//			UtilityMethods.debugBDDMethods(bdd, "cs computation, losing sys states Are ", losingSystemStates);
			
			
			//compute APre of losing region as the set of system states that cannot avoid losing positions
			int badSystemStates = bdd.ref(bdd.getOne());
//			for(int act : actions){
			int[] sysAvlActs = game.getSystemActionAvailabilityList();
			for(int i=0;i<sysAvlActs.length;i++){
				int act = sysActions[i];
//				int actAvlAt = game.actionAvailabilitySet(act, game.getSystemTransitionRelation());
				int actAvlAt = sysAvlActs[i];
				int notAvl = bdd.ref(bdd.not(actAvlAt));
				//states where there is one transition labeled with act that pushes to Q
				int preQ = game.EpreImage(Q, act, cube, game.getSystemTransitionRelation());
				preQ=bdd.orTo(preQ, notAvl);
				bdd.deref(notAvl);
//				bdd.deref(actAvlAt);
				badSystemStates = bdd.andTo(badSystemStates, preQ);
				bdd.deref(preQ);
				
			}
			int newLosingSystemStates = BDDWrapper.diff(bdd, badSystemStates, losingSystemStates);
			losingSystemStates = bdd.orTo(losingSystemStates,badSystemStates);
			bdd.deref(badSystemStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "losing system states are", losingSystemStates);
			
			int QPrime = bdd.ref(bdd.replace(Q, game.vTovPrime));
			int sysTrans = bdd.ref(bdd.and(newLosingSystemStates, QPrime));
			bdd.deref(newLosingSystemStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "system transition added ", sysTrans);
			
			csT_sys=bdd.orTo(csT_sys, sysTrans);
			bdd.deref(QPrime);
			bdd.deref(sysTrans);
			
			//environment transitions are those that can reach a losing system state
//			losingEnvStates=bdd.orTo(losingEnvStates, Q);
//			bdd.deref(Q);
			
//			UtilityMethods.debugBDDMethods(bdd, "losing system states are", losingSystemStates);
//			UtilityMethods.debugBDDMethods(bdd, "cube is", cube);
//			UtilityMethods.debugBDDMethods(bdd, "env transition relation is", game.getEnvironmentTransitionRelation());;
//			UtilityMethods.getUserInput();
			
			int badEnvStates=game.EpreImage(losingSystemStates, cube, game.getEnvironmentTransitionRelation());
			int losingSystemStatesPrime=bdd.ref(bdd.replace(losingSystemStates, game.vTovPrime));
			int newEnvStates=BDDWrapper.diff(bdd, badEnvStates, Q);
			
//			int Q_old=bdd.ref(bdd.and(Q, bdd.getOne()));
			
//			if(Q_old!=-1){
//				bdd.deref(Q_old);
//			}
			
			bdd.deref(Q_old);
			Q_old=bdd.ref(Q);
			
//			Q_old=Q;
			
//			Q=bdd.ref(bdd.or(Q, badEnvStates));
			Q=bdd.orTo(Q, badEnvStates);
			bdd.deref(badEnvStates);
			int envTrans = bdd.ref(bdd.and(newEnvStates, losingSystemStatesPrime));
			
			
			
//			UtilityMethods.debugBDDMethods(bdd, "envTrans added", envTrans);
			
			csT_env = bdd.orTo(csT_env, envTrans);
			bdd.deref(losingSystemStatesPrime);
			bdd.deref(envTrans);
			bdd.deref(newEnvStates);
			
//			if(csInit != game.getInit()) bdd.deref(csInit);
			
			bdd.deref(csInit);
			csInit=bdd.ref(bdd.and(game.getInit(), Q));
			
			if(Q_old == Q){
				System.err.println("something is wrong in counter-strategy");
				game.printGameVars();
				UtilityMethods.debugBDDMethods(bdd, "cs computation, Q is ", Q);
				UtilityMethods.debugBDDMethods(bdd, "cs computation, losing sys states Are ", losingSystemStates);
				//UtilityMethods.debugBDDMethods(bdd, "Q_old is ", Q_old);
				UtilityMethods.debugBDDMethods(bdd, "init is ", game.getInit());
				UtilityMethods.debugBDDMethods(bdd, "csInit is ", csInit);
				UtilityMethods.getUserInput();
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "Q is ", Q);
//			UtilityMethods.debugBDDMethods(bdd, "csInit is ", csInit);
//			UtilityMethods.getUserInput();
			
		}while(csInit==0);
		
		bdd.deref(Q);
		bdd.deref(Q_old);
//		System.out.println("CS computation");
//		UtilityMethods.debugBDDMethods(bdd, "cs env trans", csT_env);
		
		//conjoining computed transitions 
		csT_sys=bdd.andTo(csT_sys, game.getSystemTransitionRelation());
//		csT_sys=game.getSystemTransitionRelation();
		csT_env=bdd.andTo(csT_env, game.getEnvironmentTransitionRelation());
		
		
//		UtilityMethods.debugBDDMethods(bdd, "game env trans", game.T_env);
//		UtilityMethods.debugBDDMethods(bdd, "game sys trans", game.T_sys);
//		
//		UtilityMethods.debugBDDMethods(bdd, "cs env trans", csT_env);
//		UtilityMethods.debugBDDMethods(bdd, "cs sys trans", csT_sys);
//		
//		UtilityMethods.getUserInput();
		
//		bdd.deref(cube);
		
//		UtilityMethods.debugBDDMethods(bdd, "computing counter-strategy, csInit is", csInit);
		
		//Game counterStrategy =  new Game(bdd, game.inputVariables, game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, csInit, csT_env, csT_sys, game.actionVars, game.getSystemActionAvailabilityList(), game.getEnvironmentActionAvailabilityList());
		GameStructure counterStrategy =  new GameStructure(bdd, game.variables, game.primedVariables, csInit, csT_env, csT_sys, game.envActionVars, game.sysActionVars);
		counterStrategy.setActionMap(game.getActionMap());
		counterStrategy.setStateMap(game.getStateMap());
		return counterStrategy;
	}
	
	public GameStructure counterStrategy(){
		
		
		
//		game.printGame();
//		
//		UtilityMethods.getUserInput();
//		
//		int complementObjectiveStates=bdd.ref(bdd.not(objective));
//		
//		System.out.println("computing cs");
//		System.out.println("\ncomplement objective is ");
//		bdd.printSet(complementObjectiveStates);
		
//		int Q=complementObjectiveStates;
		
		int Q=bdd.ref(bdd.not(objective));
		
		
		//int losingEnvStates = bdd.ref(bdd.not(objective)); //losingEnvStates = Q in the beginnig
//		int csInit=bdd.ref(bdd.and(game.init, Q));
		int csInit= bdd.ref(game.getInit());
		
		int csT_sys=bdd.ref(bdd.getZero());
		int csT_env=bdd.ref(bdd.getZero());
		
		int cube=game.getPrimedVariablesAndActionsCube();
		
		int losingSystemStates=getDeadLockStates(game.getSystemTransitionRelation());
		
//		System.out.println("in cs, starting, initial values");
//		
//		UtilityMethods.debugBDDMethods(bdd, "deadlock states are",  losingSystemStates);
//		
//		UtilityMethods.debugBDDMethods(bdd, "cs_init is ",  csInit);
//		UtilityMethods.debugBDDMethods(bdd, "Q is ", Q);
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//		UtilityMethods.getUserInput();
		
//		int[] actions = game.actions;
//		
//		int[] envActions= game.getEnvActions();
		
		int[] sysActions= game.getSysActions();
		
		//while init is not part of the discovered losing states
		//while(csInit == 0){
		int Q_old=bdd.ref(Q);
		do{
			
//			UtilityMethods.debugBDDMethods(bdd, "cs computation, Q is ", Q);
//			UtilityMethods.debugBDDMethods(bdd, "cs computation, losing sys states Are ", losingSystemStates);
			
			
			//compute APre of losing region as the set of system states that cannot avoid losing positions
			int badSystemStates = bdd.ref(bdd.getOne());
//			for(int act : actions){
			int[] sysAvlActs = game.getSystemActionAvailabilityList();
			for(int i=0;i<sysAvlActs.length;i++){
				int act = sysActions[i];
//				int actAvlAt = game.actionAvailabilitySet(act, game.getSystemTransitionRelation());
				int actAvlAt = sysAvlActs[i];
				int notAvl = bdd.ref(bdd.not(actAvlAt));
				//states where there is one transition labeled with act that pushes to Q
				int preQ = game.EpreImage(Q, act, cube, game.getSystemTransitionRelation());
				preQ=bdd.orTo(preQ, notAvl);
				bdd.deref(notAvl);
//				bdd.deref(actAvlAt);
				badSystemStates = bdd.andTo(badSystemStates, preQ);
				bdd.deref(preQ);
				
			}
			int newLosingSystemStates = BDDWrapper.diff(bdd, badSystemStates, losingSystemStates);
			losingSystemStates = bdd.orTo(losingSystemStates,badSystemStates);
			bdd.deref(badSystemStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "losing system states are", losingSystemStates);
			
			int QPrime = bdd.ref(bdd.replace(Q, game.vTovPrime));
			int sysTrans = bdd.ref(bdd.and(newLosingSystemStates, QPrime));
			bdd.deref(newLosingSystemStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "system transition added ", sysTrans);
			
			csT_sys=bdd.orTo(csT_sys, sysTrans);
			bdd.deref(QPrime);
			bdd.deref(sysTrans);
			
			//environment transitions are those that can reach a losing system state
//			losingEnvStates=bdd.orTo(losingEnvStates, Q);
//			bdd.deref(Q);
			
//			UtilityMethods.debugBDDMethods(bdd, "losing system states are", losingSystemStates);
//			UtilityMethods.debugBDDMethods(bdd, "cube is", cube);
//			UtilityMethods.debugBDDMethods(bdd, "env transition relation is", game.getEnvironmentTransitionRelation());;
//			UtilityMethods.getUserInput();
			
			int badEnvStates=game.EpreImage(losingSystemStates, cube, game.getEnvironmentTransitionRelation());
			int losingSystemStatesPrime=bdd.ref(bdd.replace(losingSystemStates, game.vTovPrime));
			int newEnvStates=BDDWrapper.diff(bdd, badEnvStates, Q);
			
//			int Q_old=bdd.ref(bdd.and(Q, bdd.getOne()));
			
//			if(Q_old!=-1){
//				bdd.deref(Q_old);
//			}
			
			bdd.deref(Q_old);
			Q_old=bdd.ref(Q);
			
//			Q_old=Q;
			
//			Q=bdd.ref(bdd.or(Q, badEnvStates));
			Q=bdd.orTo(Q, badEnvStates);
			bdd.deref(badEnvStates);
			int envTrans = bdd.ref(bdd.and(newEnvStates, losingSystemStatesPrime));
			
			
			
//			UtilityMethods.debugBDDMethods(bdd, "envTrans added", envTrans);
			
			csT_env = bdd.orTo(csT_env, envTrans);
			bdd.deref(losingSystemStatesPrime);
			bdd.deref(envTrans);
			bdd.deref(newEnvStates);
			
//			if(csInit != game.getInit()) bdd.deref(csInit);
			
			bdd.deref(csInit);
			csInit=bdd.ref(bdd.and(game.getInit(), Q));
			
			if(Q_old == Q){
				System.err.println("something is wrong in counter-strategy");
				game.printGameVars();
				UtilityMethods.debugBDDMethods(bdd, "cs computation, Q is ", Q);
				UtilityMethods.debugBDDMethods(bdd, "cs computation, losing sys states Are ", losingSystemStates);
				//UtilityMethods.debugBDDMethods(bdd, "Q_old is ", Q_old);
				UtilityMethods.debugBDDMethods(bdd, "init is ", game.getInit());
				UtilityMethods.debugBDDMethods(bdd, "csInit is ", csInit);
				UtilityMethods.getUserInput();
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "Q is ", Q);
//			UtilityMethods.debugBDDMethods(bdd, "csInit is ", csInit);
//			UtilityMethods.getUserInput();
			
		}while(csInit==0);
		
		bdd.deref(Q);
		bdd.deref(Q_old);
//		System.out.println("CS computation");
//		UtilityMethods.debugBDDMethods(bdd, "cs env trans", csT_env);
		
		//conjoining computed transitions 
		csT_sys=bdd.andTo(csT_sys, game.getSystemTransitionRelation());
//		csT_sys=game.getSystemTransitionRelation();
		csT_env=bdd.andTo(csT_env, game.getEnvironmentTransitionRelation());
		
		
//		UtilityMethods.debugBDDMethods(bdd, "game env trans", game.T_env);
//		UtilityMethods.debugBDDMethods(bdd, "game sys trans", game.T_sys);
//		
//		UtilityMethods.debugBDDMethods(bdd, "cs env trans", csT_env);
//		UtilityMethods.debugBDDMethods(bdd, "cs sys trans", csT_sys);
//		
//		UtilityMethods.getUserInput();
		
//		bdd.deref(cube);
		
//		UtilityMethods.debugBDDMethods(bdd, "computing counter-strategy, csInit is", csInit);
		
		//Game counterStrategy =  new Game(bdd, game.inputVariables, game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, csInit, csT_env, csT_sys, game.actionVars, game.getSystemActionAvailabilityList(), game.getEnvironmentActionAvailabilityList());
		GameStructure counterStrategy =  new GameStructure(bdd, game.variables, game.primedVariables, csInit, csT_env, csT_sys, game.envActionVars, game.sysActionVars);
		counterStrategy.setActionMap(game.getActionMap());
		counterStrategy.setStateMap(game.getStateMap());
		return counterStrategy;
		
	}
	

	public static int getDeadLockStates(BDD bdd, GameStructure game, int transitionRelation){
//		int cube = bdd.ref(bdd.and(game.getActionsCube(), game.getPrimeVariablesCube()));
		int cube = game.getPrimedVariablesAndActionsCube();
		int statesWithOutgoingTransitions=bdd.ref(bdd.exists(transitionRelation, cube));
//		bdd.deref(cube);
		int deadlockStates=bdd.ref(bdd.not(statesWithOutgoingTransitions));
		bdd.deref(statesWithOutgoingTransitions);
		return deadlockStates;
	}
	
	public int getDeadLockStates(int transitionRelation){
//		int cube = bdd.ref(bdd.and(game.getActionsCube(), game.getPrimeVariablesCube()));
		int cube = game.getPrimedVariablesAndActionsCube();
		int statesWithOutgoingTransitions=bdd.ref(bdd.exists(transitionRelation, cube));
//		bdd.deref(cube);
		int deadlockStates=bdd.ref(bdd.not(statesWithOutgoingTransitions));
		bdd.deref(statesWithOutgoingTransitions);
		return deadlockStates;
	}
	
	
	//sloving for reachability and safety objectives
	/**
	 * Given a game structure and a conjunction of safety and reachability objectives, synthesizes a strategy
	 * that satisfies the objectives
	 * @return
	 */
	public static GameSolution solveSafetyReachabilityObjectives(BDD bdd, GameStructure gameStructure, ArrayList<Objective> safetyObjectives, ArrayList<Objective> reachabilityObjectives){
		
		
		//prepare reachability objectives
		
		//introduce a boolean flag for each reachability objective
		Variable[] reachabilityFlags = Variable.createVariablesAndTheirPrimedCopy(bdd, reachabilityObjectives.size(), "reachabilityFlags");
		
		//init values for the reachability flags
		int initReachabilityFlags = bdd.ref(bdd.getOne());
		for(int i = 0; i<reachabilityObjectives.size(); i++){
			int initReachFlag_i = bdd.ref(bdd.biimp(reachabilityFlags[i].getBDDVar(), reachabilityObjectives.get(i).getObjectiveFormula()));
			initReachabilityFlags = bdd.andTo(initReachabilityFlags, initReachFlag_i);
			bdd.deref(initReachFlag_i);
		}
		
		//flag transitions
		int sameFlags = BDDWrapper.same(bdd, reachabilityFlags, Variable.getPrimedCopy(reachabilityFlags));
		
		int reachabilityTransitions = bdd.ref(bdd.getOne());
		for(int i = 0; i<reachabilityObjectives.size(); i++){
			//(s' -> f') & (f -> f') & (not f & not s' -> neg f')
			int targetState = reachabilityObjectives.get(i).getObjectiveFormula();
			int targetStatePrime = BDDWrapper.replace(bdd, targetState, gameStructure.vTovPrime);
			Variable flag = reachabilityFlags[i];
			Variable flagPrime = reachabilityFlags[i].getPrimedCopy();
			
			int sImpF = bdd.ref(bdd.imp(targetState, flag.getBDDVar()));
			int sPImpFP = bdd.ref(bdd.imp(targetStatePrime, flagPrime.getBDDVar()));
			int fTrans=bdd.ref(bdd.imp(flag.getBDDVar(), flagPrime.getBDDVar()));
			int ftmp=bdd.ref(bdd.and(bdd.not(flag.getBDDVar()), bdd.not(targetStatePrime)));
			int ftrans2=bdd.ref(bdd.imp(ftmp, bdd.not(flagPrime.getBDDVar())));
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, sImpF);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, sPImpFP);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, fTrans);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, ftrans2);
			bdd.deref(sImpF);
			bdd.deref(fTrans);
			bdd.deref(ftmp);
			bdd.deref(ftrans2);
		}
		
		//alter the game structure according to the given objectives. The new game structure must take 
		//flags into account
		
		Variable[] gameVars = Variable.unionVariables(gameStructure.variables, reachabilityFlags);
		Variable[] primedGameVars = Variable.getPrimedCopy(gameVars);
		int initAlteredGame = bdd.ref(bdd.and(gameStructure.getInit(), initReachabilityFlags));
		int T_env = bdd.ref(bdd.and(gameStructure.getEnvironmentTransitionRelation(), sameFlags));
		bdd.deref(sameFlags);
		int T_sys = bdd.ref(bdd.and(gameStructure.getSystemTransitionRelation(), reachabilityTransitions));
		bdd.deref(reachabilityTransitions);
		
		GameStructure alteredGameStructure = new GameStructure(bdd, gameVars, primedGameVars, initAlteredGame, T_env, T_sys ,gameStructure.envActionVars, gameStructure.sysActionVars);
		
		//define objectives 
		
		//prepare safety objectives
		int safetyObj = bdd.ref(bdd.getOne());
		for(Objective obj : safetyObjectives){
			safetyObj = bdd.andTo(safetyObj, obj.getObjectiveFormula());
		}
		
		//eventually all flags should become one
		int eventuallyObjective = bdd.ref(bdd.getOne());
		for(int i=0;i<reachabilityFlags.length;i++){
			eventuallyObjective=bdd.andTo(eventuallyObjective, reachabilityFlags[i].getBDDVar());
		}
		
		GameSolution reachabilitySolution = solveForReachabilyObjectives(bdd, alteredGameStructure, eventuallyObjective, safetyObj);
		
//		int reachabilityController = synthesizeControllerForReachabilityAndSafetyObjective(bdd, alteredGameStructure, eventuallyObjective, safetyObj);
		
		if(reachabilitySolution.getWinner()==Player.ENVIRONMENT){
			return reachabilitySolution;
		}
		
		GameSolution safetySolution = solve(bdd, gameStructure, safetyObj);
		
		if(safetySolution.getWinner()==Player.ENVIRONMENT){
			return safetySolution;
		}
		
		int safetyController = bdd.ref(bdd.and(safetySolution.getController(), eventuallyObjective));
		int controller = BDDWrapper.or(bdd,reachabilitySolution.getController(), safetyController);
		
		int safetyWinning = BDDWrapper.and(bdd, safetySolution.getWinningSystemStates(), eventuallyObjective);
		int winningStates = BDDWrapper.or(bdd, safetyWinning, reachabilitySolution.getWinningSystemStates());
		
		
		GameSolution solution = new GameSolution(gameStructure, Player.SYSTEM, alteredGameStructure.composeWithController(controller), winningStates, controller);
		return solution;

	}
	
	
	
	public static GameSolution solveSafetyReachabilityObjectives(BDD bdd, GameStructure gameStructure, int safetyObjective, int reachabilityObjective){
		Objective safetyObjective1 = new Objective(ObjectiveType.Safety, safetyObjective);
		ArrayList<Objective> safetyObjectives = new ArrayList<Objective>();
		safetyObjectives.add(safetyObjective1);
		Objective reachabilityObjective1 = new Objective(ObjectiveType.Reachability, reachabilityObjective);
		ArrayList<Objective> reachabilityObjectives = new ArrayList<Objective>();
		reachabilityObjectives.add(reachabilityObjective1);
		
		return solveSafetyReachabilityObjectives(bdd, gameStructure, safetyObjectives, reachabilityObjectives);
		
	}
	
	//Parametric controllers
	
	/**
	 * 
	 * @param bdd
	 * @param concreteGameStructure
	 * @param parameters
	 * @param controllerInterface
	 * @return
	 */
	
	public static ParametricCoSafeController synthesizeParamericCoSafeController(BDD bdd, 
			GameStructure concreteGameStructure, Variable[] parameters, CoSafeControllerInterface controllerInterface){
		ParametricGameStructure parametricGameStructure = concreteGameStructure.generateParametricGameStructure(parameters);
		GameSolution solution = GameSolver.solveForReachabilyObjectives(bdd, parametricGameStructure, controllerInterface);
		
		solution.print();
		
		// compute the interface for the controller
		// obtain the set of reachable states when restricting to the controller
		GameStructure strat = solution.strategyOfTheWinner();
		int reachableStates = strat.reachable();
		int finalStates = BDDWrapper.and(bdd, reachableStates, controllerInterface.getReachability());
		
		CoSafeControllerInterface refinedControllerInterface = new CoSafeControllerInterface(bdd,controllerInterface.getInit(), 
				reachableStates, finalStates);
		
		
		Controller controller = new Controller(bdd, parametricGameStructure.variables, 
				parametricGameStructure.actionVars, 
				parametricGameStructure.getInit(), solution.getController(), 
//				BDDWrapper.and(bdd, controllerInterface.getSafety(), controllerInterface.getReachability()));
				finalStates);
//		ParametricCoSafeController c = new ParametricCoSafeController(controller, parametricGameStructure, controllerInterface);
		ParametricCoSafeController c = new ParametricCoSafeController(controller, parametricGameStructure, refinedControllerInterface);
		return c;
	}
	
	public static GameSolution solveForReachabilyObjectives(BDD bdd, ParametricGameStructure parametricGameStructure,
			CoSafeControllerInterface controlInterface){
		
		GameSolution result;
		
		//compute the winning region
		ArrayList<Integer> reachabilityLayers = leastFixedPointWithSafety(bdd, parametricGameStructure, 
				controlInterface.getReachability(), controlInterface.getSafety());
		
//		for(int i=0; i<reachabilityLayers.size(); i++){
//			UtilityMethods.debugBDDMethods(bdd, "reachability layer "+i, reachabilityLayers.get(i));
////			UtilityMethods.getUserInput();
//		}
		
//		UtilityMethods.debugBDDMethods(bdd, "reachability layer ", reachabilityLayers.get(reachabilityLayers.size()-1));
		
				
		int winningState = reachabilityLayers.get(reachabilityLayers.size()-1);
		
//		UtilityMethods.debugBDDMethods(bdd, "parametric init is", controlInterface.getInit());
//		UtilityMethods.debugBDDMethods(bdd, "computing parametric controller, winning states are", winningState);
		
//		int newInit = BDDWrapper.and(bdd, controlInterface.getInit(), winningState);
//		UtilityMethods.debugBDDMethods(bdd, "new init is", newInit);
		
		
		controlInterface.updateInit(bdd, winningState);
		parametricGameStructure.setInit(controlInterface.getInit());
		
//		UtilityMethods.debugBDDMethods(bdd, "parametric init got updated", controlInterface.getInit());
//		UtilityMethods.getUserInput();
		
		//check if initial states of the game is a subset of the winning region
		if(controlInterface.getInit() == 0){
			//unrealizable
			//TODO: compute a counter strategy
			System.err.println("parametric objectives are un-enforceable!");
			result= new GameSolution(parametricGameStructure, Player.ENVIRONMENT, null, -1, -1);
			return result;
		}
		
		//synthesize the strategy
		
		int controller  = synthesizeControllerForReachability(bdd, parametricGameStructure, reachabilityLayers);
		result = new GameSolution(parametricGameStructure, Player.SYSTEM, parametricGameStructure.composeWithController(controller),reachabilityLayers.get(reachabilityLayers.size()-1), controller);
		BDDWrapper.free(bdd, reachabilityLayers);
		return result;
	}
	
	
	//controllable single player game structure
	/**
	 * Given a game structure and a conjunction of safety and reachability objectives, synthesizes a strategy
	 * that satisfies the objectives
	 * @return
	 */
	public static GameSolution solveSafetyReachabilityObjectives(BDD bdd, ControllableSinglePlayerDeterministicGameStructure gameStructure, int init, ArrayList<Objective> safetyObjectives, ArrayList<Objective> reachabilityObjectives){
		
		
		//prepare reachability objectives
		
		//introduce a boolean flag for each reachability objective
		Variable[] reachabilityFlags = Variable.createVariablesAndTheirPrimedCopy(bdd, reachabilityObjectives.size(), "reachabilityFlags");
		
		//init values for the reachability flags
		int initReachabilityFlags = bdd.ref(bdd.getOne());
		for(int i = 0; i<reachabilityObjectives.size(); i++){
			int initReachFlag_i = bdd.ref(bdd.biimp(reachabilityFlags[i].getBDDVar(), reachabilityObjectives.get(i).getObjectiveFormula()));
			initReachabilityFlags = bdd.andTo(initReachabilityFlags, initReachFlag_i);
			bdd.deref(initReachFlag_i);
		}
		
		//flag transitions
		
		int reachabilityTransitions = bdd.ref(bdd.getOne());
		for(int i = 0; i<reachabilityObjectives.size(); i++){
			//(s' -> f') & (f -> f') & (not f & not s' -> neg f')
			int targetState = reachabilityObjectives.get(i).getObjectiveFormula();
			int targetStatePrime = BDDWrapper.replace(bdd, targetState, gameStructure.vTovPrime);
			Variable flag = reachabilityFlags[i];
			Variable flagPrime = reachabilityFlags[i].getPrimedCopy();
			
			int sImpF = bdd.ref(bdd.imp(targetState, flag.getBDDVar()));
			int sPImpFP = bdd.ref(bdd.imp(targetStatePrime, flagPrime.getBDDVar()));
			int fTrans=bdd.ref(bdd.imp(flag.getBDDVar(), flagPrime.getBDDVar()));
			int ftmp=bdd.ref(bdd.and(bdd.not(flag.getBDDVar()), bdd.not(targetStatePrime)));
			int ftrans2=bdd.ref(bdd.imp(ftmp, bdd.not(flagPrime.getBDDVar())));
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, sImpF);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, sPImpFP);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, fTrans);
			reachabilityTransitions=bdd.andTo(reachabilityTransitions, ftrans2);
			bdd.deref(sImpF);
			bdd.deref(fTrans);
			bdd.deref(ftmp);
			bdd.deref(ftrans2);
		}
		
		//alter the game structure according to the given objectives. The new game structure must take 
		//flags into account
		
		Variable[] gameVars = Variable.unionVariables(gameStructure.variables, reachabilityFlags);
		int T_sys = bdd.ref(bdd.and(gameStructure.getSystemTransitionRelation(), reachabilityTransitions));
		bdd.deref(reachabilityTransitions);
		
		int initAlteredGame = bdd.ref(bdd.and(gameStructure.getInit(), initReachabilityFlags));
		
		ControllableSinglePlayerDeterministicGameStructure alteredGameStructure = new ControllableSinglePlayerDeterministicGameStructure(bdd, gameVars, T_sys, gameStructure.sysActionVars);
		
		alteredGameStructure.setInit(initAlteredGame);
		//define objectives 
		
		//prepare safety objectives
		int safetyObj = bdd.ref(bdd.getOne());
		for(Objective obj : safetyObjectives){
			safetyObj = bdd.andTo(safetyObj, obj.getObjectiveFormula());
		}
		
		//eventually all flags should become one
		int eventuallyObjective = bdd.ref(bdd.getOne());
		for(int i=0;i<reachabilityFlags.length;i++){
			eventuallyObjective=bdd.andTo(eventuallyObjective, reachabilityFlags[i].getBDDVar());
		}
		
		GameSolution reachabilitySolution = solveForReachabilyObjectives(bdd, alteredGameStructure, init, eventuallyObjective, safetyObj);
		
		return reachabilitySolution;

	}
	
	public static GameSolution solveForReachabilyObjectives(BDD bdd, 
			ControllableSinglePlayerDeterministicGameStructure game, 
			int init, 
			int reachabilityObjective, int safetyObjective){
		
		GameSolution result;
		
		//compute the winning region
		ArrayList<Integer> reachabilityLayers = leastFixedPointWithSafety(bdd, game, reachabilityObjective, safetyObjective);
		
//		for(int i=0; i<reachabilityLayers.size(); i++){
//			UtilityMethods.debugBDDMethods(bdd, "reachability layer "+i, reachabilityLayers.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		
		//check if initial states of the game is a subset of the winning region
		if(!BDDWrapper.subset(bdd, init, reachabilityLayers)){
			//unrealizable
			//TODO: compute a counter strategy
			result= new GameSolution(game, Player.ENVIRONMENT, null, -1, -1);
			return result;
		}
		
		//synthesize the strategy
		
		int controller  = synthesizeControllerForReachability(bdd, game, reachabilityLayers);
		result = new GameSolution(game, Player.SYSTEM, game.composeWithController(controller),reachabilityLayers.get(reachabilityLayers.size()-1), controller);
		BDDWrapper.free(bdd, reachabilityLayers);
		return result;
	}
	
	/**
	 * Given a sequence of winning states, where sets of states at i can reach the set of states at i-1 in one step, 
	 * synthesizes a controller that pushes the game from higher layers to lower one 
	 * @param bdd
	 * @param game
	 * @param reachabilitySets
	 * @return
	 */
	public static int synthesizeControllerForReachability(BDD bdd, ControllableSinglePlayerDeterministicGameStructure game, ArrayList<Integer> reachabilitySets){
		int controller = bdd.ref(bdd.getZero());
		for(int i=1; i<reachabilitySets.size(); i++){
			//from fromStates, push to the toStates in one step
			int fromStates = reachabilitySets.get(i);
			int toStates = reachabilitySets.get(i-1);
			int nonOverlappingStates = BDDWrapper.diff(bdd, fromStates, toStates);
			
			int toStatesPrime = BDDWrapper.replace(bdd, toStates, game.getVtoVprime());
			
			int progressTrans = BDDWrapper.and(bdd, nonOverlappingStates, toStatesPrime);
			progressTrans = BDDWrapper.andTo(bdd, progressTrans, game.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "controller trans from layer "+i+" to layer "+(i-1), progressTrans);
			
			controller = BDDWrapper.orTo(bdd, controller, progressTrans);
			
			BDDWrapper.free(bdd, nonOverlappingStates);
			BDDWrapper.free(bdd, toStatesPrime);
			BDDWrapper.free(bdd, progressTrans);
		}
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "synthesized controller is ", controller);
		
		return controller;
	}
	
	
}
