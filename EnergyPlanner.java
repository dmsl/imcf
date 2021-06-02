/*
    This file implements the Energy Planner algorithm.
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
import java.util.Date;
import java.util.Random;
import java.text.DateFormatSymbols;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EnergyPlanner {

	private String errorLine;// global

	private SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Date dtTemp;
	private double contiguous_ms = 0;
	private String monthlyKWH_listAssignment = "original"; // to help identify allowed kwh list->original, 1%,5%,10% or
															// 20%
	private ArrayList<String[]> copiediotdata;
	private ArrayList<String[]> EnergyPlannerData;
	private ArrayList<String[]> copiediotdata3;
	private ArrayList<String[]> MRTrules;
	private String kwhpermonth = "";
	private Boolean solutionsFound;
	private ArrayList<String[]> recursionResults = new ArrayList<String[]>();
	private double public_ms = 0;
	private int recursiveIterator = 0;
	private String urlString = "http://10.16.30.73/api/update-results";
	private double totalaverageerror = 0.0;
	private double budgeterror = 0.0;
	private double convinienceerror = 0.0;
	private double deviation = 0.0;
	private double totalkwh = 0.0;
	private String listRanges[][];

	public EnergyPlanner() {
		solutionsFound = false;
	}

	public void ArrayList_fromString_toStringArray(ArrayList<String> list_from, ArrayList<String[]> list_to) {

		for (int i = 0; i < list_from.size(); i++) {
			String temp[] = list_from.get(i).split("\\|");
			list_to.add(temp);
		}

	}

	// call in line 1313
	public void updateOpenhabRules(String type, String state) {

		String geturl = "http://10.16.30.73/api/get-openhabrules";
		String posturl = "http://10.16.30.73/api/update-openhabrules";
		String postdata = "";
		try {

			StringBuilder result = new StringBuilder();
			URL url = new URL(geturl);
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

				String temp = result.toString();

				JsonArray array = (JsonArray) JsonParser.parseString(temp);

				int size = array.size();

				if (type.equals("Light")) {

					for (int i = 0; i < size; i++) {

						JsonObject obj = array.get(i).getAsJsonObject();

						if (obj.get("category").getAsString().equals("Temperature")) {
							String state_value = obj.get("state").getAsString().trim();
							String[] str = state_value.split("\\s+");
							obj.remove("state");
							obj.addProperty("state", str[0] + " \u00b0C"); // This is to handle the Celcious sign
						}

						if (obj.get("category").getAsString().equals("Light")) {

							obj.remove("stateONOFF");
							obj.addProperty("stateONOFF", state);

						}

						array.set(i, obj);

					}

					postdata = array.toString();
					String urlParameters = "rules=" + postdata;
					byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
					int postDataLength = postData.length;
					url = new URL(posturl);
					conn = (HttpURLConnection) url.openConnection();

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

				} // End of If
				else if (type.equals("Temperature")) {
					for (int i = 0; i < size; i++) {

						JsonObject obj = array.get(i).getAsJsonObject();

						if (obj.get("category").getAsString().equals("Temperature")) {
							String state_value = obj.get("state").getAsString().trim();
							String[] str = state_value.split("\\s+");
							obj.remove("state");
							obj.addProperty("state", str[0] + " \u00b0C"); // This is to handle the Celcious sign
							obj.remove("stateONOFF");
							obj.addProperty("stateONOFF", state);

						}

						array.set(i, obj);

					}

					postdata = array.toString();
					String urlParameters = "rules=" + postdata;
					byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
					int postDataLength = postData.length;
					url = new URL(posturl);
					conn = (HttpURLConnection) url.openConnection();

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
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

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

			String urlParameters = "ep_kWh=" + totalkwh + "&ep_totalError=" + totalaverageerror
					+ "&ep_convenienceError=" + convinienceerror + "&ep_budgetError=" + budgeterror
					+ "&ep_standardDeviation=" + deviation + "&kwh_per_month=" + kwhpermonth;
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

	private double calculateKWH3(ArrayList<String[]> sRecordsHourly) {
		ArrayList<String[]> copiediotdatakwh = new ArrayList<String[]>(sRecordsHourly);
		ArrayList<Integer> removedMetaRules = new ArrayList<Integer>();
		boolean randomRule[] = new boolean[MRTrules.size()];
		Random rand = new Random();
		int numrules = MRTrules.size();
		int random;
		Boolean startALLzeros = false;
		Boolean startALLrandom = false;
		int numberOfLoops = 3;

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

			for (int i = 0; i < numberOfLoops; i++) {
				random = rand.nextInt(numrules);
				// random here is the rule to be removed
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

			for (int i = 0; i < numberOfLoops; i++) {
				random = rand.nextInt(numrules);
				// random here is the rule to be removed
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
		ArrayList<Double> monthlyKWH = new ArrayList<Double>();

		if (monthlyKWH_listAssignment.equals("original")) {
			monthlyKWH = new ArrayList<Double>(); // 0.24
			Double[] d = new Double[] { 1.04, 0.71, 0.33, 0.19, 0.24, 0.28, 0.33, 0.43, 0.28, 0.34, 0.28, 0.57 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("1")) {
			monthlyKWH = new ArrayList<Double>();
			Double[] d = new Double[] { 1.03, 0.70, 0.33, 0.19, 0.23, 0.28, 0.33, 0.42, 0.28, 0.23, 0.28, 0.56 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("5")) {
			monthlyKWH = new ArrayList<Double>();

			Double[] d = new Double[] { 0.99, 0.68, 0.32, 0.18, 0.23, 0.27, 0.32, 0.41, 0.27, 0.23, 0.27, 0.54 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("10")) {
			monthlyKWH = new ArrayList<Double>();

			Double[] d = new Double[] { 0.94, 0.64, 0.30, 0.17, 0.21, 0.26, 0.30, 0.38, 0.26, 0.21, 0.26, 0.51 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("20")) {
			monthlyKWH = new ArrayList<Double>();

			Double[] d = new Double[] { 0.83, 0.57, 0.27, 0.15, 0.19, 0.23, 0.27, 0.34, 0.23, 0.19, 0.23, 0.45 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("30")) {
			monthlyKWH = new ArrayList<Double>();
			Double[] d = new Double[] { 0.73, 0.50, 0.23, 0.13, 0.17, 0.20, 0.23, 0.30, 0.20, 0.17, 0.20, 0.40 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		} else if (monthlyKWH_listAssignment.equals("40")) {
			monthlyKWH = new ArrayList<Double>();
			Double[] d = new Double[] { 0.63, 0.43, 0.20, 0.11, 0.14, 0.17, 0.20, 0.26, 0.17, 0.14, 0.17, 0.34 };
			for (int i = 0; i < d.length; i++)
				monthlyKWH.add(d[i]);
		}
		try {

			ArrayList<String> listRules = new ArrayList<String>();
			for (int d = 0; d < randomRule.length; d++) {
				if (randomRule[d]) {

					// temperature
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
						if ((!rulesSplitted[1].equals("---")) && (rulesSplitted[4].equals("---"))
								&& (rulesSplitted[5].equals("---")) && (rulesSplitted[6].equals("---"))) {

							if ((getHour(copiediotdataSplitted[0]) >= ruleHourFrom(rulesSplitted[0])
									&& getHour(copiediotdataSplitted[0]) <= ruleHourTo(rulesSplitted[0]))) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms += span;
								dt1 = dt2;

								localContiguous_ms += span;
								double secondsTemp = localContiguous_ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 24;
								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								milliWatts = milliWatts + 320;
								copiediotdataSplitted[3] = rulesSplitted[3];
								copiediotdatakwh.set(i, copiediotdataSplitted);
								break; // its already on the list no need to add it twice
							}

						}

					}

					if (copiediotdataSplitted[2].startsWith("LS")) // gets LS for light sensor
					{
						// checks LIGHT , and if weather,door is empty
						if ((!rulesSplitted[2].equals("---")) && (rulesSplitted[5].equals("---"))
								&& (rulesSplitted[6].equals("---"))) {
							// checks time and light value
							if ((getHour(copiediotdataSplitted[0]) >= ruleHourFrom(rulesSplitted[0])
									&& getHour(copiediotdataSplitted[0]) <= ruleHourTo(rulesSplitted[0]))) {
								dt2 = simpleFormatter.parse(copiediotdataSplitted[0].substring(0, 18));
								span = Math.abs(dt2.getTime() - dt1.getTime());
								ms += span;
								dt1 = dt2;

								localContiguous_ms += span;
								double secondsTemp = localContiguous_ms / 1000;
								double hoursTemp = secondsTemp / 3600;
								double kWhTemp = ((hoursTemp * 320) / 1000) * 24;
								copiediotdataSplitted = addX(copiediotdataSplitted, String.valueOf(kWhTemp));

								copiediotdataSplitted[3] = rulesSplitted[3];
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

		double seconds = ms / 1000;
		double hours = seconds / 3600;

		double kWh = ((hours * 320) / 1000) * 24;

		String[] copiediotdataSplitted_Month = copiediotdatakwh.get(0);
		Date getMonth;
		int currentMonth = 0;
		try {
			getMonth = simpleFormatter.parse(copiediotdataSplitted_Month[0].substring(0, 18));
			currentMonth = getMonth.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		double allowedMonthly_kWh = toDouble(monthlyKWH.get(currentMonth - 1).toString()) * 6.5;// 6.5, *7.9;

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
					// System.out.println(copiediotdatakwh.get(i));
					String[] rangesSplitted = listRanges[k];

					if (copiediotdataSplitted2[2].startsWith("T")) // gets T for temperatures
					{

						// checks time and temperature and if season is empty
						if (((!rangesSplitted[0].equals("SUMMER")) || (!rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdatakwh.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdatakwh.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
							} else {
								dTotalError_kWh += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdatakwh.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdatakwh.get(i));
							} else {
								dTotalError_kWh += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);

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

			System.out.println("Second B: " + errorLine + "\tException:\t" + e);

			e.printStackTrace();
		}

		// TempIotData.clear();
		copiediotdata3.clear();

		// *******************************end of second phase (calculating Error
		// phase)*********************

		if ((kWh > allowedMonthly_kWh) && (solutionsFound == false)) {

			if (removedMetaRules.size() == numrules) {
				removedMetaRules.clear();
			}
			calculateKWH3(sRecordsHourly);
			return public_ms;

		} else if (recursiveIterator < 5) {
			if (recursionResults.size() == 0)// check if result found first time
			{
				String temp[] = new String[3];
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
				recursionResults.add(temp);
			} else {// now check if new result is better than the previous and replace it

				String[] splitted = recursionResults.get(0);
				if (dTotalError < toDouble(splitted[2])) {
					String temp[] = new String[3];
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
					// recursionResults.set(0, String.join(", ", removedMetaRules) + "|" +
					// String.valueOf(kWh) + "|" + String.valueOf(dTotalError));
					recursionResults.set(0, temp);
				}
			}

			recursiveIterator++;
			if (removedMetaRules.size() == numrules) {
				removedMetaRules.clear();
			}
			if (recursiveIterator == 5) {
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
			calculateKWH3(sRecordsHourly);
			return public_ms;

		} else {
			contiguous_ms = localContiguous_ms;
			dtTemp = dt1;
			EnergyPlannerData.addAll(copiediotdatakwh);
			removedMetaRules.clear();
			recursiveIterator = 0;
			recursionResults.clear();
			solutionsFound = false;

			public_ms = ms;
			return public_ms;

		}

	}// END OF CALCULATE KWH METHOD - number of modifications

	public void energyplanner(ArrayList<String[]> data, ArrayList<String[]> MRTrulesOriginal,
			Double formaliseMaxError) {
		copiediotdata = new ArrayList<String[]>(data); // clone original dataset list
		MRTrules = new ArrayList<String[]>(MRTrulesOriginal);
		listRanges = MRTrules.toArray(new String[0][]);

		try {
			dtTemp = simpleFormatter.parse("2013-10-22 02:32:34.409"); // temp date

		} catch (Exception e) {
			System.out.println(e);
		}

		contiguous_ms = 0;
		ArrayList<String[]> recordsHourly = new ArrayList<String[]>();
		double hourChange = 2;
		double total_ms = 0;
		int monthchange = 0;
		int yearchange = 1998;
		DecimalFormat df1 = new DecimalFormat("#.##");
		df1.setRoundingMode(RoundingMode.CEILING);
		String monthString;
		EnergyPlannerData = new ArrayList<String[]>();
		copiediotdata3 = new ArrayList<String[]>(); // clone original dataset list
		boolean first = true;

		try {
			for (int i = 0; i < copiediotdata.size(); i++) {
				// System.out.println(i);
				String[] copiediotdataSplitHour = copiediotdata.get(i);
				int tempHourChange = getHour(copiediotdataSplitHour[0]);
				int tempmonthchange = getMonth(copiediotdataSplitHour[0]);

				if (hourChange == tempHourChange) {

					if (i == copiediotdata.size() - 1) {
						recordsHourly.add(copiediotdata.get(i));
						double hourly_ms = calculateKWH3(recordsHourly);
						total_ms = total_ms + hourly_ms;
						recordsHourly.clear();
					} else {
						recordsHourly.add(copiediotdata.get(i));
					}

				} else {
					hourChange = tempHourChange;
					double hourly_ms = calculateKWH3(recordsHourly);
					total_ms = total_ms + hourly_ms;
					recordsHourly.clear();
					recordsHourly.add(copiediotdata.get(i));
				}

				// this is the calculation for the kwh per month

				if (monthchange != tempmonthchange) {
					monthchange = tempmonthchange;
					// first is first month
					if (first) {
						double seconds1 = total_ms / 1000;
						double hours1 = seconds1 / 3600;
						double kWh1 = ((hours1 * 320) / 1000) * 24;

						kwhpermonth = kwhpermonth.concat(df1.format(kWh1));
						first = false;
					} else {
						double seconds1 = total_ms / 1000;
						double hours1 = seconds1 / 3600;
						double kWh1 = ((hours1 * 320) / 1000) * 24;

						kwhpermonth = kwhpermonth.concat("," + df1.format(kWh1));

					}
				}
				// END of calculation for the kwh per month
			}
		} catch (Exception ex) {
			System.out.println("First A: " + errorLine + " => Exception: " + ex);
			ex.printStackTrace();
		}

		copiediotdata.clear();

		copiediotdata = new ArrayList<String[]>(EnergyPlannerData);
		EnergyPlannerData.clear();

		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);

		double seconds = total_ms / 1000;
		double hours = seconds / 3600;
		double kWh = ((hours * 320) / 1000) * 24;
		String label154 = "Kilowatt hour (kWh): " + df.format(kWh) + " kWh";
		System.out.println(label154);
		totalkwh = toDouble(df.format(kWh));
		double dTotalError = 0;
		double dLineError = 0;
		double dTotalError_kWh = 0;

		// **********SECOND AND LAST PHASE*******************//

		Double maxError = 0.0;
		Double minError = 100.0;
		Boolean errorFound = false;
		int errorsize = 0;

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
						if (((!rangesSplitted[0].equals("SUMMER")) || (!rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
								//updateOpenhabRules("Temperature", "ON");
								copiediotdata3.add(copiediotdata.get(i));

							} else {

								//updateOpenhabRules("Temperature", "OFF");

								// assigns TOTAL ERROR AND CURRENT RECORD ERROR

								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
										errorFound = true;
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));
								//updateOpenhabRules("Light", "ON");

							} else {
								//updateOpenhabRules("Light", "OFF");
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR

								dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
								dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
								dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

								if (dLineError > maxError) {
									maxError = dLineError;
								}
								if (dLineError < minError) {
									minError = dLineError;
								}
								errorFound = true;
							}

						} // end of :checks SUNNY

						// checks CLOUDY
						if (rangesSplitted[3].equals("CLOUDY")) {
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
									errorFound = true;
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError_kWh += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);

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
			System.out.println("Second: " + errorLine + "\tException:\t" + e);

		}

		df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		dTotalError = dTotalError * 5; // 10 7 5
		String label161 = "Total Error: " + df.format((dTotalError * 100) / formaliseMaxError);
		String label158 = "Max Error: " + df.format((maxError * 100) / formaliseMaxError) + "%";
		String label157 = "Min Error: " + df.format((minError * 100) / formaliseMaxError) + "%";
		// calculate Total Average Error
		double averageErrorTotal = toDouble(df.format((dTotalError / copiediotdata.size())));
		String label153 = "Total Average Error: " + df.format((averageErrorTotal * 100) / formaliseMaxError) + "%";
		totalaverageerror = toDouble(df.format((averageErrorTotal * 100) / formaliseMaxError));

		// calculate Budget Average Error
		double averageError_Budget = toDouble(df.format((dTotalError_kWh / copiediotdata.size())));
		String label151 = "Budget Average Error: " + df.format((averageError_Budget * 100) / formaliseMaxError) + "%";
		budgeterror = toDouble(df.format((averageError_Budget * 100) / formaliseMaxError));
		// calculate Convenience Average Error
		double averageError_Concenience = toDouble(df.format(((dTotalError - dTotalError_kWh) / copiediotdata.size())));
		String label152 = "Convenience Average Error: "
				+ df.format((averageError_Concenience * 100) / formaliseMaxError) + "%";
		convinienceerror = toDouble(df.format((averageError_Concenience * 100) / formaliseMaxError));

		System.out.println(label161);
		System.out.println(label158);
		System.out.println(label157);
		System.out.println(label153);
		System.out.println(label151);
		System.out.println(label152);

		copiediotdata3.clear();

		// ************SECOND RUN*******************************
		Double meansSum = 0.0;
		maxError = 0.0;
		minError = 100.0;
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
						if (((!rangesSplitted[0].equals("SUMMER")) || (!rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
									&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								}

							}
						}

						if (((rangesSplitted[0].equals("SUMMER")) || (rangesSplitted[0].equals("WINTER")))
								&& (rangesSplitted[3].equals("TEMPER."))) {
							int iSeason = getMonth(copiediotdataSplitted2[0]);

							// checks if SUMMER
							if ((iSeason == 6) || (iSeason == 7) || (iSeason == 8)) {
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
								if (toDouble(copiediotdataSplitted2[3]) >= toDouble(rangesSplitted[1])
										&& toDouble(copiediotdataSplitted2[3]) <= toDouble(rangesSplitted[2])) {
									copiediotdata3.add(copiediotdata.get(i));

								} else {
									// assigns TOTAL ERROR AND CURRENT RECORD ERROR
									if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
										dTotalError += toDouble(rangesSplitted[1])
												- toDouble(copiediotdataSplitted2[3]);
										dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

										if (dLineError > maxError) {
											maxError = dLineError;
										}
										if (dLineError < minError) {
											minError = dLineError;
										}
									} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
										dTotalError += toDouble(copiediotdataSplitted2[3])
												- toDouble(rangesSplitted[2]);
										dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								//

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
										&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
										&& toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if ((getHour(copiediotdataSplitted2[0]) >= ruleHourFrom(rangesSplitted[0])
									&& getHour(copiediotdataSplitted2[0]) <= ruleHourTo(rangesSplitted[0]))
									&& toInt(copiediotdataSplitted2[3]) >= toInt(rangesSplitted[1])
									&& toInt(copiediotdataSplitted2[3]) <= toInt(rangesSplitted[2])) {
								copiediotdata3.add(copiediotdata.get(i));

							} else {
								// assigns TOTAL ERROR AND CURRENT RECORD ERROR
								if (toDouble(copiediotdataSplitted2[3]) < toDouble(rangesSplitted[1])) {
									dTotalError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);
									dLineError += toDouble(rangesSplitted[1]) - toDouble(copiediotdataSplitted2[3]);

									if (dLineError > maxError) {
										maxError = dLineError;
									}
									if (dLineError < minError) {
										minError = dLineError;
									}
								} else if (toDouble(copiediotdataSplitted2[3]) > toDouble(rangesSplitted[2])) {
									dTotalError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);
									dLineError += toDouble(copiediotdataSplitted2[3]) - toDouble(rangesSplitted[2]);

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
							if (toDouble(copiediotdataSplitted2[7]) <= toDouble(rangesSplitted[1])) {
								copiediotdata3.add(copiediotdata.get(i));
							} else {
								dTotalError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);
								dLineError += toDouble(copiediotdataSplitted2[7]) - toDouble(rangesSplitted[2]);

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
			System.out.println("Second: " + errorLine + "\tException:\t" + e);

		}

		df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);

		double standardDeviation = Math.sqrt(meansSum / copiediotdata.size());
		String label156 = "Standard Deviation: " + df.format((standardDeviation * 100) / formaliseMaxError) + "%";
		deviation = toDouble(df.format((standardDeviation * 100) / formaliseMaxError));
		System.out.println(label156);
		copiediotdata3.clear();

		postData();
	}// END OF ENERGY PLANNER - number of modifications

}
