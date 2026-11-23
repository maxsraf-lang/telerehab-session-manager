package edu.unisa.medsim;

/**
 * Immutable snapshot of simulated patient physiological telemetry at a specific timestamp.
 */
public final class TelemetrySample {

    private final int elapsedSeconds;
    private final double heartRateBpm;
    private final double spo2Percentage;
    private final double fatigueIndex;
    private final int painScore;

    /**
     * Constructs a new immutable telemetry sample.
     *
     * @param elapsedSeconds total simulation time elapsed in seconds
     * @param heartRateBpm   instantaneous heart rate in beats per minute (BPM)
     * @param spo2Percentage peripheral oxygen saturation (0.0 - 100.0%)
     * @param fatigueIndex   perceived exertion / muscular fatigue index (0.0 - 10.0)
     * @param painScore      reported discomfort or pain scale (0 - 10)
     */
    public TelemetrySample(int elapsedSeconds, double heartRateBpm, double spo2Percentage,
                           double fatigueIndex, int painScore) {
        this.elapsedSeconds = elapsedSeconds;
        this.heartRateBpm = heartRateBpm;
        this.spo2Percentage = spo2Percentage;
        this.fatigueIndex = fatigueIndex;
        this.painScore = painScore;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public double getHeartRateBpm() {
        return heartRateBpm;
    }

    public double getSpo2Percentage() {
        return spo2Percentage;
    }

    public double getFatigueIndex() {
        return fatigueIndex;
    }

    public int getPainScore() {
        return painScore;
    }

    @Override
    public String toString() {
        return String.format(
                "TelemetrySample[t=%ds, HR=%.1f bpm, SpO2=%.1f%%, Fatigue=%.2f/10, Pain=%d/10]",
                elapsedSeconds, heartRateBpm, spo2Percentage, fatigueIndex, painScore
        );
    }
}
