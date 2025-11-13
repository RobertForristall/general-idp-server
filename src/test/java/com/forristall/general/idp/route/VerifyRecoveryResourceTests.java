package com.forristall.general.idp.route;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
import com.forristall.general.idp.entity.RecoveryVerification;
import com.forristall.general.idp.test.data.TestData;
import com.forristall.general.idp.util.SignupData;

/**
 * TODO Handle error codes 2 and 3
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class VerifyRecoveryResourceTests extends BaseIdpApplicationTests {

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;

  private String emailVerificationToken;
  private String phoneVerificationToken;
  private Long userId;
  
  

  @BeforeAll
  public void setup() throws URISyntaxException {
    testingPath = "/recovery/verify";
    testingMethod = RequestMethod.GET;
    fullTestingRoute = new URI(baseUrl + testingPath);
    SignupData signupData = TestData.getTestSignupDataWithRecoveryResources();
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    String verificationToken = signupData.getVerificationToken();
    userId = Long.valueOf(userRepo.findAll().get(0).getId());
    ResponseEntity<String> verificationResult = this.restTemplate.getForEntity(
            baseUrl + "/user/verify"
                    + getVerificationPathVariables(
                            String.valueOf(userId.intValue()),
                            verificationToken),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verificationResult.getStatusCode());
    Assertions.assertEquals("User successfully verified", verificationResult.getBody());
    List<RecoveryVerification> recoveryVerifications = recoveryVerificationRepo.findAll();
    for (RecoveryVerification recoveryVerification: recoveryVerifications) {
      if (recoveryVerification.getUser().getId().equals(userId)) {
        if (recoveryVerification.getRecoveryType().equalsName("email")) {
          emailVerificationToken = recoveryVerification.getVerificationToken();
        }
        if (recoveryVerification.getRecoveryType().equalsName("phone")) {
          phoneVerificationToken = recoveryVerification.getVerificationToken();
        }
      }
    }
  }

  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }

  @Test
  @Order(1)
  public void successfullyVerifyRecoveryEmail() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    String.valueOf(userId.intValue()),
                    "email",
                    emailVerificationToken),
            String.class);
    System.out.println(result.getBody());
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("User's recovery email successfully verified", result.getBody());
  }

  @Test
  @Order(2)
  public void successfullyVerifyRecoveryPhone() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    String.valueOf(userId.intValue()),
                    "phone",
                    phoneVerificationToken),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("User's recovery phone successfully verified", result.getBody());
  }
  
  @Test
  public void catchMissingUserId() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    null,
                    "phone",
                    phoneVerificationToken),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
  }
  
  @Test
  public void catchMissingRecoveryType() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    String.valueOf(userId.intValue()),
                    null,
                    phoneVerificationToken),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
  }
  
  @Test
  public void catchMissingVerificationToken() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    String.valueOf(userId.intValue()),
                    "phone",
                    null),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
  }
  
  @Test
  public void catchInvalidUserIdType() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    "test",
                    "phone",
                    phoneVerificationToken),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: user ID is not of type Integer");
  }
  
  @Test
  public void catchNoRecoveryVerificationEntryFound() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + getRecoveryVerificationPathVariables(
                    String.valueOf(999),
                    "phone",
                    phoneVerificationToken),
            String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            4,
            "Error: no entry in recovery validation table found");
  }
}
