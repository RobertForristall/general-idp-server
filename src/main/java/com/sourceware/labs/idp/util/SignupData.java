package com.sourceware.labs.idp.util;

import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.GsonBuilder;
import com.sourceware.labs.idp.util.RestError.RestErrorBuilder;

public class SignupData {

  private String email;

  private String password;

  private String firstName;

  private String lastName;

  private Date dob;

  private String sq1;

  private String sq2;

  private String sa1;

  private String sa2;

  private String recoveryEmail;

  private String recoveryPhone;

  private String verificationToken;

  private boolean isEmailValid() {
    return EmailValidator.getInstance().isValid(email);
  }

  private boolean isUserOldEnough() {
    return this.dob != null
            && Period
                    .between(
                            dob.toInstant().atZone(ZoneId.of("Z")).toLocalDate(),
                            new Date().toInstant().atZone(ZoneId.of("Z")).toLocalDate())
                    .getYears() >= 18;
  }

  private boolean isRecoveryEmailValid() {
    if (recoveryEmail != null) {
      return EmailValidator.getInstance().isValid(recoveryEmail);
    }
    return true;
  }

  private boolean isRecoveryPhoneValid() {
    if (recoveryPhone != null) {
      String patterns = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
              + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
              + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$";
      Pattern pattern = Pattern.compile(patterns);
      Matcher matcher = pattern.matcher(recoveryPhone);
      return matcher.matches();
    }
    return true;
  }
  
  public boolean areSecurityQuestionsPresent() {
    return (sq1 != null && !sq1.isBlank())
            || (sq2 != null && !sq2.isBlank())
            || (sa1 != null && !sa1.isBlank())
            || (sa2 != null && !sa2.isBlank());
  }

  public Optional<RestError> isDataValid(String route, RequestMethod method) {
    RestErrorBuilder errorBuilder = new RestErrorBuilder().setRoute(route).setMethod(method);
    if (!isEmailValid())
      errorBuilder.setErrorCode(1).setMsg("Error: User's email is not valid");
    if (!isUserOldEnough())
      errorBuilder.setErrorCode(2).setMsg("Error: User is not old enough");
    if (!isRecoveryEmailValid())
      errorBuilder.setErrorCode(3).setMsg("Error: User's recovery email is not valid");
    if (!isRecoveryPhoneValid())
      errorBuilder.setErrorCode(4).setMsg("Error: User's recovery phone number is not valid");
    return errorBuilder.isErrorCodeSet() ? Optional.of(errorBuilder.build()) : Optional.empty();
  }

  public SignupData() {
    super();
    this.verificationToken = RandomStringUtils.secureStrong().nextAlphanumeric(50);
  }

  public SignupData(
          String email,
          String password,
          String firstName,
          String lastName,
          Date dob,
          String sq1,
          String sq2,
          String sa1,
          String sa2,
          String recoveryEmail,
          String recoveryPhone) {
    super();
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.dob = dob;
    this.sq1 = sq1;
    this.sq2 = sq2;
    this.sa1 = sa1;
    this.sa2 = sa2;
    this.recoveryEmail = recoveryEmail;
    this.recoveryPhone = recoveryPhone;
    this.verificationToken = RandomStringUtils.secureStrong().nextAlphanumeric(50);
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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Date getDob() {
    return dob;
  }

  public void setDob(Date dob) {
    this.dob = dob;
  }

  public String getSq1() {
    return sq1;
  }

  public void setSq1(String sq1) {
    this.sq1 = sq1;
  }

  public String getSq2() {
    return sq2;
  }

  public void setSq2(String sq2) {
    this.sq2 = sq2;
  }

  public String getSa1() {
    return sa1;
  }

  public void setSa1(String sa1) {
    this.sa1 = sa1;
  }

  public String getSa2() {
    return sa2;
  }

  public void setSa2(String sa2) {
    this.sa2 = sa2;
  }

  public String getRecoveryEmail() {
    return recoveryEmail;
  }

  public void setRecoveryEmail(String recoveryEmail) {
    this.recoveryEmail = recoveryEmail;
  }

  public String getRecoveryPhone() {
    return recoveryPhone;
  }

  public void setRecoveryPhone(String recoveryPhone) {
    this.recoveryPhone = recoveryPhone;
  }

  public String getVerificationToken() {
    return verificationToken;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
            dob,
            email,
            firstName,
            lastName,
            password,
            recoveryEmail,
            recoveryPhone,
            sa1,
            sa2,
            sq1,
            sq2);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SignupData other = (SignupData) obj;
    return Objects.equals(dob, other.dob) && Objects.equals(email, other.email)
            && Objects.equals(firstName, other.firstName)
            && Objects.equals(lastName, other.lastName) && Objects.equals(password, other.password)
            && Objects.equals(recoveryEmail, other.recoveryEmail)
            && Objects.equals(recoveryPhone, other.recoveryPhone) && Objects.equals(sa1, other.sa1)
            && Objects.equals(sa2, other.sa2) && Objects.equals(sq1, other.sq1)
            && Objects.equals(sq2, other.sq2);
  }

  @Override
  public String toString() {
    return new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create().toJson(this);
  }

}
