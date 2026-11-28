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

    private final ClinicalProfile profile;
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
     * @param profile     the clinical profile of the simulated patient
     * @param seed        random seed for simulation reproducibility
     */
    public PatientTelemetrySimulator(ClinicalProfile profile, long seed) {
        this(profile, requireProfile(profile).getDefaultBaselineHr(), profile.getDefaultBaselineSpo2(), seed);
    }

    /**
     * Creates a simulator with custom resting baseline parameters.
     *
     * @param profile      the clinical profile of the simulated patient
     * @param baselineHr   resting heart rate in BPM (must be within [40, 220])
     * @param baselineSpo2 resting oxygen saturation percentage (must be within [70, 100])
     * @param seed         random seed for simulation reproducibility
     */
    public PatientTelemetrySimulator(ClinicalProfile profile, double baselineHr,
                                     double baselineSpo2, long seed) {
        this.profile = Objects.requireNonNull(profile, "profile must not be null");
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

        // Profile-specific parameters come from the strategy: no switch on the profile type
        double targetHr = baselineHr + (effectiveIntensity * profile.getHrGainPerIntensity());
        double hrAdaptationRate = profile.getHrAdaptationRate(isResting);
        double targetSpo2 = baselineSpo2 - (effectiveIntensity * profile.getSpo2DropPerIntensity());
        double spo2AdaptationRate = profile.getSpo2AdaptationRate(isResting);
        double fatigueRatePerSec = isResting
                ? profile.getRestingFatigueRate()
                : (profile.getFatigueRatePerIntensity() * effectiveIntensity);
        double noiseAmplitude = profile.getNoiseAmplitude();

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

    /**
     * @return the clinical profile of the simulated patient
     */
    public ClinicalProfile getProfile() {
        return profile;
    }

    /** Null check usable inside {@code this(...)}, before the profile is dereferenced. */
    private static ClinicalProfile requireProfile(ClinicalProfile profile) {
        return Objects.requireNonNull(profile, "profile must not be null");
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}