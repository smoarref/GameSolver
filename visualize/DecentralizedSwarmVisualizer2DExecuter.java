package visualize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

import jdd.bdd.BDD;
import specification.GridCell2D;
import swarm.DecentralizedSwarmSimulator2D;
import swarm.SwarmSimulator2D;
import utils.UtilityMethods;

public class DecentralizedSwarmVisualizer2DExecuter extends JPanel implements ActionListener{

	private final int height = 800;
	private final int width = 800;
	private int rows, columns;
	private int cellHeight, cellWidth;
	
	private final int DELAY = 500;
    private Timer timer;
    BDD bdd;

    private int agentCircleSize;
    private int cleaningRectSize;
    
    ArrayList<GridCell2D> staticObstacles;
    ArrayList<GridCell2D> targetRegions;
    
    DecentralizedSwarmSimulator2D simulator;
    
    ArrayList<GridCell2D> currentGridCells;
    ArrayList<GridCell2D> previousGridCells;
    
    public DecentralizedSwarmVisualizer2DExecuter(int xDim, int yDim,  DecentralizedSwarmSimulator2D argSimulator, 
			ArrayList<GridCell2D> argStaticObstacles ){
		
		rows=xDim;
        columns=yDim;
        setPreferredSize(new Dimension(width+1, height+1)); 
        
        
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
       agentCircleSize = cellHeight/3;
       cleaningRectSize = cellHeight/2;
        
        setDoubleBuffered(true);
        
       simulator = argSimulator;
        
       staticObstacles = argStaticObstacles;
       targetRegions = simulator.getTargetRegions();
       
        currentGridCells = simulator.getInitialPositions();
        
        previousGridCells = null;
        
        adjustAgentCircleSize();
        
    	
        timer = new Timer(DELAY, this);
        timer.start();
	}
    
    private void adjustAgentCircleSize(){
    	int numOfLocalStrategies = currentGridCells.size();
    	agentCircleSize = cellHeight/(2*numOfLocalStrategies);
    }
    
    @Override
    public void paintComponent(Graphics g) {
    	
//    	printLocations();
//    	UtilityMethods.getUserInput();
    	
    	g.clearRect(0, 0, width, height);
    	
	    int k;
	    int htOfRow = height / (rows);
	    for (k = 0; k <= rows; k++)
	        g.drawLine(0, k * htOfRow , width, k * htOfRow );
	
	    int wdOfRow = width / (columns);
	    for (k = 0; k <= columns; k++)
	    	g.drawLine(k*wdOfRow , 0, k*wdOfRow , height);
	    
	    Graphics2D g2d = (Graphics2D) g;
	    
	    
	    
	    //cleaning the board
	    if(previousGridCells != null){
		    for(int i=0;i<previousGridCells.size();i++){
		    	if(previousGridCells.get(i).getX()==-1) break;
		    	GridCell2D coordination = cellCoordination(previousGridCells.get(i));
		    	coordination = adjustCoordination(coordination, i);
		    	g2d.clearRect(coordination.getX(), coordination.getY(),cleaningRectSize, cleaningRectSize);
		    }
	    }
	    
	    if(staticObstacles != null){
	    	for(GridCell2D so : staticObstacles){
			    g2d.setColor(Color.BLACK);
			    g2d.fillRect(so.getX()*htOfRow, so.getY()*wdOfRow, htOfRow, wdOfRow);
	    	}
	    }
	    
	    if( targetRegions!= null){
	    	for(GridCell2D so : targetRegions){
			    g2d.setColor(Color.YELLOW);
			    g2d.fillRect(so.getX()*htOfRow, so.getY()*wdOfRow, htOfRow, wdOfRow);
	    	}
	    }
	    
	    int controlledCounter = 0;
	    
	    //drawing the current state
	    //TODO: assumes that currentGridCells and agents have the same number of elements
	    ArrayList<Integer> waitingAgents = simulator.getWaitingProcesses();
//	    for(int i=0; i<waitingAgents.size(); i++){
//	    	System.out.println(waitingAgents.get(i));
//	    }
//	    System.out.println();
	    for(int i=0 ; i<currentGridCells.size(); i++){
	    	//setting the color for agents
	    	if(controlledCounter == 0){
	    		g2d.setColor(Color.BLACK);
	    		controlledCounter++;
	    	}else if(controlledCounter == 1){
	    		g2d.setColor(Color.GREEN);
	    		controlledCounter++;
	    	}else if(controlledCounter == 2){
	    		g2d.setColor(Color.BLUE);
	    		controlledCounter++;
	    	}else if(controlledCounter == 3){
	    		g2d.setColor(Color.GRAY);
	    		controlledCounter++;
	    	}else if(controlledCounter == 4){
	    		g2d.setColor(Color.DARK_GRAY);
	    		controlledCounter++;
	    	}else if(controlledCounter == 5){
	    		g2d.setColor(Color.ORANGE);
	    		controlledCounter++;
	    	}else if(controlledCounter == 6){
	    		g2d.setColor(Color.PINK);
	    		controlledCounter++;
	    	}else if(controlledCounter == 7){
	    		g2d.setColor(Color.CYAN);
	    		controlledCounter++;
	    	}else if(controlledCounter == 8){
	    		g2d.setColor(Color.MAGENTA);
	    		controlledCounter++;
	    	}else{
	    		g2d.setColor(Color.GRAY);
	    	}
	    	
	    	if(waitingAgents.contains(i)){
	    		g2d.setColor(Color.RED);
	    	}
	    	
	    	GridCell2D coordination = cellCoordination(currentGridCells.get(i));
	    	coordination = adjustCoordination(coordination, i);
	    	g2d.fillOval(coordination.getX(), coordination.getY(), agentCircleSize, agentCircleSize);
	    }
        
        
        Toolkit.getDefaultToolkit().sync();
   }
    
    public GridCell2D cellCoordination(GridCell2D location){
    	int xValue = cellHeight*location.getX()+cellHeight/3;
    	int yValue = cellWidth*location.getY()+cellWidth/3;
    	return new GridCell2D(xValue,yValue);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		computeNextState();

    	repaint();
		
	}
	
	public void computeNextState(){
    	
    	previousGridCells = currentGridCells;
    	
    	currentGridCells = simulator.simulateOneStep();
    	
    	if(currentGridCells == null){
    		System.out.println("There is no next state!");
    		UtilityMethods.getUserInput();
    	}
    }
	
	//adjusts coordinations according to the number of local strategies
	public GridCell2D adjustCoordination(GridCell2D input, int index){
		int numOfLocalStrategies = currentGridCells.size();
		int middle = numOfLocalStrategies/2;
		int adjust = (index - middle)*agentCircleSize;
		GridCell2D result = new GridCell2D(input.getX() + adjust, input.getY()+adjust);
		return result;
	}

}
