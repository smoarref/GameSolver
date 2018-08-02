package automaton;

public class Node implements Comparable<Node> {
	  
	private final String name;
	   
	public Node(final String argName) {
		name = argName;
	}
	   
	public int compareTo(final Node argNode) {
		return argNode == this ? 0 : -1;
	}
	
	public String getName(){
		return name;
	}
	
	public void printNode(){
		System.out.println("Node "+getName());
		
	}
	
	public String toString(){
		return "Node "+getName();
	}
}