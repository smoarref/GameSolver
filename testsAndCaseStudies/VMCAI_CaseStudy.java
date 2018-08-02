package testsAndCaseStudies;

import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Simulator2D;
import game.Variable;

import java.util.ArrayList;

import parametric.CoSafeControllerInterface;
import parametric.ControlGameSimulator;
import parametric.ControlGameSolution;
import parametric.ControlGameSolver;
import parametric.ControlGameStructure;
import parametric.ParametricCoSafeController;
import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

//TODO: Crashes when I make it two cars, why?

public class VMCAI_CaseStudy extends CaseStudy2D {

	public VMCAI_CaseStudy(BDD argBDD, int argXDim, int argYDim,
			ArrayList<Agent2D> argAgents, GameStructure argGame,
			ArrayList<GridCell2D> argStatic) {
		super(argBDD, argXDim, argYDim, argAgents, argGame, argStatic);
		// TODO Auto-generated constructor stub
	}
	
	public static CaseStudy2D createVMCAI_CaseStudy(int dim){
		BDD bdd = new BDD(10000, 1000);
		
		int[] specialRows = new int[]{0, dim/2, dim};
		int[] specialColumns = new int[]{0, dim/3, 2*dim/3, dim};
		
		//location of the static obstacles
		ArrayList<GridCell2D> staticObstacles = fillStaticObstacles(dim, specialRows, specialColumns);
		
		int numOfBits = UtilityMethods.numOfBits(dim);
		
		//define variables
		Variable[] egoCarXVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "egoCarX");
		Variable[] egoCarYVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "egoCarY");
		Variable[] egoCarVars=Variable.unionVariables(egoCarXVars, egoCarYVars);
		Variable[] egoCarActionVars = Variable.createVariables(bdd, 3, "egoCarAct");
		
		Variable[] car1XVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "car1X");
		Variable[] car1YVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "car1Y");
		Variable[] car1Vars = Variable.unionVariables(car1XVars, car1YVars);
		Variable[] car1ActionVars = Variable.createVariables(bdd, 2, "car1Act");
		
		Variable[] car2XVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "car2X");
		Variable[] car2YVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "car2Y");
		Variable[] car2Vars = Variable.unionVariables(car2XVars, car2YVars);
		Variable[] car2ActionVars = Variable.createVariables(bdd, 2, "car2Act");
		
		
		//define initial 
		GridCell2D egoCarInitCell = new GridCell2D(specialRows[1], 0);
		int egoCarInit = UtilityFormulas.assignCell(bdd, egoCarXVars, egoCarYVars, egoCarInitCell);
		
		GridCell2D car1InitCell = new GridCell2D(dim, specialColumns[1]);
		int car1Init = UtilityFormulas.assignCell(bdd, car1XVars,car1YVars, car1InitCell);
		
		GridCell2D car2InitCell = new GridCell2D(specialRows[1], 1);
		int car2Init = UtilityFormulas.assignCell(bdd, car2XVars,car2YVars, car2InitCell);
		
		//define agents
		//ego vehicle
		
		int egoCarTrans = VMCAI_CaseStudyTransitionRelation(bdd, dim, egoCarXVars, egoCarYVars, 
				egoCarActionVars, specialRows, specialColumns);
		//add action stop
		int stopAction = BDDWrapper.assign(bdd, 4, egoCarActionVars);
		int stopTrans = UtilityTransitionRelations.halt(bdd, egoCarVars, stopAction);
		egoCarTrans = BDDWrapper.orTo(bdd, egoCarTrans, stopTrans);
		BDDWrapper.free(bdd, stopAction);
		BDDWrapper.free(bdd, stopTrans);
		
//		UtilityMethods.debugBDDMethods(bdd, "ego car trans", egoCarTrans);
		
		Agent2D egoCar = new Agent2D(bdd, "egoCar", AgentType.Controllable, egoCarVars, egoCarActionVars, 
				egoCarTrans, egoCarInit, egoCarXVars, egoCarYVars);
		
		//car 1
		int car1Trans = VMCAI_CaseStudyTransitionRelation(bdd, dim, car1XVars, 
				car1YVars, car1ActionVars, specialRows, specialColumns);
		Agent2D car1 = new Agent2D(bdd, "car1", AgentType.Uncontrollable, 
				car1Vars, car1ActionVars, car1Trans, car1Init, car1XVars, car1YVars);
		
		//car 2
		int car2Trans = VMCAI_CaseStudyTransitionRelation(bdd, dim, car2XVars, 
						car2YVars, car2ActionVars, specialRows, specialColumns);
		Agent2D car2 = new Agent2D(bdd, "car2", AgentType.Uncontrollable, 
						car2Vars, car2ActionVars, car2Trans, car2Init, car2XVars, car2YVars);
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(egoCar);
		agents.add(car1);
		agents.add(car2);
		
		//create the game structure
		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, agents);
				
		//define global objectives 
		int concreteInit = BDDWrapper.and(bdd, egoCarInit, car1Init);
//		int concreteInit = BDDWrapper.and(bdd, egoCarInit, car2Init);
		concreteInit = BDDWrapper.andTo(bdd, concreteInit, car2Init);
		
		//safety
		int safety_1 = UtilityFormulas.noCollisionObjective(bdd, egoCar.getXVars(), egoCar.getYVars(), 
				car1.getXVars(), car1.getYVars());
		int safety_2 = UtilityFormulas.noCollisionObjective(bdd, egoCar.getXVars(), egoCar.getYVars(), 
				car2.getXVars(), car2.getYVars());
		
//		int concreteSafetyObjective = bdd.ref(safety_1);
		int concreteSafetyObjective = BDDWrapper.and(bdd, safety_1, safety_2); 
		BDDWrapper.free(bdd, safety_1);
		BDDWrapper.free(bdd, safety_2);
		
		//reachability
		int concreteReachabilityObjective = UtilityFormulas.assignCell(bdd, egoCar, new GridCell2D(specialRows[0], dim));
//		int concreteReachabilityObjective = UtilityFormulas.assignCell(bdd, egoCar, new GridCell2D(specialRows[0], specialColumns[1]));
		
//		//form and solve the game centrally
//		GameSolution solution = GameSolver.solveForReachabilyObjectives(bdd, gameStructure, concreteReachabilityObjective, concreteSafetyObjective);
//		solution.print();
//		Simulator2D simulator = new Simulator2D(bdd, agents, solution.strategyOfTheWinner(), concreteInit);
//		CaseStudy2D result = new CaseStudy2D(bdd, dim, dim, agents, gameStructure, staticObstacles, simulator);
		
		//define parametric controllers 
		Variable[] px = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "px");
		int parametricInit1 = BDDWrapper.same(bdd, egoCar.getXVars(), px);
		Variable[] py = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "py");
		int parametricInit2 = BDDWrapper.same(bdd, egoCar.getYVars(), py);
		
		Variable[] parameters = Variable.unionVariables(px, py);
		
		int parametricInit = BDDWrapper.and(bdd, parametricInit1, parametricInit2);
		
//		UtilityMethods.debugBDDMethods(bdd, "x <-> px is", parametricInit1);
//		UtilityMethods.debugBDDMethods(bdd, "y <-> py is", parametricInit2);
//		UtilityMethods.debugBDDMethods(bdd, "parametric init is", parametricInit);
//		UtilityMethods.getUserInput();
		
		int cube = BDDWrapper.createCube(bdd, car1Vars);
		cube = BDDWrapper.andTo(bdd, cube, BDDWrapper.createCube(bdd, car2Vars));
		cube = BDDWrapper.andTo(bdd, cube, BDDWrapper.createCube(bdd, parameters));
		cube = BDDWrapper.andTo(bdd, cube, BDDWrapper.createCube(bdd, Variable.getPrimedCopy(car1Vars)));
		cube = BDDWrapper.andTo(bdd, cube, BDDWrapper.createCube(bdd, Variable.getPrimedCopy(car2Vars)));
		
		int interestingCell = UtilityFormulas.assignCell(bdd, egoCar, new GridCell2D(specialRows[1], specialColumns[0]));
		
		System.out.println("Computing parametric controller 1");
		
		//ctrl1 : Move North 2 steps 
		int c1Steps = 1;
		int parametricReachabilityObjective1 = UtilityTransitionRelations.subtract(bdd, px, c1Steps, egoCar.getXVars());
		parametricReachabilityObjective1 = BDDWrapper.andTo(bdd, parametricReachabilityObjective1, parametricInit2);
		int boundCheckingX = UtilityFormulas.bound(bdd, px, dim-c1Steps+1, dim);
		int parametricSafetyObjective1 = BDDWrapper.and(bdd, concreteSafetyObjective, bdd.not(boundCheckingX));
		CoSafeControllerInterface controlInterface1 = new CoSafeControllerInterface(bdd, parametricInit, parametricSafetyObjective1, parametricReachabilityObjective1);
		ParametricCoSafeController c1 = GameSolver.synthesizeParamericCoSafeController(bdd, gameStructure, parameters, controlInterface1);
		
//		c1.getControllerInterface().print(bdd);
//		UtilityMethods.getUserInput();
		
//		int partialController1 = BDDWrapper.exists(bdd, c1.getTransitionRelation(), cube);
//		UtilityMethods.debugBDDMethods(bdd, "partial c1", partialController1);
//		UtilityMethods.getUserInput();
		
		System.out.println("Computing parametric controller 2");
		
//		UtilityMethods.debugBDDMethods(bdd, "parametric init is", parametricInit);
//		UtilityMethods.getUserInput();
		
		//ctrl2 : Move west 3 steps 
		int c2Steps=3;
		int parametricReachabilityObjective2 = UtilityTransitionRelations.add(bdd, py, c2Steps, egoCar.getYVars());
		parametricReachabilityObjective2 = BDDWrapper.andTo(bdd, parametricReachabilityObjective2, parametricInit1);
		int boundCheckingY = UtilityFormulas.bound(bdd, py, dim-c2Steps+1, dim);
		int parametricSafetyObjective2 = BDDWrapper.and(bdd, concreteSafetyObjective, bdd.not(boundCheckingY));
		CoSafeControllerInterface controlInterface2 = new CoSafeControllerInterface(bdd, parametricInit, parametricSafetyObjective2, parametricReachabilityObjective2);
		ParametricCoSafeController c2 = GameSolver.synthesizeParamericCoSafeController(bdd, gameStructure, parameters, controlInterface2);
		
//		c2.getControllerInterface().print(bdd);
//		UtilityMethods.getUserInput();
		
		//form the control game
		
		System.out.println("Forming the control game structure");
		
		ArrayList<Variable[]> params = new ArrayList<Variable[]>();
		params.add(parameters);
		params.add(parameters);
		
		ArrayList<CoSafeControllerInterface> controllerInterfaces = new ArrayList<CoSafeControllerInterface>();
		controllerInterfaces.add(c1.getControllerInterface());
		controllerInterfaces.add(c2.getControllerInterface());
		
		ControlGameStructure cgsAll = new ControlGameStructure(bdd, gameStructure.variables, params, controllerInterfaces);
		
//		UtilityMethods.debugBDDMethods(bdd, "control game", cgsAll.getControlTransitionRelation(),100);
//		UtilityMethods.getUserInput();
		

		
		int controlTrans = cgsAll.getControlTransitionRelation();
//		int partialControlTrans = BDDWrapper.and(bdd, controlTrans, interestingCell);
		int partialControlTrans = BDDWrapper.exists(bdd, controlTrans, cube);
		UtilityMethods.debugBDDMethods(bdd, "partial control trans", partialControlTrans);
		UtilityMethods.getUserInput();
		
		System.out.println("Solving the control game");
		ControlGameSolution sol = ControlGameSolver.solveForReachabilyObjectives(bdd, cgsAll, concreteInit, 
				concreteReachabilityObjective, concreteSafetyObjective);
		sol.print();
		
//		int partialWinningRegion = BDDWrapper.and(bdd, sol.getWinningSystemStates(), interestingCell);
//		UtilityMethods.debugBDDMethods(bdd, "partial winnig states are", partialWinningRegion);
		

		
//		int winningEgoCarStates = BDDWrapper.exists(bdd, sol.getWinningSystemStates(), cube);
//		UtilityMethods.debugBDDMethods(bdd, "wining ego car states", winningEgoCarStates);
//		UtilityMethods.getUserInput();
		
//		int partialControlStrat = BDDWrapper.and(bdd, sol.getController(), interestingCell);
//		UtilityMethods.debugBDDMethods(bdd, "partial control strategy is", partialControlStrat);
//		UtilityMethods.debugBDDMethods(bdd, "control strategy is", sol.getController());
//		UtilityMethods.getUserInput();
		
//		int winningEgoCarStatesControl = BDDWrapper.exists(bdd, sol.getController(), cube);
//		UtilityMethods.debugBDDMethods(bdd, "wining ego car states control", winningEgoCarStatesControl);
//		UtilityMethods.getUserInput();
		
		System.out.println("preparing the simulator");
		
		//simulator 
		ArrayList<ParametricCoSafeController> parametricControllers = new ArrayList<ParametricCoSafeController>();
		parametricControllers.add(c1);
		parametricControllers.add(c2);
		ControlGameSimulator contGameSim = new ControlGameSimulator(bdd, gameStructure, agents, cgsAll, sol.getController(), parametricControllers, concreteInit);
		
		CaseStudy2D result = new CaseStudy2D(bdd, dim, dim, agents, gameStructure, staticObstacles, contGameSim);
		
		return result;
	}
	
	public static  int VMCAI_CaseStudyTransitionRelation(BDD bdd, int dim, Variable[] xVars, Variable[] yVars, Variable[] actionVars, 
			int[] specialRows, int[] specialColumns){
		int up = BDDWrapper.assign(bdd, 0, actionVars);
		int down = BDDWrapper.assign(bdd, 1, actionVars);
		int left = BDDWrapper.assign(bdd, 2, actionVars);
		int right = BDDWrapper.assign(bdd, 3, actionVars);
		
		int row_0 = BDDWrapper.assign(bdd, specialRows[0], xVars);
		int row_middle = BDDWrapper.assign(bdd, specialRows[1], xVars);
		int row_last = BDDWrapper.assign(bdd, specialRows[2], xVars);
		
		int column_0 = BDDWrapper.assign(bdd, specialColumns[0], yVars);
		int column_oneThird = BDDWrapper.assign(bdd, specialColumns[1], yVars);
		int column_twoThird = BDDWrapper.assign(bdd, specialColumns[2], yVars);
		int column_last = BDDWrapper.assign(bdd, specialColumns[3], yVars);
		
		int upOK = BDDWrapper.or(bdd, column_oneThird, column_last);
		int downOK = BDDWrapper.or(bdd, column_0, column_twoThird);
		int leftOK = bdd.ref(row_0);
		int rightOK = BDDWrapper.or(bdd, row_middle, row_last);
		
		int upTransitions = UtilityTransitionRelations.upTransitions(bdd, dim, xVars, yVars, up);
		upTransitions = BDDWrapper.andTo(bdd, upTransitions, upOK);
		
		int downTransitions = UtilityTransitionRelations.downTransitions(bdd, dim, xVars, yVars, down);
		downTransitions = BDDWrapper.andTo(bdd, downTransitions, downOK);
		
		int leftTransitions = UtilityTransitionRelations.leftTransitions(bdd, dim, xVars, yVars, left);
		leftTransitions = BDDWrapper.andTo(bdd, leftTransitions, leftOK);
		
		int rightTransitions = UtilityTransitionRelations.rightTransitions(bdd, dim, xVars, yVars, right);
		rightTransitions = BDDWrapper.andTo(bdd, rightTransitions, rightOK);
		
		int T = BDDWrapper.or(bdd, upTransitions, downTransitions);
		T = BDDWrapper.orTo(bdd, T, leftTransitions);
		T=BDDWrapper.orTo(bdd, T, rightTransitions);
		
		BDDWrapper.free(bdd, up);
		BDDWrapper.free(bdd, down);
		BDDWrapper.free(bdd, left);
		BDDWrapper.free(bdd, right);
		BDDWrapper.free(bdd, row_0);
		BDDWrapper.free(bdd, row_middle);
		BDDWrapper.free(bdd, row_last);
		BDDWrapper.free(bdd, column_0);
		BDDWrapper.free(bdd, column_oneThird);
		BDDWrapper.free(bdd, column_twoThird);
		BDDWrapper.free(bdd, column_last);
		BDDWrapper.free(bdd, upOK);
		BDDWrapper.free(bdd, downOK);
		BDDWrapper.free(bdd, rightOK);
		BDDWrapper.free(bdd, leftOK);
		BDDWrapper.free(bdd, upTransitions);
		BDDWrapper.free(bdd, downTransitions);
		BDDWrapper.free(bdd, leftTransitions);
		BDDWrapper.free(bdd, rightTransitions);
		return T;
	}
	
	public static ArrayList<GridCell2D> fillStaticObstacles(int dim, int[] specialRows, int[] specialColumns){
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		for(int i=0; i<=dim ; i++){
			boolean specialRow = (i==specialRows[0]) || (i==specialRows[1]) || (i==specialRows[2]);
			for(int j=0; j<=dim; j++){
				boolean specialColumn = (j==specialColumns[0]) || (j==specialColumns[1]) || 
						(j==specialColumns[2]) || (j==specialColumns[3]);
				if(!specialRow && !specialColumn){
					staticObstacles.add(new GridCell2D(i, j));
				}
			}
		}
		return staticObstacles;
	}
	
}
