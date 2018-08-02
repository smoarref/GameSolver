package automaton;

public class Edge<N>{
	   
	private final N from, to; 
	
	public N getFrom(){
		return from;
	}
	
	public N getTarget(){
		return to;
	}
	
	
	public Edge(final N argFrom, final N argTo){     
		from = argFrom;   
		to = argTo;
	}
	
	public void printEdge(){
		System.out.println("Edge "+from+ " --> "+ to);
	}
	
	public String toString(){
		return "Edge "+from+ " --> "+ to;
	}
	
	public boolean compareTo(Edge<N> e){
		if(e.getFrom()==from && e.getTarget()==to){
			return true;
		}
		return false;
	}
	
	public Edge<N> reversedEdge(){
		return new Edge<N>(to, from);
	}
	
	
	public static void main(String[] args){
		Edge<Node> e=new Edge<Node>(new Node("1"), new Node("2"));
	}
}
