package solver;

import java.util.ArrayList;

import coordination.CompositionalCoordinationGameStructure;
import coordination.CompositionalCoordinationStrategy;
import coordination.OptimizedCompositionalCoordinationGameStructure;
import coordination.OptimizedCompositionalCoordinationStrategy;
import jdd.bdd.BDD;
import swarm.SynchronizationGameStructure;
import utils.UtilityMethods;
import game.ControllableSinglePlayerDeterministicGameStructure;
import game.GameSolution;
import game.GameStructure;
import game.Player;
import game.Variable;

public class GR1Solver {
	
	
//	public static GameSolution solve(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
		
//		return new GameSolution(gs, argWinner, argStrategy, winningStates);
//	}
	
	public static GameSolution solve(BDD bdd, CompositionalCoordinationGameStructure cgs, int init, GR1Objective objective){
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), cgs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
//		cgs.cleanCoordinationGameStatePrintAndWait("winning states", gr1w.getWinningStates());
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		CompositionalCoordinationStrategy strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, cgs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, cgs.getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
//			Variable[] strategyVars = Variable.unionVariables(cgs.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
//			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
//			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			ArrayList<Integer> strategyFragments = new ArrayList<Integer>();
			//includes the stutter
			int numOfCommands = cgs.getCommands().length;
			ArrayList<Integer> commandsSystemTransitions = cgs.getSystemCommandTransitions();
			int stutter = game.BDDWrapper.same(bdd, cgs.variables);
			int counterUpdateFormula = bdd.ref(bdd.getZero());
			int notOfAllGuarantees = bdd.ref(bdd.getOne());
			
			for(int i=0; i<=numOfCommands; i++){
				strategyFragments.add(bdd.ref(bdd.getZero()));
			}
			
//			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
//				int post_i = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
				
				
				
				int post_i = cgs.EpostImage(pre, Player.ENVIRONMENT);
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
				
				notOfAllGuarantees = BDDWrapper.andTo(bdd, notOfAllGuarantees, bdd.not(rho1));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, Variable.getPrimedCopy(counter));
				int counter_i_update = BDDWrapper.and(bdd, rho1, nextCounter);
				BDDWrapper.free(bdd,nextCounter);
				counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, counter_i_update);
				BDDWrapper.free(bdd, counter_i_update);
				
				
				BDDWrapper.free(bdd, pre);
				BDDWrapper.free(bdd, post_i);
				
				
				
				for(int c=0; c<numOfCommands; c++){
					int commandApplicable = BDDWrapper.and(bdd, rho1, commandsSystemTransitions.get(c));
					int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
					commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
					BDDWrapper.free(bdd, commandApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(c));
					strategyFragments.set(c, commandWinningStates);
				}
				int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho1);
				int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
				stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
				BDDWrapper.free(bdd, stutterApplicable);
				BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
				strategyFragments.set(numOfCommands, stutterWinningStates);
				
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
//				int nextCounterValue = (i+1)%guarantees.size();
//				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
//				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
//				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				
//				strategyTransitions.add(rho1);
			}
			
			//finish counter update formula
			int sameCounter = BDDWrapper.same(bdd, counter);
			sameCounter = BDDWrapper.andTo(bdd, sameCounter, notOfAllGuarantees);
			BDDWrapper.free(bdd, notOfAllGuarantees);
			counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, sameCounter);
			BDDWrapper.free(bdd, sameCounter);
			
//			UtilityMethods.duration(t0, "rho1 computed ");
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					
//					int post= BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
					
//					long t1 = UtilityMethods.timeStamp();
					int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
//					UtilityMethods.duration(t1, "rho2, post computed");
					
					post = BDDWrapper.andTo(bdd, post, counter_i);
					
					
					int nextLow = BDDWrapper.replace(bdd, low, cgs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
//					t1 = UtilityMethods.timeStamp();
//					int rho2 = BDDWrapper.and(bdd, counterPart, post);
//					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
//					UtilityMethods.duration(t1, "rho2 disjunct computed");
					
//					t1 = UtilityMethods.timeStamp();
					for(int c=0; c<numOfCommands; c++){
						int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
						int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
						commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
						BDDWrapper.free(bdd, commandApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(c));
						strategyFragments.set(c, commandWinningStates);
					}
					int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
					stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextLow);
					int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
					stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
					BDDWrapper.free(bdd, stutterApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
					strategyFragments.set(numOfCommands, stutterWinningStates);
//					UtilityMethods.duration(t1, "rho2 disjunct computed");
					
					
					BDDWrapper.free(bdd, notLow);
					BDDWrapper.free(bdd, nextLow);
					BDDWrapper.free(bdd, pre);
					BDDWrapper.free(bdd, post);
					
//					t1= UtilityMethods.timeStamp();
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					UtilityMethods.duration(t1, "rho2, low updated");
					
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
//					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					strategyTransitions.add(rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
//					BDDWrapper.free(bdd, rho2);
				}
//				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.duration(t0, "rho2 computed");
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						

						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
//						int post = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
						
						int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
						BDDWrapper.free(bdd, pre);
						
						post = BDDWrapper.andTo(bdd, post, counter_i);
						
//						int rho3 = BDDWrapper.and(bdd, post, counterPart);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), cgs.getVtoVprime());
						
						for(int c=0; c<numOfCommands; c++){
							int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
							int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
							commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
							BDDWrapper.free(bdd, commandApplicable);
							BDDWrapper.free(bdd, strategyFragments.get(c));
							strategyFragments.set(c, commandWinningStates);
						}
						int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
						stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextMem_irj);
						int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
						stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
						BDDWrapper.free(bdd, stutterApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
						strategyFragments.set(numOfCommands, stutterWinningStates);
						
//						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, nextMem_irj);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
//						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
//						BDDWrapper.free(bdd, rho3);
						
//						strategyTransitions.add(rho3);
					}
				}
//				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.duration(t0,"rho3");
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
//			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			int sameCounter = BDDWrapper.same(bdd, counter);
//			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
//			BDDWrapper.free(bdd, sameCounter);
			
//			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
			
			strategy = new CompositionalCoordinationStrategy(bdd, cgs, counter, strategyFragments, counterUpdateFormula);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(cgs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
	
	public static GameSolution solve(BDD bdd, OptimizedCompositionalCoordinationGameStructure cgs, int init, GR1Objective objective){
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), cgs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
//		cgs.cleanCoordinationGameStatePrintAndWait("winning states", gr1w.getWinningStates());
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		OptimizedCompositionalCoordinationStrategy strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, cgs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			
			//gr1 counter
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, cgs.getID()+"_gr1Counter");
			
//			//gr1 counter already defined
//			Variable[] counter = Variable.getVariablesStartingWith(cgs.variables, "gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
//			Variable[] strategyVars = Variable.unionVariables(cgs.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
//			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
//			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			ArrayList<Integer> strategyFragments = new ArrayList<Integer>();
			//includes the stutter
			int numOfCommands = cgs.getCommands().length;
			ArrayList<Integer> commandsSystemTransitions = cgs.getSystemCommandTransitions();
			int stutter = game.BDDWrapper.same(bdd, cgs.variables);
			int counterUpdateFormula = bdd.ref(bdd.getZero());
			int notOfAllGuarantees = bdd.ref(bdd.getOne());
			
			for(int i=0; i<=numOfCommands; i++){
				strategyFragments.add(bdd.ref(bdd.getZero()));
			}
			
			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
//				int post_i = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
				
				
				
				int post_i = cgs.EpostImage(pre, Player.ENVIRONMENT);
				
				BDDWrapper.free(bdd, pre);
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
				BDDWrapper.free(bdd, post_i);
				
				notOfAllGuarantees = BDDWrapper.andTo(bdd, notOfAllGuarantees, bdd.not(rho1));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, Variable.getPrimedCopy(counter));
				int counter_i_update = BDDWrapper.and(bdd, rho1, nextCounter);
				BDDWrapper.free(bdd,nextCounter);
				counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, counter_i_update);
				BDDWrapper.free(bdd, counter_i_update);
				
				
				
				for(int c=0; c<numOfCommands; c++){
					int commandApplicable = BDDWrapper.and(bdd, rho1, commandsSystemTransitions.get(c));
					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//					
//					int commandApplicable = BDDWrapper.and(bdd, rho1,  cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
					
					//5+8
//					int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post_i );
//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, winningStatesPrime );
					
					
					int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
					commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
					BDDWrapper.free(bdd, commandApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(c));
					strategyFragments.set(c, commandWinningStates);
				}
				int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho1);
				int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
				stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
				BDDWrapper.free(bdd, stutterApplicable);
				BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
				strategyFragments.set(numOfCommands, stutterWinningStates);
				
				BDDWrapper.free(bdd, rho1);
				
//				BDDWrapper.free(bdd, pre);
//				BDDWrapper.free(bdd, post_i);
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
//				int nextCounterValue = (i+1)%guarantees.size();
//				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
//				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
//				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				
//				strategyTransitions.add(rho1);
			}
			
			BDDWrapper.free(bdd, winningStatesPrime);
			
			//finish counter update formula
			int sameCounter = BDDWrapper.same(bdd, counter);
			sameCounter = BDDWrapper.andTo(bdd, sameCounter, notOfAllGuarantees);
			BDDWrapper.free(bdd, notOfAllGuarantees);
			counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, sameCounter);
			BDDWrapper.free(bdd, sameCounter);
			
//			UtilityMethods.duration(t0, "rho1 computed ");
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				
//				System.out.println("computing rho2, for guarantee "+i);
				
				ArrayList<Integer> mY_i = mY.get(i);
				int low = bdd.ref(mY_i.get(0));
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					BDDWrapper.free(bdd, notLow);
					
//					int post= BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
					
//					long t1 = UtilityMethods.timeStamp();
					int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
//					UtilityMethods.duration(t1, "rho2, post computed");
					
					BDDWrapper.free(bdd, pre);
					
					post = BDDWrapper.andTo(bdd, post, counter_i);
					
					
					int nextLow = BDDWrapper.replace(bdd, low, cgs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
//					t1 = UtilityMethods.timeStamp();
//					int rho2 = BDDWrapper.and(bdd, counterPart, post);
//					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
//					UtilityMethods.duration(t1, "rho2 disjunct computed");
					
//					t1 = UtilityMethods.timeStamp();
					
//					int rho2 = BDDWrapper.and(bdd, post, nextLow);
					
					for(int c=0; c<numOfCommands; c++){
						//1
//						int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
						
						//2: performed worse than 1
//						int commandApplicable = BDDWrapper.and(bdd, post, nextLow);
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						//3
//						int commandApplicable = BDDWrapper.and(bdd, post, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						//4
//						int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
						
						//5
						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post);
						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
						
						//6: rho2 based
//						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, rho2);
						
						//7: rho2 based
//						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), rho2);
//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
						commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
						BDDWrapper.free(bdd, commandApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(c));
						strategyFragments.set(c, commandWinningStates);
					}
					int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
					stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextLow);
					
					//6 rho2 based
//					int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho2);
					
					int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
					stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
					BDDWrapper.free(bdd, stutterApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
					strategyFragments.set(numOfCommands, stutterWinningStates);
//					UtilityMethods.duration(t1, "rho2 disjunct computed");
					
					
					
					BDDWrapper.free(bdd, nextLow);
					
					BDDWrapper.free(bdd, post);
					
//					BDDWrapper.free(bdd, rho2);
					
//					t1= UtilityMethods.timeStamp();
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					UtilityMethods.duration(t1, "rho2, low updated");
					
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
//					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					strategyTransitions.add(rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
//					BDDWrapper.free(bdd, rho2);
				}
//				BDDWrapper.free(bdd, counterPart);
				
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, low);
			}
			
//			UtilityMethods.duration(t0, "rho2 computed");
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						

						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						
//						int post = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
						
						int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
						BDDWrapper.free(bdd, pre);
						
						post = BDDWrapper.andTo(bdd, post, counter_i);
						
//						int rho3 = BDDWrapper.and(bdd, post, counterPart);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), cgs.getVtoVprime());
						
//						int rho3 = BDDWrapper.and(bdd, post, nextMem_irj);
						
						for(int c=0; c<numOfCommands; c++){
							//1
//							int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
							
							//2: performed worse than 1
//							int commandApplicable = BDDWrapper.and(bdd, post, nextMem_irj);
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							//3: performed better than 1 and 2
//							int commandApplicable = BDDWrapper.and(bdd, post, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							//4
//							int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
							
							//5: performed the best 
							int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post);
							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
							
							//6: rho3 based
//							int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, rho3);
							
							//7: rho3 based
//							int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), rho3);
//							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
							commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
							BDDWrapper.free(bdd, commandApplicable);
							BDDWrapper.free(bdd, strategyFragments.get(c));
							strategyFragments.set(c, commandWinningStates);
						}
						int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
						stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextMem_irj);
						
						//6: rho3 based
//						int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho3);
						
						int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
						stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
						BDDWrapper.free(bdd, stutterApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
						strategyFragments.set(numOfCommands, stutterWinningStates);
						
//						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, post);
						BDDWrapper.free(bdd, nextMem_irj);
						
//						BDDWrapper.free(bdd, rho3);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
//						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
//						BDDWrapper.free(bdd, rho3);
						
//						strategyTransitions.add(rho3);
					}
				}
//				BDDWrapper.free(bdd, counterPart);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, low);
			}
			
//			UtilityMethods.duration(t0,"rho3");
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
//			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			int sameCounter = BDDWrapper.same(bdd, counter);
//			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
//			BDDWrapper.free(bdd, sameCounter);
			
//			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
			
			strategy = new OptimizedCompositionalCoordinationStrategy(bdd, cgs, counter, strategyFragments, counterUpdateFormula);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(cgs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
//	public static GameSolution solve(BDD bdd, CompositionalCoordinationGameStructure cgs, int init, GR1Objective objective){
//		long t0 = UtilityMethods.timeStamp();
//		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), cgs, init, objective);
//		UtilityMethods.duration(t0, "winning states computed in ");
//		
////		cgs.cleanCoordinationGameStatePrintAndWait("winning states", gr1w.getWinningStates());
//		
//		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
//		
//		
//		Player winner;
//		CoordinationStrategy strategy=null;
//		int winningStates = gr1w.getWinningStates();
//		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, cgs.getVtoVprime());
//		
//		ArrayList<Integer> guarantees = objective.getGuarantees();
//		ArrayList<Integer> assumptions = objective.getAssumptions();
//		
//		int numOfGuarantees = guarantees.size();
//		int numOfAssumptions = assumptions.size();
//		
////		System.out.println("num of guarantees "+numOfGuarantees);
////		System.out.println("num of assumptions "+numOfAssumptions);
////		UtilityMethods.getUserInput();
//		
//		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
//		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
// 		
//		if(isRealizable){
//			System.out.println("the objective is realizable!");
//
//			winner = Player.SYSTEM;
//			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
//			
//			System.out.println("number of counter variables "+numOfVars);
//			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, cgs.getID()+"_gr1Counter");
//			
////			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
//			
//			
////			Variable[] strategyVars = Variable.unionVariables(cgs.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
////			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
//			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
//			
////			int strategy_T_sys=bdd.ref(bdd.getZero());
//			
//			ArrayList<Integer> strategyTransitions = new ArrayList<Integer>();
//			
//			t0 = UtilityMethods.timeStamp();
//			
//			for(int i=0; i<guarantees.size(); i++ ){
////				int counter_i = BDDWrapper.assign(bdd, i, counter);
////				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
////				BDDWrapper.free(bdd, counter_i);
//				
//				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
////				int post_i = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
//				
//				int post_i = cgs.EpostImage(pre, Player.ENVIRONMENT);
//				
//				int rho1 = BDDWrapper.assign(bdd, i, counter);
//				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
//				BDDWrapper.free(bdd, pre);
//				BDDWrapper.free(bdd, post_i);
//				
////				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
//				
//				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
//				int nextCounterValue = (i+1)%guarantees.size();
//				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
//				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
//				
////				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
////				UtilityMethods.getUserInput();
//				
////				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
//				
//				strategyTransitions.add(rho1);
//			}
//			
//			UtilityMethods.duration(t0, "rho1 computed ");
//			
////			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
////			UtilityMethods.getUserInput();
//			
//			t0 = UtilityMethods.timeStamp();
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<Integer> mY_i = mY.get(i);
//				int low = mY_i.get(0);
//				
////				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//				
//				
////				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
////				UtilityMethods.getUserInput();
//				
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mY_i.size(); r++){
//					
//
//					
////					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
//					
////					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
//					
//					int notLow = BDDWrapper.not(bdd, low);
//					
//					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
//					
//					
////					int post= BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
//					
//					long t1 = UtilityMethods.timeStamp();
//					int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
//					UtilityMethods.duration(t1, "rho2, post computed");
//					
//					
//					int nextLow = BDDWrapper.replace(bdd, low, cgs.getVtoVprime());
////					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
//					
//					t1 = UtilityMethods.timeStamp();
//					int rho2 = BDDWrapper.and(bdd, counterPart, post);
//					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
//					UtilityMethods.duration(t1, "rho2 disjunct computed");
//					
//					
//					
//					BDDWrapper.free(bdd, notLow);
//					BDDWrapper.free(bdd, nextLow);
//					BDDWrapper.free(bdd, pre);
//					BDDWrapper.free(bdd, post);
//					
//					t1= UtilityMethods.timeStamp();
//					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					UtilityMethods.duration(t1, "rho2, low updated");
////					
////					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
////					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
////					UtilityMethods.getUserInput();
//					
////					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
//					
//					strategyTransitions.add(rho2);
//					
////					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
////					UtilityMethods.getUserInput();
//					
////					BDDWrapper.free(bdd, rho2);
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.duration(t0, "rho2 computed");
//			
////			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
////			UtilityMethods.getUserInput();
//			
//			t0 = UtilityMethods.timeStamp();
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
//				int low = bdd.ref(bdd.getZero());
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mX_i.size(); r++){
//					ArrayList<Integer> mX_i_r = mX_i.get(r);
//					for(int j=0; j< numOfAssumptions; j++){
//						
////						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//						
//
//						
//						int notLow = BDDWrapper.not(bdd, low);
//						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
//						
//						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
//						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
//						BDDWrapper.free(bdd, notLow);
//						BDDWrapper.free(bdd, notAssumption_j);
////						int post = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
//						
//						int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
//						BDDWrapper.free(bdd, pre);
//						
//						int rho3 = BDDWrapper.and(bdd, post, counterPart);
//						
//						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), cgs.getVtoVprime());
//						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
//						BDDWrapper.free(bdd, nextMem_irj);
//						
//						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
//						
////						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
////						UtilityMethods.getUserInput();
//						
////						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
////						BDDWrapper.free(bdd, rho3);
//						
//						strategyTransitions.add(rho3);
//					}
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.duration(t0,"rho3");
//			
////			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
////			UtilityMethods.getUserInput();
//			
//			
////			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
//			
////			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
////			UtilityMethods.getUserInput();
//			
////			int sameCounter = BDDWrapper.same(bdd, counter);
////			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
////			BDDWrapper.free(bdd, sameCounter);
//			
////			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
//			
//			strategy = new CoordinationStrategy(bdd, cgs, counter, strategyTransitions);
//		}else{
//			//TODO: compute counter-strategy
//			winner = Player.ENVIRONMENT;
//		}
//		
//		GameSolution solution = new GameSolution(cgs, winner, strategy, gr1w.getWinningStates());
//		return solution;
//	}
	
	public static GameSolution solve(BDD bdd, ControllableSinglePlayerDeterministicGameStructure gs, int init, GR1Objective objective){
//		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), gs, init, objective);
//		UtilityMethods.duration(t0, "winning states computed in ");
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		ControllableSinglePlayerDeterministicGameStructure strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		//it is a no-game version
		//assumptions will be ignored
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
//			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
//			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, pre);
				BDDWrapper.free(bdd, pre);
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				BDDWrapper.free(bdd, rho1);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					
					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
					int rho2 = BDDWrapper.and(bdd, counterPart, pre);
					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
					
					
					BDDWrapper.free(bdd, notLow);
					BDDWrapper.free(bdd, nextLow);
					BDDWrapper.free(bdd, pre);
					
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
					BDDWrapper.free(bdd, rho2);
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						

						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						
						
						int rho3 = BDDWrapper.and(bdd, pre, counterPart);
						BDDWrapper.free(bdd, pre);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, nextMem_irj);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
						BDDWrapper.free(bdd, rho3);
					}
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			strategy = new ControllableSinglePlayerDeterministicGameStructure(bdd, strategyVars, strategy_T_sys, gs.actionVars);
			strategy.setInit(strategyInit);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
	//TODO: does not support safety assumptions and guarantees in GR1Objective
	public static GameSolution solve(BDD bdd, GameStructure gs, int init, GR1Objective objective){
		
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), gs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		GameStructure strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
				int post_i = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
				BDDWrapper.free(bdd, pre);
				BDDWrapper.free(bdd, post_i);
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				BDDWrapper.free(bdd, rho1);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					int post= BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
					
					
					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
					int rho2 = BDDWrapper.and(bdd, counterPart, post);
					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
					
					
					BDDWrapper.free(bdd, notLow);
					BDDWrapper.free(bdd, nextLow);
					BDDWrapper.free(bdd, pre);
					BDDWrapper.free(bdd, post);
					
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
					BDDWrapper.free(bdd, rho2);
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						

						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						int post = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						BDDWrapper.free(bdd, pre);
						
						int rho3 = BDDWrapper.and(bdd, post, counterPart);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, nextMem_irj);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
						BDDWrapper.free(bdd, rho3);
					}
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			int sameCounter = BDDWrapper.same(bdd, counter);
			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
			BDDWrapper.free(bdd, sameCounter);
			
			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
//	/**
//	 * Assumes objectives include next operator 
//	 * @param bdd
//	 * @param gs
//	 * @param init
//	 * @param objective
//	 * @return
//	 */
//	public static GameSolution solve(BDD bdd, GR1GameStructureTransitionBased gs, int init, GR1Objective objective){
//		
//		long t0 = UtilityMethods.timeStamp();
//		GR1WinningStates gr1w = computeWinningStatesExtended(new BDDWrapper(bdd), gs, init, objective);
//		UtilityMethods.duration(t0, "winning states computed in ");
//		
//		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
//		
//		
//		Player winner;
//		GameStructure strategy=null;
//		int winningStates = gr1w.getWinningStates();
//		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
//		
//		ArrayList<Integer> guarantees = objective.getGuarantees();
//		ArrayList<Integer> assumptions = objective.getAssumptions();
//		
//		int numOfGuarantees = guarantees.size();
//		int numOfAssumptions = assumptions.size();
//		
////		System.out.println("num of guarantees "+numOfGuarantees);
////		System.out.println("num of assumptions "+numOfAssumptions);
////		UtilityMethods.getUserInput();
//		
//		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
//		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
// 		
//		if(isRealizable){
//			System.out.println("the objective is realizable!");
//
//			winner = Player.SYSTEM;
//			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
//			
//			System.out.println("number of counter variables "+numOfVars);
//			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
//			
////			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
//			
//			
//			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
//			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
//			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
//			
//			int strategy_T_sys=bdd.ref(bdd.getZero());
//			
//			int transitionRelation = gs.getTransitionRelation(); 
//			
//			for(int i=0; i<guarantees.size(); i++ ){
////				int counter_i = BDDWrapper.assign(bdd, i, counter);
////				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
////				BDDWrapper.free(bdd, counter_i);
//				
//				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
//				pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
////				int post_i = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//				
//				int rho1 = BDDWrapper.assign(bdd, i, counter);
//				rho1 = BDDWrapper.andTo(bdd, rho1, pre);
//				BDDWrapper.free(bdd, pre);
//				
////				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
//				
//				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
//				int nextCounterValue = (i+1)%guarantees.size();
//				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
//				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
//				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
//				
//				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
//				BDDWrapper.free(bdd, rho1);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<Integer> mY_i = mY.get(i);
//				int low = mY_i.get(0);
//				
////				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//				
//				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
//				
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mY_i.size(); r++){
//					
//
//					
////					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
//					
////					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
//					
//					int notLow = BDDWrapper.not(bdd, low);
//					
//					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
//					
////					int post= BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//					
//					pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
//					
//					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
////					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
//					
//					int rho2 = BDDWrapper.and(bdd, counterPart, pre);
//					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
//					
//					
//					BDDWrapper.free(bdd, notLow);
//					BDDWrapper.free(bdd, nextLow);
//					BDDWrapper.free(bdd, pre);
////					BDDWrapper.free(bdd, post);
//					
//					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
////					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
//					
//					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
//					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
//					
//					BDDWrapper.free(bdd, rho2);
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
//				int low = bdd.ref(bdd.getZero());
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mX_i.size(); r++){
//					ArrayList<Integer> mX_i_r = mX_i.get(r);
//					for(int j=0; j< numOfAssumptions; j++){
//						
////						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//						
//
//						
//						int notLow = BDDWrapper.not(bdd, low);
//						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
//						
//						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
//						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
//						BDDWrapper.free(bdd, notLow);
//						BDDWrapper.free(bdd, notAssumption_j);
//						
////						int post = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
////						BDDWrapper.free(bdd, pre);
//						
//						pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
//						
//						int rho3 = BDDWrapper.and(bdd, pre, counterPart);
//						BDDWrapper.free(bdd, pre);
//						
//						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
//						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
//						BDDWrapper.free(bdd, nextMem_irj);
//						
//						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
//						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
//						
//						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
//						BDDWrapper.free(bdd, rho3);
//					}
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			
////			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
//			
////			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
////			UtilityMethods.getUserInput();
//			
////			int sameCounter = BDDWrapper.same(bdd, counter);
////			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
////			BDDWrapper.free(bdd, sameCounter);
//			
////			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
//			
//			Variable[] outputVars = Variable.unionVariables(gs.outputVariables,counter);
//			strategy = new GR1GameStructureTransitionBased(bdd, gs.inputVariables, outputVars, gs.getEnvironmentTransitionRelation(), strategy_T_sys);
//			strategy.setInit(strategyInit);
//		}else{
//			//TODO: compute counter-strategy
//			winner = Player.ENVIRONMENT;
//		}
//		
//		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
//		return solution;
//	}
	
	/**
	 * Assumes objectives include next operator 
	 * @param bdd
	 * @param gs
	 * @param init
	 * @param objective
	 * @return
	 */
	public static GameSolution solve(BDD bdd, GR1GameStructureTransitionBased gs, int init, GR1Objective objective){
		
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStatesExtended(new BDDWrapper(bdd), gs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		GameStructure strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		ArrayList<Integer> guaranteesTransitionBased = new ArrayList<Integer>();
		ArrayList<Integer> assumptionsTransitionBased = new ArrayList<Integer>();
		
		for(int i=0; i<guarantees.size();i++){
			guaranteesTransitionBased.add(gs.turnIntoTransitionFormula(guarantees.get(i)));
		}
		
		for(int i=0; i<assumptions.size();i++){
			assumptionsTransitionBased.add(gs.turnIntoTransitionFormula(assumptions.get(i)));
		}
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			int transitionRelation = gs.getTransitionRelation(); 
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
//				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
				
				int pre = BDDWrapper.and(bdd, guaranteesTransitionBased.get(i), winningStates);
				
				pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
//				int post_i = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, pre);
				BDDWrapper.free(bdd, pre);
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				BDDWrapper.free(bdd, rho1);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
//					int post= BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
					
					pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
					
					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
					int rho2 = BDDWrapper.and(bdd, counterPart, pre);
					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
					
					
					BDDWrapper.free(bdd, notLow);
					BDDWrapper.free(bdd, nextLow);
					BDDWrapper.free(bdd, pre);
//					BDDWrapper.free(bdd, post);
					
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
					BDDWrapper.free(bdd, rho2);
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				int maxr_j = -1; 
				for(int j=0; j<numOfAssumptions;j++){
					int next_r = mX_i.get(j).size();
					if(maxr_j < next_r){
						maxr_j = next_r;
					}
				}
//				System.out.println("max r for guarantee "+i+" is "+maxr_j);
//				UtilityMethods.getUserInput();
				
//				for(int r=1; r<mX_i.size(); r++){
				for(int r=0; r<maxr_j; r++){
//					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						
						if(mX_i_j.size()<= r){
							continue;
						}
						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptionsTransitionBased.get(j));
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "not assumption "+j, notAssumption_j);
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
//						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						pre = BDDWrapper.andTo(bdd, pre, mX_i_j.get(r));
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "pre before anding with tr", pre);
						
//						int post = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//						BDDWrapper.free(bdd, pre);
						
						pre = BDDWrapper.andTo(bdd, pre, transitionRelation);
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "transition relation is", transitionRelation);
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "pre after anding with tr", pre);
						
						int rho3 = BDDWrapper.and(bdd, pre, counterPart);
						BDDWrapper.free(bdd, pre);
						
//						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_j.get(r), gs.getVtoVprime());
						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, nextMem_irj);
						
//						UtilityMethods.debugBDDMethodsAndWait(bdd, "mX["+i+"]["+j+"]["+r+"] is", mX_i_j.get(r));
						
//						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						low = BDDWrapper.orTo(bdd, low, mX_i_j.get(r));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
						BDDWrapper.free(bdd, rho3);
					}
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
//			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
//			int sameCounter = BDDWrapper.same(bdd, counter);
//			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
//			BDDWrapper.free(bdd, sameCounter);
			
//			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
			
			Variable[] outputVars = Variable.unionVariables(gs.outputVariables,counter);
			strategy = new GR1GameStructureTransitionBased(bdd, gs.inputVariables, outputVars, gs.getEnvironmentTransitionRelation(), strategy_T_sys);
			strategy.setInit(strategyInit);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	

	
//	public static GameSolution solve(BDD bdd, GameStructure gs, int init, GR1Objective objective){
//		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), gs, init, objective); 
//		
//		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
//		
//		System.out.println("the objective is realizable!");
//		
//		Player winner;
//		GameStructure strategy=null;
//		int winningStates = gr1w.getWinningStates();
//		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
//		
//		ArrayList<Integer> guarantees = objective.getGuarantees();
//		ArrayList<Integer> assumptions = objective.getAssumptions();
//		
//		int numOfGuarantees = guarantees.size();
//		int numOfAssumptions = assumptions.size();
//		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
//		
//		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
//		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
// 		
//		if(isRealizable){
//			winner = Player.SYSTEM;
//			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
//			
//			System.out.println("number of counter variables "+numOfVars);
//			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
//			
//			
//			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
//			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
//			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
//			BDDWrapper.free(bdd,zeroCounter);
//			
//			Variable[] counterPrime = Variable.getPrimedCopy(counter);
//			
//			int strategy_T_sys=bdd.ref(bdd.getZero());
//			
//			for(int i=0; i<guarantees.size(); i++ ){
////				int counter_i = BDDWrapper.assign(bdd, i, counter);
////				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
////				BDDWrapper.free(bdd, counter_i);
//				
//				int rho1 = BDDWrapper.assign(bdd, i, counter);
//				
////				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
//				
//				int guaranteePrime = BDDWrapper.replace(bdd, guarantees.get(i), gs.getVtoVprime());
//				rho1 = BDDWrapper.andTo(bdd, rho1, guaranteePrime);
//				BDDWrapper.free(bdd, guaranteePrime);
//				
//				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
//				int nextCounterValue = (i+1)%guarantees.size();
//				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
//				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
//				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
//				
//				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
//				BDDWrapper.free(bdd, rho1);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<Integer> mY_i = mY.get(i);
//				int low = mY_i.get(0);
//				
////				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//				
//				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
//				
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mY_i.size(); r++){
//					
//					int post_r = BDDWrapper.EpostImage(bdd, mY_i.get(r), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//					
////					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
//					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
//					
////					int notLow = BDDWrapper.not(bdd, low);
//					
//					int postLow= BDDWrapper.EpostImage(bdd, low, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//					int notPostLow = BDDWrapper.not(bdd, postLow);
//					
//					
//					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
////					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
//					
//					int lowPart = BDDWrapper.and(bdd, notPostLow, nextLow);
//					
//					rho2 = BDDWrapper.andTo(bdd, rho2, lowPart);
////					BDDWrapper.free(bdd, notLow);
//					BDDWrapper.free(bdd, notPostLow);
//					BDDWrapper.free(bdd, nextLow);
//					BDDWrapper.free(bdd, lowPart);
//					
//					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
//					
//					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
//					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
//					
//					BDDWrapper.free(bdd, rho2);
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			
//			for(int i=0; i<numOfGuarantees; i++){
//				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
//				int low = bdd.ref(bdd.getZero());
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
//				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
//				BDDWrapper.free(bdd, counter_i);
//				BDDWrapper.free(bdd, nextCounter);
//				for(int r=1; r<mX_i.size(); r++){
//					ArrayList<Integer> mX_i_r = mX_i.get(r);
//					for(int j=0; j< numOfAssumptions; j++){
//						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
//						
////						int rho3 = BDDWrapper.and(bdd, counterPart, mX_i_r.get(j));
//						
//						int rho3 = BDDWrapper.and(bdd, counterPart, postmX_irj);
//						
////						int notLow = BDDWrapper.not(bdd, low);
//						
//						
//						
//						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
//						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
//						int andNotLowNotAssump = BDDWrapper.and(bdd, notLow, notAssumption_j);
//						BDDWrapper.free(bdd, notLow);
//						BDDWrapper.free(bdd, notAssumption_j);
//						nextMem_irj = BDDWrapper.andTo(bdd, nextMem_irj, andNotLowNotAssump);
//						BDDWrapper.free(bdd, andNotLowNotAssump);
//						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
//						BDDWrapper.free(bdd, nextMem_irj);
//						
//						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
//						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
//						
//						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
//						BDDWrapper.free(bdd, rho3);
//					}
//				}
//				BDDWrapper.free(bdd, counterPart);
//			}
//			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			
//			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
//			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
//			
//			int sameCounter = BDDWrapper.same(bdd, counter);
//			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
//			BDDWrapper.free(bdd, sameCounter);
//			
//			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
//		}else{
//			//TODO: compute counter-strategy
//			winner = Player.ENVIRONMENT;
//		}
//		
//		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
//		return solution;
//	}
	
	//****OLD VERSION BEFORE CHANGING FOR SWARM EXAMPLE
//	public static GR1WinningStates computeWinningStates(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
//		int Z = bdd.getTrue();
//		int ZPrime = bdd.getTrue();
//		
//		ArrayList<Integer> guarantees = objective.getGuarantees();
//		ArrayList<Integer> assumptions = objective.getAssumptions();
//		
//		//preparing the safety part
//		int safetyAssumptionsFormula = bdd.getFalse();
//		int safetyGuaranteesFormula = bdd.getTrue();
//		
//		ArrayList<Integer> safetyAssumptions = objective.getSafetyAssumptions();
//		ArrayList<Integer> safetyGuarantees = objective.getSafetyGuarantees();
//		
//		if(safetyAssumptions != null && safetyAssumptions.size()>0){
//			for(Integer safetyAssumption : safetyAssumptions){
//				safetyAssumptionsFormula = bdd.orTo(safetyAssumptionsFormula, bdd.not(safetyAssumption));
//			}
//		}
//		
//		if(safetyGuarantees != null && safetyGuarantees.size()>0){
//			for(Integer safetyGuarantee : safetyGuarantees){
//				safetyGuaranteesFormula = bdd.andTo(safetyGuaranteesFormula, safetyGuarantee);
//			}
//		}
//		
//		int safetyFormula = bdd.or(safetyAssumptionsFormula, safetyGuaranteesFormula);
//		bdd.free(safetyAssumptionsFormula);
//		bdd.free(safetyGuaranteesFormula);
//		
//		//computing reachable part
////		int reachable = gs.reachable(init);
//		int reachable = bdd.getTrue();
//		
//		safetyFormula = bdd.and(safetyFormula, reachable);
//		
//		
//		ArrayList<ArrayList<Integer>> mY;
//		ArrayList<ArrayList<ArrayList<Integer>>> mX;
//
//		do{
//			
//
//			
//			ZPrime = Z;
//			//memory
//			//TODO: clean up the memory
//			mY = new  ArrayList<ArrayList<Integer>>();
//			mX = new ArrayList<ArrayList<ArrayList<Integer>>>();
//			
//			for(int i=0; i<guarantees.size(); i++){
//				int Y = bdd.getFalse();
//				int YPrime;
//				do{
//					YPrime = Y;
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "computing preZ, Z is ",  Z);
//					int preZ = gs.controllablePredecessor(Z);
//					int preY = gs.controllablePredecessor(Y);
//					
//					preZ = bdd.andTo(preZ, safetyFormula);
//					preY = bdd.andTo(preY, safetyFormula);
//					
////					int pre = bdd.or(preZ, preY);
////					int start = bdd.and(guarantees.get(i), pre);
////					bdd.free(pre);
//					
//					int start = bdd.and(guarantees.get(i), preZ);
//					bdd.free(preZ);
//					start = bdd.orTo(start, preY);
//					bdd.free(preY);
//					Y = bdd.getFalse();
//					for(int j=0; j<assumptions.size(); j++){
//						int X = Z;
//						int XPrime;
//						do{
//							XPrime = X;
//							int preX = gs.controllablePredecessor(X);
//							
//							preX = bdd.andTo(preX, safetyFormula);
//							
//							X = bdd.and(bdd.not(assumptions.get(j)), preX);
//							X = bdd.orTo(X, start);
//							bdd.free(preX);
//							
////							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
////							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "Z is ",  Z);
//							
//							
//							
//						}while(X != XPrime);
//						
//						//update memory
//						if(i >= mX.size() ){
//							mX.add(new ArrayList<ArrayList<Integer>>());
//						}
//						
//						ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
//						if(j >= mX_i.size()){
//							mX_i.add(new ArrayList<Integer>());
//						}
//						
//						ArrayList<Integer> mX_i_j = mX_i.get(j);
//						
//						mX_i_j.add(X);
//						
//						Y = bdd.orTo(Y,X);
//						
//						
//					}
//					if(i >= mY.size()){
//						mY.add(new ArrayList<Integer>());
//					}
//					
//					ArrayList<Integer> mY_i = mY.get(i);
//					mY_i.add(Y);
//					
//					
//					bdd.free(start);
//					
////					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
////					UtilityMethods.getUserInput();
////					
////					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
////					UtilityMethods.getUserInput();
//					
//					if(Y == YPrime){
//						System.out.println("Y and YPrime are the same");
//					}
//				}while(Y != YPrime);
//				
////				Z = Y;
//				
////				Z = bdd.bdd.ref(Y);
//				
//				Z= bdd.and(Y, bdd.getTrue());
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
//				UtilityMethods.getUserInput();
//			}
//			
//			if(Z != ZPrime){
//				//clean up the memory
//				bdd.free(ZPrime);
//				//free mX 
//				for(int i=0; i<mX.size(); i++){
//					ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
//					for(int j=0; j<mX_i.size(); j++){
//						ArrayList<Integer> mX_i_j = mX_i.get(j);
//						for(int k=0; k<mX_i_j.size();k++){
//							bdd.free(mX_i_j.get(k));
//						}
//					}
//				}
//				//free mY
//				for(int i=0; i<mY.size();i++){
//					ArrayList<Integer> mY_i = mY.get(i);
//					for(int j=0; j<mY_i.size();j++){
//						bdd.free(mY_i.get(j));
//					}
//				}
//			}else{
//				break;
//			}
//			
//		}while(Z!=ZPrime);
//		
//		GR1WinningStates winningStates = new GR1WinningStates(Z, mY, mX);
//		
//		return winningStates;
//	}
	
	public static GR1WinningStates computeWinningStates(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
		int Z = bdd.getTrue();
		int ZPrime = bdd.getTrue();
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		//preparing the safety part
		int safetyAssumptionsFormula = bdd.getFalse();
		int safetyGuaranteesFormula = bdd.getTrue();
		
		ArrayList<Integer> safetyAssumptions = objective.getSafetyAssumptions();
		ArrayList<Integer> safetyGuarantees = objective.getSafetyGuarantees();
		
		if(safetyAssumptions != null && safetyAssumptions.size()>0){
			for(Integer safetyAssumption : safetyAssumptions){
				safetyAssumptionsFormula = bdd.orTo(safetyAssumptionsFormula, bdd.not(safetyAssumption));
			}
		}
		
		if(safetyGuarantees != null && safetyGuarantees.size()>0){
			for(Integer safetyGuarantee : safetyGuarantees){
				safetyGuaranteesFormula = bdd.andTo(safetyGuaranteesFormula, safetyGuarantee);
			}
		}
		
		int safetyFormula = bdd.or(safetyAssumptionsFormula, safetyGuaranteesFormula);
		bdd.free(safetyAssumptionsFormula);
		bdd.free(safetyGuaranteesFormula);
		
		//computing reachable part
//		int reachable = gs.reachable(init);
		int reachable = bdd.getTrue();
		
		safetyFormula = bdd.and(safetyFormula, reachable);
		
		
		ArrayList<ArrayList<Integer>> mY;
		ArrayList<ArrayList<ArrayList<Integer>>> mX;

		do{
			

			ZPrime = bdd.bdd.ref(Z);
			//memory
			//TODO: clean up the memory
			mY = new  ArrayList<ArrayList<Integer>>();
			mX = new ArrayList<ArrayList<ArrayList<Integer>>>();
			
			for(int i=0; i<guarantees.size(); i++){
				int Y = bdd.getFalse();
				int YPrime = bdd.getFalse();
				do{
					bdd.free(YPrime);
					YPrime = bdd.bdd.ref(Y);
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "computing preZ, Z is ",  Z);
					int preZ = gs.controllablePredecessor(Z);
					int preY = gs.controllablePredecessor(Y);
					
					preZ = bdd.andTo(preZ, safetyFormula);
					preY = bdd.andTo(preY, safetyFormula);
					
//					int pre = bdd.or(preZ, preY);
//					int start = bdd.and(guarantees.get(i), pre);
//					bdd.free(pre);
					
					int start = bdd.and(guarantees.get(i), preZ);
					bdd.free(preZ);
					start = bdd.orTo(start, preY);
					bdd.free(preY);
					Y = bdd.getFalse();
					for(int j=0; j<assumptions.size(); j++){
						int X = bdd.bdd.ref(Z);
						int XPrime=bdd.getFalse();
						do{
							bdd.free(XPrime);
							XPrime = bdd.bdd.ref(X);
							int preX = gs.controllablePredecessor(X);
							
							preX = bdd.andTo(preX, safetyFormula);
							
							bdd.free(X);
							X = bdd.and(bdd.not(assumptions.get(j)), preX);
							X = bdd.orTo(X, start);
							bdd.free(preX);
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
//							UtilityMethods.getUserInput();
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "Z is ",  Z);
							
							
							
						}while(X != XPrime);
						bdd.free(XPrime);
						
						//update memory
						if(i >= mX.size() ){
							mX.add(new ArrayList<ArrayList<Integer>>());
						}
						
						ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
						if(j >= mX_i.size()){
							mX_i.add(new ArrayList<Integer>());
						}
						
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						
						mX_i_j.add(X);
						
						Y = bdd.orTo(Y,X);
						
						
					}
					if(i >= mY.size()){
						mY.add(new ArrayList<Integer>());
					}
					
					ArrayList<Integer> mY_i = mY.get(i);
					mY_i.add(Y);
					
					
					bdd.free(start);
					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
//					UtilityMethods.getUserInput();
//					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
//					UtilityMethods.getUserInput();
					
//					if(Y == YPrime){
//						System.out.println("Y and YPrime are the same");
//					}
				}while(Y != YPrime);
				bdd.free(YPrime);
				
//				Z = Y;
				
				Z = bdd.bdd.ref(Y);
				
				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
//				UtilityMethods.getUserInput();
			}
			
			if(Z != ZPrime){
				//clean up the memory
				bdd.free(ZPrime);
				//free mX 
				for(int i=0; i<mX.size(); i++){
					ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
					for(int j=0; j<mX_i.size(); j++){
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						for(int k=0; k<mX_i_j.size();k++){
							bdd.free(mX_i_j.get(k));
						}
					}
				}
				//free mY
				for(int i=0; i<mY.size();i++){
					ArrayList<Integer> mY_i = mY.get(i);
					for(int j=0; j<mY_i.size();j++){
						bdd.free(mY_i.get(j));
					}
				}
			}else{
				break;
			}
			
		}while(Z!=ZPrime);
		
		bdd.free(ZPrime);
		
		GR1WinningStates winningStates = new GR1WinningStates(Z, mY, mX);
		
		return winningStates;
	}
	
	/**
	 * The liveness objectives can have the next operator 
	 * @return
	 */
	public static GR1WinningStates computeWinningStatesExtended(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
		int Z = bdd.getTrue();
		int ZPrime = bdd.getTrue();
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		
		//computing reachable part
//		int reachable = gs.reachable(init);
		
		
		
		ArrayList<ArrayList<Integer>> mY;
		ArrayList<ArrayList<ArrayList<Integer>>> mX;

		do{
			

			
			ZPrime = Z;
			//memory
			//TODO: clean up the memory
			mY = new  ArrayList<ArrayList<Integer>>();
			mX = new ArrayList<ArrayList<ArrayList<Integer>>>();
			
//			UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "Z is ", Z);
			
			for(int i=0; i<guarantees.size(); i++){
				int Y = bdd.getFalse();
				int YPrime;
				do{
					YPrime = Y;
					
//					int preZ = gs.controllablePredecessor(Z);
//					int preY = gs.controllablePredecessor(Y);
					
					int transZ = gs.transitionsEndingInGoalStates(Z);
					int transY = gs.transitionsEndingInGoalStates(Y);
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "Y is", Y);
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "transZ is", transZ);
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "transY is", transY);
					
					
//					int pre = bdd.or(preZ, preY);
//					int start = bdd.and(guarantees.get(i), pre);
//					bdd.free(pre);
					
					int g_i = gs.turnIntoTransitionFormula(guarantees.get(i));
					
//					int tmp = bdd.and(guarantees.get(i), transZ);
					int tmp = bdd.and(g_i, transZ);
					bdd.free(transZ);
					tmp = bdd.orTo(tmp, transY);
					bdd.free(transY);
					int start = gs.controllablePredecessor(tmp);
					bdd.free(tmp);
					
					int startTrans = gs.turnIntoTransitionFormula(start);
					
					
//					UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "start is ", start);
					
					Y = bdd.getFalse();
					for(int j=0; j<assumptions.size(); j++){
						int X = Z;
						int XPrime;
						do{
							XPrime = X;
							
//							int preX = gs.controllablePredecessor(X);
							
							int transX = gs.transitionsEndingInGoalStates(X);
							
//							UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "transX  is ", transX);
							
							int a_j = gs.turnIntoTransitionFormula(assumptions.get(j));
							
							
//							int tmp2 = bdd.and(bdd.not(assumptions.get(j)), transX);
							int tmp2 = bdd.and(bdd.not(a_j), transX);
							bdd.free(transX);
							
							
							
//							X = gs.controllablePredecessor(tmp2);
//							bdd.free(tmp2);
//							
//							X = bdd.orTo(X, start);
							
							tmp2 = bdd.orTo(tmp2, startTrans);
							
//							UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "tmp2", tmp2);
							
							X = gs.controllablePredecessor(tmp2);
							bdd.free(tmp2);
							X = bdd.orTo(X, start);
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
//							UtilityMethods.getUserInput();
//							

							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
							
							
							
							
						}while(X != XPrime);
						
						//update memory
						if(i >= mX.size() ){
							mX.add(new ArrayList<ArrayList<Integer>>());
						}
						
						ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
						if(j >= mX_i.size()){
							mX_i.add(new ArrayList<Integer>());
						}
						
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						
						mX_i_j.add(X);
						
						Y = bdd.orTo(Y,X);
						
						
					}
					if(i >= mY.size()){
						mY.add(new ArrayList<Integer>());
					}
					
					ArrayList<Integer> mY_i = mY.get(i);
					mY_i.add(Y);
					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
//					UtilityMethods.getUserInput();
//					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
//					UtilityMethods.getUserInput();
					bdd.free(start);
				}while(Y != YPrime);
				
//				Z = Y;
				
				Z = bdd.bdd.ref(Y);
				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
//				UtilityMethods.getUserInput();
			}
			
			if(Z != ZPrime){
				//clean up the memory
				bdd.free(ZPrime);
				//free mX 
				for(int i=0; i<mX.size(); i++){
					ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
					for(int j=0; j<mX_i.size(); j++){
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						for(int k=0; k<mX_i_j.size();k++){
							bdd.free(mX_i_j.get(k));
						}
					}
				}
				//free mY
				for(int i=0; i<mY.size();i++){
					ArrayList<Integer> mY_i = mY.get(i);
					for(int j=0; j<mY_i.size();j++){
						bdd.free(mY_i.get(j));
					}
				}
			}else{
				break;
			}
			
		}while(Z!=ZPrime);
		
		GR1WinningStates winningStates = new GR1WinningStates(Z, mY, mX);
		
		return winningStates;
	}
	
	
	/**
	 * Computes winning states for GR1 games without keeping any memory. The goal here is to check the realizability without synthesizing the actual strategy
	 * @param bdd
	 * @param gs
	 * @param init
	 * @param objective
	 * @return
	 * 
	 * TODO: free X and Y
	 */
	public static int computeWinningStates_noMemory(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
		int Z = bdd.getTrue();
		int ZPrime = bdd.getTrue();
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		//preparing the safety part
		int safetyAssumptionsFormula = bdd.getFalse();
		int safetyGuaranteesFormula = bdd.getTrue();
		
		ArrayList<Integer> safetyAssumptions = objective.getSafetyAssumptions();
		ArrayList<Integer> safetyGuarantees = objective.getSafetyGuarantees();
		
		if(safetyAssumptions != null && safetyAssumptions.size()>0){
			for(Integer safetyAssumption : safetyAssumptions){
				safetyAssumptionsFormula = bdd.orTo(safetyAssumptionsFormula, bdd.not(safetyAssumption));
			}
		}
		
		if(safetyGuarantees != null && safetyGuarantees.size()>0){
			for(Integer safetyGuarantee : safetyGuarantees){
				safetyGuaranteesFormula = bdd.andTo(safetyGuaranteesFormula, safetyGuarantee);
			}
		}
		
		int safetyFormula = bdd.or(safetyAssumptionsFormula, safetyGuaranteesFormula);
		bdd.free(safetyAssumptionsFormula);
		bdd.free(safetyGuaranteesFormula);
		
		//computing reachable part
		int reachable = gs.reachable(init);
		
		safetyFormula = bdd.and(safetyFormula, reachable);

		do{
			

			
			ZPrime = Z;
			
			
			for(int i=0; i<guarantees.size(); i++){
				int Y = bdd.getFalse();
				int YPrime;
				do{
					YPrime = Y;
					int preZ = gs.controllablePredecessor(Z);
					int preY = gs.controllablePredecessor(Y);
					
					preZ = bdd.andTo(preZ, safetyFormula);
					preY = bdd.andTo(preY, safetyFormula);
					
//					int pre = bdd.or(preZ, preY);
//					int start = bdd.and(guarantees.get(i), pre);
//					bdd.free(pre);
					
					int start = bdd.and(guarantees.get(i), preZ);
					bdd.free(preZ);
					start = bdd.orTo(start, preY);
					bdd.free(preY);
					Y = bdd.getFalse();
					for(int j=0; j<assumptions.size(); j++){
						int X = Z;
						int XPrime;
						do{
							XPrime = X;
							int preX = gs.controllablePredecessor(X);
							
							preX = bdd.andTo(preX, safetyFormula);
							
							X = bdd.and(bdd.not(assumptions.get(j)), preX);
							X = bdd.orTo(X, start);
							bdd.free(preX);
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
//							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
							
							
							
							
						}while(X != XPrime);
						
						
						Y = bdd.orTo(Y,X);
						
						
					}
					
					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
//					UtilityMethods.getUserInput();
//					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
//					UtilityMethods.getUserInput();
					bdd.free(start);
				}while(Y != YPrime);
				
//				Z = Y;
				
				Z = bdd.bdd.ref(Y);
				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
//				UtilityMethods.getUserInput();
			}
			
			
		}while(Z!=ZPrime);
		
		return Z;
	}
	
	/**
	 * Assumptions can have next
	 * @param bdd
	 * @param gs
	 * @param init
	 * @param objective
	 * @return
	 */
	public static GR1WinningStates computeWinningStatesWithExtendedAssumptions(BDDWrapper bdd, GameStructure gs, int init, GR1Objective objective){
		int Z = bdd.getTrue();
		int ZPrime = bdd.getTrue();
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		ArrayList<ArrayList<Integer>> mY;
		ArrayList<ArrayList<ArrayList<Integer>>> mX;
		
		ArrayList<GameStructure> restrictedGameStructures = new ArrayList<GameStructure>();
		for(int i=0; i<assumptions.size(); i++){
			if(gs.includePrimeVariable(assumptions.get(i))){
				int notAssumption = bdd.not(assumptions.get(i));
				
				UtilityMethods.debugBDDMethods(bdd.bdd, "not assumptiuon "+i, notAssumption);
				int newT_env = bdd.and(gs.getEnvironmentTransitionRelation(), notAssumption);
				UtilityMethods.debugBDDMethods(bdd.bdd, "restricted T_env ", newT_env);
				GameStructure newGS = new GameStructure(bdd.bdd, gs.variables, gs.primedVariables, gs.getInit(), newT_env, gs.getSystemTransitionRelation(), gs.actionVars, gs.actionVars);
				restrictedGameStructures.add(newGS);
			}else{
				restrictedGameStructures.add(gs);
			}
		}

		do{
			
			ZPrime = Z;
			//memory
			//TODO: clean up the memory
			mY = new  ArrayList<ArrayList<Integer>>();
			mX = new ArrayList<ArrayList<ArrayList<Integer>>>();
			
			for(int i=0; i<guarantees.size(); i++){
				int Y = bdd.getFalse();
				int YPrime;
				do{
					YPrime = Y;
					int preZ = gs.controllablePredecessor(Z);
					int preY = gs.controllablePredecessor(Y);
					
//					int pre = bdd.or(preZ, preY);
//					int start = bdd.and(guarantees.get(i), pre);
//					bdd.free(pre);
					
					int start = bdd.and(guarantees.get(i), preZ);
					start = bdd.orTo(start, preY);
					
					bdd.free(preZ);
					bdd.free(preY);
					Y = bdd.getFalse();
					for(int j=0; j<assumptions.size(); j++){
						int X = Z;
						int XPrime;
						do{
							XPrime = X;
							int preX;
							if(gs == restrictedGameStructures.get(j)){
								preX = gs.controllablePredecessor(X);
								X = bdd.and(bdd.not(assumptions.get(j)), preX);
								bdd.free(preX);
							}else{
								UtilityMethods.debugBDDMethods(bdd.bdd, "Current X is", X);
								X = restrictedGameStructures.get(j).controllablePredecessor(X);
								UtilityMethods.debugBDDMethods(bdd.bdd, "preX", X);
							}
							X = bdd.orTo(X, start);
							
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
//							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
							
							
							
							
						}while(X != XPrime);
						
						//update memory
						if(i >= mX.size() ){
							mX.add(new ArrayList<ArrayList<Integer>>());
						}
						
						ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
						if(j >= mX_i.size()){
							mX_i.add(new ArrayList<Integer>());
						}
						
						ArrayList<Integer> mX_i_j = mX_i.get(j);
						
						mX_i_j.add(X);
						
						UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
						UtilityMethods.getUserInput();
						
						Y = bdd.orTo(Y,X);
						
						
					}
					if(i >= mY.size()){
						mY.add(new ArrayList<Integer>());
					}
					
					ArrayList<Integer> mY_i = mY.get(i);
					mY_i.add(Y);
					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
//					UtilityMethods.getUserInput();
//					
					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
					UtilityMethods.getUserInput();
					bdd.free(start);
				}while(Y != YPrime);
				
				Z = Y;
				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
				UtilityMethods.getUserInput();
			}
		}while(Z!=ZPrime);
		
		GR1WinningStates winningStates = new GR1WinningStates(Z, mY, mX);
		
		return winningStates;
	}
	
	public static GameSolution solveExtended(BDD bdd, GameStructure gs, int init, GR1Objective objective){
		
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStatesWithExtendedAssumptions(new BDDWrapper(bdd), gs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		GameStructure strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, gs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
//		System.out.println("num of guarantees "+numOfGuarantees);
//		System.out.println("num of assumptions "+numOfAssumptions);
//		UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
 		
		if(isRealizable){
			System.out.println("the objective is realizable!");

			winner = Player.SYSTEM;
			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
			
			System.out.println("number of counter variables "+numOfVars);
			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
			Variable[] strategyVars = Variable.unionVariables(gs.variables, counter);
			int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
			int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
			BDDWrapper.free(bdd,zeroCounter);
			
			Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
			int strategy_T_sys=bdd.ref(bdd.getZero());
			
			for(int i=0; i<guarantees.size(); i++ ){
//				int counter_i = BDDWrapper.assign(bdd, i, counter);
//				int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
//				BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
				int post_i = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
				BDDWrapper.free(bdd, pre);
				BDDWrapper.free(bdd, post_i);
				
//				rho1 = BDDWrapper.andTo(bdd, rho1, guarantees.get(i));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
				rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
//				UtilityMethods.getUserInput();
				
				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				BDDWrapper.free(bdd, rho1);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<Integer> mY_i = mY.get(i);
				int low = mY_i.get(0);
				
//				int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
//				UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
//				UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					

					
//					int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
//					int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, low, mY_i.get(r));
					int post= BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
					
					
					int nextLow = BDDWrapper.replace(bdd, low, gs.getVtoVprime());
//					int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
					int rho2 = BDDWrapper.and(bdd, counterPart, post);
					rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
					
					
					BDDWrapper.free(bdd, notLow);
					BDDWrapper.free(bdd, nextLow);
					BDDWrapper.free(bdd, pre);
					BDDWrapper.free(bdd, post);
					
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
//					
//					UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
//					UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
//					UtilityMethods.getUserInput();
					
					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
//					UtilityMethods.getUserInput();
					
					BDDWrapper.free(bdd, rho2);
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
				int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
				int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
//						int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						

						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						int post = BDDWrapper.EpostImage(bdd, pre, gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						BDDWrapper.free(bdd, pre);
						
						int rho3 = BDDWrapper.and(bdd, post, counterPart);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), gs.getVtoVprime());
						rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, nextMem_irj);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
//						UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
//						UtilityMethods.getUserInput();
						
						strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
						BDDWrapper.free(bdd, rho3);
					}
				}
				BDDWrapper.free(bdd, counterPart);
			}
			
//			UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			
			strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
//			UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
//			UtilityMethods.getUserInput();
			
			int sameCounter = BDDWrapper.same(bdd, counter);
			int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
			BDDWrapper.free(bdd, sameCounter);
			
			strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(gs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}

	//playing with counter ordering
	public static GameSolution solve(BDD bdd, OptimizedCompositionalCoordinationGameStructure cgs, int init, GR1Objective objective, Variable[] counter){
		long t0 = UtilityMethods.timeStamp();
		GR1WinningStates gr1w = computeWinningStates(new BDDWrapper(bdd), cgs, init, objective);
		UtilityMethods.duration(t0, "winning states computed in ");
		
	//	cgs.cleanCoordinationGameStatePrintAndWait("winning states", gr1w.getWinningStates());
		
		boolean isRealizable = BDDWrapper.subset(bdd, init, gr1w.getWinningStates());
		
		
		Player winner;
		OptimizedCompositionalCoordinationStrategy strategy=null;
		int winningStates = gr1w.getWinningStates();
		int winningStatesPrime = BDDWrapper.replace(bdd, winningStates, cgs.getVtoVprime());
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		int numOfGuarantees = guarantees.size();
		int numOfAssumptions = assumptions.size();
		
	//	System.out.println("num of guarantees "+numOfGuarantees);
	//	System.out.println("num of assumptions "+numOfAssumptions);
	//	UtilityMethods.getUserInput();
		
		ArrayList<ArrayList<Integer>> mY = gr1w.getMY();
		ArrayList<ArrayList<ArrayList<Integer>>> mX = gr1w.getMX();
			
		if(isRealizable){
			System.out.println("the objective is realizable!");
	
			winner = Player.SYSTEM;
			
//			//gr1 counter
//			int numOfVars = UtilityMethods.numOfBits(guarantees.size()-1);
//			
//			System.out.println("number of counter variables "+numOfVars);
//			//create a counter that keeps track of which guarantee is being satisfied. Zn in the GR1 paper
//			Variable[] counter = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, cgs.getID()+"_gr1Counter");
			
	//		//gr1 counter already defined
	//		Variable[] counter = Variable.getVariablesStartingWith(cgs.variables, "gr1Counter");
			
	//		Variable[] counter = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfVars, gs.getID()+"_gr1Counter");
			
			
	//		Variable[] strategyVars = Variable.unionVariables(cgs.variables, counter);
	//		int zeroCounter = BDDWrapper.assign(bdd, 0, counter);
	//		int strategyInit = BDDWrapper.and(bdd, init, zeroCounter);
	//		BDDWrapper.free(bdd,zeroCounter);
			
	//		Variable[] counterPrime = Variable.getPrimedCopy(counter);
			
	//		int strategy_T_sys=bdd.ref(bdd.getZero());
			
			ArrayList<Integer> strategyFragments = new ArrayList<Integer>();
			//includes the stutter
			int numOfCommands = cgs.getCommands().length;
			ArrayList<Integer> commandsSystemTransitions = cgs.getSystemCommandTransitions();
			int stutter = game.BDDWrapper.same(bdd, cgs.variables);
			int counterUpdateFormula = bdd.ref(bdd.getZero());
			int notOfAllGuarantees = bdd.ref(bdd.getOne());
			
			for(int i=0; i<=numOfCommands; i++){
				strategyFragments.add(bdd.ref(bdd.getZero()));
			}
			
			t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<guarantees.size(); i++ ){
	//			int counter_i = BDDWrapper.assign(bdd, i, counter);
	//			int rho1 = BDDWrapper.and(bdd, counter_i, winningStates);
	//			BDDWrapper.free(bdd, counter_i);
				
				int pre = BDDWrapper.and(bdd, guarantees.get(i), winningStates);
	//			int post_i = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
				
				
				
				int post_i = cgs.EpostImage(pre, Player.ENVIRONMENT);
				
				BDDWrapper.free(bdd, pre);
				
				int rho1 = BDDWrapper.assign(bdd, i, counter);
				rho1 = BDDWrapper.andTo(bdd, rho1, post_i);
				BDDWrapper.free(bdd, post_i);
				
				notOfAllGuarantees = BDDWrapper.andTo(bdd, notOfAllGuarantees, bdd.not(rho1));
				
				rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
				
				int nextCounterValue = (i+1)%guarantees.size();
				int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, Variable.getPrimedCopy(counter));
				int counter_i_update = BDDWrapper.and(bdd, rho1, nextCounter);
				BDDWrapper.free(bdd,nextCounter);
				counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, counter_i_update);
				BDDWrapper.free(bdd, counter_i_update);
				
				
				
				for(int c=0; c<numOfCommands; c++){
					int commandApplicable = BDDWrapper.and(bdd, rho1, commandsSystemTransitions.get(c));
					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//				
	//				int commandApplicable = BDDWrapper.and(bdd, rho1,  cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//				commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
					
					//5+8
	//				int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
	//				commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post_i );
	//				commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, winningStatesPrime );
					
					
					int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
					commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
					BDDWrapper.free(bdd, commandApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(c));
					strategyFragments.set(c, commandWinningStates);
				}
				int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho1);
				int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
				stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
				BDDWrapper.free(bdd, stutterApplicable);
				BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
				strategyFragments.set(numOfCommands, stutterWinningStates);
				
				BDDWrapper.free(bdd, rho1);
				
	//			BDDWrapper.free(bdd, pre);
	//			BDDWrapper.free(bdd, post_i);
				
	//			rho1 = BDDWrapper.andTo(bdd, rho1, winningStatesPrime);
	//			int nextCounterValue = (i+1)%guarantees.size();
	//			int nextCounter = BDDWrapper.assign(bdd, nextCounterValue, counterPrime);
	//			rho1 = BDDWrapper.andTo(bdd, rho1, nextCounter);
				
	//			UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", rho1 ", rho1);
	//			UtilityMethods.getUserInput();
				
	//			strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho1);
				
	//			strategyTransitions.add(rho1);
			}
			
			BDDWrapper.free(bdd, winningStatesPrime);
			
			//finish counter update formula
			int sameCounter = BDDWrapper.same(bdd, counter);
			sameCounter = BDDWrapper.andTo(bdd, sameCounter, notOfAllGuarantees);
			BDDWrapper.free(bdd, notOfAllGuarantees);
			counterUpdateFormula = BDDWrapper.orTo(bdd, counterUpdateFormula, sameCounter);
			BDDWrapper.free(bdd, sameCounter);
			
	//		UtilityMethods.duration(t0, "rho1 computed ");
			
	//		UtilityMethods.debugBDDMethods(bdd, "strategy T_sys after rho1 is", strategy_T_sys);
	//		UtilityMethods.getUserInput();
			
	//		t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				
	//			System.out.println("computing rho2, for guarantee "+i);
				
				ArrayList<Integer> mY_i = mY.get(i);
				int low = bdd.ref(mY_i.get(0));
				
	//			int postLow = BDDWrapper.EpostImage(bdd, mY_i.get(0), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
				
				
	//			UtilityMethods.debugBDDMethods(bdd, "post of mY("+i+")(0) is ", low);
	//			UtilityMethods.getUserInput();
				
				int counter_i = BDDWrapper.assign(bdd, i, counter);
	//			int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
	//			int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
	//			BDDWrapper.free(bdd, counter_i);
	//			BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mY_i.size(); r++){
					
	
					
	//				int rho2 = BDDWrapper.and(bdd, mY_i.get(r), counterPart);
					
	//				int rho2 = BDDWrapper.and(bdd, post_r, counterPart);
					
					int notLow = BDDWrapper.not(bdd, low);
					
					int pre = BDDWrapper.and(bdd, notLow, mY_i.get(r));
					
					BDDWrapper.free(bdd, notLow);
					
	//				int post= BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
					
	//				long t1 = UtilityMethods.timeStamp();
					int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
	//				UtilityMethods.duration(t1, "rho2, post computed");
					
					BDDWrapper.free(bdd, pre);
					
					post = BDDWrapper.andTo(bdd, post, counter_i);
					
					
					int nextLow = BDDWrapper.replace(bdd, low, cgs.getVtoVprime());
	//				int lowPart = BDDWrapper.and(bdd, notLow, nextLow);
					
	//				t1 = UtilityMethods.timeStamp();
	//				int rho2 = BDDWrapper.and(bdd, counterPart, post);
	//				rho2 = BDDWrapper.andTo(bdd, rho2, nextLow);
	//				UtilityMethods.duration(t1, "rho2 disjunct computed");
					
	//				t1 = UtilityMethods.timeStamp();
					
	//				int rho2 = BDDWrapper.and(bdd, post, nextLow);
					
					for(int c=0; c<numOfCommands; c++){
						//1
	//					int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
						
						//2: performed worse than 1
	//					int commandApplicable = BDDWrapper.and(bdd, post, nextLow);
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						//3
	//					int commandApplicable = BDDWrapper.and(bdd, post, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						//4
	//					int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
						
						//5
						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post);
						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextLow);
						
						//6: rho2 based
	//					int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, rho2);
						
						//7: rho2 based
	//					int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), rho2);
	//					commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
						
						int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
						commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
						BDDWrapper.free(bdd, commandApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(c));
						strategyFragments.set(c, commandWinningStates);
					}
					int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
					stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextLow);
					
					//6 rho2 based
	//				int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho2);
					
					int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
					stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
					BDDWrapper.free(bdd, stutterApplicable);
					BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
					strategyFragments.set(numOfCommands, stutterWinningStates);
	//				UtilityMethods.duration(t1, "rho2 disjunct computed");
					
					
					
					BDDWrapper.free(bdd, nextLow);
					
					BDDWrapper.free(bdd, post);
					
	//				BDDWrapper.free(bdd, rho2);
					
	//				t1= UtilityMethods.timeStamp();
					low = BDDWrapper.orTo(bdd, low, mY_i.get(r));
	//				UtilityMethods.duration(t1, "rho2, low updated");
					
	//				
	//				UtilityMethods.debugBDDMethods(bdd, "mY("+i+")"+"("+r+") is ", mY_i.get(r));
	//				UtilityMethods.debugBDDMethods(bdd, "current low is ", low);
	//				UtilityMethods.getUserInput();
					
	//				strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho2);
					
	//				strategyTransitions.add(rho2);
					
	//				UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+", r="+r+", rho2 ", rho2);
	//				UtilityMethods.getUserInput();
					
	//				BDDWrapper.free(bdd, rho2);
				}
	//			BDDWrapper.free(bdd, counterPart);
				
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, low);
			}
			
	//		UtilityMethods.duration(t0, "rho2 computed");
			
	//		UtilityMethods.debugBDDMethods(bdd, "after rho2, strategy T_sys is ",  strategy_T_sys);
	//		UtilityMethods.getUserInput();
			
	//		t0 = UtilityMethods.timeStamp();
			
			for(int i=0; i<numOfGuarantees; i++){
				ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
				int low = bdd.ref(bdd.getZero());
				int counter_i = BDDWrapper.assign(bdd, i, counter);
	//			int nextCounter = BDDWrapper.assign(bdd, i, counterPrime);
	//			int counterPart = BDDWrapper.and(bdd, counter_i, nextCounter);
	//			BDDWrapper.free(bdd, counter_i);
	//			BDDWrapper.free(bdd, nextCounter);
				for(int r=1; r<mX_i.size(); r++){
					ArrayList<Integer> mX_i_r = mX_i.get(r);
					for(int j=0; j< numOfAssumptions; j++){
						
	//					int postmX_irj = BDDWrapper.EpostImage(bdd, mX_i_r.get(j), gs.getVariablesAndActionsCube(), gs.getEnvironmentTransitionRelation(), gs.getVprimetoV());
						
	
						
						int notLow = BDDWrapper.not(bdd, low);
						int notAssumption_j = BDDWrapper.not(bdd, assumptions.get(j));
						
						int pre = BDDWrapper.and(bdd, notLow, notAssumption_j);
						BDDWrapper.free(bdd, notLow);
						BDDWrapper.free(bdd, notAssumption_j);
						pre = BDDWrapper.andTo(bdd, pre, mX_i_r.get(j));
						
	//					int post = BDDWrapper.EpostImage(bdd, pre, cgs.getVariablesAndActionsCube(), cgs.getEnvironmentTransitionRelation(), cgs.getVprimetoV());
						
						int post = cgs.EpostImage(pre, Player.ENVIRONMENT);
						BDDWrapper.free(bdd, pre);
						
						post = BDDWrapper.andTo(bdd, post, counter_i);
						
	//					int rho3 = BDDWrapper.and(bdd, post, counterPart);
						
						int nextMem_irj = BDDWrapper.replace(bdd, mX_i_r.get(j), cgs.getVtoVprime());
						
	//					int rho3 = BDDWrapper.and(bdd, post, nextMem_irj);
						
						for(int c=0; c<numOfCommands; c++){
							//1
	//						int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
							
							//2: performed worse than 1
	//						int commandApplicable = BDDWrapper.and(bdd, post, nextMem_irj);
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							//3: performed better than 1 and 2
	//						int commandApplicable = BDDWrapper.and(bdd, post, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							//4
	//						int commandApplicable = BDDWrapper.and(bdd, post, commandsSystemTransitions.get(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, cgs.getSameUnaffectedVariablesOverSystemTransitions(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
							
							//5: performed the best 
							int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, post);
							commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, nextMem_irj);
							
							//6: rho3 based
	//						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), commandsSystemTransitions.get(c));
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, rho3);
							
							//7: rho3 based
	//						int commandApplicable = BDDWrapper.and(bdd, cgs.getSameUnaffectedVariablesOverSystemTransitions(c), rho3);
	//						commandApplicable = BDDWrapper.andTo(bdd, commandApplicable, commandsSystemTransitions.get(c));
							
							int commandWinningStates = BDDWrapper.exists(bdd, commandApplicable, cgs.getPrimedVariablesAndActionsCube());
							commandWinningStates = BDDWrapper.orTo(bdd, commandWinningStates, strategyFragments.get(c));
							BDDWrapper.free(bdd, commandApplicable);
							BDDWrapper.free(bdd, strategyFragments.get(c));
							strategyFragments.set(c, commandWinningStates);
						}
						int stutterApplicable = game.BDDWrapper.and(bdd, stutter, post);
						stutterApplicable = BDDWrapper.andTo(bdd, stutterApplicable, nextMem_irj);
						
						//6: rho3 based
	//					int stutterApplicable = game.BDDWrapper.and(bdd, stutter, rho3);
						
						int stutterWinningStates = BDDWrapper.exists(bdd, stutterApplicable, cgs.getPrimedVariablesAndActionsCube());
						stutterWinningStates = BDDWrapper.orTo(bdd, stutterWinningStates, strategyFragments.get(numOfCommands));
						BDDWrapper.free(bdd, stutterApplicable);
						BDDWrapper.free(bdd, strategyFragments.get(numOfCommands));
						strategyFragments.set(numOfCommands, stutterWinningStates);
						
	//					rho3 = BDDWrapper.andTo(bdd, rho3, nextMem_irj);
						BDDWrapper.free(bdd, post);
						BDDWrapper.free(bdd, nextMem_irj);
						
	//					BDDWrapper.free(bdd, rho3);
						
						low = BDDWrapper.orTo(bdd, low, mX_i_r.get(j));
						
	//					UtilityMethods.debugBDDMethods(bdd, "guarantee "+i+" r="+r+" j="+j+" rho3 is ", rho3);
	//					UtilityMethods.getUserInput();
						
	//					strategy_T_sys = BDDWrapper.orTo(bdd, strategy_T_sys, rho3);
	//					BDDWrapper.free(bdd, rho3);
						
	//					strategyTransitions.add(rho3);
					}
				}
	//			BDDWrapper.free(bdd, counterPart);
				BDDWrapper.free(bdd, counter_i);
				BDDWrapper.free(bdd, low);
			}
			
	//		UtilityMethods.duration(t0,"rho3");
			
	//		UtilityMethods.debugBDDMethods(bdd, "after rho3, strategy T_sys is ", strategy_T_sys);
	//		UtilityMethods.getUserInput();
			
			
	//		strategy_T_sys = BDDWrapper.andTo(bdd, strategy_T_sys, gs.getSystemTransitionRelation());
			
	//		UtilityMethods.debugBDDMethods(bdd, "final strategy T_sys is", strategy_T_sys);
	//		UtilityMethods.getUserInput();
			
	//		int sameCounter = BDDWrapper.same(bdd, counter);
	//		int strategy_T_env = BDDWrapper.and(bdd, gs.getEnvironmentTransitionRelation(), sameCounter);
	//		BDDWrapper.free(bdd, sameCounter);
			
	//		strategy = new GameStructure(bdd, strategyVars, Variable.getPrimedCopy(strategyVars), strategyInit, strategy_T_env, strategy_T_sys, gs.envActionVars, gs.sysActionVars);
			
			strategy = new OptimizedCompositionalCoordinationStrategy(bdd, cgs, counter, strategyFragments, counterUpdateFormula);
		}else{
			//TODO: compute counter-strategy
			winner = Player.ENVIRONMENT;
		}
		
		GameSolution solution = new GameSolution(cgs, winner, strategy, gr1w.getWinningStates());
		return solution;
	}
	
	/**
	 * Computes winning states for GR1 games without keeping any memory. The goal here is to check the realizability without synthesizing the actual strategy
	 * @param bdd
	 * @param gs
	 * @param init
	 * @param objective
	 * @return
	 * 
	 * TODO: free X and Y
	 */
	public static int computeWinningStates_noMemory(BDDWrapper bdd, SynchronizationGameStructure gs, int init, GR1Objective objective){
		int Z = bdd.getTrue();
		int ZPrime = bdd.getTrue();
		
		ArrayList<Integer> guarantees = objective.getGuarantees();
		ArrayList<Integer> assumptions = objective.getAssumptions();
		
		//preparing the safety part
		int safetyAssumptionsFormula = bdd.getFalse();
		int safetyGuaranteesFormula = bdd.getTrue();
		
		ArrayList<Integer> safetyAssumptions = objective.getSafetyAssumptions();
		ArrayList<Integer> safetyGuarantees = objective.getSafetyGuarantees();
		
		if(safetyAssumptions != null && safetyAssumptions.size()>0){
			for(Integer safetyAssumption : safetyAssumptions){
				safetyAssumptionsFormula = bdd.orTo(safetyAssumptionsFormula, bdd.not(safetyAssumption));
			}
		}
		
		if(safetyGuarantees != null && safetyGuarantees.size()>0){
			for(Integer safetyGuarantee : safetyGuarantees){
				safetyGuaranteesFormula = bdd.andTo(safetyGuaranteesFormula, safetyGuarantee);
			}
		}
		
		int safetyFormula = bdd.or(safetyAssumptionsFormula, safetyGuaranteesFormula);
		bdd.free(safetyAssumptionsFormula);
		bdd.free(safetyGuaranteesFormula);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "safety formula", safetyFormula);
		
		//computing reachable part
		int reachable = gs.reachable(init);
		
//		UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "reachability formula", reachable);
		
		safetyFormula = bdd.and(safetyFormula, reachable);

		do{
			

			
			ZPrime = Z;
			
			
			for(int i=0; i<guarantees.size(); i++){
				int Y = bdd.getFalse();
				int YPrime;
				do{
					YPrime = Y;
					int preZ = gs.controllablePredecessor(Z);
					int preY = gs.controllablePredecessor(Y);
					
					preZ = bdd.andTo(preZ, safetyFormula);
					preY = bdd.andTo(preY, safetyFormula);
					
//					int pre = bdd.or(preZ, preY);
//					int start = bdd.and(guarantees.get(i), pre);
//					bdd.free(pre);
					
					int start = bdd.and(guarantees.get(i), preZ);
					bdd.free(preZ);
					start = bdd.orTo(start, preY);
					bdd.free(preY);
					Y = bdd.getFalse();
					for(int j=0; j<assumptions.size(); j++){
						int X = Z;
						int XPrime;
						do{
							XPrime = X;
							int preX = gs.controllablePredecessor(X);
							
							preX = bdd.andTo(preX, safetyFormula);
							
							X = bdd.and(bdd.not(assumptions.get(j)), preX);
							X = bdd.orTo(X, start);
							bdd.free(preX);
							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "XPrime is", XPrime);
//							UtilityMethods.getUserInput();
//							
//							UtilityMethods.debugBDDMethods(bdd.getBdd(), "X is ", X);
//							UtilityMethods.getUserInput();
							
							
							
							
						}while(X != XPrime);
						
						
						Y = bdd.orTo(Y,X);
						
						
					}
					
					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "YPrime is ", YPrime);
//					UtilityMethods.getUserInput();
//					
//					UtilityMethods.debugBDDMethods(bdd.getBdd(), "Y is", Y);
//					UtilityMethods.getUserInput();
					bdd.free(start);
				}while(Y != YPrime);
				
//				Z = Y;
				
				Z = bdd.bdd.ref(Y);
				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "ZPrime is", ZPrime);
//				UtilityMethods.getUserInput();
//				
//				UtilityMethods.debugBDDMethods(bdd.getBdd(), "Z is", Z);
//				UtilityMethods.getUserInput();
				
				//intersect Z with the set of initial states and 
				//remove the synchronization skeletons that do not cover the initial states
				int initialWinning = bdd.and(Z, init);
				Variable[] allVarsButSynchronizationSignals = Variable.difference(Variable.allVars.toArray(new Variable[Variable.allVars.size()]), gs.getSynchronizationVariables());
				int synchCube = bdd.createCube(allVarsButSynchronizationSignals);
				int winningSynchSkeletons = bdd.exists(initialWinning, synchCube);
				
//				UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "computing winning states, init is",  init);
//				UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "initial winning", initialWinning);
//				UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "synch cube is ", synchCube);
//				UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "Z is ", Z);
//				UtilityMethods.debugBDDMethodsAndWait(bdd.bdd, "winning Synchronization Skeleton", winningSynchSkeletons);
				
				Z = bdd.andTo(Z, winningSynchSkeletons);
			}
			
			
		}while(Z!=ZPrime);
		
		return Z;
	}
}
