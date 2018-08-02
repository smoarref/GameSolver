package swarm;

import game.Variable;

import java.util.ArrayList;

import automaton.Node;

public class SwarmTraceNode extends Node{
	
	//the set of regions corresponding to this traceNode
	ArrayList<Variable> regions;
	
	//the set of processes synchronizing with the process containing this node
	ArrayList<Integer> synchronizingWith = new ArrayList<Integer>();

	public SwarmTraceNode(String argId, ArrayList<Variable> argRegions) {
		super(argId);
		regions = argRegions;
	}
	
	public SwarmTraceNode(String argId, Variable region){
		super(argId);
		regions = new ArrayList<Variable>();
		regions.add(region);
	}
	
	public void setSynchronizationProcesses(ArrayList<Integer> synchronizationProcessIndices){
		synchronizingWith = synchronizationProcessIndices;
	}
	
	public ArrayList<Integer> getSynchronizationIndices(){
		return synchronizingWith;
	}
	
	public ArrayList<Variable> getRegions(){
		return regions;
	}

}
