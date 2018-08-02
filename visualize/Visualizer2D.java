package visualize;

import game.GameSolvingMethod;

import java.awt.EventQueue;

import javax.swing.JFrame;

import jdd.bdd.BDD;
import testsAndCaseStudies.CaseStudy2D;
import testsAndCaseStudies.CrossingTheStreetsCaseStudy;
import testsAndCaseStudies.PartiallyObservableGameCaseStudy;
import testsAndCaseStudies.TestCAV2016;
import testsAndCaseStudies.TestCompositionalStrategyPruningCaseStudy;
import testsAndCaseStudies.TwoWayRoadCaseStudy;
import testsAndCaseStudies.VMCAI_CaseStudy;
import utils.UtilityMethods;

public class Visualizer2D extends JFrame {
	
	public Visualizer2D(CaseStudy2D caseStudy){
//		add(new Visualizer2DExecuter(caseStudy.getBDD(), caseStudy.getXdim()+1, caseStudy.getYdim()+1,  
//				caseStudy.getSimulator(), caseStudy.getStaticObstacles()));
		add(new Visualizer2DExecuter(caseStudy.getBDD(), caseStudy.getXdim(), caseStudy.getYdim(),  
				caseStudy.getSimulator(), caseStudy.getStaticObstacles()));
			
	    setResizable(false);
	    pack();
	        

	    setTitle("RMP2D");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setLocationRelativeTo(null);
	}
	
	public static void main(String[] args){
		 EventQueue.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	            	int dim = 8;
	            	
	            	long t0 = UtilityMethods.timeStamp();
	            	//initiate the case study
//	            	CaseStudy2D cs = TwoWayRoadCaseStudy.createSimpleTwoWayRoadCaseStudy(dim, dim);
	            	
	            	
//	            	CaseStudy2D cs = CrossingTheStreetsCaseStudy.createSimpleCrossingTheStreetsCaseStudy(dim);
	            	
//	            	CaseStudy2D cs = VMCAI_CaseStudy.createVMCAI_CaseStudy(dim);
	            	
//	            	CaseStudy2D cs = PartiallyObservableGameCaseStudy.createPartiallyObservableCaseStudy(dim);
	            	
//	            	CaseStudy2D cs = TestCompositionalStrategyPruningCaseStudy.create_compositionalStrategyPruningCaseStudy(dim);
	            	
	            	BDD bdd = new BDD(10000, 1000);
	            	CaseStudy2D cs=null;
	            	try{
	            		cs = TestCAV2016.runExperiment(bdd, dim, GameSolvingMethod.Compositional_StrategyPruning);
	            	}catch(Exception e){
	            		e.printStackTrace();
	            	}
	            	
	            	
//	            	CaseStudy2D cs = new CaseStudy2D(bdd, argXDim, argYDim, argAgents, argGame, argStatic)
	            	
	            	UtilityMethods.duration(t0, "the whole process was finished in ");
	            	
	            	//run the visualization
	               Visualizer2D ex = new Visualizer2D(cs);
	                ex.setVisible(true);
	            }
	        });
	}
}
