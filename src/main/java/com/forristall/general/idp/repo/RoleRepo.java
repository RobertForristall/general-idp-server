package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.Role;
import com.forristall.general.idp.entity.Role.Application;
import com.forristall.general.idp.entity.Role.RoleName;

public interface RoleRepo extends JpaRepository<Role, Long> {
  List<Role> findRoleByApplicationAndRole(Application application, RoleName role);
}
