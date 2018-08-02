package testsAndCaseStudies;

import game.BDDWrapper;
import game.Controller;
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

public class CrossingTheStreetsCaseStudy extends CaseStudy2D{

	public CrossingTheStreetsCaseStudy(BDD argBDD, int argXDim, int argYDim,
			ArrayList<Agent2D> argAgents, GameStructure argGame,
			ArrayList<GridCell2D> argStatic) {
		super(argBDD, argXDim, argYDim, argAgents, argGame, argStatic);
		// TODO Auto-generated constructor stub
	}
	
	public static CaseStudy2D createSimpleCrossingTheStreetsCaseStudy(int dim){
		BDD bdd = new BDD(10000,1000);
		
		int numOfXBits = UtilityMethods.numOfBits(dim);
		int numOfYBits = UtilityMethods.numOfBits(dim);
		
		//location of the static obstacles
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		
		//define the variables in interleaving order
		int numOfAgents = 2;
		String agentsNamePrefix="R";
		
		Variable[] agentsActionVars = UtilityMethods.createVariablesForAgentsWithInterleavingOrder(bdd, 4, agentsNamePrefix, "act", 2);
		Variable[] agentsXvars = UtilityMethods.createVariablesAndTheirPrimedCopyForAgentsWithInterleavingOrder(bdd, numOfAgents, agentsNamePrefix, "X", numOfXBits);
		Variable[] agentsYvars = UtilityMethods.createVariablesAndTheirPrimedCopyForAgentsWithInterleavingOrder(bdd, numOfAgents, agentsNamePrefix, "Y", numOfYBits);
		
		//robot
		Variable[] agent1actionVars = Variable.getVariablesStartingWith(agentsActionVars, agentsNamePrefix+"0_act");
		Variable[] agent1Xvars = Variable.getVariablesStartingWith(agentsXvars, agentsNamePrefix+"0_X");
		Variable[] agent1Yvars = Variable.getVariablesStartingWith(agentsYvars, agentsNamePrefix+"0_Y");
		
		Variable.printVariables(agent1Xvars);
		Variable.printVariables(agent1Yvars);
		Variable.printVariables(agent1actionVars);
		
		//number of dynamic obstacles
		int numOfDynamicObstacles = numOfAgents-1;
		
		//init grid cells
		GridCell2D robotInitCell = new GridCell2D(0, 0);
//		GridCell2D dynamicObstacle1InitCell = new GridCell2D(dim/2, 1);
//		GridCell2D dynamicObstacle2InitCell = new GridCell2D(0, 3);
		
		GridCell2D[] dynamicObstaclesInitCells = new GridCell2D[numOfDynamicObstacles];
		for(int i=0; i<dynamicObstaclesInitCells.length; i++){
			dynamicObstaclesInitCells[i] = new GridCell2D(dim, 2*i+1);
		}
		
		//define agents 
//		Agent2D robot = UtilityTransitionRelations.createSimpleRobotMovingLeftAndRightWithStop(bdd, dim, "R", AgentType.Controllable, robotInitCell);
		Agent2D robot = UtilityTransitionRelations.createSimpleRobotMovingLeftAndRightWithStop(bdd, dim, "R0", AgentType.Controllable, robotInitCell, agent1Xvars, agent1Yvars, agent1actionVars);
		
//		Agent2D dynamicObstacle1 = UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, dim, "O1", AgentType.Uncontrollable, dynamicObstacle1InitCell);
//		
//		Agent2D dynamicObstacle2 = UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, dim, "O2", AgentType.Uncontrollable, dynamicObstacle2InitCell);
		
		ArrayList<Agent2D> dynamicObstacles = new ArrayList<Agent2D>();
		for(int i=1; i<=dynamicObstaclesInitCells.length; i++ ){
			Variable[] agent_i_actionVars = Variable.getVariablesStartingWith(agentsActionVars, agentsNamePrefix+i+"_act");
			Variable[] agent_i_Xvars = Variable.getVariablesStartingWith(agentsXvars, agentsNamePrefix+i+"_X");
			Variable[] agent_i_Yvars = Variable.getVariablesStartingWith(agentsYvars, agentsNamePrefix+i+"_Y");
			
			Variable.printVariables(agent_i_Xvars);
			Variable.printVariables(agent_i_Yvars);
			Variable.printVariables(agent_i_actionVars);
			
			Agent2D dynamicObs =  UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, dim, "R"+i, AgentType.Uncontrollable, dynamicObstaclesInitCells[i-1], agent_i_Xvars, agent_i_Yvars, agent_i_actionVars);
			
//			Agent2D dynamicObs =  UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, dim, "O"+i, AgentType.Uncontrollable, dynamicObstaclesInitCells[i]);
			
			dynamicObstacles.add(dynamicObs);
		}
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.addAll(dynamicObstacles);
		agents.add(robot);
		
		
		
		//form the game structure
//		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, dynamicObstacle1, robot);
//		GameStructure gameStructure2 = GameStructure.createGameForAgents(bdd, dynamicObstacle2, robot);
//		gameStructure = gameStructure.compose(gameStructure2);
		
		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, agents);
		
		//define concrete init
		//define concrete objectives 
		int concreteInit = bdd.ref(robot.getInit());
		int concreteSafetyObjective = bdd.ref(bdd.getOne());
		for(int i=0; i<dynamicObstacles.size(); i++){
			Agent2D currentDynamicObstacle = dynamicObstacles.get(i);
			int init_i = currentDynamicObstacle.getInit();
			concreteInit = BDDWrapper.andTo(bdd, concreteInit, init_i);
			int safety_i = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(), 
					currentDynamicObstacle.getXVars(), currentDynamicObstacle.getYVars());
			concreteSafetyObjective = BDDWrapper.andTo(bdd, concreteSafetyObjective, safety_i);
			BDDWrapper.free(bdd, safety_i);
		}
		
		int concreteReachabilityObjective = BDDWrapper.assign(bdd, dim, robot.getYVars());
		
		//form and solve the game centrally
//		GameSolution solution = GameSolver.solveForReachabilyObjectives(bdd, gameStructure, concreteReachabilityObjective, concreteSafetyObjective);
//		solution.print();
//		Simulator2D simulator = new Simulator2D(bdd, agents, solution.strategyOfTheWinner(), concreteInit);
//		CaseStudy2D result = new CaseStudy2D(bdd, dim, dim, agents, gameStructure, staticObstacles, simulator);
		
		
		//define parametric controllers
			
		//define c1 - horizontal movement
//		Variable[] px = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfXBits, "px");
//		int parametricInit1 = BDDWrapper.same(bdd, robot.getXVars(), px);
//		// eventually (x = px + 1)  
//		int parametricReachabilityObjective1 = UtilityTransitionRelations.addOne(bdd, px, robot.getXVars());
//		int boundCheckingX = BDDWrapper.assign(bdd, dim, px);
//		int parametricSafetyObjective1 = BDDWrapper.and(bdd, concreteSafetyObjective, bdd.not(boundCheckingX));
//		CoSafeControllerInterface controlInterface1 = new CoSafeControllerInterface(parametricInit1, parametricSafetyObjective1, parametricReachabilityObjective1);
//		ParametricCoSafeController c1 = GameSolver.synthesizeParamericCoSafeController(bdd, gameStructure, px, controlInterface1);
		
//		UtilityMethods.debugBDDMethods(bdd, "c1's init", c1.getInit());
		
		//define c2 - vertical movement
		Variable[] py = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfYBits, "py");
		int parametricInit2 = BDDWrapper.same(bdd, robot.getYVars(), py);
		
		int obstaclesYinit = bdd.ref(bdd.getOne());
		for(int i=0; i<dynamicObstaclesInitCells.length; i++){
			int yInit = BDDWrapper.assign(bdd, 2*i+1, dynamicObstacles.get(i).getYVars());
			obstaclesYinit = BDDWrapper.andTo(bdd, obstaclesYinit, yInit);
			BDDWrapper.free(bdd, obstaclesYinit);
		}
		parametricInit2 = BDDWrapper.andTo(bdd, parametricInit2, obstaclesYinit);
				
		int parametricReachabilityObjective2 = UtilityTransitionRelations.addOne(bdd, py, robot.getYVars());
		int boundCheckingY = BDDWrapper.assign(bdd, dim, py);
		int parametricSafetyObjective2 = BDDWrapper.and(bdd, concreteSafetyObjective, bdd.not(boundCheckingY));
		CoSafeControllerInterface controlInterface2 = new CoSafeControllerInterface(bdd, parametricInit2, parametricSafetyObjective2, parametricReachabilityObjective2);
		ParametricCoSafeController c2 = GameSolver.synthesizeParamericCoSafeController(bdd, gameStructure, py, controlInterface2);
		
		gameStructure.printGameVars();
//		UtilityMethods.debugBDDMethods(bdd, "c2 control transition relation is", c2.getTransitionRelation());
//		UtilityMethods.debugBDDMethods(bdd, "c2's init", c2.getInit());
		
		//form the control game
		
		ArrayList<Variable[]> parameters = new ArrayList<Variable[]>();
//		parameters.add(px);
		parameters.add(py);
		
		ArrayList<CoSafeControllerInterface> controllerInterfaces = new ArrayList<CoSafeControllerInterface>();
//		controllerInterfaces.add(controlInterface1);
//		controllerInterfaces.add(controlInterface2);
		
		controllerInterfaces.add(c2.getControllerInterface());
		
		
//		System.out.println("printing controller interfaces");
//		controlInterface2.print(bdd);
//		c2.getControllerInterface().print(bdd);
//		
//		int parameterValue = BDDWrapper.assign(bdd, 0, py);
//		Controller cc = c2.instantiateParametricController(parameterValue);
//		UtilityMethods.debugBDDMethods(bdd, "instantiated controller is ", cc.getTransitionRelation());
		
		ControlGameStructure cgsAll = new ControlGameStructure(bdd, gameStructure.variables, parameters, controllerInterfaces);
		
//		UtilityMethods.debugBDDMethods(bdd, "control game", cgsAll.getControlTransitionRelation());
		
//		int obj = BDDWrapper.and(bdd, concreteSafetyObjective, concreteReachabilityObjective);
//		int pre1 = cgsAll.controllablePredecessor(obj);
//		UtilityMethods.debugBDDMethods(bdd, "cpre of obj", pre1);
//		UtilityMethods.getUserInput();
//		int pre2 = cgsAll.controllablePredecessor(pre1);
//		UtilityMethods.debugBDDMethods(bdd, "cpre of pre1", pre2);
//		UtilityMethods.getUserInput();
//		int pre3 = cgsAll.controllablePredecessor(pre2);
//		UtilityMethods.debugBDDMethods(bdd, "cpre of obj", pre3);
//		UtilityMethods.getUserInput();
		
		ControlGameSolution sol = ControlGameSolver.solveForReachabilyObjectives(bdd, cgsAll, concreteInit, 
				concreteReachabilityObjective, concreteSafetyObjective);
		sol.print();
		
		//simulator 
		ArrayList<ParametricCoSafeController> parametricControllers = new ArrayList<ParametricCoSafeController>();
//		parametricControllers.add(c1);
		parametricControllers.add(c2);
		ControlGameSimulator contGameSim = new ControlGameSimulator(bdd, gameStructure, agents, cgsAll, sol.getController(), parametricControllers, concreteInit);
		
		//form case study
		
		CaseStudy2D result = new CaseStudy2D(bdd, dim, dim, agents, gameStructure, staticObstacles, contGameSim);
		return result;
	}
	
}
