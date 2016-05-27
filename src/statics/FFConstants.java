package statics;


public class FFConstants {

	/*Initial estimates for regression*/
	public static final double InitialCHalfValue = 1.0d;
	public static final double InitialBValue = 0.3d;
	
	
	/*For Histogram generator && FFStatistics*/
	//upper bound cutoff for histogram domain
	public static final double CLEAN_UPPER_BOUND = 10d;
	//lower bound cutoff for hisogram domain
	public static final double CLEAN_LOWER_BOUND = -10d;
	
	/*For FFChartableLogic and HTMLGenerator*/
	public static final String COMPARISON_FILENAME = "Comparison.csv";
	public static final String COMPARISON_HEADER_ADDITIONS = "A, B, C 1/2, C 1/2 SD, b, bSD, Adjusted R Squared,";
	
	/*For fitting logic*/
	public static double ADJ_R_SQ_HEURISTIC = 0.7d; //cutoff
	public static double MIDPOINT_HEURISTIC = 0.5d; //cutoff
	public static double DIFFERENCE_HEURISTIC_LOWER = 15; //%ile cutoff
	public static double LOWER_BOUND_PERCENTILE = Double.MIN_VALUE; //not calculated yet
	public static double DIFFERENCE_HEURISTIC_UPPER = 85; //%ile cutoff
	public static double UPPER_BOUND_PERCENTILE = Double.MAX_VALUE; //not calcualted yet
	public static boolean RUN_PEPTIDE_ANALYSIS = true; //default to running difference analysis
	public static double USER_DEFINED_A_VALUE = -1; //not inputted yet
	public static double USER_DEFINED_B_VALUE = -1; //not inputted yet
	
	/*Set above heuristics*/
	public static void setAdjustedRSquaredHeuristic(String set){
		try{
			ADJ_R_SQ_HEURISTIC = Double.parseDouble(set);
		}catch(Exception e){
			ADJ_R_SQ_HEURISTIC = 0.7d;
		}
	}
	
	public static void setMidPointHeuristic(String set){
		try{
			MIDPOINT_HEURISTIC = Double.parseDouble(set);
		}catch(Exception e){
		}
	}
	
	public static void setDifferenceHeuristic(String setLower, String setUpper){
		try{
			DIFFERENCE_HEURISTIC_LOWER = Double.parseDouble(setLower);
			DIFFERENCE_HEURISTIC_UPPER = Double.parseDouble(setUpper);
		}catch(Exception e){
		}
	}
	
	public static void setRunPeptideAnalysis(boolean bool){
		RUN_PEPTIDE_ANALYSIS = bool;
	}
	
	public static void setUpperBoundPercentile(double percentile){
		UPPER_BOUND_PERCENTILE = percentile;
	}
	public static void setLowerBoundPercentile(double percentile){
		LOWER_BOUND_PERCENTILE = percentile;
	}
}

