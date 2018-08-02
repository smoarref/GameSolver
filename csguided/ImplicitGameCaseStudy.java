package csguided;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jdd.bdd.BDD;
import utils.UtilityMethods;

public class ImplicitGameCaseStudy extends Game{

	
	
	int xDimension;
	int yDimension;
	
	int numOfXBits;
	int numOfYBits;
	
	//indices
	int envXindex=0;
	int envYindex=0;
	
	int sysXindex=0;
	int sysYindex=0;
	
	boolean up=true;
	boolean left=true;
	
	int sameSystemVars=-1;
	int sameEnvironmentVars=-1;
	
	int sameRx=-1;
	int sameRy=-1;
	int sameOx=-1;
	int sameOy=-1;
	
	int previousEnvTrans=-1;
	int previousSysTrans=-1;
	
	
	
	/**
	 * Environment (uncontrollable) agents X and Y variables and their primed copy
	 */
	Variable[] envXvars;
	Variable[] envXvarsPrime;
	
	Variable[] envYvars;
	Variable[] envYvarsPrime;
	
	Variable[] environmentVars;
	Variable[] environmentVarsPrime;
	
	/**
	 * System (controllable) agents X and Y variables and their primed copy
	 */
	Variable[] sysXvars;
	Variable[] sysXvarsPrime;
	
	Variable[] sysYvars;
	Variable[] sysYvarsPrime;
	
	Variable[] systemVars;
	Variable[] systemVarsPrime;
	
//	Variable[] variables;
//	Variable[] primeVariables;
	
	/**
	 * Action variables
	 */
//	Variable[] actionVars;
	
//	ArrayList<int[]> envActions;
//	ArrayList<int[]> sysActions;
//	int[] actions;
	
	public ImplicitGameCaseStudy(BDD argBDD){
		super(argBDD);
	}
	
	
	public ImplicitGameCaseStudy(BDD argBDD, int dimension, int argEnvXInit, int argEnvYInit, int argSysXInit, int argSysYInit){
		super(argBDD);
		xDimension=dimension;
		yDimension=dimension;
		calculateNumberOfBits();
		
		generateSimple2DRoboticGameWithOneAgent(argEnvXInit, argEnvYInit, argSysXInit, argSysYInit);
	}
	
//	public ImplicitGameCaseStudy(BDD argBDD, String argId, int dimension, ArrayList<GridCell2D> envInitCell, 
//			GridCell2D sysInitCell, ArrayList<Variable[]> argEnvX, 
//			ArrayList<Variable[]> argEnvY, Variable[] argSysX, Variable[] argSysY, 
//			Variable[] argEnvActs, Variable[] argSysActs){
//		super(argBDD);
//		id = argId;
//		xDimension=dimension;
//		yDimension=dimension;
//		calculateNumberOfBits();
//		
//		Variable[] env_X = Variable.unionVariables(argEnvX);
//		Variable[] env_Y = Variable.unionVariables(argEnvY);
//		
//		defineVariables(env_X, env_Y, argSysX, argSysY, argEnvActs, argSysActs);
//		
//		//define init
//		int envInit = bdd.ref(bdd.getOne());
//		for(int i=0; i<envInitCell.size();i++){
//			int init_i = assignCell(argEnvX.get(i), argEnvY.get(i), envInitCell.get(i));
//			envInit = BDDWrapper.andTo(bdd, envInit, init_i);
//			BDDWrapper.free(bdd, init_i);
//		}
//
//		int sysInit = assignCell(sysXvars, sysYvars, sysInitCell);
//		init = BDDWrapper.and(bdd, envInit, sysInit);
//		BDDWrapper.free(bdd, envInit);
//		BDDWrapper.free(bdd, sysInit);
//		
//				
//		System.out.println("init created at "+new Date().toString());
//				
//		sameRx = BDDWrapper.same(bdd, sysXvars, sysXvarsPrime);
//		sameRy = BDDWrapper.same(bdd, sysYvars, sysYvarsPrime);
//		sameOx = BDDWrapper.same(bdd, envXvars, envXvarsPrime);
//		sameOy = BDDWrapper.same(bdd, envYvars, envYvarsPrime);
//				
//		//define T_env and T_envList
//		sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
////		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
////		ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
//		T_env=-1;
//		T_envList = null;
//				
//		System.out.println("T_env created at "+new Date().toString());
//				
//		//define T_sys and T_sysList
//		sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
////		ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
//		T_sys = -1;
//		T_sysList = null;
//				
//		System.out.println("T_sys created at "+new Date().toString());
//				
//		HashMap<String , String> actionMap = new HashMap<String, String>();
//		actionMap.put("00", "up");
//		actionMap.put("01", "down");
//		actionMap.put("10", "left");
//		actionMap.put("11", "right");
//		
//		//TODO: check if we need to separate the environment and system actions
//		actions = enumerateActions();
//		envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
//		sysActions=UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
//		actionAvailabilitySets();
//	}
	
	public ImplicitGameCaseStudy(BDD argBDD, String argId, int dimension, GridCell2D envInitCell, 
			GridCell2D sysInitCell, Variable[] argEnvX, 
			Variable[] argEnvY, Variable[] argSysX, Variable[] argSysY, 
			Variable[] argEnvActs, Variable[] argSysActs){
		super(argBDD);
		id = argId;
		xDimension=dimension;
		yDimension=dimension;
		calculateNumberOfBits();
		
		defineVariables(argEnvX, argEnvY, argSysX, argSysY, argEnvActs, argSysActs);
		
		//define init
		int envInit = assignCell(envXvars, envYvars, envInitCell);
		int sysInit = assignCell(sysXvars, sysYvars, sysInitCell);
		init = BDDWrapper.and(bdd, envInit, sysInit);
		BDDWrapper.free(bdd, envInit);
		BDDWrapper.free(bdd, sysInit);
		
				
		System.out.println("init created at "+new Date().toString());
				
		sameRx = BDDWrapper.same(bdd, sysXvars, sysXvarsPrime);
		sameRy = BDDWrapper.same(bdd, sysYvars, sysYvarsPrime);
		sameOx = BDDWrapper.same(bdd, envXvars, envXvarsPrime);
		sameOy = BDDWrapper.same(bdd, envYvars, envYvarsPrime);
				
		//define T_env and T_envList
		sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
//		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
//		ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
		T_env=-1;
		T_envList = null;
				
		System.out.println("T_env created at "+new Date().toString());
				
		//define T_sys and T_sysList
		sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
//		ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
		T_sys = -1;
		T_sysList = null;
				
		System.out.println("T_sys created at "+new Date().toString());
				
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		//TODO: check if we need to separate the environment and system actions
		actions = enumerateActions();
		envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
		sysActions=UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
		actionAvailabilitySets();
		
	}
	
	
	public ImplicitGameCaseStudy(BDD argBDD, int xDim, int yDim, int argEnvXInit, int argEnvYInit, int argSysXInit, int argSysYInit){
		super(argBDD);
		xDimension=xDim;
		yDimension=yDim;
		calculateNumberOfBits();
		
		generateSimple2DRoboticGameWithOneAgent(argEnvXInit, argEnvYInit, argSysXInit, argSysYInit);
	}
	
	public void calculateNumberOfBits(){
		numOfXBits=UtilityMethods.numOfBits(xDimension-1);
		numOfYBits=UtilityMethods.numOfBits(yDimension-1);
	}
	
	private void defineVariables(Variable[] argEnvX, Variable[] argEnvY, Variable[] argSysX, Variable[] argSysY, Variable[] argEnvActs, Variable[] argSysActs){
		Variable[] ox = argEnvX;
		Variable[] oy = argEnvY;
		Variable[] rx = argSysX;
		Variable[] ry = argSysY;
		Variable[] oxPrime = Variable.getPrimedCopy(ox);
		Variable[] oyPrime = Variable.getPrimedCopy(oy);
		Variable[] rxPrime = Variable.getPrimedCopy(rx);
		Variable[] ryPrime = Variable.getPrimedCopy(ry);
		
		
		envXvars=ox;
		envXvarsPrime=oxPrime;
		
		envYvars=oy;
		envYvarsPrime=oyPrime;
		
		sysXvars=rx;
		sysXvarsPrime=rxPrime;
		
		sysYvars=ry;
		sysYvarsPrime=ryPrime;
		
		environmentVars= Variable.unionVariables(ox, oy);
		systemVars= Variable.unionVariables(rx, ry);
		
		environmentVarsPrime=Variable.unionVariables(oxPrime, oyPrime);
		systemVarsPrime=Variable.unionVariables(rxPrime, ryPrime);
		
		 variables= Variable.unionVariables(environmentVars, systemVars);
		 primedVariables=Variable.unionVariables(environmentVarsPrime, systemVarsPrime);
		
		 envActionVars = argEnvActs;
		 sysActionVars = argSysActs;
		 actionVars = Variable.unionVariables(envActionVars, sysActionVars);
		 
		 vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
	}

	public static void main(String[] args) {
//		BDD bdd = new BDD(10000,1000);
//		Simple2DRMPTestCaseGenerator tcg =  new Simple2DRMPTestCaseGenerator(bdd, 2,4);
//		Game g = tcg.generateSimple2DroboticGameWithOneAgent(1, 1, 0, 0);
//		g.printGame();
//		g.toGameGraph().draw("game.dot",1);
		
//		compareCSGuidedControlAlgs();
		
		test();
	}
	
	public static void test(){
		Date startDate = new Date();
		System.out.println("current time is "+startDate.toString());
		BDD bdd = new BDD(10000,1000);
		int dim = 32;
		
		ImplicitGameCaseStudy game = new ImplicitGameCaseStudy(bdd, dim, dim-1, dim-1, 0, 0);
		
		Variable[] ox = game.envXvars;
		Variable[] oy = game.envYvars;
		Variable[] rx = game.sysXvars;
		Variable[] ry = game.sysYvars;
	
//		System.out.println("checking the implicit game");
//		game.printGame();
		
//		for(int i=0;i<game.actions.length;i++){
//			System.out.println("action "+i+" is available at");
//			System.out.println("Environment state");
//			bdd.printSet(game.envAvlActions[i]);
//			System.out.println("System states");
//			bdd.printSet(game.sysAvlActions[i]);
//		}
		
		int objective = game.noCollisionObjective(Variable.getBDDVars(ox), Variable.getBDDVars(oy), Variable.getBDDVars(rx), Variable.getBDDVars(ry));
	
//		int[] abstractPredicates = {game.getInit(),objective};
//		
//		System.out.println("cs guided control");
//		System.out.println("current time is "+new Date().toString());
//		FastCSGuidedControl fcsg = new FastCSGuidedControl(bdd, game, abstractPredicates, objective);
//		GameSolution fcsSolution = fcsg.counterStrategyGuidedControl();
//		
//		fcsSolution.print();
		
		System.out.println("central");
		System.out.println("current time is "+new Date().toString());
		long t0 = UtilityMethods.timeStamp();
		GameSolver gs = new GameSolver(game,objective,bdd);
		System.out.println("solving the game");
		GameSolution sol = gs.solve();
		
		System.out.println("Game solved");
		sol.print();
		UtilityMethods.duration(t0, "central", 500);
//		sol.drawWinnerStrategy("strat.dot");
		
		BDDWrapper.BDD_Usage(bdd);
	}
	
	public void printGame(){
		System.out.println("printing the game "+id+" information ");
		System.out.println("variables");
		Variable.printVariables(variables);
		System.out.println("actions");
		Variable.printVariables(actionVars);
		UtilityMethods.debugBDDMethods(bdd, "init", init);
		
		System.out.println("T_env");
		printTransitionRelation(Player.ENVIRONMENT);
		System.out.println("T_sys");
		printTransitionRelation(Player.SYSTEM);
	}
	
	public void printTransitionRelation(Player p){
		while(hasNextTransition(p)){
			bdd.printSet(getNextTransition(p));
		}
	}
	
	public static void compareCSGuidedControlAlgs(){
		Date startDate = new Date();
		System.out.println("current time is "+startDate.toString());
		BDD bdd = new BDD(10000,1000);
		int dim = 33;
		Simple2DRMPTestCaseGenerator tcg =  new Simple2DRMPTestCaseGenerator(bdd, dim,dim);
		Game g = tcg.generateSimple2DRoboticGameWithOneAgent(0,0, dim-1, dim-1);
		
//		g.printGame();
//		g.toGameGraph().draw("game.dot",1);
//		UtilityMethods.getUserInput();
		
		Variable[] ox = tcg.envXvars.get(0);
		Variable[] oy = tcg.envYvars.get(0);
		Variable[] rx = tcg.sysXvars.get(0);
		Variable[] ry = tcg.sysYvars.get(0);
		
		int objective = tcg.noCollisionObjective(Variable.getBDDVars(ox), Variable.getBDDVars(oy), Variable.getBDDVars(rx), Variable.getBDDVars(ry));
		
		
		
		int[] abstractPredicates = {g.getInit(),objective};
		
//		CSGuidedControl csg = new CSGuidedControl(bdd, g, abstractPredicates, objective);
//		GameSolution csSolution = csg.counterStrategyGuidedControl();
//		
//		csSolution.print();
//		csSolution.drawWinnerStrategy("csSolution.dot"); 
		
		System.out.println("cs guided control");
		System.out.println("current time is "+new Date().toString());
		FastCSGuidedControl fcsg = new FastCSGuidedControl(bdd, g, abstractPredicates, objective);
		GameSolution fcsSolution = fcsg.counterStrategyGuidedControl();
		
		fcsSolution.print();
//		fcsSolution.drawWinnerStrategy("fcsSolution.dot");
		
		Date date = new Date();
//		System.out.println("current time is "+date.toString());
		
		System.out.println("central");
		System.out.println("current time is "+date.toString());
		long t0 = UtilityMethods.timeStamp();
		GameSolver gs = new GameSolver(g,objective,bdd);
		GameSolution sol = gs.solve();
		
		sol.print();
		UtilityMethods.duration(t0, "central", 500);
//		sol.drawWinnerStrategy("strat.dot");
		
		BDDWrapper.BDD_Usage(bdd);
		
	}
	
	public Game generateSimple2DRoboticGameWithOneAgent(){
		//define variables
				createVariablesOneAgent();
				
				Variable[] ox = envXvars;
				Variable[] oy = envYvars;
				Variable[] rx = sysXvars;
				Variable[] ry = sysYvars;
				
				//define init
				int sameX = BDDWrapper.same(bdd, ox, rx);
				int sameY = BDDWrapper.same(bdd, oy, ry);
				int same = BDDWrapper.and(bdd, sameX, sameY);
				int init = BDDWrapper.complement(bdd, same);
				
				bdd.deref(sameX);
				bdd.deref(sameY);
				bdd.deref(same);
				
				//define T_env and T_envList
				int sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
//				UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
				ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
				
				//define T_sys and T_sysList
				int sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
				ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
				
				HashMap<String , String> actionMap = new HashMap<String, String>();
				actionMap.put("00", "up");
				actionMap.put("01", "down");
				actionMap.put("10", "left");
				actionMap.put("11", "right");
				
				Game game = new Game(bdd, variables, primedVariables,  init, T_envList, T_sysList, actionVars);
				game.setActionMap(actionMap);
				return game;
	}
	
	public void generateSimple2DRoboticGameWithOneAgent(int argEnvXInit, int argEnvYInit, int argSysXInit, int argSysYInit){
		
		
		//define variables
		createVariablesOneAgent();
		
		Variable[] ox = envXvars;
		Variable[] oy = envYvars;
		Variable[] rx = sysXvars;
		Variable[] ry = sysYvars;
		
		Variable[] oxPrime = Variable.getPrimedCopy(ox);
		Variable[] oyPrime = Variable.getPrimedCopy(oy);
		Variable[] rxPrime = Variable.getPrimedCopy(rx);
		Variable[] ryPrime = Variable.getPrimedCopy(ry);
		
		
		//define init
		int envXInit=BDDWrapper.assign(bdd, argEnvXInit, ox);
		int envYInit=BDDWrapper.assign(bdd, argEnvYInit, oy);
		int envInit=bdd.ref(bdd.and(envXInit, envYInit));
		bdd.deref(envXInit);
		bdd.deref(envYInit);
				
		int sysXInit=BDDWrapper.assign(bdd, argSysXInit, rx);
		int sysYInit=BDDWrapper.assign(bdd, argSysYInit, ry);
		int sysInit=bdd.ref(bdd.and(sysXInit, sysYInit));
		bdd.deref(sysXInit);
		bdd.deref(sysYInit);
				
		init = bdd.ref(bdd.and(envInit, sysInit)); 
		bdd.deref(envInit);
		bdd.deref(sysInit);
		
		System.out.println("init created at "+new Date().toString());
		
		sameRx = BDDWrapper.same(bdd, rx, rxPrime);
		sameRy = BDDWrapper.same(bdd, ry, ryPrime);
		sameOx = BDDWrapper.same(bdd, ox, oxPrime);
		sameOy = BDDWrapper.same(bdd, oy, oyPrime);
		
		//define T_env and T_envList
		
		T_env = -1;
		T_sys = -1;
		
		sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
//		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
//		ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
		
//		System.out.println("T_env created at "+new Date().toString());
		
		//define T_sys and T_sysList
		sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
//		ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
		
//		System.out.println("T_sys created at "+new Date().toString());
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables),Variable.getBDDVars(variables));
		
		actions=enumerateActions();
		
//		systemActionAvailabilitySet();
//		environmentActionAvailabilitySet();
		
		//TODO: check if we need to separate the environment and system actions
				actions = enumerateActions();
				envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
				sysActions=UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
				actionAvailabilitySets();
		
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		setActionMap(actionMap);
	}
	
	public ArrayList<Integer> singleStepTransition(Variable[] xVars, Variable[] yVars, int samePart, int[] actions){
		BDDWrapper.BDD_Usage(bdd);
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		ArrayList<Integer> TList =  new ArrayList<Integer>();
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=0; i<xDimension;i++){
			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			//actions=00 -- up
			//x'=x-1 && y'=y
			if(i>0){
				
				int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[0]);
				trans=bdd.andTo(trans, samePart);
				
				
				bdd.deref(xPrime);
				
				TList.add(trans);
				
				
			}
			//actions=01 -- down
			if(i<xDimension-1){
				int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[1]);
				trans=bdd.andTo(trans, samePart);
			
				bdd.deref(xPrime);
				
				TList.add(trans);
				
				
			}
			bdd.deref(xState);
		}
		bdd.deref(yTrans);
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int j=0;j<yDimension;j++){
			System.out.println("yDim "+j);
			int yState = BDDWrapper.assign(bdd, j, yVars);
			//actions=10 -- left 
			if(j>0){
				
				int yPrime = BDDWrapper.assign(bdd, j-1, yVarsPrime);
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[2]);
				trans=bdd.andTo(trans, samePart);
				
				
				bdd.deref(yPrime);
				
				TList.add(trans);
				
				
			}
			//actions=11 -- right
			if(j<yDimension-1){
				int yPrime = BDDWrapper.assign(bdd, j+1, yVarsPrime);
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[3]);
				trans=bdd.andTo(trans, samePart);
				
				bdd.deref(yPrime);
				
				TList.add(trans);
				
				
			}
			bdd.deref(yState);
		}
		bdd.deref(xTrans);
		return TList;
	}
	
	private void createVariablesOneAgent(){
		Variable[] ox = Variable.createVariables(bdd, numOfXBits, "OX");
		Variable[] oy = Variable.createVariables(bdd, numOfYBits, "OY");
		Variable[] rx = Variable.createVariables(bdd, numOfXBits, "RX");
		Variable[] ry = Variable.createVariables(bdd, numOfYBits, "RY");
		Variable[] oxPrime = Variable.createPrimeVariables(bdd, ox);
		Variable[] oyPrime = Variable.createPrimeVariables(bdd, oy);
		Variable[] rxPrime = Variable.createPrimeVariables(bdd, rx);
		Variable[] ryPrime = Variable.createPrimeVariables(bdd, ry);
		
		
		envXvars=ox;
		envXvarsPrime=oxPrime;
		
		envYvars=oy;
		envYvarsPrime=oyPrime;
		
		sysXvars=rx;
		sysXvarsPrime=rxPrime;
		
		sysYvars=ry;
		sysYvarsPrime=ryPrime;
		
		environmentVars= Variable.unionVariables(ox, oy);
		systemVars= Variable.unionVariables(rx, ry);
		
		environmentVarsPrime=Variable.unionVariables(oxPrime, oyPrime);
		systemVarsPrime=Variable.unionVariables(rxPrime, ryPrime);
		
		 variables= Variable.unionVariables(environmentVars, systemVars);
		 primedVariables=Variable.unionVariables(environmentVarsPrime, systemVarsPrime);
		 
		 actionVars = Variable.createVariables(bdd, 2, "act");
		 actions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(actionVars));
		
		 envActionVars = actionVars;
		 sysActionVars = actionVars;
		 
//		 envActions=new ArrayList<int[]>();
//		 envActions.add(actions);
//		 sysActions=new ArrayList<int[]>();
//		 sysActions.add(actions);
	}
	
	/**
	 * given two variables (x1,y1) and (x2,y2) returns the formula \not \forall_i (x1[i] <-> x2[i] & y1[i] <-> y2[i]), no collision
	 * @param bdd
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public  int noCollisionObjective(int[] x1, int[] y1, int[] x2, int[] y2){
		int complementSafetyObjective=bdd.ref(bdd.getOne());
		for(int i=0;i<x1.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x1[i], x2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			bdd.deref(tmpX);
		}
		
		for(int i=0;i<y1.length;i++){
			int tmpY= bdd.ref(bdd.biimp(y1[i], y2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		bdd.deref(complementSafetyObjective);
		return safety;
	}
	
	public  int noCollisionObjective(Variable[] x1var, Variable[] y1var, Variable[] x2var, Variable[] y2var){
		int[] x1 = Variable.getBDDVars(x1var);
		int[] x2 = Variable.getBDDVars(x2var);
		int[] y1 = Variable.getBDDVars(y1var);
		int[] y2 = Variable.getBDDVars(y2var);
		
		int complementSafetyObjective=bdd.ref(bdd.getOne());
		for(int i=0;i<x1.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x1[i], x2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			bdd.deref(tmpX);
		}
		
		for(int i=0;i<y1.length;i++){
			int tmpY= bdd.ref(bdd.biimp(y1[i], y2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		bdd.deref(complementSafetyObjective);
		return safety;
	}
	
	
	public Game generateSimple2DRoboticGameWithOneAgentLarge(int argEnvXInit, int argEnvYInit, int argSysXInit, int argSysYInit){	
		//define variables
		createVariablesOneAgent();
		
		Variable[] ox = envXvars;
		Variable[] oy = envYvars;
		Variable[] rx = sysXvars;
		Variable[] ry = sysYvars;
		
		Variable[] oxPrime = Variable.getPrimedCopy(ox);
		Variable[] oyPrime = Variable.getPrimedCopy(oy);
		Variable[] rxPrime = Variable.getPrimedCopy(rx);
		Variable[] ryPrime = Variable.getPrimedCopy(ry);
		
		//define init
		int envXInit=BDDWrapper.assign(bdd, argEnvXInit, ox);
		int envYInit=BDDWrapper.assign(bdd, argEnvYInit, oy);
		int envInit=bdd.ref(bdd.and(envXInit, envYInit));
		bdd.deref(envXInit);
		bdd.deref(envYInit);
				
		int sysXInit=BDDWrapper.assign(bdd, argSysXInit, rx);
		int sysYInit=BDDWrapper.assign(bdd, argSysYInit, ry);
		int sysInit=bdd.ref(bdd.and(sysXInit, sysYInit));
		bdd.deref(sysXInit);
		bdd.deref(sysYInit);
				
		int init = bdd.ref(bdd.and(envInit, sysInit)); 
		bdd.deref(envInit);
		bdd.deref(sysInit);
		
		System.out.println("init created at "+new Date().toString());
		
		sameRx = BDDWrapper.same(bdd, rx, rxPrime);
		sameRy = BDDWrapper.same(bdd, ry, ryPrime);
		sameOx = BDDWrapper.same(bdd, ox, oxPrime);
		sameOy = BDDWrapper.same(bdd, oy, oyPrime);
		
		//define T_env and T_envList
		sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
//		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
//		ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
		
		System.out.println("T_env created at "+new Date().toString());
		
		//define T_sys and T_sysList
		sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
//		ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
		
		System.out.println("T_sys created at "+new Date().toString());
		
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		Game game = new Game(bdd, variables, primedVariables,  init, T_envList, T_sysList, actionVars);
		game.setActionMap(actionMap);
		
		System.out.println("game created at "+new Date().toString());
		
		return game;
	}
	
	public boolean hasNextTransition(Player p){
		if(p == Player.ENVIRONMENT){
//			if(envXindex < xDimension || envYindex < yDimension -1 || (envYindex == yDimension -1 && left)){
			
			if(envXindex < xDimension || envYindex < yDimension){
				return true;
			}
			resetEnvTransVars();
			return false;
		}else{
			
//			if(sysXindex < xDimension || sysYindex < yDimension -1 || (sysYindex == yDimension -1 && left)){
			
			if(sysXindex < xDimension || sysYindex < yDimension){
				return true;
			}
			
			resetSysTransVars();
			return false;
		}
	}
	
	public boolean hasNextTransitionWithoutReset(Player p){
		if(p == Player.ENVIRONMENT){
			if(envXindex < xDimension || envYindex < yDimension -1 || (envYindex == yDimension -1 && left)){
				return true;
			}
			return false;
		}else{
			if(sysXindex < xDimension || sysYindex < yDimension -1 || (sysYindex == yDimension -1 && left)){
				return true;
			}
			return false;
		}
	}
	
	public void resetEnvTransVars(){
		envXindex = 0;
		envYindex = 0;
		if(previousEnvTrans!=-1) bdd.deref(previousEnvTrans);
		previousEnvTrans = -1;
		up = true;
		left=true;
		envReset = true;
	}
	
	public void resetSysTransVars(){
		sysXindex = 0;
		sysYindex = 0;
		if(previousSysTrans!=-1) bdd.deref(previousSysTrans);
		previousSysTrans = -1;
		up = true;
		left=true;
		sysReset = true;
	}
	
	public int getNextTransition(Player p){
		if(p == Player.ENVIRONMENT){
			return getNextEnvTransition();
		}else{
			return getNextSysTransition();
		}
	}
	
	public int getNextEnvTransition(){
//		System.out.println("env x index is "+envXindex+", env y index is "+envYindex);
		
		if(!hasNextTransition(Player.ENVIRONMENT)){
			resetEnvTransVars();
			return -1;
		}
		
		envReset = false;
		
		Variable[] ox = envXvars;
		Variable[] oy = envYvars;
		Variable[] xVarsPrime=Variable.getPrimedCopy(ox);
		Variable[] yVarsPrime=Variable.getPrimedCopy(oy);
		
		if(envXindex < xDimension){
			int xState = BDDWrapper.assign(bdd, envXindex, ox);
			//actions=00 -- up
			//x'=x-1 && y'=y
//			if(envXindex == 0 && up){
//				up=!up;
//				
//			}else if(envXindex > 0 && up){
			if(up){
//				int xPrime = BDDWrapper.assign(bdd, envXindex-1, xVarsPrime);
				
				int xPrime;
				if(envXindex == 0 ){
					xPrime = BDDWrapper.assign(bdd, envXindex, xVarsPrime);
				}else{
					xPrime = BDDWrapper.assign(bdd, envXindex-1, xVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, sameOy);
				trans=bdd.andTo(trans, envActions[0]);
				trans=bdd.andTo(trans, sameSystemVars);
				
				bdd.deref(xPrime);
				bdd.deref(xState);
				
				up=!up;
				if(previousEnvTrans != -1) bdd.deref(previousEnvTrans);
				previousEnvTrans = trans;
				bdd.deref(xState);
				return trans;
			}
			
//			if(envXindex < xDimension -1 && !up){
			
			if(!up){
//				int xPrime = BDDWrapper.assign(bdd, envXindex+1, xVarsPrime);
				
				int xPrime;
				
				if(envXindex == xDimension -1 ){
					xPrime = BDDWrapper.assign(bdd, envXindex, xVarsPrime);
				}else{
					xPrime = BDDWrapper.assign(bdd, envXindex+1, xVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, sameOy);
				trans=bdd.andTo(trans, envActions[1]);
				trans=bdd.andTo(trans, sameSystemVars);
				
				bdd.deref(xPrime);
				bdd.deref(xState);
				
				up=!up;
				if(previousEnvTrans != -1) bdd.deref(previousEnvTrans);
				previousEnvTrans = trans;
				envXindex++;
				bdd.deref(xState);
				return trans;
			}
//			else{
//				envXindex++;
//			}
			
		}
		
		
//		if(envXindex == xDimension && !up && envYindex < yDimension){
		
		if(envXindex == xDimension && envYindex < yDimension){
//			System.out.println("yDim "+envYindex);
			int yState = BDDWrapper.assign(bdd, envYindex, oy);
//			if(envYindex==0 && left){
//				left = !left;
//			}else if(envYindex>0 && left){
			
			if(left){
			
//				int yPrime = BDDWrapper.assign(bdd, envYindex-1, yVarsPrime);
				
				int yPrime;
				if(envYindex == 0){
					yPrime = BDDWrapper.assign(bdd, envYindex, yVarsPrime);
				}else{
					yPrime = BDDWrapper.assign(bdd, envYindex-1, yVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, sameOx);
				trans=bdd.andTo(trans, envActions[2]);
				trans=bdd.andTo(trans, sameSystemVars);
				
				
				bdd.deref(yPrime);
				bdd.deref(yState);
				
				left = !left;
				if(previousEnvTrans != -1) bdd.deref(previousEnvTrans);
				previousEnvTrans = trans;
				bdd.deref(yState);
				return trans;
			}
			
//			if(envYindex<yDimension - 1 && !left){
			
			if(!left){
//				int yPrime = BDDWrapper.assign(bdd, envYindex+1, yVarsPrime);
				
				int yPrime;
				if(envYindex == yDimension -1){
					yPrime = BDDWrapper.assign(bdd, envYindex, yVarsPrime);
				}else{
					yPrime = BDDWrapper.assign(bdd, envYindex+1, yVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, sameOx);
				trans=bdd.andTo(trans, envActions[3]);
				trans=bdd.andTo(trans, sameSystemVars);
				
				bdd.deref(yPrime);
				bdd.deref(yState);
				
				left = !left;
				if(previousEnvTrans != -1) bdd.deref(previousEnvTrans);
				previousEnvTrans = trans;
				envYindex++;
				bdd.deref(yState);
				return trans;
			}
//			else{
//				envYindex++;
//			}
//			bdd.deref(yState);
		}
		
		//end of transitions
		resetEnvTransVars();
		return -1;
	}
	
	public int getNextSysTransition(){
//		System.out.println("sys x index is "+sysXindex+", sys y index is "+sysYindex);
		
		if(!hasNextTransition(Player.SYSTEM)){
			resetSysTransVars();
			return -1;
		}
		
		sysReset = false;
		
		Variable[] rx = sysXvars;
		Variable[] ry = sysYvars;
		Variable[] xVarsPrime=Variable.getPrimedCopy(rx);
		Variable[] yVarsPrime=Variable.getPrimedCopy(ry);
		
		if(sysXindex < xDimension){
			int xState = BDDWrapper.assign(bdd, sysXindex, rx);
			//actions=00 -- up
			//x'=x-1 && y'=y
//			if(sysXindex == 0 && up){
//				up=!up;
//				
//			}else if(sysXindex > 0 && up){
			
			if(up){
//				int xPrime = BDDWrapper.assign(bdd,sysXindex-1, xVarsPrime);
				
				int xPrime;
				if(sysXindex == 0){
					xPrime = BDDWrapper.assign(bdd,sysXindex, xVarsPrime);
				}else{
					xPrime = BDDWrapper.assign(bdd,sysXindex-1, xVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, sameRy);
				trans=bdd.andTo(trans, sysActions[0]);
				trans=bdd.andTo(trans, sameEnvironmentVars);
				
				bdd.deref(xPrime);
				bdd.deref(xState);
				
				up=!up;
				if(previousSysTrans != -1) bdd.deref(previousSysTrans);
				previousSysTrans = trans;
				bdd.deref(xState);
				return trans;
			}
			
//			if(sysXindex < xDimension -1 && !up){
			
			if(!up){
//				int xPrime = BDDWrapper.assign(bdd, sysXindex+1, xVarsPrime);
				
				int xPrime;
				if(sysXindex == xDimension -1 ){
					xPrime = BDDWrapper.assign(bdd, sysXindex, xVarsPrime);
				}else{
					xPrime = BDDWrapper.assign(bdd, sysXindex+1, xVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, sameRy);
				trans=bdd.andTo(trans, sysActions[1]);
				trans=bdd.andTo(trans, sameEnvironmentVars);
				
				bdd.deref(xPrime);
				bdd.deref(xState);
				
				up=!up;
				if(previousSysTrans != -1) bdd.deref(previousSysTrans);
				previousSysTrans = trans;
				sysXindex++;
				bdd.deref(xState);
				return trans;
			}
//			else{
//				sysXindex++;
//			}
//			bdd.deref(xState);
		}
		
		
//		if(sysXindex == xDimension && !up && sysYindex < yDimension){
		
		if(sysXindex == xDimension  && sysYindex < yDimension){
//			System.out.println("yDim "+sysYindex);
			int yState = BDDWrapper.assign(bdd, sysYindex, ry);
//			if(sysYindex==0 && left){
//				left = !left;
//			}else if(sysYindex>0 && left){
			
			if(left){
//				int yPrime = BDDWrapper.assign(bdd, sysYindex-1, yVarsPrime);
				
				int yPrime;
				if(sysYindex == 0){
					yPrime = BDDWrapper.assign(bdd, sysYindex, yVarsPrime);
				}else{
					yPrime = BDDWrapper.assign(bdd, sysYindex-1, yVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, sameRx);
				trans=bdd.andTo(trans, sysActions[2]);
				trans=bdd.andTo(trans, sameEnvironmentVars);
				
				
				bdd.deref(yPrime);
				bdd.deref(yState);
				
				left = !left;
				if(previousSysTrans != -1) bdd.deref(previousSysTrans);
				previousSysTrans = trans;
				bdd.deref(yState);
				return trans;
			}
			
//			if(sysYindex<yDimension - 1 && !left){
			
			if(!left){
//				int yPrime = BDDWrapper.assign(bdd, sysYindex+1, yVarsPrime);
				
				int yPrime;
				if(sysYindex == yDimension -1){
					yPrime = BDDWrapper.assign(bdd, sysYindex, yVarsPrime);
				}else{
					yPrime = BDDWrapper.assign(bdd, sysYindex+1, yVarsPrime);
				}
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, sameRx);
				trans=bdd.andTo(trans, sysActions[3]);
				trans=bdd.andTo(trans, sameEnvironmentVars);
				
				bdd.deref(yPrime);
				bdd.deref(yState);
				
				left = !left;
				if(previousSysTrans != -1) bdd.deref(previousSysTrans);
				previousSysTrans = trans;
				sysYindex++;
				bdd.deref(yState);
				return trans;
			}
//			else{
//				sysYindex++;
//			}
//			bdd.deref(yState);
		}
		
		//end of transitions
		resetSysTransVars();
		return -1;
	}
	
	public ArrayList<Integer> singleStepTransitionLarge(Variable[] xVars, Variable[] yVars, int samePart, int[] actions){
		BDDWrapper.BDD_Usage(bdd);
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		ArrayList<Integer> TList =  new ArrayList<Integer>();
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=0; i<xDimension;i++){
			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			//actions=00 -- up
			//x'=x-1 && y'=y
			if(i>0){
				
				int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
				
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[0]);
				trans=bdd.andTo(trans, samePart);
				
				
				bdd.deref(xPrime);
				
				TList.add(trans);
				
				
			}
			//actions=01 -- down
			if(i<xDimension-1){
				int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[1]);
				trans=bdd.andTo(trans, samePart);
			
				bdd.deref(xPrime);
				
				TList.add(trans);
				
				
			}
			bdd.deref(xState);
		}
		bdd.deref(yTrans);
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int j=0;j<yDimension;j++){
			System.out.println("yDim "+j);
			int yState = BDDWrapper.assign(bdd, j, yVars);
			//actions=10 -- left 
			if(j>0){
				
				int yPrime = BDDWrapper.assign(bdd, j-1, yVarsPrime);
				
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[2]);
				trans=bdd.andTo(trans, samePart);
				
				
				bdd.deref(yPrime);
				
				TList.add(trans);
				
				
			}
			//actions=11 -- right
			if(j<yDimension-1){
				int yPrime = BDDWrapper.assign(bdd, j+1, yVarsPrime);
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[3]);
				trans=bdd.andTo(trans, samePart);
				
				bdd.deref(yPrime);
				
				TList.add(trans);
				
				
			}
			bdd.deref(yState);
		}
		bdd.deref(xTrans);
		return TList;
	}
	
	public int EpreImage(int set, int cube, Player p){
		int preImage=bdd.ref(bdd.getZero());
		if(p == Player.ENVIRONMENT){
			while(hasNextTransition(p)){
				int trans = getNextEnvTransition();
				int pre = EpreImage(set,cube,trans);
				preImage=bdd.orTo(preImage, pre);
				bdd.deref(pre);
//				bdd.deref(trans);
			}
		}else{
			while(hasNextTransition(p)){
				int trans = getNextSysTransition();
				int pre = EpreImage(set,cube,trans);
				preImage=bdd.orTo(preImage, pre);
				bdd.deref(pre);
//				bdd.deref(trans);
			}
		}
		return preImage;
	}
	
	/**
	 * Computes the set of states where the action is available for the transition relation given in 
	 * disjunctive form.
	 * @param act
	 * @param transitionRelation
	 * @return
	 */
	public int actionAvailabilitySet(int act, Player p){
		int availabilitySet = bdd.ref(bdd.getZero());
		int cube = getPrimedVariablesAndActionsCube();
		if(p== Player.ENVIRONMENT){
			while(hasNextTransition(p)){
				int transitionRelation = getNextEnvTransition();
				int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				availabilitySet=bdd.orTo(availabilitySet, states);
				bdd.deref(states);
			}
			resetEnvTransVars();
		}else{
			while(hasNextTransition(p)){
				int transitionRelation = getNextSysTransition();
				int availableTransitions = bdd.ref(bdd.and(act, transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				availabilitySet=bdd.orTo(availabilitySet, states);
				bdd.deref(states);
			}
			resetSysTransVars();
		}
		return availabilitySet;
	}
	

	
	
	public void actionAvailabilitySets(){
		sysAvlActions = new int[sysActions.length];
		envAvlActions = new int[envActions.length];
		
		for(int i=0; i<sysActions.length;i++){
			sysAvlActions[i]= bdd.ref(bdd.getZero());
		}
		
		for(int i=0;i<envActions.length;i++){
			envAvlActions[i]= bdd.ref(bdd.getZero());
		}
		
//		int availabilitySet = bdd.ref(bdd.getZero());
		int cube = getPrimedVariablesAndActionsCube();

		while(hasNextTransition(Player.ENVIRONMENT)){
			int transitionRelation = getNextEnvTransition();
			
			for(int i=0;i<envActions.length;i++){
				int availableTransitions = bdd.ref(bdd.and(envActions[i], transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				envAvlActions[i]=bdd.orTo(envAvlActions[i], states);
				bdd.deref(states);
				
			}
		}
		resetEnvTransVars();
		
		while(hasNextTransition(Player.SYSTEM)){
			int transitionRelation = getNextSysTransition();
			for(int i=0;i<sysActions.length;i++){
				int availableTransitions = bdd.ref(bdd.and(sysActions[i], transitionRelation));
				int states=bdd.ref(bdd.exists(availableTransitions, cube));
				bdd.deref(availableTransitions);
				sysAvlActions[i]=bdd.orTo(sysAvlActions[i], states);
				bdd.deref(states);
				
			}
		}
		resetSysTransVars();
	}
	
	public int getEnvironmentTransitionRelation(){
		//if T_env is not computed 
		if(T_env==-1){
			System.out.println("computing environment transition relation");
			T_env=bdd.ref(bdd.getZero());
			while(hasNextTransition(Player.ENVIRONMENT)){
				int trans = getNextEnvTransition();
//				UtilityMethods.debugBDDMethods(bdd, "env next trans", trans);
				T_env = bdd.orTo(T_env, trans);
			}
		}
		return T_env;
	}
	
	public int getSystemTransitionRelation(){
		//if T_env is not computed 
		if(T_sys==-1){
			System.out.println("computing system transition relation");
			T_sys=bdd.ref(bdd.getZero());
			while(hasNextTransition(Player.SYSTEM)){
				int trans = getNextSysTransition();
				T_sys = bdd.orTo(T_sys, trans);
			}
		}
		return T_sys;
	}
	
	public int assignCell(Variable[] x, Variable[] y , GridCell2D cell){
		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
		int result=bdd.ref(bdd.and(xAssign, yAssing));
		bdd.deref(xAssign);
		bdd.deref(yAssing);
		return result;
	}
	
	public int noCollisionWithStaticObstacles(ArrayList<GridCell2D> staticObstacles){
		int noCollision = bdd.ref(bdd.getOne());
		for(GridCell2D obs : staticObstacles){
			int collisionWithObs = assignCell(sysXvars, sysYvars, obs);
			int noCollisionWithObs = bdd.ref(bdd.not(collisionWithObs));
			noCollision = bdd.andTo(noCollision, noCollisionWithObs);
			bdd.deref(collisionWithObs);
			bdd.deref(noCollisionWithObs);
		}
		return noCollision;
	}
	
	public ArrayList<Integer> getT_envList(){
		if(T_envList == null){
			T_envList=new ArrayList<Integer>();
			while(hasNextTransition(Player.ENVIRONMENT)) T_envList.add(getNextEnvTransition());
		}
		return T_envList;
	}
	
	public ArrayList<Integer> getT_sysList(){
		if(T_sysList == null){
			T_sysList= new ArrayList<Integer>();
			while(hasNextTransition(Player.SYSTEM)) T_sysList.add(getNextSysTransition());
		}
		return T_sysList;
	}
	
	public Variable[] getEnvXvars(){
		return envXvars;
	}
	
	public Variable[] getEnvYvars(){
		return envYvars;
	}
	
	public Variable[] getSysXvars(){
		return sysXvars;
	}
	
	public Variable[] getSysYvars(){
		return sysYvars;
	}
	
	public Game toGame(){
		ArrayList<Integer> envTransList=new ArrayList<Integer>();
		while(hasNextTransition(Player.ENVIRONMENT)){
			int trans = bdd.ref(getNextEnvTransition());
			envTransList.add(trans);
//			UtilityMethods.debugBDDMethods(bdd, "env trans", trans);
		}
		
		ArrayList<Integer> sysTransList=new ArrayList<Integer>();
		while(hasNextTransition(Player.SYSTEM)){
			int trans = bdd.ref(getNextSysTransition());
			sysTransList.add(trans);
//			UtilityMethods.debugBDDMethods(bdd, "sys trans", trans);
		}
		
		Game result = new Game(bdd, variables, primedVariables, getInit(), envTransList, sysTransList, envActionVars, sysActionVars);
		
		BDDWrapper.free(bdd, envTransList);
		BDDWrapper.free(bdd, sysTransList);
		
		return result; 
	}
	
	//TODO: remove from this part on
//	public void systemActionAvailabilitySet(){
//	sysAvlActions = new int[actions.length];
//	for(int i=0;i<actions.length;i++){
//		sysAvlActions[i]=actionAvailabilitySet(actions[i], Player.SYSTEM);
//	}		
//}
//
//public void environmentActionAvailabilitySet(){
//	envAvlActions = new int[actions.length];
//	for(int i=0;i<actions.length;i++){
//		envAvlActions[i]=actionAvailabilitySet(actions[i], Player.ENVIRONMENT);
//	}
//}
}


