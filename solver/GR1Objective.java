package solver;

import java.util.ArrayList;


public class GR1Objective {
	BDDWrapper bdd;
	private ArrayList<Integer> guarantees; 
	private ArrayList<Integer> assumptions;
	
	private ArrayList<Integer> safetyAssumptions;
	private ArrayList<Integer> safetyGuarantees;
	
	public GR1Objective(BDDWrapper argBdd, ArrayList<Integer> argAssumptions, ArrayList<Integer> argGuarantees){
		bdd = argBdd;
		assumptions = argAssumptions;
		guarantees = argGuarantees;
		safetyAssumptions = null;
		safetyGuarantees = null;
	}
	
	
	
	public GR1Objective(BDDWrapper argBdd, ArrayList<Integer> argAssumptions, ArrayList<Integer> argGuarantees, 
			ArrayList<Integer> argSafetyAssumptions, ArrayList<Integer> argSafetyGuarantees){
		bdd = argBdd;
		assumptions = argAssumptions;
		guarantees = argGuarantees;
		safetyAssumptions = argSafetyAssumptions;
		safetyGuarantees = argSafetyGuarantees;
	}
	
	public ArrayList<Integer> getAssumptions(){
		return assumptions;
	}
	
	public ArrayList<Integer> getGuarantees(){
		return guarantees;
	}
	
	public ArrayList<Integer> getSafetyAssumptions(){
		return safetyAssumptions;
	}
	
	public ArrayList<Integer> getSafetyGuarantees(){
		return safetyGuarantees;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
