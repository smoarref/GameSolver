package specification;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import game.Variable;

/**
 * Represents a bounded integer with reference to its boolean encoding
 * @author moarref
 *
 */
public class BoundedIntegerVariable {
	BDD bdd;
	/**
	 * a bounded integer variables v is bounded from below and above
	 *  lowerBound <= v <=upperBound
	 */
	int lowerBound;
	int upperBound;
	String name;
	Variable[] booleanVars;
	
	//assumes that lower bound is zero
	public BoundedIntegerVariable(BDD argBdd, String argName, int argUpperBound) {
		lowerBound = 0; 
		upperBound = argUpperBound;
		name = argName;
		int numOfBits = UtilityMethods.numOfBits(upperBound);
		booleanVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, name);
	}
	
	public BoundedIntegerVariable(BDD argBdd, String argName, int argUpperBound, Variable[] vars) {
		lowerBound = 0; 
		upperBound = argUpperBound;
		name = argName;
		booleanVars = vars;
	}
	
	public int getLowerBound(){
		return lowerBound;
	}
	
	public int getUpperBound(){
		return upperBound;
	}
	
	public String getName(){
		return name;
	}
	
	public Variable[] getBooleanEncoding(){
		return booleanVars;
	}
}
