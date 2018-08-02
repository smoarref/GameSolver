package coordination;

import game.Variable;
import jdd.bdd.BDD;

public class SimpleCommand {
	String id; 
	
	BDD bdd;
	
	int numOfRequiredRobots;
	
	//tasks that are affected by executing this command
	Variable[] affectedTasks = null;
	
	int tasksInit = -1; 
	Variable[] initDensity; 
	
	int tasksIntermediate;
	Variable[] intermediateDensity; 
	
	int tasksFinal_success = -1; 
	Variable[] finalSuccessDensity; 
	
	int tasksFinal_failure = -1;
	Variable[] finalFailureDensity;
	
	public SimpleCommand(String argId, BDD argBDD, Variable[] argAffectedTasks, 
			int argNumOfRequiredRobots, 
			int argTaskInit, Variable[] argInitDensity, 
			int argTasksIntermediate, Variable[] argIntermediateDensity, 
			int argTasksFinal_success, Variable[] argFinalSuccessDensity,
			int argTasksFinal_failure, Variable[] argFinalFailureDensity){
		
		id = argId;
		bdd = argBDD; 
		
		numOfRequiredRobots = argNumOfRequiredRobots;
		
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
	
	public SimpleCommand(String argId, BDD argBDD,
			int argNumOfRequiredRobots, 
			Variable[] argInitDensity,
			Variable[] argIntermediateDensity, 
			Variable[] argFinalSuccessDensity,
			Variable[] argFinalFailureDensity){
		
		id = argId;
		bdd = argBDD; 
		
		numOfRequiredRobots = argNumOfRequiredRobots;
		
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
	
	public Variable[] getInitDensity(){
		return initDensity;
	}
	
	public int getIntermediateTasksCondition(){
		return tasksIntermediate;
	}
	
	public Variable[] getIntermediateDensity(){
		return intermediateDensity;
	}
	
	public int getFinalSuccessTaskCondition(){
		return tasksFinal_success;
	}
	
	public Variable[] getFinalSuccessDensity(){
		return finalSuccessDensity;
	}
	
	public int getFinalFailureTaskCondition(){
		return tasksFinal_failure;
	}
	
	public Variable[] getFinalFailureDensity(){
		return finalFailureDensity;
	}
	
	public int getNumberOfRequiredRobots(){
		return numOfRequiredRobots;
	}
}
