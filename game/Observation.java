package game;

import java.util.ArrayList;

import specification.Agent2D;
import utils.UtilityMethods;
import jdd.bdd.BDD;

public class Observation {
		private BDD bdd;
		private Variable[] observableVars=null;
		private int observationMap=-1;
		
		public Observation(BDD argBdd){
			bdd= argBdd;
		}
		
		public Observation(BDD argBdd, int dim, Agent2D uncontrolledAgent, Agent2D controlledAgent){
			bdd= argBdd;
			int numOfVars = uncontrolledAgent.getVariables().length + controlledAgent.getVariables().length;
			observableVars =  Variable.createVariables(bdd, numOfVars, "obsVars_"+controlledAgent.getName());
			observationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, 
					dim, uncontrolledAgent, controlledAgent, observableVars);
		}
		
		public Observation(BDD argBDD, int dim, ArrayList<Agent2D> uncontrolledAgents, ArrayList<Agent2D> controlledAgents){
			bdd = argBDD;
			observationMap = bdd.ref(bdd.getOne());
			for(Agent2D uncontrolled : uncontrolledAgents){
				for(Agent2D controlled : controlledAgents){
					
					int numOfVars = uncontrolled.getVariables().length + controlled.getVariables().length;
					Variable[] observables = Variable.createVariables(bdd, numOfVars, "obsVars_"+uncontrolled.getName()+"_"+controlled.getName());
					observableVars = Variable.unionVariables(observableVars, observables);
					
					int currentObservationMap = -1;
					
					if(controlled.getNonCoordinationVars().length != 0){
						currentObservationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicitWithExtraVars(bdd, dim, uncontrolled, controlled, observables);
					}else{
						currentObservationMap = TurnBasedPartiallyObservableGameStructure.createSimpleLocalObservationMap2DExplicit(bdd, dim, uncontrolled, controlled, observables);
					}
					
					observationMap = BDDWrapper.andTo(bdd, observationMap, currentObservationMap);
					BDDWrapper.free(bdd, currentObservationMap);
				}
			}
		}
		
		public Variable[] getObservableVars(){
			return observableVars;
		}
		
		public int getObservationMap(){
			return observationMap;
		}
}
