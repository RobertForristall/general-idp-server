package com.sourceware.labs.idp.service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bouncycastle.operator.OperatorCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.sourceware.labs.idp.entity.Role;
import com.sourceware.labs.idp.entity.User;
import com.sourceware.labs.idp.jwt.JwtManager;
import com.sourceware.labs.idp.keystore.IdpKeyStoreData;
import com.sourceware.labs.idp.util.RecoveryCookie;
import com.sourceware.labs.idp.util.SessionCookie;

@Service
public class AuthService {

  private String clientId;

  private IdpKeyStoreData idpKeyStoreData;
  
  public AuthService(
          @Value("${keystore.dir.name}") String keyStoreDir,
          @Value("${keystore.store.name}") String keyStoreName,
          @Value("${keystore.store.password}") String keyStorePassword,
          @Value("${keystore.key.token.alias}") String tokenAlias,
          @Value("${keystore.key.token.password}") String tokenPassword,
          @Value("${jwt.clientId}") String clientId) {
    idpKeyStoreData = new IdpKeyStoreData(
            keyStoreDir,
            keyStoreName,
            keyStorePassword,
            tokenAlias,
            tokenPassword,
            "initial");
  }

  public SessionCookie generateSessionCookie(
          User user,
          String applicationName) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    Role userRole = user.getRoles()
            .stream()
            .filter(role -> role.getApplication().equalsName(applicationName))
            .findFirst()
            .orElse(null);
    if (userRole == null || userRole.getRole() == null) {
      throw new IllegalStateException("User should have a role for the application");
    }
    String accessToken = JwtManager
            .getSignedJwtToken(
                    JWSAlgorithm.ES256,
                    clientId,
                    applicationName,
                    60 * 10,
                    idpKeyStoreData,
                    user.getId(),
                    applicationName,
                    userRole.getRole().name(),
                    user.getAdditionalPermissions()
                    .stream()
                    .filter(perm -> perm.getApplication().equalsName(applicationName))
                    .map(perm -> perm.getPermission())
                    .collect(Collectors.toList()))
            .serialize();
    String refreshToken = JwtManager
            .getSignedJwtToken(
                    JWSAlgorithm.ES256,
                    clientId,
                    applicationName,
                    60 * 60,
                    idpKeyStoreData,
                    user.getId(),
                    applicationName,
                    userRole.getRole().name(),
                    user.getAdditionalPermissions()
                    .stream()
                    .filter(perm -> perm.getApplication().equalsName(applicationName))
                    .map(perm -> perm.getPermission())
                    .collect(Collectors.toList()))
            .serialize();
    return new SessionCookie(
            user.getId(),
            applicationName,
            userRole.getRole().toString(),
            user.getAdditionalPermissions()
                    .stream()
                    .filter(perm -> perm.getApplication().equalsName(applicationName))
                    .map(perm -> perm.getPermission())
                    .collect(Collectors.toList()),
            accessToken,
            refreshToken);
  }
  
  public RecoveryCookie generateRecoveryCookie(User user) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    return new RecoveryCookie(
            user.getId(),
            JwtManager
            .getSignedJwtToken(
                    JWSAlgorithm.ES256,
                    clientId,
                    clientId,
                    60 * 1,
                    idpKeyStoreData,
                    user.getId())
            .serialize());
  }

  public boolean verifyAccessToken(
          SessionCookie cookie) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException, ParseException {
    return JwtManager
            .verifySignedJwtToken(cookie.getAccessToken(), JWSAlgorithm.ES256, idpKeyStoreData);
  }

  public Optional<SessionCookie> verifyRefreshToken(
          SessionCookie cookie) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException, ParseException {
    if (JwtManager
            .verifySignedJwtToken(cookie.getRefreshToken(), JWSAlgorithm.ES256, idpKeyStoreData)) {
      JWTClaimsSet claimsSet = JwtManager.getClaimsSetFromJwt(cookie.getRefreshToken(), JWSAlgorithm.ES256, idpKeyStoreData);
      Long userId = claimsSet.getLongClaim("userId");
      String applicationName = claimsSet.getStringClaim("application");
      String roleName = claimsSet.getStringClaim("roleName");
      List<String> listAdditionalPermissions = List.of(new Gson().fromJson(claimsSet.getStringClaim("additionalPermissions"), String[].class));
      cookie.setAccessToken(
              JwtManager
                      .getSignedJwtToken(
                              JWSAlgorithm.ES256,
                              clientId,
                              cookie.getApplication(),
                              60 * 10,
                              idpKeyStoreData,
                              userId,
                              applicationName,
                              roleName,
                              listAdditionalPermissions)
                      .serialize());
      cookie.setRefreshToken(
              JwtManager
                      .getSignedJwtToken(
                              JWSAlgorithm.ES256,
                              clientId,
                              cookie.getApplication(),
                              60 * 60,
                              idpKeyStoreData,
                              userId,
                              applicationName,
                              roleName,
                              listAdditionalPermissions)
                      .serialize());
      return Optional.of(cookie);
    } else {
      return Optional.empty();
    }
  }
  
  

}
