package swarm;

import game.BDDWrapper;
import game.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import utils.UtilityMethods;
import jdd.bdd.BDD;

public class FrameSynchronizer {

	public FrameSynchronizer() {
		
	}

	/**
	 * each process p_i starts from q^i_0 and goes to q^i_n where n is the process length 
	 * and then  goes back to q^i_l where l is the looping point 
	 * @param bdd
	 * @param numOfProcesses
	 * @param processLength
	 * @param loopingIndex
	 * @param safetyObjective
	 * @param livenessSynchronization
	 * @return
	 */
	public static boolean[][][] computeSynchronizationSkeleton(BDD bdd, int numOfProcesses, int processLength, int loopingIndex, 
			ArrayList<Variable[]> processVariables, int safetyObjective, boolean[][][] livenessSynchronization){
		
		long t_all = UtilityMethods.timeStamp();
		
		
//		//debugging 
//		System.out.println("Computing additional synchronization points for safety");
//		System.out.println("number of processes is "+numOfProcesses);
//		System.out.println("each process is of length "+processLength);
//		System.out.println("the looping position is "+loopingIndex);
		
		//process variables
//		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
//				
//					
//		for(int i=0; i<numOfProcesses; i++){
//			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(processLength-1), "P"+i);
//			processVariables.add(p_i);
//		}
				
		Variable[] allProcessVariables = Variable.unionVariables(processVariables);
		
		
		
		//debugging
//		System.out.println("Computing reachable frames");
//		long t_reachableFrames = UtilityMethods.timeStamp();
		
		//compute reachable frame profiles
		ArrayList<Frame[]> reachableFrameProfiles = computeReachableFrameProfiles(numOfProcesses, processLength, loopingIndex, livenessSynchronization);
		
//		UtilityMethods.duration(t_reachableFrames, "reachable frame profiles were computed in");
//		System.out.println("printing reachable frame profiles");
//		printFrameProfiles(reachableFrameProfiles);
		
//		UtilityMethods.getUserInput();
		
		
		//check safety of the frame profiles and compute the set of possible synchronization points
//		long t_safety = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Integer>> candidateSynchronizationPoints = new ArrayList<ArrayList<Integer>>();
		for(Frame[] fp : reachableFrameProfiles){
			candidateSynchronizationPoints.addAll(checkFrameProfileSafety(bdd, allProcessVariables, processVariables, processLength, loopingIndex, fp, safetyObjective));
		}
//		UtilityMethods.duration(t_safety, "checking the safety of frame profiles and computing additional synchronization candidates was done in");
		
//		System.out.println("candidate synchronization points");
//		for(int s = 0; s<candidateSynchronizationPoints.size(); s++){
//			System.out.println("set "+s);
//			ArrayList<Integer> set = candidateSynchronizationPoints.get(s);
//			for(int i =0; i< set.size(); i++){
//				System.out.print(set.get(i));
//				if(i!=set.size()-1){
//					System.out.print(" , ");
//				}
//			}
//			System.out.println();
//		}
		
//		UtilityMethods.getUserInput();
		
		
		if(candidateSynchronizationPoints.size()!=0){
			//solve the minimal hitting set problem
//			long t_select = UtilityMethods.timeStamp();
			ArrayList<Integer> newSynchronizationPositions = selectSynchronizationPoints(processLength, candidateSynchronizationPoints);
//			UtilityMethods.duration(t_select, "synchronization points were selected in ");
			
//			System.out.println("additional synch points ");
//			for(int i=0; i<newSynchronizationPositions.size();i++){
//				System.out.print(newSynchronizationPositions.get(i)+" ");
//			}
//			System.out.println();
	//		UtilityMethods.getUserInput();
			
			//reduce the synchronization
//			long t_reduce = UtilityMethods.timeStamp();
			boolean[][][] synchronizationSkeleton = reduceSynchronization(bdd, processVariables, loopingIndex, safetyObjective, livenessSynchronization, newSynchronizationPositions);
//			UtilityMethods.duration(t_reduce, "Reducing the number of synchronization points took ");
//			printSynchronizationSkeleton(synchronizationSkeleton);
			
//			UtilityMethods.duration(t_all, "computing safety synchronization took ");
			return synchronizationSkeleton;
		}
		
		//if there is no need for additional safety synchronization, simply return the liveness synchronization skeleton
		return livenessSynchronization;
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		test();
	}
	
	public static void test(){
		BDD bdd = new BDD(10000, 1000); 
		
		long t_all = UtilityMethods.timeStamp();
		
		//each process p_i starts from q^i_0 and goes to q^i_n where n is the process length and 
		//then  goes back to q^i_l where l is the looping point 
		int numOfProcesses = 3;
		int processLength = 8;
		int loopingPosition = 0; 
		
		//debugging 
		System.out.println("Starting the experiment, time is "+new Date().toString());
		System.out.println("number of processes is "+numOfProcesses);
		System.out.println("each process is of length "+processLength);
		System.out.println("the looping position is "+loopingPosition);
		
		//process variables
		ArrayList<Variable[]> processVariables = new ArrayList<Variable[]>();
				
					
		for(int i=0; i<numOfProcesses; i++){
			Variable[] p_i = Variable.createVariablesAndTheirPrimedCopy(bdd, UtilityMethods.numOfBits(processLength-1), "P"+i);
			processVariables.add(p_i);
		}
				
		Variable[] allProcessVariables = Variable.unionVariables(processVariables);
		
		boolean[][][] partialSynchronization = new boolean[numOfProcesses][processLength][numOfProcesses];
		
		//no synchronization at all
		for(int i=0; i<numOfProcesses;i++){
			for(int j=0; j<processLength; j++){
				for(int k=0; k<numOfProcesses; k++){
					partialSynchronization[i][j][k] = false;
				}
			}
		}
		
		System.out.println("printing partial synchronization function");
		printSynchronizationSkeleton(partialSynchronization);
//		UtilityMethods.getUserInput();
		
		//TODO: define the safety objective
		
		int q3 = BDDWrapper.assign(bdd, 3, processVariables.get(0));
		int q7 = BDDWrapper.assign(bdd, 7, processVariables.get(0));
		int u1 = BDDWrapper.assign(bdd, 1, processVariables.get(1));
		int u5 = BDDWrapper.assign(bdd, 5, processVariables.get(1));
//		int v1 = BDDWrapper.assign(bdd, 1, processVariables.get(2));
//		int v5 = BDDWrapper.assign(bdd, 5, processVariables.get(2));
		int q3Orq7 = BDDWrapper.or(bdd, q3, q7);
		int u1Oru5 = BDDWrapper.or(bdd, u1, u5);
//		int v1Orv5 = BDDWrapper.or(bdd, v1, v5);
		
		int notSafe = BDDWrapper.and(bdd, q3Orq7, u1Oru5);
//		notSafe = BDDWrapper.andTo(bdd, notSafe, v1Orv5);
			
		int safetyObjective = bdd.ref(bdd.not(notSafe));
		
		//debugging 
		System.out.println("safety objective is defined");
		
		
		//debugging
		System.out.println("Computing reachable frames");
		long t_reachableFrames = UtilityMethods.timeStamp();
		
		//compute reachable frame profiles
		ArrayList<Frame[]> reachableFrameProfiles = computeReachableFrameProfiles(numOfProcesses, processLength, loopingPosition, partialSynchronization);
		
		UtilityMethods.duration(t_reachableFrames, "reachable frame profiles were computed in");
		System.out.println("printing reachable frame profiles");
		printFrameProfiles(reachableFrameProfiles);
		
//		UtilityMethods.getUserInput();
		
		
		//check safety of the frame profiles and compute the set of possible synchronization points
		long t_safety = UtilityMethods.timeStamp();
		ArrayList<ArrayList<Integer>> candidateSynchronizationPoints = new ArrayList<ArrayList<Integer>>();
		for(Frame[] fp : reachableFrameProfiles){
			candidateSynchronizationPoints.addAll(checkFrameProfileSafety(bdd, allProcessVariables, processVariables, processLength, loopingPosition, fp, safetyObjective));
		}
		UtilityMethods.duration(t_safety, "checking the safety of frame profiles and computing additional synchronization candidates was done in");
		
		System.out.println("candidate synchronization points");
		for(int s = 0; s<candidateSynchronizationPoints.size(); s++){
			System.out.println("set "+s);
			ArrayList<Integer> set = candidateSynchronizationPoints.get(s);
			for(int i =0; i< set.size(); i++){
				System.out.print(set.get(i));
				if(i!=set.size()-1){
					System.out.print(" , ");
				}
			}
			System.out.println();
		}
		
//		UtilityMethods.getUserInput();
		
		
		
		//solve the minimal hitting set problem
		long t_select = UtilityMethods.timeStamp();
		ArrayList<Integer> newSynchronizationPositions = selectSynchronizationPoints(processLength, candidateSynchronizationPoints);
		UtilityMethods.duration(t_select, "synchronization points were selected in ");
		
		System.out.println("additional synch points ");
		for(int i=0; i<newSynchronizationPositions.size();i++){
			System.out.print(newSynchronizationPositions.get(i)+" ");
		}
		System.out.println();
//		UtilityMethods.getUserInput();
		
		//reduce the synchronization
		long t_reduce = UtilityMethods.timeStamp();
		boolean[][][] synchronizationSkeleton = reduceSynchronization(bdd, processVariables, loopingPosition, safetyObjective, partialSynchronization, newSynchronizationPositions);
		UtilityMethods.duration(t_reduce, "Reducing the number of synchronization points took ");
		printSynchronizationSkeleton(synchronizationSkeleton);
		
		UtilityMethods.duration(t_all, "the whole process took ");
	}
	
	public static void printSynchronizationSkeleton(boolean[][][] synchSkeleton){
		int processLength = synchSkeleton[0].length;
		int numOfProcesses = synchSkeleton.length;
		for(int l = 0; l<processLength; l++){
			System.out.println("state "+l);
			for(int i=0; i<synchSkeleton.length; i++){
				for(int j=0; j<numOfProcesses; j++){
					System.out.print(synchSkeleton[i][l][j]+" ");
				}
				System.out.println();
			}
		}
	}
	
	private static void printFrameProfiles(ArrayList<Frame[]> frameProfiles){
		for(Frame[] fp : frameProfiles){
			printFrameProfile(fp);
			System.out.println("\n***********************************");
		}
	}
	
	private static void printFrameProfile(Frame[] fp){
		for(int i=0; i<fp.length;i++){
			fp[i].print();
			System.out.print(" ");
		}
		System.out.println();
	}
	
	private static boolean[][][] reduceSynchronization(BDD bdd, ArrayList<Variable[]> processVars, int loopingPosition, int safetyObjective, boolean[][][] partialSynchronization, ArrayList<Integer> safetySynchronization){
		
		Collections.sort(safetySynchronization);
		
		Variable[] allProcessVariables = Variable.unionVariables(processVars);
		
		
		int numOfProcesses = partialSynchronization.length;
		int processLength = partialSynchronization[0].length;
		
		boolean[][][] synchronizationSkeleton = new boolean[numOfProcesses][processLength][numOfProcesses];
		
		//initialize the synchronization skeleton
		for(int i=0; i<numOfProcesses;i++){
			for(int l=0; l<processLength;l++){
				for(int j=0; j<numOfProcesses;j++){
					if(i==j){
						synchronizationSkeleton[i][l][j] = false;
					}else{
						boolean safetySynch = safetySynchronization.contains(l);
						//synchronize if and only if it needs to synchronize according to partial synch or because of safety
						synchronizationSkeleton[i][l][j] = (partialSynchronization[i][l][j] || safetySynch);
					}
				}
			}
		}
		
//		System.out.println("Reducing the synchronization");
//		System.out.println("Combining the partial synchronization with safety synchronization");
//		printSynchronizationSkeleton(synchronizationSkeleton);
		
		for(int i=0; i<safetySynchronization.size(); i++){
			int synchronizationIndex = safetySynchronization.get(i); 
			
			//each time remove synchronization between two processes and check the safety
			for(int j=0; j<numOfProcesses; j++){
				for(int k=j+1; k<numOfProcesses; k++){
					//remove synchronization between process j and k provided that it is not included in the partial synchronization
					if(!partialSynchronization[j][synchronizationIndex][k]){
						synchronizationSkeleton[j][synchronizationIndex][k] = false;
						synchronizationSkeleton[k][synchronizationIndex][j] = false;
						
//						System.out.println("Removing the synchronization between processes "+j+" and "+k+" at state "+synchronizationIndex);
						
						ArrayList<Frame[]> reachableFrameProfiles = computeReachableFrameProfiles(numOfProcesses, processLength, loopingPosition, synchronizationSkeleton);
						
//						System.out.println("new reachable frame profiles");
//						printFrameProfiles(reachableFrameProfiles);
//						System.out.println();
//						UtilityMethods.getUserInput();
						
						boolean safe = true;
						for(Frame[] fp : reachableFrameProfiles){
							//check if the reachable frame profiles are still safe with new synchronization skeleton
							safe = justCheckFrameProfileSafety(bdd, allProcessVariables, processVars, fp, safetyObjective);
							if(!safe){
								break;
							}
						}
						//if no, roll back
						if(!safe){
//							System.out.println("the resulting reachable frame profiles are not safe, rolling back!");
//							UtilityMethods.getUserInput();
							synchronizationSkeleton[j][synchronizationIndex][k] = true;
							synchronizationSkeleton[k][synchronizationIndex][j] = true;
						}
					}
					
				}
			}
		}
		
		return synchronizationSkeleton;
		
	}
	
	/**
	 * Selects a set of synchronization points from the set of possible candidates
	 * @param candidateSynchronizationPoints
	 * @return
	 */
	private static ArrayList<Integer> selectSynchronizationPoints(int processLength, ArrayList<ArrayList<Integer>> candidateSynchronizationPoints){
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		//remove all the sets that their subsets also belongs to the candidateSynchronization points set
		ArrayList<ArrayList<Integer>> filteredSets = removeSuperSets(candidateSynchronizationPoints);
		
//		//debugging
//		System.out.println("supersets removed");
//		for(int i=0;i<filteredSets.size();i++){
//			ArrayList<Integer> set = filteredSets.get(i);
//			for(int j=0; j<set.size(); j++){
//				System.out.print(set.get(j)+" ");
//			}
//			System.out.println();
//		}
		
		//transform the sets into intervals 
		//partition the intervals based on the length of elements, single intervals and double intervals
		
		ArrayList<Interval> singleIntervals = new ArrayList<Interval>();
		ArrayList<Interval[]> doubleIntervals = new ArrayList<Interval[]>();
		for(int i=0; i<filteredSets.size(); i++){
			ArrayList<Interval> intervals_i = setToIntervals(filteredSets.get(i));
			
			if(intervals_i.size()==1){
				singleIntervals.add(intervals_i.get(0));
				
//				//debugging
//				intervals_i.get(0).print();
//				System.out.println();
			}else if(intervals_i.size()==2){
				doubleIntervals.add(new Interval[]{intervals_i.get(0), intervals_i.get(1)});
				
//				//debugging
//				intervals_i.get(0).print();
//				System.out.println();
//				//debugging
//				intervals_i.get(1).print();
//				System.out.println();
			}else{
				System.err.println("Something must be wrong! We have a set with more than two intervals");
			}
		}
		
		ArrayList<ArrayList<Interval>> intervals = new ArrayList<ArrayList<Interval>>();
		if(doubleIntervals.size()!=0){
			//for each combination of double intervals, get a set of intervals
			ArrayList<ArrayList<Interval>> intervalCombinations = doubleIntervalCombinations(doubleIntervals);
			
			
			for(int i=0; i<intervalCombinations.size(); i++){
				ArrayList<Interval> newIntervalSet = new ArrayList<Interval>();
				newIntervalSet.addAll(singleIntervals);
				newIntervalSet.addAll(intervalCombinations.get(i));
				intervals.add(newIntervalSet);
			}
		}else{
			intervals.add(singleIntervals);
		}
		
		
		
		//for each set of intervals, compute the hitting points
		ArrayList<ArrayList<Integer>> synchronizationCandidates = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<intervals.size(); i++){
			
			
			
			ArrayList<Interval> currentIntervals = intervals.get(i);
			
//			System.out.println("current synchronization candidates are");
//			for(int j=0; j< currentIntervals.size(); j++){
//				currentIntervals.get(j).print();
//				System.out.print(" ");
//			}
//			System.out.println();
			
			Interval[] currentIntervalArray = currentIntervals.toArray(new Interval[currentIntervals.size()]);
			ArrayList<Integer> sychCandidates = hittingIntervals(currentIntervalArray);
			synchronizationCandidates.add(sychCandidates);
		}
		
		//choose one hitting interval based on some criteria
		int min = synchronizationCandidates.get(0).size();
		int chosenSynchIndex = 0;
		for(int i=1;i<synchronizationCandidates.size();i++){
			if(synchronizationCandidates.get(i).size()<min){
				min = synchronizationCandidates.get(i).size();
				chosenSynchIndex = i;
			}
		}
		
		result = synchronizationCandidates.get(chosenSynchIndex);
		
		return result;
	}
	
	private static void printArrayList(ArrayList<Integer> list){
		for(int i=0; i<list.size(); i++){
			System.out.print(list.get(i)+" ");
		}
		System.out.println();
	}
	
	/**
	 * Returns different combinations of double intervals
	 * @param doubleIntervals
	 * @return
	 */
	private static ArrayList<ArrayList<Interval>> doubleIntervalCombinations(ArrayList<Interval[]> doubleIntervals){
		int l = doubleIntervals.size();
		//there are 2^l combinations
		int numOfCombinations = (int) Math.pow(2, l);
		ArrayList<ArrayList<Interval>> result = new ArrayList<ArrayList<Interval>>();
		for(int i=0; i<numOfCombinations; i++){
			String binary = Integer.toBinaryString(i);
			if(binary.length()!=l){
				int length = binary.length();
				for(int j=0; j<(l - length);j++){
					binary+="0"+binary;
				}
			}
			ArrayList<Interval> combination_i =  new ArrayList<Interval>();
			for(int j=0;j<l;j++){
				Interval[] di = doubleIntervals.get(j);
				if(binary.charAt(j)=='0'){
					combination_i.add(di[0]);
				}else{
					combination_i.add(di[1]);
				}
			}
			result.add(combination_i);
		}
		return result;
	}
	
	private static ArrayList<ArrayList<Integer>> removeSuperSets(ArrayList<ArrayList<Integer>> sets){
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<sets.size(); i++){
			boolean superset = false; 
			for(int j=0; j<sets.size(); j++){
				if(i==j) continue;
				if(subset(sets.get(j),sets.get(i))){
					if(subset(sets.get(i),sets.get(j)) && j>i){
						continue;
					}
					superset = true;
					break;
				}
			}
			if(!superset){
				result.add(sets.get(i));
			}
		}
		return result;
	}
	
	/**
	 * Converts a set to a set of intervals
	 * @param set
	 * @return
	 */
	private static ArrayList<Interval> setToIntervals(ArrayList<Integer> set){
		//sort the set 
		Collections.sort(set);
		
//		System.out.println("in setToInterval");
//		for(int i=0; i<set.size();i++){
//			System.out.println(set.get(i)+" ");
//		}
//		System.out.println();
		
		//sweep over the set and compute the intervals
		ArrayList<Interval> result = new ArrayList<Interval>();
		int currentLowerBound = set.get(0);
		int currentUpperBound = currentLowerBound;
		if(set.size()==1){
			result.add(new Interval(currentLowerBound, currentUpperBound));
			//debugging
//			System.out.println("interval added: ["+currentLowerBound+" ,  "+currentUpperBound+"] ");
			return result;
		}
		for(int i=1; i<set.size(); i++){
			int next = set.get(i);
			if(next == currentUpperBound+1){
				currentUpperBound = next; 
			}else{
				result.add(new Interval(currentLowerBound, currentUpperBound));
				
				//debugging
//				System.out.println("interval added: ["+currentLowerBound+" ,  "+currentUpperBound+"] ");
				
				currentLowerBound = next;
				currentUpperBound = next;
			}
		}
		return result;
	}
	
	/**
	 * Given a set of intervals, return a set of positions that
	 * hit all the input intervals
	 * @param intervals
	 * @return
	 */
	private static ArrayList<Integer> hittingIntervals(Interval[] intervals){
		//sort intervals
		sortIntervals(intervals);
		
//		System.out.println("intervals sorted");
//		for(int i=0; i<intervals.length; i++){
//			intervals[i].print();
//			System.out.print(" ");
//		}
//		System.out.println();
		
		
		int currentIndex = 0;
		Interval currentInterval = intervals[0];
		ArrayList<Integer> hittingPoints = new ArrayList<Integer>();
		while(true){
			currentIndex++;
			if(currentIndex == intervals.length){
				hittingPoints.add(selectPoint(currentInterval));
				break;
			}
			Interval intersect = currentInterval.intersect(intervals[currentIndex]);
			if(intersect == null){
				hittingPoints.add(selectPoint(currentInterval));
				currentInterval = intervals[currentIndex];
			}else{
				currentInterval = intersect;
			}
		}
		return hittingPoints;
	}
	
	/**
	 * Selects a point from the interval, right now it returns the upperbound
	 * @param interval
	 * @return
	 */
	private static int selectPoint(Interval interval){
		return interval.getUpperBound();
	}
	
	/**
	 * given a set of intervals, sort them based on their lower bound, if two intervals have the same lower bound, the one with 
	 * smaller upper bound comes first
	 * @param intervals
	 * @return
	 */
	private static void sortIntervals(Interval[] intervals){
		for(int i=0; i<intervals.length; i++){
			for(int j=i+1; j<intervals.length; j++){
				if(intervals[i].compareTo(intervals[j])==1){
					//swap
					Interval tmp = new Interval(intervals[i].getLowerBound(), intervals[i].getUpperBound());
					intervals[i] = intervals[j];
					intervals[j] = tmp;
				}
			}
		}
	}
	
	/**
	 * TODO: there might be frame profiles that deadlock. check if it can cause a problem. also check if a progress condition similar to 
	 * bdd variant is required
	 * Given a set of processes and synchronization skeletons, compute the set of reachable frames
	 * @param numOfProcesses
	 * @param processLength
	 * @param loopingPosition
	 * @param synchronization
	 * @return
	 */
	public static ArrayList<Frame[]> computeReachableFrameProfiles(int numOfProcesses, int processLength, int loopingPosition, boolean[][][] synchronization){
//		//compute frames for each process
//		
//		//recursively choose frame profiles and check compatiblity 
//		
//		//compute the starting frames from the initial state
//		
//		//iteratively compute the next reachable frame
//		
//		//check if the new frame does not already exists
//		
//		//if not, add the new frame profile and repeat the process
		
		ArrayList<Frame[]> reachableFrameProfiles = new ArrayList<Frame[]>();
		
		//compute the initial frame profile that is the sequence of states from initial state until the next synchronization point for each process
		Frame[] initialFrameProfile = new Frame[numOfProcesses];
		
		
		for(int i=0; i<numOfProcesses;i++){
			initialFrameProfile[i] = computeNextFrame(i, processLength, loopingPosition, 0, synchronization);
		}
		
//		reachableFrameProfiles.add(initialFrameProfile);
		
//		//debugging 
//		System.out.println("initial reachable frame profile");
//		printFrameProfile(initialFrameProfile);
		
				
		//iteratively compute the next reachable frame profiles
		ArrayList<Frame[]> frameProfilesQ = new ArrayList<Frame[]>();
		frameProfilesQ.add(initialFrameProfile);
		
		while(!frameProfilesQ.isEmpty()){
			Frame[] currentFrameProfile = frameProfilesQ.get(0);
			frameProfilesQ.remove(0);
			
			//TODO: check if the new frameProfile has already been explored
			if(!frameProfileExists(reachableFrameProfiles, currentFrameProfile)){
			
				reachableFrameProfiles.add(currentFrameProfile);
				
				ArrayList<Frame[]> nextReachableFrameProfiles = computeNextReachableFrameProfiles(currentFrameProfile, synchronization,processLength, loopingPosition);
				
				frameProfilesQ.addAll(nextReachableFrameProfiles);
			}
		}
		
		return reachableFrameProfiles;
	}
	
	/**
	 * Returns true of the frame profile "frameProfile" already exists in the set of frame profiles, false otherwise
	 * @param setOfFrameProfiles
	 * @param frameProfile
	 * @return
	 */
	private static boolean frameProfileExists(ArrayList<Frame[]> setOfFrameProfiles, Frame[] frameProfile){
		for(Frame[] fp : setOfFrameProfiles){
			if(areFrameProfilesEquivalent(fp, frameProfile)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean areFrameProfilesEquivalent(Frame[] fp1, Frame[] fp2){
		if(fp1.length != fp2.length){
			return false;
		}
		for(int i=0; i<fp1.length; i++){
			if(!fp1[i].equal(fp2[i])){
				return false;
			}
		}
		return true;
	}
	
	private static ArrayList<Frame[]> computeNextReachableFrameProfiles(Frame[] frameProfile, boolean[][][] synchronizationPoints, int processLength, int loopingPoint ){
		ArrayList<Frame[]> result = new ArrayList<Frame[]>();
		
//		//get the end points of each frame
//		ArrayList<Integer> frameEndpoints = new ArrayList<Integer>();
//		for(Frame f : frameProfile){
//			ArrayList<Integer> states = f.getIndices();
//			int endpoint = states.get(states.size()-1);
//			if(!frameEndpoints.contains(endpoint)){
//				frameEndpoints.add(endpoint);
//			}
//		}
		
//		Collections.sort(frameEndpoints);
//		
//		//for each set of end points, check if all processes needed to synchronize are ready
//		//if yes, move those processes to their next frames, while keeping the rest the same
//		//if not, move to the next set of end points
//		for(int i=0; i<frameEndpoints.size();i++){
//			int endpoint = frameEndpoints.get(i);
//			for(Frame f : frameProfile){
//				
//			}
//		}
		
		int numOfProcesses= frameProfile.length;
		
		//get the end points of each frame
		int[] frameEndpoints = new int[numOfProcesses];
		int[] frameStartPoint = new int[numOfProcesses];
		for(int i=0; i<numOfProcesses; i++){
			Frame f = frameProfile[i];
			ArrayList<Integer> states = f.getIndices();
			int endpoint = states.get(states.size()-1);
			frameEndpoints[i] = endpoint;
			frameStartPoint[i] = states.get(0);
		}
		
		//each time, one process moves from its current frame to its next frame if other processes that need to synchronize are ready
		//the next frame profile is the next frame for the scheduled process, single end points for processes that need to synchronize
		//with the scheduled process, and the rest of the processes stay the same
		for(int i=0; i<frameProfile.length;i++){
			int endpoint = frameEndpoints[i];
			
			//Processes that synchronize with process i
			ArrayList<Integer> synchronizingProcesses = new ArrayList<Integer>();
			
			//can the frame make progress? 
			boolean progress = true;
			for(int j=0; j<frameProfile.length;j++){
				if(i==j) continue;
				
				if(synchronizationPoints[i][endpoint][j]){
					synchronizingProcesses.add(j);
					if(frameEndpoints[j] != endpoint && frameStartPoint[j] != endpoint){
						progress = false;
						break;
					}
				}
			}
			
			if(progress){
				Frame[] nextFrameProfile = new Frame[numOfProcesses];
				for(int j=0; j<numOfProcesses; j++){
					if(j==i){
						nextFrameProfile[j] = computeNextFrame(j, processLength, loopingPoint, endpoint, synchronizationPoints);
					}else if(synchronizingProcesses.contains(j)){
						//TODO: check for correctness
						if(frameEndpoints[j] == endpoint && frameStartPoint[j] != endpoint){
							ArrayList<Integer> singleState = new ArrayList<Integer>();
							singleState.add(frameEndpoints[j]);
							nextFrameProfile[j] = new Frame(singleState);
						}else{
							nextFrameProfile[j] = frameProfile[j];
						}
					}else{
						nextFrameProfile[j] = frameProfile[j];
					}
					
				}
				result.add(nextFrameProfile);
			}
		}
		
		return result; 
	}
	
	private static Frame computeNextFrame(int processIndex, int processLength, int loopingPoint, int startingState, boolean[][][] synchronizationPoints){
		ArrayList<Integer> frameStates = new ArrayList<Integer>();
		
		//add the starting point
		frameStates.add(startingState);
		
		//strating from the starting state, go on until hitting the next synchronization point
		int currentState = startingState; 
		while(true){
			int nextState = currentState+1;
			if(nextState == processLength){
				nextState = loopingPoint;
			}
			
			if(frameStates.contains(nextState)){
				frameStates.add(nextState);
				break;
			}
			
			frameStates.add(nextState);
			
			//does the process synchronize with any other process at the next state? 
			boolean synchronize=false;
			for(int i=0; i<synchronizationPoints[processIndex][nextState].length; i++){
				if(synchronizationPoints[processIndex][nextState][i]){
					synchronize = true;
					break;
				}
			}
			
			//if synchronization point is reached, frame is complete
			if(synchronize){
				break;
			}
			
//			if(nextState == processLength*2-1){
//				if(startingState == 0){
//					//the whole process is a frame
//					break;
//				}
//			}
			
			//otherswise go to the next state 
			currentState = nextState;
		}
		Frame result= new Frame(frameStates);
		return result;
	}
	
//	/**
//	 * Given the skeleton of the process and synchronization points, computes the set of frames for the process
//	 * @param processLength
//	 * @param loopingPosition
//	 * @param synchronization
//	 * @return
//	 */
//	public static ArrayList<Frame> computeProcessFrames(int processIndex, int processLength, int loopingPosition, boolean[][][] synchronization){
//		
//	}
//	
//	/**
//	 * Given two frames of two processes, checks if the frames can happen at the same time, i.e., the two processes can be at these 
//	 * two frames simultaneously at any point during the execution
//	 * @param process1_index
//	 * @param f1
//	 * @param process2_index
//	 * @param f2
//	 * @param processLength
//	 * @param looping
//	 */
//	public static boolean checkFrameCompatibilty(int process1_index, Frame f1, int process2_index, Frame f2, int processLength, int loopingPosition, boolean[][][] synchronization){
//		int p1PreSynchIndex=-1;
//		int p2PreSynchIndex=-1;
//		int p1PostSynchIndex=2*processLength;
//		int p2PostSynchIndex=2*processLength;
//		
//		int p1RightmostState 
//		
//		//find the synchronization between p1 and p2 proceeding the initial states of the frames
//		
//		//find the synchronization between p1 and p2 after the final states of the frames
//		
//		//check if pre and post synchronization points between two processes match, if yes return true, otherwise return false
//		if(p1PreSynchIndex == p2PreSynchIndex && p1PostSynchIndex == p2PostSynchIndex){
//			return true;
//		}
//		
//		return false;
//	}
//	
//	
//	
//	/**
//	 * Given a state p1state for the process p1, computes the closest synchronization point 
//	 * @param p1
//	 * @param p1State
//	 * @param p2
//	 * @param processLength
//	 * @param loopingPosition
//	 * @param synchronization
//	 * @return
//	 */
//	public static int previousSynchronizationPoint(int p1, int p1State, int p2, int processLength, int loopingPosition, boolean[][][] synchronization){
//		int currentState = p1State; 
//		int previousSynchPoint = 0; 
//		
//		
//		return previousSynchPoint;
//	}
	
	public static boolean justCheckFrameProfileSafety(BDD bdd, Variable[] allProcessVariables, ArrayList<Variable[]> processVars, Frame[] frameProfile, int safetyObjective){
		//translate frames into an equivalent logical formula
				int frameProfileFormula = bdd.ref(bdd.getOne());
				//turn each frame to a conjunctive formula
				for(int i=0; i<processVars.size();i++){
					ArrayList<Integer> frame_i = frameProfile[i].getIndices();
					int frameFormula = bdd.ref(bdd.getZero());
					for(int j=0; j<frame_i.size();j++){
						int q_j = BDDWrapper.assign(bdd, frame_i.get(j), processVars.get(i));
						frameFormula = BDDWrapper.orTo(bdd, frameFormula, q_j);
						BDDWrapper.free(bdd, q_j);
					}
					frameProfileFormula = BDDWrapper.andTo(bdd, frameProfileFormula, frameFormula);
					BDDWrapper.free(bdd, frameFormula);
				}
				
				//not safe states 
				int notSafe = BDDWrapper.not(bdd, safetyObjective);
				
				//conjunct the the frameProfileFormula with the unsafe set to obtain the possible unsafe states
				int unsafeStates = BDDWrapper.and(bdd, frameProfileFormula, notSafe);
				BDDWrapper.free(bdd, notSafe);
				
				//if the set of unsafe states are not empty, compute a set of possible candidate points for synchronization
				if(unsafeStates != 0){
					return false;
				}
				
				return true;
	}
	
	/**
	 * Given a set of frames and a safety objectives, computes a set of indices where the processes need to synchronize to satisfy safety
	 * returns empty set if the frame profile is safe
	 * @param bdd
	 * @param frameProfile
	 * @param safetyObjective
	 */
	public static ArrayList<ArrayList<Integer>> checkFrameProfileSafety(BDD bdd, Variable[] allProcessVariables, ArrayList<Variable[]> processVars, int processLength, int loopingPosition, Frame[] frameProfile, int safetyObjective){
		ArrayList<ArrayList<Integer>> synchronizationPointsCandidates = new ArrayList<ArrayList<Integer>>();
		
		//translate frames into an equivalent logical formula
		int frameProfileFormula = bdd.ref(bdd.getOne());
		//turn each frame to a conjunctive formula
		for(int i=0; i<processVars.size();i++){
			ArrayList<Integer> frame_i = frameProfile[i].getIndices();
			int frameFormula = bdd.ref(bdd.getZero());
			for(int j=0; j<frame_i.size();j++){
				int q_j = BDDWrapper.assign(bdd, frame_i.get(j), processVars.get(i));
				frameFormula = BDDWrapper.orTo(bdd, frameFormula, q_j);
				BDDWrapper.free(bdd, q_j);
			}
			frameProfileFormula = BDDWrapper.andTo(bdd, frameProfileFormula, frameFormula);
			BDDWrapper.free(bdd, frameFormula);
		}
		
		//not safe states 
		int notSafe = BDDWrapper.not(bdd, safetyObjective);
		
		//conjunct the the frameProfileFormula with the unsafe set to obtain the possible unsafe states
		int unsafeStates = BDDWrapper.and(bdd, frameProfileFormula, notSafe);
		BDDWrapper.free(bdd, notSafe);
		
//		UtilityMethods.debugBDDMethods(bdd, "unsafe states are ", unsafeStates);
		
		//if the set of unsafe states are not empty, compute a set of possible candidate points for synchronization
		if(unsafeStates != 0){
			ArrayList<String> unsafeMinterms = BDDWrapper.minterms(bdd, unsafeStates, allProcessVariables);
			
			//for each unsafe state, compute its corresponding set of candidate synchronization points
			

			
			for(int i=0; i<unsafeMinterms.size(); i++){
//				ArrayList<ArrayList<Integer>> candidateSynchPointsForEachFrame = new ArrayList<ArrayList<Integer>>();
				
				String unsafeState = unsafeMinterms.get(i);
				
//				System.out.println("minterm is "+unsafeState);
				
				//parse the unsafe state to a <i_1, i_2,..,i_p> tuple
				int currentIndex = 0;
				
				int[] unsafeStateIndices = new int[frameProfile.length];
				for(int j=0; j<frameProfile.length; j++){
					
					int p_j_numOfVars = processVars.get(j).length;
					String p_j_minterm= unsafeState.substring(currentIndex, currentIndex+p_j_numOfVars);
					currentIndex += p_j_numOfVars;
					
					//translate to integer
					int p_j_state = Integer.parseInt(p_j_minterm, 2);
					
					unsafeStateIndices[j] = p_j_state;
					
//					ArrayList<Integer> candidateSynchPointsForUnsafeStateAndFrame_i = new ArrayList<Integer>();
//					
//					for(int k=0; k<frameProfile[j].indices.size(); k++){
//						if(frameProfile[j].indices.get(k)!=p_j_state){
//							candidateSynchPointsForUnsafeStateAndFrame_i.add(frameProfile[j].indices.get(k));
//						}
//					}
//					
//					candidateSynchPointsForEachFrame.add(candidateSynchPointsForUnsafeStateAndFrame_i);	
				}
				
				//debugging
//				System.out.println("unsafe state indices");
//				for(int j=0; j<frameProfile.length;j++){
//					System.out.println(unsafeStateIndices[j]+" ");
//				}
				
				//compute min and max of indices for the unsafe state
				int minIndex = min(unsafeStateIndices);
				int maxIndex = max(unsafeStateIndices);
//				int minAfterLoop = min(unsafeStateIndices, loopingPosition);
				
//				System.out.println("min index is "+minIndex);
//				System.out.println("max index is "+ maxIndex);
				
				//any state with index between min and max is a possible synchronization point
				ArrayList<Integer> increasingSynchPoints = new ArrayList<Integer>();
				for(int index=minIndex+1; index<maxIndex; index++){
					increasingSynchPoints.add(index);
				}
				
				ArrayList<Integer> loopingSynchPoints = new ArrayList<Integer>();
				if(minIndex>=loopingPosition){
					for(int index = maxIndex+1; ; index++){
						if(index == processLength){
							index = loopingPosition;
						}
						if(index == minIndex){
							break;
						}
						loopingSynchPoints.add(index);
					}
				}
				
				if(increasingSynchPoints.size() != 0){
					synchronizationPointsCandidates.add(increasingSynchPoints);
				}else{
					System.out.println("increasing synch points was empty for frame profile! Possible bug?");
				}
				
				if(loopingSynchPoints.size()!=0){
					synchronizationPointsCandidates.add(loopingSynchPoints);
				}else{
					System.out.println("looping synch points was empty for frame profile! Possible bug?");
				}
				
//				System.out.println("increasing synch points");
//				for(Integer n : increasingSynchPoints){
//					System.out.print(n+" ");
//				}
//				System.out.println();
//				
//				System.out.println("looping synch points");
//				for(Integer n : loopingSynchPoints){
//					System.out.print(n+" ");
//				}
//				System.out.println();
				
				
				
//				//intersect candidate synchronization points for each frame and add the intersection to the candidate synchronization sets
//				ArrayList<Integer> candidateSetForUnsafeMinterm = intersectSets(candidateSynchPointsForEachFrame);
//				synchronizationPointsCandidates.add(candidateSetForUnsafeMinterm);
			}
			
			
		}
		
		return synchronizationPointsCandidates;
	}
	
	private static int min(int[] arr){
		int min = arr[0];
		for(int i=1; i<arr.length;i++){
			if(min>arr[i]){
				min=arr[i];
			}
		}
		return min;
	}
	
	/**
	 * the minimum element greater than or equal to the threshold
	 * assumes threshold is greater than or equal to zero
	 * @param arr
	 * @param threshold
	 * @return
	 */
	private static int min(int[] arr , int threshold){
		int min = Integer.MAX_VALUE;
		for(int i=0; i<arr.length;i++){
			if(arr[i]>=threshold && min>arr[i]){
				min = arr[i];
			}
		}
		return min;
	}
	
	private static int max(int[] arr){
		int max = arr[0];
		for(int i=1; i<arr.length;i++){
			if(max<arr[i]){
				max=arr[i];
			}
		}
		return max;
	}
	
	public static ArrayList<Integer> intersectSets(ArrayList<ArrayList<Integer>> sets){
		ArrayList<Integer> result=new ArrayList<Integer>();
		
		ArrayList<Integer> firstSet = sets.get(0);
		for(int i=0; i<firstSet.size(); i++){
			int index = firstSet.get(i);
			boolean allSetsHaveIt = true;
			for(int j=1; j<sets.size(); j++){
				allSetsHaveIt = false;
				ArrayList<Integer> nextSet = sets.get(j);
				for(int k=0; k<nextSet.size();k++){
					int element = nextSet.get(k);
					if(element == index){
						allSetsHaveIt=true;
						break;
					}
				}
				if(!allSetsHaveIt){
					break;
				}
			}
			if(allSetsHaveIt){
				result.add(index);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns true if set1 is subset of or equal to set2
	 * @param set1
	 * @param set2
	 * @return
	 */
	private static boolean subset(ArrayList<Integer> set1, ArrayList<Integer> set2){
		for(Integer e : set1){
			if(!set2.contains(e)){
				return false;
			}
		}
		return true;
	}

}

class Frame{
	ArrayList<Integer> indices;
	
	Frame(ArrayList<Integer> argIndices){
		indices = argIndices;
	}
	
	ArrayList<Integer> getIndices(){
		return indices;
	}
	
	boolean equal(Frame f2){
		ArrayList<Integer> indices2 = f2.getIndices();
		if(indices.size() != indices2.size()){
			return false;
		}
		for(int i=0; i<indices.size(); i++){
			if(indices.get(i) != indices2.get(i)){
				return false;
			}
		}
		return true;
	}
	
	void print(){
		System.out.print("{");
		for(int i=0; i<indices.size();i++){
			System.out.print(indices.get(i));
			if(i != indices.size()-1) System.out.print(" , ");
		}
		System.out.print("}");
	}
}

class Interval{
	int lowerBound;
	int upperBound; 
	
	Interval(int argLB, int argUB){
		lowerBound = argLB;
		upperBound = argUB; 
	}
	
	boolean equal(Interval i){
		return (lowerBound == i.getLowerBound() && upperBound == i.getUpperBound());
	}
	
	int getLowerBound(){
		return lowerBound;
	}
	
	int getUpperBound(){
		return upperBound;
	}
	
	/**
	 * returns 0 if this interval is lexagraphically smaller than interval i, returns 1 otherwise
	 * @param i
	 * @return
	 */
	int compareTo(Interval i){
		if(lowerBound<i.getLowerBound()){
			return 0;
		}else if(lowerBound>i.getLowerBound()){
			return 1;
		}else if(lowerBound == i.getLowerBound()){
			if(upperBound <= i.getUpperBound()){
				return 0;
			}else{
				return 1;
			}
		}
		return 0;
	}
	
	Interval intersect(Interval i){
		int newLowerBound = Math.max(lowerBound, i.getLowerBound());
		int newUpperBound = Math.min(upperBound, i.getUpperBound());
		//if the intersection is empty, return null
		if(newLowerBound > newUpperBound){
			return null;
		}
		return new Interval(newLowerBound, newUpperBound);
	}
	
	void print(){
		System.out.print("["+lowerBound+","+upperBound+"]");
	}
	
}
