package swarm;


import java.util.ArrayList;

import automaton.Edge;
import automaton.Node;

public class PartitionedCentralLTS {
	
	ArrayList<ArrayList<Node>> partitions = new ArrayList<ArrayList<Node>>();
	ArrayList<ArrayList<Edge<Node>>> matching = new ArrayList<ArrayList<Edge<Node>>>();
	ArrayList<Boolean> sync = new ArrayList<Boolean>();
	int loopingIndex;

	public PartitionedCentralLTS(ArrayList<ArrayList<Node>> argPartitions, ArrayList<ArrayList<Edge<Node>>> argMatching, 
			ArrayList<Boolean> argSynch, int argLoopingIndex) {
		partitions = argPartitions;
		matching = argMatching;
		sync = argSynch;
		loopingIndex = argLoopingIndex;
	}
	
	public ArrayList<ArrayList<Node>> getPartitions(){
		return partitions;
	}
	
	public ArrayList<ArrayList<Edge<Node>>> getMatching(){
		return matching;
	}
	
	public ArrayList<Boolean> getSynchronization(){
		return sync;
	}
	
	public int getLoopingIndex(){
		return loopingIndex;
	}

}
