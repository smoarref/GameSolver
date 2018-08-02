package testsAndCaseStudies;

import java.awt.EventQueue;
import java.util.ArrayList;

import autonomousVehicle.Direction;
import autonomousVehicle.SimpleAutonomousVehicle;
import game.BDDWrapper;
import game.GameStructure;
import game.GameSolution;
import game.GameSolver;
import specification.GridCell2D;
import game.Variable;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import specification.Agent2D;
import specification.AgentType;
import utils.UtilityFormulas;

/**
 * The first attemt to model a very simple intersection where the ego car wants to cross an intersection
 * @author moarref
 *
 */
public class IntersectionCaseStudy1 {
	
	BDD bdd;
	ArrayList<Agent2D> agents;
	int xDim;
	int yDim;
	GameStructure solution;
	ArrayList<GridCell2D> staticObstacles;
	
	public IntersectionCaseStudy1(int dim){
		bdd = new BDD(10000, 1000);
		
		//user input
		
		//roadnetwork
		//dimension of the board
		xDim = dim;
		yDim = dim;
		
		//location of the static obstacles
		staticObstacles = new ArrayList<GridCell2D>();
//		for(int i=0; i<=dim ; i++){
//			for(int j=0; j<=dim; j++){
//				if(i !=dim/2 && j != dim/2 ){
//					staticObstacles.add(new GridCell2D(i, j));
//				}
//			}
//		}
		
		//init location for cars
		GridCell2D egoCarInit = new GridCell2D(dim/2, 0);
		GridCell2D environmentCar1Init = new GridCell2D(dim/2, 1);
		
		
		//initiate egoCar agent
		
		
		
		
//		Agent2D egoCar = SimpleVehicle.createSimpleVehicle(bdd, dim, dim, egoCarInit, staticObstacles, 
//				"egoCar", AgentType.Controllable); 
		
		SimpleAutonomousVehicle egoCar = SimpleAutonomousVehicle.createSimpleAutonomousVehicle(bdd, "egoCar", AgentType.Controllable,
				dim, dim, egoCarInit, Direction.East);

		
		//initiate environmentCar1 agent
		
//		Agent2D environmentCar1 = SimpleVehicle.createSimpleVehicle(bdd, dim, dim, environmentCar1Init, staticObstacles, 
//				"envCar1", AgentType.Uncontrollable);
		
		SimpleAutonomousVehicle environmentCar1 = SimpleAutonomousVehicle.createSimpleAutonomousVehicle(bdd, "envCar1", 
				AgentType.Uncontrollable, dim, dim, environmentCar1Init, Direction.East);
		
		int noCollisionWithStaticObs = UtilityFormulas.noCollisionWithStaticObstacles(bdd,
													Variable.getPrimedCopy(environmentCar1.getXVars()), 
													Variable.getPrimedCopy(environmentCar1.getYVars()), 
													staticObstacles);
		
		int newT = BDDWrapper.andFree(bdd, environmentCar1.getTransitionRelation(), noCollisionWithStaticObs);
		environmentCar1.setTransitionRelation(newT);
		
		System.out.println("agents created");
		
		//define objectives
//		int sameX = BDDWrapper.same(bdd, egoCar.getXVars(), environmentCar1.getXVars());
//		int sameY = BDDWrapper.same(bdd, egoCar.getYVars(), environmentCar1.getYVars());
//		int sameLoc = BDDWrapper.andFree(bdd, sameX, sameY);
//		int obj = BDDWrapper.not(bdd, sameLoc);
		int obj=UtilityFormulas.noCollisionObjective(bdd, egoCar.getXVars(), egoCar.getYVars(), 
				environmentCar1.getXVars(), environmentCar1.getYVars());
		
		int noCollisionWithStaticObsObj = UtilityFormulas.noCollisionWithStaticObstacles(bdd, egoCar.getXVars(),
				egoCar.getYVars(), staticObstacles);
		obj = bdd.andTo(obj, noCollisionWithStaticObsObj);
		
		System.out.println("objective created");
		
		//form games and synthesize
		Variable[] gameVars = Variable.unionVariables(egoCar.getVariables(), environmentCar1.getVariables());
		Variable[] gamePrimedVars = Variable.getPrimedCopy(gameVars);
		int init = BDDWrapper.and(bdd, egoCar.getInit(), environmentCar1.getInit());
		int T_env = BDDWrapper.and(bdd, environmentCar1.getTransitionRelation(), egoCar.getSame());
		int T_sys = BDDWrapper.and(bdd, egoCar.getTransitionRelation(), environmentCar1.getSame());
		GameStructure g = new GameStructure(bdd, gameVars , gamePrimedVars, init, T_env, T_sys, environmentCar1.getActionVars(), egoCar.getActionVars());
		
		int targetStates = BDDWrapper.assign(bdd, dim, egoCar.getXVars());
		int[] xList = Variable.getBDDVars(egoCar.getXVars());
		int[] xPrimeList = Variable.getBDDVars(Variable.getPrimedCopy(egoCar.getXVars()));
		Permutation p = bdd.createPermutation(xList, xPrimeList);
		int targetStatesPrime = BDDWrapper.replace(bdd, targetStates, p);
		bdd.printSet(targetStates);
		bdd.printSet(targetStatesPrime);
		GameStructure gReachable = g.gameWithBoundedReachabilityObjective(8, "reachabilityGame", targetStates, targetStatesPrime);
		
		System.out.println("game created");
		
		GameSolver gs = new GameSolver(gReachable, obj, bdd);
		GameSolution sol = gs.solve();
		
		sol.print();
		
		solution = sol.strategyOfTheWinner();
		
		System.out.println("Game solved");
		
		System.out.println("model checking safety, result is");
		System.out.println(solution.safetyModelCheck(obj));
		
//		environmentCar1.printTransitionRelation(sol.strategyOfTheWinner().getEnvironmentTransitionRelation());
//		egoCar.printTransitionRelation(sol.strategyOfTheWinner().getSystemTransitionRelation());
		
		agents = new ArrayList<Agent2D>();
		agents.add(egoCar);
		agents.add(environmentCar1);
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
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
		
	}
	
	public static void test(){
		BDD bdd = new BDD(10000, 1000);
		
		//user input
		
		//roadnetwork
		//dimension of the board
		int dim =8;
		
		
		//location of the static obstacles
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		for(int i=0; i<dim ; i++){
			for(int j=0; j<dim; j++){
				if(i !=dim/2 && j != dim/2 ){
					staticObstacles.add(new GridCell2D(i, j));
				}
			}
		}
		
		//init location for cars
		GridCell2D egoCarInit = new GridCell2D(dim/2, 0);
		GridCell2D environmentCar1Init = new GridCell2D(dim/2, 1);
		
		
		//initiate egoCar agent
		
		
		
		
		Agent2D egoCar = SimpleVehicle.createSimpleVehicle(bdd, dim, dim, egoCarInit, staticObstacles, 
				"egoCar", AgentType.Controllable); 
		
		
		//initiate environmentCar1 agent
		
		Agent2D environmentCar1 = SimpleVehicle.createSimpleVehicle(bdd, dim, dim, environmentCar1Init, staticObstacles, 
				"envCar1", AgentType.Uncontrollable);
		
		int noCollisionWithStaticObs = UtilityFormulas.noCollisionWithStaticObstacles(bdd,
													Variable.getPrimedCopy(environmentCar1.getXVars()), 
													Variable.getPrimedCopy(environmentCar1.getYVars()), 
													staticObstacles);
		
		int newT = BDDWrapper.andFree(bdd, environmentCar1.getTransitionRelation(), noCollisionWithStaticObs);
		environmentCar1.setTransitionRelation(newT);
		
		System.out.println("agents created");
		
		//define objectives
//		int sameX = BDDWrapper.same(bdd, egoCar.getXVars(), environmentCar1.getXVars());
//		int sameY = BDDWrapper.same(bdd, egoCar.getYVars(), environmentCar1.getYVars());
//		int sameLoc = BDDWrapper.andFree(bdd, sameX, sameY);
//		int obj = BDDWrapper.not(bdd, sameLoc);
		int obj=bdd.getOne();
		
		int noCollisionWithStaticObsObj = UtilityFormulas.noCollisionWithStaticObstacles(bdd, egoCar.getXVars(),
				egoCar.getYVars(), staticObstacles);
		obj = noCollisionWithStaticObsObj;
		
		System.out.println("objective created");
		
		//form games and synthesize
		Variable[] gameVars = Variable.unionVariables(egoCar.getVariables(), environmentCar1.getVariables());
		Variable[] gamePrimedVars = Variable.getPrimedCopy(gameVars);
		int init = BDDWrapper.and(bdd, egoCar.getInit(), environmentCar1.getInit());
		int T_env = BDDWrapper.and(bdd, environmentCar1.getTransitionRelation(), egoCar.getSame());
		int T_sys = BDDWrapper.and(bdd, egoCar.getTransitionRelation(), environmentCar1.getSame());
		GameStructure g = new GameStructure(bdd, gameVars , gamePrimedVars, init, T_env, T_sys, environmentCar1.getActionVars(), egoCar.getActionVars());
		
		System.out.println("game created");
		
		GameSolver gs = new GameSolver(g, obj, bdd);
		GameSolution sol = gs.solve();
		
		sol.print();
		
		System.out.println("Game solved");
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.add(egoCar);
		agents.add(environmentCar1);
		
		
		//visualize the strategy
	}
	
	

}
