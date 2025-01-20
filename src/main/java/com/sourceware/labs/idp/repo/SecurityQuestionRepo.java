package com.sourceware.labs.idp.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sourceware.labs.idp.entity.SecurityQuestion;
import com.sourceware.labs.idp.entity.User;

public interface SecurityQuestionRepo extends JpaRepository<SecurityQuestion, Long> {
  List<SecurityQuestion> findSecurityQuestionByUser(User user);
}
