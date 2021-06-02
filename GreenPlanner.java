/*
    This file implements the Green Planner algorithm.
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
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GreenPlanner {

	private String errorLine;// global

	private SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat getHour = new SimpleDateFormat("HH");
	private Date dtTemp;
	private double contiguous_ms = 0;
	private String monthlyKWH_listAssignment = "original"; // to help identify allowed kwh ArrayList->original,
															// 1%,5%,10% or 20%
	private ArrayList<String[]> copiediotdata;
	private ArrayList<String[]> GreenPlannerData;
	private ArrayList<String[]> copiediotdata3;
	private ArrayList<String[]> MRTrules;
	private boolean solutionsFound;
	private ArrayList<String[]> recursionResults = new ArrayList<String[]>();
	private double public_ms = 0;
	private int recursiveIterator = 0;
	private String urlString = "http://10.16.30.73/imcfgp/api/update-results";
	private double totalaverageerror = 0.0;
	private double budgeterror = 0.0;
	private double convinienceerror = 0.0;
	private double deviation = 0.0;
	private double totalkwh = 0.0;
	private double total_daily_ms_CO2 = 0;
	private double kwh_with_CO2 = 0;
	private double kwh_without_CO2 = 0;
	private double CO2_kg_emissions = 0;

	private String listRanges[][];
	private String kwhpermonth = "";

	public GreenPlanner() {
		solutionsFound = false;
	}

	public void ArrayArrayList_fromString_toStringArray(ArrayList<String> ArrayList_from,
			ArrayList<String[]> ArrayList_to) {

		for (int i = 0; i < ArrayList_from.size(); i++) {
			String temp[] = ArrayList_from.get(i).split("\\|");
			ArrayList_to.add(temp);
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
	
	// converts string to double
	public double toDouble(String str) {

		return Double.parseDouble(str);

	}

	// converts string to double
	public int toInt(String str) {

		return Integer.parseInt(str);

	}
	
	
	// returns hour from record datetime
	public int getHour(String date) {

		return toInt(date.substring(11, 13));

	}
	

	// returns month from record datetime
	public int getMonth(String date) {

		return toInt(date.substring(5, 7));

	}
	
	// returns month from record datetime
	public int getDay(String date) {

		return toInt(date.substring(8, 10));

	}

	// returns the hour the rule starts
	public int ruleHourFrom(String ruletime) {

		return toInt(ruletime.substring(0, 2));

	}

	// returns the hour the rule ends
	public int ruleHourTo(String ruletime) {

		return toInt(ruletime.substring(6, 8));

	}	
	

	public void postData() {
		try {

			String urlParameters = "gp_kWh=" + totalkwh + "&gp_totalError=" + totalaverageerror
					+ "&gp_convenienceError=" + convinienceerror + "&gp_budgetError=" + budgeterror
					+ "&gp_standardDeviation=" + deviation + "&gp_kwh_per_month=" + kwhpermonth + "&gp_kwh_with_co2="
					+ kwh_with_CO2 + "&gp_kwh_without_co2=" + kwh_without_CO2 + "&gp_co2_emissions_kg="
					+ CO2_kg_emissions;
			byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;

			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);

			conn.connect();

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.write(postData);
			conn.getResponseCode();

		} catch (Exception e) {
			for (StackTraceElement s : e.getStackTrace())
				System.out.println(s);
		}
	}

	// START OF CALCULATE KWH METHOD - number of modifications (GREEN PLANNER)
	public double calculateKWH5(ArrayList<String[]> sRecordsDaily) {

		ArrayList<String[]> copiediotdatakwh = new ArrayList<String[]>(sRecordsDaily);
		ArrayList<Integer> removedMetaRules = new ArrayList<Integer>();

		Random rand = new Random();
		boolean startALLzeros = false;
		boolean startALLrandom = false;
		int numofloops = 3;
		boolean randomRule[] = new boolean[listRanges.length];
		int numrules = listRanges.length;
		int random;
		if (removedMetaRules.size() == 0) {
			if (startALLzeros == true) {
				for (int d = 0; d < randomRule.length; d++) {
					randomRule[d] = false;
					removedMetaRules.add(d);
				}
			} else if (startALLrandom == true) {
				for (int d = 0; d < randomRule.length; d++) {
					// generates random number from 0 to 1
					random = rand.nextInt(2);
					if (random == 0) {
						randomRule[d] = false;
						removedMetaRules.add(d);
					} else {
						randomRule[d] = true;
					}

				}

			} else {
				for (int d = 0; d < randomRule.length; d++) {
					randomRule[d] = true;
				}
			}

			// for predifined times add or remove rules
			for (int i = 0; i < numofloops; i++) {
				random = rand.nextInt(numrules);

				if (randomRule[random]) {
					randomRule[random] = false;
					removedMetaRules.add(random);
				} else {
					randomRule[random] = true;
					removedMetaRules.remove(Integer.valueOf(random));
				}

			}
		}

		if (solutionsFound == false) {

			for (int i = 0; i < numofloops; i++) {
				random = rand.nextInt(numrules);

				if (randomRule[random]) {
					randomRule[random] = false;
					removedMetaRules.add(random);
				} else {
					randomRule[random] = true;
					removedMetaRules.remove(Integer.valueOf(random));
				}

			}
		} else if (solutionsFound == true) {
			for (int d = 0; d < randomRule.length; d++) {
				randomRule[d] = true;
			}

			for (int i = 0; i < removedMetaRules.size(); i++) {
				randomRule[removedMetaRules.get(i)] = false;

			}
		}

		int IFTTTconstraintsApplied = 0;
		double milliWatts = 0;
		Date dt1 = dtTemp;
		Date dt2;
		double span;
		double ms = 0;
		double localContiguous_ms = contiguous_ms;
		double daily_ms_CO2 = 0;
		ArrayList<Double> monthlyKWH = new ArrayList<Double>();
		Calendar cdt1 = GregorianCalendar.getInstance(); // creates a new calendar instance
		Calendar cdt2 = GregorianCalendar.getInstance();
		

		if (monthlyKWH_listAssignment.equals("original")) {
			monthlyKWH = new ArrayList<Double>();
			// [] d= new []{ 25.02, 17.06, 7.96, 4.55, 5.69, 6.82, 7.96, 10.23,
			// 6.82, 5.69, 6.82, 13.65 };
			double[] d = new double[] { 1091.81, 744.42, 347.39, 198.51, 248.14, 297.77, 347.39, 446.65, 297.77, 248.14, 297.77, 595.53 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("1")) {
			monthlyKWH = new ArrayList<>();
			double[] d = new double[] { 25.02, 17.06, 7.96, 4.55, 5.69, 6.82, 7.96, 10.23, 6.82, 5.69, 6.82, 13.65 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("5")) {
			monthlyKWH = new ArrayList<>();

			double[] d = new double[] { 23.77, 16.21, 7.56, 4.32, 5.40, 6.48, 7.56, 9.72, 6.48, 5.40, 6.48, 12.96 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("10")) {
			monthlyKWH = new ArrayList<>();

			double[] d = new double[] { 22.52, 15.35, 7.17, 4.09, 5.12, 6.14, 7.17, 9.21, 6.14, 5.12, 6.14, 12.28 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("20")) {
			monthlyKWH = new ArrayList<>();

			double[] d = new double[] { 20.01, 13.65, 6.37, 3.64, 4.55, 5.46, 6.37, 8.19, 5.46, 4.55, 5.46, 10.92 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("30")) {
			monthlyKWH = new ArrayList<>();
			double[] d = new double[] { 17.51, 11.94, 5.57, 3.18, 3.98, 4.78, 5.57, 7.16, 4.78, 3.98, 4.78, 9.55 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("40")) {
			monthlyKWH = new ArrayList<>();
			double[] d = new double[] { 15.01, 10.24, 4.78, 2.73, 3.41, 4.09, 4.78, 6.14, 4.09, 3.41, 4.09, 8.19 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		}

		try {

			ArrayList<String> listRules = new ArrayList<String>();

			for (int d = 0; d < randomRule.length; d++) {
				if (randomRule[d]) {
					// temp
					if (listRanges[d][3].equals("TEMPER.")) {
						listRules.add(listRanges[d][0] + "|" + "+" + listRanges[d][1] + "|" + "---" + "|"
								+ listRanges[d][1] + "|" + "---" + "|" + "---" + "|" + "---" + "|" + "SET TEMPERATURE");
					}
					// light
					if (listRanges[d][3].equals("LIGHT")) {
						listRules.add(listRanges[d][0] + "|" + "---" + "|" + "+" + listRanges[d][1] + "|"
								+ listRanges[d][1] + "|" + "---" + "|" + "---" + "|" + "---" + "|" + "SET LIGHT");
					}
					IFTTTconstraintsApplied++;

				}
			}

			for (int i = 0; i < copiediotdatakwh.size(); i++) {

				for (int k = 0; k < listRules.size(); k++) {
					errorLine = String.join("|", copiediotdatakwh.get(i));

					String[] copiediotdataSplitted = copiediotdatakwh.get(i);
					String[] rulesSplitted = listRules.get(k).split("\\|");

					if (copiediotdataSplitted[2].startsWith("T")) // gets T for temperatures
					{
						// checks time and temperature and if season,wether,door is empty
						if (!rulesSplitted[1].equals("---") && rulesSplitted[4].equals("---")
								&& rulesSplitted[5].equals("---") && rulesSplitted[6].equals("---")) {

							if (getHour(copiediotdataSplitted[0]) >= 
									ruleHourFrom(rulesSplitted[0])
									&& getHour(copiediotdataSplitted[0]) <= 
											ruleHourTo(rulesSplitted[0])) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms += span;
								dt1 = dt2;
								cdt2 = GregorianCalendar.getInstance(); // creates a new calendar instance
								cdt2.setTime(dt2);   // assigns calendar to given date
								
								if ( cdt2.get(Calendar.HOUR) >= 8 && cdt2.get(Calendar.HOUR) <= 15
										&& copiediotdataSplitted[4].equals("sky is clear")) {
									daily_ms_CO2 += span;
								}

								localContiguous_ms += span;
								double secondsTemp = localContiguous_ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 1100;
								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								milliWatts = milliWatts + 320;
								copiediotdataSplitted[3] = rulesSplitted[3];
								copiediotdatakwh.set(i, copiediotdataSplitted);
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
							if (getHour(copiediotdataSplitted[0]) >= 
									ruleHourFrom(rulesSplitted[0])
									&& getHour(copiediotdataSplitted[0]) <= 
											ruleHourTo(rulesSplitted[0])) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms += span;
								dt1 = dt2;
								cdt2 = GregorianCalendar.getInstance(); // creates a new calendar instance
								cdt2.setTime(dt2);   // assigns calendar to given date
								if (cdt2.get(Calendar.HOUR) >= 8 && cdt2.get(Calendar.HOUR) <= 15
										&& copiediotdataSplitted[4].equals("sky is clear")) {
									daily_ms_CO2 += span;
								}

								localContiguous_ms += span;
								double secondsTemp = localContiguous_ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 1100;
								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								copiediotdataSplitted[3] = rulesSplitted[3];
								// copiediotdata2.add(String.Join("|", copiediotdataSplitted));
								copiediotdatakwh.set(i, copiediotdataSplitted);
								break;
							}

						} // end of : //checks LIGHT and if weather is empty

					}

					if (k == listRules.size() - 1) {
						dt1 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
					}

				}

				String[] copiediotdataSplittedAfterIFTTT = copiediotdatakwh.get(i);
				if (copiediotdataSplittedAfterIFTTT.length < 8) {
					copiediotdatakwh.set(i, addX(copiediotdataSplittedAfterIFTTT, "00"));

				}
			}
		} catch (Exception ex) {
			System.out.println("First B: " + errorLine + " => Exception: " + ex);
		}

		// calculations for TIME(hours), watts and kWh
		double seconds = ms / 1000;
		double hours = seconds / 3600;

		double kWh = ((hours * 320) / 1000) * 1100;

		double seconds_CO2 = daily_ms_CO2 / 1000;
		double hours_CO2 = seconds_CO2 / 3600;
		double kWh_CO2 = ((hours_CO2 * 320) / 1000) * 1100;

		String[] copiediotdataSplitted_Month = copiediotdatakwh.get(0);
		Date Month;
		Calendar calendar;
		int currentMonth = 0;
		try {
			Month = simpleFormatter.parse(copiediotdataSplitted_Month[0].substring(0, 18));
			calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
			calendar.setTime(Month);   // assigns calendar to given date
			currentMonth = calendar.get(Calendar.MONTH);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		// months from calendar are indexed from 0 therefore currentMonth=0 is January
		double allowedMonthly_kWh = monthlyKWH.get(currentMonth) * 6.5;// 6.5, *7.9;

		// **********SECOND AND LAST
		// PHASE********************************************************************//
		double dTotalError = 0;
		double dLineError = 0;
		double dTotalError_kWh = 0;
		double maxError = 0;
		double minError = 100;
		boolean errorFound = false;
		int errorsize = 0;

		try {

			for (int i = 0; i < copiediotdatakwh.size(); i++) {

				dLineError = 0;// gets the total record line error in regards of all the ranges
				for (int k = 0; k < listRanges.length; k++) {
					errorLine = String.join("|", copiediotdatakwh.get(i));

					String[] copiediotdataSplitted2 = copiediotdatakwh.get(i);
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{

						// checks time and temperature and if season is empty
						if ((!rangesSplitted[0].equals("SUMMER") || !rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							if ((getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0]))
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toDouble(copiediotdataSplitted2[3]) >= 
											toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= 
											toDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0]))
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2]) ) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdatakwh.get(i));
									// its already on the ArrayList no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdatakwh.get(i));
									// its already on the ArrayList no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
					errorsize++;
					errorFound = false;
				}

			}
		} catch (Exception e) {
			System.out.println("Second B: " + errorLine);

		}

		copiediotdata3.clear();

		// *******************************end of second phase (calculating Error
		// phase)*********************

		if ((kWh > allowedMonthly_kWh) && (solutionsFound == false))// || (recursiveIterator<6))
		{

			if (removedMetaRules.size() == numrules) {
				removedMetaRules.clear();
			}

			calculateKWH5(sRecordsDaily);
			return public_ms;

		} else if (recursiveIterator < 15) {
			if (recursionResults.size() == 0)// check if result found first time
			{
				String temp[] = new String[4];
				String t = "";
				boolean first = true;
				for (int d = 0; d < removedMetaRules.size(); d++) {
					if (first) {
						t = removedMetaRules.get(d).toString();
						first = false;
					} else {
						t = t.concat("," + removedMetaRules.get(d));
					}
				}
				temp[0] = t;
				temp[1] = String.valueOf(kWh);
				temp[2] = String.valueOf(dTotalError);
				temp[3] = String.valueOf(kWh_CO2);
				recursionResults.add(temp);
			} else {// now check if new result is better than the previous and replace it
				String[] splitted = recursionResults.get(0);
				int recIterMinusOne = recursiveIterator - 1;
				double anneallingValue = recIterMinusOne - recIterMinusOne * (2 / 15);
				Random randAnnealing = new Random();
				int numRandAnnealing = randAnnealing.nextInt(100);

				if (kWh_CO2 >= toDouble(splitted[3])) {

					String temp[] = new String[4];
					String t = "";
					boolean first = true;
					for (int d = 0; d < removedMetaRules.size(); d++) {
						if (first) {
							t = removedMetaRules.get(d).toString();
							first = false;
						} else {
							t = t.concat("," + removedMetaRules.get(d));
						}
					}
					temp[0] = t;
					temp[1] = String.valueOf(kWh);
					temp[2] = String.valueOf(dTotalError);
					temp[3] = String.valueOf(kWh_CO2);
					recursionResults.set(0, temp);
				} else if (numRandAnnealing < anneallingValue) {
					String temp[] = new String[4];
					String t = "";
					boolean first = true;
					for (int d = 0; d < removedMetaRules.size(); d++) {
						if (first) {
							t = removedMetaRules.get(d).toString();
							first = false;
						} else {
							t = t.concat("," + removedMetaRules.get(d));
						}
					}
					temp[0] = t;
					temp[1] = String.valueOf(kWh);
					temp[2] = String.valueOf(dTotalError);
					temp[3] = String.valueOf(kWh_CO2);
					recursionResults.set(0, temp);
				}

			}

			recursiveIterator++;
			if (removedMetaRules.size() == numrules) {
				removedMetaRules.clear();
			}
			if (recursiveIterator == 15) {
				solutionsFound = true;
				removedMetaRules.clear();
				String[] rs = recursionResults.get(0);
				String[] splitted = rs[0].split(",");
				for (int i = 0; i < splitted.length; i++) {
					if (splitted[0].length() == 0)
						break;
					removedMetaRules.add(Integer.valueOf(splitted[i]));
				}

			}
			calculateKWH5(sRecordsDaily);
			return public_ms;
			// return 0;
		} else {
			contiguous_ms = localContiguous_ms;
			dtTemp = dt1;
			GreenPlannerData.addAll(copiediotdatakwh);
			removedMetaRules.clear();
			recursiveIterator = 0;
			recursionResults.clear();
			solutionsFound = false;

			public_ms = ms;
			total_daily_ms_CO2 += daily_ms_CO2;
			return public_ms;

		}

	}// END OF CALCULATE KWH METHOD - number of modifications

	// GREEN PLANNER (CALCULATEkwh5 - associated recursion function)
	public void greenplanner(ArrayList<String[]> data, ArrayList<String[]> MRTrulesOriginal, double formaliseMaxError) {

		copiediotdata = new ArrayList<String[]>(data); // clone original dataset ArrayList
		MRTrules = new ArrayList<String[]>(MRTrulesOriginal);
		listRanges = MRTrules.toArray(new String[0][]);
		try {
			dtTemp = simpleFormatter.parse("2013-10-22 02:32:34.409"); // temp date
		} catch (Exception e) {
			System.out.println(e);
		}
		contiguous_ms = 0;
		ArrayList<String[]> recordsDaily = new ArrayList<String[]>();
		double dayChange = 22;
		double total_ms = 0;
		GreenPlannerData = new ArrayList<String[]>();
		copiediotdata3 = new ArrayList<String[]>();

		int monthchange = 0;
		int yearchange = 1998;
		DecimalFormat df1 = new DecimalFormat("#.##");
		df1.setRoundingMode(RoundingMode.CEILING);
		String monthString;
		boolean first = true;

		try {
			for (int i = 0; i < copiediotdata.size(); i++) {
				String[] copiediotdataSplitHour = copiediotdata.get(i);

				double tempDAYChange = getDay(copiediotdataSplitHour[0]);
				int tempmonthchange = getMonth(copiediotdataSplitHour[0]);

				if (dayChange == tempDAYChange) {
					if (i == copiediotdata.size() - 1) // only for the last record something...
					{
						recordsDaily.add(copiediotdata.get(i));
						double hourly_ms = calculateKWH5(recordsDaily);

						total_ms = total_ms + hourly_ms;
						recordsDaily.clear();
					} else {
						recordsDaily.add(copiediotdata.get(i));
					}

				} else {
					dayChange = tempDAYChange;
					double hourly_ms = calculateKWH5(recordsDaily);

					total_ms = total_ms + hourly_ms;
					recordsDaily.clear();
					recordsDaily.add(copiediotdata.get(i));
				}

				if (monthchange != tempmonthchange) {
					monthchange = tempmonthchange;

					if (first) {
						double seconds1 = total_ms / 1000;
						double hours1 = seconds1 / 3600;
						double kWh1 = ((hours1 * 320) / 1000) * 1100;

						kwhpermonth = kwhpermonth.concat(df1.format(kWh1));
						first = false;
					} else {
						double seconds1 = total_ms / 1000;
						double hours1 = seconds1 / 3600;
						double kWh1 = ((hours1 * 320) / 1000) * 1100;

						kwhpermonth = kwhpermonth.concat("," + df1.format(kWh1));

					}
				}
				// END of calculation for the kwh per month

			}
		} catch (Exception ex) {

			System.out.println("First A: " + errorLine + " => Exception: " + ex + "\nAt line:\t");
			ex.printStackTrace();
		}

		copiediotdata.clear();
		copiediotdata.addAll(GreenPlannerData);
		GreenPlannerData.clear();

		double seconds = total_ms / 1000;
		double hours = seconds / 3600;
		double kWh = ((hours * 320) / 1000) * 1100;
		String label183 = "Total Kilowatt hour (kWh): " + Math.round(kWh) + " kWh";
		System.out.println(label183);
		totalkwh = Math.round(kWh);
		double seconds_CO2 = total_daily_ms_CO2 / 1000;
		double hours_CO2 = seconds_CO2 / 3600;
		double kWh_CO2 = ((hours_CO2 * 320) / 1000) * 1100;// 26
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		String label176 = "Kilowatt hour (kWh) with CO2 emmisions: " + df.format(kWh - kWh_CO2) + " kWh";
		System.out.println(label176);
		kwh_with_CO2 = toDouble(df.format(kWh - kWh_CO2));
		String label177 = "Kilowatt hour (kWh) without CO2 emmisions: " + df.format(kWh_CO2) + " kWh";
		kwh_without_CO2 = toDouble(df.format(kWh_CO2));
		System.out.println(label177);
		double KgCO2_co2 = 0.449;
		String label179 = "CO2 emissions : " + df.format((kWh - kWh_CO2) * KgCO2_co2) + " kg CO2e";
		CO2_kg_emissions = toDouble(df.format((kWh - kWh_CO2) * KgCO2_co2));
		System.out.println(label179);
		double dTotalError = 0;
		double dLineError = 0;
		double dTotalError_kWh = 0;

		// **********SECOND AND LAST PHASE*******************//

		double maxError = 0;
		double minError = 100;
		boolean errorFound = false;
		int errorsize = 0;

		try {

			for (int i = 0; i < copiediotdata.size(); i++) {

				dLineError = 0;// gets the total record line error in regards of all the ranges
				for (int k = 0; k < listRanges.length; k++) {
					errorLine = String.join("|", copiediotdata.get(i));

					String[] copiediotdataSplitted2 = copiediotdata.get(i);
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{

						// checks time and temperature and if season is empty
						if ((!rangesSplitted[0].equals("SUMMER") || !rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toDouble(copiediotdataSplitted2[3]) >= 
											toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= 
											toDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
									// its already on the ArrayList no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
									// its already on the ArrayList no need to add it twice
								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
							} else if (toDouble(copiediotdataSplitted2[3]) < 
									toDouble(rangesSplitted[1])) {

								dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
										- toDouble(copiediotdataSplitted2[3])) / 1);
								dLineError = dLineError + ((toDouble(rangesSplitted[1])
										- toDouble(copiediotdataSplitted2[3])) / 1);

								if (dLineError > maxError) {
									maxError = dLineError;
								} else if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							} else if (toDouble(copiediotdataSplitted2[3]) > 
									toDouble(rangesSplitted[2])) {

								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
										- toDouble(rangesSplitted[2])) / 1);

								if (dLineError > maxError) {
									maxError = dLineError;
								} else if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}

						} // end of :checks CLOUDY

						// checks DOOR OPEN-LIGHT
						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice
							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh = dTotalError_kWh + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
					errorsize++;
					errorFound = false;
				}

			}
		} catch (Exception e) {
			System.out.println("Second: " + errorLine);

		}

		// *******************************end of second phase *********************

		df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		dTotalError = dTotalError * 10;

		String label189 = "Total Error: " + df.format((dTotalError * 100) / formaliseMaxError);
		String label186 = "Max Error: " + df.format((maxError * 100) / formaliseMaxError) + "%";
		String label185 = "Min Error: " + df.format((minError * 100) / formaliseMaxError) + "%";
		System.out.println(label189);
		System.out.println(label186);
		System.out.println(label185);

		// calculate Total Average Error
		double averageErrorTotal = Math.round((dTotalError / copiediotdata.size()));
		String label182 = "Total Average Error: " + df.format((averageErrorTotal * 100) / formaliseMaxError) + "%";
		System.out.println(label182);
		totalaverageerror = toDouble(df.format((averageErrorTotal * 100) / formaliseMaxError));
		// calculate Budget Average Error
		double averageError_Budget = Math.round((dTotalError_kWh / copiediotdata.size()));
		String label180 = "Budget Average Error: " + df.format((averageError_Budget * 100) / formaliseMaxError) + "%";
		System.out.println(label180);
		budgeterror = toDouble(df.format((averageError_Budget * 100) / formaliseMaxError));
		// calculate Convenience Average Error
		double averageError_Concenience = Math.round(((dTotalError - dTotalError_kWh) / copiediotdata.size()));
		String label181 = "Convenience Average Error: "
				+ df.format((averageError_Concenience * 100) / formaliseMaxError) + "%";
		System.out.println(label181);
		convinienceerror = toDouble(df.format((averageError_Concenience * 100) / formaliseMaxError));

		copiediotdata3.clear();

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
					errorLine = String.join("|", copiediotdata.get(i));

					String[] copiediotdataSplitted2 = copiediotdata.get(i);
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{
						// checks time and temperature and if season is empty
						if ((!rangesSplitted[0].equals("SUMMER") || !rangesSplitted[0].equals("WINTER"))
								&& rangesSplitted[3].equals("TEMPER.")) {
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toDouble(copiediotdataSplitted2[3]) >= 
											toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= 
											toDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}

								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
									// its already on the ArrayList no need to add it twice

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}

									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
								if (toDouble(copiediotdataSplitted2[3]) >= 
										toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= 
												toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));
									// its already on the ArrayList no need to add it twice

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < 
											toDouble(rangesSplitted[1])) {
										dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);
										dLineError = dLineError + ((toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3])) / 1);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}

									} else if (toDouble(copiediotdataSplitted2[3]) > 
											toDouble(rangesSplitted[2])) {
										dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);
										dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) < 
												toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}

								} else if (getHour(copiediotdataSplitted2[0]) >= 
										ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= 
												ruleHourTo(rangesSplitted[0])
										&& toDouble(copiediotdataSplitted2[3]) > 
												toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}

								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice

							} else if (toDouble(copiediotdataSplitted2[3]) < 
									toDouble(rangesSplitted[1])) {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
										- toDouble(copiediotdataSplitted2[3])) / 1);
								dLineError = dLineError + ((toDouble(rangesSplitted[1])
										- toDouble(copiediotdataSplitted2[3])) / 1);

								if (dLineError > maxError) {
									maxError = dLineError;
								} else if (dLineError < minError) {
									minError = dLineError;
								}

							} else if (toDouble(copiediotdataSplitted2[3]) > 
									toDouble(rangesSplitted[2])) {
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
										- toDouble(rangesSplitted[2])) / 1);

								if (dLineError > maxError) {
									maxError = dLineError;
								} else if (dLineError < minError) {
									minError = dLineError;
								}

							}

						} // end of :checks CLOUDY

						// checks DOOR OPEN-LIGHT
						if (rangesSplitted[3].equals("DOOR OPEN-LIGHT")) {
							if (getHour(copiediotdataSplitted2[0]) >= 
									ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= 
											ruleHourTo(rangesSplitted[0])
									&& toInt(copiediotdataSplitted2[3]) >= 
											toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= 
											toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								// its already on the ArrayList no need to add it twice

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < 
										toDouble(rangesSplitted[1])) {
									dTotalError = dTotalError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);
									dLineError = dLineError + ((toDouble(rangesSplitted[1])
											- toDouble(copiediotdataSplitted2[3])) / 1);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}

								} else if (toDouble(copiediotdataSplitted2[3]) > 
										toDouble(rangesSplitted[2])) {
									dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);
									dLineError = dLineError + ((toDouble(copiediotdataSplitted2[3])
											- toDouble(rangesSplitted[2])) / 1);

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
							if (toDouble(copiediotdataSplitted2[7]) <= 
									toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError = dTotalError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);
								dLineError = dLineError + ((toDouble(copiediotdataSplitted2[7])
										- toDouble(rangesSplitted[2])) / 1);

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
		} catch (Exception e) {
			System.out.println("Second: " + errorLine);

		}

		double standardDeviation = Math.sqrt(meansSum / copiediotdata.size());
		String label184 = "Standard Deviation: " + df.format((standardDeviation * 100) / formaliseMaxError) + "%";
		deviation = toDouble(df.format((standardDeviation * 100) / formaliseMaxError));

		copiediotdata3.clear();

		postData();
	}// END OF Green PLANNER - number of modifications

}
