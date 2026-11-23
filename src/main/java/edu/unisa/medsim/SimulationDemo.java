package edu.unisa.medsim;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Console demonstration showing how {@link PatientTelemetrySimulator} generates
 * physiological telemetry across clinical profiles, comparing a nominal session
 * (initial draw <= 0.85) with a stochastically perturbed session (initial draw > 0.85).
 */
public class SimulationDemo {

    private static final long NOMINAL_SEED = 42L;    // first nextDouble() = 0.7276 (<= 0.85)
    private static final long PERTURBED_SEED = 9999L; // first nextDouble() = 0.8815 (> 0.85)
    private static final int STEP_SECONDS = 5;

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("  MEDSIM - Patient Telemetry Simulator Demo");
        System.out.println("=========================================================================\n");

        // 1. Nominal session trace (seed 42 -> random draw <= 0.85)
        runSingleProfileTrace(ClinicalProfileType.PULMONARY_COPD, NOMINAL_SEED, "Nominal Session");

        // 2. Perturbed session trace (seed 9999 -> random draw > 0.85)
        runSingleProfileTrace(ClinicalProfileType.PULMONARY_COPD, PERTURBED_SEED, "Perturbed Session");

        // 3. Side-by-side comparison across all clinical behavioral profiles
        runComparativeSession();
    }

    private static void runSingleProfileTrace(ClinicalProfileType profile, long seed, String label) {
        System.out.printf("--- %s: %s (Seed: %d) ---%n", label, profile, seed);
        PatientTelemetrySimulator simulator = new PatientTelemetrySimulator(profile, seed);
        System.out.println("Initial state: " + simulator.getCurrentSample());

        System.out.println("\n[Phase 1: Warm-up - 15s, Intensity 3/10]");
        for (int i = 0; i < 3; i++) {
            System.out.println("  " + simulator.step(STEP_SECONDS, 3, false));
        }

        System.out.println("\n[Phase 2: Moderate Exertion - 25s, Intensity 6/10]");
        for (int i = 0; i < 5; i++) {
            System.out.println("  " + simulator.step(STEP_SECONDS, 6, false));
        }

        System.out.println("\n[Phase 3: Recovery Rest - 20s, Resting]");
        for (int i = 0; i < 4; i++) {
            System.out.println("  " + simulator.step(STEP_SECONDS, 0, true));
        }
        System.out.println();
    }

    private static void runComparativeSession() {
        System.out.println("--- Multi-Profile Comparison (60s Protocol, Seed: " + NOMINAL_SEED + ") ---");
        Map<ClinicalProfileType, PatientTelemetrySimulator> simulators = new LinkedHashMap<>();
        for (ClinicalProfileType type : ClinicalProfileType.values()) {
            simulators.put(type, new PatientTelemetrySimulator(type, NOMINAL_SEED));
        }

        System.out.printf("%-6s | %-10s | %-18s | %-18s | %-18s | %-18s%n",
                "Time", "Phase", "ATHLETIC", "CARDIAC_REHAB", "FRAGILE_ELDERLY", "PULMONARY_COPD");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        for (int step = 1; step <= 12; step++) {
            String phase;
            int intensity;
            boolean isResting;

            if (step <= 3) {
                phase = "WARM-UP";
                intensity = 3;
                isResting = false;
            } else if (step <= 8) {
                phase = "WORKLOAD";
                intensity = 8;
                isResting = false;
            } else {
                phase = "REST";
                intensity = 0;
                isResting = true;
            }

            Map<ClinicalProfileType, TelemetrySample> samples = new LinkedHashMap<>();
            for (Map.Entry<ClinicalProfileType, PatientTelemetrySimulator> entry : simulators.entrySet()) {
                samples.put(entry.getKey(), entry.getValue().step(STEP_SECONDS, intensity, isResting));
            }

            int currentTime = step * STEP_SECONDS;
            System.out.printf("%4ds  | %-10s | %s | %s | %s | %s%n",
                    currentTime,
                    phase,
                    formatCompact(samples.get(ClinicalProfileType.ATHLETIC)),
                    formatCompact(samples.get(ClinicalProfileType.CARDIAC_REHAB)),
                    formatCompact(samples.get(ClinicalProfileType.FRAGILE_ELDERLY)),
                    formatCompact(samples.get(ClinicalProfileType.PULMONARY_COPD)));
        }
    }

    private static String formatCompact(TelemetrySample s) {
        return String.format("%5.1fbpm %4.1f%% F:%.1f",
                s.getHeartRateBpm(), s.getSpo2Percentage(), s.getFatigueIndex());
    }
}