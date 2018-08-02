package visualize;

import game.GameSolvingMethod;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JFrame;

import jdd.bdd.BDD;
import specification.GridCell2D;
import swarm.SwarmRoboticAlgorithmsTester;
import swarm.SwarmSimulator2D;
import testsAndCaseStudies.CaseStudy2D;
import testsAndCaseStudies.TestCAV2016;
import utils.UtilityMethods;

public class SwarmVisualizer2D extends JFrame{
	public SwarmVisualizer2D(int dim, SwarmSimulator2D simulator, ArrayList<GridCell2D> staticObstacles){
//		add(new Visualizer2DExecuter(caseStudy.getBDD(), caseStudy.getXdim()+1, caseStudy.getYdim()+1,  
//				caseStudy.getSimulator(), caseStudy.getStaticObstacles()));
		add(new SwarmVisualizer2DExecuter(dim, dim,  
				simulator, staticObstacles));
			
	    setResizable(false);
	    pack();
	        

	    setTitle("SwarmVisualizer2D");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	}
	
	public static void main(String[] args){
		 EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	int dim = 3;
	            	
	            	long t0 = UtilityMethods.timeStamp();
	            	//initiate the case study
	            	SwarmSimulator2D simulator = SwarmRoboticAlgorithmsTester.simpleTest1(dim);
	            	
	            	UtilityMethods.duration(t0, "the whole process was finished in ");
	            	
	            	ArrayList<GridCell2D> staticObstacles = null;
	            	
	            	//run the visualization
	               SwarmVisualizer2D ex = new SwarmVisualizer2D(dim, simulator, staticObstacles);
	               
	                ex.setVisible(true);
	            }
	        });
	}
}
