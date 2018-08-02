package testsAndCaseStudies;

import java.util.ArrayList;

import utils.UtilityMethods;
import utils.UtilityTransitionRelations;
import game.BDDWrapper;
import automaton.GameGraph;
import jdd.bdd.BDD;
import game.GameSolution;
import game.GameSolver;
import game.GameStructure;
import game.Objective;
import game.Objective.ObjectiveType;
import game.Variable;

public class SampleGameStructureGenerator {
	public static GameStructure simpleGame(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable l = new Variable(bdd, "l");
		
		
		Variable input[] = {a};
		Variable output[] = {b};
		Variable[] inputPrime={aPrime};
		Variable[] outputPrime={bPrime};
		Variable[] actions={l};
		int[] avl={};
		
		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		
		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t1=bdd.andTo(t1, l.getBDDVar());
		t1=bdd.andTo(t1, aPrime.getBDDVar());
		t1=bdd.andTo(t1, bPrime.getBDDVar());
		
		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
		t2=bdd.andTo(t2, bPrime.getBDDVar());
		
		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
		t3=bdd.andTo(t3, aPrime.getBDDVar());
		t3=bdd.andTo(t3, bPrime.getBDDVar());
		
		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
		t4=bdd.andTo(t4, bPrime.getBDDVar());
		
		int T_env = bdd.ref(bdd.or(t1,t2));
		T_env=bdd.orTo(T_env, t3);
		T_env=bdd.orTo(T_env, t4);
		
		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t5=bdd.andTo(t5, l.getBDDVar());
		t5=bdd.andTo(t5, aPrime.getBDDVar());
		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
		
		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t6=bdd.andTo(t6, l.getBDDVar());
		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
		
		
		int T_sys=bdd.ref(bdd.or(t5, t6));
		
		GameStructure simpleGame=new GameStructure(bdd, Variable.unionVariables(input, output), Variable.unionVariables(inputPrime, outputPrime), init, T_env, T_sys, actions, actions);
		return simpleGame;
	}
	
	public static GameStructure simpleGameStructure(BDD bdd, int dim){
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, dim, "X");
		Variable[] output = Variable.createVariablesAndTheirPrimedCopy(bdd, dim, "Y");
		Variable[] actionVars = Variable.createVariables(bdd, 1, "inc");
		int[] actions = UtilityMethods.enumerate(bdd, actionVars);
		
		int envInit = BDDWrapper.assign(bdd, 0, input);
		int sysInit = BDDWrapper.assign(bdd, dim, output);
		int init = BDDWrapper.and(bdd, envInit, sysInit);
		
		int envInc = UtilityTransitionRelations.addOne(bdd, input);
		int envDec = UtilityTransitionRelations.subtractOne(bdd, input);
		
		int envIncTrans = BDDWrapper.and(bdd, envInc, actions[1]);
		int envDecTrans = BDDWrapper.and(bdd, envDec, actions[0]);
		
		int envTrans = BDDWrapper.or(bdd, envIncTrans, envDecTrans);
		int sameSysTrans = BDDWrapper.same(bdd, output);
		envTrans = BDDWrapper.andTo(bdd, envTrans, sameSysTrans);
		
		int sysInc = UtilityTransitionRelations.addOne(bdd, output);
		int sysDec = UtilityTransitionRelations.subtractOne(bdd, output);
		
		int sysIncTrans = BDDWrapper.and(bdd, sysInc, actions[1]);
		int sysDecTrans = BDDWrapper.and(bdd, sysDec, actions[0]);
		
		int sysTrans = BDDWrapper.or(bdd, sysIncTrans, sysDecTrans);
		int sameEnvTrans = BDDWrapper.same(bdd, input);
		sysTrans = BDDWrapper.andTo(bdd, sysTrans, sameEnvTrans);
		
		Variable[] vars= Variable.unionVariables(input, output);
		GameStructure g = new GameStructure(bdd,  vars, Variable.getPrimedCopy(vars), init, envTrans, sysTrans, actionVars, actionVars);
		return g;
		
	}
	
	/**
	 * Two robots that can stay put or move forward in a circle, the controlled robot can only observe itself and 
	 * one step ahead of itself
	 * @param bdd
	 * @param dim
	 * @return
	 */
	public static GameStructure simpleGameStructureForTestingPartiallyObservableGamesWithStayPut(BDD bdd, int dim){
		
		
		int numOfBits = UtilityMethods.numOfBits(dim-1);
		
		//define vars
//		Variable[] all_vars = UtilityMethods.createVariablesAndTheirPrimedCopiesForAgentsWithInterleavingOrder(bdd, 2, "R", "X", numOfBits);
//		
//
//		
//		Variable[] output = Variable.getVariablesStartingWith(all_vars, "R1");
//		Variable[] input = Variable.getVariablesStartingWith(all_vars, "R0");
//		
//		Variable.printVariables(output);
//		System.out.println();
//		Variable.printVariables(input);
//		UtilityMethods.getUserInput();
		
		Variable[] actionVars = Variable.createVariables(bdd, 1, "inc");
		int[] actions = UtilityMethods.enumerate(bdd, actionVars);
		
		Variable[] output = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "Y");
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "X");
		

		
		Variable[] inputPrime = Variable.getPrimedCopy(input);
		Variable[] outputPrime = Variable.getPrimedCopy(output);
		
		int envInit = BDDWrapper.assign(bdd, 0, input);
		int sysInit = BDDWrapper.assign(bdd,dim-1, output);
		int init = BDDWrapper.and(bdd, envInit, sysInit);
		
		int envStayPut = BDDWrapper.same(bdd, input);
		envStayPut = BDDWrapper.andTo(bdd, envStayPut, actions[0]);
		
		int sysStayPut = BDDWrapper.same(bdd, output);
		sysStayPut = BDDWrapper.andTo(bdd, sysStayPut, actions[0]);
		
		int envTrans = envStayPut;
		int sysTrans = sysStayPut;
		
		for(int i=0; i<dim; i++){
			
			
			
			
			if(i==dim-1){
				//env inc
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, 0, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[1]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys inc
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, 0, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[1]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
			
			
			
			if(i != dim-1){
				//env inc
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, i+1, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[1]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys inc
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, i+1, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[1]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
		}
		
		int sameSysTrans = BDDWrapper.same(bdd, output);
		envTrans = BDDWrapper.andTo(bdd, envTrans, sameSysTrans);

		
		int sameEnvTrans = BDDWrapper.same(bdd, input);
		sysTrans = BDDWrapper.andTo(bdd, sysTrans, sameEnvTrans);
		
		Variable[] vars= Variable.unionVariables(input, output);
		GameStructure g = new GameStructure(bdd,  vars, Variable.getPrimedCopy(vars), init, envTrans, sysTrans, actionVars, actionVars);
		return g;
		
	}
	
	
	
	/**
	 * Two robots in one dimensional space,the robot can only see its own location and one step ahead of himself
	 * @param bdd
	 * @param dim
	 * @return
	 */
	public static GameStructure simpleGameStructureForPartiallyObservableGameTesting(BDD bdd, int dim){
		int numOfBits = UtilityMethods.numOfBits(dim-1);
		Variable[] input = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "X");
		Variable[] output = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfBits, "Y");
		Variable[] actionVars = Variable.createVariables(bdd, 1, "inc");
		int[] actions = UtilityMethods.enumerate(bdd, actionVars);
		
		Variable[] inputPrime = Variable.getPrimedCopy(input);
		Variable[] outputPrime = Variable.getPrimedCopy(output);
		
		int envInit = BDDWrapper.assign(bdd, 0, input);
		int sysInit = BDDWrapper.assign(bdd, 0, output);
		int init = BDDWrapper.and(bdd, envInit, sysInit);
		
		int envTrans = bdd.ref(bdd.getZero());
		int sysTrans = bdd.ref(bdd.getZero());
		
		for(int i=0; i<dim; i++){
			if(i==0){
				//env dec
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, dim-1, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[0]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys dec
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, dim-1, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[0]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
			
			if(i==dim-1){
				//env inc
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, 0, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[1]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys inc
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, 0, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[1]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
			
			if(i != 0){
				//env dec
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, i-1, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[0]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys dec
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, i-1, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[0]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
			
			if(i != dim-1){
				//env inc
				int s = BDDWrapper.assign(bdd, i, input);
				int d = BDDWrapper.assign(bdd, i+1, inputPrime);
				int envInc = BDDWrapper.and(bdd, s, d);
				envInc = BDDWrapper.andTo(bdd, envInc, actions[1]);
				envTrans = BDDWrapper.orTo(bdd, envTrans, envInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, envInc);
				
				//sys inc
				s = BDDWrapper.assign(bdd, i, output);
				d = BDDWrapper.assign(bdd, i+1, outputPrime);
				int sysInc = BDDWrapper.and(bdd, s, d);
				sysInc = BDDWrapper.andTo(bdd, sysInc, actions[1]);
				sysTrans = BDDWrapper.orTo(bdd, sysTrans, sysInc);
				BDDWrapper.free(bdd, s);
				BDDWrapper.free(bdd, d);
				BDDWrapper.free(bdd, sysInc);
			}
		}
		

		int sameSysTrans = BDDWrapper.same(bdd, output);
		envTrans = BDDWrapper.andTo(bdd, envTrans, sameSysTrans);

		
		int sameEnvTrans = BDDWrapper.same(bdd, input);
		sysTrans = BDDWrapper.andTo(bdd, sysTrans, sameEnvTrans);
		
		Variable[] vars= Variable.unionVariables(input, output);
		GameStructure g = new GameStructure(bdd,  vars, Variable.getPrimedCopy(vars), init, envTrans, sysTrans, actionVars, actionVars);
		return g;
		
	}
	
	public static GameStructure simpleGameWith3varsList(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr",a.getType(), a);
		Variable bPrime=new Variable(bdd, "bPr", b.getType(), b);
		Variable cPrime=new Variable(bdd, "cPr", c.getType(), c);
		a.setPrimedCopy(aPrime);
		b.setPrimedCopy(bPrime);
		c.setPrimedCopy(cPrime);
		
		Variable l1 = new Variable(bdd, "l1");
		Variable l2 = new Variable(bdd, "l2");

		Variable[] inputVars={a};
		Variable[] primeInputVars={aPrime};
		
		Variable[] outputVars={b,c};
		Variable[] primeOutputVars={bPrime, cPrime};
		
		Variable[] vars={a,b,c};
		Variable[] primeVars={aPrime, bPrime, cPrime};
		Variable[] actionVars={l1,l2};
		
		int init = bdd.ref(bdd.minterm("000-----"));
		int te[] = new int[4];
		ArrayList<Integer> T_envList=new ArrayList<Integer>();
		te[0]=bdd.ref(bdd.minterm("0--0--00"));
		te[1]=bdd.ref(bdd.minterm("0--1--11"));
		te[2]=bdd.ref(bdd.minterm("1--0--00"));
		te[3]=bdd.ref(bdd.minterm("1--1--11"));
		
		int sameOutput = BDDWrapper.same(bdd, outputVars, primeOutputVars);
		
		int T_env = bdd.ref(bdd.getZero());
		for(int i=0;i<te.length;i++){
			te[i]=bdd.andTo(te[i], sameOutput);
			T_envList.add(te[i]);
			T_env=bdd.orTo(T_env, te[i]);
//			bdd.deref(te[i]);
		}
		
//		T_env=bdd.andTo(T_env, sameOutput);
		bdd.deref(sameOutput);
		
		int ts1[] = new int[4];
		ts1[0]=bdd.ref(bdd.minterm("-0--0-0-"));
		ts1[1]=bdd.ref(bdd.minterm("-0--1-1-"));
		ts1[2]=bdd.ref(bdd.minterm("-1--0-0-"));
		ts1[3]=bdd.ref(bdd.minterm("-1--1-1-"));

		int ts2[] = new int[4];
		ts2[0]=bdd.ref(bdd.minterm("--0--0-0"));
		ts2[1]=bdd.ref(bdd.minterm("--0--1-1"));
		ts2[2]=bdd.ref(bdd.minterm("--1--0-0"));
		ts2[3]=bdd.ref(bdd.minterm("--1--1-1"));
		
		int T_sys=bdd.ref(bdd.getZero());
		ArrayList<Integer> T_sysList=new ArrayList<Integer>();
		int sameInput=BDDWrapper.same(bdd, inputVars, primeInputVars);
		
		
		for(int i=0;i<ts1.length;i++){
			for(int j=0;j<ts2.length;j++){
				int disjunct = bdd.ref(bdd.and(ts1[i],ts2[j]));
				disjunct = bdd.andTo(disjunct, sameInput);
				T_sysList.add(disjunct);
				T_sys=bdd.orTo(T_sys, disjunct);
			}
		}
		
		bdd.deref(sameInput);
		
		
		
		GameStructure simpleGame=new GameStructure(bdd, vars, primeVars, init, T_env, T_sys, actionVars, actionVars);
		simpleGame.setT_envList(T_envList);
		simpleGame.setT_sysList(T_sysList);
		
//		simpleGame.environmentActionAvailabilitySet();
//		simpleGame.systemActionAvailabilitySet();
		
//		int[] envActions=simpleGame.envAvlActions;
//		int[] sysActions=simpleGame.sysAvlActions;
//		
//		System.out.println("env avl set");
//		for(int avl : envActions ){
//			bdd.printSet(avl);
//		}
//		
//		System.out.println("sys avl set");
//		for(int avl : sysActions ){
//			bdd.printSet(avl);
//		}
		
		return simpleGame;
	}
	
	public static GameStructure simpleNondeterministicGame(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable aPrime=new Variable(bdd, "aPr");
		Variable bPrime=new Variable(bdd, "bPr");
		Variable l = new Variable(bdd, "l");
		//Variable d = new Variable(bdd, "d");
		
		Variable[] vars = {a,b};
		Variable[] varPrime = {aPrime, bPrime};
		Variable[] actions={l};
		
		
		int init = bdd.ref(bdd.and(a.getBDDVar(), b.getBDDVar()));
		
//		int t1 = bdd.and(a.getBDDVar(), b.getBDDVar());
//		t1=bdd.andTo(t1, l.getBDDVar());
//		t1=bdd.andTo(t1, aPrime.getBDDVar());
//		t1=bdd.andTo(t1, bPrime.getBDDVar());
		int t1=bdd.getZero();
		
		int t2 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t2=bdd.andTo(t2, bdd.not(l.getBDDVar()));
		t2=bdd.andTo(t2, bdd.not(aPrime.getBDDVar()));
		t2=bdd.andTo(t2, bPrime.getBDDVar());
		
		int t3 = bdd.and(a.getBDDVar(), bdd.not(b.getBDDVar()));
		t3=bdd.andTo(t3, bdd.not(l.getBDDVar()));
		t3=bdd.andTo(t3, aPrime.getBDDVar());
		t3=bdd.andTo(t3, bPrime.getBDDVar());
		
		int t4 = bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(l.getBDDVar()));
		t4=bdd.andTo(t4, bdd.not(aPrime.getBDDVar()));
		t4=bdd.andTo(t4, bPrime.getBDDVar());
		
		int T_env = bdd.ref(bdd.or(t1,t2));
		T_env=bdd.orTo(T_env, t3);
		T_env=bdd.orTo(T_env, t4);
		
		int t5 = bdd.and(a.getBDDVar(), b.getBDDVar());
		t5=bdd.andTo(t5, l.getBDDVar());
		t5=bdd.andTo(t5, aPrime.getBDDVar());
		t5=bdd.andTo(t5, bdd.not(bPrime.getBDDVar()));
		
		int t6 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t6=bdd.andTo(t6, l.getBDDVar());
		t6=bdd.andTo(t6, bdd.not(aPrime.getBDDVar()));
		t6=bdd.andTo(t6, bdd.not(bPrime.getBDDVar()));
		
		int t7 = bdd.ref(bdd.and(bdd.not(a.getBDDVar()), bdd.not(b.getBDDVar())));
		t7=bdd.andTo(t7, l.getBDDVar());
		t7=bdd.andTo(t7, aPrime.getBDDVar());
		t7=bdd.andTo(t7, bPrime.getBDDVar());
		
		int T_sys=bdd.ref(bdd.or(t5, t6));
		T_sys =bdd.orTo(T_sys, t7);
		
		int t9 = bdd.minterm("01101");
		T_sys =bdd.orTo(T_sys, t9);
		
		//adding an environment transition
		int t8 = bdd.and(bdd.not(a.getBDDVar()), b.getBDDVar());
		t8=bdd.andTo(t8, l.getBDDVar());
		t8=bdd.andTo(t8, bdd.not(aPrime.getBDDVar()));
		t8=bdd.andTo(t8, bdd.not(bPrime.getBDDVar()));
		
		T_env = bdd.orTo(T_env, t8);
		
		GameStructure simpleGame=new GameStructure(bdd,vars, varPrime, init, T_env, T_sys, actions, actions);
		return simpleGame;
	}
	
	public static GameStructure simpleGame3(BDD bdd){
		Variable a = new Variable(bdd, "a");
		Variable b = new Variable(bdd, "b");
		Variable c = new Variable(bdd, "c");
		Variable aPrime=new Variable(bdd, "aPr",a.getType(), a);
		Variable bPrime=new Variable(bdd, "bPr", b.getType(), b);
		Variable cPrime=new Variable(bdd, "cPr", c.getType(), c);
		a.setPrimedCopy(aPrime);
		b.setPrimedCopy(bPrime);
		c.setPrimedCopy(cPrime);
		Variable l = new Variable(bdd, "l");
		
		Variable[] variables={a,b,c};
		Variable[] variablesPrime={aPrime, bPrime, cPrime};
		Variable[] actions={l};
		int[] envAvl=new int[2];
		int[] sysAvl=new int[2];
		
		//define init
		int init = bdd.minterm("111----");
		
		//define environment transitions
		int t[]=new int[8];
		t[0]=bdd.minterm("1111111");
		t[1]=bdd.minterm("1111101");
		t[2]=bdd.minterm("1110110");
		t[3]=bdd.minterm("1110100");
		t[4]=bdd.minterm("1011110");
		t[5]=bdd.minterm("1011100");
		t[6]=bdd.minterm("0010110");
		t[7]=bdd.minterm("0010100");
		
		int envTrans=bdd.getZero();
		for(int i=0;i<t.length;i++){
			envTrans=bdd.orTo(envTrans, t[i]);
			bdd.deref(t[i]);
		}
		
		//define system transitions
		int[] s=new int[4];
		s[0]=bdd.minterm("1111011");
		s[1]=bdd.minterm("1101011");
		s[2]=bdd.minterm("0110011");
		s[3]=bdd.minterm("0100011");
		
		int sysTrans=bdd.ref(bdd.getZero());
		for(int i=0;i<s.length;i++){
			sysTrans=bdd.orTo(sysTrans, s[i]);
			bdd.deref(s[i]);
		}
		
		//define available actions for environment states
		//env states
		int e1=bdd.minterm("111----");
		int e2=bdd.minterm("101----");
		int e3=bdd.minterm("001----");
		//states where action l=0
		envAvl[0]=bdd.ref(bdd.or(e1, e2));
		//states where action l=1
		envAvl[1]=bdd.ref(bdd.or(e1,e3));
		
		bdd.deref(e1);
		bdd.deref(e2);
		bdd.deref(e3);
		
		//define available actions for system states		
		//sys states
		int s1=bdd.minterm("111----");
		int s2=bdd.minterm("110----");
		int s3=bdd.minterm("011----");
		int s4=bdd.minterm("010----");
		
		//states where action l=0
		sysAvl[0]=bdd.getZero();
		//states where action l=1
		sysAvl[1]=bdd.ref(bdd.or(s1,s2));
		sysAvl[1]=bdd.orTo(sysAvl[1], s3);
		sysAvl[1]=bdd.orTo(sysAvl[1], s4);
				
		bdd.deref(s1);
		bdd.deref(s2);
		bdd.deref(s3);
		bdd.deref(s4);
		
		GameStructure game=new GameStructure(bdd, variables, variablesPrime, init, envTrans, sysTrans, actions, actions);
		return game;
	}
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000,1000);
//		GameStructure simpleGame = SampleGameStructureGenerator.simpleGame(bdd);
		GameStructure simpleGame = SampleGameStructureGenerator.simpleGameWith3varsList(bdd);
//		GameStructure simpleGame = SampleGameStructureGenerator.simpleNondeterministicGame(bdd);
//		GameStructure simpleGame = SampleGameStructureGenerator.simpleGame3(bdd);
		simpleGame.printGame();
		GameGraph gg = simpleGame.toGameGraph();
		gg.draw("simpleGame.dot", 1);
		
		int[] vars = simpleGame.getVariables();
		
//		int obj = bdd.ref(bdd.and(bdd.not(vars[0]), vars[1]));
//		int objective = bdd.ref(bdd.not(obj));
//		bdd.deref(obj);
//		bdd.printSet(objective);
		
//		GameSolver gs = new GameSolver(simpleGame, objective, bdd);
//		GameSolution sol = gs.solve();
//		sol.print();
		
		//testing safety objectives
		int safetyObj = bdd.ref(bdd.biimp(vars[0], vars[2]));
		
		GameSolution sol = GameSolver.solve(bdd, simpleGame, safetyObj);
		
		//testing reachability objective 
		//eventually vars[1] = 1
		int reachabilityObjective = bdd.ref(vars[1]);
		
//		sol = GameSolver.solveForReachabilyObjectives(bdd, simpleGame, reachabilityObjective, safetyObj);
		Objective safetyObjective1 = new Objective(ObjectiveType.Safety, safetyObj);
		ArrayList<Objective> safetyObjectives = new ArrayList<Objective>();
		safetyObjectives.add(safetyObjective1);
		Objective reachabilityObjective1 = new Objective(ObjectiveType.Reachability, reachabilityObjective);
		ArrayList<Objective> reachabilityObjectives = new ArrayList<Objective>();
		
		reachabilityObjectives.add(reachabilityObjective1);
		sol = GameSolver.solveSafetyReachabilityObjectives(bdd, simpleGame, safetyObjectives, reachabilityObjectives);
		
		sol.print();
		sol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("strat.dot", 1);
		bdd.printSet(sol.getController());
	}
}
