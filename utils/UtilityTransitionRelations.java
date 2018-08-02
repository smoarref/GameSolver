package utils;

import java.util.ArrayList;

import specification.Agent2D;
import specification.AgentType;
import specification.GridCell2D;
import jdd.bdd.BDD;
import game.BDDWrapper;
import game.Variable;

public class UtilityTransitionRelations {
	
	public static int upTransitions(BDD bdd, int xDim, Variable[] xVars, Variable[] yVars, int upAct){
		return upTransitions(bdd, 0,  xDim,xVars,  yVars, upAct);
	}
	
	
	public static int upTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars, int upAct){
		
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=lowerBound+1; i<=upperBound;i++){
//			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			//actions=00 -- up
			//x'=x-1 && y'=y
			int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
			int trans = bdd.ref(bdd.and(xState,xPrime));
//			trans=bdd.andTo(trans, yTrans);
			trans=bdd.andTo(trans, upAct);
				
			bdd.deref(xPrime);
				
//			TList.add(trans);
			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(xState);	
				
		}
		T=bdd.andTo(T, yTrans);	
		bdd.deref(yTrans);
		return T;
	}
	
	public static int upTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars){
		
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=lowerBound+1; i<=upperBound;i++){
//			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			//actions=00 -- up
			//x'=x-1 && y'=y
			int xPrime = BDDWrapper.assign(bdd, i-1, xVarsPrime);
			int trans = bdd.ref(bdd.and(xState,xPrime));		
			bdd.deref(xPrime);
			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(xState);	
				
		}
		T=bdd.andTo(T, yTrans);	
		bdd.deref(yTrans);
		return T;
	}
	
	public static int downTransitions(BDD bdd, int xDim, Variable[] xVars, Variable[] yVars, int downAct){
		return downTransitions(bdd, 0, xDim, xVars, yVars, downAct);
	}
	
	public static int downTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars, int downAct){
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=lowerBound; i<=upperBound-1;i++){
//			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
			int trans = bdd.ref(bdd.and(xState,xPrime));
//			trans=bdd.andTo(trans, yTrans);
			trans=bdd.andTo(trans, downAct);
			bdd.deref(xPrime);
				
	//		TList.add(trans);
			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(xState);
		}
		T=bdd.andTo(T, yTrans);
		bdd.deref(yTrans);
		return T;
	}
	
	public static int downTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars){
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int yTrans = BDDWrapper.same(bdd, yVars, yVarsPrime);
		for(int i=lowerBound; i<=upperBound-1;i++){
//			System.out.println("xDim "+i);
			int xState = BDDWrapper.assign(bdd, i, xVars);
			int xPrime = BDDWrapper.assign(bdd, i+1, xVarsPrime);
			int trans = bdd.ref(bdd.and(xState,xPrime));
			bdd.deref(xPrime);

			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(xState);
		}
		T=bdd.andTo(T, yTrans);
		bdd.deref(yTrans);
		return T;
	}
	
	//left and right
	public static int leftTransitions(BDD bdd, int yDim, Variable[] xVars, Variable[] yVars, int leftAct){
		return leftTransitions(bdd, 0,  yDim,xVars,  yVars, leftAct);
	}
	
	public static int leftTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars, int leftAct){
		
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int i=lowerBound+1; i<=upperBound;i++){
//			System.out.println("yDim "+i);
			int yState = BDDWrapper.assign(bdd, i, yVars);
			//actions=00 -- up
			//y'=y-1 && x'=x
			int yPrime = BDDWrapper.assign(bdd, i-1, yVarsPrime);
			int trans = bdd.ref(bdd.and(yState,yPrime));
//			trans=bdd.andTo(trans, yTrans);
			trans=bdd.andTo(trans, leftAct);
				
			bdd.deref(yPrime);
				
//			TList.add(trans);
			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(yState);	
				
		}
		T=bdd.andTo(T, xTrans);	
		bdd.deref(xTrans);
		return T;
	}
	
	public static int leftTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars){
		
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int i=lowerBound+1; i<=upperBound;i++){
//			System.out.println("yDim "+i);
			int yState = BDDWrapper.assign(bdd, i, yVars);
			//actions=00 -- up
			//y'=y-1 && x'=x
			int yPrime = BDDWrapper.assign(bdd, i-1, yVarsPrime);
			int trans = bdd.ref(bdd.and(yState,yPrime));
				
			bdd.deref(yPrime);

			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(yState);	
				
		}
		T=bdd.andTo(T, xTrans);	
		bdd.deref(xTrans);
		return T;
	}
	
	public static int rightTransitions(BDD bdd, int yDim, Variable[] xVars, Variable[] yVars, int rightAct){
		return rightTransitions(bdd, 0, yDim, xVars, yVars, rightAct);
	}
	
	public static int rightTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars, int rightAct){
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int i=lowerBound; i<=upperBound-1;i++){
//			System.out.println("yDim "+i);
			int yState = BDDWrapper.assign(bdd, i, yVars);
			int yPrime = BDDWrapper.assign(bdd, i+1, yVarsPrime);
			int trans = bdd.ref(bdd.and(yState,yPrime));
//			trans=bdd.andTo(trans, yTrans);
			trans=bdd.andTo(trans, rightAct);
			bdd.deref(yPrime);
				
	//		TList.add(trans);
			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(yState);
		}
		T=bdd.andTo(T, xTrans);
		bdd.deref(xTrans);
		return T;
	}
	
	public static int rightTransitions(BDD bdd, int lowerBound, int upperBound, Variable[] xVars, Variable[] yVars){
		Variable[] xVarsPrime=Variable.getPrimedCopy(xVars);
		Variable[] yVarsPrime=Variable.getPrimedCopy(yVars);
		
		int T=bdd.ref(bdd.getZero());
		
		int xTrans = BDDWrapper.same(bdd, xVars, xVarsPrime);
		for(int i=lowerBound; i<=upperBound-1;i++){
//			System.out.println("yDim "+i);
			int yState = BDDWrapper.assign(bdd, i, yVars);
			int yPrime = BDDWrapper.assign(bdd, i+1, yVarsPrime);
			int trans = bdd.ref(bdd.and(yState,yPrime));
			bdd.deref(yPrime);

			T=bdd.orTo(T, trans);
			bdd.deref(trans);
			bdd.deref(yState);
		}
		T=bdd.andTo(T, xTrans);
		bdd.deref(xTrans);
		return T;
	}
	
	public static int halt(BDD bdd, Variable[] vars, int haltAct){
		Variable[] varsPrime=Variable.getPrimedCopy(vars);
		int same = BDDWrapper.same(bdd, vars, varsPrime);
		same=bdd.andTo(same, haltAct);
		return same;
	}

	
	public static int transition (BDD bdd, Variable[] vars, int value, Variable[] primedVars, int valuePrime){
		int result = BDDWrapper.assign(bdd,value, vars);
		int nextValue = BDDWrapper.assign(bdd, valuePrime, primedVars);
		result=BDDWrapper.andTo(bdd, result, nextValue);
		BDDWrapper.free(bdd,nextValue);
		return result;
	}
	
	public static int constantAddition(BDD bdd, Variable[] vars, int lowerBound, int upperBound, int progressValue){
		int T = bdd.ref(bdd.getZero());
		Variable[] primedVars = Variable.getPrimedCopy(vars);
		for(int i=lowerBound; i<=upperBound; i++){
			int nextValue = i+progressValue;
			if(nextValue >= lowerBound && nextValue <= upperBound){
				int trans = transition(bdd, vars, i, primedVars, nextValue);
				T=bdd.orTo(T, trans);
				bdd.deref(trans);
			}
		}
		return T;
	}
	
	public static int position2DTransition(BDD bdd, Variable[] changing, Variable[] notChanging, int dim, int progressValue){
		return position2DTransition(bdd, changing, notChanging, 0, dim, progressValue);
	}
	
	public static int position2DTransition(BDD bdd, Variable[] changing, Variable[] notChanging, int lowerBound, int upperBound, int progressValue){
		Variable[] primedNotChanging = Variable.getPrimedCopy(notChanging);
		int same = BDDWrapper.same(bdd, notChanging, primedNotChanging);
		int T = constantAddition(bdd, changing, lowerBound, upperBound, progressValue);
		T=bdd.andTo(T, same);
		return T;
	}
	
	
	//Binary arithmetic 
	
	//TODO: assumes tha X[0] is the most significant bit. Make sure it is consistent everywhere
	/**
	 * generates X' = X + 1. The method assumes an unsigned representation for variable X, and does not
	 * prevent overflow.
	 * 
	 * if X = X_n .. X_1 X_0, then we have 
	 * X'_0 = X_0 XOR 1
	 * X'_1 = X_1 XOR X_0
	 * X'_2 = X_2 XOR (X_1 AND X_0)
	 * ..
	 * X'_n = X_n XOR (X_{N-1} AND ... AND X_0)
	 *   
	 * @param bdd
	 * @param X
	 * @return
	 */
	public static int addOne(BDD bdd, Variable[] X){
//		int result= bdd.ref(bdd.getOne());
//		Variable[] Xprime = Variable.getPrimedCopy(X);
//		
//		int carry = bdd.ref(bdd.getOne());
//		
//		for(int i = X.length-1; i>=0;i--){
//			//X_i XOR carry = X_i XOR (X_{i-1} AND .. AND X_0)
//			int sum_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), carry));
//			int sum_bit_i_assign = bdd.ref(bdd.biimp(Xprime[i].getBDDVar(), sum_bit_i));
//			bdd.deref(sum_bit_i);
//			result=bdd.andTo(result, sum_bit_i_assign);
//			bdd.deref(sum_bit_i_assign);
//			carry = bdd.andTo(carry, X[i].getBDDVar());
//		}
//		
//		return result;
		
		return addOne(bdd, X, Variable.getPrimedCopy(X));
	}
	
	//TODO: assumes that S and X have the same length. Generalize
	public static int addOne(BDD bdd, Variable[] X, Variable[] S){
		if(S.length != X.length ){
			//throw new Exception("The length of sum bits is less than X bits");
			System.out.println("inconsitency between length of X and S in addOne method!");
			return -1;
		}
		
		int result= bdd.ref(bdd.getOne());
		
		int carry = bdd.ref(bdd.getOne());
		
		for(int i = X.length-1; i>=0;i--){
			//X_i XOR carry = X_i XOR (X_{i-1} AND .. AND X_0)
			int sum_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), carry));
			int sum_bit_i_assign = bdd.ref(bdd.biimp(S[i].getBDDVar(), sum_bit_i));
			bdd.deref(sum_bit_i);
			result=bdd.andTo(result, sum_bit_i_assign);
			bdd.deref(sum_bit_i_assign);
			carry = bdd.andTo(carry, X[i].getBDDVar());
		}
		
		
		
		return result;
	}
	
	public static int add(BDD bdd, Variable[] X, int n, Variable[] S){
		if(S.length != X.length ){
			//throw new Exception("The length of sum bits is less than X bits");
			System.out.println("inconsitency between length of X and S in addOne method!");
			return -1;
		}
		
		String operand = Integer.toBinaryString(n);
		int diff = X.length-operand.length();
		if(operand.length() < X.length){
			for(int i=0;i<diff;i++){
				operand = "0"+operand;
			}
		}

		
		int result= bdd.ref(bdd.getOne());
		
		int carry = bdd.ref(bdd.getZero());
		
		for(int i = X.length-1; i>=0;i--){
			char operandChar = operand.charAt(i);
			int operandBit = operandChar == '0'? bdd.ref(bdd.getZero()) : bdd.ref(bdd.getOne());
			int temp = bdd.ref(bdd.xor(X[i].getBDDVar(), carry));
			int sum_bit_i = bdd.ref(bdd.xor(temp, operandBit));
			bdd.deref(temp);
			int sum_bit_i_assign = bdd.ref(bdd.biimp(S[i].getBDDVar(), sum_bit_i));
			bdd.deref(sum_bit_i);
			result=bdd.andTo(result, sum_bit_i_assign);
			bdd.deref(sum_bit_i_assign);
			
			//computing carry c_{i+1} = (c_i & X_i) | (c_i & operandBit) | (X_i & operandBit) 
			int tmp1 = BDDWrapper.and(bdd, X[i].getBDDVar(), operandBit);
			int tmp2 = BDDWrapper.and(bdd, X[i].getBDDVar(), carry);
			int tmp3 = BDDWrapper.and(bdd, operandBit, carry);
			bdd.deref(carry);
			carry = BDDWrapper.or(bdd, tmp1, tmp2);
			carry = BDDWrapper.orTo(bdd, carry, tmp3);
			BDDWrapper.free(bdd, tmp1);
			BDDWrapper.free(bdd, tmp2);
			BDDWrapper.free(bdd, tmp3);
		}
		
		
		
		return result;
	}
	
	public static int subtract(BDD bdd, Variable[] X, int n, Variable[] D){
		if(D.length != X.length ){
			//throw new Exception("The length of sum bits is less than X bits");
			System.out.println("inconsitency between length of X and S in addOne method!");
			return -1;
		}
		
		String operand = Integer.toBinaryString(n);
		int diff = X.length-operand.length();
		if(operand.length() < X.length){
			for(int i=0;i<diff;i++){
				operand = "0"+operand;
			}
		}

		
		int result= bdd.ref(bdd.getOne());
		
		int borrow = bdd.ref(bdd.getZero());
		
		for(int i = X.length-1; i>=0;i--){
			char operandChar = operand.charAt(i);
			int operandBit = operandChar == '0'? bdd.ref(bdd.getZero()) : bdd.ref(bdd.getOne());
			
			//D = (!borrow & (D xor OperandBit)) or (borrow & !(D xor OperandBit))
			int DxorOperand = bdd.ref(bdd.xor(X[i].getBDDVar(), operandBit));
			int notDxorOperand = bdd.ref(bdd.not(DxorOperand));
			int part1 = bdd.ref(bdd.and(bdd.not(borrow), DxorOperand));
			int part2 = bdd.ref(bdd.and(borrow, notDxorOperand));
			int subtract_bit_i = bdd.ref(bdd.or(part1, part2));
			BDDWrapper.free(bdd, DxorOperand);
			BDDWrapper.free(bdd, notDxorOperand);
			BDDWrapper.free(bdd, part1);
			BDDWrapper.free(bdd, part2);
			
			int subtract_bit_i_assign = bdd.ref(bdd.biimp(D[i].getBDDVar(), subtract_bit_i));
			bdd.deref(subtract_bit_i);
			result=bdd.andTo(result, subtract_bit_i_assign);
			bdd.deref(subtract_bit_i_assign);
			
			//computing borrow b_{i+1} = (b_i & !X_i) | (b_i & operandBit) | (!X_i & operandBit) 
			int tmp1 = BDDWrapper.and(bdd, bdd.not(X[i].getBDDVar()), operandBit);
			int tmp2 = BDDWrapper.and(bdd, bdd.not(X[i].getBDDVar()), borrow);
			int tmp3 = BDDWrapper.and(bdd, operandBit, borrow);
			bdd.deref(borrow);
			borrow = BDDWrapper.or(bdd, tmp1, tmp2);
			borrow = BDDWrapper.orTo(bdd, borrow, tmp3);
			BDDWrapper.free(bdd, tmp1);
			BDDWrapper.free(bdd, tmp2);
			BDDWrapper.free(bdd, tmp3);
		}
		
		
		
		return result;
	}
	
	//TODO: assumes tha X[0] is the most significant bit. Make sure it is consistent everywhere
	/**
	 * generates X' = X - 1. The method assumes an unsigned representation for variable X, and does not
	 * prevent overflow.
	 * 
	 * if X = X_n .. X_1 X_0, then we have 
	 * X'_0 = X_0 XOR 1
	 * X'_1 = X_1 XOR NOT X_0
	 * X'_2 = X_2 XOR (NOT X_1 AND NOT X_0)
	 * ..
	 * X'_n = X_n XOR (NOT X_{N-1} AND ... AND NOT X_0)
	 *   
	 * @param bdd
	 * @param X
	 * @return
	 */
	public static int subtractOne(BDD bdd, Variable[] X){
//		int result= bdd.ref(bdd.getOne());
//		Variable[] Xprime = Variable.getPrimedCopy(X);
//		
//		int borrow = bdd.ref(bdd.getOne());
//		
//		for(int i = X.length-1; i>=0;i--){
//			//X_i XOR carry = X_i XOR (X_{i-1} AND .. AND X_0)
//			int sub_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), borrow));
//			int sub_bit_i_assign = bdd.ref(bdd.biimp(Xprime[i].getBDDVar(), sub_bit_i));
//			bdd.deref(sub_bit_i);
//			result=bdd.andTo(result, sub_bit_i_assign);
//			bdd.deref(sub_bit_i_assign);
//			borrow = bdd.andTo(borrow, bdd.not(X[i].getBDDVar()));
//		}
//		
//		return result;
		
		return subtractOne(bdd, X, Variable.getPrimedCopy(X));
	}
	
	//Assumes that X and D have the same length. Generalize
	public static int subtractOne(BDD bdd, Variable[] X, Variable[] D){
		if(D.length != X.length ){
			//throw new Exception("The length of sum bits is less than X bits");
			System.out.println("inconsitency between length of X and D in subtractOne method!");
			return -1;
		}
		
		int result= bdd.ref(bdd.getOne());
		int borrow = bdd.ref(bdd.getOne());
		
		for(int i = X.length-1; i>=0;i--){
			//X_i XOR carry = X_i XOR (X_{i-1} AND .. AND X_0)
			int sub_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), borrow));
			int sub_bit_i_assign = bdd.ref(bdd.biimp(D[i].getBDDVar(), sub_bit_i));
			bdd.deref(sub_bit_i);
			result=bdd.andTo(result, sub_bit_i_assign);
			bdd.deref(sub_bit_i_assign);
			borrow = bdd.andTo(borrow, bdd.not(X[i].getBDDVar()));
		}
		
		return result;
	}
	
	/**
	 *TODO: extend to the case where number of bits can be different
	 * Adds two unsigned binary variables X and Y returns S = X + Y. 
	 * Assumes that X and Y has the same length
	 * @param bdd
	 * @param X
	 * @param Y
	 * @return
	 */
	public static int addUnsigned(BDD bdd, Variable[] X, Variable[] Y, Variable[] S){
		if(X.length != Y.length){
			System.out.println("The length of two variables are not the same!");
			return -1;
		}
		
		if(S.length != X.length){
			System.out.println("The length of the sum is not compatible with the length of two variables!");
			return -1;
		}
		
		int result= bdd.ref(bdd.getOne());
		
		int carry =bdd.ref(bdd.getZero());
		
		for(int i = X.length-1; i>=0;i--){
			int sum_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), Y[i].getBDDVar()));
			int sum_bit_i_xor_carry = bdd.ref(bdd.xor(sum_bit_i, carry));
			
			int sum_bit_i_assign = bdd.ref(bdd.biimp(S[i].getBDDVar(), sum_bit_i_xor_carry));
			bdd.deref(sum_bit_i_xor_carry);
			result=bdd.andTo(result, sum_bit_i_assign);
			bdd.deref(sum_bit_i_assign);
			//carry_i = (X_i AND Y_i) OR (C_{i-1} AND (X_i XOR Y_i))
			int tmp = bdd.ref(bdd.and(carry, sum_bit_i));
			bdd.deref(carry);
			bdd.deref(sum_bit_i);
			carry = bdd.ref(bdd.and(X[i].getBDDVar(), Y[i].getBDDVar()));
			carry=bdd.orTo(carry, tmp);
			bdd.deref(tmp);
		}
		
		return result;
	}
	
	/**
	 *TODO: extend to the case where number of bits can be different
	 * Subtracts two unsigned binary variables X and Y returns D = X - Y. 
	 * Assumes that X and Y has the same length
	 * @param bdd
	 * @param X
	 * @param Y
	 * @return
	 */
	public static int subtractUnsigned(BDD bdd, Variable[] X, Variable[] Y, Variable[] D){
		if(X.length != Y.length){
			System.out.println("The length of two variables are not the same!");
			return -1;
		}
		
		if(D.length != X.length){
			System.out.println("The length of the sum is not compatible with the length of two variables!");
			return -1;
		}
		
		int result= bdd.ref(bdd.getOne());
		
		int borrow =bdd.ref(bdd.getZero());
		
		for(int i = X.length-1; i>=0;i--){
			int sub_bit_i = bdd.ref(bdd.xor(X[i].getBDDVar(), Y[i].getBDDVar()));
			int sub_bit_i_xor_carry = bdd.ref(bdd.xor(sub_bit_i, borrow));
			
			int sum_bit_i_assign = bdd.ref(bdd.biimp(D[i].getBDDVar(), sub_bit_i_xor_carry));
			bdd.deref(sub_bit_i_xor_carry);
			result=bdd.andTo(result, sum_bit_i_assign);
			bdd.deref(sum_bit_i_assign);
			//borrow_i = (NOT(X_i) AND Y_i) OR (B_{i-1} AND NOT (X_i XOR Y_i))
			int tmp = bdd.ref(bdd.and(borrow, bdd.not(sub_bit_i)));
			bdd.deref(borrow);
			bdd.deref(sub_bit_i);
			borrow = bdd.ref(bdd.and(bdd.not(X[i].getBDDVar()), Y[i].getBDDVar()));
			borrow=bdd.orTo(borrow, tmp);
			bdd.deref(tmp);
		}
		
		return result;
	}
	
	/**
	 *TODO: free the memory for transitions
	 * creates a simple transition relation with four directions. 
	 * Assumes that actions are up, down, left and right, respectively. 
	 * @param bdd
	 * @param xdim
	 * @param ydim
	 * @param xVars
	 * @param yVars
	 * @param actions
	 * @return
	 */
	public static int createSimple2DTransitionRelation(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		int up = BDDWrapper.assign(bdd, 0, actionVars);
		int down = BDDWrapper.assign(bdd, 1, actionVars);
		int left = BDDWrapper.assign(bdd, 2, actionVars);
		int right = BDDWrapper.assign(bdd, 3, actionVars);
		int T = UtilityTransitionRelations.upTransitions(bdd, xdim, xVars, yVars, up);
		T=bdd.orTo(T, UtilityTransitionRelations.downTransitions(bdd, xdim, xVars, yVars, down));
		T=bdd.orTo(T, UtilityTransitionRelations.leftTransitions(bdd, ydim, xVars, yVars, left));
		T=bdd.orTo(T, UtilityTransitionRelations.rightTransitions(bdd, ydim, xVars, yVars, right));
		BDDWrapper.free(bdd, up);
		BDDWrapper.free(bdd, down);
		BDDWrapper.free(bdd, left);
		BDDWrapper.free(bdd, right);
		return T;
	}
	
	public static int createSimple2DTransitionRelationWithStayPut(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		int up = BDDWrapper.assign(bdd, 0, actionVars);
		int down = BDDWrapper.assign(bdd, 1, actionVars);
		int left = BDDWrapper.assign(bdd, 2, actionVars);
		int right = BDDWrapper.assign(bdd, 3, actionVars);
		int stayPut = BDDWrapper.assign(bdd, 4, actionVars);
		
		int T = UtilityTransitionRelations.upTransitions(bdd, xdim, xVars, yVars, up);
		T=bdd.orTo(T, UtilityTransitionRelations.downTransitions(bdd, xdim, xVars, yVars, down));
		T=bdd.orTo(T, UtilityTransitionRelations.leftTransitions(bdd, ydim, xVars, yVars, left));
		T=bdd.orTo(T, UtilityTransitionRelations.rightTransitions(bdd, ydim, xVars, yVars, right));
		T=bdd.orTo(T, UtilityTransitionRelations.halt(bdd, Variable.unionVariables(xVars, yVars), stayPut));
		BDDWrapper.free(bdd, up);
		BDDWrapper.free(bdd, down);
		BDDWrapper.free(bdd, left);
		BDDWrapper.free(bdd, right);
		BDDWrapper.free(bdd, stayPut);
		return T;
	}
	
	public static int createSimple2DTransitionRelationWithStayPut(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars){

		int T = UtilityTransitionRelations.upTransitions(bdd,0, xdim, xVars, yVars);
		T=bdd.orTo(T, UtilityTransitionRelations.downTransitions(bdd, 0 , xdim, xVars, yVars));
		T=bdd.orTo(T, UtilityTransitionRelations.leftTransitions(bdd, 0, ydim, xVars, yVars));
		T=bdd.orTo(T, UtilityTransitionRelations.rightTransitions(bdd, 0, ydim, xVars, yVars));
		T=bdd.orTo(T, UtilityTransitionRelations.halt(bdd, Variable.unionVariables(xVars, yVars), bdd.getOne()));
		return T;
	}
	
	public static void main(String[] args){
		BDD bdd = new BDD(10000, 1000);
//		int numOfVars = 3;
//		Variable[] X = Variable.createVariables(bdd, numOfVars, "X");
//		Variable[] a = Variable.createVariables(bdd, numOfVars, "a");
//		Variable[] XPrime = Variable.createPrimeVariables(bdd, X);
//		Variable[] vars = Variable.unionVariables(X, XPrime);
//		Variable[] varsAndparams = Variable.unionVariables(vars, a);
		
//		int assign = BDDWrapper.assign(bdd, 2, X);
//		bdd.printSet(assign);
//		
//		int addOne = UtilityTransitionRelations.addOne(bdd, X);
//		bdd.printSet(addOne);
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, addOne, vars));
//		System.out.println();
//		
//		int subOne = UtilityTransitionRelations.subtractOne(bdd, X);
//		bdd.printSet(subOne);
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, subOne, vars));
//		
//		System.out.println("adding two vars");
//		int addA = UtilityTransitionRelations.addUnsigned(bdd, X, a, XPrime);
//		bdd.printSet(addA);
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, addA, varsAndparams));
//		
//		System.out.println("subtracting two vars");
//		int subA = UtilityTransitionRelations.subtractUnsigned(bdd, X, a, XPrime);
//		bdd.printSet(subA);
//		System.out.println(BDDWrapper.BDDtoFormula(bdd, subA, varsAndparams));
		
//		int add2 = UtilityTransitionRelations.add(bdd, a, 3, X);
//		bdd.printSet(add2);
		
//		int sub2 = UtilityTransitionRelations.subtract(bdd, a, 3, X);
//		bdd.printSet(sub2);
		
		int dim = 3;
//		int numOfBits = UtilityMethods.numOfBits(dim);
//		
//		
////		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfBits, "Rx");
////		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopies_interleaving(bdd, numOfBits, "Ry");
//		
//		Variable[] xVars = new Variable[numOfBits];
//		Variable[] yVars = new Variable[numOfBits];
//		for(int i=0; i<numOfBits; i++){
//			xVars[i] = Variable.createVariableAndPrimedVariable(bdd, "Rx"+i);
//			yVars[i] = Variable.createVariableAndPrimedVariable(bdd, "Ry"+i);
//		}
//		
//		
//		Variable[] actionVars = Variable.createVariables(bdd, 2, "Ract");
//		
//		Agent2D agent = createSimpleRobot(bdd, dim, "R", AgentType.Controllable, new GridCell2D(0, 0), xVars, yVars, actionVars);
//		
//		agent.print();
		
		ArrayList<GridCell2D> uncontrolledAgentsInitCells= new ArrayList<GridCell2D>();
		uncontrolledAgentsInitCells.add(new GridCell2D(dim-1, dim-1));
		
		ArrayList<GridCell2D> controlledAgentsInitCells = new ArrayList<GridCell2D>();
		controlledAgentsInitCells.add(new GridCell2D(0, 0));
		
		ArrayList<Agent2D> agents = createSimpleRobots(bdd, dim, "R", uncontrolledAgentsInitCells, controlledAgentsInitCells);
		
		System.out.println("printing agents");
		for(Agent2D agent : agents ){
			agent.print();
		}
		
		
	}
	
	//creating simple agents
	/**
	 * Creates a simple robot that can move around in a grid-world with four actions: left, right, up and down
	 * @param bdd
	 * @param dim
	 * @param name
	 * @param type
	 * @param init
	 * @return
	 */
	public static Agent2D createSimpleRobot(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
		Variable[] actionVars = Variable.createVariables(bdd, 2, name+"act");
		return createSimpleRobot(bdd, dim, name, type, initCell, xVars, yVars, actionVars);
	}
	
	public static Agent2D createSimpleRobotWithStayPut(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
		Variable[] actionVars = Variable.createVariables(bdd, 3, name+"act");
		return createSimpleRobotWithStayPut(bdd, dim, name, type, initCell, xVars, yVars, actionVars);
	}
	
	/**
	 * Create simple controlled and uncontrolled robots with interleaving variable ordering
	 * @param bdd
	 * @param dim
	 * @param namePrefix
	 * @param uncontrolledAgentsInitCells
	 * @param controlledAgentsInitCells
	 * @return
	 */
	public static ArrayList<Agent2D> createSimpleRobots(BDD bdd, int dim, String namePrefix, 
			ArrayList<GridCell2D> uncontrolledAgentsInitCells, ArrayList<GridCell2D> controlledAgentsInitCells){
		
		ArrayList<Agent2D> agents = new ArrayList<Agent2D>();
		
		String uncontrolledAgentsNamePrefix = "Uncontrolled"+namePrefix;
		String controlledAgentsNamePrefix = "Controlled"+namePrefix;
		
		
		int numOfUncontrolledAgents = uncontrolledAgentsInitCells.size();
		int numOfControlledAgents = controlledAgentsInitCells.size();
		int numOfVars = UtilityMethods.numOfBits(dim);
		
		ArrayList<Variable[]> uncontrolledAgentsXvars = new ArrayList<Variable[]>();
		ArrayList<Variable[]> uncontrolledAgentsYvars = new ArrayList<Variable[]>();
		ArrayList<Variable[]> uncontrolledAgentsActionvars = new ArrayList<Variable[]>();
		
		for(int i=0;i<numOfUncontrolledAgents; i++){
			Variable[] uncontrolledAgent_i_xVars = new Variable[numOfVars];
			uncontrolledAgentsXvars.add(uncontrolledAgent_i_xVars);
			Variable[] uncontrolledAgent_i_yVars = new Variable[numOfVars];
			uncontrolledAgentsYvars.add(uncontrolledAgent_i_yVars);
//			Variable[] uncontrolledAgent_i_actionVars = new Variable[2];
//			uncontrolledAgentsActionvars.add(uncontrolledAgent_i_actionVars);
		}
		
		ArrayList<Variable[]> controlledAgentsXvars = new ArrayList<Variable[]>();
		ArrayList<Variable[]> controlledAgentsYvars = new ArrayList<Variable[]>();
		ArrayList<Variable[]> controlledAgentsActionvars = new ArrayList<Variable[]>();
		
		for(int i=0;i<numOfControlledAgents; i++){
			Variable[] controlledAgent_i_xVars = new Variable[numOfVars];
			controlledAgentsXvars.add(controlledAgent_i_xVars);
			Variable[] controlledAgent_i_yVars = new Variable[numOfVars];
			controlledAgentsYvars.add(controlledAgent_i_yVars);
//			Variable[] controlledAgent_i_actionVars = new Variable[2];
//			controlledAgentsActionvars.add(controlledAgent_i_actionVars);
		}
		
		//create variables with special order
		
		//create action vars
		for(int i=0;i<numOfUncontrolledAgents; i++){
			uncontrolledAgentsActionvars.add(Variable.createVariables(bdd, 1, uncontrolledAgentsNamePrefix+"_"+i+"_act"));
		}
		
		for(int i=0;i<numOfControlledAgents; i++){
			controlledAgentsActionvars.add(Variable.createVariables(bdd, 3, controlledAgentsNamePrefix+"_"+i+"_act"));
		}
		
		// the order of variables for agents --> each agents x1[i], x1Prime[i], y1[i], y1Prime[i], x2[i], x2Prime[i], y2[i], y2Prime[i]
		for(int i=0; i<numOfVars; i++){
			for(int j = 0; j<numOfUncontrolledAgents; j++){
				Variable[] uncontrolledAgent_j_xVars = uncontrolledAgentsXvars.get(j);
				uncontrolledAgent_j_xVars[i] = Variable.createVariableAndPrimedVariable(bdd, uncontrolledAgentsNamePrefix+j+"_x"+i);
				Variable[] uncontrolledAgent_j_yVars = uncontrolledAgentsYvars.get(j);
				uncontrolledAgent_j_yVars[i] = Variable.createVariableAndPrimedVariable(bdd, uncontrolledAgentsNamePrefix+j+"_y"+i);
			}
			
			for(int j=0;j<numOfControlledAgents; j++){
				Variable[] controlledAgent_j_xVars = controlledAgentsXvars.get(j);
				controlledAgent_j_xVars[i] = Variable.createVariableAndPrimedVariable(bdd, controlledAgentsNamePrefix+j+"_x"+i);
				Variable[] controlledAgent_j_yVars = controlledAgentsYvars.get(j);
				controlledAgent_j_yVars[i] = Variable.createVariableAndPrimedVariable(bdd, controlledAgentsNamePrefix+j+"_y"+i);
			}
		}
		
	
		
		//create uncontrolled agents
		for(int i=0; i< numOfUncontrolledAgents; i++){
//			Agent2D agent = createSimpleRobot(bdd, dim, "Uncontrolled"+namePrefix+i, AgentType.Uncontrollable, 
//					uncontrolledAgentsInitCells.get(i), uncontrolledAgentsXvars.get(i), uncontrolledAgentsYvars.get(i), 
//					uncontrolledAgentsActionvars.get(i));
			
			Agent2D agent = createSimpleRobotMovingUpAndDown(bdd, dim, "Uncontrolled"+namePrefix+i, AgentType.Uncontrollable, 
					uncontrolledAgentsInitCells.get(i), uncontrolledAgentsXvars.get(i), uncontrolledAgentsYvars.get(i), 
					uncontrolledAgentsActionvars.get(i));
			agents.add(agent);
		}
		
		//create controlled agents
		for(int i=0; i<numOfControlledAgents; i++){
			Agent2D agent = createSimpleRobotWithStayPut(bdd, dim, "controlled"+namePrefix+i, AgentType.Controllable, 
					controlledAgentsInitCells.get(i), controlledAgentsXvars.get(i), controlledAgentsYvars.get(i), 
					controlledAgentsActionvars.get(i));
			agents.add(agent);
		}
		
		//return agents
		return agents;
	}
	
	public static Agent2D createSimpleRobotWithStayPut(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		
		int trans = createSimple2DTransitionRelationWithStayPut(bdd, dim, dim, xVars, yVars, actionVars);
		
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static Agent2D createSimpleRobot(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		
		//define transition relation
//		int trans = bdd.ref(bdd.getZero());
//		
//		int upAct = BDDWrapper.assign(bdd, 0, actionVars);
//		int upTransitions = UtilityTransitionRelations.upTransitions(bdd, dim, xVars, yVars, upAct);
//		trans=bdd.orTo(trans, upTransitions);
//		
//		int downAct = BDDWrapper.assign(bdd, 1, actionVars);
//		int downTransitions = UtilityTransitionRelations.downTransitions(bdd, dim, xVars, yVars, downAct);
//		trans=bdd.orTo(trans, downTransitions);
//		
//		int leftAct = BDDWrapper.assign(bdd, 2, actionVars);
//		int leftTransitions = UtilityTransitionRelations.leftTransitions(bdd, dim, xVars, yVars, leftAct);
//		trans=bdd.orTo(trans, leftTransitions);
//		
//		int rightAct = BDDWrapper.assign(bdd, 3, actionVars);
//		int rightTransitions = UtilityTransitionRelations.rightTransitions(bdd, dim, xVars, yVars, rightAct);
//		trans=bdd.orTo(trans, rightTransitions);
		
		int trans = createSimple2DTransitionRelation(bdd, dim, dim, xVars, yVars, actionVars);
		
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static int createUpAndDownTransitionRelation(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars, int upAct, int downAct){
		int T = UtilityTransitionRelations.upTransitions(bdd, xdim, xVars, yVars, upAct);
		T=bdd.orTo(T, UtilityTransitionRelations.downTransitions(bdd, xdim, xVars, yVars, downAct));
		return T;
	}
	
	public static int createLeftAndRightTransitionRelation(BDD bdd, int xdim, int ydim, Variable[] xVars, Variable[] yVars, int leftAct, int rightAct){
		int T = UtilityTransitionRelations.leftTransitions(bdd, xdim, xVars, yVars, leftAct);
		T=bdd.orTo(T, UtilityTransitionRelations.rightTransitions(bdd, xdim, xVars, yVars, rightAct));
		return T;
	}
	
	public static Agent2D createSimpleRobotMovingUpAndDown(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
		Variable[] actionVars = Variable.createVariables(bdd, 1, name+"Act");
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		return createSimpleRobotMovingUpAndDown(bdd, dim, name, type, initCell, xVars, yVars, actionVars);
//		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
//		int upAct = BDDWrapper.assign(bdd, 0, actionVars);
//		int downAct = BDDWrapper.assign(bdd, 1, actionVars);
//		int trans = createUpAndDownTransitionRelation(bdd, dim, dim, xVars, yVars, upAct, downAct);
//		BDDWrapper.free(bdd, upAct);
//		BDDWrapper.free(bdd, downAct);
//		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
//		return robot;
	}
	
	public static Agent2D createSimpleRobotMovingUpAndDown(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		int upAct = BDDWrapper.assign(bdd, 0, actionVars);
		int downAct = BDDWrapper.assign(bdd, 1, actionVars);
		int trans = createUpAndDownTransitionRelation(bdd, dim, dim, xVars, yVars, upAct, downAct);
		BDDWrapper.free(bdd, upAct);
		BDDWrapper.free(bdd, downAct);
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static Agent2D createSimpleRobotMovingLeftAndRight(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
		Variable[] actionVars = Variable.createVariables(bdd, 1, name+"Act");
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		int leftAct = BDDWrapper.assign(bdd, 0, actionVars);
		int rightAct = BDDWrapper.assign(bdd, 1, actionVars);
		int trans = createLeftAndRightTransitionRelation(bdd, dim, dim, xVars, yVars, leftAct, rightAct);
		BDDWrapper.free(bdd, leftAct);
		BDDWrapper.free(bdd, rightAct);
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static Agent2D createSimpleRobotMovingLeftAndRight(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		int leftAct = BDDWrapper.assign(bdd, 0, actionVars);
		int rightAct = BDDWrapper.assign(bdd, 1, actionVars);
		int trans = createLeftAndRightTransitionRelation(bdd, dim, dim, xVars, yVars, leftAct, rightAct);
		BDDWrapper.free(bdd, leftAct);
		BDDWrapper.free(bdd, rightAct);
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static Agent2D createSimpleRobotMovingLeftAndRightWithStop(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell){
		int numOfVars = UtilityMethods.numOfBits(dim);
		Variable[] xVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"X");
		Variable[] yVars = Variable.createVariablesAndTheirPrimedCopy(bdd, numOfVars, name+"Y");
		Variable[] actionVars = Variable.createVariables(bdd, 2, name+"Act");
		return createSimpleRobotMovingLeftAndRightWithStop(bdd, dim, name, type, initCell, xVars, yVars, actionVars);
//		Variable[] vars = Variable.unionVariables(xVars, yVars);
//		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
//		int leftAct = BDDWrapper.assign(bdd, 0, actionVars);
//		int rightAct = BDDWrapper.assign(bdd, 1, actionVars);
//		int trans = createLeftAndRightTransitionRelation(bdd, dim, dim, xVars, yVars, leftAct, rightAct);
//		int haltAct = BDDWrapper.assign(bdd, 2, actionVars);
//		int haltTrans = halt(bdd, Variable.unionVariables(xVars, yVars), haltAct);
//		trans = BDDWrapper.orTo(bdd, trans, haltTrans);
//		BDDWrapper.free(bdd, leftAct);
//		BDDWrapper.free(bdd, rightAct);
//		BDDWrapper.free(bdd, haltAct);
//		BDDWrapper.free(bdd, haltTrans);
//		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
//		return robot;
	}
	
	public static Agent2D createSimpleRobotMovingLeftAndRightWithStop(BDD bdd, int dim, String name, AgentType type, GridCell2D initCell, Variable[] xVars, Variable[] yVars, Variable[] actionVars){
		Variable[] vars = Variable.unionVariables(xVars, yVars);
		int init = UtilityFormulas.assignCell(bdd, xVars, yVars, initCell);
		int leftAct = BDDWrapper.assign(bdd, 0, actionVars);
		int rightAct = BDDWrapper.assign(bdd, 1, actionVars);
		int trans = createLeftAndRightTransitionRelation(bdd, dim, dim, xVars, yVars, leftAct, rightAct);
		int haltAct = BDDWrapper.assign(bdd, 2, actionVars);
		int haltTrans = halt(bdd, Variable.unionVariables(xVars, yVars), haltAct);
		trans = BDDWrapper.orTo(bdd, trans, haltTrans);
		BDDWrapper.free(bdd, leftAct);
		BDDWrapper.free(bdd, rightAct);
		BDDWrapper.free(bdd, haltAct);
		BDDWrapper.free(bdd, haltTrans);
		Agent2D robot = new Agent2D(bdd, name, type, vars, actionVars, trans, init, xVars, yVars);
		return robot;
	}
	
	public static int increment(BDD bdd, Variable[] vars, int lowerBound, int upperBound, int incAmount, int action){
		Variable[] varsPrime = Variable.getPrimedCopy(vars);
		if(varsPrime == null){
			System.out.println("The variables have no primed copy!");
			return -1;
		}
		
		int trans = bdd.ref(bdd.getZero());
		for(int i=lowerBound; i<upperBound; i++){
			int pre = BDDWrapper.assign(bdd, i, vars);
			int post = BDDWrapper.assign(bdd, i+incAmount, varsPrime);
			pre = BDDWrapper.andTo(bdd, pre, post);
			BDDWrapper.free(bdd, post);
			trans = BDDWrapper.orTo(bdd, trans, pre);
			BDDWrapper.free(bdd, pre);
		}
		
		trans = BDDWrapper.andTo(bdd, trans, action);
		return trans;
	}
	
	public static int decrement(BDD bdd, Variable[] vars, int lowerBound, int upperBound, int decAmount, int action){
		Variable[] varsPrime = Variable.getPrimedCopy(vars);
		if(varsPrime == null){
			System.out.println("The variables have no primed copy!");
			return -1;
		}
		
		int trans = bdd.ref(bdd.getZero());
		for(int i=upperBound; i>lowerBound; i--){
			int pre = BDDWrapper.assign(bdd, i, vars);
			int post = BDDWrapper.assign(bdd, i-decAmount, varsPrime);
			pre = BDDWrapper.andTo(bdd, pre, post);
			BDDWrapper.free(bdd, post);
			trans = BDDWrapper.orTo(bdd, trans, pre);
			BDDWrapper.free(bdd, pre);
		}
		
		trans = BDDWrapper.andTo(bdd, trans, action);
		return trans;
	}
	
	

	
}
