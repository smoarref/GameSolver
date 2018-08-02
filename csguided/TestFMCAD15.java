package csguided;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import utils.FileOps;
import utils.UtilityMethods;

public class TestFMCAD15 {
	public static void main(String[] args){
		
//		int dim = 4;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		
		for(int dim = 2048*2; dim < 2048*8; dim=dim*2){
			
		
		
			
		
			for(int j=1; j<3; j++){
				System.out.println("******************************************************************************************************************");
				System.out.println("**************************************************New Experiment**************************************************");
				System.out.println("experiment started at "+new Date().toString());
				System.out.println("dimension of the gird world is "+dim);
				System.out.println("synthesis method is ");
				switch(j){
					case 1 : System.out.println("central");
							break;
					case 2 : System.out.println("compositional");
							break;
					case 3 : System.out.println("Counter-Strategy-Guided central");
							break;
					case 4 : System.out.println("Counter-Strategy-Guided compositional");
							break;
//					default : System.out.println("Shouldn't reach here!");
				}
				BDD bdd = new BDD(10000,1000);
				test(bdd, dim, j);
				bdd.cleanup();
				System.out.flush();
			    String result=baos.toString();
			    //System.out.println(satisfying);
			    System.out.println("experiment finished at "+new Date().toString());
			    System.out.println("******************************************************************************************************************");
			    FileOps.write(result, "experimentResults.txt");
			    
			}
			
			
				
		
		}
		
		System.setOut(old);
	}
	
	public static void test(BDD bdd, int dim, int experiment ){
		try{
		GridCell2D env1Init = new GridCell2D(dim-1, dim-2);
//		GridCell2D env2Init = new GridCell2D(dim-1, dim-3);
		GridCell2D agent1Init = new GridCell2D(0, 0);
		GridCell2D agent2Init = new GridCell2D(0, 1);
		GridCell2D agent3Init = new GridCell2D(1, 1);
		GridCell2D agent4Init = new GridCell2D(1, 0);
		
		System.out.println("initializing the games");
		
//		ArrayList<GridCell2D> initialCells = new ArrayList<GridCell2D>();
//		initialCells.add(env1Init);
//		initialCells.add(agent1Init);
//		initialCells.add(agent2Init);
////		initialCells.add(agent3Init);
////		initialCells.add(agent4Init);
//		
//		ImplicitMultiAgentGameCaseStudy multiAgentGame = new ImplicitMultiAgentGameCaseStudy(bdd, dim, initialCells);
		
		
		ArrayList<GridCell2D> envInit=new ArrayList<GridCell2D>();
		envInit.add(env1Init);
//		envInit.add(env2Init);
		
		ArrayList<GridCell2D> sysInit=new ArrayList<GridCell2D>();
		sysInit.add(agent1Init);
		sysInit.add(agent2Init);
//		sysInit.add(agent3Init);
//		sysInit.add(agent4Init);
		
//		ImplicitMultiAgentGameCaseStudy multiAgentGame = new ImplicitMultiAgentGameCaseStudy(bdd, dim, envInit, sysInit);
		
		ImplicitMultiAgentGameCaseStudy2 multiAgentGame = new ImplicitMultiAgentGameCaseStudy2(bdd, dim, envInit, sysInit);
		

		
//		for(Game game :  multiAgentGame.getGames()){
////			ImplicitGameCaseStudy impGame = (ImplicitGameCaseStudy) game;
//			game.printGame();
//			impGame.toGameGraph().draw(impGame.getID()+".dot", 1, 0);
//			game.removeUnreachableStates().toGameGraph().draw(game.getID()+".dot",1, 0);
//			UtilityMethods.getUserInput();
//		}
//		
//		UtilityMethods.getUserInput();
		
		ArrayList<Game> games = multiAgentGame.getGames();
		
		ArrayList<Game> concreteGames = new ArrayList<Game>();
		
		for(int i=0;i<games.size();i++){
//			ImplicitGameCaseStudy igcs = (ImplicitGameCaseStudy) games.get(i);
			ImplicitGameCaseStudy2 igcs = (ImplicitGameCaseStudy2) games.get(i);
			concreteGames.add(igcs.toGame());
		}
		
		CompositeGame compGame = new CompositeGame(bdd, concreteGames);

		compGame.sysActions = multiAgentGame.sysActions;
		compGame.envActions = multiAgentGame.envActions;
		compGame.envAvlActions = multiAgentGame.envAvlActions;
		compGame.sysAvlActions = multiAgentGame.sysAvlActions;
		
//		UtilityMethods.getUserInput();
		
		System.out.println("creating the objectives");
		
		
		//creating the static obstacles
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
//		staticObstacles.add(new GridCell2D(1, 0));
//		staticObstacles.add(new GridCell2D(1, dim - 1));
//		staticObstacles.add(new GridCell2D(1, 1));
//		staticObstacles.add(new GridCell2D(1, dim - 2));
//		staticObstacles.add(new GridCell2D(dim-2, 0));
//		staticObstacles.add(new GridCell2D(dim-2, dim - 1));
//		staticObstacles.add(new GridCell2D(dim-2, 1));
//		staticObstacles.add(new GridCell2D(dim-2, dim - 2));
		
		int lowerBound = dim/3;
		int upperBound = 2*dim/3;
		
		for(int i=lowerBound+1; i<upperBound;i++){
			staticObstacles.add(new GridCell2D(0, i));
			staticObstacles.add(new GridCell2D(1, i));
			
			staticObstacles.add(new GridCell2D((dim-1)/2 , i ));
			staticObstacles.add(new GridCell2D((dim-1)/2+1, i ));
			
			staticObstacles.add(new GridCell2D(dim-2 , i ));
			staticObstacles.add(new GridCell2D(dim-1 , i ));
		}
		
		multiAgentGame.setStaticObstacles(staticObstacles);
		
		
		//initializing abstract predicates
		ArrayList<Integer> abstractPreds = new ArrayList<Integer>();
		
//		for(Integer ind : noCollisionWithEnvironmentObjectives ){
//			abstractPreds.add(ind);
//		}
//		
//		for(Integer col : noCollisionBetweenControllableAgents){
//			abstractPreds.add(col);
//		}
		
		//Abstraction predicates for compositional cs-guided control
		ArrayList<ArrayList<Integer>> abstractionPredicates = new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<games.size();i++){
			ArrayList<Integer> game_i_absPreds = new ArrayList<Integer>();
			abstractionPredicates.add(game_i_absPreds);
		}
		
		//defining objectives
		ArrayList<Integer> noCollisionWithEnvironmentObjectives = new ArrayList<Integer>();
		
		ArrayList<Variable[]> envXvars = multiAgentGame.getEnvXvars();
		ArrayList<Variable[]> envYvars = multiAgentGame.getEnvYvars();
		ArrayList<Variable[]> sysXvars = multiAgentGame.getSysXvars();
		ArrayList<Variable[]> sysYvars = multiAgentGame.getSysYvars();
		
		for(int i = 0; i<multiAgentGame.getNumberOfControllableAgents(); i++){
			int noCollisionWithStObs = multiAgentGame.noCollisionWithStaticObstacles(sysXvars.get(i), sysYvars.get(i), staticObstacles);
			
			abstractPreds.add(noCollisionWithStObs);
			
			for(int j=0; j<multiAgentGame.getNumberOfUncontrollableAgents(); j++){
				int noCollisionWithEnv =  multiAgentGame.noCollisionObjective(envXvars.get(j), envYvars.get(j), sysXvars.get(i), sysYvars.get(i));
				
				abstractPreds.add(noCollisionWithEnv);
				
				abstractionPredicates.get(i*multiAgentGame.getNumberOfUncontrollableAgents()+ j).add(noCollisionWithStObs);
				abstractionPredicates.get(i*multiAgentGame.getNumberOfUncontrollableAgents()+ j).add(noCollisionWithEnv);
				
				int indObj = bdd.ref(bdd.and(noCollisionWithEnv, noCollisionWithStObs));
				
//				bdd.deref(noCollisionWithEnv);
				
				noCollisionWithEnvironmentObjectives.add(indObj);
				
//				noCollisionWithEnvironmentObjectives.add(noCollisionWithEnv);
			}
//			bdd.deref(noCollisionWithStObs);
		}
		
//		for(int i=0; i<games.size();i++){
//			ImplicitGameCaseStudy igcs = (ImplicitGameCaseStudy) games.get(i);
//			int noCollisionWithEnv = igcs.noCollisionObjective(igcs.getEnvXvars(), igcs.getEnvYvars(), igcs.getSysXvars(), igcs.getSysYvars());
//			
////			int noCollisionWithStObs = igcs.noCollisionWithStaticObstacles(staticObstacles);
////			int indObj = bdd.ref(bdd.and(noCollisionWithEnv, noCollisionWithStObs));
////			bdd.deref(noCollisionWithEnv);
////			bdd.deref(noCollisionWithStObs);
////			noCollisionWithEnvironmentObjectives.add(indObj);
//			
//			noCollisionWithEnvironmentObjectives.add(noCollisionWithEnv);
//			
////			int notSameRow = bdd.ref(bdd.not(BDDWrapper.same(bdd, igcs.getEnvXvars(), igcs.getSysXvars())));
////			noCollisionWithEnvironmentObjectives.add(notSameRow);
//		}
		
		ArrayList<Integer> noCollisionBetweenControllableAgents = new ArrayList<Integer>();
		
//		ArrayList<Integer> formation = new ArrayList<Integer>();
		
		
		for(int i = 0; i<multiAgentGame.getNumberOfControllableAgents(); i++){
			for(int j=i+1; j<multiAgentGame.getNumberOfControllableAgents(); j++){
				int noCol = multiAgentGame.noCollisionObjective(sysXvars.get(i), sysYvars.get(i), sysXvars.get(j), sysYvars.get(j));
				noCollisionBetweenControllableAgents.add(noCol);
				
				abstractPreds.add(noCol);
				
				for(int k=0; k<games.size(); k++){
					abstractionPredicates.get(k).add(noCol);
				}
				
//				if(j==i+1){
//					int xFormation = BDDWrapper.same(bdd, sysXvars.get(i), sysXvars.get(j));
//					int yFormation = BDDWrapper.same(bdd, sysYvars.get(i), sysYvars.get(j));
//					int form = bdd.ref(bdd.or(xFormation, yFormation));
//					formation.add(form);
//					bdd.deref(xFormation);
//					bdd.deref(yFormation);
//					
//					abstractPreds.add(form);
//					
//					for(int k=0; k<games.size(); k++){
//						abstractionPredicates.get(k).add(form);
//					}
//				}
			}
		}
		
//		int numOfControllableAgents=multiAgentGame.getNumberOfControllableAgents(); 
//		for(int i = 0; i<numOfControllableAgents; i++){
//			int xFormation = BDDWrapper.same(bdd, sysXvars.get(i), sysXvars.get((i+1)%numOfControllableAgents));
//			int yFormation = BDDWrapper.same(bdd, sysYvars.get(i), sysYvars.get((i+1)%numOfControllableAgents));
//			
////			int xFormation = BDDWrapper.same(bdd, sysXvars.get(i), sysXvars.get(i+1));
////			int yFormation = BDDWrapper.same(bdd, sysYvars.get(i), sysYvars.get(i+1));
//			
//			int form = bdd.ref(bdd.or(xFormation, yFormation));
//			formation.add(form);
//			bdd.deref(xFormation);
//			bdd.deref(yFormation);
//			
//			abstractPreds.add(form);
//			
//			for(int k=0; k<games.size(); k++){
//				abstractionPredicates.get(k).add(form);
//			}
//		}
		
		
		
//		for(int i=0;i<games.size()-1;i++){
//			ImplicitGameCaseStudy igcs1 = (ImplicitGameCaseStudy) games.get(i);
//			for(int j=i+1;j<games.size();j++){
//				ImplicitGameCaseStudy igcs2 = (ImplicitGameCaseStudy) games.get(j);
//				noCollisionBetweenControllableAgents.add(igcs1.noCollisionObjective(igcs1.getSysXvars(), igcs1.getSysYvars(), igcs2.getSysXvars(), igcs2.getSysYvars()));
//				
////				int xFormation = BDDWrapper.same(bdd, igcs1.getSysXvars(), igcs2.getSysXvars());
////				int yFormation = BDDWrapper.same(bdd, igcs1.getSysYvars(), igcs2.getSysYvars());
////				int form = bdd.ref(bdd.or(xFormation, yFormation));
////				formation.add(form);
////				bdd.deref(xFormation);
////				bdd.deref(yFormation);
//			}
//		}
		
		
		
		
		int collectiveObjective = bdd.ref(bdd.getOne());
		
		for(Integer obj : noCollisionBetweenControllableAgents){
			collectiveObjective = bdd.andTo(collectiveObjective, obj);
			
		}
		
//		for(Integer obj : formation){
//			collectiveObjective = bdd.andTo(collectiveObjective, obj);
//		}
		
		int objective = bdd.ref(bdd.and(collectiveObjective, noCollisionWithEnvironmentObjectives.get(0)));
		for(int i=1;i<noCollisionWithEnvironmentObjectives.size();i++){
			objective = bdd.andTo(objective, noCollisionWithEnvironmentObjectives.get(i));
		}
		
		int noCollisionWithEnv = bdd.ref(bdd.getOne());
		for(Integer obj : noCollisionWithEnvironmentObjectives){
			noCollisionWithEnv = bdd.andTo(noCollisionWithEnv, obj);
		}
		
//		System.out.println("No collision with environment");
//		for(Integer c : noCollisionWithEnvironmentObjectives){
//			bdd.printSet(c);
//		}
//		
//		System.out.println("No collion between system players");
//		for(Integer c : noCollisionBetweenControllableAgents){
//			bdd.printSet(c);
//		}
//		
//		UtilityMethods.getUserInput();
		
		
		//reachability objectives
//		for(int i=0; i<games.size();i++){
//			int bound = dim+1;
//			String reachabilityGameNamePefix="reach";
//			
//			ImplicitGameCaseStudy2 rg = (ImplicitGameCaseStudy2) games.get(i);
//			int reachRow = BDDWrapper.assign(bdd, dim-1, rg.getSysYvars());
//			int reachRowPrime = BDDWrapper.replace(bdd, reachRow, rg.getVtoVprime());
//			Game reach = concreteGames.get(i).gameWithBoundedReachabilityObjective(bound, reachabilityGameNamePefix+i, reachRow, reachRowPrime);
//			games.set(i, reach);
//			concreteGames.set(i, reach);
//			
//			Variable flag = Variable.getVariable(reach.variables, reachabilityGameNamePefix+i+"f");
////			flag.print();
////			UtilityMethods.getUserInput();
//			
//			int flagup = bdd.ref(flag.getBDDVar());
//			abstractPreds.add(flagup);
////			abstractionPredicates.get(i).add(flagup);
//			
//			abstractPreds.add(reachRow);
//			
//			abstractionPredicates.get(i).add(reachRow);
//			
//		}
//		
//		compGame = new CompositeGame(bdd, concreteGames);
		
//		ImplicitGameCaseStudy rg = (ImplicitGameCaseStudy) games.get(0);
//		ImplicitGameCaseStudy rg2 = (ImplicitGameCaseStudy) games.get(1);
////		ImplicitGameCaseStudy rg3 = (ImplicitGameCaseStudy) games.get(2);
////		int reachCell = rg.assignCell(rg.getSysXvars(), rg.getSysYvars(), new GridCell2D(0, dim-1));
////		int reachCellPrime = BDDWrapper.replace(bdd, reachCell, rg.getVtoVprime());
//		
//		int reachRow = BDDWrapper.assign(bdd, dim-2, rg.getSysXvars());
//		int reachRow2 = BDDWrapper.assign(bdd, dim-2, rg2.getSysXvars());
////		int reachRow3 = BDDWrapper.assign(bdd, dim-2, rg3.getSysXvars());
////		UtilityMethods.debugBDDMethods(bdd, "row", reachRow);
//		int reachRowPrime = BDDWrapper.replace(bdd, reachRow, rg.getVtoVprime());
//		int reachRowPrime2 = BDDWrapper.replace(bdd, reachRow2, rg2.getVtoVprime());
////		int reachRowPrime3 = BDDWrapper.replace(bdd, reachRow3, rg3.getVtoVprime());
//		
//		Game reach = concreteGames.get(0).gameWithBoundedReachabilityObjective(dim-2, "reach0", reachRow, reachRowPrime);
//		Game reach2 = concreteGames.get(1).gameWithBoundedReachabilityObjective(dim-2, "reach1", reachRow2, reachRowPrime2);
////		Game reach3 = concreteGames.get(2).gameWithBoundedReachabilityObjective(dim-2, "reach2", reachRow3, reachRowPrime3);
////		reach.printGameVars();
////		reach.printGame();
//		games.set(0, reach);
//		concreteGames.set(0, reach);
//		games.set(1, reach2);
//		concreteGames.set(1, reach2);
////		games.set(2, reach3);
////		concreteGames.set(2, reach3);
//		compGame = new CompositeGame(bdd, concreteGames);
//		compGame.printGameVars();
////		UtilityMethods.getUserInput();
//		
////		multiAgentGame.composeVars();
////		multiAgentGame.printGameVars();
		

		
		//initializing abstract predicates
//		ArrayList<Integer> abstractPreds = new ArrayList<Integer>();
//		
//		for(Integer ind : noCollisionWithEnvironmentObjectives ){
//			abstractPreds.add(ind);
//		}
//		
//		for(Integer col : noCollisionBetweenControllableAgents){
//			abstractPreds.add(col);
//		}
		
//		ArrayList<ArrayList<Integer>> abstractionPredicates = new ArrayList<ArrayList<Integer>>();
//		for(int i=0;i<games.size();i++){
//			ArrayList<Integer> game_i_absPreds = new ArrayList<Integer>();
//			game_i_absPreds.add(noCollisionWithEnvironmentObjectives.get(i));
//			game_i_absPreds.add(collectiveObjective);
//			abstractionPredicates.add(game_i_absPreds);
//			
//		}
		
//		for(Integer form : formation){
//			abstractPreds.add(form);
//		}
		
		System.out.println(new Date().toString());
		long t0_whole = UtilityMethods.timeStamp();
		Game solution;
		
		if(experiment == 1){
	//		Game solution = testCentral(bdd, games, objective);
			solution = testCentral(bdd, concreteGames, objective);
		}else if(experiment == 2){
			//		Game solution = testCompositional(bdd, games, noCollisionWithEnvironmentObjectives, collectiveObjective);
			solution = testCompositional(bdd, concreteGames, noCollisionWithEnvironmentObjectives, collectiveObjective);
		}else if(experiment ==3){
//			Game  solution = testCSguidedCentral(bdd, multiAgentGame, objective, abstractPreds);
			solution = testCSguidedCentral(bdd, compGame, objective, abstractPreds);
		}else{
//			Game solution = testCSguidedCompositional2(bdd, multiAgentGame, noCollisionWithEnvironmentObjectives, collectiveObjective, abstractionPredicates);
			solution = testCSguidedCompositional(bdd, compGame, noCollisionWithEnvironmentObjectives, collectiveObjective, abstractionPredicates);
		}
		
		
//		Game solution = testCSguidedCompositional(bdd, multiAgentGame, noCollisionWithEnvironmentObjectives, collectiveObjective, abstractPreds);
//		Game solution = testCSguidedCompositional(bdd, compGame, noCollisionWithEnvironmentObjectives, collectiveObjective, abstractPreds);
		
		
		
//		Game solution = testCSguided(bdd, games.get(0), noCollisionWithEnvironmentObjectives.get(0), 
//				new int[]{games.get(0).getInit(), noCollisionWithEnvironmentObjectives.get(0)});
		
		
		//testing compositional
//		ImplicitGameCaseStudy imgc1 = (ImplicitGameCaseStudy) games.get(0);
//		ImplicitGameCaseStudy imgc2 = (ImplicitGameCaseStudy) games.get(1);
//		int obj1 = imgc1.assignCell(imgc1.getSysXvars(), imgc1.getSysYvars(), new GridCell2D(1, 1));
//		int obj2 = imgc2.assignCell(imgc2.getSysXvars(), imgc2.getSysYvars(), new GridCell2D(1, 1));
//		int obj = bdd.ref(bdd.and(obj1, obj2));
//		int obj3 = imgc1.assignCell(imgc1.getEnvXvars(), imgc1.getEnvYvars(), new GridCell2D(0, 0));
//		obj = bdd.andTo(obj, obj3);
//		
//		testCompositeGame(bdd, multiAgentGame, obj);
//		
//		UtilityMethods.getUserInput();
//		
		System.out.println(new Date().toString());
		UtilityMethods.duration(t0_whole, "the whole process took", 500);
		
		//testing the solution

//		bdd.printSet(solution.getSystemTransitionRelation());
		

		
//		int noColEnv = bdd.ref(bdd.replace(noCollisionWithEnv, multiAgentGame.getVtoVprime()));
//		
//		int collectiveObjectivePrime = bdd.ref(bdd.replace(collectiveObjective, multiAgentGame.getVtoVprime()));
//		
//		int indObj0 = bdd.ref(bdd.replace(noCollisionWithEnvironmentObjectives.get(0), multiAgentGame.getVtoVprime()));
		
//		bdd.printSet(indObj0);
//		System.out.println("objective is");
//		bdd.printSet(objective);
//		
//		UtilityMethods.debugBDDMethods(bdd, "not obj Prime is ", bdd.not(objPrime));
//		
//		UtilityMethods.getUserInput();
		

		
//		int badIndStates=bdd.ref(bdd.and(t, bdd.not(noColEnv)));
//		
//		int badCollectiveStates = bdd.ref(bdd.and(t, bdd.not(collectiveObjectivePrime)));
//		
//		int badIndObj0=bdd.ref(bdd.and(t, bdd.not(indObj0)));
		
		System.out.println("*************************");
		System.out.println("\nchecking the solution");
//		bdd.printSet(badStates);
		int t = solution.getSystemTransitionRelation();
		int objPrime = bdd.ref(bdd.replace(objective, multiAgentGame.getVtoVprime()));
		int badStates = bdd.ref(bdd.and(t, bdd.not(objPrime)));
		if(badStates != 0 ){
			System.out.println("something is wrong in the synthesized strategy!!:(");
		}else{
			System.out.println("evrything seems fine!:)");
		}
		
//		UtilityMethods.getUserInput();
		UtilityMethods.memoryUsage();
		BDDWrapper.BDD_Usage(bdd);
		
//		solution.printGame();
//		solution.printGameVars();
		
		}catch(Exception e){
			System.out.println("There was an exception in test "+experiment+" with dim "+dim);
		}

	}
	
	public static void checkSolution(BDD bdd, Game solution, int objective, Permutation VtoVprime){
		System.out.println("*************************");
		System.out.println("\nchecking the solution");
//		bdd.printSet(badStates);
		int t = solution.getSystemTransitionRelation();
		int objPrime = bdd.ref(bdd.replace(objective, VtoVprime));
		int badStates = bdd.ref(bdd.and(t, bdd.not(objPrime)));
		if(badStates != 0 ){
			System.out.println("something is wrong in the synthesized strategy!!:(");
		}else{
			System.out.println("evrything seems fine!:)");
		}
	}
	
	public static Game testCentral(BDD bdd, ArrayList<Game> games, int objective){
    	System.out.println("central");
		Game compositionOfGames = games.get(0);
		
		
		long t0_central = UtilityMethods.timeStamp();
		for(int i=1;i<games.size();i++){
			compositionOfGames = compositionOfGames.compose(games.get(i));
			games.get(i).cleanUp();
		}
		
//		compositionOfGames.printGame();
//		UtilityMethods.getUserInput();
//		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
//		UtilityMethods.getUserInput();
		System.out.println("solving the game");
		GameSolver gs_central = new GameSolver(compositionOfGames, objective, bdd);
		GameSolution centralSol = gs_central.solve();
		centralSol.print();
		UtilityMethods.duration(t0_central, "central solution ", 500);
//		centralSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("central.dot", 1, 0);
		BDDWrapper.BDD_Usage(bdd);
		return centralSol.strategyOfTheWinner();
    }
    
    public static Game testCSguided(BDD bdd, Game game, int objective, int[] abstractPreds){
    	System.out.println("testing cs guided on an individual game");
    	long t0_csguided = UtilityMethods.timeStamp();
    	FastCSGuidedControl csg = new FastCSGuidedControl(bdd, game, abstractPreds, objective);
		GameSolution abstractSol = csg.counterStrategyGuidedControl();
		abstractSol.print();
		
		
//		abstractSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("central.dot", 1, 0);
		BDDWrapper.BDD_Usage(bdd);
		
//		abstractSol.getGameStructure().printGame();
//		UtilityMethods.getUserInput();
//		abstractSol.strategyOfTheWinner().printGame();
//		UtilityMethods.getUserInput();
		
		AbstractGame absGame = (AbstractGame) abstractSol.getGameStructure();
		Game abstractSolution = abstractSol.strategyOfTheWinner();
		
		AbstractGame restricted = absGame.restrict(abstractSolution.getEnvironmentTransitionRelation(), abstractSolution.getSystemTransitionRelation());
		
//		UtilityMethods.debugBDDMethods(bdd, "restricted: T_sys",restricted.getSystemTransitionRelation());
		
		Game concreteStrat = restricted.concretizeAbstractStrategy();
		
		UtilityMethods.duration(t0_csguided, "cs guided central solution ", 500);
		
		return concreteStrat;
    }
    
    public static Game testCSguidedCentral(BDD bdd, CompositeGame compGame, int objective, ArrayList<Integer> abstractPredicates){
    	System.out.println("cs guided central");
    	long t0_csguided = UtilityMethods.timeStamp();
    	
    	
    	ArrayList<Game> games = compGame.getGames();
		Game compositionOfGames = games.get(0);
		for(int i=1;i<games.size();i++){
			compositionOfGames = compositionOfGames.compose(games.get(i));
			games.get(i).cleanUp();
		}
		if(games.size()>1) games.get(0).cleanUp();
		
    	
		
		
//		UtilityMethods.debugBDDMethods(bdd, "env trans of comp game", compositionOfGames.getEnvironmentTransitionRelation());
		
		ArrayList<Integer> absPredsArraylist = new ArrayList<Integer>();
//		absPredsArraylist.add(compositionOfGames.getInit());
		absPredsArraylist.add(compGame.getInit());
		absPredsArraylist.addAll(abstractPredicates);
//		abstractPredicates.add(compositionOfGames.getInit());
		int[] absPreds = UtilityMethods.IntegerArrayListTointArray(absPredsArraylist);
		
//		for(int i=0 ; i<absPreds.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "abs preds are ", absPreds[i]);
//		}
//		UtilityMethods.getUserInput();
		
		
//		FastCSGuidedControl csg = new FastCSGuidedControl(bdd, compositionOfGames, absPreds, objective);
//		GameSolution abstractSol = csg.counterStrategyGuidedControl();
//		abstractSol.print();
//		UtilityMethods.duration(t0_csguided, "cs guided central solution ", 500);
		
		//all games should have access to all variables
//		for(Game game : compGame.getGames()){
//			game.setVariables(compGame.variables);
//    		game.setInit(compGame.getInit());
//		}
		
//		FastCSGuidedControl csg = new FastCSGuidedControl(bdd, compGame, absPreds, objective);
		FastCSGuidedControl csg = new FastCSGuidedControl(bdd, compositionOfGames, absPreds, objective);
		GameSolution abstractSol = csg.counterStrategyGuidedControl();
		abstractSol.print();
		
		
//		abstractSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("central.dot", 1, 0);
		BDDWrapper.BDD_Usage(bdd);
		
//		abstractSol.getGameStructure().printGame();
//		UtilityMethods.getUserInput();
//		abstractSol.strategyOfTheWinner().printGame();
//		UtilityMethods.getUserInput();
		
		AbstractGame absGame = (AbstractGame) abstractSol.getGameStructure();
		Game abstractSolution = abstractSol.strategyOfTheWinner();
		
//		UtilityMethods.debugBDDMethods(bdd, "absGame: T_sys",absGame.getSystemTransitionRelation());
		
		AbstractGame restricted = absGame.restrict(abstractSolution.getEnvironmentTransitionRelation(), abstractSolution.getSystemTransitionRelation());
		
//		UtilityMethods.debugBDDMethods(bdd, "restricted: T_sys",restricted.getSystemTransitionRelation());
		
		Game concreteStrat = restricted.concretizeAbstractStrategy();
		
		
//		UtilityMethods.debugBDDMethods(bdd, "concreteStrategy: T_env", concreteStrat.getEnvironmentTransitionRelation());
//		UtilityMethods.debugBDDMethods(bdd, "concreteStrategy: T_sys", concreteStrat.getSystemTransitionRelation());
		
//		absGame.printGameVars();
		
//		UtilityMethods.debugBDDMethods(bdd, "env trans", concreteStrat.getEnvironmentTransitionRelation());
		
//		absGame.printGameVars();
//		absGame.printEquivalenceClasses();
//		concreteStrat.printGame();
//		UtilityMethods.getUserInput();
		UtilityMethods.duration(t0_csguided, "cs guided central solution ", 500);
		
		
		System.out.println("number of variables in abstraction "+absGame.numOfVariables());
		
		return concreteStrat;
    }
    
    
    
    public static Game testCSguidedCompositional(BDD bdd, CompositeGame compGame, ArrayList<Integer> individualObjectives, int collectiveObjective, ArrayList<ArrayList<Integer>> abstractPredicates){
    	System.out.println("cs guided compositional");
    	long t0_csguidedComp = UtilityMethods.timeStamp();
    	ArrayList<ArrayList<Integer>> absPredsArraylist = new ArrayList<ArrayList<Integer>>();
//    	compGame.getVariables();
    	
    	for(int i=0; i<abstractPredicates.size();i++){
    		ArrayList<Integer> absp = new ArrayList<Integer>();
    		absp.add(compGame.getInit());
    		absp.addAll(abstractPredicates.get(i));
    		absPredsArraylist.add(absp);
    	}
		ArrayList<int[]> absPreds = new ArrayList<int[]>();
		for(int i=0; i<absPredsArraylist.size();i++){
			absPreds.add(UtilityMethods.IntegerArrayListTointArray(absPredsArraylist.get(i)));
		}
		
		ArrayList<Game> games = compGame.getGames();
		
		Game compositionOfGames = games.get(0);
		for(int i=1;i<games.size();i++){
			compositionOfGames = compositionOfGames.compose(games.get(i));
//			games.get(i).cleanUp();
		}
		
		Game compositeGame = compositionOfGames;
		
//		Game compositeGame = compGame;
	
		
    	if(individualObjectives.size() !=  games.size()){
    		System.err.println("number of individual objectives is not the same as number of games");
    		return null;
    	}
    	
    	System.out.println("solving individual objectives");
    	ArrayList<AbstractGame> finalAbsGames=new ArrayList<AbstractGame>();
    	ArrayList<GameSolution> indSols=new ArrayList<GameSolution>();
    	ArrayList<AbstractGame> restrictedAbsGames= new ArrayList<AbstractGame>();
    	for(int i=0;i<individualObjectives.size();i++){
//    	for(int i=individualObjectives.size()-1;i>=0;i--){
    		
    		Game g = games.get(i);
//    		System.out.println("solving for "+g.getID());
//    		UtilityMethods.getUserInput();
    		
    		g.setVariables(compGame.variables);
    		g.setInit(compGame.getInit());
    		
    		FastCSGuidedControl csg = new FastCSGuidedControl(bdd, g, absPreds.get(i), individualObjectives.get(i));
			GameSolution abstractSol = csg.counterStrategyGuidedControl();
			abstractSol.print();
			indSols.add(abstractSol);
			AbstractGame absGame = (AbstractGame) abstractSol.getGameStructure();
			
			finalAbsGames.add(absGame);
			
//			System.out.println("final abstraction for ind obj for game "+i);
//			absGame.printEquivalenceClasses();
//			absGame.printGame();
//			UtilityMethods.getUserInput();
			
			restrictedAbsGames.add(absGame.restrict(abstractSol.strategyOfTheWinner().getEnvironmentTransitionRelation(), abstractSol.strategyOfTheWinner().getSystemTransitionRelation()));
			
			bdd.gc();
//			System.out.println("bdd root nodes"+bdd.countRootNodes());
    	}
    	
    	
    	
    	System.out.println("composing individual solutions");
    	
    	AbstractGame composedAbsGame = restrictedAbsGames.get(0);
    	
    	for(int i=1;i<restrictedAbsGames.size();i++){
    		composedAbsGame = composedAbsGame.compose(restrictedAbsGames.get(i));
    		
    		restrictedAbsGames.get(i).cleanUp();
    	}
    	
    	bdd.gc();
    	//TODO: why is this different from above?
//    	AbstractGame composedAbsGame = AbstractGame.compose(restrictedAbsGames);
    	
    	
    	
//    	AbstractGame composedFinalAbstractGames = AbstractGame.compose(finalAbsGames);
    	
    	
//    	composedAbsGame.printEquivalenceClasses();
//    	UtilityMethods.getUserInput();
//    	
//    	composedFinalAbstractGames.printEquivalenceClasses();
//    	UtilityMethods.getUserInput();
    	
//    	composedAbsGame.printGame();
//    	Variable.printVariables(compGame.actionVars);
//    	Variable.printVariables(compGame.variables);
//    	UtilityMethods.getUserInput();
//    	composedAbsGame.printEquivalenceClasses();
//    	UtilityMethods.getUserInput();
    	
//    	compGame.printGame();
//    	UtilityMethods.getUserInput();
    	
    	
    	
    	System.out.println("solving for the collective objective");
    	
    	int abstractObj = composedAbsGame.computeAbstractObjective(collectiveObjective);
    	
//    	UtilityMethods.debugBDDMethods(bdd, "abs obj", abstractObj);
//    	
//    	UtilityMethods.getUserInput();
    	
    	GameSolver gs = new GameSolver(composedAbsGame, abstractObj, bdd);
    	GameSolution sol = gs.solve();
    	sol.print();
//    	UtilityMethods.getUserInput();
    	
    	
    	
    	Game concreteStrat;
    	
    	if(sol.getWinner() == Player.ENVIRONMENT){
    		
    		//cleaning the solution
    		sol.cleanUp(bdd);
    		composedAbsGame.cleanUp();
    		
    		
    		int objective = bdd.ref(collectiveObjective);
    		for(int i=0; i<individualObjectives.size();i++){
    			objective = bdd.andTo(objective, individualObjectives.get(i));
    		}
    		
//    		UtilityMethods.debugBDDMethods(bdd, "collective obj is ", collectiveObjective);
//    		UtilityMethods.debugBDDMethods(bdd, "obj is ", objective);
//    		UtilityMethods.getUserInput();
    	
	    	AbstractGame composedFinalAbstractGames = finalAbsGames.get(0);
	    	
	    	for(int i=1;i<finalAbsGames.size();i++){
	    		composedFinalAbstractGames = composedFinalAbstractGames.compose(finalAbsGames.get(i));
	    	}
	    	
//	    	composedFinalAbstractGames.printEquivalenceClasses();
//	    	UtilityMethods.getUserInput();
//	    	
//	    	composedFinalAbstractGames.printGame();
//	    	UtilityMethods.getUserInput();
	    	
	    	
//	    	int abstractObj2 = composedFinalAbstractGames.computeAbstractObjective(objective);
//	    	GameSolver gs1 = new GameSolver(composedFinalAbstractGames, abstractObj2, bdd);
//	    	GameSolution sol1 = gs1.solve();
//	    	sol1.print();
//	    	UtilityMethods.getUserInput();
	    	
	    	
//	    	UtilityMethods.debugBDDMethods(bdd, "collective obj is", collectiveObjective);
//	    	UtilityMethods.getUserInput();
	    	
//	    	FastCSGuidedControl csg = new FastCSGuidedControl(bdd, compGame, composedFinalAbstractGames, objective);
//	    	FastCSGuidedControl csg = new FastCSGuidedControl(bdd, composedAbsGame.getConcreteGame(), composedAbsGame, collectiveObjective);
	    	
	    	FastCSGuidedControl csg = new FastCSGuidedControl(bdd, compositeGame, composedFinalAbstractGames, objective);
			GameSolution abstractSol = csg.counterStrategyGuidedControlWithInitialAbstraction();
			abstractSol.print();
//			UtilityMethods.getUserInput();
			
			
			AbstractGame absGame = (AbstractGame) abstractSol.getGameStructure();
			
			System.out.println("num of variables in abstract game "+absGame.numOfVariables());
//			absGame.printEquivalenceClasses();
			
			Game abstractSolution = abstractSol.strategyOfTheWinner();
			
			AbstractGame restricted = absGame.restrict(abstractSolution.getEnvironmentTransitionRelation(), abstractSolution.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "restricted: T_sys",restricted.getSystemTransitionRelation());
			
			concreteStrat = restricted.concretizeAbstractStrategy();
    	}else{
    		AbstractGame absGame = (AbstractGame) sol.getGameStructure();
			Game abstractSolution = sol.strategyOfTheWinner();
			
			System.out.println("num of variables in abstract game "+absGame.numOfVariables());
			
			AbstractGame restricted = absGame.restrict(abstractSolution.getEnvironmentTransitionRelation(), abstractSolution.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "restricted: T_sys",restricted.getSystemTransitionRelation());
			
			concreteStrat = restricted.concretizeAbstractStrategy();
    	}
		
    	
    	UtilityMethods.duration(t0_csguidedComp, "compositional cs guided control took");
    	
//		abstractSol.strategyOfTheWinner().printGame();
    	
		
//		UtilityMethods.debugBDDMethods(bdd, "env trans of comp game", compositionOfGames.getEnvironmentTransitionRelation());
		
//		abstractSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("central.dot", 1, 0);
		BDDWrapper.BDD_Usage(bdd);
		
//		abstractSol.getGameStructure().printGame();
//		UtilityMethods.getUserInput();
//		abstractSol.strategyOfTheWinner().printGame();
//		UtilityMethods.getUserInput();
		
		
		
//		UtilityMethods.debugBDDMethods(bdd, "absGame: T_sys",absGame.getSystemTransitionRelation());
		
		
		
		return concreteStrat;
    }
    
    public static Game testCompositional(BDD bdd, ArrayList<Game> games, ArrayList<Integer> individualObjective, int collectiveObjective){
    	System.out.println("compositional");
		long t0_compositional = UtilityMethods.timeStamp();
		
//		CompositeGame cg = new CompositeGame(bdd, games);
//		for(Game game : games){
//			game.setVariables(cg.variables);
//		}
		
		System.out.println("Solving the games for individual objectives");
		
		ArrayList<GameSolution> concreteSolutionsForIndObjectives = new ArrayList<GameSolution>();
		for(int i=0; i<games.size();i++){
			System.out.println("Solving for the game "+games.get(i).getID());
//			 games.get(i).printGame();
//			if(i>0) games.get(i).printGame();
//			UtilityMethods.debugBDDMethods(bdd, "and the objective", noCollisionWithEnvironmentObjectives.get(i));
			GameSolver gs = new GameSolver(games.get(i), individualObjective.get(i), bdd);
//			UtilityMethods.getUserInput();
			GameSolution s = gs.solve();
			concreteSolutionsForIndObjectives.add(s);
		}

		
		System.out.println("Concrete games solutions for individual objectives");
		int n=1;
		for(GameSolution s : concreteSolutionsForIndObjectives){
//			s.strategyOfTheWinner().printGame();
//			s.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("conc"+n+".dot", 1, 0);
			n++;
			s.print();
//			s.getGameStructure().cleanUp();
//			UtilityMethods.getUserInput();
		}
		
//		UtilityMethods.getUserInput();
		
		System.out.println("Composing the games");
		
		Game composedGame = concreteSolutionsForIndObjectives.get(0).strategyOfTheWinner();
		for(int i=1;i<concreteSolutionsForIndObjectives.size();i++){
			composedGame = composedGame.compose(concreteSolutionsForIndObjectives.get(i).strategyOfTheWinner());
		}
		
		//free the memory
		for(int i=0;i<games.size();i++){
			games.get(i).cleanUp();
			concreteSolutionsForIndObjectives.get(i).strategyOfTheWinner().cleanUp();
		}
		
//		System.out.println("printing the composition of the solutuions");
		
//		composedGame.printGame();
//		
//		composedGame.removeUnreachableStates().toGameGraph().draw("composedSol.dot", 1, 0);
//		UtilityMethods.debugBDDMethods(bdd,"init set of composed game", composedGame.getInit());
//		
//		UtilityMethods.getUserInput();
		
		
		
//		UtilityMethods.debugBDDMethods(bdd, "collective obj", collectiveObjective);
//		UtilityMethods.getUserInput();
		

		
		System.out.println("Solving for collective objectives");
		GameSolver gs = new GameSolver(composedGame, collectiveObjective, bdd);
		GameSolution concreteCompSol = gs.solve();
		UtilityMethods.duration(t0_compositional, "compositional", 500);
//		concreteCompSol.strategyOfTheWinner().printGame();
//		concreteCompSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("compStrat.dot", 1, 0);
//		concreteCompSol.print();
		
//		concreteCompSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("composedSolution.dot", 1, 0);
		
		
    	
//		ImplicitGameCaseStudy g = (ImplicitGameCaseStudy) games.get(0);
//		
//		ArrayList<Variable[]> envX = new ArrayList<Variable[]>();
//		envX.add(g.getEnvXvars());
//		
//		ArrayList<Variable[]> envY = new ArrayList<Variable[]>();
//		envY.add(g.getEnvYvars());
//		
//		ArrayList<Variable[]> sysX = new ArrayList<Variable[]>();
//		sysX.add(g.getSysXvars());
//		
//		ArrayList<Variable[]> sysY = new ArrayList<Variable[]>();
//		sysY.add(g.getSysYvars());
//        add(new RoboticMP2D(h, w, dim, dim, bdd, g, envX, envY, sysX, sysY));
		
		BDDWrapper.BDD_Usage(bdd);
		
		
		
		return concreteCompSol.strategyOfTheWinner();
    }
    
    public static void testCompositeGame(BDD bdd, ImplicitMultiAgentGameCaseStudy multiAgentGame, int objective){
    	for(Game g : multiAgentGame.getGames()){
    		g.setVariables(multiAgentGame.variables);
    	}
    	
    	ArrayList<Game> games = multiAgentGame.getGames();
		Game compositionOfGames = games.get(0);
		for(int i=1;i<games.size();i++){
			compositionOfGames = compositionOfGames.compose(games.get(i));
//			games.get(i).cleanUp();
		}
    	
//    	int T_env  = multiAgentGame.getEnvironmentTransitionRelation();
//    	int T_sys = multiAgentGame.getSystemTransitionRelation();
    	
    	int T_env = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.ENVIRONMENT)){
    		T_env = bdd.orTo(T_env, multiAgentGame.getNextEnvTransition());
    	}
    	
    	int T_sys = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.SYSTEM)){
    		T_sys = bdd.orTo(T_sys, multiAgentGame.getNextSysTransition());
    	}
    	
    	UtilityMethods.debugBDDMethods(bdd, "MAS T_env", T_env);
    	UtilityMethods.getUserInput();
    	
    	UtilityMethods.debugBDDMethods(bdd, "MAS T_sys", T_sys);
    	UtilityMethods.getUserInput();
    	
    	UtilityMethods.debugBDDMethods(bdd, "comp game T_env", compositionOfGames.getEnvironmentTransitionRelation());
    	UtilityMethods.getUserInput();
    	
    	UtilityMethods.debugBDDMethods(bdd, "comp game T_sys", compositionOfGames.getSystemTransitionRelation());
    	UtilityMethods.getUserInput();
    	
    	System.out.println("checking if equivalent");
    	if(T_env == compositionOfGames.getEnvironmentTransitionRelation()){
    		System.out.println("env equal");
    	}else{
    		System.out.println("env not equal!");
    	}
    	
    	if(T_sys == compositionOfGames.getSystemTransitionRelation()){
    		System.out.println("sys equal");
    	}else{
    		System.out.println("sys not equal!");
    	}
    	
    	UtilityMethods.getUserInput();
    	
    	T_env = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.ENVIRONMENT)){
    		T_env = bdd.orTo(T_env, multiAgentGame.getNextEnvTransition());
    	}
    	
    	T_sys = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.SYSTEM)){
    		T_sys = bdd.orTo(T_sys, multiAgentGame.getNextSysTransition());
    	}
    	
    	System.out.println("checking if equivalent");
    	if(T_env == compositionOfGames.getEnvironmentTransitionRelation()){
    		System.out.println("env equal");
    	}else{
    		System.out.println("env not equal!");
    	}
    	
    	if(T_sys == compositionOfGames.getSystemTransitionRelation()){
    		System.out.println("sys equal");
    	}else{
    		System.out.println("sys not equal!");
    	}
    	
    	UtilityMethods.getUserInput();
    	
    	T_env = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.ENVIRONMENT)){
    		T_env = bdd.orTo(T_env, multiAgentGame.getNextEnvTransition());
    	}
    	
    	T_sys = bdd.ref(bdd.getZero());
    	while(multiAgentGame.hasNextTransition(Player.SYSTEM)){
    		T_sys = bdd.orTo(T_sys, multiAgentGame.getNextSysTransition());
    	}
    	
    	System.out.println("checking if equivalent");
    	if(T_env == compositionOfGames.getEnvironmentTransitionRelation()){
    		System.out.println("env equal");
    	}else{
    		System.out.println("env not equal!");
    	}
    	
    	if(T_sys == compositionOfGames.getSystemTransitionRelation()){
    		System.out.println("sys equal");
    	}else{
    		System.out.println("sys not equal!");
    	}
    	
    	UtilityMethods.getUserInput();
    	
    	multiAgentGame.testTransitionRelations();
    	
    	System.out.println("testing epre");
    	
    	int cube = multiAgentGame.getPrimedVariablesAndActionsCube();
//    	int objPrime = bdd.ref(bdd.replace(objective, multiAgentGame.getVtoVprime()));
    	int epre = multiAgentGame.EpreImage(objective, cube, Player.SYSTEM);
    	UtilityMethods.debugBDDMethods(bdd, "system epre of objective is ", epre);
    	UtilityMethods.getUserInput();
    	
//    	int eprePrime = bdd.ref(bdd.replace(epre, multiAgentGame.getVtoVprime()));
    	int envEpre = multiAgentGame.EpreImage(epre, cube, Player.ENVIRONMENT);
    	UtilityMethods.debugBDDMethods(bdd, "env epre of objective is ", envEpre);
    	UtilityMethods.getUserInput();
    	
    	System.out.println("testing transition relations again");
    	multiAgentGame.testTransitionRelations();
    }
}
