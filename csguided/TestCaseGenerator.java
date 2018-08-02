package csguided;

import java.util.ArrayList;
import java.util.HashMap;

import utils.UtilityMethods;
import automaton.GameGraph;
import jdd.bdd.BDD;

//TODO: make it coherent. The class must be able to generate testcases with different number of agents, 
// diffrent board sizes, etc. 
public class TestCaseGenerator {
	private int dim;
	Game game;
	
	int logDim;
	
	//environment variables
	Variable objX[];
	Variable objY[];
	
	//agent 1
	Variable x[];
	Variable y[];
	
	//prime variables
	Variable objXPrime[];
	Variable objYPrime[];
			
	Variable xprime[];
	Variable yprime[];
	
	BDD bdd;
	
	public int getDimension(){
		return dim;
	}
	
	public void setDimension(int argDim){
		dim=argDim;
	}
	
	public TestCaseGenerator(BDD argbdd){
		bdd=argbdd;
		
	}
	
	public TestCaseGenerator(BDD argbdd, int argDim){
		bdd=argbdd;
		
		dim=argDim;
		logDim = numOfBits(dim-1);
		
		//environment variables
		objX = new Variable[logDim];
		objY = new Variable[logDim];
		
		//agent 1
		x = new Variable[logDim];
		y = new Variable[logDim];
		
		//prime variables
		objXPrime = new Variable[logDim];
		objYPrime = new Variable[logDim];
				
		xprime = new Variable[logDim];
		yprime = new Variable[logDim];
	}
	
	public Game[] generateMultiAgentRMPTestCase(BDD bdd, int argDim){
		//two agents
		//Game[] agentGames = new Game[2];
		
		dim = argDim;
		logDim = numOfBits(dim-1);
		
		//environment variables
		Variable[] objX = new Variable[logDim];
		Variable[] objY = new Variable[logDim];
				
		//agent 1
		Variable[] x1 = new Variable[logDim];
		Variable[] y1 = new Variable[logDim];
		
		//agent 2
		Variable[] x2 = new Variable[logDim];
		Variable[] y2 = new Variable[logDim];
				
		//prime variables
		Variable[] objXPrime = new Variable[logDim];
		Variable[] objYPrime = new Variable[logDim];
						
		Variable[] x1prime = new Variable[logDim];
		Variable[] y1prime = new Variable[logDim];
		
		Variable[] x2prime = new Variable[logDim];
		Variable[] y2prime = new Variable[logDim];
		
		//createBDDVars
		for(int i=0;i<logDim;i++){
			objX[i]= new Variable(bdd, "oX"+i);
		}
								
		for(int i=0;i<logDim;i++){
			objY[i]= new Variable(bdd, "oY"+i);
		}
						
		for(int i=0;i<logDim;i++){
			x1[i]= new Variable(bdd, "X1"+i);
		}
								
		for(int i=0;i<logDim;i++){
			y1[i]= new Variable(bdd, "Y1"+i);
		}
		
		for(int i=0;i<logDim;i++){
			x2[i]= new Variable(bdd, "X2"+i);
		}
								
		for(int i=0;i<logDim;i++){
			y2[i]= new Variable(bdd, "Y2"+i);
		}
				
		for(int i=0;i<logDim;i++){
			objXPrime[i]= new Variable(bdd, "oX'"+i);
		}
						
		for(int i=0;i<logDim;i++){
			objYPrime[i]= new Variable(bdd, "oY'"+i);
		}
				
		for(int i=0;i<logDim;i++){
			x1prime[i]= new Variable(bdd, "X1'"+i);
		}
						
		for(int i=0;i<logDim;i++){
			y1prime[i]= new Variable(bdd, "Y1'"+i);
		}
		
		for(int i=0;i<logDim;i++){
			x2prime[i]= new Variable(bdd, "X2'"+i);
		}
						
		for(int i=0;i<logDim;i++){
			y2prime[i]= new Variable(bdd, "Y2'"+i);
		}
				
		//create actions
		//(act0,act1): (0,0)=up, (0,1)=down, (1,0)=left, (1,1)=right
		Variable act10 = new Variable(bdd, "act10");
		Variable act11 = new Variable(bdd, "act11");
		Variable[] actions1 = {act10, act11}; 
		int[] avl1 = new int[4];
		for(int i=0; i<4; i++){
			avl1[i]=bdd.getZero();
		}
				
		HashMap<String , String> actionMap1 = new HashMap<String, String>();
		actionMap1.put("00", "up");
		actionMap1.put("01", "down");
		actionMap1.put("10", "left");
		actionMap1.put("11", "right");
		
		Variable act20 = new Variable(bdd, "act20");
		Variable act21 = new Variable(bdd, "act21");
		Variable[] actions2 = {act20, act21}; 
		int[] avl2 = new int[4];
		for(int i=0; i<4; i++){
			avl2[i]=bdd.getZero();
		}
				
		HashMap<String , String> actionMap2 = new HashMap<String, String>();
		actionMap2.put("00", "up");
		actionMap2.put("01", "down");
		actionMap2.put("10", "left");
		actionMap2.put("11", "right");
				
				
		Variable[] inputVariables=concatenateArrays(objX, objY);
		Variable[] inputPrimeVars= concatenateArrays(objXPrime, objYPrime);
				
		Variable[] agent1OutputVariables=concatenateArrays(x1, y1);
		Variable[] agent1OutputPrimeVars=concatenateArrays(x1prime, y1prime);
		
		Variable[] agent2OutputVariables=concatenateArrays(x2, y2);
		Variable[] agent2OutputPrimeVars=concatenateArrays(x2prime, y2prime);
		
		Variable[] outputVariables = concatenateArrays(agent1OutputVariables, agent2OutputVariables);
		Variable[] outputVariablesPrime = concatenateArrays(agent1OutputPrimeVars, agent2OutputPrimeVars);
				
		//define initial state
		int objXInit=stateToBDD(Variable.getBDDVars(objX), dim-1);
		int objYInit=stateToBDD(Variable.getBDDVars(objY), dim-1);
		int objInit = bdd.ref(bdd.and(objXInit, objYInit));
				
		int x1Init=stateToBDD(Variable.getBDDVars(x1), 0);
		int y1Init=stateToBDD(Variable.getBDDVars(y1), 0);
		int agent1Init=bdd.ref(bdd.and(x1Init, y1Init));
		
		int x2Init=stateToBDD(Variable.getBDDVars(x2), 0);
		int y2Init=stateToBDD(Variable.getBDDVars(y2), dim-1);
		int agent2Init=bdd.ref(bdd.and(x2Init, y2Init));
		
		int init1 = bdd.ref(bdd.and(objInit, agent1Init));
		int init2=bdd.ref(bdd.and(objInit,  agent2Init));
		
		int init = bdd.ref(bdd.and(init1, init2));
				
				
		bdd.deref(x1Init);
		bdd.deref(y1Init);
		bdd.deref(objYInit);
		bdd.deref(objXInit);
		bdd.deref(objInit);
		bdd.deref(agent1Init);
		bdd.deref(x2Init);
		bdd.deref(y2Init);
		bdd.deref(agent2Init);
		
		//define transitions
		int objectTransitions1 = singleStepTransitionSystem(dim, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime), Variable.getBDDVars(actions1), avl1);
		objectTransitions1=bdd.andTo(objectTransitions1, same(Variable.getBDDVars(x1), Variable.getBDDVars(y1), Variable.getBDDVars(x1prime), Variable.getBDDVars(y1prime)));
		
		int objectTransitions2 = singleStepTransitionSystem(dim, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime), Variable.getBDDVars(actions2), avl2);
		objectTransitions2=bdd.andTo(objectTransitions2, same(Variable.getBDDVars(x2), Variable.getBDDVars(y2), Variable.getBDDVars(x2prime), Variable.getBDDVars(y2prime)));
		
		int agent1Transitions = singleStepTransitionSystem(dim, Variable.getBDDVars(x1), Variable.getBDDVars(y1), Variable.getBDDVars(x1prime), Variable.getBDDVars(y1prime), Variable.getBDDVars(actions1), avl1);
		agent1Transitions = bdd.andTo(agent1Transitions, same(Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime)));
//		Game game1= new Game(bdd, inputVariables, agent1OutputVariables, inputPrimeVars, agent1OutputPrimeVars, init1, objectTransitions1, agent1Transitions, actions1 , avl1, avl1);
		Game game1= new Game(bdd, Variable.unionVariables(inputVariables, outputVariables), Variable.unionVariables(inputPrimeVars, outputVariablesPrime), init, objectTransitions1, agent1Transitions, actions1);
		game1.setActionMap(actionMap1);
		
		int agent2Transitions = singleStepTransitionSystem(dim, Variable.getBDDVars(x2), Variable.getBDDVars(y2), Variable.getBDDVars(x2prime), Variable.getBDDVars(y2prime), Variable.getBDDVars(actions2), avl2);
		agent2Transitions = bdd.andTo(agent2Transitions, same(Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime)));
//		Game game2= new Game(bdd, inputVariables, agent2OutputVariables, inputPrimeVars, agent2OutputPrimeVars, init2, objectTransitions2, agent2Transitions, actions2 , avl2, avl2);
		Game game2= new Game(bdd, Variable.unionVariables(inputVariables, outputVariables), Variable.unionVariables(inputPrimeVars, outputVariablesPrime), init, objectTransitions2, agent2Transitions, actions2 );
		game2.setActionMap(actionMap2);
				
				
//		HashMap<String, String> stateMap1 = buildStateMap();
//		game.setStateMap(stateMap);
		
		Game[] agentGames  = {game1, game2};
		
		return agentGames;
		
	}

	public static void bddTest(){
		BDD bdd= new BDD(10000, 1000);
		int x=bdd.createVar();
		int y=bdd.createVar();
		
		int xp=bdd.createVar();
		int yp=bdd.createVar();
		
		int[] vars={x,y};
		int[] varsPrime={xp, yp};
		
		bdd.createPermutation(vars, varsPrime);
		
		int formula = bdd.ref(bdd.or(x, y));
		bdd.printCubes(formula);
		
		int ynot=bdd.ref(bdd.not(y));
		//int cube = y;
		int restrictedFormula=bdd.restrict(formula, ynot);
		bdd.printSet(restrictedFormula);
		
		if(bdd.member(formula, new boolean[]{true, false})){
			System.out.println("x=1 & y=0 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{false, true})){
			System.out.println("x=0 & y=1 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{false, false})){
			System.out.println("x=0 & y=0 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{true, true})){
			System.out.println("x=1 & y=1 is a member");
		}
		
		int cube=bdd.ref(x);
		int e=bdd.ref(bdd.exists(bdd.restrict(formula, cube), cube));
		bdd.printSet(e);
		
	}
	
	public static void main(String[] args) {
//		long t0 = System.currentTimeMillis();
//		
//		testGameWithActions(2);
//
//		long t1= System.currentTimeMillis();
//		System.out.println("Time spent "+(t1-t0));
		
		BDD bdd = new BDD(10000,1000);
		Game g = twoCellsRoboticGame(bdd);
//		GameGraph gg = g.removeUnreachableStates().toGameGraph("simpleGameForAbs");
//		gg.draw("simpleGame.dot", 1);
		

//		int targetStates=bdd.ref(bdd.and(g.variables[0].getBDDVar(), bdd.not(g.variables[1].getBDDVar())));
//		targetStates=bdd.andTo(targetStates, g.variables[2].getBDDVar());
//		
//		int targetStatesPrime=bdd.ref(bdd.and(g.primedVariables[0].getBDDVar(), bdd.not(g.primedVariables[1].getBDDVar())));
//		targetStatesPrime=bdd.andTo(targetStatesPrime, g.primedVariables[2].getBDDVar());
		
//		int targetStates=bdd.ref(bdd.and(g.variables[2].getBDDVar(), bdd.not(g.variables[1].getBDDVar())));
//		int targetStatesPrime=bdd.ref(bdd.and(g.primedVariables[2].getBDDVar(), bdd.not(g.primedVariables[1].getBDDVar())));
		
		int targetStates=bdd.ref(g.variables[1].getBDDVar());
		int targetStatesPrime=bdd.ref(g.primedVariables[1].getBDDVar());
		
		Game brGame=g.gameWithBoundedReachabilityObjective(20, "r", targetStates, targetStatesPrime);
//		brGame.removeUnreachableStates().toGameGraph().draw("brGame.dot", 1);
		
//		int objective=bdd.getOne();
		int objective=bdd.ref(bdd.not(bdd.biimp(g.variables[0].getBDDVar(), g.variables[1].getBDDVar())));
		int[] abstractPredicates={brGame.init, objective};
		
		long t0=System.currentTimeMillis();
		GameSolver gs=new GameSolver(brGame,objective,bdd);
		GameSolution sol = gs.solve();
		sol.print();
//		sol.drawWinnerStrategy("strat.dot");
//		sol.strategyOfTheWinner().printGame();
		long t1=System.currentTimeMillis();
		System.out.println("time is "+(t1-t0));
		
		System.out.println("cs guided control");
		
		CSGuidedControl csgc1=new CSGuidedControl(bdd, brGame, abstractPredicates, objective);
		t0=System.currentTimeMillis();
		GameSolution absSol1=csgc1.counterStrategyGuidedControl();
		t1=System.currentTimeMillis();
		System.out.println("time is "+(t1-t0));
		absSol1.print();
//		AbstractGame refinedAbsGame1 = csgc1.getAbstractGame();
//		refinedAbsGame1.removeUnreachableStates().toGameGraph().draw("refinedAbsGame1.dot", 1, 1);
//		Game absStrat1=absSol1.strategyOfTheWinner();
//		absStrat1.removeUnreachableStates().toGameGraph().draw("absStrat1.dot", 1, 1);
//		absStrat1.printGame();
		
//		TestCaseGenerator t =  new TestCaseGenerator(bdd, 3);
//		HashMap<String , String> map = t.buildStateMap();
//		
//		for(String state : map.keySet()){
//			System.out.println(state + " -> "+ map.get(state));
//		}
		
//		Game[] games=oneRowMultiAgentRMPGame(bdd);
//		games[0].printGame();
//		games[1].printGame();
//		
//		Variable[] oX = {games[0].variables[0]};
//		Variable[] r1 = {games[0].variables[1]};
//		Variable[] r2 = {games[0].variables[2]};
//		
//		int same_o_r1=BDDWrapper.same(bdd, oX, r1);
//		int obj1=bdd.ref(bdd.not(same_o_r1));
//		
//		int same_o_r2=BDDWrapper.same(bdd, oX, r2);
//		int obj2=bdd.ref(bdd.not(same_o_r2));
//		
//		int same_r1_r2=BDDWrapper.same(bdd, r1, r2);
//		//int obj3=bdd.ref(bdd.not(same_r1_r2));
//		int obj3=same_r1_r2;
////		
//////		int absPred=bdd.ref(bdd.or(obj1, obj2));
//////		absPred=bdd.orTo(absPred, obj3);
//		
//		int[] objectives = {obj1,obj2,obj3};
//		
//		int[] abstractPredicates={games[0].init, obj1, obj2,obj3};
////		int[] abstractPredicates={games[0].init, absPred};
//		
////		Game[] games=simpleMultiAgentRMPgame(bdd);
////		
//////		games[0].removeUnreachableStates();
//////		games[1].removeUnreachableStates();
////		
////		
////		Variable[] oX = {games[0].variables[0], games[0].variables[1]};
////		Variable[] r1 = {games[0].variables[3], games[0].variables[4]};
////		Variable[] r2 = {games[0].variables[6], games[0].variables[7]};
////		
////		int same_o_r1=BDDWrapper.same(bdd, oX, r1);
////		int obj1=bdd.ref(bdd.not(same_o_r1));
////		
////		int same_o_r2=BDDWrapper.same(bdd, oX, r2);
////		int obj2=bdd.ref(bdd.not(same_o_r2));
////		
////		int same_r1_r2=BDDWrapper.same(bdd, r1, r2);
////		int obj3=bdd.ref(bdd.not(same_r1_r2));
////		
////		int[] objectives = {obj1,obj2,obj3};
////		
////		int[] abstractPredicates={games[0].init, obj1, obj2,obj3};
//		
//		testGames(bdd, games, objectives, abstractPredicates);
		
	}
	
	public static void testGames(BDD bdd, Game[] games, int[] objectives, int[] abstractPredicates){

		
//		games[0].removeUnreachableStates().toGameGraph().draw("game0.dot", 1, 1);
//		games[1].removeUnreachableStates().toGameGraph().draw("game1.dot", 1, 1);
		
		System.out.println("testing the games");
		
		int[] eqClasses=BDDWrapper.getEquivalenceClassesFromPredicates(bdd, abstractPredicates);
		
		Game concreteGame=games[0].compose(games[1]);
		
//		concreteGame.removeUnreachableStates().toGameGraph().draw("concreteGame.dot", 1, 1);
		
		AbstractGame absGame0=games[0].computeAbstraction(eqClasses);
//		absGame0.removeUnreachableStates().toGameGraph().draw("absGame0.dot", 1, 1);
		
		AbstractGame absGame1=games[1].computeAbstraction(eqClasses);
//		absGame1.removeUnreachableStates().toGameGraph().draw("absGame1.dot", 1, 1);
		
		
		
		GameSolver gs1 = new GameSolver(games[0],objectives[0],bdd);
		GameSolution concSol1=gs1.solve();
		concSol1.print();
		Game concStrat1=concSol1.strategyOfTheWinner();
//		concStrat1.removeUnreachableStates().toGameGraph().draw("concSol1.dot", 1, 1);
		
		GameSolver gs2 = new GameSolver(games[1],objectives[1],bdd);
		GameSolution concSol2=gs2.solve();
		concSol2.print();
		Game concStrat2=concSol2.strategyOfTheWinner();
//		concStrat2.removeUnreachableStates().toGameGraph().draw("concSol2.dot", 1, 1);
		
		Game composedConcStrat=concStrat1.compose(concStrat2);
//		composedConcStrat.removeUnreachableStates().toGameGraph().draw("composedConcStrat.dot", 1, 1);
		
		GameSolver gs3=new GameSolver(composedConcStrat,objectives[2],bdd);
//		composedConcStrat.printGame();
//		UtilityMethods.debugBDDMethods(bdd, "obj3", objectives[2]);
		GameSolution finalSol=gs3.solve();
		finalSol.print();
		Game finalStrat=finalSol.strategyOfTheWinner();
//		finalStrat.printGame();
//		finalStrat.removeUnreachableStates().toGameGraph().draw("finalConcStrat.dot", 1, 1);
		
		System.out.println("\n\n CS guided Control\n\n");
		
		games[0].printGameVars();
		
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractPredicates, objectives[0]);
		GameSolution absSol1=csgc1.counterStrategyGuidedControl();
		absSol1.print();
//		AbstractGame refinedAbsGame1 = csgc1.getAbstractGame();
//		refinedAbsGame1.removeUnreachableStates().toGameGraph().draw("refinedAbsGame1.dot", 1, 1);
		Game absStrat1=absSol1.strategyOfTheWinner();
//		absStrat1.removeUnreachableStates().toGameGraph().draw("absStrat1.dot", 1, 1);
		
//		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractPredicates, objectives[1]);
//		GameSolution absSol2=csgc2.counterStrategyGuidedControl();
//		absSol2.print();
////		AbstractGame refinedAbsGame2 = csgc2.getAbstractGame();
////		refinedAbsGame2.removeUnreachableStates().toGameGraph().draw("refinedAbsGame2.dot", 1, 1);
//		Game absStrat2=absSol2.strategyOfTheWinner();
////		absStrat2.removeUnreachableStates().toGameGraph().draw("absStrat2.dot", 1, 1);
//		
//		Game composedAbsStrat=absStrat1.compose(absStrat2);
////		composedAbsStrat.removeUnreachableStates().toGameGraph().draw("composedAbsStrat.dot", 1, 1);
//		
//		GameSolver gs4=new GameSolver(composedAbsStrat,objectives[2],bdd);
//		GameSolution finalAbsSol=gs4.solve();
//		finalAbsSol.print();
//		Game finalAbsStrat=finalAbsSol.strategyOfTheWinner();
////		finalAbsStrat.printGame();
////		finalAbsStrat.removeUnreachableStates().toGameGraph().draw("finalAbsStrat.dot", 1, 1);
	}
	
	public static void testGameWithActions(int dim){
		BDD bdd = new BDD(10000,1000);
		TestCaseGenerator tcg=new TestCaseGenerator(bdd, dim);
		Game g = tcg.generateGameWithActions();
		
		System.out.println("printing T_env");
		bdd.printSet(g.T_env);
		
		System.out.println("printing T_sys");
		bdd.printSet(g.T_sys);
		
		System.out.println("printing avl(up)");
		bdd.printSet(g.sysAvlActions[0]);
		
		System.out.println("\nthe initial set is ");
		bdd.printSet(g.getInit());
		
		System.out.println("\nthe post image of initial set is ");
		int post = g.symbolicGameOneStepExecution(g.getInit());
		bdd.printSet(post);
		
		System.out.println("\nthe post image of the post");
		post = g.symbolicGameOneStepExecution(post);
		bdd.printSet(post);
		
		System.out.println("\ncontrollable predecessor of init");
		int cpre=g.controllablePredecessor(g.getInit());
		bdd.printSet(cpre);
		
		System.out.println("\ncontrollable predecessor of R=0");
		int[] sysVars=g.getOutputVariables();
		int sysLoc=bdd.getOne();
		for(int i=0;i<sysVars.length;i++){
			sysLoc=bdd.andTo(sysLoc, sysVars[i]);
		}
		cpre=g.controllablePredecessor(sysLoc);
		bdd.printSet(cpre);
		
		
		
		
	}

	private static int numOfBits(int number){
		int guess=(int) Math.floor(Math.log(number)/Math.log(2));
		if(Math.pow(2,guess)>number){
			
			return guess;
		}
		return guess+1;
		
	}
	
	public static int[] concatenateArrays(int[] arr1, int[] arr2){
		int length = arr1.length + arr2.length;
		int[] result=new int[length];
		int arr1length=arr1.length;
		for(int i=0;i<length;i++){
			if(i<arr1.length){
				result[i]=arr1[i];
			}else{
				result[i]=arr2[i-arr1length];
			}
		}
		return result;
	}
	
	public static Variable[] concatenateArrays(Variable[] arr1, Variable[] arr2){
		int length = arr1.length + arr2.length;
		Variable[] result=new Variable[length];
		int arr1length=arr1.length;
		for(int i=0;i<length;i++){
			if(i<arr1.length){
				result[i]=arr1[i];
			}else{
				result[i]=arr2[i-arr1length];
			}
		}
		return result;
	}
	
	public int stateToBDD(int[] variable, int value){
		int[] num = intToBinary(value);
		return stateToBDD(variable, num);
	}
	
	public int stateToBDD(int[] variable, int[] value){
		int result = bdd.ref(bdd.getOne());
		int[] valuePrime=value;
		if(variable.length > value.length){
			//append zero in front of value
			int[] zeroArray=new int[variable.length - value.length];
			for(int i=0; i<zeroArray.length; i++){
				zeroArray[i]=0;
			}
			valuePrime=concatenateArrays(zeroArray, valuePrime);
		}
		for(int i=0; i<variable.length;i++){
			int tmp;
			tmp = bdd.ref(valuePrime[i]==0 ? bdd.not(variable[i]) : variable[i]);
			result = bdd.andTo(result, tmp);
			bdd.deref(tmp);
		}
		return result;
	}
	
	public static int[] intToBinary(int num){
		String binary=Integer.toBinaryString(num);
		int length=binary.length();
		int[] result=new int[length];
		for(int i=0;i<length;i++){
			//result[length-1-i]=(binary.charAt(i)=='0'? 0 : 1);
			result[i]=(binary.charAt(i)=='0'? 0 : 1);
		}
		return result;
	}
	
	//TODO: just saying keep the system variables same for environment transitions is not enough
	//you should make sure the those variables are within bounds
	//for now keep the board size a power of 2
	public Game generateGameWithActions(){
		
		
		//createBDDVars
		for(int i=0;i<logDim;i++){
			objX[i]= new Variable(bdd, "oX"+i);
		}
						
		for(int i=0;i<logDim;i++){
			objY[i]= new Variable(bdd, "oY"+i);
		}
				
		for(int i=0;i<logDim;i++){
			x[i]= new Variable(bdd, "X"+i);
		}
						
		for(int i=0;i<logDim;i++){
			y[i]= new Variable(bdd, "Y"+i);
		}
		
		for(int i=0;i<logDim;i++){
			objXPrime[i]= new Variable(bdd, "oX'"+i);
		}
				
		for(int i=0;i<logDim;i++){
			objYPrime[i]= new Variable(bdd, "oY'"+i);
		}
		
		for(int i=0;i<logDim;i++){
			xprime[i]= new Variable(bdd, "X'"+i);
		}
				
		for(int i=0;i<logDim;i++){
			yprime[i]= new Variable(bdd, "Y'"+i);
		}
		
		//create actions
		//(act0,act1): (0,0)=up, (0,1)=down, (1,0)=left, (1,1)=right
		Variable act0 = new Variable(bdd, "act0");
		Variable act1 = new Variable(bdd, "act1");
		Variable[] actions = {act0, act1}; 
		int[] avl = new int[4];
		for(int i=0; i<4; i++){
			avl[i]=bdd.getZero();
		}
		
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		
		Variable[] inputVariables=concatenateArrays(objX, objY);
		Variable[] inputPrimeVars= concatenateArrays(objXPrime, objYPrime);
		
		Variable[] outputVariables=concatenateArrays(x, y);
		Variable[] outputPrimeVars=concatenateArrays(xprime, yprime);
		
		//define initial state
		int objXInit=stateToBDD(Variable.getBDDVars(objX), dim-1);
		int objYInit=stateToBDD(Variable.getBDDVars(objY), dim-1);
		int objInit = bdd.ref(bdd.and(objXInit, objYInit));
		
		int xInit=stateToBDD(Variable.getBDDVars(x), 0);
		int yInit=stateToBDD(Variable.getBDDVars(y), 0);
		int agentInit=bdd.ref(bdd.and(xInit, yInit));
		int init = bdd.ref(bdd.and(objInit, agentInit));
		
		
		bdd.deref(xInit);
		bdd.deref(yInit);
		bdd.deref(objYInit);
		bdd.deref(objXInit);
		bdd.deref(objInit);
		bdd.deref(agentInit);
		
		//define transitions
		int objectTransitions = singleStepTransitionSystem(dim, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime), Variable.getBDDVars(actions), avl);
		objectTransitions=bdd.andTo(objectTransitions, same(Variable.getBDDVars(x), Variable.getBDDVars(y), Variable.getBDDVars(xprime), Variable.getBDDVars(yprime)));
		int agentTransitions = singleStepTransitionSystem(dim, Variable.getBDDVars(x), Variable.getBDDVars(y), Variable.getBDDVars(xprime), Variable.getBDDVars(yprime), Variable.getBDDVars(actions), avl);
		agentTransitions = bdd.andTo(agentTransitions, same(Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime)));
		game= new Game(bdd, Variable.unionVariables(inputVariables, outputVariables), Variable.unionVariables(inputPrimeVars, outputPrimeVars), init, objectTransitions, agentTransitions, actions);
		
		game.setActionMap(actionMap);
		
		HashMap<String, String> stateMap = buildStateMap();
		game.setStateMap(stateMap);
		
		return game;
	}
	
	public HashMap<String, String> buildStateMap(){
		HashMap<String, String> stateMap=new HashMap<String, String>();
		int numOfVars=objX.length+objY.length+x.length+y.length;
		
		String[] states = UtilityMethods.enumerate(numOfVars);
		
		for(String state : states){
			String humanReadable = interpretState(state);
			stateMap.put(state, humanReadable);
		}
		
		return stateMap;
	}
	
	public String interpretState(String state){
		int varLength = x.length+y.length;
		String inputPart = state.substring(0, varLength);
		String outputPart = state.substring(varLength, state.length());
		String result = "("+Integer.parseInt(inputPart, 2)+","+Integer.parseInt(outputPart, 2)+")";
		return result;
	}
	
	
	public  int same(int[] x, int[] y, int[] xprime, int[] yprime){
		int T=bdd.ref(bdd.getOne());
		for(int i=0; i<x.length;i++){
			int xeq=bdd.ref(bdd.biimp(x[i], xprime[i]));
			T=bdd.andTo(T, xeq);
			int yeq = bdd.ref(bdd.biimp(y[i], yprime[i]));
			T=bdd.andTo(T, yeq);
			bdd.deref(xeq);
			bdd.deref(yeq);
		}
		return T;
	}
	
	public int singleStepTransitionSystem( int boardDimension, int[] x, int[] y, int[] xPrime, int[] yPrime, int[] actions, int[] avl){
		int T=bdd.ref(bdd.getZero());
		
		for(int i=0; i<boardDimension; i++){
			int currentX = stateToBDD(x, i);
			for(int j=0;j<boardDimension;j++){
				int currentY = stateToBDD(y, j);
				int current = bdd.ref(bdd.and(currentX, currentY));
				//bdd.deref(currentX);
				bdd.deref(currentY);
				
				int nextXtemp;
				int nextYtemp;
				int next;
				for(int ip=0;ip<boardDimension;ip++){
					for(int jp=0;jp<boardDimension; jp++){
						if(i==ip){
							if((j-jp)==1){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								//left move
								int act = bdd.ref(bdd.and(actions[0], bdd.not(actions[1])));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								bdd.deref(act);
								bdd.deref(next);
								//update avl(l)
								avl[2]=bdd.orTo(avl[2], current);
							}else if(jp - j == 1){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								//right move
								int act = bdd.ref(bdd.and(actions[0], actions[1]));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[3]=bdd.orTo(avl[3], current);
								bdd.deref(act);
								bdd.deref(next);
							}
						}else if((i-ip)==1){
							if(j==jp){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
							
								//up move
								int act = bdd.ref(bdd.and(bdd.not(actions[0]), bdd.not(actions[1])));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[0]=bdd.orTo(avl[0], current);
								bdd.deref(act);
								bdd.deref(next);
								
							}
						}else if((ip - i)==1){
							if(j==jp){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
							
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								
								
								//down move
								int act = bdd.ref(bdd.and(bdd.not(actions[0]), actions[1]));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[1]=bdd.orTo(avl[1], current);
								bdd.deref(act);
								bdd.deref(next);
							}
						}
					}
				}
				
//				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printSet(T);
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
			}
			
		}
		return T;
	}
	
	public int singleStepTransitionSystem( int boardXDimension, int boardYDimension, int[] x, int[] y, int[] xPrime, int[] yPrime, int[] actions, int[] avl){
		int T=bdd.getZero();
		
		for(int i=0; i<boardXDimension; i++){
			int currentX = stateToBDD(x, i);
			for(int j=0;j<boardYDimension;j++){
				int currentY = stateToBDD(y, j);
				int current = bdd.ref(bdd.and(currentX, currentY));
				//bdd.deref(currentX);
				bdd.deref(currentY);
				
				int nextXtemp;
				int nextYtemp;
				int next;
				for(int ip=0;ip<boardXDimension;ip++){
					for(int jp=0;jp<boardYDimension; jp++){
						if(i==ip){
							if((j-jp)==1){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								//left move
								int act = bdd.ref(bdd.and(actions[0], bdd.not(actions[1])));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								bdd.deref(act);
								bdd.deref(next);
								//update avl(l)
								avl[2]=bdd.orTo(avl[2], current);
							}else if(jp - j == 1){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								//right move
								int act = bdd.ref(bdd.and(actions[0], actions[1]));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[3]=bdd.orTo(avl[3], current);
								bdd.deref(act);
								bdd.deref(next);
							}
						}else if((i-ip)==1){
							if(j==jp){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
							
								//up move
								int act = bdd.ref(bdd.and(bdd.not(actions[0]), bdd.not(actions[1])));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[0]=bdd.orTo(avl[0], current);
								bdd.deref(act);
								bdd.deref(next);
								
							}
						}else if((ip - i)==1){
							if(j==jp){
								nextXtemp = stateToBDD(xPrime, ip);
								nextYtemp = stateToBDD(yPrime, jp);
								next=bdd.ref(bdd.and(nextXtemp, nextYtemp));
							
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								
								
								
								//down move
								int act = bdd.ref(bdd.and(bdd.not(actions[0]), actions[1]));
								int pre = bdd.and(current, act);
								T=bdd.orTo(T, bdd.and(pre, next));
								bdd.deref(pre);
								avl[1]=bdd.orTo(avl[1], current);
								bdd.deref(act);
								bdd.deref(next);
							}
						}
					}
				}
				
//				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printSet(T);
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
			}
			
		}
		return T;
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
	
	public int noCollisionObjective(){
		int[] xVars = Variable.getBDDVars(x);
		int[] yVars = Variable.getBDDVars(y);
		int[] oXVars = Variable.getBDDVars(objX);
		int[] oYVars = Variable.getBDDVars(objY);
		return noCollisionObjective(xVars, yVars, oXVars, oYVars);
	}
	
	public int[] safetyAbstractpredicates(){
		int numOfBits=x.length;
		
		int[] predicates=new int[2*numOfBits];
		
		for(int i=0; i<numOfBits; i++){
			predicates[i] = bdd.ref(bdd.biimp(x[i].getBDDVar(), objX[i].getBDDVar()));
			predicates[i+numOfBits]=bdd.ref(bdd.biimp(y[i].getBDDVar(), objY[i].getBDDVar()));
		}
		return predicates;
	}
	
	public int[] abstractPredicates(int init){
		int numOfBits=x.length;
		
		int[] predicates=new int[2*numOfBits+1];
		
		for(int i=0; i<numOfBits; i++){
			predicates[i] = bdd.ref(bdd.biimp(x[i].getBDDVar(), objX[i].getBDDVar()));
			predicates[i+numOfBits]=bdd.ref(bdd.biimp(y[i].getBDDVar(), objY[i].getBDDVar()));
		}
		
		predicates[2*numOfBits] = init;
		return predicates;
	}
	
	//generates the safety objective G(!(x=loc_x & y=loc_y))
	public int safetyObjective(int[] x, int[] y, int loc_x, int loc_y ){
		
		int xConstraint = stateToBDD(x, loc_x);
		int yConstraint = stateToBDD(y, loc_y);
		int complementObjective = bdd.ref(bdd.and(xConstraint, yConstraint));
		bdd.deref(xConstraint);
		bdd.deref(yConstraint);
		int objective = bdd.ref(bdd.not(complementObjective));
		bdd.deref(complementObjective);
		return objective;
		
	}
	
	public static Game twoCellsRoboticGame(BDD bdd){
		Variable ox=new Variable(bdd, "ox");
		Variable x=new Variable(bdd, "x");
		Variable oxPrime=new Variable(bdd, "oxPrime");
		Variable xPrime=new Variable(bdd, "xPrime");
		Variable act = new Variable(bdd,"l");
		
		Variable[] vars={ox,x};
		Variable[] primedVars={oxPrime,xPrime};
		Variable[] actions = {act};
		
		//init
		int init = bdd.ref(bdd.and(ox.getBDDVar(),bdd.not(x.getBDDVar())));
		
		//T_env
		int[] t=new int[8];
		t[0]=bdd.minterm("00000");
		t[1]=bdd.minterm("00101");
		t[2]=bdd.minterm("10000");
		t[3]=bdd.minterm("10101");
		t[4]=bdd.minterm("01010");
		t[5]=bdd.minterm("01111");
		t[6]=bdd.minterm("11010");
		t[7]=bdd.minterm("11111");
		
//		//no staying put
//		int[] t=new int[4];
//		t[0]=bdd.minterm("00101");
//		t[1]=bdd.minterm("10000");
//		t[2]=bdd.minterm("01111");
//		t[3]=bdd.minterm("11010");
		
		int envTrans=bdd.ref(bdd.getZero());
		for(int i=0;i<t.length;i++){
			envTrans=bdd.orTo(envTrans, t[i]);
			bdd.deref(t[i]);
		}
		
		
		//T_sys
		int[] s=new int[8];
		s[0]=bdd.minterm("00000");
		s[1]=bdd.minterm("00011");
		s[2]=bdd.minterm("10100");
		s[3]=bdd.minterm("10111");
		s[4]=bdd.minterm("01000");
		s[5]=bdd.minterm("01011");
		s[6]=bdd.minterm("11100");
		s[7]=bdd.minterm("11111");		
		
		int sysTrans=bdd.ref(bdd.getZero());
		for(int i=0;i<s.length;i++){
			sysTrans=bdd.orTo(sysTrans, s[i]);
			bdd.deref(s[i]);
		}
		
		Game game=new Game(bdd, vars, primedVars, init, envTrans, sysTrans, actions);
		return game;
	}
	
	public static Game simpleGame(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable l = new Variable(bdd, "l");
		
		Variable input[] = {a};
		Variable output[] = {b};
		Variable[] inputPrime={aPrime};
		Variable[] outputPrime={bPrime};
		Variable[] actions={l};
		int[] avl={};
		
		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		
		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t1=bdd.andTo(t1, l.getBDDVar());
		t1=bdd.andTo(t1, aPrime.getBDDVar());
		t1=bdd.andTo(t1, bPrime.getBDDVar());
		
		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
		t2=bdd.andTo(t2, bPrime.getBDDVar());
		
		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
		t3=bdd.andTo(t3, aPrime.getBDDVar());
		t3=bdd.andTo(t3, bPrime.getBDDVar());
		
		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
		t4=bdd.andTo(t4, bPrime.getBDDVar());
		
		int T_env = bdd.ref(bdd.or(t1,t2));
		T_env=bdd.orTo(T_env, t3);
		T_env=bdd.orTo(T_env, t4);
		
		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t5=bdd.andTo(t5, l.getBDDVar());
		t5=bdd.andTo(t5, aPrime.getBDDVar());
		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
		
		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t6=bdd.andTo(t6, l.getBDDVar());
		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
		
		
		int T_sys=bdd.ref(bdd.or(t5, t6));
		
		Game simpleGame=new Game(bdd, Variable.unionVariables(input, output), Variable.unionVariables(inputPrime, outputPrime), init, T_env, T_sys, actions);
		return simpleGame;
	}
	
	public static Game simple2DroboticGame(BDD bdd, int dimension){
		int numOfBits=UtilityMethods.numOfBits(dimension-1);
		
		//define variables
		Variable[] ox = Variable.createVariables(bdd, numOfBits, "OX");
		Variable[] oy = Variable.createVariables(bdd, numOfBits, "OY");
		Variable[] rx = Variable.createVariables(bdd, numOfBits, "RX");
		Variable[] ry = Variable.createVariables(bdd, numOfBits, "RY");
		Variable[] oxPrime = Variable.createPrimeVariables(bdd, ox);
		Variable[] oyPrime = Variable.createPrimeVariables(bdd, oy);
		Variable[] rxPrime = Variable.createPrimeVariables(bdd, rx);
		Variable[] ryPrime = Variable.createPrimeVariables(bdd, ry);
		
		Variable[] envVars= Variable.unionVariables(ox, oy);
		Variable[] sysVars= Variable.unionVariables(rx, ry);
		
		Variable[] envPrimeVars=Variable.unionVariables(oxPrime, oyPrime);
		Variable[] sysPrimeVars=Variable.unionVariables(rxPrime, ryPrime);
		
		Variable[] vars = Variable.unionVariables(envVars, sysVars);

		
		Variable[] primedVars = Variable.unionVariables(envPrimeVars, sysPrimeVars);
		
		Variable[] actionVars = Variable.createVariables(bdd, 2, "act");
		int[] actions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(actionVars));
		
		//define init
		int envXInit=BDDWrapper.assign(bdd, dimension-1, ox);
		int envYInit=BDDWrapper.assign(bdd, dimension-1, oy);
		int envInit=bdd.ref(bdd.and(envXInit, envYInit));
		bdd.deref(envXInit);
		bdd.deref(envYInit);
		
		int sysXInit=BDDWrapper.assign(bdd, 0, rx);
		int sysYInit=BDDWrapper.assign(bdd, 0, ry);
		int sysInit=bdd.ref(bdd.and(sysXInit, sysYInit));
		bdd.deref(sysXInit);
		bdd.deref(sysYInit);
		
		int init = bdd.ref(bdd.and(envInit, sysInit)); 
		bdd.deref(envInit);
		bdd.deref(sysInit);
		
		//define T_env and T_envList
		int sameSystemVars = BDDWrapper.same(bdd, sysVars, sysPrimeVars);
//		UtilityMethods.debugBDDMethods(bdd, "same system", sameSystemVars);
		ArrayList<Integer> T_envList= singleStepTransition(bdd, dimension, ox, oy, sameSystemVars, actions);
		
		//define T_sys and T_sysList
		int sameEnvironmentVars=BDDWrapper.same(bdd, envVars, envPrimeVars);
		ArrayList<Integer> T_sysList= singleStepTransition(bdd, dimension, rx, ry, sameEnvironmentVars, actions);
		
		HashMap<String , String> actionMap = new HashMap<String, String>();
		actionMap.put("00", "up");
		actionMap.put("01", "down");
		actionMap.put("10", "left");
		actionMap.put("11", "right");
		
		Game game = new Game(bdd, vars, primedVars,  init, T_envList, T_sysList, actionVars);
		game.setActionMap(actionMap);
		return game;
	}
	
	/**
	 * creates a transition relation for a dimension*dimension board
	 * @param bdd
	 * @param dimension
	 * @param vars
	 * @param primedVars
	 * @param samePart
	 * @param actionVars
	 * @return
	 */
	public static ArrayList<Integer> singleStepTransition(BDD bdd, int dimension, Variable[] xVars, Variable[] yVars, int samePart, int[] actions){
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		ArrayList<Integer> TList =  new ArrayList<Integer>();
		for(int i=0; i<dimension;i++){
			//actions=00 -- up
			//x'=x-1 && y'=y
			if(i>0){
				int xState = BDDWrapper.assign(bdd, i, xVars);
				int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
				int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[0]);
				trans=bdd.andTo(trans, samePart);
				TList.add(trans);
			}
			//actions=01 -- down
			if(i<dimension-1){
				int xState = BDDWrapper.assign(bdd, i, xVars);
				int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
				int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
				int trans = bdd.ref(bdd.and(xState,xPrime));
				trans=bdd.andTo(trans, yTrans);
				trans=bdd.andTo(trans, actions[1]);
				trans=bdd.andTo(trans, samePart);
				TList.add(trans);
			}
		}
		
		for(int j=0;j<dimension;j++){
			//actions=10 -- left 
			if(j>0){
				int yState = BDDWrapper.assign(bdd, j, yVars);
				int yPrime = BDDWrapper.assign(bdd, j-1, yVarsPrime);
				int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[2]);
				trans=bdd.andTo(trans, samePart);
				TList.add(trans);
			}
			//actions=11 -- right
			if(j<dimension-1){
				int yState = BDDWrapper.assign(bdd, j, yVars);
				int yPrime = BDDWrapper.assign(bdd, j+1, yVarsPrime);
				int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
				int trans = bdd.ref(bdd.and(yState,yPrime));
				trans=bdd.andTo(trans, xTrans);
				trans=bdd.andTo(trans, actions[3]);
				trans=bdd.andTo(trans, samePart);
				TList.add(trans);
			}
		}
		return TList;
	}
	
	public static Game simpleGameWith3varsList(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable cPrime=new Variable(bdd, "cPr");
		Variable l1 = new Variable(bdd, "l1");
		Variable l2 = new Variable(bdd, "l2");

		Variable[] inputVars={a};
		Variable[] primeInputVars={aPrime};
		
		Variable[] outputVars={b,c};
		Variable[] primeOutputVars={bPrime, cPrime};
		
		Variable[] vars={a,b,c};
		Variable[] primeVars={aPrime, bPrime, cPrime};
		Variable[] actionVars={l1,l2};
		
		int init = bdd.ref(bdd.minterm("000-----"));
		int te[] = new int[4];
		ArrayList<Integer> T_envList=new ArrayList<Integer>();
		te[0]=bdd.ref(bdd.minterm("0--0--00"));
		te[1]=bdd.ref(bdd.minterm("0--1--11"));
		te[2]=bdd.ref(bdd.minterm("1--0--00"));
		te[3]=bdd.ref(bdd.minterm("1--1--11"));
		
		int sameOutput = BDDWrapper.same(bdd, outputVars, primeOutputVars);
		
		int T_env = bdd.ref(bdd.getZero());
		for(int i=0;i<te.length;i++){
			te[i]=bdd.andTo(te[i], sameOutput);
			T_envList.add(te[i]);
			T_env=bdd.orTo(T_env, te[i]);
//			bdd.deref(te[i]);
		}
		
//		T_env=bdd.andTo(T_env, sameOutput);
		bdd.deref(sameOutput);
		
		int ts1[] = new int[4];
		ts1[0]=bdd.ref(bdd.minterm("-0--0-0-"));
		ts1[1]=bdd.ref(bdd.minterm("-0--1-1-"));
		ts1[2]=bdd.ref(bdd.minterm("-1--0-0-"));
		ts1[3]=bdd.ref(bdd.minterm("-1--1-1-"));

		int ts2[] = new int[4];
		ts2[0]=bdd.ref(bdd.minterm("--0--0-0"));
		ts2[1]=bdd.ref(bdd.minterm("--0--1-1"));
		ts2[2]=bdd.ref(bdd.minterm("--1--0-0"));
		ts2[3]=bdd.ref(bdd.minterm("--1--1-1"));
		
		int T_sys=bdd.ref(bdd.getZero());
		ArrayList<Integer> T_sysList=new ArrayList<Integer>();
		int sameInput=BDDWrapper.same(bdd, inputVars, primeInputVars);
		
		
		for(int i=0;i<ts1.length;i++){
			for(int j=0;j<ts2.length;j++){
				int disjunct = bdd.ref(bdd.and(ts1[i],ts2[j]));
				disjunct = bdd.andTo(disjunct, sameInput);
				T_sysList.add(disjunct);
				T_sys=bdd.orTo(T_sys, disjunct);
			}
		}
		
		bdd.deref(sameInput);
		
		
		
		Game simpleGame=new Game(bdd, vars, primeVars, init, T_env, T_sys, actionVars);
		simpleGame.setT_envList(T_envList);
		simpleGame.setT_sysList(T_sysList);
		
//		simpleGame.environmentActionAvailabilitySet();
//		simpleGame.systemActionAvailabilitySet();
		
//		int[] envActions=simpleGame.envAvlActions;
//		int[] sysActions=simpleGame.sysAvlActions;
//		
//		System.out.println("env avl set");
//		for(int avl : envActions ){
//			bdd.printSet(avl);
//		}
//		
//		System.out.println("sys avl set");
//		for(int avl : sysActions ){
//			bdd.printSet(avl);
//		}
		
		return simpleGame;
	}
	public static Game simpleGameWith3vars(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable cPrime=new Variable(bdd, "cPr");
		Variable l1 = new Variable(bdd, "l1");
		Variable l2 = new Variable(bdd, "l2");

		Variable[] inputVars={a};
		Variable[] primeInputVars={aPrime};
		
		Variable[] outputVars={b,c};
		Variable[] primeOutputVars={bPrime, cPrime};
		
		Variable[] vars={a,b,c};
		Variable[] primeVars={aPrime, bPrime, cPrime};
		Variable[] actionVars={l1,l2};
		
		int init = bdd.ref(bdd.minterm("000-----"));
		int te[] = new int[4];
		te[0]=bdd.ref(bdd.minterm("0--0--00"));
		te[1]=bdd.ref(bdd.minterm("0--1--11"));
		te[2]=bdd.ref(bdd.minterm("1--0--00"));
		te[3]=bdd.ref(bdd.minterm("1--1--11"));
		
		int sameOutput = BDDWrapper.same(bdd, outputVars, primeOutputVars);
		
		int T_env = bdd.ref(bdd.getZero());
		for(int i=0;i<te.length;i++){
			T_env=bdd.orTo(T_env, te[i]);
			bdd.deref(te[i]);
		}
		
		T_env=bdd.andTo(T_env, sameOutput);
		bdd.deref(sameOutput);
		
		int ts1[] = new int[4];
		ts1[0]=bdd.ref(bdd.minterm("-0--0-0-"));
		ts1[1]=bdd.ref(bdd.minterm("-0--1-1-"));
		ts1[2]=bdd.ref(bdd.minterm("-1--0-0-"));
		ts1[3]=bdd.ref(bdd.minterm("-1--1-1-"));

		int ts2[] = new int[4];
		ts2[0]=bdd.ref(bdd.minterm("--0--0-0"));
		ts2[1]=bdd.ref(bdd.minterm("--0--1-1"));
		ts2[2]=bdd.ref(bdd.minterm("--1--0-0"));
		ts2[3]=bdd.ref(bdd.minterm("--1--1-1"));
		
		int T_sys1 = bdd.ref(bdd.getZero());
		for(int i=0;i<ts1.length;i++){
			T_sys1=bdd.orTo(T_sys1, ts1[i]);
			bdd.deref(ts1[i]);
		}
		
		int T_sys2 = bdd.ref(bdd.getZero());
		for(int i=0;i<ts1.length;i++){
			T_sys2=bdd.orTo(T_sys2, ts2[i]);
			bdd.deref(ts2[i]);
		}
		
		int sameInput=BDDWrapper.same(bdd, inputVars, primeInputVars);
		
		int T_sys = bdd.ref(bdd.and(T_sys1, T_sys2));
		T_sys = bdd.andTo(T_sys, sameInput);
		bdd.deref(sameInput);
		bdd.deref(T_sys1);
		bdd.deref(T_sys2);
		
		
		
		Game simpleGame=new Game(bdd, vars, primeVars, init, T_env, T_sys, actionVars);
		return simpleGame;
	}
	
//	public static FairGame simpleFairGame(BDD bdd){
//		Variable a = new Variable(bdd, "a");
//		Variable b = new Variable(bdd, "b");
//		Variable aPrime=new Variable(bdd, "aPr");
//		Variable bPrime=new Variable(bdd, "bPr");
//		Variable l = new Variable(bdd, "l");
//		
//		Variable input[] = {a};
//		Variable output[] = {b};
//		Variable[] inputPrime={aPrime};
//		Variable[] outputPrime={bPrime};
//		Variable[] actions={l};
//		int[] avl={};
//		
//		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
//		
//		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
//		t1=bdd.andTo(t1, l.getBDDVar());
//		t1=bdd.andTo(t1, aPrime.getBDDVar());
//		t1=bdd.andTo(t1, bPrime.getBDDVar());
//		
//		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
//		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
//		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
//		t2=bdd.andTo(t2, bPrime.getBDDVar());
//		
//		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
//		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
//		t3=bdd.andTo(t3, aPrime.getBDDVar());
//		t3=bdd.andTo(t3, bPrime.getBDDVar());
//		
//		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
//		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
//		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
//		t4=bdd.andTo(t4, bPrime.getBDDVar());
//		
//		int T_env = bdd.ref(bdd.or(t1,t2));
//		T_env=bdd.orTo(T_env, t3);
//		T_env=bdd.orTo(T_env, t4);
//		
//		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
//		t5=bdd.andTo(t5, l.getBDDVar());
//		t5=bdd.andTo(t5, aPrime.getBDDVar());
//		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
//		
//		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
//		t6=bdd.andTo(t6, l.getBDDVar());
//		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
//		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
//		
//		
//		int T_sys=bdd.ref(bdd.or(t5, t6));
//		
//		FairGame simpleGame=new FairGame(bdd, input, output, inputPrime, outputPrime, init, T_env, T_sys, actions, avl, avl);
//		
//		int q11 = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
//		int q10 = bdd.ref(bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar())));
//		simpleGame.fairTransitions.add(q11);
//		simpleGame.fairTransitions.add(q10);
//		
//		return simpleGame;
//	}
	
	//unreachability test
	public static Game simpleGameTest2(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable l = new Variable(bdd, "l");
		
		Variable input[] = {a};
		Variable output[] = {b};
		Variable[] inputPrime={aPrime};
		Variable[] outputPrime={bPrime};
		Variable[] actions={l};
		int[] avl={};
		
		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		
		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t1=bdd.andTo(t1, l.getBDDVar());
		t1=bdd.andTo(t1, aPrime.getBDDVar());
		t1=bdd.andTo(t1, bPrime.getBDDVar());
		
		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
		t2=bdd.andTo(t2, bPrime.getBDDVar());
		
		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
		t3=bdd.andTo(t3, aPrime.getBDDVar());
		t3=bdd.andTo(t3, bPrime.getBDDVar());
		
		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
		t4=bdd.andTo(t4, bPrime.getBDDVar());
		
		int T_env = bdd.ref(bdd.or(t1,t2));
		T_env=bdd.orTo(T_env, t3);
		T_env=bdd.orTo(T_env, t4);
		
		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t5=bdd.andTo(t5, l.getBDDVar());
		t5=bdd.andTo(t5, aPrime.getBDDVar());
		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
		
		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t6=bdd.andTo(t6, l.getBDDVar());
		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
		
		int t7 = bdd.ref(bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar())));
		t7=bdd.andTo(t7, l.getBDDVar());
		t7=bdd.andTo(t7, aPrime.getBDDVar());
		t7=bdd.andTo(t7, bPrime.getBDDVar());
		
		int T_sys=bdd.ref(bdd.or(t5, t6));
		T_sys =bdd.orTo(T_sys, t7);
		
		//adding an environment transition
		int t8 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t8=bdd.andTo(t8, l.getBDDVar());
		t8=bdd.andTo(t8, bdd.not(aPrime.getBDDVar()));
		t8=bdd.andTo(t8, bdd.not(bPrime.getBDDVar()));
		
		T_env = bdd.orTo(T_env, t8);
		
		Game simpleGame=new Game(bdd, Variable.unionVariables(input, output), Variable.unionVariables(inputPrime, outputPrime), init, T_env, T_sys, actions);
		return simpleGame;
	}
	
	public static Game simpleNondeterministicGame(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable l = new Variable(bdd, "l");
		//Variable d = new Variable(bdd, "d");
		
		Variable[] vars = {a,b};
		Variable[] varPrime = {aPrime, bPrime};
		Variable[] actions={l};
		
		
		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		
//		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
//		t1=bdd.andTo(t1, l.getBDDVar());
//		t1=bdd.andTo(t1, aPrime.getBDDVar());
//		t1=bdd.andTo(t1, bPrime.getBDDVar());
		int t1=bdd.getZero();
		
		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
		t2=bdd.andTo(t2, bPrime.getBDDVar());
		
		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
		t3=bdd.andTo(t3, aPrime.getBDDVar());
		t3=bdd.andTo(t3, bPrime.getBDDVar());
		
		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
		t4=bdd.andTo(t4, bPrime.getBDDVar());
		
		int T_env = bdd.ref(bdd.or(t1,t2));
		T_env=bdd.orTo(T_env, t3);
		T_env=bdd.orTo(T_env, t4);
		
		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t5=bdd.andTo(t5, l.getBDDVar());
		t5=bdd.andTo(t5, aPrime.getBDDVar());
		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
		
		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t6=bdd.andTo(t6, l.getBDDVar());
		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
		
		int t7 = bdd.ref(bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar())));
		t7=bdd.andTo(t7, l.getBDDVar());
		t7=bdd.andTo(t7, aPrime.getBDDVar());
		t7=bdd.andTo(t7, bPrime.getBDDVar());
		
		int T_sys=bdd.ref(bdd.or(t5, t6));
		T_sys =bdd.orTo(T_sys, t7);
		
		int t9 = bdd.minterm("01101");
		T_sys =bdd.orTo(T_sys, t9);
		
		//adding an environment transition
		int t8 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t8=bdd.andTo(t8, l.getBDDVar());
		t8=bdd.andTo(t8, bdd.not(aPrime.getBDDVar()));
		t8=bdd.andTo(t8, bdd.not(bPrime.getBDDVar()));
		
		T_env = bdd.orTo(T_env, t8);
		
		Game simpleGame=new Game(bdd,vars, varPrime, init, T_env, T_sys, actions);
		return simpleGame;
	}
	
	public static Game simpleGame3(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable cPrime=new Variable(bdd, "cPr");
		Variable l = new Variable(bdd, "l");
		
		Variable[] variables={a,b,c};
		Variable[] variablesPrime={aPrime, bPrime, cPrime};
		Variable[] actions={l};
		int[] envAvl=new int[2];
		int[] sysAvl=new int[2];
		
		//define init
		int init = bdd.minterm("111----");
		
		//define environment transitions
		int t[]=new int[8];
		t[0]=bdd.minterm("1111111");
		t[1]=bdd.minterm("1111101");
		t[2]=bdd.minterm("1110110");
		t[3]=bdd.minterm("1110100");
		t[4]=bdd.minterm("1011110");
		t[5]=bdd.minterm("1011100");
		t[6]=bdd.minterm("0010110");
		t[7]=bdd.minterm("0010100");
		
		int envTrans=bdd.getZero();
		for(int i=0;i<t.length;i++){
			envTrans=bdd.orTo(envTrans, t[i]);
			bdd.deref(t[i]);
		}
		
		//define system transitions
		int[] s=new int[4];
		s[0]=bdd.minterm("1111011");
		s[1]=bdd.minterm("1101011");
		s[2]=bdd.minterm("0110011");
		s[3]=bdd.minterm("0100011");
		
		int sysTrans=bdd.ref(bdd.getZero());
		for(int i=0;i<s.length;i++){
			sysTrans=bdd.orTo(sysTrans, s[i]);
			bdd.deref(s[i]);
		}
		
		//define available actions for environment states
		//env states
		int e1=bdd.minterm("111----");
		int e2=bdd.minterm("101----");
		int e3=bdd.minterm("001----");
		//states where action l=0
		envAvl[0]=bdd.ref(bdd.or(e1, e2));
		//states where action l=1
		envAvl[1]=bdd.ref(bdd.or(e1,e3));
		
		bdd.deref(e1);
		bdd.deref(e2);
		bdd.deref(e3);
		
		//define available actions for system states		
		//sys states
		int s1=bdd.minterm("111----");
		int s2=bdd.minterm("110----");
		int s3=bdd.minterm("011----");
		int s4=bdd.minterm("010----");
		
		//states where action l=0
		sysAvl[0]=bdd.getZero();
		//states where action l=1
		sysAvl[1]=bdd.ref(bdd.or(s1,s2));
		sysAvl[1]=bdd.orTo(sysAvl[1], s3);
		sysAvl[1]=bdd.orTo(sysAvl[1], s4);
				
		bdd.deref(s1);
		bdd.deref(s2);
		bdd.deref(s3);
		bdd.deref(s4);
		
		Game game=new Game(bdd, variables, variablesPrime, init, envTrans, sysTrans, actions);
		return game;
	}
	
	
	public static Game[] oneRowMultiAgentRMPGame(BDD bdd){
		int dim=1;
		//create Variables
		Variable[] oX = Variable.createVariables(bdd, dim, "oX");
		Variable[] r1 = Variable.createVariables(bdd, dim, "r1");
		Variable[] r2 = Variable.createVariables(bdd, dim, "r2");
		
		Variable[] oXPrime=Variable.createPrimeVariables(bdd, oX);
		Variable[] r1Prime = Variable.createPrimeVariables(bdd, r1);
		Variable[] r2Prime = Variable.createPrimeVariables(bdd, r2);
		
		//create actions
		Variable[] actionVars1 = Variable.createVariables(bdd, dim, "a1");
		Variable[] actionVars2 = Variable.createVariables(bdd, dim, "a2");
		
		Variable[] vars=UtilityMethods.concatenateArrays(oX, r1);
		vars=UtilityMethods.concatenateArrays(vars, r2);
		
		Variable[] primeVars=UtilityMethods.concatenateArrays(oXPrime, r1Prime);
		primeVars = UtilityMethods.concatenateArrays(primeVars, r2Prime);
		
		//set action maps
		HashMap<String , String> actionMap1 = new HashMap<String, String>();
//		actionMap1.put("00", "0");
//		actionMap1.put("01", "1");
//		actionMap1.put("10", "2");
//		actionMap1.put("11", "3");
		actionMap1.put("0", "0");
		actionMap1.put("1", "1");

		
		HashMap<String , String> actionMap2 = new HashMap<String, String>();
//		actionMap2.put("00", "0");
//		actionMap2.put("01", "1");
//		actionMap2.put("10", "2");
//		actionMap2.put("11", "3");
		actionMap2.put("0", "0");
		actionMap2.put("1", "1");
		
		//define init
		int objInit=BDDWrapper.assign(bdd, 0, oX);
		int r1Init=BDDWrapper.assign(bdd, 1, r1);
		int r2Init=BDDWrapper.assign(bdd, 1, r2);
		int init = bdd.ref(bdd.and(objInit, r1Init));
		init=bdd.andTo(init, r2Init);
		
		
		//same formulas to keep variables unchanged during a transition
		int sameEnvVars=BDDWrapper.same(bdd, oX, oXPrime);
		int sameAgent1Vars=BDDWrapper.same(bdd, r1, r1Prime);
		int sameAgent2Vars=BDDWrapper.same(bdd, r2, r2Prime);
		
		//define T_env
		int T_env1=generateTransOneRow(bdd, 2, oX, oXPrime, actionVars1);
		T_env1=bdd.ref(bdd.andTo(T_env1, sameAgent1Vars));
		
		int T_env2=generateTransOneRow(bdd, 2, oX, oXPrime, actionVars2);
		T_env2=bdd.ref(bdd.andTo(T_env2, sameAgent2Vars));
		
		//define T_sys
		int T_sys1=generateTransOneRow(bdd, 2, r1, r1Prime, actionVars1);
		T_sys1=bdd.ref(bdd.andTo(T_sys1, sameEnvVars));
		
		
		int T_sys2=generateTransOneRow(bdd, 2, r2, r2Prime, actionVars2);
		T_sys2=bdd.ref(bdd.andTo(T_sys2, sameEnvVars));
		
		//define games
		Game game1= new Game(bdd, vars, primeVars, init, T_env1, T_sys1, actionVars1);
		game1.setActionMap(actionMap1);
		Game game2= new Game(bdd, vars, primeVars, init, T_env2, T_sys2, actionVars2);
		game2.setActionMap(actionMap2);
		
		Game[] games={game1,game2};
		return games;
	}
	
	public static int generateTransOneRow(BDD bdd, int dim, Variable[] vars, Variable[] primeVars, Variable[] actionVars){
		int transitions=bdd.getZero();
		for(int i=0;i<dim;i++){
			int pre = BDDWrapper.assign(bdd, i, vars);
			for(int j=0;j<dim;j++){
				//if(i==j) continue;
				int post = BDDWrapper.assign(bdd, j, primeVars);
				int action = BDDWrapper.assign(bdd, j, actionVars);
				int trans=bdd.ref(bdd.and(pre, action));
				trans=bdd.andTo(trans, post);
				bdd.deref(post);
				bdd.deref(action);
				transitions=bdd.ref(bdd.orTo(transitions, trans));
				bdd.deref(trans);
			}
			bdd.deref(pre);
		}
		return transitions;
	}
	
	//simple robot motion planning game
	public static Game[] simpleMultiAgentRMPgame(BDD bdd){
		
		TestCaseGenerator tcg=new TestCaseGenerator(bdd);
		
		//create variables
		Variable oX0 = new Variable(bdd, "oX0");
		Variable oX1 = new Variable(bdd, "oX1");
		Variable oY0 = new Variable(bdd, "oY0");
		
		Variable x1_0 = new Variable(bdd, "x1_0");
		Variable x1_1 = new Variable(bdd, "x1_1");
		Variable y1_0 = new Variable(bdd, "y1_0");
		
		Variable x2_0 = new Variable(bdd, "x2_0");
		Variable x2_1 = new Variable(bdd, "x2_1");
		Variable y2_0 = new Variable(bdd, "y2_0");
		
		Variable oX0Prime = new Variable(bdd, "oX0'");
		Variable oX1Prime  = new Variable(bdd, "oX1'");
		Variable oY0Prime  = new Variable(bdd, "oY0'");
		
		Variable x1_0Prime  = new Variable(bdd, "x1_0'");
		Variable x1_1Prime  = new Variable(bdd, "x1_1'");
		Variable y1_0Prime  = new Variable(bdd, "y1_0'");
		
		Variable x2_0Prime  = new Variable(bdd, "x2_0'");
		Variable x2_1Prime  = new Variable(bdd, "x2_1'");
		Variable y2_0Prime  = new Variable(bdd, "y2_0'");
		
		Variable[] objX = {oX0, oX1};
		Variable[] objY = {oY0};
		
		Variable[] objXPrime = {oX0Prime, oX1Prime};
		Variable[] objYPrime = {oY0Prime};
		
		Variable[] x1= {x1_0, x1_1};
		Variable[] y1 = {y1_0};
		
		Variable[] x1prime= {x1_0Prime, x1_1Prime};
		Variable[] y1prime = {y1_0Prime};
		
		Variable[] x2= {x2_0, x2_1};
		Variable[] y2 = {y2_0};
		
		Variable[] x2prime= {x2_0Prime, x2_1Prime};
		Variable[] y2prime = {y2_0Prime};
		
		Variable[] inputVariables = {oX0,oX1,oY0};
		Variable[] inputPrimeVars={oX0Prime, oX1Prime, oY0Prime};
		
		Variable[] agent1OutputVariables = {x1_0, x1_1, y1_0};
		Variable[] agent1OutputPrimeVars = {x1_0Prime, x1_1Prime, y1_0Prime};
		
		Variable[] agent2OutputVariables = {x2_0, x2_1, y2_0};
		Variable[] agent2OutputPrimeVars = {x2_0Prime, x2_1Prime, y2_0Prime};
		
		Variable[] outputVariables = {x1_0, x1_1, y1_0, x2_0, x2_1, y2_0};
		Variable[] outputVariablesPrime = {x1_0Prime, x1_1Prime, y1_0Prime, x2_0Prime, x2_1Prime, y2_0Prime};
		
		//create action variables
		//create actions
		//(act0,act1): (0,0)=up, (0,1)=down, (1,0)=left, (1,1)=right
		Variable act10 = new Variable(bdd, "act10");
		Variable act11 = new Variable(bdd, "act11");
		Variable[] actions1 = {act10, act11}; 
		int[] avl1 = new int[4];
		for(int i=0; i<4; i++){
			avl1[i]=bdd.getZero();
		}
						
		HashMap<String , String> actionMap1 = new HashMap<String, String>();
		actionMap1.put("00", "up");
		actionMap1.put("01", "down");
		actionMap1.put("10", "left");
		actionMap1.put("11", "right");
				
		Variable act20 = new Variable(bdd, "act20");
		Variable act21 = new Variable(bdd, "act21");
		Variable[] actions2 = {act20, act21}; 
		int[] avl2 = new int[4];
		for(int i=0; i<4; i++){
			avl2[i]=bdd.getZero();
		}
						
		HashMap<String , String> actionMap2 = new HashMap<String, String>();
		actionMap2.put("00", "up");
		actionMap2.put("01", "down");
		actionMap2.put("10", "left");
		actionMap2.put("11", "right");
		
		//define init
		//define initial state
		int objInit=bdd.ref(bdd.and(bdd.not(oX0.getBDDVar()), oX1.getBDDVar()));
		objInit = bdd.andTo(objInit, oY0.getBDDVar());
		
		int agent1Init=bdd.ref(bdd.and(bdd.not(x1_0.getBDDVar()), bdd.not(x1_1.getBDDVar())));
		agent1Init = bdd.andTo(agent1Init, y1_0.getBDDVar());
		
		int agent2Init=bdd.ref(bdd.and(x2_0.getBDDVar(), bdd.not(x2_1.getBDDVar())));
		agent2Init = bdd.andTo(agent2Init, y2_0.getBDDVar());
				
		int init1 = bdd.ref(bdd.and(objInit, agent1Init));
		int init2=bdd.ref(bdd.and(objInit,  agent2Init));
		
		int init = bdd.ref(bdd.and(init1, init2));

		bdd.deref(objInit);
		bdd.deref(agent1Init);
		bdd.deref(agent2Init);
		
		int xDim=3;
		int yDim=2;
		
		//define env transitions

		
		int preX1 = BDDWrapper.assign(bdd, 1, objX);
		int preY1 = BDDWrapper.assign(bdd, 1, objY);
		int pre1=bdd.ref(bdd.and(preX1, preY1));
		int actLeft1 = BDDWrapper.assign(bdd, 2, actions1);
		int actLeft2 = BDDWrapper.assign(bdd, 2, actions2);
		int postX1 = BDDWrapper.assign(bdd, 1, objXPrime);
		int postY1 = BDDWrapper.assign(bdd, 0, objYPrime);
		int post1=bdd.ref(bdd.and(postX1, postY1));
		
		int transLeft1=bdd.ref(bdd.and(pre1, actLeft1));
		transLeft1=bdd.ref(bdd.andTo(transLeft1, post1));
		
		int transLeft2=bdd.ref(bdd.and(pre1, actLeft2));
		transLeft2=bdd.ref(bdd.andTo(transLeft2, post1));
		
		int preY2= BDDWrapper.assign(bdd, 0, objY);
		int actRight1 = BDDWrapper.assign(bdd, 3, actions1);
		int actRight2 = BDDWrapper.assign(bdd, 3, actions2);
		int postY2 = BDDWrapper.assign(bdd, 1, objYPrime);
		int pre2=bdd.ref(bdd.and(preX1, preY2));
		int post2=bdd.ref(bdd.and(postX1, postY2));
		
		int transRight1=bdd.ref(bdd.and(pre2, actRight1));
		transRight1=bdd.ref(bdd.andTo(transRight1, post2));
		
		int transRight2=bdd.ref(bdd.and(pre2, actRight2));
		transRight2=bdd.ref(bdd.andTo(transRight2, post2));
		
		int objectTransitions1=bdd.ref(bdd.or(transLeft1, transRight1));
		int objectTransitions2=bdd.ref(bdd.or(transLeft2, transRight2));
		
		UtilityMethods.debugBDDMethods(bdd, "object trans 1", objectTransitions1);
		UtilityMethods.debugBDDMethods(bdd, "object trans 2", objectTransitions2);
		
		
//		int objectTransitions1 = tcg.singleStepTransitionSystem(xDim, yDim, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime), Variable.getBDDVars(actions1), avl1);
		objectTransitions1=bdd.andTo(objectTransitions1, BDDWrapper.same(bdd,Variable.getBDDVars(x1), Variable.getBDDVars(y1), Variable.getBDDVars(x1prime), Variable.getBDDVars(y1prime)));
		
//		int objectTransitions2 = tcg.singleStepTransitionSystem(xDim, yDim, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime), Variable.getBDDVars(actions2), avl2);
		objectTransitions2=bdd.andTo(objectTransitions2, BDDWrapper.same(bdd, Variable.getBDDVars(x2), Variable.getBDDVars(y2), Variable.getBDDVars(x2prime), Variable.getBDDVars(y2prime)));
		
		//define system transitions
		int agent1Transitions = tcg.singleStepTransitionSystem(xDim, yDim, Variable.getBDDVars(x1), Variable.getBDDVars(y1), Variable.getBDDVars(x1prime), Variable.getBDDVars(y1prime), Variable.getBDDVars(actions1), avl1);
		agent1Transitions = bdd.andTo(agent1Transitions, BDDWrapper.same(bdd, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime)));
		Game game1= new Game(bdd, Variable.unionVariables(inputVariables, outputVariables), Variable.unionVariables(inputPrimeVars, outputVariablesPrime), init, objectTransitions1, agent1Transitions, actions1);
		game1.setActionMap(actionMap1);
		
		int agent2Transitions = tcg.singleStepTransitionSystem(xDim, yDim, Variable.getBDDVars(x2), Variable.getBDDVars(y2), Variable.getBDDVars(x2prime), Variable.getBDDVars(y2prime), Variable.getBDDVars(actions2), avl2);
		agent2Transitions = bdd.andTo(agent2Transitions, BDDWrapper.same(bdd, Variable.getBDDVars(objX), Variable.getBDDVars(objY), Variable.getBDDVars(objXPrime), Variable.getBDDVars(objYPrime)));
		Game game2= new Game(bdd, Variable.unionVariables(inputVariables, outputVariables), Variable.unionVariables(inputPrimeVars, outputVariablesPrime), init, objectTransitions2, agent2Transitions, actions2 );
		game2.setActionMap(actionMap2);
		
		
		//define game

		Game[] agentGames  = {game1, game2};
		
		return agentGames;
	}
	
	
	public static Game[] simpleGames(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable d = new Variable(bdd, "d");
		Variable e = new Variable(bdd, "e");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable cPrime=new Variable(bdd, "cPr");
		Variable dPrime=new Variable(bdd, "dPr");
		Variable ePrime=new Variable(bdd, "ePr");
		Variable l1 = new Variable(bdd, "l");
		Variable l2 = new Variable(bdd, "l");
		
		Variable[] a1Variables={a,b,c};
		Variable[] a1VariablesPrime={aPrime, bPrime, cPrime};
		Variable[] actions1={l1};
		
		Variable[] a2Variables={a,d,e};
		Variable[] a2VariablesPrime={aPrime, dPrime, ePrime};
		Variable[] actions2={l2};
		
		//TODO: changing variables to whole set of variables
		Variable[] vars = {a,b,c,d,e};
		Variable[] varPrime = {aPrime, bPrime, cPrime, dPrime, ePrime};
		Variable[] actions={l1,l2};
		
		//define init
		int init1 = bdd.minterm("011---------");
		int init2 = bdd.minterm("0--11-------");
		
		int init = bdd.minterm("01111-------");
		
		//define environment transitions
		int t1[]=new int[6];
		t1[0]=bdd.minterm("011--011--0-");
		t1[1]=bdd.minterm("011--010--0-");
		t1[2]=bdd.minterm("011--111--1-");
		t1[3]=bdd.minterm("011--110--1-");
		t1[4]=bdd.minterm("101--101--1-");
		t1[5]=bdd.minterm("111--110--1-");
		
		int envTrans1=bdd.ref(bdd.getZero());
		for(int i=0;i<t1.length;i++){
			envTrans1=bdd.orTo(envTrans1, t1[i]);
			bdd.deref(t1[i]);
		}
		
		int t2[]=new int[6];
		t2[0]=bdd.minterm("0--110--11-0");
		t2[1]=bdd.minterm("0--110--10-0");
		t2[2]=bdd.minterm("0--111--11-1");
		t2[3]=bdd.minterm("0--111--10-1");
		t2[4]=bdd.minterm("1--011--01-1");
		t2[5]=bdd.minterm("1--111--10-1");
		
		int envTrans2=bdd.ref(bdd.getZero());
		for(int i=0;i<t2.length;i++){
			envTrans2=bdd.orTo(envTrans2, t2[i]);
			bdd.deref(t2[i]);
		}
		
		//define system transitions
		int[] s1=new int[9];
		s1[0]=bdd.minterm("011--011--1-");
		s1[1]=bdd.minterm("010--011--1-");
		s1[2]=bdd.minterm("010--101--0-");
		s1[3]=bdd.minterm("111--011--1-");
		s1[4]=bdd.minterm("111--101--0-");
		s1[5]=bdd.minterm("111--111--1-");
		s1[6]=bdd.minterm("110--111--1-");
		s1[7]=bdd.minterm("110--101--0-");
		s1[8]=bdd.minterm("101--101--0-");
		
		int sysTrans1=bdd.ref(bdd.getZero());
		for(int i=0;i<s1.length;i++){
			sysTrans1=bdd.orTo(sysTrans1, s1[i]);
			bdd.deref(s1[i]);
		}
		
		int[] s2=new int[9];
		s2[0]=bdd.minterm("0--110--11-1");
		s2[1]=bdd.minterm("0--100--11-1");
		s2[2]=bdd.minterm("0--101--01-0");
		s2[3]=bdd.minterm("1--110--11-1");
		s2[4]=bdd.minterm("1--111--01-0");
		s2[5]=bdd.minterm("1--111--11-1");
		s2[6]=bdd.minterm("1--101--11-1");
		s2[7]=bdd.minterm("1--101--01-0");
		s2[8]=bdd.minterm("1--011--01-0");
		
		int sysTrans2=bdd.ref(bdd.getZero());
		for(int i=0;i<s2.length;i++){
			sysTrans2=bdd.orTo(sysTrans2, s2[i]);
			bdd.deref(s2[i]);
		}
		
//		Game game1= new Game(bdd, a1Variables, a1VariablesPrime, init1, envTrans1, sysTrans1, actions1);
//		Game game2= new Game(bdd, a2Variables, a2VariablesPrime, init2, envTrans2, sysTrans2, actions2);
		
//		game1.setPermutations(vars, varPrime);
//		game2.setPermutations(vars, varPrime);
		
		Game game1= new Game(bdd, vars, varPrime, init, envTrans1, sysTrans1, actions1);
		Game game2= new Game(bdd, vars, varPrime, init, envTrans2, sysTrans2, actions2);
		
		
		
		Game[] games = {game1, game2};
		return games;
	}
	
	public static Game[] verySimpleGames(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable cPrime=new Variable(bdd, "cPr");
		Variable l1 = new Variable(bdd, "l");
		Variable l2 = new Variable(bdd, "l");
		
		Variable[] a1Variables={a,b};
		Variable[] a1VariablesPrime={aPrime, bPrime};
		Variable[] actions1={l1};
		
		Variable[] a2Variables={a,c};
		Variable[] a2VariablesPrime={aPrime, cPrime};
		Variable[] actions2={l2};
		
		//TODO: changing variables to whole set of variables
		Variable[] vars = {a,b,c};
		Variable[] varPrime = {aPrime, bPrime, cPrime};
		Variable[] actions={l1,l2};
		
		//define init
		int init1 = bdd.minterm("01------");
		int init2 = bdd.minterm("0-1-----");
		
		int init = bdd.minterm("011-----");
		
		//define environment transitions
		int t1[]=new int[8];
		t1[0]=bdd.minterm("00-00-0-");
		t1[1]=bdd.minterm("00-10-1-");
		t1[2]=bdd.minterm("01-01-0-");
		t1[3]=bdd.minterm("01-11-1-");
		t1[4]=bdd.minterm("10-10-1-");
		t1[5]=bdd.minterm("10-00-0-");
		t1[6]=bdd.minterm("11-01-0-");
		t1[7]=bdd.minterm("11-11-1-");
		
		int envTrans1=bdd.ref(bdd.getZero());
		for(int i=0;i<t1.length;i++){
			envTrans1=bdd.orTo(envTrans1, t1[i]);
			bdd.deref(t1[i]);
		}
		
		int t2[]=new int[8];
		t2[0]=bdd.minterm("0-00-0-0");
		t2[1]=bdd.minterm("0-01-0-1");
		t2[2]=bdd.minterm("0-10-1-0");
		t2[3]=bdd.minterm("0-11-1-1");
		t2[4]=bdd.minterm("1-01-0-1");
		t2[5]=bdd.minterm("1-00-0-0");
		t2[6]=bdd.minterm("1-10-1-0");
		t2[7]=bdd.minterm("1-11-1-1");
		
		int envTrans2=bdd.ref(bdd.getZero());
		for(int i=0;i<t2.length;i++){
			envTrans2=bdd.orTo(envTrans2, t2[i]);
			bdd.deref(t2[i]);
		}
		
		//define system transitions
		int[] s1=new int[8];
		s1[0]=bdd.minterm("00-00-0-");
		s1[1]=bdd.minterm("00-01-1-");
		s1[2]=bdd.minterm("01-00-0-");
		s1[3]=bdd.minterm("01-01-1-");
		s1[4]=bdd.minterm("10-11-1-");
		s1[5]=bdd.minterm("10-10-0-");
		s1[6]=bdd.minterm("11-10-0-");
		s1[7]=bdd.minterm("11-11-1-");
		
		int sysTrans1=bdd.getZero();
		for(int i=0;i<s1.length;i++){
			sysTrans1=bdd.orTo(sysTrans1, s1[i]);
			bdd.deref(s1[i]);
		}
		
		int[] s2=new int[9];
		s2[0]=bdd.minterm("0-00-0-0");
		s2[1]=bdd.minterm("0-00-1-1");
		s2[2]=bdd.minterm("0-10-0-0");
		s2[3]=bdd.minterm("0-10-1-1");
		s2[4]=bdd.minterm("1-01-1-1");
		s2[5]=bdd.minterm("1-01-0-0");
		s2[6]=bdd.minterm("1-11-0-0");
		s2[7]=bdd.minterm("1-11-1-1");
		
		int sysTrans2=bdd.ref(bdd.getZero());
		for(int i=0;i<s2.length;i++){
			sysTrans2=bdd.orTo(sysTrans2, s2[i]);
			bdd.deref(s2[i]);
		}
		
//		Game game1= new Game(bdd, a1Variables, a1VariablesPrime, init1, envTrans1, sysTrans1, actions1);
//		Game game2= new Game(bdd, a2Variables, a2VariablesPrime, init2, envTrans2, sysTrans2, actions2);
		
//		game1.setPermutations(vars, varPrime);
//		game2.setPermutations(vars, varPrime);
		
		Game game1= new Game(bdd, vars, varPrime, init, envTrans1, sysTrans1, actions1);
		Game game2= new Game(bdd, vars, varPrime, init, envTrans2, sysTrans2, actions2);
		
		
		
		Game[] games = {game1, game2};
		return games;
	}
	
	
}
