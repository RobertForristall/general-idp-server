package com.forristall.general.idp.entity;

import java.sql.Timestamp;
import java.util.Objects;

import com.google.gson.Gson;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "SecurityQuestions")
public class SecurityQuestion {

  // Internal database ID for the user
  private @Id @GeneratedValue @NotNull Long id;

  @NotBlank(message = "Security question 1 must be defined")
  private String question1;

  @NotBlank(message = "Security question 2 must be defined")
  private String question2;

  @NotBlank(message = "Security answer 1 must be defined")
  private String answer1;

  @NotBlank(message = "Security answer 2 must be defined")
  private String answer2;

  @NotNull(message = "All security questions should have an accurate created time")
  private Timestamp created;

  @NotNull(message = "All security questions should have an up-to-date modified time")
  private Timestamp modified;

  @OneToOne(mappedBy = "securityQuestion")
  private User user;

  public SecurityQuestion() {
    super();
  }

  public SecurityQuestion(
          @NotNull Long id,
          String question1,
          String question2,
          String answer1,
          String answer2,
          Timestamp created,
          Timestamp modified,
          User user) {
    super();
    this.id = id;
    this.question1 = question1;
    this.question2 = question2;
    this.answer1 = answer1;
    this.answer2 = answer2;
    this.created = created;
    this.modified = modified;
    this.user = user;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getQuestion1() {
    return question1;
  }

  public void setQuestion1(String question1) {
    this.question1 = question1;
  }

  public String getQuestion2() {
    return question2;
  }

  public void setQuestion2(String question2) {
    this.question2 = question2;
  }

  public String getAnswer1() {
    return answer1;
  }

  public void setAnswer1(String answer1) {
    this.answer1 = answer1;
  }

  public String getAnswer2() {
    return answer2;
  }

  public void setAnswer2(String answer2) {
    this.answer2 = answer2;
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  @Override
  public int hashCode() {
    return Objects.hash(answer1, answer2, created, id, modified, question1, question2, user);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SecurityQuestion other = (SecurityQuestion) obj;
    return Objects.equals(answer1, other.answer1) && Objects.equals(answer2, other.answer2)
            && Objects.equals(created, other.created) && Objects.equals(id, other.id)
            && Objects.equals(modified, other.modified)
            && Objects.equals(question1, other.question1)
            && Objects.equals(question2, other.question2) && Objects.equals(user, other.user);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }

}
