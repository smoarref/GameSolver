package symbolic;

import jdd.bdd.*;

public class SymbolicTransitionSystem {

	
	int[] variables; //variables of the transition system
	int[] primeVariables; //primed version of the variables
	int init; //bdd representing the initial states of the transition system
	int T; //bdd representing the transition relation
	BDD bdd; //bdd object managing the binary decision diagrams
	Permutation vTovPrime; //permutation from variables to their prime version
	Permutation vPrimeTov; //permutation from primed version of the variables to their original form
	
	int variablesCube;
	int primeVariablesCube;
	
	public SymbolicTransitionSystem(BDD argBdd, int[] argVariables, int[] argPrimedVars, int initial, int transitionRelation){
		bdd=argBdd;
		variables=argVariables;
		primeVariables=argPrimedVars;
		init=initial;
		T=transitionRelation;
		
		vTovPrime=bdd.createPermutation(variables, primeVariables);
		vPrimeTov=bdd.createPermutation(primeVariables, variables);
		
		//cube for the variables, initially -1, meaning that they are not currently initialized
		variablesCube=-1;
		primeVariablesCube=-1;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SymbolicTransitionSystem.test();
	}
	
	/**
	 * 
	 * @param currentSet: the set for which we compute the post image 
	 * @return the bdd for the post image
	 */
	public int postImage(int currentSet){
		//if variables cube is not already defined, define it
		if(variablesCube==-1){
			variablesCube=andAll(variables);
		}
		int temp = bdd.ref(bdd.and(currentSet, T));
		int tempImage=bdd.ref(bdd.exists(temp, variablesCube));
		bdd.deref(temp);
		
		int postImage=bdd.ref(bdd.replace(tempImage, vPrimeTov));
		bdd.deref(tempImage);
		
		return postImage;
	}
	
	public int preImage(int targetSet){
		if(primeVariablesCube==-1){
			primeVariablesCube=andAll(primeVariables);
		}
		int targetSetPrime=bdd.ref(bdd.replace(targetSet, vTovPrime));
		int temp = bdd.ref(bdd.and(targetSetPrime, T));
		int preImage=bdd.ref(bdd.exists(temp, primeVariablesCube));
		bdd.deref(temp);
		bdd.deref(targetSetPrime);
		
		return preImage;
	}
	
	/**
	 * Returns a set of states that all of their successors lie in the given set
	 * @param set: 
	 * @return the bdd representing the set of states with all successors in the set
	 */
	public int ApreImage(int set){
		int statesWithTransitionToSet = preImage(set);
		
//		System.out.println("pre image computed");
//		bdd.printSet(statesWithTransitionToSet);
		
		int temp = bdd.ref(bdd.not(set));
		int statesWithTransitionsToComplementSet= preImage(temp);
		
//		System.out.println("pre image of complement of the set computed");
//		bdd.printSet(statesWithTransitionsToComplementSet);
		
		bdd.deref(temp);
		int diff= BDDWrapper.diff(bdd, statesWithTransitionToSet, statesWithTransitionsToComplementSet);
		
//		System.out.println("the diff was computed");
//		bdd.printSet(diff);
		
		return diff;
	}
	
	public int leastFixPoint(int set){
		int Q=0;
		int Qprime=set;
		while(Q != Qprime){
			bdd.deref(Q);
			Q=Qprime;
			int pre = preImage(Qprime);
			Qprime = bdd.orTo(Qprime, pre);
			bdd.deref(pre);
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public int greatestFixPoint(int set){
		int Q=1;
		int Qprime=set;
		while(Q != Qprime){
			bdd.deref(Q);
			Q=Qprime;
			int pre = preImage(Qprime);
			Qprime = bdd.andTo(Qprime, pre);
			bdd.deref(pre);
		}
		bdd.deref(Qprime);
		return Q;
	}
	
	public int reachableSet(int set){
		int reachableTmp=0;
		int reachable=set;
		while(reachableTmp != reachable){
			bdd.deref(reachableTmp);
			reachableTmp = reachable;
			int post = postImage(reachable);
			reachable=bdd.orTo(reachable, post);
			bdd.deref(post);
		}
		return reachable;
	}
	
	private int andAll(int[] vars){
		int result=1;
		for(int i=0;i<vars.length;i++){
			result=bdd.andTo(result, vars[i]);
		}
		return result;
	}

	public void cleanup(){
		bdd.cleanup();
	}
	
	public static void test(){
		BDD bdd = new BDD(10000,1000);
		int x=bdd.createVar();
		int y=bdd.createVar();
		int xPrime=bdd.createVar();
		int yPrime=bdd.createVar();
		int[] vars={x,y};
		int[] primeVars={xPrime, yPrime};
		
		//define the initial state
		int xNotTemp=bdd.ref(bdd.not(x));
		int yNotTemp=bdd.ref(bdd.not(y));
		int init=bdd.ref(bdd.and(xNotTemp, yNotTemp));
		bdd.deref(xNotTemp);
		bdd.deref(yNotTemp);
		
		//define the transition system
		int[][] trans=new int[2][2];
		for(int i=0;i<=1;i++){
			int currentX = bdd.ref(i==0 ? bdd.not(x) : x);
			for(int j=0;j<=1;j++){
				int currentY = bdd.ref(j==0 ? bdd.not(y) : y);
				int current = bdd.ref(bdd.and(currentX, currentY));
				bdd.deref(currentX);
				bdd.deref(currentY);
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(current);
//				bdd.printSet(current);
				
				int nextXtemp;
				int nextYtemp;
				int next=0;
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
							}
						}
					}
				}
				
				trans[i][j]=bdd.ref(bdd.and(current, next));
				
//				System.out.println("position is ("+i+" , "+j+")");
//				bdd.printCubes(next);
//				bdd.printSet(next);
				
				bdd.deref(current);
				bdd.deref(next);
			}
		}
		
		int transitions=0;
		for(int i=0;i<=1;i++){
			for(int j=0;j<=1;j++){
				transitions=bdd.orTo(transitions,trans[i][j]);
			}
		}
		
//		//adding a safety formula G(x=0) 
//		int safety = bdd.ref(bdd.not(x));
//		int tmp1=bdd.and(bdd.not(x), bdd.not(xPrime));
//		safety=bdd.andTo(safety, tmp1);
//		bdd.deref(tmp1);
//		transitions=bdd.andTo(transitions, safety);
		
		SymbolicTransitionSystem sts= new SymbolicTransitionSystem(bdd, vars, primeVars, init, transitions);
		
		
		int set=bdd.ref(bdd.and(bdd.not(x), bdd.not(y)));
		int pre = sts.preImage(set);
		System.out.println("pre image of the (0,0)");
		bdd.printSet(pre);
		
		System.out.println("Apre image of the (0,0)");
		pre=sts.ApreImage(set);
		bdd.printSet(pre);
		
		set=bdd.ref(bdd.and(x, bdd.not(y)));
		System.out.println("post image of the (1,0)");
		int post = sts.postImage(set);
		bdd.printSet(post);
		
		set=bdd.ref(bdd.not(bdd.and(x, bdd.not(y))));
		System.out.println("Apre image of {(0,0), (0,1), (1,1)}");
		int Apre=sts.ApreImage(set);
		bdd.printSet(Apre);
		
		
		System.out.println("reachable set from init");
		int reachable=sts.reachableSet(init);
		bdd.printSet(reachable);
		
		System.out.println("\nlfp");
		set=bdd.ref(bdd.and(x, bdd.not(y)));
		
		int lfp=sts.leastFixPoint(set);
		bdd.printSet(lfp);
		
		System.out.println("\ngfp");
		int tmp=bdd.ref(bdd.and(x, y));
		set=bdd.orTo(set,tmp);
		bdd.deref(tmp);
		int gfp=sts.greatestFixPoint(set);
		bdd.printSet(gfp);
		
		int check = bdd.ref(bdd.and(init, gfp));
		if(check == 0){
			System.out.println("\ninit does not belong to the gfp");
		}
		
		sts.cleanup();
	}
}
