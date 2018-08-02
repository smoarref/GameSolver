package swarm;

import game.BDDWrapper;

import java.util.ArrayList;

import automaton.Node;
import specification.GridCell2D;
import utils.UtilityMethods;

public class SwarmSimulator2D {
	
	Node[][] regions;
	ArrayList<ArrayList<SynchedNode>> localStrategies;
	
	int[] currentState;
	boolean syncIssued = false;
	
	ArrayList<GridCell2D> targetRegions = null; 
	ArrayList<GridCell2D> avoidRegions = null;
	
	public SwarmSimulator2D(Node[][] argRegions, ArrayList<ArrayList<SynchedNode>> argLocalStrategies){
		regions = argRegions;
		localStrategies = argLocalStrategies;
		currentState = new int[localStrategies.size()];
		//initially all local strategies at their initial state
		for(int i=0; i<localStrategies.size();i++){
			currentState[i] = 0;
		}
	}
	
	public SwarmSimulator2D(Node[][] argRegions, ArrayList<ArrayList<SynchedNode>> argLocalStrategies, ArrayList<GridCell2D> argTargetRegions, ArrayList<GridCell2D> argAvoidRegions){
		regions = argRegions;
		localStrategies = argLocalStrategies;
		currentState = new int[localStrategies.size()];
		//initially all local strategies at their initial state
		for(int i=0; i<localStrategies.size();i++){
			currentState[i] = 0;
		}
		targetRegions = argTargetRegions;
		avoidRegions = argAvoidRegions;
	}
	
	
	public ArrayList<GridCell2D> getInitialPositions(){
		ArrayList<GridCell2D> result = new ArrayList<GridCell2D>();
		for(int i=0; i<localStrategies.size();i++){
			ArrayList<SynchedNode> localStrat =  localStrategies.get(i);
			SynchedNode init = localStrat.get(0);
			GridCell2D initGridCell = getGridCell(init);
			result.add(initGridCell);
		}
		return result;
	}
	
	public ArrayList<GridCell2D> getCurrentPositions(){
		ArrayList<GridCell2D> result = new ArrayList<GridCell2D>();
		for(int i=0; i<localStrategies.size();i++){
			ArrayList<SynchedNode> localStrat =  localStrategies.get(i);
			if(currentState[i] >= localStrat.size()){
				return null;
			}
			SynchedNode currentNode = localStrat.get(currentState[i]);
			GridCell2D currentGridCell = getGridCell(currentNode);
			result.add(currentGridCell);
		}
		return result;
	}
	
	private GridCell2D getGridCell(SynchedNode node){
		for(int i=0; i<regions.length; i++){
			for(int j=0; j<regions[i].length; j++){
				if(regions[i][j] == node.getNode()){
					return new GridCell2D(i, j);
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return the next position, or null if there is no such step
	 */
	public ArrayList<GridCell2D> simulateOneStep(){
		//check if all local strategies are in sync mode, then issue the sync signal
		
		syncIssued = true;
		for(int i=0; i<localStrategies.size(); i++){
			ArrayList<SynchedNode> localStrat = localStrategies.get(i);
			SynchedNode currentStateForLocalStrat = localStrat.get(currentState[i]);
			if(!currentStateForLocalStrat.isSynched()){
				syncIssued = false;
				break;
			}
		}
		
		//everybody is in a sync mode, synchronization signal is issued and processes may continue.
		//the sync signal in local strategies is turned off
		if(syncIssued){
			for(int i=0; i<localStrategies.size(); i++){
				ArrayList<SynchedNode> localStrat = localStrategies.get(i);
				SynchedNode currentStateForLocalStrat = localStrat.get(currentState[i]);
				currentStateForLocalStrat.sync = false;
			}
		}
		
		//check if all strategies has reached their end
		boolean allEnded = true;
		for(int i=0; i<localStrategies.size();i++){
			ArrayList<SynchedNode> localStrat = localStrategies.get(i);
			if(currentState[i] != localStrat.size()-1){
				allEnded = false;
				break;
			}
		}
		
		if(allEnded){
			return getCurrentPositions();
		}
		
		//choose a random local strategy to execute that does not need to synchronize
		do{
			int randomIndex = UtilityMethods.randomIndex(localStrategies.size());
			ArrayList<SynchedNode> localStrat = localStrategies.get(randomIndex);
			SynchedNode  currentStateForLocalStrat = localStrat.get(currentState[randomIndex]);
			if(!currentStateForLocalStrat.sync && currentState[randomIndex] < localStrat.size()-1){
				currentState[randomIndex]++;
				break;
			}
		}while(true);
		
		return getCurrentPositions();
	}
	
	public ArrayList<GridCell2D> getTargetRegions(){
		return targetRegions;
	}
	
	public ArrayList<GridCell2D> getAvoidRegions(){
		return avoidRegions;
	}

}
