package com.forristall.general.idp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Spring configuration file for defining the server properties displayed in the OpenAPI (Swagger)
 * UI
 *
 * @author Robert Forristall (robert.s.forristall@gmail.com)
 */
@Configuration
public class OpenAPIConfiguration {

  /**
   * Bean function for defining the server properties for OpenAPI to reference
   * 
   * @return {@link OpenAPI}
   */
  @Bean
  public OpenAPI defineOpenApi() {
    Server server = new Server();
    server.setUrl("http://localhost:8080");
    server.setDescription("Development");

    Contact myContact = new Contact();
    myContact.setName("Robert Forristall");
    myContact.setEmail("robert.s.forristall@gmail.com");

    Info information = new Info().title("Forristall General IDP Server API")
            .version("1.0")
            .description(
                    "This API exposes endpoints to authenticate and authorize users using the OAuth2 standard")
            .contact(myContact);
    return new OpenAPI().info(information).servers(List.of(server));
  }
}
