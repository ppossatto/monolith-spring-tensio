package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.entity.SubstationEntity;
import com.ppossatto.tensio.entity.TransformerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransformerMapper {

  Transformer toDomain(TransformerEntity entity);

  Substation toDomain(SubstationEntity entity);
}
