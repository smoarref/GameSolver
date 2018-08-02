package csguided;

import automaton.GameGraph;

public class FairGameSolution {
	private FairGame game;
	private Player winner;
	private FairGame strategyOfTheWinner;
	private int winningSystemStates;
	
	public FairGameSolution(FairGame argGame, Player argWinner, FairGame argStrategy, int winningStates){
		game = argGame;
		winner = argWinner;
		strategyOfTheWinner = argStrategy;
		winningSystemStates = winningStates;
	}
	
	public Player getWinner(){
		return winner;
	}
	
	public FairGame getGameStructure(){
		return game;
	}
	
	public FairGame strategyOfTheWinner(){
		return strategyOfTheWinner;
	}
	
	public int getWinningSystemStates(){
		return winningSystemStates;
	}
	
	public void setWinner(Player w){
		winner=w;
	}
	
	public void setStrategyOfTheWinner(FairGame strategy){
		strategyOfTheWinner=strategy;
	}
	
	public boolean isRealizable(){
		if(winner==Player.SYSTEM){
			return true;
		}
		return false;
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
		GameGraph gg = strategyOfTheWinner.removeUnreachableStates().toGameGraph("1");
		gg.draw(file, 1);
	}
	
	public void drawWinnerStrategy(String file, int nodeVerbosity, int edgeVerbosity){
		GameGraph gg = strategyOfTheWinner.removeUnreachableStates().toGameGraph("1");
		gg.draw(file, nodeVerbosity, edgeVerbosity);
	}
}
