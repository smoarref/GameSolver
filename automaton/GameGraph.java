package automaton;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GameGraph extends DirectedGraph<String, GameNode, LabeledEdge<GameNode, Label>> {
	
	public GameGraph(String argId){
		this.setId(argId);
	}
	
	public static void main(String[] args){
		GameGraph g=new GameGraph("Game Example");
		GameNode[] nodes = new GameNode[5];
		nodes[0]= new GameNode("q_1", true, false, 'e', new StringLabel("a & b"));
		nodes[1]= new GameNode("q_2",'s', new StringLabel("a & b"));
		nodes[2] = new GameNode("q_3", 's', new StringLabel("!a & b"));
		nodes[3] = new GameNode("q_4", 'e', new StringLabel("a & !b"));
		nodes[4] = new GameNode("q_5", false, true, 'e', new StringLabel("!a & !b"));
		
		for(int i=0;i<nodes.length;i++){
			g.addNode(nodes[i]);
		}
		
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[0], nodes[1], new StringLabel("l")));
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[0], nodes[2], new StringLabel("!l")));
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[1], nodes[3], new StringLabel("l")));
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[3], nodes[1], new StringLabel("!l")));
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[2], nodes[4], new StringLabel("l")));
		g.addEdge(new LabeledEdge<GameNode, Label>(nodes[4], nodes[2], new StringLabel("!l")));
		
		g.print();
		g.draw("gamegraph.dot", 1);
		
	}
	
	/**
	 * Group the edges with the same source and destination
	 * Assumes that all edges have the same source
	 * Assumes that there is only one single edge for any action
	 * @param edges
	 * @return
	 */
	private List<LabeledEdge<GameNode, Label>> groupEdges(List<LabeledEdge<GameNode, Label>> edges){
		ArrayList<LabeledEdge<GameNode, Label>> groupedEdges=new ArrayList<LabeledEdge<GameNode,Label>>();
		if(edges!=null && !edges.isEmpty()){
			for(LabeledEdge<GameNode, Label> e : edges){
				LabeledEdge<GameNode, Label> groupedEdge=destinationExists(groupedEdges, e);
				if(groupedEdge != null){
					StringLabel label=new StringLabel(groupedEdge.getLabel()+","+e.getLabel());
					groupedEdge.setLabel(label);
				}else{
					groupedEdge=new LabeledEdge<GameNode, Label>(e.getFrom(),e.getTarget(),e.getLabel());
					groupedEdges.add(groupedEdge);
				}
				
			}
		}
		return groupedEdges;
	}
	
	private LabeledEdge<GameNode, Label> destinationExists(List<LabeledEdge<GameNode, Label>> edges , LabeledEdge<GameNode, Label> edge ){
		for(LabeledEdge<GameNode, Label> e : edges){
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
		
		for(GameNode n : getNodes()){
			
			result+=n.draw(nodeVerbosity);
			
			List<LabeledEdge<GameNode, Label>> edges = getAdjacent(n);
			if(edges!=null && !edges.isEmpty()){
				List<LabeledEdge<GameNode, Label>> groupedEdges = groupEdges(edges);
				for(LabeledEdge<GameNode, Label> e : groupedEdges){
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
			Runtime.getRuntime().exec("dot -Tpdf "+file+" -o "+file.substring(0, file.indexOf("."))+".pdf");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void draw(String file, int verbosity){
		draw(file, verbosity, verbosity);
	}
	
	//TODO: check why do we need this, why it does not automatically realizes that the comparedTo method for labeledEdge should be called in directedGraph
	public boolean containsEdge(LabeledEdge<GameNode, Label> edge){
		List<LabeledEdge<GameNode, Label>> edges=getAdjacent(edge.getFrom());
		   if(edges==null || edges.isEmpty()){
			   return false;
		   }
		   for(LabeledEdge<GameNode, Label> e : edges){
			   if(e.compareTo(edge)){
				   return true;
			   }
		   }
		   return false;
	}
}
