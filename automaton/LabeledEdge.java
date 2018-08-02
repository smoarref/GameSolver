package automaton;

public class LabeledEdge<N extends Node, L extends Label>  extends Edge<N>{
	
	L label;
	
	public LabeledEdge(final N argFrom, final N argTo, L argLabel){
		super(argFrom, argTo);
		label = argLabel;
	}
	
	public L getLabel(){
		return label;
	}
	
	public void setLabel(L argLabel){
		label=argLabel;
	}
	
	public void printEdge(){
		System.out.println("Edge "+getFrom()+ "-- "+label.toString()+" --> "+ getTarget());
	}
	
	public String toString(){
		return "Edge "+getFrom()+ "-- "+label.toString()+" --> "+ getTarget();
	}
	
	public boolean compareTo(LabeledEdge<N,L> e){
		if(e.getFrom()==getFrom() && e.getTarget()==getTarget() && e.getLabel().equals(getLabel())){
			return true;
		}
		return false;
	}
	
	public LabeledEdge<N,L> reversedEdge(){
		return new LabeledEdge<N,L>(getTarget(), getFrom(), getLabel());
	}
	
	public String draw(int verbosity){
		String result= this.getFrom().getName()+" -> "+this.getTarget().getName();
		if(verbosity>=1){
			result+=" [label=\""+label.toString()+"\"]";
		}
		result+=";\n";
		return result;
	}
}
