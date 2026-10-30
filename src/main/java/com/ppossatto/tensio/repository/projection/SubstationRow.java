package com.ppossatto.tensio.repository.projection;

/// Substation summary for lists.
///
/// @param id               substation id
/// @param acronym          acronym
/// @param name             name, may be `null`
/// @param city             city, may be `null`
/// @param state            state code, may be `null`
/// @param active           whether the substation is in use
/// @param transformerCount non-decommissioned transformers
/// @param dangerCount      of those, how many are in DANGER
public record SubstationRow(Long id, String acronym, String name, String city, String state, Boolean active,
                            Long transformerCount, Long dangerCount) {}
