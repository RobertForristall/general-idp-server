package com.sourceware.labs.idp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.Gson;
import com.sourceware.labs.idp.repo.RecoveryVerificationRepo;
import com.sourceware.labs.idp.repo.UserRepo;
import com.sourceware.labs.idp.service.AwsEmailService;
import com.sourceware.labs.idp.util.HttpErrorResponse;
import com.sourceware.labs.idp.util.RestError;
import com.sourceware.labs.idp.util.RestError.RestErrorBuilder;

/**
 * Base testing class for all tests that need to use the Spring Boot server during the test <br/>
 * Creates an underlying test container to act as a mock database during the process of the tests
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = {
    "server.port=8090",
    "cloud.aws.email.from=from@test.com",
    "cloud.aws.region.static=us-east-1",
    "cloud.aws.region.auto=false",
    "cloud.aws.stack.auto=false",
    "cloud.aws.credentials.access-key=testAccessKey",
    "cloud.aws.credentials.secret-key=testSecretKey",
    "keystore.store.password=testPass",
    "keystore.key.token.password=testKeyPass",
    "spring.datasource.hikari.schema=public" })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIdpApplicationTests {

  /**
   * Test container created in docker that contains a MySQL database whose connection properties are
   * injected into the Spring server for testing database interaction
   */
  protected static PostgreSQLContainer<?> postgres;
  static {
    postgres = new PostgreSQLContainer("postgres:17");
    postgres.start();
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.password", postgres.getPassword());
    System.setProperty("spring.datasource.username", postgres.getUsername());
  }

  /**
   * The port that the server is running on TODO remove this once SpringBootTest properties is fixed
   * so it works on its own
   */
  int port = 8090;

  /**
   * Injected testing rest access point for sending requests to the server
   */
  @Autowired
  protected TestRestTemplate restTemplate;

  @Autowired
  protected UserRepo userRepo;
  
  @Autowired
  protected RecoveryVerificationRepo recoveryVerificationRepo;

  @MockitoBean
  protected AwsEmailService awsEmailService;

  /**
   * Base URL of the testing Spring server
   */
  protected final String baseUrl = "http://localhost:" + port;

  protected void assertRestErrorsEqual(
          ResponseEntity<String> result,
          String route,
          RequestMethod method,
          int responseCode,
          int errorCode,
          String msg) {
    Assertions.assertEquals(HttpStatusCode.valueOf(responseCode), result.getStatusCode());
    RestError expectedError = getRestError(route, method, errorCode, msg);
    RestError actualError = new Gson().fromJson(result.getBody(), HttpErrorResponse.class)
            .getMessageAsRestError();
    if (actualError == null) {
      actualError = new Gson().fromJson(result.getBody(), RestError.class);
    }
    Assertions.assertEquals(expectedError, actualError);
  }

  protected RestError getRestError(String route, RequestMethod method, int code, String msg) {
    return new RestErrorBuilder().setRoute(route)
            .setMethod(method)
            .setErrorCode(code)
            .setMsg(msg)
            .build();
  }

  protected String getVerificationPathVariables(String userId, String verificationToken) {
    return "/%s/%s".formatted(
            userId != null ? userId : "",
            verificationToken != null ? verificationToken : "");
  }

  protected String getSecurityQuestionsParams(String email) {
    return "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
  }

  protected String getRecoveryVerificationPathVariables(
          String userId,
          String recoveryType,
          String token) {
    return "/%s/%s/%s".formatted(
            userId != null ? userId : "",
            recoveryType != null ? recoveryType : "",
            token != null ? token : "");
  }

}
