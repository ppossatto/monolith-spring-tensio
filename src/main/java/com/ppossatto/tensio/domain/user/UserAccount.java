package com.ppossatto.tensio.domain.user;

import java.time.LocalDate;

/// Application user as shown in the user pages (never carries the password hash).
///
/// @param id        identifier
/// @param username  e-mail used to sign in
/// @param role      role
/// @param active    whether the user can sign in
/// @param createdAt date the user was created
/// @param updatedAt date of the last change
public record UserAccount(Long id, String username, UserRole role, boolean active, LocalDate createdAt,
                          LocalDate updatedAt) {
}
