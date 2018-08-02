package autonomousVehicle;

import java.util.ArrayList;

import specification.GridCell2D;

public class OneWayRoad extends RoadSegment {
	private Direction direction;
	
	public OneWayRoad(String name, ArrayList<GridCell2D> argGrids, Direction argDirection){
		super(name, RoadSegmentType.OneWayRoad, argGrids);
		direction = argDirection;
	}
	
	public Direction getDirection(){
		return direction;
	}
}
