package com.ppossatto.tensio.domain.diagnosis;

import java.time.LocalDate;

/// An analysis just stored with its computed diagnosis.
///
/// @param analysisId     id of the stored analysis
/// @param transformerId  analysed transformer id
/// @param transformerTag analysed transformer tag
/// @param sampleDate     sample date
/// @param diagnosis      the computed diagnosis
public record DiagnosedAnalysis(Long analysisId, Long transformerId, String transformerTag, LocalDate sampleDate,
                                Diagnosis diagnosis) {
}
