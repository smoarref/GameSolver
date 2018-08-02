package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
//import java.nio.file.Files;


public class FileOps {
	public static void copy(String from, String to){
		InputStream inStream = null;
		OutputStream outStream = null;
	    try{
	    	File source =new File(from);
	    	File dest =new File(to);	 
	    	inStream = new FileInputStream(source);
	    	outStream = new FileOutputStream(dest);
	    	byte[] buffer = new byte[1024];
	 
	    	int length;
	    	//copy the file content in bytes 
	    	while ((length = inStream.read(buffer)) > 0){
	    	    outStream.write(buffer, 0, length);	 
	    	}
	 
	    	inStream.close();
	    	outStream.close();
	 
	    	//System.out.println("File is copied successful!");
	 
	   }catch(Exception e){
	    	e.printStackTrace();
	   }
	}
	
	public static void write(String str, String file){
		try{
			PrintWriter pw = new PrintWriter(file);
			pw.println(str);
			pw.flush();
			pw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//	public static void deleteFile(String path){
//		try{
//			File file = new File(path);
//			Files.deleteIfExists(file.toPath());
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}
	
	public static String readFile(String file){
		String sCurrentLine;
		String fileContent="";
		try{
			//read properties from property file
			BufferedReader br = new BufferedReader(new FileReader(file));
			while((sCurrentLine=br.readLine())!=null){
				fileContent+=sCurrentLine+"\n";
			}
			br.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return fileContent;
	}
}
