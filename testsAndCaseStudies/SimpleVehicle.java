package testsAndCaseStudies;

import java.util.ArrayList;

import jdd.bdd.BDD;
import game.BDDWrapper;
import specification.GridCell2D;
import game.Variable;
import specification.Agent2D;
import specification.AgentType;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;

public class SimpleVehicle{
	
	public static Agent2D createSimpleVehicle(BDD bdd, int xDim, int yDim, GridCell2D initCell, ArrayList<GridCell2D> staticObstacles,
			String argName, AgentType argType){
		
		int numOfXBits = UtilityMethods.numOfBits(xDim);
		Variable[] argXVars = Variable.createVariables(bdd, numOfXBits, argName+"_x");
		
		
		
		int numOfYBits = UtilityMethods.numOfBits(yDim);
		Variable[] argYVars = Variable.createVariables(bdd, numOfYBits, argName+"_Y");
		
//		//TODO: visible is for special agents which can disappear, visible is not included in the set of variables
//		// right now
//		Variable argVisible = null;
		
		//variables of the simpleVehicle is union of x and y positions
		Variable[] argVars = Variable.unionVariables(argXVars, argYVars);
		
		Variable[] argPrimeVars = Variable.createPrimeVariables(bdd, argVars);
		
		//four actions for simple vehicle
		//00: stop, 01: move backard, 10: move forward, 11: fast move forward; move two steps forward
		Variable[] argActions = Variable.createVariables(bdd, 2, "act_"+argName);
		
		int[] actions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(argActions));
		
		int argInit = UtilityFormulas.assignCell(bdd, argXVars, argYVars, initCell);
		
		int argTrans = createTransitionRelation(bdd, xDim, yDim, argXVars, argYVars, actions);
		
		
		
		
		
		Agent2D agent =  new Agent2D(bdd, argName, argType, argVars, argActions, argTrans, argInit,
				argXVars, argYVars);
		
		agent.setActions(actions);
		
		int same = BDDWrapper.same(bdd, argVars, argPrimeVars);
		agent.setSame(same);
		
		return agent;
	}
	
	
	public static int createTransitionRelation(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars, int[] actions){
		
		int T = UtilityTransitionRelations.upTransitions(bdd, xdim, xVars, yVars, actions[0]);
		T=bdd.orTo(T, UtilityTransitionRelations.downTransitions(bdd, xdim, xVars, yVars, actions[1]));
		T=bdd.orTo(T, UtilityTransitionRelations.leftTransitions(bdd, ydim, xVars, yVars, actions[2]));
		T=bdd.orTo(T, UtilityTransitionRelations.rightTransitions(bdd, ydim, xVars, yVars, actions[3]));
		
//		BDDWrapper.BDD_Usage(bdd);
//		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
//		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
//		
//		int T=bdd.ref(bdd.getZero());
		
////		ArrayList<Integer> TList =  new ArrayList<Integer>();
//		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
//		for(int i=0; i<xdim;i++){
//			System.out.println("xDim "+i);
//			int xState = BDDWrapper.assign(bdd, i, xVars);
//			//actions=00 -- up
//			//x'=x-1 && y'=y
//			if(i>0){
//				
//				int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
//				
//				int trans = bdd.ref(bdd.and(xState,xPrime));
//				trans=bdd.andTo(trans, yTrans);
//				trans=bdd.andTo(trans, actions[0]);
//				
//				
//				bdd.deref(xPrime);
//				
////				TList.add(trans);
//				T=bdd.orTo(T, trans);
//				bdd.deref(trans);
//				
//				
//			}
//			//actions=01 -- down
//			if(i<xdim-1){
//				int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
//				int trans = bdd.ref(bdd.and(xState,xPrime));
//				trans=bdd.andTo(trans, yTrans);
//				trans=bdd.andTo(trans, actions[1]);
//			
//				bdd.deref(xPrime);
//				
////				TList.add(trans);
//				T=bdd.orTo(T, trans);
//				bdd.deref(trans);
//				
//				
//			}
//			bdd.deref(xState);
//		}
//		bdd.deref(yTrans);
//		
//		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
//		for(int j=0;j<ydim;j++){
//			System.out.println("yDim "+j);
//			int yState = BDDWrapper.assign(bdd, j, yVars);
//			//actions=10 -- left 
//			if(j>0){
//				
//				int yPrime = BDDWrapper.assign(bdd, j-1, yVarsPrime);
//				
//				int trans = bdd.ref(bdd.and(yState,yPrime));
//				trans=bdd.andTo(trans, xTrans);
//				trans=bdd.andTo(trans, actions[2]);
//				
//				
//				bdd.deref(yPrime);
//				
////				TList.add(trans);
//				T=bdd.orTo(T, trans);
//				bdd.deref(trans);
//				
//				
//			}
//			//actions=11 -- right
//			if(j<ydim-1){
//				int yPrime = BDDWrapper.assign(bdd, j+1, yVarsPrime);
//				int trans = bdd.ref(bdd.and(yState,yPrime));
//				trans=bdd.andTo(trans, xTrans);
//				trans=bdd.andTo(trans, actions[3]);
//				
//				bdd.deref(yPrime);
//				
////				TList.add(trans);
//				T=bdd.orTo(T, trans);
//				bdd.deref(trans);
//				
//				
//			}
//			bdd.deref(yState);
//		}
//		bdd.deref(xTrans);
		return T;
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
