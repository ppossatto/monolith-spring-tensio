package com.ppossatto.tensio.form;

import com.ppossatto.tensio.domain.user.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/// Input of the user edit form (`POST /users/{id}`): the access an administrator can change. The e-mail and
/// the password are not edited here.
@Getter
@Setter
@NoArgsConstructor
public class UserForm {

  /// Role granted to the user.
  @NotNull
  private UserRole role;

  /// Whether the user can sign in.
  private boolean active;
}
