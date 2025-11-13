package com.forristall.general.idp.entity;

import java.sql.Timestamp;
import java.util.Objects;

import com.google.gson.Gson;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "RecoveryPhones")
public class RecoveryPhone {

  // Internal database ID for the user
  private @Id @GeneratedValue @NotNull Long id;

  private String phoneNumber;

  private boolean verified;

  private Timestamp created;

  private Timestamp modified;

  @OneToOne(mappedBy = "recoveryPhone")
  private User user;

  public RecoveryPhone() {
    super();
  }

  public RecoveryPhone(
          @NotNull Long id,
          String phoneNumber,
          boolean verified,
          Timestamp created,
          Timestamp modified) {
    super();
    this.id = id;
    this.phoneNumber = phoneNumber;
    this.verified = verified;
    this.created = created;
    this.modified = modified;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public Timestamp getCreated() {
    return created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  public Timestamp getModified() {
    return modified;
  }

  public void setModified(Timestamp modified) {
    this.modified = modified;
  }

  @Override
  public int hashCode() {
    return Objects.hash(created, id, modified, phoneNumber, verified);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecoveryPhone other = (RecoveryPhone) obj;
    return Objects.equals(created, other.created) && Objects.equals(id, other.id)
            && Objects.equals(modified, other.modified)
            && Objects.equals(phoneNumber, other.phoneNumber) && verified == other.verified;
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
