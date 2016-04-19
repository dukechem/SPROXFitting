package statics;


public class FFConstants {

	/*For FFGraphGenerator*/
	public static final double InitialCHalfValue = 1.0d;
	public static final double InitialBValue = 0.3d;
	
	/*For FFChartableComparator*/
	public static double ADJ_R_SQ_HEURISTIC = 0.7d;
	public static double MIDPOINT_HEURISTIC = 0.5d;
	public static double DIFFERENCE_HEURISTIC_LOWER = 15;
	public static double LOWER_BOUND_PERCENTILE = Double.MIN_VALUE;
	public static double DIFFERENCE_HEURISTIC_UPPER = 85;
	public static double UPPER_BOUND_PERCENTILE = Double.MAX_VALUE;
	public static boolean RUN_PEPTIDE_ANALYSIS = true;
	
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
	
	/*For Histogram generator && FFStatistics*/
	public static final double CLEAN_UPPER_BOUND = 10d;
	public static final double CLEAN_LOWER_BOUND = -10d;
	
	/*For FFChartableComparator and HTMLGenerator*/
	public static final String COMPARISON_FILENAME = "Comparison.csv";
}

