package com.forristall.general.idp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;

import io.awspring.cloud.ses.SimpleEmailServiceJavaMailSender;
import io.awspring.cloud.ses.SimpleEmailServiceMailSender;

/**
 * Spring configuration file for AWS Simple Email Service (SES) which contains the injected
 * access-key, secret-key, and region information used to connect the service to AWS
 * 
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@Configuration
public class AwsSesConfig {

  /**
   * AWS access key for the IAM user that will be sending emails
   */
  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  /**
   * AWS secret key for the IAM user that will be sending emails
   */
  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  /**
   * AWS region that the IAM user will be connecting to
   */
  @Value("${cloud.aws.region.static}")
  private String region;

  /**
   * Bean function for creating a new AWS SES Java instance
   * 
   * @return {@link AmazonSimpleEmailService}
   */
  @Bean
  public AmazonSimpleEmailService amazonSimpleEmailService() {
    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    return AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build();
  }

  /**
   * Bean function for creating a new mail sender
   * 
   * @param amazonSimpleEmailService - the AWS SES service that the mail sender will use
   * @return {@link MailSender}
   */
  @Bean
  public MailSender mailSender(AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceMailSender(amazonSimpleEmailService);
  }

  /**
   * Bean function for creating a new Java mail sender
   * 
   * @param amazonSimpleEmailService - the AWS SES service that the mail sender will use
   * @return {@link JavaMailSender}
   */
  @Bean
  public JavaMailSender javaMailSender(AmazonSimpleEmailService amazonSimpleEmailService) {
    return new SimpleEmailServiceJavaMailSender(amazonSimpleEmailService);
  }

}
