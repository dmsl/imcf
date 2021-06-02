/*
    This file implements the noRule/noIFTTT algorithm.
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

import java.io.FileWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class noIFTT {

	private static ArrayList<String[]> IotData = new ArrayList<String[]>();
	private ArrayList<String[]> IotData3 = new ArrayList<String[]>(); // for use in noIFTT function
	private ArrayList<String> TempIotData = new ArrayList<String>();
	private static ArrayList<String[]> IFTTrules;
	private static ArrayList<String[]> MRTrules;
	private String errorLine = "";
	private double formaliseMaxError = 0;
	private String pathNoIFTTresults;

	public noIFTT(ArrayList<String[]> ifttrules, ArrayList<String[]> mrtrules, ArrayList<String[]> data,
			String pathForResults) {
		IFTTrules = new ArrayList<String[]>(ifttrules);
		MRTrules = new ArrayList<String[]>(mrtrules);
		IotData = new ArrayList<String[]>(data);
		pathNoIFTTresults = pathForResults;
	}

	public void printLine(String[] array) {
		int length = array.length;
		int count = 0;

		for (String temp : array) {
			if (count == length - 1) {
				System.out.println(temp);
			} else {
				System.out.print(temp + "|");
			}
			count++;
		}
	}

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

	// returns formaliseMaxError value
	public double method() {

		String listRanges[][] = MRTrules.toArray(new String[0][]);

		double dTotalError = 0;
		double dTempError = 0;
		double dLineError = 0;

		long startime = System.nanoTime();
		double maxError = 0;
		double minError = 100;
		boolean errorFound = false;
		int errorCounter = 0;

		int line = 0;
		try {

			for (int i = 0; i < IotData.size(); i++) {
				// gets the total record line error in regards of all the ranges
				dLineError = 0;

				for (int k = 0; k < listRanges.length; k++) {
					String[] rangesSplitted = listRanges[k];
					errorLine = makeDataLine(IotData.get(i));

					String[] IotDataSplitted2 = IotData.get(i);
					// gets T for temperatures
					if (IotDataSplitted2[2].startsWith("T")) {

						// checks time and temperature and if season is empty

						if ((!(rangesSplitted[0].equals("SUMMER")) || !(rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {

							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
									&& Double.parseDouble(IotDataSplitted2[3]) <= Double
											.parseDouble(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								// its already on the list no need to add it twice
								break;

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dLineError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dLineError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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

						if (((rangesSplitted[0].equals("SUMMER")) || (rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							int iSeason = Integer.parseInt(IotDataSplitted2[0].substring(5, 7));

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(IotDataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									IotData3.add(IotData.get(i));
									// its already on the list no need to add it twice
									break;
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(IotDataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										dLineError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;

									} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										dLineError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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
							}
							// checks if WINTER
							else if ((iSeason == 12) || (iSeason == 1) || (iSeason == 2)) {
								if (Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(IotDataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									IotData3.add(IotData.get(i));
									// its already on the list no need to add it twice
									break;
								} else {

									if (Double.parseDouble(IotDataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										dTotalError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										dLineError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
										// break;
										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;

									} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										dTotalError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										dLineError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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

					}

					if (IotDataSplitted2[2].startsWith("LS")) // gets LS for light sensor
					{
						if (rangesSplitted[3].equals("LIGHT")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								// its already on the list no need to add it twice
								break;
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dLineError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
									// break;
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;

								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dLineError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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
						}

						if (rangesSplitted[3].equals("SUNNY")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								// its already on the list no need to add it twice
								break;
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dLineError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;

								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dLineError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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

						if (rangesSplitted[3].equals("CLOUDY")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								// its already on the list no need to add it twice
								break;
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dLineError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;

								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dLineError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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

						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dLineError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;

								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dLineError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									TempIotData.add(makeDataLine(IotData.get(i)) + "| Error: " + dLineError);
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

				}

				if (errorFound == true) {
					errorCounter = errorCounter + 1;
					errorFound = false;
				}

			}
		} catch (Exception ex) {
			System.out
					.println("error first(comparison data with meta-range rules): " + errorLine + ", error msg: " + ex);

		}

		// save formalized max error
		formaliseMaxError = maxError;

		dTotalError = dTotalError * 10;

		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		String lblError1 = "Total Error: " + Math.round((dTotalError) / formaliseMaxError);
		String label60 = "Max Error: " + df.format((maxError) * 100 / formaliseMaxError) + "%";
		String label59 = "Min Error: " + df.format((minError) * 100 / formaliseMaxError) + "%";

		Double averageErrorTotal = Double.parseDouble(df.format((dTotalError / IotData.size())));
		String label71 = "Total Average Error: " + df.format((averageErrorTotal * 100) / formaliseMaxError) + "%";

		TempIotData.clear();
		IotData3.clear();

		System.out.println(lblError1);
		System.out.println(label60);
		System.out.println(label59);
		// System.out.println(label58);
		System.out.println(label71);
		// ***********second run***************

		Double meansSum = 0.0;
		maxError = 0;
		minError = 100;
		dTotalError = 0;
		dTempError = 0;

		try {

			for (int i = 0; i < IotData.size(); i++) {
				for (int k = 0; k < listRanges.length; k++) {
					String[] rangesSplitted = listRanges[k];

					errorLine = makeDataLine(IotData.get(i));

					String[] IotDataSplitted2 = IotData.get(i);

					if (IotDataSplitted2[2].startsWith("T")) // gets T for temperatures
					{
						// checks time and temperature and if season is empty
						if ((!(rangesSplitted[0].equals("SUMMER")) || !(rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
									&& Double.parseDouble(IotDataSplitted2[3]) <= Double
											.parseDouble(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dTempError = Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									break;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dTempError = Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									break;
								}

							}
						}

						if (((rangesSplitted[0].equals("SUMMER")) || (rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							int iSeason = Integer.parseInt(IotDataSplitted2[0].substring(5, 7));

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(IotDataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									IotData3.add(IotData.get(i));
									break; // its already on the list no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(IotDataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										TempIotData.add(makeDataLine(IotData.get(i)));
										dTotalError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										dTempError = Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										break;
									} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										TempIotData.add(makeDataLine(IotData.get(i)));
										dTotalError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										dTempError = Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										break;
									}
								}
							}
							// checks if WINTER
							else if ((iSeason == 12) || (iSeason == 1) || (iSeason == 2)) {
								if (Double.parseDouble(IotDataSplitted2[3]) >= Double.parseDouble(rangesSplitted[1])
										&& Double.parseDouble(IotDataSplitted2[3]) <= Double
												.parseDouble(rangesSplitted[2])) {
									IotData3.add(IotData.get(i));
									break; // its already on the list no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (Double.parseDouble(IotDataSplitted2[3]) < Double
											.parseDouble(rangesSplitted[1])) {
										TempIotData.add(makeDataLine(IotData.get(i)));
										dTotalError += Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										dTempError = Double.parseDouble(rangesSplitted[1])
												- Double.parseDouble(IotDataSplitted2[3]);
										break;
									} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
											.parseDouble(rangesSplitted[2])) {
										TempIotData.add(makeDataLine(IotData.get(i)));
										dTotalError += Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										dTempError = Double.parseDouble(IotDataSplitted2[3])
												- Double.parseDouble(rangesSplitted[2]);
										break;
									}
								}
							}

						}

					}

					if (IotDataSplitted2[2].startsWith("LS")) // gets LS for light sensor
					{
						if (rangesSplitted[3].equals("LIGHT")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dTempError = Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									break;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dTempError = Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									break;
								}
							}
						}

						if (rangesSplitted[3].equals("SUNNY")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dTempError = Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									break;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dTempError = Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									break;
								}
							}
						}

						if (rangesSplitted[3].equals("CLOUDY")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dTempError = Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									break;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dTempError = Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									break;
								}
							}
						}

						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if ((Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) >= Double
									.parseDouble(rangesSplitted[0].substring(0, 2))
									&& Double.parseDouble(IotDataSplitted2[0].substring(11, 13)) <= Double
											.parseDouble(rangesSplitted[0].substring(6, 8)))
									&& Integer.parseInt(IotDataSplitted2[3]) >= Integer.parseInt(rangesSplitted[1])
									&& Integer.parseInt(IotDataSplitted2[3]) <= Integer.parseInt(rangesSplitted[2])) {
								IotData3.add(IotData.get(i));
								break; // its already on the list no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (Double.parseDouble(IotDataSplitted2[3]) < Double.parseDouble(rangesSplitted[1])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									dTempError = Double.parseDouble(rangesSplitted[1])
											- Double.parseDouble(IotDataSplitted2[3]);
									break;
								} else if (Double.parseDouble(IotDataSplitted2[3]) > Double
										.parseDouble(rangesSplitted[2])) {
									TempIotData.add(makeDataLine(IotData.get(i)));
									dTotalError += Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									dTempError = Double.parseDouble(IotDataSplitted2[3])
											- Double.parseDouble(rangesSplitted[2]);
									break;
								}
							}
						}

					}

				}

				meansSum = meansSum + Math.pow(dLineError - averageErrorTotal, 2);

			}
		} catch (Exception e) {
			System.out.println(
					"error second(comparison data with meta-range rules): " + errorLine + " , Error message: " + e);
			System.out.println("Number of line:\t" + e.getStackTrace()[2].getLineNumber());
		}

		double standardDeviation = Math.sqrt(meansSum / IotData.size());

		String label57 = "Standard Deviation: " + df.format((standardDeviation) * 100 / formaliseMaxError) + "%";
		System.out.println(label57);

		IotData3.clear(); // clears the entire list
		TempIotData.clear();

		try {

			FileWriter myWriter = new FileWriter(pathNoIFTTresults);
			myWriter.write(lblError1);
			myWriter.write("\n");
			myWriter.write(label60);
			myWriter.write("\n");

			myWriter.write(label59);
			myWriter.write("\n");

			// System.out.println(label58);
			myWriter.write(label71);
			myWriter.write("\n");

			myWriter.write(label57);
			myWriter.write("\n");
			myWriter.close();

		} catch (Exception e) {
			System.out.println("Unable to write data for noIFTT.");
		}

		return formaliseMaxError;
	} // end of comparison imported data and meta rule ranges

}
