package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.user.Credentials;
import com.ppossatto.tensio.domain.user.UserAccount;
import com.ppossatto.tensio.entity.UserEntity;
import com.ppossatto.tensio.form.UserForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/// MapStruct mapper between `UserEntity`, the user domain types and the user edit form.
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  /// Converts an entity to the account shown in the user pages (without the password hash).
  ///
  /// @param entity the entity
  /// @return the account
  UserAccount toAccount(UserEntity entity);

  /// Converts an entity to the data needed to authenticate it.
  ///
  /// @param entity the entity
  /// @return the credentials
  Credentials toCredentials(UserEntity entity);

  /// Converts the audit timestamps to a date in the server time zone.
  ///
  /// @param instant the timestamp, may be `null`
  /// @return the date, `null` when `instant` is `null`
  default LocalDate toLocalDate(Instant instant) {
    return instant == null ? null : LocalDate.ofInstant(instant, ZoneId.systemDefault());
  }

  /// Fills the edit form with the current role and status of a user.
  ///
  /// @param account the user
  /// @return a new form
  UserForm toForm(UserAccount account);

  /// Copies the role and status of the form to the entity. The e-mail and the password hash are never
  /// changed here.
  ///
  /// @param form   the validated form
  /// @param entity the user to update
  @Mapping(target = "username", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  void update(UserForm form, @MappingTarget UserEntity entity);
}
