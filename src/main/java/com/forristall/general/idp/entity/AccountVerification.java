package com.forristall.general.idp.entity;

import java.util.Objects;

import com.google.gson.Gson;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "AccountVerification")
public class AccountVerification {

  // Internal database ID for the user
  private @Id @GeneratedValue @NotNull Long id;

  private String verificationToken;

  @OneToOne(mappedBy = "accountVerification")
  private User user;

  public AccountVerification() {
    super();
  }

  public AccountVerification(@NotNull Long id, String verificationToken, User user) {
    super();
    this.id = id;
    this.verificationToken = verificationToken;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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
    return Objects.hash(id, user, verificationToken);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AccountVerification other = (AccountVerification) obj;
    return Objects.equals(id, other.id) && Objects.equals(user, other.user)
            && Objects.equals(verificationToken, other.verificationToken);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
