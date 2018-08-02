package solver;

import java.util.ArrayList;

import jdd.bdd.BDD;
import utils.UtilityMethods;

public class GR1WinningStates {
	private int Z;
	private ArrayList<ArrayList<Integer>> mY = new  ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<ArrayList<Integer>>> mX = new ArrayList<ArrayList<ArrayList<Integer>>>();
	
	public GR1WinningStates(int argZ, ArrayList<ArrayList<Integer>> argMY, ArrayList<ArrayList<ArrayList<Integer>>> argMX){
		Z = argZ;
		mY = argMY;
		mX = argMX;
	}
	
	public int getWinningStates(){
		return Z;
	}
	
	public ArrayList<ArrayList<Integer>> getMY(){
		return mY;
	}
	
	public ArrayList<ArrayList<ArrayList<Integer>>> getMX(){
		return mX;
	}
	
	public void printMemory(BDD bdd){
		System.out.println("printing the memory");
		System.out.println("mY");
		ArrayList<ArrayList<Integer>> mY = getMY();
		for(int i=0; i<mY.size(); i++){
			ArrayList<Integer> mY_i = mY.get(i);
			for(int j=0; j<mY_i.size(); j++){
				UtilityMethods.debugBDDMethods(bdd, "mY["+i+"]["+j+"] is ",mY_i.get(j));
				
				if(j>0){
					int mY_i_j = mY_i.get(j);
					int mY_i_j_1 = mY_i.get(j-1);
					if(mY_i_j == mY_i_j_1){
						System.out.println("current memory is equivalent to the previous one!");
					}
				}
				
				UtilityMethods.getUserInput();
			}
		}
		
		System.out.println("mX");
		ArrayList<ArrayList<ArrayList<Integer>>> mX = getMX();
		for(int i=0; i<mX.size(); i++){
			ArrayList<ArrayList<Integer>> mX_i = mX.get(i);
			for(int j=0; j<mX_i.size(); j++){
				ArrayList<Integer> mX_i_j = mX_i.get(j);
				for(int k=0; k<mX_i_j.size(); k++){
					UtilityMethods.debugBDDMethods(bdd, "mX["+i+"]["+j+"]["+k+"] is ",mX_i_j.get(k));
					UtilityMethods.getUserInput();
				}
			}
		}
	}
}
