package swarm;

import game.BDDWrapper;
import game.GameStructure;
import game.Variable;

import java.util.ArrayList;

import utils.UtilityMethods;
import jdd.bdd.BDD;

public class SymmetricSynchronizationTransitionRelation extends GameStructure {
	
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

	public SymmetricSynchronizationTransitionRelation(BDD argBdd) {
		super(argBdd);
	}
	
	public SymmetricSynchronizationTransitionRelation(BDD argBDD, int argProcessLength, ArrayList<Variable[]> argProcessVariables, Variable[][][] argSynchronizationVariables, Variable[][] argAdvanceVariables, Variable[] argSchedulingVars, int[] argProcesses, int[] argProcessesInit ){
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
		
		allSynchronizationSignals = new Variable[(processLength*numOfProcesses*(numOfProcesses-1))/2];
		int counter = 0;
		for(int i=0; i<synchronizationSignals.length-1;i++){
			for(int j=0; j<synchronizationSignals[i].length; j++){
				for(int k=i+1; k<synchronizationSignals[i][j].length; k++){
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
				
				int not_a_i_j = bdd.not(a_i_j.getBDDVar());
				int not_a_j_i = bdd.not(a_j_i.getBDDVar());
				phi[i][j] = BDDWrapper.and(bdd, not_a_i_j, not_a_j_i);
				BDDWrapper.free(bdd, not_a_i_j);
				BDDWrapper.free(bdd, not_a_j_i);
				
				int phi_i = bdd.ref(bdd.getZero());
				for(int l=0; l<processLength; l++){
					//q^i_l
					int q_i_l  = processStates.get(i).get(2*l);
					
					//q^j_l
					int q_j_l = processStates.get(j).get(2*l);
					
					//s^{i,j}_l
					Variable s_i_j_l = synchronizationSignals[i][l][j];
					
					int psi_l = BDDWrapper.and(bdd, q_i_l, s_i_j_l.getBDDVar());
					psi_l = BDDWrapper.andTo(bdd, psi_l, q_j_l);
					
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
//				Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
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
							if(i<j){
								synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
							}else{
								synchronizationVariables[i][l][j] = synchronizationVariables[j][l][i];
							}
						}
							
					}
				}
			}	


			
			
			
//			for(int l=0; l<processLength; l++){
//				for(int i=0; i<numOfProcesses; i++){
//					for(int j=0; j<numOfProcesses; j++){
//						if(i==j){ 
//							synchronizationVariables[i][l][j] = null;
//						}else{
//							synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
//						}
//							
//					}
//				}
//			}
			
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
				
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
			
			long t_gameConstruction = UtilityMethods.timeStamp();
			//form the synchronization game structure
			SynchronizationGameStructure sgs = new SynchronizationGameStructure(bdd, processLength, processVariables,  synchronizationVariables, 
						advanceVariables, schedulingVars, processes, processesInit);
			
			UtilityMethods.duration(t_gameConstruction, "game constructed in ");
				
			sgs.printGameVars();
//			UtilityMethods.getUserInput();
			
			
			//TODO: specify the safety objective
			//not(q3 & u1) & not(q3 & u7) & not(q5 & u1) & not(q5 & u7)
//			int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
//			int q5 = BDDWrapper.assign(bdd, 5, processVariables.get(0));
//			int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
//			int u7 = BDDWrapper.assign(bdd, 7, processVariables.get(1));
//			int notq3u1 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u1));
//			int notq3u7 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u7));
//			int notq5u1 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u1));
//			int notq5u7 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u7));
//				
//			int safetySet = BDDWrapper.and(bdd, notq3u1, notq3u7);
//			safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u1);
//			safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u7);
			
			int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
			int q7 = BDDWrapper.assign(bdd, 7, processVariables.get(0));
			int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
			int u5 = BDDWrapper.assign(bdd, 5, processVariables.get(1));
//			int v1 = BDDWrapper.assign(bdd, 1, processVariables.get(2));
//			int v5 = BDDWrapper.assign(bdd, 5, processVariables.get(2));
			int q3Orq7 = BDDWrapper.or(bdd, q3, q7);
			int u1Oru5 = BDDWrapper.or(bdd, u1, u5);
//			int v1Orv5 = BDDWrapper.or(bdd, v1, v5);
			
			int notSafe = BDDWrapper.and(bdd, q3Orq7, u1Oru5);
//			notSafe = BDDWrapper.andTo(bdd, notSafe, v1Orv5);
				
			int safetySet = bdd.ref(bdd.not(notSafe));
			
			//form the game
				
			//compute the solution
			long t0 = UtilityMethods.timeStamp();
//			int winning = sgs.greatestFixedPoint(safetySet, init);
			int winning = sgs.winningRegion2(safetySet, init);
			UtilityMethods.duration(t0, "the greatest fixed point was computed in");
			
			int initialWinning = BDDWrapper.and(bdd, winning, init);
				
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
				
			Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
			int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
			int winningSynchSkeletons = BDDWrapper.exists(bdd, initialWinning, synchCube);
				
//			UtilityMethods.debugBDDMethods(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
			
//			//test the solution 
//			//choose one synchronization skeleton 
//			int chosen = BDDWrapper.oneSatisfyingAssignment(bdd, winningSynchSkeletons, sgs.allSynchronizationSignals);
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen synch skeleton is ", chosen);
//			
//			sgs.restrictSynchronizationSkeleton(chosen);
//			
//			int currentState = BDDWrapper.and(bdd, init, chosen);
//			for(int i=0; i<10; i++){
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current state is ", currentState);
//				int nextStates = sgs.symbolicGameOneStepExecution(currentState);
//				currentState = BDDWrapper.oneRandomMinterm(bdd, nextStates, sgs.variables);
//				
//			}
			
			
		}
		
		//Synthesis with partial synchronization
		//Generalizing to arbitrary length processes
		private static void test4(){
			BDD bdd = new BDD(10000, 1000);
				
			int numOfProcesses = 3;
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
//				Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(2*processLength-1), "P"+i);
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
							if(i<j){
								synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
							}else{
								synchronizationVariables[i][l][j] = synchronizationVariables[j][l][i];
							}
						}
							
					}
				}
			}	


			
			
			
//			for(int l=0; l<processLength; l++){
//				for(int i=0; i<numOfProcesses; i++){
//					for(int j=0; j<numOfProcesses; j++){
//						if(i==j){ 
//							synchronizationVariables[i][l][j] = null;
//						}else{
//							synchronizationVariables[i][l][j] = Variable.createVariableAndPrimedVariable(bdd, "S_"+i+"_"+j+"_"+l);
//						}
//							
//					}
//				}
//			}
			
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
				
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "init is ", init);
			
			long t_gameConstruction = UtilityMethods.timeStamp();
			//form the synchronization game structure
			SymmetricSynchronizationTransitionRelation sgs = new SymmetricSynchronizationTransitionRelation(bdd, processLength, processVariables,  synchronizationVariables, 
						advanceVariables, schedulingVars, processes, processesInit);
			
			UtilityMethods.duration(t_gameConstruction, "game constructed in ");
				
			sgs.printGameVars();
//			UtilityMethods.getUserInput();
			
			
			//TODO: specify the safety objective
			//not(q3 & u1) & not(q3 & u7) & not(q5 & u1) & not(q5 & u7)
//			int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
//			int q5 = BDDWrapper.assign(bdd, 5, processVariables.get(0));
//			int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
//			int u7 = BDDWrapper.assign(bdd, 7, processVariables.get(1));
//			int notq3u1 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u1));
//			int notq3u7 = BDDWrapper.or(bdd, bdd.not(q3), bdd.not(u7));
//			int notq5u1 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u1));
//			int notq5u7 = BDDWrapper.or(bdd, bdd.not(q5), bdd.not(u7));
//				
//			int safetySet = BDDWrapper.and(bdd, notq3u1, notq3u7);
//			safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u1);
//			safetySet = BDDWrapper.andTo(bdd, safetySet, notq5u7);
			
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
				
			int safetySet = bdd.ref(bdd.not(notSafe));
			
			//form the game
				
			//compute the solution
			long t0 = UtilityMethods.timeStamp();
			int winning = sgs.greatestFixedPoint(safetySet, init);
			UtilityMethods.duration(t0, "the greatest fixed point was computed in");
			
			int initialWinning = BDDWrapper.and(bdd, winning, init);
				
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "winning set is",winning);
				
			Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), sgs.getSynchronizationVariables());
			int synchCube = BDDWrapper.createCube(bdd, allVarsButSynchronizationSignals);
			int winningSynchSkeletons = BDDWrapper.exists(bdd, initialWinning, synchCube);
				
//			UtilityMethods.debugBDDMethods(bdd, "winning synchronization skeletons ", winningSynchSkeletons);
			
//			//test the solution 
//			//choose one synchronization skeleton 
//			int chosen = BDDWrapper.oneSatisfyingAssignment(bdd, winningSynchSkeletons, sgs.allSynchronizationSignals);
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "chosen synch skeleton is ", chosen);
//			
//			sgs.restrictSynchronizationSkeleton(chosen);
//			
//			int currentState = BDDWrapper.and(bdd, init, chosen);
//			for(int i=0; i<10; i++){
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current state is ", currentState);
//				int nextStates = sgs.symbolicGameOneStepExecution(currentState);
//				currentState = BDDWrapper.oneRandomMinterm(bdd, nextStates, sgs.variables);
//				
//			}
			
			
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
		
		public static void main(String[] args) {
//			test1();
			
//			test2_unrealizableObjective();
			
//			test3();
			
			test4();
			
//			test5();
			
//			test_harmony();
		}

}
