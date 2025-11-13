package com.forristall.general.idp.util;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.web.bind.annotation.RequestMethod;

import com.forristall.general.idp.util.RestError.RestErrorBuilder;
import com.google.gson.Gson;

public class LoginData {

  private String email;
  
  private String password;

  public LoginData() {
    super();
  }

  public LoginData(String email, String password) {
    super();
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
  
  private boolean isEmailValid() {
    return EmailValidator.getInstance().isValid(email);
  }
  
  private boolean isPasswordValid() {
    return password != null && !password.isEmpty() && !password.isBlank();
  }
  
  public Optional<RestError> isDataValid(String route, RequestMethod method) {
    RestErrorBuilder errorBuilder = new RestErrorBuilder().setRoute(route).setMethod(method);
    if (!isEmailValid()) errorBuilder.setErrorCode(1).setMsg("Error: User's email is not valid");
    if (!isPasswordValid()) errorBuilder.setErrorCode(2).setMsg("Error: Password must have a non-null and non-blank value");
    return errorBuilder.isErrorCodeSet() ? Optional.of(errorBuilder.build()) : Optional.empty();
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, password);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LoginData other = (LoginData) obj;
    return Objects.equals(email, other.email) && Objects.equals(password, other.password);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
  
  
  
}
