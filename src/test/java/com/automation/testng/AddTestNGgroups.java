package com.automation.testng;

import java.io.*;

public class AddTestNGgroups {

	@SuppressWarnings("resource")
	public static void main(String[] groups) {

		String inputFile = "src/test/resources/testng/testng.xml";
		String outputFile = "src/test/resources/testng/testng_groups.xml";
		File file = new File(outputFile);

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);

			BufferedReader br = new BufferedReader(new FileReader(inputFile));
			String line = br.readLine();
			bw.write(line + "\n");						

			while (line != null) {
				line = br.readLine();
				try {
					if (!line.contains("INCLUDEGROUP") ) {
						bw.write(line + "\n");											
					}					
					if (line.contains("<run>")) {												
						for (String group : groups) {
							bw.write("<include name=\""+group.replace(",", "").trim()+"\" />" + "\n");							
						}
					}					
					if (line.contains("</suite>")) {
						break;
					}
				} catch (Exception e) {
				}
			}
			bw.close();
			System.out.println("completed " + outputFile);
		} catch (IOException e) {
		}
	}
}