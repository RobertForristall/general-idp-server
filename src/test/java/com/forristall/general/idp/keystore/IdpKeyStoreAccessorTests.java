package com.forristall.general.idp.keystore;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

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

import com.forristall.general.idp.keystore.IdpKeyStoreAccessor;
import com.forristall.general.idp.keystore.IdpKeyStoreData;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class IdpKeyStoreAccessorTests {

  private final String storeDir = "testKeyStoreDir";
  private final String storeName = "testKeyStoreName";
  private final String storePass = "testKeyStorePass";
  private final String ecKeyAlias = "ecTestKeyAlias";
  private final String ecKeyPass = "ecTestKeyPass";
  private final String ecKeyId = "ecTestKeyId";
  private final String rsaKeyAlias = "rsaTestKeyAlias";
  private final String rsaKeyPass = "rsaTestKeyPass";
  private final String rsaKeyId = "rsaTestKeyId";

  private final IdpKeyStoreData ecIdpKeyStoreData = new IdpKeyStoreData(
          storeDir,
          storeName,
          storePass,
          ecKeyAlias,
          ecKeyPass,
          ecKeyId);
  private final IdpKeyStoreData rsaIdpKeyStoreData = new IdpKeyStoreData(
          storeDir,
          storeName,
          storePass,
          rsaKeyAlias,
          rsaKeyPass,
          rsaKeyId);

  private final File keyStoreDir = new File(storeDir);
  private final File keyStoreFile = new File(storeDir + "/" + storeName);

  @BeforeAll
  public void setupKeystores() {
    if (!keyStoreDir.exists()) {
      keyStoreDir.mkdir();
    }
    Assertions.assertTrue(
            IdpKeyStoreAccessor.prepareKeyStoreAndSet(JWSAlgorithm.ES256, ecIdpKeyStoreData));
    Assertions.assertTrue(
            IdpKeyStoreAccessor.prepareKeyStoreAndSet(JWSAlgorithm.RS384, rsaIdpKeyStoreData));
  }

  @AfterAll
  public void cleanupKeystores() {
    keyStoreFile.delete();
    keyStoreDir.delete();
  }

  @Test
  @Order(1)
  public void testKeystoreExists() {
    Assertions.assertTrue(keyStoreFile.exists());
  }

  @Test
  @Order(2)
  public void testEcKey() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    Assertions.assertInstanceOf(
            ECKey.class,
            IdpKeyStoreAccessor.accessKeyStore(JWSAlgorithm.ES256, ecIdpKeyStoreData));
  }

  @Test
  @Order(3)
  public void testRsaKey() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableEntryException, OperatorCreationException, IOException, JOSEException {
    Assertions.assertInstanceOf(
            RSAKey.class,
            IdpKeyStoreAccessor.accessKeyStore(JWSAlgorithm.RS384, rsaIdpKeyStoreData));
  }
}
