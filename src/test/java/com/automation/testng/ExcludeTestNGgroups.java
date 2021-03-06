package com.automation.testng;

import java.io.*;

public class ExcludeTestNGgroups {

	@SuppressWarnings("resource")
	public static void main(String[] groups) {

		String inputFile = "src/test/resources/testng/testng.xml";
		String outputFile = "src/test/resources/testng/testng_groups.xml";
		File ifile = new File(inputFile);
		File ofile = new File(outputFile);

		try {
			if (!ofile.exists()) {
				ofile.createNewFile();
			}
			FileWriter fw = new FileWriter(ofile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();
			bw.write(line + "\n");	
			int i = 0;

			while (line != null) {
				line = br.readLine();
				try {
					if (!line.contains("EXCLUDEGROUP") ) {
						bw.write(line + "\n");											
					}					
					if (line.contains("<run>") && i == 0) {
						for (String group : groups) {
							bw.write("<exclude name=\""+group.replace(",", "").trim()+"\" />" + "\n");							
						}
						i++;
					}					
					if (line.contains("</suite>")) {
						break;
					}
				} catch (Exception e) {
				}
			}
			bw.close();			
			ofile.renameTo(ifile);
			System.out.println("updated " + inputFile);
		} catch (IOException e) {
		}
	}
}
