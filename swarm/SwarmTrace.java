package swarm;

import java.util.ArrayList;

public class SwarmTrace {
	
	ArrayList<SwarmTraceNode> trace; 
	int loopingIndex;

	public SwarmTrace(ArrayList<SwarmTraceNode> argTrace, int argLoopingIndex) {
		trace = argTrace;
		loopingIndex = argLoopingIndex;
	}
	
	public ArrayList<SwarmTraceNode> getTrace(){
		return trace;
	}
	
	public int loopingIndex(){
		return loopingIndex;
	}

}
