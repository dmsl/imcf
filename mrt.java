/*
    This file implements the MR algorithm.
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class mrt {

	private String errorLine;// global

	private SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private ArrayList<String[]> copiediotdata;
	private ArrayList<String[]> copiediotdata2 = new ArrayList<String[]>();
	private ArrayList<String[]> copiediotdata3 = new ArrayList<String[]>();
	// private ArrayList <String[]> TempIotData;
	// private ArrayList<String[]> listRules;

	public static String[] addX(String myarray[], String ele) {

		int n = myarray.length;
		String newArray[] = new String[n + 1];
		// copy original array into new array
		for (int i = 0; i < n; i++)
			newArray[i] = myarray[i];

		// add element to the new array
		newArray[n] = ele;

		return newArray;
	}

	// makes one line of data from an array of data
	public String makeDataLine(String[] array) {
		String s = "";
		String seperator = "|";
		int count = 0;
		int length = array.length;

		for (String temp : array) {
			if (count == length - 1) {
				s = s.concat(temp);
			} else {
				s = s.concat(temp);
				s = s.concat(seperator);
			}
			count++;
		}

		return s;
	}

	// MR function
	public void MR(ArrayList<String[]> data, ArrayList<String[]> MRTrules, Double formaliseMaxError) {
		copiediotdata = new ArrayList<String[]>(data); // clone original dataset ArrayList
		String listRanges[][] = MRTrules.toArray(new String[0][]);
		double dTotalError = 0;
		double dLineError = 0;
		double milliWatts = 0;
		Date dt1;
		Date dt2;
		double span;
		double ms = 0;
		double dTotalError_kWh = 0;

		try {
			dt1 = simpleFormatter.parse("2013-10-22 02:32:34.409"); // temp date

			ArrayList<String> listRules = new ArrayList<String>();
			listRules.add(listRanges[0][0] + "|" + "+" + listRanges[0][1] + "|" + "---" + "|" + listRanges[0][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET TEMPERATURE");
			listRules.add(listRanges[1][0] + "|" + "---" + "|" + "+" + listRanges[1][1] + "|" + listRanges[1][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET LIGHT");
			listRules.add(listRanges[2][0] + "|" + "+" + listRanges[2][1] + "|" + "---" + "|" + listRanges[2][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET TEMPERATURE");
			listRules.add(listRanges[3][0] + "|" + "---" + "|" + "+" + listRanges[3][1] + "|" + listRanges[3][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET LIGHT");
			listRules.add(listRanges[4][0] + "|" + "+" + listRanges[4][1] + "|" + "---" + "|" + listRanges[4][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET TEMPERATURE");
			listRules.add(listRanges[5][0] + "|" + "---" + "|" + "+" + listRanges[5][1] + "|" + listRanges[5][1] + "|"
					+ "---" + "|" + "---" + "|" + "---" + "|" + "SET LIGHT");

			for (int i = 0; i < copiediotdata.size(); i++) {
				for (int k = 0; k < listRules.size(); k++) {
					errorLine = makeDataLine(copiediotdata.get(i));

					String[] copiediotdataSplitted = copiediotdata.get(i);
					String[] rulesSplitted = listRules.get(k).split("\\|");

					if (copiediotdataSplitted[2].startsWith("T")) // gets T for temperatures
					{
						// checks time and temperature and if season,wether,door is empty
						if (!rulesSplitted[1].equals("---") && rulesSplitted[4].equals("---")
								&& rulesSplitted[5].equals("---") && rulesSplitted[6].equals("---")) {

							// checks time and temp value
							if ((Double.parseDouble(copiediotdataSplitted[0].substring(11, 13)) >= Double
									.parseDouble(rulesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted[0].substring(11, 13)) <= Double
											.parseDouble(rulesSplitted[0].substring(6, 8)))) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms = ms + span;
								dt1 = dt2;

								double secondsTemp = ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 24;

								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								milliWatts = milliWatts + 320;
								copiediotdataSplitted[3] = rulesSplitted[3];
								copiediotdata2.add(copiediotdataSplitted);
								copiediotdata.set(i, copiediotdataSplitted);
								break; // its already on the ArrayList no need to add it twice
							}

						}

					}

					if (copiediotdataSplitted[2].startsWith("LS")) // gets LS for light sensor
					{
						// checks LIGHT , and if weather,door is empty
						if (!rulesSplitted[2].equals("---") && rulesSplitted[5].equals("---")
								&& rulesSplitted[6].equals("---")) {

							// checks time and light value
							if ((Double.parseDouble(copiediotdataSplitted[0].substring(11, 13)) >= Double
									.parseDouble(rulesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted[0].substring(11, 13)) <= Double
											.parseDouble(rulesSplitted[0].substring(6, 8)))) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms = ms + span;
								dt1 = dt2;

								double secondsTemp = ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 24;
								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								copiediotdataSplitted[3] = rulesSplitted[3];
								copiediotdata2.add(copiediotdataSplitted);
								copiediotdata.set(i, copiediotdataSplitted);
								break;
							}

						} // end of : //checks LIGHT and if weather is empty

					}

					if (k == listRules.size() - 1) {
						dt1 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
					}

				}

				String[] copiediotdataSplittedAfterIFTTT = copiediotdata.get(i);
				if (copiediotdataSplittedAfterIFTTT.length < 8) {

					copiediotdata.set(i, addX(copiediotdataSplittedAfterIFTTT, "00"));
				}
			}
		} catch (Exception e) {
			System.out.println("Error at line:" + e.getStackTrace()[0].getLineNumber());
			System.out.println(e.getStackTrace()[0]);

			System.out.println("First: " + errorLine + " => Exception: " + e);
		}

		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		// calculations for TIME(hours), watts and kWh
		double seconds = ms / 1000;
		double hours = seconds / 3600;

		double watts = milliWatts;
		String label89 = "Watt (W): " + df.format(watts) + " W";
		System.out.println(label89);
		double kW = watts / 1000;
		double kWh = ((hours * 320) / 1000) * 24;

		String kilowatts = "Kilowatt hour (kWh): " + df.format(kWh) + " kWh";
		System.out.println(kilowatts);

		// **********SECOND AND LAST PHASE*******************//

		double maxError = 0;
		double minError = 100;
		Boolean errorFound = false;
		int errorCounter = 0;

		try {

			for (int i = 0; i < copiediotdata.size(); i++) {
				dLineError = 0;// gets the total record line error in regards of all the ranges
				for (int k = 0; k < listRanges.length; k++) {
					errorLine = makeDataLine(copiediotdata.get(i));

					String[] copiediotdataSplitted2 = copiediotdata.get(i);
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{

						// checks time and temperature and if season is empty
						if ((!rangesSplitted[0].equals("SUMMER") || !rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Double.parseDouble(copiediotdataSplitted2[3]) >= Double
											.parseDouble(rangesSplitted[1])
									&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
											.parseDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) < Double
												.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) > Double
												.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								}

							}
						}

						if ((rangesSplitted[0].equals("SUMMER") || rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							int iSeason = Integer.parseInt(copiediotdataSplitted2[0].substring(6, 8));

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (Double.parseDouble(copiediotdataSplitted2[3]) >= Double
										.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									}
								}
							}
							// checks if WINTER
							else if ((iSeason == 12) || (iSeason == 1) || (iSeason == 2)) {
								if (Double.parseDouble(copiediotdataSplitted2[3]) >= Double
										.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									}
								}
							}

						}

						if (rangesSplitted[0].equals("kWh")) {
							if (Double.parseDouble(copiediotdataSplitted2[7]) <= Double
									.parseDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}
						}

					}

					if (copiediotdataSplitted2[2].startsWith("LS")) // gets LS for light sensor
					{
						// checks LIGHT
						if (rangesSplitted[3].equals("LIGHT")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) < Double
												.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) > Double
												.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									// TempIotData.add(addX(copiediotdata.get(i),"| Error: " + dLineError));
									// break;
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								}
							}
						} // end of: checks LIGHT

						// checks SUNNY
						if (rangesSplitted[3].equals("SUNNY")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								}
							}
						} // end of :checks SUNNY

						// checks CLOUDY
						if (rangesSplitted[3].equals("CLOUDY")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								}
							}
						} // end of :checks CLOUDY

						// checks DOOR OPEN-LIGHT
						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// break; //its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								}
							}
						} // end of :checks DOOR OPEN-LIGHT

						if (rangesSplitted[0].equals("kWh")) {
							if (Double.parseDouble(copiediotdataSplitted2[7]) <= Double
									.parseDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								// TempIotData.add(addX(copiediotdata.get(i),"| Error: " + dLineError));
								// break;
								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}
						} // end of kWh check

					} // end of LS

				}

				if (errorFound == true) {
					errorCounter = errorCounter + 1;
					errorFound = false;
				}

			}
			copiediotdata3.clear();
		} catch (Exception e) {
			System.out.println("Second: " + errorLine);

		}

		// *******************************end of second phase *********************

		String totalerror = "Total Error: " + df.format((dTotalError * 100) / formaliseMaxError);
		System.out.println(totalerror);
		String maxerror = "Max Error: " + df.format((maxError * 100) / formaliseMaxError) + "%";
		System.out.println(maxerror);
		String minerror = "Min Error: " + df.format((minError * 100) / formaliseMaxError) + "%";
		System.out.println(minerror);
		Double averageError = dTotalError / errorCounter;
		String averageerror = "Average Error (diveded only with error counter): "
				+ df.format((averageError * 100) / formaliseMaxError) + "%";
		System.out.println(averageerror);
		// calculate Total Average Error
		Double averageErrorTotal = dTotalError / copiediotdata.size();
		String totalaverageerror = "Total Average Error: " + df.format((averageErrorTotal * 100) / formaliseMaxError)
				+ "%";
		System.out.println(totalaverageerror);
		// calculate Budget Average Error
		Double averageError_Budget = dTotalError_kWh / copiediotdata.size();
		String budgeterror = "Budget Average Error: " + df.format((averageError_Budget * 100) / formaliseMaxError)
				+ "%";
		System.out.println(budgeterror);
		// calculate Convenience Average Error
		Double averageError_Concenience = (dTotalError - dTotalError_kWh) / copiediotdata.size();
		String convenienceerror = "Convenience Average Error: "
				+ df.format((averageError_Concenience * 100) / formaliseMaxError) + "%";
		System.out.println(convenienceerror);

		// ************SECOND RUN*******************************
		double meansSum = 0;
		maxError = 0;
		minError = 100;
		dTotalError = 0;
		dLineError = 0;

		try {

			for (int i = 0; i < copiediotdata.size(); i++) {
				dLineError = 0;// gets the total record line error in regards of all the ranges
				for (int k = 0; k < listRanges.length; k++) {
					errorLine = makeDataLine(copiediotdata.get(i));

					String[] copiediotdataSplitted2 = copiediotdata.get(i);
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{
						// checks time and temperature and if season is empty
						if ((!rangesSplitted[0].equals("SUMMER") || !rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Double.parseDouble(copiediotdataSplitted2[3]) >= Double
											.parseDouble(rangesSplitted[1])
									&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
											.parseDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) < Double
												.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) > Double
												.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}

							}
						}

						if ((rangesSplitted[0].equals("SUMMER") || rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							int iSeason = Integer.parseInt(copiediotdataSplitted2[0].substring(6, 8));

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (Double.parseDouble(copiediotdataSplitted2[3]) >= Double
										.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									}
								}
							}
							// checks if WINTER
							else if ((iSeason == 12) || (iSeason == 1) || (iSeason == 2)) {
								if (Double.parseDouble(copiediotdataSplitted2[3]) >= Double
										.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(copiediotdataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2])) / 1);
										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									}
								}
							}

						}

						if (rangesSplitted[0].equals("kWh")) {
							if (Double.parseDouble(copiediotdataSplitted2[7]) <= Double
									.parseDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);

								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}
						}

					}

					if (copiediotdataSplitted2[2].startsWith("LS")) // gets LS for light sensor
					{
						// checks LIGHT
						if (rangesSplitted[3].equals("LIGHT")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) < Double
												.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
										.parseDouble(rangesSplitted[0].substring(0, 2))
										&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
												.parseDouble(rangesSplitted[0].substring(6, 8)))
										&& Double.parseDouble(copiediotdataSplitted2[3]) > Double
												.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}
							}
						} // end of: checks LIGHT

						// checks SUNNY
						if (rangesSplitted[3].equals("SUNNY")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}
							}
						} // end of :checks SUNNY

						// checks CLOUDY
						if (rangesSplitted[3].equals("CLOUDY")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// break; //its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}
							}
						} // end of :checks CLOUDY

						// checks DOOR OPEN-LIGHT
						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if ((Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(copiediotdataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(copiediotdataSplitted2[3]) >= Integer
											.parseInt(rangesSplitted[1])
									&& Integer.parseInt(copiediotdataSplitted2[3]) <= Integer
											.parseInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(copiediotdataSplitted2[3]) < Double
										.parseDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(copiediotdataSplitted2[3])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (Double.parseDouble(copiediotdataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2])) / 1);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}
							}
						} // end of :checks DOOR OPEN-LIGHT

						if (rangesSplitted[0].equals("kWh")) {
							if (Double.parseDouble(copiediotdataSplitted2[7]) <= Double
									.parseDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError = dTotalError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((Double.parseDouble(copiediotdataSplitted2[7])
										- Double.parseDouble(rangesSplitted[2])) / 1);
								// TempIotData.add(addX(copiediotdata.get(i),"| Error: " + dLineError));
								// break;
								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}
						} // end of kWh check

					} // end of LS

				}

				meansSum = meansSum + Math.pow(dLineError - averageErrorTotal, 2);

			}
			copiediotdata2.clear();
			copiediotdata3.clear();
		} catch (Exception e) {
			System.out.println("Second: " + errorLine);

		}

		double standardDeviation = Math.sqrt(meansSum / copiediotdata.size());
		String standarddeviation = "Standard Deviation: " + df.format((standardDeviation * 100) / formaliseMaxError)
				+ "%";
		System.out.println(standarddeviation);

	}// END OF MR function

}
