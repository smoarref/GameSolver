package csguided;

import java.util.ArrayList;
import jdd.bdd.BDD;
import utils.UtilityMethods;

public class ApproximateCSGuidedControl {
	private Game concreteGame;
	private int objective;
	
	private AbstractGame abstractGame;
	private int[] abstractionPredicates;
	private int abstractObjective;
	private BDD bdd;
	
	private ArrayList<Integer> envStatesFocusedSets;
	private ArrayList<Integer> sysStatesFocusedSets;
	private ArrayList<Boolean> envStatesFocusedSetsComputed;
	private ArrayList<Boolean> sysStatesFocusedSetsComputed;
	
//	private ArrayList<Integer> alphaStatesFocusedSets;
//	private ArrayList<Integer> betaStatesFocusedSets;
	
	private ArrayList<ArrayList<Integer>> statesWithExtraActionsFocusedSets;
	private ArrayList<ArrayList<Integer>> statesWithoutUnsafeSuccessorsFocusedSets;
	
	private int numOfRefinements=0;
	
	
	public ApproximateCSGuidedControl(BDD argBdd , Game game , int[] argAbstractionPredicates, int argObjective){
		bdd=argBdd;
		concreteGame = game;
		abstractionPredicates =  BDDWrapper.copyWithReference(bdd, argAbstractionPredicates);
		objective = bdd.ref(argObjective);
	}
	
	
	public ApproximateCSGuidedControl(BDD argBdd , Game game , AbstractGame initialAbstraction, int argObjective){
		bdd=argBdd;
		concreteGame = game;
		abstractGame=initialAbstraction;
		objective = bdd.ref(argObjective);
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
		long t0;
		
		System.out.println("computing initial abstraction");
		t0 = UtilityMethods.timeStamp();
		
		computeInitialAbstraction();
		
//		computeInitialAbstractionUsingActionAvlList();
		
		UtilityMethods.duration(t0, "initial abstraction computed in ", 500);
		
//		abstractGame.printGame();
//		UtilityMethods.getUserInput();
		
//		abstractGame.printGame();
//		
//		System.out.println("printing initial abstraction");
//		abstractGame.printGame();
////		abstractGame.removeUnreachableStates().toGameGraph().draw("initAbs.dot", 1,0);
//		UtilityMethods.getUserInput();
				
		t0=UtilityMethods.timeStamp();
		GameSolver gs;
		GameSolution solution = null;
		char winner = 'u';//unknown
		do{
//			System.out.println("printing the abstract game");
//			abstractGame.printGame();
//			UtilityMethods.getUserInput();
			
			long tAbsObj = UtilityMethods.timeStamp();
			abstractObjective = abstractGame.abstractionFunction(objective);
			UtilityMethods.duration(tAbsObj, "computing abstract objective in", 500);
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete objective is ", objective);
//			UtilityMethods.debugBDDMethods(bdd, "refining the abs obj ", abstractObjective);
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, abstractObjective, abstractGame.variables));
//			UtilityMethods.getUserInput();

			
//			System.out.println("cs guided control");
//			System.out.println("abs game before solving");
//			abstractGame.printEquivalenceClasses();
//			UtilityMethods.getUserInput();
//			System.out.println("after");
//			abstractGame.printEquivalenceClasses();
			if(solution != null){
				solution.cleanUp(bdd);
			}
			
			System.out.println("csguided, solving the game");
			long tSolve=UtilityMethods.timeStamp();
			gs = new GameSolver(abstractGame, abstractObjective, bdd);
			solution = gs.solve();
			UtilityMethods.duration(tSolve, "csguided, game solved in ", 500);
			
			BDDWrapper.free(bdd, abstractObjective);
			
//			solution.drawWinnerStrategy("absSolution.dot");
//			solution.strategyOfTheWinner().printGame();
//			UtilityMethods.prompt("abstractGame solved, winner is "+solution.getWinner());
			
//			if(solution.getWinner() == Player.ENVIRONMENT){
//				solution.drawWinnerStrategy("cs.dot", 0, 0);
//			}
			
//			t0=UtilityMethods.timeStamp();
			if(solution.getWinner() == Player.SYSTEM){
				winner = 's';
			}else if(refineAbstraction(solution.strategyOfTheWinner())){
//				System.out.println("the abstraction was refined and returned true");
				winner='e';
			}
//			UtilityMethods.duration(t0, "abstraction refined in ");
//			System.out.println("refined abstraction is ");
//			abstractGame.printGame();
//			abstractGame.removeUnreachableStates().toGameGraph().draw("refinedAbs.dot", 1,0);
//			UtilityMethods.getUserInput();
			
		}while(winner == 'u');
		UtilityMethods.duration(t0, "cs guided control finished in ",500);
		System.out.println("number of refinements "+numOfRefinements);
		return solution;
	}
	
	public GameSolution counterStrategyGuidedControlWithInitialAbstraction(){

				
		long t0=UtilityMethods.timeStamp();
		GameSolver gs;
		GameSolution solution= null;
		char winner = 'u';//unknown
		do{
//			System.out.println("printing the abstract game");
//			abstractGame.printGame();
//			UtilityMethods.getUserInput();
			
			long tAbsObj = UtilityMethods.timeStamp();
			abstractObjective = abstractGame.abstractionFunction(objective);
			UtilityMethods.duration(tAbsObj, "computing abstract objective in", 500);
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete objective is ", objective);
//			UtilityMethods.debugBDDMethods(bdd, "refining the abs obj ", abstractObjective);
//			System.out.println(BDDWrapper.BDDtoFormula(bdd, abstractObjective, abstractGame.variables));
//			UtilityMethods.getUserInput();

			
//			System.out.println("cs guided control");
//			System.out.println("abs game before solving");
//			abstractGame.printEquivalenceClasses();
//			UtilityMethods.getUserInput();
//			System.out.println("after");
//			abstractGame.printEquivalenceClasses();
			
			if(solution != null){
				solution.cleanUp(bdd);
			}
			
			System.out.println("csguided, solving the game");
			long tSolve=UtilityMethods.timeStamp();
			gs = new GameSolver(abstractGame, abstractObjective, bdd);
			solution = gs.solve();
			UtilityMethods.duration(tSolve, "csguided, game solved in ", 500);
			
			BDDWrapper.free(bdd, abstractObjective);
			
//			solution.drawWinnerStrategy("absSolution.dot");
//			solution.strategyOfTheWinner().printGame();
//			UtilityMethods.prompt("abstractGame solved, winner is "+solution.getWinner());
			
//			if(solution.getWinner() == Player.ENVIRONMENT){
//				solution.drawWinnerStrategy("cs.dot", 0, 0);
//			}
			
//			t0=UtilityMethods.timeStamp();
			if(solution.getWinner() == Player.SYSTEM){
				winner = 's';
			}else if(refineAbstraction(solution.strategyOfTheWinner())){
//				System.out.println("the abstraction was refined and returned true");
				winner='e';
			}
//			UtilityMethods.duration(t0, "abstraction refined in ");
//			System.out.println("refined abstraction is ");
//			abstractGame.printGame();
//			abstractGame.removeUnreachableStates().toGameGraph().draw("refinedAbs.dot", 1,0);
//			UtilityMethods.getUserInput();
			
		}while(winner == 'u');
		UtilityMethods.duration(t0, "cs guided control finished in ",500);
		System.out.println("number of refinements "+numOfRefinements);
		return solution;
	}
	
	public AbstractGame computeInitialAbstraction(){
		abstractGame =  new AbstractGame(bdd, abstractionPredicates, concreteGame, true);
		return abstractGame;
	}
	
	public AbstractGame computeInitialAbstractionUsingActionAvlList(){
		ArrayList<Integer> absPreds=new ArrayList<Integer>();
		for(int absP : abstractionPredicates){
			absPreds.add(absP);
		}
		for(int i=0;i<concreteGame.envAvlActions.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "env action "+i, concreteGame.envAvlActions[i]);
			absPreds.add(bdd.ref(concreteGame.envAvlActions[i]));
		}
		for(int i=0;i<concreteGame.sysAvlActions.length;i++){
//			UtilityMethods.debugBDDMethods(bdd, "sys action "+i, concreteGame.sysAvlActions[i]);
			absPreds.add(bdd.ref(concreteGame.sysAvlActions[i]));
		}
		int[] absPredicates = UtilityMethods.IntegerArrayListTointArray(absPreds);
		
		System.out.println("num of abstract predicates"+absPredicates.length);
		
		abstractGame =  new AbstractGame(bdd, absPredicates, concreteGame, true);
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
			
//			shatteredSets=new ArrayList<Integer>();
			
//			ArrayList<Integer> newEqClasses=new ArrayList<Integer>();
			
////			long t0=UtilityMethods.timeStamp();
//			//TODO: shattering can be optimized more, e.g., the equivalence classes that are not shattered can be kept the same
//			int[] eqClasses=abstractGame.getEquivalenceClasses();
//			for(int i=0;i<eqClasses.length;i++){
//				shatteredSets.add(eqClasses[i]);
//			}
			
			//add shattered sets here
//			for(int i=0; i<envStatesFocusedSets.size();i++){
//				newEqClasses.addAll(shatterAndRefine(i, Player.ENVIRONMENT));
//			}
//			
//			for(int i=0; i<sysStatesFocusedSets.size();i++){
//				newEqClasses.addAll(shatterAndRefine(i, Player.SYSTEM));
//			}
			
//			UtilityMethods.duration(t0, "shattered in ");
			
//			System.out.println("refining the abstraction");
		
//			System.out.println("computing new equivalence classes based on shattered sets");
//			t0 = UtilityMethods.timeStamp();
//			int[] newEqClasses = computeEquivalenceClasses(shatteredSets);
//			UtilityMethods.duration(t0, "new eqClasses were computed");
			
			
//			System.out.println("new eq classes");
//			
//			System.out.println("\nnew equivalence classes were computed ");
//			for(int i=0;i<newEqClasses.length;i++){
//				UtilityMethods.debugBDDMethods(bdd, "new eq class "+i, newEqClasses[i]);
//			}
			
//			System.out.println("printing the concrete game");
//			concreteGame.printGame();
			
			
//			ArrayList<Integer> newAbstractPredicates=shatterAndRefineAbstractPredicates();
			
//			System.out.println("printing new set of abstract predicates");
//			for(int i=0 ; i< newAbstractPredicates.size(); i++){
//				UtilityMethods.debugBDDMethods(bdd, "absPred"+i, newAbstractPredicates.get(i));
//				System.out.println();
//			}
//			UtilityMethods.getUserInput();
			
			ArrayList<Integer> newEquivalenceClasses = shatterAndRefineAbstraction();
			
			UtilityMethods.duration(t0, "refining eq classes in ", 500);
			
			//computeAbstractGame(newEqClasses);
			long t02=UtilityMethods.timeStamp();
			
//			int[] newAbstractPredicatesArray = UtilityMethods.IntegerArrayListTointArray(newAbstractPredicates);
//			abstractGame=new AbstractGame(bdd, newAbstractPredicatesArray, concreteGame,true);
			
			int[] newEqClasses = UtilityMethods.IntegerArrayListTointArray(newEquivalenceClasses);
			
			//free previous abstraction and focused sets
			abstractGame.cleanUp();
			freeFocusedSets();
			
			abstractGame=new AbstractGame(bdd, newEqClasses, concreteGame);
			
			BDDWrapper.free(bdd, newEqClasses);
			
//			abstractGame.printEquivalenceClasses();
//			UtilityMethods.getUserInput();
			
			numOfRefinements++;
			
			UtilityMethods.duration(t02, "abs game computed in ",500);
			
//			BDDWrapper.BDD_Usage(bdd);
					
			return false;
		}
		return true;
	}
	
	
	public void freeFocusedSets(){
//		for(int i=0;i<envStatesFocusedSets.size();i++){
//			BDDWrapper.free(bdd, envStatesFocusedSets.get(i));
//		}
//		for(int i=0;i<sysStatesFocusedSets.size();i++){
//			BDDWrapper.free(bdd, sysStatesFocusedSets.get(i));
//		}
		BDDWrapper.free(bdd, envStatesFocusedSets);
		BDDWrapper.free(bdd, sysStatesFocusedSets);
		
		//We deref them during shatterAndRefine
//		for(ArrayList<Integer> extra : statesWithExtraActionsFocusedSets){
//			BDDWrapper.free(bdd, extra);
//		}
//		
//		for(ArrayList<Integer> safe : statesWithoutUnsafeSuccessorsFocusedSets){
//			BDDWrapper.free(bdd, safe);
//		}
	}
	

	

	
	/**
	 * returns true if a genuine counter-strategy, false otherwise
	 * @param cs
	 * @return
	 */
	public boolean checkCounterStrategy(Game cs){
		
		long t0=UtilityMethods.timeStamp();

		System.out.println("Checking if the counter-strategy is genuine");
		initializeFocusedSets();
		
		int init=cs.getInit();
//		UtilityMethods.debugBDDMethods(bdd, "checking the counter-strategy, initial state is", cs.getInit());
		
//		int[] absStates = abstractGame.enumerateAbstractStates(init);
//		System.out.println("enumeraing the set of abs states in init");
//		for(int as : absStates){
//			bdd.printSet(as);
//			UtilityMethods.debugBDDMethods(bdd, "the abstract state correspond to ", abstractGame.concretize(as));
//		}
//		
//		UtilityMethods.getUserInput();
		
//		long t0=UtilityMethods.timeStamp();
		int r = focus(cs,init,'e');
//		UtilityMethods.duration(t0, "checking counter-strategy in");
		
		UtilityMethods.duration(t0, "checked cs in ", 500);
		
//		UtilityMethods.debugBDDMethods(bdd, "the root is ", r);
		//if r is empty
		if(r==0){
			return false;
		}
		return true;
	}
	
	
	

	
	public ArrayList<Integer> shatterAndRefineAbstraction(){
		int[] eqClasses=abstractGame.getEquivalenceClasses();
		
//		System.out.println("Old equivalence classes are");
//		abstractGame.printEquivalenceClasses();
//		UtilityMethods.getUserInput();
		
		ArrayList<Integer> newEqClasses=new ArrayList<Integer>();

		for(int i=0;i<eqClasses.length;i++){
			ArrayList<Integer> sets=new ArrayList<Integer>();
			
			int rEnv = envStatesFocusedSets.get(i);
			addFormula(sets, rEnv);
			
//			int diffEnv=BDDWrapper.diff(bdd, eqClasses[i], rEnv);
//			if(diffEnv != 0) addFormula(sets, diffEnv);
			
			int rSys = sysStatesFocusedSets.get(i);
			addFormula(sets, rSys);			
			//q\r
			int diff=BDDWrapper.diff(bdd, eqClasses[i], rSys);
			if(diff != 0){
//				addFormula(sets, diff);
				//q\r \cap Avl(l) | l \not in C(n)
	//			int statesWithExtraActions=BDDWrapper.intersect(bdd, diff, alphaStatesFocusedSets.get(i));			
				
				ArrayList<Integer> alphaSets = statesWithExtraActionsFocusedSets.get(i);
				
				for(int j=0;j<alphaSets.size();j++){
					int statesWithExtraActions=BDDWrapper.diff(bdd, diff, alphaSets.get(j));
					if(statesWithExtraActions != 0){
						addFormula(sets, statesWithExtraActions);
					}
					bdd.deref(statesWithExtraActions);
					bdd.deref(alphaSets.get(j));
				}
			
				
				
				//q\r \cap notBeta
				
				ArrayList<Integer> betaSets = statesWithoutUnsafeSuccessorsFocusedSets.get(i);
				
	//			int statesWithSafeSuccessors=BDDWrapper.intersect(bdd, diff, betaStatesFocusedSets.get(i));
				
				for(int j=0;j<betaSets.size();j++){
					int statesWithSafeSuccessors=BDDWrapper.diff(bdd, diff, betaSets.get(j));
					if(statesWithSafeSuccessors != 0){
						addFormula(sets, statesWithSafeSuccessors);
					}
					bdd.deref(statesWithSafeSuccessors);
					bdd.deref(betaSets.get(j));
				}
			}
			bdd.deref(diff);
			
			newEqClasses.addAll(partitionEquivalenceClass(eqClasses[i], sets));
			BDDWrapper.free(bdd, sets);
		}
		
		
//		for(int i=0;i<newEqClasses.size();i++){
//			UtilityMethods.debugBDDMethods(bdd, "eqClass "+i, newEqClasses.get(i));
//		}
//		UtilityMethods.getUserInput();
		
		return newEqClasses;
	}
	
	/**
	 * very similar to computeEquivalenceClassesFromAbstractPredicates() method in AbstractGame class
	 * @param sets
	 * @return
	 */
	public ArrayList<Integer> partitionEquivalenceClass(int eqClass, ArrayList<Integer> sets){
		ArrayList<Integer> eqClasses=new ArrayList<Integer>();
		partitionEqClass(bdd.getOne(), eqClass,  eqClasses, sets, 0);
		return eqClasses;
	}
	
	public void partitionEqClass(int formula, int eqClass, ArrayList<Integer> eqClasses, ArrayList<Integer> sets, int index){
		if(index==sets.size()){
			if(formula != 0) eqClasses.add(bdd.ref(formula));
			return;
		}
		
		int diff=BDDWrapper.diff(bdd, eqClass, sets.get(index));
		
		int f0 = bdd.ref(bdd.and(formula, sets.get(index)));
//		int notAbsPre = bdd.ref(bdd.not(abstractPredicates[index]));
		int f1 = bdd.ref(bdd.and(formula, diff));
		
		if(f0 != 0){
			partitionEqClass(f0, eqClass, eqClasses, sets, index+1);
		}
		
		if(f1 !=0){
			partitionEqClass(f1, eqClass, eqClasses, sets, index+1);
		}
		
//		if(index < sets.size()-1){
//			bdd.deref(f0);
//			bdd.deref(f1);
//		}
		
		bdd.deref(f0);
		bdd.deref(f1);
//		bdd.deref(notAbsPre);
	}
	
	public void addFormula(ArrayList<Integer> formulas, int newFormula){
		if(!formulas.contains(newFormula) && !formulas.contains(bdd.not(newFormula))){
			formulas.add(bdd.ref(newFormula));
		}
	}
	

	

	
	private void initializeFocusedSets(){
		envStatesFocusedSets = new ArrayList<Integer>();
		sysStatesFocusedSets = new ArrayList<Integer>();
		
		envStatesFocusedSetsComputed = new ArrayList<Boolean>();
		sysStatesFocusedSetsComputed = new ArrayList<Boolean>();
		
		//alpha is the set of states where all available actions lead to a spoiling next state
//		alphaStatesFocusedSets=new ArrayList<Integer>();
		//beta is the set of states where all actions are contained in available actions
//		betaStatesFocusedSets=new ArrayList<Integer>();
		
		int[] eqClasses = abstractGame.getEquivalenceClasses();
		
		statesWithExtraActionsFocusedSets=new ArrayList<ArrayList<Integer>>();
		statesWithoutUnsafeSuccessorsFocusedSets=new ArrayList<ArrayList<Integer>>();
		
		for(int i=0;i<eqClasses.length;i++){
			envStatesFocusedSets.add(bdd.ref(eqClasses[i]));
			sysStatesFocusedSets.add(bdd.ref(eqClasses[i]));
			envStatesFocusedSetsComputed.add(false);
			sysStatesFocusedSetsComputed.add(false);

//			alphaStatesFocusedSets.add(bdd.getZero());
//			betaStatesFocusedSets.add(bdd.getZero());
			
			statesWithExtraActionsFocusedSets.add(new ArrayList<Integer>());
			statesWithoutUnsafeSuccessorsFocusedSets.add(new ArrayList<Integer>());
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
//			long t_avl = UtilityMethods.timeStamp(); 
			
			int availableActions = counterStrategy.getAvailableActionsAtState(abstractState, Player.ENVIRONMENT);
			
//			UtilityMethods.duration(t_avl, "in focus, available actions computed", 500);
			
			//if leaf, then error state
			if(availableActions==0){
				envStatesFocusedSets.set(eqClassIndex, abstractGame.concretize(abstractStateToBoolArray));	
				BDDWrapper.free(bdd,availableActions);
				return envStatesFocusedSets.get(eqClassIndex);
			}else{
				int r = envStatesFocusedSets.get(eqClassIndex);
				//get the set of successive abstract states according to the counter-strategy
				//int cube = bdd.ref(bdd.and(counterStrategy.getActionsCube(), counterStrategy.getVariablesCube()));
				int cube = counterStrategy.getVariablesAndActionsCube();
				int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getEnvironmentTransitionRelation());
//				int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getT_envList());
//				bdd.deref(cube);
				
//				UtilityMethods.debugBDDMethods(bdd, "next abstract states for env state", nextAbsStates);
				
				//enumerate the children abstract states and recursively focus on the child abstract states and update the focused set
				int[] nextAbstractStates=abstractGame.enumerateAbstractStates(nextAbsStates);
				bdd.deref(nextAbsStates);
				
//				long t_conc = UtilityMethods.timeStamp();
				int concreteChildrenStates=bdd.ref(bdd.getZero());
				for(int absState : nextAbstractStates){
					
					int[] eqClassIndexForAbsState = abstractGame.eqClassIndices(absState);
					if(eqClassIndexForAbsState.length>1){
						System.out.println("abs state corresponds to more than one equivalence class!!");
						UtilityMethods.getUserInput();
					}
					
					int concreteChild;
					
					int index = eqClassIndexForAbsState[0];
					if(sysStatesFocusedSetsComputed.get(index)){
						concreteChild = sysStatesFocusedSets.get(index);
					}else{
						concreteChild = focus(counterStrategy, absState, 's');
					}
					
//					UtilityMethods.debugBDDMethods(bdd, "focusing on abstract state ", absState);
//					UtilityMethods.debugBDDMethods(bdd, "concrete child is ", concreteChild);
					
					//compute the set of focused concrete state sets
					concreteChildrenStates=bdd.orTo(concreteChildrenStates, concreteChild);
					//bdd.deref(concreteChild);
				}
				
//				UtilityMethods.duration(t_conc, "concrete children for env node, it has "+nextAbstractStates.length+" children, computed in ",500);
				
//				UtilityMethods.debugBDDMethods(bdd, "the set of concrete children states ", concreteChildrenStates);
				
				
				//compute the value for the current state
				int cube2=concreteGame.getPrimeVariablesCube();
				
//				int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getT_envList());
				
//				long t_epre = UtilityMethods.timeStamp();
				
				int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, Player.ENVIRONMENT);
				
//				UtilityMethods.debugBDDMethods(bdd, "Epre image for env in focus", pre);
//				UtilityMethods.getUserInput();
				
//				UtilityMethods.duration(t_epre, "epre image based on concrete game was computed in ", 500);
				
				bdd.deref(concreteChildrenStates);
				
//				UtilityMethods.debugBDDMethods(bdd,"pre image of concrete children states", pre);
				
				//TODO: why am I doing this? Is it necessary?
				pre = bdd.andTo(pre, availableActions);
				int epre = bdd.ref(bdd.exists(pre, counterStrategy.getActionsCube()));
				
//				UtilityMethods.debugBDDMethods(bdd,"pre image of concrete children states without actions", epre);
				
//				r=bdd.ref(bdd.and(r, epre));
				r=bdd.andTo(r, epre);
				envStatesFocusedSets.set(eqClassIndex, r);
				envStatesFocusedSetsComputed.set(eqClassIndex, true);
				

				BDDWrapper.free(bdd,availableActions);
				return r;
			}
		}else if(player=='s'){
//			System.out.println("Focusing on system node");

//			System.out.println("focusing on eq class "+eqClassIndex);
			
			int r = sysStatesFocusedSets.get(eqClassIndex);
			
//			UtilityMethods.debugBDDMethods(bdd, "focus on ", r);
			
			//get the set of successive abstract states according to the counter-strategy
//			int cube = bdd.ref(bdd.and(counterStrategy.getActionsCube(), counterStrategy.getVariablesCube()));
			int cube = counterStrategy.getVariablesAndActionsCube();
			
//			long t_csNext = UtilityMethods.timeStamp();
			
			int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getSystemTransitionRelation());
			
//			UtilityMethods.duration(t_csNext, "EpostImage from cs ", 500);
//			int nextAbsStates=counterStrategy.EpostImage(abstractState, cube, counterStrategy.getT_sysList());
//			bdd.deref(cube);
			
//			UtilityMethods.debugBDDMethods(bdd, "next abstract states ", nextAbsStates);

			
			
			//enumerate the children abstract states and recursively focus on the child abstract states and update the focused set
			int[] nextAbstractStates=abstractGame.enumerateAbstractStates(nextAbsStates);
			
//			if(nextAbstractStates == null) System.out.println("next abstract states are null");
			
			bdd.deref(nextAbsStates);
			

//			long t_conc = UtilityMethods.timeStamp();
			int concreteChildrenStates=bdd.ref(bdd.getZero());
			if(nextAbstractStates != null){
				for(int absState : nextAbstractStates){
					
					int[] eqClassIndexForAbsState = abstractGame.eqClassIndices(absState);
					if(eqClassIndexForAbsState.length>1){
						System.out.println("abs state corresponds to more than one equivalence class!!");
						UtilityMethods.getUserInput();
					}
					
					int concreteChild;
					int index = eqClassIndexForAbsState[0];
					if(envStatesFocusedSetsComputed.get(index)){
						concreteChild = envStatesFocusedSets.get(index);
					}else{
						concreteChild = focus(counterStrategy, absState, 'e');
					}
					
//					int concreteChild = focus(counterStrategy, absState, 'e');
					//compute the set of focused concrete state sets
					concreteChildrenStates=bdd.orTo(concreteChildrenStates, concreteChild);
					//bdd.deref(concreteChild);
				}
			}
			
//			if(nextAbstractStates != null) System.out.println("System node has "+nextAbstractStates.length+" children" );
//			UtilityMethods.duration(t_conc, "computing concrete children for system node ", 500);
			
			//UtilityMethods.debugBDDMethods(bdd, "concrete children states are", concreteChildrenStates);
			

			
			//compute the value for the current state
			//get the list of available and unavailable actions
//			int availableActions = counterStrategy.getAvailableActions(abstractState, counterStrategy.getSystemTransitionRelation());
			
//			long t_avl=UtilityMethods.timeStamp();
			
			int availableActions = counterStrategy.getAvailableActionsAtState(abstractState, Player.SYSTEM);
			
//			UtilityMethods.duration(t_avl, "computing available actions in cs in ", 500);
			
//			UtilityMethods.debugBDDMethods(bdd, "available actions are ", availableActions);
			//int unAvailableActions = bdd.ref(bdd.not(availableActions));
//			int[] actionsList = concreteGame.enumerateActions();
			int[] actionsList = concreteGame.sysActions;
			//alpha is the set of states where all available actions lead to a spoiling next state
			int alpha=bdd.ref(bdd.getOne());
			//beta is the set of states where all actions are contained in available actions
			int beta=bdd.ref(bdd.getOne());
			
			int cube2=concreteGame.getPrimeVariablesCube();
//			int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getSystemTransitionRelation());
			
//			int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, concreteGame.getT_sysList());
			
//			long t_epre = UtilityMethods.timeStamp();
			
			int pre = concreteGame.EpreImage(concreteChildrenStates, cube2, Player.SYSTEM);
			
//			UtilityMethods.debugBDDMethods(bdd, "Epre image for sys in focus", pre);
//			UtilityMethods.getUserInput();
			
//			UtilityMethods.duration(t_epre, "Epre image based on concrete game computed in ", 500);
			
			int actionCube = concreteGame.getActionsCube();
			
			bdd.deref(concreteChildrenStates);
			
//			UtilityMethods.debugBDDMethods(bdd, "concrete game system trans", concreteGame.getSystemTransitionRelation());
			
			ArrayList<Integer> alphaSets = statesWithExtraActionsFocusedSets.get(eqClassIndex);
			ArrayList<Integer> betaSets = statesWithoutUnsafeSuccessorsFocusedSets.get(eqClassIndex);
			
//			long t_actions = UtilityMethods.timeStamp();
			
			for(int i=0; i<actionsList.length;i++){
				int act = actionsList[i];
				int isActAvailable = bdd.ref(bdd.and(act, availableActions));
				//if the action is available
				if(isActAvailable != 0){
					

					
					//compute alpha = \cap_{l \in Avl(n) Epre(\cup_j r_(l,j),l)}
					int actionPreTmp = bdd.ref(bdd.and(pre, act));
					int actionPre = bdd.ref(bdd.exists(actionPreTmp, actionCube));
					//TODO: check next three lines of code for correctness
					alpha=bdd.andTo(alpha, actionPre);
					alphaSets.add(actionPre);
					//bdd.deref(actionPre);
					bdd.deref(actionPreTmp);
				}else{
					
					
					
//					UtilityMethods.debugBDDMethods(bdd, "action is not available ", act);
					
					//compute beta = \cap_{l \not \in Avl(n)} \not(Avl(l))
					//int statesWithActAvailable=concreteGame.actionAvailabilitySet(act, concreteGame.getSystemTransitionRelation());
					int statesWithActAvailable=concreteGame.sysAvlActions[i];
					
//					UtilityMethods.debugBDDMethods(bdd, "state with action available", statesWithActAvailable);
					
//					int statesWithActAvailable=bdd.ref(bdd.exists(statesWithActAvailableTmp, actionCube));
//					
//					UtilityMethods.debugBDDMethods(bdd, "state with action available", statesWithActAvailable);
					
					int unavailable = bdd.ref(bdd.not(statesWithActAvailable));
					
//					UtilityMethods.debugBDDMethods(bdd, "state with action unavailable", unavailable);
					
					betaSets.add(unavailable);
					beta=bdd.andTo(beta, unavailable);

//					bdd.deref(statesWithActAvailableTmp);
					
					
				}
				bdd.deref(isActAvailable);
			}
			

//			UtilityMethods.duration(t_actions, "states with available actions and states without unsafe successors computed in ", 500);
			
			//bdd.deref(cube2);
			bdd.deref(pre);
			//bdd.deref(actionCube);
			
//			UtilityMethods.debugBDDMethods(bdd, "alpha is ", alpha);
//			UtilityMethods.debugBDDMethods(bdd, "beta is ", beta);
//			UtilityMethods.debugBDDMethods(bdd, "r is ", r);
			
			//compute and return r \cap alpha \cap beta
			int alphaCapBeta = bdd.ref(bdd.and(alpha, beta));
			
			r=bdd.andTo(r, alphaCapBeta);
//			r=bdd.ref(bdd.and(r, alphaCapBeta));
			
			bdd.deref(alpha);
			bdd.deref(beta);
			bdd.deref(alphaCapBeta);
			
			sysStatesFocusedSets.set(eqClassIndex, r);

//			int notAlpha=bdd.ref(bdd.not(alpha));
//			alphaStatesFocusedSets.set(eqClassIndex, notAlpha);
//			int notBeta=bdd.ref(bdd.not(beta));
//			betaStatesFocusedSets.set(eqClassIndex, notBeta);
			
//			UtilityMethods.debugBDDMethods(bdd, "focusing ", r);
			
//			bdd.gc();
//			bdd.gc();
//			System.out.println("after s");
//			abstractGame.printEquivalenceClasses();
			
//			System.out.println("r computed ");
//			BDDWrapper.BDD_Usage(bdd);
			
			return r;
		}else{
			System.err.println("error: the player type can be either e or s");
		}
		return -1;
	}
	
	
	//TODO
	public static void main(String[] args) {		
		Tester.csGuidedControlTest2();
	}
	
	
	
//	
	
	public int[] getEquivalenceClassesFromPredicates(int[] predicates){
		return UtilityMethods.enumerate(bdd, predicates);
	}
	
//	/**
//	 * Transforms the objective for the concrete game to an abstract objective for the abstract game
//	 * @param concreteObjective
//	 * @param abstractGame
//	 * @return
//	 */
//	public int computeAbstractObjective(int concreteObjective, AbstractGame abstractGame){
//		
//		int abstractObjective = bdd.ref(bdd.getZero());
//		int[] eqClasses=abstractGame.getEquivalenceClasses();
//		for(int i=0;i<eqClasses.length;i++){
//			int intersection = BDDWrapper.intersect(bdd, concreteObjective, eqClasses[i]);
//			//if the equivalence class is a subset of states represented by the concrete objective
//			if(intersection != 0){
//				//add the abstract state corresponding to this equivalence class to the abstract objective
//				int abstractState = abstractGame.equivalenceClassToAbstractStateMap(i);
//				abstractObjective=bdd.orTo(abstractObjective, abstractState);
//				bdd.deref(abstractState);
//			}
//			bdd.deref(intersection);
//		}
//		return abstractObjective;
//	}
	
//	public ArrayList<Integer> shatterAndRefine(int focusedSetIndex, Player player){
//	int[] eqClasses=abstractGame.getEquivalenceClasses();
//	//result contains the new eq classes from the old eq class
//	ArrayList<Integer> result = new ArrayList<Integer>();
//	if(player == Player.ENVIRONMENT){
//		//r
//		result.add(envStatesFocusedSets.get(focusedSetIndex));
//		//q\r
//		int diff=BDDWrapper.diff(bdd, eqClasses[focusedSetIndex], envStatesFocusedSets.get(focusedSetIndex));
//		if(diff!=0) result.add(diff);
//		
//	}else{
//		//r
//		result.add(sysStatesFocusedSets.get(focusedSetIndex));
//		//q\r
//		int diff=BDDWrapper.diff(bdd, eqClasses[focusedSetIndex], sysStatesFocusedSets.get(focusedSetIndex));
//		//q\r \cap Avl(l) | l \not in C(n)
//		int statesWithExtraActions=BDDWrapper.intersect(bdd, diff, alphaStatesFocusedSets.get(focusedSetIndex));
//		//q\r \cap notBeta
//		int statesWithSafeSuccessors=BDDWrapper.intersect(bdd, diff, betaStatesFocusedSets.get(focusedSetIndex));
//		
////		if(statesWithExtraActions != 0 ) result.add(statesWithExtraActions);
////		if(statesWithSafeSuccessors != 0 ) result.add(statesWithSafeSuccessors);
//		
//		ArrayList<Integer> shatteredSets=new ArrayList<Integer>();
//		shatteredSets.add(statesWithExtraActions);
//		shatteredSets.add(statesWithSafeSuccessors);
//		
//		result.addAll(computeEquivalenceClasses(shatteredSets));
//	}
//	return result;
//}
	
//	public ArrayList<Integer> shatterAndRefineAbstractPredicates(){
//	int[] eqClasses=abstractGame.getEquivalenceClasses();
//	ArrayList<Integer> newAbstractPredicates=new ArrayList<Integer>();
//	for(int i=0;i<abstractGame.abstractPredicates.length;i++){
//		newAbstractPredicates.add(abstractGame.abstractPredicates[i]);
//	}
//	
//	for(int i=0; i<envStatesFocusedSets.size();i++){
//		int r  = envStatesFocusedSets.get(i);
//		if(r!=0){
//			if(!newAbstractPredicates.contains(r) && !newAbstractPredicates.contains(bdd.not(r))){
//				newAbstractPredicates.add(r);
//			}
//		}
//	}
//	
//	for(int i=0; i<sysStatesFocusedSets.size();i++){
//		int r=sysStatesFocusedSets.get(i);
//		if(r!=0){
//			if(!newAbstractPredicates.contains(r) && !newAbstractPredicates.contains(bdd.not(r))){
//				newAbstractPredicates.add(r);
//			}
//		}
//		
//		//q\r
//		int diff=BDDWrapper.diff(bdd, eqClasses[i], r);
//		//q\r \cap Avl(l) | l \not in C(n)
////		int statesWithExtraActions=BDDWrapper.intersect(bdd, diff, alphaStatesFocusedSets.get(i));			
//		
//		ArrayList<Integer> alphaSets = statesWithExtraActionsFocusedSets.get(i);
//		
//		for(int j=0;j<alphaSets.size();j++){
//			int statesWithExtraActions=BDDWrapper.intersect(bdd, diff, alphaSets.get(j));
//			if(statesWithExtraActions != 0){
//				if(!newAbstractPredicates.contains(statesWithExtraActions) && !newAbstractPredicates.contains(bdd.not(statesWithExtraActions))){
//					newAbstractPredicates.add(statesWithExtraActions);
//				}
//			}
//			bdd.deref(alphaSets.get(j));
//		}
//		
//		
//		//q\r \cap notBeta
//		
//		ArrayList<Integer> betaSets = statesWithoutUnsafeSuccessorsFocusedSets.get(i);
//		
////		int statesWithSafeSuccessors=BDDWrapper.intersect(bdd, diff, betaStatesFocusedSets.get(i));
//		
//		for(int j=0;j<betaSets.size();j++){
//			int statesWithSafeSuccessors=BDDWrapper.intersect(bdd, diff, betaSets.get(j));
//			if(statesWithSafeSuccessors != 0){
//				if(!newAbstractPredicates.contains(statesWithSafeSuccessors) && !newAbstractPredicates.contains(bdd.not(statesWithSafeSuccessors))){
//					newAbstractPredicates.add(statesWithSafeSuccessors);
//				}
//			}
//			bdd.deref(betaSets.get(j));
//		}
//		
////		if(statesWithExtraActions != 0){
////			if(!newAbstractPredicates.contains(statesWithExtraActions) && !newAbstractPredicates.contains(bdd.not(statesWithExtraActions))){
////				newAbstractPredicates.add(statesWithExtraActions);
////			}
////		}
//		
////		if(statesWithSafeSuccessors != 0){
////			if(!newAbstractPredicates.contains(statesWithSafeSuccessors) && !newAbstractPredicates.contains(bdd.not(statesWithSafeSuccessors))){
////				newAbstractPredicates.add(statesWithSafeSuccessors);
////			}
////		}
//	}
//	
//	return newAbstractPredicates;
//}
	
//	/**
//	 * given a set of shattered sets, computes the equivalence classes 
//	 * where a and b are in the same equivalence class exactly when for each set[i], a \in set[i] <-> b \in set[i]
//	 * @param sets
//	 * @return
//	 */
//	public ArrayList<Integer> computeEquivalenceClasses(ArrayList<Integer> sets){
//		
////		System.out.println("Shattered sets are ");
////		for(int i=0;i<sets.size();i++){
////			UtilityMethods.debugBDDMethods(bdd, "\nset "+i, sets.get(i));
////		}
//		
//		ArrayList<Integer> eqClasses=sets;
//		ArrayList<Integer> eqClassesPrime=new ArrayList<Integer>();
//		ArrayList<Integer> newShatteredSets=new ArrayList<Integer>();
//		while(!shatteredSetsFixedPoint(eqClasses, eqClassesPrime)){
//			eqClassesPrime = eqClasses;
//			newShatteredSets =  new ArrayList<Integer>();
//			for(int i=0;i<eqClasses.size();i++){
//				for(int j=i+1;j<eqClasses.size();j++){
//					
////					UtilityMethods.debugBDDMethods(bdd, "eq class "+i, eqClasses.get(i));
////					UtilityMethods.debugBDDMethods(bdd, "eq class "+j, eqClasses.get(j));
//					
//					int intersection = bdd.ref(bdd.and(eqClasses.get(i), eqClasses.get(j)));
//					
////					UtilityMethods.debugBDDMethods(bdd, "intersection", intersection);
//					
//					int diff1 = BDDWrapper.diff(bdd, eqClasses.get(i), eqClasses.get(j));
//					
////					UtilityMethods.debugBDDMethods(bdd, "diff 1", diff1);
//					
//					int diff2 = BDDWrapper.diff(bdd, eqClasses.get(j), eqClasses.get(i));
//					
////					UtilityMethods.debugBDDMethods(bdd, "diff 2", diff2);
//					
//					if(intersection != 0 && intersection !=1){
//						if(!newShatteredSets.contains(intersection)){
//							newShatteredSets.add(intersection);
//							
////							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", intersection);
//						}
//					}
//					
//					if(diff1 != 0 && diff1 != 1){
//						if(!newShatteredSets.contains(diff1)){
//							newShatteredSets.add(diff1);
//							
////							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", diff1);
//						}
//					}
//					
//					if(diff2 != 0 && diff2 != 1){
//						if(!newShatteredSets.contains(diff2)){
//							newShatteredSets.add(diff2);
//							
////							UtilityMethods.debugBDDMethods(bdd, "added to shattered sets", diff2);
//						}
//					}
//				}
//			}
//			//eqClasses=newShatteredSets;
//			
//			eqClasses=new ArrayList<Integer>();
//			for(int i=0; i<newShatteredSets.size();i++){
//				int subset=bdd.ref(bdd.getZero());
//				//FALSE: if there is a set which is subset of this shattered set, remove it from the next iteration of equivalence classes
//				for(int j=0;j<newShatteredSets.size();j++){
//					if(j==i) continue;
//					int diff  = BDDWrapper.diff(bdd, newShatteredSets.get(j), newShatteredSets.get(i));
//					if(diff == 0){
//						subset = bdd.orTo(subset, newShatteredSets.get(j));
//						bdd.deref(diff);
//					}
//				}
//				int currentShatteredSet = BDDWrapper.diff(bdd, newShatteredSets.get(i), subset);
//				bdd.deref(subset);
//				if(currentShatteredSet !=0 && !eqClasses.contains(currentShatteredSet)){
//					eqClasses.add(currentShatteredSet);
//				}
//			}
//			
////			System.out.println("New shattered sets are ");
////			for(int i=0;i<newShatteredSets.size();i++){
////				UtilityMethods.debugBDDMethods(bdd, "\nset "+i, newShatteredSets.get(i));
////			}
////			UtilityMethods.getUserInput();
////			
////			System.out.println("New eq classes are ");
////			for(int i=0;i<eqClasses.size();i++){
////				UtilityMethods.debugBDDMethods(bdd, "\nset "+i, eqClasses.get(i));
////			}
////			UtilityMethods.getUserInput();
//		}
//		
////		int[] result=UtilityMethods.IntegerArrayListTointArray(newShatteredSets);
//		
////		System.out.println("\nnew equivalence classes were computed ");
////		for(int i=0;i<result.length;i++){
////			UtilityMethods.debugBDDMethods(bdd, "new eq class "+i, result[i]);
////		}
//		
//		return newShatteredSets;
//	}
	
//	//TODO: can be optimized
//	private boolean shatteredSetsFixedPoint(ArrayList<Integer> set1, ArrayList<Integer> set2){
//		if(set1.size()==set2.size()){
//			for(int i=0; i<set1.size();i++){
//				if(!set2.contains(set1.get(i))){
//					return false;
//				}
//				if(!set1.contains(set2.get(i))){
//					return false;
//				}
//			}
//			return true;
//		}
//		return false;
//	}
	

	
	/**
	 * Computes the abstract game for the concrete game given the set of equivalence classes
	 * @param equivalenceClasses
	 */
	//TODO: copy and paste from initial abstraction, revise both methods

}
