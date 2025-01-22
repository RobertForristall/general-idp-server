package com.sourceware.labs.idp.route;

import java.net.URI;
import java.net.URISyntaxException;

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

import com.google.gson.Gson;
import com.sourceware.labs.idp.BaseIdpApplicationTests;
import com.sourceware.labs.idp.test.data.TestData;
import com.sourceware.labs.idp.util.GetSecurityQuestions;
import com.sourceware.labs.idp.util.SignupData;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class GetSecurityQuestionsTests extends BaseIdpApplicationTests{

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;
  private String email;
  
  @BeforeAll
  public void setup() throws URISyntaxException {
    testingPath = "/recovery/questions";
    testingMethod = RequestMethod.GET;
    fullTestingRoute = new URI(baseUrl + testingPath);
    SignupData signupData = TestData.getTestSignupData();
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    String verificationToken = signupData.getVerificationToken();
    Long userId = Long.valueOf(userRepo.findAll().get(0).getId());
    ResponseEntity<String> verificationResult = this.restTemplate.getForEntity(baseUrl + "/user/verify" + getVerificationPathVariables(String.valueOf(userId.intValue()), verificationToken), String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verificationResult.getStatusCode());
    Assertions.assertEquals("User successfully verified", verificationResult.getBody());
    email = signupData.getEmail();
  }
  
  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }
  
  @Test
  @Order(1)
  public void successfullyGetSecurityQuestions() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute
                    + getSecurityQuestionsParams(email),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    GetSecurityQuestions questions = new Gson().fromJson(result.getBody(), GetSecurityQuestions.class);
    Assertions.assertEquals(TestData.getTestSecurityQuestion().getQuestion1(), questions.getQuestion1());
    Assertions.assertEquals(TestData.getTestSecurityQuestion().getQuestion2(), questions.getQuestion2());
  }
  
  @Test
  public void catchEmailIsNull() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute,
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: The 'email' request parameter must be defined");
  }
  
  @Test
  public void catchEmailIsBlank() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getSecurityQuestionsParams(""),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: The provided email is not valid");
  }
  
  @Test
  public void catchEmailIsInvalid() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getSecurityQuestionsParams("testing"),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: The provided email is not valid");
  }
  
  @Test
  public void catchNoUserFound() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getSecurityQuestionsParams("test2@test.com"),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            404,
            3,
            "Error: No user found using provided email");
  }
  
  @Test
  public void catchUserIsNotVerified() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test3@test.com");
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getSecurityQuestionsParams(signupData.getEmail()),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            401,
            4,
            "Error: User is not verified");
  }
  
  @Test
  public void catchUserHasNoQuestions() {
    SignupData signupData = TestData.getTestSignupData();
    signupData.setEmail("test4@test.com");
    signupData.setSa1(null);
    signupData.setSa2(null);
    signupData.setSq1(null);
    signupData.setSq2(null);
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    String verificationToken = signupData.getVerificationToken();
    Long userId = Long.valueOf(userRepo.findUserByEmail(signupData.getEmail()).get(0).getId());
    ResponseEntity<String> verificationResult = this.restTemplate.getForEntity(baseUrl + "/user/verify" + getVerificationPathVariables(String.valueOf(userId.intValue()), verificationToken), String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verificationResult.getStatusCode());
    Assertions.assertEquals("User successfully verified", verificationResult.getBody());
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getSecurityQuestionsParams(signupData.getEmail()),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            404,
            5,
            "Error: User did not provide security questions");
  }
  
}
