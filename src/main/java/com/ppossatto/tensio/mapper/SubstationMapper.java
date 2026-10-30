package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.entity.SubstationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubstationMapper {

  Substation toDomain(SubstationEntity entity);
}
