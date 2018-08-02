package swarm;

import java.util.ArrayList;

import solver.GR1Objective;
import solver.GR1Solver;
import utils.UtilityMethods;
import jdd.bdd.BDD;
import game.BDDWrapper;
import game.GameStructure;
import game.Variable;

public class SynchronizationGameStructure extends GameStructure {
	
	Variable[] allSynchronizationSignals;
	Variable[] allSynchronizationSignalsPrime;
	
	//s^{i,j}_l
	//the first dimension --> process i
	//the second dimension --> the state l of process i 
	//the third dimension --> process j
	Variable[][][] synchronizationSignals;
	
	Variable[][] advanceVariables;
	
	Variable[] allAdvanceVariables;
	Variable[] allAdvanceVariablesPrime;
	
	ArrayList<Variable[]> processVariables;
	
	Variable[] allProcessVariables; 
	Variable[] allProcessVariablesPrime;
	
	Variable[] schedulingVars; 
	Variable[] schedulingVarsPrime; 
	
	//local processes defined over processVariables
	int[] processes;
	
	//local processes initial states
	int[] processesInit;
	
	//process states
	ArrayList<ArrayList<Integer>> processStates;
	
	//process length without intermediate states
	int processLength;
	
	int asynchronousTransitionRelation=-1;
	
	ArrayList<Integer> partitionTransitionRelation;
	
	ArrayList<ArrayList<Integer>> partitionStatesTransitionRelation;
	

	public SynchronizationGameStructure(BDD argBdd) {
		super(argBdd);
		// TODO Auto-generated constructor stub
	}

	public SynchronizationGameStructure(BDD argBDD, int argProcessLength, ArrayList<Variable[]> argProcessVariables, Variable[][][] argSynchronizationVariables, Variable[][] argAdvanceVariables, Variable[] argSchedulingVars, int[] argProcesses, int[] argProcessesInit ){
		super(argBDD);
		
	
		processes = argProcesses;
		
		processesInit = argProcessesInit;
		
		int numOfProcesses = processes.length;
		
		processLength = argProcessLength;
		
		//define vars
		processVariables = argProcessVariables;
		allProcessVariables = Variable.unionVariables(processVariables);
		allProcessVariablesPrime = Variable.getPrimedCopy(allProcessVariables);
		
		synchronizationSignals = argSynchronizationVariables;
		
		allSynchronizationSignals = new Variable[processLength*numOfProcesses*(numOfProcesses-1)];
		int counter = 0;
		for(int i=0; i<synchronizationSignals.length;i++){
			for(int j=0; j<synchronizationSignals[i].length; j++){
				for(int k=0; k<synchronizationSignals[i][j].length; k++){
					Variable v = synchronizationSignals[i][j][k];
					if(v != null){
//						v.print();
						allSynchronizationSignals[counter] = v;
						counter++;
					}
				}
			}
		}
		allSynchronizationSignalsPrime = Variable.getPrimedCopy(allSynchronizationSignals);
		
		advanceVariables = argAdvanceVariables;
		
		allAdvanceVariables = new Variable[numOfProcesses * numOfProcesses - numOfProcesses];
		counter = 0; 
		for(int i=0; i<advanceVariables.length; i++){
			for(int j=0; j<advanceVariables[i].length; j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					allAdvanceVariables[counter] = v;
					counter++;
				}
			}
		}
		
		allAdvanceVariablesPrime = Variable.getPrimedCopy(allAdvanceVariables);
		
		schedulingVars = argSchedulingVars; 
		schedulingVarsPrime = Variable.getPrimedCopy(schedulingVars);
		
		variables = Variable.unionVariables(allProcessVariables, allSynchronizationSignals);
		variables = Variable.unionVariables(variables, allAdvanceVariables);
		variables = Variable.unionVariables(variables, schedulingVars);
		
		primedVariables = Variable.getPrimedCopy(variables); 
		
		//define permutations
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//define actionvars
		actionVars = null;
		sysActionVars = null; 
		envActionVars = null;
		
		//define transitions
		init = -1; 
		T_env=-1;
		T_sys = -1; 
		
		computeProcessesStates();
		
//		System.out.println("printing process states");
//		printProcessesStates();
//		UtilityMethods.getUserInput();
		
		//check that all processes have the same length
		int processLengthWithIntermediates = processStates.get(0).size();
		for(int i=1; i<processStates.size(); i++){
			if(processLengthWithIntermediates != processStates.get(i).size()){
				System.err.println("Processes are not the same length!");
			}
		}
		
		processLength = processLengthWithIntermediates/2;
		
		computeTransitionRelation();

			
		T_envList=null;
		T_sysList=null;
	}
	
	public Variable[] getSynchronizationVariables(){
		return allSynchronizationSignals;
	}
	
	public void computeProcessesStates(){
		processStates = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<processes.length; i++){
			int process = processes[i];
			int processInit = processesInit[i];
			ArrayList<Integer> reachableStates = processReachableStates(process, processInit);
			processStates.add(reachableStates);
		}
	}
	
	public void printProcessesStates(){
		for(int i=0; i<processStates.size(); i++){
			ArrayList<Integer> pi = processStates.get(i);
			System.out.println("Process "+i);
			for(int j=0; j<pi.size();j++){
				UtilityMethods.debugBDDMethods(bdd, "state "+j +" of process "+i, pi.get(j));
			}
		}
	}
	
	/**
	 * Computes the sequence of reachable states for the process
	 * Assumes that the process is deterministic
	 * Assumes that init is a single state
	 * @param process
	 * @param init
	 * @return
	 */
	private ArrayList<Integer> processReachableStates(int process, int init){
		ArrayList<Integer> reachableStates = new ArrayList<Integer>();
		reachableStates.add(init);
		int current = init;
		int Q = bdd.ref(init);
		while(true){
			int next = BDDWrapper.EpostImage(bdd, current, getVariablesCube(), process,  getVprimetoV());
			if(BDDWrapper.subset(bdd, next, Q)){
				break;
			}
			reachableStates.add(next);
			current = next; 
		}
		
		return reachableStates;
	}
	
	/**
	 * Defines the transition relation for the synchronization game structure
	 */
	public void computeTransitionRelation(){
		//synchronization signals do not change over transitions
		int T_synch = BDDWrapper.same(bdd, allSynchronizationSignals);
		
		//advance variables transitions
		
		//\phi_{i,j}
		int[][] phi = new int[processes.length][processes.length];
		for(int i=0; i<processes.length; i++){
			for(int j=0; j<processes.length; j++){
				if(i==j) continue;
				
				//a_{i,j}
				Variable a_i_j = advanceVariables[i][j];
				//a_{j,i}
				Variable a_j_i = advanceVariables[j][i];
				
				phi[i][j] = BDDWrapper.and(bdd, bdd.not(a_i_j.getBDDVar()), bdd.not(a_j_i.getBDDVar()));
				
				int phi_i = bdd.ref(bdd.getZero());
				for(int l=0; l<processLength; l++){
					//q^i_l
					int q_i_l  = processStates.get(i).get(2*l);
					//s^{i,j}_l
					Variable s_i_j_l = synchronizationSignals[i][l][j];
					
					int psi_l = BDDWrapper.and(bdd, q_i_l, s_i_j_l.getBDDVar());
					
					int phi_j = bdd.ref(bdd.getZero());
					for(int m=0; m<processLength; m++){
						//s^{j,i}_m
						Variable s_j_i_m = synchronizationSignals[j][m][i];
						//q^j_m
						int q_j_m = processStates.get(j).get(2*m);
						
						int psi_m = BDDWrapper.and(bdd, q_j_m, s_j_i_m.getBDDVar());
						
						phi_j = BDDWrapper.orTo(bdd, phi_j, psi_m);
						BDDWrapper.free(bdd, psi_m);
					}
					
					psi_l = BDDWrapper.andTo(bdd, psi_l, phi_j);
					BDDWrapper.free(bdd, phi_j);
					
					phi_i = BDDWrapper.orTo(bdd, phi_i, psi_l);
					BDDWrapper.free(bdd, psi_l);
				}
				
				phi[i][j] = BDDWrapper.andTo(bdd, phi[i][j], phi_i);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "phi["+i+"]["+j+"] = ", phi[i][j]);
			}
		}
		
		//T_{a_{i,j}}
		int[][] T_a = new int[processes.length][processes.length];
		for(int i=0; i<processes.length; i++){
			int alpha_i = BDDWrapper.assign(bdd, i, schedulingVarsPrime);
			for(int j=0; j<processes.length;j++){
				if(i==j) continue; 
				//a_{i,j}
				Variable a_i_j = advanceVariables[i][j];
				
				
				int c1 = BDDWrapper.and(bdd, a_i_j.getBDDVar(), bdd.not(alpha_i));
				int phi1 = BDDWrapper.implies(bdd, c1, a_i_j.getPrimedCopy().getBDDVar());
				BDDWrapper.free(bdd, c1);
				
				int c2 = BDDWrapper.and(bdd, a_i_j.getBDDVar(), alpha_i);
				int phi2 = BDDWrapper.implies(bdd, c2, bdd.not(a_i_j.getPrimedCopy().getBDDVar()));
				BDDWrapper.free(bdd, c2);
				
				int phi3 = BDDWrapper.implies(bdd,phi[i][j], a_i_j.getPrimedCopy().getBDDVar());
				
				int c4 = BDDWrapper.and(bdd, bdd.not(a_i_j.getBDDVar()), bdd.not(phi[i][j]));
				int phi4 = BDDWrapper.implies(bdd, c4, bdd.not(a_i_j.getPrimedCopy().getBDDVar()));
				BDDWrapper.free(bdd, c4);
				
				T_a[i][j]=BDDWrapper.and(bdd, phi1, phi2);
				T_a[i][j]=BDDWrapper.andTo(bdd, T_a[i][j], phi3);
				T_a[i][j]=BDDWrapper.andTo(bdd, T_a[i][j], phi4);
				BDDWrapper.free(bdd, phi1);
				BDDWrapper.free(bdd, phi2);
				BDDWrapper.free(bdd, phi3);
				BDDWrapper.free(bdd, phi4);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "T_a["+i+"]["+j+"] = ", T_a[i][j]);
			}
		}
		
		//T^{\mathcal{A}}
		int T_adv =  bdd.ref(bdd.getOne());
		for(int i=0; i<processes.length; i++){
			for(int j=0; j<processes.length;j++){
				if(i==j) continue;
				T_adv = BDDWrapper.andTo(bdd, T_adv, T_a[i][j]);
			}
		}
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "T_adv", T_adv);
		
		//free T_a[i][j]
		for(int i=0; i<processes.length; i++){
			for(int j=0; j<processes.length;j++){
				if(i==j) continue;
				BDDWrapper.free(bdd, T_a[i][j]);
			}
		}
		
		//process progress condition
		int[] phi_p = new int[processes.length];
		for(int i=0; i<processes.length; i++){
			int notIntermediateState = bdd.ref(bdd.getZero());
			phi_p[i]=bdd.ref(bdd.getZero());
			for(int l=0; l<processLength;l++){
				//q^i_l
				int q_i_l  = processStates.get(i).get(2*l);
				notIntermediateState = BDDWrapper.orTo(bdd, notIntermediateState, q_i_l);
				int guard = bdd.ref(bdd.getOne());
				for(int j=0; j<processes.length; j++){
					if(i==j) continue; 
					//a_{i,j}
					Variable a_i_j = advanceVariables[i][j];
					//s^{i,j}_l
					Variable s_i_j_l = synchronizationSignals[i][l][j];
					int g = BDDWrapper.or(bdd, bdd.not(s_i_j_l.getBDDVar()), a_i_j.getBDDVar());
					guard = BDDWrapper.andTo(bdd, guard, g);
					BDDWrapper.free(bdd, g);
				}
				int tmp = BDDWrapper.and(bdd, q_i_l, guard);
				BDDWrapper.free(bdd, guard);
				phi_p[i] = BDDWrapper.orTo(bdd, phi_p[i], tmp);
				BDDWrapper.free(bdd, tmp);
			}
			
			int intermediateStates = BDDWrapper.not(bdd, notIntermediateState);
			phi_p[i]=BDDWrapper.orTo(bdd, phi_p[i], intermediateStates);
			BDDWrapper.free(bdd, intermediateStates);
			BDDWrapper.free(bdd, intermediateStates);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "Progress condition for process "+i, phi_p[i]);
		}
			
		
		//process transition
		int[] trans = new int[processes.length];
		for(int i=0;i<processes.length;i++){
			int c1 = BDDWrapper.implies(bdd, phi_p[i], processes[i]);
			int samePi = BDDWrapper.same(bdd, processVariables.get(i));
			int c2 = BDDWrapper.implies(bdd, bdd.not(phi_p[i]), samePi);
			trans[i] = BDDWrapper.and(bdd, c1, c2);
			BDDWrapper.free(bdd, c1);
			BDDWrapper.free(bdd, samePi);
			BDDWrapper.free(bdd, c2);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "individual process transitions "+i, trans[i]);
		}
		
		//free progress conditions
		for(int i=0; i<processes.length;i++){
			BDDWrapper.free(bdd, phi_p[i]);
		}
		
		//transition relation
		
		partitionTransitionRelation = new ArrayList<Integer>();
		
		asynchronousTransitionRelation = BDDWrapper.and(bdd, T_synch, T_adv);
		
		BDDWrapper.free(bdd, T_synch);
		BDDWrapper.free(bdd, T_adv);
		int processExec = bdd.ref(bdd.getZero());
		for(int i=0; i<processes.length; i++){
			int alpha_i = BDDWrapper.assign(bdd, i, schedulingVarsPrime);
			int tmp = BDDWrapper.and(bdd, alpha_i, trans[i]);
			BDDWrapper.free(bdd, alpha_i);
			int sameOtherProcesses = bdd.ref(bdd.getOne());
			for(int j=0; j<processes.length;j++){
				if(i==j) continue;
				int samePj = BDDWrapper.same(bdd, processVariables.get(j));
				sameOtherProcesses = BDDWrapper.andTo(bdd, sameOtherProcesses, samePj);
				BDDWrapper.free(bdd, samePj);
			}
			tmp = BDDWrapper.andTo(bdd, tmp, sameOtherProcesses);
			BDDWrapper.free(bdd, sameOtherProcesses);
			
			int part_i = BDDWrapper.and(bdd, tmp, asynchronousTransitionRelation);
			partitionTransitionRelation.add(part_i);
			
			processExec = BDDWrapper.orTo(bdd, processExec, tmp);
			BDDWrapper.free(bdd, tmp);
			
		}
		asynchronousTransitionRelation = BDDWrapper.andTo(bdd, asynchronousTransitionRelation, processExec);
		BDDWrapper.free(bdd, processExec);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "asynchronous transition relation", asynchronousTransitionRelation);
		
//		//partition state transition relation
//		partitionStatesTransitionRelation = new ArrayList<ArrayList<Integer>>();
//		for(int i=0; i<processes.length;i++){
//			int part_i = partitionTransitionRelation.get(i);
//			ArrayList<Integer> p_i_states = processStates.get(i);
//			ArrayList<Integer> part_i_trans = new ArrayList<Integer>();
//			for(int j=0;j<p_i_states.size();j++){
//				int p_i_q_j = p_i_states.get(j);
//				int q_j_trans = BDDWrapper.and(bdd, part_i, p_i_q_j);
//				part_i_trans.add(q_j_trans);
//			}
//			partitionStatesTransitionRelation.add(part_i_trans);
//		}
		
		//free process transitions 
		for(int i=0; i<processes.length; i++){
			BDDWrapper.free(bdd, trans[i]);
		}
		
		
	}
	
	public int getTransitionRelation(){
		return asynchronousTransitionRelation;
	}
	
	public int controllablePredecessor(int set){
		if(set == 0){
			return bdd.getZero();
		}
		
		int cube = getPrimedVariablesAndActionsCube();
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "set", set);
		
		int cPre = BDDWrapper.ApreImage_EPreBased(bdd, set, cube, getTransitionRelation(), vTovPrime);
		
		return cPre;
	}
	
	public int symbolicGameOneStepExecution(int set){
		int cube=getVariablesAndActionsCube();
		int next = BDDWrapper.EpostImage(bdd, set, cube, getTransitionRelation(), vPrimeTov);
		return next; 
	}
	
	public int reachable(){
		int Q=bdd.ref(bdd.getZero());
		int Qprime = bdd.ref(getInit());
		while(Q != Qprime){
			bdd.deref(Q);
			Q = bdd.ref(Qprime);
			int post = symbolicGameOneStepExecution(Qprime);
			Qprime = bdd.orTo(Qprime, post);
			bdd.deref(post);
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public int reachable(int init){
		int Q=bdd.ref(bdd.getZero());
		int Qprime = bdd.ref(init);
		while(Q != Qprime){
			bdd.deref(Q);
			Q = bdd.ref(Qprime);
			int post = symbolicGameOneStepExecution(Qprime);
			Qprime = bdd.orTo(Qprime, post);
			bdd.deref(post);
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public static void main(String[] args) {
//		test1();
		
//		test2_unrealizableObjective();
		
//		test3();
		
		test4();
		
//		test5();
		
//		test_harmony();
	}
	
	private static void test_harmony(){
		
		BDD bdd = new BDD(10000, 1000);
		
		int numOfProcesses = 4;
		int processLength = 32;
		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
		
			
		for(int i=0; i<numOfProcesses; i++){
			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			processVariables.add(p_i);
		}
		
		Variable[] allProcessVariables = Variable.unionVariables(processVariables);
		
		//define a frame
		//the whole process is a frame
		int frame = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			int processFormula = bdd.ref(bdd.getZero());
			for(int j=0; j<2*processLength; j++){
				int q_j = BDDWrapper.assign(bdd, j, processVariables.get(i));
				processFormula = BDDWrapper.orTo(bdd, processFormula, q_j);
				BDDWrapper.free(bdd, q_j);
			}
			frame = BDDWrapper.andTo(bdd, frame, processFormula);
			BDDWrapper.free(bdd, processFormula);
		}
		
		//define the safety objective
		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
		int q7 = BDDWrapper.assign(bdd, 7, processVariables.get(0));
		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
		int u5 = BDDWrapper.assign(bdd, 5, processVariables.get(1));
		int v1 = BDDWrapper.assign(bdd, 1, processVariables.get(2));
		int v5 = BDDWrapper.assign(bdd, 5, processVariables.get(2));
		int q3Orq7 = BDDWrapper.or(bdd, q3, q7);
		int u1Oru5 = BDDWrapper.or(bdd, u1, u5);
		int v1Orv5 = BDDWrapper.or(bdd, v1, v5);
		
		int notSafe = BDDWrapper.and(bdd, q3Orq7, u1Oru5);
		notSafe = BDDWrapper.andTo(bdd, notSafe, v1Orv5);
		
		//check if the frame staisfies the safety objective
		//(q_0 or .. or q_7) and (v_0 or .. or v_7) and (u_0 or .. or u_7) \models safety?
		
		int violation = BDDWrapper.and(bdd, frame, notSafe);
		
		
		//if not, enumerate the unsafe states
		if(violation != 0){
			System.out.println("there is an interleaving that violates safety!");
		}
		
//		int[] unsafeStates = BDDWrapper.enumerate(bdd, violation, allProcessVariables);
		
		ArrayList<String> unsafeMinterms = BDDWrapper.minterms(bdd, violation, allProcessVariables);
		
//		System.out.println("unsafe minterms");
//		for(String u : unsafeMinterms){
//			System.out.println(u);
//		}
		
		
		//for each unsafe state, compute its corresponding interval
		ArrayList<Integer> intervalLowerBounds = new ArrayList<Integer>();
		ArrayList<Integer> intervalUpperBounds = new ArrayList<Integer>();
		
		for(int i=0; i<unsafeMinterms.size(); i++){
			String unsafeState = unsafeMinterms.get(i);
			
			System.out.println("minterm is "+unsafeState);
			
			//parse the unsafe state to a <i_1, i_2,..,i_p> tuple
			//we only need to keep the lowerbound and upperbound
			int lowerBound = 2*numOfProcesses; 
			int upperBound = 0;
			int currentIndex = 0;
			for(int j=0; j<numOfProcesses; j++){
				int p_j_numOfVars = processVariables.get(j).length;
				String p_j_minterm= unsafeState.substring(currentIndex, currentIndex+p_j_numOfVars);
				currentIndex += p_j_numOfVars;
				
				//translate to integer
				int p_j_state = Integer.parseInt(p_j_minterm, 2);
				
				if(p_j_state < lowerBound){
					lowerBound = p_j_state;
				}
				
				if(p_j_state > upperBound){
					upperBound = p_j_state;
				}
				
				
			}
			
			System.out.println("lower bound is "+lowerBound);
			System.out.println("upper bound is "+upperBound);
			
			//add the corresponding interval
			boolean addInterval = true; 
			for(int k=0; k<intervalLowerBounds.size();k++){
				if(lowerBound == intervalLowerBounds.get(k)){
					if(upperBound == intervalUpperBounds.get(k));{
						//interval already exist
						addInterval = false;
						break;
					}
				}
			}
			if(addInterval){
				intervalLowerBounds.add(lowerBound);
				intervalUpperBounds.add(upperBound);
			}
		}
		
		System.out.println("printing intervals");
		for(int i=0;i<intervalLowerBounds.size();i++){
			System.out.print("["+intervalLowerBounds.get(i)+" , "+intervalUpperBounds.get(i)+"]");
		}
		
		//find a set of synchronization points that covers all the intervals
		//TODO: check refined algorithms for hitting many intervals by Damschke
	}
	
	//Synthesis with partial synchronization and iterative and compositional algorithm for computing winning set
	//Generalizing to arbitrary length processes
	private static void test5(){
		BDD bdd = new BDD(10000, 1000);
			
		int numOfProcesses = 2;
		int processLength = 8;
		
		
		
		//advance variables
		Variable[][] advanceVariables = new Variable[numOfProcesses][numOfProcesses];
			
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<numOfProcesses; j++){
				if(i==j){ 
					advanceVariables[i][j] =null;
				}else{
					advanceVariables[i][j] = Variable.createVariableAndPrimedVariable(bdd, "a_"+i+"_"+j);
				}
			}
		}
		

		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
			
		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			processVariables.add(p_i);
		}
			
		//synchronization variables 
		Variable[][][] synchronizationVariables= new Variable[numOfProcesses][processLength][numOfProcesses];
			
		for(int i=0; i<numOfProcesses; i++){
			for(int l=0; l<processLength; l++){
				for(int j=0; j<numOfProcesses; j++){
					if(i==j){ 
						synchronizationVariables[i][l][j] = null;
					}else{
						synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
					}
						
				}
			}
		}	


		
		
		
//		for(int l=0; l<processLength; l++){
//			for(int i=0; i<numOfProcesses; i++){
//				for(int j=0; j<numOfProcesses; j++){
//					if(i==j){ 
//						synchronizationVariables[i][l][j] = null;
//					}else{
//						synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
//					}
//						
//				}
//			}
//		}
		
		//TODO: Scheduling variables are not needed when we separate the liveness from safety, remove them
				//scheduling vars
				Variable[] schedulingVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(numOfProcesses-1), "alpha");	


				
				
		
		//local programs
			
		int loopStarts = 0;
			
		int[] processes = new int[numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			Variable[] piVars = processVariables.get(i);
			processes[i]=bdd.ref(bdd.getZero());
			for(int j=0; j<2*processLength;j++){
				int q_i_j = BDDWrapper.assign(bdd, j, piVars);
				int next = j+1;
				if(next == 2*processLength){
					next = loopStarts;
				}
				int q_i_next = BDDWrapper.assign(bdd, next, Variable.getPrimedCopy(piVars));
				int trans = BDDWrapper.and(bdd, q_i_j, q_i_next);
				BDDWrapper.free(bdd, q_i_j);
				BDDWrapper.free(bdd, q_i_next);
				processes[i] = BDDWrapper.orTo(bdd, processes[i], trans);
				BDDWrapper.free(bdd, trans);
			}
		}
			
			
		//initial states
		int[] processesInit=new int[numOfProcesses];
		int init = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			processesInit[i] = BDDWrapper.assign(bdd, 0, processVariables.get(i));
			init = BDDWrapper.andTo(bdd, init, processesInit[i]);
		}
			
			
		//initially all advance variables are false
		for(int i =0; i<advanceVariables.length;i++){
			for(int j=0; j<advanceVariables[i].length;j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					init = BDDWrapper.andTo(bdd, init, bdd.not(v.getBDDVar()));
				}
			}
		}
		
		//the synchronization signals that are already set 
		//all the processes synchronize at the end of their loop
		int partialSynch = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			for(int j=i+1; j<numOfProcesses; j++){
				Variable s_i_j_last = synchronizationVariables[i][processLength-3][j];
				Variable s_j_i_last = synchronizationVariables[j][processLength-3][i]; 
				
				int i_j_synch = BDDWrapper.and(bdd, s_i_j_last.getBDDVar(), s_j_i_last.getBDDVar());
				partialSynch = BDDWrapper.andTo(bdd, partialSynch, i_j_synch);
				BDDWrapper.free(bdd, i_j_synch);
			}
		}
		
		init = BDDWrapper.andTo(bdd, init, partialSynch);
			
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
		
		long t_gameConstruction = UtilityMethods.timeStamp();
		//form the synchronization game structure
		SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
					advanceVariables, schedulingVars, processes, processesInit);
		
		UtilityMethods.duration(t_gameConstruction, "game constructed in ");
			
		sgs.printGameVars();
//		UtilityMethods.getUserInput();
		
		
		//TODO: specify the safety objective
		//not(q3 & u1) & not(q3 & u7) & not(q5 & u1) & not(q5 & u7)
//		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
//		int q5 = BDDWrapper.assign(bdd, 5, processVariables.get(0));
//		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
//		int u7 = BDDWrapper.assign(bdd, 7, processVariables.get(1));
//		int notq3u1 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u1));
//		int notq3u7 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u7));
//		int notq5u1 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u1));
//		int notq5u7 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u7));
//			
//		int safetySet = BDDWrapper.and(bdd, notq3u1, notq3u7);
//		safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u1);
//		safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u7);
		
		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
		int q7 = BDDWrapper.assign(bdd, 7, processVariables.get(0));
		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
		int u5 = BDDWrapper.assign(bdd, 5, processVariables.get(1));
//		int v1 = BDDWrapper.assign(bdd, 1, processVariables.get(2));
//		int v5 = BDDWrapper.assign(bdd, 5, processVariables.get(2));
		int q3Orq7 = BDDWrapper.or(bdd, q3, q7);
		int u1Oru5 = BDDWrapper.or(bdd, u1, u5);
//		int v1Orv5 = BDDWrapper.or(bdd, v1, v5);
		
		int notSafe = BDDWrapper.and(bdd, q3Orq7, u1Oru5);
//		notSafe = BDDWrapper.andTo(bdd, notSafe, v1Orv5);
			
		int safetySet = bdd.ref(bdd.not(notSafe));
		
		//form the game
			
		//compute the solution
		long t0 = UtilityMethods.timeStamp();
//		int winning = sgs.greatestFixedPoint(safetySet, init);
		int winning = sgs.winningRegion2(safetySet, init);
		UtilityMethods.duration(t0, "the greatest fixed point was computed in");
		
		int initialWinning = BDDWrapper.and(bdd, winning, init);
			
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
			
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		int winningSynchSkeletons = BDDWrapper.exists(bdd, initialWinning, synchCube);
			
//		UtilityMethods.debugBDDMethods(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
		
//		//test the solution 
//		//choose one synchronization skeleton 
//		int chosen = BDDWrapper.oneSatisfyingAssignment(bdd, winningSynchSkeletons, sgs.allSynchronizationSignals);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen synch skeleton is ", chosen);
//		
//		sgs.restrictSynchronizationSkeleton(chosen);
//		
//		int currentState = BDDWrapper.and(bdd, init, chosen);
//		for(int i=0; i<10; i++){
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "current state is ", currentState);
//			int nextStates = sgs.symbolicGameOneStepExecution(currentState);
//			currentState = BDDWrapper.oneRandomMinterm(bdd, nextStates, sgs.variables);
//			
//		}
		
		
	}
	
	//Synthesis with partial synchronization
	//Generalizing to arbitrary length processes
	private static void test4(){
		BDD bdd = new BDD(10000, 1000);
			
		int numOfProcesses = 3;
		int processLength = 4;
		
		
		
		//advance variables
		Variable[][] advanceVariables = new Variable[numOfProcesses][numOfProcesses];
			
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<numOfProcesses; j++){
				if(i==j){ 
					advanceVariables[i][j] =null;
				}else{
					advanceVariables[i][j] = Variable.createVariableAndPrimedVariable(bdd, "a_"+i+"_"+j);
				}
			}
		}
		

		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
			
		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			processVariables.add(p_i);
		}
			
		//synchronization variables 
		Variable[][][] synchronizationVariables= new Variable[numOfProcesses][processLength][numOfProcesses];
			
		for(int i=0; i<numOfProcesses; i++){
			for(int l=0; l<processLength; l++){
				for(int j=0; j<numOfProcesses; j++){
					if(i==j){ 
						synchronizationVariables[i][l][j] = null;
					}else{
						synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
					}
						
				}
			}
		}	


		
		
		
//		for(int l=0; l<processLength; l++){
//			for(int i=0; i<numOfProcesses; i++){
//				for(int j=0; j<numOfProcesses; j++){
//					if(i==j){ 
//						synchronizationVariables[i][l][j] = null;
//					}else{
//						synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
//					}
//						
//				}
//			}
//		}
		
		//TODO: Scheduling variables are not needed when we separate the liveness from safety, remove them
				//scheduling vars
				Variable[] schedulingVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(numOfProcesses-1), "alpha");	


				
				
		
		//local programs
			
		int loopStarts = 0;
			
		int[] processes = new int[numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			Variable[] piVars = processVariables.get(i);
			processes[i]=bdd.ref(bdd.getZero());
			for(int j=0; j<2*processLength;j++){
				int q_i_j = BDDWrapper.assign(bdd, j, piVars);
				int next = j+1;
				if(next == 2*processLength){
					next = loopStarts;
				}
				int q_i_next = BDDWrapper.assign(bdd, next, Variable.getPrimedCopy(piVars));
				int trans = BDDWrapper.and(bdd, q_i_j, q_i_next);
				BDDWrapper.free(bdd, q_i_j);
				BDDWrapper.free(bdd, q_i_next);
				processes[i] = BDDWrapper.orTo(bdd, processes[i], trans);
				BDDWrapper.free(bdd, trans);
			}
		}
			
			
		//initial states
		int[] processesInit=new int[numOfProcesses];
		int init = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			processesInit[i] = BDDWrapper.assign(bdd, 0, processVariables.get(i));
			init = BDDWrapper.andTo(bdd, init, processesInit[i]);
		}
			
			
		//initially all advance variables are false
		for(int i =0; i<advanceVariables.length;i++){
			for(int j=0; j<advanceVariables[i].length;j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					init = BDDWrapper.andTo(bdd, init, bdd.not(v.getBDDVar()));
				}
			}
		}
		
		//the synchronization signals that are already set 
		//all the processes synchronize at the end of their loop
		int partialSynch = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			for(int j=i+1; j<numOfProcesses; j++){
				Variable s_i_j_last = synchronizationVariables[i][processLength-3][j];
				Variable s_j_i_last = synchronizationVariables[j][processLength-3][i]; 
				
				int i_j_synch = BDDWrapper.and(bdd, s_i_j_last.getBDDVar(), s_j_i_last.getBDDVar());
				partialSynch = BDDWrapper.andTo(bdd, partialSynch, i_j_synch);
				BDDWrapper.free(bdd, i_j_synch);
			}
		}
		
		init = BDDWrapper.andTo(bdd, init, partialSynch);
			
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
		
		long t_gameConstruction = UtilityMethods.timeStamp();
		//form the synchronization game structure
		SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
					advanceVariables, schedulingVars, processes, processesInit);
		
		UtilityMethods.duration(t_gameConstruction, "game constructed in ");
			
		sgs.printGameVars();
//		UtilityMethods.getUserInput();
		
		
		//TODO: specify the safety objective
		//not(q3 & u1) & not(q3 & u7) & not(q5 & u1) & not(q5 & u7)
//		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
//		int q5 = BDDWrapper.assign(bdd, 5, processVariables.get(0));
//		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
//		int u7 = BDDWrapper.assign(bdd, 7, processVariables.get(1));
//		int notq3u1 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u1));
//		int notq3u7 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u7));
//		int notq5u1 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u1));
//		int notq5u7 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u7));
//			
//		int safetySet = BDDWrapper.and(bdd, notq3u1, notq3u7);
//		safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u1);
//		safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u7);
		
		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
		int q7 = BDDWrapper.assign(bdd, 7, processVariables.get(0));
		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
		int u5 = BDDWrapper.assign(bdd, 5, processVariables.get(1));
//		int v1 = BDDWrapper.assign(bdd, 1, processVariables.get(2));
//		int v5 = BDDWrapper.assign(bdd, 5, processVariables.get(2));
		int q3Orq7 = BDDWrapper.or(bdd, q3, q7);
		int u1Oru5 = BDDWrapper.or(bdd, u1, u5);
//		int v1Orv5 = BDDWrapper.or(bdd, v1, v5);
		
		int notSafe = BDDWrapper.and(bdd, q3Orq7, u1Oru5);
//		notSafe = BDDWrapper.andTo(bdd, notSafe, v1Orv5);
			
		int safetySet = bdd.ref(bdd.not(notSafe));
		
		//form the game
			
		//compute the solution
		long t0 = UtilityMethods.timeStamp();
		int winning = sgs.greatestFixedPoint(safetySet, init);
		UtilityMethods.duration(t0, "the greatest fixed point was computed in");
		
		int initialWinning = BDDWrapper.and(bdd, winning, init);
			
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
			
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		int winningSynchSkeletons = BDDWrapper.exists(bdd, initialWinning, synchCube);
			
//		UtilityMethods.debugBDDMethods(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
		
//		//test the solution 
//		//choose one synchronization skeleton 
//		int chosen = BDDWrapper.oneSatisfyingAssignment(bdd, winningSynchSkeletons, sgs.allSynchronizationSignals);
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen synch skeleton is ", chosen);
//		
//		sgs.restrictSynchronizationSkeleton(chosen);
//		
//		int currentState = BDDWrapper.and(bdd, init, chosen);
//		for(int i=0; i<10; i++){
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "current state is ", currentState);
//			int nextStates = sgs.symbolicGameOneStepExecution(currentState);
//			currentState = BDDWrapper.oneRandomMinterm(bdd, nextStates, sgs.variables);
//			
//		}
		
		
	}
	
	public void restrictSynchronizationSkeleton(int chosenSynchronizationSkeleton){
		asynchronousTransitionRelation = BDDWrapper.andTo(bdd, asynchronousTransitionRelation, chosenSynchronizationSkeleton);
	}
	
	//Generalizing to arbitrary length processes
	private static void test3(){
		BDD bdd = new BDD(10000, 1000);
		
		int numOfProcesses = 2;
		int processLength = 4;
		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
		
//		Variable A1 = Variable.createVariableAndPrimedVariable(bdd, "A1");
//		Variable B1 = Variable.createVariableAndPrimedVariable(bdd, "B1");
//		Variable M1 = Variable.createVariableAndPrimedVariable(bdd, "M1");
//		Variable[] p1 = new Variable[]{A1,B1,M1};
		
		for(int i=0; i<numOfProcesses; i++){
			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
			processVariables.add(p_i);
		}
		
		
		//synchronization variables 
		Variable[][][] synchronizationVariables= new Variable[numOfProcesses][processLength][numOfProcesses];
		
		for(int i=0; i<numOfProcesses; i++){
			for(int l=0; l<processLength; l++){
				for(int j=0; j<numOfProcesses; j++){
					if(i==j){ 
						synchronizationVariables[i][l][j] = null;
					}else{
						synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
					}
					
				}
			}
		}
		
		//advance variables
		Variable[][] advanceVariables = new Variable[numOfProcesses][numOfProcesses];
		
		for(int i=0; i<numOfProcesses; i++){
			for(int j=0; j<numOfProcesses; j++){
				if(i==j){ 
					advanceVariables[i][j] =null;
				}else{
					advanceVariables[i][j] = Variable.createVariableAndPrimedVariable(bdd, "a_"+i+"_"+j);
				}
			}
		}
		
		//scheduling vars
		Variable[] schedulingVars = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(numOfProcesses-1), "alpha");
		
		//local programs
		
		int loopStarts = 0;
		
		int[] processes = new int[numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			Variable[] piVars = processVariables.get(i);
			processes[i]=bdd.ref(bdd.getZero());
			for(int j=0; j<2*processLength;j++){
				int q_i_j = BDDWrapper.assign(bdd, j, piVars);
				int next = j+1;
				if(next == 2*processLength){
					next = loopStarts;
				}
				int q_i_next = BDDWrapper.assign(bdd, next, Variable.getPrimedCopy(piVars));
				int trans = BDDWrapper.and(bdd, q_i_j, q_i_next);
				BDDWrapper.free(bdd, q_i_j);
				BDDWrapper.free(bdd, q_i_next);
				processes[i] = BDDWrapper.orTo(bdd, processes[i], trans);
				BDDWrapper.free(bdd, trans);
			}
		}
		
		
		//initial states
		int[] processesInit=new int[numOfProcesses];
		int init = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses; i++){
			processesInit[i] = BDDWrapper.assign(bdd, 0, processVariables.get(i));
			init = BDDWrapper.andTo(bdd, init, processesInit[i]);
		}
		
		
		//initially all advance variables are false
		for(int i =0; i<advanceVariables.length;i++){
			for(int j=0; j<advanceVariables[i].length;j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					init = BDDWrapper.andTo(bdd, init, bdd.not(v.getBDDVar()));
				}
			}
		}
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
		
		//form the synchronization game structure
		SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
				advanceVariables, schedulingVars, processes, processesInit);
		
		sgs.printGameVars();
		UtilityMethods.getUserInput();
		
		
		//specify the GR1 objective
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
		//always eventually each process is scheduled
		for(int i=0; i<numOfProcesses; i++){
			int assumption_i = BDDWrapper.assign(bdd, i, schedulingVars);
			assumptions.add(assumption_i);
			
			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumption_i);
		}
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		
		
		//always eventually q0 & u0
		int allProcesses0 = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses;i++){
			int tmp = BDDWrapper.assign(bdd, 0, processVariables.get(i));
			allProcesses0 = BDDWrapper.andTo(bdd, allProcesses0, tmp);
			BDDWrapper.free(bdd, tmp);
		}
		
		guarantees.add(allProcesses0);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee 1 ", allProcesses0);
		
		//always eventually q4 & u4
		int allProcesses4 = bdd.ref(bdd.getOne());
		for(int i=0; i<numOfProcesses;i++){
			int tmp = BDDWrapper.assign(bdd, 4, processVariables.get(i));
			allProcesses4 = BDDWrapper.andTo(bdd, allProcesses4, tmp);
			BDDWrapper.free(bdd, tmp);
		}
		
		guarantees.add(allProcesses4);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee 2 ", allProcesses4);
		
		//not(q3 & u1) & not(q3 & u7) & not(q5 & u1) & not(q5 & u7)
		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
		int q5 = BDDWrapper.assign(bdd, 5, processVariables.get(0));
		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
		int u7 = BDDWrapper.assign(bdd, 7, processVariables.get(1));
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		int notq3u1 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u1));
		int notq3u7 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u7));
		int notq5u1 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u1));
		int notq5u7 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u7));
		safetyGuarantees.add(notq3u1);
		safetyGuarantees.add(notq3u7);
		safetyGuarantees.add(notq5u1);
		safetyGuarantees.add(notq5u7);
		
		GR1Objective obj = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees, null, safetyGuarantees);
		
		//form the game
		
		//compute the solution
		int winning = GR1Solver.computeWinningStates_noMemory(new solver.BDDWrapper(bdd), sgs, init, obj);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		int winningSynchSkeletons = BDDWrapper.exists(bdd, winning, synchCube);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
	}
	
	private static void test1(){
		BDD bdd = new BDD(10000, 1000);
		
		int numOfProcesses = 2;
		int processLength = 2;
		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
		
//		Variable A1 = Variable.createVariableAndPrimedVariable(bdd, "A1");
//		Variable B1 = Variable.createVariableAndPrimedVariable(bdd, "B1");
//		Variable M1 = Variable.createVariableAndPrimedVariable(bdd, "M1");
//		Variable[] p1 = new Variable[]{A1,B1,M1};
		
		Variable[] p1 = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "vars1");
		
//		Variable C2 = Variable.createVariableAndPrimedVariable(bdd, "C2");
//		Variable D2 = Variable.createVariableAndPrimedVariable(bdd, "D2");
//		Variable M2 = Variable.createVariableAndPrimedVariable(bdd, "M2");
//		Variable[] p2 = new Variable[]{C2,D2,M2};
		
		Variable[] p2 = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "vars2");
		
		processVariables.add(p1);
		processVariables.add(p2);
		
		//synchronization variables 
		Variable[][][] synchronizationVariables= new Variable[numOfProcesses][processLength][numOfProcesses];
		
		//i=j then null
		synchronizationVariables[0][0][0]=null; 
		synchronizationVariables[0][0][1]=Variable.createVariableAndPrimedVariable(bdd, "S_0_1_0");
		synchronizationVariables[0][1][0]=null;
		synchronizationVariables[0][1][1]=Variable.createVariableAndPrimedVariable(bdd, "S_0_1_1");
		
		synchronizationVariables[1][0][0]=Variable.createVariableAndPrimedVariable(bdd, "S_1_0_0");
		synchronizationVariables[1][0][1]=null;
		synchronizationVariables[1][1][0]=Variable.createVariableAndPrimedVariable(bdd, "S_1_0_1");
		synchronizationVariables[1][1][1]=null;
		
		
		//advance variables
		Variable[][] advanceVariables = new Variable[numOfProcesses][numOfProcesses];
		
		advanceVariables[0][0] = null;
		advanceVariables[0][1] = Variable.createVariableAndPrimedVariable(bdd, "a_0_1");
		
		advanceVariables[1][0] = Variable.createVariableAndPrimedVariable(bdd, "a_1_0");
		advanceVariables[1][1] = null;
		
		//scheduling vars 
		Variable[] schedulingVars = new Variable[]{Variable.createVariableAndPrimedVariable(bdd, "alpha")};
		
		//local programs
		
		
		//Repeat: q0: A1 \wedge not B1 --> q1: A1 \wedge B1 --> q2: not A1 \wedge B1 --> q3: A1 \wedge B1 
		int q0 = BDDWrapper.assign(bdd, 0, p1);
		int q0Prime = BDDWrapper.assign(bdd,0, Variable.getPrimedCopy(p1));
		int q1 = BDDWrapper.assign(bdd, 1, p1);
		int q1Prime = BDDWrapper.assign(bdd,1, Variable.getPrimedCopy(p1));
		int q2 = BDDWrapper.assign(bdd, 2, p1);
		int q2Prime = BDDWrapper.assign(bdd,2, Variable.getPrimedCopy(p1));
		int q3 = BDDWrapper.assign(bdd, 3, p1);
		int q3Prime = BDDWrapper.assign(bdd,3, Variable.getPrimedCopy(p1));
		
		int t0 = BDDWrapper.and(bdd, q0, q1Prime);
		int t1 = BDDWrapper.and(bdd, q1, q2Prime);
		int t2 = BDDWrapper.and(bdd, q2, q3Prime);
		int t3 = BDDWrapper.and(bdd, q3, q0Prime);
		
		int trans1 = BDDWrapper.or(bdd, t0, t1);
		trans1 = BDDWrapper.orTo(bdd, trans1, t2);
		trans1 = BDDWrapper.orTo(bdd, trans1, t3);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "process 1", trans1);
		
//		BDDWrapper.free(bdd, q0);
		BDDWrapper.free(bdd, q0Prime);
		BDDWrapper.free(bdd, q1);
		BDDWrapper.free(bdd, q1Prime);
//		BDDWrapper.free(bdd, q2);
		BDDWrapper.free(bdd, q2Prime);
		BDDWrapper.free(bdd, q3);
		BDDWrapper.free(bdd, q3Prime);
		BDDWrapper.free(bdd, t0);
		BDDWrapper.free(bdd, t1);
		BDDWrapper.free(bdd, t2);
		BDDWrapper.free(bdd, t3);
		
		//Repeat: C2 \wedge not D2 --> C2 \wedge D2 --> not C2 \wedge D2 --> C2 \wedge D2 
		int u0 = BDDWrapper.assign(bdd, 0, p2);
		int u0Prime = BDDWrapper.assign(bdd,0, Variable.getPrimedCopy(p2));
		int u1 = BDDWrapper.assign(bdd, 1, p2);
		int u1Prime = BDDWrapper.assign(bdd,1, Variable.getPrimedCopy(p2));
		int u2 = BDDWrapper.assign(bdd, 2, p2);
		int u2Prime = BDDWrapper.assign(bdd,2, Variable.getPrimedCopy(p2));
		int u3 = BDDWrapper.assign(bdd, 3, p2);
		int u3Prime = BDDWrapper.assign(bdd,3, Variable.getPrimedCopy(p2));
		
		int r0 = BDDWrapper.and(bdd, u0, u1Prime);
		int r1 = BDDWrapper.and(bdd, u1, u2Prime);
		int r2 = BDDWrapper.and(bdd, u2, u3Prime);
		int r3 = BDDWrapper.and(bdd, u3, u0Prime);
		
		int trans2 = BDDWrapper.or(bdd, r0, r1);
		trans2 = BDDWrapper.orTo(bdd, trans2, r2);
		trans2 = BDDWrapper.orTo(bdd, trans2, r3);
		
		UtilityMethods.debugBDDMethods(bdd, "process 2", trans2);
		
//		BDDWrapper.free(bdd, u0);
		BDDWrapper.free(bdd, u0Prime);
		BDDWrapper.free(bdd, u1);
		BDDWrapper.free(bdd, u1Prime);
//		BDDWrapper.free(bdd, u2);
		BDDWrapper.free(bdd, u2Prime);
		BDDWrapper.free(bdd, u3);
		BDDWrapper.free(bdd, u3Prime);
		BDDWrapper.free(bdd, r0);
		BDDWrapper.free(bdd, r1);
		BDDWrapper.free(bdd, r2);
		BDDWrapper.free(bdd, r3);
		
		int[] processes =  new int[]{trans1,trans2};
		
		//initial states
		//initially A1
		//initially C2
		int[] processesInit=new int[]{q0,u0};
		
		int init = BDDWrapper.and(bdd, q0, u0);
		
		//initially all advance variables are false
		for(int i =0; i<advanceVariables.length;i++){
			for(int j=0; j<advanceVariables[i].length;j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					init = BDDWrapper.andTo(bdd, init, bdd.not(v.getBDDVar()));
				}
			}
		}
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
		
		//form the synchronization game structure
		SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
				advanceVariables, schedulingVars, processes, processesInit);
		
		
		//specify the GR1 objective
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
		//always eventually each process is scheduled
		int p1Scheduled = BDDWrapper.assign(bdd, 0, schedulingVars);
		int p2Scheduled = BDDWrapper.assign(bdd, 1, schedulingVars);
		assumptions.add(p1Scheduled);
		assumptions.add(p2Scheduled);
		
		UtilityMethods.debugBDDMethods(bdd, "assumption 1", p1Scheduled);
		UtilityMethods.debugBDDMethods(bdd, "assumption 2", p2Scheduled);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//always eventually q0 & u0
		int q0u0 = BDDWrapper.and(bdd, q0, u0);
		
		//always eventually q2 & u2
		int q2u2 = BDDWrapper.and(bdd, q2, u2);
		
		guarantees.add(q0u0);
		guarantees.add(q2u2);
		
		UtilityMethods.debugBDDMethods(bdd, "guarantee 1", q0u0);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee 2", q2u2);
		
		GR1Objective obj = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		//form the game
		
		//compute the solution
		int winning = GR1Solver.computeWinningStates_noMemory(new solver.BDDWrapper(bdd), sgs, init, obj);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		int winningSynchSkeletons = BDDWrapper.exists(bdd, winning, synchCube);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
	}

	private static void test2_unrealizableObjective(){
		BDD bdd = new BDD(10000, 1000);
		
		int numOfProcesses = 2;
		int processLength = 2;
		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
		
//		Variable A1 = Variable.createVariableAndPrimedVariable(bdd, "A1");
//		Variable B1 = Variable.createVariableAndPrimedVariable(bdd, "B1");
//		Variable M1 = Variable.createVariableAndPrimedVariable(bdd, "M1");
//		Variable[] p1 = new Variable[]{A1,B1,M1};
		
		Variable[] p1 = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "vars1");
		
//		Variable C2 = Variable.createVariableAndPrimedVariable(bdd, "C2");
//		Variable D2 = Variable.createVariableAndPrimedVariable(bdd, "D2");
//		Variable M2 = Variable.createVariableAndPrimedVariable(bdd, "M2");
//		Variable[] p2 = new Variable[]{C2,D2,M2};
		
		Variable[] p2 = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "vars2");
		
		processVariables.add(p1);
		processVariables.add(p2);
		
		//synchronization variables 
		Variable[][][] synchronizationVariables= new Variable[numOfProcesses][processLength][numOfProcesses];
		
		//i=j then null
		synchronizationVariables[0][0][0]=null; 
		synchronizationVariables[0][0][1]=Variable.createVariableAndPrimedVariable(bdd, "S_0_1_0");
		synchronizationVariables[0][1][0]=null;
		synchronizationVariables[0][1][1]=Variable.createVariableAndPrimedVariable(bdd, "S_0_1_1");
		
		synchronizationVariables[1][0][0]=Variable.createVariableAndPrimedVariable(bdd, "S_1_0_0");
		synchronizationVariables[1][0][1]=null;
		synchronizationVariables[1][1][0]=Variable.createVariableAndPrimedVariable(bdd, "S_1_0_1");
		synchronizationVariables[1][1][1]=null;
		
		
		//advance variables
		Variable[][] advanceVariables = new Variable[numOfProcesses][numOfProcesses];
		
		advanceVariables[0][0] = null;
		advanceVariables[0][1] = Variable.createVariableAndPrimedVariable(bdd, "a_0_1");
		
		advanceVariables[1][0] = Variable.createVariableAndPrimedVariable(bdd, "a_1_0");
		advanceVariables[1][1] = null;
		
		//scheduling vars 
		Variable[] schedulingVars = new Variable[]{Variable.createVariableAndPrimedVariable(bdd, "alpha")};
		
		//local programs
		
		
		//Repeat: q0: A1 \wedge not B1 --> q1: A1 \wedge B1 --> q2: not A1 \wedge B1 --> q3: A1 \wedge B1 
		int q0 = BDDWrapper.assign(bdd, 0, p1);
		int q0Prime = BDDWrapper.assign(bdd,0, Variable.getPrimedCopy(p1));
		int q1 = BDDWrapper.assign(bdd, 1, p1);
		int q1Prime = BDDWrapper.assign(bdd,1, Variable.getPrimedCopy(p1));
		int q2 = BDDWrapper.assign(bdd, 2, p1);
		int q2Prime = BDDWrapper.assign(bdd,2, Variable.getPrimedCopy(p1));
		int q3 = BDDWrapper.assign(bdd, 3, p1);
		int q3Prime = BDDWrapper.assign(bdd,3, Variable.getPrimedCopy(p1));
		
		int t0 = BDDWrapper.and(bdd, q0, q1Prime);
		int t1 = BDDWrapper.and(bdd, q1, q2Prime);
		int t2 = BDDWrapper.and(bdd, q2, q3Prime);
		int t3 = BDDWrapper.and(bdd, q3, q0Prime);
		
		int trans1 = BDDWrapper.or(bdd, t0, t1);
		trans1 = BDDWrapper.orTo(bdd, trans1, t2);
		trans1 = BDDWrapper.orTo(bdd, trans1, t3);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "process 1", trans1);
		
//		BDDWrapper.free(bdd, q0);
		BDDWrapper.free(bdd, q0Prime);
//		BDDWrapper.free(bdd, q1);
		BDDWrapper.free(bdd, q1Prime);
//		BDDWrapper.free(bdd, q2);
		BDDWrapper.free(bdd, q2Prime);
//		BDDWrapper.free(bdd, q3);
		BDDWrapper.free(bdd, q3Prime);
		BDDWrapper.free(bdd, t0);
		BDDWrapper.free(bdd, t1);
		BDDWrapper.free(bdd, t2);
		BDDWrapper.free(bdd, t3);
		
		//Repeat: C2 \wedge not D2 --> C2 \wedge D2 --> not C2 \wedge D2 --> C2 \wedge D2 
		int u0 = BDDWrapper.assign(bdd, 0, p2);
		int u0Prime = BDDWrapper.assign(bdd,0, Variable.getPrimedCopy(p2));
		int u1 = BDDWrapper.assign(bdd, 1, p2);
		int u1Prime = BDDWrapper.assign(bdd,1, Variable.getPrimedCopy(p2));
		int u2 = BDDWrapper.assign(bdd, 2, p2);
		int u2Prime = BDDWrapper.assign(bdd,2, Variable.getPrimedCopy(p2));
		int u3 = BDDWrapper.assign(bdd, 3, p2);
		int u3Prime = BDDWrapper.assign(bdd,3, Variable.getPrimedCopy(p2));
		
		int r0 = BDDWrapper.and(bdd, u0, u1Prime);
		int r1 = BDDWrapper.and(bdd, u1, u2Prime);
		int r2 = BDDWrapper.and(bdd, u2, u3Prime);
		int r3 = BDDWrapper.and(bdd, u3, u0Prime);
		
		int trans2 = BDDWrapper.or(bdd, r0, r1);
		trans2 = BDDWrapper.orTo(bdd, trans2, r2);
		trans2 = BDDWrapper.orTo(bdd, trans2, r3);
		
		UtilityMethods.debugBDDMethods(bdd, "process 2", trans2);
		
//		BDDWrapper.free(bdd, u0);
		BDDWrapper.free(bdd, u0Prime);
//		BDDWrapper.free(bdd, u1);
		BDDWrapper.free(bdd, u1Prime);
//		BDDWrapper.free(bdd, u2);
		BDDWrapper.free(bdd, u2Prime);
//		BDDWrapper.free(bdd, u3);
		BDDWrapper.free(bdd, u3Prime);
		BDDWrapper.free(bdd, r0);
		BDDWrapper.free(bdd, r1);
		BDDWrapper.free(bdd, r2);
		BDDWrapper.free(bdd, r3);
		
		int[] processes =  new int[]{trans1,trans2};
		
		//initial states
		//initially A1
		//initially C2
		int[] processesInit=new int[]{q0,u0};
		
		int init = BDDWrapper.and(bdd, q0, u0);
		
		//initially all advance variables are false
		for(int i =0; i<advanceVariables.length;i++){
			for(int j=0; j<advanceVariables[i].length;j++){
				Variable v = advanceVariables[i][j];
				if(v != null){
					init = BDDWrapper.andTo(bdd, init, bdd.not(v.getBDDVar()));
				}
			}
		}
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
		
		//form the synchronization game structure
		SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
				advanceVariables, schedulingVars, processes, processesInit);
		
		
		//specify the GR1 objective
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		
		//always eventually each process is scheduled
		int p1Scheduled = BDDWrapper.assign(bdd, 0, schedulingVars);
		int p2Scheduled = BDDWrapper.assign(bdd, 1, schedulingVars);
		assumptions.add(p1Scheduled);
		assumptions.add(p2Scheduled);
		
		UtilityMethods.debugBDDMethods(bdd, "assumption 1", p1Scheduled);
		UtilityMethods.debugBDDMethods(bdd, "assumption 2", p2Scheduled);
		
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//always eventually q0 & u0
		int q0u0 = BDDWrapper.and(bdd, q0, u0);
		
		//always eventually q2 & u2
		int q2u2 = BDDWrapper.and(bdd, q2, u2);
		
		guarantees.add(q0u0);
		guarantees.add(q2u2);
		
		UtilityMethods.debugBDDMethods(bdd, "guarantee 1", q0u0);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "guarantee 2", q2u2);
		
		ArrayList<Integer> safetyGuarantees = new ArrayList<Integer>();
		int notq1u1 = BDDWrapper.or(bdd, bdd.not(q1), bdd.not(u1));
		int notq3u3 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u3));
		safetyGuarantees.add(notq1u1);
		safetyGuarantees.add(notq3u3);
		
//		GR1Objective obj = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		GR1Objective obj = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees, null, safetyGuarantees);
		
		//form the game
		
		//compute the solution
		int winning = GR1Solver.computeWinningStates_noMemory(new solver.BDDWrapper(bdd), sgs, init, obj);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		int winningSynchSkeletons = BDDWrapper.exists(bdd, winning, synchCube);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
	}
	
	public int greatestFixedPoint(int set, int init){
		int Q=bdd.ref(bdd.getOne());
		int Qprime = bdd.ref(set);
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		do{
			bdd.deref(Q);
			Q = bdd.ref(Qprime);
			int pre = controllablePredecessor(Qprime);
			Qprime = bdd.ref(bdd.and(Qprime, pre));
			bdd.deref(pre);	
			
			int initialWinning = bdd.ref(bdd.and(Qprime, init));
			int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
			Qprime = BDDWrapper.andTo(bdd, Qprime, winningSynchSkeletons);
			BDDWrapper.free(bdd, initialWinning);
			BDDWrapper.free(bdd, winningSynchSkeletons);
			
			numOfIterations++;
			
		}while(Q != Qprime);
		bdd.deref(Qprime);
		BDDWrapper.free(bdd, synchCube);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return Q;
	}
	
	/**
	 * an iterative algorithm for computing the set of winning states iteratively using partition transition relation
	 * @param set
	 * @param init
	 * @return
	 */
	public int winningRegion(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		do{
			fixedPoint = true;
			//for each process 
			for(int i=0; i<processes.length; i++){
				//for each state of the process
				ArrayList<Integer> p_i_states = processStates.get(i);
				int trans_i = partitionTransitionRelation.get(i);
				for(int j=0; j<p_i_states.size(); j++){
					//compute the set of possible winning states for q_j of process p_i
					int q_j = p_i_states.get(j);
					int safe_q_j = BDDWrapper.and(bdd, winning, q_j);
					int post = BDDWrapper.EpostImage(bdd, safe_q_j, getVariablesAndActionsCube(), trans_i, getVprimetoV());
					BDDWrapper.free(bdd, safe_q_j);
					int unsafePost = BDDWrapper.and(bdd, post, bdd.not(winning));
					BDDWrapper.free(bdd, post);
					int unsafePre = BDDWrapper.EpreImage(bdd, unsafePost, getPrimedVariablesAndActionsCube(), trans_i, getVtoVprime());
					BDDWrapper.free(bdd, unsafePost);
					int tmp = BDDWrapper.and(bdd, winning, bdd.not(unsafePre));
					if(tmp != winning){
						fixedPoint = false;
						BDDWrapper.free(bdd, winning);
						winning = tmp;
						
						//prune the set of synchronization skeletons based on current winning set
						int initialWinning = bdd.ref(bdd.and(winning, init));
						int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
						winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
						BDDWrapper.free(bdd, initialWinning);
						BDDWrapper.free(bdd, winningSynchSkeletons);
					}
					BDDWrapper.free(bdd, unsafePre);
					
					
				}
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	/**
	 * an iterative algorithm for computing the set of winning states iteratively using partition transition relation
	 * @param set
	 * @param init
	 * @return
	 */
	public int winningRegion2(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
//		int reachable = reachable(init);
//		
//		winning = BDDWrapper.andTo(bdd, winning, reachable);
//		
//		BDDWrapper.free(bdd, reachable);
		
		do{
			fixedPoint = true;
			//for each process 
			for(int i=0; i<processes.length; i++){
				//for each state of the process
				int trans_i = partitionTransitionRelation.get(i);
				int post = BDDWrapper.EpostImage(bdd, winning, getVariablesAndActionsCube(), trans_i, getVprimetoV());
				int notWinning = BDDWrapper.not(bdd, winning);
				int unsafePost = BDDWrapper.and(bdd, post, notWinning);
				BDDWrapper.free(bdd, notWinning);
				BDDWrapper.free(bdd, post);
				int unsafePre = BDDWrapper.EpreImage(bdd, unsafePost, getPrimedVariablesAndActionsCube(), trans_i, getVtoVprime());
				BDDWrapper.free(bdd, unsafePost);
				int notUnsafePre = BDDWrapper.not(bdd, unsafePre);
				int tmp = BDDWrapper.and(bdd, winning, notUnsafePre);
				BDDWrapper.free(bdd, notUnsafePre);
				if(tmp != winning){
					fixedPoint = false;
					BDDWrapper.free(bdd, winning);
					winning = tmp;
					
					//prune the set of synchronization skeletons based on current winning set
					int initialWinning = bdd.ref(bdd.and(winning, init));
					int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
					winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
					BDDWrapper.free(bdd, initialWinning);
					BDDWrapper.free(bdd, winningSynchSkeletons);
				}
				BDDWrapper.free(bdd, unsafePre);
			}
			
//			int initialWinning = BDDWrapper.and(bdd, winning, init);
//			reachable = reachable(initialWinning);
//			winning = BDDWrapper.andTo(bdd, winning, reachable);
//			BDDWrapper.free(bdd, reachable);
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	public int winningRegion3(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		int processLength = processStates.get(0).size();
		
		do{
			fixedPoint = true;
			
			for(int j=0; j<processLength; j++){
				//for each process 
				for(int i=0; i<processes.length; i++){
					//for each state of the process
					ArrayList<Integer> p_i_states = processStates.get(i);
					int trans_i = partitionTransitionRelation.get(i);
				
					//compute the set of possible winning states for q_j of process p_i
					int q_j = p_i_states.get(j);
					int safe_q_j = BDDWrapper.and(bdd, winning, q_j);
					int post = BDDWrapper.EpostImage(bdd, safe_q_j, getVariablesAndActionsCube(), trans_i, getVprimetoV());
					BDDWrapper.free(bdd, safe_q_j);
					int unsafePost = BDDWrapper.and(bdd, post, bdd.not(winning));
					BDDWrapper.free(bdd, post);
					int unsafePre = BDDWrapper.EpreImage(bdd, unsafePost, getPrimedVariablesAndActionsCube(), trans_i, getVtoVprime());
					BDDWrapper.free(bdd, unsafePost);
					int tmp = BDDWrapper.and(bdd, winning, bdd.not(unsafePre));
					if(tmp != winning){
						fixedPoint = false;
						BDDWrapper.free(bdd, winning);
						winning = tmp;
						
						//prune the set of synchronization skeletons based on current winning set
						int initialWinning = bdd.ref(bdd.and(winning, init));
						int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
						winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
						BDDWrapper.free(bdd, initialWinning);
						BDDWrapper.free(bdd, winningSynchSkeletons);
					}
					BDDWrapper.free(bdd, unsafePre);
					
					
				}
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	public int winningRegion4(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		do{
			fixedPoint = true;
			//for each process 
			for(int i=0; i<processes.length; i++){
				//for each state of the process
				int trans_i = partitionTransitionRelation.get(i);
				int notWinning = bdd.ref(bdd.not(winning));
				int unsafePre = BDDWrapper.EpreImage(bdd, notWinning, getPrimedVariablesAndActionsCube(), trans_i, getVtoVprime());
				BDDWrapper.free(bdd, notWinning);
				int notUnsafe = bdd.ref(bdd.not(unsafePre));
				int tmp = BDDWrapper.and(bdd, winning, notUnsafe);
				BDDWrapper.free(bdd, unsafePre);
				BDDWrapper.free(bdd, notUnsafe);
				if(tmp != winning){
					fixedPoint = false;
					BDDWrapper.free(bdd, winning);
					winning = tmp;
					
					//prune the set of synchronization skeletons based on current winning set
					int initialWinning = bdd.ref(bdd.and(winning, init));
					int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
					winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
					BDDWrapper.free(bdd, initialWinning);
					BDDWrapper.free(bdd, winningSynchSkeletons);
				}
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	public int winningRegion5(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		do{
			fixedPoint = true;
			//for each process 
			for(int i=0; i<processes.length; i++){
				ArrayList<Integer> p_i_trans = partitionStatesTransitionRelation.get(i);
				
				for(int j=0; j<p_i_trans.size(); j++){
					int trans_i_j = p_i_trans.get(j);
					int post = BDDWrapper.EpostImage(bdd, winning, getVariablesAndActionsCube(), trans_i_j, getVprimetoV());
					int notWinning = BDDWrapper.not(bdd, winning);
					int unsafePost = BDDWrapper.and(bdd, post, notWinning);
					BDDWrapper.free(bdd, notWinning);
					BDDWrapper.free(bdd, post);
					int unsafePre = BDDWrapper.EpreImage(bdd, unsafePost, getPrimedVariablesAndActionsCube(), trans_i_j, getVtoVprime());
					BDDWrapper.free(bdd, unsafePost);
					int notUnsafePre = BDDWrapper.not(bdd, unsafePre);
					int tmp = BDDWrapper.and(bdd, winning, notUnsafePre);
					BDDWrapper.free(bdd, notUnsafePre);
					BDDWrapper.free(bdd, unsafePre);
					if(tmp != winning){
						fixedPoint = false;
						BDDWrapper.free(bdd, winning);
						winning = tmp;
						
						//prune the set of synchronization skeletons based on current winning set
						int initialWinning = bdd.ref(bdd.and(winning, init));
						int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
						winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
						BDDWrapper.free(bdd, initialWinning);
						BDDWrapper.free(bdd, winningSynchSkeletons);
					}
				}
				
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	public int winningRegion6(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		int processLength = partitionStatesTransitionRelation.get(0).size();
		
		do{
			fixedPoint = true;
			for(int j=0; j<processLength; j++){
				//for each process 
				for(int i=0; i<processes.length; i++){
					ArrayList<Integer> p_i_trans = partitionStatesTransitionRelation.get(i);
				
				
					int trans_i_j = p_i_trans.get(j);
					int post = BDDWrapper.EpostImage(bdd, winning, getVariablesAndActionsCube(), trans_i_j, getVprimetoV());
					int notWinning = BDDWrapper.not(bdd, winning);
					int unsafePost = BDDWrapper.and(bdd, post, notWinning);
					BDDWrapper.free(bdd, notWinning);
					BDDWrapper.free(bdd, post);
					int unsafePre = BDDWrapper.EpreImage(bdd, unsafePost, getPrimedVariablesAndActionsCube(), trans_i_j, getVtoVprime());
					BDDWrapper.free(bdd, unsafePost);
					int notUnsafePre = BDDWrapper.not(bdd, unsafePre);
					int tmp = BDDWrapper.and(bdd, winning, notUnsafePre);
					BDDWrapper.free(bdd, notUnsafePre);
					BDDWrapper.free(bdd, unsafePre);
					if(tmp != winning){
						fixedPoint = false;
						BDDWrapper.free(bdd, winning);
						winning = tmp;
						
						//prune the set of synchronization skeletons based on current winning set
						int initialWinning = bdd.ref(bdd.and(winning, init));
						int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
						winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
						BDDWrapper.free(bdd, initialWinning);
						BDDWrapper.free(bdd, winningSynchSkeletons);
					}
				}
				
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
	
	public int winningRegion7(int set, int init){
		int winning = bdd.ref(set);
		
		boolean fixedPoint = true;
		
		int numOfIterations = 0;
		
		Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), getSynchronizationVariables());
		int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
		
		int processLength = partitionStatesTransitionRelation.get(0).size();
		
		do{
			fixedPoint = true;
			for(int j=0; j<processLength; j++){
				//for each process 
				for(int i=0; i<processes.length; i++){
					ArrayList<Integer> p_i_trans = partitionStatesTransitionRelation.get(i);
				
				
					int trans_i_j = p_i_trans.get(j);
					int notWinning = BDDWrapper.not(bdd, winning);
					int unsafePre = BDDWrapper.EpreImage(bdd, notWinning, getPrimedVariablesAndActionsCube(), trans_i_j, getVtoVprime());
					BDDWrapper.free(bdd, notWinning);
					int notUnsafePre = BDDWrapper.not(bdd, unsafePre);
					int tmp = BDDWrapper.and(bdd, winning, notUnsafePre);
					BDDWrapper.free(bdd, notUnsafePre);
					BDDWrapper.free(bdd, unsafePre);
					if(tmp != winning){
						fixedPoint = false;
						BDDWrapper.free(bdd, winning);
						winning = tmp;
						
						//prune the set of synchronization skeletons based on current winning set
						int initialWinning = bdd.ref(bdd.and(winning, init));
						int winningSynchSkeletons = bdd.ref(bdd.exists(initialWinning, synchCube));
						winning = BDDWrapper.andTo(bdd, winning, winningSynchSkeletons);
						BDDWrapper.free(bdd, initialWinning);
						BDDWrapper.free(bdd, winningSynchSkeletons);
					}
				}
				
			}
			
			numOfIterations++;
			
		}while(!fixedPoint);
		
		System.out.println("number of iterations to compute greatest fixed point "+numOfIterations);
		
		return winning;
	}
}
