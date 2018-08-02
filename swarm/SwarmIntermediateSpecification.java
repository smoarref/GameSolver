package swarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import solver.GR1Objective;
import specification.GridCell2D;
import utils.UtilityMethods;
import game.BDDWrapper;
import game.ControllableSinglePlayerDeterministicGameStructure;
import game.Variable;
import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;
import jdd.bdd.BDD;

public class SwarmIntermediateSpecification {
	
	BDD bdd; 
	Node[] regions; 
	Node[][] regions2D;
	Variable[] regionVars;
	Variable synchronizationSignal;
	Variable[] vars;
	
	HashMap<Node, Variable> regionToVarMap;
	HashMap<Variable, Node> varToRegionMap;
	
	DirectedGraph<String, Node, Edge<Node>> regionGraph;
	
	ControllableSinglePlayerDeterministicGameStructure swarmGame; 
	
	int init;
	
	ArrayList<Integer> safetyObjectives; 
	int safetyFormula; 
	int unsafeStates;
	
	ArrayList<Integer> collectiveLiveness;
	ArrayList<Integer> individualLiveness;
	ArrayList<Integer> liveness;
	GR1Objective objective;

	public SwarmIntermediateSpecification(BDD argBdd, Node[] argRegions, Variable[] argRegionVars, Variable argSynchronizationSignal, 
			HashMap<Node, Variable> argRegionToVarMap, HashMap<Variable, Node> argVarToRegionMap, 
			DirectedGraph<String, Node, Edge<Node>> argRegionGraph, int argInit, 
			ArrayList<Integer> argSafetyObjectives, ArrayList<Integer> argCollectiveLiveness, ArrayList<Integer> argIndividualLiveness) {	
		
		bdd = argBdd; 
		
		regions = argRegions;
		
		regionVars = argRegionVars; 
		
		synchronizationSignal = argSynchronizationSignal;
		
		vars = Variable.unionVariables(regionVars, new Variable[]{synchronizationSignal});
		
		regionToVarMap = argRegionToVarMap;
		varToRegionMap = argVarToRegionMap;
		
		regionGraph = argRegionGraph;
		
		int transitionRelation = directedGraphToTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		
		//prepare controllable game structure 
		swarmGame = new ControllableSinglePlayerDeterministicGameStructure(bdd, vars, transitionRelation, null);
		
		init = argInit;
		
		swarmGame.setInit(init);
		
		safetyObjectives = argSafetyObjectives;
		
		safetyFormula = bdd.ref(bdd.getOne());
		for(int i=0; i<safetyObjectives.size();i++){
			safetyFormula = BDDWrapper.andTo(bdd, safetyFormula, safetyObjectives.get(i));
		}
		unsafeStates = BDDWrapper.not(bdd, safetyFormula);
		
		collectiveLiveness = argCollectiveLiveness;
		individualLiveness = argIndividualLiveness;
		
		liveness = new ArrayList<Integer>();
		liveness.addAll(individualLiveness);
		liveness.addAll(collectiveLiveness);
		
		ArrayList<Integer> livenessAssumptions = new ArrayList<Integer>();
		livenessAssumptions.add(bdd.ref(bdd.getOne()));
		
		objective = new GR1Objective(new solver.BDDWrapper(bdd), livenessAssumptions, liveness, null, safetyObjectives);
		
	}
	
	public void setRegions2D(Node[][] argRegions2D){
		regions2D = argRegions2D;
	}
	
	public Node[][] getRegions2D(){
		if(regions2D==null){
			System.err.println("regions2D are not set! Null is returned!");
		}
		return regions2D;
	}
	
	public int getSafetyFormula(){
		return safetyFormula;
	}
	
	public int getUnsafeStates(){
		return unsafeStates;
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
	
	public BDD getBDD(){
		return bdd;
	}
	
	public Node[] getRegions(){
		return regions;
	}
	
	public Variable[] getRegionVariables(){
		return regionVars;
	}
	
	public Variable getSynchronizationSignal(){
		return synchronizationSignal;
	}
	
	public Variable[] getVariables(){
		return vars;
	}
	
	public HashMap<Node, Variable> getRegionToVarMap(){
		return regionToVarMap;
	}
	
	public HashMap<Variable, Node> getVarToRegionMap(){
		return varToRegionMap;
	}
	
	public DirectedGraph<String, Node, Edge<Node>> getRegionGraph(){
		return regionGraph;
	}
	
	public ControllableSinglePlayerDeterministicGameStructure getSwarmGame(){
		return swarmGame;
	}
	
	public int getInit(){
		return init;
	}
	
	public ArrayList<Integer> getSafetyObjectives(){
		return safetyObjectives;
	}
	
	public ArrayList<Integer> getCollectiveLivenessGuarantees(){
		return collectiveLiveness;
	}
	
	public ArrayList<Integer> getIndividualLivenessGuarantees(){
		return individualLiveness;
	}
	
	public ArrayList<Integer> getLivenessGuarantees(){
		return liveness;
	}
	
	public GR1Objective getObjective(){
		return objective;
	}
	
	public void print(){
		System.out.println("***********************************");
		System.out.println("printing input specification");
		System.out.println("region graph");
		regionGraph.print();
		System.out.println("Variables");
		Variable.printVariables(vars);
		UtilityMethods.debugBDDMethods(bdd, "init", init);
		System.out.println();
		System.out.println("safety objectives");
		for(int i =0; i<safetyObjectives.size();i++){
			UtilityMethods.debugBDDMethods(bdd, "safety objective "+i, safetyObjectives.get(i));
		}
		System.out.println();
		System.out.println("individual liveness guarantee");
		for(int i=0; i<individualLiveness.size(); i++){
			UtilityMethods.debugBDDMethods(bdd, "individual liveness "+i, individualLiveness.get(i));
		}
		System.out.println();
		System.out.println("collective liveness guarantee");
		for(int i=0; i<collectiveLiveness.size(); i++){
			UtilityMethods.debugBDDMethods(bdd, "collective liveness" + i, collectiveLiveness.get(i));
		}
		System.out.println("***********************************");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static int createRegionPredicate(BDD bdd, Variable[][] regions, ArrayList<GridCell2D> occupiedRegions){
		int predicate = bdd.ref(bdd.getOne());
		for(int i=0; i<regions.length; i++){
			for(int j=0; j<regions[i].length; j++){
				if(isRegionOccupied(occupiedRegions, i, j)){
					predicate = BDDWrapper.andTo(bdd, predicate, regions[i][j].getBDDVar());
				}else{
					predicate = BDDWrapper.andTo(bdd, predicate, bdd.not(regions[i][j].getBDDVar()));
				}
			}
		}
		return predicate;
	}
	
	private static boolean isRegionOccupied(ArrayList<GridCell2D> occupied, int x, int y){
		for(int i=0; i<occupied.size();i++){
			GridCell2D currentCell = occupied.get(i);
			if(currentCell.getX() == x && currentCell.getY() == y){
				return true;
			}
		}
		return false;
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
	
	private static Variable[][] correspondingVars(Node[][] regionNodes, HashMap<Node, Variable> regionToVarMap){
		Variable[][] regions = new Variable[regionNodes.length][regionNodes[0].length];
		for(int i=0; i<regionNodes.length; i++){
			for(int j=0; j<regionNodes[i].length; j++){
				regions[i][j] = regionToVarMap.get(regionNodes[i][j]);
			}
		}
		return regions;
	}
	
	private static Variable[] createRegionVars(BDD bdd, Node seedNode, DirectedGraph<String, Node, Edge<Node>> regionGraph, 
			HashMap<Node, Variable> regionToVarMap, 
			HashMap<Variable, Node> varToRegionMap){
		
		ArrayList<Variable> vars = new ArrayList<Variable>();
		
		ArrayList<Node> nodeQ = new ArrayList<Node>();
		nodeQ.add(seedNode);
		
		while(nodeQ.size()!=0){
			Node currentNode = nodeQ.remove(0);
			
			//check if the currentNode has a corresponding variable
			Variable currentVariable = regionToVarMap.get(currentNode);
			
			//if not, create a variable for it
			if(currentVariable == null){
				currentVariable = new Variable(bdd, currentNode.getName());
				regionToVarMap.put(currentNode, currentVariable);
				varToRegionMap.put(currentVariable, currentNode);
				vars.add(currentVariable);
			}
			
			//get neighbors
			List<Edge<Node>> neighbors = regionGraph.getAdjacent(currentNode);
			
			//for each neighbor
			for(Edge<Node> e : neighbors){
				//if the neighbor has not created, create it and add it to the end of Q
				Node neighbor = e.getTarget();
				Variable neighborVariable = regionToVarMap.get(neighbor);
				if(neighborVariable == null){
					neighborVariable = new Variable(bdd, neighbor.getName());
					regionToVarMap.put(neighbor, neighborVariable);
					varToRegionMap.put(neighborVariable, neighbor);
					vars.add(neighborVariable);
					nodeQ.add(neighbor);
				}
			}
			
			
			
			//if all neighbors have corresponding variables, then add the prime variable for the current node
			Variable.createPrimeVariable(bdd, currentVariable);
		}
		
		Variable[] result = vars.toArray(new Variable[vars.size()]);
		return result;
	}
	
	private static int createRegionPredicate(BDD bdd, Variable[] regionVars, ArrayList<Variable> occupiedRegions){
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
	
	/**
	 * Creates a Hexbug example where we already know there will be a group of Spheros. 
	 * Spheros must move between regions A,B,F, E, and G, and avoid regions C and D at all times. 
	 * Hegbugs must move between C and D and G, and avoid A,B,E, and F at all times.
	 */
	public static SwarmIntermediateSpecification createHexbugExample(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G")};
		
		//A <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[0]));
		
		//B <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[1]));
		
		//C <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[2]));
		
		//D <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[3]));
		
		//E <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
		
		//F <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
				
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
						
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
				
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
				
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
					
						
		Variable.printVariables(regionVars);
				
		//define objective
				
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
						
								
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
						
		
				
		//always eventually all in D 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[3]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
						
		//always eventually C & D
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[2]);
		occupiedRegions2.add(regionVars[3]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
						
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
						
		livenessGuarantees.addAll(individualLivenessGuarantees);
						
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
				
		//never A
		int safety1 = BDDWrapper.not(bdd, regionVars[0].getBDDVar());
		safetyGuarantees.add(safety1);
		
		//never B
		int safety2 = BDDWrapper.not(bdd, regionVars[1].getBDDVar());
		safetyGuarantees.add(safety2);
		
		//never E
		int safety3 = BDDWrapper.not(bdd, regionVars[4].getBDDVar());
				safetyGuarantees.add(safety3);
				
		//never F
		int safety4 = BDDWrapper.not(bdd, regionVars[5].getBDDVar());
		safetyGuarantees.add(safety4);
		
		//define initial state
		//initially swarm in regions C 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[2]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
								
								
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
							synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
							init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
								
								
								
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
								
		return result;
	}
	
	/**
	 * Creates a sphero example where we already know there will be a group of Hexbugs. 
	 * Spheros must move between regions A,B,F, E, and G, and avoid regions C and D at all times. 
	 * Hegbugs must move between C and D and G, and avoid A,B,E, and F at all times.
	 */
	public static SwarmIntermediateSpecification createSpheroExample(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G")};
		
		//A <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[0]));
		
		//B <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[1]));
		
		//C <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[2]));
		
		//D <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[3]));
		
		//E <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
		
		//F <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
				
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
						
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
				
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
				
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
					
						
		Variable.printVariables(regionVars);
				
		//define objective
				
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		
		//always eventually all in A 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[0]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually B and F and E
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[1]);
		occupiedRegions2.add(regionVars[4]);
		occupiedRegions2.add(regionVars[5]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
						
		//all the robots must visit E
		ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
		occupiedRegions1.add(regionVars[4]);
		int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
		individualLivenessGuarantees.add(individualLiveness1);
								
		
						
		
				
		
						
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
						
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
						
		livenessGuarantees.addAll(individualLivenessGuarantees);
						
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
				
		//never C
		int safety1 = BDDWrapper.not(bdd, regionVars[2].getBDDVar());
		safetyGuarantees.add(safety1);
		
		//never D
		int safety2 = BDDWrapper.not(bdd, regionVars[3].getBDDVar());
		safetyGuarantees.add(safety2);
				
		//define initial state
		//initially swarm in regions A 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
								
								
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
							synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
							init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
								
								
								
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
								
		return result;
	}
	
	/**
	 * Creates a map with 8 region, two of which are corridors 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample9(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I")};
		
		//A <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[0]));
		
		//B <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//E <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[4]));
		
		//F <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
		
		//F <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[4]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[5]));
		
		//G <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
		
		//H <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
		
		//I <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[8]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[5]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually G and H and I 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[6]);
		occupiedRegions3.add(regionVars[7]);
		occupiedRegions3.add(regionVars[8]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually the whole swarm in F
		ArrayList<Variable> occupiedRegions4 = new ArrayList<Variable>();
		occupiedRegions4.add(regionVars[5]);
		int collectiveLiveness3 = createRegionPredicate(bdd, regionVars, occupiedRegions4);
		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through E
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[4]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never E and F at the same time
		int notSafe1 = BDDWrapper.and(bdd, regionVars[3].getBDDVar(), regionVars[4].getBDDVar());
		int safety1 = BDDWrapper.not(bdd, notSafe1);
		safetyGuarantees.add(safety1);
		
		//never (G or H or I ) and (A or B)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[6].getBDDVar(), regionVars[7].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[8].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
//		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A and B and C 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 7 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample8(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G")};
		
		//A <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));
		
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
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B and C
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		occupiedRegions2.add(regionVars[2]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually E and F and G
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[4]);
		occupiedRegions3.add(regionVars[5]);
		occupiedRegions3.add(regionVars[6]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through C
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[3]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (D or E or F) and (A or B or C)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, regionVars[2].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[4].getBDDVar(), regionVars[5].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[6].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A and B and C
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		occupiedRegionsInit.add(regionVars[2]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 5 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample7(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), new Node("E"), new Node("F")};
		
		//A <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[0]));
		
		//B <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//C <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[4]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[2]));
		
		//C <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[2]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually D and E and F
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[3]);
		occupiedRegions3.add(regionVars[4]);
		occupiedRegions3.add(regionVars[5]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through C
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[2]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (D or E or F) and (A or B)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[3].getBDDVar(), regionVars[4].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[5].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 5 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample6(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), new Node("E")};
		
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
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually C and D and E
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[2]);
		occupiedRegions3.add(regionVars[3]);
		occupiedRegions3.add(regionVars[4]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through B
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[1]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (C or D or E) and (A)
		int tmp2 = BDDWrapper.or(bdd, regionVars[2].getBDDVar(), regionVars[3].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[3].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, regionVars[0].getBDDVar(), tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 4 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample5(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				};
		
		//A <-> B
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
		//B <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//B <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually C and D
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[2]);
		occupiedRegions3.add(regionVars[3]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through F
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[1]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (C or D) and (A)
		int tmp2 = BDDWrapper.or(bdd, regionVars[2].getBDDVar(), regionVars[3].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, regionVars[0].getBDDVar(), tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 3 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample4(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C")};
		
		//A <-> B
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
		//B <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually C
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[2]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		

		
		//all the robots must pass through D
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[1]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (A and C)
		
		int notSafe2 = BDDWrapper.and(bdd, regionVars[0].getBDDVar(), regionVars[2].getBDDVar());
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 7 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample3(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G")};
		
		//A <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[0]));
		
		//B <-> C
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[2]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//E <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
		
		
		//F <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[4]));
		
		//G <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually F and G 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[5]);
		occupiedRegions3.add(regionVars[6]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually the whole swarm in E
		ArrayList<Variable> occupiedRegions4 = new ArrayList<Variable>();
		occupiedRegions4.add(regionVars[4]);
		int collectiveLiveness3 = createRegionPredicate(bdd, regionVars, occupiedRegions4);
		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through D
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[3]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (F or G) and (A or B)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[5].getBDDVar(), regionVars[6].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
//		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A and B and C 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 9 region, one of which is a corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample2(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I")};
		
		//A <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));
		
		//B <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//E <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
		
		
		//F <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[4]));
		
		//G <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
		
		//H <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[7]));
		
		//I <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[8]));
		
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B and C
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		occupiedRegions2.add(regionVars[2]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually G and H and I 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[6]);
		occupiedRegions3.add(regionVars[7]);
		occupiedRegions3.add(regionVars[8]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually the whole swarm in F
		ArrayList<Variable> occupiedRegions4 = new ArrayList<Variable>();
		occupiedRegions4.add(regionVars[5]);
		int collectiveLiveness3 = createRegionPredicate(bdd, regionVars, occupiedRegions4);
		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through F
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[4]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		
		//never (G or H or I ) and (A or B or C)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, regionVars[2].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[6].getBDDVar(), regionVars[7].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[8].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A and B and C 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		occupiedRegionsInit.add(regionVars[2]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	/**
	 * Creates a map with 10 region, two of which are corridors 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCorridorExample(){
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		//regions
		//A,B,C,D,E,F,G,H,I,J
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I"), new Node("J")};
		
		//A <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[0]));
		
		//B <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[1]));
		
		//C <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[2], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[2]));
		
		//E <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[4]));
		
		//F <-> D
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[3]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[3], regionNodes[5]));
		
		//G <-> E
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[4]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[4], regionNodes[6]));
		
		//G <-> F
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[5]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[5], regionNodes[6]));
		
		//H <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[7]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[7], regionNodes[6]));
		
		//I <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[8]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[8], regionNodes[6]));
		
		//J <-> G
		regionGraph.addEdge(new Edge<Node>(regionNodes[6], regionNodes[9]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[6]));
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		//Define variables 
		//define regions
		ArrayList<Variable> variables = new ArrayList<Variable>();
				
		variables = createRegionVariables(bdd, regionNodes, regionToVarMap, varToRegionMap);
		
		Variable[] regionVars = variables.toArray(new Variable[variables.size()]);
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
			
				
		Variable.printVariables(regionVars);
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
//				
//				//all the robots must visit storage room
//				ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
//				occupiedRegions1.add(storageRoom);
//				int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
//				individualLivenessGuarantees.add(individualLiveness1);
						
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
				
		//always eventually A and B and C
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(regionVars[0]);
		occupiedRegions2.add(regionVars[1]);
		occupiedRegions2.add(regionVars[2]);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually H and I and J 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(regionVars[7]);
		occupiedRegions3.add(regionVars[8]);
		occupiedRegions3.add(regionVars[9]);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually the whole swarm in G
		ArrayList<Variable> occupiedRegions4 = new ArrayList<Variable>();
		occupiedRegions4.add(regionVars[6]);
		int collectiveLiveness3 = createRegionPredicate(bdd, regionVars, occupiedRegions4);
		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
//		//always eventually E
//		int collectiveLiveness4 = bdd.ref(regionVars[4].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness4);
//		
//		//always eventually F
//		int collectiveLiveness5 = bdd.ref(regionVars[5].getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness5);
		
		//all the robots must pass through F
		ArrayList<Variable> occupiedRegions5 = new ArrayList<Variable>();
		occupiedRegions5.add(regionVars[5]);
		int collectiveLiveness6 = createRegionPredicate(bdd, regionVars, occupiedRegions5);
		collectiveLivenessGuarantees.add(collectiveLiveness6);
				
				
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
				
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
				
		livenessGuarantees.addAll(individualLivenessGuarantees);
				
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never E and F at the same time
		int notSafe1 = BDDWrapper.and(bdd, regionVars[4].getBDDVar(), regionVars[5].getBDDVar());
		int safety1 = BDDWrapper.not(bdd, notSafe1);
//		safetyGuarantees.add(safety1);
		
		//never (H or I or J) and (A or B or C)
		int tmp1 = BDDWrapper.or(bdd, regionVars[0].getBDDVar(), regionVars[1].getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, regionVars[2].getBDDVar());
		int tmp2 = BDDWrapper.or(bdd, regionVars[7].getBDDVar(), regionVars[8].getBDDVar());
		tmp2 = BDDWrapper.orTo(bdd, tmp2, regionVars[9].getBDDVar());
		
		int notSafe2 = BDDWrapper.and(bdd, tmp1, tmp2);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		BDDWrapper.free(bdd, tmp1);
		BDDWrapper.free(bdd, tmp2);
//		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially swarm in regions A and B and C 
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(regionVars[0]);
		occupiedRegionsInit.add(regionVars[1]);
		occupiedRegionsInit.add(regionVars[2]);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
						
						
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
					synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, 
					init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
						
						
						
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
						
		return result;
	}
	
	private static ArrayList<Variable> createRegionVariables(BDD bdd, Node[] regionNodes, HashMap<Node, Variable> regionToVarMap, HashMap<Variable, Node> varToRegionMap){
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
		for(int i=0; i<regionNodes.length; i++){
			Variable region =  Variable.createVariableAndPrimedVariable(bdd, regionNodes[i].getName());
			variables.add(region);
			regionToVarMap.put(regionNodes[i],region);
			varToRegionMap.put(region, regionNodes[i]);
		}
		
		return variables;
	}
	
	/**
	 * Cornell 5th floor case study
	 * Three offices, and two labs are connected through a corridor
	 * The swarm starts at the right side of the corridor
	 * it must always eventually visit offices and synchronize, and then move to the labs and synchronize
	 * The swarm must not occupy the labs and the offices at the same time
	 * Every robot in the swarm must infinitely often visit storage room in the right side of the corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCornellWorkSpace2(){
		BDD bdd = new BDD(10000, 1000);
		
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
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
	
		
		Variable.printVariables(regionVars);
//		UtilityMethods.getUserInput();
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		
		//all the robots must visit storage room
		ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
		occupiedRegions1.add(storageRoom);
		int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
		individualLivenessGuarantees.add(individualLiveness1);
				
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		
		//always eventually office1 and office2 and office3
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(office1);
		occupiedRegions2.add(office2);
		occupiedRegions2.add(office3);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
//		//always eventually lab1 
//		int collectiveLiveness2 = bdd.ref(lab1.getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness2);
//		
//		//always eventually lab2
//		int collectiveLiveness3 = bdd.ref(lab2.getBDDVar());
//		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
		//always eventually lab1 and lab2 
		ArrayList<Variable> occupiedRegions3 = new ArrayList<Variable>();
		occupiedRegions3.add(lab1);
		occupiedRegions3.add(lab2);
		int collectiveLiveness2 = createRegionPredicate(bdd, regionVars, occupiedRegions3);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		livenessGuarantees.addAll(individualLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never lab1 and (office1 or office2 or office3)
		int tmp = BDDWrapper.or(bdd, office1.getBDDVar(), office2.getBDDVar());
		tmp = BDDWrapper.orTo(bdd, tmp, office3.getBDDVar());
		
		int notSafe1 = BDDWrapper.and(bdd, lab1.getBDDVar(), tmp);
		int safety1 = BDDWrapper.not(bdd, notSafe1);
		safetyGuarantees.add(safety1);
		
		//never lab2 and (office1 or office2 or office3)
		int notSafe2 = BDDWrapper.and(bdd, lab2.getBDDVar(), tmp);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially full swarm in region A
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(A);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
				
				
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
						synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
				
				
				
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
				
		return result;
	}
	
	/**
	 * Cornell 5th floor case study
	 * Three offices, and two labs are connected through a corridor
	 * The swarm starts at the right side of the corridor
	 * it must always eventually visit offices and synchronize, and then move to the labs
	 * The swarm must not occupy the labs and the offices at the same time
	 * Every robot in the swarm must infinitely often visit storage room in the right side of the corridor 
	 * @return
	 */
	public static SwarmIntermediateSpecification createCornellWorkSpace1(){
		BDD bdd = new BDD(10000, 1000);
		
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
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
	
		
//		Variable.printVariables(regionVars);
//		UtilityMethods.getUserInput();
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		
		//all the robots must visit storage room
		ArrayList<Variable> occupiedRegions1 = new ArrayList<Variable>();
		occupiedRegions1.add(storageRoom);
		int individualLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions1);
		individualLivenessGuarantees.add(individualLiveness1);
				
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		
		//always eventually office1 and office2 and office3
		ArrayList<Variable> occupiedRegions2 = new ArrayList<Variable>();
		occupiedRegions2.add(office1);
		occupiedRegions2.add(office2);
		occupiedRegions2.add(office3);
		int collectiveLiveness1 = createRegionPredicate(bdd, regionVars, occupiedRegions2);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		
		//always eventually lab1 
		int collectiveLiveness2 = bdd.ref(lab1.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		//always eventually lab2
		int collectiveLiveness3 = bdd.ref(lab2.getBDDVar());
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		livenessGuarantees.addAll(individualLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never lab1 and (office1 or office2 or office3)
		int tmp = BDDWrapper.or(bdd, office1.getBDDVar(), office2.getBDDVar());
		tmp = BDDWrapper.orTo(bdd, tmp, office3.getBDDVar());
		
		int notSafe1 = BDDWrapper.and(bdd, lab1.getBDDVar(), tmp);
		int safety1 = BDDWrapper.not(bdd, notSafe1);
		safetyGuarantees.add(safety1);
		
		//never lab2 and (office1 or office2 or office3)
		int notSafe2 = BDDWrapper.and(bdd, lab2.getBDDVar(), tmp);
		int safety2 = BDDWrapper.not(bdd, notSafe2);
		safetyGuarantees.add(safety2);
		
		//define initial state
		//initially full swarm in region A
		ArrayList<Variable> occupiedRegionsInit = new ArrayList<Variable>();
		occupiedRegionsInit.add(A);
		int init = createRegionPredicate(bdd, regionVars, occupiedRegionsInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
				
				
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, regionNodes, regionVars, 
						synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
				
				
				
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
				
		return result;
	}
	
	/**
	 * dim*dim board
	 * robots initially start at [dim/2][dim/2]
	 * always eventually all of swarm is in [dim/2][dim/2]
	 * always eventually part of swarm visits [dim/2][0], [0][dim/2], [dim-1][dim/2], [dim-1][dim/2]
	 * always eventually part of swarm visit [0][0],[dim-1][0],[0][dim-1][dim-1][dim-1]
	 * 
	 * @return
	 */
	public static SwarmIntermediateSpecification createPatterns(int dim){
		int h = dim;
		int w = dim;
		int middle = dim/2;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
			}
		}
		
		regionGraph = createLocalGraph(regionNodes);
		
		//define the maps between region graph nodes and region boolean variables
		HashMap<Node, Variable> regionToVarMap = new HashMap<Node, Variable>();
		HashMap<Variable, Node> varToRegionMap = new HashMap<Variable, Node>();
		
		
		//Define variables 
		//define regions
		Variable[][] regions = new Variable[w][h];
		Variable[] regionVars = new Variable[w*h];
		ArrayList<Variable> variables = new ArrayList<Variable>();
		
//		for(int i=0; i<w; i++){
//			for(int j=0; j<h; j++){
//				regions[i][j] = Variable.createVariableAndPrimedVariable(bdd, "region["+i+"]["+j+"]");
//				
////				regions[i][j] = new Variable(bdd, "region["+i+"]["+j+"]");
//				
//				variables.add(regions[i][j]);
//				regionVars[i*h+j] = regions[i][j];
//				
//				regionToVarMap.put(regionNodes[i][j], regions[i][j]);
//				varToRegionMap.put(regions[i][j], regionNodes[i][j]);
//			}
//		}
//		
////		for(int i=0; i<w; i++){
////			for(int j=0; j<h; j++){
////				Variable.createPrimeVariable(bdd, regions[i][j]);
////			}
////		}
		
		//different variable orderings 
		regionVars = createRegionVars(bdd, regionNodes[middle][middle], regionGraph, regionToVarMap, varToRegionMap);
		regions = correspondingVars(regionNodes, regionToVarMap);
		for(int i=0; i<regionVars.length;i++){
			variables.add(regionVars[i]);
		}
		
		
		//define sync variable 
		Variable synchronizeSignal = Variable.createVariableAndPrimedVariable(bdd, "sync");
		variables.add(synchronizeSignal);
	
		
		Variable.printVariables(regionVars);
		UtilityMethods.getUserInput();

		
		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		
		//always eventually all of swarm is in [dim/2][dim/2]
		int collectiveLiveness1 = createFullRegionPredicate(bdd, regions, middle, middle);
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		
		
//		//always eventually part of swarm visits [dim/2][0], [0][dim/2], [dim-1][dim/2], [dim-1][dim/2]
//		int collectiveLiveness2 = BDDWrapper.and(bdd, regions[0][middle].getBDDVar(), regions[middle][0].getBDDVar());
//		int tmp2 = BDDWrapper.and(bdd, regions[dim-1][middle].getBDDVar(), regions[middle][dim-1].getBDDVar());
//		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, tmp2);
//		BDDWrapper.free(bdd, tmp2);
//		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		
		//always eventually part of swarm visit [0][0],[dim-1][0],[0][dim-1][dim-1][dim-1]
//		int collectiveLiveness3 = BDDWrapper.and(bdd, regions[0][0].getBDDVar(), regions[0][dim-1].getBDDVar());
//		int tmp3 = BDDWrapper.and(bdd, regions[dim-1][0].getBDDVar(), regions[dim-1][dim-1].getBDDVar());
//		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, tmp3);
//		BDDWrapper.free(bdd, tmp3);
//		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		
		GridCell2D cell00 = new GridCell2D(0, 0);
		GridCell2D cell0d = new GridCell2D(0, dim-1);
		GridCell2D celld0 = new GridCell2D(dim-1, 0);
		GridCell2D celldd = new GridCell2D(dim-1, dim-1);
		ArrayList<GridCell2D> occupiedRegions3 = new ArrayList<GridCell2D>();
		occupiedRegions3.add(cell00);
		occupiedRegions3.add(cell0d);
		occupiedRegions3.add(celld0);
		occupiedRegions3.add(celldd);
		int collectiveLiveness3 = createRegionPredicate(bdd, regions, occupiedRegions3);
		collectiveLiveness3 = BDDWrapper.andTo(bdd, collectiveLiveness3, synchronizeSignal.getBDDVar());
		
		
		collectiveLivenessGuarantees.add(collectiveLiveness1);
//		collectiveLivenessGuarantees.add(collectiveLiveness2);
		collectiveLivenessGuarantees.add(collectiveLiveness3);
		
//		collectiveLivenessGuarantees.add(region_1_1_full);
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		livenessGuarantees.addAll(individualLivenessGuarantees);
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
//		//never region[1][0] and region[0][1] be occupied at the same time 
//		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][0].getBDDVar());
//		
////		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
////		unsafe = BDDWrapper.andTo(bdd, unsafe, regions[2][1].getBDDVar());
//////		int tmp2 = BDDWrapper.and(bdd, regions[1][0].getBDDVar(), regions[1][1].getBDDVar());
//////		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
//////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
//////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
//////		BDDWrapper.free(bdd, tmp2);
//////		BDDWrapper.free(bdd, tmp3);
////		
//		int safetyObj = BDDWrapper.not(bdd, unsafe);
//		BDDWrapper.free(bdd, unsafe);
//		safetyGuarantees.add(safetyObj);
		
		int safetyObj = bdd.ref(bdd.getOne());
		safetyGuarantees.add(safetyObj);
		

		//define initial state
		//initially full swarm in regions[middle][middle]
		int init = createFullRegionPredicate(bdd, regions, middle, middle);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	/**
	 * Different groups of robots start at different columns 
	 * Robots move in their columns, they must avoid being in the middle row all at the same time
	 * They must infinitely often visit first and last rows 
	 * @param dim
	 * @return
	 */
	public static SwarmIntermediateSpecification createSynchronizationTest(int dim){
		int h = dim;
		int w = dim;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
	
		
		regionGraph = createSimpleGraph(regionNodes);

		

		
		//define objective
		
		ArrayList<Integer> individualLivenessGuarantees = new ArrayList<Integer>();
		
		ArrayList<Integer> collectiveLivenessGuarantees = new ArrayList<Integer>();
		

		//always eventually row 0
		int collectiveLiveness1 = bdd.ref(synchronizeSignal.getBDDVar());
		for(int i=0; i<dim;i++){
			collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, regions[i][0].getBDDVar());
		}
		
		//always eventually last row 
		int collectiveLiveness2 = bdd.ref(synchronizeSignal.getBDDVar());
		for(int i=0; i<dim; i++){
			collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, regions[i][dim-1].getBDDVar());
		}
		
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		livenessGuarantees.addAll(individualLivenessGuarantees);
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		int middle = dim/2;
		
		//never region[1][0] and region[0][1] be occupied at the same time 
//		int unsafe = BDDWrapper.and(bdd, region_0_1_partial, region_1_0_partial);
		
		int unsafe = bdd.ref(bdd.getOne());
		for(int i=0; i<dim; i++){
			unsafe = BDDWrapper.andTo(bdd, unsafe, regions[i][middle].getBDDVar());
		}
		
		int safetyObj = BDDWrapper.not(bdd, unsafe);
		BDDWrapper.free(bdd, unsafe);
		safetyGuarantees.add(safetyObj);
		

		//define initial state
		
		int init = bdd.ref(bdd.not(synchronizeSignal.getBDDVar()));
		for(int i=0; i<dim;i++){
			for(int j=0; j<dim; j++){
				if(j==0){
					init = BDDWrapper.andTo(bdd, init, regions[i][j].getBDDVar());
				}else{
					int emptyRegion = BDDWrapper.not(bdd, regions[i][j].getBDDVar());
					init = BDDWrapper.andTo(bdd, init, emptyRegion);
					BDDWrapper.free(bdd, emptyRegion);
				}
			}
		}

				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	/**
	 * 4*4 board
	 * robots initially start at [0][0]
	 * always eventually part of swarm visits [3][0] and other part [0][3]
	 * the whole swarm visits [3][3] at the same time infinitely often 
	 * @return
	 */
	public static SwarmIntermediateSpecification createSimpleTest5(){
		int h = 5;
		int w = 5;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
	
		
		regionGraph = createLocalGraph(regionNodes);

		

		
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
		
		int collectiveLiveness1 = BDDWrapper.and(bdd, regions[0][h-1].getBDDVar(), regions[w-1][0].getBDDVar());
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		

		int collectiveLiveness2 = createFullRegionPredicate(bdd, regions, w-1, h-1);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		
		//region[size-1][size-1] must be full always eventually
//		int region_1_1_full = createFullRegionPredicate(bdd, regions, size-1, size-1);
//		region_1_1_full = BDDWrapper.andTo(bdd, region_1_1_full, synchronizeSignal.getBDDVar());
		
//		collectiveLivenessGuarantees.add(region_1_0_partial);
//		collectiveLivenessGuarantees.add(region_0_1_partial);
		
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
//		collectiveLivenessGuarantees.add(region_1_1_full);
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		livenessGuarantees.addAll(individualLivenessGuarantees);
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never region[1][0] and region[0][1] be occupied at the same time 
		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][0].getBDDVar());
		
//		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
//		unsafe = BDDWrapper.andTo(bdd, unsafe, regions[2][1].getBDDVar());
////		int tmp2 = BDDWrapper.and(bdd, regions[1][0].getBDDVar(), regions[1][1].getBDDVar());
////		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
////		BDDWrapper.free(bdd, tmp2);
////		BDDWrapper.free(bdd, tmp3);
//		
		int safetyObj = BDDWrapper.not(bdd, unsafe);
		BDDWrapper.free(bdd, unsafe);
		safetyGuarantees.add(safetyObj);
		
//		int safetyObj = bdd.ref(bdd.getOne());
//		safetyGuarantees.add(safetyObj);
		

		//define initial state
		//initially full swarm in regions[0][0]
		int init = createFullRegionPredicate(bdd, regions, 0, 0);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	/**
	 * 4*4 board
	 * robots initially start at [0][0]
	 * always eventually part of swarm visits [3][0] and other part [0][3]
	 * the whole swarm visits [3][3] at the same time infinitely often 
	 * @return
	 */
	public static SwarmIntermediateSpecification createSimpleTest4(){
		int h = 4;
		int w = 4;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
	
		
		regionGraph = createLocalGraph(regionNodes);

		

		
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
		
		int collectiveLiveness1 = BDDWrapper.and(bdd, regions[0][h-1].getBDDVar(), regions[w-1][0].getBDDVar());
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		

		int collectiveLiveness2 = createFullRegionPredicate(bdd, regions, w-1, h-1);
		collectiveLiveness2 = BDDWrapper.andTo(bdd, collectiveLiveness2, synchronizeSignal.getBDDVar());
		
		//region[size-1][size-1] must be full always eventually
//		int region_1_1_full = createFullRegionPredicate(bdd, regions, size-1, size-1);
//		region_1_1_full = BDDWrapper.andTo(bdd, region_1_1_full, synchronizeSignal.getBDDVar());
		
//		collectiveLivenessGuarantees.add(region_1_0_partial);
//		collectiveLivenessGuarantees.add(region_0_1_partial);
		
		collectiveLivenessGuarantees.add(collectiveLiveness1);
		collectiveLivenessGuarantees.add(collectiveLiveness2);
		
//		collectiveLivenessGuarantees.add(region_1_1_full);
		
		ArrayList<Integer> livenessGuarantees = new ArrayList<Integer>();
		livenessGuarantees.addAll(individualLivenessGuarantees);
		livenessGuarantees.addAll(collectiveLivenessGuarantees);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		
		//never region[1][0] and region[0][1] be occupied at the same time 
//		int unsafe = BDDWrapper.and(bdd, region_0_1_partial, region_1_0_partial);
		
//		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
//		unsafe = BDDWrapper.andTo(bdd, unsafe, regions[2][1].getBDDVar());
////		int tmp2 = BDDWrapper.and(bdd, regions[1][0].getBDDVar(), regions[1][1].getBDDVar());
////		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
////		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
////		BDDWrapper.free(bdd, tmp2);
////		BDDWrapper.free(bdd, tmp3);
//		
//		int safetyObj = BDDWrapper.not(bdd, unsafe);
//		BDDWrapper.free(bdd, unsafe);
//		safetyGuarantees.add(safetyObj);
		
		int safetyObj = bdd.ref(bdd.getOne());
		safetyGuarantees.add(safetyObj);
		

		//define initial state
		//initially full swarm in regions[0][0]
		int init = createFullRegionPredicate(bdd, regions, 0, 0);
		init = BDDWrapper.andTo(bdd, init, bdd.not(synchronizeSignal.getBDDVar()));				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	public static SwarmIntermediateSpecification createSimpleTest3(){
		int h = 3;
		int w = 3;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
	
		
		regionGraph = createSimpleGraph(regionNodes);

		

		
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
		
		int collectiveLiveness = BDDWrapper.and(bdd, regions[0][2].getBDDVar(), regions[1][2].getBDDVar());
		collectiveLiveness = BDDWrapper.andTo(bdd, collectiveLiveness, regions[2][2].getBDDVar());
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
		
		int unsafe = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
		unsafe = BDDWrapper.andTo(bdd, unsafe, regions[2][1].getBDDVar());
//		int tmp2 = BDDWrapper.and(bdd, regions[1][0].getBDDVar(), regions[1][1].getBDDVar());
//		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
//		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
//		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
//		BDDWrapper.free(bdd, tmp2);
//		BDDWrapper.free(bdd, tmp3);
		
		int safetyObj = BDDWrapper.not(bdd, unsafe);
		BDDWrapper.free(bdd, unsafe);
		safetyGuarantees.add(safetyObj);
		

		//define initial state
		//initially full swarm in regions[0][0]
//		int region_0_0_full = createFullRegionPredicate(bdd, regions, 0, 0);
//		int init = BDDWrapper.and(bdd, region_0_0_full, bdd.not(synchronizeSignal.getBDDVar()));
		
		int region_0_0_partial = bdd.ref(regions[0][0].getBDDVar());
		int region_0_1_empty = BDDWrapper.not(bdd, regions[0][1].getBDDVar());
		int region_1_1_empty = BDDWrapper.not(bdd, regions[1][1].getBDDVar());
		int region_2_1_empty = BDDWrapper.not(bdd, regions[2][1].getBDDVar());
		int region_0_2_empty = BDDWrapper.not(bdd, regions[0][2].getBDDVar());
		int region_1_2_empty = BDDWrapper.not(bdd, regions[1][2].getBDDVar());
		int region_2_2_empty = BDDWrapper.not(bdd, regions[2][2].getBDDVar());
		
		int init = BDDWrapper.and(bdd,region_0_0_partial, regions[1][0].getBDDVar());
		init = BDDWrapper.andTo(bdd, init, regions[2][0].getBDDVar());
		init = BDDWrapper.andTo(bdd, init, region_0_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_1_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_2_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_0_2_empty);
		init = BDDWrapper.andTo(bdd, init, region_1_2_empty);
		init = BDDWrapper.andTo(bdd, init, region_2_2_empty);
		init = BDDWrapper.andTo(bdd,init,bdd.not(synchronizeSignal.getBDDVar()));
		BDDWrapper.free(bdd, region_0_0_partial);
		BDDWrapper.free(bdd, region_0_1_empty);
		BDDWrapper.free(bdd, region_1_1_empty);
		BDDWrapper.free(bdd, region_2_1_empty);
		BDDWrapper.free(bdd, region_0_2_empty);
		BDDWrapper.free(bdd, region_1_2_empty);
		BDDWrapper.free(bdd, region_2_2_empty);
				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	public static SwarmIntermediateSpecification createSimpleTest2(){
		int h = 2;
		int w = 2;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
	
		
		regionGraph = createSimpleGraph(regionNodes);

		

		
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
//		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, regions[2][0].getBDDVar());
		collectiveLiveness1 = BDDWrapper.andTo(bdd, collectiveLiveness1, synchronizeSignal.getBDDVar());
		
		int collectiveLiveness = BDDWrapper.and(bdd, regions[0][1].getBDDVar(), regions[1][1].getBDDVar());
//		collectiveLiveness = BDDWrapper.andTo(bdd, collectiveLiveness, regions[2][1].getBDDVar());
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
//		int tmp3 = BDDWrapper.and(bdd, regions[2][0].getBDDVar(), regions[2][1].getBDDVar());
		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp2);
//		unsafe = BDDWrapper.andTo(bdd, unsafe, tmp3);
		BDDWrapper.free(bdd, tmp2);
//		BDDWrapper.free(bdd, tmp3);
		
		int safetyObj = BDDWrapper.not(bdd, unsafe);
		BDDWrapper.free(bdd, unsafe);
		safetyGuarantees.add(safetyObj);
		

		//define initial state
		//initially full swarm in regions[0][0]
//		int region_0_0_full = createFullRegionPredicate(bdd, regions, 0, 0);
//		int init = BDDWrapper.and(bdd, region_0_0_full, bdd.not(synchronizeSignal.getBDDVar()));
		
		int region_0_0_partial = bdd.ref(regions[0][0].getBDDVar());
		int region_0_1_empty = BDDWrapper.not(bdd, regions[0][1].getBDDVar());
		int region_1_1_empty = BDDWrapper.not(bdd, regions[1][1].getBDDVar());
//		int region_2_1_empty = BDDWrapper.not(bdd, regions[2][1].getBDDVar());
		
		int init = BDDWrapper.and(bdd,region_0_0_partial, regions[1][0].getBDDVar());
//		init = BDDWrapper.andTo(bdd, init, regions[2][0].getBDDVar());
		init = BDDWrapper.andTo(bdd, init, region_0_1_empty);
		init = BDDWrapper.andTo(bdd, init, region_1_1_empty);
//		init = BDDWrapper.andTo(bdd, init, region_2_1_empty);
		init = BDDWrapper.andTo(bdd,init,bdd.not(synchronizeSignal.getBDDVar()));
		BDDWrapper.free(bdd, region_0_0_partial);
		BDDWrapper.free(bdd, region_0_1_empty);
		BDDWrapper.free(bdd, region_1_1_empty);
//		BDDWrapper.free(bdd, region_2_1_empty);
				
		
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	public static SwarmIntermediateSpecification createSimpleTest1(){
		int h = 2;
		int w = 3;
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		Node[][] regionNodes = new Node[w][h];
		Node[] allRegionNodes = new Node[w*h];
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				regionNodes[i][j] = new Node("region_"+i+"_"+j);
				allRegionNodes[i*h+j] = regionNodes[i][j];
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
		
//		Variable[] vars = UtilityMethods.variableArrayListToArray(variables); 
		
		//Create transition relation for swarm movement
//		int trans = createSwarmTransitionRelationSquareWorkSpace(bdd, size, regions, regionNodes, regionGraph);
		
		regionGraph = createSimpleGraph(regionNodes);
//		int trans = directedGraphToTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
//		printTransitionRelation(bdd, trans, regionNodes.length*regionNodes[0].length);
//		UtilityMethods.getUserInput();
		
		
		
		//Create a ControllableSinglePlayerDeterministicTransitionRelation
//		ControllableSinglePlayerDeterministicGameStructure swarmGame = new ControllableSinglePlayerDeterministicGameStructure(bdd, vars, trans, null);
				
		
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
		
//		ArrayList<Integer> livenessAssumptions = new ArrayList<Integer>();
//		livenessAssumptions.add(bdd.ref(bdd.getOne()));
//		
//		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), livenessAssumptions, livenessGuarantees, null, safetyGuarantees);
		
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
				
		
		
		
//		swarmGame.setInit(init);
		
//		System.out.println("Checking the input");
//		System.out.println("Printing the variables");
//		Variable.printVariables(vars);
//		
//		System.out.println("objectives are ");
//		System.out.println("individual liveness");
//		for(int i=0; i<individualLivenessGuarantees.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "liveness guarantee "+i, individualLivenessGuarantees.get(i));
//		}
//		
//		System.out.println("collective liveness");
//		for(int i=0; i<collectiveLivenessGuarantees.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "liveness guarantee "+i, collectiveLivenessGuarantees.get(i));
//		}
//		
//		System.out.println("safety objectives ");
//		for(int i=0; i<safetyGuarantees.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "safety guarantee "+i, safetyGuarantees.get(i));
//		}
//		
//		UtilityMethods.debugBDDMethods(bdd, "init", init);
//		
//		System.out.println("End of input");
//		UtilityMethods.getUserInput();
		
		SwarmIntermediateSpecification result = new SwarmIntermediateSpecification(bdd, allRegionNodes, regionVars, 
				synchronizeSignal, regionToVarMap, varToRegionMap, regionGraph, init, safetyGuarantees, collectiveLivenessGuarantees, individualLivenessGuarantees);
		
		result.setRegions2D(regionNodes);
		
		result.print();
		System.out.println();
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	private static DirectedGraph<String, Node, Edge<Node>> createSimpleGraph(Node[][] regionNodes){
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		for(int i=0;i<regionNodes.length;i++){
			for(int j=0; j<regionNodes[i].length-1;j++){
				Edge<Node> e1 = new Edge<Node>(regionNodes[i][j], regionNodes[i][j+1]);
				regionGraph.addEdge(e1);
				Edge<Node> e2 = new Edge<Node>(regionNodes[i][j+1], regionNodes[i][j]);
				regionGraph.addEdge(e2);
			}

		}
		
		return regionGraph;
	}
	
	/**
	 * each node is connected to its neighbors
	 * @param regionNodes
	 * @return
	 */
	private static DirectedGraph<String, Node, Edge<Node>> createLocalGraph(Node[][] regionNodes){
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
		
		int numOfRows = regionNodes.length;
		int numOfColumns = regionNodes[0].length;
		
		for(int i=0;i<regionNodes.length;i++){
			for(int j=0; j<regionNodes[i].length;j++){
				if(i+1<numOfRows){
					Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i+1][j]);
					regionGraph.addEdge(e);
				}
				if(i>0){
					Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i-1][j]);
					regionGraph.addEdge(e);
				}
				if(j+1<numOfColumns){
					Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i][j+1]);
					regionGraph.addEdge(e);
				}
				if(j>0){
					Edge<Node> e = new Edge<Node>(regionNodes[i][j], regionNodes[i][j-1]);
					regionGraph.addEdge(e);
				}
			}

		}
		
		return regionGraph;
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
	
	

}
