package models;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javafx.concurrent.Task;

/**
 * Saves the header and ArrayList<String[]> parameters to a new file File is
 * located INSIDE folder, saved as CalculatedParameters.csv
 * 
 * @author jkarnuta
 *
 */
public class FFModelSave extends Task<String> {

	private final List<String[]> headers;
	private final List<String[]> runs;
	private final String directoryPath;
	private static final String APPEND_FILE_NAME = "CalculatedParameters.csv";

	public FFModelSave(AbstractFFModel model) {
		this.headers = model.data.getHeaders1();
		this.runs = model.data.getRuns1();
		this.directoryPath = model.getSuperPath();
	}

	@Override
	public String call() {
		return save();
	}

	public String save() {
		File newFile = new File(this.directoryPath + APPEND_FILE_NAME);
		long totalIterations = this.runs.size() - 1; // ignoring header
		long currentIteration = 0;

		try {
			FileWriter fw = new FileWriter(newFile);

			// write header
			for (String[] ele : this.headers) {
				fw.write(stringArrayToCSV(ele));
				fw.write("\n");
			}

			// loop through runs and write each to file
			for (String[] run : runs) {
				fw.write(stringArrayToCSV(run));
				fw.write("\n");
				currentIteration++;
				updateProgress(currentIteration, totalIterations);
			}

			fw.flush();
			fw.close();

		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();

		}

		return "";
	}

	/**
	 * Converts a string array into a CSV readable string String[]{"x","y","z"}
	 * ==> "x,y,z"
	 * 
	 * @param array
	 * @return CSV'd string
	 */
	private String stringArrayToCSV(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (String ele : array) {
			sb.append("," + ele);
		}
		return (sb.length() > 1) ? sb.substring(1) : sb.toString(); // clip the
																	// first
																	// comma
	}
}
