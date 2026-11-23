package it.unisa.telerehab.ui;

import edu.unisa.medsim.ClinicalProfileType;
import edu.unisa.medsim.PatientTelemetrySimulator;
import edu.unisa.medsim.TelemetrySample;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller of the Pre-Game "Hello world" window.
 *
 * <p>It proves that the view (FXML), the controller and the provided simulator
 * are wired together. It will be replaced by the real views in Sprint 1.</p>
 */
public class HelloController {

    private static final long DEMO_SEED = 42L;
    private static final int STEP_SECONDS = 1;
    private static final int DEMO_INTENSITY = 6;

    @FXML
    private Label sampleLabel;

    private PatientTelemetrySimulator simulator;

    @FXML
    private void initialize() {
        simulator = new PatientTelemetrySimulator(ClinicalProfileType.CARDIAC_REHAB, DEMO_SEED);
        show(simulator.getCurrentSample());
    }

    @FXML
    private void onStep() {
        show(simulator.step(STEP_SECONDS, DEMO_INTENSITY, false));
    }

    @FXML
    private void onReset() {
        simulator.reset();
        show(simulator.getCurrentSample());
    }

    private void show(TelemetrySample sample) {
        sampleLabel.setText(sample.toString());
    }
}
