package visualize;

import game.Simulator2D;
import game.Variable;

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

import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import utils.UtilityMethods;
import jdd.bdd.BDD;

public class Visualizer2DExecuter extends JPanel implements ActionListener{
	
	private final int height = 800;
	private final int width = 800;
	private int rows, columns;
	private int cellHeight, cellWidth;
	
	private final int DELAY = 1000;
    private Timer timer;
    BDD bdd;

    private int agentCircleSize;
    private int cleaningRectSize;
    
    ArrayList<GridCell2D> staticObstacles;
    
    Simulator2D simulator;
    ArrayList<Agent2D> agents;
    
    ArrayList<GridCell2D> currentGridCells;
    ArrayList<GridCell2D> previousGridCells;
    
    
	
	public Visualizer2DExecuter(BDD argBDD, int xDim, int yDim,  Simulator2D argSimulator, 
			ArrayList<GridCell2D> argStaticObstacles ){
		
		bdd=argBDD;
		rows=xDim;
        columns=yDim;
        setPreferredSize(new Dimension(width+1, height+1)); 
        
        staticObstacles = argStaticObstacles;
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
       agentCircleSize = cellHeight/3;
       cleaningRectSize = cellHeight/2;
        
        setDoubleBuffered(true);
        
        simulator = argSimulator;
        agents = simulator.getAgents();
        
        currentGridCells = simulator.getInitialPositions();
        
        previousGridCells = null;
        
    	
        timer = new Timer(DELAY, this);
        timer.start();
	}
	
    @Override
    public void paintComponent(Graphics g) {
    	
//    	printLocations();
//    	UtilityMethods.getUserInput();
    	
	    int k;
	    int htOfRow = height / (rows);
	    for (k = 0; k <= rows; k++)
	        g.drawLine(0, k * htOfRow , width, k * htOfRow );
	
	    int wdOfRow = width / (columns);
	    for (k = 0; k <= columns; k++)
	    	g.drawLine(k*wdOfRow , 0, k*wdOfRow , height);
	    
	    Graphics2D g2d = (Graphics2D) g;
	    
	    if(staticObstacles != null){
	    	for(GridCell2D so : staticObstacles){
			    g2d.setColor(Color.BLACK);
			    g2d.fillRect(so.getX()*htOfRow, so.getY()*wdOfRow, htOfRow, wdOfRow);
	    	}
	    }
	    //cleaning the board
	    if(previousGridCells != null){
		    for(int i=0;i<previousGridCells.size();i++){
		    	if(previousGridCells == null || previousGridCells.get(i).getX()==-1) break;
		    	GridCell2D coordination = cellCoordination(previousGridCells.get(i));
		    	g2d.clearRect(coordination.getX(), coordination.getY(),cleaningRectSize, cleaningRectSize);
		    }
	    }
	    
	    int controlledCounter = 0;
	    
	    //drawing the current state
	    //TODO: assumes that currentGridCells and agents have the same number of elements
	    for(int i=0 ; i<currentGridCells.size(); i++){
	    	//setting the color for agents
	    	if(agents.get(i).getType() == AgentType.Uncontrollable){
	    		g2d.setColor(Color.RED);
	    	}else if(controlledCounter == 0){
	    		g2d.setColor(Color.BLACK);
	    		controlledCounter++;
	    	}else if(controlledCounter == 1){
	    		g2d.setColor(Color.GREEN);
	    		controlledCounter++;
	    	}else if(controlledCounter == 2){
	    		g2d.setColor(Color.BLUE);
	    		controlledCounter++;
	    	}else{
	    		g2d.setColor(Color.GRAY);
	    	}
	    	GridCell2D coordination = cellCoordination(currentGridCells.get(i));
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
    }

}
