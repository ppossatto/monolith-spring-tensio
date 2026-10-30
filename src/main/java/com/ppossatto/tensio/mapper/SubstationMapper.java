package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.entity.SubstationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/// MapStruct mapper from `SubstationEntity` to the `Substation` domain type.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubstationMapper {

  /// Converts an entity to its domain type.
  ///
  /// @param entity the entity
  /// @return the domain object
  Substation toDomain(SubstationEntity entity);
}
