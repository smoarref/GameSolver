package csguided;

import java.util.ArrayList;
import java.util.HashMap;

import utils.UtilityMethods;
import automaton.GameGraph;
import automaton.GameNode;
import automaton.Label;
import automaton.LabeledEdge;
import automaton.StringLabel;
import jdd.bdd.BDD;

public class FairGame extends Game{
	
	//In order to make progress, system states fairTransitions.get(i) are labeled with a fairness requirement 
	// to always eventually make a transition according to it. 
	// each fairness requirement encodes the transition between two different sets of states
	ArrayList<Integer> fairTransitions;

//	public FairGame(BDD argBdd, Variable[] argInputVariables,
//			Variable[] argOutputVariables, Variable[] argInputPrimedVars,
//			Variable[] argOutputPrimedVars, int initial, int envTrans,
//			int sysTrans, Variable[] argActions, int[] argSysAvl,
//			int[] argEnvAvl) {
//		super(argBdd, argInputVariables, argOutputVariables, argInputPrimedVars,
//				argOutputPrimedVars, initial, envTrans, sysTrans, argActions,
//				argSysAvl, argEnvAvl);
//		fairTransitions=new ArrayList<Integer>();
//	}
	
	public FairGame(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argActions){
		super(argBdd, argVariables, argPrimedVars, initial, envTrans, sysTrans, argActions);
		fairTransitions=new ArrayList<Integer>();
	}
	
	public FairGame(BDD argBdd, Variable[] argVariables, Variable[] argPrimedVars,  int initial, int envTrans, int sysTrans, Variable[] argActions, ArrayList<Integer> fairness){
		super(argBdd, argVariables, argPrimedVars, initial, envTrans, sysTrans, argActions);
		fairTransitions=fairness;
	}
	
	public ArrayList<Integer> getFairnessRequirements(){
		return fairTransitions;
	}
	
	public GameGraph toGameGraph(){
		return toGameGraph("");
	}
	
	public GameGraph toGameGraph(String id){
		GameGraph gg=new GameGraph(id);
		ArrayList<String>  envTrans = parseTransitions(getEnvironmentTransitionRelation());
		ArrayList<String>  sysTrans = parseTransitions(getSystemTransitionRelation());
		
		HashMap<String, GameNode> labelToSystemNode= new HashMap<String, GameNode>();
	    HashMap<String, GameNode> labelToEnvironmentNode= new HashMap<String, GameNode>();
		
		 //environment transitions
	    int i=0;
	    int numOfVariables=variables.length;
	    for(String tr : envTrans){
	    	if(tr.equals("FALSE")){
	    		continue;
	    	}
	    	
	    	String state = tr.substring(0,numOfVariables);
	    	String nextState= tr.substring(numOfVariables, 2*numOfVariables);
	    	String action = tr.substring(2*numOfVariables, tr.length());
	    	
	    	//parse the labels
	    	if(stateMap!=null){
	    		state=stateMap.get(state);
	    		nextState=stateMap.get(nextState);
	    	}
//	    	else{
//	    		state=parseLabel(state, variables);
//		    	nextState=parseLabel(nextState, variables);
//	    	}
	    	
	    	if(actionMap != null){
	    		action=getActionName(action);
	    	}else{
	    		action = parseLabel(action, actionVars);
	    	}
	    	
	    	
//	    	System.out.println("state: "+state+" & next state: "+nextState+" & action is "+action);
	    	GameNode gn ;
	    	if(labelToEnvironmentNode.containsKey(state)){
	    		gn = labelToEnvironmentNode.get(state);
	    	}else{
	    		gn = new GameNode("q"+i, 'e', new StringLabel(state));
	    		
	    		//check if the new node is initial
	    		if(BDDWrapper.isMember(bdd, state, variables, init)){
	    			gn.setInitial(true);
	    		}
	    		
	    		i++;
	    		gg.addNode(gn);
	    		labelToEnvironmentNode.put(state, gn);
	    	}
	    	
	    	GameNode gn2;
	    	if(labelToSystemNode.containsKey(nextState)){
	    		gn2 = labelToSystemNode.get(nextState);
	    	}else{
	    		gn2 = new GameNode("q"+i, 's', new StringLabel(nextState));
	    		i++;
	    		gg.addNode(gn2);
	    		labelToSystemNode.put(nextState, gn2);
	    	}
	    	
	    	
	    	LabeledEdge<GameNode, Label> e = new LabeledEdge<GameNode, Label>(gn, gn2, new StringLabel(action));
	    	gg.addEdge(e);
	    	
	    }
	    
	    
	    //system transitions
	    for(String tr : sysTrans){
	    	if(tr.equals("FALSE")){
	    		continue;
	    	}
	    	String state = tr.substring(0,numOfVariables);
	    	String nextState= tr.substring(numOfVariables, 2*numOfVariables);
	    	String action = tr.substring(2*numOfVariables, tr.length());
	    	
	    	//parse the labels
	    	if(stateMap!=null){
	    		state=stateMap.get(state);
	    		nextState=stateMap.get(nextState);
	    	}
//	    	else{
//	    		state=parseLabel(state, variables);
//		    	nextState=parseLabel(nextState, variables);
//	    	}
	    	
	    	if(actionMap != null){
	    		action=getActionName(action);
	    	}else{
	    		action = parseLabel(action, actionVars);
	    	}
	    	
//	    	System.out.println("state: "+state+" & next state: "+nextState);
	    	GameNode gn ;
	    	if(labelToSystemNode.containsKey(state)){
	    		gn = labelToSystemNode.get(state);
	    	}else{
	    		gn = new GameNode("q"+i, 's', new StringLabel(state));
	    		i++;
	    		gg.addNode(gn);
	    		labelToSystemNode.put(state, gn);
	    	}
	    	
	    	GameNode gn2;
	    	if(labelToEnvironmentNode.containsKey(nextState)){
	    		gn2 = labelToEnvironmentNode.get(nextState);
	    	}else{
	    		gn2 = new GameNode("q"+i, 'e', new StringLabel(nextState));
	    		
	    		//check if the new node is initial
	    		if(BDDWrapper.isMember(bdd, nextState, variables, init)){
	    			gn2.setInitial(true);
	    		}
	    		
	    		i++;
	    		gg.addNode(gn2);
	    		labelToEnvironmentNode.put(nextState, gn2);
	    	}
	    	
	    	if(isFairEdge(state, nextState)){
	    		action+="*";
	    	}
	    	LabeledEdge<GameNode, Label> e = new LabeledEdge<GameNode, Label>(gn, gn2, new StringLabel(action));
	    	gg.addEdge(e);
	    }
		return gg;
	}
	
	public boolean isFairEdge(String state, String nextState){
		int stateBdd = BDDWrapper.assign(bdd, state, variables);
		int nextStateBdd = BDDWrapper.assign(bdd, nextState, primedVariables);
		int transition = bdd.ref(bdd.and(stateBdd, nextStateBdd));
		bdd.deref(stateBdd);
		bdd.deref(nextStateBdd);
		for(int i=0;i<fairTransitions.size();i++){
			int intersection = BDDWrapper.intersect(bdd, transition, fairTransitions.get(i));
			if(intersection != 0){// if state belongs to the fairness transitions
				bdd.deref(transition);
				bdd.deref(intersection);
				return true;
			}
			bdd.deref(intersection);
		}
		bdd.deref(transition);
		return false;
		
		//OLD CODE for assumption that there is a sequence between fairness transitions
//		for(int i=0;i<fairTransitions.size()-1;i++){
//			int intersection = BDDWrapper.intersect(bdd, stateBdd, fairTransitions.get(i));
//			if(intersection != 0){// if state belongs to the fairness transitions
//				int nextIntersection = BDDWrapper.intersect(bdd, nextStateBdd, fairTransitions.get(i+1));
//				bdd.deref(stateBdd);
//				bdd.deref(nextStateBdd);
//				bdd.deref(intersection);
//				if(nextIntersection != 0){
//					bdd.deref(nextIntersection);
//					return true;
//				}else{
//					bdd.deref(nextIntersection);
//					return false;
//				}
//			}
//		}
//		return false;
	}
	
	public FairGame FairGameWithReachabilityObjective(String namePrefix, int targetStates, int targetStatesPrime ){
		
		//define variables of the reachability game
		Variable[] flag=createReachabilityFlag(namePrefix);
		Variable[] flagVar={flag[0]};
		Variable[] flagPrimeVar={flag[1]};
		
		//define variables of the new game
		Variable[] vars=UtilityMethods.concatenateArrays(this.variables, flagVar);
		Variable[] primedVars=UtilityMethods.concatenateArrays(this.primedVariables, flagPrimeVar);
		
		//define initial values for reachability game
		int reachabilityGameInit=reachabilityGameInit(targetStates, flag[0]);
				
		//define transition relation for bounded reachability game 
		int reachTrans=reachabilityGameTransitions(targetStates, targetStatesPrime, flag[0], flag[1]); 
		int sameFlag=bdd.ref(bdd.biimp(flag[0].getBDDVar(), flag[1].getBDDVar()));
				
				
		int reachabilityGameT_env= bdd.ref(bdd.and(this.T_env, sameFlag));
		int reachabilityGameT_sys=  bdd.ref(bdd.and(this.T_sys, reachTrans));
		
		//fairness constraints
		ArrayList<Integer> newFairTransitions = new ArrayList<Integer>();
		//old fairness transitions are copied into second part of the new transition system where the flag is one
		for(int i=0;i<fairTransitions.size();i++){
			int fairTrans_i = fairTransitions.get(i);
			int newFairTrans_i  = bdd.ref(bdd.and(fairTrans_i, sameFlag));
			newFairTransitions.add(newFairTrans_i);
		}
		
		bdd.deref(sameFlag);
		bdd.deref(reachTrans);
				
//				UtilityMethods.debugBDDMethods(bdd, "reachTransitions", reachTrans1);
				
				//create game based on reachability obj
		FairGame reachabilityGame=new FairGame(bdd, vars, primedVars, reachabilityGameInit, reachabilityGameT_env, reachabilityGameT_sys, this.actionVars, newFairTransitions);
		return reachabilityGame;
	}
	
	/**
	 * creates the memory variable for a  reachability objective which indicates whether the objective is 
	 * fulfilled or not
	 * @param namePrefix
	 * @return
	 */
	public Variable[] createReachabilityFlag(String namePrefix){
		Variable flag = new Variable(bdd,namePrefix+"f");
		Variable flagPrime = new Variable(bdd,"namePrefix"+"fPrime");
		Variable[] flagVars = {flag, flagPrime};
		return flagVars;
	}
	
	public int reachabilityGameInit(int targetState, Variable flag){
		//init= init & targetState <-> f
		int initReachabilityFormula=bdd.ref(bdd.biimp(targetState, flag.getBDDVar()));
		initReachabilityFormula = bdd.andTo(initReachabilityFormula, this.init);
		return initReachabilityFormula;
	}
	
	public int reachabilityGameTransitions(int targetState, int targetStatePrime, Variable flag, Variable flagPrime){
		// flag || targetStatePrime <-> flagPrime
		int tmp = bdd.ref(bdd.or(flag.getBDDVar(), targetStatePrime));
		int transitions = bdd.ref(bdd.biimp(tmp, flagPrime.getBDDVar()));
		bdd.deref(tmp);
		return transitions;
	}
	
	public FairGame removeUnreachableStates(){
		
		int reachable = reachable();
		int newT_env = bdd.ref(bdd.andTo(T_env, reachable));
		
		int cube = bdd.ref(bdd.and(getActionsCube(), getVariablesCube()));
		int reachableSystemStates = EpostImage(reachable, cube , T_env);

		int newT_sys =  bdd.ref(bdd.and(T_sys, reachableSystemStates));
		FairGame newGame=new FairGame(bdd, variables, primedVariables, init, newT_env, newT_sys, actionVars, fairTransitions);
		newGame.setActionMap(actionMap);
		return newGame;
	}
	
	public FairGame compose(FairGame game){
		Variable[] gameVariables = Variable.unionVariables(variables, game.variables);
		Variable[] gamePrimeVariables=Variable.unionVariables(primedVariables, game.primedVariables);
		
		int gameInit= bdd.ref(bdd.and(getInit(), game.getInit()));
		int gameEnvTrans = bdd.ref(bdd.and(getEnvironmentTransitionRelation(), game.getEnvironmentTransitionRelation()));
		int gameSysTrans = bdd.ref(bdd.and(getSystemTransitionRelation(), game.getSystemTransitionRelation()));
		
		Variable[] gameActionVars = Variable.unionVariables(actionVars, game.actionVars);
		
		ArrayList<Integer> newFairTrans =  new ArrayList<Integer>();
		newFairTrans.addAll(fairTransitions);
		newFairTrans.addAll(game.fairTransitions);
		
		FairGame composedGame= new FairGame(bdd, gameVariables, gamePrimeVariables, gameInit, gameEnvTrans, gameSysTrans, gameActionVars, newFairTrans);
		composedGame.setActionMap(composeActionMaps(getActionMap(), game.getActionMap()));
		return composedGame;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BDD bdd = new BDD(10000,1000);
//		FairGame g = TestCaseGenerator.simpleFairGame(bdd);
//		GameGraph gg = g.toGameGraph();
//		gg.draw("simpleGame.dot", 1);

	}
	
	

}
