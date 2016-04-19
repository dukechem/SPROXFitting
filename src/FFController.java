import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AbstractFFModel;
import models.FFModelDualSinglet;
import models.FFModelSinglet;
import statics.FFConstants;
import statics.TextFlowWriter;
import datasets.AbstractDataSet;
import datasets.DualSingletDataSet;
import datasets.SingletDataSet;

public class FFController implements Initializable {

	private static final boolean DEBUG = false;

	/*
	 * Injectible fields for FXML
	 */

	@FXML
	private Button SPROXBrowse, SPROX2Browse, DenaturantBrowse, AnalyzeButton;

	@FXML
	private TextField SPROXField, SPROX2Field, DenaturantField, MidPointValue,
			AdjustedRSquaredValue, DifferenceValueLower, DifferenceValueUpper;

	@FXML
	private Menu MidpointCriteria, AdjustedRSquaredCriteria,
			DifferenceCriteria;

	@FXML
	private CheckBox Graphs, dualExperiment, detectOxCheckBox,
			runPeptideAnalysis;

	@FXML
	private Group SPROXGroup, SPROX2Group;

	@FXML
	private MenuItem Exit, peptideLowerBound, peptideUpperBound;

	@FXML
	private TextFlow FFInfo;

	@FXML
	private ScrollPane FFInfoContainer;

	@FXML
	private ProgressBar progressBar;

	/*
	 * Variables needed for model functions
	 */
	private boolean detectOx = false; // psuedo bound to detectOxCheckBox
	private boolean generateGraphs = false;

	/*
	 * initial values displayed on check boxes
	 */

	private final String detectOxInitialMessage = "Not Detecting Ox Met";
	private final String detectOxChangedMessage = "Detecting Ox Met";
	private final boolean detectOxInitialCondition = false;

	private final String dualExperimentInitialMessage = "-/+ Ligand Experiment";
	private final String dualExperimentChangedMessage = "Single Experiment";
	private final boolean dualExperimentInitialCondition = true;

	private final String generateGraphsInitialMessage = "Not Generating Graphs";
	private final String generateGraphsChangedMessage = "Generating Graphs";
	private final boolean generateGraphsInitialCondition = false;

	private final String runPeptideAnalysisInitialMessage = "Running Difference Analysis";
	private final String runPeptideAnalysisChangedMessage = "Not Running Difference Analysis";
	private final boolean runPeptideAnalysisInitialCondition = true;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FFMain.setController(this);
		initTextFields();

		/**
		 * Add listeners and bindings
		 */

		/*
		 * Setup graphs
		 */
		FFCheckBoxChangeListener graphsListener = new FFCheckBoxChangeListener(
				generateGraphsInitialMessage, generateGraphsChangedMessage,
				generateGraphsInitialCondition);

		Graphs.setAllowIndeterminate(false);
		Graphs.setSelected(generateGraphsInitialCondition);
		Graphs.selectedProperty().addListener(graphsListener);
		Graphs.textProperty().bind(graphsListener.getStringProperty());

		/*
		 * Set indeterminate on detectOxCheckBox to false Bind changes in
		 * detectOxCheckBox to detectOx
		 */
		FFCheckBoxChangeListener detectOxChangeListener = new FFCheckBoxChangeListener(
				detectOxInitialMessage, detectOxChangedMessage,
				detectOxInitialCondition);

		detectOxCheckBox.setAllowIndeterminate(false);
		detectOxCheckBox.setSelected(detectOxInitialCondition);
		detectOxCheckBox.selectedProperty().addListener(detectOxChangeListener);
		detectOxCheckBox.textProperty().bind(
				detectOxChangeListener.getStringProperty());

		/*
		 * setup dual experiment check box
		 */
		FFCheckBoxChangeListener dualExperimentChangeListener = new FFCheckBoxChangeListener(
				dualExperimentInitialMessage, dualExperimentChangedMessage,
				dualExperimentInitialCondition);

		dualExperiment.setAllowIndeterminate(false);
		dualExperiment.setSelected(dualExperimentInitialCondition);
		dualExperiment.selectedProperty().addListener(
				dualExperimentChangeListener);
		dualExperiment.textProperty().bind(
				dualExperimentChangeListener.getStringProperty());

		/*
		 * set up peptide difference analysis check box
		 */
		FFCheckBoxChangeListener runPeptideAnalysisChangeListener = new FFCheckBoxChangeListener(
				runPeptideAnalysisInitialMessage,
				runPeptideAnalysisChangedMessage,
				runPeptideAnalysisInitialCondition);
		runPeptideAnalysis.setAllowIndeterminate(false);
		runPeptideAnalysis.setSelected(runPeptideAnalysisInitialCondition);
		runPeptideAnalysis.selectedProperty().addListener(runPeptideAnalysisChangeListener);
		runPeptideAnalysis.textProperty().bind(runPeptideAnalysisChangeListener.getStringProperty());
		
		/*
		 * add listeners to the peptide analysis text fields
		 */
		DifferenceValueLower.setText(String
				.valueOf(FFConstants.DIFFERENCE_HEURISTIC_LOWER));
		DifferenceValueLower.textProperty().addListener(
				new BoundDoubleTextFieldChangeListener(0, 100,
						DifferenceValueLower));

		DifferenceValueUpper.setText(String
				.valueOf(FFConstants.DIFFERENCE_HEURISTIC_UPPER));
		DifferenceValueUpper.textProperty().addListener(
				new BoundDoubleTextFieldChangeListener(0, 100,
						DifferenceValueUpper));

		/*
		 * Force FFInfo to scroll down on each append
		 */
		FFInfo.getChildren().addListener(
				(ListChangeListener<Node>) ((change) -> {
					FFInfo.layout();
					FFInfoContainer.layout();
					FFInfoContainer.setVvalue(1.0f);
				}));

		/*
		 * Disable analyze button if nothing in 1st Sprox / Denaturants
		 */
		AnalyzeButton.disableProperty().bind(
				Bindings.or(SPROXGroup.disabledProperty(), Bindings.or(
						SPROXField.textProperty().isEqualTo(""),
						DenaturantField.textProperty().isEqualTo(""))));

		/*
		 * Write Greeting
		 */
		TextFlowWriter.writeLine("", FFInfo);
		String name = System.getProperty("user.name");
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String version = System.getProperty("os.version");
		TextFlowWriter.writeInfo("Hello " + name, FFInfo);
		TextFlowWriter.writeInfo("I've detected you're running " + os + " ("
				+ version + ", " + arch + ")", FFInfo);

		if (DEBUG) {
			SPROXField
					.setText("/Users/jkarnuta/Desktop/10-16-12 manA Control Data.csv");
			DenaturantField.setText("/Users/jkarnuta/Desktop/manATags.csv");
			AnalyzeButton.fire();
		}
	}

	/*
	 * Exits the javaFX thread Called from Exit
	 */
	public void ExitOnAction() {
		FFMain.exit();
	}

	/**
	 * TEXT FIELD METHODS
	 */

	private void initTextFields() {
		initSPROXField();
		initDenaturantsField();
	}

	private void initSPROXField() {
		setupDragOver(SPROXField);
		setupDragDrop(SPROXField);
	}

	private void initDenaturantsField() {
		setupDragOver(DenaturantField);
		setupDragDrop(DenaturantField);
	}

	private void setupDragOver(TextField tf) {
		tf.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (event.getGestureSource() != tf
						&& event.getDragboard().hasFiles()) {
					event.acceptTransferModes(TransferMode.LINK);
				}
				event.consume();
			}
		});
	}

	private void setupDragDrop(TextField tf) {
		tf.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasFiles() && db.getFiles().size() == 1) {
					tf.setText(db.getFiles().get(0).getAbsolutePath());
					success = true;
				}
				event.setDropCompleted(success);
				event.consume();
			}
		});
	}

	public void SPROXOnDragEntered() {
		TextFieldOnDragEntered(SPROXField);
	}

	public void SPROXOnDragExited() {
		TextFieldOnDragExited(SPROXField);
	}

	public void SPROX2OnDragEntered() {
		TextFieldOnDragEntered(SPROX2Field);
	}

	public void SPROX2OnDragExited() {
		TextFieldOnDragExited(SPROX2Field);
	}

	public void DenaturantOnDragEntered() {
		TextFieldOnDragEntered(DenaturantField);
	}

	public void DenaturantOnDragExited() {
		TextFieldOnDragExited(DenaturantField);
	}

	private void TextFieldOnDragEntered(TextField tf) {
		tf.setStyle("-fx-border-color:#235F9C;-fx-border-style:solid;");
	}

	private void TextFieldOnDragExited(TextField tf) {
		tf.setStyle("-fx-border-style:none;-fx-border-color: transparent;");
	}

	/**
	 * TEXTFIELD BUTTON METHODS
	 */
	public void SPROXButtonOnAction() {
		fetchFile(SPROXField, "Select SPROX csv");
	}

	public void SPROX2ButtonOnAction() {
		fetchFile(SPROX2Field, "Select SPROX csv for Comparison");
	}

	public void DenaturantButtonOnAction() {
		fetchFile(DenaturantField, "Select Denaturants File");
	}

	private void fetchFile(TextField tf, String title) {
		Stage fcStage = new Stage();
		fcStage.initModality(Modality.APPLICATION_MODAL);
		// fcStage.initOwner(stage.getScene().getWindow());

		FileChooser fc = new FileChooser();
		fc.setTitle(title);
		fc.setInitialDirectory(new File(System.getProperty("user.dir")));
		File chosenFile = fc.showOpenDialog(fcStage);

		if (chosenFile != null) {
			tf.setText(chosenFile.getAbsolutePath());
		}

	}

	/*
	 * Reset bindings
	 */

	/*
	 * Analyze On Action
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void AnalyzeOnAction() {

		/*
		 * Clear TextFlow
		 */

		TextFlowWriter.clear(FFInfo);

		/*
		 * Build constants for constructors
		 */
		boolean genGraphs = Graphs.isSelected();
		String SPROX1 = SPROXField.getText();
		String denaturants = DenaturantField.getText();

		/*
		 * Set values for heuristics
		 */
		FFConstants.setAdjustedRSquaredHeuristic(AdjustedRSquaredValue
				.getText());
		FFConstants.setMidPointHeuristic(MidPointValue.getText());
		FFConstants.setDifferenceHeuristic(DifferenceValueLower.getText(),
				DifferenceValueUpper.getText());
		FFConstants.setRunPeptideAnalysis(runPeptideAnalysis.isSelected());

		AbstractFFModel model;
		AbstractDataSet dataset;
		// Compare two files of type Singlets
		// Not Implemented

		// Compare two experiments in one file of type Doublet
		if (dualExperiment.isSelected()) {
			model = new FFModelDualSinglet(SPROX1, denaturants, FFInfo,
					genGraphs);
			dataset = new DualSingletDataSet(SPROX1, denaturants, FFInfo,
					detectOx);
		}

		// Default option
		else {
			model = new FFModelSinglet(SPROX1, denaturants, FFInfo, genGraphs);
			dataset = new SingletDataSet(SPROX1, denaturants, FFInfo, detectOx);
		}
		/*
		 * Add bindings for communication between model and controller
		 */
		SPROXGroup.setDisable(true);
		model.runningProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue,
					Object newValue) {
				SPROXGroup.setDisable((boolean) newValue);
			}
		});

		progressBar.progressProperty().bind(model.progressProperty());
		model.load(dataset);
		FFMain.loadAndStart(model);
	}

	/**
	 * Getter Methods
	 */

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public boolean getDetectOx() {
		return detectOx;
	}

	public boolean getGenerateGraphs() {
		return generateGraphs;
	}

	private class BoundDoubleTextFieldChangeListener implements
			ChangeListener<String> {

		private final double lowerBound;
		private final double upperBound;
		private final String errorMessage = "Percentiles must be between 0 and 100";
		private final TextField tf;

		// add upper and lower bound
		public BoundDoubleTextFieldChangeListener(double lower, double upper,
				TextField tf) {
			lowerBound = lower;
			upperBound = upper;
			this.tf = tf;
		}

		@Override
		public void changed(ObservableValue<? extends String> observable,
				String oldValue, String newValue) {
			// if empty do nothing
			if (newValue.length() == 0) {
				return;
			}
			double newVal = 0;
			try {
				newVal = Double.parseDouble(newValue);
			} catch (NumberFormatException nfe) {
				TextFlowWriter.writeError(errorMessage, FFInfo);
				tf.setText(oldValue);
				return;
			}
			if (!(newVal >= lowerBound) || !(newVal <= upperBound)) {
				TextFlowWriter.writeError(errorMessage, FFInfo);
				tf.setText(oldValue);
			}
		}
	}

	private class FFCheckBoxChangeListener implements ChangeListener<Boolean> {
		// initialValue - final
		private final boolean initialValue;
		// default message
		private final String defaultMessage;
		// message displayed when changed
		private final String changedMessage;

		// text property to update
		private StringProperty textProperty;

		/**
		 * 
		 * @param defaultMessage
		 *            initial displayed message
		 * @param changedMessage
		 *            message displayed when changed
		 * @param initialValue
		 *            initial value of the check box
		 * @param checkBox
		 *            checkbox object to change
		 */
		public FFCheckBoxChangeListener(String defaultMessage,
				String changedMessage, boolean initialValue) {
			// sole constructor
			this.initialValue = initialValue;
			this.defaultMessage = defaultMessage;
			this.changedMessage = changedMessage;

			this.textProperty = new SimpleStringProperty(defaultMessage);
		}

		@Override
		public void changed(ObservableValue<? extends Boolean> observable,
				Boolean oldValue, Boolean newValue) {
			// changed
			if (initialValue != newValue) {
				textProperty.set(changedMessage);
			}

			// initialValue == newVal
			// state returns to default
			else {
				textProperty.set(defaultMessage);
			}
		}

		public StringProperty getStringProperty() {
			return textProperty;
		}

	}
}
