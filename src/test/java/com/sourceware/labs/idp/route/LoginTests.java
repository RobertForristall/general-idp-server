package com.sourceware.labs.idp.route;

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
import java.util.List;

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

import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.sourceware.labs.idp.BaseIdpApplicationTests;
import com.sourceware.labs.idp.jwt.JwtManager;
import com.sourceware.labs.idp.keystore.IdpKeyStoreData;
import com.sourceware.labs.idp.test.data.TestData;
import com.sourceware.labs.idp.util.LoginData;
import com.sourceware.labs.idp.util.SessionCookie;
import com.sourceware.labs.idp.util.SignupData;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class LoginTests extends BaseIdpApplicationTests{
  
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
    testingPath = "/user/login";
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
  public void successfullyLoginUser() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    LoginData loginData = TestData.getLoginData();
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("Login Successful", result.getBody());
    Assertions.assertTrue(validateCookie(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
  }
  
  @Test
  public void catchEmailIsNull() {
    LoginData loginData = TestData.getLoginData();
    loginData.setEmail(null);
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: User's email is not valid");
  }
  
  @Test
  public void catchEmailIsBlank() {
    LoginData loginData = TestData.getLoginData();
    loginData.setEmail("");
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
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
    LoginData loginData = TestData.getLoginData();
    loginData.setEmail("testing");
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            1,
            "Error: User's email is not valid");
  }
  
  @Test
  public void catchPasswordIsNull() {
    LoginData loginData = TestData.getLoginData();
    loginData.setPassword(null);
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: Password must have a non-null and non-blank value");
  }
  
  @Test
  public void catchPasswordIsBlank() {
    LoginData loginData = TestData.getLoginData();
    loginData.setPassword("");
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            400,
            2,
            "Error: Password must have a non-null and non-blank value");
  }
  
  @Test
  public void noUserFound() {
    LoginData loginData = TestData.getLoginData();
    loginData.setPassword("12345");
    HttpEntity<LoginData> request = new HttpEntity<>(loginData, new HttpHeaders());
    ResponseEntity<String> result = this.restTemplate.postForEntity(fullTestingRoute, request, String.class);
    assertRestErrorsEqual(
            result,
            testingPath,
            testingMethod,
            401,
            3,
            "Error: No user found using provided credentials");
  }
  
  private boolean validateCookie(String cookie) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    //TODO Implement me!!!
    String decodedCookie = URLDecoder.decode(cookie, StandardCharsets.UTF_8);
    String cookieId = decodedCookie.split("=")[0];
    SessionCookie sessionCookie = new Gson().fromJson(decodedCookie.split("=")[1], SessionCookie.class);
    Assertions.assertEquals("SourcewareLabIdp", cookieId);
    Assertions.assertEquals(userId, sessionCookie.getUserId());
    Assertions.assertEquals("RealQuick", sessionCookie.getApplication());
    Assertions.assertEquals("User", sessionCookie.getRole());
    Assertions.assertIterableEquals(List.of(), sessionCookie.getAdditionalPermissions());
    validateJwtClaimSet(sessionCookie.getAccessToken());
    validateJwtClaimSet(sessionCookie.getRefreshToken());
    return true;
  }
  
  public void validateJwtClaimSet(String jwt) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    JWTClaimsSet claimSet = JwtManager.getClaimsSetFromJwt(jwt, JWSAlgorithm.ES256, ecIdpKeyStoreData);
    Assertions.assertEquals(userId, claimSet.getLongClaim("userId"));
    Assertions.assertEquals("RealQuick", claimSet.getStringClaim("application"));
    Assertions.assertEquals("User", claimSet.getStringClaim("roleName"));
    Assertions.assertIterableEquals(List.of(), List.of(new Gson().fromJson(claimSet.getStringClaim("additionalPermissions"), String[].class)));
  }

}
