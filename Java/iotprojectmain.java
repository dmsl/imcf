/*
    This file implements the IMCF local controller main for using 
    the Energy Planner algorithm.
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

public class iotprojectmain {

	private double formaliseMaxError = 0.0;

	public iotprojectmain() {
	}

	public static void main(String[] args) {

		// Paths for windows
		/**/
		String pathIFTT = "C:\\Users\\anton\\Documents\\EPL400\\Data\\IFTTrules.txt";
		String pathMRT = "C:\\Users\\anton\\Documents\\EPL400\\Data\\MRTapartment.txt";
		String pathDATA = "C:\\Users\\anton\\Documents\\EPL400\\GreenPlanner\\green_data.txt";
		String pathNoIFTTresults = "C:\\Users\\anton\\Documents\\EPL400\\Data\\noiftt_results.txt";
		
		// Paths for server
		/*
		  String pathIFTT="apartmentData/IFTTrules.txt"; String
		  pathMRT="apartmentData/MRTapartment.txt"; String
		  pathDATA="apartmentData/apartmentDataset.txt"; String
		  pathNoIFTTresults="noiftt_results.txt";
		 */
		iotprojectmain project = new iotprojectmain();
		importData impdata = new importData(pathMRT, pathIFTT, pathDATA);

		// get updated meta-rules for Energy Planner project
		impdata.get_updated_MRTrules();

		// import data and rules
		impdata.import_IFTTT_rules();
		impdata.import_MRT_rules();
		impdata.import_IoT_data();

		// run with no ifttt rules
		noIFTT run = new noIFTT(impdata.getIFTTTrules(), impdata.getMRTrules(),
				 impdata.getIotData(),pathNoIFTTresults);
		project.formaliseMaxError = run.method();
		
		// run energy planner
		
		 EnergyPlanner ep = new EnergyPlanner();
		 ep.energyplanner(impdata.getIotData(),impdata.getMRTrules(),project.
		 formaliseMaxError);
		  
		 /* 
		 * // run with meta rules 
		 * mrt mr = new mrt();
		 * mr.MR(impdata.getIotData(),impdata.getMRTrules(),project.formaliseMaxError);
		 * 
		 * // run with ifttt rules 
		 * ifttt iftt = new ifttt();
		 * iftt.iftttMethod(impdata.getIotData(),impdata.getMRTrules(),impdata.
		 * getIFTTTrules(),project.formaliseMaxError);
		 *
		 *
		 * GreenPlanner gp = new GreenPlanner();
		 * gp.greenplanner(impdata.getIotData(), impdata.getMRTrules(), project.formaliseMaxError);
		 */
	}

}
