package com.ppossatto.tensio.entity;

import com.ppossatto.tensio.domain.user.UserRole;
import com.ppossatto.tensio.entity.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/// JPA mapping of the `app_user` table. See {@link com.ppossatto.tensio.domain.user.UserAccount}.
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private UserRole role;

  @Column(nullable = false)
  private boolean active = true;
}
