package containers;


public class GraphStatus {

	
	private int runNumber;
	private String message;
	
	public GraphStatus(int number, String message){
		this.runNumber = number;
		this.message =message;
	}
	
	public int getNumber(){
		return this.runNumber;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String toString(){
		return "<GraphStatus>:\nNumber: "+this.runNumber+"\nMessage: " +this.message;
	}
}
