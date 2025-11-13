package com.forristall.general.idp.route;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.text.ParseException;

import org.bouncycastle.operator.OperatorCreationException;
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
import com.forristall.general.idp.jwt.JwtManager;
import com.forristall.general.idp.keystore.IdpKeyStoreData;
import com.forristall.general.idp.test.data.TestData;
import com.forristall.general.idp.util.RecoveryCookie;
import com.forristall.general.idp.util.RecoveryDataAnswers;
import com.forristall.general.idp.util.SignupData;
import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class RecoveryUsingQuestionsTests extends BaseIdpApplicationTests{

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;
  private Long userId;
  
  private final String storeDir = "testKeyStoreDir";
  private final String storeName = "testKeyStoreName";
  private final String storePass = "testKeyStorePass";
  private final String ecKeyAlias = "ecTestKeyAlias";
  private final String ecKeyPass = "ecTestKeyPass";
  private final String ecKeyId = "ecTestKeyId";
  
  private final IdpKeyStoreData ecIdpKeyStoreData = new IdpKeyStoreData(
          storeDir,
          storeName,
          storePass,
          ecKeyAlias,
          ecKeyPass,
          ecKeyId);
  
  @BeforeAll
  public void setup() throws URISyntaxException {
    testingPath = "/recovery/questions";
    testingMethod = RequestMethod.POST;
    fullTestingRoute = new URI(baseUrl + testingPath);
    SignupData signupData = TestData.getTestSignupData();
    HttpEntity<SignupData> signupRequest = new HttpEntity<>(signupData, new HttpHeaders());
    ResponseEntity<String> signupResult = this.restTemplate
            .postForEntity(baseUrl + "/user/signup", signupRequest, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(201), signupResult.getStatusCode());
    Assertions.assertEquals("User Created Successfully", signupResult.getBody());
    String verificationToken = signupData.getVerificationToken();
    userId = Long.valueOf(userRepo.findAll().get(0).getId());
    ResponseEntity<String> verificationResult = this.restTemplate.getForEntity(baseUrl + "/user/verify" + getVerificationPathVariables(String.valueOf(userId.intValue()), verificationToken), String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), verificationResult.getStatusCode());
    Assertions.assertEquals("User successfully verified", verificationResult.getBody());
  }
  
  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }
  
  @Test
  @Order(1)
  public void successfullyGetRecoveryToken() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("Recovery Cookie Obtained!", result.getBody());
    Assertions.assertTrue(validateCookie(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
  }
  
  @Test
  public void catchEmailisNull() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail(null);
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: Provided email is not valid");
  }
  
  @Test
  public void catchEmailisBlank() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail("");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: Provided email is not valid");
  }
  
  @Test
  public void catchEmailisNotValid() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail("testing");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: Provided email is not valid");
  }
  
  @Test
  public void catchAnswer1isNull() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setAnswer1(null);
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: An answer for the first security question must be supplied");
  }
  
  @Test
  public void catchAnswer1isBlank() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setAnswer1("");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: An answer for the first security question must be supplied");
  }
  
  @Test
  public void catchAnswer2isNull() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setAnswer2(null);
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            3,
            "Error: An answer for the second security question must be supplied");
  }
  
  @Test
  public void catchAnswer2isBlank() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setAnswer2("");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            3,
            "Error: An answer for the second security question must be supplied");
  }
  
  @Test
  public void catchUserIsNotFound() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail("test2@test.com");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            404,
            4,
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
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail(signupData.getEmail());
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            401,
            5,
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
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setEmail(signupData.getEmail());
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            404,
            6,
            "Error: User did not provide security questions");
  }
  
  @Test
  public void catchAnswersDoNotMatch() {
    RecoveryDataAnswers recoveryDataAnswers = TestData.getRecoveryDataAnswers();
    recoveryDataAnswers.setAnswer1("falseAnswer1");
    recoveryDataAnswers.setAnswer2("falseAnswer2");
    HttpEntity<RecoveryDataAnswers> request = new HttpEntity<>(recoveryDataAnswers, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            401,
            7,
            "Error: Answers do not match");
  }
  
  private boolean validateCookie(String cookie) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    String decodedCookie = URLDecoder.decode(cookie, StandardCharsets.UTF_8);
    String cookieId = decodedCookie.split("=")[0];
    RecoveryCookie recoveryCookie = new Gson().fromJson(decodedCookie.split("=")[1], RecoveryCookie.class);
    Assertions.assertEquals("ForristallGeneralIdpRecovery", cookieId);
    Assertions.assertEquals(userId, recoveryCookie.getUserId());
    validateJwtClaimSet(recoveryCookie.getAccessToken());
    return true;
  }
  
  private void validateJwtClaimSet(String jwt) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    JWTClaimsSet claimSet = JwtManager.getClaimsSetFromJwt(jwt, JWSAlgorithm.ES256, ecIdpKeyStoreData);
    Assertions.assertEquals(userId, claimSet.getLongClaim("userId"));
  }
  
}
