package csguided;

import java.util.ArrayList;

import utils.UtilityMethods;
import csguided.Objective.ObjectiveType;
import jdd.bdd.BDD;

public class FairGameSolver {
	
	public static FairGameSolution solve(BDD bdd, FairGame game, Objective obj){
//		FairGame strategyOfTheWinner;
//		Player winner;
//		FairGameSolution solution = computeWinningRegion(bdd, game, obj);
//		if(solution.getWinner()==Player.SYSTEM){
//			FairGame mostGeneralStrategy = mostGeneralStrategy(bdd, solution);
//			solution.setStrategyOfTheWinner(mostGeneralStrategy);
//		}else{
//			FairGame mostGeneralCounterStrategy= mostGeneralStrategy(bdd, solution);
//			solution.setStrategyOfTheWinner(mostGeneralCounterStrategy);
//		}
//		return solution;
		
		return mostGeneralStrategy(bdd, game, obj);
	}
	
	public static FairGameSolution solve(BDD bdd, FairGame game, ArrayList<Objective> objectives){
		//TODO: sorting the objectives
		FairGame currentFairGame = game;
		FairGameSolution solution = null;
		for(Objective obj : objectives){
			solution =  solve(bdd, currentFairGame, obj);
			if(solution.getWinner()==Player.SYSTEM){
				currentFairGame = solution.strategyOfTheWinner();
			}else{
				System.out.println("The objectives can not be enforced");
				return solution;
			}
		}
		return solution;
	}
	
	public static FairGame mostGeneralStrategy(BDD bdd, FairGameSolution solution){
		return synthesize(bdd, solution.getGameStructure(), solution.getWinningSystemStates());
	}
	
	public static FairGameSolution mostGeneralStrategy(BDD bdd, FairGame game, Objective obj){
		FairGameSolution solution=null;
		if(obj.type == ObjectiveType.Reachability){
			int targetStates = obj.getObjectiveFormula();
			int targetStatesPrime = bdd.replace(targetStates, game.vTovPrime);
			FairGame reachabilityGame = game.FairGameWithReachabilityObjective("RG", targetStates, targetStatesPrime);
//			reachabilityGame.removeUnreachableStates().toGameGraph().draw("reachabilityGame.dot", 1, 0);
			return computeMostGeneralStrategyForReachabilityObjectives(bdd, reachabilityGame, obj.getObjectiveFormula());
		}else if(obj.type == ObjectiveType.Safety){
			if(game.fairTransitions.size()==0){
				
				int winningRegion = greatestFixPoint(bdd, game, obj.getObjectiveFormula());
				FairGame strategy = synthesize(bdd, game, winningRegion);
				return new FairGameSolution(game, Player.SYSTEM, strategy, winningRegion); 
			}else{
				
				return computeMostGeneralStrategyForSafetyObjectivesWithFairness(bdd, game, obj.getObjectiveFormula());
			}
		}
		return solution;
	}
	
	public static FairGameSolution computeMostGeneralStrategyForSafetyObjectivesWithFairness(BDD bdd, FairGame game, int obj){
		//computhe the set of winning system states with no fairness requirements
		int winningRegion = greatestFixPoint(bdd, game, obj);
//		UtilityMethods.debugBDDMethods(bdd, "winning region", winningRegion);
		int statesWithoutFairnessReqs=statesWithNoFairnessRequirements(bdd, game);
//		UtilityMethods.debugBDDMethods(bdd, "states with no fairness", statesWithoutFairnessReqs);
		int winningSystemStates=game.controllablePredecessor_system(winningRegion);
//		UtilityMethods.debugBDDMethods(bdd, "safe system states", winningSystemStates);
		int winningSystemStatesWithoutFairnessReqs=bdd.ref(bdd.and(statesWithoutFairnessReqs, winningSystemStates));
//		UtilityMethods.debugBDDMethods(bdd, "winning system states without any fairness ", winningSystemStatesWithoutFairnessReqs);
		
		//Environment states that pushes the game to finitary fariness part, 
		//the new objective is to reach these states while stayin in I
		int T = game.controllablePredecessor_env(winningSystemStatesWithoutFairnessReqs);
		T=bdd.andTo(T, winningRegion);
		
		//compute the strategy that ensures safety by staying in the winning region and making progress toward 
		//the winning states with no fairness requirement
		return computeMostGeneralStrategyFor_I_until_objective(bdd, game, winningRegion, T);
	}
	
	public static int statesWithNoFairnessRequirements(BDD bdd, FairGame game){
		ArrayList<Integer> fairnessReqs=game.getFairnessRequirements();
		int statesWithFairnessReqs=bdd.ref(bdd.getZero());
		for(Integer f : fairnessReqs){
			int states = bdd.ref(bdd.exists(f, game.getPrimeVariablesCube()));
			statesWithFairnessReqs = bdd.orTo(statesWithFairnessReqs, states);
			bdd.deref(states);
		}
		int statesWithoutFairnessReqs=BDDWrapper.complement(bdd, statesWithFairnessReqs);
		bdd.deref(statesWithFairnessReqs);
		return statesWithoutFairnessReqs;
	}
	
	/**
	 * Ensures that the system stays in the safety set I until it reaches the objective
	 * obj \subseteq I
	 * @param bdd
	 * @param game
	 * @param I
	 * @param obj
	 * @return
	 */
	public static FairGameSolution computeMostGeneralStrategyFor_I_until_objective(BDD bdd, FairGame game, int I, int obj){
		//compute the winning states with a least fixed point computation while keeping the intermediate results
		ArrayList<Integer> strategyFairness=new ArrayList<Integer>();
		
//		game.printGameVars();
		Variable flag = game.variables[game.variables.length-1];
		int flagDown = BDDWrapper.complement(bdd, flag.getBDDVar());
//		UtilityMethods.debugBDDMethods(bdd, "flag down", flagDown);
				
		for(int i=0;i<game.fairTransitions.size();i++){
			strategyFairness.add(game.fairTransitions.get(i));
		}
				
		int Q = bdd.ref(bdd.getZero());
		int winningEnvStates = bdd.ref(bdd.and(obj, bdd.getOne()));
		int winningSysStates = bdd.ref(bdd.getZero());
		do{
			bdd.deref(Q);
			Q = winningEnvStates;
			int preSys = game.controllablePredecessor_system(winningEnvStates);
//			preSys=bdd.andTo(preSys, I);
			int newWinningSysStates = BDDWrapper.diff(bdd, preSys, winningSysStates);
//			UtilityMethods.debugBDDMethods(bdd, "new winning system states", newWinningSysStates);
			
			//only add those states that their flag is down (because of finitary fairness)
			newWinningSysStates = bdd.andTo(newWinningSysStates, flagDown);
//			UtilityMethods.debugBDDMethods(bdd, "new winning system states with flag down", newWinningSysStates);
					
			//add the transition from new system winning states to the previously computed winning states to 
			//strategy fairness requirements to ensure progress
			int Qprime = bdd.ref(bdd.replace(Q, game.vTovPrime));
			int trans=bdd.ref(bdd.and(newWinningSysStates, Qprime));
			strategyFairness.add(trans);
//			UtilityMethods.debugBDDMethods(bdd, "fairness added", trans);
			bdd.deref(Qprime);
					
			winningSysStates=bdd.orTo(winningSysStates, preSys);
			int pre = game.controllablePredecessor_env(preSys);
			pre=bdd.andTo(pre, I);
			bdd.deref(preSys);
			winningEnvStates = bdd.ref(bdd.or(winningEnvStates, pre));
			bdd.deref(pre);
					
					
					
		}while(Q != winningEnvStates);
		//TODO: it might be unnecessary, check if we should uncomment it
//		bdd.deref(winningEnvStates);
				
		//setting the transition relations for the strategy
		//TODO: what if it is unrealizable? check that Q_0 \in Winning states
		FairGame mostGeneralStrategy = synthesize(bdd, game, winningEnvStates);
		mostGeneralStrategy.fairTransitions = strategyFairness;
				
		Player winner;
		if(BDDWrapper.subset(bdd, game.getInit(), winningEnvStates)){
			winner = Player.SYSTEM;
		}else{
			winner=Player.ENVIRONMENT;
		}
				
		FairGameSolution solution = new FairGameSolution(game, winner, mostGeneralStrategy, winningEnvStates);
		return solution;
	}
	
	public static FairGameSolution computeMostGeneralStrategyForReachabilityObjectives(BDD bdd, FairGame game, int obj){
		return computeMostGeneralStrategyFor_I_until_objective(bdd, game, bdd.getOne(), obj);
	}
	
	public static FairGame synthesize(BDD bdd, FairGame game, int winningRegion){
		
		int winningRegionPrime=bdd.ref(bdd.replace(winningRegion, game.vTovPrime));
//		int winningTrans=bdd.ref(bdd.and(winningRegion, winningRegionPrime));
//		bdd.deref(winningRegionPrime);
		
		int losingSysTransitions = bdd.ref(bdd.and(game.T_sys, bdd.not(winningRegionPrime)));
		int losingActsAtStates = bdd.ref(bdd.exists(losingSysTransitions, game.getPrimeVariablesCube()));
		
		//T_env for the env states of the strategy
		int T_env = bdd.ref(bdd.and(game.T_env, winningRegion));
		int T_sys = bdd.ref(bdd.and(game.T_sys, winningRegionPrime));
		
		//remove nondeterministic acts that can lead to a losing state
		T_sys = BDDWrapper.diff(bdd, T_sys, losingActsAtStates);
		bdd.deref(losingActsAtStates);
		bdd.deref(winningRegionPrime);
		//TODO: correct actions and available actions
		//Game strategy = new Game(bdd, game.inputVariables,game.outputVariables, game.inputPrimeVariables, game.outputPrimeVariables, game.getInit(), T_env, T_sys, game.actionVars, game.sysAvlActions, game.envAvlActions);
		FairGame strategy = new FairGame(bdd, game.variables, game.primedVariables, game.getInit(), T_env, T_sys, game.actionVars);
		strategy.setActionMap(game.getActionMap());
		strategy.setStateMap(game.getStateMap());
		return strategy;
	}
	
	

	public static FairGameSolution computeWinningRegion(BDD bdd, FairGame game, Objective obj ){
		Player winner;
		FairGameSolution solution;
		int winningRegion=-1;
		if(obj.type == ObjectiveType.Safety){
			winningRegion=greatestFixPoint(bdd, game, obj.getObjectiveFormula());
		}else if(obj.type == ObjectiveType.Reachability){
			winningRegion = leastFixedPoint(bdd, game, obj.getObjectiveFormula());
		}
		if(BDDWrapper.subset(bdd, game.getInit(), winningRegion)){
			winner = Player.SYSTEM;
		}else{
			winner=Player.ENVIRONMENT;
		}
		
		return new FairGameSolution(game, winner, null, winningRegion);
	}
	
	public static int greatestFixPoint(BDD bdd, FairGame game, int set){
		int Q=bdd.ref(bdd.getOne());
		//TODO: it was QPrime = set, but not sure deref ing QPrime effects set too or not
		int Qprime=bdd.ref(bdd.and(set, bdd.getOne()));
		do{
//		while(Q != Qprime){
			bdd.deref(Q);
			//TODO: it was Q=QPrime
			Q=bdd.ref(bdd.and(Qprime, bdd.getOne()));
			
//			UtilityMethods.debugBDDMethods(bdd, "gfp", Q);
			
			int pre = game.controllablePredecessor(Qprime);
			Qprime = bdd.andTo(Qprime, pre);
			bdd.deref(pre);	
		}while(Q != Qprime);
		bdd.deref(Qprime);
		return Q;
	}
	
	public static int leastFixedPoint(BDD bdd, FairGame game, int set){
		int Q = bdd.ref(bdd.getZero());
		int Qprime = bdd.ref(bdd.and(set, bdd.getOne()));
		do{
			bdd.deref(Q);
			Q = Qprime;
			int pre = game.controllablePredecessor(Qprime);
			Qprime = bdd.ref(bdd.or(Qprime, pre));
			bdd.deref(pre);
		}while(Q != Qprime);
		bdd.deref(Qprime);
		return Q;
	}
	

	
	public static void main(String[] args) {
		
		
		test4();
		
	}
	
	public static void test1(){
		BDD bdd = new BDD(10000, 1000);
		Game g = TestCaseGenerator.simpleGame(bdd);
		g.toGameGraph().draw("game.dot", 1);
		
		FairGame fg = g.toFairGame();
		fg.toGameGraph().draw("fairGame.dot", 1);
		
		//define objectives
		Variable[] vars=fg.variables;
		int a = vars[0].getBDDVar();
		int b = vars[1].getBDDVar();
		
		//objective 3: F (!b)
		int objF3 = bdd.ref(bdd.not(b));
		Objective obj3 = new Objective(ObjectiveType.Reachability, objF3);
		
		//testing for safety objective with no fairness
		FairGameSolution solution1 = FairGameSolver.solve(bdd, fg, obj3);
		solution1.print();
		solution1.drawWinnerStrategy("obj2.dot", 1, 1);
	}
	
	public static void test2(){
		BDD bdd = new BDD(10000, 1000);
		Game g = TestCaseGenerator.simpleGameWith3vars(bdd);
		g.toGameGraph().draw("game.dot", 1);
		
		FairGame fg = g.toFairGame();
		fg.toGameGraph().draw("fairGame.dot", 1);
		
		//define objectives
		Variable[] vars=fg.variables;
		int a = vars[0].getBDDVar();
		int b = vars[1].getBDDVar();
		int c = vars[2].getBDDVar();
		
		//objective 1: G(a != c)
		int objF1 = bdd.ref(bdd.biimp(a, c));
//		int objF1c = bdd.ref(bdd.nodeCount(objF1));
		Objective obj1 = new Objective(ObjectiveType.Safety, objF1);
		
		//objective 2: F (b)
		int objF2 = bdd.ref(b);
		Objective obj2 = new Objective(ObjectiveType.Reachability, objF2);
		
		//objective 3: F (!b)
		int objF3 = bdd.ref(bdd.not(b));
		Objective obj3 = new Objective(ObjectiveType.Reachability, objF3);
		
		//testing for safety objective with no fairness
		FairGameSolution solution1 = FairGameSolver.solve(bdd, fg, obj2);
		solution1.print();
		solution1.drawWinnerStrategy("obj2.dot", 1, 1);
		
		FairGameSolution solution2 = FairGameSolver.solve(bdd, solution1.strategyOfTheWinner(), obj1);
		solution2.print();
		solution2.drawWinnerStrategy("obj1.dot", 1, 1);
		
		FairGameSolution solution3 = FairGameSolver.solve(bdd, solution2.strategyOfTheWinner(), obj3);
		solution3.print();
		solution3.drawWinnerStrategy("obj3.dot", 1, 1);
	}
	
	public static void test3(){
		BDD bdd = new BDD(10000, 1000);
		Game g = TestCaseGenerator.simpleGame3(bdd);
		g.toGameGraph().draw("game.dot", 1);
		
		FairGame fg = g.toFairGame();
		fg.toGameGraph().draw("fairGame.dot", 1);
		
		//define objectives
		Variable[] vars=fg.variables;
		int a = vars[0].getBDDVar();
		int b = vars[1].getBDDVar();
		int c = vars[2].getBDDVar();
		
		//objective 1: G(a | b | c)
		int objF1 = bdd.ref(bdd.or(a, b));
		objF1 = bdd.orTo(objF1, c);
		Objective obj1 = new Objective(ObjectiveType.Safety, objF1);
		
		//objective 2: F (!b & c )
		int objF2 = bdd.ref(bdd.and(bdd.not(b), c));
		Objective obj2 = new Objective(ObjectiveType.Reachability, objF2);
				
//		//testing for safety objective with no fairness
//		FairGameSolution solution1 = FairGameSolver.solve(bdd, fg, obj1);
//		solution1.print();
//		solution1.drawWinnerStrategy("obj1.dot", 1, 1);
//		
//		FairGameSolution solution2 = FairGameSolver.solve(bdd, solution1.strategyOfTheWinner(), obj2);
//		solution2.print();
//		solution2.drawWinnerStrategy("obj2.dot", 1, 1);

		//testing for reachability objective first
		FairGameSolution solution1 = FairGameSolver.solve(bdd, fg, obj2);
		solution1.print();
		solution1.drawWinnerStrategy("obj2.dot", 1, 1);
				
		FairGameSolution solution2 = FairGameSolver.solve(bdd, solution1.strategyOfTheWinner(), obj1);
		solution2.print();
		solution2.drawWinnerStrategy("obj1.dot", 1, 1);
	}
	
	public static void test4(){
		BDD bdd = new BDD(10000, 1000);
		Game g = TestCaseGenerator.simpleNondeterministicGame(bdd);
		g.toGameGraph().draw("game.dot", 1);
		
		FairGame fg = g.toFairGame();
		fg.toGameGraph().draw("fairGame.dot", 1);
		
		//define objectives
		Variable[] vars=fg.variables;
		int a = vars[0].getBDDVar();
		int b = vars[1].getBDDVar();
		
		//objective 1: F (!a && !b )
		int objF1 = BDDWrapper.and(bdd, bdd.not(a), bdd.not(b));
		Objective obj1 = new Objective(ObjectiveType.Reachability, objF1);
		
		int objF2 = BDDWrapper.and(bdd, a, BDDWrapper.not(bdd, b));
		Objective obj2 = new Objective(ObjectiveType.Reachability, objF2);
		
		
		FairGameSolution solution1 = FairGameSolver.solve(bdd, fg, obj1);
		solution1.print();
		solution1.drawWinnerStrategy("obj1.dot", 1, 1);
				
		FairGameSolution solution2 = FairGameSolver.solve(bdd, fg, obj2);
		solution2.print();
		solution2.drawWinnerStrategy("obj2.dot", 1, 1);
		
		int objF3 = bdd.ref(bdd.or(objF1, objF2));
		Objective obj3 = new Objective(ObjectiveType.Reachability, objF3);
		FairGameSolution solution3 = FairGameSolver.solve(bdd, fg, obj3);
		solution3.print();
		solution3.drawWinnerStrategy("obj3.dot", 1, 1);
	}

}
