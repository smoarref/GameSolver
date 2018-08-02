package swarm;

import automaton.Node;

public class SynchedNode{
	Node node; 
	boolean sync; 
	
	SynchedNode(Node argNode, boolean isSynched){
		node = argNode;
		sync = isSynched;
	}
	
	Node getNode(){
		return node;
	}
	
	boolean isSynched(){
		return sync;
	}
	
	boolean equal(SynchedNode otherNode){
		if(node == otherNode.getNode() && sync == otherNode.isSynched()){
			return true;
		}
		return false;
	}
	
	public void print(){
		System.out.println(node.getName()+", sync = "+sync);
	}
}
