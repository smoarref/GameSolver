package csguided;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.omg.CORBA.portable.IndirectionException;

import csguided.Objective.ObjectiveType;
import automaton.GameGraph;
import utils.UtilityMethods;
import jdd.bdd.BDD;

/**
 * generates simple test cases for simple robot motion planning case studies
 * @author moarref
 *
 */
public class RMPTestCaseGenerator {
	
	BDD bdd;
	
	private ArrayList<Variable[]> environmentVars;
	private ArrayList<Variable[]> environmentVarsPrime;
	private ArrayList<ArrayList<Variable[]>> agentsVars;
	private ArrayList<ArrayList<Variable[]>> agentsVarsPrime;
	private ArrayList<Integer> environmentVarsDimensions;
	
	private Variable[] environmentActionVars;
	private ArrayList<Variable[]> agentsActionVars;
	private ArrayList<ArrayList<Integer>> agentsVariablesDimensions;
	Variable[] actionVars;
	
	private Variable[] inputVars;
	private Variable[] inputVarsPrime;
	private Variable[] outputVars;
	private Variable[] outputVarsPrime;
	
	private Variable[] variables;
	private Variable[] primeVariables;
	
	
	//defining init
	ArrayList<Integer> envInitValues;
	int envInit;
	
	ArrayList<ArrayList<Integer>> sysInitValues;
	ArrayList<Integer> agentsInitSets;
	int sysInit;
	
	int init;
	
	
	
	public ArrayList<Variable[]> getEnvironmentVariables(){
		return environmentVars;
	}
	
	public ArrayList<ArrayList<Variable[]>> getSystemVariables(){
		return agentsVars;
	}
	
	public Variable[] getEnvironmentActionVars(){
		return environmentActionVars;
	}
	
	public ArrayList<Variable[]> getAgentsActionVars(){
		return agentsActionVars;
	}
	
	public RMPTestCaseGenerator(BDD argBDD, ArrayList<Integer> envVarsDimensions, ArrayList<ArrayList<Integer>> agentVarsDimensions){
		bdd=argBDD;
		//create BDD Variables
		environmentVarsDimensions=envVarsDimensions;
		agentsVariablesDimensions = agentVarsDimensions;
		createGameVariables();
		createActionVars();
	}
	
	public void createActionVars(){
		int numOfActionVars=UtilityMethods.numOfBits(2*environmentVars.size()-1);
		environmentActionVars=Variable.createVariables(bdd, numOfActionVars, "act_e");
		ArrayList<Variable[]> sysActionVars=new ArrayList<Variable[]>();
		Variable[] agent1ActionVars=Variable.createVariables(bdd, numOfActionVars, "act_s1");
		Variable[] agent2ActionVars=Variable.createVariables(bdd, numOfActionVars, "act_s2");
		sysActionVars.add(agent1ActionVars);
		sysActionVars.add(agent2ActionVars);
		agentsActionVars = sysActionVars;
		actionVars = UtilityMethods.concatenateArrays(environmentActionVars, agent1ActionVars);
		actionVars = UtilityMethods.concatenateArrays(actionVars, agent2ActionVars);
	}
	
	public void createGameVariables(){
		//environment vars
		environmentVars=createVarsFromDimensions("oX", environmentVarsDimensions);
		//system vars
		agentsVars=new ArrayList<ArrayList<Variable[]>>();
		for(int i=0;i<agentsVariablesDimensions.size();i++){
			agentsVars.add(createVarsFromDimensions("rX"+i+"_", agentsVariablesDimensions.get(i)));
		}
		//environment prime vars
		environmentVarsPrime=new ArrayList<Variable[]>();
		for(int i=0;i<environmentVars.size();i++){
			environmentVarsPrime.add(Variable.createPrimeVariables(bdd, environmentVars.get(i)));
		}
		//system prime vars
		agentsVarsPrime=new ArrayList<ArrayList<Variable[]>>();
		for(int i=0;i<agentsVars.size();i++){
			ArrayList<Variable[]> agent_i_primeVars=new ArrayList<Variable[]>();
			ArrayList<Variable[]> agent_i_vars=agentsVars.get(i);
			for(int j=0;j<agent_i_vars.size();j++){
				agent_i_primeVars.add(Variable.createPrimeVariables(bdd, agent_i_vars.get(j)));
			}
			agentsVarsPrime.add(agent_i_primeVars);
		}
		//define the set of input and output variables
		defineInputOutputVars();
	}
	
	private void defineInputOutputVars(){
		inputVars=concatenateArrayList(environmentVars);
		inputVarsPrime=concatenateArrayList(environmentVarsPrime);
		ArrayList<Variable[]> agentsOutputs=new ArrayList<Variable[]>();
		ArrayList<Variable[]> agentsOutputsPrime=new ArrayList<Variable[]>();
		for(int i=0;i<agentsVars.size();i++){
			agentsOutputs.add(concatenateArrayList(agentsVars.get(i)));
			agentsOutputsPrime.add(concatenateArrayList(agentsVarsPrime.get(i)));
		}
		outputVars=concatenateArrayList(agentsOutputs);
		outputVarsPrime=concatenateArrayList(agentsOutputsPrime);
		
		variables=UtilityMethods.concatenateArrays(inputVars, outputVars);
		primeVariables=UtilityMethods.concatenateArrays(inputVarsPrime, outputVarsPrime);
	}
	
	public Variable[] concatenateArrayList(ArrayList<Variable[]> vars){
		if(vars==null || vars.size()==0){
			return null;
		}
		Variable[] result=vars.get(0);
		for(int i=1;i<vars.size();i++){
			result=UtilityMethods.concatenateArrays(result, vars.get(i));
		}
		return result;
		
	}
	
	public ArrayList<Variable[]> createVarsFromDimensions(String namePrefix, ArrayList<Integer> dimensions){
		ArrayList<Variable[]> result=new ArrayList<Variable[]>();
		for(int i=0;i<dimensions.size();i++){
			int dim=dimensions.get(i);
			int logDim=UtilityMethods.numOfBits(dim-1);
			Variable[] vars=Variable.createVariables(bdd, logDim, namePrefix+i+"_");
			result.add(vars);
		}
		return result;
	}
	
	public int createInitSet(ArrayList<Integer> environmentInitValues, ArrayList<ArrayList<Integer>> agentsInitValues){
		init=bdd.ref(bdd.getOne());
		envInitValues=environmentInitValues;
		sysInitValues=agentsInitValues;
		
		envInit = assign(environmentVars, environmentInitValues);
		init = bdd.andTo(init, envInit);
		sysInit=bdd.ref(bdd.getOne());
		agentsInitSets=new ArrayList<Integer>();
		for(int i=0;i<agentsInitValues.size();i++){
			int agent_i_init=assign(agentsVars.get(i), agentsInitValues.get(i));
			agentsInitSets.add(agent_i_init);
			sysInit=bdd.andTo(sysInit, agent_i_init);
		}
		
		init=bdd.andTo(init, sysInit);
		return init;
	}
	
	public int assign(ArrayList<Variable[]> vars, ArrayList<Integer> values){
		if(vars.size()!=values.size()){
			System.err.println("the size of the variables and the values are not the same");
		}
		int result=bdd.ref(bdd.getOne());
		for(int i=0;i<vars.size();i++){
			int tmp=stateToBDD(Variable.getBDDVars(vars.get(i)), values.get(i));
			result=bdd.andTo(result, tmp);
			bdd.deref(tmp);
		}
		return result;
	}
	
	public int stateToBDD(int[] variable, int value){
		int[] num = UtilityMethods.intToBinary(value);
		return stateToBDD(variable, num);
	}
	
	public int stateToBDD(int[] variable, int[] value){
		int result = bdd.ref(bdd.getOne());
		int[] valuePrime=value;
		if(variable.length > value.length){
			//append zero in front of value
			int[] zeroArray=new int[variable.length - value.length];
			for(int i=0; i<zeroArray.length; i++){
				zeroArray[i]=0;
			}
			valuePrime=UtilityMethods.concatenateArrays(zeroArray, valuePrime);
		}
		for(int i=0; i<variable.length;i++){
			int tmp;
			tmp = bdd.ref(valuePrime[i]==0 ? bdd.not(variable[i]) : variable[i]);
			result = bdd.andTo(result, tmp);
			bdd.deref(tmp);
		}
		return result;
	}
	
	/**
	 * generates the formula \forall i x[i] <-> xPrime[i], i.e., the value of the variable stay constant during the transition
	 * @param x
	 * @param xPrime
	 * @return
	 */
	public  int same(int[] x, int[] xPrime){
		int T=bdd.ref(bdd.getOne());
		for(int i=0; i<x.length;i++){
			int xeq=bdd.ref(bdd.biimp(x[i], xPrime[i]));
			T=bdd.andTo(T, xeq);
			bdd.deref(xeq);
		}
		return T;
	}
	
	public int same(ArrayList<Variable[]> vars, ArrayList<Variable[]> varsPrime){
		int T=bdd.ref(bdd.getOne());
		for(int i=0;i<vars.size();i++){
			int same=same(Variable.getBDDVars(vars.get(i)),Variable.getBDDVars(varsPrime.get(i)));
			T=bdd.andTo(T, same);
			bdd.deref(same);
		}
		return T;
	}
	
	/**
	 * var & action -> var'=var+1
	 * var & not-action -> var'=var-1
	 * @param var
	 * @param dimension
	 * @return
	 */
	public int singleStepTransition(Variable[] var, Variable[] varPrime, int dimension, int[] releventActs){
		int T=bdd.ref(bdd.getZero());
		for(int i=0;i<dimension;i++){
			if(i-1>=0){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[0];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i-1);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i+1<dimension){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[1];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i+1);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
		}
		return T;
	}
	
	/**
	 * Single step transition where blocked actions stay put
	 * @param var
	 * @param varPrime
	 * @param dimension
	 * @param releventActs
	 * @return
	 */
	public int singleStepTransitionStayPut(Variable[] var, Variable[] varPrime, int dimension, int[] releventActs){
		int T=bdd.ref(bdd.getZero());
		for(int i=0;i<dimension;i++){
			if(i-1>=0){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[0];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i-1);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i==0){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[0];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i+1<dimension){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[1];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i+1);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i==dimension-1){
				int current=stateToBDD(Variable.getBDDVars(var), i);
				int act=releventActs[1];
				int next=stateToBDD(Variable.getBDDVars(varPrime), i);
				int trans=bdd.ref(bdd.and(current, act));
				trans=bdd.andTo(trans, next);
				T=bdd.orTo(T, trans);
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(trans);
			}
		}
		return T;
	}
	
	public int singleStepTransitionStayPut(ArrayList<Variable[]> vars, ArrayList<Variable[]> varsPrime, ArrayList<Integer> dimensions, Variable[] actions){
		if(vars.size()!=varsPrime.size() || vars.size()!=dimensions.size() ){
			System.err.println("single step transition relation could not be constructed, input is incompatible");
			return -1;
		}
		int transitionRelation=bdd.ref(bdd.getZero());
		int[] actionsBDDVars=Variable.getBDDVars(actions);
		int actionCounter=0;
		for(int i=0;i<vars.size();i++){
			int act0=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int act1=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int releventActs[]={act0,act1};
			int T=singleStepTransitionStayPut(vars.get(i), varsPrime.get(i), dimensions.get(i), releventActs);
			int same=bdd.ref(bdd.getOne());
			for(int j=0;j<vars.size();j++){
				if(i==j) continue;
				int sameVal=same(Variable.getBDDVars(vars.get(j)), Variable.getBDDVars(varsPrime.get(j)));
				same=bdd.andTo(same, sameVal);
				bdd.deref(sameVal);
			}
			T=bdd.andTo(T, same);
			transitionRelation=bdd.orTo(transitionRelation, T);
			bdd.deref(T);
			bdd.deref(same);
		}
		return transitionRelation;
	}
	
	public int singleStepTransition(ArrayList<Variable[]> vars, ArrayList<Variable[]> varsPrime, ArrayList<Integer> dimensions, Variable[] actions){
		if(vars.size()!=varsPrime.size() || vars.size()!=dimensions.size() ){
			System.err.println("single step transition relation could not be constructed, input is incompatible");
			return -1;
		}
		int transitionRelation=bdd.ref(bdd.getZero());
		int[] actionsBDDVars=Variable.getBDDVars(actions);
		int actionCounter=0;
		for(int i=0;i<vars.size();i++){
			int act0=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int act1=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int releventActs[]={act0,act1};
			int T=singleStepTransition(vars.get(i), varsPrime.get(i), dimensions.get(i), releventActs);
			int same=bdd.ref(bdd.getOne());
			for(int j=0;j<vars.size();j++){
				if(i==j) continue;
				int sameVal=same(Variable.getBDDVars(vars.get(j)), Variable.getBDDVars(varsPrime.get(j)));
				same=bdd.andTo(same, sameVal);
				bdd.deref(sameVal);
			}
			T=bdd.andTo(T, same);
			transitionRelation=bdd.orTo(transitionRelation, T);
			bdd.deref(T);
			bdd.deref(same);
		}
		return transitionRelation;
	}
	
	/**
	 * the transition relation that only changes x
	 * @param vars
	 * @param varsPrime
	 * @param dimensions
	 * @param actions
	 * @return
	 */
	public int singleStepTransitionX(ArrayList<Variable[]> vars, ArrayList<Variable[]> varsPrime, ArrayList<Integer> dimensions, Variable[] actions){
		if(vars.size()!=varsPrime.size() || vars.size()!=dimensions.size() ){
			System.err.println("single step transition relation could not be constructed, input is incompatible");
			return -1;
		}
		int transitionRelation=bdd.ref(bdd.getZero());
		int[] actionsBDDVars=Variable.getBDDVars(actions);
		int actionCounter=0;
		int i=0;
			int act0=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int act1=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int releventActs[]={act0,act1};
			int T=singleStepTransition(vars.get(i), varsPrime.get(i), dimensions.get(i), releventActs);
			int same=bdd.ref(bdd.getOne());
			for(int j=0;j<vars.size();j++){
				if(i==j) continue;
				int sameVal=same(Variable.getBDDVars(vars.get(j)), Variable.getBDDVars(varsPrime.get(j)));
				same=bdd.andTo(same, sameVal);
				bdd.deref(sameVal);
			}
			T=bdd.andTo(T, same);
			transitionRelation=bdd.orTo(transitionRelation, T);
			bdd.deref(T);
			bdd.deref(same);
		return transitionRelation;
	}
	
	public ArrayList<Integer> singleStepTransitionXList(ArrayList<Variable[]> vars, ArrayList<Variable[]> varsPrime, ArrayList<Integer> dimensions, Variable[] actions){
		if(vars.size()!=varsPrime.size() || vars.size()!=dimensions.size() ){
			System.err.println("single step transition relation could not be constructed, input is incompatible");
			return null;
		}
		ArrayList<Integer> transitionRelationList=new ArrayList<Integer>();
		int[] actionsBDDVars=Variable.getBDDVars(actions);
		int actionCounter=0;
		int i=0;
			int act0=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int act1=stateToBDD(actionsBDDVars, actionCounter);
			actionCounter++;
			int releventActs[]={act0,act1};
			int T=singleStepTransition(vars.get(i), varsPrime.get(i), dimensions.get(i), releventActs);
			int same=bdd.ref(bdd.getOne());
			for(int j=0;j<vars.size();j++){
				if(i==j) continue;
				int sameVal=same(Variable.getBDDVars(vars.get(j)), Variable.getBDDVars(varsPrime.get(j)));
				same=bdd.andTo(same, sameVal);
				bdd.deref(sameVal);
			}
			T=bdd.andTo(T, same);
			transitionRelationList.add(T);
			bdd.deref(same);
		return transitionRelationList;
	}
	
	public int createEnvTransitionRelation(){
//		int T_env=singleStepTransition(environmentVars, environmentVarsPrime, environmentVarsDimensions, environmentActionVars);
		int T_env=singleStepTransitionX(environmentVars, environmentVarsPrime, environmentVarsDimensions, environmentActionVars);
		return T_env;
	}
	
	public ArrayList<Integer> createEnvTransitionRelationList(){
//		int T_env=singleStepTransition(environmentVars, environmentVarsPrime, environmentVarsDimensions, environmentActionVars);
		ArrayList<Integer> T_envList=singleStepTransitionXList(environmentVars, environmentVarsPrime, environmentVarsDimensions, environmentActionVars);
		return T_envList;
	}
	
	public int[] createAgentsTransitionRelation(){
		int[] agentsTransitionRelations=new int[agentsVars.size()];
		for(int i=0;i<agentsTransitionRelations.length;i++){
			agentsTransitionRelations[i]=singleStepTransitionStayPut(agentsVars.get(i), agentsVarsPrime.get(i), agentsVariablesDimensions.get(i), agentsActionVars.get(i));
		}
		return agentsTransitionRelations;
	}
	
	public Game[] generateGames(ArrayList<Integer> environmentInitValues, ArrayList<ArrayList<Integer>> agentsInitValues){
		//create BDD vars
		//createGameVariables();
		//create init
		int initial=createInitSet(environmentInitValues, agentsInitValues);
		//create environment transition
		int envTrans=createEnvTransitionRelation();
		int envSame=same(environmentVars,environmentVarsPrime);
		//create system transition
		int[] agentsTrans=createAgentsTransitionRelation();
		//create games
		Game[] games=new Game[agentsTrans.length];
		for(int i=0;i<games.length;i++){
			int agent_i_same=same(agentsVars.get(i), agentsVarsPrime.get(i));
			int T_env=bdd.ref(bdd.and(envTrans, agent_i_same));
			int T_sys_i=bdd.ref(bdd.and(agentsTrans[i], envSame));
//			games[i]= new Game(bdd, variables, primeVariables, initial, T_env, T_sys_i, UtilityMethods.concatenateArrays(environmentActionVars, agentsActionVars.get(i)));
			games[i]= new Game(bdd, variables, primeVariables, initial, T_env, T_sys_i, actionVars);
			bdd.deref(agent_i_same);
			bdd.deref(agentsTrans[i]);
		}
		bdd.deref(envSame);
		bdd.deref(envTrans);
		return games;
	}
	
	public static void main(String[] args){
		BDD bdd=new BDD(10000,1000);
//		testFairGameWithReachabilityAndSafetyObjectiveOneAgent(bdd, 2, 14);
//		testWithBoundedObjOneAgent(bdd, 2, 14, 18);
		
//		test1(bdd, 2,8);
//		testWithBoundedObjective(bdd, 2, 12);
		
//		compMotionPrimitivesCaseStudy2(bdd, 2, 7, 14);
		compMotionPrimitivesFairGamesCaseStudy(bdd, 2, 8);
		
	}
	
	public static void testFairGameWithReachabilityAndSafetyObjectiveOneAgent(BDD bdd, int numOfVars, int dim){
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
//			environmentInitValues.add(dim-1);
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent1init.add(1);
			}else{
				agent1init.add(dim-1);
			}
		}
		agentsInitValues.add(agent1init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
		FairGame game = games[0].toFairGame();
		
		
//		game.removeUnreachableStates().toGameGraph().draw("game.dot", 1, 0);
		
		//define objectives
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		
		//create bounded reachability objectives
		
//		int bound=2;
//		int tx1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(0));
//		int tx1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(0));
		int ty1=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
		int ty1Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(0).get(1));
//		int targetStates1=bdd.ref(bdd.and(tx1, ty1));
//		int targetStates1Prime=bdd.ref(bdd.and(tx1Prime, ty1Prime));
		int targetStates1=ty1;
		int targetStates1Prime=ty1Prime;
		
		Objective reachabilityObjective = new Objective(ObjectiveType.Reachability, targetStates1);
		
		
		//safety objective
		int objective1 = tcg.noCollisionObjective(x_e, y_e, x_1, y_1);
		
		Objective safetyObjective = new Objective(ObjectiveType.Safety, objective1);
		
		long t0;
		long t1;
		
//		System.out.println("Solving the reachability first");
//		t0=UtilityMethods.timeStamp();
////		t0 = System.currentTimeMillis();
//		FairGameSolution solution1 = FairGameSolver.solve(bdd, game, reachabilityObjective);
//		solution1.print();
////		solution1.drawWinnerStrategy("obj1.dot", 1, 0);
//
//		UtilityMethods.duration(t0, "reachability objective was computed in ");
//				
//		FairGameSolution solution2 = FairGameSolver.solve(bdd, solution1.strategyOfTheWinner(), safetyObjective);
//		solution2.print();
////		solution2.drawWinnerStrategy("obj2.dot", 1, 0);
//		UtilityMethods.duration(t0, "the computation was done in ");

		
		
	
		System.out.println("Solving the safety first");
		t0=UtilityMethods.timeStamp();
		FairGameSolution solution3 = FairGameSolver.solve(bdd, game, safetyObjective);
		solution3.print();
//		solution3.drawWinnerStrategy("obj3.dot", 1, 0);
		UtilityMethods.duration(t0, "safety objective was computed in ");
				
		FairGameSolution solution4 = FairGameSolver.solve(bdd, solution3.strategyOfTheWinner(), reachabilityObjective);
		solution4.print();
//		solution4.drawWinnerStrategy("obj4.dot", 1, 0);
		UtilityMethods.duration(t0, "the computation was done in ");

		
		BDDWrapper.BDD_Usage(bdd);
	}
	
	//TODO: we need to give the reachability flag as an objective for initial abstraction, inspect why and if it is necessary
	public static void testWithBoundedObjOneAgent(BDD bdd, int numOfVars, int dim, int bound){
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
//			environmentInitValues.add(dim-1);
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent1init.add(1);
			}else{
				agent1init.add(dim-1);
			}
		}
		agentsInitValues.add(agent1init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
		
//		games[0].printGame();
//		UtilityMethods.prompt("game printed");
//		HashMap<String , String> actionMap1 = new HashMap<String, String>();
//		actionMap1.put("0000", "");
//		actionMap1.put("01", "down");
//		actionMap1.put("10", "left");
//		actionMap1.put("11", "right");
		
//		System.out.println("printing games before reachability objectives");
//		games[0].printGame();
		
		//define objectives
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		
		//create bounded reachability objectives
		
//		int bound=2;
//		int tx1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(0));
//		int tx1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(0));
		int ty1=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
		int ty1Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(0).get(1));
//		int targetStates1=bdd.ref(bdd.and(tx1, ty1));
//		int targetStates1Prime=bdd.ref(bdd.and(tx1Prime, ty1Prime));
		int targetStates1=ty1;
		int targetStates1Prime=ty1Prime;
		
		
//		games[0].setActionMap(actionMap1);
		
//		games[0].removeUnreachableStates().toGameGraph().draw("game.dot", 1, 0);
		
		long t0=System.currentTimeMillis();
		
		games[0]=games[0].gameWithBoundedReachabilityObjective(bound, "br1", targetStates1, targetStates1Prime);
//		games[0].printGameVars();
		
		
		
//		games[0].setActionMap(actionMap1);
		
//		games[0].removeUnreachableStates().toGameGraph().draw("gameBounded.dot", 1, 0);
		
//		games[0]=games[0].removeUnreachableStates();
//		games[0].toGameGraph().draw("gameBounded.dot", 1, 0);
//		int deadlocks=games[0].getDeadLockStates(games[0].T_sys);
//		System.out.println("the main game is");
//		games[0].printGame();
		
//		System.out.println("sys vars are");
//		games[0].printGameVars();
		
		
//		int boundBits=UtilityMethods.numOfBits(bound);
//		Variable[] counter=new Variable[boundBits];
//		for(int i=0;i<boundBits;i++){
//			counter[i]=Variable.getVariable(games[0].variables, "ibr1"+i);
//			System.out.println(counter[i].toString());
//		}
//		Variable flag=Variable.getVariable(games[0].variables, "br1f");
//		Variable[] x_e_vars=tcg.environmentVars.get(0);
//		Variable[] x_1_vars=tcg.agentsVars.get(0).get(0);
		
////		int newInit=BDDWrapper.assign(bdd, 0, tcg.environmentVars.get(0));
////		newInit = bdd.andTo(newInit, BDDWrapper.assign(bdd, 1, tcg.agentsVars.get(0).get(0)));
//		
//		int newInit=bdd.ref(bdd.not(BDDWrapper.same(bdd, x_e_vars, x_1_vars)));
//		games[0].init=newInit;
		
		//safety objective
		int objective1 = tcg.noCollisionObjective(x_e, y_e, x_1, y_1);
//		int objective2 = bdd.ref(games[0].variables[8].getBDDVar());
		
//		int objective1=bdd.ref(bdd.not(BDDWrapper.same(bdd, x_e_vars, x_1_vars)));
//		int objective1=bdd.ref(bdd.getOne());
//		//x_e !=x1 
//		int objective1 = BDDWrapper.complement(bdd, tcg.same(x_e, x_1));
		
		int[] objectives = {objective1};
		
//		int init=games[0].init;
//		
//		int flagFormula=bdd.ref(flag.getBDDVar());
//		int counterformula= counter[0].getBDDVar();
//		
//		int counterEq0 = BDDWrapper.assign(bdd, 0, counter);
//		int counterEq1 = BDDWrapper.assign(bdd, 1, counter);
//		int counterEq2 = BDDWrapper.assign(bdd, 2, counter);
//		int counterEq3 = BDDWrapper.assign(bdd, 3, counter);
//		int counterEq4 = BDDWrapper.assign(bdd, 4, counter);
				
//		int[] abstractionPredicates={init,  deadlocks, x_1[1], x_1[0], counterEq0, counterEq1};//, counterformula, counter[1].getBDDVar(), counter[2].getBDDVar(), counter[3].getBDDVar()
		
//		System.out.println("printing games before reachability objectives");
//		games[0].printGame();
		
		
		//TestCaseGenerator.testGames(bdd, games, objectives, abstractionPredicates);
		GameSolver gs1 = new GameSolver(games[0],objectives[0],bdd);
		GameSolution concSol1=gs1.solve();
		concSol1.print();
		long t1=System.currentTimeMillis();
//		Game concStrat1=concSol1.strategyOfTheWinner();
//		concStrat1.removeUnreachableStates().toGameGraph().draw("concSol1.dot", 1, 0);
		System.out.println("com "+(t1-t0));
		
//		System.out.println("\n\n CS guided Control\n\n");
//		
//		
//		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objectives[0]);
//		long t2=System.currentTimeMillis();
//		GameSolution absSol1=csgc1.counterStrategyGuidedControl();
//		long t3=System.currentTimeMillis();
//		System.out.println("cs guided "+(t3-t2));
//		absSol1.print();
//		AbstractGame refinedAbsGame1 = csgc1.getAbstractGame();
//		refinedAbsGame1.removeUnreachableStates().toGameGraph().draw("refinedAbsGame1.dot", 1, 0);
////		refinedAbsGame1.printGame();
//		Game absStrat1=absSol1.strategyOfTheWinner();
//		absStrat1.removeUnreachableStates().toGameGraph().draw("absStrat1.dot", 1,0);
	}
	
	public static void testWithBoundedObjective(BDD bdd, int numOfVars, int dim){
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		ArrayList<Integer> agent2VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent2VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		agentsVarsDimensions.add(agent2VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
//			environmentInitValues.add(dim-1);
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add(1);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1init.add(0);
		}
		ArrayList<Integer> agent2init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
//			if(i==0){
//				agent2init.add(0);
//			}
//			else{
//				agent2init.add(dim-1);
//			}
			agent2init.add(dim-1);
		}
		agentsInitValues.add(agent1init);
		agentsInitValues.add(agent2init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
//		System.out.println("printing games before reachability objectives");
//		games[0].printGame();
//		games[1].printGame();
		
		//define objectives
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		int[] x_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(0));
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		int[] y_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(1));
		
		//create bounded reachability objectives
//		int bound=5;
//		Variable[][] counter1=tcg.createBoundedReachabilityCounter(bound, "c1");
//		Variable[] flag1=tcg.createBoundedReachabilityFlag("f1");
//		//r1 should reach (dim-1,0)
//		int tx1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(0));
//		int tx1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(0));
//		int ty1=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
//		int ty1Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(0).get(1));
//		int targetState1=bdd.ref(bdd.and(tx1, ty1));
//		int targetState1Prime=bdd.ref(bdd.and(tx1Prime, ty1Prime));
//		int reachInit1=tcg.boundedReachabilityInit(targetState1, flag1[0], counter1[0]);
//		int reachTrans1=tcg.boundedReachabilityTransitions(bound, targetState1, targetState1Prime, counter1[0], counter1[1], flag1[0], flag1[1]); 
//		int sameCounter1=BDDWrapper.same(bdd, counter1[0], counter1[1]);
//		int sameFlag1=bdd.ref(bdd.biimp(flag1[0].getBDDVar(), flag1[1].getBDDVar()));
//		int sameReachTrans1=bdd.ref(bdd.and(sameCounter1, sameFlag1));
//		
//		//update game based on reachability obj
//		Variable[] flag1var = {flag1[0]};
//		Variable[] flag1varPrime={flag1[1]};
//		Variable[] reach1Vars=UtilityMethods.concatenateArrays(counter1[0], flag1var);
//		Variable[] reach1VarsPrime=UtilityMethods.concatenateArrays(counter1[1], flag1varPrime);
//		games[0].variables=UtilityMethods.concatenateArrays(games[0].variables, reach1Vars);
//		games[0].primedVariables=UtilityMethods.concatenateArrays(games[0].primedVariables, reach1VarsPrime); 
//		games[0].init = bdd.andTo(games[0].init, reachInit1);
//		games[0].T_env = bdd.andTo(games[0].T_env, sameReachTrans1);
//		games[0].T_sys = bdd.andTo(games[0].T_sys, reachTrans1);
//		
//		//reachability for robot 2
//		Variable[][] counter2=tcg.createBoundedReachabilityCounter(bound, "c2");
//		Variable[] flag2=tcg.createBoundedReachabilityFlag("f2");
//		//r2 should reach (0,0)
//		int tx2=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(1).get(0));
//		int tx2Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(1).get(0));
//		int ty2=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(1).get(1));
//		int ty2Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(1).get(1));
//		int targetState2=bdd.ref(bdd.and(tx2, ty2));
//		int targetState2Prime=bdd.ref(bdd.and(tx2Prime, ty2Prime));
//		int reachInit2=tcg.boundedReachabilityInit(targetState2, flag2[0], counter2[0]);
//		int reachTrans2=tcg.boundedReachabilityTransitions(bound, targetState2, targetState2Prime, counter2[0], counter2[1], flag2[0], flag2[1]); 
//		int sameCounter2=BDDWrapper.same(bdd, counter2[0], counter2[1]);
//		int sameFlag2=bdd.ref(bdd.biimp(flag2[0].getBDDVar(), flag2[1].getBDDVar()));
//		int sameReachTrans2=bdd.ref(bdd.and(sameCounter2, sameFlag2));
//		
//		//update game based on reachability obj
//		Variable[] flag2var = {flag2[0]};
//		Variable[] flag2varPrime={flag2[1]};
//		Variable[] reach2Vars=UtilityMethods.concatenateArrays(counter2[0], flag2var);
//		Variable[] reach2VarsPrime=UtilityMethods.concatenateArrays(counter2[1], flag2varPrime);
//		games[1].variables=UtilityMethods.concatenateArrays(games[1].variables, reach2Vars);
//		games[1].primedVariables=UtilityMethods.concatenateArrays(games[1].primedVariables, reach2VarsPrime); 
//		games[1].init = bdd.andTo(games[1].init, reachInit2);
//		games[1].T_env = bdd.andTo(games[1].T_env, sameReachTrans2);
//		games[1].T_sys = bdd.andTo(games[1].T_sys, reachTrans2);		
		
		//reachability objectives
		int bound=2;
		//r1 should reach (0,dim-1)
		int tx1=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(0));
		int tx1Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(0).get(0));
		int ty1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(1));
		int ty1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(1));
		int targetStates1=bdd.ref(bdd.and(tx1, ty1));
		int targetStates1Prime=bdd.ref(bdd.and(tx1Prime, ty1Prime));
		
		//r2 should reach (dim-1,0)
		int tx2=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(1).get(0));
		int tx2Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(1).get(0));
		int ty2=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(1).get(1));
		int ty2Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(1).get(1));
		int targetStates2=bdd.ref(bdd.and(tx2, ty2));
		int targetStates2Prime=bdd.ref(bdd.and(tx2Prime, ty2Prime));
		
		
		games[0]=games[0].gameWithBoundedReachabilityObjective(bound, "br1", targetStates1, targetStates1Prime);
		games[1]=games[1].gameWithBoundedReachabilityObjective(bound, "br2", targetStates2, targetStates2Prime);
		
		//updating the set of variables with the variables introduced by reachability objectives
		Variable[] vars=Variable.unionVariables(games[0].variables, games[1].variables);
		Variable[] varsPrime=Variable.unionVariables(games[0].primedVariables, games[1].primedVariables);
		
		games[0].variables=vars;
		games[0].primedVariables=varsPrime;
		
		games[1].variables=vars;
		games[1].primedVariables=varsPrime;
		
		//safety objective
		int objective1 = tcg.noCollisionObjective(x_e, y_e, x_1, y_1);
		int objective2 = tcg.noCollisionObjective(x_e, y_e, x_2, y_2);
		int objective3 = tcg.noCollisionObjective(x_1, y_1, x_2, y_2);
		
		
//		//x_e !=x1 
//		int objective1 = BDDWrapper.complement(bdd, tcg.same(x_e, x_1));
//		//x_e !=x2 
//		int objective2 = BDDWrapper.complement(bdd, tcg.same(x_e, x_2));
//		//y1 != y2
//		int objective3 = BDDWrapper.complement(bdd, tcg.same(y_1, y_2));
		
		int[] objectives = {objective1, objective2, objective3};
		
		int init=bdd.ref(bdd.and(games[0].init, games[1].init));
				
		int[] abstractionPredicates={init, objective1, objective2, objective3};
		
//		System.out.println("printing games before reachability objectives");
//		games[0].printGame();
//		games[1].printGame();
		

		TestCaseGenerator.testGames(bdd, games, objectives, abstractionPredicates);
		
		
	}
	
	public static void compMotionPrimitivesCaseStudy(BDD bdd, int numOfVars, int dim, int bound){
		long t0,t1;
		long t;
		
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		ArrayList<Integer> agent2VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent2VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		agentsVarsDimensions.add(agent2VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1init.add(0);
		}
		ArrayList<Integer> agent2init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent2init.add((dim-1)/2);
			}
			else{
				agent2init.add(dim-1);
			}
		}
		agentsInitValues.add(agent1init);
		agentsInitValues.add(agent2init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
//		System.out.println("printing games ");
//		games[0].printGame();
		
//		games[1].printGame();
		
		
		//define objectives
		//x_e !=x1 
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		int[] x_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(0));
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		int[] y_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(1));

		int sameXeX1 = BDDWrapper.same(bdd, x_e, x_1);
		int sameXeX2 = BDDWrapper.same(bdd, x_e, x_2);
		int sameX1X2 = BDDWrapper.same(bdd, x_1, x_2);
		
		int sameYeY1 = BDDWrapper.same(bdd, y_e, y_1);
		int sameYeY2 = BDDWrapper.same(bdd, y_e, y_2);
		int sameY1Y2 = BDDWrapper.same(bdd, y_1, y_2);
		
		int sameLoc1 = bdd.ref(bdd.and(sameXeX1, sameYeY1));
		int sameLoc2 = bdd.ref(bdd.and(sameXeX2, sameYeY2));
		int sameLoc3 = bdd.ref(bdd.and(sameX1X2, sameY1Y2));
		
		int objective1 = BDDWrapper.complement(bdd, sameLoc1);
		int objective2 = BDDWrapper.complement(bdd, sameLoc2);
		int objective3 = BDDWrapper.complement(bdd, sameLoc3);
		
		
		
		//reachability objectives
//		int bound=3;
		//r1 should reach (0,dim-1)
		int ty1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(1));
		int ty1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(1));
		int targetStates1=ty1;
		int targetStates1Prime=ty1Prime;
		
		//r2 should reach (dim-1,0)
		int ty2=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(1).get(1));
		int ty2Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(1).get(1));
		int targetStates2=ty2;
		int targetStates2Prime=ty2Prime;
		
		
		games[0]=games[0].gameWithBoundedReachabilityObjective(bound, "br1", targetStates1, targetStates1Prime);
		games[1]=games[1].gameWithBoundedReachabilityObjective(bound, "br2", targetStates2, targetStates2Prime);
		
		Variable flag1=Variable.getVariable(games[0].variables, "br1f");
		Variable flag2=Variable.getVariable(games[1].variables, "br2f");
		
		//updating the set of variables with the variables introduced by reachability objectives
		Variable[] vars=Variable.unionVariables(games[0].variables, games[1].variables);
		Variable[] varsPrime=Variable.unionVariables(games[0].primedVariables, games[1].primedVariables);
		
		games[0].variables=vars;
		games[0].primedVariables=varsPrime;
		
		games[1].variables=vars;
		games[1].primedVariables=varsPrime;
		
//		int newR0Init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
//		int newX1_init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(0));
//		int newInit1=bdd.ref(bdd.and(newR0Init, newX1_init));
//		int newR1Init=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(1).get(1));
//		int newX2_init=BDDWrapper.assign(bdd, 1, tcg.agentsVars.get(1).get(0));
//		int newInit2=bdd.ref(bdd.and(newR1Init, newX2_init));
//		int newObjInit=BDDWrapper.assign(bdd, (dim-1)/2, tcg.environmentVars.get(1));
//		int newObjXInit=BDDWrapper.assign(bdd, dim-1, tcg.environmentVars.get(0));
//		int newInit3=bdd.ref(bdd.and(newObjInit, newObjXInit));
//		
////		int newInit=bdd.ref(bdd.and(newR0Init,newR1Init));
//		int newInit=bdd.ref(bdd.and(newInit1, newInit2));
//		newInit=bdd.andTo(newInit, newInit3);
		
//		games[0].init=newInit;
//		games[1].init=newInit;
		
		games[0].init=bdd.ref(bdd.and(games[0].init,games[1].init));
		games[1].init = games[0].init;
		
		games[0].printGameVars();
//		UtilityMethods.debugBDDMethods(bdd, "games[0] init is ", games[0].init);
//		
//		games[1].printGameVars();
//		UtilityMethods.debugBDDMethods(bdd, "games[1] init is ", games[1].init);
		
		Date date = new Date();
		
		System.out.println("Compositional");
		System.out.println("current time is "+date.toString());
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(games[0], objective1, bdd);
		//games[0].printGame();
//		games[0].toGameGraph().draw("concGame0.dot", 1);
		GameSolution concSol1 = gs1.solve();
		concSol1.print();
//		concSol1.drawWinnerStrategy("concSol1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		GameSolver gs2 = new GameSolver(games[1], objective2, bdd);
		GameSolution concSol2 = gs2.solve();
		concSol2.print();
//		concSol2.drawWinnerStrategy("concSol2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		
		t0=UtilityMethods.timeStamp();
		Game composedGame=concSol1.strategyOfTheWinner().compose(concSol2.strategyOfTheWinner());
//		composedGame.printGame();
		GameSolver compGS=new GameSolver(composedGame,objective3, bdd);
		GameSolution sol3 = compGS.solve();
		UtilityMethods.duration(t0, "for composition in ");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		sol3.print();
		
//		GameGraph gg = sol3.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		UtilityMethods.duration(t, "whole process in ");
	}
	
	public static void compMotionPrimitivesCaseStudy2(BDD bdd, int numOfVars, int dim, int bound){
		
		long t0_all = UtilityMethods.timeStamp();
		
		long t0,t1;
		long t;
		
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		ArrayList<Integer> agent2VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent2VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		agentsVarsDimensions.add(agent2VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1init.add(0);
		}
		ArrayList<Integer> agent2init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent2init.add(1);
			}
			else{
				agent2init.add(0);
			}
		}
		agentsInitValues.add(agent1init);
		agentsInitValues.add(agent2init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
//		System.out.println("printing games ");
//		games[0].printGame();
		
//		games[1].printGame();
		
		
		//define objectives
		Variable[] x_e_Vars=tcg.environmentVars.get(0);
		Variable[] x_e_VarsPrime=tcg.environmentVarsPrime.get(0);
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		Variable[] x_1_Vars=tcg.agentsVars.get(0).get(0);
		Variable[] x_1_VarsPrime=tcg.agentsVarsPrime.get(0).get(0);
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		Variable[] x_2_Vars=tcg.agentsVars.get(1).get(0);
		Variable[] x_2_VarsPrime=tcg.agentsVarsPrime.get(1).get(0);
		int[] x_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(0));
		Variable[] y_e_Vars=tcg.environmentVars.get(1);
		Variable[] y_e_VarsPrime=tcg.environmentVarsPrime.get(1);
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		Variable[] y_1_Vars=tcg.agentsVars.get(0).get(1);
		Variable[] y_1_VarsPrime=tcg.agentsVarsPrime.get(0).get(1);
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		Variable[] y_2_Vars=tcg.agentsVars.get(1).get(1);
		Variable[] y_2_VarsPrime=tcg.agentsVarsPrime.get(1).get(1);
		int[] y_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(1));

		int sameXeX1 = BDDWrapper.same(bdd, x_e, x_1);
		int sameXeX2 = BDDWrapper.same(bdd, x_e, x_2);
		int sameX1X2 = BDDWrapper.same(bdd, x_1, x_2);
		
		int sameYeY1 = BDDWrapper.same(bdd, y_e, y_1);
		int sameYeY2 = BDDWrapper.same(bdd, y_e, y_2);
		int sameY1Y2 = BDDWrapper.same(bdd, y_1, y_2);
		
		int sameLoc1 = bdd.ref(bdd.and(sameXeX1, sameYeY1));
		int sameLoc2 = bdd.ref(bdd.and(sameXeX2, sameYeY2));
		int sameLoc3 = bdd.ref(bdd.and(sameX1X2, sameY1Y2));
		
		//no collision with uncontrollable agent
		int objective1 = BDDWrapper.complement(bdd, sameLoc1);
		int objective2 = BDDWrapper.complement(bdd, sameLoc2);
		
		//no collision with each other
		int objective3 = BDDWrapper.complement(bdd, sameLoc3);
		
		//no collision with static obstacles
		//x_i,y_i != (y=1 & x!=(dim-1)/2)
		int x_1Static = BDDWrapper.assign(bdd, (dim-1)/2, x_1_Vars);
		int x_1StaticComplement = BDDWrapper.complement(bdd, x_1Static);
		int y_1Static = BDDWrapper.assign(bdd, 1, y_1_Vars);
		int r1Static = bdd.ref(bdd.and(x_1StaticComplement, y_1Static));
		int objective4= BDDWrapper.complement(bdd, r1Static);
		
		int x_2Static = BDDWrapper.assign(bdd, (dim-1)/2, x_2_Vars);
		int x_2StaticComplement = BDDWrapper.complement(bdd, x_2Static);
		int y_2Static = BDDWrapper.assign(bdd, 1, y_2_Vars);
		int r2Static = bdd.ref(bdd.and(x_2StaticComplement, y_2Static));
		int objective5= BDDWrapper.complement(bdd, r2Static);
		
		
		//maintaining linear formation
		int objective6 = bdd.ref(bdd.or(sameX1X2, sameY1Y2));
		
		int indObjectiveR1=bdd.ref(bdd.and(objective1, objective4));
		int indObjectiveR2=bdd.ref(bdd.and(objective2, objective5));
		int collectiveObjective=bdd.ref(bdd.and(objective3, objective6));
		
		//reachability objectives
//		int bound=3;
		//r1 should reach (0,dim-1)
		int tx1=BDDWrapper.assign(bdd, 0, x_1_Vars);
		int tx1Prime=BDDWrapper.assign(bdd, 0, x_1_VarsPrime);
		int ty1=BDDWrapper.assign(bdd, dim-1, y_1_Vars);
		int ty1Prime=BDDWrapper.assign(bdd, dim-1, y_1_VarsPrime);
		int targetStates1=bdd.ref(bdd.and(tx1, ty1));
		int targetStates1Prime=bdd.ref(bdd.and(tx1Prime, ty1Prime));
		
		//r2 should reach (1,dim-1)
		int tx2=BDDWrapper.assign(bdd, 1, x_2_Vars);
		int tx2Prime=BDDWrapper.assign(bdd, 1, x_2_VarsPrime);
		int ty2=BDDWrapper.assign(bdd, dim-1, y_2_Vars);
		int ty2Prime=BDDWrapper.assign(bdd, dim-1, y_2_VarsPrime);
		int targetStates2=bdd.ref(bdd.and(tx2, ty2));
		int targetStates2Prime=bdd.ref(bdd.and(tx2Prime, ty2Prime));
		
		
		games[0]=games[0].gameWithBoundedReachabilityObjective(bound, "br1", targetStates1, targetStates1Prime);
		games[1]=games[1].gameWithBoundedReachabilityObjective(bound, "br2", targetStates2, targetStates2Prime);
		
		Variable flag1=Variable.getVariable(games[0].variables, "br1f");
		Variable flag2=Variable.getVariable(games[1].variables, "br2f");
		
		//updating the set of variables with the variables introduced by reachability objectives
		Variable[] vars=Variable.unionVariables(games[0].variables, games[1].variables);
		Variable[] varsPrime=Variable.unionVariables(games[0].primedVariables, games[1].primedVariables);
		
		games[0].variables=vars;
		games[0].primedVariables=varsPrime;
		
		games[1].variables=vars;
		games[1].primedVariables=varsPrime;
		
//		int newR0Init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
//		int newX1_init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(0));
//		int newInit1=bdd.ref(bdd.and(newR0Init, newX1_init));
//		int newR1Init=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(1).get(1));
//		int newX2_init=BDDWrapper.assign(bdd, 1, tcg.agentsVars.get(1).get(0));
//		int newInit2=bdd.ref(bdd.and(newR1Init, newX2_init));
//		int newObjInit=BDDWrapper.assign(bdd, (dim-1)/2, tcg.environmentVars.get(1));
//		int newObjXInit=BDDWrapper.assign(bdd, dim-1, tcg.environmentVars.get(0));
//		int newInit3=bdd.ref(bdd.and(newObjInit, newObjXInit));
//		
////		int newInit=bdd.ref(bdd.and(newR0Init,newR1Init));
//		int newInit=bdd.ref(bdd.and(newInit1, newInit2));
//		newInit=bdd.andTo(newInit, newInit3);
		
//		games[0].init=newInit;
//		games[1].init=newInit;
		
		games[0].init=bdd.ref(bdd.and(games[0].init,games[1].init));
		games[1].init = games[0].init;
		
//		games[0].printGameVars();
//		UtilityMethods.debugBDDMethods(bdd, "games[0] init is ", games[0].init);
//		
//		games[1].printGameVars();
//		UtilityMethods.debugBDDMethods(bdd, "games[1] init is ", games[1].init);
		
		Date date = new Date();
		
		System.out.println("Compositional");
		System.out.println("current time is "+date.toString());
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(games[0], indObjectiveR1, bdd);
		//games[0].printGame();
//		games[0].toGameGraph().draw("concGame0.dot", 1);
		GameSolution concSol1 = gs1.solve();
		concSol1.print();
//		concSol1.drawWinnerStrategy("concSol1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+((double)(t1-t0)/1000));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		GameSolver gs2 = new GameSolver(games[1], indObjectiveR2, bdd);
		GameSolution concSol2 = gs2.solve();
		concSol2.print();
//		concSol2.drawWinnerStrategy("concSol2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+((double)(t1-t0)/1000));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		
		t0=UtilityMethods.timeStamp();
		Game composedGame=concSol1.strategyOfTheWinner().compose(concSol2.strategyOfTheWinner());
//		composedGame.printGame();
		GameSolver compGS=new GameSolver(composedGame,collectiveObjective, bdd);
		GameSolution sol3 = compGS.solve();
		UtilityMethods.duration(t0, "for composition in ");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+((double)(t1-t0)/1000));
		t+=t1-t0;
		sol3.print();
		
		System.out.println("whole process in "+((double)t/1000)+" s");
		
//		GameGraph gg = sol3.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		System.out.println("BDD usage");
		
		System.out.println("Num of variables in BDD: "+bdd.numberOfVariables() );
		
		System.out.println("BDD memory usage: "+((double)bdd.getMemoryUsage()/1048576) + " MB");
		
		
		UtilityMethods.duration(t0_all, "the whole process in ");
		
		
	}
	
	public static void compMotionPrimitivesFairGamesCaseStudy(BDD bdd, int numOfVars, int dim){
		
		long t0_all = UtilityMethods.timeStamp();
		
		long t0,t1;
		long t;
		
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		ArrayList<Integer> agent2VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent2VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		agentsVarsDimensions.add(agent2VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1init.add(0);
		}
		ArrayList<Integer> agent2init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent2init.add(1);
			}
			else{
				agent2init.add(0);
			}
		}
		agentsInitValues.add(agent1init);
		agentsInitValues.add(agent2init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
//		System.out.println("printing games ");
//		games[0].printGame();
		
//		games[1].printGame();
		
		FairGame[] fairGames = {games[0].toFairGame(), games[1].toFairGame()};
		
		//define objectives
		Variable[] x_e_Vars=tcg.environmentVars.get(0);
		Variable[] x_e_VarsPrime=tcg.environmentVarsPrime.get(0);
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		Variable[] x_1_Vars=tcg.agentsVars.get(0).get(0);
		Variable[] x_1_VarsPrime=tcg.agentsVarsPrime.get(0).get(0);
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		Variable[] x_2_Vars=tcg.agentsVars.get(1).get(0);
		Variable[] x_2_VarsPrime=tcg.agentsVarsPrime.get(1).get(0);
		int[] x_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(0));
		Variable[] y_e_Vars=tcg.environmentVars.get(1);
		Variable[] y_e_VarsPrime=tcg.environmentVarsPrime.get(1);
		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
		Variable[] y_1_Vars=tcg.agentsVars.get(0).get(1);
		Variable[] y_1_VarsPrime=tcg.agentsVarsPrime.get(0).get(1);
		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
		Variable[] y_2_Vars=tcg.agentsVars.get(1).get(1);
		Variable[] y_2_VarsPrime=tcg.agentsVarsPrime.get(1).get(1);
		int[] y_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(1));

		int sameXeX1 = BDDWrapper.same(bdd, x_e, x_1);
		int sameXeX2 = BDDWrapper.same(bdd, x_e, x_2);
		int sameX1X2 = BDDWrapper.same(bdd, x_1, x_2);
		
		int sameYeY1 = BDDWrapper.same(bdd, y_e, y_1);
		int sameYeY2 = BDDWrapper.same(bdd, y_e, y_2);
		int sameY1Y2 = BDDWrapper.same(bdd, y_1, y_2);
		
		int sameLoc1 = bdd.ref(bdd.and(sameXeX1, sameYeY1));
		int sameLoc2 = bdd.ref(bdd.and(sameXeX2, sameYeY2));
		int sameLoc3 = bdd.ref(bdd.and(sameX1X2, sameY1Y2));
		
		//no collision with uncontrollable agent
		int objective1 = BDDWrapper.complement(bdd, sameLoc1);
		int objective2 = BDDWrapper.complement(bdd, sameLoc2);
		
		//no collision with each other
		int objective3 = BDDWrapper.complement(bdd, sameLoc3);
		
		
		Objective safetyObjective1 = new Objective(ObjectiveType.Safety, objective1);
		Objective safetyObjective2 = new Objective(ObjectiveType.Safety, objective2);
		Objective safetyObjective3 = new Objective(ObjectiveType.Safety, objective3);
		
		
		//no collision with static obstacles
		//x_i,y_i != (y=1 & x!=(dim-1)/2)
		int x_1Static = BDDWrapper.assign(bdd, (dim-1)/2, x_1_Vars);
		int x_1StaticComplement = BDDWrapper.complement(bdd, x_1Static);
		int y_1Static = BDDWrapper.assign(bdd, 1, y_1_Vars);
		int r1Static = bdd.ref(bdd.and(x_1StaticComplement, y_1Static));
		int objective4= BDDWrapper.complement(bdd, r1Static);
		
		Objective safetyObjective4 = new Objective(ObjectiveType.Safety, objective4);
		
		int x_2Static = BDDWrapper.assign(bdd, (dim-1)/2, x_2_Vars);
		int x_2StaticComplement = BDDWrapper.complement(bdd, x_2Static);
		int y_2Static = BDDWrapper.assign(bdd, 1, y_2_Vars);
		int r2Static = bdd.ref(bdd.and(x_2StaticComplement, y_2Static));
		int objective5= BDDWrapper.complement(bdd, r2Static);
		
		Objective safetyObjective5 = new Objective(ObjectiveType.Safety, objective5);
		
		//maintaining linear formation
		int objective6 = bdd.ref(bdd.or(sameX1X2, sameY1Y2));
		
		Objective safetyObjective6 = new Objective(ObjectiveType.Safety, objective6);
		
		int indObjectiveR1=bdd.ref(bdd.and(objective1, objective4));
		int indObjectiveR2=bdd.ref(bdd.and(objective2, objective5));
		int collectiveObjective=bdd.ref(bdd.and(objective3, objective6));
		
		Objective indSafetyObjectiveR1 = new Objective(ObjectiveType.Safety, indObjectiveR1);
		Objective indSafetyObjectiveR2 = new Objective(ObjectiveType.Safety, indObjectiveR2);
		
		Objective collectiveSafetyObjective= new Objective(ObjectiveType.Safety, collectiveObjective);
		
		//reachability objectives
//		int bound=3;
		//r1 should reach (0,dim-1)
		int tx1=BDDWrapper.assign(bdd, 0, x_1_Vars);
		int ty1=BDDWrapper.assign(bdd, dim-1, y_1_Vars);
		int targetStates1=bdd.ref(bdd.and(tx1, ty1));
		
		Objective reachabilityObjective1 = new Objective(ObjectiveType.Reachability, targetStates1);
		
		
		//r2 should reach (1,dim-1)
		int tx2=BDDWrapper.assign(bdd, 1, x_2_Vars);
		int ty2=BDDWrapper.assign(bdd, dim-1, y_2_Vars);
		int targetStates2=bdd.ref(bdd.and(tx2, ty2));
		
		Objective reachabilityObjective2 = new Objective(ObjectiveType.Reachability, targetStates2);
		
		//updating the set of variables with the variables introduced by reachability objectives
//		Variable[] vars=Variable.unionVariables(games[0].variables, games[1].variables);
//		Variable[] varsPrime=Variable.unionVariables(games[0].primedVariables, games[1].primedVariables);
//		
//		games[0].variables=vars;
//		games[0].primedVariables=varsPrime;
//		
//		games[1].variables=vars;
//		games[1].primedVariables=varsPrime;
		
		
//		games[0].init=bdd.ref(bdd.and(games[0].init,games[1].init));
//		games[1].init = games[0].init;
		
		
		Date date = new Date();
		
		System.out.println("Compositional");
		System.out.println("current time is "+date.toString());
		System.out.println("Solving for individual objectives of R1");
		t0 = UtilityMethods.timeStamp();
		long safetyT0_1=UtilityMethods.timeStamp();
		FairGameSolution safetySolution1 = FairGameSolver.solve(bdd, fairGames[0], indSafetyObjectiveR1);
		safetySolution1.print();
		UtilityMethods.duration(safetyT0_1, "safety solution for game 1 was computed in ");
		
		long reachabilityT0_1 = UtilityMethods.timeStamp();
		FairGameSolution reachabilitySolution1 = FairGameSolver.solve(bdd, safetySolution1.strategyOfTheWinner(), reachabilityObjective1);
		reachabilitySolution1.print();
		UtilityMethods.duration(reachabilityT0_1, "safety solution for game 1 was computed in ");
		FairGame solution1 = reachabilitySolution1.strategyOfTheWinner();
		
		UtilityMethods.duration(t0, "individual objectives for R1 in ");
		
		System.out.println("Solving for individual objectives of R2");
		t0 = UtilityMethods.timeStamp();
		long safetyT0_2=UtilityMethods.timeStamp();
		FairGameSolution safetySolution2 = FairGameSolver.solve(bdd, fairGames[1], indSafetyObjectiveR2);
		safetySolution2.print();
		UtilityMethods.duration(safetyT0_2, "safety solution for game 2 was computed in ");
		
		long reachabilityT0_2 = UtilityMethods.timeStamp();
		FairGameSolution reachabilitySolution2 = FairGameSolver.solve(bdd, safetySolution2.strategyOfTheWinner(), reachabilityObjective2);
		reachabilitySolution2.print();
		UtilityMethods.duration(reachabilityT0_2, "safety solution for game 2 was computed in ");
		FairGame solution2 = reachabilitySolution2.strategyOfTheWinner();
		
		UtilityMethods.duration(safetyT0_2, "individual objectives for R2 in ");
		
		UtilityMethods.duration(t0, "individual objectives for both robots");
		
		System.out.println("solving for compositional");
		long compT0=UtilityMethods.timeStamp();
		FairGame composedGame = solution1.compose(solution2);
		FairGameSolution solution = FairGameSolver.solve(bdd, composedGame, collectiveSafetyObjective);
		solution.print();
//		composedGame.printGame();
		UtilityMethods.duration(compT0, "for composition in ");
		
		UtilityMethods.duration(t0, "whole process in ");
		
		
		BDDWrapper.BDD_Usage(bdd);
		
		UtilityMethods.duration(t0_all, "the whole process in ");
	}
	
	public static void test1(BDD bdd, int numOfVars, int dim){
		
		long t0,t1;
		long t;
		
		ArrayList<Integer> envVarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			envVarsDimensions.add(dim);
		}
		
		ArrayList<ArrayList<Integer>> agentsVarsDimensions=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1VarsDimensions.add(dim);
		}
		ArrayList<Integer> agent2VarsDimensions=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent2VarsDimensions.add(dim);
		}
		agentsVarsDimensions.add(agent1VarsDimensions);
		agentsVarsDimensions.add(agent2VarsDimensions);
		
		RMPTestCaseGenerator tcg=new RMPTestCaseGenerator(bdd, envVarsDimensions, agentsVarsDimensions);
		
		ArrayList<Integer> environmentInitValues=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				environmentInitValues.add(dim-1);
			}else{
				environmentInitValues.add((dim-1)/2);
			}
		}
		
		
		ArrayList<ArrayList<Integer>> agentsInitValues=new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> agent1init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			agent1init.add(0);
		}
		ArrayList<Integer> agent2init=new ArrayList<Integer>();
		for(int i=0;i<numOfVars;i++){
			if(i==0){
				agent2init.add(0);
			}
			else{
				agent2init.add(dim-1);
			}
		}
		agentsInitValues.add(agent1init);
		agentsInitValues.add(agent2init);
		
		Game[] games=tcg.generateGames(environmentInitValues, agentsInitValues);
		
//		System.out.println("printing games ");
//		games[0].printGame();
		
//		games[1].printGame();
		
		
		//define objectives
		//x_e !=x1 
		int[] x_e = Variable.getBDDVars(tcg.environmentVars.get(0));
		int[] x_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(0));
		int[] x_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(0));
//		int[] y_e = Variable.getBDDVars(tcg.environmentVars.get(1));
//		int[] y_1 = Variable.getBDDVars(tcg.agentsVars.get(0).get(1));
//		int[] y_2 = Variable.getBDDVars(tcg.agentsVars.get(1).get(1));
//		int y_1eqEnvY = BDDWrapper.assign(bdd, (dim-1)/2, tcg.agentsVars.get(0).get(1));
//		int y_2eqEnvY = BDDWrapper.assign(bdd, (dim-1)/2, tcg.agentsVars.get(1).get(1));
//		int x1_eq_x_e=BDDWrapper.same(bdd, x_e, x_1);
//		int x2_eq_x_e=BDDWrapper.same(bdd, x_e, x_2);
//		int sameLoc1 = bdd.ref(bdd.and(y_1eqEnvY, x1_eq_x_e));
//		int sameLoc2 = bdd.ref(bdd.and(y_2eqEnvY, x2_eq_x_e));
//		int objective1 = BDDWrapper.complement(bdd, sameLoc1);
//		int objective2 = BDDWrapper.complement(bdd, sameLoc2);
		int objective1 = BDDWrapper.complement(bdd, tcg.same(x_e, x_1));
		int objective2 = BDDWrapper.complement(bdd, tcg.same(x_e, x_2));
		
		int init=tcg.init;
		
		
//		//reachability objectives
//		int bound=3;
//		//r1 should reach (0,dim-1)
//		int ty1=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(0).get(1));
//		int ty1Prime=BDDWrapper.assign(bdd, dim-1, tcg.agentsVarsPrime.get(0).get(1));
//		int targetStates1=ty1;
//		int targetStates1Prime=ty1Prime;
//		
//		//r2 should reach (dim-1,0)
//		int ty2=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(1).get(1));
//		int ty2Prime=BDDWrapper.assign(bdd, 0, tcg.agentsVarsPrime.get(1).get(1));
//		int targetStates2=ty2;
//		int targetStates2Prime=ty2Prime;
//		
//		
//		games[0]=games[0].gameWithBoundedReachabilityObjective(bound, "br1", targetStates1, targetStates1Prime);
//		games[1]=games[1].gameWithBoundedReachabilityObjective(bound, "br2", targetStates2, targetStates2Prime);
//		
//		Variable flag1=Variable.getVariable(games[0].variables, "br1f");
//		Variable flag2=Variable.getVariable(games[1].variables, "br2f");
//		
//		//updating the set of variables with the variables introduced by reachability objectives
//		Variable[] vars=Variable.unionVariables(games[0].variables, games[1].variables);
//		Variable[] varsPrime=Variable.unionVariables(games[0].primedVariables, games[1].primedVariables);
//		
//		games[0].variables=vars;
//		games[0].primedVariables=varsPrime;
//		
//		games[1].variables=vars;
//		games[1].primedVariables=varsPrime;
//		
//		int newR0Init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(1));
//		int newX1_init=BDDWrapper.assign(bdd, 0, tcg.agentsVars.get(0).get(0));
//		int newInit1=bdd.ref(bdd.and(newR0Init, newX1_init));
//		int newR1Init=BDDWrapper.assign(bdd, dim-1, tcg.agentsVars.get(1).get(1));
//		int newX2_init=BDDWrapper.assign(bdd, 1, tcg.agentsVars.get(1).get(0));
//		int newInit2=bdd.ref(bdd.and(newR1Init, newX2_init));
//		int newObjInit=BDDWrapper.assign(bdd, (dim-1)/2, tcg.environmentVars.get(1));
//		int newObjXInit=BDDWrapper.assign(bdd, dim-1, tcg.environmentVars.get(0));
//		int newInit3=bdd.ref(bdd.and(newObjInit, newObjXInit));
//		
////		int newInit=bdd.ref(bdd.and(newR0Init,newR1Init));
//		int newInit=bdd.ref(bdd.and(newInit1, newInit2));
//		newInit=bdd.andTo(newInit, newInit3);
//		
//		games[0].init=newInit;
//		games[1].init=newInit;
//		
////		int[] abstractionPredicates={newInit, ty1, ty2,  y_1eqEnvY, y_2eqEnvY, x1_eq_x_e, x2_eq_x_e, flag1.getBDDVar(), flag2.getBDDVar()};
//		int[] abstractionPredicates={newInit, objective1, objective2, flag1.getBDDVar(), flag2.getBDDVar()};
		int[] abstractionPredicates={games[0].init, objective1, objective2};
		Date date = new Date();
		
		System.out.println("central");
		System.out.println("current time is "+date.toString());
		t0=System.currentTimeMillis();
		GameSolver gs1 = new GameSolver(games[0], objective1, bdd);
		//games[0].printGame();
//		games[0].toGameGraph().draw("concGame0.dot", 1);
		GameSolution concSol1 = gs1.solve();
		concSol1.print();
//		concSol1.drawWinnerStrategy("concSol1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		GameSolver gs2 = new GameSolver(games[1], objective2, bdd);
		GameSolution concSol2 = gs2.solve();
		concSol2.print();
//		concSol2.drawWinnerStrategy("concSol2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		System.out.println("accumulated time "+t);
		
		System.out.println("\n\n cs guided control \n\n");
		
		System.out.println("current time is "+date.toString());
		t0=System.currentTimeMillis();
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates, objective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
		solution1.print();
//		solution1.drawWinnerStrategy("absStrat1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
		t=t1-t0;
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates, objective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
//		solution2.drawWinnerStrategy("absStrat2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
		t+=t1-t0;
		
//		t0=System.currentTimeMillis();
//		Game absComp = compose(bdd, solution1.strategyOfTheWinner(), solution2.strategyOfTheWinner());
//		t1=System.currentTimeMillis();
//		System.out.println("time for composing "+(t1-t0));
//		t+=t1-t0;
//		System.out.println("accumulated time "+t);
		

		
		
	}
	
	/**
	 * given two variables (x1,y1) and (x2,y2) returns the formula \not \forall_i (x1[i] <-> x2[i] & y1[i] <-> y2[i]), no collision
	 * @param bdd
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public  int noCollisionObjective(int[] x1, int[] y1, int[] x2, int[] y2){
		int complementSafetyObjective=bdd.ref(bdd.getOne());
		for(int i=0;i<x1.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x1[i], x2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			bdd.deref(tmpX);
		}
		
		for(int i=0;i<y1.length;i++){
			int tmpY= bdd.ref(bdd.biimp(y1[i], y2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		bdd.deref(complementSafetyObjective);
		return safety;
	}
}
