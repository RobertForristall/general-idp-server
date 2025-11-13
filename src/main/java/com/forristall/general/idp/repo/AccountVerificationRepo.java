package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.AccountVerification;

public interface AccountVerificationRepo extends JpaRepository<AccountVerification, Long> {
  List<AccountVerification> findAccountVerificationByUserIdAndVerificationToken(
          Long userId,
          String verificationToken);
}
