package testsAndCaseStudies;

import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.TurnBasedPartiallyObservableGameStructure;
import game.Variable;
import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class TestPartiallyObservableGame {

	public static void main(String[] args) {
//		BDD bdd = new BDD(10000, 1000);
//		int numOfCells = 3;
//		Agent2D controlledRobot = UtilityTransitionRelations.createSimpleRobot(bdd, numOfCells-1, "R1", 
//				AgentType.Controllable, new GridCell2D(0, 0));
//		Agent2D uncontrolledRobot =  UtilityTransitionRelations.createSimpleRobot(bdd, numOfCells-1, "R2", 
//				AgentType.Uncontrollable, new GridCell2D(numOfCells-1, numOfCells-1));
//		
//		GameStructure gs = GameStructure.createGameForAgents(bdd, uncontrolledRobot, controlledRobot);
//		gs.printGame();
//		
////		gs.removeUnreachableStates().toGameGraph().draw("robotGame.dot", 1,0);
//		
//		Variable[] observables =  Variable.createVariables(bdd, gs.variables.length, "obsVars");
//		
//		int observationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
//				numOfCells, uncontrolledRobot, controlledRobot, observables);
//		
//		//define observation map
////		UtilityMethods.debugBDDMethods(bdd, "observation map is", observationMap);
////		UtilityMethods.getUserInput();
//		
//		//define partially observable game structure
////		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
////				gs.variables, gs.getEnvironmentTransitionRelation(), gs.getSystemTransitionRelation(), 
////				gs.actionVars, gs.actionVars, observables, observationMap);
//		
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, gs, observables, observationMap);
//		
//		//2) call the subset construction procedure
//		System.out.println("constructing the knowledge game");
//		GameStructure kGS = pogs.createKnowledgeGame(gs.getInit());
////		kGS.printGame();
////		kGS.toGameGraph().draw("knowledgeGame.dot", 1, 0);
////		pogs.printObservationMap();
//				
//		//define objective 
//		int phi = UtilityFormulas.noCollisionObjective(bdd, uncontrolledRobot.getXVars(), 
//							uncontrolledRobot.getYVars(), controlledRobot.getXVars(), controlledRobot.getYVars());		
//				
//		//translate to objective for knowledge game
//		int objective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(phi);
//				
//		UtilityMethods.debugBDDMethods(bdd, "phi is", phi);
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
//				
//		GameSolution sol = GameSolver.solve(bdd, kGS, objective);
//		sol.print();
		
		testSimpleRobotsWithStayPut();
		
	}
	
	public static void testSimpleRobotsWithStayPut(){
		BDD bdd = new BDD(10000, 1000);
		int numOfCells = 4;
		Agent2D controlledRobot = UtilityTransitionRelations.createSimpleRobotWithStayPut(bdd, numOfCells-1, "R1", 
				AgentType.Controllable, new GridCell2D(0, 0));
		Agent2D uncontrolledRobot =  UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, numOfCells-1, "R2", 
				AgentType.Uncontrollable, new GridCell2D(numOfCells-1, numOfCells-2));
		
		GameStructure gs = GameStructure.createGameForAgents(bdd, uncontrolledRobot, controlledRobot);
		gs.printGame();
		
//		gs.removeUnreachableStates().toGameGraph().draw("robotGame.dot", 1,0);
		
		Variable[] observables =  Variable.createVariables(bdd, gs.variables.length, "obsVars");
		
		int observationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
				numOfCells, uncontrolledRobot, controlledRobot, observables);
		
		//define observation map
		UtilityMethods.debugBDDMethods(bdd, "observation map is", observationMap);
		UtilityMethods.getUserInput();
		
		//define partially observable game structure
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
//				gs.variables, gs.getEnvironmentTransitionRelation(), gs.getSystemTransitionRelation(), 
//				gs.actionVars, gs.actionVars, observables, observationMap);
		
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, gs, observables, observationMap);
		
		//2) call the subset construction procedure
		System.out.println("constructing the knowledge game");
		GameStructure kGS = pogs.createKnowledgeGame(gs.getInit());
//		kGS.printGame();
//		kGS.toGameGraph().draw("knowledgeGame.dot", 1, 0);
//		pogs.printObservationMap();
				
		//define objective 
		int phi = UtilityFormulas.noCollisionObjective(bdd, uncontrolledRobot.getXVars(), 
							uncontrolledRobot.getYVars(), controlledRobot.getXVars(), controlledRobot.getYVars());		
				
		//translate to objective for knowledge game
		int objective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(phi);
				
		UtilityMethods.debugBDDMethods(bdd, "phi is", phi);
		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
				
		GameSolution sol = GameSolver.solve(bdd, kGS, objective);
		sol.print();
	}

}
