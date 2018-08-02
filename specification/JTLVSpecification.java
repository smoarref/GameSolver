package specification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import utils.FileOps;

public class JTLVSpecification implements Specification{
	
	String smvFile="";
	String spcFile="";
	String strategyFile="";
	
	String SMVSignals;
	ArrayList<String> SMVProperties;
	
	BufferedReader nextLineReader;
	
	//SMV file
	ArrayList<String> envVars;
	ArrayList<String> sysVars;
	ArrayList<String> vars;
	ArrayList<String> properties;
	ArrayList<String> assumptions;
	ArrayList<String> guarantees;
	
	//SMV and SPC files
	String environmentSpec="";
	String systemSpec="";
	
	public JTLVSpecification(String smvFileName){
		//smvFile=smvFilePath;
		smvFile=smvFileName+"Test.smv";
		spcFile=smvFileName+"Test.spc";
		strategyFile=smvFileName+"Test.aut";
		
		FileOps.copy(smvFileName+".smv",smvFile);
		FileOps.copy(smvFileName+".spc",spcFile);
		readSpecification();
	}
	
	public void readSpecification(){
		readSMVFile();
		readSPCFile();
	}
	
	public void readSMVFile(){
		String sCurrentLine;
		BufferedReader br;
		envVars=new ArrayList<String>();
		sysVars=new ArrayList<String>();
		vars=new ArrayList<String>();
		StringTokenizer st;
		
		try{
			br = new BufferedReader(new FileReader(smvFile));
			while(!(readNextLine(br)).contains("MODULE env"));
			//VAR
			if(!readNextLine(br).startsWith("VAR")){
				throw new Exception("Wrong SMV file format");
			}
			
			while(!(sCurrentLine=readNextLine(br)).startsWith("MODULE")){
				if(sCurrentLine.contains(";")){
					st=new StringTokenizer(sCurrentLine," 	:;");
					String variable=st.nextToken();
					vars.add(variable);
					envVars.add(variable);
				}
			}
			
			if(!sCurrentLine.startsWith("MODULE sys")){
				throw new Exception("Wrong SMV file format");
			}
			
			//VAR
			if(!readNextLine(br).startsWith("VAR")){
				throw new Exception("Wrong SMV file format");
			}
			
			while((sCurrentLine=readNextLine(br)) != null ){
				if(sCurrentLine.contains(";")){
					st=new StringTokenizer(sCurrentLine," 	:;");
					String variable=st.nextToken();
					vars.add(variable);
					sysVars.add(variable);
				}
			}
			br.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		
	}
	
	public String readNextLine(BufferedReader br){
		String sCurrentLine="";
		try{
			sCurrentLine=br.readLine();
			if(sCurrentLine == null) return null;
			sCurrentLine=sCurrentLine.trim();
			//System.out.println(sCurrentLine);
			while(sCurrentLine.startsWith("--") || sCurrentLine.equals("")){
				sCurrentLine=br.readLine().trim();
			}
			if(sCurrentLine.contains("--")){
				sCurrentLine=sCurrentLine.substring(0,sCurrentLine.indexOf("--"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		//System.out.println(sCurrentLine);
		return sCurrentLine;
	}
	
	//Assumes each property is in one line
	public void readSPCFile(){
		String sCurrentLine;
		BufferedReader br;
		properties=new ArrayList<String>();
		assumptions=new ArrayList<String>();
		guarantees= new ArrayList<String>();
		String property="";
		
		try{
			br = new BufferedReader(new FileReader(spcFile));
			while(!(sCurrentLine=readNextLine(br)).contains("LTLSPEC"));
			environmentSpec+=sCurrentLine+"\n";
			//reading environment assumptions
			while(!(sCurrentLine=readNextLine(br)).contains("LTLSPEC")){
				if(sCurrentLine.equals(";"))	environmentSpec+=sCurrentLine+"\n";
				else environmentSpec+="\t"+sCurrentLine+"\n";
				//checking if it's the last assumption
				if(sCurrentLine.endsWith("&")){
					sCurrentLine=sCurrentLine.substring(0,sCurrentLine.lastIndexOf('&')-1);
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					assumptions.add(property);
				}else if(sCurrentLine.endsWith(";") && sCurrentLine.length()!=1){
					sCurrentLine=sCurrentLine.substring(0,sCurrentLine.lastIndexOf(';')).trim();
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					assumptions.add(property);
				}else if(!sCurrentLine.endsWith(";")){
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					assumptions.add(property);
				}
			}
			
			//Guarantees
			systemSpec+=sCurrentLine+"\n";
			//reading system guarantees
			while((sCurrentLine=readNextLine(br)) != null){
				systemSpec+="\t"+sCurrentLine+"\n";
				//checking if it's the last assumption
				if(sCurrentLine.endsWith("&")){
					sCurrentLine=sCurrentLine.substring(0,sCurrentLine.lastIndexOf('&')-1);
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					guarantees.add(property);
				}else if(sCurrentLine.endsWith(";") && sCurrentLine.length()!=1){
					sCurrentLine=sCurrentLine.substring(0,sCurrentLine.lastIndexOf(';')).trim();
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					guarantees.add(property);
				}else if(!sCurrentLine.endsWith(";")){
					property=propertyProcess(sCurrentLine);
					properties.add(property);
					guarantees.add(property);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		systemSpec=systemSpec.substring(0,systemSpec.lastIndexOf(";")).trim();
	}
	
	public String propertyProcess(String property){
		String result="";
		if(property.startsWith("[]<>")){
			result=property.replace("[]<>", "G(F")+")";
		}else if(property.startsWith("[]")){
			result=property.replace("[]", "G");
		}else{
			result=property;
		}
		result=result.replace("s.", "");
		result=result.replace("e.", "");
		return result;
	}
	
	public void generateGR1Spec(ArrayList<String> guarantees){
		if(guarantees == null || guarantees.size()==0){
			FileOps.write(environmentSpec+"\n"+systemSpec+"\n;", spcFile);
			return;
		}
		String spec=environmentSpec+"\n";
		spec+=systemSpec+" & \n";
		
		for(String g : guarantees){
			if(g.startsWith("G(F(")){
				String guarantee=g.replace("G(F", "[]<>");
				guarantee=guarantee.substring(0,guarantee.lastIndexOf(")"));
				spec+="\t"+addOwner(guarantee)+ " & \n";
			}else{//starts with G(
				g=g.replace(" X ", " next");
				g=g.replace("G", "[]");
				spec+="\t"+addOwner(g)+" & \n";
			}
		}
		spec=spec.substring(0,spec.lastIndexOf("&")).trim();
		spec+="\n;";
		FileOps.write(spec, spcFile);
	}
	
	// G(F(pf0 & d)) -> G(F(e.d & s.pf0)) 
	public String addOwner(String property){
		//System.out.println("property is: "+property);
		for(String envV : envVars){
			//System.out.println("envVar is: "+envV);
			property=property.replace(envV, "e."+envV);
		}
		
		for(String sysV : sysVars){
			//System.out.println("sysVar is: "+sysV);
			property=property.replace(sysV, "s."+sysV);
		}
		//System.out.println("property afte process is: "+property);
		return property;
	}

	public static void main(String[] args) {
		
		JTLVSpecification j = new JTLVSpecification("CaseStudies/simple_dist_rob1");
		
		System.out.println("environment variables");
		for(String e : j.envVars){
			System.out.println(e);
		}
		
		System.out.println("\nsystem variables");
		for(String s : j.sysVars){
			System.out.println(s);
		}
		
		System.out.println("\nproperties");
		
		for(String s : j.properties){
			System.out.println(s);
		}
		
		System.out.println("\n\n assumptions");
		for(String s : j.assumptions){
			System.out.println(s);
		}
		
		
		System.out.println("\n\n guarantees");
		for(String s : j.guarantees){
			System.out.println(s);
		}
	}

	@Override
	public ArrayList<String> getGuarantees() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addGuarantee(String guarantee) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addGuarantees(ArrayList<String> guarantees) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<String> getAssumptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAssumption(String assumption) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAssumptions(ArrayList<String> assumptions) {
		// TODO Auto-generated method stub
		
	}

}
