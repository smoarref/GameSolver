package specification;

import java.util.ArrayList;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import utils.UtilityMethods;
import game.BDDWrapper;
import game.Variable;

public class Agent {
	//@assumption: agents are separable, they only control their own controllable variables.
	//TODO: do we need to consider uncontrollable variables?
	
	/**
	 * 
	 */
	protected BDD bdd;

	/**
	 * name of the agent
	 */
	protected String name ;
	
	/**
	 * type of the agent
	 */
	protected AgentType type;
	
	/**
	 * variables controlled by the agent
	 */
	protected Variable[] variables;
	
	/**
	 * primed copy of the variables controlled by the agent
	 */
	protected Variable[] primedVariables;
	
	/**
	 * action variables of the agent
	 */
	protected Variable[] actionVars;
	
	/**
	 * actions
	 */
	
	protected int[] actions = null;
	
	/**
	 * transition relation of the agent
	 */
	protected int transitionRelation;
	
	/**
	 * when the variables stays unchanged during a transition
	 */
	
	protected int same=-1;
	
	/**
	 * initial states of the agent 
	 */
	
	protected int init;
	
	protected int variablesCube=-1;
	
	protected int actionsCube=-1; 
	
	protected int variablesAndActionsCube=-1;
	
	protected Permutation vToVPrime = null;
	
	protected Permutation vPrimeToV = null;
	
	public Permutation getVtoVPrime(){
		if(vToVPrime == null){
			vToVPrime = BDDWrapper.createPermutations(bdd, variables, primedVariables);
		}
		return vToVPrime;
	}
	
	public Permutation getVPrimeToV(){
		if(vPrimeToV == null){
			vPrimeToV = BDDWrapper.createPermutations( bdd, primedVariables, variables);
		}
		return vPrimeToV;
	}
	
	public int getVariablesCube(){
		if(variablesCube == -1){
			variablesCube = BDDWrapper.createCube(bdd, variables);
		}
		return variablesCube;
	}
	
	public int getActionsCube(){
		if(actionsCube == -1){
			actionsCube = BDDWrapper.createCube(bdd, actionVars);
		}
		return actionsCube;
	}
	
	public int getVariablesAndActionsCube(){
		if(variablesAndActionsCube == -1){
			variablesAndActionsCube = BDDWrapper.and(bdd, getVariablesCube(), getActionsCube());
		}
		return variablesAndActionsCube;
	}
	
	public String getName(){
		return name;
	}
	
	public AgentType getType(){
		return type;
	}
	
	public Variable[] getVariables(){
		return variables;
	}
	
	public Variable[] getPrimeVariables(){
		return primedVariables;
	}
	
	public int[] getActions(){
		return actions;
	}
	
	public int getTransitionRelation(){
		return transitionRelation;
	}
	
	public void setTransitionRelation(int t){
		transitionRelation = bdd.ref(t);
	}
	
	public void setTransitionRelationAndFreeOldTransitionRelation(int t){
		BDDWrapper.free(bdd, getTransitionRelation());
		setTransitionRelation(t);
		
	}
	
	public int getInit(){
		return init;
	}
	
	public void setInit(int argInit){
		init = bdd.ref(argInit);
	}
	
	public void setInitAndFreeOldInit(int argInit){
		BDDWrapper.free(bdd, getInit());
		setInit(argInit);
	}
	
	public void setActions(int[] argActions){
		actions = argActions;
	}
	
	public Variable[] getActionVars(){
		return actionVars;
	}
	
	public int getSame(){
		return same;
	}
	
	public void setSame(int argSame){
		same= argSame;
	}
	
	public BDD getBDD(){
		return bdd;
	}
	
	public void print(){
		System.out.println("Printing agent "+this.getName()+" information");
		System.out.println("Type = "+this.getType());
		System.out.println("Variables");
		Variable.printVariables(variables);
		
		System.out.println("Actions");
		Variable.printVariables(actionVars);
		
		System.out.println("init");
//		BDDWrapper.BDDtoFormula(bdd,  init, variables);
		bdd.printSet(init);
		
		System.out.println("transitions");
		bdd.printSet(transitionRelation);
	
	}
	
	public void addVariables(Variable[] newVars){
		variables = Variable.unionVariables(variables, newVars);
		primedVariables = Variable.getPrimedCopy(variables);
		same = BDDWrapper.same(bdd,variables,primedVariables);
	}
	
	
	public Agent(BDD argBdd, String argName, AgentType argType, Variable[] argVars, Variable[] argActionVars, int argTrans, int argInit){
		bdd = argBdd;
		name = argName;
		type = argType;
		variables = argVars;
		primedVariables = Variable.getPrimedCopy(variables);
		actionVars = argActionVars;
		actions = UtilityMethods.enumerate(bdd, actionVars);
		transitionRelation = argTrans;
		init = argInit;
		same = BDDWrapper.same(bdd,variables,primedVariables);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
