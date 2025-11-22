package com.forristall.general.idp.controller;

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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.forristall.general.idp.entity.RecoveryCode;
import com.forristall.general.idp.entity.RecoveryCode.RecoveryType;
import com.forristall.general.idp.entity.RecoveryEmail;
import com.forristall.general.idp.entity.RecoveryVerification;
import com.forristall.general.idp.entity.SecurityQuestion;
import com.forristall.general.idp.entity.User;
import com.forristall.general.idp.repo.RecoveryCodeRepo;
import com.forristall.general.idp.repo.RecoveryEmailRepo;
import com.forristall.general.idp.repo.RecoveryVerificationRepo;
import com.forristall.general.idp.repo.SecurityQuestionRepo;
import com.forristall.general.idp.repo.UserRepo;
import com.forristall.general.idp.service.AuthService;
import com.forristall.general.idp.service.AwsEmailService;
import com.forristall.general.idp.util.GetSecurityQuestions;
import com.forristall.general.idp.util.RecoveryCookie;
import com.forristall.general.idp.util.RecoveryDataAnswers;
import com.forristall.general.idp.util.RestError;
import com.forristall.general.idp.util.RestError.RestErrorBuilder;
import com.nimbusds.jose.JOSEException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/recovery")
public class RecoveryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RecoveryController.class);
  private static final String RECOVERY_PATH_VERIFY_RESOURCE = "/verify/{userId}/{recoveryType}/{verificationToken}";
  private static final String RECOVERY_PATH_SQ = "/questions";
  private static final String RECOVERY_PATH_EMAIL = "/email";
  private static final String RECOVERY_PATH_EMAIL_VERIFY = "/email/verify";

  private final UserRepo userRepo;

  private final SecurityQuestionRepo securityQuestionRepo;
  
  private final RecoveryVerificationRepo recoveryVerificationRepo;
  
  private final RecoveryCodeRepo recoveryCodeRepo;
  
  private final RecoveryEmailRepo recoveryEmailRepo;

  private final AuthService authService;
  
  private final AwsEmailService awsEmailService;

  @Autowired
  RecoveryController(
          UserRepo userRepo,
          SecurityQuestionRepo securityQuestionRepo,
          RecoveryVerificationRepo recoveryVerificationRepo,
          RecoveryCodeRepo recoveryCodeRepo,
          RecoveryEmailRepo recoveryEmailRepo,
          AuthService authService,
          AwsEmailService awsEmailService) {
    this.userRepo = userRepo;
    this.securityQuestionRepo = securityQuestionRepo;
    this.recoveryVerificationRepo = recoveryVerificationRepo;
    this.recoveryCodeRepo = recoveryCodeRepo;
    this.recoveryEmailRepo = recoveryEmailRepo;
    this.authService = authService;
    this.awsEmailService = awsEmailService;
    this.BASE_PATH = "/recovery";
  }
  
  @GetMapping(RECOVERY_PATH_VERIFY_RESOURCE)
  String verifyRecoveryResource(
          @PathVariable Long userId,
          @PathVariable String recoveryType,
          @PathVariable String verificationToken,
          HttpServletResponse response) throws IOException {
    List<RecoveryVerification> verifications = recoveryVerificationRepo
            .findRecoveryVerificationByUserIdAndRecoveryTypeAndVerificationToken(
                    userId,
                    recoveryType.equals("email") ? RecoveryType.EMAIL : RecoveryType.PHONE,
                    verificationToken);
    if (verifications.size() == 1) {
      User user = userRepo.getReferenceById(userId);
      if (recoveryType.equals(RecoveryType.EMAIL.name())) {
        user.getRecoveryEmail().setVerified(true);
      } else {
        user.getRecoveryPhone().setVerified(true);
      }
      user = userRepo.save(user);
      response.setStatus(HttpStatus.OK.value());
      return "User's recovery " + recoveryType + " successfully verified";
    } else {
      RestError restError = new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_VERIFY_RESOURCE))
              .setMethod(RequestMethod.GET)
              .setErrorCode(4)
              .setMsg("Error: no entry in recovery validation table found")
              .build();
      LOGGER.error(restError.toString());
      response.sendError(HttpStatus.BAD_REQUEST.value(), restError.toString());
    }
    return null;
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
                            "ForristallGeneralIdpRecovery",
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
  
  //TODO handle sending recovery token email
  @GetMapping(RECOVERY_PATH_EMAIL)
  String sendRecoveryEmail(
          @PathVariable(name="email", required = true) String email,
          HttpServletResponse response) throws IOException {
    List<User> users = userRepo.findUserByEmail(email);
    if (users.size() == 1) {
      User user = users.getFirst();
      if (user.isVerified()) {
        sendRecoveryCodeEmail(user, email);
        response.setStatus(HttpStatus.OK.value());
        return "If an account was associated with the provided email then a recovery email has been sent...";
      } else {
        response.sendError(
                HttpStatus.UNAUTHORIZED.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_EMAIL))
                        .setMethod(RequestMethod.GET)
                        .setErrorCode(5)
                        .setMsg("Error: User is not verified")
                        .build()
                        .toString());
      }
    } else {
      List<RecoveryEmail> recoveryEmails = recoveryEmailRepo.findRecoveryEmailByEmail(email);
      if (recoveryEmails.size() == 1) {
        RecoveryEmail recoveryEmail = recoveryEmails.getFirst();
        User user = userRepo.findUserByRecoveryEmail(recoveryEmail);
        if (user.isVerified() && recoveryEmail.isVerified()) {
          sendRecoveryCodeEmail(user, email);
          response.setStatus(HttpStatus.OK.value());
          return "If an account was associated with the provided email then a recovery email has been sent...";
        } else {
          response.sendError(
                  HttpStatus.UNAUTHORIZED.value(),
                  new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_EMAIL))
                          .setMethod(RequestMethod.GET)
                          .setErrorCode(user.isVerified() ? 8 : 5)
                          .setMsg("Error: "+(user.isVerified() ? "Recovery email" : "User")+" is not verified")
                          .build()
                          .toString());
        }
      } else {
        response.sendError(
                HttpStatus.NOT_FOUND.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_EMAIL))
                        .setMethod(RequestMethod.GET)
                        .setErrorCode(4)
                        .setMsg("Error: No user found using provided email")
                        .build()
                        .toString());
      }
    }
    return null;
  }
  
  //TODO handle verifying recovery token email and return a recovery token
  @GetMapping(RECOVERY_PATH_EMAIL_VERIFY)
  String verifyEmailRecoveryToken(
          @PathVariable(name = "userId", required = true) Long userId,
          @PathVariable(name = "recoveryToken", required = true) String code,
          HttpServletResponse response) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    List<RecoveryCode> recoveryCodes = recoveryCodeRepo.findRecoveryCodeByCode(code);
    if (recoveryCodes.size() == 1) {
      RecoveryCode recoveryCode = recoveryCodes.get(0);
      if (recoveryCode.getUser().getId() == userId) {
        RecoveryCookie recoveryCookie = authService.generateRecoveryCookie(recoveryCode.getUser());
        response.addCookie(new Cookie("ForristallGeneralIdpRecovery", URLEncoder.encode(recoveryCookie.toString(), StandardCharsets.UTF_8)));
        response.setStatus(HttpStatus.OK.value());
        return "Recovery Token Obtained";
      } else {
        //TODO determine better response message since this information should not be available to potential attackers 
        response.sendError(
                HttpStatus.BAD_REQUEST.value(),
                new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_EMAIL_VERIFY))
                        .setMethod(RequestMethod.GET)
                        .setErrorCode(9)
                        .setMsg("Error: userId does not match user tied to provided recovery code")
                        .build()
                        .toString());
      }
    } else {
      response.sendError(
              HttpStatus.NOT_FOUND.value(),
              new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_EMAIL_VERIFY))
                      .setMethod(RequestMethod.GET)
                      .setErrorCode(10)
                      .setMsg("Error: No recovery code found using provided code")
                      .build()
                      .toString());
    }
    return null;
  }
  
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public String handleError(HttpServletRequest req, MethodArgumentTypeMismatchException ex) {
    if (req.getRequestURI().contains("verify")) {
      RestErrorBuilder builder = new RestErrorBuilder().setRoute(getRoutePath(RECOVERY_PATH_VERIFY_RESOURCE))
              .setMethod(RequestMethod.GET);
      if (ex.getLocalizedMessage().contains("userId")) {
        builder.setErrorCode(1).setMsg("Error: user ID is not of type Integer");
      } else if (ex.getLocalizedMessage().contains("recoveryType")) {
        builder.setErrorCode(2).setMsg("Error: recovery type is not of type String");
      } else {
        builder.setErrorCode(3).setMsg("Error: verification token is not of type String");
      }
      RestError restError = builder.build();
      LOGGER.error(restError.toString());
      return restError.toString();
    }
    LOGGER.error(ex.getLocalizedMessage());
    return ex.getLocalizedMessage();
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
  
  private void sendRecoveryCodeEmail(User user, String email) {
    RecoveryCode recoveryCode = createRecoveryCode(RecoveryType.EMAIL, user);
    recoveryCodeRepo.save(recoveryCode);
    awsEmailService.sendMessage(
            awsEmailService.createSimpleMailMessage(
                    email,
                    "Forristall General IDP User Recovery",
                    awsEmailService.createRecoveryTokenEmailBody(
                            user.getId(),
                            recoveryCode.getCode())));
  }
  
  private RecoveryCode createRecoveryCode(RecoveryType recoveryType, User user) {
    RecoveryCode recoveryCode = new RecoveryCode();
    recoveryCode.setRecoveryType(recoveryType);
    recoveryCode.setCode(RandomStringUtils.secureStrong().nextAlphanumeric(50));
    recoveryCode.setUser(user);
    return recoveryCode;
  }
}
