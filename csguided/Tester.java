package csguided;

import java.util.ArrayList;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import automaton.Edge;
import automaton.GameGraph;

public class Tester {

	public static void main(String[] args) {
		//simpleGameSolverTest();
		
		//simpleRMPtest();
		
		//bddTest();
		
		//otherTests();
		
		//csGuidedControlTest1();
		
		//gameSolverTest();
		
		//bddRefTest();

		//csGuidedControlTest2();
		
		//abstractionAndCompositionTest();
		
//		testApproximateAbstraction();
		
//		testSimple2DRoboticGame(20);
	}
	
	
	public static void testSimple2DRoboticGame(int dim){
		BDD bdd = new BDD(10000,1000);
		Game g = TestCaseGenerator.simple2DroboticGame(bdd, dim);
		
		int numOfBits = UtilityMethods.numOfBits(dim-1);
		Variable[] ox = subarray(g.variables,0,numOfBits);
		Variable[] rx = subarray(g.variables,2*numOfBits,3*numOfBits);
		
		int sameX = BDDWrapper.same(bdd, ox, rx);
		int[] abstractPredicates = {g.init , sameX};
		
		long t0,t1;
		
//		System.out.println("computing the abstraction");
//		t0=UtilityMethods.timeStamp();
//		AbstractGame absGame1 = g.computeAbstractionFromAbstractPredicates(abstractPredicates, false);
////		absGame1.toGameGraph().draw("exactAbsGame.dot", 1, 0);
//		UtilityMethods.duration(t0, "the exact abstraction was computed in ",1000);
		
		System.out.println("computing the approximate abstraction");
		t0=UtilityMethods.timeStamp();
		AbstractGame absGame2 = g.computeAbstractionFromAbstractPredicates(abstractPredicates, true);
//		absGame2.toGameGraph().draw("apprAbsGame.dot", 1, 0);
		UtilityMethods.duration(t0, "the approximate abstraction was computed in ",1000);
		
//		absGame1.printGame();
//		absGame2.printGame();
		
//		g.toGameGraph().draw("game.dot", 1);
//		g.printGame();
	}
	
	public static Variable[] subarray(Variable[] vars, int prefix, int postfix){
		int length=postfix-prefix;
		Variable[] subarray=new Variable[length];
		for(int i=0;i<length;i++){
			subarray[i]=vars[i+prefix];
		}
		return subarray;
	}
	
	public static void testApproximateAbstraction(){
		BDD bdd = new BDD(10000, 1000);
		Game g = TestCaseGenerator.simpleGameWith3varsList(bdd);
		g.toGameGraph().draw("newGame.dot", 1);
		
		Variable[] vars = g.variables;
		
		int init = g.getInit(); 
		int pred1 = BDDWrapper.assign(bdd, '0', vars[0]); //a=0
		int pred2 = BDDWrapper.assign(bdd, '0', vars[1]); //b=0
		
		int[] abstractPredicates = {init, pred1, pred2};
		long t0,t1;
		
		System.out.println("computing the abstraction");
		t0=UtilityMethods.timeStamp();
		AbstractGame absGame1 = g.computeAbstractionFromAbstractPredicates(abstractPredicates, false);
		absGame1.toGameGraph().draw("exactAbsGame.dot", 1, 0);
		UtilityMethods.duration(t0, "the exact abstraction was computed in ",1000);
		
		System.out.println("computing the approximate abstraction");
		t0=UtilityMethods.timeStamp();
		AbstractGame absGame2 = g.computeAbstractionFromAbstractPredicates(abstractPredicates, true);
		absGame2.toGameGraph().draw("apprAbsGame.dot", 1, 0);
		UtilityMethods.duration(t0, "the approximate abstraction was computed in ",1000);
		
		absGame1.printGame();
		absGame2.printGame();
		
	}
	
	public static void gameSolverTest(){
		BDD bdd = new BDD(10000,1000);
		
		long t0, t1;
		
		TestCaseGenerator tcg=new TestCaseGenerator(bdd,3);
		Game game = tcg.generateGameWithActions();
		int objective = tcg.noCollisionObjective(Variable.getBDDVars(tcg.x), Variable.getBDDVars(tcg.y), Variable.getBDDVars(tcg.objX), Variable.getBDDVars(tcg.objY));
		//objective=bdd.andTo(objective, tcg.safetyObjective(Variable.getBDDVars(tcg.x), Variable.getBDDVars(tcg.y) , 1, 0));
		
		GameGraph gameGraph = game.toGameGraph("game");
		gameGraph.draw("game.dot", 1);
		
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(game, objective, bdd);
		System.out.println("the game is "+(gs1.isRealizable()?"realizable":" no realizable"));
		t1=System.currentTimeMillis();
		System.out.println("the realizability of the central game was checked in "+(t1-t0)+" milliseconds");
		
		GameSolution solution = gs1.solve();
		
		solution.print();
		solution.drawWinnerStrategy("strat.dot");
		
		
	}
	
	public static void csGuidedControlTest2(){
		BDD bdd = new BDD(10000,1000);
		long t0, t1;
		
		t0=System.currentTimeMillis();
		TestCaseGenerator tcg=new TestCaseGenerator(bdd,2);
		Game game = tcg.generateGameWithActions();
		int objective = bdd.ref(tcg.noCollisionObjective());
//		GameSolver gs1 = new GameSolver(game, objective, bdd);
//		GameSolution concreteSol = gs1.solve();
//		concreteSol.print();
//		t1=System.currentTimeMillis();
//		System.out.println("the realizability of the central game was checked in "+(t1-t0)+" milliseconds");
		
//		GameSolution sol = gs1.solve();
//		sol.drawWinnerStrategy("strategy.dot");
//		
//		UtilityMethods.prompt("\nthe central game was solved\n\n");
		
//		int[] abstractionPredicates=tcg.abstractPredicates(game.getInit());
		int[] abstractionPredicates = {game.getInit(), objective};

//		System.out.println("abs preds");
//		for(int i=0;i<abstractionPredicates.length;i++){
//			bdd.printSet(abstractionPredicates[i]);
//		}
//		
//		UtilityMethods.prompt("\nabstract predicates were computed\n\n");
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc=new CSGuidedControl(bdd, game, abstractionPredicates, objective);
		
		GameSolution solution = csgc.counterStrategyGuidedControl();
		solution.print();
		t1=System.currentTimeMillis();
		System.out.println("the realizability of the abstract game was checked in "+(t1-t0)+" milliseconds");
//		solution.drawWinnerStrategy("abstract.dot", 0 ,1);
		
	}
	
	public static void csGuidedControlTest1(){
		BDD bdd = new BDD(10000,1000);
		
		long t0, t1;
		
//		Game game = TestCaseGenerator.simpleGame3(bdd);
//		int tmp = bdd.ref(bdd.and(game.variables[0].getBDDVar(), bdd.not(game.variables[1].getBDDVar())));
//		tmp=bdd.andTo(tmp, game.variables[2].getBDDVar());
//		int objective = bdd.ref(bdd.not(tmp));
//		bdd.deref(tmp);
//		int[] abstractionPredicates={game.variables[0].getBDDVar(), game.variables[1].getBDDVar()};
//		
//		//TODO: when the abstraction predicate is only 'a', the abstraction has deadlock, check for correctness
//		//int[] abstractionPredicates={game.variables[0].getBDDVar()};
		
		TestCaseGenerator tcg=new TestCaseGenerator(bdd,3);
		Game game = tcg.generateGameWithActions();
		game.printGame();
		//int objective = bdd.ref(tcg.noCollisionObjective());
		int objective = tcg.stateToBDD(Variable.getBDDVars(tcg.x), tcg.getDimension()-1);
		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
		//objective=bdd.andTo(objective, tcg.safetyObjective(Variable.getBDDVars(tcg.x), Variable.getBDDVars(tcg.y) , 1, 0));
		
//		System.out.println("\n\ncurrent objective is");
//		bdd.printSet(objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
		
//		GameGraph gameGraph = game.toGameGraph("game");
//		gameGraph.draw("game.dot", 1);
		
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(game, objective, bdd);
		System.out.println("the game is "+(gs1.isRealizable()?"realizable":" no realizable"));
		t1=System.currentTimeMillis();
		System.out.println("the realizability of the central game was checked in "+(t1-t0)+" milliseconds");
		
		GameSolution sol = gs1.solve();
		sol.drawWinnerStrategy("strategy.dot");
		
		
//		int f1 = bdd.ref(bdd.biimp(game.variables[0].getBDDVar(), game.variables[2].getBDDVar()));
//		int f2 = bdd.ref(bdd.biimp(game.variables[1].getBDDVar(), game.variables[3].getBDDVar()));
		
		//int f=bdd.ref(game.variables[2].getBDDVar());
//		int[] abstractionPredicates={f1,f2};
		
		int[] abstractionPredicates=tcg.safetyAbstractpredicates();

//		System.out.println("abs preds");
//		for(int i=0;i<abstractionPredicates.length;i++){
//			bdd.printSet(abstractionPredicates[i]);
//		}
		
		CSGuidedControl csgc=new CSGuidedControl(bdd, game, abstractionPredicates, objective);

		
		
//		AbstractGame abstractGame=csgc.initialAbstraction();
//		abstractGame.printEquivalenceClasses();
				
//		UtilityMethods.debugBDDMethods(bdd, "objective is ", objective);
		
//		GameGraph g = abstractGame.toGameGraph("absGame");
//		g.draw("absGame.dot",1);
		
//		System.out.println("\n\neq classes of the initial abstraction");
//		abstractGame.printEquivalenceClasses();
				
		
//		System.out.println("objective is");
//		bdd.printSet(objective);
		
		
		
//		int abstractObjective=csgc.computeAbstractObjective(objective, abstractGame);
//		System.out.println("\n\nabstract objective is");
//		bdd.printSet(abstractObjective);
//		
//		GameSolver gs = new GameSolver(abstractGame, abstractObjective, bdd);
//		System.out.println("\nis abstract game realizable?");
//		System.out.println(gs.isRealizable(abstractObjective)?"yes\n":"no\n");
//		
//		GameSolution absSol=gs.solve();
//		absSol.print();
//		absSol.drawWinnerStrategy("absCS.dot");
		
//		bdd.printSet(absSol.strategyOfTheWinner().getInit());
		
//		Game cs = gs.counterStrategy();
//		GameGraph csgg=cs.toGameGraph("absCs");
//		csgg.draw("abstractGameCS.dot", 1);
//		
//		Game cs2 = gs.counterStrategyUnreachablePartRemoved();
//		cs2.toGameGraph("1").draw("absGameCS2.dot", 1);
//		
	
		
//		System.out.println("checking the cs, is it genuine? "+csgc.checkCounterStrategy(absSol.strategyOfTheWinner()));
//		
//		ArrayList<Integer> envFocusedSets = csgc.getEnvStatesFocusedSets();
//		ArrayList<Integer> sysFocusedSets = csgc.getSysStatesFocusedSets();
//		
//		System.out.println("\nafter focusing, the env focused sets are");
//		for(int i=0; i< envFocusedSets.size();i++){
//			System.out.println("Focused set "+i);
//			bdd.printSet(envFocusedSets.get(i));
//		}
//		
//		System.out.println("\nafter focusing, the sys focused sets are");
//		for(int i=0; i< sysFocusedSets.size();i++){
//			System.out.println("Focused set "+i);
//			bdd.printSet(sysFocusedSets.get(i));
//		}

		
//		UtilityMethods.debugBDDMethods(bdd, "before refining the abstraction", game.getInit());
		
//		System.out.println("\nrefining the abstraction");
//		csgc.refineAbstraction(cs2);
//		abstractGame=csgc.getAbstractGame();
////		abstractGame.toGameGraph("3").draw("refinedAbs.dot",1);
////		abstractGame.printGame();
//		
//		UtilityMethods.debugBDDMethods(bdd, "before final test", game.getInit());
//		
//		System.out.println("final test");
		
		t0=System.currentTimeMillis();
		//Game result = csgc.csGuidedControl();
//		result.toGameGraph("1").draw("csGuided.dot", 1);
		
		GameSolution solution = csgc.counterStrategyGuidedControl();
		solution.print();
		solution.drawWinnerStrategy("abstract.dot", 0 ,1);
		
//		AbstractGame finalAbsGame=csgc.getAbstractGame();
//		System.out.println("printing eq classes for the final abstract game");
//		finalAbsGame.printEquivalenceClasses();
//		int[] absGameEqClasses = finalAbsGame.getEquivalenceClasses();
//		for(int e : absGameEqClasses){
//			bdd.printSet(e);
//		}
		
		t1=System.currentTimeMillis();
		System.out.println("the realizability of the abstract game was checked in "+(t1-t0)+" milliseconds");
	}
	
	public static void otherTests(){
		String str =Integer.toBinaryString(3);
		System.out.println(str);
		System.out.println(str.length());
		System.out.println(Integer.parseInt("101",2));
		boolean[] arr={false, true};
	
	}
	
	public static void bddTest(){
		BDD bdd = new BDD(10000,1000);
		int a = bdd.createVar();
		int b=bdd.createVar();
		int c=bdd.createVar();
//		int d = bdd.createVar();
		
//		int f = bdd.ref(bdd.and(a, c));
//		bdd.printSet(f);
//		int g = bdd.minterm(new boolean[]{false, true, false, true});
//		bdd.printSet(g);
//		int h=bdd.minterm("0000");
//		bdd.printSet(h);
//		int h1=bdd.minterm("-0-0");
//		bdd.printSet(h1);
//		int v=bdd.getVar(f);
//		bdd.printSet(v);
//		int validFormula=bdd.ref(bdd.or(a, bdd.not(a)));
//		System.out.println(bdd.isValid(validFormula));
//		System.out.println(bdd.isValid(f));
//		bdd.deref(b);
//		bdd.printSet(f);
//		
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, f);
//		for(String str : minterms){
//			System.out.println(str);
//		}
		
//		int f=bdd.ref(bdd.and(a, b));
//		int f2=f;
//		bdd.deref(f);
//		bdd.gc();
//		int g=bdd.ref(bdd.and(bdd.not(a), bdd.not(b)));
//		UtilityMethods.debugBDDMethods(bdd, "f is ", f);
//		UtilityMethods.debugBDDMethods(bdd, "f2 is ", f2);
		
		int f=bdd.ref(bdd.and(bdd.not(a), bdd.not(b)));
		f=bdd.andTo(f, c);
		bdd.printSet(f);
		
		int g = bdd.ref(bdd.and(f,bdd.getOne()));
		bdd.printSet(g);
		
		bdd.deref(f);
		bdd.gc();
		int h= bdd.ref(bdd.and(bdd.not(a), bdd.not(b)));
		bdd.printSet(h);
		bdd.printSet(f);
		bdd.printSet(g);
		
		System.out.println(f);
		System.out.println(g);
		
		System.out.println(bdd.countRootNodes());
		
	}
	
	public static void bddRefTest(){
		BDD bdd = new BDD(10000,1000);
		int a = bdd.createVar();
		int b=bdd.createVar();
		
		int f=bdd.ref(bdd.and(a, b));
		bdd.printSet(f);
		
		
		//bdd.gc();
		
		bdd.printSet(f);
		
		
		int c=bdd.createVar();
		int g= bdd.ref(bdd.and(f,c));
		bdd.deref(f);
		//bdd.gc();
		System.out.println("g is ");
		bdd.printSet(g);
		
		
		int d = bdd.createVar();
		
		int[] set1 = {a,b};
		int[] set2 = {c,d};
		Permutation p =bdd.createPermutation(set1, set2);
		int fprime=bdd.ref(bdd.replace(f, p));
		System.out.println("fprime is");
		bdd.printSet(fprime);
		System.out.println("f is");
		bdd.printSet(f);
		
		int cube = bdd.ref(bdd.and(a, b));
		int r = bdd.ref(bdd.exists(g, cube));
		bdd.printSet(r);
		bdd.deref(cube);
		bdd.printSet(r);
		
		int z=bdd.ref(bdd.exists(g, cube));
		bdd.printSet(z);
		
		System.out.println("f is");
		bdd.printSet(f);
		System.out.println("g is");
		bdd.printSet(g);
		
		int cube1=bdd.ref(bdd.and(a, c));
		int cube2=bdd.ref(bdd.and(b,d));
		int cube3=bdd.ref(bdd.and(cube1, cube2));
		
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		
		bdd.deref(cube1);
		bdd.deref(cube2);
		
		System.out.println("after deref");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		
		bdd.gc();
		System.out.println("after gc");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		
		bdd.gc();
		System.out.println("after gc");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		
		cube1 = bdd.ref(bdd.and(a,bdd.not(d)));
		System.out.println("new cube 1");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		
		cube2=bdd.getOne();
		System.out.println("new cube 2");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		System.out.println(cube2);
		System.out.println(bdd.getOne());
		
		
		
		bdd.deref(cube2);
		bdd.gc();
		System.out.println("deref cube 2");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		System.out.println(cube2);
		System.out.println(bdd.getOne());
		
		cube2=bdd.ref(bdd.getOne());
		System.out.println("new cube 2 with ref");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		System.out.println(cube2);
		System.out.println(bdd.getOne());
		
		bdd.deref(cube2);
		bdd.gc();
		System.out.println("deref cube 2");
		UtilityMethods.debugBDDMethods(bdd, "cube1", cube1);
		UtilityMethods.debugBDDMethods(bdd, "cube2", cube2);
		UtilityMethods.debugBDDMethods(bdd, "cube3", cube3);
		System.out.println(cube2);
		System.out.println(bdd.getOne());
		
		System.out.println("final test");
		bdd.deref(cube1);
		bdd.gc();
		int newF=bdd.ref(bdd.and(cube1, bdd.getOne()));
		bdd.printSet(newF);
	}
	
//	public static void simpleGameSolverTest(){
//		BDD bdd = new BDD(10000,1000);
//		
//		Game simpleGame=TestCaseGenerator.simpleGameTest2(bdd);
//		int compObj=bdd.ref(bdd.and(simpleGame.inputVariables[0].getBDDVar(), bdd.not(simpleGame.outputVariables[0].getBDDVar())));
//		int objective = bdd.ref(bdd.not(compObj));
//		GameSolver gs = new GameSolver(simpleGame, objective, bdd);
//		
//		GameGraph gg = simpleGame.toGameGraph("gg");
//		gg.draw("simpleGame_gg.dot", 1);
//		
//
//		
//		System.out.println("checking for realizability");
//		System.out.println(gs.isRealizable(objective));
//		
//		Game cs = gs.counterStrategy();
//		bdd.printSet(cs.T_env);
//		bdd.printSet(cs.T_sys);
//		
//		cs.toGameGraph("cs_gg").draw("csgg.dot", 1);
//
//		
//		System.out.println("removing the unreachable part");
//		
//		Game cs2 = gs.counterStrategyUnreachablePartRemoved();
//		cs2.toGameGraph("cs2_gg").draw("csgg2.dot", 1);
//
//		
//		System.out.println("checking the actions and availability for games");
//		System.out.println("the set of available actions for environment state a&b");
//		int state = bdd.ref(bdd.and(simpleGame.inputVariables[0].getBDDVar(), simpleGame.outputVariables[0].getBDDVar()));
//		int availableActions = simpleGame.getAvailableActions(state, simpleGame.T_env);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of available actions for system state a&b");
//		state = bdd.ref(bdd.and(simpleGame.inputVariables[0].getBDDVar(), simpleGame.outputVariables[0].getBDDVar()));
//		availableActions = simpleGame.getAvailableActions(state, simpleGame.T_sys);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of available actions for environment state !a&b");
//		state = bdd.ref(bdd.and(bdd.not(simpleGame.inputVariables[0].getBDDVar()), simpleGame.outputVariables[0].getBDDVar()));
//		availableActions = simpleGame.getAvailableActions(state, simpleGame.T_env);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of environment states where the action l=1 is available");
//		int act = bdd.ref(simpleGame.actionVars[0].getBDDVar());
//		int avl1=simpleGame.actionAvailabilitySet(act, simpleGame.getEnvironmentTransitionRelation());
//		bdd.printSet(avl1);
//		
//		System.out.println("the set of system states where the action l=1 is available");
//		act = bdd.ref(simpleGame.actionVars[0].getBDDVar());
//		avl1=simpleGame.actionAvailabilitySet(act, simpleGame.getSystemTransitionRelation());
//		bdd.printSet(avl1);
//		
//		System.out.println("the set of environment states where the action l=0 is available");
//		act = bdd.ref(bdd.not(simpleGame.actionVars[0].getBDDVar()));
//		int avl0=simpleGame.actionAvailabilitySet(act, simpleGame.getEnvironmentTransitionRelation());
//		bdd.printSet(avl0);
//		
//		
//	}
//	
//	/**
//	 * solving nondeterministic game test
//	 */
//	public static void simpleNDGameSolverTest(){
//		BDD bdd = new BDD(10000,1000);
//		
//		Game simpleGame=TestCaseGenerator.simpleNondeterministicGame(bdd);
//		int compObj=bdd.ref(bdd.and(simpleGame.variables[0].getBDDVar(), bdd.not(simpleGame.variables[1].getBDDVar())));
//		int objective = bdd.ref(bdd.not(compObj));
//		GameSolver gs = new GameSolver(simpleGame, objective, bdd);
//		
//		GameGraph gg = simpleGame.toGameGraph("gg");
//		gg.draw("simpleGame_gg.dot", 1);
//		
//		
//		UtilityMethods.debugBDDMethods(bdd,"objective is ", objective);
//		int cpre = simpleGame.controllablePredecessor(objective);
//		UtilityMethods.debugBDDMethods(bdd, "cpre is ", cpre);
//		
//		int cpre2 = simpleGame.controllablePredecessor(cpre);
//		UtilityMethods.debugBDDMethods(bdd, "cpre2 is ", cpre2);
//		
//		System.out.println("checking for realizability");
////		System.out.println(gs.isRealizable(objective));
//		
//		UtilityMethods.prompt("before computing counter-strategy");
//		
//		Game cs = gs.counterStrategy();
//		bdd.printSet(cs.T_env);
//		bdd.printSet(cs.T_sys);
//		
//		cs.toGameGraph("cs_gg").draw("csgg.dot", 1);
//		
//		
//		System.out.println("removing the unreachable part");
//		
//		Game cs2 = gs.counterStrategyUnreachablePartRemoved();
//		cs2.toGameGraph("cs2_gg").draw("csgg2.dot", 1);
//		
//		
//		System.out.println("checking the actions and availability for games");
//		System.out.println("the set of available actions for environment state a&b");
//		int state = bdd.ref(bdd.and(simpleGame.variables[0].getBDDVar(), simpleGame.variables[1].getBDDVar()));
//		int availableActions = simpleGame.getAvailableActions(state, simpleGame.T_env);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of available actions for system state a&b");
//		state = bdd.ref(bdd.and(simpleGame.variables[0].getBDDVar(), simpleGame.variables[1].getBDDVar()));
//		availableActions = simpleGame.getAvailableActions(state, simpleGame.T_sys);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of available actions for environment state !a&b");
//		state = bdd.ref(bdd.and(bdd.not(simpleGame.variables[0].getBDDVar()), simpleGame.variables[1].getBDDVar()));
//		availableActions = simpleGame.getAvailableActions(state, simpleGame.T_env);
//		bdd.printSet(availableActions);
//		
//		System.out.println("the set of environment states where the action l=1 is available");
//		int act = bdd.ref(simpleGame.actionVars[0].getBDDVar());
//		int avl1=simpleGame.actionAvailabilitySet(act, simpleGame.getEnvironmentTransitionRelation());
//		bdd.printSet(avl1);
//		
//		System.out.println("the set of system states where the action l=1 is available");
//		act = bdd.ref(simpleGame.actionVars[0].getBDDVar());
//		avl1=simpleGame.actionAvailabilitySet(act, simpleGame.getSystemTransitionRelation());
//		bdd.printSet(avl1);
//		
//		System.out.println("the set of environment states where the action l=0 is available");
//		act = bdd.ref(bdd.not(simpleGame.actionVars[0].getBDDVar()));
//		int avl0=simpleGame.actionAvailabilitySet(act, simpleGame.getEnvironmentTransitionRelation());
//		bdd.printSet(avl0);
//		
//		
//	}
//	
//	public static void simpleRMPtest(){
//		BDD bdd = new BDD(10000,1000);
//		TestCaseGenerator tcg=new TestCaseGenerator(bdd,2);
//		Game g = tcg.generateGameWithActions();
//		int objective = tcg.noCollisionObjective(Variable.getBDDVars(tcg.x), Variable.getBDDVars(tcg.y), Variable.getBDDVars(tcg.objX), Variable.getBDDVars(tcg.objY));
//		objective=bdd.andTo(objective, tcg.safetyObjective(Variable.getBDDVars(tcg.x), Variable.getBDDVars(tcg.y) , 1, 0));
//		
//		
//		System.out.println("objective is ");
//		bdd.printSet(objective);
//		GameSolver gs= new GameSolver(g, objective, bdd);
//		
//		g.toGameGraph("gameGraphTest").draw("gggbbb.dot", 1);
//		
//		
//		
//		System.out.println("checking for realizability");
//		System.out.println(gs.isRealizable(objective));
//		
//		Game strat = gs.synthesize(gs.winningRegion);
//		bdd.printSet(strat.T_env);
//		bdd.printSet(strat.T_sys);
//		
//		System.out.println("the initial set of init is ");
//		bdd.printSet(strat.getInit());
//		
//		System.out.println("one step execution");
//		int secondStep=strat.symbolicGameOneStepExecution(strat.getInit());
//		bdd.printSet(secondStep);
//		
//		System.out.println("second step execution");
//		int thirdStep=strat.symbolicGameOneStepExecution(secondStep);
//		bdd.printSet(thirdStep);
//		
//		
//		
//		Game cs = gs.counterStrategy();
//		bdd.printSet(cs.T_env);
//		bdd.printSet(cs.T_sys);
//		
//		System.out.println("the initial set of init is ");
//		bdd.printSet(cs.getInit());
//		
//		System.out.println("one step execution");
//		int secondStepcs=cs.symbolicGameOneStepExecution(cs.getInit());
//		bdd.printSet(secondStepcs);
//		
//		System.out.println("second step execution");
//		int thirdStepcs=cs.symbolicGameOneStepExecution(secondStepcs);
//		bdd.printSet(thirdStepcs);
//		
//		cs.toGameGraph("cs").draw("csGG.dot", 1);
//		
//		
//		
//		Game cs2=gs.counterStrategyUnreachablePartRemoved();
//		
//		
//		GameGraph gg1 = cs2.toGameGraph("gg1");
//		gg1.draw("gg1.dot", 1);
//		
//		cs2.toGameGraph("cs2gg").draw("cs2gg.dot", 1);
//	}
//	
//	public static void abstractionAndCompositionTest(){
//		BDD bdd=new BDD(10000,1000);
//		Game[] games=TestCaseGenerator.simpleGames(bdd);
//		
//		games[0].toGameGraph().draw("game0.dot", 1);
//		games[1].toGameGraph().draw("game1.dot", 1);
//		
//		int init=games[0].getInit();
//		int[] a = {games[0].variables[0].getBDDVar()};
//		int[] b = {games[0].variables[1].getBDDVar()};
//		int[] d = {games[0].variables[3].getBDDVar()};
//		int objective1 = bdd.ref(bdd.not(BDDWrapper.same(bdd, a , b )));
//		int objective2 = bdd.ref(bdd.not(BDDWrapper.same(bdd, a , d ))); 
//		
//		UtilityMethods.debugBDDMethods(bdd, "init", init);
//		UtilityMethods.debugBDDMethods(bdd, "objective1", objective1);
//		UtilityMethods.debugBDDMethods(bdd, "objective2", objective2);
//		
//		int[] abstractionPredicates={init, objective1, objective2};
//		
//		Game composedGame=games[0].compose(games[1]);
//		composedGame.toGameGraph().draw("composedGame.dot", 1);
//		
//		CSGuidedControl cgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
//		CSGuidedControl cgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
//		
//		cgc1.computeInitialAbstraction();
//		cgc2.computeInitialAbstraction();
//		
//		AbstractGame absGame1=cgc1.getAbstractGame();
//		absGame1.toGameGraph().draw("absGame1.dot", 1);
//		AbstractGame absGame2=cgc2.getAbstractGame();
//		absGame2.toGameGraph().draw("absGame2.dot", 1);
//		
//		AbstractGame composedAbsGame=absGame1.compose(absGame2);
//		composedAbsGame.toGameGraph().draw("composedAbsGame.dot", 1);
//		composedAbsGame.printGame();
//		
//	}

}