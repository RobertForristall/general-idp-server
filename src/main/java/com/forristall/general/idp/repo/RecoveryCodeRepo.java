package com.forristall.general.idp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.RecoveryCode;

public interface RecoveryCodeRepo extends JpaRepository<RecoveryCode, Long> {

}
