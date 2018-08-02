package csguided;

import java.util.ArrayList;

import javax.rmi.CORBA.Util;

import jdd.bdd.BDD;
import jdd.sat.Var;
import utils.UtilityMethods;

public class ImplicitMultiAgentGameCaseStudy2 extends CompositeGame{
	
	int numOfAgents;
	int numOfControllableAgents;
	int numOfUncontrollableAgents;

	int xDimension;
	int yDimension;
	
	int numOfXBits;
	int numOfYBits;
	
	
	
	/**
	 * Environment (uncontrollable) agents X and Y variables and their primed copy
	 */
	ArrayList<Variable[]> envXvars;
	ArrayList<Variable[]> envXvarsPrime;
	
	ArrayList<Variable> envDirection;
	ArrayList<Variable> envDirectionPrime;
	
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
	
	
	/**
	 * Action variables
	 */
//	Variable[] envActionVars;
	ArrayList<Variable[]> uncontrollabelAgentsActionVars;
	ArrayList<Variable[]> agentsActionVars;
	
	//Static obstacles 
	ArrayList<GridCell2D> staticObstacles=null;
	
	public ImplicitMultiAgentGameCaseStudy2(BDD argBDD){
		super(argBDD);
	}
	
	public ImplicitMultiAgentGameCaseStudy2(BDD argBDD, int dimension, ArrayList<GridCell2D> envInitialCells, ArrayList<GridCell2D> sysInitCells){
		super(argBDD);
		xDimension=dimension;
		yDimension=dimension;
		calculateNumberOfBits();
		
		numOfControllableAgents = sysInitCells.size();
		numOfUncontrollableAgents = envInitialCells.size();
		numOfAgents = envInitialCells.size()+sysInitCells.size();
		
		createVariables(envInitialCells.size(), sysInitCells.size());
		
		System.out.println("variables created");
		
		System.out.println("num of agents"+numOfAgents);

		
		//create implicitGames
		for(int i=0; i<numOfControllableAgents; i++){
			for(int j=0; j<numOfUncontrollableAgents; j++){
//				System.out.println("Env init");
//				envInitialCells.get(j).print();
//				System.out.println("Sys init");
//				sysInitCells.get(i).print();
				ImplicitGameCaseStudy2 g = new ImplicitGameCaseStudy2(bdd, "game_"+i, dimension, 
								envInitialCells.get(j), sysInitCells.get(i), envXvars.get(j), 
								envYvars.get(j), envDirection.get(j), sysXvars.get(i), sysYvars.get(i), uncontrollabelAgentsActionVars.get(j), 
								agentsActionVars.get(i));
						System.out.println("implicit game "+g.id+" was created");
						addGame(g);
//						g.printGame();
//						UtilityMethods.getUserInput();
			}
		}
				
		actionAvailabilitySets();
		
//		System.out.println("testing individual games");
//		
//		for(Game g : getGames()){
//			g.printGame();
//			UtilityMethods.getUserInput();
//			g.printActionsAvlSets();
//			UtilityMethods.getUserInput();
//			
//			System.out.println("correctness of hasNext");
//			while(g.hasNextTransition(Player.ENVIRONMENT)){
//				UtilityMethods.debugBDDMethods(bdd, "env trans", g.getNextEnvTransition());
//			}
//			UtilityMethods.getUserInput();
//			while(g.hasNextTransition(Player.SYSTEM)){
//				UtilityMethods.debugBDDMethods(bdd, "sys trans", g.getNextSysTransition());
//			}
//			UtilityMethods.getUserInput();
//			
//			while(g.hasNextTransition(Player.ENVIRONMENT)){
//				UtilityMethods.debugBDDMethods(bdd, "env trans", g.getNextEnvTransition());
//			}
//			UtilityMethods.getUserInput();
//		}
//		
//		
//		System.out.println("avl sets for composite game");
//		printActionsAvlSets();
//		UtilityMethods.getUserInput();
//		
//		System.out.println("printing games T_env");
//		
//		
//		System.out.println("checking the transitions");
//		System.out.println("T_env");
//		int test = bdd.ref(bdd.getZero());
//		while(hasNextTransition(Player.ENVIRONMENT)){
////			bdd.printSet(getNextEnvTransition());
//			test = bdd.orTo(test, getNextEnvTransition());
//			
//		}
//		bdd.printSet(test);
//		UtilityMethods.getUserInput();
//		
//		System.out.println("whole env trans");
//		bdd.printSet(getEnvironmentTransitionRelation());
//		UtilityMethods.getUserInput();
//		
//		
//		System.out.println("T_sys");
//		while(hasNextTransition(Player.SYSTEM)){
//			bdd.printSet(getNextSysTransition());
////			UtilityMethods.getUserInput();
//			
//		}
//		UtilityMethods.getUserInput();
//		
//		System.out.println("whole T_sys");
//		bdd.printSet(getSystemTransitionRelation());
//		UtilityMethods.getUserInput();
//		
//		System.out.println("T_env");
//		while(hasNextTransition(Player.ENVIRONMENT)){
//			bdd.printSet(getNextEnvTransition());
//			
//		}
//		UtilityMethods.getUserInput();
//		
//		System.out.println("T_sys");
//		while(hasNextTransition(Player.SYSTEM)){
//			bdd.printSet(getNextSysTransition());
//			
//		}
//		UtilityMethods.getUserInput();
	}
	
	
	public void setStaticObstacles(ArrayList<GridCell2D> obs){
		staticObstacles = obs;
	}
	
	public ArrayList<GridCell2D> getStaticObstacles(){
		return staticObstacles;
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
		
//		compareCSGuidedControlAlgs();
		
		test();
	}
	
	public static void test(){
		BDD bdd = new BDD(10000,1000);
		
		int dim =32;
		
		GridCell2D envInit = new GridCell2D(dim-1, dim-1);
		GridCell2D agent1Init = new GridCell2D(0, 0);
		GridCell2D agent2Init = new GridCell2D(0, dim-1);
		
		ArrayList<GridCell2D> initialCells = new ArrayList<GridCell2D>();
		initialCells.add(envInit);
		initialCells.add(agent1Init);
		initialCells.add(agent2Init);
		
		System.out.println("initializing the games");
		ImplicitMultiAgentGameCaseStudy multiAgentGame = new ImplicitMultiAgentGameCaseStudy(bdd, dim, initialCells);
		
//		for(Game game :  multiAgentGame.getGames()){
//			ImplicitGameCaseStudy impGame = (ImplicitGameCaseStudy) game;
//			game.printGame();
//			impGame.toGameGraph().draw(impGame.id+".dot", 1, 0);
//			game.removeUnreachableStates().toGameGraph().draw(game.getID()+".dot",1, 0);
//		}
		
//		UtilityMethods.getUserInput();
		
		ArrayList<Game> games = multiAgentGame.getGames();
		
		System.out.println("creating the objectives");
		ArrayList<Integer> noCollisionWithEnvironmentObjectives = new ArrayList<Integer>();
		
		for(int i=0; i<games.size();i++){
			ImplicitGameCaseStudy igcs = (ImplicitGameCaseStudy) games.get(i);
			noCollisionWithEnvironmentObjectives.add(igcs.noCollisionObjective(igcs.envXvars, igcs.envYvars, igcs.sysXvars, igcs.sysYvars));
		}
		
		ArrayList<Integer> noCollisionBetweenControllableAgents = new ArrayList<Integer>();
		
		for(int i=0;i<games.size()-1;i++){
			ImplicitGameCaseStudy igcs1 = (ImplicitGameCaseStudy) games.get(i);
			for(int j=i+1;j<games.size();j++){
				ImplicitGameCaseStudy igcs2 = (ImplicitGameCaseStudy) games.get(j);
				noCollisionBetweenControllableAgents.add(igcs1.noCollisionObjective(igcs1.sysXvars, igcs1.sysYvars, igcs2.sysXvars, igcs2.sysYvars));
			}
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
		
		System.out.println("Solving the games for individual objectives");
		
		ArrayList<GameSolution> concreteSolutionsForIndObjectives = new ArrayList<GameSolution>();
		for(int i=0; i<games.size();i++){
			System.out.println("Solving for the game "+games.get(i).getID());
//			if(i>0) games.get(i).printGame();
//			UtilityMethods.debugBDDMethods(bdd, "and the objective", noCollisionWithEnvironmentObjectives.get(i));
			GameSolver gs = new GameSolver(games.get(i), noCollisionWithEnvironmentObjectives.get(i), bdd);
//			UtilityMethods.getUserInput();
			GameSolution s = gs.solve();
			concreteSolutionsForIndObjectives.add(s);
		}

		
		System.out.println("Concrete games solutions for individual objectives");
		int n=1;
		for(GameSolution s : concreteSolutionsForIndObjectives){
//			if(n>1) s.strategyOfTheWinner().printGame();
//			s.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("conc"+n+".dot", 1, 0);
			n++;
			s.print();
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
		
		System.out.println("printing the composition of the solutuions");
		
//		composedGame.printGameVars();
//		
//		UtilityMethods.debugBDDMethods(bdd,"init set of composed game", composedGame.getInit());
//		
//		UtilityMethods.getUserInput();
		
		int collectiveObjective = bdd.ref(bdd.getOne());
		
		for(Integer obj : noCollisionBetweenControllableAgents){
			collectiveObjective = bdd.andTo(collectiveObjective, obj);
		}
		
//		UtilityMethods.debugBDDMethods(bdd, "collective obj", collectiveObjective);
//		UtilityMethods.getUserInput();
		

		
		System.out.println("Solving for collective objectives");
		GameSolver gs = new GameSolver(composedGame, collectiveObjective, bdd);
		GameSolution concreteCompSol = gs.solve();
//		concreteCompSol.strategyOfTheWinner().printGame();
//		concreteCompSol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("compStrat.dot", 1, 0);
		concreteCompSol.print();
		
		BDDWrapper.BDD_Usage(bdd);
//		bdd.cleanup();
		
		
		
		
		
		
		
	}
	
private void createVariables(int numOfUncontrollableAgents, int numOfControllableAgents){
		
		//creating action vars
		actionVars=null;
		uncontrollabelAgentsActionVars = new ArrayList<Variable[]>();
		for(int i=0; i<numOfUncontrollableAgents;i++){
			Variable[] agentActVars = Variable.createVariables(bdd, 2, "act_e"+i);
			 uncontrollabelAgentsActionVars.add(agentActVars);
			 
			 if(i==0){
				 envActionVars = agentActVars;
			 }else{
				 envActionVars = Variable.unionVariables(envActionVars, agentActVars);
			 }
			 
			 actionVars = Variable.unionVariables(actionVars, agentActVars);
		}
		 
		 agentsActionVars=new ArrayList<Variable[]>();
		 for(int i=0;i<numOfControllableAgents;i++){
			 Variable[] agentActVars = Variable.createVariables(bdd, 2, "act_s"+i);
			 agentsActionVars.add(agentActVars);
			 
			 if(i==0){
				 sysActionVars = agentActVars;
			 }else{
				 sysActionVars = Variable.unionVariables(sysActionVars, agentActVars);
			 }
			 
			 actionVars = Variable.unionVariables(actionVars, agentActVars);
		 }
		 
		 envActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(envActionVars));
		 sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
		 actions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(actionVars));
		
		//creating the variables 
//		 Variable[] ox = new Variable[numOfXBits];
//		 Variable[] oy = new Variable[numOfYBits];
//		 Variable[] oxPrime = new Variable[numOfXBits];
//		 Variable[] oyPrime = new Variable[numOfYBits];
		 
		envXvars=new ArrayList<Variable[]>();
		envXvarsPrime=new ArrayList<Variable[]>();
		
		envYvars=new ArrayList<Variable[]>();
		envYvarsPrime=new ArrayList<Variable[]>();
		
		envDirection = new ArrayList<Variable>();
		envDirectionPrime = new ArrayList<Variable>();
		
		sysXvars = new ArrayList<Variable[]>();
		sysXvarsPrime = new ArrayList<Variable[]>();
		
		sysYvars = new ArrayList<Variable[]>();
		sysYvarsPrime = new ArrayList<Variable[]>();
		
		for(int i=0;i<numOfUncontrollableAgents;i++){
			envXvars.add(new Variable[numOfXBits]);
			envXvarsPrime.add(new Variable[numOfXBits]);
			envYvars.add(new Variable[numOfYBits]);
			envYvarsPrime.add(new Variable[numOfYBits]);
		 }
		
		for(int i=0;i<numOfControllableAgents;i++){
			sysXvars.add(new Variable[numOfXBits]);
			sysXvarsPrime.add(new Variable[numOfXBits]);
			sysYvars.add(new Variable[numOfYBits]);
			sysYvarsPrime.add(new Variable[numOfYBits]);
		 }
		
		
			
		 for(int i=0; i<numOfXBits;i++){
			 
			 for(int j=0; j<numOfUncontrollableAgents; j++){
				 
				 if(i==0){
					 Variable envDir_j = new Variable(bdd, "dir"+j);
					 envDirection.add(envDir_j);
					 envDirectionPrime.add(Variable.createPrimeVariable(bdd, envDir_j));
				 }
				 
				 Variable[] agent_j = envXvars.get(j);
				 agent_j[i]=new Variable(bdd, "OX"+j+""+i);
				 Variable[] agent_j_xPrime = envXvarsPrime.get(j);
				 agent_j_xPrime[i]=Variable.createPrimeVariable(bdd, agent_j[i]);
			 }
			
			 for(int j=0; j<numOfControllableAgents; j++){
				 Variable[] agent_j = sysXvars.get(j);
				 agent_j[i]=new Variable(bdd, "RX"+j+""+i);
				 Variable[] agent_j_xPrime = sysXvarsPrime.get(j);
				 agent_j_xPrime[i]=Variable.createPrimeVariable(bdd, agent_j[i]);
			 }
			 
		 }
		 
		 for(int i=0;i<numOfYBits;i++){

			 
			 for(int j=0; j<numOfUncontrollableAgents; j++){
				 Variable[] agent_j = envYvars.get(j);
				 agent_j[i]=new Variable(bdd, "OY"+j+""+i);
				 Variable[] agent_j_yPrime = envYvarsPrime.get(j);
				 agent_j_yPrime[i]=Variable.createPrimeVariable(bdd, agent_j[i]);
			 }
			 
			 for(int j=0; j<numOfControllableAgents; j++){
				 Variable[] agent_j = sysYvars.get(j);
				 agent_j[i]=new Variable(bdd, "RY"+j+""+i);
				 Variable[] agent_j_yPrime = sysYvarsPrime.get(j);
				 agent_j_yPrime[i]=Variable.createPrimeVariable(bdd, agent_j[i]);
			 }
		 }
		 
		 
		 for(int i=0;i<numOfUncontrollableAgents;i++){
			 Variable[] agentVars = Variable.unionVariables(envXvars.get(i), envYvars.get(i));
			 Variable[] agentVarsPrime = Variable.unionVariables(envXvarsPrime.get(i), envYvarsPrime.get(i));
			 if(i==0){
				 environmentVars = agentVars;
				 environmentVarsPrime = agentVarsPrime; 
			 }else{
				 environmentVars = Variable.unionVariables(environmentVars, agentVars);
				environmentVarsPrime=Variable.unionVariables(environmentVarsPrime, agentVarsPrime);
			 }
			 
			 Variable[] dirVars = new Variable[]{envDirection.get(i)};
			 Variable[] dirVarsPrime = new Variable[]{envDirectionPrime.get(i)};
			 environmentVars=Variable.unionVariables(environmentVars, dirVars);
			 environmentVarsPrime = Variable.unionVariables(environmentVarsPrime, dirVarsPrime);
		 }
		 
		 
		 
		 for(int i=0;i<numOfControllableAgents;i++){
			 Variable[] agentVars = Variable.unionVariables(sysXvars.get(i), sysYvars.get(i));
			 Variable[] agentVarsPrime = Variable.unionVariables(sysXvarsPrime.get(i), sysYvarsPrime.get(i));
			 if(i==0){
				 systemVars = agentVars;
				 systemVarsPrime = agentVarsPrime; 
			 }else{
				 systemVars = Variable.unionVariables(systemVars, agentVars);
				systemVarsPrime=Variable.unionVariables(systemVarsPrime, agentVarsPrime);
			 }
			 
		 }
		
		 variables= Variable.unionVariables(environmentVars, systemVars);
		 primedVariables=Variable.unionVariables(environmentVarsPrime, systemVarsPrime);
		 
		 setPermutations(variables, primedVariables);
		 
	}
	
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
	
	public ArrayList<Variable[]> getEnvXvars(){
		return envXvars;
	}
	
	public ArrayList<Variable[]> getEnvYvars(){
		return envYvars;
	}
	
	public ArrayList<Variable[]> getSysXvars(){
		return sysXvars;
	}
	
	public ArrayList<Variable[]> getSysYvars(){
		return sysYvars;
	}
	
	public int getNumberOfControllableAgents(){
		return numOfControllableAgents;
	}
	
	public int getNumberOfUncontrollableAgents(){
		return numOfUncontrollableAgents;
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
	
//	/**
//	 * Ensures that two agents do not switch cells in two consecutive steps to avoid collision
//	 * @return
//	 */
//	public int noSwitchingCellsObjective(Variable[] x1var, Variable[] y1var, Variable[] x2var, Variable[] y2var){
//		int[] x1 = Variable.getBDDVars(x1var);
//		int[] x2 = Variable.getBDDVars(x2var);
//		int[] y1 = Variable.getBDDVars(y1var);
//		int[] y2 = Variable.getBDDVars(y2var);
//		
//		int[] x1p = Variable.getBDDVars(Variable.getPrimedCopy(x1var));
//		int[] x2p = Variable.getBDDVars(Variable.getPrimedCopy(x1var));
//		int[] y1p = Variable.getBDDVars(Variable.getPrimedCopy(x1var));
//		int[] y2p = Variable.getBDDVars(Variable.getPrimedCopy(x1var));
//		
//		int same1Next2 = BDDWrapper.same(bdd, x1, y1, x2p, y2p);
//		int same2Next1 = BDDWrapper.same(bdd, x1p, y1p, x2, y2);
//		
//	}
	
	public int noCollisionWithStaticObstacles(Variable[] xvars, Variable[] yvars, ArrayList<GridCell2D> staticObstacles){
		int noCollision = bdd.ref(bdd.getOne());
		for(GridCell2D obs : staticObstacles){
			int collisionWithObs = assignCell(xvars, yvars, obs);
			int noCollisionWithObs = bdd.ref(bdd.not(collisionWithObs));
			noCollision = bdd.andTo(noCollision, noCollisionWithObs);
			bdd.deref(collisionWithObs);
			bdd.deref(noCollisionWithObs);
		}
		return noCollision;
	}
	
	
	public int assignCell(Variable[] x, Variable[] y , GridCell2D cell){
		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
		int result=bdd.ref(bdd.and(xAssign, yAssing));
		bdd.deref(xAssign);
		bdd.deref(yAssing);
		return result;
	}
}


