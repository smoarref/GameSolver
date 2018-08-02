package autonomousVehicle;

import java.util.ArrayList;

import automaton.Node;
import specification.GridCell2D;

public class RoadSegment extends Node{
	private RoadSegmentType type;
	private ArrayList<GridCell2D> grids; 
	
	public RoadSegmentType getType(){
		return type;
	}
	
	public ArrayList<GridCell2D> getGrids(){
		return grids;
	}
	
	public RoadSegment(String id, RoadSegmentType argType, ArrayList<GridCell2D> argGrids){
		super(id);
		type = argType;
		grids = argGrids;
	}
	
}
