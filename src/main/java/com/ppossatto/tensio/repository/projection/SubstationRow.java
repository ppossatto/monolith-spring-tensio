package com.ppossatto.tensio.repository.projection;

public record SubstationRow(Long id, String acronym, String name, String city, String state,
                            Long transformerCount, Long dangerCount) {}
