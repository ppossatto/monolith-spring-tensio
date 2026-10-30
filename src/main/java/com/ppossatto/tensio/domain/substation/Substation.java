package com.ppossatto.tensio.domain.substation;

import java.math.BigDecimal;

/// Electrical substation.
///
/// @param id        identifier
/// @param name      full name, may be `null`
/// @param acronym   unique short name, e.g. `SNO`
/// @param city      city, may be `null`
/// @param state     two-letter state code, may be `null`
/// @param latitude  latitude in decimal degrees, may be `null`
/// @param longitude longitude in decimal degrees, may be `null`
/// @param active    whether the substation is in use
public record Substation(
   Long id,
   String name,
   String acronym,
   String city,
   String state,
   BigDecimal latitude,
   BigDecimal longitude,
   boolean active
) {
}
