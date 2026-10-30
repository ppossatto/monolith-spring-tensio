package com.ppossatto.tensio.repository.projection;

public record SubstationRow(Long id, String acronym, String name, String city, String state, Boolean active,
                            Long transformerCount, Long dangerCount) {}
