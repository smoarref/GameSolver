package specification;

import java.util.ArrayList;




public interface Specification {
	
	public String toString();
	
	public ArrayList<String> getGuarantees();
	
	public void addGuarantee(String guarantee);
	
	public void addGuarantees(ArrayList<String> guarantees);
	
	public ArrayList<String> getAssumptions();
	
	public void addAssumption(String assumption);
	
	public void addAssumptions(ArrayList<String> assumptions);
	
}
