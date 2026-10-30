package com.ppossatto.tensio.domain.diagnosis;

/// Diagnosis of one analysis: fault found by each DGA method, final fault and health status.
/// A method fault is `null` when the method is not applicable to the analysis.
///
/// @param rogersFault      Rogers ratios method
/// @param duvalFault       Duval triangle method
/// @param iecFault         IEC 60599 ratios method
/// @param doernenburgFault Doernenburg ratios method
/// @param finalFault       weighted vote of the methods (see {@link FinalFaultResolver})
/// @param healthStatus     resulting health status
public record Diagnosis(Fault rogersFault, Fault duvalFault, Fault iecFault, Fault doernenburgFault,
                        Fault finalFault, HealthStatus healthStatus) {
}
