package coordination;

import jdd.bdd.BDD;
import utils.UtilityMethods;
import game.BDDWrapper;
import game.Variable;

public class Command {
	
	String id;
	
	//tasks that are affected by executing this command
	Variable[] affectedTasks;
	
	//tasks current and next state for this command
	boolean[] tasksState;
	boolean[] nextTasksState;
	
	int taskStateFormula = -1;
	int nextTaskStateFormula = -1;
	
	int[] density;
	int[] densityPrime;
	
	public Command(String argId, Variable[] argTasks, boolean[] argTasksState, boolean[] argNextTasksState, int[] argDensity, 	
			int[] argNextDensity){
		affectedTasks = argTasks;
		tasksState = argTasksState;
		nextTasksState = argNextTasksState;
		density = argDensity;
		densityPrime = argNextDensity;
		id = argId;
	}
	
	/**
	 * When executing the command does not influence the tasks state
	 * @param argDensity
	 * @param argNextDensity
	 */
	public Command(String argId, int[] argDensity, int[] argNextDensity){
		affectedTasks = null;
		tasksState = null;
		nextTasksState = null;
		density = argDensity;
		densityPrime = argNextDensity;
		id = argId;
	}
	
	public int[] getDensityVector(){
		return density;
	}
	
	public int[] getNextDensityVector(){
		return densityPrime;
	}
	
	public Variable[] getTaskVariables(){
		return affectedTasks;
	}
	
	public Variable[] getTaskVariablesPrime(){
		return Variable.getPrimedCopy(affectedTasks);
	}
	
	public boolean[] getCurrentTaskState(){
		return tasksState;
	}
	
	public boolean[] getNextTasksState(){
		return nextTasksState;
	}
	
	public int getTaskStateFormula(BDD bdd){
		if(taskStateFormula == -1){
			if(affectedTasks == null){
				taskStateFormula = bdd.ref(bdd.getOne());
			}else{
				return BDDWrapper.assign(bdd, tasksState, affectedTasks);
			}
		}
		return taskStateFormula;
	}
	
	public int getNextTaskStateFormula(BDD bdd){
		if(nextTaskStateFormula == -1){
			if(affectedTasks == null){
				nextTaskStateFormula = bdd.ref(bdd.getOne());
			}else{
				return BDDWrapper.assign(bdd, nextTasksState, Variable.getPrimedCopy(affectedTasks));
			}
		}
		return nextTaskStateFormula;
	}
	
	public void print(){
		System.out.print(id+" ");
	}
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
