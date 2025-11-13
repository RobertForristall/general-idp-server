package com.forristall.general.idp.util;

import java.util.List;
import java.util.Objects;

import com.google.gson.Gson;

public class SessionCookie {

  private Long userId;
  private String application;
  private String role;
  private List<String> additionalPermissions;
  private String accessToken;
  private String refreshToken;

  public SessionCookie() {
    super();
  }

  public SessionCookie(
          Long userId,
          String application,
          String role,
          List<String> additionalPermissions,
          String accessToken,
          String refreshToken) {
    super();
    this.userId = userId;
    this.application = application;
    this.role = role;
    this.additionalPermissions = additionalPermissions;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public List<String> getAdditionalPermissions() {
    return additionalPermissions;
  }

  public void setAdditionalPermissions(List<String> additionalPermissions) {
    this.additionalPermissions = additionalPermissions;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @Override
  public int hashCode() {
    return Objects
            .hash(accessToken, additionalPermissions, application, refreshToken, role, userId);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SessionCookie other = (SessionCookie) obj;
    return Objects.equals(accessToken, other.accessToken)
            && Objects.equals(additionalPermissions, other.additionalPermissions)
            && Objects.equals(application, other.application)
            && Objects.equals(refreshToken, other.refreshToken) && Objects.equals(role, other.role)
            && Objects.equals(userId, other.userId);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
