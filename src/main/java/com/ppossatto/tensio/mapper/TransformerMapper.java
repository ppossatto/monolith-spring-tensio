package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.entity.TransformerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/// MapStruct mapper from `TransformerEntity` to the `Transformer` domain type.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = SubstationMapper.class)
public interface TransformerMapper {

  /// Converts an entity to its domain type.
  ///
  /// @param entity the entity
  /// @return the domain object
  Transformer toDomain(TransformerEntity entity);
}
