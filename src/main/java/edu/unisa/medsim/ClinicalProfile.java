package edu.unisa.medsim;

/**
 * Profile-specific parameters of a simulated patient (Strategy pattern).
 *
 * <p>{@link PatientTelemetrySimulator} contains the physiological model shared by all
 * patients (exponential adaptation, noise, hypoxia penalty, pain score, perturbed sessions).
 * Each clinical profile only provides the numbers that make it different. A new profile is
 * added by writing a new implementation of this interface, without modifying the simulator
 * (Open/Closed Principle).</p>
 *
 * <p>All rates are per second; "per intensity" values are multiplied by the effective
 * exercise intensity (0..10, forced to 0 while resting).</p>
 */
public interface ClinicalProfile {

    /**
     * Returns the unique identifier of the profile, used by the registry and shown in the UI
     * (for example {@code "CARDIAC_REHAB"}). Enum constants implement it automatically.
     *
     * @return the profile identifier
     */
    String name();

    /** @return resting heart rate used when no custom baseline is given, in BPM */
    double getDefaultBaselineHr();

    /** @return resting SpO2 used when no custom baseline is given, in percent */
    double getDefaultBaselineSpo2();

    /** @return heart-rate increase of the target per intensity unit, in BPM */
    double getHrGainPerIntensity();

    /**
     * @param resting {@code true} during a pause or recovery phase
     * @return speed at which the heart rate approaches its target, per second
     */
    double getHrAdaptationRate(boolean resting);

    /** @return SpO2 decrease of the target per intensity unit, in percentage points */
    double getSpo2DropPerIntensity();

    /**
     * @param resting {@code true} during a pause or recovery phase
     * @return speed at which SpO2 approaches its target, per second
     */
    double getSpo2AdaptationRate(boolean resting);

    /** @return fatigue increase per second and per intensity unit during exercise */
    double getFatigueRatePerIntensity();

    /** @return fatigue change per second while resting (negative: the patient recovers) */
    double getRestingFatigueRate();

    /** @return amplitude of the Gaussian noise added to the heart rate, in BPM */
    double getNoiseAmplitude();
}