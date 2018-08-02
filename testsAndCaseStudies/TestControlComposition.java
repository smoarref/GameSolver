package testsAndCaseStudies;

import java.util.ArrayList;

import game.GameSolution;
import game.GameSolver;
import game.BDDWrapper;
import game.GameStructure;
import game.Variable;
import jdd.bdd.BDD;
import jdd.examples.BDDQueens;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class TestControlComposition {
	public static void test(int dim){
		BDD bdd = new BDD(10000, 1000);
		int numOfVars = UtilityMethods.numOfBits(dim);
		
		//define variables
		
		int numOfAgents = 3;
		ArrayList<String> agentNames = new ArrayList<String>();
		String nameOfAgentsPrefix = "R";
		for(int i=0;i<numOfAgents;i++){
			agentNames.add(nameOfAgentsPrefix+i+"_");
		}
		
		//define action variables
		ArrayList<String> agentsActionVarsPrefix = new ArrayList<String>();
		ArrayList<String> agentsXvarsPrefix = new ArrayList<String>();
		ArrayList<String> agentsYvarsPrefix = new ArrayList<String>();
		
		for(int i= 0; i<numOfAgents;i++){
			String currentAgentName = agentNames.get(i);
			agentsActionVarsPrefix.add(currentAgentName+"act");
			agentsXvarsPrefix.add(currentAgentName+"X"); 
			agentsYvarsPrefix.add(currentAgentName+"Y");
		}
		
		ArrayList<String> agentsActionVars = UtilityMethods.createInterleavingVariableOrder(agentsActionVarsPrefix, 2);
		ArrayList<String> agentsXvars = UtilityMethods.createInterleavingVariableOrder(agentsXvarsPrefix, numOfVars);
		ArrayList<String> agentsYvars = UtilityMethods.createInterleavingVariableOrder(agentsYvarsPrefix, numOfVars);
		
		Variable[] actionVars = Variable.createVariables(bdd, agentsActionVars);
		Variable[] xVars = Variable.createVariablesAndPrimedCopies(bdd, agentsXvars);
		Variable[] yVars = Variable.createVariablesAndPrimedCopies(bdd, agentsYvars);
		
		//debugging
//		System.out.println("action vars created");
//		Variable.printVariables(actionVars);
//		
//		System.out.println("x vars created ");
//		Variable.printVariables(xVars);
//		
//		System.out.println("y vars created ");
//		Variable.printVariables(yVars);
//		
//		UtilityMethods.getUserInput();
		
		//create agents
		String robot1Name = agentNames.get(0);
		Variable[] agent1actionVars = Variable.getVariablesStartingWith(actionVars, agentsActionVarsPrefix.get(0));
		Variable[] agent1Xvars = Variable.getVariablesStartingWith(xVars, agentsXvarsPrefix.get(0));
		Variable[] agent1Yvars = Variable.getVariablesStartingWith(yVars, agentsYvarsPrefix.get(0));
		Agent2D robot1 = createSimpleRobot(bdd, dim, robot1Name, AgentType.Controllable, new GridCell2D(0, 0), agent1Xvars, agent1Yvars, agent1actionVars);
		
//		robot1.print();
//		UtilityMethods.getUserInput();
		
		String robot2Name = agentNames.get(1);
		Variable[] agent2actionVars = Variable.getVariablesStartingWith(actionVars, agentsActionVarsPrefix.get(1));
		Variable[] agent2Xvars = Variable.getVariablesStartingWith(xVars, agentsXvarsPrefix.get(1));
		Variable[] agent2Yvars = Variable.getVariablesStartingWith(yVars, agentsYvarsPrefix.get(1));
		Agent2D robot2 = createSimpleRobot(bdd, dim, robot2Name, AgentType.Controllable, new GridCell2D(dim, 0), agent2Xvars, agent2Yvars, agent2actionVars);
		
//		robot2.print();
//		UtilityMethods.getUserInput();
		
		String robot3Name = agentNames.get(2);
		Variable[] agent3actionVars = Variable.getVariablesStartingWith(actionVars, agentsActionVarsPrefix.get(2));
		Variable[] agent3Xvars = Variable.getVariablesStartingWith(xVars, agentsXvarsPrefix.get(2));
		Variable[] agent3Yvars = Variable.getVariablesStartingWith(yVars, agentsYvarsPrefix.get(2));
		Agent2D robot3 = createSimpleRobot(bdd, dim, robot3Name, AgentType.Uncontrollable, new GridCell2D(dim, dim), agent3Xvars, agent3Yvars, agent3actionVars);
		
//		robot3.print();
//		UtilityMethods.getUserInput();
		
//		String robot4Name = agentNames.get(3);
//		Variable[] agent4actionVars = Variable.getVariablesStartingWith(actionVars, agentsActionVarsPrefix.get(3));
//		Variable[] agent4Xvars = Variable.getVariablesStartingWith(xVars, agentsXvarsPrefix.get(3));
//		Variable[] agent4Yvars = Variable.getVariablesStartingWith(yVars, agentsYvarsPrefix.get(3));
//		Agent2D robot4 = createSimpleRobot(bdd, dim, robot4Name, AgentType.Controllable, new GridCell2D(0, dim), agent4Xvars, agent4Yvars, agent4actionVars);
		
		
//		robot4.print();
//		UtilityMethods.getUserInput();
		
		//define safety objectives
		int safety1 = UtilityFormulas.noCollisionObjective(bdd, robot1.getXVars(), robot1.getYVars(), robot3.getXVars(), robot3.getYVars());
		int safety2 = UtilityFormulas.noCollisionObjective(bdd, robot2.getXVars(), robot2.getYVars(), robot3.getXVars(), robot3.getYVars());
//		int safety3 = UtilityFormulas.noCollisionObjective(bdd, robot4.getXVars(), robot4.getYVars(), robot3.getXVars(), robot3.getYVars());
		int safety4 = UtilityFormulas.noCollisionObjective(bdd, robot1.getXVars(), robot1.getYVars(), robot2.getXVars(), robot2.getYVars());
		int safety = BDDWrapper.and(bdd, safety1, safety2);
//		safety = BDDWrapper.andTo(bdd, safety, safety3);
		safety = BDDWrapper.andTo(bdd, safety, safety4);
		
		//form the game structures
		GameStructure gs1 = GameStructure.createGameForAgents(bdd, robot3, robot1);
		GameStructure gs2 = GameStructure.createGameForAgents(bdd, robot3, robot2);
//		GameStructure gs3 = GameStructure.createGameForAgents(bdd, robot3, robot4);
		GameStructure gs4 = gs1.compose(gs2);
		
		long t0;
		long t0_2;
		
		//central method 
		t0 = UtilityMethods.timeStamp();
		GameStructure gs = gs1.compose(gs2);
//		gs = gs.compose(gs3);
		gs= gs.compose(gs4);
//		GameSolver gameSolver1 = new GameSolver(gs, safety1, bdd);
//		GameSolution sol1 = gameSolver1.solve();
		
		GameSolution sol1 = GameSolver.solve(bdd, gs, safety);
		sol1.print();
		UtilityMethods.duration(t0, "central method ended in");
		sol1.strategyOfTheWinner().checkSafety(safety);
		
		//compositional method
		t0 = UtilityMethods.timeStamp();
//		GameSolver gameSolver2 = new GameSolver(gs1, safety1, bdd);
		GameSolution sol2 = GameSolver.solve(bdd, gs1, safety1);
		sol2.print();
		UtilityMethods.duration(t0, "fisrt game solved in");
		
		t0_2 = UtilityMethods.timeStamp();
//		GameSolver gameSolver3 = new GameSolver(gs2, safety2, bdd);
		GameSolution sol3 = GameSolver.solve(bdd, gs2, safety2);
		sol3.print();
		UtilityMethods.duration(t0_2, "second game solved in");
		
//		t0_2 = UtilityMethods.timeStamp();
////		GameSolver gameSolver3 = new GameSolver(gs2, safety2, bdd);
//		GameSolution sol4 = GameSolver.solve(bdd, gs3, safety3);
//		sol4.print();
//		UtilityMethods.duration(t0_2, "third game solved in");
		
		t0_2 = UtilityMethods.timeStamp();
//		GameSolver gameSolver3 = new GameSolver(gs2, safety2, bdd);
		GameSolution sol5 = GameSolver.solve(bdd, gs4, safety4);
		sol5.print();
		UtilityMethods.duration(t0_2, "fourth game solved in");
		
		t0_2 = UtilityMethods.timeStamp();
		//compose controllers
		int control1 = sol2.getController();
		int control2 = sol3.getController();
//		int control3 = sol4.getController();
		int control4 = sol5.getController();
		int control = BDDWrapper.and(bdd, control1, control2);
//		control = BDDWrapper.andTo(bdd, control, control3);
		control = BDDWrapper.andTo(bdd, control, control4);
		
		Variable[] composedVars = Variable.unionVariables(gs1.variables, gs2.variables);
//		composedVars = Variable.unionVariables(composedVars, gs3.variables);
		Variable[] primedComposedVars = Variable.getPrimedCopy(composedVars);
		Variable[] composedActionVars = Variable.unionVariables(gs1.sysActionVars, gs2.sysActionVars);
//		composedActionVars = Variable.unionVariables(composedActionVars, gs3.sysActionVars);
		int gameInit = BDDWrapper.and(bdd, gs1.getInit(), gs2.getInit());
//		gameInit = BDDWrapper.andTo(bdd, gameInit, gs3.getInit());
		
		int composedT_env = BDDWrapper.and(bdd, gs1.getEnvironmentTransitionRelation(), gs2.getEnvironmentTransitionRelation());
		
//		|| control != control3
		while(control != control1 || control != control2  || control != control4){
			
			if(control != control1){
				//restrict the game by control
				GameStructure newGS1 = new GameStructure(bdd, composedVars, primedComposedVars, gameInit,
						composedT_env, 
						BDDWrapper.and(bdd, gs1.getSystemTransitionRelation(), control), 
						gs1.envActionVars, composedActionVars);
				newGS1.setID("new game 1");
				newGS1.removeUnreachableStates().toGameGraph().draw("newGame1.dot", 1, 0);
				System.out.println("new game 1 is formed");
				sol2 = GameSolver.solve(bdd, newGS1, bdd.getOne());
				System.out.println("new game 1 is solved");
				sol2.print();
				sol2.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("cs.dot", 1,0);
				control1 = sol2.getController();
			}
			
			if(control != control2){
				//restrict the game by control
				GameStructure newGS2 = new GameStructure(bdd, composedVars, primedComposedVars, gameInit,
						composedT_env, 
						BDDWrapper.and(bdd, gs2.getSystemTransitionRelation(), control), 
						gs2.envActionVars, composedActionVars);
				newGS2.setID("new game 2");
				newGS2.removeUnreachableStates().toGameGraph().draw("newGame2.dot", 1, 0);
				sol3 = GameSolver.solve(bdd, newGS2, bdd.getOne());
				System.out.println("new game 2 is solved");
				sol3.print();
				control2 = sol3.getController();
			}
			
//			if(control != control3){
//				//restrict the game by control
//				GameStructure newGS3 = new GameStructure(bdd, composedVars, primedComposedVars, gameInit,
//						gs3.getEnvironmentTransitionRelation(), 
//						BDDWrapper.and(bdd, gs3.getSystemTransitionRelation(), control), 
//						gs3.envActionVars, composedActionVars);
//				newGS3.setID("new game 3");
////				newGS3.printGame();
////				newGS3.removeUnreachableStates().toGameGraph().draw("newGS3.dot", 1, 0);
//				sol4 = GameSolver.solve(bdd, newGS3, bdd.getOne());
//				System.out.println("new game 3 is solved");
//				sol4.print();
//				control3 = sol4.getController();
//			}
			
			if(control != control4){
				//restrict the game by control
				GameStructure newGS4 = new GameStructure(bdd, composedVars, primedComposedVars, gameInit,
						composedT_env, 
						BDDWrapper.and(bdd, gs4.getSystemTransitionRelation(), control), 
						gs4.envActionVars, composedActionVars);
				newGS4.setID("new game 4");
				sol5 = GameSolver.solve(bdd, newGS4, bdd.getOne());
				System.out.println("new game 4 is solved");
				sol5.print();
				control4 = sol5.getController();
			}
			
			control = BDDWrapper.and(bdd, control1, control2);
//			control = BDDWrapper.andTo(bdd, control, control3);
			control = BDDWrapper.andTo(bdd, control, control4);
		}
		
		UtilityMethods.duration(t0, "compositional method ended in");
		GameStructure sol = sol2.strategyOfTheWinner().compose(sol3.strategyOfTheWinner());
//		sol=sol.compose(sol4.strategyOfTheWinner());
		sol=sol.compose(sol5.strategyOfTheWinner());
		sol.checkSafety(safety);
		
	}
	
	public static void main(String[] args){
		test(1);
	}
	
	/**
	 * Creates a simple robot that can move around in a grid-world with four actions: left, right, up and down
	 * @param bdd
	 * @param dim
	 * @param name
	 * @param type
	 * @param init
	 * @return
	 */
	public static Agent2D createSimpleRobot(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
//		Variable[] vars = Variable.unionVariables(xVars, yVars);
		Variable[] actionVars = Variable.createVariables(bdd, 2, name);
		
		
//		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
//		
//		//define transition relation
//		int trans = bdd.ref(bdd.getZero());
//		
//		int upAct = BDDWrapper.assign(bdd, 0, actionVars);
//		int upTransitions = UtilityTransitionRelations.upTransitions(bdd, dim, xVars, yVars, upAct);
//		trans=bdd.orTo(trans, upTransitions);
//		
//		int downAct = BDDWrapper.assign(bdd, 1, actionVars);
//		int downTransitions = UtilityTransitionRelations.downTransitions(bdd, dim, xVars, yVars, downAct);
//		trans=bdd.orTo(trans, downTransitions);
//		
//		int leftAct = BDDWrapper.assign(bdd, 2, actionVars);
//		int leftTransitions = UtilityTransitionRelations.leftTransitions(bdd, dim, xVars, yVars, leftAct);
//		trans=bdd.orTo(trans, leftTransitions);
//		
//		int rightAct = BDDWrapper.assign(bdd, 3, actionVars);
//		int rightTransitions = UtilityTransitionRelations.rightTransitions(bdd, dim, xVars, yVars, rightAct);
//		trans=bdd.orTo(trans, rightTransitions);
//		
//		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
//		return robot;
		
		return createSimpleRobot(bdd, dim, name, type, initCell, xVars, yVars, actionVars);
	}
	
	public static Agent2D createSimpleRobot(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		
		//define transition relation
		int trans = bdd.ref(bdd.getZero());
		
		int upAct = BDDWrapper.assign(bdd, 0, actionVars);
		int upTransitions = UtilityTransitionRelations.upTransitions(bdd, dim, xVars, yVars, upAct);
		trans=bdd.orTo(trans, upTransitions);
		
		int downAct = BDDWrapper.assign(bdd, 1, actionVars);
		int downTransitions = UtilityTransitionRelations.downTransitions(bdd, dim, xVars, yVars, downAct);
		trans=bdd.orTo(trans, downTransitions);
		
		int leftAct = BDDWrapper.assign(bdd, 2, actionVars);
		int leftTransitions = UtilityTransitionRelations.leftTransitions(bdd, dim, xVars, yVars, leftAct);
		trans=bdd.orTo(trans, leftTransitions);
		
		int rightAct = BDDWrapper.assign(bdd, 3, actionVars);
		int rightTransitions = UtilityTransitionRelations.rightTransitions(bdd, dim, xVars, yVars, rightAct);
		trans=bdd.orTo(trans, rightTransitions);
		
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	
}
