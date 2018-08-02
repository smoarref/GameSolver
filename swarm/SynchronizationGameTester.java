package swarm;

import game.Variable;
import jdd.bdd.BDD;

public class SynchronizationGameTester {

	public static void main(String[] args) {
		
		BDD bdd = new BDD(10000, 1000);
		
		//define region propositions for each local program 
		//pi_A^1, pi_B^1
		Variable A1 = Variable.createVariableAndPrimedVariable(bdd, "A1");
		Variable B1 = Variable.createVariableAndPrimedVariable(bdd, "B1");
		
		//pi_C^2, pi_D^2
		Variable C2 = Variable.createVariableAndPrimedVariable(bdd, "C2");
		Variable D2 = Variable.createVariableAndPrimedVariable(bdd, "D2");
		
		//memory variables
		Variable[] M1 = Variable.createVariablesAndTheirPrimedCopy(bdd, 3, "M1");
		Variable[] M2 = Variable.createVariablesAndTheirPrimedCopy(bdd, 3, "M2");
		
		//synchronization signals 
		Variable[] S1 = Variable.createVariablesAndTheirPrimedCopy(bdd, 4, "S1");
		Variable[] S2 = Variable.createVariablesAndTheirPrimedCopy(bdd, 4, "S2");
		
		//advance variables
		Variable a1 = Variable.createVariableAndPrimedVariable(bdd, "a1");
		Variable a2 = Variable.createVariableAndPrimedVariable(bdd, "a2");
		
		//process variables 
		Variable[] alpha = Variable.createVariables(bdd, 2, "P");
		
		
		//input
		//local strategies extended with intermediate states
		int[] localStrategies = new int[2];
		
		//initial state of the game
		
		//objective
		
		//form the synchronization game 
		
		//solve the synchronization game 
		
		
		//output: synchronization skeleton

	}

}
