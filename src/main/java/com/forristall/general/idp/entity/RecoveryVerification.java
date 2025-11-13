package com.forristall.general.idp.entity;

import java.util.Objects;

import com.forristall.general.idp.entity.RecoveryCode.RecoveryType;
import com.google.gson.Gson;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "RecoveryVerification")
public class RecoveryVerification {

  // Internal database ID for the user
  private @Id @GeneratedValue @NotNull Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "recoveryType")
  private RecoveryType recoveryType;

  private String verificationToken;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public RecoveryVerification() {
    super();
  }

  public RecoveryVerification(
          @NotNull Long id,
          RecoveryType recoveryType,
          String verificationToken,
          User user) {
    super();
    this.id = id;
    this.recoveryType = recoveryType;
    this.verificationToken = verificationToken;
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

  public String getVerificationToken() {
    return verificationToken;
  }

  public void setVerificationToken(String verificationToken) {
    this.verificationToken = verificationToken;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, recoveryType, user, verificationToken);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecoveryVerification other = (RecoveryVerification) obj;
    return Objects.equals(id, other.id) && recoveryType == other.recoveryType
            && Objects.equals(user, other.user)
            && Objects.equals(verificationToken, other.verificationToken);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
