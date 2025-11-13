package com.forristall.general.idp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import com.forristall.general.idp.entity.RecoveryCode.RecoveryType;

/**
 * Spring service class for sending mail using AWS Simple Email Service (SES)
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@Service
public class AwsEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AwsEmailService.class);

  // Inject the mail sender configured to use AWS
  @Autowired
  private MailSender mailSender;

  // Get the verified from email address to use for all sent messages
  @Value("${cloud.aws.email.from}")
  public String fromAddress;

  /**
   * Send a simple email message using AWS SES
   * 
   * @param simpleMailMessage - The message information to be sent
   */
  public void sendMessage(SimpleMailMessage simpleMailMessage) {
    LOGGER.info("Sending email to addresses: " + simpleMailMessage.getTo()[0]);
    this.mailSender.send(simpleMailMessage);
  }

  /**
   * Helper function for creating a simple email message
   * 
   * @param toAddress - The email address to send the message to
   * @param subject - The subject of the email
   * @param body - The body/text of the email
   * @return {@link SimpleMailMessage} configured using the provided parameters
   */
  public SimpleMailMessage createSimpleMailMessage(String toAddress, String subject, String body) {
    LOGGER.debug("Creating simple mail message...");
    SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setFrom(fromAddress);
    simpleMailMessage.setTo(toAddress);
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(body);
    return simpleMailMessage;
  }

  public String createVerificatioEmailBody(Long userId, String verificationToken) {
    StringBuilder b = new StringBuilder();
    b.append("<h1>Forristall General IDP</h1>\n");
    b.append(
            "<p>Click the following link to verify your account: http://localhost:8080/user/verify?userId="
                    + userId + "&verificationToken=" + verificationToken + "</p>\n");
    return b.toString();
  }
  
  public String createRecoveryVerificatioEmailBody(Long userId, String verificationToken, RecoveryType recoveryType) {
    StringBuilder b = new StringBuilder();
    b.append("<h1>Forristall General IDP</h1>\n");
    b.append(
            "<p>Click the following link to verify your recovery " + recoveryType.name()+ ": http://localhost:8080/recovery/verify?userId="
                    + userId + "&verificationToken=" + verificationToken + "&recoveryType=" + recoveryType.name() +"</p>\n");
    return b.toString();
  }

}
