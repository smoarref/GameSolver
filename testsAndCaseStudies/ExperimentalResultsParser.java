package testsAndCaseStudies;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.rmi.CORBA.Util;

import utils.FileOps;
import utils.UtilityMethods;

public class ExperimentalResultsParser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		parseInput("testingExpParsing", "");
	
//		parseInput_BothApproachInLineFormat("cav16perfectExps.txt", "");
		
		parseInput_BothApproachInLineFormat("cav16partialExps.text", "");
		
//		System.out.println(round("0.2801218032836914 MB"));
//		System.out.println(round("14.0 ms"));
//		System.out.println(round("1.203 seconds"));
//		System.out.println(round("8.803533333333332 minutes"));
	}
	
	//assumes the file has the format
	//method \t num of uncontrolled	\t num of controlled \t objective \t dim \t time \t memory \t num of variables
	public static void parseInput(String inputFile, String outputFile){
		String centralResults ="";
		
		//read file 
		String fileContent = FileOps.readFile(inputFile);
	
//		System.out.println(fileContent);
		//parse and store experimental results
		StringTokenizer lines = new StringTokenizer(fileContent,"\n");
		
		ArrayList<ArrayList<String>> experimentsResults = new ArrayList<ArrayList<String>>();

		
		while(lines.hasMoreTokens()){
			String line = lines.nextToken();
			StringTokenizer lineTokens = new StringTokenizer(line, "\t");
			ArrayList<String> results = new ArrayList<String>();
			while(lineTokens.hasMoreTokens()){
				results.add(lineTokens.nextToken());
			}
			experimentsResults.add(results);
		}
		
		//write in latex format
		String latexString = "";
//		ArrayList<String> heading = experimentsResults.get(0);
		for(int i=2; i<experimentsResults.size();i=i+2){
			ArrayList<String> currentCentralExp = experimentsResults.get(i);
			ArrayList<String> currentCompositionalExp = experimentsResults.get(i-1);
			
			//latex format
			//num of uncontrolled & num of controlled & objective & size & time & memory (central) & time & memory (compositional)
			
			//num of uncontrolled
			latexString += "$"+currentCentralExp.get(1)+"$ & ";
			
			centralResults+= currentCentralExp.get(1)+"\t";
			
			//num of controlled
			latexString += "$"+currentCentralExp.get(2)+"$ & ";
			
			centralResults+= currentCentralExp.get(2)+"\t";
			
			//objective
			String latexObj = "";
			String objective = currentCentralExp.get(3);
			if(objective.equals("safety")){
				latexObj = "$\\Phi_1$";
			}else if(objective.equals("safety+formation")){
				latexObj = "$\\Phi_1 \\wedge \\Phi_2$";
			}else if(objective.equals("safety+reachability")){
				latexObj = "$\\Phi_1 \\wedge \\Phi_3 $";
			}else if(objective.equals("all")){
				latexObj = "$\\Phi_1 \\wedge \\Phi_2 \\wedge \\Phi_3$";
			}else{
				System.err.println("unknown objective at line "+i);
				System.out.println(objective);
				UtilityMethods.getUserInput();
			}
			latexString += latexObj +" & ";
			
			centralResults+= currentCentralExp.get(3)+"\t";
			
			//size
			latexString += "$"+currentCentralExp.get(4)+" \\times "+ currentCentralExp.get(4)+" $ & ";
			
			centralResults+= currentCentralExp.get(4)+"\t";
			
			//num of vars 
			latexString += "$"+currentCentralExp.get(7)+"$ & ";
			
			//central
			//time 
			latexString += "$"+currentCentralExp.get(5)+"$ & ";
			
			centralResults+= currentCentralExp.get(5)+"\t";
			
			//memory 
			latexString += "$"+currentCentralExp.get(6)+"$ & ";
			
			centralResults+= currentCentralExp.get(6)+"\n";
			
			//compositionl 
			//time 
			latexString += "$"+currentCompositionalExp.get(5)+"$ & ";
			
			//memory 
			latexString += "$"+currentCompositionalExp.get(6)+"$";
			
			latexString += "\\\\\n";
			latexString += "\\hline\n";
		}
		
		System.out.println(latexString);
		
		FileOps.write(centralResults, "centralResults.txt");
	}
	
	public static void parseInput_BothApproachInLineFormat(String inputFile, String outputFile){
		
		String outOfMemory ="mo";
		String timeOut = "to";
		
		//read file 
		String fileContent = FileOps.readFile(inputFile);
	
//		System.out.println(fileContent);
		//parse and store experimental results
		StringTokenizer lines = new StringTokenizer(fileContent,"\n");
		
		ArrayList<ArrayList<String>> experimentsResults = new ArrayList<ArrayList<String>>();

		
		while(lines.hasMoreTokens()){
			String line = lines.nextToken();
			StringTokenizer lineTokens = new StringTokenizer(line, "\t");
			ArrayList<String> results = new ArrayList<String>();
			while(lineTokens.hasMoreTokens()){
				results.add(lineTokens.nextToken());
			}
			experimentsResults.add(results);
		}
		
		//write in latex format
		String latexString = "";
//		ArrayList<String> heading = experimentsResults.get(0);
		for(int i=1; i<experimentsResults.size();i++){

			
			ArrayList<String> currentExp = experimentsResults.get(i);
			
			//latex format
			//num of uncontrolled & num of controlled & objective & size & time & memory (central) & time & memory (compositional)
			
			//num of uncontrolled
			latexString += "$"+currentExp.get(0)+"$ & ";
			
			//num of controlled
			latexString += "$"+currentExp.get(1)+"$ & ";
			
			//objective
			String latexObj = "";
			String objective = currentExp.get(2);
			if(objective.equals("safety")){
				latexObj = "$\\Phi_1$";
			}else if(objective.equals("safety+formation")){
				latexObj = "$\\Phi_{12}$";
			}else if(objective.equals("safety+reachability")){
				latexObj = "$\\Phi_{13}$";
			}else if(objective.equals("all")){
				latexObj = "$\\Phi$";
			}else{
				System.err.println("unknown objective at line "+i);
				System.out.println(objective);
				UtilityMethods.getUserInput();
			}
			latexString += latexObj +" & ";
			
			//size
			latexString += "$"+currentExp.get(3)+" \\times "+ currentExp.get(3)+" $ & ";
			
			//num of vars 
			latexString += "$"+currentExp.get(4)+"$ & ";
			
			//central
			//time 
			String centralTime = currentExp.get(5);
			if(centralTime.contains("out of memory")){
				latexString += "$"+outOfMemory+"$ & ";
			}else if(centralTime.contains("time out")){
				latexString += "$"+timeOut+"$ & ";
			}else{
				latexString += "$"+round(currentExp.get(5))+"$ & ";
			}
			
			//memory 
			String centralMem = currentExp.get(6);
			if(centralMem.contains("out of memory")){
				latexString += "$"+outOfMemory+"$ & ";
			}else if(centralMem.contains("time out")){
				latexString += "$"+timeOut+"$ & ";
			}else{
				latexString += "$"+round(currentExp.get(6))+"$ & ";
			}
			
			
			//compositionl 
			//time 
			String compTime = currentExp.get(7);
			if(compTime.contains("out of memory")){
				latexString += "$"+outOfMemory+"$ & ";
			}else{
				StringTokenizer st = new StringTokenizer(compTime, "$&");
				String resultTime="";
				while(st.hasMoreTokens()){
					resultTime+=st.nextToken();
				}
				latexString += "$"+round(resultTime)+"$ & ";
			}
			
			//memory 
			String compMem = currentExp.get(8);
			if(compMem.contains("out of memory")){
				latexString += "$"+outOfMemory+"$ & ";
			}else{
				StringTokenizer st2 = new StringTokenizer(compMem, "$&");
				String resultMem="";
				while(st2.hasMoreTokens()){
					resultMem+=st2.nextToken();
				}
				latexString += "$"+round(resultMem)+"$";
			}
			
			latexString += "\\\\\n";
			latexString += "\\hline\n";
		}
		
		System.out.println(latexString);
	}
	
	private static String round(String input){
		String result="";
		StringTokenizer st = new StringTokenizer(input, " ");
		
		//first part - round up
		String num = st.nextToken();
		double number = Double.valueOf(num);
		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);
		result+= df.format(number)+" ";
		
		//second part
		if(st.hasMoreTokens()){
			String unit = st.nextToken();
			if(unit.equalsIgnoreCase("seconds")){
				unit = "s";
			}else if(unit.equalsIgnoreCase("minutes")){
				unit = "min";
			}
		
			if(!unit.equals("MB")) result+="\\text{ }"+unit;
		}
		return result;
	}

}
