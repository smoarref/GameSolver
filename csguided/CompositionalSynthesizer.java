package csguided;

import java.util.ArrayList;
import java.util.HashMap;

import utils.UtilityMethods;
import jdd.bdd.BDD;

public class CompositionalSynthesizer {

	public static void main(String[] args) {
		//testRMP2(2);
		testRMP();
	}
	
	public static Game compose(BDD bdd, Game game1, Game game2){
		
		//For now we assume that both games are aware of each other's variavles
		Variable[] gameVariables = unionVariables(game1.variables, game2.variables);
		Variable[] gamePrimeVariables=unionVariables(game1.primedVariables, game2.primedVariables);
		
//		for(Variable v : gameVariables){
//			System.out.println(v.toString());
//		}
//		UtilityMethods.getUserInput();
//		

		
		int gameInit= bdd.and(game1.getInit(), game2.getInit());
		int gameEnvTrans = bdd.ref(bdd.and(game1.getEnvironmentTransitionRelation(), game2.getEnvironmentTransitionRelation()));
		int gameSysTrans = bdd.ref(bdd.and(game1.getSystemTransitionRelation(), game2.getSystemTransitionRelation()));
		
		Variable[] actionVars = UtilityMethods.concatenateArrays(game1.actionVars, game2.actionVars);
		
		
		Game composedGame= new Game(bdd, gameVariables, gamePrimeVariables, gameInit, gameEnvTrans, gameSysTrans, actionVars);
		composedGame.setActionMap(composeActionMaps(game1.getActionMap(), game2.getActionMap()));
		return composedGame;
	}
	
	public static Variable[] unionVariables(Variable[] set1, Variable[] set2){
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for(int i=0; i<set1.length;i++){
			vars.add(set1[i]);
		}
		for(int j=0;j<set2.length;j++){
			if(!vars.contains(set2[j])) vars.add(set2[j]);
		}
		return vars.toArray(new Variable[vars.size()]);
	}
	
	public static HashMap<String , String> composeActionMaps(HashMap<String, String> actMap1, HashMap<String, String> actMap2){
		if(actMap1 == null || actMap2 == null) return null;
		HashMap<String, String> composedActMap=new HashMap<String, String>();
		for(String act1 : actMap1.keySet()){
			for(String act2 : actMap2.keySet()){
				String act=act1+act2;
				String label = actMap1.get(act1)+","+actMap2.get(act2);
				composedActMap.put(act, label);
			}
		}
		return composedActMap;
	}
	
	//TODO: it is not necessary to compute the concreteGame for the composition of the concrete games, revise
	public static AbstractGame composeAbstractGames(BDD bdd, AbstractGame absGame1, AbstractGame absGame2){
		Game composedAbsGame = compose(bdd, absGame1, absGame2);
		Game composedConcreteGame=compose(bdd, absGame1.concreteGame, absGame2.concreteGame);
		
		//syncronize on environment transitions of the abstract game
		//makes sure that the environment takes one single action
		//TODO: think about a better way to do it, revise
		int absT_env=composedAbsGame.getEnvironmentTransitionRelation();
		int sameAction = BDDWrapper.same(bdd, Variable.getBDDVars(absGame1.actionVars), Variable.getBDDVars(absGame2.actionVars));
		absT_env = bdd.andTo(absT_env, sameAction);
		bdd.deref(sameAction);
		
		//compose equivalence classes
		int[] composedEqClasses=composeEquivalenceClasses(bdd, absGame1.getEquivalenceClasses(), absGame2.getEquivalenceClasses());
		
		AbstractGame absComposedGame=new AbstractGame(bdd, composedAbsGame.variables, composedAbsGame.primedVariables, composedAbsGame.getInit(), absT_env, composedAbsGame.getSystemTransitionRelation(), composedAbsGame.envActionVars, composedAbsGame.sysActionVars, composedConcreteGame, composedEqClasses);
		return absComposedGame;
	}
	
//	public static AbstractGame composeAbstractGames2(BDD bdd, AbstractGame absGame1, AbstractGame absGame2){
//		int[] eqClasses1 = absGame1.getEquivalenceClasses();
//		int[] eqClasses2 = absGame2.getEquivalenceClasses();
//		ArrayList<Integer> eqClassesArrayList= new ArrayList<Integer>();
//		ArrayList<Integer> eqClasses1Indices=new ArrayList<Integer>();
//		ArrayList<Integer> eqClasses2Indices=new ArrayList<Integer>();
//		
//		//create the abstract states based on equivalence classes which are not empty
//		for(int i=0;i<eqClasses1.length;i++){
//			for(int j=0;j<eqClasses2.length;j++){
//				int newEqClass=bdd.ref(bdd.and(eqClasses1[i], eqClasses2[j]));
//				if(newEqClass!=0){
//					eqClassesArrayList.add(newEqClass);
//					eqClasses1Indices.add(i);
//					eqClasses2Indices.add(j);
//				}
//			}
//		}
//		int[] equivalenceClasses = UtilityMethods.IntegerArrayListTointArray(eqClassesArrayList);
//		Variable[] abstractVars=createAbtractVariables(bdd, equivalenceClasses, "absVars");
//		Variable[] abstractVarsPrime=Variable.createPrimeVariables(bdd, abstractVars);
//		
//		//hatI
//		int hatI=bdd.getZero();
//		for(int i=0;i<eqClasses1Indices.size();i++){
//			for(int j=0;j<eqClasses2Indices.size();j++){
//				
//			}
//		}
//		
//		//hat_T_sys
//		
//		//hat_T_env
//		
//		//TODO: it is not necessary to compute the concreteGame for the composition of the concrete games, revise
//		Game composedConcreteGame=compose(bdd, absGame1.concreteGame, absGame2.concreteGame);
//		
//		AbstractGame compositeAbstractGame = new AbstractGame(bdd, abstractVars, abstractVarsPrime, initial, envTrans, sysTrans, composedConcreteGame.actionVars, composedConcreteGame, equivalenceClasses);
//	}
	
	//TODO: this method does not belong to this class, and CSGuidedControl has a same method, find a suitable class for it and refactor
	public static Variable[] createAbtractVariables(BDD bdd, int[] equivalenceClasses, String namePrefix){
		//create the abstract variables based on equivalence classes
		int numOfBits=UtilityMethods.numOfBits(equivalenceClasses.length-1);
		Variable[] abstractVariables=new Variable[numOfBits];
		for(int i=0; i<abstractVariables.length;i++){
			abstractVariables[i]=new Variable(bdd, namePrefix+i);
		}
		return abstractVariables;
	}
	
	/**
	 * Assumes that the abstractions are consistent
	 * @param eqClasses1
	 * @param eqClasses2
	 * @return
	 */
	public static int[] composeEquivalenceClasses(BDD bdd, int[] eqClasses1, int[] eqClasses2){
		int[] composedEqClasses=new int[eqClasses1.length*eqClasses2.length];
		int counter=0;
		for(int i=0;i<eqClasses1.length;i++){
			for(int j=0;j<eqClasses2.length;j++){
				composedEqClasses[counter]=bdd.ref(bdd.and(eqClasses1[i], eqClasses2[j]));
				counter++;
			}
		}
		return composedEqClasses;
	}
	
	public static void test(){
		BDD bdd = new BDD(10000, 1000);
		Game[] games = TestCaseGenerator.verySimpleGames(bdd);
		
		Game g1 = games[0];
		Game g2 = games[1];
		
		g1.toGameGraph().draw("g1.dot", 1);
		g2.toGameGraph().draw("g2.dot", 1);
		
		int objective1 = bdd.ref(bdd.not(bdd.biimp(g1.variables[0].getBDDVar(), g1.variables[1].getBDDVar())));
		int objective2 = bdd.ref(bdd.not(bdd.biimp(g2.variables[0].getBDDVar(), g2.variables[2].getBDDVar())));
		int objective = bdd.ref(bdd.and(objective1, objective2));
		
		UtilityMethods.debugBDDMethods(bdd, "obj1 is ", objective1);
		UtilityMethods.debugBDDMethods(bdd, "obj2 is ", objective2);
		
//		System.out.println("printing g1");
//		g1.printGame();
//		
//		System.out.println("printing g2");
//		g2.printGame();
//		
//		GameSolver gs1=new GameSolver(g1,objective1,bdd);
//		GameSolution concSol1 = gs1.solve();
//		concSol1.print();
//		concSol1.drawWinnerStrategy("concStrat1.dot");
//		
//		GameSolver gs2=new GameSolver(g2,objective2,bdd);
//		GameSolution concSol2 = gs2.solve();
//		concSol2.print();
//		concSol2.drawWinnerStrategy("concStrat2.dot");
//		
//		Game composition = compose(bdd, g1,g2);
//		composition.toGameGraph().draw("comp.dot", 1);
//		
//		GameSolver gs3=new GameSolver(composition,objective, bdd);
//		GameSolution concSol3 = gs3.solve();
//		concSol3.print();
//		concSol3.drawWinnerStrategy("concStrat3.dot");
//		
//		Game comStrat = compose(bdd, concSol1.strategyOfTheWinner(), concSol2.strategyOfTheWinner());
//		comStrat.toGameGraph().draw("compStrat.dot", 1);
		
		int init = bdd.ref(bdd.and(g1.getInit(), g2.getInit()));
		
		int[] abstractionPredicates= {init, objective1, objective2};
		
		UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
		UtilityMethods.debugBDDMethods(bdd, "g1 init is ", g1.getInit());
		UtilityMethods.debugBDDMethods(bdd, "g2 init is ", g2.getInit());
		
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
		solution1.print();
		solution1.drawWinnerStrategy("absStrat1.dot");
		
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
		solution2.drawWinnerStrategy("absStrat2.dot");
		
		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
		absComp.toGameGraph().draw("stratComp.dot", 1);
		
		AbstractGame absGame=composeAbstractGames(bdd, csgc1.getAbstractGame(), csgc2.getAbstractGame());
		absGame.toGameGraph().draw("absGameComp.dot", 1);
	}
	
	public static void test2(){
		
		BDD bdd = new BDD(10000, 1000);
		Game[] games = TestCaseGenerator.simpleGames(bdd);
		
		Game g1 = games[0];
		Game g2 = games[1];
		
		g1.toGameGraph().draw("g1.dot", 1);
		g2.toGameGraph().draw("g2.dot", 1);
		
		int objective1 = bdd.ref(bdd.not(bdd.biimp(g1.variables[0].getBDDVar(), g1.variables[1].getBDDVar())));
		int objective2 = bdd.ref(bdd.not(bdd.biimp(g2.variables[0].getBDDVar(), g2.variables[3].getBDDVar())));
		int objective = bdd.ref(bdd.and(objective1, objective2));
		
		UtilityMethods.debugBDDMethods(bdd, "obj1 is ", objective1);
		UtilityMethods.debugBDDMethods(bdd, "obj2 is ", objective2);
		
//		System.out.println("printing g1");
//		g1.printGame();
//		
//		System.out.println("printing g2");
//		g2.printGame();
//		
		GameSolver gs1=new GameSolver(g1,objective1,bdd);
		GameSolution concSol1 = gs1.solve();
		concSol1.print();
//		concSol1.drawWinnerStrategy("concStrat1.dot");
//		
		GameSolver gs2=new GameSolver(g2,objective2,bdd);
		GameSolution concSol2 = gs2.solve();
		concSol2.print();
//		concSol2.drawWinnerStrategy("concStrat2.dot");
//		
//		Game composition = compose(bdd, g1,g2);
//		composition.toGameGraph().draw("comp.dot", 1);
//		
//		GameSolver gs3=new GameSolver(composition,objective, bdd);
//		GameSolution concSol3 = gs3.solve();
//		concSol3.print();
//		concSol3.drawWinnerStrategy("concStrat3.dot");
//		
//		Game comStrat = compose(bdd, concSol1.strategyOfTheWinner(), concSol2.strategyOfTheWinner());
//		comStrat.toGameGraph().draw("compStrat.dot", 1);
		
		int init = bdd.ref(bdd.and(g1.getInit(), g2.getInit()));
		
		int[] abstractionPredicates= {init, objective1, objective2};
		
		UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
		UtilityMethods.debugBDDMethods(bdd, "g1 init is ", g1.getInit());
		UtilityMethods.debugBDDMethods(bdd, "g2 init is ", g2.getInit());
		
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
		solution1.print();
		solution1.drawWinnerStrategy("absStrat1.dot");
		
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
		solution2.drawWinnerStrategy("absStrat2.dot");
		
		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
		absComp.toGameGraph().draw("stratComp.dot", 1);
		
		AbstractGame absGame=composeAbstractGames(bdd, csgc1.getAbstractGame(), csgc2.getAbstractGame());
		absGame.toGameGraph().draw("absGameComp.dot", 1);
	}
	
	public static void testRMP(){
		
		long t0,t1;
		long t;
		
		BDD bdd = new BDD(10000,1000);
		TestCaseGenerator tcg = new TestCaseGenerator(bdd);
				
		//Game[] games =  tcg.generateMultiAgentRMPTestCase(bdd, 2);
		Game[] games = tcg.simpleMultiAgentRMPgame(bdd);
				
				
				
		//define objectives
		int[] objX = {games[0].inputVariables[0].getBDDVar(), games[0].inputVariables[1].getBDDVar()};
		int[] objY = {games[0].inputVariables[2].getBDDVar()};
				
		int[] x1 = {games[0].outputVariables[0].getBDDVar(), games[0].outputVariables[1].getBDDVar()};
		int[] y1 = {games[0].outputVariables[2].getBDDVar()};
				
		int[] x2 = {games[1].outputVariables[3].getBDDVar(), games[1].outputVariables[4].getBDDVar()};
		int[] y2 = {games[1].outputVariables[5].getBDDVar()};
				
		int objective1 = tcg.noCollisionObjective(objX , objY , x1, y1);
		int objective2 = tcg.noCollisionObjective(objX , objY , x2, y2);
		int objective3 = tcg.noCollisionObjective(x1, y1, x2, y2);
		int init = bdd.ref(bdd.and(games[0].getInit(), games[1].getInit()));
				
		int[] abstractionPredicates={init, objective1, objective2, objective3};
				
//				UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//				UtilityMethods.debugBDDMethods(bdd, "obj1 is ", objective1);
//				UtilityMethods.debugBDDMethods(bdd, "obj2 is ", objective2);
//				UtilityMethods.debugBDDMethods(bdd, "obj3 is ", objective3);
//				UtilityMethods.getUserInput();
				
				games[0].toGameGraph().draw("agent1.dot", 1);
				games[1].toGameGraph().draw("agent2.dot", 1);
				
		t0=System.currentTimeMillis();
				GameSolver gs1 = new GameSolver(games[0], objective1, bdd);
				GameSolution concSol1 = gs1.solve();
				concSol1.print();
				concSol1.drawWinnerStrategy("concSol1.dot");
				t1=System.currentTimeMillis();
				System.out.println("time for solving game 1 "+(t1-t0));
				t=t1-t0;
				
				t0=System.currentTimeMillis();
				GameSolver gs2 = new GameSolver(games[1], objective2, bdd);
				GameSolution concSol2 = gs2.solve();
				concSol2.print();
				concSol2.drawWinnerStrategy("concSol2.dot");
				t1=System.currentTimeMillis();
				System.out.println("time for solving game 2 "+(t1-t0));
				t+=t1-t0;
				System.out.println("accumulated time "+t);
				
				Game composition = compose(bdd, games[0], games[1]);
//				composition.printGame();
//				composition.toGameGraph().draw("composition.dot", 1);
				int objective = bdd.ref(bdd.and(objective1, objective2));
				
//				UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
//				UtilityMethods.getUserInput();
				
				t0=System.currentTimeMillis();
				GameSolver gs3 = new GameSolver(composition, objective, bdd);
				GameSolution concSol3 = gs3.solve();
				concSol3.print();
//				concSol3.drawWinnerStrategy("concSol3.dot");
				t1=System.currentTimeMillis();
				System.out.println("time for solving composition of games "+(t1-t0));
				t=t1-t0;
				
		System.out.println("\n\n cs guided control \n\n");
		
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		
		AbstractGame absGame1=csgc1.computeInitialAbstraction();
		absGame1.toGameGraph().draw("absGame1.dot", 1);
		
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
		solution1.print();
		solution1.drawWinnerStrategy("absStrat1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		
		AbstractGame absGame2=csgc2.computeInitialAbstraction();
		absGame2.toGameGraph().draw("absGame2.dot", 1);
		
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
		//solution2.drawWinnerStrategy("absStrat2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		
		t0=System.currentTimeMillis();
		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
		t1=System.currentTimeMillis();
		System.out.println("time for composing "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		//absComp.toGameGraph().draw("stratComp.dot", 1);
				
//		AbstractGame absGame=composeAbstractGames(bdd, csgc1.getAbstractGame(), csgc2.getAbstractGame());
//		
//		CSGuidedControl csgcAbs=new CSGuidedControl(bdd, absGame, abstractionPredicates, objective1);
		//absGame.toGameGraph().draw("absGameComp.dot", 1);
				
		//absGame.printGame();
		//absGame.printEquivalenceClasses();
	}
	
	public static void testRMP2(int dim){
		long t0,t1;
		long t;
		
		BDD bdd = new BDD(10000,1000);
		TestCaseGenerator tcg = new TestCaseGenerator(bdd);
		Game[] games = tcg.generateMultiAgentRMPTestCase(bdd, dim);
		
		//define objectives
		int inpLen=games[0].inputVariables.length;
		int[] objX = new int[inpLen/2];
		int[] objY = new int[inpLen/2];
		
		for(int i=0;i<inpLen/2;i++){
			objX[i]=games[0].inputVariables[i].getBDDVar();
			objY[i]=games[0].inputVariables[inpLen/2+i].getBDDVar();
		}
					
		int length = games[0].outputVariables.length;
		int partitionLength=length/4;
		
		int[] x1=new int[partitionLength];
		int[] y1=new int[partitionLength];
		int[] x2=new int[partitionLength];
		int[] y2=new int[partitionLength];
		
		for(int i=0;i<partitionLength;i++){
			x1[i]=games[0].outputVariables[i].getBDDVar();
			y1[i]=games[0].outputVariables[partitionLength+i].getBDDVar();
			x2[i]=games[0].outputVariables[2*partitionLength+i].getBDDVar();
			y2[i]=games[0].outputVariables[3*partitionLength+i].getBDDVar();
		}
						
//		int objective1 = tcg.noCollisionObjective(objX , objY , x1, y1);
//		int objective2 = tcg.noCollisionObjective(objX , objY , x2, y2);
		
		int objective1 = bdd.ref(bdd.not(tcg.stateToBDD(x1, dim-1)));
		int objective2 =  bdd.ref(bdd.not(tcg.stateToBDD(x2, dim-1)));
		//int objective3 = tcg.noCollisionObjective(x1, y1, x2, y2);
		int init = bdd.ref(bdd.and(games[0].getInit(), games[1].getInit()));
						
		int[] abstractionPredicates={init, objective1, objective2};
		
		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
		UtilityMethods.debugBDDMethods(bdd, "obj1 is ", objective1);
		UtilityMethods.debugBDDMethods(bdd, "obj2 is ", objective2);
		//UtilityMethods.debugBDDMethods(bdd, "obj3 is ", objective3);
		UtilityMethods.getUserInput();
		
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(games[0], objective1, bdd);
		//games[0].printGame();
		games[0].toGameGraph().draw("concGame0.dot", 1);
		GameSolution concSol1 = gs1.solve();
//		concSol1.print();
//		concSol1.drawWinnerStrategy("concSol1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		GameSolver gs2 = new GameSolver(games[1], objective2, bdd);
		GameSolution concSol2 = gs2.solve();
//		concSol2.print();
//		concSol2.drawWinnerStrategy("concSol2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		
//		Game composition = compose(bdd, games[0], games[1]);
////		composition.printGame();
////		composition.toGameGraph().draw("composition.dot", 1);
//		int objective = bdd.ref(bdd.and(objective1, objective2));
//		
////		UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
////		UtilityMethods.getUserInput();
//		
//		t0=System.currentTimeMillis();
//		GameSolver gs3 = new GameSolver(composition, objective, bdd);
//		GameSolution concSol3 = gs3.solve();
//		concSol3.print();
////		concSol3.drawWinnerStrategy("concSol3.dot");
//		t1=System.currentTimeMillis();
//		System.out.println("time for solving composition of games "+(t1-t0));
//		t=t1-t0;
		
		System.out.println("\n\n cs guided control \n\n");
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
//		solution1.print();
//		solution1.drawWinnerStrategy("absStrat1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
		//solution2.drawWinnerStrategy("absStrat2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		
		t0=System.currentTimeMillis();
		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
		t1=System.currentTimeMillis();
		System.out.println("time for composing "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
	}
	
	public static void testRMP3(int dim){
		long t0,t1;
		long t;
		
		BDD bdd = new BDD(10000,1000);
		//RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentVarsDimensions)
		TestCaseGenerator tcg = new TestCaseGenerator(bdd);
		Game[] games = tcg.generateMultiAgentRMPTestCase(bdd, dim);
		
		//define objectives
		int inpLen=games[0].inputVariables.length;
		int[] objX = new int[inpLen/2];
		int[] objY = new int[inpLen/2];
		
		for(int i=0;i<inpLen/2;i++){
			objX[i]=games[0].inputVariables[i].getBDDVar();
			objY[i]=games[0].inputVariables[inpLen/2+i].getBDDVar();
		}
					
		int length = games[0].outputVariables.length;
		int partitionLength=length/4;
		
		int[] x1=new int[partitionLength];
		int[] y1=new int[partitionLength];
		int[] x2=new int[partitionLength];
		int[] y2=new int[partitionLength];
		
		for(int i=0;i<partitionLength;i++){
			x1[i]=games[0].outputVariables[i].getBDDVar();
			y1[i]=games[0].outputVariables[partitionLength+i].getBDDVar();
			x2[i]=games[0].outputVariables[2*partitionLength+i].getBDDVar();
			y2[i]=games[0].outputVariables[3*partitionLength+i].getBDDVar();
		}
						
//		int objective1 = tcg.noCollisionObjective(objX , objY , x1, y1);
//		int objective2 = tcg.noCollisionObjective(objX , objY , x2, y2);
		
		int objective1 = bdd.ref(bdd.not(tcg.stateToBDD(x1, dim-1)));
		int objective2 =  bdd.ref(bdd.not(tcg.stateToBDD(x2, dim-1)));
		//int objective3 = tcg.noCollisionObjective(x1, y1, x2, y2);
		int init = bdd.ref(bdd.and(games[0].getInit(), games[1].getInit()));
						
		int[] abstractionPredicates={init, objective1, objective2};
		
		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
		UtilityMethods.debugBDDMethods(bdd, "obj1 is ", objective1);
		UtilityMethods.debugBDDMethods(bdd, "obj2 is ", objective2);
		//UtilityMethods.debugBDDMethods(bdd, "obj3 is ", objective3);
		UtilityMethods.getUserInput();
		
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(games[0], objective1, bdd);
		//games[0].printGame();
		games[0].toGameGraph().draw("concGame0.dot", 1);
		GameSolution concSol1 = gs1.solve();
//		concSol1.print();
//		concSol1.drawWinnerStrategy("concSol1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		GameSolver gs2 = new GameSolver(games[1], objective2, bdd);
		GameSolution concSol2 = gs2.solve();
//		concSol2.print();
//		concSol2.drawWinnerStrategy("concSol2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		
//		Game composition = compose(bdd, games[0], games[1]);
////		composition.printGame();
////		composition.toGameGraph().draw("composition.dot", 1);
//		int objective = bdd.ref(bdd.and(objective1, objective2));
//		
////		UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
////		UtilityMethods.getUserInput();
//		
//		t0=System.currentTimeMillis();
//		GameSolver gs3 = new GameSolver(composition, objective, bdd);
//		GameSolution concSol3 = gs3.solve();
//		concSol3.print();
////		concSol3.drawWinnerStrategy("concSol3.dot");
//		t1=System.currentTimeMillis();
//		System.out.println("time for solving composition of games "+(t1-t0));
//		t=t1-t0;
		
		System.out.println("\n\n cs guided control \n\n");
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
//		solution1.print();
//		solution1.drawWinnerStrategy("absStrat1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
		//solution2.drawWinnerStrategy("absStrat2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		
		t0=System.currentTimeMillis();
		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
		t1=System.currentTimeMillis();
		System.out.println("time for composing "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
	}

}
