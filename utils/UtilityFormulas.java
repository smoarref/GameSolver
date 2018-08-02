package utils;

import game.BDDWrapper;
import game.Variable;

import java.util.ArrayList;

import jdd.bdd.BDD;
import specification.Agent2D;
import specification.GridCell2D;

public class UtilityFormulas {
	
	//no collision with static obstacles
	public static int noCollisionWithStaticObstacles(BDD bdd, Variable[] xvars, Variable[] yvars, ArrayList<GridCell2D> staticObstacles){
		int noCollision = bdd.ref(bdd.getOne());
		for(GridCell2D obs : staticObstacles){
			int collisionWithObs = assignCell(bdd, xvars, yvars, obs);
			int noCollisionWithObs = bdd.ref(bdd.not(collisionWithObs));
			noCollision = bdd.andTo(noCollision, noCollisionWithObs);
			bdd.deref(collisionWithObs);
			bdd.deref(noCollisionWithObs);
		}
		return noCollision;
	}
	
	public static int noCollisionWithStaticObstacles(BDD bdd, Agent2D agent, ArrayList<GridCell2D> staticObstacles){
		return noCollisionWithStaticObstacles(bdd, agent.getXVars(), agent.getYVars(), staticObstacles);
	}
	
	//assigning a 2D gridCell to variables x and y 
	public static int assignCell(BDD bdd , Variable[] x, Variable[] y , GridCell2D cell){
		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
		int result=bdd.ref(bdd.and(xAssign, yAssing));
		bdd.deref(xAssign);
		bdd.deref(yAssing);
		return result;
	}
	
	public static int assignCell(BDD bdd, Agent2D agent, GridCell2D cell){
		return assignCell(bdd, agent.getXVars(), agent.getYVars(), cell);
	}
	
	public static int noCollision(BDD bdd, Agent2D agent1, Agent2D agent2){
		return noCollisionObjective(bdd, agent1.getXVars(), agent1.getYVars(), agent2.getXVars(), agent2.getYVars());
	}
	
	//No collision between agents
	public static int noCollisionObjective(BDD bdd, Variable[] x1var, Variable[] y1var, Variable[] x2var, Variable[] y2var){
		int[] x1 = Variable.getBDDVars(x1var);
		int[] x2 = Variable.getBDDVars(x2var);
		int[] y1 = Variable.getBDDVars(y1var);
		int[] y2 = Variable.getBDDVars(y2var);
		
		int complementSafetyObjective=bdd.ref(bdd.getOne());
		for(int i=0;i<x1.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x1[i], x2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			bdd.deref(tmpX);
		}
		
		for(int i=0;i<y1.length;i++){
			int tmpY= bdd.ref(bdd.biimp(y1[i], y2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		bdd.deref(complementSafetyObjective);
		return safety;
	}
	
	/**
	 * creates the formula lowerBound<= vars <=upperBound
	 * @param bdd
	 * @param vars
	 * @param lowerBound
	 * @param upperBound
	 * @return
	 */
	public static int bound(BDD bdd, Variable[] vars, int lowerBound, int upperBound){
		int result = bdd.ref(bdd.getZero());
		for(int i = lowerBound ; i<=upperBound; i++){
			int value = BDDWrapper.assign(bdd, i, vars);
			result=BDDWrapper.orTo(bdd, result, value);
			BDDWrapper.free(bdd, result);
		}
		return result;
	}
}
