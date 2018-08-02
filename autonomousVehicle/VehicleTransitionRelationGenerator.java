package autonomousVehicle;

import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import game.BDDWrapper;
import game.Variable;
import jdd.bdd.BDD;

public class VehicleTransitionRelationGenerator {
	
	public static int generateAutonomousVehicleTranstionRelation(BDD bdd, SimpleAutonomousVehicle sav, int xDim, int yDim){
		Variable[] directions = sav.getDirectionVars();
		
		
		Variable[] xVars = sav.getXVars();
		
		Variable[] yVars = sav.getYVars();
		
		int[] actions = sav.getActions();
		
		
		//for each action, define the transition relation over x,y and direction and their primed copies
		
		//direction changes with actions
		int directionsTrans = directionTransitions(bdd, actions, directions);
		
		int positionsTrans = bdd.ref(bdd.getZero());
		
		int moveNorth = UtilityTransitionRelations.upTransitions(bdd, 0, xDim, xVars, yVars);
		int moveSouth = UtilityTransitionRelations.downTransitions(bdd, 0, xDim, xVars, yVars);
		int moveWest = UtilityTransitionRelations.leftTransitions(bdd, 0, yDim, xVars, yVars);
		int moveEast = UtilityTransitionRelations.rightTransitions(bdd, 0, yDim, xVars, yVars);
		
		//direction: north=0, south=1, west=2, east=3
		//actions[0] : stop
		int stopPositionTrans = UtilityTransitionRelations.halt(bdd, sav.getVariables(), actions[0]);
		positionsTrans = bdd.orTo(positionsTrans, stopPositionTrans);
		BDDWrapper.free(bdd, stopPositionTrans);
		
		int [] dirs = UtilityMethods.enumerate(bdd, Variable.getBDDVars(directions));
		
		//actions[1] : backup,
		//direction=north & backup -> x'=x-1 & y'=y (down)
		int northDown = BDDWrapper.and(bdd, dirs[0], moveSouth);
		
		
		//direction=south & backup -> up
		int southUp = BDDWrapper.and(bdd, dirs[1], moveNorth);
		
		//direction=west & backup -> right
		int westRight = BDDWrapper.and(bdd, dirs[2], moveEast);
		
		//direction=east & backup -> left
		int eastLeft = BDDWrapper.and(bdd, dirs[3], moveWest);
		
		int backUpPositionTrans = BDDWrapper.or(bdd, northDown, southUp);
		backUpPositionTrans = BDDWrapper.orTo(bdd, backUpPositionTrans, westRight);
		backUpPositionTrans = BDDWrapper.orTo(bdd, backUpPositionTrans, eastLeft);
		backUpPositionTrans = BDDWrapper.andTo(bdd, backUpPositionTrans, actions[1]);
		
		bdd.deref(northDown);
		bdd.deref(southUp);
		bdd.deref(westRight);
		bdd.deref(eastLeft);
		

		positionsTrans = BDDWrapper.orTo(bdd, positionsTrans, backUpPositionTrans);
		bdd.deref(backUpPositionTrans);
		
		//actions[2] : moveForward,
		//direction=north & forward -> x'=x+1 & y'=y (up)
		int northUp = BDDWrapper.and(bdd, dirs[0], moveNorth);
				
		//direction=south & forward -> down
		int southDown = BDDWrapper.and(bdd, dirs[1], moveSouth);
				
		//direction=west & forward -> left
		int westLeft = BDDWrapper.and(bdd, dirs[2], moveWest);
				
		//direction=east & forward -> right
		int eastRight = BDDWrapper.and(bdd, dirs[3], moveEast);
				
		int forwardPositionTrans = BDDWrapper.or(bdd, northUp, southDown);
		forwardPositionTrans = BDDWrapper.orTo(bdd, forwardPositionTrans, westLeft);
		forwardPositionTrans = BDDWrapper.orTo(bdd, forwardPositionTrans, eastRight);
		forwardPositionTrans = BDDWrapper.andTo(bdd, forwardPositionTrans, actions[2]);
				
		bdd.deref(northUp);
		bdd.deref(southDown);
		bdd.deref(westLeft);
		bdd.deref(eastRight);
				
		positionsTrans = BDDWrapper.orTo(bdd, positionsTrans, forwardPositionTrans);
		bdd.deref(forwardPositionTrans);
		
		//actions[3] : MoveFastForward,
		int progressValue=2;
		int move2StepsNorth = UtilityTransitionRelations.position2DTransition(bdd, xVars, yVars, xDim, -1*progressValue);
		int move2StepsSouth = UtilityTransitionRelations.position2DTransition(bdd, xVars, yVars, xDim, progressValue);
		int move2StepsWest = UtilityTransitionRelations.position2DTransition(bdd, yVars, xVars, yDim, -1*progressValue);
		int move2StepsEast = UtilityTransitionRelations.position2DTransition(bdd, yVars, xVars, yDim, progressValue);
		
		//direction=north & fastforward -> x'=x+2 & y'=y (up)
		int north2Up = BDDWrapper.and(bdd, dirs[0], move2StepsNorth);
						
		//direction=south & fastforward -> down
		int south2Down = BDDWrapper.and(bdd, dirs[1], move2StepsSouth);
						
		//direction=west & fastforward -> left
		int west2Left = BDDWrapper.and(bdd, dirs[2], move2StepsWest);
						
		//direction=east & fastforward -> right
		int east2Right = BDDWrapper.and(bdd, dirs[3], move2StepsEast);
		
		int fastForwardPositionTrans = BDDWrapper.or(bdd, north2Up, south2Down);
		fastForwardPositionTrans = BDDWrapper.orTo(bdd, fastForwardPositionTrans, west2Left);
		fastForwardPositionTrans = BDDWrapper.orTo(bdd, fastForwardPositionTrans, east2Right);
		fastForwardPositionTrans = BDDWrapper.andTo(bdd, fastForwardPositionTrans, actions[3]);
				
		bdd.deref(north2Up);
		bdd.deref(south2Down);
		bdd.deref(west2Left);
		bdd.deref(east2Right);
				
		positionsTrans = BDDWrapper.orTo(bdd, positionsTrans, fastForwardPositionTrans);
		bdd.deref(fastForwardPositionTrans);
		
		//actions[4] : turnLeft,
		//direction=north & turnLeft -> x''=x & y'=y-1 (left)
		int northLeft = BDDWrapper.and(bdd, dirs[0], moveWest);
						
		//direction=south & turnLeft -> right
		int southRight = BDDWrapper.and(bdd, dirs[1], moveEast);
						
		//direction=west & turnLeft -> down
		int westDown = BDDWrapper.and(bdd, dirs[2], moveSouth);
						
		//direction=east & turnLeft -> up
		int eastUp = BDDWrapper.and(bdd, dirs[3], moveNorth);
		
		int turnLeftPositionTrans = BDDWrapper.or(bdd, northLeft, southRight);
		turnLeftPositionTrans = BDDWrapper.orTo(bdd, turnLeftPositionTrans, westDown);
		turnLeftPositionTrans = BDDWrapper.orTo(bdd, turnLeftPositionTrans, eastUp);
		turnLeftPositionTrans = BDDWrapper.andTo(bdd, turnLeftPositionTrans, actions[4]);
				
		bdd.deref(northLeft);
		bdd.deref(southRight);
		bdd.deref(westDown);
		bdd.deref(eastUp);
				
		positionsTrans = BDDWrapper.orTo(bdd, positionsTrans, turnLeftPositionTrans);
		bdd.deref(turnLeftPositionTrans);
				
		//actions[5] : turnRight
		//direction=north & turnRight -> x'=x & y'=y-1 (right)
		int northRight = BDDWrapper.and(bdd, dirs[0], moveEast);
								
		//direction=south & turnRight -> left
		int southLeft = BDDWrapper.and(bdd, dirs[1], moveWest);
								
		//direction=west & turnRight -> up
		int westUp = BDDWrapper.and(bdd, dirs[2], moveNorth);
								
		//direction=east & turnRight -> down
		int eastDown = BDDWrapper.and(bdd, dirs[3], moveSouth);
				
		int turnRightPositionTrans = BDDWrapper.or(bdd, northRight, southLeft);
		turnRightPositionTrans = BDDWrapper.orTo(bdd, turnRightPositionTrans, westUp);
		turnRightPositionTrans = BDDWrapper.orTo(bdd, turnRightPositionTrans, eastDown);
		turnRightPositionTrans = BDDWrapper.andTo(bdd, turnRightPositionTrans, actions[5]);
						
		bdd.deref(northRight);
		bdd.deref(southLeft);
		bdd.deref(westUp);
		bdd.deref(eastDown);
						
		positionsTrans = BDDWrapper.orTo(bdd, positionsTrans, turnRightPositionTrans);
		bdd.deref(turnRightPositionTrans);
		
		//free moves
		BDDWrapper.free(bdd, moveNorth);
		BDDWrapper.free(bdd, moveSouth);
		BDDWrapper.free(bdd, moveWest);
		BDDWrapper.free(bdd, moveEast);
		
		BDDWrapper.free(bdd, move2StepsNorth);
		BDDWrapper.free(bdd, move2StepsSouth);
		BDDWrapper.free(bdd, move2StepsWest);
		BDDWrapper.free(bdd, move2StepsEast);
		
		//complete transition relation
		int T= bdd.ref(bdd.and(directionsTrans, positionsTrans));
		BDDWrapper.free(bdd, positionsTrans);
		BDDWrapper.free(bdd, directionsTrans);
		return T;
	}
	
	public static int directionTransitions(BDD bdd, int[] actions, Variable[] directions){
		Variable[] directionsPrime = Variable.getPrimedCopy(directions);
		
		int directionTrans = bdd.ref(bdd.getZero());
		
		//if the actions are stop=0, backup=1, moveforward=2, moveFastForward=3, then direction will stay the same
		int sameActs = bdd.ref(bdd.or(actions[0],actions[1]));
		sameActs=bdd.orTo(sameActs, actions[2]);
		sameActs=bdd.orTo(sameActs, actions[3]);
		int sameDirection = BDDWrapper.same(bdd, directions, directionsPrime);
		int same = bdd.ref(bdd.and(sameActs, sameDirection));
		bdd.deref(sameActs);
		bdd.deref(sameDirection);
		directionTrans=bdd.orTo(directionTrans, same);
		bdd.deref(same);
		
		//direction: north=0, south=1, west=2, east=3
		
		//otherwise direction changes
		//trun left
		int act = actions[4];
		
		//north -> west
		int northToWest = UtilityTransitionRelations.transition(bdd, directions, 0, directionsPrime, 2);
		
		//west -> south
		int westToSouth = UtilityTransitionRelations.transition(bdd, directions, 2, directionsPrime, 1);
		
		//south -> east
		int southToEast = UtilityTransitionRelations.transition(bdd, directions, 1, directionsPrime, 3);
		
		//east -> north
		int eastToNorth = UtilityTransitionRelations.transition(bdd, directions, 3, directionsPrime, 0);
		
		int turnLeftDirTrans = bdd.ref(bdd.or(northToWest, westToSouth));
		turnLeftDirTrans = bdd.orTo(turnLeftDirTrans, southToEast);
		turnLeftDirTrans = bdd.orTo(turnLeftDirTrans, eastToNorth);
		turnLeftDirTrans = bdd.andTo(turnLeftDirTrans, act);
		directionTrans= bdd.orTo(directionTrans, turnLeftDirTrans);
		BDDWrapper.free(bdd, northToWest);
		BDDWrapper.free(bdd, westToSouth);
		BDDWrapper.free(bdd, southToEast);
		BDDWrapper.free(bdd, eastToNorth);
		
		//turn right 
		act=actions[5];
		
		//north -> east
		int northToEast = UtilityTransitionRelations.transition(bdd, directions, 0, directionsPrime, 3);
		
		//east -> south
		int eastToSouth = UtilityTransitionRelations.transition(bdd, directions, 3, directionsPrime, 1);
		
		//south -> west 
		int southToWest = UtilityTransitionRelations.transition(bdd, directions, 1, directionsPrime, 2);
		
		//west -> north
		int westToNorth = UtilityTransitionRelations.transition(bdd, directions, 2, directionsPrime, 0);
		
		int turnRightDirTrans = bdd.ref(bdd.or(northToEast, eastToSouth));
		turnRightDirTrans = bdd.orTo(turnRightDirTrans, southToWest);
		turnRightDirTrans = bdd.orTo(turnRightDirTrans, westToNorth);
		turnRightDirTrans = bdd.andTo(turnRightDirTrans, act);
		directionTrans= bdd.orTo(directionTrans, turnRightDirTrans);
		BDDWrapper.free(bdd, northToEast);
		BDDWrapper.free(bdd, eastToSouth);
		BDDWrapper.free(bdd, southToWest);
		BDDWrapper.free(bdd, westToNorth);
		
		return directionTrans;
	}
}
