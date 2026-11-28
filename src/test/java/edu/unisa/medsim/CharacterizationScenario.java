package edu.unisa.medsim;

import java.util.ArrayList;
import java.util.List;

/**
 * Fixed scenario used by the characterization (golden master) tests.
 *
 * <p>It is shared by the generator, which records the current behaviour of the
 * simulator, and by the test, which replays the same scenario after the refactoring.</p>
 */
final class CharacterizationScenario {

    /** Seed that gives a nominal session (first draw 0.727, below the 0.85 threshold). */
    static final long NOMINAL_SEED = 42L;

    /** First seed that gives a perturbed session: seeds 0..2047 never exceed 0.85. */
    static final long PERTURBED_SEED = 2048L;

    static final long[] SEEDS = {NOMINAL_SEED, PERTURBED_SEED};

    /** One simulation step: duration, intensity, resting flag. */
    record Step(int deltaSeconds, int intensity, boolean resting) { }

    /** 36 mixed steps: warm-up, increasing load, peak, pause, second effort, recovery. */
    static final List<Step> STEPS = buildSteps();

    private CharacterizationScenario() {
    }

    private static List<Step> buildSteps() {
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            steps.add(new Step(5, 2, false));          // warm-up
        }
        for (int intensity = 3; intensity <= 10; intensity++) {
            steps.add(new Step(10, intensity, false)); // increasing load up to the maximum
        }
        for (int i = 0; i < 4; i++) {
            steps.add(new Step(15, 10, false));        // peak, long steps
        }
        for (int i = 0; i < 6; i++) {
            steps.add(new Step(10, 7, true));          // pause: intensity must be ignored
        }
        for (int i = 0; i < 6; i++) {
            steps.add(new Step(1 + i * 4, 6, false));  // second effort, varying deltas
        }
        for (int i = 0; i < 6; i++) {
            steps.add(new Step(20, 0, true));          // final recovery
        }
        return List.copyOf(steps);
    }

    /** Runs the scenario and returns one CSV line per step. */
    static List<String> run(ClinicalProfileType profile, long seed) {
        PatientTelemetrySimulator simulator = new PatientTelemetrySimulator(profile, seed);
        List<String> lines = new ArrayList<>();
        for (Step step : STEPS) {
            TelemetrySample s = simulator.step(step.deltaSeconds(), step.intensity(), step.resting());
            lines.add(s.getElapsedSeconds() + ";" + s.getHeartRateBpm() + ";" + s.getSpo2Percentage()
                    + ";" + s.getFatigueIndex() + ";" + s.getPainScore());
        }
        return lines;
    }

    static String fileName(ClinicalProfileType profile, long seed) {
        return profile.name() + "_seed" + seed + ".csv";
    }
}