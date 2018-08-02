package autonomousVehicle;

public enum Direction {
	North, //0
	South, //1
	West, //2
	East; //3
	
	public static Direction getDirection(int ordinal){
		for(Direction dir : Direction.values()){
			if(dir.ordinal()==ordinal){
				return dir;
			}
		}
		return null;
	}
}

