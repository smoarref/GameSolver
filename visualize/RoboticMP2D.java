package visualize;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import javax.swing.Timer;
import javax.swing.JPanel;

import specification.Agent2D;
import specification.AgentType;
import utils.UtilityMethods;
import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import game.BDDWrapper;
import game.GameStructure;
import specification.GridCell2D;
import game.Variable;

public class RoboticMP2D extends JPanel implements ActionListener {

    private int width, height, rows, columns;
    private int cellHeight, cellWidth;
    
    private GameStructure game;
    private ArrayList<Variable[]> envXvars;
    private ArrayList<Variable[]> envYvars;
    private ArrayList<Variable[]> envVars;
    
    private ArrayList<Variable[]> sysXvars;
    private ArrayList<Variable[]> sysYvars;
    private ArrayList<Variable[]> sysVars;
    
    private Variable[] vars;
    
    ArrayList<GridCell2D> envGridCells;
    ArrayList<GridCell2D> sysGridCells;
    
    ArrayList<GridCell2D> previousEnvGridCells;
    ArrayList<GridCell2D> previousSysGridCells;
    
    //Initial cells
    private ArrayList<GridCell2D> initEnvCells;
    private ArrayList<GridCell2D> initSysCells;

    
    private ArrayList<ArrayList<GridCell2D>> possibleEnvNextCells;
    private ArrayList<ArrayList<GridCell2D>> possibleSysNextCells;
    

    
    
    private final int DELAY = 1000;
    private Timer timer;
    BDD bdd;

    private int agentCircleSize;
    private int cleaningRectSize;
    
    ArrayList<GridCell2D> staticObstacles;
    
    private int currentStateOfTheSystem=-1;
    
    private ArrayList<String> minterms;

    
    public RoboticMP2D(int h, int w, int r, int c, BDD argBdd, GameStructure argGame, 
    		ArrayList<Agent2D> agents, ArrayList<GridCell2D> staticObs){
    	rows=r;
        columns=c;
        width=w;
        height=h;
        setPreferredSize(new Dimension(w+1, h+1)); 
        
        staticObstacles = staticObs;
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
       agentCircleSize = cellHeight/3;
       cleaningRectSize = cellHeight/2;
        
        setDoubleBuffered(true);
        
        game = argGame;
        
        vars=argGame.variables;
        
        envXvars =  new ArrayList<Variable[]>();
        envYvars = new ArrayList<Variable[]>();
        envVars =  new ArrayList<Variable[]>();
        
        sysXvars = new ArrayList<Variable[]>();
        sysYvars = new ArrayList<Variable[]>();
        sysVars =  new ArrayList<Variable[]>();
        
        for(Agent2D agent : agents){
        	if(agent.getType() == AgentType.Uncontrollable){
        		envXvars.add(agent.getXVars());
        		envYvars.add(agent.getYVars());
        		envVars.add(agent.getVariables());
        	}else{
        		sysXvars.add(agent.getXVars());
        		sysYvars.add(agent.getYVars());
        		sysVars.add(agent.getVariables());
        	}
        }
    	
    	bdd=argBdd;
    	
    	parseInit(); 	
    	
        timer = new Timer(DELAY, this);
        timer.start();
        
    }
    
    public RoboticMP2D(int h, int w, int r, int c, BDD argBdd, GameStructure argGame, 
    		ArrayList<Variable[]> argEnvX, ArrayList<Variable[]> argEnvY, ArrayList<Variable[]> argSysX
    		, ArrayList<Variable[]> argSysY, ArrayList<GridCell2D> staticObs){
    	
    	rows=r;
        columns=c;
        width=w;
        height=h;
        setPreferredSize(new Dimension(w+1, h+1)); 
        
        staticObstacles = staticObs;
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
       agentCircleSize = cellHeight/3;
       cleaningRectSize = cellHeight/2;
        
        setDoubleBuffered(true);
        
        game = argGame;
        envXvars = argEnvX;
        envYvars = argEnvY;
        envVars=new ArrayList<Variable[]>();
        for(int i=0;i<envXvars.size();i++){
        	envVars.add(Variable.unionVariables(envXvars.get(i), envYvars.get(i)));
        }
        
        sysXvars = argSysX;
        sysYvars = argSysY;
        sysVars=new ArrayList<Variable[]>();
        for(int i=0;i<sysXvars.size();i++){
        	sysVars.add(Variable.unionVariables(sysXvars.get(i), sysYvars.get(i)));
        }
        
//        vars=envVars.get(0);
//        for(int i=1; i<envVars.size();i++){
//        	vars=Variable.unionVariables(vars, envVars.get(i));
//        }
//        
//        for(int i=0;i<sysVars.size();i++){
//        	vars=Variable.unionVariables(vars, sysVars.get(i));
//        }
        
        vars=argGame.variables;
        
    	
    	bdd=argBdd;
    	
    	parseInit(); 	
    	
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    
    
    public void parseInit(){
    	
    	ArrayList<ArrayList<GridCell2D>> envInit = getEnvGridCells(game.getInit());
    	ArrayList<ArrayList<GridCell2D>> sysInit = getSysGridCells(game.getInit());
    	
    	//TODO: generalize
    	initEnvCells = new ArrayList<GridCell2D>();
    	for(int i=0;i<envInit.size();i++){
    		initEnvCells.add(envInit.get(i).get(0));
    	}
    	
    	initSysCells = new ArrayList<GridCell2D>();
    	for(int i=0;i<sysInit.size();i++){
    		initSysCells.add(sysInit.get(i).get(0));
    	}
    	
    	envGridCells = initEnvCells;
    	sysGridCells = initSysCells;
    	
    	previousEnvGridCells = null;
    	previousSysGridCells = null;
    	
    	currentStateOfTheSystem = bdd.ref(game.getInit());
    
    }
    
    public ArrayList<GridCell2D> setOfStatesToGridCells(int states, Variable[] x, Variable[] y){
    	Variable[] relatedVariables = Variable.unionVariables(x, y);
    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, relatedVariables);
    	ArrayList<GridCell2D> gridCells = new ArrayList<GridCell2D>();
    	for(String m : minterms){
    		gridCells.add(getGridCellFromMinterm(m,x,y));
    	}
    	return gridCells;
    }
    
   
    
    public ArrayList<ArrayList<GridCell2D>> getEnvGridCells(int setOfStates){
    	return getGridCells(setOfStates, envXvars, envYvars);
    }
    
    public ArrayList<ArrayList<GridCell2D>> getSysGridCells(int setOfStates){
    	return getGridCells(setOfStates, sysXvars, sysYvars);
    }
    
    public ArrayList<ArrayList<GridCell2D>> getGridCells(int setOfStates, ArrayList<Variable[]> x, ArrayList<Variable[]> y){
    	ArrayList<ArrayList<GridCell2D>> result=new ArrayList<ArrayList<GridCell2D>>();
    	if(x.size() != y.size()){
    		System.err.println("sizes are not equal!");
    		return null;
    	}
    	
    	for(int i=0;i<x.size();i++){
    		Variable[] xVars = x.get(i);
    		Variable[] yVars = y.get(i);
    		ArrayList<GridCell2D> gridCells_i = setOfStatesToGridCells(setOfStates, xVars, yVars);
    		result.add(gridCells_i);
    	}
    	
    	return result;
    }
    

    
    public void nextCells(){
//    	int currentState = computeCurrentState();
//    	
//    	UtilityMethods.debugBDDMethods(bdd, "current state is ", currentState);
//    	
//    	int nextCells = game.symbolicGameOneStepExecution(currentState);
    	
    	int nextCells = game.symbolicGameOneStepExecution(currentStateOfTheSystem);
    	
    	bdd.deref(currentStateOfTheSystem);
//    	UtilityMethods.debugBDDMethods(bdd, "next cells are ", nextCells);
    	
    	computePossibleNextStates(nextCells);
    	bdd.deref(nextCells);
    	
//    	possibleEnvNextCells = getEnvGridCells(nextCells);
//    	
//    	possibleSysNextCells = getSysGridCells(nextCells);
    }
    
    public void computePossibleNextStates(int states){
    	possibleEnvNextCells = new ArrayList<ArrayList<GridCell2D>>();
    	possibleSysNextCells = new ArrayList<ArrayList<GridCell2D>>();
    	
//    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , states, vars);
    	minterms = BDDWrapper.minterms(bdd , states, vars);
    	
//    	minterms = BDDWrapper.parsePrintSetOutput(bdd, states);
    	
    	if(minterms == null ){
    		System.err.println("next possible states are null!");
    		return;
    	}
//    	System.out.println("minterms");
//    	for(String m : minterms){
//    		System.out.println(m);
//    	}
    	
    	
//    	int index=0;
    	for(int i=0; i<envVars.size();i++){
    		ArrayList<GridCell2D> possibleNext = new ArrayList<GridCell2D>();
    		for(int j=0; j<minterms.size();j++){
    			String minterm = minterms.get(j);
    			
//    			System.out.println("minterm is "+minterm);
//    			String filteredMinterm = minterm.substring(index, index+envVars.get(i).length);
//    			System.out.println("filtered minterm "+filteredMinterm);
    			
//    			String filteredMinterm = keepRelevant(minterm, envVars.get(i));
//    			System.out.println("filtered minterm "+filteredMinterm);

//    			String filteredMintermX = BDDWrapper.keepRelevantVariables(minterm, envXvars.get(i));
//    			
//    			System.out.println("env filteredX "+filteredMintermX);
//    			
//    			String filteredMintermY = BDDWrapper.keepRelevantVariables(minterm, envYvars.get(i));
//    			
//    			System.out.println("env filteredY "+filteredMintermY);
    			
//    			String filteredMinterm = filteredMintermX+filteredMintermY;
//    			possibleNext.add(getGridCellFromMinterm(filteredMinterm,envXvars.get(i),envYvars.get(i)));
    			
    			possibleNext.add(getGridCellFromMinterm(keepRelevant(minterm,envXvars.get(i)),keepRelevant(minterm, envYvars.get(i))));
    		}
//    		index+=envVars.get(i).length;
    		possibleEnvNextCells.add(possibleNext);
    	}
    		
    	for(int i=0; i<sysVars.size();i++){
    		ArrayList<GridCell2D> possibleNext = new ArrayList<GridCell2D>();
    		for(int j=0; j<minterms.size();j++){
    			String minterm = minterms.get(j);
    			
//    			System.out.println("minterm is "+minterm);
//    			String filteredMinterm = minterm.substring(index, index+sysVars.get(i).length);
    			
//    			String filteredMinterm = keepRelevant(minterm, sysVars.get(i));
//    			System.out.println("filtered minterm "+filteredMinterm);
    			
//    			String filteredMintermX = BDDWrapper.keepRelevantVariables(minterm, sysXvars.get(i));
//    			
//    			System.out.println("sys filteredX "+filteredMintermX);
//    			
//    			String filteredMintermY = BDDWrapper.keepRelevantVariables(minterm, sysYvars.get(i));
//    			
//    			System.out.println("sys filteredY "+filteredMintermY);
//    			
//    			String filteredMinterm = filteredMintermX+filteredMintermY;
    			
//    			possibleNext.add(getGridCellFromMinterm(filteredMinterm,sysXvars.get(i),sysYvars.get(i)));
    			
    			possibleNext.add(getGridCellFromMinterm(keepRelevant(minterm,sysXvars.get(i)),keepRelevant(minterm, sysYvars.get(i))));
    		}
//    		index+=sysVars.get(i).length;
    		possibleSysNextCells.add(possibleNext);
    	}
    	
    }
    
    public GridCell2D getGridCellFromMinterm(String xBinary, String yBinary){
    	
    	int xValue = Integer.parseInt(xBinary,2);
    	int yValue = Integer.parseInt(yBinary,2);
    	
    	GridCell2D gc = new GridCell2D(xValue, yValue);
    	return gc;
    }
    
    public GridCell2D getGridCellFromMinterm(String binary, Variable[] x , Variable[] y){
    	String xBinary = binary.substring(0, x.length);
    	String yBinary = binary.substring(y.length);
    	
    	int xValue = Integer.parseInt(xBinary,2);
    	int yValue = Integer.parseInt(yBinary,2);
    	
    	GridCell2D gc = new GridCell2D(xValue, yValue);
    	return gc;
    }
    
//    public int computeCurrentState(){
//    	int currentState=bdd.getOne();
//    	
//    	
//    	for(int i=0; i<envGridCells.size();i++){
//    		currentState = bdd.andTo(currentState, assignCell(envXvars.get(i), envYvars.get(i), envGridCells.get(i)));
//    	}
//    	
//    	for(int i=0;i<sysGridCells.size();i++){
//    		currentState = bdd.andTo(currentState, assignCell(sysXvars.get(i), sysYvars.get(i), sysGridCells.get(i)));
//    	}
//    	
//    	return currentState;
//    }
    

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
//	    Ellipse2D e = new Ellipse2D.Double(x, y, 40, 40);
//        g2d.setStroke(new BasicStroke(1));
//        g2d.setColor(Color.black);
//        g2d.draw(e);
	    
	    if(staticObstacles != null){
	    	for(GridCell2D so : staticObstacles){
			    g2d.setColor(Color.BLACK);
			    g2d.fillRect(so.getX()*htOfRow, so.getY()*wdOfRow, htOfRow, wdOfRow);
	    	}
	    }
	    //cleaning the board
	    if(previousEnvGridCells != null){
		    for(int i=0;i<previousEnvGridCells.size();i++){
		    	if(previousEnvGridCells == null || previousEnvGridCells.get(i).getX()==-1) break;
		    	GridCell2D coordination = cellCoordination(previousEnvGridCells.get(i));
		    	g2d.clearRect(coordination.getX(), coordination.getY(),cleaningRectSize, cleaningRectSize);
		    }
	    }
	    
	    if(previousSysGridCells != null){
		    for(int i=0;i<previousSysGridCells.size();i++){
		    	if(previousSysGridCells == null || previousSysGridCells.get(i).getX()==-1) break;
		    	GridCell2D coordination = cellCoordination(previousSysGridCells.get(i));
		    	g2d.clearRect(coordination.getX(), coordination.getY(),cleaningRectSize, cleaningRectSize);
		    }
	    }
	    
	    //drawing the environment
	    for(int i=0; i<envGridCells.size(); i++){
	    	g2d.setColor(Color.RED);
	    	GridCell2D coordination = cellCoordination(envGridCells.get(i));
	    	g2d.fillOval(coordination.getX(), coordination.getY(), agentCircleSize, agentCircleSize);
	    }
        
	    //TODO: generalize
	    //drawing the system
	    for(int i=0; i<sysGridCells.size(); i++){
	    	if(i==0) g2d.setColor(Color.BLACK);
	    	if(i==1) g2d.setColor(Color.GREEN);
	    	if(i==2) g2d.setColor(Color.BLUE);
	    	if(i>=3) g2d.setColor(Color.MAGENTA);
	    	GridCell2D coordination = cellCoordination(sysGridCells.get(i));
	    	g2d.fillOval(coordination.getX(), coordination.getY(), agentCircleSize, agentCircleSize);
	    }
        
        
        Toolkit.getDefaultToolkit().sync();
   }
    
    @Override
    public void actionPerformed(ActionEvent e) {

    	for(int i=0;i<envGridCells.size();i++){
    		for(int j=0; j<sysGridCells.size();j++){
    			GridCell2D env = envGridCells.get(i);
    			GridCell2D sys = sysGridCells.get(j);
    			if(env.getX() == sys.getX() && env.getY() == sys.getY()){
    				System.out.println("collision");
    				UtilityMethods.getUserInput();
    			}
    		}
    	}
//    	UtilityMethods.getUserInput();
    	
    	computeNextState();

    	repaint();
    }
    
  //TODO: generalize
    public void computeNextState(){
    	
    	previousEnvGridCells = envGridCells;
    	previousSysGridCells = sysGridCells;
    	
    	nextCells();
    	
    	chooseNextCell();
    }
    
    private void chooseNextCell(){
//    	printLocations();
    	
    	int numOfPossibleNextLoctions = possibleEnvNextCells.get(0).size();
    	int randomIndex = randomIndex(numOfPossibleNextLoctions);
    	
    	envGridCells=new ArrayList<GridCell2D>();
        
    	for(int i=0; i<possibleEnvNextCells.size();i++){
    		ArrayList<GridCell2D> possibleNextcells = possibleEnvNextCells.get(i);
    		envGridCells.add(possibleNextcells.get(randomIndex));
    	}
    	
    	sysGridCells=new ArrayList<GridCell2D>();
    	
    	for(int i=0; i<possibleSysNextCells.size();i++){
    		ArrayList<GridCell2D> possibleNextcells = possibleSysNextCells.get(i);
    		sysGridCells.add(possibleNextcells.get(randomIndex));
    	}
    	
//    	System.out.println("chosen is "+minterms.get(randomIndex));
    	currentStateOfTheSystem = BDDWrapper.mintermToBDDFormula(bdd, minterms.get(randomIndex), vars);
//    	UtilityMethods.debugBDDMethods(bdd,"current state of the system", currentStateOfTheSystem);
//    	printLocations();
    }
    
    public GridCell2D cellCoordination(GridCell2D location){
    	int xValue = cellHeight*location.getX()+cellHeight/3;
    	int yValue = cellWidth*location.getY()+cellWidth/3;
    	return new GridCell2D(xValue,yValue);
    }
    
	public int assignCell(Variable[] x, Variable[] y , GridCell2D cell){
		int xAssign=BDDWrapper.assign(bdd, cell.getX(), x);
		int yAssing=BDDWrapper.assign(bdd, cell.getY(), y);
		int result=bdd.ref(bdd.and(xAssign, yAssing));
		bdd.deref(xAssign);
		bdd.deref(yAssing);
		return result;
	}
	
	private int randomIndex(int bound){
		return (int) Math.floor(bound*Math.random());
	}
	
	public void printLocations(){
		System.out.println("environment grid cells");
    	for(int i=0; i<envGridCells.size();i++){
    		envGridCells.get(i).print();
    	}
    	
    	System.out.println("system grid cells");
    	for(int i=0; i<sysGridCells.size();i++){
    		sysGridCells.get(i).print();
    	}
	}
    
	
	private String keepRelevant(String minterm, Variable[] relatedvars){
//		String result="";
//		for(int i=0;i<relatedvars.length;i++){
//			for(int j=0;j<minterm.length();j++){
//				if(vars[j].getBDDPosition() == relatedvars[i].getBDDPosition()){
//					result+=minterm.charAt(j);
//				}
//			}
//		}
//		return result;
		
		return BDDWrapper.keepRelevantVariables(minterm, vars, relatedvars);
	}
	
    
}
