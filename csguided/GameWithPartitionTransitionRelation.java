package csguided;

import java.util.HashMap;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;

public class GameWithPartitionTransitionRelation {
	
	Variable[] inputVariables; //input variables of the transition system
	Variable[] outputVariables; //output variables of the transition system
	Variable[] inputPrimeVariables; //primed version of the input variables
	Variable[] outputPrimeVariables;
	
	Variable[] variables;
	Variable[] primedVariables;
	
	int init; //bdd representing the initial states of the transition system
	PartitionTransitionRelation T_env;
	PartitionTransitionRelation T_sys;
	
	BDD bdd; //bdd object managing the binary decision diagrams
	Permutation vTovPrime; //permutation from variables to their prime version
	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
	
	int variablesCube;
	int primeVariablesCube;
	int inputVarsCube;
	int outputVarsCube;
	int primeInputVarsCube;
	int primeOutputVarsCube;
	
	//actions 
	
	Variable[] actionVars;//TODO: this is actually action bits, rename it later
	int[] sysAvlActions;
	int[] envAvlActions;
	int actionsCube;
	
	//for more natural visual representation of states and actions in the game graph
	protected HashMap<String, String> actionMap;
	protected HashMap<String, String> stateMap;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
