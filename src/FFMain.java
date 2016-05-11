import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import containers.*;
import models.*;

/**
 * 
 * @author jkarnuta FitzFitting (known commercially as SPROXFitting), is the
 *         data analytics platform for SPROX experiments. This software is
 *         distributed without guarantee and should be used in an academic or
 *         scientific setting solely for the analysis of Stability of Proteins
 *         via Rates of OXidation (SPROX) experiments.
 * 
 *         The main author, Jaret Karnuta, can be reached via the Fitzgerald
 *         group at Duke University. Jaret is currently a medical student at the
 *         Cleveland Clinic Lerner College of Medicine.
 */
public class FFMain extends Application {

	/* Constants */
	public static final String VERSION = "3.0";

	public static Parent root;
	public static Stage stage;

	private static FFController controller;

	@Override
	public void start(Stage primaryStage) throws IOException {
		// TODO Auto-generated method stub

		Font.loadFont(FFMain.class.getResourceAsStream("expressway.ttf"), 12);
		stage = primaryStage;
		root = FXMLLoader.load(getClass().getResource("FFLayout.fxml"));
		Scene scene = new Scene(root);
		stage.setTitle("FitzFitting SPROX Analysis v" + VERSION);
		stage.setScene(scene);
		stage.getIcons().add(new Image("/images/SPROXFitting.png"));
		stage.setResizable(false);
		stage.show();
	}

	public static void loadAndStart(AbstractFFModel model) {
		/*
		 * Perform preliminary data checks, loads CSVs, and digests
		 */
		Thread modelThread = new Thread() {
			public void run() {
				// status on loading AbstractDataSet.load() and constructor
				String modelErrorMessage = model.getErrorMessage();

				if (!"".equals(modelErrorMessage)) {
					model.writeError("Error calculating model");
					model.writeError("Message returned:");
					model.writeError(modelErrorMessage);
					
					System.err.println("Terminating");
					controller.getProgressBar().progressProperty().unbind();
					model.terminate();
				}

				// If loaded, write message
				model.writeLoadedMessage();
				model.start(); //start model thread
				modelErrorMessage = model.getErrorMessage(); //initial message
				if ("".equals(modelErrorMessage)) { // No Error calculating data for model
					model.save();
					if (model.getGenerateGraphsStatus()) {
						model.generateGraphs();
					}
					model.generateHTML();
					model.generateHistograms();
					model.writeCompleted();
				} else // on calculation error
				{
					model.writeError("\nError calculating model");
					model.writeError("Message returned:");
					model.writeError(modelErrorMessage);
					controller.getProgressBar().progressProperty().unbind();
					System.err.println("Terminating");
				}
				model.terminate();
			}
		};

		modelThread.start();
	}

	public static void exit() {
		Platform.exit();
		System.exit(0);
	}

	// set FFController instance to controller
	public static void setController(FFController controller) {
		FFMain.controller = controller;
	}

	// Backup
	public static void main(String[] args) {
		launch(args);
	}
}
