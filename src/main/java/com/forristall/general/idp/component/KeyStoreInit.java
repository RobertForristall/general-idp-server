package com.forristall.general.idp.component;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.forristall.general.idp.keystore.IdpKeyStoreAccessor;
import com.forristall.general.idp.keystore.IdpKeyStoreData;
import com.nimbusds.jose.JWSAlgorithm;

import jakarta.annotation.PostConstruct;

@Component
public class KeyStoreInit {

  @Value("${keystore.dir.name}")
  private String keyStoreDir;

  @Value("${keystore.store.name}")
  private String keyStoreName;

  @Value("${keystore.store.password}")
  private String keyStorePassword;

  @Value("${keystore.key.token.alias}")
  private String tokenAlias;

  @Value("${keystore.key.token.password}")
  private String tokenPassword;

  @PostConstruct
  private void createKeystoreAndKey() {
    IdpKeyStoreData keyStoreData = new IdpKeyStoreData(
            keyStoreDir,
            keyStoreName,
            keyStorePassword,
            tokenAlias,
            tokenPassword,
            "initial");
    File dir = new File(keyStoreDir);
    if (!dir.exists()) {
      dir.mkdir();
    }
    IdpKeyStoreAccessor.prepareKeyStoreAndSet(JWSAlgorithm.ES256, keyStoreData);
  }

}
