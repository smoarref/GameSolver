package coordination;

import game.BDDWrapper;
import game.GameStructure;
import game.Player;
import game.Variable;

import java.util.ArrayList;

import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

//TODO: clean up the game and free BDDs
public class OptimizedCompositionalCoordinationGameStructure extends game.GameStructure{
	int numOfRobots;
	Variable[] taskVars; 
	ArrayList<Variable[]> densityVars;
	Variable[] successSignals;
	
	SimpleCommandVector[] commands;
	
	ArrayList<Integer> command_systemTransitions;
	ArrayList<Variable[]> affectedVariablesDuringSystemTransitions;
	ArrayList<Variable[]> unaffectedVariablesDuringSystemTransitions;
	ArrayList<Integer> sameUnaffectedVariablesOverSystemTransitions;
	ArrayList<Integer[]> command_environmentTransitions;
	ArrayList<ArrayList<Variable[]>> affectedVariablesDuringEnvironmentTransitions;
	ArrayList<ArrayList<Variable[]>> unaffectedVariablesDuringEnvironmentTransitions;
	
	ArrayList<Integer> affectedCubeSystemTransitions;
	ArrayList<Integer> affectedPrimeCubeSystemTransitions;
	ArrayList<Integer> unaffectedCubeSystemTransitions;
	ArrayList<Integer> unaffectedPrimeCubeSystemTransitions;
	ArrayList<Permutation> affectedPermutationSystemTransitions;
	ArrayList<Permutation> affectedPrimePermutationSystemTransitions;
	
	ArrayList<ArrayList<Integer>> affectedCubeEnvironmentTransitions;
	ArrayList<ArrayList<Integer>> affectedPrimeCubeEnvironmentTransitions;
	ArrayList<ArrayList<Integer>> unaffectedCubeEnvironmentTransitions;
	ArrayList<ArrayList<Integer>> unaffectedPrimeCubeEnvironmentTransitions;
	ArrayList<ArrayList<Permutation>> affectedPermutationEnvironmentTransitions;
	ArrayList<ArrayList<Permutation>> affectedPrimePermutationEnvironmentTransitions;
	
	int allSuccessSignalsFalse;
	int sameSuccessSignals;
	int successSignalsCube;
	
	int allNextSuccessSignalsOFF;
	int[] allButOneSuccessSignalsOFF;
	
	//Assumes at any state stutter is available
//	ArrayList<ArrayList<Integer>> commandAvailableEnvironmentTransitions;
	ArrayList<ArrayList<Integer>> commandNotAvailableEnvironmentTransitions;
	
	
	private int systemStutterFomula=-1; 
	
	public OptimizedCompositionalCoordinationGameStructure(BDD argBdd) {
		super(argBdd);
	}
	
	public OptimizedCompositionalCoordinationGameStructure(BDD argBDD, int argNumOfRobots, SimpleCommandVector[] argCommands, ArrayList<Variable[]> argDensityVars, Variable[] argTaskVars, Variable[] argSuccessSignals){
		super(argBDD);
		
		numOfRobots = argNumOfRobots;
		
		commands = argCommands;
		
		taskVars = argTaskVars;
		densityVars = argDensityVars;
		successSignals = argSuccessSignals;
		
		variables = Variable.unionVariables(densityVars);
		variables = Variable.unionVariables(variables, taskVars);
		variables = Variable.unionVariables(variables, successSignals); 
		
		primedVariables = Variable.getPrimedCopy(variables);
		
//		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
//		actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
//		envActionVars = actionVars;
//		sysActionVars = actionVars;
//		actionVars=Variable.unionVariables(envActionVars, sysActionVars);
//		actionsCube=-1;
		
		actionsCube = bdd.ref(bdd.getOne());
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables),Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
		//We set the T_env and T_sys to -1 to indicate that they are not computed yet and are stored as partitioned transition relation
		T_env= -1;
		T_sys= -1;
		
		command_systemTransitions = new ArrayList<Integer>();
		command_environmentTransitions = new ArrayList<Integer[]>();
		
		affectedVariablesByCommands();
		
		allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
		sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
		successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
		
		allFalseNextSuccessSignals();
		prepareAllButOneSuccessSignalsOFF();
		
		computeCommandsTransitionFormulas();
		
		computeAffectedAndUnaffectedCubes();
		
		
		
		
		computeCommandAvailabilityForEnvironmentTransitions();

		computeSameUnaffectedVariablesOverSystemTransitions();
	}
	
	private void prepareAllButOneSuccessSignalsOFF(){
		allButOneSuccessSignalsOFF = new int[successSignals.length];
		for(int i=0; i<successSignals.length; i++){
			allButOneSuccessSignalsOFF[i] = setNextSuccessSignals(bdd, successSignals, successSignals[i]);
		}
	}
	
	private void computeSameUnaffectedVariablesOverSystemTransitions(){
		sameUnaffectedVariablesOverSystemTransitions = new ArrayList<Integer>();
		for(int i=0;i<commands.length;i++){
			Variable[] unaffected = unaffectedVariablesDuringSystemTransitions.get(i);
			int same = BDDWrapper.same(bdd, unaffected);
			sameUnaffectedVariablesOverSystemTransitions.add(same);
		}
	}
	
	public int getSameUnaffectedVariablesOverSystemTransitions(int i){
		if(sameUnaffectedVariablesOverSystemTransitions == null){
			computeSameUnaffectedVariablesOverSystemTransitions();
		}
		return sameUnaffectedVariablesOverSystemTransitions.get(i);
	}
	
	private void computeCommandAvailabilityForEnvironmentTransitions(){
		int cube = getPrimedVariablesAndActionsCube();
//		commandAvailableEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		commandNotAvailableEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<commands.length; i++){
//			ArrayList<Integer> commandAvailableTrans = new ArrayList<Integer>();
			ArrayList<Integer> commandNotAvailableTrans = new ArrayList<Integer>();
			Integer[] envTransitions = command_environmentTransitions.get(i);
			for(int j=0; j<envTransitions.length; j++){
				int trans = envTransitions[j];
				//stutter
				if(j==0){
					continue;
				}
				int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
				int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
				BDDWrapper.free(bdd, commandAvailable);
//				commandAvailableTrans.add(commandAvailable);
				commandNotAvailableTrans.add(commandNotAvailable);
			}
//			commandAvailableEnvironmentTransitions.add(commandAvailableTrans);
			commandNotAvailableEnvironmentTransitions.add(commandNotAvailableTrans);
		}

	}
	
	private void computeAffectedAndUnaffectedCubes(){
		//system transitions
		affectedCubeSystemTransitions = new ArrayList<Integer>();
		affectedPrimeCubeSystemTransitions = new ArrayList<Integer>();
		unaffectedCubeSystemTransitions = new ArrayList<Integer>();
		unaffectedPrimeCubeSystemTransitions = new ArrayList<Integer>();
		affectedPermutationSystemTransitions = new ArrayList<Permutation>();
		affectedPrimePermutationSystemTransitions = new ArrayList<Permutation>();
		for(int i=0; i<command_systemTransitions.size(); i++){
			Variable[] affectedVars = affectedVariablesDuringSystemTransitions.get(i);
			Variable[] unaffectedVars = unaffectedVariablesDuringSystemTransitions.get(i);
			int unaffectedCube = BDDWrapper.createCube(bdd, unaffectedVars);
			int unaffectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(unaffectedVars));
			int affectedCube = BDDWrapper.createCube(bdd, affectedVars);
			int affectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(affectedVars));
			Permutation affectedPermutation = BDDWrapper.createPermutations(bdd, affectedVars, Variable.getPrimedCopy(affectedVars));
			Permutation affectedPrimePermutation = BDDWrapper.createPermutations(bdd, Variable.getPrimedCopy(affectedVars), affectedVars);
			
			affectedCubeSystemTransitions.add(affectedCube);
			affectedPrimeCubeSystemTransitions.add(affectedPrimeCube);
			unaffectedCubeSystemTransitions.add(unaffectedCube);
			unaffectedPrimeCubeSystemTransitions.add(unaffectedPrimeCube);
			affectedPermutationSystemTransitions.add(affectedPermutation);
			affectedPrimePermutationSystemTransitions.add(affectedPrimePermutation);
		}
		
		//environment transitions
		affectedCubeEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		affectedPrimeCubeEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		unaffectedCubeEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		unaffectedPrimeCubeEnvironmentTransitions = new ArrayList<ArrayList<Integer>>();
		affectedPermutationEnvironmentTransitions = new ArrayList<ArrayList<Permutation>>();
		affectedPrimePermutationEnvironmentTransitions = new ArrayList<ArrayList<Permutation>>();
		
		for(int i=0; i<commands.length; i++){
			Integer[] envTransitions = command_environmentTransitions.get(i);
			ArrayList<Variable[]> envAffectedVars = affectedVariablesDuringEnvironmentTransitions.get(i);
			ArrayList<Variable[]> envUnaffectedVars = unaffectedVariablesDuringEnvironmentTransitions.get(i);
			
			ArrayList<Integer> currentAffectedCube = new  ArrayList<Integer>();
			ArrayList<Integer> currentAffectedPrimeCube = new ArrayList<Integer>();
			ArrayList<Integer> currentUnaffectedCube = new ArrayList<Integer>();
			ArrayList<Integer> currentUnaffectedPrimeCube = new ArrayList<Integer>();
			ArrayList<Permutation> currentAffectedPermuation = new ArrayList<Permutation>();
			ArrayList<Permutation> currentAffectedPrimePermuation = new ArrayList<Permutation>();
			
			for(int j=1; j<envTransitions.length; j++){
				Variable[] affectedVars =  envAffectedVars.get(j-1);
				Variable[] unaffectedVars = envUnaffectedVars.get(j-1);
				int unaffectedCube = BDDWrapper.createCube(bdd, unaffectedVars);
				int unaffectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(unaffectedVars));
				int affectedCube = BDDWrapper.createCube(bdd, affectedVars);
				int affectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(affectedVars));
				Permutation affectedPermutation = BDDWrapper.createPermutations(bdd, affectedVars, Variable.getPrimedCopy(affectedVars));
				Permutation affectedPrimePermutation = BDDWrapper.createPermutations(bdd, Variable.getPrimedCopy(affectedVars), affectedVars);
				
				currentAffectedCube.add(affectedCube);
				currentAffectedPrimeCube.add(affectedPrimeCube);
				currentUnaffectedCube.add(unaffectedCube);
				currentUnaffectedPrimeCube.add(unaffectedPrimeCube);
				currentAffectedPermuation.add(affectedPermutation);
				currentAffectedPrimePermuation.add(affectedPrimePermutation);
			}
			
			affectedCubeEnvironmentTransitions.add(currentAffectedCube);
			affectedPrimeCubeEnvironmentTransitions.add(currentAffectedPrimeCube);
			unaffectedCubeEnvironmentTransitions.add(currentUnaffectedCube);
			unaffectedPrimeCubeEnvironmentTransitions.add(currentUnaffectedPrimeCube);
			affectedPermutationEnvironmentTransitions.add(currentAffectedPermuation);
			affectedPrimePermutationEnvironmentTransitions.add(currentAffectedPrimePermuation);
		}
	}
	
	/**
	 * computing the affected variables by commands
	 */
	private void affectedVariablesByCommands(){
		affectedVariablesDuringSystemTransitions = new ArrayList<Variable[]>();
		affectedVariablesDuringEnvironmentTransitions = new ArrayList<ArrayList<Variable[]>>();
		unaffectedVariablesDuringEnvironmentTransitions = new ArrayList<ArrayList<Variable[]>>();
		unaffectedVariablesDuringSystemTransitions = new ArrayList<Variable[]>();
		for(int i=0; i<commands.length; i++){
			affectedVariablesDuringSystemTransitions.add(affectedBySystemTransition(commands[i]));
			affectedVariablesDuringEnvironmentTransitions.add(affectedByEnvironmentTransition(commands[i]));
		}
		//prepare unaffected
		for(int i=0; i<commands.length; i++){
			Variable[] unaffectedSystemVars = Variable.difference(variables, affectedVariablesDuringSystemTransitions.get(i));
			unaffectedVariablesDuringSystemTransitions.add(unaffectedSystemVars);
			ArrayList<Variable[]> affectedEnvVars = affectedVariablesDuringEnvironmentTransitions.get(i);
			Variable[] unaffectedSuccessEnv = Variable.difference(variables, affectedEnvVars.get(0));
			Variable[] unaffectedFailEnv = Variable.difference(variables, affectedEnvVars.get(1));
			ArrayList<Variable[]> unaffectedEnvVars = new ArrayList<Variable[]>();
			unaffectedEnvVars.add(unaffectedSuccessEnv);
			unaffectedEnvVars.add(unaffectedFailEnv);
			unaffectedVariablesDuringEnvironmentTransitions.add(unaffectedEnvVars);
		}
	}
	
	private Variable[] affectedBySystemTransition(SimpleCommandVector command){
		Variable[] results = command.affectedTasks; 
		int[] initVector = command.initDensity;
		results = Variable.unionVariables(results, affectedVars(initVector));
		int[] intermediateVector = command.getIntermediateDensity();
		results = Variable.unionVariables(results, affectedVars(intermediateVector));
		return results; 
	}
	
	/**
	 * Variables affected by success and failure transitions, respectively
	 * @param command
	 * @return
	 */
	private ArrayList<Variable[]> affectedByEnvironmentTransition(SimpleCommandVector command){
		//note that stutter does not affect any variable, so we only need to track success and failure 
		ArrayList<Variable[]> result = new ArrayList<Variable[]>();
		Variable[] successVars = command.getAffectedTasks();
		Variable[] failureVars = command.getAffectedTasks(); 
		
		int[] intermediatedensity = command.getIntermediateDensity();
		int[] successDensity = command.getFinalSuccessDensity();
		int[] failDensity = command.getFinalFailureDensity();
		
		Variable[] affected = affectedVars(intermediatedensity);
		
		Variable[] successDensityVars = Variable.unionVariables(affected, affectedVars(successDensity));
		Variable[] failDensityVars = Variable.unionVariables(affected, affectedVars(failDensity));
		
		successVars = Variable.unionVariables(successVars, successDensityVars);
		failureVars = Variable.unionVariables(failureVars, failDensityVars);
		
		successVars = Variable.unionVariables(successVars, successSignals);
		failureVars = Variable.unionVariables(failureVars, successSignals);
		
		result.add(successVars);
		result.add(failureVars);
		
		return result;
	}
	
	private Variable[] affectedVars(int[] vector){
		Variable[] result = null; 
		for(int i=0; i<vector.length; i++){
			if(vector[i]!=0){
				result = Variable.unionVariables(result, densityVars.get(i));
			}
		}
		return result;
	}
	
	/**
	 * Computing the command formulas
	 */
	private void computeCommandsTransitionFormulas(){
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
					

			int commandFormula_sys = commandFormula_systemTransitions(i);
					
					
			command_systemTransitions.add(commandFormula_sys);
					
			Integer[] commandFormula_env = commandFormula_environmentTransitions(i);
					
					
			command_environmentTransitions.add(commandFormula_env);
					
		}
				
	}
	
	public static int allFalseNextSuccessSignals(BDD bdd, Variable[] successSignals){
		boolean[] allFalse = UtilityMethods.allFalseArray(successSignals.length);
		int falseNextSuccessSignals = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		return falseNextSuccessSignals;
	}
	
	public void allFalseNextSuccessSignals(){
		boolean[] allFalse = UtilityMethods.allFalseArray(successSignals.length);
		allNextSuccessSignalsOFF = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
	}
	
	public static int setNextSuccessSignals(BDD bdd, Variable[] successSignals, Variable commandSuccessSignal){
		int nextSuccessSignals = bdd.ref(bdd.getOne());
		for(Variable successSignal : successSignals){
			Variable successSignalPrime = successSignal.getPrimedCopy();
			if(successSignal == commandSuccessSignal){
				nextSuccessSignals = BDDWrapper.andTo(bdd, nextSuccessSignals, successSignalPrime.getBDDVar());
			}else{
				int negSuccessSignal = bdd.not(successSignalPrime.getBDDVar());
				nextSuccessSignals = BDDWrapper.andTo(bdd, nextSuccessSignals, negSuccessSignal);
			}
		}
		return nextSuccessSignals;
	}
	
	
	private Integer[] commandFormula_environmentTransitions(int index){
//		int commandFormula = bdd.ref(bdd.getOne());
		
		Integer[] commandFormula = new Integer[3];
		
		int stutter = bdd.ref(bdd.getOne());
		int success = bdd.ref(bdd.getOne());
		int fail = bdd.ref(bdd.getOne());
		
//		Variable commandSuccessSignalPrime = commandSuccessSignal.getPrimedCopy();
//		int successSignalOFF = bdd.ref(bdd.not(commandSuccessSignalPrime.getBDDVar()));
//		int successSignalON = bdd.ref(commandSuccessSignalPrime.getBDDVar());
//		
//		stutter = BDDWrapper.andTo(bdd, stutter, successSignalOFF);
//		success = BDDWrapper.andTo(bdd, success, successSignalON);
//		fail = BDDWrapper.andTo(bdd, fail, successSignalOFF);
//		
//		BDDWrapper.free(bdd, successSignalON);
//		BDDWrapper.free(bdd, successSignalOFF);
		
//		int allNextSuccessSignalsOFF = allFalseNextSuccessSignals(bdd, successSignals);
//		int allButOneSuccessSignalsOFF = setNextSuccessSignals(bdd, successSignals, successSignals[index]);
		
		
		stutter = BDDWrapper.andTo(bdd, stutter, allNextSuccessSignalsOFF);
		success = BDDWrapper.andTo(bdd, success, allButOneSuccessSignalsOFF[index]);
		fail = BDDWrapper.andTo(bdd, fail, allNextSuccessSignalsOFF);
		
//		BDDWrapper.free(bdd, allNextSuccessSignalsOFF);
//		BDDWrapper.free(bdd, allButOneSuccessSignalsOFF);
		
		SimpleCommandVector command = commands[index];
		
		//task formula 
		if(command.getAffectedTasks() != null){
			
			int sameTaskState = BDDWrapper.same(bdd, taskVars);
			stutter = bdd.andTo(stutter, sameTaskState);
			BDDWrapper.free(bdd, sameTaskState);
			
			int taskFormula = command.getIntermediateTasksCondition();
			
			Variable[] primedTaskVariables = Variable.getPrimedCopy(taskVars);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(taskVars), Variable.getBDDVars(primedTaskVariables));
			
			int taskSuccess = command.getFinalSuccessTaskCondition();
			int taskSuccessPrime = bdd.ref(bdd.replace(taskSuccess, taskPerm));
			
			int taskFail = command.getFinalFailureTaskCondition();
			int taskFailPrime = bdd.ref(bdd.replace(taskFail, taskPerm));
			
			success = bdd.andTo(success, taskFormula);
			success = bdd.andTo(success, taskSuccessPrime);
			
			fail = bdd.andTo(fail, taskFormula);
			fail = bdd.andTo(fail, taskFailPrime);
			
			BDDWrapper.free(bdd, taskSuccessPrime);
			BDDWrapper.free(bdd, taskFailPrime);
		
			//unaffected tasks stay unchanged
			Variable[] unaffectedTasks  = Variable.difference(taskVars, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			success = bdd.andTo(success, sameUnaffectedTasks);
			fail = bdd.andTo(fail, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
//			commandFormula = bdd.andTo(commandFormula, taskFormula);
//			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, taskVars);
			
			stutter = bdd.andTo(stutter, sameTaskState);
			success = bdd.andTo(success, sameTaskState);
			fail = bdd.andTo(fail, sameTaskState);
			
			BDDWrapper.free(bdd, sameTaskState);
		}
		
		//density part 
		int[] densityVector = command.getIntermediateDensity();
		int[] successDensityVector = command.getFinalSuccessDensity();
		int[] failureDensityVector = command.getFinalFailureDensity();
		
				
		//create density formula
		int densityFormula = bdd.ref(bdd.getOne());
		
		Variable[] allDensityVars = Variable.unionVariables(densityVars);
		int sameDensity = BDDWrapper.same(bdd, allDensityVars);
		stutter = BDDWrapper.andTo(bdd, stutter, sameDensity);
		BDDWrapper.free(bdd, sameDensity);
		
//		UtilityMethods.debugBDDMethods(bdd, "command env stutter",stutter);
		
						
		//x_0 >= d_0 & ... & x_k >= d_k
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfRobots);
		densityFormula = BDDWrapper.and(bdd, densityFormula, lowerBound);
		BDDWrapper.free(bdd, lowerBound);
				
		//x'_i = x_i +d'_i - d_i
		int nextSuccessDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, successDensityVector);
		int successDensityFormula = BDDWrapper.and(bdd, densityFormula, nextSuccessDensityFormula);
		BDDWrapper.free(bdd, nextSuccessDensityFormula);
		
		success = BDDWrapper.andTo(bdd, success, successDensityFormula);
		BDDWrapper.free(bdd, successDensityFormula);
		
		//x'_i = x_i +d'_i - d_i
		int nextFailureDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, failureDensityVector);
		int failureDensityFormula = BDDWrapper.andTo(bdd, densityFormula, nextFailureDensityFormula);
		BDDWrapper.free(bdd, nextFailureDensityFormula);
		
		fail = BDDWrapper.andTo(bdd, fail, failureDensityFormula);
		BDDWrapper.free(bdd, failureDensityFormula);
		
		BDDWrapper.free(bdd, densityFormula);
		
		//TODO: maybe the above code can be improved, but for now we existentially quantify the success and fail formulas
//		stutter = BDDWrapper.andTo(bdd, stutter, commandAction);
		commandFormula[0] = stutter; 
		
//		success = BDDWrapper.andTo(bdd, success, commandAction);
		int tmp = success;
		ArrayList<Variable[]> unaffectedEnvVars = unaffectedVariablesDuringEnvironmentTransitions.get(index);
//		int cube = BDDWrapper.createCube(bdd, unaffectedEnvVars.get(0));
		Variable[] unaffectedEnvVars_Success = unaffectedEnvVars.get(0);
		Variable[] unaffectedEnvVars_SuccessPrime = Variable.getPrimedCopy(unaffectedEnvVars_Success);
		Variable[] unaffectedSuccess = Variable.unionVariables(unaffectedEnvVars_Success, unaffectedEnvVars_SuccessPrime);
		int cube = BDDWrapper.createCube(bdd, unaffectedSuccess);
		commandFormula[1] = BDDWrapper.exists(bdd, success, cube);
		BDDWrapper.free(bdd, tmp);
		BDDWrapper.free(bdd,cube);
		
//		fail = BDDWrapper.andTo(bdd, fail, commandAction);
		int tmp2 = fail;
//		int cube2 = BDDWrapper.createCube(bdd, unaffectedEnvVars.get(1));
		Variable[] unaffectedFailVars = unaffectedEnvVars.get(1);
		Variable[] unaffectedFailPrime = Variable.getPrimedCopy(unaffectedFailVars);
		Variable[] unaffectedFail = Variable.unionVariables(unaffectedFailVars, unaffectedFailPrime);
		int cube2 = BDDWrapper.createCube(bdd, unaffectedFail);
		commandFormula[2] = BDDWrapper.exists(bdd, fail, cube2);
		BDDWrapper.free(bdd, tmp2);
		BDDWrapper.free(bdd,cube2);
		
		return commandFormula;
		
		
	}
	
	private int commandFormula_systemTransitions(int index){
		
		int commandFormula = bdd.ref(bdd.getOne());
		
//		Variable commandSuccessSignalPrime = commandSuccessSignal.getPrimedCopy();
//		int successSignal = bdd.ref(bdd.not(commandSuccessSignalPrime.getBDDVar()));
		
//		int sameSuccessSignal = BDDWrapper.same(bdd, commandSuccessSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
//		int sameSuccessSignal = BDDWrapper.same(bdd, successSignals);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignals);
		
//		int falseNextSuccessSignal = allFalseNextSuccessSignals(bdd, successSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, falseNextSuccessSignal);
		
		SimpleCommandVector command = commands[index];
//		Variable[] affectedVariables = affectedVariablesDuringSystemTransitions.get(index);
		Variable[] unaffectedVariables = unaffectedVariablesDuringSystemTransitions.get(index);
		Variable[] unaffectedVariablesPrime = Variable.getPrimedCopy(unaffectedVariables);
		Variable[] unaffected = Variable.unionVariables(unaffectedVariables, unaffectedVariablesPrime);
		int unaffectedCube = BDDWrapper.createCube(bdd, unaffected);
		
		//task formula 
		if(command.getAffectedTasks() != null){
			int taskFormula = bdd.ref(command.getInitTasksCondition());
			
			int taskIntermediate = command.getIntermediateTasksCondition();
			Variable[] primedTaskVariables = Variable.getPrimedCopy(taskVars);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(taskVars), Variable.getBDDVars(primedTaskVariables));
			int taskIntermediatePrime = bdd.ref(bdd.replace(taskIntermediate, taskPerm));
			
			taskFormula = bdd.andTo(taskFormula, taskIntermediatePrime);
			BDDWrapper.free(bdd, taskIntermediatePrime);
		
			//unaffected tasks stay unchanged
			Variable[] unaffectedTasks  = Variable.difference(taskVars, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			taskFormula = bdd.andTo(taskFormula, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
			commandFormula = bdd.andTo(commandFormula, taskFormula);
			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, taskVars);
			commandFormula = bdd.andTo(commandFormula, sameTaskState);
			BDDWrapper.free(bdd, sameTaskState);
		}
		
		//density part 
		int numOfRobotsRequiredForCommands = 0;
		int[] densityVector = command.getInitDensity();
		int[] nextDensityVector = command.getIntermediateDensity();
		
		
		//sum the number of robots required to perform command c
		for(int i=0;i<densityVector.length; i++){
			numOfRobotsRequiredForCommands += densityVector[i];
		}
		
		//if number of robots required is more than the existing agents, then commands set is inconsistent
		if(numOfRobotsRequiredForCommands>numOfRobots){
			commandFormula = bdd.ref(bdd.getZero());
			return commandFormula;
		}
		
		//create density formula
		int densityFormula = bdd.ref(bdd.getOne());
				
		//x_0 >= d_0 & ... & x_k >= d_k
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfRobots);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, lowerBound);
		BDDWrapper.free(bdd, lowerBound);
		
		//x'_i = x_i +d'_i - d_i
		int nextDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, nextDensityVector);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, nextDensityFormula);
		BDDWrapper.free(bdd, nextDensityFormula);
				
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, densityFormula);
		
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
		//TODO: maybe we can optimize the code above, for now we simply existensially quantify the unaffected vars
//		Variable[] unaffectedVars = Variable.difference(variables, affectedVariables);
		int tmp = commandFormula;
//		int cube = BDDWrapper.createCube(bdd, unaffectedVariables);
		
		commandFormula = BDDWrapper.exists(bdd, commandFormula, unaffectedCube);
		BDDWrapper.free(bdd, tmp);
//		BDDWrapper.free(bdd,cube);
		BDDWrapper.free(bdd,unaffectedCube);
		
		return commandFormula;
	}
	
	public static int densityLowerBoundConstraints(BDD bdd, ArrayList<Variable[]> densityVars, int[] lowerBounds, int upperBound){
		int result= bdd.ref(bdd.getOne());
		for(int i=0; i<densityVars.size();i++){
			int x_i = densityLowerBoundCondition(bdd, densityVars.get(i), lowerBounds[i], upperBound);
			result = BDDWrapper.andTo(bdd, result, x_i);
			BDDWrapper.free(bdd, x_i);
		}
		return result;
	}
	
	/**
	 * create the formaula x_i >= d_i & x_i <= upperBound by enumerating
	 * TODO: make it symbolic by implementing the binary arithmatic
	 * @param bdd
	 * @param vars
	 * @param lowerBound
	 * @return
	 */
	public static int densityLowerBoundCondition(BDD bdd, Variable[] variable, int lowerBound, int upperBound){
		int result = bdd.ref(bdd.getZero());
		for(int i=lowerBound; i<=upperBound; i++){
			int value = BDDWrapper.assign(bdd, i, variable);
			result= BDDWrapper.orTo(bdd, result, value);
			BDDWrapper.free(bdd, value);
		}
		return result;
	}
	
	public static int nextDensityFormula(BDD bdd, ArrayList<Variable[]> densityVars, int[] currentDensity, int[] nextDensity){
		int result=bdd.ref(bdd.getOne());
		for(int i=0; i<densityVars.size(); i++){
			int densityFormula_i = nextDensityFormula(bdd, densityVars.get(i), currentDensity[i], nextDensity[i]);
			result=BDDWrapper.andTo(bdd, result, densityFormula_i);
			BDDWrapper.free(bdd, densityFormula_i);
		}
		return result;
	}
	
	//x'_i = x_i +d'_i - d_i
	public static int nextDensityFormula(BDD bdd, Variable[] var, int currentDensity, int nextDensity){
		int result;
		Variable[] nextVars = Variable.getPrimedCopy(var);
		int diff = nextDensity - currentDensity;
		if(diff>=0){
			result = UtilityTransitionRelations.add(bdd, var, diff, nextVars);
		}else{
			result = UtilityTransitionRelations.subtract(bdd, var, Math.abs(diff), nextVars);
		}
		return result;
	}

	public ArrayList<Integer> getSystemCommandTransitions(){
		return command_systemTransitions;
	}
	
	public SimpleCommandVector[] getCommands(){
		return commands;
	}
	
	public int getSystemStutterFormula(){
		if(systemStutterFomula==-1){
			systemStutterFomula = createStutterFormula();
		}
		return systemStutterFomula;
	}
	
	public int createStutterFormula(){
		int stutter = bdd.ref(bdd.getOne());
		
		for(int i=0; i<densityVars.size(); i++){
			int sameDensityVars = BDDWrapper.same(bdd, densityVars.get(i));
			stutter = BDDWrapper.andTo(bdd, stutter, sameDensityVars);
			BDDWrapper.free(bdd, sameDensityVars);
		}
		
		int sameTasks = BDDWrapper.same(bdd, taskVars);
		stutter = BDDWrapper.andTo(bdd, stutter, sameTasks);
		BDDWrapper.free(bdd, sameTasks);
		
//		boolean[] allFalse = UtilityMethods.allFalseArray(successSignals.length);
//		int falseNextSuccessSignals = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
//		stutter = BDDWrapper.andTo(bdd, stutter, falseNextSuccessSignals);
//		BDDWrapper.free(bdd, falseNextSuccessSignals);
		
		int sameSuccessSignals = BDDWrapper.same(bdd,successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, sameSuccessSignals);
		BDDWrapper.free(bdd, sameSuccessSignals);
		
		return stutter;
	}
	
	public Variable[] getTaskVars(){
		return taskVars;
	}
	
	public ArrayList<Variable[]> getDensitVars(){
		return densityVars;
	}
	
	public Variable[] getSuccessSignals(){
		return successSignals;
	}
	
	
	//Mixture of enumeration and symbolic
//	public int controllablePredecessor(int set){
//		
////		UtilityMethods.debugBDDMethods(bdd, "computing controllable predecessor, target set is ",  set);
////		UtilityMethods.getUserInput();
//		
////		cleanCoordinationGameStatePrintAndWait("computing the controllable predecessor, target set is",  set);
//		
//		if(set == 0){
////			cleanCoordinationGameStatePrintAndWait("cpre of set is", set);
//			return bdd.ref(set);
//		}
//			
//		int cube = getPrimedVariablesAndActionsCube();
//		
//		//stutter command let the system to preserve the current state
//		int controllableEpre = bdd.ref(set);
//		
//		
//		
//		for(int i=0; i<commands.length; i++){
//			int trans = command_systemTransitions.get(i);
//			
////			UtilityMethods.debugBDDMethodsAndWait(bdd, "current system trans is ", trans);
//			
//			//enumerate the set based on affected variables by the current command
////			printGameVars();
////			System.out.println("\n\naffected");
////			Variable.printVariables(affectedVariablesDuringSystemTransitions.get(i));
////			System.out.println("\n\nunaffected");
////			Variable.printVariables(unaffectedVariablesDuringSystemTransitions.get(i));
////			UtilityMethods.debugBDDMethodsAndWait(bdd, "unaffected cube", unaffectedCubeSystemTransitions.get(i));
////			UtilityMethods.debugBDDMethods(bdd, "set", set);
//			
//			int[] decomposedSet = decomposeSetOfStates(set, affectedVariablesDuringSystemTransitions.get(i), unaffectedCubeSystemTransitions.get(i));
//			
//			if(decomposedSet == null) continue;
//			int affectedCube = affectedCubeSystemTransitions.get(i);
//			
//			//compute the EPre for the changing part & combine it with the unchanging part
//			for(int j=0; j<decomposedSet.length; j++){
//				
////				UtilityMethods.debugBDDMethodsAndWait(bdd, "decomposedSet["+j+"]", decomposedSet[j]);
////				cleanCoordinationGameStatePrintAndWait("decomposedSet["+j+"]", decomposedSet[j]);
//				
//				//TODO: try affected cube and affected permutation
////				int epre = BDDWrapper.EpreImage(bdd, decomposedSet[j], cube, trans, getVtoVprime());
//				int epre = BDDWrapper.EpreImage(bdd, decomposedSet[j], affectedCubeSystemTransitions.get(i), trans, affectedPermutationSystemTransitions.get(i));
//				
////				UtilityMethods.debugBDDMethodsAndWait(bdd, "epre is", epre);
////				cleanCoordinationGameStatePrintAndWait("epre is", epre);
//				
//				int intersect = BDDWrapper.and(bdd, decomposedSet[j], set);
//				
//				int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
//				
////				UtilityMethods.debugBDDMethodsAndWait(bdd, "samePart is", samePart);
////				cleanCoordinationGameStatePrintAndWait("samePart is", samePart);
//				
//				BDDWrapper.free(bdd, intersect);
//				
//				epre = BDDWrapper.andTo(bdd, epre, samePart);
//				
////				cleanCoordinationGameStatePrintAndWait("finally epre is", epre);
//				
//				BDDWrapper.free(bdd, samePart);
//				controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, epre);
//				BDDWrapper.free(bdd, epre);
//			}
//			
//			
//		}
//		
////		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
////		int tmp = BDDWrapper.exists(bdd, controllableEpre, successSignalsCube);
////		BDDWrapper.free(bdd, controllableEpre);
////		controllableEpre = tmp;
////		BDDWrapper.free(bdd, successSignalsCube);
//		
////		cleanCoordinationGameStatePrintAndWait("ControlleableEpre is", controllableEpre);
//		
//		
//		
//		int controllablePredecessor = bdd.ref(bdd.getOne());
//		
//		for(int i=0; i<commands.length; i++){
////			System.out.println("computing controllable predecessor, environment part, for the command "+i);
//			
//			Integer[] envTransitions = command_environmentTransitions.get(i);
//			ArrayList<Variable[]> affectedEnvVars = affectedVariablesDuringEnvironmentTransitions.get(i);
//			ArrayList<Integer> affectedCubes = affectedCubeEnvironmentTransitions.get(i);
//			ArrayList<Integer> unaffectedCubes = unaffectedCubeEnvironmentTransitions.get(i);
//			ArrayList<Permutation> affectedPermutations = affectedPermutationEnvironmentTransitions.get(i);
////			ArrayList<Variable[]> unaffectedEnvVars = unaffectedVariablesDuringEnvironmentTransitions.get(i);
//			
//			//stutter with false next signals
//			int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
//			int controllableStatesWithCommand = BDDWrapper.exists(bdd, part, successSignalsCube);
//			BDDWrapper.free(bdd, part);
//			
////			cleanCoordinationGameStatePrintAndWait("stutter part", controllableStatesWithCommand);
//			
////			int controllableStatesWithCommand = bdd.ref(controllableEpre);
//
//			ArrayList<Integer> commandNotAvailableTrans = commandNotAvailableEnvironmentTransitions.get(i);
//			
//			for(int j=0; j<envTransitions.length; j++){
//				int trans = envTransitions[j];
//				
////				UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
//				
//				//stutter
//				if(j==0){
//					continue;
//				}else{
//					int[] decomposedSet = decomposeSetOfStates(controllableEpre, affectedEnvVars.get(j-1), unaffectedCubes.get(j-1));
//					
//					if(decomposedSet == null) continue;
//					
//					int epre = bdd.ref(bdd.getZero());
//					
//					int affectedCube = affectedCubes.get(j-1);
//					
//					for(int k=0; k<decomposedSet.length; k++){
//						
////						UtilityMethods.debugBDDMethodsAndWait(bdd, "decomposed set["+k+"]", decomposedSet[k]);
//						
////						cleanCoordinationGameStatePrintAndWait( "decomposed set["+k+"]", decomposedSet[k]);
//						
//						//TODO: try affected cube and affected permutation
////						int eprePart = BDDWrapper.EpreImage(bdd, decomposedSet[k], cube, trans, getVtoVprime());
//						int eprePart = BDDWrapper.EpreImage(bdd, decomposedSet[k], affectedCubes.get(j-1), trans, affectedPermutations.get(j-1));
//						
//						int intersect = BDDWrapper.and(bdd, decomposedSet[k], controllableEpre);
//						
//						int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
//						BDDWrapper.free(bdd, intersect);
//						
//						eprePart = BDDWrapper.andTo(bdd, eprePart, samePart);
//						BDDWrapper.free(bdd, samePart);
//						
//						epre = BDDWrapper.orTo(bdd, epre, eprePart);
//						BDDWrapper.free(bdd, eprePart);
//					}
//					
//					int commandNotAvailable = commandNotAvailableTrans.get(j-1);
//					epre = BDDWrapper.orTo(bdd, epre, commandNotAvailable);
//					
////					cleanCoordinationGameStatePrintAndWait("Epre is ", epre);
//					
//					controllableStatesWithCommand = BDDWrapper.andTo(bdd, controllableStatesWithCommand, epre);
//					
//					BDDWrapper.free(bdd, epre);
//				}
//				
//			}
//			
////			cleanCoordinationGameStatePrintAndWait("controllable states with commands are", controllableStatesWithCommand);
//			
//			controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//		}	
//			
//		
////		cleanCoordinationGameStatePrintAndWait("cpre of set is", controllablePredecessor);
//		return controllablePredecessor;
//	}
	
	//Symbolic + optimized + focused + without enumeration
		public int controllablePredecessor(int set){
			if(set == 0){
//				cleanCoordinationGameStatePrintAndWait("cpre of set is", set);
				return bdd.ref(set);
			}
			
			//stutter command let the system to preserve the current state
			int controllableEpre = bdd.ref(set);
			
//			cleanCoordinationGameStatePrintAndWait("Computing cpre for the set", set);
			
//			UtilityMethods.debugBDDMethods(bdd, "in cpre, set is", set);
//			System.out.println("affected vars are");
			
			
			for(int i=0; i<commands.length; i++){
				int trans = command_systemTransitions.get(i);
				
//				int epre = BDDWrapper.EpreImage(bdd, set, cube, trans, getVtoVprime());
				
//				int unaffectedCube = unaffectedCubeSystemTransitions.get(i);
				int affectedPrimeCube = affectedPrimeCubeSystemTransitions.get(i);
				
//				Variable.printVariables(affectedVars);
//				UtilityMethods.getUserInput();
				
				//TODO: compute once and save 
				Permutation affectedPermutation = affectedPermutationSystemTransitions.get(i);
				
				int affectedVarsReplaced = BDDWrapper.replace(bdd, set, affectedPermutation);
				
//				UtilityMethods.debugBDDMethods(bdd, "affected vars replaced by their primed copies", affectedVarsReplaced);

//				int changingPart = BDDWrapper.exists(bdd, affectedVarsReplaced, unaffectedCube); 
//				int temp = BDDWrapper.and(bdd, trans, changingPart);
//				temp = BDDWrapper.andTo(bdd, temp, affectedVarsReplaced);
				
				int temp = BDDWrapper.and(bdd, trans, affectedVarsReplaced);
				
				BDDWrapper.free(bdd, affectedVarsReplaced);
//				BDDWrapper.free(bdd, changingPart);
				int epre = BDDWrapper.exists(bdd, temp, affectedPrimeCube);
				
				controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, epre);
				
				BDDWrapper.free(bdd, epre);
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current system trans is ", trans);
			}
			
//			int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
//			int tmp = BDDWrapper.exists(bdd, controllableEpre, successSignalsCube);
//			BDDWrapper.free(bdd, controllableEpre);
//			controllableEpre = tmp;
//			BDDWrapper.free(bdd, successSignalsCube);
			
//			cleanCoordinationGameStatePrintAndWait("ControlleableEpre is", controllableEpre);
			
			
			
//			int controllablePredecessor = bdd.ref(bdd.getOne());
			
//			//stutter with false next signals
//			int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
//			int controllableStatesWithCommandStart = BDDWrapper.exists(bdd, part, successSignalsCube);
//			BDDWrapper.free(bdd, part);
			

			
			//stutter with false next signals
			int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
			int controllablePredecessor = BDDWrapper.exists(bdd, part, successSignalsCube);
			BDDWrapper.free(bdd, part);
			
			for(int i=0; i<commands.length; i++){
//				System.out.println("computing controllable predecessor, environment part, for the command "+i);
				
				Integer[] envTransitions = command_environmentTransitions.get(i);
				
//				int controllableStatesWithCommand = bdd.ref(controllableStatesWithCommandStart);
				
//				cleanCoordinationGameStatePrintAndWait("stutter part", controllableStatesWithCommand);
				
//				int controllableStatesWithCommand = bdd.ref(controllableEpre);
				
				
//				ArrayList<Integer> unaffectedCubes = unaffectedCubeEnvironmentTransitions.get(i);
				ArrayList<Integer> affectedPrimeCubes = affectedPrimeCubeEnvironmentTransitions.get(i);
				ArrayList<Permutation> affectedPermutations = affectedPermutationEnvironmentTransitions.get(i);
				
//				ArrayList<Integer> commandAvailableTrans = commandAvailableEnvironmentTransitions.get(i);
				ArrayList<Integer> commandNotAvailableTrans = commandNotAvailableEnvironmentTransitions.get(i);
				
				for(int j=0; j<envTransitions.length; j++){
					int trans = envTransitions[j];
					
//					if(j!=0){
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
//					}
					
					//stutter
					if(j==0){
						continue;
					}
					
//					int epre = BDDWrapper.EpreImage(bdd, controllableEpre, cube, trans, getVtoVprime());
					

					
//					int unaffectedCube = unaffectedCubes.get(j-1);
					int affectedPrimeCube = affectedPrimeCubes.get(j-1);
					Permutation affectedPermutation = affectedPermutations.get(j-1);
					
					int affectedVarsReplaced = BDDWrapper.replace(bdd, controllableEpre, affectedPermutation);
					
//					int changingPart = BDDWrapper.exists(bdd, affectedVarsReplaced, unaffectedCube); 
//					int temp = BDDWrapper.and(bdd, trans, changingPart);
//					temp = BDDWrapper.andTo(bdd, temp, affectedVarsReplaced);
					
					int temp = BDDWrapper.and(bdd, trans, affectedVarsReplaced);
					
					
					BDDWrapper.free(bdd, affectedVarsReplaced);
//					BDDWrapper.free(bdd, changingPart);
					int epre = BDDWrapper.exists(bdd, temp, affectedPrimeCube);
					
					int commandNotAvailable = commandNotAvailableTrans.get(j-1);
					epre = BDDWrapper.orTo(bdd, epre, commandNotAvailable);
					
//					controllableStatesWithCommand = BDDWrapper.andTo(bdd, controllableStatesWithCommand, epre);
					
					controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, epre);
					
//					cleanCoordinationGameStatePrintAndWait("controllable states with this trans are", epre);
					
					BDDWrapper.free(bdd, epre);
				}
				
//				cleanCoordinationGameStatePrintAndWait("controllable states with commands are", controllableStatesWithCommand);
				
//				controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
//				BDDWrapper.free(bdd, controllableStatesWithCommand);
			}	
				
			
//			cleanCoordinationGameStatePrintAndWait("cpre of set is", controllablePredecessor);
			return controllablePredecessor;
		}
	
		//OLDER VERSION before optimizations
//	//TODO: compute the command availability beforehand
//	public int controllablePredecessor(int set){
//		if(set == 0){
////			cleanCoordinationGameStatePrintAndWait("cpre of set is", set);
//			return bdd.ref(set);
//		}
//			
//		int cube = getPrimedVariablesAndActionsCube();
//		
//		//stutter command let the system to preserve the current state
//		int controllableEpre = bdd.ref(set);
//		
////		cleanCoordinationGameStatePrintAndWait("Computing cpre for the set", set);
//		
////		UtilityMethods.debugBDDMethods(bdd, "in cpre, set is", set);
////		System.out.println("affected vars are");
//		
//		
//		for(int i=0; i<commands.length; i++){
//			int trans = command_systemTransitions.get(i);
//			
////			int epre = BDDWrapper.EpreImage(bdd, set, cube, trans, getVtoVprime());
//			
//			Variable[] affectedVars = affectedVariablesDuringSystemTransitions.get(i);
//			Variable[] unaffectedVars = unaffectedVariablesDuringSystemTransitions.get(i);
//			
//			//TODO: compute once and save 
//			int unaffectedCube = BDDWrapper.createCube(bdd, unaffectedVars);
//			int affectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(affectedVars));
//			
////			Variable.printVariables(affectedVars);
////			UtilityMethods.getUserInput();
//			
//			//TODO: compute once and save 
//			Permutation affectedPermutation = BDDWrapper.createPermutations(bdd, affectedVars, Variable.getPrimedCopy(affectedVars));
//			
//			int affectedVarsReplaced = BDDWrapper.replace(bdd, set, affectedPermutation);
//			
////			UtilityMethods.debugBDDMethods(bdd, "affected vars replaced by their primed copies", affectedVarsReplaced);
//			
//			int changingPart = BDDWrapper.exists(bdd, affectedVarsReplaced, unaffectedCube); 
//			int temp = BDDWrapper.and(bdd, trans, changingPart);
//			temp = BDDWrapper.andTo(bdd, temp, affectedVarsReplaced);
//			BDDWrapper.free(bdd, affectedVarsReplaced);
//			BDDWrapper.free(bdd, changingPart);
//			BDDWrapper.free(bdd, unaffectedCube);
//			int epre = BDDWrapper.exists(bdd, temp, affectedPrimeCube);
//			BDDWrapper.free(bdd, affectedPrimeCube);
//			
//			controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, epre);
//			
////			UtilityMethods.debugBDDMethodsAndWait(bdd, "current system trans is ", trans);
//		}
//		
////		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
////		int tmp = BDDWrapper.exists(bdd, controllableEpre, successSignalsCube);
////		BDDWrapper.free(bdd, controllableEpre);
////		controllableEpre = tmp;
////		BDDWrapper.free(bdd, successSignalsCube);
//		
////		cleanCoordinationGameStatePrintAndWait("ControlleableEpre is", controllableEpre);
//		
//		
//		
//		int controllablePredecessor = bdd.ref(bdd.getOne());
//		
//		//stutter with false next signals
//		int allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
//		int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
//		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
//		int controllableStatesWithCommandStart = BDDWrapper.exists(bdd, part, successSignalsCube);
//		BDDWrapper.free(bdd, allSuccessSignalsFalse);
//		BDDWrapper.free(bdd, part);
//		
//		for(int i=0; i<commands.length; i++){
////			System.out.println("computing controllable predecessor, environment part, for the command "+i);
//			
//			Integer[] envTransitions = command_environmentTransitions.get(i);
//			
//			int controllableStatesWithCommand = bdd.ref(controllableStatesWithCommandStart);
//			
////			cleanCoordinationGameStatePrintAndWait("stutter part", controllableStatesWithCommand);
//			
////			int controllableStatesWithCommand = bdd.ref(controllableEpre);
//			
//			ArrayList<Variable[]> envAffectedVars = affectedVariablesDuringEnvironmentTransitions.get(i);
//			ArrayList<Variable[]> envUnaffectedVars = unaffectedVariablesDuringEnvironmentTransitions.get(i);
//			
//			for(int j=0; j<envTransitions.length; j++){
//				int trans = envTransitions[j];
//				
////				if(j!=0){
////					UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
////				}
//				
//				//stutter
//				if(j==0){
//					continue;
//				}
//				
////				int epre = BDDWrapper.EpreImage(bdd, controllableEpre, cube, trans, getVtoVprime());
//				
//				Variable[] affectedVars =  envAffectedVars.get(j-1);
//				Variable[] unaffectedVars = envUnaffectedVars.get(j-1);
//				int unaffectedCube = BDDWrapper.createCube(bdd, unaffectedVars);
//				int affectedPrimeCube = BDDWrapper.createCube(bdd, Variable.getPrimedCopy(affectedVars));
//				Permutation affectedPermutation = BDDWrapper.createPermutations(bdd, affectedVars, Variable.getPrimedCopy(affectedVars));
//				int affectedVarsReplaced = BDDWrapper.replace(bdd, controllableEpre, affectedPermutation);
//				int changingPart = BDDWrapper.exists(bdd, affectedVarsReplaced, unaffectedCube); 
//				int temp = BDDWrapper.and(bdd, trans, changingPart);
//				temp = BDDWrapper.andTo(bdd, temp, affectedVarsReplaced);
//				BDDWrapper.free(bdd, affectedVarsReplaced);
//				BDDWrapper.free(bdd, changingPart);
//				BDDWrapper.free(bdd, unaffectedCube);
//				int epre = BDDWrapper.exists(bdd, temp, affectedPrimeCube);
//				BDDWrapper.free(bdd, affectedPrimeCube);
//				
//				int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
//				int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
//				BDDWrapper.free(bdd, commandAvailable);
//				epre = BDDWrapper.orTo(bdd, epre, commandNotAvailable);
//				BDDWrapper.free(bdd, commandNotAvailable);
//				
//				controllableStatesWithCommand = BDDWrapper.andTo(bdd, controllableStatesWithCommand, epre);
//				
////				cleanCoordinationGameStatePrintAndWait("controllable states with this trans are", epre);
//				
//				BDDWrapper.free(bdd, epre);
//			}
//			
////			cleanCoordinationGameStatePrintAndWait("controllable states with commands are", controllableStatesWithCommand);
//			
//			controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//		}	
//			
//		
////		cleanCoordinationGameStatePrintAndWait("cpre of set is", controllablePredecessor);
//		return controllablePredecessor;
//	}
	
	public static void main(String[] args){
		
	}
	
	public int assignValueToDensityVars(int[] densityVector){
		if(densityVector.length != densityVars.size()){
			System.out.println("the density vector does not match the size of the density vars!");
			return bdd.ref(bdd.getZero());
		}
		int result = bdd.ref(bdd.getOne());
		for(int i=0; i<densityVars.size(); i++){
			int value = BDDWrapper.assign(bdd, densityVector[i], densityVars.get(i));
			result = BDDWrapper.andTo(bdd, result, value);
			BDDWrapper.free(bdd, value);
		}
		return result;
	}
	
	public void printCommandsInfo(){
		System.out.println("printing system part");
		for(int i =0; i<commands.length; i++){
			System.out.println();
			UtilityMethods.debugBDDMethods(bdd, "command "+i, command_systemTransitions.get(i));
			
			System.out.println("affected vars");
			Variable.printVariables(affectedVariablesDuringSystemTransitions.get(i));
			
			System.out.println("unaffected vars");
			Variable.printVariables(unaffectedVariablesDuringSystemTransitions.get(i));
		}
		
		System.out.println();
		System.out.println("prnting environment part");
		for(int i=0; i<commands.length; i++){
			System.out.println();
			Integer[] envTrans = command_environmentTransitions.get(i);
			
			ArrayList<Variable[]> affectedVars = affectedVariablesDuringEnvironmentTransitions.get(i);
			ArrayList<Variable[]> unaffectedVars = unaffectedVariablesDuringEnvironmentTransitions.get(i);
			
			System.out.println("command "+i);
			for(int j=0; j<envTrans.length; j++){
				UtilityMethods.debugBDDMethods(bdd, "part "+j, envTrans[j]);
				
				if(j!=0){
					System.out.println("affected vars");
					Variable.printVariables(affectedVars.get(j-1));
					
					System.out.println("unaffected vars");
					Variable.printVariables(unaffectedVars.get(j-1));
				}
			}
		}
	}
	
	public void cleanPrintSetOutWithDontCares(int formula){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
//		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		
		if(formula ==1){
			System.out.println("TRUE");
			return;
		}
		

		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, formula, variables);
		if(minterms == null){
			System.out.println("FALSE");
			return;
		}
		for(String minterm : minterms ){
//			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			System.out.print("densityVars: ");
			for(int i=0; i<densityVars.size(); i++){
				String value = minterm.substring(index, index+densityVars.get(i).length);
				index+=densityVars.get(i).length;
				System.out.print(value+" ");
			}
			//tasks
			System.out.print("Tasks: ");
			String value = minterm.substring(index, index+taskVars.length);
			index+= taskVars.length;
			System.out.print(value);
			System.out.print(" Signals: ");
			String successSignalsValues = minterm.substring(index);
			System.out.print(successSignalsValues);
			System.out.println();
		}
	}
	
	public void cleanCoordinationGameStatePrintAndWait(int states){
		cleanPrintSetOutWithDontCares(states);
		UtilityMethods.getUserInput();
	}
	
	public void cleanCoordinationGameStatePrintAndWait(String message, int states){
		System.out.println(message);
		cleanPrintSetOutWithDontCares(states);
		UtilityMethods.getUserInput();
	}
	
	public int EpostImage(int set, Player player){
		
//		System.out.println("Running epost in compositional");
//		long t0=UtilityMethods.timeStamp();
//		
		if(set == 0){
			return set;
		}
		
		
		int Epost = bdd.ref(bdd.getOne());
		
		
		
		if(player == Player.ENVIRONMENT){
			Epost = BDDWrapper.exists(bdd, set, successSignalsCube);
			Epost = BDDWrapper.andTo(bdd, Epost, allSuccessSignalsFalse);
			
			for(int i=0; i<command_environmentTransitions.size(); i++){
				Integer[] envTrans = command_environmentTransitions.get(i);
				

				
				ArrayList<Integer> affectedCubes = affectedCubeEnvironmentTransitions.get(i);
//				ArrayList<Integer> unaffectedCubes = unaffectedCubeEnvironmentTransitions.get(i);
				ArrayList<Permutation> affectedPrimePermutations = affectedPrimePermutationEnvironmentTransitions.get(i);
				
				
				for(int j=1; j<envTrans.length; j++){
					int trans = envTrans[j]; 
					
					
//					int post = BDDWrapper.EpostImage(bdd, set, cube, trans, getVprimetoV());
					

//					int unaffectedCube = unaffectedCubes.get(j-1);
					int affectedCube = affectedCubes.get(j-1);
					Permutation affectedPrimePermutation = affectedPrimePermutations.get(j-1);
					
//					int changingPart = BDDWrapper.exists(bdd, set, unaffectedCube); 
//					int temp = BDDWrapper.and(bdd, trans, changingPart);
//					temp = BDDWrapper.andTo(bdd, temp, set);
					
					int temp = BDDWrapper.and(bdd, trans, set);
					
//					BDDWrapper.free(bdd, changingPart);
					int temp2 = BDDWrapper.exists(bdd, temp, affectedCube);
					BDDWrapper.free(bdd, temp);
					int post = BDDWrapper.replace(bdd, temp2, affectedPrimePermutation);
					BDDWrapper.free(bdd, temp2);
					
					Epost = BDDWrapper.orTo(bdd, Epost, post);
					BDDWrapper.free(bdd, post);	
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
				}		
			}
		}else{
			Epost = bdd.ref(set);
			for(int i=0; i<commands.length; i++){
				int trans = command_systemTransitions.get(i);
				
//				int post = BDDWrapper.EpostImage(bdd, set, cube, trans, getVprimetoV());

//				int unaffectedCube = unaffectedCubeSystemTransitions.get(i);
				int affectedCube = affectedCubeSystemTransitions.get(i);
				Permutation affectedPrimePermutation = affectedPrimePermutationSystemTransitions.get(i);
				
//				int changingPart = BDDWrapper.exists(bdd, set, unaffectedCube); 
//				int temp = BDDWrapper.and(bdd, trans, changingPart);
//				temp = BDDWrapper.andTo(bdd, temp, set);
//				BDDWrapper.free(bdd, changingPart);
				
				int temp = BDDWrapper.and(bdd, trans, set);
				
				int temp2 = BDDWrapper.exists(bdd, temp, affectedCube);
				BDDWrapper.free(bdd, temp);
				int post = BDDWrapper.replace(bdd, temp2, affectedPrimePermutation);
				BDDWrapper.free(bdd, temp2);
				
				Epost = BDDWrapper.orTo(bdd, Epost, post);
				BDDWrapper.free(bdd, post);
			}
		}
		
//		UtilityMethods.duration(t0, "epost computed in ");
		
		return Epost;
	}
	
	public int symbolicGameOneStepExecution(int set){
		int envMoves = EpostImage(set, Player.ENVIRONMENT);
		
//		cleanCoordinationGameStatePrintAndWait("env moves in symbolic step", envMoves);
		
		int sysMoves = EpostImage(envMoves, Player.SYSTEM);
		
//		cleanCoordinationGameStatePrintAndWait("sys moves in symbolic step ", sysMoves);
		
		return sysMoves;
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
	
	private int[] decomposeSetOfStates(int set, Variable[] affectedVars, int unaffectedCube){
		int affectedVarsPart = BDDWrapper.exists(bdd, set, unaffectedCube);
		int[] result = BDDWrapper.enumerate(bdd, affectedVarsPart, affectedVars);
		return result;
	}
	
	private int[] decomposeSetOfStates(int set, Variable[] affectedVars){
		Variable[] unaffectedVars = Variable.difference(variables, affectedVars);
		int cube = BDDWrapper.createCube(bdd, unaffectedVars);
		int affectedVarsPart = BDDWrapper.exists(bdd, set, cube);
		int[] result = BDDWrapper.enumerate(bdd, affectedVarsPart, affectedVars);
		return result;
	}
	
}
