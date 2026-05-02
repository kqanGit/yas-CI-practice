package com.yas.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.DuplicatedException;
import com.yas.commonlibrary.exception.ForbiddenException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.WrongEmailFormatException;
import com.yas.customer.config.KeycloakPropsConfig;
import com.yas.customer.viewmodel.customer.CustomerAdminVm;
import com.yas.customer.viewmodel.customer.CustomerListVm;
import com.yas.customer.viewmodel.customer.CustomerPostVm;
import com.yas.customer.viewmodel.customer.CustomerProfileRequestVm;
import com.yas.customer.viewmodel.customer.CustomerVm;
import com.yas.customer.viewmodel.customer.GuestUserVm;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

class CustomerServiceTest {

  private static final String REALM_NAME = "test-realm";
  private static final String USER_NAME = "test-username";
  private static final String VALID_EMAIL = "valid@example.com";
  private static final String ACCESS_DENIED_MESSAGE = "Access denied";

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

  private UserRepresentation createUserRepresentation(String id, String username, String email,
      String firstName, String lastName, boolean enabled) {
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

  // ===== getCustomers tests =====

  @Test
  void getCustomers_shouldReturnCustomerListVm_whenUsersExist() {
    List<UserRepresentation> users = List.of(
        createUserRepresentation("1", "user1", "user1@example.com", "First1", "Last1", true),
        createUserRepresentation("2", "user2", "user2@example.com", "First2", "Last2", true)
    );
    when(usersResource.search(any(), anyInt(), anyInt())).thenReturn(users);

    CustomerListVm result = customerService.getCustomers(1);

    assertThat(result.totalUser()).isEqualTo(2);
    assertThat(result.totalPage()).isEqualTo(1);
    assertThat(result.customers()).hasSize(2);
  }

  @Test
  void getCustomers_shouldReturnEmptyList_whenNoUsersExist() {
    when(usersResource.search(any(), anyInt(), anyInt())).thenReturn(List.of());

    CustomerListVm result = customerService.getCustomers(1);

    assertThat(result.totalUser()).isZero();
    assertThat(result.totalPage()).isZero();
    assertThat(result.customers()).isEmpty();
  }

  @Test
  void getCustomers_shouldThrowAccessDeniedException_whenForbiddenException() {
    when(usersResource.search(any(), anyInt(), anyInt()))
        .thenThrow(new ForbiddenException(ACCESS_DENIED_MESSAGE));

    assertThrows(AccessDeniedException.class, () -> customerService.getCustomers(1));
  }

  // ===== getCustomerByEmail tests =====

  @Test
  void getCustomerByEmail_shouldReturnCustomerAdminVm_whenUserExists() {
    List<UserRepresentation> users = List.of(
        createUserRepresentation("1", "user1", VALID_EMAIL, "First", "Last", true)
    );
    when(usersResource.search(VALID_EMAIL, true)).thenReturn(users);

    CustomerAdminVm result = customerService.getCustomerByEmail(VALID_EMAIL);

    assertThat(result.email()).isEqualTo(VALID_EMAIL);
    assertThat(result.id()).isEqualTo("1");
    assertThat(result.username()).isEqualTo("user1");
  }

  @Test
  void getCustomerByEmail_shouldThrowNotFoundException_whenUserNotFound() {
    when(usersResource.search(VALID_EMAIL, true)).thenReturn(List.of());

    assertThrows(NotFoundException.class, () -> customerService.getCustomerByEmail(VALID_EMAIL));
  }

  @Test
  void getCustomerByEmail_shouldThrowWrongEmailFormatException_whenEmailInvalid() {
    assertThrows(WrongEmailFormatException.class,
        () -> customerService.getCustomerByEmail("invalid-email"));
  }

  @Test
  void getCustomerByEmail_shouldThrowAccessDeniedException_whenForbiddenException() {
    when(usersResource.search(VALID_EMAIL, true))
        .thenThrow(new ForbiddenException(ACCESS_DENIED_MESSAGE));

    assertThrows(AccessDeniedException.class, () -> customerService.getCustomerByEmail(VALID_EMAIL));
  }

  // ===== getCustomerProfile tests =====

  @Test
  void getCustomerProfile_shouldReturnCustomerVm_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "test@example.com",
        "John", "Doe", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    CustomerVm result = customerService.getCustomerProfile(USER_NAME);

    assertThat(result.firstName()).isEqualTo("John");
    assertThat(result.lastName()).isEqualTo("Doe");
    assertThat(result.email()).isEqualTo("test@example.com");
  }

  @Test
  void getCustomerProfile_shouldReturnNullFields_whenUserDoesNotExist() {
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(null);

    CustomerVm result = customerService.getCustomerProfile(USER_NAME);

    assertThat(result.firstName()).isNull();
    assertThat(result.lastName()).isNull();
    assertThat(result.email()).isNull();
  }

  @Test
  void getCustomerProfile_shouldThrowAccessDeniedException_whenForbiddenException() {
    when(usersResource.get(USER_NAME))
        .thenThrow(new ForbiddenException(ACCESS_DENIED_MESSAGE));

    assertThrows(AccessDeniedException.class, () -> customerService.getCustomerProfile(USER_NAME));
  }

  // ===== updateCustomer tests =====

  @Test
  void updateCustomer_shouldUpdateUser_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "old@example.com",
        "OldFirst", "OldLast", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    CustomerProfileRequestVm request = new CustomerProfileRequestVm("NewFirst", "NewLast", "new@example.com");
    customerService.updateCustomer(USER_NAME, request);

    verify(userResource).update(any(UserRepresentation.class));
  }

  @Test
  void updateCustomer_shouldThrowNotFoundException_whenUserNotFound() {
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(null);

    CustomerProfileRequestVm request = new CustomerProfileRequestVm("NewFirst", "NewLast", "new@example.com");
    assertThrows(NotFoundException.class, () -> customerService.updateCustomer(USER_NAME, request));
  }

  // ===== deleteCustomer tests =====

  @Test
  void deleteCustomer_shouldDisableUser_whenUserExists() {
    UserRepresentation user = createUserRepresentation(USER_NAME, USER_NAME, "test@example.com",
        "First", "Last", true);
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(user);

    customerService.deleteCustomer(USER_NAME);

    verify(userResource).update(any(UserRepresentation.class));
  }

  @Test
  void deleteCustomer_shouldThrowNotFoundException_whenUserNotFound() {
    UserResource userResource = mock(UserResource.class);
    when(usersResource.get(USER_NAME)).thenReturn(userResource);
    when(userResource.toRepresentation()).thenReturn(null);

    assertThrows(NotFoundException.class, () -> customerService.deleteCustomer(USER_NAME));
  }

  // ===== createGuestUser tests =====

  @Test
  void createGuestUser_shouldCreateGuestUser() throws Exception {
    Response response = mock(Response.class);
    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
    when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
    when(response.getLocation()).thenReturn(new java.net.URI("/test/1"));

    UserResource userResource = mock(UserResource.class);
    when(usersResource.get("1")).thenReturn(userResource);

    RolesResource rolesResource = mock(RolesResource.class);
    when(realmResource.roles()).thenReturn(rolesResource);
    RoleResource roleResource = mock(RoleResource.class);
    when(rolesResource.get("GUEST")).thenReturn(roleResource);
    when(roleResource.toRepresentation()).thenReturn(mock(RoleRepresentation.class));

    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(roleMappingResource.realmLevel()).thenReturn(mock(org.keycloak.admin.client.resource.RoleScopeResource.class));

    GuestUserVm result = customerService.createGuestUser();

    assertThat(result.userId()).isEqualTo("1");
    assertThat(result.email()).contains("_guest@yas.com");
    assertThat(result.password()).isEqualTo("GUEST");
  }

  // ===== create (customer) tests =====

  @Test
  void create_shouldCreateCustomer_whenValidInput() throws Exception {
    CustomerPostVm customerPostVm = new CustomerPostVm("newuser", "newuser@test.com",
        "John", "Doe", "password123", "ADMIN");

    when(realmResource.users().search(anyString(), any(Boolean.class)))
        .thenReturn(Collections.emptyList());
    when(realmResource.users().search(any(), any(), any(), anyString(), any(), any()))
        .thenReturn(Collections.emptyList());

    Response response = mock(Response.class);
    when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
    when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);
    when(response.getLocation()).thenReturn(new java.net.URI("/test/1"));

    UserResource userResource = mock(UserResource.class);
    when(usersResource.get("1")).thenReturn(userResource);

    RolesResource rolesResource = mock(RolesResource.class);
    when(realmResource.roles()).thenReturn(rolesResource);
    RoleResource roleResource = mock(RoleResource.class);
    when(rolesResource.get("ADMIN")).thenReturn(roleResource);
    when(roleResource.toRepresentation()).thenReturn(mock(RoleRepresentation.class));

    RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
    when(userResource.roles()).thenReturn(roleMappingResource);
    when(roleMappingResource.realmLevel()).thenReturn(mock(org.keycloak.admin.client.resource.RoleScopeResource.class));

    CustomerVm result = customerService.create(customerPostVm);

    assertThat(result.username()).isEqualTo("newuser");
    assertThat(result.email()).isEqualTo("newuser@test.com");
  }

  @Test
  void create_shouldThrowDuplicatedException_whenUsernameExists() {
    CustomerPostVm customerPostVm = new CustomerPostVm("existinguser", "new@test.com",
        "John", "Doe", "password123", "ADMIN");

    when(realmResource.users().search(anyString(), any(Boolean.class)))
        .thenReturn(Collections.singletonList(mock(UserRepresentation.class)));

    assertThrows(DuplicatedException.class, () -> customerService.create(customerPostVm));
  }

  @Test
  void create_shouldThrowDuplicatedException_whenEmailExists() {
    CustomerPostVm customerPostVm = new CustomerPostVm("newuser", "existing@test.com",
        "John", "Doe", "password123", "ADMIN");

    when(realmResource.users().search(anyString(), any(Boolean.class)))
        .thenReturn(Collections.emptyList());
    when(realmResource.users().search(any(), any(), any(), anyString(), any(), any()))
        .thenReturn(Collections.singletonList(mock(UserRepresentation.class)));

    assertThrows(DuplicatedException.class, () -> customerService.create(customerPostVm));
  }

  // ===== createPasswordCredentials tests =====

  @Test
  void createPasswordCredentials_shouldCreateCredentials() {
    var result = CustomerService.createPasswordCredentials("testpassword");

    assertThat(result.getType()).isEqualTo("password");
    assertThat(result.getValue()).isEqualTo("testpassword");
    assertThat(result.isTemporary()).isFalse();
  }
}