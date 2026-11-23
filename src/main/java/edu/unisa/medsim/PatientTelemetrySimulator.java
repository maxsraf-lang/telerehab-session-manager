package edu.unisa.medsim;

import java.util.Objects;
import java.util.Random;

/**
 * Discrete-time physiological telemetry simulator for telerehabilitation sessions.
 *
 * <p>At the start of each session ({@link #reset()}), the simulator draws a uniform
 * random value in {@code [0.0, 1.0)}. If the draw exceeds {@code 0.85}, the session
 * exhibits perturbed physiological responses deviating from the profile's nominal
 * Gaussian expectations.</p>
 */
public class PatientTelemetrySimulator {

    private static final double MIN_HR_BPM = 40.0;
    private static final double MAX_HR_BPM = 220.0;
    private static final double MIN_SPO2 = 70.0;
    private static final double MAX_SPO2 = 100.0;
    private static final double MIN_FATIGUE = 0.0;
    private static final double MAX_FATIGUE = 10.0;
    private static final int MIN_INTENSITY = 0;
    private static final int MAX_INTENSITY = 10;

    private static final double PERTURBATION_PROBABILITY_THRESHOLD = 0.85;
    private static final double HYPOXIA_THRESHOLD_SPO2 = 92.0;
    private static final double HYPOXIA_FATIGUE_FACTOR = 0.015;
    private static final double PAIN_FATIGUE_THRESHOLD = 7.5;
    private static final double PAIN_RECOVERY_FATIGUE_THRESHOLD = 5.0;

    private final ClinicalProfileType profileType;
    private final double baselineHr;
    private final double baselineSpo2;
    private final long seed;
    private final Random random;

    private boolean perturbedSession;
    private int elapsedSeconds;
    private double currentHr;
    private double currentSpo2;
    private double currentFatigue;
    private int currentPain;

    /**
     * Creates a simulator with default clinical baselines for the specified profile.
     *
     * @param profileType the clinical behavioral profile of the simulated patient
     * @param seed        random seed for simulation reproducibility
     */
    public PatientTelemetrySimulator(ClinicalProfileType profileType, long seed) {
        this(profileType, defaultBaselineHr(profileType), defaultBaselineSpo2(profileType), seed);
        
    }

    /**
     * Creates a simulator with custom resting baseline parameters.
     *
     * @param profileType  the clinical behavioral profile of the simulated patient
     * @param baselineHr   resting heart rate in BPM (must be within [40, 220])
     * @param baselineSpo2 resting oxygen saturation percentage (must be within [70, 100])
     * @param seed         random seed for simulation reproducibility
     */
    public PatientTelemetrySimulator(ClinicalProfileType profileType, double baselineHr,
                                     double baselineSpo2, long seed) {
        this.profileType = Objects.requireNonNull(profileType, "profileType must not be null");
        if (baselineHr < MIN_HR_BPM || baselineHr > MAX_HR_BPM) {
            throw new IllegalArgumentException("baselineHr out of physiological bounds");
        }
        if (baselineSpo2 < MIN_SPO2 || baselineSpo2 > MAX_SPO2) {
            throw new IllegalArgumentException("baselineSpo2 out of physiological bounds");
        }
        this.baselineHr = baselineHr;
        this.baselineSpo2 = baselineSpo2;
        this.seed = seed;
        this.random = new Random(seed);
        reset();
    }

    /**
     * Starts/resets a session: resets the clock and vital signs to resting baselines,
     * and draws a random value in [0, 1) to determine whether this session behaves
     * nominally (<= 0.85) or generates perturbed telemetry (> 0.85).
     */
    public final void reset() {
        this.random.setSeed(this.seed);
        this.perturbedSession = this.random.nextDouble() > PERTURBATION_PROBABILITY_THRESHOLD;
        this.elapsedSeconds = 0;
        this.currentHr = this.baselineHr;
        this.currentSpo2 = this.baselineSpo2;
        this.currentFatigue = MIN_FATIGUE;
        this.currentPain = 0;
    }

    /**
     * Advances the simulation by {@code deltaSeconds} under the given workload.
     *
     * @param deltaSeconds      time step duration in seconds (must be > 0)
     * @param exerciseIntensity prescribed workload intensity on a 0..10 scale
     * @param isResting         {@code true} if the patient is in a recovery/pause phase
     * @return an immutable {@link TelemetrySample} reflecting the updated vital signs
     */
    public TelemetrySample step(int deltaSeconds, int exerciseIntensity, boolean isResting) {
        if (deltaSeconds <= 0) {
            throw new IllegalArgumentException("deltaSeconds must be positive");
        }
        if (exerciseIntensity < MIN_INTENSITY || exerciseIntensity > MAX_INTENSITY) {
            throw new IllegalArgumentException("exerciseIntensity must be between 0 and 10");
        }

        int effectiveIntensity = isResting ? 0 : exerciseIntensity;

        double targetHr;
        double hrAdaptationRate;
        double targetSpo2;
        double spo2AdaptationRate;
        double fatigueRatePerSec;
        double noiseAmplitude;

        
        switch (profileType) {
            case ATHLETIC:
                targetHr = baselineHr + (effectiveIntensity * 8.5);
                hrAdaptationRate = isResting ? 0.10 : 0.08;
                targetSpo2 = baselineSpo2 - (effectiveIntensity * 0.05);
                spo2AdaptationRate = 0.15;
                fatigueRatePerSec = isResting ? -0.06 : (0.008 * effectiveIntensity);
                noiseAmplitude = 0.4;
                break;

            case CARDIAC_REHAB:
                targetHr = baselineHr + (effectiveIntensity * 11.5);
                hrAdaptationRate = isResting ? 0.035 : 0.12;
                targetSpo2 = baselineSpo2 - (effectiveIntensity * 0.18);
                spo2AdaptationRate = 0.08;
                fatigueRatePerSec = isResting ? -0.03 : (0.018 * effectiveIntensity);
                noiseAmplitude = 0.7;
                break;

            case FRAGILE_ELDERLY:
                targetHr = baselineHr + (effectiveIntensity * 6.5);
                hrAdaptationRate = isResting ? 0.03 : 0.05;
                targetSpo2 = baselineSpo2 - (effectiveIntensity * 0.22);
                spo2AdaptationRate = 0.06;
                fatigueRatePerSec = isResting ? -0.025 : (0.024 * effectiveIntensity);
                noiseAmplitude = 0.6;
                break;

            case PULMONARY_COPD:
                targetHr = baselineHr + (effectiveIntensity * 9.5);
                hrAdaptationRate = isResting ? 0.045 : 0.09;
                targetSpo2 = baselineSpo2 - (effectiveIntensity * 0.75);
                spo2AdaptationRate = isResting ? 0.04 : 0.07;
                fatigueRatePerSec = isResting ? -0.03 : (0.020 * effectiveIntensity);
                noiseAmplitude = 0.8;
                break;

            default:
                throw new IllegalStateException("Unsupported clinical profile: " + profileType);
        }

        // If the session random draw exceeded 0.85, perturb expected targets and Gaussian dispersion
        if (perturbedSession) {
            
            targetHr += random.nextDouble(10, 21);
            targetSpo2 -= random.nextDouble(2, 8);
            fatigueRatePerSec += random.nextDouble(0.1, 0.2);
            noiseAmplitude *= random.nextDouble(2.0, 3.5);
        }

        // Exponential approach toward steady-state targets + Gaussian variability
        double hrFactor = 1.0 - Math.exp(-hrAdaptationRate * deltaSeconds);
        double hrNoise = random.nextGaussian() * noiseAmplitude;
        currentHr = clamp(currentHr + (targetHr - currentHr) * hrFactor + hrNoise, MIN_HR_BPM, MAX_HR_BPM);

        double spo2Factor = 1.0 - Math.exp(-spo2AdaptationRate * deltaSeconds);
        double spo2Noise = random.nextGaussian() * (noiseAmplitude * 0.15);
        currentSpo2 = clamp(currentSpo2 + (targetSpo2 - currentSpo2) * spo2Factor + spo2Noise, MIN_SPO2, MAX_SPO2);

        // Hypoxia penalty: low SpO2 accelerates perceived fatigue
        double hypoxiaPenalty = (currentSpo2 < HYPOXIA_THRESHOLD_SPO2)
                ? (HYPOXIA_THRESHOLD_SPO2 - currentSpo2) * HYPOXIA_FATIGUE_FACTOR
                : 0.0;
        currentFatigue = clamp(
                currentFatigue + (fatigueRatePerSec + hypoxiaPenalty) * deltaSeconds,
                MIN_FATIGUE,
                MAX_FATIGUE
        );

        // Pain score escalates when fatigue exceeds critical clinical tolerance
        if (currentFatigue > PAIN_FATIGUE_THRESHOLD && !isResting) {
            currentPain = (int) Math.min(10, Math.round((currentFatigue - 6.0) * 2.0));
        } else if (isResting && currentPain > 0 && currentFatigue < PAIN_RECOVERY_FATIGUE_THRESHOLD) {
            currentPain = Math.max(0, currentPain - 1);
        }

        elapsedSeconds += deltaSeconds;
        return getCurrentSample();
    }

    public TelemetrySample getCurrentSample() {
        return new TelemetrySample(elapsedSeconds, currentHr, currentSpo2, currentFatigue, currentPain);
    }

    public ClinicalProfileType getProfileType() {
        return profileType;
    }

    private static double defaultBaselineHr(ClinicalProfileType type) {
        Objects.requireNonNull(type, "profileType must not be null");
        switch (type) {
            case ATHLETIC:
                return 58.0;
            case CARDIAC_REHAB:
                return 72.0;
            case FRAGILE_ELDERLY:
                return 68.0;
            case PULMONARY_COPD:
                return 78.0;
            default:
                return 70.0;
        }
    }

    private static double defaultBaselineSpo2(ClinicalProfileType type) {
        Objects.requireNonNull(type, "profileType must not be null");
        return (type == ClinicalProfileType.PULMONARY_COPD) ? 95.0 : 98.5;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}