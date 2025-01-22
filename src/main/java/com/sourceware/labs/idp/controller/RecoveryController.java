package com.sourceware.labs.idp.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.validator.routines.EmailValidator;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.nimbusds.jose.JOSEException;
import com.sourceware.labs.idp.entity.SecurityQuestion;
import com.sourceware.labs.idp.entity.User;
import com.sourceware.labs.idp.repo.SecurityQuestionRepo;
import com.sourceware.labs.idp.repo.UserRepo;
import com.sourceware.labs.idp.service.AuthService;
import com.sourceware.labs.idp.util.GetSecurityQuestions;
import com.sourceware.labs.idp.util.RecoveryCookie;
import com.sourceware.labs.idp.util.RecoveryDataAnswers;
import com.sourceware.labs.idp.util.RestError;
import com.sourceware.labs.idp.util.RestError.RestErrorBuilder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/recovery")
public class RecoveryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryController.class);
  private static final String RECOVERY_PATH_SQ = "/questions";
  private static final String RECOVERY_PATH_EMAIL = "/email";
  private static final String RECOVERY_PATH_EMAIL_VERIFY = "/email/verify";

  private final UserRepo userRepo;

  private final SecurityQuestionRepo securityQuestionRepo;

  private final AuthService authService;

  @Autowired
  RecoveryController(
          UserRepo userRepo,
          SecurityQuestionRepo securityQuestionRepo,
          AuthService authService) {
    this.userRepo = userRepo;
    this.securityQuestionRepo = securityQuestionRepo;
    this.authService = authService;
    this.BASE_PATH = "/recovery";
  }

  @GetMapping(RECOVERY_PATH_SQ)
  String getRecoveryQuestions(
          @RequestParam(name = "email", required = true) String email,
          HttpServletResponse response) throws IOException {
    email = URLDecoder.decode(email, StandardCharsets.UTF_8);
    if (!EmailValidator.getInstance().isValid(email)) {
      response.sendError(
              HttpStatus.BAD_REQUEST.value(),
              new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                      .setMethod(RequestMethod.GET)
                      .setErrorCode(2)
                      .setMsg("Error: The provided email is not valid")
                      .build()
                      .toString());
    } else {
      List<User> users = userRepo.findUserByEmail(email);
      if (users.size() != 1) {
        response.sendError(
                HttpStatus.NOT_FOUND.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                        .setMethod(RequestMethod.GET)
                        .setErrorCode(3)
                        .setMsg("Error: No user found using provided email")
                        .build()
                        .toString());
      } else if (!users.get(0).isVerified()) {
        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                        .setMethod(RequestMethod.GET)
                        .setErrorCode(4)
                        .setMsg("Error: User is not verified")
                        .build()
                        .toString());
      } else {
        List<SecurityQuestion> questions = securityQuestionRepo
                .findSecurityQuestionByUser(users.get(0));
        if (questions.size() != 1) {
          response.sendError(
                  HttpStatus.NOT_FOUND.value(),
                  new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                          .setMethod(RequestMethod.GET)
                          .setErrorCode(5)
                          .setMsg("Error: User did not provide security questions")
                          .build()
                          .toString());
        } else {
          response.setStatus(HttpStatus.OK.value());
          return new GetSecurityQuestions(
                  questions.get(0).getQuestion1(),
                  questions.get(0).getQuestion2()).toString();
        }
      }
    }
    return null;
  }

  @PostMapping(RECOVERY_PATH_SQ)
  String recoveryUsingQuestions(
          @RequestBody RecoveryDataAnswers recoveryDataQuestions,
          HttpServletResponse response) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, JOSEException {
    Optional<RestError> error = recoveryDataQuestions
            .isDataValid(getRoutePath(RECOVERY_PATH_SQ), RequestMethod.POST);
    if (error.isPresent()) {
      response.sendError(HttpStatus.BAD_REQUEST.value(), error.get().toString());
    } else {
      List<User> users = userRepo.findUserByEmail(recoveryDataQuestions.getEmail());
      if (users.size() != 1) {
        response.sendError(
                HttpStatus.NOT_FOUND.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                        .setMethod(RequestMethod.POST)
                        .setErrorCode(4)
                        .setMsg("Error: No user found using provided email")
                        .build()
                        .toString());
      } else if (!users.get(0).isVerified()) {
        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                        .setMethod(RequestMethod.POST)
                        .setErrorCode(5)
                        .setMsg("Error: User is not verified")
                        .build()
                        .toString());
      } else {
        List<SecurityQuestion> questions = securityQuestionRepo
                .findSecurityQuestionByUser(users.get(0));
        if (questions.size() != 1) {
          response.sendError(
                  HttpStatus.NOT_FOUND.value(),
                  new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                          .setMethod(RequestMethod.POST)
                          .setErrorCode(6)
                          .setMsg("Error: User did not provide security questions")
                          .build()
                          .toString());
        } else {
          SecurityQuestion securityQuestion = questions.get(0);
          if (securityQuestion.getAnswer1().equals(recoveryDataQuestions.getAnswer1())
                  && securityQuestion.getAnswer2().equals(recoveryDataQuestions.getAnswer2())) {
            RecoveryCookie cookie = authService.generateRecoveryCookie(users.get(0));
            response.addCookie(
                    new Cookie(
                            "SourcewareLabIdpRecovery",
                            URLEncoder.encode(cookie.toString(), StandardCharsets.UTF_8)));
            response.setStatus(HttpStatus.OK.value());
            return "Recovery Cookie Obtained!";
          } else {
            response.sendError(
                    HttpStatus.UNAUTHORIZED.value(),
                    new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
                            .setMethod(RequestMethod.POST)
                            .setErrorCode(7)
                            .setMsg("Error: Answers do not match")
                            .build()
                            .toString());
          }
        }
      }
    }
    return null;
  }

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public String handleError(HttpServletRequest req, MissingServletRequestParameterException ex) {
    if (req.getRequestURI().contains("questions")) {
      RestError restError = new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_SQ))
              .setMethod(RequestMethod.GET)
              .setErrorCode(1)
              .setMsg("Error: The 'email' request parameter must be defined")
              .build();
      LOGGER.error(restError.toString());
      return restError.toString();
    }
    LOGGER.error(ex.getLocalizedMessage());
    return ex.getLocalizedMessage();
  }
}
