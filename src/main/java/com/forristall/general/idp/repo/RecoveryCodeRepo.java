package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.RecoveryCode;

public interface RecoveryCodeRepo extends JpaRepository<RecoveryCode, Long> {

  List<RecoveryCode> findRecoveryCodeByCode(String code);
  
}
