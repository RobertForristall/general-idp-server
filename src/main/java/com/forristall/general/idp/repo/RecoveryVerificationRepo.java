package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.RecoveryVerification;
import com.forristall.general.idp.entity.RecoveryCode.RecoveryType;

public interface RecoveryVerificationRepo extends JpaRepository<RecoveryVerification, Long> {
  List<RecoveryVerification> findRecoveryVerificationByUserIdAndRecoveryTypeAndVerificationToken(
          Long userId,
          RecoveryType recoveryType,
          String verificationToken);
}
