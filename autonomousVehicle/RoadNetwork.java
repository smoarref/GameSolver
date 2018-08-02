package autonomousVehicle;

import automaton.DirectedGraph;
import automaton.Edge;

public class RoadNetwork extends DirectedGraph<String, RoadSegment, Edge<RoadSegment>>{
	
	public RoadNetwork(){
		
	}
	
	public RoadNetwork(String id){
		this.setId(id);
	}
	
	
}
