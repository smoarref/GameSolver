package testsAndCaseStudies;

import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Variable;

import java.util.ArrayList;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class StrategyPruningCompositionalTest {
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000, 1000);
		int dim = 15;
		
		Agent2D robot1 = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R1", AgentType.Uncontrollable, new GridCell2D(0, 0));
		Agent2D robot2 = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R2", AgentType.Controllable, new GridCell2D(dim-1, dim-1));
		Agent2D robot3 = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R3", AgentType.Controllable, new GridCell2D(0, dim-1));
		Agent2D robot4 = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R4", AgentType.Controllable, new GridCell2D(dim-1, 0));
//		Agent2D robot5 = UtilityTransitionRelations.createSimpleRobot(bdd, dim, "R5", AgentType.Controllable, new GridCell2D(0, dim-1));
		
		
		//central
		System.out.println("central method");
		long t0_central = UtilityMethods.timeStamp();
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(robot1);
		agents.add(robot2);
		agents.add(robot3);
		agents.add(robot4);
//		agents.add(robot5);
		
		long t0 = UtilityMethods.timeStamp();
		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, agents);
		UtilityMethods.duration(t0, "constructing the central game structure in ");
		
		t0 = UtilityMethods.timeStamp();
		int objective = bdd.getOne();
		for(int i=0; i< agents.size(); i++){
			Agent2D robot_i = agents.get(i);
			for(int j=i+1; j<agents.size(); j++){
//				if(i==1 && j==3) continue;
				Agent2D robot_j = agents.get(j);
				int objective_i = UtilityFormulas.noCollisionObjective(bdd, robot_i.getXVars(), robot_i.getYVars(), 
						robot_j.getXVars(), robot_j.getYVars());
				objective = BDDWrapper.andTo(bdd, objective, objective_i);
				bdd.deref(objective_i);
			}
		}
		UtilityMethods.duration(t0, "objective was computed in ");

//		t0 = UtilityMethods.timeStamp();
//		GameSolution sol = GameSolver.solve(bdd, gameStructure, objective);
//		sol.print();
//		UtilityMethods.duration(t0, "game was solved in ");
//		
//		UtilityMethods.duration(t0_central, "the whole central method took ");
		
		//compositional method
		System.out.println("trying compositional method");
		long t0_comp = UtilityMethods.timeStamp();
		
		//game structures
		GameStructure gs1 = GameStructure.createGameForAgents(bdd, robot1, robot2);
		GameStructure gs2 = GameStructure.createGameForAgents(bdd, robot1, robot3);
		GameStructure gs3 = GameStructure.createGameForAgents(bdd, robot1, robot4);
		
		//compositions 
		t0 = UtilityMethods.timeStamp();
		GameStructure gs12 = gs1.compose(gs2);
		GameStructure gs13 = gs1.compose(gs3);
		GameStructure gs23 = gs2.compose(gs3);
		UtilityMethods.duration(t0, "composite game structures in ");
		
		//objectives
		t0 = UtilityMethods.timeStamp();
		int safety12 = UtilityFormulas.noCollisionObjective(bdd, robot1.getXVars(), robot1.getYVars(), 
				robot2.getXVars(), robot2.getYVars());
		int safety13 = UtilityFormulas.noCollisionObjective(bdd, robot1.getXVars(), robot1.getYVars(), 
				robot3.getXVars(), robot3.getYVars());
		int safety14 = UtilityFormulas.noCollisionObjective(bdd, robot1.getXVars(), robot1.getYVars(), 
				robot4.getXVars(), robot4.getYVars());
		
		int safety23 = UtilityFormulas.noCollisionObjective(bdd, robot2.getXVars(), robot2.getYVars(), 
				robot3.getXVars(), robot3.getYVars());
		int safety24 = UtilityFormulas.noCollisionObjective(bdd, robot2.getXVars(), robot2.getYVars(), 
				robot4.getXVars(), robot4.getYVars());
		int safety34 = UtilityFormulas.noCollisionObjective(bdd, robot3.getXVars(), robot3.getYVars(), 
				robot4.getXVars(), robot4.getYVars());
		
		int objective12 = bdd.getOne();
		objective12 = bdd.andTo(objective12, safety12);
		objective12 = bdd.andTo(objective12, safety13);
		objective12 = bdd.andTo(objective12, safety23);
		
		int objective13 = bdd.getOne();
		objective13 = bdd.andTo(objective13, safety12);
		objective13 = bdd.andTo(objective13, safety14);
		objective13 = bdd.andTo(objective13, safety24);
		
		int objective23 = bdd.getOne();
		objective23 = bdd.andTo(objective23, safety13);
		objective23 = bdd.andTo(objective23, safety14);
		objective23 = bdd.andTo(objective23, safety34);
		UtilityMethods.duration(t0, "objectives was computed in ");
		
//		t0 = UtilityMethods.timeStamp();
//		GameSolution sol12 = GameSolver.solve(bdd, gs12, objective12);
//		System.out.println("first game solved");
//		GameSolution sol13 = GameSolver.solve(bdd, gs13, objective13);
//		System.out.println("second game solved");
//		GameSolution sol23 = GameSolver.solve(bdd, gs23, objective23);
//		System.out.println("third game solved");
//		t0 = UtilityMethods.timeStamp();
//		UtilityMethods.duration(t0, "initial games were solved in ");
//		
//		int control12 = sol12.getController();
//		int control13 = sol13.getController();
//		int control23 = sol23.getController();
		
		int robot2ActionsAndVarsCube = BDDWrapper.createCube(bdd, Variable.unionVariables(robot2.getVariables(), robot2.getActionVars()));
		int robot3ActionsAndVarsCube = BDDWrapper.createCube(bdd, Variable.unionVariables(robot3.getVariables(), robot3.getActionVars()));
		int robot4ActionsAndVarsCube = BDDWrapper.createCube(bdd, Variable.unionVariables(robot4.getVariables(), robot4.getActionVars()));
		
		int control12;
		int control13;
		int control23;
		
		int projectedController12;
		int projectedController13;
		int projectedController23;
		
		do{
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			GameSolution sol12 = GameSolver.solve(bdd, gs12, objective12);
			System.out.println("first game solved");
			GameSolution sol13 = GameSolver.solve(bdd, gs13, objective13);
			System.out.println("second game solved");
			GameSolution sol23 = GameSolver.solve(bdd, gs23, objective23);
			System.out.println("third game solved");
			t0 = UtilityMethods.timeStamp();
			UtilityMethods.duration(t0, "initial games were solved in ");
			
			control12 = sol12.getController();
			control13 = sol13.getController();
			control23 = sol23.getController();
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			int composedController = BDDWrapper.and(bdd, control12, control13);
			composedController = BDDWrapper.andTo(bdd, composedController, control23);
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("controllersprojected ");
			t0=UtilityMethods.timeStamp();
			projectedController12 = BDDWrapper.exists(bdd, composedController, robot4ActionsAndVarsCube);
			projectedController13 = BDDWrapper.exists(bdd, composedController, robot3ActionsAndVarsCube);
			projectedController23 = BDDWrapper.exists(bdd, composedController, robot2ActionsAndVarsCube);
			UtilityMethods.duration(t0, "controllers projected in ");
			
			if(projectedController12 == control12 && projectedController13 == control13 && 
					projectedController23 == control23){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			gs12 = gs12.composeWithController(projectedController12);
			gs13 = gs13.composeWithController(projectedController13);
			gs23 = gs23.composeWithController(projectedController23);
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		
		
		
		//project controllers to their agents
//		int control12_1 = BDDWrapper.exists(bdd, control12, robot2ActionsAndVarsCube);
//		int control12_2 = BDDWrapper.exists(bdd, control12, robot3ActionsAndVarsCube);
		
//		int control13_1 = BDDWrapper.exists(bdd, control13, robot2ActionsAndVarsCube);
//		int control13_3 = BDDWrapper.exists(bdd, control13, robot4ActionsAndVarsCube);
		
//		int control23_2 = BDDWrapper.exists(bdd, control23, robot3ActionsAndVarsCube);
//		int control23_3 = BDDWrapper.exists(bdd, control23, robot4ActionsAndVarsCube);
		
//		int restrict12 = BDDWrapper.and(bdd, control13_1, control23_2);
//		gs12 = gs12.composeWithController(restrict12);
		
//		gs12 = gs12.composeWithController(control23_2);
		
//		int restrict13 = BDDWrapper.and(bdd, control12_1, control23_3);
//		gs13 = gs13.composeWithController(restrict13);
		
//		int restrict23 = BDDWrapper.and(bdd, control12_2, control13_3);
//		gs23 = gs23.composeWithController(restrict23);
		
//		gs23 = gs23.composeWithController(control12_2);
		
//		t0 = UtilityMethods.timeStamp();
//		sol12 = GameSolver.solve(bdd, gs12, objective12);
//		System.out.println("first game solved");
////		sol13 = GameSolver.solve(bdd, gs13, objective13);
////		System.out.println("second game solved");
//		sol23 = GameSolver.solve(bdd, gs23, objective23);
//		System.out.println("third game solved");
//		UtilityMethods.duration(t0, "second round of games were solved in ");
//		
//		int newcontrol12 = sol12.getController();
////		int newcontrol13 = sol13.getController();
//		int newcontrol23 = sol23.getController();
		
//		if(newcontrol12 == control12){
//			System.out.println("new and old control12 are the same");
//		}
//		
////		if(newcontrol13 == control13){
////			System.out.println("new and old control13 are the same");
////		}
//		
//		if(newcontrol23 == control23){
//			System.out.println("new and old control23 are the same");
//		}
		
//		int oldController12 = bdd.getOne();
//		int oldController13 = bdd.getOne();
//		int oldController23 = bdd.getOne();
//		
//		while(oldController12 != control12 || oldController13 != control13 || oldController23 != control23){
//			
//			//project controllers to agents
//			int control12_1 = BDDWrapper.exists(bdd, control12, robot3ActionsAndVarsCube);
//			int control12_2 = BDDWrapper.exists(bdd, control12, robot2ActionsAndVarsCube);
//			
//			int control13_1 = BDDWrapper.exists(bdd, control13, robot4ActionsAndVarsCube);
//			int control13_3 = BDDWrapper.exists(bdd, control13, robot2ActionsAndVarsCube);
//			
//			int control23_2 = BDDWrapper.exists(bdd, control23, robot4ActionsAndVarsCube);
//			int control23_3 = BDDWrapper.exists(bdd, control23, robot3ActionsAndVarsCube);
//			
//			//restrict games
//			int restrict12 = BDDWrapper.and(bdd, control13_1, control23_2);
//			gs12 = gs12.composeWithController(restrict12);
//			
//			int restrict13 = BDDWrapper.and(bdd, control12_1, control23_3);
//			gs13 = gs13.composeWithController(restrict13);
//			
//			
//			int restrict23 = BDDWrapper.and(bdd, control12_2, control13_3);
//			gs23 = gs23.composeWithController(restrict23);
//			
////			gs12 = gs12.composeWithController(control23_2);
////			gs23 = gs23.composeWithController(control12_2);
//			
//			//solve new games
//			t0 = UtilityMethods.timeStamp();
//			sol12 = GameSolver.solve(bdd, gs12, objective12);
//			System.out.println("first game solved");
//			sol13 = GameSolver.solve(bdd, gs13, objective13);
//			System.out.println("second game solved");
//			sol23 = GameSolver.solve(bdd, gs23, objective23);
//			System.out.println("third game solved");
//			UtilityMethods.duration(t0, "second round of games were solved in ");
//			
//			oldController12 = control12;
//			oldController13 = control13;
//			oldController23 = control23;
//			
//			control12 = sol12.getController();
//			control13 = sol13.getController();
//			control23 = sol23.getController();
//			
//			if(control12 == oldController12){
//				System.out.println("new and old control12 are the same");
//			}
//			
//			if(control13 == oldController13){
//				System.out.println("new and old control13 are the same");
//			}
//			
//			if(control23 == oldController23){
//				System.out.println("new and old control23 are the same");
//			}
//		}
//		
		UtilityMethods.duration(t0_comp, "the whole compositional took ");
		
//		int compositeController = BDDWrapper.and(bdd, control12, control13);
//		compositeController = BDDWrapper.andTo(bdd, compositeController, control23);
//		GameStructure restrictedGame = gameStructure.composeWithController(compositeController);
//		if(restrictedGame.safetyModelCheck(objective)){
//			System.out.println("passed the safety check!");
//		}
		
		
		
	}

}
