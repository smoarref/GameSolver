package csguided;

import jdd.bdd.BDD;
import automaton.GameGraph;

/**
 * Keep the results of solving a game:
 * 1- The winner: environment or the system
 * 2- The set of winning states for the system
 * 3- The computed strategy for the winner: a winning strategy for the system, 
 * or a counter-strategy for the environment
 * 4- A reference to the game structure
 * @author moarref
 *
 */
public class GameSolution {
	private Game game;
	private Player winner;
	private Game strategyOfTheWinner;
	private int winningSystemStates;
	
	public GameSolution(Game argGame, Player argWinner, Game argStrategy, int winningStates){
		game = argGame;
		winner = argWinner;
		strategyOfTheWinner = argStrategy;
		winningSystemStates = winningStates;
	}
	
	public Player getWinner(){
		return winner;
	}
	
	public Game getGameStructure(){
		return game;
	}
	
	public Game strategyOfTheWinner(){
		return strategyOfTheWinner;
	}
	
	public int getWinningSystemStates(){
		return winningSystemStates;
	}
	
	public void cleanUp(BDD bdd){
		BDDWrapper.free(bdd, winningSystemStates);
		strategyOfTheWinner.cleanUp();
	}
	
	public void print(){
		System.out.println("*********************************************");
		System.out.println("Printing the game solution");
		System.out.println("The winner is "+winner);
//		System.out.println("strategy of the winner is ");
//		GameGraph gg = strategyOfTheWinner.toGameGraph("1");
//		gg.print();
		System.out.println("*********************************************");
	}
	
	public void drawWinnerStrategy(String file){
		GameGraph gg = strategyOfTheWinner.toGameGraph("1");
		gg.draw(file, 1);
	}
	
	public void drawWinnerStrategy(String file, int nodeVerbosity, int edgeVerbosity){
		GameGraph gg = strategyOfTheWinner.toGameGraph("1");
		gg.draw(file, nodeVerbosity, edgeVerbosity);
	}
}
