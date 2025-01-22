package com.sourceware.labs.idp.util;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.sourceware.labs.idp.util.RestError.RestErrorBuilder;

public class RecoveryDataAnswers {
  private String email;
  private String answer1;
  private String answer2;

  public RecoveryDataAnswers(String email, String answer1, String answer2) {
    super();
    this.email = email;
    this.answer1 = answer1;
    this.answer2 = answer2;
  }
  
  public String getEmail() {
    return email;
  }

  public String getAnswer1() {
    return answer1;
  }

  public String getAnswer2() {
    return answer2;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }

  public void setAnswer1(String answer1) {
    this.answer1 = answer1;
  }

  public void setAnswer2(String answer2) {
    this.answer2 = answer2;
  }

  private boolean isEmailValid() {
    return EmailValidator.getInstance().isValid(email);
  }
  
  private boolean isAnswerValid(String answer) {
    return answer != null && !answer.isEmpty() && !answer.isBlank();
  }
  
  public Optional<RestError> isDataValid(String path, RequestMethod method) {
    RestErrorBuilder errorBuilder = new RestErrorBuilder().setRoute(path).setMethod(method);
    if (!isEmailValid()) return Optional.of(errorBuilder.setErrorCode(1).setMsg("Error: Provided email is not valid").build());
    if (!isAnswerValid(answer1)) return Optional.of(errorBuilder.setErrorCode(2).setMsg("Error: An answer for the first security question must be supplied").build());
    if (!isAnswerValid(answer2)) return Optional.of(errorBuilder.setErrorCode(3).setMsg("Error: An answer for the second security question must be supplied").build());
    return Optional.empty();
  }

  @Override
  public int hashCode() {
    return Objects.hash(answer1, answer2, email);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecoveryDataAnswers other = (RecoveryDataAnswers) obj;
    return Objects.equals(answer1, other.answer1) && Objects.equals(answer2, other.answer2)
            && Objects.equals(email, other.email);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
