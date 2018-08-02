package symbolic;

import java.util.ArrayList;
import java.util.Arrays;

import jdd.bdd.BDD;

public class CounterExampleGuidedControl {
	
	private final SymbolicGame concreteGame;
	private ArrayList<Integer> abstractionPropositionsBDDs;
	private ArrayList<Integer> abstractEnvVariables;
	private ArrayList<Integer> abstractSysVariables;
	private SymbolicGame abstractGame;
	private BDD bdd;
	
	
	public CounterExampleGuidedControl(BDD argBdd , SymbolicGame game , ArrayList<Integer> obj){
		bdd=argBdd;
		concreteGame = game;
		abstractionPropositionsBDDs = obj;
		abstractEnvVariables=new ArrayList<Integer>();
		abstractSysVariables=new ArrayList<Integer>();
	}
	
	public int  csGuidedControl(int objective){
		//compute initial abstraction
		abstractGame= initialAbstraction();
		
		//
		while(!abstractGame.isRealizable(objective)){
			//TODO: correct this part
			int winningRegion=-1;
			int cs = abstractGame.counterStrategy(winningRegion, objective); 
			//check if counter-strategy is spurious 
			if(checkCounterStrategy(cs)){
				abstractGame=refineAbstraction();
			}else{
				return cs;
			}
		}
		
		return -1;
	}
	
	public boolean checkCounterStrategy(int cs){
		return false;
	}
	
	
	//TODO
	public static void main(String[] args) {
		Integer[] a = new Integer[2];
		
		

	}
	
	//TODO
	public SymbolicGame initialAbstraction(){
		
		
		//creating the abstract variables d_1, ... , d_n where d_i <-> h_i and h_i \in abstract propositions
		for(int i=0;i<abstractionPropositionsBDDs.size();i++){
			abstractEnvVariables.add(bdd.createVar());
		}
		for(int i=0;i<abstractionPropositionsBDDs.size();i++){
			abstractSysVariables.add(bdd.createVar());
		}
		
		
		//create the initial relation \hat{I} of the abstract game
		int hatI;
		
		//create the transition relation \hat{T} of the abstract game
		int hatT;
		
		//create the abstract game corresponding to \hat{I} and \hat{T}
		//abstractGame = new SymbolicGame(bdd, argInputVariables, argOutputVariables, argInputPrimedVars, argOutputPrimedVars, hatI, hatT);
		
		return null;
	}
	
	//TODO
	public SymbolicGame refineAbstraction(){
		return null;
	}
	
	

}
