package containers;



public class SingleFit implements Comparable<SingleFit>{
	public final double[] fittedParameters;
	public final double A;
	public final double B;
	public final int removedValue;
	public SingleFit(double[] arr, int removedValue, double A, double B){
		this.fittedParameters = arr;
		this.removedValue = removedValue;
		this.A=A;
		this.B=B;
	}
	
	@Override
	public int compareTo(SingleFit o) {
		double thisRsq = this.fittedParameters[4];
		double thatRsq = o.fittedParameters[4];

		boolean lg = thisRsq < thatRsq;
		boolean eq = thisRsq == thatRsq;
		if(eq) return (this.removedValue < o.removedValue) ? 1 : -1;
		return lg ? 1 : -1;
	}
	
	public String toString(){
		String s = "";
		s += ("#Removed: "+removedValue+"\n");
		s += ("adjRsq: "+fittedParameters[4]+"\n");
		return s;
	}
}