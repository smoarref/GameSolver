package solver;

import jdd.bdd.Permutation;

public interface GameStructureInterface {
	public Permutation getVtoVprime();
	
	public Permutation getVprimetoV();
	
	public String getID();
	
	public void setID(String argId);
}
