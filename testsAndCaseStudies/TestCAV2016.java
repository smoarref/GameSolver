package testsAndCaseStudies;

import game.BDDWrapper;
import game.DecoupledMultiAgentSimulator2D;
import game.GameSolution;
import game.GameSolver;
import game.GameSolvingMethod;
import game.GameStructure;
import game.Observation;
import game.Player;
import game.Simulator2D;
import game.TurnBasedPartiallyObservableGameStructure;
import game.Variable;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.FileOps;
import utils.UtilityFormulas;
import utils.UtilityMethods;
import utils.UtilityTransitionRelations;


// TODO: handle special case where objective is true, or does not involve any of the controlled agents
// TODO: Generalize to the case where reachability objectives can refer to multiple agents
// TODO: what if the objective is not realizable
public class TestCAV2016 {
	
	static ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
	static int currentIndex = -1;
	
	public static void main(String[] args){
		
		//define agents
		//define objectives
		
		//testing different methods (central, compositional, old-compositional) with respect to 
		// 1) Different objectives
		//		a) Collision avoidance (dynamic and static)
		//		b) Formation control
		//		c) Bounded reachability 
		// 2) Different number of agents
		// 		a) Controllable
		//		b) Uncontrollable
		// 3) different dimensions for the grid-world
		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(baos);
//		PrintStream old = System.out;
//		System.setOut(ps);
//		
//		for(int dim = 2048*2; dim < 2048*8; dim=dim*2){
//			
//			for(int j=1; j<4; j++){
//				System.out.println("******************************************************************************************************************");
//				System.out.println("**************************************************New Experiment**************************************************");
//				System.out.println("experiment started at "+new Date().toString());
//				System.out.println("dimension of the gird world is "+dim);
//				System.out.println("synthesis method is ");
//				switch(j){
//					case 1 : System.out.println("Compositional");
//							break;
//					case 2 : System.out.println("Compositional - OLD");
//							break;
//					case 3 : System.out.println("Central");
//							break;
////					case 4 : System.out.println("Counter-Strategy-Guided compositional");
////							break;
////					default : System.out.println("Shouldn't reach here!");
//				}
//				BDD bdd = new BDD(10000,1000);
//				runExperiment(bdd, dim, j);
//				bdd.cleanup();
//				System.out.flush();
//			    String result=baos.toString();
//			    //System.out.println(satisfying);
//			    System.out.println("experiment finished at "+new Date().toString());
//			    System.out.println("******************************************************************************************************************");
//			    FileOps.write(result, "experimentResults.txt");
//			    
//			}
//		}
//		
//		System.setOut(old);
		
		//testing each part
//		BDD bdd = new BDD(10000, 1000);
//		ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
//		try{
////			runExperiment(bdd, 4, GameSolvingMethod.Central);
//			runExperiment(bdd, 1, 2, experimentObjectiveTypes.get(2), 5, GameSolvingMethod.Compositional_StrategyPruning);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
//		testCaseStudyCav16();
		
		//single test
//		BDD bdd = new BDD(10000, 1000);
//		int dim = 5;
//		ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
//		try{
//			runExperiment(bdd, 1, 1, experimentObjectiveTypes.get(0), dim, GameSolvingMethod.Central);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
		//testing partially observable games
//		BDD bdd = new BDD(10000, 1000);
//		int dim = 4;
//		ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
//		try{
//			runExperiment_partiallyObservable(bdd, 1, 2, experimentObjectiveTypes.get(1), dim, 
//					GameSolvingMethod.Compositional_StrategyPruning);
//		}catch(Exception e){
//			e.printStackTrace();
//		}
		
		//experimenting with partial observation
//		testCaseStudyCav16_partiallyObservable();
		
		//testing when there is no uncontrolled agent
//		testGameStructuresWithNoUncontrolledAgent();
		
		//test static object
//		ArrayList<String> newExp = new ArrayList<String>();
//		newExp.add("hello");
//		results.add(newExp);
//		ArrayList<String> get = results.get(currentIndex);
//		currentIndex++;
//		System.out.println(get.get(0));
//		System.out.println("current index "+currentIndex);
		
		//testing optimized algorithms 
//		testCaseStudyCav16_optimized();
		
		//testing partial observation with no uncontrolled
//		testGameStructuresWithNoUncontrolledAgent_partiallyObservable();
		
		testCaseStudyCav16_partiallyObservable_optimized();
	}
	
	public static void testCaseStudyCav16(){
		//testing different methods (central, compositional, old-compositional) with respect to 
				// 1) Different objectives
				//		a) Collision avoidance (dynamic and static)
				//		b) Formation control
				//		c) Bounded reachability 
				// 2) Different number of agents
				// 		a) Controllable
				//		b) Uncontrollable
				// 3) different dimensions for the grid-world
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				PrintStream old = System.out;
				System.setOut(ps);
				
				//define the maximum number of agents for the experiments
				int maxNumOfUncontrolledAgents = 2;
				int maxNumOfControlledAgents = 3;
				//define maximum dimension to be considered
				int maxDim = 16;
				
				//define different objective types for different experiments
				ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
				
				//different number of uncontrolled agents
				for(int numOfUncontrolledAgents = 2; numOfUncontrolledAgents<= maxNumOfUncontrolledAgents; numOfUncontrolledAgents++){
					//different number of controlled agents
					for(int numOfControlledAgents = 2; numOfControlledAgents<= maxNumOfControlledAgents; numOfControlledAgents++){
						//different objectives
						//TODO
						for(int i =0; i<experimentObjectiveTypes.size(); i++ ){
							Boolean[] currentObjectiveForExperminet = experimentObjectiveTypes.get(i);
							//different dimensions
							for(int dim = 4; dim < maxDim; dim=dim*2){
								
								for(int j=1; j<3; j++){
									
//									//filter already tested or when you know it is not going to scale 
//									if(numOfControlledAgents == 2){
//										continue;
//									}
//									
//									// && (i!=3 || dim>32)
//									if(numOfControlledAgents == 3){
//										continue;
//									}
//								
//									if(numOfControlledAgents == 4 && i==0){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && i==1){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && dim>8){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && i<=2){
//										continue;
//									}
									
									System.out.println();
									System.out.println("******************************************************************************************************************");
									System.out.println("**************************************************New Experiment**************************************************");		
									System.out.println("experiment started at "+new Date().toString());
									System.out.println("number of uncontrolled agents "+numOfUncontrolledAgents);
									System.out.println("number of controlled agents "+numOfControlledAgents);
									printObjectiveTypeForExperiment(currentObjectiveForExperminet);
									System.out.println("dimension of the gird world is "+dim);
									System.out.println("synthesis method is ");
									GameSolvingMethod method = GameSolvingMethod.Compositional_StrategyPruning;
									switch(j){
										case 1 : System.out.println("Compositional");
													method = GameSolvingMethod.Compositional_StrategyPruning;
												break;
										case 2 : System.out.println("Central");
												method = GameSolvingMethod.Central;
												break;
										case 3 : System.out.println("Compositional - OLD");
												method = GameSolvingMethod.Compositional_OLD;
												break;
//										case 4 : System.out.println("Counter-Strategy-Guided compositional");
//												break;
//										default : System.out.println("Shouldn't reach here!");
									}
									BDD bdd = new BDD(10000,1000);
									try{
										runExperiment(bdd, numOfUncontrolledAgents, numOfControlledAgents, currentObjectiveForExperminet, dim, method);
									}catch(Exception e){
										e.printStackTrace();
									}
									bdd.cleanup();
									System.out.flush();
								    String result=baos.toString();
								    //System.out.println(satisfying);
								    System.out.println("experiment finished at "+new Date().toString());
								    System.out.println("******************************************************************************************************************");
								    FileOps.write(result, "experimentResults.txt");
								    
								}
							}
						}
						
					}
				}
				
				
				
				System.setOut(old);
	}
	
	public static void testCaseStudyCav16_optimized(){
		//testing different methods (central, compositional, old-compositional) with respect to 
				// 1) Different objectives
				//		a) Collision avoidance (dynamic and static)
				//		b) Formation control
				//		c) Bounded reachability 
				// 2) Different number of agents
				// 		a) Controllable
				//		b) Uncontrollable
				// 3) different dimensions for the grid-world
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				PrintStream old = System.out;
				System.setOut(ps);
				
				//define the maximum number of agents for the experiments
				int maxNumOfUncontrolledAgents = 2;
				int maxNumOfControlledAgents = 3;
				//define maximum dimension to be considered
				int maxDim = 32;
				int minDim = 4;
				
				//define different objective types for different experiments
				ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
				
				//different number of uncontrolled agents
				for(int numOfUncontrolledAgents = 2; numOfUncontrolledAgents<= maxNumOfUncontrolledAgents; numOfUncontrolledAgents++){
					//different number of controlled agents
					for(int numOfControlledAgents = 3; numOfControlledAgents<= maxNumOfControlledAgents; numOfControlledAgents++){
						//different objectives
						//TODO
						for(int i =0; i<experimentObjectiveTypes.size(); i++ ){
							Boolean[] currentObjectiveForExperminet = experimentObjectiveTypes.get(i);
							//different dimensions
							for(int dim = minDim; dim < maxDim; dim=dim*2){
								
								for(int j=1; j<3; j++){
									
									if(j!=2){
										continue;
									}
									
									if(i<=2){
										continue;
									}
									
									
									//filter already tested or when you know it is not going to scale 
									
									if(numOfUncontrolledAgents ==1 && numOfControlledAgents==2){
										if(i>=2){
											if(dim>32){
												continue;
											}
										}
									}
									
									if(numOfUncontrolledAgents ==1 && numOfControlledAgents==3){
										if(i<=1 && dim>32 ){
											continue;
										}
										
										if(i>=2 && dim>8){
											continue;
										}
									}
									
									if(numOfUncontrolledAgents ==1 && numOfControlledAgents==4){
										if(i<=1 && dim>8 ){
											continue;
										}
										
										if(i>=2 && dim>4){
											continue;
										}
									}
									
									if(numOfUncontrolledAgents ==2 && numOfControlledAgents>=2){
										if(dim>8){
											continue;
										}
									}
									
//									if(numOfControlledAgents == 2){
//										continue;
//									}
//									
//									// && (i!=3 || dim>32)
//									if(numOfControlledAgents == 3){
//										continue;
//									}
//								
//									if(numOfControlledAgents == 4 && i==0){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && i==1){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && dim>8){
//										continue;
//									}
//									
//									if(numOfControlledAgents == 4 && i<=2){
//										continue;
//									}
									
									System.out.println();
									System.out.println("******************************************************************************************************************");
									System.out.println("**************************************************New Experiment**************************************************");
									System.out.println("experiment started at "+new Date().toString());
									System.out.println("number of uncontrolled agents "+numOfUncontrolledAgents);
									System.out.println("number of controlled agents "+numOfControlledAgents);
									printObjectiveTypeForExperiment(currentObjectiveForExperminet);
									System.out.println("dimension of the gird world is "+dim);
									System.out.println("synthesis method is ");
									GameSolvingMethod method = GameSolvingMethod.Compositional_StrategyPruning;
									switch(j){
										case 1 : System.out.println("Compositional");
													method = GameSolvingMethod.Compositional_StrategyPruning;
												break;
										case 2 : System.out.println("Central");
												method = GameSolvingMethod.Central;
												break;
										case 3 : System.out.println("Compositional - OLD");
												method = GameSolvingMethod.Compositional_OLD;
												break;
//										case 4 : System.out.println("Counter-Strategy-Guided compositional");
//												break;
//										default : System.out.println("Shouldn't reach here!");
									}
									
									//prepare the results table
									ArrayList<String> newExperiment = new ArrayList<String>();
									results.add(newExperiment);
									currentIndex++;
									newExperiment.add(""+numOfUncontrolledAgents);
									newExperiment.add(""+numOfControlledAgents);
									
									
									BDD bdd = new BDD(10000,1000);
									try{
										runExperiment_optimized(bdd, numOfUncontrolledAgents, numOfControlledAgents, currentObjectiveForExperminet, dim, method);
									}catch(Exception e){
										e.printStackTrace();
									}
									bdd.cleanup();
									System.out.flush();
								    String result=baos.toString();
								    //System.out.println(satisfying);
								    System.out.println("experiment finished at "+new Date().toString());
								    System.out.println("******************************************************************************************************************");
								    FileOps.write(result, "experimentResults.txt");
								    
								    //write structured results in a file
								    writeStructuredResultsToFile("structuredResults.txt");
								}
							}
						}
						
					}
				}
				
				
				
				System.setOut(old);
	}
	
	public static void testCaseStudyCav16_partiallyObservable(){
		//testing different methods (central, compositional, old-compositional) with respect to 
				// 1) Different objectives
				//		a) Collision avoidance (dynamic and static)
				//		b) Formation control
				//		c) Bounded reachability 
				// 2) Different number of agents
				// 		a) Controllable
				//		b) Uncontrollable
				// 3) different dimensions for the grid-world
				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
				
		//define the maximum number of agents for the experiments
		int maxNumOfUncontrolledAgents = 1;
		int maxNumOfControlledAgents = 3;
		//define maximum dimension to be considered
		int maxDim = 16;
				
		//define different objective types for different experiments
		ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
				
		//different number of uncontrolled agents
		for(int numOfUncontrolledAgents = 1; numOfUncontrolledAgents<= maxNumOfUncontrolledAgents; numOfUncontrolledAgents++){
		//different number of controlled agents
			for(int numOfControlledAgents = 1; numOfControlledAgents<= maxNumOfControlledAgents; numOfControlledAgents++){
				//different objectives
				//TODO
				for(int i =0; i<experimentObjectiveTypes.size(); i++ ){
					Boolean[] currentObjectiveForExperminet = experimentObjectiveTypes.get(i);
					//different dimensions
					for(int dim = 4; dim < maxDim; dim=dim*2){
								
						for(int j=1; j<3; j++){
							
							if(numOfControlledAgents <=2){
								continue;
							}
							
							if(i==0){
								continue;
							}
									
							System.out.println();
							System.out.println("******************************************************************************************************************");
							System.out.println("**************************************************New Experiment**************************************************");
							System.out.println("experiment started at "+new Date().toString());
							System.out.println("number of uncontrolled agents "+numOfUncontrolledAgents);
							System.out.println("number of controlled agents "+numOfControlledAgents);
							printObjectiveTypeForExperiment(currentObjectiveForExperminet);
							System.out.println("dimension of the gird world is "+dim);
							System.out.println("synthesis method is ");
							GameSolvingMethod method = GameSolvingMethod.Compositional_StrategyPruning;
							switch(j){
								case 1 : System.out.println("Compositional");
									method = GameSolvingMethod.Compositional_StrategyPruning;
									break;
								case 2 : System.out.println("Central");
									method = GameSolvingMethod.Central;
									break;
								case 3 : System.out.println("Compositional - OLD");
									method = GameSolvingMethod.Compositional_OLD;
									break;
//								case 4 : System.out.println("Counter-Strategy-Guided compositional");
//									break;
//									default : System.out.println("Shouldn't reach here!");
							}
							BDD bdd = new BDD(10000,1000);
							try{
								runExperiment_partiallyObservable(bdd, numOfUncontrolledAgents, numOfControlledAgents, currentObjectiveForExperminet, dim, method);
							}catch(Exception e){
								e.printStackTrace();
							}
							bdd.cleanup();
							System.out.flush();
							String result=baos.toString();
							//System.out.println(satisfying);
							System.out.println("experiment finished at "+new Date().toString());
							System.out.println("******************************************************************************************************************");
							FileOps.write(result, "experimentResults.txt");
								    
						}
					}
				}
						
			}
		}
				
				
				
		System.setOut(old);
	}
	
	
	public static void testCaseStudyCav16_partiallyObservable_optimized(){
		//testing different methods (central, compositional, old-compositional) with respect to 
				// 1) Different objectives
				//		a) Collision avoidance (dynamic and static)
				//		b) Formation control
				//		c) Bounded reachability 
				// 2) Different number of agents
				// 		a) Controllable
				//		b) Uncontrollable
				// 3) different dimensions for the grid-world
				
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
				
		//define the maximum number of agents for the experiments
		int maxNumOfUncontrolledAgents = 1;
		int maxNumOfControlledAgents = 3;
		//define maximum dimension to be considered
		int minDim = 10;
		int maxDim = 11;
				
		//define different objective types for different experiments
		ArrayList<Boolean[]> experimentObjectiveTypes = createObjectiveTypesForExperiments();
				
		//different number of uncontrolled agents
		for(int numOfUncontrolledAgents = 1; numOfUncontrolledAgents<= maxNumOfUncontrolledAgents; numOfUncontrolledAgents++){
		//different number of controlled agents
			for(int numOfControlledAgents = 3; numOfControlledAgents<= maxNumOfControlledAgents; numOfControlledAgents++){
				//different objectives
				//TODO
				for(int i =0; i<experimentObjectiveTypes.size(); i++ ){
					Boolean[] currentObjectiveForExperminet = experimentObjectiveTypes.get(i);
					//different dimensions
					for(int dim = minDim; dim < maxDim; dim++){
								
						for(int j=1; j<3; j++){
							
//							if(numOfControlledAgents <=2){
//								continue;
//							}
//							
//							if(i==0){
//								continue;
//							}
							
							if(j!=1){
								continue;
							}
							
							if(i!=2){
								continue;
							}
									
							System.out.println();
							System.out.println("******************************************************************************************************************");
							System.out.println("**************************************************New Experiment**************************************************");
							System.out.println("experiment started at "+new Date().toString());
							System.out.println("number of uncontrolled agents "+numOfUncontrolledAgents);
							System.out.println("number of controlled agents "+numOfControlledAgents);
							printObjectiveTypeForExperiment(currentObjectiveForExperminet);
							System.out.println("dimension of the gird world is "+dim);
							System.out.println("synthesis method is ");
							GameSolvingMethod method = GameSolvingMethod.Compositional_StrategyPruning;
							switch(j){
								case 1 : System.out.println("Compositional");
									method = GameSolvingMethod.Compositional_StrategyPruning;
									break;
								case 2 : System.out.println("Central");
									method = GameSolvingMethod.Central;
									break;
								case 3 : System.out.println("Compositional - OLD");
									method = GameSolvingMethod.Compositional_OLD;
									break;
//								case 4 : System.out.println("Counter-Strategy-Guided compositional");
//									break;
//									default : System.out.println("Shouldn't reach here!");
							}
							
							//prepare the results table
							ArrayList<String> newExperiment = new ArrayList<String>();
							results.add(newExperiment);
							currentIndex++;
							newExperiment.add(""+numOfUncontrolledAgents);
							newExperiment.add(""+numOfControlledAgents);
							
							BDD bdd = new BDD(10000,1000);
							try{
								runExperiment_partiallyObservable_optimized(bdd, numOfUncontrolledAgents, numOfControlledAgents, currentObjectiveForExperminet, dim, method);
							}catch(Exception e){
								e.printStackTrace();
							}
							bdd.cleanup();
							System.out.flush();
							String result=baos.toString();
							//System.out.println(satisfying);
							System.out.println("experiment finished at "+new Date().toString());
							System.out.println("******************************************************************************************************************");
							FileOps.write(result, "experimentResults.txt");
							
							//write structured results in a file
						    writeStructuredResultsToFile("structuredResults_po.txt");
								    
						}
					}
				}
						
			}
		}
				
				
				
		System.setOut(old);
	}
	private static ArrayList<Boolean[]> createObjectiveTypesForExperiments(){
		ArrayList<Boolean[]> result = new ArrayList<Boolean[]>();
		
		//we consider three forms of objectives: collision avoidance, formation control, and reachabiliy
		
		//experiment1: only collision avoidance
		Boolean[] experiment1 = new Boolean[]{true, false, false};
		
		//experment2: collision avoidance + formation control
		Boolean[] experiment2 = new Boolean[]{true, true, false};
		
		//experment3: collision avoidance + reachability
		Boolean[] experiment3 = new Boolean[]{true, false, true};
		
		//experment4: collision avoidance + formation control + reachability
		Boolean[] experiment4 = new Boolean[]{true, true, true};
		
		result.add(experiment1);
		result.add(experiment2);
		result.add(experiment3);
		result.add(experiment4);
		
		return result;
	}
	
	private static void printObjectiveTypeForExperiment(Boolean[] type){
		System.out.print("Objectives considered: ");
		if(type[0]){
			System.out.print("Collision avoidance + ");
		}
		if(type[1]){
			System.out.print("formation control + ");
		}
		if(type[2]){
			System.out.print("reachability");
		}
		System.out.println();
	}
	
	//TODO: add verification
	public static void runExperiment_partiallyObservable(BDD bdd, int numOfUncontrolledAgents, int numOfControlledAgents, 
			Boolean[] objectiveTypes, int dim, GameSolvingMethod method) throws Exception{
		//create experiment
		System.out.println("creating the case study for partially observable games");
		//define the init cells
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		//TODO: create a reachabilityObjective class and move these there
		
		
		//create agents
//		ArrayList<Agent2D> uncontrolledAgents = createAgents(bdd, dim-1, "uncontrolled_R", AgentType.Uncontrollable, uncontrolledAgentsInitCells);
//		ArrayList<Agent2D> controlledAgents = createAgentsWithStayPutAction(bdd, dim-1, "controlled_R", AgentType.Controllable, controlledAgentsInitCells);
//				
//		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
//		agents.addAll(uncontrolledAgents);
//		agents.addAll(controlledAgents);
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
				
		//create objectives
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
						objectiveTypes[0], objectiveTypes[0], objectiveTypes[1]);
			
//		//define reachability variables
//		Variable[] flags = null;
//		ArrayList<Variable[]> counters=null;
//		int bound = dim+1;
//		if(objectiveTypes[2]){
//			flags = UtilityMethods.createBoundedReachabilityFlags(bdd, controlledAgentsInitCells.size(), "controlledR_f");
//			counters = UtilityMethods.createBoundedReachabilityCounters(bdd, controlledAgentsInitCells.size(), bound,  "controlledR_Counter");
//		}
		
		//define reachability objective
		if(objectiveTypes[2]){
			createReachabilityObjectives(bdd, controlledAgents, dim);

//			createReachabilityObjectives(bdd, controlledAgents, dim, bound, flags, counters);
		}
		
		System.out.println("objectives created");
		
		//synthesize a strategy using a game solving method and verify
		System.out.println("synthesizing the strategy");
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			applyCompositionalGameSolvingMethod_partiallyObservable(bdd, dim, uncontrolledAgents, controlledAgents, objectives);
		}else if(method == GameSolvingMethod.Compositional_OLD){
			System.err.println("not implemented yet!");
			apply_OLD_CompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else{
			applyCentralGameSolvingMethod_partiallyObservable(bdd, dim, uncontrolledAgents, controlledAgents, objectives);
		}
	}
	
	//TODO: add verification
	public static void runExperiment_partiallyObservable_optimized(BDD bdd, int numOfUncontrolledAgents, int numOfControlledAgents, 
			Boolean[] objectiveTypes, int dim, GameSolvingMethod method) throws Exception{
		//create experiment
		System.out.println("creating the case study for partially observable games");
		//define the init cells
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		//TODO: create a reachabilityObjective class and move these there
		
		
		//create agents
//		ArrayList<Agent2D> uncontrolledAgents = createAgents(bdd, dim-1, "uncontrolled_R", AgentType.Uncontrollable, uncontrolledAgentsInitCells);
//		ArrayList<Agent2D> controlledAgents = createAgentsWithStayPutAction(bdd, dim-1, "controlled_R", AgentType.Controllable, controlledAgentsInitCells);
//				
//		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
//		agents.addAll(uncontrolledAgents);
//		agents.addAll(controlledAgents);
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
				
		//create objectives
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
						objectiveTypes[0], objectiveTypes[0], objectiveTypes[1]);
			
//		//define reachability variables
//		Variable[] flags = null;
//		ArrayList<Variable[]> counters=null;
//		int bound = dim+1;
//		if(objectiveTypes[2]){
//			flags = UtilityMethods.createBoundedReachabilityFlags(bdd, controlledAgentsInitCells.size(), "controlledR_f");
//			counters = UtilityMethods.createBoundedReachabilityCounters(bdd, controlledAgentsInitCells.size(), bound,  "controlledR_Counter");
//		}
		
		//define reachability objective later
//		if(objectiveTypes[2]){
//			createReachabilityObjectives(bdd, controlledAgents, dim);
//
////			createReachabilityObjectives(bdd, controlledAgents, dim, bound, flags, counters);
//		}
		
		System.out.println("objectives created");
		
		//update static results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		if(objectiveTypes[1] && objectiveTypes[2]){
			currentExperiment.add("all");
		}else if(objectiveTypes[1]){
			currentExperiment.add("safety+formation");
		}else if(objectiveTypes[2]){
			currentExperiment.add("safety+reachability");
		}else{
			currentExperiment.add("safety");
		}
		currentExperiment.add(""+dim);
		
		//synthesize a strategy using a game solving method and verify
		System.out.println("synthesizing the strategy");
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			applyCompositionalGameSolvingMethod_partiallyObservable_optimized(bdd, dim, uncontrolledAgents, controlledAgents, objectives, objectiveTypes[2]);
		}else if(method == GameSolvingMethod.Compositional_OLD){
			System.err.println("not implemented yet!");
			apply_OLD_CompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else{
			applyCentralGameSolvingMethod_partiallyObservable_optimized(bdd, dim, uncontrolledAgents, controlledAgents, objectives, objectiveTypes[2] );
		}
	}
	
	/**
	 * COmputes an estimation of the number of variables required in the knowledge game structure based on 
	 * local observation assumption
	 * @param dim
	 * @param uncontrolledAgents
	 * @param controlledAgents
	 * @return
	 */
	private static int estimateNumberOfVariablesInKnowledgeGameStructure(ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents){
		int numOfVars = 0; 
		
		//for each uncontrolled agent we may need an exponentially larger number of variables
		for(Agent2D agent : uncontrolledAgents){
			int n = agent.getVariables().length;
			int n_pow_2 = (int) Math.pow(n, 2);
			numOfVars+=n_pow_2;
		}
		
		//for each controlled agent we need exactly the same number of vars
		for(Agent2D agent : controlledAgents){
			numOfVars += agent.getVariables().length;
		}
		
		return numOfVars;
	}
	
	private static int estimateNumberOfVariablesInKnowledgeGameStructure(ArrayList<Agent2D> agents){
		int numOfVars = 0; 
		
		
		for(Agent2D agent : agents){
			//for each uncontrolled agent we may need an exponentially larger number of variables
			if(agent.getType() == AgentType.Uncontrollable){
				int n = agent.getVariables().length;
				int n_pow_2 = (int) Math.pow(n, 2);
				numOfVars+=n_pow_2;
			}else{//for each controlled agent we need exactly the same number of vars
				numOfVars += agent.getVariables().length;
			}	
		}
		
		return numOfVars;
	}
	
	private static int estimateNumberOfVariablesInKnowledgeGameStructure(Agent2D uncontrolledAgent, Agent2D controlledAgent){
		ArrayList<Agent2D> uncontrolledAgents = new ArrayList<Agent2D>();
		uncontrolledAgents.add(uncontrolledAgent);
		ArrayList<Agent2D> controlledAgents = new ArrayList<Agent2D>();
		controlledAgents.add(controlledAgent);
		return estimateNumberOfVariablesInKnowledgeGameStructure(uncontrolledAgents, controlledAgents);
	}
	
	private static void testGameStructuresWithNoUncontrolledAgent(){
		BDD bdd = new BDD(10000, 1000);
		int dim = 2;
		int numOfControlledAgents = 2;
		int numOfUncontrolledAgents = 0;
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
		
		System.out.println("Uncontrolled agents are");
		for(Agent2D agent : uncontrolledAgents){
			agent.print();
		}
		
		System.out.println("controlled agents are");
		for(Agent2D agent : controlledAgents){
			agent.print();
		}
		
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
				true, true, true);
		
		System.out.println("agents created");
		
		for(Integer obj : objectives){
			UtilityMethods.debugBDDMethods(bdd, "objective is", obj);
		}
		
		System.out.println("objectives created");
		
		//create central game
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, agents);
		centralGame.printGame();
		UtilityMethods.getUserInput();
				
		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
				
		//create central objective
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		
		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
				
		//solve the game 
		game.GameSolution sol = GameSolver.solve(bdd, centralGame, objective);
		sol.print();
		
		ArrayList<GameStructure> reachabilityGames = createReachabilityObjectivesAsGameStructures(bdd, controlledAgents, dim);
		System.out.println("reach objs");
		for(GameStructure gs : reachabilityGames){
			gs.printGame();
			UtilityMethods.getUserInput();
		}
		
		GameStructure newGS = centralGame.compose(reachabilityGames.get(0));
		
		GameSolution sol1 = GameSolver.solve(bdd, newGS, bdd.ref(bdd.getOne()));
		sol1.print();
		
	}
	
	private static void testGameStructuresWithNoUncontrolledAgent_partiallyObservable(){
		BDD bdd = new BDD(10000, 1000);
		int dim = 4;
		int numOfControlledAgents = 1;
		int numOfUncontrolledAgents = 1;
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
		
//		System.out.println("Uncontrolled agents are");
//		for(Agent2D agent : uncontrolledAgents){
//			agent.print();
//		}
//		
//		System.out.println("controlled agents are");
//		for(Agent2D agent : controlledAgents){
//			agent.print();
//		}
		
//		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
//				true, true, true);
		
		
		
//		System.out.println("agents created");
		
//		for(Integer obj : objectives){
//			UtilityMethods.debugBDDMethods(bdd, "objective is", obj);
//		}
		
		int objective = UtilityFormulas.assignCell(bdd, controlledAgents.get(0), new GridCell2D(dim-1, dim-1));
		objective = BDDWrapper.not(bdd, objective);
		
		System.out.println("objectives created");
		
		//create central game
//		GameStructure centralGame = GameStructure.createGameForAgents(bdd,controlledAgents);
		GameStructure centralGame = GameStructure.createGameForAgents(bdd,agents);
//		centralGame.printGame();
//		UtilityMethods.getUserInput();
		
		Observation obs = new Observation(bdd, dim, uncontrolledAgents, controlledAgents);
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
				centralGame, obs.getObservableVars(), obs.getObservationMap());
		
		int numOfKnowledgeGameVars = estimateNumberOfVariablesInKnowledgeGameStructure(agents);
		GameStructure kGS = pogs.createKnowledgeGame(centralGame.getInit(), numOfKnowledgeGameVars);
		
//		kGS.printGame();
				
//		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
//				
		//create central objective
//		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		
		int kObjective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(objective);
		
		
//		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
//				
		//solve the game 
//		game.GameSolution sol = GameSolver.solve(bdd, kGS, kObjective);
//		sol.print();
//		
//		ArrayList<TurnBasedPartiallyObservableGameStructure> POGSs = new ArrayList<TurnBasedPartiallyObservableGameStructure>();
//		POGSs.add(pogs);
//		
//		ArrayList<GameStructure> knowledgeGameStructures = new ArrayList<GameStructure>();
//		knowledgeGameStructures.add(kGS);
//		
////		ArrayList<GameStructure> reachabilityGames = createReachabilityObjectivesAsGameStructures(bdd, controlledAgents, dim);
//		ArrayList<GameStructure> reachabilityGames = createReachabilityObjectivesAsGameStructures_partiallyObservable(bdd, 
//				controlledAgents, POGSs, knowledgeGameStructures, dim);
		
		ArrayList<GameStructure> reachabilityGames = createReachabilityObjectivesAsGameStructures_partiallyObservable(bdd,
				controlledAgents, pogs, kGS, dim);
		System.out.println("reach objs");
		for(GameStructure gs : reachabilityGames){
			gs.printGame();
			UtilityMethods.getUserInput();
		}
		
		GameStructure newGS = kGS.compose(reachabilityGames.get(0));
		
		GameSolution sol1 = GameSolver.solve(bdd, newGS, bdd.ref(bdd.getOne()));
		sol1.print();
		
	}
	
	//TODO: add verification
	public static void runExperiment(BDD bdd, int numOfUncontrolledAgents, int numOfControlledAgents, 
			Boolean[] objectiveTypes, int dim, GameSolvingMethod method) throws Exception{
		//create experiment
		System.out.println("creating the case study");
		//define the init cells
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		//TODO: create a reachabilityObjective class and move these there
		
		
		//create agents
//		ArrayList<Agent2D> uncontrolledAgents = createAgents(bdd, dim-1, "uncontrolled_R", AgentType.Uncontrollable, uncontrolledAgentsInitCells);
//		ArrayList<Agent2D> controlledAgents = createAgentsWithStayPutAction(bdd, dim-1, "controlled_R", AgentType.Controllable, controlledAgentsInitCells);
//				
//		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
//		agents.addAll(uncontrolledAgents);
//		agents.addAll(controlledAgents);
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
				
		//create objectives
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
						objectiveTypes[0], objectiveTypes[0], objectiveTypes[1]);
			
//		//define reachability variables
//		Variable[] flags = null;
//		ArrayList<Variable[]> counters=null;
//		int bound = dim+1;
//		if(objectiveTypes[2]){
//			flags = UtilityMethods.createBoundedReachabilityFlags(bdd, controlledAgentsInitCells.size(), "controlledR_f");
//			counters = UtilityMethods.createBoundedReachabilityCounters(bdd, controlledAgentsInitCells.size(), bound,  "controlledR_Counter");
//		}
		
		//define reachability objective
		if(objectiveTypes[2]){
			createReachabilityObjectives(bdd, controlledAgents, dim);

//			createReachabilityObjectives(bdd, controlledAgents, dim, bound, flags, counters);
		}
		
		System.out.println("objectives created");
		
		//synthesize a strategy using a game solving method and verify
		System.out.println("synthesizing the strategy");
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			applyCompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else if(method == GameSolvingMethod.Compositional_OLD){
			apply_OLD_CompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else{
			applyCentralGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}
	}
	
	public static void runExperiment_optimized(BDD bdd, int numOfUncontrolledAgents, int numOfControlledAgents, 
			Boolean[] objectiveTypes, int dim, GameSolvingMethod method) throws Exception{
		//create experiment
		System.out.println("creating the case study");
		//define the init cells
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-i-2));
		}
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		for(int i=0; i<numOfControlledAgents; i++){
			controlledAgentsInitCells.add(new GridCell2D(0+i, 0));
		}
		
		//TODO: create a reachabilityObjective class and move these there
		
		
		//create agents
//		ArrayList<Agent2D> uncontrolledAgents = createAgents(bdd, dim-1, "uncontrolled_R", AgentType.Uncontrollable, uncontrolledAgentsInitCells);
//		ArrayList<Agent2D> controlledAgents = createAgentsWithStayPutAction(bdd, dim-1, "controlled_R", AgentType.Controllable, controlledAgentsInitCells);
//				
//		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
//		agents.addAll(uncontrolledAgents);
//		agents.addAll(controlledAgents);
		
		ArrayList<Agent2D> agents = UtilityTransitionRelations.createSimpleRobots(bdd, dim-1, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		ArrayList<Agent2D> uncontrolledAgents = Agent2D.getAgentsWithType(agents, AgentType.Uncontrollable);
		ArrayList<Agent2D> controlledAgents = Agent2D.getAgentsWithType(agents, AgentType.Controllable);
		
		System.out.println("agents created");
				
		//create objectives
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
						objectiveTypes[0], objectiveTypes[0], objectiveTypes[1]);
			
//		//define reachability variables
//		Variable[] flags = null;
//		ArrayList<Variable[]> counters=null;
//		int bound = dim+1;
//		if(objectiveTypes[2]){
//			flags = UtilityMethods.createBoundedReachabilityFlags(bdd, controlledAgentsInitCells.size(), "controlledR_f");
//			counters = UtilityMethods.createBoundedReachabilityCounters(bdd, controlledAgentsInitCells.size(), bound,  "controlledR_Counter");
//		}
		
		//define reachability objective as game structures
		ArrayList<GameStructure> reachabilityObjectives = null;
		if(objectiveTypes[2]){
			reachabilityObjectives = createReachabilityObjectivesAsGameStructures(bdd, controlledAgents, dim);	
		}
		
		System.out.println("objectives created");
		
		//update static results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		if(objectiveTypes[1] && objectiveTypes[2]){
			currentExperiment.add("all");
		}else if(objectiveTypes[1]){
			currentExperiment.add("safety+formation");
		}else if(objectiveTypes[2]){
			currentExperiment.add("safety+reachability");
		}else{
			currentExperiment.add("safety");
		}
		currentExperiment.add(""+dim);
		
		//synthesize a strategy using a game solving method and verify
		System.out.println("synthesizing the strategy");
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			applyCompositionalGameSolvingMethod_optimized(bdd, uncontrolledAgents, controlledAgents, objectives, reachabilityObjectives);
		}else if(method == GameSolvingMethod.Compositional_OLD){
			apply_OLD_CompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else{
			applyCentralGameSolvingMethod_optimized(bdd, uncontrolledAgents, controlledAgents, objectives, reachabilityObjectives);
		}
	}
	
	public static CaseStudy2D runExperiment(BDD bdd, int dim, GameSolvingMethod method) throws Exception{
		//create experiment
		System.out.println("creating the case study");
		//define the init cells
		ArrayList<GridCell2D> uncontrolledAgentsInitCells = new ArrayList<GridCell2D>();
		uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-2));
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		controlledAgentsInitCells.add(new GridCell2D(0, 0));
		controlledAgentsInitCells.add(new GridCell2D(0, 1));
//		controlledAgentsInitCells.add(new GridCell2D(1, 1));
//		controlledAgentsInitCells.add(new GridCell2D(1, 0));
		
		//create agents
		ArrayList<Agent2D> uncontrolledAgents = createAgents(bdd, dim-1, "uncontrolled_R", AgentType.Uncontrollable, uncontrolledAgentsInitCells);
		ArrayList<Agent2D> controlledAgents = createAgentsWithStayPutAction(bdd, dim-1, "controlled_R", AgentType.Controllable, controlledAgentsInitCells);
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		agents.addAll(uncontrolledAgents);
		agents.addAll(controlledAgents);
		
		System.out.println("agents created");
		
		//create objectives
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = createSafetyObjectives(bdd, dim, uncontrolledAgents, controlledAgents, 
				true, true, false);
	
		
		
		//define reachability objective
		createReachabilityObjectives(bdd, controlledAgents, dim);
		
//		testing involved agents computation
//		ArrayList<ArrayList<Agent2D>> involved = involvedAgentsInConjuncts(bdd, controlledAgents, objectives);
//		ArrayList<Integer> notInvolvedCubes = notInvolvedAgentsCubeForConjuncts(bdd, controlledAgents, involved);
//		System.out.println("listing involved agents");
//		for(int i=0; i<objectives.size(); i++){
//			UtilityMethods.debugBDDMethods(bdd, "conjunct is", objectives.get(i));
//			System.out.println("involved agents are");
//			for(Agent2D agent : involved.get(i)){
//				System.out.println(agent.getName());
//			}
//			UtilityMethods.debugBDDMethods(bdd, "not involved cube", notInvolvedCubes.get(i));
//			UtilityMethods.getUserInput();
//		}
		
		
		
		System.out.println("objectives created");
		
		//synthesize a strategy using a game solving method and verify
		System.out.println("synthesizing the strategy");
		int controller=-1;
		GameStructure restrictedGameStructure = null;
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			controller = applyCompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else if(method == GameSolvingMethod.Compositional_OLD){
			apply_OLD_CompositionalGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}else{
			restrictedGameStructure = applyCentralGameSolvingMethod(bdd, uncontrolledAgents, controlledAgents, objectives);
		}
		
		//prepare a case study object for running the simulation
		ArrayList<GridCell2D> staticObstacles = defineStaticObstacles(dim);
		Simulator2D simulator=null;
		int agentsInitialState = UtilityMethods.getAgentsInitialState(bdd, agents);
		if(method == GameSolvingMethod.Compositional_StrategyPruning){
			simulator = new DecoupledMultiAgentSimulator2D(bdd, agents, controller, agentsInitialState);
		}else if(method == GameSolvingMethod.Central){
			simulator = new Simulator2D(bdd, agents, restrictedGameStructure, agentsInitialState);
		}
		CaseStudy2D caseStudy = new CaseStudy2D(bdd, dim, dim, controlledAgents, uncontrolledAgents, null, staticObstacles, simulator);
		
		return caseStudy;
		
		//verify the solution
	}
	
	public static GameStructure applyCentralGameSolvingMethod_partiallyObservable(BDD bdd, int dim, 
			ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, 
			ArrayList<Integer> objectives) throws Exception{
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		System.out.println("Solving game centrally");
		long t0_all = UtilityMethods.timeStamp();
		long t0 = UtilityMethods.timeStamp();
		
		//create central game
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
		UtilityMethods.duration(t0, "central game was created in");
		
//		Variable.printVariables(centralGame.variables);
//		UtilityMethods.getUserInput();
//		
//		centralGame.printGame();
//		UtilityMethods.getUserInput();
		
//		Variable[] observables =  Variable.createVariables(bdd, centralGame.variables.length, "obsVars");
//		
//		int[][] observationMaps = new int[uncontrolledAgents.size()][controlledAgents.size()];
//		int observationMap = bdd.ref(bdd.getOne());
//		for(int i=0; i<uncontrolledAgents.size(); i++){
//			for(int j=0; j<controlledAgents.size(); j++){
//				observationMaps[i][j]= TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
//						dim, uncontrolledAgents.get(i), controlledAgents.get(j), observables);
//				observationMap = BDDWrapper.andTo(bdd, observationMap, observationMaps[i][j]);
//			}
//		}
		
		
		
		Observation observation = new Observation(bdd, dim, uncontrolledAgents, controlledAgents);
		
		
//		Variable.printVariables(observation.getObservableVars());
//		UtilityMethods.debugBDDMethods(bdd, "observationMap is", observation.getObservationMap());
//		UtilityMethods.debugBDDMethods(bdd, "central game init is", centralGame.getInit());
//		UtilityMethods.getUserInput();
		
//		UtilityMethods.debugBDDMethods(bdd, "observation map is", observation.getObservationMap());
//		UtilityMethods.getUserInput();
		
		//define partially observable game structure
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, centralGame, observables, observationMap);
		
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, centralGame, observation.getObservableVars(), observation.getObservationMap());

		
		//2) call the subset construction procedure
		System.out.println("constructing the knowledge game");
		//estimate the number of required variables 
		int numOfKnowledgeGameStructureVars = estimateNumberOfVariablesInKnowledgeGameStructure(uncontrolledAgents, controlledAgents);
		GameStructure kGS = pogs.createKnowledgeGame(centralGame.getInit(), numOfKnowledgeGameStructureVars);
		
		
//		System.out.println("\n num of knowledge game vars "+kGS.variables.length);
//		System.out.println("\n");
//		UtilityMethods.getUserInput();
//		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
		
		//create central objective
		t0 = UtilityMethods.timeStamp();
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		UtilityMethods.duration(t0, "central objective was created in");
		
		//translate to objective for knowledge game
		int knowledgeGameObjective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
		
		//solve the game 
		t0 = UtilityMethods.timeStamp();
		game.GameSolution sol = GameSolver.solve(bdd, kGS, knowledgeGameObjective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in ");
		
		System.out.println();
		System.out.println();
		UtilityMethods.duration(t0_all, "The whole central method took");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//verify the solution
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = sol.strategyOfTheWinner().safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		return sol.strategyOfTheWinner();
	}
	
	public static GameStructure applyCentralGameSolvingMethod_partiallyObservable_optimized(BDD bdd, int dim, 
			ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, 
			ArrayList<Integer> objectives, boolean reachability) throws Exception{
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		System.out.println("Solving game centrally");
		long t0_all = UtilityMethods.timeStamp();
		long t0 = UtilityMethods.timeStamp();
		
		//create central game
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
		UtilityMethods.duration(t0, "central game was created in");
		
//		Variable.printVariables(centralGame.variables);
//		UtilityMethods.getUserInput();
//		
//		centralGame.printGame();
//		UtilityMethods.getUserInput();
		
//		Variable[] observables =  Variable.createVariables(bdd, centralGame.variables.length, "obsVars");
//		
//		int[][] observationMaps = new int[uncontrolledAgents.size()][controlledAgents.size()];
//		int observationMap = bdd.ref(bdd.getOne());
//		for(int i=0; i<uncontrolledAgents.size(); i++){
//			for(int j=0; j<controlledAgents.size(); j++){
//				observationMaps[i][j]= TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
//						dim, uncontrolledAgents.get(i), controlledAgents.get(j), observables);
//				observationMap = BDDWrapper.andTo(bdd, observationMap, observationMaps[i][j]);
//			}
//		}
		
		
		
		Observation observation = new Observation(bdd, dim, uncontrolledAgents, controlledAgents);
		
		
//		Variable.printVariables(observation.getObservableVars());
//		UtilityMethods.debugBDDMethods(bdd, "observationMap is", observation.getObservationMap());
//		UtilityMethods.debugBDDMethods(bdd, "central game init is", centralGame.getInit());
//		UtilityMethods.getUserInput();
		
//		UtilityMethods.debugBDDMethods(bdd, "observation map is", observation.getObservationMap());
//		UtilityMethods.getUserInput();
		
		//define partially observable game structure
//		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, centralGame, observables, observationMap);
		
		TurnBasedPartiallyObservableGameStructure pogs = new TurnBasedPartiallyObservableGameStructure(bdd, centralGame, observation.getObservableVars(), observation.getObservationMap());

		
		//2) call the subset construction procedure
		System.out.println("constructing the knowledge game");
		//estimate the number of required variables 
		int numOfKnowledgeGameStructureVars = estimateNumberOfVariablesInKnowledgeGameStructure(uncontrolledAgents, controlledAgents);
		GameStructure kGS = pogs.createKnowledgeGame(centralGame.getInit(), numOfKnowledgeGameStructureVars);
		

		
				
//		System.out.println("\n num of knowledge game vars "+kGS.variables.length);
//		System.out.println("\n");
//		UtilityMethods.getUserInput();
//		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
		
		//create central objective
		t0 = UtilityMethods.timeStamp();
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		UtilityMethods.duration(t0, "central objective was created in");
		
		//translate to objective for knowledge game
		int knowledgeGameObjective = pogs.translateConcreteObjectiveToKnowledgeGameObjective(objective);
		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
		
		if(reachability){
			ArrayList<GameStructure> reachabilityGameStructures = createReachabilityObjectivesAsGameStructures_partiallyObservable(bdd, controlledAgents, pogs, kGS, dim);
			for(GameStructure rGame : reachabilityGameStructures){
				kGS = kGS.compose(rGame);
			}
		}
		
		//solve the game 
		t0 = UtilityMethods.timeStamp();
		game.GameSolution sol = GameSolver.solve(bdd, kGS, knowledgeGameObjective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in ");
		
		System.out.println();
		System.out.println();
		String duration = UtilityMethods.duration_string(t0_all);
		UtilityMethods.duration(t0_all, "The whole central method took");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		String numOfVarsInBDD = BDDWrapper.numOfVariablesInBDD(bdd)+"";
//		String bdd_mem_usage = "$" + BDDWrapper.BDD_memory_usage(bdd)+"$";
		String bdd_mem_usage = BDDWrapper.BDD_memory_usage(bdd);
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//update experiment results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		currentExperiment.add(numOfVarsInBDD);
		currentExperiment.add(kGS.variables.length+"");
		currentExperiment.add(duration);
		currentExperiment.add(bdd_mem_usage);
		
		//verify the solution
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = sol.strategyOfTheWinner().safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		return sol.strategyOfTheWinner();
	}
	
	public static GameStructure applyCentralGameSolvingMethod(BDD bdd, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives) throws Exception{
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		System.out.println("Solving game centrally");
		long t0_all = UtilityMethods.timeStamp();
		long t0 = UtilityMethods.timeStamp();
		
		//create central game
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
		UtilityMethods.duration(t0, "central game was created in");
		
//		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
		
		//create central objective
		t0 = UtilityMethods.timeStamp();
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		UtilityMethods.duration(t0, "central objective was created in");
		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
		
		//solve the game 
		t0 = UtilityMethods.timeStamp();
		game.GameSolution sol = GameSolver.solve(bdd, centralGame, objective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in ");
		
		System.out.println();
		System.out.println();
		UtilityMethods.duration(t0_all, "The whole central method took");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//verify the solution
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = sol.strategyOfTheWinner().safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		return sol.strategyOfTheWinner();
	}
	
	public static GameStructure applyCentralGameSolvingMethod_optimized(BDD bdd, 
			ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, 
			ArrayList<Integer> objectives, 
			ArrayList<GameStructure> reachabilityObjectives) throws Exception{
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		System.out.println("Solving game centrally");
		long t0_all = UtilityMethods.timeStamp();
		long t0 = UtilityMethods.timeStamp();
		
		//create central game
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
		UtilityMethods.duration(t0, "central game was created in");
		
//		centralGame.removeUnreachableStates().toGameGraph().draw("centralGame.dot", 1, 0);
		
		//create central objective
		t0 = UtilityMethods.timeStamp();
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		UtilityMethods.duration(t0, "central objective was created in");
		
//		UtilityMethods.debugBDDMethods(bdd, "objective is", objective);
		
		if(reachabilityObjectives != null){
			for(GameStructure rGame : reachabilityObjectives){
				centralGame = centralGame.compose(rGame);
			}
		}
		
		//solve the game 
		t0 = UtilityMethods.timeStamp();
		game.GameSolution sol = GameSolver.solve(bdd, centralGame, objective);
		sol.print();
		UtilityMethods.duration(t0, "game was solved in ");
		
		System.out.println();
		System.out.println();
		String duration = UtilityMethods.duration_string(t0_all);
		UtilityMethods.duration(t0_all, "The whole central method took");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		String numOfVarsInBDD = BDDWrapper.numOfVariablesInBDD(bdd)+"";
		String bdd_mem_usage = BDDWrapper.BDD_memory_usage(bdd);
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//update experiment results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		currentExperiment.add(numOfVarsInBDD);
		currentExperiment.add(duration);
		currentExperiment.add(bdd_mem_usage);
		
		//verify the solution
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = sol.strategyOfTheWinner().safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		return sol.strategyOfTheWinner();
	}
	
	public static int applyCompositionalGameSolvingMethod_partiallyObservable(BDD bdd, int dim, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives) throws Exception{
		
		long t0;
		long t0_comp = UtilityMethods.timeStamp();
		
		//form the games and build the knowledge game structures for each controlled agent
		t0 = UtilityMethods.timeStamp();
		System.out.println("forming the knowledge game structures");
//		ArrayList<TurnBasedPartiallyObservableGameStructure> partiallyObservableGameStructures = new ArrayList<TurnBasedPartiallyObservableGameStructure>();
//		ArrayList<GameStructure> knowledgeGameStructures = new ArrayList<GameStructure>();
		
		HashMap<Agent2D, TurnBasedPartiallyObservableGameStructure> partiallyObservableGameStructuresForControlledAgents = new HashMap<Agent2D, TurnBasedPartiallyObservableGameStructure>();
		HashMap<Agent2D, GameStructure> knowledgeGameStructuresForControlledAgents = new HashMap<Agent2D, GameStructure>();
		
		for(int i=0; i<controlledAgents.size(); i++){
			ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
			agents.addAll(uncontrolledAgents);
			agents.add(controlledAgents.get(i));
			//dummy arraylist to use the observation method
			ArrayList<Agent2D> agent_i_arrayList = new ArrayList<Agent2D>();
			agent_i_arrayList.add(controlledAgents.get(i));
			GameStructure agent_i_gameStructure = GameStructure.createGameForAgents(bdd, agents);
			Observation agent_i_observation = new Observation(bdd, dim,  uncontrolledAgents, agent_i_arrayList);
			
			TurnBasedPartiallyObservableGameStructure agent_i_pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
					agent_i_gameStructure, agent_i_observation.getObservableVars(), 
					agent_i_observation.getObservationMap());
			
//			agent_i_gameStructure.printGameVars();
			
//			Variable.printVariables(agent_i_observation.getObservableVars());
			
//			partiallyObservableGameStructures.add(agent_i_pogs);
			partiallyObservableGameStructuresForControlledAgents.put(controlledAgents.get(i), agent_i_pogs);
			
			//estimate the number of knowledge game vars 
			int numOfKnowledgeGameVars = estimateNumberOfVariablesInKnowledgeGameStructure(agents);
			GameStructure kGS_i = agent_i_pogs.createKnowledgeGame(agent_i_gameStructure.getInit(), numOfKnowledgeGameVars);
			
//			knowledgeGameStructures.add(kGS_i);
			
			knowledgeGameStructuresForControlledAgents.put(controlledAgents.get(i), kGS_i);
		}
		
		UtilityMethods.duration(t0, "knowledge games constructed");
		
		//analyze the conjuncts to check which controlled agents are involved for each
		t0 = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts = involvedAgentsInConjuncts(bdd, controlledAgents, objectives);
		UtilityMethods.duration(t0, "involved agents computed in");
		
		
		//translate the objectives to objectives for knowledge games
		ArrayList<Integer> knowledgeGameObjectives = new ArrayList<Integer>();
		
		//compose the game structure based on involved agents in conjuncts
		t0 = UtilityMethods.timeStamp();
		ArrayList<GameStructure> composedGameStructures= new ArrayList<GameStructure>();
		
//		for(ArrayList<Agent2D> involved : involvedAgentsInConjuncts){
		for(int obj_index =0; obj_index<objectives.size(); obj_index++){	
			ArrayList<Agent2D> involved = involvedAgentsInConjuncts.get(obj_index);
			int currentConjunct = objectives.get(obj_index);
			
			Agent2D currentCOntrolledAgent = involved.get(0);
			//compose the maps so that later we can translate the objective
			HashMap<Integer, Integer> composedMap = partiallyObservableGameStructuresForControlledAgents.get(currentCOntrolledAgent).getKnowledgeGameStatesToPartiallyObservableGameStructureStates();
			GameStructure composedGameForInvolvedAgents = knowledgeGameStructuresForControlledAgents.get(currentCOntrolledAgent);
			for(int i=1; i<involved.size(); i++){
				Agent2D nextControlledAgent = involved.get(i);
				GameStructure gameStructureForNextControlledAgent = knowledgeGameStructuresForControlledAgents.get(nextControlledAgent);
				//TODO: clean the games --> free previous composed games
				composedGameForInvolvedAgents = composedGameForInvolvedAgents.compose(gameStructureForNextControlledAgent);
				
				//compose the maps
				HashMap<Integer, Integer> nextAgentMap = partiallyObservableGameStructuresForControlledAgents.get(nextControlledAgent).getKnowledgeGameStatesToPartiallyObservableGameStructureStates();
				composedMap = TurnBasedPartiallyObservableGameStructure.composeKnowledgeToPerfectInformationStatesMap(bdd, composedMap,
						nextAgentMap);
			}
			//composed knowledge game structures
			composedGameStructures.add(composedGameForInvolvedAgents);
			
			//translate the objective
			int knowledgeGameObj_i = TurnBasedPartiallyObservableGameStructure.translateConcreteObjectiveToKnowledgeGameObjective(bdd, 
					currentConjunct, composedMap);
			knowledgeGameObjectives.add(knowledgeGameObj_i);
			
//			UtilityMethods.debugBDDMethods(bdd, "objective is", currentConjunct);
//			UtilityMethods.debugBDDMethods(bdd, "translated objective is", knowledgeGameObj_i);
//			UtilityMethods.getUserInput();
		}
		UtilityMethods.duration(t0, "composite game structures and knowledge game objectives computed in");
		
		//prepare the projection cubes
		t0 = UtilityMethods.timeStamp();
		System.out.println("computing the projection cubes");
		ArrayList<Integer> projectionCubes = computeProjectionCubes(bdd, composedGameStructures);
		UtilityMethods.duration(t0, "projection cubes computed in");
		
		//form the games and solve them
		

		
		//enter the main loop of compositional synthesis
		

		
		ArrayList<Integer> controllers = new ArrayList<Integer>();
		int composedController=bdd.ref(bdd.getZero());
		ArrayList<Integer> projectedControllers = new ArrayList<Integer>();
		
		do{
			//TODO: somewhere I should clean up the solutions
			ArrayList<GameSolution> solutions = new ArrayList<GameSolution>();
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			for(int i = 0 ; i< objectives.size(); i++){
				GameSolution solution_i = GameSolver.solve(bdd, composedGameStructures.get(i), knowledgeGameObjectives.get(i)); 
				solutions.add(solution_i);
				//to check if there is an error
				if(solution_i.getWinner() == Player.ENVIRONMENT){
					solution_i.print();
				}
			}
			UtilityMethods.duration(t0, "games were solved in ");
			
			//TODO: clean up the controllers
			controllers = new ArrayList<Integer>();
			for(int i=0; i<solutions.size(); i++){
				controllers.add(solutions.get(i).getController());
			}
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			BDDWrapper.free(bdd, composedController);
			composedController = bdd.ref(bdd.getOne());
			for(int controller : controllers){
				composedController = BDDWrapper.andTo(bdd, composedController, controller);
			}
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("projecting controllers");
			t0=UtilityMethods.timeStamp();
			//TODO: clean up the projections 
			projectedControllers = new ArrayList<Integer>();
			for(int i=0; i<controllers.size(); i++){
				int projectedController = BDDWrapper.exists(bdd, composedController, projectionCubes.get(i));
				projectedControllers.add(projectedController);
			}
			UtilityMethods.duration(t0, "controllers projected in ");
			
			//check if a fixed point is reached
			boolean fixedPointReached = true;
			for(int i=0; i<controllers.size(); i++){
				int c = controllers.get(i);
				int pc = projectedControllers.get(i);
				if(pc != c){
					fixedPointReached = false;
					break;
				}
			}
			
			if(fixedPointReached){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
			for(int i=0; i<composedGameStructures.size(); i++){
				GameStructure currentGameStructure = composedGameStructures.get(i);
				GameStructure restrictedGameStructure = currentGameStructure.composeWithController(projectedControllers.get(i));
				restrictedGameStructures.add(restrictedGameStructure);
			}
			//TODO: clean up game structures
			composedGameStructures = restrictedGameStructures;
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		System.out.println();
		System.out.println();
		UtilityMethods.duration(t0_comp, "compositional algorithm terminated in ");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//verifying the solution
//		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
//		allAgents.addAll(uncontrolledAgents);
//		allAgents.addAll(controlledAgents);
		
		//create central game
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
//		UtilityMethods.duration(t0, "central game was created in"); 
//		GameStructure restrictedGame = centralGame.composeWithController(composedController);
//		//create central objective
//		t0 = UtilityMethods.timeStamp();
//		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
//		UtilityMethods.duration(t0, "central objective was created in");
//		//verify the solution
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = restrictedGame.safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		
		return composedController;
	}
	
public static int applyCompositionalGameSolvingMethod_partiallyObservable_optimized(BDD bdd, int dim, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives, boolean reachability) throws Exception{
		
		long t0;
		long t0_comp = UtilityMethods.timeStamp();
		
		//form the games and build the knowledge game structures for each controlled agent
		t0 = UtilityMethods.timeStamp();
		System.out.println("forming the knowledge game structures");
//		ArrayList<TurnBasedPartiallyObservableGameStructure> partiallyObservableGameStructures = new ArrayList<TurnBasedPartiallyObservableGameStructure>();
//		ArrayList<GameStructure> knowledgeGameStructures = new ArrayList<GameStructure>();
		
		HashMap<Agent2D, TurnBasedPartiallyObservableGameStructure> partiallyObservableGameStructuresForControlledAgents = new HashMap<Agent2D, TurnBasedPartiallyObservableGameStructure>();
		HashMap<Agent2D, GameStructure> knowledgeGameStructuresForControlledAgents = new HashMap<Agent2D, GameStructure>();
		
		ArrayList<TurnBasedPartiallyObservableGameStructure> POGSs = new ArrayList<TurnBasedPartiallyObservableGameStructure>();
		ArrayList<GameStructure> knowledgeGames = new ArrayList<GameStructure>();
		
		int numOfKVars = 0;
		
		for(int i=0; i<controlledAgents.size(); i++){
			ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
			agents.addAll(uncontrolledAgents);
			agents.add(controlledAgents.get(i));
			//dummy arraylist to use the observation method
			ArrayList<Agent2D> agent_i_arrayList = new ArrayList<Agent2D>();
			agent_i_arrayList.add(controlledAgents.get(i));
			GameStructure agent_i_gameStructure = GameStructure.createGameForAgents(bdd, agents);
			Observation agent_i_observation = new Observation(bdd, dim,  uncontrolledAgents, agent_i_arrayList);
			
			TurnBasedPartiallyObservableGameStructure agent_i_pogs = new TurnBasedPartiallyObservableGameStructure(bdd, 
					agent_i_gameStructure, agent_i_observation.getObservableVars(), 
					agent_i_observation.getObservationMap());
			
			POGSs.add(agent_i_pogs);
			
//			agent_i_gameStructure.printGameVars();
			
//			Variable.printVariables(agent_i_observation.getObservableVars());
			
//			partiallyObservableGameStructures.add(agent_i_pogs);
			partiallyObservableGameStructuresForControlledAgents.put(controlledAgents.get(i), agent_i_pogs);
			
			//estimate the number of knowledge game vars 
			int numOfKnowledgeGameVars = estimateNumberOfVariablesInKnowledgeGameStructure(agents);
			GameStructure kGS_i = agent_i_pogs.createKnowledgeGame(agent_i_gameStructure.getInit(), numOfKnowledgeGameVars);
			
			
			
//			knowledgeGameStructures.add(kGS_i);
			
			knowledgeGameStructuresForControlledAgents.put(controlledAgents.get(i), kGS_i);
			
			knowledgeGames.add(kGS_i);
			
			numOfKVars += kGS_i.variables.length;
		}
		
		
		
		UtilityMethods.duration(t0, "knowledge games constructed");
		
		
		
		//analyze the conjuncts to check which controlled agents are involved for each
		t0 = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts = involvedAgentsInConjuncts(bdd, controlledAgents, objectives);
		UtilityMethods.duration(t0, "involved agents computed in");
		
		
		//translate the objectives to objectives for knowledge games
		ArrayList<Integer> knowledgeGameObjectives = new ArrayList<Integer>();
		
		//compose the game structure based on involved agents in conjuncts
		t0 = UtilityMethods.timeStamp();
		ArrayList<GameStructure> composedGameStructures= new ArrayList<GameStructure>();
		
//		for(ArrayList<Agent2D> involved : involvedAgentsInConjuncts){
		for(int obj_index =0; obj_index<objectives.size(); obj_index++){	
			ArrayList<Agent2D> involved = involvedAgentsInConjuncts.get(obj_index);
			int currentConjunct = objectives.get(obj_index);
			
			Agent2D currentCOntrolledAgent = involved.get(0);
			//compose the maps so that later we can translate the objective
			HashMap<Integer, Integer> composedMap = partiallyObservableGameStructuresForControlledAgents.get(currentCOntrolledAgent).getKnowledgeGameStatesToPartiallyObservableGameStructureStates();
			GameStructure composedGameForInvolvedAgents = knowledgeGameStructuresForControlledAgents.get(currentCOntrolledAgent);
			for(int i=1; i<involved.size(); i++){
				Agent2D nextControlledAgent = involved.get(i);
				GameStructure gameStructureForNextControlledAgent = knowledgeGameStructuresForControlledAgents.get(nextControlledAgent);
				//TODO: clean the games --> free previous composed games
				composedGameForInvolvedAgents = composedGameForInvolvedAgents.compose(gameStructureForNextControlledAgent);
				
				//compose the maps
				HashMap<Integer, Integer> nextAgentMap = partiallyObservableGameStructuresForControlledAgents.get(nextControlledAgent).getKnowledgeGameStatesToPartiallyObservableGameStructureStates();
				composedMap = TurnBasedPartiallyObservableGameStructure.composeKnowledgeToPerfectInformationStatesMap(bdd, composedMap,
						nextAgentMap);
			}
			//composed knowledge game structures
			composedGameStructures.add(composedGameForInvolvedAgents);
			
			//translate the objective
			int knowledgeGameObj_i = TurnBasedPartiallyObservableGameStructure.translateConcreteObjectiveToKnowledgeGameObjective(bdd, 
					currentConjunct, composedMap);
			knowledgeGameObjectives.add(knowledgeGameObj_i);
			
//			UtilityMethods.debugBDDMethods(bdd, "objective is", currentConjunct);
//			UtilityMethods.debugBDDMethods(bdd, "translated objective is", knowledgeGameObj_i);
//			UtilityMethods.getUserInput();
		}
		UtilityMethods.duration(t0, "composite game structures and knowledge game objectives computed in");
		
		//reachability objectives 
		if(reachability){
			ArrayList<GameStructure> reachabilityGames = createReachabilityObjectivesAsGameStructures_partiallyObservable(bdd, 
						controlledAgents, POGSs, knowledgeGames, dim);
			System.out.println("reachability objectives created");
			for(int i=0;i<reachabilityGames.size(); i++){
				GameStructure rGame = reachabilityGames.get(i);
				GameStructure kGame = knowledgeGames.get(i);
				GameStructure newComposedGame = kGame.compose(rGame);
				composedGameStructures.add(newComposedGame);
				knowledgeGameObjectives.add(bdd.ref(bdd.getOne()));
			}
		}
		
		//prepare the projection cubes
		t0 = UtilityMethods.timeStamp();
		System.out.println("computing the projection cubes");
		ArrayList<Integer> projectionCubes = computeProjectionCubes(bdd, composedGameStructures);
		UtilityMethods.duration(t0, "projection cubes computed in");
		
		//form the games and solve them
		

		
		//enter the main loop of compositional synthesis
		

		
		ArrayList<Integer> controllers = new ArrayList<Integer>();
		int composedController=bdd.ref(bdd.getZero());
		ArrayList<Integer> projectedControllers = new ArrayList<Integer>();
		
		do{
			//TODO: somewhere I should clean up the solutions
			ArrayList<GameSolution> solutions = new ArrayList<GameSolution>();
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			for(int i = 0 ; i< knowledgeGameObjectives.size(); i++){
				GameSolution solution_i = GameSolver.solve(bdd, composedGameStructures.get(i), knowledgeGameObjectives.get(i)); 
				solutions.add(solution_i);
				//to check if there is an error
				if(solution_i.getWinner() == Player.ENVIRONMENT){
					solution_i.print();
				}
			}
			UtilityMethods.duration(t0, "games were solved in ");
			
			//TODO: clean up the controllers
			controllers = new ArrayList<Integer>();
			for(int i=0; i<solutions.size(); i++){
				controllers.add(solutions.get(i).getController());
			}
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			BDDWrapper.free(bdd, composedController);
			composedController = bdd.ref(bdd.getOne());
			for(int controller : controllers){
				composedController = BDDWrapper.andTo(bdd, composedController, controller);
			}
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("projecting controllers");
			t0=UtilityMethods.timeStamp();
			//TODO: clean up the projections 
			projectedControllers = new ArrayList<Integer>();
			for(int i=0; i<controllers.size(); i++){
				int projectedController = BDDWrapper.exists(bdd, composedController, projectionCubes.get(i));
				projectedControllers.add(projectedController);
			}
			UtilityMethods.duration(t0, "controllers projected in ");
			
			//check if a fixed point is reached
			boolean fixedPointReached = true;
			for(int i=0; i<controllers.size(); i++){
				int c = controllers.get(i);
				int pc = projectedControllers.get(i);
				if(pc != c){
					fixedPointReached = false;
					break;
				}
			}
			
			if(fixedPointReached){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
			for(int i=0; i<composedGameStructures.size(); i++){
				GameStructure currentGameStructure = composedGameStructures.get(i);
				GameStructure restrictedGameStructure = currentGameStructure.composeWithController(projectedControllers.get(i));
				restrictedGameStructures.add(restrictedGameStructure);
			}
			//TODO: clean up game structures
			composedGameStructures = restrictedGameStructures;
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		System.out.println();
		System.out.println();
//		String duration = "$"+ UtilityMethods.duration_string(t0_comp)+" $ & ";
		String duration = UtilityMethods.duration_string(t0_comp);
		UtilityMethods.duration(t0_comp, "compositional algorithm terminated in ");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		String numOfVarsInBDD = BDDWrapper.numOfVariablesInBDD(bdd)+"";
//		String bdd_mem_usage = "$" + BDDWrapper.BDD_memory_usage(bdd)+"$";
		String bdd_mem_usage = BDDWrapper.BDD_memory_usage(bdd);
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//update experiment results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		currentExperiment.add(numOfVarsInBDD);
		currentExperiment.add(numOfKVars+"");
		currentExperiment.add(duration);
		currentExperiment.add(bdd_mem_usage);
		
		//verifying the solution
//		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
//		allAgents.addAll(uncontrolledAgents);
//		allAgents.addAll(controlledAgents);
		
		//create central game
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
//		UtilityMethods.duration(t0, "central game was created in"); 
//		GameStructure restrictedGame = centralGame.composeWithController(composedController);
//		//create central objective
//		t0 = UtilityMethods.timeStamp();
//		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
//		UtilityMethods.duration(t0, "central objective was created in");
//		//verify the solution
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = restrictedGame.safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		
		return composedController;
	}
	
	
	public static ArrayList<Integer> computeProjectionCubes(BDD bdd, ArrayList<GameStructure> gameStructures ){
		//first compute the set of all variables in the knowledge game structures
//		Collection<GameStructure> kGameStructures = knowledgeGameStructuresForControlledAgents.values();
		Variable[] allKnowledgeGameVariablesAndActions = null;
		for(GameStructure kGS : gameStructures){
			Variable[] currentKGSVarsAndActions = Variable.unionVariables(kGS.variables, kGS.actionVars);
			allKnowledgeGameVariablesAndActions = Variable.unionVariables(allKnowledgeGameVariablesAndActions, 
							currentKGSVarsAndActions);
			
//			System.out.println("all variables");
//			Variable.printVariables(allKnowledgeGameVariablesAndActions);
//			UtilityMethods.getUserInput();
		}
				
		//next, for each composed game structure, find out which variables do not appear
		ArrayList<Integer> projectionCubes = new ArrayList<Integer>();
		for(int i=0; i<gameStructures.size(); i++){
			GameStructure currentComposedGameStructure = gameStructures.get(i);
			Variable[] vars = currentComposedGameStructure.variables;
			Variable[] actionVars = currentComposedGameStructure.actionVars;
			Variable[] varsAndActions = Variable.unionVariables(vars, actionVars);
			Variable[] notAppearing = Variable.difference(allKnowledgeGameVariablesAndActions, varsAndActions);
			int projectionCube_i = BDDWrapper.createCube(bdd, notAppearing);
			projectionCubes.add(projectionCube_i);
			
//			System.out.println("not appearing in game structure "+i);
//			Variable.printVariables(notAppearing);
//			UtilityMethods.debugBDDMethods(bdd, "projection cube", projectionCube_i);
//			UtilityMethods.getUserInput();
		}
		
		return projectionCubes;
	}
	
	public static int applyCompositionalGameSolvingMethod(BDD bdd, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives) throws Exception{
		
		long t0;
		long t0_comp = UtilityMethods.timeStamp();
		
		//analyze the conjuncts to check which controlled agents are involved for each
		t0 = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts = involvedAgentsInConjuncts(bdd, controlledAgents, objectives);
		UtilityMethods.duration(t0, "involved agents computed in");
		
		//TODO: different forms of grouping
//		//group the objectives based on above analysis
//		ArrayList<Integer> groupedObjectives;
//		//decide which game structures must be composed based on the above analysis
//		ArrayList<ArrayList<Agent2D>> involvedControlledAgentsSets;
		
		//compose the game structure based on involved agents in conjuncts
		t0 = UtilityMethods.timeStamp();
		ArrayList<GameStructure> composedGameStructures= new ArrayList<GameStructure>();
		for(ArrayList<Agent2D> involved : involvedAgentsInConjuncts){
			ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
			agents.addAll(uncontrolledAgents);
			agents.addAll(involved);
			composedGameStructures.add(GameStructure.createGameForAgents(bdd, agents));
		}
		UtilityMethods.duration(t0, "composite game structure computed in");
		
		//form the games and solve them
		
		//enter the main loop of compositional synthesis
		
		//create cubes for agents that are not involved for each conjunct
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> notInvolvedAgentsCubeForConjuncts = notInvolvedAgentsCubeForConjuncts(bdd, controlledAgents, involvedAgentsInConjuncts);
		UtilityMethods.duration(t0, "not involved agents cubes were computed in");
		
		ArrayList<Integer> controllers = new ArrayList<Integer>();
		int composedController=bdd.ref(bdd.getZero());
		ArrayList<Integer> projectedControllers = new ArrayList<Integer>();
		
		do{
			//TODO: somewhere I should clean up the solutions
			ArrayList<GameSolution> solutions = new ArrayList<GameSolution>();
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			for(int i = 0 ; i< objectives.size(); i++){
				GameSolution solution_i = GameSolver.solve(bdd, composedGameStructures.get(i), objectives.get(i)); 
				solutions.add(solution_i);
				//to check if there is an error
				if(solution_i.getWinner() == Player.ENVIRONMENT){
					solution_i.print();
				}
			}
			UtilityMethods.duration(t0, "games were solved in ");
			
			//TODO: clean up the controllers
			controllers = new ArrayList<Integer>();
			for(int i=0; i<solutions.size(); i++){
				controllers.add(solutions.get(i).getController());
			}
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			BDDWrapper.free(bdd, composedController);
			composedController = bdd.ref(bdd.getOne());
			for(int controller : controllers){
				composedController = BDDWrapper.andTo(bdd, composedController, controller);
			}
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("projecting controllers");
			t0=UtilityMethods.timeStamp();
			//TODO: clean up the projections 
			projectedControllers = new ArrayList<Integer>();
			for(int i=0; i<controllers.size(); i++){
				int projectedController = BDDWrapper.exists(bdd, composedController, notInvolvedAgentsCubeForConjuncts.get(i));
				projectedControllers.add(projectedController);
			}
			UtilityMethods.duration(t0, "controllers projected in ");
			
			//check if a fixed point is reached
			boolean fixedPointReached = true;
			for(int i=0; i<controllers.size(); i++){
				int c = controllers.get(i);
				int pc = projectedControllers.get(i);
				if(pc != c){
					fixedPointReached = false;
					break;
				}
			}
			
			if(fixedPointReached){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
			for(int i=0; i<composedGameStructures.size(); i++){
				GameStructure currentGameStructure = composedGameStructures.get(i);
				GameStructure restrictedGameStructure = currentGameStructure.composeWithController(projectedControllers.get(i));
				restrictedGameStructures.add(restrictedGameStructure);
			}
			//TODO: clean up game structures
			composedGameStructures = restrictedGameStructures;
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		System.out.println();
		System.out.println();
		UtilityMethods.duration(t0_comp, "compositional algorithm terminated in ");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//verifying the solution
//		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
//		allAgents.addAll(uncontrolledAgents);
//		allAgents.addAll(controlledAgents);
		
		//create central game
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
//		UtilityMethods.duration(t0, "central game was created in"); 
//		GameStructure restrictedGame = centralGame.composeWithController(composedController);
//		//create central objective
//		t0 = UtilityMethods.timeStamp();
//		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
//		UtilityMethods.duration(t0, "central objective was created in");
//		//verify the solution
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = restrictedGame.safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		
		return composedController;
	}
	
	/**
	 * assumes that reachability objectives are given as safety games one for each controlled agent, generalize
	 * @param bdd
	 * @param uncontrolledAgents
	 * @param controlledAgents
	 * @param objectives
	 * @param reachabilityObjectives
	 * @return
	 * @throws Exception
	 */
	public static int applyCompositionalGameSolvingMethod_optimized(BDD bdd, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives, 
			ArrayList<GameStructure> reachabilityObjectives) throws Exception{
		
		long t0;
		long t0_comp = UtilityMethods.timeStamp();
		
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		//analyze the conjuncts to check which controlled agents are involved for each
		t0 = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts = involvedAgentsInConjuncts(bdd, allAgents, objectives);
		UtilityMethods.duration(t0, "involved agents computed in");
		
		//TODO: different forms of grouping
//		//group the objectives based on above analysis
//		ArrayList<Integer> groupedObjectives;
//		//decide which game structures must be composed based on the above analysis
//		ArrayList<ArrayList<Agent2D>> involvedControlledAgentsSets;
		
		//compose the game structure based on involved agents in conjuncts
		t0 = UtilityMethods.timeStamp();
		ArrayList<GameStructure> composedGameStructures= new ArrayList<GameStructure>();
		for(ArrayList<Agent2D> involved : involvedAgentsInConjuncts){
			ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
			agents.addAll(uncontrolledAgents);
			agents.addAll(involved);
			composedGameStructures.add(GameStructure.createGameForAgents(bdd, agents));
		}
		
		//now for reachability objectives
		//special case, generalize later 
		if(reachabilityObjectives != null){
			for(int i=0; i< reachabilityObjectives.size(); i++){
				ArrayList<Agent2D> involved = new ArrayList<Agent2D>();
				involved.add(controlledAgents.get(i));
				GameStructure agentGS = GameStructure.createGameForAgents(bdd, involved);
				GameStructure rGameStructure = reachabilityObjectives.get(i).compose(agentGS);
				
				composedGameStructures.add(rGameStructure);
				objectives.add(bdd.ref(bdd.getOne()));
			}
		}
		UtilityMethods.duration(t0, "composite game structure computed in");
		
		//form the games and solve them
		
		//enter the main loop of compositional synthesis
		
		//prepare the projection cubes
		t0 = UtilityMethods.timeStamp();
		System.out.println("computing the projection cubes");
		ArrayList<Integer> projectionCubes = computeProjectionCubes(bdd, composedGameStructures);
		UtilityMethods.duration(t0, "projection cubes computed in");
		
		ArrayList<Integer> controllers = new ArrayList<Integer>();
		int composedController=bdd.ref(bdd.getZero());
		ArrayList<Integer> projectedControllers = new ArrayList<Integer>();
		
		do{
			//TODO: somewhere I should clean up the solutions
			ArrayList<GameSolution> solutions = new ArrayList<GameSolution>();
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			for(int i = 0 ; i< objectives.size(); i++){
				GameSolution solution_i = GameSolver.solve(bdd, composedGameStructures.get(i), objectives.get(i)); 
				solutions.add(solution_i);
				//to check if there is an error
				if(solution_i.getWinner() == Player.ENVIRONMENT){
					solution_i.print();
				}
			}
			UtilityMethods.duration(t0, "games were solved in ");
			
			//TODO: clean up the controllers
			controllers = new ArrayList<Integer>();
			for(int i=0; i<solutions.size(); i++){
				controllers.add(solutions.get(i).getController());
			}
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			BDDWrapper.free(bdd, composedController);
			composedController = bdd.ref(bdd.getOne());
			for(int controller : controllers){
				composedController = BDDWrapper.andTo(bdd, composedController, controller);
			}
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("projecting controllers");
			t0=UtilityMethods.timeStamp();
			//TODO: clean up the projections 
			projectedControllers = new ArrayList<Integer>();
			for(int i=0; i<controllers.size(); i++){
				int projectedController = BDDWrapper.exists(bdd, composedController, projectionCubes.get(i));
				projectedControllers.add(projectedController);
			}
			UtilityMethods.duration(t0, "controllers projected in ");
			
			//check if a fixed point is reached
			boolean fixedPointReached = true;
			for(int i=0; i<controllers.size(); i++){
				int c = controllers.get(i);
				int pc = projectedControllers.get(i);
				if(pc != c){
					fixedPointReached = false;
					break;
				}
			}
			
			if(fixedPointReached){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
			for(int i=0; i<composedGameStructures.size(); i++){
				GameStructure currentGameStructure = composedGameStructures.get(i);
				GameStructure restrictedGameStructure = currentGameStructure.composeWithController(projectedControllers.get(i));
				restrictedGameStructures.add(restrictedGameStructure);
			}
			//TODO: clean up game structures
			composedGameStructures = restrictedGameStructures;
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		System.out.println();
		System.out.println();
		String duration = UtilityMethods.duration_string(t0_comp);
		UtilityMethods.duration(t0_comp, "compositional algorithm terminated in ");
		System.out.println();
		System.out.println();
		
		System.out.println();
		System.out.println();
		String numOfVarsInBDD = BDDWrapper.numOfVariablesInBDD(bdd)+"";
		String bdd_mem_usage = BDDWrapper.BDD_memory_usage(bdd);
		BDDWrapper.BDD_Usage(bdd);
		System.out.println();
		System.out.println();
		
		//update experiment results 
		ArrayList<String> currentExperiment = results.get(currentIndex);
		currentExperiment.add(numOfVarsInBDD);
		currentExperiment.add(duration);
		currentExperiment.add(bdd_mem_usage);
		
		//verifying the solution
//		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
//		allAgents.addAll(uncontrolledAgents);
//		allAgents.addAll(controlledAgents);
		
		//create central game
//		System.out.println("\n\n******************Verification******************");
//		t0 = UtilityMethods.timeStamp();
//		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
//		UtilityMethods.duration(t0, "central game was created in"); 
//		GameStructure restrictedGame = centralGame.composeWithController(composedController);
//		//create central objective
//		t0 = UtilityMethods.timeStamp();
//		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
//		UtilityMethods.duration(t0, "central objective was created in");
//		//verify the solution
//		t0 = UtilityMethods.timeStamp();
//		boolean modelCheck = restrictedGame.safetyModelCheck(objective);
//		if(modelCheck){
//			System.out.println("computed solution is verified");
//		}else{
//			System.out.println("computed solution did not pass the verification!");
//		}
//		UtilityMethods.duration(t0, "verification done in");
		
		
		return composedController;
	}
	
	public static void apply_OLD_CompositionalGameSolvingMethod(BDD bdd, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, ArrayList<Integer> objectives){
		long t0;
		long t0_comp = UtilityMethods.timeStamp();
		
		//analyze the conjuncts to check which controlled agents are involved for each
		t0 = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts = involvedAgentsInConjuncts(bdd, controlledAgents, objectives);
		UtilityMethods.duration(t0, "involved agents computed in");
		
		//compose the game structure based on involved agents in conjuncts
		t0 = UtilityMethods.timeStamp();
		ArrayList<GameStructure> composedGameStructures= new ArrayList<GameStructure>();
		for(ArrayList<Agent2D> involved : involvedAgentsInConjuncts){
			ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
			agents.addAll(uncontrolledAgents);
			agents.addAll(involved);
			composedGameStructures.add(GameStructure.createGameForAgents(bdd, agents));
		}
		UtilityMethods.duration(t0, "composite game structure computed in");
		
		//form the games and solve them
		
		//enter the main loop of compositional synthesis
		
		//create cubes for agents that are not involved for each conjunct
		t0 = UtilityMethods.timeStamp();
		ArrayList<Integer> notInvolvedAgentsCubeForConjuncts = notInvolvedAgentsCubeForConjuncts(bdd, controlledAgents, involvedAgentsInConjuncts);
		UtilityMethods.duration(t0, "not involved agents cubes were computed in");
		
		ArrayList<Integer> controllers = new ArrayList<Integer>();
		int composedController=bdd.ref(bdd.getZero());
		ArrayList<Integer> projectedControllers = new ArrayList<Integer>();
		
		do{
			//TODO: somewhere I should clean up the solutions
			ArrayList<GameSolution> solutions = new ArrayList<GameSolution>();
			System.out.println("Solving the games");
			t0 = UtilityMethods.timeStamp();
			for(int i = 0 ; i< objectives.size(); i++){
				solutions.add(GameSolver.solve(bdd, composedGameStructures.get(i), objectives.get(i)));
			}
			UtilityMethods.duration(t0, "games were solved in ");
			
			t0 = UtilityMethods.timeStamp();
			GameStructure composedGameStructure = solutions.get(0).strategyOfTheWinner();
			for(int i=1; i<solutions.size(); i++){
				composedGameStructure = composedGameStructure.compose(solutions.get(i).strategyOfTheWinner());
			}
			UtilityMethods.duration(t0, "composing the restricted game structures done in");
			
			//Execute a backward symbolic step on the composed game structure
			t0 = UtilityMethods.timeStamp();
			int composedWinningRegion = composedGameStructure.symbolicOneStepBackwardExecution(bdd.getOne());
			UtilityMethods.duration(t0, "composed winning region computed in ");
			UtilityMethods.getUserInput();
			
			//TODO: clean up the controllers
			controllers = new ArrayList<Integer>();
			for(int i=0; i<solutions.size(); i++){
				controllers.add(solutions.get(i).getController());
			}
			
			System.out.println("composing the controllers");
			t0 = UtilityMethods.timeStamp();
			BDDWrapper.free(bdd, composedController);
			composedController = bdd.ref(bdd.getOne());
			for(int controller : controllers){
				composedController = BDDWrapper.andTo(bdd, composedController, controller);
			}
			UtilityMethods.duration(t0, "Controllers were composed in ");
			
			System.out.println("projecting controllers");
			t0=UtilityMethods.timeStamp();
			//TODO: clean up the projections 
			projectedControllers = new ArrayList<Integer>();
			for(int i=0; i<controllers.size(); i++){
				int projectedController = BDDWrapper.exists(bdd, composedController, notInvolvedAgentsCubeForConjuncts.get(i));
				projectedControllers.add(projectedController);
			}
			UtilityMethods.duration(t0, "controllers projected in ");
			
			//check if a fixed point is reached
			boolean fixedPointReached = true;
			for(int i=0; i<controllers.size(); i++){
				int c = controllers.get(i);
				int pc = projectedControllers.get(i);
				if(pc != c){
					fixedPointReached = false;
					break;
				}
			}
			
			if(fixedPointReached){
				System.out.println("A fixed point on strategies is reached");
				break;
			}
			
			System.out.println("restricting the games");
			t0 = UtilityMethods.timeStamp();
			ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
			for(int i=0; i<composedGameStructures.size(); i++){
				GameStructure currentGameStructure = composedGameStructures.get(i);
				GameStructure restrictedGameStructure = currentGameStructure.composeWithController(projectedControllers.get(i));
				restrictedGameStructures.add(restrictedGameStructure);
			}
			//TODO: clean up game structures
			composedGameStructures = restrictedGameStructures;
			UtilityMethods.duration(t0, "restricted games were computed in");
		}while(true);
		
		UtilityMethods.duration(t0_comp, "compositional algorithm terminated in ");
		
		//verifying the solution
		ArrayList<Agent2D> allAgents = new ArrayList<Agent2D>();
		allAgents.addAll(uncontrolledAgents);
		allAgents.addAll(controlledAgents);
		
		//create central game
		System.out.println("\n\n******************Verification******************");
		t0 = UtilityMethods.timeStamp();
		GameStructure centralGame = GameStructure.createGameForAgents(bdd, allAgents);
		UtilityMethods.duration(t0, "central game was created in"); 
		GameStructure restrictedGame = centralGame.composeWithController(composedController);
		//create central objective
		t0 = UtilityMethods.timeStamp();
		int objective = UtilityMethods.computeConjunctiveObjective(bdd, objectives);
		UtilityMethods.duration(t0, "central objective was created in");
		//verify the solution
		t0 = UtilityMethods.timeStamp();
		boolean modelCheck = restrictedGame.safetyModelCheck(objective);
		if(modelCheck){
			System.out.println("computed solution is verified");
		}else{
			System.out.println("computed solution did not pass the verification!");
		}
		UtilityMethods.duration(t0, "verification done in");
	}
	
//	/**
//	 * Checks if involved.get(index) has already appeared before in the list and return the index of occurrence, otherwise return -1 
//	 * @param agents
//	 * @param index
//	 * @return
//	 */
//	private static int gameStructureAlreadyConstructed(ArrayList<ArrayList<Agent2D>> involved, int index){
//		ArrayList<Agent2D> goalAgentsList = involved.get(index);
//		int result = -1;
//		for(int i=0; i<index; i++){
//			ArrayList<Agent2D> currentAgentsList = involved.get(i);
//			
//		}
//	}
	
	public static ArrayList<Integer> notInvolvedAgentsCubeForConjuncts(BDD bdd, ArrayList<Agent2D> agents, 
			ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i=0; i< involvedAgentsInConjuncts.size(); i++){
			ArrayList<Agent2D> involved = involvedAgentsInConjuncts.get(i);
			//obtain the agents that are not involved
			ArrayList<Agent2D> notInvolved = new ArrayList<Agent2D>();
			for(Agent2D agent : agents){
				if(!involved.contains(agent)){
					notInvolved.add(agent);
				}
			}
			//create the cube for agents that are not involved
			//TODO: compute the agents cube beforehand to avoid repetition
			int cube = bdd.ref(bdd.getOne());
			for(Agent2D agent : notInvolved){
				int agentCube = BDDWrapper.createCube(bdd, Variable.unionVariables(agent.getVariables(), agent.getActionVars()));
				cube = BDDWrapper.andTo(bdd, cube, agentCube);
				BDDWrapper.free(bdd, agentCube);
			}
			result.add(cube);
		}
		
		return result;
	}
	
	/**
	 * TODO: handle special case where objective is true, or does not involve any of the controlled agents
	 * @param bdd
	 * @param controlledAgents
	 * @param objectives
	 * @return
	 */
	public static ArrayList<ArrayList<Agent2D>> involvedAgentsInConjuncts(BDD bdd, ArrayList<Agent2D> agents, ArrayList<Integer> objectives){
		ArrayList<ArrayList<Agent2D>> result = new ArrayList<ArrayList<Agent2D>>();
		for(int i=0; i<objectives.size(); i++){
			ArrayList<Agent2D> involved = analyzeConjunctForInvolvedAgents(bdd, agents, objectives.get(i));
			result.add(involved);
		}
		return result;
	}
	
	public static ArrayList<Agent2D> analyzeConjunctForInvolvedAgents(BDD bdd, ArrayList<Agent2D> agents, int conjunct){
		ArrayList<Agent2D> result = new ArrayList<Agent2D>();
		
		//for each agent, check if its variables appears in the conjunct's formula
		for(Agent2D agent : agents){
			//form the variable cube for the agent
			int cube = BDDWrapper.createCube(bdd, agent.getVariables());
			//existentially quantify the formula
			int exist = BDDWrapper.exists(bdd, conjunct, cube);
			//if the existentially quantify formula is the same as the original formula, the agent is not involved, otherwise it is
			if(exist != conjunct){
				result.add(agent);
			}
			BDDWrapper.free(bdd, cube);
			BDDWrapper.free(bdd, exist);
		}
		
		return result;
	}
	
	public static ArrayList<Agent2D> createAgents(BDD bdd, int dim, String namePrefix, AgentType type, ArrayList<GridCell2D> initCells){
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		
		int numberOfAgents = initCells.size();
		
		//create uncontrolled agents
		for(int i=0; i<numberOfAgents; i++){
			String agentName = namePrefix+i;
			Agent2D agent = UtilityTransitionRelations.createSimpleRobotMovingUpAndDown(bdd, dim, agentName, type, initCells.get(i));
			agents.add(agent);
		}
		return agents;
	}
	
	public static ArrayList<Agent2D> createAgentsWithStayPutAction(BDD bdd, int dim, String namePrefix, AgentType type, ArrayList<GridCell2D> initCells){
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		
		int numberOfAgents = initCells.size();
		
		//create uncontrolled agents
		for(int i=0; i<numberOfAgents; i++){
			String agentName = namePrefix+i;
			Agent2D agent = UtilityTransitionRelations.createSimpleRobotWithStayPut(bdd, dim, agentName, type, initCells.get(i));
			agents.add(agent);
		}
		return agents;
	}
	
	private static ArrayList<Integer> createNoCollisionObjectives(BDD bdd, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents){
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		
		//no collision between uncontrolled and controlled agents
		for(Agent2D uncontrolledAgent : uncontrolledAgents){
			for(Agent2D controlledAgent : controlledAgents){
				int safetyObj = UtilityFormulas.noCollisionObjective(bdd, uncontrolledAgent.getXVars(), uncontrolledAgent.getYVars(), 
						controlledAgent.getXVars(), controlledAgent.getYVars());
				//do not add if the objective is trivial
				if(safetyObj != 1) objectives.add(safetyObj);
			}
		}
		
		//no collision between controlled agents
		for(int i=0; i<controlledAgents.size(); i++){
			Agent2D controlledAgent1=controlledAgents.get(i);
			for(int j=i+1; j<controlledAgents.size(); j++){
				Agent2D controlledAgent2 = controlledAgents.get(j);
				int safetyObj = UtilityFormulas.noCollisionObjective(bdd, controlledAgent1.getXVars(), controlledAgent1.getYVars(), 
						controlledAgent2.getXVars(), controlledAgent2.getYVars());
				if(safetyObj != 1) objectives.add(safetyObj);
			}
		}
		
		
//		System.out.println("printing no collision objectives");
//		printObjectives(bdd, objectives);
//		UtilityMethods.getUserInput();
		
		return objectives;
	}
	
	private static void printObjectives(BDD bdd, ArrayList<Integer> objectives){
		for(Integer obj : objectives){
			UtilityMethods.debugBDDMethods(bdd, "a conjunct of objectives", obj);
		}
	}
	
	private static ArrayList<Integer> createSafetyObjectives(BDD bdd, int dim, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents, 
			boolean collisionAvoidance, boolean staticObstaclesCollisionAvoidance, boolean formationControl){
		//the objective of the system is the conjunction of the objectives in the arraylist objectives
		ArrayList<Integer> objectives = new ArrayList<Integer>();
				
		//collision avoidance
		if(collisionAvoidance){
			objectives.addAll(createNoCollisionObjectives(bdd, uncontrolledAgents, controlledAgents));
		}
		
		//static obstacles
		//only define static obstacles if the dimension of the grid-world is greater than 8
		if(staticObstaclesCollisionAvoidance && dim>=8){
			ArrayList<GridCell2D> staticObstacles = defineStaticObstacles(dim);
			objectives.addAll(createNoCollisionWithStaticObstaclesObjective(bdd, controlledAgents, staticObstacles));
		}
		
		//formation control
		if(formationControl){
			objectives.addAll(createRectangularOrLinearFormationObjectives(bdd, controlledAgents));
		}
		
//		System.out.println("printing the objective");
//		printObjectives(bdd, objectives);
//		UtilityMethods.getUserInput();
		
		return objectives;
	}
	
	private static ArrayList<Integer> createNoCollisionWithStaticObstaclesObjective(BDD bdd, ArrayList<Agent2D> agents, ArrayList<GridCell2D> staticObstacles){
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		for(Agent2D agent : agents){
			int obj = UtilityFormulas.noCollisionWithStaticObstacles(bdd, agent, staticObstacles);
			if(obj != 1) objectives.add(obj);
		}
		
//		System.out.println("static obstacles are located at");
//		for(GridCell2D cell : staticObstacles){
//			cell.print();
//		}
//		
//		System.out.println("printing no collision with static obstacles");
//		printObjectives(bdd, objectives);
//		UtilityMethods.getUserInput();
		
		return objectives;
	}
	
	private static ArrayList<GridCell2D> defineStaticObstacles(int dim){
		ArrayList<GridCell2D> staticObstacles = new ArrayList<GridCell2D>();
		
		int lowerBound = dim/3;
		int upperBound = 2*dim/3;
		
		for(int i=lowerBound+1; i<upperBound;i++){
			staticObstacles.add(new GridCell2D(0, i));
			staticObstacles.add(new GridCell2D(1, i));
			
			staticObstacles.add(new GridCell2D((dim-1)/2 , i ));
			staticObstacles.add(new GridCell2D((dim-1)/2+1, i ));
			
			staticObstacles.add(new GridCell2D(dim-2 , i ));
			staticObstacles.add(new GridCell2D(dim-1 , i ));
		}
		
		return staticObstacles;
	}
	
	//TODO: make it truly rectangular 
	private static ArrayList<Integer> createRectangularOrLinearFormationObjectives(BDD bdd, ArrayList<Agent2D> agents){
		ArrayList<Integer> formationObjectives = new ArrayList<Integer>();
		
		int numOfAgents = agents.size();
		
		//TODO: -1 was added after agents.size() in the loop termination
		for(int i=0 ; i<agents.size()-1; i++){
			Agent2D agent1 = agents.get(i);
			Agent2D agent2 = agents.get((i+1)%numOfAgents);
			int xFormation = BDDWrapper.same(bdd, agent1.getXVars(), agent2.getXVars());
			int yFormation = BDDWrapper.same(bdd, agent1.getYVars(), agent2.getYVars());
			int form = bdd.ref(bdd.or(xFormation, yFormation));
			if(form != 1) formationObjectives.add(form);
			bdd.deref(xFormation);
			bdd.deref(yFormation);
		}
		
//		System.out.println("printing the formation control objective");
//		printObjectives(bdd, formationObjectives);
//		UtilityMethods.getUserInput();
		
		return formationObjectives;
	}
	
	/**
	 * Creates a single reachability objective for each controlled agent.
	 * @param bdd
	 * @param agents
	 */
	private static void createReachabilityObjectives(BDD bdd, ArrayList<Agent2D> agents, int dim){
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		for(Agent2D agent : agents ){
			bounds.add(dim+3);
			int reachabilityObjective = BDDWrapper.assign(bdd, dim-1, agent.getYVars());
			objectives.add(reachabilityObjective);
		}
		
//		System.out.println("printing reachability objectives");
//		printObjectives(bdd, objectives);
//		System.out.println("Printing bounds");
//		UtilityMethods.printArrayList(bounds);
//		UtilityMethods.getUserInput();
		
//		System.out.println("printing agents before update");
//		for(Agent2D agent : agents){
//			agent.print();
//			UtilityMethods.getUserInput();
//		}
		
		updateControlledAgentsTransitionsBasedOnReachabilityObjectives(bdd, agents, objectives, bounds);
		
//		System.out.println("printing agents after update");
//		for(Agent2D agent : agents){
//			agent.print();
//			UtilityMethods.getUserInput();
//		}
	}
	
	/**
	 * Creates reachability objectives for agents as game structures
	 * @param bdd
	 * @param agents
	 * @param dim
	 * @return
	 */
	private static ArrayList<GameStructure> createReachabilityObjectivesAsGameStructures(BDD bdd, ArrayList<Agent2D> agents, 
			int dim){
		ArrayList<GameStructure> reachabilityGameStructures = new ArrayList<GameStructure>();
		
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		ArrayList<Integer> bounds = new ArrayList<Integer>();
		for(Agent2D agent : agents ){
			int bound = dim+3;
			int reachabilityObjective = BDDWrapper.assign(bdd, dim-1, agent.getYVars());
			
			String namePrefix = agent.getName()+"_reachability_";
			
			Permutation p = BDDWrapper.createPermutations(bdd, agent.getVariables(), agent.getPrimeVariables());
			int reachabilityObjectivePrime = BDDWrapper.replace(bdd, reachabilityObjective, p);
			
			//define components of the reachability objective
			Variable flag = UtilityMethods.createBoundedReachabilityFlag(bdd, namePrefix+"flag");
			Variable[] counter = UtilityMethods.createBoundedReachabilityCounter(bdd, bound, namePrefix+"Counter");
			int reachabilityInit = UtilityMethods.boundedReachabilityInit(bdd, reachabilityObjective, flag, counter);
			int reachabilityTrans = UtilityMethods.boundedReachabilityTransitions(bdd, bound, reachabilityObjective, reachabilityObjectivePrime, counter, flag);
			
			
			
			//update the agent variables
			Variable[] flagArray = new Variable[]{flag};
			Variable[] reachabilityVars = Variable.unionVariables(counter, flagArray);
			reachabilityVars =Variable.unionVariables(reachabilityVars, agent.getVariables());
			
			int sameCounter=BDDWrapper.same(bdd, counter, Variable.getPrimedCopy(counter));
			int sameFlag=bdd.ref(bdd.biimp(flag.getBDDVar(), flag.getPrimedCopy().getBDDVar()));
			int sameReachTrans=bdd.ref(bdd.and(sameCounter, sameFlag));
			
			GameStructure reachabilityGS = new GameStructure(bdd, reachabilityVars, Variable.getPrimedCopy(reachabilityVars), reachabilityInit, sameReachTrans , reachabilityTrans, null, agent.getActionVars());
			reachabilityGameStructures.add(reachabilityGS);
		}
		
		return reachabilityGameStructures;
	}
	
	private static ArrayList<GameStructure> createReachabilityObjectivesAsGameStructures_partiallyObservable(BDD bdd, 
			ArrayList<Agent2D> agents, 
			ArrayList<TurnBasedPartiallyObservableGameStructure> POGSs, 
			ArrayList<GameStructure> knowledgeGameStructures,
			int dim){
		ArrayList<GameStructure> reachabilityGameStructures = new ArrayList<GameStructure>();
		
		if(POGSs.size() != agents.size()){
			System.err.println("error in create reachability partial");
			return null;
		}
		
		for(int i=0; i<agents.size(); i++){
			Agent2D agent = agents.get(i);
			TurnBasedPartiallyObservableGameStructure pogs = POGSs.get(i);
			GameStructure kGS = knowledgeGameStructures.get(i);
			
			int bound = dim+3;
			int reachabilityObjective = BDDWrapper.assign(bdd, dim-1, agent.getYVars());
			
			int kReachObj = pogs.translateConcreteObjectiveToKnowledgeGameObjective(reachabilityObjective);
			
			
			
			String namePrefix = agent.getName()+"_reachability_";
			
			Permutation p = kGS.getVtoVprime();
			int kReachObjPrime = BDDWrapper.replace(bdd, kReachObj, p);
			
//			Permutation p = BDDWrapper.createPermutations(bdd, kGS.variables, agent.getPrimeVariables());
//			int reachabilityObjectivePrime = BDDWrapper.replace(bdd, reachabilityObjective, p);
			
			//define components of the reachability objective
			Variable flag = UtilityMethods.createBoundedReachabilityFlag(bdd, namePrefix+"flag");
			Variable[] counter = UtilityMethods.createBoundedReachabilityCounter(bdd, bound, namePrefix+"Counter");
			int reachabilityInit = UtilityMethods.boundedReachabilityInit(bdd, reachabilityObjective, flag, counter);
			int reachabilityTrans = UtilityMethods.boundedReachabilityTransitions(bdd, bound, kReachObj, kReachObjPrime, counter, flag);
			
			
			
			//update the agent variables
			Variable[] flagArray = new Variable[]{flag};
			Variable[] reachabilityVars = Variable.unionVariables(counter, flagArray);
			reachabilityVars =Variable.unionVariables(reachabilityVars, kGS.variables);
			
			int sameCounter=BDDWrapper.same(bdd, counter, Variable.getPrimedCopy(counter));
			int sameFlag=bdd.ref(bdd.biimp(flag.getBDDVar(), flag.getPrimedCopy().getBDDVar()));
			int sameReachTrans=bdd.ref(bdd.and(sameCounter, sameFlag));
			
			GameStructure reachabilityGS = new GameStructure(bdd, reachabilityVars, Variable.getPrimedCopy(reachabilityVars), reachabilityInit, sameReachTrans , reachabilityTrans, null, agent.getActionVars());
			reachabilityGameStructures.add(reachabilityGS);
		}
		
		return reachabilityGameStructures;
	}
	
	private static ArrayList<GameStructure> createReachabilityObjectivesAsGameStructures_partiallyObservable(BDD bdd, 
			ArrayList<Agent2D> agents, 
			TurnBasedPartiallyObservableGameStructure POGS, 
			GameStructure knowledgeGameStructure,
			int dim){
		ArrayList<GameStructure> reachabilityGameStructures = new ArrayList<GameStructure>();
		
		
		for(int i=0; i<agents.size(); i++){
			Agent2D agent = agents.get(i);
			TurnBasedPartiallyObservableGameStructure pogs = POGS;
			GameStructure kGS = knowledgeGameStructure;
			
			int bound = dim+3;
			int reachabilityObjective = BDDWrapper.assign(bdd, dim-1, agent.getYVars());
			
			int kReachObj = pogs.translateConcreteObjectiveToKnowledgeGameObjective(reachabilityObjective);
			
			
			
			String namePrefix = agent.getName()+"_reachability_";
			
			Permutation p = kGS.getVtoVprime();
			int kReachObjPrime = BDDWrapper.replace(bdd, kReachObj, p);
			
//			Permutation p = BDDWrapper.createPermutations(bdd, kGS.variables, agent.getPrimeVariables());
//			int reachabilityObjectivePrime = BDDWrapper.replace(bdd, reachabilityObjective, p);
			
			//define components of the reachability objective
			Variable flag = UtilityMethods.createBoundedReachabilityFlag(bdd, namePrefix+"flag");
			Variable[] counter = UtilityMethods.createBoundedReachabilityCounter(bdd, bound, namePrefix+"Counter");
			int reachabilityInit = UtilityMethods.boundedReachabilityInit(bdd, reachabilityObjective, flag, counter);
			int reachabilityTrans = UtilityMethods.boundedReachabilityTransitions(bdd, bound, kReachObj, kReachObjPrime, counter, flag);
			
			
			
			//update the agent variables
			Variable[] flagArray = new Variable[]{flag};
			Variable[] reachabilityVars = Variable.unionVariables(counter, flagArray);
			reachabilityVars =Variable.unionVariables(reachabilityVars, kGS.variables);
			
			int sameCounter=BDDWrapper.same(bdd, counter, Variable.getPrimedCopy(counter));
			int sameFlag=bdd.ref(bdd.biimp(flag.getBDDVar(), flag.getPrimedCopy().getBDDVar()));
			int sameReachTrans=bdd.ref(bdd.and(sameCounter, sameFlag));
			
			GameStructure reachabilityGS = new GameStructure(bdd, reachabilityVars, Variable.getPrimedCopy(reachabilityVars), reachabilityInit, sameReachTrans , reachabilityTrans, null, agent.getActionVars());
			reachabilityGameStructures.add(reachabilityGS);
		}
		
		return reachabilityGameStructures;
	}
	
	private static void createReachabilityObjectives(BDD bdd, ArrayList<Agent2D> agents, int dim, int bound, Variable[] flags, 
			ArrayList<Variable[]> counters){
		ArrayList<Integer> objectives = new ArrayList<Integer>();
		for(Agent2D agent : agents ){
			int reachabilityObjective = BDDWrapper.assign(bdd, dim-1, agent.getYVars());
			objectives.add(reachabilityObjective);
		}
		
		updateControlledAgentsTransitionsBasedOnReachabilityObjectives(bdd, agents, objectives, bound, flags, counters);
	}
	
	private static ArrayList<Agent2D> updateControlledAgentsTransitionsBasedOnReachabilityObjectives(BDD bdd, ArrayList<Agent2D> agents, 
			ArrayList<Integer> reachabilityObjectives, ArrayList<Integer> bounds){
		//the reachability objectives and bounds must have the same length
		if(reachabilityObjectives.size() != bounds.size()){
			System.err.println("reachability objectives and bounds must have the same size, agents did not get updated!!");
			return agents;
		}
		
		for(int i=0; i<reachabilityObjectives.size(); i++){
			Integer rObj = reachabilityObjectives.get(i);
			int bound = bounds.get(i);
			ArrayList<Agent2D> involved = analyzeConjunctForInvolvedAgents(bdd, agents, rObj);
			for(Agent2D agent : involved){
				String namePrefix = agent.getName()+"_"+i+"_";
				updateAgentForReachabilityObjective(bdd, agent, rObj, bound, namePrefix);
			}
		}
		
		return agents;
	}
	
	private static ArrayList<Agent2D> updateControlledAgentsTransitionsBasedOnReachabilityObjectives(BDD bdd, ArrayList<Agent2D> agents, 
			ArrayList<Integer> reachabilityObjectives, int bound,Variable[] flags, ArrayList<Variable[]> counters){	
		for(int i=0; i<reachabilityObjectives.size(); i++){
			Integer rObj = reachabilityObjectives.get(i);
			updateAgentForReachabilityObjective(bdd, agents.get(i), rObj, bound, flags[i], counters.get(i));
		}
		
		return agents;
	}
	
	private static void updateAgentForReachabilityObjective(BDD bdd, Agent2D agent, int reachabilityObjective, int bound, String namePrefix){
		//TODO: assumes that each reachability objective only refers to one single agent, extend to general case
		Permutation p = BDDWrapper.createPermutations(bdd, agent.getVariables(), agent.getPrimeVariables());
		int reachabilityObjectivePrime = BDDWrapper.replace(bdd, reachabilityObjective, p);
		
		//define components of the reachability objective
		Variable flag = UtilityMethods.createBoundedReachabilityFlag(bdd, namePrefix+"flag");
		Variable[] counter = UtilityMethods.createBoundedReachabilityCounter(bdd, bound, namePrefix+"Counter");
		int reachabilityInit = UtilityMethods.boundedReachabilityInit(bdd, reachabilityObjective, flag, counter);
		int reachabilityTrans = UtilityMethods.boundedReachabilityTransitions(bdd, bound, reachabilityObjective, reachabilityObjectivePrime, counter, flag);
		
		//update the agent variables
		Variable[] flagArray = new Variable[]{flag};
		Variable[] newVars = Variable.unionVariables(counter, flagArray);
		agent.addVariables(newVars);
		
		//update agent's init
		int newInit = BDDWrapper.and(bdd, agent.getInit(), reachabilityInit);
		agent.setInitAndFreeOldInit(newInit);
		BDDWrapper.free(bdd, reachabilityInit);
		
		//update agent transition relation
		int newTrans = BDDWrapper.and(bdd, agent.getTransitionRelation(), reachabilityTrans);
		agent.setTransitionRelationAndFreeOldTransitionRelation(newTrans);
		BDDWrapper.free(bdd, reachabilityTrans);
	}
	
	private static void updateAgentForReachabilityObjective(BDD bdd, Agent2D agent, int reachabilityObjective, int bound, Variable flag, Variable[] counter){
		//TODO: assumes that each reachability objective only refers to one single agent, extend to general case
		Permutation p = BDDWrapper.createPermutations(bdd, agent.getVariables(), agent.getPrimeVariables());
		int reachabilityObjectivePrime = BDDWrapper.replace(bdd, reachabilityObjective, p);
		
		//define components of the reachability objective
		int reachabilityInit = UtilityMethods.boundedReachabilityInit(bdd, reachabilityObjective, flag, counter);
		int reachabilityTrans = UtilityMethods.boundedReachabilityTransitions(bdd, bound, reachabilityObjective, reachabilityObjectivePrime, counter, flag);
		
		//update the agent variables
		Variable[] flagArray = new Variable[]{flag};
		Variable[] newVars = Variable.unionVariables(counter, flagArray);
		agent.addVariables(newVars);
		
		//update agent's init
		int newInit = BDDWrapper.and(bdd, agent.getInit(), reachabilityInit);
		agent.setInitAndFreeOldInit(newInit);
		BDDWrapper.free(bdd, reachabilityInit);
		
		//update agent transition relation
		int newTrans = BDDWrapper.and(bdd, agent.getTransitionRelation(), reachabilityTrans);
		agent.setTransitionRelationAndFreeOldTransitionRelation(newTrans);
		BDDWrapper.free(bdd, reachabilityTrans);
	}
	
	private static void writeStructuredResultsToFile(String fileName){
		String expResults = "";
		for(ArrayList<String> exp : results){
			for(int i=0; i<exp.size(); i++){
				String data = exp.get(i);
				if(i != exp.size()-1){
					data+="\t";
				}
				expResults+=data;
			}
			expResults+="\n";
		}
		FileOps.write(expResults, fileName);
	}
	
}
