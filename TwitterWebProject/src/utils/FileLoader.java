package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class FileLoader {
	public static String loadFile(String file) throws IOException {
		String result = "";
		Scanner sc;
		InputStream in;
		
		in = new FileInputStream(new File(file));
		
		sc = new Scanner(in);
		
		
		while (sc.hasNext()) 
			result += sc.nextLine() + "\n";
		
		sc.close();
		in.close();
		
		return result;
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(loadFile("mapReduceFunctions/mapDF.js"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
