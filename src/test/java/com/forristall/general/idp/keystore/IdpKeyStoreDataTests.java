package com.forristall.general.idp.keystore;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.forristall.general.idp.keystore.IdpKeyStoreData;

@TestInstance(Lifecycle.PER_CLASS)
public class IdpKeyStoreDataTests {

  private final String storeDir = "testKeyStoreDir";
  private final String storeName = "testKeyStoreName";
  private final String storePass = "testKeyStorePass";
  private final String keyAlias = "testKeyAlias";
  private final String keyPass = "testKeyPass";
  private final String keyId = "testKeyId";

  private final IdpKeyStoreData idpKeyStoreData = new IdpKeyStoreData(
          storeDir,
          storeName,
          storePass,
          keyAlias,
          keyPass,
          keyId);

  @Test
  public void testStoreDir() {
    Assertions.assertEquals(storeDir, idpKeyStoreData.getStoreDir());
  }

  @Test
  public void testStoreName() {
    Assertions.assertEquals(storeName, idpKeyStoreData.getStoreFileName());
  }

  @Test
  public void testSorePass() {
    Assertions.assertEquals(storePass, idpKeyStoreData.getStorePassword());
  }

  @Test
  public void testKeyAlias() {
    Assertions.assertEquals(keyAlias, idpKeyStoreData.getKeyAlias());
  }

  @Test
  public void testKeyPass() {
    Assertions.assertEquals(keyPass, idpKeyStoreData.getKeyPassword());
  }

  @Test
  public void testKeyId() {
    Assertions.assertEquals(keyId, idpKeyStoreData.getKeyId());
  }

}
