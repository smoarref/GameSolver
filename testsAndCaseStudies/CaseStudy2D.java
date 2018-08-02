package testsAndCaseStudies;

import game.GameStructure;
import game.Simulator2D;

import java.util.ArrayList;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;

//TODO: remove the solution and replace them with only simulator
public class CaseStudy2D {
	protected BDD bdd;
	protected ArrayList<Agent2D> agents;
	protected ArrayList<Agent2D> controlledAgents;
	protected ArrayList<Agent2D> uncontrolledAgents;
	protected int xDim;
	protected int yDim;
	protected GameStructure solution;
	protected  ArrayList<GridCell2D> staticObstacles;
	protected Simulator2D simulator;
	
	public CaseStudy2D(BDD argBDD, int argXDim, int argYDim, ArrayList<Agent2D> argAgents, GameStructure argGame, 
			ArrayList<GridCell2D> argStatic){
		bdd = argBDD;
		xDim = argXDim;
		yDim = argYDim;
		agents = argAgents;
		separateAgents();
		solution = argGame;
		staticObstacles = argStatic;
	}
	
	
	public CaseStudy2D(BDD argBDD, int argXDim, int argYDim, ArrayList<Agent2D> argAgents, GameStructure argGame, 
			ArrayList<GridCell2D> argStatic, Simulator2D argSimulator){
		bdd = argBDD;
		xDim = argXDim;
		yDim = argYDim;
		agents = argAgents;
		separateAgents();
		solution = argGame;
		staticObstacles = argStatic;
		simulator = argSimulator;
	}
	
	public CaseStudy2D(BDD argBDD, int argXDim, int argYDim, ArrayList<Agent2D> argControlledAgents, ArrayList<Agent2D> argUncontrolledAgents, 
			GameStructure argGame, ArrayList<GridCell2D> argStatic, Simulator2D argSimulator){
		bdd = argBDD;
		xDim = argXDim;
		yDim = argYDim;
		controlledAgents = argControlledAgents;
		uncontrolledAgents = argUncontrolledAgents;
		agents = new ArrayList<Agent2D>();
		agents.addAll(controlledAgents);
		agents.addAll(uncontrolledAgents);
		separateAgents();
		solution = argGame;
		staticObstacles = argStatic;
		simulator = argSimulator;
	}
	
	private void separateAgents(){
		if(agents == null) return;
		controlledAgents = new ArrayList<Agent2D>();
		uncontrolledAgents = new ArrayList<Agent2D>();
		for(Agent2D agent : agents){
			if(agent.getType() == AgentType.Uncontrollable){
				uncontrolledAgents.add(agent);
			}else{
				controlledAgents.add(agent);
			}
		}
	}
	
	public BDD getBDD(){
		return bdd;
	}
	
	public ArrayList<Agent2D> getAgents(){
		return agents;
	}
	
	public int getXdim(){
		return xDim;
	}
	
	public int getYdim(){
		return yDim;
	}
	
	public GameStructure getSolution(){
		return solution;
	}
	
	public ArrayList<GridCell2D> getStaticObstacles(){
		return staticObstacles;
	}
	
	public Simulator2D getSimulator(){
		return simulator;
	}

}
