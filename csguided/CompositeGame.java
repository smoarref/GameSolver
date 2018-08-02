package csguided;

import java.util.ArrayList;

import javax.rmi.CORBA.Util;

import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;

/**
 * The class CompositeGame represents compoite games which are composition of a set of games, however, their 
 * composition is delayed until computing the composition is really necessary
 * @author moarref
 *
 */
public class CompositeGame extends Game {
	
	private ArrayList<Game> games;
	
	private int actionsAvlSetsIndex;
	

	
	boolean[] getNextSysAct=null;
	int[] currentSysActs=null;
	int[] currentEnvActs=null;
	int previousSysTrans=-1;
	int previousEnvTrans = -1;
	boolean toggle = false;
	
	boolean envToggle=false;

	

	public CompositeGame(BDD argBdd){
		super(argBdd);
		games=null;
		
		//initializing to null, only computed if necessary
		this.variables=null;
		this.primedVariables=null;
		
		this.vTovPrime=null;
		this.vPrimeTov=null;
		
		this.init=-1;
		this.T_env=-1;
		this.T_sys=-1;
		
		this.variablesCube=-1;
		this.primeVariablesCube=-1;
		
		this.actionVars=null;
		this.actionsCube=-1;
	}
	
	public CompositeGame(BDD argBdd, ArrayList<Game> argGames){
		super(argBdd);
		games=argGames;
		
		//initializing to null, only computed if necessary
//		this.variables=null;
//		this.primedVariables=null;
//		
//		this.vTovPrime=null;
//		this.vPrimeTov=null;
//		
//		this.init=-1;
		composeVars();
		composeActionVars();
		composeInit();
		
		this.T_env=-1;
		this.T_sys=-1;
		
		this.variablesCube=-1;
		this.primeVariablesCube=-1;
		
//		this.actionVars=null;
//		this.actionsCube=-1;
	}
	
	public void addGame(Game g){
		if(games == null) games = new ArrayList<Game>();
		games.add(g);
	}
	
	public ArrayList<Game> getGames(){
		return games;
	}
	
	public Game getGame(int index){
		return games.get(index);
	}
	
	public int[] getVariables(){
		if(variables == null){
			composeVars();
		}
		return Variable.getBDDVars(variables);
	}
	
	public void composeVars(){
		variables = games.get(0).variables;
		for(int i=1;i<games.size();i++){
			variables=Variable.unionVariables(variables, games.get(i).variables);
		}
		composePrimeVars();
		createPermutations();
	}
	
	public int[] getPrimeVariables(){
		if(primedVariables == null){
			composePrimeVars();
		}
		return Variable.getBDDVars(primedVariables);
	}
	
	private void composePrimeVars(){
		primedVariables = games.get(0).primedVariables;
		for(int i=1;i<games.size();i++){
			primedVariables=Variable.unionVariables(primedVariables, games.get(i).primedVariables);
		}
	}
	
	public int[] getActions(){
		if(actionVars == null){
			composeActionVars();
		}
		return Variable.getBDDVars(actionVars);
	}
	
	private void composeActionVars(){
		actionVars = games.get(0).actionVars;
		envActionVars = games.get(0).envActionVars;
		sysActionVars = games.get(0).sysActionVars;
		for(int i=1;i<games.size();i++){
			actionVars=Variable.unionVariables(actionVars, games.get(i).actionVars);
			envActionVars = Variable.unionVariables(envActionVars, games.get(i).envActionVars);
			sysActionVars = Variable.unionVariables(sysActionVars, games.get(i).sysActionVars);
		}
		
		actions = enumerateActions();
		envActions = enumerateEnvActions();
		sysActions = enumerateSysActions();
	}
	
	public Permutation getVtoVprime(){
		if(vTovPrime == null){
			createPermutations();
		}
		return vTovPrime;
	}
	
	public Permutation getVprimetoV(){
		if(vPrimeTov == null){
			createPermutations();
		}
		return vPrimeTov;
	}
	
	private void createPermutations(){
		if(variables==null){
			composeVars();
		}
		if(primedVariables==null){
			composePrimeVars();
		}
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
	}
	
	public int getInit(){
		if(init==-1){
			composeInit();
		}
		return init;
	}
	
	private void composeInit(){
		init=bdd.ref(bdd.getOne());
		for(Game g : games){
			init=bdd.andTo(init, g.getInit());
		}
	}
	
	public int getEnvironmentTransitionRelation(){
		if(T_env==-1){
			composeT_env();
		}
		return T_env;
	}
	
	private void composeT_env(){
		T_env=bdd.ref(bdd.getOne());
//		int i=0;
		for(Game g : games){
//			if(i==1) UtilityMethods.debugBDDMethods(bdd, "T_env for "+g.id, g.getEnvironmentTransitionRelation());
//			i++;
			T_env=bdd.andTo(T_env, g.getEnvironmentTransitionRelation());
		}
	}
	
	public int getSystemTransitionRelation(){
		if(T_sys==-1){
			composeT_sys();
		}
		return T_sys;
	}
	
	private void composeT_sys(){
		T_sys=bdd.ref(bdd.getOne());
		for(Game g : games){
			T_sys=bdd.andTo(T_sys, g.getSystemTransitionRelation());
		}
	}
	
	public int getVariablesCube(){
		if(variablesCube==-1){
			composeVariablesCube();
		}
		return variablesCube;
	}
	
	private void composeVariablesCube(){
		variablesCube=bdd.ref(bdd.getOne());
		for(Game g : games){
			variablesCube=bdd.andTo(variablesCube, g.getVariablesCube());
		}
	}
	
	public int getPrimeVariablesCube(){
		if(primeVariablesCube==-1){
			composePrimeVariablesCube();
		}
		return primeVariablesCube;
	}
	
	private void composePrimeVariablesCube(){
		primeVariablesCube=bdd.ref(bdd.getOne());
		for(Game g : games){
			primeVariablesCube=bdd.andTo(primeVariablesCube, g.getPrimeVariablesCube());
		}
	}
	
	
	public int getActionsCube(){
		if(actionsCube==-1){
			composeActionsCube();
		}
		return actionsCube;
	}
	
	private void composeActionsCube(){
		actionsCube=bdd.ref(bdd.getOne());
		for(Game g : games){
			actionsCube=bdd.andTo(actionsCube, g.getActionsCube());
		}
	}
	
	public CompositeGame compose(Game g){
		ArrayList<Game> composition = UtilityMethods.copyArrayList(games);
		composition.add(g);
		CompositeGame cg=new CompositeGame(bdd, composition);
		return cg;
	}
	
	//TODO: compute composition
	public void computeComposition(){
		composeVars();
		
		composeInit();
		composeT_env();
		composeT_sys();
		composeVariablesCube();
		composePrimeVariablesCube();
		composeActionVars();
		composeActionsCube();
	}
	
//	//TODO: it is for separable game structures, generalize later
//	//TODO: the environment part might have problems
//	public int EpreImage(int set, int cube, Player p){
//		
////		System.out.println("in composite game Epre");
////		UtilityMethods.debugBDDMethods(bdd, "set of states are", set);
////		UtilityMethods.debugBDDMethods(bdd, "cube is", cube);
////		System.out.println("Player is "+p.toString());
////		UtilityMethods.getUserInput();
//		
//		int preImage=bdd.ref(bdd.getZero());
//		if(p == Player.ENVIRONMENT){
//			//model of the environment is the same for all
////			Game g =  games.get(0);
//			
//			preImage = bdd.ref(bdd.getOne());
//			for(Game g : games){
//				int agentPreImage = bdd.ref(bdd.getZero());
//				while(g.hasNextTransition(p)){
//					int trans = g.getNextEnvTransition();
//					
////					UtilityMethods.debugBDDMethods(bdd,"trans is ", trans);
//					
//					int pre = g.EpreImage(set,cube,trans);
//					
////					UtilityMethods.debugBDDMethods(bdd, "Epre for trans is ", pre);
//					agentPreImage=bdd.orTo(agentPreImage, pre);
//					bdd.deref(pre);	
//				}
////				System.out.println("game "+g.id);
////				UtilityMethods.debugBDDMethods(bdd, "env agent pre image", agentPreImage);
////				UtilityMethods.getUserInput();
//				
//				preImage = bdd.andTo(preImage, agentPreImage);
//				bdd.deref(agentPreImage);
//			}
//		}else{
//			
//			preImage = bdd.ref(bdd.getOne());
//			for(Game g : games){
//				int agentPreImage = bdd.ref(bdd.getZero());
//				while(g.hasNextTransition(p)){
//					int trans = g.getNextSysTransition();
//					
////					UtilityMethods.debugBDDMethods(bdd,"trans is ", trans);
//					
//					//TODO: maybe restricting the cube?
//					int pre = g.EpreImage(set,cube,trans);
//					
////					UtilityMethods.debugBDDMethods(bdd, "Epre for trans is ", pre);
//					
//					agentPreImage=bdd.orTo(agentPreImage, pre);
//					bdd.deref(pre);
//				}
//				
////				System.out.println("game "+g.id);
////				UtilityMethods.debugBDDMethods(bdd, "sys agent pre image", agentPreImage);
////				UtilityMethods.getUserInput();
//				
//				preImage = bdd.andTo(preImage, agentPreImage);
//				bdd.deref(agentPreImage);
//			}
//		}
////		UtilityMethods.debugBDDMethods(bdd, "computed pre image is ", preImage);
////		UtilityMethods.getUserInput();
//		
//		return preImage;
//	}
	
	//TODO: for separable games, generalize
	//TODO: exponential blow up because of direct computation of actionsAvlSets
	public void actionAvailabilitySets(){
//		System.out.println("in composite game action avl sets");
//		UtilityMethods.getUserInput();
		
		Game firstGame = games.get(0);
//		envAvlActions = firstGame.envAvlActions;
		
		envAvlActions = new int[envActions.length];
		for(int i=0;i<envAvlActions.length;i++){
			envAvlActions[i]= bdd.ref(bdd.getOne());
			for(Game g : games){
				int[] gActions = g.getEnvActions();
				for(int j=0; j<gActions.length; j++){
					int act = gActions[j];
					int imp = bdd.ref(bdd.imp(envActions[i], act ));
					if(imp==1){
						envAvlActions[i]=BDDWrapper.andTo(bdd, envAvlActions[i], g.envAvlActions[j]);
					}
					BDDWrapper.free(bdd, imp);
				}
			}
		}
		
		sysAvlActions = new int[sysActions.length];
		for(int i=0;i<sysAvlActions.length;i++){
			sysAvlActions[i]= bdd.ref(bdd.getOne());
			for(Game g : games){
				int[] gActions = g.getSysActions();
				for(int j=0; j<gActions.length; j++){
					int act = gActions[j];
					int imp = bdd.ref(bdd.imp(sysActions[i], act ));
					if(imp==1){
						sysAvlActions[i]=BDDWrapper.andTo(bdd, sysAvlActions[i], g.sysAvlActions[j]);
					}
					BDDWrapper.free(bdd, imp);
				}
			}
		}
//		actionsAvlSetsIndex = 0;
//		
//		for(int i=0;i<firstGame.envAvlActions.length;i++){
//			envActionsAvailabilitySets(bdd.ref(firstGame.envAvlActions[i]), 1);
//		}
//		
//		sysAvlActions = new int[sysActions.length];
//		for(int i=0;i<sysAvlActions.length;i++){
//			sysAvlActions[i]= bdd.ref(bdd.getOne());
//		}
//		actionsAvlSetsIndex = 0;
//		
//		for(int i=0;i<firstGame.sysAvlActions.length;i++){
//			sysActionsAvailabilitySets(bdd.ref(firstGame.sysAvlActions[i]), 1);
//		}
		
//		for(Game g : games){
//			g.printGameVars();
//			for(int i=0; i<g.envActions.length;i++){
//				UtilityMethods.debugBDDMethods(bdd, "envAction", g.envActions[i]);
//				UtilityMethods.debugBDDMethods(bdd, "envAction avl at", g.envAvlActions[i]);
//			}
//			
//			for(int i=0; i<g.sysActions.length;i++){
//				UtilityMethods.debugBDDMethods(bdd, "sysAction", g.sysActions[i]);
//				UtilityMethods.debugBDDMethods(bdd, "sysAction avl at", g.sysAvlActions[i]);
//			}
//			
//			UtilityMethods.getUserInput();
//		}
//		
//		for(int i=0; i<envActions.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "envAction", envActions[i]);
//			UtilityMethods.debugBDDMethods(bdd, "envAction avl at", envAvlActions[i]);
//		}
//		
//		for(int i=0; i<sysActions.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "sysAction", sysActions[i]);
//			UtilityMethods.debugBDDMethods(bdd, "sysAction avl at", sysAvlActions[i]);
//		}
//		
//		UtilityMethods.getUserInput();
	}
	
	private void envActionsAvailabilitySets(int avl, int gameIndex){
		if(gameIndex == games.size()){
			envAvlActions[actionsAvlSetsIndex] = bdd.ref(avl);
			actionsAvlSetsIndex++;
			return;
		}
		Game currentGame = games.get(gameIndex);
		gameIndex++;
		for(int i=0;i<currentGame.envAvlActions.length; i++){
			int actAvl = bdd.ref(bdd.and(avl, currentGame.envAvlActions[i]));
			envActionsAvailabilitySets(actAvl, gameIndex);
			bdd.deref(actAvl);
		}
	}
	
	private void sysActionsAvailabilitySets(int avl, int gameIndex){
		if(gameIndex == games.size()){
			sysAvlActions[actionsAvlSetsIndex] = bdd.ref(avl);
			actionsAvlSetsIndex++;
			return;
		}
		Game currentGame = games.get(gameIndex);
		gameIndex++;
		for(int i=0;i<currentGame.sysAvlActions.length; i++){
			int actAvl = bdd.ref(bdd.and(avl, currentGame.sysAvlActions[i]));
			sysActionsAvailabilitySets(actAvl, gameIndex);
			bdd.deref(actAvl);
		}
	}
	
	//TODO: for separable game structures, generalize
	//TODO: env and sys methods are very similar, merge them
	public boolean hasNextTransition(Player p){
		//assumption: the model of the environment is the same for every agent
		
		if(p==Player.ENVIRONMENT){
			if(currentEnvActs == null){
				currentEnvActs = new int[games.size()];
				for(int i=0; i< games.size();i++){
					currentEnvActs[i]=games.get(i).getNextEnvTransition();
				}
			}
			
			if(previousEnvTrans !=-1) bdd.deref(previousEnvTrans);
			previousEnvTrans = getNextEnvTransRec(0);
			
//			System.out.println("pre is "+previousSysTrans);
			
//			boolean h = hasNextRec(0);
//			if(!h){
			if(previousEnvTrans == -1){
//				System.out.println("no next trans");
				resetEnvTransVars();
				for(Game g : games){
					g.resetEnvTransVars();
				}
				return false;
			}
			return true;
		}else{
			sysReset = false;
			
			if(currentSysActs == null){
				currentSysActs = new int[games.size()];
				for(int i=0; i< games.size();i++){
					currentSysActs[i]=games.get(i).getNextSysTransition();
				}
//				resetSysTransVars();
			}
			
			if(previousSysTrans !=-1) bdd.deref(previousSysTrans);
			previousSysTrans = getNextSysTransRec(0);
			
//			System.out.println("pre is "+previousSysTrans);
			
//			boolean h = hasNextRec(0);
//			if(!h){
			if(previousSysTrans == -1){
//				System.out.println("no next trans");
				resetSysTransVars();
				for(Game g : games){
					g.resetSysTransVars();
				}
				return false;
			}
			return true;
		}

	}
	
	public boolean hasNextRec(int gameIndex){
		if(gameIndex == games.size()){
			if(toggle){
//				toggle = !toggle;
				return false;
			}else{
//				toggle = !toggle;
				return true;
			}
//			getNextSysAct[gameIndex-1] = true;
//			return bdd.ref(bdd.getOne());
		}
		
		boolean next = hasNextRec(gameIndex+1);
		
		if(!next){
			Game currentGame = games.get(gameIndex);
			if(currentGame.hasNextTransitionWithoutReset(Player.SYSTEM)){
				return true;
			}else{
				return false;
			}
		}
		
		return true;
	}
	
	//TODO: has problems
	public int getNextEnvTransition(){
//		if(hasNextTransition(Player.ENVIRONMENT)){
//			
//			envReset = false;
//			
//			int nextEnvT = bdd.ref(bdd.getOne());
//			for(Game g : games){
//				int gameTrans = g.getNextEnvTransition();
//				nextEnvT = bdd.andTo(nextEnvT, gameTrans);
////				UtilityMethods.debugBDDMethods(bdd, g.id+"'s next env trans", gameTrans);
////				UtilityMethods.getUserInput();
//			}
//			
//
//			if(previousEnvTrans != -1 ) bdd.deref(previousEnvTrans);
//			previousEnvTrans = nextEnvT;
//			return nextEnvT;
//		}
//		return -1;
		
		return previousEnvTrans;
	
	}
	
	//TODO: assumes that each transition system is at its initial position 
	//and that all of them have at least one transition. Ensure correctness
	public void resetSysTransVars(){
		super.resetSysTransVars();
		toggle = false;
//		getNextSysAct = new boolean[games.size()];
		currentSysActs = null;
		if(previousSysTrans != -1) bdd.deref(previousSysTrans);
		previousSysTrans = -1;
		sysReset = true;
	}
	
	public void resetEnvTransVars(){
		super.resetEnvTransVars();
		envToggle = false;
//		getNextSysAct = new boolean[games.size()];
		currentEnvActs = null;
		if(previousEnvTrans != -1) bdd.deref(previousEnvTrans);
		previousEnvTrans = -1;
		envReset = true;
	}
	
	public int getNextSysTransRec(int gameIndex){
		
//		if(!hasNextTransition(Player.SYSTEM)){
//			return -1;
//		}
		
		
		
		if(gameIndex == games.size()){
			if(toggle){
				toggle = !toggle;
				return -1;
			}else{
				toggle = !toggle;
				return bdd.ref(bdd.getOne());
			}
//			getNextSysAct[gameIndex-1] = true;
//			return bdd.ref(bdd.getOne());
		}
		
		int nextTrans = getNextSysTransRec(gameIndex+1);
		
//		System.out.println("next trans"+nextTrans);
//		UtilityMethods.debugBDDMethods(bdd, "in comp get next sys trans, nextTrans is ", nextTrans);
//		UtilityMethods.getUserInput();
		
		if(nextTrans == -1){//need to move to the next transition
			Game currentGame = games.get(gameIndex);
//			System.out.println("current game is "+currentGame.id);
			//TODO: in current version we always assume that hasNext and getNext are called together
			if(currentGame.hasNextTransition(Player.SYSTEM)){
//				System.out.println("current game does have next transition");
				currentSysActs[gameIndex] = currentGame.getNextSysTransition();
				nextTrans =  getNextSysTransRec(gameIndex+1);
			}else{
				currentSysActs[gameIndex] = currentGame.getNextSysTransition();
//				System.out.println("current "+currentGame.id+" does not have next transition");
				return -1;
			}
		}
		
		
		int nextSysT = bdd.ref(bdd.and(currentSysActs[gameIndex], nextTrans));
		bdd.deref(nextTrans);
		return nextSysT;
		
	}
	
	public int getNextSysTransition(){
//		if(!hasNextTransition(Player.SYSTEM)){
//			return -1;
//		}
		
//		sysReset = false;
//		
//		if(currentSysActs == null){
//			resetSysTransVars();
//		}
//		
//		if(previousSysTrans !=-1) bdd.deref(previousSysTrans);
//		previousSysTrans = getNextSysTransRec(0);
		
//		UtilityMethods.debugBDDMethods(bdd, "next sys trans", previousSysTrans);
//		UtilityMethods.getUserInput();
		
		return previousSysTrans;
	}
	
	public int getNextEnvTransRec(int gameIndex){
		
		
		if(gameIndex == games.size()){
			if(envToggle){
				envToggle = !envToggle;
				return -1;
			}else{
				envToggle = !envToggle;
				return bdd.ref(bdd.getOne());
			}
//			getNextSysAct[gameIndex-1] = true;
//			return bdd.ref(bdd.getOne());
		}
		
		int nextTrans = getNextEnvTransRec(gameIndex+1);
		
//		System.out.println("next trans"+nextTrans);
//		UtilityMethods.debugBDDMethods(bdd, "in comp get next sys trans, nextTrans is ", nextTrans);
//		UtilityMethods.getUserInput();
		
		if(nextTrans == -1){//need to move to the next transition
			Game currentGame = games.get(gameIndex);
//			System.out.println("current game is "+currentGame.id);
			//TODO: in current version we always assume that hasNext and getNext are called together
			if(currentGame.hasNextTransition(Player.ENVIRONMENT)){
//				System.out.println("current game does have next transition");
				currentEnvActs[gameIndex] = currentGame.getNextEnvTransition();
				nextTrans =  getNextEnvTransRec(gameIndex+1);
			}else{
				currentEnvActs[gameIndex] = currentGame.getNextEnvTransition();
//				System.out.println("current "+currentGame.id+" does not have next transition");
				return -1;
			}
		}
		
		
		int nextEnvT = bdd.ref(bdd.and(currentEnvActs[gameIndex], nextTrans));
		bdd.deref(nextTrans);
		return nextEnvT;
		
	}
	public void testTransitionRelations(){
    	while(hasNextTransition(Player.ENVIRONMENT)){
    		UtilityMethods.debugBDDMethods(bdd, "next env trans", getNextEnvTransition());
    		UtilityMethods.getUserInput();
    	}
    	UtilityMethods.getUserInput();
    	
    	while(hasNextTransition(Player.SYSTEM)){
    		UtilityMethods.debugBDDMethods(bdd, "next sys trans", getNextSysTransition());
    		UtilityMethods.getUserInput();
    	}
    	UtilityMethods.getUserInput();
    	
    	System.out.println("checking again");
    	
    	while(hasNextTransition(Player.ENVIRONMENT)){
    		UtilityMethods.debugBDDMethods(bdd, "next env trans", getNextEnvTransition());
    	}
    	UtilityMethods.getUserInput();
    	
    	while(hasNextTransition(Player.SYSTEM)){
    		UtilityMethods.debugBDDMethods(bdd, "next sys trans", getNextSysTransition());
    	}
    	UtilityMethods.getUserInput();
    	
    	System.out.println("and even again");
    	
    	while(hasNextTransition(Player.ENVIRONMENT)){
    		UtilityMethods.debugBDDMethods(bdd, "next env trans", getNextEnvTransition());
    	}
    	UtilityMethods.getUserInput();
    	
    	while(hasNextTransition(Player.SYSTEM)){
    		UtilityMethods.debugBDDMethods(bdd, "next sys trans", getNextSysTransition());
    	}
    	UtilityMethods.getUserInput();
	}
	
	public int numOfVariables(){
		int num = 0;
		for(Game g : games){
			num+=g.numOfVariables();
		}
		return num;
	}

}
