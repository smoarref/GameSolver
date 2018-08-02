package coordination;

import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Variable;

import java.util.ArrayList;

import solver.GR1Objective;
import solver.GR1Solver;
import solver.GR1WinningStates;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

public class TestSimpleCoordination {
	
	public static void main(String[] args) {

		simpleTwoRoomsTest2(16,4,4);

//		testSimpleCoordinationGameCompositional_twoRooms(32, 8, 8);
		
//		simpleTwoRoomsTest_coordinationGame(64,4,4);
		
//		testPaperExample(2, 1,1);
		
//		simpleTwoRoomsTest();
		
//		testPaperExample();
		
//		testPaperExample(4);
		
//		testPaperExampleWithCoordinationGameStructure(8);
		
		
		//trying different variable ordering
//		testPaperExample_DifferentVariableOrdering(2, 1, 1);
		
		testPaperExample_DifferentVariableOrdering2(2, 1, 1);
		
//		testPaperExample_coordiantionGameStructure(2, 1, 1);
		
//		simpleTwoRoomsTest_noSuccessSignals(32, 1, 1);
		

	}
	
	public static void playCoordinationGameRandomly(BDD bdd, GameStructure coordinationGameStructure, SimpleCommandVector[] commands, 
			ArrayList<Variable[]> densityVars, ArrayList<String> densityVarsNames, Variable[] taskVars, Variable[] successSignals){
		
		Variable[] vars = coordinationGameStructure.variables;
		int init = coordinationGameStructure.getInit();
		
	
		
		
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, init, vars);
//		int randomIndex = UtilityMethods.randomIndex(minterms.size());
//		String chosenMinterm = minterms.get(randomIndex);
//		int currentState = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		
		int currentState = chooseStateRandomly(bdd, init, vars);
		
		while(true){
			System.out.println("current signal is");
			
//			cleanPrintSetOutWithDontCares(bdd, currentState, densityVars, taskVars, successSignals);
			
			parseCoordinationStateFormula(bdd, currentState, densityVars, densityVarsNames, taskVars);
			
			UtilityMethods.getUserInput();
			
			int possibleNextStates = coordinationGameStructure.symbolicGameOneStepExecution(currentState);
			
			currentState = chooseStateRandomly(bdd, possibleNextStates, vars);
//			cleanPrintSetOutWithDontCares(bdd, currentState, densityVars, taskVars, successSignals);
			
		}
		
		
		
	}
	
	public static void playCoordinationGameRandomly(BDD bdd, GameStructure coordinationGameStructure, SimpleCommandVector[] commands, 
			ArrayList<Variable[]> densityVars, ArrayList<String> densityVarsNames, Variable[] taskVars){
		
		Variable[] vars = coordinationGameStructure.variables;
		int init = coordinationGameStructure.getInit();
		
	
		
		
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, init, vars);
//		int randomIndex = UtilityMethods.randomIndex(minterms.size());
//		String chosenMinterm = minterms.get(randomIndex);
//		int currentState = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		
		int currentState = chooseStateRandomly(bdd, init, vars);
		
		while(true){
			System.out.println("current signal is");
			
//			cleanPrintSetOutWithDontCares(bdd, currentState, densityVars, taskVars, successSignals);
			
			parseCoordinationStateFormula(bdd, currentState, densityVars, densityVarsNames, taskVars);
			
			UtilityMethods.getUserInput();
			
			int possibleNextStates = coordinationGameStructure.symbolicGameOneStepExecution(currentState);
			
			currentState = chooseStateRandomly(bdd, possibleNextStates, vars);
//			cleanPrintSetOutWithDontCares(bdd, currentState, densityVars, taskVars, successSignals);
			
		}
		
		
		
	}
	
	public static int chooseStateRandomly(BDD bdd, int states, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, states, vars);
		int randomIndex = UtilityMethods.randomIndex(minterms.size());
		String chosenMinterm = minterms.get(randomIndex);
		int state = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		return state;
		
	}

	public static int excactlyN_Agent(BDD bdd, ArrayList<Variable[]> densityVars, int N){
		return exactlyN_Agent_rec(bdd, densityVars, N, 0);
	}
	
	public static int exactlyN_Agent_rec(BDD bdd, ArrayList<Variable[]> densityVars, int N, int index){
		int result = bdd.ref(bdd.getZero());
		if(index==densityVars.size()-1){
			result = BDDWrapper.assign(bdd, N, densityVars.get(index));
		}else{
			for(int i=0; i<=N; i++){
				int current = BDDWrapper.assign(bdd, i, densityVars.get(index));
				int rest = exactlyN_Agent_rec(bdd, densityVars, N-i, index+1);
				current = BDDWrapper.andTo(bdd, current, rest);
				BDDWrapper.free(bdd, rest);
				result = BDDWrapper.orTo(bdd, result, current);
				BDDWrapper.free(bdd, current);
			}
		}
		return result;
	}
	
	public static void parseCoordinationStateFormula(BDD bdd, int formula, ArrayList<Variable[]> densityVariables, ArrayList<String> densityVarsNames, 
			Variable[] taskVariables){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		Variable[] variables = Variable.unionVariables(densityVariables);
		variables = Variable.unionVariables(variables, taskVariables);
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula, variables);
		for(String minterm : minterms ){
			System.out.println("State");
//			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			for(int i=0; i<densityVariables.size(); i++){
				String value = minterm.substring(index, index+densityVariables.get(i).length);
				index+=densityVariables.get(i).length;
				System.out.println(densityVarsNames.get(i)+" = "+Integer.valueOf(value, 2));
			}
			//tasks
			String value = minterm.substring(index, index+taskVariables.length);
			index+= taskVariables.length;
			for(int i=0; i<taskVariables.length; i++){
				System.out.println("task"+i +" = "+value.charAt(i));
			}
			
			System.out.println();
		}
	}
	
	public static void parseCoordinationStateFormula(BDD bdd, int formula, ArrayList<Variable[]> densityVariables, ArrayList<String> densityVarsNames, 
			Variable[] taskVariables, 
			Variable[] successSignals){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		Variable[] variables = Variable.unionVariables(densityVariables);
		variables = Variable.unionVariables(variables, taskVariables);
		variables = Variable.unionVariables(variables, successSignals);
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula, variables);
		for(String minterm : minterms ){
			System.out.println("State");
			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			for(int i=0; i<densityVariables.size(); i++){
				String value = minterm.substring(index, index+densityVariables.get(i).length);
				index+=densityVariables.get(i).length;
				System.out.println(densityVarsNames.get(i)+" = "+Integer.valueOf(value, 2));
			}
			//tasks
			String value = minterm.substring(index, index+taskVariables.length);
			index+= taskVariables.length;
			for(int i=0; i<taskVariables.length; i++){
				System.out.println("task"+i +" = "+value.charAt(i));
			}
			
			
			String successSignalsValues = minterm.substring(index);
			for(int i=0; i<successSignals.length; i++){
				System.out.println("successSignal"+i+" = "+successSignalsValues.charAt(i));
			}
			System.out.println();
		}
	}
	
	public static void cleanPrintSetOut(BDD bdd, int formula, ArrayList<Variable[]> densityVariables, ArrayList<String> densityVarsNames, 
			Variable[] taskVariables, 
			Variable[] successSignals){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		Variable[] variables = Variable.unionVariables(densityVariables);
		variables = Variable.unionVariables(variables, taskVariables);
		variables = Variable.unionVariables(variables, successSignals);
		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula, variables);
		for(String minterm : minterms ){
			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			System.out.print("densityVars: ");
			for(int i=0; i<densityVariables.size(); i++){
				String value = minterm.substring(index, index+densityVariables.get(i).length);
				index+=densityVariables.get(i).length;
				System.out.print(value+" ");
			}
			//tasks
			System.out.print("Tasks: ");
			String value = minterm.substring(index, index+taskVariables.length);
			index+= taskVariables.length;
			System.out.print(value);
			System.out.print(" Signals: ");
			String successSignalsValues = minterm.substring(index);
			System.out.print(successSignalsValues);
			System.out.println();
		}
	}
	
	public static void cleanPrintSetOutWithDontCares(BDD bdd, int formula, ArrayList<Variable[]> densityVariables, 
			Variable[] taskVariables, 
			Variable[] successSignals){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
//		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		Variable[] variables = Variable.unionVariables(densityVariables);
		variables = Variable.unionVariables(variables, taskVariables);
		variables = Variable.unionVariables(variables, successSignals);
		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, formula, variables);
		for(String minterm : minterms ){
//			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			System.out.print("densityVars: ");
			for(int i=0; i<densityVariables.size(); i++){
				String value = minterm.substring(index, index+densityVariables.get(i).length);
				index+=densityVariables.get(i).length;
				System.out.print(value+" ");
			}
			//tasks
			System.out.print("Tasks: ");
			String value = minterm.substring(index, index+taskVariables.length);
			index+= taskVariables.length;
			System.out.print(value);
			System.out.print(" Signals: ");
			String successSignalsValues = minterm.substring(index);
			System.out.print(successSignalsValues);
			System.out.println();
		}
	}
	
	public static void cleanPrintSetOutWithDontCares(BDD bdd, int formula, ArrayList<Variable[]> densityVariables, 
			Variable[] taskVariables){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
//		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		Variable[] variables = Variable.unionVariables(densityVariables);
		variables = Variable.unionVariables(variables, taskVariables);
		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, formula, variables);
		for(String minterm : minterms ){
//			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			System.out.print("densityVars: ");
			for(int i=0; i<densityVariables.size(); i++){
				String value = minterm.substring(index, index+densityVariables.get(i).length);
				index+=densityVariables.get(i).length;
				System.out.print(value+" ");
			}
			//tasks
			System.out.print("Tasks: ");
			String value = minterm.substring(index, index+taskVariables.length);
			index+= taskVariables.length;
			System.out.print(value);
			System.out.println();
		}
	}
	
	
	public static void testSimpleCoordinationGameCompositional_twoRooms(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots =1; 
		
		
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 1;
		Variable[] tasks = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfTasks, "Task");
		
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 2, "successSignal_");
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r2
		//c2 : executes task one in r1 with two robots
				
		//defining buckets: ready_r1, busy_c2_r1, busy_c1_r1 , ready_r2 
		int densityVectorLength = 4; 
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		String[] densityNames = new String[]{"ready_r1", "busy_c2_r1", "busy_c1_r1", "ready_r2" };
		ArrayList<String> densityVarNames = new ArrayList<String>();
		for(int i=0; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}

		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0};
		int[] c1_finalSuccessDensity = {0,0,0,numOfMovingRobots};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
						
				
		//c2 : executes task one in r1 with two robots
		int[] c2_initDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_intermediateDensity = {0,numOfRequiredRobotsForTask,0,0};
		int[] c2_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0};
						
		Variable[] c2_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c2_taskInit = bdd.ref(bdd.not(task0));
		int c2_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c2_tasksFinal_success = bdd.ref(task0);
		int c2_tasksFinal_failure = bdd.ref(bdd.not(task0));
						
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_affectedTasks, c2_taskInit, c2_initDensity, c2_tasksIntermediate, c2_intermediateDensity, c2_tasksFinal_success, c2_finalSuccessDensity, c2_tasksFinal_failure, c2_finalFailureDensity);
						
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2};
		
		t0 = UtilityMethods.timeStamp();
		
		CoordinationGameStructure cgs = new CoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
		cgs.printGameVars();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+cgs.actionVars.length);
		int total = 2*cgs.variables.length+cgs.actionVars.length;
		System.out.println("over all "+total+" of variables in system");
		
		//test the controllable predecessor operator
		//define a set of states 
		
//		cgs.printCommandsInfo();
//		UtilityMethods.getUserInput();
		
		
		boolean[] allFalseTasks = UtilityMethods.allFalseArray(tasks.length);
		int setTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		
//		int phi = cgs.assignValueToDensityVars(new int[]{0,1,0,0});
//		phi = BDDWrapper.andTo(bdd, phi, setTasksToFalse);
//		
//		UtilityMethods.debugBDDMethods(bdd, "phi", phi);
//		UtilityMethods.getUserInput();
//		
//		int cpre = cgs.controllablePredecessor(phi);
//		UtilityMethods.debugBDDMethods(bdd, "cpre of phi is", cpre);
//		UtilityMethods.getUserInput();
//		
//		
//		int phi2 = cgs.assignValueToDensityVars(new int[]{0,0,0,1});
//		phi2 = BDDWrapper.orTo(bdd, phi2, phi);
//		
//		cgs.cleanCoordinationGameStatePrintAndWait("phi2", phi2);
//		int cpre2 = cgs.controllablePredecessor(phi2);
//		
//		cgs.cleanCoordinationGameStatePrintAndWait("cpre phi2 ", cpre2);
		
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int r3_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int r4_init = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		init = bdd.andTo(init, r3_init);
		init = bdd.andTo(init, r4_init);
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		bdd.deref(r3_init);
		bdd.deref(r4_init);
				
		
		init = BDDWrapper.andTo(bdd, init, setTasksToFalse);
		BDDWrapper.free(bdd, setTasksToFalse);
		
////		cgs.cleanCoordinationGameStatePrintAndWait("init is", init);
//		
////		System.out.println("testing the symbolic one step execution");
//		int oneStep = cgs.symbolicGameOneStepExecution(init);
//		
////		cgs.cleanCoordinationGameStatePrintAndWait(oneStep);
//		
//		System.out.println("second step");
//		int secondStep = cgs.symbolicGameOneStepExecution(oneStep);
//		
//		cgs.cleanCoordinationGameStatePrintAndWait(secondStep);
		
		
		
		
		
		
				
				
		//**** Define the objective ****/
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
				
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, busy_c2_r1, busy_c1_r1, ready_r2, ,  
		//c1 : moves two robots from r1 to r2
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : executes task one in r1 with two robots
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
				
				
//				System.out.println("Assumtpions");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
				
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
				
		//always eventually all robots are in room r2
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(3));
		guarantees.add(reachFinalRoom);
				
//				System.out.println("Guarantees");
//				for(int i=0; i<guarantees.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, guarantees.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		//**** Solve the game ***/

				
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), cgs, init, objective);
		
//		cgs.cleanCoordinationGameStatePrintAndWait("winning states are", w.getWinningStates());
		
		GameSolution sol = GR1Solver.solve(bdd, cgs, init, objective);
		
		UtilityMethods.duration(t0, "game solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
				
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
//		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
		
		UtilityMethods.memoryUsage();
		
//		playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
		
		CoordinationStrategy strategy = (CoordinationStrategy) sol.strategyOfTheWinner();
		
		strategy.playCoordinationGameRandomly(init);
	}
	
	public static void simpleTwoRoomsTest_noSuccessSignals(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots =1; 
		
		
		
		//Tasks are represented using a set of boolean variables, one for each task
				int numOfTasks = 1;
				Variable[] tasks = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfTasks, "Task");
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r2
		//c2 : executes task one in r1 with two robots
				
		//defining buckets: ready_r1, busy_c1_r1, busy_c2_r1 , ready_r2 
		int densityVectorLength = 4; 
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		String[] densityNames = new String[]{"ready_r1", "busy_c2_r1", "busy_c1_r1", "ready_r2" };
		ArrayList<String> densityVarNames = new ArrayList<String>();
		for(int i=0; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}

		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0};
		int[] c1_finalSuccessDensity = {0,0,0,numOfMovingRobots};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
						
				
		//c2 : executes task one in r1 with two robots
		int[] c2_initDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_intermediateDensity = {0,numOfRequiredRobotsForTask,0,0};
		int[] c2_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0};
						
		Variable[] c2_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c2_taskInit = bdd.ref(bdd.not(task0));
		int c2_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c2_tasksFinal_success = bdd.ref(task0);
		int c2_tasksFinal_failure = bdd.ref(bdd.not(task0));
						
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_affectedTasks, c2_taskInit, c2_initDensity, c2_tasksIntermediate, c2_intermediateDensity, c2_tasksFinal_success, c2_finalSuccessDensity, c2_tasksFinal_failure, c2_finalFailureDensity);
						
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2};
		
		//**** obtain a game structure from commands **/
		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
		
//		UtilityMethods.debugBDDMethods(bdd, "stutter", stutter);
		
		BDDWrapper.free(bdd, act);
		BDDWrapper.free(bdd, stutter);
		
		
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int r3_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int r4_init = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		init = bdd.andTo(init, r3_init);
		init = bdd.andTo(init, r4_init);
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		bdd.deref(r3_init);
		bdd.deref(r4_init);
		
		boolean[] allFalseTasks = UtilityMethods.allFalseArray(tasks.length);
		int setTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		init = BDDWrapper.andTo(bdd, init, setTasksToFalse);
		BDDWrapper.free(bdd, setTasksToFalse);
				
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
				
		UtilityMethods.duration(t0, "game structure constructed!");
				
				gs.printGameVars();
				UtilityMethods.getUserInput();
//				gs.printGame();
//				UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
				
				
		//**** Define the objective ****/
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
				
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, busy_c1_r1, busy_c2_r1 
		//c1 : moves two robots from r1 to r2
		Variable[] d1 = densityVars.get(2);
		int ass1 = BDDWrapper.assign(bdd, 0, d1);
		int progress =UtilityMethods.lessThan(bdd, Variable.getPrimedCopy(d1), d1);
		ass1 = BDDWrapper.orTo(bdd, ass1, progress);
		BDDWrapper.free(bdd, progress);
		assumptions.add(ass1);
		
		//c2 : executes task one in r1 with two robots
		Variable[] d2 = densityVars.get(1);
		int ass2 = BDDWrapper.assign(bdd, 0, d2);
		int progress2 = UtilityMethods.lessThan(bdd, Variable.getPrimedCopy(d2), d2);
		ass2 = BDDWrapper.orTo(bdd, ass2, progress2);
		BDDWrapper.free(bdd, progress2);
		assumptions.add(ass2);
				
				
//				System.out.println("Assumtpions");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
				
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
				
		//always eventually all robots are in room r2
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(3));
		guarantees.add(reachFinalRoom);
				
//				System.out.println("Guarantees");
//				for(int i=0; i<guarantees.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, guarantees.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		//**** Solve the game ***/

				
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
		GameSolution sol = GR1Solver.solveExtended(bdd, gs, init, objective);
		
		UtilityMethods.duration(t0, "game solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
		
		System.out.println("Winning states ");
		cleanPrintSetOutWithDontCares(bdd, sol.getWinningSystemStates(), densityVars, tasks);
		System.out.println();
				
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);

		UtilityMethods.memoryUsage();
		
		playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks);
		
	}
	
	public static void simpleTwoRoomsTest_coordinationGame(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots =1; 
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r2
		//c2 : executes task one in r1 with two robots
		
		//defining buckets: ready_r1, ready_r2, 
		//					busy_c1_r1, busy_c2_r1 
		
		int densityVectorLength = 4;
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 1;
		Variable[] tasks = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfTasks, "Task");
		
		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "successSignal");
		
//		int numOfMovingRobots = 1;
//		int numOfRequiredRobotsForTask = 1;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0};
		int[] c1_finalSuccessDensity = {0,numOfMovingRobots,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
						
				
		//c2 : executes task one in r1 with two robots
		int[] c2_initDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,numOfRequiredRobotsForTask};
		int[] c2_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0};
						
		Variable[] c2_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c2_taskInit = bdd.ref(bdd.not(task0));
		int c2_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c2_tasksFinal_success = bdd.ref(task0);
		int c2_tasksFinal_failure = bdd.ref(bdd.not(task0));
						
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_affectedTasks, c2_taskInit, c2_initDensity, c2_tasksIntermediate, c2_intermediateDensity, c2_tasksFinal_success, c2_finalSuccessDensity, c2_tasksFinal_failure, c2_finalFailureDensity);
						
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2};
		
		//**** obtain a game structure from commands **/
		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		t0 = UtilityMethods.timeStamp();
//		int T_sys = bdd.ref(bdd.getZero());
//		int T_env = bdd.ref(bdd.getZero());
		
		ArrayList<Integer> T_sys = new ArrayList<Integer>();
		ArrayList<Integer> T_env = new ArrayList<Integer>();
		
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
//			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
//			BDDWrapper.free(bdd, commandFormula_sys);
			
			T_sys.add(commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
//			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
//			BDDWrapper.free(bdd, commandFormula_env);
			
			T_env.add(commandFormula_env);
			
		}
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks, successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
//		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
//		BDDWrapper.free(bdd, act);
//		BDDWrapper.free(bdd, stutter);
		
		T_sys.add(stutter);
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int r3_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int r4_init = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		init = bdd.andTo(init, r3_init);
		init = bdd.andTo(init, r4_init);
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		bdd.deref(r3_init);
		bdd.deref(r4_init);
				
//		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		CoordinationGameStructure gs = new CoordinationGameStructure(bdd, gameVariables, init, T_env, T_sys, actionVars, actionVars);
		
		UtilityMethods.duration(t0, "game structure constructed!");
				
//				gs.printGameVars();
//				UtilityMethods.getUserInput();
//				gs.printGame();
//				UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
				
				
		//**** Define the objective ****/
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
				
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, busy_c1_r1, busy_c2_r1 
		//c1 : moves two robots from r1 to r2
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : executes task one in r1 with two robots
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
				
				
//				System.out.println("Assumtpions");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
				
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
				
		//always eventually all robots are in room r2
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(1));
		guarantees.add(reachFinalRoom);
				
//				System.out.println("Guarantees");
//				for(int i=0; i<guarantees.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, guarantees.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		//**** Solve the game ***/

				
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		UtilityMethods.duration(t0, "game solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
				
		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);

		UtilityMethods.memoryUsage();
	}
	
	public static void simpleTwoRoomsTest2(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots =1; 
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r2
		//c2 : executes task one in r1 with two robots
		
		//defining buckets: ready_r1, ready_r2, 
		//					busy_c1_r1, busy_c2_r1 
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 1;
		Variable[] tasks = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfTasks, "Task");
		
		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, 2, "successSignal");
		
		int densityVectorLength = 4;
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		String[] densityNames = new String[]{"ready_r1", "busy_c2_r1" ,  "busy_c1_r1", "ready_r2" };
		ArrayList<String> densityVarNames = new ArrayList<String>();
		for(int i=0; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}
		

		
//		int numOfMovingRobots = 1;
//		int numOfRequiredRobotsForTask = 1;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0};
		int[] c1_finalSuccessDensity = {0,0,0,numOfMovingRobots};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
						
				
		//c2 : executes task one in r1 with two robots
		int[] c2_initDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_intermediateDensity = {0,numOfRequiredRobotsForTask,0,0};
		int[] c2_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0};
						
		Variable[] c2_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c2_taskInit = bdd.ref(bdd.not(task0));
		int c2_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c2_tasksFinal_success = bdd.ref(task0);
		int c2_tasksFinal_failure = bdd.ref(bdd.not(task0));
						
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_affectedTasks, c2_taskInit, c2_initDensity, c2_tasksIntermediate, c2_intermediateDensity, c2_tasksFinal_success, c2_finalSuccessDensity, c2_tasksFinal_failure, c2_finalFailureDensity);
						
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2};
		
		//**** obtain a game structure from commands **/
		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		//the last command is stutter for system
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks, successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
		
//		UtilityMethods.debugBDDMethods(bdd, "stutter", stutter);
		
		BDDWrapper.free(bdd, act);
		BDDWrapper.free(bdd, stutter);
		
		
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int r3_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int r4_init = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		init = bdd.andTo(init, r3_init);
		init = bdd.andTo(init, r4_init);
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		bdd.deref(r3_init);
		bdd.deref(r4_init);
		
		boolean[] allFalseTasks = UtilityMethods.allFalseArray(tasks.length);
		int setTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		init = BDDWrapper.andTo(bdd, init, setTasksToFalse);
		BDDWrapper.free(bdd, setTasksToFalse);
				
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
				
		UtilityMethods.duration(t0, "game structure constructed!");
				
//				gs.printGameVars();
//				UtilityMethods.getUserInput();
//				gs.printGame();
//				UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
				
				
		//**** Define the objective ****/
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
				
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, busy_c1_r1, busy_c2_r1 
		//c1 : moves two robots from r1 to r2
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : executes task one in r1 with two robots
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
				
				
//				System.out.println("Assumtpions");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
				
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
				
		//always eventually all robots are in room r2
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(3));
		guarantees.add(reachFinalRoom);
				
//				System.out.println("Guarantees");
//				for(int i=0; i<guarantees.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, guarantees.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		//**** Solve the game ***/

				
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
		GameSolution sol = GR1Solver.solve(bdd, gs, init, objective);
		
		UtilityMethods.duration(t0, "game solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
		
//		cleanPrintSetOutWithDontCares(bdd, sol.getWinningSystemStates(), densityVars, tasks,successSignals);
//		UtilityMethods.getUserInput();
				
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);

		UtilityMethods.memoryUsage();
		
//		playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
		
	}
	
	public static void simpleTwoRoomsTest(){
		BDD bdd = new BDD(10000, 1000);
		
		long t0,t1;
		
		//***specify the problem***
		
		//Number of robots
		int numOfRobots =4; 
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r2
		//c2 : executes task one in r1 with two robots
		//c3 : keeps robot in room r2
		

		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1, ready_r2, 
		//					busy_c1_r1, busy_c2_r1 
		
		int densityVectorLength = 4;
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 1;
		Variable[] tasks = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfTasks, "Task");
		
		int numOfMovingRobots = 2;
		int numOfRequiredRobotsForTask = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0};
		int[] c1_finalSuccessDensity = {0,numOfMovingRobots,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
				
		
		//c2 : executes task one in r1 with two robots
		int[] c2_initDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,numOfRequiredRobotsForTask};
		int[] c2_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0};
		int[] c2_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0};
				
		Variable[] c2_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c2_taskInit = bdd.ref(bdd.not(task0));
		int c2_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c2_tasksFinal_success = bdd.ref(task0);
		int c2_tasksFinal_failure = bdd.ref(bdd.not(task0));
				
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_affectedTasks, c2_taskInit, c2_initDensity, c2_tasksIntermediate, c2_intermediateDensity, c2_tasksFinal_success, c2_finalSuccessDensity, c2_tasksFinal_failure, c2_finalFailureDensity);
				
		//c3 :keep two robots in r2
		int[] c3_initDensity = {0,numOfMovingRobots,0,0};
		int[] c3_intermediateDensity = {0,numOfMovingRobots,0,0};
		int[] c3_finalSuccessDensity = {0,numOfMovingRobots,0,0};
		int[] c3_finalFailureDensity = {0,numOfMovingRobots,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
				
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3};
		
		//**** obtain a game structure from commands **/
		int numOfActionVars = UtilityMethods.numOfBits(commands.length-1);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal");	
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		Variable.printVariables(gameVariables);
		
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		//the last command is stutter for system
		for(int i=0; i<commands.length-1; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//define the stutter command for the system
		boolean[] allFalse = new boolean[successSignals.length];
		for(int i=0; i<allFalse.length; i++){
			allFalse[i] = false;
		}
		
//		int setSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		
		int sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
		
		Variable[] densityAndTasks = Variable.unionVariables(densityVars);
		densityAndTasks = Variable.unionVariables(densityAndTasks, tasks);
		int systemStutter = BDDWrapper.same(bdd, densityAndTasks);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, setSuccessSignalsToFalse);
		systemStutter = BDDWrapper.andTo(bdd, systemStutter, sameSuccessSignals);
		int act = BDDWrapper.assign(bdd, commands.length-1, actionVars);
		systemStutter = BDDWrapper.andTo(bdd, systemStutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, systemStutter);
		
		
		

		
		//TODO: add stutter action to the system transition relation
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int r3_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int r4_init = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		init = bdd.andTo(init, r3_init);
		init = bdd.andTo(init, r4_init);
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		bdd.deref(r3_init);
		bdd.deref(r4_init);
		
//		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//		UtilityMethods.getUserInput();
		
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		UtilityMethods.duration(t0, "game structure constructed!");
		
//		gs.printGameVars();
//		UtilityMethods.getUserInput();
//		gs.printGame();
//		UtilityMethods.getUserInput();
		
		
		//**** Define the objective ****/
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, busy_c1_r1, busy_c2_r1 
		//c1 : moves two robots from r1 to r2
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : executes task one in r1 with two robots
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(3));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		
		
//		System.out.println("Assumtpions");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
		
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
		
		//always eventually all robots are in room r2
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(1));
		guarantees.add(reachFinalRoom);
		
//		System.out.println("Guarantees");
//		for(int i=0; i<guarantees.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, guarantees.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		//**** Solve the game ***/

		
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		UtilityMethods.duration(t0, "\ngame solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
		
		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		
		
	}
	
	public static void testPaperExample_coordiantionGameStructure(int numOfRobots , int numOfMovingRobots, int numOfRequiredRobotsForTask){
BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots = 4; 
				
		//Regions: r_1, ..., r_n
//		int numOfRegions = 4;
				
		//Modes: m_1, ..., m_p
//		int numOfModes = 2;
				
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
//		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
				
				

		
		
		
				
		
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r3
		//c2 : moves two robots from r2 to r3
		//c3 : moves two robots from r3 to r4
		//c4 : executes task one in r1 with two robots
		//c5 : executes task two in r2 with two robots
		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1,  busy_c4_r1, busy_c1_r1,  
		//					ready_r2, busy_c5_r2, busy_c2_r2,  
		//					ready_r3, busy_c3_r3, ready_r4
					  
		
		int densityVectorLength = 9;
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		

		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
//		int numOfActionVars = UtilityMethods.numOfBits(5);
//		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");
		
		
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
			
//			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, densityVarsLength, "x"+i);
//			densityVars.add(density_i);
		}
		
		
		
		
		//density var names
		ArrayList<String> densityVarNames = new ArrayList<String>();
		String[] densityNames = new String[]{"ready_r1",   "busy_c4_r1", "busy_c1_r1", "ready_r2", "busy_c5_r2", "busy_c2_r2",  "ready_r3", "busy_c3_r3", "ready_r4"};
		for(int i =0 ; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}
		
//		ArrayList<String> densityVarNames = new ArrayList<String>();
//		for(int i=0; i<densityVectorLength; i++){
//			densityVarNames.add("x"+i);
//		}
//		densityVars = Variable.createVariablesWithInterleavingOrder(bdd, densityVarNames, densityVarsLength);
		

		
//		int numOfMovingRobots = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c1_finalSuccessDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
		
		//c2 : moves two robots from r2 to r3
		int[] c2_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,0,0,numOfMovingRobots,0,0,0};
		int[] c2_finalSuccessDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c2_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_initDensity, c2_intermediateDensity, c2_finalSuccessDensity, c2_finalFailureDensity);
		
		//c3 : moves two robots from r3 to r4
		int[] c3_initDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c3_intermediateDensity = {0,0,0,0,0,0,0,numOfMovingRobots,0};
		int[] c3_finalSuccessDensity = {0,0,0,0,0,0,0,0,numOfMovingRobots};
		int[] c3_finalFailureDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
		
		
		//c4 : executes task one in r1 with two robots
		int[] c4_initDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_intermediateDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c4_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		
		Variable[] c4_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c4_taskInit = bdd.ref(bdd.not(task0));
		int c4_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c4_tasksFinal_success = bdd.ref(task0);
		int c4_tasksFinal_failure = bdd.ref(bdd.not(task0));
		
		SimpleCommandVector c4 = new SimpleCommandVector("C4", bdd, c4_affectedTasks, c4_taskInit, c4_initDensity, c4_tasksIntermediate, c4_intermediateDensity, c4_tasksFinal_success, c4_finalSuccessDensity, c4_tasksFinal_failure, c4_finalFailureDensity);
		
		//c5 : executes task two in r2 with two robots
		int[] c5_initDensity = {0,0,0,numOfRequiredRobotsForTask,0,0,0,0,0};
		int[] c5_intermediateDensity = {0,0,0,0,numOfRequiredRobotsForTask,0,0,0,0};
		int[] c5_finalSuccessDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c5_finalFailureDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		
		Variable[] c5_affectedTasks = new Variable[]{tasks[1]};
		int task1 = tasks[1].getBDDVar();
		int c5_taskInit = bdd.ref(bdd.not(task1));
		int c5_tasksIntermediate = bdd.ref(bdd.not(task1));
		int c5_tasksFinal_success = bdd.ref(task1);
		int c5_tasksFinal_failure = bdd.ref(bdd.not(task1));
		
		SimpleCommandVector c5 = new SimpleCommandVector("C5", bdd, c5_affectedTasks, c5_taskInit, c5_initDensity, c5_tasksIntermediate, c5_intermediateDensity, c5_tasksFinal_success, c5_finalSuccessDensity, c5_tasksFinal_failure, c5_finalFailureDensity);
		
		//c6 : keep robots in room r4 (dummy action to make the game infinte
//		int[] c6_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_intermediateDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		SimpleCommandVector c6 = new SimpleCommandVector("C6", bdd, c6_initDensity, c6_intermediateDensity, c6_finalSuccessDensity, c6_finalFailureDensity);
		
//		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5,c6};
		
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5};
		
		//**** obtain a game structure from commands **/
		t0 = UtilityMethods.timeStamp();
		
		CoordinationGameStructure cgs = new CoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
		cgs.printGameVars();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+cgs.actionVars.length);
		int total = 2*cgs.variables.length+cgs.actionVars.length;
		System.out.println("over all "+total+" of variables in system");
		
		//test the controllable predecessor operator
		//define a set of states 
		
//		cgs.printCommandsInfo();
//		UtilityMethods.getUserInput();
		
		
		boolean[] allFalseTasks = UtilityMethods.allFalseArray(tasks.length);
		int setTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		for(int i=1; i<densityVars.size(); i++){
			if(i!=3){
				int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
				init=bdd.andTo(init, r_init);
				bdd.deref(r_init);
			}
		}
		bdd.deref(r1_init);
		bdd.deref(r2_init);
		init = bdd.andTo(init, setTasksToFalse);

		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
				
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//defining buckets: ready_r1, busy_c1_r1, busy_c4_r1, 
		//					ready_r2, busy_c2_r2, busy_c5_r2, 
		//					ready_r3, busy_c3_r3, ready_r4
		//c1 : moves two robots from r1 to r3
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : moves two robots from r2 to r3
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(5));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		//c3 : moves two robots from r3 to r4
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(7));
		ass3 = BDDWrapper.orTo(bdd, ass3, successSignals[2].getBDDVar());
		assumptions.add(ass3);
		//c4 : executes task one in r1 with two robots
		int ass4 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass4 = BDDWrapper.orTo(bdd, ass4, successSignals[3].getBDDVar());
		assumptions.add(ass4);
		//c5 : executes task two in r2 with two robots
		int ass5 = BDDWrapper.assign(bdd, 0, densityVars.get(4));
		ass5 = BDDWrapper.orTo(bdd, ass5, successSignals[4].getBDDVar());
		assumptions.add(ass5);
				
//				System.out.println("Assumtpions");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
				//guarantees
				//TODO:always the numbber of robots in the middle room is less than some bound
				
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
				
		//always eventually all robots are in room r4
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(8));
		guarantees.add(reachFinalRoom);
				
//				System.out.println("Guarantees");
//				for(int i=0; i<assumptions.size();i++){
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, assumptions.get(i));
//				}
//				UtilityMethods.getUserInput();
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
				
		//**** Solve the game ***/
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//				GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), cgs, init, objective);
				
//				cgs.cleanCoordinationGameStatePrintAndWait("winning states are", w.getWinningStates());
				
		GameSolution sol = GR1Solver.solve(bdd, cgs, init, objective);
				
		UtilityMethods.duration(t0, "game solved in ");
//				UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
						
//				int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
//				UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
				
		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
				
		UtilityMethods.memoryUsage();
				
//				playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
				
		CoordinationStrategy strategy = (CoordinationStrategy) sol.strategyOfTheWinner();
				
		strategy.playCoordinationGameRandomly(init);
		
	}
	
	public static void testPaperExample_DifferentVariableOrdering2(int numOfRobots , int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots = 4; 
				
		//Regions: r_1, ..., r_n
//		int numOfRegions = 4;
				
		//Modes: m_1, ..., m_p
//		int numOfModes = 2;
				
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
//		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
				
				

		
		
		
				
		
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r3
		//c2 : moves two robots from r2 to r3
		//c3 : moves two robots from r3 to r4
		//c4 : executes task one in r1 with two robots
		//c5 : executes task two in r2 with two robots
		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1,  busy_c4_r1, busy_c1_r1,  
		//					ready_r2, busy_c5_r2, busy_c2_r2,  
		//					ready_r3, busy_c3_r3, ready_r4
					  
		
		int densityVectorLength = 9;
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		

		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
		int numOfActionVars = UtilityMethods.numOfBits(5);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");
		
		
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
			
//			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, densityVarsLength, "x"+i);
//			densityVars.add(density_i);
		}
		
		
		
		
		//density var names
		ArrayList<String> densityVarNames = new ArrayList<String>();
		String[] densityNames = new String[]{"ready_r1",   "busy_c4_r1", "busy_c1_r1", "ready_r2", "busy_c5_r2", "busy_c2_r2",  "ready_r3", "busy_c3_r3", "ready_r4"};
		for(int i =0 ; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}
		
//		ArrayList<String> densityVarNames = new ArrayList<String>();
//		for(int i=0; i<densityVectorLength; i++){
//			densityVarNames.add("x"+i);
//		}
//		densityVars = Variable.createVariablesWithInterleavingOrder(bdd, densityVarNames, densityVarsLength);
		

		
//		int numOfMovingRobots = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		int[] c1_intermediateDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c1_finalSuccessDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
		
		//c2 : moves two robots from r2 to r3
		int[] c2_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,0,0,numOfMovingRobots,0,0,0};
		int[] c2_finalSuccessDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c2_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_initDensity, c2_intermediateDensity, c2_finalSuccessDensity, c2_finalFailureDensity);
		
		//c3 : moves two robots from r3 to r4
		int[] c3_initDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c3_intermediateDensity = {0,0,0,0,0,0,0,numOfMovingRobots,0};
		int[] c3_finalSuccessDensity = {0,0,0,0,0,0,0,0,numOfMovingRobots};
		int[] c3_finalFailureDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
		
		
		//c4 : executes task one in r1 with two robots
		int[] c4_initDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_intermediateDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c4_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		
		Variable[] c4_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c4_taskInit = bdd.ref(bdd.not(task0));
		int c4_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c4_tasksFinal_success = bdd.ref(task0);
		int c4_tasksFinal_failure = bdd.ref(bdd.not(task0));
		
		SimpleCommandVector c4 = new SimpleCommandVector("C4", bdd, c4_affectedTasks, c4_taskInit, c4_initDensity, c4_tasksIntermediate, c4_intermediateDensity, c4_tasksFinal_success, c4_finalSuccessDensity, c4_tasksFinal_failure, c4_finalFailureDensity);
		
		//c5 : executes task two in r2 with two robots
		int[] c5_initDensity = {0,0,0,numOfRequiredRobotsForTask,0,0,0,0,0};
		int[] c5_intermediateDensity = {0,0,0,0,numOfRequiredRobotsForTask,0,0,0,0};
		int[] c5_finalSuccessDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c5_finalFailureDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		
		Variable[] c5_affectedTasks = new Variable[]{tasks[1]};
		int task1 = tasks[1].getBDDVar();
		int c5_taskInit = bdd.ref(bdd.not(task1));
		int c5_tasksIntermediate = bdd.ref(bdd.not(task1));
		int c5_tasksFinal_success = bdd.ref(task1);
		int c5_tasksFinal_failure = bdd.ref(bdd.not(task1));
		
		SimpleCommandVector c5 = new SimpleCommandVector("C5", bdd, c5_affectedTasks, c5_taskInit, c5_initDensity, c5_tasksIntermediate, c5_intermediateDensity, c5_tasksFinal_success, c5_finalSuccessDensity, c5_tasksFinal_failure, c5_finalFailureDensity);
		
		//c6 : keep robots in room r4 (dummy action to make the game infinte
//		int[] c6_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_intermediateDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		SimpleCommandVector c6 = new SimpleCommandVector("C6", bdd, c6_initDensity, c6_intermediateDensity, c6_finalSuccessDensity, c6_finalFailureDensity);
		
//		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5,c6};
		
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5};
		
		//**** obtain a game structure from commands **/
//		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
//		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
//		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
		
//		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, commands.length, "successSignal_");
//		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
		
		//Note: if a set of consistent commands do not affect a subset of tasks, then the state of those tasks remain unchanged
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//define the stutter command for the system
//		boolean[] allFalse = new boolean[successSignals.length];
//		for(int i=0; i<allFalse.length; i++){
//			allFalse[i] = false;
//		}
				
//		int setSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		
//		int sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
				
//		Variable[] densityAndTasks = Variable.unionVariables(densityVars);
//		densityAndTasks = Variable.unionVariables(densityAndTasks, tasks);
//		int systemStutter = BDDWrapper.same(bdd, densityAndTasks);
////		systemStutter = BDDWrapper.andTo(bdd, systemStutter, setSuccessSignalsToFalse);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, sameSuccessSignals);
//		int act = BDDWrapper.assign(bdd, commands.length-1, actionVars);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, act);
//		T_sys = BDDWrapper.orTo(bdd, T_sys, systemStutter);
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks, successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		//TODO: add stutter action to the system transition relation
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		for(int i=1; i<densityVars.size(); i++){
			if(i!=3){
				int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
				init=bdd.andTo(init, r_init);
				bdd.deref(r_init);
			}
		}
		bdd.deref(r1_init);
		bdd.deref(r2_init);
//		init = bdd.andTo(init, setSuccessSignalsToFalse);
		
		
		boolean[] allFalseTasks = new boolean[tasks.length];
		for(int i=0; i<allFalseTasks.length; i++){
			allFalseTasks[i] = false;
		}
		int setAllTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		init = bdd.andTo(init, setAllTasksToFalse);
		
//		boolean[] allFalseSuccessSignals = UtilityMethods.allFalseArray(successSignals.length);
//		int setAllSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalseSuccessSignals, successSignals);
//		init = bdd.andTo(init, setAllSuccessSignalsToFalse);
		
//		parseCoordinationStateFormula(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
//		
//		cleanPrintSetOut(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		cleanPrintSetOutWithDontCares(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
//		int exactly2Agents = excactlyN_Agent(bdd, densityVars, numOfRobots);
//		cleanPrintSetOutWithDontCares(bdd, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
		
//		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//		UtilityMethods.getUserInput();
		
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		
		UtilityMethods.duration(t0, "game structure created!");
		
		t0= UtilityMethods.timeStamp();
		gs.printGameVars();
//		UtilityMethods.getUserInput();
//		gs.printGame();
//		UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
		
		
		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//defining buckets: ready_r1, busy_c1_r1, busy_c4_r1, 
		//					ready_r2, busy_c2_r2, busy_c5_r2, 
		//					ready_r3, busy_c3_r3, ready_r4
		//c1 : moves two robots from r1 to r3
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : moves two robots from r2 to r3
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(5));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		//c3 : moves two robots from r3 to r4
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(7));
		ass3 = BDDWrapper.orTo(bdd, ass3, successSignals[2].getBDDVar());
		assumptions.add(ass3);
		//c4 : executes task one in r1 with two robots
		int ass4 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass4 = BDDWrapper.orTo(bdd, ass4, successSignals[3].getBDDVar());
		assumptions.add(ass4);
		//c5 : executes task two in r2 with two robots
		int ass5 = BDDWrapper.assign(bdd, 0, densityVars.get(4));
		ass5 = BDDWrapper.orTo(bdd, ass5, successSignals[4].getBDDVar());
		assumptions.add(ass5);
		
//		System.out.println("Assumtpions");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
		
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
		
		//always eventually all robots are in room r4
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(8));
		guarantees.add(reachFinalRoom);
		
//		System.out.println("Guarantees");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
		
		//**** Solve the game ***/

		
		//form and solve the game
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
//		UtilityMethods.getUserInput();
		
//		int validWinningRegion = BDDWrapper.and(bdd, w.getWinningStates(), exactly2Agents);
//		cleanPrintSetOutWithDontCares(bdd, validWinningRegion, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		w.printMemory(bdd);
//		cleanPrintMemory(bdd, w, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
		
		
		GameSolution solution = GR1Solver.solve(bdd, gs, init, objective);
		

		
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		
		int intersect = BDDWrapper.and(bdd, solution.getWinningSystemStates(), init);
		
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		UtilityMethods.duration(t0, "\ngame solved!");
		
		UtilityMethods.memoryUsage();
		
//		playCoordinationGameRandomly(bdd, solution.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
	}
	
	public static void testPaperExample_DifferentVariableOrdering(int numOfRobots , int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots = 4; 
				
		//Regions: r_1, ..., r_n
//		int numOfRegions = 4;
				
		//Modes: m_1, ..., m_p
//		int numOfModes = 2;
				
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
//		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
				
				

		
		
		
				
		
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r3
		//c2 : moves two robots from r2 to r3
		//c3 : moves two robots from r3 to r4
		//c4 : executes task one in r1 with two robots
		//c5 : executes task two in r2 with two robots
		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1, busy_c1_r1, busy_c4_r1, 
		//					ready_r2, busy_c2_r2, busy_c5_r2, 
		//					ready_r3, busy_c3_r3, ready_r4
					  
		
		int densityVectorLength = 9;
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		
		
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
		int numOfActionVars = UtilityMethods.numOfBits(5);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");
		
		
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
			
//			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, densityVarsLength, "x"+i);
//			densityVars.add(density_i);
		}
		
		//density var names
		ArrayList<String> densityVarNames = new ArrayList<String>();
		String[] densityNames = new String[]{"ready_r1", "busy_c1_r1", "busy_c4_r1", "ready_r2", "busy_c2_r2", "busy_c5_r2", "ready_r3", "busy_c3_r3", "ready_r4"};
		for(int i =0 ; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}
		
//		ArrayList<String> densityVarNames = new ArrayList<String>();
//		for(int i=0; i<densityVectorLength; i++){
//			densityVarNames.add("x"+i);
//		}
//		densityVars = Variable.createVariablesWithInterleavingOrder(bdd, densityVarNames, densityVarsLength);
		

		
//		int numOfMovingRobots = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		int[] c1_intermediateDensity = {0,numOfMovingRobots,0,0,0,0,0,0,0};
		int[] c1_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
		
		//c2 : moves two robots from r2 to r3
		int[] c2_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,0,numOfMovingRobots,0,0,0,0};
		int[] c2_finalSuccessDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c2_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_initDensity, c2_intermediateDensity, c2_finalSuccessDensity, c2_finalFailureDensity);
		
		//c3 : moves two robots from r3 to r4
		int[] c3_initDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c3_intermediateDensity = {0,0,0,0,0,0,0,numOfMovingRobots,0};
		int[] c3_finalSuccessDensity = {0,0,0,0,0,0,0,0,numOfMovingRobots};
		int[] c3_finalFailureDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
		
		
		//c4 : executes task one in r1 with two robots
		int[] c4_initDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_intermediateDensity = {0,0,numOfRequiredRobotsForTask,0,0,0,0,0,0};
		int[] c4_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		
		Variable[] c4_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c4_taskInit = bdd.ref(bdd.not(task0));
		int c4_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c4_tasksFinal_success = bdd.ref(task0);
		int c4_tasksFinal_failure = bdd.ref(bdd.not(task0));
		
		SimpleCommandVector c4 = new SimpleCommandVector("C4", bdd, c4_affectedTasks, c4_taskInit, c4_initDensity, c4_tasksIntermediate, c4_intermediateDensity, c4_tasksFinal_success, c4_finalSuccessDensity, c4_tasksFinal_failure, c4_finalFailureDensity);
		
		//c5 : executes task two in r2 with two robots
		int[] c5_initDensity = {0,0,0,numOfRequiredRobotsForTask,0,0,0,0,0};
		int[] c5_intermediateDensity = {0,0,0,0,0,numOfRequiredRobotsForTask,0,0,0};
		int[] c5_finalSuccessDensity = {0,0,0,numOfRequiredRobotsForTask,0,0,0,0,0};
		int[] c5_finalFailureDensity = {0,0,0,numOfRequiredRobotsForTask,0,0,0,0,0};
		
		Variable[] c5_affectedTasks = new Variable[]{tasks[1]};
		int task1 = tasks[1].getBDDVar();
		int c5_taskInit = bdd.ref(bdd.not(task1));
		int c5_tasksIntermediate = bdd.ref(bdd.not(task1));
		int c5_tasksFinal_success = bdd.ref(task1);
		int c5_tasksFinal_failure = bdd.ref(bdd.not(task1));
		
		SimpleCommandVector c5 = new SimpleCommandVector("C5", bdd, c5_affectedTasks, c5_taskInit, c5_initDensity, c5_tasksIntermediate, c5_intermediateDensity, c5_tasksFinal_success, c5_finalSuccessDensity, c5_tasksFinal_failure, c5_finalFailureDensity);
		
		//c6 : keep robots in room r4 (dummy action to make the game infinte
//		int[] c6_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_intermediateDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		SimpleCommandVector c6 = new SimpleCommandVector("C6", bdd, c6_initDensity, c6_intermediateDensity, c6_finalSuccessDensity, c6_finalFailureDensity);
		
//		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5,c6};
		
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5};
		
		//**** obtain a game structure from commands **/
//		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
//		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
//		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
		
//		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, commands.length, "successSignal_");
//		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
		
		//Note: if a set of consistent commands do not affect a subset of tasks, then the state of those tasks remain unchanged
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//define the stutter command for the system
//		boolean[] allFalse = new boolean[successSignals.length];
//		for(int i=0; i<allFalse.length; i++){
//			allFalse[i] = false;
//		}
				
//		int setSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		
//		int sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
				
//		Variable[] densityAndTasks = Variable.unionVariables(densityVars);
//		densityAndTasks = Variable.unionVariables(densityAndTasks, tasks);
//		int systemStutter = BDDWrapper.same(bdd, densityAndTasks);
////		systemStutter = BDDWrapper.andTo(bdd, systemStutter, setSuccessSignalsToFalse);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, sameSuccessSignals);
//		int act = BDDWrapper.assign(bdd, commands.length-1, actionVars);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, act);
//		T_sys = BDDWrapper.orTo(bdd, T_sys, systemStutter);
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks, successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		//TODO: add stutter action to the system transition relation
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(3));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		for(int i=1; i<densityVars.size(); i++){
			if(i!=3){
				int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
				init=bdd.andTo(init, r_init);
				bdd.deref(r_init);
			}
		}
		bdd.deref(r1_init);
		bdd.deref(r2_init);
//		init = bdd.andTo(init, setSuccessSignalsToFalse);
		
		
		boolean[] allFalseTasks = new boolean[tasks.length];
		for(int i=0; i<allFalseTasks.length; i++){
			allFalseTasks[i] = false;
		}
		int setAllTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		init = bdd.andTo(init, setAllTasksToFalse);
		
//		boolean[] allFalseSuccessSignals = UtilityMethods.allFalseArray(successSignals.length);
//		int setAllSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalseSuccessSignals, successSignals);
//		init = bdd.andTo(init, setAllSuccessSignalsToFalse);
		
//		parseCoordinationStateFormula(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
//		
//		cleanPrintSetOut(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		cleanPrintSetOutWithDontCares(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
//		int exactly2Agents = excactlyN_Agent(bdd, densityVars, numOfRobots);
//		cleanPrintSetOutWithDontCares(bdd, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
		
//		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//		UtilityMethods.getUserInput();
		
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		
		UtilityMethods.duration(t0, "game structure created!");
		
		t0= UtilityMethods.timeStamp();
		gs.printGameVars();
//		UtilityMethods.getUserInput();
//		gs.printGame();
//		UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
		
		
		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//defining buckets: ready_r1, busy_c1_r1, busy_c4_r1, 
		//					ready_r2, busy_c2_r2, busy_c5_r2, 
		//					ready_r3, busy_c3_r3, ready_r4
		//c1 : moves two robots from r1 to r3
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : moves two robots from r2 to r3
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(4));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		//c3 : moves two robots from r3 to r4
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(7));
		ass3 = BDDWrapper.orTo(bdd, ass3, successSignals[2].getBDDVar());
		assumptions.add(ass3);
		//c4 : executes task one in r1 with two robots
		int ass4 = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		ass4 = BDDWrapper.orTo(bdd, ass4, successSignals[3].getBDDVar());
		assumptions.add(ass4);
		//c5 : executes task two in r2 with two robots
		int ass5 = BDDWrapper.assign(bdd, 0, densityVars.get(5));
		ass5 = BDDWrapper.orTo(bdd, ass5, successSignals[4].getBDDVar());
		assumptions.add(ass5);
		
//		System.out.println("Assumtpions");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
		
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
		
		//always eventually all robots are in room r4
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(8));
		guarantees.add(reachFinalRoom);
		
//		System.out.println("Guarantees");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
		
		//**** Solve the game ***/

		
		//form and solve the game
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
//		UtilityMethods.getUserInput();
		
//		int validWinningRegion = BDDWrapper.and(bdd, w.getWinningStates(), exactly2Agents);
//		cleanPrintSetOutWithDontCares(bdd, validWinningRegion, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		w.printMemory(bdd);
//		cleanPrintMemory(bdd, w, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
		
		
		GameSolution solution = GR1Solver.solve(bdd, gs, init, objective);
		

		
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		
		int intersect = BDDWrapper.and(bdd, solution.getWinningSystemStates(), init);
		
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		UtilityMethods.duration(t0, "game solved!");
		
		UtilityMethods.memoryUsage();
		
		playCoordinationGameRandomly(bdd, solution.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
	}
	
	public static void testPaperExample(int numOfRobots , int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots = 4; 
				
		//Regions: r_1, ..., r_n
//		int numOfRegions = 4;
				
		//Modes: m_1, ..., m_p
//		int numOfModes = 2;
				
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
//		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
				
				

		
		
		
				
		
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r3
		//c2 : moves two robots from r2 to r3
		//c3 : moves two robots from r3 to r4
		//c4 : executes task one in r1 with two robots
		//c5 : executes task two in r2 with two robots
		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1, ready_r2, ready_r3, ready_r4
		//					busy_c1_r1, busy_c2_r2, busy_c3_r3, busy_c4_r1, busy_c5_r2
		
		int densityVectorLength = 9;
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
				
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
				
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
				
		int numOfActionVars = UtilityMethods.numOfBits(5);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
				
//		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
				
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
//			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, densityVarsLength, "x"+i);
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		//density var names
		ArrayList<String> densityVarNames = new ArrayList<String>();
		String[] densityNames = new String[]{"ready_r1", "ready_r2", "ready_r3", "ready_r4", "busy_c1_r1", "busy_c2_r2", "busy_c3_r3", "busy_c4_r1", "busy_c5_r2"};
		for(int i =0 ; i<densityNames.length; i++){
			densityVarNames.add(densityNames[i]);
		}
		
//		ArrayList<String> densityVarNames = new ArrayList<String>();
//		for(int i=0; i<densityVectorLength; i++){
//			densityVarNames.add("x"+i);
//		}
//		densityVars = Variable.createVariablesWithInterleavingOrder(bdd, densityVarNames, densityVarsLength);
		
		
		
//		int numOfMovingRobots = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		int[] c1_intermediateDensity = {0,0,0,0,numOfMovingRobots,0,0,0,0};
		int[] c1_finalSuccessDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
		
		//c2 : moves two robots from r2 to r3
		int[] c2_initDensity = {0,numOfMovingRobots,0,0,0,0,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,0,0,numOfMovingRobots,0,0,0};
		int[] c2_finalSuccessDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c2_finalFailureDensity = {0,numOfMovingRobots,0,0,0,0,0,0,0};
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_initDensity, c2_intermediateDensity, c2_finalSuccessDensity, c2_finalFailureDensity);
		
		//c3 : moves two robots from r3 to r4
		int[] c3_initDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c3_intermediateDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c3_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c3_finalFailureDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
		
		
		//c4 : executes task one in r1 with two robots
		int[] c4_initDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_intermediateDensity = {0,0,0,0,0,0,0,numOfRequiredRobotsForTask,0};
		int[] c4_finalSuccessDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		int[] c4_finalFailureDensity = {numOfRequiredRobotsForTask,0,0,0,0,0,0,0,0};
		
		Variable[] c4_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c4_taskInit = bdd.ref(bdd.not(task0));
		int c4_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c4_tasksFinal_success = bdd.ref(task0);
		int c4_tasksFinal_failure = bdd.ref(bdd.not(task0));
		
		SimpleCommandVector c4 = new SimpleCommandVector("C4", bdd, c4_affectedTasks, c4_taskInit, c4_initDensity, c4_tasksIntermediate, c4_intermediateDensity, c4_tasksFinal_success, c4_finalSuccessDensity, c4_tasksFinal_failure, c4_finalFailureDensity);
		
		//c5 : executes task two in r2 with two robots
		int[] c5_initDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c5_intermediateDensity = {0,0,0,0,0,0,0,0,numOfRequiredRobotsForTask};
		int[] c5_finalSuccessDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		int[] c5_finalFailureDensity = {0,numOfRequiredRobotsForTask,0,0,0,0,0,0,0};
		
		Variable[] c5_affectedTasks = new Variable[]{tasks[1]};
		int task1 = tasks[1].getBDDVar();
		int c5_taskInit = bdd.ref(bdd.not(task1));
		int c5_tasksIntermediate = bdd.ref(bdd.not(task1));
		int c5_tasksFinal_success = bdd.ref(task1);
		int c5_tasksFinal_failure = bdd.ref(bdd.not(task1));
		
		SimpleCommandVector c5 = new SimpleCommandVector("C5", bdd, c5_affectedTasks, c5_taskInit, c5_initDensity, c5_tasksIntermediate, c5_intermediateDensity, c5_tasksFinal_success, c5_finalSuccessDensity, c5_tasksFinal_failure, c5_finalFailureDensity);
		
		//c6 : keep robots in room r4 (dummy action to make the game infinte
//		int[] c6_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_intermediateDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		int[] c6_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
//		SimpleCommandVector c6 = new SimpleCommandVector("C6", bdd, c6_initDensity, c6_intermediateDensity, c6_finalSuccessDensity, c6_finalFailureDensity);
		
//		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5,c6};
		
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5};
		
		//**** obtain a game structure from commands **/
//		int numOfActionVars = UtilityMethods.numOfBits(commands.length);
//		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
//		
////		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
//		
//		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, commands.length, "successSignal_");
		
		//Note: if a set of consistent commands do not affect a subset of tasks, then the state of those tasks remain unchanged
		t0 = UtilityMethods.timeStamp();
		int T_sys = bdd.ref(bdd.getZero());
		int T_env = bdd.ref(bdd.getZero());
		
		for(int i=0; i<commands.length; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
			BDDWrapper.free(bdd, commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
			BDDWrapper.free(bdd, commandFormula_env);
			
		}
		
		//define the stutter command for the system
//		boolean[] allFalse = new boolean[successSignals.length];
//		for(int i=0; i<allFalse.length; i++){
//			allFalse[i] = false;
//		}
				
//		int setSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		
//		int sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
				
//		Variable[] densityAndTasks = Variable.unionVariables(densityVars);
//		densityAndTasks = Variable.unionVariables(densityAndTasks, tasks);
//		int systemStutter = BDDWrapper.same(bdd, densityAndTasks);
////		systemStutter = BDDWrapper.andTo(bdd, systemStutter, setSuccessSignalsToFalse);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, sameSuccessSignals);
//		int act = BDDWrapper.assign(bdd, commands.length-1, actionVars);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, act);
//		T_sys = BDDWrapper.orTo(bdd, T_sys, systemStutter);
		
		//add stutter to system transition relation
		int act = BDDWrapper.assign(bdd, commands.length, actionVars);
		int stutter = createStutterFormula(bdd, densityVars, tasks, successSignals);
		stutter = BDDWrapper.andTo(bdd, stutter, act);
		T_sys = BDDWrapper.orTo(bdd, T_sys, stutter);
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		//TODO: add stutter action to the system transition relation
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(1));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		for(int i=2; i<densityVars.size(); i++){
			int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
			init=bdd.andTo(init, r_init);
			bdd.deref(r_init);
		}
		bdd.deref(r1_init);
		bdd.deref(r2_init);
//		init = bdd.andTo(init, setSuccessSignalsToFalse);
		
		
		boolean[] allFalseTasks = new boolean[tasks.length];
		for(int i=0; i<allFalseTasks.length; i++){
			allFalseTasks[i] = false;
		}
		int setAllTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
		init = bdd.andTo(init, setAllTasksToFalse);
		
//		boolean[] allFalseSuccessSignals = UtilityMethods.allFalseArray(successSignals.length);
//		int setAllSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalseSuccessSignals, successSignals);
//		init = bdd.andTo(init, setAllSuccessSignalsToFalse);
		
//		parseCoordinationStateFormula(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
//		
//		cleanPrintSetOut(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		cleanPrintSetOutWithDontCares(bdd, init, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
//		int exactly2Agents = excactlyN_Agent(bdd, densityVars, numOfRobots);
//		cleanPrintSetOutWithDontCares(bdd, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
		
		
//		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//		UtilityMethods.getUserInput();
		
		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		
		UtilityMethods.duration(t0, "game structure created!");
		
		t0= UtilityMethods.timeStamp();
		gs.printGameVars();
//		UtilityMethods.getUserInput();
//		gs.printGame();
//		UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+gs.variables.length);
		System.out.println("Number of action variables "+gs.actionVars.length);
		int total = 2*gs.variables.length+actionVars.length;
		System.out.println("over all "+total+" of variables in system");
		
		
		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, ready_r3, ready_r4
		//					busy_c1_r1, busy_c2_r2, busy_c3_r3, busy_c4_r1, busy_c5_r2
		//c1 : moves two robots from r1 to r3
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(4));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : moves two robots from r2 to r3
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(5));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		//c3 : moves two robots from r3 to r4
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(6));
		ass3 = BDDWrapper.orTo(bdd, ass3, successSignals[2].getBDDVar());
		assumptions.add(ass3);
		//c4 : executes task one in r1 with two robots
		int ass4 = BDDWrapper.assign(bdd, 0, densityVars.get(7));
		ass4 = BDDWrapper.orTo(bdd, ass4, successSignals[3].getBDDVar());
		assumptions.add(ass4);
		//c5 : executes task two in r2 with two robots
		int ass5 = BDDWrapper.assign(bdd, 0, densityVars.get(8));
		ass5 = BDDWrapper.orTo(bdd, ass5, successSignals[4].getBDDVar());
		assumptions.add(ass5);
		
//		System.out.println("Assumtpions");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
		
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
		
		//always eventually all robots are in room r4
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(3));
		guarantees.add(reachFinalRoom);
		
//		System.out.println("Guarantees");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
		
		//**** Solve the game ***/

		
		//form and solve the game
//		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
//		UtilityMethods.getUserInput();
		
//		int validWinningRegion = BDDWrapper.and(bdd, w.getWinningStates(), exactly2Agents);
//		cleanPrintSetOutWithDontCares(bdd, validWinningRegion, densityVars, densityVarNames, tasks, successSignals);
//		UtilityMethods.getUserInput();
		
//		w.printMemory(bdd);
//		cleanPrintMemory(bdd, w, exactly2Agents, densityVars, densityVarNames, tasks, successSignals);
		
		
		GameSolution solution = GR1Solver.solve(bdd, gs, init, objective);
		

		
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		
		int intersect = BDDWrapper.and(bdd, solution.getWinningSystemStates(), init);
		
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		UtilityMethods.duration(t0, "game solved!");
		
		UtilityMethods.memoryUsage();
		
		playCoordinationGameRandomly(bdd, solution.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
	}
	
	public static void testPaperExampleWithCoordinationGameStructure(int numOfRobots, int numOfMovingRobots){
		BDD bdd = new BDD(10000, 1000);
		
		long t0,t1;
		
		//***specify the problem***
		
		//Number of robots
//		int numOfRobots = 4; 
				
				
		//Modes: m_1, ..., m_p
//		int numOfModes = 2;
				
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
//		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
				
				

		
		
		
				
		
		
		//**** define commands **/
		//We are going to have following commands
		//c1 : moves two robots from r1 to r3
		//c2 : moves two robots from r2 to r3
		//c3 : moves two robots from r3 to r4
		//c4 : executes task one in r1 with two robots
		//c5 : executes task two in r2 with two robots
		
		//exisiting modes: ready, busy_c1, busy_c2, busy_c3, busy_c4, busy_c5
		
		//defining buckets: ready_r1, ready_r2, ready_r3, ready_r4
		//					busy_c1_r1, busy_c2_r2, busy_c3_r3, busy_c4_r1, busy_c5_r2
		
		int densityVectorLength = 9;
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, densityVarsLength, "x"+i);
					densityVars.add(density_i);
		}
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
//		int numOfMovingRobots = 2;
		
		//define c1 : moves two robots from r1 to r3
		int[] c1_initDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		int[] c1_intermediateDensity = {0,0,0,0,numOfMovingRobots,0,0,0,0};
		int[] c1_finalSuccessDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c1_finalFailureDensity = {numOfMovingRobots,0,0,0,0,0,0,0,0};
		SimpleCommandVector c1 = new SimpleCommandVector("C1", bdd, c1_initDensity, c1_intermediateDensity, c1_finalSuccessDensity, c1_finalFailureDensity);
		
		//c2 : moves two robots from r2 to r3
		int[] c2_initDensity = {0,numOfMovingRobots,0,0,0,0,0,0,0};
		int[] c2_intermediateDensity = {0,0,0,0,0,numOfMovingRobots,0,0,0};
		int[] c2_finalSuccessDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c2_finalFailureDensity = {0,numOfMovingRobots,0,0,0,0,0,0,0};
		SimpleCommandVector c2 = new SimpleCommandVector("C2", bdd, c2_initDensity, c2_intermediateDensity, c2_finalSuccessDensity, c2_finalFailureDensity);
		
		//c3 : moves two robots from r3 to r4
		int[] c3_initDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		int[] c3_intermediateDensity = {0,0,0,0,0,0,numOfMovingRobots,0,0};
		int[] c3_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c3_finalFailureDensity = {0,0,numOfMovingRobots,0,0,0,0,0,0};
		SimpleCommandVector c3 = new SimpleCommandVector("C3", bdd, c3_initDensity, c3_intermediateDensity, c3_finalSuccessDensity, c3_finalFailureDensity);
		
		//c4 : executes task one in r1 with two robots
		int[] c4_initDensity = {2,0,0,0,0,0,0,0,0};
		int[] c4_intermediateDensity = {0,0,0,0,0,0,0,2,0};
		int[] c4_finalSuccessDensity = {2,0,0,0,0,0,0,0,0};
		int[] c4_finalFailureDensity = {2,0,0,0,0,0,0,0,0};
		
		Variable[] c4_affectedTasks = new Variable[]{tasks[0]};
		int task0 = tasks[0].getBDDVar();
		int c4_taskInit = bdd.ref(bdd.not(task0));
		int c4_tasksIntermediate = bdd.ref(bdd.not(task0));
		int c4_tasksFinal_success = bdd.ref(task0);
		int c4_tasksFinal_failure = bdd.ref(bdd.not(task0));
		
		SimpleCommandVector c4 = new SimpleCommandVector("C4", bdd, c4_affectedTasks, c4_taskInit, c4_initDensity, c4_tasksIntermediate, c4_intermediateDensity, c4_tasksFinal_success, c4_finalSuccessDensity, c4_tasksFinal_failure, c4_finalFailureDensity);
		
		//c5 : executes task two in r2 with two robots
		int[] c5_initDensity = {0,2,0,0,0,0,0,0,0};
		int[] c5_intermediateDensity = {0,0,0,0,0,0,0,0,2};
		int[] c5_finalSuccessDensity = {2,0,0,0,0,0,0,0,0};
		int[] c5_finalFailureDensity = {2,0,0,0,0,0,0,0,0};
		
		Variable[] c5_affectedTasks = new Variable[]{tasks[1]};
		int task1 = tasks[1].getBDDVar();
		int c5_taskInit = bdd.ref(bdd.not(task1));
		int c5_tasksIntermediate = bdd.ref(bdd.not(task1));
		int c5_tasksFinal_success = bdd.ref(task1);
		int c5_tasksFinal_failure = bdd.ref(bdd.not(task1));
		
		SimpleCommandVector c5 = new SimpleCommandVector("C5", bdd, c5_affectedTasks, c5_taskInit, c5_initDensity, c5_tasksIntermediate, c5_intermediateDensity, c5_tasksFinal_success, c5_finalSuccessDensity, c5_tasksFinal_failure, c5_finalFailureDensity);
		
		//c6 : keep robots in room r4 (dummy action to make the game infinte
		int[] c6_initDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c6_intermediateDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c6_finalSuccessDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		int[] c6_finalFailureDensity = {0,0,0,numOfMovingRobots,0,0,0,0,0};
		SimpleCommandVector c6 = new SimpleCommandVector("C6", bdd, c6_initDensity, c6_intermediateDensity, c6_finalSuccessDensity, c6_finalFailureDensity);
		
		SimpleCommandVector[] commands = new SimpleCommandVector[]{c1, c2, c3, c4, c5,c6};
		
		//**** obtain a game structure from commands **/
		int numOfActionVars = UtilityMethods.numOfBits(commands.length-1);
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
//		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, commands.length, "successSignal_");
		
		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, commands.length, "successSignal_");
		
		//Note: if a set of consistent commands do not affect a subset of tasks, then the state of those tasks remain unchanged
		t0 = UtilityMethods.timeStamp();
//		int T_sys = bdd.ref(bdd.getZero());
//		int T_env = bdd.ref(bdd.getZero());
		
		ArrayList<Integer> T_env = new ArrayList<Integer>();
		ArrayList<Integer> T_sys = new ArrayList<Integer>();
		
		for(int i=0; i<commands.length-1; i++){
			SimpleCommandVector currentCommand = commands[i];
			
			int act = BDDWrapper.assign(bdd, i, actionVars);
			
			//debugging
//			System.out.println("current command is c"+(i+1));
//			UtilityMethods.debugBDDMethods(bdd, "act is ", act);
//			UtilityMethods.getUserInput();
			
			int commandFormula_sys = commandFormula_systemTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for system is", commandFormula_sys);
//			UtilityMethods.getUserInput();
			
//			T_sys = BDDWrapper.orTo(bdd, T_sys, commandFormula_sys);
//			BDDWrapper.free(bdd, commandFormula_sys);
			
			T_sys.add(commandFormula_sys);
			
			int commandFormula_env = commandFormula_environmentTransitions(bdd, currentCommand, 
					numOfRobots, densityVars, tasks, act, successSignals, successSignals[i]);
			
//			UtilityMethods.debugBDDMethods(bdd, "command formula for environment is ", commandFormula_env);
//			UtilityMethods.getUserInput();
			
//			T_env = BDDWrapper.orTo(bdd, T_env, commandFormula_env);
//			BDDWrapper.free(bdd, commandFormula_env);
			
			T_env.add(commandFormula_env);
			
		}
		
		//define the stutter command for the system
		boolean[] allFalse = new boolean[successSignals.length];
		for(int i=0; i<allFalse.length; i++){
			allFalse[i] = false;
		}
				
//		int setSuccessSignalsToFalse = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		int sameSuccessSignals = BDDWrapper.same(bdd, successSignals);
				
		Variable[] densityAndTasks = Variable.unionVariables(densityVars);
		densityAndTasks = Variable.unionVariables(densityAndTasks, tasks);
		int systemStutter = BDDWrapper.same(bdd, densityAndTasks);
//		systemStutter = BDDWrapper.andTo(bdd, systemStutter, setSuccessSignalsToFalse);
		systemStutter = BDDWrapper.andTo(bdd, systemStutter, sameSuccessSignals);
		int act = BDDWrapper.assign(bdd, commands.length-1, actionVars);
		systemStutter = BDDWrapper.andTo(bdd, systemStutter, act);
//		T_sys = BDDWrapper.orTo(bdd, T_sys, systemStutter);
		
		T_sys.add(systemStutter);
		
		Variable[] gameVariables = Variable.unionVariables(densityVars);
		gameVariables = Variable.unionVariables(gameVariables, tasks);
		gameVariables = Variable.unionVariables(gameVariables, successSignals);
		
		//TODO: add stutter action to the system transition relation
		//define init 
		int init = bdd.ref(bdd.getOne());
		int r1_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(1));
		init = bdd.andTo(init, r1_init);
		init = bdd.andTo(init, r2_init);
		for(int i=2; i<densityVars.size(); i++){
			int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
			init=bdd.andTo(init, r_init);
			bdd.deref(r_init);
		}
		bdd.deref(r1_init);
		bdd.deref(r2_init);
//		init = bdd.andTo(init, setSuccessSignalsToFalse);
		
//		boolean[] allFalseTasks = new boolean[tasks.length];
//		for(int i=0; i<allFalseTasks.length; i++){
//			allFalseTasks[i] = false;
//		}
//		int setAllTasksToFalse = BDDWrapper.assign(bdd, allFalseTasks, tasks);
//		init = bdd.andTo(init, setAllTasksToFalse);
		
		
//		UtilityMethods.debugBDDMethods(bdd, "init is ", init);
//		UtilityMethods.getUserInput();
		
//		GameStructure gs = new GameStructure(bdd, gameVariables, Variable.getPrimedCopy(gameVariables), init, T_env, T_sys, actionVars, actionVars);
		
		CoordinationGameStructure gs = new CoordinationGameStructure(bdd, gameVariables, init, T_env, T_sys, actionVars, actionVars);
		
		UtilityMethods.duration(t0, "game structure created!");
		
		t0= UtilityMethods.timeStamp();
		gs.printGameVars();
		UtilityMethods.getUserInput();
//		gs.printGame();
//		UtilityMethods.getUserInput();
		
		
		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
		
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//buckets: ready_r1, ready_r2, ready_r3, ready_r4
		//					busy_c1_r1, busy_c2_r2, busy_c3_r3, busy_c4_r1, busy_c5_r2
		//c1 : moves two robots from r1 to r3
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(4));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		//c2 : moves two robots from r2 to r3
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(5));
		ass2 = BDDWrapper.orTo(bdd, ass2, successSignals[1].getBDDVar());
		assumptions.add(ass2);
		//c3 : moves two robots from r3 to r4
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(6));
		ass3 = BDDWrapper.orTo(bdd, ass3, successSignals[2].getBDDVar());
		assumptions.add(ass3);
		//c4 : executes task one in r1 with two robots
		int ass4 = BDDWrapper.assign(bdd, 0, densityVars.get(7));
		ass4 = BDDWrapper.orTo(bdd, ass4, successSignals[3].getBDDVar());
		assumptions.add(ass4);
		//c5 : executes task two in r2 with two robots
		int ass5 = BDDWrapper.assign(bdd, 0, densityVars.get(8));
		ass5 = BDDWrapper.orTo(bdd, ass5, successSignals[4].getBDDVar());
		assumptions.add(ass5);
		
//		System.out.println("Assumtpions");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "assumption "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		//guarantees
		//TODO:always the numbber of robots in the middle room is less than some bound
		
		//always eventuall all tasks are done
		boolean[] allTrue = new boolean[tasks.length];
		for(int i=0; i<allTrue.length; i++){
			allTrue[i]=true;
		}
		int taskFormula = BDDWrapper.assign(bdd, allTrue, tasks);
		guarantees.add(taskFormula);
		
		//always eventually all robots are in room r4
		int reachFinalRoom = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(3));
		guarantees.add(reachFinalRoom);
		
//		System.out.println("Guarantees");
//		for(int i=0; i<assumptions.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i, assumptions.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
		
		//**** Solve the game ***/

		
		//form and solve the game
		GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), gs, init, objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
		
		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
		UtilityMethods.duration(t0, "game solved!");
	}
	
	public static int commandFormula_systemTransitions(BDD bdd, SimpleCommandVector command, int numOfAgents, 
			ArrayList<Variable[]> densityVars, Variable[] tasks, int commandAction, Variable[] successSignals){
		
		int commandFormula = bdd.ref(bdd.getOne());
		
//		Variable commandSuccessSignalPrime = commandSuccessSignal.getPrimedCopy();
//		int successSignal = bdd.ref(bdd.not(commandSuccessSignalPrime.getBDDVar()));
		
//		int sameSuccessSignal = BDDWrapper.same(bdd, commandSuccessSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
		int sameSuccessSignal = BDDWrapper.same(bdd, successSignals);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, sameSuccessSignal);
		
//		int falseNextSuccessSignal = allFalseNextSuccessSignals(bdd, successSignals);
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, falseNextSuccessSignal);
		
		
		//task formula 
		if(command.getAffectedTasks() != null){
			int taskFormula = bdd.ref(command.getInitTasksCondition());
			
			int taskIntermediate = command.getIntermediateTasksCondition();
			Variable[] primedTaskVariables = Variable.getPrimedCopy(tasks);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(tasks), Variable.getBDDVars(primedTaskVariables));
			int taskIntermediatePrime = bdd.ref(bdd.replace(taskIntermediate, taskPerm));
			
			taskFormula = bdd.andTo(taskFormula, taskIntermediatePrime);
			BDDWrapper.free(bdd, taskIntermediatePrime);
		
			//unaffected tasks stay unchanged
			Variable[] unaffectedTasks  = Variable.difference(tasks, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			taskFormula = bdd.andTo(taskFormula, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
			commandFormula = bdd.andTo(commandFormula, taskFormula);
			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, tasks);
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
		if(numOfRobotsRequiredForCommands>numOfAgents){
			commandFormula = bdd.ref(bdd.getZero());
			return commandFormula;
		}
		
		//create density formula
		int densityFormula = bdd.ref(bdd.getOne());
				
		//x_0 >= d_0 & ... & x_k >= d_k
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfAgents);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, lowerBound);
		BDDWrapper.free(bdd, lowerBound);
		
		//x'_i = x_i +d'_i - d_i
		int nextDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, nextDensityVector);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, nextDensityFormula);
		BDDWrapper.free(bdd, nextDensityFormula);
				
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, densityFormula);
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
		return commandFormula;
	}
	
	public static int commandFormula_systemTransitions(BDD bdd, SimpleCommandVector command, int numOfAgents, 
			ArrayList<Variable[]> densityVars, Variable[] tasks, int commandAction){
		
		int commandFormula = bdd.ref(bdd.getOne());
		
		
		//task formula 
		if(command.getAffectedTasks() != null){
			int taskFormula = bdd.ref(command.getInitTasksCondition());
			
			int taskIntermediate = command.getIntermediateTasksCondition();
			Variable[] primedTaskVariables = Variable.getPrimedCopy(tasks);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(tasks), Variable.getBDDVars(primedTaskVariables));
			int taskIntermediatePrime = bdd.ref(bdd.replace(taskIntermediate, taskPerm));
			
			taskFormula = bdd.andTo(taskFormula, taskIntermediatePrime);
			BDDWrapper.free(bdd, taskIntermediatePrime);
		
			//unaffected tasks stay unchanged
			Variable[] unaffectedTasks  = Variable.difference(tasks, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			taskFormula = bdd.andTo(taskFormula, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
			commandFormula = bdd.andTo(commandFormula, taskFormula);
			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, tasks);
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
		if(numOfRobotsRequiredForCommands>numOfAgents){
			commandFormula = bdd.ref(bdd.getZero());
			return commandFormula;
		}
		
		//create density formula
		int densityFormula = bdd.ref(bdd.getOne());
				
		//x_0 >= d_0 & ... & x_k >= d_k
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfAgents);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, lowerBound);
		BDDWrapper.free(bdd, lowerBound);
		
		//x'_i = x_i +d'_i - d_i
		int nextDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, nextDensityVector);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, nextDensityFormula);
		BDDWrapper.free(bdd, nextDensityFormula);
				
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, densityFormula);
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
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
	
	public static int commandFormula_environmentTransitions(BDD bdd, SimpleCommandVector command, int numOfAgents, 
			ArrayList<Variable[]> densityVars, Variable[] tasks, int commandAction , Variable[] successSignals, Variable commandSuccessSignal){
		int commandFormula = bdd.ref(bdd.getOne());
		
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
		int allButOneSuccessSignalsOFF = setNextSuccessSignals(bdd, successSignals, commandSuccessSignal);
		
		stutter = BDDWrapper.andTo(bdd, stutter, allNextSuccessSignalsOFF);
		success = BDDWrapper.andTo(bdd, success, allButOneSuccessSignalsOFF);
		fail = BDDWrapper.andTo(bdd, fail, allNextSuccessSignalsOFF);
		
		BDDWrapper.free(bdd, allNextSuccessSignalsOFF);
		BDDWrapper.free(bdd, allButOneSuccessSignalsOFF);
		
		//task formula 
		if(command.getAffectedTasks() != null){
			
			int sameTaskState = BDDWrapper.same(bdd, tasks);
			stutter = bdd.andTo(stutter, sameTaskState);
			BDDWrapper.free(bdd, sameTaskState);
			
			int taskFormula = command.getIntermediateTasksCondition();
			
			Variable[] primedTaskVariables = Variable.getPrimedCopy(tasks);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(tasks), Variable.getBDDVars(primedTaskVariables));
			
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
			Variable[] unaffectedTasks  = Variable.difference(tasks, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			success = bdd.andTo(success, sameUnaffectedTasks);
			fail = bdd.andTo(fail, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
//			commandFormula = bdd.andTo(commandFormula, taskFormula);
//			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, tasks);
			
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
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfAgents);
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
		
		
		
		commandFormula = bdd.ref(bdd.or(stutter, success));
		commandFormula = bdd.orTo(commandFormula, fail);
		bdd.deref(stutter);
		bdd.deref(success);
		bdd.deref(fail);
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
		return commandFormula;
		
		
	}
	
	public static int commandFormula_environmentTransitions(BDD bdd, SimpleCommandVector command, int numOfAgents, 
			ArrayList<Variable[]> densityVars, Variable[] tasks, int commandAction){
		int commandFormula = bdd.ref(bdd.getOne());
		
		int stutter = bdd.ref(bdd.getOne());
		int success = bdd.ref(bdd.getOne());
		int fail = bdd.ref(bdd.getOne());
		
		//task formula 
		if(command.getAffectedTasks() != null){
			
			int sameTaskState = BDDWrapper.same(bdd, tasks);
			stutter = bdd.andTo(stutter, sameTaskState);
			BDDWrapper.free(bdd, sameTaskState);
			
			int taskFormula = command.getIntermediateTasksCondition();
			
			Variable[] primedTaskVariables = Variable.getPrimedCopy(tasks);
			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(tasks), Variable.getBDDVars(primedTaskVariables));
			
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
			Variable[] unaffectedTasks  = Variable.difference(tasks, command.getAffectedTasks());
			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
			success = bdd.andTo(success, sameUnaffectedTasks);
			fail = bdd.andTo(fail, sameUnaffectedTasks);
			BDDWrapper.free(bdd, sameUnaffectedTasks);
			
//			commandFormula = bdd.andTo(commandFormula, taskFormula);
//			BDDWrapper.free(bdd, taskFormula);
		}else{
			int sameTaskState = BDDWrapper.same(bdd, tasks);
			
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
		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfAgents);
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
		
		
		
		commandFormula = bdd.ref(bdd.or(stutter, success));
		commandFormula = bdd.orTo(commandFormula, fail);
		bdd.deref(stutter);
		bdd.deref(success);
		bdd.deref(fail);
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
		
		return commandFormula;
	}
	
//	public static int commandFormula_environmentTransitions(BDD bdd, SimpleCommandVector command, int numOfAgents, 
//			ArrayList<Variable[]> densityVars, Variable[] tasks, int commandAction , Variable commandSuccessSignal){
//		int commandFormula = bdd.ref(bdd.getOne());
//		
//		int stutter = bdd.ref(bdd.getOne());
//		int success = bdd.ref(bdd.getOne());
//		int fail = bdd.ref(bdd.getOne());
//		
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
//		
//		//task formula 
//		if(command.getAffectedTasks() != null){
//			
//			int sameTaskState = BDDWrapper.same(bdd, tasks);
//			stutter = bdd.andTo(stutter, sameTaskState);
//			BDDWrapper.free(bdd, sameTaskState);
//			
//			int taskFormula = command.getIntermediateTasksCondition();
//			
//			Variable[] primedTaskVariables = Variable.getPrimedCopy(tasks);
//			Permutation taskPerm = bdd.createPermutation(Variable.getBDDVars(tasks), Variable.getBDDVars(primedTaskVariables));
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
//			Variable[] unaffectedTasks  = Variable.difference(tasks, command.getAffectedTasks());
//			int sameUnaffectedTasks = BDDWrapper.same(bdd, unaffectedTasks);
//			success = bdd.andTo(success, sameUnaffectedTasks);
//			fail = bdd.andTo(fail, sameUnaffectedTasks);
//			BDDWrapper.free(bdd, sameUnaffectedTasks);
//			
////			commandFormula = bdd.andTo(commandFormula, taskFormula);
////			BDDWrapper.free(bdd, taskFormula);
//		}else{
//			int sameTaskState = BDDWrapper.same(bdd, tasks);
//			
//			stutter = bdd.andTo(stutter, sameTaskState);
//			success = bdd.andTo(success, sameTaskState);
//			fail = bdd.andTo(fail, sameTaskState);
//			
//			BDDWrapper.free(bdd, sameTaskState);
//		}
//		
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
//		int lowerBound = densityLowerBoundConstraints(bdd, densityVars, densityVector, numOfAgents);
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
//		
//		
//		
//		commandFormula = bdd.ref(bdd.or(stutter, success));
//		commandFormula = bdd.orTo(commandFormula, fail);
//		bdd.deref(stutter);
//		bdd.deref(success);
//		bdd.deref(fail);
//		
//		commandFormula = BDDWrapper.andTo(bdd, commandFormula, commandAction);
//		
//		return commandFormula;
//		
//		
//	}
	
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
	
	
	public static int createStutterFormula(BDD bdd, ArrayList<Variable[]> densityVars, Variable[] tasks, Variable[] successSignals){
		int stutter = bdd.ref(bdd.getOne());
		
		for(int i=0; i<densityVars.size(); i++){
			int sameDensityVars = BDDWrapper.same(bdd, densityVars.get(i));
			stutter = BDDWrapper.andTo(bdd, stutter, sameDensityVars);
			BDDWrapper.free(bdd, sameDensityVars);
		}
		
		int sameTasks = BDDWrapper.same(bdd, tasks);
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
	
	public static int createStutterFormula(BDD bdd, ArrayList<Variable[]> densityVars, Variable[] tasks){
		int stutter = bdd.ref(bdd.getOne());
		
		for(int i=0; i<densityVars.size(); i++){
			int sameDensityVars = BDDWrapper.same(bdd, densityVars.get(i));
			stutter = BDDWrapper.andTo(bdd, stutter, sameDensityVars);
			BDDWrapper.free(bdd, sameDensityVars);
		}
		
		int sameTasks = BDDWrapper.same(bdd, tasks);
		stutter = BDDWrapper.andTo(bdd, stutter, sameTasks);
		BDDWrapper.free(bdd, sameTasks);
		
		return stutter;
	}
	
	public static int allFalseNextSuccessSignals(BDD bdd, Variable[] successSignals){
		boolean[] allFalse = UtilityMethods.allFalseArray(successSignals.length);
		int falseNextSuccessSignals = BDDWrapper.assign(bdd, allFalse, Variable.getPrimedCopy(successSignals));
		return falseNextSuccessSignals;
	}
	
	public static void cleanPrintMemory(BDD bdd, GR1WinningStates w, int exactNumOfAgents, ArrayList<Variable[]> densityVariables, ArrayList<String> densityVarsNames, 
			Variable[] taskVariables, Variable[] successSignals){
		System.out.println("printing the memory");
		System.out.println("mY");
		ArrayList<ArrayList<Integer>> mY = w.getMY();
		for(int i=0; i<mY.size(); i++){
			ArrayList<Integer> mY_i = mY.get(i);
			for(int j=0; j<mY_i.size(); j++){
//				UtilityMethods.debugBDDMethods(bdd, "mY["+i+"]["+j+"] is ",mY_i.get(j));
				
				int validmY_i = BDDWrapper.and(bdd, mY_i.get(j), exactNumOfAgents);
				
				cleanPrintSetOutWithDontCares(bdd, validmY_i, densityVariables, taskVariables, successSignals);
				
				BDDWrapper.free(bdd, validmY_i);
				
				if(j>0){
					int mY_i_j = mY_i.get(j);
					int mY_i_j_1 = mY_i.get(j-1);
					if(mY_i_j == mY_i_j_1){
						System.out.println("current memory is equivalent to the previous one!");
					}
				}
				
				UtilityMethods.getUserInput();
			}
		}
		
		System.out.println("mX");
		ArrayList<ArrayList<ArrayList<Integer>>> mX = w.getMX();
		for(int i=0; i<mX.size(); i++){
			ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
			for(int j=0; j<mX_i.size(); j++){
				ArrayList<Integer> mX_i_j = mX_i.get(j);
				for(int k=0; k<mX_i_j.size(); k++){
//					UtilityMethods.debugBDDMethods(bdd, "mX["+i+"]["+j+"]["+k+"] is ",mX_i_j.get(k));
					
					int validMx = BDDWrapper.and(bdd, mX_i_j.get(k), exactNumOfAgents);
					cleanPrintSetOutWithDontCares(bdd, validMx, densityVariables, taskVariables, successSignals);
					BDDWrapper.free(bdd, validMx);
					
					UtilityMethods.getUserInput();
				}
			}
		}
	}
}
