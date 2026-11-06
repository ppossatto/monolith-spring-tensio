package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.user.UserAccount;
import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.form.UserForm;
import com.ppossatto.tensio.mapper.UserMapper;
import com.ppossatto.tensio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/// Application user use cases: list and detail. Access is restricted to `ADMIN` in `SecurityConfig`.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private static final int PAGE_SIZE = 10;

  private final UserRepository users;
  private final UserMapper userMapper;

  /// Returns a page of users ordered by username.
  ///
  /// @param role   only users with this role, all when `null`
  /// @param search case-insensitive part of the username; blank matches every user
  /// @param page   zero-based page number (negative values are treated as 0)
  /// @return the requested page
  public Page<UserAccount> list(UserRole role, String search, int page) {
    var pageable = PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("username"));
    var text = search == null ? "" : search.strip();
    var result = role == null
       ? users.findByUsernameContainingIgnoreCase(text, pageable)
       : users.findByUsernameContainingIgnoreCaseAndRole(text, role, pageable);
    return result.map(userMapper::toAccount);
  }

  /// Returns a user.
  ///
  /// @param id the user id
  /// @return the user
  /// @throws ResponseStatusException with 404 when the user does not exist
  public UserAccount find(Long id) {
    return users.findById(id)
       .map(userMapper::toAccount)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  /// Returns the edit form of a user, filled with its current role and status.
  ///
  /// @param id the user id
  /// @return the form
  /// @throws ResponseStatusException with 404 when the user does not exist
  public UserForm form(Long id) {
    return userMapper.toForm(find(id));
  }

  /// Changes the role and status of a user. An administrator cannot remove their own access (role or
  /// status), so there is always an active administrator left. The change applies at the user's next
  /// sign-in; an open session keeps the previous access until it ends.
  ///
  /// @param id            the user to change
  /// @param form          the validated form
  /// @param administrator e-mail of the signed-in administrator making the change
  /// @throws ResponseStatusException with 404 when the user does not exist
  /// @throws FieldConflictException  when the administrator would demote or deactivate themselves
  @Transactional
  public void update(Long id, UserForm form, String administrator) {
    var entity = users.findById(id)
       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (entity.getUsername().equalsIgnoreCase(administrator)) {
      if (form.getRole() != UserRole.ADMIN) {
        throw new FieldConflictException("role", "you cannot remove your own administrator role");
      }
      if (!form.isActive()) {
        throw new FieldConflictException("active", "you cannot deactivate your own account");
      }
    }
    userMapper.update(form, entity);
  }
}
