package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.form.SubstationForm;
import com.ppossatto.tensio.form.TransformerForm;
import com.ppossatto.tensio.domain.substation.Substation;
import com.ppossatto.tensio.domain.transformer.Transformer;
import com.ppossatto.tensio.entity.SubstationEntity;
import com.ppossatto.tensio.entity.TransformerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/// MapStruct mapper between the asset forms and the entities / domain types.
/// Ids and audit fields have no setters in the entities, so a form can never change them;
/// the transformer substation is set by the service.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssetFormMapper {

  /// Copies the form values into a substation entity.
  ///
  /// @param form   the submitted form
  /// @param entity the entity to update
  void update(SubstationForm form, @MappingTarget SubstationEntity entity);

  /// Copies the form values into a transformer entity; the substation is set by the service.
  ///
  /// @param form   the submitted form
  /// @param entity the entity to update
  @Mapping(target = "substation", ignore = true)
  void update(TransformerForm form, @MappingTarget TransformerEntity entity);

  /// Fills the edit form of an existing substation.
  ///
  /// @param substation the substation
  /// @return the form
  SubstationForm toForm(Substation substation);

  /// Fills the edit form of an existing transformer.
  ///
  /// @param transformer the transformer
  /// @return the form
  @Mapping(target = "substationId", source = "substation.id")
  TransformerForm toForm(Transformer transformer);
}
