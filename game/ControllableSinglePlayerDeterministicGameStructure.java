package game;


import utils.UtilityMethods;
import jdd.bdd.BDD;

public class ControllableSinglePlayerDeterministicGameStructure extends GameStructure {

	public ControllableSinglePlayerDeterministicGameStructure(BDD argBdd) {
		super(argBdd);
		// TODO Auto-generated constructor stub
	}
	
	public ControllableSinglePlayerDeterministicGameStructure(BDD argBdd, Variable[] argVariables,  int sysTrans,  Variable[] argSysActions){
		super(argBdd);
		init = -1;
		T_env= -1;
		T_sys= bdd.ref(sysTrans);
		variables = argVariables;
		primedVariables = Variable.getPrimedCopy(variables);
		vTovPrime=bdd.createPermutation(Variable.getBDDVars(variables), Variable.getBDDVars(primedVariables));
		vPrimeTov=bdd.createPermutation(Variable.getBDDVars(primedVariables), Variable.getBDDVars(variables));
			
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
		envActionVars = null;
		sysActionVars = argSysActions;
		actionVars=sysActionVars;
		actionsCube=-1;
			
		envActions = null;
		if(sysActionVars != null && sysActionVars.length != 0){
			sysActions = UtilityMethods.enumerate(bdd, Variable.getBDDVars(sysActionVars));
		}
		actions = sysActions;
			
		T_envList=null;
		T_sysList=null;
	}
	
	public int controllablePredecessor(int set){
		if(set == 0){
			return bdd.getZero();
		}
		
		int cube = getPrimedVariablesAndActionsCube();
		
		int cPre = BDDWrapper.EpreImage(bdd, set, cube, getSystemTransitionRelation(), vTovPrime);
		
		return cPre;
	}
	
	public int symbolicGameOneStepExecution(int set){
		int cube=getVariablesAndActionsCube();
		int sysMoves = EpostImage(set, cube, getSystemTransitionRelation());
		return sysMoves;
	}
	
	public ControllableSinglePlayerDeterministicGameStructure composeWithController(int controller){
		ControllableSinglePlayerDeterministicGameStructure result = new ControllableSinglePlayerDeterministicGameStructure(bdd, variables, controller, actionVars);
		result.setInit(getInit());
		return result;
	}
}
