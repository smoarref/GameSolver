package autonomousVehicle;

import java.util.ArrayList;

import org.omg.CORBA.portable.ValueOutputStream;

import game.BDDWrapper;
import specification.GridCell2D;
import jdd.bdd.BDD;
import game.Variable;
import specification.Agent2D;
import specification.AgentType;
import utils.UtilityFormulas;
import utils.UtilityMethods;

//initial step: we consider blocks where the streets are parallel or perpendicular to each other. 
//Four defined directions are north, south, left and right
//The head of the car point to a direction. 
//The direction of the car should match the direction of the road segment 



public class SimpleAutonomousVehicle extends Agent2D {
	
	
	Variable[] direction;

	public SimpleAutonomousVehicle(BDD bdd, String argName, AgentType argType,
			Variable[] argVars, Variable[] argActions,
			int argTrans, int argInit, Variable[] argXVars,
			Variable[] argYVars, Variable[] argDirection) {
		super(bdd, argName, argType, argVars, argActions, argTrans, argInit,
				argXVars, argYVars);
		direction = argDirection;
		
	}
	
//	public SimpleAutonomousVehicle(String argName, AgentType argType, Variable[] argActions, 
//			Variable[] argXVars, Variable[] argYVars, Variable[] argDirection){
//		super(argName, argType, null, argActions, -1, argInit,
//				argXVars, argYVars);
//		direction = argDirection;
//		Variable[] vars = Variable.unionVariables(xVars, yVars);
//		vars = Variable.unionVariables(vars, direction);
//	}
	
	public Variable[] getDirectionVars(){
		return direction;
	}
	
	public static SimpleAutonomousVehicle createSimpleAutonomousVehicle(BDD bdd, 
			String argName, AgentType argType,
			int xDim, int yDim,
			GridCell2D initGrid, Direction initDir){
		
		int numOfXbits = UtilityMethods.numOfBits(xDim);
		int numOfYbits = UtilityMethods.numOfBits(yDim);
		int numOfDirectionBits = UtilityMethods.numOfBits(Direction.values().length-1);
		int numOfActionBits = UtilityMethods.numOfBits(VehicleActions.values().length-1);
		
		Variable[] xVars = Variable.createVariables(bdd, numOfXbits, argName+"_X");
		Variable[] xPrimeVars = Variable.createPrimeVariables(bdd, xVars);
		Variable[] yVars = Variable.createVariables(bdd, numOfYbits, argName+"_Y");
		Variable[] yPrimeVars = Variable.createPrimeVariables(bdd, yVars);
		Variable[] directions = Variable.createVariables(bdd, numOfDirectionBits, argName+"_Dir");
		Variable[] directionsPrime = Variable.createPrimeVariables(bdd, directions);
		Variable[] actions = Variable.createVariables(bdd, numOfActionBits, argName+"_act");
		
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		vars = Variable.unionVariables(vars, directions);
		
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initGrid);
		int initDirection = BDDWrapper.assign(bdd, initDir.ordinal(), directions);
		init = BDDWrapper.andTo(bdd, init, initDirection);
		BDDWrapper.free(bdd,initDirection);
		
		
		SimpleAutonomousVehicle sav = new SimpleAutonomousVehicle(bdd, argName, argType, 
				vars, actions, -1, init, xVars, yVars, directions);
		
		int transitionRelation = VehicleTransitionRelationGenerator.generateAutonomousVehicleTranstionRelation(bdd, sav, xDim, yDim);
		
		sav.setTransitionRelation(transitionRelation);
		
		return sav;
	}
	
	//printing in a human readable format 
	public void print(){
		System.out.println("Printing agent "+this.getName()+" information");
		System.out.println("Type = "+this.getType());
		System.out.println("Variables");
		Variable.printVariables(variables);
		
		System.out.println("Actions");
		Variable.printVariables(actionVars);
		
		printInit();
		
		printTransitionRelation(getTransitionRelation());
	}
	
	public void printInit(){
		System.out.println("init");
		//this.getBDD().printSet(getInit());
		printStates(getInit());
		
	}
	
	public void printStates(int states){
		ArrayList<String> minterms= BDDWrapper.minterms(getBDD(), getInit(), getVariables());
		for(String minterm : minterms){
			int xValue = valueInMinterm(minterm, xVars, getVariables());
			int yValue = valueInMinterm(minterm, yVars, getVariables());
			int directionValue = valueInMinterm(minterm, direction, getVariables());
			Direction dir = Direction.getDirection(directionValue);
			
			System.out.println("x="+xValue+", y="+yValue+", direction="+dir.name());
		}
	}
	
	/**
	 * the integer value of a subset subsetVars of variables vars in the given minterm
	 * @param minterm
	 * @param vars
	 * @return
	 */
	public int valueInMinterm(String minterm, Variable[] subsetVars, Variable[] vars){
		String value = "";
		for(int i=0; i<subsetVars.length;i++){
			int index = variableIndex(subsetVars[i], vars);
			value+=minterm.charAt(index);
		}
		return Integer.parseInt(value, 2);
	}
	
	private int variableIndex(Variable var, Variable[] vars){
		for(int i=0; i<vars.length;i++){
			if(var == vars[i]) return i;
		}
		return -1;
	}
	
	public void printTransitionRelation(int transitionRelation){
		System.out.println("transition relation");
		
		
//		getBDD().printSet(getTransitionRelation());
		
		Variable[] transVars = Variable.unionVariables(getVariables(), getPrimeVariables());
		transVars = Variable.unionVariables(transVars, getActionVars());
		
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, getTransitionRelation(), transVars));
		
		ArrayList<String> minterms= BDDWrapper.minterms(getBDD(), transitionRelation, transVars);
		for(String minterm : minterms){
			int xValue = valueInMinterm(minterm, xVars, transVars);
			int yValue = valueInMinterm(minterm, yVars, transVars);
			int directionValue = valueInMinterm(minterm, direction, transVars);
			Direction dir = Direction.getDirection(directionValue);
			
			int actionValue = valueInMinterm(minterm, actionVars, transVars);
			VehicleActions act = VehicleActions.getVehicleAction(actionValue);
			
			int nextXValue = valueInMinterm(minterm, Variable.getPrimedCopy(xVars), transVars);
			int nextYValue = valueInMinterm(minterm, Variable.getPrimedCopy(yVars), transVars);
			int nextDirectionValue = valueInMinterm(minterm, Variable.getPrimedCopy(direction), transVars);
			Direction nextDir = Direction.getDirection(nextDirectionValue);
			
			System.out.println("x="+xValue+", y="+yValue+", direction="+dir.name()+", act = "+act.name()
					+": next : "+"x'="+nextXValue+", y'="+nextYValue+", direction'="+nextDir.name());
		}
	}
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000, 1000);
		int xDim = 1;
		int yDim = 1;
		GridCell2D initGrid = new GridCell2D(0, 0);
		Direction initDir = Direction.East;
		SimpleAutonomousVehicle sav = SimpleAutonomousVehicle.createSimpleAutonomousVehicle(bdd, "sav1", AgentType.Controllable, xDim, yDim, initGrid, initDir);
		sav.print();
		
//		for(Direction dir : Direction.values()){
//			System.out.println(dir.name() +" -> "+dir.ordinal());
//		}

		
	}

}
