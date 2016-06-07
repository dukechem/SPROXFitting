import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
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

	public static final boolean DEBUG = false;

	/*
	 * Injectible fields for FXML
	 */

	@FXML
	private Button SPROXBrowse, SPROX2Browse, DenaturantBrowse, AnalyzeButton;

	@FXML
	private TextField SPROXField, SPROX2Field, DenaturantField, MidPointValue,
			AdjustedRSquaredValue, DifferenceValueLower, DifferenceValueUpper,
			AValue, BValue;

	@FXML
	private Menu MidpointCriteria, AdjustedRSquaredCriteria,
			DifferenceCriteria;

	@FXML
	private CheckBox Graphs, dualExperiment, detectOxCheckBox,
			runPeptideAnalysis, calculateAAndB;

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

		Graphs.setAllowIndeterminate(false);

		/*
		 * Set indeterminate on detectOxCheckBox to false Bind changes in
		 * detectOxCheckBox to detectOx
		 */
		detectOxCheckBox.setAllowIndeterminate(false);

		/*
		 * setup dual experiment check box
		 */
		dualExperiment.setAllowIndeterminate(false);

		/*
		 * set up peptide difference analysis check box
		 */
		runPeptideAnalysis.setAllowIndeterminate(false);

		/*
		 * add listeners to the peptide analysis text fields
		 */
		DifferenceValueLower.setText(String
				.valueOf(FFConstants.DIFFERENCE_HEURISTIC_LOWER));
		DifferenceValueLower.textProperty().addListener(
				new PercentileDoubleChangeListener(DifferenceValueLower));

		DifferenceValueUpper.setText(String
				.valueOf(FFConstants.DIFFERENCE_HEURISTIC_UPPER));
		DifferenceValueUpper.textProperty().addListener(
				new PercentileDoubleChangeListener(DifferenceValueUpper));

		/*
		 * add listeners to the transition parameters text fields
		 */
		AValue.textProperty().addListener(
				new TransitionDoubleChangeListener(AValue));
		BValue.textProperty().addListener(
				new TransitionDoubleChangeListener(BValue));

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
		FFConstants.setCalculateAB(calculateAAndB.isSelected());
		//must set at least of {AValue, BValue}
		if(!FFConstants.CALCULATE_A_B){
			//if AValue is entered, set
			if (!"".equals(AValue.getText())){
				FFConstants.setAValue(Double.parseDouble(AValue.getText().trim()));
			}
			//if BValue is entered, set
			if (!"".equals(BValue.getText())){
				FFConstants.setBValue(Double.parseDouble(BValue.getText().trim()));
			}
		}

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

	/**
	 * Provides validation to ensure that only proper values (between 0 and 100)
	 * are allowed
	 * 
	 * @author jkarnuta
	 *
	 */
	private class PercentileDoubleChangeListener extends
			BoundDoubleTextFieldChangeListener {
		private static final String percentileErrorMessage = "Percentiles must be between 0 and 100";

		public PercentileDoubleChangeListener(TextField tf) {
			super(0, 100, tf, percentileErrorMessage);
		}
	}

	private class TransitionDoubleChangeListener extends
			BoundDoubleTextFieldChangeListener {
		private static final String transitionErrorMessage = "Transition value must be > 0";

		public TransitionDoubleChangeListener(TextField tf) {
			super(Double.MIN_NORMAL, Double.MAX_VALUE, tf,
					transitionErrorMessage);
		}
	}

	private class BoundDoubleTextFieldChangeListener implements
			ChangeListener<String> {

		private final double lowerBound;
		private final double upperBound;
		private final String errorMessage;
		private final TextField tf;

		// add upper and lower bound
		public BoundDoubleTextFieldChangeListener(double lower, double upper,
				TextField tf, String errorMessage) {
			lowerBound = lower;
			upperBound = upper;
			this.errorMessage = errorMessage;
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
}
