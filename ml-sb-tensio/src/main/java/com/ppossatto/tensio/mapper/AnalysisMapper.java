package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.Analysis;
import com.ppossatto.tensio.entity.AnalysisEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AnalysisMapper {

  @Mapping(target = "transformerId", source = "transformer.id")
  Analysis toDomain(AnalysisEntity entity);
}
