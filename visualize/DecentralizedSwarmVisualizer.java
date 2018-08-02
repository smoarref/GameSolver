package visualize;

import game.GameSolvingMethod;

import java.awt.EventQueue;
import java.util.ArrayList;

import javax.swing.JFrame;

import jdd.bdd.BDD;
import specification.GridCell2D;
import swarm.DecentralizedSwarmSimulator2D;
import swarm.SwarmRoboticAlgorithmsTester;
import swarm.SwarmSimulator2D;
import testsAndCaseStudies.CaseStudy2D;
import testsAndCaseStudies.TestCAV2016;
import utils.UtilityMethods;
	
public class DecentralizedSwarmVisualizer extends JFrame{

		public DecentralizedSwarmVisualizer(int dim, DecentralizedSwarmSimulator2D simulator, ArrayList<GridCell2D> staticObstacles){
//			add(new Visualizer2DExecuter(caseStudy.getBDD(), caseStudy.getXdim()+1, caseStudy.getYdim()+1,  
//					caseStudy.getSimulator(), caseStudy.getStaticObstacles()));
			add(new DecentralizedSwarmVisualizer2DExecuter(dim, dim,  
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
//		            	int dim = 4;
		            	
		            	long t0 = UtilityMethods.timeStamp();
		            	//initiate the case study
		            	DecentralizedSwarmSimulator2D simulator = SwarmRoboticAlgorithmsTester.decentralizedSymbolicSwarmControlTest(3);
		            	
		            	UtilityMethods.duration(t0, "the whole process was finished in ");
		            	
		            	ArrayList<GridCell2D> staticObstacles = null;
		            	
		            	int dim = simulator.getDimension();
		            	
		            	//run the visualization
		               DecentralizedSwarmVisualizer ex = new DecentralizedSwarmVisualizer(dim, simulator, staticObstacles);
		               
		                ex.setVisible(true);
		            }
		        });
		}

}

