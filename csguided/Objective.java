package csguided;

public class Objective {
	
	public enum ObjectiveType{
		Reachability,
		Safety
	}
	
	//type of the objective
	ObjectiveType type;
	//the boolean formula
	int formula;
	
	public Objective(ObjectiveType argType, int argFormula){
		type=argType;
		formula=argFormula;
	}
	
	public int getObjectiveFormula(){
		return formula;
	}
	
	public ObjectiveType getObjectiveType(){
		return type;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
