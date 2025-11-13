package com.forristall.general.idp.util;

import java.util.Objects;

import com.google.gson.Gson;

public class GetSecurityQuestions {

  private String question1;
  private String question2;
  public GetSecurityQuestions(String question1, String question2) {
    super();
    this.question1 = question1;
    this.question2 = question2;
  }
  public String getQuestion1() {
    return question1;
  }
  public String getQuestion2() {
    return question2;
  }
  @Override
  public int hashCode() {
    return Objects.hash(question1, question2);
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GetSecurityQuestions other = (GetSecurityQuestions) obj;
    return Objects.equals(question1, other.question1) && Objects.equals(question2, other.question2);
  }
  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
  
  
  
}
