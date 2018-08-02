package specification;

import java.util.ArrayList;

import jdd.bdd.BDD;
import game.BDDWrapper;
import game.Variable;

public class Agent2D extends Agent{
	
	/**
	 * X and Y positions
	 */
	protected Variable[] xVars;
	protected Variable[] yVars;
	
//	/**
//	 * if it is visible and must be shown in the map
//	 */
//	protected Variable visible;
	

	/**
	 * Assumes xVariables and yVariables and Visible are subset of variables
	 * @param argName
	 * @param argType
	 * @param argVars
	 * @param argPrimeVars
	 * @param argActions
	 * @param argTrans
	 * @param argInit
	 */
	public Agent2D(BDD bdd, String argName, AgentType argType,
			Variable[] argVars,  Variable[] argActions,
			int argTrans, int argInit, Variable[] argXVars, Variable[] argYVars) {
		super(bdd, argName, argType, argVars,  argActions, argTrans, argInit);
		// TODO Auto-generated constructor stub
		xVars = argXVars;
		yVars = argYVars;
//		visible = argVisible;
	}
	
	public Variable[] getXVars(){
		return xVars;
	}
	
	public Variable[] getYVars(){
		return yVars;
	}
	
	/**
	 * returns all the variables other than X and Y vars for the agent
	 * @return
	 */
	public Variable[] getNonCoordinationVars(){
		Variable[] coordinates = Variable.unionVariables(getXVars(), getYVars());
		return Variable.difference(getVariables(), coordinates);
	}
	
	public static ArrayList<Agent2D> getAgentsWithType(ArrayList<Agent2D> agents, AgentType type){
		ArrayList<Agent2D> result = new ArrayList<Agent2D>();
		for(Agent2D agent : agents){
			if(agent.type == type){
				result.add(agent);
			}
		}
		return result;
	}
	
	
//	public Variable getVisible(){
//		return visible;
//	}
	
//	public int assignCell(BDD bdd, GridCell2D cell){
//		return assignCell(bdd, xVars, yVars, cell);
//	}
//	
//	public static int assignCell(BDD bdd , Variable[] x, Variable[] y , GridCell2D cell){
//		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
//		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
//		int result=bdd.ref(bdd.and(xAssign, yAssing));
//		bdd.deref(xAssign);
//		bdd.deref(yAssing);
//		return result;
//	}
//	
//	public static int noCollisionWithStaticObstacles(BDD bdd, Variable[] xvars, Variable[] yvars, ArrayList<GridCell2D> staticObstacles){
//		int noCollision = bdd.ref(bdd.getOne());
//		for(GridCell2D obs : staticObstacles){
//			int collisionWithObs = assignCell(bdd, xvars, yvars, obs);
//			int noCollisionWithObs = bdd.ref(bdd.not(collisionWithObs));
//			noCollision = bdd.andTo(noCollision, noCollisionWithObs);
//			bdd.deref(collisionWithObs);
//			bdd.deref(noCollisionWithObs);
//		}
//		return noCollision;
//	}

}
