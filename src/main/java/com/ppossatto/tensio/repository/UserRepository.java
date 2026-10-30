package com.ppossatto.tensio.repository;

import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/// Application user queries. `Containing` searches escape LIKE wildcards typed by the user.
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  /// Returns the user with the given username.
  ///
  /// @param username the username, already in lower case
  /// @return the user, empty when it does not exist
  Optional<UserEntity> findByUsername(String username);

  /// Users whose username contains `username`, ignoring case.
  ///
  /// @param username part of the username; empty matches every user
  /// @param pageable requested page and sort
  /// @return the page of users
  Page<UserEntity> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

  /// Users with the given role whose username contains `username`, ignoring case.
  ///
  /// @param username part of the username; empty matches every user
  /// @param role     the role
  /// @param pageable requested page and sort
  /// @return the page of users
  Page<UserEntity> findByUsernameContainingIgnoreCaseAndRole(String username, UserRole role, Pageable pageable);
}
