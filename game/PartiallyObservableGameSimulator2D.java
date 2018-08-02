package game;

import java.util.ArrayList;
import java.util.HashMap;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.GridCell2D;
import utils.UtilityMethods;

public class PartiallyObservableGameSimulator2D extends Simulator2D{
	
	protected TurnBasedPartiallyObservableGameStructure partiallyObservableGameStructure;
	
	protected int initialObservation;
	protected int currentObservation;

	public PartiallyObservableGameSimulator2D(BDD argBDD,
			ArrayList<Agent2D> argAgents, GameStructure argGameStructure,
			int argInit) {
		super(argBDD, argAgents, argGameStructure, argInit);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * returns the observable corresponding to the initial state
	 * @return
	 */
	public int getInitialObservation(){
		return initialObservation;
	}
	
	//TODO: argInit is the concrete game's init. Check that it is clear. Over all, separate the init from GameStructure and make it explicit in the solver and 
	//simulator
	public PartiallyObservableGameSimulator2D(BDD argBDD,
			ArrayList<Agent2D> argAgents, 
			TurnBasedPartiallyObservableGameStructure argPartiallyObservableGameStructure, 
			int argInit) {
		super(argBDD, argAgents, argPartiallyObservableGameStructure.getUnderlyingGameStructure(), argInit);
		partiallyObservableGameStructure = argPartiallyObservableGameStructure;
	}
	
	public ArrayList<GridCell2D> getInitialPositions(){
		ArrayList<GridCell2D> result = chooseState(initState); 
		initialObservation = bdd.ref(partiallyObservableGameStructure.getCorrespondingKnowledgeGameState(initState));
		currentObservation = bdd.ref(initialObservation);
		
		System.out.println("initially");
		printCurrentStateAndObseravtions(result);
		
		return result;
	}
	
	public void printCurrentStateAndObseravtions(ArrayList<GridCell2D> grids){
		UtilityMethods.debugBDDMethods(bdd, "current concrete state is ", getCurrentSimulationState());
		if(grids!=null){
			for(int i =0; i<agents.size(); i++){
				Agent2D a = agents.get(i);
				System.out.println("current gridcell for "+a.getName());
				grids.get(i).print();
			}
		}
		UtilityMethods.debugBDDMethods(bdd, "current observation is", currentObservation);
//		System.out.println("current observation coresponds to states");
		int set = partiallyObservableGameStructure.getCorrespondingGameState(currentObservation);
		UtilityMethods.debugBDDMethods(bdd, "current observation coresponds to states", set);
		UtilityMethods.getUserInput();
	}
	
	
	public ArrayList<GridCell2D> simulateOneStep(){
		System.out.println("simulating, current state is");
//		printCurrentStateAndObseravtions(null);
		
		GameStructure knowledgeGameStructure = partiallyObservableGameStructure.getKnowledgeGameStructure();
		if(knowledgeGameStructure == null){
			System.out.println("Knowledge game structure for this game structure is not computed yet!! ");
			return null;
		}
		
		//what are the possible next observations?
		int nextObservableStates = knowledgeGameStructure.symbolicGameOneStepExecution(currentObservation); 
		
		UtilityMethods.debugBDDMethods(bdd, "next observables ", nextObservableStates);
		
		//enumerate the observables
		ArrayList<String> minterms = BDDWrapper.minterms(bdd , nextObservableStates, knowledgeGameStructure.variables);
    	if(minterms == null ){
    		System.err.println("the set of next observations corresponding to the states is empty");
    		UtilityMethods.getUserInput();
    		return null;
    	}
    	
    	//compute the next observations that are consistent with possible next states
    	int nextCells = gameStructure.symbolicGameOneStepExecution(getCurrentSimulationState());
    	
    	UtilityMethods.debugBDDMethods(bdd, "concrete reachable states from current concrete state", nextCells);
    	
    	ArrayList<String> nextObservationsConsistentWithConcreteReachableStates =  new ArrayList<String>();
    	for(String m : minterms){
    		int nextObservation = BDDWrapper.mintermToBDDFormula(bdd, m, knowledgeGameStructure.variables);
    		int gameStatesCorrespondingToNextObservation = partiallyObservableGameStructure.getCorrespondingGameState(nextObservation);
    		int possibleNextCells = BDDWrapper.and(bdd, nextCells, gameStatesCorrespondingToNextObservation);
    		if(possibleNextCells != 0){
    			nextObservationsConsistentWithConcreteReachableStates.add(m);
    		}
    		BDDWrapper.free(bdd, nextObservation);
    		BDDWrapper.free(bdd, possibleNextCells);
    	}
    	
    	
//    	int randomIndex = UtilityMethods.randomIndex(minterms.size());
//    	int nextObservation = BDDWrapper.mintermToBDDFormula(bdd, minterms.get(randomIndex), knowledgeGameStructure.variables);
    	
    	if(nextObservationsConsistentWithConcreteReachableStates.size()==0){
    		System.err.println("there is no observation consistent with possible next states from current state! Something must be wrong!!");
    	}
    	int randomIndex = UtilityMethods.randomIndex(nextObservationsConsistentWithConcreteReachableStates.size());
    	int nextObservation = BDDWrapper.mintermToBDDFormula(bdd, nextObservationsConsistentWithConcreteReachableStates.get(randomIndex), knowledgeGameStructure.variables);
    	
    	UtilityMethods.debugBDDMethods(bdd, "next chosen observation", nextObservation);
    	
		BDDWrapper.free(bdd, currentObservation);
		currentObservation = nextObservation;
		BDDWrapper.free(bdd, nextObservableStates);
		
		//compute the next concrete state
    	int gameStatesCorrespondingToNextObservation = partiallyObservableGameStructure.getCorrespondingGameState(nextObservation); 	
    	
    	UtilityMethods.debugBDDMethods(bdd, "concrete states corresponding to chosen observation", gameStatesCorrespondingToNextObservation);
//    	UtilityMethods.getUserInput();
    	
		
		int possibleNextCells = BDDWrapper.and(bdd, nextCells, gameStatesCorrespondingToNextObservation);
		
		UtilityMethods.debugBDDMethods(bdd, "possible next cells are", possibleNextCells);
		
		if(possibleNextCells == 0){
			System.err.println("possible next cells are empty!! something must be wrong!!");
		}
		freeCurrentSimulationState();
		BDDWrapper.free(bdd, gameStatesCorrespondingToNextObservation);
		
		ArrayList<GridCell2D> result = chooseState(possibleNextCells);
    	BDDWrapper.free(bdd, nextCells);

    	System.out.println("in the end");
//    	printCurrentStateAndObseravtions(result);
    	
		return result;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
