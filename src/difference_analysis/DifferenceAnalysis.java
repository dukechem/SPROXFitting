package difference_analysis;

import java.util.Arrays;

import statics.FFConstants;

public class DifferenceAnalysis {

	// contains the control and ligand values
	double[] i1;
	double[] i2;

	private final double lower;
	private final double upper;

	private final boolean passed;

	private double[] peptideDifferences;

	public DifferenceAnalysis(double[] i1, double[] i2, double lower,
			double upper) {
		this.i1 = i1;
		this.i2 = i2;
		this.lower = lower;
		this.upper = upper;

		this.passed = calculatePassed();
	}

	private boolean calculatePassed() {
		if (i1.length != i2.length)
			return false;

		peptideDifferences = new double[i1.length];
		int[] comps = new int[i1.length];

		for (int i = 0; i < i1.length; i++) {
			/*
			 * 1 for i1 - i2 > 0.3 0 for | i1 - i2 | < 0.3 -1 for i1 - i2 < -0.3
			 * 
			 * where 0.3 is FFConstants.heuristic (user defined, defaults to
			 * 15th percentile)
			 */
			double difference = i1[i] - i2[i];
			peptideDifferences[i] = difference;
			int sig;
			// In between the two bounds, assign 0
			if ((difference > lower && difference < upper)) {
				sig = 0;
			} else {
				// If it is above upper bound, assign 1, else -1 (already
				// checked if between bounds
				sig = (difference >= upper) ? 1 : -1;
			}
			// assign to comps
			comps[i] = sig;
		}
		if (!FFConstants.RUN_PEPTIDE_ANALYSIS)
			return true; // bypass peptide differences
		/*
		 * iterate through comps to find two 1 or two -1 together. CANNOT HAVE
		 * BOTH
		 */
		int numberPosNext = 0;
		int numberNegNext = 0;
		for (int i = 1; i < comps.length; i++) {
			// previous
			int prev = comps[i - 1];
			int curr = comps[i];
			// 0 is basel, ignore
			if (prev == curr && prev == 1)
				numberPosNext++;
			if (prev == curr && prev == -1)
				numberNegNext++;
		}
		if (numberPosNext != 0 && numberNegNext != 0)
			return false;
		boolean pos = numberPosNext != 0;
		boolean neg = numberNegNext != 0;
		return pos ^ neg; //can't have both be true
	}

	public boolean getPassed() {
		return this.passed;
	}

	public double[] getPeptideDifferences() {
		return this.peptideDifferences;
	}

	public static void main(String[] args) {
		double[] d1 = new double[] {1.6639,	1.36218,	1.0055,	1.31179,	1.10563,	0.628371,	0.664786,	0.37346};
		double[] d2 = new double[] {1.40081,	1.53168,	1.03814,	0.783611,	0.842433,	1.12881,	0.760567,	0.664373};
		DifferenceAnalysis diff = new DifferenceAnalysis(d1, d2, -0.093881408, 0.096997);
		System.out.println(diff.getPassed());
		System.out.println(Arrays.toString(diff.getPeptideDifferences()));
	}
}