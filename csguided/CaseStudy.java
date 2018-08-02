package csguided;

import java.util.Date;

import automaton.GameGraph;
import utils.UtilityMethods;
import jdd.bdd.BDD;

public class CaseStudy {
	
	int xDimension;
	int yDimension;
	BDD bdd;
	
	int borderLine;
	
	public Variable[] oY;
	public Variable[] oX;
	public Variable envOrient;
	public Variable[] R1x;
	public Variable[] R1y;
	public Variable[] stayPutCounter1;
	public Variable[] R2x;
	public Variable[] R2y;
	public Variable[] stayPutCounter2;
	public Variable[] oXPrime;
	public Variable[] oYPrime;
	public Variable envOrientPrime;
	public Variable[] R1xPrime;
	public Variable[] R1yPrime;
	public Variable[] stayPutCounter1Prime;
	
	public Variable[] R2xPrime;
	public Variable[] R2yPrime;
	public Variable[] stayPutCounter2Prime;
	
	public Variable right;
	public Variable[] envActionVars;
	public Variable[] R1ActionVars;
	public Variable[] R2ActionVars;
	
	
	public Variable[] inputVars;
	public Variable[] outputVars;
	public Variable[] inputVarsPrime;
	public Variable[] outputVarsPrime;
	
	public Variable[] vars;
	public Variable[] varsPrime;
	public Variable[] actionVars;
	
	public CaseStudy(BDD bdd, int xdim, int ydim){
		this.bdd=bdd;
		xDimension=xdim;
		yDimension=ydim;
		
		borderLine=3*(ydim-1)/4;
		
		int xbits=UtilityMethods.numOfBits(xdim-1);
		int ybits=UtilityMethods.numOfBits(ydim-1);
		
		oX=Variable.createVariables(bdd, xbits, "oX");
		oY=Variable.createVariables(bdd, ybits, "oY");
		envOrient = new Variable(bdd, "envOrientation");
		R1x=Variable.createVariables(bdd, xbits, "R1x");
		R1y=Variable.createVariables(bdd, ybits, "R1y");
		stayPutCounter1=Variable.createVariables(bdd, 2, "spCounter1");
		R2x=Variable.createVariables(bdd, xbits, "R2x");
		R2y=Variable.createVariables(bdd, ybits, "R2y");
		stayPutCounter2=Variable.createVariables(bdd, 2, "spCounter2");
		
		oXPrime = Variable.createPrimeVariables(bdd, oX);
		oYPrime = Variable.createPrimeVariables(bdd, oY);
		envOrientPrime = new Variable(bdd, "envOrientationPrime");
		R1xPrime= Variable.createPrimeVariables(bdd, R1x);
		R1yPrime=Variable.createPrimeVariables(bdd, R1y);
		stayPutCounter1Prime=Variable.createPrimeVariables(bdd, stayPutCounter1);
		R2xPrime= Variable.createPrimeVariables(bdd, R2x);
		R2yPrime=Variable.createPrimeVariables(bdd, R2y);
		stayPutCounter2Prime=Variable.createPrimeVariables(bdd, stayPutCounter2);
		
		Variable[] envOrientArr={envOrient};
		
		inputVars = UtilityMethods.concatenateArrays(oX,envOrientArr);
		
		outputVars = UtilityMethods.concatenateArrays(R1x, R1y);
		outputVars = UtilityMethods.concatenateArrays(outputVars, stayPutCounter1);
		outputVars = UtilityMethods.concatenateArrays(outputVars, R2x);
		outputVars = UtilityMethods.concatenateArrays(outputVars, R2y);
		outputVars = UtilityMethods.concatenateArrays(outputVars, stayPutCounter2);
		
		vars=UtilityMethods.concatenateArrays(inputVars, outputVars);
		
		
		Variable[] envOrientVarPrimeArr={envOrientPrime};
		inputVarsPrime = UtilityMethods.concatenateArrays(oXPrime,envOrientVarPrimeArr);
		
		outputVarsPrime = UtilityMethods.concatenateArrays(R1xPrime, R1yPrime);
		outputVarsPrime = UtilityMethods.concatenateArrays(outputVarsPrime, stayPutCounter1Prime);
		outputVarsPrime  = UtilityMethods.concatenateArrays(outputVarsPrime, R2xPrime);
		outputVarsPrime = UtilityMethods.concatenateArrays(outputVarsPrime, R2yPrime);
		outputVarsPrime = UtilityMethods.concatenateArrays(outputVarsPrime, stayPutCounter2Prime);
		
		varsPrime=UtilityMethods.concatenateArrays(inputVarsPrime, outputVarsPrime);
		
		envActionVars = new Variable[1];
		envActionVars[0] = new Variable(bdd, "actE");
		
		R1ActionVars = Variable.createVariables(bdd, 2, "actR1");
		//R1ActionVars[0] = new Variable(bdd, "actR1");
		
		R2ActionVars = Variable.createVariables(bdd, 2, "actR2");
		//R2ActionVars[0] = new Variable(bdd, "actR2");
		
		actionVars=UtilityMethods.concatenateArrays(envActionVars, R1ActionVars);
		actionVars=UtilityMethods.concatenateArrays(actionVars, R2ActionVars);
	}
	
	public Game[] generateGames(){
		System.out.println("generating games");
		Game[] games=new Game[2];
		
		//create init
		int init = createInit();
		System.out.println("init created");
		
		//create environment transitions
		int T_env = createEnvTransitions();
		System.out.println("T_env created");
//		UtilityMethods.debugBDDMethods(bdd, "T_env", T_env);
		
		//create system transitions
//		int T_sys1=createSysTrans(R1x, R1y, R1xPrime, R1yPrime, R1ActionVars[0].getBDDVar());
//		int T_sys1=createSysTrans2(R1x, R1y, R1xPrime, R1yPrime, stayPutCounter1, stayPutCounter1Prime, R1ActionVars[0].getBDDVar());
		int T_sys1=createSysTrans3(R1x, R1y, R1xPrime, R1yPrime, stayPutCounter1, stayPutCounter1Prime, R1ActionVars);
//		UtilityMethods.debugBDDMethods(bdd, "T_sys1", T_sys1);
		
//		int T_sys2=createSysTrans(R2x, R2y, R2xPrime, R2yPrime, R2ActionVars[0].getBDDVar());
//		int T_sys2=createSysTrans2(R2x, R2y, R2xPrime, R2yPrime, stayPutCounter2, stayPutCounter2Prime, R2ActionVars[0].getBDDVar());
		int T_sys2=createSysTrans3(R2x, R2y, R2xPrime, R2yPrime, stayPutCounter2, stayPutCounter2Prime, R2ActionVars);
		
		System.out.println("T_sys created");
		
		//create games
		games[0]=new Game(bdd, vars, varsPrime, init, T_env, T_sys1, envActionVars, R1ActionVars);
		games[1]=new Game(bdd, vars, varsPrime, init, T_env, T_sys2, envActionVars, R2ActionVars);
		
		return games;
	}
	
	public Game generateSimpleGame(){
		Variable[] x= Variable.createVariables(bdd, 2, "x");
		Variable[] y= Variable.createVariables(bdd, 2, "y");
		Variable[] xPrime=Variable.createPrimeVariables(bdd, x);
		Variable[] yPrime=Variable.createPrimeVariables(bdd, y);
		Variable[] vars=UtilityMethods.concatenateArrays(x, y);
		Variable[] varsPrime=UtilityMethods.concatenateArrays(xPrime, yPrime);
		Variable[] act=new Variable[1];
		act[0]=new Variable(bdd, "actS");
		
		int xInit=BDDWrapper.assign(bdd, 1, x);
		int yInit=BDDWrapper.assign(bdd, 2, y);
		int init=bdd.ref(bdd.and(xInit, yInit));
		
		int sameX = BDDWrapper.same(bdd, x, xPrime);
		int sameY = BDDWrapper.same(bdd, y, yPrime);
		int T_env = bdd.ref(bdd.and(sameX, sameY));
		
		int T_sys = createSysTrans(x, y, xPrime, yPrime, act[0].getBDDVar());
		
		Game g = new Game(bdd, vars, varsPrime, init, T_env, T_sys, act);
		return g;
		
		
	}
	
	public int createInit(){
		int oXInit=BDDWrapper.assign(bdd, xDimension-1, oX);
		int oYInit=BDDWrapper.assign(bdd, yDimension-3, oY);
		int envOrientation=bdd.ref(bdd.not(envOrient.getBDDVar()));
		int r1yInit=BDDWrapper.assign(bdd, borderLine, R1y);
		int r1xInit=BDDWrapper.assign(bdd, 1, R1x);
		int r1init=bdd.ref(bdd.and(r1yInit, r1xInit));
		int r2yInit=BDDWrapper.assign(bdd, borderLine, R2y);
		int spCounter1Init=BDDWrapper.assign(bdd, 0, stayPutCounter1);
		r1init=bdd.andTo(r1init, spCounter1Init);
		bdd.deref(spCounter1Init);
		int r2xInit=BDDWrapper.assign(bdd, 2, R2x);
		int r2init=bdd.ref(bdd.and(r2yInit, r2xInit));
		int spCounter2Init=BDDWrapper.assign(bdd, 0, stayPutCounter2);
		r2init=bdd.andTo(r2init, spCounter2Init);
		bdd.deref(spCounter2Init);
		
		int init=bdd.ref(bdd.and(oXInit,r1init));
		init = bdd.andTo(init, oYInit);
		init=bdd.andTo(init, r2init);
		init=bdd.andTo(init, envOrientation);
		bdd.deref(oXInit);
		bdd.deref(r1yInit);
		bdd.deref(r1xInit);
		bdd.deref(r1init);
		bdd.deref(r2yInit);
		bdd.deref(r2xInit);
		bdd.deref(r2init);
		return init;
	}
	
	public int createEnvTransitions(){
		int T_env=bdd.ref(bdd.getZero());
		int downOrient = bdd.ref(bdd.not(envOrient.getBDDVar()));
		int upOrient = bdd.ref(envOrient.getBDDVar());
		int downOrientPrime = bdd.ref(bdd.not(envOrientPrime.getBDDVar()));
		int upOrientPrime = bdd.ref(envOrientPrime.getBDDVar());
		for(int i=0; i<xDimension;i++){
			int current =  BDDWrapper.assign(bdd, i, oX);
			if(i-1>=0){
				int next = BDDWrapper.assign(bdd, i-1, oXPrime);
				next=bdd.andTo(next, upOrientPrime);
				int trans=bdd.ref(bdd.and(current, upOrient));
				trans=bdd.andTo(trans, next);
				T_env=bdd.orTo(T_env, trans);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i+1<xDimension){
				int next = BDDWrapper.assign(bdd, i+1, oXPrime);
				next=bdd.andTo(next, downOrientPrime);
				int trans=bdd.ref(bdd.and(current, downOrient));
				trans=bdd.andTo(trans, next);
				T_env=bdd.orTo(T_env, trans);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i==0){
				int next = BDDWrapper.assign(bdd, i+1, oXPrime);
				next=bdd.andTo(next, downOrientPrime);
				int trans=bdd.ref(bdd.and(current, upOrient));
				trans=bdd.andTo(trans, next);
				T_env=bdd.orTo(T_env, trans);
				bdd.deref(next);
				bdd.deref(trans);
			}
			if(i==xDimension-1){
				int next = BDDWrapper.assign(bdd, i-1, oXPrime);
				next=bdd.andTo(next, upOrientPrime);
				int trans=bdd.ref(bdd.and(current, downOrient));
				trans=bdd.andTo(trans, next);
				T_env=bdd.orTo(T_env, trans);
				bdd.deref(next);
				bdd.deref(trans);
			}
			bdd.deref(current);
		}
		
		
		
		int sameY = BDDWrapper.same(bdd, oY, oYPrime);
		int sameOutputValues=BDDWrapper.same(bdd, outputVars, outputVarsPrime);
		
//		System.out.println("here");
//		int specialAgent1Action=BDDWrapper.assign(bdd, 0, R1ActionVars);
//		int specialAgent2Action=BDDWrapper.assign(bdd, 0, R2ActionVars);
		
//		int agentsTrans=bdd.ref(bdd.and(sameOutputValues, specialAgent1Action));
		
//		UtilityMethods.debugBDDMethods(bdd, "sameY",sameY);
//		UtilityMethods.debugBDDMethods(bdd, "sameOutputValues", sameOutputValues);
		
		int agentsTrans=bdd.ref(bdd.and(sameOutputValues, sameY));
		
		System.out.println("here");
		
//		agentsTrans = bdd.andTo(agentsTrans, sameY);
//		agentsTrans=bdd.andTo(agentsTrans, specialAgent2Action);
		bdd.deref(sameOutputValues);
//		bdd.deref(specialAgent1Action);
//		bdd.deref(specialAgent2Action);
		T_env=bdd.andTo(T_env, agentsTrans);
		bdd.deref(agentsTrans);
		
		
		System.out.println("here");
		return T_env;
	}
	
	public int createSysTrans(Variable[] rx, Variable[] ry, Variable[] rxPrime, Variable[] ryPrime, int act){
		int T_sys=bdd.ref(bdd.getZero());
		//int border=3*(yDimension-1)/4;
		for(int x=0;x<xDimension;x++){
			for(int y=0;y<yDimension;y++){
				
				if(y==0) continue;//dead-ends
				int currentX=BDDWrapper.assign(bdd, x, rx);
				int currentY=BDDWrapper.assign(bdd, y, ry);
				int current=bdd.ref(bdd.and(currentX, currentY));
				bdd.deref(currentX);
				bdd.deref(currentY);
				if(y<=borderLine || y==yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y-1, ryPrime);
					
					int next=bdd.ref(bdd.and(nextX, nextY));
					bdd.deref(nextX);
					bdd.deref(nextY);
					
					int trans=bdd.ref(bdd.and(current, bdd.not(act)));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
				}
				if(y>=borderLine && y!=yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y+1, ryPrime);
					int next=bdd.ref(bdd.and(nextX, nextY));
					bdd.deref(nextX);
					bdd.deref(nextY);
					
					int trans=bdd.ref(bdd.and(current, act));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
				}
			}
		}
		
		int sameEnv=BDDWrapper.same(bdd, inputVars, inputVarsPrime);
		T_sys=bdd.andTo(T_sys, sameEnv);
		
		return T_sys;
	}
	
	public int createSysTrans2(Variable[] rx, Variable[] ry, Variable[] rxPrime, Variable[] ryPrime, Variable[] stayPutCounter, Variable[] stayPutCounterPrime, int act){
		int T_sys=bdd.ref(bdd.getZero());
		//int border=3*(yDimension-1)/4;
		//stayputcounter is restarted after any transition
		int nextStayPutCounter=BDDWrapper.assign(bdd, 0, stayPutCounterPrime);
		for(int x=0;x<xDimension;x++){
			int currentX=BDDWrapper.assign(bdd, x, rx);
			for(int y=0;y<yDimension;y++){
				
				if(y==0) continue;//dead-ends
				
				int currentY=BDDWrapper.assign(bdd, y, ry);
				int current=bdd.ref(bdd.and(currentX, currentY));
				
				bdd.deref(currentY);
				if(y<=borderLine || y==yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y-1, ryPrime);
					int next=bdd.ref(bdd.and(nextX, nextY));
					next=bdd.andTo(next,nextStayPutCounter);
					bdd.deref(nextX);
					bdd.deref(nextY);
					
					int trans=bdd.ref(bdd.and(current, bdd.not(act)));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
				}
				if(y>=borderLine && y!=yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y+1, ryPrime);
					int next=bdd.ref(bdd.and(nextX, nextY));
					next=bdd.andTo(next, nextStayPutCounter);
					
					
					
					int trans=bdd.ref(bdd.and(current, act));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
					
					if(y!=borderLine){
						int nextY1 = BDDWrapper.assign(bdd, y, ryPrime);
						//the robot can stay put for the maximun steps represented by stay put counter
						for(int i=0; i<Math.pow(2, stayPutCounter.length)-1;i++){
							int currentSPCounter=BDDWrapper.assign(bdd, i, stayPutCounter);
							int currentStayPut=bdd.ref(bdd.and(current, currentSPCounter));
							int nextSPCounter = BDDWrapper.assign(bdd, i+1, stayPutCounterPrime);
							int nextStayPut = bdd.ref(bdd.and(nextX, nextY1));
							nextStayPut=bdd.andTo(nextStayPut, nextSPCounter);
							bdd.deref(currentSPCounter);
							bdd.deref(nextSPCounter);
							
							int transStayPut=bdd.ref(bdd.and(currentStayPut, nextStayPut));
							transStayPut=bdd.andTo(transStayPut, bdd.not(act));
							T_sys=bdd.orTo(T_sys, transStayPut);
							bdd.deref(transStayPut);
							
						}
						bdd.deref(nextY1);
						int maxSPC = (int) Math.pow(2, stayPutCounter.length)-1;
						int maxSPCounter = BDDWrapper.assign(bdd, maxSPC , stayPutCounter);
						int maxSPtrans = bdd.ref(bdd.and(current, act));
						maxSPtrans=bdd.andTo(maxSPtrans, maxSPCounter);
						maxSPtrans=bdd.andTo(maxSPtrans, next);
						T_sys = bdd.orTo(T_sys, maxSPtrans);
						bdd.deref(maxSPtrans);
						bdd.deref(maxSPCounter);
						bdd.deref(nextY);
					}
					bdd.deref(nextX);
					bdd.deref(next);
				}
				
			}
			bdd.deref(currentX);
		}
		bdd.deref(nextStayPutCounter);
		
		int sameEnv=BDDWrapper.same(bdd, inputVars, inputVarsPrime);
		T_sys=bdd.andTo(T_sys, sameEnv);
		
		return T_sys;
	}
	
	//act: 00 : left
	//	   01 : right
	//     10 : stayput
	//	   11 : up-down
		   
	public int createSysTrans3(Variable[] rx, Variable[] ry, Variable[] rxPrime, Variable[] ryPrime, Variable[] stayPutCounter, Variable[] stayPutCounterPrime, Variable[] acts){
		int T_sys=bdd.ref(bdd.getZero());
		
		int actLeft = BDDWrapper.assign(bdd, 0, acts);
		int actRight = BDDWrapper.assign(bdd, 1, acts);
		int actStayPut = BDDWrapper.assign(bdd, 2, acts);
		int actUpDown = BDDWrapper.assign(bdd, 3, acts);
		
		//int border=3*(yDimension-1)/4;
		//stayputcounter is restarted after any transition
		int nextStayPutCounter=BDDWrapper.assign(bdd, 0, stayPutCounterPrime);
		for(int x=0;x<xDimension;x++){
			int currentX=BDDWrapper.assign(bdd, x, rx);
			for(int y=0;y<yDimension;y++){
				
				if(y==0) continue;//dead-ends
				
				int currentY=BDDWrapper.assign(bdd, y, ry);
				int current=bdd.ref(bdd.and(currentX, currentY));
				bdd.deref(currentY);
				
				int nextYcons = BDDWrapper.assign(bdd, y, ryPrime);
				
				//down
				if(x!=xDimension-1 && y%2==1 ){
					int nextX=BDDWrapper.assign(bdd, x+1, rxPrime);
					int nextUpDown=bdd.ref(bdd.and(nextX, nextYcons));
					bdd.deref(nextX);
					
					int transUpDown=bdd.ref(bdd.and(current,nextUpDown));
					transUpDown=bdd.andTo(transUpDown, actUpDown);
					T_sys=bdd.orTo(T_sys, transUpDown);
					bdd.deref(transUpDown);
					bdd.deref(nextUpDown);
				}
				//up
				if(x!=0 && y%2==0){
					int nextX=BDDWrapper.assign(bdd, x-1, rxPrime);
					int nextUpDown=bdd.ref(bdd.and(nextX, nextYcons));
					bdd.deref(nextX);
					
					int transUpDown=bdd.ref(bdd.and(current,nextUpDown));
					transUpDown=bdd.andTo(transUpDown, actUpDown);
					T_sys=bdd.orTo(T_sys, transUpDown);
					bdd.deref(transUpDown);
					bdd.deref(nextUpDown);
				}
				bdd.deref(nextYcons);
				
				
				
				if(y<=borderLine || y==yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y-1, ryPrime);
					int next=bdd.ref(bdd.and(nextX, nextY));
					next=bdd.andTo(next,nextStayPutCounter);
					bdd.deref(nextX);
					bdd.deref(nextY);
					
					int trans=bdd.ref(bdd.and(current, actLeft));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
				}
				if(y>=borderLine && y!=yDimension-1){
					int nextX=BDDWrapper.assign(bdd, x, rxPrime);
					int nextY=BDDWrapper.assign(bdd, y+1, ryPrime);
					int next=bdd.ref(bdd.and(nextX, nextY));
					next=bdd.andTo(next, nextStayPutCounter);
					
					
					
					int trans=bdd.ref(bdd.and(current, actRight));
					trans=bdd.andTo(trans, next);
					T_sys=bdd.orTo(T_sys, trans);
					bdd.deref(trans);
					
					if(y!=borderLine){
						int nextY1 = BDDWrapper.assign(bdd, y, ryPrime);
						//the robot can stay put for the maximun steps represented by stay put counter
						for(int i=0; i<Math.pow(2, stayPutCounter.length)-1;i++){
							int currentSPCounter=BDDWrapper.assign(bdd, i, stayPutCounter);
							int currentStayPut=bdd.ref(bdd.and(current, currentSPCounter));
							int nextSPCounter = BDDWrapper.assign(bdd, i+1, stayPutCounterPrime);
							int nextStayPut = bdd.ref(bdd.and(nextX, nextY1));
							nextStayPut=bdd.andTo(nextStayPut, nextSPCounter);
							bdd.deref(currentSPCounter);
							bdd.deref(nextSPCounter);
							
							int transStayPut=bdd.ref(bdd.and(currentStayPut, nextStayPut));
							transStayPut=bdd.andTo(transStayPut, actStayPut);
							T_sys=bdd.orTo(T_sys, transStayPut);
							bdd.deref(transStayPut);
							
						}
						bdd.deref(nextY1);
						int maxSPC = (int) Math.pow(2, stayPutCounter.length)-1;
						int maxSPCounter = BDDWrapper.assign(bdd, maxSPC , stayPutCounter);
						int maxSPtrans = bdd.ref(bdd.and(current, actStayPut));
						maxSPtrans=bdd.andTo(maxSPtrans, maxSPCounter);
						maxSPtrans=bdd.andTo(maxSPtrans, next);
						T_sys = bdd.orTo(T_sys, maxSPtrans);
						bdd.deref(maxSPtrans);
						bdd.deref(maxSPCounter);
						bdd.deref(nextY);
					}
					bdd.deref(nextX);
					bdd.deref(next);
				}
				
			}
			bdd.deref(currentX);
		}
		bdd.deref(nextStayPutCounter);
		
		int sameEnv=BDDWrapper.same(bdd, inputVars, inputVarsPrime);
		T_sys=bdd.andTo(T_sys, sameEnv);
		
		return T_sys;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		BDD bdd = new BDD(10000,1000);
//		CaseStudy cs=new CaseStudy(bdd, 4, 4);
//		cs.generateGames();
		testGames();
//		testSimpleGame();
	}
	
	public static void testGames(){
		int xDim=8;
		int yDim=16;
		BDD bdd = new BDD(10000,1000);
		CaseStudy cs=new CaseStudy(bdd, xDim, yDim);
		Game[] games=cs.generateGames();
		
//		games[0].printGame();
		
		int objColumn=yDim-3;
		
		int same1=BDDWrapper.assign(bdd, objColumn, cs.R1y);
		int sameRow1=BDDWrapper.same(bdd, cs.R1x, cs.oX);
		same1=bdd.andTo(same1, sameRow1);
		bdd.deref(sameRow1);
		int safetyObjective1=bdd.ref(bdd.not(same1));
		
		int same2=BDDWrapper.assign(bdd, objColumn, cs.R2y);
		int sameRow2=BDDWrapper.same(bdd, cs.R2x, cs.oX);
		same2=bdd.andTo(same2, sameRow2);
		bdd.deref(sameRow2);
		int safetyObjective2=bdd.ref(bdd.not(same2));
		
		
		int objective3 = BDDWrapper.complement(bdd, BDDWrapper.same(bdd, cs.R1x, cs.R2x));
//		int objective3= BDDWrapper.same(bdd, cs.R1y, cs.R2y);
		
//		int objective1=bdd.ref(bdd.getOne());
//		int objective2=bdd.ref(bdd.getOne());
		long t0,t1,t;
		
//		Game composedGame=games[0].compose(games[1]);
//		GameGraph gg = composedGame.removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		/*
		 * CENTRAL
		 */
//		Date date=new Date();
//		System.out.println("current time is "+date.toString());
//		t0=UtilityMethods.timeStamp();
//		Game composedGame=games[0].compose(games[1]);
//		int mainObjective=bdd.ref(bdd.and(safetyObjective1, safetyObjective2));
//		mainObjective=bdd.andTo(mainObjective, objective3);
//		GameSolver centralSolver=new GameSolver(composedGame, mainObjective, bdd);
//		GameSolution centralSolution = centralSolver.solve();
//		UtilityMethods.duration(t0, "central computation in");
//		GameGraph gg = centralSolution.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		/*
		 * CS guided
		 * 
		 * 
		 */
//		Date date=new Date();
//		System.out.println("current time is "+date.toString());
//		
//		int border=3*(yDim-1)/4;
//		
//		int leftBorder1=bdd.ref(bdd.getZero());
//		int leftBorder2=bdd.ref(bdd.getZero());
//		for(int i=0;i<border;i++){
//			int column1=BDDWrapper.assign(bdd, i, cs.R1y);
//			int column2=BDDWrapper.assign(bdd, i, cs.R2y);
//			leftBorder1=bdd.orTo(leftBorder1, column1);
//			leftBorder2=bdd.orTo(leftBorder2, column2);
//			bdd.deref(column1);
//			bdd.deref(column2);
//		}
//		
//		int border1=BDDWrapper.assign(bdd, border, cs.R1y);
//		int border2=BDDWrapper.assign(bdd, border, cs.R2y);
//		
//		int rightBorder1=bdd.ref(bdd.getZero());
//		int rightBorder2=bdd.ref(bdd.getZero());
//		for(int i=border+1;i<yDim;i++){
//			int column1=BDDWrapper.assign(bdd, i, cs.R1y);
//			int column2=BDDWrapper.assign(bdd, i, cs.R2y);
//			rightBorder1=bdd.orTo(leftBorder1, column1);
//			rightBorder2=bdd.orTo(leftBorder2, column2);
//			bdd.deref(column1);
//			bdd.deref(column2);
//		}
//		
//		int edge1=BDDWrapper.assign(bdd, yDim-1, cs.R1y);
//		int edge2=BDDWrapper.assign(bdd, yDim-1, cs.R2y);
//		
//		int rightBorder=bdd.ref(bdd.and(rightBorder1, rightBorder2));
//		
//		int[] abstractionPredicates={ games[0].init, rightBorder,  safetyObjective1,   safetyObjective2, objective3};
//		
//		t0=UtilityMethods.timeStamp();
//		int mainObjective=bdd.ref(bdd.and(safetyObjective1, safetyObjective2));
//		mainObjective=bdd.andTo(mainObjective, objective3);
//		Game composedGame=games[0].compose(games[1]);
//		CSGuidedControl csgc1=new CSGuidedControl(bdd, composedGame, abstractionPredicates, mainObjective);
//		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
//		solution1.print();
//		UtilityMethods.duration(t0, "cs guided in ");
//		
//		GameGraph gg = solution1.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		
		/*
		 * COMPOSITIONAL
		 */
//		Date date=new Date();
//		System.out.println("current time is "+date.toString());
//		long tAll=UtilityMethods.timeStamp();
//		System.out.println("Compositional");
//		GameSolver gs=new GameSolver(games[0], safetyObjective1, bdd);
//		t0=UtilityMethods.timeStamp();
//		GameSolution sol=gs.solve();
//		UtilityMethods.duration(t0, "for length "+yDim+" in ");
//		sol.print();
//		
//		
//		GameSolver gs2=new GameSolver(games[1], safetyObjective2, bdd);
//		t0=UtilityMethods.timeStamp();
//		GameSolution sol2=gs2.solve();
////		sol2.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("stratGame2.dot", 1, 0);
//		UtilityMethods.duration(t0, "for length "+yDim+" in ");
//		sol2.print();
//		
//		t0=UtilityMethods.timeStamp();
//		Game composedGame=sol.strategyOfTheWinner().compose(sol2.strategyOfTheWinner());
////		composedGame.printGame();
//		GameSolver compGS=new GameSolver(composedGame,objective3, bdd);
//		GameSolution sol3 = compGS.solve();
//		UtilityMethods.duration(t0, "for composition in ");
//		sol3.print();
//		GameGraph gg = sol3.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
//		System.out.println("# of states of the solution "+gg.numOfStates());
//		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
//		
//		UtilityMethods.duration(tAll, "whole process in ");
		
		/*
		 * 
		 * 
		 * CS GUIDED & compositional
		 */
		
		int border=3*(yDim-1)/4;
		
		int leftBorder1=bdd.ref(bdd.getZero());
		int leftBorder2=bdd.ref(bdd.getZero());
		for(int i=0;i<border;i++){
			int column1=BDDWrapper.assign(bdd, i, cs.R1y);
			int column2=BDDWrapper.assign(bdd, i, cs.R2y);
			leftBorder1=bdd.orTo(leftBorder1, column1);
			leftBorder2=bdd.orTo(leftBorder2, column2);
			bdd.deref(column1);
			bdd.deref(column2);
		}
		
		int border1=BDDWrapper.assign(bdd, border, cs.R1y);
		int border2=BDDWrapper.assign(bdd, border, cs.R2y);
		
		int rightBorder1=bdd.ref(bdd.getZero());
		int rightBorder2=bdd.ref(bdd.getZero());
		for(int i=border+1;i<yDim;i++){
			int column1=BDDWrapper.assign(bdd, i, cs.R1y);
			int column2=BDDWrapper.assign(bdd, i, cs.R2y);
			rightBorder1=bdd.orTo(leftBorder1, column1);
			rightBorder2=bdd.orTo(leftBorder2, column2);
			bdd.deref(column1);
			bdd.deref(column2);
		}
		
		int edge1=BDDWrapper.assign(bdd, yDim-1, cs.R1y);
		int edge2=BDDWrapper.assign(bdd, yDim-1, cs.R2y);
		
		
		int[] abstractionPredicates={ rightBorder1, rightBorder2, leftBorder1, leftBorder2, border1, border2, safetyObjective1, safetyObjective2, objective3};
		
//		int[] abstractionPredicates1={ leftBorder1, border1, rightBorder1,  safetyObjective1,  objective3, cs.R1x[0].getBDDVar(), cs.R1x[1].getBDDVar(), edge1};//, cs.oX[1].getBDDVar(), cs.oX[0].getBDDVar(), leftBorder1, border1, games[0].init, rightBorder1, , cs.R1y[0].getBDDVar(), cs.R1y[1].getBDDVar(), cs.R1y[2].getBDDVar(), cs.R1y[3].getBDDVar()
//		int[] abstractionPredicates2={ leftBorder2, border2, rightBorder2,   safetyObjective2,  objective3, cs.R2x[0].getBDDVar(), cs.R2x[1].getBDDVar(), edge2};//cs.R2x[0].getBDDVar(), cs.R2x[1].getBDDVar(). leftBorder2, border2, games[0].init, rightBorder2, , cs.R2y[0].getBDDVar(), cs.R2y[1].getBDDVar(), cs.R2y[2].getBDDVar(), cs.R2y[3].getBDDVar()

		int[] abstractionPredicates1={ games[0].init, rightBorder1,  safetyObjective1,  cs.R1x[0].getBDDVar(), cs.R1x[1].getBDDVar(), edge1};//, cs.oX[1].getBDDVar(), cs.oX[0].getBDDVar(), leftBorder1, border1, games[0].init, rightBorder1, , cs.R1y[0].getBDDVar(), cs.R1y[1].getBDDVar(), cs.R1y[2].getBDDVar(), cs.R1y[3].getBDDVar()
		int[] abstractionPredicates2={ games[0].init, rightBorder2,   safetyObjective2, cs.R2x[0].getBDDVar(), cs.R2x[1].getBDDVar(), edge2};//cs.R2x[0].getBDDVar(), cs.R2x[1].getBDDVar(). leftBorder2, border2, games[0].init, rightBorder2, , cs.R2y[0].getBDDVar(), cs.R2y[1].getBDDVar(), cs.R2y[2].getBDDVar(), cs.R2y[3].getBDDVar()

		Date date=new Date();
		System.out.println("\n\n cs guided control \n\n");
		
		
		
		System.out.println("current time is "+date.toString());
		t0=System.currentTimeMillis();
		CSGuidedControl csgc1=new CSGuidedControl(bdd, games[0], abstractionPredicates1, safetyObjective1);
		GameSolution solution1 = csgc1.counterStrategyGuidedControl();
		solution1.print();
//		solution1.drawWinnerStrategy("absStrat1.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 1 "+(t1-t0));
//		long t=t1-t0;
		
		t0=System.currentTimeMillis();
		CSGuidedControl csgc2=new CSGuidedControl(bdd, games[1], abstractionPredicates2, safetyObjective2);
		GameSolution solution2 = csgc2.counterStrategyGuidedControl();
		solution2.print();
//		solution2.drawWinnerStrategy("absStrat2.dot");
		t1=System.currentTimeMillis();
		System.out.println("time for solving game 2 "+(t1-t0));
//		t+=t1-t0;
		
//		bdd.gc();
		
		

		
		AbstractGame refinedAbs1=(AbstractGame) solution1.getGameStructure();
		AbstractGame restrictedAbsGame1 = refinedAbs1.restrict(solution1.strategyOfTheWinner().getEnvironmentTransitionRelation(), solution1.strategyOfTheWinner().getSystemTransitionRelation());
		
		
		AbstractGame refinedAbs2=(AbstractGame) solution2.getGameStructure();
		AbstractGame restrictedAbsGame2 = refinedAbs2.restrict(solution2.strategyOfTheWinner().getEnvironmentTransitionRelation(), solution2.strategyOfTheWinner().getSystemTransitionRelation());
		
		
		
		AbstractGame compAbsGame=restrictedAbsGame1.compose(restrictedAbsGame2);
		
		Game composedMainGame=games[0].compose(games[1]);
		
//		compAbsGame.printGame();
		
		CSGuidedControl csgc3=new CSGuidedControl(bdd, composedMainGame, compAbsGame, objective3);
		t0=System.currentTimeMillis();
		GameSolution solution3=csgc3.counterStrategyGuidedControlWithInitialAbstraction();
		UtilityMethods.duration(t0, "abstract composition solved in ");
		solution3.print();
		
		//int absObj=compAbsGame.computeAbstractObjective(objective2)
		
//		GameSolver gsComp = new GameSolver(compAbsGame, objective3, bdd);
//		GameSolution sol  = gsComp.solve();
//		
//		sol.print();
//		sol.strategyOfTheWinner().removeUnreachableStates().toGameGraph().draw("final.dot", 1,0);
//		
//		compAbsGame.removeUnreachableStates().toGameGraph().draw("compAbsgame.dot", 1, 0);
		
		GameGraph gg = solution3.strategyOfTheWinner().removeUnreachableStates().toGameGraph();
		System.out.println("# of states of the solution "+gg.numOfStates());
		System.out.println("# of transitions of the solution "+gg.numOfTransitions());
		
		
		
	}
	
	public static void testSimpleGame(){
		int xDim=4;
		int yDim=4;
		BDD bdd = new BDD(10000,1000);
		CaseStudy cs=new CaseStudy(bdd, xDim, yDim);
		Game g = cs.generateSimpleGame();
		
		long t0;
		int objective1=bdd.ref(bdd.getOne());
		System.out.println("central");
		GameSolver gs=new GameSolver(g, objective1, bdd);
		t0=UtilityMethods.timeStamp();
		GameSolution sol=gs.solve();
		UtilityMethods.duration(t0, "for length "+yDim+" in ");
		sol.print();
	}

}
