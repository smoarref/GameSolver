package csguided;


import java.util.ArrayList;
import java.util.Arrays;

import utils.UtilityMethods;
import jdd.bdd.BDD;

//TODO: the cs guided control methods can be optimized in many ways, revise
//TODO: foucs and shatter's code has a lot in common, revise the code
//TODO: in current implementation the set of environment and system states are divided into the same equivalence 
//classes, because we have not separated the env and sys eq classes. revise
//TODO: check the code for bdd.deref methods, ensure consistency
//TODO: it seems that the problems with bdd package is because of ref and deref, especially check greatestFixedPoint method, check the methods for instances of Q=QPrime and check what happens to either of them when you deref one
//TODO: we need to give the reachability flag as an objective for initial abstraction, inspect why and if it is necessary
public class CSGuidedControl {
	
	private Game concreteGame;
	private int objective;
	
	private AbstractGame abstractGame;
	private int[] abstractionPredicates;
	private int abstractObjective;
	private BDD bdd;
	
	private ArrayList<Integer> envStatesFocusedSets;
	private ArrayList<Integer> sysStatesFocusedSets;
	
	private ArrayList<Integer> shatteredSets;
	
	
	
	public CSGuidedControl(BDD argBdd , Game game , int[] argAbstractionPredicates, int argObjective){
		bdd=argBdd;
		concreteGame = game;
		abstractionPredicates =  argAbstractionPredicates;
		objective = argObjective;
	}
	
	
	public CSGuidedControl(BDD argBdd , Game game , AbstractGame initialAbstraction, int argObjective){
		bdd=argBdd;
		concreteGame = game;
		abstractGame=initialAbstraction;
		objective = argObjective;
	}
	
	public AbstractGame getAbstractGame(){
		return abstractGame;
	}
	
	public Game getConcreteGame(){
		return concreteGame;
	}
	
	public ArrayList<Integer> getEnvStatesFocusedSets(){
		return envStatesFocusedSets;
	}
	
	public ArrayList<Integer> getSysStatesFocusedSets(){
		return sysStatesFocusedSets;
	}
	
	/**
	 * TODO: Note that the abstract predicates and objectives should be consistent
	 * @return
	 */
	
	public GameSolution counterStrategyGuidedControl(){
		//compute initial abstraction
		
		//abstractGame= initialAbstraction();
		
		//abstractGame.printGame();
		long t0;
		
		System.out.println("computing initial abstraction");
		t0 = UtilityMethods.timeStamp();
		computeInitialAbstraction();
		UtilityMethods.duration(t0, "initial abstraction computed in ");
		
//		abstractGame.printGame();
		
		
		//abstractGame.printGame();
		
//		System.out.println("printing initial abstraction");
//		abstractGame.printGame();
//		abstractGame.removeUnreachableStates().toGameGraph().draw("initAbs.dot", 1,0);
//		UtilityMethods.getUserInput();
				
		t0=UtilityMethods.timeStamp();
		GameSolver gs;
		GameSolution solution;
		char winner = 'u';//unknown
		do{
			abstractObjective = computeAbstractObjective(objective, abstractGame);
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete objective is ", objective);
//			UtilityMethods.debugBDDMethods(bdd, "refining the abs obj ", abstractObjective);
//			UtilityMethods.getUserInput();

			
//			System.out.println("cs guided control");
//			System.out.println("abs game before solving");
//			abstractGame.printEquivalenceClasses();
//			System.out.println("after");
//			abstractGame.printEquivalenceClasses();
			
//			System.out.println("csguided, solving the game");
//			t0=UtilityMethods.timeStamp();
			gs = new GameSolver(abstractGame, abstractObjective, bdd);
			solution = gs.solve();
//			UtilityMethods.duration(t0, "csguided, game solved in ");
			
//			solution.drawWinnerStrategy("absSolution.dot");
//			UtilityMethods.prompt("abstractGame solved, winner is "+solution.getWinner());
			
//			t0=UtilityMethods.timeStamp();
			if(solution.getWinner() == Player.SYSTEM){
				winner = 's';
			}else if(refineAbstraction(solution.strategyOfTheWinner())){
//				System.out.println("the abstraction was refined and returned true");
				winner='e';
			}
//			UtilityMethods.duration(t0, "abstraction refined in ");
//			abstractGame.printGame();
//			abstractGame.removeUnreachableStates().toGameGraph().draw("refinedAbs.dot", 1,0);
//			UtilityMethods.getUserInput();
			
		}while(winner == 'u');
		UtilityMethods.duration(t0, "cs guided control finished in ");
		return solution;
	}
	
	public GameSolution counterStrategyGuidedControlWithInitialAbstraction(){

				
		long t0=UtilityMethods.timeStamp();
		GameSolver gs;
		GameSolution solution;
		char winner = 'u';//unknown
		do{
			abstractObjective = computeAbstractObjective(objective, abstractGame);
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete objective is ", objective);
//			UtilityMethods.debugBDDMethods(bdd, "refining the abs obj ", abstractObjective);
//			UtilityMethods.getUserInput();

			
//			System.out.println("cs guided control");
//			System.out.println("abs game before solving");
//			abstractGame.printEquivalenceClasses();
//			System.out.println("after");
//			abstractGame.printEquivalenceClasses();
			
//			System.out.println("csguided, solving the game");
//			t0=UtilityMethods.timeStamp();
			gs = new GameSolver(abstractGame, abstractObjective, bdd);
			solution = gs.solve();
//			UtilityMethods.duration(t0, "csguided, game solved in ");
			
//			solution.drawWinnerStrategy("absSolution.dot");
//			UtilityMethods.prompt("abstractGame solved, winner is "+solution.getWinner());
			
//			t0=UtilityMethods.timeStamp();
			if(solution.getWinner() == Player.SYSTEM){
				winner = 's';
			}else if(refineAbstraction(solution.strategyOfTheWinner())){
//				System.out.println("the abstraction was refined and returned true");
				winner='e';
			}
			UtilityMethods.duration(t0, "abstraction refined in ");
//			abstractGame.printGame();
//			abstractGame.removeUnreachableStates().toGameGraph().draw("refinedAbs.dot", 1,0);
//			UtilityMethods.getUserInput();
			
		}while(winner == 'u');
		UtilityMethods.duration(t0, "cs guided control finished in ");
		return solution;
	}
	
	public AbstractGame computeInitialAbstraction(){
		int[] equivalenceClasses = getEquivalenceClassesFromPredicates(abstractionPredicates);
		abstractGame =  new AbstractGame(bdd, equivalenceClasses, concreteGame);
		return abstractGame;
	}
	
	
	/**
	 * returns true if the counter-strategy is genuine, false otherwise, refines the abstraction if the counter-strategy is spurious 
	 * @param counterStrategy
	 * @return
	 */
	public boolean refineAbstraction(Game counterStrategy){
	
		
//		System.out.println("checking cs");
		if(!checkCounterStrategy(counterStrategy)){
			
			long t0 =UtilityMethods.timeStamp();
//			System.out.println("cs checked, shattering");
			System.out.println("refining the abstraction");
//			System.out.println("old eq classes");
//			abstractGame.printEquivalenceClasses();
			
			shatteredSets=new ArrayList<Integer>();
			
			
//			long t0=UtilityMethods.timeStamp();
			//TODO: shattering can be optimized more, e.g., the equivalence classes that are not shattered can be kept the same
			int[] eqClasses=abstractGame.getEquivalenceClasses();
			for(int i=0;i<eqClasses.length;i++){
				shatteredSets.add(eqClasses[i]);
			}
			
			//add shattered sets here
			for(int i=0; i<envStatesFocusedSets.size();i++){
				shatteredSets.addAll(shatter(counterStrategy, abstractGame.equivalenceClassToAbstractStateMap(i), eqClasses[i], envStatesFocusedSets.get(i), 'e'));
			}
			
			for(int i=0; i<sysStatesFocusedSets.size();i++){
				shatteredSets.addAll(shatter(counterStrategy, abstractGame.equivalenceClassToAbstractStateMap(i), eqClasses[i], sysStatesFocusedSets.get(i), 's'));
			}
			
//			UtilityMethods.duration(t0, "shattered in ");
			
//			System.out.println("refining the abstraction");
		
//			System.out.println("computing new equivalence classes based on shattered sets");
//			t0 = UtilityMethods.timeStamp();
			int[] newEqClasses = computeEquivalenceClasses(shatteredSets);
//			UtilityMethods.duration(t0, "new eqClasses were computed");
			
			
			System.out.println("new eq classes");
//			
//			System.out.println("\nnew equivalence classes were computed ");
//			for(int i=0;i<newEqClasses.length;i++){
//				UtilityMethods.debugBDDMethods(bdd, "new eq class "+i, newEqClasses[i]);
//			}
			
//			System.out.println("printing the concrete game");
//			concreteGame.printGame();
			
			UtilityMethods.duration(t0, "refining abstraction in ");
			
			//computeAbstractGame(newEqClasses);
			long t02=UtilityMethods.timeStamp();
			abstractGame=new AbstractGame(bdd, newEqClasses, concreteGame);
			UtilityMethods.duration(t02, "abs game computed in ");
			
			
			
			
			
					
			return false;
		}
		return true;
	}
	
	/**
	 * given a set of shattered sets, computes the equivalence classes 
	 * where a and b are in the same equivalence class exactly when for each set[i], a \in set[i] <-> b \in set[i]
	 * @param sets
	 * @return
	 */
	public int[] computeEquivalenceClasses(ArrayList<Integer> sets){
		
//		System.out.println("Shattered sets are ");
//		for(int i=0;i<sets.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "\nset "+i, sets.get(i));
//		}
		
		ArrayList<Integer> eqClasses=sets;
		ArrayList<Integer> eqClassesPrime=new ArrayList<Integer>();
		ArrayList<Integer> newShatteredSets=new ArrayList<Integer>();
		while(!shatteredSetsFixedPoint(eqClasses, eqClassesPrime)){
			eqClassesPrime = eqClasses;
			newShatteredSets =  new ArrayList<Integer>();
			for(int i=0;i<eqClasses.size();i++){
				for(int j=i+1;j<eqClasses.size();j++){
					
//					UtilityMethods.debugBDDMethods(bdd, "eq class "+i, eqClasses.get(i));
//					UtilityMethods.debugBDDMethods(bdd, "eq class "+j, eqClasses.get(j));
					
					int intersection = bdd.ref(bdd.and(eqClasses.get(i), eqClasses.get(j)));
					
//					UtilityMethods.debugBDDMethods(bdd, "intersection", intersection);
					
					int diff1 = BDDWrapper.diff(bdd, eqClasses.get(i), eqClasses.get(j));
					
//					UtilityMethods.debugBDDMethods(bdd, "diff 1", diff1);
					
					int diff2 = BDDWrapper.diff(bdd, eqClasses.get(j), eqClasses.get(i));
					
//					UtilityMethods.debugBDDMethods(bdd, "diff 2", diff2);
					
					if(intersection != 0 && intersection !=1){
						if(!newShatteredSets.contains(intersection)){
							newShatteredSets.add(intersection);
							
//							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", intersection);
						}
					}
					
					if(diff1 != 0 && diff1 != 1){
						if(!newShatteredSets.contains(diff1)){
							newShatteredSets.add(diff1);
							
//							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", diff1);
						}
					}
					
					if(diff2 != 0 && diff2 != 1){
						if(!newShatteredSets.contains(diff2)){
							newShatteredSets.add(diff2);
							
//							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", diff2);
						}
					}
				}
			}
			//eqClasses=newShatteredSets;
			
			eqClasses=new ArrayList<Integer>();
			for(int i=0; i<newShatteredSets.size();i++){
				int subset=bdd.ref(bdd.getZero());
				//FALSE: if there is a set which is subset of this shattered set, remove it from the next iteration of equivalence classes
				for(int j=0;j<newShatteredSets.size();j++){
					if(j==i) continue;
					int diff  = BDDWrapper.diff(bdd, newShatteredSets.get(j), newShatteredSets.get(i));
					if(diff == 0){
						subset = bdd.orTo(subset, newShatteredSets.get(j));
						bdd.deref(diff);
					}
				}
				int currentShatteredSet = BDDWrapper.diff(bdd, newShatteredSets.get(i), subset);
				bdd.deref(subset);
				if(currentShatteredSet !=0 && !eqClasses.contains(currentShatteredSet)){
					eqClasses.add(currentShatteredSet);
				}
			}
			
//			System.out.println("New shattered sets are ");
//			for(int i=0;i<newShatteredSets.size();i++){
//				UtilityMethods.debugBDDMethods(bdd, "\nset "+i, newShatteredSets.get(i));
//			}
//			UtilityMethods.getUserInput();
//			
//			System.out.println("New eq classes are ");
//			for(int i=0;i<eqClasses.size();i++){
//				UtilityMethods.debugBDDMethods(bdd, "\nset "+i, eqClasses.get(i));
//			}
//			UtilityMethods.getUserInput();
		}
		
		int[] result=UtilityMethods.IntegerArrayListTointArray(newShatteredSets);
		
//		System.out.println("\nnew equivalence classes were computed ");
//		for(int i=0;i<result.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "new eq class "+i, result[i]);
//		}
		
		return result;
	}
	
	//TODO: can be optimized
	private boolean shatteredSetsFixedPoint(ArrayList<Integer> set1, ArrayList<Integer> set2){
		if(set1.size()==set2.size()){
			for(int i=0; i<set1.size();i++){
				if(!set2.contains(set1.get(i))){
					return false;
				}
				if(!set1.contains(set2.get(i))){
					return false;
				}
			}
			return true;
		}
		return false;
	}
	

	
	/**
	 * Computes the abstract game for the concrete game given the set of equivalence classes
	 * @param equivalenceClasses
	 */
	//TODO: copy and paste from initial abstraction, revise both methods

	

	
	/**
	 * returns true if a genuine counter-strategy, false otherwise
	 * @param cs
	 * @return
	 */
	public boolean checkCounterStrategy(Game cs){
		
		long t0=UtilityMethods.timeStamp();

		
		initializeFocusedSets();
		
		int init=cs.getInit();
//		UtilityMethods.debugBDDMethods(bdd, "checking the counter-strategy, initial state is", cs.getInit());
		
//		int[] absStates = abstractGame.enumerateAbstractStates(init);
//		System.out.println("enumeraing the set of abs states in init");
//		for(int as : absStates){
//			bdd.printSet(as);
//			UtilityMethods.debugBDDMethods(bdd, "the abstract state correspond to ", abstractGame.concretize(as));
//		}
		
//		long t0=UtilityMethods.timeStamp();
		int r = focus(cs,init,'e');
//		UtilityMethods.duration(t0, "checking counter-strategy in");
		
		UtilityMethods.duration(t0, "checking cs in ");
		
//		UtilityMethods.debugBDDMethods(bdd, "the root is ", r);
		//if r is empty
		if(r==0){
			return false;
		}
		return true;
	}
	
	public ArrayList<Integer> shatter(Game counterStrategy, int abstractState, int q , int r, char player){
		ArrayList<Integer> result = new ArrayList<Integer>();
		if(player=='e'){
			result.add(r);
			//q\r
			int diff=BDDWrapper.diff(bdd, q, r);
			result.add(diff);
			return result;
		}else if(player=='s'){
			result.add(r);
			//q\r
			int diff=BDDWrapper.diff(bdd, q, r);
			
			//get the set of successive abstract states according to the counter-strategy
			int cube = bdd.ref(bdd.and(counterStrategy.getActionsCube(), counterStrategy.getVariablesCube()));
			int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getSystemTransitionRelation());
			bdd.deref(cube);
			
			//enumerate the children abstract states and recursively focus on the child abstract states and update the focused set
			int[] nextAbstractStates=abstractGame.enumerateAbstractStates(nextAbsStates);
			bdd.deref(nextAbsStates);
			int concreteChildrenStates=bdd.ref(bdd.getZero());
			if(nextAbstractStates != null){
				for(int absState : nextAbstractStates){
					int concreteChild = focus(counterStrategy, absState, 'e');
					//compute the set of focused concrete state sets
					concreteChildrenStates=bdd.ref(bdd.orTo(concreteChildrenStates, concreteChild));
					//bdd.deref(concreteChild);
				}
			}
			
			
			//get the list of available and unavailable actions
			int availableActions = counterStrategy.getAvailableActions(abstractState, counterStrategy.getSystemTransitionRelation());
			//int unAvailableActions = bdd.ref(bdd.not(availableActions));
			int[] actionsList = concreteGame.enumerateActions();
			
			int cube2=concreteGame.getPrimeVariablesCube();
			int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getSystemTransitionRelation());
			int actionCube=concreteGame.getActionsCube();
			for(int act : actionsList){
				int isActAvailable = bdd.ref(bdd.and(act, availableActions));
				//if the action is available
				if(isActAvailable != 0){
					int actionPreTmp = bdd.ref(bdd.and(pre, act));
					int actionPre = bdd.ref(bdd.exists(actionPreTmp, actionCube));
					int notActionPre = bdd.ref(bdd.not(actionPre));
					int shatteredSet=BDDWrapper.diff(bdd, diff, notActionPre);
					bdd.deref(actionPre);
					bdd.deref(notActionPre);
					bdd.deref(actionPreTmp);
					result.add(shatteredSet);
				}else{
					int statesWithActAvailableTmp=concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());
					int statesWithActAvailable = bdd.ref(bdd.exists(statesWithActAvailableTmp, actionCube));
					int shatteredSet=bdd.ref(bdd.and(diff, statesWithActAvailable));
					bdd.deref(statesWithActAvailableTmp);
					result.add(shatteredSet);
				}
			}
//			bdd.deref(cube2);
			bdd.deref(pre);
//			bdd.deref(actionCube);
			return result;
		}
		
		System.err.println("Wrong player type");
		return null;
	}
	
	private void initializeFocusedSets(){
		envStatesFocusedSets = new ArrayList<Integer>();
		sysStatesFocusedSets = new ArrayList<Integer>();
		
		int[] eqClasses = abstractGame.getEquivalenceClasses();
		
		for(int i=0;i<eqClasses.length;i++){
			envStatesFocusedSets.add(eqClasses[i]);
			sysStatesFocusedSets.add(eqClasses[i]);
		}
	}
	
	/**
	 * given the counter-strategy and one of its abstract states, along with the type of the state, 
	 * computes a subset of states that might belong to a genuine counter-example and returns it
	 * @param counterStrategy
	 * @param abstractState
	 * @return
	 */
	public int focus(Game counterStrategy, int abstractState, char player){
		//interpret the abstract state to get the corresponding equivalence class
		boolean[] abstractStateToBoolArray = BDDWrapper.bddPrintSetMintermToBooleanArray(bdd, abstractState);
		int eqClassIndex = UtilityMethods.booleanArrayToInteger(abstractStateToBoolArray);
		if(player=='e'){
			
			
			
//			UtilityMethods.debugBDDMethods(bdd, "focusing on environement abstract state", abstractState);
			
			int availableActions = counterStrategy.getAvailableActions(abstractState, counterStrategy.getEnvironmentTransitionRelation());
			//if leaf, then error state
			if(availableActions==0){
				envStatesFocusedSets.set(eqClassIndex, abstractGame.concretize(abstractStateToBoolArray));				
				return envStatesFocusedSets.get(eqClassIndex);
			}else{
				int r = envStatesFocusedSets.get(eqClassIndex);
				//get the set of successive abstract states according to the counter-strategy
				int cube = bdd.ref(bdd.and(counterStrategy.getActionsCube(), counterStrategy.getVariablesCube()));
				int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getEnvironmentTransitionRelation());
				bdd.deref(cube);
				
//				UtilityMethods.debugBDDMethods(bdd, "next abstract states for env state", nextAbsStates);
				
				//enumerate the children abstract states and recursively focus on the child abstract states and update the focused set
				int[] nextAbstractStates=abstractGame.enumerateAbstractStates(nextAbsStates);
				bdd.deref(nextAbsStates);
				int concreteChildrenStates=bdd.ref(bdd.getZero());
				for(int absState : nextAbstractStates){

					
					int concreteChild = focus(counterStrategy, absState, 's');
					
//					UtilityMethods.debugBDDMethods(bdd, "focusing on abstract state ", absState);
//					UtilityMethods.debugBDDMethods(bdd, "concrete child is ", concreteChild);
					
					//compute the set of focused concrete state sets
					concreteChildrenStates=bdd.orTo(concreteChildrenStates, concreteChild);
					//bdd.deref(concreteChild);
				}
				
				
				
//				UtilityMethods.debugBDDMethods(bdd, "the set of concrete children states ", concreteChildrenStates);
				
				
				//compute the value for the current state
				int cube2=concreteGame.getPrimeVariablesCube();
				int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getEnvironmentTransitionRelation());
				
//				UtilityMethods.debugBDDMethods(bdd,"pre image of concrete children states", pre);
				
				pre = bdd.andTo(pre, availableActions);
				int epre = bdd.ref(bdd.exists(pre, counterStrategy.getActionsCube()));
				
//				UtilityMethods.debugBDDMethods(bdd,"pre image of concrete children states without actions", epre);
				
				//r=bdd.andTo(r, epre);
				r=bdd.ref(bdd.and(r, epre));
				envStatesFocusedSets.set(eqClassIndex, r);
				

				
				return r;
			}
		}else if(player=='s'){
			

//			System.out.println("focusing on eq class "+eqClassIndex);
			
			int r = sysStatesFocusedSets.get(eqClassIndex);
			
//			UtilityMethods.debugBDDMethods(bdd, "focus on ", r);
			
			//get the set of successive abstract states according to the counter-strategy
			int cube = bdd.ref(bdd.and(counterStrategy.getActionsCube(), counterStrategy.getVariablesCube()));
			int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getSystemTransitionRelation());
			bdd.deref(cube);
			
//			UtilityMethods.debugBDDMethods(bdd, "next abstract states ", nextAbsStates);

			
			
			//enumerate the children abstract states and recursively focus on the child abstract states and update the focused set
			int[] nextAbstractStates=abstractGame.enumerateAbstractStates(nextAbsStates);
			
//			if(nextAbstractStates == null) System.out.println("next abstract states are null");
			
			bdd.deref(nextAbsStates);
			

			
			int concreteChildrenStates=bdd.ref(bdd.getZero());
			if(nextAbstractStates != null){
				for(int absState : nextAbstractStates){
					int concreteChild = focus(counterStrategy, absState, 'e');
					//compute the set of focused concrete state sets
					concreteChildrenStates=bdd.orTo(concreteChildrenStates, concreteChild);
					//bdd.deref(concreteChild);
				}
			}
			
			//UtilityMethods.debugBDDMethods(bdd, "concrete children states are", concreteChildrenStates);
			

			
			//compute the value for the current state
			//get the list of available and unavailable actions
			int availableActions = counterStrategy.getAvailableActions(abstractState, counterStrategy.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "available actions are ", availableActions);
			//int unAvailableActions = bdd.ref(bdd.not(availableActions));
			int[] actionsList = concreteGame.enumerateActions();
			//alpha is the set of states where all available actions lead to a spoiling next state
			int alpha=bdd.ref(bdd.getOne());
			//beta is the set of states where all actions are contained in available actions
			int beta=bdd.ref(bdd.getOne());
			
			int cube2=concreteGame.getPrimeVariablesCube();
			int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getSystemTransitionRelation());
			int actionCube = concreteGame.getActionsCube();
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete game system trans", concreteGame.getSystemTransitionRelation());
			
			for(int act : actionsList){
				int isActAvailable = bdd.ref(bdd.and(act, availableActions));
				//if the action is available
				if(isActAvailable != 0){
					

					
					//compute alpha = \cap_{l \in Avl(n) Epre(\cup_j r_(l,j),l)}
					int actionPreTmp = bdd.ref(bdd.and(pre, act));
					int actionPre = bdd.ref(bdd.exists(actionPreTmp, actionCube));
					alpha=bdd.andTo(alpha, actionPre);
					bdd.deref(actionPre);
					bdd.deref(actionPreTmp);
				}else{
					
					
					
//					UtilityMethods.debugBDDMethods(bdd, "action is not available ", act);
					
					//compute beta = \cap_{l \not \in Avl(n)} \not(Avl(l))
					int statesWithActAvailable=concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());
					
//					UtilityMethods.debugBDDMethods(bdd, "state with action available", statesWithActAvailable);
					
//					int statesWithActAvailable=bdd.ref(bdd.exists(statesWithActAvailableTmp, actionCube));
//					
//					UtilityMethods.debugBDDMethods(bdd, "state with action available", statesWithActAvailable);
					
					int unavailable = bdd.ref(bdd.not(statesWithActAvailable));
					
//					UtilityMethods.debugBDDMethods(bdd, "state with action unavailable", unavailable);
					
					
					beta=bdd.andTo(beta, unavailable);
					bdd.deref(statesWithActAvailable);
//					bdd.deref(statesWithActAvailableTmp);
					
					
				}
			}
			

			
			
			//bdd.deref(cube2);
			bdd.deref(pre);
			//bdd.deref(actionCube);
			
//			UtilityMethods.debugBDDMethods(bdd, "alpha is ", alpha);
//			UtilityMethods.debugBDDMethods(bdd, "beta is ", beta);
//			UtilityMethods.debugBDDMethods(bdd, "r is ", r);
			
			//compute and return r \cap alpha \cap beta
			int alphaCapBeta = bdd.ref(bdd.and(alpha, beta));
			//r=bdd.andTo(r, alphaCapBeta);
			r=bdd.ref(bdd.and(r, alphaCapBeta));
			
			
			sysStatesFocusedSets.set(eqClassIndex, r);
			
//			UtilityMethods.debugBDDMethods(bdd, "focusing ", r);
			
//			bdd.gc();
//			bdd.gc();
//			System.out.println("after s");
//			abstractGame.printEquivalenceClasses();
			
			return r;
		}else{
			System.err.println("error: the player type can be either e or s");
		}
		return -1;
	}
	
	
	//TODO
	public static void main(String[] args) {
//		BDD bdd = new BDD(10000, 1000);
//		
//		int a  = bdd.createVar();
//		int b = bdd.createVar();
//		int c = bdd.createVar();
//		
////		int ab= bdd.ref(bdd.and(a, b));
//////		System.out.println(ab);
////		int ac= bdd.ref(bdd.and(a, c));
//////		System.out.println(ac);
////		
////		int ab_ac = bdd.ref(bdd.or(ab, ac));
////		int rest=bdd.ref(bdd.not(ab_ac));
////		bdd.deref(ab_ac);
//////		System.out.println(rest);
////		
//////		int ab_ac_rest=BDDWrapper.diff(bdd, rest, ab_ac);
//////		bdd.printSet(rest);
//////		bdd.printSet(ab_ac_rest);
//////		System.out.println(ab_ac_rest);
////		
////		ArrayList<Integer> classes = new ArrayList<Integer>();
////		classes.add(ab);
////		classes.add(ac);
////		classes.add(rest);
//		
//		//second test case
//		int nota = bdd.ref(bdd.not(a));
//		int notaAndb = bdd.ref(bdd.and(bdd.not(a), b));
//		int aAndNotb = bdd.ref(bdd.and(a, bdd.not(b)));
//		int aAndbOrc = bdd.ref(bdd.or(b, c));
//		aAndbOrc = bdd.andTo(aAndbOrc, a);
//		int all = bdd.getOne();
//		
//		ArrayList<Integer> classes = new ArrayList<Integer>();
//		classes.add(nota);
//		classes.add(notaAndb);
//		classes.add(aAndNotb);
//		classes.add(aAndbOrc);
//		classes.add(all);
//		
//		CSGuidedControl csgc = new CSGuidedControl(bdd, null, null, -1);
//		int[] result  = csgc.computeEquivalenceClasses(classes);
//		System.out.println("\nnew equivalence classes were computed ");
//		for(int i=0;i<result.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "new eq class "+i, result[i]);
//		}
		
		Tester.csGuidedControlTest2();

	}
	
	
	
//	
	
	public int[] getEquivalenceClassesFromPredicates(int[] predicates){
		int[] equivalenceClasses  = new int[(int) Math.pow(2, predicates.length)];
		boolean[] valuation=new boolean[predicates.length];
		equivalenceClassesFromPredicates(equivalenceClasses, valuation, 0, predicates);
		return equivalenceClasses;
	}
	
	private void equivalenceClassesFromPredicates(int[] equivalenceClasses, boolean[] valuation, int index,  int[] predicates){
		if(index==predicates.length){
			int eqClassIndex=UtilityMethods.booleanArrayToInteger(valuation);
			equivalenceClasses[eqClassIndex]=bdd.ref(bdd.getOne());
			for(int i=0; i<valuation.length;i++){
				int predicateValue = bdd.ref(valuation[i]?predicates[i]:bdd.not(predicates[i]));
				equivalenceClasses[eqClassIndex]=bdd.andTo(equivalenceClasses[eqClassIndex], predicateValue);
			}
			return;
		}
		valuation[index]=false;
		equivalenceClassesFromPredicates(equivalenceClasses, valuation, index+1, predicates);
		valuation[index]=true;
		equivalenceClassesFromPredicates(equivalenceClasses, valuation, index+1, predicates);
	}
	

	
	/**
	 * Transforms the objective for the concrete game to an abstract objective for the abstract game
	 * @param concreteObjective
	 * @param abstractGame
	 * @return
	 */
	public int computeAbstractObjective(int concreteObjective, AbstractGame abstractGame){
		
		int abstractObjective = bdd.ref(bdd.getZero());
		int[] eqClasses=abstractGame.getEquivalenceClasses();
		for(int i=0;i<eqClasses.length;i++){
			//TODO: it seems that the following commented code was wrong
//			int diff = BDDWrapper.diff(bdd, eqClasses[i], concreteObjective);
//			//if the equivalence class is a subset of states represented by the concrete objective
//			if(diff == 0){
//				//add the abstract state corresponding to this equivalence class to the abstract objective
//				int abstractState = abstractGame.equivalenceClassToAbstractStateMap(i);
//				abstractObjective=bdd.orTo(abstractObjective, abstractState);
//				bdd.deref(abstractState);
//			}
//			bdd.deref(diff);
			
			int intersection = BDDWrapper.intersect(bdd, concreteObjective, eqClasses[i]);
			//if the equivalence class is a subset of states represented by the concrete objective
			if(intersection != 0){
				//add the abstract state corresponding to this equivalence class to the abstract objective
				int abstractState = abstractGame.equivalenceClassToAbstractStateMap(i);
				abstractObjective=bdd.orTo(abstractObjective, abstractState);
				bdd.deref(abstractState);
			}
			bdd.deref(intersection);
		}
		return abstractObjective;
	}
	
	//TODO: clean up the code
//	private String booleanArrayToString(boolean[] arr ){
//	String result="";
//	for(int i=0;i<arr.length;i++){
//		result+=arr[i]?"1":"0";
//	}
//	return result;
//}
//
//private int booleanArrayToInteger(boolean[] arr){
//	String binary = booleanArrayToString(arr);
//	return Integer.parseInt(binary, 2);
//}
//
//private int numOfBits(int number){
//	String binary = Integer.toBinaryString(number);
//	return binary.length();
//}
//
//private int equivalenceClassToAbstractStateMap(int index, Variable[] abstractVariables){
//	int abstractState=bdd.getOne();
//	String binary = Integer.toBinaryString(index);
//	//append zeros in front of the binary if the length of binary and abstractVariables are not the same
//	int binaryLength=binary.length();
//	for(int i=0;i<(abstractVariables.length-binaryLength);i++){
//		binary="0"+binary;
//	}
//	for(int j=0;j<binary.length();j++){
//		if(binary.charAt(j)=='0'){
//			abstractState=bdd.andTo(abstractState, bdd.not(abstractVariables[j].getBDDVar()));
//		}else{
//			abstractState=bdd.andTo(abstractState, abstractVariables[j].getBDDVar());
//		}
//	}
//	return abstractState;
//}
	
	//TODO
//	public AbstractGame initialAbstraction(){
//		//removing this dummy variables will change the behavior of the bdd package
//		//TODO: why do we need these dummy references?
//		int dummy = bdd.ref(bdd.and(objective, bdd.getOne()));
//		int dummyInit=bdd.ref(bdd.and(concreteGame.getInit(), bdd.getOne()));
//		int dummyTenv=bdd.ref(bdd.and(concreteGame.getEnvironmentTransitionRelation(), bdd.getOne()));
//		int dummyTsys=bdd.ref(bdd.and(concreteGame.getSystemTransitionRelation(), bdd.getOne()));
//		
//		
//		//create the equivalence classes based on abstraction predicates, each equivalence class represents an abstract state
//		//TODO: probably we need to separate the equivalence classes for system and environment
//		int[] equivalenceClasses = getEquivalenceClassesFromPredicates(abstractionPredicates);
//		
////		System.out.println("equivalence classes are");
////		for(int ec : equivalenceClasses){
////			bdd.printSet(ec);
////		}
//		
//		//create the abstract variables based on equivalence classes
//		int numOfBits=numOfBits(equivalenceClasses.length-1);
//		Variable[] abstractVariables=new Variable[numOfBits];
//		for(int i=0; i<abstractVariables.length;i++){
//			abstractVariables[i]=new Variable(bdd, "absVar"+i);
//		}
//		
//		
//		
//		Variable[] abstractVariablesPrime=new Variable[numOfBits];
//		for(int i=0; i<abstractVariablesPrime.length;i++){
//			abstractVariablesPrime[i]=new Variable(bdd, "absVar"+(abstractVariablesPrime.length+i));
//		}
//		
////		System.out.println((2*numOfBits)+" abstract variables was created");
//		
////		UtilityMethods.debugBDDMethods(bdd, "the concrete game's initial state is", concreteGame.getInit());
//		
//		//create the initial relation \hat{I} of the abstract game
//		int hatI=bdd.getZero();
//		for(int i=0;i<equivalenceClasses.length;i++){
//			int initIntersection=bdd.ref(bdd.and(concreteGame.getInit(), equivalenceClasses[i]));
//			//if intersection is not empty
//			if(initIntersection != 0){
//				int abstractState=equivalenceClassToAbstractStateMap(i, abstractVariables);
//				hatI=bdd.orTo(hatI, abstractState);
//				bdd.deref(abstractState);
//			}
//			bdd.deref(initIntersection);
//		}
//		
//		
//		
//		
////		System.out.println("concrete Game's init state");
////		bdd.printSet(concreteGame.getInit());
////		System.out.println("the initial state for abstract game is");
////		bdd.printSet(hatI);
//		
//		//create the transition relation \hat{T}_env of the abstract game
//		int hatT_env=bdd.ref(bdd.getZero());
//		int variablesCube=concreteGame.getVariablesCube();
//		int variablesPrimeCube=concreteGame.getPrimeVariablesCube();
//		
//		
//		
//		for(int i=0;i<equivalenceClasses.length;i++){
//			
//			for(int j=0; j<equivalenceClasses.length;j++){
//				
//				
//				
//				//if \exists s1 \in eqClass[i] and s2 \in eqClass[j] and l \in actions s.t. T_env(s,s',l), 
//				//then add eqClass[i],eqClass[j], l to hatT_env
//				
//				
//				
//				//bdd.printSet(equivalenceClasses[j]);
//				int eqClass_j_prime=bdd.ref(bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime));
//				
////				if(i==4 && j==13){
////					UtilityMethods.debugBDDMethods(bdd, "objective is at the beginnign of loop "+i+" "+j, objective);
//////					UtilityMethods.debugBDDMethods(bdd, "eq class i ", equivalenceClasses[i]);
//////					UtilityMethods.debugBDDMethods(bdd, "eq class j ", eqClass_j_prime);
////				}
//				
//				
//				
////				if(i==4 && j==13){
////					UtilityMethods.debugBDDMethods(bdd, "dummy is ", dummy);
////				}
//				
//				int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//				
////				if(i==4) UtilityMethods.debugBDDMethods(bdd, "concrete init after trans "+i+" "+j, concreteGame.getInit());
//				
//				transition=bdd.andTo(transition, concreteGame.getEnvironmentTransitionRelation());
//				
//				
//				
////				UtilityMethods.debugBDDMethods(bdd, "computing possible trans between eq classes "+i+" & "+j, transition);
//				
//				int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
////				bdd.deref(variablesCube);
////				bdd.deref(variablesPrimeCube);
//				int actions = bdd.ref(bdd.exists(transition, cube));
//				
////				UtilityMethods.debugBDDMethods(bdd, "possible actions are", actions);
//				
//				bdd.deref(eqClass_j_prime);
//				bdd.deref(transition);
//				
//				
//				
//				//if the set of actions is not empty, add a transition between two abstract states representing
//				//the equivalence classes i and j
//				if(actions != 0){
//					int abstractState_i=equivalenceClassToAbstractStateMap(i, abstractVariables);					
//					int abstractState_j_prime=equivalenceClassToAbstractStateMap(j, abstractVariablesPrime);
//					int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//					abstractTransition=bdd.andTo(abstractTransition, actions);
//					hatT_env=bdd.orTo(hatT_env, abstractTransition);
//					bdd.deref(abstractState_i);
//					bdd.deref(abstractState_j_prime);
//					bdd.deref(abstractTransition);
//				}
//				bdd.deref(actions);
//				
//				
//			}
//			
//			
//		}
//		
//		
//		
////		System.out.println("the abstract environment transition relation is ");
////		bdd.printSet(hatT_env);
//		
//		//create the transition relation \hat{T}_sys of the abstract game
//		int hatT_sys=bdd.getZero();
//		int[] actionsList = concreteGame.enumerateActions();
//
////		System.out.println("the set of actions");
////		for(int acccc: actionsList){
////			bdd.printSet(acccc);
////		}
//		
//		//a weird behavior from JDD (probably a bug), if I remove the following line, the objective gets reset at some point
//		//int dummy=bdd.ref(bdd.and(objective, bdd.getOne()));
//		
//		
//		
//		int avl_acts[]=new int[actionsList.length];
//		for(int i=0; i< actionsList.length;i++){
//			avl_acts[i] = concreteGame.actionAvailabilitySet(actionsList[i], concreteGame.getSystemTransitionRelation());
////			UtilityMethods.debugBDDMethods(bdd,"action ", actionsList[i]);
////			UtilityMethods.debugBDDMethods(bdd,"is available at ", avl_acts[i]);
//		}
//		
//		
//		
//		for(int i=0;i<equivalenceClasses.length;i++){
//			
//			
//			
//			//first compute the set of available actions in the abstract state Avl^\alpha(v^\alpha)
//			//Avl^\alpha(v^\alpha) = \cap_(v \in v^\alpha) Avl(v)
//			
//			//for any action l, if eqClass[i] \subseteq Avl(l), then add l to Avl^\alpha
//			int abstractAvailabilityList_i=bdd.getZero();
//			
//			
//			for(int j=0;j<avl_acts.length;j++){
//				
////				System.out.println("current action is");
////				bdd.printSet(act);
//				
//				//TODO: this repetitive, move it to outside of loop
//				//int avl_act = concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());				
//				
////				System.out.println("the set of availale states are ");
////				bdd.printSet(avl_act);
//				
//				int subset = BDDWrapper.diff(bdd, equivalenceClasses[i], avl_acts[j]);
//				
////				System.out.println("current eq class is ");
////				bdd.printSet(equivalenceClasses[i]);
////				System.out.println("current action is"+(subset!=0?" NOT ":" ")+ "available at all states");
////				bdd.printSet(subset);
//				
//				//if eqClass[i] is a subset of Avl(act)
//				if(subset == 0){
//					//add the action to the availability list of class i
//					abstractAvailabilityList_i=bdd.orTo(abstractAvailabilityList_i, actionsList[j]);
//
//					
//				}
//				
//				bdd.deref(subset);
//				//bdd.deref(avl_act);
//			}
//			
//			
//			
//			
//			
////			System.out.println("the availability list is ");
////			bdd.printSet(abstractAvailabilityList_i);
//			
//			if(abstractAvailabilityList_i!=0){
//				for(int j=0; j<equivalenceClasses.length;j++){
//					//if there exists action l and states v and v' such that avl^\alpha(l) & v \in eqClass[i]
//					// & v' \in eqClass[j] & T_sys(v,v',l), then add a transition between two abstract states representing
//					//the equivalence classes i and j
//					
//					int eqClass_j_prime=bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime);
//					int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//					transition=bdd.andTo(transition, concreteGame.getSystemTransitionRelation());
//					int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
//					int existActions = bdd.ref(bdd.exists(transition, cube));
//					bdd.deref(eqClass_j_prime);
//					bdd.deref(transition);
//					//the set of available actions
//					int possibleActs = bdd.ref(bdd.and(existActions, abstractAvailabilityList_i));
//					bdd.deref(existActions);
//					if(possibleActs != 0){
//						int abstractState_i=equivalenceClassToAbstractStateMap(i, abstractVariables);
//						int abstractState_j_prime=equivalenceClassToAbstractStateMap(j, abstractVariablesPrime);
//						int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//						abstractTransition=bdd.andTo(abstractTransition, possibleActs);
//						hatT_sys=bdd.orTo(hatT_sys, abstractTransition);
//						bdd.deref(abstractState_i);
//						bdd.deref(abstractState_j_prime);
//						bdd.deref(abstractTransition);
//					}
//					bdd.deref(possibleActs);
//				}
//			}
//			
//
//		}
//		
//		
//		
//		for(int act : actionsList){
//			bdd.deref(act);
//		}
//		
//		
//		
////		System.out.println("the abstract system transition relation is ");
////		bdd.printSet(hatT_sys);
//		
//		//create the abstract game corresponding to \hat{I} and \hat{T}
//		abstractGame = new AbstractGame(bdd, abstractVariables, abstractVariablesPrime, hatI, hatT_env, hatT_sys, concreteGame.actionVars, concreteGame, equivalenceClasses);
//		
//		abstractGame.setActionMap(concreteGame.getActionMap());
//		
//		
//		//TODO: dummy vars should not be deref'ed, don't know why 
////		bdd.deref(dummy);
////		bdd.deref(dummyInit);
////		bdd.deref(dummyTenv);
////		bdd.deref(dummyTsys);
//		
//		return abstractGame;
//	}
	
//	private void addIntArray(ArrayList<Integer> arrlist, int[] arr){
//	for(int i=0;i<arr.length;i++){
//		arrlist.add(arr[i]);
//	}
//}
	
//	public AbstractGame computeAbstractGame(int[] equivalenceClasses){
//	
////	System.out.println("computing the abstract game");
////	System.out.println("concrete game info");
////	concreteGame.printGame();
//	
//	//create the abstract variables based on equivalence classes
//	int numOfBits=numOfBits(equivalenceClasses.length-1);
//	Variable[] abstractVariables=new Variable[numOfBits];
//	for(int i=0; i<abstractVariables.length;i++){
//		abstractVariables[i]=new Variable(bdd, "absVar"+i);
//	}
//			
//			
//			
//	Variable[] abstractVariablesPrime=new Variable[numOfBits];
//	for(int i=0; i<abstractVariablesPrime.length;i++){
//		abstractVariablesPrime[i]=new Variable(bdd, "absVar"+(abstractVariablesPrime.length+i));
//	}
//			
////	System.out.println((2*numOfBits)+" abstract variables was created");
//			
//	//create the initial relation \hat{I} of the abstract game
//	int hatI=bdd.getZero();
//	for(int i=0;i<equivalenceClasses.length;i++){
//		int initIntersection=bdd.ref(bdd.and(concreteGame.getInit(), equivalenceClasses[i]));
//		//if intersection is not empty
//		if(initIntersection != 0){
//			int abstractState=equivalenceClassToAbstractStateMap(i, abstractVariables);
//			hatI=bdd.orTo(hatI, abstractState);
//			bdd.deref(abstractState);
//		}
//		bdd.deref(initIntersection);
//	}
//			
////	System.out.println("concrete Game's init state");
////	bdd.printSet(concreteGame.getInit());
////	System.out.println("the initial state for abstract game is");
////	bdd.printSet(hatI);
//			
//	//create the transition relation \hat{T}_env of the abstract game
//	int hatT_env=bdd.getZero();
//	for(int i=0;i<equivalenceClasses.length;i++){
//		for(int j=0; j<equivalenceClasses.length;j++){
//			//if \exists s1 \in eqClass[i] and s2 \in eqClass[j] and l \in actions s.t. T_env(s,s',l), 
//			//then add eqClass[i],eqClass[j], l to hatT_env
//			int variablesCube=concreteGame.getVariablesCube();
//			int variablesPrimeCube=concreteGame.getPrimeVariablesCube();
//			int eqClass_j_prime=bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime);
//			int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//			transition=bdd.andTo(transition, concreteGame.getEnvironmentTransitionRelation());
//					
////			UtilityMethods.debugBDDMethods(bdd, "computing possible trans between eq classes "+i+" & "+j, transition);
//					
//			int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
//			int actions = bdd.ref(bdd.exists(transition, cube));
//					
////			UtilityMethods.debugBDDMethods(bdd, "possible actions are", actions);
//					
//			bdd.deref(eqClass_j_prime);
//			bdd.deref(transition);
//					
//			//if the set of actions is not empty, add a transition between two abstract states representing
//			//the equivalence classes i and j
//			if(actions != 0){
//				int abstractState_i=equivalenceClassToAbstractStateMap(i, abstractVariables);					
//				int abstractState_j_prime=equivalenceClassToAbstractStateMap(j, abstractVariablesPrime);
//				int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//				abstractTransition=bdd.andTo(abstractTransition, actions);
//				hatT_env=bdd.orTo(hatT_env, abstractTransition);
//				bdd.deref(abstractState_i);
//				bdd.deref(abstractState_j_prime);
//				bdd.deref(abstractTransition);
//			}
//			bdd.deref(actions);
//		}
//	}
//			
////	System.out.println("the abstract environment transition relation is ");
////	bdd.printSet(hatT_env);
//			
//	//create the transition relation \hat{T}_sys of the abstract game
//	int hatT_sys=bdd.getZero();
//	int[] actionsList = concreteGame.enumerateActions();
//
////	System.out.println("the set of actions");
////	for(int acccc: actionsList){
////		bdd.printSet(acccc);
////	}
//			
//	for(int i=0;i<equivalenceClasses.length;i++){
//		//first compute the set of available actions in the abstract state Avl^\alpha(v^\alpha)
//		//Avl^\alpha(v^\alpha) = \cap_(v \in v^\alpha) Avl(v)
//				
//		//for any action l, if eqClass[i] \subseteq Avl(l), then add l to Avl^\alpha
//		int abstractAvailabilityList_i=bdd.getZero();
//		for(int act : actionsList){
//					
////			System.out.println("current action is");
////			bdd.printSet(act);
//					
//			//TODO: this repetitive, move it to outside of loop
//			int avl_act = concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());
//					
////			System.out.println("the set of availale states are ");
////			bdd.printSet(avl_act);
//					
//			int subset = BDDWrapper.diff(bdd, equivalenceClasses[i], avl_act);
//					
////			System.out.println("current eq class is ");
////			bdd.printSet(equivalenceClasses[i]);
////			System.out.println("current action is"+(subset!=0?" NOT ":" ")+ "available at all states");
////			bdd.printSet(subset);
//					
//			//if eqClass[i] is a subset of Avl(act)
//			if(subset == 0){
//				//add the action to the availability list of class i
//				abstractAvailabilityList_i=bdd.orTo(abstractAvailabilityList_i, act);
//			}
//					
//			bdd.deref(subset);
//			bdd.deref(avl_act);
//		}
//				
////		System.out.println("the availability list is ");
////		bdd.printSet(abstractAvailabilityList_i);
//				
//		if(abstractAvailabilityList_i!=0){
//			for(int j=0; j<equivalenceClasses.length;j++){
//				//if there exists action l and states v and v' such that avl^\alpha(l) & v \in eqClass[i]
//				// & v' \in eqClass[j] & T_sys(v,v',l), then add a transition between two abstract states representing
//				//the equivalence classes i and j
//						
//				int variablesCube=concreteGame.getVariablesCube();
//				int variablesPrimeCube=concreteGame.getPrimeVariablesCube();
//				int eqClass_j_prime=bdd.replace(equivalenceClasses[j], concreteGame.vTovPrime);
//				int transition = bdd.ref(bdd.and(equivalenceClasses[i], eqClass_j_prime));
//				transition=bdd.andTo(transition, concreteGame.getSystemTransitionRelation());
//				int cube=bdd.ref(bdd.and(variablesCube, variablesPrimeCube));
//				int existActions = bdd.ref(bdd.exists(transition, cube));
//				bdd.deref(eqClass_j_prime);
//				bdd.deref(transition);
//				//the set of available actions
//				int possibleActs = bdd.ref(bdd.and(existActions, abstractAvailabilityList_i));
//				bdd.deref(existActions);
//				if(possibleActs != 0){
//					int abstractState_i=equivalenceClassToAbstractStateMap(i, abstractVariables);
//					int abstractState_j_prime=equivalenceClassToAbstractStateMap(j, abstractVariablesPrime);
//					int abstractTransition = bdd.ref(bdd.and(abstractState_i, abstractState_j_prime));
//					abstractTransition=bdd.andTo(abstractTransition, possibleActs);
//					hatT_sys=bdd.orTo(hatT_sys, abstractTransition);
//					bdd.deref(abstractState_i);
//					bdd.deref(abstractState_j_prime);
//					bdd.deref(abstractTransition);
//				}
//				bdd.deref(possibleActs);
//			}
//		}
//	}
//			
//	for(int act : actionsList){
//		bdd.deref(act);
//	}
//			
////	System.out.println("the abstract system transition relation is ");
////	bdd.printSet(hatT_sys);
//			
//	//create the abstract game corresponding to \hat{I} and \hat{T}
//	abstractGame = new AbstractGame(bdd, abstractVariables, abstractVariablesPrime, hatI, hatT_env, hatT_sys, concreteGame.actionVars, concreteGame, equivalenceClasses);
//	
//	abstractGame.setActionMap(concreteGame.getActionMap());
//	
//	return abstractGame;
//}
	
//	public Variable[] createAbtractVariables(int[] equivalenceClasses, String namePrefix){
//	//create the abstract variables based on equivalence classes
//	int numOfBits=numOfBits(equivalenceClasses.length-1);
//	Variable[] abstractVariables=new Variable[numOfBits];
//	for(int i=0; i<abstractVariables.length;i++){
//		abstractVariables[i]=new Variable(bdd, namePrefix+i);
//	}
//	return abstractVariables;
//}
}


