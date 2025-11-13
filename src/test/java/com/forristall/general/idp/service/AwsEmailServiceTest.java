package com.forristall.general.idp.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.mail.SimpleMailMessage;

import com.forristall.general.idp.service.AwsEmailService;

/**
 * Testing class for testing methods used when sending an email through the AWS Simple Email Sender
 * (SES)
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@TestInstance(Lifecycle.PER_CLASS)
public class AwsEmailServiceTest {

  // The from email to be used for the service
  private final String fromAddress = "from@test.com";

  // The email service that will create and send email messages
  private AwsEmailService awsEmailService;

  /**
   * Setup the AWS SES service and set its from address since that is usually injected from the
   * properties file
   */
  @BeforeAll
  public void setup() {
    awsEmailService = new AwsEmailService();
    awsEmailService.fromAddress = fromAddress;
  }

  /**
   * Test creating a simple email message instance and assert that the main properties are assigned
   */
  @Test
  public void testCreateSimpleEmailMessage() {
    String toEmail = "to@test.com";
    String subject = "Test Subject";
    String body = "Testing the email service";
    SimpleMailMessage simpleMailMessage = awsEmailService
            .createSimpleMailMessage(toEmail, subject, body);
    Assertions.assertEquals(fromAddress, simpleMailMessage.getFrom());
    Assertions.assertEquals(toEmail, List.of(simpleMailMessage.getTo()).get(0));
    Assertions.assertEquals(subject, simpleMailMessage.getSubject());
    Assertions.assertEquals(body, simpleMailMessage.getText());
  }

}
