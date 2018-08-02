package automaton;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DirectedGraph <S, N extends Node, E extends Edge<N>> {
	
	private S id;
	private ArrayList<N> nodes=new ArrayList<N>();
	private Map<N, List<E>> adjacencies = new HashMap<N, List<E>>();
	   

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String getId(){
		if(id == null) return "";
	   return id.toString();
	}
	
	public void setId(S argId){
		id = argId;
	}
	 
	/**
	 * if automaton has changed since last time it is set
	 */
	private boolean modified=false;
	   
	public boolean isModified(){
		return modified;
	}
	   
	public void setModified(boolean value){
		modified = value;
	}

	public void addNode(N argNode){
		nodes.add(argNode);
		modified=true;
	} 

	/**
	 * 
	 * @return the number of states in the current automaton
	 */
	public int numOfStates(){
		return nodes.size();
	}
	   
	   
	/**
	 * 
	 * @return the number of transitions in the current automaton
	 */
	public int numOfTransitions(){
		int num=0;
		for(List<E> edges : adjacencies.values()){
			num+=edges.size();
		}
		return num;
	}
	   
	//TODO
	public void deleteNode(Node node){
		//delete incoming edges
		modified=true;
	}
	   
	public List<N> getNodes(){
		return nodes;
	}
	   
	public void addNodes(List<N> argNodes){
		nodes.addAll(argNodes);
		modified=true;
	}
	
	public boolean containsNode(N node){
		return nodes.contains(node);
	}
	
	public boolean containsEdge(E edge){
		List<E> edges=getAdjacent(edge.getFrom());
		   if(edges==null || edges.isEmpty()){
			   return false;
		   }
		   for(E e : edges){
			   if(e.compareTo(edge)){
				   return true;
			   }
		   }
		   return false;
	}
	   
	public void addEdge(E e){
		modified=true;
		N source=e.getFrom();
		N target=e.getTarget();
		if(!nodes.contains(source)){
			nodes.add(source);
		}
		   
		if(!nodes.contains(target)){
			nodes.add(target);
		}
		   
		List<E> list;
	    if(!adjacencies.containsKey(source)) {
	    	list = new ArrayList<E>();
	        adjacencies.put(source, list);
	    } else {
	    	list = adjacencies.get(source);
	    }
	    if(!containsEdge(e))	list.add(e);
	}
	
	public void removeEdge(E edge){
		List<E> list=adjacencies.get(edge.getFrom());
		if(list!=null && !list.isEmpty()){
			for(E e : list){
				if(e.compareTo(edge)){
					list.remove(e);
					modified=true;
				}
			}
		}
	}
	
	public List<E> getAdjacent(N source) {
		return adjacencies.get(source);
	}

	@SuppressWarnings("unchecked")
	public void reverseEdge(E e){
		adjacencies.get(e.getFrom()).remove(e);
		try{
			addEdge((E) e.reversedEdge());
		}catch(Exception ex){
			System.err.println("edge type mismatch exception!");
			ex.printStackTrace();
		}
	}

	public void reverse(){
		adjacencies = getReversed().adjacencies;
	    modified=true;
	}
	
	@SuppressWarnings("unchecked")
	public DirectedGraph<S,N,E> getReversed(){
		DirectedGraph<S,N,E> newlist = new DirectedGraph<S,N,E>();
	    newlist.nodes=nodes;
	    for(List<E> edges : adjacencies.values()) {
	    	for(E e : edges) {
	           newlist.addEdge((E) e.reversedEdge());
	        }
	    }
	    return newlist;
	}
	
	public Set<N> getSourceNodeSet() {
	       return adjacencies.keySet();
	}

	public Collection<E> getAllEdges() {
		List<E> edges = new ArrayList<E>();
	    for(List<E> e : adjacencies.values()) {
	    	edges.addAll(e);
	    }
	    return edges;
	}
	
	public void print(){
		System.out.println();
		System.out.println("printing the graph "+getId()+" information");
		System.out.println("Graph has "+numOfStates()+" nodes");
		for(Node n : getNodes()){
			n.printNode();
			List<E> edges = adjacencies.get(n);
			if(edges != null && !edges.isEmpty()){
				for(E e : adjacencies.get(n)){
					System.out.print("		");
					e.printEdge();
				}
			}
		}
		System.out.println();
	}
	
	public String toString(){
		String result="";
		result+="\nprinting the graph "+getId()+" information\n";
		result+="Graph has "+numOfStates()+" nodes\n";
		for(Node n : getNodes()){
			result+=n.toString();
			List<E> edges = adjacencies.get(n);
			if(edges != null && !edges.isEmpty()){
				for(E e : adjacencies.get(n)){
					result+="		";
					result+=e.toString();
				}
			}
		}
		return result;
	}
	
	/**
	 * Generates a dot file for the given automaton
	 * @param aut: the input automaton
	 * @param file: the output file
	 * @param verbosity: the amount of information to be shown in output graph - 0: just the graph, 1: also show labels
	 */
	public void draw(String file){
		String result="";
		String space="    ";
		result+="digraph G{\n";
		
		for(N n : getNodes()){
			String currentNode="q"+n.getName();

			
			List<E> edges = getAdjacent(n);
			if(edges!=null && !edges.isEmpty()){
				for(E e : edges){
					result+=space+currentNode+" -> q"+e.getTarget().getName();
					result+=";\n";
				}
			}
		}
		
		result+="}\n";
		//write the result in a .dot file
		try{
			PrintWriter pw = new PrintWriter(file);
			pw.println(result);
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//run the dot program to produce the graph as a .ps file
		try{
			Runtime.getRuntime().exec("dot -Tps "+file+" -o "+file.substring(0, file.indexOf("."))+".ps");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a dot file for the given automaton
	 * @param aut: the input automaton
	 * @param file: the output file
	 * @param verbosity: the amount of information to be shown in output graph - 0: just the graph, 1: also show labels
	 */
	public void printDOT(String file){
		String result="";
		String space="    ";
		result+="digraph G{\n";
		
		for(N n : getNodes()){
			String currentNode="q"+n.getName();

			
			List<E> edges = getAdjacent(n);
			if(edges!=null && !edges.isEmpty()){
				for(E e : edges){
					result+=space+currentNode+" -> q"+e.getTarget().getName();
					result+=";\n";
				}
			}
		}
		
		result+="}\n";
		//write the result in a .dot file
		try{
			PrintWriter pw = new PrintWriter(file);
			pw.println(result);
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}   

}
