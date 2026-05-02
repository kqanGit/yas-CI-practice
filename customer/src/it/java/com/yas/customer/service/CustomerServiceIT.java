package com.yas.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.ForbiddenException;
import com.yas.customer.config.KeycloakPropsConfig;
import com.yas.customer.viewmodel.customer.CustomerAdminVm;
import com.yas.customer.viewmodel.customer.CustomerListVm;
import com.yas.customer.viewmodel.customer.CustomerProfileRequestVm;
import com.yas.customer.viewmodel.customer.CustomerVm;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * Integration tests for CustomerService.
 */
class CustomerServiceIT {

  private static final String REALM_NAME = "Yas";
  private static final String USER_NAME = "test-user";
  private static final String VALID_EMAIL = "test@example.com";

  private Keycloak keycloak;
  private KeycloakPropsConfig keycloakPropsConfig;
  private RealmResource realmResource;
  private UsersResource usersResource;
  private CustomerService customerService;

  @BeforeEach
  void setUp() {
    keycloak = mock(Keycloak.class);
    keycloakPropsConfig = mock(KeycloakPropsConfig.class);
    realmResource = mock(RealmResource.class);
    usersResource = mock(UsersResource.class);

    when(keycloakPropsConfig.getRealm()).thenReturn(REALM_NAME);
    when(keycloak.realm(REALM_NAME)).thenReturn(realmResource);
    when(realmResource.users()).thenReturn(usersResource);

    customerService = new CustomerService(keycloak, keycloakPropsConfig);
  }

  private UserRepresentation createUserRepresentation(String id, String username,
      String email, String firstName, String lastName, boolean enabled) {
    UserRepresentation user = new UserRepresentation();
    user.setId(id);
    user.setUsername(username);
    user.setEmail(email);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEnabled(enabled);
    user.setCreatedTimestamp(946684800000L);
    return user;
  }

  @Test
  void getCustomers_shouldReturnCustomerListVm_whenNoUsers() {
    when(usersResource.search(any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

    CustomerListVm result = customerService.getCustomers(1);

    assertNotNull(result);
    assertThat(result.totalUser()).isZero();
    assertThat(result.totalPage()).isZero();
  }

  @Test
  void getCustomers_shouldReturnCustomerList_whenUsersExist() {
    List<UserRepresentation> users = List.of(
        createUserRepresentation("1", "user1", "user1@test.com", "First1", "Last1", true),
        createUserRepresentation("2", "user2", "user2@test.com", "First2", "Last2", true)
    );
    when(usersResource.search(any(), anyInt(), anyInt())).thenReturn(users);

    CustomerListVm result = customerService.getCustomers(1);

    assertNotNull(result);
    assertThat(result.totalUser()).isEqualTo(2);
    assertThat(result.customers()).hasSize(2);
  }

  @Test
  void getCustomerByEmail_shouldReturnCustomer_whenUserExists() {
    List<UserRepresentation> users = List.of(
        createUserRepresentation("1", "user1", VALID_EMAIL, "First", "Last", true)
    );
    when(usersResource.search(VALID_EMAIL, true)).thenReturn(users);

    CustomerAdminVm result = customerService.getCustomerByEmail(VALID_EMAIL);

    assertNotNull(result);
    assertThat(result.email()).isEqualTo(VALID_EMAIL);
    assertThat(result.id()).isEqualTo("1");
  }

  @Test
  void getCustomerProfile_shouldReturnCustomerVm_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "test@test.com", "John", "Doe", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    CustomerVm result = customerService.getCustomerProfile(USER_NAME);

    assertNotNull(result);
    assertThat(result.firstName()).isEqualTo("John");
    assertThat(result.lastName()).isEqualTo("Doe");
  }

  @Test
  void updateCustomer_shouldUpdateUser_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "old@test.com", "OldFirst", "OldLast", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    CustomerProfileRequestVm request = new CustomerProfileRequestVm("NewFirst", "NewLast", "new@test.com");
    customerService.updateCustomer(USER_NAME, request);

    verify(userResource).update(any(UserRepresentation.class));
  }

  @Test
  void deleteCustomer_shouldDisableUser_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "test@test.com", "First", "Last", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    customerService.deleteCustomer(USER_NAME);

    verify(userResource).update(any(UserRepresentation.class));
  }

  @Test
  void createPasswordCredentials_shouldCreateValidCredentials() {
    var credentials = CustomerService.createPasswordCredentials("testPassword");

    assertNotNull(credentials);
    assertThat(credentials.getType()).isEqualTo("password");
    assertThat(credentials.getValue()).isEqualTo("testPassword");
  }
}