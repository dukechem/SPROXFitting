package difference_analysis;

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
				// If it is above upper bound, assign 1, else -1 (already checked if between bounds
				sig = (difference >= upper) ? 1 : -1;
			}
			// assign to comps
			comps[i] = sig;
		}
		if (!FFConstants.RUN_PEPTIDE_ANALYSIS)
			return true; // bypass peptide differences
		/*
		 * iterate through sigs to find two 1 or two -1 together. CANNOT HAVE
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
		return pos ^ neg;
	}

	public boolean getPassed() {
		return this.passed;
	}

	public double[] getPeptideDifferences() {
		return this.peptideDifferences;
	}

	public static void main(String[] args) {
		double[] d1 = new double[] { 1.44923, 2.64822, 1.79728, 0.788501,
				0.635343, 0.532062, 0.465364, 0.383963 };
		double[] d2 = new double[] { 1.62644, 2.98272, 1.7875, 0.853763,
				0.497392, 0.527146, 0.406604, 0.425795 };
		DifferenceAnalysis diff = new DifferenceAnalysis(d1, d2, -.1, .1);
		/*
		 * comps = [-1, -1, 0, 0, 1, 0, 0, 0] numberPosNext = 0 numberNegNext =
		 * 1 passed = true getPeptideDifference = [-0.17721, -0.3345, 0.00978,
		 * -0.065262, 0.137951, 0.004916, 0.05876, -0.041832]
		 */
	}
}