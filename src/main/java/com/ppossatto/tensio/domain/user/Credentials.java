package com.ppossatto.tensio.domain.user;

/// Data needed to authenticate a user.
///
/// @param username     e-mail used to sign in
/// @param passwordHash password hash with its encoder id prefix, e.g. `{bcrypt}...`
/// @param role         role
/// @param active       whether the user can sign in
public record Credentials(String username, String passwordHash, UserRole role, boolean active) {
}
