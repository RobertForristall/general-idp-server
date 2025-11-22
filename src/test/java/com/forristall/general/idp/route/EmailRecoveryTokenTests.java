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

import com.forristall.general.idp.BaseIdpApplicationTests;
import com.forristall.general.idp.entity.RecoveryVerification;
import com.forristall.general.idp.entity.User;
import com.forristall.general.idp.jwt.JwtManager;
import com.forristall.general.idp.keystore.IdpKeyStoreData;
import com.forristall.general.idp.test.data.TestData;
import com.forristall.general.idp.util.RecoveryCookie;
import com.forristall.general.idp.util.SignupData;
import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class EmailRecoveryTokenTests extends BaseIdpApplicationTests{

  private String testingPath;
  private RequestMethod testingMethod;
  private URI fullTestingRoute;

  private Long userId;
  private String recoveryToken;
  
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
    testingPath = "/recovery/email/verify";
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
          String emailVerificationToken = recoveryVerification.getVerificationToken();
          String verifyRecoveryEmailUrl = baseUrl + "/recovery/verify" + getRecoveryVerificationPathVariables(
                  String.valueOf(userId.intValue()),
                  "email",
                  emailVerificationToken);
          ResponseEntity<String> verifyRecoveryEmail = this.restTemplate.getForEntity(
                  verifyRecoveryEmailUrl,
                  String.class);
          Assertions.assertEquals(HttpStatusCode.valueOf(200), verifyRecoveryEmail.getStatusCode());
        }
      }
    }
    ResponseEntity<String> result = this.restTemplate
            .getForEntity(baseUrl + "/recovery/email" + sendRecoveryEmailParams(signupData.getEmail()), String.class);
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("If an account was associated with the provided email then a recovery email has been sent...", result.getBody());
    User user = userRepo.findUserByEmail(signupData.getEmail()).get(0);
    recoveryToken = user.getRecoveryCode().getCode();
  }
  
  @AfterAll
  public void cleanup() {
    userRepo.deleteAll();
  }
  
  @Test
  @Order(1)
  public void successfullyGetRecoveryToken() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + verifyRecoveryEmailTokenParams(
                    String.valueOf(userId.intValue()),
                    recoveryToken),
            String.class);
    System.out.println(result.getBody());
    Assertions.assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    Assertions.assertEquals("Recovery Token Obtained", result.getBody());
    Assertions.assertTrue(validateCookie(result.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
  }
  
  @Test
  public void catchMissingUserId() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + verifyRecoveryEmailTokenParams(
                    "",
                    recoveryToken),
            String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 400, 1, "Error: The 'userId' request parameter must be defined");
  }
  
  @Test
  public void catchInvalidDatatypeForUserId() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + verifyRecoveryEmailTokenParams(
                    "test",
                    recoveryToken),
            String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 400, 1, "Error: user ID is not of type Integer");
  }
  
  @Test
  public void catchUserIdParamDoesNotMatchRecoveryCode() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + verifyRecoveryEmailTokenParams(
                    String.valueOf(99),
                    recoveryToken),
            String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 400, 9, "Error: userId does not match user tied to provided recovery code");
  }
  
  @Test
  public void catchNoRecoveryCodeFound() {
    ResponseEntity<String> result = this.restTemplate.getForEntity(
            fullTestingRoute + verifyRecoveryEmailTokenParams(
                    String.valueOf(userId.intValue()),
                    "invalid"),
            String.class);
    assertRestErrorsEqual(result, testingPath, testingMethod, 404, 10, "Error: No recovery code found using provided code");
  }
  
  private boolean validateCookie(String cookie) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    String decodedCookie = URLDecoder.decode(cookie, StandardCharsets.UTF_8);
    String cookieId = decodedCookie.split("=")[0];
    RecoveryCookie sessionCookie = new Gson().fromJson(decodedCookie.split("=")[1], RecoveryCookie.class);
    Assertions.assertEquals("ForristallGeneralIdpRecovery", cookieId);
    Assertions.assertEquals(userId, sessionCookie.getUserId());
    validateJwtClaimSet(sessionCookie.getAccessToken());
    return true;
  }
  
  public void validateJwtClaimSet(String jwt) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, ParseException, IOException, JOSEException {
    JWTClaimsSet claimSet = JwtManager.getClaimsSetFromJwt(jwt, JWSAlgorithm.ES256, ecIdpKeyStoreData);
    Assertions.assertEquals(userId, claimSet.getLongClaim("userId"));
  }
}
