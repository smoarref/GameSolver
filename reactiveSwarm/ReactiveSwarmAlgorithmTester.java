package reactiveSwarm;

import game.BDDWrapper;
import game.GameSolution;
import game.GameStructure;
import game.Player;
import game.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.rmi.CORBA.Util;

import solver.GR1GameStructure;
import solver.GR1Objective;
import solver.GR1Solver;
import solver.GR1WinningStates;
import utils.UtilityMethods;
import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;
import jdd.bdd.BDD;

public class ReactiveSwarmAlgorithmTester {
	
	 

	public static void main(String[] args) {
		
		BDD bdd = new BDD(10000,1000); 
//		int a = bdd.createVar(); 
//		int b = bdd.createVar(); 
//		int c= bdd.createVar();
//		int f = bdd.ref(bdd.and(a, b));
//		f = bdd.orTo(f, c);
//		bdd.printSet(f);
//		int m = bdd.oneSat(f);
//		bdd.printSet(m);
////		firstExample();
//		Variable a = new Variable(bdd, "a");
//		Variable b = new Variable(bdd, "b");
//		Variable c = new Variable(bdd, "c");
//		Variable[] vars = new Variable[]{a,b};
//		
//		int f = BDDWrapper.or(bdd, a.getBDDVar(), b.getBDDVar());
//		UtilityMethods.debugBDDMethods(bdd, "f is ", f);
//		int[] minterms = BDDWrapper.enumerate(bdd, f, vars);
//		for(int m : minterms){
//			UtilityMethods.debugBDDMethods(bdd," minterm is ", m);
//		}
		
		
//		simpleExample();
		
//		cornellExample();
		
//		generateCorridorExampleForSlugs();
		
//		generateCorridorExample14RegionsForSlugs();
		
//		generateCorridorExample14MoreEdgesRegionsForSlugs();
		
		generateCorridorExample14MoreEdgesRegionsForLatex();
	}
	
	//building a simple initial example
	public static void cornellExample(){
		/*****INPUT****************/
		//the inputs to the problem: 
		//a region graph G
		//a GR1 specification Phi
		//a set of initial states phi_init		
		/****OUTPUT****************/
		//a strategy that is winning with respect to intermediate states 
		
		//Algorithm start
		
		//define BDD
		BDD bdd = new BDD(10000,1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//8 regions 
		//A, Corridor, Lab1, Lab2, Office1, Office2, Office3, StorageRoom
		Node[] regionNodes = new Node[]{new Node("A"), new Node("Corrdior"), new Node("Lab1"), new Node("Lab2"), new Node("office1"), new Node("office2"), new Node("office3"), new Node("StorageRoom")};
		
		//A <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
		//Lab1 <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		
		//Lab2 <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
		
		//Office1 <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[4]));
		
		//Office2 <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[5]));
		
		//Office3 <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[6]));
		
		//Storage room <-> corridor
		regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[7]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		variables.add(A);
		regionToVarMap.put(regionNodes[0],A);
		varToRegionMap.put(A, regionNodes[0]);
		Variable corridor = Variable.createVariableAndPrimedVariable(bdd, "Corridor");
		variables.add(corridor);
		regionToVarMap.put(regionNodes[1],corridor);
		varToRegionMap.put(corridor, regionNodes[1]);
		Variable lab1 = Variable.createVariableAndPrimedVariable(bdd, "Lab1");
		variables.add(lab1);
		regionToVarMap.put(regionNodes[2],lab1);
		varToRegionMap.put(lab1, regionNodes[2]);
		Variable lab2 = Variable.createVariableAndPrimedVariable(bdd, "Lab2");
		variables.add(lab2);
		regionToVarMap.put(regionNodes[3],lab2);
		varToRegionMap.put(lab2, regionNodes[3]);
		Variable office1 = Variable.createVariableAndPrimedVariable(bdd, "Office1");
		variables.add(office1);
		regionToVarMap.put(regionNodes[4],office1);
		varToRegionMap.put(office1, regionNodes[4]);
		Variable office2 = Variable.createVariableAndPrimedVariable(bdd, "Office2");
		variables.add(office2);
		regionToVarMap.put(regionNodes[5],office2);
		varToRegionMap.put(office2, regionNodes[5]);
		Variable office3 = Variable.createVariableAndPrimedVariable(bdd, "Office3");
		variables.add(office3);
		regionToVarMap.put(regionNodes[6],office3);
		varToRegionMap.put(office3, regionNodes[6]);
		Variable storageRoom = Variable.createVariableAndPrimedVariable(bdd, "StorageRoom");
		variables.add(storageRoom);
		regionToVarMap.put(regionNodes[7],storageRoom);
		varToRegionMap.put(storageRoom, regionNodes[7]);
		 
		Variable[] regionVars = new Variable[]{A,corridor,lab1, lab2, office1, office2, office3, storageRoom};
		
		
		//define input variable
		Variable inp = Variable.createVariableAndPrimedVariable(bdd, "inp");
		variables.add(inp);
		
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		//define GR1 objective
		//guarantees
		//always eventually the whole swarm in office1, office2, and office3
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(office1);
		l1OccupiedRegions.add(office2);
		l1OccupiedRegions.add(office3);
		int l1 = createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		
		//always eventually if the target is in Lba1, then the swarm isolates it
		//GF inp -> Lab1 and Lab2 and Corridor
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(lab1);
		l2OccupiedRegions.add(lab2);
		l2OccupiedRegions.add(corridor);
		int l2regions = createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int l2 = BDDWrapper.implies(bdd, inp.getBDDVar(), l2regions);
		BDDWrapper.free(bdd, l2regions);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(l1);
		guarantees.add(l2);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		//define the initial states 
		//initially the whole swarm is in D
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(A);
		int init = createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp.getBDDVar()));
				
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
				
						
		System.out.println("ready to synthesize!");
		UtilityMethods.getUserInput();
				
		long t0 = UtilityMethods.timeStamp();
		
		
		//compute the set of winning states and winning layers using the atomic game structure 
		GameSolution sol = swarmReactiveControllerSynthesizer(bdd, regionGraph, regionVars, new Variable[]{inp}, 
				vars, regionToVarMap, varToRegionMap, init, objective);
	
		
		UtilityMethods.duration(t0," strategy computed in ");
		
		
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
	}
	
	public static void simpleExample(){
		/*****INPUT****************/
		//the inputs to the problem: 
		//a region graph G
		//a GR1 specification Phi
		//a set of initial states phi_init		
		/****OUTPUT****************/
		//a strategy that is winning with respect to intermediate states 
		
		//Algorithm start
		
		//define BDD
		BDD bdd = new BDD(10000,1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
				
		//4 regions 
		//A, B, C
		Node[] regionNodes = new Node[]{new Node("A"), new Node("C"), new Node("D")};
		

		
		//A <-> D 
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[0]));
		

		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
				
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		variables.add(A);
		regionToVarMap.put(regionNodes[0],A);
		varToRegionMap.put(A, regionNodes[0]);
		
		
		Variable C = Variable.createVariableAndPrimedVariable(bdd, "C");
		variables.add(C);
		regionToVarMap.put(regionNodes[1],C);
		varToRegionMap.put(C, regionNodes[1]);
		
		Variable D = Variable.createVariableAndPrimedVariable(bdd, "D");
		variables.add(D);
		regionToVarMap.put(regionNodes[2],D);
		varToRegionMap.put(D, regionNodes[2]);
		
		//define input variable
		Variable inp_B = Variable.createVariableAndPrimedVariable(bdd, "inp_B");
		variables.add(inp_B);
		
		Variable[] regionVars = new Variable[]{A,C,D};
		
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		//define GR1 objective
		
		//guarantees
		//always eventually the whole swarm in D 
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(D);
		int l1 = createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		
		//always eventually if the target is in region B, then the swarm isolates it
		//GF inp_B -> A  and C
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		l2OccupiedRegions.add(C);
		int l2regions = createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int l2 = BDDWrapper.implies(bdd, inp_B.getBDDVar(), l2regions);
		BDDWrapper.free(bdd, l2regions);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(l1);
		guarantees.add(l2);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//define the initial states 
		//initially the whole swarm is in D
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(D);
		int init = createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp_B.getBDDVar()));
		
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
		
				
		System.out.println("ready to synthesize!");
		UtilityMethods.getUserInput();
		
		long t0 = UtilityMethods.timeStamp();
				
		//compute the set of winning states and winning layers using the atomic game structure 
		GameSolution sol = swarmReactiveControllerSynthesizer(bdd, regionGraph, regionVars, new Variable[]{inp_B}, 
				vars, regionToVarMap, varToRegionMap, init, objective);
	
		
		UtilityMethods.duration(t0," strategy computed in ");
		
		
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
		
		
	}
	
	//according to the paper
	public static void firstExample(){
		/*****INPUT****************/
		//the inputs to the problem: 
		//a region graph G
		//a GR1 specification Phi
		//a set of initial states phi_init		
		/****OUTPUT****************/
		//a strategy that is winning with respect to intermediate states 
		
		//Algorithm start
		
		//define BDD
		BDD bdd = new BDD(10000,1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
				
		//4 regions 
		//A, B, C, D
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D")};
		
		//A <-> B
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
		//A <-> D 
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));
		
		//B <-> C 
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
				
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		variables.add(A);
		regionToVarMap.put(regionNodes[0],A);
		varToRegionMap.put(A, regionNodes[0]);
		
		Variable B = Variable.createVariableAndPrimedVariable(bdd, "B");
		variables.add(B);
		regionToVarMap.put(regionNodes[1],B);
		varToRegionMap.put(B, regionNodes[1]);
		
		Variable C = Variable.createVariableAndPrimedVariable(bdd, "C");
		variables.add(C);
		regionToVarMap.put(regionNodes[2],C);
		varToRegionMap.put(C, regionNodes[2]);
		
		Variable D = Variable.createVariableAndPrimedVariable(bdd, "D");
		variables.add(D);
		regionToVarMap.put(regionNodes[3],D);
		varToRegionMap.put(D, regionNodes[3]);
		
		//define input variable
		Variable inp_B = Variable.createVariableAndPrimedVariable(bdd, "inp_B");
		variables.add(inp_B);
		
		Variable[] regionVars = new Variable[]{A,B,C,D};
		
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		//define GR1 objective
		
		//guarantees
		//always eventually the whole swarm in D 
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(D);
		int l1 = createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		
		//always eventually if the target is in region B, then the swarm isolates it
		//GF inp_B -> A and B and C
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		l2OccupiedRegions.add(B);
		l2OccupiedRegions.add(C);
		int l2regions = createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int l2 = BDDWrapper.implies(bdd, inp_B.getBDDVar(), l2regions);
		BDDWrapper.free(bdd, l2regions);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(l1);
		guarantees.add(l2);
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//define the initial states 
		//initially the whole swarm is in D
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(D);
		int init = createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		
		
				
		
				
		//compute the set of winning states and winning layers using the atomic game structure 
		
		
		
				
	}
	
	private static GameSolution swarmReactiveControllerSynthesizer(BDD bdd, 
			DirectedGraph<String, Node, Edge<Node>> regionGraph, 
			Variable[] regionVars, 
			Variable[] inputVars, 
			Variable[] vars, 
			HashMap<Node, Variable> regionToVarMap, 
			HashMap<Variable, Node> varToRegionMap, 
			int init, GR1Objective objective){
		
		//form the atomic game structure and compute the winning states and winning layers 
		
		//turn the region graph into a GR1 specification
		int t = directedGraphToAtomicTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		
		//create the atomic game structure
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = t;
		GR1GameStructure atomicGS = new GR1GameStructure(bdd, inputVars, regionVars, T_env, T_sys);
		
//		System.out.println("atomicGS defined!");
//		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
//		UtilityMethods.getUserInput();
		
		//compute the set of winning states for the atomic game struture 
		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), atomicGS, init, objective);
		
		
		System.out.println("winning states computed");
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "the winning states", gr1w.getWinningStates());
//		gr1w.printMemory(bdd);
		
		//check if the game is realizable 
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		//compute the strategy using the original transition relation where multiple action can be taken at the same time
		if(isRealizable){
			
			System.out.println("The game is realizable");
			
			int sysTransitionRelation = directedGraphToTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "non-atomic transition relation", sysTransitionRelation);
			
			int envTransitionRelation = atomicGS.getEnvironmentTransitionRelation();
			
			Player winner;
//			GameStructure strategy=null;
			int winningStates = gr1w.getWinningStates();
//			int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, atomicGS.getVtoVprime());
			
			ArrayList<Integer> guarantees = objective.getGuarantees();
			ArrayList<Integer> assumptions = objective.getAssumptions();
			
			int numOfGuarantees = guarantees.size();
			int numOfAssumptions = assumptions.size();
			
//			System.out.println("num of guarantees "+numOfGuarantees);
//			System.out.println("num of assumptions "+numOfAssumptions);
//			UtilityMethods.getUserInput();
			
			ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
			ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
			
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
//			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, atomicGS.getID()+"_gr1Counter");
			int counterCube = BDDWrapper.createCube(bdd, counter);
			
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(atomicGS.variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy init is ", strategyInit);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			//the set of states to process
			int[] initialStates = BDDWrapper.enumerate(bdd, strategyInit, strategyVars);
			ArrayList<Integer> Q = new ArrayList<Integer>();
			for(int q : initialStates){
				Q.add(q);
//				UtilityMethods.debugBDDMethods(bdd, "following state got added to Q", q);
			}
			System.out.println("initial states added to Q");
			
			//the set of states already processed
			int P = bdd.ref(bdd.getZero());
			
			while(Q.size()!=0){
				//pick a state to process
				int state = Q.remove(0);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "Processing state ", state);
				
				//check if the state is already processed
				int stateInP = BDDWrapper.and(bdd, state, P);
				if(state == stateInP){
//					System.out.println("state is already processed! ");
					continue;
				}
				
				P = BDDWrapper.orTo(bdd, P, state);
				
				//Compute the counter value to know what guarantee we are trying to satisfy
				int counterValue = BDDWrapper.exists(bdd, state, atomicGS.getVariablesCube());
				ArrayList<String> minterms  = BDDWrapper.minterms(bdd, counterValue, counter);
				if(minterms.size()!=1){
					System.err.println("there are more than one counter assigned to the state, possible bug!");
				}
				int index = Integer.parseInt(minterms.get(0),2);
				
//				System.out.println("counter value is "+index);
				int stateWithoutCounter = BDDWrapper.exists(bdd, state, counterCube);
				BDDWrapper.free(bdd, state);
				state = stateWithoutCounter;
//				UtilityMethods.getUserInput();
//				BDDWrapper.free(bdd, counterValue);
				
				
				
				//does the state satisfies the predicate guarantees[index]?
				int satG_index = BDDWrapper.and(bdd, state, guarantees.get(index));
				int nextIndex; 
				int fromLayerIndex;
				
				//TODO: generalize, the state might satisfy the next guarantee too, 
				//then we should increase the counter until the next guartantee that is not satisfied
				//if a state satisfies all the guarantees, just apply an action and take it from there 
				//right now we assume that liveness goals do not overlap --> extend to remove this assumption
				if(satG_index == state){
					//the state satisfies the guarantee[index] 
					//update the counter
					nextIndex = (index+1)%numOfGuarantees;
//					fromLayerIndex = locateState(bdd, state, mY.get(nextIndex));
					
					fromLayerIndex = mY.get(nextIndex).size();
					
//					System.out.println("state satisfies guarantee "+index);
//					System.out.println("next index is "+nextIndex);
//					System.out.println("state is in layer "+fromLayerIndex);
//					UtilityMethods.getUserInput();
				}else{
					nextIndex = index;
					fromLayerIndex = locateState(bdd, state, mY.get(index));
//					System.out.println("next index is "+nextIndex);
//					System.out.println("state is in layer "+fromLayerIndex);
//					UtilityMethods.getUserInput();
				}
				
				//compute possible next moves based on environment 
//				int nextEnv = BDDWrapper.EpostImage(bdd, state, atomicGS.getVariablesAndActionsCube(), envTransitionRelation, atomicGS.getVprimetoV());
				
				int nextEnv = BDDWrapper.and(bdd, state, envTransitionRelation);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "next env", nextEnv);
				
				
				Variable[] varsAndInputPrimeVars = Variable.unionVariables(atomicGS.variables, atomicGS.inputPrimeVariables);
				int[] nextEnvMoves = BDDWrapper.enumerate(bdd, nextEnv, varsAndInputPrimeVars);
				BDDWrapper.free(bdd, nextEnv);
				
				//for each input
				for(int inp : nextEnvMoves){
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "considering input ", inp);
					
					boolean winningSysMoveFound = false;
					
					//choose a next state 
					//what are the possible next winning moves for the system 
					int nextSys = BDDWrapper.EpostImage(bdd, inp, atomicGS.getVariablesCube(), sysTransitionRelation, atomicGS.getVprimetoV());
					nextSys = BDDWrapper.andTo(bdd, nextSys, winningStates);
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "possible winning next states", nextSys);
					
					//choose a next state that goes deeper into mY[nextIndex],
					//i.e., min_r s.t. q' \in mY[nextIndex][r] and q->q'
					ArrayList<Integer> nextGuaranteeLayers = mY.get(nextIndex);
					//TODO: handle the case where fromLayerIndex = 0, meaning that the state satisfies multiple guarantees
					//at the same time
					for(int i=0; i<fromLayerIndex;i++){
						//is there any winning state in the layer i? 
						int intersect = BDDWrapper.and(bdd, nextSys, nextGuaranteeLayers.get(i));
						if(intersect != 0){
							
//							UtilityMethods.debugBDDMethods(bdd,"intersection is non-empty with layer "+i, intersect);
//							UtilityMethods.getUserInput();
							
							//remove the intersect from the nextSys
							int tmp = BDDWrapper.diff(bdd, nextSys, intersect);
							BDDWrapper.free(bdd, nextSys);
							nextSys = tmp;
							
							//enumerate the states in the intersect 
							int[] nextSysMoves = BDDWrapper.enumerate(bdd, intersect, atomicGS.variables);
							
							//choose a state from intersect 
							for(int j =0 ; j<nextSysMoves.length; j++){
								//check if the transition is winning w.r.t. intermediate states
								int checkIntermediate = areIntermediateStatesWinning(bdd, regionGraph, state, 
										nextSysMoves[j], regionToVarMap, 
										varToRegionMap, regionVars, assumptions, fromLayerIndex, mY.get(nextIndex), 
										mX.get(nextIndex));
								
								
								if(checkIntermediate !=-1){
									
//									System.out.println("all intermediate states are fine!");
									
									//if yes, add the transition to the strategy
									int from = BDDWrapper.and(bdd, inp, counterValue);
									int nextCounter = BDDWrapper.assign(bdd, nextIndex, counterPrime);
									int next = BDDWrapper.replace(bdd, nextSysMoves[j], atomicGS.getVtoVprime());
									int to = BDDWrapper.and(bdd, nextCounter, next);
									BDDWrapper.free(bdd,nextCounter);
									BDDWrapper.free(bdd, next);
									int trans = BDDWrapper.and(bdd, from, to);
									BDDWrapper.free(bdd, from);
									BDDWrapper.free(bdd, to);
									
//									UtilityMethods.debugBDDMethodsAndWait(bdd, "adding transition to the strategy", trans);
									
									strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, trans);
									BDDWrapper.free(bdd, trans);
									
									//add the intermediate states and next state to Q
									nextCounter = BDDWrapper.assign(bdd, nextIndex, counter);
									int nextState = BDDWrapper.and(bdd, nextSysMoves[j], nextCounter);
									Q.add(nextState);
//									UtilityMethods.debugBDDMethodsAndWait(bdd, "destination added to the Q", nextState);
									
									if(checkIntermediate!=0){
										int[] intermediateStates = BDDWrapper.enumerate(bdd, checkIntermediate, atomicGS.variables);
										BDDWrapper.free(bdd, checkIntermediate);
									
										for(int is : intermediateStates){
											int nextIS = BDDWrapper.and(bdd, is, nextCounter);
											Q.add(nextIS);
//											UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate state added to the Q", nextIS);
											BDDWrapper.free(bdd, is);
										}
									}
									BDDWrapper.free(bdd, nextCounter);
									
									//free intersect
									BDDWrapper.free(bdd, intersect);
									for(int k=j;k<nextSysMoves.length;k++){
										BDDWrapper.free(bdd, nextSysMoves[k]);
									}
									
									//break and go to the next input
									winningSysMoveFound = true;
									break;
								}else{
									BDDWrapper.free(bdd, nextSysMoves[j]);
								}
							}
						}
						if(winningSysMoveFound){
							BDDWrapper.free(bdd, nextSys);
							break; 
						}
					}
					
					//if we cannot push forward, can we violate an assumption?
					if(!winningSysMoveFound){
					
						if(assumptions != null && assumptions.size() !=0){
							for(int i=0; i<assumptions.size(); i++){
								int notAssumption_i = BDDWrapper.not(bdd, assumptions.get(i));
								int intersect = BDDWrapper.and(bdd, state, notAssumption_i);
								if(intersect == state){
									int layer = mX.get(nextIndex).get(i).get(fromLayerIndex);
									int w = BDDWrapper.and(bdd, nextSys, layer);
									if(w!=0){
										//TODO: remove the intersect from the nextSys
										int tmp = BDDWrapper.diff(bdd, nextSys, intersect);
										BDDWrapper.free(bdd, nextSys);
										nextSys = tmp;
										
										//enumerate the states in the intersect 
										int[] nextSysMoves = BDDWrapper.enumerate(bdd, intersect, atomicGS.variables);
										
										//choose a state from intersect 
										for(int j =0 ; j<nextSysMoves.length; j++){
											//check if the transition is winning w.r.t. intermediate states
											int checkIntermediate = areIntermediateStatesWinning(bdd, regionGraph, state, 
													nextSysMoves[j], regionToVarMap, 
													varToRegionMap, regionVars, assumptions, fromLayerIndex, mY.get(nextIndex), 
													mX.get(nextIndex));
											
											if(checkIntermediate !=-1){
												//if yes, add the transition to the strategy
												int from = BDDWrapper.and(bdd, inp, counterValue);
												int nextCounter = BDDWrapper.assign(bdd, nextIndex, counterPrime);
												int next = BDDWrapper.replace(bdd, nextSysMoves[j], atomicGS.getVtoVprime());
												int to = BDDWrapper.and(bdd, nextCounter, next);
												BDDWrapper.free(bdd,nextCounter);
												BDDWrapper.free(bdd, next);
												int trans = BDDWrapper.and(bdd, from, to);
												BDDWrapper.free(bdd, from);
												BDDWrapper.free(bdd, to);
												strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, trans);
												BDDWrapper.free(bdd, trans);
												
												//add the intermediate states and next state to Q
												nextCounter = BDDWrapper.assign(bdd, nextIndex, counter);
												int nextState = BDDWrapper.and(bdd, nextSysMoves[j], nextCounter);
												Q.add(nextState);
												
												if(checkIntermediate != 0){
													int[] intermediateStates = BDDWrapper.enumerate(bdd, checkIntermediate, atomicGS.variables);
													BDDWrapper.free(bdd, checkIntermediate);
													
													for(int is : intermediateStates){
														int nextIS = BDDWrapper.and(bdd, is, nextCounter);
														Q.add(nextIS);
//														UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate state added to the Q", nextIS);
														BDDWrapper.free(bdd, is);
													}
												}
												BDDWrapper.free(bdd, nextCounter);
												
												//free intersect
												BDDWrapper.free(bdd, intersect);
												for(int k=j;k<nextSysMoves.length;k++){
													BDDWrapper.free(bdd, nextSysMoves[k]);
												}
												
												//break and go to the next input
												winningSysMoveFound = true;
												break;
											}else{
												BDDWrapper.free(bdd, nextSysMoves[j]);
											}
										}
									}
								}
								BDDWrapper.free(bdd, notAssumption_i);
								BDDWrapper.free(bdd, intersect);
								if(winningSysMoveFound){
									break;
								}
							}
						}
					}
				}
				
				//state is processed and added to P already, release the memory
				BDDWrapper.free(bdd, state);
				BDDWrapper.free(bdd, counterValue);
				
				
			}
			
//			int sameCounter = BDDWrapper.same(bdd,counter);
//			envTransitionRelation = BDDWrapper.andTo(bdd, envTransitionRelation, sameCounter); 
//			BDDWrapper.free(bdd, sameCounter);
			
			
			Variable[] strategyOutputVars = Variable.unionVariables(regionVars,counter);
			GR1GameStructure strategy = new GR1GameStructure(bdd, atomicGS.inputVariables, strategyOutputVars, atomicGS.getEnvironmentTransitionRelation(), strategy_T_sys);
			strategy.setInit(strategyInit);
			
//			GameStructure strat = new GameStructure(bdd, strategyVars,Variable.getPrimedCopy(strategyVars),strategyInit,envTransitionRelation, strategy_T_sys, atomicGS.actionVars, atomicGS.actionVars);
			
			GameSolution result = new GameSolution(atomicGS, Player.SYSTEM, strategy, gr1w.getWinningStates());
			return result; 
		}
		
		
		
		System.out.println("the specification is unrealizable!");
		return new GameSolution(atomicGS, Player.ENVIRONMENT, null, gr1w.getWinningStates());
		
	}
	
//	private static GameSolution swarmReactiveControllerSynthesizer(BDD bdd, 
//			DirectedGraph<String, Node, Edge<Node>> regionGraph, 
//			Variable[] regionVars, 
//			Variable[] inputVars, 
//			Variable[] vars, 
//			HashMap<Node, Variable> regionToVarMap, 
//			HashMap<Variable, Node> varToRegionMap, 
//			int init, GR1Objective objective){
//		
//		//form the atomic game structure and compute the winning states and winning layers 
//		
//		//turn the region graph into a GR1 specification
//		int t = directedGraphToAtomicTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
//		
////		int sameEnvVars = BDDWrapper.same(bdd, inputVars);
//					
//		//create the atomic game structure
////		//TODO: generalize: T_env can be more general
////		int T_env = BDDWrapper.same(bdd, regionVars);
////		int T_sys = BDDWrapper.and(bdd, t, sameEnvVars);
////		BDDWrapper.free(bdd, t);
////		BDDWrapper.free(bdd, sameEnvVars);
////				
////		//create the atomic game structure
////		GameStructure atomicGS = new GameStructure(bdd, vars, Variable.getPrimedCopy(vars), init, T_env, T_sys, null, null);
//		
//		//create the atomic game structure
//		int T_env = bdd.ref(bdd.getOne());
//		int T_sys = t;
//		GR1GameStructure atomicGS = new GR1GameStructure(bdd, inputVars, regionVars, T_env, T_sys);
//		
//		System.out.println("atomicGS defined!");
//		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
//		UtilityMethods.getUserInput();
//		
//		//compute the set of winning states for the atomic game struture 
//		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), atomicGS, init, objective);
//		
//		
//		System.out.println("winning states computed");
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "the winning states", gr1w.getWinningStates());
//		gr1w.printMemory(bdd);
//		
//		//check if the game is realizable 
//		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
//		
//		//compute the strategy using the original transition relation where multiple action can be taken at the same time
//		if(isRealizable){
//			
//			System.out.println("The game is realizable");
//			
//			int sysTransitionRelation = directedGraphToTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
//			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "non-atomic transition relation", sysTransitionRelation);
//			
//			int envTransitionRelation = atomicGS.getEnvironmentTransitionRelation();
//			
//			Player winner;
////			GameStructure strategy=null;
//			int winningStates = gr1w.getWinningStates();
////			int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, atomicGS.getVtoVprime());
//			
//			ArrayList<Integer> guarantees = objective.getGuarantees();
//			ArrayList<Integer> assumptions = objective.getAssumptions();
//			
//			int numOfGuarantees = guarantees.size();
//			int numOfAssumptions = assumptions.size();
//			
////			System.out.println("num of guarantees "+numOfGuarantees);
////			System.out.println("num of assumptions "+numOfAssumptions);
////			UtilityMethods.getUserInput();
//			
//			ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
//			ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
//			
//			System.out.println("the objective is realizable!");
//
//			winner = Player.SYSTEM;
//			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
//			
//			System.out.println("number of counter variables "+numOfVars);
//			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, atomicGS.getID()+"_gr1Counter");
//			int counterCube = BDDWrapper.createCube(bdd, counter);
//			
//			
////			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
//			
//			
//			Variable[] strategyVars = Variable.unionVariables(atomicGS.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
//			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
//			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy init is ", strategyInit);
//			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
//			
//			int strategy_T_sys=bdd.ref(bdd.getZero());
//			
//			//the set of states to process
//			int[] initialStates = BDDWrapper.enumerate(bdd, strategyInit, strategyVars);
//			ArrayList<Integer> Q = new ArrayList<Integer>();
//			for(int q : initialStates){
//				Q.add(q);
//				UtilityMethods.debugBDDMethods(bdd, "following state got added to Q", q);
//			}
//			System.out.println("initial states added to Q");
//			
//			//the set of states already processed
//			int P = bdd.ref(bdd.getZero());
//			
//			while(Q.size()!=0){
//				//pick a state to process
//				int state = Q.remove(0);
//				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "Processing state ", state);
//				
//				//check if the state is already processed
//				int stateInP = BDDWrapper.and(bdd, state, P);
//				if(state == stateInP){
//					System.out.println("state is already processed! ");
//					continue;
//				}
//				
//				P = BDDWrapper.orTo(bdd, P, state);
//				
//				//Compute the counter value to know what guarantee we are trying to satisfy
//				int counterValue = BDDWrapper.exists(bdd, state, atomicGS.getVariablesCube());
//				ArrayList<String> minterms  = BDDWrapper.minterms(bdd, counterValue, counter);
//				if(minterms.size()!=1){
//					System.err.println("there are more than one counter assigned to the state, possible bug!");
//				}
//				int index = Integer.parseInt(minterms.get(0),2);
//				
//				System.out.println("counter value is "+index);
//				int stateWithoutCounter = BDDWrapper.exists(bdd, state, counterCube);
//				BDDWrapper.free(bdd, state);
//				state = stateWithoutCounter;
//				UtilityMethods.getUserInput();
////				BDDWrapper.free(bdd, counterValue);
//				
//				//compute possible next moves based on environment 
//				int nextEnv = BDDWrapper.EpostImage(bdd, state, atomicGS.getVariablesAndActionsCube(), envTransitionRelation, atomicGS.getVprimetoV());
//				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "next env", nextEnv);
//				
//				int[] nextEnvMoves = BDDWrapper.enumerate(bdd, nextEnv, atomicGS.variables);
//				BDDWrapper.free(bdd, nextEnv);
//				
//				//does the state satisfies the predicate guarantees[index]?
//				int satG_index = BDDWrapper.and(bdd, state, guarantees.get(index));
//				int nextIndex; 
//				int fromLayerIndex;
//				
//				//TODO: generalize, the state might satisfy the next guarantee too, 
//				//then we should increase the counter until the next guartantee that is not satisfied
//				//if a state satisfies all the guarantees, just apply an action and take it from there 
//				//right now we assume that liveness goals do not overlap --> extend to remove this assumption
//				if(satG_index == state){
//					//the state satisfies the guarantee[index] 
//					//update the counter
//					nextIndex = (index+1)%numOfGuarantees;
//					fromLayerIndex = locateState(bdd, state, mY.get(nextIndex));
//					
//					System.out.println("state satisfies guarantee "+index);
//					System.out.println("next index is "+nextIndex);
//					System.out.println("state is in layer "+fromLayerIndex);
//					UtilityMethods.getUserInput();
//				}else{
//					nextIndex = index;
//					fromLayerIndex = locateState(bdd, state, mY.get(index));
//					System.out.println("next index is "+nextIndex);
//					System.out.println("state is in layer "+fromLayerIndex);
//					UtilityMethods.getUserInput();
//				}
//				
//				//for each input
//				for(int inp : nextEnvMoves){
//					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "considering input ", inp);
//					
//					boolean winningSysMoveFound = false;
//					
//					//choose a next state 
//					//what are the possible next winning moves for the system 
//					int nextSys = BDDWrapper.EpostImage(bdd, inp, atomicGS.getVariablesAndActionsCube(), sysTransitionRelation, atomicGS.getVprimetoV());
//					nextSys = BDDWrapper.andTo(bdd, nextSys, winningStates);
//					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "possible winning next states", nextSys);
//					
//					//choose a next state that goes deeper into mY[nextIndex],
//					//i.e., min_r s.t. q' \in mY[nextIndex][r] and q->q'
//					ArrayList<Integer> nextGuaranteeLayers = mY.get(nextIndex);
//					//TODO: handle the case where fromLayerIndex = 0, meaning that the state satisfies multiple guarantees
//					//at the same time
//					for(int i=0; i<fromLayerIndex;i++){
//						//is there any winning state in the layer i? 
//						int intersect = BDDWrapper.and(bdd, nextSys, nextGuaranteeLayers.get(i));
//						if(intersect != 0){
//							
//							UtilityMethods.debugBDDMethods(bdd,"intersection is non-empty with layer "+i, intersect);
//							UtilityMethods.getUserInput();
//							
//							//remove the intersect from the nextSys
//							int tmp = BDDWrapper.diff(bdd, nextSys, intersect);
//							BDDWrapper.free(bdd, nextSys);
//							nextSys = tmp;
//							
//							//enumerate the states in the intersect 
//							int[] nextSysMoves = BDDWrapper.enumerate(bdd, intersect, atomicGS.variables);
//							
//							//choose a state from intersect 
//							for(int j =0 ; j<nextSysMoves.length; j++){
//								//check if the transition is winning w.r.t. intermediate states
//								int checkIntermediate = areIntermediateStatesWinning(bdd, regionGraph, state, 
//										nextSysMoves[j], regionToVarMap, 
//										varToRegionMap, regionVars, assumptions, fromLayerIndex, mY.get(nextIndex), 
//										mX.get(nextIndex));
//								
//								
//								if(checkIntermediate !=-1){
//									
//									System.out.println("all intermediate states are fine!");
//									
//									//if yes, add the transition to the strategy
//									int from = BDDWrapper.and(bdd, inp, counterValue);
//									int nextCounter = BDDWrapper.assign(bdd, nextIndex, counterPrime);
//									int next = BDDWrapper.replace(bdd, nextSysMoves[j], atomicGS.getVtoVprime());
//									int to = BDDWrapper.and(bdd, nextCounter, next);
//									BDDWrapper.free(bdd,nextCounter);
//									BDDWrapper.free(bdd, next);
//									int trans = BDDWrapper.and(bdd, from, to);
//									BDDWrapper.free(bdd, from);
//									BDDWrapper.free(bdd, to);
//									
//									UtilityMethods.debugBDDMethodsAndWait(bdd, "adding transition to the strategy", trans);
//									
//									strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, trans);
//									BDDWrapper.free(bdd, trans);
//									
//									//add the intermediate states and next state to Q
//									int[] intermediateStates = BDDWrapper.enumerate(bdd, checkIntermediate, atomicGS.variables);
//									BDDWrapper.free(bdd, checkIntermediate);
//									nextCounter = BDDWrapper.assign(bdd, nextIndex, counter);
//									int nextState = BDDWrapper.and(bdd, nextSysMoves[j], nextCounter);
//									Q.add(nextState);
//									UtilityMethods.debugBDDMethodsAndWait(bdd, "destination added to the Q", nextSysMoves[j]);
//									for(int is : intermediateStates){
//										int nextIS = BDDWrapper.and(bdd, is, nextCounter);
//										Q.add(nextIS);
//										UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate state added to the Q", nextIS);
//										BDDWrapper.free(bdd, is);
//									}
//									BDDWrapper.free(bdd, nextCounter);
//									
//									//free intersect
//									BDDWrapper.free(bdd, intersect);
//									for(int k=j;k<nextSysMoves.length;k++){
//										BDDWrapper.free(bdd, nextSysMoves[k]);
//									}
//									
//									//break and go to the next input
//									winningSysMoveFound = true;
//									break;
//								}else{
//									BDDWrapper.free(bdd, nextSysMoves[j]);
//								}
//							}
//						}
//						if(winningSysMoveFound){
//							BDDWrapper.free(bdd, nextSys);
//							break; 
//						}
//					}
//					
//					//if we cannot push forward, can we violate an assumption?
//					if(!winningSysMoveFound){
//					
//						if(assumptions != null && assumptions.size() !=0){
//							for(int i=0; i<assumptions.size(); i++){
//								int notAssumption_i = BDDWrapper.not(bdd, assumptions.get(i));
//								int intersect = BDDWrapper.and(bdd, state, notAssumption_i);
//								if(intersect == state){
//									int layer = mX.get(nextIndex).get(i).get(fromLayerIndex);
//									int w = BDDWrapper.and(bdd, nextSys, layer);
//									if(w!=0){
//										//TODO: remove the intersect from the nextSys
//										int tmp = BDDWrapper.diff(bdd, nextSys, intersect);
//										BDDWrapper.free(bdd, nextSys);
//										nextSys = tmp;
//										
//										//enumerate the states in the intersect 
//										int[] nextSysMoves = BDDWrapper.enumerate(bdd, intersect, atomicGS.variables);
//										
//										//choose a state from intersect 
//										for(int j =0 ; j<nextSysMoves.length; j++){
//											//check if the transition is winning w.r.t. intermediate states
//											int checkIntermediate = areIntermediateStatesWinning(bdd, regionGraph, state, 
//													nextSysMoves[j], regionToVarMap, 
//													varToRegionMap, regionVars, assumptions, fromLayerIndex, mY.get(nextIndex), 
//													mX.get(nextIndex));
//											
//											if(checkIntermediate !=-1){
//												//if yes, add the transition to the strategy
//												int from = BDDWrapper.and(bdd, inp, counterValue);
//												int nextCounter = BDDWrapper.assign(bdd, nextIndex, counterPrime);
//												int next = BDDWrapper.replace(bdd, nextSysMoves[j], atomicGS.getVtoVprime());
//												int to = BDDWrapper.and(bdd, nextCounter, next);
//												BDDWrapper.free(bdd,nextCounter);
//												BDDWrapper.free(bdd, next);
//												int trans = BDDWrapper.and(bdd, from, to);
//												BDDWrapper.free(bdd, from);
//												BDDWrapper.free(bdd, to);
//												strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, trans);
//												BDDWrapper.free(bdd, trans);
//												
//												//add the intermediate states and next state to Q
//												int[] intermediateStates = BDDWrapper.enumerate(bdd, checkIntermediate, atomicGS.variables);
//												BDDWrapper.free(bdd, checkIntermediate);
//												nextCounter = BDDWrapper.assign(bdd, nextIndex, counter);
//												int nextState = BDDWrapper.and(bdd, nextSysMoves[j], nextCounter);
//												Q.add(nextState);
//												for(int is : intermediateStates){
//													int nextIS = BDDWrapper.and(bdd, is, nextCounter);
//													Q.add(nextIS);
//													UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate state added to the Q", nextIS);
//													BDDWrapper.free(bdd, is);
//												}
//												BDDWrapper.free(bdd, nextCounter);
//												
//												//free intersect
//												BDDWrapper.free(bdd, intersect);
//												for(int k=j;k<nextSysMoves.length;k++){
//													BDDWrapper.free(bdd, nextSysMoves[k]);
//												}
//												
//												//break and go to the next input
//												winningSysMoveFound = true;
//												break;
//											}else{
//												BDDWrapper.free(bdd, nextSysMoves[j]);
//											}
//										}
//									}
//								}
//								BDDWrapper.free(bdd, notAssumption_i);
//								BDDWrapper.free(bdd, intersect);
//								if(winningSysMoveFound){
//									break;
//								}
//							}
//						}
//					}
//					
//					
//					
//					
////					//does the state satisfies the predicate guarantees[index]?
////					if(satG_index == state){
////						//the state satisfies the guarantee[index] 
////						//update the counter
////						nextIndex = (index+1)%numOfGuarantees;
////						
////						
////						//what are the possible next winning moves for the system 
////						int nextSys = BDDWrapper.EpostImage(bdd, inp, atomicGS.getVariablesAndActionsCube(), sysTransitionRelation, atomicGS.getVprimetoV());
////						nextSys = BDDWrapper.andTo(bdd, nextSys, winningStates);
////						
////						//choose a next state that goes deeper into mY[nextIndex],
////						//i.e., min_r s.t. q' \in mY[nextIndex][r] and q->q'
////						ArrayList<Integer> nextGuaranteeLayers = mY.get(nextIndex);
////						for(int i=0; i<nextGuaranteeLayers.size();i++){
////							//is there any winning state in the layer i? 
////							int intersect = BDDWrapper.and(bdd, nextSys, nextGuaranteeLayers.get(i));
////							if(intersect != 0){
////								//TODO: remove the intersect from the nextSys
////								
////								//enumerate the states in the intersect 
////								int[] nextSysMoves = BDDWrapper.enumerate(bdd, intersect, atomicGS.variables);
////								
////								//choose a state from intersect 
////								for(int j =0 ; j<nextSysMoves.length; j++){
////									//check if the transition is winning w.r.t. intermediate states
////									
////									//if yes, add the transition to the strategy
////									int trans = BDDWrapper.and(bdd, inp, nextSysMoves[j]);
////									strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, trans);
////									BDDWrapper.free(bdd, trans);
////									
////									//free intersect 
////									
////									//break and go to the next input
////									break;
////								}
////								
////								
////							}
////						}
////					}else{//if not, which layer r of mY[index][r] it belongs to? Can it push to deeper level?
////						
////					}
//					
//					
//					
//					//if not, can we loop and violate an environment assumption? 
//					
//					//check if all the intermediate states belong to deeper levels 
//					
//					//if yes, add the transition to the strategy, and add the next state and intermediate states 
//					//for future processing 
//				}
//				
//				//state is processed and added to P already, release the memory
//				BDDWrapper.free(bdd, state);
//				BDDWrapper.free(bdd, counterValue);
//				
//				
//			}
//			
//			int sameCounter = BDDWrapper.same(bdd,counter);
//			envTransitionRelation = BDDWrapper.andTo(bdd, envTransitionRelation, sameCounter); 
//			BDDWrapper.free(bdd, sameCounter);
//			
//			
//			GameStructure strat = new GameStructure(bdd, strategyVars,Variable.getPrimedCopy(strategyVars),strategyInit,envTransitionRelation, strategy_T_sys, atomicGS.actionVars, atomicGS.actionVars);
//			GameSolution result = new GameSolution(atomicGS, Player.SYSTEM, strat, gr1w.getWinningStates());
//			return result; 
//			
//			
////			GameSolution result = new GameSolution(atomicGS, Player.SYSTEM, strategy, gr1w.getWinningStates());
//		}
//		
//		
//		
//		System.out.println("the specification is unrealizable!");
//		return new GameSolution(atomicGS, Player.ENVIRONMENT, null, gr1w.getWinningStates());
//		
//	}
	
	
	/**
	 * return the index of the deepest layer in mY_j that contains the state, -1 if mY_j does not contain the state
	 * @param bdd
	 * @param state
	 * @param mY_j
	 * @return
	 */
	private static int locateState(BDD bdd, int state,  ArrayList<Integer> mY_j){
		for(int i=0; i<mY_j.size();i++){
			int intersect = BDDWrapper.and(bdd, state, mY_j.get(i));
			if(intersect !=0 ){
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the set of intermediate states if all of them belong to deeper level, -1 otherwise
	 * Any intermediate state that can be visited during transition from fromState to the toState must 
	 * belong to a deeper level than the fromLayerIndex, i.e., q_int \in mY[<fromLayerIndex] 
	 * or there exists an assumption that is violated in the current state and we can stay in the corresponding mX layer, 
	 * i.e., \exists a_j \in assumptions s.t. fromState \models \neg a_j and mx[j][i][r] and  
	 *  
	 * @param bdd
	 * @param regionGraph
	 * @param fromState
	 * @param toState
	 * @param regionToVarMap
	 * @param varToRegionMap
	 * @param regionVars
	 * @param fromLayerIndex
	 * @param mY_j
	 * @param mX_j
	 * @return
	 */
	private static int areIntermediateStatesWinning(BDD bdd, DirectedGraph<String, Node, Edge<Node>> regionGraph, 
			int fromState, int toState, 
			HashMap<Node, Variable> regionToVarMap, 
			HashMap<Variable, Node> varToRegionMap, 
			Variable[] regionVars, 
			ArrayList<Integer> assumptions, 
			int fromLayerIndex, ArrayList<Integer> mY_j, ArrayList<ArrayList<Integer>> mX_j ){
		
//		System.out.println("checking if the intermediate states are winning");
//		UtilityMethods.debugBDDMethods(bdd, "source state ", fromState);
//		UtilityMethods.debugBDDMethods(bdd, "destination state ", toState);
//		System.out.println("the source state is in layer "+fromLayerIndex);
//		UtilityMethods.getUserInput();
		
		//compute intermediate states 
		int intermediateStates = intermediateStates(bdd, regionGraph, fromState, toState, regionToVarMap, 
				varToRegionMap, regionVars);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate states computed ", intermediateStates);
		
		int winningIntermediateStates = bdd.ref(bdd.getZero());
		
		//remove interemediate states that belong to deeper levels in mY
		for(int i=0; i<fromLayerIndex; i++){
			int currentLayer = mY_j.get(i); 
			int intersect = BDDWrapper.and(bdd, currentLayer, intermediateStates);
			if(intersect!=0){
				int w = BDDWrapper.diff(bdd, intermediateStates, intersect);
				winningIntermediateStates = BDDWrapper.orTo(bdd, winningIntermediateStates, intersect);
				BDDWrapper.free(bdd, intersect);
				BDDWrapper.free(bdd, intermediateStates);
				intermediateStates = w; 
			}
		}
		
		
		if(intermediateStates == 0){//all the intermediate states belong to a deeper level
			
//			System.out.println("all intermediate states belong to deeper levels");
			
			return winningIntermediateStates;
		}else{//check for assumptions violation
			if(assumptions != null && assumptions.size() !=0){
				for(int i=0; i<assumptions.size(); i++){
					if(intermediateStates == 0){
						return winningIntermediateStates; 
					}
					int notAssumption_i = BDDWrapper.not(bdd, assumptions.get(i));
					int intersect = BDDWrapper.and(bdd, fromState, notAssumption_i);
					if(intersect == fromState){
						int layer = mX_j.get(i).get(fromLayerIndex);
						int w = BDDWrapper.and(bdd, intermediateStates, layer);
						winningIntermediateStates = BDDWrapper.orTo(bdd, winningIntermediateStates, w);
						if(w!=0){
							int tmp = BDDWrapper.diff(bdd, intermediateStates, w);
							BDDWrapper.free(bdd, w);
							BDDWrapper.free(bdd, intermediateStates);
							intermediateStates = tmp;
						}
					}
					BDDWrapper.free(bdd, notAssumption_i);
					BDDWrapper.free(bdd, intersect);
				}
			}
		}
		
		BDDWrapper.free(bdd, intermediateStates);
		return -1;
		
	}
	
	private static int intermediateStates(BDD bdd, DirectedGraph<String, Node, Edge<Node>> regionGraph, 
			int currentState, int nextState, HashMap<Node, Variable> regionToVarMap, 
			HashMap<Variable, Node> varToRegionMap, 
			Variable[] regionVars){
		
		ArrayList<Node> currentPartitions = partitionStateIntoRegions(bdd, currentState, regionVars, varToRegionMap);
		ArrayList<Node> nextPartitions = partitionStateIntoRegions(bdd, nextState, regionVars, varToRegionMap);
		int intermediateStates = intermediateStates(bdd, regionGraph, currentPartitions, nextPartitions, regionToVarMap, regionVars);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "intermediate states are ", intermediateStates);
		
		//remove the currentstate and nextState from the set of intermediate states
		Variable[] varsOtherThanRegionVars = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), regionVars);
		int notRegionVarsCube = BDDWrapper.createCube(bdd, varsOtherThanRegionVars);
		int currentStateRegions = BDDWrapper.exists(bdd, currentState, notRegionVarsCube);
		int notCurrentStateRegions = BDDWrapper.not(bdd, currentStateRegions);
		int nextStateRegions = BDDWrapper.exists(bdd, nextState, notRegionVarsCube);
		int notNextStateRegions = BDDWrapper.not(bdd, nextStateRegions);
		intermediateStates = BDDWrapper.andTo(bdd, intermediateStates, notCurrentStateRegions);
		intermediateStates = BDDWrapper.andTo(bdd, intermediateStates, notNextStateRegions);
		BDDWrapper.free(bdd, notRegionVarsCube);
		BDDWrapper.free(bdd, currentStateRegions);
		BDDWrapper.free(bdd, nextStateRegions);
		BDDWrapper.free(bdd, notCurrentStateRegions);
		BDDWrapper.free(bdd, notNextStateRegions);
		
//		int notCurrentState = BDDWrapper.not(bdd, currentState);
//		int notNextState = BDDWrapper.not(bdd, nextState);
//		intermediateStates = BDDWrapper.andTo(bdd, intermediateStates, notCurrentState);
//		intermediateStates = BDDWrapper.andTo(bdd, intermediateStates, notNextState);
//		BDDWrapper.free(bdd, notCurrentState);
//		BDDWrapper.free(bdd, notNextState);
		
		//keep the input value the same as next state: the values of input variables do not change during jumping to an 
		//intermediate state 
		int regionVarsCube = BDDWrapper.createCube(bdd, regionVars);
		int nextInput = BDDWrapper.exists(bdd, nextState, regionVarsCube);
		BDDWrapper.free(bdd, regionVarsCube);
		intermediateStates = BDDWrapper.andTo(bdd, intermediateStates, nextInput);
		BDDWrapper.free(bdd, nextInput);
				
		return intermediateStates;
	}
	
	private static int intermediateStates(BDD bdd, DirectedGraph<String, Node, Edge<Node>> regionGraph, 
			ArrayList<Node> currentPartitions, ArrayList<Node> nextPartitions, 
			HashMap<Node, Variable> regionToVarMap,
			Variable[] regionVars){
		
		int result=bdd.ref(bdd.getOne());
		
		ArrayList<Variable> involvedVars = new ArrayList<Variable>();
		
		for(int i=0; i<currentPartitions.size(); i++){
			Node currentRegion = currentPartitions.get(i);
			Variable currentVar = regionToVarMap.get(currentRegion);
			if(!involvedVars.contains(currentVar)) involvedVars.add(currentVar);
			List<Edge<Node>> neighbors = regionGraph.getAdjacent(currentRegion);
			
			for(Edge<Node> e : neighbors){
				Node next = e.getTarget();
				if(nextPartitions.contains(next)){
//					nextNodes.add(next);
					Variable nextVar = regionToVarMap.get(next);
					if(!involvedVars.contains(nextVar)) involvedVars.add(nextVar);
					int intermediateStates = BDDWrapper.or(bdd, currentVar.getBDDVar(), nextVar.getBDDVar());
					result=BDDWrapper.andTo(bdd, result, intermediateStates);
					BDDWrapper.free(bdd, intermediateStates);
				}
			}
			
			if(nextPartitions.contains(currentRegion)) result=BDDWrapper.andTo(bdd, result, currentVar.getBDDVar());
		}
		
		Variable[] involvedVarsArray = involvedVars.toArray(new Variable[involvedVars.size()]);
		Variable[] notInvolved = Variable.difference(regionVars, involvedVarsArray);
		int notInvolvedFormula = bdd.ref(bdd.getOne());
		for(int i=0; i<notInvolved.length; i++){
			notInvolvedFormula = BDDWrapper.andTo(bdd, notInvolvedFormula, BDDWrapper.not(bdd, notInvolved[i].getBDDVar()));
		}
		
		result = BDDWrapper.andTo(bdd, result, notInvolvedFormula);
		BDDWrapper.free(bdd, notInvolvedFormula);
		
		
		return result;
	}
	

	private static ArrayList<Node> partitionStateIntoRegions(BDD bdd, int state, Variable[] regionVars, HashMap<Variable, Node> varToRegionMap){
		ArrayList<Variable> involvedRegions = partitionStateIntoRegions(bdd, state, regionVars);
		ArrayList<Node> result = new ArrayList<Node>();
		for(Variable var : involvedRegions){
			result.add(varToRegionMap.get(var));
		}
		return result;
	}
	
	private static ArrayList<Variable> partitionStateIntoRegions(BDD bdd, int state, Variable[] regionVars){
		ArrayList<Variable> result = new ArrayList<Variable>();
		for(int i=0; i<regionVars.length; i++){
			int includesRegion = BDDWrapper.and(bdd, state, regionVars[i].getBDDVar());
			if(includesRegion != 0 ){
				result.add(regionVars[i]);
			}
			BDDWrapper.free(bdd, includesRegion);
		}
		return result;
	}
	
//	private static GameSolution swarmReactiveControllerSynthesis(BDD bdd, GameStructure gs, int init, GR1Objective objective){
//		//in an infinite loop
//		//solve the GR(1) game to obtain the set of winning states		
//		do{
//			GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
//		}while(true);
//				
//		//the specification is unrealizable considering all the intermediate states
//	}
	

	
	public static int createRegionPredicate(BDD bdd, Variable[] regionVars, ArrayList<Variable> occupiedRegions){
		int predicate = bdd.ref(bdd.getOne());
		for(int i=0; i<regionVars.length; i++){
			if(occupiedRegions.contains(regionVars[i])){
				predicate = BDDWrapper.andTo(bdd, predicate, regionVars[i].getBDDVar());
			}else{
				predicate = BDDWrapper.andTo(bdd, predicate, bdd.not(regionVars[i].getBDDVar()));
			}
		}
		return predicate;
	}

	private static int directedGraphToTransitionRelation(BDD bdd, DirectedGraph<String, Node, Edge<Node>> graph, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		
		System.out.println("creating graph transition relation");
		
		
		int transitionFormula = bdd.ref(bdd.getOne());
		
		Set<Node> sources = graph.getSourceNodeSet();
		
		for(Node n : sources){
			
//			System.out.println("computing forward rules for node ");
//			n.printNode();
			
			List<Edge<Node>> adj = graph.getAdjacent(n);
			ArrayList<Node> neighbors = new ArrayList<Node>();
			for(Edge<Node> e : adj){
				if(!neighbors.contains(e.getTarget())){
					neighbors.add(e.getTarget());
				}
			}
			neighbors.add(n);
			
			Variable fromRegion = regionToVarMap.get(n);
			int transition = bdd.ref(bdd.getZero());
			for(Node o : neighbors){
				Variable toRegion = regionToVarMap.get(o);
				transition = BDDWrapper.orTo(bdd, transition, toRegion.getPrimedCopy().getBDDVar());
			}
			int transitionRule = BDDWrapper.implies(bdd, fromRegion.getBDDVar(), transition);
			BDDWrapper.free(bdd, transition);
			transitionFormula = BDDWrapper.andTo(bdd, transitionFormula, transitionRule);
			BDDWrapper.free(bdd, transitionRule);
		}
		
		DirectedGraph<String, Node, Edge<Node>> reverseGraph = graph.getReversed();
		
		Set<Node> destinations = reverseGraph.getSourceNodeSet();
		
		for(Node n : destinations){
			
//			System.out.println("computing backward rules for node ");
//			n.printNode();
			
			List<Edge<Node>> adj = reverseGraph.getAdjacent(n);
			ArrayList<Node> neighbors = new ArrayList<Node>();
			for(Edge<Node> e : adj){
				if(!neighbors.contains(e.getTarget())){
					neighbors.add(e.getTarget());
				}
			}
			neighbors.add(n);
			
			Variable fromRegion = regionToVarMap.get(n);
			int transition = bdd.ref(bdd.getZero());
			for(Node o : neighbors){
				Variable toRegion = regionToVarMap.get(o);
				transition = BDDWrapper.orTo(bdd, transition, toRegion.getBDDVar());
			}
			int transitionRule = BDDWrapper.implies(bdd, fromRegion.getPrimedCopy().getBDDVar(), transition);
			BDDWrapper.free(bdd, transition);
			transitionFormula = BDDWrapper.andTo(bdd, transitionFormula, transitionRule);
			BDDWrapper.free(bdd, transitionRule);
		}
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "Graph translated into a logical formula", transitionFormula);
		
//		String[] minterms = BDDWrapper.parsePrintSetOutput(bdd, transitionFormula);
//		System.out.println("num of minterms "+minterms.length);
////		for(String m : minterms){
////			System.out.print(m);
////		}
//		UtilityMethods.getUserInput();
		
		return transitionFormula;
	}
	
	
	/**
	 * Creates a transition relation for swarm from the given region graph such that at each step only atomic transitions 
	 * are allowed, i.e., only a single part of swarm can move at each step. 
	 * @param bdd
	 * @param graph
	 * @param regionVars
	 * @param regionToVarMap
	 * @return
	 */
	private static int directedGraphToAtomicTransitionRelation(BDD bdd, DirectedGraph<String, Node, Edge<Node>> graph, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		
		System.out.println("creating graph atomic transition relation");
		
		
//		int transitionFormula = bdd.ref(bdd.getZero());
		
		//define stutter action where region variables stay the same 
		int transitionFormula = BDDWrapper.same(bdd, regionVars);
		
		Set<Node> sources = graph.getSourceNodeSet();
		
		for(Node n : sources){
			
//			System.out.println("computing forward rules for node ");
//			n.printNode();
			
			List<Edge<Node>> adj = graph.getAdjacent(n);
			ArrayList<Node> neighbors = new ArrayList<Node>();
			for(Edge<Node> e : adj){
				if(!neighbors.contains(e.getTarget())){
					neighbors.add(e.getTarget());
				}
			}
//			neighbors.add(n);
			Variable fromRegion = regionToVarMap.get(n);
			
			for(Node o : neighbors){
				Variable toRegion = regionToVarMap.get(o);
				//make sure that o is not the same as n, self loops are not allowed!
				if(o==n){
					System.err.println("An edge from a node to itself is not allowed in the region graph! the self loop is ignored");
					continue; 
				}
				
				//regions other than n and o stay the same 
				ArrayList<Variable> unchangedRegions = new ArrayList<Variable>();
				for(Variable v : regionVars){
					if(v!=fromRegion && v!=toRegion){
						unchangedRegions.add(v);
					}
				}
				int atomicTransitions = BDDWrapper.same(bdd, unchangedRegions.toArray(new Variable[unchangedRegions.size()]));
				
				//\pi_n \wedge \neg \pi_o \wedge \pi_n' \wedge \pi_o'
				int case1 = BDDWrapper.and(bdd, fromRegion.getBDDVar(), BDDWrapper.not(bdd, toRegion.getBDDVar()));
				int case1Prime = BDDWrapper.and(bdd, fromRegion.getPrimedCopy().getBDDVar(), toRegion.getPrimedCopy().getBDDVar());
				case1= BDDWrapper.andTo(bdd, case1, case1Prime);
				BDDWrapper.free(bdd, case1Prime);
				
				//\pi_n \wedge \pi_o \wedge \neg \pi_n' \wedge \pi_o'
				int case2 = BDDWrapper.and(bdd, fromRegion.getBDDVar(), toRegion.getBDDVar());
				int case2Prime = BDDWrapper.and(bdd, BDDWrapper.not(bdd, fromRegion.getPrimedCopy().getBDDVar()), toRegion.getPrimedCopy().getBDDVar());
				case2= BDDWrapper.andTo(bdd, case2, case2Prime);
				BDDWrapper.free(bdd, case2Prime);
				
//				System.out.println("from region is "+fromRegion.getName());
//				System.out.println("to region is "+toRegion.getName());
				
				int trans = BDDWrapper.or(bdd, case1, case2);
				BDDWrapper.free(bdd, case1);
				BDDWrapper.free(bdd, case2);
				atomicTransitions = BDDWrapper.andTo(bdd, atomicTransitions, trans);
				BDDWrapper.free(bdd, trans);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "atomic trans is", atomicTransitions);
				
//				atomicTransitions = BDDWrapper.andTo(bdd, atomicTransitions, case1);
//				BDDWrapper.free(bdd, case1);
//				atomicTransitions = BDDWrapper.andTo(bdd, atomicTransitions, case2);
//				BDDWrapper.free(bdd, case2);
				
				transitionFormula = BDDWrapper.orTo(bdd, transitionFormula, atomicTransitions);
				BDDWrapper.free(bdd, atomicTransitions);
			}
			
		}
		
		return transitionFormula;
	}
	
	public static void generateCorridorExample14MoreEdgesRegionsForLatex(){
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
								
		//11 regions 
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
				new Node("L"), new Node("M"), new Node("N")};
					
		//A <-> B
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
					
			//A <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));

					
			//B <-> C
			regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
					
			//B <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
					
			//C <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
					
			//D <-> E
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
			
			//D <-> F
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
			
			//D <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[3]));
			
			//D <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[3]));
					
			//E <-> H
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[4]));
			
			//E <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
					
			//F <-> H
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
			
			//F <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[5]));
					
			//H <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[6]));
					
			//H <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[7]));
							
			//H <-> J
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[9]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[7]));
					
			//H <-> K
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[10]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[7]));
				
			//J <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[9]));
			
			//J <-> L
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[9]));
				
			//K <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[10]));
			
			//K <-> L
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[10]));
					
			//L <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[6]));
					
			//L <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[11]));
			
			//L <-> M
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[12]));
					
			//L <-> N
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[13]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[11]));
							
					
			//M <-> N
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[13]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[12]));
			
//			String spec = generateSlugsSpecCorridorExample(regionGraph);
			String spec = generateLatexSpec(regionGraph);
			System.out.println(spec);
	}
	
	public static void generateCorridorExample14MoreEdgesRegionsForSlugs(){
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
								
		//11 regions 
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("O"), new Node("P"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
				new Node("L"), new Node("M"), new Node("N")};
					
		//A <-> B
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
					
			//A <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));

					
			//B <-> C
			regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
					
			//B <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
					
			//C <-> D
			regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
					
			//D <-> E
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
			
			//D <-> F
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
			
			//D <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[3]));
			
			//D <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[3]));
					
			//E <-> H
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[4]));
			
			//E <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
					
			//F <-> H
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
			
			//F <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[5]));
					
			//H <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[7]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[6]));
					
			//H <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[7]));
							
			//H <-> J
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[9]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[7]));
					
			//H <-> K
			regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[10]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[7]));
				
			//J <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[6]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[9]));
			
			//J <-> L
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[9]));
				
			//K <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[10]));
			
			//K <-> L
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[10]));
					
			//L <-> G
			regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[6]));
					
			//L <-> I
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[8]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[11]));
			
			//L <-> M
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[12]));
					
			//L <-> N
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[13]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[11]));
							
					
			//M <-> N
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[13]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[12]));
			
//			String spec = generateSlugsSpecCorridorExample(regionGraph);
			String spec = generateLatexSpec(regionGraph);
			System.out.println(spec);
	}
	
	public static void generateCorridorExample14RegionsForSlugs(){
	//define region graph
			DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
									
			//11 regions 
			Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
					new Node("E"), new Node("O"), new Node("P"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
					new Node("L"), new Node("M"), new Node("N")};
						
			//A <-> B
			regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
						
				//A <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));

						
				//B <-> C
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
						
				//B <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
						
				//C <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
						
				//D <-> E
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
				
				//D <-> F
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
						
				//E <-> H
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[4]));
						
				//F <-> H
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
						
				//H <-> G
				regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[6]));
						
				//H <-> I
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[8]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[7]));
								
				//H <-> J
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[9]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[7]));
						
				//H <-> K
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[10]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[7]));
										
				//J <-> L
				regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[9]));
						
				//K <-> L
				regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[10]));
						
				//L <-> M
				regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[12]));
						
				//L <-> N
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[13]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[11]));
								
						
				//M <-> N
				regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[13]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[12]));
				
				String spec = generateSlugsSpecCorridorExample(regionGraph);
				System.out.println(spec);
	}
	
	public static void generateCorridorExample16RegionsForSlugs(){
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
									
		//11 regions 
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
					new Node("E"), new Node("Q"), new Node("R"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
					new Node("L"), new Node("M"), new Node("N"), new Node("O"), new Node("P")};
						
				//A <-> B
				regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
						
				//A <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));

						
				//B <-> C
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
						
				//B <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
						
				//C <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
						
				//D <-> E
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
				
				//D <-> F
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
						
				//E <-> H
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[4]));
						
				//F <-> H
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
						
				//H <-> G
				regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[6]));
						
				//H <-> I
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[8]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[7]));
								
				//H <-> J
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[9]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[7]));
						
				//H <-> K
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[10]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[7]));
										
				//J <-> L
				regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[9]));
						
				//K <-> L
				regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[10]));
						
				//L <-> M
				regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[11]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[12]));
						
				//L <-> N
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[13]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[11]));
								
				//L <-> O
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[14]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[14], regionNodes[11]));
						
				//L <-> P
				regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[15]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[15], regionNodes[11]));
						
				//M <-> N
				regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[13]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[12]));
						
				//N <-> O
				regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[14]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[14], regionNodes[13]));
						
				//O <-> P
				regionGraph.addEdge(new Edge<Node>(regionNodes[14], regionNodes[15]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[15], regionNodes[14]));
				
				String spec = generateSlugsSpecCorridorExample(regionGraph);
				System.out.println(spec);
	}
	
	public static void generateCorridorExampleForSlugs(){
		//define region graph
				DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
							
				//11 regions 
				Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
						new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I"), new Node("J"), new Node("K")};
				
				//A <-> B
				regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
				
				//B <-> C
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
				
				//B <-> D
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
				
				//B <-> E
				regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[4]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[1]));
				
				//D <-> F
				regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
				
				//E <-> F
				regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[5]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[4]));
				
				//F <-> G
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
				
				//F <-> H
				regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
				
				//G <-> I
				regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[8]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[6]));
						
				//H <-> I
				regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[8]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[7]));
				
				//I <-> J
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[9]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[8]));
								
				//I <-> K
				regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[10]));
				regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[8]));
				
		String spec = generateSlugsSpecCorridorExample(regionGraph);
		System.out.println(spec);
	}
	
	public static String generateLatexSpec(DirectedGraph<String, Node, Edge<Node>> regionGraph){
		String result=""; 
		List<Node> nodes = regionGraph.getNodes();
		//input variables 
		result+="[INPUT]\n";
		result+="\\begin{itemize}\n";
		result+="\\item $input0$\n";
		result+="\\item $input1$\n";
		//region propositions 
		for(Node n : regionGraph.getNodes()){
			result+="\\item $"+n.getName()+"$\n";
		}
		result+="\\end{itemize}\n";
		
		//output variables
		result+="\n[OUTPUT]\n";
		result+="\\begin{itemize}\n";
		//stay put actions 
		for(Node n : regionGraph.getNodes()){
			result+="\\item $"+n.getName()+n.getName()+"$\n";
		}
		//transition actions 
		for(Edge<Node> e : regionGraph.getAllEdges()){
			result+="\\item $"+e.getFrom().getName()+e.getTarget().getName()+"$\n";
		}
		result+="\\end{itemize}\n";
		
		//env init 
		result+="\n[ENV_INIT]\n";
		result+="$\\neg input0 \\wedge \\neg input1 \\wedge ";
		for(int i=0; i<nodes.size();i++){
			if(i==1){
				result+=nodes.get(i).getName()+" \\wedge ";
			}else if(i==nodes.size()-1){
				result+="\\neg "+nodes.get(i).getName()+"$\n";
			}else{
				result+="\\neg "+nodes.get(i).getName()+" \\wedge ";
			}
			
		}
		
		//env transitions 
		result+="\n[ENV_TRANS]\n";
		result+="\\begin{itemize}\n";
		for(Node n : nodes){
			//self transition: r & rr -> r'
			String r = n.getName();
			result+="\\item $\\G("+r+" \\wedge "+r+r+" \\rightarrow "+r+"')$\n";
			
			String allActions = ""+r+r;
			
			//trans to other regions 
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r & rv -> r' | v' 
				result+="\\item $\\G("+r+" \\wedge "+r+v+" \\rightarrow "+r+"' \\vee "+v+"')$\n";  
				allActions+=" \\vee "+r+v;
			}
			
			//r & !allactions -> r'
			result+="\\item $\\G("+r+" \\wedge \\neg("+allActions+") \\rightarrow "+r+"')$\n";
			
			//r' -> r | (v & vr)
			result+="\\item $\\G("+r+"' \\rightarrow "+ r;
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r -> r' | (v & vr)  
				result+=" \\vee ("+v+" \\wedge "+v+r+")"; 
			}
			result+=")$\n";
		}
		
		result+="\\end{itemize}\n";
		
		//env liveness 
		result+= "\n[ENV_LIVENESS]\n";
		result+="\\begin{itemize}\n";
		for(Node n : nodes){
			String r = n.getName();
			
			boolean first = true;
			String allActions = "";
			
			//move a part to a region
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r & rv ->  v' 
				result+="\\item $\\GF("+r+" \\wedge "+r+v+" \\rightarrow "+v+"')$\n";  
				if(!first){
					allActions+=" \\vee "+r+v;
				}else{
					allActions = r+v+"";
					first=false;
				}
			}
			
			String incoming = ""; 
			for(Node n2 : nodes){
				List<Edge<Node>> incomingEdges = regionGraph.getAdjacent(n2);
				for(Edge<Node> e : incomingEdges){
					if(e.getTarget() == n){
						incoming += " \\wedge \\neg "+n2.getName()+r;
					}
				}
			}
			
			
			//move a part out of region 
			//r & !rr & !vr & ... & (rv | ... ) -> !r'
			result+="\\item $\\GF("+r +" \\wedge \\neg "+r+r+incoming+" \\wedge ("+allActions+") \\rightarrow \\neg "+r+"')$\n"; 
		}
		result+="\\end{itemize}\n";
		
		//sys_liveness
		result+="\n[SYS_LIVENESS]\n";
		result+="\\begin{itemize}\n";
		result+="\\item $\\GF(\\neg input0 \\wedge \\neg input1 \\rightarrow "; 
		ArrayList<Integer> positions_g1 = new ArrayList<Integer>();
		positions_g1.add(0);
		positions_g1.add(1);
		positions_g1.add(2); 
		result+=regionPredicate(nodes, positions_g1);
		result+=")$\n"; 
		
		result+="\\item $\\GF(input0 \\wedge \\neg input1 \\rightarrow "; 
		ArrayList<Integer> positions_g2 = new ArrayList<Integer>();
		positions_g2.add(4);
		positions_g2.add(5); 
		positions_g2.add(9);
		positions_g2.add(10); 
		result+=regionPredicate(nodes, positions_g2);
		result+=")$\n"; 
		
		result+="\\item $\\GF( \\neg input0 \\wedge input1 \\rightarrow "; 
		ArrayList<Integer> positions_g3 = new ArrayList<Integer>();
		positions_g3.add(6);
		positions_g3.add(8); 
		result+=regionPredicate(nodes, positions_g3);
		result+=")$\n"; 
		
		result+="\\item $\\GF( input0 \\wedge input1 \\rightarrow "; 
		ArrayList<Integer> positions_g4 = new ArrayList<Integer>();
		positions_g4.add(12);
		positions_g4.add(13); 
		result+=regionPredicate(nodes, positions_g4);
		result+=")$\n"; 	
		
		result+="\\end{itemize}\n";
		
		return result;
	}
	
	public static String generateSlugsSpecCorridorExample(DirectedGraph<String, Node, Edge<Node>> regionGraph){
		String result=""; 
		List<Node> nodes = regionGraph.getNodes();
		//input variables 
		result+="[INPUT]\n";
		result+="input0\n";
		result+="input1\n";
		//region propositions 
		for(Node n : regionGraph.getNodes()){
			result+=n.getName()+"\n";
		}
		
		//output variables
		result+="\n[OUTPUT]\n";
		//stay put actions 
		for(Node n : regionGraph.getNodes()){
			result+=n.getName()+n.getName()+"\n";
		}
		//transition actions 
		for(Edge<Node> e : regionGraph.getAllEdges()){
			result+=e.getFrom().getName()+e.getTarget().getName()+"\n";
		}
		
		//env init 
		result+="\n[ENV_INIT]\n";
		result+="!input0 & !input1 & ";
		for(int i=0; i<nodes.size();i++){
			if(i==1){
				result+=nodes.get(i).getName()+" & ";
			}else if(i==nodes.size()-1){
				result+="!"+nodes.get(i).getName()+"\n";
			}else{
				result+="!"+nodes.get(i).getName()+" & ";
			}
			
		}
		
		//env transitions 
		result+="\n[ENV_TRANS]\n";
		for(Node n : nodes){
			//self transition: r & rr -> r'
			String r = n.getName();
			result+=r+" & "+r+r+" -> "+r+"'\n";
			
			String allActions = ""+r+r;
			
			//trans to other regions 
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r & rv -> r' | v' 
				result+=r+" & "+r+v+" -> "+r+"' | "+v+"'\n";  
				allActions+=" | "+r+v;
			}
			
			//r & !allactions -> r'
			result+=r+" & !("+allActions+") -> "+r+"'\n";
			
			//r' -> r | (v & vr)
			result+=r+"' -> "+ r;
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r -> r' | (v & vr)  
				result+=" | ("+v+" & "+v+r+")"; 
			}
			result+="\n";
		}
		
		//env liveness 
		result+= "\n[ENV_LIVENESS]\n";
		for(Node n : nodes){
			String r = n.getName();
			
			boolean first = true;
			String allActions = "";
			
			//move a part to a region
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r & rv ->  v' 
				result+=r+" & "+r+v+" -> "+v+"'\n";  
				if(!first){
					allActions+=" | "+r+v;
				}else{
					allActions = r+v+"";
					first=false;
				}
			}
			
			String incoming = ""; 
			for(Node n2 : nodes){
				List<Edge<Node>> incomingEdges = regionGraph.getAdjacent(n2);
				for(Edge<Node> e : incomingEdges){
					if(e.getTarget() == n){
						incoming += " & !"+n2.getName()+r;
					}
				}
			}
			
			
			//move a part out of region 
			//r & !rr & !vr & ... & (rv | ... ) -> !r'
			result+=r +" & !"+r+r+incoming+" & ("+allActions+") -> !"+r+"'\n"; 
		}
		
		//sys_liveness
		result+="\n[SYS_LIVENESS]\n";
		
		result+="!input0 & !input1 -> "; 
		ArrayList<Integer> positions_g1 = new ArrayList<Integer>();
		positions_g1.add(0);
		positions_g1.add(1); 
		positions_g1.add(2); 
		result+=regionPredicate(nodes, positions_g1);
		result+="\n"; 
		
		result+="input0 & !input1 -> "; 
		ArrayList<Integer> positions_g2 = new ArrayList<Integer>();
		positions_g2.add(4);
		positions_g2.add(5); 
		positions_g2.add(9);
		positions_g2.add(10); 
		result+=regionPredicate(nodes, positions_g2);
		result+="\n"; 
		
		result+="!input0 & input1 -> "; 
		ArrayList<Integer> positions_g3 = new ArrayList<Integer>();
		positions_g3.add(6);
		positions_g3.add(8); 
		result+=regionPredicate(nodes, positions_g3);
		result+="\n"; 
		
		result+="input0 & input1 -> "; 
		ArrayList<Integer> positions_g4 = new ArrayList<Integer>();
		positions_g4.add(12);
		positions_g4.add(13); 
		result+=regionPredicate(nodes, positions_g4);
		result+="\n"; 	
		
		return result;
	}
	
	private static String regionPredicate(List<Node> nodes, ArrayList<Integer> positions){
		String result = "";
		for(int i=0; i<nodes.size();i++){
			String r = nodes.get(i).getName();
			if(positions.contains(i)){
				result+=r;
			}else{
				result+="!"+r;
			}
			if(i<nodes.size()-1){
				result+=" & ";
			}
		}
		return result; 
	}
}
