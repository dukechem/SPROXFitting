package statics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FFMath {

	/**
	 * Calculates the max value in a double array (I could generalize it, but not necessary)
	 * @param Double array
	 * @return max value in the array
	 */
	public static double max(double[] arr){
		double max = Double.MIN_VALUE;
		for (double d : arr){
			if (d > max){
				max = d;
			}
		}
		return max;
	}

	public static double max(Double[] arr) {
		double max = Double.MIN_VALUE;
		for (double d : arr){
			if (d > max){
				max = d;
			}
		}
		return max;
	}

	public static double calculatePercentile(double k, List<Double> vals){
		//make copy of vals
		List<Double> temp = new ArrayList<>(vals);
		//sort vals (in place sorting)
		Collections.sort(temp);
		//calculate the std dev using same algorithm as excel
		int bucket = (int) Math.ceil(k*0.01*temp.size()) -1 ;
		if (bucket > temp.size()){
			return temp.get(temp.size()-1);
		}
		else if (bucket < 0){
			return temp.get(0);
		}
		return temp.get(bucket);
	}
	
	/**
	 * Calculates the min value in a double array
	 * @param Double array
	 * @return min value in the array
	 */
	public static double min(double[] arr){
		double min = Double.MAX_VALUE;
		for (double d : arr){
			if (d < min){
				min = d;
			}
		}
		return min;
	}
	
	public static double min(Double[] arr){
		double min = Double.MAX_VALUE;
		for (double d : arr){
			if (d < min){
				min = d;
			}
		}
		return min;
	}
	
	public static void main(String[] args){
		System.out.println(FFMath.min(new double[]{1d,2d,3d,3.01d,2d, 1d}));
		
	}
}
