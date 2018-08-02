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
import parametric.ParametricGameStructure;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import jdd.bdd.BDD;

public class TwoWayRoadCaseStudy extends CaseStudy2D {

	
	public TwoWayRoadCaseStudy(BDD argBDD, int argXDim, int argYDim,
			ArrayList<Agent2D> argAgents, GameStructure argGame,
			ArrayList<GridCell2D> argStatic) {
		super(argBDD, argXDim, argYDim, argAgents, argGame, argStatic);
		// TODO Auto-generated constructor stub
	}

	/**
	 * creates a xDim*yDim board where there is a two-way road in the middle.
	 * there are two cars, one controllable, the other one uncontrollable. 
	 * @param dim
	 * @return
	 */
	public static CaseStudy2D createSimpleTwoWayRoadCaseStudy(int xDim, int yDim){
		BDD bdd = new BDD(10000,1000);
		
		//location of the static obstacles
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		for(int i=0; i<=yDim ; i++){
			for(int j=0; j<=yDim; j++){
				if(j<yDim/2 || j>yDim/2+1){
					staticObstacles.add(new GridCell2D(i, j));
				}
			}
		}
		

		
		int numOfXBits = UtilityMethods.numOfBits(xDim);
		int numOfYBits = UtilityMethods.numOfBits(yDim);
		
		
		//define obstacle
		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "oX");
		Variable[] oyVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(yDim), "oY");
		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
		Variable[] oVars = Variable.unionVariables(oxVars, oyVars);
		
		//transition relation
		int upAct = BDDWrapper.assign(bdd, 0, oActions);
		int upTrans = UtilityTransitionRelations.upTransitions(bdd, xDim, oxVars, oyVars, upAct);
		int downAct = BDDWrapper.assign(bdd, 1, oActions);
		int downTrans = UtilityTransitionRelations.downTransitions(bdd, xDim, oxVars, oyVars, downAct);
		int obsTrans = BDDWrapper.or(bdd, upTrans, downTrans);
		
		BDDWrapper.free(bdd, upAct);
		BDDWrapper.free(bdd, upTrans);
		
		BDDWrapper.free(bdd, downAct);
		BDDWrapper.free(bdd, downTrans);
		
//		int obsTrans = downTrans;
		
		
		//init 
		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, oyVars, new GridCell2D(xDim, yDim/2+1));
		
		Agent2D obstacle = new Agent2D(bdd, "obstacle", AgentType.Uncontrollable, oVars, oActions, obsTrans, obsInit, oxVars, oyVars); 
		
		//define robot
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "robotX");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd,UtilityMethods.numOfBits(yDim), "robotY");
		Variable[] robotActions = Variable.createVariables(bdd, 2, "act");
		Variable[] robotVars = Variable.unionVariables(xVars, yVars);
		
		//transition relation
		int robotTransitionRelation=UtilityTransitionRelations.createSimple2DTransitionRelation(bdd, xDim, yDim, xVars, yVars, robotActions);
		
		//init (0,0) 
		int rx0 = BDDWrapper.assign(bdd, 0, xVars);
		int ry0 = BDDWrapper.assign(bdd, yDim/2+1, yVars);
		int rinit=bdd.ref(bdd.and(rx0, ry0));
		bdd.deref(rx0);
		bdd.deref(ry0);
		
		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, yVars);
		
		
		//define the game structure
		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, obstacle, robot);
		
//		gameStructure.removeUnreachableStates().toGameGraph().draw("gameStructure.dot", 1, 0); 
		
		//define objectives 
		int safetyObjective = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(),obstacle.getXVars(), obstacle.getYVars());
		
//		GameSolution sol = GameSolver.solve(bdd, gameStructure, safetyObjective);
//		sol.drawReachableWinnerStrategy("strategySafety2by2.dot");
		
		int reachabilityObjective = BDDWrapper.assign(bdd, xDim, xVars);
//		sol = GameSolver.solveForReachabilyObjectives(bdd, gameStructure, reachabilityObjective, safetyObjective);
//		sol.drawReachableWinnerStrategy("stratReachability2by2.dor");
		
//		sol = GameSolver.solveSafetyReachabilityObjectives(bdd, gameStructure, safetyObjective, reachabilityObjective);
//		sol.drawReachableWinnerStrategy("stratSafetReachability2by2.dot");
		
		int noCollisionWithStaticObsObj = UtilityFormulas.noCollisionWithStaticObstacles(bdd, robot.getXVars(),
				robot.getYVars(), staticObstacles);
		safetyObjective = bdd.andTo(safetyObjective, noCollisionWithStaticObsObj);
		
//		GameSolution solution = GameSolver.solveSafetyReachabilityObjectives(bdd, gameStructure, 
//				safetyObjective, reachabilityObjective);
//		
//		solution.print();
//		solution.drawReachableWinnerStrategy("strategy.dot");
		
		//define agents
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(obstacle);
		agents.add(robot);
		

		//define parameters 
		Variable[] px = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfXBits, "px");
		
		Variable[] params = px;
		
//		int paramTrans = BDDWrapper.same(bdd, params, Variable.getPrimedCopy(params));
//		
//		//define parametric game structure
//		Variable[] parametricGameVariables = Variable.unionVariables(gameStructure.variables, params);
//		int parametricT_env = bdd.ref(bdd.and(gameStructure.getEnvironmentTransitionRelation(), paramTrans));
//		int parametricT_sys = bdd.ref(bdd.and(gameStructure.getSystemTransitionRelation(), paramTrans));
//		
//		GameStructure parametricGame = new GameStructure(bdd, parametricGameVariables, Variable.getPrimedCopy(parametricGameVariables), gameStructure.getInit(), parametricT_env, parametricT_sys, gameStructure.envActionVars, gameStructure.sysActionVars);
		
		ParametricGameStructure parametricGame = gameStructure.generateParametricGameStructure(params);
		
		//define parametric init 
		int sameXP = BDDWrapper.same(bdd, xVars, px);
				
		int parametricInit = BDDWrapper.and(bdd, safetyObjective, sameXP);
		BDDWrapper.free(bdd, sameXP);
		parametricGame.setInit(parametricInit);
		
		// eventually (x = px + 1)  
		int parametricReachabilityObjective1 = UtilityTransitionRelations.addOne(bdd, px, xVars);
		
		int boundCheckingX = BDDWrapper.assign(bdd, xDim, px);
		int parametricSafetyObjective1 = BDDWrapper.and(bdd, safetyObjective, bdd.not(boundCheckingX));
		
		int newInit = BDDWrapper.and(bdd, parametricGame.getInit(), parametricSafetyObjective1);
		parametricGame.setInit(newInit);
		
		CoSafeControllerInterface controlInterface = new CoSafeControllerInterface(bdd, newInit, parametricSafetyObjective1, parametricReachabilityObjective1);
		
		GameSolution solution = GameSolver.solveForReachabilyObjectives(bdd, parametricGame, parametricReachabilityObjective1, parametricSafetyObjective1);
		solution.print();
		
		parametricGame.printGameVars();
		
		//define parametric controller
		Controller c1Controller = new Controller(bdd, parametricGame.variables, parametricGame.actionVars, 
				parametricGame.getInit(), solution.getController(), 
				BDDWrapper.and(bdd, parametricSafetyObjective1, parametricReachabilityObjective1)); 
		ParametricCoSafeController c1 = new ParametricCoSafeController(c1Controller, parametricGame, controlInterface);
		
		//define the second parametric controller
		Variable[] py = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfYBits, "py");
				
		ParametricGameStructure parametricGame2 = gameStructure.generateParametricGameStructure(py);
		
		//define parametric init 
		int sameYP = BDDWrapper.same(bdd, yVars, py);
				
		int parametricInit2 = BDDWrapper.and(bdd, safetyObjective, sameYP);
		BDDWrapper.free(bdd, sameYP);
		parametricGame2.setInit(parametricInit2);
		
		// eventually (x = px + 1)  
		int parametricReachabilityObjective2 = UtilityTransitionRelations.addOne(bdd, py, yVars);
		
		int boundCheckingY = BDDWrapper.assign(bdd, yDim, py);
		boundCheckingY = BDDWrapper.orTo(bdd, boundCheckingY, BDDWrapper.assign(bdd, yDim-1, py));
		boundCheckingY = BDDWrapper.orTo(bdd, boundCheckingY, BDDWrapper.assign(bdd, yDim-2, py));
		int parametricSafetyObjective2 = BDDWrapper.and(bdd, safetyObjective, bdd.not(boundCheckingY));
		
		int newInit2 = BDDWrapper.and(bdd, parametricGame2.getInit(), parametricSafetyObjective2);
		parametricGame2.setInit(newInit2);
		
		CoSafeControllerInterface controlInterface2 = new CoSafeControllerInterface(bdd, newInit2, parametricSafetyObjective2, parametricReachabilityObjective2);
		
		GameSolution solution2 = GameSolver.solveForReachabilyObjectives(bdd, parametricGame2, parametricReachabilityObjective2, parametricSafetyObjective2);
		solution2.print();
		
		parametricGame2.printGameVars();
		
		//define parametric controller
		Controller c2Controller = new Controller(bdd, parametricGame2.variables, parametricGame2.actionVars, 
				parametricGame2.getInit(), solution2.getController(), 
				BDDWrapper.and(bdd, parametricSafetyObjective2, parametricReachabilityObjective2)); 
		ParametricCoSafeController c2 = new ParametricCoSafeController(c2Controller, parametricGame2, controlInterface2);
		
		//can we compose the controller using control interfaces and compute a control strategy
		//form the control game
//		ControlGameStructure cgs = new ControlGameStructure(bdd, gameStructure.variables, params, controlInterface);
//		
//		cgs.toGameGraph().draw("controlGame.dot", 1, 0);
//		bdd.printSet(cgs.getControlTransitionRelation());
		
//		int concreteObj = BDDWrapper.and(bdd, reachabilityObjective, safetyObjective);
//		
//		UtilityMethods.debugBDDMethods(bdd,"reach+safety", concreteObj);
//		
//		int pre = cgs.controllablePredecessor(concreteObj);
//		pre = BDDWrapper.andTo(bdd, pre, safetyObjective);
//		UtilityMethods.debugBDDMethods(bdd, "cpr of x=xdim", pre);
//		
//		pre = cgs.controllablePredecessor(pre);
//		pre = BDDWrapper.andTo(bdd, pre, safetyObjective);
//		UtilityMethods.debugBDDMethods(bdd, "cpr of x=xdim-1", pre);
//		
//		pre = cgs.controllablePredecessor(pre);
//		pre = BDDWrapper.andTo(bdd, pre, safetyObjective);
//		UtilityMethods.debugBDDMethods(bdd, "cpr of x=xdim-2", pre);
		
		//form the control game structure 
		ArrayList<Variable[]> parameters = new ArrayList<Variable[]>();
		parameters.add(px);
		parameters.add(py);
		
		ArrayList<CoSafeControllerInterface> controllerInterfaces = new ArrayList<CoSafeControllerInterface>();
		controllerInterfaces.add(controlInterface);
		controllerInterfaces.add(controlInterface2);
		
		ControlGameStructure cgsAll = new ControlGameStructure(bdd, gameStructure.variables, parameters, controllerInterfaces);
		
		//define concrete objectives
		
		int concreteInit = BDDWrapper.and(bdd, obstacle.getInit(), robot.getInit());
		int concreteSafety = safetyObjective;
		int concreteReachability = BDDWrapper.assign(bdd, xDim, robot.getXVars());
		
		ControlGameSolution sol = ControlGameSolver.solveForReachabilyObjectives(bdd, cgsAll, concreteInit, 
				concreteReachability, concreteSafety);
		
//		ControlGameSolution sol = ControlGameSolver.solveForReachabilyObjectives(bdd, cgsAll, concreteInit, 
//				concreteReachability, concreteSafety);
		
//		UtilityMethods.debugBDDMethods(bdd, "controller is ", sol.getController());
		
//		ControlGameStructure cgs2 = cgs.composeWithController(sol.getController());
//		UtilityMethods.debugBDDMethods(bdd, "controlled game", cgs2.getControlTransitionRelation());
//		
//		cgs2.removeUnreachableStates(concreteInit).toGameGraph().draw("cgs2.dot", 1, 0);
		
		//determinize the computed control strategy
//		int deterministicController = cgsAll.determinizeController(sol.getController());
//		
//		UtilityMethods.debugBDDMethods(bdd, "deterministic controller is ", deterministicController);
		
//		ControlGameStructure cgs3 = cgs.composeWithController(deterministicController);
//		UtilityMethods.debugBDDMethods(bdd, "controlled game", cgs3.getControlTransitionRelation());
//		
//		cgs3.removeUnreachableStates(concreteInit).toGameGraph().draw("cgs3.dot", 1, 0);
		
		//compose the control strategy with controllers' transition relation
//		int controlAct0 = BDDWrapper.assign(bdd, 0, cgsAll.getControlVariables());
//		int controller0 = BDDWrapper.and(bdd, solution.getController(), controlAct0);
//		
//		int controlAct1 = BDDWrapper.assign(bdd, 1, cgsAll.getControlVariables());
//		int controller1 = BDDWrapper.and(bdd, solution.getController(), controlAct1);
		
//		deterministicController = BDDWrapper.andTo(bdd, deterministicController, controller0);
//		deterministicController = BDDWrapper.andTo(bdd, deterministicController, controller1);
		
//		int composedController = BDDWrapper.or(bdd, controller0, controller1);
//		deterministicController = BDDWrapper.andTo(bdd, deterministicController, composedController);
		
//		UtilityMethods.debugBDDMethods(bdd, "deterministic controller after composition", deterministicController);
		
//		int controller = BDDWrapper.exists(bdd, deterministicController, cgsAll.getParametersAndControlVariablesCube());
		
//		UtilityMethods.debugBDDMethods(bdd, "concrete controller", controller);
		
//		if(sol.getController() == deterministicController) System.out.println("controller and deterministic controllers are the same");
		
//		GameStructure finalGame = gameStructure.composeWithController(controller);
		
//		Simulator2D simulator = new Simulator2D(bdd, agents, finalGame, concreteInit);
		
		ArrayList<ParametricCoSafeController> parametricControllers = new ArrayList<ParametricCoSafeController>();
		parametricControllers.add(c1);
		parametricControllers.add(c2);
		ControlGameSimulator contGameSim = new ControlGameSimulator(bdd, gameStructure, agents, cgsAll, sol.getController(), parametricControllers, concreteInit);
		
		//define 
//		CaseStudy2D result = new CaseStudy2D(bdd, xDim, yDim, agents, finalGame, staticObstacles);
		CaseStudy2D result = new CaseStudy2D(bdd, xDim, yDim, agents, gameStructure, staticObstacles, contGameSim);
		return result;
	}
	
	public static void main(String[] args){
		int dim = 3;
		CaseStudy2D cs = TwoWayRoadCaseStudy.createSimpleTwoWayRoadCaseStudy(dim, dim);
		
//		BDD bdd = new BDD(10000, 1000);
//		Variable a = Variable.createVariableAndPrimedVariable(bdd, "a");
//		Variable b = Variable.createVariableAndPrimedVariable(bdd, "b");
//		
//		int f = BDDWrapper.or(bdd, a.getBDDVar(), b.getBDDVar());
//		f = BDDWrapper.orTo(bdd, f, a.getPrimedCopy().getBDDVar());
//		bdd.printSet(f);
//		
//		int g = bdd.oneSat(f);
//		bdd.printSet(g);
//		
//		int f2 = BDDWrapper.diff(bdd, f, g);
//		bdd.printSet(f2);
//		
//		int g2 = bdd.oneSat(f2);
//		bdd.printSet(g2);
//		
//		Variable[] vars = new Variable[]{a,b,a.getPrimedCopy()};
//		int g3 = BDDWrapper.oneSatisfyingAssignment(bdd, g2, vars);
//		
//		bdd.printSet(g3);
//		
//		int f4 = BDDWrapper.diff(bdd, f2, g3);
//		bdd.printSet(f4);
//		
//		int f3 = bdd.restrict(f, bdd.not(a.getBDDVar()));
//		bdd.printSet(f3);
		
	}
	
	
}
