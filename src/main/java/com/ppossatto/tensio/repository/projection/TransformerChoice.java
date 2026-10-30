package com.ppossatto.tensio.repository.projection;

/// Transformer shown as an option of a select.
///
/// @param id         transformer id
/// @param tag        tag
/// @param substation substation acronym
public record TransformerChoice(Long id, String tag, String substation) {}
