package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.RecoveryEmail;

public interface RecoveryEmailRepo extends JpaRepository<RecoveryEmail, Long> {

  List<RecoveryEmail> findRecoveryEmailByEmail(String email);
  
}
