package com.forristall.general.idp.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

/**
 * Class for managing the IDP Java Keystore
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public class IdpKeyStoreAccessor {

  // Type of KeyStore to be accessed/created
  private static final String KS_TYPE = "PKCS12";

  /**
   * Function for only preparing a new KeyStore
   * 
   * @param securityAlg {@link JWSAlgorithm} that will be used for the key, Supported:
   *          {@link JWSAlgorithm.RS384}, {@link JWSAlgorithm.ES256}
   * 
   * @param keyStoreData {@link IdpKeyStoreData} that contains information related to the keystore
   * @return true if everything was created/prepared successfully and false if there was an
   *         exception
   */
  public static boolean prepareKeyStoreAndSet(
          JWSAlgorithm securityAlg,
          IdpKeyStoreData keyStoreData) {
    try {
      JWK key = accessKeyStore(securityAlg, keyStoreData);
      if (key == null) {
        System.err.println("Failed to create key store and JWK Set...");
        return false;
      }
      return true;
    } catch (Exception ex) {
      System.err.println("Error preparing key store: " + ex.getLocalizedMessage());
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Helper function for getting a key from an existing KeyStore or creating a new one if one did
   * not already exist
   * 
   * @param securityAlg {@link JWSAlgorithm} that will be used for the key, Supported:
   *          {@link JWSAlgorithm.RS384}, {@link JWSAlgorithm.ES256}
   * 
   * @param keyStoreData {@link IdpKeyStoreData} that contains information related to the keystore
   * 
   * @return {@link ECKey} from the KeyStore
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
  public static JWK accessKeyStore(
          JWSAlgorithm securityAlg,
          IdpKeyStoreData keyStoreData) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, JOSEException {
    KeyStore ks = KeyStore.getInstance(KS_TYPE);

    try (FileInputStream fis = new FileInputStream(
            keyStoreData.getStoreDir() + "/" + keyStoreData.getStoreFileName())) {
      ks.load(fis, keyStoreData.getStorePassword().toCharArray());
    } catch (FileNotFoundException ex) {
      ks.load(null, keyStoreData.getStorePassword().toCharArray());
    }

    KeyStore.ProtectionParameter keyProtParam = new KeyStore.PasswordProtection(
            keyStoreData.getKeyPassword().toCharArray());

    KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks
            .getEntry(keyStoreData.getKeyAlias(), keyProtParam);
    if (pkEntry == null) {
      JWK key = generateNewPrivateKey(securityAlg, keyStoreData.getKeyId());
      PublicKey publicKey = securityAlg.equals(JWSAlgorithm.RS384) ? key.toRSAKey().toPublicKey()
              : key.toECKey().toPublicKey();
      PrivateKey privateKey = securityAlg.equals(JWSAlgorithm.RS384) ? key.toRSAKey().toPrivateKey()
              : key.toECKey().toPrivateKey();
      X500Name certName = new X500Name("CN=Robert F., O=Forristall Services, C=US");
      SubjectPublicKeyInfo pubKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
      final Date start = new Date();
      final Date until = Date.from(
              LocalDate.now().plus(365, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.UTC));
      final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
              certName,
              new BigInteger(10, new SecureRandom()),
              start,
              until,
              certName,
              pubKeyInfo);
      ContentSigner contentSigner = new JcaContentSignerBuilder(
              securityAlg.equals(JWSAlgorithm.RS384) ? "SHA256WithRSAEncryption"
                      : "SHA256withECDSA").build(privateKey);
      X509Certificate certificate = new JcaX509CertificateConverter()
              .setProvider(new BouncyCastleProvider())
              .getCertificate(certificateBuilder.build(contentSigner));
      Certificate[] certChain = new Certificate[1];
      certChain[0] = certificate;
      pkEntry = new KeyStore.PrivateKeyEntry(privateKey, certChain);
      ks.setEntry(keyStoreData.getKeyAlias(), pkEntry, keyProtParam);
      File keyStoreOutputDir = new File(keyStoreData.getStoreDir());
      if (!keyStoreOutputDir.exists()) {
        keyStoreOutputDir.mkdir();
      }
      try (FileOutputStream fos = new FileOutputStream(
              keyStoreData.getStoreDir() + "/" + keyStoreData.getStoreFileName())) {
        ks.store(fos, keyStoreData.getStorePassword().toCharArray());
      }
    }
    return securityAlg.equals(JWSAlgorithm.RS384)
            ? new RSAKey.Builder((RSAPublicKey) pkEntry.getCertificate().getPublicKey())
                    .privateKey((RSAPrivateKey) pkEntry.getPrivateKey())
                    .algorithm(JWSAlgorithm.RS384)
                    .keyID(keyStoreData.getKeyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .build()
            : new ECKey.Builder(Curve.P_256, (ECPublicKey) pkEntry.getCertificate().getPublicKey())
                    .privateKey((ECPrivateKey) pkEntry.getPrivateKey())
                    .algorithm(JWSAlgorithm.ES256)
                    .keyID(keyStoreData.getKeyId())
                    .keyUse(KeyUse.SIGNATURE)
                    .build();
  }

  /**
   * Helper function for creating a new key and JWK set
   * 
   * @param keyId {@link String} ID of the key in the store file to obtain that relates to a public
   *          key in hosted JWK set
   * @return {@link ECKey} representing the newly created key
   * @throws NoSuchAlgorithmException If there is an issue finding the algorithm to use for key
   *           signing/creation
   * @throws FileNotFoundException If there is an issue creating a file output stream
   * @throws IOException If there is an issue accessing the KeyStore file using I/O operations
   * @throws JOSEException If there is an issue creating/manipulating a key set
   */
  private static JWK generateNewPrivateKey(
          JWSAlgorithm securityAlg,
          String keyId) throws NoSuchAlgorithmException, FileNotFoundException, IOException, JOSEException {
    JWK key = generateKeyForProvider(securityAlg, keyId);
    // JWKSet jwkSet = new JWKSet(key);
    // File keySetOutputDir = new File("ks");
    // if (!keySetOutputDir.exists()) {
    // keySetOutputDir.mkdir();
    // }
    // String formattedDate = new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date());
    // try (FileOutputStream fos = new FileOutputStream(
    // "ks" + "/jwk_set-" + keyId + "-" + formattedDate + ".json")) {
    // fos.write(jwkSet.toString().getBytes());
    // fos.flush();
    // }
    return key;
  }

  private static JWK generateKeyForProvider(
          JWSAlgorithm securityAlg,
          String keyId) throws NoSuchAlgorithmException {
    if (securityAlg.equals(JWSAlgorithm.RS384)) {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048, SecureRandom.getInstanceStrong());
      KeyPair pair = keyGen.generateKeyPair();
      return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
              .privateKey((RSAPrivateKey) pair.getPrivate())
              .algorithm(JWSAlgorithm.RS384)
              .keyID(keyId)
              .keyUse(KeyUse.SIGNATURE)
              .build();
    } else {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
      keyGen.initialize(256, SecureRandom.getInstanceStrong());
      KeyPair pair = keyGen.generateKeyPair();
      return new ECKey.Builder(Curve.P_256, (ECPublicKey) pair.getPublic())
              .privateKey((ECPrivateKey) pair.getPrivate())
              .algorithm(JWSAlgorithm.ES256)
              .keyID(keyId)
              .keyUse(KeyUse.SIGNATURE)
              .build();
    }
  }
}
