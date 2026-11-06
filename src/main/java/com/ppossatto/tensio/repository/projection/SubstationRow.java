package com.ppossatto.tensio.repository.projection;

import java.math.BigDecimal;

/// Substation summary for lists.
///
/// @param id               substation id
/// @param acronym          acronym
/// @param name             name, may be `null`
/// @param city             city, may be `null`
/// @param state            state code, may be `null`
/// @param active           whether the substation is in use
/// @param latitude         decimal degrees (WGS 84), `null` when not registered
/// @param longitude        decimal degrees (WGS 84), `null` when not registered
/// @param transformerCount non-decommissioned transformers
/// @param dangerCount      of those, how many are in DANGER
public record SubstationRow(Long id, String acronym, String name, String city, String state, Boolean active,
                            BigDecimal latitude, BigDecimal longitude, Long transformerCount, Long dangerCount) {}
