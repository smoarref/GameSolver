package coordination;

import game.BDDWrapper;
import game.Player;
import game.Variable;

import java.util.ArrayList;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

public class AbstractCoordinationGameStructure extends game.GameStructure {
	
	int numOfRobots;
	
	//the number of robots that are being used for the abstraction
	int thresholdNumOfRobots;
	
	Variable[] taskVars; 
	ArrayList<Variable[]> densityVars;
	Variable[] successSignals;
	Variable[] persistenceSignals;
	
	SimpleCommand[] commands;
	
	ArrayList<Integer> command_systemTransitions;
	ArrayList<Integer[]> command_environmentTransitions;
	
	private int systemStutterFomula=-1; 
	
	
	public AbstractCoordinationGameStructure(BDD argBdd) {
		super(argBdd);
	}
	
	public AbstractCoordinationGameStructure(BDD argBDD, int argNumOfRobots, 
			int argThresholdNumOfRobots, 
			SimpleCommand[] argCommands, 
			ArrayList<Variable[]> argDensityVars, Variable[] argTaskVars, 
			Variable[] argSuccessSignals, 
			Variable[] argPersistenceSignals){
		
		super(argBDD);
		
		numOfRobots = argNumOfRobots;
		
		thresholdNumOfRobots = argThresholdNumOfRobots;
		
		commands = argCommands;
		
		taskVars = argTaskVars;
		densityVars = argDensityVars;
		successSignals = argSuccessSignals;
		persistenceSignals = argPersistenceSignals;
		
		variables = Variable.unionVariables(densityVars);
		variables = Variable.unionVariables(variables, taskVars);
		variables = Variable.unionVariables(variables, successSignals); 
		variables = Variable.unionVariables(variables, persistenceSignals);
		
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
		
		computeCommandsTransitionFormulas();
		
	}
	
	private void computeCommandsTransitionFormulas(){
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
					
//			int act = BDDWrapper.assign(bdd, i, actionVars);
					
					//debugging
//					System.out.println("current command is c"+(i+1));
//					UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//					UtilityMethods.getUserInput();
					
			int commandFormula_sys = commandFormula_systemTransitions(i);
					
//					UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//					UtilityMethods.getUserInput();
					
			command_systemTransitions.add(commandFormula_sys);
					
			Integer[] commandFormula_env = commandFormula_environmentTransitions(i);
					
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
	
	private int computeTaskTransition(Variable[] affectedTaskVars, int taskPre, int taskPost){
		//task formula 
		int taskFormula = bdd.ref(bdd.getOne());
		if(affectedTaskVars != null){
			taskFormula = BDDWrapper.andTo(bdd, taskFormula, taskPre);
					
			Variable[] primedTaskVariables = Variable.getPrimedCopy(taskVars);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(taskVars), Variable.getBDDVars(primedTaskVariables));
			int taskPostPrime = bdd.ref(bdd.replace(taskPost, taskPerm));
					
			taskFormula = bdd.andTo(taskFormula, taskPostPrime);
			BDDWrapper.free(bdd, taskPostPrime);
				
			//unaffected tasks stay unchanged
			Variable[] unaffectedTasks  = Variable.difference(taskVars, affectedTaskVars);
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			taskFormula = bdd.andTo(taskFormula, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
					
		}else{
			int sameTaskState = BDDWrapper.same(bdd, taskVars);
			taskFormula = BDDWrapper.andTo(bdd, taskFormula, sameTaskState);
		}
		return taskFormula;
	}
	
	private int computeDensityTransition(int numOfRequiredRobots, Variable[] commandDensityVars, Variable[] commandNextDensityVars){
		int densityFormula = bdd.ref(bdd.getOne());
		
		//d_\alpha >= numOfRequiredRobots
		int lowerBound = densityLowerBoundCondition(bdd, commandDensityVars, numOfRequiredRobots, thresholdNumOfRobots);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, lowerBound);
		BDDWrapper.free(bdd, lowerBound);
		
		
		//d_\beta' = d_\beta+n
		int beta = bdd.ref(bdd.getZero());
		for(int i=0; i<=thresholdNumOfRobots; i++){
			int tmp = BDDWrapper.assign(bdd, i, commandNextDensityVars);
			if(i<= thresholdNumOfRobots-numOfRequiredRobots-1){
				 int betaPrime = BDDWrapper.assign(bdd, i+numOfRequiredRobots, Variable.getPrimedCopy(commandNextDensityVars));
				 tmp = BDDWrapper.andTo(bdd, tmp, betaPrime);
				 BDDWrapper.free(bdd, betaPrime);
			}else{
				int betaPrime = BDDWrapper.assign(bdd, thresholdNumOfRobots, Variable.getPrimedCopy(commandNextDensityVars));
				 tmp = BDDWrapper.andTo(bdd, tmp, betaPrime);
				 BDDWrapper.free(bdd, betaPrime);
			}
			beta = BDDWrapper.orTo(bdd, beta, tmp);
			BDDWrapper.free(bdd, tmp);
		}
		
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, beta);
		BDDWrapper.free(bdd, beta);
		
		//d_\alpha' = d_\alpha-n
		int alpha = bdd.ref(bdd.getZero());
		for(int i=numOfRequiredRobots; i<=thresholdNumOfRobots; i++){
			int tmp = BDDWrapper.assign(bdd, i, commandNextDensityVars);
			if(i<= thresholdNumOfRobots-1){
				 int alphaPrime = BDDWrapper.assign(bdd, i-numOfRequiredRobots, Variable.getPrimedCopy(commandDensityVars));
				 tmp = BDDWrapper.andTo(bdd, tmp, alphaPrime);
				 BDDWrapper.free(bdd, alphaPrime);
			}else{
				int alphaPrime = bdd.ref(bdd.getZero());
				for(int j=thresholdNumOfRobots-numOfRequiredRobots; j<thresholdNumOfRobots; j++){
					int tmp2 = BDDWrapper.assign(bdd, j, Variable.getPrimedCopy(commandDensityVars));
					alphaPrime = BDDWrapper.orTo(bdd, alphaPrime, tmp2);
					BDDWrapper.free(bdd, tmp2);
				}
				 tmp = BDDWrapper.andTo(bdd, tmp, alphaPrime);
				 BDDWrapper.free(bdd, alphaPrime);
			}
			alpha = BDDWrapper.orTo(bdd, alpha, tmp);
			BDDWrapper.free(bdd, tmp);
		}
		
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, alpha);
		BDDWrapper.free(bdd, alpha);
		
		//uaffected density vars remain unchanged
		Variable[] affectedDensityVars = Variable.unionVariables(commandDensityVars, commandNextDensityVars);	
		Variable[] unaffectedDensityVars = Variable.difference(Variable.unionVariables(densityVars), affectedDensityVars);
		int sameUnaffected = BDDWrapper.same(bdd, unaffectedDensityVars);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, sameUnaffected);
		BDDWrapper.free(bdd, sameUnaffected);
		
		return densityFormula;
		
		
	}
	
	private int setNextSignalsFalseButOne( Variable[] signals, int index){
		int nextSignals = bdd.ref(bdd.getOne());
		for(int i=0; i<signals.length; i++){
			Variable signalPrime = signals[i].getPrimedCopy();
			if(i== index){
				nextSignals = BDDWrapper.andTo(bdd, nextSignals, signalPrime.getBDDVar());
			}else{
				int negSignal = bdd.not(signalPrime.getBDDVar());
				nextSignals = BDDWrapper.andTo(bdd, nextSignals, negSignal);
			}
		}
		return nextSignals;
	}
	
	
	private int commandFormula_systemTransitions(int index){
		
		int commandFormula = bdd.ref(bdd.getOne());
		
//		Variable commandSuccessSignalPrime = commandSuccessSignal.getPrimedCopy();
//		int successSignal = bdd.ref(bdd.not(commandSuccessSignalPrime.getBDDVar()));
		
//		int sameSuccessSignal = BDDWrapper.same(bdd, commandSuccessSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
		int sameSuccessSignal = BDDWrapper.same(bdd, successSignals);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
		//persistence 
		int setPersistence = setNextSignalsFalseButOne(persistenceSignals, index);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, setPersistence);
		BDDWrapper.free(bdd, setPersistence);
		
//		int falseNextSuccessSignal = allFalseNextSuccessSignals(bdd, successSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, falseNextSuccessSignal);
		
		SimpleCommand command = commands[index];
		
		//task formula 
//		if(command.getAffectedTasks() != null){
//			int taskFormula = bdd.ref(command.getInitTasksCondition());
//			
//			int taskIntermediate = command.getIntermediateTasksCondition();
//			Variable[] primedTaskVariables = Variable.getPrimedCopy(taskVars);
//			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(taskVars), Variable.getBDDVars(primedTaskVariables));
//			int taskIntermediatePrime = bdd.ref(bdd.replace(taskIntermediate, taskPerm));
//			
//			taskFormula = bdd.andTo(taskFormula, taskIntermediatePrime);
//			BDDWrapper.free(bdd, taskIntermediatePrime);
//		
//			//unaffected tasks stay unchanged
//			Variable[] unaffectedTasks  = Variable.difference(taskVars, command.getAffectedTasks());
//			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
//			taskFormula = bdd.andTo(taskFormula, sameUnaffectedTasks);
//			BDDWrapper.free(bdd, sameUnaffectedTasks);
//			
//			commandFormula = bdd.andTo(commandFormula, taskFormula);
//			BDDWrapper.free(bdd, taskFormula);
//		}else{
//			int sameTaskState = BDDWrapper.same(bdd, taskVars);
//			commandFormula = bdd.andTo(commandFormula, sameTaskState);
//			BDDWrapper.free(bdd, sameTaskState);
//		}
		
		int taskFormula = computeTaskTransition(command.getAffectedTasks(), command.getInitTasksCondition(), command.getIntermediateTasksCondition());
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, taskFormula);
		BDDWrapper.free(bdd,taskFormula);
		
		//density part 
		int numOfRequiredRobots = command.getNumberOfRequiredRobots();
		Variable[] commandDensityVars = command.getInitDensity();
		Variable[] commandNextDensityVars = command.getIntermediateDensity();
		
		
//		//create density formula
//		int densityFormula = bdd.ref(bdd.getOne());
//				
//		//d_\alpha >= numOfRequiredRobots
//		int lowerBound = densityLowerBoundCondition(bdd, commandDensityVars, command.getNumberOfRequiredRobots(), thresholdNumOfRobots);
//		densityFormula = BDDWrapper.andTo(bdd, densityFormula, lowerBound);
//		BDDWrapper.free(bdd, lowerBound);
//		
//		
//		//d_\beta' = d_\beta+n
//		int beta = bdd.ref(bdd.getZero());
//		for(int i=0; i<=thresholdNumOfRobots; i++){
//			int tmp = BDDWrapper.assign(bdd, i, commandNextDensityVars);
//			if(i<= thresholdNumOfRobots-numOfRequiredRobots-1){
//				 int betaPrime = BDDWrapper.assign(bdd, i+numOfRequiredRobots, Variable.getPrimedCopy(commandNextDensityVars));
//				 tmp = BDDWrapper.andTo(bdd, tmp, betaPrime);
//				 BDDWrapper.free(bdd, betaPrime);
//			}else{
//				int betaPrime = BDDWrapper.assign(bdd, thresholdNumOfRobots, Variable.getPrimedCopy(commandNextDensityVars));
//				 tmp = BDDWrapper.andTo(bdd, tmp, betaPrime);
//				 BDDWrapper.free(bdd, betaPrime);
//			}
//			beta = BDDWrapper.orTo(bdd, beta, tmp);
//			BDDWrapper.free(bdd, tmp);
//		}
//		
//		//d_\alpha' = d_\alpha-n
//		int alpha = bdd.ref(bdd.getZero());
//		for(int i=numOfRequiredRobots; i<=thresholdNumOfRobots; i++){
//			int tmp = BDDWrapper.assign(bdd, i, commandNextDensityVars);
//			if(i<= thresholdNumOfRobots-1){
//				 int alphaPrime = BDDWrapper.assign(bdd, i-numOfRequiredRobots, Variable.getPrimedCopy(commandDensityVars));
//				 tmp = BDDWrapper.andTo(bdd, tmp, alphaPrime);
//				 BDDWrapper.free(bdd, alphaPrime);
//			}else{
//				int alphaPrime = bdd.ref(bdd.getZero());
//				for(int j=thresholdNumOfRobots-numOfRequiredRobots; j<thresholdNumOfRobots; j++){
//					int tmp2 = BDDWrapper.assign(bdd, j, Variable.getPrimedCopy(commandDensityVars));
//					alphaPrime = BDDWrapper.orTo(bdd, alphaPrime, tmp2);
//					BDDWrapper.free(bdd, tmp2);
//				}
//				 tmp = BDDWrapper.andTo(bdd, tmp, alphaPrime);
//				 BDDWrapper.free(bdd, alphaPrime);
//			}
//			alpha = BDDWrapper.orTo(bdd, alpha, tmp);
//			BDDWrapper.free(bdd, tmp);
//		}
//		
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, lowerBound);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, alpha);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, beta);
//		BDDWrapper.free(bdd, lowerBound);
//		BDDWrapper.free(bdd, alpha);
//		BDDWrapper.free(bdd, beta);
//		
//		
//		//uaffected density vars remain unchanged
//		Variable[] affectedDensityVars = Variable.unionVariables(commandDensityVars, commandNextDensityVars);	
//		Variable[] unaffectedDensityVars = Variable.difference(Variable.unionVariables(densityVars), affectedDensityVars);
//		int sameUnaffected = BDDWrapper.same(bdd, unaffectedDensityVars);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameUnaffected);
		
		int densityFormula = computeDensityTransition(numOfRequiredRobots, commandDensityVars, commandNextDensityVars);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, densityFormula);
		BDDWrapper.free(bdd, densityFormula);
		
		
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
	
		
		return commandFormula;
	}
	
	private int allFalseNextSignals( Variable[] signals){
		boolean[] allFalse = UtilityMethods.allFalseArray(signals.length);
		int falseNextSuccessSignals = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(signals));
		return falseNextSuccessSignals;
	}
	
	/**
	 * TODO: remove the stutter from all commands and just add it once
	 * TODO: can we remove action vars?
	 * stutter - success - failure, respectively
	 * @param index
	 * @param commandAction
	 * @return
	 */
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
		
		//success signals
		int allNextSuccessSignalsOFF = allFalseNextSignals(successSignals);
		int allButOneSuccessSignalsOFF = setNextSignalsFalseButOne(successSignals, index);
		
		stutter = BDDWrapper.andTo(bdd, stutter, allNextSuccessSignalsOFF);
		success = BDDWrapper.andTo(bdd, success, allButOneSuccessSignalsOFF);
		fail = BDDWrapper.andTo(bdd, fail, allNextSuccessSignalsOFF);
		
		BDDWrapper.free(bdd, allNextSuccessSignalsOFF);
		BDDWrapper.free(bdd, allButOneSuccessSignalsOFF);
		
		//persistence signals
		int samePersistence = BDDWrapper.same(bdd, persistenceSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, samePersistence);
		success = BDDWrapper.andTo(bdd, success, samePersistence);
		fail = BDDWrapper.andTo(bdd, fail, samePersistence);
		BDDWrapper.free(bdd, samePersistence);
		
		SimpleCommand command = commands[index];
		
		//Task formula 
		int sameTaskState = BDDWrapper.same(bdd, taskVars);
		stutter = bdd.andTo(stutter, sameTaskState);
		BDDWrapper.free(bdd, sameTaskState);
		
		int successTaskFormula = computeTaskTransition(command.getAffectedTasks(), command.getIntermediateTasksCondition(), command.getFinalSuccessTaskCondition());
		success = BDDWrapper.andTo(bdd, success, successTaskFormula);
		BDDWrapper.free(bdd, successTaskFormula);
		
		int failureTaskFormula = computeTaskTransition(command.getAffectedTasks(), command.getIntermediateTasksCondition(), command.getFinalFailureTaskCondition());
		fail = BDDWrapper.andTo(bdd, fail, failureTaskFormula);
		BDDWrapper.free(bdd, fail);
		
//		//task formula 
//		if(command.getAffectedTasks() != null){
//			
//			int sameTaskState = BDDWrapper.same(bdd, taskVars);
//			stutter = bdd.andTo(stutter, sameTaskState);
//			BDDWrapper.free(bdd, sameTaskState);
//			
//			int taskFormula = command.getIntermediateTasksCondition();
//			
//			Variable[] primedTaskVariables = Variable.getPrimedCopy(taskVars);
//			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(taskVars), Variable.getBDDVars(primedTaskVariables));
//			
//			int taskSuccess = command.getFinalSuccessTaskCondition();
//			int taskSuccessPrime = bdd.ref(bdd.replace(taskSuccess, taskPerm));
//			
//			int taskFail = command.getFinalFailureTaskCondition();
//			int taskFailPrime = bdd.ref(bdd.replace(taskFail, taskPerm));
//			
//			success = bdd.andTo(success, taskFormula);
//			success = bdd.andTo(success, taskSuccessPrime);
//			
//			fail = bdd.andTo(fail, taskFormula);
//			fail = bdd.andTo(fail, taskFailPrime);
//			
//			BDDWrapper.free(bdd, taskSuccessPrime);
//			BDDWrapper.free(bdd, taskFailPrime);
//		
//			//unaffected tasks stay unchanged
//			Variable[] unaffectedTasks  = Variable.difference(taskVars, command.getAffectedTasks());
//			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
//			success = bdd.andTo(success, sameUnaffectedTasks);
//			fail = bdd.andTo(fail, sameUnaffectedTasks);
//			BDDWrapper.free(bdd, sameUnaffectedTasks);
//			
////			commandFormula = bdd.andTo(commandFormula, taskFormula);
////			BDDWrapper.free(bdd, taskFormula);
//		}else{
//			int sameTaskState = BDDWrapper.same(bdd, taskVars);
//			
//			stutter = bdd.andTo(stutter, sameTaskState);
//			success = bdd.andTo(success, sameTaskState);
//			fail = bdd.andTo(fail, sameTaskState);
//			
//			BDDWrapper.free(bdd, sameTaskState);
//		}
		
		//density formula 
		Variable[] allDensityVars = Variable.unionVariables(densityVars);
		int sameDensity = BDDWrapper.same(bdd, allDensityVars);
		stutter = BDDWrapper.andTo(bdd, stutter, sameDensity);
		BDDWrapper.free(bdd, sameDensity);
		
		
		
//		//density part 
//		int[] densityVector = command.getIntermediateDensity();
//		int[] successDensityVector = command.getFinalSuccessDensity();
//		int[] failureDensityVector = command.getFinalFailureDensity();
//		
//				
//		//create density formula
//		int densityFormula = bdd.ref(bdd.getOne());
//		
//		Variable[] allDensityVars = Variable.unionVariables(densityVars);
//		int sameDensity = BDDWrapper.same(bdd, allDensityVars);
//		stutter = BDDWrapper.andTo(bdd, stutter, sameDensity);
//		BDDWrapper.free(bdd, sameDensity);
//		
////		UtilityMethods.debugBDDMethods(bdd, "command env stutter",stutter);
//		
//						
//		//x_0 >= d_0 & ... & x_k >= d_k
//		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfRobots);
//		densityFormula = BDDWrapper.and(bdd, densityFormula, lowerBound);
//		BDDWrapper.free(bdd, lowerBound);
//				
//		//x'_i = x_i +d'_i - d_i
//		int nextSuccessDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, successDensityVector);
//		int successDensityFormula = BDDWrapper.and(bdd, densityFormula, nextSuccessDensityFormula);
//		BDDWrapper.free(bdd, nextSuccessDensityFormula);
//		
//		success = BDDWrapper.andTo(bdd, success, successDensityFormula);
//		BDDWrapper.free(bdd, successDensityFormula);
//		
//		//x'_i = x_i +d'_i - d_i
//		int nextFailureDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, failureDensityVector);
//		int failureDensityFormula = BDDWrapper.andTo(bdd, densityFormula, nextFailureDensityFormula);
//		BDDWrapper.free(bdd, nextFailureDensityFormula);
//		
//		fail = BDDWrapper.andTo(bdd, fail, failureDensityFormula);
//		BDDWrapper.free(bdd, failureDensityFormula);
//		
//		BDDWrapper.free(bdd, densityFormula);
		
		int successDensityFormula = computeDensityTransition(command.getNumberOfRequiredRobots(), command.intermediateDensity, command.getFinalSuccessDensity());
		success = BDDWrapper.andTo(bdd, success, successDensityFormula);
		BDDWrapper.free(bdd, successDensityFormula);
		
		int failureDensityFormula = computeDensityTransition(command.getNumberOfRequiredRobots(), command.getIntermediateDensity(), command.getFinalFailureDensity());
		fail = BDDWrapper.andTo(bdd, fail, failureDensityFormula);
		BDDWrapper.free(bdd, failureDensityFormula);
		
		commandFormula[0] = stutter;
		commandFormula[1] = success;
		commandFormula[2] = fail;
		
		return commandFormula;
		
		
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
			
//			int epre = BDDWrapper.EpreImage(bdd, set, cube, trans, getVtoVprime());
			
			int epre = BDDWrapper.ApreImage_EPreBased(bdd, set, cube, trans, getVtoVprime());
			
			controllableEpre = BDDWrapper.orTo(bdd, controllableEpre, epre);
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd, "current system trans is ", trans);
		}
		
//		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
//		int tmp = BDDWrapper.exists(bdd, controllableEpre, successSignalsCube);
//		BDDWrapper.free(bdd, controllableEpre);
//		controllableEpre = tmp;
//		BDDWrapper.free(bdd, successSignalsCube);
		
//		cleanCoordinationGameStatePrintAndWait("ControlleableEpre is", controllableEpre);
		
		
		
		int controllablePredecessor = bdd.ref(bdd.getOne());
		
		//stutter with false next signals
		int allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
		int part = BDDWrapper.and(bdd, controllableEpre, allSuccessSignalsFalse);
		int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
		int controllableStatesWithCommandStart = BDDWrapper.exists(bdd, part, successSignalsCube);
		BDDWrapper.free(bdd, allSuccessSignalsFalse);
		BDDWrapper.free(bdd, part);
		
		for(int i=0; i<commands.length; i++){
//			System.out.println("computing controllable predecessor, environment part, for the command "+i);
			
			Integer[] envTransitions = command_environmentTransitions.get(i);
			
			int controllableStatesWithCommand = bdd.ref(controllableStatesWithCommandStart);
			
//			cleanCoordinationGameStatePrintAndWait("stutter part", controllableStatesWithCommand);
			
//			int controllableStatesWithCommand = bdd.ref(controllableEpre);
			
			
			for(int j=0; j<envTransitions.length; j++){
				int trans = envTransitions[j];
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
				
				//stutter
				if(j==0){
					continue;
				}
				
				//TODO: we are not considering failure for now
				if(j==2){
					continue;
				}
				
//				int epre = BDDWrapper.EpreImage(bdd, controllableEpre, cube, trans, getVtoVprime());
				
				int epre = BDDWrapper.ApreImage_EPreBased(bdd, controllableEpre, cube, trans, getVtoVprime());
				
				int commandAvailable = BDDWrapper.exists(bdd, trans, cube);
				int commandNotAvailable = BDDWrapper.not(bdd, commandAvailable);
				BDDWrapper.free(bdd, commandAvailable);
				epre = BDDWrapper.orTo(bdd, epre, commandNotAvailable);
				BDDWrapper.free(bdd, commandNotAvailable);
				
				controllableStatesWithCommand = BDDWrapper.andTo(bdd, controllableStatesWithCommand, epre);
				
				BDDWrapper.free(bdd, epre);
			}
			
//			cleanCoordinationGameStatePrintAndWait("controllable states with commands are", controllableStatesWithCommand);
			
			controllablePredecessor = BDDWrapper.andTo(bdd, controllablePredecessor, controllableStatesWithCommand);
			BDDWrapper.free(bdd, controllableStatesWithCommand);
		}	
			
		
//		cleanCoordinationGameStatePrintAndWait("cpre of set is", controllablePredecessor);
		return controllablePredecessor;
	}
	
	public int EpostImage(int set, Player player){
//		System.out.println("Running epost in compositional");
//		long t0=UtilityMethods.timeStamp();
		
		if(set == 0){
			return set;
		}
		int cube = getVariablesAndActionsCube();
		
		//stutter
		int Epost = bdd.ref(set);
		
		
		
		if(player == Player.ENVIRONMENT){
			int allSuccessSignalsFalse = UtilityMethods.allFalse(bdd, successSignals);
			int successSignalsCube = BDDWrapper.createCube(bdd, successSignals);
			Epost = BDDWrapper.exists(bdd, set, successSignalsCube);
			Epost = BDDWrapper.andTo(bdd, Epost, allSuccessSignalsFalse);
			BDDWrapper.free(bdd, allSuccessSignalsFalse);
			
			for(int i=0; i<command_environmentTransitions.size(); i++){
				Integer[] envTrans = command_environmentTransitions.get(i);
				for(int j=1; j<envTrans.length; j++){
					
					//TODO: we are ignoring failure for now. 
					if(j==2){
						continue;
					}
					
					int trans = envTrans[j]; 
					int post = BDDWrapper.EpostImage(bdd, set, cube, trans, getVprimetoV());
					Epost = BDDWrapper.orTo(bdd, Epost, post);
					BDDWrapper.free(bdd, post);	
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd, "current env trans is ", trans);
				}		
			}
		}else{
			for(int i=0; i<commands.length; i++){
				int trans = command_systemTransitions.get(i);
				int post = BDDWrapper.EpostImage(bdd, set, cube, trans, getVprimetoV());
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
	
	//clean print out
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
			String successSignalsValues = minterm.substring(index, index+successSignals.length);
			index+= successSignals.length;
			System.out.print(successSignalsValues);
			String persistenceSignalsValues = minterm.substring(index);
			System.out.println("persistence signals: ");
			System.out.println(persistenceSignalsValues);
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

}
