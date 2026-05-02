package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yas.customer.config.KeycloakPropsConfig;
import com.yas.customer.viewmodel.customer.CustomerPostVm;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for CustomerService.
 * Uses PostgreSQL testcontainer and mocked Keycloak to test the service layer.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.liquibase.enabled=false",
    "keycloak.auth-server-url=http://localhost:8080",
    "keycloak.realm=Yas",
    "keycloak.resource=customer-management"
})
@ContextConfiguration(classes = {CustomerServiceIT.KeycloakMockConfig.class})
class CustomerServiceIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16");

  @Autowired
  private CustomerService customerService;

  @Autowired
  private Keycloak keycloak;

  private RealmResource realmResource;
  private UsersResource usersResource;

  @Configuration
  static class KeycloakMockConfig {
    @Bean
    @Primary
    public Keycloak keycloak() {
      Keycloak mockKeycloak = mock(Keycloak.class);
      RealmResource realmResource = mock(RealmResource.class);
      UsersResource usersResource = mock(UsersResource.class);

      when(mockKeycloak.realm(any(String.class))).thenReturn(realmResource);
      when(realmResource.users()).thenReturn(usersResource);

      // Setup default response for search
      when(usersResource.search(any(String.class), any(Boolean.class)))
          .thenReturn(Collections.emptyList());
      when(usersResource.search(any(), any(), any(), any(String.class), any(), any()))
          .thenReturn(Collections.emptyList());

      return mockKeycloak;
    }

    @Bean
    @Primary
    public KeycloakPropsConfig keycloakPropsConfig() {
      KeycloakPropsConfig config = mock(KeycloakPropsConfig.class);
      when(config.getRealm()).thenReturn("Yas");
      return config;
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
      Jwt mockJwt = Jwt.withTokenValue("mock-token")
          .header("alg", "RS256")
          .claim("sub", "test-user")
          .claim("scope", "read write")
          .build();
      return token -> mockJwt;
    }
  }

  @BeforeEach
  void setUp() {
    realmResource = mock(RealmResource.class);
    usersResource = mock(UsersResource.class);
    when(keycloak.realm(any(String.class))).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);
  }

  @Test
  void customerService_shouldBeLoaded() {
    assertNotNull(customerService);
  }

  @Test
  void getCustomers_shouldReturnCustomerListVm_whenNoUsers() {
    when(usersResource.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

    var result = customerService.getCustomers(1);

    assertNotNull(result);
    assertTrue(result.totalUser() >= 0);
  }

  @Test
  void createPasswordCredentials_shouldCreateValidCredentials() {
    var credentials = CustomerService.createPasswordCredentials("testPassword");

    assertNotNull(credentials);
    assertTrue(credentials.getType().equals("password"));
    assertTrue(credentials.getValue().equals("testPassword"));
    assertTrue(credentials.isTemporary() == false);
  }
}