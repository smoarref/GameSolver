package automaton;

/**
 * an abstract class for node and edge labels
 * @author moarref
 *
 */
public abstract class Label {
	
	/**
	 * returns a string representing the label object
	 */
	public abstract String toString();
	
	/**
	 * check if the label is equivalent to another label
	 * @param arg
	 * @return
	 */
	public abstract boolean equals(Label arg);
}
