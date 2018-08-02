package csguided;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jdd.bdd.BDD;
import utils.UtilityMethods;

/**
 * Generates simple 2D robotic motion planning test cases
 * @author moarref
 *
 */
public class Simple2DRMPTestCaseGenerator {
	
	BDD bdd;
	int xDimension;
	int yDimension;
	
	int numOfXBits;
	int numOfYBits;
	
	/**
	 * Environment (uncontrollable) agents X and Y variables and their primed copy
	 */
	ArrayList<Variable[]> envXvars;
	ArrayList<Variable[]> envXvarsPrime;
	
	ArrayList<Variable[]> envYvars;
	ArrayList<Variable[]> envYvarsPrime;
	
	Variable[] environmentVars;
	Variable[] environmentVarsPrime;
	
	/**
	 * System (controllable) agents X and Y variables and their primed copy
	 */
	ArrayList<Variable[]> sysXvars;
	ArrayList<Variable[]> sysXvarsPrime;
	
	ArrayList<Variable[]> sysYvars;
	ArrayList<Variable[]> sysYvarsPrime;
	
	Variable[] systemVars;
	Variable[] systemVarsPrime;
	
	Variable[] variables;
	Variable[] primeVariables;
	
	/**
	 * Action variables
	 */
	ArrayList<Variable[]> envActionVars;
	ArrayList<Variable[]> sysActionVars;
	Variable[] actionVars;
	
	ArrayList<int[]> envActions;
	ArrayList<int[]> sysActions;
	int[] actions;
	
	
	public Simple2DRMPTestCaseGenerator(BDD argBDD, int dimension){
		bdd=argBDD;
		xDimension=dimension;
		yDimension=dimension;
		calculateNumberOfBits();
	}
	
	public Simple2DRMPTestCaseGenerator(BDD argBDD, int xDim, int yDim){
		bdd=argBDD;
		xDimension=xDim;
		yDimension=yDim;
		calculateNumberOfBits();
	}
	
	public void calculateNumberOfBits(){
		numOfXBits=UtilityMethods.numOfBits(xDimension-1);
		numOfYBits=UtilityMethods.numOfBits(yDimension-1);
	}

	public static void main(String[] args) {
//		BDD bdd = new BDD(10000,1000);
//		Simple2DRMPTestCaseGenerator tcg =  new Simple2DRMPTestCaseGenerator(bdd, 2,4);
//		Game g = tcg.generateSimple2DroboticGameWithOneAgent(1, 1, 0, 0);
//		g.printGame();
//		g.toGameGraph().draw("game.dot",1);
		
		compareCSGuidedControlAlgs();
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
				
				Variable[] ox = envXvars.get(0);
				Variable[] oy = envYvars.get(0);
				Variable[] rx = sysXvars.get(0);
				Variable[] ry = sysYvars.get(0);
				
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
				
				Game game = new Game(bdd, variables, primeVariables,  init, T_envList, T_sysList, actionVars);
				game.setActionMap(actionMap);
				return game;
	}
	
	public Game generateSimple2DRoboticGameWithOneAgent(int argEnvXInit, int argEnvYInit, int argSysXInit, int argSysYInit){
		
		
		//define variables
		createVariablesOneAgent();
		
		Variable[] ox = envXvars.get(0);
		Variable[] oy = envYvars.get(0);
		Variable[] rx = sysXvars.get(0);
		Variable[] ry = sysYvars.get(0);
		
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
		
		//define T_env and T_envList
		int sameSystemVars = BDDWrapper.same(bdd, systemVars, systemVarsPrime);
//		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
		ArrayList<Integer> T_envList= singleStepTransition(ox, oy, sameSystemVars, actions);
		
		System.out.println("T_env created at "+new Date().toString());
		
		//define T_sys and T_sysList
		int sameEnvironmentVars=BDDWrapper.same(bdd, environmentVars, environmentVarsPrime);
		ArrayList<Integer> T_sysList= singleStepTransition(rx, ry, sameEnvironmentVars, actions);
		
		System.out.println("T_sys created at "+new Date().toString());
		
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		Game game = new Game(bdd, variables, primeVariables,  init, T_envList, T_sysList, actionVars);
		game.setActionMap(actionMap);
		
		System.out.println("game created at "+new Date().toString());
		
		return game;
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
		
		envXvars=new ArrayList<Variable[]>();
		envXvars.add(ox);
		envXvarsPrime=new ArrayList<Variable[]>();
		envXvarsPrime.add(oxPrime);
		
		envYvars=new ArrayList<Variable[]>();
		envYvars.add(oy);
		envYvarsPrime=new ArrayList<Variable[]>();
		envYvarsPrime.add(oyPrime);
		
		sysXvars=new ArrayList<Variable[]>();
		sysXvars.add(rx);
		sysXvarsPrime=new ArrayList<Variable[]>();
		sysXvarsPrime.add(rxPrime);
		
		sysYvars=new ArrayList<Variable[]>();
		sysYvars.add(ry);
		sysYvarsPrime=new ArrayList<Variable[]>();
		sysYvarsPrime.add(ryPrime);
		
		environmentVars= Variable.unionVariables(ox, oy);
		systemVars= Variable.unionVariables(rx, ry);
		
		environmentVarsPrime=Variable.unionVariables(oxPrime, oyPrime);
		systemVarsPrime=Variable.unionVariables(rxPrime, ryPrime);
		
		 variables= Variable.unionVariables(environmentVars, systemVars);
		 primeVariables=Variable.unionVariables(environmentVarsPrime, systemVarsPrime);
		 
		 actionVars = Variable.createVariables(bdd, 2, "act");
		 actions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(actionVars));
		
		 envActionVars=new ArrayList<Variable[]>();
		 envActionVars.add(actionVars);
		 sysActionVars=new ArrayList<Variable[]>();
		 sysActionVars.add(actionVars);
		 
		 envActions=new ArrayList<int[]>();
		 envActions.add(actions);
		 sysActions=new ArrayList<int[]>();
		 sysActions.add(actions);
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
	

}
