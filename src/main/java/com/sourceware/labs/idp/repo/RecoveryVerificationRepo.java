package com.sourceware.labs.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sourceware.labs.idp.entity.RecoveryCode.RecoveryType;
import com.sourceware.labs.idp.entity.RecoveryVerification;

public interface RecoveryVerificationRepo extends JpaRepository<RecoveryVerification, Long> {
  List<RecoveryVerification> findRecoveryVerificationByUserIdAndRecoveryTypeAndVerificationToken(
          Long userId,
          RecoveryType recoveryType,
          String verificationToken);
}
