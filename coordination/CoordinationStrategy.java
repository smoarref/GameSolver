package coordination;

import java.util.ArrayList;

import utils.UtilityMethods;
import game.BDDWrapper;
import game.GameStructure;
import game.Player;
import game.Variable;
import jdd.bdd.BDD;

public class CoordinationStrategy extends CoordinationGameStructure{

	private CompositionalCoordinationGameStructure coordinationGameStructure; 
	private Variable[] counterVariables;
	private ArrayList<Integer> strategy;
	int counterCube = -1;
	
	public CoordinationStrategy(BDD argBdd) {
		super(argBdd);
	}
	
	public  CoordinationStrategy(BDD argBDD, CompositionalCoordinationGameStructure argCoordinationGameStructure, Variable[] argCounterVariables, ArrayList<Integer> argStrategy){
		super(argBDD);
		coordinationGameStructure = argCoordinationGameStructure;
		counterVariables = argCounterVariables; 
		strategy = argStrategy; 
		variables= Variable.unionVariables(coordinationGameStructure.variables,counterVariables);
		primedVariables = Variable.getPrimedCopy(variables);
		
		actionVars = coordinationGameStructure.actionVars;
		actionsCube = coordinationGameStructure.getActionsCube();
		
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables),Variable.getBDDVars(variables));
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
		//We set the T_env and T_sys to -1 to indicate that they are not computed yet and are stored as partitioned transition relation
		T_env= -1;
		T_sys= -1;
	}
	
	
	public int getCounterCube(){
		if(counterCube==-1){
			counterCube = BDDWrapper.createCube(bdd, counterVariables);
		}
		return counterCube;
	}
	
	public void playCoordinationGameRandomly(int init){
		
	
		
		
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, init, vars);
//		int randomIndex = UtilityMethods.randomIndex(minterms.size());
//		String chosenMinterm = minterms.get(randomIndex);
//		int currentState = BDDWrapper.mintermToBDDFormula(bdd, chosenMinterm, vars);
		
		int counterInit = BDDWrapper.assign(bdd, 0, counterVariables);
		int strategyInit = BDDWrapper.and(bdd, init, counterInit);
		
		int currentState = UtilityMethods.chooseStateRandomly(bdd, strategyInit, variables);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd, "current state", currentState);
		
		while(true){
			
			cleanCoordinationGameStatePrintAndWait("current state is", currentState);
			
			int possibleNextStates = symbolicGameOneStepExecution(currentState);
			
			currentState = UtilityMethods.chooseStateRandomly(bdd, possibleNextStates, variables);
//			cleanPrintSetOutWithDontCares(bdd, currentState, densityVars, taskVars, successSignals);
			
		}
		
		
		
	}
	
	public int symbolicGameOneStepExecution(int set){
		
		int counterPart = BDDWrapper.exists(bdd, set, coordinationGameStructure.getVariablesCube());
		int statePart = BDDWrapper.exists(bdd, set, getCounterCube());
		
		int envMoves = coordinationGameStructure.EpostImage(statePart, Player.ENVIRONMENT);
		
//		cleanCoordinationGameStatePrintAndWait("env moves in symbolic step", envMoves);
		
		//the value of the counter does not change over environment transitions
		int envMovesWithSameNextCounter = BDDWrapper.and(bdd, envMoves, counterPart);
		int strategyEpost = strategyEpost(envMovesWithSameNextCounter);
		BDDWrapper.free(bdd, envMovesWithSameNextCounter);
		
		
		
		int sysMoves = coordinationGameStructure.EpostImage(envMoves, Player.SYSTEM);
		
		sysMoves = BDDWrapper.andTo(bdd, sysMoves, strategyEpost);
		
//		cleanCoordinationGameStatePrintAndWait("sys moves in symbolic step ", sysMoves);
		
		return sysMoves;
	}
	
	private int strategyEpost(int set){
		int epost = bdd.ref(bdd.getZero());
		for(Integer trans : strategy){
			int post = BDDWrapper.EpostImage(bdd, set, getVariablesAndActionsCube(), trans, getVprimetoV());
			epost = BDDWrapper.orTo(bdd, epost, post);
		}
		return epost;
	}
	
	public void cleanPrintSetOutWithDontCares(int formula){
//		ArrayList<String> minterms = BDDWrapper.minterms(bdd, formula);
//		UtilityMethods.debugBDDMethods(bdd, "formula is ", formula);
		
		
		if(formula ==1){
			System.out.println("TRUE");
			return;
		}
		

		ArrayList<String> minterms = BDDWrapper.mintermsWithDontCares(bdd, formula, variables);
		if(minterms == null){
			System.out.println("FALSE");
			return;
		}
		for(String minterm : minterms ){
//			System.out.println("mintrem is "+minterm);
			int index = 0;
			//print density vars
			System.out.print("densityVars: ");
			for(int i=0; i<coordinationGameStructure.densityVars.size(); i++){
				String value = minterm.substring(index, index+coordinationGameStructure.densityVars.get(i).length);
				index+=coordinationGameStructure.densityVars.get(i).length;
				System.out.print(value+" ");
			}
			//tasks
			System.out.print("Tasks: ");
			String value = minterm.substring(index, index+coordinationGameStructure.taskVars.length);
			index+= coordinationGameStructure.taskVars.length;
			System.out.print(value);
			System.out.print(" Signals: ");
			String successSignalsValues = minterm.substring(index, index+coordinationGameStructure.successSignals.length);
			index+=coordinationGameStructure.successSignals.length;
			System.out.print(successSignalsValues);
			System.out.print(" Counter: ");
			String counterValue = minterm.substring(index);
			System.out.print(counterValue);
			System.out.println();
		}
	}
	
}
