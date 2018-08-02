package automaton;

public class StringLabel extends Label{
	
	private String label;
	
	public StringLabel(String argLabel){
		label=argLabel;
	}
	
	public String toString() {
		return label;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setLabel(String newLabel){
		label = newLabel;
	}


	public boolean equals(Label arg) {
		StringLabel l;
		try{
			l  = (StringLabel) arg;
		}catch(Exception e){
			System.err.println("type mismatch between labels, cannot compare string label with "+arg.toString());
			return false;
		}
		// TODO Auto-generated method stub
		return label.equals(l.getLabel());
	}
	
}
