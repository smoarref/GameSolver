package csguided;

import jdd.bdd.Permutation;

public class DisjunctivePartitionTransitionRelation extends PartitionTransitionRelation {

	public DisjunctivePartitionTransitionRelation(BDDWrapper argBdd,
			int transition, Variable[] argVars, Variable[] argActions,
			Variable[] argPrimedVars, Permutation argVtoVPrime,
			Permutation argVprimeToV, int argVariablesCube, int argActionsCube,
			int argPrimeVarsCube) {
		super(argBdd, transition, argVars, argActions, argPrimedVars, argVtoVPrime,
				argVprimeToV, argVariablesCube, argActionsCube, argPrimeVarsCube);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
