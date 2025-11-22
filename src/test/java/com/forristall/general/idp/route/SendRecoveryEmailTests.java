package com.forristall.general.idp.route;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;

import com.forristall.general.idp.BaseIdpApplicationTests;
import com.forristall.general.idp.entity.RecoveryVerification;
import com.forristall.general.idp.entity.User;
import com.forristall.general.idp.test.data.TestData;
import com.forristall.general.idp.util.SignupData;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class SendRecoveryEmailTests extends BaseIdpApplicationTests {

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;

  private String emailVerificationToken;
  private String phoneVerificationToken;
  private Long userId;
  private String email;
  private String recoveryEmail;

  @BeforeAll
  public void setup() throws URISyntaxException {
    testingPath = "/recovery/email";
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
    for (RecoveryVerification recoveryVerification : recoveryVerifications) {
      if (recoveryVerification.getUser().getId().equals(userId)) {
        if (recoveryVerification.getRecoveryType().equalsName("email")) {
          emailVerificationToken = recoveryVerification.getVerificationToken();
        }
        if (recoveryVerification.getRecoveryType().equalsName("phone")) {
          phoneVerificationToken = recoveryVerification.getVerificationToken();
        }
      }
    }
    String verifyRecoveryEmailUrl = baseUrl + "/recovery/verify" + getRecoveryVerificationPathVariables(
            String.valueOf(userId.intValue()),
            "email",
            emailVerificationToken);
    ResponseEntity<String> verifyRecoveryEmail = this.restTemplate.getForEntity(
            verifyRecoveryEmailUrl,
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verifyRecoveryEmail.getStatusCode());
    email = signupData.getEmail();
    recoveryEmail = signupData.getRecoveryEmail();
  }

  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }

  private Stream<Arguments> provideEmailsForTest() {
    return Stream.of(Arguments.of(email), Arguments.of(recoveryEmail));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("provideEmailsForTest")
  @Order(1)
  public void successfullySendRecoveryEmail(String email) {
    System.out.println(email);
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(fullTestingRoute + sendRecoveryEmailParams(email), String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("If an account was associated with the provided email then a recovery email has been sent...", result.getBody());
  }

  @Test
  public void catchNoEmail() {
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(fullTestingRoute, String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 400, 1, "Error: The 'email' request parameter must be defined");
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource("provideEmailsForTest")
  public void catchNoUserFound() {
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(fullTestingRoute + sendRecoveryEmailParams("fake@test.com"), String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 404, 4, "Error: No user found using provided email");
  }

  @Test
  public void catchUserIsNotVerified() {
    String unverifiedEmail = "unverified@test.com";
    SignupData signupData = TestData.getTestSignupDataWithRecoveryResources();
    signupData.setEmail(unverifiedEmail);
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(fullTestingRoute + sendRecoveryEmailParams(unverifiedEmail), String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 401, 5, "Error: User is not verified");
  }

  @Test
  public void catchRecoveryEmailIsNotVerified() {
    String userEmail = "verified@test.com";
    String unverifiedEmail = "unverifiedRecovery@test.com";
    SignupData signupData = TestData.getTestSignupDataWithRecoveryResources();
    signupData.setEmail(userEmail);
    signupData.setRecoveryEmail(unverifiedEmail);
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    String verificationToken = signupData.getVerificationToken();
    List<User> testUsers = userRepo.findUserByEmail(userEmail);
    Assertions.assertEquals(1, testUsers.size());
    User testUser = testUsers.get(0);
    Long testUserId = Long.valueOf(testUser.getId());
    ResponseEntity<String> verificationResult = this.restTemplate.getForEntity(
            baseUrl + "/user/verify"
                    + getVerificationPathVariables(
                            String.valueOf(testUserId.intValue()),
                            verificationToken),
            String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verificationResult.getStatusCode());
    Assertions.assertEquals("User successfully verified", verificationResult.getBody());
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(fullTestingRoute + sendRecoveryEmailParams(unverifiedEmail), String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 401, 8, "Error: Recovery email is not verified");
  }

}
