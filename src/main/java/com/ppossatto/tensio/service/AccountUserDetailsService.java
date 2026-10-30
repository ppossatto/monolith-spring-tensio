package com.ppossatto.tensio.service;

import com.ppossatto.tensio.mapper.UserMapper;
import com.ppossatto.tensio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/// Loads the users of the `app_user` table for Spring Security form login.
/// Inactive users are returned as disabled, so their sign-in fails.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountUserDetailsService implements UserDetailsService {

  private final UserRepository users;
  private final UserMapper userMapper;

  /// Returns the user with the given e-mail, ignoring case and surrounding spaces.
  ///
  /// @param username the e-mail typed in the login form
  /// @return the user details with role `ROLE_<role>`
  /// @throws UsernameNotFoundException when no user has this e-mail
  @Override
  public UserDetails loadUserByUsername(String username) {
    var normalized = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
    var credentials = users.findByUsername(normalized)
       .map(userMapper::toCredentials)
       .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return User.withUsername(credentials.username())
       .password(credentials.passwordHash())
       .roles(credentials.role().name())
       .disabled(!credentials.active())
       .build();
  }
}
