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

import jdd.bdd.BDD;
import jdd.bdd.Permutation;
import csguided.BDDWrapper;
import csguided.GridCell2D;
import csguided.Variable;

public class GameBoard extends JPanel implements ActionListener {

    private int width, height, rows, columns;
    private int cellHeight, cellWidth;
    private int x,y;
    private int xCoordinate, yCoordinate;
    private int previousX = -1;
    private int previousY = -1;
    private int previousXC = -1;
    private int previousYC = -1;
    private final int DELAY = 1000;
    private  int INITIAL_X = 25;
    private  int INITIAL_Y = 25;
    
    private  int INITIAL_XC = 25;
    private  int INITIAL_YC = 25;
    private Timer timer;
    
    int init;
    int transitionRelation;
    Variable[] xVars;
    Variable[] yVars;
    Variable[] xVarsPrime;
    Variable[] yVarsPrime;
    Variable[] vars;
    int variablesCube;
    Permutation vPrimeToV;
    BDD bdd;
    
    

    public GameBoard(int h, int w, int r, int c) {
    	
        init(h,w,r,c);
    }
    
    public GameBoard(int h, int w, int r, int c, BDD argBdd, int argInit, int argTrans, 
    		Variable[] argxVars, Variable[] argyVars, int argVarCube, Permutation argVtoVP){
    	
    	rows=r;
        columns=c;
        width=w;
        height=h;
        setPreferredSize(new Dimension(w+1, h+1)); 
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
       
        
        setDoubleBuffered(true);
        

    	
    	bdd=argBdd;
    	init=argInit;
    	transitionRelation = argTrans;
    	xVars = argxVars;
    	yVars = argyVars;
    	xVarsPrime = Variable.getPrimedCopy(xVars);
    	yVarsPrime = Variable.getPrimedCopy(yVars);
    	vars= Variable.unionVariables(xVars, yVars);
    	variablesCube = argVarCube;
    	vPrimeToV = argVtoVP;
    	parseInit();
    	
    	 x=INITIAL_X;
         y=INITIAL_Y;
         
         GridCell2D initC = cellCoordination(new GridCell2D(x, y));
         
         xCoordinate = initC.getX();
         yCoordinate = initC.getY();
    	
        timer = new Timer(DELAY, this);
        timer.start();
    }
    
    public void parseInit(){
    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , init, vars);
    	if(minterms.size() > 1){
    		System.out.println("more than one init states");
    	}
    	
    	GridCell2D initCell = getGridCellFromMinterm(minterms.get(0));
    	System.out.println("init cell is");
    	initCell.print();
    	INITIAL_X = initCell.getX();
    	INITIAL_Y = initCell.getY();
    	GridCell2D initCellCoordination = cellCoordination(initCell);
    	initCellCoordination.print();
    	INITIAL_XC = initCellCoordination.getX();
    	INITIAL_YC = initCellCoordination.getY();
    }
    
    public GridCell2D formulaToGridCell(int formula){
    	ArrayList<String> minterms = BDDWrapper.minterms(bdd , formula, vars);
    	if(minterms.size() > 1){
    		System.out.println("more than one states exists");
    	}
    	
    	GridCell2D cell = getGridCellFromMinterm(minterms.get(0));
    	return cell;
    }
    
    public GridCell2D nextCell(){
    	int currentCellX = BDDWrapper.assign(bdd, x, xVars);
    	int currentCellY = BDDWrapper.assign(bdd, y, yVars);
    	int currentCell = BDDWrapper.and(bdd, currentCellX, currentCellY);
    	BDDWrapper.free(bdd, currentCellX);
    	BDDWrapper.free(bdd, currentCellY);
    	
    	int intersect = BDDWrapper.and(bdd, currentCell, transitionRelation);
    	int nextCellsPrime = bdd.exists(intersect, variablesCube);
    	int nextCells = bdd.ref(bdd.replace(nextCellsPrime, vPrimeToV));
    	
    	return formulaToGridCell(nextCells);
    }
    
    public GridCell2D getGridCellFromMinterm(String binary){
    	String xBinary = binary.substring(0, xVars.length);
    	String yBinary = binary.substring(xVars.length);
    	
    	int xValue = Integer.parseInt(xBinary,2);
    	int yValue = Integer.parseInt(yBinary,2);
    	
    	GridCell2D gc = new GridCell2D(xValue, yValue);
    	return gc;
    }
    
    public void init(int h, int w, int r, int c){
    	rows=r;
        columns=c;
        width=w;
        height=h;
        setPreferredSize(new Dimension(w+1, h+1)); 
        
        cellHeight = height / rows;
        cellWidth = width / columns;
        
        x=INITIAL_X;
        y=INITIAL_Y;
        
        setDoubleBuffered(true);
        
        timer = new Timer(DELAY, this);
        timer.start();
    }
    

    @Override
    public void paintComponent(Graphics g) {
    	
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
        
        g2d.fillOval(xCoordinate, yCoordinate, cellHeight/3, cellWidth/3);
        
        if(previousX != -1) g2d.clearRect(previousXC, previousYC,cellHeight/2, cellWidth/2);
        
        
        Toolkit.getDefaultToolkit().sync();
   }
    
    @Override
    public void actionPerformed(ActionEvent e) {

    	previousX = x;
    	previousY = y;
    	
    	previousXC = xCoordinate;
    	previousYC = yCoordinate;
    	
    	GridCell2D nextGrid = nextCell();
    	
    	x = nextCell().getX();
    	y= nextCell().getY();
    	 
    	GridCell2D nextGridCoordination = cellCoordination(nextGrid);
    	
        xCoordinate = nextGridCoordination.getX();
        yCoordinate = nextGridCoordination.getY();

       

        repaint();
    }
    
    public GridCell2D cellCoordination(GridCell2D location){
    	int xValue = cellHeight*location.getX()+cellHeight/4;
    	int yValue = cellWidth*location.getY()+cellWidth/4;
    	return new GridCell2D(xValue,yValue);
    }
    
    
}
