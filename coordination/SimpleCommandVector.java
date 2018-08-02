package coordination;

import game.Variable;
import jdd.bdd.BDD;

/**
 * A simple command vector is a low level model of the command. 
 * The high-level description of commands are going to be transformed into lower level commands 
 * that are then used to generate a game structure. 
 * A simple command vector has four parts: 
 * Init: initial state of tasks and initial resources required for being able to execute the command
 * Intermediate: the intermediate states of a command where the execution is started
 * Final_success: the states where the execution of command is finished with success
 * Final_failure: the states where the execution of the command is finished with failure
 * @author moarref
 *
 */
public class SimpleCommandVector {
	
	String id; 
	
	BDD bdd; 
	
	//tasks that are affected by executing this command
	Variable[] affectedTasks = null;
	
	int tasksInit = -1; 
	int[] initDensity; 
	
	int tasksIntermediate;
	int[] intermediateDensity; 
	
	int tasksFinal_success = -1; 
	int[] finalSuccessDensity; 
	
	int tasksFinal_failure = -1;
	int[] finalFailureDensity;
	
	public SimpleCommandVector(String argId, BDD argBDD, Variable[] argAffectedTasks, 
			int argTaskInit, int[] argInitDensity, 
			int argTasksIntermediate, int[] argIntermediateDensity, 
			int argTasksFinal_success, int[] argFinalSuccessDensity,
			int argTasksFinal_failure, int[] argFinalFailureDensity){
		
		id = argId;
		bdd = argBDD; 
		
		affectedTasks = argAffectedTasks;
		
		tasksInit = argTaskInit;
		initDensity = argInitDensity;
		
		tasksIntermediate = argTasksIntermediate;
		intermediateDensity = argIntermediateDensity; 
		
		tasksFinal_success = argTasksFinal_success;
		finalSuccessDensity = argFinalSuccessDensity;
		
		tasksFinal_failure = argTasksFinal_failure;
		finalFailureDensity = argFinalFailureDensity;
	}
	
	public SimpleCommandVector(String argId, BDD argBDD, int[] argInitDensity,
			int[] argIntermediateDensity, 
			int[] argFinalSuccessDensity,
			int[] argFinalFailureDensity){
		
		id = argId;
		bdd = argBDD; 
		
		tasksInit = bdd.ref(bdd.getOne());
		initDensity = argInitDensity;
		
		tasksIntermediate = bdd.ref(bdd.getOne());
		intermediateDensity = argIntermediateDensity; 
		
		tasksFinal_success = bdd.ref(bdd.getOne());
		finalSuccessDensity = argFinalSuccessDensity;
		
		tasksFinal_failure = bdd.ref(bdd.getOne());
		finalFailureDensity = argFinalFailureDensity;
	}
	
	public String getID(){
		return id;
	}
	
	public Variable[] getAffectedTasks(){
		return affectedTasks;
	}
	
	public int getInitTasksCondition(){
		return tasksInit;
	}
	
	public int[] getInitDensity(){
		return initDensity;
	}
	
	public int getIntermediateTasksCondition(){
		return tasksIntermediate;
	}
	
	public int[] getIntermediateDensity(){
		return intermediateDensity;
	}
	
	public int getFinalSuccessTaskCondition(){
		return tasksFinal_success;
	}
	
	public int[] getFinalSuccessDensity(){
		return finalSuccessDensity;
	}
	
	public int getFinalFailureTaskCondition(){
		return tasksFinal_failure;
	}
	
	public int[] getFinalFailureDensity(){
		return finalFailureDensity;
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
