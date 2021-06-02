/*
    This file implements the IMCF data importing updating methods.
    Copyright (C) 2021  Antonis Vasileiou
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.html.
*/  

package iotprojectpackage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class importData {

	private ArrayList<String[]> IotData = new ArrayList<String[]>();
	private ArrayList<String[]> IFTTrules = new ArrayList<String[]>();
	private ArrayList<String[]> MRTrules = new ArrayList<String[]>();
	private String pathIFTTrules;
	private String pathMRTrules;
	private String pathIoTdata;

	public importData(String mrtpath, String IFTTpath, String pathData) {
		pathIFTTrules = IFTTpath;
		pathMRTrules = mrtpath;
		pathIoTdata = pathData;
	}

	public void get_updated_MRTrules() {

		String urlString = "http://10.16.30.73/api/get-metarules";

		try {

			StringBuilder result = new StringBuilder();
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			int responsecode = conn.getResponseCode();
			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					result.append(line);
				}

				br.close();

				result.deleteCharAt(result.length() - 1);
				result.deleteCharAt(0);
				String[] stemp = result.toString().split("}");
				StringBuilder stringbld = new StringBuilder();

				for (String s : stemp) {
					s = s.replace("{", "");
					s = s.replace("\"", "");
					String stemp2[] = s.split(",");
					for (String s1 : stemp2) {
						// System.out.println(s1);
						String stemp3[] = s1.split(":");
						if (stemp3[0].equals("time")) {
							if (stemp3[1].equals("kWh")) {
								stringbld.append("kWh");
								stringbld.append("|");
							} else {
								stringbld.append(stemp3[1]);
								stringbld.append(":");
								stringbld.append(stemp3[2]);
								stringbld.append(":");
								stringbld.append(stemp3[3]);
								stringbld.append("|");
							}
						} else if (stemp3[0].equals("action")) {
							stringbld.append(stemp3[1]);
							stringbld.append("|");
						} else if (stemp3[0].equals("value")) {
							stringbld.append(stemp3[1]);
							stringbld.append("|");
							stringbld.append(stemp3[1]);
							stringbld.append("\n");
						}
					}
					// System.out.println(s);

				}
				// System.out.println(stringbld);
				FileWriter myWriter = new FileWriter(pathMRTrules);
				myWriter.write("TIME| TEMPER./LIGHT    |    FROM  |  TO     |    \n");
				myWriter.write(
						"------------------------------------------------------------------------------------------------------------------\n");

				myWriter.write(stringbld.toString());
				myWriter.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	public void get_updated_MRTrules_GP() {
		// call api for data for location and date of today
		// put in dame structure as data I already have
		// put date and T105 and the value of temp the other put whatever
		// First store in text file and then import them like before
		// String API_KEY = "c5c61330eca67b966249ef80724111a6";
		// String LOCATION = "Limassol,CY";
		String urlString = "http://10.16.30.73/imcfgp/api/get-metarules";

		try {

			StringBuilder result = new StringBuilder();
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			int responsecode = conn.getResponseCode();
			if (responsecode != 200) {
				throw new RuntimeException("HttpResponseCode: " + responsecode);
			} else {
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					result.append(line);
				}

				br.close();

				result.deleteCharAt(result.length() - 1);
				result.deleteCharAt(0);
				String[] stemp = result.toString().split("}");
				StringBuilder stringbld = new StringBuilder();

				for (String s : stemp) {
					s = s.replace("{", "");
					s = s.replace("\"", "");
					String stemp2[] = s.split(",");
					for (String s1 : stemp2) {
						// System.out.println(s1);
						String stemp3[] = s1.split(":");
						if (stemp3[0].equals("time")) {
							if (stemp3[1].equals("kWh")) {
								stringbld.append("kWh");
								stringbld.append("|");
							} else {
								stringbld.append(stemp3[1]);
								stringbld.append(":");
								stringbld.append(stemp3[2]);
								stringbld.append(":");
								stringbld.append(stemp3[3]);
								stringbld.append("|");
							}
						} else if (stemp3[0].equals("action")) {
							stringbld.append(stemp3[1]);
							stringbld.append("|");
						} else if (stemp3[0].equals("value")) {
							stringbld.append(stemp3[1]);
							stringbld.append("|");
							stringbld.append(stemp3[1]);
							stringbld.append("\n");
						}
					}
					// System.out.println(s);

				}
				// System.out.println(stringbld);
				FileWriter myWriter = new FileWriter(pathMRTrules);
				myWriter.write("TIME| TEMPER./LIGHT    |    FROM  |  TO     |    \n");
				myWriter.write(
						"------------------------------------------------------------------------------------------------------------------\n");

				myWriter.write(stringbld.toString());
				myWriter.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	public void import_IoT_data() {

		int counter = 0;
		String line;

		// Read the file and display it line by line.
		File file = new File(pathIoTdata);

		try {
			System.out.println("Importing Data:\t" + file.getName());
			long startime = System.nanoTime();
			Scanner inputFile = new Scanner(file);
			// loop through every line

			while (inputFile.hasNextLine()) {
				line = inputFile.nextLine();
				// String[] col = line.split("|");

				String temp[] = new String[7];
				if (counter > 1) {
					String[] col = line.split("\\|");
					temp[0] = col[0].trim();
					temp[1] = col[1].trim();
					temp[2] = col[2].trim();
					temp[3] = col[3].trim();
					temp[4] = col[4].trim();
					temp[5] = col[5].trim();
					temp[6] = col[6].trim();

					IotData.add(temp); // fills a list with all the input IoT data
				}

				counter++;
			} // end of while loop

			inputFile.close();

			long finishtime = System.nanoTime() - startime;
			System.out.println("Import Completed!");
			System.out.println("Import Dataset Time:\t" + finishtime / 1000000 + "ms");
		} catch (Exception e) {
			System.out.println("Error with Import of IoT data.");
			System.out.println(e);
		}

		System.out.println("Number of records imported: " + IotData.size());

	}

	public void import_IFTTT_rules() {
		int counter = 0;
		String line;

		// Read the file and display it line by line.
		File file = new File(pathIFTTrules);

		System.out.println("Importing IFTT Rules:\t" + file.getName());
		try {
			long startime = System.nanoTime();
			Scanner inputFile = new Scanner(file);

			// loop through every line
			while (inputFile.hasNextLine()) {
				line = inputFile.nextLine();
				String[] col = line.split("\\|");

				String temp[] = new String[8];
				if (counter > 1) {
					temp[0] = col[0].trim();
					temp[1] = col[1].trim();
					temp[2] = col[5].trim();
					temp[3] = col[3].trim();
					temp[4] = col[6].trim();
					temp[5] = col[7].trim();
					temp[6] = col[8].trim();
					temp[7] = col[2].trim();
					IFTTrules.add(temp);
				} // end of inner while loop
				counter++;
			} // end of outer while loop
			long finishtime = System.nanoTime() - startime;
			System.out.println("Import Completed!");
			System.out.println("Import IFTT rules Time:\t" + finishtime / 1000000 + "ms");
			inputFile.close();
			System.out.printf("There were %d lines.\n", counter);
		} catch (Exception e) {
			System.out.println("Error with Import of IFTT rules.");
			System.out.println(e);
		}
		// return IFTTrules;
	}

	public void import_MRT_rules() {
		int counter = 0;
		String line;

		// Read the file and display it line by line.

		File file = new File(pathMRTrules);
		System.out.println("Importing MRT Rules:\t" + file.getName());
		try {
			long startime = System.nanoTime();
			Scanner inputFile = new Scanner(file);

			// loop through every line
			while (inputFile.hasNextLine()) {
				line = inputFile.nextLine();
				String[] col = line.split("\\|");

				String temp[] = new String[4];
				if (counter > 1) {

					temp[0] = col[0].trim();
					temp[1] = col[2].trim();
					temp[2] = col[3].trim();
					temp[3] = col[1].trim();
					MRTrules.add(temp);

				} // end of if
				counter++;

			} // end of while loop

			

			long finishtime = System.nanoTime() - startime;
			System.out.println("Import Completed!");
			System.out.println("Import MRT rules Time:\t" + finishtime / 1000000 + "ms");
			inputFile.close();
			System.out.printf("There were %d lines.\n", counter);
		} catch (Exception e) {
			System.out.println("Error with Import of MRT rules.");
			System.out.println(e);
		}

	}// END OF - Meta-rule Config SCENARIO 2

	public ArrayList<String[]> getIotData() {
		return IotData;
	}

	public ArrayList<String[]> getIFTTTrules() {
		return IFTTrules;
	}

	public ArrayList<String[]> getMRTrules() {
		return MRTrules;
	}

}
