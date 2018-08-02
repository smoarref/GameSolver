package csguided;

import jdd.bdd.Permutation;

public class ConjunctivePartitionTransitionRelation extends PartitionTransitionRelation{

	public ConjunctivePartitionTransitionRelation(BDDWrapper argBdd,
			int transition, Variable[] argVars, Variable[] argActions,
			Variable[] argPrimedVars, Permutation argVtoVPrime,
			Permutation argVprimeToV, int argVariablesCube, int argActionsCube,
			int argPrimeVarsCube) {
		super(argBdd, transition, argVars, argActions, argPrimedVars, argVtoVPrime,
				argVprimeToV, argVariablesCube, argActionsCube, argPrimeVarsCube);
		// TODO Auto-generated constructor stub
	}

}
