package automaton;

public class GameNode extends LabeledNode<StringLabel>{
	private boolean isInitial;
	private boolean isFinal;
	private char playerType;
	
	public GameNode(String argName, char player, StringLabel l ){
		super(argName, l);
		isInitial=false;
		isFinal=false;
		playerType=player;
	}
	
	public GameNode(String argName, boolean argIsInitial, boolean argIsfinal, char player, StringLabel l){
		super(argName,l);
		isInitial=argIsInitial;
		isFinal=argIsfinal;
		playerType=player;
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
	
	public char getPlayer(){
		return playerType;
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
		if(getPlayer()=='e' && isFinal){
			attributes+="shape=doublecircle ";
		}else if(getPlayer()=='e'){
			attributes+="shape=oval ";
		}else if(getPlayer()=='s' && isFinal){
			attributes+="shape=doublebox ";
		}else{
			attributes+="shape=box ";
		}
		
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
