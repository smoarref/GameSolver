package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

//import javax.rmi.CORBA.Util;








import reactiveSwarm.ReactiveSwarmAlgorithmTester;
//import solver.BDDWrapper;
import game.BDDWrapper;
import solver.GR1GameStructureTransitionBased;
//import solver.GR1GameStructure;
import solver.GR1Objective;
import solver.GR1Solver;
import solver.GR1WinningStates;
import specification.WorkSpace;
import utils.UtilityMethods;
import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;
import jdd.bdd.BDD;
import jdd.examples.BDDQueens;

public class SwarmGameStructure extends GameStructure{
	
	protected int transitionRelation = -1;
	
	protected int stutterTransitionRelation = -1;
	
	HashMap<Integer, DirectedGraph<String, Node, Edge<Node>>> outputValueToSCCGraph;
	HashMap<Node, Integer> nodeToStates; 
	
	
	public SwarmGameStructure(BDD argBdd) {
		super(argBdd);
	}
	
	public SwarmGameStructure(BDD argBDD, Variable[] inputVars, Variable[] outputVars, int argT_env, int argT_sys){
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
		
		//SCC decomposition 
		outputValueToSCCGraph = new HashMap<Integer, DirectedGraph<String,Node,Edge<Node>>>();
		nodeToStates = new HashMap<Node, Integer>();
		SCCDecomposition();
		
		
	}
	
	//precompute the SCC graphs for different values of output variables
	//TODO: it is not necessary to compute for all values, one can compute the SCC graphs as needed 
	protected void SCCDecomposition(){
		System.out.println("Computing SCC decompositions");
		int[] Y = BDDWrapper.enumerate(bdd, bdd.getOne(), outputVariables);
		for(int y : Y){
			SCCGraph(y);
		}
	}
	
	//Strongly connected component analysis
	/**
	 * assumes every state has a outgoing transition 
	 * TODO: extend
	 * @return
	 */
	private DirectedGraph<String, Node, Edge<Node>> SCCGraph(int y){
		
//		System.out.println("Computing SCC decomposition for output ");
//		bdd.printSet(y);
		
		DirectedGraph<String, Node, Edge<Node>> sccGraph = new DirectedGraph<String, Node, Edge<Node>>(); 
		
		int sameOutput = BDDWrapper.replace(bdd, y, getVtoVprime());
		int trans = BDDWrapper.and(bdd, y, sameOutput);
		BDDWrapper.free(bdd,sameOutput);
		trans = BDDWrapper.andTo(bdd, trans, getEnvironmentTransitionRelation());
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "trans^e_y is ", trans);
		
		//TODO: here we assume that every state has an outgoing edge, so we consider sources, extend later
		int sources = BDDWrapper.exists(bdd, trans, getPrimeVariablesCube());
		
		
		while(sources!=0){
			//pick a state
			int state = BDDWrapper.oneSatisfyingAssignment(bdd, sources);
			int[] noDontCare = BDDWrapper.enumerate(bdd, state, variables);
			BDDWrapper.free(bdd, state);
			state = noDontCare[0];
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "picked the state ", state);
	
			//compute the set of reachable states
//			int reachable = bdd.ref(state);
//			int tmp = bdd.ref(bdd.getZero());
//			while(true){
//				int next = BDDWrapper.EpostImage(bdd, reachable, getVariablesCube(), trans, getVprimetoV());
//				reachable = BDDWrapper.orTo(bdd, reachable, next);
//				BDDWrapper.free(bdd, next);
//				if(tmp == reachable){
//					BDDWrapper.free(bdd, tmp);
//					break;
//				}
//				tmp = bdd.ref(reachable); 
//			}
			
			int reachable=bdd.ref(bdd.getZero());
			int tmp = bdd.ref(state);
			while(reachable != tmp){
				bdd.deref(reachable);
				reachable = bdd.ref(tmp);
				int post = BDDWrapper.EpostImage(bdd, tmp, getVariablesCube(), trans, getVprimetoV());
				tmp = bdd.orTo(tmp, post);
				bdd.deref(post);
			}
			bdd.deref(tmp);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "reachable states are ", reachable);
			
			//compute the set of states that can reach state
//			int canReach = bdd.ref(state);
//			tmp = bdd.ref(bdd.getZero());
//			while(true){
//				int pre = BDDWrapper.EpreImage(bdd, canReach, getPrimeVariablesCube(), trans, getVtoVprime());
//				canReach = BDDWrapper.orTo(bdd, canReach, pre);
//				BDDWrapper.free(bdd, pre);
//				if(tmp == canReach){
//					BDDWrapper.free(bdd, tmp);
//					break;
//				}
//				tmp = bdd.ref(canReach);
//			}
			
			int canReach=bdd.ref(bdd.getZero());
			tmp = bdd.ref(state);
			while(canReach != tmp){
				bdd.deref(canReach);
				canReach = bdd.ref(tmp);
				int pre = BDDWrapper.EpreImage(bdd, tmp, getPrimeVariablesCube(), trans, getVtoVprime());
				tmp = bdd.orTo(tmp, pre);
				bdd.deref(pre);
			}
			bdd.deref(tmp);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "can reach the state ", canReach);
			
			//intersect 
			int scc = BDDWrapper.and(bdd, reachable, canReach);
			BDDWrapper.free(bdd, reachable);
			BDDWrapper.free(bdd, canReach);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "belongs to SCC", scc);
			
			//create a node for the equivalence class
			Node sccNode = new Node(""+state);
			sccGraph.addNode(sccNode);
			
			//update the node to states map
			nodeToStates.put(sccNode, scc);
			
			//remove the set of states from the sources
			int notScc = BDDWrapper.not(bdd, scc);
			sources = BDDWrapper.andTo(bdd, sources, notScc);
			BDDWrapper.free(bdd, notScc);
			
		}
		
		//create SCC graph by adding edges
		try{
			ArrayList<Node> nodes = (ArrayList<Node>) sccGraph.getNodes();
			for(Node n : nodes){
				int n_states = nodeToStates.get(n);
				//compute post image 
				int neighbors = BDDWrapper.EpostImage(bdd, n_states, getVariablesCube(), trans, getVprimetoV());
				for(Node m : nodes){
					if(n == m) continue;
					int m_states = nodeToStates.get(m);
					int edgeExist = BDDWrapper.and(bdd, neighbors, m_states);
					if(edgeExist!=0){
						BDDWrapper.free(bdd, edgeExist);
						Edge<Node> edge = new Edge<Node>(n, m);
						sccGraph.addEdge(edge);
						
//						System.out.println("There is an edge between nodes ");
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "node "+n.getName(), n_states);
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "node "+m.getName(), m_states);
						
					}
				}
				BDDWrapper.free(bdd, neighbors);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//add the SCC graph to the map
		outputValueToSCCGraph.put(y, sccGraph);
		BDDWrapper.free(bdd, trans);
		return sccGraph;
	}
	
	public int getTransitionRelation(){
		if(transitionRelation == -1){
			transitionRelation = BDDWrapper.and(bdd, getEnvironmentTransitionRelation(), getSystemTransitionRelation());
		}
		return transitionRelation;
	}
	
	public int getStutterTransitionRelation(){
		if(stutterTransitionRelation == -1){
			int same = BDDWrapper.same(bdd, outputVariables);
			stutterTransitionRelation = BDDWrapper.and(bdd, getSystemTransitionRelation(), same);
			BDDWrapper.free(bdd, same);
		}
		return stutterTransitionRelation;
	}
	
	/**
	 * the extended controllable predecessor considering the arbitrary timing of system actions
	 */
	public int controllablePredecessor(int set){
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "computing the controllable predecessor in swarm game structure for ", set);
		
		if(set == 0){
			return bdd.ref(bdd.getZero());
		}
		
		//initiate the result set with the set of states that can push the game into set by stuttering 
		int result = cPreStutter(set);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "the system can push into set by stuttering from states ", result);
		
		//standard cpre, CPre* \subseteq cpre
		int cpre_X = cPre(set);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "Original CPre(set) ", cpre_X);
		
		//the different valuations over output vars in cpre(X)
		int Y = BDDWrapper.exists(bdd, cpre_X, getInputVariablesCube());
		
		//enumerate the different valuations in Y
		int[] y = BDDWrapper.enumerate(bdd, Y, outputVariables);
		BDDWrapper.free(bdd, Y);
		
		for(int i = 0; i<y.length ; i++){
			//Obtain the SCC graph corresponding to y[i]
			DirectedGraph<String, Node, Edge<Node>> sccGraph =  outputValueToSCCGraph.get(y[i]);
			ArrayList<Node> nodes = (ArrayList<Node>) sccGraph.getNodes();
			HashMap<Node, Boolean> analyzed = new HashMap<Node, Boolean>();
			int analyzedCount = 0;
			ArrayList<Node> toAnalyze = new ArrayList<Node>();
			while(nodes.size()!=analyzedCount){
				//if the queue of the SCC nodes to be analyzed is empty, process the nodes and add new one to analyze
				if(toAnalyze.size()==0){
					for(Node n : nodes){
						if(analyzed.get(n)==null){
							ArrayList<Edge<Node>> edges = (ArrayList<Edge<Node>>) sccGraph.getAdjacent(n);
							boolean neighborsAnalyzed = true;
							if(edges != null){
								for(Edge<Node> e : edges){
									Node neighbor = e.getTarget();
									if(analyzed.get(neighbor)==null){
										neighborsAnalyzed = false;
										break;
									}
								}
							}
							if(neighborsAnalyzed){
								toAnalyze.add(n);
							}
						}
					}
				}
				//pick the next SCC node to analyze
				Node currentSCC = toAnalyze.remove(0);
				analyzed.put(currentSCC, true);
				analyzedCount++;
				
//				System.out.println("picked "+currentSCC.getName()+" to analyze");
				
				int sccStates = nodeToStates.get(currentSCC);
				
				int phi = bdd.getZero();
				
				//check that for all C \in V^y. (scc,C) \in E^y -> img(scc,transition relation) \cap C \in result
				//or alternatively, check that EpostImage(scc, trans^e_y) \backslash scc \in result
				int sameOutput = BDDWrapper.replace(bdd, y[i], getVtoVprime());
				int trans = BDDWrapper.and(bdd, y[i], sameOutput);
				BDDWrapper.free(bdd,sameOutput);
				trans = BDDWrapper.andTo(bdd, trans, getEnvironmentTransitionRelation());
				
				
				int sccImage = BDDWrapper.EpostImage(bdd, sccStates, getVariablesCube(), trans, getVprimetoV());
				int nextSCCStates = BDDWrapper.diff(bdd, sccImage, sccStates);
				BDDWrapper.free(bdd, sccImage);
				if(BDDWrapper.subset(bdd, nextSCCStates, result)){
					//if all post image is leading to Z, then check that scc \subseteq S \cup X
					int sCupX = BDDWrapper.or(bdd, cpre_X, set);
					if(BDDWrapper.subset(bdd, sccStates, sCupX)){
						//if yes, then compute phi
						int left = BDDWrapper.and(bdd, sccStates, cpre_X);
						left = BDDWrapper.andTo(bdd, left, getEnvironmentTransitionRelation());
						int sccDiffSet = BDDWrapper.diff(bdd, sccStates, set);
						int sccDiffSetPrime = BDDWrapper.replace(bdd, sccDiffSet, getVtoVprime());
						BDDWrapper.free(bdd, sccDiffSet);
						left = BDDWrapper.andTo(bdd, left, sccDiffSetPrime);
						BDDWrapper.free(bdd, sccDiffSetPrime);
						int tmp = BDDWrapper.exists(bdd, left, getPrimeOutputVariablesCube());
						BDDWrapper.free(bdd, left);
						left = tmp;
						
						int setPrime = BDDWrapper.replace(bdd, set, getVtoVprime());
						int right = BDDWrapper.and(bdd, getSystemTransitionRelation(), setPrime);
						BDDWrapper.free(bdd, setPrime);
						
						int phi_formula = BDDWrapper.implies(bdd, left, right);
						BDDWrapper.free(bdd, left);
						BDDWrapper.free(bdd, right);
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "phi_formula before quantifying ", phi_formula);
						
//						int inputVariablesPrimeCube = BDDWrapper.createCube(bdd, inputPrimeVariables);
						int phi_formula_Xprime = BDDWrapper.forall(bdd, phi_formula,getPrimeInputVarsCube());
//						BDDWrapper.free(bdd, inputVariablesPrimeCube);
						BDDWrapper.free(bdd, phi_formula);
						phi = BDDWrapper.forall(bdd, phi_formula_Xprime, getVariablesCube());
						BDDWrapper.free(bdd, phi_formula_Xprime);
//						phi_formula = BDDWrapper.exists(bdd, phi_formula_V, cube)
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "phi after quantifying", phi);
						
					}
					BDDWrapper.free(bdd, sCupX);
				}
				BDDWrapper.free(bdd, nextSCCStates);
				
				
				
				//add the set of states corresponding to current SCC to the result
				if(phi!=0){
					int scc = nodeToStates.get(currentSCC);
					int toAdd = BDDWrapper.and(bdd, scc, cpre_X);
					result = BDDWrapper.orTo(bdd, result, toAdd);
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "the states added to the result", toAdd);
					
					BDDWrapper.free(bdd, toAdd);
				}
			}
		}
		
		BDDWrapper.free(bdd, cpre_X);

		return result;
	}
	
	/**
	 * Standard controllable predecessor operator defined for synchronous games 
	 * @param set
	 * @return
	 */
	public int cPre(int set){
		if(set == 0){
			return bdd.ref(bdd.getZero());
		}
		
		int epre = BDDWrapper.EpreImage(bdd, set, getPrimeOutputVariablesCube(), getSystemTransitionRelation(), getVtoVprime());
		
		int t = BDDWrapper.implies(bdd, getEnvironmentTransitionRelation(), epre);
		BDDWrapper.free(bdd, epre);
		
		int cpre = BDDWrapper.forall(bdd, t, getPrimeInputVarsCube());
		BDDWrapper.free(bdd, t);
		return cpre;
	}
	
	/**
	 * the set of states that the system can push the game into *set* by stuttering
	 * @param set
	 * @return
	 */
	public int cPreStutter(int set){
		if(set == 0){
			return bdd.ref(bdd.getZero());
		}
		
		
//		UtilityMethods.debugBDDMethods(bdd, "stutter transition relation", getStutterTransitionRelation());
//		UtilityMethods.debugBDDMethods(bdd, "in cpre stuttre, the set is ",  set);
//		UtilityMethods.debugBDDMethodsAndWait(bdd,  "prime output variables cube is ", getPrimeOutputVariablesCube());
		
		
		int epre = BDDWrapper.EpreImage(bdd, set, getPrimeOutputVariablesCube(), getStutterTransitionRelation(), getVtoVprime());
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "computing cPreStutter over ", set);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "epre is", epre);
		
		int t = BDDWrapper.implies(bdd, getEnvironmentTransitionRelation(), epre);
		BDDWrapper.free(bdd, epre);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "t is ", t);
		
		int cPreStutter = BDDWrapper.forall(bdd, t, getPrimeInputVarsCube());
		BDDWrapper.free(bdd, t);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "cpreStutter is", cPreStutter);
		
		return cPreStutter;
	}
	
	public GameSolution solve(int init, GR1Objective objective){
		
		boolean debug = false;
		
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), this, init, objective);
		
		UtilityMethods.duration(t0, "winning states computed in ");
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		GameStructure strategy=null;
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			int sameOutput = BDDWrapper.same(bdd, outputVariables);
			int transitionWithSameOutput = BDDWrapper.and(bdd, getEnvironmentTransitionRelation(), sameOutput);
			
			
			//switching the strategy when a liveness goal is achieved, counter variable gets updated accordingly
			for(int i=0; i<guarantees.size();i++){
				int counterValue = BDDWrapper.assign(bdd,i, counter);
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
				int phi = BDDWrapper.and(bdd, counterValue, nextCounter);
				BDDWrapper.free(bdd, counterValue);
				BDDWrapper.free(bdd, nextCounter);
				phi = BDDWrapper.andTo(bdd, phi, guarantees.get(i));
				phi=BDDWrapper.andTo(bdd, phi, transitionWithSameOutput);
				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, phi);
				BDDWrapper.free(bdd, phi);
			}
			
			if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "transitions after satisfying the guarantee", strategy_T_sys);
			
			//pushing closer to the current liveness goal or enforcing an assumption violation forever
			//the counter variable stays the same 
			for(int i=0; i<guarantees.size(); i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				
				int lowAssumptions = bdd.ref(bdd.getZero());
				
				for(int r=1; r<mY_i.size(); r++){
					int notLow = BDDWrapper.not(bdd, low);
					int Q = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "mY[j][<"+r+"] is ", low);
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "current Q is ", Q);
					
					//states that can push deeper by stuttering
					int Qstutter = cPreStutter(low);
					Qstutter = BDDWrapper.andTo(bdd, Qstutter, Q);
					int QstutterTrans = BDDWrapper.and(bdd, Qstutter, transitionWithSameOutput);
					QstutterTrans = BDDWrapper.andTo(bdd, QstutterTrans, counterPart);
					
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "transitions from states that can stutter and win ", QstutterTrans);
					
					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, QstutterTrans);
					BDDWrapper.free(bdd, QstutterTrans);
					//the set of processed states, initially set to Qstutter (since they have been processed!)
					int P = Qstutter;
					//remove Qstutter from the states that need to be processed 
					int tmp = BDDWrapper.diff(bdd, Q, Qstutter);
					BDDWrapper.free(bdd, Q);
					Q = tmp;
					
					int pre =  BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "now Q is ", Q);
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "pre is ", pre);
					
					//analyzing states in Q
					while(Q != 0){//while Q is not empty
						//pick a state s \in Q
						int s = chooseAnState(Q);
						
						if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "picked the state to analyze", s);
						
						//find the SCC that s belongs to, i.e., SCC(s)
						int SCC_s = belongsToSCC(s);
						
						if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "the state belongs to ", SCC_s);
						
						BDDWrapper.free(bdd, s);
						//Q = Q \ SCC(s)
						int tmp2 = BDDWrapper.diff(bdd, Q, SCC_s);
						BDDWrapper.free(bdd, Q);
						Q = tmp2; 
						
						
						
						//if SCC(s) --> mY[j][<r]m find a common output valuation
						int t = commonOutput(SCC_s, pre, low);
						
						if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "common transition is", t);
						
						if(t != 0){
							t = BDDWrapper.andTo(bdd, t, counterPart);
							
							if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "transition added to strategy ", t);
							
							strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, t);
							BDDWrapper.free(bdd, t);
							P = BDDWrapper.orTo(bdd, P, SCC_s);
							
							if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of processed states ", P);
						}
						
						//transitions to other SCCs
						int transToOtherSCCs = BDDWrapper.and(bdd, SCC_s, pre);
						transToOtherSCCs = BDDWrapper.andTo(bdd, transToOtherSCCs, getEnvironmentTransitionRelation());
						int SCC_s_prime = BDDWrapper.replace(bdd, SCC_s, getVtoVprime());
						int notSCC_s_prime = BDDWrapper.not(bdd, SCC_s_prime);
						transToOtherSCCs = BDDWrapper.andTo(bdd, transToOtherSCCs, notSCC_s_prime);
						BDDWrapper.free(bdd, SCC_s_prime);
						BDDWrapper.free(bdd, notSCC_s_prime);
						transToOtherSCCs = BDDWrapper.andTo(bdd, transToOtherSCCs, sameOutput);
						transToOtherSCCs = BDDWrapper.andTo(bdd, transToOtherSCCs, counterPart);
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, transToOtherSCCs);
						
						if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "transitions to other sccs ", transToOtherSCCs);
						BDDWrapper.free(bdd, transToOtherSCCs);
						
					}
					
					//states that the system can force the environment to violate an assumption
					BDDWrapper.free(bdd, Q);
					int notP = BDDWrapper.not(bdd, P);
					Q = BDDWrapper.and(bdd, pre, notP);
					BDDWrapper.free(bdd, P);
					BDDWrapper.free(bdd, notP);
					BDDWrapper.free(bdd, pre);
					
					if(debug) UtilityMethods.debugBDDMethodsAndWait(bdd, "the remaining states to consider for assumption violation is", Q);
					
					ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
					
					for(int j=0; j<assumptions.size();j++){
						if(Q == 0){
							break;
						}
						
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						
						int notA_j = BDDWrapper.not(bdd, assumptions.get(j));
						int preX = BDDWrapper.and(bdd, mX_i_j.get(r), notA_j);
						BDDWrapper.free(bdd, notA_j);
						int notLowAssumptions = BDDWrapper.not(bdd, lowAssumptions);
						preX= BDDWrapper.andTo(bdd, preX, notLowAssumptions);
						BDDWrapper.free(bdd, notLowAssumptions);
						
						int Qstutter_j = cPreStutter(mX_i_j.get(r));
						int uBot = BDDWrapper.and(bdd, Qstutter_j, preX);
						BDDWrapper.free(bdd, Qstutter_j);
						int tStutter_j = BDDWrapper.and(bdd, uBot, transitionWithSameOutput);
						tStutter_j = BDDWrapper.andTo(bdd, tStutter_j, counterPart);
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, tStutter_j);
						BDDWrapper.free(bdd, tStutter_j);
						int tmp_j = BDDWrapper.diff(bdd, Q, uBot);
						BDDWrapper.free(bdd, Q);
						Q = tmp_j;
						int Q_j = BDDWrapper.and(bdd, Q, preX);
						
						while(Q_j != 0){
							int s = chooseAnState(Q_j);
							int SCC_s = belongsToSCC(s);
							BDDWrapper.free(bdd, s);
							int tmp_qj = BDDWrapper.diff(bdd, Q_j, SCC_s);
							BDDWrapper.free(bdd, Q_j);
							Q_j = tmp_qj; 
							int trans_scc_s = commonOutput(SCC_s, preX, mX_i_j.get(r));
							if(trans_scc_s != 0){
								int t = BDDWrapper.and(bdd, trans_scc_s, counterPart);
								strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, t);
								BDDWrapper.free(bdd, t);
								int tmpQ = BDDWrapper.diff(bdd, Q, SCC_s);
								BDDWrapper.free(bdd, Q);
								Q = tmpQ;
							}
						}
						lowAssumptions = BDDWrapper.orTo(bdd, lowAssumptions, mX_i_j.get(r));
					}
					
					
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
				}
				BDDWrapper.free(bdd, low);
				BDDWrapper.free(bdd, lowAssumptions);
				BDDWrapper.free(bdd, counterPart);
			}
			
			
			
			BDDWrapper.free(bdd, sameOutput);
			BDDWrapper.free(bdd, transitionWithSameOutput);
			
			Variable[] strategyOutputVars = Variable.unionVariables(outputVariables,counter);
			strategy = new GR1GameStructureTransitionBased(bdd, inputVariables, strategyOutputVars, 
					getEnvironmentTransitionRelation(), strategy_T_sys);
			strategy.setInit(strategyInit);
			
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(this, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
	
	//TODO: commonOutput function can also be used in controllablePredecessor method
	/**
	 * find a common outputvaluation for SCC \wedge pre toward X
	 * @param SCC
	 * @param pre
	 * @param X
	 * @return
	 */
	private int commonOutput(int SCC, int pre, int X){
		int left = BDDWrapper.and(bdd, SCC, pre);
		left = BDDWrapper.andTo(bdd, left, getEnvironmentTransitionRelation());
		int sccDiffSet = BDDWrapper.diff(bdd, SCC, X);
		int sccDiffSetPrime = BDDWrapper.replace(bdd, sccDiffSet, getVtoVprime());
		BDDWrapper.free(bdd, sccDiffSet);
		left = BDDWrapper.andTo(bdd, left, sccDiffSetPrime);
		BDDWrapper.free(bdd, sccDiffSetPrime);
		int tmp = BDDWrapper.exists(bdd, left, getPrimeOutputVariablesCube());
		BDDWrapper.free(bdd, left);
		left = tmp;
		
		int XPrime = BDDWrapper.replace(bdd, X, getVtoVprime());
		int right = BDDWrapper.and(bdd, getSystemTransitionRelation(), XPrime);
		BDDWrapper.free(bdd, XPrime);
		
		int phi_formula = BDDWrapper.implies(bdd, left, right);
		BDDWrapper.free(bdd, left);
		BDDWrapper.free(bdd, right);
		
		int phi_formula_Xprime = BDDWrapper.forall(bdd, phi_formula,getPrimeInputVarsCube());
		BDDWrapper.free(bdd, phi_formula);
		int phi = BDDWrapper.forall(bdd, phi_formula_Xprime, getVariablesCube());
		BDDWrapper.free(bdd, phi_formula_Xprime);
		
		if(phi!=0){
			int yPrime = chooseAnState(phi, outputPrimeVariables);
			int t = BDDWrapper.and(bdd, SCC, pre);
			t = BDDWrapper.andTo(bdd, t, getEnvironmentTransitionRelation());
			t = BDDWrapper.andTo(bdd, t, yPrime);
			BDDWrapper.free(bdd, yPrime);
			return t;
		}
		return bdd.ref(bdd.getZero());
	}
	
	/**
	 * Finds the SCC that state belongs to 
	 * @param state
	 * @return
	 */
	private int belongsToSCC(int state){
		for(Node n : nodeToStates.keySet()){
			int scc = nodeToStates.get(n);
			//check if state belongs to scc
			if(BDDWrapper.subset(bdd, state, scc)){
				return scc;
			}
		}
		System.err.println("the state does not belong to any SCC, probably a bug");
		return -1;
	}
	
	/**
	 * Choose a state that belongs to the ste of states sources
	 * @param sources: a set of states
	 * @return a chosen state
	 */
	private int chooseAnState(int sources){
		//pick a state
		int state = BDDWrapper.oneSatisfyingAssignment(bdd, sources);
		int[] noDontCare = BDDWrapper.enumerate(bdd, state, variables);
		BDDWrapper.free(bdd, state);
		state = noDontCare[0];
		return state;
	}
	
	private int chooseAnState(int sources, Variable[] vars){
		//pick a state
		int state = BDDWrapper.oneSatisfyingAssignment(bdd, sources);
		int[] noDontCare = BDDWrapper.enumerate(bdd, state, vars);
		BDDWrapper.free(bdd, state);
		state = noDontCare[0];
		return state;
	}
	
	public static void main(String[] args){
//		simpleExampleTwoRegions();
		
//		cornellExample();
		
//		simpleExampleTwoRegionsTwoSCCs();
		
//		simpleExampleThreeRegions();
		
//		simpleExampleThreeRegionsTwoSCCs();
		
//		corridorExample();
		
//		corridorExample21regions();
		
//		corridorExample16regions();
		
//		corridorExample14regions();
		
		corridorExample14regionsMoreEdges();
	}
	
	//building a more complicated example 
		public static void corridorExample14regionsMoreEdges(){
						
			BDD bdd = new BDD(10000,1000);
						 
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
						
						
						
						
				//obtain the workspace
				WorkSpace ws = new WorkSpace(bdd, regionGraph);
						
				Variable[] regionVars = ws.getRegionVars();
						
				//define the input variables 
				Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
						
				System.out.println("region variables are ");
				Variable.printVariables(regionVars);
						
				System.out.println("input variables are ");
				Variable.printVariables(inputVars);
						
				//define T_env and T_sys 
				int T_env = bdd.ref(bdd.getOne());
				int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, ws.getRegionVars(), ws.getRegionToVarsMap());
						
				//define the swarm game structure
				SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
						
				System.out.println("Swarm game structure defined!");
						
				//define the objectives 
				//guarantees
				//always eventually if i=0 then the whole swarm in A, B, C
				ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
				l1OccupiedRegions.add(regionVars[0]);
				l1OccupiedRegions.add(regionVars[1]);
				l1OccupiedRegions.add(regionVars[2]);
				int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
				int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
				int g1 = BDDWrapper.implies(bdd, inp0, l1);
						
				//always eventually if i=1 then the whole swarm in E, F, J, K
				ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
				l2OccupiedRegions.add(regionVars[4]);
				l2OccupiedRegions.add(regionVars[5]);
				l2OccupiedRegions.add(regionVars[9]);
				l2OccupiedRegions.add(regionVars[10]);
				int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
				int inp1 = BDDWrapper.assign(bdd, 1, inputVars);
				int g2 = BDDWrapper.implies(bdd, inp1, l2);
						
				//always eventually if i=2 then the whole swarm in G, I
				ArrayList<Variable> l3OccupiedRegions = new ArrayList<Variable>();
				l3OccupiedRegions.add(regionVars[6]);
				l3OccupiedRegions.add(regionVars[8]);
				int l3 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l3OccupiedRegions);
				int inp2 = BDDWrapper.assign(bdd, 2, inputVars);
				int g3 = BDDWrapper.implies(bdd, inp2, l3);
						
				//always eventually if i=3 then the whole swarm in M,N
				ArrayList<Variable> l4OccupiedRegions = new ArrayList<Variable>();
				l4OccupiedRegions.add(regionVars[12]);
				l4OccupiedRegions.add(regionVars[13]);		
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
							
							
							
				//assumptions
				ArrayList<Integer> assumptions = new ArrayList<Integer>();
				assumptions.add(bdd.getOne());
							
				GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
							
				//define the initial states 
				//initially the whole swarm is in D
				ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
				initOccupiedRegions.add(regionVars[3]);
				int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
				init = BDDWrapper.andTo(bdd, init, inp0);
									
				UtilityMethods.debugBDDMethods(bdd,"init is", init); 
						
				//solve the game 
				long t0_synthesis = UtilityMethods.timeStamp();
				GameSolution sol = sgs.solve(init, objective);
				UtilityMethods.duration(t0_synthesis, "strategy computed in ");
										
				GameStructure strat = sol.strategyOfTheWinner();
						
				UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
						
				int first = strat.symbolicGameOneStepExecution(strat.getInit());
				UtilityMethods.debugBDDMethods(bdd, "first", first);
				int second = strat.symbolicGameOneStepExecution(first);
				UtilityMethods.debugBDDMethods(bdd, "second", second);
						
			}
	
	//building a more complicated example 
	public static void corridorExample14regions(){
					
		BDD bdd = new BDD(10000,1000);
					 
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
					
					
					
					
			//obtain the workspace
			WorkSpace ws = new WorkSpace(bdd, regionGraph);
					
			Variable[] regionVars = ws.getRegionVars();
					
			//define the input variables 
			Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
					
			System.out.println("region variables are ");
			Variable.printVariables(regionVars);
					
			System.out.println("input variables are ");
			Variable.printVariables(inputVars);
					
			//define T_env and T_sys 
			int T_env = bdd.ref(bdd.getOne());
			int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, ws.getRegionVars(), ws.getRegionToVarsMap());
					
			//define the swarm game structure
			SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
					
			System.out.println("Swarm game structure defined!");
					
			//define the objectives 
			//guarantees
			//always eventually if i=0 then the whole swarm in A, B, C
			ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
			l1OccupiedRegions.add(regionVars[0]);
			l1OccupiedRegions.add(regionVars[1]);
			l1OccupiedRegions.add(regionVars[2]);
			int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
			int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
			int g1 = BDDWrapper.implies(bdd, inp0, l1);
					
			//always eventually if i=1 then the whole swarm in E, F, J, K
			ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
			l2OccupiedRegions.add(regionVars[4]);
			l2OccupiedRegions.add(regionVars[5]);
			l2OccupiedRegions.add(regionVars[9]);
			l2OccupiedRegions.add(regionVars[10]);
			int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
			int inp1 = BDDWrapper.assign(bdd, 1, inputVars);
			int g2 = BDDWrapper.implies(bdd, inp1, l2);
					
			//always eventually if i=2 then the whole swarm in G, I
			ArrayList<Variable> l3OccupiedRegions = new ArrayList<Variable>();
			l3OccupiedRegions.add(regionVars[6]);
			l3OccupiedRegions.add(regionVars[8]);
			int l3 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l3OccupiedRegions);
			int inp2 = BDDWrapper.assign(bdd, 2, inputVars);
			int g3 = BDDWrapper.implies(bdd, inp2, l3);
					
			//always eventually if i=3 then the whole swarm in M,N
			ArrayList<Variable> l4OccupiedRegions = new ArrayList<Variable>();
			l4OccupiedRegions.add(regionVars[12]);
			l4OccupiedRegions.add(regionVars[13]);		
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
						
						
						
			//assumptions
			ArrayList<Integer> assumptions = new ArrayList<Integer>();
			assumptions.add(bdd.getOne());
						
			GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
						
			//define the initial states 
			//initially the whole swarm is in D
			ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
			initOccupiedRegions.add(regionVars[3]);
			int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
			init = BDDWrapper.andTo(bdd, init, inp0);
								
			UtilityMethods.debugBDDMethods(bdd,"init is", init); 
					
			//solve the game 
			long t0_synthesis = UtilityMethods.timeStamp();
			GameSolution sol = sgs.solve(init, objective);
			UtilityMethods.duration(t0_synthesis, "strategy computed in ");
									
			GameStructure strat = sol.strategyOfTheWinner();
					
			UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
					
			int first = strat.symbolicGameOneStepExecution(strat.getInit());
			UtilityMethods.debugBDDMethods(bdd, "first", first);
			int second = strat.symbolicGameOneStepExecution(first);
			UtilityMethods.debugBDDMethods(bdd, "second", second);
					
		}
	
	//building a more complicated example 
	public static void corridorExample16regions(){
				
		BDD bdd = new BDD(10000,1000);
				
		//define the region graph 
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
							
		//11 regions 
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
					new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
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
				
				
				
		//obtain the workspace
		WorkSpace ws = new WorkSpace(bdd, regionGraph);
				
		Variable[] regionVars = ws.getRegionVars();
				
		//define the input variables 
		Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
				
		System.out.println("region variables are ");
		Variable.printVariables(regionVars);
				
		System.out.println("input variables are ");
		Variable.printVariables(inputVars);
				
		//define T_env and T_sys 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, ws.getRegionVars(), ws.getRegionToVarsMap());
				
		//define the swarm game structure
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
				
		System.out.println("Swarm game structure defined!");
				
		//define the objectives 
		//guarantees
		//always eventually if i=0 then the whole swarm in A, B, C
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(regionVars[0]);
		l1OccupiedRegions.add(regionVars[1]);
		l1OccupiedRegions.add(regionVars[2]);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
		int g1 = BDDWrapper.implies(bdd, inp0, l1);
				
		//always eventually if i=1 then the whole swarm in E, F, J, K
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(regionVars[4]);
		l2OccupiedRegions.add(regionVars[5]);
		l2OccupiedRegions.add(regionVars[9]);
		l2OccupiedRegions.add(regionVars[10]);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int inp1 = BDDWrapper.assign(bdd, 1, inputVars);
		int g2 = BDDWrapper.implies(bdd, inp1, l2);
				
		//always eventually if i=2 then the whole swarm in G, I
		ArrayList<Variable> l3OccupiedRegions = new ArrayList<Variable>();
		l3OccupiedRegions.add(regionVars[6]);
		l3OccupiedRegions.add(regionVars[8]);
		int l3 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l3OccupiedRegions);
		int inp2 = BDDWrapper.assign(bdd, 2, inputVars);
		int g3 = BDDWrapper.implies(bdd, inp2, l3);
				
		//always eventually if i=3 then the whole swarm in M,N,O,P
		ArrayList<Variable> l4OccupiedRegions = new ArrayList<Variable>();
		l4OccupiedRegions.add(regionVars[12]);
		l4OccupiedRegions.add(regionVars[13]);		
		l4OccupiedRegions.add(regionVars[14]);
		l4OccupiedRegions.add(regionVars[15]);
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
					
					
					
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
					
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
					
		//define the initial states 
		//initially the whole swarm is in D
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(regionVars[3]);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, inp0);
							
		UtilityMethods.debugBDDMethods(bdd,"init is", init); 
				
		//solve the game 
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
								
		GameStructure strat = sol.strategyOfTheWinner();
				
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
				
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
				
	}
	
	//building a more complicated example 
	public static void corridorExample21regions(){
			
		BDD bdd = new BDD(10000,1000);
			
		//define the region graph 
		//define region graph
		DirectedGraph<String, Node, Edge<Node>> regionGraph = new DirectedGraph<String, Node, Edge<Node>>();
						
		//11 regions 
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B"), new Node("C"), new Node("D"), 
				new Node("E"), new Node("F"), new Node("G"), new Node("H"), new Node("I"), new Node("J"), new Node("K"),
				new Node("L"), new Node("M"), new Node("N"), new Node("O"), new Node("P"), new Node("Q"), new Node("R"), 
				new Node("S"), new Node("T"), new Node("U")};
			
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
							
			//J <-> M
			regionGraph.addEdge(new Edge<Node>(regionNodes[9], regionNodes[12]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[9]));
			
			//K <-> M
			regionGraph.addEdge(new Edge<Node>(regionNodes[10], regionNodes[12]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[10]));
			
			//M <-> L
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[11]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[11], regionNodes[12]));
			
			//M <-> N
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[13]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[13], regionNodes[12]));
					
			//M <-> O
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[14]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[14], regionNodes[12]));
			
			//M <-> P
			regionGraph.addEdge(new Edge<Node>(regionNodes[12], regionNodes[15]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[15], regionNodes[12]));
			
			//O <-> Q
			regionGraph.addEdge(new Edge<Node>(regionNodes[14], regionNodes[16]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[14]));
			
			//P <-> Q
			regionGraph.addEdge(new Edge<Node>(regionNodes[15], regionNodes[16]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[15]));
			
			//Q <-> R
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[17]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[17], regionNodes[16]));
			
			//Q <-> S
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[18]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[18], regionNodes[16]));
					
			//Q <-> T
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[19]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[19], regionNodes[16]));
			
			//Q <-> U
			regionGraph.addEdge(new Edge<Node>(regionNodes[16], regionNodes[20]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[20], regionNodes[16]));
			
			//R <-> S
			regionGraph.addEdge(new Edge<Node>(regionNodes[17], regionNodes[18]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[18], regionNodes[17]));
			
			//S <-> T
			regionGraph.addEdge(new Edge<Node>(regionNodes[18], regionNodes[19]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[19], regionNodes[18]));
			
			//T <-> U
			regionGraph.addEdge(new Edge<Node>(regionNodes[19], regionNodes[20]));
			regionGraph.addEdge(new Edge<Node>(regionNodes[20], regionNodes[19]));
			
			//obtain the workspace
			WorkSpace ws = new WorkSpace(bdd, regionGraph);
			
			Variable[] regionVars = ws.getRegionVars();
			
			//define the input variables 
			Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
			
			System.out.println("region variables are ");
			Variable.printVariables(regionVars);
			
			System.out.println("input variables are ");
			Variable.printVariables(inputVars);
			
			//define T_env and T_sys 
			int T_env = bdd.ref(bdd.getOne());
			int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, ws.getRegionVars(), ws.getRegionToVarsMap());
			
			//define the swarm game structure
			SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
			
			System.out.println("Swarm game structure defined!");
			
			//define the objectives 
			//guarantees
			//always eventually if i=0 then the whole swarm in A, B, C
			ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
			l1OccupiedRegions.add(regionVars[0]);
			l1OccupiedRegions.add(regionVars[1]);
			l1OccupiedRegions.add(regionVars[2]);
			int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
			int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
			int g1 = BDDWrapper.implies(bdd, inp0, l1);
			
			//always eventually if i=1 then the whole swarm in E, F, O, P
			ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
			l2OccupiedRegions.add(regionVars[4]);
			l2OccupiedRegions.add(regionVars[5]);
			l2OccupiedRegions.add(regionVars[14]);
			l2OccupiedRegions.add(regionVars[15]);
			int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
			int inp1 = BDDWrapper.assign(bdd, 1, inputVars);
			int g2 = BDDWrapper.implies(bdd, inp1, l2);
			
			//always eventually if i=2 then the whole swarm in G, I, L, N
			ArrayList<Variable> l3OccupiedRegions = new ArrayList<Variable>();
			l3OccupiedRegions.add(regionVars[6]);
			l3OccupiedRegions.add(regionVars[8]);
			l3OccupiedRegions.add(regionVars[11]);
			l3OccupiedRegions.add(regionVars[13]);
			int l3 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l3OccupiedRegions);
			int inp2 = BDDWrapper.assign(bdd, 2, inputVars);
			int g3 = BDDWrapper.implies(bdd, inp2, l3);
			
			//always eventually if i=3 then the whole swarm in R, S, T, U
			ArrayList<Variable> l4OccupiedRegions = new ArrayList<Variable>();
			l4OccupiedRegions.add(regionVars[17]);
			l4OccupiedRegions.add(regionVars[18]);		
			l4OccupiedRegions.add(regionVars[19]);
			l4OccupiedRegions.add(regionVars[20]);
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
				
				
				
			//assumptions
			ArrayList<Integer> assumptions = new ArrayList<Integer>();
			assumptions.add(bdd.getOne());
				
			GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
			//define the initial states 
			//initially the whole swarm is in D
			ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
			initOccupiedRegions.add(regionVars[3]);
			int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
			init = BDDWrapper.andTo(bdd, init, inp0);
						
			UtilityMethods.debugBDDMethods(bdd,"init is", init); 
			
			//solve the game 
			long t0_synthesis = UtilityMethods.timeStamp();
			GameSolution sol = sgs.solve(init, objective);
			UtilityMethods.duration(t0_synthesis, "strategy computed in ");
							
			GameStructure strat = sol.strategyOfTheWinner();
			
			UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
			
			int first = strat.symbolicGameOneStepExecution(strat.getInit());
			UtilityMethods.debugBDDMethods(bdd, "first", first);
			int second = strat.symbolicGameOneStepExecution(first);
			UtilityMethods.debugBDDMethods(bdd, "second", second);
			
		}
	
	//building a more complicated example 
	public static void corridorExample(){
		
		BDD bdd = new BDD(10000,1000);
		
		//define the region graph 
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
		
		//obtain the workspace
		WorkSpace ws = new WorkSpace(bdd, regionGraph);
		
		Variable[] regionVars = ws.getRegionVars();
		
		//define the input variables 
		Variable[] inputVars = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
		
		System.out.println("region variables are ");
		Variable.printVariables(regionVars);
		
		System.out.println("input variables are ");
		Variable.printVariables(inputVars);
		
		//define T_env and T_sys 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, ws.getRegionVars(), ws.getRegionToVarsMap());
		
		//define the swarm game structure
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
		
		System.out.println("Swarm game structure defined!");
		
		//define the objectives 
		//guarantees
		//always eventually if i=0 then the whole swarm in A, C
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(regionVars[0]);
		l1OccupiedRegions.add(regionVars[2]);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int inp0 = BDDWrapper.assign(bdd, 0, inputVars);
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
			
			
			
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
			
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
			
		//define the initial states 
		//initially the whole swarm is in B
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(regionVars[1]);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, inp0);
					
		UtilityMethods.debugBDDMethods(bdd,"init is", init); 
		
		//solve the game 
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
						
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
		
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
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
			
		//always eventually if the target is in Lba1, then the swarm isolates it
		//GF inp -> Lab1 and Lab2 and Corridor
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(lab1);
		l2OccupiedRegions.add(lab2);
		l2OccupiedRegions.add(corridor);
		int l2regions = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
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
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp.getBDDVar()));
					
		UtilityMethods.debugBDDMethods(bdd,"init is", init); 
		
				
		//create the swarm game structure
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
				
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, new Variable[]{inp}, regionVars, T_env, T_sys);
				
		System.out.println("Swarm game structure defined!");
					
							
		System.out.println("ready to synthesize!");
		UtilityMethods.getUserInput();
					
//		long t0 = UtilityMethods.timeStamp();
			
			
//		//compute the set of winning states and winning layers using the atomic game structure 
//		GameSolution sol = swarmReactiveControllerSynthesizer(bdd, regionGraph, regionVars, new Variable[]{inp}, 
//							vars, regionToVarMap, varToRegionMap, init, objective);
		
			
//		UtilityMethods.duration(t0," strategy computed in ");
			
			
//			GameStructure strat = sol.strategyOfTheWinner();
//			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
//			
//			int first = strat.symbolicGameOneStepExecution(strat.getInit());
//			UtilityMethods.debugBDDMethods(bdd, "first", first);
//			int second = strat.symbolicGameOneStepExecution(first);
//			UtilityMethods.debugBDDMethods(bdd, "second", second);
		
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
						
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
		}
	
	
	/**
	 * very simple example for testing purposes. 
	 * Two regions A <-> B 
	 * one Boolean input variable i 
	 * no assumptions
	 * GF(inp -> B_full)
	 */
	public static void simpleExampleTwoRegions(){
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
						
		//2 regions 
		//A, B
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B")};
		
		//A <-> B 
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
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
		
		//define input variable
		Variable inp = Variable.createVariableAndPrimedVariable(bdd, "inp");
		variables.add(inp);
				
		Variable[] regionVars = new Variable[]{A,B};
				
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
				
		//define GR1 objective
				
		//guarantees
		//GF(inp -> B_full)
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(B);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int g1 = BDDWrapper.implies(bdd, inp.getBDDVar(), l1);
		BDDWrapper.free(bdd, l1);
		
		//GF(!inp -> A_full)
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int g2 = BDDWrapper.implies(bdd, bdd.not(inp.getBDDVar()), l2);
		BDDWrapper.free(bdd, l2);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(g1);
		guarantees.add(g2);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		//define the initial states 
		//initially the whole swarm is in A
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(A);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, inp.getBDDVar());
				
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
				
		//define swarm game structure 
		//define T_env 
		int T_env = bdd.ref(bdd.getOne());
		
		//define T_sys  
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		
		
		long t_gameStructureConstruction = UtilityMethods.timeStamp();
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, new Variable[]{inp}, regionVars, T_env, T_sys);
		UtilityMethods.duration(t_gameStructureConstruction, "swarm game structure constructed in ");
		
		System.out.println("Swarm game structure defined!");
		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
		UtilityMethods.getUserInput();
		
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
						
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
		
//		swarmReactiveControllerSynthesizer(bdd, regionGraph, regionVars, new Variable[]{inp}, regionVars, regionToVarMap, varToRegionMap, init, objective);
		
	}
	
	public static void simpleExampleTwoRegionsTwoSCCs(){
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
						
		//2 regions 
		//A, B
		Node[] regionNodes = new Node[]{new Node("A"), new Node("B")};
		
		//A <-> B 
		regionGraph.addEdge(new Edge<Node>(regionNodes[0], regionNodes[1]));
		regionGraph.addEdge(new Edge<Node>(regionNodes[1], regionNodes[0]));
		
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
		
		//define input variable
//		Variable inp = Variable.createVariableAndPrimedVariable(bdd, "inp");
//		variables.add(inp);
		
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
		variables.add(input[0]);
		variables.add(input[1]);
				
		Variable[] regionVars = new Variable[]{A,B};
				
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		Variable.printVariables(vars);
				
		//define GR1 objective
				
		//guarantees
//		//GF(inp=2 | 3 -> B_full)
//		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
//		l1OccupiedRegions.add(B);
//		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
//		int input2 = BDDWrapper.assign(bdd, 2, input);
//		int input3 = BDDWrapper.assign(bdd, 3, input);
//		int in2or3 = BDDWrapper.or(bdd, input2, input3);
//		int g1 = BDDWrapper.implies(bdd, in2or3, l1);
//		BDDWrapper.free(bdd, l1);
		
//		//GF((inp=2 | 3) & B_full)
//		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
//		l1OccupiedRegions.add(B);
//		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
//		int input2 = BDDWrapper.assign(bdd, 2, input);
//		int input3 = BDDWrapper.assign(bdd, 3, input);
//		int in2or3 = BDDWrapper.or(bdd, input2, input3);
//		BDDWrapper.free(bdd, input2);
//		BDDWrapper.free(bdd, input3);
//		int g1 = BDDWrapper.and(bdd, in2or3, l1);
//		BDDWrapper.free(bdd, l1);
//		BDDWrapper.free(bdd,in2or3);
		
		//GF(((inp=2 | 3) & B_full) | ((inp=0 | 1) & A_full))
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(B);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int input2 = BDDWrapper.assign(bdd, 2, input);
		int input3 = BDDWrapper.assign(bdd, 3, input);
		int in2or3 = BDDWrapper.or(bdd, input2, input3);
		BDDWrapper.free(bdd, input2);
		BDDWrapper.free(bdd, input3);
		int g1 = BDDWrapper.and(bdd, in2or3, l1);
		BDDWrapper.free(bdd, l1);
		BDDWrapper.free(bdd,in2or3);
		
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int input0 = BDDWrapper.assign(bdd, 0, input);
		int input1 = BDDWrapper.assign(bdd, 1, input);
		int in0or1 = BDDWrapper.or(bdd, input0, input1);
		BDDWrapper.free(bdd, input0);
		BDDWrapper.free(bdd, input1);
		int tmpg = BDDWrapper.and(bdd, in0or1, l2);
		BDDWrapper.free(bdd, l2);
		BDDWrapper.free(bdd,in0or1);
		g1 = BDDWrapper.orTo(bdd, g1, tmpg);
		BDDWrapper.free(bdd, tmpg);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(g1);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		//define the initial states 
		//initially the whole swarm is in A
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(A);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		int inputInit = BDDWrapper.assign(bdd, 0, input);
		init = BDDWrapper.andTo(bdd, init, inputInit);
		BDDWrapper.free(bdd, inputInit);
				
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
		
		//define T_env 
		int in0 = BDDWrapper.assign(bdd, 0, input);
		int in1 = BDDWrapper.assign(bdd, 1, input);
		int in2 = BDDWrapper.assign(bdd, 2, input);
		int in3 = BDDWrapper.assign(bdd, 3, input);
		int in0prime = BDDWrapper.assign(bdd, 0, Variable.getPrimedCopy(input));
		int in1prime = BDDWrapper.assign(bdd, 1, Variable.getPrimedCopy(input));
		int in2prime = BDDWrapper.assign(bdd, 2, Variable.getPrimedCopy(input));
		int in3prime = BDDWrapper.assign(bdd, 3, Variable.getPrimedCopy(input));
		int next0or2 = BDDWrapper.or(bdd, in0prime, in2prime);
		
		int t0 = BDDWrapper.and(bdd, in0, in1prime);
		int t1 = BDDWrapper.and(bdd, in1, next0or2);
		int t2 = BDDWrapper.and(bdd, in2, in3prime);
		int t3 = BDDWrapper.and(bdd, in3, in2prime);
		
		int T_env = BDDWrapper.or(bdd, t0, t1);
		T_env = BDDWrapper.orTo(bdd, T_env, t2);
		T_env = BDDWrapper.orTo(bdd, T_env, t3);
		
		//define T_sys 
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
				
						
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, input, regionVars, T_env, T_sys);
		
		System.out.println("Swarm game structure defined!");
		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
		UtilityMethods.getUserInput();
		
		
//		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), sgs, init, objective);
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
//		}
		
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
						
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
		
	}
	
	public static void simpleExampleThreeRegions(){
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
				
		//3 regions 
		//A, B, C
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
		
		//define input variable
		Variable inp = Variable.createVariableAndPrimedVariable(bdd, "inp");
		variables.add(inp);
		
		Variable[] regionVars = new Variable[]{A,B,C};
		
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		//define GR1 objective
		
		//guarantees
		//GF(inp -> C_full)
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(C);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int g1 = BDDWrapper.implies(bdd, inp.getBDDVar(), l1);
		BDDWrapper.free(bdd, l1);
		
		//GF(NOT inp -> A_full)
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int g2 = BDDWrapper.implies(bdd, bdd.not(inp.getBDDVar()), l2);
		BDDWrapper.free(bdd, l2);
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(g1);
		guarantees.add(g2);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//define the initial states 
		//initially the whole swarm is in B
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(B);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		init = BDDWrapper.andTo(bdd, init, bdd.not(inp.getBDDVar()));
		
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
		
				
		System.out.println("ready to synthesize!");
		UtilityMethods.getUserInput();
		
		long t0 = UtilityMethods.timeStamp();
		
		//compute the set of winning states and winning layers using the atomic game structure 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, new Variable[]{inp}, regionVars, T_env, T_sys);
		
//		System.out.println("atomicGS defined!");
//		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
//		UtilityMethods.getUserInput();
		
//		//compute the set of winning states for the atomic game struture 
//		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), sgs, init, objective);
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
//		}
		
		long t0_synthesis = UtilityMethods.timeStamp();
		GameSolution sol = sgs.solve(init, objective);
		UtilityMethods.duration(t0_synthesis, "strategy computed in ");
						
		GameStructure strat = sol.strategyOfTheWinner();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the set of winning states are", sol.getWinningSystemStates());
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "strategy is", strat.getSystemTransitionRelation());
		
		int first = strat.symbolicGameOneStepExecution(strat.getInit());
		UtilityMethods.debugBDDMethods(bdd, "first", first);
		int second = strat.symbolicGameOneStepExecution(first);
		UtilityMethods.debugBDDMethods(bdd, "second", second);
	}
	
	public static void simpleExampleThreeRegionsTwoSCCs(){
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
				
		//3 regions 
		//A, B, C
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
		
		//define input variable
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "inp");
		variables.add(input[0]);
		variables.add(input[1]);
		
		Variable[] regionVars = new Variable[]{A,B,C};
		
		Variable[] vars = variables.toArray(new Variable[variables.size()]);
		
		//define GR1 objective
		
		//GF(((inp=2 | 3) & C_full) | ((inp=0 | 1) & A_full))
		ArrayList<Variable> l1OccupiedRegions = new ArrayList<Variable>();
		l1OccupiedRegions.add(C);
		int l1 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l1OccupiedRegions);
		int input2 = BDDWrapper.assign(bdd, 2, input);
		int input3 = BDDWrapper.assign(bdd, 3, input);
		int in2or3 = BDDWrapper.or(bdd, input2, input3);
		BDDWrapper.free(bdd, input2);
		BDDWrapper.free(bdd, input3);
//		int g1 = BDDWrapper.and(bdd, in2or3, l1);
		int g1 = BDDWrapper.implies(bdd, in2or3, l1);
		BDDWrapper.free(bdd, l1);
		BDDWrapper.free(bdd,in2or3);
				
		ArrayList<Variable> l2OccupiedRegions = new ArrayList<Variable>();
		l2OccupiedRegions.add(A);
		int l2 = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, l2OccupiedRegions);
		int input0 = BDDWrapper.assign(bdd, 0, input);
		int input1 = BDDWrapper.assign(bdd, 1, input);
		int in0or1 = BDDWrapper.or(bdd, input0, input1);
		BDDWrapper.free(bdd, input0);
		BDDWrapper.free(bdd, input1);
		int tmpg = BDDWrapper.and(bdd, in0or1, l2);
		BDDWrapper.free(bdd, l2);
		BDDWrapper.free(bdd,in0or1);
//		g1 = BDDWrapper.orTo(bdd, g1, tmpg);
		int g2 = BDDWrapper.implies(bdd, in0or1, l2);
		BDDWrapper.free(bdd, tmpg);
		
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		guarantees.add(g1);
		guarantees.add(g2);
		
		System.out.println("guarantees are ");
		for(Integer g : guarantees ){
			UtilityMethods.debugBDDMethods(bdd, "guarantee", g);
		}
		
		
		
		//assumptions
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		assumptions.add(bdd.getOne());
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		
		//define the initial states 
		//initially the whole swarm is in B
		ArrayList<Variable> initOccupiedRegions = new ArrayList<Variable>();
		initOccupiedRegions.add(B);
		int init = ReactiveSwarmAlgorithmTester.createRegionPredicate(bdd, regionVars, initOccupiedRegions);
		int inputInit = BDDWrapper.assign(bdd, 0, input);
		init = BDDWrapper.andTo(bdd, init, inputInit);
		
		UtilityMethods.debugBDDMethods(bdd,"init is", init);
		
				
		System.out.println("ready to synthesize!");
		UtilityMethods.getUserInput();
		
		long time0 = UtilityMethods.timeStamp();
		
		//define T_env 
		int in0 = BDDWrapper.assign(bdd, 0, input);
		int in1 = BDDWrapper.assign(bdd, 1, input);
		int in2 = BDDWrapper.assign(bdd, 2, input);
		int in3 = BDDWrapper.assign(bdd, 3, input);
		int in0prime = BDDWrapper.assign(bdd, 0, Variable.getPrimedCopy(input));
		int in1prime = BDDWrapper.assign(bdd, 1, Variable.getPrimedCopy(input));
		int in2prime = BDDWrapper.assign(bdd, 2, Variable.getPrimedCopy(input));
		int in3prime = BDDWrapper.assign(bdd, 3, Variable.getPrimedCopy(input));
		int next0or2 = BDDWrapper.or(bdd, in0prime, in2prime);
				
		int t0 = BDDWrapper.and(bdd, in0, in1prime);
		int t1 = BDDWrapper.and(bdd, in1, next0or2);
		int t2 = BDDWrapper.and(bdd, in2, in3prime);
		int t3 = BDDWrapper.and(bdd, in3, in2prime);
				
		int T_env = BDDWrapper.or(bdd, t0, t1);
		T_env = BDDWrapper.orTo(bdd, T_env, t2);
		T_env = BDDWrapper.orTo(bdd, T_env, t3);
				
		int T_sys = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		
		
		long initTime = UtilityMethods.timeStamp();
		
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, input, regionVars, T_env, T_sys);
		
//		System.out.println("atomicGS defined!");
//		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
//		UtilityMethods.getUserInput();
		
		//compute the set of winning states for the atomic game struture 
		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), sgs, init, objective);
				
		UtilityMethods.duration(initTime, "winning states computed in ");
//		System.out.println("winning states computed");
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the winning states", gr1w.getWinningStates());
		gr1w.printMemory(bdd);
		
		//check if the game is realizable 
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		//compute the strategy using the original transition relation where multiple action can be taken at the same time
		if(isRealizable){
			
			System.out.println("The game is realizable");
		}
		
		
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
		int t = directedGraphToFineGrainedTransitionRelation(bdd, regionGraph, regionVars, regionToVarMap);
		
		//create the atomic game structure
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = t;
		
		long t0 = UtilityMethods.timeStamp();
		SwarmGameStructure sgs = new SwarmGameStructure(bdd, inputVars, regionVars, T_env, T_sys);
		
		System.out.println("Swarm game structure defined!");
//		UtilityMethods.debugBDDMethods(bdd, "T_env is", T_env);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys is", T_sys);
//		UtilityMethods.getUserInput();
		
		
		//testing 
//		//set of states \bigvee_r r\in regions
//		int f = bdd.ref(bdd.getZero());
//		for(Variable v : regionVars){
//			f=BDDWrapper.orTo(bdd, f, v.getBDDVar());
//		}
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "f is ", f);
//		
//		int replace = BDDWrapper.replace(bdd, f, sgs.getVtoVprime());
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "replaced is ", replace);
		
		//testing the controllable predecessor 
//		//test 1
//		int set = BDDWrapper.and(bdd, regionVars[1].getBDDVar(), bdd.not(regionVars[0].getBDDVar()));
//		set = BDDWrapper.implies(bdd, inputVars[0].getBDDVar(), set);
//		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "testing cpre* operator on the set", set);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "Cpre*(set) is", sgs.controllablePredecessor(set));
//		
//		//test 2
//		int Bi = BDDWrapper.and(bdd, regionVars[1].getBDDVar(), inputVars[0].getBDDVar());
//		int AnegI = BDDWrapper.and(bdd, regionVars[0].getBDDVar(), bdd.not(inputVars[0].getBDDVar()));
//		set = BDDWrapper.or(bdd, Bi, AnegI);
//				
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "testing cpre* operator on the set", set);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "Cpre*(set) is", sgs.controllablePredecessor(set));
		
//		//test 3
//		int Bfull = BDDWrapper.and(bdd, regionVars[1].getBDDVar(), bdd.not(regionVars[0].getBDDVar()));
//		int Afull = BDDWrapper.and(bdd, regionVars[0].getBDDVar(), bdd.not(regionVars[1].getBDDVar()));
//		Bfull = BDDWrapper.andTo(bdd, Bfull, inputVars[0].getBDDVar());
//		Afull = BDDWrapper.andTo(bdd, Afull, bdd.not(inputVars[0].getBDDVar()));
//		int set = BDDWrapper.or(bdd, Afull, Bfull);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "testing cpre* operator on the set", set);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "Cpre*(set) is", sgs.controllablePredecessor(set));
		
		//compute the set of winning states for the atomic game struture 
		GR1WinningStates gr1w  =GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), sgs, init, objective);
		
		UtilityMethods.duration(t0, "winning states computed in ");
		
		System.out.println("winning states computed");
		UtilityMethods.debugBDDMethodsAndWait(bdd, "the winning states", gr1w.getWinningStates());
		gr1w.printMemory(bdd);
		
		//check if the game is realizable 
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		//compute the strategy using the original transition relation where multiple action can be taken at the same time
		if(isRealizable){
			
			System.out.println("The game is realizable");
			
			
			
			//TODO: synthesize a strategy
			return null;
		}
		
		
		
		System.out.println("the specification is unrealizable!");
		return null;
		
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
	private static int directedGraphToFineGrainedTransitionRelation(BDD bdd, DirectedGraph<String, Node, Edge<Node>> graph, Variable[] regionVars, 
			HashMap<Node, Variable> regionToVarMap){
		
		System.out.println("creating fine-grained transition relation");
		
		
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
	
	
}
