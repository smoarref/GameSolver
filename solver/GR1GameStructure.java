package solver;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import game.BDDWrapper;
import game.GameStructure;
import game.Variable;

public class GR1GameStructure extends GameStructure{
	
	protected int transitionRelation = -1;

	public GR1GameStructure(BDD argBdd) {
		super(argBdd);
	}
	
	public GR1GameStructure(BDD argBDD, Variable[] inputVars, Variable[] outputVars, int argT_env, int argT_sys){
		super(argBDD);
		init = -1;
		
		//define variables
		inputVariables = inputVars;
		inputPrimeVariables = Variable.getPrimedCopy(inputVars);
		outputVariables = outputVars;
		outputPrimeVariables = Variable.getPrimedCopy(outputVars);
		variables = Variable.unionVariables(inputVariables, outputVariables);
		primedVariables = Variable.getPrimedCopy(variables);
		
		//define permutations
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		inputVarsCube = -1;
		outputVarsCube = -1;
		primeInputVarsCube = -1;
		primeOutputVarsCube = -1;
		variablesCube=-1;
		primeVariablesCube=-1;
		
		//define actions 
		envActionVars = null;
		sysActionVars = null;
		actionVars=null;
		
		//define transition relations
		T_env = bdd.ref(argT_env);
		T_sys = bdd.ref(argT_sys);
		
		
	}
	
	public int getTransitionRelation(){
		if(transitionRelation == -1){
			transitionRelation = BDDWrapper.and(bdd, getEnvironmentTransitionRelation(), getSystemTransitionRelation());
		}
		return transitionRelation;
	}
	
	public int controllablePredecessor(int set){
		if(set == 0){
			return bdd.ref(bdd.getZero());
		}
		
		int epre = BDDWrapper.EpreImage(bdd, set, getPrimeOutputVariablesCube(), getSystemTransitionRelation(), getVtoVprime());
		
		int t = BDDWrapper.implies(bdd, getEnvironmentTransitionRelation(), epre);
		BDDWrapper.free(bdd, epre);
		
		int cpre = BDDWrapper.forall(bdd, t, getPrimeInputVarsCube());
		BDDWrapper.free(bdd, t);
		return cpre;
	}
	
	public int symbolicGameOneStepExecution(int set){
		return BDDWrapper.EpostImage(bdd, set, getVariablesCube(), getTransitionRelation(), getVprimetoV());
	}
	
	public static void main(String[] args){
		
		//test1
		test1();
	}
	
	private static void test1(){
		BDD bdd = new BDD(10000, 1000);
		
		//define variables
		Variable x = Variable.createVariableAndPrimedVariable(bdd, "x");
		Variable y = Variable.createVariableAndPrimedVariable(bdd, "y");
		
		Variable[] inputVars = new Variable[]{x};
		Variable[] outpuVars = new Variable[]{y};
		
		//define transition relations
		
//		//trans 1 -- same
//		int T_env = BDDWrapper.same(bdd, inputVars);
//		int T_sys = BDDWrapper.same(bdd, outpuVars);
		
//		//trans 2 -- toggle
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int r1 = BDDWrapper.and(bdd, y.getBDDVar(), BDDWrapper.not(bdd, y.getPrimedCopy().getBDDVar()));
//		int r2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, y.getBDDVar()), y.getPrimedCopy().getBDDVar());
//		int T_sys = BDDWrapper.or(bdd, r1, r2);
		
//		//trans 3 -- env toggle, sys match x'
//		int t1 = BDDWrapper.and(bdd, x.getBDDVar(), BDDWrapper.not(bdd, x.getPrimedCopy().getBDDVar()));
//		int t2 = BDDWrapper.and(bdd, BDDWrapper.not(bdd, x.getBDDVar()), x.getPrimedCopy().getBDDVar());
//		int T_env = BDDWrapper.or(bdd, t1, t2);
//		
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
//		//trans 4 -- env free, sys match 
//		int T_env = bdd.ref(bdd.getOne());
//		int T_sys = BDDWrapper.same(bdd, Variable.getPrimedCopy(inputVars), Variable.getPrimedCopy(outpuVars));
		
		//trans 5 -- env free, sys match 
		int T_env = bdd.ref(bdd.getOne());
		int T_sys = BDDWrapper.same(bdd, inputVars, Variable.getPrimedCopy(outpuVars));
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "environment transition relation", T_env);
		UtilityMethods.debugBDDMethodsAndWait(bdd, "system transition relation", T_sys);
		
		//define the game structure
		GR1GameStructure gs = new GR1GameStructure(bdd, inputVars, outpuVars, T_env, T_sys);
		
		System.out.println("game structure is created!");
		
		//define a set of states 
//		int set = BDDWrapper.and(bdd, x.getBDDVar(), y.getBDDVar());
		
		int set = y.getBDDVar();
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "set of states define", set);
		
		//test cpre operator
		int cpre = gs.controllablePredecessor(set);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "cpre(set) is", cpre);
		
		int cpre2 = gs.controllablePredecessor(cpre);
		
		UtilityMethods.debugBDDMethodsAndWait( bdd, "cpre(cpre) is", cpre2);
		
		//test symbolicOneStepExecution operator
		
		int next1 = gs.symbolicGameOneStepExecution(set);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of set", next1);
		
		int next2 = gs.symbolicGameOneStepExecution(cpre);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of cpre(set)", next2);
		
		int next3 = gs.symbolicGameOneStepExecution(cpre2);
		
		UtilityMethods.debugBDDMethodsAndWait(bdd, "one step execution of cpre(cpre)", next3);
	}

}
