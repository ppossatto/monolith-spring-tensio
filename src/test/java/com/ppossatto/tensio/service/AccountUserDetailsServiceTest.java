package com.ppossatto.tensio.service;

import com.ppossatto.tensio.domain.user.Credentials;
import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.entity.UserEntity;
import com.ppossatto.tensio.mapper.UserMapper;
import com.ppossatto.tensio.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUserDetailsServiceTest {

  private static final String HASH = "{bcrypt}$2a$10$hash";

  @Mock
  private UserRepository users;
  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private AccountUserDetailsService service;

  @Test
  void loadsUserIgnoringCaseAndSpaces() {
    stubUser("ana@tensio.local", UserRole.ADMIN, true);

    var user = service.loadUserByUsername("  Ana@Tensio.Local ");

    assertThat(user.getUsername()).isEqualTo("ana@tensio.local");
    assertThat(user.getPassword()).isEqualTo(HASH);
    assertThat(user.isEnabled()).isTrue();
    assertThat(user.getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
  }

  @Test
  void inactiveUserIsDisabled() {
    stubUser("old@tensio.local", UserRole.USER, false);

    assertThat(service.loadUserByUsername("old@tensio.local").isEnabled()).isFalse();
  }

  @Test
  void unknownUserIsNotFound() {
    when(users.findByUsername("nobody@tensio.local")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.loadUserByUsername("nobody@tensio.local"))
       .isInstanceOf(UsernameNotFoundException.class);
  }

  private void stubUser(String username, UserRole role, boolean active) {
    var entity = new UserEntity();
    when(users.findByUsername(username)).thenReturn(Optional.of(entity));
    when(userMapper.toCredentials(entity)).thenReturn(new Credentials(username, HASH, role, active));
  }
}
