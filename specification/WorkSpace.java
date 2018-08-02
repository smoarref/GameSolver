package specification;

import game.Variable;

import java.util.HashMap;
import java.util.List;

import jdd.bdd.BDD;
import automaton.DirectedGraph;
import automaton.Edge;
import automaton.Node;

public class WorkSpace {
	DirectedGraph<String, Node, Edge<Node>> regionGraph;
	Variable[] regionVars; 
	HashMap<Node, Variable> regionToVarMap;
	HashMap<Variable, Node> varToRegionMap; 
	
	public WorkSpace(BDD bdd, DirectedGraph<String, Node, Edge<Node>> rGraph){
		regionGraph = rGraph;
		regionToVarMap = new HashMap<Node, Variable>();
		varToRegionMap = new HashMap<Variable, Node>();
		
		List<Node> regionNodes = regionGraph.getNodes();
		regionVars = new Variable[regionNodes.size()];
		
		for(int i=0; i<regionNodes.size();i++){
			Node r = regionNodes.get(i);
			//create a variable and its prime version
			Variable rVar = Variable.createVariableAndPrimedVariable(bdd, r.getName());
			regionVars[i] = rVar;
			
			//update the maps 
			regionToVarMap.put(r,rVar);
			varToRegionMap.put(rVar, r);
		}
		
	}
	
	public DirectedGraph<String, Node, Edge<Node>> getRegionGraph(){
		return regionGraph;
	}
	
	public Variable[] getRegionVars(){
		return regionVars;
	}
	
	public HashMap<Node, Variable> getRegionToVarsMap(){
		return regionToVarMap;
	}
	
	public HashMap<Variable, Node> getVarsToRegionMap(){
		return varToRegionMap;
	}
}
