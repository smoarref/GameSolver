package swarm;

import java.util.ArrayList;

import specification.GridCell2D;
import utils.UtilityMethods;
import automaton.Node;

public class DecentralizedSwarmSimulator2D {

	int numOfProcesses;
	Node[][] regions;
	ArrayList<ArrayList<SynchedNode>> localStrategies;
	boolean[][][] synchronizationSkeleton;
	ArrayList<boolean[]> synchHappened;
	int loopingIndex;
	
	int[] currentState;
	boolean syncIssued = false;
	
	ArrayList<GridCell2D> targetRegions = null; 
	ArrayList<GridCell2D> avoidRegions = null;
	
	ArrayList<Integer> waitingProcesses = new ArrayList<Integer>(); 
	
	public DecentralizedSwarmSimulator2D(Node[][] argRegions, ArrayList<ArrayList<SynchedNode>> argLocalStrategies, 
			boolean[][][] argSynchronizationSkeleton, int argLoopingIndex){
		
		regions = argRegions;
		localStrategies = argLocalStrategies;
		numOfProcesses = localStrategies.size();
		currentState = new int[localStrategies.size()];
		//initially all local strategies at their initial state
		for(int i=0; i<localStrategies.size();i++){
			currentState[i] = 0;
		}
		synchronizationSkeleton = argSynchronizationSkeleton;
		initializeSynchHappened();
		loopingIndex = argLoopingIndex;
	}
	
	public DecentralizedSwarmSimulator2D(Node[][] argRegions, ArrayList<ArrayList<SynchedNode>> argLocalStrategies, 
			boolean[][][] argSynchronizationSkeleton, 
			ArrayList<GridCell2D> argTargetRegions, ArrayList<GridCell2D> argAvoidRegions, 
			int argLoopingIndex){
		regions = argRegions;
		localStrategies = argLocalStrategies;
		numOfProcesses = localStrategies.size();
		currentState = new int[localStrategies.size()];
		//initially all local strategies at their initial state
		for(int i=0; i<localStrategies.size();i++){
			currentState[i] = 0;
		}
		targetRegions = argTargetRegions;
		avoidRegions = argAvoidRegions;
		synchronizationSkeleton = argSynchronizationSkeleton;
		initializeSynchHappened();
		loopingIndex = argLoopingIndex;
	}
	
	public int getDimension(){
		return regions.length;
	}
	
	private void initializeSynchHappened(){
		synchHappened=new ArrayList<boolean[]>();
		for(int i=0; i<localStrategies.size(); i++){
			boolean[] synchHappened_i = UtilityMethods.allFalseArray(localStrategies.size());
			synchHappened.add(synchHappened_i);
		}
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
	//TODO: extend such that extra stuttering states are remove during simulation
	public ArrayList<GridCell2D> simulateOneStep(){
		//compute the processes that can make progress
		ArrayList<Integer> progressive = new ArrayList<Integer>();
		for(int i=0; i<numOfProcesses; i++){
			boolean canProgress = true;
			//if process i satisfies following conditions, add it to the set of processes that can make progress
			//if for all process j, either process i does not need to synchronize with process j, 
			//or it does and process j is ready to synchronize
			boolean[] synchHappened_i = synchHappened.get(i);
			for(int j=i+1; j<numOfProcesses; j++){
				//does it need to synchronize with j?
				if(synchronizationSkeleton[i][currentState[i]][j] && !synchHappened_i[j]){
					//has synchronization already occurred?
					
//					System.out.println("process "+i+" needs to synchronize with process "+j);
//					String s = synchHappened_i[j]?"NOT":"";
//					System.out.println("it has "+s+" synchronized with it yet");
					
					boolean[] synchHappened_j = synchHappened.get(j);
					if(currentState[i] == currentState[j] && !synchHappened_j[i]){
						
//						System.out.println("processes are at the same state");
//						String s2 = synchHappened_j[i]?" NOT ":"";
//						System.out.println("Process "+j+" is "+s2+" ready to synchronize");
						
						
						synchHappened_i[j] = true;
						synchHappened_j[i] = true;
					}
				}
			}
			for(int j=0;j<numOfProcesses;j++){
				if(i==j) continue;
				if(synchronizationSkeleton[i][currentState[i]][j] && !synchHappened_i[j]){
					canProgress = false;
					break;
				}
			}
			if(canProgress){
				progressive.add(i);
			}
		}
		
		
//		System.out.println("processes that can make progress");
//		for(int i=0; i<progressive.size(); i++){
//			System.out.println(progressive.get(i));
//		}
//		System.out.println();
		
		waitingProcesses = new ArrayList<Integer>();
		for(int i=0; i<numOfProcesses; i++){
			if(!progressive.contains(i)) waitingProcesses.add(i);
//			System.out.println("process "+i+" waiting");
		}
		
		
		//randomly choose a process that can make progress and execute
		int randomIndex = UtilityMethods.randomIndex(progressive.size());
		
//		System.out.println("random index "+randomIndex);
		
		int selectedProcess = progressive.get(randomIndex);
		
//		System.out.println("selected process is "+selectedProcess);
		
		ArrayList<SynchedNode> localStrat = localStrategies.get(selectedProcess);
//		SynchedNode  currentStateForLocalStrat = localStrat.get(currentState[selectedProcess]);
		if(currentState[selectedProcess] < localStrat.size()-1){
			currentState[selectedProcess]++;
		}else{
			currentState[selectedProcess]=loopingIndex;
		}
		//initialize the syncHappened for the next step
		synchHappened.set(selectedProcess, UtilityMethods.allFalseArray(numOfProcesses));
		
		return getCurrentPositions();
	}
	
	public ArrayList<GridCell2D> getTargetRegions(){
		return targetRegions;
	}
	
	public ArrayList<GridCell2D> getAvoidRegions(){
		return avoidRegions;
	}
	
	public ArrayList<Integer> getWaitingProcesses(){
		return waitingProcesses;
	}

}
