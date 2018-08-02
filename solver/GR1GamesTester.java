package solver;

import game.GameSolution;
import game.GameStructure;

import java.util.ArrayList;


import game.Variable;
import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class GR1GamesTester {

	public static void main(String[] args) {
		
		BDD bdd = new BDD(10000, 1000);
		BDDWrapper bddWrapper = new BDDWrapper(bdd);
		
		
		Variable[] vars1 = Variable.createVariables(bdd, 2, "a");
		Variable[] vars2 = Variable.createVariables(bdd, 2, "b");
		
		int compare = UtilityMethods.lessThan(bdd, vars1, vars2);
		
		UtilityMethods.debugBDDMethods(bdd, "less than", compare);
		UtilityMethods.getUserInput();
		
		int dim = 1;
		
		GridCell2D envInitCell = new GridCell2D(dim, dim);
		GridCell2D sysInitCell = new GridCell2D(0, 0);
		
		//define a game structure
		Agent2D uncontrolled = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R1", AgentType.Uncontrollable, envInitCell);
		Agent2D controlled = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R2", AgentType.Controllable, sysInitCell);
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(uncontrolled);
		agents.add(controlled);
		
		GameStructure gs = GameStructure.createGameForAgents(bdd, agents);
		
//		gs.printGameVars();
//		
//		gs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		int noCollision = UtilityFormulas.noCollision(bdd, uncontrolled, controlled);
		int noCollisionPrime = BDDWrapper.replace(bdd, noCollision, gs.getVtoVprime());
		
//		UtilityMethods.debugBDDMethods(bdd, "no collision", noCollision);
//		UtilityMethods.debugBDDMethods(bdd, "no collision prime ", noCollisionPrime);
		
//		UtilityMethods.getUserInput();
		
		int newT_env = bdd.ref(bdd.and(gs.getEnvironmentTransitionRelation(), noCollision)); 
		
		int newT_sys = bdd.ref(bdd.and(gs.getSystemTransitionRelation(), noCollisionPrime));
		
		
		gs = new GameStructure(bdd, gs.variables, gs.primedVariables, gs.getInit(), newT_env, 
				newT_sys, gs.envActionVars, gs.sysActionVars);
		
		gs.printGameVars();
//		
//		gs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		//define a GR1 objective
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
//		int first = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim-1, 0));
//		int next = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(1, 0));
//		int nextPrime = BDDWrapper.replace(bdd, next, gs.getVtoVprime());
		
//		int ass1 = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim, 0));
//		ass1 = BDDWrapper.not(bdd, ass1);
		
//		int ass1 = BDDWrapper.assign(bdd, 1, uncontrolled.getXVars());
////		
//		int ass2 = BDDWrapper.assign(bdd, 0, uncontrolled.getXVars());
		
//		int ass1 = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim, 0));
//		int ass2 = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(0, dim));
		
		int ass1pre = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(0, 0));
		int assNext = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(0, dim));
		int assPost = BDDWrapper.replace(bdd, assNext, gs.getVtoVprime());
		int ass1 =bdd.imp(ass1pre, assPost);
		
		int ass2pre = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim, dim));
		int ass2 =bdd.imp(ass2pre, assPost);
		
//		int ass1 = BDDWrapper.and(bdd, first, nextPrime);
		
		UtilityMethods.debugBDDMethods(bdd, "ass1", ass1);
		UtilityMethods.debugBDDMethods(bdd, "ass2", ass2);
		UtilityMethods.getUserInput();
		
//		//preprocess assumptions
//		int tmp1 = BDDWrapper.and(bdd, ass1,gs.getEnvironmentTransitionRelation());
//		ass1 = BDDWrapper.exists(bdd, tmp1, gs.getPrimedVariablesAndActionsCube());
//		
//		int tmp2 = BDDWrapper.and(bdd, ass2,gs.getEnvironmentTransitionRelation());
//		ass2 = BDDWrapper.exists(bdd, tmp2, gs.getPrimedVariablesAndActionsCube());
//		
//		UtilityMethods.debugBDDMethods(bdd, "ass1 after normalization", ass1);
//		UtilityMethods.debugBDDMethods(bdd, "ass2 after normalization", ass2);
//		UtilityMethods.getUserInput();
		
		assumptions.add(ass1);
		assumptions.add(ass2);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		int g1 = UtilityFormulas.assignCell(bdd, controlled, new GridCell2D(dim, 0));
//		int g2 = UtilityFormulas.assignCell(bdd, controlled, new GridCell2D(0, dim));
		
		guarantees.add(g1);
//		guarantees.add(g2);
		
//		System.out.println("g1 has prime vars? "+gs.includePrimeVariable(g1));
//		System.out.println("tmp1 has prime vars? "+gs.includePrimeVariable(tmp1));
//		System.out.println("ass1 has prime vars? "+gs.includePrimeVariable(ass1));
		
		UtilityMethods.debugBDDMethods(bdd, "g1", g1);
//		UtilityMethods.debugBDDMethods(bdd, "g2", g2);
		UtilityMethods.getUserInput();
		
		GR1Objective obj = new GR1Objective(bddWrapper, assumptions, guarantees);
		
		
		//define init
		int init = bddWrapper.and(uncontrolled.getInit(), controlled.getInit());
		//compute winning states
//		GR1WinningStates W = GR1Solver.computeWinningStates(bddWrapper, gs, init, obj);
//		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", W.getWinningStates());
//		UtilityMethods.getUserInput();
		
		GR1WinningStates w2 = GR1Solver.computeWinningStatesWithExtendedAssumptions(bddWrapper, gs, init, obj);
		
		UtilityMethods.debugBDDMethods(bdd, "winning region with extended assumptions is", w2.getWinningStates());
		UtilityMethods.getUserInput();
		
		w2.printMemory(bdd);
		
//		W.printMemory(bdd);
		
//		GameSolution solution = GR1Solver.solve(bdd, gs, init, obj);
//		
//		solution.print();
	}
	
//	public static CaseStudy2D simpleGR1Test(int dim){
//		BDD bdd = new BDD(10000, 1000);
//		BDDWrapper bddWrapper = new BDDWrapper(bdd);
//		
//		
//		GridCell2D envInitCell = new GridCell2D(dim, dim);
//		GridCell2D sysInitCell = new GridCell2D(0, 0);
//		
//		//define a game structure
//		Agent2D uncontrolled = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R1", AgentType.Uncontrollable, envInitCell);
//		Agent2D controlled = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R2", AgentType.Controllable, sysInitCell);
//		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
//		agents.add(uncontrolled);
//		agents.add(controlled);
//		
//		GameStructure gs = GameStructure.createGameForAgents(bdd, agents);
//		
//gs.printGameVars();
//		
////		gs.printGame();
////		
////		UtilityMethods.getUserInput();
//		
//		int noCollision = UtilityFormulas.noCollision(bdd, uncontrolled, controlled);
//		int noCollisionPrime = BDDWrapper.replace(bdd, noCollision, gs.getVtoVprime());
//		
////		UtilityMethods.debugBDDMethods(bdd, "no collision", noCollision);
////		UtilityMethods.debugBDDMethods(bdd, "no collision prime ", noCollisionPrime);
//		
////		UtilityMethods.getUserInput();
//		
//		int newT_sys = bdd.ref(bdd.and(gs.getSystemTransitionRelation(), noCollisionPrime));
//		
//		
//		gs = new GameStructure(bdd, gs.variables, gs.primedVariables, gs.getInit(), gs.getEnvironmentTransitionRelation(), 
//				newT_sys, gs.envActionVars, gs.sysActionVars);
//		
//		gs.printGameVars();
//		
//		gs.printGame();
//		
//		UtilityMethods.getUserInput();
//		
//		//define a GR1 objective
//		ArrayList<Integer> assumptions = new ArrayList<Integer>();
//		
////		int first = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim-1, 0));
////		int next = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(1, 0));
////		int nextPrime = BDDWrapper.replace(bdd, next, gs.getVtoVprime());
//		
//		int ass1 = UtilityFormulas.assignCell(bdd, uncontrolled, new GridCell2D(dim-1, 0));
//		ass1 = BDDWrapper.not(bdd, ass1);
//		
////		int ass1 = BDDWrapper.and(bdd, first, nextPrime);
//		
//		UtilityMethods.debugBDDMethods(bdd, "ass1", ass1);
//		UtilityMethods.getUserInput();
//		
//		
//		assumptions.add(ass1);
//		
//		ArrayList<Integer> guarantees = new ArrayList<Integer>();
//		
//		int g1 = UtilityFormulas.assignCell(bdd, controlled, new GridCell2D(dim-1, 0));
//		
//		guarantees.add(g1);
//		
//		GR1Objective obj = new GR1Objective(bddWrapper, assumptions, guarantees);
//		
//		
//		//define init
//		int init = bddWrapper.and(uncontrolled.getInit(), controlled.getInit());
//		//compute winning states
//		GR1WinningStates W = GR1Solver.computeWinningStates(bddWrapper, gs, init, obj);
//		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", W.getWinningStates());
//		UtilityMethods.getUserInput();
//		
//		GameSolution solution = GR1Solver.solve(bdd, gs, init, obj);
//		
//		solution.print();
//		
//		CaseStudy2D cs = new CaseStudy2D(bdd, dim, dim, agents, argGame, argStatic);
//	}

}
