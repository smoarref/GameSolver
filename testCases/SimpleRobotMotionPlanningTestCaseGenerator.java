package testCases;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import jdd.bdd.BDD;
import symbolic.BDDWrapper;
import symbolic.SymbolicGame;

//TODO: init is changed after solving the game, probably something happens during the reachable or synthesis procedure, check
public class SimpleRobotMotionPlanningTestCaseGenerator {
	
	private int dimension;
	
	public int getDimension(){
		return dimension;
	}
	
	public void setDimension(int dim){
		dimension=dim;
	}

	public static void bddTest(){
		BDD bdd= new BDD(10000, 1000);
		int x=bdd.createVar();
		int y=bdd.createVar();
		
		int xp=bdd.createVar();
		int yp=bdd.createVar();
		
		int[] vars={x,y};
		int[] varsPrime={xp, yp};
		
		bdd.createPermutation(vars, varsPrime);
		
		int formula = bdd.ref(bdd.or(x, y));
		bdd.printCubes(formula);
		
		int ynot=bdd.ref(bdd.not(y));
		//int cube = y;
		int restrictedFormula=bdd.restrict(formula, ynot);
		bdd.printSet(restrictedFormula);
		
		if(bdd.member(formula, new boolean[]{true, false})){
			System.out.println("x=1 & y=0 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{false, true})){
			System.out.println("x=0 & y=1 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{false, false})){
			System.out.println("x=0 & y=0 is a member");
		}
		
		if(bdd.member(formula, new boolean[]{true, true})){
			System.out.println("x=1 & y=1 is a member");
		}
		
		int cube=bdd.ref(x);
		int e=bdd.ref(bdd.exists(bdd.restrict(formula, cube), cube));
		bdd.printSet(e);
		
	}
	public static void main(String[] args) {
		long t0 = System.currentTimeMillis();
		//testRMP(2);
		//testCompositionalRMP(14);
		bddTest();
		long t1= System.currentTimeMillis();
		System.out.println("Time spent "+(t1-t0));

	}

	private static int numOfBits(int number){
		int guess=(int) Math.floor(Math.log(number)/Math.log(2));
		if(Math.pow(2,guess)>number){
			
			return guess;
		}
		return guess+1;
		
	}
	
	public static int[] concatenateArrays(int[] arr1, int[] arr2){
		int length = arr1.length + arr2.length;
		int[] result=new int[length];
		int arr1length=arr1.length;
		for(int i=0;i<length;i++){
			if(i<arr1.length){
				result[i]=arr1[i];
			}else{
				result[i]=arr2[i-arr1length];
			}
		}
		return result;
	}
	
	public static int stateToBDD(BDD bdd, int[] variable, int value){
		int[] num = intToBinary(value);
		return stateToBDD(bdd, variable, num);
	}
	
	public static int stateToBDD(BDD bdd, int[] variable, int[] value){
		int result = 1;
		int[] valuePrime=value;
		if(variable.length > value.length){
			//append zero in front of value
			int[] zeroArray=new int[variable.length - value.length];
			for(int i=0; i<zeroArray.length; i++){
				zeroArray[i]=0;
			}
			valuePrime=concatenateArrays(zeroArray, valuePrime);
		}
		for(int i=0; i<variable.length;i++){
			int tmp;
			tmp = bdd.ref(valuePrime[i]==0 ? bdd.not(variable[i]) : variable[i]);
			result = bdd.andTo(result, tmp);
			bdd.deref(tmp);
		}
		return result;
	}
	
	public static int[] intToBinary(int num){
		String binary=Integer.toBinaryString(num);
		int length=binary.length();
		int[] result=new int[length];
		for(int i=0;i<length;i++){
			//result[length-1-i]=(binary.charAt(i)=='0'? 0 : 1);
			result[i]=(binary.charAt(i)=='0'? 0 : 1);
		}
		return result;
	}
	
	public static void testCompositionalRMP(int dim){
		int logDim = numOfBits(dim-1);
		BDD bdd = new BDD(10000,1000);
		
		//environment variables
		int objX[] = new int[logDim];
		int objY[] = new int[logDim];
		
		//agent 1
		int x1[] = new int[logDim];
		int y1[] = new int[logDim];
		
		//agent 2
		int x2[] = new int[logDim];
		int y2[] = new int[logDim];
		
		//prime variables
		int objXPrime[] = new int[logDim];
		int objYPrime[] = new int[logDim];
		
		int x1prime[] = new int[logDim];
		int y1prime[] = new int[logDim];
		
		int x2prime[] = new int[logDim];
		int y2prime[] = new int[logDim];
		
		//createBDDVars
		
		for(int i=0;i<logDim;i++){
			objX[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			objY[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			x1[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			y1[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			x2[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			y2[i]= bdd.createVar();
		}
			
		for(int i=0;i<logDim;i++){
			objXPrime[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			objYPrime[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			x1prime[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			y1prime[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			x2prime[i]= bdd.createVar();
		}
				
		for(int i=0;i<logDim;i++){
			y2prime[i]= bdd.createVar();
		}
		
		int[] inputVariables=concatenateArrays(objX, objY);
		int[] inputPrimeVars= concatenateArrays(objXPrime, objYPrime);
		
		int[] agent1vars=concatenateArrays(x1, y1);
		int[] agent2vars=concatenateArrays(x2, y2);
		int[] outputVariables=concatenateArrays(agent1vars, agent2vars);
		
		int[] agent1varsPrime=concatenateArrays(x1prime, y1prime);
		int[] agent2varsPrime=concatenateArrays(x2prime, y2prime);
		int[] outputPrimeVars=concatenateArrays(agent1varsPrime, agent2varsPrime);
		
		//define initial state
		int objXInit=stateToBDD(bdd, objX, dim-1);
		int objYInit=stateToBDD(bdd, objY, dim-1);
		int x1Init=stateToBDD(bdd, x1, 0);
		int y1Init=stateToBDD(bdd, y1, 0);
		int x2Init=stateToBDD(bdd, x2, dim-1);
		int y2Init=stateToBDD(bdd, y2, 0);
		
		int agent1init=bdd.ref(bdd.and(x1Init, y1Init));
//		System.out.println("agent 1 init");
//		bdd.printSet(agent1init);
		int agent2init=bdd.ref(bdd.and(x2Init, y2Init));
//		System.out.println("agent 2 init");
//		bdd.printSet(agent2init);
		int objInit = bdd.ref(bdd.and(objXInit, objYInit));
//		System.out.println("obstacle init");
//		bdd.printSet(objInit);
		bdd.deref(x1Init);
		bdd.deref(y1Init);
		bdd.deref(x2Init);
		bdd.deref(y2Init);
		bdd.deref(objYInit);
		bdd.deref(objXInit);
		
		//define transitions
		int objectTransitions = singleStepTransitionSystem(bdd, dim, objX, objY, objXPrime, objYPrime);
		int agent1Transitions = singleStepTransitionSystem(bdd, dim, x1, y1, x1prime, y1prime);
		int agent2Transitions = singleStepTransitionSystem(bdd, dim, x2, y2, x2prime, y2prime);
		
		//define ovjectives
		//no collision between robots and obstacle
		int safetyObjective1 = noCollisionObjective(bdd, x1, y1, objX, objY);
		int safetyObjective2 = noCollisionObjective(bdd, x2, y2, objX, objY);
		//no collision between robots
		int safetyObjective3 = noCollisionObjective(bdd, x1, y1, x2, y2);
		
		int safetyObjective=bdd.ref(bdd.and(safetyObjective1, safetyObjective2));
		safetyObjective=bdd.andTo(safetyObjective, safetyObjective3);
		
		//define games
		
		//define the central game
		long t0=System.currentTimeMillis();
		int init = bdd.ref(bdd.and(agent1init, objInit));
		init=bdd.andTo(init, agent2init);
//		System.out.println("system init");
//		bdd.printSet(init);
		int T=bdd.ref(bdd.and(objectTransitions, agent1Transitions));
		T=bdd.andTo(T, agent2Transitions);
		//central game 
		SymbolicGame sg = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, init, T);
		
		//int strat= testSafetyGame(bdd, sg, safetyObjective, "sg.dot");
		long t1=System.currentTimeMillis();
		System.out.println("Central game was solved in "+(t1-t0));

		//solving the central game with respect to safety objective
//		if(sg.isRealizable(safetyObjective)){
//			System.out.println("The game is realizable");
//			int winning = sg.greatestFixPoint(safetyObjective);
//			int strat = sg.synthesize(winning);
//			int reachable=sg.reachable(strat);
//			int ReachableStrat=sg.synthesize(reachable);
////			bdd.printCubes(strat);
////			bdd.printSet(strat);
////			System.out.println(getBDDPrintSetValue(bdd, strat).length);
//			sg.drawStrategy(strat);
////			sg.drawStrategy(ReachableStrat);
//			
//			
//			int post = sg.EpostImage(init, sg.getCube(sg.getVariables()), strat);
//			bdd.printSet(post);
//			bdd.printCubes(post);
//		}else{
//			System.out.println("the game is not realizable!\n");
//			int winning = sg.greatestFixPoint(safetyObjective);
//			System.out.println("The winning set is ");
//			bdd.printSet(winning);
//			System.out.println("\ncounter-strategy is\n");
//			int cs = sg.counterStrategy(winning, safetyObjective);
//			sg.drawStrategy(cs);
//			int reachable=sg.reachable(cs);
//			bdd.printSet(reachable);
////			int ReachableCS=sg.synthesize(reachable);
////			sg.drawStrategy(ReachableCS);
//		}
		
		//game 1: agent 1 versus environment
		t0=System.currentTimeMillis();
		int sys1init=bdd.ref(bdd.and(objInit, agent1init));
		int sys1trans=bdd.ref(bdd.and(objectTransitions, agent1Transitions));
		SymbolicGame sg1 = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, sys1init, sys1trans);
		
		int strat1 = testSafetyGame(bdd, sg1, safetyObjective1, "sg1.dot");
		//bdd.printSet(strat1);
		
		//game 2: agent 2 against environment
		int sys2init=bdd.ref(bdd.and(objInit, agent2init));
		int sys2trans=bdd.ref(bdd.and(objectTransitions, agent2Transitions));
		SymbolicGame sg2 = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, sys2init, sys2trans);
		
		int strat2 = testSafetyGame(bdd, sg2, safetyObjective2, "sg2.dot");
		//bdd.printSet(strat2);
		
		//game 3: composition of strat 1 and 2 with obejcetive safetyGameObjective3
		int sys3trans=bdd.ref(bdd.and(strat1, strat2));
		//bdd.printSet(sys3trans);
		init = bdd.ref(bdd.and(agent1init, objInit));
		init=bdd.andTo(init, agent2init);
//		System.out.println("system init");
//		bdd.printSet(init);
		SymbolicGame sg3 = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, init, sys3trans);
		
		int strat3 = testSafetyGame(bdd, sg3, safetyObjective3, "sg3.dot");
		t1=System.currentTimeMillis();
		System.out.println("compositional game was solved in "+(t1-t0));
		
//		if(strat3==strat){
//			System.out.println("both strategies are the same");
//		}else{
//			System.out.println("not sure");
//		}
//		
//		if(BDDWrapper.equivalent(bdd, strat3, strat)){
//			System.out.println("both strategies are really the same");
//		}
//		
//		if(strat2 == strat3){
//			System.out.println("s2 = s3");
//		}
	}
	
	public static int testSafetyGame(BDD bdd, SymbolicGame sg , int safetyObjective, String file){
		int result=-1;
		if(sg.isRealizable(safetyObjective)){
			System.out.println("The game is realizable");
			int winning = sg.greatestFixPoint(safetyObjective);
			int strat = sg.synthesize(winning);
			int reachable=sg.reachable(strat);
			int ReachableStrat=sg.synthesize(reachable);
			result=ReachableStrat;
	//		bdd.printCubes(strat);
	//		bdd.printSet(strat);
	//		System.out.println(getBDDPrintSetValue(bdd, strat).length);
	//		sg.drawStrategy(strat, "strate_"+file);
//			sg.drawStrategy(ReachableStrat, "reachable_"+file);
			
			
	//		int post = sg.EpostImage(sg.getInit(), sg.getCube(sg.getVariables()), strat);
	//		bdd.printSet(post);
	//		bdd.printCubes(post);
		}else{
			System.out.println("the game is not realizable!\n");
			int winning = sg.greatestFixPoint(safetyObjective);
//			System.out.println("The winning set is ");
//			bdd.printSet(winning);
			System.out.println("\ncounter-strategy is\n");
//			int cs = sg.counterStrategy(winning, safetyObjective);
////			sg.drawStrategy(cs, "cs_"+file);
//			int reachable=sg.reachable(cs);
//			bdd.printSet(reachable);
//			result=cs;
//			int ReachableCS=sg.synthesize(reachable);
			int ReachableCS = sg.counterStrategyReachable(winning, safetyObjective);
			sg.drawStrategy(ReachableCS, "reachableCS_"+file);
		}
		return result;
	}
	
	/**
	 * given two variables (x1,y1) and (x2,y2) returns the formula \not \forall_i (x1[i] <-> x2[i] & y1[i] <-> y2[i]), no collision
	 * @param bdd
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static int noCollisionObjective(BDD bdd, int[] x1, int[] y1, int[] x2, int[] y2){
		int complementSafetyObjective=bdd.getOne();
		for(int i=0;i<x1.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x1[i], x2[i]));
			int tmpY= bdd.ref(bdd.biimp(y1[i], y2[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpX);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		bdd.deref(complementSafetyObjective);
		return safety;
	}
	
	public static int singleStepTransitionSystem(BDD bdd, int boardDimension, int[] x, int[] y, int[] xPrime, int[] yPrime){
		int T=bdd.getOne();
		
		for(int i=0; i<boardDimension; i++){
			int currentX = stateToBDD(bdd, x, i);
			for(int j=0;j<boardDimension;j++){
				int currentY = stateToBDD(bdd, y, j);
				int current = bdd.ref(bdd.and(currentX, currentY));
				bdd.deref(currentX);
				bdd.deref(currentY);
				
				int nextXtemp;
				int nextYtemp;
				int next=0;
				for(int ip=0;ip<boardDimension;ip++){
					for(int jp=0;jp<boardDimension; jp++){
						if(i==ip){
							if(Math.abs(j-jp)==1){
								nextXtemp = stateToBDD(bdd, xPrime, ip);
								nextYtemp = stateToBDD(bdd, yPrime, jp);
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
							}
						}else if(Math.abs(i-ip)==1){
							if(j==jp){
								nextXtemp = stateToBDD(bdd, xPrime, ip);
								nextYtemp = stateToBDD(bdd, yPrime, jp);
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
							}
						}
					}
				}
				
				T=bdd.andTo(T, bdd.imp(current, next));
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
				bdd.deref(next);
			}
			
		}
		return T;
	}
	
	public static void testRMP(int dim){
		int logDim = numOfBits(dim-1);
		BDD bdd = new BDD(10000,1000);
		int x[] = new int[logDim];
		int y[] = new int[logDim];
		
		int objX[] = new int[logDim];
		int objY[] = new int[logDim];
		
		int xPrime[] = new int[logDim];
		int yPrime[] = new int[logDim];
		
		int objXPrime[] = new int[logDim];
		int objYPrime[] = new int[logDim];
		
		//createBDDVars
		for(int i=0;i<logDim;i++){
			x[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			y[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			objX[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			objY[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			xPrime[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			yPrime[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			objXPrime[i]= bdd.createVar();
		}
		
		for(int i=0;i<logDim;i++){
			objYPrime[i]= bdd.createVar();
		}
		
		int[] inputVariables=concatenateArrays(objX, objY);
		int[] outputVariables=concatenateArrays(x, y);
		int[] inputPrimeVars= concatenateArrays(objXPrime, objYPrime);
		int[] outputPrimeVars=concatenateArrays(xPrime, yPrime);
		
		
		//define initial state
		int xInit=stateToBDD(bdd, x, intToBinary(0));
		int yInit=stateToBDD(bdd, y, intToBinary(0));;
		int objXInit=stateToBDD(bdd, objX, intToBinary(dim-1));
		int objYInit=stateToBDD(bdd, objY, intToBinary(dim-1));
		int init = bdd.ref(bdd.and(xInit, yInit));
		init=bdd.andTo(init, objXInit);
		init=bdd.andTo(init, objYInit);
		bdd.deref(xInit);
		bdd.deref(yInit);
		bdd.deref(objYInit);
		bdd.deref(objXInit);
		
//		System.out.println("xinit is ");
//		bdd.printSet(xInit);
//		System.out.println("objxinit is " );
//		bdd.printSet(objXInit);
		
//		printBoard(dim, bdd, init);

		//define transitions
		int sysTransitions=1;
		int objTransitions=1;
		
		for(int i=0; i<dim; i++){
			int currentX = stateToBDD(bdd, x, intToBinary(i));
			int currentObjX = stateToBDD(bdd, objX, intToBinary(i));
			for(int j=0;j<dim;j++){
				int currentY = stateToBDD(bdd, y, intToBinary(j));
				int currentObjY = stateToBDD(bdd, objY, intToBinary(j));
				int current = bdd.ref(bdd.and(currentX, currentY));
				int currentObj = bdd.ref(bdd.and(currentObjX, currentObjY));
				bdd.deref(currentX);
				bdd.deref(currentY);
				bdd.deref(currentObjX);
				bdd.deref(currentObjY);
				
				int nextXtemp;
				int nextYtemp;
				int next=0;
				int nextObjXtemp;
				int nextObjYtemp;
				int nextObj=0;
				for(int ip=0;ip<dim;ip++){
					for(int jp=0;jp<dim; jp++){
						if(i==ip){
							if(Math.abs(j-jp)==1){
								nextXtemp = stateToBDD(bdd, xPrime, intToBinary(ip));
								nextYtemp = stateToBDD(bdd, yPrime, intToBinary(jp));
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
								
								nextObjXtemp = stateToBDD(bdd, objXPrime, intToBinary(ip));
								nextObjYtemp = stateToBDD(bdd, objYPrime, intToBinary(jp));
								int nextObjTemp=bdd.ref(bdd.and(nextObjXtemp, nextObjYtemp));
								nextObj=bdd.orTo(nextObj, nextObjTemp);
								bdd.deref(nextObjXtemp);
								bdd.deref(nextObjYtemp);
								bdd.deref(nextObjTemp);
							}
						}else if(Math.abs(i-ip)==1){
							if(j==jp){
								nextXtemp = stateToBDD(bdd, xPrime, intToBinary(ip));
								nextYtemp = stateToBDD(bdd, yPrime, intToBinary(jp));
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
								
								nextObjXtemp = stateToBDD(bdd, objXPrime, intToBinary(ip));
								nextObjYtemp = stateToBDD(bdd, objYPrime, intToBinary(jp));
								int nextObjTemp=bdd.ref(bdd.and(nextObjXtemp, nextObjYtemp));
								nextObj=bdd.orTo(nextObj, nextObjTemp);
								bdd.deref(nextObjXtemp);
								bdd.deref(nextObjYtemp);
								bdd.deref(nextObjTemp);
							}
						}
					}
				}
				
				sysTransitions=bdd.andTo(sysTransitions, bdd.imp(current, next));
				objTransitions=bdd.andTo(objTransitions, bdd.imp(currentObj, nextObj));
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(currentObj);
				bdd.deref(nextObj);
			}
			
		}
		
		int T =  bdd.ref(bdd.and(objTransitions, sysTransitions));
		bdd.deref(objTransitions);
		bdd.deref(sysTransitions);
		
		//define objectives
		
		//initialize the game object
		SymbolicGame sg = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, init, T);
		
		int cube;
		
		//test the SafetyGame object
//		System.out.println("printing the initial setting");
//		printBoard(dim, bdd, init);
//		cube = sg.getCube(sg.getVariables());
//		int postInit=sg. EpostImage(init, cube);
//		System.out.println("\n\nprinting the post image of init");
//		printBoard(dim, bdd, postInit);
		
		
		//location
//		int x1=stateToBDD(bdd, x, intToBinary(2));
//		int y1=stateToBDD(bdd, y, intToBinary(1));;
//		int objX1=stateToBDD(bdd, objX, intToBinary(dim-1));
//		int objY1=stateToBDD(bdd, objY, intToBinary(dim-1));
//		int loc = bdd.ref(bdd.and(x1, y1));
//		loc=bdd.andTo(loc, objX1);
//		loc=bdd.andTo(loc, objY1);
//		bdd.deref(x1);
//		bdd.deref(y1);
//		bdd.deref(objY1);
//		bdd.deref(objX1);
//		
//		System.out.println("\n\nPrinting the location");
//		printBoard(dim, bdd, loc);
//		cube=sg.getCube(sg.getPrimeVariables());
//		int preLoc=sg. EpreImage(loc, cube);
//		bdd.printSet(preLoc);
//		System.out.println("\n\nPrinting pre image of the location");
//		printBoard(dim, bdd, preLoc);
		
		int complementSafetyObjective=1;
		for(int i=0;i<x.length;i++){
			int tmpX= bdd.ref(bdd.biimp(x[i], objX[i]));
			int tmpY= bdd.ref(bdd.biimp(y[i], objY[i]));
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpX);
			complementSafetyObjective = bdd.andTo(complementSafetyObjective, tmpY);
			bdd.deref(tmpX);
			bdd.deref(tmpY);
		}
		
		int safety = bdd.ref(bdd.not(complementSafetyObjective));
		
		//y \not \in \set{1, ... , n}
		for(int i=1;i<dim;i++){
			int tmpY=stateToBDD(bdd, y, intToBinary(i));
			int notTmp=bdd.ref(bdd.not(tmpY));
			bdd.deref(tmpY);
			safety = bdd.andTo(safety, notTmp);
			bdd.deref(notTmp);
		}
		
		// x \not \in \set{1, ... , n}
		for(int i=1; i<dim;i++){
			int tmpX=stateToBDD(bdd, x, intToBinary(i));
			int notTmp=bdd.ref(bdd.not(tmpX));
			bdd.deref(tmpX);
			safety = bdd.andTo(safety, notTmp);
			bdd.deref(notTmp);
		}
		
		bdd.printSet(safety);
		
		if(sg.isRealizable(safety)){
			System.out.println("The game is realizable");
			int winning = sg.greatestFixPoint(safety);
			int strat = sg.synthesize(winning);
			int reachable=sg.reachable(strat);
			int ReachableStrat=sg.synthesize(reachable);
//			bdd.printCubes(strat);
//			bdd.printSet(strat);
//			System.out.println(getBDDPrintSetValue(bdd, strat).length);
			sg.drawStrategy(ReachableStrat, "reachableStrat.dot");
			
			
			int post = sg.EpostImage(init, sg.getCube(sg.getVariables()), strat);
			bdd.printSet(post);
			bdd.printCubes(post);
		}else{
			System.out.println("the game is not realizable!\n");
			int winning = sg.greatestFixPoint(safety);
			System.out.println("The winning set is ");
			bdd.printSet(winning);
			System.out.println("\ncounter-strategy is\n");
//			int cs = sg.counterStrategy(winning, safety);
//			sg.drawStrategy(cs,"cs.dot");
//			int reachable=sg.reachable(cs);
//			bdd.printSet(reachable);
//			int ReachableCS=sg.synthesize(reachable);
//			sg.drawStrategy(ReachableCS);
			int cs = sg.counterStrategyReachable(winning, safety);
			sg.drawStrategy(cs, "csReachable.dot");
		}
	}

	
	
	
	public static void test(){
		BDD bdd = new BDD(10000,1000);
		int x=bdd.createVar();
		int y=bdd.createVar();
		
		int objX=bdd.createVar();
		int objY=bdd.createVar();
		
		int xPrime=bdd.createVar();
		int yPrime=bdd.createVar();
		
		int objXPrime=bdd.createVar();
		int objYPrime=bdd.createVar();
		int[] inputVariables={objX,objY};
		int[] outputVariables={x,y};
		int[] inputPrimeVars={objXPrime, objYPrime};
		int[] outputPrimeVars={xPrime, yPrime};
		
		//define the initial state
		int xNotTemp=bdd.ref(bdd.not(x));
		int yNotTemp=bdd.ref(bdd.not(y));
		int init=bdd.ref(bdd.and(xNotTemp, yNotTemp));
		int objInit=bdd.ref(bdd.and(objX, objY));
		init=bdd.andTo(init, objInit);
		bdd.deref(xNotTemp);
		bdd.deref(yNotTemp);
		bdd.deref(objInit);
		
		//define the transition system
		int[][] trans=new int[2][2];
		int[][] objTrans=new int[2][2];
		for(int i=0;i<=1;i++){
			int currentX = bdd.ref(i==0 ? bdd.not(x) : x);
			int currentObjX = bdd.ref(i==0 ? bdd.not(objX) : objX);
			for(int j=0;j<=1;j++){
				int currentY = bdd.ref(j==0 ? bdd.not(y) : y);
				int currentObjY = bdd.ref(j==0 ? bdd.not(objY) : objY);
				int current = bdd.ref(bdd.and(currentX, currentY));
				int currentObj = bdd.ref(bdd.and(currentObjX, currentObjY));
				bdd.deref(currentX);
				bdd.deref(currentY);
				bdd.deref(currentObjX);
				bdd.deref(currentObjY);
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(current);
//				bdd.printSet(current);
				
				int nextXtemp;
				int nextYtemp;
				int next=0;
				int nextObjXtemp;
				int nextObjYtemp;
				int nextObj=0;
				for(int ip=0;ip<=1;ip++){
					for(int jp=0;jp<=1; jp++){
						if(i==ip){
							if(Math.abs(j-jp)==1){
								nextXtemp = bdd.ref(ip==0 ? bdd.not(xPrime) : xPrime);
								nextYtemp = bdd.ref(jp==0 ? bdd.not(yPrime) : yPrime);
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
								
								nextObjXtemp = bdd.ref(ip==0 ? bdd.not(objXPrime) : objXPrime);
								nextObjYtemp = bdd.ref(jp==0 ? bdd.not(objYPrime) : objYPrime);
								int nextObjTemp=bdd.ref(bdd.and(nextObjXtemp, nextObjYtemp));
								nextObj=bdd.orTo(nextObj, nextObjTemp);
								bdd.deref(nextObjXtemp);
								bdd.deref(nextObjYtemp);
								bdd.deref(nextObjTemp);
							}
						}else if(Math.abs(i-ip)==1){
							if(j==jp){
								nextXtemp = bdd.ref(ip==0 ? bdd.not(xPrime) : xPrime);
								nextYtemp = bdd.ref(jp==0 ? bdd.not(yPrime) : yPrime);
								int nextTemp=bdd.ref(bdd.and(nextXtemp, nextYtemp));
								next=bdd.orTo(next, nextTemp);
								bdd.deref(nextXtemp);
								bdd.deref(nextYtemp);
								bdd.deref(nextTemp);
								
								nextObjXtemp = bdd.ref(ip==0 ? bdd.not(objXPrime) : objXPrime);
								nextObjYtemp = bdd.ref(jp==0 ? bdd.not(objYPrime) : objYPrime);
								int nextObjTemp=bdd.ref(bdd.and(nextObjXtemp, nextObjYtemp));
								nextObj=bdd.orTo(nextObj, nextObjTemp);
								bdd.deref(nextObjXtemp);
								bdd.deref(nextObjYtemp);
								bdd.deref(nextObjTemp);
							}
						}
					}
				}
				
				trans[i][j]=bdd.ref(bdd.and(current, next));
				objTrans[i][j]=bdd.ref(bdd.and(currentObj, nextObj));
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
				bdd.deref(next);
				bdd.deref(currentObj);
				bdd.deref(nextObj);
			}
		}
		
		int transitions=0;
		int objTransitions=0;
		for(int i=0;i<=1;i++){
			for(int j=0;j<=1;j++){
				transitions=bdd.orTo(transitions,trans[i][j]);
				objTransitions=bdd.orTo(objTransitions, objTrans[i][j]);
				bdd.deref(trans[i][j]);
				bdd.deref(objTrans[i][j]);
			}
		}
		
		//adding a safety assumption formula G(objY!=0) 
		int tmp1=bdd.ref(bdd.and(objY, objYPrime));
		int safety=bdd.and(objY, tmp1);
		bdd.deref(tmp1);
		objTransitions=bdd.andTo(objTransitions, safety);
		
		
		
		System.out.println("Obj transitions ");
		bdd.printSet(objTransitions);
		System.out.println("sys transitions");
		bdd.printSet(transitions);
		
		int T=bdd.ref(bdd.and(transitions, objTransitions));
		bdd.deref(transitions);
		bdd.deref(objTransitions);
		
		
		SymbolicGame sg = new SymbolicGame(bdd, inputVariables, outputVariables, inputPrimeVars, outputPrimeVars, init, T);
		
		
		

		
		
		
		int cube;
		int pre;
		int apre;
		int cpre;
		
//		cube = sg.getCube(sg.outputPrimeVariables);
//		pre = sg.EpreImage(init,cube);
//		System.out.println("pre image of the R=(0,0) & obj=(1,1)");
//		bdd.printSet(pre);
//		
//		System.out.println("Apre image of the pre");
//		bdd.deref(cube);
//		cube=sg.getCube(inputPrimeVars);
//		apre=sg.ApreImageNoReplacement(pre, cube);
//		bdd.printSet(apre);
//		
//		System.out.println("Cpre");
//		int cpre=sg.controllablePredecessor(init);
//		bdd.printSet(cpre);
		
		System.out.println("\n\nset");
		int tmp=bdd.ref(bdd.and(bdd.not(x), y));
		int set = bdd.ref(bdd.not(tmp));
		bdd.deref(tmp);
		cube = sg.getCube(sg.getPrimeOutputVariables());
		pre = sg.EpreImage(set,cube);
		System.out.println("pre image of the R=!(0,1)");
		bdd.printSet(pre);
		
		System.out.println("Apre image of the pre");
		bdd.deref(cube);
		cube=sg.getCube(sg.getPrimeVariables());
		apre=sg.ApreImageNoReplacement(pre, cube);
		bdd.printSet(apre);
		
		System.out.println("Cpre");
		cpre=sg.controllablePredecessor(set);
		bdd.printSet(cpre);
		
		System.out.println("greatest fixed point");
		int gfp=sg.greatestFixPoint(set);
		
		System.out.println("checking realizability");
		System.out.println(sg.isRealizable(set));
		
		System.out.println("Synthesize strategy");
		int strat=sg.synthesize(gfp);
		bdd.printSet(strat);
		
		System.out.println("post image of init according to the strategy");
		int post=sg.EpostImage(init, sg.getCube(sg.getVariables()), strat);
		bdd.printSet(post);
		
		System.out.println("post image of the second set");
		post=sg.EpostImage(post, sg.getCube(sg.getVariables()), strat);
		bdd.printSet(post);
		
		
		sg.drawStrategy(strat, "strat.dot");
		
	}
	
	public static void printBoard(int dim , BDD bdd , int formula ){
		System.out.println("printing the board");
		String[] current=getBDDPrintSetValue(bdd, formula);
		int index=0;
		for(int i=0;i<current.length;i++){
			System.out.println("\npossible value "+index);
			index++;
			int[] locations = parseLocations(current[i]);
			printBoard(dim,locations[0], locations[1], locations[2], locations[3]);
		}
	}

	/**
	 * given a printSet output as input, parses the locations for the robot and the object
	 * replaces don't cares with zeros
	 *	TODO: generalize 
	 * @param input
	 * @return
	 */
	public static int[] parseLocations(String input){
//		System.out.println("Parsing location "+input);
		int[] x_y_objX_objY_xP_yP_objXP_objYP=new int[8];
		//the string can be divided into 8 parts
		int numOfBits= input.length()/8;
		for(int i=0;i<8;i++){
			String position = input.substring(i*numOfBits,(i+1)*numOfBits);
			position=position.replace('-', '0');
			x_y_objX_objY_xP_yP_objXP_objYP[i]=Integer.valueOf(position,2);
//			System.out.println("The integer result is "+x_y_objX_objY_xP_yP_objXP_objYP[i]);
		}

		return x_y_objX_objY_xP_yP_objXP_objYP;
	}
	
	
	public static String[] getBDDPrintSetValue(BDD bdd, int formula){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		bdd.printSet(formula);
		System.out.flush();
	    System.setOut(old);
	    String value=baos.toString();
	    String[] values=value.split("\n");
	    return values;
	}
	

	
	public static void printBoard(int dim, int Rx, int Ry, int objX, int objY ){
		System.out.println();
		for(int i=0; i<dim; i++){
			for(int j=0;j<dim; j++){
				if(i== Rx && j==Ry && i==objX && j==objY){
					System.out.print(" X ");
				}else if(i== Rx && j==Ry){
					System.out.print(" R ");
				}else if(i==objX && j==objY){
					System.out.print(" O ");
				}else{
					System.out.print(" - ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static int binaryToInt(int[] binary){
		String b="";
		for(int i=0; i<binary.length; i++){
			b+=binary[i];
		}
		return Integer.valueOf(b, 2);
	}
}
