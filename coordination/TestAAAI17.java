package coordination;

import game.BDDWrapper;
import game.GameSolution;
import game.Variable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;

import jdd.bdd.BDD;
import solver.GR1Objective;
import solver.GR1Solver;
import utils.FileOps;
import utils.UtilityMethods;

public class TestAAAI17 {
	public static void main(String[] args){
		
		int numOfRobots = 16;
		int numOfMovingRobots = 1;
		int numOfRequiredRobotsForTheTasks = 2;
		
		System.out.println("******************************************************************************************************************");
		System.out.println("**************************************************New Experiment**************************************************");
		System.out.println("experiment started at "+new Date().toString());
		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(baos);
//		PrintStream old = System.out;
//		System.setOut(ps);
//		
//		System.out.println();
//		System.out.println("******************************************************************************************************************");
//		System.out.println("**************************************************New Experiment**************************************************");		
//		System.out.println("experiment started at "+new Date().toString());
		

		
		//centralized
		
//		TestSimpleCoordination.simpleTwoRoomsTest2(numOfRobots,numOfMovingRobots,numOfRequiredRobotsForTheTasks);
		
//		TestSimpleCoordination.testPaperExample_DifferentVariableOrdering2(numOfRobots, numOfMovingRobots, numOfRequiredRobotsForTheTasks);

		//compositional 
		
//		testCompositionalCoordinationGameStructure_twoRooms(numOfRobots, numOfMovingRobots, numOfRequiredRobotsForTheTasks);
		
//		testCompositionalCoordinationGameStructure_paperExample(numOfRobots,numOfMovingRobots,numOfRequiredRobotsForTheTasks);
		
		//optimized version
		
//		testOptimalCompositionalCoordinationGameStructure_twoRooms(numOfRobots, numOfMovingRobots, numOfRequiredRobotsForTheTasks);
		
//		testOptimizedCompositionalCoordinationGameStructure_paperExample(numOfRobots,numOfMovingRobots,numOfRequiredRobotsForTheTasks);
		
		//abstraction
		testAbstractCoordinationGameStructure_simpleExample(numOfRobots, numOfMovingRobots, numOfRequiredRobotsForTheTasks);
		
		
	    //System.out.println(satisfying);
//	    System.out.println("experiment finished at "+new Date().toString());
//	    System.out.println("******************************************************************************************************************");
//	    System.out.flush();
//	    String result=baos.toString();
//	    FileOps.write(result, "experimentResults.txt");
//	    System.setOut(old);
		
	    System.out.println("experiment finished at "+new Date().toString());
	    System.out.println("******************************************************************************************************************");
	}
	
	/**
	 * It is a very simple example where there are two rooms, and one command that moves the robots from room r_1 to room r_2 
	 * @param numOfRobots
	 * @param numOfMovingRobots
	 * @param numOfRequiredRobotsForTasks
	 */
	public static void testAbstractCoordinationGameStructure_simpleExample(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTasks){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//**** specify the problem ***
		
		//What is the maximum number of robots we need to keep track of in the abstraction?
		//In this simple example, we have one command that moves one robot. 
		//Therefore, we have three states --> 0 robot, 1 robot, 2 or more than 2 robots 
		int thresholdNumOfRobots = 2;
		
		//task variables
		int numOfTasks = 1;
		Variable[] tasks = new Variable[numOfTasks];
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
		//*** define commands ***/
		//c1: moves one robot from room r_1 to r_2
		
		//defining density variables
		//ready_r1, busy_c1_r1, ready_r2
		
		//density variables
		int numOfDensityVars = 3;
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(thresholdNumOfRobots);
		for(int i=0; i<numOfDensityVars; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
			densityVars.add(density_i);
		}
		
		//success signals
		Variable[] successSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, 1, "successSignal");
		
		//persistence variables
		Variable[] persistenceSignals = Variable.createVariablesAndTheirPrimedCopy(bdd, 1, "persistenceSignal");
		
		//define commands
		SimpleCommand c1 = new SimpleCommand("C1", bdd, numOfMovingRobots, densityVars.get(0),densityVars.get(1), densityVars.get(2), densityVars.get(0));
		
		SimpleCommand[] commands = new SimpleCommand[]{c1};
		
		//**** obtain a game structure from commands **/
		t0 = UtilityMethods.timeStamp();
		
		
		AbstractCoordinationGameStructure cgs = new AbstractCoordinationGameStructure(bdd, numOfRobots, thresholdNumOfRobots, commands, densityVars, tasks, successSignals, persistenceSignals);
		
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
//		cgs.printGameVars();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+(cgs.actionVars==null?0:cgs.actionVars.length));
		int total = 2*cgs.variables.length+(cgs.actionVars==null?0:cgs.actionVars.length);
		System.out.println("over all "+total+" of variables in system");
		
		//define init
		int init = BDDWrapper.assign(bdd, 2, densityVars.get(0));
		int r2_init = BDDWrapper.assign(bdd, 0, densityVars.get(2));
		int middleInit = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		init = BDDWrapper.andTo(bdd, init, r2_init);
		init = BDDWrapper.andTo(bdd, init, middleInit);
		BDDWrapper.free(bdd, r2_init);
		BDDWrapper.free(bdd, middleInit);
		
		//**** Define the objective ****/
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> assumptions = new ArrayList<Integer>();
		ArrayList<Integer> guarantees = new ArrayList<Integer>();
						
		//assumptions 
		//for each command: always eventually either the intermediate mode is empty or success signal is high
		//defining buckets: ready_r1, busy_c1_r1, ready_r2, 
		int ass1 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass1 = BDDWrapper.orTo(bdd, ass1, successSignals[0].getBDDVar());
		assumptions.add(ass1);
		
		//persistence assumptions
		//always eventually persistence leads to zero robot in room r_1
		int ass2 = BDDWrapper.assign(bdd, 0, densityVars.get(0));
		ass2 = BDDWrapper.orTo(bdd, ass2, bdd.not(persistenceSignals[0].getBDDVar()));
		assumptions.add(ass2);
		
		//always eventually not persisting leads to zero robot in intermediate mode
		int ass3 = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		ass3 = BDDWrapper.orTo(bdd, ass3, persistenceSignals[0].getBDDVar());
		assumptions.add(ass3);
		
		//guarantees 
		//always eventually all the robots are in room r_2
		int r1_final = BDDWrapper.assign(bdd, 0, densityVars.get(0));
		int middle_final = BDDWrapper.assign(bdd, 0, densityVars.get(1));
		int reach = BDDWrapper.and(bdd, r1_final, middle_final);
		guarantees.add(reach); 
		
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
		
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
				
		//**** Solve the game ***/
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//				GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), cgs, init, objective);
				
//				cgs.cleanCoordinationGameStatePrintAndWait("winning states are", w.getWinningStates());
				
		GameSolution sol = GR1Solver.solve(bdd, cgs, init, objective);
				
		UtilityMethods.duration(t0, "\ngame solved in ");
//				UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
						
//				int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
				UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
				
//		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
				
		UtilityMethods.memoryUsage();
		
	}
	
	public static void testCompositionalCoordinationGameStructure_paperExample(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}
		
//		System.out.println(Variable.allVars.size());
//		System.out.println(Variable.allVars.get(0));
//		UtilityMethods.getUserInput();

		//int numOfActionVars = UtilityMethods.numOfBits(5);
		//Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");

		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");

		
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
		
		
		
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		

		
		
		
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
		
		CompositionalCoordinationGameStructure cgs = new CompositionalCoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
//		cgs.printGameVars();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+(cgs.actionVars==null?0:cgs.actionVars.length));
		int total = 2*cgs.variables.length+(cgs.actionVars==null?0:cgs.actionVars.length);
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
				
		UtilityMethods.duration(t0, "\ngame solved in ");
//				UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
						
//				int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
				UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
				
//		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
				
		UtilityMethods.memoryUsage();
				
//				playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
				
//		CompositionalCoordinationStrategy strategy = (CompositionalCoordinationStrategy) sol.strategyOfTheWinner();
				
//		strategy.playCoordinationGameRandomly(init);
	}
	
	public static void testOptimizedCompositionalCoordinationGameStructure_paperExample(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
		BDD bdd = new BDD(10000, 1000);
		
		long t0;
		
		//***specify the problem***
		
		
		
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = Variable.createVariableAndPrimedVariable(bdd, "Task"+i);
		}


				
		//int numOfActionVars = UtilityMethods.numOfBits(5);
		//Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");

		Variable[] successSignals= Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, 5, "successSignal_");

		

		
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
		
		
		
//		for(int i=0; i<numOfTasks; i++){
//			tasks[i] = new Variable(bdd, "Task"+i);
//		}
//		Variable.createPrimeVariables(bdd, tasks);
		
		

		
		
		
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
		
//		//static variable ordering, define GR1 counter here
//		int numOfGuarantees = 2;
//		int numOfCounterVars = UtilityMethods.numOfBits(numOfGuarantees-1);
//		Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfCounterVars, "gr1Counter");
		
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
		
		OptimizedCompositionalCoordinationGameStructure cgs = new OptimizedCompositionalCoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
//		cgs.printGameVars();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+(cgs.actionVars==null?0:cgs.actionVars.length));
		int total = 2*cgs.variables.length+(cgs.actionVars==null?0:cgs.actionVars.length);
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
		bdd.deref(r1_init);
		init = bdd.andTo(init, r2_init);
		bdd.deref(r2_init);
		for(int i=1; i<densityVars.size(); i++){
			if(i!=3){
				int r_init = BDDWrapper.assign(bdd, 0, densityVars.get(i));
				init=bdd.andTo(init, r_init);
				bdd.deref(r_init);
			}
		}
		init = bdd.andTo(init, setTasksToFalse);
		BDDWrapper.free(bdd, setTasksToFalse);

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
		
//		if(guarantees.size() != numOfGuarantees){
//			System.err.println("number of GR1 counter bits does not match the number of guarantees! "
//					+ "Update the value for the variable numOfGuarantees!");
//		}
				
		GR1Objective objective = new GR1Objective(new solver.BDDWrapper(bdd), assumptions, guarantees);
				
		UtilityMethods.duration(t0, "objective created!");
		t0= UtilityMethods.timeStamp();
				
		//**** Solve the game ***/
		//form and solve the game
		t0 = UtilityMethods.timeStamp();
//				GR1WinningStates w = GR1Solver.computeWinningStates(new solver.BDDWrapper(bdd), cgs, init, objective);
				
//				cgs.cleanCoordinationGameStatePrintAndWait("winning states are", w.getWinningStates());
				
		GameSolution sol = GR1Solver.solve(bdd, cgs, init, objective);
		
//		GameSolution sol = GR1Solver.solve(bdd, cgs, init, objective, counter);
				
		UtilityMethods.duration(t0, "\ngame solved in ");
//				UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
						
//				int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
				UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
				
//		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
				
		UtilityMethods.memoryUsage();
				
//				playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
				
//		CompositionalCoordinationStrategy strategy = (CompositionalCoordinationStrategy) sol.strategyOfTheWinner();
				
//		strategy.playCoordinationGameRandomly(init);
		
		bdd.cleanup();
	}
	
	public static void testCompositionalCoordinationGameStructure_twoRooms(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
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
		
		CompositionalCoordinationGameStructure cgs = new CompositionalCoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
//		cgs.printGameVars();
//		cgs.printGame();
//		UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+(cgs.actionVars==null?0:cgs.actionVars.length));
		int total = 2*cgs.variables.length+(cgs.actionVars==null?0:cgs.actionVars.length);
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
		
//		cgs.cleanCoordinationGameStatePrintAndWait("init is", init);
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
		
		UtilityMethods.duration(t0, "\ngame solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
				
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
//		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
		
		UtilityMethods.memoryUsage();
		
//		playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
		
//		CompositionalCoordinationStrategy strategy = (CompositionalCoordinationStrategy) sol.strategyOfTheWinner();
		
//		strategy.playCoordinationGameRandomly(init);
	}
	
	
	public static void testOptimalCompositionalCoordinationGameStructure_twoRooms(int numOfRobots, int numOfMovingRobots, int numOfRequiredRobotsForTask){
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
		
		OptimizedCompositionalCoordinationGameStructure cgs = new OptimizedCompositionalCoordinationGameStructure(bdd, numOfRobots, commands, densityVars, tasks, successSignals );
		
		UtilityMethods.duration(t0, "coordination game structure created! ");
		
//		cgs.printGameVars();
//		cgs.printGame();
//		UtilityMethods.getUserInput();
		
		System.out.println("Number of game variables "+cgs.variables.length);
		System.out.println("Number of action variables "+(cgs.actionVars==null?0:cgs.actionVars.length));
		int total = 2*cgs.variables.length+(cgs.actionVars==null?0:cgs.actionVars.length);
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
		
//		cgs.cleanCoordinationGameStatePrintAndWait("init is", init);
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
		
		UtilityMethods.duration(t0, "\ngame solved in ");
//		UtilityMethods.debugBDDMethods(bdd, "winning region is", w.getWinningStates());
				
//		int intersect = BDDWrapper.and(bdd, w.getWinningStates(), init);
		int intersect = BDDWrapper.and(bdd, sol.getWinningSystemStates(), init);
		UtilityMethods.debugBDDMethods(bdd, "intersection of initial set and winning set is ",intersect);
		
//		cgs.cleanCoordinationGameStatePrintAndWait("intersection of initial set and winning set is", intersect);
		
		UtilityMethods.memoryUsage();
		
//		playCoordinationGameRandomly(bdd, sol.strategyOfTheWinner(), commands, densityVars, densityVarNames, tasks, successSignals);
		
//		CompositionalCoordinationStrategy strategy = (CompositionalCoordinationStrategy) sol.strategyOfTheWinner();
		
//		strategy.playCoordinationGameRandomly(init);
	}
	
	
}
