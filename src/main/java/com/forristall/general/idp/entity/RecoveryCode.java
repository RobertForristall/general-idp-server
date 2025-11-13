package com.forristall.general.idp.entity;

import java.util.Objects;

import com.google.gson.Gson;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "RecoveryCodes")
public class RecoveryCode {

  public enum RecoveryType {
    EMAIL("email"), PHONE("phone");
    
    private final String name;

    private RecoveryType(String s) {
      name = s;
    }

    public boolean equalsName(String otherName) {
      return name.equals(otherName);
    }

    public String toString() {
      return name;
    }
  }

  // Internal database ID for the user
  private @Id @GeneratedValue @NotNull Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "recoveryType")
  private RecoveryType recoveryType;

  private String code;

  @OneToOne(mappedBy = "recoveryCode")
  private User user;

  public RecoveryCode() {
    super();
  }

  public RecoveryCode(@NotNull Long id, RecoveryType recoveryType, String code, User user) {
    super();
    this.id = id;
    this.recoveryType = recoveryType;
    this.code = code;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public RecoveryType getRecoveryType() {
    return recoveryType;
  }

  public void setRecoveryType(RecoveryType recoveryType) {
    this.recoveryType = recoveryType;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, id, recoveryType, user);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecoveryCode other = (RecoveryCode) obj;
    return Objects.equals(code, other.code) && Objects.equals(id, other.id)
            && recoveryType == other.recoveryType && Objects.equals(user, other.user);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
