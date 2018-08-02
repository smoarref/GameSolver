package testsAndCaseStudies;

import java.util.ArrayList;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Variable;

public class TestParametricObjectives {
	
	BDD bdd;
	ArrayList<Agent2D> agents;
	int xDim;
	int yDim;
	GameStructure solution;
	ArrayList<GridCell2D> staticObstacles;
	
	
	public TestParametricObjectives(int dim ){
		bdd = new BDD(10000,1000);
		xDim = dim;
		yDim = dim;
		
		int numOfXBits = UtilityMethods.numOfBits(xDim);
		int numOfYBits = UtilityMethods.numOfBits(yDim);
		
		
		//define obstacle
		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "oX");
		Variable[] oyVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(yDim), "oY");
		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
		Variable[] oVars = Variable.unionVariables(oxVars, oyVars);
		
		//transition relation
		int upAct = BDDWrapper.assign(bdd, 0, oActions);
		int downAct = BDDWrapper.assign(bdd, 1, oActions);
		int upTrans = UtilityTransitionRelations.upTransitions(bdd, xDim, oxVars, oyVars, upAct);
		int downTrans = UtilityTransitionRelations.downTransitions(bdd, xDim, oxVars, oyVars, downAct);
		int obsTrans = BDDWrapper.or(bdd, upTrans, downTrans);
		BDDWrapper.free(bdd, upAct);
		BDDWrapper.free(bdd, upTrans);
		BDDWrapper.free(bdd, downAct);
		BDDWrapper.free(bdd, downTrans);
		
		//init 
		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, oyVars, new GridCell2D(0, 1));
		
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
		int ry0 = BDDWrapper.assign(bdd, 0, yVars);
		int rinit=bdd.ref(bdd.and(rx0, ry0));
		bdd.deref(rx0);
		bdd.deref(ry0);
		
		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, yVars);
		
		
		//define the game structure
		GameStructure gameStructure = GameStructure.createGameForAgents(bdd, obstacle, robot);
		
		gameStructure.removeUnreachableStates().toGameGraph().draw("gameStructure.dot", 1, 0); 
		
		//define objectives 
		int safetyObjective = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(),obstacle.getXVars(), obstacle.getYVars());
		
		GameSolution sol = GameSolver.solve(bdd, gameStructure, safetyObjective);
		sol.drawReachableWinnerStrategy("strategySafety2by2.dot");
		
		int reachabilityObjective = BDDWrapper.assign(bdd, 1, xVars);
		sol = GameSolver.solveForReachabilyObjectives(bdd, gameStructure, reachabilityObjective, safetyObjective);
		sol.drawReachableWinnerStrategy("stratReachability2by2.dor");
		
		sol = GameSolver.solveSafetyReachabilityObjectives(bdd, gameStructure, safetyObjective, reachabilityObjective);
		sol.drawReachableWinnerStrategy("stratSafetReachability2by2.dot");
		
		//define params
		Variable[] px = Variable.createVariables(bdd, numOfXBits, "px");
		Variable[] px_prime = Variable.createPrimeVariables(bdd, px);
		
		Variable[] py = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfYBits, "py");
		
		Variable[] params = Variable.unionVariables(px,py);
		
		int paramTrans = BDDWrapper.same(bdd, params, Variable.getPrimedCopy(params));
		
		//define parametric game structure
		Variable[] parametricGameVariables = Variable.unionVariables(gameStructure.variables, params);
		int parametricT_env = bdd.ref(bdd.and(gameStructure.getEnvironmentTransitionRelation(), paramTrans));
		int parametricT_sys = bdd.ref(bdd.and(gameStructure.getSystemTransitionRelation(), paramTrans));
		
		GameStructure parametricGame = new GameStructure(bdd, parametricGameVariables, Variable.getPrimedCopy(parametricGameVariables), gameStructure.getInit(), parametricT_env, parametricT_sys, gameStructure.envActionVars, gameStructure.sysActionVars);
		
		parametricGame.removeUnreachableStates().toGameGraph().draw("parametricGame2by2.dot", 1, 0);
		
		//define parametric init 
		int sameXP = BDDWrapper.same(bdd, xVars, px);
		int sameYP = BDDWrapper.same(bdd, yVars, py);
		int sameVP = BDDWrapper.and(bdd, sameXP, sameYP);
		BDDWrapper.free(bdd, sameXP);
		BDDWrapper.free(bdd, sameYP);
		int parametricInit = BDDWrapper.and(bdd, safetyObjective, sameVP);
		BDDWrapper.free(bdd, sameVP);
		parametricGame.setInit(parametricInit);
		
		parametricGame.removeUnreachableStates().toGameGraph().draw("parametricGame2by2NewInit.dot", 1, 0);
		
		// eventually (x = px + 1)  
		int parametricReachabilityObjective1 = UtilityTransitionRelations.addOne(bdd, px, xVars);
//		int sameY = BDDWrapper.same(bdd, yVars, Variable.getPrimedCopy(yVars));
//		parametricReachabilityObjective1 = BDDWrapper.andTo(bdd, parametricReachabilityObjective1, sameY);
//		BDDWrapper.free(bdd, sameY);
		
		// eventually (y = py + 1)
		int parametricReachabilityObjective2 = UtilityTransitionRelations.addOne(bdd, py, yVars);
//		int sameX = BDDWrapper.same(bdd, xVars, Variable.getPrimedCopy(xVars));
//		parametricReachabilityObjective2 = BDDWrapper.andTo(bdd, parametricReachabilityObjective2, sameX);
//		BDDWrapper.free(bdd, sameX);
		
		int boundCheckingX = BDDWrapper.assign(bdd, 1, px);
		int parametricSafetyObjective1 = BDDWrapper.and(bdd, safetyObjective, bdd.not(boundCheckingX));
		
		int boundCheckingY = BDDWrapper.assign(bdd, 1, py);
		int parametricSafetyObjective2 = BDDWrapper.and(bdd, safetyObjective, bdd.not(boundCheckingY));
		
		int newInit = BDDWrapper.and(bdd, parametricGame.getInit(), parametricSafetyObjective1);
		parametricGame.setInit(newInit);
		
		sol = GameSolver.solveForReachabilyObjectives(bdd, parametricGame, parametricReachabilityObjective1, parametricSafetyObjective1);
		sol.drawReachableWinnerStrategy("parametricReachabilityStrat.dot");
		
		int test = BDDWrapper.and(bdd, parametricReachabilityObjective1, parametricSafetyObjective1);
		UtilityMethods.debugBDDMethods(bdd, "checking bound", test);
		parametricGame.printGameVars();
	}
	
//	public TestParametricObjectives(int argXdim, int argYdim ){
//		bdd = new BDD(10000, 1000);
//		xDim = argXdim;
//		yDim = argYdim;
//		
//		//define robot
//		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "robotX");
//		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd,UtilityMethods.numOfBits(yDim), "robotY");
//		Variable[] robotActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] robotVars = Variable.unionVariables(xVars, yVars);
//		
//		//transition relation
//		// act=0 --> stop: stop & R'_x = R_x & R'_y = R_y
//		int actStop = BDDWrapper.assign(bdd, 0, robotActions);
//		int robotStop = UtilityTransitionRelations.halt(bdd, robotVars, actStop);
//		bdd.deref(actStop);
//		int robotTransitionRelation=robotStop;
//		
//		
//		// act=1 --> right: right & R_y !=3 & R'x=R_x+1 & R'_y = R_y
//		int actRight= BDDWrapper.assign(bdd, 1, robotActions);
//		int rY3 = BDDWrapper.assign(bdd, 3, yVars);
//		int rYNot3 = bdd.ref(bdd.not(rY3));
//		int rXsame = BDDWrapper.same(bdd, xVars, Variable.getPrimedCopy(xVars));
//		int rYincrement = UtilityTransitionRelations.addOne(bdd, yVars );
//		int rightTrans = bdd.ref(bdd.and(actRight, rXsame));
//		rightTrans=bdd.andTo(rightTrans, rYNot3);
//		rightTrans=bdd.andTo(rightTrans, rYincrement);
//		
//		robotTransitionRelation=bdd.orTo(robotTransitionRelation, rightTrans);
//		
//		
//		//init (0,0) 
//		int rx0 = BDDWrapper.assign(bdd, 0, xVars);
//		int ry0 = BDDWrapper.assign(bdd, 0, yVars);
//		int rinit=bdd.ref(bdd.and(rx0, ry0));
//		bdd.deref(rx0);
//		bdd.deref(ry0);
//		
//		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, yVars);
//		
//		//define obstacle
//		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "oX");
//		Variable[] oyVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(yDim), "oY");
//		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] oVars = Variable.unionVariables(oxVars, oyVars);
//		
//		//transition relation
//		//ox' = not ox & oy' = oy
//		Variable[] oxVariablesPrime = Variable.getPrimedCopy(oxVars);
//		int sameOX = BDDWrapper.same(bdd, oxVars, oxVariablesPrime);
//		int obsTrans = bdd.ref(bdd.not(sameOX));
//		bdd.deref(sameOX);
//		int sameOY = BDDWrapper.same(bdd, oyVars, Variable.getPrimedCopy(oyVars));
//		obsTrans = bdd.andTo(obsTrans, sameOY);
//		bdd.deref(sameOY);
//		
//		
//		//init 
//		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, oyVars, new GridCell2D(0, 1));
//		
//		Agent2D obstacle = new Agent2D(bdd, "obstacle", AgentType.Uncontrollable, oVars, oActions, obsTrans, obsInit, oxVars, oyVars); 
//		
//		//define the game structure
//		Variable[] gameVars = Variable.unionVariables(robotVars, oVars);
//		int gameInit = bdd.ref(bdd.and(rinit, obsInit));
//		int sameRobot = BDDWrapper.same(bdd, robotVars, Variable.getPrimedCopy(robotVars));
//		int T_env = bdd.ref(bdd.and(obstacle.getTransitionRelation(), sameRobot));
//		int sameObstacle = BDDWrapper.same(bdd, oVars, Variable.getPrimedCopy(oVars));
//		int T_sys = bdd.ref(bdd.and(robot.getTransitionRelation(), sameObstacle));
//		GameStructure g = new GameStructure(bdd, gameVars, Variable.getPrimedCopy(gameVars), gameInit, T_env, T_sys, obstacle.getActionVars(), robot.getActionVars());
//		
//		
//		//define objectives 
//		int obj = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(),obstacle.getXVars(), obstacle.getYVars());
//
//		
//		GameSolver gs = new GameSolver(g, obj, bdd);
//		GameSolution sol = gs.solve();
//		sol.print();
//		
//		solution = sol.strategyOfTheWinner();
//		
//		staticObstacles = new ArrayList<GridCell2D>();
//		
//		agents=new ArrayList<Agent2D>();
//		agents.add(robot);
//		agents.add(obstacle);
//		
//		int reachability = BDDWrapper.assign(bdd, yDim, yVars);
//		
//		int reachable = gs.leastFixedPoint(reachability);
//		UtilityMethods.debugBDDMethods(bdd, "reachable set", reachable);
//		
//		int safeReachable = gs.leastFixedPointWithSafety(reachability, obj);
//		UtilityMethods.debugBDDMethods(bdd, "safe reachable", safeReachable);
//		
//		
//		Variable[] p = Variable.createVariables(bdd, xDim, "p");
//		
//		int parametricInit = BDDWrapper.same(bdd, yVars, p);
//		
//		int post = g.symbolicGameOneStepExecution(parametricInit);
//		UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//		post = bdd.andTo(post, bdd.not(yVars[0].getBDDVar()));
//		UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//		
//		int params = bdd.ref(bdd.exists(post, g.getVariablesAndActionsCube()));
//		UtilityMethods.debugBDDMethods(bdd, "params", params);
//		
//		
//		
//	}
//	
//	public TestParametricObjectives(int dim){
//		bdd = new BDD(10000, 1000);
//		xDim = dim;
//		yDim = dim;
//		
//		//define robot
//		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "robotX");
//		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd,UtilityMethods.numOfBits(yDim), "robotY");
//		Variable[] robotActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] robotVars = Variable.unionVariables(xVars, yVars);
//				
//		//transition relation
//		// act=0 --> stop: stop & R'_x = R_x & R'_y = R_y
//		int actStop = BDDWrapper.assign(bdd, 0, robotActions);
//		int robotStop = UtilityTransitionRelations.halt(bdd, robotVars, actStop);
//		bdd.deref(actStop);
//		int robotTransitionRelation=robotStop;
//				
//				
//		// act=1 --> move: move & R_y !=3 & R'x=R_x & R'_y = NOT R_y
//		int actRight= BDDWrapper.assign(bdd, 1, robotActions);
//		int rXsame = BDDWrapper.same(bdd, xVars, Variable.getPrimedCopy(xVars));
//		int rYsame = BDDWrapper.same(bdd, yVars, Variable.getPrimedCopy(yVars));
//		int rYNotSame = bdd.ref(bdd.not(rYsame));
//		bdd.deref(rYsame);
//		int rightTrans = bdd.ref(bdd.and(actRight, rXsame));
//		rightTrans=bdd.andTo(rightTrans, rYNotSame);
//				
//		robotTransitionRelation=bdd.orTo(robotTransitionRelation, rightTrans);
//				
//				
//		//init (0,0) 
//		int rx0 = BDDWrapper.assign(bdd, 0, xVars);
//		int ry0 = BDDWrapper.assign(bdd, 0, yVars);
//		int rinit=bdd.ref(bdd.and(rx0, ry0));
//		bdd.deref(rx0);
//		bdd.deref(ry0);
//				
//		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, yVars);
//				
//		//define obstacle
//		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "oX");
//		Variable[] oyVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(yDim), "oY");
//		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] oVars = Variable.unionVariables(oxVars, oyVars);
//				
//		//transition relation
//		//ox' = not ox & oy' = oy
//		Variable[] oxVariablesPrime = Variable.getPrimedCopy(oxVars);
//		int sameOX = BDDWrapper.same(bdd, oxVars, oxVariablesPrime);
//		int obsTrans = bdd.ref(bdd.not(sameOX));
//		bdd.deref(sameOX);
//		int sameOY = BDDWrapper.same(bdd, oyVars, Variable.getPrimedCopy(oyVars));
//		obsTrans = bdd.andTo(obsTrans, sameOY);
//		bdd.deref(sameOY);
//				
//				
//		//init 
//		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, oyVars, new GridCell2D(0, 1));
//				
//		Agent2D obstacle = new Agent2D(bdd, "obstacle", AgentType.Uncontrollable, oVars, oActions, obsTrans, obsInit, oxVars, oyVars); 
//				
//		//define the game structure
//		Variable[] gameVars = Variable.unionVariables(robotVars, oVars);
//		int gameInit = bdd.ref(bdd.and(rinit, obsInit));
//		int sameRobot = BDDWrapper.same(bdd, robotVars, Variable.getPrimedCopy(robotVars));
//		int T_env = bdd.ref(bdd.and(obstacle.getTransitionRelation(), sameRobot));
//		int sameObstacle = BDDWrapper.same(bdd, oVars, Variable.getPrimedCopy(oVars));
//		int T_sys = bdd.ref(bdd.and(robot.getTransitionRelation(), sameObstacle));
//		GameStructure g = new GameStructure(bdd, gameVars, Variable.getPrimedCopy(gameVars), gameInit, T_env, T_sys, obstacle.getActionVars(), robot.getActionVars());
//				
//				
//		//define objectives 
//		int obj = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(),obstacle.getXVars(), obstacle.getYVars());
//
//				
//				GameSolver gs = new GameSolver(g, obj, bdd);
//				GameSolution sol = gs.solve();
//				sol.print();
//				
//				solution = sol.strategyOfTheWinner();
//				
//				staticObstacles = new ArrayList<GridCell2D>();
//				
//				agents=new ArrayList<Agent2D>();
//				agents.add(robot);
//				agents.add(obstacle);
//				
//				int reachability = BDDWrapper.assign(bdd, yDim, yVars);
//				
//				int reachable = gs.leastFixedPoint(reachability);
//				UtilityMethods.debugBDDMethods(bdd, "reachable set", reachable);
//				
//				int safeReachable = gs.leastFixedPointWithSafety(reachability, obj);
//				UtilityMethods.debugBDDMethods(bdd, "safe reachable", safeReachable);
//				
//				
//				Variable[] p = Variable.createVariables(bdd, xDim, "p");
//				
//				int parametricInit = BDDWrapper.same(bdd, yVars, p);
//				
//				int post = g.symbolicGameOneStepExecution(parametricInit);
//				UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//				post = bdd.andTo(post, bdd.not(yVars[0].getBDDVar()));
//				UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//				
//				int params = bdd.ref(bdd.exists(post, g.getVariablesAndActionsCube()));
//				UtilityMethods.debugBDDMethods(bdd, "params", params);
//	}
	
	public TestParametricObjectives(int dim, boolean dummy){
		bdd = new BDD(10000, 1000);
		xDim = dim;
		yDim = dim;
		
		//define obstacle
		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "oX");
		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
		Variable[] oVars = oxVars;
				
		//transition relation
		//ox' = not ox & oy' = oy
		Variable[] oxVariablesPrime = Variable.getPrimedCopy(oxVars);
		int sameOX = BDDWrapper.same(bdd, oxVars, oxVariablesPrime);
		int obsTrans = bdd.ref(bdd.not(sameOX));
		bdd.deref(sameOX);
				
				
		//init 
//		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, null, new GridCell2D(0, 1));
		int obsInit = BDDWrapper.assign(bdd, 1, oxVars);
				
		Agent2D obstacle = new Agent2D(bdd, "obstacle", AgentType.Uncontrollable, oVars, oActions, obsTrans, obsInit, oxVars, null);
		
		//define robot
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(xDim), "robotX");
		Variable[] robotActions = Variable.createVariables(bdd, 1, "act");
		Variable[] robotVars = xVars;
				
		//transition relation
		// act=0 --> stop: stop & R'_x = R_x & R'_y = R_y
		int actStop = BDDWrapper.assign(bdd, 0, robotActions);
		int robotStop = UtilityTransitionRelations.halt(bdd, robotVars, actStop);
		bdd.deref(actStop);
		int robotTransitionRelation=robotStop;
				
				
		// act=1 --> move: move & R'x= NOT R_x
		int actRight= BDDWrapper.assign(bdd, 1, robotActions);
		int rXsame = BDDWrapper.same(bdd, xVars, Variable.getPrimedCopy(xVars));
		int rXNotSame = bdd.ref(bdd.not(rXsame));
		bdd.deref(rXsame);
		int rightTrans = bdd.ref(bdd.and(actRight, rXNotSame));
				
		robotTransitionRelation=bdd.orTo(robotTransitionRelation, rightTrans);
				
				
		//init (0,0) 
		int rinit = BDDWrapper.assign(bdd, 0, xVars);

				
		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, null);
				
 
				
		//define the game structure
		Variable[] gameVars = Variable.unionVariables(robotVars, oVars);
		int gameInit = bdd.ref(bdd.and(rinit, obsInit));
		int sameRobot = BDDWrapper.same(bdd, robotVars, Variable.getPrimedCopy(robotVars));
		int T_env = bdd.ref(bdd.and(obstacle.getTransitionRelation(), sameRobot));
		int sameObstacle = BDDWrapper.same(bdd, oVars, Variable.getPrimedCopy(oVars));
		int T_sys = bdd.ref(bdd.and(robot.getTransitionRelation(), sameObstacle));
		GameStructure g = new GameStructure(bdd, gameVars, Variable.getPrimedCopy(gameVars), gameInit, T_env, T_sys, obstacle.getActionVars(), robot.getActionVars());
				
		g.toGameGraph().draw("game.dot", 1, 0);
		
		//define objectives 
		int sameRO = BDDWrapper.same(bdd, xVars, oxVars);
		int safetyObjective = bdd.ref(bdd.not(sameRO));

				
		GameSolution sol = GameSolver.solve(bdd, g, safetyObjective);
		sol.print();
				
		solution = sol.strategyOfTheWinner();
				
		staticObstacles = new ArrayList<GridCell2D>();
				
		agents=new ArrayList<Agent2D>();
		agents.add(robot);
		agents.add(obstacle);
		
		solution.removeUnreachableStates().toGameGraph().draw("strategySafety.dot", 1, 0);
		
				
		int reachabilityObjective = BDDWrapper.assign(bdd, 1, xVars);
		
		sol = GameSolver.solveForReachabilyObjectives(bdd, g, reachabilityObjective, safetyObjective);
		sol.print();
		sol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("strategyReachability.dot", 1, 0);
		
		
		
		sol=GameSolver.solveSafetyReachabilityObjectives(bdd, g, safetyObjective, reachabilityObjective);
		sol.print();
		sol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("strategySafetyReachability.dot", 1, 0);
		
				
		Variable[] p = Variable.createVariables(bdd, xDim, "p");
		Variable[] p_prime = Variable.createPrimeVariables(bdd, p);
		
		int paramTrans = BDDWrapper.same(bdd, p, p_prime);
		
		Variable[] parametricGameVariables = Variable.unionVariables(gameVars, p);
		int parametricT_env = bdd.ref(bdd.and(g.getEnvironmentTransitionRelation(), paramTrans));
		int parametricT_sys = bdd.ref(bdd.and(g.getSystemTransitionRelation(), paramTrans));
		
		
		GameStructure parametricGame = new GameStructure(bdd, parametricGameVariables, Variable.getPrimedCopy(parametricGameVariables), g.getInit(), parametricT_env, parametricT_sys, g.envActionVars, g.sysActionVars);
		
		
		
		parametricGame.toGameGraph().draw("parametricGame.dot", 1, 0);
				
//		int parametricInit = BDDWrapper.same(bdd, xVars, p);
//				
//		int post = parametricGame.symbolicGameOneStepExecution(parametricInit);
//		UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//				post = bdd.andTo(post, bdd.not(yVars[0].getBDDVar()));
//				UtilityMethods.debugBDDMethods(bdd, "psot parametric", post);
//				
//				int params = bdd.ref(bdd.exists(post, g.getVariablesAndActionsCube()));
//				UtilityMethods.debugBDDMethods(bdd, "params", params);
		
//		int parametricGameInit = bdd.ref(safetyObjective);
//		parametricGameInit = bdd.andTo(parametricGameInit, arg1)
		
		int parametricReachabilityObjective = BDDWrapper.same(bdd, xVars, p);
		
		int newInit = BDDWrapper.and(bdd, safetyObjective, bdd.not(parametricReachabilityObjective));
		parametricGame.setInit(newInit);
		parametricGame.toGameGraph().draw("parametricGameInitChanged.dot", 1, 0);
		
		
		sol=GameSolver.solveSafetyReachabilityObjectives(bdd, parametricGame, safetyObjective, parametricReachabilityObjective);
		sol.print();
		sol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("parametricStrategySafetyReachability.dot", 1, 0);
		
		GameStructure strat = sol.strategyOfTheWinner();
		
		//replace parameter
		Variable.printVariables(parametricGameVariables);
		int param = bdd.ref(p[0].getBDDVar());
		param=bdd.ref(bdd.and(param, p_prime[0].getBDDVar()));
		int strat_init = strat.getInit();
		UtilityMethods.debugBDDMethods(bdd, "strat init is ", strat.getInit());
		int init_par_replaced =bdd.ref(bdd.restrict(strat_init, p[0].getBDDVar()));
		UtilityMethods.debugBDDMethods(bdd,"param replaced in init", init_par_replaced);
		int newT_env = bdd.ref(bdd.restrict(strat.getEnvironmentTransitionRelation(), param));
		UtilityMethods.debugBDDMethods(bdd, "parametric T_env", strat.getEnvironmentTransitionRelation());
		UtilityMethods.debugBDDMethods(bdd, "param replaced in T_env", newT_env);
		int newT_sys = bdd.ref(bdd.restrict(strat.getSystemTransitionRelation(), param));
		UtilityMethods.debugBDDMethods(bdd, "param replaced in ", newT_sys);
		
		Variable[] paramRemovedVars = new Variable[strat.variables.length-1];
		int counter=0;
		for(int i=0; i<strat.variables.length; i++){
			if(strat.variables[i] == p[0]) continue;
			paramRemovedVars[counter] = strat.variables[i];
			counter++;
		}
		
		Variable.printVariables(paramRemovedVars);
		
		GameStructure paramRemoved = new GameStructure(bdd, paramRemovedVars, Variable.getPrimedCopy(paramRemovedVars), init_par_replaced, newT_env, newT_sys, g.envActionVars, g.sysActionVars);
		paramRemoved.removeUnreachableStates().toGameGraph().draw("paramRemoved.dot", 1, 0);
	}
	
	public BDD getBDD(){
		return bdd;
	}
	
	public ArrayList<Agent2D> getAgents(){
		return agents;
	}
	
	public int getXdim(){
		return xDim;
	}
	
	public int getYdim(){
		return yDim;
	}
	
	public GameStructure getSolution(){
		return solution;
	}
	
	public ArrayList<GridCell2D> getStaticObstacles(){
		return staticObstacles;
	}
	
	public static void main(String[] args){
//		BDD bdd = new BDD(10000, 1000);
//		
//		//define robot
//		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 1, "robotX");
//		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "robotY");
//		Variable[] robotActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] robotVars = Variable.unionVariables(xVars, yVars);
//		
//		//transition relation
//		// act=0 --> stop: stop & R'_x = R_x & R'_y = R_y
//		int actStop = BDDWrapper.assign(bdd, 0, robotActions);
//		int robotStop = UtilityTransitionRelations.halt(bdd, robotVars, actStop);
//		bdd.deref(actStop);
//		int robotTransitionRelation=robotStop;
//		
//		
//		// act=1 --> right: right & R_y !=3 & R'x=R_x+1 & R'_y = R_y
//		int actRight= BDDWrapper.assign(bdd, 1, robotActions);
//		int rY3 = BDDWrapper.assign(bdd, 3, yVars);
//		int rYNot3 = bdd.ref(bdd.not(rY3));
//		int rXsame = BDDWrapper.same(bdd, xVars, Variable.getPrimedCopy(xVars));
//		int rYincrement = UtilityTransitionRelations.addOne(bdd, yVars );
//		int rightTrans = bdd.ref(bdd.and(actRight, rXsame));
//		rightTrans=bdd.andTo(rightTrans, rYNot3);
//		rightTrans=bdd.andTo(rightTrans, rYincrement);
//		
//		robotTransitionRelation=bdd.orTo(robotTransitionRelation, rightTrans);
//		
//		
//		//init (0,0) 
//		int rx0 = BDDWrapper.assign(bdd, 0, xVars);
//		int ry0 = BDDWrapper.assign(bdd, 0, yVars);
//		int rinit=bdd.ref(bdd.and(rx0, ry0));
//		bdd.deref(rx0);
//		bdd.deref(ry0);
//		
//		Agent2D robot = new Agent2D(bdd, "robot", AgentType.Controllable, robotVars, robotActions, robotTransitionRelation, rinit, xVars, yVars);
//		
//		//define obstacle
//		Variable[] oxVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 1, "oX");
//		Variable[] oyVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "oY");
//		Variable[] oActions = Variable.createVariables(bdd, 1, "act");
//		Variable[] oVars = Variable.unionVariables(oxVars, oyVars);
//		
//		//transition relation
//		//ox' = not ox & oy' = oy
//		Variable[] oxVariablesPrime = Variable.getPrimedCopy(oxVars);
//		int sameOX = BDDWrapper.same(bdd, oxVars, oxVariablesPrime);
//		int obsTrans = bdd.ref(bdd.not(sameOX));
//		bdd.deref(sameOX);
//		int sameOY = BDDWrapper.same(bdd, oyVars, Variable.getPrimedCopy(oyVars));
//		obsTrans = bdd.andTo(obsTrans, sameOY);
//		bdd.deref(sameOY);
//		
//		
//		//init 
//		int obsInit = UtilityFormulas.assignCell(bdd, oxVars, oyVars, new GridCell2D(0, 1));
//		
//		Agent2D obstacle = new Agent2D(bdd, "obstacle", AgentType.Uncontrollable, oVars, oActions, obsTrans, obsInit, oxVars, oyVars); 
//		
//		//define the game structure
//		Variable[] gameVars = Variable.unionVariables(robotVars, oVars);
//		int gameInit = bdd.ref(bdd.and(rinit, obsInit));
//		int sameRobot = BDDWrapper.same(bdd, robotVars, Variable.getPrimedCopy(robotVars));
//		int T_env = bdd.ref(bdd.and(obstacle.getTransitionRelation(), sameRobot));
//		int sameObstacle = BDDWrapper.same(bdd, oVars, Variable.getPrimedCopy(oVars));
//		int T_sys = bdd.ref(bdd.and(robot.getTransitionRelation(), sameObstacle));
//		GameStructure g = new GameStructure(bdd, gameVars, Variable.getPrimedCopy(gameVars), gameInit, T_env, T_sys, obstacle.getActionVars(), robot.getActionVars());
//		
//		
//		//define objectives 
//		int obj = UtilityFormulas.noCollisionObjective(bdd, robot.getXVars(), robot.getYVars(),obstacle.getXVars(), obstacle.getYVars());
//
//		
//		GameSolver gs = new GameSolver(g, obj, bdd);
//		GameSolution solution = gs.solve();
//		
//		solution.print();
		
		TestParametricObjectives tpo = new TestParametricObjectives(1, true);
		
		
	}
}
