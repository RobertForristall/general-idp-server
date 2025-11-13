package com.forristall.general.idp.component;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.forristall.general.idp.entity.Role;
import com.forristall.general.idp.entity.Role.Application;
import com.forristall.general.idp.entity.Role.RoleName;
import com.forristall.general.idp.repo.RoleRepo;

import jakarta.annotation.PostConstruct;

@Component
public class DatabaseInit {

  @Autowired
  private RoleRepo roleRepo;

  @PostConstruct
  private void loadInitialData() throws ParseException {

    Role adminRole = new Role();
    adminRole.setApplication(Application.RealQuick);
    adminRole.setRole(RoleName.Admin);
    adminRole.setRoleDescription("Admin user for the RealQuick application");

    Role role = new Role();
    role.setApplication(Application.RealQuick);
    role.setRole(RoleName.User);
    role.setRoleDescription("Standard user for the RealQuick application");

    roleRepo.save(adminRole);
    roleRepo.save(role);

  }

}
