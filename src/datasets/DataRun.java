package datasets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.concurrent.Task;
import regression.CHalfFunction;
import statics.FFConstants;
import statics.FFMath;
import containers.SingleFit;
import flanagan.analysis.Regression;

public class DataRun extends Task<SingleFit>{

	private final double[] denaturants;
	private final double[] intensities;
	private final boolean detectOx;

	public DataRun(double[] intensities, Double[] denaturants, boolean detectOx){

		//populate primitive double array
		this.denaturants = new double[denaturants.length];
		for (int i = 0; i < denaturants.length; i++){
			this.denaturants[i] = denaturants[i];
		}

		//populate the intensities array
		this.intensities = intensities;

		//assign detectOx
		this.detectOx = detectOx;
	}

	@SuppressWarnings("serial")
	private double[] calculateFit(
			Double[] intensities,Double[] denaturants){

		double[] x = new double[denaturants.length];
		double[] y =  new double[intensities.length];
		for (int i = 0; i < x.length; i++){
			x[i] = denaturants[i];
			y[i] = intensities[i];
		}


		double[] AAndB = calculateAAndB(intensities);
		double A = AAndB[0];
		double B = AAndB[1];

		// Instantiate CHalfFunction and assign A and B (knowns)
		CHalfFunction f = new CHalfFunction();
		f.setA(A);
		f.setB(B);

		//inital step sizes
		double[] step = new double[2];
		step[0] = 0.01; //Chalf
		step[1] = 0.01; // b
		
		//set inital estimates for chalf and b
		//this will be changed later on
		double[] estimates = new double[2];
		estimates[0] = FFConstants.InitialCHalfValue; // Chalf
		estimates[1] = FFConstants.InitialBValue; // b, calculated using heuristics

		//setup the regression
		Regression reg = new Regression(x,y);
		reg.simplex(f,estimates, step);
		
		//contains the first best estimates
		// 0 index = C 1/2
		// 1 index = b
		double[] bestEstimates = reg.getBestEstimates();
		
		//redo regression with new best estimates as suggested by Flanagan
		//http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html#simplex
		estimates[0] = 2*estimates[0] - bestEstimates[0];
		estimates[1] = 2*estimates[1] - bestEstimates[1];
		
		//calculate the updated regression
		reg.simplex(f,estimates, step);

		double[] bestEstimatesSD = reg.getBestEstimatesStandardDeviations();

		double adjRSquared = reg.getAdjustedCoefficientOfDetermination();
		double chalf = bestEstimates[0];
		double b = bestEstimates[1];
		double chalfSD = bestEstimatesSD[0];
		double bSD = bestEstimatesSD[1];


		//makes the array containing, in order,
		//c1/2, c1/2 sd, b, b sd, adjrsq
		List<Double> calculatedRun = new ArrayList<Double>(){{
			add(chalf);
			add(chalfSD);
			add(b);
			add(bSD);
			add(adjRSquared);
		}};

		Double[] preConvertedRun = new Double[calculatedRun.size()];
		preConvertedRun = calculatedRun.toArray(preConvertedRun);
		double[] convertedRun = new double[preConvertedRun.length];
		for (int i = 0; i < preConvertedRun.length; i++){
			convertedRun[i] = preConvertedRun[i];
		}
		return convertedRun;
	}

	@Override
	protected SingleFit call() throws Exception {
		final double[] fIntensities = this.intensities;
		final double[] fDenaturants = this.denaturants;

		ArrayList<SingleFit> fitList = new ArrayList<SingleFit>();

		//add without removing intensities
		Double[] x = new Double[fDenaturants.length];
		Double[] y = new Double[fIntensities.length];

		for (int i = 0; i < x.length; i++){
			x[i] = fDenaturants[i];
			y[i] = fIntensities[i];
		}
		double[] AAndB = calculateAAndB(y);
		fitList.add(new SingleFit(
				this.calculateFit(y, x),-1,AAndB[0], AAndB[1]));

		//serially remove each value and recalculate fit
		for (int i = 0; i < this.intensities.length; i++){
			//populate new arrays
			ArrayList<Double> tempListIntensities = new ArrayList<Double>();
			Double[] newIntensities = new Double[this.intensities.length-1];
			ArrayList<Double> tempListDenaturants = new ArrayList<Double>();
			Double[] newDenaturants = new Double[this.intensities.length-1];
			for (int j = 0; j < this.intensities.length; j++){
				if (j == i) continue; //ignore the one we want to remove
				tempListIntensities.add(fIntensities[j]);
				tempListDenaturants.add(fDenaturants[j]);
			}
			newIntensities = tempListIntensities.toArray(newIntensities);
			newDenaturants = tempListDenaturants.toArray(newDenaturants);

			//Calculate A and B values
			AAndB = calculateAAndB(newIntensities);
			double A = AAndB[0];
			double B = AAndB[1];

			//calculate fit
			SingleFit fit = new SingleFit(this.calculateFit(newIntensities, newDenaturants), i, A, B);
			fitList.add(fit);
		}
		Collections.sort(fitList);
		return fitList.get(0); //return largest adjRsq
	}

	private double[] calculateAAndB(Double[] intensities2){
		double[] ret = new double[2];
		/*
		 * determine if curve is oxidized or not based on heuristics
		 * 
		 * Assume to be Non-Oxidized if first half of values average to be greater than the second half of values
		 */
		double A,B, secondHalfValuesSum;
		double firstHalfValuesSum = secondHalfValuesSum = 0;
		for (int i = 0; i <intensities2.length/2; i++){
			firstHalfValuesSum += intensities2[i];
		}
		//if odd, second loop contains value in middle
		for (int i = intensities2.length/2; i < intensities2.length; i++){
			secondHalfValuesSum += intensities2[i];
		}

		//if detectOx is not selected, default to nonOx = true
		boolean nonOx;
		if (!this.detectOx){
			nonOx = true;
		}
		else{
			//average of first half intensities > average of second half intensities. Accounts for the fact that we could have odd intensities
			nonOx = firstHalfValuesSum/(intensities2.length/2) >= secondHalfValuesSum/(intensities2.length-(intensities2.length/2));
		}

		if (nonOx){
			A = FFMath.max(intensities2);
			B = FFMath.min(intensities2);
		}
		else{
			A = FFMath.min(intensities2);
			B = FFMath.max(intensities2);
		}
		ret[0] = A;
		ret[1] = B;
		return ret;
	}		
}
