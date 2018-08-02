package game;

import java.util.ArrayList;

import utils.UtilityMethods;
import jdd.bdd.BDD;

public class Variable {
	
	public static ArrayList<Variable> allVars = new ArrayList<Variable>();
	
	private BDD bdd;
	private String name;
	private char type;
	private int bddPosition;// the index of the variable in the bdd
	private int bddVar;
	private Variable primedCopy;
	
	
	public Variable(BDD argBdd, String argName, char argType, Variable argPrime){
		bdd=argBdd;
		name=argName;
		type=argType;
		bddPosition=bdd.numberOfVariables();
		bddVar=bdd.createVar();
		primedCopy = argPrime;
		
		allVars.add(this);
	}
	
	public Variable(BDD argBdd, String argName, char argType){
		bdd=argBdd;
		name=argName;
		type=argType;
		bddPosition=bdd.numberOfVariables();
		bddVar=bdd.createVar();
		primedCopy=null;
		
		allVars.add(this);
	}
	
	public Variable(BDD argBdd, String argName){
		bdd=argBdd;
		name=argName;
		type='u';//unknown
		bddPosition=bdd.numberOfVariables();
		bddVar=bdd.createVar();
		primedCopy=null;
		
		allVars.add(this);
	}
	

	
	public Variable getPrimedCopy(){
		return primedCopy;
	}
	
	
	public void setPrimedCopy(Variable copy){
		primedCopy  = copy;
	}
	
	public char getType(){
		return type;
	}
	
	public int getBDDPosition(){
		return bddPosition;
	}
	
	public String getName(){
		return name;
	}
	
	public int getBDDVar(){
		return bddVar;
	}
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000,1000);
		Variable a = new Variable(bdd,"a");
		
		System.out.println("variable "+a.getName()+" has position "+a.getBDDPosition());
		
		Variable b = new Variable(bdd, "b");
		System.out.println("variable "+b.getName()+" has position "+b.getBDDPosition());
		
		Variable c = new Variable(bdd, "c");
		System.out.println("variable "+c.getName()+" has position "+c.getBDDPosition());
	}
	
	
	public static int[] getBDDVars(Variable[] vars){
		if(vars==null){
			return null;
		}
		int[] bddVars=new int[vars.length];
		for(int i=0;i<vars.length;i++){
			bddVars[i]=vars[i].getBDDVar();
		}
		return bddVars;
	}
	
	public String toString(){
		return "Variable "+getName()+", with type='"+getType()+"', and BDD position "+getBDDPosition();
	}
	
	public void print(){
		System.out.println("Variable "+getName()+", with type='"+getType()+"', and BDD position "+getBDDPosition());
	}
	
	public static void printVariables(Variable[] vars){
		if(vars == null || vars.length == 0 ){
			System.out.println("no variables");
			return;
		}
		for(Variable v : vars){
			System.out.println(v.toString());
		}
	}
	
	public boolean compareTo(Variable v){
		if(bddPosition == v.getBDDPosition()){
			return true;
		}
		return false;
	}
	
//	public static Variable[] createVariables(BDD bdd, String namePrefix, int length){
//		Variable[] vars = new Variable[length];
//		for(int i=0;i<length;i++){
//			vars[i]= new Variable(bdd, namePrefix+i);
//		}
//		return vars;
//	}
	
	public static Variable[] createVariablesAndTheirPrimedCopies_interleaving(BDD bdd, int numOfVars, String namePrefix){
		Variable[] vars=new Variable[numOfVars];
		for(int i=0;i<vars.length;i++){
			vars[i]=createVariableAndPrimedVariable(bdd, namePrefix+i);
		}
		return vars;
	}
	
	public static Variable[] createVariables(BDD bdd, int numOfVars, String namePrefix){
		Variable[] vars=new Variable[numOfVars];
		for(int i=0;i<vars.length;i++){
			vars[i]=new Variable(bdd, namePrefix+i);
		}
		return vars;
	}
	
	public static Variable[] createVariablesAndTheirPrimedCopy(BDD bdd, int numOfVars, String namePrefix){
		Variable[] vars = createVariables(bdd, numOfVars, namePrefix);
		createPrimeVariables(bdd, vars);
		return vars;
	}
	
	/**
	 * Given a set of variables vars, creates their prime version and returns it as output.
	 * Naming convention: each prime variables has the name of its corresponding non-primed variable extended with
	 * the word prime, e.g., aPrime for the variable a 
	 * @param bdd
	 * @param vars
	 * @return
	 */
	public static Variable[] createPrimeVariables(BDD bdd, Variable[] vars){
		Variable[] primedVars=new Variable[vars.length];
		for(int i=0;i<vars.length;i++){
			primedVars[i]=new Variable(bdd, vars[i].getName()+"Prime", vars[i].getType(), vars[i]);
			vars[i].setPrimedCopy(primedVars[i]);
		}
		return primedVars;
	}
	
	public static Variable createPrimeVariable(BDD bdd, Variable var){
		Variable primeVar = new Variable(bdd, var.getName()+"Prime", var.getType(), var);
		var.setPrimedCopy(primeVar);
		return primeVar;
	}
	
	public static Variable createVariableAndPrimedVariable(BDD bdd, String name){
		Variable v = new Variable(bdd, name);
		createPrimeVariable(bdd,v);
		return v;
	}
	
	public static Variable[] unionVariables(Variable[] set1, Variable[] set2){
		if(set1 == null || set1.length == 0){
			return set2;
		}else if(set2 == null || set2.length == 0){
			return set1;
		}
		
		ArrayList<Variable> vars = new ArrayList<Variable>();
		for(int i=0; i<set1.length;i++){
			vars.add(set1[i]);
		}
		for(int j=0;j<set2.length;j++){
			if(!vars.contains(set2[j])) vars.add(set2[j]);
		}
		return vars.toArray(new Variable[vars.size()]);
	}
	
	public static Variable[] unionVariables(ArrayList<Variable[]> vars){
		Variable[] union = vars.get(0);
		for(int i=1; i<vars.size();i++){
			union=unionVariables(union, vars.get(i));
		}
		return union;
	}
	
	/**
	 * returns the set of variables in varSet1\varSet2 
	 * @param varSet1
	 * @param varSet2
	 * @return
	 */
	public static Variable[] difference(Variable[] varSet1, Variable[] varSet2){
		if(varSet2 == null || varSet2.length==0){
			return varSet1;
		}
		ArrayList<Variable> diff = new ArrayList<Variable>();
		for(int i=0; i<varSet1.length; i++){
			if(!Variable.containsVariable(varSet2, varSet1[i])){
				diff.add(varSet1[i]);
			}
		}
		return UtilityMethods.variableArrayListToArray(diff);
	}
	
	/**
	 * Checks if the set variables contains the specific var
	 * @param variables
	 * @param var
	 * @return
	 */
	public static boolean containsVariable(Variable[] variables, Variable var){
		for(int i=0;i<variables.length;i++){
			if(variables[i] == var){
				return true;
			}
		}
		return false;
	}
	
	public static Variable getVariable(Variable[] vars, String name){
		for(Variable v : vars){
			if(v.getName().equals(name)){
				return v;
			}
		}
		return null;
	}
	
	public static Variable[] getVariablesStartingWith(Variable[] vars, String namePrefix){
		ArrayList<Variable> result = new ArrayList<Variable>();
		for(Variable v : vars){
			if(v.getName().startsWith(namePrefix)){
				result.add(v);
			}
		}
		Variable[] resultArray = UtilityMethods.variableArrayListToArray(result);
		return resultArray;
	}
	
	public static Variable[] getPrimedCopy(Variable[] variables){
		Variable[] primedCopy=new Variable[variables.length];
		for(int i=0;i<variables.length;i++){
			primedCopy[i]=variables[i].getPrimedCopy();
		}
		return primedCopy;
	}
	
	/**
	 * Creates an enumeration of variables in vars
	 * @param vars
	 * @return
	 */
	public static int[] enumerate(BDD bdd, Variable[] vars){
		return UtilityMethods.enumerate(bdd, Variable.getBDDVars(vars));
	}
	
	/**
	 * Creates variables and their primed copies in the order specified by their names
	 * @param bdd
	 * @param names
	 * @return
	 */
	public static Variable[] createVariablesAndPrimedCopies(BDD bdd, ArrayList<String> names){
		Variable[] variables = new Variable[names.size()];
		for(int i=0;i<names.size();i++){
			variables[i]= createVariableAndPrimedVariable(bdd, names.get(i));
		}
		return variables;
	}
	
	public static Variable[] createVariables(BDD bdd, ArrayList<String> names){
		Variable[] variables = new Variable[names.size()];
		for(int i=0;i<names.size();i++){
			variables[i] = new Variable(bdd, names.get(i));
		}
		return variables;
	}
	
	/**
	 * Creates a set of variables with the same domain and with the given names
	 * @param bdd
	 * @param names
	 * @param bound
	 * @return
	 */
	public static ArrayList<Variable[]> createVariablesWithInterleavingOrder(BDD bdd, ArrayList<String> names, int bound){
		ArrayList<Variable[]> result = new ArrayList<Variable[]>();
		int numOfBits = UtilityMethods.numOfBits(bound);
		
		for(int i=0; i<names.size();i++){
			Variable[] name_vars = new Variable[numOfBits];
			result.add(name_vars);
		}
		
		for(int i=0; i<numOfBits; i++){
			for(int j=0; j<names.size(); j++){
				Variable[] vars_j = result.get(j);
				vars_j[i] = Variable.createVariableAndPrimedVariable(bdd, names.get(j)+"_i");
			}
		}
		return result;
	}
	
}
