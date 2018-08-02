package autonomousVehicle;

/**
 * actions of simple autonomous vehicles:
 * 
 * stop: stays at the current location while preserving the direction
 * backup: moves one step back while preserving the direction
 * move forward: moves one step in the specified direction while preserving the direction
 * move fast forward: moves two steps forward in the specified direction while preserving the direction
 * turn left: moves one step left and rotates the direction 90 degree counter-clockwise 
 * turn right: moves one step right and rotates the direction 90 degree clockwise
 * left: moves to the left cell without changing current direction
 * right: moves to the right cell without changing current direction
 * @author moarref
 *
 */

public enum VehicleActions {
	stop,
	backup,
	moveForward,
	moveFastForward,
	turnLeft,
	turnRight,
	left,
	right;
	
	public static VehicleActions getVehicleAction(int ordinal){
		for(VehicleActions act : VehicleActions.values()){
			if(act.ordinal()==ordinal){
				return act;
			}
		}
		return null;
	}
}
