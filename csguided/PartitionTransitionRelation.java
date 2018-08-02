package csguided;

import java.util.ArrayList;

import jdd.bdd.Permutation;

public class PartitionTransitionRelation extends TransitionRelation{

	public PartitionTransitionRelation(BDDWrapper argBdd, int transition,
			Variable[] argVars, Variable[] argActions,
			Variable[] argPrimedVars, Permutation argVtoVPrime,
			Permutation argVprimeToV, int argVariablesCube, int argActionsCube,
			int argPrimeVarsCube) {
		super(argBdd, transition, argVars, argActions, argPrimedVars, argVtoVPrime,
				argVprimeToV, argVariablesCube, argActionsCube, argPrimeVarsCube);
		// TODO Auto-generated constructor stub
	}

	ArrayList<TransitionRelation> transitions;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
