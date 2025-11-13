package com.forristall.general.idp.route;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import com.forristall.general.idp.BaseIdpApplicationTests;
import com.forristall.general.idp.test.data.TestData;
import com.forristall.general.idp.util.SignupData;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class SignupTests extends BaseIdpApplicationTests {

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;

  @BeforeAll
  public void setup() throws URISyntaxException {
    testingPath = "/user/signup";
    testingMethod = RequestMethod.POST;
    fullTestingRoute = new URI(baseUrl + testingPath);
  }

  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }

  @Test
  @Order(1)
  public void createValidUser() throws Exception {
    SignupData signupData = TestData.getTestSignupData();
    HttpEntity<SignupData> request = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate
            .postForEntity(fullTestingRoute, request, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), result.getStatusCode());
    Assertions.assertEquals("User Created Successfully", result.getBody());
  }

  @Test
  public void catchEmailIsNull() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: User's email is not valid");
  }

  @Test
  public void catchEmailIsEmpty() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: User's email is not valid");
  }

  @Test
  public void catchEmailIsNotValid() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test@test");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: User's email is not valid");
  }

  @Test
  public void catchEmailIsNotUnique() {
    SignupData signupData = TestData.getTestSignupData();
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            409,
            5,
            "Error: A user with the provided email already exists");
  }

  @Test
  public void catchPasswordIsNull() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setPassword(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's password must be defined");
  }

  @Test
  public void catchPasswordIsBlank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setPassword("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's password must be defined");
  }

  @Test
  public void catchFirstNameIsNull() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setFirstName(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's first name must be defined");
  }

  @Test
  public void catchFirstNameIsBlank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setFirstName("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's first name must be defined");
  }

  @Test
  public void catchLastNameIsNull() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setLastName(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's last name must be defined");
  }

  @Test
  public void catchLastNameIsBlank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setLastName("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: User's last name must be defined");
  }

  @Test
  public void catchDobIsNull() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setDob(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: User is not old enough");
  }

  @Test
  public void catchUserIsTooYoung() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setDob(new Date());
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: User is not old enough");
  }

  @Test
  public void catchSecurityQuestion1Null() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSq1(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security question 1 must be defined");
  }

  @Test
  public void catchSecurityQuestion1Blank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSq1("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security question 1 must be defined");
  }

  @Test
  public void catchSecurityQuestion2Null() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSq2(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security question 2 must be defined");
  }

  @Test
  public void catchSecurityQuestion2Blank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSq2("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security question 2 must be defined");
  }

  @Test
  public void catchSecurityAnswer1Null() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSa1(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security answer 1 must be defined");
  }

  @Test
  public void catchSecurityAnswer1Blank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSa1("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security answer 1 must be defined");
  }

  @Test
  public void catchSecurityAnswer2Null() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSa2(null);
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security answer 2 must be defined");
  }

  @Test
  public void catchSecurityAnswer2Blank() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setSa2("");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            6,
            "Error: Security answer 2 must be defined");
  }

  @Test
  public void catchRecoveryEmailValid() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setRecoveryEmail("testing");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            3,
            "Error: User's recovery email is not valid");
  }

  @Test
  public void catchRecoveryPhoneValid() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test2@test.com");
    signupData.setRecoveryPhone("123");
    ResponseEntity<String> result = issueRequest(signupData);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            4,
            "Error: User's recovery phone number is not valid");
  }
  
  @Test
  public void allowUserToBeCreatedWithoutSecurityQuestions() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test3@test.com");
    signupData.setSa1(null);
    signupData.setSa2(null);
    signupData.setSq1(null);
    signupData.setSq2(null);
    HttpEntity<SignupData> request = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate
            .postForEntity(fullTestingRoute, request, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), result.getStatusCode());
    Assertions.assertEquals("User Created Successfully", result.getBody());
  }

  private ResponseEntity<String> issueRequest(SignupData data) {
    HttpEntity<SignupData> request = new HttpEntity<>(data, new HttpHeaders());
    return this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
  }

}
