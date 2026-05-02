package com.yas.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.AccessDeniedException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.customer.model.UserAddress;
import com.yas.customer.repository.UserAddressRepository;
import com.yas.customer.viewmodel.address.ActiveAddressVm;
import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import com.yas.customer.viewmodel.useraddress.UserAddressVm;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

  @Mock
  private UserAddressRepository userAddressRepository;

  @Mock
  private LocationService locationService;

  private UserAddressService userAddressService;

  private static final String USER_ID = "user123";
  private static final String ANONYMOUS_USER = "anonymousUser";
  private static final Long ADDRESS_ID = 1L;

  @BeforeEach
  void setUp() {
    userAddressService = new UserAddressService(userAddressRepository, locationService);
  }

  private void setSecurityContext(String userId) {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        userId, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private AddressDetailVm createAddressDetailVm(Long id) {
    return new AddressDetailVm(id, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, "District 1", 1L, "State 1", 1L, "USA");
  }

  private UserAddress createUserAddress(Long id, Long addressId, Boolean isActive) {
    return UserAddress.builder()
        .id(id)
        .userId(USER_ID)
        .addressId(addressId)
        .isActive(isActive)
        .build();
  }

  // ===== getUserAddressList tests =====

  @Test
  void getUserAddressList_shouldReturnActiveAddresses_sortedByActive() {
    setSecurityContext(USER_ID);

    List<UserAddress> userAddresses = List.of(
        createUserAddress(1L, 1L, false),
        createUserAddress(2L, 2L, true)
    );
    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(userAddresses);

    List<AddressDetailVm> addressDetails = List.of(
        createAddressDetailVm(1L),
        createAddressDetailVm(2L)
    );
    when(locationService.getAddressesByIdList(anyList())).thenReturn(addressDetails);

    List<ActiveAddressVm> result = userAddressService.getUserAddressList();

    // Should be sorted with active first (reversed)
    assertThat(result).hasSize(2);
    assertThat(result.get(0).isActive()).isTrue();
    assertThat(result.get(1).isActive()).isFalse();
  }

  @Test
  void getUserAddressList_shouldThrowAccessDeniedException_whenAnonymousUser() {
    setSecurityContext(ANONYMOUS_USER);

    assertThrows(AccessDeniedException.class, () -> userAddressService.getUserAddressList());
  }

  @Test
  void getUserAddressList_shouldReturnEmptyList_whenNoAddresses() {
    setSecurityContext(USER_ID);

    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());
    when(locationService.getAddressesByIdList(anyList())).thenReturn(Collections.emptyList());

    List<ActiveAddressVm> result = userAddressService.getUserAddressList();

    assertThat(result).isEmpty();
  }

  @Test
  void getUserAddressList_shouldMatchAddressesById() {
    setSecurityContext(USER_ID);

    List<UserAddress> userAddresses = List.of(createUserAddress(1L, 10L, true));
    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(userAddresses);

    AddressDetailVm addressDetail = createAddressDetailVm(10L);
    when(locationService.getAddressesByIdList(anyList())).thenReturn(List.of(addressDetail));

    List<ActiveAddressVm> result = userAddressService.getUserAddressList();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).id()).isEqualTo(10L);
  }

  // ===== getAddressDefault tests =====

  @Test
  void getAddressDefault_shouldReturnDefaultAddress() {
    setSecurityContext(USER_ID);

    UserAddress userAddress = createUserAddress(1L, ADDRESS_ID, true);
    when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID))
        .thenReturn(Optional.of(userAddress));

    AddressDetailVm addressDetail = createAddressDetailVm(ADDRESS_ID);
    when(locationService.getAddressById(ADDRESS_ID)).thenReturn(addressDetail);

    AddressDetailVm result = userAddressService.getAddressDefault();

    assertThat(result.id()).isEqualTo(ADDRESS_ID);
  }

  @Test
  void getAddressDefault_shouldThrowAccessDeniedException_whenAnonymousUser() {
    setSecurityContext(ANONYMOUS_USER);

    assertThrows(AccessDeniedException.class, () -> userAddressService.getAddressDefault());
  }

  @Test
  void getAddressDefault_shouldThrowNotFoundException_whenNoDefaultAddress() {
    setSecurityContext(USER_ID);

    when(userAddressRepository.findByUserIdAndIsActiveTrue(USER_ID))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userAddressService.getAddressDefault());
  }

  // ===== createAddress tests =====

  @Test
  void createAddress_shouldCreateFirstAddress_asActive() {
    setSecurityContext(USER_ID);

    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

    AddressVm addressVm = new AddressVm(ADDRESS_ID, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);
    when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(addressVm);

    UserAddress savedUserAddress = createUserAddress(1L, ADDRESS_ID, true);
    when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedUserAddress);

    AddressPostVm addressPostVm = new AddressPostVm("Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);

    UserAddressVm result = userAddressService.createAddress(addressPostVm);

    assertThat(result.addressId()).isEqualTo(ADDRESS_ID);
    verify(userAddressRepository).save(any(UserAddress.class));
  }

  @Test
  void createAddress_shouldCreateSubsequentAddress_asInactive() {
    setSecurityContext(USER_ID);

    List<UserAddress> existingAddresses = List.of(createUserAddress(1L, 1L, true));
    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(existingAddresses);

    AddressVm addressVm = new AddressVm(2L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);
    when(locationService.createAddress(any(AddressPostVm.class))).thenReturn(addressVm);

    UserAddress savedUserAddress = createUserAddress(2L, 2L, false);
    when(userAddressRepository.save(any(UserAddress.class))).thenReturn(savedUserAddress);

    AddressPostVm addressPostVm = new AddressPostVm("Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);

    UserAddressVm result = userAddressService.createAddress(addressPostVm);

    assertThat(result.addressId()).isEqualTo(2L);
  }

  // ===== deleteAddress tests =====

  @Test
  void deleteAddress_shouldDeleteAddress() {
    setSecurityContext(USER_ID);

    UserAddress userAddress = createUserAddress(1L, ADDRESS_ID, true);
    when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, ADDRESS_ID))
        .thenReturn(userAddress);

    userAddressService.deleteAddress(ADDRESS_ID);

    verify(userAddressRepository).delete(userAddress);
  }

  @Test
  void deleteAddress_shouldThrowNotFoundException_whenAddressNotFound() {
    setSecurityContext(USER_ID);

    when(userAddressRepository.findOneByUserIdAndAddressId(USER_ID, ADDRESS_ID))
        .thenReturn(null);

    assertThrows(NotFoundException.class, () -> userAddressService.deleteAddress(ADDRESS_ID));
  }

  // ===== chooseDefaultAddress tests =====

  @Test
  void chooseDefaultAddress_shouldSetDefaultAddress() {
    setSecurityContext(USER_ID);

    List<UserAddress> userAddresses = List.of(
        createUserAddress(1L, 1L, false),
        createUserAddress(2L, 2L, false)
    );
    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(userAddresses);

    userAddressService.chooseDefaultAddress(2L);

    verify(userAddressRepository).saveAll(userAddresses);
  }

  @Test
  void chooseDefaultAddress_shouldHandleEmptyList() {
    setSecurityContext(USER_ID);

    when(userAddressRepository.findAllByUserId(USER_ID)).thenReturn(Collections.emptyList());

    userAddressService.chooseDefaultAddress(ADDRESS_ID);

    verify(userAddressRepository).saveAll(Collections.emptyList());
  }
}