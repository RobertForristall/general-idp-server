package com.forristall.general.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.forristall.general.idp.entity.SecurityQuestion;
import com.forristall.general.idp.entity.User;

public interface SecurityQuestionRepo extends JpaRepository<SecurityQuestion, Long> {
  List<SecurityQuestion> findSecurityQuestionByUser(User user);
}
