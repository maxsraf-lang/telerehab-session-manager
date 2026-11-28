package edu.unisa.medsim;

/**
 * Defines the clinical behavioral classes supported by the telemetry simulator.
 * Each profile models distinct cardiovascular, respiratory, and fatigue dynamics
 * during physical exertion and recovery.
 *
 * <p>Each constant is a {@link ClinicalProfile} strategy: the numbers that used to be
 * hard-coded in the {@code switch} of {@link PatientTelemetrySimulator} are now stored
 * in the constant itself.</p>
 */
public enum ClinicalProfileType implements ClinicalProfile {

    //                 baseline      HR gain  HR rate         SpO2   SpO2 rate       fatigue           noise
    //                 HR    SpO2             exer.   rest    drop   exer.   rest    per int.  rest
    /** Well-conditioned subject: moderate HR drift, stable SpO2, rapid recovery. */
    ATHLETIC(          58.0, 98.5,   8.5,     0.08,   0.10,   0.05,  0.15,   0.15,   0.008,   -0.06,  0.4),

    /** Cardiovascular patient: steep HR rise under load, delayed vagal recovery. */
    CARDIAC_REHAB(     72.0, 98.5,  11.5,     0.12,   0.035,  0.18,  0.08,   0.08,   0.018,   -0.03,  0.7),

    /** Elderly subject with reduced physiological reserve and faster fatigue onset. */
    FRAGILE_ELDERLY(   68.0, 98.5,   6.5,     0.05,   0.03,   0.22,  0.06,   0.06,   0.024,   -0.025, 0.6),

    /** Pulmonary patient prone to exertional desaturation (SpO2 drop) and dyspnea. */
    PULMONARY_COPD(    78.0, 95.0,   9.5,     0.09,   0.045,  0.75,  0.07,   0.04,   0.020,   -0.03,  0.8);

    private final double defaultBaselineHr;
    private final double defaultBaselineSpo2;
    private final double hrGainPerIntensity;
    private final double hrAdaptationRateExercise;
    private final double hrAdaptationRateResting;
    private final double spo2DropPerIntensity;
    private final double spo2AdaptationRateExercise;
    private final double spo2AdaptationRateResting;
    private final double fatigueRatePerIntensity;
    private final double restingFatigueRate;
    private final double noiseAmplitude;

    ClinicalProfileType(double defaultBaselineHr, double defaultBaselineSpo2,
                        double hrGainPerIntensity,
                        double hrAdaptationRateExercise, double hrAdaptationRateResting,
                        double spo2DropPerIntensity,
                        double spo2AdaptationRateExercise, double spo2AdaptationRateResting,
                        double fatigueRatePerIntensity, double restingFatigueRate,
                        double noiseAmplitude) {
        this.defaultBaselineHr = defaultBaselineHr;
        this.defaultBaselineSpo2 = defaultBaselineSpo2;
        this.hrGainPerIntensity = hrGainPerIntensity;
        this.hrAdaptationRateExercise = hrAdaptationRateExercise;
        this.hrAdaptationRateResting = hrAdaptationRateResting;
        this.spo2DropPerIntensity = spo2DropPerIntensity;
        this.spo2AdaptationRateExercise = spo2AdaptationRateExercise;
        this.spo2AdaptationRateResting = spo2AdaptationRateResting;
        this.fatigueRatePerIntensity = fatigueRatePerIntensity;
        this.restingFatigueRate = restingFatigueRate;
        this.noiseAmplitude = noiseAmplitude;
    }

    @Override
    public double getDefaultBaselineHr() {
        return defaultBaselineHr;
    }

    @Override
    public double getDefaultBaselineSpo2() {
        return defaultBaselineSpo2;
    }

    @Override
    public double getHrGainPerIntensity() {
        return hrGainPerIntensity;
    }

    @Override
    public double getHrAdaptationRate(boolean resting) {
        return resting ? hrAdaptationRateResting : hrAdaptationRateExercise;
    }

    @Override
    public double getSpo2DropPerIntensity() {
        return spo2DropPerIntensity;
    }

    @Override
    public double getSpo2AdaptationRate(boolean resting) {
        return resting ? spo2AdaptationRateResting : spo2AdaptationRateExercise;
    }

    @Override
    public double getFatigueRatePerIntensity() {
        return fatigueRatePerIntensity;
    }

    @Override
    public double getRestingFatigueRate() {
        return restingFatigueRate;
    }

    @Override
    public double getNoiseAmplitude() {
        return noiseAmplitude;
    }
}
