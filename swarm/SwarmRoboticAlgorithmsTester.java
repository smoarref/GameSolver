package swarm;

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.crypto.spec.PSource;

import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;
import solver.GR1Objective;
import solver.GR1Solver;
import specification.GridCell2D;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import jdd.sat.CNF;
import game.BDDWrapper;
import game.ControllableSinglePlayerDeterministicGameStructure;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Objective;
import game.Objective.ObjectiveType;
import game.Player;
import game.Variable;

public class SwarmRoboticAlgorithmsTester {

	public static void main(String[] args) {
//		simpleTest1(2);
//		BDD bdd = new BDD(10000, 1000);
//		Variable a = new Variable(bdd, "a");
//		Variable b = new Variable(bdd, "b");
//		
//		int f = BDDWrapper.and(bdd, a.getBDDVar(), b.getBDDVar());
//		UtilityMethods.debugBDDMethods(bdd, "f is", f);
//		
//		Variable a1 = new Variable(bdd,"a1");
//		Variable a2 = new Variable(bdd,"a2");
//		
//		int replace = BDDWrapper.or(bdd, a1.getBDDVar(), a2.getBDDVar());
//		int replaceFormula = BDDWrapper.biimp(bdd, a.getBDDVar(), replace);
//		int formula = BDDWrapper.and(bdd, f, replaceFormula);
//		
//		UtilityMethods.debugBDDMethods(bdd, "formula", formula);
//		
//		int exist = BDDWrapper.exists(bdd, formula, a.getBDDVar());
//		
//		UtilityMethods.debugBDDMethods(bdd, "existential quantification", exist);
//		
//		Variable[][] vars = new Variable[2][2];
//		vars[0][0]=a;
//		vars[0][1]=b;
//		vars[1][0]=a1;
//		vars[1][1]=a2;
//		
//		Variable[] vars1 = vars[1];
//		Variable.printVariables(vars1);
		
		
//		testDecentralizedLabledTransitionRelations1();
		
//		testSafeSwarmNavigation1();
		
//		decentralizedSymbolicSwarmControl1();
		
		decentralizedSymbolicSwarmControlTest();
		
	}
	
	public static DecentralizedSwarmSimulator2D decentralizedSymbolicSwarmControlTest(int dim){
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSimpleTest3();
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSynchronizationTest(dim);
		
		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createPatterns(dim);
		
		DecentralizedSwarmSimulator2D simulator = decentralizedSymbolicSwarmContollerSynthesis(specification);
		return simulator;
	}
	
	public static DecentralizedSwarmSimulator2D decentralizedSymbolicSwarmControlTest(){
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSimpleTest5();
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSynchronizationTest(3);
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createCornellWorkSpace1();
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createCornellWorkSpace2();
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createCorridorExample9();
		
		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSpheroExample();
		
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createHexbugExample();
		
		DecentralizedSwarmSimulator2D simulator = decentralizedSymbolicSwarmContollerSynthesis(specification);
		return simulator;
	}
	
	public static void decentralizedSymbolicSwarmControl1(){
//		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSimpleTest1();
		
		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSimpleTest4();
		
		DecentralizedSwarmSimulator2D simulator = decentralizedSymbolicSwarmContollerSynthesis(specification);
		ArrayList<GridCell2D> initialCells = simulator.getInitialPositions();
		System.out.println("initial cells are");
		GridCell2D.print(initialCells);
		UtilityMethods.getUserInput();
		
		System.out.println();
		while(true){
			ArrayList<GridCell2D> next = simulator.simulateOneStep();
			GridCell2D.print(next);
			UtilityMethods.getUserInput();
		}
	}
	
	//TODO: create a method that translates a directed graph to a transition relation 
	private static int directedGraphToTransitionRelation(BDD bdd, DirectedGraph<String, Node, Edge<Node>> graph, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		
		Collection<Edge<Node>> edges = graph.getAllEdges();
		int transitionFormula = bdd.ref(bdd.getOne());
		
		Set<Node> sources = graph.getSourceNodeSet();
		
		for(Node n : sources){
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
		
		return transitionFormula;
	}
	
	
	private static DirectedGraph<String, Node, Edge<Node>> createSimpleGraph(Node[][] regionNodes){
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
//		int size = regionNodes.length;
		for(int i=0;i<regionNodes.length;i++){
			for(int j=0; j<regionNodes[i].length-1;j++){
				Edge<Node> e1 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j+1]);
				regionGraph.addEdge(e1);
				Edge<Node> e2 = new Edge<Node>(regionNodes[i][j+1], regionNodes[i][j]);
				regionGraph.addEdge(e2);
			}
//			Edge<Node> e1 = new Edge<Node>(regionNodes[0][i], regionNodes[0][i+1]);
//			regionGraph.addEdge(e1);
//			Edge<Node> e2 = new Edge<Node>(regionNodes[0][i+1], regionNodes[0][i]);
//			regionGraph.addEdge(e2);
//			Edge<Node> e3 = new Edge<Node>(regionNodes[size-1][i], regionNodes[size-1][i+1]);
//			regionGraph.addEdge(e3);
//			Edge<Node> e4 = new Edge<Node>(regionNodes[size-1][i+1], regionNodes[size-1][i]);
//			regionGraph.addEdge(e4);
		}
		
		return regionGraph;
	}
	
	public static DecentralizedSwarmSimulator2D decentralizedSymbolicSwarmContollerSynthesis(SwarmIntermediateSpecification specification){
		BDD bdd = specification.getBDD();
		DirectedGraph<String, Node, Edge<Node>> regionGraph = specification.regionGraph;
		Variable[] regionVars = specification.getRegionVariables();
		Variable synchronizeSignal = specification.getSynchronizationSignal();
		Variable[] vars = specification.getVariables();
		HashMap<Node, Variable> regionToVarMap = specification.getRegionToVarMap();
		HashMap<Variable, Node> varToRegionMap = specification.getVarToRegionMap();
		int init = specification.getInit();
		ControllableSinglePlayerDeterministicGameStructure swarmGame = specification.getSwarmGame();
		GR1Objective objective = specification.objective;
		int safetyFormula = specification.getSafetyFormula();
		int unsafeStates = specification.getUnsafeStates();
		ArrayList<Integer> collectiveLivenessGuarantees = specification.getCollectiveLivenessGuarantees();
		
		
		
		//Synthesize a centralized LTS satisfying the specification
		//input: initial state, GR1 objective, transition relation based on region graph
		//output: a symbolic solution with partial synchronization
				
		System.out.println("Synthesizing a central solution");
		long t0_centralSolution = UtilityMethods.timeStamp();
				
		ControllableSinglePlayerDeterministicGameStructure centralSolution = computeCentralSolution(bdd, swarmGame, init, objective, regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph);
				
		if(centralSolution == null){
			System.out.println("Unrealizable specification!");
			return null;
		}
				
		UtilityMethods.duration(t0_centralSolution, "central solution computed in ");
//		System.out.println("\n\n Variables are ");
//		Variable.printVariables(centralSolution.variables);
//		System.out.println();
//		UtilityMethods.debugBDDMethods(bdd, "the centralized solution ", centralSolution.getSystemTransitionRelation());
//		printTransitionRelation(bdd, centralSolution.getSystemTransitionRelation(), 8);
//		UtilityMethods.getUserInput();
		
		
		//reduce the central solution, remove the stuttering states
		ControllableSinglePlayerDeterministicGameStructure reducedCentralSolution = reduceCentralSolution(bdd, centralSolution, regionVars, synchronizeSignal);
//		printTransitionRelation(bdd, reducedCentralSolution.getSystemTransitionRelation(), 8);
//		UtilityMethods.getUserInput();
				
		//partition the centralized solution
		//input: a deterministic symbolic solution with partial synchronization
		//output: a set of processes with their corresponding liveness synchronization skeleton
		//n: the length of central process
				
		long t0_partition = UtilityMethods.timeStamp();
				
		PartitionedCentralLTS pclts =  partitionStrategy(bdd, reducedCentralSolution, reducedCentralSolution.getInit(), regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph, vars, unsafeStates);
				
		UtilityMethods.duration(t0_partition, "central strategy partitioned in "); 
		
		
//		System.out.println("partitioning information");
//		System.out.println("looping index is "+pclts.getLoopingIndex());
//		ArrayList<ArrayList<Node>> partitions =  pclts.getPartitions();
//		ArrayList<ArrayList<Edge<Node>>> matchings = pclts.getMatching();
//		ArrayList<Boolean> synch = pclts.getSynchronization();
//		for(int i=0; i<partitions.size(); i++){
//			System.out.println("state "+i +" partitioned into ");
//			for(Node n : partitions.get(i)){
//				n.printNode();
//			}
//			System.out.println("matchings");
//			for(Edge<Node> e : matchings.get(i)){
//				e.printEdge();
//			}
//			System.out.println(synch.get(i)?"synchronize":"does not synchronize");
//		}
//		UtilityMethods.getUserInput();
				
		//extract local strategies
		long t0_extract = UtilityMethods.timeStamp();
				
		ArrayList<ArrayList<SynchedNode>> localStrategies =  extractLocalStrategies(pclts.getPartitions(), pclts.getMatching(), pclts.getSynchronization(), pclts.getLoopingIndex());
				
//		System.out.println();
		UtilityMethods.duration(t0_extract, "decentralized strategies extracted in ");
		System.out.println("printing decentralized strategies");
		for(int i=0; i<localStrategies.size();i++){
			System.out.println("printing decentralized strategy "+i);
			ArrayList<SynchedNode> localStrat = localStrategies.get(i); 
			for(int j=0; j<localStrat.size(); j++){
				localStrat.get(j).print();
			}
		}
		System.out.println();
//		UtilityMethods.getUserInput();
				
				
		int numOfProcesses = localStrategies.size();
		//TODO: check if it should be the following multiplied by two to account for intermediate states
		int processLength = localStrategies.get(0).size();
				
		int loopingIndex = pclts.getLoopingIndex();
				
		System.out.println("num of processes "+numOfProcesses);
		System.out.println("process length (without intermediate states) "+processLength);
		System.out.println("looping index "+loopingIndex);
//		UtilityMethods.getUserInput();
				
		//reduce liveness synchronization
		//input: a set of processes with their corresponding liveness synchronization skeleton
		//output: a reduced liveness synchronization skeleton
				
		long t0_reduceLiveness = UtilityMethods.timeStamp();
				
		boolean[][][] livenessSynchronization=computeDecentralizedLivenessSynchronization(bdd, localStrategies, regionVars, regionToVarMap, collectiveLivenessGuarantees, synchronizeSignal);
				
		UtilityMethods.duration(t0_reduceLiveness, "livness synchronization reduced in ");
		System.out.println("printing partial synchronization skeleton for liveness");
		FrameSynchronizer.printSynchronizationSkeleton(livenessSynchronization);
//		UtilityMethods.getUserInput();
				
		//**** reinforcing the synchronizations for safety purposes
		int processLengthWithIntermediateStates = 2*processLength;
		int loopingIndexWithIntermediateStates = 2*loopingIndex;
				
		boolean[][][] livenessSynchronizationWithIntermediateStates = new boolean[numOfProcesses][processLengthWithIntermediateStates][numOfProcesses];
		for(int i=0; i<numOfProcesses;i++){
			for(int j=0; j<processLength;j++){
				for(int k=0; k<numOfProcesses; k++){
					livenessSynchronizationWithIntermediateStates[i][2*j][k]= livenessSynchronization[i][j][k];
					livenessSynchronizationWithIntermediateStates[i][2*j+1][k] = false;
				}
			}
		}
				
		//create process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
						
							
		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i+"_");
			Variable[] p_i = Variable.createVariables(bdd, UtilityMethods.numOfBits(processLengthWithIntermediateStates-1), "P"+i+"_");
			processVariables.add(p_i);
		}
				
//		System.out.println("Process variables created!");
//		for(int i=0; i<numOfProcesses;i++){
//			System.out.println("process "+i+" variables");
//			Variable.printVariables(processVariables.get(i));
//		}
//		UtilityMethods.getUserInput();
				
				
				
				
		//TODO: compute the safety objective
		System.out.println("translating the safety objective");
		long t0_safetyTranslate = UtilityMethods.timeStamp();
		int translatedSafetyObjective = computeLocalSafetyObjectiveSymbolic(bdd, regionVars, localStrategies, processVariables, safetyFormula, regionToVarMap, loopingIndex);
		UtilityMethods.duration(t0_safetyTranslate, "safety objective was translated in "); 
				
		System.out.println("All the variables in the system");
		Variable.printVariables(Variable.allVars.toArray(new Variable[Variable.allVars.size()]));
//		UtilityMethods.getUserInput();
				
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "translated safety objective", translatedSafetyObjective);
				
				
				
		//synthesize safety synchronization 
		//input: structure of the processes, a safety objective, and partial synchronization for liveness
		//output: final synchronization skeleton
		long t0_safetySynch = UtilityMethods.timeStamp();
		boolean[][][] synchronizationSkeletonWithIntermediateStates = FrameSynchronizer.computeSynchronizationSkeleton(bdd, numOfProcesses, 
					processLengthWithIntermediateStates, loopingIndexWithIntermediateStates, processVariables, translatedSafetyObjective, livenessSynchronizationWithIntermediateStates);
		UtilityMethods.duration(t0_safetySynch, "safety sychronization computed in ");
		
		System.out.println("\n\n***************************************************");
		System.out.println("printing solution");
		System.out.println("\nLocal strategies are");
		printLocalStrategiesJustNodes(localStrategies);
//		System.out.println("print synchronization skeleton with intermediate states");
//		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeletonWithIntermediateStates);
				
		boolean[][][] synchronizationSkeleton = new boolean[numOfProcesses][processLength][numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<processLength; j++){
				for(int k=0; k<numOfProcesses; k++){
					synchronizationSkeleton[i][j][k] = synchronizationSkeletonWithIntermediateStates[i][2*j][k];
				}
			}
		}
		System.out.println("\nSynchronization skeleton");
		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeleton);
		
		System.out.println("\nLooping index is "+loopingIndex);
//		UtilityMethods.getUserInput();
				
		//TODO: remove the stuttering state 
		
		//create decentralized simulator object
		DecentralizedSwarmSimulator2D simulator = new DecentralizedSwarmSimulator2D(specification.getRegions2D(), localStrategies, 
				synchronizationSkeleton, loopingIndex);
		
		return simulator;
	}
	
	//TODO: check freeing 
	private static ControllableSinglePlayerDeterministicGameStructure reduceCentralSolution(BDD bdd, 
			ControllableSinglePlayerDeterministicGameStructure centralSolution, Variable[] regionVars, 
			Variable synchronizationVariable){
		
		Variable[] synchVarArray = new Variable[]{synchronizationVariable};
		Variable[] vars = Variable.unionVariables(regionVars, synchVarArray);
		int synchronizationCube = BDDWrapper.createCube(bdd, synchVarArray);
		Variable[] counterVars = Variable.difference(centralSolution.variables, vars);
		int counterCube = BDDWrapper.createCube(bdd, counterVars);
		int synchAndCounterCube = BDDWrapper.and(bdd, counterCube, synchronizationCube);
		
		int currentState = bdd.ref(centralSolution.getInit());
		ArrayList<Integer> visited = new ArrayList<Integer>();
		
		ArrayList<Integer> reducedSolution = new ArrayList<Integer>();
		
		boolean newInitComputed = false;
		int newInit = -1;
		
		//walk over the states, if two consecutive states have exactly the same regions
		//if one synchronize while the other one does not, remove the one that does not
		while(!visited.contains(currentState)){
			visited.add(currentState);
			
//			UtilityMethods.debugBDDMethods(bdd, "current state is", currentState);
//			printTransitionRelation(bdd, currentState, regionVars.length);
//			UtilityMethods.getUserInput();
			
			int currentStateRegions = BDDWrapper.exists(bdd, currentState, synchAndCounterCube);
			int nextState = centralSolution.symbolicGameOneStepExecution(currentState);
			
			
			
			int nextStateRegions = BDDWrapper.exists(bdd, nextState, synchAndCounterCube); 
			boolean nextSynched = isSynched(bdd, nextState, synchronizationVariable);
			boolean currentSynched = isSynched(bdd, currentState, synchronizationVariable);
			
//			System.out.println("current state");
//			printTransitionRelation(bdd, currentState, 8);
//			System.out.println("current state regions");
//			printTransitionRelation(bdd, currentStateRegions, 8);
//			System.out.println("synchronize? "+currentSynched);
//			
//			System.out.println("next state ");
//			printTransitionRelation(bdd, nextState, 8);
//			System.out.println("next state regions ");
//			printTransitionRelation(bdd, nextStateRegions, 8);
//			System.out.println("synchronize? "+nextSynched);
//			UtilityMethods.getUserInput();
			
			//if two consecutive states are not over the same regions or if a synchronization happens, add the current state to the final solution
			if(currentStateRegions != nextStateRegions || currentSynched){
				reducedSolution.add(currentState);
				
//				System.out.println("current state added to reduced solution");
//				printTransitionRelation(bdd, currentState, regionVars.length);
//				UtilityMethods.getUserInput();
				
				if(!newInitComputed){
					newInit = currentState;
					newInitComputed = true;
				}
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current state added to reduced solution", currentState);
			}
			
			//determine the next state
			while(currentStateRegions == nextStateRegions && currentSynched && !nextSynched && !visited.contains(nextState)){
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "next state passed ", nextState);
				
				int next = centralSolution.symbolicGameOneStepExecution(nextState);
				BDDWrapper.free(bdd, nextState);
				nextState = next;
				nextStateRegions = BDDWrapper.exists(bdd, nextState, synchAndCounterCube); 
				nextSynched = isSynched(bdd, nextState, synchronizationVariable);
			}
			
			currentState = nextState;
			BDDWrapper.free(bdd, currentStateRegions);
			BDDWrapper.free(bdd, nextStateRegions);
			
			
//			while(true){
//				
//				
//				
//				
//				if(currentStateRegions == nextStateRegions){
//					
//					
//					if(nextSynched && currentSynched){
//						reducedSolution.add(currentState);
//						currentState = nextState;
//						BDDWrapper.free(bdd, currentStateRegions);
//						BDDWrapper.free(bdd, nextStateRegions);
//						break;
//					}else if(!nextSynched && currentSynched){
//						int next = centralSolution.symbolicGameOneStepExecution(nextState);
//						BDDWrapper.free(bdd, nextState);
//						nextState = next;
//					}else{
//						currentState = nextState;
//						BDDWrapper.free(bdd, currentStateRegions);
//						BDDWrapper.free(bdd, nextStateRegions);
//						break;
//					}
//				}else{
//					reducedSolution.add(currentState);
//					//update the current
//					currentState = nextState;
//					BDDWrapper.free(bdd, currentStateRegions);
//					BDDWrapper.free(bdd, nextStateRegions);
//					break;
//				}
//				BDDWrapper.free(bdd, currentStateRegions);
//				BDDWrapper.free(bdd, nextStateRegions);
//			}
			
		}
		
//		System.out.println("current state added to reduced solution");
//		printTransitionRelation(bdd, currentState, regionVars.length);
//		UtilityMethods.getUserInput();
		
		reducedSolution.add(currentState);
		
		int reducedTrans = bdd.ref(bdd.getZero());
		//special case: when there is only a single node
		if(reducedSolution.size()==0){
			int pre = reducedSolution.get(0);
			//add transition from current state to the next state 
			int prePrime = BDDWrapper.replace(bdd, pre, centralSolution.getVtoVprime());
			int trans = BDDWrapper.and(bdd, pre, prePrime);
			reducedTrans = BDDWrapper.orTo(bdd, reducedTrans, trans);
			BDDWrapper.free(bdd, trans);
			BDDWrapper.free(bdd, pre);
		}
		else{
			for(int i=0; i<reducedSolution.size()-1;i++){
				int pre = reducedSolution.get(i);
				int post = reducedSolution.get(i+1);
				
//				System.out.println("pre is");
//				printTransitionRelation(bdd, pre, regionVars.length);
//				
//				System.out.println("post is");
//				printTransitionRelation(bdd, post, regionVars.length);
				
				//add transition from current state to the next state 
				int postPrime = BDDWrapper.replace(bdd, post, centralSolution.getVtoVprime());
				int trans = BDDWrapper.and(bdd, pre, postPrime);
				
//				System.out.println("trans is");
//				printTransitionRelation(bdd, trans, regionVars.length);
//				UtilityMethods.getUserInput();
				
				reducedTrans = BDDWrapper.orTo(bdd, reducedTrans, trans);
				BDDWrapper.free(bdd, trans);
				BDDWrapper.free(bdd, postPrime);
				BDDWrapper.free(bdd, pre);
			}
			BDDWrapper.free(bdd, reducedSolution.get(reducedSolution.size()-1));
		}
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "reduced solution is", reducedTrans);
		
		ControllableSinglePlayerDeterministicGameStructure reducedCentralSolution = new ControllableSinglePlayerDeterministicGameStructure(bdd, 
				centralSolution.variables, 
				reducedTrans, centralSolution.actionVars);
		
		reducedCentralSolution.setInit(newInit);
		
		UtilityMethods.debugBDDMethods(bdd, "new init", newInit);
		
		BDDWrapper.free(bdd, synchronizationCube);
		BDDWrapper.free(bdd, counterCube);
		BDDWrapper.free(bdd, synchAndCounterCube);
		return reducedCentralSolution;
	}
	
	public static void testSafeSwarmNavigation1(){
		SwarmIntermediateSpecification specification = SwarmIntermediateSpecification.createSimpleTest1();
		
		BDD bdd = specification.getBDD();
		DirectedGraph<String, Node, Edge<Node>> regionGraph = specification.regionGraph;
		Variable[] regionVars = specification.getRegionVariables();
		Variable synchronizeSignal = specification.getSynchronizationSignal();
		Variable[] vars = specification.getVariables();
		HashMap<Node, Variable> regionToVarMap = specification.getRegionToVarMap();
		HashMap<Variable, Node> varToRegionMap = specification.getVarToRegionMap();
		int init = specification.getInit();
		ControllableSinglePlayerDeterministicGameStructure swarmGame = specification.getSwarmGame();
		GR1Objective objective = specification.objective;
		int safetyFormula = specification.getSafetyFormula();
		int unsafeStates = specification.getUnsafeStates();
		ArrayList<Integer> collectiveLivenessGuarantees = specification.getCollectiveLivenessGuarantees();
		
		
		
		//Synthesize a centralized LTS satisfying the specification
		//input: initial state, GR1 objective, transition relation based on region graph
		//output: a symbolic solution with partial synchronization
				
		System.out.println("Synthesizing a central solution");
		long t0_centralSolution = UtilityMethods.timeStamp();
				
		ControllableSinglePlayerDeterministicGameStructure centralSolution = computeCentralSolution(bdd, swarmGame, init, objective, regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph);
				
		if(centralSolution == null){
			System.out.println("central solution is null");
		}
				
		UtilityMethods.duration(t0_centralSolution, "central solution computed in ");
		System.out.println("Variables are ");
		Variable.printVariables(centralSolution.variables);
		UtilityMethods.debugBDDMethods(bdd, "the centralized solution ", centralSolution.getSystemTransitionRelation());
//		UtilityMethods.getUserInput();
				
		//partition the centralized solution
		//input: a deterministic symbolic solution with partial synchronization
		//output: a set of processes with their corresponding liveness synchronization skeleton
		//n: the length of central process
				
		long t0_partition = UtilityMethods.timeStamp();
				
		PartitionedCentralLTS pclts =  partitionStrategy(bdd, centralSolution, centralSolution.getInit(), regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph, vars, unsafeStates);
				
		UtilityMethods.duration(t0_partition, "central strategy partitioned in "); 
		System.out.println("partitioning information");
		System.out.println("looping index is "+pclts.getLoopingIndex());
		ArrayList<ArrayList<Node>> partitions =  pclts.getPartitions();
		ArrayList<ArrayList<Edge<Node>>> matchings = pclts.getMatching();
		ArrayList<Boolean> synch = pclts.getSynchronization();
		for(int i=0; i<partitions.size(); i++){
			System.out.println("state "+i +" partitioned into ");
			for(Node n : partitions.get(i)){
				n.printNode();
			}
			System.out.println("matchings");
			for(Edge<Node> e : matchings.get(i)){
				e.printEdge();
			}
			System.out.println(synch.get(i)?"synchronize":"does not synchronize");
		}
		UtilityMethods.getUserInput();
				
		//extract local strategies
		long t0_extract = UtilityMethods.timeStamp();
				
		ArrayList<ArrayList<SynchedNode>> localStrategies =  extractLocalStrategies(pclts.getPartitions(), pclts.getMatching(), pclts.getSynchronization(),pclts.getLoopingIndex());
				
		System.out.println();
		UtilityMethods.duration(t0_extract, "decentralized strategies extracted in ");
		System.out.println("printing decentralized strategies");
		for(int i=0; i<localStrategies.size();i++){
			System.out.println("printing decentralized strategy "+i);
			ArrayList<SynchedNode> localStrat = localStrategies.get(i); 
			for(int j=0; j<localStrat.size(); j++){
				localStrat.get(j).print();
			}
		}
				
				
				
		int numOfProcesses = localStrategies.size();
		//TODO: check if it should be the following multiplied by two to account for intermediate states
		int processLength = localStrategies.get(0).size();
				
		int loopingIndex = pclts.getLoopingIndex();
				
		System.out.println("num of processes "+numOfProcesses);
		System.out.println("process length (without intermediate states) "+processLength);
		System.out.println("looping index "+loopingIndex);
		UtilityMethods.getUserInput();
				
		//reduce liveness synchronization
		//input: a set of processes with their corresponding liveness synchronization skeleton
		//output: a reduced liveness synchronization skeleton
				
		long t0_reduceLiveness = UtilityMethods.timeStamp();
				
		boolean[][][] livenessSynchronization=computeDecentralizedLivenessSynchronization(bdd, localStrategies, regionVars, regionToVarMap, collectiveLivenessGuarantees, synchronizeSignal);
				
		UtilityMethods.duration(t0_reduceLiveness, "livness synchronization reduced in ");
		System.out.println("printing partial synchronization skeleton for liveness");
		FrameSynchronizer.printSynchronizationSkeleton(livenessSynchronization);
		UtilityMethods.getUserInput();
				
		//**** reinforcing the synchronizations for safety purposes
		int processLengthWithIntermediateStates = 2*processLength;
		int loopingIndexWithIntermediateStates = 2*loopingIndex;
				
		boolean[][][] livenessSynchronizationWithIntermediateStates = new boolean[numOfProcesses][processLengthWithIntermediateStates][numOfProcesses];
		for(int i=0; i<numOfProcesses;i++){
			for(int j=0; j<processLength;j++){
				for(int k=0; k<numOfProcesses; k++){
					livenessSynchronizationWithIntermediateStates[i][2*j][k]= livenessSynchronization[i][j][k];
					livenessSynchronizationWithIntermediateStates[i][2*j+1][k] = false;
				}
			}
		}
				
		//create process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
						
							
		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i+"_");
			Variable[] p_i = Variable.createVariables(bdd, UtilityMethods.numOfBits(processLengthWithIntermediateStates-1), "P"+i+"_");
			processVariables.add(p_i);
		}
				
		System.out.println("Process variables created!");
		for(int i=0; i<numOfProcesses;i++){
			System.out.println("process "+i+" variables");
			Variable.printVariables(processVariables.get(i));
		}
		UtilityMethods.getUserInput();
				
				
				
				
		//TODO: compute the safety objective
		System.out.println("translating the safety objective");
		long t0_safetyTranslate = UtilityMethods.timeStamp();
		int translatedSafetyObjective = computeLocalSafetyObjectiveSymbolic(bdd, regionVars, localStrategies, processVariables, safetyFormula, regionToVarMap, loopingIndex);
		UtilityMethods.duration(t0_safetyTranslate, "safety objective was translated in "); 
				
		System.out.println("All the variables in the system");
		Variable.printVariables(Variable.allVars.toArray(new Variable[Variable.allVars.size()]));
		UtilityMethods.getUserInput();
				
		UtilityMethods.debugBDDMethodsAndWait(bdd, "translated safety objective", translatedSafetyObjective);
				
				
				
		//synthesize safety synchronization 
		//input: structure of the processes, a safety objective, and partial synchronization for liveness
		//output: final synchronization skeleton
		boolean[][][] synchronizationSkeletonWithIntermediateStates = FrameSynchronizer.computeSynchronizationSkeleton(bdd, numOfProcesses, 
					processLengthWithIntermediateStates, loopingIndexWithIntermediateStates, processVariables, translatedSafetyObjective, livenessSynchronizationWithIntermediateStates);
				
		printLocalStrategies(localStrategies);
		System.out.println("print synchronization skeleton with intermediate states");
		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeletonWithIntermediateStates);
				
		boolean[][][] synchronizationSkeleton = new boolean[numOfProcesses][processLength][numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<processLength; j++){
				for(int k=0; k<numOfProcesses; k++){
					synchronizationSkeleton[i][j][k] = synchronizationSkeletonWithIntermediateStates[i][2*j][k];
				}
			}
		}
		System.out.println("final synchronization skeleton");
		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeleton);
				
		//TODO: remove the stuttering state 
	}
	
	public static void testDecentralizedLabledTransitionRelations1(){
		int size = 2; 
		
		int h = 2;
		int w = 3;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
			}
		}
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		
		//Define variables 
		//define regions
		Variable[][] regions = new Variable[w][h];
		Variable[] regionVars = new Variable[w*h];
		ArrayList<Variable> variables = new ArrayList<Variable>();
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regions[i][j] = Variable.createVariableAndPrimedVariable(bdd, "region["+i+"]["+j+"]");
				variables.add(regions[i][j]);
				regionVars[i*h+j] = regions[i][j];
				
				regionToVarMap.put(regionNodes[i][j], regions[i][j]);
				varToRegionMap.put(regions[i][j], regionNodes[i][j]);
			}
		}
		
		
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
		
		Variable[] vars = UtilityMethods.variableArrayListToArray(variables); 
		
		//Create transition relation for swarm movement
//		int trans = createSwarmTransitionRelationSquareWorkSpace(bdd, size, regions, regionNodes, regionGraph);
		
		regionGraph = createSimpleGraph(regionNodes);
		int trans = directedGraphToTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		printTransitionRelation(bdd, trans, regionNodes.length*regionNodes[0].length);
		UtilityMethods.getUserInput();
		
		
		
		//Create a ControllableSinglePlayerDeterministicTransitionRelation
		ControllableSinglePlayerDeterministicGameStructure swarmGame = new ControllableSinglePlayerDeterministicGameStructure(bdd, vars, trans, null);
				
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		
		//always eventually part of swarm in (0,size-1)
		//region[0][size-11] must be visited
//		int region_0_1_partial = bdd.ref(regions[0][h-1].getBDDVar());
		
		
		//region[size-1][0] must be visited
//		int region_1_0_partial = bdd.ref(regions[w-1][0].getBDDVar());
		
//		int region_1_1_partial = bdd.ref(regions[1][1].getBDDVar());
//		
//		int region_2_1_partial = bdd.ref(regions[2][1].getBDDVar());
		
		int collectiveLiveness1 = BDDWrapper.and(bdd, regions[0][0].getBDDVar(), regions[1][0].getBDDVar());
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, regions[2][0].getBDDVar());
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		
		int collectiveLiveness = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
		collectiveLiveness = BDDWrapper.andTo(bdd, collectiveLiveness, regions[2][1].getBDDVar());
		collectiveLiveness = BDDWrapper.andTo(bdd, collectiveLiveness, synchronizeSignal.getBDDVar());
		
		//region[size-1][size-1] must be full always eventually
//		int region_1_1_full = createFullRegionPredicate(bdd, regions, size-1, size-1);
//		region_1_1_full = BDDWrapper.andTo(bdd, region_1_1_full, synchronizeSignal.getBDDVar());
		
//		collectiveLivenessGuarantees.add(region_1_0_partial);
//		collectiveLivenessGuarantees.add(region_0_1_partial);
		
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		collectiveLivenessGuarantees.add(collectiveLiveness);
		
//		collectiveLivenessGuarantees.add(region_1_1_full);
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		livenessGuarantees.addAll(individualLivenessGuarantees);
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never region[1][0] and region[0][1] be occupied at the same time 
//		int unsafe = BDDWrapper.and(bdd, region_0_1_partial, region_1_0_partial);
		
		int unsafe = BDDWrapper.and(bdd, regions[0][0].getBDDVar(), regions[0][1].getBDDVar());
		int tmp2 = BDDWrapper.and(bdd, regions[1][0].getBDDVar(), regions[1][1].getBDDVar());
		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
		BDDWrapper.free(bdd, tmp2);
		BDDWrapper.free(bdd, tmp3);
		
		int safetyObj = BDDWrapper.not(bdd, unsafe);
		BDDWrapper.free(bdd, unsafe);
		safetyGuarantees.add(safetyObj);
		
		ArrayList<Integer> livenessAssumptions = new ArrayList<Integer>();
		livenessAssumptions.add(bdd.ref(bdd.getOne()));
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), livenessAssumptions, livenessGuarantees, null, safetyGuarantees);
		
		//define initial state
		//initially full swarm in regions[0][0]
//		int region_0_0_full = createFullRegionPredicate(bdd, regions, 0, 0);
//		int init = BDDWrapper.and(bdd, region_0_0_full, bdd.not(synchronizeSignal.getBDDVar()));
		
		int region_0_0_partial = bdd.ref(regions[0][0].getBDDVar());
		int region_0_1_empty = BDDWrapper.not(bdd, regions[0][1].getBDDVar());
		int region_1_1_empty = BDDWrapper.not(bdd, regions[1][1].getBDDVar());
		int region_2_1_empty = BDDWrapper.not(bdd, regions[2][1].getBDDVar());
		
		int init = BDDWrapper.and(bdd,region_0_0_partial, regions[1][0].getBDDVar());
		init = BDDWrapper.andTo(bdd, init, regions[2][0].getBDDVar());
		init = BDDWrapper.andTo(bdd, init, region_0_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_1_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_2_1_empty);
		init = BDDWrapper.andTo(bdd,init,bdd.not(synchronizeSignal.getBDDVar()));
		BDDWrapper.free(bdd, region_0_0_partial);
		BDDWrapper.free(bdd, region_0_1_empty);
		BDDWrapper.free(bdd, region_1_1_empty);
		BDDWrapper.free(bdd, region_2_1_empty);
				
		
		
		
		swarmGame.setInit(init);
		
		System.out.println("Checking the input");
		System.out.println("Printing the variables");
		Variable.printVariables(vars);
		
		System.out.println("objectives are ");
		System.out.println("individual liveness");
		for(int i=0; i<individualLivenessGuarantees.size();i++){
			UtilityMethods.debugBDDMethods(bdd, "liveness guarantee "+i, individualLivenessGuarantees.get(i));
		}
		
		System.out.println("collective liveness");
		for(int i=0; i<collectiveLivenessGuarantees.size();i++){
			UtilityMethods.debugBDDMethods(bdd, "liveness guarantee "+i, collectiveLivenessGuarantees.get(i));
		}
		
		System.out.println("safety objectives ");
		for(int i=0; i<safetyGuarantees.size();i++){
			UtilityMethods.debugBDDMethods(bdd, "safety guarantee "+i, safetyGuarantees.get(i));
		}
		
		UtilityMethods.debugBDDMethods(bdd, "init", init);
		
		System.out.println("End of input");
		UtilityMethods.getUserInput();
		
		/**** END OF INPUT *****/
		
		//unsafe states 
		int safetyFormula = bdd.ref(bdd.getOne());
		for(int i=0; i<safetyGuarantees.size();i++){
			safetyFormula = BDDWrapper.andTo(bdd, safetyFormula, safetyGuarantees.get(i));
		}
		int unsafeStates = BDDWrapper.not(bdd, safetyFormula);
		
		
		//Synthesize a centralized LTS satisfying the specification
		//input: initial state, GR1 objective, transition relation based on region graph
		//output: a symbolic solution with partial synchronization
		
		System.out.println("Synthesizing a central solution");
		long t0_centralSolution = UtilityMethods.timeStamp();
		
		ControllableSinglePlayerDeterministicGameStructure centralSolution = computeCentralSolution(bdd, swarmGame, init, objective, regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph);
		
		if(centralSolution == null){
			System.out.println("central solution is null");
		}
		
		UtilityMethods.duration(t0_centralSolution, "central solution computed in ");
		System.out.println("Variables are ");
		Variable.printVariables(centralSolution.variables);
		UtilityMethods.debugBDDMethods(bdd, "the centralized solution ", centralSolution.getSystemTransitionRelation());
//		UtilityMethods.getUserInput();
		
		//partition the centralized solution
		//input: a deterministic symbolic solution with partial synchronization
		//output: a set of processes with their corresponding liveness synchronization skeleton
		//n: the length of central process
		
		long t0_partition = UtilityMethods.timeStamp();
		
		PartitionedCentralLTS pclts =  partitionStrategy(bdd, centralSolution, centralSolution.getInit(), regionVars, synchronizeSignal, varToRegionMap, regionToVarMap, regionGraph, vars, unsafeStates);
		
		UtilityMethods.duration(t0_partition, "central strategy partitioned in "); 
		System.out.println("partitioning information");
		System.out.println("looping index is "+pclts.getLoopingIndex());
		ArrayList<ArrayList<Node>> partitions =  pclts.getPartitions();
		ArrayList<ArrayList<Edge<Node>>> matchings = pclts.getMatching();
		ArrayList<Boolean> synch = pclts.getSynchronization();
		for(int i=0; i<partitions.size(); i++){
			System.out.println("state "+i +" partitioned into ");
			for(Node n : partitions.get(i)){
				n.printNode();
			}
			System.out.println("matchings");
			for(Edge<Node> e : matchings.get(i)){
				e.printEdge();
			}
			System.out.println(synch.get(i)?"synchronize":"does not synchronize");
		}
		UtilityMethods.getUserInput();
		
		//extract local strategies
		long t0_extract = UtilityMethods.timeStamp();
		
		ArrayList<ArrayList<SynchedNode>> localStrategies =  extractLocalStrategies(pclts.getPartitions(), pclts.getMatching(), pclts.getSynchronization(),pclts.getLoopingIndex());
		
		System.out.println();
		UtilityMethods.duration(t0_extract, "decentralized strategies extracted in ");
		System.out.println("printing decentralized strategies");
		for(int i=0; i<localStrategies.size();i++){
			System.out.println("printing decentralized strategy "+i);
			ArrayList<SynchedNode> localStrat = localStrategies.get(i); 
			for(int j=0; j<localStrat.size(); j++){
				localStrat.get(j).print();
			}
		}
		
		
		
		int numOfProcesses = localStrategies.size();
		//TODO: check if it should be the following multiplied by two to account for intermediate states
		int processLength = localStrategies.get(0).size();
		
		int loopingIndex = pclts.getLoopingIndex();
		
		System.out.println("num of processes "+numOfProcesses);
		System.out.println("process length (without intermediate states) "+processLength);
		System.out.println("looping index "+loopingIndex);
		UtilityMethods.getUserInput();
		
		//reduce liveness synchronization
		//input: a set of processes with their corresponding liveness synchronization skeleton
		//output: a reduced liveness synchronization skeleton
		
		long t0_reduceLiveness = UtilityMethods.timeStamp();
		
		boolean[][][] livenessSynchronization=computeDecentralizedLivenessSynchronization(bdd, localStrategies, regionVars, regionToVarMap, collectiveLivenessGuarantees, synchronizeSignal);
		
		UtilityMethods.duration(t0_reduceLiveness, "livness synchronization reduced in ");
		System.out.println("printing partial synchronization skeleton for liveness");
		FrameSynchronizer.printSynchronizationSkeleton(livenessSynchronization);
		UtilityMethods.getUserInput();
		
		//**** reinforcing the synchronizations for safety purposes
		int processLengthWithIntermediateStates = 2*processLength;
		int loopingIndexWithIntermediateStates = 2*loopingIndex;
		
		boolean[][][] livenessSynchronizationWithIntermediateStates = new boolean[numOfProcesses][processLengthWithIntermediateStates][numOfProcesses];
		for(int i=0; i<numOfProcesses;i++){
			for(int j=0; j<processLength;j++){
				for(int k=0; k<numOfProcesses; k++){
					livenessSynchronizationWithIntermediateStates[i][2*j][k]= livenessSynchronization[i][j][k];
					livenessSynchronizationWithIntermediateStates[i][2*j+1][k] = false;
				}
			}
		}
		
		//create process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
				
					
		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i+"_");
			Variable[] p_i = Variable.createVariables(bdd, UtilityMethods.numOfBits(processLengthWithIntermediateStates-1), "P"+i+"_");
			processVariables.add(p_i);
		}
		
		System.out.println("Process variables created!");
		for(int i=0; i<numOfProcesses;i++){
			System.out.println("process "+i+" variables");
			Variable.printVariables(processVariables.get(i));
		}
		UtilityMethods.getUserInput();
		
		
		
		
		//TODO: compute the safety objective
		System.out.println("translating the safety objective");
		long t0_safetyTranslate = UtilityMethods.timeStamp();
		int translatedSafetyObjective = computeLocalSafetyObjectiveSymbolic(bdd, regionVars, localStrategies, processVariables, safetyFormula, regionToVarMap, loopingIndex);
		UtilityMethods.duration(t0_safetyTranslate, "safety objective was translated in "); 
		
		System.out.println("All the variables in the system");
		Variable.printVariables(Variable.allVars.toArray(new Variable[Variable.allVars.size()]));
		UtilityMethods.getUserInput();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "translated safety objective", translatedSafetyObjective);
		
		
		
		//synthesize safety synchronization 
		//input: structure of the processes, a safety objective, and partial synchronization for liveness
		//output: final synchronization skeleton
		boolean[][][] synchronizationSkeletonWithIntermediateStates = FrameSynchronizer.computeSynchronizationSkeleton(bdd, numOfProcesses, 
				processLengthWithIntermediateStates, loopingIndexWithIntermediateStates, processVariables, translatedSafetyObjective, livenessSynchronizationWithIntermediateStates);
		
		printLocalStrategies(localStrategies);
		System.out.println("print synchronization skeleton with intermediate states");
		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeletonWithIntermediateStates);
		
		boolean[][][] synchronizationSkeleton = new boolean[numOfProcesses][processLength][numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<processLength; j++){
				for(int k=0; k<numOfProcesses; k++){
					synchronizationSkeleton[i][j][k] = synchronizationSkeletonWithIntermediateStates[i][2*j][k];
				}
			}
		}
		System.out.println("final synchronization skeleton");
		FrameSynchronizer.printSynchronizationSkeleton(synchronizationSkeleton);
		
		//TODO: remove the stuttering state 
		
	}
	
	private static void printLocalStrategies(ArrayList<ArrayList<SynchedNode>> localStrategies){
		System.out.println("printing local strategies");
		for(int i=0; i<localStrategies.size();i++){
			System.out.println("printing decentralized strategy "+i);
			ArrayList<SynchedNode> localStrat = localStrategies.get(i); 
			for(int j=0; j<localStrat.size(); j++){
				localStrat.get(j).print();
			}
		}
	}
	
	private static void printLocalStrategiesJustNodes(ArrayList<ArrayList<SynchedNode>> localStrategies){
		System.out.println("printing local strategies");
		for(int i=0; i<localStrategies.size();i++){
			System.out.println("printing decentralized strategy "+i);
			ArrayList<SynchedNode> localStrat = localStrategies.get(i); 
			for(int j=0; j<localStrat.size(); j++){
				localStrat.get(j).getNode().printNode();
			}
		}
	}
	
	/**
	 * Translates the global safety objective to the combination of states of local strategies
	 * @return
	 */
	private static int computeLocalSafetyObjectiveSymbolic(BDD bdd, Variable[] regionVars, ArrayList<ArrayList<SynchedNode>> localStrategies, 
			ArrayList<Variable[]> processVariables, int safetyObjective, HashMap<Node, Variable> regionToVarMap, int loopingIndex){
		int numOfProcesses = localStrategies.size();
		//define local region variables for each process
		Variable[][] localRegionVars = new Variable[regionVars.length][numOfProcesses];
		for(int i=0; i<regionVars.length; i++){
			for(int j=0; j<numOfProcesses; j++){
				localRegionVars[i][j] = new Variable(bdd, regionVars[i].getName()+"_"+j);
			}
		}
		
		//if A is a region and A^1, A^2, .. are local copies, define the predicate A <-> A^1 \vee A^2 \vee .. to encode equality
//		int[] regionEqualityPredicates = new int[regionVars.length];
		int regionEqualityPredicate = bdd.ref(bdd.getOne());
		for(int i=0; i<regionVars.length; i++){
			int orLocalRegions = bdd.ref(bdd.getZero());
			for(int j=0; j<localRegionVars[i].length;j++){
				orLocalRegions = BDDWrapper.orTo(bdd, orLocalRegions, localRegionVars[i][j].getBDDVar());
			}
			int regionEqualityPredicates_i = BDDWrapper.biimp(bdd, regionVars[i].getBDDVar(), orLocalRegions);
			regionEqualityPredicate = BDDWrapper.andTo(bdd, regionEqualityPredicate, regionEqualityPredicates_i);
			BDDWrapper.free(bdd, orLocalRegions);
			BDDWrapper.free(bdd, regionEqualityPredicates_i);
			
		}
		
		//replace local region vars in safety formula 
		//safety & regionEquality predicates
		int tmp = BDDWrapper.and(bdd, safetyObjective, regionEqualityPredicate);
		//existentially quantify region vars
		int regionsCube = BDDWrapper.createCube(bdd, regionVars);
		int localSafety = BDDWrapper.exists(bdd, tmp, regionsCube);
		BDDWrapper.free(bdd, regionsCube);
		
		//translate local strategies to formulas
		//Example: q_0,A - q_01,A,B - q_1,B --> (q_0 & A^1 & Not B^1) | (q_01 & A^1 & B^1) | (q_1 & NOT A^1 & B^1)
		int localStrategiesFormula = bdd.ref(bdd.getOne());
		for(int i=0; i<localStrategies.size();i++){
			Variable[] processLocalRegionVars = new Variable[regionVars.length];
			for(int j=0; j<regionVars.length; j++){
				processLocalRegionVars[j]=localRegionVars[j][i];
			}
			int locStrat = localStrategyFormula(bdd, processVariables.get(i), localStrategies.get(i), regionToVarMap, regionVars, processLocalRegionVars, loopingIndex);
			
			//debugging 
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "process "+i+" is translated into a logical formula ", locStrat);
			
			localStrategiesFormula = BDDWrapper.andTo(bdd, localStrategiesFormula, locStrat);
			BDDWrapper.free(bdd, locStrat);
		}
		
		//Conjoin local strategy formulas with local safety objective 
		int safeStates = BDDWrapper.and(bdd,localStrategiesFormula, localSafety);
		BDDWrapper.free(bdd, localSafety);
		BDDWrapper.free(bdd, localStrategiesFormula);
		
		//existentially quantify local region vars
		int localRegionCube = bdd.ref(bdd.getOne());
		for(int i=0; i<localRegionVars.length; i++){
			int locCube = BDDWrapper.createCube(bdd, localRegionVars[i]);
			localRegionCube = BDDWrapper.andTo(bdd, localRegionCube, locCube);
			BDDWrapper.free(bdd, locCube);
		}
		int safeCombinations = BDDWrapper.exists(bdd, safeStates, localRegionCube);
		BDDWrapper.free(bdd, safeStates);
		BDDWrapper.free(bdd, localRegionCube);
		
		return safeCombinations;
		
	}
	
	//translate local strategies to formulas
	//Example: q_0,A - q_01,A,B - q_1,B --> (q_0 & A^1 & Not B^1) | (q_01 & A^1 & B^1) | (q_1 & NOT A^1 & B^1)
	private static int localStrategyFormula(BDD bdd, Variable[] processVariables, ArrayList<SynchedNode> localStrategy, HashMap<Node, Variable> regionToVarMap,
			Variable[] regionVars, Variable[] processLocalRegionVars, int loopingIndex){
		
		int locStratFormula = bdd.ref(bdd.getZero());
		for(int i=0; i<localStrategy.size(); i++){
			//state q_i
			int q_i = BDDWrapper.assign(bdd, 2*i, processVariables);
			Node region = localStrategy.get(i).node;
			Variable regionVar = regionToVarMap.get(region);
			int index = indexOf(regionVars,regionVar);
			Variable localRegionVar = processLocalRegionVars[index];
			
			Variable[] involvedLocalVars = new Variable[]{localRegionVar};
			Variable[] notInvolved = Variable.difference(processLocalRegionVars, involvedLocalVars);
			
			int localRegionFormula = translateToRegionFormula(bdd, involvedLocalVars, notInvolved);
			int localStateFormula = BDDWrapper.and(bdd, q_i, localRegionFormula);
			BDDWrapper.free(bdd, q_i);
			BDDWrapper.free(bdd,localRegionFormula);
			locStratFormula = BDDWrapper.orTo(bdd, locStratFormula, localStateFormula);
			BDDWrapper.free(bdd, localStateFormula);
			
			//intermediate state q_i,i+1
			int q_i_Int = BDDWrapper.assign(bdd, 2*i+1, processVariables);
			int next = i<localStrategy.size()-1?i+1:loopingIndex;
			Node nextRegion = localStrategy.get(next).node;
			Variable nextRegionVar = regionToVarMap.get(nextRegion);
			int nextIndex = indexOf(regionVars, nextRegionVar);
			Variable nextLocalRegionVar = processLocalRegionVars[nextIndex];
			
			Variable[] nextInvolved = new Variable[]{localRegionVar,nextLocalRegionVar};
			Variable[] nextNotInvolved = Variable.difference(processLocalRegionVars, nextInvolved);
			
			int intermediateRegionFormula = translateToRegionFormula(bdd, nextInvolved, nextNotInvolved);
			int intermediateLocalStateFormula = BDDWrapper.and(bdd, q_i_Int, intermediateRegionFormula); 
			BDDWrapper.free(bdd, q_i_Int);
			BDDWrapper.free(bdd, intermediateRegionFormula);
			locStratFormula = BDDWrapper.orTo(bdd, locStratFormula, intermediateLocalStateFormula);
			BDDWrapper.free(bdd, intermediateLocalStateFormula);
			
		}
		
		return locStratFormula;
	}
	
	private static int indexOf(Variable[] regionVars, Variable region){
		for(int i=0; i<regionVars.length; i++){
			if(regionVars[i]==region){
				return i;
			}
		}
		return -1;
	}
	
	private static int createSwarmTransitionRelationSquareWorkSpace(BDD bdd, int size, Variable[][] regions, Node[][] regionNodes, DirectedGraph<String, Node, Edge<Node>> regionGraph){
		int trans = bdd.ref(bdd.getOne());
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				int currentCellFormula = bdd.ref(bdd.not(regions[i][j].getBDDVar()));
				
				
				//stutter
				Variable primeCurrentCell = regions[i][j].getPrimedCopy();
				currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeCurrentCell.getBDDVar());
				
				int currentCellPrimeFormula = bdd.ref(bdd.not(primeCurrentCell.getBDDVar()));
				currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j].getBDDVar());
				
				Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i][j]);
				regionGraph.addEdge(e);
				
				//neighbors
				if(i-1>=0){
					Variable primeNeighbor = regions[i-1][j].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i-1][j].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i-1][j]);
					regionGraph.addEdge(e2);
				}
				if(i+1<size){
					Variable primeNeighbor = regions[i+1][j].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i+1][j].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i+1][j]);
					regionGraph.addEdge(e2);
				}
				if(j-1>=0){
					Variable primeNeighbor = regions[i][j-1].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j-1].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j-1]);
					regionGraph.addEdge(e2);
				}
				if(j+1<size){
					Variable primeNeighbor = regions[i][j+1].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j+1].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j+1]);
					regionGraph.addEdge(e2);
				}
				trans = BDDWrapper.andTo(bdd, trans, currentCellFormula);
				trans = BDDWrapper.andTo(bdd, trans, currentCellPrimeFormula);
				BDDWrapper.free(bdd, currentCellFormula);
				BDDWrapper.free(bdd, currentCellPrimeFormula);
			}
		}
		
		int atLeastOneNonEmptyCell = bdd.getZero();
		for(int i=0; i<regions.length; i++){
			for(int j=0; j<regions[i].length;j++){
				atLeastOneNonEmptyCell = BDDWrapper.orTo(bdd, atLeastOneNonEmptyCell, regions[i][j].getBDDVar());
			}
		}
		trans = BDDWrapper.andTo(bdd, trans , atLeastOneNonEmptyCell);
		BDDWrapper.free(bdd, atLeastOneNonEmptyCell);
		return trans;
	}
	
	//TODO: implement the general partitioning algorithm, right now it does not cover some special cases 
	//it should be a recursive algorithm that traverses the strategy tree
	//TODO: what if the strategy has only a single node
	public static ArrayList<ArrayList<SynchedNode>> extractLocalStrategies(ArrayList<ArrayList<Node>> partitions, ArrayList<ArrayList<Edge<Node>>> matches, ArrayList<Boolean> sync, int loopingIndex){
		ArrayList<ArrayList<SynchedNode>> localStrategies = new ArrayList<ArrayList<SynchedNode>>(); 
		
		ArrayList<Node> initial = partitions.get(0);
//		boolean initSync = sync.get(0);
		for(int i=0; i<initial.size(); i++){
//			SynchedNode currentNode = new SynchedNode(initial.get(i), initSync);
			ArrayList<ArrayList<SynchedNode>> localStrategiesStartedWithCurrentNode = extractLocalStrategies(initial.get(i), 0, matches, sync, loopingIndex);
//			for(ArrayList<SynchedNode> localStrat : localStrategiesStartedWithCurrentNode){
//				localStrat.add(0, currentNode);
//			}
			localStrategies.addAll(localStrategiesStartedWithCurrentNode);
		}
		
		//postprocess: some of the local programs may be redundant, remove them 
		System.out.println("Removing redundant local strategies");
		localStrategies = removeRedundantLocalStrategies(matches, localStrategies, loopingIndex);
		
		return localStrategies;
	}
	
	/**
	 * Removes redundant local strategies. 
	 * A local strategy is redundant if the specification will be still satisfied after removing it
	 * @param matches
	 * @param localStrategies
	 * @return
	 */
	private static ArrayList<ArrayList<SynchedNode>> removeRedundantLocalStrategies(ArrayList<ArrayList<Edge<Node>>> matches, ArrayList<ArrayList<SynchedNode>> localStrategies, int loopingIndex){
		ArrayList<ArrayList<SynchedNode>> finalLocalStrategies = new ArrayList<ArrayList<SynchedNode>>();
		
		ArrayList<ArrayList<Integer>> sets = new ArrayList<ArrayList<Integer>>();
		
		//the set of all local strategies indices
		ArrayList<Integer> universalSet = new ArrayList<Integer>();
		for(int i=0; i<localStrategies.size();i++){
			universalSet.add(i);
		}
		
		//go over matches one by one
		for(int i =0; i<matches.size(); i++){
			ArrayList<Edge<Node>> nodeMatches = matches.get(i);
			for(int j=0; j<nodeMatches.size();j++){
				Edge<Node> e = nodeMatches.get(j);
				Node source = e.getFrom();
				Node target = e.getTarget();
				
//				System.out.println("current edge is");
//				e.printEdge();
				
				ArrayList<Integer> localStrategiesIncludingEdge = new ArrayList<Integer>();
				//for each edge compute the set of local strategies that include it
				for(int k=0; k<localStrategies.size();k++){
					ArrayList<SynchedNode> localStrat = localStrategies.get(k);
					SynchedNode localStratSource = localStrat.get(i);
					int next = i+1;
					if(next==matches.size()){
						next = loopingIndex;
					}
					SynchedNode localStratTarget = localStrat.get(next);
					//the local strategy contains a match if it contains its source and destination
					if(localStratSource.getNode() == source && localStratTarget.getNode() == target){
						//local strategy contains the match, add its index to the set
						localStrategiesIncludingEdge.add(k);
						
//						System.out.println("local strategy "+k+" contains it");
					}
				}
				sets.add(localStrategiesIncludingEdge);
			}
		}
		
		
//		System.out.println("Computing hitting sets");
		
		//compute a minimal hitting set covering all the sets
		//these are the minimal subset of local strategies that cover all the edges
		ArrayList<Integer> hittingSet= hittingSet(universalSet, sets);
		
		//keep the local strategies with the indices
		for(Integer i : hittingSet){
			finalLocalStrategies.add(localStrategies.get(i));
		}
		
		return finalLocalStrategies;
	}
	
	private static ArrayList<Integer> hittingSet(ArrayList<Integer> universal, ArrayList<ArrayList<Integer>> sets){
		
		//remove the supersets
		ArrayList<ArrayList<Integer>> newSets = removeSuperSets(sets);
		
//		System.out.println("super sets removed ");
//		for(ArrayList<Integer> set : newSets){
//			UtilityMethods.printArrayList(set);
//			System.out.println();
//		}
		
		//greedy algorithm
		//Each time remove one element, check if the remaining still covers all the sets
		ArrayList<Integer> hittingSet = UtilityMethods.copyArrayList(universal);
		for(int i=0; i<universal.size(); i++){
			hittingSet.remove(universal.get(i));
			if(!intersectionNonEmpty(hittingSet, newSets)){
				hittingSet.add(universal.get(i));
			}
		}
		
//		System.out.println("hitting set computed");
//		UtilityMethods.printArrayList(hittingSet);
//		UtilityMethods.getUserInput();
		
		return hittingSet; 
		
	}
	
	private static boolean intersectionNonEmpty(ArrayList<Integer> hittingSet, ArrayList<ArrayList<Integer>> sets){
		for(ArrayList<Integer> set : sets){
			if(intersectionEmpty(hittingSet, set)){
				return false;
			}
		}
		return true;
	}
	
	private static boolean intersectionEmpty(ArrayList<Integer> set1, ArrayList<Integer> set2){
		for(Integer i : set1){
			if(set2.contains(i)){
				return false;
			}
		}
		return true;
	}
	
	private static ArrayList<ArrayList<Integer>> removeSuperSets(ArrayList<ArrayList<Integer>> sets){
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sets.size(); i++){
			boolean superset = false; 
			for(int j=0; j<sets.size(); j++){
				if(i==j) continue;
				if(subset(sets.get(j),sets.get(i))){
					if(subset(sets.get(i),sets.get(j)) && j>i){
						continue;
					}
					superset = true;
					break;
				}
			}
			if(!superset){
				result.add(sets.get(i));
			}
		}
		return result;
	}
	
	/**
	 * Returns true if set1 is subset of or equal to set2
	 * @param set1
	 * @param set2
	 * @return
	 */
	private static boolean subset(ArrayList<Integer> set1, ArrayList<Integer> set2){
		for(Integer e : set1){
			if(!set2.contains(e)){
				return false;
			}
		}
		return true;
	}
	
	//TODO: implement the general case where the ending point of central strategy can match to different nodes in the looping point and local programs can mix up, 
	//right now we assume that the decentralized strategies preserve the structure of the centralized one and final nodes match the source of their local program 
	public static ArrayList<ArrayList<SynchedNode>> extractLocalStrategies(Node currentNode, int currentIndex, ArrayList<ArrayList<Edge<Node>>> matches, 
			ArrayList<Boolean> sync, int loopingIndex){
		
		ArrayList<ArrayList<SynchedNode>> localStrategies = new ArrayList<ArrayList<SynchedNode>>();
		
		int endOfLoop = matches.size()-1;
		
//		System.out.println("looping index is "+loopingIndex);
		
		if(currentIndex == matches.size()-1){
			ArrayList<SynchedNode> localStrat =  new ArrayList<SynchedNode>();
			boolean syncValue = sync.get(currentIndex);
			localStrat.add(new SynchedNode(currentNode, syncValue));
			localStrategies.add(localStrat);
			return localStrategies;
		}
		
		ArrayList<Edge<Node>> edges = matches.get(currentIndex);
		
//		if(nextIndex == matches.size()){
//			for(Edge<Node> e : edges){
//				Node source = e.getFrom();
//				if(source.equals(currentNode)){
//					Node nextNode = e.getTarget();
//					boolean syncValue = sync.get(nextIndex);
//					SynchedNode nextSynchedNode = new SynchedNode(nextNode, syncValue);
//					ArrayList<SynchedNode> localStrat =  new ArrayList<SynchedNode>();
//					localStrat.add(nextSynchedNode);
//					localStrategies.add(localStrat);
//				}
//			}
//			return localStrategies;
//		}
		
		SynchedNode currentSynchedNode = new SynchedNode(currentNode, sync.get(currentIndex));
		
		for(Edge<Node> e : edges){
			Node source = e.getFrom();
			if(source.equals(currentNode)){
				Node nextNode = e.getTarget();
				ArrayList<ArrayList<SynchedNode>> localStrategiesStartedWithCurrentNode = extractLocalStrategies(nextNode, currentIndex+1, matches, sync, loopingIndex);
				for(ArrayList<SynchedNode> localStrat : localStrategiesStartedWithCurrentNode){
//					if(currentIndex == loopingIndex){
//						//check if the last node of the local strategy is connected to the current node
//						SynchedNode lastSynchedNode = localStrat.get(localStrat.size()-1);
//						ArrayList<Edge<Node>> lastNodeMatches = matches.get(endOfLoop);
//						for(Edge )
//					}else{
					
//					}
					
					if(currentIndex != loopingIndex){
						localStrat.add(0, currentSynchedNode);
						localStrategies.add(localStrat);
					}else{
						
						
						
						SynchedNode lastSynchedNode = localStrat.get(localStrat.size()-1);
						Node last = lastSynchedNode.getNode();
						
						
						
						ArrayList<Edge<Node>> lastNodeMatches = matches.get(endOfLoop);
						//only if the last node has a matching to the current node, add the local strategy starting with this node
						for(Edge<Node> e2 : lastNodeMatches){
							if(e2.getFrom() == last && e2.getTarget()==currentNode){
//								System.out.println("current node is ");
//								currentNode.printNode();
//								System.out.println("last node is ");
//								last.printNode();
								localStrat.add(0, currentSynchedNode);
								localStrategies.add(localStrat);
								break;
							}
						}
					}
				}
//				localStrategies.addAll(localStrategiesStartedWithCurrentNode);
			}
		}
		
		return localStrategies;
	}
	
	//TODO: local strategies can be marked on at each position which collective liveness guarantees they fulfill --> future work
	//Here we assume that collective liveness guarantees are disjoint
	private static boolean[][][] computeDecentralizedLivenessSynchronization(BDD bdd, ArrayList<ArrayList<SynchedNode>> localStrategies, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap, ArrayList<Integer> collectiveLivenessGuarantees, Variable synchronizationVariable){
		int numOfProcesses = localStrategies.size();
		//TODO: check if it should be the following multiplied by two to account for intermediate states
		int processLength = localStrategies.get(0).size();
		boolean[][][] livenessSynchronizationSkeleton = new boolean[numOfProcesses][processLength][numOfProcesses];
		//initialize to false
		for(int i = 0; i<numOfProcesses; i++){
			for(int j=0; j<processLength; j++){
				for(int k=0; k<numOfProcesses; k++){
					livenessSynchronizationSkeleton[i][j][k]=false;
				}
			}
		}
		
		//if there is only one process there is no need for synchronization
		if(numOfProcesses == 1){
			return livenessSynchronizationSkeleton;
		}
		
		for(int i=0; i<processLength; i++){
			SynchedNode[] nodes = new SynchedNode[numOfProcesses];
			for(int j=0; j<numOfProcesses; j++){
				ArrayList<SynchedNode> localStrat = localStrategies.get(j);
				nodes[j] = localStrat.get(i);
			}
			//if synch is not necessary for fulfilling liveness
//			if(!nodes[0].sync){
//				for(int j=0; j<numOfProcesses; j++){
//					for(int k=0; k<numOfProcesses; k++){
//						livenessSynchronizationSkeleton[j][i][k] = false;
//					}
//				}
//			}else{
			if(nodes[0].sync){
				boolean[] processInvolved = new boolean[numOfProcesses];
				for(int j=0; j<numOfProcesses; j++){
					processInvolved[j] = true;
				}
				
				//translate to a formula
				int regionFormula = translateNodesToRegionFormula(bdd, nodes, processInvolved, regionVars, regionToVarMap);
				
				//check which liveness guarantee this nodes satisfy
				//TODO: simple case based on the disjoint liveness guarantees assumption
				int satisfiedLivenessGuarantee = -1;
				int synchRegionFormula = BDDWrapper.and(bdd, regionFormula, synchronizationVariable.getBDDVar());
				for(int j=0; j<collectiveLivenessGuarantees.size(); j++){
					int satisfies = BDDWrapper.implies(bdd, synchRegionFormula, collectiveLivenessGuarantees.get(j));
					if(satisfies == 1){
						satisfiedLivenessGuarantee = collectiveLivenessGuarantees.get(j);
					}
				}
				BDDWrapper.free(bdd, synchRegionFormula);
				if(satisfiedLivenessGuarantee == -1){
					System.err.println("no liveness guarantee is satisfied in this state, something must be wrong!");
				}
				
				//reduce
				for(int j=0; j<numOfProcesses; j++){
					processInvolved[j] = false;
					regionFormula = translateNodesToRegionFormula(bdd, nodes, processInvolved, regionVars, regionToVarMap);
					int satisfies = BDDWrapper.implies(bdd, regionFormula, satisfiedLivenessGuarantee);
					if(satisfies != 1){
						processInvolved[j] = true; 
					}
				}
				
				//transform to decentralized representation
				for(int j=0; j<numOfProcesses; j++){
					if(processInvolved[j]){
						for(int k=0; k<numOfProcesses; k++){
							if(j==k) continue;
							if(processInvolved[k]){
								livenessSynchronizationSkeleton[j][i][k] = true;
							}
						}
					}
				}
				
			}
		}
		
		return livenessSynchronizationSkeleton;
	}
	
//	private static int translateNodesToLocalRegionFormulas(BDD bdd, SynchedNode[] nodes, Variable[] regionVars, Variable[] localRegionVars, HashMap<Node, Variable> regionToVarMap){
//		Variable[] involvedLocalRegionVariables = new Variable[nodes.length];
//		for(int i=0; i<nodes.length; i++){
//			Variable region = regionToVarMap.get(nodes[i].node);
//			involvedLocalRegionVariables[i]=
//		}
//		Variable[] notInvolved = Variable.difference(regionVars, involvedRegionVariables);
//		
//		Varibale[] involvedLocal = 
//	}
	
	private static int translateToRegionFormula(BDD bdd, Variable[] involved, Variable[] notInvolved){
		int regionFormula = bdd.ref(bdd.getOne());
		if(involved != null && involved.length !=0){
			for(Variable v : involved){
				regionFormula = BDDWrapper.andTo(bdd, regionFormula, v.getBDDVar());
			}
		}
		if(notInvolved != null && notInvolved.length!=0){
			for(Variable v : notInvolved){
				int notV = bdd.ref(bdd.not(v.getBDDVar()));
				regionFormula = BDDWrapper.andTo(bdd, regionFormula, notV);
				BDDWrapper.free(bdd, notV);
			}
		}
		
		//debugging
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "region formula is ", regionFormula);
		
		return regionFormula;
	}
	
	private static int translateNodesToRegionFormula(BDD bdd, SynchedNode[] nodes , boolean[] processInvolved, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		
//		//debugging 
//		System.out.println("translating synchronized nodes to region formulas");
//		for(int i=0; i<processInvolved.length; i++){
//			System.out.println(processInvolved[i]?"process "+i+" involved": "process "+i+" NOT involved");
//		}
		
//		System.out.println("nodes:");
//		for(int i=0; i<nodes.length; i++){
//			nodes[i].print();
//		}
		
		Variable[] involvedRegionVars = involvedRegions(bdd, nodes, processInvolved, regionVars, regionToVarMap);
		Variable[] notInvolved = Variable.difference(regionVars, involvedRegionVars);
		
//		System.out.println("involved region vars");
//		Variable.printVariables(involvedRegionVars);
//		System.out.println("Not involved region vars");
//		Variable.printVariables(notInvolved);
		
		return translateToRegionFormula(bdd, involvedRegionVars, notInvolved);
//		int regionFormula = bdd.ref(bdd.getOne());
//		for(Variable v : involvedRegionVars){
//			regionFormula = BDDWrapper.andTo(bdd, regionFormula, v.getBDDVar());
//		}
//		for(Variable v : notInvolved){
//			int notV = bdd.ref(bdd.not(v.getBDDVar()));
//			regionFormula = BDDWrapper.andTo(bdd, regionFormula, notV);
//		}
//		return regionFormula;
	}
	
	private static Variable[] involvedRegions(BDD bdd, SynchedNode[] nodes , boolean[] processInvolved, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		ArrayList<Variable> involvedRegionVars = new ArrayList<Variable>();
		for(int i=0; i<processInvolved.length;i++){
			if(processInvolved[i]){
				Node node = nodes[i].getNode();
				Variable regionVar = regionToVarMap.get(node);
				if(!involvedRegionVars.contains(regionVar)){
					involvedRegionVars.add(regionVar);
				}
			}
		}
		if(involvedRegionVars.size()==0){
			return null;
		}
		
		Variable[] result = involvedRegionVars.toArray(new Variable[involvedRegionVars.size()]);
		return result;
		
	}
	
//	public static PartitionedCentralLTS partitionStrategy(BDD bdd, 
//			ControllableSinglePlayerDeterministicGameStructure strategy, 
//			int init, Variable[][] regions, 
//			Variable synchronizeSignal, HashMap<Variable, Node> varToRegionMap, 
//			HashMap<Node, Variable> regionToVarMap,
//			DirectedGraph<String, Node, Edge<Node>> regionGraph, Variable[] vars, 
//			int unsafeStates){
//		
//		ArrayList<ArrayList<Node>> partitions = new ArrayList<ArrayList<Node>>();
//		ArrayList<ArrayList<Edge<Node>>> matches = new ArrayList<ArrayList<Edge<Node>>>();
//		ArrayList<Boolean> sync = new ArrayList<Boolean>();
//		
//		int currentState = bdd.ref(init);
//		ArrayList<Node> currentNodePartitions = partitionStateIntoRegions(bdd, currentState, regions, varToRegionMap);
//		partitions.add(currentNodePartitions);
//		matches.add(null);
//		sync.add(isSynched(bdd, currentState, synchronizeSignal));
//		
//		ArrayList<Integer> visited = new ArrayList<Integer>();
//		while(!visited.contains(currentState)){
//			visited.add(currentState);
//			
//			
//			
//			int nextState = strategy.symbolicGameOneStepExecution(currentState);
//			
//			ArrayList<Node> nextNodePartitions = partitionStateIntoRegions(bdd, nextState, regions, varToRegionMap); 
//			partitions.add(nextNodePartitions);
//			sync.add(isSynched(bdd, nextState, synchronizeSignal));
//			
//			//TODO: match current partitioning to the next one 
//			matches.add(matchRegions(bdd, vars, regionGraph, currentNodePartitions, nextNodePartitions, regionToVarMap,  unsafeStates));
//			
//			currentNodePartitions = nextNodePartitions;
//			BDDWrapper.free(bdd, currentState);
//			currentState = nextState;
//		}
//		
//		int loopingIndex = visited.indexOf(currentState);
//		BDDWrapper.free(bdd, currentState);
//		
//		PartitionedCentralLTS result = new PartitionedCentralLTS(partitions, matches,sync,loopingIndex );
//		return result;
//	}
	
	public static PartitionedCentralLTS partitionStrategy(BDD bdd, 
			ControllableSinglePlayerDeterministicGameStructure strategy, 
			int init, Variable[] regionVars, 
			Variable synchronizeSignal, HashMap<Variable, Node> varToRegionMap, 
			HashMap<Node, Variable> regionToVarMap,
			DirectedGraph<String, Node, Edge<Node>> regionGraph, Variable[] vars, 
			int unsafeStates){
		
		ArrayList<ArrayList<Node>> partitions = new ArrayList<ArrayList<Node>>();
		ArrayList<ArrayList<Edge<Node>>> matches = new ArrayList<ArrayList<Edge<Node>>>();
		ArrayList<Boolean> sync = new ArrayList<Boolean>();
		
		int currentState = bdd.ref(init);
		ArrayList<Node> currentNodePartitions = partitionStateIntoRegions(bdd, currentState, regionVars, varToRegionMap);
		
		
		ArrayList<Integer> visited = new ArrayList<Integer>();
		while(!visited.contains(currentState)){
			visited.add(currentState);
			
			
			partitions.add(currentNodePartitions);
			sync.add(isSynched(bdd, currentState, synchronizeSignal));
			
			int nextState = strategy.symbolicGameOneStepExecution(currentState);
			
			ArrayList<Node> nextNodePartitions = partitionStateIntoRegions(bdd, nextState, regionVars, varToRegionMap); 
			
			//TODO: match current partitioning to the next one 
			matches.add(matchRegions(bdd, vars, regionGraph, currentNodePartitions, nextNodePartitions, regionToVarMap,  unsafeStates));
			
			currentNodePartitions = nextNodePartitions;
			BDDWrapper.free(bdd, currentState);
			currentState = nextState;
		}
		
		int loopingIndex = visited.indexOf(currentState);
		BDDWrapper.free(bdd, currentState);
		
		PartitionedCentralLTS result = new PartitionedCentralLTS(partitions, matches,sync,loopingIndex );
		return result;
	}
	
	private static ArrayList<Edge<Node>> matchRegions(BDD bdd, Variable[] vars, 
			DirectedGraph<String, Node, Edge<Node>> regionGraph, ArrayList<Node> currentPartitions, 
			ArrayList<Node> nextPartitions, 
			HashMap<Node, Variable> regionToVarMap, 
			int unsafeStates){
		ArrayList<ArrayList<Node>> connectedNodes = connectedNodesFromCurrentPartitionsToNextPartitions(regionGraph, currentPartitions, nextPartitions);
		
		ArrayList<ArrayList<Node>> removedNodes = new ArrayList<ArrayList<Node>>();
		//initially removed nodes are empty for every partition
		for(int i=0; i<currentPartitions.size(); i++){
			removedNodes.add(new ArrayList<Node>());
		}
		
		//each time remove an edge
		// check if the remaining matching covers all the regions
		// check if the resulting matching is safe
		//if the answer to both questions are yes, remove the edge
		//move on to the next edge
		for(int i=0; i<connectedNodes.size(); i++){
			ArrayList<Node> neighbors = connectedNodes.get(i);
			for(int j=0; j<neighbors.size(); j++){
				Node currentNeighbor = neighbors.get(j);
				ArrayList<Node> currentRemovedNodes = removedNodes.get(i);
				currentRemovedNodes.add(currentNeighbor);
				boolean canBeRemoved = false;
				if(isAllVerticesCovered(currentPartitions, nextPartitions, connectedNodes, removedNodes)){
					//check if the match is safe
					int matchingFormula = intermediateStatesFormula(bdd, vars, currentPartitions, connectedNodes, removedNodes, regionToVarMap); 
					int unsafe = BDDWrapper.and(bdd, matchingFormula, unsafeStates);
					BDDWrapper.free(bdd, matchingFormula);
					if(unsafe == 0){
						canBeRemoved = true;
					}
					BDDWrapper.free(bdd, unsafe);
				}
				if(!canBeRemoved){
					currentRemovedNodes.remove(currentNeighbor);
				}
			}
		}
		
		ArrayList<Edge<Node>> result = new ArrayList<Edge<Node>>();
		for(int i=0; i<currentPartitions.size();i++){
			Node currentNode = currentPartitions.get(i);
			ArrayList<Node> neighbors = connectedNodes.get(i);
			ArrayList<Node> removedNeighbors = removedNodes.get(i);
			for(Node n : neighbors){
				if(!removedNeighbors.contains(n)){
					result.add(new Edge<Node>(currentNode, n));
				}
			}
		}
		
		return result;
		
	}
	
	//TODO: can be optimized, however, the main complexity arise from synthesis part and therefore optimizing here cannot make a big difference
	private static boolean isAllVerticesCovered(ArrayList<Node> currentPartitions, ArrayList<Node> nextPartitions, ArrayList<ArrayList<Node>> connectedNodes, 
			ArrayList<ArrayList<Node>> removedNodes){
		//check if all the current nodes are connected to at least a next node
		for(int i=0; i<currentPartitions.size();i++){
			ArrayList<Node> neighbors = connectedNodes.get(i);
			ArrayList<Node> removedNeighbors = removedNodes.get(i); 
			boolean currentNodeCovered = false;
			for(Node n : neighbors){
				if(!removedNeighbors.contains(n)){
					currentNodeCovered = true;
					break;
				}
			}
			if(!currentNodeCovered){
				return false;
			}
		}
		
		//check if all the nodes in the next partition are connected to at least one node in the current partition
		for(int i = 0; i<nextPartitions.size(); i++){
			Node nextNode = nextPartitions.get(i);
			boolean covered = false; 
			for(int j=0; j<currentPartitions.size();j++){
				ArrayList<Node> neighbors = connectedNodes.get(j);
				ArrayList<Node> removedNeighbors = removedNodes.get(j);
				if(neighbors.contains(nextNode) && !removedNeighbors.contains(nextNode)){
					covered = true;
					break;
				}
			}
			if(!covered){
				return false;
			}
		}
		
		return true;
	}
	
	//TODO: extend with regions that do not appear
	private static int intermediateStatesFormula(BDD bdd, Variable[] vars, ArrayList<Node> currentPartitions, 
			ArrayList<ArrayList<Node>> matchedNodesToCurrentPartitions, ArrayList<ArrayList<Node>> removedNodes, 
			HashMap<Node, Variable> regionToVarMap){
		if(currentPartitions.size() != matchedNodesToCurrentPartitions.size()){
			System.err.println("Something is wrong, number of nodes in current partitions does not match with the matching to the next partitions");
			return -1;
		}
		
		int result=bdd.ref(bdd.getOne());
		
		
		for(int i=0; i<currentPartitions.size(); i++){
			Node currentRegion = currentPartitions.get(i);
			Variable currentVar = regionToVarMap.get(currentRegion);
			List<Node> neighbors = matchedNodesToCurrentPartitions.get(i);
			List<Node> removedNeighbors = removedNodes.get(i);
//			ArrayList<Node> nextNodes = new ArrayList<Node>();
			
			int andNextNodes = bdd.ref(bdd.getOne());
			int orNextNodes = bdd.ref(bdd.getZero());
			for(Node next : neighbors){
				if(!removedNeighbors.contains(next)){
					Variable nextVar = regionToVarMap.get(next);
					andNextNodes = BDDWrapper.andTo(bdd, andNextNodes, nextVar.getBDDVar());
					orNextNodes = BDDWrapper.orTo(bdd, orNextNodes, nextVar.getBDDVar());
				}
			}
			
			//translate to a logical formula
			//let current region be A, and next regions be B,C,D,...
			//translate to formula: (A | B | C | D | ... ) AND (A | (NOT A AND B AND C AND D AND ...))
			//meaning that if robots are moving from region A to regions B, C, ..., they can be distributed between regions A 
			//and destinations, however, if region A is empty, then the robots must be in regions B,C,D,etc. 
			int firstPart = BDDWrapper.or(bdd, currentVar.getBDDVar(), orNextNodes);
			int secondPart = bdd.ref(bdd.not(currentVar.getBDDVar()));
			secondPart = BDDWrapper.andTo(bdd, secondPart, andNextNodes);
			secondPart = BDDWrapper.orTo(bdd, secondPart, currentVar.getBDDVar());
			BDDWrapper.free(bdd, andNextNodes);
			BDDWrapper.free(bdd, orNextNodes);
			int formula = BDDWrapper.and(bdd, firstPart, secondPart);
			BDDWrapper.free(bdd, firstPart);
			BDDWrapper.free(bdd, secondPart);
			
			result= BDDWrapper.andTo(bdd, result, formula);
			BDDWrapper.free(bdd, formula);
			
			
		}
		
		return result;
		
	}
	
	private static ArrayList<ArrayList<Node>> connectedNodesFromCurrentPartitionsToNextPartitions(DirectedGraph<String, Node, Edge<Node>> regionGraph, ArrayList<Node> currentPartitions, 
			ArrayList<Node> nextPartitions){
		ArrayList<ArrayList<Node>> connectedNodes = new ArrayList<ArrayList<Node>>();
		
		for(int i=0; i<currentPartitions.size(); i++){
			Node currentRegion = currentPartitions.get(i);
			
			ArrayList<Node> nodesForCurrentNode = new ArrayList<Node>();
			
			//each node is connected to itself
			if(nextPartitions.contains(currentRegion)){
				nodesForCurrentNode.add(currentRegion);
			}
			
			List<Edge<Node>> neighbors = regionGraph.getAdjacent(currentRegion);
			for(Edge<Node> e : neighbors){
				Node next = e.getTarget();
				if(nextPartitions.contains(next)){
					nodesForCurrentNode.add(next);
				}
			}
			connectedNodes.add(nodesForCurrentNode);
		}
		return connectedNodes;
	}
	
	private static void printTransitionRelation(BDD bdd, int transitionRelation, int numOfRegions){
//		System.out.println("num of regions "+numOfRegions);
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, transitionRelation);
		
		Variable[] allVars = Variable.allVars.toArray(new Variable[Variable.allVars.size()]);
		
		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, transitionRelation, allVars);
		for(String m : minterms){
//			System.out.println("minterm is "+m);
			if(m == null || m.equals("\r") || m.equals("\n")){ 
				continue;
			}
			for(int i=0; i<numOfRegions+1;i++){
				System.out.print(m.substring(i*2,i*2+2)+" ");
			}
			System.out.print(m.substring(2*(numOfRegions+1)));
			System.out.println();
		}
	}
	
	public static ControllableSinglePlayerDeterministicGameStructure computeCentralSolution(BDD bdd, 
			ControllableSinglePlayerDeterministicGameStructure gs, 
			int init, GR1Objective objective, Variable[] regionVars, 
			Variable synchronizeSignal, HashMap<Variable, Node> varToRegionMap, 
			HashMap<Node, Variable> regionToVarMap,
			DirectedGraph<String, Node, Edge<Node>> regionGraph){
		
		ControllableSinglePlayerDeterministicGameStructure strategy = null;
		
//		TODO: clean up
//		ArrayList<Variable> tmp = new ArrayList<Variable>();
//		for(int i=0; i<regions.length;i++){
//			for(int j=0; j<regions[i].length; j++){
//				tmp.add(regions[i][j]);
//			}
//		}
//		Variable[] regionVars = tmp.toArray(new Variable[tmp.size()]);
		
		int safetyFormula = bdd.ref(bdd.getOne());
		ArrayList<Integer> safetyGuarantees = objective.getSafetyGuarantees();
		for(int i=0; i<safetyGuarantees.size();i++){
			safetyFormula = BDDWrapper.andTo(bdd, safetyFormula, safetyGuarantees.get(i));
		}
		int notSafe = BDDWrapper.not(bdd, safetyFormula);
		
//		UtilityMethods.debugBDDMethods(bdd, "unsafe states are", notSafe);
//		UtilityMethods.getUserInput();
		
		int additionalSafetyConstraints = bdd.ref(bdd.getOne());
		
		int numOfIterations = 0;
		
		do{
			
//			System.out.println("Computing central solution, iteration "+numOfIterations);
			numOfIterations++;
			
//			UtilityMethods.debugBDDMethods(bdd, "additional safety constraints are", additionalSafetyConstraints);
//			UtilityMethods.getUserInput();
			
			int restrictedTrans = BDDWrapper.and(bdd, gs.getSystemTransitionRelation(), additionalSafetyConstraints);
			ControllableSinglePlayerDeterministicGameStructure restrictedGS = new ControllableSinglePlayerDeterministicGameStructure(bdd, gs.variables, restrictedTrans, null); 
			GameSolution solution = GR1Solver.solve(bdd, restrictedGS, init, objective);
			BDDWrapper.free(bdd, restrictedTrans);
			
			if(solution.strategyOfTheWinner()==null){
				return null;
			}
			
			strategy = (ControllableSinglePlayerDeterministicGameStructure) solution.strategyOfTheWinner();
			
//			System.out.println("Central strategy computed");
//			UtilityMethods.debugBDDMethods(bdd, "winning states are", solution.getWinningSystemStates());
//			UtilityMethods.debugBDDMethods(bdd, "strategy is", strategy.getSystemTransitionRelation());
////			UtilityMethods.getUserInput();
			int numOfRegions = regionVars.length;
//			printTransitionRelation(bdd, strategy.getSystemTransitionRelation(), numOfRegions);
////			UtilityMethods.getUserInput();
			
			boolean safeStrategy = true;
			
			
			
			if(strategy != null){
				
//				System.out.println("strategy is not null");
//				Variable.printVariables(strategy.variables);
				
				int deterministicStrategy = bdd.ref(bdd.getZero());
				int currentState = bdd.ref(strategy.getInit());
//				int postImage;
				
//				ArrayList<ArrayList<Node>> partitions = new ArrayList<ArrayList<Node>>();
//				ArrayList<ArrayList<Edge<Node>>> edges = new ArrayList<ArrayList<Edge<Node>>>();
//				ArrayList<Boolean> sync = new ArrayList<Boolean>();
				
				ArrayList<Node> currentNodePartitions = partitionStateIntoRegions(bdd, currentState, regionVars, varToRegionMap);
//				partitions.add(currentNodePartitions);
//				edges.add(null);
//				sync.add(isSynched(bdd, currentState, synchronizeSignal));
				
				ArrayList<Integer> visited = new ArrayList<Integer>();
				
//				//debugging
//				UtilityMethods.debugBDDMethods(bdd, "current state is ", currentState);
//				System.out.println("current state is");
//				printTransitionRelation(bdd, currentState, numOfRegions);
//				System.out.println("initial node's partitions");
//				for(int i=0; i<currentNodePartitions.size();i++){
//					currentNodePartitions.get(i).printNode();
//				}
////				System.out.println("is synchronized? "+sync.get(0) );
//				UtilityMethods.getUserInput();
				
				while(!visited.contains(currentState)){
					
					//debugging
////					UtilityMethods.debugBDDMethods(bdd, "current state is ", currentState);
//					System.out.println("current state is");
//					printTransitionRelation(bdd, currentState, numOfRegions);
//					System.out.println("current node's partitions");
//					for(int i=0; i<currentNodePartitions.size();i++){
//						currentNodePartitions.get(i).printNode();
//					}
////					System.out.println("is synchronized? "+sync.get(sync.size()-1) );
////					UtilityMethods.getUserInput();
					
					//debugging 
//					int intersect = BDDWrapper.and(bdd, strategy.getSystemTransitionRelation(), currentState);
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "possible transitions from current state", intersect);
//					System.out.println("possible transitions from current state");
//					printTransitionRelation(bdd, intersect, numOfRegions);
					
					visited.add(currentState);
					
//					postImage = strategy.symbolicGameOneStepExecution(currentState);
//					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "post image is", postImage);
//					System.out.println("post image is ");
//					printTransitionRelation(bdd, postImage, numOfRegions);
//					UtilityMethods.getUserInput();
//					
//					//determinizing the strategy
//					//choose next state
//					int nextState = -1;
//					int notSyncNecessary = BDDWrapper.and(bdd, postImage, bdd.not(synchronizeSignal.getBDDVar()));
//					//synchronization is necessary at this step
//					if(notSyncNecessary == 0){
//						nextState = chooseNextState(bdd, postImage, strategy.variables);
//					}else{//otherwise choose a state where synchronization is not necessary
//						nextState = chooseNextState(bdd, notSyncNecessary, strategy.variables);
//					}
					
					int nextState = chooseNextStateGR1(bdd, strategy, currentState, regionVars, synchronizeSignal);
					
////					UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen next state is", nextState);
//					System.out.println("chosen next state is ");
//					printTransitionRelation(bdd, nextState, numOfRegions);
//					UtilityMethods.getUserInput();
					
					ArrayList<Node> nextNodePartitions = partitionStateIntoRegions(bdd, nextState, regionVars, varToRegionMap); 
//					partitions.add(nextNodePartitions);
					
					//add edges from the region graph
//					ArrayList<Edge<Node>> connectingEdges = connectPartitions(regionGraph, currentNodePartitions, nextNodePartitions); 
//					edges.add(connectingEdges);
					
//					sync.add(isSynched(bdd, nextState, synchronizeSignal));
					
					
					//translate partition into a logical formula
					int intermediateStatesFormula = transitionsWithIntermediateStates(bdd, regionGraph, 
							currentNodePartitions, nextNodePartitions, regionToVarMap, regionVars);
					
					//debugging 
//					UtilityMethods.debugBDDMethods(bdd, "transition with intermediate states", intermediateStatesFormula);
					
					//check for safety
					int intermediateSafety = BDDWrapper.and(bdd, intermediateStatesFormula, notSafe);
					
					//debugging 
//					UtilityMethods.debugBDDMethods(bdd, "intermediate trans not safe?", intermediateSafety);
					
//					UtilityMethods.getUserInput();
					
					//if adding intermediate states can cause unsafety, then mark the strategy as unsafe, 
					//and add transition from current node to the next node as unsafe
					if(intermediateSafety != 0){
						safeStrategy = false;
						
						Variable[] allVarsButGSVars = Variable.difference(strategy.variables, regionVars);
						int cube = BDDWrapper.createCube(bdd, allVarsButGSVars);
						
						int currentRegionState = BDDWrapper.exists(bdd, currentState, cube);
						int nextRegionState = BDDWrapper.exists(bdd, nextState, cube);
						BDDWrapper.free(bdd, cube);
						
						
//						int nextStatePrime = BDDWrapper.replace(bdd, nextState, strategy.getVtoVprime());
						
						int nextStatePrime = BDDWrapper.replace(bdd, nextRegionState, strategy.getVtoVprime());
						
						int notNextState = BDDWrapper.not(bdd, nextStatePrime);
						
//						int unsafeTransition = BDDWrapper.implies(bdd, currentState, notNextState);
						
						int unsafeTransition = BDDWrapper.implies(bdd, currentRegionState, notNextState);
						
						BDDWrapper.free(bdd, notNextState);
						
//						UtilityMethods.debugBDDMethods(bdd,"current state is", currentState);
//						UtilityMethods.debugBDDMethods(bdd, "next state is", nextState);
//						UtilityMethods.debugBDDMethods(bdd, "next state prime is", nextStatePrime);
//						UtilityMethods.getUserInput();
//						UtilityMethods.debugBDDMethods(bdd, "current region state is", currentRegionState);
//						UtilityMethods.debugBDDMethods(bdd, "next region state is", nextRegionState);
//						UtilityMethods.debugBDDMethods(bdd, "nextRegion state prime is", nextStatePrime);
//						UtilityMethods.getUserInput();
//						UtilityMethods.debugBDDMethods(bdd, "safety constraint to be added", unsafeTransition);
//						UtilityMethods.getUserInput();
						
						additionalSafetyConstraints = BDDWrapper.andTo(bdd, additionalSafetyConstraints, unsafeTransition);
						
						BDDWrapper.free(bdd, unsafeTransition);
					}
					
					int nextStatePrime = BDDWrapper.replace(bdd, nextState, strategy.getVtoVprime());
					int detTrans = BDDWrapper.and(bdd, currentState, nextStatePrime);
					BDDWrapper.free(bdd, currentState);
					currentState = nextState;
					BDDWrapper.free(bdd, nextStatePrime);
					
					deterministicStrategy = BDDWrapper.orTo(bdd, deterministicStrategy, detTrans);
					BDDWrapper.free(bdd, detTrans);
					
					currentNodePartitions = nextNodePartitions;
					
				}
				
				if(safeStrategy){
					ControllableSinglePlayerDeterministicGameStructure cgs = new ControllableSinglePlayerDeterministicGameStructure(bdd, strategy.variables,deterministicStrategy, null);
					cgs.setInit(strategy.getInit());
					System.out.println("total number of iterations to compute the central solution "+numOfIterations);
					return cgs;
				}else{
					BDDWrapper.free(bdd, deterministicStrategy);
				}

				

				

//				UtilityMethods.getUserInput();
				
				
			}
			
			BDDWrapper.free(bdd, restrictedTrans);
		}while(strategy != null);
		
		
		
		System.out.println("total number of iterations to compute the central solution "+numOfIterations);
		
		return null;
	}
	
	//TODO: extend, regions not appearing must be false
	private static int transitionsWithIntermediateStates(BDD bdd, DirectedGraph<String, Node, Edge<Node>> regionGraph, 
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
//			ArrayList<Node> nextNodes = new ArrayList<Node>();
			
			int andNextNodes = bdd.ref(bdd.getOne());
			int orNextNodes = bdd.ref(bdd.getZero());
			for(Edge<Node> e : neighbors){
				Node next = e.getTarget();
				if(nextPartitions.contains(next)){
//					nextNodes.add(next);
					Variable nextVar = regionToVarMap.get(next);
					if(!involvedVars.contains(nextVar)) involvedVars.add(nextVar);
					andNextNodes = BDDWrapper.andTo(bdd, andNextNodes, nextVar.getBDDVar());
					orNextNodes = BDDWrapper.orTo(bdd, orNextNodes, nextVar.getBDDVar());
				}
			}
			
			//translate to a logical formula
			//let current region be A, and next regions be B,C,D,...
			//translate to formula: (A | B | C | D | ... ) AND (A | (NOT A AND B AND C AND D AND ...))
			//meaning that if robots are moving from region A to regions B, C, ..., they can be distributed between regions A 
			//and destinations, however, if region A is empty, then the robots must be in regions B,C,D,etc. 
			int firstPart = BDDWrapper.or(bdd, currentVar.getBDDVar(), orNextNodes);
			int secondPart = bdd.ref(bdd.not(currentVar.getBDDVar()));
			secondPart = BDDWrapper.andTo(bdd, secondPart, andNextNodes);
			secondPart = BDDWrapper.orTo(bdd, secondPart, currentVar.getBDDVar());
			BDDWrapper.free(bdd, andNextNodes);
			BDDWrapper.free(bdd, orNextNodes);
			int formula = BDDWrapper.and(bdd, firstPart, secondPart);
			BDDWrapper.free(bdd, firstPart);
			BDDWrapper.free(bdd, secondPart);
			
			result= BDDWrapper.andTo(bdd, result, formula);
			BDDWrapper.free(bdd, formula);
			
			
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
	
	/**
	 * Return the subset of edges from the region graph that connects the current partition to the next one
	 * @param regionGraph
	 * @param currentPartitions
	 * @param nextPartitions
	 * @return
	 */
	private static ArrayList<Edge<Node>> connectPartitions(DirectedGraph<String, Node, Edge<Node>> regionGraph, ArrayList<Node> currentPartitions, ArrayList<Node> nextPartitions){
		ArrayList<Edge<Node>> edges = new ArrayList<Edge<Node>>();
		
		for(int i=0; i<currentPartitions.size(); i++){
			Node currentRegion = currentPartitions.get(i);
			List<Edge<Node>> neighbors = regionGraph.getAdjacent(currentRegion);
			for(Edge<Node> e : neighbors){
				Node next = e.getTarget();
				if(nextPartitions.contains(next)){
					edges.add(e);
				}
			}
		}
		
		return edges;
	}
	
//	private static int deetrminizeStrategy(BDD bdd, int init, ControllableSinglePlayerDeterministicGameStructure gs){
//		int currentState = bdd.ref(init);
//		
//		ArrayList<Integer> visited = new ArrayList<Integer>();
//		
//		while(!visited.contains(currentState)){
//			visited.add(currentState);
//			int nextState = gs.symbolicGameOneStepExecution(currentState);
//			
//		}
//	}
	
	/*****OLD CODE*****/
	
	public static SwarmSimulator2D simpleTest1(int size){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[size][size];
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
			}
		}
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		
		//Define variables 
		//define regions
		Variable[][] regions = new Variable[size][size];
		Variable[] regionVars = new Variable[size*size];
		ArrayList<Variable> variables = new ArrayList<Variable>();
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				regions[i][j] = Variable.createVariableAndPrimedVariable(bdd, "region["+i+"]["+j+"]");
				variables.add(regions[i][j]);
				regionVars[i*size+j] = regions[i][j];
				
				regionToVarMap.put(regionNodes[i][j], regions[i][j]);
				varToRegionMap.put(regions[i][j], regionNodes[i][j]);
			}
		}
		
		
		
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
		
		Variable[] vars = UtilityMethods.variableArrayListToArray(variables);
		
		//Create transition relation for swarm movement
		int trans = bdd.ref(bdd.getOne());
		for(int i=0; i<size; i++){
			for(int j=0; j<size; j++){
				int currentCellFormula = bdd.ref(bdd.not(regions[i][j].getBDDVar()));
				
				
				//stutter
				Variable primeCurrentCell = regions[i][j].getPrimedCopy();
				currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeCurrentCell.getBDDVar());
				
				int currentCellPrimeFormula = bdd.ref(bdd.not(primeCurrentCell.getBDDVar()));
				currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j].getBDDVar());
				
				Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i][j]);
				regionGraph.addEdge(e);
				
				//neighbors
				if(i-1>=0){
					Variable primeNeighbor = regions[i-1][j].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i-1][j].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i-1][j]);
					regionGraph.addEdge(e2);
				}
				if(i+1<size){
					Variable primeNeighbor = regions[i+1][j].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i+1][j].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i+1][j]);
					regionGraph.addEdge(e2);
				}
				if(j-1>=0){
					Variable primeNeighbor = regions[i][j-1].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j-1].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j-1]);
					regionGraph.addEdge(e2);
				}
				if(j+1<size){
					Variable primeNeighbor = regions[i][j+1].getPrimedCopy();
					currentCellFormula = BDDWrapper.orTo(bdd, currentCellFormula, primeNeighbor.getBDDVar());
					
					currentCellPrimeFormula = BDDWrapper.orTo(bdd, currentCellPrimeFormula, regions[i][j+1].getBDDVar());
					
					Edge<Node> e2 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j+1]);
					regionGraph.addEdge(e2);
				}
				trans = BDDWrapper.andTo(bdd, trans, currentCellFormula);
				trans = BDDWrapper.andTo(bdd, trans, currentCellPrimeFormula);
				BDDWrapper.free(bdd, currentCellFormula);
				BDDWrapper.free(bdd, currentCellPrimeFormula);
			}
		}
		
		int atLeastOneNonEmptyCell = bdd.getZero();
		for(int i=0; i<regions.length; i++){
			for(int j=0; j<regions[i].length;j++){
				atLeastOneNonEmptyCell = BDDWrapper.orTo(bdd, atLeastOneNonEmptyCell, regions[i][j].getBDDVar());
			}
		}
		trans = BDDWrapper.andTo(bdd, trans , atLeastOneNonEmptyCell);
		BDDWrapper.free(bdd, atLeastOneNonEmptyCell);
		
		
		Variable.printVariables(vars);
//		regionGraph.printDOT("regionGraph.dot");
//		UtilityMethods.getUserInput();
		
//		UtilityMethods.debugBDDMethods(bdd, "transitions", trans);
//		UtilityMethods.getUserInput();
//		
//		UtilityMethods.debugBDDMethods(bdd, "transitions", trans, Variable.unionVariables(vars, Variable.getPrimedCopy(vars)));
//		UtilityMethods.getUserInput();
		
		//Create a ControllableSinglePlayerDeterministicTransitionRelation
		ControllableSinglePlayerDeterministicGameStructure swarmGame = new ControllableSinglePlayerDeterministicGameStructure(bdd, vars, trans, null);
		
		
		
		//Create objective
		
		ArrayList<GridCell2D> targetRegions = new ArrayList<GridCell2D>();
		
		//region[1][1] eventually full and sync
		int region_1_1_full = createFullRegionPredicate(bdd, regions, size-1, size-1);
		region_1_1_full = BDDWrapper.andTo(bdd, region_1_1_full, synchronizeSignal.getBDDVar());
//		UtilityMethods.debugBDDMethods(bdd, "region_1_1_full", region_1_1_full, vars);
//		UtilityMethods.getUserInput();
		
		targetRegions.add(new GridCell2D(size-1, size-1));
		
		//region[0][1] must be visited
		int region_0_1_partial = bdd.ref(regions[0][size-1].getBDDVar());
		
		targetRegions.add(new GridCell2D(0, size-1));
		
		//region[1][0] must be visited
		int region_1_0_partial = bdd.ref(regions[size-1][0].getBDDVar());
		
		targetRegions.add(new GridCell2D(size-1, 0));
		
		//region[mid][mid] eventually full and sync
		int region_m_m_full = createFullRegionPredicate(bdd, regions, size/2, size/2);
//		region_m_m_full = BDDWrapper.andTo(bdd, region_m_m_full, synchronizeSignal.getBDDVar());
		
		targetRegions.add(new GridCell2D(size/2, size/2));
		
		ArrayList<Objective> reachabilityObjectives = new ArrayList<Objective>();
		reachabilityObjectives.add(new Objective(ObjectiveType.Reachability, region_1_1_full));
		reachabilityObjectives.add(new Objective(ObjectiveType.Reachability,region_0_1_partial));
		reachabilityObjectives.add(new Objective(ObjectiveType.Reachability,region_1_0_partial));
		reachabilityObjectives.add(new Objective(ObjectiveType.Reachability,region_m_m_full));
		
		ArrayList<Objective> safetyObjectives = new ArrayList<Objective>();
		safetyObjectives.add(new Objective(ObjectiveType.Safety,bdd.getOne()));
		
		//Define initial state
		//initially full swarm in regions[0][0]
		int region_0_0_full = createFullRegionPredicate(bdd, regions, 0, 0);
		
		
		int init = BDDWrapper.and(bdd, region_0_0_full, bdd.not(synchronizeSignal.getBDDVar()));
		swarmGame.setInit(init);
		
		//Solve the centralized game and compute the centralized strategy
		//TODO: adapt the synthesis algorithm 
		GameSolution solution = GameSolver.solveSafetyReachabilityObjectives(bdd, swarmGame, init, safetyObjectives , reachabilityObjectives);
		
		solution.print();
//		solution.getGameStructure().printGame();
//		UtilityMethods.getUserInput();
		
		
		//extract a deterministic strategy
		//start from the initial state
		//at each time, compute the post image based on the synthesized strategy
		//choose one state according to some criteria: minimize partitioning, minimize synchronization
		//repeat the process
		ControllableSinglePlayerDeterministicGameStructure solGame = (ControllableSinglePlayerDeterministicGameStructure) solution.strategyOfTheWinner();
		
		
		
		int deterministicStrategy = bdd.ref(bdd.getZero());
		int currentState = bdd.ref(solGame.getInit());
		int postImage;
		
		ArrayList<ArrayList<Node>> partitions = new ArrayList<ArrayList<Node>>();
		ArrayList<ArrayList<Edge<Node>>> matches = new ArrayList<ArrayList<Edge<Node>>>();
		ArrayList<Boolean> sync = new ArrayList<Boolean>();
		
		ArrayList<Node> currentNodePartitions = partitionStateIntoRegions(bdd, currentState, regionVars, varToRegionMap);
		partitions.add(currentNodePartitions);
		matches.add(null);
		sync.add(isSynched(bdd, currentState, synchronizeSignal));
		
//		//debugging
//		UtilityMethods.debugBDDMethods(bdd, "current state is ", currentState);
//		System.out.println("initial node's partitions");
//		for(int i=0; i<currentNodePartitions.size();i++){
//			currentNodePartitions.get(i).printNode();
//		}
//		System.out.println("is synchronized? "+sync.get(0) );
//		UtilityMethods.getUserInput();
		
		do{
			
			//debugging
//			UtilityMethods.debugBDDMethods(bdd, "current state is ", currentState);
//			System.out.println("current node's partitions");
//			for(int i=0; i<currentNodePartitions.size();i++){
//				currentNodePartitions.get(i).printNode();
//			}
//			System.out.println("is synchronized? "+sync.get(sync.size()-1) );
//			UtilityMethods.getUserInput();
			
			//debugging 
//			int intersect = BDDWrapper.and(bdd, solGame.getSystemTransitionRelation(), currentState);
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "possible transitions from current state", intersect);
			
			postImage = solGame.symbolicGameOneStepExecution(currentState);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "post image is", postImage);
			
			if(postImage == 0) break;
			
			//choose next state
			int nextState = -1;
			int notSyncNecessary = BDDWrapper.and(bdd, postImage, bdd.not(synchronizeSignal.getBDDVar()));
			//synchronization is necessary at this step
			if(notSyncNecessary == 0){
				nextState = chooseNextState(bdd, postImage, solGame.variables);
			}else{//otherwise choose a state where synchronization is not necessary
				nextState = chooseNextState(bdd, notSyncNecessary, solGame.variables);
			}
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen next state is", nextState);
			
			ArrayList<Node> nextNodePartitions = partitionStateIntoRegions(bdd, nextState, regionVars, varToRegionMap); 
			partitions.add(nextNodePartitions);
			matches.add(matchRegions(regionGraph, currentNodePartitions, nextNodePartitions));
			sync.add(isSynched(bdd, nextState, synchronizeSignal));
			
			
			
			int nextStatePrime = BDDWrapper.replace(bdd, nextState, solGame.getVtoVprime());
			int detTrans = BDDWrapper.and(bdd, currentState, nextStatePrime);
			BDDWrapper.free(bdd, currentState);
			currentState = nextState;
			BDDWrapper.free(bdd, nextStatePrime);
			
			deterministicStrategy = BDDWrapper.orTo(bdd, deterministicStrategy, detTrans);
			BDDWrapper.free(bdd, detTrans);
			
			currentNodePartitions = nextNodePartitions;
			
		}while(postImage != 0);
		
		//Partition the strategy and compute local strategies
//		ArrayList<Integer> localStrategies = partitionCentralizedStrategy(bdd, init, deterministicStrategy, solGame);
		
		//TODO: continue from here
		
		System.out.println("Partitions and sync");
		for(int i=0; i<partitions.size();i++){
			ArrayList<Node> partition = partitions.get(i);
			System.out.println("partition "+i);
			for(int j=0; j<partition.size(); j++){
				partition.get(j).printNode();
			}
			System.out.println("sync "+sync.get(i));
		}
		
		System.out.println("\n\nmatches");
		for(int i=1; i<matches.size(); i++){
			ArrayList<Edge<Node>> match = matches.get(i);
			System.out.println("match "+i);
			for(Edge<Node> e : match){
				e.printEdge();
			}
		}
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<SynchedNode>> localStrategies = extractLocalStrategies(partitions, matches, sync,-1);
		
		//print local strategies
		System.out.println("\n\nprinting local strategies");
		int localStratCounter = 1;
		for(ArrayList<SynchedNode> localStrat : localStrategies){
			System.out.println("local strategy "+localStratCounter);
			for(int i=0; i<localStrat.size(); i++){
				SynchedNode node = localStrat.get(i);
				node.print();
			}
			localStratCounter++;
		}
		
		//Minimize local strategies
		ArrayList<ArrayList<SynchedNode>> minimizedLocalStrategies = minimizeLocalStrategies(localStrategies);
		
		//print minimized local strategies
		System.out.println("\n\nprinting minimized local strategies");
		int minLocalStratCounter = 1;
		for(ArrayList<SynchedNode> localStrat : minimizedLocalStrategies){
			System.out.println("minimized local strategy "+minLocalStratCounter);
			for(int i=0; i<localStrat.size(); i++){
				SynchedNode node = localStrat.get(i);
				node.print();
			}
			minLocalStratCounter++;
		}
		
		
		//create swarm simulator for visualization
		SwarmSimulator2D simulator = new SwarmSimulator2D(regionNodes, minimizedLocalStrategies, targetRegions, null);
		
		return simulator;
	}
	
	public static ArrayList<ArrayList<SynchedNode>> minimizeLocalStrategies(ArrayList<ArrayList<SynchedNode>> localStrategies){
		ArrayList<ArrayList<SynchedNode>> minimizedLocalStrategies = new ArrayList<ArrayList<SynchedNode>>();
		for(ArrayList<SynchedNode> localStrat : localStrategies){
			ArrayList<SynchedNode> minimizedLocalStrat = new ArrayList<SynchedNode>();
			minimizedLocalStrat.add(localStrat.get(0));
			for(int i=1; i<localStrat.size(); i++){
				SynchedNode currentNode = localStrat.get(i);
				SynchedNode previousNode = localStrat.get(i-1);
				if(!currentNode.equal(previousNode)){
					minimizedLocalStrat.add(currentNode);
				}
			}
			minimizedLocalStrategies.add(minimizedLocalStrat);
		}
		return minimizedLocalStrategies;
	}
	
//	public static ArrayList<ArrayList<SynchedNode>> extractLocalStrategies(Node currentNode, int nextIndex, ArrayList<ArrayList<Edge<Node>>> matches, ArrayList<Boolean> sync){
//		
//		ArrayList<ArrayList<SynchedNode>> localStrategies = new ArrayList<ArrayList<SynchedNode>>();
//		if(nextIndex >= matches.size()){
//			ArrayList<SynchedNode> localStrat =  new ArrayList<SynchedNode>();
//			localStrategies.add(localStrat);
//			return localStrategies;
//		}
//		
//		ArrayList<Edge<Node>> edges = matches.get(nextIndex);
//		
////		if(nextIndex == matches.size()){
////			for(Edge<Node> e : edges){
////				Node source = e.getFrom();
////				if(source.equals(currentNode)){
////					Node nextNode = e.getTarget();
////					boolean syncValue = sync.get(nextIndex);
////					SynchedNode nextSynchedNode = new SynchedNode(nextNode, syncValue);
////					ArrayList<SynchedNode> localStrat =  new ArrayList<SynchedNode>();
////					localStrat.add(nextSynchedNode);
////					localStrategies.add(localStrat);
////				}
////			}
////			return localStrategies;
////		}
//		
//		for(Edge<Node> e : edges){
//			Node source = e.getFrom();
//			if(source.equals(currentNode)){
//				Node nextNode = e.getTarget();
//				boolean syncValue = sync.get(nextIndex);
//				SynchedNode nextSynchedNode = new SynchedNode(nextNode, syncValue);
//				ArrayList<ArrayList<SynchedNode>> localStrategiesStartedWithCurrentNode = extractLocalStrategies(nextNode, nextIndex+1, matches, sync);
//				for(ArrayList<SynchedNode> localStrat : localStrategiesStartedWithCurrentNode){
//					localStrat.add(0, nextSynchedNode);
//				}
//				localStrategies.addAll(localStrategiesStartedWithCurrentNode);
//			}
//		}
//		
//		return localStrategies;
//	}
	
//	//it should be a recursive algorithm that traverses the strategy tree
//	//TODO: what if the strategy has only a single node
//	public static ArrayList<ArrayList<SynchedNode>> extractLocalStrategies(ArrayList<ArrayList<Node>> partitions, ArrayList<ArrayList<Edge<Node>>> matches, ArrayList<Boolean> sync){
//		ArrayList<ArrayList<SynchedNode>> localStrategies = new ArrayList<ArrayList<SynchedNode>>(); 
//		
//		ArrayList<Node> initial = partitions.get(0);
//		boolean initSync = sync.get(0);
//		for(int i=0; i<initial.size(); i++){
//			SynchedNode currentNode = new SynchedNode(initial.get(i), initSync);
//			ArrayList<ArrayList<SynchedNode>> localStrategiesStartedWithCurrentNode = extractLocalStrategies(initial.get(i), 1, matches, sync);
//			for(ArrayList<SynchedNode> localStrat : localStrategiesStartedWithCurrentNode){
//				localStrat.add(0, currentNode);
//			}
//			localStrategies.addAll(localStrategiesStartedWithCurrentNode);
//		}
		
//		ArrayList<Node> initial = partitions.get(0);
//		boolean initSync = sync.get(0);
//		for(int i=0; i<initial.size(); i++){
//			ArrayList<SynchedNode> localStrat = new ArrayList<SynchedNode>();
//			localStrat.add(new SynchedNode(initial.get(i), initSync));
//			localStrategies.add(localStrat);
//		}
//		
//		for(int i=1; i<matches.size(); i++){
//			ArrayList<Node> currentNodes = partitions.get(i-1);
//			HashMap<Node, ArrayList<Node>> individualMatching = new HashMap<Node, ArrayList<Node>>();
//			for(Node n : currentNodes){
//				individualMatching.put(n, new ArrayList<Node>());
//			}
//			
//			ArrayList<Edge<Node>> edges = matches.get(i);
//			for(Edge<Node> e : edges){
//				ArrayList<Node> matchedTo = individualMatching.get(e.getFrom());
//				matchedTo.add(e.getTarget());
//			}
//			
//			for(ArrayList<SynchedNode> localStrat : localStrategies){
//				SynchedNode lastNode = localStrat.get(localStrat.size()-1);
//				ArrayList<Node> nextNodes = individualMatching.get(lastNode.getNode());
//				if(nextNodes.size()>1){
//					for()
//					UtilityMethods.copyArrayList(localStrat)
//				}
//			}
//		}
//		
//		return localStrategies;
//	}
	
	public static ArrayList<Integer> partitionCentralizedStrategy(BDD bdd, int init, ControllableSinglePlayerDeterministicGameStructure centralizedStrategy, Variable[] regionVars){
		ArrayList<Integer> localStrategies = new ArrayList<Integer>();
		
		//starting from the initial state, partition the current state according to regions. 
		//the value of the synchronization signal carries over
		int currentState = bdd.ref(init);
		ArrayList<Variable> currentRegions = partitionStateIntoRegions(bdd, currentState, regionVars);
		
		int nextState = centralizedStrategy.symbolicGameOneStepExecution(currentState);
		
		while(nextState!=0){
			//check the next state and partition it too
			ArrayList<Variable> nextRegions = partitionStateIntoRegions(bdd, nextState, regionVars); 
			
			//match the current state and next state partitions 
			//do the matching in a way that the number of local strategies generated is minimal
			
			
			//set the current partitions to next partitions and repeat the process
			currentRegions = nextRegions;
			BDDWrapper.free(bdd, currentState);
			int tmp = centralizedStrategy.symbolicGameOneStepExecution(nextState);
			BDDWrapper.free(bdd, nextState);
			nextState = tmp;
		}
		
		
		
		
		
		//return the generated local strategies
		
		
		return localStrategies;
	}
	
	//TODO: implement optimized algorithm for matching
	//This is an approximate algorithm that returns a maximal matching but necessarilly optimal
	public static ArrayList<Edge<Node>> matchRegions(DirectedGraph<String, Node, Edge<Node>> regionGraph, ArrayList<Node> currentPartitions, ArrayList<Node> nextPartitions){
		
		
		ArrayList<Node> nextMatchedRegions = new ArrayList<Node>();
		
		ArrayList<Edge<Node>> matches = new ArrayList<Edge<Node>>();
		
		for(int i=0; i<currentPartitions.size(); i++){
			Node currentRegion = currentPartitions.get(i);
			List<Edge<Node>> neighbors = regionGraph.getAdjacent(currentRegion);
			boolean currentRegionMatched = false;
			for(Edge<Node> e : neighbors){
				Node next = e.getTarget();
				if(nextPartitions.contains(next) && !nextMatchedRegions.contains(next)){
					matches.add(e);
					nextMatchedRegions.add(next);
					currentRegionMatched = true;
				}
			}
			if(!currentRegionMatched){
				//if all the next regions are already matched, then map the current region to one of the next regions (here to the fisrt edge)
//				matches.add(neighbors.get(0));
				for(Edge<Node> e: neighbors){
					Node next = e.getTarget();
					if(nextPartitions.contains(next)){
						matches.add(e);
						break;
					}
				}
			}
			
		}
		
		//check if all the next regions are matched
		for(int i=0; i<nextPartitions.size(); i++){
			Node next = nextPartitions.get(i);
			boolean nextMatched = false;
			if(!nextMatchedRegions.contains(next)){
				for(Node current : currentPartitions){
					List<Edge<Node>> neighbors = regionGraph.getAdjacent(current);
					for(Edge<Node> e : neighbors){
						if(e.getTarget() == next){
							matches.add(e);
							nextMatched=true;
							break;
						}
					}
					if(nextMatched) break;
				}
			}
		}
		
		return matches;
	}
	
	public static Boolean isSynched(BDD bdd, int state, Variable synchronizationSignal){
		int sync = bdd.ref(synchronizationSignal.getBDDVar());
		int isSynched = BDDWrapper.and(bdd, sync, state);
		if(isSynched == 0){
			return false;
		}
		return true;
	}
	
	public static ArrayList<Node> partitionStateIntoRegions(BDD bdd, int state, Variable[] regionVars, HashMap<Variable, Node> varToRegionMap){
		ArrayList<Variable> involvedRegions = partitionStateIntoRegions(bdd, state, regionVars);
		ArrayList<Node> result = new ArrayList<Node>();
		for(Variable var : involvedRegions){
			result.add(varToRegionMap.get(var));
		}
		return result;
	}
	
	public static ArrayList<Variable> partitionStateIntoRegions(BDD bdd, int state, Variable[] regionVars){
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
	
	public static int createFullRegionPredicate(BDD bdd, Variable[][] regions, int i, int j){
		int fullRegion = bdd.ref(regions[i][j].getBDDVar());
		for(int ii = 0 ; ii<regions.length; ii++){
			for(int jj=0; jj<regions[ii].length; jj++){
				if(ii != i || jj != j){
					fullRegion = BDDWrapper.andTo(bdd, fullRegion, bdd.not(regions[ii][jj].getBDDVar()));
				}
			}
		}
		return fullRegion;
	}
	
	public static int chooseNextState(BDD bdd, int states, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, states, vars);
		
		//choosing the next state randomly
//		int randomIndex = UtilityMethods.randomIndex(minterms.size());
//		String chosenMinterm = minterms.get(randomIndex);
		
		//chooses a next state that has minimum number of partitioning 
		String chosenMinterm = minterms.get(0);
		int minimumPartitions = UtilityMethods.numOfOnes(chosenMinterm);
		for(int i=1; i<minterms.size();i++){
			String currentMinterm = minterms.get(i);
			int currentMintermsPartitions = UtilityMethods.numOfOnes(currentMinterm);
			if( currentMintermsPartitions < minimumPartitions){
				chosenMinterm = currentMinterm;
				minimumPartitions = currentMintermsPartitions;
			}
		}
		
		int state = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		return state;
		
	}
	
	public static int chooseNextStateGR1(BDD bdd, ControllableSinglePlayerDeterministicGameStructure solution, 
			int currentState, Variable[] regionVars, Variable synchronizationSignal){
		
		int postImage = solution.symbolicGameOneStepExecution(currentState);
		
		Variable[] synchVarArray = new Variable[]{synchronizationSignal};
		Variable[] vars = Variable.unionVariables(regionVars, synchVarArray);
		Variable[] counterVars = Variable.difference(solution.variables, vars);
		
		int sameCounter = BDDWrapper.same(bdd, counterVars);
		int notSameCounter = BDDWrapper.not(bdd, sameCounter);
		BDDWrapper.free(bdd, sameCounter);
		
		//current state regions
		Variable[] nonRegionVariables = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), regionVars);
		int cube = BDDWrapper.createCube(bdd, nonRegionVariables);
		int currentStateRegions = BDDWrapper.exists(bdd, currentState, cube); 
		
		//1) make progress -- not the same counter, if possible
		//do not synchronize, if possible
		//choose the next state that has the minimum number of partitioning
		
		int postImageWithProgress = BDDWrapper.and(bdd, postImage, notSameCounter);
		int postImageWithProgressStutter = BDDWrapper.and(bdd, postImageWithProgress, currentStateRegions);
		if(postImageWithProgressStutter!=0){
			BDDWrapper.free(bdd, postImageWithProgress);
			postImageWithProgress = postImageWithProgressStutter;
		}else if(postImageWithProgress == 0){
			postImageWithProgress = postImage;
		}
		
		int syncPostImage = BDDWrapper.and(bdd, postImageWithProgress, bdd.not(synchronizationSignal.getBDDVar()));
		if(syncPostImage == 0){
			syncPostImage = postImageWithProgress;
		}
		
		BDDWrapper.free(bdd, notSameCounter);
		BDDWrapper.free(bdd, cube);
		BDDWrapper.free(bdd, currentStateRegions);
		
		return chooseNextState(bdd, syncPostImage, solution.variables);
		
	}
	
	
	

}

