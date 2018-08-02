package game;

import java.util.ArrayList;

import specification.Agent2D;
import specification.GridCell2D;
import utils.UtilityMethods;
import jdd.bdd.BDD;

/**
 * Simulates a game structure step by step
 * @author moarref
 *
 */
public class Simulator2D {
	protected BDD bdd;
	protected ArrayList<Agent2D> agents;
	protected GameStructure gameStructure; 
//	protected ArrayList<String> minterms;

	protected int initState;
	protected int currentState;
	
	public Simulator2D(BDD argBDD, ArrayList<Agent2D> argAgents, GameStructure argGameStructure, int argInit){
		bdd = argBDD;
		agents = argAgents;
		gameStructure = argGameStructure;
		initState = argInit;
//		currentState = initState;
	}
	
	public ArrayList<Agent2D> getAgents(){
		return agents;
	}
	
	public int getCurrentState(){
		return currentState;
	}
	
	public GameStructure getGameStructure(){
		return gameStructure;
	}
	
	public ArrayList<GridCell2D> getInitialPositions(){
		
//    	minterms = BDDWrapper.minterms(bdd , initState, gameStructure.variables);
//    	
//    	if(minterms == null ){
//    		System.err.println("the set of gridcells corresponding to the states is empty");
//    		UtilityMethods.getUserInput();
//    		return null;
//    	}
//		
//		GridCell2D[][] possibleInitialPositions = UtilityMethods.getGridCellsFromStates(bdd, minterms, gameStructure, agents);
//		return chooseCellsRandomly(possibleInitialPositions);
		
		return chooseState(initState);
	}
	
	public int getCurrentSimulationState(){
		return currentState;
	}
	
	protected void freeCurrentSimulationState(){
		BDDWrapper.free(bdd, currentState);
	}
	
	public ArrayList<GridCell2D> simulateOneStep(){
		int nextCells = gameStructure.symbolicGameOneStepExecution(currentState);
		freeCurrentSimulationState();
		
		ArrayList<GridCell2D> result = chooseState(nextCells);
    	BDDWrapper.free(bdd, nextCells);

    	
		return result;
	}
	
	/**
	 * computes and chooses one potential next gridcell from the given set of states 
	 * @param states
	 * @return
	 */
	public ArrayList<GridCell2D> chooseState(int states, Variable[] vars){
		ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, vars);
    	
    	if(minterms == null ){
    		System.err.println("the set of gridcells corresponding to the states is empty");
    		UtilityMethods.getUserInput();
    		return null;
    	}
    	
    	GridCell2D[][] possibleInitialPositions = UtilityMethods.getGridCellsFromStates(bdd, minterms, vars, agents);
		return chooseCellsRandomly(possibleInitialPositions, minterms, vars);
	}
	
	/**
	 * computes and chooses one potential next gridcell from the given set of states 
	 * @param states
	 * @return
	 */
	public ArrayList<GridCell2D> chooseState(int states){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, gameStructure.variables);
//    	
//    	if(minterms == null ){
//    		System.err.println("the set of gridcells corresponding to the states is empty");
//    		UtilityMethods.getUserInput();
//    		return null;
//    	}
//    	
//    	GridCell2D[][] possibleInitialPositions = UtilityMethods.getGridCellsFromStates(bdd, minterms, gameStructure, agents);
//		return chooseCellsRandomly(possibleInitialPositions, minterms);
		
		return chooseState(states, gameStructure.variables);
	}
	/**
	 * Randomly chooses cells for the agents from the given set of grid cells
	 * @param gridCells
	 * @return
	 */
	public ArrayList<GridCell2D> chooseCellsRandomly(GridCell2D[][] gridCells, ArrayList<String> minterms, Variable[] vars){
		ArrayList<GridCell2D> result = new ArrayList<GridCell2D>();
		int randomIndex = UtilityMethods.randomIndex(minterms.size());
		
		for(int i=0;i<agents.size();i++){
			result.add(gridCells[i][randomIndex]);
		}
		
		currentState = BDDWrapper.mintermToBDDFormula(bdd, minterms.get(randomIndex), vars);
		
		return result;
		
	}
	
	public BDD getBDD(){
		return bdd;
	}
	
	public int getInitStates(){
		return initState;
	}
	
	
}
