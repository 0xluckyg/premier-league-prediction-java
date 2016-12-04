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

	public static void main(String[] args) throws IOException  {		
				
		Map<String, ArrayList<ArrayList<String>>> finalData = new HashMap<String, ArrayList<ArrayList<String>>>();
		
		BufferedReader reader = null;
		File[] file = new File("../../Data").listFiles();
		for (File dataFile : file) {
	    	reader = new BufferedReader(new FileReader(dataFile));
		    String line;
		    while ((line = reader.readLine()) != null) {
		    	
		    	String[] lineParse = line.split(",");		    	
		    	ArrayList<String> team1 = new ArrayList<String>();
		    	ArrayList<String> team2 = new ArrayList<String>();		    	
		    	teamParser(team1, team2, lineParse);
		    	
		    	addTeamToHash(team1, team2, finalData, lineParse);		    			    
		    	
		    	cleanedFile (finalData);
		    	
		    	summaryFile(finalData);
		    	
		    	
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
    		if (Float.parseFloat(team1.get(2)) > Float.parseFloat(team1.get(3)) &&
    			Float.parseFloat(team1.get(2)) > Float.parseFloat(team1.get(4))) {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("F");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("F");
    			else team1.add("T");
    			
    		} else if (Float.parseFloat(team1.get(3)) > Float.parseFloat(team1.get(2)) &&
        			Float.parseFloat(team1.get(3)) > Float.parseFloat(team1.get(4))) {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("F");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("T");
    			else team1.add("F");
    			
    		} else {
    			
    			if (Float.parseFloat(team1.get(5)) > 0) team1.add("T");
    			else if (Float.parseFloat(team1.get(5)) == 0) team1.add("F");        			        			
    			else team1.add("F");
    			
    		} 
    		
    		if (Float.parseFloat(team2.get(2)) > Float.parseFloat(team2.get(3)) &&
        			Float.parseFloat(team2.get(2)) > Float.parseFloat(team2.get(4))) {
        			
        			if (Float.parseFloat(team2.get(5)) > 0) team2.add("F");
        			else if (Float.parseFloat(team2.get(5)) == 0) team2.add("F");
        			else team2.add("T");
        			
        		} else if (Float.parseFloat(team2.get(3)) > Float.parseFloat(team2.get(2)) &&
            			Float.parseFloat(team2.get(3)) > Float.parseFloat(team2.get(4))) {
        			
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
	
}
