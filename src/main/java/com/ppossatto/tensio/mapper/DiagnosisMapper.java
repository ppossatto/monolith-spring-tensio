package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.diagnosis.LatestDiagnosis;
import com.ppossatto.tensio.entity.TransformerLatestDiagnosis;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DiagnosisMapper {

  LatestDiagnosis toDomain(TransformerLatestDiagnosis entity);
}
