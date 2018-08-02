package automaton;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Automaton extends DirectedGraph<String, AutomatonNode, LabeledEdge<AutomatonNode, Label>> {

	public Automaton(String argId){
		this.setId(argId);
	}
	
	
	
	public static void main(String[] args) {
		Automaton g=new Automaton("automaton Example");
		AutomatonNode[] nodes = new AutomatonNode[5];
		nodes[0]= new AutomatonNode("q_1",  new StringLabel("a & b"), true, false);
		nodes[1]= new AutomatonNode("q_2", new StringLabel("a & b"));
		nodes[2] = new AutomatonNode("q_3", new StringLabel("!a & b"));
		nodes[3] = new AutomatonNode("q_4", new StringLabel("a & !b"));
		nodes[4] = new AutomatonNode("q_5", new StringLabel("!a & !b"), false, true);
		
		for(int i=0;i<nodes.length;i++){
			g.addNode(nodes[i]);
		}
		
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[0], nodes[1], new StringLabel("l")));
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[0], nodes[2], new StringLabel("!l")));
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[1], nodes[3], new StringLabel("l")));
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[3], nodes[1], new StringLabel("!l")));
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[2], nodes[4], new StringLabel("l")));
		g.addEdge(new LabeledEdge<AutomatonNode, Label>(nodes[4], nodes[2], new StringLabel("!l")));
		
		g.print();
		g.draw("automaton.dot", 1);

	}
	
	/**
	 * Group the edges with the same source and destination
	 * Assumes that all edges have the same source
	 * Assumes that there is only one single edge for any action
	 * @param edges
	 * @return
	 */
	private List<LabeledEdge<AutomatonNode, Label>> groupEdges(List<LabeledEdge<AutomatonNode, Label>> edges){
		ArrayList<LabeledEdge<AutomatonNode, Label>> groupedEdges=new ArrayList<LabeledEdge<AutomatonNode,Label>>();
		if(edges!=null && !edges.isEmpty()){
			for(LabeledEdge<AutomatonNode, Label> e : edges){
				LabeledEdge<AutomatonNode, Label> groupedEdge=destinationExists(groupedEdges, e);
				if(groupedEdge != null){
					StringLabel label=new StringLabel(groupedEdge.getLabel()+","+e.getLabel());
					groupedEdge.setLabel(label);
				}else{
					groupedEdge=new LabeledEdge<AutomatonNode, Label>(e.getFrom(),e.getTarget(),e.getLabel());
					groupedEdges.add(groupedEdge);
				}
				
			}
		}
		return groupedEdges;
	}
	
	private LabeledEdge<AutomatonNode, Label> destinationExists(List<LabeledEdge<AutomatonNode, Label>> edges , LabeledEdge<AutomatonNode, Label> edge ){
		for(LabeledEdge<AutomatonNode, Label> e : edges){
			if(e.getFrom()==edge.getFrom() && e.getTarget() == edge.getTarget()){
				return e;
			}
		}
		return null;
	}
	
	public void draw(String file, int nodeVerbosity, int edgeVerbosity){
		String result="";
		String space="    ";
		result+="digraph G{\n";
		
		for(AutomatonNode n : getNodes()){
			
			result+=n.draw(nodeVerbosity);
			
			List<LabeledEdge<AutomatonNode, Label>> edges = getAdjacent(n);
			if(edges!=null && !edges.isEmpty()){
				List<LabeledEdge<AutomatonNode, Label>> groupedEdges = groupEdges(edges);
				for(LabeledEdge<AutomatonNode, Label> e : groupedEdges){
					result+=space+e.draw(edgeVerbosity);
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
	
	public void draw(String file, int verbosity){
		draw(file, verbosity, verbosity);
	}
	
	//TODO: check why do we need this, why it does not automatically realizes that the comparedTo method for labeledEdge should be called in directedGraph
	public boolean containsEdge(LabeledEdge<AutomatonNode, Label> edge){
		List<LabeledEdge<AutomatonNode, Label>> edges=getAdjacent(edge.getFrom());
		   if(edges==null || edges.isEmpty()){
			   return false;
		   }
		   for(LabeledEdge<AutomatonNode, Label> e : edges){
			   if(e.compareTo(edge)){
				   return true;
			   }
		   }
		   return false;
	}

}
