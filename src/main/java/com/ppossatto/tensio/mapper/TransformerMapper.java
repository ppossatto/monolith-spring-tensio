package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.entity.TransformerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = SubstationMapper.class)
public interface TransformerMapper {

  Transformer toDomain(TransformerEntity entity);
}
