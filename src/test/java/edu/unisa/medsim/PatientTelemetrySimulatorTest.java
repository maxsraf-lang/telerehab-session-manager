package edu.unisa.medsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Characterization tests verifying nominal deterministic trajectories,
 * clinical profile differences, and session-level stochastic perturbation (> 0.85).
 */
class PatientTelemetrySimulatorTest {

    @Test
    void testDeterministicTrajectoryAndCardiacDrift() {
        // Seed 42L -> initial nextDouble() == 0.7276 (<= 0.85, nominal session)
        PatientTelemetrySimulator sim1 = new PatientTelemetrySimulator(ClinicalProfileType.CARDIAC_REHAB, 42L);
        PatientTelemetrySimulator sim2 = new PatientTelemetrySimulator(ClinicalProfileType.CARDIAC_REHAB, 42L);

        TelemetrySample last1 = null;
        TelemetrySample last2 = null;

        for (int t = 0; t < 20; t++) {
            last1 = sim1.step(1, 8, false);
            last2 = sim2.step(1, 8, false);
        }

        assertNotNull(last1);
        assertEquals(20, last1.getElapsedSeconds());
        assertEquals(last1.getHeartRateBpm(), last2.getHeartRateBpm(), 1e-6);
        assertEquals(last1.getSpo2Percentage(), last2.getSpo2Percentage(), 1e-6);
        assertEquals(last1.getFatigueIndex(), last2.getFatigueIndex(), 1e-6);
        assertTrue(last1.getHeartRateBpm() > 135.0, "Cardiac rehab patient should exhibit steep HR rise");
    }

    @Test
    void testPulmonaryCopdDesaturationUnderLoad() {
        PatientTelemetrySimulator copdSim = new PatientTelemetrySimulator(ClinicalProfileType.PULMONARY_COPD, 100L);
        PatientTelemetrySimulator athleticSim = new PatientTelemetrySimulator(ClinicalProfileType.ATHLETIC, 100L);

        TelemetrySample copdSample = null;
        TelemetrySample athleticSample = null;

        for (int t = 0; t < 30; t++) {
            copdSample = copdSim.step(1, 7, false);
            athleticSample = athleticSim.step(1, 7, false);
        }

        assertNotNull(copdSample);
        assertNotNull(athleticSample);
        assertTrue(copdSample.getSpo2Percentage() < 92.0, "COPD profile should desaturate under sustained exertion");
        assertTrue(copdSample.getSpo2Percentage() < athleticSample.getSpo2Percentage());
    }

    @Test
    void testPerturbedSessionWhenInitialRandomDrawExceedsThreshold() {
        // Seed 42L -> initial nextDouble() = 0.7276 (<= 0.85, nominal)
        // Seed 9999L -> initial nextDouble() = 0.8815 (> 0.85, perturbed)
        PatientTelemetrySimulator nominalSim = new PatientTelemetrySimulator(ClinicalProfileType.PULMONARY_COPD, 42L);
        PatientTelemetrySimulator perturbedSim = new PatientTelemetrySimulator(ClinicalProfileType.PULMONARY_COPD, 9999L);
        PatientTelemetrySimulator perturbedReplaySim = new PatientTelemetrySimulator(ClinicalProfileType.PULMONARY_COPD, 9999L);
        
        TelemetrySample nominal = null;
        TelemetrySample perturbed = null;
        TelemetrySample perturbedReplay = null;

        for (int t = 0; t < 20; t++) {
            nominal = nominalSim.step(1, 6, false);
            perturbed = perturbedSim.step(1, 6, false);
            perturbedReplay = perturbedReplaySim.step(1, 6, false);
        }

        assertNotNull(nominal);
        assertNotNull(perturbed);
        assertNotNull(perturbedReplay);
        assertEquals(perturbed.getHeartRateBpm(), perturbedReplay.getHeartRateBpm(), 1e-6);
        assertEquals(perturbed.getSpo2Percentage(), perturbedReplay.getSpo2Percentage(), 1e-6);
        assertEquals(perturbed.getFatigueIndex(), perturbedReplay.getFatigueIndex(), 1e-6);
        assertTrue(perturbed.getHeartRateBpm() > nominal.getHeartRateBpm() + 9.0,
                "Perturbed session should exhibit elevated HR compared to nominal session");
        assertTrue(perturbed.getSpo2Percentage() < nominal.getSpo2Percentage() - 3.0,
                "Perturbed session should exhibit lower SpO2 than nominal session");
        assertTrue(perturbed.getFatigueIndex() > nominal.getFatigueIndex() + 1.5,
                "Perturbed session should accumulate fatigue faster than nominal session");
    }

    @Test
    void testRecoveryAndResetBehavior() {
        PatientTelemetrySimulator sim = new PatientTelemetrySimulator(ClinicalProfileType.FRAGILE_ELDERLY, 7L);

        for (int t = 0; t < 15; t++) {
            sim.step(1, 6, false);
        }
        double peakHr = sim.getCurrentSample().getHeartRateBpm();

        for (int t = 0; t < 30; t++) {
            sim.step(1, 0, true);
        }
        double recoveredHr = sim.getCurrentSample().getHeartRateBpm();
        assertTrue(recoveredHr < peakHr, "Heart rate should decrease during rest phase");

        sim.reset();
        assertEquals(0, sim.getCurrentSample().getElapsedSeconds());
        assertEquals(68.0, sim.getCurrentSample().getHeartRateBpm(), 1e-6);
        assertEquals(0.0, sim.getCurrentSample().getFatigueIndex(), 1e-6);
    }

    @Test
    void testInvalidInputsThrowException() {
        assertThrows(NullPointerException.class, () -> new PatientTelemetrySimulator(null, 1L));
        assertThrows(IllegalArgumentException.class,
                () -> new PatientTelemetrySimulator(ClinicalProfileType.ATHLETIC, 250.0, 98.0, 1L));

        PatientTelemetrySimulator sim = new PatientTelemetrySimulator(ClinicalProfileType.ATHLETIC, 1L);
        assertThrows(IllegalArgumentException.class, () -> sim.step(0, 5, false));
        assertThrows(IllegalArgumentException.class, () -> sim.step(1, 11, false));
    }
}