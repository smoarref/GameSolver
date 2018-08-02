package coordination;

import java.util.ArrayList;

import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import jdd.bdd.BDD;
import game.BDDWrapper;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Variable;

public class testCoordination {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BDD bdd = new BDD(10000, 1000);
		
		long t0,t1;
		
		
		//test
//		int N = 3;
//		int R = 2;
//		int M = 2;
//		
//		ArrayList<Variable[]> dVars = new ArrayList<Variable[]>();
//		int dVarsLength = UtilityMethods.numOfBits(N);
//		for(int i=0; i<R*M; i++){
//			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, dVarsLength, "x"+i);
//			dVars.add(density_i);
//		}
//		
//		int d1 = agentsConservationConstraint(bdd, dVars, N);
//		UtilityMethods.debugBDDMethods(bdd, "agent conservation", d1);
//		
//		UtilityMethods.getUserInput();
//		
//		int[] lowerBounds = {1,0,0,1};
//		
//		int d2 = densityLowerBoundConstraints(bdd, dVars, lowerBounds, N);
//		UtilityMethods.debugBDDMethods(bdd, "lowerbounds", d2);
//		
//		UtilityMethods.getUserInput();
//		
//		int[] currentDensity = {1,1,0,0};
//		int[] nextDensity = {1,0,1,0};
//		int d3 = nextDensityFormula(bdd, dVars, currentDensity, nextDensity);
//		UtilityMethods.debugBDDMethods(bdd, "lowerbounds", d3);
//		
//		
//		UtilityMethods.getUserInput();
		
		//***specify the problem***
		
		//Number of robots
		int numOfRobots = 32; 
		
		//Regions: r_1, ..., r_n
		int numOfRegions = 4;
		
		//Modes: m_1, ..., m_p
		int numOfModes = 2;
		
		//Distribution of robots between different regions and modes are captured by a vector x of size n*p, 
		//where x[i][j] refers to density of robots in region r_i and mode m_p
		int densityVectorLength = numOfRegions * numOfModes;
//		int[][] densityMatrix = new int[numOfRegions][numOfModes];
		
		
		//Tasks are represented using a set of boolean variables, one for each task
		int numOfTasks = 2;
		Variable[] tasks = new Variable[numOfTasks];
		for(int i=0; i<numOfTasks; i++){
			tasks[i] = new Variable(bdd, "Task"+i);
		}
		Variable.createPrimeVariables(bdd, tasks);
		
		//create boolean variables that encode densities
		ArrayList<Variable[]> densityVars = new ArrayList<Variable[]>();
		int densityVarsLength = UtilityMethods.numOfBits(numOfRobots);
		for(int i=0; i<densityVectorLength; i++){
			Variable[] density_i = Variable.createVariablesAndTheirPrimedCopy(bdd, densityVarsLength, "x"+i);
			densityVars.add(density_i);
		}
		
		//Commands
		//Let us assume each command is executed in a single step
		//TODO: commands that need multiple steps
		//TODO: parametric commands
		//each command has a precondition with two components: resources and tasks, and changes the 
		//distribution and tasks state in the following step
		
		//command 1: move two robots from region r_1 to r_3
		int[] c1_densityPre = {2,0,0,0,0,0,0,0};
		
		int[] c1_densityPost = {0,0,0,0,2,0,0,0};
		
		Command c1 = new Command("c1", c1_densityPre, c1_densityPost);
		
		//command 2: move two robots from region r_2 to r_3
		int[] c2_densityPre = {0,0,2,0,0,0,0,0};
		
		int[] c2_densityPost = {0,0,0,0,2,0,0,0};
		
		Command c2 = new Command("c2", c2_densityPre, c2_densityPost);
		
		//command 3 and 4: collect object 1 to perform task t1
		int[] c3_densityPre = {2,0,0,0,0,0,0,0};
		
		int[] c3_densityPost = {0,2,0,0,0,0,0,0};
		
		Variable[] c3_tasks = new Variable[]{tasks[0]};
		
		Command c3 = new Command("c3", c3_tasks, new boolean[]{false}, new boolean[]{false}, c3_densityPre, c3_densityPost);
		
		int[] c4_densityPre = {0,2,0,0,0,0,0,0};
		
		int[] c4_densityPost = {2,0,0,0,0,0,0,0};
		
		Variable[] c4_tasks = new Variable[]{tasks[0]};
		
		Command c4 = new Command("c4", c4_tasks, new boolean[]{false}, new boolean[]{true}, c4_densityPre, c4_densityPost);
		
		//command 5 & 6: collect object 2 to perform task t2
		int[] c5_densityPre = {0,0,2,0,0,0,0,0};
		
		int[] c5_densityPost = {0,0,0,2,0,0,0,0};
		
		Variable[] c5_tasks = new Variable[]{tasks[1]};
		
		Command c5 = new Command("c5", c5_tasks, new boolean[]{false}, new boolean[]{false}, c5_densityPre, c5_densityPost);
		
		int[] c6_densityPre = {0,0,0,2,0,0,0,0};
		
		int[] c6_densityPost = {0,0,2,0,0,0,0,0};
		
		Variable[] c6_tasks = new Variable[]{tasks[1]};
		
		Command c6 = new Command("c6", c6_tasks, new boolean[]{false}, new boolean[]{true}, c6_densityPre, c6_densityPost);
		
		//command 7: move two robots from r_3 to r_4
		int[] c7_densityPre = {0,0,0,0,2,0,0,0};
		
		int[] c7_densityPost = {0,0,0,0,0,0,2,0};
		
		Command c7 = new Command("c7", c7_densityPre, c7_densityPost);
		
		Command[] commands = {c1,c2,c3,c4,c5,c6,c7};
		
//		ArrayList<Command> cSet1 = new ArrayList<Command>();
//		cSet1.add(c1);
		
//		int c1Formula = commandsSetFormula(bdd, cSet1, numOfRobots, densityVars);
//		UtilityMethods.debugBDDMethods(bdd, "c1 formula", c1Formula);
//		UtilityMethods.getUserInput();
		
		//***obtain the symbolic representation of the game***
		//Compute the set of maximally consistent commands
			
				
		//obtain all sets of consistent commands
		t0 = UtilityMethods.timeStamp();
		ArrayList<Command> commandsSet = UtilityMethods.arrayToArrayList(commands);	
		ArrayList<ArrayList<Command>> consistentCommands = consistentCommands(bdd, commandsSet, numOfRobots);
		UtilityMethods.duration(t0, "the set of consistent commands computed");
		
//		for(ArrayList<Command> consSet : consistentCommands){
//			for(Command c : consSet){
//				c.print();
//			}
//			System.out.println();
//		}
		
		//create action variables corresponding to the consistent commands
		int numOfActionVars = UtilityMethods.numOfBits(consistentCommands.size());
		Variable[] actionVars = Variable.createVariables(bdd, numOfActionVars, "act");
		
				
		//compute the symbolic representation of the game structure using sets of consistent commands
		//Note: if a set of consistent commands do not affect a subset of tasks, then the state of those tasks remain unchanged
		t0 = UtilityMethods.timeStamp();
		int sysTrans = bdd.ref(bdd.getZero());
		for(int i=0; i< consistentCommands.size(); i++){
			ArrayList<Command> consSet = consistentCommands.get(i);
			int commandSetFormula = commandsSetFormula(bdd, consSet, numOfRobots, densityVars);
			int action = BDDWrapper.assign(bdd, i, actionVars);
			int trans = BDDWrapper.and(bdd, commandSetFormula, action);
			sysTrans = BDDWrapper.orTo(bdd, sysTrans, trans);
			BDDWrapper.free(bdd, commandSetFormula);
			BDDWrapper.free(bdd, action);
			BDDWrapper.free(bdd, trans);
		}
		
		Variable[] gameVars = null;
		gameVars = Variable.unionVariables(gameVars, tasks);
		for(int i=0; i<densityVars.size(); i++){
			gameVars = Variable.unionVariables(gameVars, densityVars.get(i));
		}
		
		int envTrans = BDDWrapper.same(bdd, gameVars);
		
		//specify the initial state
		int init = BDDWrapper.assign(bdd, 0, tasks);
		for(int i=0; i<densityVars.size(); i++){
			if(i==0 || i==2){
				int initDens = BDDWrapper.assign(bdd, numOfRobots/2, densityVars.get(i));
				init = BDDWrapper.andTo(bdd, init, initDens);
				BDDWrapper.free(bdd, initDens);
			}
			
			int initDens = BDDWrapper.assign(bdd, 0, densityVars.get(i));
			init = BDDWrapper.andTo(bdd, init, initDens);
			BDDWrapper.free(bdd, initDens);
		}
		
		GameStructure gs = new GameStructure(bdd, gameVars, Variable.getPrimedCopy(gameVars), init, envTrans, sysTrans, actionVars, actionVars);
		UtilityMethods.duration(t0, "the game structure constructed");
		
		
		//***specify the objective***
		// the objective is defined over tasks and density predicates 
		
		//num of robots in r_3 < 5
		t0 = UtilityMethods.timeStamp();
		int safetyObj = bdd.ref(bdd.getZero());
		for(int i=0; i<5; i++){
			int dens = BDDWrapper.assign(bdd, i, densityVars.get(4));
			safetyObj = BDDWrapper.orTo(bdd, safetyObj, dens);
			BDDWrapper.free(bdd, dens);
		}
		
		//all robots must reach r4
		int reachObj = BDDWrapper.assign(bdd, numOfRobots, densityVars.get(6));
		
		UtilityMethods.duration(t0, "objective computed");
		
		
		//****Solve the game and generate the strategy
		t0 = UtilityMethods.timeStamp();
		GameSolution sol = GameSolver.solveSafetyReachabilityObjectives(bdd, gs, safetyObj, reachObj);
		
		sol.print();
		
		UtilityMethods.duration(t0, "game solved");

	}
	
	//TODO: implement
	public static ArrayList<ArrayList<Command>> maximallyConsistentCommands(BDD bdd, ArrayList<Command> commandsSet, int numOfAgents){
		ArrayList<ArrayList<Command>> maximallyConsistentCommands = new ArrayList<ArrayList<Command>>();
		return null;
	}
	
	public static ArrayList<ArrayList<Command>> consistentCommands(BDD bdd, ArrayList<Command> commands, int numOfAgents){
		
		ArrayList<ArrayList<Command>> result = new ArrayList<ArrayList<Command>>();
		int numOfSubsets = (int) Math.pow(2,  commands.size())-1;
		int numOfBits = commands.size();
		for(int i=1; i<numOfSubsets; i++){
			String operand = Integer.toBinaryString(i);
			int diff = numOfBits-operand.length();
			if(operand.length() < numOfBits){
				for(int j=0;j<diff;j++){
					operand = "0"+operand;
				}
			}
			
			ArrayList<Command> currentSet = new ArrayList<Command>();
			for(int j=0; j<operand.length();j++){
				if(operand.charAt(j)=='1'){
					currentSet.add(commands.get(j));
				}
			}
			
			if(isCommandsSetConsistent(bdd, currentSet, numOfAgents)){
				result.add(currentSet);
			}
		}
		
		return result;
	}
	
	/**
	 * Are the given commands consistent with each other? Can be executed in parallel at the same state 
	 * @param commands
	 * @return
	 */
	public static boolean isCommandsSetConsistent(BDD bdd, ArrayList<Command> commands, int numOfAgents){
		
		
		//check tasks
		int tasksPre = bdd.ref(bdd.getOne());
		int tasksPost = bdd.ref(bdd.getOne());
		for(Command c: commands){
			int c_taskPre = c.getTaskStateFormula(bdd);
			
			tasksPre = BDDWrapper.andTo(bdd, tasksPre, c_taskPre);
			
			if(tasksPre == 0){
				return false;
			}
			
			int c_taskPost = c.getNextTaskStateFormula(bdd);
			
			tasksPost = BDDWrapper.andTo(bdd, tasksPost, c_taskPost);
			
			if(tasksPost == 0){
				return false;
			}
		}
		
		//check densities
		int numOfRobotsRequiredForCommands = 0;
		for(Command c : commands){
			int[] c_density = c.getDensityVector();
			//sum the number of robots required to perform command c
			for(int i=0;i<c_density.length; i++){
				numOfRobotsRequiredForCommands += c_density[i];
			}
		}
		
		//if number of robots required is more than the existing agents, then commands set is inconsistent
		if(numOfRobotsRequiredForCommands>numOfAgents){
			return false;
		}
		
		
		
		return true;
	}
	
	public static int commandsSetFormula(BDD bdd, ArrayList<Command> commands, int numOfAgents, ArrayList<Variable[]> densityVars){
		int commandFormula = bdd.ref(bdd.getOne());
		
		//tasksFormula
		int tasksPre = bdd.ref(bdd.getOne());
		int tasksPost = bdd.ref(bdd.getOne());
		for(Command c: commands){
			int c_taskPre = c.getTaskStateFormula(bdd);
					
			tasksPre = BDDWrapper.andTo(bdd, tasksPre, c_taskPre);
					
			int c_taskPost = c.getNextTaskStateFormula(bdd);
					
			tasksPost = BDDWrapper.andTo(bdd, tasksPost, c_taskPost);		
		}
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, tasksPre);
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, tasksPost);
		BDDWrapper.free(bdd, tasksPre);
		BDDWrapper.free(bdd, tasksPost);
		
		//if command formula is false, there is no need to continue, return it because the set of commands are inconsistent
		if(commandFormula == 0){
			return commandFormula;
		}
				
		//Density formula
		int numOfRobotsRequiredForCommands = 0;
		int[] densityVector = createZeroVector(densityVars.size());
		int[] nextDensityVector = createZeroVector(densityVars.size());
		
		for(Command c : commands){
			int[] c_density = c.getDensityVector();
			int[] next_c_density = c.getNextDensityVector();
			
			densityVector = addVectors(densityVector, c_density);
			nextDensityVector = addVectors(nextDensityVector, next_c_density);
			
			//sum the number of robots required to perform command c
			for(int i=0;i<c_density.length; i++){
				numOfRobotsRequiredForCommands += c_density[i];
			}
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
		
		//x_0 + ... + x_k = N
		int agentsConservationFormula = agentsConservationConstraint(bdd, densityVars, numOfAgents);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, agentsConservationFormula);
		BDDWrapper.free(bdd, agentsConservationFormula);
		
		//x'_i = x_i +d'_i - d_i
		int nextDensityFormula = nextDensityFormula(bdd, densityVars, densityVector, nextDensityVector);
		densityFormula = BDDWrapper.andTo(bdd, densityFormula, nextDensityFormula);
		BDDWrapper.free(bdd, nextDensityFormula);
		
		commandFormula = BDDWrapper.andTo(bdd, commandFormula, densityFormula);
				
		return commandFormula;
	}
	
	//TODO: implement symbolically
	public static int agentsConservationConstraint(BDD bdd, ArrayList<Variable[]> densityVars, int numOfAgents){
		int result= bdd.ref(bdd.getZero());
		result= agentsConservationConstraint(bdd, densityVars, 0, numOfAgents);
		return result;
	}
	
	public static int agentsConservationConstraint(BDD bdd, ArrayList<Variable[]> densityVars, int index, int numOfRemainingAgents){
		int result = bdd.ref(bdd.getZero());
		
		if(index == densityVars.size()-1){
			Variable[] variable = densityVars.get(index);
			int f = BDDWrapper.assign(bdd, numOfRemainingAgents, variable);
			result=BDDWrapper.orTo(bdd, result, f);
			BDDWrapper.free(bdd, f);
			return result;
		}
		
		for(int i=0; i<=numOfRemainingAgents; i++){
			Variable[] variable = densityVars.get(index);
			int f = BDDWrapper.assign(bdd, i, variable);
			int f2 = agentsConservationConstraint(bdd, densityVars, index+1, numOfRemainingAgents - i);
			int den = BDDWrapper.and(bdd, f, f2);
			result=BDDWrapper.orTo(bdd, result, den);
			BDDWrapper.free(bdd, f);
			BDDWrapper.free(bdd, f2);
			BDDWrapper.free(bdd, den);
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
	
	public static int[] addVectors(int[] vec1, int[] vec2){
		if(vec1.length != vec2.length){
			System.err.println("the length of two vectors is not equivalent");
			return null;
		}
		int[] result = new int[vec1.length];
		for(int i = 0; i<result.length; i++){
			result[i] = vec1[i]+vec2[i];
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
	
	public static int densityLowerBoundConstraints(BDD bdd, ArrayList<Variable[]> densityVars, int[] lowerBounds, int upperBound){
		int result= bdd.ref(bdd.getOne());
		for(int i=0; i<densityVars.size();i++){
			int x_i = densityLowerBoundCondition(bdd, densityVars.get(i), lowerBounds[i], upperBound);
			result = BDDWrapper.andTo(bdd, result, x_i);
			BDDWrapper.free(bdd, x_i);
		}
		return result;
	}
	
	

}
