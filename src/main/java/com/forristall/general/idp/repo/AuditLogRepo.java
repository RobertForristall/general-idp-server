package com.forristall.general.idp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.AuditLog;

public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {

}
