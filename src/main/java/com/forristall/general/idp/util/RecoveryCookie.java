package com.forristall.general.idp.util;

import java.util.Objects;

import com.google.gson.Gson;

public class RecoveryCookie {
  
  private Long userId;
  private String accessToken;
  public RecoveryCookie(Long userId, String accessToken) {
    super();
    this.userId = userId;
    this.accessToken = accessToken;
  }
  public Long getUserId() {
    return userId;
  }
  public String getAccessToken() {
    return accessToken;
  }
  @Override
  public int hashCode() {
    return Objects.hash(accessToken, userId);
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecoveryCookie other = (RecoveryCookie) obj;
    return Objects.equals(accessToken, other.accessToken) && Objects.equals(userId, other.userId);
  }
  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
  
  
  
}
