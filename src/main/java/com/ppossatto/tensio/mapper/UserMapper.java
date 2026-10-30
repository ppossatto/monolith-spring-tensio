package com.ppossatto.tensio.mapper;

import com.ppossatto.tensio.domain.user.Credentials;
import com.ppossatto.tensio.domain.user.UserAccount;
import com.ppossatto.tensio.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/// MapStruct mapper from `UserEntity` to the user domain types.
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
}
