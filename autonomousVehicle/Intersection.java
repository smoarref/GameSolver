package autonomousVehicle;

import java.util.ArrayList;
import java.util.HashMap;

import specification.GridCell2D;

public class Intersection extends RoadSegment {
	
	/**
	 * Admissible directions in each grid 
	 */
	HashMap<GridCell2D, Direction[]> admissibleDirections;

	public Intersection(String id, RoadSegmentType argType,
			ArrayList<GridCell2D> argGrids, HashMap<GridCell2D, Direction[]> argAdmissibleDirections) {
		super(id, argType, argGrids);
		admissibleDirections = argAdmissibleDirections;
	}
	
	public Direction[] getAdmissibleDirections(GridCell2D grid){
		return admissibleDirections.get(grid);
	}
	
}
