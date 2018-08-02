package csguided;



import utils.UtilityMethods;
import automaton.GameGraph;
import jdd.bdd.BDD;


//TODO: counter-strategy forward computation
//TODO: the algorithm for computing counter-strategy is very inefficient, revise 
//TODO: removing the unreachable parts of strategy and counter-strategy
//TODO: check the whole package for the correct use of "ref" and "deref"
public class GameSolver {
	Game game;
	int objective;
	BDD bdd;
//	Game strategyOfTheWinner;
	
	boolean solved;
	char winner;
	int winningRegion;
	
	
	public GameSolver(Game g, int obj, BDD argbdd){
		game=g;
		objective=obj;
		bdd=argbdd;
		
		solved=false;
		winner='u'; //unknown
		winningRegion=-1;
	}
	
	
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000,1000);
		
		Game simpleGame=TestCaseGenerator.simpleGameTest2(bdd);
		int compObj=bdd.ref(bdd.and(simpleGame.inputVariables[0].getBDDVar(), bdd.not(simpleGame.outputVariables[0].getBDDVar())));
		int objective = bdd.ref(bdd.not(compObj));
		GameSolver gs = new GameSolver(simpleGame, objective, bdd);
		
		GameGraph gg = simpleGame.toGameGraph("gg");
		gg.draw("simpleGame_gg.dot", 1);
		
		
//		
//		System.out.println("checking for realizability");
//		System.out.println(gs.isRealizable(objective));
		
		Game cs = gs.counterStrategy();
		bdd.printSet(cs.T_env);
		bdd.printSet(cs.T_sys);
		
		cs.toGameGraph("cs_gg").draw("csgg.dot", 1);
		
		
		System.out.println("removing the unreachable part");
		
//		Game cs2 = gs.counterStrategyUnreachablePartRemoved();
//		cs2.toGameGraph("cs2_gg").draw("csgg2.dot", 1);
		
	
//		TestCaseGenerator tcg=new TestCaseGenerator(bdd,2);
//		Game g = tcg.generateGameWithActions();
//		int objective = tcg.noCollisionObjective(tcg.x, tcg.y, tcg.objX, tcg.objY);
//		objective=bdd.andTo(objective, tcg.safetyObjective(tcg.x, tcg.y, 1, 0));
//		
//		
//		System.out.println("objective is ");
//		bdd.printSet(objective);
//		GameSolver gs= new GameSolver(g, objective, bdd);
//		
//		gs.drawGame(g, "game.dot");
//		
//		System.out.println("checking for realizability");
//		System.out.println(gs.isRealizable(objective));
//		
//		Game strat = gs.synthesize(gs.winningRegion);
//		bdd.printSet(strat.T_env);
//		bdd.printSet(strat.T_sys);
//		
//		System.out.println("the initial set of init is ");
//		bdd.printSet(strat.getInit());
//		
//		System.out.println("one step execution");
//		int secondStep=strat.symbolicGameOneStepExecution(strat.getInit());
//		bdd.printSet(secondStep);
//		
//		System.out.println("second step execution");
//		int thirdStep=strat.symbolicGameOneStepExecution(secondStep);
//		bdd.printSet(thirdStep);
//		
//		gs.drawGame(strat, "strat.dot");
//		
//		Game cs = gs.counterStrategy();
//		bdd.printSet(cs.T_env);
//		bdd.printSet(cs.T_sys);
//		
//		System.out.println("the initial set of init is ");
//		bdd.printSet(cs.getInit());
//		
//		System.out.println("one step execution");
//		int secondStepcs=cs.symbolicGameOneStepExecution(cs.getInit());
//		bdd.printSet(secondStepcs);
//		
//		System.out.println("second step execution");
//		int thirdStepcs=cs.symbolicGameOneStepExecution(secondStepcs);
//		bdd.printSet(thirdStepcs);
//		
//		gs.drawGame(cs, "cs.dot");
//		
//		Game cs2=gs.counterStrategyUnreachablePartRemoved();
//		gs.drawGame(cs2, "cs2.dot");
//		
//		GameGraph gg1 = cs2.toGameGraph("gg1");
//		gg1.draw("gg1.dot", 1);
		
	}
	
	
	
	/**
	 * solves the given game with respect to the given objective
	 * returns a GameSolution object which encapsulates the information about the winner and its strategy
	 * @return
	 */
	public GameSolution solve(){
		Game strategyOfTheWinner;
		Player winner;
		if(isRealizable()){
//			UtilityMethods.prompt("inside the game solver, the winner is the system");
			winner = Player.SYSTEM;
//			strategyOfTheWinner = mostGeneralStrategyUnreachablePartRemoved();
			strategyOfTheWinner = mostGeneralStrategy();
		}else{
//			UtilityMethods.prompt("inside the game solver, the winner is the environment");
			winner = Player.ENVIRONMENT;
//			UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//			UtilityMethods.getUserInput();
//			strategyOfTheWinner = counterStrategyUnreachablePartRemoved();
			strategyOfTheWinner = counterStrategy();
		}
		GameSolution solution = new GameSolution(game, winner, strategyOfTheWinner, winningRegion);
		return solution;
	}
	
	public Game mostGeneralStrategy(){
		return synthesize(winningRegion);
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
			Q = Qprime;
			
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
	
	public Game synthesize(int winningRegion){
		
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.vTovPrime));
//		int winningTrans=bdd.ref(bdd.and(winningRegion, winningRegionPrime));
//		bdd.deref(winningRegionPrime);
		
		int losingSysTransitions = bdd.ref(bdd.and(game.getSystemTransitionRelation(), bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		
		//T_env for the env states of the strategy
//		int T_env = bdd.ref(bdd.and(game.T_env, winningRegion));
		int T_env = game.getEnvironmentTransitionRelation();
		int T_sys = bdd.ref(bdd.and(game.getSystemTransitionRelation(), winningRegionPrime));
		
		//remove nondeterministic acts that can lead to a losing state
		T_sys = BDDWrapper.diff(bdd, T_sys, losingActsAtStates);
		bdd.deref(losingActsAtStates);
		bdd.deref(winningRegionPrime);
		//TODO: correct actions and available actions
		//Game strategy = new Game(bdd, game.inputVariables,game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, game.getInit(), T_env, T_sys, game.actionVars, game.sysAvlActions, game.envAvlActions);
		Game strategy = new Game(bdd, game.variables, game.primedVariables, game.getInit(), T_env, T_sys, game.envActionVars, game.sysActionVars);
		strategy.setActionMap(game.getActionMap());
		strategy.setStateMap(game.getStateMap());
		return strategy;
	}
	
//	public Game counterStrategy(){
//		int complementWinningRegion=bdd.ref(bdd.not(winningRegion));
//		int complementObjective=bdd.ref(bdd.not(objective));
//		Game cs = synthesize(complementWinningRegion);
//		
//		int complementWinningRegionPrime=bdd.ref(bdd.replace(complementWinningRegion, game.vTovPrime));
//		int deadlocks=bdd.ref(bdd.and(complementObjective, complementWinningRegionPrime));
//		bdd.deref(complementWinningRegionPrime);
//		
////		int complementObjectivePrime=bdd.ref(bdd.replace(complementObjective, vTovPrime));
////		int deadlocks=bdd.ref(bdd.and(complementWinningRegion, complementObjectivePrime));
////		bdd.deref(complementObjectivePrime);
//		
//		bdd.deref(complementWinningRegion);
//		bdd.deref(complementObjective);
//		cs.T_sys = BDDWrapper.diff(bdd, cs.T_sys, deadlocks);
//		
//		return cs;
//	}
	
	public Game counterStrategy(){
		
		
		
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
		Game counterStrategy =  new Game(bdd, game.variables, game.primedVariables, csInit, csT_env, csT_sys, game.envActionVars, game.sysActionVars);
		counterStrategy.setActionMap(game.getActionMap());
		counterStrategy.setStateMap(game.getStateMap());
		return counterStrategy;
		
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
	
	
//	public boolean isRealizable(int complementErrorSet){
//		winningRegion=greatestFixPoint(complementErrorSet);
//		solved =true;
//		return BDDWrapper.subset(bdd, game.getInit(), winningRegion);
//		
//	}
	
	
	//This part of the code is not used right now
	//TODO: clean up 
	
//	/**
//	 * TODO: the code seems to compute the most general counter-strategy
//	 * check it in the next phase
//	 * @return
//	 */
//	public Game mostGeneralCounterStrategy(){
//		int complementWinningRegion=bdd.ref(bdd.not(winningRegion));
//		
//		
//		//compute APre of losing region as the set of system states that cannot avoid losing positions
//		int cube=bdd.ref(bdd.and(game.getActionsCube(), game.getPrimeVariablesCube()));
//		int losingSystemStates = game.ApreImage(complementWinningRegion, cube, game.T_sys);
//		
//		//in the counter-strategy, all system actions takes place in losing states
//		int csT_sys = bdd.ref(bdd.and(game.T_sys, losingSystemStates));
//		
//		//environment transitions are those that can reach a losing system state
//		int losingSystemStatesPrime=bdd.ref(bdd.replace(losingSystemStates, game.vTovPrime));
//		int csT_env = bdd.ref(bdd.and(game.T_env, losingSystemStatesPrime));
////		System.out.println("T_env is ");
////		bdd.printSet(csT_env);
////		System.out.println("the objective is ");
////		bdd.printSet(objective);
//		csT_env=bdd.andTo(csT_env, objective);
////		System.out.println("and");
////		bdd.printSet(csT_env);
//		
////		System.out.println("in cs computation. T_env is ");
////		bdd.printSet(csT_env);
//		
//
//		
//		
//		//define the initial states
//		int csInit=bdd.ref(bdd.and(game.getInit(), complementWinningRegion));
//		
////		int complementWinningRegionPrime=bdd.ref(bdd.replace(complementWinningRegion, game.vTovPrime));
////		int deadlocks=bdd.ref(bdd.and(bdd.not(objective),complementWinningRegionPrime));
////		System.out.println("deadlocks");
////		bdd.printSet(deadlocks);
////		bdd.deref(complementWinningRegionPrime);
////		
//////		int deadlocks=bdd.ref(bdd.and(bdd.not(objective), losingSystemStatesPrime));
////		
////		int csT_env = BDDWrapper.diff(bdd, csT_env_tmp, deadlocks);
//		
////		bdd.deref(csT_env_tmp);
//		bdd.deref(cube);
//		bdd.deref(losingSystemStates);
//		bdd.deref(losingSystemStatesPrime);
//		
//		Game counterStrategy =  new Game(bdd, game.inputVariables, game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, csInit, csT_env, csT_sys, game.actionVars, game.getSystemActionAvailabilityList(), game.getEnvironmentActionAvailabilityList());
//		return counterStrategy;
//	}
	
//	public Game counterStrategyUnreachablePartRemoved(){
//		Game cs = counterStrategy();
//		int reachable = cs.reachable();
//		cs.T_env = bdd.andTo(cs.T_env, reachable);
//		
//		int cube = bdd.ref(bdd.and(cs.getActionsCube(), cs.getVariablesCube()));
//		int reachableSystemStates = cs.EpostImage(reachable, cube , cs.T_env);
//		
////		int reachablePrime = bdd.ref(bdd.replace(reachable, game.vTovPrime));
////		cs.T_sys=bdd.andTo(cs.T_sys, reachablePrime);
//		cs.T_sys =  bdd.andTo(cs.T_sys, reachableSystemStates);
//		return cs;
//	}
//	
//
//	
//	public Game mostGeneralStrategyUnreachablePartRemoved(){
//		Game strat = mostGeneralStrategy();
//		int reachable = strat.reachable();
//		strat.T_env = bdd.andTo(strat.T_env, reachable);
//		
//		int cube = bdd.ref(bdd.and(strat.getActionsCube(), strat.getVariablesCube()));
//		int reachableSystemStates = strat.EpostImage(reachable, cube , strat.T_env);
//		
////		int reachablePrime = bdd.ref(bdd.replace(reachable, game.vTovPrime));
////		cs.T_sys=bdd.andTo(cs.T_sys, reachablePrime);
//		strat.T_sys =  bdd.andTo(strat.T_sys, reachableSystemStates);
//		return strat;
//	}
	
}
