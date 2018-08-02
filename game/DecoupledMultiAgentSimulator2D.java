package game;

import java.util.ArrayList;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityMethods;

//TODO: can we make the controllers compositional 
//and avoid building the central game? --> the second part already implemented
public class DecoupledMultiAgentSimulator2D extends Simulator2D {
	
	int composedController;
	ArrayList<Agent2D> controlledAgents;
	ArrayList<Agent2D> uncontrolledAgents;
	
	Variable[] vars;
	Variable[] primedVars;
	Variable[] actionVars;
	
	Variable[] varsAndActionVars;
	
	Variable[] controlledVars;
	Variable[] uncontrolledVars;
	
	Permutation VPrimeToV;
	int variablesAndActionsCube;
	
	int controlledVarsCube=-1;
	int uncontrolledVarsCube=-1;
	
//	public DecoupledMultiAgentSimulator2D(BDD argBDD,
//			ArrayList<Agent2D> argAgents, GameStructure argGameStructure,
//			int argInit) {
//		super(argBDD, argAgents, argGameStructure, argInit);
//		// TODO Auto-generated constructor stub
//	}
	
	
	public DecoupledMultiAgentSimulator2D(BDD argBDD, ArrayList<Agent2D> argAgents, 
			int argComposedController, int argInit){
		super(argBDD, argAgents, null, argInit);
		composedController = argComposedController;
		controlledAgents = new ArrayList<Agent2D>();
		uncontrolledAgents = new ArrayList<Agent2D>();
		
		controlledVars = null;
		uncontrolledVars = null;
		
		for(Agent2D agent : agents){
			if(agent.getType() == AgentType.Controllable){
				controlledAgents.add(agent);
				controlledVars = Variable.unionVariables(controlledVars, agent.getVariables());
				
			}else{
				uncontrolledAgents.add(agent);
				uncontrolledVars = Variable.unionVariables(uncontrolledVars, agent.getVariables());
			}
		}
		
		vars = UtilityMethods.getAgentsVariables(agents);
		actionVars = UtilityMethods.getAgentsActionVariables(agents);
		primedVars = Variable.getPrimedCopy(vars);
		varsAndActionVars = Variable.unionVariables(vars, actionVars);
		VPrimeToV = BDDWrapper.createPermutations(bdd, primedVars, vars);
		
		Variable[] varsAndActions = Variable.unionVariables(vars, actionVars);
		variablesAndActionsCube = BDDWrapper.createCube(bdd, varsAndActions);
		
		controlledVarsCube = BDDWrapper.createCube(argBDD, controlledVars);
		uncontrolledVarsCube = BDDWrapper.createCube(bdd, uncontrolledVars);
	}
	
	private int getControlledVarsCube(){
		return controlledVarsCube;
	}
	
	private int getUncontrolledVarsCube(){
		return uncontrolledVarsCube;
	}
	
	private Permutation getVPrimeToV(){
		return VPrimeToV;
	}
	
	private int getVariablesAndActionsCube(){
		return variablesAndActionsCube;
	}
	
//	public ArrayList<GridCell2D> getInitialPositions(){		
//		return chooseState(initState);
//	}
	
	public ArrayList<GridCell2D> simulateOneStep(){
//		System.out.println("simulating");
//		UtilityMethods.debugBDDMethods(bdd, "current state is", currentState);
//		UtilityMethods.getUserInput();
		
		//partition the current state
		int controlledPartOfCurrentState = BDDWrapper.exists(bdd, currentState, getUncontrolledVarsCube());
//		int uncontrolledPartOfCurrentState = BDDWrapper.exists(bdd, currentState, controlledVarsCube);
		
//		UtilityMethods.debugBDDMethods(bdd, "controlled part of the current state is ", controlledPartOfCurrentState);
////		UtilityMethods.debugBDDMethods(bdd, "uncontrolled part of the current state is", uncontrolledPartOfCurrentState);
//		UtilityMethods.getUserInput();
		
		int envMove = bdd.ref(bdd.getOne());
		for(Agent2D agent : uncontrolledAgents){
//			int uncontrolledAgentMove = BDDWrapper.EpostImage(bdd, currentState, agent.getVariablesAndActionsCube(), agent.getTransitionRelation(), agent.getVPrimeToV());
			int uncontrolledAgentMove = BDDWrapper.EpostImage(bdd, currentState, getVariablesAndActionsCube(), agent.getTransitionRelation(), getVPrimeToV());
			
//			UtilityMethods.debugBDDMethods(bdd, "uncontrolled agent's moves", uncontrolledAgentMove);
//			UtilityMethods.getUserInput();
			
			envMove = BDDWrapper.andTo(bdd, envMove, uncontrolledAgentMove);
			BDDWrapper.free(bdd, uncontrolledAgentMove);
		}
		
		//keep the system state the same
		int intermediateState = BDDWrapper.and(bdd, envMove, controlledPartOfCurrentState);
		BDDWrapper.free(bdd, controlledPartOfCurrentState);
		
		int randomMinterm = BDDWrapper.oneRandomMinterm(bdd, intermediateState, vars);
		BDDWrapper.free(bdd, intermediateState);
		
//		UtilityMethods.debugBDDMethods(bdd, "env move is", envMove);
//		UtilityMethods.getUserInput();
//		
//		UtilityMethods.debugBDDMethods(bdd, "next randomly chosen ", randomMinterm);
//		UtilityMethods.getUserInput();
		
		if(randomMinterm== -1){
			System.err.println("Something is wrong in simulation");
			UtilityMethods.debugBDDMethods(bdd, "uncontrolled agent possible next states are", envMove);
			UtilityMethods.getUserInput();
		}
		
		int sysMove = bdd.ref(bdd.getOne());
		int allowedActions = BDDWrapper.and(bdd, randomMinterm, composedController);
		
//		UtilityMethods.debugBDDMethods(bdd, "allowed actions are", allowedActions);
//		UtilityMethods.getUserInput();
		
		int chosenAction = BDDWrapper.oneRandomMinterm(bdd, allowedActions, varsAndActionVars);
		
//		UtilityMethods.debugBDDMethods(bdd, "chosen action", chosenAction);
//		UtilityMethods.getUserInput();
		
		for(Agent2D agent : controlledAgents){
			
			int controlledAgentMove = BDDWrapper.EpostImage(bdd, chosenAction, getVariablesAndActionsCube(), agent.getTransitionRelation(), getVPrimeToV());
			sysMove = BDDWrapper.andTo(bdd, sysMove, controlledAgentMove);
			
//			UtilityMethods.debugBDDMethods(bdd, "sys move is", sysMove);
//			UtilityMethods.getUserInput();
			
			BDDWrapper.free(bdd, controlledAgentMove);
		}
		
		int envState = BDDWrapper.exists(bdd, randomMinterm, controlledVarsCube);
		
//		UtilityMethods.debugBDDMethods(bdd, "intermediate env state", envState);
//		UtilityMethods.getUserInput();
		
		sysMove = BDDWrapper.andTo(bdd, sysMove, envState);
		BDDWrapper.free(bdd, envMove);
		BDDWrapper.free(bdd, envState);
		
//		UtilityMethods.debugBDDMethods(bdd, "available system moves", sysMove);
//		UtilityMethods.getUserInput();
		
		int nextCells = BDDWrapper.oneRandomMinterm(bdd, sysMove, vars);
		
//		UtilityMethods.debugBDDMethods(bdd, "next randomly chosen ", nextCells);
//		UtilityMethods.getUserInput();
		
		if(nextCells == -1){
			System.err.println("Something is wrong in simulation");
			UtilityMethods.debugBDDMethods(bdd, "possible next states are", nextCells);
			UtilityMethods.getUserInput();
		}
		
		BDDWrapper.free(bdd, randomMinterm);
		BDDWrapper.free(bdd, allowedActions);
		freeCurrentSimulationState();
		
		ArrayList<GridCell2D> result = chooseState(nextCells);
    	BDDWrapper.free(bdd, nextCells);
    	BDDWrapper.free(bdd, sysMove);

    	
		return result;
	}
	
	/**
	 * computes and chooses one potential next gridcell from the given set of states 
	 * @param states
	 * @return
	 */
	public ArrayList<GridCell2D> chooseState(int states, Variable[] vars){
		String minterm = BDDWrapper.oneRandomMintermString(bdd, states, vars);
		if(minterm == null ){
    		System.err.println("the set of gridcells corresponding to the states is empty");
    		UtilityMethods.getUserInput();
    		return null;
    	}
		ArrayList<GridCell2D> result = UtilityMethods.getGridCellsFromState(bdd, minterm, vars, agents);
		currentState = BDDWrapper.mintermToBDDFormula(bdd, minterm, vars);
		return result;
	}
	
	public ArrayList<GridCell2D> chooseState(int states){		
		return chooseState(states, vars);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
