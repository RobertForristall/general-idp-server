package com.forristall.general.idp.keystore;

import java.util.Objects;

import com.google.gson.Gson;

/**
 * Class for holding all of the data needed to interact with the keystore
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public class IdpKeyStoreData {

  private String storeDir;
  private String storeFileName;
  private String storePassword;
  private String keyAlias;
  private String keyPassword;
  private String keyId;

  public IdpKeyStoreData(
          String storeDir,
          String storeFileName,
          String storePassword,
          String keyAlias,
          String keyPassword,
          String keyId) {
    super();
    this.storeDir = storeDir;
    this.storeFileName = storeFileName;
    this.storePassword = storePassword;
    this.keyAlias = keyAlias;
    this.keyPassword = keyPassword;
    this.keyId = keyId;
  }

  public String getStoreDir() {
    return storeDir;
  }

  public String getStoreFileName() {
    return storeFileName;
  }

  public String getStorePassword() {
    return storePassword;
  }

  public String getKeyAlias() {
    return keyAlias;
  }

  public String getKeyPassword() {
    return keyPassword;
  }

  public String getKeyId() {
    return keyId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyAlias, keyId, keyPassword, storeDir, storeFileName, storePassword);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdpKeyStoreData other = (IdpKeyStoreData) obj;
    return Objects.equals(keyAlias, other.keyAlias) && Objects.equals(keyId, other.keyId)
            && Objects.equals(keyPassword, other.keyPassword)
            && Objects.equals(storeDir, other.storeDir)
            && Objects.equals(storeFileName, other.storeFileName)
            && Objects.equals(storePassword, other.storePassword);
  }

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
  
  

}
