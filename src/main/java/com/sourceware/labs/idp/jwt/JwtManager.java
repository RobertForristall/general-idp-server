package com.sourceware.labs.idp.jwt;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bouncycastle.operator.OperatorCreationException;

import com.google.gson.Gson;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sourceware.labs.idp.keystore.IdpKeyStoreAccessor;
import com.sourceware.labs.idp.keystore.IdpKeyStoreData;

/**
 * Class for managing JWT tokens (Signing/Validation)
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public class JwtManager {

  /**
   * Function for getting a signed JWT token
   * 
   * @param securityAlg {@link JWSAlgorithm} that will be used for the key, Supported:
   *          {@link JWSAlgorithm.RS384}, {@link JWSAlgorithm.ES256}
   * @param clientId {@link String} ID identifying the application requesting a token
   * @param audience {@link String} endpoints that the token can be used for
   * @param tokenExpiration Time (long) that a token should be valid
   * @param keyStoreData {@link IdpKeyStoreData} that contains information related to the keystore
   * @return {@link SignedJWT} that can be used to validate the user's session
   * @throws KeyStoreException If there is an issue accessing the keystore
   * @throws NoSuchAlgorithmException If there is an issue finding the algorithm to use for key
   *           signing/creation
   * @throws CertificateException If the certificate associated with the key is no longer valid
   * @throws UnrecoverableEntryException If the key entry in the KeyStore is no longer able to be
   *           accessed due to error/corruption
   * @throws OperatorCreationException If there is an issue creating a certificate writer
   * @throws IOException If there is an issue accessing the KeyStore file using I/O operations
   * @throws JOSEException If there is an issue creating/manipulating a key set
   */
  public static SignedJWT getSignedJwtToken(
          JWSAlgorithm securityAlg,
          String clientId,
          String audience,
          long tokenExpiration,
          IdpKeyStoreData keyStoreData,
          Long userId,
          String applicationName,
          String roleName,
          List<String> additionalPermissions) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, IOException, OperatorCreationException, JOSEException {
    JWK key = IdpKeyStoreAccessor.accessKeyStore(securityAlg, keyStoreData);
    Date now = new Date();
    JWSHeader header = new JWSHeader.Builder((JWSAlgorithm) key.getAlgorithm())
            .keyID(key.getKeyID())
            .type(JOSEObjectType.JWT)
            .build();
    JWTClaimsSet jwt = new JWTClaimsSet.Builder().issuer(clientId)
            .subject(clientId)
            .audience(audience)
            .claim("kid", key.getKeyID())
            .notBeforeTime(Date.from(now.toInstant().minusSeconds(5)))
            .expirationTime(Date.from(now.toInstant().plusSeconds((tokenExpiration))))
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", userId)
            .claim("application", applicationName)
            .claim("roleName", roleName)
            .claim("additionalPermissions", new Gson().toJson(additionalPermissions))
            .build();
    SignedJWT signedJWT = new SignedJWT(header, jwt);
    JWSSigner signer = securityAlg.equals(JWSAlgorithm.RS384)
            ? new RSASSASigner(key.toRSAKey().toRSAPrivateKey())
            : new ECDSASigner(key.toECKey().toECPrivateKey());
    signedJWT.sign(signer);
    return signedJWT;
  }
  
  public static SignedJWT getSignedJwtToken(
          JWSAlgorithm securityAlg,
          String clientId,
          String audience,
          long tokenExpiration,
          IdpKeyStoreData keyStoreData,
          Long userId) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, IOException, OperatorCreationException, JOSEException {
    JWK key = IdpKeyStoreAccessor.accessKeyStore(securityAlg, keyStoreData);
    Date now = new Date();
    JWSHeader header = new JWSHeader.Builder((JWSAlgorithm) key.getAlgorithm())
            .keyID(key.getKeyID())
            .type(JOSEObjectType.JWT)
            .build();
    JWTClaimsSet jwt = new JWTClaimsSet.Builder().issuer(clientId)
            .subject(clientId)
            .audience(audience)
            .claim("kid", key.getKeyID())
            .notBeforeTime(Date.from(now.toInstant().minusSeconds(5)))
            .expirationTime(Date.from(now.toInstant().plusSeconds((tokenExpiration))))
            .jwtID(UUID.randomUUID().toString())
            .claim("userId", userId)
            .build();
    SignedJWT signedJWT = new SignedJWT(header, jwt);
    JWSSigner signer = securityAlg.equals(JWSAlgorithm.RS384)
            ? new RSASSASigner(key.toRSAKey().toRSAPrivateKey())
            : new ECDSASigner(key.toECKey().toECPrivateKey());
    signedJWT.sign(signer);
    return signedJWT;
  }

  /**
   * Function for verifying a Signed JWT
   * 
   * @param jwt {@link String} representation of the token
   * @param securityAlg {@link JWSAlgorithm} that the token was signed using
   * @param keyStoreData {@link IdpKeyStoreData} that contains information related to the keystore
   * @return True if the token is valid and False otherwise
   * @throws KeyStoreException if there is an issue accessing the keystore
   * @throws NoSuchAlgorithmException if the provided algorithm is not accepted
   * @throws CertificateException if there is an issue generating/fetching the certificate
   *           associated with a key
   * @throws UnrecoverableEntryException if there is an issue getting the specific key in the
   *           keystore
   * @throws OperatorCreationException if there is an issue creating a certificate writer for the
   *           key
   * @throws IOException if there is an IO related issue when trying to access the keystore
   * @throws JOSEException If there is an issue creating the token verifier
   * @throws ParseException if there is an issue parsing the provided JWT
   */
  public static boolean verifySignedJwtToken(
          String jwt,
          JWSAlgorithm securityAlg,
          IdpKeyStoreData keyStoreData) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException, ParseException {
    JWK key = IdpKeyStoreAccessor.accessKeyStore(securityAlg, keyStoreData);
    SignedJWT signedJwt = SignedJWT.parse(jwt);
    JWSVerifier verifier = securityAlg.equals(JWSAlgorithm.RS384)
            ? new RSASSAVerifier(key.toRSAKey())
            : new ECDSAVerifier(key.toECKey());
    return signedJwt.verify(verifier);
  }
  
  public static JWTClaimsSet getClaimsSetFromJwt(
          String jwt,
          JWSAlgorithm securityAlg,
          IdpKeyStoreData keyStoreData) throws ParseException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    JWK key = IdpKeyStoreAccessor.accessKeyStore(securityAlg, keyStoreData);
    SignedJWT signedJwt = SignedJWT.parse(jwt);
    return signedJwt.getJWTClaimsSet();
  }

}
