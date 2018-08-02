package csguided;

import jdd.bdd.Permutation;

/**
 * Specifies a symbolic transition relation
 * TODO: implement
 * @author moarref
 *
 */
public class TransitionRelation {
	
	//BDD object
	BDDWrapper bdd;
	
	//the transition relation
	int T;
	
	//the transition relation is a boolean combination over a set of variables vars, actions, and a primed 
	//copy of variables varPrime
	
	Variable[] vars;
	Variable[] actionVars;
	Variable[] primedVars;
	
	Permutation vTovPrime; //permutation from variables to their prime version
	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
	
	int variablesCube;
	int actionsCube;
	int primeVariablesCube;
	
	public TransitionRelation(BDDWrapper argBdd, int transition, Variable[] argVars, 
			Variable[] argActions, Variable[] argPrimedVars, Permutation argVtoVPrime, Permutation argVprimeToV,
			int argVariablesCube, int argActionsCube, int argPrimeVarsCube) {
		
		bdd=argBdd;
		T=transition;
		vars=argVars;
		actionVars=argActions;
		primedVars=argPrimedVars;
		vTovPrime = argVtoVPrime;
		vPrimeTov = argVprimeToV;
		variablesCube = argVariablesCube;
		actionsCube = argActionsCube;
		primeVariablesCube = argPrimeVarsCube;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public int Epre(int states){
		return bdd.EpreImage(states, primeVariablesCube, T, vTovPrime);
	}
	
	public int Epre(int states, int action){
		return bdd.EpreImage(states, primeVariablesCube, T, vTovPrime);
	}
	
	public int Apre(int states){
		return -1;
	}
	
	public int Epost(int states){
		return -1;
	}

}
