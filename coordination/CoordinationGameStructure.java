package coordination; 

import java.util.ArrayList;

import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import game.BDDWrapper;
import game.GameStructure;
import game.Player;
import game.Variable;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

/**
 * TODO: do we need the action vars? 
 * TODO: Do we need the define the stutter explicitely
 * @author salarmoarref
 *
 */
public class CoordinationGameStructure extends game.GameStructure{

	int numOfRobots;
	Variable[] taskVars; 
	ArrayList<Variable[]> densityVars;
	Variable[] successSignals;
	
	SimpleCommandVector[] commands;
	
	ArrayList<Integer> command_systemTransitions;
	ArrayList<Variable[]> affectedVariablesDuringSystemTransitions;
	ArrayList<Variable[]> unaffectedVariablesDuringSystemTransitions;
	ArrayList<Integer[]> command_environmentTransitions;
	ArrayList<ArrayList<Variable[]>> affectedVariablesDuringEnvironmentTransitions;
	ArrayList<ArrayList<Variable[]>> unaffectedVariablesDuringEnvironmentTransitions;
	
	public CoordinationGameStructure(BDD argBdd) {
		super(argBdd);
	}
	
	public CoordinationGameStructure(BDD argBDD, int argNumOfRobots, SimpleCommandVector[] argCommands, ArrayList<Variable[]> argDensityVars, Variable[] argTaskVars, Variable[] argSuccessSignals){
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
		
		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
		actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		envActionVars = actionVars;
		sysActionVars = actionVars;
		actionVars=Variable.unionVariables(envActionVars, sysActionVars);
		actionsCube=-1;
		
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
		computeCommandsTransitionFormulas();
		
	}
	
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
	
	private void computeCommandsTransitionFormulas(){
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
					
			int act = BDDWrapper.assign(bdd, i, actionVars);
					
					//debugging
//					System.out.println("current command is c"+(i+1));
//					UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//					UtilityMethods.getUserInput();
					
			int commandFormula_sys = commandFormula_systemTransitions(i, act);
					
//					UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//					UtilityMethods.getUserInput();
					
			command_systemTransitions.add(commandFormula_sys);
					
			Integer[] commandFormula_env = commandFormula_environmentTransitions(i, act);
					
//					UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//					UtilityMethods.getUserInput();
					
					command_environmentTransitions.add(commandFormula_env);
					
				}
				
				//add stutter to system transition relation
//				int act = BDDWrapper.assign(bdd, commands.length, actionVars);
//				int stutter = createStutterFormula();
//				stutter = BDDWrapper.andTo(bdd, stutter, act);
//				command_systemTransitions.add(stutter);
	}
	
	private int commandFormula_systemTransitions(int index, int commandAction){
		
		int commandFormula = bdd.ref(bdd.getOne());
		
//		Variable commandSuccessSignalPrime = commandSuccessSignal.getPrimedCopy();
//		int successSignal = bdd.ref(bdd.not(commandSuccessSignalPrime.getBDDVar()));
		
//		int sameSuccessSignal = BDDWrapper.same(bdd, commandSuccessSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
		int sameSuccessSignal = BDDWrapper.same(bdd, successSignals);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
//		int falseNextSuccessSignal = allFalseNextSuccessSignals(bdd, successSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, falseNextSuccessSignal);
		
		SimpleCommandVector command = commands[index];
		Variable[] affectedVariables = affectedVariablesDuringSystemTransitions.get(index);
		
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
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
		//TODO: maybe we can optimize the code above, for now we simply existensially quantify the unaffected vars
		Variable[] unaffectedVars = Variable.difference(variables, affectedVariables);
		int tmp = commandFormula;
		int cube = BDDWrapper.createCube(bdd, unaffectedVars);
		commandFormula = BDDWrapper.exists(bdd, commandFormula, cube);
		BDDWrapper.free(bdd, tmp);
		BDDWrapper.free(bdd,cube);
		
		return commandFormula;
	}
	
	/**
	 * TODO: remove the stutter from all commands and just add it once
	 * TODO: can we remove action vars?
	 * stutter - success - failure, respectively
	 * @param index
	 * @param commandAction
	 * @return
	 */
	private Integer[] commandFormula_environmentTransitions(int index, int commandAction){
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
		
		int allNextSuccessSignalsOFF = allFalseNextSuccessSignals(bdd, successSignals);
		int allButOneSuccessSignalsOFF = setNextSuccessSignals(bdd, successSignals, successSignals[index]);
		
		stutter = BDDWrapper.andTo(bdd, stutter, allNextSuccessSignalsOFF);
		success = BDDWrapper.andTo(bdd, success, allButOneSuccessSignalsOFF);
		fail = BDDWrapper.andTo(bdd, fail, allNextSuccessSignalsOFF);
		
		BDDWrapper.free(bdd, allNextSuccessSignalsOFF);
		BDDWrapper.free(bdd, allButOneSuccessSignalsOFF);
		
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
		stutter = BDDWrapper.andTo(bdd, stutter, commandAction);
		commandFormula[0] = stutter; 
		
		success = BDDWrapper.andTo(bdd, success, commandAction);
		int tmp = success;
		ArrayList<Variable[]> unaffectedEnvVars = unaffectedVariablesDuringEnvironmentTransitions.get(index);
		int cube = BDDWrapper.createCube(bdd, unaffectedEnvVars.get(0));
		commandFormula[1] = BDDWrapper.exists(bdd, success, cube);
		BDDWrapper.free(bdd, tmp);
		BDDWrapper.free(bdd,cube);
		
		fail = BDDWrapper.andTo(bdd, fail, commandAction);
		int tmp2 = fail;
		int cube2 = BDDWrapper.createCube(bdd, unaffectedEnvVars.get(1));
		commandFormula[2] = BDDWrapper.exists(bdd, fail, cube2);
		BDDWrapper.free(bdd, tmp2);
		BDDWrapper.free(bdd,cube2);
		
		return commandFormula;
		
		
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
	
	public static int densityLowerBoundConstraints(BDD bdd, ArrayList<Variable[]> densityVars, int[] lowerBounds, int upperBound){
		int result= bdd.ref(bdd.getOne());
		for(int i=0; i<densityVars.size();i++){
			int x_i = densityLowerBoundCondition(bdd, densityVars.get(i), lowerBounds[i], upperBound);
			result = BDDWrapper.andTo(bdd, result, x_i);
			BDDWrapper.free(bdd, x_i);
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
	
	public static int[] createZeroVector(int length){
		int[] zeroVector = new int[length];
		for(int i=0; i<length; i++){
			zeroVector[i]=0;
		}
		return zeroVector;
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

	
	public static int allFalseNextSuccessSignals(BDD bdd, Variable[] successSignals){
		boolean[] allFalse = UtilityMethods.allFalseArray(successSignals.length);
		int falseNextSuccessSignals = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		return falseNextSuccessSignals;
	}
	
	
	public CoordinationGameStructure(BDD argBdd, Variable[] variables, int init, ArrayList<Integer> T_env, ArrayList<Integer> T_sys,  Variable[] argEnvActions, Variable[] argSysActions){
		super(argBdd, variables, Variable.getPrimedCopy(variables), init,  T_env, T_sys, argEnvActions, argSysActions); 
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
	
	private int[] decomposeSetOfStates(int set, Variable[] affectedVars){
		Variable[] unaffectedVars = Variable.difference(variables, affectedVars);
		int cube = BDDWrapper.createCube(bdd, unaffectedVars);
		int affectedVarsPart = BDDWrapper.exists(bdd, set, cube);
		int[] result = BDDWrapper.enumerate(bdd, affectedVarsPart, affectedVars);
//		for(int i=0; i<result.length; i++){
//			result[i]=BDDWrapper.andTo(bdd, result[i], set);
//		}
		
//		System.out.println("in decompose set");
//		UtilityMethods.debugBDDMethods(bdd, "set is", set);
//		System.out.println("unaffected vars are");
//		Variable.printVariables(unaffectedVars);
//		UtilityMethods.debugBDDMethods(bdd, "affectedVarsPart", affectedVarsPart);
//		System.out.println("enumerating");
//		for(int i=0; i<result.length; i++){
//			UtilityMethods.debugBDDMethods(bdd, "result["+i+"]", result[i]);
//		}
//		UtilityMethods.getUserInput();
		
		return result;
	}
	
	public int controllablePredecessor(int set){
			
//		UtilityMethods.debugBDDMethods(bdd, "computing controllable predecessor, target set is ",  set);
//		UtilityMethods.getUserInput();
		
//		cleanCoordinationGameStatePrintAndWait("computing the controllable predecessor, target set is",  set);
		
		if(set == 0){
//			cleanCoordinationGameStatePrintAndWait("cpre of set is", set);
			return bdd.ref(set);
		}
			
		int cube = getPrimedVariablesAndActionsCube();
		
		//stutter command let the system to preserve the current state
		int controllableEpre = bdd.ref(set);
		
		
		
		for(int i=0; i<commands.length; i++){
			int trans = command_systemTransitions.get(i);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "current system trans is ", trans);
			
			//enumerate the set based on affected variables by the current command
			int[] decomposedSet = decomposeSetOfStates(set, affectedVariablesDuringSystemTransitions.get(i));
			
			if(decomposedSet == null) continue;
			int affectedCube = BDDWrapper.createCube(bdd, affectedVariablesDuringSystemTransitions.get(i));
			
			//compute the EPre for the changing part & combine it with the unchanging part
			for(int j=0; j<decomposedSet.length; j++){
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "decomposedSet["+j+"]", decomposedSet[j]);
//				cleanCoordinationGameStatePrintAndWait("decomposedSet["+j+"]", decomposedSet[j]);
				
				int epre = BDDWrapper.EpreImage(bdd, decomposedSet[j], cube, trans, getVtoVprime());
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "epre is", epre);
//				cleanCoordinationGameStatePrintAndWait("epre is", epre);
				
				int intersect = BDDWrapper.and(bdd, decomposedSet[j], set);
				
				int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "samePart is", samePart);
//				cleanCoordinationGameStatePrintAndWait("samePart is", samePart);
				
				BDDWrapper.free(bdd, intersect);
				
				epre = BDDWrapper.andTo(bdd, epre, samePart);
				
//				cleanCoordinationGameStatePrintAndWait("finally epre is", epre);
				
				BDDWrapper.free(bdd, samePart);
				controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, epre);
				BDDWrapper.free(bdd, epre);
			}
			
			BDDWrapper.free(bdd, affectedCube);
			
		}
		
//		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
//		int tmp = BDDWrapper.exists(bdd, controllableEpre, successSignalsCube);
//		BDDWrapper.free(bdd, controllableEpre);
//		controllableEpre = tmp;
//		BDDWrapper.free(bdd, successSignalsCube);
		
//		cleanCoordinationGameStatePrintAndWait("ControlleableEpre is", controllableEpre);
		
		
		
		int controllablePredecessor = bdd.ref(bdd.getOne());
		
		for(int i=0; i<commands.length; i++){
//			System.out.println("computing controllable predecessor, environment part, for the command "+i);
			
			Integer[] envTransitions = command_environmentTransitions.get(i);
			ArrayList<Variable[]> affectedEnvVars = affectedVariablesDuringEnvironmentTransitions.get(i);
//			ArrayList<Variable[]> unaffectedEnvVars = unaffectedVariablesDuringEnvironmentTransitions.get(i);
			
			//stutter with false next signals
			int allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
			int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
			int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
			int controllableStatesWithCommand = BDDWrapper.exists(bdd, part, successSignalsCube);
			BDDWrapper.free(bdd, allSuccessSignalsFalse);
			BDDWrapper.free(bdd, part);
			
//			cleanCoordinationGameStatePrintAndWait("stutter part", controllableStatesWithCommand);
			
//			int controllableStatesWithCommand = bdd.ref(controllableEpre);
			
			
			for(int j=0; j<envTransitions.length; j++){
				int trans = envTransitions[j];
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
				
				//stutter
				if(j==0){
					continue;
				}else{
					int[] decomposedSet = decomposeSetOfStates(controllableEpre, affectedEnvVars.get(j-1));
					
					if(decomposedSet == null) continue;
					
					int epre = bdd.ref(bdd.getZero());
					
					int affectedCube = BDDWrapper.createCube(bdd, affectedEnvVars.get(j-1));
					
					for(int k=0; k<decomposedSet.length; k++){
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "decomposed set["+k+"]", decomposedSet[k]);
						
//						cleanCoordinationGameStatePrintAndWait( "decomposed set["+k+"]", decomposedSet[k]);
						
						int eprePart = BDDWrapper.EpreImage(bdd, decomposedSet[k], cube, trans, getVtoVprime());
						int intersect = BDDWrapper.and(bdd, decomposedSet[k], controllableEpre);
						
						int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
						BDDWrapper.free(bdd, intersect);
						
						eprePart = BDDWrapper.andTo(bdd, eprePart, samePart);
						BDDWrapper.free(bdd, samePart);
						
						epre = BDDWrapper.orTo(bdd, epre, eprePart);
						BDDWrapper.free(bdd, eprePart);
					}
					
					BDDWrapper.free(bdd, affectedCube);
					int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
					int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
					BDDWrapper.free(bdd, commandAvailable);
					epre = BDDWrapper.orTo(bdd, epre, commandNotAvailable);
					BDDWrapper.free(bdd, commandNotAvailable);
					
//					cleanCoordinationGameStatePrintAndWait("Epre is ", epre);
					
					controllableStatesWithCommand = BDDWrapper.andTo(bdd, controllableStatesWithCommand, epre);
					
					BDDWrapper.free(bdd, epre);
				}
				
			}
			
//			cleanCoordinationGameStatePrintAndWait("controllable states with commands are", controllableStatesWithCommand);
			
			controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
			BDDWrapper.free(bdd, controllableStatesWithCommand);
		}	
			
		
//		cleanCoordinationGameStatePrintAndWait("cpre of set is", controllablePredecessor);
		return controllablePredecessor;
	}
	
//	//Assumes every transition in the arraylist corresponds to a single command
//	public int controllablePredecessor(int set){
//		
//		UtilityMethods.debugBDDMethods(bdd, "computing controllable predecessor, target set is ",  set);
//		UtilityMethods.getUserInput();
//		
//		int cube = getPrimedVariablesAndActionsCube();
//		
//		int controllableEpre = bdd.ref(bdd.getOne());
//		
//		//Note that in coordination game, the system player always moves first
//		for(Integer trans : getT_envList()){
//			
//			UtilityMethods.debugBDDMethods(bdd, "current trans is", trans);
//			
//			int controllableStatesWithCommand = ApreImage(set, cube, trans);
//			
//			int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
//			int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
//			BDDWrapper.free(bdd, commandAvailable);
//			controllableStatesWithCommand = BDDWrapper.orTo(bdd, controllableStatesWithCommand, commandNotAvailable);
//			BDDWrapper.free(bdd, commandNotAvailable);
//			
//			controllableEpre = BDDWrapper.andTo(bdd, controllableEpre, controllableStatesWithCommand);
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//			
//			UtilityMethods.debugBDDMethods(bdd, "current controllableEpre is", controllableEpre);
//			UtilityMethods.getUserInput();
//		}
//		
//		int controllablePredecessor = bdd.getZero();
//		
//		for(Integer trans : getT_sysList()){
//			
//			UtilityMethods.debugBDDMethods(bdd, "sys trans is", trans);
//			
//			int controllableStatesWithCommand = EpreImage(controllableEpre, cube, trans);
//			controllablePredecessor = BDDWrapper.orTo(bdd, controllablePredecessor, controllableStatesWithCommand);
//		
//			UtilityMethods.debugBDDMethods(bdd, "controllable States with command is", controllableStatesWithCommand);
//			
//			
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//			UtilityMethods.getUserInput();
//			
//		
//		}
//		
//		return controllablePredecessor;
//	}
	
//	//Assumes every transition in the arraylist corresponds to a single command
//	public int controllablePredecessor(int set){
//			
////		UtilityMethods.debugBDDMethods(bdd, "computing controllable predecessor, target set is ",  set);
////		UtilityMethods.getUserInput();
//			
//		int cube = getPrimedVariablesAndActionsCube();
//			
//		int controllableEpre = bdd.ref(bdd.getZero());
//		
//		for(Integer trans : getT_sysList()){
//			
////			UtilityMethods.debugBDDMethods(bdd, "sys trans is", trans);
//			
//			int controllableStatesWithCommand = EpreImage(set, cube, trans);
//			controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, controllableStatesWithCommand);
//		
////			UtilityMethods.debugBDDMethods(bdd, "controllable States with command is", controllableStatesWithCommand);
////			UtilityMethods.getUserInput();
//			
//			
////			UtilityMethods.debugBDDMethods(bdd, "controllableEpre is ", controllableEpre);
//			
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//		
//		}
//		
//		int controllablePredecessor = bdd.getOne();
//		
//		for(Integer trans : getT_envList()){
//				
////			UtilityMethods.debugBDDMethods(bdd, "current trans is", trans);
//				
//			int controllableStatesWithCommand = ApreImage(controllableEpre, cube, trans);
//				
//			int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
//			int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
//			BDDWrapper.free(bdd, commandAvailable);
//			controllableStatesWithCommand = BDDWrapper.orTo(bdd, controllableStatesWithCommand, commandNotAvailable);
//			BDDWrapper.free(bdd, commandNotAvailable);
//				
//			controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
//			BDDWrapper.free(bdd, controllableStatesWithCommand);
//				
////			UtilityMethods.debugBDDMethods(bdd, "current controllable predecessor is", controllablePredecessor);
////			UtilityMethods.getUserInput();
//		}
//			
//			
//			
//			
//			
//		return controllablePredecessor;
//	}
	
	public static void main(String[] args){
		simpleTest2();
	}
	
	public static void  simpleTest1(){
		BDD bdd = new BDD(10000, 1000); 
		
		int upperBound = 7; 
		int numOfBits = UtilityMethods.numOfBits(upperBound);
		
		Variable[] vars = Variable.createVariables(bdd, numOfBits, "X");
		Variable[] varsPrime = Variable.createPrimeVariables(bdd, vars);
		
		int init = BDDWrapper.assign(bdd, 0, vars);
		
		Variable act = new Variable(bdd, "act");
		Variable[] actionVars = new Variable[]{act};
		
		ArrayList<Integer> T_env = new  ArrayList<Integer>();
		ArrayList<Integer> T_sys = new ArrayList<Integer>();
		
		//increment
		int inc = BDDWrapper.assign(bdd, 0, actionVars);
		int incTrans = bdd.ref(bdd.getZero());
		for(int i=0; i<upperBound; i++){
			int pre = BDDWrapper.assign(bdd, i, vars);
			int post = BDDWrapper.assign(bdd, i+1, varsPrime);
			int trans = BDDWrapper.and(bdd, pre, inc);
			trans = BDDWrapper.andTo(bdd, trans, post);
			BDDWrapper.free(bdd, pre);
			BDDWrapper.free(bdd, post);
			incTrans = BDDWrapper.orTo(bdd, incTrans, trans);
//			T_env.add(trans);
//			T_sys.add(trans);
		}
		
		T_env.add(incTrans);
		T_sys.add(incTrans);
		
		//decrement
		int dec = BDDWrapper.assign(bdd, 1, actionVars);
		int decTrans = bdd.ref(bdd.getZero());
		for(int i=upperBound; i>0; i--){
			int pre = BDDWrapper.assign(bdd, i, vars);
			int post = BDDWrapper.assign(bdd, i-1, varsPrime);
			int trans = BDDWrapper.and(bdd, pre, dec);
			trans = BDDWrapper.andTo(bdd, trans, post);
			BDDWrapper.free(bdd, pre);
			BDDWrapper.free(bdd, post);
			decTrans = BDDWrapper.orTo(bdd, decTrans, trans);
//			T_env.add(trans);
//			T_sys.add(trans);
		}
		
		T_env.add(decTrans);
		T_sys.add(decTrans);
		
		
		
		
		//define two game structures: one a simple game structure, the other one  coordination game structure
		GameStructure gs = new GameStructure(bdd, vars, Variable.getPrimedCopy(vars), init, T_env, T_sys, actionVars, actionVars);
//		
//		gs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		CoordinationGameStructure cgs = new CoordinationGameStructure(bdd, vars, init, T_env, T_sys, actionVars, actionVars); 
		
//		cgs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		int Z = BDDWrapper.assign(bdd, 2, vars);
		
		int cpreGS = gs.controllablePredecessor(Z);
		
		UtilityMethods.debugBDDMethods(bdd, "cpre simple game structure", cpreGS);
		
		UtilityMethods.getUserInput();
		
		int cpreCGS = cgs.controllablePredecessor(Z);
		
		UtilityMethods.debugBDDMethods(bdd, "cpre coordination game structure ", cpreCGS);
		
		UtilityMethods.getUserInput();
	}
	
	public static void simpleTest2(){
		BDD bdd = new BDD(10000, 1000);
		
		int upperBound = 32767; 
		int numOfBits = UtilityMethods.numOfBits(upperBound); 
		
		Variable[] vars = Variable.createVariables(bdd, numOfBits, "X");
		Variable[] varsPrime = Variable.createPrimeVariables(bdd, vars);
		
		int init = BDDWrapper.assign(bdd, 0, vars);
		
//		Variable act = new Variable(bdd, "act");
//		Variable[] actionVars = new Variable[]{act};
		
		int maxIncOrDec = 31; 
		int numOfActionBits = UtilityMethods.numOfBits(2*maxIncOrDec-1);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionBits, "actionVars");
		
		ArrayList<Integer> T_env = new  ArrayList<Integer>();
		ArrayList<Integer> T_sys = new ArrayList<Integer>();
		
		int actionIndex = 0;
		for(int i = 1; i<=maxIncOrDec; i++){
			//inc by i
			int incAct = BDDWrapper.assign(bdd, actionIndex, actionVars);
			actionIndex++;
			int transInc = UtilityTransitionRelations.increment(bdd, vars, 0, upperBound-i+1, i, incAct);
			
			T_env.add(transInc);
			T_sys.add(transInc);
			
			//dec by i
			int decAct = BDDWrapper.assign(bdd, actionIndex, actionVars);
			actionIndex++;
			int transDec = UtilityTransitionRelations.decrement(bdd, vars, 0+i-1, upperBound, i, decAct);
			
			T_env.add(transDec);
			T_sys.add(transDec);
		}
		
		//define two game structures: one a simple game structure, the other one  coordination game structure
		GameStructure gs = new GameStructure(bdd, vars, Variable.getPrimedCopy(vars), init, T_env, T_sys, actionVars, actionVars);
		
//		gs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		CoordinationGameStructure cgs = new CoordinationGameStructure(bdd, vars, init, T_env, T_sys, actionVars, actionVars); 
		
//		cgs.printGame();
//		
//		UtilityMethods.getUserInput();
		
		//define Z the set of states
		int Z = bdd.ref(bdd.getZero());
		
		int zLow = 0; 
		
		int zHigh = maxIncOrDec; 
		
		for(int i=zLow; i<=zHigh ; i++){
			int state = BDDWrapper.assign(bdd, i, vars);
			Z = BDDWrapper.orTo(bdd, Z, state);
			BDDWrapper.free(bdd, state);
		}
		
		//testing CPre

		long t0 = UtilityMethods.timeStamp();
//		int cpreGS = gs.controllablePredecessor(Z);
//		
//		UtilityMethods.duration(t0, "Cpre for the simple game structure");
//		UtilityMethods.debugBDDMethods(bdd, "cpre simple game structure", cpreGS);
//		
//		UtilityMethods.getUserInput();
		
		
		t0 = UtilityMethods.timeStamp();
		int cpreCGS = cgs.controllablePredecessor(Z);
		
		UtilityMethods.duration(t0, "Cpre for the coordination game structure");
		UtilityMethods.debugBDDMethods(bdd, "cpre coordination game structure ", cpreCGS);
		
//		UtilityMethods.getUserInput();
		
//		int incAct = BDDWrapper.assign(bdd, 0, actionVars);
//		
//		int transInc1 = UtilityTransitionRelations.increment(bdd, vars, 0, upperBound-1, 2, incAct);
//		
//		UtilityMethods.debugBDDMethods(bdd, "inc by 2", transInc1);
//		
//		int decAct = BDDWrapper.assign(bdd, 1, actionVars);
//		
//		int transDec1 = UtilityTransitionRelations.decrement(bdd, vars, 1, upperBound, 2, decAct);
//		
//		UtilityMethods.debugBDDMethods(bdd, "dec by 2", transDec1);
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
			System.out.println("affcted variables");
			Variable.printVariables(affectedVariablesDuringSystemTransitions.get(i));
			System.out.println();
			System.out.println("unaffected variables ");
			Variable.printVariables(unaffectedVariablesDuringSystemTransitions.get(i));
		}
		
		System.out.println();
		System.out.println("prnting environment part");
		for(int i=0; i<commands.length; i++){
			System.out.println();
			Integer[] envTrans = command_environmentTransitions.get(i);
			System.out.println("command "+i);
			for(int j=0; j<envTrans.length; j++){
				UtilityMethods.debugBDDMethods(bdd, "part "+j, envTrans[j]);
				if(j !=0 ){
					System.out.println("affcted variables");
					Variable.printVariables(affectedVariablesDuringEnvironmentTransitions.get(i).get(j-1));
					System.out.println("\nunaffected variables");
					Variable.printVariables(unaffectedVariablesDuringEnvironmentTransitions.get(i).get(j-1));
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
		if(set == 0){
			return set;
		}
		int cube = getVariablesAndActionsCube();
		
		//stutter
//		int Epost = bdd.ref(set);
		
		int allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
		int Epost = BDDWrapper.exists(bdd, set, successSignalsCube);
		Epost = BDDWrapper.andTo(bdd, Epost, allSuccessSignalsFalse);
		BDDWrapper.free(bdd, allSuccessSignalsFalse);
		
		if(player == Player.ENVIRONMENT){
			for(int i=0; i<command_environmentTransitions.size(); i++){
				Integer[] envTrans = command_environmentTransitions.get(i);
				ArrayList<Variable[]> affectedEnvVars = affectedVariablesDuringEnvironmentTransitions.get(i);
				for(int j=1; j<envTrans.length; j++){
					int trans = envTrans[j]; 
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
					
					int[] decomposedSet = decomposeSetOfStates(set, affectedEnvVars.get(j-1));
					int affectedCube = BDDWrapper.createCube(bdd, affectedEnvVars.get(j-1));
					for(int k=0; k<decomposedSet.length; k++){
						int post = BDDWrapper.EpostImage(bdd, decomposedSet[k], cube, trans, getVprimetoV());
						
						
//						cleanCoordinationGameStatePrintAndWait("current decompose set is", decomposedSet[j]);
//						cleanCoordinationGameStatePrintAndWait("post is", post);
						
						int intersect = BDDWrapper.and(bdd, decomposedSet[k], set);
						
						int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
						
//						cleanCoordinationGameStatePrintAndWait("same part is",samePart);
						
						BDDWrapper.free(bdd, intersect);
						
						post = BDDWrapper.andTo(bdd, post, samePart);
						BDDWrapper.free(bdd, samePart);
						
//						cleanCoordinationGameStatePrintAndWait("Epost of this trans is", post);
					
						Epost = BDDWrapper.orTo(bdd, Epost, post);
						BDDWrapper.free(bdd, post);		
					}
					BDDWrapper.free(bdd, affectedCube);
				}		
			}
		}else{
			for(int i=0; i<commands.length; i++){
				int trans = command_systemTransitions.get(i);
				int[] decomposedSet = decomposeSetOfStates(set, affectedVariablesDuringSystemTransitions.get(i));
				
				int affectedCube = BDDWrapper.createCube(bdd, affectedVariablesDuringSystemTransitions.get(i));
				//compute the EPre for the changing part & combine it with the unchanging part
				for(int j=0; j<decomposedSet.length; j++){
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "decomposedSet["+j+"]", decomposedSet[j]);
//					cleanCoordinationGameStatePrintAndWait("decomposedSet["+j+"]", decomposedSet[j]);
					
					int post = BDDWrapper.EpostImage(bdd, decomposedSet[j], cube, trans, getVprimetoV());
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "epre is", epre);
//					cleanCoordinationGameStatePrintAndWait("epre is", epre);
					
					int intersect = BDDWrapper.and(bdd, decomposedSet[j], set);
					
					int samePart = BDDWrapper.exists(bdd, intersect, affectedCube);
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "samePart is", samePart);
//					cleanCoordinationGameStatePrintAndWait("samePart is", samePart);
					
					BDDWrapper.free(bdd, intersect);
					
					post = BDDWrapper.andTo(bdd, post, samePart);
					
//					cleanCoordinationGameStatePrintAndWait("finally epre is", epre);
					
					BDDWrapper.free(bdd, samePart);
					Epost = BDDWrapper.orTo(bdd, Epost, post);
					BDDWrapper.free(bdd, post);
				}
				BDDWrapper.free(bdd, affectedCube);
			}
		}
		return Epost;
	}
	
	public int symbolicGameOneStepExecution(int set){
		int envMoves = EpostImage(set, Player.ENVIRONMENT);
		
//		cleanCoordinationGameStatePrintAndWait("env moves in symbolic step", envMoves);
		
		int sysMoves = EpostImage(envMoves, Player.SYSTEM);
		
//		cleanCoordinationGameStatePrintAndWait("sys moves in symbolic step ", sysMoves);
		
		return sysMoves;
	}
	
	
	
}