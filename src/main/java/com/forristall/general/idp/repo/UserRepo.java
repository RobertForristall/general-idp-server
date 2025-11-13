package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.RecoveryEmail;
import com.forristall.general.idp.entity.User;

/**
 * JPA repository for interacting with the User table and its linked tables
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
public interface UserRepo extends JpaRepository<User, Long> {
  List<User> findUserByEmailAndPassword(String email, String password);
  
  List<User> findUserByEmail(String email);
  
  User findUserByRecoveryEmail(RecoveryEmail recoveryEmail);
}
