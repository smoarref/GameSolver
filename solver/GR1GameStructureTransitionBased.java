package solver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;
import reactiveSwarm.ReactiveSwarmAlgorithmTester;
import specification.WorkSpace;
import game.BDDWrapper;
import game.GameSolution;
import game.GameStructure;
import game.Variable;
import jdd.bdd.BDD;
import utils.UtilityMethods;

public class GR1GameStructureTransitionBased extends GameStructure{
	protected int transitionRelation = -1;

	public GR1GameStructureTransitionBased(BDD argBdd) {
		super(argBdd);
	}
	
	public GR1GameStructureTransitionBased(BDD argBDD, Variable[] inputVars, Variable[] outputVars, int argT_env, int argT_sys){
		super(argBDD);
		init = -1;
		
		//define variables
		inputVariables = inputVars;
		inputPrimeVariables = Variable.getPrimedCopy(inputVars);
		outputVariables = outputVars;
		outputPrimeVariables = Variable.getPrimedCopy(outputVars);
		variables = Variable.unionVariables(inputVariables, outputVariables);
		primedVariables = Variable.getPrimedCopy(variables);
		
		//define permutations
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		inputVarsCube = -1;
		outputVarsCube = -1;
		primeInputVarsCube = -1;
		primeOutputVarsCube = -1;
		variablesCube=-1;
		primeVariablesCube=-1;
		
		//define actions 
		envActionVars = null;
		sysActionVars = null;
		actionVars=null;
		
		//define transition relations
		T_env = bdd.ref(argT_env);
		T_sys = bdd.ref(argT_sys);
		
		
	}
	
	public int getTransitionRelation(){
		if(transitionRelation == -1){
			transitionRelation = BDDWrapper.and(bdd, getEnvironmentTransitionRelation(), getSystemTransitionRelation());
		}
		return transitionRelation;
	}
	
	public int transitionsEndingInGoalStates(int goalStates){
		int goalStatesPrime = BDDWrapper.replace(bdd, goalStates, getVtoVprime());
		int result = BDDWrapper.and(bdd, getTransitionRelation(), goalStatesPrime);
		BDDWrapper.free(bdd, goalStatesPrime);
		return result;
	}
	
	public int turnIntoTransitionFormula(int formula){
		int goal = formula; 
		//is it a set of states?
		int tmp = BDDWrapper.exists(bdd, formula, getPrimeVariablesCube());
		if(tmp==formula){
			goal = transitionsEndingInGoalStates(formula);
		}else{
			BDDWrapper.free(bdd, tmp);
		}
		return goal;
	}
	
	public int controllablePredecessor(int set){
		if(set == 0){
			return bdd.ref(bdd.getZero());
		}
		
		int goal = set; 
		
		//is it a set of states?
		int tmp = BDDWrapper.exists(bdd, set, getPrimeVariablesCube());
		if(tmp==set){
			goal = transitionsEndingInGoalStates(set);
		}else{
			BDDWrapper.free(bdd, tmp);
		}
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "goal set is ", goal);
		
		int epre = BDDWrapper.and(bdd, getSystemTransitionRelation(), goal);
		int e = BDDWrapper.exists(bdd, epre, getPrimeOutputVariablesCube());
		BDDWrapper.free(bdd, epre);
		int t = BDDWrapper.implies(bdd, getEnvironmentTransitionRelation(), e);
		
		
		int cpre = BDDWrapper.forall(bdd, t, getPrimeInputVarsCube());
		BDDWrapper.free(bdd, t);
		return cpre;
	}
	
	public int symbolicGameOneStepExecution(int set){
//		System.out.println("executing symbolic step");
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "set is ", set);
////		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is ", getTransitionRelation());
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "transition relation intersecting with set is", BDDWrapper.and(bdd, set, getTransitionRelation()));
		return BDDWrapper.EpostImage(bdd, set, getVariablesCube(), getTransitionRelation(), getVprimetoV());
	}
	
	
	public int simulate(int state){
		int next = symbolicGameOneStepExecution(state);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "next states ", next);
		
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, next, variables);
		int randomIndex = UtilityMethods.randomIndex(minterms.size());
		int chosenState = BDDWrapper.mintermToBDDFormula(bdd, minterms.get(randomIndex), variables);
		return chosenState;
	}
	
	public static void main(String[] args){
		
		testCorridorExample();
		
//		testCornellExample();
		
//		testSimpleSwarmExample();
		
//		testPaperExample();
		
//		testMove2();
		
//		testMove();
		
//		Vasu_example();
		
//		testExtendedSolver();
		
//		test();
		
		//test1
//		test1();
		
//		testThreeRegionsTwoSCCs();
	}
	
	
	/**
	 * Two regions A and B, move back and forth between them
	 */
	private static void testSimpleSwarmExample(){
		long t0 = UtilityMethods.timeStamp();
		BDD bdd = new BDD(10000, 1000);
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		Variable B = Variable.createVariableAndPrimedVariable(bdd, "B");
		
		Variable AA = Variable.createVariableAndPrimedVariable(bdd, "AA");
		Variable AB = Variable.createVariableAndPrimedVariable(bdd, "AB");
		Variable BA = Variable.createVariableAndPrimedVariable(bdd, "BA");
		Variable BB = Variable.createVariableAndPrimedVariable(bdd, "BB");
		
		Variable[] inputVars = new Variable[]{A,B};
		Variable[] outputVars = new Variable[]{AA, AB, BA, BB};
		
		//define transition relation
		//A \wedge AA -> A'
		int T_env = swarmTransition(bdd, A, AA, new Variable[]{A});
		//A \wedge AB -> A' \vee B'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, A, AB, new Variable[]{A,B}));
		//A \wedge \neg (AA \vee AB) -> A'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, A, new Variable[]{AA,AB}));
		
		
		//B \wedge BB -> B'
		T_env = BDDWrapper.andTo(bdd, T_env,swarmTransition(bdd, B, BB, new Variable[]{B}));
		//B \wedge BA -> A' \vee B'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, B, BA, new Variable[]{A,B}));
		//B \wedge \neg (BA \vee BB) -> B'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, B, new Variable[]{BA,BB}));
		
		
		//A' -> A \vee (B \wedge BA)
		int tmp1 = BDDWrapper.and(bdd, B.getBDDVar(), BA.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, A.getBDDVar());
		int AprimeTrans = BDDWrapper.implies(bdd, A.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, AprimeTrans);
		BDDWrapper.free(bdd, AprimeTrans);
				
		//B' -> B \vee (A \wedge AB)
		tmp1 = BDDWrapper.and(bdd, A.getBDDVar(), AB.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, B.getBDDVar());
		int BprimeTrans = BDDWrapper.implies(bdd, B.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, BprimeTrans);
		BDDWrapper.free(bdd, BprimeTrans);
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outputVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		//initially the whole swarm in D
		int init = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(B.getBDDVar()));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		//always eventually A \wedge AB -> B'
		int a_AB = swarmTransition(bdd, A, AB, new Variable[]{B});
		assumptions.add(a_AB);
		int a_BA = swarmTransition(bdd, B, BA, new Variable[]{A});
		assumptions.add(a_BA);
		
		//A'
		int tmp = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(BA.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, AB.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, bdd.not(AA.getBDDVar()));
		int a_Aprime = BDDWrapper.implies(bdd, tmp, bdd.not(A.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Aprime);
		
		//C'
		tmp = BDDWrapper.and(bdd, B.getBDDVar(), bdd.not(AB.getBDDVar()));
		tmp = BDDWrapper.andTo(bdd, tmp, BA.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, bdd.not(BB.getBDDVar()));
		int a_Bprime = BDDWrapper.implies(bdd, tmp, bdd.not(B.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Bprime);
		
		for(int i=0; i<assumptions.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption "+i,assumptions.get(i));
		}
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually the whole swarm in D
		int g1 = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(B.getBDDVar()));
		guarantees.add(g1);
		
		int g2 = BDDWrapper.and(bdd, B.getBDDVar(), bdd.not(A.getBDDVar()));
		guarantees.add(g2);
		
		for(int i=0; i<guarantees.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantees "+i, guarantees.get(i));
		}
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "game formed in ");
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.duration(t0, "game solved in ");
		UtilityMethods.getUserInput();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("simulation #"+i);
			runRandomSimulations(bdd, strat, 10);
		}
	}
	
	private static DirectedGraph<String, Node, Edge<Node>> createCorridorRegionGraph(){
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
		
		return regionGraph;
	}
	
	private static void testCorridorExample(){
		long t0 = UtilityMethods.timeStamp();
		BDD bdd = new BDD(10000, 1000);
		
		System.out.println("experiment started at "+new Date().toString());
		
		DirectedGraph<String, Node, Edge<Node>> regionGraph = createCorridorRegionGraph();
		
		//define variables
		
		//define the input variables 
		Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
				
		//obtain the workspace
		WorkSpace ws = new WorkSpace(bdd, regionGraph);
				
		Variable[] regionVars = ws.getRegionVars();
				
		
				
		System.out.println("region variables are ");
		Variable.printVariables(regionVars);
				
		System.out.println("input variables are ");
		Variable.printVariables(inputVars);
		
		//define action vars 
		ArrayList<Variable> actionVariables = new ArrayList<Variable>();
		
		
		
		//stay put actions 
		for(Node n : regionGraph.getNodes()){
			Variable nn = Variable.createVariableAndPrimedVariable(bdd, n.getName()+n.getName());
			actionVariables.add(nn);
		}
		//transition actions 
		for(Edge<Node> e : regionGraph.getAllEdges()){
			Variable fromTo = Variable.createVariableAndPrimedVariable(bdd,e.getFrom().getName()+e.getTarget().getName());
			actionVariables.add(fromTo);
		}
	
		Variable[] actionVars = actionVariables.toArray(new Variable[actionVariables.size()]);
		System.out.println("action variables are");
		Variable.printVariables(actionVars);
		
		System.out.println("num of actions vars are "+actionVars.length);
		
		UtilityMethods.getUserInput();
		
		//define environment transition relation
		System.out.println("defining the environment transition relation");
		int T_env = bdd.ref(bdd.getOne());
		for(Node n : regionGraph.getNodes()){
			//self transition: r & rr -> r'
			Variable regionVar = Variable.getVariable(regionVars, n.getName());
			Variable act = Variable.getVariable(actionVars, n.getName()+n.getName());
			Variable[] next = new Variable[]{regionVar};
			
//			System.out.println("current node is "+n.getName());
//			regionVar.print();
//			act.print();
			
			int selfTrans = swarmTransition(bdd, regionVar, act, next);
			T_env = BDDWrapper.andTo(bdd, T_env, selfTrans);
			BDDWrapper.free(bdd, selfTrans);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "self trans is", selfTrans);
			
			ArrayList<Variable> allActions = new ArrayList<Variable>();
			allActions.add(act);
			
			//trans to other regions 
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				
//				System.out.println("neighbor node is "+v);
				
				//r & rv -> r' | v' 
				act = Variable.getVariable(actionVars, n.getName()+v);
				
//				System.out.println("action is");
//				act.print();
				
//				System.out.println("next variable is ");
//				Variable.getVariable(regionVars, v).print();
				
				allActions.add(act);
				next = new Variable[]{regionVar,Variable.getVariable(regionVars, v)};
				int trans = swarmTransition(bdd, regionVar, act, next);
				
//				UtilityMethods.debugBDDMethods(bdd, "trans is ", trans);
				
				T_env = BDDWrapper.andTo(bdd, T_env, trans);
				BDDWrapper.free(bdd, trans);
			}
			
			//r & !allactions -> r'
			Variable[] allActionArray = allActions.toArray(new Variable[allActions.size()]);
			int preserve = partitionPreservation(bdd, regionVar, allActionArray);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "preserve is ", preserve);
			
			T_env = BDDWrapper.andTo(bdd, T_env, preserve);
			BDDWrapper.free(bdd, preserve);
			
//			System.out.println("next transitions");
			
			//r' -> r | (v & vr)
			int left = regionVar.getPrimedCopy().getBDDVar();
			int right = regionVar.getBDDVar();
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r -> r' | (v & vr)  
				
				Variable vRegion = Variable.getVariable(regionVars, v);
				Variable vr = Variable.getVariable(actionVars, v+n.getName());
				
//				System.out.println("next region is");
//				vRegion.print();
//				
//				System.out.println("next act is");
//				vr.print();
				
				//v & vr 
				int v_and_vr = BDDWrapper.and(bdd, vRegion.getBDDVar(), vr.getBDDVar());
				right = BDDWrapper.orTo(bdd, right, v_and_vr);
				BDDWrapper.free(bdd, v_and_vr);
			}
			int nextTrans = BDDWrapper.implies(bdd, left, right);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "next trans is ", nextTrans);
			
			BDDWrapper.free(bdd, left);
			BDDWrapper.free(bdd, right);
			T_env = BDDWrapper.andTo(bdd, T_env, nextTrans);
		}
		
		//define system transition relation
		int T_sys = bdd.ref(bdd.getOne());
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
//		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		Variable[] inputVariables = Variable.unionVariables(regionVars, inputVars);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVariables,actionVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		//initially the whole swarm is in B
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(regionVars[1]);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
		init = BDDWrapper.andTo(bdd, init, inp0);
							
		UtilityMethods.debugBDDMethods(bdd,"init is", init); 
		
		//define liveness assumptions 
		
		System.out.println("defining liveness assumptions");
		
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		for(Node n : regionGraph.getNodes()){
			String r = n.getName();
			Variable rVar = Variable.getVariable(regionVars, r);
			
//			System.out.println("current node is "+r);
			
//			ArrayList<Variable> allActions = new ArrayList<Variable>();
			Variable rr = Variable.getVariable(actionVars, r+r);
			
			int moveOutAct = bdd.ref(bdd.getZero());
			
			//move a part to a region
			List<Edge<Node>> outgoing = regionGraph.getAdjacent(n);
			for(Edge<Node> e : outgoing){
				String v = e.getTarget().getName();
				//r & rv ->  v' 
				
				Variable act = Variable.getVariable(actionVars, r+v);
				Variable[] next = new Variable[]{Variable.getVariable(regionVars, v)};
				
//				System.out.println("act is ");
//				act.print();
//				
//				System.out.println("next is");
//				Variable.getVariable(regionVars, v).print();
				
				int assump = swarmTransition(bdd, rVar, act, next);
				assumptions.add(assump);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption is", assump);
				
				moveOutAct = BDDWrapper.orTo(bdd, moveOutAct, act.getBDDVar());
				
//				allActions.add(act);
			}
			
//			ArrayList<Variable> incomingActs = new ArrayList<Variable>();
//			incomingActs.add(rr);
			
			int incomingActs = bdd.ref(bdd.not(rr.getBDDVar()));
			
			for(Node n2 : regionGraph.getNodes()){
				List<Edge<Node>> incomingEdges = regionGraph.getAdjacent(n2);
				for(Edge<Node> e : incomingEdges){
					if(e.getTarget() == n){
						Variable inAct = Variable.getVariable(actionVars, n2.getName()+r);
//						incomingActs.add(inAct);
						
//						System.out.println("inact is");
//						inAct.print();
						
						incomingActs = BDDWrapper.andTo(bdd, incomingActs, bdd.ref(bdd.not(inAct.getBDDVar())));
					}
				}
			}
			
			
			//move a part out of region 
			//r & !rr & !vr & ... & (rv | ... ) -> !r'
			int left = BDDWrapper.and(bdd, rVar.getBDDVar(), incomingActs);
			left = BDDWrapper.andTo(bdd, left, moveOutAct);
			int right = bdd.ref(bdd.not(rVar.getPrimedCopy().getBDDVar()));
			
			int assump = BDDWrapper.implies(bdd, left, right);
			BDDWrapper.free(bdd, left);
			BDDWrapper.free(bdd, right);
			assumptions.add(assump);
			
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "evacuation assumption is ", assump);
		}
		
		//define liveness guarantees 
		//guarantees
		//always eventually if i=0 then the whole swarm in A, C
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(regionVars[0]);
		l1OccupiedRegions.add(regionVars[2]);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int g1 = BDDWrapper.implies(bdd, inp0, l1);
				
		//always eventually if i=1 then the whole swarm in A, C, J, K
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(regionVars[0]);
		l2OccupiedRegions.add(regionVars[2]);
		l2OccupiedRegions.add(regionVars[9]);
		l2OccupiedRegions.add(regionVars[10]);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int inp1 = BDDWrapper.assign(bdd, 1, inputVars);
		int g2 = BDDWrapper.implies(bdd, inp1, l2);
				
		//always eventually if i=2 then the whole swarm in J, K
		ArrayList<Variable> l3OccupiedRegions = new ArrayList<Variable>();
		l3OccupiedRegions.add(regionVars[9]);
		l3OccupiedRegions.add(regionVars[10]);
		int l3 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l3OccupiedRegions);
		int inp2 = BDDWrapper.assign(bdd, 2, inputVars);
		int g3 = BDDWrapper.implies(bdd, inp2, l3);
				
		//always eventually if i=3 then the whole swarm in D,E,G,H
		ArrayList<Variable> l4OccupiedRegions = new ArrayList<Variable>();
		l4OccupiedRegions.add(regionVars[3]);
		l4OccupiedRegions.add(regionVars[4]);		
		l4OccupiedRegions.add(regionVars[6]);
		l4OccupiedRegions.add(regionVars[7]);
		int l4 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l4OccupiedRegions);
		int inp3 = BDDWrapper.assign(bdd, 3, inputVars);
		int g4 = BDDWrapper.implies(bdd, inp3, l4);
				
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(g1);
		guarantees.add(g2);
		guarantees.add(g3);
		guarantees.add(g4);
					
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		System.out.println("num of variables "+(inputVariables.length+actionVars.length));
		System.out.println("num of assumptions "+assumptions.size());
		UtilityMethods.getUserInput();
		
		
		//solve the game 
		System.out.println("starting to solve the game, current time is "+new Date().toString());
		t0 = UtilityMethods.timeStamp();
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		UtilityMethods.duration(t0, "game solved in ");
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("*********************************************************");
			System.out.println("simulation #"+i);
			UtilityMethods.getUserInput();
			runRandomSimulations(bdd, strat, 20);
		}
	}
	
	private static void testCornellExample(){
		
		long t0 = UtilityMethods.timeStamp();
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		//define regions 
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		Variable Corridor = Variable.createVariableAndPrimedVariable(bdd, "Corridor");
		Variable Lab1 = Variable.createVariableAndPrimedVariable(bdd, "Lab1");
		Variable Lab2 = Variable.createVariableAndPrimedVariable(bdd, "Lab2");
		Variable Office1 = Variable.createVariableAndPrimedVariable(bdd, "Office1");
		Variable Office2 = Variable.createVariableAndPrimedVariable(bdd, "Office2");
		Variable Office3 = Variable.createVariableAndPrimedVariable(bdd, "Office3");
		Variable StorageRoom = Variable.createVariableAndPrimedVariable(bdd, "StorageRoom");
		Variable inp = Variable.createVariableAndPrimedVariable(bdd, "inp"); 
		
		//define system actions
		Variable AA = Variable.createVariableAndPrimedVariable(bdd, "AA");
		Variable AC = Variable.createVariableAndPrimedVariable(bdd, "AC");
		Variable CC = Variable.createVariableAndPrimedVariable(bdd, "CC");
		Variable CA = Variable.createVariableAndPrimedVariable(bdd, "CA");
		Variable CL1 = Variable.createVariableAndPrimedVariable(bdd, "CL1");
		Variable CL2 = Variable.createVariableAndPrimedVariable(bdd, "CL2");
		Variable CO1 = Variable.createVariableAndPrimedVariable(bdd, "CO1");
		Variable CO2 = Variable.createVariableAndPrimedVariable(bdd, "CO2");
		Variable CO3 = Variable.createVariableAndPrimedVariable(bdd, "CO3");
		Variable CS = Variable.createVariableAndPrimedVariable(bdd, "CS");
		Variable L1L1 = Variable.createVariableAndPrimedVariable(bdd, "L1L1");
		Variable L1C = Variable.createVariableAndPrimedVariable(bdd, "L1C");
		Variable L2L2 = Variable.createVariableAndPrimedVariable(bdd, "L2L2");
		Variable L2C = Variable.createVariableAndPrimedVariable(bdd, "L2C");
		Variable O1O1 = Variable.createVariableAndPrimedVariable(bdd, "O1O1");
		Variable O1C = Variable.createVariableAndPrimedVariable(bdd, "O1C");
		Variable O2O2 = Variable.createVariableAndPrimedVariable(bdd, "O2O2");
		Variable O2C = Variable.createVariableAndPrimedVariable(bdd, "O2C");
		Variable O3O3 = Variable.createVariableAndPrimedVariable(bdd, "O3O3");
		Variable O3C = Variable.createVariableAndPrimedVariable(bdd, "O3C");
		Variable SS = Variable.createVariableAndPrimedVariable(bdd, "SS");
		Variable SC = Variable.createVariableAndPrimedVariable(bdd, "SC");
		
		Variable[] regionVars = new Variable[]{A,Corridor,Lab1,Lab2,Office1,Office2,Office3,StorageRoom};
		Variable[] inputVars = new Variable[]{A,Corridor,Lab1,Lab2,Office1,Office2,Office3,StorageRoom,inp};
		Variable[] outputVars = new Variable[]{AA,AC,CC,CA,CL1,CL2,CO1,CO2,CO3,CS,L1L1,L1C, L2L2,L2C,O1O1,
				O1C,O2O2,O2C,O3O3,O3C,SS,SC};
		
		//define transition relation
		
		//A
		//A \wedge AA -> A'
		int T_env = swarmTransition(bdd, A, AA, new Variable[]{A});
		//A \wedge AC -> A' \vee C'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, A, AC, new Variable[]{A,Corridor}));
		//A \wedge \neg (AA \vee AC) -> A'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, A, new Variable[]{AA,AC}));
		
		//Corridor
		//Corridor \wedge CA -> A' \vee Corridor' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CA, new Variable[]{A,Corridor}));
		//Corridor \wedge CC -> Corridor' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CC, new Variable[]{Corridor}));
		//Corridor \wedge CL1 -> Corridor' \vee Lab1'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CL1, new Variable[]{Lab1,Corridor}));
		//Corridor \wedge CL2 -> Corridor' \vee Lab2'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CL2, new Variable[]{Lab2,Corridor}));
		//Corridor \wedge CO1 -> Corridor' \vee Office1'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CO1, new Variable[]{Office1,Corridor}));
		//Corridor \wedge CO2 -> Corridor' \vee Office2'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CO2, new Variable[]{Office2,Corridor}));
		//Corridor \wedge CO3 -> Corridor' \vee Office3'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CO3, new Variable[]{Office3,Corridor}));
		//Corridor \wedge CS -> Corridor' \vee StorageRoo,'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Corridor, CS, new Variable[]{StorageRoom,Corridor}));
		//Corridor \wedge \neg (CA \vee CC \vee CL1 \vee CL2 \vee CO1 \vee CO2 \vee CO3 \vee CS) -> Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Corridor, new Variable[]{CA,CC,CL1,CL2,CO1,CO2,CO3,CS}));
		
		//Lab1
		//Lab1 \wedge L1L1 -> Lab1'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Lab1, L1L1, new Variable[]{Lab1}));
		//Lab1 \wedge L1C -> Lab1' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Lab1, L1C, new Variable[]{Lab1,Corridor}));
		//Lab1 \wedge \neg (L1L1 \vee L1C) -> Lab1'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Lab1, new Variable[]{L1L1,L1C}));
		
		//Lab2
		//Lab2 \wedge L2L2 -> Lab2'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Lab2, L2L2, new Variable[]{Lab2}));
		//Lab2 \wedge L2C -> Lab2' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Lab2, L2C, new Variable[]{Lab2,Corridor}));
		//Lab2 \wedge \neg (L2L2 \vee L2C) -> Lab2'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Lab2, new Variable[]{L2L2,L2C}));
		
		//Office1
		//Office1 \wedge O1O1 -> Office1'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office1, O1O1, new Variable[]{Office1}));
		//Office1 \wedge O1C -> Office1' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office1, O1C, new Variable[]{Office1,Corridor}));
		//Office1 \wedge \neg (O1O1 \vee O1C) -> Office1'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Office1, new Variable[]{O1O1,O1C}));
		
		//Office2
		//Office2 \wedge O2O2 -> Office2'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office2, O2O2, new Variable[]{Office2}));
		//Office2 \wedge O2C -> Office2' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office2, O2C, new Variable[]{Office2,Corridor}));
		//Office2 \wedge \neg (O2O2 \vee O2C) -> Office2'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Office2, new Variable[]{O2O2,O2C}));
		
		//Office3
		//Office3 \wedge O3O3 -> Office3'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office3, O3O3, new Variable[]{Office3}));
		//Office3 \wedge O3C -> Office3' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, Office3, O3C, new Variable[]{Office3,Corridor}));
		//Office3 \wedge \neg (O3O3 \vee O3C) -> Office3'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, Office3, new Variable[]{O3O3,O3C}));
		
		//Storage room
		//StorageRoom \wedge SS -> StorageRoom'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, StorageRoom, SS, new Variable[]{StorageRoom}));
		//StorageRoom \wedge SC -> StorageRoom' \vee Corridor'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, StorageRoom, SC, new Variable[]{StorageRoom,Corridor}));
		//StorageRoom \wedge \neg (SS \vee SC) -> StorageRoom'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, StorageRoom, new Variable[]{SS,SC}));
		
		
		//A' -> A \vee (Corridor \wedge CA)
		int tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CA.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, A.getBDDVar());
		int AprimeTrans = BDDWrapper.implies(bdd, A.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, AprimeTrans);
		BDDWrapper.free(bdd, AprimeTrans);
		
		//C' -> C \vee (A & AC) \vee (L1 & L1C) \vee (L2 & L2C) \vee (O1 & O1C) \vee (O2 & O2C) \vee (O3 & O3C) \vee
		//(S & SC) 
		tmp1 = Corridor.getBDDVar();
		int tmp2 = BDDWrapper.and(bdd, A.getBDDVar(), AC.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, Lab1.getBDDVar(), L1C.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, Lab2.getBDDVar(), L2C.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, Office1.getBDDVar(), O1C.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, Office2.getBDDVar(), O2C.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, Office3.getBDDVar(), O3C.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		tmp2 = BDDWrapper.and(bdd, StorageRoom.getBDDVar(), SC.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		int Corridor_primeTrans = BDDWrapper.implies(bdd, Corridor.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Corridor_primeTrans);
		BDDWrapper.free(bdd, Corridor_primeTrans);
		
		//Lab1' -> Lab1 \vee (Corridor \wedge CL1)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CL1.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, Lab1.getBDDVar());
		int Lab1primeTrans = BDDWrapper.implies(bdd, Lab1.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Lab1primeTrans);
		BDDWrapper.free(bdd, Lab1primeTrans);
		
		//Lab2' -> Lab2 \vee (Corridor \wedge CL2)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CL2.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, Lab2.getBDDVar());
		int Lab2primeTrans = BDDWrapper.implies(bdd, Lab2.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Lab2primeTrans);
		BDDWrapper.free(bdd, Lab2primeTrans);
		
		//Office1' -> Office1 \vee (Corridor \wedge CO1)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CO1.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, Office1.getBDDVar());
		int Office1primeTrans = BDDWrapper.implies(bdd, Office1.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Office1primeTrans);
		BDDWrapper.free(bdd, Office1primeTrans);
		
		//Office2' -> Office2 \vee (Corridor \wedge CO2)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CO2.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, Office2.getBDDVar());
		int Office2primeTrans = BDDWrapper.implies(bdd, Office2.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Office2primeTrans);
		BDDWrapper.free(bdd, Office2primeTrans);
		
		//Office3' -> Office3 \vee (Corridor \wedge CO3)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CO3.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, Office3.getBDDVar());
		int Office3primeTrans = BDDWrapper.implies(bdd, Office3.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, Office3primeTrans);
		BDDWrapper.free(bdd, Office3primeTrans);
		
		//StorageRoom' -> StorageRoom \vee (Corridor \wedge CS)
		tmp1 = BDDWrapper.and(bdd, Corridor.getBDDVar(), CS.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, StorageRoom.getBDDVar());
		int StorageRoom_primeTrans = BDDWrapper.implies(bdd, StorageRoom.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, StorageRoom_primeTrans);
		BDDWrapper.free(bdd, StorageRoom_primeTrans);
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outputVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		//initially the whole swarm in D
		int init = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(Corridor.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(Lab1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(Lab2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(Office1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(Office2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(Office3.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(StorageRoom.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CL1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CL2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CO1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CO2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CO3.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CS.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(L1L1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(L1C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(L2L2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(L2C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O1O1.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O1C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O2O2.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O2C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O3O3.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(O3C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(SS.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(SC.getBDDVar()));
		
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		//always eventually A \wedge AD -> D'
		int a_AC = swarmTransition(bdd, A, AC, new Variable[]{Corridor});
		assumptions.add(a_AC);
		int a_CA = swarmTransition(bdd, Corridor, CA, new Variable[]{A});
		assumptions.add(a_CA);
		int a_CL1 = swarmTransition(bdd, Corridor, CL1, new Variable[]{Lab1});
		assumptions.add(a_CL1);
		int a_L1C = swarmTransition(bdd, Lab1, L1C, new Variable[]{Corridor});
		assumptions.add(a_L1C);
		int a_CL2 = swarmTransition(bdd, Corridor, CL2, new Variable[]{Lab2});
		assumptions.add(a_CL2);
		int a_L2C = swarmTransition(bdd, Lab2, L2C, new Variable[]{Corridor});
		assumptions.add(a_L2C);
		int a_CO1 = swarmTransition(bdd, Corridor, CO1, new Variable[]{Office1});
		assumptions.add(a_CO1);
		int a_O1C = swarmTransition(bdd, Office1, O1C, new Variable[]{Corridor});
		assumptions.add(a_O1C);
		int a_CO2 = swarmTransition(bdd, Corridor, CO2, new Variable[]{Office2});
		assumptions.add(a_CO2);
		int a_O2C = swarmTransition(bdd, Office2, O2C, new Variable[]{Corridor});
		assumptions.add(a_O2C);
		int a_CO3 = swarmTransition(bdd, Corridor, CO3, new Variable[]{Office3});
		assumptions.add(a_CO3);
		int a_O3C = swarmTransition(bdd, Office3, O3C, new Variable[]{Corridor});
		assumptions.add(a_O3C);
		int a_CS = swarmTransition(bdd, Corridor, CS, new Variable[]{StorageRoom});
		assumptions.add(a_CS);
		int a_SC = swarmTransition(bdd, StorageRoom, SC, new Variable[]{Corridor});
		assumptions.add(a_SC);
				
		//A'
		int tmp = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(AA.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CA.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, AC.getBDDVar());
		int a_Aprime = BDDWrapper.implies(bdd, tmp, bdd.not(A.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Aprime);
				
		//C'
		tmp = BDDWrapper.and(bdd, Corridor.getBDDVar(), bdd.not(CC.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(AC.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, L1C.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, L2C.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, O1C.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, O2C.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, O3C.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, SC.getBDDVar());
		tmp1 = BDDWrapper.or(bdd, CA.getBDDVar(), CL1.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, CL2.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, CO1.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, CO2.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, CO3.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, CS.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, tmp1);
		BDDWrapper.free(bdd, tmp1);
		int a_Cprime = BDDWrapper.implies(bdd, tmp, bdd.not(Corridor.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Cprime);
				
		//L1'
		tmp = BDDWrapper.and(bdd, Lab1.getBDDVar(), bdd.not(L1L1.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CL1.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, L1C.getBDDVar());
		int a_L1prime = BDDWrapper.implies(bdd, tmp, bdd.not(Lab1.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_L1prime);
		
		//L2'
		tmp = BDDWrapper.and(bdd, Lab2.getBDDVar(), bdd.not(L2L2.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CL2.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, L2C.getBDDVar());
		int a_L2prime = BDDWrapper.implies(bdd, tmp, bdd.not(Lab2.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_L2prime);
		
		//O1'
		tmp = BDDWrapper.and(bdd, Office1.getBDDVar(), bdd.not(O1O1.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CO1.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, O1C.getBDDVar());
		int a_O1prime = BDDWrapper.implies(bdd, tmp, bdd.not(Office1.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_O1prime);
		
		//O2'
		tmp = BDDWrapper.and(bdd, Office2.getBDDVar(), bdd.not(O2O2.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CO2.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, O2C.getBDDVar());
		int a_O2prime = BDDWrapper.implies(bdd, tmp, bdd.not(Office2.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_O2prime);
		
		//O3'
		tmp = BDDWrapper.and(bdd, Office3.getBDDVar(), bdd.not(O3O3.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CO3.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, O3C.getBDDVar());
		int a_O3prime = BDDWrapper.implies(bdd, tmp, bdd.not(Office3.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_O3prime);
		
		//S'
		tmp = BDDWrapper.and(bdd, StorageRoom.getBDDVar(), bdd.not(SS.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CS.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, SC.getBDDVar());
		int a_Sprime = BDDWrapper.implies(bdd, tmp, bdd.not(StorageRoom.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Sprime);
				
		for(int i=0; i<assumptions.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption "+i,assumptions.get(i));
		}
		
		//guarantees
		//always eventually the whole swarm in office1, office2, and office3
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(Office1);
		l1OccupiedRegions.add(Office2);
		l1OccupiedRegions.add(Office3);
		int l1 = createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
				
		//always eventually if the target is in Lba1, then the swarm isolates it
		//GF inp -> Lab1 and Lab2 and Corridor
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(Lab1);
		l2OccupiedRegions.add(Lab2);
		l2OccupiedRegions.add(Corridor);
		int l2regions = createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int l2 = BDDWrapper.implies(bdd, inp.getBDDVar(), l2regions);
		BDDWrapper.free(bdd, l2regions);
				
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(l1);
		guarantees.add(l2);
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		System.out.println("num of variables "+(inputVars.length+outputVars.length));
		System.out.println("num of assumptions "+assumptions.size());
		UtilityMethods.getUserInput();
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("*********************************************************");
			System.out.println("simulation #"+i);
			UtilityMethods.getUserInput();
			runRandomSimulations(bdd, strat, 20);
		}
		
	}
	
	/**
	 * initially in region D
	 * always eventually gather in D
	 * if the environment input is on, then always eventually gather the swarm in A and C
	 * 
	 */
	private static void testPaperExample(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		//define regions 
		Variable D = Variable.createVariableAndPrimedVariable(bdd, "D");
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		Variable C = Variable.createVariableAndPrimedVariable(bdd, "C");
		Variable inp_B = Variable.createVariableAndPrimedVariable(bdd, "inp_B"); 
		
		//define system actions
		Variable DD = Variable.createVariableAndPrimedVariable(bdd, "DD");
		Variable DA = Variable.createVariableAndPrimedVariable(bdd, "DA");
		Variable DC = Variable.createVariableAndPrimedVariable(bdd, "DC");
		Variable AA = Variable.createVariableAndPrimedVariable(bdd, "AA");
		Variable AD = Variable.createVariableAndPrimedVariable(bdd, "AD");
		Variable CC = Variable.createVariableAndPrimedVariable(bdd, "CC");
		Variable CD = Variable.createVariableAndPrimedVariable(bdd, "CD");
		
		Variable[] inputVars = new Variable[]{D,A,C,inp_B};//
		Variable[] outputVars = new Variable[]{DD,DA,DC,AA,AD,CC,CD};
		
		//define transition relation
		//A \wedge AA -> A'
		int T_env = swarmTransition(bdd, A, AA, new Variable[]{A});
		//A \wedge AD -> A' \vee D'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, A, AD, new Variable[]{A,D}));
		//A \wedge \neg (AA \vee AD) -> A'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, A, new Variable[]{AA,AD}));
		//D \wedge DA -> A' \vee D' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, D, DA, new Variable[]{A,D}));
		//D \wedge DD -> D' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, D, DD, new Variable[]{D}));
		//D \wedge \neg (DD \vee DA \vee DC) -> D'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, D, new Variable[]{DD,DA,DC}));
		//D \wedge DC -> C' \vee D' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, D, DC, new Variable[]{C,D}));
		//C \wedge CC -> C'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, C, CC, new Variable[]{C}));
		//C \wedge CD -> C' \vee D'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, C, CD, new Variable[]{C,D}));
		//C \wedge \neg (CC \vee CD) -> C'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, C, new Variable[]{CC,CD}));
		
		//A' -> A \vee (D \wedge DA)
		int tmp1 = BDDWrapper.and(bdd, D.getBDDVar(), DA.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, A.getBDDVar());
		int AprimeTrans = BDDWrapper.implies(bdd, A.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, AprimeTrans);
		BDDWrapper.free(bdd, AprimeTrans);
		
		//C' -> C \vee (D \wedge DC)
		tmp1 = BDDWrapper.and(bdd, D.getBDDVar(), DC.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, C.getBDDVar());
		int CprimeTrans = BDDWrapper.implies(bdd, C.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, CprimeTrans);
		BDDWrapper.free(bdd, CprimeTrans);
		
		//D' -> D \vee (A \wedge AD) \vee (C \wedge CD)
		tmp1 = BDDWrapper.and(bdd, A.getBDDVar(), AD.getBDDVar());
		int tmp2 = BDDWrapper.and(bdd, C.getBDDVar(), CD.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, D.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		int DprimeTrans = BDDWrapper.implies(bdd, D.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, DprimeTrans);
		BDDWrapper.free(bdd, DprimeTrans);
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outputVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		//initially the whole swarm in D
		int init = BDDWrapper.and(bdd, D.getBDDVar(), bdd.not(A.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(C.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp_B.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(DD.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(DC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(DA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AD.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CD.getBDDVar()));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		//always eventually A \wedge AD -> D'
		int a_AD = swarmTransition(bdd, A, AD, new Variable[]{D});
		assumptions.add(a_AD);
		int a_CD = swarmTransition(bdd, C, CD, new Variable[]{D});
		assumptions.add(a_CD);
		int a_DA = swarmTransition(bdd, D, DA, new Variable[]{A});
		assumptions.add(a_DA);
		int a_DC = swarmTransition(bdd, D, DC, new Variable[]{C});
		assumptions.add(a_DC);
		
		//A'
		int tmp = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(AA.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(DA.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, AD.getBDDVar());
		int a_Aprime = BDDWrapper.implies(bdd, tmp, bdd.not(A.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Aprime);
		
		//C'
		tmp = BDDWrapper.and(bdd, C.getBDDVar(), bdd.not(CC.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(DC.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, CD.getBDDVar());
		int a_Cprime = BDDWrapper.implies(bdd, tmp, bdd.not(C.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Cprime);
		
		//D'
		tmp = BDDWrapper.and(bdd, D.getBDDVar(), bdd.not(DD.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(AD.getBDDVar())); 
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CD.getBDDVar())); 
		tmp1 = BDDWrapper.or(bdd, DA.getBDDVar(), DC.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, tmp1);
		BDDWrapper.free(bdd, tmp1);
		int a_Dprime = BDDWrapper.implies(bdd, tmp, bdd.not(D.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Dprime);
		
		for(int i=0; i<assumptions.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption "+i,assumptions.get(i));
		}
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually the whole swarm in D
		int g1 = BDDWrapper.and(bdd, D.getBDDVar(), bdd.not(A.getBDDVar()));
		g1 = BDDWrapper.andTo(bdd, g1, bdd.not(C.getBDDVar()));
		guarantees.add(g1);
		
		//always eventually inp_B -> A \wedge C \wedge \neg D
		int g2tmp = BDDWrapper.and(bdd, A.getBDDVar(), C.getBDDVar());
		g2tmp = BDDWrapper.andTo(bdd, g2tmp, bdd.not(D.getBDDVar()));
//		guarantees.add(g2tmp);
		int g2 = BDDWrapper.implies(bdd, inp_B.getBDDVar(), g2tmp);
		BDDWrapper.free(bdd, g2tmp);
		guarantees.add(g2);
		
		for(int i=0; i<guarantees.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantees "+i, guarantees.get(i));
		}
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("*********************************************************");
			System.out.println("simulation #"+i);
			UtilityMethods.getUserInput();
			runRandomSimulations(bdd, strat, 20);
		}
		
	}
	
	/**
	 * region graph A<->B<->C
	 * Input: i=0<->i=1->i=2<->i=3
	 * initially whole swarm in region B
	 * always eventually gather ((i=0 | 1)&A_full) | ((i=2|3)& C_full)
	 * 
	 */
	private static void testThreeRegionsTwoSCCs(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		//define regions 
		Variable A = Variable.createVariableAndPrimedVariable(bdd, "A");
		Variable B = Variable.createVariableAndPrimedVariable(bdd, "B");
		Variable C = Variable.createVariableAndPrimedVariable(bdd, "C");
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
		
		//define system actions
		Variable BB = Variable.createVariableAndPrimedVariable(bdd, "BB");
		Variable BA = Variable.createVariableAndPrimedVariable(bdd, "BA");
		Variable BC = Variable.createVariableAndPrimedVariable(bdd, "BC");
		Variable AA = Variable.createVariableAndPrimedVariable(bdd, "AA");
		Variable AB = Variable.createVariableAndPrimedVariable(bdd, "AB");
		Variable CC = Variable.createVariableAndPrimedVariable(bdd, "CC");
		Variable CB = Variable.createVariableAndPrimedVariable(bdd, "CB");
		
		Variable[] inputVars = new Variable[]{A,B,C,input[0],input[1]};//
		Variable[] outputVars = new Variable[]{BB,BA,BC,AA,AB,CC,CB};
		
		//define transition relation
		//A \wedge AA -> A'
		int T_env = swarmTransition(bdd, A, AA, new Variable[]{A});
		//A \wedge AB -> A' \vee B'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, A, AB, new Variable[]{A,B}));
		//A \wedge \neg (AA \vee AB) -> A'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, A, new Variable[]{AA,AB}));
		//B \wedge BA -> A' \vee B' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, B, BA, new Variable[]{A,B}));
		//B \wedge BB -> B' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, B, BB, new Variable[]{B}));
		//B \wedge \neg (BB \vee BA \vee BC) -> B'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, B, new Variable[]{BB,BA,BC}));
		//B \wedge BC -> C' \vee B' 
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, B, BC, new Variable[]{C,B}));
		//C \wedge CC -> C'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, C, CC, new Variable[]{C}));
		//C \wedge CB -> C' \vee B'
		T_env = BDDWrapper.andTo(bdd, T_env, swarmTransition(bdd, C, CB, new Variable[]{C,B}));
		//C \wedge \neg (CC \vee CB) -> C'
		T_env = BDDWrapper.andTo(bdd, T_env, partitionPreservation(bdd, C, new Variable[]{CC,CB}));
		
		//A' -> A \vee (B \wedge BA)
		int tmp1 = BDDWrapper.and(bdd, B.getBDDVar(), BA.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, A.getBDDVar());
		int AprimeTrans = BDDWrapper.implies(bdd, A.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, AprimeTrans);
		BDDWrapper.free(bdd, AprimeTrans);
		
		//C' -> C \vee (B \wedge DC)
		tmp1 = BDDWrapper.and(bdd, B.getBDDVar(), BC.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, C.getBDDVar());
		int CprimeTrans = BDDWrapper.implies(bdd, C.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, CprimeTrans);
		BDDWrapper.free(bdd, CprimeTrans);
		
		//B' -> B \vee (A \wedge AB) \vee (C \wedge CB)
		tmp1 = BDDWrapper.and(bdd, A.getBDDVar(), AB.getBDDVar());
		int tmp2 = BDDWrapper.and(bdd, C.getBDDVar(), CB.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, B.getBDDVar());
		tmp1 = BDDWrapper.orTo(bdd, tmp1, tmp2);
		BDDWrapper.free(bdd, tmp2);
		int DprimeTrans = BDDWrapper.implies(bdd, B.getPrimedCopy().getBDDVar(), tmp1);
		BDDWrapper.free(bdd, tmp1);
		T_env = BDDWrapper.andTo(bdd, T_env, DprimeTrans);
		BDDWrapper.free(bdd, DprimeTrans);
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outputVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		//initially the whole swarm in B
		int init = BDDWrapper.and(bdd, B.getBDDVar(), bdd.not(A.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(C.getBDDVar()));
		int inputInit = BDDWrapper.assign(bdd, 0, input);
		init = BDDWrapper.andTo(bdd, init, inputInit);
		BDDWrapper.free(bdd, inputInit);
		init = BDDWrapper.andTo(bdd, init, bdd.not(BB.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(BC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(BA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AA.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(AB.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CC.getBDDVar()));
		init = BDDWrapper.andTo(bdd, init, bdd.not(CB.getBDDVar()));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		//always eventually A \wedge AB -> B'
		int a_AB = swarmTransition(bdd, A, AB, new Variable[]{B});
		assumptions.add(a_AB);
		int a_CB = swarmTransition(bdd, C, CB, new Variable[]{B});
		assumptions.add(a_CB);
		int a_BA = swarmTransition(bdd, B, BA, new Variable[]{A});
		assumptions.add(a_BA);
		int a_BC = swarmTransition(bdd, B, BC, new Variable[]{C});
		assumptions.add(a_BC);
		
		//A'
		int tmp = BDDWrapper.and(bdd, A.getBDDVar(), bdd.not(AA.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(BA.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, AB.getBDDVar());
		int a_Aprime = BDDWrapper.implies(bdd, tmp, bdd.not(A.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Aprime);
		
		//C'
		tmp = BDDWrapper.and(bdd, C.getBDDVar(), bdd.not(CC.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(BC.getBDDVar())); 
		tmp = BDDWrapper.andTo(bdd, tmp, CB.getBDDVar());
		int a_Cprime = BDDWrapper.implies(bdd, tmp, bdd.not(C.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Cprime);
		
		//B'
		tmp = BDDWrapper.and(bdd, B.getBDDVar(), bdd.not(BB.getBDDVar()));
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(AB.getBDDVar())); 
		tmp=BDDWrapper.andTo(bdd, tmp, bdd.not(CB.getBDDVar())); 
		tmp1 = BDDWrapper.or(bdd, BA.getBDDVar(), BC.getBDDVar());
		tmp = BDDWrapper.andTo(bdd, tmp, tmp1);
		BDDWrapper.free(bdd, tmp1);
		int a_Dprime = BDDWrapper.implies(bdd, tmp, bdd.not(B.getPrimedCopy().getBDDVar()));
		BDDWrapper.free(bdd, tmp);
		assumptions.add(a_Dprime);
		
		for(int i=0; i<assumptions.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption "+i,assumptions.get(i));
		}
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//GF(((inp=2 | 3) & C_full) | ((inp=0 | 1) & A_full))
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(C);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, new Variable[]{A,B,C}, l1OccupiedRegions);
		int input2 = BDDWrapper.assign(bdd, 2, input);
		int input3 = BDDWrapper.assign(bdd, 3, input);
		int in2or3 = BDDWrapper.or(bdd, input2, input3);
		BDDWrapper.free(bdd, input2);
		BDDWrapper.free(bdd, input3);
//		int g1 = BDDWrapper.and(bdd, in2or3, l1);
		int g1 = BDDWrapper.implies(bdd, in2or3, l1);
		BDDWrapper.free(bdd, l1);
		BDDWrapper.free(bdd,in2or3);
		guarantees.add(g1);
				
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, new Variable[]{A,B,C}, l2OccupiedRegions);
		int input0 = BDDWrapper.assign(bdd, 0, input);
		int input1 = BDDWrapper.assign(bdd, 1, input);
		int in0or1 = BDDWrapper.or(bdd, input0, input1);
		BDDWrapper.free(bdd, input0);
		BDDWrapper.free(bdd, input1);
//		g1 = BDDWrapper.orTo(bdd, g1, tmpg);
		int g2 = BDDWrapper.implies(bdd, in0or1, l2);
		BDDWrapper.free(bdd, l2);
		BDDWrapper.free(bdd,in0or1);
		guarantees.add(g2);
		
		
		
		for(int i=0; i<guarantees.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantees "+i, guarantees.get(i));
		}
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("*********************************************************");
			System.out.println("simulation #"+i);
			UtilityMethods.getUserInput();
			runRandomSimulations(bdd, strat, 20);
		}
		
	}
	
	/**
	 * If no action is applied to a partition, it remains the same
	 * region & !(act_1 | ... | act_k) -> region'
	 * @param bdd
	 * @param region
	 * @param actions
	 * @return
	 */
	private static int partitionPreservation(BDD bdd, Variable region, Variable[] actions){
		int tmp = bdd.ref(bdd.getZero());
		for(Variable act : actions){
			tmp=BDDWrapper.orTo(bdd, tmp, act.getBDDVar());
		}
		int notTmp = BDDWrapper.not(bdd, tmp);
		BDDWrapper.free(bdd, tmp);
		tmp = BDDWrapper.and(bdd, region.getBDDVar(), notTmp);
		int result = BDDWrapper.implies(bdd, tmp, region.getPrimedCopy().getBDDVar());
		BDDWrapper.free(bdd, tmp);
		BDDWrapper.free(bdd, notTmp);
		return result;
	}
	
	private static int swarmTransition(BDD bdd, Variable from, Variable action, Variable[] next){
		int tmp = BDDWrapper.and(bdd, from.getBDDVar(), action.getBDDVar());
		int tmp2 = bdd.ref(bdd.getZero());
		for(int i=0;i<next.length;i++){
			tmp2 = BDDWrapper.orTo(bdd, tmp2, next[i].getPrimedCopy().getBDDVar());
		}
		int result = BDDWrapper.implies(bdd, tmp, tmp2);
		BDDWrapper.free(bdd, tmp);
		BDDWrapper.free(bdd, tmp2);
		return result;
	}
	
	/**
	 * move between rooms 1 and 2
	 */
	private static void testMove2(){
		BDD bdd = new BDD(10000, 1000);
		
		
		//r=0 --> room 1, r=1 --> room 2
		Variable r = Variable.createVariableAndPrimedVariable(bdd, "r");
		
		Variable move = Variable.createVariableAndPrimedVariable(bdd, "move");
		
		Variable[] inputVars = new Variable[]{r};
		Variable[] outpuVars = new Variable[]{move};
		
		//define transition relations 
		//if in room2, stays in room 2
		int same = BDDWrapper.biimp(bdd, r.getBDDVar(), r.getPrimedCopy().getBDDVar());
		int notMove = BDDWrapper.implies(bdd, BDDWrapper.not(bdd, move.getBDDVar()), same);
		int T_env = notMove;
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outpuVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		int init = BDDWrapper.not(bdd, r.getBDDVar());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
//		int a1_1 = BDDWrapper.and(bdd, move.getBDDVar(), r.getPrimedCopy().getBDDVar());
//		int a1_2 = BDDWrapper.and(bdd, move.getBDDVar(), BDDWrapper.not(bdd, move.getPrimedCopy().getBDDVar()));
//		int a1 = BDDWrapper.or(bdd, a1_1, a1_2);
//		BDDWrapper.free(bdd, a1_1);
//		BDDWrapper.free(bdd, a1_2);
		
		int movingOutOf_r1 = BDDWrapper.and(bdd, move.getBDDVar(), BDDWrapper.not(bdd, r.getBDDVar()));
		int a1 = BDDWrapper.implies(bdd, movingOutOf_r1, r.getPrimedCopy().getBDDVar());
		
		int movingOutOf_r2 = BDDWrapper.and(bdd, move.getBDDVar(), r.getBDDVar());
		int a2 = BDDWrapper.implies(bdd, movingOutOf_r2, BDDWrapper.not(bdd, r.getPrimedCopy().getBDDVar()));
		
		assumptions.add(a1);
		assumptions.add(a2);

		for(int i=0; i<assumptions.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption "+i,assumptions.get(i));
		}
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually in r1
		int g1 = bdd.ref(bdd.not(r.getBDDVar()));
		//always eventually in r2
		int g2 = bdd.ref(r.getBDDVar());
		guarantees.add(g1);
		guarantees.add(g2);
		
		for(int i=0; i<guarantees.size();i++){
			UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantees "+i, guarantees.get(i));
		}
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
//		System.out.println("symbolic execution from the initial state");
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states are", strat.getInit());
//		
//		
//		
//		int sym1 = strat.symbolicGameOneStepExecution(init);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over init", sym1);
//		
//		int sym2 = strat.symbolicGameOneStepExecution(sym1);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over sym1", sym2);
		
//		System.out.println("first simulation");
//		runRandomSimulations(bdd, strat, 5);
//		
//		System.out.println("second simulation");
//		runRandomSimulations(bdd, strat, 5);
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("simulation #"+i);
			UtilityMethods.getUserInput();
			runRandomSimulations(bdd, strat, 10);
		}
		
	}
	
	/**
	 * move from room r1 to r2
	 */
	private static void testMove(){
		BDD bdd = new BDD(10000, 1000);
		
		
		//r=0 --> room 1, r=1 --> room 2
		Variable r = Variable.createVariableAndPrimedVariable(bdd, "r");
		
		Variable move = Variable.createVariableAndPrimedVariable(bdd, "move");
		
		Variable[] inputVars = new Variable[]{r};
		Variable[] outpuVars = new Variable[]{move};
		
		//define transition relations 
		//if in room2, stays in room 2
		int same = BDDWrapper.biimp(bdd, r.getBDDVar(), r.getPrimedCopy().getBDDVar());
		int notMove = BDDWrapper.implies(bdd, BDDWrapper.not(bdd, move.getBDDVar()), same);
		int T_env = BDDWrapper.implies(bdd, r.getBDDVar(), r.getPrimedCopy().getBDDVar());
		T_env = BDDWrapper.andTo(bdd, T_env, notMove);
		
		int T_sys = bdd.ref(bdd.getOne());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outpuVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
		int init = BDDWrapper.not(bdd, r.getBDDVar());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
//		int a1_1 = BDDWrapper.and(bdd, move.getBDDVar(), r.getPrimedCopy().getBDDVar());
//		int a1_2 = BDDWrapper.and(bdd, move.getBDDVar(), BDDWrapper.not(bdd, move.getPrimedCopy().getBDDVar()));
//		int a1 = BDDWrapper.or(bdd, a1_1, a1_2);
//		BDDWrapper.free(bdd, a1_1);
//		BDDWrapper.free(bdd, a1_2);
		
		int a1 = BDDWrapper.implies(bdd, move.getBDDVar(), r.getPrimedCopy().getBDDVar());
		
		assumptions.add(a1);


		UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption",a1);
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually in r2
		int g1 = bdd.ref(r.getBDDVar());
		guarantees.add(g1);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee", g1);
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
//		System.out.println("symbolic execution from the initial state");
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states are", strat.getInit());
//		
//		
//		
//		int sym1 = strat.symbolicGameOneStepExecution(init);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over init", sym1);
//		
//		int sym2 = strat.symbolicGameOneStepExecution(sym1);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over sym1", sym2);
		
//		System.out.println("first simulation");
//		runRandomSimulations(bdd, strat, 5);
//		
//		System.out.println("second simulation");
//		runRandomSimulations(bdd, strat, 5);
		
		int numOfSimulations = 3;
		for(int i=0; i<numOfSimulations; i++){
			System.out.println("simulation #"+i);
			runRandomSimulations(bdd, strat, 5);
		}
		
	}
	
	private static void runRandomSimulations(BDD bdd, GR1GameStructureTransitionBased gs, int numOfSteps){
		int chosenInit = UtilityMethods.chooseStateRandomly(bdd, gs.getInit(), gs.variables);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen initial state is ", chosenInit);
		
		int currentState = chosenInit;
		for(int i=0; i<numOfSteps; i++){
			System.out.println("step "+i);
			int nextState = gs.simulate(currentState);
			UtilityMethods.debugBDDMethodsAndWait(bdd, "next state is", nextState);
			BDDWrapper.free(bdd, currentState);
			currentState = nextState;
		}
	}
	
	/**
	 * simplified example
	 * moving from one room to another
	 */
	private static void Vasu_example(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables 
		Variable r1c = Variable.createVariableAndPrimedVariable(bdd, "r1c");
		Variable r2c = Variable.createVariableAndPrimedVariable(bdd, "r2c");
		Variable r1 = Variable.createVariableAndPrimedVariable(bdd, "r1");
		Variable r2 = Variable.createVariableAndPrimedVariable(bdd, "r2");
		
		Variable[] inputVars = new Variable[]{r1c,r2c};
		Variable[] outpuVars = new Variable[]{r1,r2};
		
		//define transition relation 
		int phi_r1 = BDDWrapper.and(bdd, r1.getBDDVar(), BDDWrapper.not(bdd, r2.getBDDVar()));
		int phi_r2 = BDDWrapper.and(bdd, r2.getBDDVar(), BDDWrapper.not(bdd, r1.getBDDVar()));		
		int phi_r1_prime = BDDWrapper.and(bdd, r1.getPrimedCopy().getBDDVar(), BDDWrapper.not(bdd, r2.getPrimedCopy().getBDDVar()));
		int phi_r2_prime = BDDWrapper.and(bdd, r2.getPrimedCopy().getBDDVar(), BDDWrapper.not(bdd, r1.getPrimedCopy().getBDDVar()));

		
		int T_env = bdd.ref(bdd.getOne());
		int nonEmpty = BDDWrapper.or(bdd, r1c.getBDDVar(), r2c.getBDDVar());
		
		int nonEmptyPrime = BDDWrapper.or(bdd, r1c.getPrimedCopy().getBDDVar(), r2c.getPrimedCopy().getBDDVar());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "non empty", nonEmpty);
		
		
		int mutex = bdd.ref(bdd.biimp(r1c.getBDDVar(), bdd.not(r2c.getBDDVar())));
		
		int mutexPrime = bdd.ref(bdd.biimp(r1c.getPrimedCopy().getBDDVar(), bdd.not(r2c.getPrimedCopy().getBDDVar())));
		
		UtilityMethods.debugBDDMethods(bdd, "mutex", mutex);
		
		int r1trans = BDDWrapper.and(bdd, r1c.getBDDVar(), phi_r1);
		int r1t = BDDWrapper.implies(bdd, r1trans, r1c.getPrimedCopy().getBDDVar());
		int r2trans = BDDWrapper.and(bdd, r2c.getBDDVar(), phi_r2);
		int r2t = BDDWrapper.implies(bdd, r2trans, r2c.getPrimedCopy().getBDDVar());
		T_env = BDDWrapper.and(bdd, nonEmpty, mutex);
		T_env = BDDWrapper.andTo(bdd, T_env, nonEmptyPrime);
		T_env = BDDWrapper.andTo(bdd, T_env, mutexPrime);
		UtilityMethods.debugBDDMethods(bdd, "non empty mutex", T_env);
		
		BDDWrapper.free(bdd, nonEmpty);
		BDDWrapper.free(bdd, mutex);
		BDDWrapper.free(bdd, nonEmptyPrime);
		BDDWrapper.free(bdd, mutexPrime);
		T_env = BDDWrapper.andTo(bdd, T_env, r1t);
		T_env = BDDWrapper.andTo(bdd, T_env, r2t);
		BDDWrapper.free(bdd, r1trans);
		BDDWrapper.free(bdd, r2trans);
		BDDWrapper.free(bdd, r1t);
		BDDWrapper.free(bdd, r2t);
		
		int T_sys = BDDWrapper.or(bdd, phi_r1, phi_r2);
		BDDWrapper.free(bdd, phi_r1);
		BDDWrapper.free(bdd, phi_r2);
		
		int phi_prime = BDDWrapper.or(bdd, phi_r1_prime, phi_r2_prime);
		T_sys = BDDWrapper.andTo(bdd, T_sys, phi_prime);
		BDDWrapper.free(bdd, phi_prime);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_env is", T_env);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_sys is", T_sys);
		
		//define game structure 
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars,outpuVars,T_env,T_sys);
		
		System.out.println("game structure is created!");
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "gs transition relation", gs.getTransitionRelation());
		
		//define init 
//		int init = bdd.ref(r1c.getBDDVar());
		int init = BDDWrapper.and(bdd, r1c.getBDDVar(), BDDWrapper.not(bdd, r2c.getBDDVar()));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states ", init);
		
		//define objectives 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
		int not_phi_r1 = BDDWrapper.not(bdd, phi_r1);
		int not_phi_r1_prime = BDDWrapper.not(bdd, phi_r1_prime);
		int not_phi_r2 = BDDWrapper.not(bdd, phi_r2);
		int not_phi_r2_prime = BDDWrapper.not(bdd, phi_r2_prime);
		
//		int a1_1 = BDDWrapper.and(bdd, r1.getBDDVar(), r1c.getPrimedCopy().getBDDVar());
//		int a1_2 = BDDWrapper.and(bdd, r2.getBDDVar(), r2c.getPrimedCopy().getBDDVar());
//		int a1_3 = BDDWrapper.and(bdd, r1.getBDDVar(), BDDWrapper.not(bdd, r1.getPrimedCopy().getBDDVar()));
//		int a1_4 = BDDWrapper.and(bdd, r2.getBDDVar(), BDDWrapper.not(bdd, r2.getPrimedCopy().getBDDVar()));
		
		int a1_1 = BDDWrapper.and(bdd, phi_r1, r1c.getPrimedCopy().getBDDVar());
		int a1_2 = BDDWrapper.and(bdd, phi_r2, r2c.getPrimedCopy().getBDDVar());
		int a1_3 = BDDWrapper.and(bdd, phi_r1, BDDWrapper.not(bdd, phi_r1_prime));
		int a1_4 = BDDWrapper.and(bdd, phi_r2, BDDWrapper.not(bdd, phi_r2_prime));
		
		int a1 = BDDWrapper.or(bdd, a1_1, a1_2);
		a1 = BDDWrapper.orTo(bdd, a1, a1_3);
		a1 = BDDWrapper.orTo(bdd, a1, a1_4);
		assumptions.add(a1);
		BDDWrapper.free(bdd, a1_1);
		BDDWrapper.free(bdd, a1_2);
		BDDWrapper.free(bdd, a1_3);
		BDDWrapper.free(bdd, a1_4);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "assumption",a1);
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually in r2
		int g1 = bdd.ref(r2c.getBDDVar());
		guarantees.add(g1);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee", g1);
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//solve the game 
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning states are", sol.getWinningSystemStates());
		
		GR1GameStructureTransitionBased strat = (GR1GameStructureTransitionBased) sol.strategyOfTheWinner();
		
		System.out.println("symbolic execution from the initial state");
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states are", strat.getInit());
		
		
		
		int sym1 = strat.symbolicGameOneStepExecution(init);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over init", sym1);
		
		int sym2 = strat.symbolicGameOneStepExecution(sym1);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over sym1", sym2);
	}
	
	private static void test(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		Variable x = Variable.createVariableAndPrimedVariable(bdd, "x");
		Variable y = Variable.createVariableAndPrimedVariable(bdd, "y");
		
		Variable[] inputVars = new Variable[]{x};
		Variable[] outpuVars = new Variable[]{y};
		
		//define transition relations
		
//		//trans 1 -- same
//		int T_env = BDDWrapper.same(bdd, inputVars);
//		int T_sys = BDDWrapper.same(bdd, outpuVars);
		
//		//trans 2 -- toggle
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int r1 = BDDWrapper.and(bdd, y.getBDDVar(), BDDWrapper.not(bdd, y.getPrimedCopy().getBDDVar()));
//		int r2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, y.getBDDVar()), y.getPrimedCopy().getBDDVar());
//		int T_sys = BDDWrapper.or(bdd, r1, r2);
		
//		//trans 3 -- env toggle, sys match x'
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
//		//trans 4 -- env free, sys match 
//		int T_env = bdd.ref(bdd.getOne());
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
		//trans 5 -- env free, sys match 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = BDDWrapper.same(bdd, inputVars, Variable.getPrimedCopy(outpuVars));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "environment transition relation", T_env);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "system transition relation", T_sys);
		
		//define the game structure
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars, outpuVars, T_env, T_sys);
		
		System.out.println("game structure is created!");
		
		//define a set of states 
//		int set = BDDWrapper.and(bdd, x.getBDDVar(), y.getBDDVar());
		
		int set = x.getBDDVar();
		set = BDDWrapper.andTo(bdd, set, y.getPrimedCopy().getBDDVar());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "set of states define", set);
		
		//test cpre operator
		int cpre = gs.controllablePredecessor(set);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "cpre(set) is", cpre);
		
		int cpre2 = gs.controllablePredecessor(cpre);
		
		UtilityMethods.debugBDDMethodsAndWait( bdd, "cpre(cpre) is", cpre2);
	}
	
	private static void testExtendedSolver(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		Variable x = Variable.createVariableAndPrimedVariable(bdd, "x");
		Variable y = Variable.createVariableAndPrimedVariable(bdd, "y");
		
		Variable[] inputVars = new Variable[]{x};
		Variable[] outpuVars = new Variable[]{y};
		
		//define transition relations
		
//		//trans 1 -- same
//		int T_env = BDDWrapper.same(bdd, inputVars);
//		int T_sys = BDDWrapper.same(bdd, outpuVars);
		
//		//trans 2 -- toggle
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int r1 = BDDWrapper.and(bdd, y.getBDDVar(), BDDWrapper.not(bdd, y.getPrimedCopy().getBDDVar()));
//		int r2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, y.getBDDVar()), y.getPrimedCopy().getBDDVar());
//		int T_sys = BDDWrapper.or(bdd, r1, r2);
		
//		//trans 3 -- env toggle, sys match x'
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
//		//trans 4 -- env free, sys match 
//		int T_env = bdd.ref(bdd.getOne());
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
		//trans 5 -- env free, sys match 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = bdd.ref(bdd.getOne());
//		int T_sys = BDDWrapper.same(bdd, inputVars, Variable.getPrimedCopy(outpuVars));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "environment transition relation", T_env);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "system transition relation", T_sys);
		
		//define the game structure
		GR1GameStructureTransitionBased gs = new GR1GameStructureTransitionBased(bdd, inputVars, outpuVars, T_env, T_sys);
		
		System.out.println("game structure is created!");
		
		//define initial state 
		int init = BDDWrapper.and(bdd, x.getBDDVar(), y.getBDDVar());
		
		//define GR1 objective 
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		//always eventually x=1 and x'=0
		int ass1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
		assumptions.add(ass1);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		//always eventually y=1
		int gua1 = y.getBDDVar();
		guarantees.add(gua1);
		
		GR1Objective obj = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		GameSolution sol = GR1Solver.solve(bdd, gs, init, obj);
		
		System.out.println("game solved");
		sol.print();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy of the winnier is ", strat.getSystemTransitionRelation());
		
		System.out.println("symbolic execution from the initial state");
		UtilityMethods.debugBDDMethodsAndWait(bdd, "initial states are", strat.getInit());
		
		
		
		int sym1 = strat.symbolicGameOneStepExecution(init);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over init", sym1);
		
		int sym2 = strat.symbolicGameOneStepExecution(sym1);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "symbolic step over sym1", sym2);
	}
	
	private static void test1(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		Variable x = Variable.createVariableAndPrimedVariable(bdd, "x");
		Variable y = Variable.createVariableAndPrimedVariable(bdd, "y");
		
		Variable[] inputVars = new Variable[]{x};
		Variable[] outpuVars = new Variable[]{y};
		
		//define transition relations
		
//		//trans 1 -- same
//		int T_env = BDDWrapper.same(bdd, inputVars);
//		int T_sys = BDDWrapper.same(bdd, outpuVars);
		
//		//trans 2 -- toggle
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int r1 = BDDWrapper.and(bdd, y.getBDDVar(), BDDWrapper.not(bdd, y.getPrimedCopy().getBDDVar()));
//		int r2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, y.getBDDVar()), y.getPrimedCopy().getBDDVar());
//		int T_sys = BDDWrapper.or(bdd, r1, r2);
		
//		//trans 3 -- env toggle, sys match x'
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
//		//trans 4 -- env free, sys match 
//		int T_env = bdd.ref(bdd.getOne());
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
		//trans 5 -- env free, sys match 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = BDDWrapper.same(bdd, inputVars, Variable.getPrimedCopy(outpuVars));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "environment transition relation", T_env);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "system transition relation", T_sys);
		
		//define the game structure
		GR1GameStructure gs = new GR1GameStructure(bdd, inputVars, outpuVars, T_env, T_sys);
		
		System.out.println("game structure is created!");
		
		//define a set of states 
//		int set = BDDWrapper.and(bdd, x.getBDDVar(), y.getBDDVar());
		
		int set = y.getBDDVar();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "set of states define", set);
		
		//test cpre operator
		int cpre = gs.controllablePredecessor(set);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "cpre(set) is", cpre);
		
		int cpre2 = gs.controllablePredecessor(cpre);
		
		UtilityMethods.debugBDDMethodsAndWait( bdd, "cpre(cpre) is", cpre2);
		
		//test symbolicOneStepExecution operator
		
		int next1 = gs.symbolicGameOneStepExecution(set);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of set", next1);
		
		int next2 = gs.symbolicGameOneStepExecution(cpre);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of cpre(set)", next2);
		
		int next3 = gs.symbolicGameOneStepExecution(cpre2);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of cpre(cpre)", next3);
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
}
