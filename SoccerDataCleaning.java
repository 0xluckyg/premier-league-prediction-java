import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class SoccerDataCleaning {

	public static String keys = "B365H,B365D,B365A,BSH,BSD,BSA,BWH,BWD,BWA,GBH,GBD,GBA,IWH,IWD,IWA,"
			+ "LBH,LBD,LBA,PSH,PSD,PSA,SOH,SOD,SOA,SBH,SBD,SBA,SJH,SJD,SJA,SYH,SYD,SYA,"
			+ "VCH,VCD,VCA,WHH,WHD,WHA";
	public static ArrayList<String> fileKeyList = new ArrayList<String>();

	public static HashMap<String, Integer> makeKeyMap() {		
		String[] keysArray = keys.split(",");
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int i = 0; i < keysArray.length; i++) {
			map.put(keysArray[i], 0);
		}
		
		return map;
	}
	
	public static void main(String[] args) throws IOException  {								
		
		Map<String, ArrayList<ArrayList<String>>> finalData = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		BufferedReader reader = null;
		File[] file = new File("../../Data").listFiles();
		for (File dataFile : file) {
	    	reader = new BufferedReader(new FileReader(dataFile));
		    String line;
	    	ArrayList<String> keyList = new ArrayList<String>();
			keyList.add("OppositeTeam");
			keyList.add("Date");
			keyList.add("Result");
			
			String[] keyArray = keys.split(",");
			String keyString = new String();
			for (int i = 0; i < keyArray.length; i ++) {
				switch (i % 3) {
					case 0:
						keyString = keyArray[i].substring(0, keyArray[i].length() - 1);
						keyString += "W";
						keyList.add(keyString);
						break;
					case 1:
						keyString += "D";
						keyList.add(keyString);
						break;
					case 2:
						keyString += "L";
						keyList.add(keyString);
						
						keyString = keyArray[i].substring(0, keyArray[i].length() - 1);
						keyList.add(keyString + "WP");
						keyList.add(keyString + "DP");
						keyList.add(keyString + "LP");
						break;
				}
			}
			
			HashMap<String, Integer> keyMap = makeKeyMap();			
			
		    while ((line = reader.readLine()) != null) {
		    	String[] lineParse = line.split(",");			    	
		    	ArrayList<String> team1 = new ArrayList<String>();
		    	ArrayList<String> team2 = new ArrayList<String>();		    	
		    		
		    	if (lineParse.length >= 65 && lineParse[0].equals("Div")) {		    		
		    		System.out.println("AGAIN");
		    		for (String key : keyMap.keySet()) {		    			
		    			keyMap.put(key, 0);
		    		}	
		    		for (String key : lineParse) {
		    			if (keyMap.get(key) != null) {
		    				keyMap.put(key, 1);
		    			}
		    		}
		    		
		    		fileKeyList = new ArrayList<String>();
					for (int i = 0; i < lineParse.length; i ++) {
						if (keyMap.get(lineParse[i]) != null) {
							fileKeyList.add(lineParse[i]);
						}
					}		    							
		    	}
		    	
		    	teamParser2(team1, team2, lineParse, keyMap);		    	    	
		    	
		    	addTeamToHash(team1, team2, finalData, lineParse);		    			    
		    			    			    	
		    	cleanedFile2 (finalData, keyList);		    			    		    	
    				    	
		    }
	    }		
				
	}
	
	public static void cleanedFile2 (Map<String, ArrayList<ArrayList<String>>> finalData, ArrayList<String> keys) throws IOException {
	    PrintWriter writer = new PrintWriter("../../Result/dataCleanResult.csv", "UTF-8");
	    String keyString = new String();
	    for (int i = 0; i < keys.size(); i ++) {
	    	keyString = keyString + "," + keys.get(i);	    	
	    }
	    writer.println(keyString);
	    
		for(String key: finalData.keySet()){
			
			ArrayList<ArrayList<String>> array = finalData.get(key);
			
			Collections.sort(array, new Comparator<ArrayList<String>>() {
				@Override
				public int compare(ArrayList<String> o1, ArrayList<String> o2) {
					
					String[] date1 = o1.get(1).split("/");
					String[] date2 = o2.get(1).split("/");
					
					Integer.parseInt(date1[2]);
					
					if (Integer.parseInt(date1[2]) == Integer.parseInt(date2[2])) {
						if (Integer.parseInt(date1[1]) == Integer.parseInt(date2[1])) {
							return Integer.parseInt(date2[0]) - Integer.parseInt(date1[0]);
						} else {
							return Integer.parseInt(date2[1]) - Integer.parseInt(date1[1]);
						}
					} else {
						return Integer.parseInt(date2[2]) - Integer.parseInt(date1[2]); 
					}						
				}						
			});
			
			String line = key;
			for (int i = 0; i < array.size(); i ++) {
				for (int j = 0; j < array.get(i).size(); j ++) {
					line = line + "," + array.get(i).get(j);
				} 
			    writer.println(line);
			    line = key;
			}			
        }
		writer.close();
	}
		
	public static void teamParser2 (ArrayList<String> team1, ArrayList<String> team2, 
			String[] lineParse, HashMap<String, Integer> keyMap) {		
		
		if (lineParse.length >= 65 && !lineParse[0].equals("Div")) {
    		//Add opposite team
    		team1.add(lineParse[3]);
    		team2.add(lineParse[2]);
    		
    		//Add date
    		team1.add(lineParse[1]);
    		team2.add(lineParse[1]);
    		
    		//Add result
    		//If result is positive, home wins
    		//If result is negative, away wins
    		int result = (Integer.parseInt(lineParse[4]) - Integer.parseInt(lineParse[5]));
    		team1.add(String.valueOf(result));
    		team2.add(String.valueOf(-result));
    		
    		//Add odds
    		String[] oddsArray = new String[3];

    		String[] totalKeys = keys.split(",");
    		
    		int j = 0;
    		for (int i = 0; i < totalKeys.length; i ++) {
    			if (lineParse.length >= 65 && !lineParse[j + 23].isEmpty()) {    				
    				if (keyMap.get(totalKeys[i]) == 1) {
    					String HDA = totalKeys[i].substring(totalKeys[i].length() - 1);
    					
    	    			if (HDA.equals("H")) {    	    				
	    					oddsArray[0] = lineParse[j + 23];
	    					j++;
    	    			} else if (HDA.equals("D")) {    	    				
	    					oddsArray[1] = lineParse[j + 23];
	    					j++;
    	    			} else {    	    				
	    					oddsArray[2] = lineParse[j + 23];	
	    					j++;	    					
	    					float a = Float.parseFloat(oddsArray[0]);
	    					float b = Float.parseFloat(oddsArray[1]);
	    					float c = Float.parseFloat(oddsArray[2]);
	    					
	    		    		team1.add(oddsArray[0]);
	    		    		team1.add(oddsArray[1]);
	    		    		team1.add(oddsArray[2]);
	    		    		
	    		    		float Pa = (float) (1.0/(1.0 + (a/b) + (a/c)));
	    		    		float Pb = (float) (1.0/(1.0 + (b/a) + (b/c)));
	    		    		float Pc = (float) (1.0/(1.0 + (c/a) + (c/b)));
	    		    		
	    		    		team1.add(Float.toString(Pa));
	    		    		team1.add(Float.toString(Pb));
	    		    		team1.add(Float.toString(Pc));
	    		    		
	    					a = Float.parseFloat(oddsArray[2]);
	    					b = Float.parseFloat(oddsArray[1]);
	    					c = Float.parseFloat(oddsArray[0]);
	    		    		
	    		    		team2.add(oddsArray[2]);
	    		    		team2.add(oddsArray[1]);
	    		    		team2.add(oddsArray[0]);
	    		    		
	    		    		Pa = (float) (1.0/(1.0 + (a/b) + (a/c)));
	    		    		Pb = (float) (1.0/(1.0 + (b/a) + (b/c)));
	    		    		Pc = (float) (1.0/(1.0 + (c/a) + (c/b)));
	    		    		
	    		    		team2.add(Float.toString(Pa));
	    		    		team2.add(Float.toString(Pb));
	    		    		team2.add(Float.toString(Pc));
    	    			}	
    				} else {	    				
	    		    	team1.add("X");	    		   			    		   		
	    		   		team2.add("X");	 
	    		   		team1.add("X");	    		   			    		   		
	    		   		team2.add("X");	 
    				}
    			} else {
    				team1.add("X");	    		   			    		   		
    		   		team2.add("X");	  
    		   		team1.add("X");	    		   			    		   		
    		   		team2.add("X");	 
    			}
    		}    		
		}						
	}
	
	public static void teamParser (ArrayList<String> team1, ArrayList<String> team2, String[] lineParse) {
		
//		oppositeTeam;
//    	date;
//    	winOdd;
//    	drawOdd;
//    	loseOdd;
//    	result;
//    	prediction;
		
		if (lineParse.length >= 65 && !lineParse[0].equals("Div")) {
    		//Add opposite team
    		team1.add(lineParse[3]);
    		team2.add(lineParse[2]);
    		
    		//Add date
    		team1.add(lineParse[1]);
    		team2.add(lineParse[1]);
    		
    		//Add odds
    		float[] oddsArray = new float[3];
    		int homeOddNumber = 0;
    		int drawOddNumber = 0;
    		int awayOddNumber = 0;
    		for (int i = 0; i <= 18; i ++) {
    			if (!lineParse[i + 23].isEmpty()) {
    			float odds = Float.parseFloat(lineParse[i + 23]);     			
	    			switch (i % 3) {
	    				case 0:
	    					homeOddNumber ++;
	    					oddsArray[0] = oddsArray[0] + odds;
	    					break;
	    				case 1:
	    					drawOddNumber ++;
	    					oddsArray[1] = oddsArray[1] + odds;
	    					break;
	    				case 2:
	    					awayOddNumber ++;
	    					oddsArray[2] = oddsArray[2] + odds;
	    					break;
	    			}	
    			}
    		}
    		String homeTeamWinOdds = String.valueOf(oddsArray[0]/homeOddNumber);
    		String drawOdds = String.valueOf(oddsArray[1]/drawOddNumber);
    		String awayTeamWinOdds = String.valueOf(oddsArray[2]/awayOddNumber);
    		team1.add(homeTeamWinOdds);
    		team1.add(drawOdds);
    		team1.add(awayTeamWinOdds);
    		team2.add(awayTeamWinOdds);
    		team2.add(drawOdds);
    		team2.add(homeTeamWinOdds);
    				    		
    		//Add result
    		//If result is positive, home wins
    		//If result is negative, away wins
    		int result = (Integer.parseInt(lineParse[4]) - Integer.parseInt(lineParse[5]));
    		team1.add(String.valueOf(result));
    		team2.add(String.valueOf(-result));
    		
    		//Add prediction
    		if (Float.parseFloat(team1.get(2)) < Float.parseFloat(team1.get(3)) &&
    			Float.parseFloat(team1.get(2)) < Float.parseFloat(team1.get(4))) {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("F");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("F");
    			else team1.add("T");
    			
    		} else if (Float.parseFloat(team1.get(3)) < Float.parseFloat(team1.get(2)) &&
        			Float.parseFloat(team1.get(3)) < Float.parseFloat(team1.get(4))) {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("F");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("T");
    			else team1.add("F");
    			
    		} else {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("T");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("F");        			        			
    			else team1.add("F");
    			
    		} 
    		
    		if (Float.parseFloat(team2.get(2)) < Float.parseFloat(team2.get(3)) &&
        			Float.parseFloat(team2.get(2)) < Float.parseFloat(team2.get(4))) {
        			
        			if (Float.parseFloat(team2.get(5)) > 0) team2.add("F");
        			else if (Float.parseFloat(team2.get(5)) == 0) team2.add("F");
        			else team2.add("T");
        			
        		} else if (Float.parseFloat(team2.get(3)) < Float.parseFloat(team2.get(2)) &&
            			Float.parseFloat(team2.get(3)) < Float.parseFloat(team2.get(4))) {
        			
        			if (Float.parseFloat(team2.get(5)) > 0) team2.add("F");
        			else if (Float.parseFloat(team2.get(5)) == 0) team2.add("T");
        			else team2.add("F");
        			
        		} else {
        			
        			if (Float.parseFloat(team2.get(5)) > 0) team2.add("T");
        			else if (Float.parseFloat(team2.get(5)) == 0) team2.add("F");        			        			
        			else team2.add("F");
        			
        		}   
    		
    	}
	}
	
	public static void summaryFile (Map<String, ArrayList<ArrayList<String>>> finalData) throws IOException {
		
	    PrintWriter writer = new PrintWriter("../../Result/summaryResult.csv", "UTF-8");
	    writer.println("Team,Win,Lose,Draw,Goals,GoalsTaken,NumberOfAccuratePredictions,NumberOfWrongPredictions,"
	    		+ "TotalWinOdds,TotalDrawOdds,TotalLoseOdds,WinPercent,LosePercent,DrawPercent,"
	    		+ "AccuratePredictionPercent,WrongPredictionPercent");
	    
		for(String key: finalData.keySet()){
			ArrayList<ArrayList<String>> array = finalData.get(key);

			float wins = 0, losses = 0, draws = 0, numberOfGoals = 0, numberOfGoalsTaken = 0, 
				numberOfPredictionsGuessedAccurate = 0, numberOfPredictionsGuessedWrong = 0;
				
			float totalWinOdds = 0, totalDrawOdds = 0, totalLossOdds = 0, 
					percentOfWins, percentOfLosses, percentOfDraws, percentOfAccurateGuesses, percentOfWrongGuesses;
			
			for (int i = 0; i < array.size(); i ++) {

				int result = Integer.parseInt(array.get(i).get(5));
				if (result > 0) {
					wins ++;
					numberOfGoals += result;
				} else if (result == 0) {
					draws ++;					
				} else {
					losses ++;
					numberOfGoalsTaken += -result;
				}
				
				totalLossOdds += Float.parseFloat(array.get(i).get(2));
				totalDrawOdds += Float.parseFloat(array.get(i).get(3));
				totalWinOdds += Float.parseFloat(array.get(i).get(4));
				
				if (array.get(i).get(6).equals("T")) {
					numberOfPredictionsGuessedAccurate ++;
				} else {
					numberOfPredictionsGuessedWrong ++;
				}
							    			    
			}
			
			percentOfWins = (wins / (wins + draws + losses)) * 100;
			percentOfLosses = (losses / (wins + draws + losses)) * 100;
			percentOfDraws = (draws / (wins + draws + losses)) * 100;
			percentOfAccurateGuesses = (numberOfPredictionsGuessedAccurate / 
										(numberOfPredictionsGuessedAccurate + numberOfPredictionsGuessedWrong)) * 100;
			percentOfWrongGuesses = (numberOfPredictionsGuessedWrong / 
										(numberOfPredictionsGuessedAccurate + numberOfPredictionsGuessedWrong)) * 100;					 
			
			String line = String.format("%s,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", 
					key, wins, losses, draws, numberOfGoals, numberOfGoalsTaken,
					numberOfPredictionsGuessedAccurate, numberOfPredictionsGuessedWrong,
					totalWinOdds, totalDrawOdds, totalLossOdds, 
					percentOfWins, percentOfLosses, percentOfDraws, percentOfAccurateGuesses, percentOfWrongGuesses); 
			
			writer.println(line);
		}		
		writer.close();
		
	}
	
	public static void cleanedFile (Map<String, ArrayList<ArrayList<String>>> finalData) throws IOException {
	    PrintWriter writer = new PrintWriter("../../Result/dataCleanResult.csv", "UTF-8");
	    writer.println("HomeTeam,OppositeTeam,Date,WinOdds,DrawOdds,LoseOdds,Result,PredictionValidity");
		for(String key: finalData.keySet()){
			
			ArrayList<ArrayList<String>> array = finalData.get(key);
			
			Collections.sort(array, new Comparator<ArrayList<String>>() {
				@Override
				public int compare(ArrayList<String> o1, ArrayList<String> o2) {
					
					String[] date1 = o1.get(1).split("/");
					String[] date2 = o2.get(1).split("/");
					
					Integer.parseInt(date1[2]);
					
					if (Integer.parseInt(date1[2]) == Integer.parseInt(date2[2])) {
						if (Integer.parseInt(date1[1]) == Integer.parseInt(date2[1])) {
							return Integer.parseInt(date2[0]) - Integer.parseInt(date1[0]);
						} else {
							return Integer.parseInt(date2[1]) - Integer.parseInt(date1[1]);
						}
					} else {
						return Integer.parseInt(date2[2]) - Integer.parseInt(date1[2]); 
					}						
				}						
			});
			
			String line = key;
			for (int i = 0; i < array.size(); i ++) {
				for (int j = 0; j < array.get(i).size(); j ++) {
					line = line + "," + array.get(i).get(j);
				} 
			    writer.println(line);
			    line = key;
			}			
        }
		writer.close();
	}
		
	public static void addTeamToHash (ArrayList<String> team1, ArrayList<String> team2, Map<String, ArrayList<ArrayList<String>>> finalData, String[] lineParse) {
    	if (lineParse.length >= 65 && !lineParse[0].equals("Div")) {		    		
	    	if (finalData.get(lineParse[2]) == null) {
		        ArrayList<ArrayList<String>> teamData = new ArrayList<ArrayList<String>>();  		        		        		        		       
		        teamData.add(team1);		        
		        
		        finalData.put(lineParse[2], teamData);			    		
	    	} else {
	    		finalData.get(lineParse[2]).add(team1);
	    	};
	    	
	    	if (finalData.get(lineParse[3]) == null) {
		        ArrayList<ArrayList<String>> teamData = new ArrayList<ArrayList<String>>();  		        		        		        		       
		        teamData.add(team2);
		        
		        finalData.put(lineParse[3], teamData);			    		
	    	} else {
	    		finalData.get(lineParse[3]).add(team2);
	    	};
    	}	
	}

}
