package automaton;

public class AutomatonNode extends LabeledNode<StringLabel>{
	
	private boolean isInitial;
	private boolean isFinal;

	public AutomatonNode(String argName, StringLabel argLabel) {
		super(argName, argLabel);
		isInitial=false;
		isFinal=false;
	}
	
	public AutomatonNode(String argName, StringLabel argLabel, boolean argeIsInitial, boolean argIsFinal){
		super(argName, argLabel);
		isInitial=argeIsInitial;
		isFinal=argIsFinal;
	}
	
	public boolean isInitial(){
		return isInitial;
	}
	
	public boolean isFinal(){
		return isFinal;
	}
	
	public void setInitial(boolean init){
		isInitial=init;
	}
	
	public void setFinal(boolean fin){
		isFinal=fin;
	}
	
	public String toString(){
		return "Node "+getName()+", label="+this.getLabel().toString()+(isInitial?", is initial, ":" ")+(isFinal?", is accepting":" ");
	}
	
	
	/**
	 * returns the string that can be used in a dot file for visualization of the game graph node
	 * @return
	 */
	public String draw(int verbosity){
		String attributes=getName()+" [";

		
		if(isInitial){
			attributes+="style=filled,color=green ";
		}
		
		
		if(verbosity>=1){
			attributes+="label=\"" +getName()+"\\n "+getLabel().toString()+ "\"";
		}
		
		attributes+="];\n";
		return attributes;
	}
	
}
