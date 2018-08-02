package automaton;

public class LabeledNode<L extends Label> extends Node{
	private L label;
	
	/**
	 * Constructor
	 * @param argName
	 * @param argLabel
	 */
	public LabeledNode(final String argName, L argLabel){
		super(argName);
		label=argLabel;
	}
	
	public L getLabel(){
		return label;
	}
	
	public void setLabel(L l){
		label=l;
	}
	
	public void printNode(){
		System.out.println("Node "+getName()+", label="+label.toString());
		
	}
	
	public String toString(){
		return "Node "+getName()+", label="+label.toString();
	}
}
