package testsAndCaseStudies;

import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.PartiallyObservableGameSimulator2D;
import game.TurnBasedPartiallyObservableGameStructure;
import game.Variable;

import java.util.ArrayList;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class PartiallyObservableGameCaseStudy extends CaseStudy2D {

	public PartiallyObservableGameCaseStudy(BDD argBDD, int argXDim,
			int argYDim, ArrayList<Agent2D> argAgents, GameStructure argGame,
			ArrayList<GridCell2D> argStatic) {
		super(argBDD, argXDim, argYDim, argAgents, argGame, argStatic);
		// TODO Auto-generated constructor stub
	}
	
	public static CaseStudy2D createPartiallyObservableCaseStudy(int dim){
		BDD bdd = new BDD(10000,1000);
		
		long t0_all = UtilityMethods.timeStamp();
		long t0 =UtilityMethods.timeStamp();
		System.out.println("creating agents and perfect information game structure");
		Agent2D controlledRobot = UtilityTransitionRelations.createSimpleRobot(bdd, dim-1, "R1", 
				AgentType.Controllable, new GridCell2D(0, 0));
		Agent2D uncontrolledRobot =  UtilityTransitionRelations.createSimpleRobot(bdd, dim-1, "R2", 
				AgentType.Uncontrollable, new GridCell2D(dim-1, dim-1));
		
		GameStructure gs = GameStructure.createGameForAgents(bdd, uncontrolledRobot, controlledRobot);
//		gs.printGame();
		UtilityMethods.duration(t0_all, "perfect information game structure was created in ");
		
//		gs.removeUnreachableStates().toGameGraph().draw("robotGame.dot", 1,0);
		
		Variable[] observables =  Variable.createVariables(bdd, gs.variables.length, "obsVars");
		
		System.out.println();
		System.out.println("Creating the observation map");
		t0 = UtilityMethods.timeStamp();
		int observationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
				dim, uncontrolledRobot, controlledRobot, observables);
		UtilityMethods.duration(t0, "observation map was created in ");
		
		//define observation map
//		UtilityMethods.debugBDDMethods(bdd, "observation map is", observationMap);
//		UtilityMethods.getUserInput();
		
		//define partially observable game structure
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
//				gs.variables, gs.getEnvironmentTransitionRelation(), gs.getSystemTransitionRelation(), 
//				gs.actionVars, gs.actionVars, observables, observationMap);
		
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, gs, observables, observationMap);
		
		//2) call the subset construction procedure
		System.out.println();
		System.out.println("constructing the knowledge game");
		t0 = UtilityMethods.timeStamp();
		GameStructure kGS = pogs.createKnowledgeGame(gs.getInit());
//		kGS.printGame();
//		kGS.toGameGraph().draw("knowledgeGame.dot", 1, 0);
//		pogs.printObservationMap();
		
//		pogs.printPartiallyObservableGameToKnowledgeGameStatesMap();
		
		UtilityMethods.duration(t0, "subset constrcution done in ");		
		
		//define objective 
		System.out.println("");
		System.out.println("defining the objective");
		t0 = UtilityMethods.timeStamp();
		int phi = UtilityFormulas.noCollisionObjective(bdd, uncontrolledRobot.getXVars(), 
							uncontrolledRobot.getYVars(), controlledRobot.getXVars(), controlledRobot.getYVars());		
		UtilityMethods.duration(t0, "objective was define in");
		
		//translate to objective for knowledge game
		System.out.println();
		System.out.println("translating the objective to knowledge game level");
		t0 = UtilityMethods.timeStamp();
		int objective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(phi);
		UtilityMethods.duration(t0, "objective for knowledge game was defined in");
				
//		UtilityMethods.debugBDDMethods(bdd, "phi is", phi);
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
				
		System.out.println("\nSolving the knowledge game");
		t0 = UtilityMethods.timeStamp();
		GameSolution sol = GameSolver.solve(bdd, kGS, objective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in");
		
		System.out.println("\nrestrcting the knowledge game");
		pogs.restrictKnowledgeGame(sol.getController());
		
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(uncontrolledRobot);
		agents.add(controlledRobot);
		
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		
		PartiallyObservableGameSimulator2D simulator = new PartiallyObservableGameSimulator2D(bdd, agents, pogs, gs.getInit());
		
		CaseStudy2D result = new CaseStudy2D(bdd, dim, dim, agents, gs, staticObstacles, simulator);
		
		UtilityMethods.duration(t0_all, "the whole process took");
		return result;
	}
	
	public static void main(String[] args){
		CaseStudy2D c = PartiallyObservableGameCaseStudy.createPartiallyObservableCaseStudy(4);
	}

}
