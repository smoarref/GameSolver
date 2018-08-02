package game;

import jdd.bdd.BDD;

/**
 * to represent a winning strategy for the system player
 * @author moarref
 *
 */
public class Controller {
	protected BDD bdd;
	protected Variable[] inputVariables;
	protected Variable[] outputVariables;
	protected Variable[] vars;
	protected Variable[] actionVars;
	
	protected int init;
	protected int transitionRelation;
	protected int finalStates;
	
	public Controller(BDD argBDD, Variable[] argVars, Variable[] argActions, int argInit, int argTrans, int argFinalStates){
		bdd = argBDD;
		vars = argVars;
		actionVars = argActions;
		init = argInit;
		transitionRelation = argTrans;
		finalStates = argFinalStates;
	}
	
	public BDD getBDD(){
		return bdd;
	}
	
	public int getInit(){
		return init;
	}
	
	public int getTransitionRelation(){
		return transitionRelation;
	}
	
	public Variable[] getInputVariables(){
		return inputVariables;
	}
	
	public Variable[] getOutputVariables(){
		return outputVariables;
	}
	
	public Variable[] getVariables(){
		return vars;
	}
	
	
	public Variable[] getActionVars(){
		return actionVars;
	}
	
	public int getFinalStates(){
		return finalStates;
	}
}
