package edu.unisa.medsim;

/**
 * Defines the clinical behavioral classes supported by the telemetry simulator.
 * Each profile models distinct cardiovascular, respiratory, and fatigue dynamics
 * during physical exertion and recovery.
 */
public enum ClinicalProfileType {
    /** Well-conditioned subject: moderate HR drift, stable SpO2, rapid recovery. */
    ATHLETIC,

    /** Cardiovascular patient: steep HR rise under load, delayed vagal recovery. */
    CARDIAC_REHAB,

    /** Elderly subject with reduced physiological reserve and faster fatigue onset. */
    FRAGILE_ELDERLY,

    /** Pulmonary patient prone to exertional desaturation (SpO2 drop) and dyspnea. */
    PULMONARY_COPD
}
